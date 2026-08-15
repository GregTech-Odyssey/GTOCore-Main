package com.gtocore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;

import java.util.Set;

// todo: move codes to gtceu
public interface IDataAccessHatchMachineAccessor {

    NotifiableItemStackHandler gtocore$getImportItems();

    Set<GTRecipeDefinition> gtocore$recipes();
}
