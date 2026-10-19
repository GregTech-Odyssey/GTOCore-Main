package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.MultiBlockI;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class UhvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("uhv", c -> c
            .title("12. 极高压(UHV)")
            .subtitle("Tier9-UHV")
            .icon(MultiBlockA.STELLAR_FORGE)
            .hideQuestsUntilDepsVisible());

    public static final Quest NETHERITE_INGOT = CHAPTER.quest("netherite_ingot", q -> q
            .at(-0.5, 2.5)
            .subtitle("并没有原版那么简单！")
            .desc(d -> d
                    .line("探索{猪灵堡垒}和{地牢}（如锻造厂）有概率获取下界合金锭...不过显然你不会满足", YELLOW, YELLOW)
                    .line()
                    .line("好消息是，你可以使用中子活化器来制作{下界合金粉}！用远古残骸直接制作的{动能消耗}远不是你现在所能承受的，你应当选择{碎片}配方", GOLD, GREEN, YELLOW)
                    .line()
                    .line("{使用陨星之板能获得很多下界合金块，你也可以选择坠个大的然后慢慢挖}", GRAY.italic()))
            .task(Task.item(Items.NETHERITE_INGOT))
            .dependsOn(LuvChapter.NEUTRON_ACTIVATOR)
            .hideDependencyLines());

    public static final Quest ENERGETIC_NETHERITE_DUST = CHAPTER.quest("energetic_netherite_dust", q -> q
            .at(-0.5, 1)
            .subtitle("电压！电压！")
            .desc(d -> d
                    .line("很可惜... 你只能暂时用下界合金块去合成！")
                    .line()
                    .line("{超导电磁工厂可以使用双UHV能源仓到达UEV电压来大幅降低造价}", GRAY.italic()))
            .task(Task.item(TagPrefix.dust, GTOMaterials.EnergeticNetherite))
            .dependsOn(NETHERITE_INGOT));

    public static final Quest ENGRAVING_LASER_PLANT = CHAPTER.quest("engraving_laser_plant", q -> q
            .at(6.5, 10.5)
            .subtitle("巨构，但不完全巨构")
            .desc(d -> d
                    .line("无损超频，但是不能用激光仓，精密配方不支持并行")
                    .line()
                    .line("和你的{焊接}和{蚀刻}说再见吧！", YELLOW, YELLOW))
            .task(Task.item(MultiBlockA.ENGRAVING_LASER_PLANT))
            .dependsOn(UvChapter.BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest BROMINE_BUCKET = CHAPTER.quest("bromine_bucket", q -> q
            .at(2, 1)
            .icon(GTMaterials.Bromine.getBucket())
            .task(Task.item(GTMaterials.Bromine.getBucket()).optional(), Task.checkmark())
            .dependsOn(UvChapter.RESOURCE_COLLECTION_MODULE)
            .hideDependencyLines());

    public static final Quest FULLERENE_DUST = CHAPTER.quest("fullerene_dust", q -> q
            .at(2, 2.5)
            .subtitle("谁也不知道这些原子为什么会变成足球")
            .desc(d -> d.line("享受你的产线吧x2"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.Fullerene))
            .dependsOn(BROMINE_BUCKET));

    public static final Quest MATTER_FABRICATOR = CHAPTER.quest("matter_fabricator", q -> q
            .at(0.5, 12.5)
            .subtitle("废料大一统")
            .task(Task.item(MultiBlockA.MATTER_FABRICATOR))
            .dependsOn(UvChapter.BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest UU_AMPLIFIER_BUCKET = CHAPTER.quest("uu_amplifier_bucket", q -> q
            .at(-1.5, 12.5)
            .icon(GTOMaterials.UuAmplifier.getBucket())
            .task(Task.item(GTOMaterials.UuAmplifier.getBucket()).optional(), Task.checkmark())
            .dependsOn(MATTER_FABRICATOR)
            .hideDependentLines());

    public static final Quest GLOWSTONE_NANITES = CHAPTER.quest("glowstone_nanites", q -> q
            .at(10.5, 8.5)
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Glowstone))
            .dependsOn(UvChapter.NANO_FORGE, UU_AMPLIFIER_BUCKET));

    public static final Quest SPACE_DRONE_MK2 = CHAPTER.quest("space_drone_mk2", q -> q
            .at(0.5, 10.5)
            .task(Task.item(GTOItems.SPACE_DRONE_MK2))
            .dependsOn(UvChapter.ORICHALCUM_INGOT)
            .hideDependencyLines());

    public static final Quest INGOT_FIELD_SHAPE = CHAPTER.quest("ingot_field_shape", q -> q
            .at(2.5, 6.5)
            .desc(d -> d.line("冷却成热锭的效率略微高些"))
            .task(Task.item(GTOItems.INGOT_FIELD_SHAPE))
            .dependsOn(UvChapter.PLASMA_CONDENSER)
            .optional());

    public static final Quest ORICHALCUM_NANITES = CHAPTER.quest("orichalcum_nanites", q -> q
            .at(8.5, 4.5)
            .desc(d -> d.line("在主机里放一个{山铜}蜂群以将{PCB工厂}提升至2级", LIGHT_PURPLE, GOLD))
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Orichalcum))
            .dependsOn(UvChapter.PCB_FACTORY, UU_AMPLIFIER_BUCKET));

    public static final Quest WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE = CHAPTER.quest("welcome_to_the_extreme_high_voltage", q -> q
            .at(-3.5, -1)
            .title("欢迎来到{极高压}阶段", DARK_RED)
            .subtitle("{你需要天马行空般的想象力}", DARK_RED)
            .desc(d -> d
                    .line("以安普洛为主，你可能需要注意自己的超能硅岩余量")
                    .line()
                    .line("来吧，既然体验过{生}{物}{线}，你应该也不会再对{光}{学}{线}产生畏惧了！", GREEN, DARK_GREEN, GREEN, GOLD, YELLOW, GOLD)
                    .line()
                    .line("简而言之，就是漫长的自动化样板过程..."))
            .task(Task.item(GTMachines.HULL[GTValues.UHV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(UvChapter.POLYETHERETHERKETONE_BUCKET, UvChapter.AMPROSIUM_INGOT));

    public static final Quest PERIODICALLY_POLED_LITHIUM_NIOBATE_BOULE = CHAPTER.quest("periodically_poled_lithium_niobate_boule", q -> q
            .at(5, 1)
            .task(Task.item(GTOItems.PERIODICALLY_POLED_LITHIUM_NIOBATE_BOULE))
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .hideDependencyLines());

    public static final Quest LASER = CHAPTER.quest("laser", q -> q
            .at(5, 2.5)
            .title("激光")
            .subtitle("样板大户")
            .task(
                    Task.item(GTOItems.NON_LINEAR_OPTICAL_LENS),
                    Task.item(GTOItems.LOW_FREQUENCY_LASER),
                    Task.item(GTOItems.MEDIUM_FREQUENCY_LASER),
                    Task.item(GTOItems.HIGH_FREQUENCY_LASER))
            .dependsOn(PERIODICALLY_POLED_LITHIUM_NIOBATE_BOULE));

    public static final Quest HIGH_ENERGY_LASER_PURIFICATION_UNIT = CHAPTER.quest("high_energy_laser_purification_unit", q -> q
            .at(4.5, 8.5)
            .task(Task.item(MultiBlockB.HIGH_ENERGY_LASER_PURIFICATION_UNIT))
            .dependsOn(LASER)
            .hideDependencyLines());

    public static final Quest ELECTRIC_EQUILIBRIUM_WATER_BUCKET = CHAPTER.quest("electric_equilibrium_water_bucket", q -> q
            .at(6.5, 8.5)
            .icon(GTOMaterials.ElectricEquilibriumWater.getBucket())
            .subtitle("透镜神力")
            .desc(d -> d.line("非常难的自动化"))
            .task(Task.item(GTOMaterials.ElectricEquilibriumWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(HIGH_ENERGY_LASER_PURIFICATION_UNIT));

    public static final Quest THULIUM_HEXABORIDE_CERAMICS_FLAKE = CHAPTER.quest("thulium_hexaboride_ceramics_flake", q -> q
            .at(6.5, 1)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.ThuliumHexaborideCeramics))
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .hideDependencyLines());

    public static final Quest HEXANITROHEXAAXAISOWURTZITANE_DUST = CHAPTER.quest("hexanitrohexaaxaisowurtzitane_dust", q -> q
            .at(4.5, 10.5)
            .subtitle("谁设计的？！")
            .desc(d -> d.line("超级恶心"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.Hexanitrohexaaxaisowurtzitane))
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .hideDependencyLines());

    public static final Quest ENDER_CRYSTAL = CHAPTER.quest("ender_crystal", q -> q
            .at(2.5, 8.5)
            .desc(d -> d.line("第一颗需要结晶而成"))
            .task(Task.item(GTOItems.ENDER_CRYSTAL))
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .hideDependencyLines());

    public static final Quest LIQUIDCRYSTALKEVLAR_BUCKET = CHAPTER.quest("liquidcrystalkevlar_bucket", q -> q
            .at(8.5, 1)
            .icon(GTOMaterials.LiquidCrystalKevlar.getBucket())
            .task(Task.item(GTOMaterials.LiquidCrystalKevlar.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE)
            .hideDependencyLines());

    public static final Quest KEVLAR_PLATE = CHAPTER.quest("kevlar_plate", q -> q
            .at(8.5, 2.5)
            .desc(d -> d.line("享受你的产线吧"))
            .task(Task.item(TagPrefix.plate, GTOMaterials.Kevlar))
            .dependsOn(LIQUIDCRYSTALKEVLAR_BUCKET));

    public static final Quest OPTICAL_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("optical_printed_circuit_board", q -> q
            .at(6.5, 4.5)
            .subtitle("闪闪发光")
            .task(Task.item(GTOItems.OPTICAL_PRINTED_CIRCUIT_BOARD))
            .dependsOn(KEVLAR_PLATE, ORICHALCUM_NANITES));

    public static final Quest OPTICAL_DATA_STICK = CHAPTER.quest("optical_data_stick", q -> q
            .at(6.5, 2.5)
            .subtitle("寄存器的存储已经不能满足我们的CPU！")
            .desc(d -> d.line("太空组装太慢？试试更换{动力模块}，它可以增加配方处理并行！", AQUA))
            .task(Task.item(GTOItems.OPTICAL_DATA_STICK))
            .dependsOn(OPTICAL_PRINTED_CIRCUIT_BOARD, THULIUM_HEXABORIDE_CERAMICS_FLAKE));

    public static final Quest RUTHERFORDIUM_AMPROSIUM_BOULE = CHAPTER.quest("rutherfordium_amprosium_boule", q -> q
            .at(10.5, 12.5)
            .desc(d -> d.line("并不是之前的单晶硅的上位，只能制作特定的晶圆"))
            .task(Task.item(GTOItems.RUTHERFORDIUM_AMPROSIUM_BOULE))
            .dependsOn(UvChapter.NEUTRONIUM_BOULE));

    public static final Quest RUTHERFORDIUM_AMPROSIUM_WAFER = CHAPTER.quest("rutherfordium_amprosium_wafer", q -> q
            .at(8.5, 12.5)
            .task(Task.item(GTOItems.RUTHERFORDIUM_AMPROSIUM_WAFER))
            .dependsOn(RUTHERFORDIUM_AMPROSIUM_BOULE));

    public static final Quest RAW_PHOTON_CARRYING_WAFER = CHAPTER.quest("raw_photon_carrying_wafer", q -> q
            .at(8.5, 10.5)
            .desc(d -> d.line("好多透镜！"))
            .task(Task.item(GTOItems.RAW_PHOTON_CARRYING_WAFER))
            .dependsOn(RUTHERFORDIUM_AMPROSIUM_WAFER, ENGRAVING_LASER_PLANT));

    public static final Quest NM_WAFER = CHAPTER.quest("nm_wafer", q -> q
            .at(6.5, 12.5)
            .task(Task.item(GTOItems.NM_WAFER))
            .dependsOn(RUTHERFORDIUM_AMPROSIUM_WAFER, ENGRAVING_LASER_PLANT)
            .hideDependentLines());

    public static final Quest UHV_ENERGY_INPUT_HATCH = CHAPTER.quest("uhv_energy_input_hatch", q -> q
            .at(-0.5, -1)
            .desc(d -> d.line("啊哈，升压也不是什么麻烦事！"))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UHV]))
            .dependsOn(WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE, ENERGETIC_NETHERITE_DUST, UvChapter.ORICHALCUM_INGOT, NM_WAFER));

    public static final Quest UHV_FUSION_REACTOR = CHAPTER.quest("uhv_fusion_reactor", q -> q
            .at(3.5, -1)
            .subtitle("蓝莓甜甜圈")
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UHV]))
            .dependsOn(UHV_ENERGY_INPUT_HATCH));

    public static final Quest LEMURITE_BUCKET = CHAPTER.quest("lemurite_bucket", q -> q
            .at(6, -1)
            .icon(GTOMaterials.Lemurite.getBucket())
            .task(Task.item(GTOMaterials.Lemurite.getBucket()).optional(), Task.checkmark())
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest QUANTANIUM_INGOT = CHAPTER.quest("quantanium_ingot", q -> q
            .at(8, -1)
            .subtitle("无法想象的物质")
            .desc(d -> d.line("UEV阶段的主要材料"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Quantanium))
            .dependsOn(LEMURITE_BUCKET));

    public static final Quest SUPERHEAVY_ELEMENT = CHAPTER.quest("superheavy_element", q -> q
            .at(3.5, 1)
            .title("超重元素")
            .desc(d -> d.line("为什么它们不会衰变？！别问，问就是史蒂夫的魔法"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Dubnium), Task.item(TagPrefix.ingot, GTMaterials.Seaborgium))
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest SMD_CAPACITOR_OPTICAL = CHAPTER.quest("smd_capacitor_optical", q -> q
            .at(3.5, 2.5)
            .subtitle("熟悉的五人组")
            .task(
                    Task.item(GTOItems.SMD_CAPACITOR_OPTICAL),
                    Task.item(GTOItems.SMD_DIODE_OPTICAL),
                    Task.item(GTOItems.SMD_RESISTOR_OPTICAL),
                    Task.item(GTOItems.SMD_INDUCTOR_OPTICAL),
                    Task.item(GTOItems.SMD_TRANSISTOR_OPTICAL))
            .dependsOn(SUPERHEAVY_ELEMENT));

    public static final Quest QUICKSILVER_BUCKET = CHAPTER.quest("quicksilver_bucket", q -> q
            .at(3.5, -3)
            .icon(GTOMaterials.Quicksilver.getBucket())
            .desc(d -> d.line("又是魔法！你可能需要进行{盖亚魂循环}", link(Magic2Chapter.GAIA_SPIRIT_CYCLE)))
            .task(Task.item(GTOMaterials.Quicksilver.getBucket()).optional(), Task.checkmark())
            .dependsOn(UHV_FUSION_REACTOR, Magic2Chapter.GUARDIAN_OF_GAIA_2_0));

    public static final Quest INTEGRAL_FRAMEWORK_UHV = CHAPTER.quest("integral_framework_uhv", q -> q
            .at(5.5, -3)
            .subtitle("升级你的大型机器#5")
            .desc(d -> d
                    .line("你马上要将家里的所有机器都换{巨构}了（在做出{光}{学}{处}{理}{器}{主}{机}之后），而巨构们{不需要}整体框架。", GOLD, GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW, GOLD, RED)
                    .line()
                    .line("这也是你最后一次看到这个系列任务了，不过整体框架仍然还会在你后面的旅程中出现"))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_UHV))
            .dependsOn(QUICKSILVER_BUCKET));

    public static final Quest RARE_EARTH_CENTRIFUGAL = CHAPTER.quest("rare_earth_centrifugal", q -> q
            .at(10.5, 1)
            .desc(d -> d
                    .line("摆脱冗长的稀土产线，拥抱稀土离心")
                    .line()
                    .line("可以进行{稀土}章节余下的处理了", link(RareDustChapter.Q_75569B55)))
            .task(Task.item(MultiBlockA.RARE_EARTH_CENTRIFUGAL))
            .dependsOn(UevChapter.WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE));

    public static final Quest TRANSCENDING_MATTER_BUCKET = CHAPTER.quest("transcending_matter_bucket", q -> q
            .at(-3.5, 2.5)
            .icon(GTOMaterials.TranscendingMatter.getBucket())
            .desc(d -> d.line("必须制作盖亚魂循环，请参考{魔法线}", link(Magic2Chapter.GAIA_SPIRIT_CYCLE)))
            .task(Task.item(GTOMaterials.TranscendingMatter.getBucket()).optional(), Task.checkmark())
            .dependsOn(Magic2Chapter.GAIA_SPIRIT_CYCLE));

    public static final Quest LARGE_ALGAE_FARM = CHAPTER.quest("large_algae_farm", q -> q
            .at(10.5, 2.5)
            .icon(MultiBlockI.LARGE_ALGAE_FARM)
            .task(Task.item(MultiBlockI.LARGE_ALGAE_FARM), Task.item(GTAEMachines.ALGAE_ACCESS_HATCH))
            .dependsOn(SMD_CAPACITOR_OPTICAL)
            .hideDependencyLines());

    public static final Quest BOSE_EINSTEIN_COOLING_CONTAINER = CHAPTER.quest("bose_einstein_cooling_container", q -> q
            .at(4.5, 6.5)
            .task(Task.item(GTOItems.BOSE_EINSTEIN_COOLING_CONTAINER))
            .dependsOn(UvChapter.NANO_FORGE)
            .hideDependencyLines());

    public static final Quest FRANCIUM_DUST = CHAPTER.quest("francium_dust", q -> q
            .at(10.5, 10.5)
            .desc(d -> d.line("享受你的产线吧x10086"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Francium))
            .dependsOn(FULLERENE_DUST)
            .hideDependencyLines());

    public static final Quest OPTICAL_WAFER = CHAPTER.quest("optical_wafer", q -> q
            .at(8.5, 8.5)
            .task(Task.item(GTOItems.OPTICAL_WAFER))
            .dependsOn(UvChapter.CYCLOPARAPHENYLENE_BUCKET, GLOWSTONE_NANITES, RAW_PHOTON_CARRYING_WAFER, FRANCIUM_DUST));

    public static final Quest SIMPLE_OPTICAL_SOC = CHAPTER.quest("simple_optical_soc", q -> q
            .at(6.5, 6.5)
            .subtitle("用于贴在核心表层")
            .task(Task.item(GTOItems.SIMPLE_OPTICAL_SOC))
            .dependsOn(OPTICAL_WAFER, ELECTRIC_EQUILIBRIUM_WATER_BUCKET));

    public static final Quest OPTICAL_PROCESSING_CORE = CHAPTER.quest("optical_processing_core", q -> q
            .at(4.5, 4.5)
            .subtitle("激光之力，小子")
            .desc(d -> d.line("为什么原料这么多？！"))
            .task(Task.item(GTOItems.OPTICAL_PROCESSING_CORE))
            .dependsOn(
                    SMD_CAPACITOR_OPTICAL,
                    LASER,
                    SIMPLE_OPTICAL_SOC,
                    OPTICAL_PRINTED_CIRCUIT_BOARD,
                    OPTICAL_DATA_STICK,
                    FULLERENE_DUST,
                    BOSE_EINSTEIN_COOLING_CONTAINER));

    public static final Quest OPTICAL_PROCESSOR = CHAPTER.quest("optical_processor", q -> q
            .at(2.5, 4.5)
            .title("{光}{学}{处}{理}{器}", GOLD, YELLOW, GOLD, YELLOW, GOLD)
            .task(Task.item(GTOItems.OPTICAL_PROCESSOR))
            .dependsOn(OPTICAL_PROCESSING_CORE));

    public static final Quest OPTICAL_PROCESSING_UNIT_CLUSTER = CHAPTER.quest("optical_processing_unit_cluster", q -> q
            .at(0.5, 4.5)
            .title("{光}{学}{处}{理}{器}{集}{群}", GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW, GOLD)
            .task(Task.item(GTOItems.OPTICAL_ASSEMBLY))
            .dependsOn(OPTICAL_PROCESSOR));

    public static final Quest OPTICAL_PROCESSOR_SUPER_COMPUTER = CHAPTER.quest("optical_processor_super_computer", q -> q
            .at(-1.5, 4.5)
            .title("{光}{学}{处}{理}{器}{超}{级}{计}{算}{机}", GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW)
            .task(Task.item(GTOItems.OPTICAL_COMPUTER))
            .dependsOn(OPTICAL_PROCESSING_UNIT_CLUSTER));

    public static final Quest STELLAR_FORGE = CHAPTER.quest("stellar_forge", q -> q
            .at(2.5, 12.5)
            .subtitle("比冶炼更加极端高温！")
            .desc(d -> d
                    .line("机器配方{无视}电压等级，只要功率足够即可", YELLOW)
                    .line()
                    .line("如果{供能不足}时运行会炸，范围真的很大！", RED))
            .task(Task.item(MultiBlockA.STELLAR_FORGE))
            .dependsOn(UvChapter.BIOACTIVITY_PROCESSOR_CONTROLLER, NM_WAFER));

    public static final Quest NAQUADRIA_CHARGE = CHAPTER.quest("naquadria_charge", q -> q
            .at(2.5, 10.5)
            .desc(d -> d
                    .line("真的可以引爆，爆炸半径约12个区块，会炸开基岩")
                    .line()
                    .line("{别手残在基地里点了！！！}", GRAY.italic()))
            .task(Task.item(GTOBlocks.NAQUADRIA_CHARGE))
            .dependsOn(HEXANITROHEXAAXAISOWURTZITANE_DUST, STELLAR_FORGE));

    public static final Quest ENDERIUM_INGOT = CHAPTER.quest("enderium_ingot", q -> q
            .at(0.5, 8.5)
            .desc(d -> d.line("自增产线"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Enderium))
            .dependsOn(NAQUADRIA_CHARGE, ENDER_CRYSTAL, UvChapter.PLASMA_CONDENSER));

    public static final Quest NEURAL_MATRIX = CHAPTER.quest("neural_matrix", q -> q
            .at(6.5, 14.5)
            .desc(d -> d.line("四级数据载体"))
            .task(Task.item(GTOItems.NEURAL_MATRIX))
            .dependsOn(ZpmChapter.WETWARE_PROCESSOR_MAINFRAME, UvChapter.BIOWARE_PRINTED_CIRCUIT_BOARD, NM_WAFER));

    public static final Quest BIO_DATA_ACCESS_HATCH = CHAPTER.quest("bio_data_access_hatch", q -> q
            .at(4.5, 14.5)
            .desc(d -> d.line("你必须使用本访问仓或等级更高的访问仓才可以放入神经矩阵"))
            .task(Task.item(ExResearchMachines.BIO_DATA_ACCESS_HATCH))
            .dependsOn(NEURAL_MATRIX, ZpmChapter.WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest TITANSTEEL_INGOT = CHAPTER.quest("titansteel_ingot", q -> q
            .at(-1.5, 10.5)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TitanSteel))
            .dependsOn(UU_AMPLIFIER_BUCKET, SPACE_DRONE_MK2));

    public static final Quest TITANSTEEL_COIL_BLOCK = CHAPTER.quest("titansteel_coil_block", q -> q
            .at(-1.5, 8.5)
            .task(Task.item(GTOBlocks.TITANSTEEL_COIL_BLOCK))
            .dependsOn(TITANSTEEL_INGOT));

    public static final Quest ENDERITE_INGOT = CHAPTER.quest("enderite_ingot", q -> q
            .at(-1.5, 6.5)
            .desc(d -> d.line("温馨小提示，空间站能够做的配方比整体框架高一级"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Enderite))
            .dependsOn(ENDERIUM_INGOT, TITANSTEEL_COIL_BLOCK, SpaceStationChapter.ORBITAL_SMELTING_FACILITY));

    public static final Quest OPTICAL_PROCESSOR_CONTROLLER = CHAPTER.quest("optical_processor_controller", q -> q
            .at(-3.5, 4.5)
            .title("{光}{学}{处}{理}{器}{主}{机}", GOLD, YELLOW, GOLD, YELLOW, GOLD, YELLOW, GOLD)
            .subtitle("史诗级跃进")
            .desc(d -> d
                    .line("做出这个意味着你解锁了绝大多数巨构，你即将迎来一次生产力的最终飞跃！")
                    .line()
                    .line("好好享受{增幅仓}带来的超巨大产能吧！！！", GREEN)
                    .line()
                    .line("{做出它实际上需要你在UEV阶段有一定的进度，但是由于后面没写所以全放这了}", GRAY.italic()))
            .task(Task.item(GTOItems.OPTICAL_MAINFRAME))
            .dependsOn(OPTICAL_PROCESSOR_SUPER_COMPUTER, ENDERITE_INGOT, TRANSCENDING_MATTER_BUCKET, SpaceStationChapter.SPACE_DRONE_DOCK)
            .ignoreRewardBlocking());

    public static final Quest SPACE_DRONE_MK3 = CHAPTER.quest("space_drone_mk3", q -> q
            .at(-3.5, 10.5)
            .task(Task.item(GTOItems.SPACE_DRONE_MK3))
            .dependsOn(TITANSTEEL_INGOT));

    public static final Quest COMPONENT_ASSEMBLY_LINE = CHAPTER.quest("component_assembly_line", q -> q
            .at(-5.5, 8.5)
            .desc(d -> d.line("终于，部件装配它回来了！"))
            .task(Task.item(MultiBlockA.COMPONENT_ASSEMBLY_LINE))
            .dependsOn(SPACE_DRONE_MK3, ENDERITE_INGOT, OPTICAL_PROCESSING_CORE)
            .hideDependencyLines());

    public static final Quest SMART_SIFTERING_HUB = CHAPTER.quest("smart_siftering_hub", q -> q
            .at(-5.5, 10.5)
            .task(Task.item(MultiBlockH.SMART_FILTERING_HUB))
            .dependsOn(SPACE_DRONE_MK3));

    public static final Quest GIANT_SINTERING_ARRAY = CHAPTER.quest("giant_sintering_array", q -> q
            .at(-3.5, 8.5)
            .task(Task.item(MultiBlockH.GIANT_SINTERING_ARRAY))
            .dependsOn(TITANSTEEL_INGOT));

    public static final Quest EXTREME_ELECTRIC_FURNACE = CHAPTER.quest("extreme_electric_furnace", q -> q
            .at(-3.5, 12.5)
            .task(Task.item(MultiBlockC.EXTREME_ELECTRIC_FURNACE))
            .dependsOn(TITANSTEEL_INGOT));

    public static final Quest MOLECULAR_OSCILLATION_DEHYDRATOR = CHAPTER.quest("molecular_oscillation_dehydrator", q -> q
            .at(-5.5, 12.5)
            .task(Task.item(MultiBlockB.MOLECULAR_OSCILLATION_DEHYDRATOR))
            .dependsOn(SPACE_DRONE_MK3));

    public static final Quest MANTLE_CRUSHER = CHAPTER.quest("mantle_crusher", q -> q
            .at(8.5, 14.5)
            .task(Task.item(MultiBlockH.MANTLE_CRUSHER))
            .dependsOn(NEURAL_MATRIX));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(UvChapter.PLASMA_CONDENSER, 0.5, 6.5))
                .link(QuestLink.of(AeChapter.ME_EXTEND_PATTERN_BUFFER_ULTRA, -3.5, -3))
                .link(QuestLink.of(UvChapter.ORICHALCUM_INGOT, -0.5, -3))
                .link(QuestLink.of(SpaceStationChapter.ORBITAL_SMELTING_FACILITY, -3.5, 6.5))
                .link(QuestLink.of(UvChapter.NANO_FORGE, 10.5, 6.5))
                .link(QuestLink.of(UvChapter.PCB_FACTORY, 10.5, 4.5))
                .link(QuestLink.of(UevChapter.WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE, 10.5, -1))
                .link(QuestLink.of(UtilChapter.ME_WILDCARD_PATTERN_BUFFER_2, 1.5, -3))
                .link(QuestLink.of(SpaceStationChapter.SPACE_DRONE_DOCK, -5.5, 6.5)));
    }
}
