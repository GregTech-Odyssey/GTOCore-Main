package com.gtocore.common.machine.electric;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public class ScannerMachine extends SimpleTieredMachine {

    public ScannerMachine(MetaMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }
}
