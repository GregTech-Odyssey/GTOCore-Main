package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.celsiusqc.ad_astra_rocketed.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class IvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("iv", c -> c
            .title("8. 强导压(IV)")
            .subtitle("Tier5-IV")
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .hideQuestsUntilDepsVisible());

    public static final Quest HSSG_INGOT = CHAPTER.quest("hssg_ingot", q -> q
            .at(10, -1.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSSG))
            .dependsOn(EvChapter.RTM_ALLOY_COIL_BLOCK));

    public static final Quest QUANTUM_STAR = CHAPTER.quest("quantum_star", q -> q
            .at(18, 0)
            .task(Task.item(GTItems.QUANTUM_STAR))
            .dependsOn(EvChapter.NETHER_STAR));

    public static final Quest IV_LAUNCHER_AND_IV_SENSOR = CHAPTER.quest("iv_launcher_and_iv_sensor", q -> q
            .at(18, -2.5)
            .title("{IV}发射器和{IV}传感器", IV, IV)
            .task(Task.item(GTItems.EMITTER_IV), Task.item(GTItems.SENSOR_IV))
            .dependsOn(QUANTUM_STAR));

    public static final Quest IV_PARALLEL_HATCH = CHAPTER.quest("iv_parallel_hatch", q -> q
            .at(20, 0)
            .desc(d -> d
                    .line("为你的大机器提供并行！")
                    .line()
                    .line("允许机器一次性以多倍功率处理多个配方，某种意义下相当于提供无损超频。"))
            .task(Task.item(GCYMMachines.PARALLEL[GTValues.IV]))
            .dependsOn(IV_LAUNCHER_AND_IV_SENSOR));

    public static final Quest IV_PROCESSING_ARRAY = CHAPTER.quest("iv_processing_array", q -> q
            .at(3, -4.5)
            .desc(d -> d
                    .line("如果你遇到了很慢的配方，可以试试这个")
                    .line()
                    .line("处理阵列可以安装{激光仓}来进行更多次数的{超频}，但处理阵列每次超频获得的{耗时减免}{较少}。你可以用{高能耗}作为代价来获得比大型机器更快的{处理速度}", GOLD, YELLOW, YELLOW, AQUA, RED, YELLOW))
            .task(Task.item(MultiBlockG.PROCESSING_ARRAY[GTValues.IV]))
            .dependsOn(IV_LAUNCHER_AND_IV_SENSOR)
            .hideDependencyLines());

    public static final Quest POLYVINYL_BUTYRAL_BUCKET = CHAPTER.quest("polyvinyl_butyral_bucket", q -> q
            .at(1.5, -3)
            .icon(GTMaterials.PolyvinylButyral.getBucket())
            .desc(d -> d.line("夹层玻璃所需"))
            .task(Task.item(GTMaterials.PolyvinylButyral.getBucket()).optional(), Task.checkmark())
            .dependsOn(HvChapter.POLYMERIZATION_REACTION)
            .hideDependencyLines());

    public static final Quest ADVANCED_SOLDERING_COMPONENTS = CHAPTER.quest("advanced_soldering_components", q -> q
            .at(8, -1.5)
            .icon(GTMaterials.Polybenzimidazole.getBucket())
            .title("高级贴片元件")
            .task(Task.item(GTMaterials.Polybenzimidazole.getBucket()).optional(), Task.checkmark())
            .dependsOn(HSSG_INGOT));

    public static final Quest ADVANCED_SMD_TRANSISTOR = CHAPTER.quest("advanced_smd_transistor", q -> q
            .at(6, 0.5)
            .task(Task.item(GTItems.ADVANCED_SMD_TRANSISTOR))
            .dependsOn(ADVANCED_SOLDERING_COMPONENTS));

    public static final Quest ADVANCED_SMD_RESISTOR = CHAPTER.quest("advanced_smd_resistor", q -> q
            .at(6, -2.5)
            .task(Task.item(GTItems.ADVANCED_SMD_RESISTOR))
            .dependsOn(ADVANCED_SOLDERING_COMPONENTS));

    public static final Quest ADVANCED_SMD_DIODE = CHAPTER.quest("advanced_smd_diode", q -> q
            .at(6, -3.5)
            .task(Task.item(GTItems.ADVANCED_SMD_DIODE))
            .dependsOn(ADVANCED_SOLDERING_COMPONENTS));

    public static final Quest ADVANCED_SMD_INDUCTOR = CHAPTER.quest("advanced_smd_inductor", q -> q
            .at(6, -1.5)
            .task(Task.item(GTItems.ADVANCED_SMD_INDUCTOR))
            .dependsOn(ADVANCED_SOLDERING_COMPONENTS));

    public static final Quest ASSEMBLY_LINE_RECIPE_DATA = CHAPTER.quest("assembly_line_recipe_data", q -> q
            .at(20, -4.5)
            .icon(GTItems.TOOL_DATA_STICK)
            .title("装配线配方数据")
            .desc(d -> d
                    .line("{前往}{科技树}{章节组来查看全新的研究线！}", YELLOW, link(TechtreeChapter.IV_SCANNER), YELLOW)
                    .line()
                    .line("装配线在合成物品时，必须要先经过{研究}", YELLOW)
                    .line()
                    .line("你需要先在{数据导出机}中将配方数据导出到闪存，然后把闪存放入{数据访问仓}中，才能让装配线解锁配方", GOLD, GOLD)
                    .line()
                    .line("在此之后，你还会遇到更加强大的数据载体和研究仪器"))
            .task(Task.item(GTMachines.SCANNER[GTValues.IV]), Task.item(GTItems.TOOL_DATA_STICK))
            .dependsOn(IV_LAUNCHER_AND_IV_SENSOR, EvChapter.NANOPROCESSOR_MAINFRAME));

    public static final Quest COLD_ICE_FREEZER = CHAPTER.quest("cold_ice_freezer", q -> q
            .at(20, -2.5)
            .desc(d -> d.line("更高效的真空冷冻机，具有最高64并行，同时对你的能源供应也提出了更高的要求"))
            .task(Task.item(MultiBlockD.COLD_ICE_FREEZER))
            .dependsOn(IV_LAUNCHER_AND_IV_SENSOR));

    public static final Quest WELCOME_TO_THE_HIGH_PRESSURE_STAGE = CHAPTER.quest("welcome_to_the_high_pressure_stage", q -> q
            .at(6, -5.5)
            .title("欢迎来到{强导压}阶段", BLUE)
            .subtitle("{你需要天马行空般的想象力}", BLUE)
            .desc(d -> d
                    .line("在{IV马达}之前，你需要面对一条超级长的{稀土产线}。与铂处类似，我同样建议你搭建一条独立产线。", GOLD, YELLOW)
                    .line()
                    .line("在获取IV马达之后，将基地里的通用工厂逐步替换成真正的{多方块机器}是一个不错的选择。", YELLOW)
                    .line()
                    .line("这个阶段的机器需要大量的{钨钢}来制作，因此开始发展该阶段前确保你有持续的{钨}产出。", GOLD, GOLD)
                    .line()
                    .line("好消息是，不是所有东西都需要IV马达，在取得{EV能源仓}后你就可以制作{IV并行控制仓}和{寒冰冷冻机}等机器！", GREEN, GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.IV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(EvChapter.TUNGSTEN_STEEL_INGOT));

    public static final Quest QBIT_CPU_CHIP = CHAPTER.quest("qbit_cpu_chip", q -> q
            .at(13, -10.5)
            .subtitle("更便宜的EV电路")
            .desc(d -> d
                    .line("有了它你就能够生产{量子系列电路}", GOLD)
                    .line()
                    .line("我更推荐你使用带有{铟}的配方", GOLD))
            .task(Task.item(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT))
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest Q_02C29988 = CHAPTER.quest("q_02c29988", q -> q
            .at(6, -7.5)
            .icon(TagPrefix.dustSmall, GTMaterials.RareEarth)
            .desc(d -> d
                    .line("你有两条路线来制作稀土，选择你想要的一条，它们都是8步产出稀土")
                    .line()
                    .line("产出稀土的路上有比较多的配方和副产，为了避免你头痛，我已经帮你整理好了")
                    .line()
                    .line("完成本任务后解锁{稀土}章节，查看详细流程", link(RareDustChapter.Q_75569B55))
                    .line()
                    .line("{如果想挑战自己，也可以选择不看}", GRAY.italic()))
            .task(Task.checkmark().title("稀土元素"))
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE));

    public static final Quest BASTNASITE_DUST = CHAPTER.quest("bastnasite_dust", q -> q
            .at(5.5, -8.5)
            .desc(d -> d.line("一路做到钐稀土精粉即可产出稀土"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Bastnasite))
            .dependsOn(Q_02C29988));

    public static final Quest MONAZITE_DUST = CHAPTER.quest("monazite_dust", q -> q
            .at(6.5, -8.5)
            .desc(d -> d
                    .line("相比左边中间多了两个增产配方，分别使用富铈混合物粉和钐精粉进行单步100%和50%的增产")
                    .line()
                    .line("一路做到饱和独居石稀土粉即可产出稀土"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Monazite))
            .dependsOn(Q_02C29988));

    public static final Quest DISSOLVING_TANK_AND_DIGESTION_TANK = CHAPTER.quest("dissolving_tank_and_digestion_tank", q -> q
            .at(6, -10)
            .title("溶解罐和煮解池")
            .desc(d -> d.line("稀土两件套"))
            .task(Task.item(MultiBlockD.DISSOLVING_TANK), Task.item(MultiBlockB.DIGESTION_TANK))
            .dependsOn(BASTNASITE_DUST, MONAZITE_DUST));

    public static final Quest RARE_EARTH_DUST = CHAPTER.quest("rare_earth_dust", q -> q
            .at(7.5, -10)
            .desc(d -> d
                    .line("和之前所有阶段不太一样，制造出IV马达对目前的你来说是一个巨大的挑战")
                    .line()
                    .line("你需要从{稀土}中获取生产{石墨烯}需要的{钇}元素", GOLD, GOLD, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth))
            .dependsOn(DISSOLVING_TANK_AND_DIGESTION_TANK));

    public static final Quest TUNGSTEN_TETRABORIDE_CERAMICS_FLAKE = CHAPTER.quest("tungsten_tetraboride_ceramics_flake", q -> q
            .at(16, -12)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TungstenTetraborideCeramics))
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest EPOXY_BUCKET = CHAPTER.quest("epoxy_bucket", q -> q
            .at(12, -12)
            .icon(GTMaterials.Epoxy.getBucket())
            .task(Task.item(GTMaterials.Epoxy.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest FIBER_REINFORCED_CIRCUIT_BOARD = CHAPTER.quest("fiber_reinforced_circuit_board", q -> q
            .at(14, -12)
            .task(Task.item(GTItems.FIBER_BOARD))
            .dependsOn(EPOXY_BUCKET));

    public static final Quest FIBER_REINFORCED_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("fiber_reinforced_printed_circuit_board", q -> q
            .at(15, -10.5)
            .task(Task.item(GTItems.EXTREME_CIRCUIT_BOARD))
            .dependsOn(TUNGSTEN_TETRABORIDE_CERAMICS_FLAKE, FIBER_REINFORCED_CIRCUIT_BOARD));

    public static final Quest POLYPHENYLENE_SULFIDE_FOIL = CHAPTER.quest("polyphenylene_sulfide_foil", q -> q
            .at(8, -14)
            .desc(d -> d.line("PPS特种工程塑料，你需要它来制作线缆"))
            .task(Task.item(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide))
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest RED_MUD_BUCKET = CHAPTER.quest("red_mud_bucket", q -> q
            .at(7, -7.5)
            .icon(GTOMaterials.RedMud.getBucket())
            .subtitle("不推荐走这条路")
            .desc(d -> d
                    .line("这是什么邪道？！好吧，如果你只是急需少量的稀土产物（比如在制作石墨烯中作为催化剂长杆的一部分），而又不想马上处理漫长的稀土产线，可以试一试")
                    .line()
                    .line("但我仍然推荐你正常完成整个稀土产线，毕竟石头粉的产率肯定是远不如常规稀土矿物")
                    .line()
                    .line("{不知道你有没有发现赤泥甚至可以在HV不上天的情况下产出钛。二周目的时候你可以试一试...}", GRAY.italic()))
            .task(Task.item(GTOMaterials.RedMud.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_HIGH_PRESSURE_STAGE)
            .optional().hideDependencyLines().hideDependentLines());

    public static final Quest YTTRIUM_OXIDE_DUST = CHAPTER.quest("yttrium_oxide_dust", q -> q
            .at(7.5, -8.5)
            .desc(d -> d
                    .line("与铂处类似，你无需此刻完成所有金属粉的产出")
                    .line()
                    .line("不过它们的处理方法都一模一样，为什么不一起做完呢"))
            .task(Task.item(TagPrefix.dust, GTOMaterials.YttriumOxide))
            .dependsOn(RARE_EARTH_DUST, RED_MUD_BUCKET)
            .requireAny());

    public static final Quest YTTRIUM_BARIUM_CUPRATE_INGOT = CHAPTER.quest("yttrium_barium_cuprate_ingot", q -> q
            .at(8.5, -10)
            .desc(d -> d
                    .line("为什么它的{合金冶炼}电压需求这么夸张？！或许我们要考虑{电弧炉}...", GOLD, GOLD)
                    .line()
                    .line("{本质上还是化工产线}", GRAY.italic()))
            .task(Task.item(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate))
            .dependsOn(YTTRIUM_OXIDE_DUST));

    public static final Quest GRAPHENE_FOIL = CHAPTER.quest("graphene_foil", q -> q
            .at(8.5, -8.5)
            .desc(d -> d.line("你必须研磨 化学气相沉积产出的{石墨烯箔} 来获得第一批石墨烯粉，此后你可以使用分子重组仪", GOLD))
            .task(Task.item(TagPrefix.foil, GTMaterials.Graphene))
            .dependsOn(YTTRIUM_BARIUM_CUPRATE_INGOT, EvChapter.CHEMICAL_VAPOR_DEPOSITION));

    public static final Quest IV_ELECTRIC_MOTOR = CHAPTER.quest("iv_electric_motor", q -> q
            .at(10, -8.5)
            .subtitle("产能大爆发")
            .desc(d -> d
                    .line("强导压时代解锁了多方块版本的加工机器，它们具有更高的处理速度和更低的能耗！")
                    .line()
                    .line("现在你的目标是努力向{IV能源仓}和{量子电路}进发", GOLD, GOLD))
            .task(Task.item(GTItems.ELECTRIC_MOTOR_IV))
            .dependsOn(GRAPHENE_FOIL));

    public static final Quest IV_ASSEMBLER = CHAPTER.quest("iv_assembler", q -> q
            .at(10, -5.5)
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.IV]))
            .dependsOn(IV_ELECTRIC_MOTOR));

    public static final Quest IV_CIRCUIT_ASSEMBLER = CHAPTER.quest("iv_circuit_assembler", q -> q
            .at(12, -8.5)
            .desc(d -> d.line("老规矩，制造{IV电路组装机}需要消耗两个昂贵的{LuV级电路}", GOLD, GOLD))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.IV]))
            .dependsOn(IV_ELECTRIC_MOTOR, EvChapter.NANOPROCESSOR_MAINFRAME));

    public static final Quest HSSG_COIL_BLOCK = CHAPTER.quest("hssg_coil_block", q -> q
            .at(10, -3.5)
            .task(Task.item(GTBlocks.COIL_HSSG))
            .dependsOn(IV_ASSEMBLER, HSSG_INGOT));

    public static final Quest QUANTUM_PROCESSOR = CHAPTER.quest("quantum_processor", q -> q
            .at(14, -8.5)
            .title("{量子处理器}", GREEN)
            .subtitle("更便宜的EV级电路")
            .task(Task.item(GTItems.QUANTUM_PROCESSOR_EV))
            .dependsOn(QBIT_CPU_CHIP, FIBER_REINFORCED_PRINTED_CIRCUIT_BOARD, IV_CIRCUIT_ASSEMBLER));

    public static final Quest QUANTUM_PROCESSOR_ASSEMBLY = CHAPTER.quest("quantum_processor_assembly", q -> q
            .at(16, -8.5)
            .title("{量子处理器集群}", GREEN)
            .subtitle("更便宜的IV级电路")
            .task(Task.item(GTItems.QUANTUM_ASSEMBLY_IV))
            .dependsOn(QUANTUM_PROCESSOR));

    public static final Quest QUANTUM_PROCESSOR_SUPERCOMPUTER = CHAPTER.quest("quantum_processor_supercomputer", q -> q
            .at(18, -8.5)
            .title("{量子处理器超级计算机}", GREEN)
            .subtitle("更便宜的LuV级电路")
            .task(Task.item(GTItems.QUANTUM_COMPUTER_LuV))
            .dependsOn(QUANTUM_PROCESSOR_ASSEMBLY));

    public static final Quest QUANTUM_PROCESSOR_MAINFRAME = CHAPTER.quest("quantum_processor_mainframe", q -> q
            .at(18, -10.5)
            .title("{量子处理器主机}", GREEN)
            .subtitle("第一个ZPM电路")
            .task(Task.item(GTItems.QUANTUM_MAINFRAME_ZPM))
            .dependsOn(QUANTUM_PROCESSOR_SUPERCOMPUTER));

    public static final Quest SAMARIUM_IRON_ARSENIC_OXIDE_INGOT = CHAPTER.quest("samarium_iron_arsenic_oxide_ingot", q -> q
            .at(12, -2.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.SamariumIronArsenicOxide))
            .dependsOn(HSSG_COIL_BLOCK));

    public static final Quest OSTRUM_INGOT = CHAPTER.quest("ostrum_ingot", q -> q
            .at(12, 0)
            .task(Task.item(earth.terrarium.adastra.common.registry.ModItems.OSTRUM_INGOT.get()))
            .dependsOn(SAMARIUM_IRON_ARSENIC_OXIDE_INGOT, EvChapter.RAW_OSTRUM));

    public static final Quest IV_FIELD_GENERATOR = CHAPTER.quest("iv_field_generator", q -> q
            .at(14, -2.5)
            .task(Task.item(GTItems.FIELD_GENERATOR_IV))
            .dependsOn(SAMARIUM_IRON_ARSENIC_OXIDE_INGOT, IV_LAUNCHER_AND_IV_SENSOR));

    public static final Quest TIER_5_ROCKET = CHAPTER.quest("tier_5_rocket", q -> q
            .at(14, 0)
            .icon(ModItems.TIER_5_ROCKET.get())
            .desc(d -> d
                    .line("你现在能够前往T5星球")
                    .line()
                    .line("别忘了穿上{T3}器官，它需要你使用{装配线}制作{LuV部件}来合成", AQUA, GOLD, YELLOW))
            .task(Task.item(ModItems.TIER_5_ROCKET.get()), Task.item(GTOMaterials.RocketFuelH8n4c2o4.getBucket()))
            .dependsOn(IV_FIELD_GENERATOR, OSTRUM_INGOT));

    public static final Quest BLAZE_BLAST_FURNACE = CHAPTER.quest("blaze_blast_furnace", q -> q
            .at(14, -4.5)
            .desc(d -> d.line("更高效的电力高炉，具有最高64并行，同时对你的能源供应也提出了更高的要求"))
            .task(Task.item(MultiBlockD.BLAZE_BLAST_FURNACE))
            .dependsOn(IV_FIELD_GENERATOR));

    public static final Quest DRILLING_CONTROL_CENTER = CHAPTER.quest("drilling_control_center", q -> q
            .at(3, -6)
            .subtitle("流体钻机的好帮手")
            .task(Task.item(MultiBlockG.DRILLING_CONTROL_CENTER))
            .dependsOn(IV_FIELD_GENERATOR)
            .hideDependencyLines());

    public static final Quest ROCKET_MEGA_TURBINE = CHAPTER.quest("rocket_mega_turbine", q -> q
            .at(20, -10.5)
            .task(Task.item(GeneratorMultiblock.ROCKET_MEGA_TURBINE))
            .dependsOn(EvChapter.ROCKET_LARGE_TURBINE, QUANTUM_PROCESSOR_MAINFRAME)
            .optional());

    public static final Quest HASTELLOY_N_75_INGOT = CHAPTER.quest("hastelloy_n_75_ingot", q -> q
            .at(12, -4.5)
            .desc(d -> d.line("该用合金冶炼炉了"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HastelloyN75))
            .dependsOn(HSSG_COIL_BLOCK, EvChapter.ALLOY_BLAST_SMELTER));

    public static final Quest OZONATION_PURIFICATION_UNIT = CHAPTER.quest("ozonation_purification_unit", q -> q
            .at(12, -6.5)
            .task(Task.item(MultiBlockB.OZONATION_PURIFICATION_UNIT))
            .dependsOn(HASTELLOY_N_75_INGOT));

    public static final Quest OZONE_WATER_BUCKET = CHAPTER.quest("ozone_water_bucket", q -> q
            .at(14, -6.5)
            .icon(GTOMaterials.OzoneWater.getBucket())
            .task(Task.item(GTOMaterials.OzoneWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(OZONATION_PURIFICATION_UNIT));

    public static final Quest HPIC_CHIP = CHAPTER.quest("hpic_chip", q -> q
            .at(16, -6.5)
            .task(Task.item(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT))
            .dependsOn(OZONE_WATER_BUCKET));

    public static final Quest ASSEMBLY_LINE_CASING = CHAPTER.quest("assembly_line_casing", q -> q
            .at(18, -6.5)
            .task(Task.item(GTBlocks.CASING_ASSEMBLY_CONTROL))
            .dependsOn(HPIC_CHIP, IV_LAUNCHER_AND_IV_SENSOR));

    public static final Quest ASSEMBLY_LINE = CHAPTER.quest("assembly_line", q -> q
            .at(20, -6.5)
            .subtitle("新的组装方式")
            .desc(d -> d
                    .line("装配线最长17格，你可以切换EMI多方块结构中的P:0，P:1来查看最小和最大的装配线")
                    .line()
                    .line("装配线需要{有序}输入物品，你必须按照配方顺序给每个格子填充原料，离装配线主机{最近}的{输入总线}为配方中第一个材料", YELLOW, AQUA, GOLD)
                    .line()
                    .line("{流体无需有序}", GRAY.italic()))
            .task(Task.item(GTMultiMachines.ASSEMBLY_LINE))
            .dependsOn(ASSEMBLY_LINE_CASING));

    public static final Quest IV_ENERGY_INPUT_HATCH = CHAPTER.quest("iv_energy_input_hatch", q -> q
            .at(16, -4.5)
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.IV]))
            .dependsOn(HPIC_CHIP));

    public static final Quest AUTO_CONFIGURATION_MAINTENANCE_HATCH = CHAPTER.quest("auto_configuration_maintenance_hatch", q -> q
            .at(20, -8.5)
            .desc(d -> d
                    .line("根据游戏难度的不同有不同的调节范围，可以无条件地提升机器的速度和减慢发电机的燃料消耗速度！")
                    .line()
                    .line("{所有的环境维护仓也有它们的可配置版本}", GRAY.italic()))
            .task(Task.item(GTOMachines.AUTO_CONFIGURATION_MAINTENANCE_HATCH))
            .dependsOn(ASSEMBLY_LINE));

    public static final Quest CHEMICAL_PLANT = CHAPTER.quest("chemical_plant", q -> q
            .at(22.5, -6.5)
            .desc(d -> d.line("比一般的反应釜有线圈加成，还可以安装催化剂仓"))
            .task(Task.item(MultiBlockA.CHEMICAL_PLANT))
            .dependsOn(ASSEMBLY_LINE));

    public static final Quest VISIT_IO = CHAPTER.quest("visit_io", q -> q
            .at(16, 0)
            .desc(d -> d.line("别忘了穿上{T3}器官", AQUA))
            .task(Task.dimension(Dimension.IO).title("{访问木卫一}", LIGHT_PURPLE).icon(GTOGlobes.Items.IO_GLOBE.get()))
            .dependsOn(TIER_5_ROCKET, BeyondTheStarsChapter.CONFINEMENT_OF_SPACE_AND_TIME_ETERNAL));

    public static final Quest QUAKE_ORE_VEIN = CHAPTER.quest("quake_ore_vein", q -> q
            .at(16, 2)
            .icon(TagPrefix.rawOre, GTMaterials.Naquadah)
            .title("硅岩矿脉")
            .desc(d -> d.line("你在木卫一上拿到的硅岩将会成为你接下来几个阶段最重要的材料之一"))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Naquadah), Task.item(TagPrefix.rawOre, GTMaterials.Plutonium239))
            .dependsOn(VISIT_IO));

    public static final Quest Q_79A84A1D = CHAPTER.quest("q_79a84a1d", q -> q
            .at(8.5, -7)
            .icon(GCYMMachines.LARGE_AUTOCLAVE)
            .desc(d -> d
                    .line("IV时代解锁了大量的{多方块机器}，这些机器可以安装{并行控制仓}，{加速仓}和至多{8个}{能源仓}。将你的单方块机器升级为大批量加工巨兽吧！", YELLOW, GOLD, GOLD, GREEN, GOLD)
                    .line()
                    .line("详见{大批量加工}章节", link(MassiveManufacturingChapter.Q_68C24A1E)))
            .task(Task.checkmark().title("大批量加工"))
            .dependsOn(IV_ELECTRIC_MOTOR));

    public static final Quest INTEGRAL_FRAMEWORK_IV = CHAPTER.quest("integral_framework_iv", q -> q
            .at(8.5, -3.5)
            .subtitle("升级你的大型机器#1")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_IV))
            .dependsOn(HSSG_COIL_BLOCK));

    public static final Quest ADVANCED_TESSERACT_GENERATOR = CHAPTER.quest("advanced_tesseract_generator", q -> q
            .at(22, -8.5)
            .icon(GTOMachines.ADVANCED_TESSERACT_GENERATOR)
            .subtitle("轮椅自动化")
            .desc(d -> d
                    .line("你可以用这个来代替过去的存储总线方案")
                    .line()
                    .line("进阶版本可同时绑定{20}个目标，可以使用{坐标标签枪}批量绑定，{螺丝刀}点击可{切换模式}", AQUA, GOLD, GOLD, YELLOW)
                    .line()
                    .line("{顺序模式}：按照放入的信息卡的{顺序}作为优先级发送物品", GOLD, YELLOW)
                    .line()
                    .line("{轮询调度模式}：往放入的信息卡{均分}发送以样板为单位的物品", GOLD, YELLOW))
            .task(Task.item(GTOMachines.ADVANCED_TESSERACT_GENERATOR), Task.item(GTOItems.TESSERACT_TARGET_MARKER).optional())
            .dependsOn(ASSEMBLY_LINE, AeChapter.P2P_SAMPLE)
            .optional());

    public static final Quest CATALYST_HATCH = CHAPTER.quest("catalyst_hatch", q -> q
            .at(24.5, -6.5)
            .desc(d -> d
                    .line("催化剂仓允许你重复利用催化剂，每次使用只会减少{1点}耐久而非直接消耗", AQUA)
                    .line()
                    .line("注意催化剂仓中只能放入由{催化剂基底}合成的催化剂", GOLD)
                    .line()
                    .line("{如果此类催化剂不放入}{催化剂仓}{或者}{催化剂总成}{，则每次合成会直接消耗}", YELLOW, YELLOW.bold(), YELLOW, YELLOW.bold(), YELLOW))
            .task(Task.item(GTOMachines.CATALYST_HATCH), Task.item(GTOMachines.ADVANCED_CATALYST_HATCH))
            .dependsOn(CHEMICAL_PLANT));

    public static final Quest INFINITE_WATER_HATCH = CHAPTER.quest("infinite_water_hatch", q -> q
            .at(11.5, -10)
            .subtitle("真正意义上的无限水！")
            .task(Task.item(GTOMachines.INFINITE_WATER_HATCH))
            .dependsOn(IV_ELECTRIC_MOTOR));

    public static final Quest WIRELESS_DIMENSION_REPEATER = CHAPTER.quest("wireless_dimension_repeater", q -> q
            .at(3, -9)
            .subtitle("终于不需要在外星放发电机了")
            .desc(d -> d
                    .line("跨维度传无线电")
                    .line()
                    .line("注意搭建时需要在{每个}需要无线电的维度都搭建一个（除了无线能源塔所在的维度），能够使用的{最高电压}取决于其上安装的{整体框架}", YELLOW, YELLOW, GOLD)
                    .line()
                    .line("{如果你需要一些哭泣的黑曜石，可以去看看黑曜石的tooltips}", GRAY.italic()))
            .task(Task.item(MultiBlockG.WIRELESS_DIMENSION_REPEATER))
            .dependsOn(IV_ASSEMBLER)
            .hideDependencyLines());

    public static final Quest DATA_ACCESS_HATCH = CHAPTER.quest("data_access_hatch", q -> q
            .at(22, -4.5)
            .desc(d -> d
                    .line("将{数据载体}——目前是闪存——放入{数据访问仓}，即可在对应装配线解锁配方", GOLD, GOLD)
                    .line()
                    .line("目前一个装配线只能同时解锁{9个}配方，后续你可以制造{数据库}同时解锁更多配方！", GREEN, GOLD)
                    .line()
                    .line("同样的，你之后会遇到更加强大的数据访问仓"))
            .task(Task.item(GTResearchMachines.DATA_ACCESS_HATCH))
            .dependsOn(ASSEMBLY_LINE_RECIPE_DATA, ASSEMBLY_LINE));

    public static final Quest IV_256A_LASER_TARGET_HATCH = CHAPTER.quest("iv_256a_laser_target_hatch", q -> q
            .at(17, -3.5)
            .icon(GTMachines.LASER_INPUT_HATCH_256[GTValues.IV])
            .desc(d -> d
                    .line("从IV开始可以使用{激光能源仓}来为机器提供更高的功率", GOLD)
                    .line()
                    .line("同样有其无线版本"))
            .task(Task.item(GTMachines.LASER_INPUT_HATCH_256[GTValues.IV]), Task.item(GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.IV]))
            .dependsOn(IV_LAUNCHER_AND_IV_SENSOR, IV_ENERGY_INPUT_HATCH));

    public static final Quest ASSEMBLY_LINE_EASY = CHAPTER.quest("assembly_line_easy", q -> q
            .at(18.75, -7.5)
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .title("装配线{（简单）}", GREEN)
            .subtitle("简单模式下，装配线无需有序输入！")
            .task(Task.checkmark())
            .dependsOn(UtilChapter.Q_056EC4E6, ASSEMBLY_LINE)
            .optional().hideUntilDepsVisible());

    public static final Quest ELECTROPLATING_BATH = CHAPTER.quest("electroplating_bath", q -> q
            .at(10, -10.25)
            .desc(d -> d.line("来自GTO的新机器，用于{电镀}和{给转子镀膜}", YELLOW, YELLOW))
            .task(Task.item(MultiBlockH.ELECTROPLATING_BATH))
            .dependsOn(IV_ELECTRIC_MOTOR)
            .hideDependentLines());

    public static final Quest FISSION_REACTOR = CHAPTER.quest("fission_reactor", q -> q
            .at(3, -3)
            .desc(d -> d.line("能够获取高压蒸汽和超临界蒸汽用于发电，不过更重要的是获取核废料"))
            .task(Task.item(MultiBlockD.FISSION_REACTOR))
            .dependsOn(POLYVINYL_BUTYRAL_BUCKET, ELECTROPLATING_BATH));

    public static final Quest HEAT_EXCHANGER = CHAPTER.quest("heat_exchanger", q -> q
            .at(3, -1.5)
            .desc(d -> d.line("核电，搭配超临界蒸汽涡轮使用"))
            .task(Task.item(MultiBlockD.HEAT_EXCHANGER))
            .dependsOn(FISSION_REACTOR));

    public static final Quest NUCLEAR_WASTE = CHAPTER.quest("nuclear_waste", q -> q
            .at(1.5, -1.5)
            .desc(d -> d.line("可以产出些有趣的资源，包括你下个阶段会用到的{钋}", GOLD))
            .task(Task.item(GTOItems.NUCLEAR_WASTE))
            .dependsOn(FISSION_REACTOR));

    public static final Quest ADVANCED_SMD_CAPACITOR = CHAPTER.quest("advanced_smd_capacitor", q -> q
            .at(6, -0.5)
            .task(Task.item(GTItems.ADVANCED_SMD_CAPACITOR))
            .dependsOn(ADVANCED_SOLDERING_COMPONENTS, ELECTROPLATING_BATH));

    public static final Quest MOLECULAR_TRANSFORMER = CHAPTER.quest("molecular_transformer", q -> q
            .at(3, -7.5)
            .subtitle("分子料理")
            .desc(d -> d.line("YummYummmm……你想来一点吗？"))
            .task(Task.item(MultiBlockC.MOLECULAR_TRANSFORMER))
            .dependsOn(ELECTROPLATING_BATH)
            .hideDependencyLines());

    public static final Quest ASSEMBLY_LINE_EXPERT = CHAPTER.quest("assembly_line_expert", q -> q
            .at(19, -7.5)
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .title("装配线{（专家）}", LIGHT_PURPLE)
            .subtitle("专家模式下，装配线流体也需要有序输入！")
            .task(Task.checkmark())
            .dependsOn(ASSEMBLY_LINE, UtilChapter.Q_1FB1EC5B)
            .optional().hideUntilDepsVisible());

    public static final Quest OSTRUM_VAJRA = CHAPTER.quest("ostrum_vajra", q -> q
            .at(12, 2)
            .task(Task.item(ToolHelper.get(GTOToolType.VAJRA_EV, GTOMaterials.Ostrum).getItem()))
            .dependsOn(OSTRUM_INGOT));

    public static final Quest RHODIUM_PLATED_PALLADIUM_INGOT = CHAPTER.quest("rhodium_plated_palladium_ingot", q -> q
            .at(10, -12)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium))
            .dependsOn(ELECTROPLATING_BATH));

    public static final Quest Q_0BFFC724 = CHAPTER.quest("q_0bffc724", q -> q
            .at(18, -12.5)
            .icon(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE)
            .desc(d -> d
                    .line("工业空间站在{UV}阶段之前都不是必需品，不过我仍建议你尽早规划建造", UV)
                    .line()
                    .line("这里简单列出了你需要的新原料和新机器，但你还需要编写大量的样板{（真的很多！）}", ITALIC)
                    .line()
                    .line("别忘了去{穹宇居所}章节看看你可以制造的仓室", link(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE)))
            .task(Task.checkmark().title("关于工业空间站"))
            .dependsOn(QUANTUM_PROCESSOR_MAINFRAME));

    public static final Quest FIBER_EXTRUDER = CHAPTER.quest("fiber_extruder", q -> q
            .at(19, -12)
            .subtitle("空间站定制材料丝")
            .desc(d -> d.line("工业空间站的很多材料需要它"))
            .task(Task.item(MultiBlockH.FIBER_EXTRUDER))
            .dependsOn(EvChapter.RTM_ALLOY_COIL_BLOCK, Q_0BFFC724));

    public static final Quest BROMINE_BUCKET = CHAPTER.quest("bromine_bucket", q -> q
            .at(17, -12)
            .icon(GTMaterials.Bromine.getBucket())
            .desc(d -> d.line("盐水提纯"))
            .task(Task.item(GTMaterials.Bromine.getBucket()).optional(), Task.checkmark())
            .dependsOn(Q_0BFFC724));

    public static final Quest RHENIUM_DUST = CHAPTER.quest("rhenium_dust", q -> q
            .at(17, -13)
            .desc(d -> d.line("从辉钼矿粉中来"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Rhenium))
            .dependsOn(Q_0BFFC724));

    public static final Quest THERMO_PRESS = CHAPTER.quest("thermo_press", q -> q
            .at(19, -13)
            .subtitle("空间站定制材料块")
            .desc(d -> d.line("工业空间站的很多材料需要它"))
            .task(Task.item(MultiBlockH.THERMO_PRESS))
            .dependsOn(Q_0BFFC724));

    public static final Quest HIGH_PRESSURE_STEAM_INPUT_HATCH = CHAPTER.quest("high_pressure_steam_input_hatch", q -> q
            .at(20, -13)
            .subtitle("蒸汽机器死忠粉")
            .desc(d -> d.line("继续提升蒸汽机器的配方电压一级，解锁更多次数的超频"))
            .task(Task.item(GTOMachines.HIGH_PRESSURE_STEAM_INPUT_HATCH))
            .dependsOn(THERMO_PRESS)
            .optional());

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.ME_CPU, 8.5, -5.5))
                .link(QuestLink.of(LuvChapter.WELCOME_TO_THE_DRAMA_PRESSURE_STAGE, 10, -14))
                .link(QuestLink.of(EvChapter.RAW_OSTRUM, 10, 0))
                .link(QuestLink.of(BeyondTheStarsChapter.SPACE_QUARKTECH_CHESTPLATE, 14, 2))
                .link(QuestLink.of(EvChapter.NETHER_STAR, 18, 2))
                .link(QuestLink.of(BeyondTheStarsChapter.CONFINEMENT_OF_SPACE_AND_TIME_ETERNAL, 16, -1.5))
                .link(QuestLink.of(SpaceStationChapter.INDUSTRIAL_SPACE_STATION, 18, -14)));
    }
}
