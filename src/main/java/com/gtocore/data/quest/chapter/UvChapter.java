package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockH;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import static com.gtolib.api.quest.QuestStyle.*;

public final class UvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("uv", c -> c
            .title("11. 极限压(UV)")
            .subtitle("Tier8-UV")
            .icon(MultiBlockD.SPACE_ELEVATOR)
            .hideQuestsUntilDepsVisible());

    public static final Quest TELLURATE_CERAMICS_FLAKE = CHAPTER.quest("tellurate_ceramics_flake", q -> q
            .at(11, 14.5)
            .desc(d -> d
                    .line("还记得你的碲从哪里来吗？")
                    .line()
                    .line("不过这次你有太空电梯了")
                    .line()
                    .line("如果你已经搭设了PCB工厂，这个陶瓷就不是必需品了"))
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TellurateCeramics))
            .optional().invisible().hideDependentLines());

    public static final Quest PH_NEUTRALIZATION_PURIFICATION_UNIT = CHAPTER.quest("ph_neutralization_purification_unit", q -> q
            .at(0, 9.5)
            .task(Task.item(MultiBlockB.PH_NEUTRALIZATION_PURIFICATION_UNIT))
            .dependsOn(LuvChapter.FLOCCULATION_PURIFICATION_UNIT));

    public static final Quest EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT = CHAPTER.quest("extreme_temperature_fluctuation_purification_unit", q -> q
            .at(2, 9.5)
            .task(Task.item(MultiBlockB.EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT))
            .dependsOn(PH_NEUTRALIZATION_PURIFICATION_UNIT)
            .hideDependencyLines());

    public static final Quest PH_NEUTRAL_WATER_BUCKET = CHAPTER.quest("ph_neutral_water_bucket", q -> q
            .at(1, 8.5)
            .icon(GTOMaterials.PHNeutralWater.getBucket())
            .subtitle("[H+]=1e-7")
            .task(Task.item(GTOMaterials.PHNeutralWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(PH_NEUTRALIZATION_PURIFICATION_UNIT));

    public static final Quest EXTREME_TEMPERATURE_WATER_BUCKET = CHAPTER.quest("extreme_temperature_water_bucket", q -> q
            .at(3, 8.5)
            .icon(GTOMaterials.ExtremeTemperatureWater.getBucket())
            .subtitle("生物恒温环境")
            .task(Task.item(GTOMaterials.ExtremeTemperatureWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT, PH_NEUTRAL_WATER_BUCKET));

    public static final Quest CEREBRUM = CHAPTER.quest("cerebrum", q -> q
            .at(-1, 12.5)
            .desc(d -> d.line("还是一样，用捕获附魔获取蛋"))
            .task(Task.item(GTOItems.CEREBRUM))
            .dependsOn(ZpmChapter.BIOCHEMICAL_EXTRACTION));

    public static final Quest CARBON_NANITES = CHAPTER.quest("carbon_nanites", q -> q
            .at(-7.5, 10.5)
            .desc(d -> d
                    .line("{装配线上，某位员工突发奇思妙想：将}{生物细胞}{与}{精密电路}{装配在一起会发生什么？}", ITALIC, YELLOW.italic(), ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{于是}{纳米蜂群}{就诞生了}", ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{前面几个必须用装配线，后面就可以换成纳米锻炉了}", ITALIC))
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Carbon))
            .dependsOn(ZpmChapter.STEM_CELLS)
            .hideDependencyLines());

    public static final Quest NANO_FORGE = CHAPTER.quest("nano_forge", q -> q
            .at(-5, 10.5)
            .subtitle("蜂群，但不是养蜂")
            .desc(d -> d
                    .line("{...那名员工的发现被迅速上报至}{公司总部}{。数个科学家团队昼夜攻关，仿照蜂群结构研制出多种不同属性的纳米蜂群，并探索出一种基于复制的大规模量产方式}", ITALIC, DARK_AQUA.italic(), ITALIC)
                    .line()
                    .line("{以一个原始蜂群为基底，通过}{激光蚀刻}{不同属性的材料，就可以源源不断地制造出对应属性的蜂群}", ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{如果想用终端搭建，你需要先在主机里}{放一个蜂群}来指定等级，随后才可以开始搭建", ITALIC, GREEN.italic())
                    .line()
                    .line("机器每次{拿出或放入}蜂群都会重新触发结构检测", YELLOW))
            .task(Task.item(MultiBlockD.NANO_FORGE))
            .dependsOn(CARBON_NANITES));

    public static final Quest NANITES_INTEGRATED_PROCESSING_CENTER = CHAPTER.quest("nanites_integrated_processing_center", q -> q
            .at(-5, 13)
            .desc(d -> d
                    .line("{纳米蜂群}{的问世和量产推动了}{格雷科技有限公司}{的}{第一次技术革命}{。老旧复杂的生产线被彻底改造，取而代之的是一体化的蜂群生产车间——无数的纳米蜂群从原子层面上对物质进行拆分和组合，}{一步完成整个生产过程}", GOLD.italic(), ITALIC, DARK_AQUA.italic(), ITALIC, YELLOW.italic(), ITALIC, YELLOW.italic())
                    .line()
                    .line("你可以用它来大幅简化过去和未来的复杂产线，每个纳米加工中心可以放置{两个}模块", GREEN)
                    .line()
                    .line("放入{主机}的蜂群并不参与配方运行，而是减少{生产时}蜂群的污染概率。现在你可以放入{铱纳米蜂群}", YELLOW, YELLOW, AQUA)
                    .line()
                    .line("可以使用很多增益仓，不要忘记看看{这里}", link(UvChapter.BUFFERING_HATCH))
                    .line()
                    .line("{虽然模块本身也可以运行配方，但是只有主机可以安装增益仓，因此只需要在主机上安装各种仓室即可}", GRAY.italic()))
            .task(Task.item(MultiBlockC.NANITES_INTEGRATED_PROCESSING_CENTER))
            .dependsOn(NANO_FORGE));

    public static final Quest PIRANHA_SOLUTION_BUCKET = CHAPTER.quest("piranha_solution_bucket", q -> q
            .at(-5, 14.5)
            .icon(GTOMaterials.PiranhaSolution.getBucket())
            .subtitle("洗个澡澡！")
            .desc(d -> d
                    .line("{...就是蜂群的消耗有些快，所幸，科学家们发现了一种特殊的洗液可以}{重新激发蜂群的活力}", ITALIC, YELLOW.italic())
                    .line()
                    .line("蜂群们为你那么勤劳地工作，也许你可能需要给他们一点点{回报}...", DARK_PURPLE)
                    .line()
                    .line("那就给他们洗个澡吧！{过氧化氢}的强氧化性和{硫酸}的酸性环境能重新激发它们的活力！", AQUA, GOLD))
            .task(Task.item(GTOMaterials.PiranhaSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest BIOENGINEERING_MODULE = CHAPTER.quest("bioengineering_module", q -> q
            .at(-3.5, 14)
            .subtitle("蜂群也是生物，很合理")
            .desc(d -> d.line("更方便地制作生物原料"))
            .task(Task.item(MultiBlockC.BIOENGINEERING_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest ORE_EXTRACTION_MODULE = CHAPTER.quest("ore_extraction_module", q -> q
            .at(-3.5, 12)
            .subtitle("产线杀手")
            .desc(d -> d
                    .line("一步到位完成矿石处理产线！")
                    .line()
                    .line("厌倦了被动产线大量堆砌的机器？这个会满足你的"))
            .task(Task.item(MultiBlockC.ORE_EXTRACTION_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest POLYMER_TWISTING_MODULE = CHAPTER.quest("polymer_twisting_module", q -> q
            .at(-2.5, 13)
            .subtitle("超级产线杀手")
            .desc(d -> d
                    .line("一步到位完成化工产线！")
                    .line()
                    .line("你最好随时注意解锁的新配方，来{替换}你之前的下单产线，别忘了处理蜂群的洗涤和回收", YELLOW))
            .task(Task.item(MultiBlockC.POLYMER_TWISTING_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest PCB_FACTORY = CHAPTER.quest("pcb_factory", q -> q
            .at(-2.25, 10.5)
            .desc(d -> d
                    .line("和锻炉类似，你也需要先在主机里放一个{金纳米蜂群}来指定等级", GOLD)
                    .line()
                    .line("可以更高效地制作PCB板子，你可以改进之前的产线了")
                    .line()
                    .line("生物印刷电路基板任务并{不强制要求}PCB工厂", YELLOW))
            .task(Task.item(MultiBlockD.PCB_FACTORY))
            .dependsOn(NANO_FORGE));

    public static final Quest BIOWARE_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("bioware_printed_circuit_board", q -> q
            .at(3, 10.5)
            .desc(d -> d.line("如果你还没有PCB工厂，手持{亚碲酸盐陶瓷薄片}也可以解锁这个任务", GOLD))
            .task(Task.item(GTOItems.BIOWARE_PRINTED_CIRCUIT_BOARD))
            .dependsOn(PCB_FACTORY, TELLURATE_CERAMICS_FLAKE)
            .requireAny());

    public static final Quest WELCOME_TO_THE_EXTREME_PRESSURE_STAGE = CHAPTER.quest("welcome_to_the_extreme_pressure_stage", q -> q
            .at(-5, 1.5)
            .title("欢迎来到{极限压}阶段", DARK_AQUA)
            .subtitle("{你需要天马行空般的想象力}", DARK_AQUA)
            .desc(d -> d
                    .line("极限压阶段仍还有一半的{生}{物}{线}要走", GREEN, DARK_GREEN, GREEN)
                    .line()
                    .line("不过作为回报，你将正式解锁第一个巨构，开始你的超大杯之旅！")
                    .line()
                    .line("从{安普洛}和{纳米锻炉}开始你的极限压之旅吧", GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.UV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(ZpmChapter.DARMSTADTIUM_INGOT)
            .hideDependencyLines(false));

    public static final Quest UV_ENERGY_INPUT_HATCH = CHAPTER.quest("uv_energy_input_hatch", q -> q
            .at(-2, 1.5)
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UV]))
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE));

    public static final Quest UV_FUSION_REACTOR = CHAPTER.quest("uv_fusion_reactor", q -> q
            .at(1, 1.5)
            .subtitle("芥末甜甜圈")
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.UV]))
            .dependsOn(UV_ENERGY_INPUT_HATCH, ZpmChapter.WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest AMPROSIUM_INGOT = CHAPTER.quest("amprosium_ingot", q -> q
            .at(5, 1.5)
            .subtitle("控制重力的力量")
            .desc(d -> d
                    .line("{UHV}阶段的主要材料", UHV)
                    .line()
                    .line("试试把它拿在手上？希望你一切安好。"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Neutronium))
            .dependsOn(UV_FUSION_REACTOR));

    public static final Quest INFINITY_FLUID_DRILLING_RIG = CHAPTER.quest("infinity_fluid_drilling_rig", q -> q
            .at(5, -0.5)
            .subtitle("再也不用给钻机换位置了")
            .task(Task.item(MultiBlockD.INFINITY_FLUID_DRILLING_RIG))
            .dependsOn(AMPROSIUM_INGOT));

    public static final Quest NEUTRONIUM_BOULE = CHAPTER.quest("neutronium_boule", q -> q
            .at(7, 3.5)
            .desc(d -> d.line("镭的来源是{硫酸铀废液}，你也可以通过{快中子增殖堆}获得", GOLD, GOLD))
            .task(Task.item(GTItems.NEUTRONIUM_BOULE))
            .dependsOn(AMPROSIUM_INGOT));

    public static final Quest NEUTRONIUM_WAFER = CHAPTER.quest("neutronium_wafer", q -> q
            .at(7, 6.5)
            .task(Task.item(GTItems.NEUTRONIUM_WAFER))
            .dependsOn(NEUTRONIUM_BOULE));

    public static final Quest HIGHLY_ADVANCED_SOC = CHAPTER.quest("highly_advanced_soc", q -> q
            .at(7, 8.5)
            .task(Task.item(GTItems.HIGHLY_ADVANCED_SOC))
            .dependsOn(NEUTRONIUM_WAFER));

    public static final Quest POLYIMIDE_BUCKET = CHAPTER.quest("polyimide_bucket", q -> q
            .at(5, 14.5)
            .icon(GTOMaterials.Polyimide.getBucket())
            .desc(d -> d.line("聚酰亚胺是一种特种工程材料，广泛应用在航空、航天、微电子、纳米、液晶、分离膜、激光等领域"))
            .task(Task.checkmark().title("聚酰亚胺"), Task.item(GTOMaterials.Polyimide.getBucket()).optional())
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest CIRCUIT_ASSEMBLY_LINE = CHAPTER.quest("circuit_assembly_line", q -> q
            .at(11, 10.5)
            .icon(MultiBlockD.CIRCUIT_ASSEMBLY_LINE)
            .desc(d -> d
                    .line("所有的电路装配线配方都需要一个机器人作为工作单位")
                    .line()
                    .line("使用更高等级的机器人电路配方可以为电路制作提供{无损超频}", YELLOW)
                    .line()
                    .line("往{主机}中塞入同配方的机器人可以提供并行（不可以替代配方中的机器人）", YELLOW))
            .task(Task.item(MultiBlockD.CIRCUIT_ASSEMBLY_LINE), Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK1))
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest POLYETHERETHERKETONE_BUCKET = CHAPTER.quest("polyetheretherketone_bucket", q -> q
            .at(11, -0.5)
            .icon(GTOMaterials.Polyetheretherketone.getBucket())
            .desc(d -> d.line("特种工程塑料"))
            .task(Task.item(GTOMaterials.Polyetheretherketone.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest MITHRIL_INGOT = CHAPTER.quest("mithril_ingot", q -> q
            .at(0, 0)
            .desc(d -> d.line("{等离子体}需要冷凝成{液体}之后才能固化成锭，目前推荐使用{热交换机}冷凝", GOLD, YELLOW, GOLD))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Mithril))
            .dependsOn(UV_FUSION_REACTOR)
            .hideDependentLines());

    public static final Quest INTEGRAL_FRAMEWORK_UV = CHAPTER.quest("integral_framework_uv", q -> q
            .at(-7, 1.5)
            .subtitle("升级你的大型机器#4")
            .desc(d -> d.line("不需要做单方块组装机也能做哦"))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_UV))
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE));

    public static final Quest GRAVI_STAR = CHAPTER.quest("gravi_star", q -> q
            .at(3, 3.5)
            .task(Task.item(GTItems.GRAVI_STAR))
            .dependsOn(AMPROSIUM_INGOT));

    public static final Quest GRAVITY_HATCH = CHAPTER.quest("gravity_hatch", q -> q
            .at(1, 3.5)
            .desc(d -> d.line("重力控制仓可以调至0模拟低重力环境，也可以调至100提供强重力环境"))
            .task(Task.item(GTOMachines.GRAVITY_HATCH))
            .dependsOn(GRAVI_STAR));

    public static final Quest UV_FORCE_FIELD_GENERATOR_LAUNCHER_AND = CHAPTER.quest("uv_force_field_generator_launcher_and", q -> q
            .at(5, 3.5)
            .title("{UV}力场发生器，发射器和传感器", UV)
            .task(Task.item(GTItems.FIELD_GENERATOR_UV), Task.item(GTItems.EMITTER_UV), Task.item(GTItems.SENSOR_UV))
            .dependsOn(GRAVI_STAR));

    public static final Quest BUFFERING_HATCH = CHAPTER.quest("buffering_hatch", q -> q
            .at(11, 3.5)
            .icon(GTOMachines.OVERCLOCK_HATCH[GTValues.UV])
            .title("增益仓")
            .subtitle("{产能至上！}", DARK_AQUA)
            .desc(d -> d
                    .line("在巨构中，你将可以使用四种增益仓室提高机器效率。{首先是两位老朋友：}", YELLOW)
                    .line()
                    .line("{加速仓}：大部分机器可以使用。{无条件}加快机器速度，数字表示当加速仓等级{高于}配方等级时，配方时间将乘以这个百分比。当加速仓等级每{低于}配方一级，加速效果减少20%。", GREEN, AQUA, AQUA, AQUA)
                    .line()
                    .line("{并行控制仓}：可并行机器即可使用。允许机器一次性翻倍功率处理多个配方，某种意义下相当于提供无损超频。", GREEN)
                    .page()
                    .line("{然后是两位新朋友：}", YELLOW)
                    .line()
                    .line("{线程仓}：仅跨配方并行机器可以使用。允许机器尽量同时多运行数个{不同配方}来跑满并行和最高效率。", GREEN, AQUA)
                    .line()
                    .line("{超频仓}：几乎只有巨构可以使用。非常强大，能直接{修改机器超频的时间倍率系数}。UV级别是一般的有损超频，UEV级别即可达到无损超频，在这之后机器越超频越省电！（UEV以上机器会优先计算超频再计算并行）", GREEN, AQUA)
                    .line()
                    .line("此外，负荷调节维护仓也可以提供微弱的机器加速效果")
                    .line()
                    .line("{别忘了给你的狂飙也换上}", GRAY.italic()))
            .task(
                    Task.item(GCYMMachines.PARALLEL[GTValues.UV]).optional(),
                    Task.item(GTOMachines.ACCELERATE_HATCH[GTValues.UV]).optional(),
                    Task.item(GTOMachines.THREAD_HATCH[GTValues.UV]).optional(),
                    Task.item(GTOMachines.OVERCLOCK_HATCH[GTValues.UV]).optional(),
                    Task.checkmark())
            .dependsOn(UV_FORCE_FIELD_GENERATOR_LAUNCHER_AND)
            .hideDependencyLines());

    public static final Quest WETWARE_SOC = CHAPTER.quest("wetware_soc", q -> q
            .at(1, 12.5)
            .subtitle("布满了脑细胞")
            .desc(d -> d.line("更便宜的制作{湿件处理器}的原材料", GOLD))
            .task(Task.item(GTOItems.WETWARE_SOC))
            .dependsOn(HIGHLY_ADVANCED_SOC, CEREBRUM)
            .hideDependencyLines());

    public static final Quest SUPERCOMPUTING_CENTER = CHAPTER.quest("supercomputing_center", q -> q
            .at(3, 12.5)
            .subtitle("天河一号")
            .desc(d -> d
                    .line("超级计算中心，可以放下足足{840}个组件。一台足够提供你整个基地的算力供应", AQUA)
                    .line()
                    .line("请使用{空组件}填充你用不到的组件槽位", GOLD)
                    .line()
                    .line("{导热剂}并非必需品，Tier2/3放入导热剂后会提升超级计算中心输出的算力", GOLD))
            .task(Task.item(ExResearchMachines.SUPERCOMPUTING_CENTER))
            .dependsOn(WETWARE_SOC));

    public static final Quest ORICHALCUM_INGOT = CHAPTER.quest("orichalcum_ingot", q -> q
            .at(2, 0)
            .desc(d -> d.line("{UHV}阶段部件的主要材料", UHV))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Orichalcum))
            .dependsOn(UV_FUSION_REACTOR));

    public static final Quest CYCLOPARAPHENYLENE_BUCKET = CHAPTER.quest("cycloparaphenylene_bucket", q -> q
            .at(-7, 8)
            .icon(GTOMaterials.Cycloparaphenylene.getBucket())
            .task(Task.item(GTOMaterials.Cycloparaphenylene.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_EXTREME_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest NANOTUBE_SPOOL = CHAPTER.quest("nanotube_spool", q -> q
            .at(-7, 6.5)
            .desc(d -> d.line("太空电梯第一次启动的时候需要，放置在太空电梯的输入总线内自动消耗"))
            .task(Task.item(GTOItems.NANOTUBE_SPOOL, 256L))
            .dependsOn(
                    SpaceStationChapter.ORBITAL_FINE_MATERIALS_FACTORY,
                    SpaceStationChapter.SPACE_STATION_ENVIRONMENTAL_MAINTENANCE_MODULE,
                    CYCLOPARAPHENYLENE_BUCKET));

    public static final Quest HUMAN_CANNON = CHAPTER.quest("human_cannon", q -> q
            .at(-5, 6.5)
            .title("人间大炮")
            .subtitle("我们的目标是星辰大海")
            .desc(d -> d
                    .line("这是T8火箭，解锁巴纳德C")
                    .line()
                    .line("{电梯的配方中有两组碳纳米蜂群的需求，如果还用装配线做显然太贵了，因此我将纳米锻炉设为了前置。}", GRAY.italic()))
            .task(Task.item(MultiBlockD.SPACE_ELEVATOR))
            .dependsOn(ZpmChapter.WETWARE_PROCESSOR_MAINFRAME, NANO_FORGE, NANOTUBE_SPOOL));

    public static final Quest ASSEMBLER_MODULE = CHAPTER.quest("assembler_module", q -> q
            .at(-3, 6.5)
            .task(Task.item(MultiBlockD.ASSEMBLER_MODULE))
            .dependsOn(HUMAN_CANNON));

    public static final Quest RESOURCE_COLLECTION_MODULE = CHAPTER.quest("resource_collection_module", q -> q
            .at(-3, 8)
            .task(Task.item(MultiBlockD.RESOURCE_COLLECTION_MODULE))
            .dependsOn(HUMAN_CANNON));

    public static final Quest SPACE_DRONE_MK1 = CHAPTER.quest("space_drone_mk1", q -> q
            .at(-1, 6.5)
            .desc(d -> d
                    .line("使用太空电梯采集资源必须要搭配无人机，部分高级资源必须在取得更高级的无人机之后才可以采集")
                    .line()
                    .line("每种资源都可以使用两种火箭燃料之一挖掘，更换更高等级的无人机并不会提升采集效率"))
            .task(Task.item(GTOItems.SPACE_DRONE_MK1))
            .dependsOn(ASSEMBLER_MODULE));

    public static final Quest ALIEN_ALGAE_SLAG_ORE = CHAPTER.quest("alien_algae_slag_ore", q -> q
            .at(-1, 8)
            .icon(TagPrefix.ore, GTOMaterials.AlienAlgae)
            .title("异星藻类渣矿石")
            .task(Task.item(TagPrefix.ore, GTOMaterials.AlienAlgae).optional(), Task.checkmark())
            .dependsOn(RESOURCE_COLLECTION_MODULE, SPACE_DRONE_MK1));

    public static final Quest BARNARD_C = CHAPTER.quest("barnard_c", q -> q
            .at(-3, 5)
            .title("巴纳德C")
            .desc(d -> d
                    .line("点击太空电梯启程按钮前往巴纳德C")
                    .line()
                    .line("别忘了穿上{T4}器官", AQUA))
            .task(Task.dimension(Dimension.BARNARDA_C).icon(GTOGlobes.Items.BARNARDA_C_GLOBE.get()))
            .dependsOn(HUMAN_CANNON, BeyondTheStarsChapter.TRANSCEND_THE_FLESH_BIRTH_OF_A));

    public static final Quest UNKNOWWATER_BUCKET = CHAPTER.quest("unknowwater_bucket", q -> q
            .at(-1, 5)
            .icon(GTOMaterials.UnknowWater.getBucket())
            .desc(d -> d.line("带上你的钻机"))
            .task(Task.item(GTOMaterials.UnknowWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(BARNARD_C));

    public static final Quest TCETIESEAWEEDEXTRACT = CHAPTER.quest("tcetieseaweedextract", q -> q
            .at(1, 6.5)
            .subtitle("天仓五 - 新的生命")
            .desc(d -> d.line("你需要使用重力控制仓达到强重力环境"))
            .task(Task.item(GTOItems.TCETIESEAWEEDEXTRACT))
            .dependsOn(ALIEN_ALGAE_SLAG_ORE, SPACE_DRONE_MK1, UNKNOWWATER_BUCKET, MITHRIL_INGOT, GRAVITY_HATCH));

    public static final Quest ABYSSALALLOY_INGOT = CHAPTER.quest("abyssalalloy_ingot", q -> q
            .at(-3, 3.5)
            .desc(d -> d.line("前往巴纳德C采集空气"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AbyssalAlloy))
            .dependsOn(BARNARD_C));

    public static final Quest ABYSSALALLOY_COIL_BLOCK = CHAPTER.quest("abyssalalloy_coil_block", q -> q
            .at(-1, 3.5)
            .desc(d -> d.line("更高级的线圈，你可以烧一些新东西了"))
            .task(Task.item(GTOBlocks.ABYSSALALLOY_COIL_BLOCK))
            .dependsOn(ABYSSALALLOY_INGOT));

    public static final Quest BIOHMEDIUMSTERILIZED_BUCKET = CHAPTER.quest("biohmediumsterilized_bucket", q -> q
            .at(3, 5)
            .icon(GTOMaterials.BiohmediumSterilized.getBucket())
            .desc(d -> d
                    .line("是第一批生物细胞的制作原料，也是细胞增殖的原料")
                    .line()
                    .line("你可能要考虑设计处理循环产线"))
            .task(Task.item(GTOMaterials.BiohmediumSterilized.getBucket()).optional(), Task.checkmark())
            .dependsOn(ZpmChapter.BIOCHEMICAL_REACTION, TCETIESEAWEEDEXTRACT)
            .hideDependentLines());

    public static final Quest SMD_CAPACITOR_BIOWARE = CHAPTER.quest("smd_capacitor_bioware", q -> q
            .at(5, 12.5)
            .desc(d -> d.line("后面的电路每种都有对应的贴片"))
            .task(
                    Task.item(GTOItems.SMD_CAPACITOR_BIOWARE),
                    Task.item(GTOItems.SMD_DIODE_BIOWARE),
                    Task.item(GTOItems.SMD_RESISTOR_BIOWARE),
                    Task.item(GTOItems.SMD_TRANSISTOR_BIOWARE),
                    Task.item(GTOItems.SMD_INDUCTOR_BIOWARE))
            .dependsOn(POLYIMIDE_BUCKET, BIOHMEDIUMSTERILIZED_BUCKET));

    public static final Quest SPACE_ELEVATOR_POWER_MODULE_1 = CHAPTER.quest("space_elevator_power_module_1", q -> q
            .at(-5, 4.5)
            .desc(d -> d
                    .line("更好的动力模块可以提供{并行}", AQUA)
                    .line()
                    .line("你也可以给{太空电梯模块}供电{升压}来获得配方耗时减免！", GOLD, YELLOW))
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_1))
            .dependsOn(HUMAN_CANNON));

    public static final Quest BIOLOGICAL_CELLS = CHAPTER.quest("biological_cells", q -> q
            .at(3, 6.5)
            .icon(GTOItems.BIOLOGICAL_CELLS)
            .desc(d -> d.line("生化反应培养出后可使用培养缸繁殖"))
            .task(Task.item(GTOItems.BIOLOGICAL_CELLS))
            .dependsOn(BIOHMEDIUMSTERILIZED_BUCKET, TCETIESEAWEEDEXTRACT));

    public static final Quest BIOWARE_BOULE = CHAPTER.quest("bioware_boule", q -> q
            .at(5, 6.5)
            .task(Task.item(GTOItems.BIOWARE_BOULE))
            .dependsOn(BIOLOGICAL_CELLS, NEUTRONIUM_WAFER));

    public static final Quest BIOWARE_CHIP = CHAPTER.quest("bioware_chip", q -> q
            .at(5, 8.5)
            .task(Task.item(GTOItems.BIOWARE_CHIP))
            .dependsOn(BIOWARE_BOULE, EXTREME_TEMPERATURE_WATER_BUCKET));

    public static final Quest BIOWARE_PROCESSING_CORE = CHAPTER.quest("bioware_processing_core", q -> q
            .at(5, 10.5)
            .task(Task.item(GTOItems.BIOWARE_PROCESSING_CORE))
            .dependsOn(BIOWARE_PRINTED_CIRCUIT_BOARD, SMD_CAPACITOR_BIOWARE, BIOWARE_CHIP));

    public static final Quest MUTATED_LIVING_SOLDER_BUCKET = CHAPTER.quest("mutated_living_solder_bucket", q -> q
            .at(7, 12.5)
            .icon(GTOMaterials.MutatedLivingSolder.getBucket())
            .task(Task.item(GTOMaterials.MutatedLivingSolder.getBucket()).optional(), Task.checkmark())
            .dependsOn(BIOLOGICAL_CELLS)
            .hideDependencyLines());

    public static final Quest BIO_ACTIVITY_PROCESSOR = CHAPTER.quest("bio_activity_processor", q -> q
            .at(7, 10.5)
            .title("{生}{物}{活}{性}{处}{理}{器}", DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN)
            .task(Task.item(GTOItems.BIOWARE_PROCESSOR))
            .dependsOn(BIOWARE_PROCESSING_CORE, HIGHLY_ADVANCED_SOC, MUTATED_LIVING_SOLDER_BUCKET));

    public static final Quest BIOACTIVITY_PROCESSOR_HATCH_CLUSTER = CHAPTER.quest("bioactivity_processor_hatch_cluster", q -> q
            .at(9, 10.5)
            .title("{生}{物}{活}{性}{处}{理}{器}{集}{群}", DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN)
            .task(Task.item(GTOItems.BIOWARE_ASSEMBLY))
            .dependsOn(BIO_ACTIVITY_PROCESSOR, CIRCUIT_ASSEMBLY_LINE));

    public static final Quest BIOACTIVITYPROCESSORSUPERCOMPUTER = CHAPTER.quest("bioactivityprocessorsupercomputer", q -> q
            .at(9, 8.5)
            .title("{生}{物}{活}{性}{处}{理}{器}{超}{级}{计}{算}{机}", DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN)
            .task(Task.item(GTOItems.BIOWARE_COMPUTER))
            .dependsOn(BIOACTIVITY_PROCESSOR_HATCH_CLUSTER));

    public static final Quest BIOACTIVITY_PROCESSOR_CONTROLLER = CHAPTER.quest("bioactivity_processor_controller", q -> q
            .at(9, 6)
            .title("{生}{物}{活}{性}{处}{理}{器}{主}{机}", DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN, GREEN, DARK_GREEN)
            .subtitle("你已掌握微生物的法则")
            .desc(d -> d.line("恭喜！生物线完成！看看你的培养缸，然后外出休息一下吧！"))
            .task(Task.item(GTOItems.BIOWARE_MAINFRAME))
            .dependsOn(BIOACTIVITYPROCESSORSUPERCOMPUTER));

    public static final Quest SUPER_BLAST_SMELTER = CHAPTER.quest("super_blast_smelter", q -> q
            .at(9, 3.5)
            .subtitle("超大杯")
            .desc(d -> d
                    .line("这台机器标志着你正式进入巨构时代，所有的{增益仓}都已经为你开放！", GOLD)
                    .line()
                    .line("其余大部分巨构将在你制造出{UIV}电路后解锁", UIV))
            .task(Task.item(MultiBlockF.SUPER_BLAST_SMELTER))
            .dependsOn(BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest PLASMA_CONDENSER = CHAPTER.quest("plasma_condenser", q -> q
            .at(11, 8.5)
            .subtitle("牺牲发电换取产出")
            .desc(d -> d
                    .line("冷凝等离子体，牺牲发电和速度换取更高的产率")
                    .line()
                    .line("后期的等离子体将只能通过等离子冷凝来冷却"))
            .task(Task.item(MultiBlockA.PLASMA_CONDENSER))
            .dependsOn(BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest SUPERCONDUCTING_ELECTROMAGNETISM = CHAPTER.quest("superconducting_electromagnetism", q -> q
            .at(11, 6)
            .task(Task.item(MultiBlockF.SUPERCONDUCTING_ELECTROMAGNETISM))
            .dependsOn(BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest CHEMICAL_COMPLEX = CHAPTER.quest("chemical_complex", q -> q
            .at(11, 7)
            .task(Task.item(MultiBlockA.CHEMICAL_COMPLEX))
            .dependsOn(BIOACTIVITY_PROCESSOR_CONTROLLER));

    public static final Quest GEMINI_CONTAINMENT_SYSTEM = CHAPTER.quest("gemini_containment_system", q -> q
            .at(11, 5)
            .task(Task.item(MultiBlockH.GEMINI_CONTAINMENT_SYSTEM))
            .dependsOn(BIOACTIVITY_PROCESSOR_CONTROLLER));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.ME_CATALYST_PATTERN_BUFFER, 5, 5))
                .link(QuestLink.of(AeChapter.ME_EXTEND_PATTERN_BUFFER, -5, -0.5))
                .link(QuestLink.of(SpaceStationChapter.ORBITAL_FINE_MATERIALS_FACTORY, -6.5, 5))
                .link(QuestLink.of(UhvChapter.WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE, 11, 1.5))
                .link(QuestLink.of(BeyondTheStarsChapter.TRANSCEND_THE_FLESH_BIRTH_OF_A, -4, 4))
                .link(QuestLink.of(SpaceStationChapter.SPACE_STATION_ENVIRONMENTAL_MAINTENANCE_MODULE, -7.5, 5))
                .link(QuestLink.of(SpaceStationChapter.INDUSTRIAL_SPACE_STATION, -7, 3.5)));
    }
}
