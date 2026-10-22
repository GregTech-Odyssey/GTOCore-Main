package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOFluids;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class ZpmChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("zpm", c -> c
            .title("10. 零点压(ZPM)")
            .subtitle("Tier7-ZPM")
            .icon(MultiBlockD.NANO_FORGE)
            .hideQuestsUntilDepsVisible());

    public static final Quest MEAT_DUST = CHAPTER.quest("meat_dust", q -> q
            .at(-4, -2)
            .desc(d -> d.line("没有肉末？试试{渔场}！", GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Meat)));

    public static final Quest BACTERIAL_GROWTH_MEDIUM_BUCKET = CHAPTER.quest("bacterial_growth_medium_bucket", q -> q
            .at(-6, -2)
            .icon(GTOMaterials.BacterialGrowthMedium.getBucket())
            .task(Task.item(GTOMaterials.BacterialGrowthMedium.getBucket()).optional(), Task.checkmark())
            .dependsOn(MEAT_DUST));

    public static final Quest ZPM_ENERGY_INPUT_HATCH = CHAPTER.quest("zpm_energy_input_hatch", q -> q
            .at(-4.5, -4)
            .desc(d -> d.line("很好，我想你懂要做什么"))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.ZPM]))
            .dependsOn(LuvChapter.RESEARCH_STATION));

    public static final Quest ALGAE_FARM = CHAPTER.quest("algae_farm", q -> q
            .at(-11, 0)
            .desc(d -> d.line("不需要电力的机器"))
            .task(Task.item(MultiBlockG.ALGAE_FARM))
            .dependsOn(HvChapter.WELCOME_TO_THE_HIGH_VOLTAGE_STAGE));

    public static final Quest ALGAE = CHAPTER.quest("algae", q -> q
            .at(-11, 2)
            .title("藻类")
            .desc(d -> d.line("以你现在的科技水平，应当不介意多做几台农场吧！"))
            .task(
                    Task.item(GTOItems.BLUE_ALGAE),
                    Task.item(GTOItems.GOLD_ALGAE),
                    Task.item(GTOItems.BROWN_ALGAE),
                    Task.item(GTOItems.GREEN_ALGAE),
                    Task.item(GTOItems.RED_ALGAE))
            .dependsOn(ALGAE_FARM));

    public static final Quest BIO_NUTRIENT = CHAPTER.quest("bio_nutrient", q -> q
            .at(-11, 4)
            .title("生物养料")
            .subtitle("这些生物真难伺候！")
            .desc(d -> d.line("可能是养料，也可能是魔法... 总之我们需要这些！"))
            .task(
                    Task.item(GTOFluids.NUTRIENT_DISTILLATION.get().getBucket()),
                    Task.item(GTOFluids.CLOUD_SEED_CONCENTRATED.get().getBucket()),
                    Task.item(GTOFluids.FIRE_WATER.get().getBucket()),
                    Task.item(GTOFluids.VAPOR_OF_LEVITY.get().getBucket()))
            .dependsOn(ALGAE));

    public static final Quest NUCLEAR_WASTE_BROTHERS = CHAPTER.quest("nuclear_waste_brothers", q -> q
            .at(6, 2)
            .title("核废料兄弟")
            .task(Task.item(TagPrefix.dustTiny, GTMaterials.Protactinium), Task.item(TagPrefix.dustTiny, GTMaterials.Neptunium))
            .dependsOn(IvChapter.NUCLEAR_WASTE));

    public static final Quest WELCOME_TO_THE_ZERO_PRESSURE_PHASE = CHAPTER.quest("welcome_to_the_zero_pressure_phase", q -> q
            .at(-9, -4)
            .title("欢迎来到{零点压}阶段", RED)
            .subtitle("{你需要天马行空般的想象力}", RED)
            .desc(d -> d
                    .line("这一章节你将体验到一门独特的产线——{生}{物}{线}", GREEN, DARK_GREEN, GREEN)
                    .line()
                    .line("普通的{晶体处理器}已经接近无机物的极限... 或许{菌}{群}可以帮我们做些计算？", GOLD, GREEN, DARK_GREEN)
                    .line()
                    .line("在GT，所有的科技就像{魔}{法}一样", GREEN, DARK_GREEN)
                    .line()
                    .line("你的目标是制作并{增}{殖}{干细胞}", GREEN, DARK_GREEN, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.ZPM]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(LuvChapter.NAQUADAH_ALLOY_INGOT));

    public static final Quest ZPM_FUSION_REACTOR = CHAPTER.quest("zpm_fusion_reactor", q -> q
            .at(-2, -4)
            .subtitle("金粉甜甜圈")
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.ZPM]))
            .dependsOn(ZPM_ENERGY_INPUT_HATCH, LuvChapter.CRYSTAL_PROCESSOR_MAINFRAME, WELCOME_TO_THE_ZERO_PRESSURE_PHASE));

    public static final Quest TRITANIUM_INGOT = CHAPTER.quest("tritanium_ingot", q -> q
            .at(-2, -2)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Tritanium))
            .dependsOn(ZPM_FUSION_REACTOR, LuvChapter.DURANIUM_INGOT));

    public static final Quest STERILIZING_FILTER_CASING = CHAPTER.quest("sterilizing_filter_casing", q -> q
            .at(-2, 0)
            .desc(d -> d.line("实际上整个生物线几乎全部都需要无菌超净间"))
            .task(Task.item(GTBlocks.FILTER_CASING_STERILE))
            .dependsOn(TRITANIUM_INGOT));

    public static final Quest TRITANIUM_COIL_BLOCK = CHAPTER.quest("tritanium_coil_block", q -> q
            .at(0, -2)
            .task(Task.item(GTBlocks.COIL_TRITANIUM))
            .dependsOn(TRITANIUM_INGOT));

    public static final Quest ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT = CHAPTER.quest("enriched_naquadah_trinium_europium_duranide_ingot", q -> q
            .at(2, -2)
            .subtitle("UV超导")
            .task(Task.item(TagPrefix.ingot, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide))
            .dependsOn(TRITANIUM_COIL_BLOCK));

    public static final Quest NETWORK_SWITCH = CHAPTER.quest("network_switch", q -> q
            .at(4, 0)
            .desc(d -> d
                    .line("做出它后你可以突破单台计算机可以获取的{算力上限}。是时候铺设计算机以获取{算力}了！", YELLOW, AQUA)
                    .line()
                    .line("在上面可以安装{算力数据仓}来实现算力分发，别忘了给提供算力的计算机安装{桥接组件}！", GOLD, GOLD))
            .task(Task.item(GTResearchMachines.NETWORK_SWITCH))
            .dependsOn(ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT, LuvChapter.HPCA_ADVANCED_COMPUTATION_COMPONENT)
            .hideDependentLines());

    public static final Quest DARMSTADTIUM_INGOT = CHAPTER.quest("darmstadtium_ingot", q -> q
            .at(2, -4)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Darmstadtium))
            .dependsOn(ZPM_FUSION_REACTOR));

    public static final Quest STERILE_CLEANING_MAINTENANCE_HATCH = CHAPTER.quest("sterile_cleaning_maintenance_hatch", q -> q
            .at(0, 0)
            .desc(d -> d.line("无菌超净间不是超净间的上位替代，但无菌维护仓是超净维护仓的上位替代。"))
            .task(Task.item(GTOMachines.STERILE_CLEANING_MAINTENANCE_HATCH))
            .dependsOn(STERILIZING_FILTER_CASING));

    public static final Quest LARGE_INCUBATOR = CHAPTER.quest("large_incubator", q -> q
            .at(-11, -2)
            .desc(d -> d.line("尽管你可能没有做过小型的，但我依然建议你直接做大型的"))
            .task(Task.item(MultiBlockD.LARGE_INCUBATOR))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE));

    public static final Quest RADIATION_HATCH = CHAPTER.quest("radiation_hatch", q -> q
            .at(-11, -4)
            .subtitle("小心辐射")
            .desc(d -> d
                    .line("按U查看EMI来控制辐射原料和剂量，考验你数学基本功的时候到了")
                    .line()
                    .line("在容器内保持1根以上的杆会一直提供恒定的辐射"))
            .task(Task.item(GTOMachines.RADIATION_HATCH))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE));

    public static final Quest HYDROXYAPATITE_CERAMIC_FLAKE = CHAPTER.quest("hydroxyapatite_ceramic_flake", q -> q
            .at(0, 2)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.HydroxyapatiteCeramic))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE)
            .hideDependencyLines());

    public static final Quest LAFIUM_INGOT = CHAPTER.quest("lafium_ingot", q -> q
            .at(2, 0)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Lafium))
            .dependsOn(TRITANIUM_COIL_BLOCK));

    public static final Quest STERILIZED_PETRI_DISH = CHAPTER.quest("sterilized_petri_dish", q -> q
            .at(-2, 2)
            .desc(d -> d.line("无水乙醇可以通过乙醇+自循环沸石产生"))
            .task(Task.item(GTOItems.STERILIZED_PETRI_DISH))
            .dependsOn(STERILIZING_FILTER_CASING));

    public static final Quest PREPARATION_PETRI_DISH = CHAPTER.quest("preparation_petri_dish", q -> q
            .at(-4, 0)
            .task(Task.item(GTOItems.PREPARATION_PETRI_DISH))
            .dependsOn(LuvChapter.PRECISION_ASSEMBLER, STERILIZED_PETRI_DISH)
            .requireAny());

    public static final Quest WETWARE_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("wetware_printed_circuit_board", q -> q
            .at(0, 4)
            .task(Task.item(GTItems.WETWARE_CIRCUIT_BOARD))
            .dependsOn(HYDROXYAPATITE_CERAMIC_FLAKE, STERILIZED_PETRI_DISH));

    public static final Quest DATA_MODULE = CHAPTER.quest("data_module", q -> q
            .at(2, 4)
            .desc(d -> d.line("三级数据载体"))
            .task(Task.item(GTItems.TOOL_DATA_MODULE))
            .dependsOn(WETWARE_PRINTED_CIRCUIT_BOARD));

    public static final Quest FUEL_REFINING_COMPLEX = CHAPTER.quest("fuel_refining_complex", q -> q
            .at(2, 2)
            .task(Task.item(MultiBlockB.FUEL_REFINING_COMPLEX))
            .dependsOn(DATA_MODULE, NETWORK_SWITCH));

    public static final Quest TIER_7_ROCKET = CHAPTER.quest("tier_7_rocket", q -> q
            .at(2, 6)
            .icon(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_7_ROCKET.get())
            .desc(d -> d.line("只需要{T3}器官", AQUA))
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_7_ROCKET.get()), Task.item(GTOMaterials.StellarEnergyRocketFuel.getBucket()))
            .dependsOn(DATA_MODULE));

    public static final Quest MODULAR_CONFIGURATION_MAINTENANCE_HATCH = CHAPTER.quest("modular_configuration_maintenance_hatch", q -> q
            .at(-11, -6)
            .subtitle("不需要为一个环境单开一台机器了")
            .task(Task.item(GTOMachines.MODULAR_CONFIGURATION_MAINTENANCE_HATCH))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE));

    public static final Quest WOOD_DISTILLATION = CHAPTER.quest("wood_distillation", q -> q
            .at(-7.5, -2.5)
            .subtitle("木头的一步处理")
            .task(Task.item(MultiBlockB.WOOD_DISTILLATION))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE));

    public static final Quest MUTAGEN_BUCKET = CHAPTER.quest("mutagen_bucket", q -> q
            .at(-7, 6)
            .icon(GTMaterials.Mutagen.getBucket())
            .task(Task.item(GTMaterials.Mutagen.getBucket()).optional(), Task.checkmark())
            .dependsOn(STERILIZING_FILTER_CASING)
            .hideDependencyLines());

    public static final Quest Q_3AD590B7 = CHAPTER.quest("q_3ad590b7", q -> q
            .at(4, -2)
            .icon(SpaceMultiblock.ORBITAL_SMELTING_FACILITY)
            .desc(d -> d
                    .line("占地更大，速度更快的冶炼冷却机器，允许安装{并行控制仓}{（终于！）}，{加速仓}和至多{8个}{能源仓}", GOLD, ITALIC, GOLD, GREEN, GOLD)
                    .line()
                    .line("它们也可以贴上升级模块"))
            .task(Task.checkmark().title("冶炼三件套"))
            .dependsOn(ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT));

    public static final Quest MEGA_BLAST_FURNACE = CHAPTER.quest("mega_blast_furnace", q -> q
            .at(4.75, -2.75)
            .task(Task.item(GCYMMachines.MEGA_BLAST_FURNACE))
            .dependsOn(Q_3AD590B7));

    public static final Quest MEGA_ALLOY_BLAST_SMELTER = CHAPTER.quest("mega_alloy_blast_smelter", q -> q
            .at(4.75, -1.25)
            .task(Task.item(GCYMMachines.MEGA_ALLOY_BLAST_SMELTER))
            .dependsOn(Q_3AD590B7));

    public static final Quest MEGA_VACUUM_FREEZER = CHAPTER.quest("mega_vacuum_freezer", q -> q
            .at(5.5, -2)
            .task(Task.item(GCYMMachines.MEGA_VACUUM_FREEZER))
            .dependsOn(Q_3AD590B7));

    public static final Quest ZPM_ASSEMBLER = CHAPTER.quest("zpm_assembler", q -> q
            .at(-7, -6)
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.ZPM]))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE, LuvChapter.EUROPIUM_INGOT));

    public static final Quest VISIT_GLACIO = CHAPTER.quest("visit_glacio", q -> q
            .at(4, 6)
            .icon(ModItems.GLACIO_GLOBE.get())
            .task(Task.dimension(Dimension.GLACIO).title("{访问霜原星}", LIGHT_PURPLE).icon(ModItems.GLACIO_GLOBE.get()))
            .dependsOn(TIER_7_ROCKET)
            .hideDependentLines());

    public static final Quest GLACIO_SPIRIT = CHAPTER.quest("glacio_spirit", q -> q
            .at(-5, 6)
            .desc(d -> d
                    .line("霜原星的{羊}概率掉落", GOLD)
                    .line()
                    .line("需要{倍增}产线，主要作为{增殖}各种细胞的原材料", YELLOW, GREEN))
            .task(Task.item(GTOItems.GLACIO_SPIRIT))
            .dependsOn(VISIT_GLACIO));

    public static final Quest ZPM_FORCE_FIELD_GENERATOR_LAUNCHER_AND = CHAPTER.quest("zpm_force_field_generator_launcher_and", q -> q
            .at(-4.5, -6)
            .title("{ZPM}力场发生器，发射器和传感器", ZPM)
            .task(Task.item(GTItems.EMITTER_ZPM), Task.item(GTItems.SENSOR_ZPM), Task.item(GTItems.FIELD_GENERATOR_ZPM))
            .dependsOn(LuvChapter.RESEARCH_STATION)
            .hideDependentLines());

    public static final Quest BIOCHEMICAL_REACTION = CHAPTER.quest("biochemical_reaction", q -> q
            .at(-9, 0)
            .desc(d -> d
                    .line("通常用于制作第一批生物原料")
                    .line()
                    .line("从有机物直接变出生命来，很神奇吧！"))
            .task(Task.item(MultiBlockC.BIOCHEMICAL_REACTION))
            .dependsOn(WELCOME_TO_THE_ZERO_PRESSURE_PHASE, ZPM_FORCE_FIELD_GENERATOR_LAUNCHER_AND));

    public static final Quest BIOCHEMICAL_EXTRACTION = CHAPTER.quest("biochemical_extraction", q -> q
            .at(-9, 4)
            .desc(d -> d.line("在机器中养殖动物获取需要的原料"))
            .task(Task.item(MultiBlockC.BIOCHEMICAL_EXTRACTION))
            .dependsOn(BIOCHEMICAL_REACTION, BIO_NUTRIENT));

    public static final Quest I_NEED_DNA_3 = CHAPTER.quest("i_need_dna_3", q -> q
            .at(-6, 4)
            .title("我需要DNA#3")
            .desc(d -> d.line("用神化的{捕获附魔}获取普通蛋，并通过生化反应诱导获取外星蛋", YELLOW))
            .task(
                    Task.item(ModItems.PYGRO_BRUTE_SPAWN_EGG.get()),
                    Task.item(ModItems.PYGRO_SPAWN_EGG.get()),
                    Task.item(ModItems.MOGLER_SPAWN_EGG.get()),
                    Task.item(ModItems.SULFUR_CREEPER_SPAWN_EGG.get()))
            .dependsOn(BIOCHEMICAL_EXTRACTION, GLACIO_SPIRIT, MUTAGEN_BUCKET));

    public static final Quest Q_6742F9E1 = CHAPTER.quest("q_6742f9e1", q -> q
            .at(-6, 0)
            .icon(GTItems.BIO_CHAFF)
            .desc(d -> d.line("培养细菌较为复杂，你需要在{生化反应室}中先完成{初代培养}，再转移至{培养缸}进行{增殖}", GOLD, YELLOW, GOLD, YELLOW))
            .task(Task.checkmark().title("培养细菌"))
            .dependsOn(BIOCHEMICAL_REACTION, PREPARATION_PETRI_DISH, BACTERIAL_GROWTH_MEDIUM_BUCKET));

    public static final Quest I_NEED_DNA_2 = CHAPTER.quest("i_need_dna_2", q -> q
            .at(-6, 2)
            .title("我需要DNA#2")
            .desc(d -> d.line("你必须先用生化反应来制作前{16个}细菌", GREEN))
            .task(
                    Task.item(TagPrefix.dust, GTOMaterials.EschericiaColi, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.BifidobacteriumBreve, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.BrevibacteriumFlavium, 16),
                    Task.item(TagPrefix.dust, GTOMaterials.StreptococcusPyogenes, 16))
            .dependsOn(Q_6742F9E1));

    public static final Quest SELF_GROWING = CHAPTER.quest("self_growing", q -> q
            .at(-7.5, 2)
            .icon(GTMaterials.Bacteria.getBucket())
            .title("自行生长")
            .subtitle("给我增殖！")
            .desc(d -> d.line("在做出最初的{16个}细菌之后就可以开始{增殖}。动物细胞则较为简单，只需要{刷怪蛋}即可。", GREEN, YELLOW, GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(GTMaterials.Bacteria.getBucket()).optional(),
                    Task.item(GTOMaterials.RapidlyReplicatingAnimalCells.getBucket()).optional())
            .dependsOn(I_NEED_DNA_3, LARGE_INCUBATOR, Q_6742F9E1)
            .hideDependencyLines());

    public static final Quest PLURIPOTENCY_INDUCTION_GENE_THERAPY_FLUID_BUCKET = CHAPTER.quest("pluripotency_induction_gene_therapy_fluid_bucket", q -> q
            .at(-4, 4)
            .icon(GTOMaterials.PluripotencyInductionGeneTherapyFluid.getBucket())
            .subtitle("基因大杂烩")
            .task(Task.item(GTOMaterials.PluripotencyInductionGeneTherapyFluid.getBucket()).optional(), Task.checkmark())
            .dependsOn(I_NEED_DNA_2, I_NEED_DNA_3));

    public static final Quest I_NEED_DNA_1 = CHAPTER.quest("i_need_dna_1", q -> q
            .at(-4, 2)
            .title("我需要DNA#1")
            .desc(d -> d.line("你必须先用生化反应来制作前{16个}细菌", GREEN))
            .task(Task.item(TagPrefix.dust, GTOMaterials.CupriavidusNecator, 16))
            .dependsOn(Q_6742F9E1));

    public static final Quest STERILIZED_GROWTH_MEDIUM_BUCKET = CHAPTER.quest("sterilized_growth_medium_bucket", q -> q
            .at(-2, 4)
            .icon(GTMaterials.SterileGrowthMedium.getBucket())
            .desc(d -> d
                    .line("是第一批干细胞的制作原料，也是细胞增殖的原料")
                    .line()
                    .line("有更高效的循环产线制作它"))
            .task(Task.item(GTMaterials.SterileGrowthMedium.getBucket()).optional(), Task.checkmark())
            .dependsOn(I_NEED_DNA_1));

    public static final Quest STEM_CELLS = CHAPTER.quest("stem_cells", q -> q
            .at(-2, 6)
            .subtitle("生命自此繁衍")
            .desc(d -> d
                    .line("终于！现在你可以走向{湿件}处理器了！", DARK_RED)
                    .line()
                    .line("做够一定数量之后你就该使用增殖配方来生产细胞了"))
            .task(Task.item(GTItems.STEM_CELLS, 32))
            .dependsOn(PLURIPOTENCY_INDUCTION_GENE_THERAPY_FLUID_BUCKET, STERILIZED_GROWTH_MEDIUM_BUCKET)
            .hideDependencyLines(false));

    public static final Quest NEURO_PROCESSING_UNIT = CHAPTER.quest("neuro_processing_unit", q -> q
            .at(0, 8)
            .task(Task.item(GTItems.NEURO_PROCESSOR))
            .dependsOn(STEM_CELLS, WETWARE_PRINTED_CIRCUIT_BOARD));

    public static final Quest WETWARE_PROCESSOR = CHAPTER.quest("wetware_processor", q -> q
            .at(-2, 8)
            .title("{湿件处理器}", DARK_RED)
            .task(Task.item(GTItems.WETWARE_PROCESSOR_LuV))
            .dependsOn(NEURO_PROCESSING_UNIT));

    public static final Quest WETWARE_PROCESSOR_ASSEMBLY = CHAPTER.quest("wetware_processor_assembly", q -> q
            .at(-4, 8)
            .title("{湿件处理器集群}", DARK_RED)
            .task(Task.item(GTItems.WETWARE_PROCESSOR_ASSEMBLY_ZPM))
            .dependsOn(WETWARE_PROCESSOR));

    public static final Quest WETWARE_PROCESSOR_SUPERCOMPUTER = CHAPTER.quest("wetware_processor_supercomputer", q -> q
            .at(-6, 8)
            .title("{湿件处理器超级计算机}", DARK_RED)
            .task(Task.item(GTItems.WETWARE_SUPER_COMPUTER_UV))
            .dependsOn(WETWARE_PROCESSOR_ASSEMBLY));

    public static final Quest DECAY_HASTENER = CHAPTER.quest("decay_hastener", q -> q
            .at(2, -6)
            .task(Task.item(MultiBlockA.DECAY_HASTENER))
            .dependsOn(LuvChapter.NAQUADRIA_INGOT, DARMSTADTIUM_INGOT, LuvChapter.HPCA_ADVANCED_COMPUTATION_COMPONENT)
            .hideDependentLines());

    public static final Quest SUPER_PARTICLE_COLLIDER = CHAPTER.quest("super_particle_collider", q -> q
            .at(4, 2)
            .task(Task.item(MultiBlockF.SUPER_PARTICLE_COLLIDER))
            .dependsOn(NUCLEAR_WASTE_BROTHERS, LAFIUM_INGOT, DECAY_HASTENER)
            .hideDependentLines());

    public static final Quest WETWARE_PROCESSOR_MAINFRAME = CHAPTER.quest("wetware_processor_mainframe", q -> q
            .at(-9, 8)
            .title("{湿件处理器主机}", DARK_RED)
            .subtitle("千载独步，惟公一人")
            .desc(d -> d
                    .line("先别急着庆祝，你还有一半的路没走呢")
                    .line()
                    .line("不过最难的路已经走完了"))
            .task(Task.item(GTItems.WETWARE_MAINFRAME_UHV))
            .dependsOn(WETWARE_PROCESSOR_SUPERCOMPUTER, NETWORK_SWITCH, SUPER_PARTICLE_COLLIDER));

    public static final Quest ADVANCED_ASSEMBLY_LINE = CHAPTER.quest("advanced_assembly_line", q -> q
            .at(-11, 8)
            .subtitle("真的进阶... 吗？")
            .desc(d -> d
                    .line("显然你已经受够了普通装配线的速度，是时候迭代更新了！")
                    .line()
                    .line("进阶装配线是装配线的并行版本，可以用{巨型输入总线}，而且可以上{激光仓}", AQUA, YELLOW)
                    .line()
                    .line("需要注意，盲目超频不可取，因为进阶装配线是{超损超频}的！换言之，{4倍}的功率只能让配方耗时变为{0.65}倍，但是并行可以正常结算", RED, GREEN, RED))
            .task(Task.item(MultiBlockD.ADVANCED_ASSEMBLY_LINE))
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest NAME_CASTING_MOLD = CHAPTER.quest("name_casting_mold", q -> q
            .at(-11, 6)
            .subtitle("额外的自动化步骤")
            .desc(d -> d
                    .line("你注意到类似于{LuV力场发生器}之类的配方了吗？它们有两组一样的物品，会给有序的装配线带来困难", GOLD)
                    .line()
                    .line("不过别担心！你可以尝试一下{命名模具}。把它放在冲压机中即可实现物品的重命名，或许对你有所帮助", GRAY))
            .task(Task.item(GTItems.SHAPE_MOLD_NAME))
            .dependsOn(ADVANCED_ASSEMBLY_LINE));

    public static final Quest PETROCHEMICAL_PLANT = CHAPTER.quest("petrochemical_plant", q -> q
            .at(-9, 10)
            .subtitle("油的一步处理")
            .desc(d -> d.line("虽说一步处理，不过考虑到后续流程，你可能还得生产一些石脑油"))
            .task(Task.item(MultiBlockA.PETROCHEMICAL_PLANT))
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest INTEGRATED_ORE_PROCESSOR = CHAPTER.quest("integrated_ore_processor", q -> q
            .at(-9, 6)
            .subtitle("矿石的一步处理")
            .desc(d -> d.line("虽说一步处理，但部分矿物处理存在特殊步骤，仍需单独隔离处理"))
            .task(Task.item(MultiBlockA.INTEGRATED_ORE_PROCESSOR))
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest ASTATINE_DUST = CHAPTER.quest("astatine_dust", q -> q
            .at(4, 4)
            .desc(d -> d
                    .line("可以通过{粒子对撞}获得，也可以通过{大型虚空采矿场}挖取{凯金化合物}然后处理获得", GOLD, GOLD, GOLD)
                    .line()
                    .line("不过无论如何获得你都需要{三钛线圈}", GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Astatine))
            .dependsOn(SUPER_PARTICLE_COLLIDER, LuvChapter.ESSENCE)
            .requireAny());

    public static final Quest INTEGRAL_FRAMEWORK_ZPM = CHAPTER.quest("integral_framework_zpm", q -> q
            .at(6, 4)
            .subtitle("升级你的大型机器#3")
            .desc(d -> d.line("其实你可以直接跳过{ZPM}做{UV}的", ZPM, UV))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_ZPM))
            .dependsOn(ASTATINE_DUST)
            .optional());

    public static final Quest FAST_NEUTRON_BREEDER_REACTOR = CHAPTER.quest("fast_neutron_breeder_reactor", q -> q
            .at(6, 6)
            .desc(d -> d
                    .line("全新的反应堆，仍在测试阶段")
                    .line()
                    .line("提示：你只需要知道如何定性控制机器即可，无需真的看懂公式"))
            .task(Task.item(MultiBlockH.FAST_NEUTRON_BREEDER_REACTOR))
            .dependsOn(ASTATINE_DUST));

    public static final Quest LIGHT_EMITTING_CHARGED_SUSPICIOUS_WASTE_DUST = CHAPTER.quest("light_emitting_charged_suspicious_waste_dust", q -> q
            .at(6, 8)
            .desc(d -> d.line("EMI里面部分配方废料粉是第七个产物所以没空间显示了，实际上是有的"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.LightEmittingChargedSuspiciousWaste))
            .dependsOn(FAST_NEUTRON_BREEDER_REACTOR));

    public static final Quest ETRIUM_DUST = CHAPTER.quest("etrium_dust", q -> q
            .at(4, 8)
            .desc(d -> d.line("是{空间站高能转换调配仓}和{太空电梯连接仓}的材料之一，也用于燃料电池发电", GOLD, GOLD))
            .task(Task.item(TagPrefix.dust, GTOMaterials.Etrium))
            .dependsOn(LIGHT_EMITTING_CHARGED_SUSPICIOUS_WASTE_DUST));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.SUPER_MOLECULAR_ASSEMBLER, -9, -6))
                .link(QuestLink.of(UvChapter.WELCOME_TO_THE_EXTREME_PRESSURE_STAGE, 6, -4))
                .link(QuestLink.of(BeyondTheStarsChapter.SPACE_ADVANCED_QUARKTECH_CHESTPLATE, 2, 8))
                .link(QuestLink.of(LuvChapter.RESEARCH_STATION, -6.5, -4)));
    }
}
