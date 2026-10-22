package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.SpaceNodes;

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

public final class NewUivChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_uiv", c -> c
            .title("14. 极巨压时代(UIV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.UIV]));

    public static final Quest VIBRANIUM = CHAPTER.quest("vibranium_ingot", q -> q
            .at(2, 0)
            .milestone()
            .title("振金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Vibranium, 80))
            .scaledReward(TagPrefix.plate, GTOMaterials.Vibranium, 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest UXV_CASING = CHAPTER.quest("uxv_casing", q -> q
            .at(12, 2)
            .title("UXV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_UXV))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.Taranium, 1)
            .dependsOn(VIBRANIUM));

    public static final Quest TARANIUM_CABLE = CHAPTER.quest("taranium_cable", q -> q
            .at(12, -2)
            .title("1x 塔兰线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTOMaterials.Taranium, 2))
            .scaledReward(GTOMaterials.Zylon.getBucket(), 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest PURIFIED_TENGAM = CHAPTER.quest("purified_tengam", q -> q
            .at(16, 6)
            .title("纯镃")
            .task(Task.item(TagPrefix.dust, GTOMaterials.PurifiedTengam, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.SamariumMagnetic, 4)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest ATTUNED_TENGAM = CHAPTER.quest("attuned_tengam", q -> q
            .at(16, 4)
            .title("谐镃")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AttunedTengam, 12))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.Adamantine, 16)
            .dependsOn(PURIFIED_TENGAM));

    public static final Quest SPACE_ESSENCE = CHAPTER.quest("space_essence", q -> q
            .at(20, 0)
            .title("宇宙精华")
            .task(Task.item(GTOItems.SPACE_ESSENCE, 64))
            .scaledReward(GTOMaterials.MutatedLivingSolder.getBucket(), 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest DRACONIUM_DIRT = CHAPTER.quest("draconium_dirt", q -> q
            .at(18, -2)
            .title("龙尘")
            .task(Task.item(GTOItems.DRACONIUM_DIRT, 64))
            .scaledReward(GTOMaterials.MutatedLivingSolder.getBucket(), 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest SUPER_MUTATED_LIVING_SOLDER = CHAPTER.quest("super_mutated_living_solder", q -> q
            .at(18, 0)
            .title("超突变活体焊料")
            .task(Task.item(GTOMaterials.SuperMutatedLivingSolder.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTOMaterials.Infuscolium, 8)
            .dependsOn(SPACE_ESSENCE, DRACONIUM_DIRT));

    public static final QuestLink SUPER_MUTATED_LIVING_SOLDER_COPY_1 = CHAPTER.copy(SUPER_MUTATED_LIVING_SOLDER, -2, 2);

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE5 = CHAPTER.quest("tech_component_in_assembly_line5", q -> q
            .at(18, 4)
            .title("装配线进阶部件V研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuiv))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest UIV_MOTOR = CHAPTER.quest("uiv_electric_motor", q -> q
            .at(18, 2)
            .title("UIV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_UIV, 2))
            .scaledReward(TagPrefix.rotor, GTOMaterials.Infuscolium, 1)
            .dependsOn(ATTUNED_TENGAM, SUPER_MUTATED_LIVING_SOLDER, TECH_COMPONENT_IN_ASSEMBLY_LINE5));

    public static final QuestLink UIV_MOTOR_COPY_1 = CHAPTER.copy(UIV_MOTOR, 16, 10);

    public static final Quest UIV_PUMP = CHAPTER.quest("uiv_electric_pump", q -> q
            .at(16, 0)
            .title("UIV 电泵")
            .task(Task.item(GTItems.ELECTRIC_PUMP_UIV, 2))
            .scaledReward(GTOItems.SPACE_COOLANT_CELL_60K, 1)
            .dependsOn(UIV_MOTOR));

    public static final QuestLink UIV_PUMP_COPY_1 = CHAPTER.copy(UIV_PUMP, -4, 12);

    public static final Quest UIV_VOLTAGE_COIL = CHAPTER.quest("uiv_voltage_coil", q -> q
            .at(16, 2)
            .title("UIV 电压线圈")
            .task(Task.item(GTOItems.UIV_VOLTAGE_COIL, 4))
            .scaledReward(GTOItems.PM_CHIP, 1)
            .dependsOn(ATTUNED_TENGAM));

    public static final Quest TECH_ENERGY_IO_UIV = CHAPTER.quest("tech_energy_io_uiv", q -> q
            .at(14, -2)
            .title("高压能量输入输出VI研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UIV]))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest UIV_ENERGY_HATCH = CHAPTER.quest("uiv_energy_input_hatch", q -> q
            .at(14, 0)
            .milestone()
            .title("UIV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UIV], 2))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Neutronium, 4)
            .dependsOn(UIV_PUMP, UIV_VOLTAGE_COIL, TECH_ENERGY_IO_UIV));

    public static final Quest ARCEUS_ALLOY = CHAPTER.quest("arceus_alloy_2b", q -> q
            .at(16, 14)
            .title("阿尔宙斯合金2B")
            .task(Task.item(TagPrefix.foil, GTOMaterials.ArceusAlloy2B, 64))
            .scaledReward(TagPrefix.foil, GTOMaterials.Adamantine, 16)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest ECHOITE = CHAPTER.quest("echoite_ingot", q -> q
            .at(18, 12)
            .title("回响合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Echoite, 16))
            .scaledReward(TagPrefix.gemExquisite, GTMaterials.EchoShard, 4)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final QuestLink ECHOITE_COPY_1 = CHAPTER.copy(ECHOITE, 6, 12);

    public static final QuestLink ECHOITE_COPY_2 = CHAPTER.copy(ECHOITE, -2, 14);

    public static final Quest TECH_CRYOTHEUM_SUPERCONDUCTING_TECH = CHAPTER.quest("tech_cryotheum_superconducting_tech", q -> q
            .at(-2, 10)
            .title("凛冰超导技术")
            .task(Task.techNode(BaseNodes.CryotheumSupercoductingTech))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUevChapter.UIV_HULL, NewUevChapter.TECH_SPS_TECH));

    public static final Quest ECHOITE_WIRE = CHAPTER.quest("echoite_wire", q -> q
            .at(-2, 12)
            .title("1x 回响合金导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTOMaterials.Echoite, 32))
            .scaledReward(TagPrefix.ingot, GTOMaterials.Echoite, 4)
            .dependsOn(ECHOITE_COPY_2, UIV_PUMP_COPY_1, TECH_CRYOTHEUM_SUPERCONDUCTING_TECH));

    public static final QuestLink ECHOITE_WIRE_COPY_1 = CHAPTER.copy(ECHOITE_WIRE, 0, 8);

    public static final Quest UIV_PARTS = CHAPTER.quest("uiv_parts", q -> q
            .at(16, 12)
            .title("UIV 部件")
            .icon(GTItems.ROBOT_ARM_UIV)
            .task(Task.item(GTItems.ROBOT_ARM_UIV, 2), Task.item(GTItems.SENSOR_UIV, 3), Task.item(GTItems.EMITTER_UIV, 2), Task.item(GTItems.FIELD_GENERATOR_UIV))
            .scaledReward(GTOItems.UNSTABLE_STAR, 1)
            .dependsOn(UIV_MOTOR_COPY_1, ARCEUS_ALLOY, ECHOITE));

    public static final QuestLink UIV_PARTS_COPY_1 = CHAPTER.copy(UIV_PARTS, 2, 14);

    public static final Quest NAQUADRIATIC_TARANIUM = CHAPTER.quest("naquadriatic_taranium", q -> q
            .at(6, 6)
            .title("超能硅岩-塔兰金属合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.NaquadriaticTaranium, 192))
            .scaledReward(GTOItems.MICA_INSULATOR_FOIL, 48)
            .dependsOn(NewUevChapter.ORBITAL_SMELTING_FACILITY, UIV_ENERGY_HATCH));

    public static final Quest NAQUADRIATIC_TARANIUM_COIL = CHAPTER.quest("naquadriatictaranium_coil_block", q -> q
            .at(8, 6)
            .title("超能硅岩-塔兰金属合金线圈")
            .task(Task.item(GTOBlocks.NAQUADRIATICTARANIUM_COIL_BLOCK, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.Titanium, 16)
            .dependsOn(NAQUADRIATIC_TARANIUM));

    public static final Quest BLACK_TITANIUM = CHAPTER.quest("black_titanium", q -> q
            .at(8, 8)
            .title("黑钛合金")
            .task(Task.item(TagPrefix.plate, GTOMaterials.BlackTitanium, 8))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Technetium, 4)
            .dependsOn(NAQUADRIATIC_TARANIUM_COIL));

    public static final QuestLink BLACK_TITANIUM_COPY_1 = CHAPTER.copy(BLACK_TITANIUM, 0, 14);

    public static final Quest EXOTIC_MAINFRAME = CHAPTER.quest("exotic_mainframe", q -> q
            .at(0, 12)
            .title("奇异处理器主机")
            .task(Task.item(GTOItems.EXOTIC_MAINFRAME, 4))
            .scaledReward(TagPrefix.plate, GTOMaterials.Pikyonium, 2)
            .dependsOn(BLACK_TITANIUM_COPY_1, ECHOITE_WIRE));

    public static final Quest ROBOT_MK4 = CHAPTER.quest("precision_circuit_assembly_robot_mk4", q -> q
            .at(2, 12)
            .title("精密电路装配机器人 MK IV")
            .task(Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK4))
            .scaledReward(GTOItems.COSMIC_RAM_CHIP, 6)
            .dependsOn(EXOTIC_MAINFRAME, UIV_PARTS_COPY_1, UIV_ENERGY_HATCH));

    public static final Quest GAMMA_RAYS_PHOTORESIST = CHAPTER.quest("gamma_rays_photoresist", q -> q
            .at(6, 2)
            .title("γ射线光刻胶")
            .task(Task.item(GTOMaterials.GammaRaysPhotoresist.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.EuvPhotoresist.getBucket(), 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final QuestLink GAMMA_RAYS_PHOTORESIST_COPY_1 = CHAPTER.copy(GAMMA_RAYS_PHOTORESIST, -2, 6);

    public static final Quest PREPARED_COSMIC_SOC_WAFER = CHAPTER.quest("prepared_cosmic_soc_wafer", q -> q
            .at(6, 0)
            .milestone()
            .title("预备寰宇SoC晶圆")
            .task(Task.item(GTOItems.PREPARED_COSMIC_SOC_WAFER, 2))
            .scaledReward(GTOItems.TARANIUM_WAFER, 2)
            .dependsOn(GAMMA_RAYS_PHOTORESIST));

    public static final Quest DEGASSER_PURIFICATION_UNIT = CHAPTER.quest("residual_decontaminant_degasser_purification_unit", q -> q
            .at(4, 0)
            .milestone()
            .title("残余污染物脱气净化装置")
            .task(Task.item(MultiBlockB.RESIDUAL_DECONTAMINANT_DEGASSER_PURIFICATION_UNIT))
            .scaledReward(GTOMaterials.ElectricEquilibriumWater.getBucket(), 4)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest SIMPLE_COSMIC_SOC = CHAPTER.quest("simple_cosmic_soc", q -> q
            .at(8, 0)
            .milestone()
            .title("简易寰宇SoC")
            .task(Task.item(GTOItems.SIMPLE_COSMIC_SOC, 8))
            .scaledReward(GTOMaterials.DegassedWater.getBucket(), 2)
            .dependsOn(PREPARED_COSMIC_SOC_WAFER, DEGASSER_PURIFICATION_UNIT));

    public static final Quest LAW_CLEANROOM = CHAPTER.quest("law_cleaning_maintenance_hatch", q -> q
            .at(10, 2)
            .title("绝对洁净维护仓")
            .icon(GTOMachines.LAW_CLEANING_MAINTENANCE_HATCH)
            .task(Task.item(GTOBlocks.LAW_FILTER_CASING, 16), Task.item(GTOMachines.LAW_CLEANING_MAINTENANCE_HATCH))
            .scaledReward(GTOBlocks.LAW_FILTER_CASING, 4)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final QuestLink LAW_CLEANROOM_COPY_1 = CHAPTER.copy(LAW_CLEANROOM, 4, 4);

    public static final Quest COSMIC_RAM = CHAPTER.quest("cosmic_ram_chip", q -> q
            .at(10, 0)
            .milestone()
            .title("寰宇RAM晶片")
            .task(Task.item(GTOItems.COSMIC_RAM_CHIP, 32))
            .scaledReward(GTItems.RANDOM_ACCESS_MEMORY_WAFER, 2)
            .dependsOn(PREPARED_COSMIC_SOC_WAFER, LAW_CLEANROOM));

    public static final Quest NEUTRONIUM_NANITES = CHAPTER.quest("neutronium_nanites", q -> q
            .at(12, 14)
            .title("安普洛纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Neutronium, 1))
            .scaledReward(TagPrefix.block, GTOMaterials.Enderium, 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final QuestLink NEUTRONIUM_NANITES_COPY_1 = CHAPTER.copy(NEUTRONIUM_NANITES, -2, 0);

    public static final Quest VIBRANIUM_NANITES = CHAPTER.quest("vibranium_nanites", q -> q
            .at(0, 2)
            .title("振金纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Vibranium, 1))
            .scaledReward(GTItems.HIGHLY_ADVANCED_SOC, 16)
            .dependsOn(VIBRANIUM, NEUTRONIUM_NANITES_COPY_1, SUPER_MUTATED_LIVING_SOLDER_COPY_1));

    public static final Quest ASTRAL_TITANIUM = CHAPTER.quest("astral_titanium", q -> q
            .at(8, 2)
            .title("星体钛")
            .task(Task.item(GTOMaterials.AstralTitanium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.plate, GTOMaterials.Taranium, 2)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest ADVANCED_STELLAR_CASING = CHAPTER.quest("advanced_stellar_containment_casing", q -> q
            .at(8, 4)
            .title("高级恒星热力容器")
            .task(Task.item(GTOBlocks.ADVANCED_STELLAR_CONTAINMENT_CASING, 8))
            .scaledReward(TagPrefix.screw, GTOMaterials.Vibranium, 8)
            .dependsOn(VIBRANIUM, ASTRAL_TITANIUM));

    public static final Quest LEPTONIC_CHARGE = CHAPTER.quest("leptonic_charge", q -> q
            .at(10, 4)
            .title("轻子爆弹")
            .task(Task.item(GTOBlocks.LEPTONIC_CHARGE, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.DegenerateRhenium, 10)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest QUARK_GLUON_PLASMA = CHAPTER.quest("quark_gluon_plasma", q -> q
            .at(10, 6)
            .title("夸克-胶子等离子")
            .icon(GTOMaterials.QuarkGluon.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.QuarkGluon.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOBlocks.LEPTONIC_CHARGE, 1)
            .dependsOn(ADVANCED_STELLAR_CASING, LEPTONIC_CHARGE, UIV_ENERGY_HATCH));

    public static final QuestLink QUARK_GLUON_PLASMA_COPY_1 = CHAPTER.copy(QUARK_GLUON_PLASMA, 0, 4);

    public static final Quest TECH_SPACE_ELEVATOR3 = CHAPTER.quest("tech_space_elevator3", q -> q
            .at(18, 6)
            .title("太空电梯动力改良II研究")
            .task(Task.techNode(SpaceNodes.SpaceElevator3))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest TECH_SPACE_ELEVATOR4 = CHAPTER.quest("tech_space_elevator4", q -> q
            .at(18, 8)
            .title("太空电梯动力改良III研究")
            .task(Task.techNode(SpaceNodes.SpaceElevator4))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(TECH_SPACE_ELEVATOR3));

    public static final Quest TECH_SPACE_PROBE_SURFACE_RECEPTION = CHAPTER.quest("tech_space_probe_surface_reception", q -> q
            .at(16, 8)
            .title("空间探测器表面接收技术")
            .task(Task.techNode(SpaceNodes.SpaceElevator5), Task.techNode(SpaceNodes.SpaceProbeSurfaceReception))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(TECH_SPACE_ELEVATOR4));

    public static final Quest SPACE_PROBE = CHAPTER.quest("space_probe_surface_reception", q -> q
            .at(14, 10)
            .milestone()
            .title("宇宙探测器接收单元")
            .icon(MultiBlockD.SPACE_PROBE_SURFACE_RECEPTION)
            .task(Task.item(MultiBlockD.SPACE_PROBE_SURFACE_RECEPTION))
            .scaledReward(TagPrefix.plate, GTOMaterials.Adamantium, 4)
            .dependsOn(UIV_PARTS, UIV_ENERGY_HATCH, TECH_SPACE_PROBE_SURFACE_RECEPTION));

    public static final Quest SPACE_PROBE_PRODUCT = CHAPTER.quest("space_probe_product", q -> q
            .at(2, 2)
            .title("宇宙探测器 MK I")
            .task(Task.item(GTOItems.SPACE_PROBE_MK1))
            .scaledReward(TagPrefix.plate, GTOMaterials.Adamantium, 4)
            .dependsOn(SPACE_PROBE));

    public static final Quest COSMIC_COMPUTING_MIXTURE = CHAPTER.quest("cosmic_computing_mixture", q -> q
            .at(2, 4)
            .title("寰宇计算混合物")
            .task(Task.item(GTOMaterials.CosmicComputingMixture.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Indium, 10)
            .dependsOn(VIBRANIUM_NANITES, QUARK_GLUON_PLASMA_COPY_1, SPACE_PROBE_PRODUCT));

    public static final Quest COSMIC_SUPERCONDUCTOR = CHAPTER.quest("cosmic_superconductor", q -> q
            .at(4, 6)
            .title("寰宇超导液")
            .task(Task.item(GTOMaterials.CosmicSuperconductor.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.EMPTY_LASER_COOLING_CONTAINER, 1)
            .dependsOn(COSMIC_COMPUTING_MIXTURE, LAW_CLEANROOM_COPY_1));

    public static final Quest METASTABLE_HASSIUM = CHAPTER.quest("metastable_hassium_plasma", q -> q
            .at(12, 6)
            .title("亚稳态𬭶等离子")
            .icon(GTOMaterials.MetastableHassium.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.MetastableHassium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Radon.getBucket(), 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest HEAVY_QUARK_DEGENERATE_MATTER = CHAPTER.quest("heavy_quark_degenerate_matter", q -> q
            .at(12, 8)
            .title("重夸克简并物质")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HeavyQuarkDegenerateMatter, 8))
            .scaledReward(GTOBlocks.LEPTONIC_CHARGE, 1)
            .dependsOn(QUARK_GLUON_PLASMA, METASTABLE_HASSIUM));

    public static final QuestLink HEAVY_QUARK_DEGENERATE_MATTER_COPY_1 = CHAPTER.copy(HEAVY_QUARK_DEGENERATE_MATTER, 8, 12);

    public static final Quest COSMIC_PROCESSING_UNIT_CORE = CHAPTER.quest("cosmic_processing_unit_core", q -> q
            .at(8, 10)
            .milestone()
            .title("寰宇处理器单元")
            .task(Task.item(GTOItems.COSMIC_PROCESSING_UNIT_CORE))
            .scaledReward(GTOItems.BOSE_EINSTEIN_COOLING_CONTAINER, 1)
            .dependsOn(SIMPLE_COSMIC_SOC, COSMIC_SUPERCONDUCTOR, HEAVY_QUARK_DEGENERATE_MATTER_COPY_1));

    public static final Quest ENDERIUM_NANITES = CHAPTER.quest("enderium_nanites", q -> q
            .at(12, 12)
            .title("末影素纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Enderium, 1))
            .scaledReward(TagPrefix.plate, GTOMaterials.Kevlar, 4)
            .dependsOn(NEUTRONIUM_NANITES));

    public static final Quest URUIUM = CHAPTER.quest("uruium_ingot", q -> q
            .at(14, 12)
            .title("乌鲁")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Uruium, 8))
            .scaledReward(GTMaterials.SodiumPersulfate.getBucket(), 4)
            .dependsOn(NewUevChapter.SPACE_DRONE_MK3));

    public static final Quest COSMIC_PCB = CHAPTER.quest("cosmic_printed_circuit_board", q -> q
            .at(12, 10)
            .milestone()
            .title("寰宇印刷电路基板")
            .task(Task.item(GTOItems.COSMIC_PRINTED_CIRCUIT_BOARD, 2))
            .scaledReward(GTMaterials.Iron3Chloride.getBucket(), 4)
            .dependsOn(ENDERIUM_NANITES, HEAVY_QUARK_DEGENERATE_MATTER, URUIUM));

    public static final Quest SUPERHEAVY_ALLOYS = CHAPTER.quest("superheavy_alloys", q -> q
            .at(10, 8)
            .title("超重元素合金")
            .icon(TagPrefix.plate, GTOMaterials.SuperheavyHAlloy)
            .task(Task.item(TagPrefix.plate, GTOMaterials.SuperheavyHAlloy, 2), Task.item(TagPrefix.plate, GTOMaterials.SuperheavyLAlloy, 2))
            .scaledReward(TagPrefix.plate, GTOMaterials.AbyssalAlloy, 4)
            .dependsOn(NewUevChapter.ORBITAL_SMELTING_FACILITY));

    public static final Quest BLAZE_CUBE = CHAPTER.quest("blazecube", q -> q
            .at(10, 12)
            .title("炽骨立方")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.BlazeCube, 2))
            .scaledReward(TagPrefix.wireFine, GTOMaterials.HastelloyX78, 8)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest COSMIC_SMD = CHAPTER.quest("cosmic_smd", q -> q
            .at(10, 10)
            .milestone()
            .title("寰宇贴片元件")
            .icon(GTOItems.SMD_TRANSISTOR_COSMIC)
            .task(Task.item(GTOItems.SMD_CAPACITOR_COSMIC, 24), Task.item(GTOItems.SMD_DIODE_COSMIC, 24), Task.item(GTOItems.SMD_RESISTOR_COSMIC, 24), Task.item(GTOItems.SMD_TRANSISTOR_COSMIC, 24), Task.item(GTOItems.SMD_INDUCTOR_COSMIC, 24))
            .scaledReward(GTOMaterials.FullerenePolymerMatrixPulp.getBucket(), 1)
            .dependsOn(VIBRANIUM, BLACK_TITANIUM, SUPERHEAVY_ALLOYS, BLAZE_CUBE));

    public static final Quest ULTRASHORT_PULSE_LASER = CHAPTER.quest("ultrashort_pulse_laser", q -> q
            .at(4, 12)
            .title("超短脉冲激光器")
            .task(Task.item(GTOItems.ULTRASHORT_PULSE_LASER))
            .scaledReward(GTOItems.BOSE_EINSTEIN_COOLING_CONTAINER, 1)
            .dependsOn(NewUevChapter.UIV_HULL));

    public static final Quest TECH_COSMIC_TECHNOLOGY = CHAPTER.quest("tech_cosmic_technology", q -> q
            .at(4, 8)
            .title("寰宇处理器技术")
            .task(Task.techNode(BaseNodes.CosmicTech))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUevChapter.UIV_HULL, NewUevChapter.TECH_EXOTIC_TECHNOLOGY));

    public static final Quest COSMIC_PROCESSING_CORE = CHAPTER.quest("cosmic_processing_core", q -> q
            .at(6, 10)
            .milestone()
            .title("寰宇处理器核心")
            .task(Task.item(GTOItems.COSMIC_PROCESSING_CORE, 8))
            .scaledReward(GTOItems.COSMIC_RAM_CHIP, 8)
            .dependsOn(COSMIC_PROCESSING_UNIT_CORE, COSMIC_PCB, COSMIC_SMD, ULTRASHORT_PULSE_LASER, ECHOITE_COPY_1, TECH_COSMIC_TECHNOLOGY));

    public static final Quest COSMIC_PROCESSOR = CHAPTER.quest("cosmic_processor", q -> q
            .at(4, 10)
            .milestone()
            .title("寰宇处理器")
            .task(Task.item(GTOItems.COSMIC_PROCESSOR, 4))
            .scaledReward(GTItems.HIGHLY_ADVANCED_SOC, 2)
            .dependsOn(COSMIC_PROCESSING_CORE, COSMIC_RAM));

    public static final Quest COSMIC_ASSEMBLY = CHAPTER.quest("cosmic_assembly", q -> q
            .at(2, 10)
            .milestone()
            .title("寰宇处理器集群")
            .task(Task.item(GTOItems.COSMIC_ASSEMBLY, 3))
            .scaledReward(GTOItems.FM_CHIP, 1)
            .dependsOn(COSMIC_PROCESSOR, ROBOT_MK4));

    public static final Quest FM_CHIP = CHAPTER.quest("fm_chip", q -> q
            .at(-2, 8)
            .title("飞米功率集成电路")
            .task(Task.item(GTOItems.FM_CHIP, 2))
            .scaledReward(GTOMaterials.ExtremeTemperatureWater.getBucket(), 2)
            .dependsOn(GAMMA_RAYS_PHOTORESIST_COPY_1));

    public static final Quest COSMIC_COMPUTER = CHAPTER.quest("cosmic_computer", q -> q
            .at(0, 10)
            .milestone()
            .title("寰宇处理器超级计算机")
            .task(Task.item(GTOItems.COSMIC_COMPUTER))
            .scaledReward(GTOItems.UNSTABLE_STAR, 2)
            .dependsOn(COSMIC_ASSEMBLY, FM_CHIP, ECHOITE_WIRE_COPY_1));

    public static final Quest UXV_HULL = CHAPTER.quest("uxv_hull", q -> q
            .at(12, 0)
            .milestone()
            .title("欢迎来到{极顶压}阶段", DARK_GREEN)
            .task(Task.item(GTMachines.HULL[GTValues.UXV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.UXV], 1))
            .dependsOn(UXV_CASING, TARANIUM_CABLE));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewUevChapter.UIV_HULL, 0, 0))
                .mainLine(NewUevChapter.UIV_HULL, VIBRANIUM, DEGASSER_PURIFICATION_UNIT, PREPARED_COSMIC_SOC_WAFER, SIMPLE_COSMIC_SOC, COSMIC_RAM, UXV_HULL, UIV_ENERGY_HATCH, SPACE_PROBE, COSMIC_PCB, COSMIC_SMD, COSMIC_PROCESSING_UNIT_CORE, COSMIC_PROCESSING_CORE, COSMIC_PROCESSOR, COSMIC_ASSEMBLY, COSMIC_COMPUTER));
    }
}
