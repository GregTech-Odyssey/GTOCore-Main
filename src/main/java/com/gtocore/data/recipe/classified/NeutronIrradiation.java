package com.gtocore.data.recipe.classified;

import com.gtocore.api.data.tag.GTOTagPrefix;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gtocore.common.data.GTORecipeDataKeys.NEUTRON_FLUX;
import static com.gtocore.common.data.GTORecipeTypes.*;

class NeutronIrradiation {

    public static void init() {
        NEUTRON_IRRADIATION_RECIPES.builder("uranium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Uranium238)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Uranium238)
                .addData(NEUTRON_FLUX, 5f)
                .duration(5400)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("uranium_235_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Uranium235)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Uranium235)
                .addData(NEUTRON_FLUX, 5f)
                .duration(5400)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("neptunium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Neptunium)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Neptunium)
                .addData(NEUTRON_FLUX, 16f)
                .duration(4800)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("plutonium_241_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Plutonium241)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Plutonium241)
                .addData(NEUTRON_FLUX, 27f)
                .duration(4200)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("plutonium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Plutonium239)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Plutonium239)
                .addData(NEUTRON_FLUX, 27f)
                .duration(4200)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("americium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Americium)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Americium)
                .addData(NEUTRON_FLUX, 27f)
                .duration(3600)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("curium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Curium)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Curium)
                .addData(NEUTRON_FLUX, 38f)
                .duration(3000)
                .save();
        NEUTRON_IRRADIATION_RECIPES.builder("berkelium_excited_stainless_steel_target")
                .inputItems(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Berkelium)
                .outputItems(GTOTagPrefix.EXCITED_STAINLESS_STEEL_TARGET, GTMaterials.Berkelium)
                .addData(NEUTRON_FLUX, 50f)
                .duration(2400)
                .save();
    }
}
