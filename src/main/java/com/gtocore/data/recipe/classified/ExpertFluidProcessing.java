package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOMaterials;

import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import static com.gtocore.common.data.GTORecipeTypes.*;

public class ExpertFluidProcessing {

    public static void init() {
        CENTRIFUGE_RECIPES.builder("sand1")
                .chancedOutput(Items.SAND, 3090, 100)
                .inputFluids(GTOMaterials.LightOilWithSand, 1000)
                .outputFluids(GTMaterials.OilLight, 750)
                .EUt(30)
                .duration(450)
                .save();
        CENTRIFUGE_RECIPES.builder("sand2")
                .chancedOutput(Items.SAND, 3090, 100)
                .inputFluids(GTOMaterials.HeavyOilWithSand, 1000)
                .outputFluids(GTMaterials.OilHeavy, 750)
                .EUt(30)
                .duration(450)
                .save();

        PYROLYSE_RECIPES.builder("coal_dust1")
                .outputItems(TagPrefix.dust, GTMaterials.Coal)
                .inputFluids(GTOMaterials.ShaleOil, 1000)
                .outputFluids(GTOMaterials.RetortedShaleOil, 1000)
                .EUt(120)
                .duration(700)
                .save();
        DISTILLATION_RECIPES.builder("asphalt_residual_oil")
                .inputFluids(GTOMaterials.RetortedShaleOil, 8000)
                .outputFluids(GTOMaterials.ResidualOilMixture, 500)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:oil", 6800))
                .outputFluids(GTOMaterials.ShaleGas, 700)
                .EUt(240)
                .duration(1200)
                .save();
        DISTILLATION_RECIPES.builder("helium_31")
                .inputFluids(GTOMaterials.ShaleGas, 6000)
                .outputFluids(GTMaterials.Helium3, 7)
                .outputFluids(GTMaterials.Steam, 100)
                .outputFluids(GTMaterials.Helium, 80)
                .outputFluids(GTMaterials.NaturalGas, 5700)
                .outputFluids(GTMaterials.OilLight, 113)
                .EUt(240)
                .duration(900)
                .save();
        PYROLYSE_RECIPES.builder("coal_dust2")
                .outputItems(TagPrefix.dust, GTMaterials.Coal)
                .inputFluids(GTOMaterials.TightOilCrudeOil, 1000)
                .outputFluids(GTOMaterials.ProcessedTightOilCrudeOil, 1000)
                .EUt(120)
                .duration(700)
                .save();
        DISTILLATION_RECIPES.builder("oil_light1")
                .inputFluids(GTOMaterials.ProcessedTightOilCrudeOil, 14000)
                .outputFluids(GTOMaterials.ResidualOilMixture, 800)
                .outputFluids(GTMaterials.RawOil, 10700)
                .outputFluids(GTMaterials.OilLight, 750)
                .outputFluids(GTMaterials.Steam, 750)
                .outputFluids(GTMaterials.NaturalGas, 1000)
                .EUt(240)
                .duration(1600)
                .save();
        CENTRIFUGE_RECIPES.builder("small_rock_salt_dust1")
                .outputItems(TagPrefix.dustSmall, GTMaterials.RockSalt)
                .outputItems(TagPrefix.dustSmall, GTMaterials.Salt)
                .inputFluids(GTOMaterials.ResidualOilMixture, 2000)
                .outputFluids(GTOMaterials.SulfurContainingAsphaltResidualOil, 1500)
                .outputFluids(RegistriesUtils.getFluidStack("minecraft:water", 200))
                .EUt(120)
                .duration(400)
                .save();
        DESULFURIZER_RECIPES.builder("sulfur_dust2")
                .outputItems(TagPrefix.dust, GTMaterials.Sulfur)
                .inputFluids(GTOMaterials.SulfurContainingAsphaltResidualOil, 8000)
                .outputFluids(GTOMaterials.AsphaltResidualOil, 8000)
                .EUt(30)
                .duration(120)
                .save();
        CHEMICAL_RECIPES.builder("hydrogen_sulfide2")
                .inputFluids(GTOMaterials.SulfurContainingAsphaltResidualOil, 4000)
                .inputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 1000))
                .outputFluids(GTMaterials.HydrogenSulfide, 480)
                .outputFluids(GTOMaterials.AsphaltResidualOil, 4000)
                .EUt(7)
                .duration(400)
                .save();
        CRACKING_RECIPES.builder("cracked_asphalt_residual_oil")
                .inputFluids(GTOMaterials.AsphaltResidualOil, 1000)
                .inputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 6000))
                .outputFluids(GTOMaterials.CrackedAsphaltResidualOil, 1000)
                .EUt(120)
                .duration(600)
                .save();
        DISTILLATION_RECIPES.builder("ammonia3")
                .inputFluids(GTOMaterials.CrackedAsphaltResidualOil, 1000)
                .outputFluids(GTMaterials.Ammonia, 60)
                .outputFluids(GTMaterials.Octane, 100)
                .outputFluids(GTOMaterials.Cetane, 140)
                .outputFluids(GTMaterials.Phenol, 160)
                .outputFluids(GTMaterials.OilHeavy, 300)
                .outputFluids(GTMaterials.Naphthalene, 140)
                .outputFluids(GTOMaterials.Dimethylnaphthalene, 100)
                .EUt(240)
                .duration(1200)
                .save();
        GAS_COMPRESSOR_RECIPES.builder("low_pressure_aromatic_hydrocarbon_mixture")
                .inputFluids(GTOMaterials.AromaticHydrocarbonMixture, 1000)
                .outputFluids(GTOMaterials.LowPressureAromaticHydrocarbonMixture, 4000)
                .EUt(120)
                .duration(400)
                .save();
        GAS_COMPRESSOR_RECIPES.builder("low_pressure_light_gas")
                .inputFluids(GTOMaterials.LightGas, 1000)
                .outputFluids(GTOMaterials.LowPressureLightGas, 4000)
                .EUt(120)
                .duration(400)
                .save();
        DISTILLATION_RECIPES.builder("helium4")
                .inputFluids(GTOMaterials.LowPressureLightGas, 4000)
                .outputFluids(GTMaterials.Helium, 10)
                .outputFluids(GTMaterials.CarbonMonoxide, 300)
                .outputFluids(GTMaterials.Methane, 600)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 90))
                .EUt(240)
                .duration(600)
                .save();
        DISTILLATION_RECIPES.builder("naphthalene1")
                .inputFluids(GTOMaterials.LowPressureAromaticHydrocarbonMixture, 4000)
                .outputFluids(GTMaterials.Naphthalene, 140)
                .outputFluids(GTMaterials.Dimethylbenzene, 12)
                .outputFluids(GTMaterials.Styrene, 12)
                .outputFluids(GTMaterials.Toluene, 120)
                .outputFluids(GTMaterials.Benzene, 500)
                .outputFluids(GTMaterials.Ethylbenzene, 100)
                .outputFluids(GTMaterials.Cumene, 10)
                .EUt(240)
                .duration(600)
                .save();

        impureToPure(GTOMaterials.EasyToEscapeMixedGas, GTOMaterials.PureHelium);
        impureToPure(GTOMaterials.MixedNeon, GTOMaterials.PureNeon);
        impureToPure(GTOMaterials.MixedArgon, GTOMaterials.PureArgon);
        impureToPure(GTOMaterials.MixedKrypton, GTOMaterials.PureKrypton);
        impureToPure(GTOMaterials.MixedXenon, GTOMaterials.PureXenon);
        impureToPure(GTOMaterials.HighRadiationGas, GTOMaterials.PureRadon);
        impureToPure(GTOMaterials.BleachingGas, GTOMaterials.PureChlorine);
        impureToPure(GTOMaterials.FlashExplosionGas, GTOMaterials.PureHydrogen);
        impureToPure(GTOMaterials.MixedFluorine, GTOMaterials.PureFluorine);
        impureToPure(GTOMaterials.ImpureSulfuricAcid, GTOMaterials.PureSulfuricAcid);
        impureToPure(GTOMaterials.ImpureNitricAcid, GTOMaterials.PureNitricAcid);
        impureToPure(GTOMaterials.MixedHydrochloricAcid, GTOMaterials.PureHydrochloricAcid);

        CHEMICAL_BATH_RECIPES.builder("neon1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureNeon, 1000)
                .outputFluids(GTMaterials.Neon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("argon1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureArgon, 1000)
                .outputFluids(GTMaterials.Argon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("krypton1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureKrypton, 1000)
                .outputFluids(GTMaterials.Krypton, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("xenon1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureXenon, 1000)
                .outputFluids(GTMaterials.Xenon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("radon1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureRadon, 1000)
                .outputFluids(GTMaterials.Radon, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("chlorine1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureChlorine, 1000)
                .outputFluids(GTMaterials.Chlorine, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("sulfuric_acid1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureSulfuricAcid, 1000)
                .outputFluids(GTMaterials.SulfuricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("nitric_acid1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureNitricAcid, 1000)
                .outputFluids(GTMaterials.NitricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("hydrochloric_acid1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 200, 10)
                .inputFluids(GTOMaterials.PureHydrochloricAcid, 1000)
                .outputFluids(GTMaterials.HydrochloricAcid, 1000)
                .EUt(120)
                .duration(350)
                .save();
        CHEMICAL_BATH_RECIPES.builder("fluorine1")
                .chancedInput(TagPrefix.dust, GTOMaterials.GoldTrifluoride, 200, 10)
                .inputFluids(GTOMaterials.PureFluorine, 1000)
                .outputFluids(GTMaterials.Fluorine, 1000)
                .EUt(120)
                .duration(350)
                .save();
        REACTION_FURNACE_RECIPES.builder("hydrogen1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 600, 30)
                .inputFluids(GTOMaterials.PureHydrogen, 3000)
                .outputFluids(RegistriesUtils.getFluidStack("ad_astra:hydrogen", 2970))
                .outputFluids(GTMaterials.Deuterium, 29)
                .outputFluids(GTMaterials.Tritium, 1)
                .EUt(120)
                .duration(1050)
                .save();
        REACTION_FURNACE_RECIPES.builder("helium1")
                .chancedInput(TagPrefix.dust, GTOMaterials.SilicaGel, 400, 20)
                .inputFluids(GTOMaterials.PureHelium, 2000)
                .outputFluids(GTMaterials.Helium, 1998)
                .outputFluids(GTMaterials.Helium3, 2)
                .EUt(120)
                .duration(700)
                .save();
    }

    private static void impureToPure(Material impure, Material pure) {
        CENTRIFUGE_RECIPES.builder("pure_" + pure.getName())
                .inputFluids(impure, 1000)
                .outputFluids(pure, 600)
                .chancedOutput(pure.getFluid(200 / 2), 2000, 500)
                .chancedOutput(pure.getFluid(100 / 4), 1000, 500)
                .EUt(120)
                .duration(400)
                .save();
    }
}
