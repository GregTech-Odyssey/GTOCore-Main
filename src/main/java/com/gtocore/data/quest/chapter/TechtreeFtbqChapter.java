package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockE;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.MultiBlockI;
import com.gtocore.common.data.machines.SpaceMultiblock;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.celsiusqc.ad_astra_rocketed.common.registry.ModItems;

public final class TechtreeFtbqChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("techtree_ftbq", c -> c
            .title("科技树任务")
            .icon(GTOItems.DATA_CRYSTAL_MK2)
            .hideQuestsUntilDepsComplete()
            .autofocus(TechtreeFtbqChapter.ME_CATALYST_PATTERN_BUFFER));

    public static final Quest ACTIVE_TRANSFORMER = CHAPTER.quest("active_transformer", q -> q
            .at(0.5, -1)
            .task(Task.item(GTMultiMachines.ACTIVE_TRANSFORMER)));

    public static final Quest DATA_BANK = CHAPTER.quest("data_bank", q -> q
            .at(2.58, -3.05)
            .task(Task.item(GTResearchMachines.DATA_BANK)));

    public static final Quest HIGH_PERFORMANCE_COMPUTATION_ARRAY = CHAPTER.quest("high_performance_computation_array", q -> q
            .at(5.41, -5.25)
            .task(
                    Task.item(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY),
                    Task.item(GTResearchMachines.NETWORK_SWITCH),
                    Task.item(ExResearchMachines.COMPUTATIONAL_DATA_HOLDER))
            .dependsOn(DATA_BANK));

    public static final Quest NEUTRON_IRRADIATION_HATCH = CHAPTER.quest("neutron_irradiation_hatch", q -> q
            .at(-3.05, -4.14)
            .task(Task.item(GTOMachines.NEUTRON_IRRADIATION_HATCH)));

    public static final Quest ADVANCED_DATA_ACCESS_HATCH = CHAPTER.quest("advanced_data_access_hatch", q -> q
            .at(3.52, -4.7)
            .task(Task.item(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH), Task.item(ExResearchMachines.DATA_CENTER))
            .dependsOn(DATA_BANK));

    public static final Quest ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_SUPERCONDUCTOR_BASE = CHAPTER.quest("enriched_naquadah_trinium_europium_duranide_superconductor_base", q -> q
            .at(-2.34, -2.38)
            .task(
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide),
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.RutheniumTriniumAmericiumNeutronate),
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.UraniumRhodiumDinaquadide)));

    public static final Quest LUV_FUSION_REACTOR = CHAPTER.quest("luv_fusion_reactor", q -> q
            .at(-4.71, -3.15)
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.LuV]))
            .dependsOn(NEUTRON_IRRADIATION_HATCH, ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_SUPERCONDUCTOR_BASE));

    public static final Quest OBJECT_HOLDER = CHAPTER.quest("object_holder", q -> q
            .at(2, -4.5)
            .task(
                    Task.item(GTResearchMachines.OBJECT_HOLDER),
                    Task.item(ExResearchMachines.PRIMORDIAL_SCANNING_STATION),
                    Task.item(ExResearchMachines.RESEARCH_HOLDER),
                    Task.item(GTResearchMachines.RESEARCH_STATION))
            .dependsOn(ACTIVE_TRANSFORMER));

    public static final Quest THERMODYNAMIC_DATA_HOLDER = CHAPTER.quest("thermodynamic_data_holder", q -> q
            .at(-9.51, 10.51)
            .task(Task.item(ExResearchMachines.THERMODYNAMIC_DATA_HOLDER), Task.item(ExResearchMachines.THERMODYNAMIC_ANALYSIS_PLATFORM)));

    public static final Quest ME_PATTERN_BUFFER = CHAPTER.quest("me_pattern_buffer", q -> q
            .at(-3, 2.11)
            .task(
                    Task.item(GTAEMachines.ME_PATTERN_BUFFER),
                    Task.item(GTAEMachines.ME_PATTERN_BUFFER_PROXY),
                    Task.item(GTAEMachines.ME_REQUESTABLE_INPUT_HATCH_MACHINE),
                    Task.item(GTAEMachines.ME_REQUESTABLE_INPUT_BUS_MACHINE)));

    public static final Quest ME_CRAFT_PATTERN_PART_MACHINE = CHAPTER.quest("me_craft_pattern_part_machine", q -> q
            .at(-4.35, 3.44)
            .task(Task.item(GTAEMachines.ME_CRAFT_PATTERN_PART_MACHINE), Task.item(MultiBlockG.SUPER_MOLECULAR_ASSEMBLER))
            .dependsOn(ME_PATTERN_BUFFER));

    public static final Quest ME_EXTEND_PATTERN_BUFFER = CHAPTER.quest("me_extend_pattern_buffer", q -> q
            .at(-8.06, 4.58)
            .task(Task.item(GTAEMachines.ME_EXTEND_PATTERN_BUFFER), Task.item(GTAEMachines.ME_EXTEND_PATTERN_BUFFER_ULTRA))
            .dependsOn(ME_PATTERN_BUFFER));

    public static final Quest ME_CATALYST_PATTERN_BUFFER = CHAPTER.quest("me_catalyst_pattern_buffer", q -> q
            .at(-5.78, 2.2)
            .task(Task.item(GTAEMachines.ME_CATALYST_ME_PATTERN_BUFFER))
            .dependsOn(ME_PATTERN_BUFFER));

    public static final Quest ME_INPUT_BUFFER_PART_MACHINE = CHAPTER.quest("me_input_buffer_part_machine", q -> q
            .at(-7.6, 2.66)
            .task(Task.item(GTAEMachines.ME_INPUT_BUFFER_PART_MACHINE), Task.item(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER))
            .dependsOn(ME_PATTERN_BUFFER));

    public static final Quest ME_MUFFLER_HATCH = CHAPTER.quest("me_muffler_hatch", q -> q
            .at(-6.15, 4.1)
            .task(Task.item(GTAEMachines.MUFFLER_HATCH_ME))
            .dependsOn(ME_PATTERN_BUFFER));

    public static final Quest LUV_ELECTRIC_MOTOR = CHAPTER.quest("luv_electric_motor", q -> q
            .at(3.25, -1.24)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_LuV),
                    Task.item(GTItems.ELECTRIC_PUMP_LuV),
                    Task.item(GTItems.FLUID_REGULATOR_LuV),
                    Task.item(GTItems.CONVEYOR_MODULE_LuV),
                    Task.item(GTItems.ELECTRIC_PISTON_LuV),
                    Task.item(GTItems.ROBOT_ARM_LuV),
                    Task.item(GTItems.FIELD_GENERATOR_LuV),
                    Task.item(GTItems.EMITTER_LuV),
                    Task.item(GTItems.SENSOR_LuV)));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_LUV = CHAPTER.quest("component_assembly_line_casing_luv", q -> q
            .at(6.5, 0.5)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest ZPM_ELECTRIC_MOTOR = CHAPTER.quest("zpm_electric_motor", q -> q
            .at(7, -1)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_ZPM),
                    Task.item(GTItems.ELECTRIC_PUMP_ZPM),
                    Task.item(GTItems.FLUID_REGULATOR_ZPM),
                    Task.item(GTItems.CONVEYOR_MODULE_ZPM),
                    Task.item(GTItems.ELECTRIC_PISTON_ZPM),
                    Task.item(GTItems.ROBOT_ARM_ZPM),
                    Task.item(GTItems.FIELD_GENERATOR_ZPM),
                    Task.item(GTItems.EMITTER_ZPM),
                    Task.item(GTItems.SENSOR_ZPM))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_ZPM = CHAPTER.quest("component_assembly_line_casing_zpm", q -> q
            .at(8, 1)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM))
            .dependsOn(COMPONENT_ASSEMBLY_LINE_CASING_LUV, ZPM_ELECTRIC_MOTOR));

    public static final Quest UV_ELECTRIC_MOTOR = CHAPTER.quest("uv_electric_motor", q -> q
            .at(9, -0.5)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_UV),
                    Task.item(GTItems.ELECTRIC_PUMP_UV),
                    Task.item(GTItems.FLUID_REGULATOR_UV),
                    Task.item(GTItems.CONVEYOR_MODULE_UV),
                    Task.item(GTItems.ELECTRIC_PISTON_UV),
                    Task.item(GTItems.ROBOT_ARM_UV),
                    Task.item(GTItems.FIELD_GENERATOR_UV),
                    Task.item(GTItems.EMITTER_UV),
                    Task.item(GTItems.SENSOR_UV))
            .dependsOn(ZPM_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_UV = CHAPTER.quest("component_assembly_line_casing_uv", q -> q
            .at(9.5, 1.5)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UV))
            .dependsOn(COMPONENT_ASSEMBLY_LINE_CASING_ZPM, UV_ELECTRIC_MOTOR));

    public static final Quest UHV_ELECTRIC_MOTOR = CHAPTER.quest("uhv_electric_motor", q -> q
            .at(10.5, 0)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_UHV),
                    Task.item(GTItems.ELECTRIC_PUMP_UHV),
                    Task.item(GTItems.FLUID_REGULATOR_UHV),
                    Task.item(GTItems.CONVEYOR_MODULE_UHV),
                    Task.item(GTItems.ELECTRIC_PISTON_UHV),
                    Task.item(GTItems.ROBOT_ARM_UHV),
                    Task.item(GTItems.FIELD_GENERATOR_UHV),
                    Task.item(GTItems.EMITTER_UHV),
                    Task.item(GTItems.SENSOR_UHV))
            .dependsOn(UV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_UHV = CHAPTER.quest("component_assembly_line_casing_uhv", q -> q
            .at(11, 2)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UHV))
            .dependsOn(UHV_ELECTRIC_MOTOR, COMPONENT_ASSEMBLY_LINE_CASING_UV));

    public static final Quest UEV_ELECTRIC_MOTOR = CHAPTER.quest("uev_electric_motor", q -> q
            .at(12, 0.5)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_UEV),
                    Task.item(GTItems.ELECTRIC_PUMP_UEV),
                    Task.item(GTItems.FLUID_REGULATOR_UEV),
                    Task.item(GTItems.CONVEYOR_MODULE_UEV),
                    Task.item(GTItems.ELECTRIC_PISTON_UEV),
                    Task.item(GTItems.ROBOT_ARM_UEV),
                    Task.item(GTItems.FIELD_GENERATOR_UEV),
                    Task.item(GTItems.EMITTER_UEV),
                    Task.item(GTItems.SENSOR_UEV))
            .dependsOn(UHV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_UEV = CHAPTER.quest("component_assembly_line_casing_uev", q -> q
            .at(12.5, 2.5)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UEV))
            .dependsOn(COMPONENT_ASSEMBLY_LINE_CASING_UHV, UEV_ELECTRIC_MOTOR));

    public static final Quest UIV_ELECTRIC_MOTOR = CHAPTER.quest("uiv_electric_motor", q -> q
            .at(13.5, 1)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_UIV),
                    Task.item(GTItems.ELECTRIC_PUMP_UIV),
                    Task.item(GTItems.FLUID_REGULATOR_UIV),
                    Task.item(GTItems.CONVEYOR_MODULE_UIV),
                    Task.item(GTItems.ELECTRIC_PISTON_UIV),
                    Task.item(GTItems.ROBOT_ARM_UIV),
                    Task.item(GTItems.FIELD_GENERATOR_UIV),
                    Task.item(GTItems.EMITTER_UIV),
                    Task.item(GTItems.SENSOR_UIV))
            .dependsOn(UEV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_UIV = CHAPTER.quest("component_assembly_line_casing_uiv", q -> q
            .at(14, 3)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UIV))
            .dependsOn(UIV_ELECTRIC_MOTOR, COMPONENT_ASSEMBLY_LINE_CASING_UEV));

    public static final Quest UXV_ELECTRIC_MOTOR = CHAPTER.quest("uxv_electric_motor", q -> q
            .at(15, 1.5)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_UXV),
                    Task.item(GTItems.ELECTRIC_PUMP_UXV),
                    Task.item(GTItems.FLUID_REGULATOR_UXV),
                    Task.item(GTItems.CONVEYOR_MODULE_UXV),
                    Task.item(GTItems.ELECTRIC_PISTON_UXV),
                    Task.item(GTItems.ROBOT_ARM_UXV),
                    Task.item(GTItems.FIELD_GENERATOR_UXV),
                    Task.item(GTItems.EMITTER_UXV),
                    Task.item(GTItems.SENSOR_UXV))
            .dependsOn(UIV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_UXV = CHAPTER.quest("component_assembly_line_casing_uxv", q -> q
            .at(15.5, 3.5)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UXV))
            .dependsOn(UXV_ELECTRIC_MOTOR, COMPONENT_ASSEMBLY_LINE_CASING_UIV));

    public static final Quest OPV_ELECTRIC_MOTOR = CHAPTER.quest("opv_electric_motor", q -> q
            .at(16.5, 2)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_OpV),
                    Task.item(GTItems.ELECTRIC_PUMP_OpV),
                    Task.item(GTItems.FLUID_REGULATOR_OpV),
                    Task.item(GTItems.CONVEYOR_MODULE_OpV),
                    Task.item(GTItems.ELECTRIC_PISTON_OpV),
                    Task.item(GTItems.ROBOT_ARM_OpV),
                    Task.item(GTItems.FIELD_GENERATOR_OpV),
                    Task.item(GTItems.EMITTER_OpV),
                    Task.item(GTItems.SENSOR_OpV))
            .dependsOn(UXV_ELECTRIC_MOTOR));

    public static final Quest MAX_ELECTRIC_MOTOR = CHAPTER.quest("max_electric_motor", q -> q
            .at(18, 2.5)
            .task(
                    Task.item(GTOItems.MAX_ELECTRIC_MOTOR),
                    Task.item(GTOItems.MAX_ELECTRIC_PUMP),
                    Task.item(GTOItems.MAX_CONVEYOR_MODULE),
                    Task.item(GTOItems.MAX_ROBOT_ARM),
                    Task.item(GTOItems.MAX_FIELD_GENERATOR),
                    Task.item(GTOItems.MAX_EMITTER),
                    Task.item(GTOItems.MAX_SENSOR),
                    Task.item(GTOItems.MAX_ELECTRIC_PISTON))
            .dependsOn(OPV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_OPV = CHAPTER.quest("component_assembly_line_casing_opv", q -> q
            .at(17, 4)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_OPV))
            .dependsOn(COMPONENT_ASSEMBLY_LINE_CASING_UXV, OPV_ELECTRIC_MOTOR));

    public static final Quest COMPONENT_ASSEMBLY_LINE_CASING_MAX = CHAPTER.quest("component_assembly_line_casing_max", q -> q
            .at(18.5, 4.5)
            .task(Task.item(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_MAX))
            .dependsOn(MAX_ELECTRIC_MOTOR, COMPONENT_ASSEMBLY_LINE_CASING_OPV));

    public static final Quest CRYSTAL_PROCESSOR_MAINFRAME = CHAPTER.quest("crystal_processor_mainframe", q -> q
            .at(8, -3)
            .task(Task.item(GTItems.CRYSTAL_MAINFRAME_UV))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest DATA_FORM_TESTING_ME_INTERFACE = CHAPTER.quest("data_form_testing_me_interface", q -> q
            .at(4.29, -6.86)
            .task(
                    Task.item(ExResearchMachines.DATA_FORM_TESTING_ME_INTERFACE),
                    Task.item(ExResearchMachines.DATA_FORM_TESTING_PLANT),
                    Task.item(ExResearchMachines.STORAGE_DATA_HOLDER))
            .dependsOn(HIGH_PERFORMANCE_COMPUTATION_ARRAY));

    public static final Quest LARGE_VOID_MINER = CHAPTER.quest("large_void_miner", q -> q
            .at(-1.14, -3.85)
            .task(Task.item(MultiBlockA.LARGE_VOID_MINER), Task.item(MultiBlockA.VOID_FLUID_DRILLING_RIG)));

    public static final Quest ZPM_FUSION_REACTOR = CHAPTER.quest("zpm_fusion_reactor", q -> q
            .at(-5.94, -4.75)
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.ZPM]))
            .dependsOn(LUV_FUSION_REACTOR));

    public static final Quest WETWARE_PROCESSOR_COMPUTER = CHAPTER.quest("wetware_processor_computer", q -> q
            .at(-3.64, -1.01)
            .task(Task.item(GTItems.WETWARE_SUPER_COMPUTER_UV), Task.item(GTItems.WETWARE_MAINFRAME_UHV)));

    public static final Quest VIRTUAL_COIN_MINER = CHAPTER.quest("virtual_coin_miner", q -> q
            .at(7.15, -4.53)
            .task(Task.item(MultiBlockI.VIRTUAL_COIN_MINER))
            .dependsOn(HIGH_PERFORMANCE_COMPUTATION_ARRAY));

    public static final Quest ADJUSTABLE_SEMI_REFLECTOR = CHAPTER.quest("adjustable_semi_reflector", q -> q
            .at(-1, 1.5)
            .task(
                    Task.item(GTOMachines.ADJUSTABLE_SEMI_REFLECTOR),
                    Task.item(GTOMachines.BEAM_ACCESS_HATCH),
                    Task.item(GTOMachines.BEAM_REDIRECTOR),
                    Task.item(GTOMachines.EXCITATION_CRYSTAL),
                    Task.item(ExResearchMachines.LASER_COMPUTATION_TESTING_PLATFORM),
                    Task.item(ExResearchMachines.OPTICAL_DATA_HOLDER),
                    Task.item(GTOMachines.RAY_BEAM_POLARIZER))
            .dependsOn(ACTIVE_TRANSFORMER));

    public static final Quest ENGRAVING_LASER_PLANT = CHAPTER.quest("engraving_laser_plant", q -> q
            .at(-1.86, -0.3)
            .task(Task.item(MultiBlockA.ENGRAVING_LASER_PLANT))
            .dependsOn(ACTIVE_TRANSFORMER));

    public static final Quest BIOWARE_PROCESSING_CORE = CHAPTER.quest("bioware_processing_core", q -> q
            .at(-5.56, -1.42)
            .task(Task.item(GTOItems.BIOWARE_PROCESSING_CORE))
            .dependsOn(WETWARE_PROCESSOR_COMPUTER));

    public static final Quest DECAY_HASTENER = CHAPTER.quest("decay_hastener", q -> q
            .at(-4.22, -5.64)
            .task(Task.item(MultiBlockA.DECAY_HASTENER), Task.item(MultiBlockF.SUPER_PARTICLE_COLLIDER))
            .dependsOn(NEUTRON_IRRADIATION_HATCH));

    public static final Quest SUPERCOMPUTING_CENTER = CHAPTER.quest("supercomputing_center", q -> q
            .at(5.75, -8.18)
            .task(Task.item(ExResearchMachines.SUPERCOMPUTING_CENTER))
            .dependsOn(HIGH_PERFORMANCE_COMPUTATION_ARRAY));

    public static final Quest BIO_DATA_ACCESS_HATCH = CHAPTER.quest("bio_data_access_hatch", q -> q
            .at(-8.16, 0.84)
            .task(Task.item(ExResearchMachines.BIO_DATA_ACCESS_HATCH))
            .dependsOn(BIOWARE_PROCESSING_CORE));

    public static final Quest UV_FUSION_REACTOR = CHAPTER.quest("uv_fusion_reactor", q -> q
            .at(-7.88, -4.46)
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.UV]))
            .dependsOn(ZPM_FUSION_REACTOR));

    public static final Quest CHEMICAL_PLANT = CHAPTER.quest("chemical_plant", q -> q
            .at(-7.69, 11.4)
            .task(Task.item(MultiBlockA.CHEMICAL_PLANT), Task.item(ExResearchMachines.CATALYSIS_DATA_HOLDER)));

    public static final Quest AUTO_CONFIGURATION_MAINTENANCE_HATCH = CHAPTER.quest("auto_configuration_maintenance_hatch", q -> q
            .at(4.5, -2.97)
            .task(Task.item(GTOMachines.AUTO_CONFIGURATION_MAINTENANCE_HATCH), Task.item(GTOMachines.MODULAR_CONFIGURATION_MAINTENANCE_HATCH))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest IRIDIUM_CASING = CHAPTER.quest("iridium_casing", q -> q
            .at(1.84, 0.54)
            .task(Task.item(GTOBlocks.IRIDIUM_CASING)));

    public static final Quest ISA_MILL = CHAPTER.quest("isa_mill", q -> q
            .at(6.5, -3)
            .task(Task.item(MultiBlockD.ISA_MILL), Task.item(MultiBlockA.INDUSTRIAL_FLOTATION_CELL))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest FUEL_REFINING_COMPLEX = CHAPTER.quest("fuel_refining_complex", q -> q
            .at(-7.56, 13.7)
            .task(Task.item(MultiBlockB.FUEL_REFINING_COMPLEX))
            .dependsOn(CHEMICAL_PLANT));

    public static final Quest CONTAINMENT_FIELD_GENERATOR = CHAPTER.quest("containment_field_generator", q -> q
            .at(2.2, 3.96)
            .task(Task.item(GTOBlocks.CONTAINMENT_FIELD_GENERATOR), Task.item(GTOBlocks.HOLLOW_CASING), Task.item(GTOBlocks.MOLECULAR_COIL))
            .dependsOn(IRIDIUM_CASING));

    public static final Quest PRECISION_ASSEMBLER = CHAPTER.quest("precision_assembler", q -> q
            .at(9.5, -2)
            .task(Task.item(MultiBlockA.PRECISION_ASSEMBLER))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest ADVANCED_ASSEMBLY_LINE = CHAPTER.quest("advanced_assembly_line", q -> q
            .at(12.11, -3)
            .task(Task.item(MultiBlockD.ADVANCED_ASSEMBLY_LINE))
            .dependsOn(PRECISION_ASSEMBLER));

    public static final Quest Q_479044A4 = CHAPTER.quest("q_479044a4", q -> q
            .at(0.5, 3.5)
            .task(Task.checkmark().title("激光能源革命"))
            .dependsOn(ACTIVE_TRANSFORMER, IRIDIUM_CASING, ADJUSTABLE_SEMI_REFLECTOR));

    public static final Quest HAND_OF_ARACHNE = CHAPTER.quest("hand_of_arachne", q -> q
            .at(-4.68, 5.34)
            .task(
                    Task.item(MultiBlockC.HAND_OF_ARACHNE),
                    Task.item(MultiBlockC.NANITES_CIRCUIT_ASSEMBLY_FACTORY),
                    Task.item(MultiBlockC.PRECISION_ASSEMBLY_CENTER),
                    Task.item(MultiBlockA.INTEGRATED_ASSEMBLER))
            .dependsOn(Q_479044A4));

    public static final Quest HOLY_SEPARATOR = CHAPTER.quest("holy_separator", q -> q
            .at(-2.5, 3.96)
            .task(
                    Task.item(MultiBlockF.HOLY_SEPARATOR),
                    Task.item(MultiBlockH.KERR_NEWMAN_HOMOGENIZER),
                    Task.item(MultiBlockH.MANTLE_CRUSHER),
                    Task.item(MultiBlockB.MOLECULAR_OSCILLATION_DEHYDRATOR),
                    Task.item(MultiBlockB.NANO_PHAGOCYTOSIS_PLANT))
            .dependsOn(Q_479044A4));

    public static final Quest HIGH_ENERGY_LASER_LATHE = CHAPTER.quest("high_energy_laser_lathe", q -> q
            .at(-3.05, 6.31)
            .task(
                    Task.item(MultiBlockB.HIGH_ENERGY_LASER_LATHE),
                    Task.item(MultiBlockA.MEGA_WIREMILL),
                    Task.item(MultiBlockB.NEUTRONIUM_WIRE_CUTTING))
            .dependsOn(Q_479044A4));

    public static final Quest LIGHTNING_ROD = CHAPTER.quest("lightning_rod", q -> q
            .at(0.12, 6.75)
            .task(
                    Task.item(MultiBlockB.LIGHTNING_ROD),
                    Task.item(MultiBlockB.MAGNETIC_ENERGY_REACTION_FURNACE),
                    Task.item(MultiBlockA.SUPERCONDUCTING_MAGNETIC_PRESSER))
            .dependsOn(Q_479044A4));

    public static final Quest CHEMICAL_COMPLEX = CHAPTER.quest("chemical_complex", q -> q
            .at(-1.34, 5.53)
            .task(
                    Task.item(MultiBlockA.CHEMICAL_COMPLEX),
                    Task.item(MultiBlockF.COMPOUND_DISTILLATION_FRACTIONATOR),
                    Task.item(MultiBlockF.CRYSTAL_BUILDER),
                    Task.item(MultiBlockH.DISSOLUTION_CORE),
                    Task.item(MultiBlockH.GIANT_ELECTROCHEMICAL_WORKSTATION),
                    Task.item(MultiBlockH.GIANT_FLOTATION_TANK),
                    Task.item(MultiBlockC.HIGH_TEMPERATURE_REACTION_HUB),
                    Task.item(MultiBlockB.MEGA_BATH_TANK),
                    Task.item(MultiBlockB.MEGA_BREWER),
                    Task.item(MultiBlockA.PHASE_CHANGE_CUBE))
            .dependsOn(Q_479044A4));

    public static final Quest CRACKER_HUB = CHAPTER.quest("cracker_hub", q -> q
            .at(0.49, 4.85)
            .task(
                    Task.item(MultiBlockC.CRACKER_HUB),
                    Task.item(MultiBlockC.EXTREME_ELECTRIC_FURNACE),
                    Task.item(MultiBlockH.GIANT_SINTERING_ARRAY),
                    Task.item(MultiBlockB.MEGA_VACUUM_DRYING_FURNACE),
                    Task.item(MultiBlockA.MOLTEN_CORE),
                    Task.item(MultiBlockF.SUPER_BLAST_SMELTER))
            .dependsOn(Q_479044A4));

    public static final Quest GEMINI_CONTAINMENT_SYSTEM = CHAPTER.quest("gemini_containment_system", q -> q
            .at(2.07, 5.86)
            .task(
                    Task.item(MultiBlockH.GEMINI_CONTAINMENT_SYSTEM),
                    Task.item(MultiBlockA.PARTICLE_STREAM_MATRIX_FILLING_MACHINE),
                    Task.item(MultiBlockH.SMART_FILTERING_HUB))
            .dependsOn(Q_479044A4));

    public static final Quest EXTREME_COMPRESSOR = CHAPTER.quest("extreme_compressor", q -> q
            .at(-0.72, 3.29)
            .task(
                    Task.item(MultiBlockB.HORIZONTAL_COMPRESSOR),
                    Task.item(MultiBlockF.FIELD_EXTRUDER_FACTORY),
                    Task.item(MultiBlockC.GRAVITY_BENDING_DEVICE),
                    Task.item(MultiBlockA.HEAVY_ROLLING),
                    Task.item(MultiBlockH.NEUTRON_FORGING_ANVIL),
                    Task.item(MultiBlockA.SUPERCONDUCTING_MAGNETIC_PRESSER))
            .dependsOn(Q_479044A4));

    public static final Quest DIMENSION_INJECTION_CASING = CHAPTER.quest("dimension_injection_casing", q -> q
            .at(2.55, 7.74)
            .task(
                    Task.item(GTOBlocks.DIMENSION_INJECTION_CASING),
                    Task.item(GTOBlocks.DIMENSIONAL_BRIDGE_CASING),
                    Task.item(GTOBlocks.QUARK_PIPE),
                    Task.item(GTOBlocks.RESTRAINT_DEVICE),
                    Task.item(GTOBlocks.SPACETIME_ASSEMBLY_LINE_UNIT))
            .dependsOn(CONTAINMENT_FIELD_GENERATOR));

    public static final Quest PLASMA_CONDENSER = CHAPTER.quest("plasma_condenser", q -> q
            .at(0.78, 8.51)
            .task(Task.item(MultiBlockA.PLASMA_CONDENSER))
            .dependsOn(Q_479044A4));

    public static final Quest NEUTRON_VORTEX = CHAPTER.quest("neutron_vortex", q -> q
            .at(-1.59, 7.52)
            .task(Task.item(MultiBlockC.NEUTRON_VORTEX))
            .dependsOn(Q_479044A4));

    public static final Quest COMPONENT_ASSEMBLY_LINE = CHAPTER.quest("component_assembly_line", q -> q
            .at(-4.96, 7.21)
            .task(Task.item(MultiBlockA.COMPONENT_ASSEMBLY_LINE))
            .dependsOn(HAND_OF_ARACHNE));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UHV = CHAPTER.quest("magneto_resonatic_circuit_uhv", q -> q
            .at(-3.38, 8.25)
            .task(Task.item(GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UHV]))
            .dependsOn(HAND_OF_ARACHNE));

    public static final Quest COMPOUND_EXTREME_COOLING_UNIT = CHAPTER.quest("compound_extreme_cooling_unit", q -> q
            .at(-5.26, 9.09)
            .task(Task.item(MultiBlockF.COMPOUND_EXTREME_COOLING_UNIT))
            .dependsOn(COMPONENT_ASSEMBLY_LINE));

    public static final Quest LIFE_FORGE = CHAPTER.quest("life_forge", q -> q
            .at(-1.06, 9.36)
            .task(Task.item(MultiBlockH.LIFE_FORGE), Task.item(MultiBlockB.MICROORGANISM_MASTER), Task.item(MultiBlockH.TRANSLIMINAL_OASIS))
            .dependsOn(Q_479044A4));

    public static final Quest PLANETARY_GAS_COLLECTOR = CHAPTER.quest("planetary_gas_collector", q -> q
            .at(-2.92, 10.08)
            .task(
                    Task.item(SpaceMultiblock.PLANETARY_GAS_COLLECTOR),
                    Task.item(SpaceMultiblock.SPACE_3D_PRINTER),
                    Task.item(SpaceMultiblock.SPACE_STATION_ENERGY_CONVERSION_MODULE))
            .dependsOn(Q_479044A4));

    public static final Quest COMPREHENSIVE_TOMBARTHITE_PROCESSING_FACILITY = CHAPTER.quest("comprehensive_tombarthite_processing_facility", q -> q
            .at(-2.2, -5.84)
            .task(Task.item(MultiBlockA.ADVANCED_RARE_EARTH_CENTRIFUGAL))
            .dependsOn(LARGE_VOID_MINER));

    public static final Quest NYARLATHOTEPS_TENTACLE = CHAPTER.quest("nyarlathoteps_tentacle", q -> q
            .at(-6.59, 5.95)
            .task(Task.item(MultiBlockD.NYARLATHOTEPS_TENTACLE))
            .dependsOn(HAND_OF_ARACHNE));

    public static final Quest DYSON_CONTROL_TOROID = CHAPTER.quest("dyson_control_toroid", q -> q
            .at(2.15, 11.57)
            .task(Task.item(GTOBlocks.DYSON_CONTROL_TOROID), Task.item(GTOBlocks.DYSON_DEPLOYMENT_CORE), Task.item(GTOBlocks.DYSON_DEPLOYMENT_CASING))
            .dependsOn(DIMENSION_INJECTION_CASING));

    public static final Quest MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE = CHAPTER.quest("magnetic_confinement_dimensionality_shock_device", q -> q
            .at(0.55, 10.4)
            .task(Task.item(MultiBlockA.MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE))
            .dependsOn(DIMENSION_INJECTION_CASING));

    public static final Quest QUANTUM_FORCE_TRANSFORMER_COIL = CHAPTER.quest("quantum_force_transformer_coil", q -> q
            .at(0.16, 12.29)
            .task(
                    Task.item(GTOBlocks.QUANTUM_FORCE_TRANSFORMER_COIL),
                    Task.item(GTOBlocks.SPACETIME_CONTINUUM_RIPPER),
                    Task.item(GTOBlocks.SPACETIME_BENDING_CORE))
            .dependsOn(DIMENSION_INJECTION_CASING));

    public static final Quest DIMENSIONAL_STABILITY_CASING = CHAPTER.quest("dimensional_stability_casing", q -> q
            .at(2.33, 9.65)
            .task(
                    Task.item(GTOBlocks.DIMENSIONAL_STABILITY_CASING),
                    Task.item(GTOBlocks.DIMENSION_CREATION_CASING),
                    Task.item(GTOBlocks.SPACETIME_COMPRESSION_FIELD_GENERATOR))
            .dependsOn(DIMENSION_INJECTION_CASING));

    public static final Quest QUANTUM_FORCE_TRANSFORMER = CHAPTER.quest("quantum_force_transformer", q -> q
            .at(1.55, 13.7)
            .task(Task.item(MultiBlockA.QUANTUM_FORCE_TRANSFORMER))
            .dependsOn(QUANTUM_FORCE_TRANSFORMER_COIL));

    public static final Quest STELLAR_FORGE = CHAPTER.quest("stellar_forge", q -> q
            .at(3.94, 6.19)
            .task(Task.item(MultiBlockA.STELLAR_FORGE))
            .dependsOn(CONTAINMENT_FIELD_GENERATOR));

    public static final Quest BEDROCK_DRILLING_RIG = CHAPTER.quest("bedrock_drilling_rig", q -> q
            .at(-0.46, -6.62)
            .task(Task.item(MultiBlockD.BEDROCK_DRILLING_RIG))
            .dependsOn(LARGE_VOID_MINER));

    public static final Quest DIMENSIONAL_FOCUS_ENGRAVING_ARRAY = CHAPTER.quest("dimensional_focus_engraving_array", q -> q
            .at(-4.39, 0.81)
            .task(Task.item(MultiBlockE.DIMENSIONAL_FOCUS_ENGRAVING_ARRAY))
            .dependsOn(ENGRAVING_LASER_PLANT));

    public static final Quest OPTICAL_PROCESSING_CORE = CHAPTER.quest("optical_processing_core", q -> q
            .at(-7.44, -1.19)
            .task(Task.item(GTOItems.OPTICAL_PROCESSING_CORE))
            .dependsOn(BIOWARE_PROCESSING_CORE));

    public static final Quest NICH_EMPTY_COMPONENT = CHAPTER.quest("nich_empty_component", q -> q
            .at(-9.5, 3.19)
            .task(
                    Task.item(ExResearchMachines.NICH_EMPTY_COMPONENT),
                    Task.item(ExResearchMachines.NICH_COMPUTING_COMPONENTS),
                    Task.item(ExResearchMachines.NICH_COOLING_COMPONENTS),
                    Task.item(ExResearchMachines.NICH_BRIDGE_COMPONENT))
            .dependsOn(BIOWARE_PROCESSING_CORE));

    public static final Quest INTELLIGENT_SCANNING_ME_PROXY = CHAPTER.quest("intelligent_scanning_me_proxy", q -> q
            .at(3, -8)
            .task(Task.item(ExResearchMachines.INTELLIGENT_SCANNING_ME_PROXY), Task.item(ExResearchMachines.INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM))
            .dependsOn(OBJECT_HOLDER));

    public static final Quest MATTER_FABRICATOR = CHAPTER.quest("matter_fabricator", q -> q
            .at(-4.47, -7.54)
            .task(Task.item(MultiBlockA.MATTER_FABRICATOR))
            .dependsOn(DECAY_HASTENER));

    public static final Quest BLACK_HOLE_DATA_ACCESS_HATCH = CHAPTER.quest("black_hole_data_access_hatch", q -> q
            .at(-10.01, 1.29)
            .task(Task.item(ExResearchMachines.BLACK_HOLE_DATA_ACCESS_HATCH))
            .dependsOn(BIO_DATA_ACCESS_HATCH));

    public static final Quest UHV_FUSION_REACTOR = CHAPTER.quest("uhv_fusion_reactor", q -> q
            .at(-9.16, -5.85)
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UHV]))
            .dependsOn(UV_FUSION_REACTOR));

    public static final Quest LEPTONIC_CHARGE = CHAPTER.quest("leptonic_charge", q -> q
            .at(6.35, 7.9)
            .task(Task.item(GTOBlocks.LEPTONIC_CHARGE))
            .dependsOn(STELLAR_FORGE));

    public static final Quest ADVANCED_INFINITE_DRILLER = CHAPTER.quest("advanced_infinite_driller", q -> q
            .at(-1.93, -7.89)
            .task(Task.item(MultiBlockC.ADVANCED_INFINITE_DRILLER), Task.item(MultiBlockC.PLANET_CORE_DRILLING))
            .dependsOn(LARGE_VOID_MINER));

    public static final Quest ENTROPY_FLUX_ENGINE = CHAPTER.quest("entropy_flux_engine", q -> q
            .at(-5.98, -8.75)
            .task(Task.item(MultiBlockH.ENTROPY_FLUX_ENGINE))
            .dependsOn(DECAY_HASTENER));

    public static final Quest GWCA_EMPTY_COMPONENT = CHAPTER.quest("gwca_empty_component", q -> q
            .at(6.84, -9.76)
            .task(
                    Task.item(ExResearchMachines.GWCA_EMPTY_COMPONENT),
                    Task.item(ExResearchMachines.GWCA_COMPUTING_COMPONENTS),
                    Task.item(ExResearchMachines.GWCA_COOLING_COMPONENTS))
            .dependsOn(SUPERCOMPUTING_CENTER));

    public static final Quest VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH = CHAPTER.quest("virtual_universe_data_access_hatch", q -> q
            .at(-11.91, 1.54)
            .task(Task.item(ExResearchMachines.VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH))
            .dependsOn(BLACK_HOLE_DATA_ACCESS_HATCH));

    public static final Quest UEV_FUSION_REACTOR = CHAPTER.quest("uev_fusion_reactor", q -> q
            .at(-10.53, -7.16)
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UEV]))
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest SPS_CRAFTING = CHAPTER.quest("sps_crafting", q -> q
            .at(-9.96, -4.07)
            .task(Task.item(MultiBlockA.SPS_CRAFTING))
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest ATOMIC_ENERGY_EXCITATION_PLANT = CHAPTER.quest("atomic_energy_excitation_plant", q -> q
            .at(-12.09, -3.54)
            .task(Task.item(MultiBlockA.ATOMIC_ENERGY_EXCITATION_PLANT))
            .dependsOn(SPS_CRAFTING));

    public static final Quest ADVANCED_SPS_CRAFTING = CHAPTER.quest("advanced_sps_crafting", q -> q
            .at(-8.61, -2.65)
            .task(Task.item(MultiBlockA.ADVANCED_SPS_CRAFTING), Task.item(GTOItems.TIME_DILATION_CONTAINMENT_UNIT))
            .dependsOn(SPS_CRAFTING));

    public static final Quest MASS_FABRICATOR = CHAPTER.quest("mass_fabricator", q -> q
            .at(-11.38, -5.4)
            .task(Task.item(MultiBlockA.MASS_FABRICATOR))
            .dependsOn(SPS_CRAFTING));

    public static final Quest AWAKENED_DRACONIUM_SUPERCONDUCTOR_BASE = CHAPTER.quest("awakened_draconium_superconductor_base", q -> q
            .at(-6.7, -2.96)
            .task(
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.AwakenedDraconium),
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.Echoite),
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.Enderite),
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.Legendarium))
            .dependsOn(SPS_CRAFTING, ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_SUPERCONDUCTOR_BASE));

    public static final Quest EXOTIC_PROCESSING_CORE = CHAPTER.quest("exotic_processing_core", q -> q
            .at(-9.3, -0.74)
            .task(Task.item(GTOItems.EXOTIC_PROCESSING_CORE))
            .dependsOn(OPTICAL_PROCESSING_CORE, ADVANCED_SPS_CRAFTING));

    public static final Quest COSMIC_PROCESSING_CORE = CHAPTER.quest("cosmic_processing_core", q -> q
            .at(-11.14, -0.25)
            .task(Task.item(GTOItems.COSMIC_PROCESSING_CORE))
            .dependsOn(EXOTIC_PROCESSING_CORE));

    public static final Quest EXTREMELY_DURABLE_PLASMA_CELL = CHAPTER.quest("extremely_durable_plasma_cell", q -> q
            .at(-1.5, 11.3)
            .task(Task.item(GTOItems.EXTREMELY_DURABLE_PLASMA_CELL))
            .dependsOn(PLASMA_CONDENSER));

    public static final Quest HYPERDIMENSIONAL_PLASMA_FUSION_CORE = CHAPTER.quest("hyperdimensional_plasma_fusion_core", q -> q
            .at(4.4, 8.12)
            .task(Task.item(MultiBlockD.HYPERDIMENSIONAL_PLASMA_FUSION_CORE))
            .dependsOn(STELLAR_FORGE));

    public static final Quest STAR_ULTIMATE_MATERIAL_FORGE_FACTORY = CHAPTER.quest("star_ultimate_material_forge_factory", q -> q
            .at(3.98, 10.6)
            .task(Task.item(MultiBlockE.STAR_ULTIMATE_MATERIAL_FORGE_FACTORY))
            .dependsOn(STELLAR_FORGE));

    public static final Quest INFINITY_COIL_BLOCK = CHAPTER.quest("infinity_coil_block", q -> q
            .at(5.71, 9.72)
            .task(Task.item(GTOBlocks.INFINITY_COIL_BLOCK), Task.item(GTOBlocks.HYPOGEN_COIL_BLOCK), Task.item(GTOBlocks.ETERNITY_COIL_BLOCK))
            .dependsOn(HYPERDIMENSIONAL_PLASMA_FUSION_CORE));

    public static final Quest QUANTUM_CHROMODYNAMIC_CHARGE = CHAPTER.quest("quantum_chromodynamic_charge", q -> q
            .at(7.61, 9.34)
            .task(Task.item(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE))
            .dependsOn(LEPTONIC_CHARGE));

    public static final Quest ADVANCED_MASS_FABRICATOR = CHAPTER.quest("advanced_mass_fabricator", q -> q
            .at(-11.97, -8.36)
            .task(Task.item(MultiBlockA.ADVANCED_MASS_FABRICATOR))
            .dependsOn(MASS_FABRICATOR));

    public static final Quest ELEMENT_COPYING = CHAPTER.quest("element_copying", q -> q
            .at(-12.81, -6.67)
            .task(Task.item(MultiBlockA.ELEMENT_COPYING))
            .dependsOn(MASS_FABRICATOR));

    public static final Quest MANIFOLD_OSCILLATORY_POWER_CELL = CHAPTER.quest("manifold_oscillatory_power_cell", q -> q
            .at(-10.45, -2.24)
            .task(Task.item(GTOItems.MANIFOLD_OSCILLATORY_POWER_CELL))
            .dependsOn(EXOTIC_PROCESSING_CORE));

    public static final Quest SUPRACAUSAL_PROCESSING_CORE = CHAPTER.quest("supracausal_processing_core", q -> q
            .at(-12.75, -1.23)
            .task(Task.item(GTOItems.SUPRACAUSAL_PROCESSING_CORE))
            .dependsOn(COSMIC_PROCESSING_CORE));

    public static final Quest WYVERN_CORE = CHAPTER.quest("wyvern_core", q -> q
            .at(-14.36, -0.26)
            .task(Task.item(GTOItems.WYVERN_CORE))
            .dependsOn(SUPRACAUSAL_PROCESSING_CORE));

    public static final Quest EYE_OF_HARMONY = CHAPTER.quest("eye_of_harmony", q -> q
            .at(-13.8, 1.64)
            .task(Task.item(MultiBlockD.EYE_OF_HARMONY))
            .dependsOn(VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH));

    public static final Quest AWAKENED_CORE = CHAPTER.quest("awakened_core", q -> q
            .at(-16.24, -0.41)
            .task(Task.item(GTOItems.AWAKENED_CORE), Task.item(GTOItems.DRACONIC_ENERGY_CORE))
            .dependsOn(WYVERN_CORE));

    public static final Quest HYPERDIMENSIONAL_DRONE = CHAPTER.quest("hyperdimensional_drone", q -> q
            .at(-6.83, 7.84)
            .task(Task.item(GTOItems.HYPERDIMENSIONAL_DRONE))
            .dependsOn(NYARLATHOTEPS_TENTACLE));

    public static final Quest SUPRACHRONAL_CIRCUIT_ULV = CHAPTER.quest("suprachronal_circuit_ulv", q -> q
            .at(-7.97, 9.38)
            .task(
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.ULV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.MV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.HV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.EV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.IV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LuV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.ZPM]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UHV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UEV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UIV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.UXV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.OpV]),
                    Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.MAX]))
            .dependsOn(HYPERDIMENSIONAL_DRONE));

    public static final Quest CHAOTIC_CORE = CHAPTER.quest("chaotic_core", q -> q
            .at(-18.14, -0.38)
            .task(Task.item(GTOItems.CHAOTIC_CORE), Task.item(GTOItems.CHAOTIC_ENERGY_CORE))
            .dependsOn(AWAKENED_CORE));

    public static final Quest CREATE_AGGREGATIONE_CORE = CHAPTER.quest("create_aggregatione_core", q -> q
            .at(-20.11, -0.38)
            .task(
                    Task.item(GTOBlocks.CREATE_AGGREGATIONE_CORE),
                    Task.item(GTOItems.CREATE_ULTIMATE_BATTERY),
                    Task.item(GTOItems.SUPRACHRONAL_MAINFRAME_COMPLEX))
            .dependsOn(CHAOTIC_CORE));

    public static final Quest LUV_ENERGY_INPUT_HATCH = CHAPTER.quest("luv_energy_input_hatch", q -> q
            .at(3.9, 0.58)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.LuV]),
                    Task.item(GTOBlocks.LUV_WIRELESS_ENERGY_UNIT)));

    public static final Quest LAPOTRONIC_ENERGY_ORB_CLUSTER = CHAPTER.quest("lapotronic_energy_orb_cluster", q -> q
            .at(2.84, 2.19)
            .task(Task.item(GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER), Task.item(GTItems.ENERGY_MODULE), Task.item(GTItems.ENERGY_CLUSTER)));

    public static final Quest ZPM_ENERGY_INPUT_HATCH = CHAPTER.quest("zpm_energy_input_hatch", q -> q
            .at(5.5, 1.5)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.ZPM]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.ZPM]),
                    Task.item(GTOBlocks.ZPM_WIRELESS_ENERGY_UNIT))
            .dependsOn(LUV_ENERGY_INPUT_HATCH));

    public static final Quest ENERGY_DATA_HOLDER = CHAPTER.quest("energy_data_holder", q -> q
            .at(5.96, 4.16)
            .task(Task.item(ExResearchMachines.ENERGY_DATA_HOLDER))
            .dependsOn(LAPOTRONIC_ENERGY_ORB_CLUSTER));

    public static final Quest BIO_OSCILLATION_GENERATOR = CHAPTER.quest("bio_oscillation_generator", q -> q
            .at(-6.31, 0.34)
            .task(Task.item(GeneratorMultiblock.BIO_OSCILLATION_GENERATOR), Task.item(GeneratorMultiblock.BIO_OSCILLATION_ELECTRIC_STIMULATOR))
            .dependsOn(BIOWARE_PROCESSING_CORE));

    public static final Quest LARGE_NAQUADAH_REACTOR = CHAPTER.quest("large_naquadah_reactor", q -> q
            .at(-14.07, -3.01)
            .task(Task.item(GeneratorMultiblock.LARGE_NAQUADAH_REACTOR)));

    public static final Quest MAX_BATTERY = CHAPTER.quest("max_battery", q -> q
            .at(4.07, 4.22)
            .task(Task.item(GTItems.ULTIMATE_BATTERY))
            .dependsOn(LAPOTRONIC_ENERGY_ORB_CLUSTER));

    public static final Quest UV_ENERGY_INPUT_HATCH = CHAPTER.quest("uv_energy_input_hatch", q -> q
            .at(7, 2.5)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.UV]),
                    Task.item(GTOBlocks.UV_WIRELESS_ENERGY_UNIT))
            .dependsOn(ZPM_ENERGY_INPUT_HATCH));

    public static final Quest UHV_ENERGY_INPUT_HATCH = CHAPTER.quest("uhv_energy_input_hatch", q -> q
            .at(8.5, 3.5)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UHV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.UHV]),
                    Task.item(GTOBlocks.UHV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UV_ENERGY_INPUT_HATCH));

    public static final Quest REALLY_MAX_BATTERY = CHAPTER.quest("really_max_battery", q -> q
            .at(5.84, 6.06)
            .task(Task.item(GTOItems.REALLY_MAX_BATTERY))
            .dependsOn(MAX_BATTERY));

    public static final Quest UEV_ENERGY_INPUT_HATCH = CHAPTER.quest("uev_energy_input_hatch", q -> q
            .at(9.76, 4.31)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UEV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.UEV]),
                    Task.item(GTOBlocks.UEV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UHV_ENERGY_INPUT_HATCH));

    public static final Quest UIV_ENERGY_INPUT_HATCH = CHAPTER.quest("uiv_energy_input_hatch", q -> q
            .at(11.36, 5.36)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UIV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.UIV]),
                    Task.item(GTOBlocks.UIV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UEV_ENERGY_INPUT_HATCH));

    public static final Quest DYSON_SPHERE_LAUNCH_SILO = CHAPTER.quest("dyson_sphere_launch_silo", q -> q
            .at(3.36, 13.03)
            .task(Task.item(GeneratorMultiblock.DYSON_SPHERE_LAUNCH_SILO), Task.item(GeneratorMultiblock.DYSON_SPHERE_RECEIVING_STATION))
            .dependsOn(DYSON_CONTROL_TOROID));

    public static final Quest HYPER_REACTOR = CHAPTER.quest("hyper_reactor", q -> q
            .at(-13.62, -4.85)
            .task(Task.item(GeneratorMultiblock.HYPER_REACTOR))
            .dependsOn(LARGE_NAQUADAH_REACTOR, ATOMIC_ENERGY_EXCITATION_PLANT));

    public static final Quest ADVANCED_HYPER_REACTOR = CHAPTER.quest("advanced_hyper_reactor", q -> q
            .at(-15.36, -5.59)
            .task(Task.item(GeneratorMultiblock.ADVANCED_HYPER_REACTOR))
            .dependsOn(HYPER_REACTOR));

    public static final Quest TRANSCENDENT_MAX_BATTERY = CHAPTER.quest("transcendent_max_battery", q -> q
            .at(7.74, 6.38)
            .task(Task.item(GTOItems.TRANSCENDENT_MAX_BATTERY))
            .dependsOn(REALLY_MAX_BATTERY));

    public static final Quest UXV_ENERGY_INPUT_HATCH = CHAPTER.quest("uxv_energy_input_hatch", q -> q
            .at(13.12, 6.11)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UXV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.UXV]),
                    Task.item(GTOBlocks.UXV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UIV_ENERGY_INPUT_HATCH));

    public static final Quest ANNIHILATE_GENERATOR = CHAPTER.quest("annihilate_generator", q -> q
            .at(-13.73, -9.25)
            .task(Task.item(GeneratorMultiblock.ANNIHILATE_GENERATOR))
            .dependsOn(ADVANCED_MASS_FABRICATOR));

    public static final Quest EXTREMELY_MAX_BATTERY = CHAPTER.quest("extremely_max_battery", q -> q
            .at(8.88, 7.88)
            .task(Task.item(GTOItems.EXTREMELY_MAX_BATTERY))
            .dependsOn(TRANSCENDENT_MAX_BATTERY));

    public static final Quest OPV_ENERGY_INPUT_HATCH = CHAPTER.quest("opv_energy_input_hatch", q -> q
            .at(14.85, 6.92)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.OpV]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.OpV]),
                    Task.item(GTOBlocks.OPV_WIRELESS_ENERGY_UNIT))
            .dependsOn(UXV_ENERGY_INPUT_HATCH));

    public static final Quest INSANELY_MAX_BATTERY = CHAPTER.quest("insanely_max_battery", q -> q
            .at(9.81, 9.55)
            .task(Task.item(GTOItems.INSANELY_MAX_BATTERY))
            .dependsOn(EXTREMELY_MAX_BATTERY));

    public static final Quest MEGA_MAX_BATTERY = CHAPTER.quest("mega_max_battery", q -> q
            .at(11.24, 10.84)
            .task(Task.item(GTOItems.MEGA_MAX_BATTERY))
            .dependsOn(INSANELY_MAX_BATTERY));

    public static final Quest MAX_ENERGY_INPUT_HATCH = CHAPTER.quest("max_energy_input_hatch", q -> q
            .at(16.57, 7.74)
            .task(
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.MAX]),
                    Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.MAX]),
                    Task.item(GTOBlocks.MAX_WIRELESS_ENERGY_UNIT))
            .dependsOn(OPV_ENERGY_INPUT_HATCH));

    public static final Quest CARBON_NANITES = CHAPTER.quest("carbon_nanites", q -> q
            .at(-9.19, 12.68)
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Carbon), Task.item(MultiBlockD.NANO_FORGE))
            .dependsOn(CHEMICAL_PLANT));

    public static final Quest CIRCUIT_ASSEMBLY_LINE = CHAPTER.quest("circuit_assembly_line", q -> q
            .at(-9.74, 14.55)
            .task(Task.item(MultiBlockD.CIRCUIT_ASSEMBLY_LINE), Task.item(MultiBlockD.PCB_FACTORY))
            .dependsOn(CARBON_NANITES));

    public static final Quest BIOENGINEERING_MODULE = CHAPTER.quest("bioengineering_module", q -> q
            .at(-11.07, 11.61)
            .task(
                    Task.item(MultiBlockC.BIOENGINEERING_MODULE),
                    Task.item(MultiBlockC.NANITES_INTEGRATED_PROCESSING_CENTER),
                    Task.item(MultiBlockC.ORE_EXTRACTION_MODULE),
                    Task.item(MultiBlockC.POLYMER_TWISTING_MODULE))
            .dependsOn(CARBON_NANITES));

    public static final Quest SWARM_CORE = CHAPTER.quest("swarm_core", q -> q
            .at(-11.33, 13.49)
            .task(Task.item(MultiBlockF.SWARM_CORE))
            .dependsOn(CARBON_NANITES));

    public static final Quest TIER_7_ROCKET = CHAPTER.quest("tier_7_rocket", q -> q
            .at(0.69, -3.33)
            .task(Task.item(ModItems.TIER_7_ROCKET.get())));

    public static final Quest SPACE_ELEVATOR = CHAPTER.quest("space_elevator", q -> q
            .at(0.78, -5.21)
            .task(
                    Task.item(MultiBlockD.SPACE_ELEVATOR),
                    Task.item(MultiBlockD.ASSEMBLER_MODULE),
                    Task.item(MultiBlockD.RESOURCE_COLLECTION_MODULE),
                    Task.item(MultiBlockD.ENGINEERED_DATA_MODULE),
                    Task.item(ExResearchMachines.INTERSTELLAR_ENGINEERING_DATA_HOLDER),
                    Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_1))
            .dependsOn(TIER_7_ROCKET));

    public static final Quest SPACE_ELEVATOR_POWER_MODULE_2 = CHAPTER.quest("space_elevator_power_module_2", q -> q
            .at(1.57, -7.03)
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_2))
            .dependsOn(SPACE_ELEVATOR));

    public static final Quest SPACE_ELEVATOR_POWER_MODULE_3 = CHAPTER.quest("space_elevator_power_module_3", q -> q
            .at(0.2, -8.39)
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_3))
            .dependsOn(SPACE_ELEVATOR_POWER_MODULE_2));

    public static final Quest SPACE_ELEVATOR_POWER_MODULE_4 = CHAPTER.quest("space_elevator_power_module_4", q -> q
            .at(0.4, -10.26)
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_4))
            .dependsOn(SPACE_ELEVATOR_POWER_MODULE_3));

    public static final Quest ROAD_OF_HEAVEN = CHAPTER.quest("road_of_heaven", q -> q
            .at(0.38, -12.18)
            .task(Task.item(MultiBlockB.ROAD_OF_HEAVEN), Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_5))
            .dependsOn(SPACE_ELEVATOR_POWER_MODULE_4));

    public static final Quest SPACE_PROBE_SURFACE_RECEPTION = CHAPTER.quest("space_probe_surface_reception", q -> q
            .at(0.38, -14.09)
            .task(Task.item(MultiBlockD.SPACE_PROBE_SURFACE_RECEPTION))
            .dependsOn(ROAD_OF_HEAVEN));

    public static final Quest Q_2D33C6F4 = CHAPTER.quest("q_2d33c6f4", q -> q
            .at(-11, 6)
            .desc(d -> d
                    .line("此章节为EMI中展示的科技树的研究奖励一览，隐去了当前暂未解锁的科技节点。")
                    .line()
                    .line("完成某个任务可以查看与之相关联的后续科技节点的奖励。"))
            .task(Task.checkmark()));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -11.5, 5.5, 2, 2)));
    }
}
