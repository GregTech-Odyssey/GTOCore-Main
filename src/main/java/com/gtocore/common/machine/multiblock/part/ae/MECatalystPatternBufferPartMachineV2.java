package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.machine.trait.NotifiableNotConsumableItemHandler;
import com.gtolib.api.recipe.extension.CatalystRecipeExtension;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import org.jetbrains.annotations.NotNull;

public class MECatalystPatternBufferPartMachineV2 extends MEPatternBufferPartMachineV2 {

    public MECatalystPatternBufferPartMachineV2(MetaMachineBlockEntity holder, PatternBufferType type) {
        super(holder, type);
    }

    @Override
    @NotNull
    NotifiableNotConsumableItemHandler createShareInventory() {
        return CatalystRecipeExtension.createSharedInventory(this, SHARE_SLOTS);
    }
}
