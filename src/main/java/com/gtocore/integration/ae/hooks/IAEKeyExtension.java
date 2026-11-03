package com.gtocore.integration.ae.hooks;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;

public interface IAEKeyExtension {

    String gtocore$getTranslatedLower(String langCode);

    Material getGtocore$material();

    static Material get$Material(AEKey key) {
        if (key.getType() == AEKeyTypes.FLUIDS) {
            return ChemicalHelper.getMaterial(((AEFluidKey) key).fluid);
        } else if (key.getType() == AEKeyTypes.ITEMS) {
            var entry = ChemicalHelper.getMaterialEntry(((AEItemKey) key).item);
            return entry == null ? null : entry.material();
        }
        return null;
    }
}
