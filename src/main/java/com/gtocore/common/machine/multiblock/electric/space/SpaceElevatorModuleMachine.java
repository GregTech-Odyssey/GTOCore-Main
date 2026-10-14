package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.CustomParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiModule;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToLongFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpaceElevatorModuleMachine extends CustomParallelMultiblockMachine implements IMultiModule<SpaceElevatorMachine> {

    @Nullable
    @Setter
    @Getter
    private SpaceElevatorMachine controller;

    private final boolean powerModuleTier;

    public SpaceElevatorModuleMachine(MetaMachineBlockEntity holder, boolean powerModuleTier) {
        this(holder, powerModuleTier, m -> {
            var module = (SpaceElevatorModuleMachine) m;
            var controller = module.getController();
            if (controller == null || module.getSpaceElevatorTier() <= 7) return 0;
            return parallelLimit(module.isSuper(), controller.getCasingTier(GTORecipeDataKeys.POWER_MODULE_TIER));
        });
    }

    public static int parallelBase(boolean road) {
        return road ? 8 : 4;
    }

    public static long parallelLimit(boolean road, int powerModuleTier) {
        return (long) Math.pow(parallelBase(road), powerModuleTier - 1);
    }

    public static double durationMultiplier(double linkMultiplier, int elevatorTier, boolean road) {
        return Math.sqrt(linkMultiplier / ((elevatorTier - GTValues.ZPM) * (road ? 2 : 1)));
    }

    SpaceElevatorModuleMachine(MetaMachineBlockEntity holder, boolean powerModuleTier, ToLongFunction<CustomParallelMultiblockMachine> getParallel) {
        super(holder, getParallel);
        this.powerModuleTier = powerModuleTier;
    }

    public int getSpaceElevatorTier() {
        if (controller != null && controller.getRecipeLogic().isWorking()) {
            return controller.getTier();
        }
        return 0;
    }

    private boolean isSuper() {
        return controller instanceof SuperSpaceElevatorMachine;
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        var controller = getController();
        if (controller == null || getSpaceElevatorTier() < 8) {
            (controller == null ? IdleReason.SPACE_ELEVATOR_NOT_CONNECTED : IdleReason.SPACE_ELEVATOR_NOT_RUNNING).setReason(this);
            return null;
        }
        if (powerModuleTier && recipe.data.getInt(GTORecipeDataKeys.POWER_MODULE_TIER) > controller.getCasingTier(GTORecipeDataKeys.POWER_MODULE_TIER)) {
            IdleReason.POWER_MODULE_TIER.setReason(this, recipe.data.getInt(GTORecipeDataKeys.POWER_MODULE_TIER), controller.getCasingTier(GTORecipeDataKeys.POWER_MODULE_TIER));
            return null;
        }
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, getParallel());
        if (recipe == null) return null;
        return RecipeModifier.overclocking(this, unit, recipe, false, 1, getDurationMultiplier(), 0.5);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (!super.handleTickRecipe(recipe)) return false;
        if (getOffsetTimer() % 10 == 0) {
            if (getSpaceElevatorTier() >= 8) return true;
            (getController() == null ? IdleReason.SPACE_ELEVATOR_NOT_CONNECTED : IdleReason.SPACE_ELEVATOR_NOT_RUNNING).setReason(this);
            return false;
        }
        return true;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(getSpaceElevatorTier() < 8 ? "gtocore.machine.space_elevator.not_connected" : "gtocore.machine.space_elevator.connected"));
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable("gtocore.machine.duration_multiplier.tooltip", FormattingUtil.formatNumbers(getDurationMultiplier())));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtocore.machine.duration_multiplier.tooltip", MultiblockPage.decimalText(this::getDurationMultiplier, ""));
    }

    @Override
    public boolean handleRecipeOutput(GTRecipe recipe) {
        var result = super.handleRecipeOutput(recipe);
        if (result && controller != null) {
            controller.addModuleWorks(recipe.batchParallels);
        }
        return result;
    }

    private double getDurationMultiplier() {
        double mul = 1;
        if (controller != null) {
            mul = controller.netMachineCache == null ? 1.0d : controller.netMachineCache.getDurationMultiplier();
        }
        return durationMultiplier(mul, getSpaceElevatorTier(), isSuper());
    }
}
