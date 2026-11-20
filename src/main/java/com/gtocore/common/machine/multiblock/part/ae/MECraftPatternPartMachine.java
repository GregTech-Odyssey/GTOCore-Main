package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MECraftPatternPartMachine extends MECraftPatternPartMachineV2 {

    public MECraftPatternPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    public MECraftPatternPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, maxPatternCount);
    }

    @Override
    protected boolean isCapacityLimited() {
        return false;
    }
}
