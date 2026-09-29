package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.api.gui.overview.OverviewAdapter;
import com.gtocore.api.gui.overview.OverviewDocking;
import com.gtocore.api.gui.overview.OverviewSnapshot;
import com.gtocore.api.gui.overview.OverviewWidget;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.machines.GTOMachineProtocols;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PlayerSupply;
import com.gregtechceu.gtceu.api.pattern.ControllerPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@DataGeneratorScanned
public final class ElevatorOverviewAdapter implements OverviewAdapter {

    @RegisterLanguage(cn = "太空电梯总览", en = "Space Elevator Overview")
    public static final String LANG_TITLE = "gtocore.machine.space_elevator.overview.title";
    @RegisterLanguage(cn = "通天之路总览", en = "Road of Heaven Overview")
    public static final String LANG_TITLE_ROAD = "gtocore.machine.road_of_heaven.overview.title";
    @RegisterLanguage(cn = "查看整座太空电梯，并在空接口上安装模块", en = "View the whole elevator and install modules at free ports")
    public static final String LANG_OPEN = "gtocore.machine.space_elevator.overview.open";
    @RegisterLanguage(cn = "选择模块", en = "Choose a Module")
    public static final String LANG_CHOOSE = "gtocore.machine.space_elevator.overview.choose";
    @RegisterLanguage(cn = "模块接口：左键或右键选择要安装的模块", en = "Module port: left or right click to choose a module")
    public static final String LANG_ANCHOR_MODULE = "gtocore.machine.space_elevator.overview.anchor.module";
    @RegisterLanguage(cn = "巨型模块接口：左键或右键选择要安装的巨型模块", en = "Mega module port: left or right click to choose a mega module")
    public static final String LANG_ANCHOR_MEGA = "gtocore.machine.space_elevator.overview.anchor.mega";
    @RegisterLanguage(cn = "这个接口只能安装太空电梯模块", en = "Only space elevator modules fit this port")
    public static final String LANG_MODULE_ONLY = "gtocore.machine.space_elevator.overview.module_only";
    @RegisterLanguage(cn = "这个接口只能安装巨型太空电梯模块", en = "Only mega space elevator modules fit this port")
    public static final String LANG_MEGA_ONLY = "gtocore.machine.space_elevator.overview.mega_only";

    public static final ElevatorOverviewAdapter ELEVATOR = new ElevatorOverviewAdapter(false);
    public static final ElevatorOverviewAdapter ROAD = new ElevatorOverviewAdapter(true);

    private static final int MODULE = 0;
    private static final int MEGA = 1;
    private static final List<Kind> KINDS = List.of(new Kind(ANCHOR_CYAN, LANG_ANCHOR_MODULE), new Kind(ANCHOR_AMBER, LANG_ANCHOR_MEGA));
    private static final OverviewDocking.CellRule RULE = (pos, existing, wanted, depth) -> OverviewDocking.FREE.accepts(pos, existing, wanted, depth) ||
            existing.is(GTOBlocks.HIGH_STRENGTH_CONCRETE.get()) && wanted.is(GTOBlocks.MODULE_CONNECTOR.get());

    private final boolean road;
    private final MachineProtocol[] protocols;

    private ElevatorOverviewAdapter(boolean road) {
        this.road = road;
        this.protocols = road ? new MachineProtocol[] { GTOMachineProtocols.SPACE_ELEVATOR_MODULE, GTOMachineProtocols.MEGA_SPACE_ELEVATOR_MODULE } :
                new MachineProtocol[] { GTOMachineProtocols.SPACE_ELEVATOR_MODULE };
    }

    public static ElevatorOverviewAdapter of(SpaceElevatorMachine machine) {
        return machine instanceof SuperSpaceElevatorMachine ? ROAD : ELEVATOR;
    }

    @Override
    public String titleKey() {
        return road ? LANG_TITLE_ROAD : LANG_TITLE;
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
    public List<Component> categories() {
        var list = new ArrayList<Component>(protocols.length);
        for (var protocol : protocols) list.add(protocol.getName());
        return list;
    }

    @Override
    public int category(MultiblockMachineDefinition definition) {
        return road && GTOMachineProtocols.MEGA_SPACE_ELEVATOR_MODULE.accepts(definition) ? MEGA : MODULE;
    }

    @Override
    public String unavailableKey(OverviewSnapshot.Anchor anchor) {
        return anchor.kind() == MEGA ? LANG_MEGA_ONLY : LANG_MODULE_ONLY;
    }

    @Override
    public List<MultiblockMachineDefinition> members(OverviewSnapshot.Anchor anchor) {
        if (anchor.kind() < 0 || anchor.kind() >= protocols.length) return Collections.emptyList();
        return OverviewAdapter.multiblockMembers(protocols[anchor.kind()]);
    }

    @Override
    public @Nullable MultiblockControllerMachine child(MultiblockControllerMachine parent, MetaMachine found) {
        return parent instanceof SpaceElevatorMachine && found instanceof SpaceElevatorModuleMachine module ? module : null;
    }

    @Override
    public boolean linked(MultiblockControllerMachine parent, MultiblockControllerMachine child) {
        return child instanceof SpaceElevatorModuleMachine module && module.getController() == parent;
    }

    @Override
    public void anchors(MultiblockControllerMachine machine, Assembly assembly, Layout layout, double[] centroid, Level level, Consumer<OverviewSnapshot.Anchor> out) {
        if (!(machine instanceof SpaceElevatorMachine)) return;
        for (int kind = 0; kind < protocols.length; kind++) {
            for (var port : assembly.ports(protocols[kind])) {
                out.accept(new OverviewSnapshot.Anchor(Collections.singletonList(port.immutable()), kind, OverviewDocking.horizontalAway(port, centroid), port.getX() + 0.5f,
                        port.getY() + 0.5f, port.getZ() + 0.5f));
            }
        }
    }

    @Override
    public void prepare(ServerPlayer player, @Nullable PlayerSupply supply, MultiblockMachineDefinition definition, Layout layout, Item[] choices,
                        OverviewDocking.DockPose pose, ServerLevel level) {
        var connector = GTOBlocks.MODULE_CONNECTOR.get();
        var connectorItem = connector.asItem();
        var concrete = GTOBlocks.HIGH_STRENGTH_CONCRETE.get();
        var cells = layout.cells();
        var cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i < cells.size(); i++) {
            if (choices[i] != connectorItem) continue;
            var predicate = cells.get(i).predicate();
            if (predicate instanceof ControllerPredicate || !predicate.candidateItems().contains(connectorItem)) continue;
            layout.worldPos(pose.port(), i, pose.front(), pose.up(), false, cursor);
            if (!level.isLoaded(cursor) || !level.getBlockState(cursor).is(concrete) || !level.mayInteract(player, cursor)) continue;
            if (supply != null) {
                if (!supply.take(connectorItem)) {
                    player.sendSystemMessage(Component.translatable(OverviewWidget.LANG_MISSING_BLOCK, new ItemStack(connector).getHoverName()));
                    return;
                }
                player.getInventory().placeItemBackInInventory(new ItemStack(concrete));
            }
            level.setBlock(cursor.immutable(), connector.defaultBlockState(), 3);
        }
    }

    @Override
    public List<OverviewDocking.DockPose> orientations(MultiblockMachineDefinition definition, Layout layout, Item[] items, OverviewSnapshot.Anchor anchor,
                                                       BlockGetter world) {
        return OverviewDocking.mount(definition, layout, items, anchor.cells(), OverviewDocking.ANY_HORIZONTAL, OverviewDocking.ups(definition), world, null, null,
                RULE);
    }
}
