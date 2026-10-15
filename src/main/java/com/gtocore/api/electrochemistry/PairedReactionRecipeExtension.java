package com.gtocore.api.electrochemistry;

import com.gtocore.common.machine.multiblock.electric.ElectrochemicalMachine;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.recipesearch.IntLongMap;

public final class PairedReactionRecipeExtension extends RecipeExtension<PairedReactionRecipeExtension.Inputs> {

    private static final DataSyncCodec<GTRecipe> RECIPE_CODEC = DataSyncCodec.of(GTRecipe.STREAM_CODEC, GTRecipe.DATA_CODEC);
    public static final PairedReactionRecipeExtension INSTANCE = new PairedReactionRecipeExtension();

    public record Inputs(GTRecipe left, GTRecipe right) {

        private static final DataSyncCodec<Inputs> CODEC = CombinedCodec.composite(
                RECIPE_CODEC, Inputs::left, RECIPE_CODEC, Inputs::right, Inputs::new);
    }

    private PairedReactionRecipeExtension() {
        super("paired_half_reactions", Inputs.CODEC, false);
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        var machine = (ElectrochemicalMachine) holder;
        return machine.getPairedInputParallel(recipe, parallel);
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {
        var inputs = recipe.data.getData(INSTANCE);
        var left = inputs.left().copy();
        var right = inputs.right().copy();
        left.modifier(parallel, false);
        right.modifier(parallel, false);
        recipe.mutableData().put(INSTANCE, new Inputs(left, right));
    }
}
