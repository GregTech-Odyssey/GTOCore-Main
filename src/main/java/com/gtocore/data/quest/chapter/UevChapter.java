package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockE;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.world.item.Items;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.hepdd.gtmthings.data.CreativeMachines;

import static com.gtolib.api.quest.QuestStyle.*;

public final class UevChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("uev", c -> c
            .title("13. 极超压(UEV)")
            .subtitle("Tier10-UEV")
            .icon(MultiBlockA.SPS_CRAFTING)
            .hideQuestsUntilDepsVisible());

    public static final Quest OBSIDIAN_MATRIX = CHAPTER.quest("obsidian_matrix", q -> q
            .at(-8, 1)
            .subtitle("六级数据载体")
            .task(Task.item(GTOItems.OBSIDIAN_MATRIX)));

    public static final Quest CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT = CHAPTER.quest("closed_timelike_curve_guidance_unit", q -> q
            .at(-6, 1)
            .subtitle("七级数据载体(最高级)")
            .task(Task.item(GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT)));

    public static final Quest ATOMIC_ARCHIVES = CHAPTER.quest("atomic_archives", q -> q
            .at(-10, 1)
            .subtitle("五级数据载体")
            .task(Task.item(GTOItems.ATOMIC_ARCHIVES)));

    public static final Quest QUANTUM_FORCE_TRANSFORMER = CHAPTER.quest("quantum_force_transformer", q -> q
            .at(0, -0.5)
            .task(Task.item(MultiBlockA.QUANTUM_FORCE_TRANSFORMER)));

    public static final Quest EYE_OF_HARMONY = CHAPTER.quest("eye_of_harmony", q -> q
            .at(2, -2)
            .subtitle("万物大一统")
            .desc(d -> d
                    .line("我们修改了鸿蒙之眼的机制，目前你无需提升鸿蒙之眼的等级即可制作任意等级的配方")
                    .line()
                    .line("{不需要挂机等待它升级}", GRAY.italic()))
            .task(Task.item(MultiBlockD.EYE_OF_HARMONY)));

    public static final Quest UEV_FUSION_REACTOR = CHAPTER.quest("uev_fusion_reactor", q -> q
            .at(-4, -2)
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UEV])));

    public static final Quest SPS_CRAFTING = CHAPTER.quest("sps_crafting", q -> q
            .at(-4, -0.5)
            .task(Task.item(MultiBlockA.SPS_CRAFTING)));

    public static final Quest BEDROCK_DRILLING_RIG = CHAPTER.quest("bedrock_drilling_rig", q -> q
            .at(-4, 1)
            .task(Task.item(MultiBlockD.BEDROCK_DRILLING_RIG)));

    public static final Quest MASS_FABRICATOR = CHAPTER.quest("mass_fabricator", q -> q
            .at(-2, -2)
            .task(Task.item(MultiBlockA.MASS_FABRICATOR)));

    public static final Quest SPACE_PROBE_SURFACE_RECEPTION = CHAPTER.quest("space_probe_surface_reception", q -> q
            .at(-2, -0.5)
            .task(Task.item(MultiBlockD.SPACE_PROBE_SURFACE_RECEPTION)));

    public static final Quest DYSON_SPHERE_LAUNCH_SILO = CHAPTER.quest("dyson_sphere_launch_silo", q -> q
            .at(0, 1)
            .task(Task.item(GeneratorMultiblock.DYSON_SPHERE_LAUNCH_SILO)));

    public static final Quest MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE = CHAPTER.quest("magnetic_confinement_dimensionality_shock_device", q -> q
            .at(-2, 1)
            .task(Task.item(MultiBlockA.MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE)));

    public static final Quest ATOMIC_ENERGY_EXCITATION_PLANT = CHAPTER.quest("atomic_energy_excitation_plant", q -> q
            .at(-4, 2.5)
            .task(Task.item(MultiBlockA.ATOMIC_ENERGY_EXCITATION_PLANT)));

    public static final Quest STAR_ULTIMATE_MATERIAL_FORGE_FACTORY = CHAPTER.quest("star_ultimate_material_forge_factory", q -> q
            .at(-2, 2.5)
            .task(Task.item(MultiBlockE.STAR_ULTIMATE_MATERIAL_FORGE_FACTORY)));

    public static final Quest HYPERDIMENSIONAL_PLASMA_FUSION_CORE = CHAPTER.quest("hyperdimensional_plasma_fusion_core", q -> q
            .at(-2, 4)
            .task(Task.item(MultiBlockD.HYPERDIMENSIONAL_PLASMA_FUSION_CORE)));

    public static final Quest DRAGON_EGG_COPIER = CHAPTER.quest("dragon_egg_copier", q -> q
            .at(0, -2)
            .task(Task.item(MultiBlockA.DRAGON_EGG_COPIER)));

    public static final Quest MAX_NEUTRON_COMPRESSOR = CHAPTER.quest("max_neutron_compressor", q -> q
            .at(2, -0.5)
            .icon(GTOMachines.NEUTRON_COMPRESSOR[GTValues.MAX])
            .task(Task.item(GTOMachines.NEUTRON_COMPRESSOR[GTValues.MAX]), Task.item(GTOItems.INFINITY_SINGULARITY))
            .reward(Reward.item(MultiBlockC.NEUTRON_COMPRESSOR)));

    public static final Quest ANNIHILATE_GENERATOR = CHAPTER.quest("annihilate_generator", q -> q
            .at(2, 1)
            .task(Task.item(GeneratorMultiblock.ANNIHILATE_GENERATOR)));

    public static final Quest COMPREHENSIVE_TOMBARTHITE_PROCESSING_FACILITY = CHAPTER.quest("comprehensive_tombarthite_processing_facility", q -> q
            .at(-4, 4)
            .task(Task.item(MultiBlockA.ADVANCED_RARE_EARTH_CENTRIFUGAL)));

    public static final Quest WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE = CHAPTER.quest("welcome_to_the_extreme_overpressure_stage", q -> q
            .at(-10, -5)
            .title("欢迎来到{极超压}阶段", DARK_GREEN)
            .subtitle("{你需要天马行空般的想象力}", DARK_GREEN)
            .desc(d -> d.line("未完待续..."))
            .task(Task.item(GTMachines.HULL[GTValues.UEV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(UhvChapter.QUANTANIUM_INGOT));

    public static final Quest Q_4BEE8DA7 = CHAPTER.quest("q_4bee8da7", q -> q
            .at(-6, -4)
            .desc(d -> d
                    .line("恭喜你！当你到达这里的时候，目前完善的内容都已体验完毕！")
                    .line()
                    .line("当然，你可以继续游玩目前尚未完善的内容，直到做出右边的毕业物品为止。你可能会遭遇部分物品无法制作，需要创造获取的情况。")
                    .line()
                    .line("{/gtocore givecell 可快捷获取所需物品的创造元件}", GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE, UhvChapter.OPTICAL_PROCESSOR_CONTROLLER));

    public static final Quest MEGASTRUCTURES = CHAPTER.quest("megastructures", q -> q
            .at(-6, -6)
            .title("巨构们")
            .desc(d -> d.line("你可以开始着手把机器都换成巨构了，这将大大提升你的生产效率"))
            .task(Task.item(MultiBlockF.FIELD_EXTRUDER_FACTORY))
            .dependsOn(Q_4BEE8DA7));

    public static final Quest LHC = CHAPTER.quest("lhc", q -> q
            .at(-8, -6)
            .desc(d -> d.line("仅供观赏，目前无任何作用"))
            .task(Task.item(MultiBlockH.LHC))
            .dependsOn(MEGASTRUCTURES)
            .optional());

    public static final Quest NYARLATHOTEPS_TENTACLE = CHAPTER.quest("nyarlathoteps_tentacle", q -> q
            .at(-2, -6)
            .desc(d -> d.line("单独提一下这个机器，你需要{重夸克简并物质}才可以制作其方块中的夸克管道，它要到UIV阶段才能制作", GOLD))
            .task(Task.item(MultiBlockD.NYARLATHOTEPS_TENTACLE))
            .dependsOn(MEGASTRUCTURES));

    public static final Quest EXOTIC_MAINFRAME = CHAPTER.quest("exotic_mainframe", q -> q
            .at(-4, -4)
            .task(Task.item(GTOItems.EXOTIC_MAINFRAME))
            .dependsOn(Q_4BEE8DA7));

    public static final Quest COSMIC_MAINFRAME = CHAPTER.quest("cosmic_mainframe", q -> q
            .at(-2, -4)
            .task(Task.item(GTOItems.COSMIC_MAINFRAME))
            .dependsOn(EXOTIC_MAINFRAME));

    public static final Quest SUPRACAUSAL_MAINFRAME = CHAPTER.quest("supracausal_mainframe", q -> q
            .at(0, -4)
            .task(Task.item(GTOItems.SUPRACAUSAL_MAINFRAME))
            .dependsOn(COSMIC_MAINFRAME));

    public static final Quest SUPRACHRONAL_CIRCUIT_MAX = CHAPTER.quest("suprachronal_circuit_max", q -> q
            .at(2, -4)
            .desc(d -> d.line("可以低价大批量制作"))
            .task(Task.item(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.MAX]))
            .dependsOn(SUPRACAUSAL_MAINFRAME));

    public static final Quest ADVANCED_INTEGRATED_ORE_PROCESSOR = CHAPTER.quest("advanced_integrated_ore_processor", q -> q
            .at(-4, 5.5)
            .task(Task.item(MultiBlockA.ADVANCED_INTEGRATED_ORE_PROCESSOR)));

    public static final Quest DATA_CENTER = CHAPTER.quest("data_center", q -> q
            .at(-4, 7)
            .task(Task.item(ExResearchMachines.DATA_CENTER)));

    public static final Quest MICROCOSM = CHAPTER.quest("microcosm", q -> q
            .at(-6, 2.5)
            .subtitle("??级数据载体")
            .desc(d -> d.line("你不该发现的"))
            .task(Task.item(GTOItems.MICROCOSM))
            .optional().invisible());

    public static final Quest ROAD_OF_HEAVEN = CHAPTER.quest("road_of_heaven", q -> q
            .at(-2, 5.5)
            .task(Task.item(MultiBlockB.ROAD_OF_HEAVEN)));

    public static final Quest ELEMENT_COPYING = CHAPTER.quest("element_copying", q -> q
            .at(-2, 7)
            .task(Task.item(MultiBlockA.ELEMENT_COPYING)));

    public static final Quest SUPRACHRONAL_MAINFRAME_COMPLEX = CHAPTER.quest("suprachronal_mainframe_complex", q -> q
            .at(4, -4)
            .task(Task.item(GTOItems.SUPRACHRONAL_MAINFRAME_COMPLEX))
            .dependsOn(SUPRACHRONAL_CIRCUIT_MAX));

    public static final Quest CREATE_ULTIMATE_BATTERY = CHAPTER.quest("create_ultimate_battery", q -> q
            .at(4, -2)
            .task(Task.item(GTOItems.CREATE_ULTIMATE_BATTERY))
            .dependsOn(SUPRACHRONAL_MAINFRAME_COMPLEX)
            .optional());

    public static final Quest CHAOTIC_ENERGY_CORE = CHAPTER.quest("chaotic_energy_core", q -> q
            .at(4, -0.5)
            .desc(d -> d.line("创造之门以及后面的东西都没了，所以直接送你命令方块"))
            .task(Task.item(GTOItems.CHAOTIC_ENERGY_CORE))
            .reward(Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(Items.CHAIN_COMMAND_BLOCK)), 1))
            .dependsOn(CREATE_ULTIMATE_BATTERY)
            .optional());

    public static final Quest CREATIVE_LASER_HATCH = CHAPTER.quest("creative_laser_hatch", q -> q
            .at(4, 1)
            .subtitle("毕业了")
            .task(Task.item(CreativeMachines.CREATIVE_LASER_INPUT_HATCH))
            .reward(
                    Reward.item(GTMachines.CREATIVE_ITEM, 99L),
                    Reward.item(GTMachines.CREATIVE_FLUID, 99L),
                    Reward.item(CreativeMachines.CREATIVE_FLUID_INPUT_HATCH, 99L),
                    Reward.item(CreativeMachines.CREATIVE_ITEM_INPUT_BUS, 99L),
                    Reward.item(CreativeMachines.CREATIVE_LASER_INPUT_HATCH, 99L),
                    Reward.item(GTMachines.CREATIVE_COMPUTATION_PROVIDER, 99L),
                    Reward.item(GTOItems.ALL_FLUIDS_CELL))
            .dependsOn(CHAOTIC_ENERGY_CORE)
            .optional());

    public static final Quest BARYONIC_PERFECTION_WATER_EXCHANGE = CHAPTER.quest("baryonic_perfection_water_exchange", q -> q
            .at(-8, -0.5)
            .icon(GTOMaterials.DegassedWater.getBucket())
            .title("重子完美水 兑换处")
            .desc(d -> d.line("夸克释放催化剂目前还做不了，所以只要你做出了7级水，就可以直接来这里领取8级水的元件"))
            .task(Task.item(GTOMaterials.DegassedWater.getBucket()))
            .reward(
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEFluidKey.of(GTOMaterials.BaryonicPerfectionWater.getFluid())), 1))
            .hideDependentLines());

    public static final Quest Q_635BFF4E = CHAPTER.quest("q_635bff4e", q -> q
            .at(2, -6)
            .desc(d -> d.line("缺失配方的材料，直接送你了"))
            .task(Task.checkmark())
            .reward(
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEFluidKey.of(GTOMaterials.SpacetimeContinuum.getFluid())), 1),
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEFluidKey.of(GTOMaterials.DimensionAllyshiftedSuperFluid.getFluid())), 1),
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(GTOItems.AWAKENED_CORE)), 1),
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(ChemicalHelper.getItem(TagPrefix.ingot, GTOMaterials.ChaosInfinityAlloy))), 1),
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(GTOItems.CHAOTIC_CORE)), 1))
            .dependsOn(SUPRACAUSAL_MAINFRAME));

    public static final Quest SWARM_CORE = CHAPTER.quest("swarm_core", q -> q
            .at(0, 2.5)
            .task(Task.item(MultiBlockF.SWARM_CORE)));

    public static final Quest ADVANCED_MASS_FABRICATOR = CHAPTER.quest("advanced_mass_fabricator", q -> q
            .at(0, 4)
            .task(Task.item(MultiBlockA.ADVANCED_MASS_FABRICATOR)));

    public static final Quest VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH_EXCHANGE = CHAPTER.quest("virtual_universe_data_access_hatch_exchange", q -> q
            .at(-6, -0.5)
            .icon(ExResearchMachines.VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH)
            .title(" 虚拟宇宙数据访问仓 兑换处")
            .desc(d -> d.line("六级和七级数据载体需要用到这个访问仓，它还暂时做不出来，送你了"))
            .task(Task.item(GTOItems.OBSIDIAN_MATRIX))
            .reward(
                    Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(ExResearchMachines.VIRTUAL_UNIVERSE_DATA_ACCESS_HATCH)), 1)));

    public static final Quest ANTIMATTER_CONTAINMENT_CASING_EXCHANGE = CHAPTER.quest("antimatter_containment_casing_exchange", q -> q
            .at(-6, -2)
            .icon(GTOBlocks.ANTIMATTER_CONTAINMENT_CASING)
            .title("反物质隔离机械方块 兑换处")
            .desc(d -> d.line("人造恒星需要用到它们"))
            .task(Task.item(GeneratorMultiblock.ANNIHILATE_GENERATOR))
            .reward(Reward.stack(() -> EPPItemAndBlock.INFINITY_CELL.getRecordCell(AEItemKey.of(GTOBlocks.ANTIMATTER_CONTAINMENT_CASING)), 1)));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UEV, -4, -4.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UIV, -2, -4.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UXV, 0, -4.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_OPV, 2, -4.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MAX, 4, -4.5, 1, 1))
                .link(QuestLink.of(UhvChapter.OPTICAL_PROCESSOR_CONTROLLER, -10, -3)));
    }
}
