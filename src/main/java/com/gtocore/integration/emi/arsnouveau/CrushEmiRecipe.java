package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.network.chat.Component;

import com.hollingsworth.arsnouveau.common.crafting.recipes.CrushRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class CrushEmiRecipe extends BasicEmiRecipe {

    private final Component[] chanceTexts;
    private final Component[] rangeTexts;

    public CrushEmiRecipe(CrushRecipe recipe) {
        super(ArsNouveauEmi.CRUSH, recipe.getId(), 120, 56);
        inputs = Collections.singletonList(EmiIngredient.of(recipe.input));
        int n = recipe.outputs.size();
        outputs = new ObjectArrayList<>(n);
        chanceTexts = new Component[n];
        rangeTexts = new Component[n];
        for (int i = 0; i < n; i++) {
            CrushRecipe.CrushOutput output = recipe.outputs.get(i);
            outputs.add(EmiStack.of(output.stack).setChance(output.chance));
            chanceTexts[i] = Component.literal(Math.round(100 * output.chance - 0.5f) + "%");
            rangeTexts[i] = output.maxRange > 1 ? Component.literal("1-" + output.maxRange) : null;
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 5, 4);
        widgets.addFillingArrow(22, 6, 2000);
        for (int i = 0; i < chanceTexts.length; i++) {
            widgets.addSlot(outputs.get(i), 49, 4 + 16 * i).recipeContext(this);
            widgets.addText(chanceTexts[i], 98, 11 + 17 * i, ArsRingEmiRecipe.TEXT_COLOR, false);
            if (rangeTexts[i] != null) widgets.addText(rangeTexts[i], 75, 11 + 17 * i, ArsRingEmiRecipe.TEXT_COLOR, false);
        }
    }
}
