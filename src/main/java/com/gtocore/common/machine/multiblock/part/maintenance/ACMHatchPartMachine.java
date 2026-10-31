package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.display.MachineScreen;
import com.gregtechceu.gtceu.uiwidgets.maintenance.MaintenanceReadouts;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ACMHatchPartMachine extends WorkableTieredPartMachine implements IMaintenanceMachine, IMachineLife {

    private static final double DURATION_STEP = 0.01;

    @RegisterLanguage(cn = "处理耗时倍率", en = "Duration Multiplier")
    private static final String DURATION = "gtocore.machine.maintenance_hatch.duration_multiplier";
    @RegisterLanguage(cn = "数值越低配方越快，自动维护不累积损坏程度", en = "Lower values mean faster recipes; automatic maintenance accumulates no damage")
    private static final String DURATION_TOOLTIP = "gtocore.machine.maintenance_hatch.duration_multiplier.tooltip";
    @RegisterLanguage(cn = "洁净环境", en = "Cleanroom")
    protected static final String READ_CLEANROOM = "gtocore.machine.maintenance_hatch.read.cleanroom";
    @RegisterLanguage(cn = "重力", en = "Gravity")
    protected static final String READ_GRAVITY = "gtocore.machine.maintenance_hatch.read.gravity";
    @RegisterLanguage(cn = "真空等级", en = "Vacuum Tier")
    protected static final String READ_VACUUM = "gtocore.machine.maintenance_hatch.read.vacuum";
    @RegisterLanguage(cn = "温度", en = "Temperature")
    protected static final String READ_TEMPERATURE = "gtocore.machine.maintenance_hatch.read.temperature";

    @SaveToDisk(defaultValue = "1.0")
    @SyncToClient
    private float durationMultiplier = 1.0F;

    public ACMHatchPartMachine(MetaMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId, 5);
    }

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public boolean isTaped() {
        return false;
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public byte startProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public byte getMaintenanceProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public void setMaintenanceProblems(byte problems) {}

    @Override
    public int getTimeActive() {
        return 0;
    }

    @Override
    public void setTimeActive(int time) {}

    @Override
    @Nullable
    public GTRecipe modifyRecipe(IWorkableMultiController controller, RecipeHandlerUnit unit, GTRecipe recipe) {
        recipe.duration = Math.max(1, (int) (recipe.duration * durationMultiplier));
        return recipe;
    }

    @Override
    public void onMachinePlaced(@org.jetbrains.annotations.Nullable LivingEntity player, ItemStack stack) {
        if (player != null && player.isShiftKeyDown()) {
            durationMultiplier = GTORules.CONFIGURABLE_MAINTENANCE_MIN.get();
        }
    }

    @Override
    public float getTimeMultiplier() {
        var result = 1.0F;
        if (durationMultiplier < 1.0) result = -20 * durationMultiplier + 21;
        else result = -8 * durationMultiplier + 9;
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.screenPage(this, this::addReadouts, this::addMaintenanceControls);
    }

    protected void addReadouts(MachineScreen screen) {
        MaintenanceReadouts.addState(screen, this);
        MaintenanceReadouts.addDuration(screen, this::getDurationMultiplier);
    }

    protected void addMaintenanceControls(ControlPanel controls) {
        controls.addDecimal(DURATION, () -> durationMultiplier, value -> durationMultiplier = (float) value,
                () -> GTORules.CONFIGURABLE_MAINTENANCE_MIN.get(), () -> GTORules.CONFIGURABLE_MAINTENANCE_MAX.get(), DURATION_STEP, DURATION_TOOLTIP);
    }

    @Override
    public float getDurationMultiplier() {
        return this.durationMultiplier;
    }

    @RegisterLanguage(cn = "全自动维护，不出现维护问题", en = "Fully automatic maintenance; no maintenance problems occur")
    private static final String TIP_SUMMARY = "gtocore.machine.configurable_maintenance.summary";
    @RegisterLanguage(cn = "处理耗时倍率", en = "Duration Multiplier")
    private static final String TIP_DURATION_HEADING = "gtocore.machine.configurable_maintenance.duration_heading";
    @RegisterLanguage(cn = "在难度规则的上下限内调节", en = "Adjustable within the difficulty rule limits")
    private static final String TIP_DURATION_FREE = "gtocore.machine.configurable_maintenance.duration_free";
    @RegisterLanguage(cn = "高负荷不累积损坏程度", en = "High load accumulates no damage")
    private static final String TIP_NO_COST = "gtocore.machine.configurable_maintenance.no_cost";
    @RegisterLanguage(cn = "作用于超频前的基础耗时，功率不变", en = "Applied to the base duration before overclocking; power draw is unchanged")
    private static final String TIP_BEFORE_OVERCLOCK = "gtocore.machine.configurable_maintenance.before_overclock";
    @RegisterLanguage(cn = "潜行放置", en = "Sneak + Place")
    private static final String TIP_SNEAK_PLACE = "gtocore.machine.configurable_maintenance.sneak_place";
    @RegisterLanguage(cn = "倍率设为下限", en = "Set multiplier to minimum")
    private static final String TIP_SET_MIN = "gtocore.machine.configurable_maintenance.set_min";

    public static TipSpec.Builder tipsBuilder() {
        return TipSpec.builder()
                .summary(Component.translatable(TIP_SUMMARY))
                .group(Component.translatable(TIP_DURATION_HEADING), l -> {
                    l.feature(Component.translatable(TIP_DURATION_FREE));
                    l.bonus(Component.translatable(TIP_NO_COST));
                    l.detail(Component.translatable(TIP_BEFORE_OVERCLOCK));
                    l.rightClick(Tips.keyAction(Component.translatable(TIP_SNEAK_PLACE), Component.translatable(TIP_SET_MIN)));
                });
    }

    public static TipSpec tips() {
        return tipsBuilder().build();
    }
}
