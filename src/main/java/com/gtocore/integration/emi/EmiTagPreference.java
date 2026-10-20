package com.gtocore.integration.emi;

import com.gtocore.data.tag.Tags;

import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.bom.BoM;
import org.jetbrains.annotations.Nullable;

public final class EmiTagPreference {

    private EmiTagPreference() {}

    @Nullable
    public static EmiStack get(EmiIngredient ingredient) {
        if (ingredient instanceof TagEmiIngredient tag && !Tags.CIRCUITS_ARRAY.containsKey(tag.key) && BoM.getRecipe(ingredient) instanceof EmiResolutionRecipe resolution && ingredient.getEmiStacks().contains(resolution.stack)) {
            return resolution.stack;
        }
        return null;
    }

    public static Ingredient narrow(Ingredient ingredient) {
        if (ingredient.isEmpty()) return ingredient;
        var preferred = get(EmiIngredient.of(ingredient));
        if (preferred == null || preferred.getItemStack().isEmpty()) return ingredient;
        return Ingredient.of(preferred.getItemStack());
    }
}
