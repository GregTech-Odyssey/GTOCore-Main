package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.ench.Ench;
import vazkii.botania.common.item.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class ApotheosisChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("apotheosis", c -> c
            .title("神化")
            .subtitle("Apotheosis")
            .icon(Adventure.Items.MYTHIC_MATERIAL.get())
            .hideQuestsUntilDepsVisible()
            .flexible());

    public static final Quest RARITY_FORGE = CHAPTER.quest("rarity_forge", q -> q
            .at(-2, -2.5)
            .subtitle("械能萃奇珍，工巧炼天华")
            .desc(d -> d.line("通过{格雷科技}的先进工艺，可以高效获取{珍宝材料}", RED, BLUE))
            .task(Task.item(MultiBlockG.RARITY_FORGE))
            .reward(Reward.xpLevels(10))
            .dependsOn(UlvChapter.PRIMITIVE_ASSEMBLY));

    public static final Quest COMMON_MATERIAL = CHAPTER.quest("common_material", q -> q
            .at(-3.5, -1.5)
            .desc(d -> d.line("一块形状怪异且无法辨认的金属"))
            .task(Task.item(Adventure.Items.COMMON_MATERIAL.get()))
            .reward(Reward.xpLevels(1))
            .dependsOn(RARITY_FORGE));

    public static final Quest UNCOMMON_MATERIAL = CHAPTER.quest("uncommon_material", q -> q
            .at(-0.5, -1.5)
            .desc(d -> d.line("一块非常耐用的布匹"))
            .task(Task.item(Adventure.Items.UNCOMMON_MATERIAL.get()))
            .reward(Reward.xpLevels(1))
            .dependsOn(RARITY_FORGE));

    public static final Quest RARE_MATERIAL = CHAPTER.quest("rare_material", q -> q
            .at(-3.5, -3.5)
            .desc(d -> d.line("一块有史以来最明亮的水晶"))
            .task(Task.item(Adventure.Items.RARE_MATERIAL.get()))
            .reward(Reward.xpLevels(1))
            .dependsOn(RARITY_FORGE));

    public static final Quest EPIC_MATERIAL = CHAPTER.quest("epic_material", q -> q
            .at(-0.5, -3.5)
            .desc(d -> d.line("来自魔界的沙子，魔法扭曲了那里所有的生命"))
            .task(Task.item(Adventure.Items.EPIC_MATERIAL.get()))
            .reward(Reward.xpLevels(1))
            .dependsOn(RARITY_FORGE));

    public static final Quest MYTHIC_MATERIAL = CHAPTER.quest("mythic_material", q -> q
            .at(-2, -4.5)
            .desc(d -> d.line("一颗净化过的神秘珍珠，及其不稳定"))
            .task(Task.item(Adventure.Items.MYTHIC_MATERIAL.get()))
            .reward(Reward.item(Adventure.Items.MYTHIC_MATERIAL.get()), Reward.xpLevels(5))
            .dependsOn(RARITY_FORGE));

    public static final Quest GEM_FUSED_SLATE = CHAPTER.quest("gem_fused_slate", q -> q
            .at(-2, -8)
            .task(Task.item(Adventure.Items.GEM_FUSED_SLATE.get()))
            .reward(Reward.item(Adventure.Items.GEM_FUSED_SLATE.get()), Reward.xpLevels(1)));

    public static final Quest SIGIL_OF_REBIRTH = CHAPTER.quest("sigil_of_rebirth", q -> q
            .at(-1, -6.5)
            .desc(d -> d.line("用于为{重铸台}提供燃料", DARK_PURPLE))
            .task(Task.item(Adventure.Items.SIGIL_OF_REBIRTH.get()))
            .reward(Reward.item(Adventure.Items.SIGIL_OF_REBIRTH.get()), Reward.xpLevels(1))
            .dependsOn(GEM_FUSED_SLATE, MYTHIC_MATERIAL));

    public static final Quest SIGIL_OF_ENHANCEMENT = CHAPTER.quest("sigil_of_enhancement", q -> q
            .at(-3, -6.5)
            .desc(d -> d.line("用于为{强化台}提供燃料", DARK_PURPLE))
            .task(Task.item(Adventure.Items.SIGIL_OF_ENHANCEMENT.get()))
            .reward(Reward.item(Adventure.Items.SIGIL_OF_ENHANCEMENT.get()), Reward.xpLevels(1))
            .dependsOn(GEM_FUSED_SLATE, MYTHIC_MATERIAL));

    public static final Quest SIGIL_OF_SOCKETING = CHAPTER.quest("sigil_of_socketing", q -> q
            .at(-0.5, -8)
            .desc(d -> d.line("用于在{锻造台}中给装备{镶孔}，{最多}镶嵌3个孔位", DARK_PURPLE, DARK_BLUE, DARK_RED))
            .task(Task.item(Adventure.Items.SIGIL_OF_SOCKETING.get()))
            .reward(Reward.item(Adventure.Items.SIGIL_OF_SOCKETING.get()), Reward.xpLevels(1))
            .dependsOn(GEM_FUSED_SLATE));

    public static final Quest SIGIL_OF_WITHDRAWAL = CHAPTER.quest("sigil_of_withdrawal", q -> q
            .at(-3.5, -8)
            .desc(d -> d.line("在{锻造台}中，将镶嵌过{宝石}的装备与摘除符石结合，以从装备中去除镶嵌的宝石", DARK_PURPLE, BLUE))
            .task(Task.item(Adventure.Items.SIGIL_OF_WITHDRAWAL.get()))
            .reward(Reward.item(Adventure.Items.SIGIL_OF_WITHDRAWAL.get()), Reward.xpLevels(1))
            .dependsOn(GEM_FUSED_SLATE));

    public static final Quest SIGIL_OF_UNNAMING = CHAPTER.quest("sigil_of_unnaming", q -> q
            .at(-2, -9.5)
            .desc(d -> d
                    .line("在{锻造台}中，将带有{词缀}的装备与除名符石结合，以从装备中去除词缀文本", DARK_PURPLE, BLUE)
                    .line()
                    .line("{不会清除}原词缀附加的属性", DARK_RED))
            .task(Task.item(Adventure.Items.SIGIL_OF_UNNAMING.get()))
            .reward(Reward.item(Adventure.Items.SIGIL_OF_UNNAMING.get()), Reward.xpLevels(1))
            .dependsOn(GEM_FUSED_SLATE));

    public static final Quest AUGMENTING_TABLE = CHAPTER.quest("augmenting_table", q -> q
            .at(-2, -6.5)
            .subtitle("粹炼独缀，极神话境")
            .desc(d -> d
                    .line("用于强化装备的{单个}词缀", BLUE)
                    .line()
                    .line("通常情况下，你应该只强化至少{神话}稀有度的装备", GOLD))
            .task(Task.item(Adventure.Items.AUGMENTING_TABLE.get()))
            .reward(Reward.item(Items.NETHER_STAR), Reward.xpLevels(5))
            .dependsOn(MYTHIC_MATERIAL)
            .hideDependentLines());

    public static final Quest BOSS_SUMMONER = CHAPTER.quest("boss_summoner", q -> q
            .at(-2, 0)
            .subtitle("寰宇召临，弑神之机")
            .desc(d -> d
                    .line("{格雷科技远程召唤系统}：足不出户，{直接召唤BOSS}！", RED, DARK_RED)
                    .line()
                    .line("通电后自动刷新{神化BOSS}，推荐搭建一个牢房", GOLD)
                    .line()
                    .line("作为获取{玄奥沙}和{神铸珍珠}的唯一来源", DARK_PURPLE, GOLD)
                    .line()
                    .line("不同维度决定召唤上限：")
                    .line("{主世界}：{罕见} / {珍贵}", GREEN, DARK_GREEN, AQUA)
                    .line("{下界}：最高 {史诗}", RED, DARK_PURPLE)
                    .line("{末地}：最高 {神话}", DARK_PURPLE, GOLD))
            .task(Task.item(MultiBlockG.BOSS_SUMMONER))
            .reward(Reward.xpLevels(10))
            .dependsOn(UlvChapter.PRIMITIVE_ASSEMBLY));

    public static final Quest SOME_TIPS = CHAPTER.quest("some_tips", q -> q
            .at(-21, -5)
            .icon(BotaniaItems.questgiverMark)
            .title("一些小提示")
            .subtitle("为什么不看我呢?")
            .task(Task.checkmark().title("一些小提示")));

    public static final Quest WELCOME_TO_APOTHEOSIS = CHAPTER.quest("welcome_to_apotheosis", q -> q
            .at(-21, -1.5)
            .icon(Items.ENCHANTING_TABLE)
            .title("{欢迎来到神化}", DARK_PURPLE)
            .subtitle("天地异变，神启新章")
            .desc(d -> d
                    .line("你发现了吗？这个世界的甘蔗和仙人掌都长得特别高")
                    .line()
                    .line("——别担心，你并没有误入{巨人国}", GRAY)
                    .line()
                    .line("在{神化}的影响下，这个世界{确实发生了改变}，而你也拥有着能充分利用它们的契机", DARK_PURPLE.bold(), LIGHT_PURPLE)
                    .line()
                    .line("通过本章节任务你将逐步了解{神化}的各种机制，并用它们去征服这个世界吧！", DARK_PURPLE.bold())
                    .line()
                    .line("{欢迎来到神化}", GOLD.bold().italic()))
            .task(Task.item(Items.ENCHANTING_TABLE))
            .reward(Reward.xpLevels(5))
            .dependsOn(SOME_TIPS)
            .hideDependencyLines().flexible());

    public static final Quest BOOKSHELF = CHAPTER.quest("bookshelf", q -> q
            .at(-17, 2)
            .subtitle("破凡桎而登极，化神域以触巅")
            .desc(d -> d
                    .line("在{神化}世界中，原版书架不过是凡俗的{起点}，它将附魔之力禁锢在{30级}的巅峰。但这绝非终点，而是你征程的{开端}", DARK_PURPLE.bold(), DARK_GREEN, DARK_RED, DARK_GREEN)
                    .line()
                    .line("在神化的加持下，冲破凡界的极限，让附魔等级攀升至前所未有的{100级}，触摸神之领域", DARK_PURPLE))
            .task(Task.item(Items.BOOKSHELF))
            .reward(Reward.item(Items.LAPIS_LAZULI, 16))
            .dependsOn(WELCOME_TO_APOTHEOSIS));

    public static final Quest ABOUT_ANVILS = CHAPTER.quest("about_anvils", q -> q
            .at(-22.5, -4.5)
            .icon(Items.ANVIL)
            .title("关于铁砧")
            .desc(d -> d
                    .line("{拆分}是铁砧的专属附魔", BLUE)
                    .line()
                    .line("有了{拆分铁砧}， 多魔咒附魔书不再是“捆绑销售”", BLUE)
                    .line()
                    .line("把附魔书丢在地上， 再让铁砧从高处落下——")
                    .line()
                    .line("剩下的事，交给重力"))
            .task(Task.checkmark().title("关于铁砧"))
            .dependsOn(SOME_TIPS));

    public static final Quest OBLITERATION = CHAPTER.quest("obliteration", q -> q
            .at(-23.5, -4)
            .iconStack(() -> QuestStacks.enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 8))
            .title("分裂")
            .desc(d -> d
                    .line("{分裂}是铁砧的专属附魔。", BLUE)
                    .line()
                    .line("有了{分裂铁砧}，一个魔咒，也能“一分为二”。", BLUE)
                    .line()
                    .line("把附魔书丢在地上，再让铁砧从高处落下——")
                    .line()
                    .line("剩下的事，交给重力。"))
            .task(Task.checkmark().title("分裂"))
            .dependsOn(ABOUT_ANVILS));

    public static final Quest SPLITTING = CHAPTER.quest("splitting", q -> q
            .at(-23.5, -5)
            .iconStack(() -> QuestStacks.enchantedBook(Enchantments.ALL_DAMAGE_PROTECTION, 8))
            .title("拆分")
            .desc(d -> d
                    .line("{拆分}是铁砧的专属附魔。", BLUE)
                    .line()
                    .line("有了{拆分铁砧}，多魔咒附魔书不再是“捆绑销售”。", BLUE)
                    .line()
                    .line("把附魔书丢在地上，再让铁砧从高处落下")
                    .line()
                    .line("——剩下的事，交给重力。"))
            .task(Task.checkmark().title("拆分"))
            .dependsOn(ABOUT_ANVILS));

    public static final Quest TREASURE_TOME_SECRETS = CHAPTER.quest("treasure_tome_secrets", q -> q
            .at(-19, -1.5)
            .title("宝典秘籍")
            .desc(d -> d.line("{宝典}可以像书一样被{附魔}，但只能接受其{特定}的魔咒", GOLD, DARK_PURPLE, DARK_RED))
            .task(Task.item(Items.BOOK))
            .dependsOn(WELCOME_TO_APOTHEOSIS));

    public static final Quest TOME_OF_WEAPONRY = CHAPTER.quest("tome_of_weaponry", q -> q
            .at(-18, -2.5)
            .title("武器宝典")
            .task(Task.anyOf(1, Ench.Items.WEAPON_TOME.get(), Ench.Items.BOW_TOME.get()).title("任意武器宝典"))
            .dependsOn(TREASURE_TOME_SECRETS));

    public static final Quest TOOLS_ENCYCLOPEDIA = CHAPTER.quest("tools_encyclopedia", q -> q
            .at(-17, -2.5)
            .icon(Ench.Items.PICKAXE_TOME.get())
            .title("工具宝典")
            .task(Task.item(Ench.Items.PICKAXE_TOME.get()))
            .dependsOn(TOME_OF_WEAPONRY));

    public static final Quest ARMOR_COMPENDIUM = CHAPTER.quest("armor_compendium", q -> q
            .at(-16, -2.5)
            .title("护甲宝典")
            .task(
                    Task.anyOf(1, Ench.Items.HELMET_TOME.get(), Ench.Items.CHESTPLATE_TOME.get(), Ench.Items.LEGGINGS_TOME.get(), Ench.Items.BOOTS_TOME.get()).title("任意护甲宝典"))
            .dependsOn(TOOLS_ENCYCLOPEDIA));

    public static final Quest SCRAP_TOME = CHAPTER.quest("scrap_tome", q -> q
            .at(-18, -0.5)
            .desc(d -> d
                    .line("{拆解宝典}是一种从物品中获取魔咒的方法", GOLD)
                    .line()
                    .line("再铁砧中把他和另一件物品结合，会销毁物品并{随机}做出一本带有一些魔咒的附魔书", DARK_RED))
            .task(Task.item(Ench.Items.SCRAP_TOME.get()))
            .dependsOn(TREASURE_TOME_SECRETS));

    public static final Quest IMPROVED_SCRAP_TOME = CHAPTER.quest("improved_scrap_tome", q -> q
            .at(-17, -0.5)
            .desc(d -> d.line("{高级拆解宝典}能够提取物品上的{所有}附魔", GOLD, DARK_RED))
            .task(Task.item(Ench.Items.IMPROVED_SCRAP_TOME.get()))
            .dependsOn(SCRAP_TOME));

    public static final Quest EXTRACTION_TOME = CHAPTER.quest("extraction_tome", q -> q
            .at(-16, -0.5)
            .desc(d -> d.line("{提取宝典}能够提取物品上的所有附魔，并且{不会销毁}该物品", GOLD, DARK_RED))
            .task(Task.item(Ench.Items.EXTRACTION_TOME.get()))
            .dependsOn(IMPROVED_SCRAP_TOME));

    public static final Quest HELLSHELF = CHAPTER.quest("hellshelf", q -> q
            .at(-14.5, 0)
            .task(Task.item(Ench.Items.HELLSHELF.get()))
            .reward(Reward.item(Ench.Items.HELLSHELF.get()), Reward.xpLevels(1))
            .dependsOn(BOOKSHELF));

    public static final Quest INFUSED_HELLSHELF = CHAPTER.quest("infused_hellshelf", q -> q
            .at(-13, 0)
            .task(Task.item(Ench.Items.INFUSED_HELLSHELF.get()))
            .reward(Reward.item(Ench.Items.INFUSED_HELLSHELF.get()), Reward.xpLevels(1))
            .dependsOn(HELLSHELF));

    public static final Quest BLAZING_HELLSHELF = CHAPTER.quest("blazing_hellshelf", q -> q
            .at(-11.5, 0)
            .task(Task.item(Ench.Items.BLAZING_HELLSHELF.get()))
            .reward(Reward.item(Ench.Items.BLAZING_HELLSHELF.get()), Reward.xpLevels(5))
            .dependsOn(INFUSED_HELLSHELF));

    public static final Quest GLOWING_HELLSHELF = CHAPTER.quest("glowing_hellshelf", q -> q
            .at(-10, 0)
            .task(Task.item(Ench.Items.GLOWING_HELLSHELF.get()))
            .reward(Reward.item(Ench.Items.GLOWING_HELLSHELF.get()), Reward.xpLevels(5))
            .dependsOn(BLAZING_HELLSHELF));

    public static final Quest SIGHTSHELF = CHAPTER.quest("sightshelf", q -> q
            .at(-12.5, -1)
            .task(Task.item(Ench.Items.SIGHTSHELF.get()))
            .reward(Reward.item(Ench.Items.SIGHTSHELF.get()), Reward.xpLevels(5))
            .dependsOn(INFUSED_HELLSHELF));

    public static final Quest SIGHTSHELF_T2 = CHAPTER.quest("sightshelf_t2", q -> q
            .at(-13.5, -1)
            .task(Task.item(Ench.Items.SIGHTSHELF_T2.get()))
            .reward(Reward.item(Ench.Items.SIGHTSHELF_T2.get()), Reward.xpLevels(5))
            .dependsOn(INFUSED_HELLSHELF));

    public static final Quest BOSS_ITEM = CHAPTER.quest("boss_item", q -> q
            .at(-21, -7)
            .icon(Items.NETHERITE_CHESTPLATE)
            .title("Boss物品")
            .desc(d -> d
                    .line("每个Boss都携带着一件独特的{词缀装备}作为战利品", DARK_PURPLE)
                    .line()
                    .line("这些装备不仅是击败Boss的证明，更是它们生前身份的象征")
                    .line()
                    .line("——若无意收藏，可将它置于回收台，分解转化成珍宝材料"))
            .task(Task.checkmark())
            .dependsOn(SOME_TIPS));

    public static final Quest WANDERING_TETHER = CHAPTER.quest("wandering_tether", q -> q
            .at(-19.5, -4.5)
            .icon(Items.LEAD)
            .title("流浪拴绳")
            .desc(d -> d
                    .line("流浪商人，{又称流浪拴绳}，通常它们只卖废品", STRIKE)
                    .line()
                    .line("但在神化的加持下，它们的货架上出现了好多珍贵好物！")
                    .line()
                    .line("你可以买到{信标}，{不死图腾}，和带有强力{附魔}的装备", BLUE, DARK_GREEN, YELLOW))
            .task(Task.checkmark())
            .dependsOn(SOME_TIPS));

    public static final Quest SEASHELF = CHAPTER.quest("seashelf", q -> q
            .at(-14.5, 4)
            .task(Task.item(Ench.Items.SEASHELF.get()))
            .reward(Reward.item(Ench.Items.SEASHELF.get()), Reward.xpLevels(5))
            .dependsOn(BOOKSHELF));

    public static final Quest INFUSED_SEASHELF = CHAPTER.quest("infused_seashelf", q -> q
            .at(-13, 4)
            .task(Task.item(Ench.Items.INFUSED_SEASHELF.get()))
            .reward(Reward.item(Ench.Items.INFUSED_SEASHELF.get()), Reward.xpLevels(5))
            .dependsOn(SEASHELF));

    public static final Quest CRYSTAL_SEASHELF = CHAPTER.quest("crystal_seashelf", q -> q
            .at(-11.5, 4)
            .task(Task.item(Ench.Items.CRYSTAL_SEASHELF.get()))
            .reward(Reward.item(Ench.Items.CRYSTAL_SEASHELF.get()), Reward.xpLevels(5))
            .dependsOn(INFUSED_SEASHELF));

    public static final Quest HEART_SEASHELF = CHAPTER.quest("heart_seashelf", q -> q
            .at(-10, 4)
            .task(Task.item(Ench.Items.HEART_SEASHELF.get()))
            .reward(Reward.item(Ench.Items.HEART_SEASHELF.get()), Reward.xpLevels(5))
            .dependsOn(CRYSTAL_SEASHELF));

    public static final Quest DORMANT_DEEPSHELF = CHAPTER.quest("dormant_deepshelf", q -> q
            .at(-8, 2)
            .task(Task.item(Ench.Items.DORMANT_DEEPSHELF.get()))
            .reward(Reward.item(Ench.Items.DORMANT_DEEPSHELF.get()), Reward.xpLevels(10))
            .dependsOn(GLOWING_HELLSHELF, HEART_SEASHELF));

    public static final Quest DEEPSHELF = CHAPTER.quest("deepshelf", q -> q
            .at(-6, 2)
            .task(Task.item(Ench.Items.DEEPSHELF.get()))
            .reward(Reward.item(Ench.Items.DEEPSHELF.get()), Reward.xpLevels(10))
            .dependsOn(DORMANT_DEEPSHELF));

    public static final Quest ECHOING_DEEPSHELF = CHAPTER.quest("echoing_deepshelf", q -> q
            .at(-6, 3.5)
            .task(Task.item(Ench.Items.ECHOING_DEEPSHELF.get()))
            .reward(Reward.item(Ench.Items.ECHOING_DEEPSHELF.get()), Reward.xpLevels(10))
            .dependsOn(DEEPSHELF));

    public static final Quest ECHOING_SCULKSHELF = CHAPTER.quest("echoing_sculkshelf", q -> q
            .at(-4, 3.5)
            .task(Task.item(Ench.Items.ECHOING_SCULKSHELF.get()))
            .reward(Reward.item(Ench.Items.ECHOING_SCULKSHELF.get()), Reward.xpLevels(10))
            .dependsOn(ECHOING_DEEPSHELF));

    public static final Quest SOUL_TOUCHED_DEEPSHELF = CHAPTER.quest("soul_touched_deepshelf", q -> q
            .at(-4, 2)
            .task(Task.item(Ench.Items.SOUL_TOUCHED_DEEPSHELF.get()))
            .reward(Reward.item(Ench.Items.SOUL_TOUCHED_DEEPSHELF.get()), Reward.xpLevels(10))
            .dependsOn(DEEPSHELF));

    public static final Quest Q_571EA614 = CHAPTER.quest("q_571ea614", q -> q
            .at(-19.5, -5.5)
            .icon(Apoth.Items.ENDER_LEAD.get())
            .desc(d -> d
                    .line("{末影拴绳}可以帮助你轻松捕捉动物", DARK_PURPLE)
                    .line()
                    .line("用拴绳{左击}一个动物来捕捉它，然后{右击}释放", GREEN, GREEN))
            .task(Task.checkmark().title("末影拴绳"))
            .dependsOn(SOME_TIPS));

    public static final Quest OBTAIN_GEMS = CHAPTER.quest("obtain_gems", q -> q
            .at(-17, -5)
            .iconStack(() -> QuestExternalRefs.mageslayerGem())
            .title("获取宝石")
            .subtitle("珍宝天成，杀伐可得")
            .desc(d -> d.line("{宝石}可在箱子的战利品中找到，也能通过击杀怪物随机掉落", LIGHT_PURPLE))
            .task(Task.checkmark().title("获取宝石"))
            .dependsOn(WELCOME_TO_APOTHEOSIS));

    public static final Quest GEM_DUST = CHAPTER.quest("gem_dust", q -> q
            .at(-14, -5)
            .title("宝石粉")
            .subtitle("砧落晶华，粉铸根基")
            .desc(d -> d
                    .line("{宝石粉}可以通过用铁砧砸落{宝石}获得，这是不可或缺的材料", DARK_GREEN, BLUE)
                    .line()
                    .line("另一种获取途径是使用{宝石锻炉}，不过那就属于后话了", LIGHT_PURPLE))
            .task(Task.item(Adventure.Items.GEM_DUST.get()))
            .reward(Reward.item(Adventure.Items.GEM_DUST.get()))
            .dependsOn(OBTAIN_GEMS));

    public static final Quest SIMPLE_REFORGING_TABLE = CHAPTER.quest("simple_reforging_table", q -> q
            .at(-12, -4)
            .subtitle("易缀焕新生，粹奇珍铸华章")
            .desc(d -> d
                    .line("使用{珍宝材料}和{新生符石}来改变装备词缀", GREEN, GREEN)
                    .line()
                    .line("它允许你重铸稀有度高达{珍贵}的物品", YELLOW))
            .task(Task.item(Adventure.Items.SIMPLE_REFORGING_TABLE.get()))
            .reward(Reward.item(Adventure.Items.GEM_DUST.get(), 16))
            .dependsOn(GEM_DUST));

    public static final Quest REFORGING_TABLE = CHAPTER.quest("reforging_table", q -> q
            .at(-12, -3)
            .subtitle("淬炼圣器，铸魂涅槃")
            .desc(d -> d.line("要重铸{史诗}或更高稀有度的装备 ，你需要{重铸台}", GOLD, DARK_PURPLE))
            .task(Task.item(Adventure.Items.REFORGING_TABLE.get()))
            .reward(Reward.item(Adventure.Items.GEM_DUST.get(), 32))
            .dependsOn(SIMPLE_REFORGING_TABLE));

    public static final Quest GEM_CUTTING_TABLE = CHAPTER.quest("gem_cutting_table", q -> q
            .at(-12, -6)
            .subtitle("精琢奇珍，粹炼升华")
            .desc(d -> d.line("{宝石切割台}用于升级{宝石}，升级宝石需要消耗{宝石粉}、 {珍宝材料}和第二个相同稀有度的宝石", LIGHT_PURPLE, BLUE, GREEN, GREEN))
            .task(Task.item(Adventure.Items.GEM_CUTTING_TABLE.get()))
            .reward(Reward.item(Adventure.Items.GEM_DUST.get(), 16))
            .dependsOn(GEM_DUST));

    public static final Quest SMITHING_TABLE = CHAPTER.quest("smithing_table", q -> q
            .at(-12, -7)
            .subtitle("神工铸台，宝石赋能")
            .desc(d -> d.line("想要镶嵌宝石需要用到原版的{锻造台}", DARK_PURPLE))
            .task(Task.item(Items.SMITHING_TABLE))
            .reward(Reward.item(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.MV]))
            .dependsOn(GEM_DUST));

    public static final Quest SALVAGING_TABLE = CHAPTER.quest("salvaging_table", q -> q
            .at(-12, -5)
            .subtitle("解构珍奇，还源归真")
            .desc(d -> d
                    .line("用于将词缀物品分解为其相对应的{珍宝材料}", DARK_GREEN)
                    .line()
                    .line("装备越稀有，产出的材料越珍贵")
                    .line()
                    .line("稀有度 → 分解产物：")
                    .line("{普通（灰字）} → {神秘废金属}", GRAY, GRAY)
                    .line("{罕见（绿字）} → {陈旧布匹}", DARK_GREEN, DARK_GREEN)
                    .line("{珍贵（蓝字）} → {发光水晶碎片}", AQUA, AQUA)
                    .line("{史诗（紫字）} → {玄奥沙}", DARK_PURPLE, DARK_PURPLE)
                    .line("{神话（橙字）} → {神铸珍珠}", GOLD, GOLD))
            .task(Task.item(Adventure.Items.SALVAGING_TABLE.get()))
            .reward(Reward.item(Adventure.Items.GEM_DUST.get(), 8))
            .dependsOn(GEM_DUST));

    public static final Quest REMOVE_ENCHANTMENT = CHAPTER.quest("remove_enchantment", q -> q
            .at(-22.5, -5.5)
            .icon(Ench.Items.PRISMATIC_WEB.get())
            .title("移除魔咒")
            .desc(d -> d
                    .line("有时候，最原始的方法反而最有效")
                    .line()
                    .line("{蜘蛛网}会抹去物品上的{全部魔咒}", BOLD, DARK_RED)
                    .line()
                    .line("而{海晶蜘蛛网}，只针对那些{不该存在的诅咒}", DARK_AQUA, DARK_RED))
            .task(Task.checkmark().title("移除魔咒"))
            .dependsOn(SOME_TIPS));

    public static final Quest SOUL_TOUCHED_SCULKSHELF = CHAPTER.quest("soul_touched_sculkshelf", q -> q
            .at(-2, 3)
            .task(Task.item(Ench.Items.SOUL_TOUCHED_SCULKSHELF.get()))
            .reward(Reward.item(Ench.Items.SOUL_TOUCHED_SCULKSHELF.get()), Reward.xpLevels(10))
            .dependsOn(SOUL_TOUCHED_DEEPSHELF, ECHOING_SCULKSHELF)
            .requireAllStarted());

    public static final Quest ENDSHELF = CHAPTER.quest("endshelf", q -> q
            .at(-0.5, 3)
            .task(Task.item(Ench.Items.ENDSHELF.get()))
            .reward(Reward.item(Ench.Items.ENDSHELF.get()), Reward.xpLevels(15))
            .dependsOn(SOUL_TOUCHED_SCULKSHELF));

    public static final Quest DRACONIC_ENDSHELF = CHAPTER.quest("draconic_endshelf", q -> q
            .at(1, 2)
            .task(Task.item(Ench.Items.DRACONIC_ENDSHELF.get()))
            .reward(Reward.item(Ench.Items.DRACONIC_ENDSHELF.get()), Reward.xpLevels(15))
            .dependsOn(ENDSHELF));

    public static final Quest PEARL_ENDSHELF = CHAPTER.quest("pearl_endshelf", q -> q
            .at(1, 4)
            .task(Task.item(Ench.Items.PEARL_ENDSHELF.get()))
            .reward(Reward.item(Ench.Items.PEARL_ENDSHELF.get()), Reward.xpLevels(15))
            .dependsOn(ENDSHELF));

    public static final Quest PERFECT_ENCHANTMENT = CHAPTER.quest("perfect_enchantment", q -> q
            .at(2.5, 3)
            .title("完美附魔")
            .subtitle("极境未至，神案天工")
            .desc(d -> d
                    .line("{恭喜达成终极目标！}你已构筑出一套{完美的附魔体系}。", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{......什么？尚未完成？}", DARK_GREEN)
                    .line()
                    .line("那么，这里有一套经过验证的{终极方案}：", BLUE)
                    .line()
                    .line("{水之过滤海洋书架} ×1", AQUA)
                    .line("{巨龙末地书架} ×1", DARK_PURPLE)
                    .line("{精湛视界书架} ×1", LIGHT_PURPLE)
                    .line("{末地聚合书架} ×4", BLUE)
                    .line("{回响幽匿书架} ×4", DARK_AQUA)
                    .line("{璀璨末地书架} ×4", LIGHT_PURPLE)
                    .line("{灵魂触摸幽匿书架} ×2", DARK_GRAY)
                    .line("{任意头颅} ×2", GRAY)
                    .line()
                    .line("这套方案能够{提供}：", GREEN)
                    .line("{50 位阶}｜{100% 量子化}｜{100% 阿卡那}｜{100% 校准}｜{7 条魔咒线索}", GOLD, AQUA, LIGHT_PURPLE, GREEN, YELLOW))
            .task(Task.checkmark())
            .reward(Reward.xpLevels(30))
            .dependsOn(DRACONIC_ENDSHELF, PEARL_ENDSHELF));

    public static final Quest NEGATIVE_VALUE_ADJUSTMENT = CHAPTER.quest("negative_value_adjustment", q -> q
            .at(-14.5, 2)
            .title("负值调节")
            .task(Task.anyOf(1, Ench.Items.BEESHELF.get(), Ench.Items.MELONSHELF.get(), Ench.Items.STONESHELF.get()).title("任意调节书架"))
            .reward(Reward.xpLevels(5))
            .dependsOn(BOOKSHELF));

    public static final Quest RECTIFICATION_ADJUSTMENT = CHAPTER.quest("rectification_adjustment", q -> q
            .at(-13, 2)
            .title("校准调节")
            .task(Task.anyOf(1, Ench.Items.RECTIFIER.get(), Ench.Items.RECTIFIER_T2.get(), Ench.Items.RECTIFIER_T3.get()).title("任意校准书架"))
            .reward(Reward.xpLevels(5))
            .dependsOn(BOOKSHELF));

    public static final Quest SPECIAL_BOOKSHELF = CHAPTER.quest("special_bookshelf", q -> q
            .at(-11.5, 2)
            .title("特殊书架")
            .task(Task.anyOf(1, Ench.Items.FILTERING_SHELF.get(), Ench.Items.TREASURE_SHELF.get(), Ench.Items.ENDER_LIBRARY.get()).title("任意特殊书架"))
            .reward(Reward.xpLevels(5))
            .dependsOn(BOOKSHELF)
            .hideDependentLines());

    public static final Quest SPAWN_EGG = CHAPTER.quest("spawn_egg", q -> q
            .at(-22, -6.5)
            .icon(Items.ALLAY_SPAWN_EGG)
            .title("刷怪蛋")
            .desc(d -> d
                    .line("通常情况下是{无法获取}刷怪蛋的，但我们可以通过给武器附上“捕获”魔咒，可以使怪物掉落对应的{刷怪蛋}", DARK_RED, DARK_GREEN)
                    .line()
                    .line("这枚刷怪蛋，其真正用途在于{改造刷怪笼}", DARK_RED)
                    .line()
                    .line("——右击刷怪笼为其注入新的生命，使其生成对应的怪物。"))
            .task(Task.checkmark().title("如何获得刷怪蛋"))
            .dependsOn(SOME_TIPS));

    public static final Quest THE_PRIMORDIAL_RECONSTRUCTOR = CHAPTER.quest("the_primordial_reconstructor", q -> q
            .at(-9, -5)
            .subtitle("元枢衍万法，一器掌天工")
            .desc(d -> d
                    .line("一台机器解决神化的所有问题")
                    .line()
                    .line("可以产出{附魔精粹}和{刻印精粹}", GOLD, GOLD))
            .task(Task.item(ManaMultiBlock.THE_PRIMORDIAL_RECONSTRUCTOR))
            .reward(Reward.xpLevels(100))
            .dependsOn(
                    SPECIAL_BOOKSHELF,
                    REFORGING_TABLE,
                    SALVAGING_TABLE,
                    GEM_CUTTING_TABLE,
                    SMITHING_TABLE,
                    AUGMENTING_TABLE,
                    Magic2Chapter.GJALLAR_HORN_FULL));

    public static final Quest AMULET = CHAPTER.quest("amulet", q -> q
            .at(-20, -6.5)
            .iconStack(() -> QuestStacks.potion(Apoth.Items.POTION_CHARM.get().asItem(), Potions.NIGHT_VISION))
            .title("护身符")
            .desc(d -> d
                    .line("{格雷科技}员工终于想通了：与其不停{喝药}，不如让{药水}自己干活。", RED, LIGHT_PURPLE, BLUE)
                    .line()
                    .line("护符在{启用}状态下会释放药水效果，代价只是一些{耐久}", GOLD, GRAY))
            .task(Task.checkmark().title("护身符"))
            .dependsOn(SOME_TIPS));
}
