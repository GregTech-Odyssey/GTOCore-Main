package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOMaterials;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gtolib.api.quest.QuestStyle.*;

public final class RareDustChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("rare_dust", c -> c
            .title("稀土")
            .subtitle("Rare Earth")
            .icon(TagPrefix.dust, GTMaterials.RareEarth)
            .hideQuestsUntilDepsVisible());

    public static final Quest Q_75569B55 = CHAPTER.quest("q_75569b55", q -> q
            .at(-9, -4.5)
            .desc(d -> d
                    .line("本章节仅供流程参考")
                    .line()
                    .line("规则:")
                    .line()
                    .line("六角形:原材料")
                    .line("圆形:主要步骤")
                    .line("菱形:处理副产步骤")
                    .line()
                    .line("齿轮形:最终主要产物")
                    .line("圆角方形:最终副产物")
                    .line()
                    .line("五角形:稀土离心使用")
                    .line()
                    .line("副产步骤不是必须的，{稀土}会产出所有产物，副产步骤仅供想回收副产的人参考。", GOLD))
            .task(Task.checkmark())
            .dependsOn(IvChapter.Q_02C29988)
            .hideUntilDepsComplete().hideDependentLines());

    public static final Quest BASTNASITE_DUST = CHAPTER.quest("bastnasite_dust", q -> q
            .at(-9, 1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Bastnasite))
            .dependsOn(Q_75569B55));

    public static final Quest FLUORO_CARBON_LANTHANIDE_CERIUM_SOLUTION_BUCKET = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_solution_bucket", q -> q
            .at(-7.5, 1)
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(BASTNASITE_DUST));

    public static final Quest STEAM_CRACKED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET = CHAPTER.quest("steam_cracked_fluoro_carbon_lanthanide_slurry_bucket", q -> q
            .at(-6.5, 1)
            .icon(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .dependsOn(FLUORO_CARBON_LANTHANIDE_CERIUM_SOLUTION_BUCKET));

    public static final Quest CERIUM_RICH_MIXTURE_DUST = CHAPTER.quest("cerium_rich_mixture_dust", q -> q
            .at(-3.5, -0.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CeriumRichMixturePowder))
            .dependsOn(BASTNASITE_DUST));

    public static final Quest CONCENTRATED_CERIUM_CHLORIDE_SOLUTION_BUCKET = CHAPTER.quest("concentrated_cerium_chloride_solution_bucket", q -> q
            .at(-3.5, 3.5)
            .icon(GTOMaterials.ConcentratedCeriumChlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.ConcentratedCeriumChlorideSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(CERIUM_RICH_MIXTURE_DUST));

    public static final Quest P507_BUCKET = CHAPTER.quest("p507_bucket", q -> q
            .at(-4, 5)
            .icon(GTOMaterials.P507.getBucket())
            .desc(d -> d
                    .line("UHV以后的稀土离心路线")
                    .line()
                    .line("多了钷粉的产出"))
            .task(Task.item(GTOMaterials.P507.getBucket()).optional(), Task.checkmark())
            .dependsOn(UhvChapter.RARE_EARTH_CENTRIFUGAL, Q_75569B55));

    public static final Quest Q_45B6AB36 = CHAPTER.quest("q_45b6ab36", q -> q
            .at(-3, 5)
            .task(
                    Task.anyOf(1, GTOMaterials.CeriumExtractionNanoResin.getBucket(), GTOMaterials.DysprosiumExtractionNanoResin.getBucket(), GTOMaterials.ErbiumExtractionNanoResin.getBucket(), GTOMaterials.EuropiumExtractionNanoResin.getBucket(), GTOMaterials.GadoliniumExtractionNanoResin.getBucket(), GTOMaterials.HolmiumExtractionNanoResin.getBucket(), GTOMaterials.LanthanumExtractionNanoResin.getBucket(), GTOMaterials.LutetiumExtractionNanoResin.getBucket(), GTOMaterials.NeodymiumExtractionNanoResin.getBucket(), GTOMaterials.PraseodymiumExtractionNanoResin.getBucket(), GTOMaterials.PromethiumExtractionNanoResin.getBucket(), GTOMaterials.SamariumExtractionNanoResin.getBucket(), GTOMaterials.ScandiumExtractionNanoResin.getBucket(), GTOMaterials.TerbiumExtractionNanoResin.getBucket(), GTOMaterials.ThuliumExtractionNanoResin.getBucket(), GTOMaterials.YtterbiumExtractionNanoResin.getBucket(), GTOMaterials.YttriumExtractionNanoResin.getBucket()).title("任意纳米萃取树脂桶"))
            .dependsOn(P507_BUCKET));

    public static final Quest Q_5FE4742B = CHAPTER.quest("q_5fe4742b", q -> q
            .at(1, 5)
            .task(
                    Task.anyOf(1, GTOMaterials.CeriumExtractedNanoResin.getBucket(), GTOMaterials.DysprosiumExtractedNanoResin.getBucket(), GTOMaterials.ErbiumExtractedNanoResin.getBucket(), GTOMaterials.EuropiumExtractedNanoResin.getBucket(), GTOMaterials.GadoliniumExtractedNanoResin.getBucket(), GTOMaterials.HolmiumExtractedNanoResin.getBucket(), GTOMaterials.LanthanumExtractedNanoResin.getBucket(), GTOMaterials.LutetiumExtractedNanoResin.getBucket(), GTOMaterials.NeodymiumExtractedNanoResin.getBucket(), GTOMaterials.PraseodymiumExtractedNanoResin.getBucket(), GTOMaterials.PromethiumExtractedNanoResin.getBucket(), GTOMaterials.SamariumExtractedNanoResin.getBucket(), GTOMaterials.ScandiumExtractedNanoResin.getBucket(), GTOMaterials.TerbiumExtractedNanoResin.getBucket(), GTOMaterials.ThuliumExtractedNanoResin.getBucket(), GTOMaterials.YtterbiumExtractedNanoResin.getBucket(), GTOMaterials.YttriumExtractedNanoResin.getBucket()).title("任意溢满的纳米萃取树脂桶"))
            .dependsOn(Q_45B6AB36));

    public static final Quest MODULATED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET = CHAPTER.quest("modulated_fluoro_carbon_lanthanide_slurry_bucket", q -> q
            .at(-5.5, 1)
            .icon(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .dependsOn(STEAM_CRACKED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET));

    public static final Quest DILUTED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET = CHAPTER.quest("diluted_fluoro_carbon_lanthanide_slurry_bucket", q -> q
            .at(-2, 1)
            .icon(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .dependsOn(MODULATED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET));

    public static final Quest FLUORO_CARBON_LANTHANIDE_CERIUM_OXIDE_DUST = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_oxide_dust", q -> q
            .at(-1, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder))
            .dependsOn(DILUTED_FLUORO_CARBON_LANTHANIDE_SLURRY_BUCKET));

    public static final Quest SAMARIUM_REFINED_DUST = CHAPTER.quest("samarium_refined_dust", q -> q
            .at(0.5, -0.5)
            .desc(d -> d.line("钕矿副产物"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumRefinedPowder))
            .dependsOn(Q_75569B55));

    public static final Quest SAMARIUM_CHLORIDE_CONCENTRATE_SOLUTION_BUCKET = CHAPTER.quest("samarium_chloride_concentrate_solution_bucket", q -> q
            .at(0.5, 3.5)
            .icon(GTOMaterials.SamariumChlorideConcentrateSolution.getBucket())
            .task(Task.item(GTOMaterials.SamariumChlorideConcentrateSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(SAMARIUM_REFINED_DUST));

    public static final Quest CONCENTRATED_RARE_EARTH_CHLORIDE_SOLUTION_BUCKET = CHAPTER.quest("concentrated_rare_earth_chloride_solution_bucket", q -> q
            .at(-2, 5)
            .icon(GTOMaterials.ConcentratedRareEarthChlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.ConcentratedRareEarthChlorideSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(SAMARIUM_CHLORIDE_CONCENTRATE_SOLUTION_BUCKET, CONCENTRATED_CERIUM_CHLORIDE_SOLUTION_BUCKET));

    public static final Quest ENRICHED_RARE_EARTH_CHLORIDE_SOLUTION_BUCKET = CHAPTER.quest("enriched_rare_earth_chloride_solution_bucket", q -> q
            .at(-1, 5)
            .icon(GTOMaterials.EnrichedRareEarthChlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.EnrichedRareEarthChlorideSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(CONCENTRATED_RARE_EARTH_CHLORIDE_SOLUTION_BUCKET));

    public static final Quest DILUTED_RARE_EARTH_CHLORIDE_SOLUTION_BUCKET = CHAPTER.quest("diluted_rare_earth_chloride_solution_bucket", q -> q
            .at(0, 5)
            .icon(GTOMaterials.DilutedRareEarthChlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.DilutedRareEarthChlorideSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(ENRICHED_RARE_EARTH_CHLORIDE_SOLUTION_BUCKET));

    public static final Quest SAMARIUM_RARE_EARTH_TURBID_LIQUID_BUCKET = CHAPTER.quest("samarium_rare_earth_turbid_liquid_bucket", q -> q
            .at(-1, 0)
            .icon(GTOMaterials.SamariumRrareEearthTurbidLiquid.getBucket())
            .task(Task.item(GTOMaterials.SamariumRrareEearthTurbidLiquid.getBucket()).optional(), Task.checkmark())
            .dependsOn(SAMARIUM_REFINED_DUST));

    public static final Quest CERIUM_RICH_MIXTURE_DUST_2 = CHAPTER.quest("cerium_rich_mixture_dust_2", q -> q
            .at(-2.5, -0.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CeriumRichMixturePowder))
            .dependsOn(SAMARIUM_RARE_EARTH_TURBID_LIQUID_BUCKET));

    public static final Quest CERIUM_OXIDE_DUST = CHAPTER.quest("cerium_oxide_dust", q -> q
            .at(-1, -1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CeriumOxide))
            .dependsOn(SAMARIUM_RARE_EARTH_TURBID_LIQUID_BUCKET));

    public static final Quest FLUORO_CARBON_LANTHANIDE_CERIUM_RARE_EARTH_SUSPENSION_BUCKET = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_rare_earth_suspension_bucket", q -> q
            .at(1.5, 1)
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket()).optional(), Task.checkmark())
            .dependsOn(FLUORO_CARBON_LANTHANIDE_CERIUM_OXIDE_DUST));

    public static final Quest NEODYMIUM_RARE_EARTH_CONCENTRATE_DUST = CHAPTER.quest("neodymium_rare_earth_concentrate_dust", q -> q
            .at(2.5, -0.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NeodymiumRareEarthConcentratePowder))
            .dependsOn(FLUORO_CARBON_LANTHANIDE_CERIUM_RARE_EARTH_SUSPENSION_BUCKET));

    public static final Quest SAMARIUM_RARE_EARTH_CONCENTRATE_DUST = CHAPTER.quest("samarium_rare_earth_concentrate_dust", q -> q
            .at(2.5, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder))
            .dependsOn(FLUORO_CARBON_LANTHANIDE_CERIUM_RARE_EARTH_SUSPENSION_BUCKET));

    public static final Quest RARE_EARTH_DUST = CHAPTER.quest("rare_earth_dust", q -> q
            .at(3.75, 2.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth))
            .dependsOn(SAMARIUM_RARE_EARTH_CONCENTRATE_DUST)
            .requireAnyStarted());

    public static final Quest NEODYMIUM_OXIDE_DUST = CHAPTER.quest("neodymium_oxide_dust", q -> q
            .at(3.5, 0)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NeodymiumOxide))
            .dependsOn(NEODYMIUM_RARE_EARTH_CONCENTRATE_DUST));

    public static final Quest LANTHANUM_CHLORIDE_DUST = CHAPTER.quest("lanthanum_chloride_dust", q -> q
            .at(3.5, -1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.LanthanumChloride))
            .dependsOn(NEODYMIUM_RARE_EARTH_CONCENTRATE_DUST));

    public static final Quest FLUORINATED_SAMARIUM_CONCENTRATE_DUST = CHAPTER.quest("fluorinated_samarium_concentrate_dust", q -> q
            .at(5, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder))
            .dependsOn(SAMARIUM_RARE_EARTH_CONCENTRATE_DUST));

    public static final Quest HOLMIUM_DUST = CHAPTER.quest("holmium_dust", q -> q
            .at(6, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Holmium))
            .dependsOn(FLUORINATED_SAMARIUM_CONCENTRATE_DUST));

    public static final Quest SAMARIUM_TERBIUM_MIXTURE_DUST = CHAPTER.quest("samarium_terbium_mixture_dust", q -> q
            .at(6, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumTerbiumMixturePowder))
            .dependsOn(FLUORINATED_SAMARIUM_CONCENTRATE_DUST));

    public static final Quest NITRIDED_SAMARIUM_TERBIUM_MIXTURE_DUST = CHAPTER.quest("nitrided_samarium_terbium_mixture_dust", q -> q
            .at(7, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NitridedSamariumTerbiumMixturePowder))
            .dependsOn(SAMARIUM_TERBIUM_MIXTURE_DUST));

    public static final Quest TERBIUM_NITRATE_DUST = CHAPTER.quest("terbium_nitrate_dust", q -> q
            .at(8, 1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.TerbiumNitratePowder))
            .dependsOn(NITRIDED_SAMARIUM_TERBIUM_MIXTURE_DUST));

    public static final Quest TERBIUM_DUST = CHAPTER.quest("terbium_dust", q -> q
            .at(9, 1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Terbium))
            .dependsOn(TERBIUM_NITRATE_DUST));

    public static final Quest SAMARIUM_PRECIPITATE_DUST = CHAPTER.quest("samarium_precipitate_dust", q -> q
            .at(8, -1)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder))
            .dependsOn(NITRIDED_SAMARIUM_TERBIUM_MIXTURE_DUST));

    public static final Quest MONAZITE_DUST = CHAPTER.quest("monazite_dust", q -> q
            .at(-9, -2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Monazite))
            .dependsOn(Q_75569B55));

    public static final Quest MONAZITE_RARE_EARTH_TURBID_LIQUID_BUCKET = CHAPTER.quest("monazite_rare_earth_turbid_liquid_bucket", q -> q
            .at(-7.5, -2)
            .icon(GTOMaterials.MonaziteRareEarthTurbidLiquid.getBucket())
            .task(Task.item(GTOMaterials.MonaziteRareEarthTurbidLiquid.getBucket()).optional(), Task.checkmark())
            .dependsOn(MONAZITE_DUST));

    public static final Quest DILUTED_MONAZITE_SLURRY_BUCKET = CHAPTER.quest("diluted_monazite_slurry_bucket", q -> q
            .at(-6.5, -2)
            .icon(GTOMaterials.DilutedMonaziteSlurry.getBucket())
            .task(Task.item(GTOMaterials.DilutedMonaziteSlurry.getBucket()).optional(), Task.checkmark())
            .dependsOn(MONAZITE_RARE_EARTH_TURBID_LIQUID_BUCKET));

    public static final Quest ACIDIC_MONAZITE_DUST = CHAPTER.quest("acidic_monazite_dust", q -> q
            .at(-5.5, -2)
            .task(Task.item(TagPrefix.dust, GTOMaterials.AcidicMonazitePowder))
            .dependsOn(DILUTED_MONAZITE_SLURRY_BUCKET));

    public static final Quest THORIUM_PHOSPHATE_REFINED_DUST = CHAPTER.quest("thorium_phosphate_refined_dust", q -> q
            .at(-5.5, -4)
            .task(Task.item(TagPrefix.dust, GTOMaterials.ThoriumPhosphateRefinedPowder))
            .dependsOn(ACIDIC_MONAZITE_DUST)
            .requireAny()
            .repeatable(false));

    public static final Quest CONCENTRATED_MONAZITE_RARE_EARTH_HYDROXIDE_DUST = CHAPTER.quest("concentrated_monazite_rare_earth_hydroxide_dust", q -> q
            .at(-3.5, -2)
            .task(Task.item(TagPrefix.dust, GTOMaterials.ConcentratedMonaziteRareEarthHydroxidePowder))
            .dependsOn(ACIDIC_MONAZITE_DUST));

    public static final Quest NITRIC_LEACHATE_FROM_MONAZITE_BUCKET = CHAPTER.quest("nitric_leachate_from_monazite_bucket", q -> q
            .at(-1.5, -2)
            .icon(GTOMaterials.NitricLeachateFromMonazite.getBucket())
            .desc(d -> d.line("增产100%"))
            .task(Task.item(GTOMaterials.NitricLeachateFromMonazite.getBucket(), 2).optional(), Task.checkmark())
            .dependsOn(CERIUM_RICH_MIXTURE_DUST, CONCENTRATED_MONAZITE_RARE_EARTH_HYDROXIDE_DUST));

    public static final Quest URANIUM_FILTER_RESIDUE_DUST = CHAPTER.quest("uranium_filter_residue_dust", q -> q
            .at(-4.5, -3)
            .task(Task.item(TagPrefix.dust, GTOMaterials.UraniumFilterResiduePowder))
            .dependsOn(ACIDIC_MONAZITE_DUST));

    public static final Quest URANIUM_235_DUST = CHAPTER.quest("uranium_235_dust", q -> q
            .at(-4.5, -4)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium235))
            .dependsOn(URANIUM_FILTER_RESIDUE_DUST)
            .requireAny());

    public static final Quest NITRIC_LEACHATE_FROM_MONAZITE_BUCKET_2 = CHAPTER.quest("nitric_leachate_from_monazite_bucket_2", q -> q
            .at(-1.5, -3)
            .icon(GTOMaterials.NitricLeachateFromMonazite.getBucket())
            .task(Task.item(GTOMaterials.NitricLeachateFromMonazite.getBucket()).optional(), Task.checkmark())
            .dependsOn(CONCENTRATED_MONAZITE_RARE_EARTH_HYDROXIDE_DUST));

    public static final Quest EUROPIUM_OXIDE_DUST = CHAPTER.quest("europium_oxide_dust", q -> q
            .at(-2, -4)
            .task(Task.item(TagPrefix.dust, GTOMaterials.EuropiumOxide))
            .dependsOn(NITRIC_LEACHATE_FROM_MONAZITE_BUCKET_2));

    public static final Quest MONAZITE_RARE_EARTH_PRECIPITATE_DUST = CHAPTER.quest("monazite_rare_earth_precipitate_dust", q -> q
            .at(0.5, -2)
            .task(Task.item(TagPrefix.dust, GTOMaterials.MonaziteRareEarthPrecipitatePowder))
            .dependsOn(NITRIC_LEACHATE_FROM_MONAZITE_BUCKET, NITRIC_LEACHATE_FROM_MONAZITE_BUCKET_2));

    public static final Quest SATURATED_MONAZITE_RARE_EARTH_DUST = CHAPTER.quest("saturated_monazite_rare_earth_dust", q -> q
            .at(2.5, -3)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SaturatedMonaziteRareEarthPowder, 2))
            .dependsOn(MONAZITE_RARE_EARTH_PRECIPITATE_DUST));

    public static final Quest SATURATED_MONAZITE_RARE_EARTH_DUST_2 = CHAPTER.quest("saturated_monazite_rare_earth_dust_2", q -> q
            .at(2.5, -2)
            .desc(d -> d.line("增产50%"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.SaturatedMonaziteRareEarthPowder, 3))
            .dependsOn(SAMARIUM_REFINED_DUST, MONAZITE_RARE_EARTH_PRECIPITATE_DUST));

    public static final Quest SAMARIUM_PRECIPITATE_DUST_2 = CHAPTER.quest("samarium_precipitate_dust_2", q -> q
            .at(8, -2)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder))
            .dependsOn(SATURATED_MONAZITE_RARE_EARTH_DUST_2));

    public static final Quest SAMARIUM_DUST = CHAPTER.quest("samarium_dust", q -> q
            .at(9, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Samarium))
            .dependsOn(SAMARIUM_PRECIPITATE_DUST_2, SAMARIUM_PRECIPITATE_DUST));

    public static final Quest GADOLINIUM_DUST = CHAPTER.quest("gadolinium_dust", q -> q
            .at(9, -2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Gadolinium))
            .dependsOn(SAMARIUM_PRECIPITATE_DUST_2, SAMARIUM_PRECIPITATE_DUST));

    public static final Quest MONAZITE_DUST_2 = CHAPTER.quest("monazite_dust_2", q -> q
            .at(-9, -2).iconScale(0.1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Monazite))
            .dependsOn(THORIUM_PHOSPHATE_REFINED_DUST));

    public static final Quest RARE_EARTH_DUST_2 = CHAPTER.quest("rare_earth_dust_2", q -> q
            .at(3.75, -3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth))
            .dependsOn(SATURATED_MONAZITE_RARE_EARTH_DUST_2, SATURATED_MONAZITE_RARE_EARTH_DUST)
            .requireAnyStarted());

    public static final Quest CERIUM_OXIDE_DUST_2 = CHAPTER.quest("cerium_oxide_dust_2", q -> q
            .at(-1, -4)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CeriumOxide))
            .dependsOn(NITRIC_LEACHATE_FROM_MONAZITE_BUCKET_2));

    public static final Quest RARE_EARTH_DUST_3 = CHAPTER.quest("rare_earth_dust_3", q -> q
            .at(3, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth))
            .dependsOn(Q_75569B55)
            .requireAnyStarted());

    public static final Quest RARE_EARTH_HYDROXIDES_BUCKET = CHAPTER.quest("rare_earth_hydroxides_bucket", q -> q
            .at(4.5, 5)
            .icon(GTOMaterials.RareEarthHydroxides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthHydroxides.getBucket()).optional(), Task.checkmark())
            .dependsOn(RARE_EARTH_DUST_3));

    public static final Quest RARE_EARTH_CHLORIDES_BUCKET = CHAPTER.quest("rare_earth_chlorides_bucket", q -> q
            .at(6, 5)
            .icon(GTOMaterials.RareEarthChlorides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthChlorides.getBucket()).optional(), Task.checkmark())
            .dependsOn(RARE_EARTH_HYDROXIDES_BUCKET));

    public static final Quest Q_771071BC = CHAPTER.quest("q_771071bc", q -> q
            .at(7.5, 5)
            .task(
                    Task.anyOf(1, GTOMaterials.ErLuOxidesSolution.getBucket(), GTOMaterials.LaNdOxidesSolution.getBucket(), GTOMaterials.SmGdOxidesSolution.getBucket(), GTOMaterials.TbHoOxidesSolution.getBucket()).title("任意氧化物溶液桶"))
            .dependsOn(RARE_EARTH_CHLORIDES_BUCKET));

    public static final Quest Q_06352042 = CHAPTER.quest("q_06352042", q -> q
            .at(9, 5)
            .desc(d -> d.line("钷粉不在这里"))
            .task(
                    Task.anyOf(1, ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.CeriumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.DysprosiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.ErbiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.EuropiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.GadoliniumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.HolmiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.LanthanumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.LutetiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.NeodymiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.PraseodymiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.SamariumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.ScandiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.TerbiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.ThuliumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.YtterbiumOxide), ChemicalHelper.getItem(TagPrefix.dust, GTOMaterials.YttriumOxide)).title("任意稀土氧化物粉"))
            .dependsOn(Q_771071BC));

    public static final Quest RARE_EARTH_OXIDE_DUST = CHAPTER.quest("rare_earth_oxide_dust", q -> q
            .at(-9.3, 4.5)
            .desc(d -> d
                    .line("UHV以后的稀土离心路线")
                    .line()
                    .line("多了钷粉的产出"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.RareEarthOxide))
            .dependsOn(RARE_EARTH_CHLORIDES_BUCKET)
            .hideDependencyLines());

    public static final Quest RARE_EARTH_METAL_ORE = CHAPTER.quest("rare_earth_metal_ore", q -> q
            .at(-9.3, 5.5)
            .desc(d -> d.line("从太空无人机4开始获取"))
            .task(Task.item(TagPrefix.ore, GTOMaterials.RareEarthMetal))
            .dependsOn(Q_75569B55));

    public static final Quest RARE_EARTH_METAL_DUST = CHAPTER.quest("rare_earth_metal_dust", q -> q
            .at(-7.8, 5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RareEarthMetal))
            .dependsOn(RARE_EARTH_METAL_ORE, RARE_EARTH_OXIDE_DUST));

    public static final Quest Q_1B3B842E = CHAPTER.quest("q_1b3b842e", q -> q
            .at(-6.3, 5)
            .task(
                    Task.anyOf(1, ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Cerium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Dysprosium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Erbium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Europium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Gadolinium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Holmium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Lanthanum), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Lutetium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Neodymium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Praseodymium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Promethium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Samarium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Scandium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Terbium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Thulium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Ytterbium), ChemicalHelper.getItem(TagPrefix.dustSmall, GTMaterials.Yttrium)).title("任意稀土小堆粉"))
            .dependsOn(RARE_EARTH_METAL_DUST));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UHV, -4.7, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UHV, -10, 4.5, 1, 1)));
    }
}
