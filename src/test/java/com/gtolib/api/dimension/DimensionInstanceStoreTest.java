package com.gtolib.api.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.gto.datasynclib.datastream.data.StringData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 隔离临时目录中的实例持久化测试，覆盖身份、分页和有界缓存、事务恢复、冻结定义、长 JSON、系列地址及种子稳定性。
 */
class DimensionInstanceStoreTest {

    static {
        net.minecraft.SharedConstants.tryDetectVersion();
        // Plain JUnit lacks Forge's event-class transformer; bootstrap its network listener lists first.
        try {
            var listeners = net.minecraftforge.eventbus.api.EventListenerHelper.class.getDeclaredMethod("getListenerListInternal", Class.class, boolean.class);
            listeners.setAccessible(true);
            listeners.invoke(null, net.minecraftforge.network.NetworkEvent.class, true);
            for (var event : net.minecraftforge.network.NetworkEvent.class.getDeclaredClasses()) {
                if (!net.minecraftforge.eventbus.api.Event.class.isAssignableFrom(event)) continue;
                var parent = event.getSuperclass();
                if (parent != net.minecraftforge.network.NetworkEvent.class && parent != net.minecraftforge.eventbus.api.Event.class) listeners.invoke(null, parent, true);
                listeners.invoke(null, event, true);
            }
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @TempDir
    Path directory;
    private static final ResourceLocation TEMPLATE = new ResourceLocation("gtocore", "test");
    private static final OwnerRef OWNER = new OwnerRef(OwnerRef.Kind.PLAYER, UUID.fromString("43265327-df96-48f3-81cb-130ca1dff314"));
    private static final ResolvedTemplate DEFINITION = new ResolvedTemplate(TEMPLATE, 1, "{\"generator\":\"frozen\"}", DimensionTemplate.SpawnPolicy.VOID_PLATFORM, DimensionEnvironment.VOID);

    @Test
    void logicalIdentityIncludesOwnerKindAndSlotButNotTemplateVersion() {
        String personal = InstanceKey.privateKey(OWNER, TEMPLATE, "main");
        String team = InstanceKey.privateKey(new OwnerRef(OwnerRef.Kind.TEAM, OWNER.id()), TEMPLATE, "main");
        assertNotEquals(InstanceKey.id(personal), InstanceKey.id(team));
        assertNotEquals(InstanceKey.id(personal), InstanceKey.id(InstanceKey.privateKey(OWNER, TEMPLATE, "main/guest")));
        var upgraded = new ResolvedTemplate(TEMPLATE, 2, "different", DimensionTemplate.SpawnPolicy.SURFACE, DimensionEnvironment.OVERWORLD);
        assertEquals(new InstanceDescriptor(personal, 42, DEFINITION, OWNER, InstanceDescriptor.AccessPolicy.OWNER, 0).id(),
                new InstanceDescriptor(personal, 42, upgraded, OWNER, InstanceDescriptor.AccessPolicy.OWNER, 0).id());
    }

    @Test
    void pagingAndColdLookupsHaveABoundedCacheAndRetainFullDefinition() throws Exception {
        var store = new InstanceStore(directory, 2);
        for (int i = 0; i < 512; i++) store.create(descriptor(i));
        assertEquals(512, store.count());
        assertEquals(2, store.cachedCount());
        var descriptor = store.find(descriptor(3).id());
        descriptor.setSpawn(new BlockPos(17, 65, -9), 42);
        descriptor.grant(OWNER.id());
        store.save(descriptor);
        var restarted = new InstanceStore(directory, 2);
        assertEquals(0, restarted.cachedCount());
        var restored = restarted.find(descriptor.id());
        assertEquals(descriptor.seed(), restored.seed());
        assertEquals(DEFINITION, restored.template());
        assertEquals(descriptor.spawn(), restored.spawn());
        assertEquals(42, restored.spawnAngle());
        assertTrue(restored.isVisitor(OWNER.id()));
        var page = restarted.page(4, 3);
        assertEquals(3, page.size());
        assertEquals(descriptor(4).id(), page.getFirst().id());
        assertEquals(2, restarted.cachedCount());
        assertTrue(restarted.page(1000, 10).isEmpty());
        assertFalse(Files.exists(directory.resolve("dimensions")));
    }

    @Test
    void templateUpgradeRetainsExistingInstancesAndFailedCatalogWritesRollBack() throws Exception {
        Path catalogFile = directory.resolve("catalog.dat");
        var catalog = new DimensionCatalog(catalogFile);
        catalog.register(DEFINITION);
        var store = new InstanceStore(directory.resolve("instances"), 2);
        var original = descriptor(0);
        store.create(original);
        var upgraded = new ResolvedTemplate(TEMPLATE, 2, "updated", DimensionTemplate.SpawnPolicy.SURFACE, DimensionEnvironment.OVERWORLD);
        catalog.register(upgraded);
        assertEquals(upgraded, new DimensionCatalog(catalogFile).templates().get(TEMPLATE));
        assertEquals(DEFINITION, new InstanceStore(directory.resolve("instances"), 2).find(original.id()).template());
        Files.move(catalogFile, directory.resolve("catalog.before-failure.dat"));
        Files.createDirectory(catalogFile);
        Files.writeString(catalogFile.resolve("blocked"), "injected write failure");
        var next = new ResolvedTemplate(TEMPLATE, 3, "next", DimensionTemplate.SpawnPolicy.SURFACE, DimensionEnvironment.OVERWORLD);
        assertThrows(IOException.class, () -> catalog.register(next));
        assertEquals(upgraded, catalog.templates().get(TEMPLATE));
        var series = new SeriesDefinition(new ResourceLocation("gtocore", "failed_series"), upgraded, 42, null, InstanceDescriptor.AccessPolicy.PUBLIC);
        assertThrows(IOException.class, () -> catalog.createSeries(series));
        assertTrue(catalog.series().isEmpty());
        assertThrows(IOException.class, () -> catalog.setForced(original.dimension().location(), true));
        assertTrue(catalog.forcedDimensions().isEmpty());
    }

    @Test
    void interruptedIndexAppendRecoversFromOneJournalWithoutScanningHistory() throws Exception {
        var store = new InstanceStore(directory, 2);
        store.create(descriptor(0));
        DimensionDataIO.writeAtomic(directory.resolve("pending.dat"), DimensionDataIO.Kind.PENDING, descriptor(1).save());
        try (var index = new RandomAccessFile(directory.resolve("instances.index").toFile(), "rw")) {
            index.seek(index.length());
            index.writeInt(123);
        }
        var recovered = new InstanceStore(directory, 2);
        assertEquals(2, recovered.count());
        assertEquals(descriptor(1).id(), recovered.page(1, 1).getFirst().id());
        assertFalse(Files.exists(directory.resolve("pending.dat")));
        assertEquals(0x47544443, ByteBuffer.wrap(uncompressed(instancePath(descriptor(1).id()))).getInt());
        assertEquals(2, new InstanceStore(directory, 2).count());
    }

    @Test
    void seriesAddressesDoNotCreateFilesAndOverflowIsExplicit() throws Exception {
        var series = new SeriesDefinition(new ResourceLocation("gtocore", "series"), DEFINITION, 123, null, InstanceDescriptor.AccessPolicy.PUBLIC);
        var address = new SeriesAddress(series.id(), Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> address.offset(1));
        assertEquals(Long.MAX_VALUE - 1, address.offset(-1).index());
        assertNotEquals(series.seed(0), series.seed(1));
        assertEquals(series.seed(Long.MIN_VALUE), SeriesDefinition.load(series.save()).seed(Long.MIN_VALUE));
        try (var files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }

    private static InstanceDescriptor descriptor(int index) {
        return new InstanceDescriptor(InstanceKey.privateKey(OWNER, TEMPLATE, "slot-" + index), 42L + index, DEFINITION, OWNER, InstanceDescriptor.AccessPolicy.OWNER, index);
    }

    @Test
    void fullGenerationDefinitionsSupportMoreThan64KiBOfUtf8() throws Exception {
        String generation = "生成定义".repeat(30000);
        var full = new ResolvedTemplate(TEMPLATE, 1, generation, DimensionTemplate.SpawnPolicy.SURFACE, DimensionEnvironment.OVERWORLD);
        var instance = new InstanceDescriptor(InstanceKey.privateKey(OWNER, TEMPLATE, "large-definition"), 71, full, OWNER, InstanceDescriptor.AccessPolicy.OWNER, 0);
        var store = new InstanceStore(directory, 2);
        store.create(instance);
        assertEquals(generation, new InstanceStore(directory, 2).find(instance.id()).template().generationDefinition());
    }

    @ParameterizedTest
    @ValueSource(strings = { "CATALOG", "INSTANCE", "PENDING" })
    void unsupportedDimensionFormatsFailWithoutRewriting(String name) throws Exception {
        var kind = DimensionDataIO.Kind.valueOf(name);
        var file = directory.resolve("unsupported.dat");
        try (var output = new GZIPOutputStream(Files.newOutputStream(file))) {
            output.write(new byte[] { 10, 0, 0, 0 });
        }
        byte[] original = Files.readAllBytes(file);
        assertThrows(IOException.class, () -> DimensionDataIO.read(file, kind));
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    void newPendingRecoveryIsIdempotentAtEveryTransactionBoundary(int stage) throws Exception {
        var store = new InstanceStore(directory, 2);
        store.create(descriptor(0));
        var next = descriptor(1);
        DimensionDataIO.writeAtomic(directory.resolve("pending.dat"), DimensionDataIO.Kind.PENDING, next.save());
        if (stage >= 1) DimensionDataIO.writeAtomic(instancePath(next.id()), DimensionDataIO.Kind.INSTANCE, next.save());
        if (stage >= 2) {
            try (var index = new RandomAccessFile(directory.resolve("instances.index").toFile(), "rw")) {
                index.seek(index.length());
                if (stage == 2) index.writeInt(123);
                else {
                    index.writeLong(next.id().getMostSignificantBits());
                    index.writeLong(next.id().getLeastSignificantBits());
                }
            }
        }
        var recovered = new InstanceStore(directory, 2);
        assertEquals(2, recovered.count());
        assertEquals(next.id(), recovered.page(1, 1).getFirst().id());
        assertEquals(32, Files.size(directory.resolve("instances.index")));
        assertFalse(Files.exists(directory.resolve("pending.dat")));
        assertEquals(2, new InstanceStore(directory, 2).count());
    }

    @ParameterizedTest
    @ValueSource(strings = { "kind", "storage", "schema", "length", "truncated", "trailing", "data_trailing", "missing_seed", "invalid_seed", "identity", "owner", "access", "environment", "template_version" })
    void corruptNewDescriptorsFailWithoutReplacement(String corruption) throws Exception {
        var original = descriptor(0);
        var store = new InstanceStore(directory, 2);
        store.create(original);
        var path = instancePath(original.id());
        var map = original.save();
        if (corruption.equals("missing_seed")) map.remove(DimensionDataComponents.SEED);
        if (corruption.equals("identity")) map.put(DimensionDataComponents.ID, UUID.randomUUID());
        if (corruption.equals("owner")) map.remove(DimensionDataComponents.OWNER);
        var data = DimensionDataComponents.INSTANCE.encode(map).asStringMapData();
        if (corruption.equals("access")) data.put("access", StringData.valueOf("invalid"));
        if (corruption.equals("invalid_seed")) data.put("seed", StringData.valueOf("0"));
        if (corruption.equals("environment")) data.getStringMap().get("template").asStringMapData().getStringMap().remove("environment");
        if (corruption.equals("template_version")) data.getStringMap().get("template").asStringMapData().putInt("version", 0);
        byte[] payload = data.writeToBytes();
        byte[] bytes = uncompressed(path);
        if (corruption.equals("kind")) bytes[4] = 3;
        else if (corruption.equals("storage")) ByteBuffer.wrap(bytes).putInt(5, 2);
        else if (corruption.equals("schema")) ByteBuffer.wrap(bytes).putInt(9, 99);
        else if (corruption.equals("length")) ByteBuffer.wrap(bytes).putInt(17, -1);
        else if (corruption.equals("truncated")) bytes = Arrays.copyOf(bytes, bytes.length - 1);
        else if (corruption.equals("trailing")) bytes = Arrays.copyOf(bytes, bytes.length + 1);
        else {
            if (corruption.equals("data_trailing")) payload = Arrays.copyOf(payload, payload.length + 1);
            bytes = Arrays.copyOf(bytes, 21 + payload.length);
            ByteBuffer.wrap(bytes).putInt(17, payload.length);
            System.arraycopy(payload, 0, bytes, 21, payload.length);
        }
        try (var output = new GZIPOutputStream(Files.newOutputStream(path))) {
            output.write(bytes);
        }
        byte[] corruptBytes = Files.readAllBytes(path);
        var restarted = new InstanceStore(directory, 2);
        assertThrows(IOException.class, () -> restarted.find(original.id()));
        assertEquals(0, restarted.cachedCount());
        assertEquals(1, restarted.count());
        assertArrayEquals(corruptBytes, Files.readAllBytes(path));
    }

    @Test
    void zeroAndExtremeSeedsAndOptionalFieldsSurviveActualBinaryRoundTrips() throws Exception {
        var store = new InstanceStore(directory, 2);
        long[] seeds = { 0, Long.MIN_VALUE, Long.MAX_VALUE };
        for (int i = 0; i < seeds.length; i++) {
            var record = new InstanceDescriptor(InstanceKey.seriesKey(new SeriesAddress(TEMPLATE, seeds[i])), seeds[i], DEFINITION, null, InstanceDescriptor.AccessPolicy.PUBLIC, i);
            store.create(record);
        }
        var page = new InstanceStore(directory, 2).page(0, 3);
        for (int i = 0; i < seeds.length; i++) {
            assertEquals(seeds[i], page.get(i).seed());
            assertEquals(i, page.get(i).ordinal());
            assertNull(page.get(i).owner());
            assertNull(page.get(i).spawn());
        }
    }

    private Path instancePath(UUID id) {
        return directory.resolve(id.toString().substring(0, 2)).resolve(id + ".dat");
    }

    private static byte[] uncompressed(Path path) throws Exception {
        try (var input = new GZIPInputStream(Files.newInputStream(path))) {
            return input.readAllBytes();
        }
    }
}
