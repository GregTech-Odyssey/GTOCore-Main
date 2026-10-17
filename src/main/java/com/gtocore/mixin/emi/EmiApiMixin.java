package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.LazyRecipeTab;
import com.gtocore.integration.emi.multipage.MultiblockInfoEmiRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = EmiApi.class, remap = false)
public class EmiApiMixin {

    @Unique
    @Nullable
    private static Item gtocore$structureItem(EmiRecipe recipe) {
        if (recipe instanceof MultiblockInfoEmiRecipe info) return info.definition.asItem();
        if (recipe instanceof com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiRecipe info) return info.definition.asItem();
        return null;
    }

    @ModifyVariable(method = "setPages", at = @At("HEAD"), argsOnly = true)
    private static Map<EmiRecipeCategory, List<EmiRecipe>> gtocore$ownStructureFirst(Map<EmiRecipeCategory, List<EmiRecipe>> recipes, Map<EmiRecipeCategory, List<EmiRecipe>> ignored,
                                                                                     EmiIngredient stack) {
        if (stack.isEmpty()) return recipes;
        var item = stack.getEmiStacks().getFirst().getItemStack().getItem();
        Map<EmiRecipeCategory, List<EmiRecipe>> result = null;
        for (var entry : recipes.entrySet()) {
            var list = entry.getValue();
            int own = -1;
            for (int i = 0; i < list.size(); i++) {
                if (gtocore$structureItem(list.get(i)) == item) {
                    own = i;
                    break;
                }
            }
            if (own <= 0) continue;
            if (result == null) result = new LinkedHashMap<>(recipes);
            var sorted = new ArrayList<EmiRecipe>(list.size());
            sorted.add(list.get(own));
            for (int i = 0; i < list.size(); i++) {
                if (i != own) sorted.add(list.get(i));
            }
            result.put(entry.getKey(), sorted);
        }
        return result == null ? recipes : result;
    }

    @Inject(method = "displayUses", at = @At("TAIL"))
    private static void gtocore$focusWorkstationCategory(EmiIngredient stack, CallbackInfo ci) {
        if (stack.isEmpty() || !(Minecraft.getInstance().screen instanceof RecipeScreen screen)) return;
        EmiStack zero = stack.getEmiStacks().getFirst();
        var manager = EmiApi.getRecipeManager();
        for (RecipeTab tab : ((RecipeScreenAccessor) screen).gtocore$getTabs()) {
            for (EmiIngredient workstation : manager.getWorkstations(tab.category)) {
                for (EmiStack candidate : workstation.getEmiStacks()) {
                    if (candidate.isEqual(zero)) {
                        screen.focusCategory(tab.category);
                        return;
                    }
                }
            }
        }
        var item = zero.getItemStack().getItem();
        for (RecipeTab tab : ((RecipeScreenAccessor) screen).gtocore$getTabs()) {
            var recipes = ((LazyRecipeTab) tab).gtocore$getRecipes();
            if (!recipes.isEmpty() && gtocore$structureItem(recipes.getFirst()) == item) {
                screen.focusRecipe(recipes.getFirst());
                return;
            }
        }
    }
}
