package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructureBlocks;
import com.gregtechceu.gtceu.api.pattern.ControllerPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class OverviewDocking {

    public static final Direction[] HORIZONTAL = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
    public static final Direction[] FIXED_UP = { Direction.NORTH };
    public static final Function<BlockPos, Direction[]> ANY_HORIZONTAL = port -> HORIZONTAL;
    public static final CellRule FREE = (pos, existing, wanted, depth) -> existing.isAir() || existing.getBlock() == wanted.getBlock();
    private static final double RING_DISTANCE_SQR = 16.5;

    public record DockPose(BlockPos port, Direction front, Direction up, double score) {

        public boolean same(BlockPos port, Direction front, Direction up) {
            return this.port.equals(port) && this.front == front && this.up == up;
        }
    }

    @FunctionalInterface
    public interface CellRule {

        boolean accepts(BlockPos pos, BlockState existing, BlockState wanted, int depth);
    }

    private OverviewDocking() {}

    public static Direction[] ups(MultiblockMachineDefinition definition) {
        return definition.isAllowExtendedFacing() ? HORIZONTAL : FIXED_UP;
    }

    public static List<List<BlockPos>> rings(Collection<BlockPos> ports) {
        var rings = new ArrayList<List<BlockPos>>();
        for (var port : ports) {
            List<BlockPos> found = null;
            for (var ring : rings) {
                for (var other : ring) {
                    if (other.distSqr(port) <= RING_DISTANCE_SQR) {
                        found = ring;
                        break;
                    }
                }
                if (found != null) break;
            }
            if (found == null) {
                found = new ArrayList<>();
                rings.add(found);
            }
            found.add(port.immutable());
        }
        return rings;
    }

    public static double[] center(List<BlockPos> cells) {
        double x = 0, y = 0, z = 0;
        for (var q : cells) {
            x += q.getX();
            y += q.getY();
            z += q.getZ();
        }
        int n = Math.max(1, cells.size());
        return new double[] { x / n, y / n, z / n };
    }

    public static Direction horizontalAway(BlockPos pos, double[] from) {
        double dx = pos.getX() - from[0], dz = pos.getZ() - from[2];
        if (Math.abs(dx) >= Math.abs(dz)) return dx >= 0 ? Direction.EAST : Direction.WEST;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static BlockState controllerState(MultiblockMachineDefinition definition, Direction front, Direction up) {
        var block = (MetaMachineBlock) definition.get();
        var state = block.defaultBlockState();
        if (state.hasProperty(block.rotationState.property)) state = state.setValue(block.rotationState.property, front);
        if (definition.isAllowExtendedFacing() && state.hasProperty(MetaMachineBlock.UPWARDS_FACING_PROPERTY)) {
            state = state.setValue(MetaMachineBlock.UPWARDS_FACING_PROPERTY, up);
        }
        return state;
    }

    public static Long2ObjectOpenHashMap<BlockState> worldBlocks(MultiblockMachineDefinition definition, Layout layout, Item[] items, DockPose pose) {
        var blocks = StructureBlocks.worldBlocks(layout, items, pose.port, pose.front, pose.up, false);
        blocks.put(pose.port.asLong(), controllerState(definition, pose.front, pose.up));
        return blocks;
    }

    public static List<DockPose> mount(MultiblockMachineDefinition definition, Layout layout, Item[] items, List<BlockPos> ports, Function<BlockPos, Direction[]> fronts,
                                       Direction[] ups, BlockGetter world, @Nullable Direction outward, @Nullable double[] reference, CellRule rule) {
        var result = new ArrayList<DockPose>();
        var block = (MetaMachineBlock) definition.get();
        var seen = new ArrayList<LongOpenHashSet>();
        var cells = layout.cells();
        var cursor = new BlockPos.MutableBlockPos();
        var level = world instanceof Level loaded ? loaded : null;
        for (var port : ports) {
            double rx = reference == null ? port.getX() : reference[0];
            double ry = reference == null ? port.getY() : reference[1];
            double rz = reference == null ? port.getZ() : reference[2];
            for (var front : fronts.apply(port)) {
                if (!block.rotationState.test(front)) continue;
                for (var up : ups) {
                    var positions = new LongOpenHashSet();
                    boolean bad = false;
                    double sx = 0, sy = 0, sz = 0;
                    int count = 0;
                    for (int i = 0; i < cells.size() && !bad; i++) {
                        if (items[i] == null || cells.get(i).predicate() instanceof ControllerPredicate) continue;
                        var state = StructureBlocks.stateOf(items[i]);
                        if (state == null || state.isAir() || state.is(Blocks.BARRIER)) continue;
                        layout.worldPos(port, i, front, up, false, cursor);
                        positions.add(cursor.asLong());
                        sx += cursor.getX();
                        sy += cursor.getY();
                        sz += cursor.getZ();
                        count++;
                        if (level != null && !level.isLoaded(cursor)) {
                            bad = true;
                            continue;
                        }
                        int depth = outward == null ? 0 : (cursor.getX() - port.getX()) * outward.getStepX() + (cursor.getY() - port.getY()) * outward.getStepY() +
                                (cursor.getZ() - port.getZ()) * outward.getStepZ();
                        if (!rule.accepts(cursor, world.getBlockState(cursor), state, depth)) bad = true;
                    }
                    if (bad || count == 0 || seen.contains(positions)) continue;
                    seen.add(positions);
                    double score = 0;
                    if (outward != null) {
                        double cx = sx / count - rx, cy = sy / count - ry, cz = sz / count - rz;
                        double along = cx * outward.getStepX() + cy * outward.getStepY() + cz * outward.getStepZ();
                        if (along <= 0) continue;
                        double px = cx - along * outward.getStepX(), py = cy - along * outward.getStepY(), pz = cz - along * outward.getStepZ();
                        score = Math.sqrt(px * px + py * py + pz * pz);
                    }
                    result.add(new DockPose(port.immutable(), front, up, score));
                }
            }
        }
        result.sort(Comparator.comparingDouble(DockPose::score));
        return result;
    }
}
