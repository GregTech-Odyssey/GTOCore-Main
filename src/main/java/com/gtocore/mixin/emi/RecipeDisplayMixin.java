package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.EmiPageLayout;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeDisplay;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EMI 配方界面翻页排版（只有 {@code RecipeTab} 为每个配方建 {@link RecipeDisplay}）时，GT 配方页用"整页"外框：
 * <ul>
 * <li>构造时报的尺寸：宽度让出右下角缺口里的按钮列；</li>
 * <li>{@link RecipeDisplay#getWidgets} 期间建的页面画卡片、挖缺口。</li>
 * </ul>
 * 其余地方（悬停预览、截图、生产规划图）仍取 {@code getDisplayWidth/Height} 的紧凑尺寸、用紧凑外框。
 */
@Mixin(value = RecipeDisplay.class, remap = false)
public class RecipeDisplayMixin {

    @Redirect(method = "<init>(Ldev/emi/emi/api/recipe/EmiRecipe;)V",
              at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/recipe/EmiRecipe;getDisplayWidth()I"))
    private int gtocore$pagedWidth(EmiRecipe recipe) {
        return recipe instanceof EmiPageLayout.Paged paged ? paged.getPagedWidth() : recipe.getDisplayWidth();
    }

    @Redirect(method = "<init>(Ldev/emi/emi/api/recipe/EmiRecipe;)V",
              at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/recipe/EmiRecipe;getDisplayHeight()I"))
    private int gtocore$pagedHeight(EmiRecipe recipe) {
        return recipe instanceof EmiPageLayout.Paged paged ? paged.getPagedHeight() : recipe.getDisplayHeight();
    }

    @Inject(method = "getWidgets", at = @At("HEAD"))
    private void gtocore$beginPagedBuild(int x, int y, int availableWidth, int availableHeight, CallbackInfoReturnable<WidgetGroup> cir) {
        EmiPageLayout.setPagedBuild(true);
    }

    @Inject(method = "getWidgets", at = @At("RETURN"))
    private void gtocore$endPagedBuild(int x, int y, int availableWidth, int availableHeight, CallbackInfoReturnable<WidgetGroup> cir) {
        EmiPageLayout.setPagedBuild(false);
    }
}
