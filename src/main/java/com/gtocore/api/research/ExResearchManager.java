package com.gtocore.api.research;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ExResearchManager {

    public static boolean hasRenderableMainOutput(GTRecipeDefinition recipe) {
        return getMainItemOutput(recipe) != null;
    }

    public static @Nullable AEKey getMainItemOutput(GTRecipeDefinition recipe) {
        if (!recipe.itemOutputs.isEmpty()) {
            var ingredient = recipe.itemOutputs.getFirst().inner;
            ItemStack stack = ingredient.getInnerItemStack().copy();
            return AEItemKey.of(stack);
        }
        if (!recipe.fluidOutputs.isEmpty()) {
            var ingredient = recipe.fluidOutputs.getFirst().inner;
            FluidStack stack = ingredient.getFluidStack();
            return AEFluidKey.of(stack);
        }
        return null;
    }

    public static Component getMainOutputDisplayName(GTRecipeDefinition recipe) {
        @Nullable
        AEKey key = getMainItemOutput(recipe);
        if (key != null) {
            return key.getDisplayName();
        }
        return Component.empty();
    }

    @Nullable
    public static GTRecipeDefinition getRecipeInDataItem(ItemStack stack) {
        ResearchManager.ResearchItem researchData = ResearchManager.readResearchId(stack);
        if (researchData == null) return null;

        Collection<GTRecipeDefinition> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
        if (recipes == null || recipes.isEmpty()) return null;
        return recipes.iterator().next();
    }
}
