package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestRewardTables;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.player.OrganTier;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.item.Items;

import appeng.core.definitions.AEItems;

import com.cazsius.solcarrot.item.SOLCarrotItems;
import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.naturescompass.NaturesCompass;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.hepdd.gtmthings.data.CustomItems;
import de.maxhenkel.easyvillagers.items.ModItems;
import dev.shadowsoffire.apotheosis.Apoth;

import static com.gtolib.api.quest.QuestStyle.*;

public final class TipsChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("tips", c -> c
            .title("提示")
            .subtitle("Tips and Tricks")
            .icon(Items.WRITABLE_BOOK)
            .hideQuestsUntilDepsVisible()
            .autofocus(TipsChapter.Q_79646E8E));

    public static final Quest Q_79646E8E = CHAPTER.quest("q_79646e8e", q -> q
            .at(0, -1)
            .icon(Items.WRITABLE_BOOK)
            .subtitle("我以为你从来都不会来看我呢...")
            .desc(d -> d
                    .line("常回来看看，这里收集了大部分关于游戏机制的技巧与提示！")
                    .line()
                    .line("围成一圈的是对{开始游戏非常有用的提示}，请务必在开始游玩整合包之前看一遍", YELLOW)
                    .line()
                    .line("右边单独列出的机制{随着游戏进程而解锁}，所以它们都是锁着的，你也可以现在就看一看", GOLD))
            .task(Task.checkmark().title("提示"))
            .reward(Reward.random(QuestRewardTables.ULV).excludeFromClaimAll()));

    public static final Quest CHUNK_LOADING = CHAPTER.quest("chunk_loading", q -> q
            .at(-3.75, -2.25)
            .icon(Items.CLOCK)
            .title("区块加载")
            .subtitle("裂变反应堆的一生之敌")
            .desc(d -> d
                    .line("{我的机器为什么在偷懒？}", DARK_GREEN)
                    .line()
                    .line("众所周知 Minecraft 有区块加载机制，当玩家离开区块一段时间之后区块就会暂停加载——那机器也就没法持续运行了")
                    .line()
                    .line("所以为了你的基地的正常运行，确保你的基地区块{全部认领}且{强制加载}", GOLD, RED)
                    .line()
                    .line("{点击翻页查看操作}", GRAY)
                    .line()
                    .page()
                    .line("按{[M]}打开地图，然后点击或者用右键框选你附近的区块，选择{[建立领地]}", YELLOW, YELLOW)
                    .line()
                    .line("再次框选你的认领区块，右键选择{[标记强制加载]}即可", YELLOW)
                    .line()
                    .line("{注意：不要认领离你太远的区块，这样做是无效的}", LIGHT_PURPLE))
            .task(Task.checkmark())
            .reward(Reward.item(Items.CLOCK))
            .dependsOn(Q_79646E8E)
            .hideDependencyLines(false));

    public static final Quest Q_567EBE02 = CHAPTER.quest("q_567ebe02", q -> q
            .at(-1.5, -4.5)
            .icon(earth.terrarium.adastra.common.registry.ModItems.GRAVITY_NORMALIZER.get())
            .subtitle("集大成者")
            .desc(d -> d
                    .line("将鼠标移动到你想要查询合成表/被合成的物品上")
                    .line()
                    .line("按下{R}或者{鼠标左键}查询合成表", DARK_RED, DARK_RED)
                    .line("{- 即此物品怎么才能获取}", YELLOW)
                    .line("{按下}{U}或者{鼠标右键}查询被合成表", YELLOW, DARK_RED, DARK_RED)
                    .line("{- 即此物品可以用来合成哪些物品}", YELLOW)
                    .page()
                    .line("打开界面后上面有两个带箭头的行")
                    .line()
                    .line("第一行和最上的图标一样可以切换合成工具")
                    .line("{- 点击这一行的文字可以查看这种方式的所有合成表}", YELLOW)
                    .line("{第二行可以翻页}", YELLOW)
                    .line("{- 很多物品有大量的合成方法，请仔细浏览，尤其是多方块机器的仓室摆放位置}", YELLOW))
            .task(Task.checkmark().title("EMI小课堂"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest FTB_UTIMINE = CHAPTER.quest("ftb_utimine", q -> q
            .at(3.75, -2.25).minWidth(240)
            .icon(Items.CHAIN)
            .title("FTB Utimine")
            .subtitle("我是打铁匠，这就是锁连")
            .desc(d -> d
                    .line("整合包加入了{FTBUtimine}，但出于平衡考虑，只有高级工具才有更多连锁数量。", AQUA)
                    .line()
                    .line("为了激活{FTBUtimine}，必须手持工具，连锁数量受到工具类型和等级加成：", AQUA)
                    .line()
                    .line("- {格雷基础工具}，基础数量是32，如{铁斧}，{钢镐}", GOLD, GOLD, GOLD)
                    .line("- 使用{金刚杵}、{采矿锤}或者{电钻}{采矿}时、使用{链锯}{伐木}时数量x4", GOLD, GOLD, GOLD, YELLOW, GOLD, YELLOW)
                    .line("- 如果使用电动工具如{电钻}、{链锯}，再{x2^电力等级}", GOLD, GOLD, YELLOW))
            .task(Task.checkmark().title("连锁"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(Q_79646E8E));

    public static final Quest WHAT_ARE_THESE_KEYS = CHAPTER.quest("what_are_these_keys", q -> q
            .at(3.75, 0.75).iconScale(2)
            .icon(earth.terrarium.adastra.common.registry.ModItems.GLACIAN_BUTTON.get())
            .title("这都是啥按键啊？")
            .subtitle("（真）寸步难行 ")
            .desc(d -> d
                    .line("你必须要检查一下你的快捷键绑定是否存在冲突")
                    .line()
                    .line("以下是几个最关键的{快捷键：}", DARK_RED)
                    .line()
                    .line("{默认}{[M]}打开地图", DARK_RED, YELLOW)
                    .line("按住{[~]}键可以启用连锁", YELLOW)
                    .line("默认{[TAB]}键快速打开任务", YELLOW)
                    .line("按住{[Z]}键调整HUD位置和人物属性", YELLOW))
            .task(Task.checkmark().title("这都是啥按键啊！"))
            .reward(Reward.item(Items.LEATHER_BOOTS))
            .dependsOn(Q_79646E8E));

    public static final Quest GT_TOOL_USER_MANUAL = CHAPTER.quest("gt_tool_user_manual", q -> q
            .at(-3.75, 0.75)
            .icon(ToolHelper.get(GTToolType.WRENCH, GTMaterials.Iron).getItem())
            .title("GT 工具使用手册")
            .subtitle("使用说明书")
            .desc(d -> d
                    .line("扳手：    设置GT机器的{输出口}，{旋转}机器，{拆卸}GT方块、{连接/断开}{管道}", YELLOW, YELLOW, YELLOW, YELLOW, GOLD)
                    .line("软锤：    {开启/关闭}机器", YELLOW)
                    .line("螺丝刀：设置机器{自动输出}，设置{允许从输出口输入}", YELLOW, YELLOW)
                    .line("撬棍：     {拆卸}{覆盖板}", YELLOW, GOLD)
                    .line("剪线钳： {连接/断开}{线缆}", YELLOW, GOLD)
                    .line("锻造锤：{静音}机器", YELLOW)
                    .page()
                    .line("{扳手使用手册：}", GOLD.bold())
                    .line("1. 手持{扳手}对准可使用{扳手}的方块时，方块各面会显示出{九宫格}：中心指向{该面}，四边指向{该面侧面}，四角指向{该面背面}", GOLD, GOLD, DARK_GREEN.underline(), DARK_GREEN, DARK_GREEN, DARK_GREEN)
                    .line("2. {右击}{九宫格}对应格，可将机器{旋转到该面}（仅有面区分的非GT方块）或将{输出口旋转至该面}（对于GT机器）；但如果右击的是无面区分的非GT机器，则{拆卸机器}", YELLOW, UNDERLINE, YELLOW, YELLOW, YELLOW)
                    .line("3. 当{潜行右击}九宫格对应格，将{正面旋转到该面}（对于所有有面区分的机器）；对于无面的区分的非GT机器，则{拆卸机器}", YELLOW, YELLOW, YELLOW)
                    .line("4. {右击}{管道}的九宫格对应格，{连接/断开}管道对应面；{潜行右击}控制{单向输入}。", YELLOW, GOLD, YELLOW, YELLOW, YELLOW)
                    .page()
                    .line("{软锤使用手册：}", GOLD.bold())
                    .line("1. {右击}可{开启/关闭}机器。蹲下右键机器可以让机器在当前配方运行完成之后自动停止。", YELLOW, YELLOW)
                    .line()
                    .line("{螺丝刀使用手册：}", GOLD.bold())
                    .line("1. {右击}机器输出口，调整是否{自动输出}。", YELLOW, YELLOW)
                    .line("2.  {潜行右击}机器输出口，调整是否{允许从输出口输入}", YELLOW, YELLOW)
                    .line()
                    .line("{撬棍使用手册：}", GOLD.bold())
                    .line("1. {右击}拆卸{覆盖板}。", YELLOW, GOLD)
                    .line()
                    .line("{剪线钳使用手册：}", GOLD.bold())
                    .line("1. {右击}{导线}九宫格，{连接/断开}线缆", YELLOW, GOLD, YELLOW)
                    .line()
                    .line("{锻造锤使用手册：}", GOLD.bold())
                    .line("1. {右击}{机器}，{静音}机器运行音效", YELLOW, GOLD, YELLOW))
            .task(Task.checkmark().title("GT 工具"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest DETECTION_DELAY = CHAPTER.quest("detection_delay", q -> q
            .at(-3.75, -0.75)
            .icon(TagPrefix.pipeHugeFluid, GTOMaterials.SpaceTime)
            .title("检测延迟")
            .desc(d -> d.line("许多周期性检测事件的检测周期得到了不同程度的延长，如管道与线缆的连接检测、多方块机器自动成型检测。这属于正常现象，这使得在超大量多方块的存档中依然保持良好的TPS"))
            .task(Task.checkmark())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest DIFFICULTY_AND_MODES = CHAPTER.quest("difficulty_and_modes", q -> q
            .at(3.75, -0.75)
            .icon(Items.COMPASS)
            .title("难度与模式")
            .subtitle("在一切的一切开始之前......")
            .desc(d -> d
                    .line("你可以在创建世界时更改难度，也可在mod配置里切换难度 （游戏菜单 > MOD > 搜索 GTOCore > 配置 > 游戏难度）。创建世界后你将{无法更改难度}！", RED)
                    .line()
                    .line("{简单难度}：{新来的看看这里！}简单模式专为新人设计：{更强的发电机，未经修改的原版/AE配方与更弱的怪物}，带来更轻松舒适的前期体验，助你完美完成入职计划！推荐给格雷科技或科技模组新手。", GREEN, LIGHT_PURPLE, YELLOW)
                    .line()
                    .line("{普通难度}：{正式员工来这里报到！}普通模式提供最原汁原味的格雷游戏体验： {正常的发电机和怪物，经过魔改的原版/AE配方}，享受标准流畅的游戏流程。适合有格雷经验或熟练的科技包玩家，希望你顺利完成阶段考核！", AQUA, LIGHT_PURPLE, YELLOW)
                    .line()
                    .line("{专家难度}：{仅限老员工前来报到！}专家模式开启更具挑战的征程：{更难的AE，更弱的发电机与产量更低的配方}，充满“制作组诚意”的困难开局等你应战。仅推荐给本包通关玩家或资深挑战者——{选择此难度前请三思而后行！}", RED, LIGHT_PURPLE, YELLOW, RED)
                    .line()
                    .line("{请注意，本包为}{格雷专家包}{——即使是简单难度，其挑战性依然高于多数科技整合包。}", YELLOW, GOLD.bold(), YELLOW)
                    .page()
                    .line("难度的影响包括但不限于")
                    .line()
                    .line("·不同复杂度的合成配方")
                    .line("·部分机器和仓室的数值调整")
                    .line("·发电机和能量单元的数值调整")
                    .line("·AE频道限制")
                    .line("·不同的任务指引和任务奖励")
                    .line("·不同强度的怪物")
                    .line()
                    .line("{详细特性请查看WIKI：}{简单、普通、专家模式特性指南}", YELLOW, url("https://gtodyssey.com/zh-hans/wiki/简单普通专家模式特性指南/"))
                    .line()
                    .line("{如果访问不了WIKI也可前往}{内置指南}", GRAY.italic(), link(TipsChapter.EASY_NORMAL_EXPERT_MODE_FEATURE_GUIDE))
                    .line()
                    .line("{强烈建议在更改难度后，重启MC，然后再创建新的世界！}", YELLOW)
                    .line()
                    .line("{这个任务完成不了？点左上角小箭头查看前置！}", GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(Q_79646E8E, GregtechOdysseyChapter.Q_19EFE14C));

    public static final Quest SELF_DISCIPLINE_MODE = CHAPTER.quest("self_discipline_mode", q -> q
            .at(3.75, 2.25)
            .icon(AEItems.ITEM_CELL_CREATIVE)
            .title("自我约束模式")
            .desc(d -> d
                    .line("如果你想挑战不通过任何作弊手段通关{GregTech Odyssey}，你可以在任何时候通过{GTOCore}的模组配置打开{自我约束模式}，一旦开启，你将永远不能使用作弊指令，以及从EMI作弊获取任何物品！", DARK_RED, GOLD, GOLD)
                    .line()
                    .line("{你将永远处于该模式！}", YELLOW)
                    .line("{你将永远处于该模式！}", YELLOW)
                    .line("{你将永远处于该模式！}", YELLOW)
                    .line()
                    .line("{点击这个任务的检查标记并不会开启自我约束模式}", GRAY.italic()))
            .task(Task.checkmark())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest ORES_AND_VEINS = CHAPTER.quest("ores_and_veins", q -> q
            .at(3, -3.75)
            .icon(GTMaterialBlocks.SURFACE_ROCK_BLOCKS.get(GTMaterials.Gold))
            .title("矿石与矿脉")
            .subtitle("告诉我他们在哪？")
            .desc(d -> d
                    .line("{地表岩石}很好地指示了地下的矿脉，在开局你可以利用这个特点寻找你需要的矿脉。如果你不知道它长什么样，你可以看看任务图标，这是一个{地表岩石（金）}", GOLD, GOLD)
                    .line()
                    .line("并且，{GregTech}添加了对{Xaero's WorldMap}的矿脉查看支持，打开{世界地图}点击右下角的{显示GT矿脉}和{显示基岩流体矿脉}按钮即可查看你已经发现的矿脉", AQUA, AQUA, YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("当然，我更推荐你使用{探矿仪}来寻找矿石。去{商店}页面换一个吧", GOLD, AQUA)
                    .line()
                    .line("前往{矿石图鉴}章节查看你需要的矿石资源", link(OresChapter.GOOD_AT_USING_EMI_TO_CHECK)))
            .task(Task.checkmark().title("我爱挖矿！"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(Q_79646E8E));

    public static final Quest EASY_NORMAL_EXPERT_MODE_FEATURE_GUIDE = CHAPTER.quest("easy_normal_expert_mode_feature_guide", q -> q
            .at(-3, -3.75)
            .iconStack(() -> QuestExternalRefs.astroduxBook())
            .title("简单、普通、专家模式特性指南")
            .subtitle("{难度不影响游戏流程或内容，只影响挑战性！}", RED)
            .desc(d -> d
                    .line("{简单模式 (Easy)}", AQUA)
                    .line()
                    .line("- AE2 使用原始配方（无魔改）")
                    .line("- ME 网络无限频道")
                    .line("- 增幅蒸汽 / 发电产能，提升发电阵列机器容量 / 涡轮产出")
                    .line("- 负荷调节维护仓上下限提升")
                    .line("- 无需超净间")
                    .line("- 部分配方产量提升 / 简化")
                    .line("- （MV 阶段）ME 简易样板总成：含 9 个样板槽的样板总成")
                    .line("- 装配线无需有序输入")
                    .line("- 基岩矿脉 / 基岩流体矿脉储量无限")
                    .line("- 并行控制仓 / 加速仓等数值提升")
                    .line("- 蒸汽大机器的并行数值提升")
                    .line("- 连锁采掘的最大可挖掘数量提升")
                    .line("- 任务书大量物资补给")
                    .line("- LV阶段即可进入超平坦/虚空维度")
                    .page()
                    .line("{普通模式 (Normal)}", AQUA)
                    .line()
                    .line("- AE2 全面魔改（MV 阶段初步解锁，HV 阶段开启自动化）")
                    .line("- ME 网络无限频道")
                    .line("- 怪物增强（更高血量、更强攻击力，请小心应对）")
                    .line("- 装配线需要有序输入物品才能工作")
                    .page()
                    .line("{专家模式 (Expert)}", AQUA)
                    .line()
                    .line("- 更难的 {AE}2 魔改（{MV} 阶段初步解锁，{EV} 阶段开启自动化）", GOLD, MV, EV)
                    .line("- 怪物增强（更高血量、更强攻击力，请小心应对）")
                    .line("- 蒸汽 / 发电产能降低")
                    .line("- 机器淋雨会爆炸")
                    .line("- 配方产出降低 / 复杂化")
                    .line("- 维护仓上下限降低")
                    .line("- 更高的无线电损耗")
                    .line("- 机器运行时能量不足将导致配方失败且输入被清空")
                    .line("- 装配线需要有序输入物品与流体才能工作")
                    .line("- 并行控制仓 / 加速仓等数值降低")
                    .line("- 发电设备在输出仓满时将继续发电并销毁多余能量")
                    .line("- 机器所安装的消声仓等级需与电压等级相同或相差±1级，否则将无法运行")
                    .line("- 涡轮有额外的高速倍率模式调节，允许以更高的转子损坏速度换取更高的运行速度"))
            .task(Task.checkmark())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest BUILDING_TIPS = CHAPTER.quest("building_tips", q -> q
            .at(-3.75, 2.25)
            .icon(QuestExternalRefs.ConstructionWandItems.IRON_WAND)
            .title("建造提示")
            .subtitle("Effortless Building")
            .desc(d -> d
                    .line("无论你想建造球形、圆形、柱体或者其他任何你想要的结构，按下{[Alt]}并选择对应形状，注意不要一次建造过大的结构，否则会造成FPS灾难", YELLOW)
                    .line()
                    .line("{建筑手杖}和{建筑小帮手}同样可用", GOLD, GOLD)
                    .line()
                    .line("本包内添加的{工业平台展开工具}通过消耗工业组件，也可快速建造预设平台", GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(QuestExternalRefs.ConstructionWandItems.STONE_WAND).optional(),
                    Task.item(Registration.Building_Gadget.get()).optional(),
                    Task.item(GTOMachines.INDUSTRIAL_PLATFORM_DEPLOYMENT_TOOLS).optional(),
                    Task.item(GTOItems.INDUSTRIAL_COMPONENTS[0][0]).optional())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest Q_113BFE54 = CHAPTER.quest("q_113bfe54", q -> q
            .at(1.5, -4.5)
            .iconStack(() -> QuestExternalRefs.healingWine())
            .desc(d -> d
                    .line("如果你将游戏托管在一台服务器上，那么你可能需要关注{OPAC(Open Parties  and Claims)}{玩家离线强制加载规则}", AQUA, GOLD)
                    .line()
                    .line("在当前默认设置下，服务器仅有玩家在线时会继续加载{已标记强制加载的区块}", YELLOW)
                    .line()
                    .line("具体设置在{服务器存档}{serverconfig}目录下的{openpartiesandclaims-default-player-config.toml}，你可以将{offlineForceload}设置为{true}来开启{离线强制加载}", YELLOW, AQUA, AQUA, YELLOW, DARK_GREEN, YELLOW))
            .task(Task.checkmark().title("服务器玩家须知"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest WE_NEED_TO_COMMUNICATE = CHAPTER.quest("we_need_to_communicate", q -> q
            .at(0, -5.25)
            .icon(GTOItems.INTEGRATED_CONTROL_CORE_MAX)
            .title("我们需要交流！")
            .subtitle("群U永远是你最坚实的后盾")
            .desc(d -> d
                    .line("QQ: GTO寰宇重工集团·Alpha部门：927923997（已满）")
                    .line()
                    .line("QQ: GTO寰宇重工集团·Beta部门：532385262（已满）")
                    .line()
                    .line("QQ: GTO寰宇重工集团·Gamma部门：1091812286")
                    .line()
                    .line("Discord: {https://discord.gg/PxruFSbtCn}", url("https://discord.gg/PxruFSbtCn")))
            .task(Task.checkmark().title("这当然是官方群"))
            .reward(Reward.item(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.ULV]))
            .dependsOn(Q_79646E8E));

    public static final Quest TANGIBLE_SMALL_BOARD = CHAPTER.quest("tangible_small_board", q -> q
            .at(2.25, 2.25)
            .icon(GTOItems.WIRELESS_CHARGER_COVER)
            .title("有形的小板")
            .subtitle("新鲜事简单报")
            .desc(d -> d
                    .line("有几种覆盖板是GTO新增加的，在这里一一介绍一下：")
                    .line()
                    .line(" {       无线传输覆盖板}：分为{无线物品传输覆盖板}和{无线流体覆盖板}及其{进阶版}；", AQUA, GOLD, GOLD, LIGHT_PURPLE)
                    .line("         用法：主手拿着{覆盖板}对任一容器如{超级箱}，{超级缸}，{ME接口}等，蹲下右键绑定，随后将{覆盖板}贴到{单方块机器的输出口}或{多方块机器的输出仓室}，即可无线传输物品/流体；", YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW, YELLOW)
                    .line("        特点：可进行{跨维度传输}；进阶版可以安装{过滤卡};", LIGHT_PURPLE, YELLOW)
                    .page()
                    .line(" {       无线充能覆盖板：}", AQUA)
                    .line("        用法：在搭建{无线充能器}结构后，在主机中放置{HV及以上}等级的{力场发生器}，可以给{无线充能覆盖板}所贴方块内的物品充电(如给{原木拟生场}中的{链锯}充电)；", GOLD, YELLOW, GOLD, YELLOW, YELLOW, YELLOW)
                    .line("        特点：可以安装在{板条箱}和{部分}机器主机上；", YELLOW, YELLOW)
                    .page()
                    .line(" {       可编程覆盖板：}", AQUA)
                    .line("        用法：在{单方块机器}上安装{可编程覆盖板}，就可在发配样板物品时为机器发送{虚拟物品};", LIGHT_PURPLE, GOLD, YELLOW)
                    .line("        特点：使得只需{单台}单方块机器即可运行不同电路或者模具/模头的配方;", RED)
                    .page()
                    .line(" {       存储覆盖板：}", AQUA)
                    .line("        用法：在{单方块机器}上安装即可；", DARK_RED)
                    .line("        特点：为{单台}单方块机器额外提供在GUI内可以打开的18格存储空间，物品{不可堆叠}；", RED, DARK_RED)
                    .page()
                    .line(" {       机器控制覆盖板：}", AQUA)
                    .line("        用法：在{单方块机器}，{多方块机器主机}或{输入仓室}任意一面安装即可；通过设置{红石信号}和普通/反相模式{控制机器开关}；", YELLOW, YELLOW, YELLOW, DARK_RED, AQUA)
                    .line("        特点：若安装的方块上还有{电动泵}，{传送带}等物流覆盖板，则{机器控制覆盖板}可以设置为对{物流覆盖板}进行控制；{机器控制覆盖板}可被强/弱充能方块激活；", YELLOW, YELLOW, GOLD, YELLOW, GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(CustomItems.WIRELESS_ITEM_TRANSFER_COVER).optional(),
                    Task.item(CustomItems.WIRELESS_FLUID_TRANSFER_COVER).optional(),
                    Task.item(GTOItems.WIRELESS_CHARGER_COVER).optional(),
                    Task.item(CustomItems.PROGRAMMABLE_COVER).optional(),
                    Task.item(GTItems.COVER_STORAGE).optional(),
                    Task.item(GTItems.COVER_MACHINE_CONTROLLER).optional())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest EMI_MULTIBLOCK_SPECIAL = CHAPTER.quest("emi_multiblock_special", q -> q
            .at(-2, -6)
            .icon(earth.terrarium.adastra.common.registry.ModItems.ETRIONIC_BLAST_FURNACE.get())
            .title("EMI多方块专题")
            .desc(d -> d
                    .line("在对某些方块（通常是主机）查看用途的时候,你会发现一个特殊页面——{\"多方块结构信息\"}页。", YELLOW)
                    .line()
                    .line("这是专门用来查看该方块可搭建的多方块结构使用的，其右侧有两个按钮，P 和 ALL 。")
                    .line()
                    .line("- {P}用于切换多方块不同的形态，默认显示第0形态即{P: 0}。通常有{可变长度}的多方块的P1会是其{最长形态}，拥有{附属模块}的多方块的P1是其{附属模块}。", AQUA, AQUA, YELLOW, GOLD, YELLOW, GOLD)
                    .line()
                    .line("- {ALL}用于切换显示层数，默认状态为全部显示。点击切换为{L: X}显示第X层的结构。", AQUA, AQUA)
                    .line()
                    .line("- {H}用于显示多方块中可以被替换的方块。变为绿色的方块即为可替换方块。", AQUA)
                    .page()
                    .line("直接{点击，拖曳，放大/缩小}可查看多方块的整体外观，{选中特定方块}后在右上角查看该位置可以放置什么方块，某个方块最少/最多摆放的数量。{点击右上角列表中的方块可跳转查看可以放置的方块列表。}{在你多方块不成型却又没有缺失方块的时候请优先检查方块数量限制！}", YELLOW, YELLOW, YELLOW, RED)
                    .line()
                    .line("在{样板编码终端}中打开此页的时候，你会发现右下角多了一个{加号}，用于将多方块的所有方块编写到一个样板中。点击{P}可编写不同的形态。", GOLD, AQUA, AQUA)
                    .line()
                    .line("想要查看{加速仓/并行控制仓}等仓室会被什么多方块机器使用,也是对这些仓室查看用途", GOLD))
            .task(Task.checkmark())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_567EBE02, SteamChapter.COKE_OVEN));

    public static final Quest CHARACTERISTICS_OF_WIRES = CHAPTER.quest("characteristics_of_wires", q -> q
            .at(7, -7.5)
            .icon(TagPrefix.wireGtSingle, GTOMaterials.ConductiveAlloy)
            .title("导线的特性")
            .subtitle("我的导线在漏电?")
            .desc(d -> d
                    .line("{导线/线缆}是你在前期传输电的{主要手段}。", GOLD, YELLOW)
                    .line()
                    .line("{未经任何处理}的{导线}会有较大的{线损}（而且会电人），当你拉的线越{长}，传递到远端损耗的电量也会越{多}。当线拉的{过长}时，电压就有可能{过低}，导致远端的机器无法获得足够的电量来工作。", YELLOW, GOLD, RED, AQUA, YELLOW, RED, RED)
                    .line()
                    .line("给导线{过胶}可显著减少线损。但最推荐的永远是{超导体}——{无需过胶，不会漏电，且不会电人}", YELLOW, GOLD, YELLOW)
                    .line()
                    .line("{不要让导线承担超过标定的电压和电流！}", GRAY.italic()))
            .task(
                    Task.checkmark(),
                    Task.item(TagPrefix.wireGtSingle, GTMaterials.Copper).optional(),
                    Task.item(TagPrefix.cableGtSingle, GTMaterials.Copper).optional(),
                    Task.item(TagPrefix.wireGtSingle, GTOMaterials.EnergeticAlloy).optional())
            .dependsOn(Q_79646E8E, SteamChapter.YOU_CAN_STICK_WIRES)
            .requireAny()
            .hideDependencyLines());

    public static final Quest MEANS_OF_ACCELERATION = CHAPTER.quest("means_of_acceleration", q -> q
            .at(7, -0.5)
            .icon(GTMachines.WORLD_ACCELERATOR[GTValues.UV])
            .title("提速的手段")
            .subtitle("机器太慢，人生太短")
            .desc(d -> d
                    .line("GTO为了提升机器工作的速度加入了很多{提速手段}。", YELLOW)
                    .line()
                    .line("这一页会随着你的{游戏进程}不断解锁新{加速方式}，点击{检查标记}以解锁第一个", AQUA, YELLOW, GOLD)
                    .line()
                    .line("当你感觉机器{太慢}的时候，不妨看看这里", RED))
            .task(Task.checkmark())
            .dependsOn(Q_79646E8E)
            .hideDependencyLines());

    public static final Quest OVERCLOCKING = CHAPTER.quest("overclocking", q -> q
            .at(9, -2)
            .icon(GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.EV])
            .title("超频！")
            .subtitle("耗能=功率x耗时 功率=电压x电流")
            .desc(d -> d
                    .line("超频，最底层的提速手段。通过提升输入的功率，机器可以获得更快的工作速度。{超频和电压无关，只和功率有关。}在游戏前期，超频分{有损超频}和{无损超频}两种。", RED, GOLD, GOLD)
                    .line()
                    .line("绝大多数机器都为{有损超频}，这也是默认的超频模式。机器每能获得超过配方{4倍}的功率，便会：", GOLD, GREEN)
                    .line()
                    .line("{-} {0.5x} 耗时", BLUE, GREEN)
                    .line("{-} {4x} 功率", BLUE, RED)
                    .line("{->} {2x} 总能量消耗", BLUE, RED)
                    .line()
                    .line("{无损超频}仅有特定机器支持。机器每能获得超过配方{4倍}的功率，便会：", GOLD, GREEN)
                    .line()
                    .line("{-} {0.25x} 耗时", BLUE, GREEN)
                    .line("{-} {4x} 功率", BLUE, RED)
                    .line("{->} {1x} 总能量消耗", BLUE, AQUA)
                    .line()
                    .page()
                    .line("超频可以触发{无限次}。如果你给有损超频机器提供了16倍功率，那么机器就会执行2次超频（{4x4}），并且耗时变为{0.25x}；如果提供64倍则执行3次超频耗时变为{0.125x}。", YELLOW, GREEN, GREEN, GREEN)
                    .line()
                    .line("{单方块机器}{无法}常规提升电流的输入（默认1A），可通过更换{更高级}的单方块机器{提升电压}来触发超频。", GOLD, RED, AQUA, YELLOW)
                    .page()
                    .line("假设目前我要执行一个{MV}配方，{有损超频}", MV, GOLD)
                    .line()
                    .line("①安装2个{2A}{MV}能源仓：", YELLOW, MV)
                    .line()
                    .line("{-} {4A}{MV} / {4A}{MV} 使用 / 提供功率", BLUE, YELLOW, MV, YELLOW, MV)
                    .line("{-} {0.5x} 耗时", BLUE, GREEN)
                    .line("{->} {2x} 总能量消耗", BLUE, RED)
                    .line()
                    .line("②安装1个{2A}{MV}能源仓，1个{16A}{MV}能源仓：", YELLOW, MV, YELLOW, MV)
                    .line()
                    .line("{-} {16A}{MV} / {18A}{MV} 使用 / 提供功率", BLUE, YELLOW, MV, YELLOW, MV)
                    .line("{-} {0.25x} 耗时", BLUE, GREEN)
                    .line("{->} {4x} 总能量消耗", BLUE, RED)
                    .line()
                    .line("{注意这里没有使用满18A！在不安装够执行下一次超频的能源仓时，机器只会使用当前超频所需要的功率，而非能源仓提供的全部功率}", GRAY.italic())
                    .line()
                    .line("{③安装2个}{16A}{ULV}能源仓，一共提供了{2A}{MV}的功率，但是机器配方电压只有{LV}，{无法执行配方}", GRAY.italic(), YELLOW.italic(), ULV, YELLOW, MV, LV, RED)
                    .line()
                    .line("{-} {--A}{MV} / {8A}{LV}  使用 / 提供功率", BLUE, YELLOW, MV, YELLOW, LV)
                    .page()
                    .line("你一定发现了多方块的UI中左下角有一个栏叫做{\"调整机器超频的最小时间\"}，这是用于保护tps而设计的。它的默认值是{20t}，也就是{1秒}。", YELLOW, GREEN, GREEN)
                    .line()
                    .line("如果配方的耗时在超频过程中低于这个值，另一个机制{\"补偿并行\"}将会发挥作用。它会将试图超频到{低于最小时间}的超频次数转化为{并行}，每一次超频转化为{2x}额外并行。这也就是为什么可以在小机器上看到并行。", YELLOW, AQUA, YELLOW, GREEN)
                    .line()
                    .line("{“补偿并行”机制生效一次等效于有损超频一次}，因此无损超频的机器可以把这个值调低一些。", YELLOW))
            .task(
                    Task.checkmark(),
                    Task.item(GTOMachines.ENERGY_INPUT_HATCH_16A[GTValues.LV]).optional(),
                    Task.item(GTOMachines.WIRELESS_INPUT_HATCH_64[GTValues.EV]).optional())
            .dependsOn(MEANS_OF_ACCELERATION)
            .hideUntilDepsComplete());

    public static final Quest BOOST_MECHANISM = CHAPTER.quest("boost_mechanism", q -> q
            .at(7, -2.25)
            .icon(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV])
            .title("升压机制")
            .subtitle("我在LV怎么做MV的配方？")
            .desc(d -> d
                    .line("{双仓升压}：在一台多方块机器上安装至少{两个}{同等级能源仓}（无需电流相同）可以触发机器{升压}，机器升压后可以执行{高一级电压}的配方", GREEN, GREEN, GOLD, GOLD, AQUA)
                    .line()
                    .line("要注意机器的UI中标识了两个电压，{最大功率}标注的电压与{超频次数}相关，{最大配方等级}才是机器能够制作的{配方电压}", GOLD, YELLOW, GOLD, YELLOW)
                    .line()
                    .line("{升压并没有改变能源仓提供的功率，只是提升了电压，降低了电流。升压机制一定能实现的原因是最低电流的能源仓也至少可以给机器提供2A电流，因此双仓升压之后一定能提供至少1A的电流。}", GRAY.italic()))
            .task(Task.checkmark(), Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV]).optional())
            .dependsOn(Q_79646E8E, LvChapter.ENERGY_HATCH)
            .hideDependencyLines());

    public static final Quest OVERCLOCKING_OVERLOAD = CHAPTER.quest("overclocking_overload", q -> q
            .at(11, -2)
            .icon(MultiBlockD.ADVANCED_ASSEMBLY_LINE)
            .title("超损超频！？")
            .subtitle("能使用激光仓的代价")
            .desc(d -> d
                    .line("随着机器能够使用{激光仓}，超频开始变得更为{昂贵}。对于寻常的{超级机器}，机器每能获得超过配方{4倍}的功率，便会：", GOLD, RED, YELLOW, GREEN)
                    .line()
                    .line("{-} {0.55x} 耗时", BLUE, GREEN)
                    .line("{-} {4x} 功率", BLUE, GREEN)
                    .line("{->} {2.2x} 总能量消耗", BLUE, RED)
                    .line()
                    .line("而处理阵列和进阶装配线则更为特殊：它们的{耗时乘数}为{0.65x}（{2.6x} 总能量消耗）。", YELLOW, GREEN, RED)
                    .line()
                    .line("{这导致了进阶装配线做单个配方还没装配线快}", GRAY.italic()))
            .task(
                    Task.checkmark(),
                    Task.item(MultiBlockD.ADVANCED_ASSEMBLY_LINE).optional(),
                    Task.item(MultiBlockG.PROCESSING_ARRAY[GTValues.IV]).optional(),
                    Task.item(GTOMachines.WIRELESS_INPUT_HATCH_4096[GTValues.IV]).optional())
            .dependsOn(OVERCLOCKING, IvChapter.IV_256A_LASER_TARGET_HATCH)
            .hideUntilDepsComplete());

    public static final Quest SUPER_OVERCLOCKING = CHAPTER.quest("super_overclocking", q -> q
            .at(13, -2)
            .icon(GTOMachines.OVERCLOCK_HATCH[GTValues.UV])
            .title("超级超频！！！")
            .subtitle("可以更改超频效果了")
            .desc(d -> d
                    .line("{超频仓}，{格雷科技有限公司}研发部最得意的作品之一。能够无条件调控每次超频中{耗时的乘数}。", GOLD, DARK_AQUA, YELLOW)
                    .line()
                    .line("以{UHV超频仓}举例，安装之后每进行一次{超频}，耗时都将变为{0.33x}。", GOLD, YELLOW, GREEN)
                    .line()
                    .line("从{UIV超频仓}开始，耗时乘数突破了{4}，这意味着超频不仅{省时}，甚至还{节能}。", GOLD, GREEN, YELLOW, YELLOW)
                    .line()
                    .line("并且在乘数大于等于{4}的时候，超频比并行拥有{更高}的优先级：机器会优先计算超频，再计算并行。", GREEN, RED))
            .task(Task.checkmark(), Task.item(GTOMachines.OVERCLOCK_HATCH[GTValues.UV]).optional())
            .dependsOn(OVERCLOCKING_OVERLOAD, UvChapter.SUPER_BLAST_SMELTER)
            .hideUntilDepsComplete());

    public static final Quest PATCH_SERIES = CHAPTER.quest("patch_series", q -> q
            .at(11, 1)
            .icon(GTOItems.LUV_POWER_AMPLIFIERS)
            .title("贴片系列")
            .subtitle("增幅器能拆，模块不能")
            .desc(d -> d
                    .line("{增幅器}的效果和无损超频非常类似，提升功率，降低耗时，总耗能不变。最高级的增幅器{等效}于一次无损超频。", GOLD, YELLOW)
                    .line()
                    .line("{增幅器的优先级比超频高，因此很多时候会顶掉一次超频，这不是bug，因此不要给无损超频的机器贴}", YELLOW)
                    .line()
                    .line("{在0次超频的机器上贴增幅器有可能会使机器拉取超过上限的功率，导致机器断电}", RED)
                    .line()
                    .line("翻页查看模块效果")
                    .page()
                    .line("{能量模块}和{速度模块}{无条件}{降低配方功率}和{提升速度},两者最高都是{0.5x}，比增幅器强非常多", GOLD, GOLD, AQUA, YELLOW, YELLOW, GREEN)
                    .line()
                    .line("{右键}可使用模块的机器会消耗玩家最多{30970点}（即{100级}）的经验来获取附加属性。玩家至少需要拥有10级经验，当消耗100级经验时附加属性几率达到最大值", YELLOW, GREEN, GREEN)
                    .line()
                    .line("以下为已知可以使用模块的机器：")
                    .line()
                    .line("- 所有单方块机器")
                    .line("- 电力高炉，反应炉，真空冷冻机和合金冶炼炉")
                    .line("- 烈焰高炉和寒冰冷冻机")
                    .line("- {“大批量加工”}章节中的机器", YELLOW)
                    .line("- 转底炉，巨型合金冶炼炉和巨型真空冷冻机")
                    .line()
                    .line("{注意：拆除机器主机会直接清空机器主机上的升级！}", RED))
            .task(
                    Task.checkmark(),
                    Task.item(GTOItems.LUV_POWER_AMPLIFIERS).optional(),
                    Task.item(GTOItems.SPEED_UPGRADE_MODULE).optional(),
                    Task.item(GTOItems.ENERGY_UPGRADE_MODULE).optional())
            .dependsOn(MEANS_OF_ACCELERATION, UtilChapter.PATCH_ASSIST)
            .hideUntilDepsComplete());

    public static final Quest CONFIGURABLE_MAINTENANCE_HATCH = CHAPTER.quest("configurable_maintenance_hatch", q -> q
            .at(9, 1)
            .icon(GTMachines.CONFIGURABLE_MAINTENANCE_HATCH)
            .title("负荷调节维护仓")
            .subtitle("聊胜于无")
            .desc(d -> d.line("根据难度不同可以获得不同的{加速速度}。每个{环境仓}都有对应的变体。值得注意的是{减速模式}可以用于{发电机}来增加发电。", YELLOW, YELLOW, YELLOW, GOLD))
            .task(Task.checkmark(), Task.item(GTMachines.CONFIGURABLE_MAINTENANCE_HATCH).optional())
            .dependsOn(MEANS_OF_ACCELERATION, HvChapter.CONFIGURABLE_MAINTENANCE_HATCH)
            .hideUntilDepsComplete());

    public static final Quest PARALLEL_CONTROL_HATCH = CHAPTER.quest("parallel_control_hatch", q -> q
            .at(11, -0.5)
            .icon(GCYMMachines.PARALLEL[GTValues.IV])
            .title("并行控制仓")
            .subtitle("大批量加工中等效于无损超频")
            .desc(d -> d
                    .line("并行控制仓的原理是允许{同时加工}多份原材料。当制作大量配方时，并行的效果就是制作{x份}物品，花费{x份}能量，使用{1份}时间。", YELLOW, GREEN, GREEN, GREEN)
                    .line()
                    .line("平均到1份配方上，就是使用了{1份}能量，使用了{1/x份}时间。这个效果与无损超频是{等效}的。", GREEN, GREEN, GOLD)
                    .line()
                    .line("在并行与超频的联动上，机器会{优先进行并行计算}，若计算完并行后仍有{多余的功率}则进行超频。", YELLOW, YELLOW))
            .task(Task.checkmark(), Task.item(GCYMMachines.PARALLEL[GTValues.IV]).optional())
            .dependsOn(MEANS_OF_ACCELERATION, IvChapter.IV_PARALLEL_HATCH)
            .hideUntilDepsComplete());

    public static final Quest ACCELERATION_HATCH = CHAPTER.quest("acceleration_hatch", q -> q
            .at(9, -0.5)
            .icon(GTOMachines.ACCELERATE_HATCH[GTValues.LV])
            .title("加速仓")
            .subtitle("无条件加速")
            .desc(d -> d
                    .line("{无条件}加快机器速度，耗时乘以{加速仓}UI中显示的数值（如显示{50}就是{0.50x}）。当加速仓等级每{低于}配方一级，加速效果减少{20%}（如从0.50x变为0.70x）。", AQUA, GOLD, GREEN, GREEN, AQUA, GREEN)
                    .line()
                    .line("{无条件就是指总耗能也变少了}", GRAY.italic()))
            .task(Task.checkmark(), Task.item(GTOMachines.ACCELERATE_HATCH[GTValues.LV]).optional())
            .dependsOn(MEANS_OF_ACCELERATION, LvChapter.MAKE_TIME_GO_FASTER)
            .hideUntilDepsComplete());

    public static final Quest THREADING_HATCH = CHAPTER.quest("threading_hatch", q -> q
            .at(13, 1)
            .icon(GTOMachines.THREAD_HATCH[GTValues.UV])
            .title("线程仓")
            .subtitle("跨配方并行")
            .desc(d -> d
                    .line("和并行控制仓的区别：并行控制仓只能允许{相同配方}同时运行，而线程仓允许{不同配方}也同时运行", YELLOW, YELLOW)
                    .line()
                    .line("同时，每个线程都能独立拉取机器最大功率的电量"))
            .task(Task.checkmark(), Task.item(GTOMachines.THREAD_HATCH[GTValues.UV]).optional())
            .dependsOn(MEANS_OF_ACCELERATION, UvChapter.SUPER_BLAST_SMELTER)
            .hideUntilDepsComplete());

    public static final Quest HEATING_COILS_GLASS = CHAPTER.quest("heating_coils_glass", q -> q
            .at(13, -0.5)
            .icon(GTBlocks.COIL_CUPRONICKEL)
            .title("线圈/玻璃")
            .subtitle("这也算吗")
            .desc(d -> d.line("在面对特殊的机器时，更高等级的{线圈}和{玻璃}也能加速", GOLD, GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(GTBlocks.COIL_CUPRONICKEL).optional(),
                    Task.item(GTMultiMachines.LARGE_CHEMICAL_REACTOR).optional(),
                    Task.item(MultiBlockA.CHEMICAL_PLANT).optional(),
                    Task.item(MultiBlockA.CHEMICAL_COMPLEX).optional(),
                    Task.item(GTBlocks.CASING_TEMPERED_GLASS).optional(),
                    Task.item(MultiBlockG.INTEGRATED_VAPOR_DEPOSITION_SYSTEM).optional())
            .dependsOn(MEANS_OF_ACCELERATION, LvChapter.ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF)
            .hideUntilDepsComplete());

    public static final Quest OMISSION_ALERT = CHAPTER.quest("omission_alert", q -> q
            .at(5, -6.5)
            .title("遗漏提示？")
            .desc(d -> d.line("希望贡献更多提示？在qq群/discord/github上留下你的脚印！"))
            .task(Task.checkmark()));

    public static final Quest FUNCTIONAL_BLOCK_VOLTAGE = CHAPTER.quest("functional_block_voltage", q -> q
            .at(7, 1.25)
            .icon(GTBlocks.CASING_TEMPERED_GLASS)
            .title("功能性方块的电压")
            .subtitle("玻璃,线圈的等级怎么对应上电压啊?")
            .desc(d -> d
                    .line("绝大多数有等级的{功能性方块}标明了其所属{电压}", GOLD, YELLOW)
                    .line()
                    .line("有一些方块如{线圈}和{玻璃}没有标注，{等级1对应LV电压}。你也可以在{无线电网}和{里程碑}两章查看对应的电压", GOLD, GOLD, YELLOW, AQUA, AQUA))
            .task(Task.checkmark(), Task.item(GTBlocks.COIL_CUPRONICKEL).optional(), Task.item(GTBlocks.CASING_TEMPERED_GLASS).optional())
            .dependsOn(Q_79646E8E, LvChapter.COIL_BLOCK)
            .hideDependencyLines());

    public static final Quest MAXIMUM_POWER_AND_MAXIMUM_RECIPE_TIER = CHAPTER.quest("maximum_power_and_maximum_recipe_tier", q -> q
            .at(7, -4)
            .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
            .title("最大功率和最大配方等级")
            .subtitle("我的多方块机器UI上为什么有两个电压？")
            .desc(d -> d
                    .line("{最大功率}：机器可以调用的最大功率，这个数值影响并行和超频的上限，也就是说影响{机器运行的速度}。", GOLD, AQUA)
                    .line()
                    .line("{最大配方等级}：机器可以执行的最高等级配方，也就是说影响{机器是否能够执行配方}。", GOLD, AQUA)
                    .line()
                    .line("{最大配方等级（或者说机器配方等级）和最大功率是两码事，不要搞混了！}", YELLOW))
            .task(Task.checkmark())
            .dependsOn(Q_79646E8E, LvChapter.ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF)
            .hideDependencyLines());

    public static final Quest SPACE_ELEVATOR_POWER_MODULE = CHAPTER.quest("space_elevator_power_module", q -> q
            .at(15, -0.5)
            .title("太空电梯动力模块")
            .subtitle("这也算吗？？？")
            .desc(d -> d.line("太空电梯通过升级{动力模块}可以获得更高的并行", GOLD))
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_1))
            .dependsOn(MEANS_OF_ACCELERATION, UvChapter.SPACE_ELEVATOR_POWER_MODULE_1)
            .hideUntilDepsComplete());

    public static final Quest ALL_TURNED_INTO_WIRELESS_ENERGY_HATCHES = CHAPTER.quest("all_turned_into_wireless_energy_hatches", q -> q
            .at(7, 4.75)
            .icon(GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.IV])
            .title("都变成无线能源仓了")
            .subtitle("哪里有无线激光仓啊?")
            .desc(d -> d.line("GTO中将{无线激光仓}更名为{无线能源/动力仓}，请在看到{激光仓/多A/高A能源仓}字样的时候默认为{256A}及以上的{无线能源仓/动力仓}。", GOLD, GOLD, YELLOW, AQUA, GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.IV]).optional(),
                    Task.item(GTOMachines.WIRELESS_OUTPUT_HATCH_256[GTValues.IV]).optional())
            .dependsOn(Q_79646E8E, IvChapter.IV_256A_LASER_TARGET_HATCH)
            .hideDependencyLines());

    public static final Quest THE_NETHER_THE_END_AND_OTHER = CHAPTER.quest("the_nether_the_end_and_other", q -> q
            .at(-2.25, 2.25)
            .icon(QuestTextures.NETHER_PORTAL)
            .title("下界，末地，和其他维度")
            .subtitle("原版MC的方法去不了")
            .desc(d -> d
                    .line("下界需要：HV阶段通过下界反应核前往")
                    .line()
                    .line("末地需要：末地创世的十二颗不同的眼睛，详见{魔法线}", link(Magic1Chapter.THE_END_GENESIS))
                    .line()
                    .line("超平坦世界与虚空世界：HV阶段通过虚空传送器访问")
                    .line()
                    .line("其他星球和空间站：通过游戏进程中发射火箭解锁"))
            .task(Task.checkmark(), Task.dimension(Dimension.THE_NETHER).optional(), Task.dimension(Dimension.THE_END).optional())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest FLIGHT_MODE = CHAPTER.quest("flight_mode", q -> q
            .at(-0.75, 2.25)
            .icon(GTOOrganItems.FAIRY_WING)
            .title("飞行模式")
            .subtitle("如何获取飞行?")
            .desc(d -> d
                    .line("其中飞行护符，飞行之板，翅膀，喷气背包可以作为前期飞行手段")
                    .line()
                    .line("沉浸式飞行是飞行载具，同样能让你在前期获得飞行能力")
                    .line()
                    .line("按住{[Z]}键可以进入HUD编辑状态，然后点击右上角“+”号唤出{“玩家实时属性”}窗口，编辑飞行速度以及飞行的物理特性", YELLOW, AQUA)
                    .line()
                    .line("{认真看提示的奖励是：提前送你器官修改器！}", GRAY.italic()))
            .task(
                    Task.checkmark(),
                    Task.item(Apoth.Items.POTION_CHARM.get()).optional(),
                    Task.item(GTOOrganItems.FAIRY_WING).optional(),
                    Task.item(GTOOrganItems.MANA_STEEL_WING).optional(),
                    Task.item(GTOOrganItems.MECHANICAL_WING).optional(),
                    Task.item(QuestExternalRefs.RITUAL_FLIGHT).optional(),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.PROTOTYPE)).optional(),
                    Task.item(GTItems.LIQUID_FUEL_JETPACK).optional(),
                    Task.item(QuestExternalRefs.ImmersiveAircraftItems.BIPLANE).optional())
            .reward(Reward.item(GTOOrganItems.ORGAN_MODIFIER))
            .dependsOn(Q_79646E8E));

    public static final Quest CAN_T_FIT = CHAPTER.quest("can_t_fit", q -> q
            .at(7, 3)
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .title("塞不下...")
            .subtitle("能以其他的姿势摆放多方块吗?")
            .desc(d -> d
                    .line("多方块主机都可以旋转，但{前期的}大部分机器（如大部分蒸汽机器，电力高炉等）{只能沿Y轴旋转}", YELLOW, RED)
                    .line()
                    .line("使用{扳手}{潜行右键}主机可以{旋转}主机，然后对其使用终端就可搭建旋转后的多方块", GOLD, YELLOW, GOLD)
                    .line()
                    .line("在处理过长的多方块的时候，就可以将主机旋转{90度}，来让多方块{竖在地上}（如装配线）", GREEN, YELLOW)
                    .line()
                    .line("还可以{镜像成型}，只需要使用{高级终端}打开镜像模式搭建即可", YELLOW, GOLD))
            .task(Task.checkmark())
            .dependsOn(Q_79646E8E, LvChapter.ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF)
            .hideDependencyLines());

    public static final Quest EATING_AND_GETTING_SICK = CHAPTER.quest("eating_and_getting_sick", q -> q
            .at(0.75, 2.25)
            .icon(SOLCarrotItems.FOOD_BOOK.get())
            .title("进食与生病")
            .desc(d -> d
                    .line("你应该已经发现了，连续吃{相同}的食物，给予的饱食度会越来越{少}——这是{生活调味料}在发力！", GREEN, RED, YELLOW)
                    .line()
                    .line("不过整合包里同样携带了{生活调味料：胡萝卜版}，你可以多多尝试不同的食物以获得最多十颗生命上限。", YELLOW)
                    .line()
                    .line("你还会发现有时你会{得病}，这是因为你的包里装了{危险物质}。尽快丢掉他们，或者穿戴一整套{防护用品}。你还可以使用{解毒剂}来治愈部分长期疾病。", YELLOW, RED, AQUA, AQUA))
            .task(
                    Task.checkmark(),
                    Task.anyOf(1, GTOItems.WREATH, GTItems.PARACETAMOL_PILL, GTItems.RAD_AWAY_PILL).optional().title("解毒剂"),
                    Task.tag(CustomTags.PPE_ARMOR, 1).optional().title("防护用品"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(Q_79646E8E));

    public static final Quest ALL_BUTTONS = CHAPTER.quest("all_buttons", q -> q
            .at(9, -4)
            .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
            .title("全是按钮")
            .subtitle("我的多方块机器UI左下角怎么这么多按钮？")
            .desc(d -> d
                    .line("我们从上至下介绍：")
                    .line()
                    .line("{电源}：用于控制{开关机}，等效于{软锤}右键锤一下。", GOLD, YELLOW, GOLD)
                    .line()
                    .line("{超频最小时间}：用于控制机器{超频}可以到达的{最小时间}。具体机制请查看本页的超频介绍。你可以在GTOCore中配置默认超频最小时间。", GOLD, YELLOW, AQUA)
                    .line()
                    .line("{批处理}：GTO新加入的机制，当开启之后机器会{尽可能}使用输入仓室中的所有材料，并将输出时间{乘以}执行的{配方数量}。单次批处理时间有上限。", GOLD, RED, AQUA, YELLOW)
                    .line()
                    .line("{更新结构检查}：GTO为了性能，在多方块搭建完成后{不会}立即判断{是否成型}。你可以点击此按钮来立刻判断。", GOLD, RED, YELLOW)
                    .line()
                    .line("在某些机器中还会有一个按钮可以用来{调整机器的最大并行数}。", YELLOW))
            .task(Task.checkmark())
            .dependsOn(LvChapter.ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF, MAXIMUM_POWER_AND_MAXIMUM_RECIPE_TIER));

    public static final Quest THE_MYSTERIES_OF_RESEARCH = CHAPTER.quest("the_mysteries_of_research", q -> q
            .at(7, 6.5)
            .icon(GTResearchMachines.RESEARCH_STATION)
            .title("研究的奥秘")
            .subtitle("我爱学习！")
            .desc(d -> d
                    .line("{研究}将与{装配线}一直陪伴你到游戏结束。所有的{装配线配方}都需要{研究}。", AQUA, GOLD, YELLOW, AQUA)
                    .line()
                    .line("将{数据载体}放入{扫描仪/研究站}，等待一段时间之后会获得一份{研究数据实体}，将其放入{数据访问仓}即为{成功研究}。", GOLD, GOLD, GOLD, GOLD, GREEN)
                    .line()
                    .line("若你{无法}将研究数据放入数据访问仓，说明你的数据访问仓需要{升级}了。只有{高级访问仓}才能放入高级研究数据。", RED, YELLOW, YELLOW)
                    .line()
                    .line("使用{靶仓}和{源仓}传递{研究数据}。", GOLD, GOLD, YELLOW)
                    .page()
                    .line("部分{维度聚焦激光蚀刻}配方需要研究，研究需要的材料是{光学闪存}和研究物本体。", YELLOW, GOLD))
            .task(
                    Task.checkmark(),
                    Task.item(GTItems.TOOL_DATA_STICK).optional(),
                    Task.item(GTItems.TOOL_DATA_ORB).optional(),
                    Task.item(GTItems.TOOL_DATA_MODULE).optional(),
                    Task.item(GTResearchMachines.DATA_ACCESS_HATCH).optional(),
                    Task.item(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH).optional(),
                    Task.item(ExResearchMachines.BIO_DATA_ACCESS_HATCH).optional(),
                    Task.item(GTResearchMachines.DATA_HATCH_RECEIVER).optional(),
                    Task.item(GTResearchMachines.DATA_HATCH_TRANSMITTER).optional(),
                    Task.item(GTBlocks.OPTICAL_PIPES[0]).optional())
            .dependsOn(IvChapter.ASSEMBLY_LINE_RECIPE_DATA, Q_79646E8E)
            .hideDependencyLines());

    public static final Quest MAGIC_S_LOSSLESS_OVERCLOCKING = CHAPTER.quest("magic_s_lossless_overclocking", q -> q
            .at(11, -3.5)
            .icon(ManaMachine.MANA_AMPLIFIER_HATCH)
            .title("魔法的无损超频")
            .subtitle("以魔力为代价")
            .desc(d -> d
                    .line("为仓室输入{等同于机器最大功率的魔力}可以使机器本次工作变为{无损超频}，大大加快机器运行速度。", AQUA, YELLOW)
                    .line()
                    .line("魔力增幅仓内的UI可调整其缓存大小。机器开启{批处理}时，仍然只需要给仓室输入{一份}最大功率的魔力。", YELLOW, GREEN)
                    .line()
                    .line("建议在{批处理}的机器中安装此仓，你的魔力供应很难撑住非批处理的机器。", YELLOW))
            .task(Task.checkmark(), Task.item(ManaMachine.MANA_AMPLIFIER_HATCH).optional())
            .dependsOn(OVERCLOCKING, Magic2Chapter.MANA_AMPLIFIER_HATCH)
            .hideUntilDepsComplete());

    public static final Quest SINGLE_BLOCK_CONFIG = CHAPTER.quest("single_block_config", q -> q
            .at(7, -5.75)
            .icon(GTOMachines.ULV_WIREMILL[GTValues.ULV])
            .title("单方块配置")
            .subtitle("格雷大学习")
            .desc(d -> d
                    .line("{任何机器/仓室}的左边都有一个{齿轮形状}的标签页，你可以在这个标签页中{检查方块的朝向，覆盖版的情况，以及（对于单方块机器）输入输出的方向}。", GOLD, AQUA, YELLOW)
                    .line()
                    .line("你可以拖动旋转方块，点击方块的六个面以观察方块。")
                    .line()
                    .line("在这个标签页中，右下角是放置{覆盖版}的槽位。选中一个面后，你可以将此面的覆盖版{拿出}，也可以{放入}覆盖版。当有覆盖版在内的时候，你可以点击槽位左边的{齿轮图标}来设置覆盖版本身。（{无需随身携带撬棍和螺丝刀}）", GOLD, AQUA, AQUA, AQUA, YELLOW)
                    .line()
                    .line("对于小机器来说，由于其输入输出一体，你可以自由配置其输出的方向，决定输出是否阻挡输入，以及决定是否要自动输出：")
                    .line()
                    .line("当你在非输出面点击左下角{第一个按钮}，即可{将输出调整至此面}。再次点击即可开启{自动输出}。", GREEN, YELLOW, YELLOW)
                    .line()
                    .line("左下角{第二个按钮}决定{输入是否能从输出面进入}。", GREEN, YELLOW))
            .task(Task.checkmark())
            .dependsOn(UlvChapter.PRIMITIVE_ASSEMBLY, Q_79646E8E)
            .hideDependencyLines());

    public static final Quest Q_7B11F10A = CHAPTER.quest("q_7b11f10a", q -> q
            .at(0, -8)
            .desc(d -> d
                    .line("在编写任务书描述时，尤其是长段文字，应使用颜色高亮关键内容，并尽量以不同颜色区分不同的重要内容。")
                    .line()
                    .line("{黄色}：文本中的重要信息。只要你认为重要的，无论是词汇，还是一段话，都可以黄色标记", YELLOW)
                    .line()
                    .line("{橙色}：文本中的重要实体（当一个实体在之前被标记过，若后续反复出现，可以不标记。无需标记整段话中的所有实体，当然也可以这么做 ）。重要实体范围包括，实体的方块，物品，机器，流体，一类实体物品的统称也可以使用橙色，也可以使用黄色。", GOLD)
                    .line()
                    .line("{亮红}/{深红}：文本中体现的重要否定意味词语。 如 一定{不要}{跳入岩浆}。 常规标法如果只标记跳入岩浆，有可能被误读为“就是要跳进去”。我更推荐使用亮红。", RED, DARK_RED, RED, YELLOW)
                    .line()
                    .line("{亮绿}/{亮蓝}：通常用于标注数字，具体使用什么颜色根据上下文颜色而定。如果没有特别的上下文，推荐使用绿色。标记范围包括数字，分数等以及其单位。标不标注单位均可，推荐标注单位", GREEN, AQUA)
                    .line()
                    .line("{灰色}：文中的提示信息，通常与斜体共用。 {提示：这是提示}  顺序最好是先斜体，当然显示上完全不会有任何区别。", GRAY, GRAY.italic())
                    .line()
                    .line("{紫色}等其他颜色：其他颜色已经被使用，还想使用不同的新颜色以突出重点时。紫色还通常被使用在科技线中强调魔法侧内容上", LIGHT_PURPLE)
                    .line()
                    .line("在使用其他颜色的时候，我更推荐使用亮色。暗色的阅读稍微更困难一些。")
                    .line()
                    .line("当你觉得文中使用的黄色够多的时候，你完全也可以使用蓝色或其他颜色标注另一些同样关键的文本，颜色并非强制要求，仅作推荐。")
                    .page()
                    .line("对于电压的标注，有多种情况：")
                    .line()
                    .line("1 标电压颜色，如{HV}{MV}等", HV, MV)
                    .line()
                    .line("2 标成黄色。若不想使用电压本体颜色，或人为其不便识别，可以同一使用黄色")
                    .line()
                    .line("3 被其他标色覆盖。如一个物品IV能源仓， 可以是{IV能源仓}， 也可以是{IV}能源仓 ，也可以是{IV}{能源仓}（不太推荐）", GOLD, IV, IV, GOLD)
                    .page()
                    .line("对于格式的使用：")
                    .line()
                    .line("斜体通常与灰色一起配合，作为提示出现。也可作为背景故事，补充知识等使用")
                    .line()
                    .line("下划线通常出现在超链接上。")
                    .line()
                    .line("删除线更适用于一些吐槽上")
                    .line()
                    .line("加粗则是用的较少，如标注重要信息。但是重要信息主要使用黄色标注，通常只有在黄色的中间再想体现重点的时候，才会使用加粗"))
            .task(Task.checkmark().title("致编辑者-行文的建议"))
            .optional().invisible());

    public static final Quest IRON_FARM = CHAPTER.quest("iron_farm", q -> q
            .at(-2.25, 4.5)
            .desc(d -> d.line("除了铁傀儡农场，Easy Villagers还有很多好东西来帮助你度过前期，如刷取村民和自动交易等"))
            .task(Task.item(ModItems.IRON_FARM.get()))
            .reward(Reward.item(ModItems.FARMER.get()), Reward.item(ModItems.TRADER.get()))
            .dependsOn(SteamChapter.PREPARING_GLASS_STEP_THREE)
            .hideDependencyLines());

    public static final Quest BELT = CHAPTER.quest("belt", q -> q
            .at(-0.75, 4.5)
            .desc(d -> d
                    .line("各种各样的工具塞满了你的物品栏？你可以将它们都放在皮带里")
                    .line()
                    .line("{工具皮带}与{皮带袋}在铁砧上合成可增加皮带的槽位", GOLD, GOLD)
                    .line()
                    .line("维护消声仓时会自动调用皮带中的工具"))
            .task(Task.item(QuestExternalRefs.ToolBeltItems.BELT))
            .reward(Reward.item(QuestExternalRefs.ToolBeltItems.POUCH))
            .dependsOn(MainStoneageChapter.A_FULL_ARSENAL));

    public static final Quest LV_ITEM_MAGNET = CHAPTER.quest("lv_item_magnet", q -> q
            .at(3.75, 6)
            .icon(GTItems.ITEM_MAGNET_LV)
            .desc(d -> d.line("吸附掉落物"))
            .task(Task.item(GTItems.ITEM_MAGNET_LV), Task.item(QuestExternalRefs.SophisticatedBackpacksItems.MAGNET_UPGRADE))
            .dependsOn(LvChapter.ANY_LV));

    public static final Quest Q_067AC6B5 = CHAPTER.quest("q_067ac6b5", q -> q
            .at(-5.25, 4.5)
            .desc(d -> d.line("这里有一些任务没提及的小工具"))
            .task(Task.checkmark().title("有用的工具")));

    public static final Quest TRAVEL_STAFF = CHAPTER.quest("travel_staff", q -> q
            .at(0.75, 4.5)
            .icon(GTOItems.TRAVEL_STAFF)
            .desc(d -> d
                    .line("从Ender IO移植而来的旅行手杖")
                    .line()
                    .line("将样板供应器作为传送锚点的功能可以在（游戏菜单 > MOD > 搜索 GTOCore > 配置）中关闭"))
            .task(Task.item(GTOItems.TRAVEL_STAFF), Task.item(GTOMachines.TRAVEL_ANCHOR))
            .dependsOn(LvChapter.WELCOME_TO_THE_LOW_VOLTAGE_STAGE));

    public static final Quest TEMPLATE_MANAGER = CHAPTER.quest("template_manager", q -> q
            .at(3.75, 4.5)
            .icon(Registration.TemplateManager_ITEM.get())
            .desc(d -> d.line("模板管理器经过魔改可以放入AE的{空白样板}，将复制小帮手复制的方块直接存为{样板}", GOLD, GOLD))
            .task(Task.item(Registration.TemplateManager_ITEM.get()), Task.item(Registration.CopyPaste_Gadget.get())));

    public static final Quest Q_2F1EBC6B = CHAPTER.quest("q_2f1ebc6b", q -> q
            .at(-3.75, 6)
            .desc(d -> d
                    .line("GTO中的EMI经过了大幅的性能优化和体验优化")
                    .line()
                    .line("你可以将EMI中的物品直接拖入AE终端和EMI的搜索栏")
                    .line()
                    .line("EMI的搜索文字也可以存为书签")
                    .line()
                    .line("佩戴无线终端后，EMI中尝试取出物品会等效于在无线AE终端中取出物品，将物品放入EMI等效于直接将物品放回ME网络，EMI中中键点击物品会直接打开下单页面"))
            .task(Task.checkmark().title("EMI")));

    public static final Quest Q_466185D2 = CHAPTER.quest("q_466185d2", q -> q
            .at(2.25, 6)
            .desc(d -> d
                    .line("应用能源（AE2）章节左上角有许多小工具，别忘了去看看！")
                    .line()
                    .line("还有许多新终端，别忘了去章节右下角看看"))
            .task(Task.checkmark().title("AE小工具")));

    public static final Quest LV_TRADING_STATION = CHAPTER.quest("lv_trading_station", q -> q
            .at(-3.75, 8)
            .icon(GTOMachines.TRADING_STATION[GTValues.LV])
            .desc(d -> d
                    .line("从掌上银行获取格雷会员卡")
                    .line()
                    .line("目前已知多人游戏有bug，且游戏货币系统不完善，暂时没什么用"))
            .task(Task.item(GTOMachines.TRADING_STATION[GTValues.LV]), Task.item(GTOItems.PALM_SIZED_BANK)));

    public static final Quest EXPLORERSCOMPASS = CHAPTER.quest("explorerscompass", q -> q
            .at(-3.75, 4.5)
            .desc(d -> d
                    .line("前期探险的好帮手")
                    .line()
                    .line("{从某个彩蛋任务中你可以获得一整套指南针}", GRAY.italic()))
            .task(Task.item(ExplorersCompass.explorersCompass), Task.item(NaturesCompass.naturesCompass), Task.item(AEItems.METEORITE_COMPASS)));

    public static final Quest GADGET_CUT_PASTE = CHAPTER.quest("gadget_cut_paste", q -> q
            .at(2.25, 4.5)
            .desc(d -> d.line("快速搬家，拆走各种不可掉落方块的利器"))
            .task(Task.item(Registration.CutPaste_Gadget.get())));

    public static final Quest VILLAGE_TRADING_STATION = CHAPTER.quest("village_trading_station", q -> q
            .at(-2.25, 8)
            .task(Task.item(GTOMachines.VILLAGE_TRADING_STATION))
            .dependsOn(HvChapter.POLYTETRAFLUOROETHYLENE_INGOT));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.WIRELESS_CRAFTING_TERMINAL, -2.25, 6))
                .link(QuestLink.of(AeChapter.LEVEL_EMITTER, 0.75, 6))
                .link(QuestLink.of(AeChapter.ME_2IN1_TERMINAL, -0.75, 6)));
    }
}
