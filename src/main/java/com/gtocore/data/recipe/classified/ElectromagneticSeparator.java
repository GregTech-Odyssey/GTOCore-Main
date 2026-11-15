package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.Chance;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gtocore.common.data.GTORecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES;

final class ElectromagneticSeparator {

    public static void init() {
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("graphene_oxide_dust")
                .inputItems(GTOItems.GRAPHENE_IRON_PLATE)
                .outputItems(TagPrefix.dust, GTOMaterials.GrapheneOxide, 9)
                .outputItems(TagPrefix.dust, GTMaterials.Iron)
                .EUt(30)
                .duration(120)
                .save();

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("raw_tengam_dust")
                .inputItems(TagPrefix.dustPure, GTOMaterials.Jasper)
                .outputItems(TagPrefix.dust, GTOMaterials.Jasper)
                .chancedOutput(TagPrefix.dust, GTOMaterials.RawTengam, Chance.of(1000))
                .chancedOutput(TagPrefix.dust, GTOMaterials.RawTengam, Chance.of(500))
                .EUt(24)
                .duration(200)
                .save();

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("purified_tengam_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.CleanRawTengam)
                .outputItems(TagPrefix.dust, GTOMaterials.PurifiedTengam)
                .chancedOutput(TagPrefix.dust, GTMaterials.NeodymiumMagnetic, Chance.of(1000))
                .chancedOutput(TagPrefix.dust, GTMaterials.SamariumMagnetic, Chance.of(500))
                .EUt(7864320)
                .duration(200)
                .save();
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("enrich_etrium_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.LightEmittingChargedSuspiciousWaste)
                .chancedOutput(TagPrefix.dust, GTOMaterials.EnrichedLightEmittingChargedSuspiciousWaste, Chance.of(9000))
                .chancedOutput(TagPrefix.dust, GTOMaterials.EnrichedLightEmittingChargedSuspiciousWaste, Chance.of(1000))
                .chancedOutput(TagPrefix.dust, GTMaterials.Fermium, Chance.of(4500))
                .EUt(122800)
                .duration(200)
                .save();
    }
}
