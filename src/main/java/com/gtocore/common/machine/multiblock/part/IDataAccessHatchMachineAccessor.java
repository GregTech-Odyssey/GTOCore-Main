package com.gtocore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;

import appeng.api.stacks.AEItemKey;

import java.util.Set;

// todo: move codes to gtceu
public interface IDataAccessHatchMachineAccessor {

    NotifiableInventory<AEItemKey> gtocore$getImportItems();

    Set<GTRecipeDefinition> gtocore$recipes();
}
