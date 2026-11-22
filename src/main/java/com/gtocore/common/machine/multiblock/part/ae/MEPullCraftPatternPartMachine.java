package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MEPullCraftPatternPartMachine extends MEPullCraftPatternPartMachineV2 {

    public MEPullCraftPatternPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, maxPatternCount);
    }

    @Override
    protected boolean isCapacityLimited() {
        return false;
    }
}
