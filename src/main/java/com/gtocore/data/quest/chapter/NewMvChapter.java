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

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import appeng.core.definitions.AEItems;

import com.hepdd.gtmthings.data.CustomItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewMvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_mv", c -> c
            .title("5. 中压时代(MV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.MV]));

    public static final Quest MV_COMPONENTS = CHAPTER.quest("mv_components", q -> q
            .at(6, 4)
            .title("MV 部件")
            .desc(d -> d.line("同样的，在正式开始制造{MV}阶段的机器前，我建议你大量制作马达等部件", MV))
            .icon(GTItems.ELECTRIC_MOTOR_MV)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_MV, 2),
                    Task.item(GTItems.ELECTRIC_PISTON_MV, 2),
                    Task.item(GTItems.ELECTRIC_PUMP_MV),
                    Task.item(GTItems.CONVEYOR_MODULE_MV, 2),
                    Task.item(GTItems.ROBOT_ARM_MV, 2),
                    Task.item(GTItems.FLUID_REGULATOR_MV, 2))
            .scaledReward(GTItems.ELECTRIC_MOTOR_MV, 2)
            .dependsOn(NewLvChapter.MV_CIRCUIT));

    public static final QuestLink MV_COMPONENTS_COPY_1 = CHAPTER.copy(MV_COMPONENTS, 16, -4);

    public static final QuestLink MV_COMPONENTS_COPY_2 = CHAPTER.copy(MV_COMPONENTS, 10, -6);

    public static final QuestLink MV_COMPONENTS_COPY_3 = CHAPTER.copy(MV_COMPONENTS, 20, 4);

    public static final QuestLink MV_COMPONENTS_COPY_4 = CHAPTER.copy(MV_COMPONENTS, 20, 12);

    public static final QuestLink MV_COMPONENTS_COPY_5 = CHAPTER.copy(MV_COMPONENTS, 10, 2);

    public static final QuestLink MV_COMPONENTS_COPY_6 = CHAPTER.copy(MV_COMPONENTS, 10, 12);

    public static final QuestLink MV_COMPONENTS_COPY_7 = CHAPTER.copy(MV_COMPONENTS, -4, 16);

    public static final QuestLink MV_COMPONENTS_COPY_8 = CHAPTER.copy(MV_COMPONENTS, 2, 20);

    public static final Quest MV_ELECTROLYZER = CHAPTER.quest("mv_electrolyzer", q -> q
            .at(14, -6)
            .title("MV 电解机")
            .desc(d -> d
                    .line("{进阶电解机}解锁了更多获取铝、硅、锂、氟等重要单质的配方", GOLD)
                    .line()
                    .line("用它来大量获取{铝}和{硅粉}吧", GOLD, GOLD))
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Ruby, 12)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final QuestLink MV_ELECTROLYZER_COPY_1 = CHAPTER.copy(MV_ELECTROLYZER, 22, 6);

    public static final Quest CHROMIUM = CHAPTER.quest("chromium", q -> q
            .at(14, -4)
            .title("铬")
            .task(Task.item(TagPrefix.dust, GTMaterials.Chromium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 4)
            .dependsOn(MV_ELECTROLYZER));

    public static final QuestLink CHROMIUM_COPY_1 = CHAPTER.copy(CHROMIUM, 6, 12);

    public static final QuestLink CHROMIUM_COPY_2 = CHAPTER.copy(CHROMIUM, -4, 14);

    public static final Quest MV_MIXER = CHAPTER.quest("mv_mixer", q -> q
            .at(16, -2)
            .title("MV 搅拌机")
            .subtitle("中压时代最重要的单方块机器#2")
            .desc(d -> d
                    .line("{进阶搅拌机}能制作更复杂的合金粉末，{进阶浸洗机}用来冷却热锭", GOLD, GOLD)
                    .line()
                    .line("这些合金对推进你的格雷事业大有用处！"))
            .task(Task.item(GTMachines.MIXER[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 2)
            .dependsOn(MV_COMPONENTS_COPY_1));

    public static final QuestLink MV_MIXER_COPY_1 = CHAPTER.copy(MV_MIXER, 22, -2);

    public static final QuestLink MV_MIXER_COPY_2 = CHAPTER.copy(MV_MIXER, 6, 10);

    public static final QuestLink MV_MIXER_COPY_3 = CHAPTER.copy(MV_MIXER, -2, 12);

    public static final QuestLink MV_MIXER_COPY_4 = CHAPTER.copy(MV_MIXER, 12, -4);

    public static final Quest MV_DISTILLERY = CHAPTER.quest("mv_distillery", q -> q
            .at(10, -4)
            .title("MV 蒸馏室")
            .task(Task.item(GTMachines.DISTILLERY[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Lazurite, 8)
            .dependsOn(MV_COMPONENTS_COPY_2));

    public static final Quest DISTILLED_WATER = CHAPTER.quest("distilled_water", q -> q
            .at(10, -2)
            .title("蒸馏水")
            .task(Task.item(GTMaterials.DistilledWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Lazurite, 4)
            .dependsOn(MV_DISTILLERY));

    public static final QuestLink DISTILLED_WATER_COPY_1 = CHAPTER.copy(DISTILLED_WATER, 12, 12);

    public static final Quest COOLANT = CHAPTER.quest("coolant", q -> q
            .at(12, -2)
            .title("冷却液")
            .task(Task.item(GTOMaterials.CoolantLiquid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.CoolantLiquid.getBucket(), 1)
            .dependsOn(DISTILLED_WATER, MV_MIXER_COPY_4));

    public static final QuestLink COOLANT_COPY_1 = CHAPTER.copy(COOLANT, 20, 6);

    public static final Quest KANTHAL_DUST = CHAPTER.quest("kanthal_dust", q -> q
            .at(14, -2)
            .title("坎塔尔粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Kanthal, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Iron, 4)
            .dependsOn(CHROMIUM, MV_MIXER));

    public static final Quest KANTHAL = CHAPTER.quest("kanthal", q -> q
            .at(14, 0)
            .milestone()
            .title("坎塔尔锭")
            .icon(TagPrefix.ingot, GTMaterials.Kanthal)
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.MV]), Task.item(TagPrefix.ingot, GTMaterials.Kanthal, 32))
            .scaledReward(TagPrefix.ingot, GTMaterials.Kanthal, 8)
            .dependsOn(KANTHAL_DUST, COOLANT));

    public static final Quest MV_ASSEMBLER = CHAPTER.quest("mv_assembler", q -> q
            .at(22, 12)
            .title("MV 组装机")
            .subtitle("中压时代最重要的单方块机器#1")
            .desc(d -> d
                    .line("复仇者，再次集结！")
                    .line()
                    .line("中压时代开始，各种造价昂贵的{多方块机器}开始担当主角", GOLD)
                    .line()
                    .line("它们能够{大幅节省你的零件造价}、{生产大量资源}或{提供极高的生产速度}！", YELLOW, YELLOW, YELLOW))
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.MV]))
            .scaledReward(TagPrefix.foil, GTMaterials.Aluminium, 16)
            .dependsOn(MV_COMPONENTS_COPY_4));

    public static final QuestLink MV_ASSEMBLER_COPY_1 = CHAPTER.copy(MV_ASSEMBLER, -4, -4);

    public static final QuestLink MV_ASSEMBLER_COPY_2 = CHAPTER.copy(MV_ASSEMBLER, -2, 8);

    public static final QuestLink MV_ASSEMBLER_COPY_3 = CHAPTER.copy(MV_ASSEMBLER, 18, 10);

    public static final Quest LIQUEFACTION_FURNACE = CHAPTER.quest("liquefaction_furnace", q -> q
            .at(4, 0)
            .milestone()
            .title("液化炉")
            .subtitle("给提取机放个假？")
            .desc(d -> d
                    .line("GTO新增的机器，{主线流程中必须制造的机器}。你可以用这个大家伙将固态物质{高温液化}为液态形式", YELLOW, YELLOW)
                    .line()
                    .line("由于熔点过高，{流体提取机}无法做到很多事情，需要{液化炉}出马", GOLD, GOLD)
                    .line()
                    .line("加装附属模块后支持{并行}", YELLOW))
            .task(Task.item(MultiBlockB.LIQUEFACTION_FURNACE))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 8)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest MV_LATHE = CHAPTER.quest("mv_lathe", q -> q
            .at(8, 6)
            .title("MV 车床")
            .task(Task.item(GTMachines.LATHE[GTValues.MV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Sapphire, 4)
            .dependsOn(MV_COMPONENTS));

    public static final Quest KANTHAL_COIL = CHAPTER.quest("kanthal_coil", q -> q
            .at(22, 10)
            .title("坎塔尔线圈")
            .desc(d -> d
                    .line("许多配方有{线圈等级}的要求。有了{坎塔尔合金线圈}你才能烧制出{硅锭}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("{液态铜}是无法通过提取机提取的，如果你还没有{液化炉}，是时候造一台了", GOLD, GOLD))
            .task(Task.item(GTBlocks.COIL_KANTHAL, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Kanthal, 8)
            .dependsOn(MV_ASSEMBLER, KANTHAL, LIQUEFACTION_FURNACE));

    public static final Quest SILICON_DIOXIDE = CHAPTER.quest("silicon_dioxide", q -> q
            .at(24, 8)
            .title("二氧化硅粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.SiliconDioxide, 27))
            .scaledReward(Items.SAND, 32)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest SILICON_DUST = CHAPTER.quest("silicon_dust", q -> q
            .at(22, 8)
            .title("硅粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Silicon, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.SiliconDioxide, 12)
            .dependsOn(SILICON_DIOXIDE, MV_ELECTROLYZER_COPY_1));

    public static final QuestLink SILICON_DUST_COPY_1 = CHAPTER.copy(SILICON_DUST, 14, 4);

    public static final Quest VANADIUM_STEEL = CHAPTER.quest("vanadium_steel", q -> q
            .at(8, 10)
            .title("钒钢")
            .subtitle("超坚硬、超耐磨")
            .desc(d -> d
                    .line("制造涡轮转子、锯片、工具的优良材料")
                    .line()
                    .line("重要的是你可以开始建立{AE存储系统}！", YELLOW))
            .task(Task.item(TagPrefix.ingot, GTMaterials.VanadiumSteel, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.VanadiumSteel, 2)
            .dependsOn(CHROMIUM_COPY_1, MV_MIXER_COPY_2));

    public static final Quest SILICON = CHAPTER.quest("silicon", q -> q
            .at(20, 8)
            .title("硅锭")
            .desc(d -> d.line("硅是非常重要的单质之一，你需要它制作各种晶圆，同时它也正式开启了AE章节"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Silicon, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.Silicon, 2)
            .dependsOn(SILICON_DUST, KANTHAL_COIL, COOLANT_COPY_1));

    public static final QuestLink SILICON_COPY_1 = CHAPTER.copy(SILICON, 14, 12);

    public static final Quest CUTTER_MV = CHAPTER.quest("cutter_mv", q -> q
            .at(10, 10)
            .title("MV 切割机")
            .desc(d -> d.line("有了{进阶切割机}你才能切割{硅块}、{单晶硅}和{晶圆}", GOLD, GOLD, GOLD, GOLD))
            .task(Task.item(GTMachines.CUTTER[GTValues.MV]))
            .scaledReward(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon, 1)
            .dependsOn(MV_COMPONENTS_COPY_6, VANADIUM_STEEL));

    public static final Quest SILICON_SEED = CHAPTER.quest("silicon_seed", q -> q
            .at(12, 10)
            .title("硅晶种")
            .subtitle("眼看它长成参天大树")
            .icon(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon)
            .task(Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon))
            .scaledReward(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon, 1)
            .dependsOn(CUTTER_MV, SILICON_COPY_1, DISTILLED_WATER_COPY_1));

    public static final Quest CRYSTALLIZATION_CHAMBER = CHAPTER.quest("crystallization_chamber", q -> q
            .at(8, 0)
            .milestone()
            .title("结晶器")
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
            .scaledReward(TagPrefix.dust, GTMaterials.GalliumArsenide, 1)
            .dependsOn(MV_COMPONENTS));

    public static final Quest TRICHLOROSILANE = CHAPTER.quest("trichlorosilane", q -> q
            .at(14, 2)
            .title("三氯硅烷")
            .task(Task.item(GTOMaterials.Trichlorosilane.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 2)
            .dependsOn(SILICON_DUST_COPY_1));

    public static final Quest ELECTRONIC_GRADE_SILICON_DUST = CHAPTER.quest("electronic_grade_silicon_dust", q -> q
            .at(12, 2)
            .title("电子级硅粉")
            .desc(d -> d
                    .line("普通的{硅粉}纯度不足以制造单晶硅，你需要{电子级硅粉}", GOLD, GOLD)
                    .line()
                    .line("单单为了它而搭建一条专门的产线是非常有必要的"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.ElectronicGradeSilicon, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.ElectronicGradeSilicon, 8)
            .dependsOn(TRICHLOROSILANE));

    public static final Quest ELECTRONIC_GRADE_SILICON_INGOT = CHAPTER.quest("electronic_grade_silicon_ingot", q -> q
            .at(12, 4)
            .title("电子级硅锭")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.ElectronicGradeSilicon, 32))
            .scaledReward(TagPrefix.ingot, GTOMaterials.ElectronicGradeSilicon, 8)
            .dependsOn(ELECTRONIC_GRADE_SILICON_DUST));

    public static final Quest ELECTRONIC_GRADE_SILICON = CHAPTER.quest("electronic_grade_silicon", q -> q
            .at(12, 6)
            .title("电子级硅液")
            .task(Task.item(GTOMaterials.ElectronicGradeSilicon.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.GalliumArsenide, 1)
            .dependsOn(ELECTRONIC_GRADE_SILICON_INGOT, LIQUEFACTION_FURNACE));

    public static final Quest MV_BREWERY = CHAPTER.quest("mv_brewery", q -> q
            .at(10, 4)
            .title("MV 酿造机")
            .task(Task.item(GTMachines.BREWERY[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Talc, 8)
            .dependsOn(MV_COMPONENTS_COPY_5));

    public static final Quest LUBRICANT = CHAPTER.quest("lubricant", q -> q
            .at(10, 6)
            .title("润滑油")
            .desc(d -> d.line("{润滑油}对提升切割速度至关重要，也是后续大型发电机组持续运转所必需的供应液体", GOLD))
            .task(Task.item(GTMaterials.Lubricant.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Talc, 8)
            .dependsOn(MV_BREWERY));

    public static final Quest SILICON_WAFER = CHAPTER.quest("silicon_wafer", q -> q
            .at(12, 8)
            .milestone()
            .title("硅晶圆")
            .subtitle("一切芯片的起点")
            .desc(d -> d
                    .line("哼哈……")
                    .line()
                    .line("你会不断地……不断地……用到它……"))
            .icon(GTItems.SILICON_WAFER)
            .task(Task.item(GTItems.SILICON_BOULE), Task.item(GTItems.SILICON_WAFER, 4))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(CRYSTALLIZATION_CHAMBER, SILICON_SEED, ELECTRONIC_GRADE_SILICON, LUBRICANT, CUTTER_MV));

    public static final Quest SIFTER_MV = CHAPTER.quest("sifter_mv", q -> q
            .at(20, 2)
            .title("MV 筛选机")
            .task(Task.item(GTMachines.SIFTER[GTValues.MV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Emerald, 4)
            .dependsOn(MV_COMPONENTS_COPY_3));

    public static final QuestLink SIFTER_MV_COPY_1 = CHAPTER.copy(SIFTER_MV, -6, 0);

    public static final Quest MV_EMITTER = CHAPTER.quest("mv_emitter", q -> q
            .at(-4, 0)
            .title("MV 发射器")
            .icon(GTItems.EMITTER_MV)
            .task(Task.item(TagPrefix.gemFlawless, GTMaterials.Emerald), Task.item(GTItems.EMITTER_MV))
            .scaledReward(TagPrefix.gem, GTMaterials.Emerald, 4)
            .dependsOn(SIFTER_MV_COPY_1));

    public static final Quest LASER_ENGRAVER_MV = CHAPTER.quest("laser_engraver_mv", q -> q
            .at(0, 4)
            .title("MV 激光蚀刻机")
            .desc(d -> d
                    .line("MV阶段刻蚀芯片所需的透镜都能使用某些光谱带宽极窄的天然宝石制成")
                    .line()
                    .line("至于更高级的芯片大多都需要通过{染料浸洗}以得到窄带宽的透镜", YELLOW))
            .task(Task.item(GTMachines.LASER_ENGRAVER[GTValues.MV]))
            .scaledReward(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(MV_EMITTER));

    public static final QuestLink LASER_ENGRAVER_MV_COPY_1 = CHAPTER.copy(LASER_ENGRAVER_MV, -4, 6);

    public static final Quest ULPIC = CHAPTER.quest("ulpic", q -> q
            .at(-2, 4)
            .title("超低功率集成电路")
            .icon(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(TagPrefix.lens, GTMaterials.Sapphire), Task.item(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2))
            .scaledReward(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(LASER_ENGRAVER_MV_COPY_1, SILICON_WAFER));

    public static final Quest MV_VOLTAGE_COIL = CHAPTER.quest("mv_voltage_coil", q -> q
            .at(-2, 6)
            .title("MV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_MV)
            .task(Task.item(GTItems.VOLTAGE_COIL_MV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Aluminium, 16)
            .dependsOn(MV_ASSEMBLER_COPY_2));

    public static final Quest MV_ENERGY_HATCH = CHAPTER.quest("mv_energy_hatch", q -> q
            .at(2, 8)
            .milestone()
            .title("MV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.MV], 2))
            .scaledReward(TagPrefix.cableGtQuadruple, GTMaterials.Copper, 2)
            .dependsOn(MV_VOLTAGE_COIL, ULPIC));

    public static final Quest MV_GENERATOR = CHAPTER.quest("mv_generator", q -> q
            .at(-2, 16)
            .title("MV 发电")
            .task(Task.anyOf(4, GTMachines.STEAM_TURBINE[GTValues.MV], GTMachines.COMBUSTION[GTValues.MV], GTMachines.GAS_TURBINE[GTValues.MV]).title("任意 MV 发电机"))
            .scaledReward(Items.COAL_BLOCK, 4)
            .dependsOn(MV_COMPONENTS_COPY_7));

    public static final Quest STAINLESS_STEEL_DUST = CHAPTER.quest("stainless_steel_dust", q -> q
            .at(-2, 14)
            .title("不锈钢粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.StainlessSteel, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Manganese, 2)
            .dependsOn(CHROMIUM_COPY_2, MV_MIXER_COPY_3));

    public static final Quest STAINLESS_STEEL = CHAPTER.quest("stainless_steel", q -> q
            .at(0, 14)
            .milestone()
            .title("不锈钢锭")
            .desc(d -> d
                    .line("高压时代的基础金属材料")
                    .line()
                    .line("用量非常巨大，需要提前进行囤积式生产！")
                    .line()
                    .line("{别忘了双仓升压机制，这次你需要给电力高炉安装两个MV能源仓来升压到HV}", GRAY.italic()))
            .task(Task.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.StainlessSteel, 2)
            .dependsOn(MV_ENERGY_HATCH, MV_GENERATOR, STAINLESS_STEEL_DUST));

    public static final Quest HV_CASING = CHAPTER.quest("hv_casing", q -> q
            .at(18, 16)
            .title("HV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_HV))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 1)
            .dependsOn(STAINLESS_STEEL));

    public static final Quest VINYL_CHLORIDE = CHAPTER.quest("vinyl_chloride", q -> q
            .at(20, 16)
            .title("氯乙烯")
            .task(Task.item(GTMaterials.VinylChloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest PVC = CHAPTER.quest("pvc", q -> q
            .at(20, 14)
            .title("聚氯乙烯")
            .desc(d -> d.line("经典配方，不用多说，你也应该搭建至少一条小产线"))
            .task(Task.item(GTMaterials.PolyvinylChloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.plate, GTMaterials.PolyvinylChloride, 2)
            .dependsOn(VINYL_CHLORIDE));

    public static final Quest GOLD_CABLE = CHAPTER.quest("gold_cable", q -> q
            .at(18, 12)
            .title("金线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Gold, 2))
            .scaledReward(TagPrefix.ingot, GTMaterials.Gold, 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest HV_HULL = CHAPTER.quest("hv_hull", q -> q
            .at(18, 14)
            .milestone()
            .title("HV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", GOLD)
            .desc(d -> d
                    .line("这个阶段你的首要目标是做出{EV电路}。它能解锁这个阶段中最重要的机器{真空冷冻机}，让你能够更新原有的{石化产线}，并解锁你接下来会大量使用到的{大型化学反应釜}", GOLD, GOLD, YELLOW, GOLD)
                    .line()
                    .line("同时，在这个阶段你会遇到第一个{生产挑战}：制造自己的第一个{火箭}并{登月}。{T1}器官将是你造过最贵的东西，请务必{提升产能}，希望你的航天之旅一切顺利！", GREEN, GOLD, LIGHT_PURPLE, AQUA, YELLOW)
                    .line()
                    .line("{不要忘了发展AE与无线电网，它们同样是你这个阶段的重要目标}", GRAY.italic()))
            .task(Task.item(GTMachines.HULL[GTValues.HV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.HV], 1))
            .dependsOn(HV_CASING, PVC, GOLD_CABLE));

    public static final Quest GEM_LENSES = CHAPTER.quest("gem_lenses", q -> q
            .at(4, 10)
            .title("宝石透镜")
            .task(Task.item(TagPrefix.lens, GTMaterials.Ruby), Task.item(TagPrefix.lens, GTMaterials.Emerald))
            .scaledReward(TagPrefix.gem, GTMaterials.Ruby, 4)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest LOGIC_AND_MEMORY = CHAPTER.quest("logic_and_memory", q -> q
            .at(4, 8)
            .milestone()
            .title("逻辑电路与内存芯片")
            .desc(d -> d
                    .line("集成电路序列的核心芯片")
                    .line()
                    .line("现代计算机均基于冯·诺依曼架构，所以要构成最小计算机系统，RAM芯片是必需的组件")
                    .line()
                    .line("这也预示着之后的大量{高级主机}与{计算机}都需要它作为组件参与合成", GOLD, GOLD)
                    .line()
                    .line("做得再多也不嫌多"))
            .task(Task.item(GTItems.INTEGRATED_LOGIC_CIRCUIT, 8), Task.item(GTItems.RANDOM_ACCESS_MEMORY, 8))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(GEM_LENSES, LASER_ENGRAVER_MV, SILICON_WAFER));

    public static final Quest CIRCUIT_ASSEMBLER_LV = CHAPTER.quest("circuit_assembler_lv", q -> q
            .at(-2, 10)
            .title("LV 电路组装机")
            .subtitle("电路自动化！！")
            .desc(d -> d
                    .line("{电路组装机}是每个阶段最贵也是最值得的机器，它能将你之前的手动合成电路板的成本降低为一半以下", GOLD)
                    .line()
                    .line("从现在起你后面的几乎所有的电路都需要在{电路组装机}上组装，直到电路装配线", GOLD)
                    .line()
                    .line("{有的配方中需要}{额外的条件}{，这些都需要满足，否则电路组装机是不会执行配方的}", YELLOW, GOLD, YELLOW))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LV]))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_MV, 1)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest INTEGRATED_CIRCUITS = CHAPTER.quest("integrated_circuits", q -> q
            .at(0, 8)
            .milestone()
            .title("集成电路")
            .subtitle("集成电路#1")
            .desc(d -> d
                    .line("你开始制作更高级的电路序列——{集成电路}", YELLOW)
                    .line()
                    .line("从该序列开始，LV电路不再需要ULV电路合成，而是基于刻蚀成的芯片{直接合成}", YELLOW)
                    .line()
                    .line("{基础集成电路}是最初级的{集成电路}", GOLD, YELLOW)
                    .line()
                    .line("{优质集成电路}是高一级的{集成电路}", GOLD, YELLOW))
            .icon(GTItems.INTEGRATED_CIRCUIT_MV)
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_LV, 4), Task.item(GTItems.INTEGRATED_CIRCUIT_MV, 2))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_MV, 1)
            .dependsOn(LOGIC_AND_MEMORY, CIRCUIT_ASSEMBLER_LV));

    public static final Quest TRANSISTOR = CHAPTER.quest("transistor", q -> q
            .at(18, 8)
            .milestone()
            .title("晶体管")
            .task(Task.item(GTItems.TRANSISTOR, 8))
            .scaledReward(GTItems.TRANSISTOR, 4)
            .dependsOn(MV_ASSEMBLER_COPY_3, SILICON));

    public static final Quest SODIUM_ALUMINATE = CHAPTER.quest("sodium_aluminate", q -> q
            .at(22, 2)
            .title("铝酸钠")
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumAluminate, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Sapphire, 10)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest ALUMINIUM_HYDROXIDE = CHAPTER.quest("aluminium_hydroxide", q -> q
            .at(22, 0)
            .title("氢氧化铝")
            .desc(d -> d
                    .line("陶瓷材料，是用天然或合成化合物经过成形和高温烧结制成的一类无机非金属材料。具有高熔点、高硬度、高耐磨性、耐氧化等优点。")
                    .line()
                    .line("在现阶段，它能作为不错的{集成电路基板}为我们所用", YELLOW)
                    .line()
                    .line("为了烧制一炉最简单的{氧化铝陶瓷}，你需要制备一些{氢氧化铝粉}，不过仅仅一炉是不够你发展太远的，你应该尽早搭建起{陶瓷产线}", YELLOW, GOLD, YELLOW))
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminiumHydroxide, 56))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(SODIUM_ALUMINATE, MV_MIXER_COPY_1));

    public static final Quest REACTION_FURNACE = CHAPTER.quest("reaction_furnace", q -> q
            .at(20, -2)
            .title("反应炉")
            .subtitle("高温化学反应")
            .desc(d -> d.line("陶瓷线的重要机器！加装附属模块后支持{并行}", YELLOW))
            .task(Task.item(MultiBlockB.REACTION_FURNACE))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminiumHydroxide, 14)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest ALUMINA = CHAPTER.quest("alumina", q -> q
            .at(20, 0)
            .title("氧化铝")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Alumina, 20))
            .scaledReward(TagPrefix.dust, GTOMaterials.Alumina, 5)
            .dependsOn(ALUMINIUM_HYDROXIDE, REACTION_FURNACE));

    public static final Quest ALUMINA_CERAMIC_DUST = CHAPTER.quest("alumina_ceramic_dust", q -> q
            .at(18, 0)
            .milestone()
            .title("氧化铝陶瓷粉")
            .subtitle("高温反应")
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10))
            .scaledReward(TagPrefix.dust, GTOMaterials.Alumina, 4)
            .dependsOn(ALUMINA, SIFTER_MV));

    public static final Quest COMPRESSOR_MV = CHAPTER.quest("compressor_mv", q -> q
            .at(2, 18)
            .title("MV 压缩机")
            .task(Task.item(GTMachines.COMPRESSOR[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10)
            .dependsOn(MV_COMPONENTS_COPY_8));

    public static final Quest ALUMINA_CERAMIC_ROUGH_BLANK = CHAPTER.quest("alumina_ceramic_rough_blank", q -> q
            .at(4, 18)
            .title("氧化铝陶瓷粗胚")
            .subtitle("压制成型")
            .icon(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.AluminaCeramic)
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.AluminaCeramic))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10)
            .dependsOn(COMPRESSOR_MV, ALUMINA_CERAMIC_DUST));

    public static final Quest GAS_COLLECTOR_LV = CHAPTER.quest("gas_collector_lv", q -> q
            .at(4, 20)
            .title("LV 集气室")
            .task(Task.item(GTMachines.GAS_COLLECTOR[GTValues.LV]))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest NITROGEN = CHAPTER.quest("nitrogen", q -> q
            .at(6, 20)
            .title("氮")
            .icon(GTMaterials.Nitrogen.getBucket())
            .task(Task.item(GTMachines.CENTRIFUGE[GTValues.MV]), Task.item(GTMaterials.Nitrogen.getBucket()))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 2)
            .dependsOn(GAS_COLLECTOR_LV));

    public static final Quest ALUMINA_CERAMIC_BLOCK = CHAPTER.quest("alumina_ceramic_block", q -> q
            .at(6, 18)
            .title("氧化铝陶瓷块")
            .subtitle("烧结")
            .desc(d -> d
                    .line("现阶段你可以临时起一台{电力高炉}", GOLD)
                    .line()
                    .line("等到下一阶段，我们有更专业的机器替代高炉")
                    .line()
                    .line("{是什么呢？是什么呢？好难猜呢……}", GRAY))
            .task(Task.item(TagPrefix.block, GTOMaterials.AluminaCeramic))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 1)
            .dependsOn(ALUMINA_CERAMIC_ROUGH_BLANK, NITROGEN));

    public static final Quest CERAMIC_FLAKES = CHAPTER.quest("ceramic_flakes", q -> q
            .at(6, 16)
            .title("氧化铝陶瓷薄片")
            .subtitle("切片")
            .desc(d -> d.line("36片具有优秀电学性能的基板，对于{进阶集成电路}是必需的", GOLD))
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.AluminaCeramic, 8))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.AluminaCeramic, 4)
            .dependsOn(ALUMINA_CERAMIC_BLOCK));

    public static final Quest ADVANCED_IC = CHAPTER.quest("advanced_ic", q -> q
            .at(4, 14)
            .milestone()
            .title("进阶集成电路")
            .subtitle("Knock Knock")
            .desc(d -> d
                    .line("集成电路的最终版本")
                    .line()
                    .line("你已经敲开了高压时代的大门！"))
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_HV, 4))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 1)
            .dependsOn(CERAMIC_FLAKES, TRANSISTOR, INTEGRATED_CIRCUITS));

    public static final Quest INFINITE_RESOURCE_PROVIDER = CHAPTER.quest("infinite_resource_provider", q -> q
            .at(0, -2)
            .title("无限资源供应器")
            .icon(GTAEMachines.INFINITE_RESOURCE_PROVIDER)
            .task(Task.item(GTAEMachines.INFINITE_RESOURCE_PROVIDER))
            .scaledReward(TagPrefix.ingot, GTMaterials.Aluminium, 4)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest ELECTRICITY_SOURCES_IN_THE_MEDIUM_VOLTAGE = CHAPTER.quest("electricity_sources_in_the_medium_voltage", q -> q
            .at(2, -2)
            .side()
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
            .optional()
            .dependsOn(NewLvChapter.MV_HULL, NewLvChapter.MV_CIRCUIT));

    public static final Quest MV_BATTERY_BUFFER_16X = CHAPTER.quest("mv_battery_buffer_16x", q -> q
            .at(0, 2)
            .side()
            .desc(d -> d.line("中压时代唯一的蓄电方式"))
            .task(Task.item(GTMachines.BATTERY_BUFFER_16[GTValues.MV]))
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest MV_BENDER = CHAPTER.quest("mv_bender", q -> q
            .at(6, 6)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTMachines.BENDER[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest MV_WIREMILL = CHAPTER.quest("mv_wiremill", q -> q
            .at(6, 2)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTMachines.WIREMILL[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest MV_CLUSTER = CHAPTER.quest("mv_cluster", q -> q
            .at(8, 4)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.CLUSTER[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest MV_LAMINATOR = CHAPTER.quest("mv_laminator", q -> q
            .at(4, 2)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.LAMINATOR[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest MV_LOOM = CHAPTER.quest("mv_loom", q -> q
            .at(4, 4)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.LOOM[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest MV_ARC_GENERATOR = CHAPTER.quest("mv_arc_generator", q -> q
            .at(-2, 0)
            .side()
            .desc(d -> d.line("比低压机器更高效的机器"))
            .task(Task.item(GTOMachines.ARC_GENERATOR[GTValues.MV]))
            .dependsOn(MV_EMITTER));

    public static final Quest MV_SUPERCONDUCTOR = CHAPTER.quest("mv_superconductor", q -> q
            .at(2, 2)
            .side()
            .title("MV超导")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.EnergeticAlloy))
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest PROCESSING_PLANT = CHAPTER.quest("processing_plant", q -> q
            .at(22, 14)
            .side()
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
            .dependsOn(MV_ASSEMBLER));

    public static final Quest COMPONENT_ASSEMBLER = CHAPTER.quest("component_assembler", q -> q
            .at(24, 10)
            .side()
            .subtitle("百分之一亿的贵得要死#1")
            .desc(d -> d
                    .line("批量部件制作，比普通组装省25%材料。{部件装配机外壳}影响可以可以制作的{部件等级}。", GOLD, YELLOW)
                    .line()
                    .line("想办法自动化执行部装配方！")
                    .line()
                    .line("不过{部件装配机外壳}在此阶段{过于昂贵}，可以考虑到达{HV}阶段{提升生产力}之后再进行制作", GOLD, RED, HV, YELLOW))
            .task(Task.item(MultiBlockC.COMPONENT_ASSEMBLER))
            .dependsOn(MV_ASSEMBLER));

    public static final Quest MV_FLUID_DRILLING_RIG = CHAPTER.quest("mv_fluid_drilling_rig", q -> q
            .at(8, 12)
            .side()
            .desc(d -> d
                    .line("无限石油")
                    .line()
                    .line("你可以用它来挖掘基岩流体，包括但不限于石油，原油，天然气，稀有气体等")
                    .line()
                    .line("可以查看探矿仪的EMI来查询基岩流体信息"))
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV]))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(VANADIUM_STEEL));

    public static final Quest MV_MINER = CHAPTER.quest("mv_miner", q -> q
            .at(-4, -2)
            .side()
            .task(Task.item(GTMachines.MINER[GTValues.MV]))
            .dependsOn(MV_EMITTER, MV_ASSEMBLER_COPY_1));

    public static final Quest MV_AIR_SCRUBBER = CHAPTER.quest("mv_air_scrubber", q -> q
            .at(4, 6)
            .side()
            .desc(d -> d
                    .line("如{上一章节}所述，它拥有更高的净化能力和净化范围", link(NewLvChapter.LV_AIR_SCRUBBER))
                    .line()
                    .line("下一阶段或许可以{完全自动化掏灰}？", YELLOW))
            .task(Task.item(GTMachines.AIR_SCRUBBER[GTValues.MV]))
            .dependsOn(MV_COMPONENTS));

    public static final Quest Q_2386117F = CHAPTER.quest("q_2386117f", q -> q
            .at(-4, 4)
            .side()
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
            .dependsOn(ULPIC));

    public static final Quest VACUUM_INTERFACE = CHAPTER.quest("vacuum_interface", q -> q
            .at(22, 16)
            .side()
            .subtitle("也许你有抽真空的需求...")
            .desc(d -> d
                    .line("将能提供真空的小机器贴着接口放即可")
                    .line()
                    .line("{你问如何给多方块机器提供热量？你前面一定已经用过了}", GRAY.italic()))
            .task(Task.item(GTOMachines.VACUUM_INTERFACE))
            .optional()
            .dependsOn(PROCESSING_PLANT));

    public static final Quest MV_COMPONENT_SUBSIDY = CHAPTER.quest("mv_component_subsidy", q -> q
            .at(24, 12)
            .side()
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
            .optional().hideUntilDepsVisible()
            .dependsOn(MV_ASSEMBLER));

    public static final Quest STAINLESS_STEEL_SUBSIDY = CHAPTER.quest("stainless_steel_subsidy", q -> q
            .at(0, 16)
            .side()
            .icon(TagPrefix.ingot, GTMaterials.StainlessSteel)
            .title("不锈钢补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 320L))
            .normalSubsidy(Reward.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 56))
            .optional().hideUntilDepsVisible()
            .dependsOn(STAINLESS_STEEL));

    public static final Quest MV_FORCE_FIELD_GENERATOR = CHAPTER.quest("mv_force_field_generator", q -> q
            .at(-4, 2)
            .side()
            .icon(GTItems.FIELD_GENERATOR_MV)
            .title("{MV}力场发生器...?", MV)
            .desc(d -> d.line("不是所有标明电压的物品都可以在对应电压内制作，{MV}力场发生器就是一个例子，它需要你在{HV}阶段制作{真空冷冻机}之后才能够制作", MV, HV, GOLD))
            .task(Task.checkmark())
            .dependsOn(MV_EMITTER));

    public static final Quest PLASTIC_SUBSIDY_RENEWED = CHAPTER.quest("plastic_subsidy_renewed", q -> q
            .at(14, 6)
            .side()
            .icon(TagPrefix.plate, GTMaterials.Polyethylene)
            .title("塑料补助再续")
            .desc(d -> d.line("后面还要用到大量的塑料，我再送你一点"))
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 256L))
            .optional().hideUntilDepsVisible()
            .dependsOn(SILICON_WAFER));

    public static final Quest ENERGETIC_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("energetic_photovoltaic_power_station", q -> q
            .at(14, 10)
            .side()
            .subtitle("免费能源")
            .desc(d -> d.line("有些昂贵...值得吗？"))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_ENERGETIC))
            .optional()
            .dependsOn(SILICON_WAFER));

    public static final Quest AE_CIRCUIT_BOARD = CHAPTER.quest("ae_circuit_board", q -> q
            .at(20, 10)
            .side()
            .title("AE电路板")
            .desc(d -> d.line("AE之路真正的开始，别忘了去{应用能源}章节看看你现在能做什么", link(AeChapter.AE_CIRCUIT_BOARD)))
            .task(
                    Task.item(AEItems.SILICON_PRINT),
                    Task.item(AEItems.CALCULATION_PROCESSOR_PRINT),
                    Task.item(AEItems.ENGINEERING_PROCESSOR_PRINT),
                    Task.item(AEItems.LOGIC_PROCESSOR_PRINT))
            .requireAny()
            .dependsOn(SILICON));

    public static final Quest NOTHING_FROM_NOTHING = CHAPTER.quest("nothing_from_nothing", q -> q
            .at(-2, -2)
            .side()
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
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest GREAT_POWER_BRINGS_MIRACLES_EASY = CHAPTER.quest("great_power_brings_miracles_easy", q -> q
            .at(18, -2)
            .side()
            .icon(MultiBlockG.ISOSTATIC_PRESS)
            .title("大力出奇迹{（简单）}", GREEN)
            .desc(d -> d
                    .line("简单模式陶瓷无需压制成型")
                    .line()
                    .line("{史蒂夫的手可以解决一切}", GRAY.italic()))
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(ALUMINA_CERAMIC_DUST, UtilChapter.Q_056EC4E6));

    public static final Quest ALUMINA_CERAMIC_BRICK = CHAPTER.quest("alumina_ceramic_brick", q -> q
            .at(8, 18)
            .side()
            .subtitle("切块")
            .task(Task.item(GTOTagPrefix.BRICK, GTOMaterials.AluminaCeramic, 9))
            .dependsOn(ALUMINA_CERAMIC_BLOCK));

    public static final Quest MV_BEDROCK_ORE_MINER = CHAPTER.quest("mv_bedrock_ore_miner", q -> q
            .at(2, 12)
            .side()
            .desc(d -> d
                    .line("无限矿石")
                    .line()
                    .line("你可以用它来挖掘基岩矿脉，在实体矿脉中相对稀少的矿石在基岩矿脉中占比将{大大增加}（如金矿石）", GOLD)
                    .line()
                    .line("可以查看探矿仪的EMI来查询矿脉信息（基岩矿脉与实体矿脉共享）"))
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.MV]))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(ADVANCED_IC));

    public static final Quest STEEL_LARGE_BOILER = CHAPTER.quest("steel_large_boiler", q -> q
            .at(4, 12)
            .side()
            .task(Task.item(GTMultiMachines.LARGE_BOILER_STEEL))
            .dependsOn(ADVANCED_IC));

    public static final Quest CHIP_SUBSIDY = CHAPTER.quest("chip_subsidy", q -> q
            .at(4, 16)
            .side()
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
            .optional().hideUntilDepsVisible()
            .dependsOn(ADVANCED_IC));

    public static final Quest PROGRAMMABLE_HATCH_EASY = CHAPTER.quest("programmable_hatch_easy", q -> q
            .at(2, 16)
            .side()
            .minWidth(250)
            .icon(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE)
            .title("{可编程仓室}{（简单）}", YELLOW, GREEN)
            .desc(d -> d
                    .line("其具体用途请前往{HV阶段同名任务}查看", GRAY.underline())
                    .line()
                    .line("简单难度下，虚拟物品的解锁条件提前")
                    .line()
                    .line("请无视HV阶段的解锁条件"))
            .task(
                    Task.item(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE),
                    Task.item(GTOMachines.PROGRAMMABLEC_HATCH[GTValues.MV]).optional(),
                    Task.item(CustomItems.PROGRAMMABLE_COVER).optional())
            .optional().hideUntilDepsVisible()
            .dependsOn(ADVANCED_IC, UtilChapter.Q_056EC4E6));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewLvChapter.MV_HULL, 0, 0))
                .mainLine(NewLvChapter.MV_HULL, LIQUEFACTION_FURNACE, CRYSTALLIZATION_CHAMBER, KANTHAL, ALUMINA_CERAMIC_DUST, TRANSISTOR, SILICON_WAFER, LOGIC_AND_MEMORY, MV_ENERGY_HATCH, INTEGRATED_CIRCUITS, STAINLESS_STEEL, ADVANCED_IC, HV_HULL));
    }
}
