package com.gtocore.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.List;

public interface LazyRecipeTab {

    List<EmiRecipe> gtocore$getRecipes();

    static boolean contains(List<EmiRecipe> recipes, EmiRecipe recipe) {
        for (var r : recipes) {
            if (r == recipe) return true;
        }
        return false;
    }
}
