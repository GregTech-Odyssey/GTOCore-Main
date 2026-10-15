package com.gtocore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

public final class AdvancedSemipermeableMembraneHatch extends SemipermeableMembraneHatch {

    public AdvancedSemipermeableMembraneHatch(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected int membraneSlots() {
        return 9;
    }

    @Override
    public long durabilityCost(long parallels, boolean discharge) {
        long root = (long) Math.sqrt(Math.sqrt(parallels));
        long squared = root * root;
        return squared * squared < parallels ? root + 1 : root;
    }

    @Override
    public long parallelLimit(long durability) {
        if (durability <= 0) return 0;
        long squared = durability * durability;
        return squared > ParallelLogic.MAX_PARALLEL / squared ? ParallelLogic.MAX_PARALLEL : squared * squared;
    }
}
