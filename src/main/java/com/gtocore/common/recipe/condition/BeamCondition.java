package com.gtocore.common.recipe.condition;

import com.gtocore.common.machine.electric.beam.BeamAccessPartMachine;
import com.gtocore.data.IdleReason;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class BeamCondition extends RecipeCondition {

    private static final double TWO_PI = Math.PI * 2.0D;

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
    public IssueType getIssueType() {
        return IdleReason.BEAM_INTENSITY.type();
    }

    @Override
    public void reportFailure(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe, int index) {
        holder.reportIssue(IdleReason.BEAM_INTENSITY.type(), IssueStage.CONDITION, IO.NONE, null, index, minIntensity, -1, recipe);
    }

    @Override
    public @Nullable Component describeCurrent(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (!(holder instanceof IMultiController controller)) return null;
        var samples = new HashMap<BeamAccessPartMachine.BeamKey, long[]>();
        for (var part : controller.getParts()) {
            if (part instanceof BeamAccessPartMachine receiver) receiver.collectRecentIntensitySamples(samples);
        }
        int minimumWavelength = Math.min(minWavelength, maxWavelength);
        int maximumWavelength = Math.max(minWavelength, maxWavelength);
        float requiredPolarization = normalizePolarizationDegrees(polarization);
        long best = -1;
        for (var entry : samples.entrySet()) {
            var key = entry.getKey();
            if (key.waveLength() < minimumWavelength || key.waveLength() > maximumWavelength) continue;
            if (hasPolarization && Float.compare(key.polarization(), requiredPolarization) != 0) continue;
            best = Math.max(best, average(entry.getValue()));
        }
        if (best < 0) return Component.translatable("gtocore.issue.current.no_beam");
        return Component.translatable("gtocore.issue.current.beam", best);
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (!(holder instanceof IMultiController controller)) return false;

        var samples = new HashMap<BeamAccessPartMachine.BeamKey, long[]>();
        for (var part : controller.getParts()) {
            if (part instanceof BeamAccessPartMachine receiver) receiver.collectRecentIntensitySamples(samples);
        }

        int minimumWavelength = Math.min(minWavelength, maxWavelength);
        int maximumWavelength = Math.max(minWavelength, maxWavelength);
        float requiredPolarization = normalizePolarizationDegrees(polarization);
        for (var entry : samples.entrySet()) {
            var key = entry.getKey();
            if (key.waveLength() < minimumWavelength || key.waveLength() > maximumWavelength) continue;
            if (hasPolarization && Float.compare(key.polarization(), requiredPolarization) != 0) continue;
            if (average(entry.getValue()) >= minIntensity) return true;
        }
        return false;
    }

    private static long average(long[] samples) {
        long quotient = 0L;
        long remainder = 0L;
        for (long sample : samples) {
            quotient += sample / BeamAccessPartMachine.AVERAGE_WINDOW_TICKS;
            remainder += sample % BeamAccessPartMachine.AVERAGE_WINDOW_TICKS;
        }
        return quotient + remainder / BeamAccessPartMachine.AVERAGE_WINDOW_TICKS;
    }

    private static float normalizePolarizationDegrees(double degrees) {
        if (!Double.isFinite(degrees)) return 0.0F;
        double radians = Math.toRadians(degrees) % TWO_PI;
        if (radians > Math.PI) radians -= TWO_PI;
        if (radians < -Math.PI) radians += TWO_PI;
        return radians == 0.0D ? 0.0F : (float) radians;
    }
}
