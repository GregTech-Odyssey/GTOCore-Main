package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtolib.api.machine.feature.IGravityPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.gto.datasynclib.annotations.SaveToDisk;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CGCHatchPartMachine extends ACMHatchPartMachine implements IGravityPartMachine {

    @SaveToDisk(defaultValue = "0")
    private int currentGravity;

    public CGCHatchPartMachine(MetaMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int getCurrentGravity() {
        return currentGravity;
    }

    @Override
    protected void addMaintenanceControls(ControlPanel controls) {
        super.addMaintenanceControls(controls);
        controls.addInt("gtocore.machine.modular_maintenance.gravity_config", () -> currentGravity, value -> currentGravity = value, 0, 100);
    }
}
