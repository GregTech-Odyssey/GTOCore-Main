package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockE;
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

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewUxvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_uxv", c -> c
            .title("15. 极顶压时代(UXV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.UXV]));

    public static final Quest HASTELLOY_K243 = CHAPTER.quest("hastelloy_k243", q -> q
            .at(-2, 2)
            .title("哈斯特洛依合金-K243")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HastelloyK243, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.HastelloyX78, 10)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest ASTRAL_TITANIUM = CHAPTER.quest("astral_titanium", q -> q
            .at(-2, -2)
            .title("星体钛")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AstralTitanium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Titanium, 32)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final QuestLink ASTRAL_TITANIUM_COPY_1 = CHAPTER.copy(ASTRAL_TITANIUM, 4, 4);

    public static final Quest TITAN_PRECISION_STEEL = CHAPTER.quest("titan_precision_steel", q -> q
            .at(2, 4)
            .title("泰坦精钢")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TitanPrecisionSteel, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitanSteel, 12)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest VIBRAMANTIUM = CHAPTER.quest("vibramantium", q -> q
            .at(8, 2)
            .title("艾德曼振金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Vibramantium, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.Adamantium, 6)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest PERIODICIUM = CHAPTER.quest("periodicium", q -> q
            .at(10, 6)
            .title("錭錤錶")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Periodicium, 40))
            .scaledReward(TagPrefix.ingot, GTOMaterials.Periodicium, 4)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final QuestLink PERIODICIUM_COPY_1 = CHAPTER.copy(PERIODICIUM, -8, 14);

    public static final Quest TAIRITSU = CHAPTER.quest("tairitsu", q -> q
            .at(0, -2)
            .title("对立合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Tairitsu, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 16)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest HIKARIUM = CHAPTER.quest("hikarium", q -> q
            .at(14, 0)
            .title("光素")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Hikarium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Silver, 16)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest TIME_DILATION_UNIT = CHAPTER.quest("time_dilation_containment_unit", q -> q
            .at(8, 16)
            .title("时间膨胀密闭单元")
            .task(Task.item(GTOItems.TIME_DILATION_CONTAINMENT_UNIT, 8))
            .scaledReward(TagPrefix.plate, GTOMaterials.DegenerateRhenium, 2)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final QuestLink TIME_DILATION_UNIT_COPY_1 = CHAPTER.copy(TIME_DILATION_UNIT, -6, 0);

    public static final Quest DRAGON_CELLS = CHAPTER.quest("dragon_cells", q -> q
            .at(8, 4)
            .title("龙细胞")
            .icon(GTOItems.DRAGON_CELLS)
            .task(Task.item(GTOItems.DRAGON_STEM_CELLS), Task.item(GTOItems.DRAGON_CELLS, 16))
            .scaledReward(GTItems.STEM_CELLS, 16)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest DRAGON_ELEMENT = CHAPTER.quest("dragon_element", q -> q
            .at(8, 6)
            .title("龙素")
            .icon(GTOMaterials.DragonElement.getBucket())
            .task(Task.item(GTOMaterials.DragonElement.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.DRAGON_CELLS, 4)
            .dependsOn(DRAGON_CELLS));

    public static final Quest POWER_MODULE_3 = CHAPTER.quest("space_elevator_power_module_3", q -> q
            .at(-6, 6)
            .title("太空电梯动力模块 MK III")
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_3, 16))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UEV, 2)
            .dependsOn(NewUivChapter.UXV_HULL, NewUivChapter.TECH_SPACE_ELEVATOR3));

    public static final QuestLink POWER_MODULE_3_COPY_1 = CHAPTER.copy(POWER_MODULE_3, 6, 18);

    public static final Quest POWER_MODULE_4 = CHAPTER.quest("space_elevator_power_module_4", q -> q
            .at(-6, 4)
            .title("太空电梯动力模块 MK IV")
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_4, 16))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UIV, 2)
            .dependsOn(POWER_MODULE_3, NewUivChapter.TECH_SPACE_ELEVATOR4));

    public static final QuestLink POWER_MODULE_4_COPY_1 = CHAPTER.copy(POWER_MODULE_4, -6, 16);

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE6 = CHAPTER.quest("tech_component_in_assembly_line6", q -> q
            .at(-2, 0)
            .title("装配线进阶部件VI研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuxv))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL, NewUivChapter.TECH_COMPONENT_IN_ASSEMBLY_LINE5));

    public static final QuestLink TECH_COMPONENT_IN_ASSEMBLY_LINE6_COPY_1 = CHAPTER.copy(TECH_COMPONENT_IN_ASSEMBLY_LINE6, 14, 14);

    public static final Quest UXV_MOTOR = CHAPTER.quest("uxv_electric_motor", q -> q
            .at(-4, -2)
            .title("UXV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_UXV, 4))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.AttunedTengam, 1)
            .dependsOn(HASTELLOY_K243, NewUivChapter.NAQUADRIATIC_TARANIUM, ASTRAL_TITANIUM, TECH_COMPONENT_IN_ASSEMBLY_LINE6));

    public static final QuestLink UXV_MOTOR_COPY_1 = CHAPTER.copy(UXV_MOTOR, -2, 4);

    public static final Quest UXV_PUMP = CHAPTER.quest("uxv_electric_pump", q -> q
            .at(0, -4)
            .title("UXV 电泵")
            .task(Task.item(GTItems.ELECTRIC_PUMP_UXV, 2))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UXV, 1)
            .dependsOn(UXV_MOTOR, NewUivChapter.HEAVY_QUARK_DEGENERATE_MATTER));

    public static final Quest UXV_VOLTAGE_COIL = CHAPTER.quest("uxv_voltage_coil", q -> q
            .at(4, -2)
            .title("极顶压线圈")
            .task(Task.item(GTOItems.UXV_VOLTAGE_COIL, 4))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.NaquadriaticTaranium, 16)
            .dependsOn(NewUivChapter.NAQUADRIATIC_TARANIUM));

    public static final Quest TECH_ENERGY_IO_UXV = CHAPTER.quest("tech_energy_io_uxv", q -> q
            .at(2, 2)
            .title("高压能量输入输出VII研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UXV]))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL, NewUivChapter.TECH_ENERGY_IO_UIV));

    public static final Quest UXV_ENERGY_HATCH = CHAPTER.quest("uxv_energy_input_hatch", q -> q
            .at(4, 0)
            .milestone()
            .title("UXV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UXV], 2))
            .scaledReward(GTOItems.UXV_VOLTAGE_COIL, 2)
            .dependsOn(UXV_PUMP, UXV_VOLTAGE_COIL, TECH_ENERGY_IO_UXV));

    public static final Quest UXV_EMITTER_SENSOR = CHAPTER.quest("uxv_emitter_and_sensor", q -> q
            .at(0, 4)
            .title("UXV 发射器与传感器")
            .icon(GTItems.SENSOR_UXV)
            .task(Task.item(GTItems.EMITTER_UXV, 8), Task.item(GTItems.SENSOR_UXV, 8))
            .scaledReward(TagPrefix.foil, GTOMaterials.TitanPrecisionSteel, 32)
            .dependsOn(UXV_MOTOR_COPY_1, TITAN_PRECISION_STEEL));

    public static final Quest HEAVY_LEPTON_MIXTURE = CHAPTER.quest("heavy_lepton_mixture", q -> q
            .at(2, 6)
            .title("重轻子混合物")
            .icon(GTOMaterials.HeavyLeptonMixture.getBucket())
            .task(Task.item(GTOMaterials.HeavyLeptonMixture.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOBlocks.NAQUADRIA_CHARGE, 4)
            .dependsOn(NewUivChapter.SPACE_PROBE_PRODUCT));

    public static final Quest NEUTRON_PLASMA_CELL = CHAPTER.quest("neutron_plasma_containment_cell", q -> q
            .at(4, 6)
            .title("中子等离子体密闭容器")
            .task(Task.item(GTOItems.NEUTRON_PLASMA_CONTAINMENT_CELL, 8))
            .scaledReward(GTOItems.PLASMA_CONTAINMENT_CELL, 4)
            .dependsOn(HEAVY_LEPTON_MIXTURE));

    public static final QuestLink NEUTRON_PLASMA_CELL_COPY_1 = CHAPTER.copy(NEUTRON_PLASMA_CELL, 16, 2);

    public static final Quest NEUTRONIUM_SPHERES = CHAPTER.quest("triplet_neutronium_sphere", q -> q
            .at(14, 2)
            .title("三连中子素球体")
            .icon(GTOItems.CHARGED_TRIPLET_NEUTRONIUM_SPHERE)
            .task(Task.item(GTOItems.TRIPLET_NEUTRONIUM_SPHERE, 16), Task.item(GTOItems.CHARGED_TRIPLET_NEUTRONIUM_SPHERE, 8))
            .scaledReward(GTOItems.NEUTRON_PLASMA_CONTAINMENT_CELL, 2)
            .dependsOn(NEUTRON_PLASMA_CELL_COPY_1));

    public static final QuestLink NEUTRONIUM_SPHERES_COPY_1 = CHAPTER.copy(NEUTRONIUM_SPHERES, 8, 18);

    public static final QuestLink NEUTRONIUM_SPHERES_COPY_2 = CHAPTER.copy(NEUTRONIUM_SPHERES, -8, 2);

    public static final Quest CHDPM = CHAPTER.quest("contained_high_density_protonic_matter", q -> q
            .at(6, 16)
            .title("遏制高密度质子物质")
            .task(Task.item(GTOItems.CONTAINED_HIGH_DENSITY_PROTONIC_MATTER, 4))
            .scaledReward(GTOItems.TIME_DILATION_CONTAINMENT_UNIT, 2)
            .dependsOn(NewUivChapter.LEPTONIC_CHARGE, TIME_DILATION_UNIT, NEUTRONIUM_SPHERES_COPY_1, NewUivChapter.ADVANCED_STELLAR_CASING));

    public static final Quest FREE_PROTON_GAS = CHAPTER.quest("free_proton_gas", q -> q
            .at(6, 12)
            .title("高密度自由质子气")
            .icon(GTOMaterials.FreeProtonGas.getBucket())
            .task(Task.item(GTOMaterials.FreeProtonGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.CONTAINED_HIGH_DENSITY_PROTONIC_MATTER, 1)
            .dependsOn(CHDPM));

    public static final Quest LEGENDARIUM = CHAPTER.quest("legendarium", q -> q
            .at(6, 4)
            .title("传奇合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Legendarium, 32))
            .scaledReward(GTOItems.NEUTRON_PLASMA_CONTAINMENT_CELL, 2)
            .dependsOn(NewUivChapter.LEPTONIC_CHARGE, NEUTRON_PLASMA_CELL, NewUivChapter.ADVANCED_STELLAR_CASING));

    public static final QuestLink LEGENDARIUM_COPY_1 = CHAPTER.copy(LEGENDARIUM, 16, 12);

    public static final QuestLink LEGENDARIUM_COPY_2 = CHAPTER.copy(LEGENDARIUM, 16, 8);

    public static final QuestLink LEGENDARIUM_COPY_3 = CHAPTER.copy(LEGENDARIUM, -8, 16);

    public static final Quest UXV_ARM_FIELD = CHAPTER.quest("uxv_robot_arm_and_field_generator", q -> q
            .at(14, 12)
            .title("UXV 机械臂与力场发生器")
            .icon(GTItems.FIELD_GENERATOR_UXV)
            .task(Task.item(GTItems.ROBOT_ARM_UXV, 8), Task.item(GTItems.FIELD_GENERATOR_UXV, 8))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.Legendarium, 32)
            .dependsOn(LEGENDARIUM_COPY_1, TECH_COMPONENT_IN_ASSEMBLY_LINE6_COPY_1));

    public static final Quest CELESTIAL_TUNGSTEN = CHAPTER.quest("celestial_tungsten", q -> q
            .at(4, 2)
            .title("天体钨")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.CelestialTungsten, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 32)
            .dependsOn(TITAN_PRECISION_STEEL, ASTRAL_TITANIUM_COPY_1));

    public static final Quest ULTIMATE_STELLAR_CASING = CHAPTER.quest("ultimate_stellar_containment_casing", q -> q
            .at(6, 2)
            .title("终极恒星热力容器")
            .task(Task.item(GTOBlocks.ULTIMATE_STELLAR_CONTAINMENT_CASING, 64))
            .scaledReward(GTItems.FIELD_GENERATOR_UEV, 2)
            .dependsOn(VIBRAMANTIUM, LEGENDARIUM, NewUivChapter.HEAVY_QUARK_DEGENERATE_MATTER, CELESTIAL_TUNGSTEN));

    public static final Quest TECH_STELLAR_ULTIMATE_FORGE = CHAPTER.quest("tech_stellar_ultimate_forge", q -> q
            .at(8, -2)
            .title("恒星终极锻造技术")
            .task(Task.techNode(BaseNodes.StellarUltimateForge))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest UMF_FACTORY = CHAPTER.quest("star_ultimate_material_forge_factory", q -> q
            .at(8, 0)
            .milestone()
            .title("恒星终极物质锻造工厂")
            .task(Task.item(MultiBlockE.STAR_ULTIMATE_MATERIAL_FORGE_FACTORY))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Vibramantium, 8)
            .dependsOn(ULTIMATE_STELLAR_CASING, TECH_STELLAR_ULTIMATE_FORGE));

    public static final Quest PROTO_MATTER = CHAPTER.quest("proto_matter", q -> q
            .at(14, 4)
            .title("元始物质")
            .task(Task.item(GTOItems.PROTO_MATTER, 16))
            .scaledReward(GTOItems.TRIPLET_NEUTRONIUM_SPHERE, 4)
            .dependsOn(UMF_FACTORY, NEUTRONIUM_SPHERES, UXV_ENERGY_HATCH));

    public static final QuestLink PROTO_MATTER_COPY_1 = CHAPTER.copy(PROTO_MATTER, 2, 10);

    public static final Quest CORPOREAL_MATTER = CHAPTER.quest("corporeal_matter", q -> q
            .at(4, 10)
            .title("凡尘物质")
            .task(Task.item(GTOItems.CORPOREAL_MATTER, 16))
            .scaledReward(GTOItems.PROTO_MATTER, 4)
            .dependsOn(PROTO_MATTER_COPY_1));

    public static final QuestLink CORPOREAL_MATTER_COPY_1 = CHAPTER.copy(CORPOREAL_MATTER, -4, 16);

    public static final Quest CRYSTAL_MATRIX_PLASMA = CHAPTER.quest("crystal_matrix_plasma", q -> q
            .at(4, 12)
            .title("水晶矩阵等离子")
            .icon(GTOMaterials.CrystalMatrix.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.CrystalMatrix.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.CORPOREAL_MATTER, 4)
            .dependsOn(CORPOREAL_MATTER, FREE_PROTON_GAS));

    public static final Quest CRYSTAL_MATRIX = CHAPTER.quest("crystal_matrix_ingot", q -> q
            .at(6, 10)
            .title("水晶矩阵")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.CrystalMatrix, 16))
            .scaledReward(GTOItems.PLASMA_CONTAINMENT_CELL, 4)
            .dependsOn(CRYSTAL_MATRIX_PLASMA, NewUivChapter.ENDERIUM_NANITES));

    public static final QuestLink CRYSTAL_MATRIX_COPY_1 = CHAPTER.copy(CRYSTAL_MATRIX, -8, 8);

    public static final Quest CRYSTAL_MATRIX_CABLE = CHAPTER.quest("crystal_matrix_cable", q -> q
            .at(-6, 8)
            .title("1x 水晶矩阵线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTOMaterials.CrystalMatrix, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.CrystalMatrix, 2)
            .dependsOn(CRYSTAL_MATRIX_COPY_1));

    public static final Quest STARMETAL = CHAPTER.quest("starmetal", q -> q
            .at(8, 12)
            .title("星辉")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Starmetal, 32))
            .scaledReward(GTOBlocks.LEPTONIC_CHARGE, 2)
            .dependsOn(FREE_PROTON_GAS));

    public static final QuestLink STARMETAL_COPY_1 = CHAPTER.copy(STARMETAL, -8, 18);

    public static final Quest STARMETAL_COIL = CHAPTER.quest("starmetal_coil_block", q -> q
            .at(8, 10)
            .title("星辉线圈")
            .task(Task.item(GTOBlocks.STARMETAL_COIL_BLOCK, 16))
            .scaledReward(GTOItems.MICA_INSULATOR_FOIL, 32)
            .dependsOn(STARMETAL, CRYSTAL_MATRIX));

    public static final Quest DRACONIUM_DUST = CHAPTER.quest("draconium_dust", q -> q
            .at(6, 6)
            .title("龙粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Draconium, 16))
            .scaledReward(GTOItems.DRACONIUM_DIRT, 16)
            .dependsOn(DRAGON_ELEMENT, NewUivChapter.ENDERIUM_NANITES));

    public static final Quest DRACONIUM = CHAPTER.quest("draconium_ingot", q -> q
            .at(6, 8)
            .milestone()
            .title("龙锭")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Draconium, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.Draconium, 4)
            .dependsOn(DRACONIUM_DUST, STARMETAL_COIL));

    public static final Quest OPV_CASING = CHAPTER.quest("opv_casing", q -> q
            .at(-4, 6)
            .title("OpV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_OpV))
            .scaledReward(TagPrefix.plate, GTOMaterials.Draconium, 2)
            .dependsOn(DRACONIUM));

    public static final Quest OPV_HULL = CHAPTER.quest("opv_hull", q -> q
            .at(-4, 8)
            .milestone()
            .title("欢迎来到{过载压}阶段", DARK_GREEN)
            .task(Task.item(GTMachines.HULL[GTValues.OpV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.OpV], 1))
            .dependsOn(OPV_CASING, CRYSTAL_MATRIX_CABLE));

    public static final Quest QCD_CHARGE = CHAPTER.quest("quantum_chromodynamic_charge", q -> q
            .at(-8, 12)
            .title("量子色动力学爆弹")
            .task(Task.item(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE, 16))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Infuscolium, 2)
            .dependsOn(DRACONIUM));

    public static final Quest QCD_PLATING = CHAPTER.quest("quantumchromodynamic_protective_plating", q -> q
            .at(-6, 10)
            .title("量子色动力学防护层")
            .task(Task.item(GTOItems.QUANTUMCHROMODYNAMIC_PROTECTIVE_PLATING, 32))
            .scaledReward(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE, 1)
            .dependsOn(QCD_CHARGE));

    public static final QuestLink QCD_PLATING_COPY_1 = CHAPTER.copy(QCD_PLATING, -2, 12);

    public static final Quest QCD_MATTER = CHAPTER.quest("quantum_chromo_dynamically_confined_matter", q -> q
            .at(-6, 12)
            .title("量子色动力学封闭物质")
            .icon(GTOMaterials.QuantumChromoDynamicallyConfinedMatter.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.QuantumChromoDynamicallyConfinedMatter.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.QUANTUMCHROMODYNAMIC_PROTECTIVE_PLATING, 4)
            .dependsOn(QCD_PLATING));

    public static final QuestLink QCD_MATTER_COPY_1 = CHAPTER.copy(QCD_MATTER, 2, 18);

    public static final Quest DENSE_NEUTRON_PLASMA = CHAPTER.quest("dense_neutron_plasma", q -> q
            .at(-6, 14)
            .title("致密中子等离子")
            .icon(GTOMaterials.DenseNeutron.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.DenseNeutron.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTOMaterials.Periodicium, 8)
            .dependsOn(QCD_CHARGE, PERIODICIUM_COPY_1));

    public static final Quest SUPRACAUSAL_PCB = CHAPTER.quest("supracausal_printed_circuit_board", q -> q
            .at(-4, 14)
            .milestone()
            .title("超因果印刷电路基板")
            .task(Task.item(GTOItems.SUPRACAUSAL_PRINTED_CIRCUIT_BOARD, 2))
            .scaledReward(TagPrefix.foil, GTOMaterials.Legendarium, 32)
            .dependsOn(DENSE_NEUTRON_PLASMA, QCD_MATTER));

    public static final Quest SINGULARITIES = CHAPTER.quest("contained_singularities", q -> q
            .at(-6, 2)
            .title("遏制奇点")
            .icon(GTOItems.CONTAINED_KERR_SINGULARITY)
            .task(Task.item(GTOItems.CONTAINED_REISSNER_NORDSTROM_SINGULARITY, 2), Task.item(GTOItems.CONTAINED_KERR_NEWMANN_SINGULARITY, 2), Task.item(GTOItems.CONTAINED_KERR_SINGULARITY))
            .scaledReward(GTOItems.TIME_DILATION_CONTAINMENT_UNIT, 8)
            .dependsOn(TIME_DILATION_UNIT_COPY_1, NEUTRONIUM_SPHERES_COPY_2, NewUivChapter.LEPTONIC_CHARGE, NewUivChapter.ADVANCED_STELLAR_CASING));

    public static final Quest ANTIMATTER_PELLET = CHAPTER.quest("antimatter_pellet_and_void_matter", q -> q
            .at(10, 2)
            .title("反物质与虚空物质")
            .icon(GTOItems.VOID_MATTER)
            .task(Task.item(GTOItems.PELLET_ANTIMATTER, 8), Task.item(GTOItems.VOID_MATTER))
            .scaledReward(GTOItems.PLASMA_CONTAINMENT_CELL, 4)
            .dependsOn(UMF_FACTORY));

    public static final QuestLink ANTIMATTER_PELLET_COPY_1 = CHAPTER.copy(ANTIMATTER_PELLET, -4, 2);

    public static final Quest MICROWORMHOLE_GENERATOR = CHAPTER.quest("microwormhole_generator", q -> q
            .at(-4, 4)
            .title("微型虫洞发生器")
            .task(Task.item(GTOItems.MICROWORMHOLE_GENERATOR, 4))
            .scaledReward(TagPrefix.plate, GTOMaterials.CelestialTungsten, 4)
            .dependsOn(POWER_MODULE_4, SINGULARITIES, ANTIMATTER_PELLET_COPY_1, NewUivChapter.ENDERIUM_NANITES));

    public static final QuestLink MICROWORMHOLE_GENERATOR_COPY_1 = CHAPTER.copy(MICROWORMHOLE_GENERATOR, 0, 10);

    public static final QuestLink MICROWORMHOLE_GENERATOR_COPY_2 = CHAPTER.copy(MICROWORMHOLE_GENERATOR, 4, 18);

    public static final Quest TECH_ULTIMATE_BATTERY4 = CHAPTER.quest("tech_ultimate_battery4", q -> q
            .at(16, 10)
            .title("终极电池IV研究")
            .task(Task.techNode(EnergyNodes.LapotronEnergeStorge), Task.techNode(EnergyNodes.UltimateEnergyStorage), Task.techNode(EnergyNodes.UltimateBattery2), Task.techNode(EnergyNodes.UltimateBattery3), Task.techNode(EnergyNodes.UltimateBattery4))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest EXTREMELY_MAX_BATTERY = CHAPTER.quest("extremely_max_battery", q -> q
            .at(14, 8)
            .title("极·终极电池")
            .task(Task.item(GTOItems.EXTREMELY_MAX_BATTERY))
            .scaledReward(TagPrefix.wireGtDouble, GTOMaterials.Legendarium, 16)
            .dependsOn(LEGENDARIUM_COPY_2, NewUivChapter.HEAVY_QUARK_DEGENERATE_MATTER, TECH_ULTIMATE_BATTERY4));

    public static final Quest TECH_DIMENSION_SERIES_CASINGS = CHAPTER.quest("tech_dimension_series_casings", q -> q
            .at(10, 12)
            .title("维度级系列外壳研究")
            .task(Task.techNode(MachinesNode.DimensionSeriesCasings))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUivChapter.UXV_HULL));

    public static final Quest TECH_QFT_SERIES_CASING = CHAPTER.quest("tech_qft_series_casing", q -> q
            .at(12, 12)
            .title("量子场论级系列外壳研究")
            .task(Task.techNode(MachinesNode.QFTSeriesCasing))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(TECH_DIMENSION_SERIES_CASINGS));

    public static final Quest SPACETIME_CASINGS = CHAPTER.quest("spacetime_ripper_and_bending_core", q -> q
            .at(14, 10)
            .title("时空连续体撕裂方块与时空扭曲核心方块")
            .icon(GTOBlocks.SPACETIME_BENDING_CORE)
            .task(Task.item(GTOBlocks.SPACETIME_CONTINUUM_RIPPER, 4), Task.item(GTOBlocks.SPACETIME_BENDING_CORE, 4))
            .scaledReward(GTOBlocks.MANIPULATOR, 2)
            .dependsOn(UXV_ARM_FIELD, UXV_ENERGY_HATCH, TECH_QFT_SERIES_CASING));

    public static final Quest COSMIC_MAINFRAME = CHAPTER.quest("cosmic_mainframe", q -> q
            .at(14, 6)
            .title("寰宇处理器主机")
            .task(Task.item(GTOItems.COSMIC_MAINFRAME, 8))
            .scaledReward(GTOItems.PROTO_MATTER, 4)
            .dependsOn(PROTO_MATTER));

    public static final Quest TECH_QFT_MANIPULATOR = CHAPTER.quest("tech_qft_manipulator", q -> q
            .at(12, 10)
            .title("量子场论操纵器研究")
            .task(Task.techNode(MachinesNode.QFTManipulator))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(TECH_QFT_SERIES_CASING));

    public static final Quest QUANTUM_FORCE_TRANSFORMER = CHAPTER.quest("quantum_force_transformer", q -> q
            .at(12, 8)
            .milestone()
            .title("量子操纵者")
            .task(Task.item(MultiBlockA.QUANTUM_FORCE_TRANSFORMER))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.TitanPrecisionSteel, 16)
            .dependsOn(COSMIC_MAINFRAME, SPACETIME_CASINGS, EXTREMELY_MAX_BATTERY, PERIODICIUM, TECH_QFT_MANIPULATOR));

    public static final Quest RADOX = CHAPTER.quest("radox", q -> q
            .at(0, 6)
            .title("拉多X聚合物")
            .icon(GTOMaterials.Radox.getBucket())
            .task(Task.item(GTOMaterials.Radox.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RadoxGas.getBucket(), 4)
            .dependsOn(QUANTUM_FORCE_TRANSFORMER));

    public static final Quest SUPRACAUSAL_SMD = CHAPTER.quest("supracausal_smd", q -> q
            .at(0, 8)
            .milestone()
            .title("超因果贴片元件")
            .icon(GTOItems.SMD_TRANSISTOR_SUPRACAUSAL)
            .task(Task.item(GTOItems.SMD_CAPACITOR_SUPRACAUSAL, 64), Task.item(GTOItems.SMD_DIODE_SUPRACAUSAL, 64), Task.item(GTOItems.SMD_RESISTOR_SUPRACAUSAL, 64), Task.item(GTOItems.SMD_TRANSISTOR_SUPRACAUSAL, 64), Task.item(GTOItems.SMD_INDUCTOR_SUPRACAUSAL, 64))
            .scaledReward(TagPrefix.ring, GTOMaterials.Draconium, 2)
            .dependsOn(MICROWORMHOLE_GENERATOR, RADOX, DRACONIUM));

    public static final Quest SUPRACAUSAL_RAM = CHAPTER.quest("supracausal_ram_chip", q -> q
            .at(12, 0)
            .milestone()
            .title("超因果RAM晶片")
            .task(Task.item(GTOItems.SUPRACAUSAL_RAM_CHIP, 4))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Hikarium, 2)
            .dependsOn(ANTIMATTER_PELLET, HIKARIUM));

    public static final Quest NUCLEAR_CLOCK = CHAPTER.quest("nuclear_clock", q -> q
            .at(4, 16)
            .title("核时钟")
            .task(Task.item(GTOItems.NUCLEAR_CLOCK))
            .scaledReward(TagPrefix.plate, GTMaterials.Rhodium, 4)
            .dependsOn(POWER_MODULE_3_COPY_1));

    public static final Quest TOPOLOGICAL_MANIPULATOR = CHAPTER.quest("topological_manipulator_unit", q -> q
            .at(0, 12)
            .title("拓扑控制操纵单元")
            .task(Task.item(GTOItems.TOPOLOGICAL_MANIPULATOR_UNIT))
            .scaledReward(GTOItems.QUANTUMCHROMODYNAMIC_PROTECTIVE_PLATING, 2)
            .dependsOn(QCD_PLATING_COPY_1, MICROWORMHOLE_GENERATOR_COPY_1));

    public static final Quest SPINORIAL_MEMORY = CHAPTER.quest("relativistic_spinorial_memory_system", q -> q
            .at(-6, 18)
            .title("相对论旋量存储器")
            .task(Task.item(GTOItems.RELATIVISTIC_SPINORIAL_MEMORY_SYSTEM))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.Legendarium, 2)
            .dependsOn(POWER_MODULE_4_COPY_1, LEGENDARIUM_COPY_3, STARMETAL_COPY_1));

    public static final Quest AMORPHOUS_MATTER = CHAPTER.quest("amorphous_matter", q -> q
            .at(-2, 16)
            .title("非晶态物质")
            .task(Task.item(GTOItems.AMORPHOUS_MATTER, 8))
            .scaledReward(GTOItems.CORPOREAL_MATTER, 2)
            .dependsOn(CORPOREAL_MATTER_COPY_1));

    public static final QuestLink AMORPHOUS_MATTER_COPY_1 = CHAPTER.copy(AMORPHOUS_MATTER, 12, 18);

    public static final Quest DARK_MATTER = CHAPTER.quest("dark_matter", q -> q
            .at(0, 18)
            .title("暗物质")
            .task(Task.item(GTOItems.DARK_MATTER))
            .scaledReward(GTOItems.AMORPHOUS_MATTER, 2)
            .dependsOn(AMORPHOUS_MATTER, QCD_MATTER_COPY_1));

    public static final Quest GRAVITON_TRANSDUCER = CHAPTER.quest("graviton_transducer", q -> q
            .at(2, 16)
            .title("引力子换能器")
            .task(Task.item(GTOItems.GRAVITON_TRANSDUCER))
            .scaledReward(GTOItems.CONTAINED_REISSNER_NORDSTROM_SINGULARITY, 1)
            .dependsOn(MICROWORMHOLE_GENERATOR_COPY_2, DARK_MATTER));

    public static final Quest TECH_MANIFOLD_OSCILLATORY = CHAPTER.quest("tech_manifold_oscillatory", q -> q
            .at(10, 16)
            .title("多维流形振荡技术")
            .task(Task.techNode(BaseNodes.ManifoldOscillatory))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL, NewUevChapter.TECH_EXOTIC_TECHNOLOGY));

    public static final Quest MANIFOLD_POWER_CELL = CHAPTER.quest("manifold_oscillatory_power_cell", q -> q
            .at(12, 16)
            .title("流形振荡电池")
            .task(Task.item(GTOItems.MANIFOLD_OSCILLATORY_POWER_CELL))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.Legendarium, 8)
            .dependsOn(AMORPHOUS_MATTER_COPY_1, TECH_MANIFOLD_OSCILLATORY));

    public static final Quest TECH_SUPRACAUSAL_TECH = CHAPTER.quest("tech_supracausal_tech", q -> q
            .at(2, 12)
            .title("超因果技术")
            .task(Task.techNode(BaseNodes.SupracausalTech))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUivChapter.UXV_HULL, NewUivChapter.TECH_COSMIC_TECHNOLOGY));

    public static final Quest SUPRACAUSAL_CORE = CHAPTER.quest("supracausal_processing_core", q -> q
            .at(2, 14)
            .milestone()
            .title("超因果处理器核心")
            .task(Task.item(GTOItems.SUPRACAUSAL_PROCESSING_CORE, 8))
            .scaledReward(GTOItems.SUPRACAUSAL_PRINTED_CIRCUIT_BOARD, 1)
            .dependsOn(SUPRACAUSAL_PCB, SUPRACAUSAL_SMD, NUCLEAR_CLOCK, TOPOLOGICAL_MANIPULATOR, SPINORIAL_MEMORY, GRAVITON_TRANSDUCER, TECH_SUPRACAUSAL_TECH));

    public static final Quest SUPRACAUSAL_PROCESSOR = CHAPTER.quest("supracausal_processor", q -> q
            .at(12, 14)
            .milestone()
            .title("超因果处理器")
            .task(Task.item(GTOItems.SUPRACAUSAL_PROCESSOR, 4))
            .scaledReward(GTOItems.SUPRACAUSAL_RAM_CHIP, 4)
            .dependsOn(SUPRACAUSAL_CORE, SUPRACAUSAL_RAM, MANIFOLD_POWER_CELL));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewUivChapter.UXV_HULL, 0, 0))
                .mainLine(NewUivChapter.UXV_HULL, UXV_ENERGY_HATCH, UMF_FACTORY, SUPRACAUSAL_RAM, QUANTUM_FORCE_TRANSFORMER, DRACONIUM, SUPRACAUSAL_SMD, OPV_HULL, SUPRACAUSAL_PCB, SUPRACAUSAL_CORE, SUPRACAUSAL_PROCESSOR));
    }
}
