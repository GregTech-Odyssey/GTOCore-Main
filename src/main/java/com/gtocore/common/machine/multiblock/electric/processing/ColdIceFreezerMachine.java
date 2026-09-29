package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.machine.multiblock.CustomParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraftforge.fluids.FluidStack;

import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ColdIceFreezerMachine extends CustomParallelMultiblockMachine {

    private static final FluidStack ICE = GTMaterials.Ice.getFluid(1);

    @SyncToClient(listener = "onAtomizationModuleChanged")
    private boolean atomizationModule;

    public ColdIceFreezerMachine(MetaMachineBlockEntity holder) {
        super(holder, m -> 64);
    }

    @Override
    public void onStructureFormed() {
        atomizationModule = hasStructurePart(StructureModuleKeys.COLD_ICE_FREEZER_ATOMIZATION);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        atomizationModule = false;
        super.onStructureInvalid();
    }

    private void onAtomizationModuleChanged(boolean newValue, boolean oldValue) {
        if (newValue != oldValue) setAvailableRecipeTypesCache(null);
    }

    private boolean inputFluid(@Nullable RecipeHandlerUnit unit) {
        if (inputFluid(unit, ICE.getRawFluid(), (1L << Math.max(0, getTier() - 2)) * 10L)) {
            return true;
        }
        setIdleReason(() -> ActionResult.failInsufficientIn(ICE.getDisplayName()).reason());
        return false;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (getOffsetTimer() % 20 == 0 && !inputFluid(getRecipeLogic().getLastOriginUnit())) return false;
        return super.handleTickRecipe(recipe);
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        return inputFluid(unit) && super.handleRecipeInput(unit, recipe);
    }

    @Override
    public boolean recipeTypeAvailable(GTRecipeType type) {
        if (type == GTORecipeTypes.ATOMIZATION_CONDENSATION_RECIPES) {
            return atomizationModule;
        }
        return true;
    }
}
