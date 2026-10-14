package com.gtocore.common.machine.multiblock.electric;

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

/** Keeps the workstation's coil parallelism and recipe threads for paired plating. */
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
        if (type == GTORecipeTypes.ELECTROPLATING_RECIPES) return super.findRecipe(type, canHandle, lockedRecipe);
        for (var unit : ordinaryInputUnits) {
            if (unit.findRecipe(type, canHandle)) return true;
        }
        return false;
    }
}
