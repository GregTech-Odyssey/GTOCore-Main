package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOAEParts;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestRewardTables;

import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;
import net.xalcon.torchmaster.common.ModBlocks;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.naturescompass.NaturesCompass;
import com.hepdd.gtmthings.data.CustomItems;
import commoble.jumbofurnace.JumboFurnace;
import earth.terrarium.adastra.common.registry.ModItems;
import vazkii.botania.common.item.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class MainStoneageChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("main_stoneage", c -> c
            .title("1. 石器时代(Stone Age)")
            .subtitle("Hello World!")
            .icon(Items.GRASS_BLOCK)
            .hideQuestsUntilDepsVisible());

    public static final Quest PROSPECTOR = CHAPTER.quest("prospector", q -> q
            .at(-3, 0)
            .title("淘金客")
            .subtitle("西部大开发")
            .desc(d -> d.line("你要问为什么不是先砍树——你砍得动吗？"))
            .task(Task.item(Items.GRAVEL, 64))
            .reward(Reward.item(Items.FLINT, 32), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(GregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest WHERE_ARE_MY_TOOLS = CHAPTER.quest("where_are_my_tools", q -> q
            .at(-1.5, -1.5)
            .icon(Items.WOODEN_AXE)
            .title("我的工具呢？")
            .subtitle("唉......")
            .desc(d -> d
                    .line("所有的木制和石制工具都被禁用了，所以你得从燧石工具开始做起。")
                    .line()
                    .line("你需要做出第一个{燧石斧}才能砍树，并且不用工作台做的{燧石斧}只有16点耐久", GOLD, GOLD))
            .task(Task.checkmark())
            .reward(Reward.item(Items.BREAD).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 4))
            .dependsOn(PROSPECTOR)
            .requireAllStarted()
            .hideDependencyLines());

    public static final Quest MORE_WOODEN_STICKS = CHAPTER.quest("more_wooden_sticks", q -> q
            .at(-3, -1.5)
            .icon(Items.DIRT)
            .title("来点木棍")
            .subtitle("小福利")
            .desc(d -> d.line("整点泥土来，木棍收好嗷"))
            .task(Task.tag(ItemTags.DIRT, 16).title("任意泥土*16"))
            .reward(Reward.item(Items.STICK, 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(GregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest FLINT_KNIFE = CHAPTER.quest("flint_knife", q -> q
            .at(-1.5, 0)
            .subtitle("樵夫乐事")
            .desc(d -> d.line("用小刀打草概率掉落植物纤维，空手破坏树叶概率掉落木棍"))
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.FLINT_KNIFE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WHERE_ARE_MY_TOOLS, PROSPECTOR));

    public static final Quest PLANT_FIBER = CHAPTER.quest("plant_fiber", q -> q
            .at(0, 0)
            .subtitle("这可不能做成蓑衣")
            .desc(d -> d.line("你需要收集一些"))
            .task(Task.item(GTOItems.PLANT_FIBER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(FLINT_KNIFE));

    public static final Quest FLINT_AXE = CHAPTER.quest("flint_axe", q -> q
            .at(1.5, 0)
            .subtitle("燧石大力量")
            .desc(d -> d.line("制造一把{燧石斧}，放开了去砍吧！", GOLD))
            .task(Task.item(ToolHelper.get(GTToolType.AXE, GTMaterials.Flint).getItem()))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(PLANT_FIBER));

    public static final Quest WANT_TO_GET_RICH_CHOP_TREES = CHAPTER.quest("want_to_get_rich_chop_trees", q -> q
            .at(3.5, 0)
            .icon(Items.OAK_LOG)
            .title("想致富，先撸树！")
            .subtitle("有种熟悉的感觉")
            .desc(d -> d.line("有木头才能继续推进流程，如果你发现你砍不了树，不妨去看看前置任务"))
            .task(Task.tag(ItemTags.LOGS, 64).title("任意原木"))
            .reward(
                    Reward.choice(QuestRewardTables.PLANKS).excludeFromClaimAll().title("任意木板*64"),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(FLINT_AXE));

    public static final Quest DALLYING_WITH_PLANTS = CHAPTER.quest("dallying_with_plants", q -> q
            .at(4.5, -1)
            .icon(GTBlocks.RUBBER_SAPLING)
            .title("沾花惹草")
            .subtitle("“不过就是做了一些该做的事情嘛”")
            .desc(d -> d
                    .line("你需要收集这些植物来为后续发展打下基础")
                    .line()
                    .line("1. {橡胶树}跟工业2版本的有很大区别，{树脂}是随着原木或树叶一起掉落的。", GOLD, GOLD)
                    .line()
                    .line("2. {甘蔗}主要用于造纸，当然在 GregTech 整合包造纸的方式与原版 Minecraft 有些区别。", GOLD)
                    .line()
                    .line("建议先预留一块地方来种植这些作物。"))
            .task(Task.item(Items.SUGAR_CANE, 16), Task.item(GTBlocks.RUBBER_SAPLING, 4))
            .reward(Reward.item(Items.BONE_MEAL, 64))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES));

    public static final Quest EAT_WELL_TO_KEEP_GOING = CHAPTER.quest("eat_well_to_keep_going", q -> q
            .at(4.5, 1)
            .icon(QuestExternalRefs.FarmersDelightItems.CABBAGE)
            .title("吃饱饭才能加油干")
            .subtitle("民以食为天")
            .desc(d -> d
                    .line("FTBUtimine 的连锁挖掘功能默认消耗饱食度，所以吃饱肚子还是很关键的。")
                    .line()
                    .line("搞点简单的农业产出是个很不错的选择。")
                    .line()
                    .line("任选一项提交即可"))
            .task(Task.tag(Tags.Items.CROPS, 64).title("任意作物*64"), Task.tag(Tags.Items.SEEDS, 32).title("任意种子*32"))
            .reward(
                    Reward.item(Items.BREAD, 32).personal(),
                    Reward.item(AEItems.FLUIX_SWORD).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES));

    public static final Quest SWEET_TREAT_AFTER_WORK = CHAPTER.quest("sweet_treat_after_work", q -> q
            .at(2.5, -1)
            .icon(Items.SPRUCE_WOOD)
            .title("下班甜点~")
            .subtitle("彩蛋 #1")
            .desc(d -> d.line("这可能是你发现的第一个彩蛋任务"))
            .task(Task.tag(ItemTags.LOGS, 256L).title("任意原木"))
            .reward(Reward.item(Items.IRON_AXE).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES)
            .optional().hideDependencyLines().invisible());

    public static final Quest WORKBENCH = CHAPTER.quest("workbench", q -> q
            .at(6.5, 0)
            .icon(Items.CRAFTING_TABLE)
            .title("工作台")
            .subtitle("制作你的第一个工作台")
            .desc(d -> d
                    .line("它的配方与原版 Minecraft 有着截然不同的差距，你需要用两个{燧石}和两个{原木}。", GOLD, GOLD)
                    .line()
                    .line("你可以查看EMI来获得合成配方，然后在材料充足的情况下点击加号来自动合成。")
                    .line()
                    .line("{简单模式下配方不变，四个木板即可}", GRAY.italic()))
            .task(Task.item(Items.CRAFTING_TABLE))
            .reward(Reward.item(Items.TORCH, 64).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES));

    public static final Quest BACKPACK_TIME = CHAPTER.quest("backpack_time", q -> q
            .at(6.5, -2)
            .icon(QuestExternalRefs.SophisticatedBackpacksItems.BACKPACK)
            .title("背背背起了行囊")
            .subtitle("精妙背包")
            .desc(d -> d
                    .line("一开始你做的{基础背包}装不了多少东西，但也总算拯救了你的物品栏", GOLD)
                    .line()
                    .line("终端成型建造多方块结构的时候可以把装不下的方块仓室什么的放到背包里面")
                    .line()
                    .line("可以在{摩登时代}章节继续寻找升级的方式", link(BackpackChapter.Q_1CE34BD4)))
            .task(Task.item(Items.LEATHER, 4))
            .reward(
                    Reward.item(QuestExternalRefs.SophisticatedBackpacksItems.BACKPACK).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(WORKBENCH));

    public static final Quest DIG_THROUGH_THE_EARTH = CHAPTER.quest("dig_through_the_earth", q -> q
            .at(5, -2)
            .iconStack(() -> QuestStacks.mana(GTOItems.PROSPECTOR_MANA_ULV, 500000))
            .title("挖穿地心！")
            .subtitle("麻雀虽小")
            .desc(d -> d
                    .line("找找看附近有趣的东西，记得用魔力充能嗷！")
                    .line()
                    .line("可以去会员店买多一个，如果有需要的话"))
            .task(Task.item(Items.COBBLESTONE, 256L).consume())
            .reward(Reward.stack(() -> QuestStacks.mana(GTOItems.PROSPECTOR_MANA_ULV, 500000), 1).personal())
            .dependsOn(WORKBENCH));

    public static final Quest TIME_TO_MINE = CHAPTER.quest("time_to_mine", q -> q
            .at(11, 0)
            .icon(Items.DIAMOND_PICKAXE)
            .title("下矿时间到！")
            .subtitle("该来的还是要来")
            .desc(d -> d
                    .line("格雷科技的矿石都是以矿脉形式生成的，每个矿脉都有特定的矿石，{按U可查看矿脉生成信息}", YELLOW)
                    .line()
                    .line("你可以使用{探矿仪}扫描来寻找矿脉，也可以观察地表，地表上会有地表岩石，指示下方有什么矿脉", GOLD)
                    .line()
                    .line("你能连锁的{上限}取决于你的{工具等级}，等级越高能连锁的方块也就越多，如果能{通电}则能连锁更多的方块，详见提示章节", GREEN, YELLOW, YELLOW)
                    .line()
                    .line("你现在可以看看{矿石生成}章节内主世界矿物的分布了", link(OresChapter.GOOD_AT_USING_EMI_TO_CHECK))
                    .line()
                    .line("{别忘了领取前面任务给你发的魔力钢探矿仪}", GRAY.italic()))
            .task(Task.checkmark().title("那么该去下矿了！"))
            .reward(
                    Reward.item(ToolHelper.get(GTToolType.MINING_HAMMER, GTMaterials.HSSE).getItem()).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(WORKBENCH));

    public static final Quest OIL_MEDIUM_BUCKET = CHAPTER.quest("oil_medium_bucket", q -> q
            .at(12, -2)
            .icon(GTMaterials.RawOil.getBucket())
            .subtitle("是美军吸引器")
            .desc(d -> d
                    .line("恭喜你遇到了原油")
                    .line()
                    .line("拿桶舀一点吧，现在可能用不上但后面会需要用到的")
                    .line()
                    .line("等到了LV阶段就可以用基础泵来抽取原油泉了——你会需要原油并搭建石化产线"))
            .task(Task.item(GTMaterials.RawOil.getBucket()))
            .reward(Reward.item(Items.BUCKET, 4), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(TIME_TO_MINE));

    public static final Quest WHERE_IS_STEVE = CHAPTER.quest("where_is_steve", q -> q
            .at(13, 0)
            .title("史蒂夫去哪儿？")
            .subtitle("前期特别重要的矿石")
            .desc(d -> d
                    .line("铜矿和锡矿，你当前最重要的矿")
                    .line()
                    .line("如果附近没有的需要走远点看看，这个阶段挖掉一整个矿脉应该是够用了"))
            .task(
                    Task.anyOf(1, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Tin), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Cassiterite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.CassiteriteSand)).title("任意粗锡矿"),
                    Task.anyOf(1, Items.RAW_COPPER, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Chalcopyrite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Malachite)).title("任意粗铜矿"))
            .reward(
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.PICKAXE, GTMaterials.DamascusSteel), Enchantments.BLOCK_FORTUNE, 3), 1).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(TIME_TO_MINE));

    public static final Quest MOVING_MOUNTAINS = CHAPTER.quest("moving_mountains", q -> q
            .at(11, 4)
            .icon(Items.RAW_IRON)
            .title("愚公移山")
            .subtitle("当前重要的矿石")
            .desc(d -> d.line("你还需要这些，建议多挖点"))
            .task(Task.item(Items.IRON_INGOT, 64))
            .reward(
                    Reward.item(Items.CHEST, 16),
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.MINING_HAMMER, GTMaterials.DamascusSteel), Enchantments.BLOCK_FORTUNE, 3), 1).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16),
                    Reward.item(Items.IRON_INGOT, 64))
            .dependsOn(TIME_TO_MINE));

    public static final Quest OTHER_ORES = CHAPTER.quest("other_ores", q -> q
            .at(13, 2)
            .icon(Items.RAW_GOLD)
            .title("其他矿石")
            .subtitle("也来点")
            .desc(d -> d.line("这里有你其他需要挖的矿物，现阶段不是那么紧要，先备着些"))
            .task(
                    Task.item(Items.RAW_GOLD, 64),
                    Task.anyOf(64, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Galena), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Lead)).title("任意粗铅矿"),
                    Task.item(TagPrefix.rawOre, GTMaterials.Redstone, 64))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(TIME_TO_MINE));

    public static final Quest TOOL_UP = CHAPTER.quest("tool_up", q -> q
            .at(9, 4)
            .icon(ToolHelper.get(GTToolType.HARD_HAMMER, GTMaterials.Iron).getItem())
            .title("工欲善其事")
            .subtitle("必先利其器")
            .desc(d -> d
                    .line("这些工具主要用于工作台合成")
                    .line()
                    .line("大多数使用到工具的合成配方到后面你都能用相应的机器代替你完成，而且更加节省资源。但到那一步之前你得先手动合成一些东西")
                    .line()
                    .line("提供一些你现在能合成的工具，换一些好一点的工具"))
            .task(
                    Task.tag(CustomTags.HAMMERS, 1).consume().title("任意锻造锤"),
                    Task.tag(CustomTags.FILES, 1).consume().title("任意锉"),
                    Task.tag(CustomTags.MORTARS, 1).consume().title("任意研钵"))
            .reward(
                    Reward.item(ToolHelper.get(GTToolType.HARD_HAMMER, GTMaterials.TungstenSteel).getItem()).personal(),
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.MORTAR, GTMaterials.CobaltBrass), Enchantments.UNBREAKING, 3), 1).personal(),
                    Reward.item(ToolHelper.get(GTToolType.FILE, GTMaterials.TungstenSteel).getItem()).personal(),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(MOVING_MOUNTAINS));

    public static final Quest Q_514A8AAF = CHAPTER.quest("q_514a8aaf", q -> q
            .at(10, -2)
            .subtitle("谁才是真正的“油”戏王？")
            .desc(d -> d
                    .line("这是Ad Astra的石油井，这种结构只在海上生成")
                    .line()
                    .line("如果你找不到Gregtech原版的油泉，抽它也可以")
                    .line()
                    .line("{注意石油和原油的区别，你应该会查看EMI}", GRAY.italic()))
            .task(Task.structure(QuestExternalRefs.OIL_WELL).title("海上黑金").icon(ModItems.OIL_BUCKET.get()))
            .reward(Reward.item(Items.BUCKET, 4), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(TIME_TO_MINE));

    public static final Quest LOOKING_FOR_A_LUSH_CAVE = CHAPTER.quest("looking_for_a_lush_cave", q -> q
            .at(4.5, 2.5)
            .icon(Items.OAK_SAPLING)
            .title("在找繁茂洞穴？")
            .subtitle("彩蛋 #2")
            .task(Task.tag(ItemTags.SAPLINGS, 32).title("任意树苗"))
            .reward(
                    Reward.item(NaturesCompass.naturesCompass),
                    Reward.item(AEItems.METEORITE_COMPASS),
                    Reward.item(ExplorersCompass.explorersCompass),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES)
            .optional().hideDependencyLines().invisible());

    public static final Quest ANY_BUTCHERY_KNIVES = CHAPTER.quest("any_butchery_knives", q -> q
            .at(2.5, 2.5)
            .iconStack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.BUTCHERY_KNIFE, GTMaterials.Iron), Enchantments.MOB_LOOTING, 3))
            .subtitle("免费的抢夺Ⅲ")
            .desc(d -> d.line("也许你需要准备一个小牧场来产出食物"))
            .task(Task.tag(CustomTags.BUTCHERY_KNIVES, 1).title("屠刀"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MOVING_MOUNTAINS)
            .optional().hideDependencyLines());

    public static final Quest AS_FOR_ANIMALS_MAY_THEY_ALL = CHAPTER.quest("as_for_animals_may_they_all", q -> q
            .at(2.5, 1)
            .icon(Items.BEEF)
            .title("至于动物？我祝他们一切安好")
            .subtitle("是的，我肚子饿了")
            .desc(d -> d.line("兜兜转转大半天可能还是去获取点肉来能填饱肚子"))
            .task(Task.item(Items.LEATHER, 16))
            .reward(Reward.item(Items.COOKED_BEEF, 64).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WANT_TO_GET_RICH_CHOP_TREES, ANY_BUTCHERY_KNIVES)
            .requireAny());

    public static final Quest WOOD_MALLET = CHAPTER.quest("wood_mallet", q -> q
            .at(8, 2)
            .subtitle("其实是个开关")
            .desc(d -> d
                    .line("软锤不仅可以作为合成材料，也可以作为工具")
                    .line()
                    .line("{右键}机器即可暂停机器工作，{潜行右键}机器可以让机器在{运行完当前配方之后}再暂停工作", AQUA, AQUA, YELLOW))
            .task(Task.item(ToolHelper.get(GTToolType.SOFT_MALLET, GTMaterials.Wood).getItem()))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WORKBENCH)
            .optional());

    public static final Quest ANY_BEDS = CHAPTER.quest("any_beds", q -> q
            .at(9.75, 2)
            .icon(Items.WHITE_BED)
            .subtitle("高科技时间折跃装置")
            .desc(d -> d.line("这也许是防止遭受怪物袭击最好的手段"))
            .task(Task.tag(ItemTags.BEDS, 1).title("床"))
            .dependsOn(WOOD_MALLET, UtilChapter.ANY_BEDS)
            .requireAnyStarted());

    public static final Quest GRASS_HARVESTER = CHAPTER.quest("grass_harvester", q -> q
            .at(8, -2)
            .subtitle("又名种子收割刀")
            .desc(d -> d.line("右击{草}极大概率掉落小麦种子，有小概率掉落胡萝卜等农作物", GOLD))
            .task(Task.item(GTOItems.GRASS_HARVESTER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WORKBENCH));

    public static final Quest NEED_DYE = CHAPTER.quest("need_dye", q -> q
            .at(13, 6)
            .title("需要染料？")
            .subtitle("拒绝辣手摧花")
            .desc(d -> d.line("你可以使用{农夫乐事}模组的{砧板}获得它，在这里你不能徒手分解花朵！", YELLOW, GOLD))
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CUTTING_BOARD), Task.tag(QuestExternalRefs.TAG_C_DYES, 1).title("任意染料"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(FLINT_KNIFE)
            .hideDependencyLines());

    public static final Quest A_FULL_ARSENAL = CHAPTER.quest("a_full_arsenal", q -> q
            .at(11, 6)
            .icon(ToolHelper.get(GTToolType.WRENCH, GTMaterials.Iron).getItem())
            .title("琳琅满目")
            .subtitle("真是个十八般兵器全上阵")
            .desc(d -> d
                    .line("扳手：    右键点击可以{设置GT机器的端口}，而潜行右键点击可以{旋转机器}，也可以代替镐子破坏机器", YELLOW.bold(), YELLOW.bold())
                    .line("螺丝刀：可以{打开机器覆盖板}", YELLOW.bold())
                    .line("{撬棍：     可以拆下覆盖板}", YELLOW.bold())
                    .line("剪线钳： 可以{连接线缆}", YELLOW.bold())
                    .line()
                    .line("你可以查看提示章节的{GT工具使用手册}", link(TipsChapter.GT_TOOL_USER_MANUAL)))
            .task(
                    Task.tag(CustomTags.WRENCHES, 1).consume().title("提交任意扳手"),
                    Task.tag(CustomTags.SCREWDRIVERS, 1).consume().title("提交任意螺丝刀"),
                    Task.tag(CustomTags.WIRE_CUTTERS, 1).consume().title("提交任意剪线钳"),
                    Task.tag(CustomTags.KNIVES, 1).consume().title("提交任意小刀"),
                    Task.tag(CustomTags.SAWS, 1).consume().title("提交任意锯子"),
                    Task.tag(CustomTags.CROWBARS, 1).consume().title("提交任意撬棍"),
                    Task.tag(CustomTags.MALLETS, 1).consume().title("提交任意软锤"))
            .reward(
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.WRENCH, GTMaterials.DamascusSteel), Enchantments.BLOCK_FORTUNE, 3), 1).personal(),
                    Reward.item(ToolHelper.get(GTToolType.SCREWDRIVER, GTMaterials.DamascusSteel).getItem()).personal(),
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.WIRE_CUTTER, GTMaterials.DamascusSteel), Enchantments.BLOCK_FORTUNE, 3), 1).personal(),
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.KNIFE, GTMaterials.DamascusSteel), Enchantments.MOB_LOOTING, 3), 1).personal(),
                    Reward.item(ToolHelper.get(GTToolType.SAW, GTMaterials.DamascusSteel).getItem()).personal(),
                    Reward.item(ToolHelper.get(GTToolType.CROWBAR, GTMaterials.DamascusSteel).getItem()).personal(),
                    Reward.item(ToolHelper.get(GTToolType.SOFT_MALLET, GTMaterials.SiliconeRubber).getItem()).personal())
            .dependsOn(MOVING_MOUNTAINS, NEED_DYE));

    public static final Quest Q_996 = CHAPTER.quest("q_996", q -> q
            .at(9, 6)
            .icon(Items.BLAST_FURNACE)
            .title("996")
            .subtitle("你现在最好的矿物处理方式——")
            .desc(d -> d
                    .line("——就是把他们烧掉")
                    .line()
                    .line("现在你{没办法}{增产矿物}，也{没办法}获得矿物的{副产物}", RED, YELLOW, RED, YELLOW)
                    .line()
                    .line("等时机成熟，你会搭建一套简易的矿物处理产线！"))
            .task(Task.checkmark())
            .reward(Reward.item(Items.BLAST_FURNACE, 9), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(TOOL_UP, A_FULL_ARSENAL));

    public static final Quest ANCIENT_STEELMAKING = CHAPTER.quest("ancient_steelmaking", q -> q
            .at(6.5, 6)
            .title("古法炼钢")
            .subtitle("千锤百炼")
            .desc(d -> d
                    .line("把{铁锭}丢到炉子里面烧成{热铁锭}，然后在工作台上用{锻造锤}把它打成{锻铁锭}", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("你在下一个阶段会{大量}需要它的", DARK_PURPLE))
            .task(Task.item(TagPrefix.ingot, GTMaterials.WroughtIron))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.WroughtIron, 32), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(Q_996));

    public static final Quest SANXINGDUI_PEOPLE = CHAPTER.quest("sanxingdui_people", q -> q
            .at(6.5, 8)
            .icon(TagPrefix.dust, GTMaterials.Bronze)
            .title("三星堆人")
            .subtitle("可惜不能搓青铜面具")
            .desc(d -> d
                    .line("将3个{铜粉}和1个{锡粉}放置在工作台上合成{青铜粉}，放进熔炉里进行熔炼", GOLD, GOLD, GOLD)
                    .line()
                    .line("Ahh...高炉不行")
                    .line()
                    .line("{无序合成配方，意味着你想把粉放在哪个格子都行}", GRAY.italic()))
            .task(Task.item(TagPrefix.dust, GTMaterials.Copper, 3), Task.item(TagPrefix.dust, GTMaterials.Tin))
            .reward(Reward.item(TagPrefix.dust, GTMaterials.Bronze, 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(Q_996));

    public static final Quest KEEP_GOING = CHAPTER.quest("keep_going", q -> q
            .at(3.5, 8)
            .title("继续前进！")
            .subtitle("你终于可以开始用蒸汽做点事了")
            .desc(d -> d
                    .line("到这了？如果你从开始一直玩到这的话可以去休息会。")
                    .line()
                    .line("你需要制作一个{低压蒸汽固体燃料锅炉}，你不得不从这台机器开始。", GOLD)
                    .line()
                    .line("{继续前进，路还很长呢。}", DARK_GREEN))
            .task(Task.item(GTMachines.STEAM_SOLID_BOILER.first()))
            .reward(
                    Reward.item(Items.COAL, 128L),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(ANCIENT_STEELMAKING, SANXINGDUI_PEOPLE));

    public static final Quest SIMPLE_CRAFTING_TERMINAL = CHAPTER.quest("simple_crafting_terminal", q -> q
            .at(6.5, 4)
            .subtitle("合成站")
            .desc(d -> d
                    .line("{简易合成终端}可以与对应容器交互，当作前期的AE", GOLD)
                    .line()
                    .line("但是这个合成终端{只能链接单个容器}", RED)
                    .line()
                    .line("{提示：你可以直接把物品从EMI中拖到终端的搜索栏上来快速搜索}", GRAY.italic()))
            .task(Task.item(GTOAEParts.SIMPLE_CRAFTING_TERMINAL.get()))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8), Reward.item(GTMachines.STEEL_CRATE))
            .dependsOn(ANCIENT_STEELMAKING)
            .hideDependencyLines(false));

    public static final Quest MULTIBLOCK_HELPER = CHAPTER.quest("multiblock_helper", q -> q
            .at(3.5, 4.5)
            .title("多方块帮手")
            .subtitle("你会需要它的")
            .desc(d -> d
                    .line("多方块机器需要手撘？不不不，终端会帮你搞定一切")
                    .line()
                    .line("手持终端，确保物品栏或背包中有需要的方块，潜行右键多方块主机即可搭建结构")
                    .line()
                    .line("当然多方块的成型判定会有一定延迟")
                    .line()
                    .line("{这个任务放在这里是为了提醒你要做这东西，并不是意味着你要在当前阶段就把它搓出来（你也搓不出来）}", DARK_GRAY.italic()))
            .task(Task.item(GTItems.TERMINAL))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 4))
            .dependsOn(ANCIENT_STEELMAKING));

    public static final Quest STRONGER_AND_BIGGER = CHAPTER.quest("stronger_and_bigger", q -> q
            .at(0, 4.5)
            .title("更强更大")
            .subtitle("Ter-100-EX全能型终端")
            .desc(d -> d
                    .line("高级终端功能极其强大，蹲下并右键可以看到详细的设置，这里将一一详细介绍：")
                    .line()
                    .line("{等级方块：}点开右上角选择要安装的等级方块，包括但不限于玻璃，线圈，外壳等", RED)
                    .line()
                    .line("{重复结构次数：}影响多方块的重复结构层数，例如装配线的长度，中子活化器的高度等。", DARK_PURPLE)
                    .line("eg.在搭建装配线的时候选择了重复结构15，则自动搭建合成配方16格的装配线")
                    .line()
                    .line("{无仓室模式：}在这个模式开启之后，可在该多方块上放置多个的功能性仓室将不会被放置，而唯一的功能性仓室则不受影响。", YELLOW)
                    .line("eg.在电力高炉上开启该模式后放置时，只会放置消声仓而不会放置输入输出仓等")
                    .line()
                    .line("{替换模式：}模式开启后，对现有的多方块使用终端的时候会尝试使用物品栏/背包/AE内的符合的等级方块来进行替换", GREEN)
                    .line()
                    .line("{使用AE物品：}顾名思义会使用AE的物品，前提是你身上有终端并且终端通过无线链接到了你的ME存储", AQUA)
                    .page()
                    .line()
                    .line("{镜像搭建：}将多方块左右镜像搭建", BLUE)
                    .line()
                    .line("{模块搭建：}仅搭建对应多方块的附属模块", DARK_GREEN)
                    .line()
                    .line("{拆除模式：}拆除搭建好的多方块", DARK_RED)
                    .line()
                    .line("注：{拆除模式}配合{模块搭建}可以指定要拆除的部分（模块or本体）", YELLOW, YELLOW))
            .task(Task.item(CustomItems.ADVANCED_TERMINAL))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 4))
            .dependsOn(MULTIBLOCK_HELPER, SteamChapter.FIRST_STEEL_INGOT));

    public static final Quest Q_1BA9FA93 = CHAPTER.quest("q_1ba9fa93", q -> q
            .at(-3, 8)
            .iconStack(() -> QuestStacks.withNbt(new ItemStack(BotaniaItems.lexicon), "{\"botania:elven_unlock\":1b}"))
            .desc(d -> d
                    .line("如果你暂时不想推进主线了，可以看看支线：{独善其身}", AQUA)
                    .line()
                    .line("包含AE，魔法，神化，食物以及一些其他东西")
                    .line()
                    .line("{虽然魔法在到达LuV前并不是必选项，但我还是你建议发展魔法。}发展魔法能够极大的提升你在科技线中的体验，{沉淀总是有所回报的}", YELLOW, ITALIC)
                    .page()
                    .line("简单提一下魔法在主线中的应用：HV之前只需魔力池做超导体，HV需要魔力钢做样板更好的配方，需要开精灵门做源质钢做样板供应器更好的配方，LuV需要开门拿精灵玻璃做精灵透镜（{必选}），后续有部分超导体合成配方；EV做符文可以做下界之星更好的配方，UHV开始需要用到盖亚钢和盖亚魂钢，UEV开始大量使用盖亚魂，准备好盖亚魂循环", GOLD))
            .task(Task.checkmark().title("看看支线？"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(SteamChapter.MISTY_MOVEMENT));

    public static final Quest BRONZE_SUBSIDY = CHAPTER.quest("bronze_subsidy", q -> q
            .at(0, 10)
            .icon(TagPrefix.ingot, GTMaterials.Bronze)
            .title("青铜补助")
            .desc(d -> d.line("重要节点会按任务补助档位赠送补助：简单最多，普通适量，专家只有象征性奖励"))
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 320L),
                    Reward.stack(() -> QuestStacks.charged(ToolHelper.get(GTOToolType.VAJRA_HV, GTOMaterials.DarkSteel), 1600000L, 1600000L), 1).personal(),
                    Reward.stack(() -> QuestStacks.charged(GTItems.PROSPECTOR_HV, 16000000L), 1).personal(),
                    Reward.item(TagPrefix.ingot, GTMaterials.WroughtIron, 320L),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .normalSubsidy(
                    Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 64),
                    Reward.stack(() -> QuestStacks.charged(GTItems.PROSPECTOR_LV, 100000L), 1).personal(),
                    Reward.item(TagPrefix.ingot, GTMaterials.WroughtIron, 128L))
            .expertSubsidy(Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 1), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(SteamChapter.MISTY_MOVEMENT)
            .optional().hideUntilDepsVisible());

    public static final Quest PRIMITIVE_BLAST_FURNACE = CHAPTER.quest("primitive_blast_furnace", q -> q
            .at(6.5, 2)
            .title("巨型熔炉")
            .subtitle("你的第一个多方块结构...真的算是吗？")
            .desc(d -> d
                    .line("用熔炉在地上摆放一个{3x3x3}形状的立方体，就会自动成型为{巨型熔炉}", GREEN, GOLD)
                    .line()
                    .line("在其原料栏位放入{27个}熔炉可以烧制出一个同样的{巨型熔炉}物品，放入右下的槽位可以增加{1并行}", GREEN, GOLD, YELLOW)
                    .line()
                    .line("手持剪刀{[Shift]+[右键]}可以拆除巨型熔炉", YELLOW)
                    .line()
                    .line("{它的性价比完爆了蒸汽时代熔炉，65并行足以满足你前期的烧炼需求。它还因为过强险些被删除}", GRAY.italic())
                    .line()
                    .line("{没有主机的多方块结构不能叫做多方块结构}", GRAY.italic().strike()))
            .task(Task.observe(JumboFurnace.get().jumboFurnaceBlock.get(), 0).title("观察: {巨型熔炉}", GREEN).icon(JumboFurnace.get().jumboFurnaceItem.get()))
            .reward(Reward.item(JumboFurnace.get().jumboFurnaceItem.get()))
            .dependsOn(WORKBENCH));

    public static final Quest NIGHT_VISION = CHAPTER.quest("night_vision", q -> q
            .at(-1.5, 1.5)
            .icon(GTItems.NIGHTVISION_GOGGLES)
            .title("夜视")
            .subtitle("无需火把——但是为了避免刷怪还是插上吧")
            .desc(d -> d.line("GTO自带了夜视——如果你没有改键位的话，按住{[Z]}键可以进入HUD编辑状态，然后点击右上角“+”号唤出{“玩家实时属性”}窗口，编辑各种属性，包括夜视！", YELLOW, AQUA))
            .task(Task.checkmark())
            .reward(Reward.item(ModBlocks.itemMegaTorch.get()))
            .dependsOn(GregtechOdysseyChapter.HELLO_WORLD)
            .hideDependencyLines());

    public static final Quest TRASH_CAN = CHAPTER.quest("trash_can", q -> q
            .at(13, 4)
            .icon(AEBlocks.CONDENSER)
            .title("垃圾桶")
            .subtitle("别丢错东西了")
            .desc(d -> d
                    .line("销毁你的垃圾")
                    .line()
                    .line("后面还能用来制作物质球和奇点"))
            .task(Task.checkmark())
            .reward(Reward.item(AEBlocks.CONDENSER), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MOVING_MOUNTAINS));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(TipsChapter.ORES_AND_VEINS, 11, -2.5))
                .link(QuestLink.of(SteamChapter.MISTY_MOVEMENT, 0, 8))
                .link(QuestLink.of(GregtechOdysseyChapter.HELLO_WORLD, -5.5, 0))
                .link(QuestLink.of(TipsChapter.FTB_UTIMINE, 11, -3.5))
                .link(QuestLink.of(TipsChapter.BELT, 11, 8))
                .link(QuestLink.of(TitlesChapter.Q_49943789, -3, 1.5)));
    }
}
