package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOFluids;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.MachinesNode;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewZpmChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_zpm", c -> c
            .title("10. 零点压时代(ZPM)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.ZPM]));

    public static final Quest DURANIUM = CHAPTER.quest("duranium", q -> q
            .at(0, 2)
            .title("铿铀")
            .icon(TagPrefix.ingot, GTMaterials.Duranium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Duranium, 4),
                    Task.item(TagPrefix.pipeSmallFluid, GTMaterials.Duranium, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Gallium, 8)
            .dependsOn(NewLuvChapter.LUV_FUSION_REACTOR, NewLuvChapter.ZPM_HULL));

    public static final Quest EUROPIUM_FINE_WIRE = CHAPTER.quest("europium_fine_wire", q -> q
            .at(18, 8)
            .title("铕细线")
            .task(Task.item(TagPrefix.wireFine, GTMaterials.Europium, 64))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Europium, 4)
            .dependsOn(NewLuvChapter.EUROPIUM));

    public static final Quest BUTADIENE_AND_STYRENE = CHAPTER.quest("butadiene_and_styrene", q -> q
            .at(20, 8)
            .title("丁二烯与苯乙烯")
            .icon(GTMaterials.Butadiene.getBucket())
            .task(Task.item(GTMaterials.Butadiene.getBucket()).optional(),
                    Task.item(GTMaterials.Styrene.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Benzene.getBucket(), 4)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest RAW_SBR = CHAPTER.quest("raw_styrene_butadiene_rubber", q -> q
            .at(20, 6)
            .title("生丁苯橡胶")
            .task(Task.item(TagPrefix.dust, GTMaterials.RawStyreneButadieneRubber, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(BUTADIENE_AND_STYRENE));

    public static final Quest SBR = CHAPTER.quest("styrene_butadiene_rubber", q -> q
            .at(20, 4)
            .title("丁苯橡胶")
            .icon(GTMaterials.StyreneButadieneRubber.getBucket())
            .task(Task.item(GTMaterials.StyreneButadieneRubber.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Styrene.getBucket(), 4)
            .dependsOn(RAW_SBR));

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE1 = CHAPTER.quest("tech_component_in_assembly_line1", q -> q
            .at(16, 6)
            .title("装配线进阶部件I研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLinezpm))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest ZPM_MOTOR = CHAPTER.quest("zpm_electric_motor", q -> q
            .at(18, 6)
            .title("ZPM 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_ZPM, 8))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Europium, 16)
            .dependsOn(EUROPIUM_FINE_WIRE, TECH_COMPONENT_IN_ASSEMBLY_LINE1));

    public static final Quest ZPM_PARTS = CHAPTER.quest("zpm_parts", q -> q
            .at(18, 4)
            .title("ZPM 部件")
            .icon(GTItems.ROBOT_ARM_ZPM)
            .task(Task.item(GTItems.ELECTRIC_PISTON_ZPM, 6),
                    Task.item(GTItems.ROBOT_ARM_ZPM, 6),
                    Task.item(GTItems.CONVEYOR_MODULE_ZPM, 6),
                    Task.item(GTItems.ELECTRIC_PUMP_ZPM, 14))
            .scaledReward(GTItems.ELECTRIC_MOTOR_ZPM, 1)
            .dependsOn(ZPM_MOTOR, SBR));

    public static final Quest ZPM_ASSEMBLER = CHAPTER.quest("zpm_assembler", q -> q
            .at(16, 4)
            .title("ZPM 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.ZPM]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 2)
            .dependsOn(ZPM_PARTS));

    public static final Quest TRINIUM_COIL = CHAPTER.quest("trinium_coil_block", q -> q
            .at(16, 2)
            .title("凯金线圈方块")
            .task(Task.item(GTBlocks.COIL_TRINIUM, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.NaquadahEnriched, 8)
            .dependsOn(ZPM_ASSEMBLER));

    public static final Quest ZPM_VOLTAGE_COIL = CHAPTER.quest("zpm_voltage_coil", q -> q
            .at(14, 4)
            .title("ZPM 电压线圈")
            .task(Task.item(GTItems.VOLTAGE_COIL_ZPM, 20))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Europium, 16)
            .dependsOn(ZPM_ASSEMBLER));

    public static final Quest URHNQ_DUST = CHAPTER.quest("uranium_rhodium_dinaquadide_dust", q -> q
            .at(18, 2)
            .title("铀铑硅岩合金粉")
            .icon(TagPrefix.dust, GTMaterials.UraniumRhodiumDinaquadide)
            .task(Task.item(GTMachines.MIXER[GTValues.ZPM]),
                    Task.item(TagPrefix.dust, GTMaterials.UraniumRhodiumDinaquadide, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Rhodium, 4)
            .dependsOn(ZPM_PARTS));

    public static final Quest URHNQ_INGOT = CHAPTER.quest("uranium_rhodium_dinaquadide_ingot", q -> q
            .at(16, 0)
            .milestone()
            .title("铀铑硅岩合金锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.UraniumRhodiumDinaquadide, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.UraniumRhodiumDinaquadide, 8)
            .dependsOn(URHNQ_DUST, TRINIUM_COIL));

    public static final Quest URHNQ_WIRE = CHAPTER.quest("uranium_rhodium_dinaquadide_wire", q -> q
            .at(24, 6)
            .title("1x 铀铑硅岩合金导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.UraniumRhodiumDinaquadide, 64))
            .scaledReward(TagPrefix.ingot, GTMaterials.UraniumRhodiumDinaquadide, 8)
            .dependsOn(URHNQ_INGOT));

    public static final Quest TETRACHLOROSILANE = CHAPTER.quest("tetrachlorosilane", q -> q
            .at(24, 8)
            .title("四氯硅烷")
            .icon(GTOMaterials.Tetrachlorosilane.getBucket())
            .task(Task.item(GTOMaterials.Tetrachlorosilane.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Silicon, 8)
            .dependsOn(URHNQ_WIRE));

    public static final QuestLink TETRACHLOROSILANE_COPY_1 = CHAPTER.copy(TETRACHLOROSILANE, 22, 4);

    public static final Quest HIGH_PURITY_SILICA = CHAPTER.quest("high_purity_silica", q -> q
            .at(24, 4)
            .title("高纯度二氧化硅")
            .task(Task.item(TagPrefix.dust, GTOMaterials.HighPuritySilica, 128))
            .scaledReward(GTOMaterials.Tetrachlorosilane.getBucket(), 8)
            .dependsOn(TETRACHLOROSILANE_COPY_1));

    public static final Quest SILICA_COLUMN = CHAPTER.quest("high_purity_silica_column", q -> q
            .at(24, 2)
            .title("高纯度二氧化硅柱")
            .task(Task.item(GTOItems.HIGH_PURITY_SILICA_COLUMN))
            .scaledReward(TagPrefix.dust, GTOMaterials.HighPuritySilica, 32)
            .dependsOn(HIGH_PURITY_SILICA));

    public static final Quest SILICA_TUBE = CHAPTER.quest("high_purity_silica_tube", q -> q
            .at(26, 2)
            .title("高纯石英管")
            .task(Task.item(GTOItems.HIGH_PURITY_SILICA_TUBE))
            .scaledReward(GTOMaterials.SodiumHydroxideSolution.getBucket(), 2)
            .dependsOn(SILICA_COLUMN));

    public static final Quest ASH_LEACHING = CHAPTER.quest("ash_leaching_solution", q -> q
            .at(26, 8)
            .title("灰烬浸出液")
            .icon(GTOMaterials.AshLeachingSolution.getBucket())
            .task(Task.item(GTOMaterials.AshLeachingSolution.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Ash, 24)
            .dependsOn(TETRACHLOROSILANE));

    public static final Quest GERMANIUM_PRECIPITATE = CHAPTER.quest("germanium_containing_precipitate", q -> q
            .at(26, 6)
            .title("含锗沉淀物")
            .task(Task.item(TagPrefix.dust, GTOMaterials.GermaniumContainingPrecipitate, 4))
            .scaledReward(Items.NETHER_WART_BLOCK, 16)
            .dependsOn(ASH_LEACHING));

    public static final Quest GERMANIUM_ASH = CHAPTER.quest("germanium_ash", q -> q
            .at(28, 6)
            .title("锗灰")
            .task(Task.item(TagPrefix.dust, GTOMaterials.GermaniumAsh, 2))
            .scaledReward(TagPrefix.dust, GTOMaterials.GermaniumContainingPrecipitate, 2)
            .dependsOn(GERMANIUM_PRECIPITATE));

    public static final Quest GERMANIUM_TETRACHLORIDE = CHAPTER.quest("germanium_tetrachloride", q -> q
            .at(28, 4)
            .title("四氯化锗")
            .icon(GTOMaterials.GermaniumTetrachlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.GermaniumTetrachlorideSolution.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.GermaniumAsh, 2)
            .dependsOn(GERMANIUM_ASH));

    public static final Quest CARBON_TETRAFLUORIDE = CHAPTER.quest("carbon_tetrafluoride", q -> q
            .at(26, 4)
            .title("四氟化碳")
            .icon(GTOMaterials.CarbonTetrafluoride.getBucket())
            .task(Task.item(GTOMaterials.CarbonTetrafluoride.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 4)
            .dependsOn(SILICA_TUBE));

    public static final Quest FIBER_PREFORM = CHAPTER.quest("simple_optical_fiber_preform", q -> q
            .at(28, 2)
            .title("光纤预制棒")
            .task(Task.item(GTOItems.SIMPLE_OPTICAL_FIBER_PREFORM))
            .scaledReward(GTOMaterials.Tetrachlorosilane.getBucket(), 4)
            .dependsOn(SILICA_TUBE, GERMANIUM_TETRACHLORIDE, CARBON_TETRAFLUORIDE));

    public static final QuestLink FIBER_PREFORM_COPY_1 = CHAPTER.copy(FIBER_PREFORM, 36, 8);

    public static final QuestLink FIBER_PREFORM_COPY_2 = CHAPTER.copy(FIBER_PREFORM, 36, 14);

    public static final Quest DRAWING_TOWER = CHAPTER.quest("drawing_tower", q -> q
            .at(26, 0)
            .milestone()
            .title("拉丝塔")
            .icon(MultiBlockG.DRAWING_TOWER)
            .task(Task.item(MultiBlockG.DRAWING_TOWER), Task.item(GTOMachines.SPOOL_HATCH))
            .scaledReward(GTOItems.SPOOLS_MICRO, 2)
            .dependsOn(FIBER_PREFORM));

    public static final Quest DRAWING_TOWER_PRODUCT = CHAPTER.quest("drawing_tower_product", q -> q
            .at(34, 10)
            .title("微型线轴")
            .task(Task.item(GTOItems.SPOOLS_MICRO, 4))
            .scaledReward(GTOItems.SPOOLS_MICRO, 2)
            .dependsOn(DRAWING_TOWER));

    public static final Quest FIBER_ROUGH = CHAPTER.quest("simple_fiber_optic_rough", q -> q
            .at(34, 8)
            .title("粗光纤")
            .task(Task.item(GTOItems.SIMPLE_FIBER_OPTIC_ROUGH, 64))
            .scaledReward(GTOItems.SIMPLE_FIBER_OPTIC_ROUGH, 16)
            .dependsOn(DRAWING_TOWER_PRODUCT));

    public static final Quest ALLYL_CHLORIDE = CHAPTER.quest("allyl_chloride", q -> q
            .at(36, 6)
            .title("烯丙基氯")
            .icon(GTMaterials.AllylChloride.getBucket())
            .task(Task.item(GTMaterials.AllylChloride.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Propene.getBucket(), 4)
            .dependsOn(FIBER_PREFORM_COPY_1));

    public static final Quest ACRYLIC_ACID = CHAPTER.quest("acrylic_acid", q -> q
            .at(34, 6)
            .title("丙烯酸")
            .icon(GTOMaterials.AcrylicAcid.getBucket())
            .task(Task.item(GTOMaterials.AcrylicAcid.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.AllylChloride.getBucket(), 2)
            .dependsOn(ALLYL_CHLORIDE));

    public static final Quest ETHYL_ACRYLATE = CHAPTER.quest("ethyl_acrylate", q -> q
            .at(32, 6)
            .title("丙烯酸乙酯")
            .icon(GTOMaterials.EthylAcrylate.getBucket())
            .task(Task.item(GTOMaterials.EthylAcrylate.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Ethanol.getBucket(), 4)
            .dependsOn(ACRYLIC_ACID));

    public static final Quest FIBER_OPTIC = CHAPTER.quest("simple_fiber_optic", q -> q
            .at(32, 8)
            .title("光纤")
            .task(Task.item(GTOItems.SIMPLE_FIBER_OPTIC, 64))
            .scaledReward(GTOItems.SIMPLE_FIBER_OPTIC_ROUGH, 16)
            .dependsOn(FIBER_ROUGH, ETHYL_ACRYLATE));

    public static final Quest CARBON_FIBER_YARN = CHAPTER.quest("carbon_fiber_yarn", q -> q
            .at(34, 14)
            .title("碳纤维丝")
            .task(Task.item(GTOTagPrefix.FIBER, GTMaterials.Carbon, 8))
            .scaledReward(GTItems.CARBON_FIBERS, 8)
            .dependsOn(FIBER_PREFORM_COPY_2));

    public static final Quest CARBON_MESH = CHAPTER.quest("carbon_mesh", q -> q
            .at(34, 12)
            .title("碳纤维网")
            .task(Task.item(GTItems.CARBON_MESH, 4))
            .scaledReward(GTOTagPrefix.FIBER, GTMaterials.Carbon, 4)
            .dependsOn(CARBON_FIBER_YARN));

    public static final Quest DUCT_TAPE = CHAPTER.quest("duct_tape", q -> q
            .at(32, 12)
            .title("胶带")
            .task(Task.item(GTItems.DUCT_TAPE, 16))
            .scaledReward(GTItems.CARBON_MESH, 1)
            .dependsOn(CARBON_MESH));

    public static final Quest OPTICAL_PIPE = CHAPTER.quest("normal_optical_pipe", q -> q
            .at(32, 10)
            .title("光缆")
            .task(Task.item(GTBlocks.OPTICAL_PIPES[0], 48))
            .scaledReward(GTOItems.SIMPLE_FIBER_OPTIC, 16)
            .dependsOn(FIBER_OPTIC, DUCT_TAPE));

    public static final QuestLink OPTICAL_PIPE_COPY_1 = CHAPTER.copy(OPTICAL_PIPE, 24, 16);

    public static final Quest COMPUTER_CASINGS = CHAPTER.quest("data_orb_and_computer_casings", q -> q
            .at(30, 12)
            .milestone()
            .title("数据球与计算机外壳")
            .icon(GTBlocks.COMPUTER_CASING)
            .task(Task.item(GTItems.TOOL_DATA_ORB, 8),
                    Task.item(GTBlocks.COMPUTER_CASING, 24),
                    Task.item(GTBlocks.ADVANCED_COMPUTER_CASING, 16),
                    Task.item(GTBlocks.COMPUTER_HEAT_VENT, 24))
            .scaledReward(TagPrefix.plate, GTMaterials.Iridium, 12)
            .dependsOn(OPTICAL_PIPE));

    public static final Quest OPTICAL_DATA_HATCH = CHAPTER.quest("optical_data_hatch", q -> q
            .at(30, 14)
            .title("光学数据仓")
            .icon(GTResearchMachines.DATA_HATCH_TRANSMITTER)
            .task(Task.item(GTResearchMachines.DATA_HATCH_TRANSMITTER),
                    Task.item(GTResearchMachines.DATA_HATCH_RECEIVER))
            .scaledReward(GTBlocks.OPTICAL_PIPES[0], 8)
            .dependsOn(COMPUTER_CASINGS));

    public static final Quest COMPUTATIONAL_DATA_HATCH = CHAPTER.quest("computational_data_hatch", q -> q
            .at(28, 16)
            .title("算力数据仓")
            .icon(GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER)
            .task(Task.item(GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER),
                    Task.item(GTResearchMachines.COMPUTATION_HATCH_RECEIVER))
            .scaledReward(GTBlocks.OPTICAL_PIPES[0], 8)
            .dependsOn(OPTICAL_DATA_HATCH));

    public static final Quest TECH_DATA_BASE = CHAPTER.quest("tech_data_base", q -> q
            .at(36, 18)
            .title("数据库研究")
            .task(Task.techNode(BaseNodes.DataBase))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest DATA_BANK = CHAPTER.quest("data_bank", q -> q
            .at(32, 14)
            .title("数据库")
            .task(Task.item(GTResearchMachines.DATA_BANK, 2))
            .scaledReward(GTItems.TOOL_DATA_ORB, 2)
            .dependsOn(COMPUTER_CASINGS, TECH_DATA_BASE));

    public static final Quest LUV_FIELD_GENERATOR = CHAPTER.quest("luv_field_generator", q -> q
            .at(24, 18)
            .title("LuV 力场发生器")
            .task(Task.item(GTItems.FIELD_GENERATOR_LuV, 16))
            .scaledReward(TagPrefix.wireFine, GTMaterials.IndiumTinBariumTitaniumCuprate, 32)
            .dependsOn(OPTICAL_PIPE_COPY_1));

    public static final Quest HPCA_COMPONENTS = CHAPTER.quest("hpca_components", q -> q
            .at(26, 18)
            .title("HPCA 组件")
            .icon(GTResearchMachines.HPCA_COMPUTATION_COMPONENT)
            .task(Task.item(GTResearchMachines.HPCA_COMPUTATION_COMPONENT, 4),
                    Task.item(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 4))
            .scaledReward(GTMaterials.PCBCoolant.getBucket(), 2)
            .dependsOn(LUV_FIELD_GENERATOR));

    public static final Quest TECH_COMPUTATION_ARRAY = CHAPTER.quest("tech_computation_array", q -> q
            .at(32, 20)
            .title("算力供应传输基础研究")
            .task(Task.techNode(BaseNodes.ComputationArray))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(TECH_DATA_BASE));

    public static final Quest HPCA = CHAPTER.quest("high_performance_computation_array", q -> q
            .at(28, 18)
            .title("高性能计算阵列")
            .task(Task.item(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY))
            .scaledReward(GTMaterials.PCBCoolant.getBucket(), 4)
            .dependsOn(COMPUTATIONAL_DATA_HATCH, DATA_BANK, HPCA_COMPONENTS, TECH_COMPUTATION_ARRAY));

    public static final Quest LUV_SCANNER = CHAPTER.quest("luv_scanner", q -> q
            .at(30, 18)
            .title("LuV 扫描仪")
            .task(Task.item(GTMachines.SCANNER[GTValues.LuV]))
            .scaledReward(GTItems.SENSOR_LuV, 1)
            .dependsOn(HPCA));

    public static final Quest TECH_SCAN_STATION = CHAPTER.quest("tech_scan_station", q -> q
            .at(34, 18)
            .title("扫描站研究")
            .task(Task.techNode(BaseNodes.ScanStation))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(TECH_DATA_BASE));

    public static final Quest RESEARCH_STATION = CHAPTER.quest("research_station", q -> q
            .at(32, 18)
            .title("研究站")
            .icon(GTResearchMachines.RESEARCH_STATION)
            .task(Task.item(GTResearchMachines.RESEARCH_STATION),
                    Task.item(GTResearchMachines.OBJECT_HOLDER))
            .scaledReward(GTItems.TOOL_DATA_ORB, 4)
            .dependsOn(LUV_SCANNER, TECH_SCAN_STATION));

    public static final QuestLink RESEARCH_STATION_COPY_1 = CHAPTER.copy(RESEARCH_STATION, -8, 8);

    public static final Quest ZPM_EMITTER_SENSOR = CHAPTER.quest("zpm_emitter_and_sensor", q -> q
            .at(-6, 8)
            .title("ZPM 发射器与传感器")
            .icon(GTItems.EMITTER_ZPM)
            .task(Task.item(GTItems.EMITTER_ZPM, 4),
                    Task.item(GTItems.SENSOR_ZPM, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.Duranium, 16)
            .dependsOn(RESEARCH_STATION_COPY_1));

    public static final QuestLink ZPM_EMITTER_SENSOR_COPY_1 = CHAPTER.copy(ZPM_EMITTER_SENSOR, 20, 20);

    public static final Quest ZPM_FIELD_GENERATOR = CHAPTER.quest("zpm_field_generator", q -> q
            .at(-6, 6)
            .title("ZPM 力场发生器")
            .task(Task.item(GTItems.FIELD_GENERATOR_ZPM, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.UraniumRhodiumDinaquadide, 32)
            .dependsOn(ZPM_EMITTER_SENSOR));

    public static final Quest NAQUADRIA_SULFATE = CHAPTER.quest("naquadria_sulfate", q -> q
            .at(-2, -2)
            .title("硫酸超能硅岩")
            .task(Task.item(TagPrefix.dust, GTMaterials.NaquadriaSulfate, 96))
            .scaledReward(GTMaterials.AcidicNaquadriaSolution.getBucket(), 3)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest NAQUADRIA_INGOT = CHAPTER.quest("naquadria_ingot", q -> q
            .at(-2, -4)
            .title("超能硅岩锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Naquadria, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadria, 4)
            .dependsOn(NAQUADRIA_SULFATE));

    public static final Quest BIPHENYL = CHAPTER.quest("biphenyl", q -> q
            .at(24, 10)
            .title("联苯")
            .task(Task.item(TagPrefix.dust, GTMaterials.Biphenyl, 8))
            .scaledReward(GTMaterials.Benzene.getBucket(), 4)
            .dependsOn(TETRACHLOROSILANE));

    public static final Quest PCB = CHAPTER.quest("polychlorinated_biphenyl", q -> q
            .at(26, 10)
            .title("多氯联苯")
            .icon(GTMaterials.PolychlorinatedBiphenyl.getBucket())
            .task(Task.item(GTMaterials.PolychlorinatedBiphenyl.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Biphenyl, 4)
            .dependsOn(BIPHENYL));

    public static final Quest PCB_COOLANT = CHAPTER.quest("pcb_coolant", q -> q
            .at(28, 10)
            .title("多氯联苯冷却液")
            .icon(GTMaterials.PCBCoolant.getBucket())
            .task(Task.item(GTMaterials.PCBCoolant.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.PolychlorinatedBiphenyl.getBucket(), 2)
            .dependsOn(PCB));

    public static final Quest TITANIUM_NITRIDE_CERAMIC = CHAPTER.quest("titanium_nitride_ceramic", q -> q
            .at(-2, 0)
            .title("氮化钛陶瓷")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TitaniumNitrideCeramic, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Titanium, 9)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest TITANIUM_NITRIDE_ROUGH_BLANK = CHAPTER.quest("titanium_nitride_ceramic_rough_blank", q -> q
            .at(-4, 0)
            .title("氮化钛陶瓷粗胚")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.TitaniumNitrideCeramic, 4))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 2)
            .dependsOn(TITANIUM_NITRIDE_CERAMIC));

    public static final Quest TITANIUM_NITRIDE_BLOCK = CHAPTER.quest("titanium_nitride_ceramic_block", q -> q
            .at(-6, -2)
            .title("氮化钛陶瓷块")
            .task(Task.item(TagPrefix.block, GTOMaterials.TitaniumNitrideCeramic, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitaniumNitrideCeramic, 9)
            .dependsOn(TITANIUM_NITRIDE_ROUGH_BLANK));

    public static final Quest SPECIAL_CERAMICS = CHAPTER.quest("special_ceramics", q -> q
            .at(-6, -4)
            .title("特种陶瓷")
            .task(Task.item(GTOItems.SPECIAL_CERAMICS, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.SpecialCeramics, 4)
            .dependsOn(TITANIUM_NITRIDE_BLOCK));

    public static final Quest BORON_CALCIUM_PLASMA = CHAPTER.quest("boron_and_calcium_plasma", q -> q
            .at(2, -6)
            .title("硼与钙等离子体")
            .icon(GTMaterials.Boron.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Boron.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(),
                    Task.item(GTMaterials.Calcium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 8)
            .dependsOn(NewLuvChapter.LUV_FUSION_REACTOR));

    public static final Quest NEON_PLASMA = CHAPTER.quest("neon_plasma", q -> q
            .at(2, -4)
            .title("氖等离子体")
            .icon(GTMaterials.Neon.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Neon.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 8)
            .dependsOn(BORON_CALCIUM_PLASMA));

    public static final Quest TECH_CRYSTAL_TECH_MAINFRAME = CHAPTER.quest("tech_crystal_tech_mainframe", q -> q
            .at(0, -2)
            .title("晶体技术主机研究")
            .task(Task.techNode(BaseNodes.CrystalTechMainframe))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest CRYSTAL_MAINFRAME = CHAPTER.quest("crystal_processor_mainframe", q -> q
            .at(2, -2)
            .title("晶体处理器主机")
            .task(Task.item(GTItems.CRYSTAL_MAINFRAME_UV, 20))
            .scaledReward(GTItems.CRYSTAL_COMPUTER_ZPM, 1)
            .dependsOn(NewLuvChapter.CRYSTAL_COMPUTER, NEON_PLASMA, TECH_CRYSTAL_TECH_MAINFRAME));

    public static final QuestLink CRYSTAL_MAINFRAME_COPY_1 = CHAPTER.copy(CRYSTAL_MAINFRAME, 28, -2);

    public static final QuestLink CRYSTAL_MAINFRAME_COPY_2 = CHAPTER.copy(CRYSTAL_MAINFRAME, 22, 14);

    public static final QuestLink CRYSTAL_MAINFRAME_COPY_3 = CHAPTER.copy(CRYSTAL_MAINFRAME, 10, 2);

    public static final QuestLink CRYSTAL_MAINFRAME_COPY_4 = CHAPTER.copy(CRYSTAL_MAINFRAME, 22, 18);

    public static final Quest HPCA_ADVANCED = CHAPTER.quest("hpca_advanced_computation_component", q -> q
            .at(4, -2)
            .title("HPCA 高级计算组件")
            .task(Task.item(GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, 2))
            .scaledReward(GTMaterials.PCBCoolant.getBucket(), 4)
            .dependsOn(CRYSTAL_MAINFRAME));

    public static final QuestLink HPCA_ADVANCED_COPY_1 = CHAPTER.copy(HPCA_ADVANCED, 6, 2);

    public static final QuestLink HPCA_ADVANCED_COPY_2 = CHAPTER.copy(HPCA_ADVANCED, 20, -2);

    public static final Quest TECH_ENERGY_IO_ZPM = CHAPTER.quest("tech_energy_io_zpm", q -> q
            .at(26, 14)
            .title("高压能量输入输出II研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.ZPM]))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest ZPM_ENERGY_HATCH = CHAPTER.quest("zpm_energy_input_hatch", q -> q
            .at(26, 12)
            .milestone()
            .title("ZPM 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.ZPM], 10))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 4)
            .dependsOn(RESEARCH_STATION, TECH_ENERGY_IO_ZPM));

    public static final Quest LARGE_ASSEMBLER = CHAPTER.quest("large_assembler", q -> q
            .at(4, 0)
            .milestone()
            .title("大型组装机")
            .task(Task.item(GCYMMachines.LARGE_ASSEMBLER))
            .scaledReward(GTItems.ROBOT_ARM_IV, 2)
            .dependsOn(HPCA_ADVANCED));

    public static final Quest BLAST_ALLOY_SMELTER = CHAPTER.quest("blast_alloy_smelter", q -> q
            .at(6, 0)
            .milestone()
            .title("合金冶炼炉")
            .task(Task.item(GCYMMachines.BLAST_ALLOY_SMELTER), Task.item(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING, 70))
            .scaledReward(TagPrefix.dust, GTOMaterials.HastelloyN, 25)
            .dependsOn(HPCA_ADVANCED_COPY_1));

    public static final Quest HASTELLOY_N_AND_INCONEL_792 = CHAPTER.quest("hastelloy_n_and_inconel_792", q -> q
            .at(20, 2)
            .title("哈斯特洛依合金-N 与镍铬基合金-792")
            .icon(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING)
            .task(Task.item(TagPrefix.plateDouble, GTOMaterials.HastelloyN, 8), Task.item(TagPrefix.plateDouble, GTOMaterials.Inconel792, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.HastelloyN, 25)
            .dependsOn(BLAST_ALLOY_SMELTER));

    public static final Quest MNP_MGB2 = CHAPTER.quest("manganese_phosphide_and_magnesium_diboride", q -> q
            .at(22, -2)
            .title("磷化锰与二硼化镁")
            .icon(TagPrefix.ingot, GTMaterials.MagnesiumDiboride)
            .task(Task.item(TagPrefix.ingot, GTMaterials.ManganesePhosphide, 40),
                    Task.item(TagPrefix.ingot, GTMaterials.MagnesiumDiboride, 40))
            .scaledReward(TagPrefix.dust, GTMaterials.Magnesium, 8)
            .dependsOn(HPCA_ADVANCED_COPY_2));

    public static final Quest TECH_PRECISE_MANUFACTURING_TECH = CHAPTER.quest("tech_precise_manufacturing_tech", q -> q
            .at(22, 2)
            .title("精密制造技术")
            .task(Task.techNode(MachinesNode.PreciseManufacturingTech))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest PRECISION_ASSEMBLER = CHAPTER.quest("precision_assembler", q -> q
            .at(22, 0)
            .milestone()
            .title("精密组装机")
            .task(Task.item(MultiBlockA.PRECISION_ASSEMBLER))
            .scaledReward(GTItems.ROBOT_ARM_ZPM, 1)
            .dependsOn(LARGE_ASSEMBLER, HASTELLOY_N_AND_INCONEL_792, MNP_MGB2, TECH_PRECISE_MANUFACTURING_TECH));

    public static final Quest IMPROVED_SC_COIL = CHAPTER.quest("improved_superconductor_coil", q -> q
            .at(22, 10)
            .title("改良型超导线圈方块")
            .task(Task.item(GTOBlocks.IMPROVED_SUPERCONDUCTOR_COIL, 4))
            .scaledReward(GTBlocks.SUPERCONDUCTING_COIL, 1)
            .dependsOn(PRECISION_ASSEMBLER));

    public static final Quest FUSION_COIL = CHAPTER.quest("fusion_coil", q -> q
            .at(18, 10)
            .title("聚变线圈方块")
            .task(Task.item(GTBlocks.FUSION_COIL, 52))
            .scaledReward(TagPrefix.plate, GTMaterials.Europium, 8)
            .dependsOn(NewLuvChapter.SUPERCONDUCTING_COIL, NewLuvChapter.EUROPIUM));

    public static final Quest FUSION_CASING_MK2 = CHAPTER.quest("fusion_casing_mk2", q -> q
            .at(20, 10)
            .title("聚变机械方块 MK-II")
            .task(Task.item(GTBlocks.FUSION_CASING_MK2, 48))
            .scaledReward(GTItems.VOLTAGE_COIL_ZPM, 4)
            .dependsOn(FUSION_COIL));

    public static final Quest NAQUADRIA_PLATE_AND_COOLANT_60K = CHAPTER.quest("naquadria_plate_and_coolant_cell_60k", q -> q
            .at(-2, -6)
            .title("超能硅岩双层板与 60K 冷却单元")
            .icon(TagPrefix.plateDouble, GTMaterials.Naquadria)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Naquadria, 8),
                    Task.item(GTOItems.COOLANT_CELL_60K, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Naquadria, 4)
            .dependsOn(NAQUADRIA_INGOT));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR2 = CHAPTER.quest("tech_tokamak_fusion_reactor2", q -> q
            .at(20, 14)
            .title("托卡马克聚变反应堆II研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor2))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL, NewLuvChapter.TECH_TOKAMAK_FUSION_REACTOR));

    public static final Quest ZPM_FUSION_REACTOR = CHAPTER.quest("zpm_fusion_reactor", q -> q
            .at(22, 12)
            .milestone()
            .title("ZPM 聚变反应堆")
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.ZPM]))
            .scaledReward(TagPrefix.dust, GTMaterials.Arsenic, 16)
            .dependsOn(IMPROVED_SC_COIL, ZPM_ENERGY_HATCH, CRYSTAL_MAINFRAME_COPY_2, FUSION_CASING_MK2, TECH_TOKAMAK_FUSION_REACTOR2));

    public static final Quest DARMSTADTIUM = CHAPTER.quest("darmstadtium_ingot", q -> q
            .at(18, 12)
            .milestone()
            .title("𫟼")
            .icon(TagPrefix.ingot, GTMaterials.Darmstadtium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Darmstadtium, 8),
                    Task.item(TagPrefix.plate, GTMaterials.Darmstadtium, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Ruthenium, 4)
            .dependsOn(ZPM_FUSION_REACTOR));

    public static final Quest UV_CASING = CHAPTER.quest("uv_casing", q -> q
            .at(16, 14)
            .title("UV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_UV))
            .scaledReward(TagPrefix.plate, GTMaterials.Darmstadtium, 2)
            .dependsOn(DARMSTADTIUM));

    public static final Quest YBCO_CABLE = CHAPTER.quest("yttrium_barium_cuprate_cable", q -> q
            .at(16, 10)
            .title("钇钡铜氧线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.YttriumBariumCuprate, 2)
            .dependsOn(DARMSTADTIUM));

    public static final Quest UV_HULL = CHAPTER.quest("welcome_to_the_extreme_pressure_stage", q -> q
            .at(14, 12)
            .milestone()
            .title("欢迎来到{极限压}阶段", DARK_AQUA)
            .task(Task.item(GTMachines.HULL[GTValues.UV]))
            .reward(
                    Reward.item(GTMachines.HULL[GTValues.UV], 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(UV_CASING, YBCO_CABLE));

    public static final Quest PETRI_DISH_AND_ETHANOL = CHAPTER.quest("petri_dish_and_absolute_ethanol", q -> q
            .at(-2, 6)
            .title("培养皿与无水乙醇")
            .icon(GTItems.PETRI_DISH)
            .task(Task.item(GTItems.PETRI_DISH, 16),
                    Task.item(GTOMaterials.AbsoluteEthanol.getBucket()))
            .scaledReward(GTItems.PETRI_DISH, 4)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest TRITANIUM_INGOT = CHAPTER.quest("tritanium_ingot", q -> q
            .at(-4, 6)
            .title("三钛锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Tritanium, 32))
            .scaledReward(TagPrefix.ingot, GTMaterials.Tritanium, 4)
            .dependsOn(ZPM_FUSION_REACTOR));

    public static final Quest STERILIZING_FILTER_CASING = CHAPTER.quest("sterilizing_filter_casing", q -> q
            .at(-4, 8)
            .title("无菌过滤外壳")
            .task(Task.item(GTBlocks.FILTER_CASING_STERILE, 16))
            .scaledReward(TagPrefix.frameGt, GTMaterials.Tritanium, 4)
            .dependsOn(TRITANIUM_INGOT, ZPM_EMITTER_SENSOR));

    public static final Quest STERILIZED_PETRI_DISH = CHAPTER.quest("sterilized_petri_dish", q -> q
            .at(-2, 8)
            .title("灭菌培养皿")
            .task(Task.item(GTOItems.STERILIZED_PETRI_DISH, 16))
            .scaledReward(GTItems.PETRI_DISH, 8)
            .dependsOn(STERILIZING_FILTER_CASING, PETRI_DISH_AND_ETHANOL));

    public static final Quest INDALLOY_140 = CHAPTER.quest("indalloy_140", q -> q
            .at(0, 6)
            .title("铋铅合金-140")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Indalloy140, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Bismuth, 8)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest WETWARE_BOARD = CHAPTER.quest("wetware_board", q -> q
            .at(0, 8)
            .title("湿件电路基板")
            .task(Task.item(GTItems.WETWARE_BOARD, 16))
            .scaledReward(GTItems.MULTILAYER_FIBER_BOARD, 4)
            .dependsOn(STERILIZED_PETRI_DISH, INDALLOY_140));

    public static final Quest HIGH_PURITY_CALCIUM_CARBONATE = CHAPTER.quest("high_purity_calcium_carbonate", q -> q
            .at(2, 8)
            .title("高纯度碳酸钙")
            .task(Task.item(TagPrefix.dust, GTOMaterials.HighPurityCalciumCarbonate, 5))
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 16)
            .dependsOn(WETWARE_BOARD));

    public static final Quest HYDROXYAPATITE_DUST = CHAPTER.quest("hydroxyapatite_ceramic_dust", q -> q
            .at(2, 10)
            .title("羟基磷灰石陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.HydroxyapatiteCeramic, 9))
            .scaledReward(TagPrefix.dust, GTOMaterials.HighPurityCalciumCarbonate, 2)
            .dependsOn(HIGH_PURITY_CALCIUM_CARBONATE));

    public static final Quest HYDROXYAPATITE_CERAMIC_FLAKE = CHAPTER.quest("hydroxyapatite_ceramic_flake", q -> q
            .at(4, 10)
            .title("羟基磷灰石陶瓷片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.HydroxyapatiteCeramic, 32))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.HydroxyapatiteCeramic, 8)
            .dependsOn(HYDROXYAPATITE_DUST));

    public static final Quest WETWARE_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("wetware_printed_circuit_board", q -> q
            .at(4, 12)
            .milestone()
            .title("湿件印刷电路基板")
            .task(Task.item(GTItems.WETWARE_CIRCUIT_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.NiobiumTitanium, 32)
            .dependsOn(HYDROXYAPATITE_CERAMIC_FLAKE));

    public static final Quest ZPM_CIRCUIT_ASSEMBLER = CHAPTER.quest("zpm_circuit_assembler", q -> q
            .at(22, 20)
            .title("ZPM 电路组装机")
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.ZPM]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 2)
            .dependsOn(CRYSTAL_MAINFRAME_COPY_4, ZPM_EMITTER_SENSOR_COPY_1));

    public static final Quest BIO_CHAFF = CHAPTER.quest("bio_chaff", q -> q
            .at(12, -6)
            .title("生物碎屑")
            .task(Task.item(GTItems.BIO_CHAFF, 16))
            .scaledReward(GTItems.PLANT_BALL, 8)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest BLOOD = CHAPTER.quest("blood_cells_and_plasma", q -> q
            .at(8, -6)
            .title("血细胞与血浆")
            .icon(GTOMaterials.BloodCells.getBucket())
            .task(
                    Task.item(GTOMaterials.BloodCells.getBucket()).optional(),
                    Task.item(GTOMaterials.BloodPlasma.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Meat, 16)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest BACTERIAL_GROWTH_MEDIUM_BUCKET = CHAPTER.quest("bacterial_growth_medium_bucket", q -> q
            .at(8, -4)
            .title("细菌培养基")
            .icon(GTOMaterials.BacterialGrowthMedium.getBucket())
            .task(Task.item(GTOMaterials.BacterialGrowthMedium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.BacterialGrowthMedium.getBucket(), 2)
            .dependsOn(BLOOD));

    public static final Quest BACTERIA = CHAPTER.quest("bacteria", q -> q
            .at(12, -4)
            .title("细菌")
            .icon(GTMaterials.Bacteria.getBucket())
            .task(Task.item(GTMaterials.Bacteria.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.BIO_CHAFF, 8)
            .dependsOn(BIO_CHAFF));

    public static final Quest TCP_CERAMIC_DUST = CHAPTER.quest("tricalcium_phosphate_ceramic_dust", q -> q
            .at(10, -6)
            .title("磷酸三钙陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TricalciumPhosphateCeramic, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.TricalciumPhosphate, 16)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest TCP_CERAMIC_FLAKE = CHAPTER.quest("tricalcium_phosphate_ceramic_flake", q -> q
            .at(10, -4)
            .title("磷酸三钙陶瓷片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TricalciumPhosphateCeramic, 64))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.TricalciumPhosphateCeramic, 16)
            .dependsOn(TCP_CERAMIC_DUST));

    public static final Quest BIOLOGICAL_MECHANICAL_CASING = CHAPTER.quest("biological_mechanical_casing", q -> q
            .at(10, -2)
            .title("生物机械方块")
            .task(Task.item(GTOBlocks.BIOLOGICAL_MECHANICAL_CASING, 64))
            .scaledReward(GTOBlocks.FLOCCULATION_CASING, 8)
            .dependsOn(BACTERIA, BACTERIAL_GROWTH_MEDIUM_BUCKET, TCP_CERAMIC_FLAKE));

    public static final Quest TECH_IRIDIUM_CASING_PRODUCTION = CHAPTER.quest("tech_iridium_casing_production", q -> q
            .at(6, -2)
            .title("高性能机器外壳研究")
            .task(Task.techNode(MachinesNode.IridiumCasingProduction))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest IRIDIUM_CASING = CHAPTER.quest("iridium_casing", q -> q
            .at(8, -2)
            .title("铱强化机械方块")
            .task(Task.item(GTOBlocks.IRIDIUM_CASING, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Iridium, 8)
            .dependsOn(BIOLOGICAL_MECHANICAL_CASING, TECH_IRIDIUM_CASING_PRODUCTION));

    public static final Quest ZPM_CHEMICAL_REACTOR_AND_EXTRACTOR = CHAPTER.quest("zpm_chemical_reactor_and_extractor", q -> q
            .at(12, -2)
            .title("ZPM 化学反应釜与提取机")
            .icon(GTMachines.CHEMICAL_REACTOR[GTValues.ZPM])
            .task(Task.item(GTMachines.CHEMICAL_REACTOR[GTValues.ZPM]), Task.item(GTMachines.EXTRACTOR[GTValues.ZPM]))
            .scaledReward(GTItems.ELECTRIC_PUMP_ZPM, 2)
            .dependsOn(BIOLOGICAL_MECHANICAL_CASING));

    public static final Quest BIOCHEMICAL_REACTION = CHAPTER.quest("biochemical_reaction", q -> q
            .at(10, 0)
            .milestone()
            .title("生化反应室")
            .task(Task.item(MultiBlockC.BIOCHEMICAL_REACTION))
            .scaledReward(TagPrefix.plate, GTMaterials.UraniumRhodiumDinaquadide, 4)
            .dependsOn(BIOLOGICAL_MECHANICAL_CASING, ZPM_CHEMICAL_REACTOR_AND_EXTRACTOR, IRIDIUM_CASING));

    public static final Quest GELATIN = CHAPTER.quest("gelatin", q -> q
            .at(22, -6)
            .title("明胶")
            .task(Task.item(TagPrefix.dust, GTMaterials.Gelatin, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Collagen, 4)
            .dependsOn(BIOCHEMICAL_REACTION));

    public static final Quest AGAR = CHAPTER.quest("agar", q -> q
            .at(24, -6)
            .title("琼脂")
            .task(Task.item(TagPrefix.dust, GTMaterials.Agar, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Gelatin, 4)
            .dependsOn(GELATIN));

    public static final Quest PREPARATION_PETRI_DISH = CHAPTER.quest("preparation_petri_dish", q -> q
            .at(26, -6)
            .title("准备培养皿")
            .task(Task.item(GTOItems.PREPARATION_PETRI_DISH, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Agar, 8)
            .dependsOn(AGAR));

    public static final Quest BIOCHEMICAL_EXTRACTION = CHAPTER.quest("biochemical_extraction", q -> q
            .at(26, -4)
            .title("生物提取机")
            .task(Task.item(MultiBlockC.BIOCHEMICAL_EXTRACTION))
            .scaledReward(TagPrefix.rod, GTMaterials.NaquadahEnriched, 8)
            .dependsOn(PREPARATION_PETRI_DISH));

    public static final Quest BLOCK_CONVERSION_ROOM = CHAPTER.quest("block_conversion_room", q -> q
            .at(28, -4)
            .title("方块转换室")
            .task(Task.item(MultiBlockD.BLOCK_CONVERSION_ROOM))
            .scaledReward(Items.BONE_BLOCK, 16)
            .dependsOn(BIOCHEMICAL_EXTRACTION, CRYSTAL_MAINFRAME_COPY_1));

    public static final Quest ESSENCE = CHAPTER.quest("essence", q -> q
            .at(30, -4)
            .title("精华")
            .icon(GTOItems.ESSENCE)
            .task(Task.item(GTOItems.ESSENCE, 4))
            .scaledReward(Items.BONE_BLOCK, 16)
            .dependsOn(BLOCK_CONVERSION_ROOM));

    public static final Quest ALGAE_FARM = CHAPTER.quest("algae_farm", q -> q
            .at(28, -6)
            .title("藻类农场")
            .task(Task.item(MultiBlockG.ALGAE_FARM))
            .scaledReward(GTOItems.GREEN_ALGAE, 16)
            .dependsOn(PREPARATION_PETRI_DISH));

    public static final Quest ALGAE = CHAPTER.quest("algae", q -> q
            .at(30, -6)
            .title("藻类")
            .icon(GTOItems.GREEN_ALGAE)
            .task(
                    Task.item(GTOItems.RED_ALGAE, 8),
                    Task.item(GTOItems.GOLD_ALGAE, 8),
                    Task.item(GTOItems.BLUE_ALGAE, 8),
                    Task.item(GTOItems.GREEN_ALGAE, 8),
                    Task.item(GTOItems.BROWN_ALGAE, 8))
            .scaledReward(GTOItems.RED_ALGAE, 8)
            .dependsOn(ALGAE_FARM));

    public static final Quest BIO_NUTRIENT = CHAPTER.quest("bio_nutrient", q -> q
            .at(32, -4)
            .title("生物养料")
            .icon(GTOFluids.NUTRIENT_DISTILLATION.get().getBucket())
            .task(
                    Task.item(GTOFluids.NUTRIENT_DISTILLATION.get().getBucket()).optional(),
                    Task.item(GTOFluids.CLOUD_SEED_CONCENTRATED.get().getBucket()).optional(),
                    Task.item(GTOFluids.FIRE_WATER.get().getBucket()).optional(),
                    Task.item(GTOFluids.VAPOR_OF_LEVITY.get().getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOItems.GOLD_ALGAE, 8)
            .dependsOn(ALGAE, ESSENCE));

    public static final Quest BACTERIAL_SLUDGE = CHAPTER.quest("bacterial_sludge", q -> q
            .at(34, -4)
            .title("细菌污泥")
            .icon(GTMaterials.BacterialSludge.getBucket())
            .task(Task.item(GTMaterials.BacterialSludge.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Biomass.getBucket(), 4)
            .dependsOn(BIO_NUTRIENT));

    public static final Quest ENRICHED_BACTERIAL_SLUDGE = CHAPTER.quest("enriched_bacterial_sludge", q -> q
            .at(36, -4)
            .title("富集细菌污泥")
            .icon(GTMaterials.EnrichedBacterialSludge.getBucket())
            .task(Task.item(GTMaterials.EnrichedBacterialSludge.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dustTiny, GTMaterials.Naquadria, 4)
            .dependsOn(BACTERIAL_SLUDGE));

    public static final Quest MUTAGEN_BUCKET = CHAPTER.quest("mutagen_bucket", q -> q
            .at(34, -2)
            .title("诱变剂")
            .icon(GTMaterials.Mutagen.getBucket())
            .task(Task.item(GTMaterials.Mutagen.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.EnrichedBacterialSludge.getBucket(), 2)
            .dependsOn(ENRICHED_BACTERIAL_SLUDGE));

    public static final Quest I_NEED_DNA_2 = CHAPTER.quest("i_need_dna_2", q -> q
            .at(26, -8)
            .title("我需要DNA#2")
            .icon(TagPrefix.dust, GTOMaterials.EschericiaColi)
            .task(
                    Task.item(TagPrefix.dust, GTOMaterials.EschericiaColi, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.BifidobacteriumBreve, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.BrevibacteriumFlavium, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.StreptococcusPyogenes, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.CupriavidusNecator, 16))
            .scaledReward(GTOMaterials.BacterialGrowthMedium.getBucket(), 2)
            .dependsOn(PREPARATION_PETRI_DISH));

    public static final Quest UNLOCK_MERCURY = CHAPTER.quest("unlock_mercury", q -> q
            .at(32, 0)
            .title("解锁水星")
            .icon(ModItems.MERCURY_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelCn3h7o3.getBucket(), 4)
            .dependsOn(MUTAGEN_BUCKET));

    public static final Quest MERCURY = CHAPTER.quest("mercury", q -> q
            .at(30, 0)
            .milestone()
            .title("登陆水星")
            .icon(ModItems.MERCURY_GLOBE.get())
            .task(
                    Task.dimension(Dimension.MERCURY).title("造访水星").icon(ModItems.MERCURY_GLOBE.get()))
            .scaledReward(ModItems.RAW_CALORITE.get(), 16)
            .dependsOn(UNLOCK_MERCURY));

    public static final Quest CALORITE = CHAPTER.quest("calorite_ingot", q -> q
            .at(10, 10)
            .title("耐热金属锭")
            .icon(ModItems.CALORITE_INGOT.get())
            .task(Task.item(ModItems.CALORITE_INGOT.get(), 64), Task.item(TagPrefix.plateDense, GTOMaterials.Calorite, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.Calorite, 16)
            .dependsOn(MERCURY));

    public static final Quest CALORITE_ENGINE = CHAPTER.quest("calorite_engine_and_tank", q -> q
            .at(10, 8)
            .title("耐热金属引擎与储罐")
            .icon(ModItems.CALORITE_ENGINE.get())
            .task(Task.item(ModItems.CALORITE_ENGINE.get(), 8), Task.item(ModItems.CALORITE_TANK.get(), 8))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(CALORITE));

    public static final Quest TIER_6_ROCKET = CHAPTER.quest("tier_6_rocket", q -> q
            .at(10, 6)
            .title("T6 火箭")
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_6_ROCKET.get()))
            .scaledReward(GTItems.FIELD_GENERATOR_LuV, 1)
            .dependsOn(CALORITE_ENGINE));

    public static final Quest TIER_7_ROCKET = CHAPTER.quest("tier_7_rocket", q -> q
            .at(10, 4)
            .title("T7 火箭")
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_7_ROCKET.get()))
            .scaledReward(GTItems.FIELD_GENERATOR_ZPM, 1)
            .dependsOn(TIER_6_ROCKET, CRYSTAL_MAINFRAME_COPY_3));

    public static final Quest HEXAMINE = CHAPTER.quest("hexamethylenetetramine", q -> q
            .at(8, 4)
            .title("环六亚甲基四胺")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Hexamethylenetetramine, 44))
            .scaledReward(GTMaterials.Formaldehyde.getBucket(), 4)
            .dependsOn(TIER_7_ROCKET));

    public static final Quest FUMING_NITRIC_ACID_AND_ACETIC_ANHYDRIDE = CHAPTER.quest("fuming_nitric_acid_and_acetic_anhydride", q -> q
            .at(8, 6)
            .title("发烟硝酸与乙酸酐")
            .icon(GTOMaterials.FumingNitricAcid.getBucket())
            .task(
                    Task.item(GTOMaterials.FumingNitricAcid.getBucket()).optional(),
                    Task.item(GTMaterials.AceticAnhydride.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(HEXAMINE));

    public static final Quest HMX = CHAPTER.quest("hmx_explosive", q -> q
            .at(6, 4)
            .title("HMX 炸药")
            .task(Task.item(TagPrefix.dust, GTOMaterials.HmxExplosive, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.Hexamethylenetetramine, 22)
            .dependsOn(HEXAMINE, FUMING_NITRIC_ACID_AND_ACETIC_ANHYDRIDE));

    public static final Quest GELLED_TOLUENE = CHAPTER.quest("gelled_toluene", q -> q
            .at(6, 6)
            .title("胶凝甲苯")
            .task(Task.item(GTItems.GELLED_TOLUENE, 16))
            .scaledReward(GTMaterials.Toluene.getBucket(), 2)
            .dependsOn(HMX));

    public static final Quest GLYCERYL_TRINITRATE = CHAPTER.quest("glyceryl_trinitrate", q -> q
            .at(4, 4)
            .title("硝酸甘油")
            .icon(GTMaterials.GlycerylTrinitrate.getBucket())
            .task(Task.item(GTMaterials.GlycerylTrinitrate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Glycerol.getBucket(), 1)
            .dependsOn(HMX));

    public static final Quest EXPLOSIVE_HYDRAZINE = CHAPTER.quest("explosive_hydrazine", q -> q
            .at(4, 6)
            .title("爆炸肼")
            .icon(GTOMaterials.ExplosiveHydrazine.getBucket())
            .task(Task.item(GTOMaterials.ExplosiveHydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.DenseHydrazineFuelMixture.getBucket(), 3)
            .dependsOn(GELLED_TOLUENE, GLYCERYL_TRINITRATE));

    public static final Quest NITROBENZENE = CHAPTER.quest("nitrobenzene", q -> q
            .at(4, 8)
            .title("硝基苯")
            .icon(GTMaterials.Nitrobenzene.getBucket())
            .task(Task.item(GTMaterials.Nitrobenzene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Benzene.getBucket(), 4)
            .dependsOn(EXPLOSIVE_HYDRAZINE));

    public static final Quest STELLAR_FUEL = CHAPTER.quest("stellar_energy_rocket_fuel", q -> q
            .at(6, 8)
            .title("星能火箭燃料")
            .icon(GTOMaterials.StellarEnergyRocketFuel.getBucket())
            .task(Task.item(GTOMaterials.StellarEnergyRocketFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Nitrobenzene.getBucket(), 4)
            .dependsOn(EXPLOSIVE_HYDRAZINE, NITROBENZENE));

    public static final Quest UNLOCK_GLACIO = CHAPTER.quest("unlock_glacio", q -> q
            .at(8, 10)
            .title("解锁霜原星")
            .icon(ModItems.GLACIO_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.StellarEnergyRocketFuel.getBucket(), 4)
            .dependsOn(STELLAR_FUEL));

    public static final Quest VISIT_GLACIO = CHAPTER.quest("visit_glacio", q -> q
            .at(8, 12)
            .milestone()
            .title("登陆霜原星")
            .icon(ModItems.GLACIO_GLOBE.get())
            .task(
                    Task.dimension(Dimension.GLACIO).title("造访霜原星").icon(ModItems.GLACIO_GLOBE.get()))
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 4)
            .dependsOn(UNLOCK_GLACIO));

    public static final Quest GLACIO_SPIRIT = CHAPTER.quest("glacio_spirit", q -> q
            .at(-8, 10)
            .title("霜原碎片")
            .task(Task.item(GTOItems.GLACIO_SPIRIT, 8))
            .scaledReward(GTOItems.GLACIO_SPIRIT, 1)
            .dependsOn(VISIT_GLACIO));

    public static final Quest VANILLA_SPAWN_EGGS = CHAPTER.quest("vanilla_spawn_eggs", q -> q
            .at(-8, 14)
            .title("刷怪蛋")
            .icon(Items.PIGLIN_SPAWN_EGG)
            .task(
                    Task.item(Items.PIGLIN_SPAWN_EGG),
                    Task.item(Items.PIGLIN_BRUTE_SPAWN_EGG),
                    Task.item(Items.HOGLIN_SPAWN_EGG),
                    Task.item(Items.CREEPER_SPAWN_EGG))
            .scaledReward(GTMaterials.Mutagen.getBucket(), 1)
            .dependsOn(VISIT_GLACIO));

    public static final Quest I_NEED_DNA_3 = CHAPTER.quest("i_need_dna_3", q -> q
            .at(-8, 12)
            .title("我需要DNA#3")
            .icon(ModItems.PYGRO_SPAWN_EGG.get())
            .task(
                    Task.item(ModItems.PYGRO_SPAWN_EGG.get()),
                    Task.item(ModItems.PYGRO_BRUTE_SPAWN_EGG.get()),
                    Task.item(ModItems.MOGLER_SPAWN_EGG.get()),
                    Task.item(ModItems.SULFUR_CREEPER_SPAWN_EGG.get()))
            .scaledReward(GTMaterials.Mutagen.getBucket(), 1)
            .dependsOn(GLACIO_SPIRIT, VANILLA_SPAWN_EGGS));

    public static final Quest ANIMAL_CELLS = CHAPTER.quest("animal_cells", q -> q
            .at(-6, 12)
            .title("动物细胞")
            .icon(GTOMaterials.AnimalCells.getBucket())
            .task(Task.item(GTOMaterials.AnimalCells.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Meat, 16)
            .dependsOn(I_NEED_DNA_3));

    public static final Quest RAPID_ANIMAL_CELLS = CHAPTER.quest("rapidly_replicating_animal_cells", q -> q
            .at(-6, 10)
            .title("快速增殖动物细胞")
            .icon(GTOMaterials.RapidlyReplicatingAnimalCells.getBucket())
            .task(Task.item(GTOMaterials.RapidlyReplicatingAnimalCells.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.AnimalCells.getBucket(), 2)
            .dependsOn(ANIMAL_CELLS));

    public static final Quest ETHANOLAMINE = CHAPTER.quest("ethanolamine", q -> q
            .at(-4, 10)
            .title("乙醇胺")
            .icon(GTOMaterials.Ethanolamine.getBucket())
            .task(Task.item(GTOMaterials.Ethanolamine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 4)
            .dependsOn(RAPID_ANIMAL_CELLS));

    public static final QuestLink ETHANOLAMINE_COPY_1 = CHAPTER.copy(ETHANOLAMINE, 4, 24);

    public static final QuestLink ETHANOLAMINE_COPY_2 = CHAPTER.copy(ETHANOLAMINE, 2, 26);

    public static final Quest HEAVY_FUEL = CHAPTER.quest("heavy_fuel", q -> q
            .at(-2, 10)
            .title("重燃油")
            .icon(GTMaterials.HeavyFuel.getBucket())
            .task(Task.item(GTMaterials.HeavyFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricHeavyFuel.getBucket(), 4)
            .dependsOn(ETHANOLAMINE));

    public static final Quest HYDRO_CRACKED_HEAVY_FUEL = CHAPTER.quest("lightly_hydro_cracked_heavy_fuel", q -> q
            .at(0, 10)
            .title("轻度加氢裂化重燃油")
            .icon(GTMaterials.LightlyHydroCrackedHeavyFuel.getBucket())
            .task(Task.item(GTMaterials.LightlyHydroCrackedHeavyFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HeavyFuel.getBucket(), 4)
            .dependsOn(HEAVY_FUEL));

    public static final Quest CETANE = CHAPTER.quest("cetane", q -> q
            .at(0, 12)
            .milestone()
            .title("十六烷")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Cetane, 4))
            .scaledReward(GTMaterials.LightlyHydroCrackedHeavyFuel.getBucket(), 4)
            .dependsOn(HYDRO_CRACKED_HEAVY_FUEL));

    public static final Quest HEXADECYL_BROMIDE_AND_TRIMETHYLAMINE = CHAPTER.quest("hexadecyl_bromide_and_trimethylamine", q -> q
            .at(-2, 14)
            .title("1-溴代十六烷与三甲胺")
            .icon(TagPrefix.dust, GTOMaterials.HexadecylBromide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.HexadecylBromide, 4),
                    Task.item(GTOMaterials.Trimethylamine.getBucket()))
            .scaledReward(GTMaterials.Bromine.getBucket(), 4)
            .dependsOn(CETANE));

    public static final Quest CTAB = CHAPTER.quest("ctab", q -> q
            .at(-4, 16)
            .title("十六烷基三甲基溴化铵")
            .task(Task.item(TagPrefix.dust, GTOMaterials.CTAB, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.HexadecylBromide, 1)
            .dependsOn(HEXADECYL_BROMIDE_AND_TRIMETHYLAMINE));

    public static final Quest CATALYST_BASE = CHAPTER.quest("catalyst_base", q -> q
            .at(2, 24)
            .title("催化剂基底")
            .task(Task.item(GTOItems.CATALYST_BASE, 4))
            .scaledReward(TagPrefix.bolt, GTMaterials.Steel, 16)
            .dependsOn(ETHANOLAMINE_COPY_1));

    public static final Quest ETHYLENEDIAMINE_AND_CHLOROACETIC_ACID = CHAPTER.quest("ethylenediamine_and_chloroacetic_acid", q -> q
            .at(0, 26)
            .title("乙二胺与氯乙酸")
            .icon(GTOMaterials.Ethylenediamine.getBucket())
            .task(Task.item(GTOMaterials.Ethylenediamine.getBucket()),
                    Task.item(GTOTagPrefix.CATALYST, GTMaterials.Palladium, 1),
                    Task.item(GTOMaterials.ChloroaceticAcid.getBucket()))
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 4)
            .dependsOn(ETHANOLAMINE_COPY_2, CATALYST_BASE));

    public static final Quest EDTA = CHAPTER.quest("edta", q -> q
            .at(-2, 24)
            .title("乙二胺四乙酸")
            .task(Task.item(TagPrefix.dust, GTOMaterials.EDTA, 5))
            .scaledReward(GTOMaterials.ChloroaceticAcid.getBucket(), 2)
            .dependsOn(ETHYLENEDIAMINE_AND_CHLOROACETIC_ACID));

    public static final Quest TRIS = CHAPTER.quest("tris", q -> q
            .at(-2, 26)
            .title("三羟甲基氨基甲烷")
            .icon(GTOMaterials.Tris.getBucket())
            .task(Task.item(GTOMaterials.Tris.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.EthyleneOxide.getBucket(), 3)
            .dependsOn(ETHYLENEDIAMINE_AND_CHLOROACETIC_ACID));

    public static final Quest TRIS_HYDROCHLORIDE = CHAPTER.quest("tris_hydrochloride_solution", q -> q
            .at(-4, 24)
            .title("Tris-盐酸")
            .icon(GTOMaterials.TrisHydrochlorideSolution.getBucket())
            .task(Task.item(GTOMaterials.TrisHydrochlorideSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Tris.getBucket(), 1)
            .dependsOn(TRIS));

    public static final Quest DNA_EXTRACTION_BUFFER = CHAPTER.quest("dna_extraction_buffer", q -> q
            .at(-4, 22)
            .title("DNA提取缓冲液")
            .icon(GTOMaterials.DNAExtractionBuffer.getBucket())
            .task(Task.item(GTOMaterials.DNAExtractionBuffer.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.CTAB, 1)
            .dependsOn(EDTA, CTAB, TRIS_HYDROCHLORIDE));

    public static final QuestLink DNA_EXTRACTION_BUFFER_COPY_1 = CHAPTER.copy(DNA_EXTRACTION_BUFFER, 4, 16);

    public static final Quest CAS9_PROTEIN = CHAPTER.quest("cas9_protein", q -> q
            .at(4, 18)
            .title("CAS-9-蛋白")
            .icon(GTOMaterials.Cas9Protein.getBucket())
            .task(Task.item(GTOMaterials.Cas9Protein.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.StreptococcusPyogenes, 12)
            .dependsOn(DNA_EXTRACTION_BUFFER_COPY_1));

    public static final Quest PLURIPOTENCY_GENES = CHAPTER.quest("pluripotency_genes", q -> q
            .at(-2, 22)
            .title("四种多能性基因")
            .icon(GTOMaterials.MycGene.getBucket())
            .task(
                    Task.item(GTOMaterials.MycGene.getBucket()).optional(),
                    Task.item(GTOMaterials.Oct4Gene.getBucket()).optional(),
                    Task.item(GTOMaterials.Sox2Gene.getBucket()).optional(),
                    Task.item(GTOMaterials.Kfl4Gene.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.RapidlyReplicatingAnimalCells.getBucket(), 4)
            .dependsOn(DNA_EXTRACTION_BUFFER));

    public static final QuestLink PLURIPOTENCY_GENES_COPY_1 = CHAPTER.copy(PLURIPOTENCY_GENES, 2, 16);

    public static final Quest CHITIN = CHAPTER.quest("chitin", q -> q
            .at(0, 24)
            .title("甲壳素")
            .icon(GTOMaterials.Chitin.getBucket())
            .task(Task.item(GTOMaterials.Chitin.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.BROWN_MUSHROOM, 16)
            .dependsOn(PLURIPOTENCY_GENES));

    public static final Quest CHITOSAN = CHAPTER.quest("chitosan", q -> q
            .at(0, 22)
            .milestone()
            .title("壳聚糖")
            .icon(GTOMaterials.Chitosan.getBucket())
            .task(Task.item(GTOMaterials.Chitosan.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Chitin.getBucket(), 4)
            .dependsOn(CHITIN));

    public static final Quest GENE_PLASMIDS = CHAPTER.quest("pluripotency_induction_gene_plasmids", q -> q
            .at(2, 18)
            .title("多能性诱导基因质粒")
            .icon(GTOMaterials.PluripotencyInductionGenePlasmids.getBucket())
            .task(Task.item(GTOMaterials.PluripotencyInductionGenePlasmids.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Cas9Protein.getBucket(), 1)
            .dependsOn(PLURIPOTENCY_GENES_COPY_1, CAS9_PROTEIN));

    public static final Quest PLURIPOTENCY_INDUCTION_GENE_THERAPY_FLUID_BUCKET = CHAPTER.quest("pluripotency_induction_gene_therapy_fluid_bucket", q -> q
            .at(2, 20)
            .title("多能性诱导基因治疗液")
            .icon(GTOMaterials.PluripotencyInductionGeneTherapyFluid.getBucket())
            .task(Task.item(GTOMaterials.PluripotencyInductionGeneTherapyFluid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Chitosan.getBucket(), 1)
            .dependsOn(GENE_PLASMIDS, CHITOSAN));

    public static final Quest CLEAR_AMMONIA = CHAPTER.quest("clear_ammonia_solution", q -> q
            .at(-2, 20)
            .title("澄清氨溶液")
            .icon(GTOMaterials.ClearAmmoniaSolution.getBucket())
            .task(Task.item(GTOMaterials.ClearAmmoniaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 4)
            .dependsOn(PLURIPOTENCY_INDUCTION_GENE_THERAPY_FLUID_BUCKET));

    public static final Quest GLUTAMINE = CHAPTER.quest("glutamine", q -> q
            .at(-6, 20)
            .title("谷氨酰胺")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Glutamine, 40))
            .scaledReward(TagPrefix.dust, GTMaterials.Sugar, 16)
            .dependsOn(CLEAR_AMMONIA));

    public static final Quest LEACHED_TURPENTINE = CHAPTER.quest("leached_turpentine", q -> q
            .at(-6, 24)
            .title("松油浸出物")
            .icon(GTOMaterials.LeachedTurpentine.getBucket())
            .task(Task.item(GTOMaterials.LeachedTurpentine.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.DARK_OAK_LOG, 32)
            .dependsOn(GLUTAMINE));

    public static final QuestLink LEACHED_TURPENTINE_COPY_1 = CHAPTER.copy(LEACHED_TURPENTINE, 26, 30);

    public static final Quest STEAM_CRACKED_TURPENTINE = CHAPTER.quest("steam_cracked_turpentine", q -> q
            .at(-2, 28)
            .title("蒸汽裂化的松油浸出物")
            .icon(GTOMaterials.SteamCrackedTurpentine.getBucket())
            .task(Task.item(GTOMaterials.SteamCrackedTurpentine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.LeachedTurpentine.getBucket(), 2)
            .dependsOn(LEACHED_TURPENTINE));

    public static final Quest TURPENTINE = CHAPTER.quest("turpentine", q -> q
            .at(4, 26)
            .title("松节油")
            .icon(GTOMaterials.Turpentine.getBucket())
            .task(Task.item(GTOMaterials.Turpentine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.SteamCrackedTurpentine.getBucket(), 2)
            .dependsOn(STEAM_CRACKED_TURPENTINE));

    public static final Quest BETA_PINENE = CHAPTER.quest("beta_pinene", q -> q
            .at(6, 26)
            .title("β-蒎烯")
            .task(Task.item(TagPrefix.dust, GTOMaterials.BetaPinene, 26))
            .scaledReward(GTOMaterials.Turpentine.getBucket(), 2)
            .dependsOn(TURPENTINE));

    public static final Quest ISOPRENE = CHAPTER.quest("isoprene", q -> q
            .at(6, 24)
            .title("异戊二烯")
            .icon(GTMaterials.Isoprene.getBucket())
            .task(Task.item(GTMaterials.Isoprene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Propene.getBucket(), 4)
            .dependsOn(TURPENTINE));

    public static final Quest CITRAL = CHAPTER.quest("citral", q -> q
            .at(8, 22)
            .milestone()
            .title("柠檬醛")
            .icon(GTOMaterials.Citral.getBucket())
            .task(Task.item(GTOMaterials.Citral.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.BetaPinene, 13)
            .dependsOn(BETA_PINENE, ISOPRENE));

    public static final Quest YEAST = CHAPTER.quest("yeast", q -> q
            .at(12, 24)
            .title("酵母")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Yeast, 12))
            .scaledReward(Items.SWEET_BERRIES, 32)
            .dependsOn(CITRAL));

    public static final Quest LINOLEIC_ACID_AND_BIOTIN = CHAPTER.quest("linoleic_acid_and_biotin", q -> q
            .at(14, 24)
            .title("亚油酸与生物素")
            .icon(GTOMaterials.LinoleicAcid.getBucket())
            .task(
                    Task.item(GTOMaterials.LinoleicAcid.getBucket()).optional(),
                    Task.item(GTOMaterials.Biotin.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.Yeast, 6)
            .dependsOn(YEAST));

    public static final Quest BETA_IONONE = CHAPTER.quest("beta_ionone", q -> q
            .at(12, 26)
            .title("β-香堇酮")
            .icon(GTOMaterials.BetaIonone.getBucket())
            .task(Task.item(GTOMaterials.BetaIonone.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Citral.getBucket(), 2)
            .dependsOn(CITRAL));

    public static final Quest CALCIUM_CARBIDE = CHAPTER.quest("calcium_carbide", q -> q
            .at(24, 30)
            .title("电石")
            .task(Task.item(TagPrefix.dust, GTOMaterials.CalciumCarbide, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Quicklime, 8)
            .dependsOn(LEACHED_TURPENTINE_COPY_1));

    public static final Quest ACETYLENE = CHAPTER.quest("acetylene", q -> q
            .at(20, 30)
            .title("乙炔")
            .icon(GTOMaterials.Acetylene.getBucket())
            .task(Task.item(GTOMaterials.Acetylene.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.CalciumCarbide, 3)
            .dependsOn(CALCIUM_CARBIDE));

    public static final Quest PROPARGYL_ALCOHOL = CHAPTER.quest("propargyl_alcohol", q -> q
            .at(18, 30)
            .title("炔丙醇")
            .icon(GTOMaterials.PropargylAlcohol.getBucket())
            .task(Task.item(GTOTagPrefix.CATALYST, GTMaterials.Copper, 1),
                    Task.item(GTOMaterials.PropargylAlcohol.getBucket()))
            .scaledReward(GTOMaterials.Acetylene.getBucket(), 2)
            .dependsOn(ACETYLENE));

    public static final Quest PROPARGYL_CHLORIDE = CHAPTER.quest("propargyl_chloride", q -> q
            .at(16, 30)
            .title("炔丙基氯")
            .icon(GTOMaterials.PropargylChloride.getBucket())
            .task(Task.item(GTOMaterials.PropargylChloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.PropargylAlcohol.getBucket(), 1)
            .dependsOn(PROPARGYL_ALCOHOL));

    public static final Quest VITAMIN_A = CHAPTER.quest("vitamin_a", q -> q
            .at(14, 28)
            .title("维生素A")
            .icon(GTOMaterials.VitaminA.getBucket())
            .task(Task.item(GTOMaterials.VitaminA.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.BetaIonone.getBucket(), 4)
            .dependsOn(BETA_IONONE, PROPARGYL_CHLORIDE));

    public static final Quest AMMONIUM_NITRATE = CHAPTER.quest("ammonium_nitrate_solution", q -> q
            .at(16, 28)
            .title("硝酸铵溶液")
            .icon(GTOMaterials.AmmoniumNitrateSolution.getBucket())
            .task(Task.item(GTOMaterials.AmmoniumNitrateSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(VITAMIN_A));

    public static final Quest B27_SUPPLEMENT = CHAPTER.quest("b27_supplement", q -> q
            .at(14, 26)
            .title("B27细胞培养添加剂")
            .icon(GTOMaterials.B27Supplement.getBucket())
            .task(Task.item(GTOMaterials.B27Supplement.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.VitaminA.getBucket(), 2)
            .dependsOn(VITAMIN_A, LINOLEIC_ACID_AND_BIOTIN));

    public static final Quest RAW_GROWTH_MEDIUM = CHAPTER.quest("raw_growth_medium", q -> q
            .at(16, 26)
            .title("原始生长培养基")
            .icon(GTMaterials.RawGrowthMedium.getBucket())
            .task(Task.item(GTMaterials.RawGrowthMedium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.B27Supplement.getBucket(), 1)
            .dependsOn(B27_SUPPLEMENT, AMMONIUM_NITRATE));

    public static final Quest STERILIZED_GROWTH_MEDIUM_BUCKET = CHAPTER.quest("sterilized_growth_medium_bucket", q -> q
            .at(16, 24)
            .title("灭菌生长培养基")
            .icon(GTMaterials.SterileGrowthMedium.getBucket())
            .task(Task.item(GTMaterials.SterileGrowthMedium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RawGrowthMedium.getBucket(), 2)
            .dependsOn(RAW_GROWTH_MEDIUM));

    public static final Quest GROWTH_FACTORS = CHAPTER.quest("growth_factors", q -> q
            .at(-6, 26)
            .title("生长因子")
            .icon(GTOMaterials.BasicFibroblastGrowthFactor.getBucket())
            .task(
                    Task.item(GTOMaterials.BasicFibroblastGrowthFactor.getBucket()).optional(),
                    Task.item(GTOMaterials.EpidermalGrowthFactor.getBucket()).optional(),
                    Task.item(GTOMaterials.Catalase.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.BloodPlasma.getBucket(), 4)
            .dependsOn(LEACHED_TURPENTINE));

    public static final Quest STEM_CELLS = CHAPTER.quest("stem_cells", q -> q
            .at(14, 22)
            .milestone()
            .title("干细胞")
            .task(Task.item(GTItems.STEM_CELLS, 32))
            .scaledReward(GTItems.STEM_CELLS, 4)
            .dependsOn(STERILIZED_GROWTH_MEDIUM_BUCKET));

    public static final Quest GREENHOUSE = CHAPTER.quest("greenhouse", q -> q
            .at(14, 20)
            .title("温室")
            .task(Task.item(MultiBlockD.GREENHOUSE))
            .scaledReward(GTItems.ELECTRIC_PISTON_MV, 2)
            .dependsOn(STEM_CELLS));

    public static final Quest INCUBATOR = CHAPTER.quest("incubator", q -> q
            .at(16, 20)
            .title("培养缸")
            .task(Task.item(MultiBlockD.INCUBATOR))
            .scaledReward(GTBlocks.FILTER_CASING, 2)
            .dependsOn(STEM_CELLS, GREENHOUSE));

    public static final Quest NEURO_PROCESSING_UNIT = CHAPTER.quest("neuro_processing_unit", q -> q
            .at(18, 22)
            .milestone()
            .title("神经处理单元")
            .task(Task.item(GTItems.NEURO_PROCESSOR, 8))
            .scaledReward(GTItems.STEM_CELLS, 16)
            .dependsOn(INCUBATOR, WETWARE_PRINTED_CIRCUIT_BOARD));

    public static final Quest ADVANCED_SMD = CHAPTER.quest("advanced_smd_resistor", q -> q
            .at(20, 24)
            .title("高级贴片电阻")
            .task(Task.item(GTItems.ADVANCED_SMD_RESISTOR, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Graphene, 4)
            .dependsOn(NEURO_PROCESSING_UNIT));

    public static final Quest WETWARE_PROCESSOR = CHAPTER.quest("wetware_processor", q -> q
            .at(22, 22)
            .milestone()
            .title("湿件处理器")
            .task(Task.item(GTItems.WETWARE_PROCESSOR_LuV, 8))
            .scaledReward(GTItems.NEURO_PROCESSOR, 1)
            .dependsOn(NEURO_PROCESSING_UNIT, ADVANCED_SMD, ZPM_CIRCUIT_ASSEMBLER));

    public static final Quest WETWARE_PROCESSOR_ASSEMBLY = CHAPTER.quest("wetware_processor_assembly", q -> q
            .at(26, 22)
            .milestone()
            .title("湿件处理器集群")
            .task(Task.item(GTItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM, 4))
            .scaledReward(GTItems.WETWARE_PROCESSOR_LuV, 2)
            .dependsOn(WETWARE_PROCESSOR));

    public static final Quest ZINC_PLASMA = CHAPTER.quest("zinc_plasma", q -> q
            .at(36, 26)
            .title("锌等离子体")
            .icon(GTMaterials.Zinc.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Zinc.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTMaterials.Trinium, 2)
            .dependsOn(ZPM_FUSION_REACTOR));

    public static final Quest TECH_WETWARE_TECH = CHAPTER.quest("tech_wetware_tech", q -> q
            .at(30, 24)
            .title("湿件技术")
            .task(Task.techNode(BaseNodes.WetwareTech))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewLuvChapter.ZPM_HULL));

    public static final Quest WETWARE_PROCESSOR_SUPERCOMPUTER = CHAPTER.quest("wetware_processor_supercomputer", q -> q
            .at(30, 22)
            .milestone()
            .title("湿件处理器超级计算机")
            .task(Task.item(GTItems.WETWARE_SUPER_COMPUTER_UV, 2))
            .scaledReward(GTItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM, 2)
            .dependsOn(WETWARE_PROCESSOR_ASSEMBLY, ZINC_PLASMA, TECH_WETWARE_TECH));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewLuvChapter.ZPM_HULL, 0, 0))
                .mainLine(NewLuvChapter.ZPM_HULL, LARGE_ASSEMBLER, BLAST_ALLOY_SMELTER, BIOCHEMICAL_REACTION, URHNQ_INGOT, PRECISION_ASSEMBLER, DRAWING_TOWER, MERCURY, COMPUTER_CASINGS, ZPM_ENERGY_HATCH, ZPM_FUSION_REACTOR, DARMSTADTIUM, UV_HULL, VISIT_GLACIO, WETWARE_PRINTED_CIRCUIT_BOARD, CETANE, CHITOSAN, CITRAL, STEM_CELLS, NEURO_PROCESSING_UNIT, WETWARE_PROCESSOR, WETWARE_PROCESSOR_ASSEMBLY, WETWARE_PROCESSOR_SUPERCOMPUTER));
    }
}
