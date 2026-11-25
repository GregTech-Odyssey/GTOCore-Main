package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.ExResearchMachines;
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
            .at(-7.5, -2.5)
            .title("科技树总览")
            .desc(d -> d
                    .line("{科技树}是新的扫描研究系统：扫描物品或流体得到研究点数（以及尤里卡），研究点数先写入数据晶片，再由基元扫描站转化为团队共有的研究点数。", YELLOW)
                    .line()
                    .line("{研究点数与算力共同推进科技节点}；节点解锁后，才能导出对应的配方数据或获得全局效果。", YELLOW)
                    .line()
                    .line("本章按从左到右的流程介绍：扫描 → 晶片 → 研究点数 → 科技节点 → 数据导出。"))
            .task(Task.item(GTMachines.SCANNER[GTValues.IV])));

    public static final Quest Q_68213A51 = CHAPTER.quest("q_68213a51", q -> q
            .at(-8, 3)
            .title("新旧流程对照")
            .desc(d -> d
                    .line("旧流程：扫描仪/研究站消耗算力 → 闪存等介质 → 数据库 → 配方。")
                    .line("新流程：{扫描仪}扫描物品得到研究点数晶片；前期靠尤里卡直接解锁科技节点；后期{数据中心}检查团队研究点数并积累算力来解锁科技节点。用{数据导出机}或{数据中心}导出到闪存等介质 → 数据库 → 配方。", YELLOW, YELLOW, YELLOW, YELLOW)
                    .line("{尤里卡不是必需条件}，但扫描指定对象可以降低对应节点所需的算力，前期节点甚至可以完全免算力解锁。", RED))
            .task(Task.checkmark()));

    public static final Quest IV_SCANNER_2 = CHAPTER.quest("iv_scanner_2", q -> q
            .at(-4.5, -2.5)
            .title("扫描仪革新")
            .desc(d -> d
                    .line("扫描仪的{输入}是一个{可扫描对象}和一张有足够剩余空间的{数据晶片}，输出是写入研究点数的晶片。", RED, RED, RED)
                    .line()
                    .line("同一材料的不同形态会共用扫描记录。{尤里卡只有首次扫描能触发}", YELLOW)
                    .line()
                    .line("重复扫描收益将降低到25%(简单)、6.25%(普通)或1.5625%(专家)。")
                    .line()
                    .line("{扫描时间简单/普通/专家难度为10/20/30秒；基础耗能为本次研究点数总字节数 × 8 + 8 EU/t，支持超频。}", DARK_GRAY))
            .task(Task.item(GTMachines.SCANNER[GTValues.IV]))
            .dependsOn(IV_SCANNER));

    public static final Quest Q_2E41B952 = CHAPTER.quest("q_2e41b952", q -> q
            .at(-3.5, -4)
            .desc(d -> d
                    .line("扫描目标的配方可以在 EMI 的“数据扫描”分类中查看。")
                    .line("左侧放入{待扫描的物品或流体}，右侧放入能装下本次全部研究点数的{数据晶片}；容量不足时配方不会运行。", RED, RED)
                    .line("扫描完成时，如果目标是某个科技节点的尤里卡对象，会同时触发{尤里卡！}", YELLOW))
            .task(Task.checkmark().title("完成一次数据扫描"))
            .dependsOn(IV_SCANNER_2));

    public static final Quest DATA_CRYSTAL_MK1 = CHAPTER.quest("data_crystal_mk1", q -> q
            .at(-5.5, -4)
            .title("数据晶片与容量")
            .desc(d -> d
                    .line("数据晶片按等级提供存储空间：1级为4 kB，每升一级容量提高32倍。")
                    .line("{每种研究点数的单点字节数不同}，例如材料16 B、数据存储64 B、计算/能源/热力学/组装128 B、精灵/催化256 B；更高阶标签会占用更多空间。", AQUA)
                    .line("在 EMI 搜索研究点数可以查看标签的显示名、每点占用空间以及一张晶片的总占用。"))
            .task(Task.item(GTOItems.DATA_CRYSTAL_MK1))
            .dependsOn(IV_SCANNER_2));

    public static final Quest Q_0F425AE2 = CHAPTER.quest("q_0f425ae2", q -> q
            .at(5.5, 3.5)
            .title("材料")
            .desc(d -> d
                    .line("{材料研究点数}", GOLD.bold())
                    .line("{每点占用：}16 B，是晶片中最紧凑的研究点数标签。", YELLOW)
                    .line("扫描任何能识别为 GT 材料的物品或流体会得到32点；同一材料的不同形态共用扫描记录与重复扫描惩罚。")
                    .line("这是大量组件、加工与材料科技节点的基础消耗。"))
            .task(Task.checkmark().title("材料研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_70763186 = CHAPTER.quest("q_70763186", q -> q
            .at(3.5, 0)
            .title("数据存储")
            .desc(d -> d
                    .line("{数据存储研究点数}", GOLD.bold())
                    .line("{每点占用：}64 B。", YELLOW)
                    .line("扫描1M、4M、16M、64M、256M与无限存储组件会分别得到10、15、30、40、50与120点；数据棒、数据球和数据模块也会提供少量点数。")
                    .line("数据形态测试厂可通过压缩测试持续生产此类研究点数。"))
            .task(Task.checkmark().title("数据存储研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_554100B9 = CHAPTER.quest("q_554100b9", q -> q
            .at(2, 1.5)
            .title("计算")
            .desc(d -> d
                    .line("{计算研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("主要由计算数据支架产生：每5秒读取所在计算机或超算中心的最大算力与当前使用算力，产出 √(最大算力 + 当前使用算力) 点。")
                    .line("研究点数写入支架内的数据晶片，之后仍需送入基元扫描站。"))
            .task(Task.checkmark().title("计算研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_3EB8A458 = CHAPTER.quest("q_3eb8a458", q -> q
            .at(2.5, 0.5)
            .title("热力学")
            .desc(d -> d
                    .line("{热力学研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描热管、炽热外壳、电力高炉、真空冷冻机等热力学设备可获得少量点数。")
                    .line("热力学分析平台在高温与低温体系达到平衡条件后分析30秒，结束时按温差/250 K生成研究点数。"))
            .task(Task.checkmark().title("热力学研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_46370C47 = CHAPTER.quest("q_46370c47", q -> q
            .at(4.5, 0)
            .title("能源")
            .desc(d -> d
                    .line("{能源研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描能源水晶、兰波顿电池、能量模块与更高阶电池可以直接获得点数。")
                    .line("能源数据支架安装在发电设备上；完成满足功率门槛的配方时，按输出功率等级的平方关系生成能源研究点数。"))
            .task(Task.checkmark().title("能源研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_635018E8 = CHAPTER.quest("q_635018e8", q -> q
            .at(2, 2.5)
            .title("组装")
            .desc(d -> d
                    .line("{组装研究点数}", GOLD.bold())
                    .line("{每点占用：}128 B。", YELLOW)
                    .line("扫描能由组装机、装配线、电路组装机或太空组装模块产出的物品，通常会额外得到4点组装研究点数。")
                    .line("数据棒、数据球、数据模块和后期数据介质也会提供组装研究点数。"))
            .task(Task.checkmark().title("组装研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_4E12983A = CHAPTER.quest("q_4e12983a", q -> q
            .at(5.5, 0.5)
            .title("精灵")
            .desc(d -> d
                    .line("{精灵研究点数}", GOLD.bold())
                    .line("{每点占用：}256 B。", YELLOW)
                    .line("扫描带有魔法材料标记的物品或流体会得到8点精灵研究点数，同时仍会得到该材料对应的材料研究点数。")
                    .line("这是魔法线与科技树衔接时使用的研究点数标签。"))
            .task(Task.checkmark().title("精灵研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_02963400 = CHAPTER.quest("q_02963400", q -> q
            .at(6, 2.5)
            .title("催化")
            .desc(d -> d
                    .line("{催化研究点数}", GOLD.bold())
                    .line("{每点占用：}256 B。", YELLOW)
                    .line("扫描带有催化剂生成标记的材料会得到3点催化研究点数。")
                    .line("催化数据支架安装在化工厂等设备上；含催化剂的配方完成后，依据总效率、并行数和原始配方等级生成点数。"))
            .task(Task.checkmark().title("催化研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_2C77D899 = CHAPTER.quest("q_2c77d899", q -> q
            .at(4.5, 4)
            .title("星际工程")
            .desc(d -> d
                    .line("{星际工程研究点数}", GOLD.bold())
                    .line("{每点占用：}512 B。", YELLOW)
                    .line("扫描各级火箭、太空电梯与电力模块可直接取得点数；能独立在太空工作的多方块主机也会提供3点。")
                    .line("首次解锁一颗星球时，还会按2^星球等级直接加入团队总量；太空电梯工程信息模块可继续生产。"))
            .task(Task.checkmark().title("星际工程研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_1CD85CAE = CHAPTER.quest("q_1cd85cae", q -> q
            .at(3.5, 4)
            .title("机械")
            .desc(d -> d
                    .line("{机械研究点数}", GOLD.bold())
                    .line("{每点占用：}1024 B。", YELLOW)
                    .line("扫描一个多方块主机物品会得到1点机械研究点数；扫描指南针也会得到1点。")
                    .line("机械标签主要用于机器、装配设备与巨构类科技节点。"))
            .task(Task.checkmark().title("机械研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_497561B8 = CHAPTER.quest("q_497561b8", q -> q
            .at(7, 4.5)
            .title("粒子")
            .desc(d -> d
                    .line("{粒子研究点数}", GOLD.bold())
                    .line("{每点占用：}4096 B。", YELLOW)
                    .line("该标签为粒子物理研究预留。当前扫描表与研究配方没有稳定的通用生产来源，只有实际科技节点列出需求时才需要准备。"))
            .task(Task.checkmark().title("粒子研究点数"))
            .dependsOn(TechtreeChapter.Q_2E2EA0A8));

    public static final Quest Q_0163AF39 = CHAPTER.quest("q_0163af39", q -> q
            .at(2.5, 3.5)
            .title("生物")
            .desc(d -> d
                    .line("{生物研究点数}", GOLD.bold())
                    .line("{每点占用：}4096 B。", YELLOW)
                    .line("扫描干细胞、生物细胞、菌种粉和心肌细胞簇可获得生物研究点数。")
                    .line("生物研究太空舱运行专用配方时还能大量生成此类点数，并写入其数据晶片。"))
            .task(Task.checkmark().title("生物研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_67E6EF7B = CHAPTER.quest("q_67e6ef7b", q -> q
            .at(6, 1.5)
            .title("光学")
            .desc(d -> d
                    .line("{光学研究点数}", GOLD.bold())
                    .line("{每点占用：}65536 B。", YELLOW)
                    .line("扫描激光二极管、非线性光学透镜、光学晶圆与光学电路板等器件可获得少量点数。")
                    .line("光束引导计算测试配方满足指定波长、强度和偏振条件后，也会生成光学研究点数。"))
            .task(Task.checkmark().title("光学研究点数"))
            .hiddenDependsOn(TechtreeChapter.COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest Q_4B48A95D = CHAPTER.quest("q_4b48a95d", q -> q
            .at(9, 2.5)
            .title("量子")
            .desc(d -> d
                    .line("{量子研究点数}", GOLD.bold())
                    .line("{每点占用：}2097152 B。", YELLOW)
                    .line("该标签面向 UEV 及以后的量子研究。当前没有稳定的通用生产来源，准备高等级数据晶片并以节点详情为准。"))
            .task(Task.checkmark().title("量子研究点数"))
            .dependsOn(TechtreeChapter.Q_2E2EA0A8));

    public static final Quest Q_692F56B2 = CHAPTER.quest("q_692f56b2", q -> q
            .at(8, 4.5)
            .title("奇异")
            .desc(d -> d
                    .line("{奇异研究点数}", GOLD.bold())
                    .line("{每点占用：}67108864 B。", YELLOW)
                    .line("扫描里德堡旋量组件会得到2点；龙心肌细胞簇、改造龙心与觉醒龙心会在生物研究点数之外额外提供3点。")
                    .line("这是 UIV 阶段附近的高密度研究点数，低级晶片很容易因容量不足而无法扫描。"))
            .task(Task.checkmark().title("奇异研究点数"))
            .dependsOn(TechtreeChapter.Q_2E2EA0A8));

    public static final Quest Q_6F4A78AC = CHAPTER.quest("q_6f4a78ac", q -> q
            .at(9, 3.5)
            .title("超因果")
            .desc(d -> d
                    .line("{超因果研究点数}", GOLD.bold())
                    .line("{每点占用：}2147483648 B。", YELLOW)
                    .line("扫描闭合类时曲线制导单元或小宇宙等后期数据介质可以获得少量超因果研究点数。")
                    .line("这是当前字节密度最高的标签，必须使用高等级晶片并预留足够容量。"))
            .task(Task.checkmark().title("超因果研究点数"))
            .dependsOn(TechtreeChapter.Q_2E2EA0A8));

    public static final Quest Q_094AF354 = CHAPTER.quest("q_094af354", q -> q
            .at(1, -2.5)
            .icon(GTItems.FIELD_GENERATOR_LuV)
            .desc(d -> d
                    .line("科技节点的解锁条件由三部分组成：前置节点、研究点数和 CWU 算力。")
                    .line()
                    .line("节点解锁后会把它关联的配方数据或机器效果加入团队可用内容；同一节点不会在队伍中重复解锁。")
                    .line()
                    .line("前期的研究节点解锁起来相对简单，直接在扫描仪内可通过扫描特定物品获取100%的尤里卡来解锁。")
                    .line()
                    .line("{在解锁研究站之后，尤里卡就不足以完成100%的进度，需要你投入算力（CWU）来完成剩下的部分。}", AQUA)
                    .line()
                    .line("部分研究还需要研究点数，你需要使用基元扫描站将晶片中的研究点数转化为团队共有的点数。")
                    .line()
                    .line("推荐解锁的第一个节点为：{装配线基础部件}。将一枚数据晶片与IV力场发生器放入扫描仪，即可触发尤里卡并完成此研究节点。", link(TechnodeChapter.COMPONENT_IN_ASSEMBLY_LINE)))
            .task(Task.checkmark().title("解锁第一个科技节点"))
            .dependsOn(IV_SCANNER_2));

    public static final Quest Q_0A227168 = CHAPTER.quest("q_0a227168", q -> q
            .at(2, -1)
            .desc(d -> d
                    .line("扫描材料或使用专用机器都可以获得对应领域的研究点数标签。")
                    .line("当前主要来源：")
                    .line("精灵 - 魔法材料扫描；")
                    .line("组装 - 可由组装机、太空组装或装配线处理的材料；")
                    .line("计算 - 扫描或计算数据支架；")
                    .line("生物 - 生物研究太空舱；")
                    .line("催化 - 催化材料或催化数据支架。")
                    .line("能源 - 能源数据支架；")
                    .line("光学 - 激光计算测试平台。")
                    .line("热力学 - 热力学分析平台；")
                    .line("星际工程 - 解锁星球的数据，也可由太空电梯工程信息模块提供；")
                    .line("数据存储 - 数据形态测试厂；"))
            .task(Task.checkmark().title("认识各领域的研究点数来源"))
            .dependsOn(Q_094AF354));

    public static final Quest DATA_EXPORT_MACHINE = CHAPTER.quest("data_export_machine", q -> q
            .at(0, -1)
            .title("导出科技配方")
            .desc(d -> d
                    .line("数据导出机可以把已解锁科技节点的配方数据复制到闪存等承载介质。")
                    .line()
                    .line("在界面选择配方奖励，再放入对应等级的介质并确认导出；导出过程固定消耗7680 EU/t并需要一段时间。")
                    .line()
                    .line("数据中心集成了配方导出机的功能。"))
            .task(Task.item(GTOMachines.DATA_EXPORT_MACHINE))
            .dependsOn(Q_094AF354));

    public static final Quest COMMAND_BLOCK = CHAPTER.quest("command_block", q -> q
            .at(-6, 3)
            .title("管理与调试命令")
            .desc(d -> d
                    .line("科技树管理界面可使用 /gtocore techtree ui 打开。玩家可以切换科技树、预览节点并查看解锁状态。")
                    .line("管理员命令 /gtocore techtree unlock <玩家> <科技树ID> <节点ID> 会强制解锁节点及其依赖；/gtocore techtree reset <玩家> <科技树ID> 只重置指定科技树。")
                    .line("/gtocore researchtag add/remove 可调整队伍研究点数；/gtocore scan add_hand、remove_hand、get_self 用于维护或查看扫描记录。"))
            .task(Task.item(Items.COMMAND_BLOCK)));

    public static final Quest INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM = CHAPTER.quest("intelligent_scanning_management_platform", q -> q
            .at(-4.5, -0.5)
            .title("智能扫描管理")
            .desc(d -> d
                    .line("智能扫描管理平台允许自动化扫描。")
                    .line("平台每次工作消耗一张晶片；物品目标消耗1个物品，流体目标消耗1000 mB流体。")
                    .line("支持四种模式：选定物品循环、选定物品单次、未学习物品循环、未学习物品单次。")
                    .page()
                    .line("智能扫描 ME 代理连接 ME 网络，为平台提供库存中的物品与流体。")
                    .line("数据晶体不会加入扫描目标；带标签的对象按无标签的基础类型处理；流体只有库存超过1000 mB时才会进入列表。"))
            .task(Task.item(ExResearchMachines.INTELLIGENT_SCANNING_MANAGEMENT_PLATFORM))
            .dependsOn(IV_SCANNER_2));

    public static final Quest COMPUTATIONAL_DATA_HOLDER = CHAPTER.quest("computational_data_holder", q -> q
            .at(4.5, -1)
            .title("计算研究点数")
            .desc(d -> d
                    .line("计算研究点数可以由计算数据支架生产。支架安装在计算机或超算中心等能输出算力流动数据的设备上。")
                    .line("每5秒生成 √(最大算力 + 当前使用算力) 的计算研究点数，并把结果存入机器内的数据晶片。"))
            .task(Task.item(ExResearchMachines.COMPUTATIONAL_DATA_HOLDER))
            .dependsOn(Q_0A227168));

    public static final Quest COMPUTATIONAL_DATA_HOLDER_2 = CHAPTER.quest("computational_data_holder_2", q -> q
            .at(4, 2)
            .nodeScale(3)
            .title("各种研究点数")
            .desc(d -> d
                    .line("催化数据支架安装在化工厂或激光化工厂上；")
                    .line("能量数据支架安装在发电设备上；")
                    .line("计算数据支架安装在计算设备上；")
                    .line("存储、热力学、光学支架安装在对应的测试平台上；")
                    .line("生物学数据支架安装在生物研究太空舱上；")
                    .line("星际工程数据支架安装在太空电梯工程信息模块上；"))
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
            .at(7, 1.5)
            .desc(d -> d
                    .line("光束系统把光束作为独立的机器输入：光束具有波长、强度和偏振等属性，配方可以要求其中的一个或多个范围。")
                    .line("光束发生器、偏振器、反射/转向部件和光束接入部件共同组成光路；光路变化时，Beam 管理器会重新计算路径并同步给客户端。")
                    .line("需要光学研究点数的配方会在 EMI 中显示所需光束的波长、强度和偏振条件。"))
            .task(Task.checkmark().title("理解 Beam 光束机制"))
            .dependsOn(Q_67E6EF7B));

    public static final Quest Q_2E2EA0A8 = CHAPTER.quest("q_2e2ea0a8", q -> q
            .at(7, 2.5)
            .subtitle("AI语真难改")
            .desc(d -> d
                    .line("基础标签之外的高阶标签会随着流程逐步开放。粒子、量子、奇异和超因果是后期内容的预留或高阶研究方向，当前不一定都有独立的生产设备。")
                    .line("当某个标签的来源加入游戏后，扫描或对应研究设备会把它写入晶片；节点详情会明确列出实际需要的标签。")
                    .line("{简单来说就是没做完}", RED))
            .task(Task.checkmark().title("高阶研究标签"))
            .hiddenDependsOn(COMPUTATIONAL_DATA_HOLDER_2));

    public static final Quest DATA_BASE = CHAPTER.quest("data_base", q -> q
            .at(-4.5, -4.5)
            .title("数据基础")
            .desc(d -> d
                    .line("科技节点会解锁配方，你还需要{数据导出机}导出配方到闪存，再放入{数据访问仓}才能使用。", RED, RED))
            .task(Task.techNode(BaseNodes.DataBase))
            .dependsOn(IV_SCANNER_2));

    public static final Quest COMPUTATION_ARRAY = CHAPTER.quest("computation_array", q -> q
            .at(3.5, -5.5)
            .title("计算阵列")
            .desc(d -> d.line("计算阵列节点代表研究算力的基础设施。解锁后可以把更高等级的计算数据接入数据中心，为后续节点提供稳定的计算研究点数与算力供应。"))
            .task(Task.techNode(BaseNodes.ComputationArray)));

    public static final Quest SCAN_STATION = CHAPTER.quest("scan_station", q -> q
            .at(2, -4)
            .title("基元扫描站")
            .desc(d -> d
                    .line("{基元扫描站把晶片解析成团队研究点数}，是数据中心使用研究点数的前置。", AQUA)
                    .line("{晶片扫描的耗时耗能固定}，与存储多少点数无关。", RED))
            .task(Task.techNode(BaseNodes.ScanStation))
            .dependsOn(Q_094AF354));

    public static final Quest DATAUNIT_SCANNING_STATION = CHAPTER.quest("dataunit_scanning_station", q -> q
            .at(4.5, -4)
            .title("解析数据晶片")
            .desc(d -> d
                    .line("基元扫描站{把晶片解析成团队研究点数}，是数据中心使用研究点数的前置。", RED))
            .task(Task.item(ExResearchMachines.PRIMORDIAL_SCANNING_STATION))
            .dependsOn(COMPUTATION_ARRAY, SCAN_STATION));

    public static final Quest DATA_IN_RESEARCH = CHAPTER.quest("data_in_research", q -> q
            .at(5.5, -2.5)
            .title("数据中心研究")
            .desc(d -> d
                    .line("{数据中心}同时有{数据库}、{数据导出机}的功能，且可以接入计算设备{研究科技节点}。", YELLOW, YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("研究状态会消耗电力并需要{多氯联苯冷却剂}。", AQUA)
                    .line()
                    .line("可接受的算力上限取决于玻璃等级，以及数据访问仓和其中配方数据的数量。")
                    .line()
                    .line("在数据中心界面选择科技节点并启动研究，它会持续消耗算力累积CWU进度，直到解锁。")
                    .line()
                    .line("{注意：}若此时前置节点或研究点数不足，节点不会解锁，但数据中心仍会停止本次研究；补足条件后需要重新选择节点并启动。", RED))
            .task(Task.techNode(BaseNodes.DataCenter))
            .dependsOn(Q_094AF354, DATAUNIT_SCANNING_STATION, COMPUTATIONAL_DATA_HOLDER)
            .mainNode());

    public static final Quest Q_2CB2DFC2 = CHAPTER.quest("q_2cb2dfc2", q -> q
            .at(9, -2.5)
            .desc(d -> d
                    .line("数据中心超频允许研究过程使用超过当前单次输入上限的总算力。")
                    .line()
                    .line("解锁后，当数据中心为节点处理的算力达到最大可接受算力的3倍时，研究速度提升至2倍；更高总算力会继续按超频逻辑增加超频等级。")
                    .line()
                    .line("研究仍然需要满足节点的前置、研究点数标签和尤里卡条件；超频只改变算力推进速度。"))
            .task(Task.checkmark().title("解锁数据中心超频"))
            .dependsOn(DATA_IN_RESEARCH));

    public static final Quest Q_0F66B0E1 = CHAPTER.quest("q_0f66b0e1", q -> q
            .at(-7.5, -5.5)
            .title("测试声明")
            .desc(d -> d.line("此章节仍在测试阶段，其呈现效果不代表最终成品章节结构。"))
            .task(Task.checkmark()));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -7.8, -2.9, 2, 2))
                .image(QuestImage.of(QuestTextures.SPACETIME_ASSEMBLY_LINE_CASING, -7, 0, 3.39, 3.39).hover("打开科技树").clickCommand("/gtocore techtree ui")));
    }
}
