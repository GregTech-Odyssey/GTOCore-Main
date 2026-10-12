package com.gtocore.common.recipe.condition;

import com.gtocore.api.machine.part.IHeatContainerPart;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;

public final class HeatCondition extends RecipeCondition {

    private final int temperature;
    // 为 true 时检查机器自身的热容器，否则检查外部热源或线圈。
    private final boolean machineCheck;

    public HeatCondition(int temperature) {
        this(false, temperature, false);
    }

    public HeatCondition(boolean isReverse, int temperature, boolean machineCheck) {
        super(isReverse);
        this.temperature = temperature;
        this.machineCheck = machineCheck;
    }

    public static HeatCondition maximumMachineTemperature(int temperature) {
        return new HeatCondition(true, temperature, true);
    }

    /** 外部热源温度必须严格低于给定温度。 */
    public static HeatCondition maximumTemperature(int temperature) {
        return new HeatCondition(true, temperature, false);
    }

    @Override
    public Component getTooltips() {
        if (isReverse) {
            return Component.translatable("gtocore.recipe.heat.temperature.reverse", temperature);
        }
        return Component.translatable("gtocore.recipe.heat.temperature", temperature);
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (machineCheck) {
            if (holder instanceof IHeatContainerMachine machine) {
                return machine.getHeatContainer().getTemperature() >= temperature;
            }
            var container = IHeatContainer.getCapability(holder.self().holder);
            return container == null ? isReverse : container.getTemperature() >= temperature;
        }
        if (holder instanceof IMultiController controller) {
            boolean hasHeatSource = false;
            if (holder instanceof ICoilMachine coilMachine) {
                hasHeatSource = true;
                if (coilMachine.getTemperature() >= temperature) return true;
            }
            for (var p : controller.getParts()) {
                if (p instanceof IHeatContainerPart t) {
                    hasHeatSource = true;
                    if (t.getHeatContainer().getTemperature() >= temperature) return true;
                }
            }
            // check() 会反转结果；缺少热源时两种温度限制都应失败。
            return !hasHeatSource && isReverse;
        } else {
            var container = IHeatContainer.getCapability(holder.self().holder);
            if (container != null) {
                return container.getTemperature() >= temperature;
            }
        }
        return isReverse;
    }
}
