package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.machines.ExResearchMachines;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import static com.gtolib.api.quest.QuestStyle.*;

public final class SopDatabaseChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("sop_database", c -> c
            .title("SOP 数据库")
            .subtitle("SOP Database")
            .icon(ExResearchMachines.BIO_DATA_ACCESS_HATCH)
            .alwaysInvisible());

    public static final Quest Q_4C6CF025 = CHAPTER.quest("q_4c6cf025", q -> q
            .at(-1, -1.5)
            .desc(d -> d
                    .line("// 来自格雷科技有限公司 中央技术委员会")
                    .line()
                    .line("主题：关于《工业核心协议》数据库的访问说明")
                    .line()
                    .line("新员工，欢迎。")
                    .line()
                    .line("本数据库收录了公司所有生产、研发活动的核心操作协议。这些协议定义了设备交互、能量传输与物料处理的基础规则，是确保效率、安全与合规性的唯一标准。")
                    .line()
                    .line("请务必在相应设备解锁后，及时查阅并遵循相关协议。违反协议可能导致设备损毁、生产事故及绩效扣分。")
                    .line()
                    .line("—— 遵守协议，高效生产。")
                    .line()
                    .line("ver2")
                    .line()
                    .line("“欢迎加入格雷科技有限公司，新员工。你的个人终端已激活，并接入中央SOP数据库。所有生产流程、设备操作与安全规范均需严格遵循相关SOP。违规操作导致的设备损毁及人员伤亡，公司将不予承担。”"))
            .task(Task.checkmark()));

    public static final Quest Q_53501924 = CHAPTER.quest("q_53501924", q -> q
            .at(-4, -2)
            .desc(d -> d
                    .line("[SOP更新]《导线安培容量与热损耗管理》已发布至中央SOP数据库\"")
                    .line()
                    .line()
                    .line("新规程《导线安培容量与热损耗管理》已发布。")
                    .line()
                    .line("规程更新，已同步至中央SOP数据库")
                    .line()
                    .line("已解锁规程：[SOP-ET-101] 低压线路敷设与热损耗管理")
                    .line()
                    .line()
                    .line("[通知] 新规程已同步至中央SOP数据库。")
                    .line()
                    .line("相关规程已更新，请查阅《标准作业程序》。")
                    .line()
                    .line("[SOP同步] 请参阅最新规程：《导线安培容量与热损耗管理》")
                    .line()
                    .line()
                    .line("提示：相关操作规范参见《导线安培容量规范》")
                    .line()
                    .line("[文件分发] 你收到了新的公司文件：《SOP-MF-104：生产设施效率提升的授权方案》")
                    .line()
                    .line()
                    .line()
                    .line("{本任务将在}{提示}{章节解锁新提示}", YELLOW, YELLOW.bold(), YELLOW)
                    .line()
                    .line("{前往}{升压机制}{以获取更多知识！}", YELLOW, link(TipsChapter.BOOST_MECHANISM), YELLOW))
            .task(Task.checkmark()));

    public static final Quest Q_2CF44FCC = CHAPTER.quest("q_2cf44fcc", q -> q
            .at(-2, -3.5)
            .desc(d -> d
                    .line()
                    .line("部门代码全称 (部门)负责内容示例")
                    .line("EL Electrical (电力部)一切与发电、输电、配电、用电相关SOP-EL-001：低压线路敷设与安全规范")
                    .line("MF Manufacturing (制造部)单方块机器操作、覆盖板、加速、并行SOP-MF-101：超频插槽使用与能耗管理")
                    .line("MB Multiblock (多方块结构部)多方块机器组装、朝向、升级、维护SOP-MB-201：大型离心机装配与校准流程")
                    .line("CH Chemical (化工部)化学反应、流体处理、管道、催化剂SOP-CH-301：高爆性化学品安全处理规程")
                    .line("RD Research & Development (研发部)扫描仪、研究台、数据管理、解密SOP-RD-401：对未知物品的逆向工程标准流程")
                    .line("LV Logistics & Vehicle (物流与交通部)物品传输、管道、机器人、交通工具SOP-LV-501：物品管道网络流量控制指南")
                    .line("EN Energy Core (能源核心部)聚变、能源仓、无线能量传输、激光SOP-EN-601：无线激光收发器对准与调试手册")
                    .line("SA Safety & Compliance (安全与合规部)所有安全警告、事故案例、合规条款SOP-SA-998：因违规超频导致主电力室熔毁的事故报告")
                    .line()
                    .line()
                    .line()
                    .line("协议 ETP-001：导线安培规范")
                    .line()
                    .line("协议 MVP-002：机器电压参数解读")
                    .line()
                    .line("协议 MSP-003：多方块结构装配准则")
                    .line()
                    .line("协议 WPT-004：无线能量传输标准")
                    .line()
                    .line("协议 OPP-005：生产效率优化手册")
                    .line()
                    .line("协议 RDP-101：研究流程标准协议"))
            .task(Task.checkmark()));

    public static final Quest Q_3574E679 = CHAPTER.quest("q_3574e679", q -> q
            .at(-5, -5)
            .desc(d -> d
                    .line("个人知识库终端")
                    .line()
                    .line("个人知识档案 (P.K.A.)")
                    .line()
                    .line("格雷工程典章")
                    .line()
                    .line("工业设施规范")
                    .line()
                    .line("格雷生产总纲")
                    .line()
                    .line("工业核心规程")
                    .line()
                    .line("生产规程总览")
                    .line()
                    .line("技术规范集成")
                    .line()
                    .line("格雷工程法典")
                    .line()
                    .line("SOP"))
            .task(Task.checkmark()));

    public static final Quest Q_01288345 = CHAPTER.quest("q_01288345", q -> q
            .at(-7, -3)
            .desc(d -> d
                    .line("规划：将属于机制的部分从提示搬迁过来")
                    .line()
                    .line("同时在解锁对应机制的任务增加一个提示和超链接跳转"))
            .task(Task.checkmark().title("筹备中")));
}
