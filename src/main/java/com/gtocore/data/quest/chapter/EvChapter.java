package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class EvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("ev", c -> c
            .title("7. 超高压(EV)")
            .subtitle("Tier4-EV")
            .icon(GCYMMachines.BLAST_ALLOY_SMELTER)
            .hideQuestsUntilDepsVisible()
            .autofocus(EvChapter.TUNGSTEN_DUST));

    public static final Quest URANIUM_ORE = CHAPTER.quest("uranium_ore", q -> q
            .at(6, -9.5)
            .title("铀矿")
            .desc(d -> d.line("你在月球捡到的，还记得吗？"))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Pitchblende), Task.item(TagPrefix.rawOre, GTMaterials.Uraninite))
            .dependsOn(HvChapter.HEAD_TO_THE_MOON));

    public static final Quest URANIUM_HEXAFLUORIDE_BUCKET = CHAPTER.quest("uranium_hexafluoride_bucket", q -> q
            .at(5, -10)
            .icon(GTMaterials.UraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.UraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .dependsOn(URANIUM_ORE));

    public static final Quest ENRICHED_URANIUM_HEXAFLUORIDE_BUCKET = CHAPTER.quest("enriched_uranium_hexafluoride_bucket", q -> q
            .at(4.5, -11)
            .icon(GTMaterials.EnrichedUraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.EnrichedUraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .dependsOn(URANIUM_HEXAFLUORIDE_BUCKET));

    public static final Quest DEPLETED_URANIUM_HEXAFLUORIDE_BUCKET = CHAPTER.quest("depleted_uranium_hexafluoride_bucket", q -> q
            .at(5.5, -11)
            .icon(GTMaterials.DepletedUraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.DepletedUraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .dependsOn(URANIUM_HEXAFLUORIDE_BUCKET));

    public static final Quest URANIUM_DUST = CHAPTER.quest("uranium_dust", q -> q
            .at(5, -12)
            .subtitle("小孩勿食")
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium238))
            .dependsOn(DEPLETED_URANIUM_HEXAFLUORIDE_BUCKET));

    public static final Quest NETHER_STAR = CHAPTER.quest("nether_star", q -> q
            .at(4.5, -13)
            .desc(d -> d
                    .line("电弧发生器合成它需要{无重力环境}，这是你第二次前往空间站", BLUE)
                    .line()
                    .line("凋零骷髅头...看看工业屠宰场吧")
                    .line()
                    .line("亦或者通过{魔法}获取", LIGHT_PURPLE))
            .task(Task.item(Items.NETHER_STAR))
            .dependsOn(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE)
            .hideDependencyLines());

    public static final Quest WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE = CHAPTER.quest("welcome_to_the_extreme_voltage_stage", q -> q
            .at(5, 8.5)
            .title("欢迎来到{超高压}阶段", DARK_PURPLE)
            .subtitle("{你需要天马行空般的想象力}", DARK_PURPLE)
            .desc(d -> d
                    .line("你的最终目标是{IV}{机器外壳}......？", IV, GOLD)
                    .line()
                    .line("不，这是一个{航天时代}！最终目标是{四阶火箭}", LIGHT_PURPLE, GOLD)
                    .line()
                    .line("我建议你首先发射二阶火箭获取{钨}，然后开始着手设计你的{铂处}产线", GOLD, GOLD)
                    .line()
                    .line("{希望你喜欢这个任务“火箭”}", GRAY.italic()))
            .task(Task.item(GTMachines.HULL[GTValues.EV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(HvChapter.TITANIUM_INGOT));

    public static final Quest SOFT_AND_FLUFFY_PLATINUM_ORE = CHAPTER.quest("soft_and_fluffy_platinum_ore", q -> q
            .at(12, 2)
            .title("香香软软铂系矿泥")
            .desc(d -> d
                    .line("你需要投入你所有的铂金属来源：{斑铜矿、黝铜矿、辉铜矿、谢尔顿矿}", GOLD)
                    .line()
                    .line("你无需一次性完成整条铂处产线，可以先进行你马上需要用到的{钌}与{铑}的产出。但我仍建议一定要搭建一条独立完整的铂处产线", GOLD, GOLD)
                    .line()
                    .line("希望你享受铂族处理！"))
            .task(Task.item(TagPrefix.dust, GTMaterials.PlatinumGroupSludge))
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest EPOXY_BUCKET = CHAPTER.quest("epoxy_bucket", q -> q
            .at(2.5, -3.5)
            .icon(GTMaterials.Epoxy.getBucket())
            .task(Task.item(GTMaterials.Epoxy.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest DENSE_HYDRAZINE_FUEL_MIXTURE_BUCKET = CHAPTER.quest("dense_hydrazine_fuel_mixture_bucket", q -> q
            .at(7, -4)
            .icon(GTOMaterials.DenseHydrazineFuelMixture.getBucket())
            .task(Task.item(GTOMaterials.DenseHydrazineFuelMixture.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest ROCKET_FUEL_CN3H7O3_BUCKET = CHAPTER.quest("rocket_fuel_cn3h7o3_bucket", q -> q
            .at(7, -16)
            .icon(GTOMaterials.RocketFuelCn3h7o3.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelCn3h7o3.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest ROCKET_FUEL_RP_1_BUCKET = CHAPTER.quest("rocket_fuel_rp_1_bucket", q -> q
            .at(7, 5.5)
            .icon(GTOMaterials.RocketFuelRp1.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelRp1.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest CARBON_FIBERS = CHAPTER.quest("carbon_fibers", q -> q
            .at(-0.5, -5)
            .task(Task.item(GTItems.CARBON_FIBERS))
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest EV_ROCKET_ENGINE = CHAPTER.quest("ev_rocket_engine", q -> q
            .at(3.5, 8.5)
            .desc(d -> d.line("EV初期你最强力的单方块电力来源"))
            .task(Task.item(GTOMachines.ROCKET_ENGINE_GENERATOR[GTValues.EV]))
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .optional());

    public static final Quest SILICONE_RUBBER_BUCKET = CHAPTER.quest("silicone_rubber_bucket", q -> q
            .at(5, 10.5)
            .icon(GTMaterials.SiliconeRubber.getBucket())
            .desc(d -> d
                    .line("EV时代开始你开始需要更高级的橡胶来制作讨厌的{传送带}和{电动泵}，以及IV级别的{线缆}", GOLD, GOLD, GOLD)
                    .line()
                    .line("给予它足够的重视，建立一个大量持续产出的产线！"))
            .task(Task.item(GTMaterials.SiliconeRubber.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE));

    public static final Quest RESERVOIR_HATCH = CHAPTER.quest("reservoir_hatch", q -> q
            .at(6.5, 8.5)
            .subtitle("很多很多水！")
            .desc(d -> d.line("暂停工作的时候会停止产水哦"))
            .task(Task.item(GTMachines.RESERVOIR_HATCH))
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE, HvChapter.INFINITE_WATER_COVER));

    public static final Quest HEAVY_DUTY_PLATE_2 = CHAPTER.quest("heavy_duty_plate_2", q -> q
            .at(3, 5.5)
            .desc(d -> d.line("注意EMI页面中化学浸洗机右下的维度标识！这意味着你需要去到特定维度才能制作配方"))
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_2))
            .dependsOn(HvChapter.TITANIUM_INGOT));

    public static final Quest TIER_2_ROCKET = CHAPTER.quest("tier_2_rocket", q -> q
            .at(5, 5.5)
            .icon(ModItems.TIER_2_ROCKET.get())
            .desc(d -> d
                    .line("没有什么能阻挡你再次飞向星辰")
                    .line()
                    .line("来一点钛，再来一点\"润滑油泡钢\"...")
                    .line()
                    .line("别忘了通过卫星控制中心解锁{火星}再上天", LIGHT_PURPLE))
            .task(Task.item(ModItems.TIER_2_ROCKET.get()))
            .dependsOn(WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE, ROCKET_FUEL_RP_1_BUCKET, HEAVY_DUTY_PLATE_2));

    public static final Quest VISIT_MARS = CHAPTER.quest("visit_mars", q -> q
            .at(5, 3.5)
            .desc(d -> d
                    .line("火星是需要乘载T2火箭到达的星球，简称T2星球")
                    .line()
                    .line("从火星上获得大量白钨矿，开始你的第一个产线挑战"))
            .task(Task.dimension(Dimension.MARS).title("{访问火星}", LIGHT_PURPLE).icon(ModItems.MARS_GLOBE.get()))
            .dependsOn(TIER_2_ROCKET));

    public static final Quest RAW_SCHEELITE = CHAPTER.quest("raw_scheelite", q -> q
            .at(5, 1.75)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Scheelite), Task.item(TagPrefix.rawOre, GTMaterials.Tungstate))
            .dependsOn(VISIT_MARS));

    public static final Quest TUNGSTEN_DUST = CHAPTER.quest("tungsten_dust", q -> q
            .at(5, 0)
            .subtitle("EV阶段的第一个生产挑战")
            .desc(d -> d
                    .line("{钨粉}是EV阶段最重要的单质，你需要大量生产", GOLD)
                    .line()
                    .line("随即解锁的极其有用的三种合金都需要钨元素"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Tungsten))
            .dependsOn(RAW_SCHEELITE));

    public static final Quest TUNGSTEN_STEEL_INGOT = CHAPTER.quest("tungsten_steel_ingot", q -> q
            .at(5, -2)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenSteel))
            .dependsOn(TUNGSTEN_DUST));

    public static final Quest CERAMIC_TWO_PIECE_SET = CHAPTER.quest("ceramic_two_piece_set", q -> q
            .at(3, -2)
            .title("陶瓷两件套")
            .subtitle("绝对相等")
            .desc(d -> d
                    .line("高级陶瓷块需要更为精确的{等静压成型}，否则会因为压力不均产生内部缺陷", GOLD)
                    .line()
                    .line("烧结炉将{陶瓷粗胚}烧结成为{陶瓷块}", GOLD, GOLD))
            .task(Task.item(MultiBlockG.ISOSTATIC_PRESS), Task.item(MultiBlockG.SINTERING_FURNACE))
            .dependsOn(TUNGSTEN_STEEL_INGOT));

    public static final Quest TUNGSTEN_CARBIDE_INGOT = CHAPTER.quest("tungsten_carbide_ingot", q -> q
            .at(1, -1)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenCarbide))
            .dependsOn(TUNGSTEN_DUST));

    public static final Quest CHEMICAL_VAPOR_DEPOSITION = CHAPTER.quest("chemical_vapor_deposition", q -> q
            .at(1, -3)
            .desc(d -> d.line("化学气相沉积(Chemical Vapor Deposition, CVD)是一种先进的材料制备技术，在今后你需要它来制作大量功能各异的新型材料"))
            .task(Task.item(MultiBlockC.CHEMICAL_VAPOR_DEPOSITION))
            .dependsOn(TUNGSTEN_CARBIDE_INGOT));

    public static final Quest NANO_CPU_CHIP = CHAPTER.quest("nano_cpu_chip", q -> q
            .at(1, -5)
            .task(Task.item(GTItems.NANO_CENTRAL_PROCESSING_UNIT))
            .dependsOn(CHEMICAL_VAPOR_DEPOSITION, CARBON_FIBERS));

    public static final Quest RADON_BUCKET = CHAPTER.quest("radon_bucket", q -> q
            .at(3, 3.5)
            .icon(GTMaterials.Radon.getBucket())
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .dependsOn(VISIT_MARS)
            .hideDependentLines());

    public static final Quest QUANTUM_EYE = CHAPTER.quest("quantum_eye", q -> q
            .at(-2, -1)
            .desc(d -> d.line("由于其亚稳态性，{量子之眼}需要在{无重力环境}中用惰性气体{氡气}进行浸泡", GOLD, LIGHT_PURPLE, GOLD))
            .task(Task.item(GTItems.QUANTUM_EYE))
            .dependsOn(RADON_BUCKET, SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE));

    public static final Quest HV_FIELD_GENERATOR = CHAPTER.quest("hv_field_generator", q -> q
            .at(-1, -1)
            .task(Task.item(GTItems.FIELD_GENERATOR_HV))
            .dependsOn(QUANTUM_EYE));

    public static final Quest VIBRANT_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("vibrant_photovoltaic_power_station", q -> q
            .at(7, -10.5)
            .desc(d -> d.line("光伏电站的终极方案"))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_VIBRANT))
            .dependsOn(TUNGSTEN_STEEL_INGOT)
            .optional().hideDependencyLines());

    public static final Quest DARK_STEEL_VAJRA = CHAPTER.quest("dark_steel_vajra", q -> q
            .at(-1.5, -2).iconScale(0.75)
            .desc(d -> d
                    .line("作为一把万能工具，你在基地大部分需要工具的地方都能用到{金刚杵}", GOLD)
                    .line()
                    .line("如果你没改动过按键设置，按{[J]}和{[Shift]+[J]}键调整挖掘速度，调试产线时最好不要设置得太大", YELLOW, YELLOW)
                    .line()
                    .line("{如果你在多人服务器游玩并因此搞砸了某些事情，我预见到你的朋友可能会提着另一根金刚杵来找你……}", GRAY.italic()))
            .task(Task.item(ToolHelper.get(GTOToolType.VAJRA_HV, GTOMaterials.DarkSteel).getItem()))
            .dependsOn(HV_FIELD_GENERATOR));

    public static final Quest CLEANING_MAINTENANCE_HATCH = CHAPTER.quest("cleaning_maintenance_hatch", q -> q
            .at(-1.5, 0)
            .desc(d -> d
                    .line("它可以为{多方块机器}提供超净间环境，但{不支持}{通用工厂}", YELLOW, DARK_RED, GOLD)
                    .line()
                    .line("{神奇的是，它可以安装在蒸汽机器上...}", GRAY.italic()))
            .task(Task.item(GTMachines.CLEANING_MAINTENANCE_HATCH))
            .dependsOn(HV_FIELD_GENERATOR));

    public static final Quest RUTHENIUM_DUST = CHAPTER.quest("ruthenium_dust", q -> q
            .at(11, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Ruthenium))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines());

    public static final Quest RTM_ALLOY_INGOT = CHAPTER.quest("rtm_alloy_ingot", q -> q
            .at(9, -1)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RTMAlloy))
            .dependsOn(TUNGSTEN_DUST, RUTHENIUM_DUST));

    public static final Quest RTM_ALLOY_COIL_BLOCK = CHAPTER.quest("rtm_alloy_coil_block", q -> q
            .at(9, -4.5)
            .desc(d -> d.line("T4级别的线圈方块，集合了你至今为止所有的矿处产线成果"))
            .task(Task.item(GTBlocks.COIL_RTMALLOY))
            .dependsOn(RTM_ALLOY_INGOT)
            .hideDependentLines());

    public static final Quest URANIUM_TRIPLATINUM_INGOT = CHAPTER.quest("uranium_triplatinum_ingot", q -> q
            .at(5.5, -13)
            .task(Task.item(TagPrefix.ingot, GTMaterials.UraniumTriplatinum))
            .dependsOn(RTM_ALLOY_COIL_BLOCK, URANIUM_DUST));

    public static final Quest IRIDIUM_DUST = CHAPTER.quest("iridium_dust", q -> q
            .at(12.5, -2)
            .desc(d -> d.line("下个阶段的重要材料"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Iridium))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines());

    public static final Quest PLATINUM_DUST = CHAPTER.quest("platinum_dust", q -> q
            .at(11.5, 0)
            .desc(d -> d.line("无需经过复杂的矿泥处理也可以获取"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Platinum))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines().hideDependentLines());

    public static final Quest EV_EMITTER = CHAPTER.quest("ev_emitter", q -> q
            .at(-2.5, -2)
            .task(Task.item(GTItems.EMITTER_EV))
            .dependsOn(QUANTUM_EYE, PLATINUM_DUST));

    public static final Quest EV_CIRCUIT_ASSEMBLER = CHAPTER.quest("ev_circuit_assembler", q -> q
            .at(-1.5, -6.5)
            .desc(d -> d.line("获得了{进阶电路组装机 Ⅲ}？丢进你的超净间，{纳米电路}即将排产！", GOLD, GOLD))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.EV]))
            .dependsOn(EV_EMITTER, HvChapter.MICROPROCESSOR_MAINFRAME));

    public static final Quest EV_SENSOR = CHAPTER.quest("ev_sensor", q -> q
            .at(-2.5, 0)
            .task(Task.item(GTItems.SENSOR_EV))
            .dependsOn(QUANTUM_EYE, PLATINUM_DUST)
            .hideDependentLines());

    public static final Quest TIER_3_ROCKET = CHAPTER.quest("tier_3_rocket", q -> q
            .at(5, -4)
            .icon(ModItems.TIER_3_ROCKET.get())
            .desc(d -> d.line("别忘了穿上{T2}器官", AQUA))
            .task(Task.item(ModItems.TIER_3_ROCKET.get()))
            .dependsOn(TUNGSTEN_STEEL_INGOT, DENSE_HYDRAZINE_FUEL_MIXTURE_BUCKET, EV_SENSOR));

    public static final Quest VISIT_VENUS = CHAPTER.quest("visit_venus", q -> q
            .at(5, -8)
            .desc(d -> d
                    .line("金星的环境非常恶劣，其地表温度过高，你需要抗火能力")
                    .line()
                    .line("HV阶段解锁的{太空纳米胸甲}免费提供了该能力", GOLD)
                    .line()
                    .line("前往{金星}以完成此任务", GOLD))
            .task(Task.dimension(Dimension.VENUS).title("{访问金星}", LIGHT_PURPLE).icon(ModItems.VENUS_GLOBE.get()))
            .dependsOn(TIER_3_ROCKET, BeyondTheStarsChapter.MORTAL_FLESH_DECAYS_GENES_REFORGED));

    public static final Quest RAW_DESH = CHAPTER.quest("raw_desh", q -> q
            .at(7, -8)
            .desc(d -> d.line("如果你从其他地方弄到了一定数量也可以直接完成此任务"))
            .task(Task.item(ModItems.RAW_DESH.get()))
            .dependsOn(VISIT_VENUS, UtilChapter.RAW_DESH)
            .requireAny());

    public static final Quest PALLADIUM_DUST = CHAPTER.quest("palladium_dust", q -> q
            .at(12.5, 0)
            .desc(d -> d.line("无需经过复杂的矿泥处理也可以获取"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Palladium))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines());

    public static final Quest OSMIUM_DUST = CHAPTER.quest("osmium_dust", q -> q
            .at(13, -1)
            .desc(d -> d.line("下个阶段的重要材料"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Osmium))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines());

    public static final Quest RHODIUM_DUST = CHAPTER.quest("rhodium_dust", q -> q
            .at(11.5, -2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rhodium))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE)
            .hideDependencyLines());

    public static final Quest LET_ME_SEE_YOUR_RESULTS = CHAPTER.quest("let_me_see_your_results", q -> q
            .at(12, -1)
            .title("让我看看你的成果！")
            .desc(d -> d.line("EV阶段的第二个单质生产挑战，你终于完成了铂系处理产线！"))
            .task(
                    Task.item(TagPrefix.dust, GTMaterials.Platinum),
                    Task.item(TagPrefix.dust, GTMaterials.Palladium),
                    Task.item(TagPrefix.dust, GTMaterials.Ruthenium),
                    Task.item(TagPrefix.dust, GTMaterials.Rhodium),
                    Task.item(TagPrefix.dust, GTMaterials.Iridium),
                    Task.item(TagPrefix.dust, GTMaterials.Osmium))
            .reward(Reward.item(Items.NETHER_STAR))
            .dependsOn(SOFT_AND_FLUFFY_PLATINUM_ORE, PLATINUM_DUST, RHODIUM_DUST, OSMIUM_DUST, RUTHENIUM_DUST, PALLADIUM_DUST, IRIDIUM_DUST));

    public static final Quest EV_FIELD_GENERATOR = CHAPTER.quest("ev_field_generator", q -> q
            .at(5, -14.25)
            .task(Task.item(GTItems.FIELD_GENERATOR_EV))
            .dependsOn(URANIUM_TRIPLATINUM_INGOT, NETHER_STAR));

    public static final Quest DESH_INGOT = CHAPTER.quest("desh_ingot", q -> q
            .at(9, -8)
            .desc(d -> d.line("为什么会需要铑才能制作？{也许这就是恶趣味}", GRAY.italic().strike()))
            .task(Task.item(ModItems.DESH_INGOT.get()))
            .dependsOn(RAW_DESH, RTM_ALLOY_COIL_BLOCK, RHODIUM_DUST));

    public static final Quest WATER_PURIFICATION_PLANT = CHAPTER.quest("water_purification_plant", q -> q
            .at(9, -10)
            .desc(d -> d
                    .line("随着芯片制作越发精密，普通的蒸馏水已经不能满足切割晶圆的需要。所以... 你已经解锁了GTO的一条新的支线——{净化水}！", AQUA)
                    .line()
                    .line("净化水厂是整个净化水网络的核心。它用于把所有水厂收纳为统一的网络，并为它们集中供电")
                    .line()
                    .line("{你可以在机器的tooltips上看到它支持激光仓，你马上就会在IV阶段解锁激光仓。如果普通能源仓供电不足，别忘了换上它}", GRAY.italic()))
            .task(Task.item(MultiBlockB.WATER_PURIFICATION_PLANT))
            .dependsOn(DESH_INGOT));

    public static final Quest CLARIFIER_PURIFICATION_UNIT = CHAPTER.quest("clarifier_purification_unit", q -> q
            .at(9, -12)
            .desc(d -> d.line("净化水机器并行需要调至{1000}的整数倍才会运行，并行数决定了一次周期内制作多少水和耗电量", GREEN))
            .task(Task.item(MultiBlockB.CLARIFIER_PURIFICATION_UNIT))
            .dependsOn(WATER_PURIFICATION_PLANT));

    public static final Quest FILTERED_WATER_BUCKET = CHAPTER.quest("filtered_water_bucket", q -> q
            .at(8.2, -12.8)
            .icon(GTOMaterials.FilteredSater.getBucket())
            .desc(d -> d.line("水净化处理的第一个步骤，恰好满足当前的洁净度需求"))
            .task(Task.item(GTOMaterials.FilteredSater.getBucket()).optional(), Task.checkmark())
            .dependsOn(CLARIFIER_PURIFICATION_UNIT));

    public static final Quest MPIC_CHIP = CHAPTER.quest("mpic_chip", q -> q
            .at(7.55, -13.46)
            .task(Task.item(GTItems.POWER_INTEGRATED_CIRCUIT))
            .dependsOn(FILTERED_WATER_BUCKET));

    public static final Quest EV_ENERGY_INPUT_HATCH = CHAPTER.quest("ev_energy_input_hatch", q -> q
            .at(6.75, -14.25)
            .desc(d -> d
                    .line("EV能源仓！")
                    .line()
                    .line("给你的机器来一次更新换代！"))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.EV]))
            .dependsOn(MPIC_CHIP));

    public static final Quest TIER_4_ROCKET = CHAPTER.quest("tier_4_rocket", q -> q
            .at(5, -16)
            .icon(ModItems.TIER_4_ROCKET.get())
            .desc(d -> d.line("四阶火箭的强大推进力解锁了一颗小行星带的矮行星——谷神星"))
            .task(Task.item(ModItems.TIER_4_ROCKET.get()))
            .dependsOn(ROCKET_FUEL_CN3H7O3_BUCKET, EV_ENERGY_INPUT_HATCH, EV_FIELD_GENERATOR));

    public static final Quest VISIT_CERES = CHAPTER.quest("visit_ceres", q -> q
            .at(5, -18.5)
            .desc(d -> d.line("前往{谷神星}以完成此任务", GOLD))
            .task(Task.dimension(Dimension.CERES).title("{访问谷神星}", LIGHT_PURPLE).icon(GTOGlobes.Items.CERES_GLOBE.get()))
            .dependsOn(TIER_4_ROCKET));

    public static final Quest RAW_OSTRUM = CHAPTER.quest("raw_ostrum", q -> q
            .at(5, -20.5)
            .desc(d -> d.line("你需要用紫金制作下一阶火箭"))
            .task(Task.item(ModItems.RAW_OSTRUM.get()))
            .dependsOn(VISIT_CERES));

    public static final Quest IRIDIUM_INGOT = CHAPTER.quest("iridium_ingot", q -> q
            .at(3.25, -12.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Iridium))
            .dependsOn(EV_ENERGY_INPUT_HATCH, IRIDIUM_DUST)
            .hideDependencyLines());

    public static final Quest ALLOY_BLAST_SMELTER = CHAPTER.quest("alloy_blast_smelter", q -> q
            .at(7.5, -6)
            .subtitle("炖史炉")
            .desc(d -> d
                    .line("合金冶炼炉和工业高炉类似，是专门用来生产{熔融态合金}的重要多方块机器", GOLD)
                    .line()
                    .line("熔融态合金与工业高炉产出的热锭类似，需要经过{真空冷冻机}冷却才能进一步加工", GOLD)
                    .line()
                    .line("当你后续看到新合金锭，你可以直接去尝试查找其熔融形态的合金冶炼炉配方。它能一次性产出{多个锭}的量的{熔融流体}，一般要比搅拌后再用高炉去烧{速度要快上不少}", AQUA, YELLOW, YELLOW)
                    .line()
                    .line("从{下个阶段}开始会有大量{只有}它才能制作的金属", YELLOW, RED))
            .task(Task.item(GCYMMachines.BLAST_ALLOY_SMELTER))
            .dependsOn(RTM_ALLOY_COIL_BLOCK));

    public static final Quest INTEGRAL_FRAMEWORK_EV = CHAPTER.quest("integral_framework_ev", q -> q
            .at(5.75, -6)
            .subtitle("别忘了替换机器中的整体框架")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_EV))
            .dependsOn(ALLOY_BLAST_SMELTER));

    public static final Quest EV_DRONE = CHAPTER.quest("ev_drone", q -> q
            .at(-3, -1)
            .desc(d -> d.line("维护半径大了三倍，一台足以覆盖你的整个基地"))
            .task(Task.item(GTOItems.EV_DRONE))
            .dependsOn(EV_EMITTER, EV_SENSOR, TUNGSTEN_STEEL_INGOT)
            .hideDependencyLines());

    public static final Quest GREAT_FORCE_CREATES_MIRACLES_EASY = CHAPTER.quest("great_force_creates_miracles_easy", q -> q
            .at(4.18, -2.82)
            .icon(MultiBlockG.ISOSTATIC_PRESS)
            .title("大力出奇迹{（简单）}", GREEN)
            .desc(d -> d
                    .line("简单模式陶瓷无需压制成型")
                    .line()
                    .line("{史蒂夫的手可以解决一切}", GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(UtilChapter.Q_056EC4E6, TUNGSTEN_STEEL_INGOT)
            .optional().hideUntilDepsVisible());

    public static final Quest BARIUM_TITANATE_CERAMIC_FLAKE = CHAPTER.quest("barium_titanate_ceramic_flake", q -> q
            .at(3.5, -3.5)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic))
            .dependsOn(CERAMIC_TWO_PIECE_SET, GREAT_FORCE_CREATES_MIRACLES_EASY)
            .requireAny());

    public static final Quest EPOXY_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("epoxy_printed_circuit_board", q -> q
            .at(3, -5)
            .task(Task.item(GTItems.ADVANCED_CIRCUIT_BOARD))
            .dependsOn(BARIUM_TITANATE_CERAMIC_FLAKE, EPOXY_BUCKET));

    public static final Quest NANOPROCESSOR = CHAPTER.quest("nanoprocessor", q -> q
            .at(1, -6.5)
            .title("{纳米处理器}", AQUA)
            .subtitle("更便宜的HV级电路")
            .desc(d -> d
                    .line("在你后续加工新的电路板时，切记要优化一下之前的配方。现在你可以使用更加廉价的贴片系列！")
                    .line()
                    .line("检查清单：电阻，电容，晶体管是否是贴片版本"))
            .task(Task.item(GTItems.NANO_PROCESSOR_HV))
            .dependsOn(
                    EV_CIRCUIT_ASSEMBLER,
                    EPOXY_PRINTED_CIRCUIT_BOARD,
                    NANO_CPU_CHIP,
                    HvChapter.SMD_RESISTOR,
                    HvChapter.SMD_CAPACITOR,
                    HvChapter.SMD_TRANSISTOR));

    public static final Quest NANOPROCESSOR_ASSEMBLY = CHAPTER.quest("nanoprocessor_assembly", q -> q
            .at(1, -8)
            .title("{纳米处理器集群}", AQUA)
            .subtitle("更便宜的EV级电路")
            .desc(d -> d.line("检查清单：电感，电容，同样是贴片版本"))
            .task(Task.item(GTItems.NANO_PROCESSOR_ASSEMBLY_EV))
            .dependsOn(NANOPROCESSOR, HvChapter.SMD_INDUCTOR));

    public static final Quest NANOPROCESSOR_SUPERCOMPUTER = CHAPTER.quest("nanoprocessor_supercomputer", q -> q
            .at(1, -9.75)
            .title("{纳米处理器超级计算机}", AQUA)
            .subtitle("更便宜的IV级电路")
            .desc(d -> d.line("检查清单：二极管，还是贴片版本"))
            .task(Task.item(GTItems.NANO_COMPUTER_IV))
            .dependsOn(NANOPROCESSOR_ASSEMBLY, HvChapter.SMD_DIODE));

    public static final Quest NANOPROCESSOR_MAINFRAME = CHAPTER.quest("nanoprocessor_mainframe", q -> q
            .at(1, -12)
            .title("{纳米处理器主机}", AQUA)
            .subtitle("第一个LuV级电路")
            .desc(d -> d.line("觉得太贵？别担心，你最终会找到更便宜的路径"))
            .task(Task.item(GTItems.NANO_MAINFRAME_LuV))
            .dependsOn(NANOPROCESSOR_SUPERCOMPUTER));

    public static final Quest LARGE_GAS_COLLECTOR = CHAPTER.quest("large_gas_collector", q -> q
            .at(3.25, -14.25)
            .desc(d -> d
                    .line("用来大量收集各维度的{空气}，放入{真空冷冻机}以直接收集{液态空气}", GOLD, GOLD, GOLD)
                    .line()
                    .line("蒸馏某些空气能够获得{非常稀有的气态物质}，你应该考虑搭建专门的产线持续产出这些物质（如保护气{氩}）", YELLOW, GREEN))
            .task(Task.item(MultiBlockA.LARGE_GAS_COLLECTOR))
            .dependsOn(NANOPROCESSOR_MAINFRAME, IRIDIUM_INGOT, EV_FIELD_GENERATOR));

    public static final Quest VOID_MINER = CHAPTER.quest("void_miner", q -> q
            .at(-0.5, -10.5)
            .desc(d -> d.line("{进阶基岩采矿机}的延伸版本，仅需在指定维度即可产出矿石", GOLD))
            .task(Task.item(GTMultiMachines.LARGE_PLASMA_TURBINE))
            .dependsOn(HvChapter.HV_BEDROCK_ORE_MINER, NANOPROCESSOR_SUPERCOMPUTER)
            .hideDependencyLines(false));

    public static final Quest EV_BEDROCK_ORE_MINER = CHAPTER.quest("ev_bedrock_ore_miner", q -> q
            .at(-0.5, -9)
            .desc(d -> d.line("{进阶基岩采矿机}的再进阶版本！", GOLD))
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.EV]))
            .dependsOn(NANOPROCESSOR_SUPERCOMPUTER)
            .hideDependencyLines(false));

    public static final Quest ROCKET_LARGE_TURBINE = CHAPTER.quest("rocket_large_turbine", q -> q
            .at(2.5, -10.5)
            .desc(d -> d.line("火箭引擎的多方块机器版本，效率与发电量远远超过单方块机器"))
            .task(Task.item(GeneratorMultiblock.ROCKET_LARGE_TURBINE))
            .dependsOn(NANOPROCESSOR_SUPERCOMPUTER)
            .optional());

    public static final Quest STEAM_MEGA_TURBINE = CHAPTER.quest("steam_mega_turbine", q -> q
            .at(2.5, -9)
            .task(Task.item(GeneratorMultiblock.STEAM_MEGA_TURBINE))
            .dependsOn(HvChapter.STEAM_LARGE_TURBINE, NANOPROCESSOR_SUPERCOMPUTER)
            .optional());

    public static final Quest GAS_MEGA_TURBINE = CHAPTER.quest("gas_mega_turbine", q -> q
            .at(-1, -12)
            .task(Task.item(GeneratorMultiblock.GAS_MEGA_TURBINE))
            .dependsOn(NANOPROCESSOR_MAINFRAME)
            .optional());

    public static final Quest DATA_ORB = CHAPTER.quest("data_orb", q -> q
            .at(3, -6.5)
            .subtitle("二级数据载体")
            .desc(d -> d.line("目前没什么用,{LuV}阶段才有它的用武之地", LUV))
            .task(Task.item(GTItems.TOOL_DATA_ORB))
            .dependsOn(EPOXY_PRINTED_CIRCUIT_BOARD));

    public static final Quest LARGE_GREENHOUSE = CHAPTER.quest("large_greenhouse", q -> q
            .at(7, -12)
            .task(Task.item(MultiBlockG.LARGE_GREENHOUSE))
            .dependsOn(NANOPROCESSOR_MAINFRAME, EV_FIELD_GENERATOR)
            .hideDependencyLines());

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.ME_EXPORT_BUS_AND_ME_EXPORT, 6, 7))
                .link(QuestLink.of(AeChapter.ME_IMPORT_BUS_AND_ME_IMPORT, 4, 7))
                .link(QuestLink.of(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE, -2, 2))
                .link(QuestLink.of(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, 7, -2))
                .link(QuestLink.of(BeyondTheStarsChapter.SPACE_ADVANCED_NANOMUSCLE_CHESTPLATE, 7, 3.5))
                .link(QuestLink.of(BeyondTheStarsChapter.MORTAL_FLESH_DECAYS_GENES_REFORGED, 3.5, -8)));
    }
}
