package com.gtocore.data.recipe.classified;

import com.gtocore.api.data.SpaceResourceIndex;
import com.gtocore.common.data.GTOItems;

import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static com.gtocore.common.data.GTORecipeTypes.LARGE_GAS_COLLECTOR_RECIPES;

final class LargeGasCollector {

    private static void index(int circuit, ResourceKey<Level> dimension, Material material) {
        SpaceResourceIndex.addGas(SpaceResourceIndex.Type.VOID_GAS, circuit, dimension, material.getFluid());
    }

    public static void init() {
        index(1, GTODimensions.OVERWORLD, GTMaterials.Air);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("1")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.OVERWORLD))
                .circuitMeta(1)
                .outputFluids(GTMaterials.Air, 100000)
                .EUt(120)
                .duration(200)
                .save();

        index(1, GTODimensions.THE_END, GTMaterials.EnderAir);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("3")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.THE_END))
                .circuitMeta(1)
                .outputFluids(GTMaterials.EnderAir, 100000)
                .EUt(1920)
                .duration(200)
                .save();

        index(1, GTODimensions.THE_NETHER, GTMaterials.NetherAir);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("2")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.THE_NETHER))
                .circuitMeta(1)
                .outputFluids(GTMaterials.NetherAir, 100000)
                .EUt(480)
                .duration(200)
                .save();

        index(0, GTODimensions.THE_NETHER, GTMaterials.LiquidNetherAir);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("5")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.THE_NETHER))
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .outputFluids(GTMaterials.LiquidNetherAir, 100000)
                .EUt(1920)
                .duration(2000)
                .save();

        index(0, GTODimensions.OVERWORLD, GTMaterials.LiquidAir);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("4")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.OVERWORLD))
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .outputFluids(GTMaterials.LiquidAir, 100000)
                .EUt(480)
                .duration(2000)
                .save();

        index(0, GTODimensions.THE_END, GTMaterials.LiquidEnderAir);
        LARGE_GAS_COLLECTOR_RECIPES.recipeBuilder("6")
                .notConsumable(GTOItems.DIMENSION_DATA.get().getDimensionData(GTODimensions.THE_END))
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .outputFluids(GTMaterials.LiquidEnderAir, 100000)
                .EUt(7680)
                .duration(2000)
                .save();
    }
}
