package com.gtocore.integration.ae.hooks;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

public interface IAEKeyExtension {

    String gtocore$getTranslatedLower(String langCode);

    Material getGtocore$material();

    static Material get$Material(AEKey key) {
        if (key.getType() == AEKeyType.fluids()) {
            return ChemicalHelper.getMaterial(((AEFluidKey) key).fluid);
        } else if (key.getType() == AEKeyType.items()) {
            return ChemicalHelper.getMaterialEntry(((AEItemKey) key).item).material();
        }
        return GTMaterials.NULL;
    }
}
