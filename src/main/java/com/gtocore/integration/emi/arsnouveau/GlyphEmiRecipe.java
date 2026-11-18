package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.network.chat.Component;

import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class GlyphEmiRecipe extends ArsRingEmiRecipe {

    private final Component text;

    public GlyphEmiRecipe(GlyphRecipe recipe) {
        super(ArsNouveauEmi.GLYPH, recipe.getId(), null, false, recipe.inputs, EmiStack.of(recipe.output));
        text = Component.translatable("ars_nouveau.exp", ScribesTile.getLevelsFromExp(recipe.exp));
    }

    @Override
    protected void addText(WidgetHolder widgets) {
        addLine(widgets, text, 100);
    }
}
