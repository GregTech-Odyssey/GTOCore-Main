package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.config.GTORules;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

public class MESimplePatternBufferPartMachine extends MEPatternBufferPartMachine {

    public MESimplePatternBufferPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.SIMPLE_PATTERN_BUFFER_SLOTS.get());
    }

    @Override
    public boolean allowWirelessConnection() {
        return false;
    }
}
