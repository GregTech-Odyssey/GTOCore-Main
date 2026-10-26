package com.gtocore.common.recipe.condition;

import com.gtolib.adastra.NoSpaceChallenge;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.feature.IGravityPartMachine;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;

import earth.terrarium.adastra.api.planets.PlanetApi;

public final class GravityCondition extends RecipeCondition {

    public final boolean zero;

    public GravityCondition(boolean zero) {
        this.zero = zero;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.condition." + (zero ? "zero_" : "") + "gravity");
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (NoSpaceChallenge.testGravity(holder.self(), zero)) return true;
        if (holder instanceof MultiblockControllerMachine controllerMachine) {
            for (var part : controllerMachine.getParts(IGravityPartMachine.class)) {
                return part.getCurrentGravity() == (zero ? 0 : 100);
            }
        }
        var dim = holder.self().getLevel().dimension();
        if (dim == GTODimensions.SOLAR_SURFACE) {
            return !zero;
        }
        var planet = PlanetApi.API.getPlanet(dim);
        return planet != null && planet.isSpace() && zero;
    }
}
