package com.gtocore.data.recipe;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.data.GTORecipeTypes.BEAM_GENERATE_RECIPES;
import static com.gtocore.common.data.GTORecipeTypes.BEAM_POLARIZE_RECIPES;
import static java.lang.Math.PI;

public class OpticalRecipe {

    public static void init() {
        BEAM_GENERATE_RECIPES.builder("infrared")
                .notConsumable(GTOItems.LOW_FREQUENCY_LASER)
                .circuitMeta(1)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 1000)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("red_700nm")
                .notConsumable(GTOItems.LOW_FREQUENCY_LASER)
                .circuitMeta(2)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 750)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("yellow_450nm")
                .notConsumable(GTOItems.MEDIUM_FREQUENCY_LASER)
                .circuitMeta(3)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 450)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("green_550nm")
                .notConsumable(GTOItems.MEDIUM_FREQUENCY_LASER)
                .circuitMeta(4)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 550)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("violet_380nm")
                .notConsumable(GTOItems.HIGH_FREQUENCY_LASER)
                .circuitMeta(5)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 380)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("ultraviolet_280nm")
                .notConsumable(GTOItems.HIGH_FREQUENCY_LASER)
                .circuitMeta(6)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 64)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 300)
                .EUt(VA[UV])
                .duration(200)
                .save();

        BEAM_GENERATE_RECIPES.builder("xray_10nm")
                .notConsumable(GTOItems.X_RAY_LASER)
                .circuitMeta(7)
                .addData(GTORecipeDataKeys.RAY_INTENSITY, 1024)
                .addData(GTORecipeDataKeys.RAY_WAVELENGTH, 10)
                .EUt(VA[UEV])
                .duration(200)
                .save();

        BEAM_POLARIZE_RECIPES.builder("45_degree_polarization")
                .notConsumableFluid(GTOMaterials.Chloroethoxyethane.getFluid(1000))
                .circuitMeta(1)
                .addData(GTORecipeDataKeys.RAY_POLARIZATION, PI / 4)
                .duration(200)
                .save();
    }
}
