package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Legacy machine ID; existing machines retain unlimited capacity. */
@Deprecated
public final class MEPatternBufferProxyPartMachine extends MEPatternBufferProxyPartMachineV2 {

    public MEPatternBufferProxyPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }
}
