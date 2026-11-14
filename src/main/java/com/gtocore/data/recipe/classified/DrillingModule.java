package com.gtocore.data.recipe.classified;

import com.gtocore.api.data.SpaceResourceIndex;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;

import earth.terrarium.adastra.common.registry.ModFluids;

import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.PLASMA;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.BarnardaAir;
import static com.gtocore.common.data.GTOMaterials.UnknowWater;
import static com.gtocore.common.data.GTORecipeTypes.DRILLING_MODULE_RECIPES;

final class DrillingModule {

    private static final Item[] drones = {
            GTOItems.SPACE_DRONE_MK1.asItem(), GTOItems.SPACE_DRONE_MK2.asItem(), GTOItems.SPACE_DRONE_MK3.asItem(),
            GTOItems.SPACE_DRONE_MK4.asItem(), GTOItems.SPACE_DRONE_MK5.asItem(), GTOItems.SPACE_DRONE_MK6.asItem()
    };
    private static final FluidStack[][] fuels = {
            { GTMaterials.RocketFuel.getFluid(10000), GTOMaterials.RocketFuelRp1.getFluid(6000) },
            { GTOMaterials.DenseHydrazineFuelMixture.getFluid(10000), GTOMaterials.RocketFuelCn3h7o3.getFluid(6000) },
            { GTOMaterials.RocketFuelH8n4c2o4.getFluid(10000), new FluidStack(ModFluids.CRYO_FUEL.get(), 6000) },
            {},
            {},
            {}
    };
    private static final FluidStack[] stellarFuel = { GTOMaterials.StellarEnergyRocketFuel.getFluid(10000) };
    private static final int[] baseTime = { 600, 400 };

    private static void buildDrillingModule(int circuit, int minDroneTire, FluidStack fluidStack) {
        buildDrillingModule("space_fluid_" + circuit, circuit, minDroneTire, fluidStack);
    }

    private static void buildDrillingModule(String id, int circuit, int minDroneTire, FluidStack fluidStack) {
        SpaceResourceIndex.addDrilling(circuit, drones[minDroneTire - 1], fuels[minDroneTire - 1], fluidStack);
        for (int i = minDroneTire - 1; i < 6; i++) {
            for (int j = 0; j < fuels[minDroneTire - 1].length; j++) {
                DRILLING_MODULE_RECIPES.recipeBuilder(id + "_" + i + "_" + j)
                        .notConsumable(drones[i], 16)
                        .circuitMeta(circuit)
                        .inputFluids(fuels[minDroneTire - 1][j])
                        .outputFluids(fluidStack)
                        .EUt(VA[8 + i])
                        .addData(GTORecipeDataKeys.TIER, minDroneTire)
                        .duration(baseTime[j] >> i)
                        .save();
            }
        }
    }

    private static void buildStellarDrillingModule(int circuit, FluidStack fluidStack) {
        SpaceResourceIndex.addDrilling(circuit, drones[5], stellarFuel, fluidStack);
        DRILLING_MODULE_RECIPES.recipeBuilder("space_fluid_" + circuit)
                .notConsumable(drones[5], 64)
                .circuitMeta(circuit)
                .inputFluids(stellarFuel[0])
                .outputFluids(fluidStack)
                .EUt(503316480)
                .addData(GTORecipeDataKeys.TIER, 6)
                .duration(750)
                .save();
    }

    public static void init() {
        buildDrillingModule(1, 1, Hydrogen.getFluid(1000000));
        buildDrillingModule(2, 1, Helium.getFluid(1000000));
        buildDrillingModule(3, 1, Nitrogen.getFluid(1000000));
        buildDrillingModule(4, 1, Methane.getFluid(1000000));
        buildDrillingModule(5, 1, SulfurDioxide.getFluid(1000000));
        buildDrillingModule(6, 1, CarbonDioxide.getFluid(1000000));
        buildDrillingModule(7, 1, NitrogenDioxide.getFluid(1000000));
        buildDrillingModule(8, 1, Ammonia.getFluid(1000000));
        buildDrillingModule(9, 1, Chlorine.getFluid(1000000));
        buildDrillingModule(10, 1, Fluorine.getFluid(1000000));
        buildDrillingModule(11, 1, CarbonMonoxide.getFluid(1000000));
        buildDrillingModule(12, 1, Oxygen.getFluid(1000000));
        buildDrillingModule(13, 2, UnknowWater.getFluid(1000000));
        buildDrillingModule(14, 2, Neon.getFluid(1000000));
        buildDrillingModule(15, 2, Argon.getFluid(1000000));
        buildDrillingModule(16, 2, Krypton.getFluid(1000000));
        buildDrillingModule(17, 2, Xenon.getFluid(1000000));
        buildDrillingModule(18, 2, Radon.getFluid(1000000));
        buildDrillingModule(19, 2, Helium3.getFluid(1000000));
        buildDrillingModule(20, 3, Deuterium.getFluid(1000000));
        buildDrillingModule(21, 3, Tritium.getFluid(1000000));
        buildDrillingModule(22, 3, HeavyFuel.getFluid(1000000));
        buildDrillingModule(23, 3, LightFuel.getFluid(1000000));
        buildDrillingModule(24, 3, Naphtha.getFluid(1000000));
        buildDrillingModule(25, 3, RefineryGas.getFluid(1000000));
        buildDrillingModule(26, 3, CoalGas.getFluid(1000000));
        buildDrillingModule(27, 3, Bromine.getFluid(1000000));
        buildDrillingModule(28, 3, BarnardaAir.getFluid(1000000));

        // 复用电路时用不同燃料区分，保留原有配方。
        buildDrillingModule("space_fluid_oil", 13, 1, Oil.getFluid(1000000));
        buildDrillingModule("space_fluid_raw_oil", 14, 1, RawOil.getFluid(1000000));
        buildDrillingModule("space_fluid_heavy_oil", 15, 1, OilHeavy.getFluid(1000000));
        buildDrillingModule("space_fluid_light_oil", 16, 1, OilLight.getFluid(1000000));
        buildDrillingModule("space_fluid_natural_gas", 17, 1, NaturalGas.getFluid(1000000));
        buildDrillingModule("space_fluid_benzene", 18, 1, Benzene.getFluid(1000000));
        buildDrillingModule("space_fluid_charcoal_byproducts", 19, 1, CharcoalByproducts.getFluid(1000000));
        buildDrillingModule("space_fluid_sulfuric_acid", 20, 1, SulfuricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_nitric_acid", 21, 1, NitricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_hydrochloric_acid", 22, 1, HydrochloricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_salt_water", 23, 1, SaltWater.getFluid(1000000));
        buildDrillingModule("space_fluid_lava", 24, 1, Lava.getFluid(1000000));

        buildDrillingModule("space_fluid_heavy_oil_with_sand", 1, 2, GTOMaterials.HeavyOilWithSand.getFluid(1000000));
        buildDrillingModule("space_fluid_light_oil_with_sand", 2, 2, GTOMaterials.LightOilWithSand.getFluid(1000000));
        buildDrillingModule("space_fluid_shale_gas", 3, 2, GTOMaterials.ShaleGas.getFluid(1000000));
        buildDrillingModule("space_fluid_shale_oil", 4, 2, GTOMaterials.ShaleOil.getFluid(1000000));
        buildDrillingModule("space_fluid_tight_oil_crude_oil", 5, 2, GTOMaterials.TightOilCrudeOil.getFluid(1000000));
        buildDrillingModule("space_fluid_aromatic_hydrocarbon_mixture", 6, 2, GTOMaterials.AromaticHydrocarbonMixture.getFluid(1000000));
        buildDrillingModule("space_fluid_residual_oil_mixture", 7, 2, GTOMaterials.ResidualOilMixture.getFluid(1000000));
        buildDrillingModule("space_fluid_impure_sulfuric_acid", 8, 2, GTOMaterials.ImpureSulfuricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_impure_nitric_acid", 9, 2, GTOMaterials.ImpureNitricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_mixed_hydrochloric_acid", 10, 2, GTOMaterials.MixedHydrochloricAcid.getFluid(1000000));
        buildDrillingModule("space_fluid_easy_to_escape_mixed_gas", 11, 2, GTOMaterials.EasyToEscapeMixedGas.getFluid(1000000));
        buildDrillingModule("space_fluid_easy_to_escape_mixed_isotope_gas", 12, 2, GTOMaterials.EasyToEscapeMixedIsotopeGas.getFluid(1000000));
        buildDrillingModule("space_fluid_high_radiation_gas", 20, 2, GTOMaterials.HighRadiationGas.getFluid(1000000));
        buildDrillingModule("space_fluid_light_gas", 21, 2, GTOMaterials.LightGas.getFluid(1000000));
        buildDrillingModule("space_fluid_heavy_flash_explosion_gas", 22, 2, GTOMaterials.HeavyFlashExplosionGas.getFluid(1000000));
        buildDrillingModule("space_fluid_mixed_xenon", 23, 2, GTOMaterials.MixedXenon.getFluid(1000000));
        buildDrillingModule("space_fluid_mixed_krypton", 24, 2, GTOMaterials.MixedKrypton.getFluid(1000000));
        buildDrillingModule("space_fluid_mixed_neon", 25, 2, GTOMaterials.MixedNeon.getFluid(1000000));
        buildDrillingModule("space_fluid_mixed_fluorine", 26, 2, GTOMaterials.MixedFluorine.getFluid(1000000));
        buildDrillingModule("space_fluid_bleaching_gas", 27, 2, GTOMaterials.BleachingGas.getFluid(1000000));

        buildDrillingModule("space_fluid_fractal_petal_solvent", 1, 3, GTOMaterials.FractalPetalSolvent.getFluid(1000000));
        buildDrillingModule("space_fluid_the_water_from_the_well_of_wisdom", 2, 3, GTOMaterials.TheWaterFromTheWellOfWisdom.getFluid(1000000));
        buildDrillingModule("space_fluid_animium", 3, 3, GTOMaterials.Animium.getFluid(1000000));

        buildStellarDrillingModule(29, GTOMaterials.WhiteDwarfMatter.getFluid(100000));
        buildStellarDrillingModule(30, GTOMaterials.BlackDwarfMatter.getFluid(100000));
        buildStellarDrillingModule(31, GTOMaterials.PrimordialHeliumPlasma.getFluid(PLASMA, 100000));
        buildStellarDrillingModule(32, GTOMaterials.PrimordialPaleozoicIronPlasma.getFluid(PLASMA, 100000));
    }
}
