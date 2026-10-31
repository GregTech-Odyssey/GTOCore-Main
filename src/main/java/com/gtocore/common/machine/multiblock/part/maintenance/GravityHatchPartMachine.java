package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtolib.api.machine.feature.IGravityPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.AutoMaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.maintenance.MaintenanceReadouts;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class GravityHatchPartMachine extends AutoMaintenanceHatchPartMachine implements IGravityPartMachine {

    public GravityHatchPartMachine(MetaMachineBlockEntity blockEntity) {
        super(blockEntity);
    }

    private static final int MIN_GRAVITY = 0;
    private static final int MAX_GRAVITY = 100;

    @SaveToDisk(defaultValue = "0")
    private int currentGravity;

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.screenPage(this, screen -> {
            MaintenanceReadouts.addState(screen, this);
            screen.addLine(ACMHatchPartMachine.READ_GRAVITY, MultiblockPage.cached(() -> currentGravity, value -> Component.literal(Long.toString(value))));
        }, controls -> controls.addInt("gtocore.machine.modular_maintenance.gravity_config", this::getCurrentGravity, this::setCurrentGravity, MIN_GRAVITY, MAX_GRAVITY));
    }

    private void setCurrentGravity(int gravity) {
        currentGravity = Mth.clamp(gravity, MIN_GRAVITY, MAX_GRAVITY);
    }

    @Override
    public int getCurrentGravity() {
        return this.currentGravity;
    }
}
