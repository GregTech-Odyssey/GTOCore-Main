package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MEInputBusPartMachine extends MEInputBusPartMachineV2 {

    public MEInputBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected boolean isCapacityLimited() {
        return false;
    }
}
