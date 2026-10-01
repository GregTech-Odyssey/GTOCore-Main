package com.gtocore.integration.emi;

import com.gtocore.api.research.recipe.ResearchPointsRecipeExtion;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class GTEMIRecipe extends GTEmiRecipe {

    public GTEMIRecipe(GTRecipeDefinition recipe, EmiRecipeCategory category) {
        super(recipe, category);
    }

    @Override
    protected void collectIngredients(List<EmiIngredient> inputs, List<EmiStack> outputs, List<EmiIngredient> catalysts) {
        super.collectIngredients(inputs, outputs, catalysts);
        if (!recipe.data.containsKey(ResearchPointsRecipeExtion.INSTANCE)) return;
        var points = recipe.data.getData(ResearchPointsRecipeExtion.INSTANCE);
        if (points == null) return;
        for (var it = points.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            outputs.add(new ResearchTagEmiStack(entry.getKey()).setAmount(entry.getLongValue()));
        }
    }
}
