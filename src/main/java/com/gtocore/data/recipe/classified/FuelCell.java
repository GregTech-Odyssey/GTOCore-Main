package com.gtocore.data.recipe.classified;

import com.gtocore.api.electrochemistry.ElectrolyteType;
import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.IonAmount;
import com.gtocore.api.electrochemistry.IonCombinations;
import com.gtocore.api.electrochemistry.Ions;
import com.gtocore.common.data.GTOFluidStorageKey;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.generator.FlowFuelCellGenerator;
import com.gtocore.common.recipe.condition.HeatCondition;

import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;

public final class FuelCell {

    public static void init() {
        IonCombinations.init();

        CHEMICAL_BATH_RECIPES.builder("nickel_tetracarbonyl")
                .inputItems(TagPrefix.dust, GTMaterials.Nickel)
                .inputFluids(GTMaterials.CarbonMonoxide, 4000)
                .outputFluids(GTOMaterials.NickelTetracarbonyl, 4000)
                .EUt(542L)
                .duration(200)
                .save();

        CANNER_RECIPES.builder("hydrogen_saturated_nickel_tetracarbonyl")
                .inputFluids(GTOMaterials.NickelTetracarbonyl, 100)
                .inputFluids(GTOMaterials.LiquidHydrogen, 10)
                .outputFluids(GTOMaterials.HydrogenSaturatedNickelTetracarbonyl, 10)
                .outputFluids(GTOMaterials.NickelTetracarbonyl, 90)
                .EUt(122000L)
                .duration(200)
                .addCondition(HeatCondition.maximumMachineTemperature(10))
                .maxHeat(10)
                .save();

        ELECTRIC_IMPLOSION_COMPRESSOR_RECIPES.builder("chipped_metallic_hydrogen_gem")
                .inputItems(TagPrefix.dust, GTOMaterials.HmxExplosive)
                .inputItems(TagPrefix.dust, GTOMaterials.HydrogenSaturatedNickelTetracarbonyl)
                .chancedOutput(ChemicalHelper.getItem(TagPrefix.gemChipped, GTOMaterials.MetallicHydrogen), 5000, 0)
                .EUt(400000L)
                .duration(20)
                .maxHeat(10)
                .save();

        FlowFuelCellGenerator.Wrapper.ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.reference2LongEntrySet().fastForEach(entry -> {
            var material = entry.getKey();
            long density = entry.getLongValue();
            var electrolyteType = ElectrolyteType.WATER;
            FUEL_CELL_ENERGY_ABSORPTION_RECIPES.builder(material.getName() + "_charge")
                    .halfReaction(electrolyteType, false, new IonAmount(Ions.HYDROXIDE, 1))
                    .inputFluids(material.getFluid(GTOFluidStorageKey.ENERGY_RELEASE), 1000)
                    .outputFluids(material.getFluid(GTOFluidStorageKey.ENERGY_STORAGE), 1000)
                    .addData(GTORecipeDataKeys.CONVERTED_ENERGY, density)
                    .EUt(1).duration(20).save();
            FUEL_CELL_ENERGY_RELEASE_RECIPES.builder(material.getName() + "_release")
                    .inputFluids(material.getFluid(GTOFluidStorageKey.ENERGY_STORAGE), 200)
                    .outputFluids(material.getFluid(GTOFluidStorageKey.ENERGY_RELEASE), 190)
                    .EUt(-density).duration(200).save();
        });

        FUEL_CELL_ENERGY_ABSORPTION_RECIPES.builder("metallic_hydrogen_charge")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .ions(new IonAmount(Ions.PROTON, 8L))
                                .fuel(true)
                                .build())
                .inputItems(TagPrefix.gemExquisite, GTOMaterials.MetallicHydrogen)
                .outputFluids(GTOMaterials.SupercriticalSteam, 4000)
                .EUt(1L)
                .duration(20)
                .addData(GTORecipeDataKeys.CONVERTED_ENERGY, 1152 * 360000L)
                .save();

        PowerlessJetpack.FUELS.forEach((fuel, duration) -> {
            if (!(fuel.ingredient().key() instanceof AEFluidKey key)) return;
            var fluid = key.getFluid();
            var composition = FuelCellFuel.find(RegistriesUtils.getIdLocation(fluid).toString());
            long energy = Math.multiplyExact(Math.multiplyExact((long) duration, 4), composition.amount()) / fuel.amount();
            var builder = oxidize(fluid, composition, energy);
            addResidues(builder, composition);
            builder.save();

            // The mixer uses 1000 mB UDMH and 1000 mB O2 to make 3000 mB rocket fuel.
            if (fluid == RocketFuel.getFluid()) {
                var udmh = FuelCellFuel.find("gtceu:dimethylhydrazine");
                long udmhEnergy = Math.multiplyExact(Math.multiplyExact((long) duration, 4), 3000) / fuel.amount();
                oxidize(Dimethylhydrazine.getFluid(), udmh, udmhEnergy).save();
            }
        });
    }

    private static RecipeBuilder oxidize(Fluid fluid, FuelCellFuel fuel, long energy) {
        var reaction = HalfReaction.builder(ElectrolyteType.WATER)
                .ions(new IonAmount(Ions.PROTON, fuel.protons())).fuel(true);
        if (fuel.chlorine() > 0) {
            reaction.ions(new IonAmount(Ions.PROTON, fuel.protons()), new IonAmount(Ions.CHLORIDE, fuel.chlorine()));
        }
        var builder = FUEL_CELL_ENERGY_ABSORPTION_RECIPES.builder(fuel.fluidId().replace(':', '_'))
                .halfReaction(reaction.build())
                .inputFluids(fluid, fuel.amount()).EUt(1).duration(20)
                .addData(GTORecipeDataKeys.CONVERTED_ENERGY, energy);
        if (fuel.water() > 0) builder.inputFluids(Water.getFluid(), fuel.water() * 100);
        else if (fuel.water() < 0) builder.outputFluids(Water.getFluid(), -fuel.water() * 100);
        if (fuel.carbon() > 0) builder.outputFluids(CarbonDioxide.getFluid(), fuel.carbon() * 100);
        if (fuel.sulfur() > 0) builder.outputFluids(SulfurDioxide.getFluid(), fuel.sulfur() * 100);
        if (fuel.nitrogen() > 0) builder.outputFluids(Nitrogen.getFluid(), fuel.nitrogen() * 50);
        if (fuel.helium() > 0) builder.outputFluids(Helium.getFluid(), fuel.helium() * 100);
        return builder;
    }

    private static void addResidues(RecipeBuilder builder, FuelCellFuel fuel) {
        switch (fuel.fluidId()) {
            case "gtceu:coal_gas" -> builder.outputFluids(Creosote.getFluid(), fuel.amount() / 25);
            case "gtocore:explosivehydrazine", "ad_astra:cryo_fuel", "gtocore:stellar_energy_rocket_fuel" -> {
                long explosiveAmount = switch (fuel.fluidId()) {
                    case "ad_astra:cryo_fuel" -> fuel.amount();
                    case "gtocore:stellar_energy_rocket_fuel" -> fuel.amount() * 3 / 5;
                    default -> fuel.amount();
                };
                // Oxidizable fire-charge and gelled-toluene atoms are already in the explicit table.
                builder.outputItems(TagPrefix.dustTiny, Saltpeter, Math.toIntExact(explosiveAmount * 2 / 1000));
                if (fuel.fluidId().equals("ad_astra:cryo_fuel")) {
                    builder.outputItems(RegistriesUtils.getItem("ad_astra:ice_shard"), Math.toIntExact(fuel.amount() * 16 / 1000));
                } else if (fuel.fluidId().equals("gtocore:stellar_energy_rocket_fuel")) {
                    builder.outputItems(TagPrefix.dustTiny, GTOMaterials.HmxExplosive, Math.toIntExact(fuel.amount() * 18 / 5000))
                            .outputItems(TagPrefix.dustTiny, Naquadah, Math.toIntExact(fuel.amount() * 9 / 5000));
                }
            }
        }
    }
}
