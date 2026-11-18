package com.gtocore.integration.emi.apotheosis;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class FletchingEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEX = new ResourceLocation(ApotheosisEmi.MODID, "textures/gui/fletching_table.png");

    FletchingEmiRecipe(EmiRecipeCategory category, FletchingRecipe recipe) {
        super(category, recipe.getId(), 139, 54);
        List<Ingredient> ings = recipe.getIngredients();
        List<EmiIngredient> in = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            in.add(i < ings.size() ? EmiIngredient.of(ings.get(i)) : EmiStack.EMPTY);
        }
        this.inputs = in;
        this.outputs = Collections.singletonList(EmiStack.of(recipe.getOutput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEX, 0, 0, 139, 54, 6, 16);
        for (int i = 0; i < 3; i++) {
            widgets.addSlot(inputs.get(i), 41, 18 * i).drawBack(false);
        }
        widgets.addSlot(outputs.get(0), 116, 18).drawBack(false).recipeContext(this);
    }
}
