package com.gtocore.integration.emi;

import com.gregtechceu.gtceu.integration.emi.orevein.VeinEmiRecipe;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.widget.WidgetHolder;

public final class PagedVeinEmiRecipe extends VeinEmiRecipe implements EmiPageLayout.Paged {

    private final Size[] pagedSizes = new Size[6];
    private int pagedButtons = -1;

    public PagedVeinEmiRecipe(VeinEmiRecipe recipe) {
        super(recipe);
    }

    private GTRecipeWidget.PageFrame pagedFrame(int fillHeight, int buttons) {
        return new GTRecipeWidget.PageFrame(EmiPageLayout.minPageWidth(), fillHeight, buttons, true);
    }

    private Size pagedSize(int buttons) {
        if (buttons >= pagedSizes.length) return measure(pagedFrame(0, buttons));
        var size = pagedSizes[buttons];
        if (size == null) pagedSizes[buttons] = size = measure(pagedFrame(0, buttons));
        return size;
    }

    private int pagedDisplayWidth(int buttons) {
        return EmiPageLayout.displayWidth(pagedSize(buttons).width, buttons);
    }

    @Override
    public int getPagedWidth() {
        int buttons = EmiPageLayout.sideButtons(this);
        pagedButtons = buttons;
        return pagedDisplayWidth(buttons);
    }

    @Override
    public int getPagedHeight() {
        return pagedSize(Math.max(pagedButtons, 0)).height;
    }

    @Override
    protected GTRecipeWidget.PageFrame frameFor(WidgetHolder widgets) {
        if (pagedButtons >= 0 && EmiPageLayout.claimPagedGroup(widgets, pagedDisplayWidth(pagedButtons))) {
            return pagedFrame(widgets.getHeight(), pagedButtons);
        }
        return GTRecipeWidget.PageFrame.COMPACT;
    }
}
