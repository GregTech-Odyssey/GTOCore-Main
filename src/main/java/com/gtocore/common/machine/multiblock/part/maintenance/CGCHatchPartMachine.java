package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IGravityPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.uiwidgets.display.MachineScreen;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
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
    protected void addReadouts(MachineScreen screen) {
        super.addReadouts(screen);
        screen.addLine(READ_GRAVITY, MultiblockPage.cached(() -> currentGravity, value -> Component.literal(Long.toString(value))));
    }

    @Override
    protected void addMaintenanceControls(ControlPanel controls) {
        super.addMaintenanceControls(controls);
        controls.addInt("gtocore.machine.modular_maintenance.gravity_config", () -> currentGravity, value -> currentGravity = value, MIN_GRAVITY, MAX_GRAVITY);
    }

    private static final int MIN_GRAVITY = 0;
    private static final int MAX_GRAVITY = 100;

    @RegisterLanguage(cn = "重力可调：%s ~ %s", en = "Adjustable gravity: %s ~ %s")
    private static final String TIP_GRAVITY = "gtocore.machine.configurable_maintenance.gravity";
    @RegisterLanguage(cn = "无重力配方需设为 %s，强重力配方需设为 %s", en = "Zero-gravity recipes need %s; strong-gravity recipes need %s")
    private static final String TIP_GRAVITY_RECIPES = "gtocore.machine.configurable_maintenance.gravity_recipes";

    public static TipSpec tips() {
        return tipsBuilder().group(null, l -> {
            l.feature(Component.translatable(TIP_GRAVITY, MIN_GRAVITY, MAX_GRAVITY));
            l.detail(Component.translatable(TIP_GRAVITY_RECIPES, MIN_GRAVITY, MAX_GRAVITY));
        }).build();
    }
}
