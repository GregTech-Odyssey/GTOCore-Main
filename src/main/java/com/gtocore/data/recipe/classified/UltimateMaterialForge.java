package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.content.Chance;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import appeng.core.definitions.AEItems;

import static com.gtocore.common.data.GTORecipeTypes.ULTIMATE_MATERIAL_FORGE_RECIPES;

final class UltimateMaterialForge {

    public static void init() {
        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("void_matter")
                .inputItems(GTOItems.OMNI_MATTER)
                .inputItems(GTOItems.PELLET_ANTIMATTER)
                .inputFluids(GTMaterials.UUMatter, 2000)
                .inputFluids(GTOMaterials.Gluons, 1000)
                .outputItems(GTOItems.VOID_MATTER)
                .chancedOutput(GTOItems.CORPOREAL_MATTER.asItem(), Chance.of(2000))
                .EUt(503316480)
                .duration(400)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("temporal_matter")
                .notConsumable(GTOItems.QUANTUM_ANOMALY.asItem())
                .inputItems(GTOItems.KINETIC_MATTER)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTOMaterials.AwakenedDraconium.getFluid(FluidStorageKeys.PLASMA, 1000))
                .outputItems(GTOItems.TEMPORAL_MATTER)
                .chancedOutput(GTOItems.OMNI_MATTER.asItem(), Chance.of(500))
                .EUt(503316480)
                .duration(600)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("corporeal_matter")
                .inputItems(GTOItems.PROTO_MATTER)
                .inputItems(TagPrefix.block, GTMaterials.Iron)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTMaterials.Nihonium, 100)
                .outputItems(GTOItems.CORPOREAL_MATTER)
                .chancedOutput(TagPrefix.nugget, GTOMaterials.HeavyQuarkDegenerateMatter, Chance.of(500))
                .EUt(503316480)
                .duration(800)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("proto_matter")
                .inputItems(GTOItems.TRIPLET_NEUTRONIUM_SPHERE)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .outputItems(GTOItems.PROTO_MATTER)
                .chancedOutput(TagPrefix.ingot, GTOMaterials.Neutron, Chance.of(6000))
                .EUt(503316480)
                .duration(1600)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("dark_matter")
                .inputItems(GTOItems.TEMPORAL_MATTER)
                .inputItems(GTOItems.VOID_MATTER)
                .inputFluids(GTMaterials.UUMatter, 3000)
                .inputFluids(GTOMaterials.DimensionallyTranscendentCrudeCatalyst, 1000)
                .outputItems(GTOItems.DARK_MATTER)
                .chancedOutput(GTOItems.KINETIC_MATTER.asItem(), Chance.of(1000))
                .EUt(503316480)
                .duration(1200)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("kinetic_matter")
                .inputItems(GTOItems.CORPOREAL_MATTER)
                .inputItems(TagPrefix.block, GTMaterials.Tritanium)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTMaterials.Naquadria, 1000)
                .outputItems(GTOItems.KINETIC_MATTER)
                .chancedOutput(GTOItems.AMORPHOUS_MATTER.asItem(), Chance.of(200))
                .EUt(503316480)
                .duration(600)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("amorphous_matter")
                .inputItems(GTOItems.CORPOREAL_MATTER)
                .inputItems(TagPrefix.block, GTOMaterials.CarbonNanotubes)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTOMaterials.Legendarium, 1000)
                .outputItems(GTOItems.AMORPHOUS_MATTER)
                .chancedOutput(GTOItems.ESSENTIA_MATTER.asItem(), Chance.of(100))
                .EUt(503316480)
                .duration(800)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("pellet_antimatter")
                .inputItems(AEItems.MATTER_BALL.asItem(), 64)
                .inputItems(TagPrefix.nugget, GTOMaterials.Neutron)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTOMaterials.Antihydrogen, 10)
                .outputItems(GTOItems.PELLET_ANTIMATTER)
                .chancedOutput(GTOItems.VOID_MATTER.asItem(), Chance.of(100))
                .EUt(125829120)
                .duration(2000)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("essentia_matter")
                .inputItems(GTOItems.AMORPHOUS_MATTER)
                .inputItems(TagPrefix.block, GTOMaterials.HeavyQuarkDegenerateMatter)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTOMaterials.QuantumChromoDynamicallyConfinedMatter, 1000)
                .outputItems(GTOItems.ESSENTIA_MATTER)
                .chancedOutput(GTOItems.DARK_MATTER.asItem(), Chance.of(100))
                .EUt(503316480)
                .duration(1200)
                .save();

        ULTIMATE_MATERIAL_FORGE_RECIPES.recipeBuilder("omni_matter")
                .inputItems(GTOItems.ESSENTIA_MATTER)
                .inputItems(GTOItems.KINETIC_MATTER)
                .inputFluids(GTMaterials.UUMatter, 1000)
                .inputFluids(GTOMaterials.DenseNeutron.getFluid(FluidStorageKeys.PLASMA, 1000))
                .outputItems(GTOItems.OMNI_MATTER)
                .chancedOutput(TagPrefix.dustTiny, GTOMaterials.CosmicNeutronium, Chance.of(1000))
                .EUt(503316480)
                .duration(800)
                .save();
    }
}
