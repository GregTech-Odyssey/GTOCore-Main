package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.SpaceMultiblock;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.MachinesNode;
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

import net.minecraft.world.item.Items;

public final class NewUevChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_uev", c -> c
            .title("13. 极超压时代(UEV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.UEV]));

    public static final Quest TITANSTEEL = CHAPTER.quest("titansteel_ingot", q -> q
            .at(20, 12)
            .title("泰坦钢")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TitanSteel, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Rhenium, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final QuestLink TITANSTEEL_COPY_1 = CHAPTER.copy(TITANSTEEL, 4, 2);

    public static final Quest TITANSTEEL_COIL = CHAPTER.quest("titansteel_coil_block", q -> q
            .at(22, 10)
            .title("泰坦钢线圈")
            .task(Task.item(GTOBlocks.TITANSTEEL_COIL_BLOCK, 16))
            .scaledReward(GTOItems.MICA_INSULATOR_FOIL, 16)
            .dependsOn(TITANSTEEL));

    public static final QuestLink TITANSTEEL_COIL_COPY_1 = CHAPTER.copy(TITANSTEEL_COIL, -2, 2);

    public static final QuestLink TITANSTEEL_COIL_COPY_2 = CHAPTER.copy(TITANSTEEL_COIL, -4, 8);

    public static final Quest HASTELLOY_X78 = CHAPTER.quest("hastelloy_x78", q -> q
            .at(22, 4)
            .title("哈斯特洛依合金-X78")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HastelloyX78, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Rhenium, 8)
            .dependsOn(TITANSTEEL_COIL));

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE4 = CHAPTER.quest("tech_component_in_assembly_line4", q -> q
            .at(20, 4)
            .title("装配线进阶部件IV研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuev))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest UEV_MOTOR = CHAPTER.quest("uev_electric_motor", q -> q
            .at(20, 2)
            .title("UEV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_UEV, 8))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.EnergeticNetherite, 2)
            .dependsOn(HASTELLOY_X78, TECH_COMPONENT_IN_ASSEMBLY_LINE4));

    public static final QuestLink UEV_MOTOR_COPY_1 = CHAPTER.copy(UEV_MOTOR, 4, 12);

    public static final Quest UEV_PARTS = CHAPTER.quest("uev_parts", q -> q
            .at(6, 10)
            .title("UEV 部件")
            .icon(GTItems.ROBOT_ARM_UEV)
            .task(Task.item(GTItems.ROBOT_ARM_UEV, 36), Task.item(GTItems.SENSOR_UEV, 2), Task.item(GTItems.EMITTER_UEV, 1), Task.item(GTItems.ELECTRIC_PISTON_UEV, 1), Task.item(GTItems.CONVEYOR_MODULE_UEV, 2))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UEV, 2)
            .dependsOn(UEV_MOTOR_COPY_1));

    public static final QuestLink UEV_PARTS_COPY_1 = CHAPTER.copy(UEV_PARTS, 8, 6);

    public static final QuestLink UEV_PARTS_COPY_2 = CHAPTER.copy(UEV_PARTS, 12, 18);

    public static final Quest STELLAR_CASING = CHAPTER.quest("stellar_containment_casing", q -> q
            .at(16, -2)
            .title("恒星热力容器")
            .task(Task.item(GTOBlocks.STELLAR_CONTAINMENT_CASING, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Trinium, 8)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest ULTIMATE_BATTERY = CHAPTER.quest("ultimate_battery", q -> q
            .at(20, 0)
            .title("终极电池")
            .task(Task.item(GTItems.ULTIMATE_BATTERY))
            .scaledReward(TagPrefix.plate, GTMaterials.Trinium, 8)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest TECH_STELLAR_FORGE = CHAPTER.quest("tech_stellar_forge", q -> q
            .at(18, -2)
            .title("恒星锻造材料研究")
            .task(Task.techNode(MachinesNode.MolecularSeriesCasings), Task.techNode(BaseNodes.StellarForge))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest STELLAR_FORGE = CHAPTER.quest("stellar_forge", q -> q
            .at(18, 0)
            .milestone()
            .title("恒星炎炀锻炉")
            .task(Task.item(MultiBlockA.STELLAR_FORGE))
            .scaledReward(TagPrefix.screw, GTMaterials.Darmstadtium, 16)
            .dependsOn(STELLAR_CASING, ULTIMATE_BATTERY, TECH_STELLAR_FORGE));

    public static final Quest NAQUADRIA_CHARGE = CHAPTER.quest("naquadria_charge", q -> q
            .at(14, 2)
            .title("硅岩炸药")
            .task(Task.item(GTOBlocks.NAQUADRIA_CHARGE, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadria, 8)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final QuestLink NAQUADRIA_CHARGE_COPY_1 = CHAPTER.copy(NAQUADRIA_CHARGE, 12, 6);

    public static final QuestLink NAQUADRIA_CHARGE_COPY_2 = CHAPTER.copy(NAQUADRIA_CHARGE, 0, 4);

    public static final Quest MAGNETIC_TRAP = CHAPTER.quest("magnetic_trap", q -> q
            .at(20, 8)
            .title("磁阱")
            .task(Task.item(GTOItems.MAGNETIC_TRAP, 4))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Tungsten, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest COPPER_NANITES = CHAPTER.quest("copper_nanites", q -> q
            .at(20, 6)
            .title("铜纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Copper, 2))
            .scaledReward(TagPrefix.block, GTMaterials.Copper, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest TECH_LASER_PLASMA_CONDENSER = CHAPTER.quest("tech_laser_plasma_condenser", q -> q
            .at(20, 10)
            .title("激光等离子体冷凝器研究")
            .task(Task.techNode(MachinesNode.LaserBatchProduction), Task.techNode(MachinesNode.LaserPlasmaCondenser))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest PLASMA_CONDENSER = CHAPTER.quest("plasma_condenser", q -> q
            .at(18, 8)
            .milestone()
            .title("等离子冷凝器")
            .task(Task.item(MultiBlockA.PLASMA_CONDENSER))
            .scaledReward(GTMaterials.Helium.getBucket(), 8)
            .dependsOn(MAGNETIC_TRAP, COPPER_NANITES, TECH_LASER_PLASMA_CONDENSER));

    public static final Quest ENDERIUM = CHAPTER.quest("enderium_ingot", q -> q
            .at(14, 4)
            .title("末影素")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Enderium, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.EnderEye, 16)
            .dependsOn(STELLAR_FORGE, PLASMA_CONDENSER, NAQUADRIA_CHARGE));

    public static final QuestLink ENDERIUM_COPY_1 = CHAPTER.copy(ENDERIUM, -2, 0);

    public static final QuestLink ENDERIUM_COPY_2 = CHAPTER.copy(ENDERIUM, 2, 16);

    public static final QuestLink ENDERIUM_COPY_3 = CHAPTER.copy(ENDERIUM, 16, 12);

    public static final Quest UEV_PUMP = CHAPTER.quest("uev_electric_pump", q -> q
            .at(16, 2)
            .title("UEV 电泵")
            .task(Task.item(GTItems.ELECTRIC_PUMP_UEV, 14))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UEV, 2)
            .dependsOn(UEV_MOTOR, ENDERIUM));

    public static final Quest UEV_VOLTAGE_COIL = CHAPTER.quest("uev_voltage_coil", q -> q
            .at(18, 10)
            .title("UEV 电压线圈")
            .task(Task.item(GTOItems.UEV_VOLTAGE_COIL, 28))
            .scaledReward(TagPrefix.rod, GTOMaterials.EnergeticNetherite, 4)
            .dependsOn(TITANSTEEL));

    public static final QuestLink UEV_VOLTAGE_COIL_COPY_1 = CHAPTER.copy(UEV_VOLTAGE_COIL, -4, 4);

    public static final Quest TECH_ENERGY_IO_UEV = CHAPTER.quest("tech_energy_io_uev", q -> q
            .at(16, 10)
            .title("高压能量输入输出V研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UEV]))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest UEV_ENERGY_HATCH = CHAPTER.quest("uev_energy_input_hatch", q -> q
            .at(16, 8)
            .milestone()
            .title("UEV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UEV], 14))
            .scaledReward(TagPrefix.cableGtSingle, GTOMaterials.Mithril, 8)
            .dependsOn(UEV_PUMP, UEV_VOLTAGE_COIL, TECH_ENERGY_IO_UEV));

    public static final Quest ORBITAL_SMELTING_FACILITY = CHAPTER.quest("orbital_smelting_facility", q -> q
            .at(4, 0)
            .milestone()
            .title("轨道冶炼舱")
            .task(Task.item(SpaceMultiblock.ORBITAL_SMELTING_FACILITY))
            .scaledReward(TagPrefix.dust, GTMaterials.ManganesePhosphide, 8)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest ENDERITE = CHAPTER.quest("enderite_ingot", q -> q
            .at(0, 2)
            .title("末影素合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Enderite, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.EnderPearl, 16)
            .dependsOn(ENDERIUM_COPY_1, ORBITAL_SMELTING_FACILITY, TITANSTEEL_COIL_COPY_1));

    public static final QuestLink ENDERITE_COPY_1 = CHAPTER.copy(ENDERITE, -2, 4);

    public static final QuestLink ENDERITE_COPY_2 = CHAPTER.copy(ENDERITE, -2, 14);

    public static final Quest OPTICAL_MAINFRAME = CHAPTER.quest("optical_mainframe", q -> q
            .at(2, 2)
            .title("光学处理器主机")
            .task(Task.item(GTOItems.OPTICAL_MAINFRAME, 20))
            .scaledReward(GTOItems.OPTICAL_RAM_CHIP, 16)
            .dependsOn(ENDERITE, TITANSTEEL_COPY_1));

    public static final QuestLink OPTICAL_MAINFRAME_COPY_1 = CHAPTER.copy(OPTICAL_MAINFRAME, 0, 6);

    public static final QuestLink OPTICAL_MAINFRAME_COPY_2 = CHAPTER.copy(OPTICAL_MAINFRAME, 14, 18);

    public static final Quest TECH_SPACE_ELEVATOR2 = CHAPTER.quest("tech_space_elevator2", q -> q
            .at(6, 12)
            .title("太空电梯动力改良研究")
            .task(Task.techNode(SpaceNodes.SpaceElevator2))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest POWER_MODULE_MK2 = CHAPTER.quest("space_elevator_power_module_2", q -> q
            .at(8, 12)
            .title("太空电梯动力模块 MK II")
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_2, 16))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.FluxedElectrum, 6)
            .dependsOn(NewUhvChapter.UEV_HULL, TECH_SPACE_ELEVATOR2));

    public static final Quest SPACE_DRONE_MK3 = CHAPTER.quest("space_drone_mk3", q -> q
            .at(8, 10)
            .title("太空无人机 MK3")
            .task(Task.item(GTOItems.SPACE_DRONE_MK3, 2))
            .scaledReward(TagPrefix.plate, GTOMaterials.Quantanium, 8)
            .dependsOn(UEV_PARTS, POWER_MODULE_MK2));

    public static final Quest BLOODSTONE = CHAPTER.quest("bloodstone_dust", q -> q
            .at(10, 10)
            .title("血石")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Bloodstone, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.Antimony, 16)
            .dependsOn(SPACE_DRONE_MK3));

    public static final Quest ADAMANTIUM = CHAPTER.quest("adamantium_ingot", q -> q
            .at(12, 8)
            .milestone()
            .title("艾德曼合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Adamantium, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.Bloodstone, 6)
            .dependsOn(STELLAR_FORGE, PLASMA_CONDENSER, UEV_ENERGY_HATCH, BLOODSTONE, NAQUADRIA_CHARGE_COPY_1));

    public static final Quest UIV_CASING = CHAPTER.quest("uiv_casing", q -> q
            .at(4, 6)
            .title("UIV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_UIV))
            .scaledReward(TagPrefix.plate, GTOMaterials.Adamantium, 2)
            .dependsOn(ADAMANTIUM));

    public static final Quest HIGHLY_INSULATING_FOIL = CHAPTER.quest("highly_insulating_foil", q -> q
            .at(2, 12)
            .title("高绝缘性箔")
            .task(Task.item(GTOItems.HIGHLY_INSULATING_FOIL, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest NEUTRONIUM_CABLE = CHAPTER.quest("neutronium_cable", q -> q
            .at(2, 10)
            .title("1x 安普洛线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Neutronium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Neutronium, 2)
            .dependsOn(HIGHLY_INSULATING_FOIL));

    public static final Quest ZYLON = CHAPTER.quest("zylon", q -> q
            .at(10, 4)
            .title("柴隆纤维")
            .icon(GTOMaterials.Zylon.getBucket())
            .task(Task.item(GTOMaterials.Zylon.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.Zylon, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final QuestLink ZYLON_COPY_1 = CHAPTER.copy(ZYLON, 4, 10);

    public static final Quest DEGENERATE_RHENIUM = CHAPTER.quest("degenerate_rhenium", q -> q
            .at(2, 4)
            .title("简并态铼")
            .task(Task.item(TagPrefix.plate, GTOMaterials.DegenerateRhenium, 8))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Rhenium, 5)
            .dependsOn(STELLAR_FORGE, PLASMA_CONDENSER, UEV_ENERGY_HATCH, NAQUADRIA_CHARGE_COPY_2));

    public static final QuestLink DEGENERATE_RHENIUM_COPY_1 = CHAPTER.copy(DEGENERATE_RHENIUM, -6, 12);

    public static final QuestLink DEGENERATE_RHENIUM_COPY_2 = CHAPTER.copy(DEGENERATE_RHENIUM, -2, 12);

    public static final QuestLink DEGENERATE_RHENIUM_COPY_3 = CHAPTER.copy(DEGENERATE_RHENIUM, 8, 16);

    public static final Quest UNSTABLE_GAIA_SOUL = CHAPTER.quest("unstable_gaia_soul", q -> q
            .at(-6, 8)
            .title("不稳定盖亚之魂")
            .task(Task.item(GTOItems.UNSTABLE_GAIA_SOUL))
            .scaledReward(GTOMaterials.Aether.getBucket(), 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest TRANSCENDING_MATTER = CHAPTER.quest("transcending_matter", q -> q
            .at(-6, 10)
            .title("超越物质")
            .icon(GTOMaterials.TranscendingMatter.getBucket())
            .task(Task.item(GTOMaterials.TranscendingMatter.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Aether.getBucket(), 1)
            .dependsOn(DEGENERATE_RHENIUM_COPY_1, UNSTABLE_GAIA_SOUL));

    public static final QuestLink TRANSCENDING_MATTER_COPY_1 = CHAPTER.copy(TRANSCENDING_MATTER, 8, 18);

    public static final QuestLink TRANSCENDING_MATTER_COPY_2 = CHAPTER.copy(TRANSCENDING_MATTER, 16, 18);

    public static final Quest RARE_EARTH_CENTRIFUGAL = CHAPTER.quest("rare_earth_centrifugal", q -> q
            .at(8, 0)
            .milestone()
            .title("稀土离心机")
            .task(Task.item(MultiBlockA.RARE_EARTH_CENTRIFUGAL))
            .scaledReward(TagPrefix.plate, GTOMaterials.Stellite, 8)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest ADAMANTINE = CHAPTER.quest("adamantine_ingot", q -> q
            .at(-4, 10)
            .title("精金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Adamantine, 128))
            .scaledReward(TagPrefix.dust, GTMaterials.Mirabilite, 17)
            .dependsOn(RARE_EARTH_CENTRIFUGAL, TRANSCENDING_MATTER, TITANSTEEL_COIL_COPY_2));

    public static final QuestLink ADAMANTINE_COPY_1 = CHAPTER.copy(ADAMANTINE, -4, 18);

    public static final QuestLink ADAMANTINE_COPY_2 = CHAPTER.copy(ADAMANTINE, 2, 18);

    public static final QuestLink ADAMANTINE_COPY_3 = CHAPTER.copy(ADAMANTINE, 18, 18);

    public static final QuestLink ADAMANTINE_COPY_4 = CHAPTER.copy(ADAMANTINE, 18, 12);

    public static final Quest ADAMANTINE_COIL = CHAPTER.quest("adamantine_coil_block", q -> q
            .at(-2, 18)
            .title("精金线圈")
            .task(Task.item(GTOBlocks.ADAMANTINE_COIL_BLOCK, 16))
            .scaledReward(GTOItems.MICA_INSULATOR_FOIL, 16)
            .dependsOn(ADAMANTINE_COPY_1, UEV_ENERGY_HATCH));

    public static final QuestLink ADAMANTINE_COIL_COPY_1 = CHAPTER.copy(ADAMANTINE_COIL, 6, 16);

    public static final QuestLink ADAMANTINE_COIL_COPY_2 = CHAPTER.copy(ADAMANTINE_COIL, 20, 16);

    public static final Quest CINOBITE = CHAPTER.quest("cinobite_ingot", q -> q
            .at(-2, 16)
            .title("西诺柏合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Cinobite, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Terbium, 4)
            .dependsOn(ADAMANTINE_COIL));

    public static final QuestLink CINOBITE_COPY_1 = CHAPTER.copy(CINOBITE, 14, 12);

    public static final Quest INFUSCOLIUM = CHAPTER.quest("infuscolium", q -> q
            .at(16, 16)
            .title("魔金")
            .task(Task.item(TagPrefix.plate, GTOMaterials.Infuscolium, 4))
            .scaledReward(Items.END_CRYSTAL, 4)
            .dependsOn(ADAMANTINE_COPY_3, TRANSCENDING_MATTER_COPY_2));

    public static final Quest UNSTABLE_STAR = CHAPTER.quest("unstable_star", q -> q
            .at(-2, 10)
            .title("不稳定之星")
            .task(Task.item(GTOItems.UNSTABLE_STAR, 10))
            .scaledReward(GTItems.GRAVI_STAR, 2)
            .dependsOn(ADAMANTINE));

    public static final QuestLink UNSTABLE_STAR_COPY_1 = CHAPTER.copy(UNSTABLE_STAR, 18, 16);

    public static final Quest FULLERENE_POLYMER_MATRIX = CHAPTER.quest("fullerene_polymer_matrix_pulp", q -> q
            .at(0, 16)
            .title("富勒烯聚合物基体")
            .task(Task.item(TagPrefix.dust, GTOMaterials.FullerenePolymerMatrixPulp, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.Fullerene, 2)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest SPACE_COOLANT_60K = CHAPTER.quest("space_coolant_cell_60k", q -> q
            .at(-2, 8)
            .title("60K 空间冷却单元")
            .task(Task.item(GTOItems.SPACE_COOLANT_CELL_60K, 8))
            .scaledReward(GTOItems.SPACE_COOLANT_CELL_30K, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest FUSION_COIL_MK2 = CHAPTER.quest("fusion_coil_mk2", q -> q
            .at(-6, 6)
            .title("聚变线圈 MK2")
            .task(Task.item(GTOBlocks.FUSION_COIL_MK2, 4))
            .scaledReward(GTItems.GRAVI_STAR, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest FUSION_CASING_MK5 = CHAPTER.quest("fusion_casing_mk5", q -> q
            .at(-4, 6)
            .title("聚变外壳 MK5")
            .task(Task.item(GTOBlocks.FUSION_CASING_MK5, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Seaborgium, 6)
            .dependsOn(FUSION_COIL_MK2, UEV_VOLTAGE_COIL_COPY_1));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR5 = CHAPTER.quest("tech_tokamak_fusion_reactor5", q -> q
            .at(2, 6)
            .title("托卡马克聚变反应堆V研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor5))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest UEV_FUSION_REACTOR = CHAPTER.quest("uev_fusion_reactor", q -> q
            .at(0, 8)
            .milestone()
            .title("UEV 聚变反应堆")
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UEV]))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Seaborgium, 4)
            .dependsOn(FUSION_CASING_MK5, SPACE_COOLANT_60K, UNSTABLE_STAR, OPTICAL_MAINFRAME_COPY_1, ENDERITE_COPY_1, UEV_ENERGY_HATCH, TECH_TOKAMAK_FUSION_REACTOR5));

    public static final Quest EXOTIC_CHIP = CHAPTER.quest("exotic_chip", q -> q
            .at(12, 0)
            .milestone()
            .title("奇异芯片")
            .task(Task.item(GTOItems.EXOTIC_CHIP, 4))
            .scaledReward(GTOMaterials.ElectricEquilibriumWater.getBucket(), 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest RYDBERG_AND_X_RAY_LASER = CHAPTER.quest("rydberg_spinorial_assembly_and_x_ray_laser", q -> q
            .at(10, 6)
            .title("里德伯旋量组件与 X 射线激光器")
            .icon(GTOItems.RYDBERG_SPINORIAL_ASSEMBLY)
            .task(Task.item(GTOItems.RYDBERG_SPINORIAL_ASSEMBLY, 2), Task.item(GTOItems.X_RAY_LASER, 1))
            .scaledReward(GTOMaterials.QuantumDots.getBucket(), 1)
            .dependsOn(EXOTIC_CHIP, ZYLON));

    public static final QuestLink RYDBERG_AND_X_RAY_LASER_COPY_1 = CHAPTER.copy(RYDBERG_AND_X_RAY_LASER, 14, 10);

    public static final Quest TECH_SPS_TECH = CHAPTER.quest("tech_sps_tech", q -> q
            .at(6, 4)
            .title("超临界移相技术")
            .task(Task.techNode(BaseNodes.SPSTech))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final QuestLink TECH_SPS_TECH_COPY_1 = CHAPTER.copy(TECH_SPS_TECH, 10, 18);

    public static final Quest SPS_CRAFTING = CHAPTER.quest("sps_crafting", q -> q
            .at(8, 8)
            .milestone()
            .title("超临界合成机")
            .task(Task.item(MultiBlockA.SPS_CRAFTING))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Americium, 4)
            .dependsOn(UEV_PARTS_COPY_1, OPTICAL_MAINFRAME, DEGENERATE_RHENIUM, RYDBERG_AND_X_RAY_LASER, TECH_SPS_TECH));

    public static final Quest BEDROCK_GAS = CHAPTER.quest("bedrock_gas", q -> q
            .at(6, 18)
            .title("基岩气")
            .icon(GTOMaterials.BedrockGas.getBucket())
            .task(Task.item(GTOMaterials.BedrockGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Xenon.getBucket(), 1)
            .dependsOn(SPS_CRAFTING, ADAMANTINE_COIL_COPY_1, TRANSCENDING_MATTER_COPY_1));

    public static final Quest UNSTABLE_VIBRANIUM = CHAPTER.quest("vibranium_unstable", q -> q
            .at(4, 18)
            .title("不稳定振金")
            .icon(GTOMaterials.VibraniumUnstable.getBucket())
            .task(Task.item(GTOMaterials.VibraniumUnstable.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 4)
            .dependsOn(BEDROCK_GAS, ADAMANTINE_COPY_2));

    public static final Quest VIBRANIUM_PLASMA = CHAPTER.quest("vibranium_plasma", q -> q
            .at(4, 16)
            .title("振金等离子")
            .icon(GTOMaterials.Vibranium.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.Vibranium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.VibraniumUnstable.getBucket(), 1)
            .dependsOn(UNSTABLE_VIBRANIUM, UEV_FUSION_REACTOR, ADAMANTIUM));

    public static final Quest EXOTIC_PCB = CHAPTER.quest("exotic_printed_circuit_board", q -> q
            .at(4, 14)
            .milestone()
            .title("奇异印刷电路板")
            .task(Task.item(GTOItems.EXOTIC_PRINTED_CIRCUIT_BOARD, 5))
            .scaledReward(TagPrefix.plate, GTOMaterials.Kevlar, 2)
            .dependsOn(VIBRANIUM_PLASMA, ENDERIUM_COPY_2));

    public static final Quest EXOTIC_SMD = CHAPTER.quest("exotic_smd", q -> q
            .at(0, 14)
            .milestone()
            .title("奇异贴片元件")
            .icon(GTOItems.SMD_DIODE_EXOTIC)
            .task(Task.item(GTOItems.SMD_CAPACITOR_EXOTIC, 16), Task.item(GTOItems.SMD_DIODE_EXOTIC, 16), Task.item(GTOItems.SMD_RESISTOR_EXOTIC, 16), Task.item(GTOItems.SMD_TRANSISTOR_EXOTIC, 16), Task.item(GTOItems.SMD_INDUCTOR_EXOTIC, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.FullerenePolymerMatrixPulp, 2)
            .dependsOn(CINOBITE, ENDERITE_COPY_2, DEGENERATE_RHENIUM_COPY_2, ADAMANTIUM, FULLERENE_POLYMER_MATRIX));

    public static final Quest HIGH_PRECISION_CRYSTAL_SOC = CHAPTER.quest("high_precision_crystal_soc", q -> q
            .at(12, 10)
            .title("高精度晶体 SoC")
            .task(Task.item(GTOItems.HIGH_PRECISION_CRYSTAL_SOC, 4))
            .scaledReward(GTItems.CRYSTAL_SYSTEM_ON_CHIP, 2)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest SEALED_SINGULARITY_PLATFORM = CHAPTER.quest("sealed_singularity_platform", q -> q
            .at(10, 12)
            .title("密封奇点平台")
            .task(Task.item(GTOItems.SEALED_SINGULARITY_PLATFORM))
            .scaledReward(GTItems.SENSOR_UV, 1)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest QUANTUM_DOTS = CHAPTER.quest("quantum_dots", q -> q
            .at(12, 16)
            .title("量子点")
            .icon(GTOMaterials.QuantumDots.getBucket())
            .task(Task.item(GTOMaterials.QuantumDots.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Selenium, 4)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest TECH_EXOTIC_TECHNOLOGY = CHAPTER.quest("tech_exotic_technology", q -> q
            .at(10, 16)
            .title("奇异处理器技术")
            .task(Task.techNode(BaseNodes.TimeDilationTech), Task.techNode(BaseNodes.ExoticTech))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewUhvChapter.TECH_OPTICAL_TECH, TECH_SPS_TECH_COPY_1));

    public static final Quest EXOTIC_PROCESSING_CORE = CHAPTER.quest("exotic_processing_core", q -> q
            .at(10, 14)
            .milestone()
            .title("奇异处理核心")
            .task(Task.item(GTOItems.EXOTIC_PROCESSING_CORE, 8))
            .scaledReward(TagPrefix.foil, GTOMaterials.Kevlar, 8)
            .dependsOn(EXOTIC_PCB, EXOTIC_SMD, RYDBERG_AND_X_RAY_LASER_COPY_1, HIGH_PRECISION_CRYSTAL_SOC, SEALED_SINGULARITY_PLATFORM, QUANTUM_DOTS, DEGENERATE_RHENIUM_COPY_3, TECH_EXOTIC_TECHNOLOGY));

    public static final Quest EXOTIC_RAM = CHAPTER.quest("exotic_ram_chip", q -> q
            .at(14, 0)
            .milestone()
            .title("奇异 RAM 芯片")
            .task(Task.item(GTOItems.EXOTIC_RAM_CHIP, 24))
            .scaledReward(GTOMaterials.ExtremeTemperatureWater.getBucket(), 2)
            .dependsOn(NewUhvChapter.UEV_HULL));

    public static final Quest EXOTIC_PROCESSOR = CHAPTER.quest("exotic_processor", q -> q
            .at(14, 14)
            .milestone()
            .title("奇异处理器")
            .task(Task.item(GTOItems.EXOTIC_PROCESSOR, 6))
            .scaledReward(GTOItems.SMD_TRANSISTOR_EXOTIC, 8)
            .dependsOn(EXOTIC_PROCESSING_CORE, EXOTIC_RAM, CINOBITE_COPY_1));

    public static final Quest ROBOT_MK3 = CHAPTER.quest("precision_circuit_assembly_robot_mk3", q -> q
            .at(14, 16)
            .title("精密电路组装机器人 MK3")
            .task(Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK3))
            .scaledReward(TagPrefix.plate, GTOMaterials.Pikyonium, 2)
            .dependsOn(UEV_PARTS_COPY_2, OPTICAL_MAINFRAME_COPY_2, UEV_ENERGY_HATCH));

    public static final Quest EXOTIC_ASSEMBLY = CHAPTER.quest("exotic_assembly", q -> q
            .at(16, 14)
            .milestone()
            .title("奇异处理器集群")
            .task(Task.item(GTOItems.EXOTIC_ASSEMBLY, 3))
            .scaledReward(GTOItems.EXOTIC_RAM_CHIP, 16)
            .dependsOn(EXOTIC_PROCESSOR, ROBOT_MK3, INFUSCOLIUM));

    public static final Quest TARANIUM_RICH_HELIUM_4 = CHAPTER.quest("taranium_rich_liquid_helium_4", q -> q
            .at(22, 18)
            .title("富塔兰金属的氦-4")
            .icon(GTOMaterials.TaraniumRichLiquidHelium4.getBucket())
            .task(Task.item(GTOMaterials.TaraniumRichLiquidHelium4.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium3.getBucket(), 4)
            .dependsOn(PLASMA_CONDENSER));

    public static final Quest TARANIUM = CHAPTER.quest("taranium_ingot", q -> q
            .at(22, 16)
            .title("塔兰")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Taranium, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.Taranium, 1)
            .dependsOn(TARANIUM_RICH_HELIUM_4, ADAMANTINE_COIL_COPY_2));

    public static final Quest TARANIUM_WAFER = CHAPTER.quest("taranium_wafer", q -> q
            .at(22, 14)
            .title("塔兰晶圆")
            .task(Task.item(GTOItems.TARANIUM_WAFER, 2))
            .scaledReward(GTOMaterials.ElectricEquilibriumWater.getBucket(), 1)
            .dependsOn(TARANIUM));

    public static final Quest PM_CHIP = CHAPTER.quest("pm_chip", q -> q
            .at(20, 14)
            .title("PM 芯片")
            .task(Task.item(GTOItems.PM_CHIP, 2))
            .scaledReward(GTOItems.TARANIUM_WAFER, 1)
            .dependsOn(TARANIUM_WAFER));

    public static final Quest EXOTIC_COMPUTER = CHAPTER.quest("exotic_computer", q -> q
            .at(18, 14)
            .milestone()
            .title("奇异处理器超级计算机")
            .task(Task.item(GTOItems.EXOTIC_COMPUTER))
            .scaledReward(GTOItems.EXOTIC_PRINTED_CIRCUIT_BOARD, 1)
            .dependsOn(EXOTIC_ASSEMBLY, PM_CHIP, UNSTABLE_STAR_COPY_1, ADAMANTINE_COPY_4, ENDERIUM_COPY_3));

    public static final Quest UIV_HULL = CHAPTER.quest("uiv_hull", q -> q
            .at(4, 8)
            .milestone()
            .title("UIV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.UIV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.UIV], 1))
            .dependsOn(UIV_CASING, NEUTRONIUM_CABLE, ZYLON_COPY_1));

    static {
        CHAPTER.decorate(d -> d.link(QuestLink.of(NewUhvChapter.UEV_HULL, 0, 0))
                .mainLine(NewUhvChapter.UEV_HULL, ORBITAL_SMELTING_FACILITY, RARE_EARTH_CENTRIFUGAL, EXOTIC_CHIP, EXOTIC_RAM, STELLAR_FORGE, PLASMA_CONDENSER, UEV_ENERGY_HATCH, ADAMANTIUM, SPS_CRAFTING, UIV_HULL, UEV_FUSION_REACTOR, EXOTIC_SMD, EXOTIC_PCB, EXOTIC_PROCESSING_CORE, EXOTIC_PROCESSOR, EXOTIC_ASSEMBLY, EXOTIC_COMPUTER));
    }
}
