package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import appeng.core.definitions.AEItems;

import com.hepdd.gtmthings.data.CustomItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class MvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("mv", c -> c
            .title("5. 中压(MV)")
            .subtitle("Tier2-MV")
            .icon(MultiBlockD.PROCESSING_PLANT)
            .hideQuestsUntilDepsVisible()
            .autofocus(MvChapter.WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE));

    public static final Quest POLYVINYL_CHLORIDE_BUCKET = CHAPTER.quest("polyvinyl_chloride_bucket", q -> q
            .at(4, 7.5)
            .icon(GTMaterials.PolyvinylChloride.getBucket())
            .desc(d -> d.line("经典配方，不用多说，你也应该搭建至少一条小产线"))
            .task(Task.item(GTMaterials.PolyvinylChloride.getBucket()).optional(), Task.checkmark())
            .dependsOn(LvChapter.BLACK_BOX_REACTOR)
            .hideDependencyLines());

    public static final Quest MV_EMITTER = CHAPTER.quest("mv_emitter", q -> q
            .at(-10, -1.5)
            .task(Task.item(GTItems.EMITTER_MV), Task.item(GTItems.SENSOR_MV))
            .dependsOn(LvChapter.GOOD_ELECTRONIC_CIRCUIT, LvChapter.ALUMINIUM_INGOT));

    public static final Quest WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE = CHAPTER.quest("welcome_to_the_medium_voltage_stage", q -> q
            .at(-13, 3)
            .title("欢迎来到{中压}阶段", AQUA)
            .subtitle("{你需要天马行空般的想象力}", AQUA)
            .desc(d -> d
                    .line("欢迎来到{中压}阶段！", AQUA)
                    .line()
                    .line("也许你在之前的阶段都是手动搬运材料进行{“手工作坊式”}合成", YELLOW)
                    .line()
                    .line("进入中压时代，你将必须开始搭建几条必要的{产线}，否则你不久后会为了反复制备某一大批量需要的材料而{非常头疼}", YELLOW, RED)
                    .line()
                    .line("{进阶集成电路}是你这个阶段的最终目标", GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.MV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(LvChapter.ALUMINIUM_INGOT));

    public static final Quest ALUMINIUM_HYDROXIDE_DUST = CHAPTER.quest("aluminium_hydroxide_dust", q -> q
            .at(-3, -4.5)
            .desc(d -> d
                    .line("陶瓷材料，是用天然或合成化合物经过成形和高温烧结制成的一类无机非金属材料。具有高熔点、高硬度、高耐磨性、耐氧化等优点。")
                    .line()
                    .line("在现阶段，它能作为不错的{集成电路基板}为我们所用", YELLOW)
                    .line()
                    .line("为了烧制一炉最简单的{氧化铝陶瓷}，你需要制备一些{氢氧化铝粉}，不过仅仅一炉是不够你发展太远的，你应该尽早搭建起{陶瓷产线}", YELLOW, GOLD, YELLOW))
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminiumHydroxide, 70L))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LUBRICANT_BUCKET = CHAPTER.quest("lubricant_bucket", q -> q
            .at(-1.5, 0)
            .icon(GTMaterials.Lubricant.getBucket())
            .desc(d -> d.line("{润滑油}对提升切割速度至关重要，也是后续大型发电机组持续运转所必需的供应液体", GOLD))
            .task(Task.item(GTMaterials.Lubricant.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_EXTRUDER = CHAPTER.quest("mv_extruder", q -> q
            .at(-16.5, 2)
            .subtitle("它的春天尚未到来")
            .desc(d -> d
                    .line("由于电压等级不够高，MV压模机依然不能处理该电压级别的金属{铝}", GOLD)
                    .line()
                    .line("实用性大不如{液化炉}和{流体固化器}", GOLD, GOLD)
                    .line()
                    .line("{下个阶段也许会迎来它的春天？}", YELLOW))
            .task(Task.item(GTMachines.EXTRUDER[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest ELECTRICITY_SOURCES_IN_THE_MEDIUM_VOLTAGE = CHAPTER.quest("electricity_sources_in_the_medium_voltage", q -> q
            .at(-13, 5.25)
            .icon(GTMachines.GAS_TURBINE[GTValues.MV])
            .title("中压时代的电力来源")
            .desc(d -> d.line("鉴于它们的效率，热力发电机与蒸汽轮机或许不再是最优选择"))
            .task(
                    Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.MV]).optional(),
                    Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.MV]).optional(),
                    Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.MV]).optional(),
                    Task.item(GTMachines.COMBUSTION[GTValues.MV]).optional(),
                    Task.item(GTMachines.GAS_TURBINE[GTValues.MV]).optional(),
                    Task.item(GTMachines.STEAM_TURBINE[GTValues.MV]).optional())
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE, LvChapter.GOOD_ELECTRONIC_CIRCUIT)
            .optional());

    public static final Quest MV_BATTERY_BUFFER_16X = CHAPTER.quest("mv_battery_buffer_16x", q -> q
            .at(-16.5, 3)
            .desc(d -> d.line("中压时代唯一的蓄电方式"))
            .task(Task.item(GTMachines.BATTERY_BUFFER_16[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_BENDER = CHAPTER.quest("mv_bender", q -> q
            .at(-14.5, 3)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTMachines.BENDER[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_ELECTROLYZER = CHAPTER.quest("mv_electrolyzer", q -> q
            .at(-13, 0.75)
            .desc(d -> d
                    .line("{进阶电解机}解锁了更多获取铝、硅、锂、氟等重要单质的配方", GOLD)
                    .line()
                    .line("用它来大量获取{铝}和{硅粉}吧", GOLD, GOLD))
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE, LvChapter.GOOD_ELECTRONIC_CIRCUIT));

    public static final Quest MV_WIREMILL = CHAPTER.quest("mv_wiremill", q -> q
            .at(-15.5, 2)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTMachines.WIREMILL[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_CLUSTER = CHAPTER.quest("mv_cluster", q -> q
            .at(-14.5, 4)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.CLUSTER[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_LAMINATOR = CHAPTER.quest("mv_laminator", q -> q
            .at(-15.5, 3)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.LAMINATOR[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_ROLLING = CHAPTER.quest("mv_rolling", q -> q
            .at(-14.5, 2)
            .desc(d -> d
                    .line("比低压机器更高效的机器")
                    .line()
                    .line("可以辊轧{双层板}和{致密板}", GOLD, GOLD))
            .task(Task.item(GTOMachines.ROLLING[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_LOOM = CHAPTER.quest("mv_loom", q -> q
            .at(-15.5, 4)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.LOOM[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_ARC_GENERATOR = CHAPTER.quest("mv_arc_generator", q -> q
            .at(-16.5, 4)
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.ARC_GENERATOR[GTValues.MV]))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_SUPERCONDUCTOR = CHAPTER.quest("mv_superconductor", q -> q
            .at(-13, -1.5)
            .title("MV超导")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.EnergeticAlloy))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MV_COMPONENTS = CHAPTER.quest("mv_components", q -> q
            .at(-11, 3)
            .title("{MV}部件", MV)
            .desc(d -> d.line("同样的，在正式开始制造{MV}阶段的机器前，我建议你大量制作马达等部件", MV))
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_MV),
                    Task.item(GTItems.ELECTRIC_PISTON_MV),
                    Task.item(GTItems.CONVEYOR_MODULE_MV),
                    Task.item(GTItems.ROBOT_ARM_MV))
            .reward(Reward.item(GTItems.ELECTRIC_MOTOR_MV, 4))
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE, LvChapter.GOOD_ELECTRONIC_CIRCUIT));

    public static final Quest MIXING_SMELTING_COOLING = CHAPTER.quest("mixing_smelting_cooling", q -> q
            .at(-10, 4.5)
            .title("搅拌、烧炼、冷却")
            .subtitle("中压时代最重要的单方块机器#2")
            .desc(d -> d
                    .line("{进阶搅拌机}能制作更复杂的合金粉末，{进阶浸洗机}用来冷却热锭", GOLD, GOLD)
                    .line()
                    .line("这些合金对推进你的格雷事业大有用处！"))
            .task(Task.item(GTMachines.MIXER[GTValues.MV]), Task.item(GTMachines.CHEMICAL_BATH[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest VANADIUM_STEEL_INGOT = CHAPTER.quest("vanadium_steel_ingot", q -> q
            .at(-3.5, 6)
            .subtitle("超坚硬、超耐磨")
            .desc(d -> d
                    .line("制造涡轮转子、锯片、工具的优良材料")
                    .line()
                    .line("重要的是你可以开始建立{AE存储系统}！", YELLOW))
            .task(Task.item(TagPrefix.ingot, GTMaterials.VanadiumSteel))
            .dependsOn(MIXING_SMELTING_COOLING)
            .hideDependencyLines());

    public static final Quest MV_CUTTER = CHAPTER.quest("mv_cutter", q -> q
            .at(-3.5, 4.5)
            .desc(d -> d.line("有了{进阶切割机}你才能切割{硅块}、{单晶硅}和{晶圆}", GOLD, GOLD, GOLD, GOLD))
            .task(Task.item(GTMachines.CUTTER[GTValues.MV]))
            .dependsOn(VANADIUM_STEEL_INGOT));

    public static final Quest MV_ASSEMBLER = CHAPTER.quest("mv_assembler", q -> q
            .at(-10, 1.5)
            .subtitle("中压时代最重要的单方块机器#1")
            .desc(d -> d
                    .line("复仇者，再次集结！")
                    .line()
                    .line("中压时代开始，各种造价昂贵的{多方块机器}开始担当主角", GOLD)
                    .line()
                    .line("它们能够{大幅节省你的零件造价}、{生产大量资源}或{提供极高的生产速度}！", YELLOW, YELLOW, YELLOW))
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest REACTION_FURNACE = CHAPTER.quest("reaction_furnace", q -> q
            .at(-1.5, -2.5)
            .subtitle("高温化学反应")
            .desc(d -> d.line("陶瓷线的重要机器！加装附属模块后支持{并行}", YELLOW))
            .task(Task.item(MultiBlockB.REACTION_FURNACE))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest ALUMINA_CERAMIC_DUST = CHAPTER.quest("alumina_ceramic_dust", q -> q
            .at(-1.5, -4.5)
            .subtitle("高温反应")
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10))
            .dependsOn(REACTION_FURNACE, ALUMINIUM_HYDROXIDE_DUST));

    public static final Quest ELECTRONIC_GRADE_SILICON_DUST = CHAPTER.quest("electronic_grade_silicon_dust", q -> q
            .at(-3.5, 0)
            .desc(d -> d
                    .line("普通的{硅粉}纯度不足以制造单晶硅，你需要{电子级硅粉}", GOLD, GOLD)
                    .line()
                    .line("单单为了它而搭建一条专门的产线是非常有必要的"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.ElectronicGradeSilicon, 32))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest COOLANT_LIQUID_BUCKET = CHAPTER.quest("coolant_liquid_bucket", q -> q
            .at(-8, 3)
            .icon(GTOMaterials.CoolantLiquid.getBucket())
            .task(Task.item(GTOMaterials.CoolantLiquid.getBucket()).optional(), Task.checkmark())
            .dependsOn(MV_ASSEMBLER));

    public static final Quest KANTHAL_INGOT = CHAPTER.quest("kanthal_ingot", q -> q
            .at(-8, 4.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Kanthal))
            .dependsOn(MIXING_SMELTING_COOLING, COOLANT_LIQUID_BUCKET));

    public static final Quest KANTHAL_COIL_BLOCK = CHAPTER.quest("kanthal_coil_block", q -> q
            .at(-6.5, 4.5)
            .desc(d -> d
                    .line("许多配方有{线圈等级}的要求。有了{坎塔尔合金线圈}你才能烧制出{硅锭}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("{液态铜}是无法通过提取机提取的，如果你还没有{液化炉}，是时候造一台了", GOLD, GOLD))
            .task(Task.item(GTBlocks.COIL_KANTHAL))
            .dependsOn(KANTHAL_INGOT, LvChapter.LIQUEFACTION_FURNACE));

    public static final Quest SILICON_INGOT = CHAPTER.quest("silicon_ingot", q -> q
            .at(-5, 3)
            .desc(d -> d.line("硅是非常重要的单质之一，你需要它制作各种晶圆，同时它也正式开启了AE章节"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Silicon))
            .dependsOn(KANTHAL_COIL_BLOCK, COOLANT_LIQUID_BUCKET));

    public static final Quest TRANSISTOR = CHAPTER.quest("transistor", q -> q
            .at(7.5, -4.5)
            .task(Task.item(GTItems.TRANSISTOR))
            .dependsOn(MV_ASSEMBLER, SILICON_INGOT)
            .hideDependencyLines());

    public static final Quest PROCESSING_PLANT = CHAPTER.quest("processing_plant", q -> q
            .at(-5.25, -1.75)
            .icon(MultiBlockD.PROCESSING_PLANT)
            .subtitle("百分之一亿的贵得要死#2")
            .desc(d -> d
                    .line("单方块机器的强力增幅器，相当于给单方块机器{降能耗}的同时提供了{额外的并行和速度}", YELLOW, YELLOW)
                    .line()
                    .line("非常昂贵，需要上千个{铝锭}。拿出你所有的底蕴来招待它!", AQUA)
                    .line()
                    .line("不过如果现在做，你可能只能承担{LV}级别的整体框架，建议在{HV}阶段解锁{MV}整体框架后开始大量制作", LV, HV, MV)
                    .line()
                    .line("强烈建议将基地中的单方块机器都替换成通用工厂，享受{并行}，{能源仓}与{加速仓}带来的产能提升！", GOLD, GOLD, GOLD)
                    .line()
                    .line("=====")
                    .line()
                    .line("{机制介绍}：部分多方块机器包含{整体框架}，它会限制你所能运行的{配方等级}，其会取能源仓提供的电压和整体框架电压的{最小值}", AQUA, GOLD.bold(), YELLOW, RED)
                    .line()
                    .line("通用工厂在其中还更为特殊，你还需要保证主机中放入的{小机器}的电压与{能源仓}提供的电压{相等}", GOLD, GOLD, GREEN))
            .task(
                    Task.item(MultiBlockD.PROCESSING_PLANT),
                    Task.anyOf(1, GTOBlocks.INTEGRAL_FRAMEWORK_EV, GTOBlocks.INTEGRAL_FRAMEWORK_HV, GTOBlocks.INTEGRAL_FRAMEWORK_IV, GTOBlocks.INTEGRAL_FRAMEWORK_LUV, GTOBlocks.INTEGRAL_FRAMEWORK_LV, GTOBlocks.INTEGRAL_FRAMEWORK_MAX, GTOBlocks.INTEGRAL_FRAMEWORK_MV, GTOBlocks.INTEGRAL_FRAMEWORK_OPV, GTOBlocks.INTEGRAL_FRAMEWORK_UEV, GTOBlocks.INTEGRAL_FRAMEWORK_UHV, GTOBlocks.INTEGRAL_FRAMEWORK_UIV, GTOBlocks.INTEGRAL_FRAMEWORK_ULV, GTOBlocks.INTEGRAL_FRAMEWORK_UV, GTOBlocks.INTEGRAL_FRAMEWORK_UXV, GTOBlocks.INTEGRAL_FRAMEWORK_ZPM).title("任意整体框架"))
            .reward(Reward.item(GTOMachines.ACCELERATE_HATCH[GTValues.LV]))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest SILICON_CRYSTAL_SEED = CHAPTER.quest("silicon_crystal_seed", q -> q
            .at(-3.5, 3)
            .subtitle("眼看它长成参天大树")
            .task(Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon))
            .dependsOn(SILICON_INGOT, MV_CUTTER));

    public static final Quest GREENHOUSE = CHAPTER.quest("greenhouse", q -> q
            .at(-8.25, -0.25)
            .desc(d -> d.line("无限作物"))
            .task(Task.item(MultiBlockD.GREENHOUSE))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest COMPONENT_ASSEMBLER = CHAPTER.quest("component_assembler", q -> q
            .at(-8.25, -1.75)
            .subtitle("百分之一亿的贵得要死#1")
            .desc(d -> d
                    .line("批量部件制作，比普通组装省25%材料。{部件装配机外壳}影响可以可以制作的{部件等级}。", GOLD, YELLOW)
                    .line()
                    .line("想办法自动化执行部装配方！")
                    .line()
                    .line("不过{部件装配机外壳}在此阶段{过于昂贵}，可以考虑到达{HV}阶段{提升生产力}之后再进行制作", GOLD, RED, HV, YELLOW))
            .task(Task.item(MultiBlockC.COMPONENT_ASSEMBLER))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest MV_FLUID_DRILLING_RIG = CHAPTER.quest("mv_fluid_drilling_rig", q -> q
            .at(-6.75, -3.25)
            .desc(d -> d
                    .line("无限石油")
                    .line()
                    .line("你可以用它来挖掘基岩流体，包括但不限于石油，原油，天然气，稀有气体等")
                    .line()
                    .line("可以查看探矿仪的EMI来查询基岩流体信息"))
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV]))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(VANADIUM_STEEL_INGOT)
            .hideDependencyLines());

    public static final Quest PYROLYSE_OVEN = CHAPTER.quest("pyrolyse_oven", q -> q
            .at(-6.75, -0.25)
            .desc(d -> d.line("生产生物资源"))
            .task(Task.item(GTMultiMachines.PYROLYSE_OVEN))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest MV_FLUID_REGULATOR = CHAPTER.quest("mv_fluid_regulator", q -> q
            .at(-8, 1.5)
            .task(Task.item(GTItems.FLUID_REGULATOR_MV))
            .dependsOn(MV_ASSEMBLER));

    public static final Quest CRYSTALLIZATION_CHAMBER = CHAPTER.quest("crystallization_chamber", q -> q
            .at(-6, 1.5)
            .subtitle("结更大的晶体")
            .desc(d -> d
                    .line("顾名思义，用来结晶的机器，{单晶硅}便是一种单质晶体", GOLD)
                    .line()
                    .line("当熔融的单质硅凝固时，硅原子以金刚石晶格排列成晶核，其晶核长成晶面取向相同的晶粒，形成单晶硅。")
                    .line()
                    .line("这个过程一般称为{拉晶}", YELLOW)
                    .line()
                    .line("晶体形成一般需要{晶种}和{纯净饱和溶液}，有时还需要{保护气}", GOLD, GOLD, GOLD))
            .task(Task.item(MultiBlockG.CRYSTALLIZATION_CHAMBER))
            .dependsOn(MV_FLUID_REGULATOR));

    public static final Quest NURTURE_CRYSTAL_FOR_A_TIME_USE = CHAPTER.quest("nurture_crystal_for_a_time_use", q -> q
            .at(-3.5, 1.5)
            .title("养晶一时，用晶千日")
            .desc(d -> d
                    .line("哼哈……")
                    .line()
                    .line("你会不断地……不断地……用到它……"))
            .task(Task.item(GTItems.SILICON_BOULE))
            .dependsOn(ELECTRONIC_GRADE_SILICON_DUST, CRYSTALLIZATION_CHAMBER, SILICON_CRYSTAL_SEED));

    public static final Quest SILICON_WAFER = CHAPTER.quest("silicon_wafer", q -> q
            .at(-1.5, 1.5)
            .subtitle("一切芯片的起点")
            .task(Task.item(GTItems.SILICON_WAFER))
            .dependsOn(LUBRICANT_BUCKET, NURTURE_CRYSTAL_FOR_A_TIME_USE));

    public static final Quest ANY_CAPACITORS = CHAPTER.quest("any_capacitors", q -> q
            .at(6, 2.5)
            .icon(GTItems.CAPACITOR)
            .task(Task.tag(CustomTags.CAPACITORS, 1).title("电容"))
            .dependsOn(MV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest MV_MINER = CHAPTER.quest("mv_miner", q -> q
            .at(-10, 0)
            .task(Task.item(GTMachines.MINER[GTValues.MV]))
            .dependsOn(MV_EMITTER, MV_ASSEMBLER)
            .hideDependentLines());

    public static final Quest ALUMINA_CERAMIC_ROUGH_BLANK = CHAPTER.quest("alumina_ceramic_rough_blank", q -> q
            .at(0, -4.5)
            .subtitle("压制成型")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.AluminaCeramic))
            .dependsOn(ALUMINA_CERAMIC_DUST));

    public static final Quest MV_LASER_ENGRAVER = CHAPTER.quest("mv_laser_engraver", q -> q
            .at(0.5, 1.5)
            .desc(d -> d
                    .line("MV阶段刻蚀芯片所需的透镜都能使用某些光谱带宽极窄的天然宝石制成")
                    .line()
                    .line("至于更高级的芯片大多都需要通过{染料浸洗}以得到窄带宽的透镜", YELLOW))
            .task(Task.item(GTMachines.LASER_ENGRAVER[GTValues.MV]))
            .dependsOn(SILICON_WAFER));

    public static final Quest MV_AIR_SCRUBBER = CHAPTER.quest("mv_air_scrubber", q -> q
            .at(-9.5, 3)
            .desc(d -> d
                    .line("如{上一章节}所述，它拥有更高的净化能力和净化范围", link(LvChapter.LV_AIR_SCRUBBER))
                    .line()
                    .line("下一阶段或许可以{完全自动化掏灰}？", YELLOW))
            .task(Task.item(GTMachines.AIR_SCRUBBER[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest DIAMOND_LENS = CHAPTER.quest("diamond_lens", q -> q
            .at(2.5, 1.5)
            .task(Task.item(TagPrefix.lens, GTMaterials.Diamond))
            .dependsOn(MV_LASER_ENGRAVER));

    public static final Quest CPU_CHIP = CHAPTER.quest("cpu_chip", q -> q
            .at(4.5, 1.5)
            .desc(d -> d.line("处理器序列的核心芯片"))
            .task(Task.item(GTItems.CENTRAL_PROCESSING_UNIT))
            .dependsOn(DIAMOND_LENS));

    public static final Quest SAPPHIRE_LENS = CHAPTER.quest("sapphire_lens", q -> q
            .at(0.5, 4.5)
            .task(Task.item(TagPrefix.lens, GTMaterials.Sapphire))
            .dependsOn(MV_LASER_ENGRAVER));

    public static final Quest ULPIC_CHIP = CHAPTER.quest("ulpic_chip", q -> q
            .at(2.5, 4.5)
            .task(Task.item(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT))
            .dependsOn(SAPPHIRE_LENS));

    public static final Quest MV_ENERGY_INPUT_HATCH = CHAPTER.quest("mv_energy_input_hatch", q -> q
            .at(4.5, 4.5)
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.MV]))
            .dependsOn(ULPIC_CHIP));

    public static final Quest STAINLESS_STEEL_INGOT = CHAPTER.quest("stainless_steel_ingot", q -> q
            .at(7.5, 4.5)
            .desc(d -> d
                    .line("高压时代的基础金属材料")
                    .line()
                    .line("用量非常巨大，需要提前进行囤积式生产！")
                    .line()
                    .line("{别忘了双仓升压机制，这次你需要给电力高炉安装两个MV能源仓来升压到HV}", GRAY.italic()))
            .task(Task.item(TagPrefix.ingot, GTMaterials.StainlessSteel))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 8))
            .dependsOn(MV_ENERGY_INPUT_HATCH));

    public static final Quest RUBY_LENS = CHAPTER.quest("ruby_lens", q -> q
            .at(0.5, -0.5)
            .task(Task.item(TagPrefix.lens, GTMaterials.Ruby))
            .dependsOn(MV_LASER_ENGRAVER));

    public static final Quest ILC_CHIP = CHAPTER.quest("ilc_chip", q -> q
            .at(0.5, -2.5)
            .desc(d -> d.line("集成电路序列的核心芯片"))
            .task(Task.item(GTItems.INTEGRATED_LOGIC_CIRCUIT))
            .dependsOn(RUBY_LENS));

    public static final Quest BASIC_INTEGRATED_CIRCUIT = CHAPTER.quest("basic_integrated_circuit", q -> q
            .at(2.5, -2.5)
            .title("{基础集成电路}", GOLD)
            .subtitle("集成电路#1")
            .desc(d -> d
                    .line("你开始制作更高级的电路序列——{集成电路}", YELLOW)
                    .line()
                    .line("从该序列开始，LV电路不再需要ULV电路合成，而是基于刻蚀成的芯片{直接合成}", YELLOW)
                    .line()
                    .line("{基础集成电路}是最初级的{集成电路}", GOLD, YELLOW))
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_LV))
            .dependsOn(ILC_CHIP));

    public static final Quest GOOD_INTEGRATED_CIRCUIT = CHAPTER.quest("good_integrated_circuit", q -> q
            .at(4.5, -2.5)
            .title("{优质集成电路}", GOLD)
            .subtitle("集成电路#2")
            .desc(d -> d.line("{优质集成电路}是高一级的{集成电路}", GOLD, YELLOW))
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_MV))
            .dependsOn(BASIC_INTEGRATED_CIRCUIT));

    public static final Quest EMERALD_LENS = CHAPTER.quest("emerald_lens", q -> q
            .at(2.5, -0.5)
            .task(Task.item(TagPrefix.lens, GTMaterials.Emerald))
            .dependsOn(MV_LASER_ENGRAVER));

    public static final Quest RAM_CHIP = CHAPTER.quest("ram_chip", q -> q
            .at(4.5, -0.5)
            .desc(d -> d
                    .line("现代计算机均基于冯·诺依曼架构，所以要构成最小计算机系统，RAM芯片是必需的组件")
                    .line()
                    .line("这也预示着之后的大量{高级主机}与{计算机}都需要它作为组件参与合成", GOLD, GOLD)
                    .line()
                    .line("做得再多也不嫌多"))
            .task(Task.item(GTItems.RANDOM_ACCESS_MEMORY))
            .dependsOn(EMERALD_LENS));

    public static final Quest Q_2386117F = CHAPTER.quest("q_2386117f", q -> q
            .at(-6.75, -1.75)
            .icon(GTOMachines.WIRELESS_INPUT_HATCH_2[GTValues.MV])
            .desc(d -> d
                    .line("在{中压}阶段你可以开始使用{无线能源}减轻你的布线负担！", AQUA, YELLOW)
                    .line()
                    .line("不过{无线能源仓}需要等到{HV}阶段才会解锁，你目前可以使用{无线能源接收器}给电池箱和小机器供能", GOLD, HV, GOLD)
                    .line()
                    .line("前往{无线电网}章节查看如何搭建你的无线电网系统", link(WirelessEnergyChapter.WIRELESS_ENERGY_BINDING_TOOL))
                    .line()
                    .line("{可惜不是无限能源时代}", GRAY.italic().strike()))
            .task(Task.checkmark().title("{无线能源时代}", LIGHT_PURPLE))
            .dependsOn(ULPIC_CHIP)
            .hideDependencyLines());

    public static final Quest VACUUM_INTERFACE = CHAPTER.quest("vacuum_interface", q -> q
            .at(-3.5, -1.75)
            .subtitle("也许你有抽真空的需求...")
            .desc(d -> d
                    .line("将能提供真空的小机器贴着接口放即可")
                    .line()
                    .line("{你问如何给多方块机器提供热量？你前面一定已经用过了}", GRAY.italic()))
            .task(Task.item(GTOMachines.VACUUM_INTERFACE))
            .dependsOn(PROCESSING_PLANT)
            .optional());

    public static final Quest MV_COMPONENT_SUBSIDY = CHAPTER.quest("mv_component_subsidy", q -> q
            .at(-11.5, 0.5)
            .icon(GTItems.ELECTRIC_MOTOR_MV)
            .title("MV部件补助")
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(GTItems.ELECTRIC_MOTOR_MV, 32),
                    Reward.item(GTItems.CONVEYOR_MODULE_MV, 8),
                    Reward.item(GTItems.ELECTRIC_PISTON_MV, 8),
                    Reward.item(GTItems.ROBOT_ARM_MV, 8),
                    Reward.item(GTItems.ELECTRIC_PUMP_MV, 8))
            .normalSubsidy(Reward.item(GTItems.ELECTRIC_MOTOR_MV, 24))
            .dependsOn(MV_ASSEMBLER)
            .optional().hideUntilDepsVisible());

    public static final Quest STAINLESS_STEEL_SUBSIDY = CHAPTER.quest("stainless_steel_subsidy", q -> q
            .at(9, 4.5)
            .icon(TagPrefix.ingot, GTMaterials.StainlessSteel)
            .title("不锈钢补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 320L))
            .normalSubsidy(Reward.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 56))
            .dependsOn(STAINLESS_STEEL_INGOT)
            .optional().hideUntilDepsVisible());

    public static final Quest MV_FORCE_FIELD_GENERATOR = CHAPTER.quest("mv_force_field_generator", q -> q
            .at(-10, -3)
            .icon(GTItems.FIELD_GENERATOR_MV)
            .title("{MV}力场发生器...?", MV)
            .desc(d -> d.line("不是所有标明电压的物品都可以在对应电压内制作，{MV}力场发生器就是一个例子，它需要你在{HV}阶段制作{真空冷冻机}之后才能够制作", MV, HV, GOLD))
            .task(Task.checkmark())
            .dependsOn(MV_EMITTER));

    public static final Quest PLASTIC_SUBSIDY_RENEWED = CHAPTER.quest("plastic_subsidy_renewed", q -> q
            .at(-1.5, 3)
            .icon(TagPrefix.plate, GTMaterials.Polyethylene)
            .title("塑料补助再续")
            .desc(d -> d.line("后面还要用到大量的塑料，我再送你一点"))
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 256L))
            .dependsOn(SILICON_WAFER)
            .optional().hideUntilDepsVisible());

    public static final Quest POLYVINYL_CHLORIDE_PLATE = CHAPTER.quest("polyvinyl_chloride_plate", q -> q
            .at(5.5, 7.5)
            .task(Task.item(TagPrefix.plate, GTMaterials.PolyvinylChloride))
            .dependsOn(POLYVINYL_CHLORIDE_BUCKET));

    public static final Quest ENERGETIC_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("energetic_photovoltaic_power_station", q -> q
            .at(-5.25, -0.25)
            .subtitle("免费能源")
            .desc(d -> d.line("有些昂贵...值得吗？"))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_ENERGETIC))
            .dependsOn(SILICON_WAFER)
            .optional().hideDependencyLines());

    public static final Quest AE_CIRCUIT_BOARD = CHAPTER.quest("ae_circuit_board", q -> q
            .at(-5, 4.5)
            .title("AE电路板")
            .desc(d -> d.line("AE之路真正的开始，别忘了去{应用能源}章节看看你现在能做什么", link(AeChapter.AE_CIRCUIT_BOARD)))
            .task(
                    Task.item(AEItems.SILICON_PRINT),
                    Task.item(AEItems.CALCULATION_PROCESSOR_PRINT),
                    Task.item(AEItems.ENGINEERING_PROCESSOR_PRINT),
                    Task.item(AEItems.LOGIC_PROCESSOR_PRINT))
            .dependsOn(SILICON_INGOT)
            .requireAny());

    public static final Quest NOTHING_FROM_NOTHING = CHAPTER.quest("nothing_from_nothing", q -> q
            .at(-10, 7.5)
            .icon(TagPrefix.dust, GTMaterials.Diorite)
            .title("无中生有")
            .desc(d -> d
                    .line("挖矿还是太麻烦了，有没有一种能够无中生有产出材料的方式呢？")
                    .line()
                    .line("有的兄弟有的，GTO保留了从各种石头粉中离心产出所需材料的配方")
                    .line()
                    .line("简单举一个例子，{闪长岩粉}{离心}可以产出黏土粉，而{黏土粉}再经一步{电解}则会产出这个阶段大量需要的{铝}和{硅}", GOLD, YELLOW, GOLD, YELLOW, AQUA, AQUA)
                    .line()
                    .line("虽然产出慢了一点，但只要挂机一会，就会有大量的材料可以用啦"))
            .task(Task.checkmark(), Task.item(TagPrefix.dust, GTMaterials.Diorite).optional())
            .dependsOn(WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest GREAT_POWER_BRINGS_MIRACLES_EASY = CHAPTER.quest("great_power_brings_miracles_easy", q -> q
            .at(0, -5.5)
            .icon(MultiBlockG.ISOSTATIC_PRESS)
            .title("大力出奇迹{（简单）}", GREEN)
            .desc(d -> d
                    .line("简单模式陶瓷无需压制成型")
                    .line()
                    .line("{史蒂夫的手可以解决一切}", GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(ALUMINA_CERAMIC_DUST, UtilChapter.Q_056EC4E6)
            .optional().hideUntilDepsVisible());

    public static final Quest ALUMINA_CERAMIC_BLOCK = CHAPTER.quest("alumina_ceramic_block", q -> q
            .at(1.5, -4.5)
            .subtitle("烧结")
            .desc(d -> d
                    .line("现阶段你可以临时起一台{电力高炉}", GOLD)
                    .line()
                    .line("等到下一阶段，我们有更专业的机器替代高炉")
                    .line()
                    .line("{是什么呢？是什么呢？好难猜呢……}", GRAY))
            .task(Task.item(TagPrefix.block, GTOMaterials.AluminaCeramic))
            .dependsOn(ALUMINA_CERAMIC_ROUGH_BLANK, GREAT_POWER_BRINGS_MIRACLES_EASY)
            .requireAny());

    public static final Quest ALUMINA_CERAMIC_BRICK = CHAPTER.quest("alumina_ceramic_brick", q -> q
            .at(3, -4.5)
            .subtitle("切块")
            .task(Task.item(GTOTagPrefix.BRICK, GTOMaterials.AluminaCeramic, 9))
            .dependsOn(ALUMINA_CERAMIC_BLOCK));

    public static final Quest ALUMINA_CERAMIC_FLAKE = CHAPTER.quest("alumina_ceramic_flake", q -> q
            .at(5.25, -4.5)
            .subtitle("切片")
            .desc(d -> d.line("36片具有优秀电学性能的基板，对于{进阶集成电路}是必需的", GOLD))
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.AluminaCeramic, 36))
            .dependsOn(ALUMINA_CERAMIC_BRICK));

    public static final Quest ADVANCED_INTEGRATED_CIRCUIT = CHAPTER.quest("advanced_integrated_circuit", q -> q
            .at(7.5, -2.5)
            .title("{进阶集成电路}", GOLD)
            .subtitle("Knock Knock")
            .desc(d -> d
                    .line("集成电路的最终版本")
                    .line()
                    .line("你已经敲开了高压时代的大门！"))
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_HV))
            .reward(Reward.item(GTItems.INTEGRATED_CIRCUIT_HV, 2), Reward.item(QuestExternalRefs.FarmersDelightItems.HAMBURGER, 8))
            .dependsOn(ALUMINA_CERAMIC_FLAKE, GOOD_INTEGRATED_CIRCUIT, RAM_CHIP, TRANSISTOR));

    public static final Quest MV_CIRCUIT_ASSEMBLER = CHAPTER.quest("mv_circuit_assembler", q -> q
            .at(7.5, -0.5)
            .desc(d -> d.line("有了进阶电路组装机，你可以制造处理器序列电路了"))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.MV]))
            .dependsOn(ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest PLASTIC_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("plastic_printed_circuit_board", q -> q
            .at(6, 0.5)
            .desc(d -> d
                    .line("制作微型处理器系列需要用到的基板，所有微型处理器都会用到它")
                    .line()
                    .line("HV开始用量非常大，准备一条产线总不是坏事"))
            .task(Task.item(GTItems.PLASTIC_CIRCUIT_BOARD))
            .dependsOn(ALUMINA_CERAMIC_FLAKE)
            .hideDependencyLines());

    public static final Quest MICROPROCESSOR = CHAPTER.quest("microprocessor", q -> q
            .at(7.5, 1.5)
            .title("{微型处理器}", YELLOW)
            .desc(d -> d
                    .line("更便宜的{LV电路}和{MV电路}", GOLD, GOLD)
                    .line()
                    .line("更高级的{HV电路}组装需要{超净间}", GOLD, GOLD)
                    .line()
                    .line("=====")
                    .line()
                    .line("{微型处理器}是你接触到的{第一种}{处理器电路}", LIGHT_PURPLE, AQUA, YELLOW)
                    .line()
                    .line("从{HV}开始，每个阶段的电路板都固定为{4个}电路，分别为{处理器}，{处理器集群}，{处理器超级计算机}和{处理器主机}", HV, GREEN, GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("处理器主机通常是一个阶段中最重要的目标之一，它将为你打开下一个阶段的大门"))
            .task(Task.item(GTItems.MICROPROCESSOR_LV), Task.item(GTItems.PROCESSOR_MV))
            .dependsOn(CPU_CHIP, ANY_CAPACITORS, MV_CIRCUIT_ASSEMBLER, PLASTIC_PRINTED_CIRCUIT_BOARD));

    public static final Quest MV_BEDROCK_ORE_MINER = CHAPTER.quest("mv_bedrock_ore_miner", q -> q
            .at(-5.25, -3.25)
            .desc(d -> d
                    .line("无限矿石")
                    .line()
                    .line("你可以用它来挖掘基岩矿脉，在实体矿脉中相对稀少的矿石在基岩矿脉中占比将{大大增加}（如金矿石）", GOLD)
                    .line()
                    .line("可以查看探矿仪的EMI来查询矿脉信息（基岩矿脉与实体矿脉共享）"))
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.MV]))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(ADVANCED_INTEGRATED_CIRCUIT, MV_EMITTER)
            .hideDependencyLines());

    public static final Quest STEEL_LARGE_BOILER = CHAPTER.quest("steel_large_boiler", q -> q
            .at(-8.25, -3.25)
            .task(Task.item(GTMultiMachines.LARGE_BOILER_STEEL))
            .dependsOn(ADVANCED_INTEGRATED_CIRCUIT)
            .hideDependencyLines());

    public static final Quest CHIP_SUBSIDY = CHAPTER.quest("chip_subsidy", q -> q
            .at(9, -2.5)
            .icon(GTItems.INTEGRATED_CIRCUIT_HV)
            .title("芯片补助")
            .desc(d -> d.line("从HV开始不再有额外补助。用自己的双手创造一切吧！"))
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_LV, 32),
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_MV, 16),
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_HV, 16))
            .normalSubsidy(
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_LV, 8),
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_MV, 4),
                    Reward.item(GTItems.INTEGRATED_CIRCUIT_HV, 2))
            .dependsOn(ADVANCED_INTEGRATED_CIRCUIT)
            .optional().hideUntilDepsVisible());

    public static final Quest PROGRAMMABLE_HATCH_EASY = CHAPTER.quest("programmable_hatch_easy", q -> q
            .at(-6.5, 7.5).minWidth(250)
            .icon(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE)
            .title("{可编程仓室}{（简单）}", YELLOW, GREEN)
            .desc(d -> d
                    .line("其具体用途请前往{HV阶段同名任务}查看", link(HvChapter.PROGRAMMABLE_CHAMBER_ROOM))
                    .line()
                    .line("简单难度下，虚拟物品的解锁条件提前")
                    .line()
                    .line("请无视HV阶段的解锁条件"))
            .task(
                    Task.item(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE),
                    Task.item(GTOMachines.PROGRAMMABLEC_HATCH[GTValues.MV]).optional(),
                    Task.item(CustomItems.PROGRAMMABLE_COVER).optional())
            .dependsOn(ADVANCED_INTEGRATED_CIRCUIT, UtilChapter.Q_056EC4E6)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.DRIVE, -3.5, 7.5))
                .link(QuestLink.of(HvChapter.WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, 7.5, 7.5))
                .link(QuestLink.of(LvChapter.LIQUEFACTION_FURNACE, -8, 6))
                .link(QuestLink.of(AeChapter.ME_SIMPLE_PATTERN_BUFFER_EASY, -6.5, 6))
                .link(QuestLink.of(LvChapter.LARGE_STEAM_CIRCUIT_ASSEMBLER, -5, 7.5))
                .link(QuestLink.of(AeChapter.WORK_BLOCK_FOR_ORIGINAL_SYNTHESIS_RECIPES, -5, 6)));
    }
}
