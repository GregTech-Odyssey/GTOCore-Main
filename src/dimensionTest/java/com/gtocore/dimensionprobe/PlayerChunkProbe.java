package com.gtocore.dimensionprobe;

import com.gtolib.api.dimension.DimensionChunkEdits;
import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.DimensionTemplates;
import com.gtolib.api.dimension.OwnerRef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongSets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 两次启动隔离服务器，验证非主世界筛选、强加载、末地中央岛与持久化恢复。 */
final class PlayerChunkProbe {

    private static final UUID OWNER = UUID.fromString("d0862d95-e370-4ea8-b998-91a54d8ac15a");
    private static final BlockPos UNTOUCHED = new BlockPos(1, 64, 1);
    private static final BlockPos EDITED = new BlockPos(33, 64, 1);
    private static final BlockPos CANCELLED = new BlockPos(65, 64, 1);
    private static final BlockPos PLACED = new BlockPos(97, 64, 1);
    private static final BlockPos CONTAINER = new BlockPos(129, 64, 1);
    private static final BlockPos VANILLA_FORCED = new BlockPos(161, 64, 1);
    private static final BlockPos[] FORGE_FORCED = {
            new BlockPos(193, 64, 1), new BlockPos(225, 64, 1),
            new BlockPos(257, 64, 1), new BlockPos(289, 64, 1)
    };
    private static final BlockPos FORCE_TICK = new BlockPos(321, 64, 1);
    private static final BlockPos RELEASED = new BlockPos(353, 64, 1);
    private static final BlockPos PROMOTED = new BlockPos(385, 64, 1);
    private static final BlockPos REJECTED = new BlockPos(417, 64, 1);
    private static final BlockPos STRUCTURE_START = new BlockPos(449, 64, 1);
    private static final BlockPos STRUCTURE_REFERENCE = new BlockPos(465, 64, 1);
    private static final BlockPos INVALID_STRUCTURE = new BlockPos(481, 64, 1);
    private static final BlockPos END_MAIN = new BlockPos(120, 80, 120);
    private static final BlockPos END_OUTSIDE = new BlockPos(144, 80, 0);
    private static final ResourceKey<Level> FIXED = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("dimension_probe", "fixed_chunks"));
    private ServerLevel level;
    private ServerLevel fixed;
    private ServerLevel end;
    private int ticks;

    PlayerChunkProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::started);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
        MinecraftForge.EVENT_BUS.addListener(this::cancelPlacement);
    }

    private void cancelPlacement(BlockEvent.EntityPlaceEvent event) {
        if ((event.getLevel() == level || event.getLevel() == fixed) && event.getPos().getX() == CANCELLED.getX()) event.setCanceled(true);
    }

    private void started(ServerStartedEvent event) {
        try {
            var manager = DimensionManager.get(event.getServer());
            manager.registerDefinition(FIXED, manager.catalog().templates().get(DimensionTemplates.VOID)
                    .createStem(event.getServer().registryAccess()));
            var descriptor = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, OWNER), DimensionTemplates.VOID,
                    "player-chunk-save", 287143L);
            level = manager.loadNow(descriptor.dimension());
            fixed = manager.loadNow(FIXED);
            end = manager.loadNow(Level.END);
            var nether = manager.loadNow(Level.NETHER);
            require(DimensionChunkEdits.get(event.getServer().overworld()) == null, "Overworld received a save filter");
            require(DimensionChunkEdits.shouldSave(event.getServer().overworld(), new ChunkPos(19, 19)), "Overworld save was filtered");
            require(DimensionChunkEdits.get(nether) != null, "Nether has no save filter");
            require(!DimensionChunkEdits.shouldSave(nether, new ChunkPos(19, 19)), "Untouched Nether chunk was allowed");
            require(DimensionChunkEdits.get(fixed) != null, "Fixed definition has no save filter");
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    require(DimensionChunkEdits.shouldSave(end, new ChunkPos(x, z)) == (x >= -8 && x <= 8 && z >= -8 && z <= 8),
                            "End arena save boundary differs at " + x + "," + z);
                }
            }
            if (Files.exists(Path.of("player-chunks-stage.txt"))) {
                verifyReload(level);
                verifyReload(fixed);
                require(end.getBlockState(END_MAIN).is(Blocks.BELL), "End central island change did not survive restart");
                require(!end.getBlockState(END_OUTSIDE).is(Blocks.BELL), "Untouched End outer chunk was persisted");
                Files.writeString(Path.of("player-chunks-result.txt"), "PLAYER_CHUNKS_RESTART_PASSED");
                event.getServer().halt(false);
                return;
            }
            prepare(level);
            prepare(fixed);
            for (var pos : new BlockPos[] { END_MAIN, END_OUTSIDE }) {
                end.setBlockAndUpdate(pos, Blocks.BELL.defaultBlockState());
                addCow(end, pos);
            }
        } catch (Throwable failure) {
            fail(event.getServer(), failure);
        }
    }

    private void prepare(ServerLevel level) throws Exception {
        level.setBlockAndUpdate(UNTOUCHED, Blocks.BELL.defaultBlockState());
        level.setBlockAndUpdate(PROMOTED, Blocks.BELL.defaultBlockState());
        level.getPoiManager().flush(new ChunkPos(PROMOTED));
        level.save(null, true, false);
        var root = DimensionManager.get(level.getServer()).dimensionPath(level.dimension());
        require(read(root.resolve("region"), new ChunkPos(PROMOTED)) == null, "Unqualified clean chunk was saved");
        level.setChunkForced(PROMOTED.getX() >> 4, PROMOTED.getZ() >> 4, true);
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(PROMOTED)), "Previously skipped chunk was not promoted");

        level.setBlockAndUpdate(EDITED, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(CANCELLED.below(), Blocks.STONE.defaultBlockState());
        var player = FakePlayerFactory.get(level, new GameProfile(OWNER, "ChunkSaveProbe"));
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        require(player.gameMode.destroyBlock(EDITED), "Player break failed");
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(EDITED)), "Real player break was not tracked");
        var stack = new ItemStack(Items.DIAMOND_BLOCK);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(CANCELLED.below()), Direction.UP, CANCELLED.below(), false));
        require(level.getBlockState(CANCELLED).isAir(), "Cancelled placement was not rolled back");
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(CANCELLED)), "Cancelled placement enabled saving");
        level.setBlockAndUpdate(PLACED.below(), Blocks.STONE.defaultBlockState());
        player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(PLACED.below()), Direction.UP, PLACED.below(), false));
        require(level.getBlockState(PLACED).is(Blocks.DIAMOND_BLOCK), "Player placement failed");
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(PLACED)), "Real placement was not tracked");
        level.setBlockAndUpdate(EDITED.above(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(EDITED.offset(1, 0, 0), Blocks.BELL.defaultBlockState());
        var data = DimensionChunkEdits.get(level);
        data.begin();
        level.setBlockAndUpdate(CANCELLED, Blocks.STONE.defaultBlockState());
        data.begin();
        level.setBlockAndUpdate(CANCELLED, Blocks.AIR.defaultBlockState());
        data.finish(level);
        data.finish(level);
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(CANCELLED)), "Nested rollback enabled saving");
        level.setBlockAndUpdate(CONTAINER, Blocks.CHEST.defaultBlockState());
        var chest = (ChestBlockEntity) level.getBlockEntity(CONTAINER);
        data.begin();
        chest.setItem(0, new ItemStack(Items.EMERALD));
        data.finish(level);
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(CONTAINER)), "Player inventory mutation was not tracked");
        for (var pos : forcedPositions()) {
            level.setBlockAndUpdate(pos, Blocks.BELL.defaultBlockState());
            addCow(level, pos);
        }
        level.setChunkForced(VANILLA_FORCED.getX() >> 4, 0, true);
        for (int i = 0; i < FORGE_FORCED.length; i++) {
            var pos = FORGE_FORCED[i];
            boolean accepted = i < 2 ?
                    ForgeChunkManager.forceChunk(level, "dimension_probe", BlockPos.ZERO, pos.getX() >> 4, 0, true, (i & 1) != 0) :
                    ForgeChunkManager.forceChunk(level, "dimension_probe", OWNER, pos.getX() >> 4, 0, true, (i & 1) != 0);
            require(accepted, "Forge force ticket was rejected");
            require(DimensionChunkEdits.shouldSave(level, new ChunkPos(pos)), "Forge force did not allow saving");
        }
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(FORCE_TICK), 2, BlockPos.ZERO, true);
        level.getChunkSource().removeRegionTicket(TicketType.PORTAL, new ChunkPos(FORCE_TICK), 2, BlockPos.ZERO, true);
        level.setChunkForced(RELEASED.getX() >> 4, 0, true);
        level.setChunkForced(RELEASED.getX() >> 4, 0, false);
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(RELEASED)), "Force removal lost final-save eligibility");
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(FORCE_TICK)), "Ticking force ticket was not tracked");
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(11, 0)), "Force propagated persistence to a neighboring chunk");
        level.setBlockAndUpdate(REJECTED, Blocks.BELL.defaultBlockState());
        require(!ForgeChunkManager.forceChunk(level, "chunk_probe_not_loaded", BlockPos.ZERO, REJECTED.getX() >> 4, 0, true, false),
                "Unloaded mod unexpectedly got a force ticket");
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(REJECTED)), "Rejected force ticket enabled saving");
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(new ResourceLocation("minecraft", "buried_treasure"));
        require(structure != null, "Structure fixture type is missing");
        for (var pos : new BlockPos[] { STRUCTURE_START, STRUCTURE_REFERENCE, INVALID_STRUCTURE }) {
            level.setBlockAndUpdate(pos, Blocks.BELL.defaultBlockState());
            addCow(level, pos);
        }
        var startChunk = level.getChunkAt(STRUCTURE_START);
        startChunk.setStartForStructure(structure, new StructureStart(structure, startChunk.getPos(), 0,
                new PiecesContainer(List.of(new BuriedTreasurePieces.BuriedTreasurePiece(STRUCTURE_START),
                        new BuriedTreasurePieces.BuriedTreasurePiece(STRUCTURE_REFERENCE)))));
        level.getChunkAt(STRUCTURE_REFERENCE).addReferenceForStructure(structure, startChunk.getPos().toLong());
        level.getChunkAt(INVALID_STRUCTURE).setStartForStructure(structure, StructureStart.INVALID_START);
        level.getChunkAt(INVALID_STRUCTURE).setAllReferences(Map.of(structure, LongSets.EMPTY_SET));
        require(!DimensionChunkEdits.skipChunk(level, new ImposterProtoChunk(startChunk, false)), "Wrapped structure start was skipped");
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(STRUCTURE_REFERENCE)), "Cross-chunk structure reference was skipped");
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(INVALID_STRUCTURE)), "Invalid start or empty reference enabled saving");
        addCow(level, UNTOUCHED);
        addCow(level, EDITED);
    }

    private static BlockPos[] forcedPositions() {
        return new BlockPos[] { VANILLA_FORCED, FORGE_FORCED[0], FORGE_FORCED[1], FORGE_FORCED[2], FORGE_FORCED[3], FORCE_TICK, RELEASED, PROMOTED };
    }

    private static void addCow(ServerLevel level, BlockPos pos) {
        var cow = EntityType.COW.create(level);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
        require(level.addFreshEntity(cow), "Probe entity was not added");
    }

    private static void verifyReload(ServerLevel level) throws Exception {
        require(level.getBlockState(EDITED).isAir(), "Player break did not survive restart");
        require(level.getBlockState(EDITED.above()).is(Blocks.DIAMOND_BLOCK), "Later machine change did not survive restart");
        require(level.getBlockState(PLACED).is(Blocks.DIAMOND_BLOCK), "Player placement did not survive restart");
        var chest = (ChestBlockEntity) level.getBlockEntity(CONTAINER);
        require(chest != null && chest.getItem(0).is(Items.EMERALD), "Player inventory edit did not survive restart");
        require(DimensionChunkEdits.shouldSave(level, new ChunkPos(EDITED)), "Saved chunk lost eligibility after restart");
        require(!DimensionChunkEdits.shouldSave(level, new ChunkPos(UNTOUCHED)), "Untouched chunk became persistent after restart");
        for (var pos : forcedPositions()) {
            require(level.getBlockState(pos).is(Blocks.BELL), "Forced chunk did not survive restart at " + pos);
            require(DimensionChunkEdits.shouldSave(level, new ChunkPos(pos)), "Forced chunk lost eligibility after restart");
        }
        for (var pos : new BlockPos[] { STRUCTURE_START, STRUCTURE_REFERENCE }) {
            require(level.getBlockState(pos).is(Blocks.BELL), "Structure region content did not survive restart");
            require(DimensionChunkEdits.shouldSave(level, new ChunkPos(pos)), "Structure region lost eligibility after restart");
        }
        require(level.getForcedChunks().contains(new ChunkPos(VANILLA_FORCED).toLong()), "Vanilla force was not restored");
        var forceData = level.getDataStorage().get(net.minecraft.world.level.ForcedChunksSavedData::load, "chunks");
        require(!forceData.getBlockForcedChunks().getChunks().isEmpty() && !forceData.getBlockForcedChunks().getTickingChunks().isEmpty(),
                "Forge block tickets did not survive restart");
        require(!forceData.getEntityForcedChunks().getChunks().isEmpty() && !forceData.getEntityForcedChunks().getTickingChunks().isEmpty(),
                "Forge entity tickets did not survive restart");
        level.setBlockAndUpdate(EDITED.above(), Blocks.GOLD_BLOCK.defaultBlockState());
        level.save(null, true, false);
        require(read(DimensionManager.get(level.getServer()).dimensionPath(level.dimension()).resolve("region"), new ChunkPos(EDITED)) != null,
                "Persistent chunk was not saved after restart");
    }

    private void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || level == null || ++ticks != 40) return;
        var server = event.getServer();
        try {
            var manager = DimensionManager.get(server);
            for (var target : new ServerLevel[] { level, fixed }) {
                target.save(null, true, false);
                var root = manager.dimensionPath(target.dimension());
                for (var kind : new String[] { "region", "entities", "poi" }) {
                    require(read(root.resolve(kind), new ChunkPos(UNTOUCHED)) == null, "Untouched " + kind + " was written");
                    require(read(root.resolve(kind), new ChunkPos(CANCELLED)) == null, "Cancelled " + kind + " was written");
                    require(read(root.resolve(kind), new ChunkPos(REJECTED)) == null, "Rejected force " + kind + " was written");
                    require(read(root.resolve(kind), new ChunkPos(INVALID_STRUCTURE)) == null, "Invalid structure " + kind + " was written");
                    require(read(root.resolve(kind), new ChunkPos(EDITED)) != null, "Edited " + kind + " was not written");
                    for (var pos : forcedPositions()) {
                        require(read(root.resolve(kind), new ChunkPos(pos)) != null, "Forced " + kind + " was not written at " + pos);
                    }
                    for (var pos : new BlockPos[] { STRUCTURE_START, STRUCTURE_REFERENCE }) {
                        require(read(root.resolve(kind), new ChunkPos(pos)) != null, "Structure region " + kind + " was not written");
                    }
                }
            }
            end.save(null, true, false);
            for (var kind : new String[] { "region", "entities", "poi" }) {
                var root = manager.dimensionPath(Level.END).resolve(kind);
                require(read(root, new ChunkPos(END_MAIN)) != null, "End main island " + kind + " was not written");
                require(read(root, new ChunkPos(END_OUTSIDE)) == null, "End outer island " + kind + " was written");
            }
            Files.writeString(Path.of("player-chunks-stage.txt"), level.dimension().location() + "\n" + fixed.dimension().location());
            Files.writeString(Path.of("player-chunks-result.txt"), "PLAYER_CHUNKS_SAVE_PASSED");
            server.halt(false);
        } catch (Throwable failure) {
            fail(server, failure);
        }
    }

    private static CompoundTag read(Path directory, ChunkPos pos) throws Exception {
        var file = directory.resolve("r." + pos.getRegionX() + "." + pos.getRegionZ() + ".mca");
        if (Files.notExists(file)) return null;
        try (var region = new RegionFile(file, directory, false); var input = region.getChunkDataInputStream(pos)) {
            return input == null ? null : NbtIo.read(input);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void fail(net.minecraft.server.MinecraftServer server, Throwable failure) {
        failure.printStackTrace();
        try {
            Files.writeString(Path.of("player-chunks-result.txt"), "PLAYER_CHUNKS_FAILED: " + failure);
        } catch (Exception recordingFailure) {
            failure.addSuppressed(recordingFailure);
        }
        server.halt(false);
    }
}
