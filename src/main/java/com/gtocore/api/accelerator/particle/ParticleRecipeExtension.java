package com.gtocore.api.accelerator.particle;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.recipesearch.IntLongMap;

import java.util.List;

public final class ParticleRecipeExtension extends RecipeExtension<List<ParticleBeam>> {

    public static final ParticleRecipeExtension INSTANCE = new ParticleRecipeExtension();

    private ParticleRecipeExtension() {
        super("particle", CombinedCodec.list(ParticleBeam.CODEC), false);
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {
        var list = recipe.data.getData(INSTANCE);
        if (list == null) return;
        list.forEach(p -> map.add(p.getDefinition().ingredientId(), p.getAmount()));
    }

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return 0;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}

    /** 不在配方页显示。 */
    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {}
}
