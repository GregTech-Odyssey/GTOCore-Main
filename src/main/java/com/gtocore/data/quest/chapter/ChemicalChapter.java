package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class ChemicalChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("chemical", c -> c
            .title("化学工程")
            .subtitle("Chemical Engineering")
            .icon(GTMultiMachines.DISTILLATION_TOWER));

    public static final Quest ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS = CHAPTER.quest("another_way_to_obtain_chemical_materials", q -> q
            .at(-10.5, -7)
            .icon(GTBlocks.RUBBER_LOG)
            .title("另外一种化工材料获取途径")
            .subtitle("生物化工处理线路")
            .desc(d -> d
                    .line("通过热解原木，你可以蒸出木炭副产物，再将其蒸馏获得木醋酸， 木煤气，木焦油进行再蒸馏")
                    .line()
                    .line("而多出来的木炭则再进行热解操作，煤焦油再蒸馏即可")
                    .line()
                    .line("其他线路的走法也可以，只是这条线比较全"))
            .task(
                    Task.item(GTMultiMachines.PYROLYSE_OVEN),
                    Task.item(GTMaterials.CharcoalByproducts.getBucket()),
                    Task.item(GTMaterials.CoalTar.getBucket())));

    public static final Quest COLLECTION_AND_CENTRIFUGATION_OF_RARE_GASES = CHAPTER.quest("collection_and_centrifugation_of_rare_gases", q -> q
            .at(-8, -8.5)
            .icon(MultiBlockA.LARGE_GAS_COLLECTOR)
            .title("稀有气体的收集和离心")
            .subtitle("终于可以快速烧锭了")
            .desc(d -> d
                    .line("集气室收集的气体大部分都可以被液化再蒸馏，能走液化蒸馏线路就走")
                    .line()
                    .line("前期急需的{氮气}和{氧气}可以进行空气离心收集", GOLD, GOLD)
                    .line()
                    .line("空气也是内燃机和燃气机的常客，走燃油神教和大苯环燃气神教的准备好"))
            .task(Task.item(GTMaterials.Air.getBucket()).optional(), Task.checkmark())
            .dependsOn(LvChapter.LV_ELECTRIC_PUMP));

    public static final Quest A_SULFURIC_SMELL_AM_I_IN = CHAPTER.quest("a_sulfuric_smell_am_i_in", q -> q
            .at(-6.5, -8.5)
            .icon(GTMachines.GAS_COLLECTOR[GTValues.MV])
            .title("一股硫磺味,我在硫矿脉么？")
            .desc(d -> d.line("如果不是急需下界空气离心产物，建议是直奔液化蒸馏，同时能补充大量化工材料"))
            .task(Task.item(GTMaterials.NetherAir.getBucket()).optional(), Task.checkmark())
            .dependsOn(COLLECTION_AND_CENTRIFUGATION_OF_RARE_GASES, HvChapter.VISIT_NETHER));

    public static final Quest THE_AIR_IN_THE_END_IS = CHAPTER.quest("the_air_in_the_end_is", q -> q
            .at(-5, -8.5)
            .icon(GTMachines.GAS_COLLECTOR[GTValues.HV])
            .title("末地的空气真好闻，你问我的眼睛为什么是紫色的？")
            .desc(d -> d
                    .line("{空气液化蒸馏产物}能够提供{大量种类的稀有气体}", GOLD, GOLD)
                    .line()
                    .line("至此，你的高炉再也不缺稀有气体供应"))
            .task(Task.item(GTMaterials.EnderAir.getBucket()).optional(), Task.checkmark())
            .dependsOn(A_SULFURIC_SMELL_AM_I_IN));

    public static final Quest WANT_TO_GET_IT_ALL_IN = CHAPTER.quest("want_to_get_it_all_in", q -> q
            .at(-3.5, -8.5)
            .title("想直接一步到位？")
            .desc(d -> d.line("放入{维度数据}和{真空冷冻机}即可产出{液态空气}", GOLD, GOLD, GOLD))
            .task(Task.item(MultiBlockA.LARGE_GAS_COLLECTOR))
            .dependsOn(THE_AIR_IN_THE_END_IS));

    public static final Quest PLANT_ROUTE = CHAPTER.quest("plant_route", q -> q
            .at(-6.5, -7)
            .icon(GTItems.BIO_CHAFF)
            .title("植物路线")
            .desc(d -> d
                    .line("通过将农作物或者其他植物压缩粉碎得出的生物糠，将其热解再蒸馏将得出不错的产物，也是乙醇的主要获取点")
                    .line()
                    .line("虽然烧树木线路也能获得这些产物，但相应产物产出率还是不同的"))
            .task(Task.item(GTItems.BIO_CHAFF), Task.item(GTMaterials.FermentedBiomass.getBucket()))
            .dependsOn(ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS));

    public static final Quest IT_S_TIME_TO_SAY_GOODBYE = CHAPTER.quest("it_s_time_to_say_goodbye", q -> q
            .at(-5, -7)
            .title("是时候告别冗长的产线了")
            .desc(d -> d.line("集成木化工厂能将木头的一生完美吃干抹净，让其成为光荣的格雷材料一员"))
            .task(Task.item(MultiBlockB.WOOD_DISTILLATION))
            .dependsOn(PLANT_ROUTE));

    public static final Quest BIO_DIESEL_MANUFACTURING_ROUTE = CHAPTER.quest("bio_diesel_manufacturing_route", q -> q
            .at(-8, -5.5)
            .icon(GTMaterials.FishOil.getBucket())
            .title("生物柴油制作路线")
            .desc(d -> d
                    .line("搭配木化路线的产物乙醇可以产出{生物柴油}", GOLD)
                    .line()
                    .line("{当然，你也可以将其制作成高十六燃油}", GOLD))
            .task(Task.item(GTMaterials.FishOil.getBucket()).optional(), Task.checkmark())
            .dependsOn(ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS)
            .hideDependencyLines());

    public static final Quest BIO_DIESEL_BUCKET = CHAPTER.quest("bio_diesel_bucket", q -> q
            .at(-6.5, -5.5)
            .icon(GTMaterials.BioDiesel.getBucket())
            .task(Task.item(GTMaterials.BioDiesel.getBucket()).optional(), Task.checkmark())
            .dependsOn(BIO_DIESEL_MANUFACTURING_ROUTE)
            .hideDependencyLines());

    public static final Quest ENCOURAGE_PEOPLE_TO_STUDY_CHEMICAL_MATERIALS = CHAPTER.quest("encourage_people_to_study_chemical_materials", q -> q
            .at(-8, -11)
            .title("劝人学生化环材--XXXX")
            .desc(d -> d
                    .line("本章节主要是为了介绍化工材料获取和合成细节，以及和原版GregTech流程不同的地方")
                    .line()
                    .line("建议HV之后再来查阅，因为你需要蒸馏塔"))
            .task(Task.checkmark().title("化工部分")));

    public static final Quest PETROLEUM_PROCESSING = CHAPTER.quest("petroleum_processing", q -> q
            .at(-14, -4.5)
            .icon(GTMaterials.RawOil.getBucket())
            .title("石化处理")
            .subtitle("HV阶段——你逃不开的")
            .desc(d -> d.line("多准备点蒸馏塔，不锈钢也是，多烧，燃起来！"))
            .task(
                    Task.item(GTMaterials.RawOil.getBucket()).optional(),
                    Task.item(GTMaterials.OilHeavy.getBucket()).optional(),
                    Task.item(GTMaterials.OilLight.getBucket()).optional(),
                    Task.item(ModItems.OIL_BUCKET.get()).optional(),
                    Task.checkmark()));

    public static final Quest WHERE_IS_MY_SULFUR_DUST = CHAPTER.quest("where_is_my_sulfur_dust", q -> q
            .at(-14, -5.5)
            .icon(MultiBlockB.DESULFURIZER)
            .title("我的硫粉呢？")
            .desc(d -> d.line("如果你需要从石化获得{硫粉}将需要{脱硫机}", GOLD, GOLD))
            .task(Task.item(MultiBlockB.DESULFURIZER), Task.item(TagPrefix.dust, GTMaterials.Sulfur))
            .dependsOn(PETROLEUM_PROCESSING));

    public static final Quest STEAM_CRACKING_AND_DISTILLATION_QUITE_COMMON = CHAPTER.quest("steam_cracking_and_distillation_quite_common", q -> q
            .at(-14, -6.5)
            .icon(GTMultiMachines.CRACKER)
            .title("蒸汽裂化，然后蒸馏，很稀疏平常")
            .desc(d -> d
                    .line("脱硫后产物可以选择生产柴油，或者进行蒸汽裂化蒸馏")
                    .line()
                    .line("蒸汽除了锅炉烧，也可以通过流体加热器获得，后者是后面高电压情况下获得蒸汽最快的法子"))
            .task(Task.item(GTMultiMachines.CRACKER), Task.item(GTMaterials.Steam.getBucket()))
            .dependsOn(WHERE_IS_MY_SULFUR_DUST));

    public static final Quest HIGH_OCTANE_16_THE_FUEL_FAVORED = CHAPTER.quest("high_octane_16_the_fuel_favored", q -> q
            .at(-3.5, -4)
            .icon(GTMaterials.CetaneBoostedDiesel.getBucket())
            .title("高十六，燃油神教优选.")
            .subtitle("耐烧小王")
            .desc(d -> d
                    .line("生物柴油或者柴油加硝基甲烷就可以出高十六，硝基甲烷走{木化线路}很容易获得其合成的化学材料", RED)
                    .line()
                    .line("高十六的一桶能产出1440000EU"))
            .task(
                    Task.item(GTMaterials.Diesel.getBucket()),
                    Task.item(GTMaterials.Tetranitromethane.getBucket()),
                    Task.item(GTMaterials.CetaneBoostedDiesel.getBucket()))
            .dependsOn(ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS, STEAM_CRACKING_AND_DISTILLATION_QUITE_COMMON)
            .hideDependencyLines());

    public static final Quest THE_ULTIMATE_OF_FUEL_CULT = CHAPTER.quest("the_ultimate_of_fuel_cult", q -> q
            .at(-2, -4)
            .title("燃油神教的极致？")
            .subtitle("真正的耐烧王")
            .desc(d -> d
                    .line("辛烷可以从加氢裂化的轻燃油中获取")
                    .line()
                    .line("高辛烷一桶可以产生4800000EU，足有高十六的3.4倍，但产品线也更加复杂！"))
            .task(Task.item(GTMaterials.HighOctaneGasoline.getBucket()), Task.item(GTMaterials.Octane.getBucket()))
            .dependsOn(HIGH_OCTANE_16_THE_FUEL_FAVORED));

    public static final Quest EMBRACE_THE_GREAT_BENZENE_CYCLE_GOD = CHAPTER.quest("embrace_the_great_benzene_cycle_god", q -> q
            .at(-5, -5.5)
            .icon(GTMaterials.Benzene.getBucket())
            .title("迎接大苯环神教的怀抱吧")
            .desc(d -> d
                    .line("燃气发电线路的优选材料，也就这玩意好做了")
                    .line()
                    .line("燃气相较于燃油燃值低，但获取的简便弥补了这点")
                    .line()
                    .line("一桶苯产出352000EU（高十六是其四倍）"))
            .task(Task.item(GTMaterials.Benzene.getBucket()).optional(), Task.checkmark())
            .dependsOn(ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS)
            .hideDependencyLines());

    public static final Quest ROCKET_FUEL_LINE = CHAPTER.quest("rocket_fuel_line", q -> q
            .at(-10.5, -2.5)
            .icon(ModItems.TIER_1_ROCKET.get())
            .title("火箭燃料线路")
            .desc(d -> d
                    .line()
                    .line("在完成木化线路之后，该燃料是你在HV能接触的火箭燃料"))
            .task(Task.item(GTMaterials.RocketFuel.getBucket()).optional(), Task.checkmark())
            .dependsOn(ANOTHER_WAY_TO_OBTAIN_CHEMICAL_MATERIALS));

    public static final Quest RECOMMENDED_FUEL_SYNTHESIS_GENERATION_LINE = CHAPTER.quest("recommended_fuel_synthesis_generation_line", q -> q
            .at(-8, -1)
            .icon(GTOMaterials.RocketFuelRp1.getBucket())
            .title("火箭燃料发电线路推荐")
            .desc(d -> d
                    .line("EV之后就能做的火箭燃料rp-1，其只需要完成木化路线就能简单的合成的特性使得火箭燃料在EV之后的发电有着其一席之地")
                    .line()
                    .line("一桶为2048000EU，是高十六的1.4倍")
                    .line()
                    .line("{可作为二阶及其以下火箭燃料}", YELLOW))
            .task(Task.item(GTOMaterials.RocketFuelRp1.getBucket()).optional(), Task.checkmark())
            .dependsOn(ROCKET_FUEL_LINE));

    public static final Quest SOLVED_THE_PROBLEM_OF_RP_1 = CHAPTER.quest("solved_the_problem_of_rp_1", q -> q
            .at(-6.5, -1)
            .icon(GTOMaterials.RocketFuelH8n4c2o4.getBucket())
            .title("解决了rp-1不耐烧的问题")
            .desc(d -> d
                    .line("一桶是rp-1的四倍.  8192000EU")
                    .line()
                    .line("也是只要完成木化线路就能简易合成出来的燃料"))
            .task(Task.item(GTOMaterials.RocketFuelH8n4c2o4.getBucket()).optional(), Task.checkmark())
            .dependsOn(RECOMMENDED_FUEL_SYNTHESIS_GENERATION_LINE));

    public static final Quest DENSE_HYDRAZINE_FUEL_MIXTURE_BUCKET = CHAPTER.quest("dense_hydrazine_fuel_mixture_bucket", q -> q
            .at(-8, -2.5)
            .icon(GTOMaterials.DenseHydrazineFuelMixture.getBucket())
            .desc(d -> d
                    .line("较为复杂的合成步骤，不建议拿来烧电，仅用于火箭燃料装载")
                    .line()
                    .line("{可作为三阶及其以下火箭燃料}", YELLOW))
            .task(Task.item(GTOMaterials.DenseHydrazineFuelMixture.getBucket()).optional(), Task.checkmark())
            .dependsOn(ROCKET_FUEL_LINE));

    public static final Quest ROCKET_FUEL_CN3H7O3_BUCKET = CHAPTER.quest("rocket_fuel_cn3h7o3_bucket", q -> q
            .at(-6.5, -2.5)
            .icon(GTOMaterials.RocketFuelCn3h7o3.getBucket())
            .desc(d -> d
                    .line("混合燃料上位替代，但依旧因为肼的合成麻烦，不推荐其作为发电手段使用.")
                    .line()
                    .line("{可作为四阶及其以下火箭燃料}", YELLOW))
            .task(Task.item(GTOMaterials.RocketFuelCn3h7o3.getBucket()).optional(), Task.checkmark())
            .dependsOn(DENSE_HYDRAZINE_FUEL_MIXTURE_BUCKET));

    public static final Quest EXPLOSIVEHYDRAZINE_BUCKET = CHAPTER.quest("explosivehydrazine_bucket", q -> q
            .at(-5, -2.5)
            .icon(GTOMaterials.ExplosiveHydrazine.getBucket())
            .desc(d -> d.line("{可作为五阶及其以下火箭燃料}", YELLOW))
            .task(Task.item(GTOMaterials.ExplosiveHydrazine.getBucket()).optional(), Task.checkmark())
            .dependsOn(ROCKET_FUEL_CN3H7O3_BUCKET));

    public static final Quest FASTER_COOLING_ABILITY_THAN_A_SNOW = CHAPTER.quest("faster_cooling_ability_than_a_snow", q -> q
            .at(-3.5, -2.5)
            .icon(ModItems.CRYO_FUEL_BUCKET.get())
            .title("拥有着比细雪桶更快的降温本事")
            .desc(d -> d
                    .line("其极为寒冷的特性让他十分适合作为培养皿的常客")
                    .line()
                    .line("多用于生物电路合成线")
                    .line()
                    .line("{可作为六阶及其以下火箭燃料}", YELLOW))
            .task(Task.item(ModItems.CRYO_FUEL_BUCKET.get()).optional(), Task.checkmark())
            .dependsOn(HIGH_OCTANE_16_THE_FUEL_FAVORED, EXPLOSIVEHYDRAZINE_BUCKET));

    public static final Quest THE_PINNACLE_OF_ROCKET_FUEL = CHAPTER.quest("the_pinnacle_of_rocket_fuel", q -> q
            .at(-2, -2.5)
            .icon(GTOMaterials.StellarEnergyRocketFuel.getBucket())
            .title("火箭燃料的巅峰")
            .desc(d -> d
                    .line("大后期作为获取黑/白矮星物质的手段之一")
                    .line()
                    .line("{可作为所有火箭的燃料}", YELLOW))
            .task(Task.item(GTOMaterials.StellarEnergyRocketFuel.getBucket()).optional(), Task.checkmark())
            .dependsOn(FASTER_COOLING_ABILITY_THAN_A_SNOW));

    public static final Quest SIMPLIFIED_FUEL_SYNTHESIS = CHAPTER.quest("simplified_fuel_synthesis", q -> q
            .at(-0.5, -2.5)
            .title("更加简化的燃料合成")
            .desc(d -> d.line("终于可以抛弃那冗长复杂的火箭燃料合成"))
            .task(Task.item(MultiBlockB.FUEL_REFINING_COMPLEX))
            .dependsOn(THE_PINNACLE_OF_ROCKET_FUEL));

    public static final Quest WHY_CAN_T_MY_PLASTIC_BE = CHAPTER.quest("why_can_t_my_plastic_be", q -> q
            .at(-10.5, 1)
            .title("我的塑料怎么不能用化反合成了？")
            .desc(d -> d
                    .line("当你熟练的准备好聚四氟乙烯的材料，准备在化反合成的时候，发现无法合成，你终于发现配方变成聚合反应器的配方了")
                    .line()
                    .line("前期的简单塑料可以通过化反合成，其他高级塑料将采用聚合反应器进行合成，同时还有其他特定的化学材料")
                    .line()
                    .line("这里将简单介绍这部分的材料"))
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .dependsOn(HvChapter.MICROPROCESSOR_SUPERCOMPUTER));

    public static final Quest POLYETHYLENE_BUCKET = CHAPTER.quest("polyethylene_bucket", q -> q
            .at(-8, 1)
            .icon(GTMaterials.Polyethylene.getBucket())
            .subtitle("早期的塑料")
            .desc(d -> d
                    .line("{LV和MV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("多用于芯片材料和总成的合成"))
            .task(Task.item(GTMaterials.Polyethylene.getBucket()).optional(), Task.checkmark())
            .dependsOn(UlvChapter.C2H4_PRONOUNCED_ETHYLENE));

    public static final Quest POLYVINYL_CHLORIDE_BUCKET = CHAPTER.quest("polyvinyl_chloride_bucket", q -> q
            .at(-6.5, 1)
            .icon(GTMaterials.PolyvinylChloride.getBucket())
            .desc(d -> d
                    .line("{HV和EV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("更高阶的塑料，应用范围较广"))
            .task(Task.item(GTMaterials.PolyvinylChloride.getBucket()).optional(), Task.checkmark())
            .dependsOn(POLYETHYLENE_BUCKET));

    public static final Quest ANOTHER_WAY_TO_OBTAIN_GLUE = CHAPTER.quest("another_way_to_obtain_glue", q -> q
            .at(-8, 2.5)
            .icon(GTMaterials.PolyvinylAcetate.getBucket())
            .title("胶水的另一种获取")
            .desc(d -> d.line("多用于胶水合成及其上位材料合成"))
            .task(Task.item(GTMaterials.PolyvinylAcetate.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest TELL_ME_LAMINATED_GLASS_MUST_NEED = CHAPTER.quest("tell_me_laminated_glass_must_need", q -> q
            .at(-6.5, 2.5)
            .icon(GTMaterials.PolyvinylButyral.getBucket())
            .title("你告诉我夹层玻璃必须要这个？")
            .desc(d -> d.line("多用于夹层玻璃合成....."))
            .task(Task.item(GTMaterials.PolyvinylButyral.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest POLYTETRAFLUOROETHYLENE_BUCKET = CHAPTER.quest("polytetrafluoroethylene_bucket", q -> q
            .at(-5, 1)
            .icon(GTMaterials.Polytetrafluoroethylene.getBucket())
            .desc(d -> d
                    .line("{IV和LUV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("进阶塑料，在使用聚乙烯的配方上可以使得消耗减半")
                    .line()
                    .line("同时也是大化反的主要合成原料"))
            .task(Task.item(GTMaterials.Polytetrafluoroethylene.getBucket()).optional(), Task.checkmark())
            .dependsOn(POLYVINYL_CHLORIDE_BUCKET));

    public static final Quest POLYBENZIMIDAZOLE_BUCKET = CHAPTER.quest("polybenzimidazole_bucket", q -> q
            .at(-3.5, 1)
            .icon(GTMaterials.Polybenzimidazole.getBucket())
            .desc(d -> d
                    .line("{ZPM和UV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("聚四氟乙烯的更进阶版本，和聚乙烯一样，相比聚四氟乙烯作为原料的配方消耗进一步减半"))
            .task(Task.item(GTMaterials.Polybenzimidazole.getBucket()).optional(), Task.checkmark())
            .dependsOn(POLYTETRAFLUOROETHYLENE_BUCKET));

    public static final Quest POLYETHERETHERKETONE_BUCKET = CHAPTER.quest("polyetheretherketone_bucket", q -> q
            .at(-2, 1)
            .icon(GTOMaterials.Polyetheretherketone.getBucket())
            .desc(d -> d
                    .line("{UHV和UEV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("更高阶的塑料，应用范围较广"))
            .task(Task.item(GTOMaterials.Polyetheretherketone.getBucket()).optional(), Task.checkmark())
            .dependsOn(POLYBENZIMIDAZOLE_BUCKET));

    public static final Quest EPOXY_BUCKET = CHAPTER.quest("epoxy_bucket", q -> q
            .at(-8, 4)
            .icon(GTMaterials.Epoxy.getBucket())
            .desc(d -> d.line("多用于电路基板合成"))
            .task(Task.item(GTMaterials.Epoxy.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest PAA_BUCKET = CHAPTER.quest("paa_bucket", q -> q
            .at(-6.5, 4)
            .icon(GTOMaterials.Paa.getBucket())
            .desc(d -> d.line("多用于生物、光学电路合成"))
            .task(Task.item(GTOMaterials.Paa.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest POLYURETHANE_BUCKET = CHAPTER.quest("polyurethane_bucket", q -> q
            .at(-5, 4)
            .icon(GTOMaterials.Polyurethane.getBucket())
            .desc(d -> d.line("寰宇处理器核心合成材料之一"))
            .task(Task.item(GTOMaterials.Polyurethane.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest NECESSARY_TOOLS_FOR_NANOSCALE_EXTRACTION = CHAPTER.quest("necessary_tools_for_nanoscale_extraction", q -> q
            .at(-8, 5.5)
            .icon(GTOTagPrefix.NANITES, GTMaterials.Carbon)
            .title("纳米萃取的必要工具")
            .desc(d -> d.line("纳米萃取稀土路线的必要材料"))
            .task(Task.item(GTOMaterials.P507.getBucket()).optional(), Task.checkmark())
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest ESSENTIAL_MATERIALS_FOR_CABLE_SYNTHESIS = CHAPTER.quest("essential_materials_for_cable_synthesis", q -> q
            .at(-8, 7)
            .icon(MultiBlockA.COMPONENT_ASSEMBLY_LINE)
            .title("线缆合成必须品")
            .desc(d -> d.line("这两是线缆合成常客，也是部件装配线常客"))
            .task(Task.item(GTMaterials.PolyphenyleneSulfide.getBucket()), Task.item(TagPrefix.dust, GTMaterials.RawStyreneButadieneRubber))
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest THE_LAST_PLASTIC_SHELL_YOU_CAN = CHAPTER.quest("the_last_plastic_shell_you_can", q -> q
            .at(-0.5, 1)
            .icon(GTOMaterials.Zylon.getBucket())
            .title("所能获取的最后一个机壳塑料")
            .desc(d -> d
                    .line("{UIV和UXV机器外壳}的塑料部件原料", GOLD)
                    .line()
                    .line("更高阶的塑料，应用范围较广"))
            .task(
                    Task.item(TagPrefix.dust, GTOMaterials.PreZylon),
                    Task.item(TagPrefix.dust, GTOMaterials.Zylon),
                    Task.item(GTOMaterials.Zylon.getBucket()))
            .dependsOn(POLYETHERETHERKETONE_BUCKET));

    public static final Quest POLYCAPROLACTAM_INGOT = CHAPTER.quest("polycaprolactam_ingot", q -> q
            .at(-3.5, 4)
            .desc(d -> d.line("多用于光刻膜或者线"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polycaprolactam))
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE)
            .hideDependencyLines());

    public static final Quest LARGE_CHEMICAL_REACTOR = CHAPTER.quest("large_chemical_reactor", q -> q
            .at(-10.5, 2.5)
            .desc(d -> d.line("大化反可以运行小化反配方，反之则不行"))
            .task(Task.item(GTMultiMachines.LARGE_CHEMICAL_REACTOR))
            .dependsOn(WHY_CAN_T_MY_PLASTIC_BE));

    public static final Quest NANITES_INTEGRATED_PROCESSING_CENTER = CHAPTER.quest("nanites_integrated_processing_center", q -> q
            .at(-10.5, 4)
            .desc(d -> d
                    .line("后期的简化化学处理配方机器")
                    .line()
                    .line("与隔壁化学扭曲仪不同，你需要清洗一遍纳米，建议是备好大量纳米再来进行自动化运行")
                    .line()
                    .line("模块主方块放在多方块尾部的聚四氟乙烯管道上面，朝向是该机器的主方块反方向"))
            .task(Task.item(MultiBlockC.NANITES_INTEGRATED_PROCESSING_CENTER))
            .dependsOn(LARGE_CHEMICAL_REACTOR));

    public static final Quest QUANTUM_FORCE_TRANSFORMER = CHAPTER.quest("quantum_force_transformer", q -> q
            .at(-10.5, 5.5)
            .desc(d -> d.line("做到这里，恭喜你进入OpV时代，先出去走走，看看树木，花草"))
            .task(Task.item(MultiBlockA.QUANTUM_FORCE_TRANSFORMER))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER)
            .hideDependentLines());

    public static final Quest FULLERENE_POLYMER_MATRIX_PULP_DUST = CHAPTER.quest("fullerene_polymer_matrix_pulp_dust", q -> q
            .at(1, 1)
            .icon(GTOMaterials.FullerenePolymerMatrixPulp.getBucket())
            .desc(d -> d.line("{OPV机器外壳}的塑料部件原料", GOLD))
            .task(Task.item(TagPrefix.dust, GTOMaterials.FullerenePolymerMatrixPulp), Task.item(GTOMaterials.FullerenePolymerMatrixPulp.getBucket()))
            .dependsOn(QUANTUM_FORCE_TRANSFORMER, THE_LAST_PLASTIC_SHELL_YOU_CAN));

    public static final Quest RADOX_BUCKET = CHAPTER.quest("radox_bucket", q -> q
            .at(2.5, 1)
            .icon(GTOMaterials.Radox.getBucket())
            .desc(d -> d.line("{MAX机器外壳}的塑料部件原料", GOLD))
            .task(Task.item(GTOMaterials.Radox.getBucket()).optional(), Task.checkmark())
            .dependsOn(QUANTUM_FORCE_TRANSFORMER, FULLERENE_POLYMER_MATRIX_PULP_DUST));

    public static final Quest PETROCHEMICAL_PLANT = CHAPTER.quest("petrochemical_plant", q -> q
            .at(-14, -3.5)
            .desc(d -> d.line("后续的集成处理工厂，需求多方块多，准备好材料再来"))
            .task(Task.item(MultiBlockA.PETROCHEMICAL_PLANT))
            .dependsOn(PETROLEUM_PROCESSING));
}
