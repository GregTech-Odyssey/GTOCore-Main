package com.gtocore.integration.emi.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.Arrays;
import java.util.List;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class ApothSmithingEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEX = new ResourceLocation(ApotheosisEmi.MODID, "textures/gui/smithing_jei.png");

    private final List<EmiStack> left;
    private final EmiIngredient addition;
    private final List<EmiStack> result;
    private final Component text;
    private final int textX;

    ApothSmithingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, List<EmiStack> left, EmiIngredient addition, List<EmiStack> result, Component text, int textX) {
        super(category, id, 140, 34);
        this.left = left;
        this.addition = addition;
        this.result = result;
        this.text = text;
        this.textX = textX;
        this.inputs = Arrays.asList(EmiIngredient.of(left), addition);
        this.outputs = result;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEX, 16, 0, 108, 18, 0, 0, 108, 18, 108, 18);
        int n = Math.min(left.size(), result.size());
        int unique = id.hashCode();
        widgets.addGeneratedSlot(r -> left.get(r.nextInt(n)), unique, 34, 0).drawBack(false);
        widgets.addSlot(addition, 52, 0).drawBack(false);
        widgets.addGeneratedSlot(r -> result.get(r.nextInt(n)), unique, 106, 0).drawBack(false).recipeContext(this);
        widgets.addText(text, textX, 23, 0, false).horizontalAlign(TextWidget.Alignment.CENTER);
    }
}
