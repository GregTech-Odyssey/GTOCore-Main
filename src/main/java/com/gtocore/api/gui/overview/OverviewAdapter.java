package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Assembly;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PlayerSupply;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface OverviewAdapter {

    int ANCHOR_CYAN = 0x3FC7E6;
    int ANCHOR_AMBER = 0xF2A533;
    int ANCHOR_GOLD = 0xF2C94C;

    record Kind(int color, String tooltipKey) {}

    static List<MultiblockMachineDefinition> multiblockMembers(MachineProtocol protocol) {
        return ProtocolMembers.of(protocol);
    }

    String titleKey();

    String openKey();

    String chooseKey();

    List<Kind> kinds();

    default int legendColor() {
        return kinds().getFirst().color();
    }

    List<Component> categories();

    int category(MultiblockMachineDefinition definition);

    default String unavailableKey(OverviewSnapshot.Anchor anchor) {
        return OverviewWidget.LANG_CATEGORY_UNAVAILABLE;
    }

    List<MultiblockMachineDefinition> members(OverviewSnapshot.Anchor anchor);

    @Nullable
    default MultiblockControllerMachine child(MultiblockControllerMachine parent, MetaMachine found) {
        return null;
    }

    default boolean linked(MultiblockControllerMachine parent, MultiblockControllerMachine child) {
        return true;
    }

    default boolean acceptsPortBlock(BlockState existing) {
        return existing.isAir() || existing.is(Blocks.BARRIER);
    }

    void anchors(MultiblockControllerMachine machine, Assembly assembly, Layout layout, double[] centroid, Level level, Consumer<OverviewSnapshot.Anchor> out);

    List<OverviewDocking.DockPose> orientations(MultiblockMachineDefinition definition, Layout layout, Item[] items, OverviewSnapshot.Anchor anchor, BlockGetter world);

    default void prepare(ServerPlayer player, @Nullable PlayerSupply supply, MultiblockMachineDefinition definition, Layout layout, Item[] choices,
                         OverviewDocking.DockPose pose, ServerLevel level) {}
}
