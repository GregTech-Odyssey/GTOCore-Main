package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOItems;

import com.gregtechceu.gtceu.api.recipe.content.Chance;

import static com.gtocore.common.data.GTORecipeTypes.ANNIHILATE_GENERATOR_RECIPES;

final class AnnihilateGenerator {

    public static void init() {
        ANNIHILATE_GENERATOR_RECIPES.recipeBuilder("neutronium_antimatter_fuel_rod")
                .inputItems(GTOItems.NEUTRONIUM_ANTIMATTER_FUEL_ROD)
                .chancedOutput(GTOItems.ANNIHILATION_CONSTRAINER.asItem(), Chance.of(9000))
                .EUt(-549755813888L)
                .duration(200)
                .save();

        ANNIHILATE_GENERATOR_RECIPES.recipeBuilder("draconium_antimatter_fuel_rod")
                .inputItems(GTOItems.DRACONIUM_ANTIMATTER_FUEL_ROD)
                .chancedOutput(GTOItems.ANNIHILATION_CONSTRAINER.asItem(), Chance.of(8000))
                .EUt(-8796093022208L)
                .duration(200)
                .save();

        ANNIHILATE_GENERATOR_RECIPES.recipeBuilder("cosmic_neutronium_antimatter_fuel_rod")
                .inputItems(GTOItems.COSMIC_NEUTRONIUM_ANTIMATTER_FUEL_ROD)
                .chancedOutput(GTOItems.ANNIHILATION_CONSTRAINER.asItem(), Chance.of(7000))
                .EUt(-140737488355328L)
                .duration(200)
                .save();

        ANNIHILATE_GENERATOR_RECIPES.recipeBuilder("infinity_antimatter_fuel_rod")
                .inputItems(GTOItems.INFINITY_ANTIMATTER_FUEL_ROD)
                .chancedOutput(GTOItems.ANNIHILATION_CONSTRAINER.asItem(), Chance.of(6000))
                .EUt(-2251799813685248L)
                .duration(200)
                .save();
    }
}
