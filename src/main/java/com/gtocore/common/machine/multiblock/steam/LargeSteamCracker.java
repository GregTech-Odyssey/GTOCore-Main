package com.gtocore.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LargeSteamCracker extends BaseSteamMultiblockMachine {

    public LargeSteamCracker(MetaMachineBlockEntity holder) {
        super(holder, 1, 32, 1);
    }

    @Override
    boolean oc() {
        return true;
    }

    private float getEfficiencyMultiplier() {
        return maxOCamount * 0.125f + 1.0f;
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe r) {
        var r1 = super.getRealRecipe(unit, r);
        if (r1 != null) {
            r1.bake();
            var outputs = r1.fluidOutputs.range(0, 1);
            r1.fluidOutputs = outputs.withAmount(0, (long) (outputs.amount(0) * getEfficiencyMultiplier()));
            return r1;
        }
        return null;
    }
}
