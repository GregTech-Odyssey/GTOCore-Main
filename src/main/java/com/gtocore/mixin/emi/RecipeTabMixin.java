package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.LazyRecipeTab;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeDisplay;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = RecipeTab.class, remap = false)
public abstract class RecipeTabMixin implements LazyRecipeTab {

    @Shadow
    @Final
    @Mutable
    private List<RecipeDisplay> displays;

    @Shadow
    @Final
    @Mutable
    private int width;

    @Unique
    private List<EmiRecipe> gtocore$recipes;

    @Unique
    private boolean gtocore$lazy;

    @Unique
    private boolean gtocore$bakePending;

    @Unique
    private int gtocore$bakeHeight;

    @Shadow
    public abstract void bakePages(int height);

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;", ordinal = 0))
    private Stream<EmiRecipe> gtocore$deferDisplays(List<EmiRecipe> recipes) {
        gtocore$recipes = recipes;
        gtocore$lazy = true;
        return Stream.empty();
    }

    @Override
    public List<EmiRecipe> gtocore$getRecipes() {
        return gtocore$recipes;
    }

    @Unique
    private void gtocore$realize() {
        if (!gtocore$lazy) return;
        gtocore$lazy = false;
        var list = new ArrayList<RecipeDisplay>(gtocore$recipes.size());
        int maxWidth = 0;
        for (var recipe : gtocore$recipes) {
            RecipeDisplay display;
            try {
                display = new RecipeDisplay(recipe);
            } catch (Throwable t) {
                display = new RecipeDisplay(t);
            }
            list.add(display);
            maxWidth = Math.max(maxWidth, display.getWidth());
        }
        displays = list;
        width = maxWidth;
        if (gtocore$bakePending) {
            gtocore$bakePending = false;
            bakePages(gtocore$bakeHeight);
        }
    }

    @Inject(method = "bakePages", at = @At("HEAD"), cancellable = true)
    private void gtocore$deferBake(int height, CallbackInfo ci) {
        if (!gtocore$lazy) return;
        gtocore$bakeHeight = height;
        gtocore$bakePending = true;
        ci.cancel();
    }

    @Inject(method = "constructWidgets", at = @At("HEAD"))
    private void gtocore$realizeForWidgets(int page, int x, int y, int backgroundWidth, int backgroundHeight, CallbackInfoReturnable<List<WidgetGroup>> cir) {
        gtocore$realize();
    }

    @Inject(method = "getWidth", at = @At("HEAD"))
    private void gtocore$realizeForWidth(CallbackInfoReturnable<Integer> cir) {
        gtocore$realize();
    }

    @Inject(method = "getPageCount", at = @At("HEAD"))
    private void gtocore$realizeForPageCount(CallbackInfoReturnable<Integer> cir) {
        gtocore$realize();
    }

    @Inject(method = "getPage", at = @At("HEAD"))
    private void gtocore$realizeForPage(int page, CallbackInfoReturnable<List<RecipeDisplay>> cir) {
        gtocore$realize();
    }
}
