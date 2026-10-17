package com.gtocore.api.machine;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import org.jetbrains.annotations.Nullable;

public interface RecipeTypeOverrideUnit {

    @Nullable
    GTRecipeType effectiveRecipeType(GTRecipeType machineType);

    @Nullable
    static GTRecipeType of(RecipeHandlerUnit unit, GTRecipeType machineType) {
        return unit instanceof RecipeTypeOverrideUnit override ? override.effectiveRecipeType(machineType) : machineType;
    }
}
