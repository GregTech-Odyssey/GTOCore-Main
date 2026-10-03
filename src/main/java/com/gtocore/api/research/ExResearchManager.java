package com.gtocore.api.research;

import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public final class ExResearchManager {

    public static boolean hasRenderableMainOutput(GTRecipeDefinition recipe) {
        return getMainItemOutput(recipe) != null;
    }

    public static @Nullable AEKey getMainItemOutput(GTRecipeDefinition recipe) {
        if (!recipe.itemOutputs.isEmpty()) {
            return recipe.itemOutputs.ingredient(0).displayKey() instanceof AEItemKey key ? key : null;
        }
        if (!recipe.fluidOutputs.isEmpty()) {
            return recipe.fluidOutputs.ingredient(0).displayKey() instanceof AEFluidKey key ? key : null;
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

    public static void triggerPlanetaryResearch(UUID team, ResourceKey<Level> planet) {
        var dimTier = GTODimensions.getTier(planet);
        var context = TeamResearchSavedData.getOrCreateContext(team);
        if (context.addUnlockedDimension(planet)) {
            TeamResearchSavedData.getOrCreateContext(team).addResearchPoints(ResearchTag.INTERSTELLAR_ENGINEERING, 1L << dimTier);
        }
    }
}
