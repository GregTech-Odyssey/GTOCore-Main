package com.gtocore.common.recipe.condition;

import com.gtocore.common.machine.electric.beam.BeamAccessPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import net.minecraft.network.chat.Component;

public final class BeamCondition extends RecipeCondition {

    private final long minIntensity;
    private final int minWavelength;
    private final int maxWavelength;
    private final double polarization;
    private final boolean hasPolarization;

    public BeamCondition(long minIntensity, int minWavelength, int maxWavelength, double polarization) {
        this.minIntensity = minIntensity;
        this.minWavelength = minWavelength;
        this.maxWavelength = maxWavelength;
        this.polarization = polarization;
        this.hasPolarization = true;
    }

    public BeamCondition(long minIntensity, int minWavelength, int maxWavelength) {
        this.minIntensity = minIntensity;
        this.minWavelength = minWavelength;
        this.maxWavelength = maxWavelength;
        this.polarization = 0;
        this.hasPolarization = false;
    }

    @Override
    public Component getTooltips() {
        if (!hasPolarization) {
            return Component.translatable("gtocore.recipe.ray_requirement.2", minWavelength, maxWavelength, minIntensity);
        }
        return Component.translatable("gtocore.recipe.ray_requirement.1", minWavelength, maxWavelength, minIntensity, polarization);
    }

    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        info.sentence(() -> Component.translatable("gtocore.recipe.ray_requirement.wavelength", minWavelength, maxWavelength));
        info.sentence(() -> Component.translatable("gtocore.recipe.ray_requirement.intensity", minIntensity));
        if (hasPolarization) {
            info.sentence(() -> Component.translatable("gtocore.recipe.ray_requirement.polarization", polarization));
        }
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (!(holder instanceof IMultiController controller)) return false;

        int minimumWavelength = Math.min(minWavelength, maxWavelength);
        int maximumWavelength = Math.max(minWavelength, maxWavelength);
        double angle = hasPolarization ? Math.toRadians(polarization) : Double.NaN;
        double power = 0;
        for (var part : controller.getParts(BeamAccessPartMachine.class)) {
            power += part.averageReceivedPower(minimumWavelength, maximumWavelength, angle);
            if (power >= minIntensity) return true;
        }
        return false;
    }
}
