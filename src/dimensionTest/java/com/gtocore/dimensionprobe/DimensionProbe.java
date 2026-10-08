package com.gtocore.dimensionprobe;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.dimension.*;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 隔离 Forge 测试模组的入口，按启动参数选择生命周期、重启、关闭故障或双客户端探针。
 * <p>
 * 仅由显式启用的 dimensions-probe Gradle 脚本加入运行环境，不进入生产构建。
 */
@Mod("dimension_probe")
public final class DimensionProbe {

    private MinecraftServer server;
    private DimensionManager manager;
    private InstanceDescriptor instance;
    private ServerLevel previous;
    private ResourceKey<Level> loadingKey;
    private CompletableFuture<ServerLevel> reentrant;
    private int phase;
    private int waitTicks;
    private boolean failed;
    private long expectedRandom;
    private long terrainHash;
    private com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget remote;
    private java.util.UUID animalId;
    private TestData testData;
    private java.util.List<com.gtocore.common.weather.WeatherTimeline.Period> weather;

    /**
     * 注册隔离测试所需的事件监听，根据 JVM 属性选择探针场景。
     */
    public DimensionProbe() {
        if (Boolean.getBoolean("dimensionSuggestionsProbe")) {
            try {
                Class.forName("com.gtocore.dimensionprobe.DimensionSuggestionsProbe").getDeclaredMethod("init").invoke(null);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Suggestions probe is not available in this checkout", failure);
            }
            return;
        }
        if (Boolean.getBoolean("dimensionNetworkProbe")) {
            NetworkProbe.init();
            return;
        }
        if (Boolean.getBoolean("dimensionRestartProbe")) {
            new RestartProbe();
            return;
        }
        if (Boolean.getBoolean("dimensionCloseProbe")) {
            new CloseProbe();
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(this::started);
        MinecraftForge.EVENT_BUS.addListener(this::loaded);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }

    private void loaded(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == loadingKey) {
            reentrant = manager.load(loadingKey);
            require(!reentrant.isDone(), "Reentrant load exposed an initializing world");
        }
    }

    private void started(ServerStartedEvent event) {
        server = event.getServer();
        manager = DimensionManager.get(server);
        try {
            var tech = com.gtocore.api.research.techtree.TechTreeSavedData.INSTANCE;
            var research = com.gtocore.api.research.TeamResearchSavedData.INSTANCE;
            var common = com.gtolib.data.CommonSavaedData.INSTANCE;
            String unsaved = UUID.randomUUID().toString();
            com.gtolib.data.CommonSavaedData.getData().putString("dimension_probe_unsaved", unsaved);
            common.setDirty();
            require(manager.levels().size() == 3, "Startup loaded " + manager.levels().size() + " worlds");
            require(server.getLevel(GTODimensions.MOON) == null, "Query woke the moon");
            DimensionStations.query(server, GTODimensions.MOON);
            require(server.getLevel(GTODimensions.MOON) == null, "Station metadata woke the moon");
            appeng.spatial.SpatialStoragePlotManager.INSTANCE.getPlots();
            require(server.getLevel(appeng.spatial.SpatialStorageDimensionIds.WORLD_ID) == null, "Spatial metadata woke terrain: key=" + appeng.spatial.SpatialStorageDimensionIds.WORLD_ID + " level=" + server.getLevel(appeng.spatial.SpatialStorageDimensionIds.WORLD_ID) + " snapshot=" + manager.levels());
            var owner = new OwnerRef(OwnerRef.Kind.PLAYER, UUID.fromString("43265327-df96-48f3-81cb-130ca1dff314"));
            String slot = "probe-" + UUID.randomUUID();
            instance = manager.getOrCreatePrivate(owner, DimensionTemplates.NOISE, slot, 12345L);
            require(manager.getOrCreatePrivate(owner, DimensionTemplates.NOISE, slot, 99L).id().equals(instance.id()), "Duplicate key changed identity");
            require(!Files.exists(manager.dimensionPath(instance.dimension())), "Metadata creation generated terrain");
            var seriesId = GTOCore.id("probe_series_" + UUID.randomUUID());
            manager.createSeries(seriesId, DimensionTemplates.NOISE, 123, null);
            long before = manager.instances().count();
            new SeriesAddress(seriesId, 0).offset(1);
            require(manager.instances().count() == before, "Neighbour query created an instance");
            var seriesInstance = manager.getOrCreateSeries(new SeriesAddress(seriesId, -1));
            require(manager.getOrCreateSeries(new SeriesAddress(seriesId, -1)).id().equals(seriesInstance.id()), "Series identity changed");
            require(manager.instances().count() == before + 1, "Series eagerly created neighbours");
            require(!Files.exists(manager.dimensionPath(seriesInstance.dimension())), "Series metadata generated terrain");
            com.gtocore.common.weather.WeatherSystem.get(server).forecast(seriesInstance.dimension());
            require(server.getLevel(seriesInstance.dimension()) == null, "Weather forecast woke terrain");
            UUID stranger = UUID.randomUUID();
            require(!manager.canAccess(stranger, false, instance.dimension()), "Private access granted to stranger");
            require(server.getLevel(instance.dimension()) == null, "Access check woke an instance");
            manager.grant(instance.dimension(), stranger, true);
            require(manager.canAccess(stranger, false, instance.dimension()), "Visitor grant failed");
            manager.grant(instance.dimension(), stranger, false);
            require(!manager.canAccess(stranger, false, instance.dimension()), "Visitor revoke failed");
            verifyVisitorSaveFailure(stranger, true);
            manager.grant(instance.dimension(), stranger, true);
            verifyVisitorSaveFailure(stranger, false);
            manager.grant(instance.dimension(), stranger, false);
            require(manager.canAccess(stranger, true, instance.dimension()), "Admin access denied");
            loadingKey = instance.dimension();
            previous = manager.loadNow(loadingKey);
            require(com.gtocore.api.research.techtree.TechTreeSavedData.INSTANCE == tech && com.gtocore.api.research.TeamResearchSavedData.INSTANCE == research && com.gtolib.data.CommonSavaedData.INSTANCE == common, "Dynamic load replaced main-world global save objects");
            require(com.gtolib.data.CommonSavaedData.getData().getString("dimension_probe_unsaved").equals(unsaved), "Dynamic load overwrote unsaved global progress");
            require(reentrant != null && reentrant.join() == previous, "Reentrant request did not merge");
            require(manager.loadNow(loadingKey) == previous, "Repeated load duplicated world");
            require(previous.getSeed() == 12345L, "Instance seed missing");
            var dataField = ServerLevel.class.getDeclaredField("structureCheck");
            dataField.setAccessible(true);
            var structure = dataField.get(previous);
            var seedField = structure.getClass().getDeclaredField("seed");
            seedField.setAccessible(true);
            require(seedField.getLong(structure) == 12345L, "StructureCheck seed differs");
            previous.getChunk(0, 0);
            terrainHash = terrain(previous);
            remote = new com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget(previous, BlockPos.ZERO);
            var deniedPlayer = new net.minecraft.server.level.ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(stranger, "ProbeDenied"));
            deniedPlayer.setServerLevel(previous);
            require(deniedPlayer.serverLevel() == server.overworld(), "Direct level assignment bypassed private access");
            deniedPlayer.teleportTo(previous, 0, 80, 0, 0, 0);
            require(deniedPlayer.serverLevel() == server.overworld(), "Bottom-level teleport bypassed permission");
            require(deniedPlayer.changeDimension(previous) == null, "Bottom-level changeDimension bypassed permission");
            require(!deniedPlayer.teleportTo(previous, 0, 80, 0, java.util.Set.of(), 0, 0), "Relative teleport bypassed permission");
            var comparison = manager.getOrCreatePrivate(owner, DimensionTemplates.NOISE, slot + "-other", 12346L);
            var other = manager.loadNow(comparison.dimension());
            require(terrain(other) != terrainHash, "Independent seeds generated identical sample terrain");
            require(manager.requestUnload(comparison.dimension()), "Comparison dimension cannot unload");
            var cow = net.minecraft.world.entity.EntityType.COW.create(previous);
            cow.setPos(2, 150, 2);
            animalId = cow.getUUID();
            previous.addFreshEntity(cow);
            testData = previous.getDataStorage().computeIfAbsent(tag -> new TestData(tag.getInt("value")), () -> new TestData(77), "dimension_probe_data");
            testData.setDirty();
            weather = java.util.List.copyOf(com.gtocore.common.weather.WeatherSystem.get(server).forecast(loadingKey));
            var rocket = earth.terrarium.adastra.common.registry.ModEntityTypes.TIER_1_ROCKET.get().create(previous);
            rocket.setPos(8, 180, 8);
            previous.addFreshEntity(rocket);
            rocket.getEntityData().set(earth.terrarium.adastra.common.entities.vehicles.Rocket.HAS_LAUNCHED, true);
            rocket.tick();
            require(manager.keepAliveReasons(loadingKey).contains("ROCKET"), "Active rocket has no lease");
            rocket.discard();
            require(!manager.keepAliveReasons(loadingKey).contains("ROCKET"), "Removed rocket retained lease");
            var lander = earth.terrarium.adastra.common.registry.ModEntityTypes.LANDER.get().create(previous);
            lander.setPos(8, 180, 8);
            previous.addFreshEntity(lander);
            lander.tick();
            require(manager.keepAliveReasons(loadingKey).contains("LANDER"), "Flying lander has no lease");
            lander.setOnGround(true);
            lander.tick();
            lander.discard();
            require(!manager.keepAliveReasons(loadingKey).contains("LANDER"), "Landed lander retained lease");
            var random = previous.getRandomSequence(GTOCore.id("probe_random"));
            random.nextLong();
            var expected = new net.minecraft.world.RandomSequences(12345L).get(GTOCore.id("probe_random"));
            expected.nextLong();
            expectedRandom = expected.nextLong();
            instance.setSpawn(new BlockPos(7, 81, -4), 37);
            try (var lease = manager.keepAlive(loadingKey, DimensionLifecycle.KeepAlive.BUILD)) {
                require(!manager.requestUnload(loadingKey), "Unload accepted a live lease");
            }
            previous.setChunkForced(0, 0, true);
            require(!manager.requestUnload(loadingKey), "Unload accepted a forced chunk");
            previous.setChunkForced(0, 0, false);
            testData.fail = true;
            require(manager.requestUnload(loadingKey), "Idle instance did not accept unload");
            phase = 3;
        } catch (Throwable error) {
            fail(error);
        }
    }

    private void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || phase == 0 || failed) return;
        try {
            if (++waitTicks > 1200) throw new AssertionError("Probe phase timed out: " + phase);
            if (phase == 3 && manager.state(loadingKey) == DimensionLifecycle.State.LOADED) {
                require(server.getLevel(loadingKey) == previous, "Save failure discarded live world");
                require(testData.isDirty(), "Save failure cleared dirty data");
                testData.fail = false;
                reentrant = null;
                testData.onSave = () -> {
                    reentrant = manager.load(loadingKey);
                    require(!reentrant.isDone(), "Save reentry exposed a world before saving finished");
                };
                require(manager.requestUnload(loadingKey), "Failed save could not retry");
                phase = 5;
            } else if (phase == 5 && reentrant != null && reentrant.isDone()) {
                require(reentrant.join() == previous && server.getLevel(loadingKey) == previous, "Save reentry failed to cancel closing");
                require(manager.requestUnload(loadingKey), "Save reentry prevented a later unload");
                phase = 1;
            } else if (phase == 1 && manager.state(loadingKey) == DimensionLifecycle.State.DORMANT) {
                require(server.getLevel(loadingKey) == null, "Closed instance retained in server map");
                require(!manager.levels().contains(previous), "Snapshot retained a closed world");
                require(remote.level() == null, "Device cache retained a closed level");
                require(Files.exists(manager.dimensionPath(loadingKey).resolve("region")), "Unload removed terrain files");
                ServerLevel reopened = manager.loadNow(loadingKey);
                require(reopened != previous, "Unload kept runtime reference");
                var staleTargetPlayer = new net.minecraft.server.level.ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(instance.owner().id(), "StaleTarget"));
                var originalPosition = staleTargetPlayer.position();
                staleTargetPlayer.setServerLevel(previous);
                require(staleTargetPlayer.serverLevel() == server.overworld(), "Direct assignment accepted a closed level after reopen");
                staleTargetPlayer.teleportTo(previous, 0, 80, 0, 0, 0);
                require(staleTargetPlayer.serverLevel() == server.overworld(), "Teleport accepted a closed level after reopen");
                require(staleTargetPlayer.changeDimension(previous) == null, "changeDimension accepted a closed level after reopen");
                require(!staleTargetPlayer.teleportTo(previous, 0, 80, 0, java.util.Set.of(), 0, 0), "Relative teleport accepted a closed level after reopen");
                com.gtolib.utils.ServerUtils.teleportToDimension(previous, staleTargetPlayer, new net.minecraft.world.phys.Vec3(32, 80, 32));
                require(staleTargetPlayer.position().equals(originalPosition), "Stale utility target moved player in their source world");
                earth.terrarium.adastra.common.utils.ModUtils.land(staleTargetPlayer, previous, new net.minecraft.world.phys.Vec3(32, 80, 32));
                require(staleTargetPlayer.position().equals(originalPosition), "Stale planet landing moved player in their source world");
                require(reopened.getSeed() == 12345L, "Reopened seed changed");
                require(reopened.getSharedSpawnPos().equals(instance.spawn()), "Reopened spawn changed");
                require(terrain(reopened) == terrainHash, "Reopened terrain changed");
                require(reopened.getRandomSequence(GTOCore.id("probe_random")).nextLong() == expectedRandom, "Random sequence state did not persist independently");
                require(reopened.getDataStorage().get(tag -> new TestData(tag.getInt("value")), "dimension_probe_data").value == 77, "SavedData was not restored");
                var chunk = reopened.getChunk(0, 0);
                reopened.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL, new net.minecraft.world.level.ChunkPos(0, 0), 3, BlockPos.ZERO);
                require(!manager.requestUnload(loadingKey), "Valid temporary ticket allowed unload");
                remote.blockEntity();
                require(remote.level() == reopened, "Device cache failed to resolve reopened target");
                require(com.gtocore.common.weather.WeatherSystem.get(server).forecast(loadingKey).getFirst().weather() == weather.getFirst().weather(), "Instance weather reset");
                previous = reopened;
                phase = 4;
            } else if (phase == 4 && previous.getEntity(animalId) != null) {
                previous.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL, new net.minecraft.world.level.ChunkPos(0, 0), 3, BlockPos.ZERO);
                require(manager.requestUnload(loadingKey), "Reopened instance cannot unload");
                phase = 2;
            } else if (phase == 2 && manager.state(loadingKey) == DimensionLifecycle.State.DORMANT) {
                require(server.getTickTime(loadingKey) == null, "Tick timing cache retained old dimension");
                Files.writeString(Path.of("result.txt"), "DIMENSION_PROBE_PASSED");
                System.out.println("DIMENSION_PROBE_PASSED");
                server.halt(false);
                phase = 0;
            }
        } catch (Throwable error) {
            fail(error);
        }
    }

    private void fail(Throwable error) {
        failed = true;
        error.printStackTrace();
        try {
            Files.writeString(Path.of("result.txt"), "DIMENSION_PROBE_FAILED: " + error);
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.halt(false);
    }

    private void verifyVisitorSaveFailure(UUID visitor, boolean grant) throws Exception {
        String id = instance.id().toString();
        var file = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("gtolib/dimensions/instances").resolve(id.substring(0, 2)).resolve(id + ".dat");
        var backup = file.resolveSibling(id + ".before-failure");
        Files.move(file, backup);
        Files.createDirectory(file);
        Files.writeString(file.resolve("blocked"), "injected descriptor save failure");
        try {
            boolean failed = false;
            try {
                manager.grant(instance.dimension(), visitor, grant);
            } catch (IllegalStateException expected) {
                failed = true;
            }
            require(failed, "Visitor save failure was swallowed");
            require(instance.isVisitor(visitor) != grant, "Visitor save failure did not roll back authorization");
        } finally {
            Files.delete(file.resolve("blocked"));
            Files.delete(file);
            Files.move(backup, file);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static long terrain(ServerLevel level) {
        var chunk = level.getChunk(0, 0);
        long hash = 1;
        for (int x = 0; x < 16; x += 2) for (int z = 0; z < 16; z += 2) hash = hash * 31 + chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return hash;
    }

    /**
     * 保存探针专用 SavedData，可注入序列化失败或一次性重入加载回调，并校验数据随世界保存和恢复。
     */
    public static final class TestData extends net.minecraft.world.level.saveddata.SavedData {

        final int value;
        boolean fail;
        Runnable onSave;

        TestData(int value) {
            this.value = value;
        }

        /**
         * 执行配置的故障或重入回调后编码测试值。
         *
         * @param tag 保存目标标签
         * @return 写入测试值的同一标签
         * @throws IllegalStateException 已启用保存故障注入
         */
        @Override
        public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag) {
            if (fail) throw new IllegalStateException("INJECTED_DIMENSION_SAVE_FAILURE");
            if (onSave != null) {
                var callback = onSave;
                onSave = null;
                callback.run();
            }
            tag.putInt("value", value);
            return tag;
        }
    }
}
