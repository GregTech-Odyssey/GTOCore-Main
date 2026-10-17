package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CoilCrossRecipeMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.gto.datasynclib.annotations.SyncToClient;

@DataGeneratorScanned
public final class MoltenCoreMachine extends CoilCrossRecipeMultiblockMachine {

    @RegisterLanguage(cn = "蒸馏室", en = "Distillery Chamber")
    private static final String DISTILLERY_NAME = "gtocore.multiblock.molten_core.distillery";
    @RegisterLanguage(cn = "搭建后可运行蒸馏室配方", en = "When built, distillery recipes can be run")
    private static final String DISTILLERY_DESC = "gtocore.multiblock.molten_core.distillery.desc";
    public static final ParamKey DISTILLERY = ParamKey.of(DISTILLERY_NAME, DISTILLERY_DESC);

    @SyncToClient(listener = "onDistilleryChanged")
    private boolean distillery;

    public MoltenCoreMachine(MetaMachineBlockEntity holder) {
        super(holder, false, false, false, false, m -> m.isFormed() ? 1L << Math.min(60, (int) (m.getTemperature() / 900.0D)) : 0);
    }

    @Override
    public void onStructureFormed() {
        distillery = hasStructurePart(DISTILLERY);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        distillery = false;
        super.onStructureInvalid();
    }

    private void onDistilleryChanged(boolean newValue, boolean oldValue) {
        if (newValue != oldValue) setAvailableRecipeTypesCache(null);
    }

    @Override
    public boolean recipeTypeAvailable(GTRecipeType type) {
        return distillery || type == GTORecipeTypes.FLUID_HEATER_RECIPES;
    }
}
