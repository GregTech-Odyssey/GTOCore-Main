package com.gtocore.api.research.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;

import com.gtolib.api.machine.feature.multiblock.ICrossRecipeMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.recipesearch.IntLongMap;
import org.jetbrains.annotations.NotNull;

public final class CatalysisResearchRecipeExtension extends RecipeExtension<Void> {

    public static final CatalysisResearchRecipeExtension INSTANCE = new CatalysisResearchRecipeExtension();

    private CatalysisResearchRecipeExtension() {
        super("catalysis_research", null, false);
    }

    @Override
    public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
        if (simulate || !(holder instanceof IMultiController controller)) return true;
        if (holder instanceof ICrossRecipeMachine machine && !machine.getCrossRecipeTrait().isSeparateThread) {
            for (var definition : machine.getLastRecipes()) {
                for (var extension : definition.recipeExtensions) {
                    if (extension == INSTANCE) {
                        addResearchData(controller, recipe, definition);
                        break;
                    }
                }
            }
        } else {
            addResearchData(controller, recipe, recipe.definition);
        }
        return true;
    }

    private static void addResearchData(IMultiController controller, GTRecipe recipe, GTRecipeDefinition definition) {
        var effMod = Math.max(1e-5, recipe.duration * recipe.eut / (definition.duration * definition.eut + 1e-5));
        IResearchPointsOperation.findHatchAndAddResearchData(controller, ResearchTag.CATALYSIS, definition.tier / effMod * recipe.parallels);
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
