package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MEPatternBufferPartMachine extends MEPatternBufferPartMachineV2 {

    public MEPatternBufferPartMachine(MetaMachineBlockEntity holder, PatternBufferType type) {
        super(holder, type);
    }

    @Override
    protected boolean isCapacityLimited() {
        return false;
    }
}
