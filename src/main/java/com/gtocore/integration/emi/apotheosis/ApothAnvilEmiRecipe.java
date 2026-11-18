package com.gtocore.integration.emi.apotheosis;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.Arrays;
import java.util.Collections;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class ApothAnvilEmiRecipe extends BasicEmiRecipe {

    ApothAnvilEmiRecipe(EmiRecipeCategory category, ResourceLocation id, EmiStack left, EmiStack right, EmiStack out) {
        super(category, id, 125, 18);
        this.inputs = Arrays.asList(left, right);
        this.outputs = Collections.singletonList(out);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.PLUS, 27, 3);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 49, 0);
        widgets.addSlot(outputs.get(0), 107, 0).recipeContext(this);
    }
}
