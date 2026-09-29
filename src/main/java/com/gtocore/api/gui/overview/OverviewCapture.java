package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructurePattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class OverviewCapture {

    static final int MAX_MODULES = 64;
    static final int MAX_CELLS = 2_000_000;
    private static final int COVER_DEPTH = 3;

    private record Pending(MultiblockControllerMachine machine, @Nullable MultiblockControllerMachine parent, boolean parentConnected) {}

    private final OverviewAdapter adapter;
    private final Level level;
    private final LongOpenHashSet own;
    private final Long2ObjectOpenHashMap<int[]> reuse;
    private final LongOpenHashSet visited = new LongOpenHashSet();
    private final ArrayDeque<Pending> queue = new ArrayDeque<>();
    private final List<OverviewSnapshot.Module> modules = new ArrayList<>();
    private final List<OverviewSnapshot.Anchor> anchors = new ArrayList<>();
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private long signature = 17;
    private int total;
    private boolean truncated;
    @Nullable
    private Pending current;
    @Nullable
    private Layout layout;
    @Nullable
    private Assembly assembly;
    private int[] values = new int[0];
    private int[] blocks = new int[0];
    private Direction front = Direction.NORTH, up = Direction.NORTH;
    private boolean flip, connected;
    private int index, count;
    private double cx, cy, cz;

    OverviewCapture(OverviewAdapter adapter, MultiblockControllerMachine host, Level level, LongOpenHashSet own, Long2ObjectOpenHashMap<int[]> reuse) {
        this.adapter = adapter;
        this.level = level;
        this.own = own;
        this.reuse = reuse;
        queue.add(new Pending(host, null, true));
        visited.add(host.getPos().asLong());
    }

    OverviewSnapshot result() {
        return new OverviewSnapshot(modules, anchors, truncated ? OverviewSnapshot.TRUNCATED : 0);
    }

    long signature() {
        return truncated ? signature * 31 + 1 : signature;
    }

    boolean step(int budget) {
        int left = budget;
        while (left > 0) {
            if (current == null && !begin()) return true;
            var machine = current.machine;
            int size = layout.cells().size();
            int start = index;
            int end = (int) Math.min(size, (long) index + left);
            for (; index < end; index++) scan(machine, index);
            left -= Math.max(1, end - start);
            if (index >= size) complete(machine);
        }
        return current == null && queue.isEmpty();
    }

    private boolean begin() {
        while (!queue.isEmpty()) {
            var pending = queue.poll();
            var machine = pending.machine;
            var definition = machine.getDefinition();
            var structure = definition.hasStructure() && definition.getPatternFactory()[0].get() instanceof StructurePattern pattern ? pattern.getStructure() : null;
            if (structure == null) continue;
            assembly = machine.isFormed() ? machine.getAssembly() : null;
            values = assembly != null ? structure.valuesOf(assembly) : structure.defaultValues();
            layout = structure.layout(values);
            if (layout == null) {
                values = structure.defaultValues();
                layout = structure.layout(values);
            }
            if (layout == null) continue;
            int cells = layout.cells().size();
            if (modules.size() >= MAX_MODULES || total + cells > MAX_CELLS) {
                truncated = true;
                queue.clear();
                layout = null;
                assembly = null;
                return false;
            }
            front = machine.getFrontFacing();
            up = machine.getUpwardsFacing();
            flip = machine.isFlipped();
            connected = machine.isFormed() && pending.parentConnected && (pending.parent == null || adapter.linked(pending.parent, machine));
            var old = reuse.remove(machine.getPos().asLong());
            if (old != null && old.length == cells) {
                Arrays.fill(old, 0);
                blocks = old;
            } else {
                blocks = new int[cells];
            }
            own.clear();
            total += cells;
            index = 0;
            count = 0;
            cx = cy = cz = 0;
            current = pending;
            return true;
        }
        return false;
    }

    private void scan(MultiblockControllerMachine machine, int i) {
        var pos = layout.worldPos(machine.getPos(), i, front, up, flip, cursor);
        if (!level.isLoaded(pos)) return;
        var state = level.getBlockState(pos);
        if (state.isAir()) return;
        blocks[i] = Block.getId(state);
        cx += pos.getX();
        cy += pos.getY();
        cz += pos.getZ();
        count++;
        own.add(pos.asLong());
        if (!state.hasBlockEntity()) return;
        var found = MetaMachine.getMachine(level, pos);
        if (found == null) return;
        var child = adapter.child(machine, found);
        if (child != null && child != machine && visited.add(child.getPos().asLong())) queue.add(new Pending(child, machine, connected));
    }

    private void complete(MultiblockControllerMachine machine) {
        byte state = connected ? OverviewSnapshot.CONNECTED : OverviewSnapshot.DETACHED;
        modules.add(new OverviewSnapshot.Module(machine.getPos().immutable(), machine.getDefinition().getId(), front, up, flip, values, state, blocks));
        signature = signature * 31 + machine.getPos().asLong();
        signature = signature * 31 + state;
        signature = signature * 31 + Arrays.hashCode(values);
        signature = signature * 31 + Arrays.hashCode(blocks);
        if (assembly != null && connected && count > 0) {
            var centroid = new double[] { cx / count, cy / count, cz / count };
            int before = anchors.size();
            adapter.anchors(machine, assembly, layout, centroid, level, anchor -> {
                if (!occupied(level, anchor.cells()) && !covered(anchor)) anchors.add(anchor);
            });
            for (int i = before; i < anchors.size(); i++) signature = signature * 31 + anchors.get(i).cells().getFirst().asLong();
        }
        current = null;
        layout = null;
        assembly = null;
    }

    private boolean covered(OverviewSnapshot.Anchor anchor) {
        var outward = anchor.outward();
        for (var port : anchor.cells()) {
            for (int step = 1; step <= COVER_DEPTH; step++) {
                cursor.setWithOffset(port, outward.getStepX() * step, outward.getStepY() * step, outward.getStepZ() * step);
                if (own.contains(cursor.asLong()) || !level.isLoaded(cursor)) continue;
                if (!level.getBlockState(cursor).isAir()) return true;
            }
        }
        return false;
    }

    static boolean occupied(Level level, List<BlockPos> cells) {
        for (var port : cells) {
            if (level.isLoaded(port) && MetaMachine.getMachine(level, port) != null) return true;
        }
        return false;
    }
}
