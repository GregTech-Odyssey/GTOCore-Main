package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOAEParts;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;
import net.xalcon.torchmaster.common.ModBlocks;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.naturescompass.NaturesCompass;
import commoble.jumbofurnace.JumboFurnace;
import earth.terrarium.adastra.common.registry.ModItems;
import vazkii.botania.common.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NeolithicChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("neolithic", c -> c
            .title("1. 石器时代(Stone Age)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(ToolHelper.get(GTToolType.AXE, GTMaterials.Flint).getItem()));

    public static final Quest GOLD_PANNER = CHAPTER.quest("gold_panner", q -> q
            .at(2, 0)
            .milestone()
            .title("淘金客")
            .subtitle("西部大开发")
            .desc(d -> d.line("你要问为什么不是先砍树——你砍得动吗？"))
            .task(Task.item(Items.GRAVEL, 16), Task.item(Items.FLINT, 4))
            .scaledReward(Items.GRAVEL, 16)
            .scaledReward(Items.FLINT, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest FLINT_KNAPPER = CHAPTER.quest("flint_knapper", q -> q
            .at(-2, -2)
            .title("陶棍客")
            .task(Task.item(Items.STICK, 4))
            .scaledReward(Items.STICK, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest FLINT_KNIFE = CHAPTER.quest("flint_knife", q -> q
            .at(0, -2)
            .title("燧石小刀")
            .subtitle("樵夫乐事")
            .desc(d -> d.line("用小刀打草概率掉落植物纤维，空手破坏树叶概率掉落木棍"))
            .icon(vectorwing.farmersdelight.common.registry.ModItems.FLINT_KNIFE.get())
            .task(Task.tag(CustomTags.KNIVES, 1).title("任意小刀"))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(GOLD_PANNER, FLINT_KNAPPER));

    public static final Quest PLANT_FIBER = CHAPTER.quest("plant_fiber", q -> q
            .at(2, -2)
            .title("植物纤维")
            .subtitle("这可不能做成蓑衣")
            .desc(d -> d.line("你需要收集一些"))
            .task(Task.item(GTOItems.PLANT_FIBER, 2))
            .scaledReward(GTOItems.PLANT_FIBER, 2)
            .dependsOn(FLINT_KNIFE));

    public static final Quest FLINT_AXE = CHAPTER.quest("flint_axe", q -> q
            .at(4, 0)
            .milestone()
            .title("燧石斧")
            .subtitle("燧石大力量")
            .desc(d -> d.line("制造一把{燧石斧}，放开了去砍吧！", GOLD))
            .task(Task.item(ToolHelper.get(GTToolType.AXE, GTMaterials.Flint).getItem()))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(PLANT_FIBER));

    public static final Quest CHOP_TREES = CHAPTER.quest("chop_trees", q -> q
            .at(6, -2)
            .title("要致富，先撸树")
            .subtitle("有种熟悉的感觉")
            .desc(d -> d.line("有木头才能继续推进流程，如果你发现你砍不了树，不妨去看看前置任务"))
            .task(Task.tag(ItemTags.LOGS, 16).title("任意原木"))
            .scaledReward(Items.OAK_LOG, 16)
            .dependsOn(FLINT_AXE));

    public static final QuestLink CHOP_TREES_COPY_1 = CHAPTER.copy(CHOP_TREES, 16, 6);

    public static final Quest WORKBENCH = CHAPTER.quest("workbench", q -> q
            .at(8, 0)
            .milestone()
            .title("工作台")
            .subtitle("制作你的第一个工作台")
            .desc(d -> d
                    .line("它的配方与原版 Minecraft 有着截然不同的差距，你需要用两个{燧石}和两个{原木}。", GOLD, GOLD)
                    .line()
                    .line("你可以查看EMI来获得合成配方，然后在材料充足的情况下点击加号来自动合成。")
                    .line()
                    .line("{简单模式下配方不变，四个木板即可}", GRAY.italic()))
            .task(Task.item(Items.CRAFTING_TABLE))
            .scaledReward(Items.FLINT, 4)
            .dependsOn(CHOP_TREES));

    public static final Quest FLINT_PICKAXE = CHAPTER.quest("flint_pickaxe", q -> q
            .at(10, 2)
            .title("燧石镐")
            .task(Task.item(ToolHelper.get(GTToolType.PICKAXE, GTMaterials.Flint).getItem()))
            .scaledReward(Items.COBBLESTONE, 8)
            .dependsOn(WORKBENCH));

    public static final QuestLink FLINT_PICKAXE_COPY_1 = CHAPTER.copy(FLINT_PICKAXE, 10, 6);

    public static final Quest FURNACE = CHAPTER.quest("furnace", q -> q
            .at(12, 0)
            .milestone()
            .title("熔炉")
            .task(Task.item(Items.FURNACE))
            .scaledReward(Items.COAL, 4)
            .dependsOn(FLINT_PICKAXE));

    public static final Quest STONE = CHAPTER.quest("stone", q -> q
            .at(6, 4)
            .title("石头")
            .task(Task.item(Items.STONE, 5))
            .scaledReward(Items.COBBLESTONE, 8)
            .dependsOn(FURNACE));

    public static final Quest FLINT_MORTAR = CHAPTER.quest("flint_mortar", q -> q
            .at(8, 4)
            .title("燧石研钵")
            .icon(ToolHelper.get(GTToolType.MORTAR, GTMaterials.Flint).getItem())
            .task(Task.tag(CustomTags.MORTARS, 1).title("任意研钵"))
            .scaledReward(Items.FLINT, 2)
            .dependsOn(STONE));

    public static final Quest TIME_TO_MINE = CHAPTER.quest("time_to_mine", q -> q
            .at(12, 8)
            .milestone()
            .title("下矿时间到")
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
                    .line("{别忘了领取前面任务给你发的魔力钢探矿仪}", GRAY.italic())
                    .line()
                    .line("铜矿和锡矿，你当前最重要的矿")
                    .line()
                    .line("如果附近没有的需要走远点看看，这个阶段挖掉一整个矿脉应该是够用了"))
            .task(
                    Task.anyOf(16, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Tin), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Cassiterite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.CassiteriteSand)).title("任意粗锡"),
                    Task.anyOf(16, Items.RAW_COPPER, ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Chalcopyrite), ChemicalHelper.getItem(TagPrefix.rawOre, GTMaterials.Malachite)).title("任意粗铜"),
                    Task.item(Items.RAW_IRON, 8))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FLINT_PICKAXE_COPY_1));

    public static final Quest SMELTING = CHAPTER.quest("smelting", q -> q
            .at(8, 8)
            .milestone()
            .title("铜锭、锡锭与铁锭")
            .icon(TagPrefix.ingot, GTMaterials.Copper)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Copper, 12), Task.item(TagPrefix.ingot, GTMaterials.Tin, 4), Task.item(Items.IRON_INGOT, 6))
            .scaledReward(Items.COAL, 4)
            .dependsOn(TIME_TO_MINE, FURNACE));

    public static final Quest TOOL_USER = CHAPTER.quest("tool_user", q -> q
            .at(0, 10)
            .title("人类会使用工具")
            .icon(ToolHelper.get(GTToolType.HARD_HAMMER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.HAMMERS, 1).title("任意锻造锤"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(SMELTING));

    public static final Quest COPPER_TIN_DUST = CHAPTER.quest("copper_tin_dust", q -> q
            .at(8, 6)
            .title("铜粉与锡粉")
            .subtitle("可惜不能搓青铜面具")
            .desc(d -> d
                    .line("将3个{铜粉}和1个{锡粉}放置在工作台上合成{青铜粉}，放进熔炉里进行熔炼", GOLD, GOLD, GOLD)
                    .line()
                    .line("Ahh...高炉不行")
                    .line()
                    .line("{无序合成配方，意味着你想把粉放在哪个格子都行}", GRAY.italic()))
            .icon(TagPrefix.dust, GTMaterials.Copper)
            .task(Task.item(TagPrefix.dust, GTMaterials.Copper, 12), Task.item(TagPrefix.dust, GTMaterials.Tin, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Copper, 3)
            .dependsOn(SMELTING, FLINT_MORTAR));

    public static final Quest STIR_STIR = CHAPTER.quest("stir_stir", q -> q
            .at(6, 6)
            .title("搅拌，搅拌！")
            .task(Task.item(TagPrefix.dust, GTMaterials.Bronze, 12))
            .scaledReward(Items.COAL, 4)
            .dependsOn(COPPER_TIN_DUST));

    public static final Quest WESTERNIZATION_MOVEMENT = CHAPTER.quest("westernization_movement", q -> q
            .at(4, 8)
            .milestone()
            .title("扬雾运动")
            .subtitle("隐入蒸汽")
            .desc(d -> d
                    .line("{GregTech Odessey}中，蒸汽时代的内容被优化与拓展", DARK_RED.underline())
                    .line()
                    .line("在蒸汽的驱动下，很多需要手动合成的配方可以被更先进的方式替代")
                    .line()
                    .line("开始你的青铜大跃进吧")
                    .line()
                    .line("{钢}是你这个阶段的首要目标，然后你就可以制作{ULV机器外壳}前往下一个电压了", GOLD, GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Bronze, 12))
            .scaledReward(TagPrefix.ingot, GTMaterials.Bronze, 4)
            .dependsOn(STIR_STIR));

    public static final Quest BRONZE_PLATE = CHAPTER.quest("bronze_plate", q -> q
            .at(2, 10)
            .title("青铜板")
            .task(Task.item(TagPrefix.plate, GTMaterials.Bronze, 5))
            .scaledReward(TagPrefix.ingot, GTMaterials.Bronze, 2)
            .dependsOn(WESTERNIZATION_MOVEMENT, TOOL_USER));

    public static final Quest STICKY = CHAPTER.quest("sticky", q -> q
            .at(-2, 4)
            .title("黏糊糊？")
            .subtitle("只不过是轻轻动了动黏土而已")
            .desc(d -> d
                    .line("好吧，你现在需要去搞点黏土了")
                    .line()
                    .line("你需要一些黏土去制作后面几个大家伙")
                    .line()
                    .line("那么黏土去哪找呢？去河流边上，或者直接点，繁茂洞穴，或者再直接点，{去买点}", link(ShopChapter.A_STACK_OF_CLAY)))
            .task(Task.item(Items.CLAY_BALL, 12))
            .scaledReward(Items.CLAY_BALL, 4)
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest BAKE_IT = CHAPTER.quest("bake_it", q -> q
            .at(-2, 6)
            .title("烧一烧")
            .task(Task.item(Items.BRICK, 12))
            .scaledReward(Items.BRICK, 4)
            .dependsOn(STICKY, FURNACE));

    public static final Quest BRICK_VEIN = CHAPTER.quest("brick_vein", q -> q
            .at(-2, 8)
            .title("有没有砖矿脉？")
            .task(Task.item(Items.BRICKS, 3))
            .scaledReward(Items.BRICKS, 2)
            .dependsOn(BAKE_IT));

    public static final Quest BRONZE_BRICK_HULL = CHAPTER.quest("bronze_brick_hull", q -> q
            .at(0, 8)
            .milestone()
            .title("砖砌青铜外壳")
            .task(Task.item(GTBlocks.BRONZE_BRICKS_HULL))
            .reward(Reward.item(GTBlocks.BRONZE_BRICKS_HULL, 4))
            .dependsOn(BRONZE_PLATE, BRICK_VEIN));

    public static final Quest WHERE_ARE_MY_TOOLS = CHAPTER.quest("where_are_my_tools", q -> q
            .at(2, 2)
            .side()
            .icon(Items.WOODEN_AXE)
            .title("我的工具呢？")
            .subtitle("唉......")
            .desc(d -> d
                    .line("所有的木制和石制工具都被禁用了，所以你得从燧石工具开始做起。")
                    .line()
                    .line("你需要做出第一个{燧石斧}才能砍树，并且不用工作台做的{燧石斧}只有16点耐久", GOLD, GOLD))
            .task(Task.checkmark())
            .reward(Reward.item(Items.BREAD).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 4))
            .requireAllStarted()
            .dependsOn(GOLD_PANNER));

    public static final Quest MORE_WOODEN_STICKS = CHAPTER.quest("more_wooden_sticks", q -> q
            .at(-2, 0)
            .side()
            .icon(Items.DIRT)
            .title("来点木棍")
            .subtitle("小福利")
            .desc(d -> d.line("整点泥土来，木棍收好嗷"))
            .task(Task.tag(ItemTags.DIRT, 16).title("任意泥土*16"))
            .reward(Reward.item(Items.STICK, 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest DALLYING_WITH_PLANTS = CHAPTER.quest("dallying_with_plants", q -> q
            .at(4, -4)
            .side()
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
            .dependsOn(CHOP_TREES));

    public static final Quest EAT_WELL_TO_KEEP_GOING = CHAPTER.quest("eat_well_to_keep_going", q -> q
            .at(4, -2)
            .side()
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
            .dependsOn(CHOP_TREES));

    public static final Quest SWEET_TREAT_AFTER_WORK = CHAPTER.quest("sweet_treat_after_work", q -> q
            .at(8, -4)
            .side()
            .icon(Items.SPRUCE_WOOD)
            .title("下班甜点~")
            .subtitle("彩蛋 #1")
            .desc(d -> d.line("这可能是你发现的第一个彩蛋任务"))
            .task(Task.tag(ItemTags.LOGS, 256L).title("任意原木"))
            .reward(Reward.item(Items.IRON_AXE).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .optional().invisible()
            .dependsOn(CHOP_TREES));

    public static final Quest BACKPACK_TIME = CHAPTER.quest("backpack_time", q -> q
            .at(8, 2)
            .side()
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
            .at(8, -2)
            .side()
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

    public static final Quest MOVING_MOUNTAINS = CHAPTER.quest("moving_mountains", q -> q
            .at(14, 10)
            .side()
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
            .at(14, 6)
            .side()
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
            .at(12, 10)
            .side()
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
            .at(14, 8)
            .side()
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
            .at(6, -4)
            .side()
            .icon(Items.OAK_SAPLING)
            .title("在找繁茂洞穴？")
            .subtitle("彩蛋 #2")
            .task(Task.tag(ItemTags.SAPLINGS, 32).title("任意树苗"))
            .reward(
                    Reward.item(NaturesCompass.naturesCompass),
                    Reward.item(AEItems.METEORITE_COMPASS),
                    Reward.item(ExplorersCompass.explorersCompass),
                    Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .optional().invisible()
            .dependsOn(CHOP_TREES));

    public static final Quest ANY_BUTCHERY_KNIVES = CHAPTER.quest("any_butchery_knives", q -> q
            .at(16, 10)
            .side()
            .iconStack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.BUTCHERY_KNIFE, GTMaterials.Iron), Enchantments.MOB_LOOTING, 3))
            .subtitle("免费的抢夺Ⅲ")
            .desc(d -> d.line("也许你需要准备一个小牧场来产出食物"))
            .task(Task.tag(CustomTags.BUTCHERY_KNIVES, 1).title("屠刀"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .optional()
            .dependsOn(MOVING_MOUNTAINS));

    public static final Quest AS_FOR_ANIMALS_MAY_THEY_ALL = CHAPTER.quest("as_for_animals_may_they_all", q -> q
            .at(16, 8)
            .side()
            .icon(Items.BEEF)
            .title("至于动物？我祝他们一切安好")
            .subtitle("是的，我肚子饿了")
            .desc(d -> d.line("兜兜转转大半天可能还是去获取点肉来能填饱肚子"))
            .task(Task.item(Items.LEATHER, 16))
            .reward(Reward.item(Items.COOKED_BEEF, 64).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .requireAny()
            .dependsOn(CHOP_TREES_COPY_1, ANY_BUTCHERY_KNIVES));

    public static final Quest WOOD_MALLET = CHAPTER.quest("wood_mallet", q -> q
            .at(10, -2)
            .side()
            .subtitle("其实是个开关")
            .desc(d -> d
                    .line("软锤不仅可以作为合成材料，也可以作为工具")
                    .line()
                    .line("{右键}机器即可暂停机器工作，{潜行右键}机器可以让机器在{运行完当前配方之后}再暂停工作", AQUA, AQUA, YELLOW))
            .task(Task.item(ToolHelper.get(GTToolType.SOFT_MALLET, GTMaterials.Wood).getItem()))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .optional()
            .dependsOn(WORKBENCH));

    public static final Quest ANY_BEDS = CHAPTER.quest("any_beds", q -> q
            .at(10, -4)
            .side()
            .icon(Items.WHITE_BED)
            .subtitle("高科技时间折跃装置")
            .desc(d -> d.line("这也许是防止遭受怪物袭击最好的手段"))
            .task(Task.tag(ItemTags.BEDS, 1).title("床"))
            .requireAnyStarted()
            .dependsOn(WOOD_MALLET, UtilChapter.ANY_BEDS));

    public static final Quest GRASS_HARVESTER = CHAPTER.quest("grass_harvester", q -> q
            .at(6, 2)
            .side()
            .subtitle("又名种子收割刀")
            .desc(d -> d.line("右击{草}极大概率掉落小麦种子，有小概率掉落胡萝卜等农作物", GOLD))
            .task(Task.item(GTOItems.GRASS_HARVESTER))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(WORKBENCH));

    public static final Quest NEED_DYE = CHAPTER.quest("need_dye", q -> q
            .at(0, -4)
            .side()
            .title("需要染料？")
            .subtitle("拒绝辣手摧花")
            .desc(d -> d.line("你可以使用{农夫乐事}模组的{砧板}获得它，在这里你不能徒手分解花朵！", YELLOW, GOLD))
            .task(Task.item(QuestExternalRefs.FarmersDelightItems.CUTTING_BOARD), Task.tag(QuestExternalRefs.TAG_C_DYES, 1).title("任意染料"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(FLINT_KNIFE));

    public static final QuestLink NEED_DYE_COPY_1 = CHAPTER.copy(NEED_DYE, 12, 12);

    public static final Quest A_FULL_ARSENAL = CHAPTER.quest("a_full_arsenal", q -> q
            .at(14, 12)
            .side()
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
            .dependsOn(MOVING_MOUNTAINS, NEED_DYE_COPY_1));

    public static final QuestLink A_FULL_ARSENAL_COPY_1 = CHAPTER.copy(A_FULL_ARSENAL, 8, 12);

    public static final Quest Q_996 = CHAPTER.quest("q_996", q -> q
            .at(10, 10)
            .side()
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
            .dependsOn(TOOL_UP, A_FULL_ARSENAL_COPY_1));

    public static final Quest SIMPLE_CRAFTING_TERMINAL = CHAPTER.quest("simple_crafting_terminal", q -> q
            .at(10, 12)
            .side()
            .subtitle("合成站")
            .desc(d -> d
                    .line("{简易合成终端}可以与对应容器交互，当作前期的AE", GOLD)
                    .line()
                    .line("但是这个合成终端{只能链接单个容器}", RED)
                    .line()
                    .line("{提示：你可以直接把物品从EMI中拖到终端的搜索栏上来快速搜索}", GRAY.italic()))
            .task(Task.item(GTOAEParts.SIMPLE_CRAFTING_TERMINAL.get()))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8), Reward.item(GTMachines.STEEL_CRATE))
            .hideDependencyLines(false)
            .dependsOn(Q_996));

    public static final Quest MULTIBLOCK_HELPER = CHAPTER.quest("multiblock_helper", q -> q
            .at(8, 10)
            .side()
            .title("多方块帮手")
            .subtitle("你会需要它的")
            .desc(d -> d
                    .line("多方块机器需要手撘？不不不，终端会帮你搞定一切")
                    .line()
                    .line("手持终端右键多方块主机，打开结构预览，进入建造配置选好每种方块，确保物品栏或背包中有需要的方块，点建造即可搭建结构")
                    .line()
                    .line("在建造配置里点「设为终端一键搭建方案」，以后潜行右键同种机器的主机就会按这个方案直接搭建")
                    .line()
                    .line("当然多方块的成型判定会有一定延迟")
                    .line()
                    .line("{这个任务放在这里是为了提醒你要做这东西，并不是意味着你要在当前阶段就把它搓出来（你也搓不出来）}", DARK_GRAY.italic()))
            .task(Task.item(GTItems.TERMINAL))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 4))
            .dependsOn(Q_996));

    public static final Quest Q_1BA9FA93 = CHAPTER.quest("q_1ba9fa93", q -> q
            .at(4, 6)
            .side()
            .iconStack(() -> QuestStacks.withNbt(BotaniaItems.getItem("lexicon").getDefaultInstance(), "{\"botania:elven_unlock\":1b}"))
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
            .dependsOn(WESTERNIZATION_MOVEMENT));

    public static final Quest BRONZE_SUBSIDY = CHAPTER.quest("bronze_subsidy", q -> q
            .at(4, 10)
            .side()
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
            .optional().hideUntilDepsVisible()
            .dependsOn(WESTERNIZATION_MOVEMENT));

    public static final Quest PRIMITIVE_BLAST_FURNACE = CHAPTER.quest("primitive_blast_furnace", q -> q
            .at(12, -2)
            .side()
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
            .dependsOn(FURNACE));

    public static final Quest NIGHT_VISION = CHAPTER.quest("night_vision", q -> q
            .at(0, 2)
            .side()
            .icon(GTItems.NIGHTVISION_GOGGLES)
            .title("夜视")
            .subtitle("无需火把——但是为了避免刷怪还是插上吧")
            .desc(d -> d.line("GTO自带了夜视——如果你没有改键位的话，按住{[Z]}键可以进入HUD编辑状态，然后点击右上角“+”号唤出{“玩家实时属性”}窗口，编辑各种属性，包括夜视！", YELLOW, AQUA))
            .task(Task.checkmark())
            .reward(Reward.item(ModBlocks.itemMegaTorch.get()))
            .dependsOn(NewGregtechOdysseyChapter.HELLO_WORLD));

    public static final Quest TRASH_CAN = CHAPTER.quest("trash_can", q -> q
            .at(16, 12)
            .side()
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
                .link(QuestLink.of(NewGregtechOdysseyChapter.HELLO_WORLD, 0, 0))
                .mainLine(NewGregtechOdysseyChapter.HELLO_WORLD, GOLD_PANNER, FLINT_AXE, WORKBENCH, FURNACE, TIME_TO_MINE, SMELTING, WESTERNIZATION_MOVEMENT, BRONZE_BRICK_HULL));
    }
}
