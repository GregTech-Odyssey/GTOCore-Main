package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gtocore.common.data.GTORecipeTypes.SIFTER_RECIPES;

final class Sifter {

    public static void init() {
        SIFTER_RECIPES.recipeBuilder("tricalcium_phosphate_ceramic_dust")
                .inputItems(TagPrefix.dust, GTMaterials.TricalciumPhosphate, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.TricalciumPhosphateCeramic)
                .EUt(120)
                .duration(280)
                .save();

        SIFTER_RECIPES.recipeBuilder("cobalt_oxide_ceramic_dust")
                .inputItems(TagPrefix.dust, GTMaterials.CobaltOxide, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.CobaltOxideCeramic)
                .EUt(240)
                .duration(120)
                .save();

        SIFTER_RECIPES.recipeBuilder("alumina_ceramic_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.Alumina, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.AluminaCeramic)
                .EUt(120)
                .duration(150)
                .save();

        SIFTER_RECIPES.recipeBuilder("magnesium_oxide_ceramic_dust")
                .inputItems(TagPrefix.dust, GTMaterials.Magnesia, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.MagnesiumOxideCeramic)
                .EUt(240)
                .duration(140)
                .save();

        SIFTER_RECIPES.recipeBuilder("boron_carbide_ceramics_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.BoronCarbide, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.BoronCarbideCeramics)
                .EUt(240)
                .duration(100)
                .save();

        SIFTER_RECIPES.recipeBuilder("zirconia_ceramic_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.ZirconiumOxide, 2)
                .outputItems(TagPrefix.dust, GTOMaterials.ZirconiaCeramic)
                .EUt(480)
                .duration(120)
                .save();
    }
}
