package com.gtocore.api.machine;

import com.gtocore.client.forge.ForgeClientEvent;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.AbstractSpaceStation;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.Core;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.ISpacePredicateMachine;

import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.gtolib.api.machine.feature.multiblock.ICustomHighlightMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import earth.terrarium.adastra.api.planets.PlanetApi;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.gtocore.api.machine.ILargeSpaceStationMachine.ConnectType.*;

public interface ILargeSpaceStationMachine extends ICustomHighlightMachine, ISpacePredicateMachine, ICustomRecipeLogicHolder {

    int ROOT_HIGHLIGHT = 0xFFFFFF;

    MultiblockControllerMachine self();

    @Override
    default Set<CleanroomType> getTypes() {
        if (getRoot() != null) {
            return getRoot().getTypes();
        }
        return Collections.emptySet();
    }

    default void markDirty(boolean dirty) {
        if (getRoot() != null) getRoot().markDirty(dirty);
        else IIWirelessInteractor.getMachineNet(getLevel(), Core.class).forEach(core -> core.markDirty(dirty));
    }

    @Nullable
    Core getRoot();

    void setRoot(@Nullable Core root);

    default void tickNonCoreModule() {
        var time = getOffsetTimer();
        if (time % 80 == 0) {
            AbstractSpaceStation self = (AbstractSpaceStation) self();

            if (firstLoad() || time % 800 == 0) {
                if (getRoot() != null && getRoot().getReadyCount() > 0) {
                    provideOxygen();
                } else {
                    self.clearOxygenBlocks();
                }
            }

            self.updateSpaceMachines();
        }
    }

    ConnectType getConnectType();

    long getEUt();

    default Set<ILargeSpaceStationMachine> getConnectedModules() {
        if (getLevel() == null) return Collections.emptySet();

        Set<ILargeSpaceStationMachine> machines = new ReferenceOpenHashSet<>();
        for (BlockPos pos : ((AbstractSpaceStation) self()).getStationPorts()) {
            var blockEntity = getLevel().getBlockEntity(pos);
            if (blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity) {
                var machine = metaMachineBlockEntity.getMetaMachine();
                if (machine instanceof ILargeSpaceStationMachine largeSpaceStationMachine) {
                    machines.add(largeSpaceStationMachine);
                }
            }
        }
        return machines;
    }

    @Override
    default List<ForgeClientEvent.HighlightNeed> getCustomHighlights() {
        int color = getConnectType().color;
        var ports = ((AbstractSpaceStation) self()).getStationPorts();
        var root = getRoot();
        var list = new ArrayList<ForgeClientEvent.HighlightNeed>(ports.length + 1);
        for (var pos : ports) list.add(new ForgeClientEvent.HighlightNeed(pos, pos, color));
        if (root != null) list.add(new ForgeClientEvent.HighlightNeed(root.self().getPos(), root.self().getPos(), ROOT_HIGHLIGHT));
        return list;
    }

    @Override
    default GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (!PlanetApi.API.isSpace(getLevel())) {
            IdleReason.SPACE_STATION_NOT_IN_SPACE.setReason(this);
            return null;
        }
        if (getRoot() == null || !getRoot().isWorkspaceReady()) {
            setIdleReason(this::getWorkspaceNotReadyReason);
            return null;
        }

        return ((IEnhancedRecipeLogicMachine) self()).getRecipeBuilder().duration(200)
                .build();
    }

    default void customText(@NotNull List<Component> list) {
        list.add(Component.translatable("gui.ae2.PowerUsageRate", "%s EU/t".formatted(FormattingUtil.formatNumbers(getEUt()))).withStyle(ChatFormatting.YELLOW));
        if (getRoot() != null) {
            list.add(Component.translatable("gui.ae2.AttachedTo", "[" + getRoot().getPos().toShortString() + "]"));
            getRoot().customText(list);
        } else list.add(Component.translatable("theoneprobe.ae2.p2p_unlinked"));
    }

    enum ConnectType {

        CONJUNCTION(0xFFFF00),
        MODULE(0x00FFFF),
        CORE(0xFF0000);

        public final int color;

        ConnectType(int color) {
            this.color = color;
        }
    }
}
