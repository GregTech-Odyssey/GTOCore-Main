package com.gtocore.integration.emi;

import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import dev.emi.emi.widget.RecipeBackground;

public final class EmiPageLayout {

    private static final int BUTTON_OFFSET = 5;
    public static final int BUTTON_COLUMN = BUTTON_OFFSET + GTRecipeWidget.SIDE_BUTTON_INSET;
    public static final int SCREEN_SIDES = 16;
    private static final int RECIPE_AREA_CHROME = 46;
    private static final int SCREEN_CHROME = 52;
    private static final int BOTTOM_WORKSTATIONS = 23;
    private static final boolean HAS_OVERLAY_BUTTON = classExists("dev.emi.emi.widget.RecipeOverlayButtonWidget");
    private static final boolean HAS_PLANNER_BUTTON = classExists("dev.emi.emi.widget.RecipePlannerButtonWidget");

    private static boolean pagedBuild;

    private EmiPageLayout() {}

    public interface Paged {

        int getPagedWidth();

        int getPagedHeight();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, EmiPageLayout.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static int sideButtons(EmiRecipe recipe) {
        int count = 0;
        if (EmiConfig.recipeFillButton && EmiRecipeFiller.isSupported(recipe)) count++;
        if (recipe.getId() != null && !recipe.getOutputs().isEmpty()) {
            if (HAS_OVERLAY_BUTTON) count++;
            if (HAS_PLANNER_BUTTON && !recipe.getInputs().isEmpty()) count++;
        }
        if (recipe.supportsRecipeTree()) {
            if (EmiConfig.recipeTreeButton) count++;
            if (EmiConfig.recipeDefaultButton) count++;
        }
        return count;
    }

    public static int recipeAreaHeight(EmiRecipeCategory category) {
        if (!(Minecraft.getInstance().screen instanceof RecipeScreen)) return 0;
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int height = Math.min(EmiConfig.maximumRecipeScreenHeight, screenHeight - SCREEN_CHROME - EmiConfig.verticalMargin) - RECIPE_AREA_CHROME;
        if (EmiConfig.workstationLocation == SidebarSide.BOTTOM &&
                (!EmiApi.getRecipeManager().getWorkstations(category).isEmpty() || RecipeScreen.resolve != null)) {
            height -= BOTTOM_WORKSTATIONS;
        }
        return Math.max(0, height);
    }

    public static int minPageWidth() {
        return Math.max(UISizes.CONTENT_WIDTH, EmiConfig.minimumRecipeScreenWidth - SCREEN_SIDES);
    }

    public static int displayWidth(int pageWidth, int buttons) {
        return pageWidth - (buttons > 0 ? BUTTON_COLUMN : 0);
    }

    public static void setPagedBuild(boolean paged) {
        pagedBuild = paged;
    }

    public static boolean isPagedBuild() {
        return pagedBuild;
    }

    public static boolean claimPagedGroup(WidgetHolder widgets, int displayWidth) {
        if (!pagedBuild || !(widgets instanceof WidgetGroup group) || widgets.getWidth() != displayWidth) return false;
        group.widgets.removeIf(RecipeBackground.class::isInstance);
        return true;
    }
}
