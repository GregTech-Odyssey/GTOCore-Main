package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public class ApparatusEmiRecipe extends ArsRingEmiRecipe {

    private final Component sourceText;

    public ApparatusEmiRecipe(EnchantingApparatusRecipe recipe) {
        this(ArsNouveauEmi.APPARATUS, recipe.getId(), recipe, EmiIngredient.of(recipe.reagent), EmiStack.of(recipe.result));
    }

    protected ApparatusEmiRecipe(EmiRecipeCategory category, ResourceLocation id, EnchantingApparatusRecipe recipe, EmiIngredient center, EmiStack output) {
        super(category, id, center, true, recipe.pedestalItems, output);
        sourceText = recipe.consumesSource() ? Component.translatable("ars_nouveau.source", recipe.sourceCost) : null;
    }

    @Override
    protected void addText(WidgetHolder widgets) {
        if (sourceText != null) addLine(widgets, sourceText, 100);
    }
}
