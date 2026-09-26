package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOMaterials;

import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Helium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.NaturalGas;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steam;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;

public class ExpertFluidProcessing {

    public static void init() {
        CENTRIFUGE_RECIPES.builder("sand1")
                .chancedOutput(Items.SAND, 3090, 100)
                .inputFluids(LightOilWithSand, 1000)
                .outputFluids(OilLight, 750)
                .EUt(30)
                .duration(450)
                .save();
        CENTRIFUGE_RECIPES.builder("sand2")
                .chancedOutput(Items.SAND, 3090, 100)
                .inputFluids(HeavyOilWithSand, 1000)
                .outputFluids(OilHeavy, 750)
                .EUt(30)
                .duration(450)
                .save();

        PYROLYSE_RECIPES.builder("coal_dust1")
                .outputItems(TagPrefix.dust, AsphaltOilSlag)
                .inputFluids(ShaleOil, 1000)
                .outputFluids(RetortedShaleOil, 1000)
                .EUt(120)
                .duration(700)
                .save();
        DISTILLATION_RECIPES.builder("asphalt_residual_oil")
                .inputFluids(RetortedShaleOil, 8000)
                .outputFluids(ResidualOilMixture, 500)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:oil", 6800))
                .outputFluids(ShaleGas, 700)
                .EUt(240)
                .duration(1200)
                .save();
        DISTILLATION_RECIPES.builder("helium_31")
                .inputFluids(ShaleGas, 6000)
                .outputFluids(Helium3, 7)
                .outputFluids(Steam, 100)
                .outputFluids(Helium, 80)
                .outputFluids(NaturalGas, 5700)
                .outputFluids(OilLight, 113)
                .EUt(240)
                .duration(900)
                .save();
        PYROLYSE_RECIPES.builder("coal_dust2")
                .outputItems(TagPrefix.dust, AsphaltOilSlag)
                .inputFluids(TightOilCrudeOil, 1000)
                .outputFluids(ProcessedTightOilCrudeOil, 1000)
                .EUt(120)
                .duration(700)
                .save();
        DISTILLATION_RECIPES.builder("oil_light1")
                .inputFluids(ProcessedTightOilCrudeOil, 14000)
                .outputFluids(ResidualOilMixture, 800)
                .outputFluids(RawOil, 10700)
                .outputFluids(OilLight, 750)
                .outputFluids(Steam, 750)
                .outputFluids(NaturalGas, 1000)
                .EUt(240)
                .duration(1600)
                .save();
        CENTRIFUGE_RECIPES.builder("small_rock_salt_dust1")
                .outputItems(TagPrefix.dustSmall, RockSalt)
                .outputItems(TagPrefix.dustSmall, Salt)
                .inputFluids(ResidualOilMixture, 2000)
                .outputFluids(SulfurContainingAsphaltResidualOil, 1500)
                .outputFluids(RegistriesUtils.getFluidStack("minecraft:water", 200))
                .EUt(120)
                .duration(400)
                .save();
        DESULFURIZER_RECIPES.builder("sulfur_dust2")
                .outputItems(TagPrefix.dust, Sulfur)
                .inputFluids(SulfurContainingAsphaltResidualOil, 8000)
                .outputFluids(AsphaltResidualOil, 8000)
                .EUt(30)
                .duration(120)
                .save();
        CHEMICAL_RECIPES.builder("hydrogen_sulfide2")
                .inputFluids(SulfurContainingAsphaltResidualOil, 4000)
                .inputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 1000))
                .outputFluids(HydrogenSulfide, 480)
                .outputFluids(AsphaltResidualOil, 4000)
                .EUt(7)
                .duration(400)
                .save();
        CRACKING_RECIPES.builder("cracked_asphalt_residual_oil")
                .inputFluids(AsphaltResidualOil, 1000)
                .inputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 6000))
                .outputFluids(CrackedAsphaltResidualOil, 1000)
                .EUt(120)
                .duration(600)
                .save();
        DISTILLATION_RECIPES.builder("ammonia3")
                .inputFluids(CrackedAsphaltResidualOil, 1000)
                .outputFluids(Ammonia, 60)
                .outputFluids(Octane, 100)
                .outputFluids(Cetane, 140)
                .outputFluids(Phenol, 160)
                .outputFluids(OilHeavy, 300)
                .outputFluids(Naphthalene, 140)
                .outputFluids(Dimethylnaphthalene, 100)
                .EUt(240)
                .duration(1200)
                .save();
        GAS_COMPRESSOR_RECIPES.builder("low_pressure_aromatic_hydrocarbon_mixture")
                .inputFluids(AromaticHydrocarbonMixture, 1000)
                .outputFluids(LowPressureAromaticHydrocarbonMixture, 4000)
                .EUt(120)
                .duration(400)
                .save();
        GAS_COMPRESSOR_RECIPES.builder("low_pressure_light_gas")
                .inputFluids(LightGas, 1000)
                .outputFluids(LowPressureLightGas, 4000)
                .EUt(120)
                .duration(400)
                .save();
        DISTILLATION_RECIPES.builder("helium4")
                .inputFluids(LowPressureLightGas, 4000)
                .outputFluids(Helium, 10)
                .outputFluids(CarbonMonoxide, 300)
                .outputFluids(Methane, 600)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 90))
                .EUt(240)
                .duration(600)
                .save();
        DISTILLATION_RECIPES.builder("naphthalene1")
                .inputFluids(LowPressureAromaticHydrocarbonMixture, 4000)
                .outputFluids(Naphthalene, 140)
                .outputFluids(Dimethylbenzene, 12)
                .outputFluids(Styrene, 12)
                .outputFluids(Toluene, 120)
                .outputFluids(Benzene, 500)
                .outputFluids(Ethylbenzene, 100)
                .outputFluids(Cumene, 10)
                .EUt(240)
                .duration(600)
                .save();

        impureToPure(EasyToEscapeMixedGas, PureHelium, SedimentarySludge, FineDustSoil);
        impureToPure(MixedNeon, PureNeon, FineDustSoil, MixedMetalDustSoil, IgneousSludge);
        impureToPure(MixedArgon, PureArgon, FineDustSoil, MixedMetalDustSoil, MetamorphicSludge);
        impureToPure(MixedKrypton, PureKrypton, FineDustSoil, GlassySludge, IgneousSludge);
        impureToPure(MixedXenon, PureXenon, FineDustSoil, GlassySludge, MetamorphicSludge);
        impureToPure(HighRadiationGas, PureRadon, RadioactiveWasteMud, CalcareousSludge);
        impureToPure(BleachingGas, PureChlorine, BleachingStone, CalcareousSludge);
        impureToPure(FlashExplosionGas, PureHydrogen, MixedMetalDustSoil);
        impureToPure(MixedFluorine, PureFluorine, FluorideContainingSlagMud);
        impureToPure(ImpureSulfuricAcid, PureSulfuricAcid, CalciumSulfateStone);
        impureToPure(ImpureNitricAcid, PureNitricAcid, AcidicOxidizedMudSlag);
        impureToPure(MixedHydrochloricAcid, PureHydrochloricAcid, AcidicOxidizedMudSlag);

        CHEMICAL_BATH_RECIPES.builder("neon1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureNeon, 1000)
                .outputFluids(Neon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("argon1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureArgon, 1000)
                .outputFluids(Argon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("krypton1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureKrypton, 1000)
                .outputFluids(Krypton, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("xenon1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureXenon, 1000)
                .outputFluids(Xenon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("radon1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureRadon, 1000)
                .outputFluids(Radon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("chlorine1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureChlorine, 1000)
                .outputFluids(Chlorine, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("sulfuric_acid1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureSulfuricAcid, 1000)
                .outputFluids(SulfuricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("nitric_acid1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureNitricAcid, 1000)
                .outputFluids(NitricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("hydrochloric_acid1")
                .chancedInput(TagPrefix.dust, SilicaGel, 200, 10)
                .inputFluids(PureHydrochloricAcid, 1000)
                .outputFluids(HydrochloricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("fluorine1")
                .chancedInput(TagPrefix.dust, GoldTrifluoride, 200, 10)
                .inputFluids(PureFluorine, 1000)
                .outputFluids(Fluorine, 1000)
                .EUt(120)
                .duration(350)
                .save();
        REACTION_FURNACE_RECIPES.builder("hydrogen1")
                .chancedInput(TagPrefix.dust, SilicaGel, 600, 30)
                .inputFluids(PureHydrogen, 3000)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 2970))
                .outputFluids(Deuterium, 29)
                .outputFluids(Tritium, 1)
                .EUt(120)
                .duration(1050)
                .save();
        REACTION_FURNACE_RECIPES.builder("helium1")
                .chancedInput(TagPrefix.dust, SilicaGel, 400, 20)
                .inputFluids(PureHelium, 2000)
                .outputFluids(Helium, 1998)
                .outputFluids(Helium3, 2)
                .EUt(120)
                .duration(700)
                .save();
        SIFTER_RECIPES.builder("stone_dust2")
                .inputItems(TagPrefix.dust, SedimentarySludge)
                .outputItems(TagPrefix.dust, Clay)
                .chancedOutput(TagPrefix.dustImpure, Calcite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, RockSalt, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Apatite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Pyrolusite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Goethite, 7500, 700)
                .EUt(30)
                .duration(2000)
                .save();
        SIFTER_RECIPES.builder("glass_dust2")
                .inputItems(TagPrefix.dust, GlassySludge)
                .chancedOutput(TagPrefix.dustImpure, Quartzite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Spessartine, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Pollucite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Opal, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Borax, 7500, 700)
                .EUt(30)
                .duration(2000)
                .save();
        SIFTER_RECIPES.builder("impure_ilmeniete_dust")
                .inputItems(TagPrefix.dust, IgneousSludge)
                .chancedOutput(TagPrefix.dustImpure, Ilmenite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Monazite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, GarnetRed, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Lepidolite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, BasalticMineralSand, 7500, 700)
                .chancedOutput(TagPrefix.dust, ZirconiumOxide, 7500, 700)
                .EUt(30)
                .duration(2000)
                .save();
        SIFTER_RECIPES.builder("impure_lepideolite_dust")
                .inputItems(TagPrefix.dust, MetamorphicSludge)
                .chancedOutput(TagPrefix.dustImpure, Lepidolite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, GreenSapphire, 7500, 700)
                .chancedOutput(TagPrefix.dust, MetalMixture, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Graphite, 7500, 700)
                .chancedOutput(TagPrefix.dust, MetallicResidues, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, NaquadahEnriched, 7500, 700)
                .EUt(30)
                .duration(2000)
                .save();
        SIFTER_RECIPES.builder("quicklieme_dust")
                .inputItems(TagPrefix.dust, CalcareousSludge)
                .chancedOutput(TagPrefix.dust, Quicklime, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Calcite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Pyrite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Magnesite, 7500, 700)
                .chancedOutput(TagPrefix.dustImpure, Gypsum, 7500, 700)
                .chancedOutput(TagPrefix.dust, Clay, 7500, 700)
                .EUt(30)
                .duration(2000)
                .save();
        CENTRIFUGE_RECIPES.builder("calcium_chloreide_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.BleachingStone, 6)
                .outputItems(TagPrefix.dust, GTMaterials.CalciumChloride)
                .outputItems(TagPrefix.dust, GTMaterials.FullersEarth)
                .outputItems(TagPrefix.dust, GTMaterials.CalciumCarbonate)
                .outputItems(TagPrefix.dust, GTMaterials.MagnesiumChloride)
                .outputFluids(GTMaterials.Chlorine, 1000)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:oxygen", 1000))
                .EUt(120)
                .duration(300)
                .save();
        CENTRIFUGE_RECIPES.builder("uvarovite_deust")
                .inputItems(TagPrefix.dust, GTOMaterials.CalciumSulfateStone, 9)
                .outputItems(TagPrefix.dust, GTMaterials.Uvarovite)
                .outputItems(TagPrefix.dustImpure, GTMaterials.TricalciumPhosphate)
                .outputItems(TagPrefix.dustImpure, GTMaterials.Borax)
                .outputItems(TagPrefix.dustImpure, GTMaterials.Gypsum, 5)
                .outputFluids(GTMaterials.DilutedSulfuricAcid, 1000)
                .EUt(120)
                .duration(300)
                .save();
        CENTRIFUGE_RECIPES.builder("aluminium_treifluoride_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.FluorideContainingSlagMud, 29)
                .outputItems(TagPrefix.dust, GTOMaterials.AluminiumTrifluoride, 12)
                .outputItems(TagPrefix.dust, GTOMaterials.NaquadahContainRareEarthFluoride)
                .outputItems(TagPrefix.dust, GTMaterials.TitaniumTrifluoride, 4)
                .outputItems(TagPrefix.dust, GTOMaterials.Fluorite, 11)
                .outputItems(TagPrefix.dust, GTOMaterials.TriniumTetrafluoride)
                .EUt(120)
                .duration(3000)
                .save();
        CENTRIFUGE_RECIPES.builder("iron_58e_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.RadioactiveWasteMud, 15)
                .outputItems(TagPrefix.dust, GTOMaterials.Iron58Source)
                .outputItems(TagPrefix.dust, GTMaterials.Technetium)
                .outputItems(TagPrefix.dust, GTOMaterials.Chromium54Source, 4)
                .outputItems(TagPrefix.dustImpure, GTMaterials.Lead, 5)
                .outputItems(TagPrefix.dustImpure, GTMaterials.Naquadah)
                .outputItems(TagPrefix.dust, GTOMaterials.Zinc70Source)
                .outputFluids(GTMaterials.Radon, 1000)
                .outputFluids(GTOMaterials.Titanium50Tetrafluoride, 1000)
                .EUt(120)
                .duration(3000)
                .save();
        CENTRIFUGE_RECIPES.builder("chromium_tri3oxide_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.AcidicOxidizedMudSlag, 6)
                .outputItems(TagPrefix.dust, GTMaterials.ChromiumTrioxide)
                .outputItems(TagPrefix.dust, GTOMaterials.Alumina)
                .outputItems(TagPrefix.dust, GTMaterials.PhosphorusPentoxide)
                .outputItems(TagPrefix.dust, GTMaterials.SiliconDioxide)
                .outputFluids(GTMaterials.NitricAcid, 1000)
                .outputFluids(GTMaterials.Iron3Chloride, 1000)
                .EUt(120)
                .duration(300)
                .save();
        CENTRIFUGE_RECIPES.builder("small_clay_d3ust")
                .inputItems(TagPrefix.dust, GTOMaterials.FineDustSoil)
                .outputItems(TagPrefix.dustSmall, GTMaterials.Clay)
                .outputItems(TagPrefix.dustTiny, GTMaterials.Diatomite)
                .outputItems(TagPrefix.dustTiny, GTOMaterials.GnomeCrystal)
                .outputItems(TagPrefix.dustTiny, GTOMaterials.SylphCrystal)
                .EUt(120)
                .duration(300)
                .save();
        CENTRIFUGE_RECIPES.builder("ferromagnetic_residues_3dust")
                .inputItems(TagPrefix.dust, GTOMaterials.MixedMetalDustSoil, 4)
                .outputItems(TagPrefix.dust, GTOMaterials.FerromagneticResidues)
                .outputItems(TagPrefix.dust, GTOMaterials.FineDustSoil)
                .outputItems(TagPrefix.dust, GTOMaterials.ParamagneticResidues)
                .outputItems(TagPrefix.dust, GTOMaterials.HeavyFerromagneticResidues)
                .EUt(120)
                .duration(3000)
                .save();
    }

    private static void impureToPure(Material impure, Material pure, Material... extra) {
        var c = CENTRIFUGE_RECIPES.builder("pure_" + pure.getName())
                .inputFluids(impure, 1000)
                .outputFluids(pure, 650)
                .chancedOutput(pure.getFluid(350), 5000, 500)
                .chancedOutput(pure.getFluid(250), 3000, 500)
                .EUt(120)
                .duration(400);
        for (Material m : extra) {
            c.chancedOutput(TagPrefix.dust, m, 1500, 500);
        }
        c.save();
    }
}
