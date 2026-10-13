package com.gtocore.data.recipe.classified;

import com.gtocore.api.electrochemistry.ElectrolyteType;
import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.IonAmount;
import com.gtocore.api.electrochemistry.Ions;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.NEUTRON_REFLECTOR;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.ELECTROPLATING_RECIPES;

public class ElectroPlating {

    public static void init() {
        ELECTROPLATING_RECIPES.builder("hydrogen_oxidation")
                .halfReaction(ElectrolyteType.WATER, new IonAmount(Ions.PROTON, 2))
                .inputFluids(Hydrogen, 1000).duration(200).EUt(30).save();
        ELECTROPLATING_RECIPES.builder("oxygen_reduction")
                .halfReaction(ElectrolyteType.WATER, new IonAmount(Ions.HYDROXIDE, 4))
                .inputFluids(Oxygen, 1000).inputFluids(Water, 2000).duration(200).EUt(30).save();
        ELECTROPLATING_RECIPES.builder("oxygen_reduction_acidic")
                .halfReaction(HalfReaction.builder(ElectrolyteType.WATER).requiredIons(new IonAmount(Ions.PROTON, 4)).build())
                .inputFluids(Oxygen, 1000).outputFluids(Water, 2000).duration(200).EUt(30).save();
        ELECTROPLATING_RECIPES.builder("sodium_oxidation")
                .halfReaction(ElectrolyteType.MOLTEN_SALT, new IonAmount(Ions.SODIUM, 1))
                .inputItems(dust, Sodium).duration(200).EUt(30).save();
        ELECTROPLATING_RECIPES.builder("chlorine_reduction")
                .halfReaction(ElectrolyteType.WATER, new IonAmount(Ions.CHLORIDE, 2))
                .inputFluids(Chlorine, 1000).duration(200).EUt(30).save();
        ELECTROPLATING_RECIPES.builder("supercritical_carbon_dioxide")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.SUPERCRITICAL_FLUID)
                                .requiredIons(new IonAmount(Ions.SUPERCRITICAL_OXYGEN, 2L))
                                .build())
                .inputItems(TagPrefix.dust, GTMaterials.ActivatedCarbon)
                .outputFluids(GTOMaterials.SupercriticalCarbonDioxide, 3000)
                .EUt(400L)
                .duration(100)
                .save();
        ELECTROPLATING_RECIPES.builder("aniline")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.ORGANIC)
                                .requiredIons(new IonAmount(Ions.PROTON, 6L))
                                .build())
                .notConsumable(GTItems.CARBON_FIBER_PLATE.asItem())
                .inputFluids(GTMaterials.Nitrobenzene.getFluid(1000))
                .outputFluids(GTOMaterials.Aniline, 1000)
                .EUt(40L)
                .duration(10)
                .save();
        ELECTROPLATING_RECIPES.builder("acetaldehyde")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.ORGANIC)
                                .ions(new IonAmount(Ions.PROTON, 2L))
                                .build())
                .notConsumable(ChemicalHelper.getItem(TagPrefix.plate, GTMaterials.Platinum))
                .inputFluids(GTMaterials.Ethanol.getFluid(1000))
                .outputFluids(GTOMaterials.Acetaldehyde, 1000)
                .EUt(40L)
                .duration(10)
                .save();

        ELECTROPLATING_RECIPES.builder("titanium_nanotube_precursor")
                .halfReaction(ElectrolyteType.ORGANIC, new IonAmount(Ions.PROTON, 2))
                .inputItems(foil, Titanium, 16)
                .notConsumable(foil, Platinum, 16)
                .inputFluids(GTOMaterials.DiethyleneGlycol.getFluid(96 * 16))
                .inputFluids(GTOMaterials.AmmoniumBifluorideSolution, 1600)
                .inputFluids(GTMaterials.DistilledWater, 1600)
                .outputItems(dust, GTOMaterials.TitaniumNanotubePrecursor)
                .outputFluids(GTOMaterials.DiethyleneGlycol.getFluid(72 * 16))
                .outputFluids(GTMaterials.HydrofluoricAcid.getFluid(72 * 16))
                .EUt(60)
                .duration(5400)
                .save();
        ELECTROPLATING_RECIPES.builder("chlorine")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .ions(new IonAmount(Ions.SODIUM, 4L))
                                .build())
                .notConsumable(ChemicalHelper.getItem(TagPrefix.plate, GTMaterials.Platinum))
                .inputFluids(GTMaterials.SaltWater.getFluid(1000))
                .outputFluids(GTMaterials.Chlorine, 1000)
                .outputFluids(RegistriesUtils.getFluidStack("minecraft:water", 1000))
                .EUt(40L)
                .duration(10)
                .save();

        ELECTROPLATING_RECIPES.builder("rhodium_plated_palladium_ingot")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .requiredIons(new IonAmount(Ions.RHODIUM, 2L))
                                .build())
                .inputItems(TagPrefix.plate, GTMaterials.Palladium, 6)
                .inputItems(TagPrefix.dust, GTMaterials.AmmoniumChloride, 2)
                .outputItems(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium, 8)
                .notConsumableFluid(RegistriesUtils.getFluidStack("minecraft:water", 1000))
                .outputFluids(GTMaterials.Ammonia, 2000)
                .EUt(7680L)
                .duration(2400)
                .save();
        ELECTROPLATING_RECIPES.builder("rhodium_plating")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .ions(new IonAmount(Ions.RHODIUM, 1L))
                                .build())
                .inputItems(TagPrefix.plate, GTMaterials.Rhodium)
                .chancedInput(GTOMaterials.TrisHydrochlorideSolution.getFluid(1000), 20, 0)
                .EUt(7680L)
                .duration(200)
                .save();

        ELECTROPLATING_RECIPES.builder("eutron_reflector")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.MOLTEN_SALT)
                                .requiredIons(new IonAmount(Ions.BERYLLIUM, 4L))
                                .build())
                .inputItems(TagPrefix.plate, GTMaterials.Ruridit)
                .inputItems(TagPrefix.plateDouble, GTMaterials.TungstenCarbide, 2)
                .outputItems(GTItems.NEUTRON_REFLECTOR)
                .inputFluids(GTMaterials.TinAlloy, 9216)
                .outputFluids(GTMaterials.TinAlloy, 4608)
                .EUt(7500L)
                .duration(20)
                .save();
        ELECTROPLATING_RECIPES.builder("beryllium_plating")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.MOLTEN_SALT)
                                .ions(new IonAmount(Ions.BERYLLIUM, 2L))
                                .build())
                .inputItems(TagPrefix.plate, GTMaterials.Beryllium, 3)
                .inputFluids(GTMaterials.Aluminium, 432)
                .outputFluids(GTMaterials.Beryllium, 144)
                .outputFluids(GTMaterials.Aluminium, 288)
                .EUt(120L)
                .duration(4000)
                .save();
        ELECTROPLATING_RECIPES.builder("emerald_plating")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.MOLTEN_SALT)
                                .ions(new IonAmount(Ions.BERYLLIUM, 3L))
                                .build())
                .notConsumable(TagPrefix.plate, GTMaterials.Platinum)
                .inputItems(TagPrefix.dust, GTOMaterials.SodiumFluoride, 6)
                .inputItems(TagPrefix.gem, GTMaterials.Emerald)
                .outputItems(TagPrefix.dust, GTMaterials.SiliconDioxide, 6)
                .outputItems(TagPrefix.dust, GTOMaterials.Cryolite)
                .EUt(120L)
                .duration(4000)
                .save();

        ELECTROPLATING_RECIPES.builder("nano_gold_deposited_carbon_nanotube_dust")
                .halfReaction(ElectrolyteType.WATER, new IonAmount(Ions.PROTON, 1), new IonAmount(Ions.CHLORIDE, 4))
                .inputItems(TagPrefix.dust, GTOMaterials.EtchedCarbonNanotube)
                .outputItems(TagPrefix.dust, GTOMaterials.NanoGoldDepositedCarbonNanotube)
                .inputFluids(GTOMaterials.ChloroauricAcid, 1000)
                .EUt(129600)
                .duration(1296)
                .save();
        ELECTROPLATING_RECIPES.builder("bipyridine")
                .halfReaction(ElectrolyteType.ORGANIC, new IonAmount(Ions.PROTON, 2))
                .notConsumable(TagPrefix.foil, GTMaterials.Graphene, 16)
                .inputFluids(GTOMaterials.Pyridine, 2000)
                .outputFluids(GTOMaterials.Bipyridine, 2000)
                .EUt(400)
                .duration(150)
                .save();
        ELECTROPLATING_RECIPES.builder("hydroquinone")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.ORGANIC)
                                .requiredIons(new IonAmount(Ions.PROTON, 2L))
                                .build())
                .notConsumable(GTItems.CARBON_FIBER_PLATE.asItem())
                .inputFluids(GTOMaterials.ParaBenzoquinone, 1000)
                .outputFluids(GTOMaterials.Hydroquinone, 1000)
                .EUt(120L)
                .duration(4000)
                .save();
        ELECTROPLATING_RECIPES.builder("tetrahydrofuran")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.ORGANIC)
                                .requiredIons(new IonAmount(Ions.PROTON, 4L))
                                .build())
                .notConsumable(GTItems.CARBON_FIBER_PLATE.asItem())
                .inputFluids(GTOMaterials.Acetylene, 1000)
                .inputFluids(GTMaterials.Formaldehyde, 2000)
                .outputFluids(GTOMaterials.Tetrahydrofuran, 1000)
                .EUt(480L)
                .duration(120)
                .save();

        ELECTROPLATING_RECIPES.builder("prussian_blue_copper_i_nanotubes_dust")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .ions(new IonAmount(Ions.HYDROXIDE, 1L))
                                .build())
                .inputItems(TagPrefix.dust, GTMaterials.PrussianBlue)
                .inputItems(TagPrefix.dust, GTMaterials.CupricOxide)
                .outputItems(TagPrefix.dust, GTOMaterials.PrussianBlueCopperINanotubes, 2)
                .notConsumableFluid(GTMaterials.DistilledWater.getFluid(1000))
                .EUt(400L)
                .duration(150)
                .save();
        ELECTROPLATING_RECIPES.builder("cycle_of_blossoms_solvent")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .requiredIons(new IonAmount(Ions.ANIMIUM, 4L))
                                .build())
                .inputItems(GTOItems.COLORFUL_MYSTICAL_FLOWER)
                .inputFluids(GTMaterials.SulfuricAcid, 1000)
                .inputFluids(GTOMaterials.FractalPetalSolvent, 250)
                .outputFluids(GTOMaterials.CycleofBlossomsSolvent, 1000)
                .chancedOutput(GTOMaterials.FractalPetalSolvent.getFluid(250), 9000, 0)
                .EUt(120)
                .duration(3264)
                .save();
        ELECTROPLATING_RECIPES.builder("animium")
                .halfReaction(
                        HalfReaction.builder(ElectrolyteType.WATER)
                                .requiredIons(new IonAmount(Ions.ANIMIUM, 4L))
                                .build())
                .inputItems("extrabotany:spirit_fragment")
                .inputFluids(GTMaterials.SulfuricAcid, 1000)
                .inputFluids(GTOMaterials.TheWaterFromTheWellOfWisdom, 250)
                .outputFluids(GTOMaterials.Animium, 1000)
                .chancedOutput(GTOMaterials.TheWaterFromTheWellOfWisdom.getFluid(250), 9000, 0)
                .EUt(120L)
                .duration(3264)
                .save();
    }
}
