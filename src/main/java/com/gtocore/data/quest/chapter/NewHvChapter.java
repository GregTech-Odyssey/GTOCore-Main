package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.item.misc.OrganType;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.player.OrganTier;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewHvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_hv", c -> c
            .title("6. 高压时代(HV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.HV]));

    public static final Quest POLYETHYLENE_FOIL = CHAPTER.quest("polyethylene_foil", q -> q
            .at(26, -2)
            .title("聚乙烯箔")
            .task(Task.item(TagPrefix.foil, GTMaterials.Polyethylene, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.Aluminium, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CAPACITOR = CHAPTER.quest("capacitor", q -> q
            .at(22, -2)
            .title("电容")
            .task(Task.item(GTItems.CAPACITOR, 16))
            .scaledReward(GTItems.CAPACITOR, 8)
            .dependsOn(POLYETHYLENE_FOIL));

    public static final Quest ELECTRUM = CHAPTER.quest("electrum", q -> q
            .at(16, -4)
            .title("琥珀金")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Electrum, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Silver, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest HV_COMPONENTS = CHAPTER.quest("hv_components", q -> q
            .at(16, -2)
            .title("HV 部件")
            .icon(GTItems.ELECTRIC_MOTOR_HV)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_HV, 2),
                    Task.item(GTItems.ELECTRIC_PISTON_HV),
                    Task.item(GTItems.ELECTRIC_PUMP_HV),
                    Task.item(GTItems.CONVEYOR_MODULE_HV, 2),
                    Task.item(GTItems.ROBOT_ARM_HV))
            .scaledReward(GTItems.ELECTRIC_MOTOR_HV, 1)
            .dependsOn(ELECTRUM));

    public static final QuestLink HV_COMPONENTS_COPY_1 = CHAPTER.copy(HV_COMPONENTS, 20, 2);

    public static final QuestLink HV_COMPONENTS_COPY_2 = CHAPTER.copy(HV_COMPONENTS, 16, 6);

    public static final QuestLink HV_COMPONENTS_COPY_3 = CHAPTER.copy(HV_COMPONENTS, 10, 14);

    public static final QuestLink HV_COMPONENTS_COPY_4 = CHAPTER.copy(HV_COMPONENTS, 4, 6);

    public static final QuestLink HV_COMPONENTS_COPY_5 = CHAPTER.copy(HV_COMPONENTS, 4, 18);

    public static final Quest HV_GENERATOR = CHAPTER.quest("hv_generator", q -> q
            .at(18, -2)
            .title("HV 发电")
            .task(Task.anyOf(2, GTMachines.STEAM_TURBINE[GTValues.HV], GTMachines.COMBUSTION[GTValues.HV], GTMachines.GAS_TURBINE[GTValues.HV], GTOMachines.SEMI_FLUID_GENERATOR[GTValues.HV]).title("任意 HV 发电机"))
            .scaledReward(Items.COAL_BLOCK, 8)
            .dependsOn(HV_COMPONENTS));

    public static final Quest ITEM_FILTER = CHAPTER.quest("item_filter", q -> q
            .at(14, -2)
            .title("物品过滤器")
            .task(Task.item(TagPrefix.foil, GTMaterials.Zinc, 24), Task.item(GTItems.ITEM_FILTER, 3))
            .scaledReward(TagPrefix.foil, GTMaterials.Zinc, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BLUE_ALLOY = CHAPTER.quest("blue_alloy", q -> q
            .at(14, 2)
            .title("蓝色合金")
            .task(Task.item(TagPrefix.ingot, GTMaterials.BlueAlloy, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Electrotine, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CONCRETE = CHAPTER.quest("concrete", q -> q
            .at(16, 2)
            .title("混凝土")
            .task(Task.item(GTMaterials.Concrete.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Stone, 16)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CLEANROOM = CHAPTER.quest("cleanroom", q -> q
            .at(16, 0)
            .milestone()
            .title("超净间")
            .task(Task.item(GTMultiMachines.CLEANROOM), Task.item(GTBlocks.FILTER_CASING, 8), Task.item(GTBlocks.PLASTCRETE, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Polyethylene, 12)
            .dependsOn(ITEM_FILTER, BLUE_ALLOY, CONCRETE, HV_COMPONENTS));

    public static final Quest INDUCTOR = CHAPTER.quest("inductor", q -> q
            .at(24, 0)
            .title("电感")
            .task(Task.item(GTItems.INDUCTOR, 4))
            .scaledReward(TagPrefix.ring, GTMaterials.Steel, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PLASTIC_SUBSTRATE = CHAPTER.quest("plastic_substrate", q -> q
            .at(6, -2)
            .title("塑料基板")
            .task(Task.item(GTItems.PLASTIC_BOARD, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.Copper, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PLASTIC_BOARD = CHAPTER.quest("plastic_board", q -> q
            .at(8, 0)
            .milestone()
            .title("塑料印刷电路板")
            .task(Task.item(GTItems.PLASTIC_CIRCUIT_BOARD, 4))
            .scaledReward(GTItems.PLASTIC_CIRCUIT_BOARD, 2)
            .dependsOn(PLASTIC_SUBSTRATE));

    public static final Quest GLASS_LENS = CHAPTER.quest("glass_lens", q -> q
            .at(12, -2)
            .title("玻璃透镜")
            .task(Task.item(TagPrefix.lens, GTMaterials.Glass, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Glass, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final QuestLink GLASS_LENS_COPY_1 = CHAPTER.copy(GLASS_LENS, 26, 18);

    public static final QuestLink GLASS_LENS_COPY_2 = CHAPTER.copy(GLASS_LENS, -4, 12);

    public static final Quest CPU = CHAPTER.quest("cpu", q -> q
            .at(12, 0)
            .milestone()
            .title("CPU 芯片")
            .task(Task.item(TagPrefix.lens, GTMaterials.Diamond), Task.item(GTItems.CENTRAL_PROCESSING_UNIT, 8))
            .scaledReward(GTItems.CENTRAL_PROCESSING_UNIT, 4)
            .dependsOn(GLASS_LENS));

    public static final Quest MICROPROCESSOR = CHAPTER.quest("microprocessor", q -> q
            .at(20, 0)
            .milestone()
            .title("微型处理器")
            .icon(GTItems.PROCESSOR_MV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.MV]), Task.item(GTItems.PROCESSOR_MV, 6))
            .scaledReward(GTItems.PROCESSOR_MV, 2)
            .dependsOn(PLASTIC_BOARD, CPU, CAPACITOR));

    public static final Quest PROCESSOR_ASSEMBLY = CHAPTER.quest("processor_assembly", q -> q
            .at(22, 0)
            .milestone()
            .title("微型处理器集群")
            .task(Task.item(GTItems.PROCESSOR_ASSEMBLY_HV, 6))
            .scaledReward(GTItems.PROCESSOR_ASSEMBLY_HV, 2)
            .dependsOn(MICROPROCESSOR, INDUCTOR, CLEANROOM));

    public static final Quest WORKSTATION = CHAPTER.quest("workstation", q -> q
            .at(22, 8)
            .milestone()
            .title("微型处理器超级计算机")
            .task(Task.item(GTItems.WORKSTATION_EV, 2))
            .scaledReward(GTItems.WORKSTATION_EV, 1)
            .dependsOn(PROCESSOR_ASSEMBLY));

    public static final Quest BENTONITE = CHAPTER.quest("bentonite", q -> q
            .at(26, 8)
            .title("膨润土")
            .task(Task.item(TagPrefix.dust, GTMaterials.Bentonite, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final QuestLink BENTONITE_COPY_1 = CHAPTER.copy(BENTONITE, 2, 12);

    public static final Quest ORANGE_DYE = CHAPTER.quest("orange_dye", q -> q
            .at(26, 10)
            .title("橙色染料")
            .task(Task.item(GTMaterials.DyeOrange.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.ORANGE_DYE, 8)
            .dependsOn(BENTONITE));

    public static final Quest ORANGE_LENS = CHAPTER.quest("orange_lens", q -> q
            .at(26, 14)
            .title("橙色透镜")
            .icon(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Orange))
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.HV]), Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Orange)))
            .scaledReward(Items.ORANGE_DYE, 8)
            .dependsOn(ORANGE_DYE, GLASS_LENS_COPY_1));

    public static final Quest LPIC = CHAPTER.quest("lpic", q -> q
            .at(22, 10)
            .title("低功率集成电路")
            .icon(GTItems.LOW_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(GTMachines.CUTTER[GTValues.HV]), Task.item(GTItems.LOW_POWER_INTEGRATED_CIRCUIT, 4))
            .scaledReward(GTItems.LOW_POWER_INTEGRATED_CIRCUIT, 2)
            .dependsOn(ORANGE_LENS));

    public static final Quest HV_ASSEMBLER = CHAPTER.quest("hv_assembler", q -> q
            .at(20, 4)
            .title("HV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.HV]))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(HV_COMPONENTS_COPY_1));

    public static final QuestLink HV_ASSEMBLER_COPY_1 = CHAPTER.copy(HV_ASSEMBLER, 10, 6);

    public static final Quest BLACK_BRONZE = CHAPTER.quest("black_bronze", q -> q
            .at(18, 4)
            .title("黑青铜")
            .task(Task.item(TagPrefix.dust, GTMaterials.BlackBronze, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Electrum, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BLACK_STEEL = CHAPTER.quest("black_steel", q -> q
            .at(18, 6)
            .title("黑钢")
            .icon(TagPrefix.ingot, GTMaterials.BlackSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.BlackSteel, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Nickel, 2)
            .dependsOn(BLACK_BRONZE));

    public static final Quest HV_VOLTAGE_COIL = CHAPTER.quest("hv_voltage_coil", q -> q
            .at(20, 6)
            .title("HV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_HV)
            .task(Task.item(GTItems.VOLTAGE_COIL_HV, 2))
            .scaledReward(TagPrefix.rod, GTMaterials.SteelMagnetic, 1)
            .dependsOn(BLACK_STEEL, HV_ASSEMBLER));

    public static final Quest SODIUM_AND_POTASSIUM = CHAPTER.quest("sodium_and_potassium", q -> q
            .at(20, 12)
            .title("钠与钾")
            .icon(TagPrefix.dust, GTMaterials.Potassium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Sodium, 2), Task.item(TagPrefix.dust, GTMaterials.Potassium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.RockSalt, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SODIUM_POTASSIUM = CHAPTER.quest("sodium_potassium", q -> q
            .at(20, 10)
            .title("钠钾合金")
            .icon(GTMaterials.SodiumPotassium.getBucket())
            .task(Task.item(GTMaterials.SodiumPotassium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 1)
            .dependsOn(SODIUM_AND_POTASSIUM));

    public static final Quest BLAZE_POWDER = CHAPTER.quest("blaze_powder", q -> q
            .at(10, 12)
            .title("烈焰粉")
            .icon(Items.BLAZE_POWDER)
            .task(Task.item(GTMachines.CHEMICAL_REACTOR[GTValues.HV]), Task.item(Items.BLAZE_POWDER, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(HV_COMPONENTS_COPY_3));

    public static final QuestLink BLAZE_POWDER_COPY_1 = CHAPTER.copy(BLAZE_POWDER, -4, 4);

    public static final QuestLink BLAZE_POWDER_COPY_2 = CHAPTER.copy(BLAZE_POWDER, 18, 22);

    public static final QuestLink BLAZE_POWDER_COPY_3 = CHAPTER.copy(BLAZE_POWDER, 16, 10);

    public static final Quest ENDER_EYE = CHAPTER.quest("ender_eye", q -> q
            .at(12, 12)
            .title("末影之眼")
            .icon(Items.ENDER_EYE)
            .task(Task.item(Items.ENDER_EYE, 4))
            .scaledReward(Items.ENDER_PEARL, 2)
            .dependsOn(BLAZE_POWDER));

    public static final Quest HV_EMITTER_SENSOR = CHAPTER.quest("hv_emitter_sensor", q -> q
            .at(12, 10)
            .title("HV 发射器与传感器")
            .task(Task.item(GTItems.EMITTER_HV, 2), Task.item(GTItems.SENSOR_HV, 2))
            .scaledReward(Items.BLAZE_POWDER, 8)
            .dependsOn(ENDER_EYE));

    public static final QuestLink HV_EMITTER_SENSOR_COPY_1 = CHAPTER.copy(HV_EMITTER_SENSOR, 6, 6);

    public static final QuestLink HV_EMITTER_SENSOR_COPY_2 = CHAPTER.copy(HV_EMITTER_SENSOR, -2, 16);

    public static final QuestLink HV_EMITTER_SENSOR_COPY_3 = CHAPTER.copy(HV_EMITTER_SENSOR, 2, 20);

    public static final QuestLink HV_EMITTER_SENSOR_COPY_4 = CHAPTER.copy(HV_EMITTER_SENSOR, 2, 14);

    public static final Quest HV_ENERGY_HATCH = CHAPTER.quest("hv_energy_hatch", q -> q
            .at(20, 8)
            .milestone()
            .title("HV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.HV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Gold, 4)
            .dependsOn(HV_VOLTAGE_COIL, SODIUM_POTASSIUM, LPIC));

    public static final Quest PHOSPHORUS = CHAPTER.quest("phosphorus", q -> q
            .at(-4, 10)
            .title("磷")
            .task(Task.item(TagPrefix.dust, GTMaterials.Phosphorus, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 18)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PHOSPHORUS_BOULE = CHAPTER.quest("phosphorus_boule", q -> q
            .at(-2, 10)
            .title("磷晶柱")
            .task(Task.item(GTItems.PHOSPHORUS_BOULE))
            .scaledReward(TagPrefix.dust, GTMaterials.Phosphorus, 8)
            .dependsOn(HV_ENERGY_HATCH, PHOSPHORUS));

    public static final Quest PHOSPHORUS_WAFER = CHAPTER.quest("phosphorus_wafer", q -> q
            .at(-2, 12)
            .title("磷晶圆")
            .task(Task.item(GTItems.PHOSPHORUS_WAFER, 2))
            .scaledReward(GTItems.PHOSPHORUS_WAFER, 1)
            .dependsOn(PHOSPHORUS_BOULE));

    public static final Quest DYE_LENSES = CHAPTER.quest("dye_lenses", q -> q
            .at(-4, 14)
            .title("粉色、灰色与青色透镜")
            .icon(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Pink))
            .task(
                    Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Pink)),
                    Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Gray)),
                    Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Cyan)))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 6)
            .dependsOn(GLASS_LENS_COPY_2));

    public static final Quest MEMORY_CHIPS = CHAPTER.quest("memory_chips", q -> q
            .at(-2, 14)
            .title("存储芯片")
            .icon(GTItems.NAND_MEMORY_CHIP)
            .task(
                    Task.item(GTMachines.LASER_ENGRAVER[GTValues.HV]),
                    Task.item(GTItems.NOR_MEMORY_CHIP, 16),
                    Task.item(GTItems.NAND_MEMORY_CHIP, 32))
            .scaledReward(GTItems.NAND_MEMORY_CHIP, 8)
            .dependsOn(PHOSPHORUS_WAFER, DYE_LENSES, HV_EMITTER_SENSOR_COPY_2));

    public static final QuestLink MEMORY_CHIPS_COPY_1 = CHAPTER.copy(MEMORY_CHIPS, 10, 22);

    public static final Quest SIMPLE_SOC = CHAPTER.quest("simple_soc", q -> q
            .at(0, 16)
            .milestone()
            .title("简易 SoC")
            .icon(GTItems.SIMPLE_SYSTEM_ON_CHIP)
            .task(Task.item(GTItems.SIMPLE_SYSTEM_ON_CHIP, 12))
            .scaledReward(GTItems.SIMPLE_SYSTEM_ON_CHIP, 4)
            .dependsOn(MEMORY_CHIPS));

    public static final Quest DATA_STICK = CHAPTER.quest("data_stick", q -> q
            .at(12, 22)
            .title("数据棒")
            .icon(GTItems.TOOL_DATA_STICK)
            .task(Task.item(GTItems.TOOL_DATA_STICK, 2))
            .scaledReward(GTItems.NAND_MEMORY_CHIP, 8)
            .dependsOn(MEMORY_CHIPS_COPY_1, MICROPROCESSOR));

    public static final Quest PLANET_DATA_CHIP = CHAPTER.quest("planet_data_chip", q -> q
            .at(12, 20)
            .title("星球数据芯片")
            .icon(GTOItems.PLANET_DATA_CHIP)
            .task(Task.item(GTOItems.PLANET_DATA_CHIP))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 4)
            .dependsOn(DATA_STICK, SIMPLE_SOC));

    public static final Quest MV_SCANNER = CHAPTER.quest("mv_scanner", q -> q
            .at(10, 20)
            .title("MV 扫描仪")
            .task(Task.item(GTMachines.SCANNER[GTValues.MV]))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 1)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SALTPETER = CHAPTER.quest("saltpeter", q -> q
            .at(-4, 20)
            .title("硝石")
            .icon(TagPrefix.dust, GTMaterials.Saltpeter)
            .task(Task.item(TagPrefix.dust, GTMaterials.Saltpeter, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest GUNPOWDER = CHAPTER.quest("gunpowder", q -> q
            .at(-4, 18)
            .title("火药")
            .icon(Items.GUNPOWDER)
            .task(Task.item(Items.GUNPOWDER, 48))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 8)
            .dependsOn(SALTPETER));

    public static final Quest POWDER_BARREL = CHAPTER.quest("powder_barrel", q -> q
            .at(-2, 18)
            .title("火药桶")
            .icon(GTBlocks.POWDERBARREL)
            .task(Task.item(GTBlocks.POWDERBARREL, 12))
            .scaledReward(Items.GUNPOWDER, 16)
            .dependsOn(GUNPOWDER));

    public static final Quest IMPLOSION_COMPRESSOR = CHAPTER.quest("implosion_compressor", q -> q
            .at(4, 0)
            .milestone()
            .title("内爆压缩机")
            .icon(GTMultiMachines.IMPLOSION_COMPRESSOR)
            .task(Task.item(GTMultiMachines.IMPLOSION_COMPRESSOR))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest FLUORINE = CHAPTER.quest("fluorine", q -> q
            .at(-4, -2)
            .title("氟")
            .icon(GTMaterials.Fluorine.getBucket())
            .task(Task.item(GTMaterials.Fluorine.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.gem, GTMaterials.Topaz, 6)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest HYDROFLUORIC_ACID = CHAPTER.quest("hydrofluoric_acid", q -> q
            .at(-2, 0)
            .title("氢氟酸")
            .icon(GTMaterials.HydrofluoricAcid.getBucket())
            .task(Task.item(GTMaterials.HydrofluoricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 2)
            .dependsOn(FLUORINE));

    public static final Quest METHANE = CHAPTER.quest("methane", q -> q
            .at(-2, 8)
            .title("甲烷")
            .icon(GTMaterials.Methane.getBucket())
            .task(Task.item(GTMaterials.Methane.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CHLOROFORM = CHAPTER.quest("chloroform", q -> q
            .at(-2, 6)
            .title("氯仿")
            .icon(GTMaterials.Chloroform.getBucket())
            .task(Task.item(GTMaterials.Chloroform.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(METHANE));

    public static final Quest TETRAFLUOROETHYLENE = CHAPTER.quest("tetrafluoroethylene", q -> q
            .at(-2, 4)
            .title("四氟乙烯")
            .icon(GTMaterials.Tetrafluoroethylene.getBucket())
            .task(Task.item(GTMaterials.Tetrafluoroethylene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(HYDROFLUORIC_ACID, CHLOROFORM, BLAZE_POWDER_COPY_1));

    public static final Quest POLYMERIZATION_REACTOR = CHAPTER.quest("polymerization_reactor", q -> q
            .at(16, 8)
            .milestone()
            .title("聚合反应器")
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .scaledReward(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 6)
            .dependsOn(WORKSTATION, HV_COMPONENTS_COPY_2));

    public static final Quest PTFE = CHAPTER.quest("ptfe", q -> q
            .at(0, 6)
            .title("聚四氟乙烯")
            .icon(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 36))
            .scaledReward(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 6)
            .dependsOn(POLYMERIZATION_REACTOR, TETRAFLUOROETHYLENE));

    public static final Quest HERMETIC_CASINGS = CHAPTER.quest("hermetic_casings", q -> q
            .at(0, 8)
            .milestone()
            .title("密封外壳")
            .icon(GTBlocks.HERMETIC_CASING_HV)
            .task(Task.item(GTBlocks.HERMETIC_CASING_LV, 24), Task.item(GTBlocks.HERMETIC_CASING_HV, 23))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 16)
            .dependsOn(PTFE));

    public static final Quest AE_PRESSES = CHAPTER.quest("ae_presses", q -> q
            .at(-2, 20)
            .title("压印模板")
            .icon(AEItems.LOGIC_PROCESSOR_PRESS)
            .task(Task.item(AEItems.LOGIC_PROCESSOR_PRESS), Task.item(AEItems.SILICON_PRESS))
            .scaledReward(TagPrefix.plate, GTMaterials.Gold, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest LOGIC_PROCESSOR = CHAPTER.quest("logic_processor", q -> q
            .at(-2, 22)
            .title("逻辑处理器")
            .icon(AEItems.LOGIC_PROCESSOR)
            .task(Task.item(AEItems.LOGIC_PROCESSOR, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Silicon, 4)
            .dependsOn(AE_PRESSES));

    public static final Quest AE_CORES = CHAPTER.quest("ae_cores", q -> q
            .at(0, 22)
            .title("构型核心与湮灭核心")
            .icon(AEItems.FORMATION_CORE)
            .task(Task.item(AEItems.FORMATION_CORE, 2), Task.item(AEItems.ANNIHILATION_CORE, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.CertusQuartz, 4)
            .dependsOn(LOGIC_PROCESSOR));

    public static final Quest MOLECULAR_ASSEMBLER = CHAPTER.quest("molecular_assembler", q -> q
            .at(0, 20)
            .title("分子装配室")
            .task(Task.item(AEBlocks.MOLECULAR_ASSEMBLER))
            .scaledReward(TagPrefix.plate, GTMaterials.Invar, 6)
            .dependsOn(AE_CORES));

    public static final Quest NASA_WORKBENCH = CHAPTER.quest("nasa_workbench", q -> q
            .at(2, 18)
            .title("NASA 工作台")
            .task(Task.item(ModItems.NASA_WORKBENCH.get()))
            .scaledReward(GTItems.ROBOT_ARM_HV, 1)
            .dependsOn(MOLECULAR_ASSEMBLER, HV_EMITTER_SENSOR_COPY_3, HV_COMPONENTS_COPY_5));

    public static final Quest IMPLOSION_COMPRESSOR_PRODUCT = CHAPTER.quest("implosion_compressor_product", q -> q
            .at(0, 18)
            .title("一阶重型板")
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_1, 8))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(IMPLOSION_COMPRESSOR, POWDER_BARREL));

    public static final Quest ROCKET_ASSEMBLER = CHAPTER.quest("rocket_assembler", q -> q
            .at(2, 16)
            .milestone()
            .title("火箭装配机")
            .icon(MultiBlockG.ROCKET_ASSEMBLER)
            .task(Task.item(MultiBlockG.ROCKET_ASSEMBLER), Task.item(GTBlocks.CASING_GRATE, 49))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(NASA_WORKBENCH, HERMETIC_CASINGS, IMPLOSION_COMPRESSOR_PRODUCT, HV_EMITTER_SENSOR_COPY_4));

    public static final Quest AMMONIA = CHAPTER.quest("ammonia", q -> q
            .at(18, 20)
            .title("氨")
            .icon(GTMaterials.Ammonia.getBucket())
            .task(Task.item(GTMaterials.Ammonia.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 4)
            .dependsOn(BLAZE_POWDER_COPY_2));

    public static final Quest HYPOCHLOROUS_ACID = CHAPTER.quest("hypochlorous_acid", q -> q
            .at(16, 18)
            .title("次氯酸")
            .task(Task.item(GTMaterials.HypochlorousAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest DIMETHYLHYDRAZINE = CHAPTER.quest("dimethylhydrazine", q -> q
            .at(16, 20)
            .title("偏二甲肼")
            .icon(GTMaterials.Dimethylhydrazine.getBucket())
            .task(Task.item(GTMaterials.Dimethylhydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Methanol.getBucket(), 2)
            .dependsOn(AMMONIA, HYPOCHLOROUS_ACID));

    public static final Quest ROCKET_FUEL = CHAPTER.quest("rocket_fuel", q -> q
            .at(14, 20)
            .title("火箭燃料")
            .task(Task.item(GTMaterials.RocketFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 4)
            .dependsOn(DIMETHYLHYDRAZINE));

    public static final Quest ENGINE_PARTS = CHAPTER.quest("engine_parts", q -> q
            .at(8, 14)
            .title("储罐与引擎配件")
            .icon(ModItems.STEEL_TANK.get())
            .task(Task.item(ModItems.STEEL_TANK.get(), 8), Task.item(ModItems.FAN.get(), 4), Task.item(GTItems.LIQUID_FUEL_JETPACK, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest GLASS_GEM = CHAPTER.quest("glass_gem", q -> q
            .at(6, 22)
            .title("玻璃宝石")
            .icon(TagPrefix.gem, GTMaterials.Glass)
            .task(Task.item(TagPrefix.gem, GTMaterials.Glass, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Glass, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest EXQUISITE_GLASS = CHAPTER.quest("exquisite_glass", q -> q
            .at(6, 20)
            .title("完美玻璃宝石")
            .icon(TagPrefix.gemExquisite, GTMaterials.Glass)
            .task(Task.item(TagPrefix.gemExquisite, GTMaterials.Glass))
            .scaledReward(TagPrefix.gem, GTMaterials.Glass, 2)
            .dependsOn(GLASS_GEM));

    public static final Quest NOSE_CONE = CHAPTER.quest("rocket_nose_cone", q -> q
            .at(6, 18)
            .title("火箭鼻锥")
            .icon(ModItems.ROCKET_NOSE_CONE.get())
            .task(Task.item(ModItems.ROCKET_NOSE_CONE.get()))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 2)
            .dependsOn(EXQUISITE_GLASS));

    public static final Quest COBALT_PIPE = CHAPTER.quest("cobalt_restrictive_pipe", q -> q
            .at(8, 22)
            .title("钴限流管")
            .icon(TagPrefix.pipeNormalRestrictive, GTMaterials.Cobalt)
            .task(Task.item(TagPrefix.pipeNormalRestrictive, GTMaterials.Cobalt, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Cobalt, 6)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest ENGINE_FRAME = CHAPTER.quest("engine_frame", q -> q
            .at(8, 20)
            .title("引擎框架")
            .icon(ModItems.ENGINE_FRAME.get())
            .task(Task.item(ModItems.ENGINE_FRAME.get(), 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.BlackSteel, 4)
            .dependsOn(COBALT_PIPE));

    public static final Quest STEEL_ENGINE = CHAPTER.quest("steel_engine", q -> q
            .at(8, 18)
            .title("钢引擎")
            .icon(ModItems.STEEL_ENGINE.get())
            .task(Task.item(ModItems.STEEL_ENGINE.get(), 4))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(ENGINE_FRAME));

    public static final Quest MAGNESIUM = CHAPTER.quest("magnesium", q -> q
            .at(4, 12)
            .title("镁")
            .task(Task.item(TagPrefix.dust, GTMaterials.Magnesium, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 30)
            .dependsOn(BENTONITE_COPY_1));

    public static final QuestLink MAGNESIUM_COPY_1 = CHAPTER.copy(MAGNESIUM, 18, 12);

    public static final Quest MAGNALIUM = CHAPTER.quest("magnalium", q -> q
            .at(4, 14)
            .title("镁铝合金与铝青铜")
            .icon(TagPrefix.dust, GTMaterials.Magnalium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Magnalium, 36), Task.item(TagPrefix.plateDouble, GTOMaterials.AluminiumBronze, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 8)
            .dependsOn(MAGNESIUM));

    public static final Quest ROCKET_FIN = CHAPTER.quest("rocket_fin", q -> q
            .at(6, 14)
            .title("火箭尾翼")
            .icon(ModItems.ROCKET_FIN.get())
            .task(Task.item(ModItems.ROCKET_FIN.get(), 4))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 1)
            .dependsOn(MAGNALIUM));

    public static final Quest TIER_1_ROCKET = CHAPTER.quest("tier_1_rocket", q -> q
            .at(8, 16)
            .milestone()
            .title("T1 火箭")
            .task(Task.item(ModItems.TIER_1_ROCKET.get()))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(NOSE_CONE, STEEL_ENGINE, ROCKET_FIN, ENGINE_PARTS, ROCKET_ASSEMBLER));

    public static final Quest TANTALUM = CHAPTER.quest("tantalum", q -> q
            .at(6, 12)
            .title("钽")
            .icon(TagPrefix.dust, GTMaterials.Tantalum)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tantalum, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Tantalite, 9)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest ANTIMONY = CHAPTER.quest("antimony", q -> q
            .at(12, 2)
            .title("锑")
            .icon(TagPrefix.ingot, GTMaterials.Antimony)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Antimony, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Antimony, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BATTERY_ALLOY = CHAPTER.quest("battery_alloy", q -> q
            .at(12, 4)
            .title("电池合金")
            .icon(TagPrefix.ingot, GTMaterials.BatteryAlloy)
            .task(Task.item(TagPrefix.plate, GTMaterials.BatteryAlloy, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Lead, 16)
            .dependsOn(ANTIMONY));

    public static final Quest CADMIUM_BATTERY = CHAPTER.quest("cadmium_battery", q -> q
            .at(12, 6)
            .title("镉电池")
            .icon(GTItems.BATTERY_HV_CADMIUM)
            .task(Task.item(TagPrefix.dust, GTMaterials.Cadmium, 64), Task.item(GTItems.BATTERY_HV_CADMIUM, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Cadmium, 16)
            .dependsOn(BATTERY_ALLOY));

    public static final Quest TANTALUM_BATTERY = CHAPTER.quest("tantalum_battery", q -> q
            .at(6, 10)
            .title("钽电容")
            .icon(GTItems.BATTERY_ULV_TANTALUM)
            .task(Task.item(GTItems.BATTERY_ULV_TANTALUM, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Manganese, 8)
            .dependsOn(TANTALUM));

    public static final Quest SUPER_CAPACITOR = CHAPTER.quest("super_capacitor", q -> q
            .at(8, 10)
            .title("超级电容")
            .icon(GTOItems.SUPER_CAPACITOR)
            .task(Task.item(GTOItems.SUPER_CAPACITOR, 2))
            .scaledReward(TagPrefix.rod, GTMaterials.Lapis, 8)
            .dependsOn(TANTALUM_BATTERY));

    public static final Quest SOLAR_COVER = CHAPTER.quest("solar_cover", q -> q
            .at(10, 10)
            .title("太阳能板")
            .icon(GTItems.COVER_SOLAR_PANEL)
            .task(Task.item(GTItems.COVER_SOLAR_PANEL, 2))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(SUPER_CAPACITOR));

    public static final Quest SCAN_SATELLITE = CHAPTER.quest("scan_satellite", q -> q
            .at(12, 8)
            .milestone()
            .title("行星扫描卫星")
            .task(Task.item(GTOItems.PLANET_SCAN_SATELLITE))
            .scaledReward(TagPrefix.dust, GTMaterials.Cadmium, 16)
            .dependsOn(SOLAR_COVER, CADMIUM_BATTERY, HV_EMITTER_SENSOR, HV_ASSEMBLER_COPY_1));

    public static final Quest FLUIX = CHAPTER.quest("fluix", q -> q
            .at(8, 4)
            .title("福鲁伊克斯")
            .icon(AEItems.FLUIX_DUST)
            .task(Task.item(AEItems.FLUIX_DUST, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.NetherQuartz, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest WIRELESS_BOOSTER = CHAPTER.quest("wireless_booster", q -> q
            .at(8, 6)
            .title("无线增幅器")
            .task(Task.item(AEItems.WIRELESS_BOOSTER))
            .scaledReward(AEItems.FLUIX_DUST, 2)
            .dependsOn(FLUIX));

    public static final Quest SATELLITE_CONTROL_CENTER = CHAPTER.quest("satellite_control_center", q -> q
            .at(8, 8)
            .milestone()
            .title("卫星控制中心")
            .icon(MultiBlockG.SATELLITE_CONTROL_CENTER)
            .task(Task.item(MultiBlockG.SATELLITE_CONTROL_CENTER), Task.item(GTOBlocks.MULTI_FUNCTIONAL_CASING, 31))
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 4)
            .dependsOn(HV_EMITTER_SENSOR_COPY_1, WIRELESS_BOOSTER));

    public static final Quest UNLOCK_MOON = CHAPTER.quest("unlock_moon", q -> q
            .at(12, 18)
            .title("解锁月球")
            .task(Task.checkmark())
            .reward(Reward.item(ModItems.LAUNCH_PAD.get(), 1))
            .dependsOn(SATELLITE_CONTROL_CENTER, SCAN_SATELLITE, TIER_1_ROCKET, ROCKET_FUEL, PLANET_DATA_CHIP, MV_SCANNER));

    public static final Quest ORGANS = CHAPTER.quest("organs", q -> q
            .at(14, 18)
            .title("标准级器官")
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.STANDARD)))
            .scaledReward(GTItems.ROBOT_ARM_MV, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SPACE_SUIT = CHAPTER.quest("space_suit", q -> q
            .at(12, 14)
            .title("宇航服")
            .task(
                    Task.item(ModItems.SPACE_HELMET.get()),
                    Task.item(ModItems.SPACE_SUIT.get()),
                    Task.item(ModItems.SPACE_PANTS.get()),
                    Task.item(ModItems.SPACE_BOOTS.get()))
            .scaledReward(ModItems.STEEL_PLATE.get(), 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest MOON = CHAPTER.quest("moon", q -> q
            .at(14, 16)
            .milestone()
            .title("登陆月球")
            .icon(ModItems.MOON_GLOBE.get())
            .task(Task.dimension(Dimension.MOON).title("造访月球").icon(ModItems.MOON_GLOBE.get()))
            .scaledReward(TagPrefix.dust, GTMaterials.Magnesium, 8)
            .dependsOn(UNLOCK_MOON, ORGANS, SPACE_SUIT));

    public static final Quest ILMENITE = CHAPTER.quest("ilmenite", q -> q
            .at(16, 14)
            .title("钛铁矿")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 4)
            .dependsOn(MOON));

    public static final Quest BAUXITE = CHAPTER.quest("bauxite", q -> q
            .at(14, 14)
            .title("铝土矿")
            .icon(TagPrefix.rawOre, GTMaterials.Bauxite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bauxite, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Bauxite, 15)
            .dependsOn(MOON));

    public static final Quest RUTILE = CHAPTER.quest("rutile", q -> q
            .at(14, 12)
            .title("金红石")
            .task(Task.item(TagPrefix.dust, GTMaterials.Rutile, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Rutile, 2)
            .dependsOn(ILMENITE, BAUXITE));

    public static final Quest TITANIUM_TETRACHLORIDE = CHAPTER.quest("titanium_tetrachloride", q -> q
            .at(16, 12)
            .title("四氯化钛")
            .task(Task.item(GTMaterials.TitaniumTetrachloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(RUTILE, BLAZE_POWDER_COPY_3));

    public static final Quest HOT_TITANIUM = CHAPTER.quest("hot_titanium", q -> q
            .at(18, 14)
            .title("热钛锭")
            .task(Task.item(TagPrefix.ingotHot, GTMaterials.Titanium, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Magnesium, 8)
            .dependsOn(TITANIUM_TETRACHLORIDE, MAGNESIUM_COPY_1));

    public static final Quest VACUUM_FREEZER = CHAPTER.quest("vacuum_freezer", q -> q
            .at(4, 8)
            .milestone()
            .title("真空冷冻机")
            .task(Task.item(GTMultiMachines.VACUUM_FREEZER))
            .scaledReward(GTItems.ELECTRIC_PUMP_HV, 1)
            .dependsOn(HV_COMPONENTS_COPY_4, WORKSTATION));

    public static final Quest TITANIUM = CHAPTER.quest("titanium", q -> q
            .at(18, 16)
            .milestone()
            .title("钛锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Titanium, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.Titanium, 2)
            .dependsOn(HOT_TITANIUM, VACUUM_FREEZER));

    public static final Quest EV_CASING = CHAPTER.quest("ev_casing", q -> q
            .at(20, 14)
            .title("EV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_EV))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 1)
            .dependsOn(TITANIUM));

    public static final Quest EV_HULL = CHAPTER.quest("ev_hull", q -> q
            .at(22, 16)
            .milestone()
            .title("EV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.EV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.EV], 1))
            .dependsOn(EV_CASING));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewMvChapter.HV_HULL, 0, 0))
                .mainLine(NewMvChapter.HV_HULL, IMPLOSION_COMPRESSOR, PLASTIC_BOARD, CPU, CLEANROOM, MICROPROCESSOR, PROCESSOR_ASSEMBLY, WORKSTATION, HV_ENERGY_HATCH, POLYMERIZATION_REACTOR, SCAN_SATELLITE, SATELLITE_CONTROL_CENTER, VACUUM_FREEZER, HERMETIC_CASINGS, SIMPLE_SOC, ROCKET_ASSEMBLER, TIER_1_ROCKET, MOON, TITANIUM, EV_HULL));
    }
}
