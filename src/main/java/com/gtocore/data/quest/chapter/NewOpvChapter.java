package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.EnergyNodes;
import com.gtocore.data.techtree.MachinesNode;

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

import net.minecraft.world.item.Items;

public final class NewOpvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_opv", c -> c
            .title("16. 过载压时代(OpV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.OpV]));

    public static final Quest OPV_VOLTAGE_COIL = CHAPTER.quest("opv_voltage_coil", q -> q
            .at(10, 2)
            .title("过载压线圈")
            .task(Task.item(GTOItems.OPV_VOLTAGE_COIL, 4))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.Starmetal, 16)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE7 = CHAPTER.quest("tech_component_in_assembly_line7", q -> q
            .at(8, -2)
            .title("装配线进阶部件VII研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineopv))
            .scaledReward(GTOItems.OBSIDIAN_MATRIX, 1)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink TECH_COMPONENT_IN_ASSEMBLY_LINE7_COPY_1 = CHAPTER.copy(TECH_COMPONENT_IN_ASSEMBLY_LINE7, 8, 4);

    public static final Quest OPV_MOTION_COMPONENTS = CHAPTER.quest("opv_motor_pump_arm", q -> q
            .at(10, -2)
            .title("OpV 马达、泵与机械臂")
            .icon(GTItems.ELECTRIC_PUMP_OpV)
            .task(Task.item(GTItems.ELECTRIC_MOTOR_OpV, 8), Task.item(GTItems.ELECTRIC_PUMP_OpV, 2), Task.item(GTItems.ROBOT_ARM_OpV, 8))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.AttunedTengam, 2)
            .dependsOn(NewUxvChapter.OPV_HULL, TECH_COMPONENT_IN_ASSEMBLY_LINE7));

    public static final QuestLink OPV_MOTION_COMPONENTS_COPY_1 = CHAPTER.copy(OPV_MOTION_COMPONENTS, 4, 8);

    public static final QuestLink OPV_MOTION_COMPONENTS_COPY_2 = CHAPTER.copy(OPV_MOTION_COMPONENTS, -2, 6);

    public static final QuestLink OPV_MOTION_COMPONENTS_COPY_3 = CHAPTER.copy(OPV_MOTION_COMPONENTS, -4, 12);

    public static final Quest OPV_FIELD_COMPONENTS = CHAPTER.quest("opv_emitter_sensor_field", q -> q
            .at(6, 4)
            .title("OpV 发射器、传感器与力场发生器")
            .icon(GTItems.FIELD_GENERATOR_OpV)
            .task(Task.item(GTItems.EMITTER_OpV, 2), Task.item(GTItems.SENSOR_OpV, 2), Task.item(GTItems.FIELD_GENERATOR_OpV, 1))
            .scaledReward(GTOItems.UNSTABLE_STAR, 1)
            .dependsOn(NewUxvChapter.OPV_HULL, TECH_COMPONENT_IN_ASSEMBLY_LINE7_COPY_1));

    public static final QuestLink OPV_FIELD_COMPONENTS_COPY_1 = CHAPTER.copy(OPV_FIELD_COMPONENTS, 2, 14);

    public static final Quest TECH_ENERGY_IO_OPV = CHAPTER.quest("tech_energy_io_opv", q -> q
            .at(8, 2)
            .title("高压能量输入输出VIII研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.OpV]))
            .scaledReward(GTOItems.OBSIDIAN_MATRIX, 1)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest OPV_ENERGY_HATCH = CHAPTER.quest("opv_energy_hatch", q -> q
            .at(10, 0)
            .milestone()
            .title("OpV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.OpV], 2))
            .scaledReward(TagPrefix.cableGtSingle, GTOMaterials.CrystalMatrix, 4)
            .dependsOn(OPV_MOTION_COMPONENTS, OPV_VOLTAGE_COIL, NewUevChapter.SPACE_COOLANT_60K, TECH_ENERGY_IO_OPV));

    public static final Quest PRECISION_ROBOT_MK5 = CHAPTER.quest("precision_circuit_assembly_robot_mk5", q -> q
            .at(14, 4)
            .title("精密电路装配机器人 MK V")
            .task(Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK5))
            .scaledReward(TagPrefix.plate, GTOMaterials.Pikyonium, 2)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest TECH_HYPER_DIMENSIONAL_FORGE = CHAPTER.quest("tech_hyper_dimensional_forge", q -> q
            .at(2, -2)
            .title("超维锻造技术")
            .task(Task.techNode(BaseNodes.HyperDimensionalForge))
            .scaledReward(GTOItems.OBSIDIAN_MATRIX, 1)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink TECH_HYPER_DIMENSIONAL_FORGE_COPY_1 = CHAPTER.copy(TECH_HYPER_DIMENSIONAL_FORGE, 4, 16);

    public static final Quest HYPERDIMENSIONAL_PLASMA_FUSION_CORE = CHAPTER.quest("hyperdimensional_plasma_fusion_core", q -> q
            .at(4, 0)
            .milestone()
            .title("高维等离子聚变核心")
            .icon(MultiBlockD.HYPERDIMENSIONAL_PLASMA_FUSION_CORE)
            .task(Task.item(MultiBlockD.HYPERDIMENSIONAL_PLASMA_FUSION_CORE), Task.item(GTOBlocks.URUIUM_COIL_BLOCK, 551))
            .scaledReward(GTOBlocks.URUIUM_COIL_BLOCK, 8)
            .dependsOn(NewUxvChapter.OPV_HULL, TECH_HYPER_DIMENSIONAL_FORGE));

    public static final Quest DT_EXOTIC_CATALYST = CHAPTER.quest("dimensionally_transcendent_exotic_catalyst", q -> q
            .at(18, -4)
            .title("异星超维度催化剂")
            .icon(GTOMaterials.DimensionallyTranscendentExoticCatalyst.getBucket())
            .task(Task.item(GTOMaterials.DimensionallyTranscendentExoticCatalyst.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA).getBucket(), 1)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink DT_EXOTIC_CATALYST_COPY_1 = CHAPTER.copy(DT_EXOTIC_CATALYST, 8, 16);

    public static final Quest EXTREMELY_DURABLE_PLASMA_CELL = CHAPTER.quest("extremely_durable_plasma_cell", q -> q
            .at(-2, 0)
            .title("高耐久等离子体容器")
            .task(Task.item(GTOItems.EXTREMELY_DURABLE_PLASMA_CELL, 2))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Adamantine, 2)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest DENSE_NEUTRON_PLASMA_CELL = CHAPTER.quest("dense_neutron_plasma_cell", q -> q
            .at(-2, 2)
            .title("致密中子等离子体单元")
            .task(Task.item(GTOItems.DENSE_NEUTRON_PLASMA_CELL, 2))
            .scaledReward(TagPrefix.block, GTOMaterials.Neutron, 2)
            .dependsOn(NewUxvChapter.QCD_CHARGE, HYPERDIMENSIONAL_PLASMA_FUSION_CORE, EXTREMELY_DURABLE_PLASMA_CELL));

    public static final Quest COSMIC_NEUTRONIUM = CHAPTER.quest("cosmic_neutronium", q -> q
            .at(0, 2)
            .title("宇宙中子素")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.CosmicNeutronium, 16))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.CosmicNeutronium, 2)
            .dependsOn(DENSE_NEUTRON_PLASMA_CELL));

    public static final QuestLink COSMIC_NEUTRONIUM_COPY_1 = CHAPTER.copy(COSMIC_NEUTRONIUM, 10, 16);

    public static final QuestLink COSMIC_NEUTRONIUM_COPY_2 = CHAPTER.copy(COSMIC_NEUTRONIUM, 18, 10);

    public static final Quest COSMIC_NEUTRONIUM_CABLE = CHAPTER.quest("cosmic_neutronium_cable", q -> q
            .at(18, 12)
            .title("1x 宇宙中子素线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTOMaterials.CosmicNeutronium, 2))
            .scaledReward(GTOItems.HIGHLY_INSULATING_FOIL, 2)
            .dependsOn(COSMIC_NEUTRONIUM_COPY_2));

    public static final Quest INFINITY = CHAPTER.quest("infinity_ingot", q -> q
            .at(2, 4)
            .title("无尽")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Infinity, 16))
            .scaledReward(GTOItems.VIBRANT_CRYSTAL, 4)
            .dependsOn(COSMIC_NEUTRONIUM));

    public static final QuestLink INFINITY_COPY_1 = CHAPTER.copy(INFINITY, 0, 4);

    public static final QuestLink INFINITY_COPY_2 = CHAPTER.copy(INFINITY, 14, 8);

    public static final QuestLink INFINITY_COPY_3 = CHAPTER.copy(INFINITY, 4, 14);

    public static final Quest HIGHLY_DENSE_POLYMER_PLATE = CHAPTER.quest("highly_dense_polymer_plate", q -> q
            .at(2, 2)
            .title("高密度聚合物板")
            .task(Task.item(GTOItems.HIGHLY_DENSE_POLYMER_PLATE))
            .scaledReward(TagPrefix.plate, GTOMaterials.CarbonNanotubes, 2)
            .dependsOn(COSMIC_NEUTRONIUM));

    public static final Quest COSMIC_MESH = CHAPTER.quest("cosmic_mesh", q -> q
            .at(4, 2)
            .title("寰宇织网")
            .icon(GTOMaterials.CosmicMesh.getBucket())
            .task(Task.item(GTOMaterials.CosmicMesh.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.TIME_DILATION_CONTAINMENT_UNIT, 2)
            .dependsOn(HIGHLY_DENSE_POLYMER_PLATE));

    public static final Quest DRACONIUM_NANITES = CHAPTER.quest("draconium_nanites", q -> q
            .at(18, 0)
            .title("龙纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Draconium, 1))
            .scaledReward(TagPrefix.block, GTOMaterials.Draconium, 2)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink DRACONIUM_NANITES_COPY_1 = CHAPTER.copy(DRACONIUM_NANITES, 6, 2);

    public static final Quest COSMIC_NEUTRONIUM_NANITES = CHAPTER.quest("cosmic_neutronium_nanites", q -> q
            .at(4, 4)
            .title("宇宙中子素纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.CosmicNeutronium, 1))
            .scaledReward(TagPrefix.block, GTOMaterials.CosmicNeutronium, 1)
            .dependsOn(COSMIC_MESH, DRACONIUM_NANITES_COPY_1));

    public static final QuestLink COSMIC_NEUTRONIUM_NANITES_COPY_1 = CHAPTER.copy(COSMIC_NEUTRONIUM_NANITES, -4, 4);

    public static final QuestLink COSMIC_NEUTRONIUM_NANITES_COPY_2 = CHAPTER.copy(COSMIC_NEUTRONIUM_NANITES, -4, 14);

    public static final QuestLink COSMIC_NEUTRONIUM_NANITES_COPY_3 = CHAPTER.copy(COSMIC_NEUTRONIUM_NANITES, -6, 8);

    public static final Quest INFUSED_OBSIDIAN = CHAPTER.quest("infused_obsidian", q -> q
            .at(-4, 16)
            .title("注入龙力的黑曜石")
            .task(Task.item(GTOBlocks.INFUSED_OBSIDIAN, 2))
            .scaledReward(GTOItems.DRACONIUM_DIRT, 2)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest CHAOS_SHARD = CHAPTER.quest("chaos_shard", q -> q
            .at(-2, 16)
            .title("混沌碎片")
            .task(Task.item(GTOItems.CHAOS_SHARD, 2))
            .scaledReward(Items.BEDROCK, 1)
            .dependsOn(NewUxvChapter.QCD_CHARGE, HYPERDIMENSIONAL_PLASMA_FUSION_CORE, INFUSED_OBSIDIAN));

    public static final Quest CHAOS = CHAPTER.quest("chaos_ingot", q -> q
            .at(-2, 14)
            .title("混沌物质")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Chaos, 8))
            .scaledReward(GTOItems.TIME_DILATION_CONTAINMENT_UNIT, 2)
            .dependsOn(CHAOS_SHARD, COSMIC_NEUTRONIUM_NANITES_COPY_2));

    public static final Quest CONTAINED_EXOTIC_MATTER = CHAPTER.quest("contained_exotic_matter", q -> q
            .at(18, 6)
            .title("遏制奇异物质")
            .task(Task.item(GTOItems.CONTAINED_EXOTIC_MATTER, 64))
            .scaledReward(TagPrefix.dustTiny, GTOMaterials.DegenerateRhenium, 9)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink CONTAINED_EXOTIC_MATTER_COPY_1 = CHAPTER.copy(CONTAINED_EXOTIC_MATTER, 8, 8);

    public static final QuestLink CONTAINED_EXOTIC_MATTER_COPY_2 = CHAPTER.copy(CONTAINED_EXOTIC_MATTER, 16, 8);

    public static final Quest ESSENTIA_MATTER = CHAPTER.quest("essentia_matter", q -> q
            .at(16, 10)
            .title("根源物质")
            .task(Task.item(GTOItems.ESSENTIA_MATTER, 64))
            .scaledReward(GTOItems.AMORPHOUS_MATTER, 4)
            .dependsOn(NewUxvChapter.AMORPHOUS_MATTER));

    public static final Quest INFINITY_CATALYST = CHAPTER.quest("infinity_catalyst", q -> q
            .at(14, 10)
            .title("无尽催化剂")
            .task(Task.item(GTOItems.INFINITY_CATALYST))
            .scaledReward(GTOItems.CONTAINED_EXOTIC_MATTER, 8)
            .dependsOn(CONTAINED_EXOTIC_MATTER_COPY_2, ESSENTIA_MATTER, INFINITY_COPY_2));

    public static final QuestLink INFINITY_CATALYST_COPY_1 = CHAPTER.copy(INFINITY_CATALYST, 10, 4);

    public static final QuestLink INFINITY_CATALYST_COPY_2 = CHAPTER.copy(INFINITY_CATALYST, 18, 14);

    public static final Quest QUANTUM_ANOMALY = CHAPTER.quest("quantum_anomaly", q -> q
            .at(18, -2)
            .title("量子反常")
            .task(Task.item(GTOItems.QUANTUM_ANOMALY, 1))
            .scaledReward(GTOItems.ENTANGLED_SINGULARITY, 2)
            .dependsOn(NewUxvChapter.QCD_CHARGE, HYPERDIMENSIONAL_PLASMA_FUSION_CORE, DRACONIUM_NANITES, DT_EXOTIC_CATALYST));

    public static final Quest TEMPORAL_MATTER = CHAPTER.quest("temporal_matter", q -> q
            .at(16, -2)
            .title("时间物质")
            .task(Task.item(GTOItems.TEMPORAL_MATTER, 2))
            .scaledReward(GTOItems.KINETIC_MATTER, 2)
            .dependsOn(QUANTUM_ANOMALY));

    public static final Quest RECURSIVELY_FOLDED_NEGATIVE_SPACE = CHAPTER.quest("recursively_folded_negative_space", q -> q
            .at(16, 0)
            .milestone()
            .title("递归折叠负空间")
            .task(Task.item(GTOItems.RECURSIVELY_FOLDED_NEGATIVE_SPACE, 1))
            .scaledReward(GTOItems.MACROWORMHOLE_GENERATOR, 2)
            .dependsOn(HYPERDIMENSIONAL_PLASMA_FUSION_CORE, TEMPORAL_MATTER));

    public static final Quest SUPRACAUSAL_ASSEMBLY = CHAPTER.quest("supracausal_assembly", q -> q
            .at(16, 6)
            .milestone()
            .title("超因果处理器集群")
            .task(Task.item(GTOItems.SUPRACAUSAL_ASSEMBLY, 3))
            .scaledReward(GTOItems.SIMPLE_OPTICAL_SOC, 4)
            .dependsOn(RECURSIVELY_FOLDED_NEGATIVE_SPACE, CONTAINED_EXOTIC_MATTER, PRECISION_ROBOT_MK5));

    public static final Quest NUCLEAR_STAR = CHAPTER.quest("nuclear_star", q -> q
            .at(-2, 4)
            .title("核能之星")
            .task(Task.item(GTOItems.NUCLEAR_STAR, 1))
            .scaledReward(GTOItems.UNSTABLE_STAR, 2)
            .dependsOn(COSMIC_NEUTRONIUM_NANITES_COPY_1, INFINITY_COPY_1));

    public static final Quest STABILIZED_WORMHOLE_GENERATOR = CHAPTER.quest("stabilized_wormhole_generator", q -> q
            .at(10, 8)
            .title("稳定虫洞发生器")
            .task(Task.item(GTOItems.STABILIZED_WORMHOLE_GENERATOR, 2))
            .scaledReward(GTOItems.MICROWORMHOLE_GENERATOR, 2)
            .dependsOn(CONTAINED_EXOTIC_MATTER_COPY_1));

    public static final Quest EIGENFOLDED_KERR_MANIFOLD = CHAPTER.quest("eigenfolded_kerr_manifold", q -> q
            .at(10, 6)
            .milestone()
            .title("本征折叠克尔流形")
            .task(Task.item(GTOItems.EIGENFOLDED_KERR_MANIFOLD, 1))
            .scaledReward(GTOItems.STABILIZED_WORMHOLE_GENERATOR, 1)
            .dependsOn(STABILIZED_WORMHOLE_GENERATOR, RECURSIVELY_FOLDED_NEGATIVE_SPACE));

    public static final Quest SUPRACAUSAL_COMPUTER = CHAPTER.quest("supracausal_computer", q -> q
            .at(0, 6)
            .milestone()
            .title("超因果处理器超级计算机")
            .task(Task.item(GTOItems.SUPRACAUSAL_COMPUTER, 2))
            .scaledReward(TagPrefix.plate, GTOMaterials.Starmetal, 4)
            .dependsOn(SUPRACAUSAL_ASSEMBLY, NUCLEAR_STAR, EIGENFOLDED_KERR_MANIFOLD));

    public static final Quest OBSIDIAN_MATRIX = CHAPTER.quest("obsidian_matrix", q -> q
            .at(-6, 4)
            .title("黑曜石矩阵")
            .task(Task.item(GTOItems.OBSIDIAN_MATRIX, 1))
            .scaledReward(GTOItems.COSMIC_RAM_CHIP, 16)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest CTC_GUIDANCE_UNIT = CHAPTER.quest("closed_timelike_curve_guidance_unit", q -> q
            .at(-6, 6)
            .title("封闭类时曲线引导单元")
            .task(Task.item(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT, 1))
            .scaledReward(GTOItems.NUCLEAR_STAR, 2)
            .dependsOn(OBSIDIAN_MATRIX));

    public static final Quest CLOSED_TIMELIKE_CURVE_UNIT = CHAPTER.quest("closed_timelike_curve_computational_unit", q -> q
            .at(-4, 6)
            .title("封闭类时曲线计算单元")
            .task(Task.item(GTOItems.CLOSED_TIMELIKE_CURVE_COMPUTATIONAL_UNIT, 1))
            .scaledReward(GTOItems.CLOSED_TIMELIKE_CURVE_COMPUTATIONAL_UNIT_CONTAINER, 1)
            .dependsOn(EIGENFOLDED_KERR_MANIFOLD, CTC_GUIDANCE_UNIT));

    public static final Quest COSMIC_FABRIC = CHAPTER.quest("cosmic_fabric", q -> q
            .at(-4, 8)
            .title("寰宇织料")
            .task(Task.item(GTOItems.COSMIC_FABRIC, 64))
            .scaledReward(TagPrefix.foil, GTMaterials.Rubber, 16)
            .dependsOn(NewUxvChapter.AMORPHOUS_MATTER, COSMIC_NEUTRONIUM_NANITES_COPY_3));

    public static final Quest SUPRACAUSAL_MAINFRAME = CHAPTER.quest("supracausal_mainframe", q -> q
            .at(-2, 8)
            .title("超因果处理器主机")
            .task(Task.item(GTOItems.SUPRACAUSAL_MAINFRAME, 16))
            .scaledReward(TagPrefix.frameGt, GTOMaterials.Infinity, 2)
            .dependsOn(SUPRACAUSAL_COMPUTER, COSMIC_FABRIC, CLOSED_TIMELIKE_CURVE_UNIT, OPV_MOTION_COMPONENTS_COPY_2));

    public static final Quest TECH_ULTIMATE_BATTERY5 = CHAPTER.quest("tech_ultimate_battery5", q -> q
            .at(-6, 10)
            .title("终极电池V研究")
            .task(Task.techNode(EnergyNodes.UltimateBattery5))
            .scaledReward(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT, 1)
            .dependsOn(NewUxvChapter.OPV_HULL, NewUxvChapter.TECH_ULTIMATE_BATTERY4));

    public static final Quest INSANELY_MAX_BATTERY = CHAPTER.quest("insanely_max_battery", q -> q
            .at(-4, 10)
            .title("狂·终极电池")
            .task(Task.item(GTOItems.INSANELY_MAX_BATTERY, 4))
            .scaledReward(GTOItems.PM_WAFER, 16)
            .dependsOn(OPV_MOTION_COMPONENTS_COPY_3, TECH_ULTIMATE_BATTERY5));

    public static final Quest TECH_UNIVERSE_SIMULATION = CHAPTER.quest("tech_universe_simulation", q -> q
            .at(0, 14)
            .title("宇宙创造与湮灭技术")
            .task(Task.techNode(BaseNodes.BiowareDataStorage), Task.techNode(BaseNodes.BlockholeDataStorage), Task.techNode(BaseNodes.VirtualUniverseDataStorage), Task.techNode(BaseNodes.UniverseSimulation))
            .scaledReward(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT, 1)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final Quest TECH_TIME_DILATION_DIMENSION_SERIES_CASING = CHAPTER.quest("tech_time_dilation_dimension_series_casing", q -> q
            .at(-2, 12)
            .title("时间膨胀维度级系列外壳研究")
            .task(Task.techNode(MachinesNode.TimeDilationDimensionSeriesCasing))
            .scaledReward(GTOItems.OBSIDIAN_MATRIX, 1)
            .dependsOn(NewUxvChapter.OPV_HULL, NewUxvChapter.TECH_DIMENSION_SERIES_CASINGS));

    public static final Quest EYE_OF_HARMONY = CHAPTER.quest("eye_of_harmony", q -> q
            .at(0, 12)
            .milestone()
            .title("鸿蒙之眼")
            .icon(MultiBlockD.EYE_OF_HARMONY)
            .task(Task.item(MultiBlockD.EYE_OF_HARMONY),
                    Task.item(GTOBlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR, 138),
                    Task.item(GTOBlocks.DIMENSIONAL_STABILITY_CASING, 48))
            .scaledReward(GTOBlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR, 4)
            .dependsOn(SUPRACAUSAL_MAINFRAME, CHAOS, OPV_FIELD_COMPONENTS_COPY_1, INSANELY_MAX_BATTERY, TECH_UNIVERSE_SIMULATION, TECH_TIME_DILATION_DIMENSION_SERIES_CASING));

    public static final Quest DIMENSION_DATA = CHAPTER.quest("dimension_data", q -> q
            .at(2, 8)
            .title("维度数据")
            .task(Task.item(GTOItems.DIMENSION_DATA))
            .scaledReward(GTItems.TOOL_DATA_STICK, 4)
            .dependsOn(NewUxvChapter.OPV_HULL));

    public static final QuestLink DIMENSION_DATA_COPY_1 = CHAPTER.copy(DIMENSION_DATA, 18, 8);

    public static final Quest SPACETIME = CHAPTER.quest("spacetime", q -> q
            .at(2, 10)
            .title("时空")
            .icon(GTOMaterials.SpaceTime.getBucket())
            .task(Task.item(GTOMaterials.SpaceTime.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.CosmicElement.getBucket(), 16)
            .dependsOn(EYE_OF_HARMONY, NewUivChapter.SPACE_PROBE_PRODUCT, DIMENSION_DATA));

    public static final QuestLink SPACETIME_COPY_1 = CHAPTER.copy(SPACETIME, 20, 6);

    public static final Quest RAW_STAR_MATTER = CHAPTER.quest("raw_star_matter", q -> q
            .at(20, 8)
            .title("原始恒星混合物")
            .icon(GTOMaterials.RawStarMatter.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.RawStarMatter.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.CosmicElement.getBucket(), 16)
            .dependsOn(EYE_OF_HARMONY, NewUivChapter.SPACE_PROBE_PRODUCT, DIMENSION_DATA_COPY_1));

    public static final Quest DT_STELLAR_CATALYST = CHAPTER.quest("dimensionally_transcendent_stellar_catalyst", q -> q
            .at(20, 10)
            .title("恒星超维度催化剂")
            .icon(GTOMaterials.DimensionallyTranscendentStellarCatalyst.getBucket())
            .task(Task.item(GTOMaterials.DimensionallyTranscendentStellarCatalyst.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA).getBucket(), 1)
            .dependsOn(RAW_STAR_MATTER));

    public static final Quest HYPERCUBE = CHAPTER.quest("hypercube", q -> q
            .at(8, 14)
            .title("超立方体")
            .task(Task.item(GTOItems.HYPERCUBE, 2))
            .scaledReward(TagPrefix.rod, GTOMaterials.CelestialTungsten, 8)
            .dependsOn(COSMIC_NEUTRONIUM_COPY_1, DT_EXOTIC_CATALYST_COPY_1));

    public static final QuestLink HYPERCUBE_COPY_1 = CHAPTER.copy(HYPERCUBE, 22, 6);

    public static final Quest TECH_HYPER_DIMENSIONAL_FORGE_COIL = CHAPTER.quest("tech_hyper_dimensional_forge_coil", q -> q
            .at(6, 16)
            .title("超维锻造线圈改良研究")
            .task(Task.techNode(BaseNodes.HyperDimensionalForgeCoil))
            .scaledReward(GTOItems.OBSIDIAN_MATRIX, 1)
            .dependsOn(TECH_HYPER_DIMENSIONAL_FORGE_COPY_1));

    public static final Quest INFINITY_COIL = CHAPTER.quest("infinity_coil_block", q -> q
            .at(6, 14)
            .title("无尽线圈方块")
            .task(Task.item(GTOBlocks.INFINITY_COIL_BLOCK, 551))
            .scaledReward(TagPrefix.wireGtDouble, GTOMaterials.Infinity, 2)
            .dependsOn(INFINITY_COPY_3, TECH_HYPER_DIMENSIONAL_FORGE_COIL));

    public static final QuestLink INFINITY_COIL_COPY_1 = CHAPTER.copy(INFINITY_COIL, 12, 2);

    public static final QuestLink INFINITY_COIL_COPY_2 = CHAPTER.copy(INFINITY_COIL, 22, 16);

    public static final Quest SPACETIME_CATALYST = CHAPTER.quest("spacetime_catalyst", q -> q
            .at(8, 10)
            .title("时空催化剂")
            .task(Task.item(GTOItems.SPACETIME_CATALYST))
            .scaledReward(GTOMaterials.SpaceTime.getBucket(), 1)
            .dependsOn(INFINITY_CATALYST, SPACETIME));

    public static final Quest TRANSCENDENT_METAL = CHAPTER.quest("transcendent_metal", q -> q
            .at(8, 12)
            .milestone()
            .title("超时空金属")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TranscendentMetal, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Tennessine, 4)
            .dependsOn(INFINITY_COIL, SPACETIME_CATALYST, HYPERCUBE));

    public static final Quest RHUGNOR = CHAPTER.quest("rhugnor", q -> q
            .at(12, 4)
            .title("鲁格诺")
            .icon(GTOMaterials.Rhugnor.getBucket())
            .task(Task.item(GTOMaterials.Rhugnor.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.ENERGIUM_CRYSTAL, 16)
            .dependsOn(INFINITY_COIL_COPY_1, INFINITY_CATALYST_COPY_1));

    public static final Quest HYPOGEN = CHAPTER.quest("hypogen_ingot", q -> q
            .at(12, 8)
            .title("海珀珍")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Hypogen, 16))
            .scaledReward(GTOMaterials.DragonBlood.getBucket(), 2)
            .dependsOn(RHUGNOR));

    public static final Quest HYPOGEN_COIL = CHAPTER.quest("hypogen_coil_block", q -> q
            .at(12, 14)
            .title("海珀珍线圈方块")
            .task(Task.item(GTOBlocks.HYPOGEN_COIL_BLOCK, 551))
            .scaledReward(TagPrefix.screw, GTOMaterials.TranscendentMetal, 4)
            .dependsOn(HYPOGEN, TRANSCENDENT_METAL));

    public static final Quest NEUTRON_COMPRESSOR = CHAPTER.quest("neutron_compressor", q -> q
            .at(4, 6)
            .milestone()
            .title("中子态素压缩机")
            .task(Task.item(MultiBlockC.NEUTRON_COMPRESSOR))
            .scaledReward(TagPrefix.ingot, GTOMaterials.CosmicNeutronium, 4)
            .dependsOn(HYPERDIMENSIONAL_PLASMA_FUSION_CORE, OPV_MOTION_COMPONENTS_COPY_1, OPV_FIELD_COMPONENTS, COSMIC_NEUTRONIUM_NANITES, INFINITY));

    public static final Quest COMBINED_SINGULARITY = CHAPTER.quest("combined_singularity", q -> q
            .at(20, 14)
            .title("特化奇点")
            .icon(GTOItems.COMBINED_SINGULARITY_0)
            .task(Task.item(GTOItems.COMBINED_SINGULARITY_0), Task.item(GTOItems.COMBINED_SINGULARITY_1),
                    Task.item(GTOItems.COMBINED_SINGULARITY_2), Task.item(GTOItems.COMBINED_SINGULARITY_3),
                    Task.item(GTOItems.COMBINED_SINGULARITY_4), Task.item(GTOItems.COMBINED_SINGULARITY_5),
                    Task.item(GTOItems.COMBINED_SINGULARITY_6), Task.item(GTOItems.COMBINED_SINGULARITY_7),
                    Task.item(GTOItems.COMBINED_SINGULARITY_8), Task.item(GTOItems.COMBINED_SINGULARITY_9),
                    Task.item(GTOItems.COMBINED_SINGULARITY_10), Task.item(GTOItems.COMBINED_SINGULARITY_11),
                    Task.item(GTOItems.COMBINED_SINGULARITY_12), Task.item(GTOItems.COMBINED_SINGULARITY_13),
                    Task.item(GTOItems.COMBINED_SINGULARITY_14), Task.item(GTOItems.COMBINED_SINGULARITY_15))
            .scaledReward(TagPrefix.block, GTOMaterials.Neutron, 8)
            .dependsOn(NEUTRON_COMPRESSOR));

    public static final Quest INFINITY_SINGULARITY = CHAPTER.quest("infinity_singularity", q -> q
            .at(20, 12)
            .title("无尽奇点")
            .task(Task.item(GTOItems.INFINITY_SINGULARITY, 2))
            .scaledReward(GTOMaterials.AwakenedDraconium.getBucket(), 1)
            .dependsOn(INFINITY_CATALYST_COPY_2, COMBINED_SINGULARITY, DT_STELLAR_CATALYST));

    public static final Quest PRIMORDIAL_MATTER = CHAPTER.quest("primordial_matter", q -> q
            .at(22, 8)
            .title("流体本源物质")
            .icon(GTOMaterials.PrimordialMatter.getBucket())
            .task(Task.item(GTOMaterials.PrimordialMatter.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.SpatialFluid.getBucket(), 1)
            .dependsOn(SPACETIME_COPY_1, RAW_STAR_MATTER, HYPERCUBE_COPY_1));

    public static final Quest ETERNITY = CHAPTER.quest("eternity_ingot", q -> q
            .at(22, 14)
            .title("永恒")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Eternity, 4))
            .scaledReward(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE, 4)
            .dependsOn(INFINITY_SINGULARITY, PRIMORDIAL_MATTER, INFINITY_COIL_COPY_2));

    public static final Quest ETERNITY_COIL = CHAPTER.quest("eternity_coil_block", q -> q
            .at(14, 16)
            .title("永恒线圈方块")
            .task(Task.item(GTOBlocks.ETERNITY_COIL_BLOCK, 551))
            .scaledReward(TagPrefix.foil, GTOMaterials.Chaos, 8)
            .dependsOn(ETERNITY, HYPOGEN_COIL));

    public static final Quest CHAOS_INFINITY_ALLOY = CHAPTER.quest("chaos_infinity_alloy_ingot", q -> q
            .at(14, 12)
            .milestone()
            .title("混沌无尽合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.ChaosInfinityAlloy, 8))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.CosmicNeutronium, 2)
            .dependsOn(ETERNITY_COIL));

    public static final Quest MAX_CASING = CHAPTER.quest("max_casing", q -> q
            .at(16, 14)
            .title("MAX 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_MAX))
            .scaledReward(TagPrefix.plate, GTOMaterials.ChaosInfinityAlloy, 2)
            .dependsOn(CHAOS_INFINITY_ALLOY));

    public static final Quest MAX_HULL = CHAPTER.quest("max_hull", q -> q
            .at(16, 12)
            .milestone()
            .title("MAX 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.MAX]))
            .reward(Reward.item(GTMachines.HULL[GTValues.MAX], 1))
            .dependsOn(MAX_CASING, COSMIC_NEUTRONIUM_CABLE));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewUxvChapter.OPV_HULL, 0, 0))
                .mainLine(NewUxvChapter.OPV_HULL, HYPERDIMENSIONAL_PLASMA_FUSION_CORE, OPV_ENERGY_HATCH, RECURSIVELY_FOLDED_NEGATIVE_SPACE, SUPRACAUSAL_ASSEMBLY, EIGENFOLDED_KERR_MANIFOLD, NEUTRON_COMPRESSOR, SUPRACAUSAL_COMPUTER, EYE_OF_HARMONY, TRANSCENDENT_METAL, CHAOS_INFINITY_ALLOY, MAX_HULL));
    }
}
