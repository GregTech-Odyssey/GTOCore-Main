package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.network.chat.Component;

import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class ImbuementEmiRecipe extends ArsRingEmiRecipe {

    private final Component text;

    public ImbuementEmiRecipe(ImbuementRecipe recipe) {
        super(ArsNouveauEmi.IMBUEMENT, recipe.getId(), EmiIngredient.of(recipe.input), true, recipe.pedestalItems, EmiStack.of(recipe.output));
        text = Component.translatable("ars_nouveau.source", recipe.source);
    }

    @Override
    protected void addText(WidgetHolder widgets) {
        addLine(widgets, text, 100);
    }
}
