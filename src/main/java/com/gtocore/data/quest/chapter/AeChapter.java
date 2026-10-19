package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOAEParts;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.OptionalMachine;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;
import com.gtocore.integration.ae.TieredInterfaces;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;

import appbot.forge.ABItems;
import com.almostreliable.merequester.Registration;
import com.glodblock.github.ae2netanalyser.common.AEAItems;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.hepdd.gtmthings.data.CustomMachines;
import com.ultramega.ae2insertexportcard.registry.ModItems;
import de.mari_023.ae2wtlib.AE2wtlib;
import gripe._90.arseng.definition.ArsEngItems;
import io.github.lapis256.ae2_toggleable_view_cell.AE2ToggleableViewCell;

import static com.gtolib.api.quest.QuestStyle.*;

public final class AeChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("ae", c -> c
            .title("应用能源")
            .subtitle("Applied Energistics 2")
            .icon(AEBlocks.INTERFACE)
            .hideQuestsUntilDepsVisible()
            .autofocus(AeChapter.LOGIC_PROCESSOR));

    public static final Quest GETTING_STARTED_WITH_AE = CHAPTER.quest("getting_started_with_ae", q -> q
            .at(-9, 1.5)
            .icon(AEBlocks.ENERGY_ACCEPTOR)
            .title("应用能源起步")
            .subtitle("没有频道的AE真好")
            .desc(d -> d
                    .line("你终于可以制作{应用能源(AE2)}的敲门砖——{能源接收器}了！", link(AeChapter.GETTING_STARTED_WITH_AE), GOLD)
                    .line()
                    .line("{不过先别太激动}{——可惜的是，在LV时代你能做的AE物品其实就只有这玩意儿（还有其他几个小玩意）……}", AQUA.italic(), GRAY.italic())
                    .line()
                    .line("{想要真正在AE上大展拳脚？还是得等到MV才行。}", GRAY.italic())
                    .line()
                    .line("{AE机器和ME网络都需要AE能量才能工作，使用}{能源接收器}将GT的电能转化为AE能量", GRAY.italic(), GOLD.italic())
                    .line()
                    .line("GTO的AE默认{无限频道}，无需{ME控制器}，只需要确保为当前网络提供足够的能量即可", GREEN, GOLD)
                    .line()
                    .line("{如果你还不会AE，请尽快寻找教程快速入门。本包无需精通AE，但至少需要了解如何下单。}", GRAY.italic()))
            .task(Task.item(AEBlocks.ENERGY_ACCEPTOR))
            .reward(Reward.item(AEItems.NETWORK_TOOL).personal())
            .dependsOn(SteamChapter.BACK_TO_THE_PAST, LvChapter.DIODE, LvChapter.LIQUEFACTION_FURNACE));

    public static final Quest ALIEN_TECHNOLOGY = CHAPTER.quest("alien_technology", q -> q
            .at(-13, 3.5)
            .icon(AEBlocks.MYSTERIOUS_CUBE)
            .title("外星科技")
            .subtitle("小小的压印模板铸造宏大的自动化网络")
            .desc(d -> d
                    .line("在外探索看到一个巨坑，挖掉外层的陨石（不要丢），挖掉神秘方块即可获得")
                    .line()
                    .line("如果你不知道该怎么寻找，不妨试试{指南针}", link(TipsChapter.EXPLORERSCOMPASS)))
            .task(
                    Task.item(AEItems.CALCULATION_PROCESSOR_PRESS),
                    Task.item(AEItems.ENGINEERING_PROCESSOR_PRESS),
                    Task.item(AEItems.LOGIC_PROCESSOR_PRESS),
                    Task.item(AEItems.SILICON_PRESS)));

    public static final Quest CERTUS_QUARTZ_CRYSTAL = CHAPTER.quest("certus_quartz_crystal", q -> q
            .at(-13, 9.5)
            .desc(d -> d
                    .line("{赛特斯石英水晶}有多种来源，最简便的方法就是直接熔炼{赛特斯石英矿}，不过这会损失一些副产物", GOLD, GOLD)
                    .line()
                    .line("你也可以选择其他方法：")
                    .line()
                    .line("· {世界交互}", GOLD)
                    .line("· {筛选纯净赛特斯石英矿}", GOLD)
                    .line("· {高压釜}", GOLD)
                    .line("· {聚爆}", GOLD)
                    .line("· {村民交易}", GOLD)
                    .line("· {物质重组}", GOLD))
            .task(Task.item(AEItems.CERTUS_QUARTZ_CRYSTAL)));

    public static final Quest INSCRIBER_NORMAL_EXPERT = CHAPTER.quest("inscriber_normal_expert", q -> q
            .at(-13, 5.5)
            .title("压印器（{普通} & {专家}）", YELLOW, LIGHT_PURPLE)
            .desc(d -> d.line("这个机器不能压印模板，你需要使用激光蚀刻机"))
            .task(Task.item(AEBlocks.INSCRIBER))
            .dependsOn(UtilChapter.NORMAL_EXPERT)
            .optional().hideUntilDepsVisible());

    public static final Quest VIBRATION_CHAMBER = CHAPTER.quest("vibration_chamber", q -> q
            .at(-11, 1.5)
            .desc(d -> d.line("火力发电机，只不过发出的是AE能量，你基本用不到它——毕竟你都有能源接收器了！"))
            .task(Task.item(AEBlocks.VIBRATION_CHAMBER))
            .dependsOn(GETTING_STARTED_WITH_AE)
            .optional());

    public static final Quest CERTUS_QUARTZ_DUST = CHAPTER.quest("certus_quartz_dust", q -> q
            .at(-11, 9.5)
            .desc(d -> d
                    .line("{赛特斯石英粉}同样有很多来源，你可以选择不处理石英矿，直接将{粗赛特斯石英}投入{熔炉}并{磨粉}是不错的选择", GOLD, YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("当然，如果你搭建了一套矿物处理产线，就可以从远古世界挖掘到的{赛特斯石英矿}/{石英岩矿}处理出大量{赛特斯石英粉}", YELLOW, YELLOW, GOLD))
            .task(Task.item(AEItems.CERTUS_QUARTZ_DUST))
            .dependsOn(CERTUS_QUARTZ_CRYSTAL));

    public static final Quest QUARTZ_FIBER = CHAPTER.quest("quartz_fiber", q -> q
            .at(-11, 11.5)
            .desc(d -> d.line("它传输AE能量，但不传输AE频道，常用于隔开两个网络"))
            .task(Task.item(AEParts.QUARTZ_FIBER))
            .dependsOn(CERTUS_QUARTZ_DUST));

    public static final Quest CHARGED_CERTUS_QUARTZ_CRYSTAL = CHAPTER.quest("charged_certus_quartz_crystal", q -> q
            .at(-13, 11.5)
            .task(Task.item(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
            .dependsOn(CERTUS_QUARTZ_CRYSTAL));

    public static final Quest FLUIX_CRYSTAL = CHAPTER.quest("fluix_crystal", q -> q
            .at(-15, 11.5)
            .desc(d -> d
                    .line("你会需要很多福鲁伊克斯水晶来搭建你的ME网络，和赛特斯水晶（粉）类似，同样有多种来源：")
                    .line()
                    .line("· {世界交互}，将{下界石英}、{红石粉}、{充能赛特斯石英水晶}丢进水中。纯种的AE人喜欢用纯AE实现自动化！", GOLD, YELLOW, YELLOW, YELLOW)
                    .line("· {电弧发生器}，{一步产出}，强力推荐", GOLD, YELLOW)
                    .line("· {村民交易}，由于有{简易村民}模组的存在，你也可以用{自动村民交易}的方式获取，但单方块产能存在限制，只适合少量获取", GOLD, YELLOW, YELLOW)
                    .line("· {聚爆}，搅一搅，爆一爆！谁让你无意中处理出了太多无用的{炸药}和{粉末}呢！", GOLD, YELLOW, YELLOW))
            .task(Task.item(AEItems.FLUIX_CRYSTAL))
            .dependsOn(CHARGED_CERTUS_QUARTZ_CRYSTAL));

    public static final Quest FLUIX_GLASS_CABLE = CHAPTER.quest("fluix_glass_cable", q -> q
            .at(-11, 13.5)
            .desc(d -> d.line("ME网络使用的线缆，如果你不是专家模式，只使用这种线缆就足够了"))
            .task(Task.item(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT)))
            .dependsOn(QUARTZ_FIBER, FLUIX_CRYSTAL));

    public static final Quest DRIVE = CHAPTER.quest("drive", q -> q
            .at(-9, 11.5)
            .icon(AEBlocks.DRIVE)
            .subtitle("你的网络存储~扩容区")
            .desc(d -> d
                    .line("放入{存储元件}，ME网络几乎可以存储任何东西", GOLD)
                    .line()
                    .line("{避免将大量带有NBT的物品存入网络}，虽然存储元件没有种类限制了，但依然会造成不可避免的{性能延迟}", YELLOW, DARK_RED))
            .task(Task.item(AEBlocks.DRIVE))
            .dependsOn(FLUIX_GLASS_CABLE, MvChapter.VANADIUM_STEEL_INGOT, UtilChapter.Q_056EC4E6)
            .requireAny()
            .requireCount(2));

    public static final Quest ME_P2P_TUNNEL = CHAPTER.quest("me_p2p_tunnel", q -> q
            .at(-15, 15.5)
            .desc(d -> d
                    .line("{P2P通道}是一种模拟面对面传输的面板，可以通过{P2P通道调谐}来传输不同种类的信号，并且需要进行输入输出端的绑定才能正常使用", GOLD, YELLOW)
                    .line()
                    .line("手持{内存卡}{潜行+右键}点击一个P2P通道可将其设置为{输入端}并生成一个频道，同时内存卡也会存储这个频道", GOLD, YELLOW, AQUA)
                    .line()
                    .line("然后，用{同一张}内存卡右键点击其他P2P通道，即可将其他P2P通道设置为{输出端}并绑定进同一个频道中", YELLOW, AQUA)
                    .line()
                    .line("{本包不兼容样板P2P通道。作为替代，你可以使用}{超立方体发生器}{来模拟样板P2P通道的行为}", GRAY.italic(), link(AeChapter.P2P_SAMPLE), GRAY.italic()))
            .task(Task.item(AEParts.ME_P2P_TUNNEL))
            .dependsOn(FLUIX_CRYSTAL));

    public static final Quest IO_PORT = CHAPTER.quest("io_port", q -> q
            .at(-5, 13.5)
            .desc(d -> d.line("可以快速将元件的内容导入ME网络，或将ME网络的内容导入元件"))
            .task(Task.item(AEBlocks.IO_PORT))
            .dependsOn(DRIVE, FLUIX_GLASS_CABLE));

    public static final Quest EX_DRIVE = CHAPTER.quest("ex_drive", q -> q
            .at(-7, 11.5)
            .task(Task.item(EPPItemAndBlock.EX_DRIVE))
            .dependsOn(DRIVE));

    public static final Quest ME_STORAGE = CHAPTER.quest("me_storage", q -> q
            .at(-5, 11.5)
            .icon(MultiBlockG.ME_STORAGE)
            .subtitle("格雷科技™信息园数据中心")
            .desc(d -> d
                    .line("{ME驱动器}的多方块版本，格雷公司™主脑的唯一数据中心，由于你晋升到了{LuV}阶段，公司为你开放了制作这台{终}{极}{存}{储}{器}的权限以满足你的所需", GOLD, LUV, DARK_RED, BLUE, YELLOW, GREEN, LIGHT_PURPLE)
                    .line()
                    .line("不同级别的{存储核心}能存储的数据量也不同。{存储核心}可以{混搭}安装", GOLD, GOLD, YELLOW)
                    .line()
                    .line("通过{ME存储访问仓}连接ME网络", GOLD)
                    .line()
                    .line("存储器UI中的索引调整为{[机器]}时，存储空间与{存储器主机}绑定，拆除主机后会发现其上带着标记存储空间的nbt；", YELLOW, GOLD)
                    .line()
                    .line("存储器UI中的索引调整为{[玩家]}时，存储空间与{玩家}绑定，且由同一玩家放下的存储器会共享存储空间。", YELLOW, GOLD)
                    .line()
                    .line("{记得供电}", GRAY.strike()))
            .task(
                    Task.item(MultiBlockG.ME_STORAGE),
                    Task.item(GTAEMachines.ME_STORAGE_ACCESS_HATCH),
                    Task.item(GTOItems.STORAGE_ACCESSOR_REPLACER1).optional())
            .dependsOn(EX_DRIVE, IvChapter.IV_LAUNCHER_AND_IV_SENSOR, LuvChapter.EUROPIUM_INGOT)
            .hideDependentLines());

    public static final Quest FLUIX_COVERED_CABLE = CHAPTER.quest("fluix_covered_cable", q -> q
            .at(-11, 15.5)
            .task(Task.item(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT)))
            .dependsOn(FLUIX_GLASS_CABLE));

    public static final Quest FLUIX_SMART_CABLE = CHAPTER.quest("fluix_smart_cable", q -> q
            .at(-11, 17.5)
            .task(Task.item(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT)))
            .dependsOn(FLUIX_COVERED_CABLE)
            .hideDependentLines());

    public static final Quest FLUIX_COVERED_DENSE_CABLE = CHAPTER.quest("fluix_covered_dense_cable", q -> q
            .at(-13, 15.5)
            .task(Task.item(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT)))
            .dependsOn(FLUIX_COVERED_CABLE));

    public static final Quest FLUIX_DUST = CHAPTER.quest("fluix_dust", q -> q
            .at(-15, 9.5)
            .task(Task.item(AEItems.FLUIX_DUST))
            .dependsOn(FLUIX_CRYSTAL));

    public static final Quest TRANSPORTABLE_AE = CHAPTER.quest("transportable_ae", q -> q
            .at(-13, 1.5)
            .title("可以搬运的AE")
            .desc(d -> d
                    .line("可以用来搬运ME接口和ME样板供应器，同时保存内部配置和样板")
                    .line()
                    .line("{对准并Shift右击}", GRAY))
            .task(Task.item(EPPItemAndBlock.PACKING_TAPE))
            .dependsOn(FLUIX_DUST)
            .hideDependencyLines());

    public static final Quest ENERGY_CELL = CHAPTER.quest("energy_cell", q -> q
            .at(-15, 7.5)
            .task(Task.item(AEBlocks.ENERGY_CELL))
            .dependsOn(FLUIX_DUST));

    public static final Quest DENSE_ENERGY_CELL = CHAPTER.quest("dense_energy_cell", q -> q
            .at(-15, 5.5)
            .task(Task.item(AEBlocks.DENSE_ENERGY_CELL))
            .dependsOn(ENERGY_CELL));

    public static final Quest CREATIVE_ENERGY_CELL = CHAPTER.quest("creative_energy_cell", q -> q
            .at(-15, 3.5)
            .task(Task.item(AEBlocks.CREATIVE_ENERGY_CELL))
            .dependsOn(DENSE_ENERGY_CELL));

    public static final Quest ITEM_CELL_HOUSING = CHAPTER.quest("item_cell_housing", q -> q
            .at(1.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_HOUSING))
            .dependsOn(DRIVE)
            .hideDependencyLines());

    public static final Quest SOURCE_CELL_HOUSING = CHAPTER.quest("source_cell_housing", q -> q
            .at(1.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_HOUSING))
            .dependsOn(DRIVE)
            .hideDependencyLines());

    public static final Quest FLUID_CELL_HOUSING = CHAPTER.quest("fluid_cell_housing", q -> q
            .at(1.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_HOUSING))
            .dependsOn(DRIVE)
            .hideDependencyLines());

    public static final Quest MANA_CELL_HOUSING = CHAPTER.quest("mana_cell_housing", q -> q
            .at(1.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_HOUSING.get()))
            .dependsOn(DRIVE)
            .hideDependencyLines());

    public static final Quest THE_SECOND_PIECE_OF_THE_AUTO = CHAPTER.quest("the_second_piece_of_the_auto", q -> q
            .at(1.5, 10.5)
            .icon(AEBlocks.CRAFTING_UNIT)
            .title("自动合成系统的第二块拼图")
            .desc(d -> d
                    .line("有了它你才可以开始自动合成")
                    .line()
                    .line("现在合成存储器与并行单元的底层代码已经被大幅度修改，性能{极为强悍}，放心使用即可", DARK_RED))
            .task(
                    Task.item(AEBlocks.CRAFTING_UNIT),
                    Task.anyOf(1, AEBlocks.CRAFTING_STORAGE_1K, AEBlocks.CRAFTING_STORAGE_4K, AEBlocks.CRAFTING_STORAGE_16K, AEBlocks.CRAFTING_STORAGE_64K, AEBlocks.CRAFTING_STORAGE_256K).title("任意合成存储器"))
            .reward(Reward.item(AEBlocks.CRAFTING_ACCELERATOR))
            .dependsOn(FLUIX_GLASS_CABLE, HvChapter.HV_ASSEMBLER, UtilChapter.Q_056EC4E6)
            .requireAny()
            .requireCount(2)
            .hideDependencyLines());

    public static final Quest CRAFTING_ACCELERATOR = CHAPTER.quest("crafting_accelerator", q -> q
            .at(1.5, 11.5)
            .task(Task.item(AEBlocks.CRAFTING_ACCELERATOR))
            .dependsOn(THE_SECOND_PIECE_OF_THE_AUTO));

    public static final Quest FLUIX_PEARL = CHAPTER.quest("fluix_pearl", q -> q
            .at(-17, 11.5)
            .task(Task.item(AEItems.FLUIX_PEARL))
            .dependsOn(FLUIX_CRYSTAL));

    public static final Quest WIRELESS_RECEIVER = CHAPTER.quest("wireless_receiver", q -> q
            .at(-19, 11.5)
            .desc(d -> d.line("没有什么实际用处，只作为合成材料使用"))
            .task(Task.item(AEItems.WIRELESS_RECEIVER))
            .dependsOn(FLUIX_PEARL, HvChapter.MICROPROCESSOR_SUPERCOMPUTER, UtilChapter.Q_056EC4E6)
            .requireAny()
            .requireCount(2));

    public static final Quest WIRELESS_CONNECTION = CHAPTER.quest("wireless_connection", q -> q
            .at(-19, 9.5)
            .icon(EPPItemAndBlock.WIRELESS_CONNECTOR)
            .title("无线连接")
            .subtitle("Wifi到处飞")
            .desc(d -> d.line("无线传输{当前维度的}网络，只能一对一连接！", RED))
            .task(Task.item(EPPItemAndBlock.WIRELESS_CONNECTOR), Task.item(EPPItemAndBlock.WIRELESS_TOOL))
            .dependsOn(WIRELESS_RECEIVER, FLUIX_SMART_CABLE));

    public static final Quest WIRELESS_TERMINAL_ACCESS_POINT = CHAPTER.quest("wireless_terminal_access_point", q -> q
            .at(-21, 11.5)
            .icon(AEBlocks.WIRELESS_ACCESS_POINT)
            .title(" 无线终端的访问入口")
            .subtitle("终于可以随时随地访问了")
            .desc(d -> d.line("将{无线终端}塞入ME无线网络访问点的{右上角槽位}可以让无线终端远程访问ME网络，不过你应该更喜欢{量子桥卡}", GOLD, YELLOW, GOLD))
            .task(Task.item(AEBlocks.WIRELESS_ACCESS_POINT), Task.item(AEItems.WIRELESS_BOOSTER))
            .dependsOn(WIRELESS_RECEIVER));

    public static final Quest QUANTUM_RING = CHAPTER.quest("quantum_ring", q -> q
            .at(-17, 7.5)
            .icon(AEBlocks.QUANTUM_RING)
            .task(Task.item(AEBlocks.QUANTUM_RING))
            .dependsOn(ENERGY_CELL, FLUIX_SMART_CABLE));

    public static final Quest QUANTUM_LINK = CHAPTER.quest("quantum_link", q -> q
            .at(-17, 9.5)
            .task(Task.item(AEBlocks.QUANTUM_LINK))
            .dependsOn(FLUIX_PEARL));

    public static final Quest SEMI_DARK_MONITOR = CHAPTER.quest("semi_dark_monitor", q -> q
            .at(-9, 17.5)
            .subtitle("AE学——照亮世界！")
            .task(Task.item(AEParts.SEMI_DARK_MONITOR), Task.item(AEParts.MONITOR).optional(), Task.item(AEParts.DARK_MONITOR).optional())
            .dependsOn(CERTUS_QUARTZ_DUST)
            .hideDependencyLines());

    public static final Quest TERMINAL = CHAPTER.quest("terminal", q -> q
            .at(-7, 17.5)
            .subtitle("访问网络")
            .task(Task.item(AEParts.TERMINAL))
            .dependsOn(SEMI_DARK_MONITOR));

    public static final Quest CRAFTING_TERMINAL = CHAPTER.quest("crafting_terminal", q -> q
            .at(-5, 17.5)
            .task(Task.item(AEParts.CRAFTING_TERMINAL))
            .dependsOn(TERMINAL));

    public static final Quest PATTERN_ENCODING_TERMINAL = CHAPTER.quest("pattern_encoding_terminal", q -> q
            .at(-3, 17.5)
            .desc(d -> d
                    .line("用于编写样板，在样板编码终端页面中呼出EMI时，可通过配方右下角的{加号}来直接填充样板。", AQUA)
                    .line()
                    .line("{多方块}同样也可以通过加号填充样板，并且可以编写不同形态。", YELLOW)
                    .line()
                    .line("{不要傻傻的手写样板哦}", GRAY.italic()))
            .task(Task.item(AEParts.PATTERN_ENCODING_TERMINAL))
            .dependsOn(CRAFTING_TERMINAL));

    public static final Quest PATTERN_ACCESS_TERMINAL = CHAPTER.quest("pattern_access_terminal", q -> q
            .at(-7, 21.5)
            .desc(d -> d
                    .line("用于管理所有连接至ME网络的样板供应器和样板总成")
                    .line()
                    .line("建议直接使用隔壁扩展版本的"))
            .task(Task.item(AEParts.PATTERN_ACCESS_TERMINAL))
            .dependsOn(SEMI_DARK_MONITOR));

    public static final Quest WIRELESS_TERMINAL = CHAPTER.quest("wireless_terminal", q -> q
            .at(-7, 19.5)
            .subtitle("掌上网络")
            .desc(d -> d
                    .line("ME终端的无线版本，可以让你随时随地访问终端")
                    .line()
                    .line("插上量子桥卡和量子缠绕奇点后可以无限制访问，{奇点}所对应的量子环需要与你的网络保持连接", GOLD))
            .task(Task.item(AEItems.WIRELESS_TERMINAL))
            .dependsOn(DENSE_ENERGY_CELL, WIRELESS_RECEIVER, TERMINAL)
            .hideDependencyLines());

    public static final Quest WIRELESS_CRAFTING_TERMINAL = CHAPTER.quest("wireless_crafting_terminal", q -> q
            .at(-5, 19.5)
            .desc(d -> d
                    .line("无线合成终端多了一个设置按钮，进入可以看到5个选框，功能依次是：")
                    .line()
                    .line("- 中键点击实体方块可以直接取用ME网络中对应的方块")
                    .line("- 中键点击实体方块如果缺失且可以合成则自动打开合成页面")
                    .line("- 自动将AE物品补充至背包并且在物品栏显示库存")
                    .line()
                    .line("- 磁铁开关（安装了磁卡的情况下）")
                    .line("- 是否直接将掉落物存入ME网络"))
            .task(Task.item(AEItems.WIRELESS_CRAFTING_TERMINAL))
            .dependsOn(CRAFTING_TERMINAL, DENSE_ENERGY_CELL, WIRELESS_RECEIVER)
            .hideDependencyLines());

    public static final Quest WIRELESS_PATTERN_ACCESS_TERMINAL = CHAPTER.quest("wireless_pattern_access_terminal", q -> q
            .at(-7, 23.5)
            .desc(d -> d.line("样板管理终端的无线版本"))
            .task(Task.item(AE2wtlib.PATTERN_ACCESS_TERMINAL))
            .dependsOn(PATTERN_ACCESS_TERMINAL, WIRELESS_RECEIVER, DENSE_ENERGY_CELL)
            .hideDependencyLines());

    public static final Quest WIRELESS_PATTERN_ENCODING_TERMINAL = CHAPTER.quest("wireless_pattern_encoding_terminal", q -> q
            .at(-3, 19.5)
            .task(Task.item(AE2wtlib.PATTERN_ENCODING_TERMINAL))
            .dependsOn(PATTERN_ENCODING_TERMINAL, WIRELESS_RECEIVER, DENSE_ENERGY_CELL)
            .hideDependencyLines());

    public static final Quest SPATIAL_CELL_COMPONENT_2 = CHAPTER.quest("spatial_cell_component_2", q -> q
            .at(7.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_2_CELL_COMPONENT))
            .dependsOn(FLUIX_PEARL)
            .hideDependencyLines());

    public static final Quest SPATIAL_CELL_COMPONENT_16 = CHAPTER.quest("spatial_cell_component_16", q -> q
            .at(8.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_16_CELL_COMPONENT))
            .dependsOn(SPATIAL_CELL_COMPONENT_2));

    public static final Quest SPATIAL_CELL_COMPONENT_128 = CHAPTER.quest("spatial_cell_component_128", q -> q
            .at(9.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_128_CELL_COMPONENT))
            .dependsOn(SPATIAL_CELL_COMPONENT_16));

    public static final Quest SPATIAL_STORAGE_CELL_2 = CHAPTER.quest("spatial_storage_cell_2", q -> q
            .at(10.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_CELL2))
            .dependsOn(SPATIAL_CELL_COMPONENT_2));

    public static final Quest SPATIAL_STORAGE_CELL_16 = CHAPTER.quest("spatial_storage_cell_16", q -> q
            .at(11.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_CELL16))
            .dependsOn(SPATIAL_CELL_COMPONENT_16));

    public static final Quest SPATIAL_STORAGE_CELL_128 = CHAPTER.quest("spatial_storage_cell_128", q -> q
            .at(12.5, 11.5)
            .task(Task.item(AEItems.SPATIAL_CELL128))
            .dependsOn(SPATIAL_CELL_COMPONENT_128));

    public static final Quest SPATIAL_PYLON = CHAPTER.quest("spatial_pylon", q -> q
            .at(4.5, 11.5)
            .task(Task.item(AEBlocks.SPATIAL_PYLON))
            .dependsOn(FLUIX_DUST, FLUIX_GLASS_CABLE)
            .hideDependencyLines());

    public static final Quest SPATIAL_IO_PORT = CHAPTER.quest("spatial_io_port", q -> q
            .at(5.5, 11.5)
            .task(Task.item(AEBlocks.SPATIAL_IO_PORT))
            .dependsOn(IO_PORT, FLUIX_GLASS_CABLE)
            .hideDependencyLines());

    public static final Quest SPATIAL_ANCHOR = CHAPTER.quest("spatial_anchor", q -> q
            .at(6.5, 11.5)
            .task(Task.item(AEBlocks.SPATIAL_ANCHOR))
            .dependsOn(SPATIAL_CELL_COMPONENT_128));

    public static final Quest CHARGER = CHAPTER.quest("charger", q -> q
            .at(-17, 21.5)
            .desc(d -> d.line("我们有{电弧发生器}，真的还需要这玩意吗？", GOLD))
            .task(Task.item(AEBlocks.CHARGER))
            .optional());

    public static final Quest BASIC_MONITOR = CHAPTER.quest("basic_monitor", q -> q
            .at(1.5, 13.5)
            .icon(GTOMachines.BASIC_MONITOR)
            .desc(d -> d
                    .line("使用列出的方块互相拼接，可以拼出一个或多个自定义的监控墙，供实时观察基地内情况")
                    .line()
                    .line("{搭建一面监控器墙!}", GREEN))
            .task(Task.item(GTOMachines.BASIC_MONITOR), Task.item(GTOItems.COORDINATE_CARD).optional())
            .dependsOn(LvChapter.ALUMINIUM_INGOT));

    public static final Quest MONITOR_ELECTRICITY = CHAPTER.quest("monitor_electricity", q -> q
            .at(2.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_MACHINE_ELECTRICITY))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_MANA = CHAPTER.quest("monitor_mana", q -> q
            .at(3.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_MACHINE_MANA))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_CWU = CHAPTER.quest("monitor_cwu", q -> q
            .at(4.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_MACHINE_CWU))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_CUSTOM = CHAPTER.quest("monitor_custom", q -> q
            .at(5.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_MACHINE_CUSTOM))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_AE_THROUGHPUT = CHAPTER.quest("monitor_ae_throughput", q -> q
            .at(6.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_AE_THROUGHPUT))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_AE_CPU = CHAPTER.quest("monitor_ae_cpu", q -> q
            .at(7.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_AE_CPU))
            .dependsOn(BASIC_MONITOR));

    public static final Quest MONITOR_MACHINE = CHAPTER.quest("monitor_machine", q -> q
            .at(8.5, 13.5)
            .task(Task.item(GTOMachines.MONITOR_MACHINE))
            .dependsOn(BASIC_MONITOR));

    public static final Quest CONDENSER = CHAPTER.quest("condenser", q -> q
            .at(-17, 13.5)
            .desc(d -> d
                    .line("用于合成{奇点}和小批量合成{物质球}，也可以当作{垃圾桶}用", GOLD, GOLD, YELLOW)
                    .line()
                    .line("合成物品时需要在上方放入一个{ME存储组件}（等级任意），且其容量需要大于合成所需要的容量", GOLD)
                    .line()
                    .line("{如果不放入ME存储组件则所有模式都是销毁模式}", YELLOW))
            .task(Task.item(AEBlocks.CONDENSER))
            .hideDependencyLines());

    public static final Quest SINGULARITY = CHAPTER.quest("singularity", q -> q
            .at(-17, 15.5)
            .task(Task.item(AEItems.SINGULARITY))
            .reward(Reward.item(Items.ENDER_PEARL))
            .dependsOn(CONDENSER));

    public static final Quest MATTER_BALL = CHAPTER.quest("matter_ball", q -> q
            .at(-19, 15.5)
            .task(Task.item(AEItems.MATTER_BALL))
            .dependsOn(CONDENSER));

    public static final Quest CROSS_DIMENSIONAL_TRANSMISSION = CHAPTER.quest("cross_dimensional_transmission", q -> q
            .at(-17, 17.5)
            .title("跨维度传输")
            .subtitle("{[-(ħ^2/2m)∇^2 + V(r,t)]ψ(r,t) = iħψ_t(r,t)}", BOLD)
            .desc(d -> d.line("可以放置在{量子网桥}和{无线终端}中进行跨维度通信", GOLD, GOLD))
            .task(Task.item(AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2))
            .dependsOn(SINGULARITY)
            .hideDependentLines());

    public static final Quest VIEW_CELL = CHAPTER.quest("view_cell", q -> q
            .at(1.5, 16.5)
            .icon(AEItems.VIEW_CELL)
            .desc(d -> d
                    .line("{显示元件}可实现{选择性显示}网络的内容物", GOLD, YELLOW)
                    .line()
                    .line("使用{元件工作台}进行标记，配合{模糊卡}、{反相卡}可以标记显示或屏蔽一类物品", GOLD, GOLD, GOLD)
                    .line()
                    .line("{e.g. 标记}{虚拟物品提供器}{+模糊卡+反相卡=屏蔽全部的虚拟物品}", GREEN, GOLD, GREEN))
            .task(Task.item(AEItems.VIEW_CELL), Task.item(AEItems.FUZZY_CARD).optional(), Task.item(AEItems.INVERTER_CARD).optional())
            .dependsOn(ITEM_CELL_HOUSING));

    public static final Quest HIDE_SOME_THINGS_YOU_DON_T = CHAPTER.quest("hide_some_things_you_don_t", q -> q
            .at(2.5, 16.5)
            .icon(AE2ToggleableViewCell.TOGGLEABLE_VIEW_CELL_ITEM.get())
            .title("用来隐藏一些你不想看到的玩意")
            .subtitle("清爽的界面")
            .desc(d -> d.line("{可切换显示元件}是{显示元件}的高级版本，可实现{选择性显示}网络的内容物，同时带有{可切换启用}功能", GOLD, GOLD, YELLOW, YELLOW))
            .task(Task.item(AE2ToggleableViewCell.TOGGLEABLE_VIEW_CELL_ITEM.get()))
            .dependsOn(VIEW_CELL));

    public static final Quest ENTANGLED_SINGULARITY = CHAPTER.quest("entangled_singularity", q -> q
            .at(-19, 17.5)
            .task(Task.item(GTOItems.ENTANGLED_SINGULARITY))
            .dependsOn(SINGULARITY));

    public static final Quest P2P_SAMPLE = CHAPTER.quest("p2p_sample", q -> q
            .at(-19, 19.5)
            .icon(GTOMachines.TESSERACT_GENERATOR)
            .title("超立方效应的应用")
            .subtitle("比镜像更好")
            .desc(d -> d.line("和纠缠方块类似，可以代理一个流体或物品容器，相当于隔空操作方块进行物流！"))
            .task(
                    Task.item(GTOMachines.TESSERACT_GENERATOR),
                    Task.item(GTOItems.COORDINATE_CARD).optional(),
                    Task.item(GTOItems.TESSERACT_TARGET_MARKER).optional())
            .dependsOn(ENTANGLED_SINGULARITY, HvChapter.IMPLOSION_COMPRESSOR)
            .optional());

    public static final Quest PAINT = CHAPTER.quest("paint", q -> q
            .at(-21, 15.5)
            .title("染色球")
            .task(Task.tag(ConventionTags.PAINT_BALLS, 1).title("任意染色球"))
            .dependsOn(MATTER_BALL));

    public static final Quest LIGHT_TRANSMISSION_PAINT = CHAPTER.quest("light_transmission_paint", q -> q
            .at(-23, 15.5)
            .icon(AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.BLACK))
            .title("光通染色球")
            .task(
                    Task.anyOf(1, AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.BLACK), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.LIGHT_BLUE), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.YELLOW), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.LIME), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.PINK), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.GRAY), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.LIGHT_GRAY), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.CYAN), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.PURPLE), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.MAGENTA), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.ORANGE), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.BROWN), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.WHITE), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.RED), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.GREEN)).title("任意光通染色球"))
            .dependsOn(PAINT));

    public static final Quest ME_CPU = CHAPTER.quest("me_cpu", q -> q
            .at(-3, 11.5).minWidth(225)
            .icon(MultiBlockG.ME_CPU)
            .subtitle("格雷科技™信息园算力中心")
            .desc(d -> d
                    .line("这个多方块机器是格雷公司™最强大的主脑（合成CPU），最多支持481个{合成存储核心}，可安装{并行控制仓}提高CPU并行数，{存储核心}可以{混搭}安装", GOLD, GOLD, GOLD, YELLOW)
                    .line()
                    .line("虽然控制器方块只需要{IV}电路和{EV}组装机，但是{高能机械方块}在{IV}阶段才能制作", IV, EV, GOLD, IV)
                    .line()
                    .line("{存储容量} = 已安装的{合成存储核心}容量之和", YELLOW, GOLD)
                    .line("{并行数} = 并行控制仓的并行数 * 已安装{合成存储核心}个数", YELLOW, GOLD)
                    .line("{线程数} = 2^结构玻璃等级", YELLOW))
            .task(Task.item(MultiBlockG.ME_CPU), Task.item(GTAEMachines.CRAFTING_CPU_INTERFACE))
            .dependsOn(EvChapter.ALLOY_BLAST_SMELTER, EvChapter.CHEMICAL_VAPOR_DEPOSITION, IvChapter.IV_ASSEMBLER));

    public static final Quest CRYSTAL_FIXER = CHAPTER.quest("crystal_fixer", q -> q
            .at(-15, 13.5)
            .desc(d -> d
                    .line("用于修复{赛特斯石英母岩}（包括{赛特斯石英块}），每次修复都会使母岩等级{提升一级}", GOLD, GOLD, YELLOW)
                    .line()
                    .line("可以将母岩修复至最高“有暇”的等级。再往上修复需要通过{化学浸洗机}修复", GOLD)
                    .line()
                    .line("在底部输入能量，顶部放置母岩，中间塞入{充能赛特斯石英水晶}即可修复", GOLD)
                    .line()
                    .line("母岩在生长赛特斯石英晶体的过程中有概率退化，最终变为赛特斯石英块。{无瑕的}赛特斯石英母岩不会退化", AQUA)
                    .line()
                    .line("{直接挖赛特斯石英矿脉可能来的更快}", DARK_GRAY.italic()))
            .task(Task.item(EPPItemAndBlock.CRYSTAL_FIXER))
            .dependsOn(FLUIX_CRYSTAL));

    public static final Quest OBTAIN_SET_QUARTZ_USING_VANILLA_METHODS = CHAPTER.quest("obtain_set_quartz_using_vanilla_methods", q -> q
            .at(-13, 13.5)
            .icon(AEBlocks.FLAWLESS_BUDDING_QUARTZ)
            .title("原版的赛特斯石英水晶获取方式")
            .subtitle("无限的赛特斯石英水晶")
            .desc(d -> d
                    .line("使用{催生器}加速母岩生成。催生器的顶面和底面可用于输入能量，周围四个面{紧贴母岩}来加速石英芽生长", GOLD, YELLOW)
                    .line()
                    .line("同样也可以用来加速其他通过随机刻生长的作物")
                    .line()
                    .line("{使用破坏面板和存储总线做个子网络实现自动采集}", GRAY.italic())
                    .line()
                    .line("{直接挖赛特斯石英矿脉可能来的更快}", DARK_GRAY.italic()))
            .task(
                    Task.item(AEBlocks.GROWTH_ACCELERATOR),
                    Task.anyOf(1, AEBlocks.DAMAGED_BUDDING_QUARTZ, AEBlocks.CHIPPED_BUDDING_QUARTZ, AEBlocks.FLAWED_BUDDING_QUARTZ, AEBlocks.FLAWLESS_BUDDING_QUARTZ).optional().title("任意赛特斯石英母岩"))
            .dependsOn(FLUIX_GLASS_CABLE, CRYSTAL_FIXER)
            .requireAny());

    public static final Quest THE_THIRD_PIECE_OF_THE_AUTO = CHAPTER.quest("the_third_piece_of_the_auto", q -> q
            .at(-7, 3.5)
            .title("自动合成系统的第三块拼图")
            .desc(d -> d
                    .line("有了它你才可以开始自动合成")
                    .line()
                    .line("下单UI经过修改，c和f分别代表向上和向下取整，按住Shift即可从加减调整数量变成乘除调整数量。对发信器UI同样有效"))
            .task(Task.item(AEItems.BLANK_PATTERN))
            .reward(Reward.item(AEItems.BLANK_PATTERN, 4), Reward.item(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 16))
            .dependsOn(HvChapter.NAND_MEMORY_CHIP, UtilChapter.Q_056EC4E6)
            .requireAny());

    public static final Quest PATTERN_MODIFIER = CHAPTER.quest("pattern_modifier", q -> q
            .at(-5, 3.5)
            .task(Task.item(EPPItemAndBlock.PATTERN_MODIFIER), Task.item(GTOItems.PATTERN_MODIFIER_PRO))
            .dependsOn(THE_THIRD_PIECE_OF_THE_AUTO));

    public static final Quest ORDER = CHAPTER.quest("order", q -> q
            .at(-17, 1.5)
            .subtitle("妙妙工具系列#1")
            .desc(d -> d
                    .line("{订单}是一种用来完成一次{“伪合成请求”}的{编码样板}，你可以用它来直接请求合成{一台或多台多方块机器的所有方块}，或自定义一个你想要的{“套餐”}，比如{一座建筑的所有建筑材料}！", GOLD, YELLOW, GOLD, AQUA, YELLOW, AQUA)
                    .line()
                    .line("{多方块页面的填充配方已经内置了订单，快去试试吧}", GRAY.italic()))
            .task(Task.item(GTOItems.ORDER))
            .dependsOn(THE_THIRD_PIECE_OF_THE_AUTO)
            .hideDependencyLines());

    public static final Quest PATTERN_CONTENT_ACCESS_TERMINAL = CHAPTER.quest("pattern_content_access_terminal", q -> q
            .at(-3, 3.5)
            .subtitle("妙妙工具系列#3")
            .desc(d -> d
                    .line("你是否遇到了下列问题：")
                    .line()
                    .line("你想要优先消耗更便宜的电路（比如磁共振电路），但基地里的样板早已成一团乱麻，即使使用{样板修改器PRO}修改样板也仍使你疲倦不堪！", GOLD)
                    .line()
                    .line("这个动态样板修改器能够在ME合成系统每次发配样板原料时{动态修改}其样板内容，从而让{特定原料能够优先消耗}！", GOLD, YELLOW))
            .task(Task.item(GTOAEParts.Pattern_Content_Access_Terminal.get()))
            .dependsOn(THE_THIRD_PIECE_OF_THE_AUTO));

    public static final Quest ME_WIRELESS_CONNECTION_MACHINE = CHAPTER.quest("me_wireless_connection_machine", q -> q
            .at(-21, 9.5)
            .subtitle("妙妙工具系列#2")
            .desc(d -> d
                    .line("真正的无线ME网络，能够{跨维度传输}", YELLOW)
                    .line()
                    .line("后续你取得的ME仓室大多都内置它的功能，你可以将线缆接到任意ME仓室上从而接出网络"))
            .task(Task.item(GTOMachines.ME_WIRELESS_CONNECTION_MACHINE))
            .dependsOn(WIRELESS_CONNECTION, HvChapter.MICROPROCESSOR_SUPERCOMPUTER));

    public static final Quest BUILD_QUANTUM_BRIDGE = CHAPTER.quest("build_quantum_bridge", q -> q
            .at(-19, 7.5)
            .icon(GTOItems.QUANTUM_ANOMALY)
            .title("搭建量子网桥")
            .desc(d -> d
                    .line("摆放两个如下所示的{量子网桥}，将一对{量子缠绕态奇点}放入其中，两个量子网桥就可以跨维度相连", GOLD, GOLD)
                    .image(QuestTextures.QUANTUM_RING, 100, 100))
            .task(Task.checkmark())
            .dependsOn(CROSS_DIMENSIONAL_TRANSMISSION, QUANTUM_RING, QUANTUM_LINK)
            .hideDependentLines());

    public static final Quest QUANTUM_BRIDGE_CARD = CHAPTER.quest("quantum_bridge_card", q -> q
            .at(-19, 13.5)
            .desc(d -> d.line("给无线终端装上{量子桥卡}，然后将一对{量子缠绕态奇点}的一个放在{无线终端}中，一个放在链接ME网络的{量子网桥}中，即可让无线终端{无视距离，跨维度通信}ME网络", GOLD, GOLD, GREEN, GREEN, YELLOW))
            .task(Task.item(AE2wtlib.QUANTUM_BRIDGE_CARD))
            .dependsOn(WIRELESS_RECEIVER, BUILD_QUANTUM_BRIDGE));

    public static final Quest SPRAY_CAN = CHAPTER.quest("spray_can", q -> q
            .at(-19, 1.5)
            .title("喷漆罐")
            .desc(d -> d
                    .line("你可以用这个工具染色任意的{输入/输出仓室}，包括{样板总成}！", GOLD, GOLD)
                    .line()
                    .line("{染同色}的输入仓室内部{互通}。若你染色输入和输出仓室，则输入和输出仓室将会被绑定。", YELLOW, YELLOW)
                    .line()
                    .line("同色仓室与无色/异色仓室之间会被{隔离}。", YELLOW)
                    .line()
                    .line("{再说明两种情况：1）如果输入染色，输出不染色，仍然能够正常输出；2）在染色的输入仓室中打开仓室隔离仍然会隔离其他同色仓室}", GRAY.italic()))
            .task(
                    Task.anyOf(1, GTItems.SPRAY_CAN_DYES[15], GTItems.SPRAY_CAN_DYES[11], GTItems.SPRAY_CAN_DYES[12], GTItems.SPRAY_CAN_DYES[9], GTItems.SPRAY_EMPTY, GTItems.SPRAY_CAN_DYES[7], GTItems.SPRAY_CAN_DYES[13], GTItems.INFINITE_SPRAY_CAN, GTItems.SPRAY_CAN_DYES[3], GTItems.SPRAY_CAN_DYES[8], GTItems.SPRAY_CAN_DYES[5], GTItems.SPRAY_CAN_DYES[2], GTItems.SPRAY_CAN_DYES[1], GTItems.SPRAY_CAN_DYES[6], GTItems.SPRAY_CAN_DYES[10], GTItems.SPRAY_CAN_DYES[14], GTItems.SPRAY_SOLVENT, GTItems.SPRAY_CAN_DYES[0], GTItems.SPRAY_CAN_DYES[4]).title("任意喷漆罐"))
            .dependsOn(SteamChapter.PREPARING_GLASS_STEP_THREE));

    public static final Quest NO_AE_TWEAKS_EASY = CHAPTER.quest("no_ae_tweaks_easy", q -> q
            .at(-7, -0.5)
            .icon(AEItems.WIRELESS_TERMINAL)
            .title("无AE魔改{（简单）}", GREEN)
            .subtitle("简单模式下，AE配方未经魔改！")
            .desc(d -> d
                    .line("在简单模式下，部分任务指引不适用，如{驱动器}，{空白样板}，{样板供应器}，{合成单元}，{分子装配室}等。请{无视它们在LV、MV的前置要求}", GOLD, GOLD, GOLD, GOLD, GOLD, YELLOW)
                    .line()
                    .line("{在蒸汽时代，你已经可以使用AE开始自动化}", YELLOW)
                    .line()
                    .line("{也请忽略下“应用能源起步”任务里面电压限制相关内容啦，笔者暂时技术力不够，没办法把这个任务藏起来}", GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(UtilChapter.Q_056EC4E6, GETTING_STARTED_WITH_AE)
            .optional().hideUntilDepsVisible());

    public static final Quest AE_CIRCUIT_BOARD = CHAPTER.quest("ae_circuit_board", q -> q
            .at(-11, 3.5)
            .title("AE电路板")
            .task(
                    Task.item(AEItems.SILICON_PRINT),
                    Task.item(AEItems.CALCULATION_PROCESSOR_PRINT),
                    Task.item(AEItems.ENGINEERING_PROCESSOR_PRINT),
                    Task.item(AEItems.LOGIC_PROCESSOR_PRINT))
            .dependsOn(UtilChapter.INSCRIBER_EASY, ALIEN_TECHNOLOGY, MvChapter.SILICON_INGOT)
            .requireAny()
            .requireCount(2)
            .hideDependentLines());

    public static final Quest ME_ENERGY_SUBSTATION = CHAPTER.quest("me_energy_substation", q -> q
            .at(-1, 11.5)
            .icon(MultiBlockG.ME_ENERGY_SUBSTATION)
            .subtitle("格雷科技™信息园能源中心")
            .desc(d -> d
                    .line("如果你需要大量的AE能源，可以看看这台多方块")
                    .line()
                    .line("不过它的寿命可能有点短，你在UV就可以取得创造能源元件，无需给ME网络传输电量了！"))
            .task(Task.item(MultiBlockG.ME_ENERGY_SUBSTATION), Task.item(GTAEMachines.ME_ENERGY_ACCESS_HATCH))
            .dependsOn(IvChapter.HPIC_CHIP));

    public static final Quest ME_MUFFLER_HATCH = CHAPTER.quest("me_muffler_hatch", q -> q
            .at(1.5, 19.5)
            .desc(d -> d.line("解除消声仓前必须空三格的限制，且将灰烬自动收集进ME网络"))
            .task(Task.item(GTAEMachines.MUFFLER_HATCH_ME))
            .dependsOn(LuvChapter.LUV_FIELD_GENERATOR, LuvChapter.IV_DRONE));

    public static final Quest FLUIX_SMART_DENSE_CABLE = CHAPTER.quest("fluix_smart_dense_cable", q -> q
            .at(-13, 17.5)
            .task(Task.item(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT)))
            .dependsOn(FLUIX_SMART_CABLE));

    public static final Quest ANY_CERTUS_QUARTZ_CUTTING_KNIFE = CHAPTER.quest("any_certus_quartz_cutting_knife", q -> q
            .at(-23, 1.5)
            .desc(d -> d
                    .line("Shift+右键可以获取任何方块的原始名称，右键属于AE的方块可以直接打开重命名页面")
                    .line()
                    .line("{主要用于给样板供应器改名}", GRAY.italic()))
            .task(Task.anyOf(1, AEItems.CERTUS_QUARTZ_KNIFE, AEItems.NETHER_QUARTZ_KNIFE).title("任意石英切割刀")));

    public static final Quest EX_PATTERN_ACCESS_PART = CHAPTER.quest("ex_pattern_access_part", q -> q
            .at(-5, 21.5)
            .desc(d -> d.line("用于管理所有连接至ME网络的样板供应器和样板总成，附带搜索和定位功能"))
            .task(Task.item(EPPItemAndBlock.EX_PATTERN_TERMINAL))
            .dependsOn(PATTERN_ACCESS_TERMINAL));

    public static final Quest ME_2IN1_TERMINAL = CHAPTER.quest("me_2in1_terminal", q -> q
            .at(-3, 21.5)
            .desc(d -> d
                    .line("2合1终端整合了样板管理和样板编码的功能，能够批量编码一系列同种类的样板，支持一键填充配方！")
                    .line()
                    .line("- 同时显示两个终端显示的所有面板")
                    .line()
                    .line("- 对着样板撰写面板中的物品中键可以重命名样板物品（方便装配线的样板编写）")
                    .line()
                    .line("- 可以快速将样板填充入样板供应器和样板总成（点击样板管理终端面板的左侧按钮开启此功能）")
                    .line()
                    .line("- 在EMI中查看压缩机，压膜机，车床等有批量编码需求的配方可以使用Alt+左键打开批量编码页面")
                    .line()
                    .line("- {拖曳任意面板的右下角可调整其大小}", GOLD))
            .task(Task.item(GTOAEParts.ME_2IN1_TERMINAL.get()))
            .dependsOn(EX_PATTERN_ACCESS_PART));

    public static final Quest WIRELESS_ME2IN1_TERMINAL = CHAPTER.quest("wireless_me2in1_terminal", q -> q
            .at(-3, 23.5)
            .desc(d -> d.line("2合1终端的无线版本"))
            .task(Task.item(GTOItems.WIRELESS_ME2IN1))
            .dependsOn(ME_2IN1_TERMINAL, WIRELESS_RECEIVER, DENSE_ENERGY_CELL)
            .hideDependencyLines());

    public static final Quest WIRELESS_EX_PAT = CHAPTER.quest("wireless_ex_pat", q -> q
            .at(-5, 23.5)
            .desc(d -> d.line("样板管理终端的无线版本"))
            .task(Task.item(EPPItemAndBlock.WIRELESS_EX_PAT))
            .dependsOn(EX_PATTERN_ACCESS_PART, WIRELESS_RECEIVER, DENSE_ENERGY_CELL)
            .hideDependencyLines());

    public static final Quest FACILITY_TERMINAL = CHAPTER.quest("facility_terminal", q -> q
            .at(-1, 21.5)
            .icon(AEParts.FACILITY_MANAGEMENT)
            .desc(d -> d.line("用于管理各类ME输入和输出仓室，附带流量监控、搜索和定位功能"))
            .task(Task.item(AEParts.FACILITY_MANAGEMENT))
            .dependsOn(SEMI_DARK_MONITOR)
            .hideDependencyLines());

    public static final Quest EX_CRAFTING_TERMINAL = CHAPTER.quest("ex_crafting_terminal", q -> q
            .at(-9, 23.5)
            .task(Task.item(EPPItemAndBlock.EX_CRAFTING_TERMINAL))
            .dependsOn(CRAFTING_TERMINAL)
            .hideDependencyLines());

    public static final Quest SOURCE_NODE_AND_CHILD_NODE = CHAPTER.quest("source_node_and_child_node", q -> q
            .at(-21, 7.5)
            .icon(AEItems.NETWORK_TOOL)
            .title("源节点和子节点")
            .desc(d -> d
                    .line("0.5.6-alpha解除了专家的频道限制，但仍然保留了源子节点")
                    .line()
                    .line("每个无线网络中都必须拥有{恰好一个源节点}和{无限数量的子节点}，源节点只能通过{ME无线连接机}设置", RED, AQUA, GOLD)
                    .line()
                    .line("简单来说：")
                    .line()
                    .line("1. 首先摆放一个单独的{无线连接机}（这个无线连接机建议和你的网络存储直连），进入GUI", GOLD)
                    .line()
                    .line("2. {新建无线网络}，然后在下方区域选择对应的无线网络，点击切换节点类型设置为{源节点}", YELLOW, GREEN)
                    .line()
                    .line("3. 将你需要的机器都连上网络即可，这些机器默认都会成为子节点。别忘了你可以收藏某个网络来简化连接过程"))
            .task(Task.checkmark())
            .dependsOn(ME_WIRELESS_CONNECTION_MACHINE)
            .hideUntilDepsVisible());

    public static final Quest ME_INPUT_BUFFER_PART_MACHINE = CHAPTER.quest("me_input_buffer_part_machine", q -> q
            .at(9.5, 19.5)
            .task(Task.item(GTAEMachines.ME_INPUT_BUFFER_PART_MACHINE)));

    public static final Quest ME_REQUESTABLE_INPUT_BUS_MACHINE = CHAPTER.quest("me_requestable_input_bus_machine", q -> q
            .at(5.5, 19.5)
            .task(Task.item(GTAEMachines.ME_REQUESTABLE_INPUT_BUS_MACHINE)));

    public static final Quest ME_REQUESTABLE_INPUT_HATCH_MACHINE = CHAPTER.quest("me_requestable_input_hatch_machine", q -> q
            .at(7.5, 19.5)
            .task(Task.item(GTAEMachines.ME_REQUESTABLE_INPUT_HATCH_MACHINE)));

    public static final Quest Q_40FDBD03 = CHAPTER.quest("q_40fdbd03", q -> q
            .at(3.5, 19.5)
            .desc(d -> d.line("0.5.5新加入的仓室"))
            .task(Task.checkmark().title("新加入的仓室→")));

    public static final Quest LOGIC_PROCESSOR = CHAPTER.quest("logic_processor", q -> q
            .at(-9, 4.5)
            .subtitle("AE的科技起步")
            .task(Task.item(AEItems.LOGIC_PROCESSOR), Task.item(AEItems.CALCULATION_PROCESSOR), Task.item(AEItems.ENGINEERING_PROCESSOR))
            .dependsOn(GETTING_STARTED_WITH_AE, AE_CIRCUIT_BOARD));

    public static final Quest TOOL_FOR_CHECKING_NETWORKS = CHAPTER.quest("tool_for_checking_networks", q -> q
            .at(-15, 1.5)
            .title("检查网络的工具")
            .subtitle("好家伙！")
            .task(Task.item(AEAItems.ANALYSER))
            .dependsOn(LOGIC_PROCESSOR)
            .hideDependencyLines());

    public static final Quest ANNIHILATION_CORE = CHAPTER.quest("annihilation_core", q -> q
            .at(-9, 7.5)
            .subtitle("AE的工业起步")
            .task(Task.item(AEItems.ANNIHILATION_CORE), Task.item(AEItems.FORMATION_CORE))
            .dependsOn(CERTUS_QUARTZ_DUST, LOGIC_PROCESSOR)
            .requireAny());

    public static final Quest IMPORT_BUS = CHAPTER.quest("import_bus", q -> q
            .at(-9, 13.5)
            .icon(AEParts.IMPORT_BUS)
            .subtitle("ME网络的交互节点")
            .desc(d -> d
                    .line("将贴着的方块的内容物主动抽取至ME网络，没有过滤时默认全部抽取")
                    .line()
                    .line("经修改，单次抽取上限非常高"))
            .task(Task.item(AEParts.IMPORT_BUS))
            .dependsOn(FLUIX_GLASS_CABLE, ANNIHILATION_CORE));

    public static final Quest INTERFACE = CHAPTER.quest("interface", q -> q
            .at(-11, 7.5)
            .desc(d -> d
                    .line("可以自动接收其他相邻方块主动传输过来的物品并送入ME网络")
                    .line()
                    .line("也可以从ME网络中获取物品暂时存储在自身，等待相邻方块抽取")
                    .line()
                    .line("有和{存储总线}搭配的进阶用法，会将链接的ME网络模拟成一个{存储容器}，可用于过滤和构建子网", GOLD, YELLOW)
                    .line()
                    .line("{不会主动输出}", GRAY.italic()))
            .task(Task.item(AEBlocks.INTERFACE))
            .dependsOn(ANNIHILATION_CORE));

    public static final Quest STORAGE_BUS = CHAPTER.quest("storage_bus", q -> q
            .at(-15, 17.5)
            .desc(d -> d
                    .line("字面意义，会将ME网络中{无处可去的}物品存入贴着的方块，也可以读取贴着的方块的内容物显示在ME网络中", YELLOW)
                    .line()
                    .line("有和{ME接口}搭配的进阶用法，贴着ME接口放置可将ME接口所在的ME网络视为一个{存储容器}，可用于过滤和构建子网", GOLD, YELLOW)
                    .line()
                    .line("{直接使用它并不能抽取已经在其他存储容器（如驱动器）中的物品}", GRAY.italic()))
            .task(Task.item(AEParts.STORAGE_BUS))
            .dependsOn(INTERFACE)
            .hideDependencyLines());

    public static final Quest REQUESTER = CHAPTER.quest("requester", q -> q
            .at(1.5, 12.5)
            .task(Task.item(Registration.REQUESTER))
            .dependsOn(CRAFTING_ACCELERATOR, INTERFACE, FLUIX_GLASS_CABLE)
            .hideDependencyLines().hideDependentLines());

    public static final Quest PRECISE_STORAGE_BUS = CHAPTER.quest("precise_storage_bus", q -> q
            .at(-15, 19.5)
            .task(Task.item(EPPItemAndBlock.PRECISE_STORAGE_BUS))
            .dependsOn(STORAGE_BUS));

    public static final Quest THE_FIRST_PIECE_OF_THE_AUTO = CHAPTER.quest("the_first_piece_of_the_auto", q -> q
            .at(-7, 5.5)
            .title("自动合成系统的第一块拼图")
            .subtitle("自动合成起步")
            .desc(d -> d
                    .line("有了它你才可以开始自动合成")
                    .line()
                    .line("样板供应器有很多阻挡模式，大部分时候开启{“非同一样板时阻止插入”}即可", YELLOW))
            .task(Task.item(AEBlocks.PATTERN_PROVIDER))
            .reward(Reward.item(AEBlocks.PATTERN_PROVIDER), Reward.item(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 16))
            .dependsOn(ANNIHILATION_CORE, HvChapter.HV_ASSEMBLER, UtilChapter.Q_056EC4E6)
            .requireAny()
            .requireCount(2)
            .hideDependentLines());

    public static final Quest ME_SIMPLE_PATTERN_BUFFER_EASY = CHAPTER.quest("me_simple_pattern_buffer_easy", q -> {
        q.at(-5, -0.5)
                .title("ME简单样板总成{（简单）}", GREEN)
                .subtitle("超前期的样板总成")
                .desc(d -> d
                        .line("只在简单模式可用！")
                        .line()
                        .line("放入样板并连接ME网络以自动发配合成原料，继承了普通样板总成的所有优化特性"))
                .task(Task.item(QuestExternalRefs.optional(() -> OptionalMachine.ME_SIMPLE_PATTERN_BUFFER != null, () -> OptionalMachine.ME_SIMPLE_PATTERN_BUFFER)))
                .dependsOn(THE_FIRST_PIECE_OF_THE_AUTO, UtilChapter.Q_056EC4E6, NO_AE_TWEAKS_EASY, LvChapter.LIQUEFACTION_FURNACE)
                .optional().hideUntilDepsVisible();
        if (OptionalMachine.ME_SIMPLE_PATTERN_BUFFER == null) q.invisible();
    });

    public static final Quest EX_INTERFACE = CHAPTER.quest("ex_interface", q -> q
            .at(-13, 7.5)
            .icon(TieredInterfaces.block(1))
            .task(Task.item(TieredInterfaces.block(1)), Task.item(TieredInterfaces.part(1)).optional())
            .dependsOn(INTERFACE));

    public static final Quest EX_IMPORT_BUS_PART = CHAPTER.quest("ex_import_bus_part", q -> q
            .at(-7, 13.5)
            .task(Task.item(EPPItemAndBlock.EX_IMPORT_BUS))
            .dependsOn(IMPORT_BUS));

    public static final Quest ADVANCED_CARD = CHAPTER.quest("advanced_card", q -> q
            .at(1.5, 15.5)
            .task(Task.item(AEItems.ADVANCED_CARD))
            .dependsOn(LOGIC_PROCESSOR, MvChapter.STAINLESS_STEEL_INGOT)
            .hideDependencyLines());

    public static final Quest BASIC_CARD = CHAPTER.quest("basic_card", q -> q
            .at(1.5, 14.5)
            .task(Task.item(AEItems.BASIC_CARD))
            .dependsOn(LOGIC_PROCESSOR, LvChapter.ALUMINIUM_INGOT)
            .hideDependencyLines());

    public static final Quest REDSTONE_CARD = CHAPTER.quest("redstone_card", q -> q
            .at(4.5, 14.5)
            .task(Task.item(AEItems.REDSTONE_CARD))
            .dependsOn(BASIC_CARD));

    public static final Quest CAPACITY_CARD = CHAPTER.quest("capacity_card", q -> q
            .at(5.5, 14.5)
            .task(Task.item(AEItems.CAPACITY_CARD))
            .dependsOn(BASIC_CARD));

    public static final Quest VOID_CARD = CHAPTER.quest("void_card", q -> q
            .at(3.5, 14.5)
            .task(Task.item(AEItems.VOID_CARD))
            .dependsOn(BASIC_CARD));

    public static final Quest FUZZY_CARD = CHAPTER.quest("fuzzy_card", q -> q
            .at(3.5, 15.5)
            .desc(d -> d
                    .line("顾名思义，插入该升级后，将模糊匹配选定物品，比如:")
                    .line()
                    .line("- 标记原木则会匹配所有原木")
                    .line("- 忽略选定物品的NBT值"))
            .task(Task.item(AEItems.FUZZY_CARD))
            .dependsOn(ADVANCED_CARD));

    public static final Quest SPEED_CARD = CHAPTER.quest("speed_card", q -> q
            .at(2.5, 15.5)
            .desc(d -> d.line("顾名思义，加速机器运作，但需要耗费更多能量"))
            .task(Task.item(AEItems.SPEED_CARD))
            .dependsOn(ADVANCED_CARD)
            .hideDependentLines());

    public static final Quest ME_IMPORT_BUS_AND_ME_IMPORT = CHAPTER.quest("me_import_bus_and_me_import", q -> q
            .at(-7, 9.5)
            .icon(GTAEMachines.ITEM_IMPORT_BUS_ME)
            .title("ME输入总线和ME输入仓")
            .subtitle("好用，爱用，多用")
            .desc(d -> d.line("不存在总成版本"))
            .task(Task.item(GTAEMachines.ITEM_IMPORT_BUS_ME), Task.item(GTAEMachines.FLUID_IMPORT_HATCH_ME))
            .dependsOn(SPEED_CARD, EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE));

    public static final Quest ME_EXPORT_BUS_AND_ME_EXPORT = CHAPTER.quest("me_export_bus_and_me_export", q -> q
            .at(-7, 7.5)
            .title("ME输出总线和ME输出仓")
            .desc(d -> d
                    .line("将多方块机器的产物直接输出到ME网络！")
                    .line()
                    .line("该类仓可以让机器使用异步输出，让配方输出完全不占用TPS"))
            .task(Task.item(GTAEMachines.FLUID_EXPORT_HATCH_ME), Task.item(GTAEMachines.ITEM_EXPORT_BUS_ME))
            .dependsOn(SPEED_CARD, EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE));

    public static final Quest ME_EXPORT_BUFFER = CHAPTER.quest("me_export_buffer", q -> q
            .at(-5, 7.5)
            .desc(d -> d.line("和普通输出总成类似，能够同时输出物品和流体"))
            .task(Task.item(CustomMachines.ME_EXPORT_BUFFER))
            .dependsOn(ME_EXPORT_BUS_AND_ME_EXPORT, LuvChapter.LUV_ASSEMBLER, ME_IMPORT_BUS_AND_ME_IMPORT));

    public static final Quest ME_IMPORT_BUS_AND_ME_IMPORT_2 = CHAPTER.quest("me_import_bus_and_me_import_2", q -> q
            .at(-5, 9.5)
            .title("ME库存输入总线和ME库存输入仓")
            .desc(d -> d
                    .line("开启自动拉取会直接拉取网络前16的物品，流体，建议做个子网络进行自动拉取")
                    .line()
                    .line("不存在总成版本"))
            .task(Task.item(GTAEMachines.STOCKING_IMPORT_BUS_ME), Task.item(GTAEMachines.STOCKING_IMPORT_HATCH_ME))
            .dependsOn(ME_IMPORT_BUS_AND_ME_IMPORT));

    public static final Quest EX_IO_PORT = CHAPTER.quest("ex_io_port", q -> q
            .at(-3, 13.5)
            .task(Task.item(EPPItemAndBlock.EX_IO_PORT))
            .dependsOn(IO_PORT, SPEED_CARD));

    public static final Quest INVERTER_CARD = CHAPTER.quest("inverter_card", q -> q
            .at(4.5, 15.5)
            .desc(d -> d.line("顾名思义，这个升级卡用于将所选定物品加以反向选择，以实现黑名单过滤"))
            .task(Task.item(AEItems.INVERTER_CARD))
            .dependsOn(ADVANCED_CARD));

    public static final Quest CRAFTING_CARD = CHAPTER.quest("crafting_card", q -> q
            .at(2.5, 14.5)
            .task(Task.item(AEItems.CRAFTING_CARD))
            .dependsOn(BASIC_CARD));

    public static final Quest ENERGY_CARD = CHAPTER.quest("energy_card", q -> q
            .at(5.5, 15.5)
            .desc(d -> d.line("它能够提升工具存储能源的上限，用在无线连接器上能够减少10%的基础能量消耗"))
            .task(Task.item(AEItems.ENERGY_CARD))
            .dependsOn(ADVANCED_CARD));

    public static final Quest INSERT_CARD = CHAPTER.quest("insert_card", q -> q
            .at(6.5, 15.5)
            .desc(d -> d
                    .line("自动将{选定物品}从玩家物品栏{导入}ME网络", YELLOW, YELLOW)
                    .line()
                    .line("需要在{终端左侧}配置白名单进行选定", YELLOW)
                    .line()
                    .line("{典型应用场景：}将挖矿得到的矿石自动通过无线终端存储到ME网络", GOLD))
            .task(Task.item(ModItems.INSERT_CARD.get()))
            .dependsOn(ADVANCED_CARD));

    public static final Quest EXPORT_CARD = CHAPTER.quest("export_card", q -> q
            .at(7.5, 15.5)
            .desc(d -> d
                    .line("从ME网络抽取{选定物品}并{自动补充}玩家物品栏", GOLD, YELLOW)
                    .line()
                    .line("需要在{终端左侧}配置白名单进行选定", YELLOW)
                    .line()
                    .line("{典型应用场景：}在建造基地、多方块机器时，自动补充选定物品", GOLD))
            .task(Task.item(ModItems.EXPORT_CARD.get()))
            .dependsOn(ADVANCED_CARD));

    public static final Quest LEVEL_EMITTER = CHAPTER.quest("level_emitter", q -> q
            .at(-11, 19.5)
            .desc(d -> d
                    .line("大化反的好朋友，被动产线的控制器，强烈建议与{机器覆盖版}搭配使用，{而不是触发总线}", GOLD, GRAY)
                    .line()
                    .line("{发信器UI经过修改，c和f分别代表向上和向下取整，按住Shift即可从加减调整数量变成乘除调整数量。对下单UI同样有效}", GRAY))
            .task(Task.item(AEParts.LEVEL_EMITTER))
            .dependsOn(LOGIC_PROCESSOR)
            .hideDependencyLines());

    public static final Quest HOW_TO_USE_ME_NETWORK_FOR = CHAPTER.quest("how_to_use_me_network_for", q -> q
            .at(-13, 19.5)
            .icon(GTMultiMachines.LARGE_CHEMICAL_REACTOR)
            .title("如何使用ME网络进行简单的缓存")
            .subtitle("不要总使用样板下单，你的时间很宝贵")
            .desc(d -> d
                    .line("{机器控制覆盖版}和{发信器}互相配合，利用{红石信号}做到ME网络{特定}物品或流体的{库存维持}（各种ME仓室也原生支持红石控制，你也可以直接让发信器控制ME仓室）", GOLD, GOLD, YELLOW, AQUA, YELLOW)
                    .line()
                    .line("比如和{大化反}搭配就可以自动合成{硫酸}等常用的化工材料，这样下单合成就不必因产能问题成为合成瓶颈", YELLOW, YELLOW)
                    .line()
                    .line("{ME请求器}也可以做到这点，但其属于自动下单，会占用到{CPU}进行合成", GOLD, YELLOW)
                    .line()
                    .line("{也许你还知道可以用触发总线与发信器配合，但我们不建议你这么做。触发总线的频繁开关会导致频繁重建网络拓扑，大量使用会导致TPS大幅下降}", GRAY.italic()))
            .task(
                    Task.item(GTItems.COVER_MACHINE_CONTROLLER),
                    Task.item(AEParts.LEVEL_EMITTER),
                    Task.item(EPPItemAndBlock.THRESHOLD_LEVEL_EMITTER).optional())
            .dependsOn(LEVEL_EMITTER));

    public static final Quest STORAGE_MONITOR = CHAPTER.quest("storage_monitor", q -> q
            .at(-9, 19.5)
            .task(Task.item(AEParts.STORAGE_MONITOR))
            .dependsOn(LEVEL_EMITTER, SEMI_DARK_MONITOR));

    public static final Quest ENERGY_LEVEL_EMITTER = CHAPTER.quest("energy_level_emitter", q -> q
            .at(-11, 23.5)
            .task(Task.item(AEParts.ENERGY_LEVEL_EMITTER))
            .dependsOn(LEVEL_EMITTER));

    public static final Quest THRESHOLD_LEVEL_EMITTER = CHAPTER.quest("threshold_level_emitter", q -> q
            .at(-11, 21.5)
            .task(Task.item(EPPItemAndBlock.THRESHOLD_LEVEL_EMITTER))
            .dependsOn(LEVEL_EMITTER));

    public static final Quest CELL_COMPONENT_1K = CHAPTER.quest("cell_component_1k", q -> q
            .at(2.5, 5.5)
            .task(Task.item(AEItems.CELL_COMPONENT_1K))
            .dependsOn(LOGIC_PROCESSOR)
            .hideDependencyLines());

    public static final Quest CELL_COMPONENT_4K = CHAPTER.quest("cell_component_4k", q -> q
            .at(3.5, 5.5)
            .task(Task.item(AEItems.CELL_COMPONENT_4K))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest CELL_COMPONENT_16K = CHAPTER.quest("cell_component_16k", q -> q
            .at(4.5, 5.5)
            .task(Task.item(AEItems.CELL_COMPONENT_16K))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest CELL_COMPONENT_64K = CHAPTER.quest("cell_component_64k", q -> q
            .at(5.5, 5.5)
            .task(Task.item(AEItems.CELL_COMPONENT_64K))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest CELL_COMPONENT_256K = CHAPTER.quest("cell_component_256k", q -> q
            .at(6.5, 5.5)
            .task(Task.item(AEItems.CELL_COMPONENT_256K))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest CELL_COMPONENT_1M = CHAPTER.quest("cell_component_1m", q -> q
            .at(7.5, 5.5)
            .task(Task.item(GTOItems.CELL_COMPONENT_1M))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest CELL_COMPONENT_4M = CHAPTER.quest("cell_component_4m", q -> q
            .at(8.5, 5.5)
            .task(Task.item(GTOItems.CELL_COMPONENT_4M))
            .dependsOn(CELL_COMPONENT_1M));

    public static final Quest CELL_COMPONENT_16M = CHAPTER.quest("cell_component_16m", q -> q
            .at(9.5, 5.5)
            .task(Task.item(GTOItems.CELL_COMPONENT_16M))
            .dependsOn(CELL_COMPONENT_4M));

    public static final Quest CELL_COMPONENT_64M = CHAPTER.quest("cell_component_64m", q -> q
            .at(10.5, 5.5)
            .task(Task.item(GTOItems.CELL_COMPONENT_64M))
            .dependsOn(CELL_COMPONENT_16M));

    public static final Quest CELL_COMPONENT_256M = CHAPTER.quest("cell_component_256m", q -> q
            .at(11.5, 5.5)
            .task(Task.item(GTOItems.CELL_COMPONENT_256M))
            .dependsOn(CELL_COMPONENT_64M));

    public static final Quest ITEM_STORAGE_CELL_1K = CHAPTER.quest("item_storage_cell_1k", q -> q
            .at(2.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_1K))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest ITEM_STORAGE_CELL_4K = CHAPTER.quest("item_storage_cell_4k", q -> q
            .at(3.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_4K))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest ITEM_STORAGE_CELL_16K = CHAPTER.quest("item_storage_cell_16k", q -> q
            .at(4.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_16K))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest ITEM_STORAGE_CELL_64K = CHAPTER.quest("item_storage_cell_64k", q -> q
            .at(5.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_64K))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest ITEM_STORAGE_CELL_256K = CHAPTER.quest("item_storage_cell_256k", q -> q
            .at(6.5, 6.5)
            .task(Task.item(AEItems.ITEM_CELL_256K))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest FLUID_STORAGE_CELL_1K = CHAPTER.quest("fluid_storage_cell_1k", q -> q
            .at(2.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_1K))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest FLUID_STORAGE_CELL_4K = CHAPTER.quest("fluid_storage_cell_4k", q -> q
            .at(3.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_4K))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest FLUID_STORAGE_CELL_16K = CHAPTER.quest("fluid_storage_cell_16k", q -> q
            .at(4.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_16K))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest FLUID_STORAGE_CELL_64K = CHAPTER.quest("fluid_storage_cell_64k", q -> q
            .at(5.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_64K))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest FLUID_STORAGE_CELL_256K = CHAPTER.quest("fluid_storage_cell_256k", q -> q
            .at(6.5, 7.5)
            .task(Task.item(AEItems.FLUID_CELL_256K))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest SOURCE_STORAGE_CELL_1K = CHAPTER.quest("source_storage_cell_1k", q -> q
            .at(2.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_1K))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest SOURCE_STORAGE_CELL_4K = CHAPTER.quest("source_storage_cell_4k", q -> q
            .at(3.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_4K))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest SOURCE_STORAGE_CELL_16K = CHAPTER.quest("source_storage_cell_16k", q -> q
            .at(4.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_16K))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest SOURCE_STORAGE_CELL_64K = CHAPTER.quest("source_storage_cell_64k", q -> q
            .at(5.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_64K))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest SOURCE_STORAGE_CELL_256K = CHAPTER.quest("source_storage_cell_256k", q -> q
            .at(6.5, 9.5)
            .task(Task.item(ArsEngItems.SOURCE_CELL_256K))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest MANA_STORAGE_CELL_1K = CHAPTER.quest("mana_storage_cell_1k", q -> q
            .at(2.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_1K.get()))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest MANA_STORAGE_CELL_4K = CHAPTER.quest("mana_storage_cell_4k", q -> q
            .at(3.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_4K.get()))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest MANA_STORAGE_CELL_16K = CHAPTER.quest("mana_storage_cell_16k", q -> q
            .at(4.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_16K.get()))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest MANA_STORAGE_CELL_64K = CHAPTER.quest("mana_storage_cell_64k", q -> q
            .at(5.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_64K.get()))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest MANA_STORAGE_CELL_256K = CHAPTER.quest("mana_storage_cell_256k", q -> q
            .at(6.5, 8.5)
            .task(Task.item(ABItems.MANA_CELL_256K.get()))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest T1_ME_STORAGE_CORE = CHAPTER.quest("t1_me_storage_core", q -> q
            .at(7.5, 6.5)
            .task(Task.item(GTOBlocks.T1_ME_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_1M));

    public static final Quest T2_ME_STORAGE_CORE = CHAPTER.quest("t2_me_storage_core", q -> q
            .at(8.5, 6.5)
            .task(Task.item(GTOBlocks.T2_ME_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_4M));

    public static final Quest T3_ME_STORAGE_CORE = CHAPTER.quest("t3_me_storage_core", q -> q
            .at(9.5, 6.5)
            .task(Task.item(GTOBlocks.T3_ME_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_16M));

    public static final Quest T4_ME_STORAGE_CORE = CHAPTER.quest("t4_me_storage_core", q -> q
            .at(10.5, 6.5)
            .task(Task.item(GTOBlocks.T4_ME_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_64M));

    public static final Quest T5_ME_STORAGE_CORE = CHAPTER.quest("t5_me_storage_core", q -> q
            .at(11.5, 6.5)
            .task(Task.item(GTOBlocks.T5_ME_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_256M));

    public static final Quest Q_1K_CRAFTING_STORAGE = CHAPTER.quest("q_1k_crafting_storage", q -> q
            .at(2.5, 10.5)
            .task(Task.item(AEBlocks.CRAFTING_STORAGE_1K))
            .dependsOn(CELL_COMPONENT_1K));

    public static final Quest Q_16K_CRAFTING_STORAGE = CHAPTER.quest("q_16k_crafting_storage", q -> q
            .at(4.5, 10.5)
            .task(Task.item(AEBlocks.CRAFTING_STORAGE_16K))
            .dependsOn(CELL_COMPONENT_16K));

    public static final Quest Q_4K_CRAFTING_STORAGE = CHAPTER.quest("q_4k_crafting_storage", q -> q
            .at(3.5, 10.5)
            .task(Task.item(AEBlocks.CRAFTING_STORAGE_4K))
            .dependsOn(CELL_COMPONENT_4K));

    public static final Quest Q_64K_CRAFTING_STORAGE = CHAPTER.quest("q_64k_crafting_storage", q -> q
            .at(5.5, 10.5)
            .task(Task.item(AEBlocks.CRAFTING_STORAGE_64K))
            .dependsOn(CELL_COMPONENT_64K));

    public static final Quest Q_256K_CRAFTING_STORAGE = CHAPTER.quest("q_256k_crafting_storage", q -> q
            .at(6.5, 10.5)
            .task(Task.item(AEBlocks.CRAFTING_STORAGE_256K))
            .dependsOn(CELL_COMPONENT_256K));

    public static final Quest T1_CRAFTING_STORAGE_CORE = CHAPTER.quest("t1_crafting_storage_core", q -> q
            .at(7.5, 10.5)
            .task(Task.item(GTOBlocks.T1_CRAFTING_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_1M));

    public static final Quest T2_CRAFTING_STORAGE_CORE = CHAPTER.quest("t2_crafting_storage_core", q -> q
            .at(8.5, 10.5)
            .task(Task.item(GTOBlocks.T2_CRAFTING_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_4M));

    public static final Quest T3_CRAFTING_STORAGE_CORE = CHAPTER.quest("t3_crafting_storage_core", q -> q
            .at(9.5, 10.5)
            .task(Task.item(GTOBlocks.T3_CRAFTING_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_16M));

    public static final Quest T4_CRAFTING_STORAGE_CORE = CHAPTER.quest("t4_crafting_storage_core", q -> q
            .at(10.5, 10.5)
            .task(Task.item(GTOBlocks.T4_CRAFTING_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_64M));

    public static final Quest T5_CRAFTING_STORAGE_CORE = CHAPTER.quest("t5_crafting_storage_core", q -> q
            .at(11.5, 10.5)
            .task(Task.item(GTOBlocks.T5_CRAFTING_STORAGE_CORE))
            .dependsOn(CELL_COMPONENT_256M));

    public static final Quest INFINITY_CELL = CHAPTER.quest("infinity_cell", q -> q
            .at(8.5, 7.5)
            .task(Task.item(EPPItemAndBlock.INFINITY_CELL))
            .dependsOn(FLUID_STORAGE_CELL_256K));

    public static final Quest INFINITY_CELL_2 = CHAPTER.quest("infinity_cell_2", q -> q
            .at(7.5, 7.5)
            .task(Task.item(EPPItemAndBlock.INFINITY_CELL))
            .dependsOn(CELL_COMPONENT_1M));

    public static final Quest CELL_WORKBENCH = CHAPTER.quest("cell_workbench", q -> q
            .at(1.5, 5.5)
            .desc(d -> d
                    .line("AE模组经过GTOCore大量修改，字节限制与原版稍有不同，{无类型数量限制，也无类型字节占用}", YELLOW)
                    .line()
                    .line("字节的计算与原版相同，即可存储物品/流体数量为字节数*8"))
            .task(Task.item(AEBlocks.CELL_WORKBENCH))
            .dependsOn(LOGIC_PROCESSOR)
            .hideDependencyLines());

    public static final Quest TAG_STORAGE_BUS = CHAPTER.quest("tag_storage_bus", q -> q
            .at(-15, 21.5)
            .desc(d -> d
                    .line("多用于矿物处理产线设计、热锭的自动输出")
                    .line()
                    .line("{配合库存输入总线搭建子网有奇效}", GRAY.italic()))
            .task(Task.item(EPPItemAndBlock.TAG_STORAGE_BUS))
            .dependsOn(STORAGE_BUS));

    public static final Quest EXPORT_BUS = CHAPTER.quest("export_bus", q -> q
            .at(-9, 15.5)
            .subtitle("ME网络的交互节点")
            .desc(d -> d
                    .line("将ME网络中的物品主动输出至贴着的方块，没有过滤时不输出任何东西")
                    .line()
                    .line("经修改，单次输出上限非常高"))
            .task(Task.item(AEParts.EXPORT_BUS))
            .dependsOn(FLUIX_GLASS_CABLE, ANNIHILATION_CORE));

    public static final Quest THRESHOLD_EXPORT_BUS = CHAPTER.quest("threshold_export_bus", q -> q
            .at(-5, 15.5)
            .task(Task.item(EPPItemAndBlock.THRESHOLD_EXPORT_BUS))
            .dependsOn(EXPORT_BUS, LEVEL_EMITTER)
            .hideDependencyLines());

    public static final Quest MOD_EXPORT_BUS = CHAPTER.quest("mod_export_bus", q -> q
            .at(-3, 15.5)
            .task(Task.item(EPPItemAndBlock.MOD_EXPORT_BUS))
            .dependsOn(EXPORT_BUS));

    public static final Quest EX_EXPORT_BUS_PART = CHAPTER.quest("ex_export_bus_part", q -> q
            .at(-7, 15.5)
            .task(Task.item(EPPItemAndBlock.EX_EXPORT_BUS))
            .dependsOn(EXPORT_BUS));

    public static final Quest TAG_EXPORT_BUS = CHAPTER.quest("tag_export_bus", q -> q
            .at(-1, 15.5)
            .task(Task.item(EPPItemAndBlock.TAG_EXPORT_BUS))
            .dependsOn(EXPORT_BUS));

    public static final Quest MOD_STORAGE_BUS = CHAPTER.quest("mod_storage_bus", q -> q
            .at(-15, 23.5)
            .task(Task.item(EPPItemAndBlock.MOD_STORAGE_BUS))
            .dependsOn(STORAGE_BUS));

    public static final Quest EX_CHARGER = CHAPTER.quest("ex_charger", q -> q
            .at(-17, 19.5)
            .desc(d -> d.line("我们有{电弧发生器}，真的还需要这玩意吗？", GOLD))
            .task(Task.item(EPPItemAndBlock.EX_CHARGER))
            .dependsOn(STORAGE_BUS, CHARGER)
            .optional());

    public static final Quest EXCHANGE_STORAGE_MONITOR = CHAPTER.quest("exchange_storage_monitor", q -> q
            .at(-9, 21.5)
            .task(Task.item(GTOAEParts.EXCHANGE_STORAGE_MONITOR.get()))
            .dependsOn(STORAGE_MONITOR));

    public static final Quest CRAFTING_MONITOR = CHAPTER.quest("crafting_monitor", q -> q
            .at(2.5, 11.5)
            .task(Task.item(AEBlocks.CRAFTING_MONITOR))
            .dependsOn(THE_SECOND_PIECE_OF_THE_AUTO, STORAGE_MONITOR)
            .hideDependencyLines());

    public static final Quest INGREDIENT_BUFFER = CHAPTER.quest("ingredient_buffer", q -> q
            .at(3.5, 11.5)
            .task(Task.item(EPPItemAndBlock.INGREDIENT_BUFFER))
            .dependsOn(CELL_COMPONENT_1K)
            .hideDependencyLines());

    public static final Quest CANER = CHAPTER.quest("caner", q -> q
            .at(-17, 23.5)
            .task(Task.item(EPPItemAndBlock.CANER))
            .dependsOn(IMPORT_BUS, EXPORT_BUS, INGREDIENT_BUFFER)
            .optional().hideDependencyLines());

    public static final Quest WORK_BLOCK_FOR_ORIGINAL_SYNTHESIS_RECIPES = CHAPTER.quest("work_block_for_original_synthesis_recipes", q -> q
            .at(-7, 1.5)
            .icon(AEBlocks.MOLECULAR_ASSEMBLER)
            .title("原版合成配方的工作方块")
            .subtitle("记得插加速卡")
            .desc(d -> d
                    .line("配合{样板供应器}进行自动化合成，虽然在{MV}阶段你就可以制造它，但你至少需要达到{HV}阶段才可以建立自动合成系统", GOLD, MV, HV)
                    .line()
                    .line("它也是{大型蒸汽电路组装机}的原材料之一", GOLD))
            .task(Task.item(AEBlocks.MOLECULAR_ASSEMBLER))
            .dependsOn(ANNIHILATION_CORE, MvChapter.AE_CIRCUIT_BOARD));

    public static final Quest ASSEMBLY_MATRIX = CHAPTER.quest("assembly_matrix", q -> q
            .at(-3, 1.5)
            .icon(EPPItemAndBlock.ASSEMBLER_MATRIX_PATTERN)
            .title("装配矩阵")
            .subtitle("分子装配室是你吗？")
            .desc(d -> d
                    .line("{Credit:MC百科}", YELLOW)
                    .line()
                    .line("每个{装配矩阵样板核心}都可以容纳{36}个{合成样板}，相当于{4}个{ME样板供应器}或{1}个{ME扩展样板供应器}", GOLD, DARK_GREEN, GOLD, DARK_GREEN, GOLD, DARK_GREEN, GOLD)
                    .line("每个{装配矩阵合成核心}都可以同时执行{8}个{合成任务}，大约相当于{16}个{分子装配室}或{1}个{扩展分子装配室}", GOLD, DARK_GREEN, GOLD, DARK_GREEN, GOLD, DARK_GREEN, GOLD)
                    .line("每个{装配矩阵速度核心}都可以提供类似于加速卡的效果，{最多生效5个}", GOLD, YELLOW)
                    .line()
                    .line("外层：")
                    .line("{装配矩阵框架} ：用于搭建装配矩阵的每一条棱，框定了装配矩阵的大小，默认外径最大为 7×7×7", GOLD)
                    .line("{装配矩阵墙壁/装配矩阵玻璃} ：用于搭建装配矩阵的每一个面，将面上填满即可", GOLD)
                    .line()
                    .line("内部：")
                    .line("{装配矩阵样板核心} ：{至少安装1个}，用于为装配矩阵提供样板容量，每个可以容纳36个合成样板", GOLD, RED)
                    .line("{装配矩阵合成核心} ：{至少安装1个}，用于让装配矩阵进行合成，每个可以同时执行8个合成任务", GOLD, RED)
                    .line("{装配矩阵速度核心} ： {最多生效5个}，用于为装配矩阵加速", GOLD, RED))
            .task(
                    Task.item(EPPItemAndBlock.ASSEMBLER_MATRIX_PATTERN),
                    Task.item(EPPItemAndBlock.ASSEMBLER_MATRIX_CRAFTER),
                    Task.item(EPPItemAndBlock.ASSEMBLER_MATRIX_SPEED),
                    Task.item(EPPItemAndBlock.ASSEMBLER_MATRIX_WALL),
                    Task.item(EPPItemAndBlock.ASSEMBLER_MATRIX_FRAME))
            .dependsOn(WORK_BLOCK_FOR_ORIGINAL_SYNTHESIS_RECIPES, HvChapter.MICROPROCESSOR_SUPERCOMPUTER));

    public static final Quest SUPER_MOLECULAR_ASSEMBLER = CHAPTER.quest("super_molecular_assembler", q -> q
            .at(-1, 1.5)
            .icon(MultiBlockG.SUPER_MOLECULAR_ASSEMBLER)
            .subtitle("比装配矩阵更快，性能更好")
            .desc(d -> d
                    .line("自动按发配数量翻倍样板，因此无需并行单元")
                    .line("直接消耗输入，提取输出到机器，没有模拟合成工序"))
            .task(Task.item(MultiBlockG.SUPER_MOLECULAR_ASSEMBLER), Task.item(GTAEMachines.ME_CRAFT_PATTERN_PART_MACHINE))
            .dependsOn(ZpmChapter.WELCOME_TO_THE_ZERO_PRESSURE_PHASE, ZpmChapter.ZPM_FORCE_FIELD_GENERATOR_LAUNCHER_AND, ASSEMBLY_MATRIX));

    public static final Quest EX_MOLECULAR_ASSEMBLER = CHAPTER.quest("ex_molecular_assembler", q -> q
            .at(-5, 1.5)
            .desc(d -> d.line("分子装配室的高级版本"))
            .task(Task.item(EPPItemAndBlock.EX_ASSEMBLER))
            .dependsOn(WORK_BLOCK_FOR_ORIGINAL_SYNTHESIS_RECIPES));

    public static final Quest MEMORY_CARD = CHAPTER.quest("memory_card", q -> q
            .at(-21, 1.5)
            .desc(d -> d
                    .line("用于复制粘贴各种接口的配置")
                    .line()
                    .line("还可以用来复制样板供应器内的样板到另一个样板供应器里，但物品栏中要有足够数量的空白样板"))
            .task(Task.item(AEItems.MEMORY_CARD))
            .dependsOn(LOGIC_PROCESSOR)
            .hideDependencyLines());

    public static final Quest ME_IO_STORAGE_ACCESS_HATCH = CHAPTER.quest("me_io_storage_access_hatch", q -> q
            .at(-1, 13.5)
            .icon(GTAEMachines.ME_IO_STORAGE_ACCESS_HATCH)
            .desc(d -> d.line("存储器专供版本，将物品从ME存储器导入或导出"))
            .task(Task.item(GTAEMachines.ME_IO_STORAGE_ACCESS_HATCH), Task.item(GTOItems.STORAGE_ACCESSOR_REPLACER2).optional())
            .dependsOn(EX_IO_PORT, ME_STORAGE));

    public static final Quest CONTROLLER = CHAPTER.quest("controller", q -> q
            .at(-11, 5.5)
            .icon(AEBlocks.CONTROLLER)
            .desc(d -> d.line("因为AE已经无限频道了，这个东西只是一个装饰品"))
            .task(Task.item(AEBlocks.CONTROLLER))
            .dependsOn(LOGIC_PROCESSOR)
            .optional().hideUntilDepsVisible());

    public static final Quest REQUESTER_TERMINAL = CHAPTER.quest("requester_terminal", q -> q
            .at(-1, 17.5)
            .icon(Registration.TERMINAL)
            .desc(d -> d.line("管理连接至ME网络的ME请求器，附带搜索功能"))
            .task(Task.item(Registration.TERMINAL))
            .dependsOn(REQUESTER, SEMI_DARK_MONITOR));

    public static final Quest FORMATION_PLANE = CHAPTER.quest("formation_plane", q -> q
            .at(-13, 21.5)
            .desc(d -> d
                    .line("{ME成型面板}会将正在输入ME网络的物品以方块的形式摆放到面前", GOLD)
                    .line()
                    .line("{ME破坏面板}会将面前的方块破坏并输入到ME网络", GOLD)
                    .line()
                    .line("{一个经典应用场合是在方块转换室中达成自动化}", GRAY.italic()))
            .task(Task.item(AEParts.FORMATION_PLANE), Task.item(AEParts.ANNIHILATION_PLANE))
            .dependsOn(FLUIX_CRYSTAL, ANNIHILATION_CORE)
            .hideDependencyLines());

    public static final Quest EX_PATTERN_PROVIDER = CHAPTER.quest("ex_pattern_provider", q -> q
            .at(-5, 5.5)
            .task(Task.item(EPPItemAndBlock.EX_PATTERN_PROVIDER))
            .dependsOn(THE_FIRST_PIECE_OF_THE_AUTO));

    public static final Quest ME_PATTERN_BUFFER = CHAPTER.quest("me_pattern_buffer", q -> q
            .at(-3, 5.5)
            .icon(GTAEMachines.ME_PATTERN_BUFFER)
            .subtitle("更好的下单")
            .desc(d -> d
                    .line("支持{样板之间的完全隔离}，妈妈再也不用担心我化反串配方了！", YELLOW.bold())
                    .line()
                    .line("有特殊的优化，配方处理性能比普通的仓更好：")
                    .line()
                    .line("- 优化的发配机制，按发配数量自动翻倍内部样板，实现一次性全部发完，大幅降低发配的性能消耗")
                    .line("- 优化的配方处理机制，使用原生类型，没有二次转换")
                    .line("- 缓存样板对应的配方，下次查找时直接调用这个配方，而不是重新搜索")
                    .line()
                    .line("若无特殊说明，样板总成可以独立于机器设置配方类型，从而实现一台机器运行多种配方类型的配方")
                    .line()
                    .line("另外，样板总成被破坏后能够保留其内部所有样板！")
                    .line()
                    .line("注意样板总成和其他的输入仓室之间默认{开启隔离}，你需要{喷漆罐}来手动解除隔离", YELLOW, GOLD))
            .task(Task.item(GTAEMachines.ME_PATTERN_BUFFER))
            .dependsOn(EX_PATTERN_PROVIDER, LuvChapter.EUROPIUM_INGOT));

    public static final Quest ME_CATALYST_PATTERN_BUFFER = CHAPTER.quest("me_catalyst_pattern_buffer", q -> q
            .at(-1, 7.5)
            .desc(d -> d
                    .line("不会消耗催化剂的耐久")
                    .line()
                    .line("这个总成还支持你把催化剂直接作为{虚拟物品}编入{样板}中（终于！）", AQUA, GOLD))
            .task(Task.item(GTAEMachines.ME_CATALYST_ME_PATTERN_BUFFER))
            .dependsOn(ME_PATTERN_BUFFER, UvChapter.UV_FORCE_FIELD_GENERATOR_LAUNCHER_AND));

    public static final Quest ME_EXTEND_PATTERN_BUFFER = CHAPTER.quest("me_extend_pattern_buffer", q -> q
            .at(-3, 7.5)
            .icon(GTAEMachines.ME_EXTEND_PATTERN_BUFFER)
            .desc(d -> d.line("三种样板总成都有其对应的升级器"))
            .task(Task.item(GTAEMachines.ME_EXTEND_PATTERN_BUFFER), Task.item(GTOItems.PATTERN_BUFFER_UPGRADER1).optional())
            .dependsOn(ME_PATTERN_BUFFER, UvChapter.WELCOME_TO_THE_EXTREME_PRESSURE_STAGE));

    public static final Quest ME_EXTEND_PATTERN_BUFFER_ULTRA = CHAPTER.quest("me_extend_pattern_buffer_ultra", q -> q
            .at(-3, 9.5)
            .subtitle("样板总成再次升级！")
            .task(Task.item(GTAEMachines.ME_EXTEND_PATTERN_BUFFER_ULTRA))
            .dependsOn(ME_EXTEND_PATTERN_BUFFER, UhvChapter.WELCOME_TO_THE_EXTREME_HIGH_VOLTAGE));

    public static final Quest ME_PATTERN_BUFFER_PROXY = CHAPTER.quest("me_pattern_buffer_proxy", q -> q
            .at(-1, 5.5)
            .desc(d -> d.line("和样板总成配合使用，与样板总成绑定后被视作同一个方块，下单发配的时候会同时发配到总成和镜像中"))
            .task(Task.item(GTAEMachines.ME_PATTERN_BUFFER_PROXY))
            .dependsOn(EX_PATTERN_PROVIDER, LuvChapter.HPCA_ADVANCED_COMPUTATION_COMPONENT));

    public static final Quest WIRELESS_FACILITY_MANAGEMENT_TERMINAL = CHAPTER.quest("wireless_facility_management_terminal", q -> q
            .at(-1, 23.5)
            .task(Task.item(GTOItems.WIRELESS_WFT)));

    public static final Quest WIRELESS_REQUESTER_TERMINAL = CHAPTER.quest("wireless_requester_terminal", q -> q
            .at(-1, 19.5)
            .task(Task.item(GTOItems.WIRELESS_WRT)));

    public static final Quest WIRELESS_UNIVERSAL_TERMINAL = CHAPTER.quest("wireless_universal_terminal", q -> q
            .at(1.5, 21.5)
            .desc(d -> d
                    .line("将两个无线终端拼合在一起获得")
                    .line()
                    .line("通用终端可以继续拼接其余的无线终端")
                    .line()
                    .line("开启GTOCore设置中的{“AE 终端页面切换样式重做”}选项后，“切换到下一个终端”按钮将变为展开一个候选终端列表，玩家需要手动选择跳转的终端", AQUA))
            .task(Task.item(AE2wtlib.UNIVERSAL_TERMINAL))
            .dependsOn(
                    WIRELESS_ME2IN1_TERMINAL,
                    WIRELESS_PATTERN_ACCESS_TERMINAL,
                    WIRELESS_PATTERN_ENCODING_TERMINAL,
                    WIRELESS_CRAFTING_TERMINAL,
                    WIRELESS_TERMINAL,
                    WIRELESS_EX_PAT,
                    WIRELESS_FACILITY_MANAGEMENT_TERMINAL,
                    WIRELESS_REQUESTER_TERMINAL)
            .requireAny()
            .requireCount(2)
            .hideDependencyLines());

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LV, 3.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_ULV, 2.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, 4.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, 5.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_EV, 6.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, 7.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, 8.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UV, 10.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_ZPM, 9.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UHV, 11.5, 5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -5.5, 9.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_EV, -7.5, 7.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_EV, -7.5, 9.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, 1, 10.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.MONITOR_AE_CPU, 7.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_AE_THROUGHPUT, 6.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_CUSTOM, 5.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_CWU, 4.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_ELECTRICITY, 2.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_MACHINE, 8.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.MONITOR_MANA, 3.5, 12.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, -9.5, 21.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -3.7, 5.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -1.7, 5.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UV, -3.5, 7.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UV, -1.5, 7.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UHV, -3.7, 9.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -7.5, 3.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -7.5, 5.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_ZPM, -1.7, 1.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_EV, 1, 12.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -19.5, 11.15, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, -9.5, 11.15, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.GUI_OVERLAY_OUT_SLOT_OVERLAY, -9.85, 15, 1, 1))
                .image(QuestImage.of(QuestTextures.GUI_OVERLAY_IN_SLOT_OVERLAY, -9.85, 13, 1, 1))
                .image(QuestImage.of(QuestTextures.COVER_OVERLAY_DISPLAY_EMISSIVE, -9.7, 16.8, 1, 1))
                .image(QuestImage.of(QuestTextures.OVERLAY_INVENTORY_CONFIGURATOR, -15.75, 17.3, 0.8, 0.8))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -1.5, 3.5, 1, 1))
                .image(QuestImage.of(QuestTextures.LOGO_APPIED_ENERGISTICS, -20.5, 4.5, 9.27, 3))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -5.7, 7.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -3.5, 1.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -3.5, 11.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LV, -9.5, 1, 2, 2).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -19.5, 19.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_EV, -19.5, 21.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -21.5, 9.5, 1, 1).after(UtilChapter.Q_056EC4E6))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -3.5, 3.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -5.7, 11.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, 0.7, 19.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -1.7, 13.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, -5.5, -0.5, 1, 1).after(UtilChapter.Q_056EC4E6))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, -1.7, 9.5, 1, 1).after(UtilChapter.Q_056EC4E6))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UHV, -1.7, 9.5, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -1.5, 11.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_MV, -9.5, 4.15, 1, 1).after(UtilChapter.NORMAL_EXPERT))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, 9.5, 19, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, 5.5, 19, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, 7.5, 19, 1, 1))
                .link(QuestLink.of(HvChapter.PROGRAMMABLE_CHAMBER_ROOM, -1, 3.5))
                .link(QuestLink.of(IvChapter.ADVANCED_TESSERACT_GENERATOR, -19, 21.5))
                .link(QuestLink.of(UtilChapter.INSCRIBER_EASY, -13, 5.5))
                .link(QuestLink.of(UtilChapter.ME_WILDCARD_PATTERN_BUFFER, -1, 9.5))
                .link(QuestLink.of(UtilChapter.ME_WILDCARD_PATTERN_BUFFER_2, -1, 9.5)));
    }
}
