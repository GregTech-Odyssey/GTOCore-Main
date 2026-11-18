package com.gtocore.integration.emi.arsnouveau;

import com.hollingsworth.arsnouveau.api.recipe.BuddingConversionRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.Collections;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class BuddingConversionEmiRecipe extends BasicEmiRecipe {

    public BuddingConversionEmiRecipe(BuddingConversionRecipe recipe) {
        super(ArsNouveauEmi.BUDDING_CONVERSION, recipe.getId(), 120, 24);
        inputs = Collections.singletonList(EmiStack.of(recipe.input()));
        outputs = Collections.singletonList(EmiStack.of(recipe.result()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 5, 3);
        widgets.addFillingArrow(48, 5, 2000);
        widgets.addSlot(outputs.get(0), 97, 3).recipeContext(this);
    }
}
