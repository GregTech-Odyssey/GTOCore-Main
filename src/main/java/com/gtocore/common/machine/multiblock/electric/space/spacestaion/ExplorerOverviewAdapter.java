package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.gui.overview.OverviewAdapter;
import com.gtocore.api.gui.overview.OverviewDocking;
import com.gtocore.api.gui.overview.OverviewSnapshot;
import com.gtocore.common.data.machines.GTOMachineProtocols;
import com.gtocore.common.machine.multiblock.generator.PhotovoltaicSailControllerMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@DataGeneratorScanned
public final class ExplorerOverviewAdapter implements OverviewAdapter {

    public static final ExplorerOverviewAdapter INSTANCE = new ExplorerOverviewAdapter();

    @RegisterLanguage(cn = "探索者号空间站总览", en = "Explorer Station Overview")
    public static final String LANG_TITLE = "gtocore.machine.space_station.explorer.overview.title";
    @RegisterLanguage(cn = "查看空间站与光伏帆板，并在空位上安装帆板", en = "View the station with its photovoltaic sails and install sails at free docks")
    public static final String LANG_OPEN = "gtocore.machine.space_station.explorer.overview.open";
    @RegisterLanguage(cn = "帆板接口：左键或右键选择要安装的光伏帆板", en = "Sail dock: left or right click to choose a photovoltaic sail")
    public static final String LANG_ANCHOR = "gtocore.machine.space_station.explorer.overview.anchor";
    @RegisterLanguage(cn = "选择光伏帆板", en = "Choose a Photovoltaic Sail")
    public static final String LANG_CHOOSE = "gtocore.machine.space_station.explorer.overview.choose";
    @RegisterLanguage(cn = "光伏帆板", en = "Photovoltaic Sails")
    public static final String LANG_SAILS = "gtocore.machine.space_station.explorer.overview.sails";

    private static final List<Kind> KINDS = Collections.singletonList(new Kind(ANCHOR_GOLD, LANG_ANCHOR));

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
    public List<Component> categories() {
        return Collections.singletonList(Component.translatable(LANG_SAILS));
    }

    @Override
    public int category(MultiblockMachineDefinition definition) {
        return 0;
    }

    @Override
    public List<MultiblockMachineDefinition> members(OverviewSnapshot.Anchor anchor) {
        return OverviewAdapter.multiblockMembers(GTOMachineProtocols.PHOTOVOLTAIC_SAIL);
    }

    @Override
    public @Nullable MultiblockControllerMachine child(MultiblockControllerMachine parent, MetaMachine found) {
        return parent instanceof SimpleSpaceStationMachine && found instanceof PhotovoltaicSailControllerMachine sail ? sail : null;
    }

    @Override
    public void anchors(MultiblockControllerMachine machine, Assembly assembly, Layout layout, double[] centroid, Level level, Consumer<OverviewSnapshot.Anchor> out) {
        if (!(machine instanceof SimpleSpaceStationMachine)) return;
        var axis = machine.getFrontFacing().getAxis();
        for (var port : assembly.ports(GTOMachineProtocols.PHOTOVOLTAIC_SAIL)) {
            var outward = switch (axis) {
                case X -> port.getZ() >= centroid[2] ? Direction.SOUTH : Direction.NORTH;
                case Z -> port.getX() >= centroid[0] ? Direction.EAST : Direction.WEST;
                case Y -> OverviewDocking.horizontalAway(port, centroid);
            };
            out.accept(new OverviewSnapshot.Anchor(Collections.singletonList(port.immutable()), 0, outward, port.getX() + 0.5f, port.getY() + 0.5f, port.getZ() + 0.5f));
        }
    }

    @Override
    public List<OverviewDocking.DockPose> orientations(MultiblockMachineDefinition definition, Layout layout, Item[] items, OverviewSnapshot.Anchor anchor,
                                                       BlockGetter world) {
        return OverviewDocking.mount(definition, layout, items, anchor.cells(), OverviewDocking.ANY_HORIZONTAL, OverviewDocking.FIXED_UP, world, anchor.outward(),
                null, OverviewDocking.FREE);
    }
}
