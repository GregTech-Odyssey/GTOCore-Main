package com.gtocore.data.recipe.processing;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustSmall;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;

public final class MembraneMaterialRecipes {

    public static void init() {
        solventsAndAdditives();
        polymers();
        nafion();
        biomaterials();
        membrane();
    }

    // 游戏性简化配方：1 粉 = 有粉材料的 144 mB = 无粉材料的 1000 mB，消耗与产出总当量相等。
    private static void solventsAndAdditives() {
        LARGE_CHEMICAL_RECIPES.builder("dimethylformamide")
                .inputFluids(Methanol, 2000)
                .inputFluids(Ammonia, 1000)
                .inputFluids(CarbonMonoxide, 1000)
                .outputFluids(Dimethylformamide, 3000)
                .outputFluids(Water, 1000)
                .duration(240).EUt(VA[HV]).save();

        CHEMICAL_RECIPES.builder("dioxane_14")
                .inputFluids(EthyleneGlycol, 2000)
                .catalyst(GTMaterials.Magnesium)
                .outputFluids(Dioxane14, 1000)
                .outputFluids(Water, 1000)
                .duration(200).EUt(VA[HV]).save();

        LARGE_CHEMICAL_RECIPES.builder("tetraethylammonium_hexafluorophosphate")
                .inputItems(dust, Phosphorus)
                .inputFluids(Methanol, 4000)
                .inputFluids(Ammonia, 1000)
                .inputFluids(Fluorine, 6000)
                .outputItems(dust, TetraethylammoniumHexafluorophosphate, 10)
                .outputFluids(Hydrogen, 2000)
                .duration(400).EUt(VA[EV]).save();

        LARGE_CHEMICAL_RECIPES.builder("cryolite")
                .inputItems(dust, Sodium, 3)
                .inputItems(dust, Aluminium)
                .inputFluids(Fluorine, 6000)
                .outputItems(dust, Cryolite, 10)
                .duration(240).EUt(VA[HV]).save();

        CHEMICAL_RECIPES.builder("acrylamide")
                .inputFluids(Acrylonitrile, 1000)
                .inputFluids(Water, 1000)
                .outputItems(dust, Acrylamide, 2)
                .duration(200).EUt(VA[HV]).save();

        FLUID_HEATER_RECIPES.builder("acrylamide_monomer")
                .inputItems(dust, Acrylamide)
                .outputFluids(AcrylamideMonomer, 1000)
                .duration(100).EUt(VA[MV]).save();

        CHEMICAL_RECIPES.builder("sodium_citrate")
                .inputItems(dust, Sodium, 3)
                .inputFluids(CitricAcid, 1000)
                .outputItems(dust, SodiumCitrate, 3)
                .outputFluids(Hydrogen, 1000)
                .duration(160).EUt(VA[HV]).save();

        BIOCHEMICAL_REACTION_RECIPES.builder("malic_acid")
                .notConsumable(dust, EschericiaColi)
                .inputItems(dust, Sugar)
                .inputFluids(Water, 1000)
                .outputItems(dust, MalicAcid)
                .outputFluids(Hydrogen, 1000)
                .duration(400).EUt(VA[EV]).save();

        LARGE_CHEMICAL_RECIPES.builder("hexadecyl_phosphate")
                .inputItems(dust, HexadecylBromide)
                .inputFluids(PhosphoricAcid, 1000)
                .inputFluids(Water, 1000)
                .outputItems(dust, HexadecylPhosphate, 2)
                .outputFluids(HydrobromicAcid, 1000)
                .duration(240).EUt(VA[EV]).save();

        BIOCHEMICAL_REACTION_RECIPES.builder("adenosine_triphosphate_disodium_salt")
                .inputItems(dust, Sugar, 2)
                .inputItems(dust, Sodium, 2)
                .inputItems(dust, Phosphorus, 3)
                .inputFluids(Ammonia, 5000)
                .inputFluids(BacterialGrowthMedium, 1000)
                .outputItems(dust, AdenosineTriphosphateDisodiumSalt, 12)
                .outputFluids(Water, 1000)
                .cleanroom(2)
                .duration(600).EUt(VA[IV]).save();

        CHEMICAL_RECIPES.builder("sulfonated_hydroquinone")
                .inputItems(TagPrefix.dust, GTOMaterials.HydroquinoneDiacetate, 2)
                .inputFluids(GTMaterials.SulfuricAcid, 1000)
                .outputFluids(GTOMaterials.SulfonatedHydroquinone, 1000)
                .outputFluids(GTMaterials.AceticAcid, 2000)
                .EUt(1920L)
                .duration(200)
                .save();

        CHEMICAL_RECIPES.builder("hydroquinone_diacetate")
                .inputFluids(Hydroquinone, 1000)
                .inputFluids(AceticAcid, 2000)
                .outputItems(dust, HydroquinoneDiacetate, 2)
                .outputFluids(Water, 1000)
                .duration(240).EUt(VA[EV]).save();

        CHEMICAL_RECIPES.builder("z_difluoroethylene")
                .inputFluids(Ethylene, 1000)
                .inputFluids(Fluorine, 2000)
                .outputFluids(ZDifluoroethylene, 1000)
                .outputFluids(HydrofluoricAcid, 2000)
                .circuitMeta(5)
                .duration(200).EUt(VA[HV]).save();
    }

    private static void polymers() {
        REACTION_FURNACE_RECIPES.builder("polyvinyl_alcohol_dust")
                .outputItems(dust, PolyvinylAlcohol, 2)
                .inputFluids(PolyvinylAcetate, 1000)
                .notConsumableFluid(SodiumHydroxideSolution.getFluid(1000))
                .outputFluids(AceticAcid, 1000)
                .EUt(1830L)
                .duration(200)
                .save();

        POLYMERIZATION_REACTOR_RECIPES.builder("polyacrylamide")
                .notConsumable(GTItems.BLACKLIGHT.asItem())
                .outputItems(TagPrefix.dust, GTOMaterials.Polyacrylamide)
                .inputFluids(GTOMaterials.AcrylamideMonomer, 1000)
                .EUt(1920L)
                .duration(300)
                .save();

        POLYMERIZATION_REACTOR_RECIPES.builder("polypyrrole")
                .inputItems(dust, Pyrrole, 2)
                .inputFluids(PeroxyaceticAcid, 1000)
                .outputItems(dust, Polypyrrole, 2)
                .outputFluids(Water, 1000)
                .outputFluids(AceticAcid, 1000)
                .duration(400).EUt(VA[IV]).save();

        POLYMERIZATION_REACTOR_RECIPES.builder("sodium_fluoride_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.Difluorobenzophenone, 24)
                .inputItems(TagPrefix.dust, GTMaterials.SodaAsh, 6)
                .outputItems(TagPrefix.dust, GTOMaterials.SodiumFluoride)
                .inputFluids(GTOMaterials.SulfonatedHydroquinone, 1000)
                .outputFluids(SulfonatedPolyetherEtherKetone, 2592)
                .outputFluids(GTMaterials.CarbonDioxide, 1000)
                .EUt(122880L)
                .duration(250)
                .addCondition(new CleanroomCondition(false, 1))
                .save();

        POLYMERIZATION_REACTOR_RECIPES.builder("polysulfone_dust")
                .outputItems(TagPrefix.dust, GTOMaterials.Polysulfone, 4)
                .inputFluids(GTOMaterials.Hydroquinone, 1000)
                .inputFluids(GTOMaterials.ChlorosulfonicAcid, 1000)
                .outputFluids(GTMaterials.HydrochloricAcid, 1000)
                .EUt(7680L)
                .duration(400)
                .save();

        POLYMERIZATION_REACTOR_RECIPES.builder("polyvinylidene_fluoride_dust")
                .outputItems(TagPrefix.dust, GTOMaterials.PolyvinylideneFluoride, 2)
                .inputFluids(Oxygen, 1000)
                .inputFluids(GTOMaterials.ZDifluoroethylene, 192)
                .circuitMeta(1)
                .EUt(30L)
                .duration(160)
                .save();

        MIXER_RECIPES.builder("polysulfone_polyvinylpyrrolidone_blend")
                .inputItems(dust, Polysulfone, 2)
                .inputFluids(Polyvinylpyrrolidone, 144)
                .outputItems(dust, PolysulfonePolyvinylpyrrolidoneBlend, 3)
                .duration(240).EUt(VA[UV]).save();

        LARGE_CHEMICAL_RECIPES.builder("bpdc_ni_ppy_hydrogel")
                .inputItems(dust, DicarboxyBiphenyl)
                .inputItems(dust, Nickel)
                .inputItems(dust, Polypyrrole)
                .inputFluids(Water, 1000)
                .inputFluids(Dioxane14, 1000)
                .outputItems(dust, BPDCNiPPYHydrogel, 4)
                .outputFluids(Dioxane14, 1000)
                .duration(600).EUt(VA[IV]).save();
    }

    private static void nafion() {
        REACTION_FURNACE_RECIPES.builder("hydrofluoric_acidperfluorooctanesulfonic_acid")
                .inputFluids(Octane, 1000)
                .inputFluids(Fluorine, 36000)
                .inputFluids(ChlorosulfonicAcid, 1000)
                .outputFluids(HydrofluoricAcid, 72000)
                .outputFluids(PerfluorooctanesulfonicAcid, 1000)
                .outputFluids(HydrochloricAcid, 1000)
                .EUt(3600)
                .blastFurnaceTemp(6000)
                .duration(1200)
                .save();

        POLYMERIZATION_REACTOR_RECIPES.builder("perfluorosulfonic_acid_polytetrafluoroethylene_copolymer_dust")
                .inputItems(dust, BenzoylPeroxide)
                .inputItems(dust, BromoSuccinamide)
                .inputFluids(Tetrafluoroethylene, 6000)
                .inputFluids(PerfluorooctanesulfonicAcid, 4000)
                .outputItems(dust, PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer, 10)
                .EUt(72000)
                .duration(1200)
                .save();

        CHEMICAL_BATH_RECIPES.builder("nafion")
                .inputItems(GTOTagPrefix.ION_MEMBRANE, PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer)
                .inputItems(dustSmall, Amprosium)
                .inputFluids(SulfuricAcid, 1000)
                .inputFluids(DistilledWater, 1000)
                .outputItems(GTOItems.NAFION)
                .cleanroom(1)
                .duration(1200).EUt(VA[UV]).save();
    }

    private static void biomaterials() {
        CHEMICAL_BATH_RECIPES.builder("amber_gold_nano_fusilli")
                .inputItems(dust, SodiumCitrate)
                .inputFluids(TetrachloroauricAcidSilverNitratePremixSolution, 1000)
                .chancedInput(Dimethylformamide.getFluid(1000), 1000, 100)
                .outputItems(dust, AmberGoldNanoFusilli, 2)
                .duration(600).EUt(VA[IV]).save();

        MIXER_RECIPES.builder("tetrachloroauric_acid_silver_nitrate_premix_solution")
                .inputFluids(GTOMaterials.SilverNitrateSolution, 1000)
                .inputFluids(GTOMaterials.ChloroauricAcid, 1000)
                .outputFluids(GTOMaterials.TetrachloroauricAcidSilverNitratePremixSolution, 2000)
                .EUt(30L)
                .duration(200)
                .save();

        BIOCHEMICAL_REACTION_RECIPES.builder("metallochaperone_protein")
                .inputItems(dust, Glutamine)
                .inputItems(dust, Copper)
                .inputFluids(AnimalCells, 1000)
                .outputItems(dust, MetallochaperoneProtein, 3)
                .cleanroom(2)
                .duration(600).EUt(VA[IV]).save();

        BIOCHEMICAL_REACTION_RECIPES.builder("osmotic_pump_protein")
                .inputItems(dust, Glutamine)
                .inputItems(dust, AdenosineTriphosphateDisodiumSalt)
                .inputFluids(AnimalCells, 1000)
                .outputItems(dust, OsmoticPumpProtein, 3)
                .cleanroom(2)
                .duration(600).EUt(VA[IV]).save();

        PHYSICAL_VAPOR_DEPOSITION_RECIPES.builder("tritium_nanonet_layer_dust")
                .chancedInput(ChemicalHelper.getItem(TagPrefix.wireFine, GTOMaterials.CarbonNanotubes), 8976, 0)
                .outputItems(TagPrefix.dust, GTOMaterials.TritiumNanonetLayer)
                .inputFluids(GTMaterials.Tritanium, 144)
                .EUt(30000L)
                .duration(200)
                .save();

        MIXER_RECIPES.builder("metallochaperone_biomaterial")
                .inputItems(dust, MetallochaperoneProtein, 2)
                .inputItems(dust, OsmoticPumpProtein)
                .inputItems(dust, TritiumNanonetLayer)
                .inputItems(dust, PolyvinylAlcohol, 2)
                .outputItems(dust, MetallochaperoneBiomaterial, 6)
                .duration(600).EUt(VA[LuV]).save();

        BIOCHEMICAL_REACTION_RECIPES.builder("proton_pump_atp_synthase")
                .inputItems(dust, OsmoticPumpProtein, 2)
                .inputItems(dust, AdenosineTriphosphateDisodiumSalt)
                .outputItems(dust, ProtonPumpATPSynthase, 3)
                .cleanroom(2)
                .duration(800).EUt(VA[LuV]).save();
    }

    private static void membrane() {
        ASSEMBLER_RECIPES.builder("sdbhic_resin_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.SDBHICResin, 16)
                .catalyst(GTOMaterials.RhodiumRheniumNaquadahCatalyst)
                .inputItems(Items.SPONGE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.SDBHICResin)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polypyrrole_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.Polypyrrole, 16)
                .catalyst(GTOMaterials.RhodiumRheniumNaquadahCatalyst)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.Polypyrrole)
                .inputItems(Items.SPONGE)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polyvinylidene_fluoride_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.PolyvinylideneFluoride, 16)
                .inputItems(GTOTagPrefix.CATALYST, Silver, 4)
                .inputItems(Items.SPONGE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.PolyvinylideneFluoride)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168L)
                .duration(600)
                .save();

        ASSEMBLER_RECIPES.builder("polytetrafluoroethylene_ion_membrane")
                .inputItems(TagPrefix.foil, GTMaterials.Polytetrafluoroethylene, 16)
                .catalyst(Nickel)
                .inputItems(Items.SPONGE.asItem())
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTMaterials.Polytetrafluoroethylene)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("graphene_ion_membrane")
                .inputItems(TagPrefix.foil, GTMaterials.Graphene, 16)
                .inputItems(GTOTagPrefix.CATALYST, Platinum, 4)
                .inputItems(Items.SPONGE.asItem())
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTMaterials.Graphene)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polous_polyolefin_sulfonate_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.PolousPolyolefinSulfonate, 16)
                .catalyst(GTOMaterials.TitaniumDioxideNanotubes)
                .inputItems(Items.SPONGE.asItem())
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.PolousPolyolefinSulfonate)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("perfluorosulfonic_acid_polytetrafluoroethylene_copolymer_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.RhodiumRheniumNaquadahCatalyst, 4)
                .inputItems(Items.SPONGE.asItem())
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polyvinyl_alcohol_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.PolyvinylAlcohol, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.NanoScaleTungsten)
                .inputItems(Items.SPONGE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.PolyvinylAlcohol)
                .inputFluids(GTMaterials.Polycaprolactam, 144)
                .EUt(8168L)
                .duration(600)
                .save();

        ASSEMBLER_RECIPES.builder("ce_ox_poly_dopamine_reinforced_polytetrafluoroethylene_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.CeOxPolyDopamineReinforcedPolytetrafluoroethylene, 16)
                .inputItems(GTOTagPrefix.CATALYST, Hafnium)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.CeOxPolyDopamineReinforcedPolytetrafluoroethylene)
                .inputItems(GTOItems.NAFION)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("nanocrack_regulated_self_humidifying_composite_material_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.NanocrackRegulatedSelfHumidifyingCompositeMaterial, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.RhodiumTriphenylphosphineChloride)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.NanocrackRegulatedSelfHumidifyingCompositeMaterial)
                .inputItems(GTOItems.NAFION)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("sulfonated_polyether_ether_ketone_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.SulfonatedPolyetherEtherKetone, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.BisCyclopentadienylHafniumOxide)
                .inputItems(GTOItems.AEROGRAPHENE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.SulfonatedPolyetherEtherKetone)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polyacrylamide_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.Polyacrylamide, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.BisCyclopentadienylHafniumOxide)
                .inputItems(GTOItems.AEROGRAPHENE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.Polyacrylamide)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("boron_oxygen_framework_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.BoronOxygenFramework, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.NanoScaleRhenium)
                .inputItems(GTOItems.NAFION)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.BoronOxygenFramework)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polyimide_ion_membrane")
                .inputItems(GTOTagPrefix.CATALYST, GTMaterials.Copper)
                .inputItems(GTOItems.AEROGRAPHENE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.Polyimide)
                .inputFluids(GTOMaterials.Polyimide, 576)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polybenzimidazole_ion_membrane")
                .inputItems(TagPrefix.foil, GTMaterials.Polybenzimidazole, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.CoAcAbCatalyst)
                .inputItems(GTOItems.AEROGRAPHENE)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTMaterials.Polybenzimidazole)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polysulfone_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.Polysulfone, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.AmberGoldNanoFusilli)
                .inputItems(GTOItems.NAFION)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.Polysulfone)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("polysulfone_polyvinylpyrrolidone_blend_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.PolysulfonePolyvinylpyrrolidoneBlend, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.DMAP)
                .inputItems(GTOItems.NAFION)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.PolysulfonePolyvinylpyrrolidoneBlend)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.builder("bpdc_ni_ppy_hydrogel_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.BPDCNiPPYHydrogel, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.DMAP)
                .inputItems(GTOItems.NAFION)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.BPDCNiPPYHydrogel)
                .EUt(8168L)
                .duration(600)
                .save();
        ASSEMBLER_RECIPES.recipeBuilder("metallochaperone_biomaterial_ion_membrane")
                .inputItems(TagPrefix.foil, GTOMaterials.MetallochaperoneBiomaterial, 16)
                .inputItems(GTOTagPrefix.CATALYST, GTOMaterials.ProtonPumpATPSynthase)
                .inputItems(GTOItems.NAFION)
                .outputItems(GTOTagPrefix.ION_MEMBRANE, GTOMaterials.MetallochaperoneBiomaterial)
                .inputFluids(GTOMaterials.Polyimide, 144)
                .EUt(8168L)
                .duration(600)
                .save();
    }
}
