package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.impl.part.VoidEnergyHatch;
import com.gtolib.api.machine.impl.part.WirelessEnergyHatchPartMachine;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public final class MagneticFluidGeneratorMachine extends TierCasingMultiblockMachine {

    @RegisterLanguage(cn = "磁流体加速通道", en = "Magnetohydrodynamic Accelerator Channel")
    public static final String EXTENSION_NAME = "gtocore.multiblock.magnetic_fluid_generator.extension";
    @RegisterLanguage(cn = "搭建后基础并行由 64 提高到 256，激光输出时每级并行倍率由 2 提高到 4，燃料效率翻倍", en = "When built, base parallel rises from 64 to 256, the per-tier parallel multiplier with laser output rises from 2 to 4, and fuel efficiency is doubled")
    public static final String EXTENSION_DESC = "gtocore.multiblock.magnetic_fluid_generator.extension.desc";
    public static final ParamKey EXTENSION = ParamKey.of(EXTENSION_NAME, EXTENSION_DESC);

    private int outputTier = 0;
    private boolean laser;
    private double efficiency = 1;

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
        if (hasStructurePart(EXTENSION)) efficiency *= 2;
        int tier = getCasingTier(GTORecipeDataKeys.GLASS_TIER);
        if (tier < outputTier) outputTier = 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        efficiency = 1;
        outputTier = 0;
        laser = false;
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (outputTier < 1) {
            IdleReason.BLOCK_TIER_NOT_SATISFIES.report(this, IssueStage.MODIFIER, -1, getCasingTier(GTORecipeDataKeys.GLASS_TIER), recipe.definition);
            return null;
        }
        boolean extension = hasStructurePart(EXTENSION);
        long parallel = (extension ? 256 : 64) * (laser ? (long) Math.pow(extension ? 4 : 2, outputTier - 1) : 1);
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, parallel);
        if (recipe == null) return null;
        recipe.durationMultiplier(efficiency);
        return RecipeModifier.generatorOverclocking(this, unit, recipe);
    }
}
