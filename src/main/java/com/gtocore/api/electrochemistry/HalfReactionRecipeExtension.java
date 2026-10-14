package com.gtocore.api.electrochemistry;

import com.gtocore.api.gui.recipe.HalfReactionUI;
import com.gtocore.api.gui.widget.IonSlot;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import net.minecraft.network.chat.Component;

import com.gto.recipesearch.IntLongMap;

public final class HalfReactionRecipeExtension extends RecipeExtension<HalfReaction> {

    public static final HalfReactionRecipeExtension INSTANCE = new HalfReactionRecipeExtension();

    private HalfReactionRecipeExtension() {
        super("half_reaction", HalfReaction.CODEC, false);
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public boolean handleInput(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, boolean simulate) {
        // Registered half reactions are search candidates; only a paired reaction may run.
        return false;
    }

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return parallel;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}

    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        var reaction = recipe.data.getData(INSTANCE);
        if (reaction == null) return;
        if (!(recipe.recipeType.getRecipeUI() instanceof HalfReactionUI)) {
            for (var ion : reaction.requiredIons()) {
                info.panelSlot(() -> new IonSlot(ion, IO.IN));
            }
            for (var ion : reaction.ions()) {
                info.panelSlot(() -> new IonSlot(ion, IO.OUT));
            }
        }
        info.sentence(Component.translatable("gtocore.recipe.half_reaction.charge", reaction.charge()));
        info.sentence(Component.translatable("gtocore.recipe.half_reaction.electrolyte_type", Component.translatable(reaction.electrolyteType().translationKey())));
    }
}
