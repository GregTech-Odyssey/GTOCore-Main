package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.HalfReactionRecipeExtension;
import com.gtocore.api.electrochemistry.PairedReactionRecipeExtension;
import com.gtocore.api.machine.RecipeTypeOverrideUnit;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.machine.feature.multiblock.ICrossRecipeElectricMachine;
import com.gtolib.api.machine.trait.CoilTrait;
import com.gtolib.api.machine.trait.CrossRecipeTrait;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.datasynclib.annotations.SaveToDisk;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

/** Pairs plating reactions within each input unit with coil parallelism and recipe threads. */
public final class ElectrochemicalWorkstation extends ElectrochemicalMachine implements ICrossRecipeElectricMachine, ICoilMachine {

    @SaveToDisk
    private final CrossRecipeTrait crossRecipeTrait;
    private final CoilTrait coilTrait;

    public ElectrochemicalWorkstation(MetaMachineBlockEntity holder) {
        super(holder);
        crossRecipeTrait = new CrossRecipeTrait(this, false, false, machine -> isFormed() ? 1L << Math.min(60, getTemperature() / 900) : 0);
        coilTrait = new CoilTrait(this, false, false);
    }

    @Override
    public CrossRecipeTrait getCrossRecipeTrait() {
        return crossRecipeTrait;
    }

    @Override
    protected boolean sharedReactionInputs() {
        return true;
    }

    @Override
    protected GTRecipeDefinition combine(GTRecipeType type, GTRecipeDefinition first, GTRecipeDefinition second, HalfReaction firstHalf, HalfReaction secondHalf) {
        if (crossRecipeTrait.duplicateCheck) {
            // Combined definitions are transient; identify duplicates by their source pair.
            for (var recipe : crossRecipeTrait.lastRecipes) {
                var inputs = recipe.data.getData(PairedReactionRecipeExtension.INSTANCE);
                if (inputs == null) continue;
                var previousFirst = inputs.left().definition;
                var previousSecond = inputs.right().definition;
                if ((previousFirst == first && previousSecond == second) || (previousFirst == second && previousSecond == first)) return null;
            }
        }
        return super.combine(type, first, second, firstHalf, secondHalf);
    }

    @Override
    public int getTemperature() {
        return coilTrait.getTemperature();
    }

    @Override
    public ICoilType getCoilType() {
        return coilTrait.getCoilType();
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        crossRecipeTrait.attachConfigurators(configuratorPanel);
    }

    @Override
    public GTRecipe getRealRecipe(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        return ICrossRecipeElectricMachine.super.getRealRecipe(unit, recipe);
    }

    @Override
    public boolean findRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle, GTRecipeDefinition lockedRecipe) {
        boolean plating = false;
        BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> wholeRecipe = (u, r) -> r.data.getData(HalfReactionRecipeExtension.INSTANCE) == null && canHandle.test(u, r);
        for (var unit : ordinaryInputUnits) {
            var unitType = RecipeTypeOverrideUnit.of(unit, type);
            if (unitType == null) continue;
            if (unitType == GTORecipeTypes.ELECTROPLATING_RECIPES) {
                plating = true;
                continue;
            }
            if (unit.findRecipe(unitType, wholeRecipe)) return true;
        }
        return plating && super.findRecipe(GTORecipeTypes.ELECTROPLATING_RECIPES, canHandle, lockedRecipe);
    }
}
