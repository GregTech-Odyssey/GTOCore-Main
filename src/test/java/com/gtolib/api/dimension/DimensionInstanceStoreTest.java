package com.gtolib.api.dimension;

import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
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
        DimensionDataIO.writeAtomic(directory.resolve("pending.dat"), DimensionDataIO.Kind.PENDING, descriptor(1));
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
        var bytes = new ByteArrayOutputStream();
        try (var output = DataIOStream.of(bytes)) {
            DimensionDataCodecs.SERIES.encode(output, series);
        }
        try (var input = DataIOStream.of(bytes.toByteArray())) {
            assertEquals(series.seed(Long.MIN_VALUE), DimensionDataCodecs.SERIES.decode(input).seed(Long.MIN_VALUE));
        }
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
        DimensionDataIO.writeAtomic(directory.resolve("pending.dat"), DimensionDataIO.Kind.PENDING, next);
        if (stage >= 1) DimensionDataIO.writeAtomic(instancePath(next.id()), DimensionDataIO.Kind.INSTANCE, next);
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
    @ValueSource(strings = { "kind", "storage", "schema", "truncated" })
    void corruptNewDescriptorsFailWithoutReplacement(String corruption) throws Exception {
        var original = descriptor(0);
        var store = new InstanceStore(directory, 2);
        store.create(original);
        var path = instancePath(original.id());
        byte[] bytes = uncompressed(path);
        if (corruption.equals("kind")) bytes[4] = 3;
        else if (corruption.equals("storage")) ByteBuffer.wrap(bytes).putInt(5, 2);
        else if (corruption.equals("schema")) ByteBuffer.wrap(bytes).putInt(9, 99);
        else if (corruption.equals("truncated")) bytes = Arrays.copyOf(bytes, bytes.length - 1);
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

    private static byte[] fixture(String name) throws IOException {
        try (var input = DimensionInstanceStoreTest.class.getResourceAsStream("/saved-data/schema-2/" + name)) {
            return input.readAllBytes();
        }
    }

    @Test
    void schema2CatalogAndDescriptorMigrateFromFixedServerFiles() throws Exception {
        var catalogFile = directory.resolve("catalog.dat");
        byte[] originalCatalog = fixture("catalog.dat");
        Files.write(catalogFile, originalCatalog);
        var catalog = new DimensionCatalog(catalogFile);
        assertFalse(catalog.templates().isEmpty());
        assertFalse(catalog.series().isEmpty());
        assertArrayEquals(originalCatalog, Files.readAllBytes(catalogFile));
        assertEquals(catalog.templates(), new DimensionCatalog(catalogFile).templates());
        catalog.save();
        assertEquals(3, ByteBuffer.wrap(uncompressed(catalogFile)).getInt(9));
        var reopened = new DimensionCatalog(catalogFile);
        assertEquals(catalog.templates(), reopened.templates());
        assertEquals(catalog.series(), reopened.series());
        assertEquals(catalog.forcedDimensions(), reopened.forcedDimensions());

        var descriptorFile = directory.resolve("instance.dat");
        byte[] originalInstance = fixture("instance.dat");
        Files.write(descriptorFile, originalInstance);
        var descriptor = DimensionDataIO.read(descriptorFile, DimensionDataIO.Kind.INSTANCE);
        assertEquals(UUID.fromString("89ce535d-ef11-301b-aeef-044637837f7a"), descriptor.id());
        assertEquals(12345, descriptor.seed());
        assertNotNull(descriptor.spawn());
        assertArrayEquals(originalInstance, Files.readAllBytes(descriptorFile));
        assertEquals(descriptor.logicalKey(), DimensionDataIO.read(descriptorFile, DimensionDataIO.Kind.INSTANCE).logicalKey());
        DimensionDataIO.writeAtomic(descriptorFile, DimensionDataIO.Kind.INSTANCE, descriptor);
        assertEquals(3, ByteBuffer.wrap(uncompressed(descriptorFile)).getInt(9));
        var loaded = DimensionDataIO.read(descriptorFile, DimensionDataIO.Kind.INSTANCE);
        assertEquals(descriptor.logicalKey(), loaded.logicalKey());
        assertEquals(descriptor.id(), loaded.id());
        assertEquals(descriptor.ordinal(), loaded.ordinal());
        assertEquals(descriptor.seed(), loaded.seed());
        assertEquals(descriptor.template(), loaded.template());
        assertEquals(descriptor.owner(), loaded.owner());
        assertEquals(descriptor.accessPolicy(), loaded.accessPolicy());
        assertEquals(descriptor.visitors(), loaded.visitors());
        assertEquals(descriptor.spawn(), loaded.spawn());
        assertEquals(descriptor.spawnAngle(), loaded.spawnAngle());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    void schema2PendingRecoveryIsIdempotentAtEveryTransactionBoundary(int stage) throws Exception {
        var oldDescriptorFile = directory.resolve("old-instance.dat");
        Files.write(oldDescriptorFile, fixture("instance.dat"));
        var descriptor = DimensionDataIO.read(oldDescriptorFile, DimensionDataIO.Kind.INSTANCE);
        byte[] payload = uncompressed(oldDescriptorFile);
        payload[4] = 3; // The old production writer uses the same descriptor payload for pending.
        var store = new InstanceStore(directory, 2);
        for (int i = 0; i < descriptor.ordinal(); i++) store.create(descriptor(i));
        var pending = directory.resolve("pending.dat");
        try (var output = new GZIPOutputStream(Files.newOutputStream(pending))) {
            output.write(payload);
        }
        if (stage >= 1) DimensionDataIO.writeAtomic(instancePath(descriptor.id()), DimensionDataIO.Kind.INSTANCE, descriptor);
        if (stage >= 2) {
            try (var index = new RandomAccessFile(directory.resolve("instances.index").toFile(), "rw")) {
                index.seek(index.length());
                if (stage == 2) index.writeInt(123);
                else {
                    index.writeLong(descriptor.id().getMostSignificantBits());
                    index.writeLong(descriptor.id().getLeastSignificantBits());
                }
            }
        }
        var recovered = new InstanceStore(directory, 2);
        assertEquals(descriptor.ordinal() + 1, recovered.count());
        assertEquals(descriptor.seed(), recovered.find(descriptor.id()).seed());
        assertFalse(Files.exists(pending));
        assertEquals(3, ByteBuffer.wrap(uncompressed(instancePath(descriptor.id()))).getInt(9));
        assertEquals(recovered.count(), new InstanceStore(directory, 2).count());
    }

    private static byte[] uncompressed(Path path) throws Exception {
        try (var input = new GZIPInputStream(Files.newInputStream(path))) {
            return input.readAllBytes();
        }
    }
}
