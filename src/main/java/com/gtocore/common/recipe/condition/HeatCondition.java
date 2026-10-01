package com.gtocore.common.recipe.condition;

import com.gtocore.api.machine.part.IHeatContainerPart;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public final class HeatCondition extends RecipeCondition {

    private final int temperature;
    private final boolean machineCheck;

    public HeatCondition(int temperature) {
        this(false, temperature, false);
    }

    private HeatCondition(boolean isReverse, int temperature, boolean machineCheck) {
        super(isReverse);
        this.temperature = temperature;
        this.machineCheck = machineCheck;
    }

    public static HeatCondition maximumMachineTemperature(int temperature) {
        return new HeatCondition(true, temperature, true);
    }

    @Override
    public Component getTooltips() {
        if (isReverse) {
            return Component.translatable("gtocore.recipe.heat.temperature.reverse", temperature);
        }
        return Component.translatable("gtocore.recipe.heat.temperature", temperature);
    }

    @Override
    public IssueType getIssueType() {
        return machineCheck ? IdleReason.MACHINE_OVERHEATED.type() : GTIssues.INSUFFICIENT_TEMPERATURE;
    }

    @Override
    public void reportFailure(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe, int index) {
        holder.reportIssue(getIssueType(), IssueStage.CONDITION, IO.NONE, null, index, temperature, -1, recipe);
    }

    @Override
    public @Nullable Component describeCurrent(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        long current = currentTemperature(holder);
        if (current < 0) return null;
        return Component.translatable("gtocore.issue.current.temperature", current);
    }

    private long currentTemperature(IRecipeHandlerHolder holder) {
        if (machineCheck && holder instanceof IHeatContainerMachine machine) return (long) machine.getHeatContainer().getTemperature();
        long best = -1;
        if (holder instanceof IMultiController controller) {
            if (holder instanceof ICoilMachine coilMachine) best = coilMachine.getTemperature();
            for (var p : controller.getParts()) {
                if (p instanceof IHeatContainerPart t) best = Math.max(best, (long) t.getHeatContainer().getTemperature());
            }
        } else {
            var container = IHeatContainer.getCapability(holder.self().holder);
            if (container != null) best = (long) container.getTemperature();
        }
        return best;
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (machineCheck) {
            if (holder instanceof IHeatContainerMachine machine) {
                return machine.getHeatContainer().getTemperature() >= temperature;
            }
            var container = IHeatContainer.getCapability(holder.self().holder);
            return container != null && container.getTemperature() >= temperature;
        }
        if (holder instanceof IMultiController controller) {
            if (holder instanceof ICoilMachine coilMachine && coilMachine.getTemperature() >= temperature) {
                return true;
            }
            for (var p : controller.getParts()) {
                if (p instanceof IHeatContainerPart t && t.getHeatContainer().getTemperature() >= temperature) return true;
            }
        } else {
            var container = IHeatContainer.getCapability(holder.self().holder);
            if (container != null) {
                return container.getTemperature() >= temperature;
            }
        }
        return false;
    }
}
