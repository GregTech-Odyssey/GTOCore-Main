package com.gtocore.api.electrochemistry;

import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;

public final class MolarUnits {

    private MolarUnits() {}

    public static long fluidAmount(Material material, long moles) {
        return Math.multiplyExact(moles, material.hasProperty(PropertyKey.DUST) ? 144L : 1000L);
    }

    public static void output(RecipeBuilder builder, Material material, long moles) {
        if (material.hasProperty(PropertyKey.DUST)) builder.outputItems(KeyIngredient.item(ChemicalHelper.getItem(TagPrefix.dust, material)), moles);
        else builder.outputFluids(material.getFluid(), fluidAmount(material, moles));
    }
}
