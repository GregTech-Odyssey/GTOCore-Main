package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestStyle;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;
import net.xalcon.torchmaster.common.ModItems;

import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAOE;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDampen;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDecelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDurationDown;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtract;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentFortune;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentPierce;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSplit;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBlink;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBounce;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBreak;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectColdSnap;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectConjureWater;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCraft;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCrush;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCut;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectDelay;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectDispel;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectEnderChest;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectEvaporate;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectExchange;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectExplosion;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectFangs;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectFell;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectFirework;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectFlare;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectFreeze;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectGlide;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectGravity;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectGrow;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarvest;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHex;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIgnite;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIntangible;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectInteract;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectInvisibility;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectKnockback;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLaunch;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLeap;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLight;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLightning;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLinger;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectName;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectOrbit;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectPhantomBlock;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectPickup;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectPlaceBlock;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectPull;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectRedstone;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectRune;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSenseMagic;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSlowfall;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSmelt;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSnare;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonDecoy;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonSteed;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonUndead;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonVex;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonWolves;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectToss;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectWindshear;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectWither;
import com.hollingsworth.arsnouveau.common.spell.method.MethodUnderfoot;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import com.teamremastered.endrem.registers.ERItems;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.item.BotaniaItems;

public final class Magic1Chapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("magic1", c -> c
            .title("魔力导论")
            .subtitle("从百花园走到符文祭坛")
            .icon(GTOItems.COLORFUL_MYSTICAL_FLOWER)
            .hideQuestsUntilDepsVisible()
            .flexible()
            .defaultMinWidth(200));

    public static final Quest WELCOME_TO_THE_MAGIC_LINE = CHAPTER.quest("welcome_to_the_magic_line", q -> q
            .at(5, 2)
            .title("欢迎来到{魔法线}", QuestStyle.LIGHT_PURPLE)
            .desc(d -> d.line("魔法线糅合了{植物魔法}和{新生魔艺}两个模组", QuestStyle.GREEN, QuestStyle.YELLOW))
            .task(Task.item(BotaniaItems.lexicon), Task.item(ItemsRegistry.WORN_NOTEBOOK)));

    public static final Quest APOTHECARY_DEFAULT = CHAPTER.quest("apothecary_default", q -> q
            .at(10.5, 6).minWidth(250)
            .desc(d -> d
                    .line("现在我们已经拥有基础花卉,可以通过{花药台}将普通植物转化为功能性产能花!", QuestStyle.GREEN)
                    .line()
                    .line("要激活{神秘花}{花瓣的能量,需先向花药台注入}{水}.可通过{右键}{使用水桶或直接投掷水桶完成注水.}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.AQUA, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{合成时只需将配方物品投入花药台.}{空手右键}{可取出已放置的物品.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{完成首次合成后,20秒内}{空手右键}可快速重复上次配方,方便批量制作!", QuestStyle.WHITE, QuestStyle.GREEN))
            .task(Task.item(BotaniaBlocks.defaultAltar))
            .dependsOn(WELCOME_TO_THE_MAGIC_LINE));

    public static final Quest OUR_FIRST_FLOWER = CHAPTER.quest("our_first_flower", q -> q
            .at(15, 6)
            .title("我们的第一朵花")
            .desc(d -> d
                    .line("{白雏菊}是您最早制作的神奇花卉之一!", QuestStyle.GREEN)
                    .line()
                    .line("该花卉能将周围8格内的{原木}或{石头}分别转化为{活木}或{活石},转化范围为以花为中心的1格半径区域.", QuestStyle.DARK_GREEN, QuestStyle.BLUE, QuestStyle.DARK_GREEN, QuestStyle.BLUE))
            .task(Task.item(BotaniaFlowerBlocks.pureDaisy))
            .dependsOn(APOTHECARY_DEFAULT));

    public static final Quest LIVINGROCK = CHAPTER.quest("livingrock", q -> q
            .at(18, 6)
            .title("{活石}", QuestStyle.GRAY)
            .desc(d -> d.line("使用{白雏菊}将石头转化为活石!", QuestStyle.GREEN))
            .task(Task.item(BotaniaBlocks.livingrock, 8))
            .dependsOn(OUR_FIRST_FLOWER));

    public static final Quest MANA = CHAPTER.quest("mana", q -> q
            .at(22, 6).minWidth(300)
            .title("{魔力}", QuestStyle.AQUA)
            .desc(d -> d
                    .line("现在我们制作了{活石},可以开始合成{魔力池}了.", QuestStyle.AQUA, QuestStyle.GREEN)
                    .line()
                    .line("{魔力池}{是储存魔力的核心设施.当池中存储足够魔力后,即可将特定资源转化为魔能材料.转换时只需将物品投入注有魔力的池中即可.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{要}{生成魔力},您需要制作{产能花},例如{火红莲}.", QuestStyle.WHITE, QuestStyle.AQUA, QuestStyle.GREEN, QuestStyle.RED)
                    .line()
                    .line("提示:{魔力池}{可以存储1,000,000魔力.手持}{森林法杖}观察池体可查看当前魔力储量.", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN))
            .task(Task.item(BotaniaBlocks.manaPool), Task.item(BotaniaFlowerBlocks.endoflame).optional())
            .dependsOn(LIVINGROCK));

    public static final Quest FERTILIZER = CHAPTER.quest("fertilizer", q -> q
            .at(17.5, 10).iconScale(2)
            .desc(d -> d
                    .line("使用{植物魔法}{的产能花需要}{神秘花}{.}", QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{获取}{神秘花}{需制作}{花肥}{,将其施用于草方块即可生长出}{神秘花}{,将其施用于石头即可生长出}{神秘蘑菇}{.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{推荐使用}{灌注室}直接制作.", QuestStyle.WHITE, QuestStyle.YELLOW)
                    .line()
                    .line("当你获得了所有{神秘花}就可以使用{附魔装置}大量生产,你之后需要超超超多的{神秘花}.", QuestStyle.GREEN, QuestStyle.YELLOW, QuestStyle.GREEN))
            .task(Task.item(BotaniaItems.fertilizer))
            .reward(Reward.item(Items.BONE_MEAL, 32))
            .dependsOn(WELCOME_TO_THE_MAGIC_LINE)
            .hideUntilDepsVisible(false).hideDependencyLines());

    public static final Quest WHITE = CHAPTER.quest("white", q -> q
            .at(18.5, 7.5)
            .title("白色")
            .task(Task.item(BotaniaBlocks.whiteFlower), Task.item(BotaniaBlocks.doubleFlowerWhite), Task.item(BotaniaBlocks.whiteMushroom))
            .reward(Reward.item(BotaniaItems.whitePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ORANGE = CHAPTER.quest("orange", q -> q
            .at(16.5, 7.5)
            .title("橙色")
            .task(Task.item(BotaniaBlocks.orangeFlower), Task.item(BotaniaBlocks.doubleFlowerOrange), Task.item(BotaniaBlocks.orangeMushroom))
            .reward(Reward.item(BotaniaItems.orangePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MAGENTA = CHAPTER.quest("magenta", q -> q
            .at(19.5, 12)
            .title("品红色")
            .task(Task.item(BotaniaBlocks.magentaFlower), Task.item(BotaniaBlocks.doubleFlowerMagenta), Task.item(BotaniaBlocks.magentaMushroom))
            .reward(Reward.item(BotaniaItems.magentaPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest LIGHT_BLUE = CHAPTER.quest("light_blue", q -> q
            .at(15, 11)
            .title("淡蓝色")
            .task(
                    Task.item(BotaniaBlocks.lightBlueFlower),
                    Task.item(BotaniaBlocks.doubleFlowerLightBlue),
                    Task.item(BotaniaBlocks.lightBlueMushroom))
            .reward(Reward.item(BotaniaItems.lightBluePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest YELLOW = CHAPTER.quest("yellow", q -> q
            .at(17.5, 12.5)
            .title("黄色")
            .task(Task.item(BotaniaBlocks.yellowFlower), Task.item(BotaniaBlocks.doubleFlowerYellow), Task.item(BotaniaBlocks.yellowMushroom))
            .reward(Reward.item(BotaniaItems.yellowPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest LIME = CHAPTER.quest("lime", q -> q
            .at(16.5, 12.5)
            .title("黄绿色")
            .task(Task.item(BotaniaBlocks.limeFlower), Task.item(BotaniaBlocks.doubleFlowerLime), Task.item(BotaniaBlocks.limeMushroom))
            .reward(Reward.item(BotaniaItems.limePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest PINK = CHAPTER.quest("pink", q -> q
            .at(17.5, 7.5)
            .title("粉色")
            .task(Task.item(BotaniaBlocks.pinkFlower), Task.item(BotaniaBlocks.doubleFlowerPink), Task.item(BotaniaBlocks.pinkMushroom))
            .reward(Reward.item(BotaniaItems.pinkPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest GRAY = CHAPTER.quest("gray", q -> q
            .at(15, 10)
            .title("灰色")
            .task(Task.item(BotaniaBlocks.grayFlower), Task.item(BotaniaBlocks.doubleFlowerGray), Task.item(BotaniaBlocks.grayMushroom))
            .reward(Reward.item(BotaniaItems.grayPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest LIGHT_GRAY = CHAPTER.quest("light_gray", q -> q
            .at(18.5, 12.5)
            .title("淡灰色")
            .task(
                    Task.item(BotaniaBlocks.lightGrayFlower),
                    Task.item(BotaniaBlocks.doubleFlowerLightGray),
                    Task.item(BotaniaBlocks.lightGrayMushroom))
            .reward(Reward.item(BotaniaItems.lightGrayPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest CYAN = CHAPTER.quest("cyan", q -> q
            .at(19.5, 8)
            .title("青色")
            .task(Task.item(BotaniaBlocks.cyanFlower), Task.item(BotaniaBlocks.doubleFlowerCyan), Task.item(BotaniaBlocks.cyanMushroom))
            .reward(Reward.item(BotaniaItems.cyanPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest PURPLE = CHAPTER.quest("purple", q -> q
            .at(15, 9)
            .title("紫色")
            .task(Task.item(BotaniaBlocks.purpleFlower), Task.item(BotaniaBlocks.doubleFlowerPurple), Task.item(BotaniaBlocks.purpleMushroom))
            .reward(Reward.item(BotaniaItems.purplePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BLUE = CHAPTER.quest("blue", q -> q
            .at(20, 9)
            .title("蓝色")
            .task(Task.item(BotaniaBlocks.blueFlower), Task.item(BotaniaBlocks.doubleFlowerBlue), Task.item(BotaniaBlocks.blueMushroom))
            .reward(Reward.item(BotaniaItems.bluePetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BROWN = CHAPTER.quest("brown", q -> q
            .at(20, 10)
            .title("棕色")
            .task(Task.item(BotaniaBlocks.brownFlower), Task.item(BotaniaBlocks.doubleFlowerBrown), Task.item(BotaniaBlocks.brownMushroom))
            .reward(Reward.item(BotaniaItems.brownPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest RED = CHAPTER.quest("red", q -> q
            .at(15.5, 12)
            .title("红色")
            .task(Task.item(BotaniaBlocks.redFlower), Task.item(BotaniaBlocks.doubleFlowerRed), Task.item(BotaniaBlocks.redMushroom))
            .reward(Reward.item(BotaniaItems.redPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BLACK = CHAPTER.quest("black", q -> q
            .at(20, 11)
            .title("黑色")
            .task(Task.item(BotaniaBlocks.blackFlower), Task.item(BotaniaBlocks.doubleFlowerBlack), Task.item(BotaniaBlocks.blackMushroom))
            .reward(Reward.item(BotaniaItems.blackPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest GREEN = CHAPTER.quest("green", q -> q
            .at(15.5, 8)
            .title("绿色")
            .task(Task.item(BotaniaBlocks.greenFlower), Task.item(BotaniaBlocks.doubleFlowerGreen), Task.item(BotaniaBlocks.greenMushroom))
            .reward(Reward.item(BotaniaItems.greenPetal, 16))
            .dependsOn(FERTILIZER)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest THAUMIUM_MANA_FLOWER = CHAPTER.quest("thaumium_mana_flower", q -> q
            .at(23.5, 4)
            .title("神秘的魔力莲花")
            .desc(d -> d.line("{黑莲花}仅存于宝箱中,可投入非空{魔力池}{为其注入大量浓缩魔力.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.anyOf(1, BotaniaItems.blackLotus, BotaniaItems.blackerLotus).title("{黑莲花}", QuestStyle.GREEN))
            .dependsOn(MANA)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MANA_CONTAINER_REGULATION = CHAPTER.quest("mana_container_regulation", q -> q
            .at(21, 8)
            .title("魔力存储调控")
            .desc(d -> d
                    .line("{魔力分配器}可将输入魔力脉冲分流至多个{魔力池}{.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{火花}用于定向传输魔力,在{植物魔法}{进阶中至关重要.}", QuestStyle.LIGHT_PURPLE, QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE)
                    .line()
                    .line("{在}{魔力池}{与接收方块上各放置火花,即可\"无线传输\"魔力.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{使用}{森林法杖}潜行-{右击}{可移除火花.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BotaniaBlocks.distributor), Task.item(BotaniaItems.spark))
            .dependsOn(MANA));

    public static final Quest PORTABLE_MANA_STORAGE = CHAPTER.quest("portable_mana_storage", q -> q
            .at(23, 8)
            .title("便携式魔力存储")
            .desc(d -> d
                    .line("{魔力石板}是便携式{魔力池}{!}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{投入池中可充/放魔力,使用}{森林法杖}潜行-{右击}{切换模式.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{注:石板不会消失.}", QuestStyle.WHITE))
            .task(Task.item(BotaniaItems.manaTablet))
            .dependsOn(MANA));

    public static final Quest MANA_RING = CHAPTER.quest("mana_ring", q -> q
            .at(22, 9)
            .desc(d -> d.line("{魔力之戒}是可佩戴的{魔力石板}{,}{可装备}{如饰品般使用.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BotaniaItems.manaRing))
            .dependsOn(PORTABLE_MANA_STORAGE));

    public static final Quest MANA_RING_GREATER = CHAPTER.quest("mana_ring_greater", q -> q
            .at(22, 11)
            .desc(d -> d.line("将{魔力之戒}{与泰拉钢锭合成可升级为}{高阶魔力之戒},魔力储存容量提升至4倍.", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.LIGHT_PURPLE))
            .task(Task.item(BotaniaItems.manaRingGreater))
            .dependsOn(MANA_RING));

    public static final Quest FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER = CHAPTER.quest("functional_flower_and_generating_flower", q -> q
            .at(13.5, 9)
            .title("功能花、产能花")
            .desc(d -> d
                    .line("通过使用{花药台}{,我们可以制造多种在冒险过程中能提供帮助的花朵,这些花朵分为两大类:}{功能花}和{产能花}.", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.GREEN)
                    .line()
                    .line("{功能花}是辅助日常事务的花卉.例如{漏斗兰}会像漏斗一样运作,吸收周围一定范围内的物品.部分{功能花}{需要消耗}{魔力}才能运作.", QuestStyle.GREEN, QuestStyle.GRAY, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.LIGHT_PURPLE)
                    .line()
                    .line("{产能花}是通过不同方式产生{魔力}的花卉.最常用的产能花是{火红莲},通过消耗附近煤炭等可燃物来生成魔力.", QuestStyle.GREEN, QuestStyle.LIGHT_PURPLE, QuestStyle.RED)
                    .line()
                    .line("在本任务链中,任务图标为{正方形}的是{产能花}{,而}{圆形}图标代表{功能花}{.您也可以通过}{植物魔典 }{植物魔法篇}查阅各花朵的具体功能.", QuestStyle.RED, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.BLUE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.LIGHT_PURPLE))
            .task(Task.checkmark().title("{功能类植物}", QuestStyle.GREEN))
            .dependsOn(APOTHECARY_DEFAULT)
            .hideUntilDepsVisible(false));

    public static final Quest USE_FIRE_TO_GENERATE_MANA = CHAPTER.quest("use_fire_to_generate_mana", q -> q
            .at(10.5, 12)
            .title("使用{火}产生魔力", QuestStyle.RED)
            .desc(d -> d.line("{火红莲}会吸收附近掉落的可燃物品/方块(逐个吸收),通过焚烧这些材料来生成{魔力}.", QuestStyle.RED, QuestStyle.AQUA))
            .task(Task.item(BotaniaFlowerBlocks.endoflame))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER));

    public static final Quest MANASTAR = CHAPTER.quest("manastar", q -> q
            .at(5.5, 12)
            .desc(d -> d
                    .line("这种杂项花朵用于检测你的{魔力池}{是否处于盈利或亏损状态.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{放置}{魔力指示花}在魔力池旁,观察花色变化:泛红光表示池子正在亏损,泛蓝光则表示有盈余!", QuestStyle.WHITE, QuestStyle.LIGHT_PURPLE))
            .task(Task.item(BotaniaFlowerBlocks.manastar))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest DANDELIFEON = CHAPTER.quest("dandelifeon", q -> q
            .at(7.5, 12)
            .task(Task.item(BotaniaFlowerBlocks.dandelifeon))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ENTROPINNYUM = CHAPTER.quest("entropinnyum", q -> q
            .at(8.5, 11)
            .task(Task.item(BotaniaFlowerBlocks.entropinnyum))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest GOURMARYLLIS = CHAPTER.quest("gourmaryllis", q -> q
            .at(8.5, 12)
            .task(Task.item(BotaniaFlowerBlocks.gourmaryllis))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest HYDROANGEAS = CHAPTER.quest("hydroangeas", q -> q
            .at(4.5, 12)
            .task(Task.item(BotaniaFlowerBlocks.hydroangeas))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .hideUntilDepsVisible(false).hideDependencyLines());

    public static final Quest KEKIMURUS = CHAPTER.quest("kekimurus", q -> q
            .at(8.5, 13)
            .task(Task.item(BotaniaFlowerBlocks.kekimurus))
            .reward(Reward.xp(50))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MUNCHDEW = CHAPTER.quest("munchdew", q -> q
            .at(7.5, 11)
            .task(Task.item(BotaniaFlowerBlocks.munchdew))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest NARSLIMMUS = CHAPTER.quest("narslimmus", q -> q
            .at(6.5, 12)
            .task(Task.item(BotaniaFlowerBlocks.narslimmus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest RAFFLOWSIA = CHAPTER.quest("rafflowsia", q -> q
            .at(4.5, 11)
            .task(Task.item(BotaniaFlowerBlocks.rafflowsia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ROSA_ARCANA = CHAPTER.quest("rosa_arcana", q -> q
            .at(7.5, 13)
            .task(Task.item(BotaniaFlowerBlocks.rosaArcana))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SHULK_ME_NOT = CHAPTER.quest("shulk_me_not", q -> q
            .at(5.5, 13)
            .task(Task.item(BotaniaFlowerBlocks.shulkMeNot))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SPECTROLUS = CHAPTER.quest("spectrolus", q -> q
            .at(6.5, 11)
            .task(Task.item(BotaniaFlowerBlocks.spectrolus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest THERMALILY = CHAPTER.quest("thermalily", q -> q
            .at(6.5, 13)
            .task(Task.item(BotaniaFlowerBlocks.thermalily))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest AGRICARNATION = CHAPTER.quest("agricarnation", q -> q
            .at(4.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.agricarnation))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BELLETHORN = CHAPTER.quest("bellethorn", q -> q
            .at(5.5, 11)
            .task(Task.item(BotaniaFlowerBlocks.bellethorn))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BERGAMUTE = CHAPTER.quest("bergamute", q -> q
            .at(4.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.bergamute))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BUBBELL = CHAPTER.quest("bubbell", q -> q
            .at(5.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.bubbell))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest CLAYCONIA = CHAPTER.quest("clayconia", q -> q
            .at(6.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.clayconia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest DAFFOMILL = CHAPTER.quest("daffomill", q -> q
            .at(6.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.daffomill))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .requireAnyStarted()
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest DREADTHORN = CHAPTER.quest("dreadthorn", q -> q
            .at(5.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.dreadthorn))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest EXOFLAME = CHAPTER.quest("exoflame", q -> q
            .at(7.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.exoflame))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest FALLEN_KANADE = CHAPTER.quest("fallen_kanade", q -> q
            .at(8.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.fallenKanade))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest HEISEI_DREAM = CHAPTER.quest("heisei_dream", q -> q
            .at(8.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.heiseiDream))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest THIS_IS_A_HOPPER = CHAPTER.quest("this_is_a_hopper", q -> q
            .at(10.5, 9)
            .title("这是一个漏斗")
            .task(Task.item(BotaniaFlowerBlocks.hopperhock))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible());

    public static final Quest HYACIDUS = CHAPTER.quest("hyacidus", q -> q
            .at(8.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.hyacidus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest JADED_AMARANTHUS = CHAPTER.quest("jaded_amaranthus", q -> q
            .at(7.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.jadedAmaranthus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest JIYUULIA = CHAPTER.quest("jiyuulia", q -> q
            .at(6.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.jiyuulia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest LOONIUM = CHAPTER.quest("loonium", q -> q
            .at(4.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.loonium))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MARIMORPHOSIS = CHAPTER.quest("marimorphosis", q -> q
            .at(5.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.marimorphosis))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MEDUMONE = CHAPTER.quest("medumone", q -> q
            .at(8.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.medumone))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ORECHID = CHAPTER.quest("orechid", q -> q
            .at(6.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.orechid))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ORECHID_IGNEM = CHAPTER.quest("orechid_ignem", q -> q
            .at(7.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.orechidIgnem))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest POLLIDISIAC = CHAPTER.quest("pollidisiac", q -> q
            .at(7.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.pollidisiac))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest RANNUNCARPUS = CHAPTER.quest("rannuncarpus", q -> q
            .at(3.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.rannuncarpus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SOLEGNOLIA = CHAPTER.quest("solegnolia", q -> q
            .at(3.5, 9)
            .task(Task.item(BotaniaFlowerBlocks.solegnolia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SPECTRANTHEMUM = CHAPTER.quest("spectranthemum", q -> q
            .at(3.5, 8)
            .task(Task.item(BotaniaFlowerBlocks.spectranthemum))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest TANGLEBERRIE = CHAPTER.quest("tangleberrie", q -> q
            .at(3.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.tangleberrie))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest TIGERSEYE = CHAPTER.quest("tigerseye", q -> q
            .at(4.5, 10)
            .task(Task.item(BotaniaFlowerBlocks.tigerseye))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest VINCULOTUS = CHAPTER.quest("vinculotus", q -> q
            .at(5.5, 7)
            .task(Task.item(BotaniaFlowerBlocks.vinculotus))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest NOVICE_SPELL_BOOK = CHAPTER.quest("novice_spell_book", q -> q
            .at(10.5, 2)
            .subtitle("第一本法术书")
            .desc(d -> d
                    .line("要开始使用,你需要先合成一本{新手法术书}.", QuestStyle.YELLOW)
                    .line()
                    .line("这是你创建和存储法术的地方.")
                    .line()
                    .line("按下{C}键将打开\"创建法术\"页面.左侧有多个标签页,但主要的3个是:法术创建、{拾色器}{和使魔.}", QuestStyle.BLUE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{该法术书仅允许你创建和使用一级符纹.要制作更强大的法术,你需要升级法术书!}", QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.NOVICE_SPELLBOOK))
            .dependsOn(WELCOME_TO_THE_MAGIC_LINE));

    public static final Quest ARCHWOOD_TREES = CHAPTER.quest("archwood_trees", q -> q
            .at(14, 2)
            .title("至高木")
            .desc(d -> d.line("为了魔法,我们需要特殊魔法木材!"))
            .task(Task.tag(QuestExternalRefs.TAG_FORGE_LOGS_ARCHWOOD, 1).title("至高木"))
            .dependsOn(NOVICE_SPELL_BOOK));

    public static final Quest LIVINGWOOD = CHAPTER.quest("livingwood", q -> q
            .at(18, 2)
            .title("{活木}", QuestStyle.DARK_GREEN)
            .desc(d -> d.line("使用{白雏菊}将至高木转化为活木!", QuestStyle.GREEN))
            .task(Task.item(BotaniaBlocks.livingwoodLog, 8))
            .dependsOn(OUR_FIRST_FLOWER, ARCHWOOD_TREES));

    public static final Quest BOTANIA_S_UNIVERSAL_TOOL = CHAPTER.quest("botania_s_universal_tool", q -> q
            .at(22, 2)
            .title("{植物魔法}{的万用工具}", QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE)
            .desc(d -> d
                    .line("{森林法杖}是{植物魔法}{的必备工具.}", QuestStyle.GREEN, QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE)
                    .line()
                    .line("{法杖拥有}{绑定}与{功能}双模式:", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.BLUE)
                    .line()
                    .line("{绑定模式}用于连接植物魔法装置,潜行-{右击}{首个方块/花朵,再次潜行-}{右击}{目标即可建立连接.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{功能模式}可旋转方块,如同扳手.", QuestStyle.GREEN))
            .task(Task.item(BotaniaItems.twigWand))
            .dependsOn(LIVINGWOOD));

    public static final Quest INFUSE_MAGIC_MATERIALS = CHAPTER.quest("infuse_magic_materials", q -> q
            .at(25.5, 4)
            .title("灌注魔法材料")
            .desc(d -> d
                    .line("将特定物品投入{魔力池}可灌注{魔力}.", QuestStyle.GREEN, QuestStyle.BLUE)
                    .line()
                    .line("各物品所需魔力请通过JEI查看合成配方."))
            .task(
                    Task.item(BotaniaItems.manaSteel),
                    Task.item(BotaniaItems.manaDiamond),
                    Task.item(TagPrefix.ingot, GTOMaterials.InfusedGold),
                    Task.item(TagPrefix.ingot, GTOMaterials.OriginalBronze))
            .dependsOn(MANA, BOTANIA_S_UNIVERSAL_TOOL));

    public static final Quest MANA_CONDUCTION = CHAPTER.quest("mana_conduction", q -> q
            .at(22, -1)
            .title("魔力传导")
            .desc(d -> d
                    .line("{魔力发射器}通过发射{魔力脉冲}{引导魔力流向.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{使用}{森林法杖}将发射器绑定至方块即可设定脉冲方向.持杖时还能查看魔力缓冲器及{魔力脉冲}{状态.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BotaniaBlocks.manaSpreader))
            .dependsOn(BOTANIA_S_UNIVERSAL_TOOL));

    public static final Quest REDSTONE_SPREADER = CHAPTER.quest("redstone_spreader", q -> q
            .at(22, -3)
            .desc(d -> d
                    .line("将{红石}与{魔力发射器}结合可制作{红石魔力发射器}.", QuestStyle.DARK_RED, QuestStyle.GREEN, QuestStyle.GREEN)
                    .line()
                    .line("该装置可通过红石信号控制,仅在接收到红石脉冲时才会发射魔力脉冲."))
            .task(Task.item(BotaniaBlocks.redstoneSpreader))
            .dependsOn(MANA_CONDUCTION)
            .optional());

    public static final Quest SCRIBES_TABLE = CHAPTER.quest("scribes_table", q -> q
            .at(13.5, -6.5)
            .subtitle("法术强化")
            .desc(d -> d
                    .line("若想施展更强大的法术,需通过{抄写台}为法术典籍{解锁}{新符文.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{符文分为三个等级,每个等级的解锁都需要消耗经验值与特定材料.}", QuestStyle.WHITE)
                    .line()
                    .line("{抄写台}{还可用于将法术刻录至}{法术羊皮纸}{.操作方式:将羊皮纸置于台上,手持法术典籍潜行右击台面选择法术.}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{制作符文时,手持典籍右击}{抄写台}{,检索目标符文后点击底部选择按钮.将所需材料右击投入即可合成符文,使用符文即可习得.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{注:工作台可自动提取周边容器内的材料.}", QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.SCRIBES_BLOCK))
            .dependsOn(INFUSE_MAGIC_MATERIALS)
            .hideDependencyLines());

    public static final Quest TIER_2_RUNES = CHAPTER.quest("tier_2_runes", q -> q
            .at(11, -6.5)
            .title("二级符文")
            .desc(d -> d
                    .line("制作二级符文需消耗5级经验值.")
                    .line()
                    .line("同时需要{法师的}{法术典籍}作为合成媒介.", QuestStyle.BLUE, QuestStyle.GREEN))
            .task(Task.checkmark())
            .dependsOn(SCRIBES_TABLE));

    public static final Quest GLYPH_FLARE = CHAPTER.quest("glyph_flare", q -> q
            .at(8.5, -7.5)
            .task(Task.item(EffectFlare.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_COLD_SNAP = CHAPTER.quest("glyph_cold_snap", q -> q
            .at(8.5, -5.5)
            .task(Task.item(EffectColdSnap.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_SLOWFALL = CHAPTER.quest("glyph_slowfall", q -> q
            .at(6.5, -6.5)
            .task(Task.item(EffectSlowfall.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_CRUSH = CHAPTER.quest("glyph_crush", q -> q
            .at(9.5, -6.5)
            .task(Task.item(EffectCrush.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_SMELT = CHAPTER.quest("glyph_smelt", q -> q
            .at(4.5, -6.5)
            .task(Task.item(EffectSmelt.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_ACCELERATE = CHAPTER.quest("glyph_accelerate", q -> q
            .at(1.5, -7.5)
            .task(Task.item(AugmentAccelerate.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_GROW = CHAPTER.quest("glyph_grow", q -> q
            .at(6.5, -5.5)
            .task(Task.item(EffectGrow.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_DAMPEN = CHAPTER.quest("glyph_dampen", q -> q
            .at(3.5, -7.5)
            .task(Task.item(AugmentDampen.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_INVISIBILITY = CHAPTER.quest("glyph_invisibility", q -> q
            .at(4.5, -5.5)
            .task(Task.item(EffectInvisibility.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_EXTRACT = CHAPTER.quest("glyph_extract", q -> q
            .at(1.5, -5.5)
            .task(Task.item(AugmentExtract.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_DURATION_DOWN = CHAPTER.quest("glyph_duration_down", q -> q
            .at(2.5, -5.5)
            .task(Task.item(AugmentDurationDown.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_EXCHANGE = CHAPTER.quest("glyph_exchange", q -> q
            .at(8.5, -6.5)
            .task(Task.item(EffectExchange.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_CONJURE_WATER = CHAPTER.quest("glyph_conjure_water", q -> q
            .at(7.5, -5.5)
            .task(Task.item(EffectConjureWater.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_FELL = CHAPTER.quest("glyph_fell", q -> q
            .at(9.5, -7.5)
            .task(Task.item(EffectFell.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_EXTEND_TIME = CHAPTER.quest("glyph_extend_time", q -> q
            .at(2.5, -7.5)
            .task(Task.item(AugmentExtendTime.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_HEAL = CHAPTER.quest("glyph_heal", q -> q
            .at(5.5, -5.5)
            .task(Task.item(EffectHeal.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_PIERCE = CHAPTER.quest("glyph_pierce", q -> q
            .at(3.5, -5.5)
            .task(Task.item(AugmentPierce.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_FORTUNE = CHAPTER.quest("glyph_fortune", q -> q
            .at(2.5, -6.5)
            .task(Task.item(AugmentFortune.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_ENDER_INVENTORY = CHAPTER.quest("glyph_ender_inventory", q -> q
            .at(9.5, -5.5)
            .task(Task.item(EffectEnderChest.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_EXPLOSION = CHAPTER.quest("glyph_explosion", q -> q
            .at(7.5, -6.5)
            .task(Task.item(EffectExplosion.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_AOE = CHAPTER.quest("glyph_aoe", q -> q
            .at(1.5, -6.5)
            .task(Task.item(AugmentAOE.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES)
            .requireAnyStarted());

    public static final Quest GLYPH_GRAVITY = CHAPTER.quest("glyph_gravity", q -> q
            .at(7.5, -7.5)
            .task(Task.item(EffectGravity.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest TIER_1_RUNES = CHAPTER.quest("tier_1_runes", q -> q
            .at(11, -3.5)
            .title("一级符文")
            .desc(d -> d.line("制作一级符文需消耗3级经验值."))
            .task(Task.checkmark())
            .dependsOn(SCRIBES_TABLE));

    public static final Quest GLYPH_IGNITE = CHAPTER.quest("glyph_ignite", q -> q
            .at(9.5, -3.5)
            .task(Task.item(EffectIgnite.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_CRAFT = CHAPTER.quest("glyph_craft", q -> q
            .at(9.5, -4.5)
            .task(Task.item(EffectCraft.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_RUNE = CHAPTER.quest("glyph_rune", q -> q
            .at(5.5, -4.5)
            .task(Task.item(EffectRune.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_SNARE = CHAPTER.quest("glyph_snare", q -> q
            .at(8.5, -2.5)
            .task(Task.item(EffectSnare.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_FREEZE = CHAPTER.quest("glyph_freeze", q -> q
            .at(5.5, -3.5)
            .task(Task.item(EffectFreeze.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_DELAY = CHAPTER.quest("glyph_delay", q -> q
            .at(2.5, -2.5)
            .task(Task.item(EffectDelay.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_LIGHT = CHAPTER.quest("glyph_light", q -> q
            .at(7.5, -2.5)
            .task(Task.item(EffectLight.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_PLACE_BLOCK = CHAPTER.quest("glyph_place_block", q -> q
            .at(2.5, -3.5)
            .task(Task.item(EffectPlaceBlock.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_CUT = CHAPTER.quest("glyph_cut", q -> q
            .at(1.5, -4.5)
            .task(Task.item(EffectCut.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_HARM = CHAPTER.quest("glyph_harm", q -> q
            .at(7.5, -3.5)
            .task(Task.item(EffectHarm.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_INTERACT = CHAPTER.quest("glyph_interact", q -> q
            .at(4.5, -2.5)
            .task(Task.item(EffectInteract.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_AMPLIFY = CHAPTER.quest("glyph_amplify", q -> q
            .at(0.5, -3)
            .task(Task.item(AugmentAmplify.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_PHANTOM_BLOCK = CHAPTER.quest("glyph_phantom_block", q -> q
            .at(3.5, -2.5)
            .task(Task.item(EffectPhantomBlock.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_LEAP = CHAPTER.quest("glyph_leap", q -> q
            .at(7.5, -4.5)
            .task(Task.item(EffectLeap.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_REDSTONE_SIGNAL = CHAPTER.quest("glyph_redstone_signal", q -> q
            .at(8.5, -4.5)
            .task(Task.item(EffectRedstone.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_HARVEST = CHAPTER.quest("glyph_harvest", q -> q
            .at(6.5, -3.5)
            .task(Task.item(EffectHarvest.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_BREAK = CHAPTER.quest("glyph_break", q -> q
            .at(3.5, -4.5)
            .task(Task.item(EffectBreak.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_PICKUP = CHAPTER.quest("glyph_pickup", q -> q
            .at(1.5, -2.5)
            .task(Task.item(EffectPickup.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_LAUNCH = CHAPTER.quest("glyph_launch", q -> q
            .at(4.5, -4.5)
            .task(Task.item(EffectLaunch.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_DISPEL = CHAPTER.quest("glyph_dispel", q -> q
            .at(5.5, -2.5)
            .task(Task.item(EffectDispel.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_PULL = CHAPTER.quest("glyph_pull", q -> q
            .at(8.5, -3.5)
            .task(Task.item(EffectPull.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_GUST = CHAPTER.quest("glyph_gust", q -> q
            .at(4.5, -3.5)
            .task(Task.item(EffectKnockback.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest TIER_3_RUNES = CHAPTER.quest("tier_3_runes", q -> q
            .at(11, -9.5)
            .title("三级符文")
            .desc(d -> d
                    .line("制作三级符文需消耗10级经验值.")
                    .line()
                    .line("需使用{大法师的}{法术典籍}进行合成.", QuestStyle.YELLOW, QuestStyle.GREEN))
            .task(Task.checkmark())
            .dependsOn(SCRIBES_TABLE));

    public static final Quest GLYPH_INTANGIBLE = CHAPTER.quest("glyph_intangible", q -> q
            .at(6.5, -10.5)
            .task(Task.item(EffectIntangible.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_SPLIT = CHAPTER.quest("glyph_split", q -> q
            .at(5.5, -9.5)
            .task(Task.item(AugmentSplit.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_SUMMON_VEX = CHAPTER.quest("glyph_summon_vex", q -> q
            .at(6.5, -8.5)
            .task(Task.item(EffectSummonVex.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_LIGHTNING = CHAPTER.quest("glyph_lightning", q -> q
            .at(8.5, -10.5)
            .task(Task.item(EffectLightning.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_BLINK = CHAPTER.quest("glyph_blink", q -> q
            .at(7.5, -9.5)
            .task(Task.item(EffectBlink.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_FANGS = CHAPTER.quest("glyph_fangs", q -> q
            .at(8.5, -9.5)
            .task(Task.item(EffectFangs.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_WITHER = CHAPTER.quest("glyph_wither", q -> q
            .at(8.5, -8.5)
            .task(Task.item(EffectWither.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_SUMMON_STEED = CHAPTER.quest("glyph_summon_steed", q -> q
            .at(6.5, -4.5)
            .task(Task.item(EffectSummonSteed.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_SUMMON_WOLVES = CHAPTER.quest("glyph_summon_wolves", q -> q
            .at(9.5, -2.5)
            .task(Task.item(EffectSummonWolves.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_UNDERFOOT = CHAPTER.quest("glyph_underfoot", q -> q
            .at(1.5, -3.5)
            .task(Task.item(MethodUnderfoot.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_SUMMON_DECOY = CHAPTER.quest("glyph_summon_decoy", q -> q
            .at(7.5, -8.5)
            .task(Task.item(EffectSummonDecoy.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_HEX = CHAPTER.quest("glyph_hex", q -> q
            .at(6.5, -9.5)
            .task(Task.item(EffectHex.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest IMBUEMENT_CHAMBER = CHAPTER.quest("imbuement_chamber", q -> q
            .at(25.5, 0)
            .subtitle("制造 {魔源水晶}", QuestStyle.GREEN)
            .desc(d -> d
                    .line("要制作奥术核心合成材料{魔源水晶},我们'需要先建造{灌注室}.", QuestStyle.GREEN, QuestStyle.GREEN)
                    .line()
                    .line("{灌注室}{需消耗源能进行物品灌注.其本身会产生微量源能,也可使用}{魔源罐}{作为能源.}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{部分配方还要求在其周围布置}{奥术基座}{.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.IMBUEMENT_BLOCK))
            .dependsOn(INFUSE_MAGIC_MATERIALS));

    public static final Quest GLYPH_ORBIT = CHAPTER.quest("glyph_orbit", q -> q
            .at(9.5, -9.5)
            .task(Task.item(EffectOrbit.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_SENSITIVE = CHAPTER.quest("glyph_sensitive", q -> q
            .at(0.5, -4)
            .task(Task.item(AugmentSensitive.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_DECELERATE = CHAPTER.quest("glyph_decelerate", q -> q
            .at(3.5, -6.5)
            .task(Task.item(AugmentDecelerate.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_GLIDE = CHAPTER.quest("glyph_glide", q -> q
            .at(7.5, -10.5)
            .task(Task.item(EffectGlide.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_WIND_SHEAR = CHAPTER.quest("glyph_wind_shear", q -> q
            .at(5.5, -6.5)
            .task(Task.item(EffectWindshear.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_SUMMON_UNDEAD = CHAPTER.quest("glyph_summon_undead", q -> q
            .at(9.5, -10.5)
            .task(Task.item(EffectSummonUndead.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_LINGER = CHAPTER.quest("glyph_linger", q -> q
            .at(9.5, -8.5)
            .task(Task.item(EffectLinger.INSTANCE.getGlyph()))
            .dependsOn(TIER_3_RUNES));

    public static final Quest GLYPH_EVAPORATE = CHAPTER.quest("glyph_evaporate", q -> q
            .at(2.5, -4.5)
            .task(Task.item(EffectEvaporate.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_BOUNCE = CHAPTER.quest("glyph_bounce", q -> q
            .at(3.5, -3.5)
            .task(Task.item(EffectBounce.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest GLYPH_SENSE_MAGIC = CHAPTER.quest("glyph_sense_magic", q -> q
            .at(5.5, -7.5)
            .task(Task.item(EffectSenseMagic.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_NAME = CHAPTER.quest("glyph_name", q -> q
            .at(4.5, -7.5)
            .task(Task.item(EffectName.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_FIREWORK = CHAPTER.quest("glyph_firework", q -> q
            .at(6.5, -7.5)
            .task(Task.item(EffectFirework.INSTANCE.getGlyph()))
            .dependsOn(TIER_2_RUNES));

    public static final Quest GLYPH_TOSS = CHAPTER.quest("glyph_toss", q -> q
            .at(6.5, -2.5)
            .task(Task.item(EffectToss.INSTANCE.getGlyph()))
            .dependsOn(TIER_1_RUNES));

    public static final Quest Q_5CFBA24B = CHAPTER.quest("q_5cfba24b", q -> q
            .at(10.5, -1)
            .icon(ItemsRegistry.SPELL_PARCHMENT)
            .desc(d -> d
                    .line("手持法术书时按C键可打开法术创建界面.")
                    .line()
                    .line("每个法术必须包含施法形态.初始拥有三种基础形态:投射型、自身施放型与接触型.")
                    .line()
                    .line("效果部分决定法术生效方式,单个法术最多可叠加9种效果.")
                    .line()
                    .line("新手阶段可使用伤害与破坏效果.")
                    .line()
                    .line("选择形态与效果组合,命名后即可创建新法术."))
            .task(Task.checkmark().title("创建你的第一个法术"))
            .dependsOn(NOVICE_SPELL_BOOK));

    public static final Quest Q_6B511C8B = CHAPTER.quest("q_6b511c8b", q -> q
            .at(8.5, -1)
            .iconStack(() -> QuestStacks.potion(Items.POTION, ModPotions.MANA_REGEN_POTION.get()))
            .subtitle("{魔能}", QuestStyle.GREEN)
            .desc(d -> d
                    .line("屏幕左下方的能量条代表你的魔力池!")
                    .line()
                    .line("随着模组进程,可通过多种方式提升魔力上限或施法效率.升级法术书也能增强魔力!"))
            .task(Task.checkmark().title("魔力"))
            .dependsOn(NOVICE_SPELL_BOOK));

    public static final Quest SOURCE_CRYSTAL = CHAPTER.quest("source_crystal", q -> q
            .at(25.5, -4)
            .title("{魔源水晶}", QuestStyle.GREEN)
            .desc(d -> d.line("制作{魔源水晶}{需将}{猫眼石}或{橄榄石}{放入}{灌注室}{,经过转化即可获得!}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.SOURCE_GEM, 2))
            .dependsOn(IMBUEMENT_CHAMBER));

    public static final Quest SOURCE_JAR = CHAPTER.quest("source_jar", q -> q
            .at(25.5, -7)
            .subtitle("储存源能")
            .desc(d -> d
                    .line("在{新生魔艺}{中,能源称作}{魔源}.", QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE, QuestStyle.BLUE)
                    .line()
                    .line("初始收集魔源需{魔源罐}{.}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.SOURCE_JAR))
            .dependsOn(SOURCE_CRYSTAL));

    public static final Quest POTION_JAR = CHAPTER.quest("potion_jar", q -> q
            .at(28, -7)
            .subtitle("存储药水")
            .desc(d -> d
                    .line("{药水罐}最多储存100瓶药水.使用空瓶或药水烧瓶可取出.", QuestStyle.GREEN)
                    .line()
                    .line("小精灵会在自动药水合成时使用这些容器."))
            .task(Task.item(BlockRegistry.POTION_JAR))
            .dependsOn(SOURCE_JAR));

    public static final Quest VOLCANIC_SOURCELINK = CHAPTER.quest("volcanic_sourcelink", q -> q
            .at(27, -6.5)
            .desc(d -> d
                    .line("{火山魔源通道}{通过消耗可燃物生成源能.}{至高木}{可产生额外魔源.}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{燃烧过程中会将周边石头转化为熔岩.}", QuestStyle.WHITE)
                    .line()
                    .line("{同时生成}{熔岩睡莲}{.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.VOLCANIC_BLOCK))
            .dependsOn(SOURCE_JAR));

    public static final Quest VITALIC_SOURCELINK = CHAPTER.quest("vitalic_sourcelink", q -> q
            .at(27, -7.5)
            .subtitle("通过生物死亡和动物繁殖生成魔源")
            .task(Task.item(BlockRegistry.VITALIC_BLOCK))
            .dependsOn(SOURCE_JAR));

    public static final Quest ALCHEMICAL_SOURCELINK = CHAPTER.quest("alchemical_sourcelink", q -> q
            .at(29, -7)
            .subtitle("从药水得到魔源")
            .desc(d -> d
                    .line("{炼金魔源通道}可从相邻药水罐中提取源力.", QuestStyle.GREEN)
                    .line()
                    .line("提取量根据药水种类与炼制复杂度而异."))
            .task(Task.item(BlockRegistry.ALCHEMICAL_BLOCK))
            .dependsOn(POTION_JAR));

    public static final Quest MYCELIAL_SOURCELINK = CHAPTER.quest("mycelial_sourcelink", q -> q
            .at(26.5, -8.5)
            .subtitle("通过食物获取魔源")
            .task(Task.item(BlockRegistry.MYCELIAL_BLOCK))
            .dependsOn(SOURCE_JAR));

    public static final Quest AGRONOMIC_SOURCELINK = CHAPTER.quest("agronomic_sourcelink", q -> q
            .at(26.5, -5.5)
            .subtitle("利用植物生长生产魔源")
            .desc(d -> d
                    .line("放置于生长中的植物或树苗附近可产生魔源.至高木提供额外魔源!")
                    .line()
                    .line("注:骨粉催生不产生魔源."))
            .task(Task.item(BlockRegistry.AGRONOMIC_SOURCELINK))
            .dependsOn(SOURCE_JAR));

    public static final Quest SOURCESTONE = CHAPTER.quest("sourcestone", q -> q
            .at(29, -4)
            .task(Task.item(QuestExternalRefs.SOURCESTONE))
            .dependsOn(SOURCE_CRYSTAL));

    public static final Quest ARCANE_PEDESTAL = CHAPTER.quest("arcane_pedestal", q -> q
            .at(29, 0)
            .title("{奥术基座}", QuestStyle.GREEN)
            .subtitle("神秘工作台")
            .desc(d -> d
                    .line("{奥术基座}{是构建}{仪式火盆}{和使用}{附魔装置}{合成物品的必备组件.}", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{建议先制作8个备用.}", QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.ARCANE_PEDESTAL, 8))
            .dependsOn(SOURCESTONE));

    public static final Quest ENCHANTING_APPARATUS = CHAPTER.quest("enchanting_apparatus", q -> q
            .at(33, 0)
            .title("{附魔装置}", QuestStyle.GREEN)
            .desc(d -> d
                    .line("{附魔装置}用于制作模组内各类物品,需消耗源能运作.", QuestStyle.GREEN)
                    .line()
                    .line("该装置为多块结构,需配合{奥术基座}{进行合成.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{首先在地面放置}{奥术核心}{,再将}{附魔装置}{置于其上.最后用}{奥术基座}{环绕装置布置.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.ENCHANTING_APP_BLOCK), Task.item(BlockRegistry.ARCANE_CORE_BLOCK))
            .dependsOn(ARCANE_PEDESTAL));

    public static final Quest RITUAL_BRAZIER = CHAPTER.quest("ritual_brazier", q -> q
            .at(29, 4)
            .desc(d -> d
                    .line("{仪式火盆}用于施放仪式,也可作为装饰.点燃火盆需对其使用光亮法术.", QuestStyle.GREEN)
                    .line()
                    .line("进行仪式还需{奥术基座}{.因存在众多仪式类型,请使用}{破旧笔记}查阅每种仪式!", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.YELLOW)
                    .line()
                    .line("手持典籍时在{仪式台}{按Ctrl键即可操作.}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BlockRegistry.RITUAL_BLOCK))
            .dependsOn(ARCANE_PEDESTAL));

    public static final Quest RITUAL_SUNRISE = CHAPTER.quest("ritual_sunrise", q -> q
            .at(30.5, 7.5)
            .task(Task.item(QuestExternalRefs.RITUAL_SUNRISE))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_WARPING = CHAPTER.quest("ritual_warping", q -> q
            .at(29.75, 5.5)
            .task(Task.item(QuestExternalRefs.RITUAL_WARPING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_OVERGROWTH = CHAPTER.quest("ritual_overgrowth", q -> q
            .at(29, 6.25)
            .task(Task.item(QuestExternalRefs.RITUAL_OVERGROWTH))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_MOONFALL = CHAPTER.quest("ritual_moonfall", q -> q
            .at(27.5, 7.5)
            .task(Task.item(QuestExternalRefs.RITUAL_MOONFALL))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_FERTILITY = CHAPTER.quest("ritual_fertility", q -> q
            .at(28.25, 5.5)
            .task(Task.item(QuestExternalRefs.RITUAL_FERTILITY))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_BURROWING = CHAPTER.quest("ritual_burrowing", q -> q
            .at(27.5, 6.25)
            .task(Task.item(QuestExternalRefs.RITUAL_BURROWING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_CHALLENGE = CHAPTER.quest("ritual_challenge", q -> q
            .at(29, 7.5)
            .task(Task.item(QuestExternalRefs.RITUAL_CHALLENGE))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_CLOUDSHAPING = CHAPTER.quest("ritual_cloudshaping", q -> q
            .at(27.5, 4.75)
            .task(Task.item(QuestExternalRefs.RITUAL_CLOUDSHAPING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_DISINTEGRATION = CHAPTER.quest("ritual_disintegration", q -> q
            .at(30.5, 4.75)
            .task(Task.item(QuestExternalRefs.RITUAL_DISINTEGRATION))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_RESTORATION = CHAPTER.quest("ritual_restoration", q -> q
            .at(29.75, 8.25)
            .task(Task.item(QuestExternalRefs.RITUAL_RESTORATION))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest MAGEBLOOM_SEED = CHAPTER.quest("magebloom_seed", q -> q
            .at(33, 3)
            .title("{法师之花种子}", QuestStyle.GREEN)
            .subtitle("培育魔法")
            .desc(d -> d
                    .line("利用{附魔装置}{结构,我们将制作首个种子——}{法师之花种子}.", QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN)
                    .line()
                    .line("用于培育魔法服饰材料!"))
            .task(Task.item(BlockRegistry.MAGE_BLOOM_CROP, 3))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest MAGEBLOOM_FIBER = CHAPTER.quest("magebloom_fiber", q -> q
            .at(33, 7)
            .desc(d -> d
                    .line("当魔灵花完成生长周期后,可用其花瓣制作魔法纤维.")
                    .line()
                    .line("这些纤维用于合成多种物品,包括您的第一套魔法护甲!"))
            .task(Task.item(ItemsRegistry.MAGE_FIBER))
            .dependsOn(MAGEBLOOM_SEED));

    public static final Quest RITUAL_AWAKENING = CHAPTER.quest("ritual_awakening", q -> q
            .at(28.25, 8.25)
            .task(Task.item(QuestExternalRefs.RITUAL_AWAKENING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_FLIGHT = CHAPTER.quest("ritual_flight", q -> q
            .at(29.75, 7)
            .task(Task.item(QuestExternalRefs.RITUAL_FLIGHT))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_WILDEN_SUMMON = CHAPTER.quest("ritual_wilden_summon", q -> q
            .at(29, 9)
            .task(Task.item(QuestExternalRefs.RITUAL_WILDEN_SUMMON))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_SCRYING = CHAPTER.quest("ritual_scrying", q -> q
            .at(28.25, 7)
            .task(Task.item(QuestExternalRefs.RITUAL_SCRYING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest RITUAL_BINDING = CHAPTER.quest("ritual_binding", q -> q
            .at(30.5, 6.25)
            .task(Task.item(QuestExternalRefs.RITUAL_BINDING))
            .dependsOn(RITUAL_BRAZIER));

    public static final Quest SPELL_PARCHMENT = CHAPTER.quest("spell_parchment", q -> q
            .at(33, 11)
            .desc(d -> d.line("需配合{抄写台}{进行法术铭刻}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.SPELL_PARCHMENT))
            .dependsOn(MAGEBLOOM_FIBER));

    public static final Quest RUNIC_CHALK = CHAPTER.quest("runic_chalk", q -> q
            .at(36, 11)
            .subtitle("可布设法术")
            .desc(d -> d
                    .line("{符文粉笔}{用于在地面绘制永久符文.这些符文会对经过的生物施法.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{要将法术附着于符文,需用}{抄写台}{将法术铭刻到}{法术羊皮纸}上.", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN)
                    .line()
                    .line("注:{符文}{需要源能驱动.}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.RUNIC_CHALK))
            .dependsOn(SPELL_PARCHMENT));

    public static final Quest SUMMON_ASSISTANCE = CHAPTER.quest("summon_assistance", q -> q
            .at(33, -5)
            .title("召唤辅助!")
            .desc(d -> d.line("{支配之杖}{用于操控世界中遭遇的生物!每类生物对法杖反应各异,请务必查阅破旧笔记!}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.DOMINION_ROD))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest STARBUNCLE_CHARM = CHAPTER.quest("starbuncle_charm", q -> q
            .at(33.75, -6.5)
            .task(Task.item(ItemsRegistry.STARBUNCLE_CHARM))
            .dependsOn(SUMMON_ASSISTANCE)
            .optional());

    public static final Quest WIXIE_CHARM = CHAPTER.quest("wixie_charm", q -> q
            .at(32.25, -6.5)
            .task(Task.item(ItemsRegistry.WIXIE_CHARM))
            .dependsOn(SUMMON_ASSISTANCE)
            .optional());

    public static final Quest WHIRLISPRIG_CHARM = CHAPTER.quest("whirlisprig_charm", q -> q
            .at(33, -6)
            .task(Task.item(ItemsRegistry.WHIRLISPRIG_CHARM))
            .dependsOn(SUMMON_ASSISTANCE)
            .optional());

    public static final Quest DRYGMY_CHARM = CHAPTER.quest("drygmy_charm", q -> q
            .at(34.5, -6)
            .task(Task.item(ItemsRegistry.DRYGMY_CHARM))
            .dependsOn(SUMMON_ASSISTANCE)
            .optional());

    public static final Quest AMETHYST_GOLEM_CHARM = CHAPTER.quest("amethyst_golem_charm", q -> q
            .at(31.5, -6)
            .task(Task.item(ItemsRegistry.AMETHYST_GOLEM_CHARM))
            .dependsOn(SUMMON_ASSISTANCE)
            .optional());

    public static final Quest ENCHANTERS_SWORD = CHAPTER.quest("enchanters_sword", q -> q
            .at(31.5, -3.5)
            .desc(d -> d
                    .line("{巫师之剑}允许附加接触类法术.", QuestStyle.GREEN)
                    .line()
                    .line("剑上所有法术的末位效果将获得1级增幅强化.")
                    .line()
                    .line("通过{抄写台}{铭刻法术时,请勿使用形态符.}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.ENCHANTERS_SWORD))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest ENCHANTERS_SHIELD = CHAPTER.quest("enchanters_shield", q -> q
            .at(33.75, -4)
            .desc(d -> d
                    .line("格挡伤害时,{巫师之盾}将赋予使用者短时效的{魔力再生}{与}{法术伤害提升}{.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{此外,盾牌会持续消耗佩戴者魔力进行自我修复.}", QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.ENCHANTERS_SHIELD))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest ENCHANTERS_MIRROR = CHAPTER.quest("enchanters_mirror", q -> q
            .at(33, -3.5)
            .desc(d -> d
                    .line("{巫师之镜}使用时可触发自身法术.", QuestStyle.GREEN)
                    .line()
                    .line("通过此镜施放的法术耗能降低且持续时间延长.")
                    .line()
                    .line("通过抄写台铭刻时,请勿使用形态符."))
            .task(Task.item(ItemsRegistry.ENCHANTERS_MIRROR))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest SPELL_BOW = CHAPTER.quest("spell_bow", q -> q
            .at(32.25, -4)
            .desc(d -> d
                    .line("此弓可通过{抄写台}{铭刻法术.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{消耗魔力将箭矢转化为}{法术箭矢}{,命中目标后触发铭刻法术.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{若无箭矢,将发射零伤害法术箭；魔力不足时发射普通箭矢.}", QuestStyle.WHITE)
                    .line()
                    .line("{巫师之弓}还可使用特殊强化箭矢提升法术威能.", QuestStyle.GREEN))
            .task(Task.item(ItemsRegistry.SPELL_BOW))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest JAR_OF_LIGHT = CHAPTER.quest("jar_of_light", q -> q
            .at(33, -2)
            .desc(d -> d
                    .line("{光之瓶}召唤环绕使用者的悬浮光源.", QuestStyle.GREEN)
                    .line()
                    .line("{湮灭之瓶}以消耗魔力为代价销毁拾取物品(可设置过滤).", QuestStyle.GREEN)
                    .line()
                    .line("配置湮灭物品:副手持物点击瓶体,或将瓶置于{抄写台}{后放置物品.}", QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{需置于快捷栏生效.}", QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.JAR_OF_LIGHT), Task.item(ItemsRegistry.VOID_JAR))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest RING_OF_LESSER_DISCOUNT = CHAPTER.quest("ring_of_lesser_discount", q -> q
            .at(31.5, -2)
            .desc(d -> d.line("{降耗戒指}{可小幅提升魔力上限与恢复速度,同时降低法术施放总消耗.}", QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.RING_OF_LESSER_DISCOUNT), Task.item(ItemsRegistry.RING_OF_GREATER_DISCOUNT))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest BELT_OF_UNSTABLE_GIFTS = CHAPTER.quest("belt_of_unstable_gifts", q -> q
            .at(34.5, -2)
            .desc(d -> d.line("此腰带可随机赋予短期增益药水效果,效果强度存在波动."))
            .task(Task.item(ItemsRegistry.BELT_OF_UNSTABLE_GIFTS))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest BELT_OF_LEVITATION = CHAPTER.quest("belt_of_levitation", q -> q
            .at(33.75, -2.5)
            .desc(d -> d
                    .line("你是否想过走到哪里都能悬浮移动？")
                    .line()
                    .line("这条腰带赋予你浮空能力.激活方式:在空中潜行(坠落或跳跃时均可)"))
            .task(Task.item(ItemsRegistry.BELT_OF_LEVITATION))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest TALISMAN = CHAPTER.quest("talisman", q -> q
            .at(32.25, -2.5)
            .title("{护符}", QuestStyle.GREEN)
            .desc(d -> d
                    .line("{魔力提升护身符}可增加最大魔力值.", QuestStyle.GREEN)
                    .line()
                    .line("{魔力再生护身符}可提升魔力恢复速度.", QuestStyle.GREEN))
            .task(Task.item(ItemsRegistry.AMULET_OF_MANA_BOOST), Task.item(ItemsRegistry.AMULET_OF_MANA_REGEN))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest WAND = CHAPTER.quest("wand", q -> q
            .at(34.5, -3.5)
            .desc(d -> d
                    .line("{施法者魔杖}仅能铭刻单个法术,需通过{抄写台}{进行铭文刻录.}", QuestStyle.BLUE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{魔杖法术必须从投射物>加速开始,且必须刻录没有其他释放方式(如触碰、自身等)的法术.}", QuestStyle.WHITE)
                    .line()
                    .line("{此方式可突破10个法术的上限.若想使用破坏术,直接单独刻录破坏法术即可.}", QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.WAND))
            .dependsOn(ENCHANTING_APPARATUS));

    public static final Quest LIVINGCLAY_BLOCK = CHAPTER.quest("livingclay_block", q -> q
            .at(18, 4)
            .desc(d -> d.line("使用{白雏菊}将泥土转化为活土!", QuestStyle.GREEN))
            .task(Task.item(TagPrefix.block, GTOMaterials.Livingclay))
            .dependsOn(OUR_FIRST_FLOWER));

    public static final Quest LIVINGSTEEL_INGOT = CHAPTER.quest("livingsteel_ingot", q -> q
            .at(21, 4)
            .desc(d -> d.line("将{ 活石} {活木} {活土} 粉碎后与钢粉混合 得到 {活铁} (钢?)", QuestStyle.YELLOW, QuestStyle.YELLOW, QuestStyle.YELLOW, QuestStyle.GREEN))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Livingsteel))
            .dependsOn(LIVINGROCK, LIVINGWOOD, LIVINGCLAY_BLOCK));

    public static final Quest APPRENTICE_SPELL_BOOK = CHAPTER.quest("apprentice_spell_book", q -> q
            .at(12.5, -1)
            .desc(d -> d
                    .line("这是法术书的进阶升级版!")
                    .line()
                    .line("将提升总体魔力值与魔力恢复速度,并允许你创建和使用二级符纹."))
            .task(Task.item(ItemsRegistry.APPRENTICE_SPELLBOOK))
            .dependsOn(NOVICE_SPELL_BOOK, LIVINGSTEEL_INGOT)
            .hideDependencyLines());

    public static final Quest FROZEN_PEARL = CHAPTER.quest("frozen_pearl", q -> q
            .at(36, 0)
            .desc(d -> d.line("你可以使用 {锯 }来采集冰块", QuestStyle.DARK_PURPLE))
            .task(Task.item(ModItems.itemFrozenPearl.get()))
            .dependsOn(ENCHANTING_APPARATUS)
            .requireAny());

    public static final Quest MANA_PEARL = CHAPTER.quest("mana_pearl", q -> q
            .at(39, 0)
            .task(Task.item(BotaniaItems.manaPearl))
            .dependsOn(FROZEN_PEARL));

    public static final Quest ALCHEMY_CATALYST = CHAPTER.quest("alchemy_catalyst", q -> q
            .at(36, 3)
            .desc(d -> d.line("将{炼金催化器}置于{魔力池}下方可开启{炼金术},实现物品转化(如腐肉变皮革).", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.BLUE))
            .task(Task.item(BotaniaBlocks.alchemyCatalyst))
            .dependsOn(MANA_PEARL));

    public static final Quest ARCHMAGE_SPELL_BOOK = CHAPTER.quest("archmage_spell_book", q -> q
            .at(14.5, -1)
            .desc(d -> d
                    .line("要制作最终阶法术书,你需要击败{荒野奇美拉}.", QuestStyle.GREEN)
                    .line()
                    .line("这是需要通过{仪式火盆}完成的{魔法仪式}{.}", QuestStyle.GREEN, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(ItemsRegistry.ARCHMAGE_SPELLBOOK))
            .dependsOn(APPRENTICE_SPELL_BOOK, RITUAL_WILDEN_SUMMON, MANA_PEARL)
            .hideDependencyLines());

    public static final Quest COLORFUL_MYSTICAL_FLOWER = CHAPTER.quest("colorful_mystical_flower", q -> q
            .at(12, 4.5)
            .subtitle("有什么用呢")
            .task(Task.item(GTOItems.COLORFUL_MYSTICAL_FLOWER))
            .dependsOn(APOTHECARY_DEFAULT)
            .hideDependencyLines());

    public static final Quest MANA_STRING = CHAPTER.quest("mana_string", q -> q
            .at(36, 6)
            .task(Task.item(BotaniaItems.manaString))
            .dependsOn(MAGEBLOOM_FIBER));

    public static final Quest MANA_INFUSER = CHAPTER.quest("mana_infuser", q -> q
            .at(39, 3)
            .subtitle("多方块魔力池")
            .task(Task.item(ManaMultiBlock.MANA_INFUSER))
            .dependsOn(MANA_PEARL));

    public static final Quest DAYBLOOM_MOTIF = CHAPTER.quest("daybloom_motif", q -> q
            .at(13.5, 12)
            .desc(d -> d.line("如果你已经有光伏电站，打开UI放入一组太阳花，你就可以使用它来简易快速地获取魔力！"))
            .task(Task.item(BotaniaBlocks.motifDaybloom, 64))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER));

    public static final Quest FEYSYTHIA = CHAPTER.quest("feysythia", q -> q
            .at(0.5, 13)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.FEYSYTHIA))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest RAINDELETIA = CHAPTER.quest("raindeletia", q -> q
            .at(0.5, 12)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.RAINDELETIA))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest WITHER_ACONITE = CHAPTER.quest("wither_aconite", q -> q
            .at(0.5, 11)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.WITHER_ACONITE))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest REIKARLILY = CHAPTER.quest("reikarlily", q -> q
            .at(1.5, 11)
            .task(Task.item(ExtrabotanyFlowerBlocks.reikarlily))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BELLFLOWER = CHAPTER.quest("bellflower", q -> q
            .at(1.5, 12)
            .task(Task.item(ExtrabotanyFlowerBlocks.bellflower))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest STONESIA = CHAPTER.quest("stonesia", q -> q
            .at(2.5, 13)
            .task(Task.item(ExtrabotanyFlowerBlocks.stonesia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest EDELWEISS = CHAPTER.quest("edelweiss", q -> q
            .at(1.5, 13)
            .task(Task.item(ExtrabotanyFlowerBlocks.edelweiss))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SUNSHINE_LILY = CHAPTER.quest("sunshine_lily", q -> q
            .at(3.5, 12)
            .task(Task.item(ExtrabotanyFlowerBlocks.sunshineLily))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MOONLIGHT_LILY = CHAPTER.quest("moonlight_lily", q -> q
            .at(3.5, 13)
            .task(Task.item(ExtrabotanyFlowerBlocks.moonlightLily))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest TWINSTAR = CHAPTER.quest("twinstar", q -> q
            .at(3.5, 11)
            .task(Task.item(ExtrabotanyFlowerBlocks.twinstar))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest OMNIVIOLET = CHAPTER.quest("omniviolet", q -> q
            .at(2.5, 11)
            .task(Task.item(ExtrabotanyFlowerBlocks.omniviolet))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest TINKLE = CHAPTER.quest("tinkle", q -> q
            .at(4.5, 13)
            .task(Task.item(ExtrabotanyFlowerBlocks.tinkle))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest BLOOD_ENCHANTRESS = CHAPTER.quest("blood_enchantress", q -> q
            .at(2.5, 12)
            .task(Task.item(ExtrabotanyFlowerBlocks.bloodEnchantress))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest TRADE_ORCHID = CHAPTER.quest("trade_orchid", q -> q
            .at(2.5, 7)
            .task(Task.item(ExtrabotanyFlowerBlocks.tradeOrchid))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ANNOYINGFLOWER = CHAPTER.quest("annoyingflower", q -> q
            .at(2.5, 9)
            .task(Task.item(ExtrabotanyFlowerBlocks.annoyingflower))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest WOODIENIA = CHAPTER.quest("woodienia", q -> q
            .at(2.5, 8)
            .task(Task.item(ExtrabotanyFlowerBlocks.woodienia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest SERENITIAN = CHAPTER.quest("serenitian", q -> q
            .at(2.5, 10)
            .task(Task.item(ExtrabotanyFlowerBlocks.serenitian))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MIRROWTUNIA = CHAPTER.quest("mirrowtunia", q -> q
            .at(1.5, 7)
            .task(Task.item(ExtrabotanyFlowerBlocks.mirrowtunia))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest NECROFLEUR = CHAPTER.quest("necrofleur", q -> q
            .at(1.5, 8)
            .task(Task.item(ExtrabotanyFlowerBlocks.necrofleur))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest MANALINK = CHAPTER.quest("manalink", q -> q
            .at(1.5, 9)
            .task(Task.item(ExtrabotanyFlowerBlocks.manalink))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ENCHANTER = CHAPTER.quest("enchanter", q -> q
            .at(1.5, 10)
            .task(Task.item(ExtrabotanyFlowerBlocks.enchanter))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest AQUAPANTHUS = CHAPTER.quest("aquapanthus", q -> q
            .at(0.5, 7)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.AQUAPANTHUS))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest EXOBLAZE = CHAPTER.quest("exoblaze", q -> q
            .at(0.5, 8)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.EXOBLAZE))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest HELLEBORE = CHAPTER.quest("hellebore", q -> q
            .at(0.5, 9)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.HELLEBORE))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest PETRUNIA = CHAPTER.quest("petrunia", q -> q
            .at(0.5, 10)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.PETRUNIA))
            .dependsOn(FUNCTIONAL_FLOWER_AND_GENERATING_FLOWER)
            .optional().hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ELVEN_SPREADER = CHAPTER.quest("elven_spreader", q -> q
            .at(20, -1)
            .desc(d -> d.line("快一些"))
            .task(Task.item(BotaniaBlocks.elvenSpreader))
            .dependsOn(MANA_CONDUCTION, Magic2Chapter.OPEN_WARP_GATE)
            .optional());

    public static final Quest GAIA_SPREADER = CHAPTER.quest("gaia_spreader", q -> q
            .at(18, -1)
            .desc(d -> d.line("更快一些"))
            .task(Task.item(BotaniaBlocks.gaiaSpreader))
            .dependsOn(MANA_CONDUCTION, Magic2Chapter.GAIA)
            .optional());

    public static final Quest MANASTEEL_CASING = CHAPTER.quest("manasteel_casing", q -> q
            .at(24.5, 7)
            .task(Task.item(GTOBlocks.MANASTEEL_CASING))
            .dependsOn(INFUSE_MAGIC_MATERIALS));

    public static final Quest FRACTAL_PETAL_SOLVENT_BUCKET = CHAPTER.quest("fractal_petal_solvent_bucket", q -> q
            .at(20, -5)
            .icon(GTOMaterials.FractalPetalSolvent.getBucket())
            .subtitle("并不是0.1%")
            .desc(d -> d.line("炼药锅的概率机制是一个复杂的函数，不过你只需要几次持续的配方运作就能获得溶剂"))
            .task(Task.item(GTOMaterials.FractalPetalSolvent.getBucket()).optional(), Task.checkmark())
            .dependsOn(COLORFUL_MYSTICAL_FLOWER)
            .hideDependencyLines());

    public static final Quest ELEMENTAL_MAGIC_CRYSTAL = CHAPTER.quest("elemental_magic_crystal", q -> q
            .at(17, -9)
            .title("元素魔晶")
            .desc(d -> d
                    .line("如何你真的找不到矿脉，可以采用异构花加活石转化")
                    .line()
                    .line("获取混沌魔晶即可完成此任务"))
            .task(
                    Task.item(TagPrefix.rawOre, GTOMaterials.PerditioCrystal),
                    Task.item(TagPrefix.rawOre, GTOMaterials.GnomeCrystal).optional(),
                    Task.item(TagPrefix.rawOre, GTOMaterials.SylphCrystal).optional(),
                    Task.item(TagPrefix.rawOre, GTOMaterials.UndineCrystal).optional(),
                    Task.item(TagPrefix.rawOre, GTOMaterials.SalamanderCrystal).optional())
            .dependsOn(MainStoneageChapter.TIME_TO_MINE));

    public static final Quest CRYSTAL_POWDER = CHAPTER.quest("crystal_powder", q -> q
            .at(20, -9)
            .title("魔晶粉")
            .desc(d -> d.line("获取混沌魔晶粉即可完成此任务"))
            .task(
                    Task.item(TagPrefix.dust, GTOMaterials.PerditioCrystal),
                    Task.item(TagPrefix.dust, GTOMaterials.GnomeCrystal).optional(),
                    Task.item(TagPrefix.dust, GTOMaterials.SylphCrystal).optional(),
                    Task.item(TagPrefix.dust, GTOMaterials.UndineCrystal).optional(),
                    Task.item(TagPrefix.dust, GTOMaterials.SalamanderCrystal).optional())
            .dependsOn(ELEMENTAL_MAGIC_CRYSTAL));

    public static final Quest ELEMENTAL_STEAMS = CHAPTER.quest("elemental_steams", q -> q
            .at(22, -7)
            .icon(GTOMaterials.Aether.getBucket())
            .title("元素蒸汽们")
            .desc(d -> d.line("以太蒸汽你会用到很多"))
            .task(
                    Task.item(GTOMaterials.Aether.getBucket()).optional(),
                    Task.item(GTOMaterials.Salamander.getBucket()).optional(),
                    Task.item(GTOMaterials.Undine.getBucket()).optional(),
                    Task.item(GTOMaterials.Sylph.getBucket()).optional(),
                    Task.item(GTOMaterials.Gnome.getBucket()).optional(),
                    Task.checkmark())
            .dependsOn(CRYSTAL_POWDER, FRACTAL_PETAL_SOLVENT_BUCKET));

    public static final Quest VARIOUS_ESSENCES = CHAPTER.quest("various_essences", q -> q
            .at(36, -2)
            .title("各种精华们")
            .desc(d -> d
                    .line("合成操纵之精华即可完成此任务")
                    .line()
                    .line("{这并非是符文祭坛的合成材料，但它们会在下个阶段用到，所以我仍要求你在进入下个阶段前完成此任务}", QuestStyle.GRAY.italic()))
            .task(
                    Task.item(ItemsRegistry.MANIPULATION_ESSENCE),
                    Task.item(ItemsRegistry.ABJURATION_ESSENCE).optional(),
                    Task.item(ItemsRegistry.CONJURATION_ESSENCE).optional(),
                    Task.item(ItemsRegistry.EARTH_ESSENCE).optional(),
                    Task.item(ItemsRegistry.AIR_ESSENCE).optional(),
                    Task.item(ItemsRegistry.FIRE_ESSENCE).optional(),
                    Task.item(ItemsRegistry.WATER_ESSENCE).optional())
            .dependsOn(ELEMENTAL_STEAMS, ENCHANTING_APPARATUS)
            .hideDependencyLines());

    public static final Quest RUNIC_ALTAR = CHAPTER.quest("runic_altar", q -> q
            .at(39, -4)
            .desc(d -> d
                    .line("{符文}是{植物魔法}{中众多高阶合成配方的重要组件,需在}{符文祭坛}{上制作.}", QuestStyle.BLUE, QuestStyle.LIGHT_PURPLE, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{使用符文祭坛时,首先将所需符文的材料放置其上.可通过}{右键点击}{或直接投掷物品完成.祭坛还需魔力供应,请确保连接一台接收魔力的}{魔力发射器}{.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE)
                    .line()
                    .line("{放置完材料后,手持法杖悬停于祭坛上方可查看合成进度.完成后将活石投入祭坛,用法杖收取符文.}", QuestStyle.WHITE)
                    .line()
                    .line("{注意:}{符文祭坛}{配方中使用的符文部分作为催化剂不会被消耗.类似}{花药台}{,配方完成后空手右键点击祭坛可自动填充上次配方所需物品.}", QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE, QuestStyle.GREEN, QuestStyle.WHITE))
            .task(Task.item(BotaniaBlocks.runeAltar))
            .dependsOn(MANA_PEARL, VARIOUS_ESSENCES));

    public static final Quest WILDEN_TRIBUTE = CHAPTER.quest("wilden_tribute", q -> q
            .at(31, 9)
            .task(Task.item(ItemsRegistry.WILDEN_TRIBUTE))
            .dependsOn(RITUAL_WILDEN_SUMMON));

    public static final Quest RITUAL_CONJURE_METEORITES = CHAPTER.quest("ritual_conjure_meteorites", q -> q
            .at(29, 11)
            .desc(d -> d
                    .line("请注意安全。")
                    .line("不建议在家中启用这个仪式。")
                    .line()
                    .line("可以在仪式中添加果实以修改陨石的中心位置。")
                    .image(QuestTextures.GUI_DIRECTION, 100, 100))
            .task(Task.item(QuestExternalRefs.RITUAL_CONJURE_METEORITES))
            .dependsOn(WILDEN_TRIBUTE, VARIOUS_ESSENCES)
            .hideDependencyLines());

    public static final Quest LARGE_PERFUSION_DEVICE = CHAPTER.quest("large_perfusion_device", q -> q
            .at(39, 6)
            .desc(d -> d.line("多方块 { 灌注室  附魔核心}", QuestStyle.DARK_PURPLE))
            .task(Task.item(ManaMultiBlock.LARGE_PERFUSION_DEVICE))
            .dependsOn(WILDEN_TRIBUTE, VARIOUS_ESSENCES, ENCHANTING_APPARATUS)
            .hideDependencyLines());

    public static final Quest WARP_SCROLL = CHAPTER.quest("warp_scroll", q -> q
            .at(36, 8)
            .task(Task.item(ItemsRegistry.WARP_SCROLL))
            .dependsOn(MAGEBLOOM_FIBER));

    public static final Quest STABLE_WARP_SCROLL = CHAPTER.quest("stable_warp_scroll", q -> q
            .at(39, 8)
            .subtitle("魔法的跨维度传送")
            .desc(d -> d
                    .line("用于记录一个传送点进行永久传送，可用于跨维度传送(你也可以用其他办法)")
                    .line()
                    .line("你还可以制作折跃传送门作为一个长期可用的传送门"))
            .task(Task.item(ItemsRegistry.STABLE_WARP_SCROLL))
            .dependsOn(WARP_SCROLL)
            .optional());

    public static final Quest WILDEN_CYCLE_MORE_WILDEN_TRIBUTES = CHAPTER.quest("wilden_cycle_more_wilden_tributes", q -> q
            .at(31, 11)
            .icon(GTOMaterials.WildenEssence.getBucket())
            .title("{荒野循环}  更多的 { 荒野供物}", QuestStyle.DARK_PURPLE, QuestStyle.DARK_PURPLE)
            .task(Task.item(GTOMaterials.WildenEssence.getBucket()).optional(), Task.checkmark())
            .dependsOn(WILDEN_TRIBUTE));

    public static final Quest THE_END_GENESIS = CHAPTER.quest("the_end_genesis", q -> q
            .at(30, -9).minWidth(250)
            .title("末地：创世")
            .desc(d -> d
                    .line("远古之眼（Old Eye）- 在沙漠神庙中找到。")
                    .line("下界之眼（Nether Eye）- 在下界要塞里找到。")
                    .line("寒冰之眼（Cold Eye）- 在雪屋中找到。")
                    .line("无赖之眼（Rogue Eye）- 在丛林神殿中找到。")
                    .line("漆黑之眼（Black Eye）- 从埋藏的箱子中找到。")
                    .line("魔力之眼（Magical Eye）- 唤魔者掉落。从林地府邸的箱子中找到。")
                    .line("迷失之眼（Lost Eye）- 从废弃矿洞中找到。")
                    .line("腐化之眼（Corrupted Eye）- 从掠夺者前哨站中找到。")
                    .line("凋零之眼（Wither Eye）- 凋灵掉落。")
                    .line("守卫者之眼（Guardian Eye）- 远古守卫者掉落。")
                    .line("女巫之眼（Witch Eye）- 使用原版末影之眼与巫师之瞳合成。")
                    .line("诅咒之眼（Cursed Eye）-  {需要前往下界}，在下界堡垒宝箱中找到。", QuestStyle.YELLOW)
                    .line("异域之眼（Exotic Eye）- 使用水产物品制作。")
                    .line("邪恶之眼（Evil Eye）- 从大师级牧师村民手中购买。")
                    .line("神秘之眼（Cryptic Eye）- 在附魔时有小概率获得，也可灌注获得。")
                    .line("亡灵之眼（Undead Eye） - 使用亡灵物品合成，合成所需的不死灵魂需要击杀骷髅马获得。{骷髅马雨天极小概率生成，或植物魔法的骷髅病毒。}", QuestStyle.GRAY.italic())
                    .line()
                    .line("{以上一共}{16个}眼睛，收集{12个}即可打开末地门。", QuestStyle.GRAY.italic(), QuestStyle.AQUA.italic(), QuestStyle.AQUA))
            .task(
                    Task.anyOf(1, ERItems.BLACK_EYE.get(), ERItems.COLD_EYE.get(), ERItems.CORRUPTED_EYE.get(), ERItems.LOST_EYE.get(), ERItems.NETHER_EYE.get(), ERItems.OLD_EYE.get(), ERItems.ROGUE_EYE.get(), ERItems.CURSED_EYE.get(), ERItems.EVIL_EYE.get(), ERItems.GUARDIAN_EYE.get(), ERItems.MAGICAL_EYE.get(), ERItems.WITHER_EYE.get(), ERItems.WITCH_EYE.get(), ERItems.UNDEAD_EYE.get(), ERItems.EXOTIC_EYE.get(), ERItems.CRYPTIC_EYE.get()).title("任意眼睛")));

    public static final Quest GO_TO_THE_END = CHAPTER.quest("go_to_the_end", q -> q
            .at(33, -9)
            .title("前往末地")
            .subtitle("找到十二颗不同的眼睛打开末地之门")
            .desc(d -> d.line("末地缺少氧气，你需要穿戴{航天套装}或者更高级的宇航套装才能在末地存活，否则将一直受到窒息伤害", QuestStyle.YELLOW.underline()))
            .task(Task.dimension(Dimension.THE_END))
            .dependsOn(THE_END_GENESIS));

    public static final Quest ENDER_AIR_BOTTLE = CHAPTER.quest("ender_air_bottle", q -> q
            .at(36, -9)
            .desc(d -> d
                    .line("在末地用玻璃瓶右键获得。")
                    .line("右键丢到石头上，使得以落地点为中心的 9 x 9 x 5 范围内的石头变成末地石。")
                    .line("一个瓶装末地空气最多可以转化64个石头。")
                    .line()
                    .line("但是如果你还无法前往末地。")
                    .line("你可以使用召唤之精华合成末地石。")
                    .line("白雏菊在末地石转换为深板岩圆石后，")
                    .line("会升起一小团的末地空气云团，")
                    .line("手持玻璃瓶可以被收集为一个瓶装末地空气。")
                    .line("也可以使用内部装有空玻璃瓶的发射器来收集。"))
            .task(Task.item(BotaniaItems.enderAirBottle))
            .dependsOn(GO_TO_THE_END)
            .requireAny());

    public static final Quest MANA_STEEL_WING = CHAPTER.quest("mana_steel_wing", q -> q
            .at(39, 11)
            .task(Task.item(GTOOrganItems.MANA_STEEL_WING))
            .dependsOn(INFUSE_MAGIC_MATERIALS)
            .hideDependencyLines());

    public static final Quest PROSPECTOR_MANA_ULV = CHAPTER.quest("prospector_mana_ulv", q -> q
            .at(39, -9)
            .task(Task.item(GTOItems.PROSPECTOR_MANA_ULV))
            .dependsOn(INFUSE_MAGIC_MATERIALS)
            .hideDependencyLines());

    public static final Quest LV_MANA_MACHINE_HULL = CHAPTER.quest("lv_mana_machine_hull", q -> q
            .at(24.5, 9)
            .subtitle("{你需要天马行空般的想象力}", QuestStyle.GRAY)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.LV]))
            .dependsOn(MANASTEEL_CASING));

    public static final Quest LV_MANA_ASSEMBLER = CHAPTER.quest("lv_mana_assembler", q -> q
            .at(24.5, 11)
            .task(Task.item(ManaMachine.MANA_ASSEMBLER[GTValues.LV]))
            .dependsOn(LV_MANA_MACHINE_HULL));

    public static final Quest EXP_OBELISK = CHAPTER.quest("exp_obelisk", q -> q
            .at(26.5, 7)
            .desc(d -> d.line("可以存储经验，贴上存储总线即可连入ME网络"))
            .task(Task.item(ManaMachine.XP_OBELISK))
            .dependsOn(INFUSE_MAGIC_MATERIALS));

    public static final Quest FEL_PUMPKIN = CHAPTER.quest("fel_pumpkin", q -> q
            .at(2, 5)
            .desc(d -> d.line("恶魔南瓜头下面放两个铁栏杆可以召唤必掉烈焰粉的烈焰人"))
            .task(Task.item(BotaniaBlocks.felPumpkin), Task.item(Items.BLAZE_POWDER))
            .reward(Reward.item(Items.BLAZE_POWDER, 4)));

    public static final Quest NORMAL_MANA_PIPE = CHAPTER.quest("normal_mana_pipe", q -> q
            .at(27.2, 1.88)
            .desc(d -> d.line("它可以用来运输魔力或魔源，{并按 4 魔源 = 1 魔力互相换算}", QuestStyle.YELLOW))
            .task(Task.item(GTOBlocks.MANA_PIPES[0]))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(INFUSE_MAGIC_MATERIALS));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.END_PORTAL_FRAME_TOP, 28, -10, 1, 1))
                .link(QuestLink.of(UlvChapter.ALCHEMY_CAULDRON, 17, -5)));
    }
}
