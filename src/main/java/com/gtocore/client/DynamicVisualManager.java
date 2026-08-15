package com.gtocore.client;

import com.gtocore.api.machine.dynamic.DynamicPartDefinition;
import com.gtocore.api.machine.dynamic.IDynamicStructureMachine;

import com.gtolib.utils.ClientUtil;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.gto.datasynclib.datastream.DataComponentKey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class DynamicVisualManager {

    private static final DataComponentKey<VisualState> STATE = DataComponentKey.createNoCodec("dynamicVisualState");

    private DynamicVisualManager() {}

    public static boolean hidePart(IDynamicStructureMachine machine, String partName) {
        if (!machine.isDynamicStructureEnabled()) return false;
        if (!(machine.getDynamicLevel() instanceof ClientLevel level)) return false;
        if (!machine.visitDynamicBlocks(partName, (symbol, pos) -> level.isLoaded(pos))) return false;
        return machine.visitDynamicBlocks(partName, (symbol, pos) -> {
            hide(level, pos);
            return true;
        });
    }

    public static void showPart(IDynamicStructureMachine machine, String partName) {
        if (!(machine.getDynamicLevel() instanceof ClientLevel level)) return;
        machine.visitDynamicBlocks(partName, (symbol, pos) -> {
            show(level, pos);
            if (!level.isLoaded(pos)) return true;
            BlockState state = machine.getDynamicBlockState(partName, symbol);
            if (state != null) level.setBlock(pos, state, Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE);
            return true;
        });
    }

    public static boolean isInRange(BlockPos pos, double distance) {
        var camera = Minecraft.getInstance().getCameraEntity();
        return camera != null && camera.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < distance * distance;
    }

    public static void register(IDynamicStructureMachine machine) {
        if (!machine.isDynamicStructureEnabled()) return;
        if (!(machine.getDynamicLevel() instanceof ClientLevel level)) return;
        get(level).machines.put(machine.getDynamicOrigin().asLong(), machine);
    }

    public static void unregister(IDynamicStructureMachine machine) {
        if (!(machine.getDynamicLevel() instanceof ClientLevel level)) return;
        VisualState state = getIfPresent(level);
        if (state != null) state.machines.remove(machine.getDynamicOrigin().asLong());
    }

    public static DynamicHitResult findDynamicHit() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameMode == null || mc.getCameraEntity() == null) return null;
        return findDynamicHit(mc.gameMode.getPickRange() + 5, mc.getFrameTime());
    }

    public static DynamicHitResult findDynamicHit(double distance, float partialTicks) {
        var mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level) || mc.getCameraEntity() == null) return null;
        Vec3 from = mc.getCameraEntity().getEyePosition();
        Vector3f lookVector = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 look = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        Vec3 to = from.add(look.scale(distance));
        VisualState state = getIfPresent(level);
        if (state == null) return null;
        DynamicHitResult result = null;
        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != level) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                DynamicHitResult hit = findPartHit(machine, partName, part, from, to, partialTicks);
                if (hit != null && (result == null || hit.distance() < result.distance())) result = hit;
            }
        }
        return result;
    }

    public static boolean isDynamicTarget(DynamicHitResult hit) {
        if (hit == null) return false;
        var mc = Minecraft.getInstance();
        HitResult target = mc.hitResult;
        if (target == null || target.getType() == HitResult.Type.MISS) return true;
        return target.getLocation().distanceTo(mc.gameRenderer.getMainCamera().getPosition()) > hit.distance() + 1.0E-4;
    }

    public static void renderDynamicHighlight(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        var mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;
        DynamicHitResult hit = findDynamicHit(mc.gameMode.getPickRange() + 5, event.getPartialTick());
        if (!isDynamicTarget(hit)) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        String[][] structure = hit.part().getStructure();
        poseStack.pushPose();
        BlockPos origin = hit.machine().getDynamicOrigin();
        poseStack.translate(origin.getX() - camera.x, origin.getY() - camera.y, origin.getZ() - camera.z);
        poseStack.mulPoseMatrix(hit.transform());
        VertexConsumer lines = event.getLevelRenderer().renderBuffers.bufferSource().getBuffer(RenderType.lines());
        double minX = structure.length / 2f - hit.x();
        double minY = -structure[0].length / 2f + hit.y();
        double minZ = -structure[0][0].length() / 2f + hit.z();
        LevelRenderer.renderLineBox(poseStack, lines, minX, minY, minZ, minX + 1, minY + 1, minZ + 1, 0, 0, 0, 1);
        poseStack.popPose();
    }

    private static DynamicHitResult findPartHit(IDynamicStructureMachine machine, String partName, DynamicPartDefinition part, Vec3 from, Vec3 to, float partialTicks) {
        String[][] structure = part.getStructure();
        Matrix4f transform = machine.getDynamicTransform(partName, partialTicks);
        Matrix4f inverse = new Matrix4f(transform).invert();
        BlockPos origin = machine.getDynamicOrigin();
        Vec3 relativeFrom = from.subtract(origin.getX(), origin.getY(), origin.getZ());
        Vec3 relativeTo = to.subtract(origin.getX(), origin.getY(), origin.getZ());
        Vec3 localFrom = transform(inverse, relativeFrom);
        Vec3 localTo = transform(inverse, relativeTo);
        Vec3 gridFrom = new Vec3(structure.length / 2f + 1 - localFrom.x, localFrom.y + structure[0].length / 2f, localFrom.z + structure[0][0].length() / 2f);
        Vec3 gridTo = new Vec3(structure.length / 2f + 1 - localTo.x, localTo.y + structure[0].length / 2f, localTo.z + structure[0][0].length() / 2f);
        return BlockGetter.traverseBlocks(gridFrom, gridTo, new Object(), (ignored, pos) -> {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if (x < 0 || x >= structure.length || y < 0 || y >= structure[0].length || z < 0 || z >= structure[0][0].length()) return null;
            char letter = structure[x][y].charAt(z);
            BlockState state = machine.getDynamicBlockState(partName, letter);
            if (state == null) return null;
            Vec3 min = new Vec3(structure.length / 2f - x, -structure[0].length / 2f + y, -structure[0][0].length() / 2f + z);
            HitBox hitBox = intersect(localFrom, localTo, min, min.add(1, 1, 1));
            if (hitBox == null) return null;
            Vec3 localHit = localFrom.lerp(localTo, hitBox.time);
            Vec3 worldHit = transform(transform, localHit).add(origin.getX(), origin.getY(), origin.getZ());
            Direction side = worldDirection(transform, hitBox.side);
            BlockPos realPos = machine.getDynamicSourcePos(partName, x, y, z);
            BlockHitResult target = new BlockHitResult(worldHit, side, realPos, false);
            return new DynamicHitResult(machine, partName, part, x, y, z, state, realPos, target, worldHit, side, from.distanceTo(worldHit), transform);
        }, ignored -> null);
    }

    private static HitBox intersect(Vec3 from, Vec3 to, Vec3 min, Vec3 max) {
        Vec3 direction = to.subtract(from);
        double near = 0, far = 1;
        Direction side = Direction.UP;
        for (Direction.Axis axis : Direction.Axis.values()) {
            double start = axis.choose(from.x, from.y, from.z);
            double delta = axis.choose(direction.x, direction.y, direction.z);
            double low = axis.choose(min.x, min.y, min.z);
            double high = axis.choose(max.x, max.y, max.z);
            if (Math.abs(delta) < 1.0E-7) {
                if (start < low || start > high) return null;
                continue;
            }
            double first = (low - start) / delta;
            double second = (high - start) / delta;
            Direction entering = delta > 0 ? Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE) : Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            if (first > second) {
                double temp = first;
                first = second;
                second = temp;
            }
            if (first > near) {
                near = first;
                side = entering;
            }
            far = Math.min(far, second);
            if (near > far || far < 0 || near > 1) return null;
        }
        return new HitBox(Math.max(near, 0), side);
    }

    private static Vec3 transform(Matrix4f matrix, Vec3 value) {
        Vector3f vector = new Vector3f((float) value.x, (float) value.y, (float) value.z);
        matrix.transformPosition(vector);
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    private static Direction worldDirection(Matrix4f matrix, Direction local) {
        Vector3f vector = new Vector3f(local.getStepX(), local.getStepY(), local.getStepZ());
        matrix.transformDirection(vector);
        return Direction.getNearest(vector.x(), vector.y(), vector.z());
    }

    public record DynamicHitResult(IDynamicStructureMachine machine, String partName, DynamicPartDefinition part,
                                   int x, int y, int z, BlockState state, BlockPos position,
                                   BlockHitResult target, Vec3 location, Direction side, double distance,
                                   Matrix4f transform) {}

    private record HitBox(double time, Direction side) {}

    public static void hide(Level level, BlockPos pos) {
        ClientLevel clientLevel = (ClientLevel) level;
        var positions = get(clientLevel).hidden.computeIfAbsent(ChunkPos.asLong(pos), ignored -> new Long2IntOpenHashMap());
        long posLong = pos.asLong();
        positions.addTo(posLong, 1);
        ClientUtil.getPreventUpdate(clientLevel).add(posLong);
        if (clientLevel.isLoaded(pos)) setAir(clientLevel, pos);
    }

    public static void show(Level level, BlockPos pos) {
        ClientLevel clientLevel = (ClientLevel) level;
        VisualState state = getIfPresent(clientLevel);
        if (state == null) return;
        var chunks = state.hidden;
        long chunkPos = ChunkPos.asLong(pos);
        var positions = chunks.get(chunkPos);
        if (positions == null) return;
        long posLong = pos.asLong();
        int count = positions.get(posLong);
        if (count == 0) return;
        if (count > 1) {
            positions.put(posLong, count - 1);
            return;
        }
        positions.remove(posLong);
        ClientUtil.getPreventUpdate(clientLevel).remove(posLong);
        if (positions.isEmpty()) chunks.remove(chunkPos);
    }

    public static void refreshChunk(ClientLevel level, int chunkX, int chunkZ) {
        VisualState state = getIfPresent(level);
        if (state == null) return;
        var positions = state.hidden.get(ChunkPos.asLong(chunkX, chunkZ));
        if (positions == null) return;
        for (long posLong : positions.keySet()) {
            ClientUtil.getPreventUpdate(level).add(posLong);
            setAir(level, BlockPos.of(posLong));
        }
    }

    public static void queueChunkLightRefresh(ClientLevel level, int chunkX, int chunkZ) {
        var pending = get(level).pendingLight;
        boolean schedule = pending.isEmpty();
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                pending.add(ChunkPos.asLong(x, z));
            }
        }
        if (schedule) level.queueLightUpdate(() -> refreshChunkLight(level));
    }

    private static void refreshChunkLight(ClientLevel level) {
        VisualState state = getIfPresent(level);
        if (state == null || state.pendingLight.isEmpty()) return;
        var lightEngine = level.getChunkSource().getLightEngine();
        for (long chunkPos : state.pendingLight) {
            var positions = state.hidden.get(chunkPos);
            if (positions == null) continue;
            for (long posLong : positions.keySet()) {
                lightEngine.checkBlock(BlockPos.of(posLong));
            }
        }
        state.pendingLight.clear();
    }

    private static void setAir(ClientLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE, 512);
    }

    private static VisualState get(ClientLevel level) {
        VisualState state = ILevel.getCapability(level, STATE);
        if (state == null) {
            state = new VisualState();
            ILevel.setCapability(level, STATE, state);
        }
        return state;
    }

    private static VisualState getIfPresent(ClientLevel level) {
        return ILevel.getCapability(level, STATE);
    }

    private static final class VisualState {

        private final Long2ObjectOpenHashMap<Long2IntOpenHashMap> hidden = new Long2ObjectOpenHashMap<>();
        private final LongOpenHashSet pendingLight = new LongOpenHashSet();
        private final Long2ObjectOpenHashMap<IDynamicStructureMachine> machines = new Long2ObjectOpenHashMap<>();
    }
}
