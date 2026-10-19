package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockB;
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

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewMvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_mv", c -> c
            .title("5. 中压时代(MV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.MV]));

    public static final Quest MV_COMPONENTS = CHAPTER.quest("mv_components", q -> q
            .at(4, -2)
            .title("MV 部件")
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

    public static final QuestLink MV_COMPONENTS_COPY_1 = CHAPTER.copy(MV_COMPONENTS, 4, 2);

    public static final QuestLink MV_COMPONENTS_COPY_2 = CHAPTER.copy(MV_COMPONENTS, 4, -4);

    public static final QuestLink MV_COMPONENTS_COPY_3 = CHAPTER.copy(MV_COMPONENTS, 14, 2);

    public static final QuestLink MV_COMPONENTS_COPY_4 = CHAPTER.copy(MV_COMPONENTS, 8, 8);

    public static final QuestLink MV_COMPONENTS_COPY_5 = CHAPTER.copy(MV_COMPONENTS, 6, 10);

    public static final QuestLink MV_COMPONENTS_COPY_6 = CHAPTER.copy(MV_COMPONENTS, -6, 0);

    public static final QuestLink MV_COMPONENTS_COPY_7 = CHAPTER.copy(MV_COMPONENTS, -6, 10);

    public static final QuestLink MV_COMPONENTS_COPY_8 = CHAPTER.copy(MV_COMPONENTS, 0, 14);

    public static final Quest MV_ELECTROLYZER = CHAPTER.quest("mv_electrolyzer", q -> q
            .at(6, 4)
            .title("MV 电解机")
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Ruby, 12)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final QuestLink MV_ELECTROLYZER_COPY_1 = CHAPTER.copy(MV_ELECTROLYZER, 14, 8);

    public static final Quest CHROMIUM = CHAPTER.quest("chromium", q -> q
            .at(8, 4)
            .title("铬")
            .task(Task.item(TagPrefix.dust, GTMaterials.Chromium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 4)
            .dependsOn(MV_ELECTROLYZER));

    public static final QuestLink CHROMIUM_COPY_1 = CHAPTER.copy(CHROMIUM, -4, -4);

    public static final QuestLink CHROMIUM_COPY_2 = CHAPTER.copy(CHROMIUM, -6, 14);

    public static final Quest MV_MIXER = CHAPTER.quest("mv_mixer", q -> q
            .at(6, 2)
            .title("MV 搅拌机")
            .task(Task.item(GTMachines.MIXER[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 2)
            .dependsOn(MV_COMPONENTS_COPY_1));

    public static final QuestLink MV_MIXER_COPY_1 = CHAPTER.copy(MV_MIXER, 12, -4);

    public static final QuestLink MV_MIXER_COPY_2 = CHAPTER.copy(MV_MIXER, -6, -2);

    public static final QuestLink MV_MIXER_COPY_3 = CHAPTER.copy(MV_MIXER, -4, 16);

    public static final QuestLink MV_MIXER_COPY_4 = CHAPTER.copy(MV_MIXER, 6, -2);

    public static final Quest MV_DISTILLERY = CHAPTER.quest("mv_distillery", q -> q
            .at(6, -4)
            .title("MV 蒸馏室")
            .task(Task.item(GTMachines.DISTILLERY[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Lazurite, 8)
            .dependsOn(MV_COMPONENTS_COPY_2));

    public static final Quest DISTILLED_WATER = CHAPTER.quest("distilled_water", q -> q
            .at(8, -4)
            .title("蒸馏水")
            .task(Task.item(GTMaterials.DistilledWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Lazurite, 4)
            .dependsOn(MV_DISTILLERY));

    public static final QuestLink DISTILLED_WATER_COPY_1 = CHAPTER.copy(DISTILLED_WATER, 0, 2);

    public static final Quest COOLANT = CHAPTER.quest("coolant", q -> q
            .at(8, -2)
            .title("冷却液")
            .task(Task.item(GTOMaterials.CoolantLiquid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.CoolantLiquid.getBucket(), 1)
            .dependsOn(DISTILLED_WATER, MV_MIXER_COPY_4));

    public static final QuestLink COOLANT_COPY_1 = CHAPTER.copy(COOLANT, 12, 4);

    public static final Quest KANTHAL_DUST = CHAPTER.quest("kanthal_dust", q -> q
            .at(8, 2)
            .title("坎塔尔粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Kanthal, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Iron, 4)
            .dependsOn(CHROMIUM, MV_MIXER));

    public static final Quest KANTHAL = CHAPTER.quest("kanthal", q -> q
            .at(8, 0)
            .milestone()
            .title("坎塔尔锭")
            .icon(TagPrefix.ingot, GTMaterials.Kanthal)
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.MV]), Task.item(TagPrefix.ingot, GTMaterials.Kanthal, 32))
            .scaledReward(TagPrefix.ingot, GTMaterials.Kanthal, 8)
            .dependsOn(KANTHAL_DUST, COOLANT));

    public static final Quest MV_ASSEMBLER = CHAPTER.quest("mv_assembler", q -> q
            .at(10, 8)
            .title("MV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.MV]))
            .scaledReward(TagPrefix.foil, GTMaterials.Aluminium, 16)
            .dependsOn(MV_COMPONENTS_COPY_4));

    public static final QuestLink MV_ASSEMBLER_COPY_1 = CHAPTER.copy(MV_ASSEMBLER, -2, 10);

    public static final Quest LIQUEFACTION_FURNACE = CHAPTER.quest("liquefaction_furnace", q -> q
            .at(2, 0)
            .milestone()
            .title("液化炉")
            .task(Task.item(MultiBlockB.LIQUEFACTION_FURNACE))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 8)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest MV_LATHE = CHAPTER.quest("mv_lathe", q -> q
            .at(2, -2)
            .title("MV 车床")
            .task(Task.item(GTMachines.LATHE[GTValues.MV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Sapphire, 4)
            .dependsOn(MV_COMPONENTS));

    public static final Quest KANTHAL_COIL = CHAPTER.quest("kanthal_coil", q -> q
            .at(12, 8)
            .title("坎塔尔线圈")
            .task(Task.item(GTBlocks.COIL_KANTHAL, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Kanthal, 8)
            .dependsOn(MV_ASSEMBLER, KANTHAL, LIQUEFACTION_FURNACE));

    public static final Quest SILICON_DIOXIDE = CHAPTER.quest("silicon_dioxide", q -> q
            .at(14, 4)
            .title("二氧化硅粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.SiliconDioxide, 27))
            .scaledReward(Items.SAND, 32)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest SILICON_DUST = CHAPTER.quest("silicon_dust", q -> q
            .at(14, 6)
            .title("硅粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Silicon, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.SiliconDioxide, 12)
            .dependsOn(SILICON_DIOXIDE, MV_ELECTROLYZER_COPY_1));

    public static final QuestLink SILICON_DUST_COPY_1 = CHAPTER.copy(SILICON_DUST, -10, 2);

    public static final Quest VANADIUM_STEEL = CHAPTER.quest("vanadium_steel", q -> q
            .at(-4, -2)
            .title("钒钢")
            .task(Task.item(TagPrefix.ingot, GTMaterials.VanadiumSteel, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.VanadiumSteel, 2)
            .dependsOn(CHROMIUM_COPY_1, MV_MIXER_COPY_2));

    public static final Quest SILICON = CHAPTER.quest("silicon", q -> q
            .at(12, 6)
            .title("硅锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Silicon, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.Silicon, 2)
            .dependsOn(SILICON_DUST, KANTHAL_COIL, COOLANT_COPY_1));

    public static final QuestLink SILICON_COPY_1 = CHAPTER.copy(SILICON, -2, -2);

    public static final Quest CUTTER_MV = CHAPTER.quest("cutter_mv", q -> q
            .at(-4, 0)
            .title("MV 切割机")
            .task(Task.item(GTMachines.CUTTER[GTValues.MV]))
            .scaledReward(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon, 1)
            .dependsOn(MV_COMPONENTS_COPY_6, VANADIUM_STEEL));

    public static final Quest SILICON_SEED = CHAPTER.quest("silicon_seed", q -> q
            .at(-2, 0)
            .title("硅晶种")
            .icon(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon)
            .task(Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon))
            .scaledReward(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Silicon, 1)
            .dependsOn(CUTTER_MV, SILICON_COPY_1, DISTILLED_WATER_COPY_1));

    public static final Quest CRYSTALLIZATION_CHAMBER = CHAPTER.quest("crystallization_chamber", q -> q
            .at(4, 0)
            .milestone()
            .title("结晶器")
            .task(Task.item(MultiBlockG.CRYSTALLIZATION_CHAMBER))
            .scaledReward(TagPrefix.dust, GTMaterials.GalliumArsenide, 1)
            .dependsOn(MV_COMPONENTS));

    public static final Quest TRICHLOROSILANE = CHAPTER.quest("trichlorosilane", q -> q
            .at(-8, 2)
            .title("三氯硅烷")
            .task(Task.item(GTOMaterials.Trichlorosilane.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 2)
            .dependsOn(SILICON_DUST_COPY_1));

    public static final Quest ELECTRONIC_GRADE_SILICON_DUST = CHAPTER.quest("electronic_grade_silicon_dust", q -> q
            .at(-6, 2)
            .title("电子级硅粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.ElectronicGradeSilicon, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.ElectronicGradeSilicon, 8)
            .dependsOn(TRICHLOROSILANE));

    public static final Quest ELECTRONIC_GRADE_SILICON_INGOT = CHAPTER.quest("electronic_grade_silicon_ingot", q -> q
            .at(-4, 2)
            .title("电子级硅锭")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.ElectronicGradeSilicon, 32))
            .scaledReward(TagPrefix.ingot, GTOMaterials.ElectronicGradeSilicon, 8)
            .dependsOn(ELECTRONIC_GRADE_SILICON_DUST));

    public static final Quest ELECTRONIC_GRADE_SILICON = CHAPTER.quest("electronic_grade_silicon", q -> q
            .at(-2, 4)
            .title("电子级硅液")
            .task(Task.item(GTOMaterials.ElectronicGradeSilicon.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.GalliumArsenide, 1)
            .dependsOn(ELECTRONIC_GRADE_SILICON_INGOT, LIQUEFACTION_FURNACE));

    public static final Quest MV_BREWERY = CHAPTER.quest("mv_brewery", q -> q
            .at(6, 8)
            .title("MV 酿造机")
            .task(Task.item(GTMachines.BREWERY[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Talc, 8)
            .dependsOn(MV_COMPONENTS_COPY_5));

    public static final Quest LUBRICANT = CHAPTER.quest("lubricant", q -> q
            .at(4, 8)
            .title("润滑油")
            .task(Task.item(GTMaterials.Lubricant.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Talc, 8)
            .dependsOn(MV_BREWERY));

    public static final Quest SILICON_WAFER = CHAPTER.quest("silicon_wafer", q -> q
            .at(2, 6)
            .milestone()
            .title("硅晶圆")
            .icon(GTItems.SILICON_WAFER)
            .task(Task.item(GTItems.SILICON_BOULE), Task.item(GTItems.SILICON_WAFER, 4))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(CRYSTALLIZATION_CHAMBER, SILICON_SEED, ELECTRONIC_GRADE_SILICON, LUBRICANT, CUTTER_MV));

    public static final Quest SIFTER_MV = CHAPTER.quest("sifter_mv", q -> q
            .at(12, 2)
            .title("MV 筛选机")
            .task(Task.item(GTMachines.SIFTER[GTValues.MV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Emerald, 4)
            .dependsOn(MV_COMPONENTS_COPY_3));

    public static final QuestLink SIFTER_MV_COPY_1 = CHAPTER.copy(SIFTER_MV, 0, 10);

    public static final Quest MV_EMITTER = CHAPTER.quest("mv_emitter", q -> q
            .at(2, 10)
            .title("MV 发射器")
            .icon(GTItems.EMITTER_MV)
            .task(Task.item(TagPrefix.gemFlawless, GTMaterials.Emerald), Task.item(GTItems.EMITTER_MV))
            .scaledReward(TagPrefix.gem, GTMaterials.Emerald, 4)
            .dependsOn(SIFTER_MV_COPY_1));

    public static final Quest LASER_ENGRAVER_MV = CHAPTER.quest("laser_engraver_mv", q -> q
            .at(2, 8)
            .title("MV 激光蚀刻机")
            .task(Task.item(GTMachines.LASER_ENGRAVER[GTValues.MV]))
            .scaledReward(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(MV_EMITTER));

    public static final QuestLink LASER_ENGRAVER_MV_COPY_1 = CHAPTER.copy(LASER_ENGRAVER_MV, -6, 4);

    public static final Quest ULPIC = CHAPTER.quest("ulpic", q -> q
            .at(-4, 4)
            .title("超低功率集成电路")
            .icon(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(TagPrefix.lens, GTMaterials.Sapphire), Task.item(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 2))
            .scaledReward(GTItems.ULTRA_LOW_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(LASER_ENGRAVER_MV_COPY_1, SILICON_WAFER));

    public static final Quest MV_VOLTAGE_COIL = CHAPTER.quest("mv_voltage_coil", q -> q
            .at(-2, 8)
            .title("MV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_MV)
            .task(Task.item(GTItems.VOLTAGE_COIL_MV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Aluminium, 16)
            .dependsOn(MV_ASSEMBLER_COPY_1));

    public static final Quest MV_ENERGY_HATCH = CHAPTER.quest("mv_energy_hatch", q -> q
            .at(-2, 6)
            .milestone()
            .title("MV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.MV], 2))
            .scaledReward(TagPrefix.cableGtQuadruple, GTMaterials.Copper, 2)
            .dependsOn(MV_VOLTAGE_COIL, ULPIC));

    public static final Quest MV_GENERATOR = CHAPTER.quest("mv_generator", q -> q
            .at(-6, 12)
            .title("MV 发电")
            .task(Task.anyOf(4, GTMachines.STEAM_TURBINE[GTValues.MV], GTMachines.COMBUSTION[GTValues.MV], GTMachines.GAS_TURBINE[GTValues.MV]).title("任意 MV 发电机"))
            .scaledReward(Items.COAL_BLOCK, 4)
            .dependsOn(MV_COMPONENTS_COPY_7));

    public static final Quest STAINLESS_STEEL_DUST = CHAPTER.quest("stainless_steel_dust", q -> q
            .at(-4, 14)
            .title("不锈钢粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.StainlessSteel, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Manganese, 2)
            .dependsOn(CHROMIUM_COPY_2, MV_MIXER_COPY_3));

    public static final Quest STAINLESS_STEEL = CHAPTER.quest("stainless_steel", q -> q
            .at(-4, 12)
            .milestone()
            .title("不锈钢锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.StainlessSteel, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.StainlessSteel, 2)
            .dependsOn(MV_ENERGY_HATCH, MV_GENERATOR, STAINLESS_STEEL_DUST));

    public static final Quest HV_CASING = CHAPTER.quest("hv_casing", q -> q
            .at(10, 14)
            .title("HV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_HV))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 1)
            .dependsOn(STAINLESS_STEEL));

    public static final Quest VINYL_CHLORIDE = CHAPTER.quest("vinyl_chloride", q -> q
            .at(12, 10)
            .title("氯乙烯")
            .task(Task.item(GTMaterials.VinylChloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest PVC = CHAPTER.quest("pvc", q -> q
            .at(12, 12)
            .title("聚氯乙烯")
            .task(Task.item(GTMaterials.PolyvinylChloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.plate, GTMaterials.PolyvinylChloride, 2)
            .dependsOn(VINYL_CHLORIDE));

    public static final Quest GOLD_CABLE = CHAPTER.quest("gold_cable", q -> q
            .at(10, 10)
            .title("金线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Gold, 2))
            .scaledReward(TagPrefix.ingot, GTMaterials.Gold, 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest HV_HULL = CHAPTER.quest("hv_hull", q -> q
            .at(10, 12)
            .milestone()
            .title("HV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.HV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.HV], 1))
            .dependsOn(HV_CASING, PVC, GOLD_CABLE));

    public static final Quest GEM_LENSES = CHAPTER.quest("gem_lenses", q -> q
            .at(0, 8)
            .title("宝石透镜")
            .task(Task.item(TagPrefix.lens, GTMaterials.Ruby), Task.item(TagPrefix.lens, GTMaterials.Emerald))
            .scaledReward(TagPrefix.gem, GTMaterials.Ruby, 4)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest LOGIC_AND_MEMORY = CHAPTER.quest("logic_and_memory", q -> q
            .at(0, 6)
            .milestone()
            .title("逻辑电路与内存芯片")
            .task(Task.item(GTItems.INTEGRATED_LOGIC_CIRCUIT, 8), Task.item(GTItems.RANDOM_ACCESS_MEMORY, 8))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(GEM_LENSES, LASER_ENGRAVER_MV, SILICON_WAFER));

    public static final Quest CIRCUIT_ASSEMBLER_LV = CHAPTER.quest("circuit_assembler_lv", q -> q
            .at(-6, 6)
            .title("LV 电路组装机")
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LV]))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_MV, 1)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest INTEGRATED_CIRCUITS = CHAPTER.quest("integrated_circuits", q -> q
            .at(-4, 6)
            .milestone()
            .title("集成电路")
            .icon(GTItems.INTEGRATED_CIRCUIT_MV)
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_LV, 4), Task.item(GTItems.INTEGRATED_CIRCUIT_MV, 2))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_MV, 1)
            .dependsOn(LOGIC_AND_MEMORY, CIRCUIT_ASSEMBLER_LV));

    public static final Quest TRANSISTOR = CHAPTER.quest("transistor", q -> q
            .at(10, 6)
            .milestone()
            .title("晶体管")
            .task(Task.item(GTItems.TRANSISTOR, 8))
            .scaledReward(GTItems.TRANSISTOR, 4)
            .dependsOn(MV_ASSEMBLER, SILICON));

    public static final Quest SODIUM_ALUMINATE = CHAPTER.quest("sodium_aluminate", q -> q
            .at(10, -2)
            .title("铝酸钠")
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumAluminate, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Sapphire, 10)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest ALUMINIUM_HYDROXIDE = CHAPTER.quest("aluminium_hydroxide", q -> q
            .at(12, -2)
            .title("氢氧化铝")
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminiumHydroxide, 56))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(SODIUM_ALUMINATE, MV_MIXER_COPY_1));

    public static final Quest REACTION_FURNACE = CHAPTER.quest("reaction_furnace", q -> q
            .at(14, 0)
            .title("反应炉")
            .task(Task.item(MultiBlockB.REACTION_FURNACE))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminiumHydroxide, 14)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest ALUMINA = CHAPTER.quest("alumina", q -> q
            .at(12, 0)
            .title("氧化铝")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Alumina, 20))
            .scaledReward(TagPrefix.dust, GTOMaterials.Alumina, 5)
            .dependsOn(ALUMINIUM_HYDROXIDE, REACTION_FURNACE));

    public static final Quest ALUMINA_CERAMIC_DUST = CHAPTER.quest("alumina_ceramic_dust", q -> q
            .at(10, 0)
            .milestone()
            .title("氧化铝陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10))
            .scaledReward(TagPrefix.dust, GTOMaterials.Alumina, 4)
            .dependsOn(ALUMINA, SIFTER_MV));

    public static final Quest COMPRESSOR_MV = CHAPTER.quest("compressor_mv", q -> q
            .at(2, 14)
            .title("MV 压缩机")
            .task(Task.item(GTMachines.COMPRESSOR[GTValues.MV]))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10)
            .dependsOn(MV_COMPONENTS_COPY_8));

    public static final Quest ALUMINA_CERAMIC_ROUGH_BLANK = CHAPTER.quest("alumina_ceramic_rough_blank", q -> q
            .at(2, 16)
            .title("氧化铝陶瓷粗胚")
            .icon(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.AluminaCeramic)
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.AluminaCeramic))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminaCeramic, 10)
            .dependsOn(COMPRESSOR_MV, ALUMINA_CERAMIC_DUST));

    public static final Quest GAS_COLLECTOR_LV = CHAPTER.quest("gas_collector_lv", q -> q
            .at(6, 14)
            .title("LV 集气室")
            .task(Task.item(GTMachines.GAS_COLLECTOR[GTValues.LV]))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 2)
            .dependsOn(NewLvChapter.MV_HULL));

    public static final Quest NITROGEN = CHAPTER.quest("nitrogen", q -> q
            .at(6, 16)
            .title("氮")
            .icon(GTMaterials.Nitrogen.getBucket())
            .task(Task.item(GTMachines.CENTRIFUGE[GTValues.MV]), Task.item(GTMaterials.Nitrogen.getBucket()))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 2)
            .dependsOn(GAS_COLLECTOR_LV));

    public static final Quest ALUMINA_CERAMIC_BLOCK = CHAPTER.quest("alumina_ceramic_block", q -> q
            .at(4, 16)
            .title("氧化铝陶瓷块")
            .task(Task.item(TagPrefix.block, GTOMaterials.AluminaCeramic))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 1)
            .dependsOn(ALUMINA_CERAMIC_ROUGH_BLANK, NITROGEN));

    public static final Quest CERAMIC_FLAKES = CHAPTER.quest("ceramic_flakes", q -> q
            .at(4, 14)
            .title("氧化铝陶瓷薄片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.AluminaCeramic, 8))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.AluminaCeramic, 4)
            .dependsOn(ALUMINA_CERAMIC_BLOCK));

    public static final Quest ADVANCED_IC = CHAPTER.quest("advanced_ic", q -> q
            .at(4, 12)
            .milestone()
            .title("进阶集成电路")
            .task(Task.item(GTItems.INTEGRATED_CIRCUIT_HV, 4))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 1)
            .dependsOn(CERAMIC_FLAKES, TRANSISTOR, INTEGRATED_CIRCUITS));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewLvChapter.MV_HULL, 0, 0))
                .mainLine(NewLvChapter.MV_HULL, LIQUEFACTION_FURNACE, CRYSTALLIZATION_CHAMBER, KANTHAL, ALUMINA_CERAMIC_DUST, TRANSISTOR, SILICON_WAFER, LOGIC_AND_MEMORY, MV_ENERGY_HATCH, INTEGRATED_CIRCUITS, STAINLESS_STEEL, ADVANCED_IC, HV_HULL));
    }
}
