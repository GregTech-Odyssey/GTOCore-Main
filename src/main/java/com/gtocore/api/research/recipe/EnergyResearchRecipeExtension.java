package com.gtocore.api.research.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.config.GTORules;

import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.recipesearch.IntLongMap;
import org.jetbrains.annotations.NotNull;

public final class EnergyResearchRecipeExtension extends RecipeExtension<Void> {

    public static final EnergyResearchRecipeExtension INSTANCE = new EnergyResearchRecipeExtension();

    private EnergyResearchRecipeExtension() {
        super("energy_research", null, false);
    }

    @Override
    public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
        if (simulate || recipe.eut >= -4194304L || !(holder instanceof IMultiController controller) || !(holder instanceof IRecipeLogicMachine logicMachine)) return true;
        if (logicMachine.getRecipeLogic().getTotalContinuousRunningTime() <= GTORules.ENERGY_DATA_WARMUP.get()) return true;
        var p = Math.floor(Math.log(-recipe.eut) / Math.log(4)) - 10;
        if (holder instanceof ICrossRecipeMachine machine && !machine.getCrossRecipeTrait().isSeparateThread) {
            for (int i = 0; i < machine.getLastRecipes().size(); i++) {
                IResearchPointsOperation.findHatchAndAddResearchData(controller, ResearchTag.ENERGY, p * p);
            }
        } else {
            IResearchPointsOperation.findHatchAndAddResearchData(controller, ResearchTag.ENERGY, p * p);
        }
        return true;
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return parallel;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}
}
