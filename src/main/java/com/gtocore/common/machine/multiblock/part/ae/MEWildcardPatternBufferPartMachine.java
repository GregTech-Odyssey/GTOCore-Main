package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MEWildcardPatternBufferPartMachine extends MEWildcardPatternBufferPartMachineV2 {

    public MEWildcardPatternBufferPartMachine(MetaMachineBlockEntity holder, PatternBufferType type) {
        super(holder, type);
    }

    @Override
    protected boolean isCapacityLimited() {
        return false;
    }
}
