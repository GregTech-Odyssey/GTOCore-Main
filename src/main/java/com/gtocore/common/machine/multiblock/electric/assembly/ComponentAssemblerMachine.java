package com.gtocore.common.machine.multiblock.electric.assembly;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import org.jetbrains.annotations.NotNull;

public class ComponentAssemblerMachine extends TierCasingMultiblockMachine {

    private int casingTier;

    public ComponentAssemblerMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORecipeDataKeys.COMPONENT_ASSEMBLY_CASING_TIER);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        int cap = hasStructurePart(StructureModuleKeys.COMPONENT_ASSEMBLER_ASSEMBLY) ? GTValues.UV : GTValues.IV;
        casingTier = Math.min(cap, getCasingTier(GTORecipeDataKeys.COMPONENT_ASSEMBLY_CASING_TIER));
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        casingTier = 0;
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        if (recipe.data.getInt(GTORecipeDataKeys.COMPONENT_ASSEMBLY_CASING_TIER) > casingTier) {
            setIdleReason(IdleReason.BLOCK_TIER_NOT_SATISFIES, recipe.data.getInt(GTORecipeDataKeys.COMPONENT_ASSEMBLY_CASING_TIER), casingTier);
            return null;
        }
        return super.getRealRecipe(unit, recipe);
    }
}
