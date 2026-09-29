package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.LazyRecipeTab;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RecipeScreen.class, remap = false)
public abstract class RecipeScreenMixin {

    @Shadow
    private List<RecipeTab> tabs;

    @Shadow
    private int tabPage;

    @Shadow
    public abstract void setPage(int tp, int t, int p);

    @Inject(method = "focusRecipe", at = @At("HEAD"), cancellable = true)
    private void gtocore$focusRecipeLazily(EmiRecipe recipe, CallbackInfo ci) {
        ci.cancel();
        for (int i = 0; i < tabs.size(); i++) {
            var tab = tabs.get(i);
            if (!LazyRecipeTab.contains(((LazyRecipeTab) tab).gtocore$getRecipes(), recipe)) continue;
            for (int j = 0; j < tab.getPageCount(); j++) {
                for (var display : tab.getPage(j)) {
                    if (display.recipe == recipe) {
                        setPage(tabPage, i, j);
                        return;
                    }
                }
            }
        }
    }
}
