package com.gtocore.integration.emi;

import com.gregtechceu.gtceu.integration.emi.orevein.VeinEmiRecipe;
import com.gregtechceu.gtceu.integration.emi.recipe.EmiPageLayout;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

import dev.emi.emi.api.widget.WidgetHolder;

public final class PagedVeinEmiRecipe extends VeinEmiRecipe implements EmiPageLayout.Paged {

    public PagedVeinEmiRecipe(VeinEmiRecipe recipe) {
        super(recipe);
    }

    @Override
    public int getPagedWidth() {
        return getSizes().getPagedWidth(this);
    }

    @Override
    public int getPagedHeight() {
        return getSizes().getPagedHeight();
    }

    @Override
    protected GTRecipeWidget.PageFrame frameFor(WidgetHolder widgets) {
        return getSizes().frameFor(widgets);
    }
}
