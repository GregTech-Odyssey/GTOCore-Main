package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.gui.overview.OverviewAdapter;
import com.gtocore.api.gui.overview.OverviewDocking;
import com.gtocore.api.gui.overview.OverviewSnapshot;
import com.gtocore.api.machine.ILargeSpaceStationMachine;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.machines.GTOMachineProtocols;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@DataGeneratorScanned
public final class StationOverviewAdapter implements OverviewAdapter {

    public static final StationOverviewAdapter INSTANCE = new StationOverviewAdapter();

    @RegisterLanguage(cn = "空间站总览", en = "Station Overview")
    public static final String LANG_TITLE = "gtocore.machine.space_station.overview.title";
    @RegisterLanguage(cn = "查看整座空间站，并在空接口上扩建舱段", en = "View the whole station and extend it at free ports")
    public static final String LANG_OPEN = "gtocore.machine.space_station.overview.open";
    @RegisterLanguage(cn = "衔接舱接口：左键或右键选择要接入的空间段", en = "Docking port: left or right click to choose a segment")
    public static final String LANG_ANCHOR_DOCKING = "gtocore.machine.space_station.overview.anchor.docking";
    @RegisterLanguage(cn = "舱段接口：左键或右键选择要接入的空间段", en = "Segment port: left or right click to choose a segment")
    public static final String LANG_ANCHOR_JUNCTION = "gtocore.machine.space_station.overview.anchor.junction";
    @RegisterLanguage(cn = "选择空间段", en = "Choose a Segment")
    public static final String LANG_CHOOSE = "gtocore.machine.space_station.overview.choose";
    @RegisterLanguage(cn = "连接舱", en = "Connectors")
    public static final String LANG_CONNECTORS = "gtocore.machine.space_station.overview.connectors";
    @RegisterLanguage(cn = "功能舱", en = "Functional")
    public static final String LANG_FUNCTIONAL = "gtocore.machine.space_station.overview.functional";
    @RegisterLanguage(cn = "这个接口只能接入连接舱", en = "Only connectors fit this port")
    public static final String LANG_CONNECTORS_ONLY = "gtocore.machine.space_station.overview.connectors_only";

    public static final int CONNECTORS = 0;
    public static final int FUNCTIONAL = 1;
    private static final int DOCKING = 0;
    private static final int JUNCTION = 1;
    private static final int RING_SIZE = 4;
    private static final float MARKER_OFFSET = 1.5f;
    private static final List<Kind> KINDS = List.of(new Kind(ANCHOR_CYAN, LANG_ANCHOR_DOCKING), new Kind(ANCHOR_AMBER, LANG_ANCHOR_JUNCTION));
    private static final MachineProtocol[] PROTOCOLS = { GTOMachineProtocols.STATION_DOCKING, GTOMachineProtocols.STATION_JUNCTION };
    private static final OverviewDocking.CellRule RULE = (pos, existing, wanted, depth) -> existing.isAir() ? depth >= 0 :
            existing.getBlock() == wanted.getBlock() && Math.abs(depth) <= 1;

    private StationOverviewAdapter() {}

    public static int categoryOf(MachineDefinition definition) {
        return definition == SpaceMultiblock.SPACE_STATION_DOCKING_MODULE || definition == SpaceMultiblock.SPACE_STATION_TRANSPARENT_DOCKING_MODULE ||
                definition == SpaceMultiblock.SPACE_STATION_EXTENSION_MODULE ? CONNECTORS : FUNCTIONAL;
    }

    @Override
    public String titleKey() {
        return LANG_TITLE;
    }

    @Override
    public String openKey() {
        return LANG_OPEN;
    }

    @Override
    public String chooseKey() {
        return LANG_CHOOSE;
    }

    @Override
    public List<Kind> kinds() {
        return KINDS;
    }

    @Override
    public int legendColor() {
        return ANCHOR_AMBER;
    }

    @Override
    public List<Component> categories() {
        return List.of(Component.translatable(LANG_CONNECTORS), Component.translatable(LANG_FUNCTIONAL));
    }

    @Override
    public int category(MultiblockMachineDefinition definition) {
        return categoryOf(definition);
    }

    @Override
    public String unavailableKey(OverviewSnapshot.Anchor anchor) {
        return anchor.kind() == DOCKING ? LANG_CONNECTORS_ONLY : OverviewAdapter.super.unavailableKey(anchor);
    }

    @Override
    public List<MultiblockMachineDefinition> members(OverviewSnapshot.Anchor anchor) {
        if (anchor.kind() < 0 || anchor.kind() >= PROTOCOLS.length) return Collections.emptyList();
        return OverviewAdapter.multiblockMembers(PROTOCOLS[anchor.kind()]);
    }

    @Override
    public @Nullable MultiblockControllerMachine child(MultiblockControllerMachine parent, MetaMachine found) {
        return found instanceof ILargeSpaceStationMachine station ? station.self() : null;
    }

    @Override
    public boolean acceptsPortBlock(BlockState existing) {
        return OverviewAdapter.super.acceptsPortBlock(existing) || existing.is(GTOBlocks.TITANIUM_ALLOY_FRAME_INTERNAL.get());
    }

    @Override
    public void anchors(MultiblockControllerMachine machine, Assembly assembly, Layout layout, double[] centroid, Level level, Consumer<OverviewSnapshot.Anchor> out) {
        for (int kind = 0; kind < PROTOCOLS.length; kind++) {
            for (var ring : OverviewDocking.rings(assembly.ports(PROTOCOLS[kind]))) {
                var outward = outward(ring, centroid);
                if (outward == null) continue;
                var c = OverviewDocking.center(ring);
                out.accept(new OverviewSnapshot.Anchor(ring, kind, outward, (float) c[0] + 0.5f + outward.getStepX() * MARKER_OFFSET,
                        (float) c[1] + 0.5f + outward.getStepY() * MARKER_OFFSET, (float) c[2] + 0.5f + outward.getStepZ() * MARKER_OFFSET));
            }
        }
    }

    @Override
    public List<OverviewDocking.DockPose> orientations(MultiblockMachineDefinition definition, Layout layout, Item[] items, OverviewSnapshot.Anchor anchor,
                                                       BlockGetter world) {
        var ring = anchor.cells();
        if (ring.size() != RING_SIZE) return Collections.emptyList();
        var c = OverviewDocking.center(ring);
        return OverviewDocking.mount(definition, layout, items, ring, port -> new Direction[] { Direction.getNearest(c[0] - port.getX(), c[1] - port.getY(), c[2] - port.getZ()) },
                OverviewDocking.ups(definition), world, anchor.outward(), c, RULE);
    }

    @Nullable
    private static Direction outward(List<BlockPos> ring, double[] hostCentroid) {
        if (ring.size() != RING_SIZE) return null;
        Direction.Axis normal = null;
        for (var axis : Direction.Axis.values()) {
            boolean same = true;
            for (var q : ring) same &= q.get(axis) == ring.getFirst().get(axis);
            if (same) normal = axis;
        }
        if (normal == null) return null;
        var c = OverviewDocking.center(ring);
        double side = normal.choose(hostCentroid[0] - c[0], hostCentroid[1] - c[1], hostCentroid[2] - c[2]);
        return Direction.fromAxisAndDirection(normal, side < 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
    }
}
