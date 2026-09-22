package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.machine.impl.part.VoidEnergyHatch;
import com.gtolib.api.machine.impl.part.WirelessEnergyHatchPartMachine;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MagneticFluidGeneratorMachine extends TierCasingMultiblockMachine {

    private int outputTier = 0;
    private boolean laser;
    private int base = 2;
    private double efficiency = 1;
    private int baseParallel = 64;

    public MagneticFluidGeneratorMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORecipeDataKeys.GLASS_TIER, GTORecipeDataKeys.HERMETIC_CASING_TIER);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (outputTier > 0) return;
        if (part instanceof LaserHatchPartMachine laserHatchPartMachine) {
            outputTier = laserHatchPartMachine.getTier();
            laser = true;
        } else if (part instanceof EnergyHatchPartMachine || part instanceof WirelessEnergyHatchPartMachine) {
            outputTier = ((ITieredMachine) part).getTier();
        } else if (part instanceof VoidEnergyHatch voidEnergyHatch) {
            outputTier = voidEnergyHatch.getSimTier();
            laser = voidEnergyHatch.isSimLaser();
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        int hermeticCasingTier = getCasingTier(GTORecipeDataKeys.HERMETIC_CASING_TIER);
        efficiency = hermeticCasingTier > GTValues.LuV ? hermeticCasingTier / 4.0 : 1.0;
        int tier = getCasingTier(GTORecipeDataKeys.GLASS_TIER);
        if (tier < outputTier) outputTier = 0;
        if (getSubFormedAmount() > 0) {
            base = 4;
            baseParallel = 256;
            efficiency *= 2;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        efficiency = 1;
        outputTier = 0;
        laser = false;
        base = 2;
        baseParallel = 64;
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (outputTier < 1) return null;
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, baseParallel * (laser ? (long) Math.pow(base, outputTier - 1) : 1));
        if (recipe == null) return null;
        recipe.durationMultiplier(efficiency);
        return RecipeModifier.generatorOverclocking(this, unit, recipe);
    }
}
