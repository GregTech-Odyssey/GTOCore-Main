package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.EnergyNodes;
import com.gtocore.data.techtree.MachinesNode;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

public final class NewMaxChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_max", c -> c
            .title("17. 上限压时代(MAX)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.MAX]));

    public static final Quest LASER_TARGET_HATCH = CHAPTER.quest("laser_target_hatch_4096a", q -> q
            .at(12, -2)
            .title("4096 安激光靶仓")
            .task(Task.item(GTMachines.LASER_INPUT_HATCH_4096[GTValues.OpV], 2))
            .scaledReward(TagPrefix.lens, GTMaterials.Diamond, 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest EXCITED_DTSC = CHAPTER.quest("excited_dtsc", q -> q
            .at(2, 2)
            .title("激发的恒星超维度催化剂")
            .icon(GTOMaterials.ExcitedDtsc.getBucket())
            .task(Task.item(GTOMaterials.ExcitedDtsc.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.DimensionallyTranscendentResidue.getBucket(), 2)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final QuestLink EXCITED_DTSC_COPY_1 = CHAPTER.copy(EXCITED_DTSC, 14, -2);

    public static final QuestLink EXCITED_DTSC_COPY_2 = CHAPTER.copy(EXCITED_DTSC, 2, 10);

    public static final Quest ETERNITY_CATALYST = CHAPTER.quest("eternity_catalyst", q -> q
            .at(14, 2)
            .title("永恒催化剂")
            .task(Task.item(GTOItems.ETERNITY_CATALYST, 2))
            .scaledReward(TagPrefix.block, GTOMaterials.Eternity, 2)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest ETERNITY_NANITES = CHAPTER.quest("eternity_nanites", q -> q
            .at(14, 0)
            .title("永恒纳米蜂群")
            .icon(GTOTagPrefix.NANITES, GTOMaterials.Eternity)
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Eternity, 1))
            .scaledReward(TagPrefix.frameGt, GTOMaterials.Eternity, 1)
            .dependsOn(ETERNITY_CATALYST, EXCITED_DTSC_COPY_1));

    public static final Quest MHDCSM = CHAPTER.quest("magnetohydrodynamically_constrained_star_matter", q -> q
            .at(12, 0)
            .milestone()
            .title("磁流体动力学约束恒星物质")
            .icon(GTOMaterials.MagnetohydrodynamicallyConstrainedStarMatter.getBucket())
            .task(Task.item(GTOMaterials.MagnetohydrodynamicallyConstrainedStarMatter.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.foil, GTOMaterials.Eternity, 4)
            .dependsOn(LASER_TARGET_HATCH, ETERNITY_NANITES));

    public static final Quest STABLE_BARYONIC_MATTER = CHAPTER.quest("stable_baryonic_matter", q -> q
            .at(2, -2)
            .title("稳定重子物质")
            .icon(GTOMaterials.StableBaryonicMatter.getBucket())
            .task(Task.item(GTOMaterials.StableBaryonicMatter.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE, 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest SPACETIME_CONTINUUM = CHAPTER.quest("spacetime_continuum", q -> q
            .at(4, 8)
            .title("时空连续体")
            .icon(GTOMaterials.SpacetimeContinuum.getBucket())
            .task(Task.item(GTOMaterials.SpacetimeContinuum.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTOMaterials.TranscendentMetal, 8)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest DIMENSION_SHIFTED_SUPER_FLUID = CHAPTER.quest("dimension_allyshifted_super_fluid", q -> q
            .at(2, 0)
            .milestone()
            .title("维度偏移超流体")
            .icon(GTOMaterials.DimensionAllyshiftedSuperFluid.getBucket())
            .task(Task.item(GTOMaterials.DimensionAllyshiftedSuperFluid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.StableBaryonicMatter.getBucket(), 1)
            .dependsOn(STABLE_BARYONIC_MATTER, EXCITED_DTSC));

    public static final Quest MAGMATTER = CHAPTER.quest("magmatter", q -> q
            .at(4, 10)
            .title("磁物质")
            .icon(TagPrefix.rodLong, GTOMaterials.Magmatter)
            .task(Task.item(TagPrefix.rodLong, GTOMaterials.Magmatter, 33))
            .scaledReward(TagPrefix.ingot, GTOMaterials.EnergeticNetherite, 4)
            .dependsOn(EXCITED_DTSC_COPY_2));

    public static final Quest SPACE_DRONE_MK6 = CHAPTER.quest("space_drone_mk6", q -> q
            .at(8, 10)
            .title("太空无人机 MK VI")
            .task(Task.item(GTOItems.SPACE_DRONE_MK6, 64))
            .scaledReward(GTOMaterials.StellarEnergyRocketFuel.getBucket(), 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest DWARF_MATTER = CHAPTER.quest("white_and_black_dwarf_matter", q -> q
            .at(6, 10)
            .title("白矮星与黑矮星物质")
            .icon(GTOMaterials.WhiteDwarfMatter.getBucket())
            .task(Task.item(GTOMaterials.WhiteDwarfMatter.getBucket()).optional(), Task.item(GTOMaterials.BlackDwarfMatter.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTOMaterials.Infinity, 4)
            .dependsOn(SPACE_DRONE_MK6));

    public static final QuestLink DWARF_MATTER_COPY_1 = CHAPTER.copy(DWARF_MATTER, -4, 4);

    public static final Quest TECH_LASER_ELECTROMAGNETIC_PROCESSING = CHAPTER.quest("tech_laser_electromagnetic_processing", q -> q
            .at(6, -2)
            .title("激光电磁加工研究")
            .task(Task.techNode(MachinesNode.LaserElectroMagneticProcessing))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest MAGNETIC_ENERGY_REACTION_FURNACE = CHAPTER.quest("magnetic_energy_reaction_furnace", q -> q
            .at(6, 0)
            .milestone()
            .title("磁能反应炉")
            .task(Task.item(MultiBlockB.MAGNETIC_ENERGY_REACTION_FURNACE))
            .scaledReward(GTOItems.INFINITY_SINGULARITY, 1)
            .dependsOn(NewOpvChapter.MAX_HULL, TECH_LASER_ELECTROMAGNETIC_PROCESSING));

    public static final Quest COSMIC_SINGULARITY = CHAPTER.quest("cosmic_singularity_and_shirabon", q -> q
            .at(-2, 2)
            .title("宇宙奇点与调律源金")
            .icon(GTOItems.COSMIC_SINGULARITY)
            .task(Task.item(GTOItems.COSMIC_SINGULARITY, 1), Task.item(TagPrefix.dust, GTOMaterials.Shirabon, 64))
            .scaledReward(GTOItems.HYPERCUBE, 2)
            .dependsOn(MHDCSM, MAGNETIC_ENERGY_REACTION_FURNACE));

    public static final Quest COSMIC_INGOT = CHAPTER.quest("cosmic_ingot", q -> q
            .at(-2, 4)
            .title("宇宙")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Cosmic, 16))
            .scaledReward(TagPrefix.ingot, GTOMaterials.TranscendentMetal, 8)
            .dependsOn(COSMIC_SINGULARITY, DWARF_MATTER_COPY_1));

    public static final Quest SPACETIME_ASSEMBLY_LINE_CASING = CHAPTER.quest("spacetime_assembly_line_casing", q -> q
            .at(10, -2)
            .title("超时空装配外壳与单元")
            .icon(GTOBlocks.SPACETIME_ASSEMBLY_LINE_CASING)
            .task(Task.item(GTOBlocks.SPACETIME_ASSEMBLY_LINE_CASING, 16), Task.item(GTOBlocks.SPACETIME_ASSEMBLY_LINE_UNIT, 16))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Dalisenite, 8)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest TECH_SUPRACHRONAL_ASSEMBLY_LINE = CHAPTER.quest("tech_suprachronal_assembly_line", q -> q
            .at(10, 2)
            .title("超时空装配线研究")
            .task(Task.techNode(MachinesNode.LaserAssemblyProcessing), Task.techNode(MachinesNode.SuprachronalAssemblyLine))
            .scaledReward(GTOItems.ATOMIC_ARCHIVES, 1)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest NYARLATHOTEPS_TENTACLE = CHAPTER.quest("nyarlathoteps_tentacle", q -> q
            .at(10, 0)
            .milestone()
            .title("奈亚拉托提普之触")
            .task(Task.item(MultiBlockD.NYARLATHOTEPS_TENTACLE))
            .scaledReward(TagPrefix.cableGtSingle, GTOMaterials.CosmicNeutronium, 16)
            .dependsOn(SPACETIME_ASSEMBLY_LINE_CASING, TECH_SUPRACHRONAL_ASSEMBLY_LINE));

    public static final Quest MAX_MOTOR_PUMP = CHAPTER.quest("max_motor_pump", q -> q
            .at(6, 8)
            .title("MAX 电动马达与电动泵")
            .icon(GTOItems.MAX_ELECTRIC_MOTOR)
            .task(Task.item(GTOItems.MAX_ELECTRIC_MOTOR, 33), Task.item(GTOItems.MAX_ELECTRIC_PUMP, 1))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.TranscendentMetal, 8)
            .dependsOn(NYARLATHOTEPS_TENTACLE, NewOpvChapter.CTC_GUIDANCE_UNIT, SPACETIME_CONTINUUM, DIMENSION_SHIFTED_SUPER_FLUID, MAGMATTER, DWARF_MATTER));

    public static final QuestLink MAX_MOTOR_PUMP_COPY_1 = CHAPTER.copy(MAX_MOTOR_PUMP, 0, 2);

    public static final Quest MAX_EMITTER_FIELD = CHAPTER.quest("max_emitter_field_generator", q -> q
            .at(0, 4)
            .title("MAX 发射器与力场发生器")
            .icon(GTOItems.MAX_FIELD_GENERATOR)
            .task(Task.item(GTOItems.MAX_EMITTER, 32), Task.item(GTOItems.MAX_FIELD_GENERATOR, 16))
            .scaledReward(GTOItems.NUCLEAR_STAR, 4)
            .dependsOn(MAX_MOTOR_PUMP_COPY_1, COSMIC_INGOT));

    public static final Quest MAX_VOLTAGE_COIL = CHAPTER.quest("max_voltage_coil", q -> q
            .at(8, 4)
            .title("上限压线圈")
            .task(Task.item(GTOItems.MAX_VOLTAGE_COIL, 2))
            .scaledReward(TagPrefix.rod, GTOMaterials.AttunedTengam, 2)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest TECH_ENERGY_IO_MAX = CHAPTER.quest("tech_energy_io_max", q -> q
            .at(8, 8)
            .title("高压能量输入输出IX研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.MAX]))
            .scaledReward(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT, 1)
            .dependsOn(NewOpvChapter.MAX_HULL, NewOpvChapter.TECH_ENERGY_IO_OPV));

    public static final Quest MAX_ENERGY_HATCH = CHAPTER.quest("max_energy_hatch", q -> q
            .at(8, 6)
            .milestone()
            .title("MAX 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.MAX]))
            .scaledReward(GTMaterials.SodiumPotassium.getBucket(), 4)
            .dependsOn(MAX_MOTOR_PUMP, MAX_VOLTAGE_COIL, TECH_ENERGY_IO_MAX));

    public static final Quest STABILIZER_CORE = CHAPTER.quest("stabilizer_core", q -> q
            .at(10, 10)
            .title("稳定核心")
            .task(Task.item(GTOItems.STABILIZER_CORE, 32))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Draconium, 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest DRACONIC_CORE = CHAPTER.quest("draconic_core", q -> q
            .at(10, 8)
            .title("龙芯")
            .task(Task.item(GTOItems.DRACONIC_CORE, 16))
            .scaledReward(GTOItems.QUANTUMCHROMODYNAMIC_PROTECTIVE_PLATING, 4)
            .dependsOn(STABILIZER_CORE));

    public static final Quest DRAGON_STABILIZER_CORE = CHAPTER.quest("dragon_stabilizer_core", q -> q
            .at(12, 8)
            .title("龙之稳定核心")
            .task(Task.item(GTOItems.DRAGON_STABILIZER_CORE, 16))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.HexaphaseCopper, 8)
            .dependsOn(STABILIZER_CORE));

    public static final Quest ANNIHILATE_CORE = CHAPTER.quest("annihilate_core", q -> q
            .at(14, 6)
            .title("湮灭核心")
            .task(Task.item(GTOBlocks.ANNIHILATE_CORE, 4))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.AstralTitanium, 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest ANNIHILATION_CONSTRAINER = CHAPTER.quest("annihilation_constrainer", q -> q
            .at(14, 4)
            .title("湮灭约束器")
            .task(Task.item(GTOItems.ANNIHILATION_CONSTRAINER, 64))
            .scaledReward(GTOItems.CONTAINED_KERR_NEWMANN_SINGULARITY, 4)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest TECH_DRAGON_CORE = CHAPTER.quest("tech_dragon_core", q -> q
            .at(10, 4)
            .title("龙之能量核心")
            .task(Task.techNode(BaseNodes.DragonCore))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.Astrium, 8)
            .dependsOn(NewOpvChapter.MAX_HULL, NewUxvChapter.TECH_SUPRACAUSAL_TECH));

    public static final QuestLink TECH_DRAGON_CORE_COPY_1 = CHAPTER.copy(TECH_DRAGON_CORE, 8, 2);

    public static final Quest WYVERN_CORE = CHAPTER.quest("wyvern_core", q -> q
            .at(12, 6)
            .milestone()
            .title("双足飞龙核心")
            .task(Task.item(GTOItems.WYVERN_CORE, 32))
            .scaledReward(GTOItems.MANIFOLD_OSCILLATORY_POWER_CELL, 2)
            .dependsOn(DRACONIC_CORE, DRAGON_STABILIZER_CORE, ANNIHILATE_CORE, ANNIHILATION_CONSTRAINER, TECH_DRAGON_CORE));

    public static final Quest TECH_AWAKENED_CORE = CHAPTER.quest("tech_awakened_core", q -> q
            .at(6, 2)
            .title("觉醒核心研究")
            .task(Task.techNode(BaseNodes.AwakenedCore))
            .scaledReward(GTOItems.MACROWORMHOLE_GENERATOR, 1)
            .dependsOn(TECH_DRAGON_CORE_COPY_1));

    public static final Quest AWAKENED_CORE = CHAPTER.quest("awakened_core", q -> q
            .at(6, 4)
            .title("觉醒核心")
            .task(Task.item(GTOItems.AWAKENED_CORE, 16))
            .scaledReward(GTOItems.CHAOS_SHARD, 4)
            .dependsOn(WYVERN_CORE, TECH_AWAKENED_CORE));

    public static final Quest TECH_CHAOS_CORE = CHAPTER.quest("tech_chaos_core", q -> q
            .at(4, 2)
            .title("混沌核心研究")
            .task(Task.techNode(BaseNodes.ChaosCore))
            .scaledReward(GTOItems.STABILIZED_WORMHOLE_GENERATOR, 1)
            .dependsOn(TECH_AWAKENED_CORE));

    public static final Quest CHAOTIC_CORE = CHAPTER.quest("chaotic_core", q -> q
            .at(4, 4)
            .title("混沌核心")
            .task(Task.item(GTOItems.CHAOTIC_CORE, 4))
            .scaledReward(GTOItems.NUCLEAR_STAR, 4)
            .dependsOn(AWAKENED_CORE, TECH_CHAOS_CORE));

    public static final Quest TECH_CREATE = CHAPTER.quest("tech_create", q -> q
            .at(2, 4)
            .title("创造")
            .task(Task.techNode(BaseNodes.Create))
            .scaledReward(TagPrefix.plate, GTOMaterials.Eternity, 2)
            .dependsOn(TECH_CHAOS_CORE));

    public static final Quest SUPRACHRONAL_MAINFRAME_COMPLEX = CHAPTER.quest("suprachronal_mainframe_complex", q -> q
            .at(4, 6)
            .milestone()
            .title("创造主机")
            .task(Task.item(GTOItems.SUPRACHRONAL_MAINFRAME_COMPLEX, 4))
            .scaledReward(TagPrefix.plate, GTOMaterials.MagnetohydrodynamicallyConstrainedStarMatter, 4)
            .dependsOn(CHAOTIC_CORE, MHDCSM, TECH_CREATE));

    public static final Quest TECH_ULTIMATE_BATTERY6 = CHAPTER.quest("tech_ultimate_battery6", q -> q
            .at(0, 8)
            .title("终极电池VI研究")
            .task(Task.techNode(EnergyNodes.UltimateBattery6))
            .scaledReward(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT, 1)
            .dependsOn(NewOpvChapter.MAX_HULL));

    public static final Quest MEGA_MAX_BATTERY = CHAPTER.quest("mega_max_battery", q -> q
            .at(2, 8)
            .title("兆·终极电池")
            .task(Task.item(GTOItems.MEGA_MAX_BATTERY, 16))
            .scaledReward(GTOItems.FM_WAFER, 32)
            .dependsOn(NewOpvChapter.MAX_HULL, TECH_ULTIMATE_BATTERY6));

    public static final Quest CREATE_ULTIMATE_BATTERY = CHAPTER.quest("create_ultimate_battery", q -> q
            .at(2, 6)
            .milestone()
            .title("创造电池")
            .task(Task.item(GTOItems.CREATE_ULTIMATE_BATTERY))
            .reward(
                    Reward.item(TagPrefix.block, GTOMaterials.MagnetohydrodynamicallyConstrainedStarMatter, 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(SUPRACHRONAL_MAINFRAME_COMPLEX, MEGA_MAX_BATTERY, MAX_EMITTER_FIELD));

    public static final Quest HAPPY_WATER_GOD = CHAPTER.quest("happy_water_god", q -> q
            .at(0, 6)
            .milestone()
            .title("无限快乐水之神雕像")
            .task(Task.item(GTOBlocks.HAPPY_WATER_GOD_STATUE))
            .reward(
                    Reward.xpLevels(100),
                    Reward.command("/function firework:stonelaunch").elevated().silent().autoClaim(AutoClaim.INVISIBLE),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(CREATE_ULTIMATE_BATTERY));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewOpvChapter.MAX_HULL, 0, 0))
                .mainLine(NewOpvChapter.MAX_HULL, DIMENSION_SHIFTED_SUPER_FLUID, MAGNETIC_ENERGY_REACTION_FURNACE, NYARLATHOTEPS_TENTACLE, MHDCSM, WYVERN_CORE, MAX_ENERGY_HATCH, SUPRACHRONAL_MAINFRAME_COMPLEX, CREATE_ULTIMATE_BATTERY, HAPPY_WATER_GOD));
    }
}
