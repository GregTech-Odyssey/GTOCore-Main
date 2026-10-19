package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import de.maxhenkel.easyvillagers.items.ModItems;
import vazkii.botania.common.item.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewGregtechOdysseyChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_gregtech_odyssey", c -> c
            .title("0. 欢迎入职格雷科技有限公司™")
            .subtitle("Hello World!")
            .iconStack(
                    () -> QuestStacks.unbreakable(QuestStacks.enchanted(QuestStacks.enchanted(ToolHelper.get(GTOToolType.VAJRA_IV, GTOMaterials.Enderium), Enchantments.BLOCK_FORTUNE, 8), Enchantments.MOB_LOOTING, 8))));

    public static final Quest Q_19EFE14C = CHAPTER.quest("q_19efe14c", q -> q
            .at(0, 0)
            .icon(Items.WRITABLE_BOOK)
            .subtitle("我无法拒绝...")
            .desc(d -> d
                    .line("{看到有这么多章节，不知道从何入手？}", GREEN)
                    .line()
                    .line("{以下是推荐阅读优先级：}", GREEN)
                    .line()
                    .line("{⭐在砍下第一棵树前建议前往}{提示}{章节查看提示，花几分钟阅读一下对你的游玩之旅大有帮助}", YELLOW, link(TipsChapter.Q_79646E8E), YELLOW)
                    .line()
                    .line("{⭐沿着}{主线}{推进科技}", YELLOW, AQUA, YELLOW)
                    .line()
                    .line("{应用能源}以及{魔法线（魔力导论&英雄之路）}是两个比较重要的支线，你可以等主线需要它们，或想提升体验时查看", AQUA, AQUA)
                    .line()
                    .line("其他支线如神化、化工、矿物处理、食物等，按任务需求或个人兴趣穿插阅读即可")
                    .line()
                    .line("{Tips：带有下划线的文本大多为超链接，可直接点击跳转}", GRAY.italic()))
            .task(Task.checkmark().title("查看提示"))
            .reward(Reward.xpLevels(5).personal()));

    public static final Quest Q_6517DDBE = CHAPTER.quest("q_6517ddbe", q -> q
            .at(6, 0)
            .milestone()
            .icon(BotaniaItems.questgiverMark)
            .desc(d -> d
                    .line("GTO的所有生物根据难度对怪物进行了血量和攻击力强化")
                    .line()
                    .line("同时还给中立生物添加了仇恨机制，如果你在动物旁边进食同类的肉，会引来动物攻击你")
                    .line()
                    .line("注意安全"))
            .task(Task.checkmark().title("关于生存"))
            .reward(Reward.item(BotaniaItems.wickedEyepatch).title("愿中二之魂燃烧你的战意"))
            .dependsOn(TipsChapter.DIFFICULTY_AND_MODES));

    public static final Quest Q_278F2909 = CHAPTER.quest("q_278f2909", q -> q
            .at(9, 0)
            .milestone()
            .icon(ModItems.VILLAGER.get())
            .desc(d -> d
                    .line("The initial {English/Russian} version of FTB Quests has been largely machine-translated from {Chinese} using {LLMs},  and it {cannot be guaranteed to be accurate}.", GOLD, GOLD, AQUA, YELLOW)
                    .line()
                    .line("If you find any parts {unclear or poorly translated}, please feel free to let us know via {Discord} or a {GitHub issue}—we totally get it, and we're sorry for any messy bits!", YELLOW, GOLD, GOLD)
                    .line()
                    .line("We greatly appreciate anyone willing to help us improve the translation and make it better!")
                    .line()
                    .line("{Github: }{https://github.com/GregTech-Odyssey/GregTech-Odyssey}", YELLOW, url("https://github.com/GregTech-Odyssey/GregTech-Odyssey"))
                    .line()
                    .line("{Discord: }{https://discord.gg/PxruFSbtCn}", YELLOW, url("https://discord.gg/PxruFSbtCn"))
                    .page()
                    .line("We plan to complete the {localization translation work} for other languages (English, Russian, etc.), but due to time and capacity constraints, {it is difficult for us to finish all the translations}.", YELLOW, YELLOW)
                    .line()
                    .line("Therefore, we have created a {project} on the translation collaboration platform and {invited everyone to join in the translation}:", GOLD, YELLOW)
                    .line()
                    .line("{English / en_us: }{Overview - GregTech-Odyssey(en) - ParaTranz}", YELLOW, url("https://paratranz.cn/projects/16320"))
                    .line()
                    .line("{Russian / ru_ru: }{Overview - GregTech-Odyssey(ru) - ParaTranz}", YELLOW, url("https://paratranz.cn/projects/16525"))
                    .line()
                    .line("If you're interested, please click to join and help improve our translations. Anyone can join the project directly. Contributions are welcome!")
                    .line()
                    .line("                                      ——GregTech Odyssey DevTeam"))
            .task(Task.checkmark().title("任务本地化相关"))
            .reward(Reward.item(GTOItems.QUEST_BOOK))
            .dependsOn(Q_6517DDBE));

    public static final Quest WIKI = CHAPTER.quest("wiki", q -> q
            .at(12, 0)
            .milestone()
            .icon(Items.BOOKSHELF)
            .title("WIKI")
            .desc(d -> d
                    .line("我们的WIKI:")
                    .line()
                    .line("{https://gtodyssey.com/}", url("https://gtodyssey.com/"))
                    .line()
                    .line("你可以在WIKI上查找文档和指南！"))
            .task(Task.checkmark())
            .reward(Reward.xpLevels(5))
            .dependsOn(Q_278F2909));

    public static final Quest HELLO_WORLD = CHAPTER.quest("hello_world", q -> q
            .at(15, 0)
            .milestone()
            .icon(Items.GRASS_BLOCK)
            .title("Hello World")
            .subtitle("To Greg, or not GTO...")
            .desc(d -> d
                    .line("欢迎来到{GregTech:Odyssey}！", RED.underline())
                    .line()
                    .line("也许可能你从未了解过格雷科技系列，也有可能是格雷科技的资深老登，任务书将深入浅出地引导你体验所有的里程碑阶段。")
                    .line()
                    .line("本整合包较原版格雷科技系列有流程上的{较大改动}，所以跟着任务书走不妨是一个不错的选择。", DARK_GREEN)
                    .line()
                    .line("{不过，任务线不保证你能获得最好的流程体验，随着进程发展，你会学着如何设计并优化自己的产线，这是格雷系列最核心的乐趣。}", GRAY)
                    .page()
                    .line("所以最开始该干什么呢？做出燧石工具，找到一片靠近水源的地方作为早期发展的基地")
                    .line()
                    .line("按 {[Tab]} 可以随时随地打开任务书，从主线的石器时代开始一步步跟随任务推进科技", YELLOW)
                    .line()
                    .line("{以及有一些隐藏彩蛋任务}", GRAY.italic().strike())
                    .line()
                    .line("{切勿}熬夜爆肝，身体是革命的本钱！！", DARK_RED)
                    .line()
                    .line("{我们的旅程是星辰大海}", BLUE.italic())
                    .line()
                    .line("{如果你现在所处主线，发现完成不了此任务，说明你还没有完成入职培训！点击本窗口左上角的锁链图标去向入职页面。}", GRAY.italic()))
            .task(Task.checkmark().title("Hello World！"))
            .reward(
                    Reward.item(Items.STONE_SHOVEL),
                    Reward.command("/function firework:stonelaunch").elevated().silent().autoClaim(AutoClaim.INVISIBLE),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(WIKI, UtilChapter.EXPERT_MAINLINE_LOCK));

    public static final Quest Q_5C4137F6 = CHAPTER.quest("q_5c4137f6", q -> q
            .at(18, 0).minWidth(350)
            .icon(GTItems.PORTABLE_DEBUG_SCANNER)
            .subtitle("电压等级对应的主要内容")
            .desc(d -> d.line("开发中"))
            .task(Task.checkmark().title("关于内容"))
            .reward(Reward.item(BotaniaItems.manaCookie))
            .dependsOn(HELLO_WORLD)
            .optional().invisible());

    public static final Quest LOOK_BEFORE_YOU_LEAP_EXPERT = CHAPTER.quest("look_before_you_leap_expert", q -> q
            .at(15, 2)
            .title("三思而后行{（专家）}", LIGHT_PURPLE)
            .subtitle("主线任务已上锁")
            .desc(d -> d
                    .line("再次提示：{创建世界后你将无法更改难度！}", RED)
                    .line()
                    .line("{请务必了解所有专家特性后，再选择游玩此难度}", YELLOW)
                    .line()
                    .line("{为了确保你已经了解过专家特性，你需要使用铁砧将纸正确命名为暗号后，方可开始游戏}", YELLOW)
                    .line()
                    .line("{暗号是：}{WIKI上，中文页面的特性指南，专家模式（不包含标题）下所有的大写英文字母，按顺序相连}", YELLOW, GOLD)
                    .line()
                    .line("{WIKI的链接可以在“难度与模式”任务中找到}", GRAY.italic())
                    .line()
                    .line("{如果访问不了WIKI也可前往}{内置指南}{查看标橙部分}", GRAY.italic(), link(TipsChapter.EASY_NORMAL_EXPERT_MODE_FEATURE_GUIDE), GRAY.italic()))
            .task(Task.checkmark())
            .reward(Reward.item(Items.ANVIL), Reward.xpLevels(5), Reward.item(Items.PAPER))
            .dependsOn(UtilChapter.Q_1FB1EC5B)
            .optional().hideUntilDepsVisible());

    public static final Quest OPEN_SESAME_EXPERT = CHAPTER.quest("open_sesame_expert", q -> q
            .at(15, 4)
            .title("芝麻开门{（专家）}", LIGHT_PURPLE)
            .subtitle("主线任务已解锁")
            .desc(d -> d.line("既然做好准备了，欢迎你，挑战者！"))
            .task(Task.item(Items.PAPER).nbt("{RepairCost:0,display:{Name:'{\"text\":\"AEMVEV\"}'}}").matchNbt(true))
            .optional().invisible());

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(TipsChapter.DIFFICULTY_AND_MODES, 3, 0))
                .mainLine(TipsChapter.DIFFICULTY_AND_MODES, Q_6517DDBE, Q_278F2909, WIKI, HELLO_WORLD));
    }
}
