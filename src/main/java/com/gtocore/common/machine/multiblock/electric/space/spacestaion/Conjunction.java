package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.machine.ILargeSpaceStationMachine;

import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.VA;

public class Conjunction extends AbstractSpaceStation implements ILargeSpaceStationMachine {

    protected Core core;

    public Conjunction(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
        shouldShowReadyText = false;
    }

    @Override
    public @Nullable Core getRoot() {
        return core;
    }

    @Override
    public void setRoot(@Nullable Core root) {
        core = root;
    }

    @Override
    public ConnectType getConnectType() {
        return ConnectType.CONJUNCTION;
    }

    @Override
    public long getEUt() {
        return VA[HV];
    }

    @Override
    public boolean isWorkspaceReady() {
        return core != null && core.isWorkspaceReady();
    }

    @Override
    public Component getWorkspaceNotReadyReason() {
        return core == null ? IdleReason.SPACE_STATION_NO_CORE.reason(getPos().toShortString()) : core.getWorkspaceNotReadyReason();
    }

    @Override
    protected void tickReady() {
        tickNonCoreModule();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onFormed();
        markDirty(true);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        onInvalid();
        markDirty(true);
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        markDirty(true);
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        ILargeSpaceStationMachine.super.customText(list);
    }
}
