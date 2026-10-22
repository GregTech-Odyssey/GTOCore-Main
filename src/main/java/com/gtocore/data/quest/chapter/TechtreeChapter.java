package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.data.quest.QuestTextures;
import com.gtocore.data.techtree.BaseNodes;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class TechtreeChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("techtree", c -> c
            .title("总览")
            .icon(GTOItems.DATA_CRYSTAL_MK1)
            .autofocus(TechtreeChapter.IV_SCANNER));

    public static final Quest IV_SCANNER = CHAPTER.quest("iv_scanner", q -> q
            .at(-8, -4)
            .title("科技树总览")
            .desc(d -> d
                    .line("科技树是新的扫描研究系统：扫描物品或流体得到研究点数（以及尤里卡），研究点数先写入数据晶片，再由基元扫描站转化为团队共有的研究点数。")
                    .line()
                    .line("研究点数与算力共同推进科技节点；节点解锁后，才能导出对应的配方数据或获得全局效果。")
                    .line()
                    .line("本章按从左到右的流程介绍：扫描 → 晶片 → 研究点数 → 科技节点 → 数据导出。"))
            .task(Task.item(GTMachines.SCANNER[GTValues.IV])));

    public static final Quest Q_68213A51 = CHAPTER.quest("q_68213a51", q -> q
            .at(-8, 6)
            .title("新旧流程对照")
            .desc(d -> d
                    .line("旧流程是：扫描仪/研究站消耗算力 → 闪存等介质 → 数据库 → 配方。")
                    .line("新流程拆成两个阶段：扫描仪只负责把对象写成研究点数晶片；数据中心再检查团队研究点数并积累算力来解锁科技节点。")
                    .line("尤里卡不是必需条件，但扫描指定对象可以降低对应节点所需的算力，前期节点甚至可以完全免算力解锁。"))
            .task(Task.checkmark()));

    public static final Quest IV_SCANNER_2 = CHAPTER.quest("iv_scanner_2", q -> q
            .at(-1, -4)
            .title("扫描仪革新")
            .desc(d -> d
                    .line("扫描仪的输入是一个可扫描对象和一张有足够剩余空间的数据晶片，输出是写入研究点数的晶片。")
                    .line()
                    .line("扫描时间为简单/普通/专家难度的10/20/30秒；基础耗能为本次研究点数总字节数 × 8 + 8 EU/t，并支持超频。")
                    .line()
                    .line("物品、流体和同一材料的不同形态会共用扫描记录；重复扫描会按难度将收益降低到25%、6.25%或1.5625%。"))
            .task(Task.item(GTMachines.SCANNER[GTValues.IV]))
            .dependsOn(IV_SCANNER));

    public static final Quest Q_2E41B952 = CHAPTER.quest("q_2e41b952", q -> q
            .at(2, -7)
            .desc(d -> d
                    .line("扫描目标的配方可以在 EMI 的“数据扫描”分类中查看。")
                    .line("左侧放入待扫描的物品或流体，右侧放入能装下本次全部研究点数的数据晶片；容量不足时配方不会运行。")
                    .line("扫描完成时，如果目标是某个科技节点的尤里卡对象，会同时记录尤里卡。"))
            .task(Task.checkmark().title("完成一次数据扫描"))
            .dependsOn(IV_SCANNER_2));

    public static final Quest DATA_CRYSTAL_MK1 = CHAPTER.quest("data_crystal_mk1", q -> q
            .at(-2, -9)
            .title("数据晶片与容量")
            .desc(d -> d
                    .line("数据晶片按等级提供存储空间：1级为4 kB，之后每升一级容量提高32倍。")
                    .line("每种研究点数标签（ResearchTag）的单点字节数不同，例如材料16 B、数据存储64 B、计算/能源/热力学/组装128 B、精灵/催化256 B；更高阶标签会占用更多空间。")
                    .line("在 EMI 搜索研究点数可以查看标签的显示名、每点占用空间以及一张晶片的总占用。"))
            .task(Task.item(GTOItems.DATA_CRYSTAL_MK1))
            .dependsOn(IV_SCANNER_2));

    public static final Quest Q_0A4FD8FD = CHAPTER.quest("q_0a4fd8fd", q -> q
            .at(-4, -7)
            .desc(d -> d
                    .line("同一材料的板、锭、流体等形态会被视为同一种扫描对象。")
                    .line("同一对象重复扫描时，扫描记录与材料记录不会重复增加；收益会按扫描难度衰减，因此应优先扫描尚未学习的对象。")
                    .line("扫描目标是否能触发尤里卡，也可以在 EMI 的科技节点页面中反查。"))
            .task(Task.checkmark().title("理解扫描记录与重复惩罚"))
            .dependsOn(IV_SCANNER_2));

    public static final Quest Q_0F425AE2 = CHAPTER.quest("q_0f425ae2", q -> q
            .at(5, 8)
            .title("材料")
            .desc(d -> d
                    .line("{材料研究点数}", GOLD.bold())
                    .line("{每点占用：}16 B，是晶片中最紧凑的研究点数标签。", YELLOW)
                    .line("扫描任何能识别为 GT 材料的物品或流体会得到32点；同一材料的不同形态共用扫描记录与重复扫描惩罚。")
                    .line("这是大量组件、加工与材料科技节点的基础消耗。"))
            .task(Task.checkmark().title("材料研究点数"))
            .hideDependencyLines());

    public static final Quest Q_70763186 = CHAPTER.quest("q_70763186", q -> q
            .at(7, 8)
            .title("数据存储")
            .desc(d -> d
                    .line("{数据存储研究点数}", GOLD.bold())
                    .line("{每点占用：}64 B。", YELLOW)
                    .line("扫描1M、4M、16M、64M、256M与无限存储组件会分别得到10、15、30、40、50与120点；数据棒、数据球和数据模块也会提供少量点数。")
                    .line("数据形态测试厂可通过压缩测试持续生产此类研究点数。"))
            .task(Task.checkmark().title("数据存储研究点数")));

    public static final Quest Q_554100B9 = CHAPTER.quest("q_554100b9", q -> q
            .at(9, 8)
            .title("计算")
            .desc(d -> d
                    .line("{计算研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("主要由计算数据支架产生：每5秒读取所在计算机或超算中心的最大算力与当前使用算力，产出 √(最大算力 + 当前使用算力) 点。")
                    .line("研究点数写入支架内的数据晶片，之后仍需送入基元扫描站。"))
            .task(Task.checkmark().title("计算研究点数")));

    public static final Quest Q_3EB8A458 = CHAPTER.quest("q_3eb8a458", q -> q
            .at(11, 8)
            .title("热力学")
            .desc(d -> d
                    .line("{热力学研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描热管、炽热外壳、电力高炉、真空冷冻机等热力学设备可获得少量点数。")
                    .line("热力学分析平台在高温与低温体系达到平衡条件后分析30秒，结束时按温差/250 K生成研究点数。"))
            .task(Task.checkmark().title("热力学研究点数")));

    public static final Quest Q_46370C47 = CHAPTER.quest("q_46370c47", q -> q
            .at(13, 8)
            .title("能源")
            .desc(d -> d
                    .line("{能源研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描能源水晶、兰波顿电池、能量模块与更高阶电池可以直接获得点数。")
                    .line("能源数据支架安装在发电设备上；完成满足功率门槛的配方时，按输出功率等级的平方关系生成能源研究点数。"))
            .task(Task.checkmark().title("能源研究点数")));

    public static final Quest Q_635018E8 = CHAPTER.quest("q_635018e8", q -> q
            .at(15, 8)
            .title("组装")
            .desc(d -> d
                    .line("{组装研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描能由组装机、装配线、电路组装机或太空组装模块产出的物品，通常会额外得到4点组装研究点数。")
                    .line("数据棒、数据球、数据模块和后期数据介质也会提供组装研究点数。"))
            .task(Task.checkmark().title("组装研究点数")));

    public static final Quest Q_4E12983A = CHAPTER.quest("q_4e12983a", q -> q
            .at(17, 8)
            .title("精灵")
            .desc(d -> d
                    .line("{精灵研究点数}", GOLD.bold())
                    .line("{每点占用：}256 B。", YELLOW)
                    .line("扫描带有魔法材料标记的物品或流体会得到8点精灵研究点数，同时仍会得到该材料对应的材料研究点数。")
                    .line("这是魔法线与科技树衔接时使用的研究点数标签。"))
            .task(Task.checkmark().title("精灵研究点数")));

    public static final Quest Q_02963400 = CHAPTER.quest("q_02963400", q -> q
            .at(19, 8)
            .title("催化")
            .desc(d -> d
                    .line("{催化研究点数}", GOLD.bold())
                    .line("{每点占用：}256 B。", YELLOW)
                    .line("扫描带有催化剂生成标记的材料会得到3点催化研究点数。")
                    .line("催化数据支架安装在化工厂等设备上；含催化剂的配方完成后，依据总效率、并行数和原始配方等级生成点数。"))
            .task(Task.checkmark().title("催化研究点数")));

    public static final Quest Q_2C77D899 = CHAPTER.quest("q_2c77d899", q -> q
            .at(5, 10)
            .title("星际工程")
            .desc(d -> d
                    .line("{星际工程研究点数}", GOLD.bold())
                    .line("{每点占用：}512 B。", YELLOW)
                    .line("扫描各级火箭、太空电梯与电力模块可直接取得点数；能独立在太空工作的多方块主机也会提供3点。")
                    .line("首次解锁一颗星球时，还会按2^星球等级直接加入团队总量；太空电梯工程信息模块可继续生产。"))
            .task(Task.checkmark().title("星际工程研究点数"))
            .hideDependencyLines());

    public static final Quest Q_1CD85CAE = CHAPTER.quest("q_1cd85cae", q -> q
            .at(7, 10)
            .title("机械")
            .desc(d -> d
                    .line("{机械研究点数}", GOLD.bold())
                    .line("{每点占用：}1024 B。", YELLOW)
                    .line("扫描一个多方块主机物品会得到1点机械研究点数；扫描指南针也会得到1点。")
                    .line("机械标签主要用于机器、装配设备与巨构类科技节点。"))
            .task(Task.checkmark().title("机械研究点数")));

    public static final Quest Q_497561B8 = CHAPTER.quest("q_497561b8", q -> q
            .at(9, 10)
            .title("粒子")
            .desc(d -> d
                    .line("{粒子研究点数}", GOLD.bold())
                    .line("{每点占用：}4096 B。", YELLOW)
                    .line("该标签为粒子物理研究预留。当前扫描表与研究配方没有稳定的通用生产来源，只有实际科技节点列出需求时才需要准备。"))
            .task(Task.checkmark().title("粒子研究点数")));

    public static final Quest Q_0163AF39 = CHAPTER.quest("q_0163af39", q -> q
            .at(11, 10)
            .title("生物")
            .desc(d -> d
                    .line("{生物研究点数}", GOLD.bold())
                    .line("{每点占用：}4096 B。", YELLOW)
                    .line("扫描干细胞、生物细胞、菌种粉和心肌细胞簇可获得生物研究点数。")
                    .line("生物研究太空舱运行专用配方时还能大量生成此类点数，并写入其数据晶片。"))
            .task(Task.checkmark().title("生物研究点数")));

    public static final Quest Q_67E6EF7B = CHAPTER.quest("q_67e6ef7b", q -> q
            .at(13, 10)
            .title("光学")
            .desc(d -> d
                    .line("{光学研究点数}", GOLD.bold())
                    .line("{每点占用：}65536 B。", YELLOW)
                    .line("扫描激光二极管、非线性光学透镜、光学晶圆与光学电路板等器件可获得少量点数。")
                    .line("光束引导计算测试配方满足指定波长、强度和偏振条件后，也会生成光学研究点数。"))
            .task(Task.checkmark().title("光学研究点数")));

    public static final Quest Q_4B48A95D = CHAPTER.quest("q_4b48a95d", q -> q
            .at(15, 10)
            .title("量子")
            .desc(d -> d
                    .line("{量子研究点数}", GOLD.bold())
                    .line("{每点占用：}2097152 B。", YELLOW)
                    .line("该标签面向 UEV 及以后的量子研究。当前没有稳定的通用生产来源，准备高等级数据晶片并以节点详情为准。"))
            .task(Task.checkmark().title("量子研究点数")));

    public static final Quest Q_692F56B2 = CHAPTER.quest("q_692f56b2", q -> q
            .at(17, 10)
            .title("奇异")
            .desc(d -> d
                    .line("{奇异研究点数}", GOLD.bold())
                    .line("{每点占用：}67108864 B。", YELLOW)
                    .line("扫描里德堡旋量组件会得到2点；龙心肌细胞簇、改造龙心与觉醒龙心会在生物研究点数之外额外提供3点。")
                    .line("这是 UIV 阶段附近的高密度研究点数，低级晶片很容易因容量不足而无法扫描。"))
            .task(Task.checkmark().title("奇异研究点数")));

    public static final Quest Q_6F4A78AC = CHAPTER.quest("q_6f4a78ac", q -> q
            .at(19, 10)
            .title("超因果")
            .desc(d -> d
                    .line("{超因果研究点数}", GOLD.bold())
                    .line("{每点占用：}2147483648 B。", YELLOW)
                    .line("扫描闭合类时曲线制导单元或小宇宙等后期数据介质可以获得少量超因果研究点数。")
                    .line("这是当前字节密度最高的标签，必须使用高等级晶片并预留足够容量。"))
            .task(Task.checkmark().title("超因果研究点数")));

    public static final Quest Q_094AF354 = CHAPTER.quest("q_094af354", q -> q
            .at(6, -4)
            .icon(GTItems.FIELD_GENERATOR_LuV)
            .desc(d -> d
                    .line("科技节点的解锁条件由三部分组成：前置节点、对应研究点数标签的研究点数和 CWU 算力。")
                    .line("节点解锁后会把它关联的配方数据或机器效果加入团队可用内容；同一节点不会在队伍中重复解锁。")
                    .line("在 EMI 中查看科技节点，可以看到前置节点、研究点数、算力、尤里卡和该节点提供的奖励。")
                    .line()
                    .line("前期的研究节点解锁起来相对简单，直接在扫描仪内可通过扫描特定物品获取100%的尤里卡来解锁。")
                    .line()
                    .line("在解锁研究站之后，尤里卡就不足以完成100%的进度，需要你投入算力（CWU）来完成剩下的部分。")
                    .line()
                    .line("部分研究还需要研究点数，你需要使用基元扫描站将晶片中的研究点数转化为团队共有的点数。")
                    .line()
                    .line("推荐解锁的第一个节点为：{装配线基础部件}。将一枚数据晶片与IV力场发生器放入扫描仪，即可触发尤里卡并完成此研究节点。", link(TechnodeChapter.COMPONENT_IN_ASSEMBLY_LINE)))
            .task(Task.checkmark().title("解锁第一个科技节点"))
            .dependsOn(IV_SCANNER_2));

    public static final Quest Q_0A227168 = CHAPTER.quest("q_0a227168", q -> q
            .at(8, -1)
            .desc(d -> d
                    .line("扫描材料或使用专用机器都可以获得对应领域的研究点数标签。")
                    .line("当前主要来源：精灵来自魔法材料扫描；组装来自可由组装机、太空组装或装配线处理的材料；计算来自扫描或计算数据支架；生物来自生物研究太空舱；催化来自催化材料或催化数据支架。")
                    .line("星际工程来自解锁星球的数据，也可由太空电梯工程信息模块提供；数据存储、能源、热力学和光学分别有对应的数据形态测试厂、能源数据支架、热力学分析平台和激光计算测试平台。"))
            .task(Task.checkmark().title("认识各领域的研究点数来源"))
            .dependsOn(Q_094AF354));

    public static final Quest DATA_EXPORT_MACHINE = CHAPTER.quest("data_export_machine", q -> q
            .at(4, -1)
            .title("导出科技配方")
            .desc(d -> d
                    .line("数据导出机可以把已解锁科技节点的配方数据复制到闪存、数据球或数据模块等承载介质。")
                    .line()
                    .line("在界面选择配方奖励，再放入对应等级的数据物品并确认导出；导出过程固定消耗7680 EU/t并需要一段复制时间。")
                    .line()
                    .line("数据中心也集成了配方导出和数据访问页面，发展出数据中心后可以直接在那里完成同样的操作。"))
            .task(Task.item(GTOMachines.DATA_EXPORT_MACHINE))
            .dependsOn(Q_094AF354));

    public static final Quest COMMAND_BLOCK = CHAPTER.quest("command_block", q -> q
            .at(-8, 10)
            .title("管理与调试命令")
            .desc(d -> d
                    .line("科技树管理界面可使用 /gtocore techtree ui 打开。玩家可以切换科技树、预览节点并查看解锁状态。")
                    .line("管理员命令 /gtocore techtree unlock <玩家> <科技树ID> <节点ID> 会强制解锁节点及其依赖；/gtocore techtree reset <玩家> <科技树ID> 只重置指定科技树。")
                    .line("/gtocore researchtag add/remove 可调整队伍研究点数；/gtocore scan add_hand、remove_hand、get_self 用于维护或查看扫描记录。"))
            .task(Task.item(Items.COMMAND_BLOCK))
            .hideDependencyLines());

    public static final Quest INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM = CHAPTER.quest("intelligent_scanning_management_platform", q -> q
            .at(-1, 3)
            .title("智能扫描管理")
            .desc(d -> d
                    .line("智能扫描管理平台把扫描队列自动化。它可以从界面选择多个物品或流体循环扫描，也可以持续寻找尚未学习的目标。")
                    .line("平台每次工作只消耗一张晶片；物品目标消耗1个物品，流体目标消耗1000 mB流体。")
                    .line("它支持四种模式：选定物品循环、选定物品单次、未学习物品循环、未学习物品单次。")
                    .page()
                    .line("智能扫描 ME 代理连接 ME 网络，为平台提供库存中的物品与流体。")
                    .line("数据晶体不会加入扫描目标；带标签的对象按无标签的基础类型处理；流体只有库存超过1000 mB时才会进入列表。")
                    .line("代理每2秒刷新一次可用目标，适合把扫描平台接入自动化仓储。"))
            .task(Task.item(ExResearchMachines.INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM))
            .dependsOn(IV_SCANNER_2));

    public static final Quest COMPUTATIONAL_DATA_HOLDER = CHAPTER.quest("computational_data_holder", q -> q
            .at(12, -1)
            .title("计算研究点数")
            .desc(d -> d
                    .line("计算研究点数可以由计算数据支架生产。支架安装在计算机或超算中心等能输出算力流动数据的设备上。")
                    .line("每5秒生成 √(最大算力 + 当前使用算力) 的计算研究点数，并把结果存入机器内的数据晶片。"))
            .task(Task.item(ExResearchMachines.COMPUTATIONAL_DATA_HOLDER))
            .dependsOn(Q_0A227168));

    public static final Quest COMPUTATIONAL_DATA_HOLDER_2 = CHAPTER.quest("computational_data_holder_2", q -> q
            .at(8, 3)
            .title("各种研究点数")
            .desc(d -> d
                    .line("其余研究点数生产设备遵循同一原则：设备产生带标签的研究点数，先写入晶片，再送入基元扫描站。")
                    .line("催化数据支架安装在化工厂或激光化工厂；能源数据支架产出能源研究点数；热力学分析平台和激光计算测试平台分别产出热力学与光学研究点数。")
                    .line("生物研究太空舱、太空电梯工程信息模块和多方块扫描还会提供生物、星际工程与机械研究点数。"))
            .task(
                    Task.item(ExResearchMachines.COMPUTATIONAL_DATA_HOLDER),
                    Task.item(ExResearchMachines.CATALYSIS_DATA_HOLDER),
                    Task.item(ExResearchMachines.THERMODYNAMIC_DATA_HOLDER),
                    Task.item(ExResearchMachines.INTERSTELLAR_ENGINEERING_DATA_HOLDER),
                    Task.item(ExResearchMachines.ENERGY_DATA_HOLDER),
                    Task.item(ExResearchMachines.STORAGE_DATA_HOLDER),
                    Task.item(ExResearchMachines.BIOLOGY_DATA_HOLDER),
                    Task.item(ExResearchMachines.OPTICAL_DATA_HOLDER))
            .dependsOn(Q_0A227168));

    public static final Quest Q_4D2E1954 = CHAPTER.quest("q_4d2e1954", q -> q
            .at(13, 12)
            .desc(d -> d
                    .line("光束系统把光束作为独立的机器输入：光束具有波长、强度和偏振等属性，配方可以要求其中的一个或多个范围。")
                    .line("光束发生器、偏振器、反射/转向部件和光束接入部件共同组成光路；光路变化时，Beam 管理器会重新计算路径并同步给客户端。")
                    .line("需要光学研究点数的配方会在 EMI 中显示所需光束的波长、强度和偏振条件。"))
            .task(Task.checkmark().title("理解 Beam 光束机制")));

    public static final Quest Q_2E2EA0A8 = CHAPTER.quest("q_2e2ea0a8", q -> q
            .at(2, 10)
            .desc(d -> d
                    .line("基础标签之外的高阶标签会随着流程逐步开放。粒子、量子、奇异和超因果是后期内容的预留或高阶研究方向，当前不一定都有独立的生产设备。")
                    .line("当某个标签的来源加入游戏后，扫描或对应研究设备会把它写入晶片；节点详情会明确列出实际需要的标签。"))
            .task(Task.checkmark().title("高阶研究标签")));

    public static final Quest ME_MUFFLER_HATCH = CHAPTER.quest("me_muffler_hatch", q -> q
            .at(-8, 8)
            .title("流程与配方迁移")
            .desc(d -> d
                    .line("研究节点和流程配方都已迁移到科技树条件。组件、巨构、空间和能源等内容按主题分布在不同节点，节点的前置、研究点数和尤里卡共同构成新的流程门槛。")
                    .line("因此，旧任务中直接制作某个配方或把数据塞入数据库的步骤，可能需要先解锁科技节点，再用数据导出机或数据中心导出新的配方数据。"))
            .task(Task.item(GTAEMachines.MUFFLER_HATCH_ME))
            .hideDependencyLines());

    public static final Quest Q_1A6BA4C0 = CHAPTER.quest("q_1a6ba4c0", q -> q
            .at(-6, 8)
            .title("太空流程调整")
            .desc(d -> d
                    .line("空间站激光仓和高级仓室不再由空间站高能转换调配舱直接锁定，而是改由对应科技节点解锁。")
                    .line("空间站高能转换调配舱现在为其他拓展舱体的超频仓提供1点增幅，并额外增强已安装的太空电梯连接舱；同一空间站安装多个时只生效一个。")
                    .line("太空电梯连接舱仍负责连接当前星球的太空电梯，但原本单独承担的增幅职责被削弱。"))
            .task(Task.checkmark().title("理解太空流程调整"))
            .dependsOn(ME_MUFFLER_HATCH));

    public static final Quest Q_15A72741 = CHAPTER.quest("q_15a72741", q -> q
            .at(-2, 8)
            .title("巨构研究分组")
            .desc(d -> d
                    .line("巨构研究按主题拆分到不同科技节点，不再用一份通用研究数据覆盖所有巨构。")
                    .line("每类节点包含同一主题的机器、对应的前置节点、研究点数、算力与尤里卡；需要哪个巨构，就在科技树或 EMI 中追踪它所属的节点。")
                    .line("解锁节点后，还要把该节点提供的配方奖励导出到正确等级的数据介质。"))
            .task(Task.checkmark().title("按主题追踪巨构研究")));

    public static final Quest Q_4C99BD45 = CHAPTER.quest("q_4c99bd45", q -> q
            .at(-4, 8)
            .title("部件解锁重排")
            .desc(d -> d
                    .line("部件装配被重排为独立的部件装配科技树。每一级力场发生器是下一级装配线部件节点的尤里卡对象，越到后期减免比例越低。")
                    .line("LuV 及以上的部件装配线外壳各有科技节点；解锁当前外壳同时要求当前等级部件节点与上一级外壳节点。")
                    .line("因此推进下一电压等级前，应同时准备上一等级力场发生器、组装研究点数和足够的 CWU。"))
            .task(Task.checkmark().title("理解部件与外壳前置")));

    public static final Quest DATA_BASE = CHAPTER.quest("data_base", q -> q
            .at(0, -9)
            .title("数据基础")
            .desc(d -> d
                    .line("第一阶段的研究节点用于把扫描链路接入科技树。")
                    .line("该节点需要数据存储研究点数与数据中心积累的算力；扫描数据中心或相关数据材料可触发尤里卡，降低解锁所需算力。"))
            .task(Task.techNode(BaseNodes.DataBase))
            .dependsOn(IV_SCANNER_2));

    public static final Quest COMPUTATION_ARRAY = CHAPTER.quest("computation_array", q -> q
            .at(12, -11)
            .title("计算阵列")
            .desc(d -> d.line("计算阵列节点代表研究算力的基础设施。解锁后可以把更高等级的计算数据接入数据中心，为后续节点提供稳定的计算研究点数与算力供应。"))
            .task(Task.techNode(BaseNodes.ComputationArray)));

    public static final Quest SCAN_STATION = CHAPTER.quest("scan_station", q -> q
            .at(8, -7)
            .title("基元扫描站")
            .desc(d -> d
                    .line("基元扫描站节点把晶片解析成团队研究点数，是数据中心能够读取研究资料的前置。")
                    .line("扫描每张晶片会固定消耗一段时间和能量；晶片内数据量只影响转入团队的研究点数，不改变单张晶片的解析成本。"))
            .task(Task.techNode(BaseNodes.ScanStation))
            .dependsOn(Q_094AF354));

    public static final Quest DATAUNIT_SCANNING_STATION = CHAPTER.quest("dataunit_scanning_station", q -> q
            .at(12, -7)
            .title("解析数据晶片")
            .desc(d -> d
                    .line("扫描数据不会直接成为科技节点的解锁材料。")
                    .line("将装有研究点数的数据晶片放入基元扫描站，扫描站会把其中的数据解析为团队共享的研究点数；晶片内有多少数据不影响单张晶片的固定解析耗时与耗能。")
                    .line("只有进入团队总量后，数据中心才会检查这些研究点数是否满足科技节点需求。"))
            .task(Task.item(ExResearchMachines.PRIMORDIAL_SCANNING_STATION))
            .dependsOn(COMPUTATION_ARRAY, SCAN_STATION));

    public static final Quest DATA_IN_RESEARCH = CHAPTER.quest("data_in_research", q -> q
            .at(14, -4)
            .title("数据中心研究")
            .desc(d -> d
                    .line("数据中心是科技树的研究核心，也是全局配方数据中心。")
                    .line("把数据访问仓接入数据中心后，仓位和已安装的配方数据会提高可接受的算力上限；研究状态会消耗电力并需要多氯联苯冷却剂。")
                    .line("在数据中心界面选择科技节点并启动研究，外部算力会逐 tick 累积到节点；达到尤里卡减免后的 CWU 要求时会尝试解锁。")
                    .line("{注意：}若此时前置节点或研究点数不足，节点不会解锁，但数据中心仍会停止本次研究；补足条件后需要重新选择节点并启动。", RED))
            .task(Task.techNode(BaseNodes.DataCenter))
            .dependsOn(Q_094AF354, DATAUNIT_SCANNING_STATION, COMPUTATIONAL_DATA_HOLDER));

    public static final Quest Q_2CB2DFC2 = CHAPTER.quest("q_2cb2dfc2", q -> q
            .at(19, -4)
            .desc(d -> d
                    .line("数据中心超频允许研究过程使用超过当前单次输入上限的总算力。")
                    .line("解锁后，当数据中心为节点处理的算力达到最大可接受算力的3倍时，研究速度提升至2倍；更高总算力会继续按超频逻辑增加超频等级。")
                    .line("研究仍然需要满足节点的前置、研究点数标签和尤里卡条件；超频只改变算力推进速度。"))
            .task(Task.checkmark().title("解锁数据中心超频"))
            .dependsOn(DATA_IN_RESEARCH));

    public static final Quest Q_0F66B0E1 = CHAPTER.quest("q_0f66b0e1", q -> q
            .at(-10.5, -7)
            .title("测试声明")
            .desc(d -> d.line("此章节仍在测试阶段，其呈现效果不代表最终成品章节结构。"))
            .task(Task.checkmark()));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -8.5, -4.5, 2, 2)));
    }
}
