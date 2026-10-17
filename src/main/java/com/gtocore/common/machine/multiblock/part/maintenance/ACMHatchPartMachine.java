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
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ACMHatchPartMachine extends WorkableTieredPartMachine implements IMaintenanceMachine, IMachineLife {

    private static final double DURATION_STEP = 0.01;

    @RegisterLanguage(cn = "处理耗时倍率", en = "Duration Multiplier")
    private static final String DURATION = "gtocore.machine.maintenance_hatch.duration_multiplier";

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
        return MachineDisplay.page(this, list -> list.add(getTextWidgetText(this::getDurationMultiplier)), this::addMaintenanceControls);
    }

    protected void addMaintenanceControls(ControlPanel controls) {
        controls.addDecimal(DURATION, () -> durationMultiplier, value -> durationMultiplier = (float) value,
                () -> GTORules.CONFIGURABLE_MAINTENANCE_MIN.get(), () -> GTORules.CONFIGURABLE_MAINTENANCE_MAX.get(), DURATION_STEP);
    }

    protected static Component getTextWidgetText(Supplier<Float> multiplier) {
        Component tooltip;
        String format = String.format("%.2f", multiplier.get());
        if (multiplier.get() == 1.0) {
            tooltip = Component.translatable("gtceu.maintenance.configurable_" + "duration" + ".unchanged_description");
        } else {
            tooltip = Component.translatable("gtceu.maintenance.configurable_" + "duration" + ".changed_description", format);
        }
        return Component.translatable("gtceu.maintenance.configurable_" + "duration", format).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }

    @Override
    public float getDurationMultiplier() {
        return this.durationMultiplier;
    }

    @RegisterLanguage(cn = "§7按下§f Shift 键§7放置以自动设置配方时间乘数为最低§r", en = "§7Hold§f Shift §7while placing to automatically set recipe time multiplier to minimum§r")
    public static final String LANG_PLACEMENT_TOOLTIP = "gtocore.machine.maintenance_hatch_placement_tooltip";
}
