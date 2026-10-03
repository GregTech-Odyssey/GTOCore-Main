package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.machine.trait.NotifiableCatalystHandler;
import com.gtolib.api.machine.trait.NotifiableNotConsumableItemHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import org.jetbrains.annotations.NotNull;

public final class MECatalystPatternBufferPartMachine extends MEPatternBufferPartMachine {

    public MECatalystPatternBufferPartMachine(MetaMachineBlockEntity holder, PatternBufferType type) {
        super(holder, type);
    }

    @Override
    @NotNull
    NotifiableNotConsumableItemHandler createShareInventory() {
        return NotifiableCatalystHandler.shared(this, SHARE_SLOTS);
    }
}
