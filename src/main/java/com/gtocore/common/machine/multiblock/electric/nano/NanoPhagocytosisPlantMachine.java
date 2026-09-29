package com.gtocore.common.machine.multiblock.electric.nano;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gto.datasynclib.annotations.SyncToClient;

public final class NanoPhagocytosisPlantMachine extends CrossRecipeMultiblockMachine {

    @SyncToClient(listener = "onGrindingModuleChanged")
    private boolean grindingModule;

    public NanoPhagocytosisPlantMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public void onStructureFormed() {
        grindingModule = hasStructurePart(StructureModuleKeys.NANO_PHAGOCYTOSIS_GRINDING);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        grindingModule = false;
        super.onStructureInvalid();
    }

    private void onGrindingModuleChanged(boolean newValue, boolean oldValue) {
        if (newValue != oldValue) setAvailableRecipeTypesCache(null);
    }

    @Override
    public boolean recipeTypeAvailable(GTRecipeType type) {
        return grindingModule || type == GTORecipeTypes.MACERATOR_RECIPES;
    }
}
