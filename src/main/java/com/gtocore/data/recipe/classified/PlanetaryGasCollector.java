package com.gtocore.data.recipe.classified;

import com.gtocore.api.data.SpaceResourceIndex;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.api.data.Dimension;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.GAS;
import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID;
import static com.gtocore.common.data.GTORecipeTypes.SPACE_GAS_COLLECTOR_RECIPES;

final class PlanetaryGasCollector {

    private static void index(int circuit, ResourceKey<Level> dimension, Material material) {
        index(circuit, dimension, material.getFluid());
    }

    private static void index(int circuit, ResourceKey<Level> dimension, Fluid fluid) {
        SpaceResourceIndex.addGas(SpaceResourceIndex.Type.PLANETARY_GAS, circuit, dimension, fluid);
    }

    public static void init() {
        index(1, Dimension.OVERWORLD.getOrbit(), GTMaterials.Air);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("1")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .circuitMeta(1)
                .outputFluids(GTMaterials.Air, 256000)
                .EUt(120 * 16)
                .duration(40)
                .save();

        index(2, Dimension.OVERWORLD.getOrbit(), GTMaterials.EnderAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("2")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .circuitMeta(2)
                .outputFluids(GTMaterials.EnderAir, 256000)
                .EUt(1920 * 16)
                .duration(40)
                .save();

        index(3, Dimension.OVERWORLD.getOrbit(), GTMaterials.NetherAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("3")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .circuitMeta(3)
                .outputFluids(GTMaterials.NetherAir, 256000)
                .EUt(480 * 16)
                .duration(40)
                .save();

        index(4, Dimension.OVERWORLD.getOrbit(), GTMaterials.LiquidNetherAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("4")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(4)
                .outputFluids(GTMaterials.LiquidNetherAir, 256000)
                .EUt(7680 * 16)
                .duration(160)
                .save();

        index(5, Dimension.OVERWORLD.getOrbit(), GTMaterials.LiquidAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("5")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(5)
                .outputFluids(GTMaterials.LiquidAir, 256000)
                .EUt(1920 * 16)
                .duration(160)
                .save();

        index(6, Dimension.OVERWORLD.getOrbit(), GTMaterials.LiquidEnderAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("6")
                .dimension(Dimension.OVERWORLD.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(6)
                .outputFluids(GTMaterials.LiquidEnderAir, 256000)
                .EUt(30720 * 16)
                .duration(160)
                .save();

        index(7, Dimension.BARNARDA_C.getOrbit(), GTOMaterials.BarnardaAir);
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("bnd")
                .dimension(Dimension.BARNARDA_C.getOrbit())
                .circuitMeta(7)
                .outputFluids(GTOMaterials.BarnardaAir, 256000)
                .EUt(122880 * 16)
                .duration(40)
                .save();

        index(8, Dimension.IO.getOrbit(), GTOMaterials.JupiterAir.getFluid(LIQUID));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("8")
                .dimension(Dimension.IO.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(8)
                .outputFluids(GTOMaterials.JupiterAir.getFluid(LIQUID), 256000)
                .EUt(30720 * 16)
                .duration(160)
                .save();

        index(9, Dimension.IO.getOrbit(), GTOMaterials.JupiterAir.getFluid(GAS));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("9")
                .dimension(Dimension.IO.getOrbit())
                .circuitMeta(9)
                .outputFluids(GTOMaterials.JupiterAir.getFluid(GAS), 256000)
                .EUt(7680 * 16)
                .duration(40)
                .save();

        index(10, Dimension.GANYMEDE.getOrbit(), GTOMaterials.JupiterAir.getFluid(LIQUID));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("10")
                .dimension(Dimension.GANYMEDE.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(10)
                .outputFluids(GTOMaterials.JupiterAir.getFluid(LIQUID), 256000)
                .EUt(30720 * 16)
                .duration(160)
                .save();

        index(11, Dimension.GANYMEDE.getOrbit(), GTOMaterials.JupiterAir.getFluid(GAS));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("11")
                .dimension(Dimension.GANYMEDE.getOrbit())
                .circuitMeta(11)
                .outputFluids(GTOMaterials.JupiterAir.getFluid(GAS), 256000)
                .EUt(7680 * 16)
                .duration(40)
                .save();

        index(12, Dimension.GLACIO.getOrbit(), GTOMaterials.GlacioAir.getFluid(LIQUID));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("12")
                .dimension(Dimension.GLACIO.getOrbit())
                .notConsumable(GTMultiMachines.VACUUM_FREEZER.asItem())
                .circuitMeta(12)
                .outputFluids(GTOMaterials.GlacioAir.getFluid(LIQUID), 256000)
                .EUt(30720 * 16)
                .duration(160)
                .save();

        index(13, Dimension.GLACIO.getOrbit(), GTOMaterials.GlacioAir.getFluid(GAS));
        SPACE_GAS_COLLECTOR_RECIPES.recipeBuilder("13")
                .dimension(Dimension.GLACIO.getOrbit())
                .circuitMeta(13)
                .outputFluids(GTOMaterials.GlacioAir.getFluid(GAS), 256000)
                .EUt(7680 * 16)
                .duration(40)
                .save();
    }
}
