package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class SteamChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("steam", c -> c
            .title("2. 蒸汽时代(Steam Age)")
            .subtitle("Tier0-Steam")
            .icon(GTMachines.STEAM_SOLID_BOILER.first())
            .hideQuestsUntilDepsVisible()
            .autofocus(SteamChapter.MISTY_MOVEMENT));

    public static final Quest MISTY_MOVEMENT = CHAPTER.quest("misty_movement", q -> q
            .at(-9, 4.5)
            .icon(TagPrefix.ingot, GTMaterials.Bronze)
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
            .task(Task.checkmark().title("前进，前进！"))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Bronze, 32))
            .dependsOn(MainStoneageChapter.KEEP_GOING));

    public static final Quest FUSION_POWER = CHAPTER.quest("fusion_power", q -> q
            .at(-6.5, 4.5)
            .title("融合之力")
            .subtitle("Fusion UP!")
            .desc(d -> d
                    .line("合金炉，将{三铜一锡}熔铸四个{青铜锭}，现在能够高效生产各种能在LV制造的合金锭", GOLD, GOLD)
                    .line()
                    .line("尽管如此，低压版本的蒸汽单方块的效率依然不能满足你的后续发展需要，你应该尽快升级到{蒸汽铸造炉}", GOLD)
                    .line()
                    .line("以及，不要堵塞{排气口}，用你的扳手调整它", RED)
                    .line()
                    .line("{16mb/t}的{蒸汽}就足够了", GREEN, GOLD)
                    .line()
                    .line("什么，你问我怎么调整排气口的位置？那你肯定是没仔细看{使用手册}", link(TipsChapter.GT_TOOL_USER_MANUAL))
                    .line()
                    .line("{如果你是专家模式，低压机器还有额外50%耗能惩罚}", GRAY.italic()))
            .task(Task.item(GTMachines.STEAM_ALLOY_SMELTER.first()))
            .dependsOn(MISTY_MOVEMENT));

    public static final Quest SUPER_CON_DUCTOR = CHAPTER.quest("super_con_ductor", q -> q
            .at(-3.5, 4.5)
            .title("超！导！体！")
            .subtitle("Ver.0")
            .desc(d -> d
                    .line("{ULV}时代重要的超导体，现在先用来制作{活塞}", ULV, GOLD)
                    .line()
                    .line("当然还有{非常重要}的{超导线}，你必然不会想错过它的吧？", DARK_PURPLE, DARK_RED))
            .task(Task.item(TagPrefix.ingot, GTMaterials.RedAlloy))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.RedAlloy, 16))
            .dependsOn(FUSION_POWER));

    public static final Quest PIS_TON = CHAPTER.quest("pis_ton", q -> q
            .at(-1, 4.5)
            .title("活...塞？")
            .subtitle("妈妈，我不要这种合成方式！")
            .desc(d -> d.line("特供版活塞，具有{格}{雷}{主}{义}的合成方式，敬请享用", AQUA, YELLOW, AQUA, YELLOW))
            .task(Task.item(Items.PISTON))
            .dependsOn(SUPER_CON_DUCTOR));

    public static final Quest BREAK_THEM = CHAPTER.quest("break_them", q -> q
            .at(-1, 6)
            .title("打碎它们！")
            .subtitle("拯救你的研钵")
            .desc(d -> d
                    .line("通入{蒸汽}，即可粉碎矿石或者是其他零零碎碎的东西", GOLD)
                    .line()
                    .line("什么，你问为什么研磨矿石没有如EMI所示出产{副产物}？", YELLOW)
                    .line()
                    .line("想要研磨矿石出产副产物需要至少{HV级}的{研磨机}，你可以在其他研磨机的tooltips中看到", YELLOW, GOLD))
            .task(Task.item(GTMachines.STEAM_MACERATOR.first()))
            .dependsOn(PIS_TON)
            .hideDependencyLines(false));

    public static final Quest SQUEEZE_YEAH_SQUEEZE = CHAPTER.quest("squeeze_yeah_squeeze", q -> q
            .at(2, 4.5)
            .title("挤！呀！挤！")
            .subtitle("瘦身之旅")
            .desc(d -> d
                    .line("想要炼钢，就要有个能炼钢的炉子，想要有能承受足够高炉温的炉子，就需要足够优秀的外壳材料，那么就需要一个{强力的机器(?)}来生产这种材料了", YELLOW)
                    .line()
                    .line("很明显{低压蒸汽压缩机}会比你手动压缩更加强力...吧？", GOLD)
                    .line()
                    .line("不管怎么说，你需要{低压蒸汽压缩机}来帮你完成耐火砖的准备，而这是建造{土高炉}所必须的", GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_COMPRESSOR.first()))
            .dependsOn(PIS_TON));

    public static final Quest BIG_HAMMER_80_SMALL_HAMMER_40 = CHAPTER.quest("big_hammer_80_small_hammer_40", q -> q
            .at(2, 9)
            .title("大锤80，小锤40！")
            .subtitle("这不是林永健的家")
            .desc(d -> d
                    .line("不仅仅是用于节省你的工具耐久，更是节省原料")
                    .line()
                    .line("用锻造锤制造1个金属板只需要1.5个锭({3个锭制造2个板})", RED)
                    .line()
                    .line("不要堵塞{蒸汽排气口}(再一次提醒！！！！)", DARK_RED)
                    .line()
                    .line("{16mb/t}的{蒸汽}就足够它跑满了", DARK_RED, GOLD))
            .task(Task.item(GTMachines.STEAM_HAMMER.first()))
            .dependsOn(SQUEEZE_YEAH_SQUEEZE));

    public static final Quest CAN_STEAM_BE_USED_FOR_COOKING = CHAPTER.quest("can_steam_be_used_for_cooking", q -> q
            .at(-9, 6.5)
            .title("蒸汽能做饭吗？")
            .subtitle("最大程度节省你的燃料")
            .desc(d -> d
                    .line("小蒸汽机器必须通入{蒸汽}才能运转，而且{排气口}{不能}被阻挡（包括管道）", GOLD, DARK_GREEN, DARK_RED)
                    .line()
                    .line("什么，你问我怎么调整排气口的位置？那你肯定是没仔细看{使用手册}", link(TipsChapter.GT_TOOL_USER_MANUAL))
                    .line()
                    .line("由于巨型熔炉的存在，蒸汽熔炉系列是可选的"))
            .task(Task.item(GTMachines.STEAM_FURNACE.first()))
            .dependsOn(MISTY_MOVEMENT)
            .optional());

    public static final Quest A_SLIGHTLY_LARGER_FURNACE = CHAPTER.quest("a_slightly_larger_furnace", q -> q
            .at(-9, 8.25)
            .title("更大一点的炉")
            .subtitle("作用可不止大了一点")
            .desc(d -> d
                    .line("如果你觉得你的低压蒸汽熔炉还不够，在此隆重介绍：{蒸汽熔炼熔炉}！", GOLD)
                    .line()
                    .line("它有{8并行}，换而言之就是可以同时运行{8个相同的配方}，但它并不能同时运行不同的配方", DARK_RED, YELLOW)
                    .line()
                    .line("即便单个配方的处理时间是小机器的1.5倍，但速度依然{非常快}！", GREEN)
                    .line()
                    .line("记得及时导出产物，{尤其是}某些配方的产出，可能会直接把输出总线撑爆", GREEN)
                    .line()
                    .line("{记得大幅度提升你的蒸汽产能先}", DARK_GRAY.strike()))
            .task(Task.item(GTMultiMachines.STEAM_OVEN))
            .dependsOn(CAN_STEAM_BE_USED_FOR_COOKING)
            .optional());

    public static final Quest LARGER_AUTO_MORTAR = CHAPTER.quest("larger_auto_mortar", q -> q
            .at(-1, 7.5)
            .title("更大的自动研钵")
            .subtitle("效率这一块")
            .desc(d -> d
                    .line("这个家伙具有{8并行}，但跟它的小机器版本一样，{没有副产物产出}", DARK_RED, DARK_RED)
                    .line()
                    .line("普通难度下，单个配方具有{1.5倍}的用时惩罚，但依然是一个非常优秀的选择", DARK_RED))
            .task(Task.item(GTMultiMachines.STEAM_GRINDER))
            .dependsOn(BREAK_THEM));

    public static final Quest THE_HAMMER_FALLS = CHAPTER.quest("the_hammer_falls", q -> q
            .at(0, 9)
            .title("大锤落下！")
            .subtitle("这一下可不止80")
            .desc(d -> d
                    .line("{8并行}的{多方块蒸汽锻造锤}", DARK_RED, GOLD)
                    .line()
                    .line("当然你需要先做出{蒸汽压缩机}来制造锻铁块，以及{蒸汽合金炉}用于制作活塞等等", GOLD, GOLD)
                    .line()
                    .line("{总归是要一步一步慢慢来的嘛}", DARK_GRAY.italic())
                    .line()
                    .line("跟其他的蒸汽小机器升级版多方块一样，差不多{250mb/t}的{蒸汽}就足够它跑满并行了", DARK_RED, GOLD))
            .task(Task.item(MultiBlockA.STEAM_PISTON_HAMMER))
            .dependsOn(BIG_HAMMER_80_SMALL_HAMMER_40));

    public static final Quest A_MESSY_STEW = CHAPTER.quest("a_messy_stew", q -> q
            .at(-6.5, 6.5)
            .title("一锅乱炖")
            .subtitle("但这口锅够大")
            .desc(d -> d
                    .line("这是一个拥有{8并行}的{蒸汽合金炉}", GREEN, GOLD)
                    .line()
                    .line("当然你需要足够的蒸汽，大概是{250mb/t}就足够了，要不然没办法满功率运作它", RED)
                    .line()
                    .line("记得千万别站在{蒸汽排气口}面前，喷出蒸汽的时候是{有伤害的}，也要注意{蒸汽排气口}是否有被{阻挡}", DARK_RED, DARK_PURPLE, DARK_RED, GREEN))
            .task(Task.item(MultiBlockA.STEAM_FOUNDRY))
            .dependsOn(FUSION_POWER));

    public static final Quest THIS_IS_NO_HYDRAULIC_PRESS = CHAPTER.quest("this_is_no_hydraulic_press", q -> q
            .at(2.047619047619044, 2.488095238095248)
            .title("这可不是液压机")
            .subtitle("我们需要更多的土高炉！")
            .desc(d -> d
                    .line("{8并行}，如同之前的其他小机器升级版本一样，也有{1.5倍}的配方时间惩罚", DARK_RED, DARK_RED)
                    .line()
                    .line("当然你现在这个阶段做这个东西肯定是为了做更多的{土高炉}，对吧？", GOLD))
            .task(Task.item(MultiBlockA.STEAM_PRESSOR))
            .dependsOn(SQUEEZE_YEAH_SQUEEZE));

    public static final Quest PREPARE_GLASS_FIRST_STEP = CHAPTER.quest("prepare_glass_first_step", q -> q
            .at(15, 10.5)
            .title("制备玻璃......第一步！")
            .subtitle("保护你的研钵！！")
            .desc(d -> d
                    .line("{石英砂}可以通过研钵在工作台上生产，但{燧石粉}就不行了，你必须要在{研磨机}内完成", YELLOW, GREEN, GOLD)
                    .line()
                    .line("那既然如此，为什么不都在{研磨机}内生产呢？", GOLD)
                    .line()
                    .line("无论如何你都需要准备多点{石英砂}，因为{玻璃粉}的制作配方里面{石英砂}需要更多", YELLOW, LIGHT_PURPLE, YELLOW)
                    .line()
                    .line("你的第一份{玻璃粉}肯定是工作台配方，而后面你就可以使用{搅拌机}来生产玻璃粉了", LIGHT_PURPLE, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.QuartzSand), Task.item(TagPrefix.dust, GTMaterials.Flint))
            .dependsOn(BREAK_THEM)
            .hideDependencyLines());

    public static final Quest HUH_YOU_TOO = CHAPTER.quest("huh_you_too", q -> q
            .at(6.75, 0)
            .title("嗯？怎么你也？")
            .subtitle("噢！是魔法超导体！")
            .desc(d -> d
                    .line("这也是一种{超导体}，只是需要{魔力灌注}", DARK_RED, AQUA)
                    .line()
                    .line("在{ULV}电压，常规超导线非常容易获取（即{红色合金线缆}）。但在后面的电压，常规超导线往往需要推进许多科技才可以制作", ULV, GOLD)
                    .line()
                    .line("因此我强烈推荐你制作每个阶段的{魔法合金}作为超导体", GOLD))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.PulsatingAlloy))
            .reward(Reward.item(TagPrefix.ingot, GTOMaterials.PulsatingAlloy, 16))
            .dependsOn(FUSION_POWER)
            .hideDependencyLines());

    public static final Quest A_LITTLE_DEVICE_THAT_CAN_CREATE = CHAPTER.quest("a_little_device_that_can_create", q -> q
            .at(15, -2)
            .icon(GTOMachines.STEAM_VACUUM_PUMP.first())
            .title("能抽真空的小家伙")
            .subtitle("真不是吸尘器！")
            .desc(d -> d
                    .line("这里就要介绍一下GTO所设立的新机制：{真空等级}", DARK_PURPLE)
                    .line()
                    .line("部分配方需要{真空等级}才能运行，而你将要制作的{真空管}就是其中之一", DARK_PURPLE, GOLD)
                    .line()
                    .line("在小机器上添加{真空条件}需要将{真空泵}{紧贴}在小机器的情况下，正确运行对应{等级}真空泵的{配方}", DARK_PURPLE, GOLD, YELLOW, AQUA, GREEN)
                    .line()
                    .line("高等级的真空等级条件可以执行低等级的真空等级配方，反之不然{(废话)}", DARK_GRAY.strike())
                    .line()
                    .line("低级的真空泵只需要{蒸汽}即可运行，而高级的真空泵则需要{电力}，太空环境则无条件提供{四级真空}，多方块机器由{真空仓}来提供{四级真空}", GOLD, GOLD, DARK_PURPLE, GOLD, DARK_PURPLE))
            .task(Task.item(GTOMachines.STEAM_VACUUM_PUMP.first()))
            .dependsOn(MISTY_MOVEMENT)
            .hideDependencyLines());

    public static final Quest DISCOUNT_TIME_BOOM_BOOM_BOOM = CHAPTER.quest("discount_time_boom_boom_boom", q -> q
            .at(0.5, 7.5)
            .title("打折时刻！爆爆爆！")
            .subtitle("用你的新锻造锤做点东西吧")
            .task(Task.item(TagPrefix.plate, GTMaterials.WroughtIron, 8))
            .reward(Reward.item(Items.IRON_INGOT, 16))
            .dependsOn(BIG_HAMMER_80_SMALL_HAMMER_40));

    public static final Quest IT_SEEMS_LIKE_A_SOMEWHAT_TROUBLESOME = CHAPTER.quest("it_seems_like_a_somewhat_troublesome", q -> q
            .at(4, 4.5)
            .title("看上去有点麻烦的材料")
            .subtitle("麻烦吗，麻烦就对了！")
            .desc(d -> d
                    .line("为了制造一个完整的{土高炉}，你需要准备总共{528个}黏土球，希望你已经找到了一个{庞大}的{繁茂洞穴}", GOLD, AQUA, LIGHT_PURPLE, RED)
                    .line()
                    .line("你需要通过将黏土球压缩成黏土，再{粉碎黏土}才能得到{黏土粉}", YELLOW, GOLD)
                    .line()
                    .line("{砖粉}则可以通过直接{烧制黏土球}变成红砖，再将其在合成台上组装成红砖之后{粉碎}获得", GOLD, YELLOW, YELLOW)
                    .line()
                    .line("如果你找到了{红砖建筑}，那么你需要的黏土球会减少一半", YELLOW)
                    .line()
                    .line("希望你的{半自动粉碎系统}已经准备好了，要不然就老费劲了", GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Fireclay))
            .dependsOn(SQUEEZE_YEAH_SQUEEZE));

    public static final Quest PRIMITIVE_BLAST_FURNACE = CHAPTER.quest("primitive_blast_furnace", q -> q
            .at(6, 2.5)
            .icon(GTMultiMachines.PRIMITIVE_BLAST_FURNACE)
            .subtitle("准备好了吗？")
            .desc(d -> d
                    .line("{土高炉}能够像{焦炉}一样{自}{动}{化}了，试试看吧", GOLD, GOLD, LIGHT_PURPLE, YELLOW, AQUA)
                    .line()
                    .line("{终于不用手搬来搬去的了}", DARK_GRAY.strike())
                    .line()
                    .line("具体的多方块结构参考EMI的“{多方块结构信息}”选项", DARK_GREEN)
                    .line()
                    .line("以及，土高炉运作的时候中间那块岩浆是{有伤害的}", DARK_RED))
            .task(
                    Task.item(GTMultiMachines.PRIMITIVE_BLAST_FURNACE),
                    Task.item(GTOMachines.PRIMITIVE_BLAST_FURNACE_HATCH),
                    Task.item(GTBlocks.CASING_PRIMITIVE_BRICKS, 31))
            .reward(Reward.item(Items.COAL_BLOCK, 4))
            .dependsOn(IT_SEEMS_LIKE_A_SOMEWHAT_TROUBLESOME));

    public static final Quest FIRST_STEEL_INGOT = CHAPTER.quest("first_steel_ingot", q -> q
            .at(9, 2.5)
            .title("第一块钢锭")
            .subtitle("真正的生产力大跃进")
            .desc(d -> d
                    .line("在这个阶段，我建议你使用{木炭/煤炭块}来生产钢。因为这种方式下生产的副产物可以生成{碳粉}，是碳粉前期的获取途径之一", GREEN, YELLOW)
                    .line()
                    .line("{另一种方法是处理滑石/石墨矿}", GRAY.italic()))
            .task(Task.item(ModItems.STEEL_INGOT.get()))
            .reward(Reward.item(ModItems.STEEL_INGOT.get(), 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(PRIMITIVE_BLAST_FURNACE));

    public static final Quest ANCIENT_REACTOR = CHAPTER.quest("ancient_reactor", q -> q
            .at(9, 0)
            .icon(MultiBlockG.ANCIENT_REACTOR_CORE)
            .title("远古反应堆")
            .subtitle("《回到过去》")
            .desc(d -> d
                    .line("用这些材料摆一个3x3的平面结构")
                    .line()
                    .line("关于结构：{远古反应核}置于中心，相邻4个方块为{钢块}，角落4个方块为钻石块，{平放}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{右键远古反应核即可前往远古世界}", GOLD)
                    .line()
                    .line("如果你还不会飞，可以带个{飞机}", link(SteamChapter.ENGINE))
                    .line()
                    .line("{MAN!}{别}{坠}{机}{了}", GOLD, ITALIC.strike(), GOLD.italic().strike(), DARK_PURPLE.strike(), STRIKE))
            .task(Task.item(MultiBlockG.ANCIENT_REACTOR_CORE), Task.item(ModItems.STEEL_BLOCK.get(), 4), Task.item(Items.DIAMOND_BLOCK, 4))
            .dependsOn(HUH_YOU_TOO, FIRST_STEEL_INGOT));

    public static final Quest BACK_TO_THE_PAST = CHAPTER.quest("back_to_the_past", q -> q
            .at(9, -2).iconScale(0.9)
            .title("回到过去？")
            .subtitle("似曾相识！")
            .desc(d -> d
                    .line("{远古世界}中会生成一些有别于{主世界}的矿脉：", DARK_RED, BLUE)
                    .line()
                    .line("{蓝黄玉矿脉}(这是你前期{氟}的主要来源，其中的辉铜矿是铂处理产线的原材料之一)", AQUA, AQUA)
                    .line()
                    .line("{硝石矿脉}(一个{钾}和{氮}的优秀来源)", DARK_GREEN, DARK_AQUA, DARK_PURPLE)
                    .line()
                    .line("{赛特斯石英/石英矿脉}(石英的主要来源，以及{AE}的核心基础材料)", LIGHT_PURPLE, DARK_RED)
                    .line()
                    .line("{黝铜矿脉}(黝铜矿是铂处理的另一种原材料，而其中的{辉锑矿}是产出{锑}的核心来源)", RED, GOLD, GOLD)
                    .line()
                    .line("{硫矿脉}(大量获取{硫}的途径，其中的{闪锌矿}离心处理可以获得{镓粉}，这是前期{唯一}的获取途径)", YELLOW, YELLOW, BLUE, BLUE, DARK_RED)
                    .line()
                    .line("以及有一些特殊的配方需要在远古世界中进行，比如{大马士革钢}的相关配方", GREEN))
            .task(Task.dimension(Dimension.ANCIENT_WORLD).title("远古世界"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(ANCIENT_REACTOR)
            .hideDependentLines());

    public static final Quest CAN_IT_BE_USED_FOR_BEAUTY = CHAPTER.quest("can_it_be_used_for_beauty", q -> q
            .at(9, 8.5)
            .title("它能拿来美容吗")
            .subtitle("不行，一股臭鸡蛋味儿")
            .desc(d -> d
                    .line("硫粉，主世界并不直接产出，但经常作为各种矿物处理中的副产物产出")
                    .line()
                    .line("不过上述的处理大多数时候都是需要{更高的电压}才能完成", DARK_GREEN)
                    .line()
                    .line("而更直接的来源是去远古世界挖掘{硫矿脉}", DARK_PURPLE)
                    .line()
                    .line("你需要准备多点，因为你需要{很多的}{橡胶}", DARK_RED, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Sulfur))
            .dependsOn(BACK_TO_THE_PAST));

    public static final Quest MIXER_STEAM_POWERED = CHAPTER.quest("mixer_steam_powered", q -> q
            .at(11.25, 3)
            .title("搅拌机，蒸汽驱动版")
            .subtitle("它会帮我混合火药吗")
            .desc(d -> d
                    .line("{搅拌机}会帮你把多种粉进行{混合}，常见于{制造合金}等", GOLD, GREEN, AQUA)
                    .line()
                    .line("当然配方可能会串，遇到多种相似原料的一定要{谨慎输入}", LIGHT_PURPLE)
                    .line()
                    .line("蒸汽需求量、并行数和debuff都跟其他的{蒸汽多方块}相似，这里就不加赘述了", GOLD))
            .task(Task.item(MultiBlockA.STEAM_MIXER))
            .reward(Reward.item(AEItems.ENDER_DUST, 32))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest ORE_SMASHER = CHAPTER.quest("ore_smasher", q -> q
            .at(11.25, 4.5)
            .title("原矿粉碎者")
            .subtitle("矿处，启动！")
            .desc(d -> d
                    .line("你终于拥有了矿处的基本能力！通过{矿石破碎机}进行处理的原矿石会增产{3倍}", GOLD, GREEN)
                    .line()
                    .line("如铁等前期矿石{允许你在熔炉里烧炼矿处过程中的}{任何形态}，但通常你只有完成矿处获得{金属粉}才可放入熔炉里烧炼成{锭}", YELLOW, YELLOW.bold(), GOLD, GOLD)
                    .line()
                    .line("处理某些矿(如粗红石)时很可能会因为产率过高而阻塞输出口，所以你可以选择设置{两个}输出口", GREEN)
                    .line()
                    .line("前往{资源获取与处理}章节看看矿处流程！", link(OreProcessingChapter.STEAM_CRUSHER)))
            .task(Task.item(MultiBlockC.STEAM_CRUSHER))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest MORPHING = CHAPTER.quest("morphing", q -> q
            .at(9, 5.15)
            .title("形态变化！")
            .subtitle("三维变二维？")
            .desc(d -> d
                    .line("希望你这时没有在使用工作台加锤子的组合来制作钢板了")
                    .line()
                    .line("你需要节省材料和时间"))
            .task(Task.item(ModItems.STEEL_PLATE.get()))
            .reward(Reward.item(ModItems.STEEL_PLATE.get(), 16))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest THE_TREASURED_PRODUCT_OF_THE_RUBBER = CHAPTER.quest("the_treasured_product_of_the_rubber", q -> q
            .at(11.25, 8.5)
            .title("橡胶树的宝贝产物")
            .subtitle("并不需要木龙头")
            .desc(d -> d
                    .line("希望你注意到了{橡胶树}并且把它们带回了家，如果没有注意到初章的任务你现在将尴尬地等待橡胶树生长", GOLD)
                    .line()
                    .line("{公司警告你禁止摸鱼}", DARK_GRAY.strike())
                    .line()
                    .line("应该尽快种起来，{种多点肯定是没错的}", YELLOW)
                    .line()
                    .line("然后用连锁功能把橡胶原木批量砍下来就行了，粘性树脂会随机掉落"))
            .task(Task.item(GTItems.STICKY_RESIN))
            .hideDependencyLines());

    public static final Quest ENGINE = CHAPTER.quest("engine", q -> q
            .at(0.5, 6)
            .subtitle("合成飞机")
            .desc(d -> d
                    .line("飞机核心部件，如果你还不会飞，可以用这个在{远古世界}探索", GOLD)
                    .line()
                    .line("前往{沉浸式飞行}章节查看飞机其他零部件", link(ImmersiveAircraftChapter.Q_1CE2E908))
                    .line()
                    .line("前往{提示}章节查看可以获得飞行的方法", link(TipsChapter.FLIGHT_MODE))
                    .line()
                    .line("{观赏性大于实用性}", GRAY.italic()))
            .task(Task.item(QuestExternalRefs.ImmersiveAircraftItems.ENGINE))
            .reward(Reward.item(Items.ENDER_PEARL, 32))
            .dependsOn(PIS_TON)
            .optional());

    public static final Quest THE_PREDECESSOR_OF_THE_MOLD = CHAPTER.quest("the_predecessor_of_the_mold", q -> q
            .at(9, 7)
            .title("模具的前身")
            .subtitle("但好事成三")
            .desc(d -> d
                    .line("你会需要三个，一个用于制作{模具(球)}，一个用于制作{模具(块)}，最后一个用于制作{模具(板)}", GOLD, GOLD, GOLD)
                    .line()
                    .line("目前模具只能用于{合金炉}，后面的阶段模具还可以用于{流体固化器}和{冲压机床}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("{模具}都是不会消耗的，意思就是你做一个出来那你就可以一直用到{宇}{宙}{终}{结}", GOLD, AQUA, YELLOW, DARK_GREEN, RED)
                    .line()
                    .line("而{模板}也可以制作{模头}，但使用{模头}的{压模器配方}起码都是LV级别起步的，绝大多数配方在LV是{无法使用的}", GOLD, GOLD, GOLD, RED, DARK_RED)
                    .line()
                    .line("{所以看清楚了，你要制作的是模具，不是模头！！！}", DARK_RED))
            .task(Task.item(GTItems.SHAPE_EMPTY, 3))
            .dependsOn(MORPHING));

    public static final Quest YOU_CAN_STICK_WIRES = CHAPTER.quest("you_can_stick_wires", q -> q
            .at(-3.5, 7.5)
            .title("你可以贴贴的导线")
            .subtitle("Another Ver. 0")
            .desc(d -> d
                    .line("无电阻的一类导线统称为{超导线}", GOLD)
                    .line()
                    .line("只要超导线接受的电流和电压{小于}它所能承载的{电流或电压上限}，它就是绝对安全的", YELLOW, LIGHT_PURPLE)
                    .line()
                    .line("{eg.这个任务指引你做的超导线承载力上限是1A/ULV，则在接受2A/ULV或1A/LV的电情况下导线会烧毁}", DARK_GRAY)
                    .line()
                    .line("{意味着你可以在导线上}{跳}{来}{跳}{去}", ITALIC, GREEN.italic(), GOLD, YELLOW, AQUA)
                    .line()
                    .line("当然如果你让超导线接收了过大的电流，它就会{起火}并且在一段时间后{烧毁}，如果电流远大于承载上限，则会{直接烧毁}", DARK_RED, DARK_RED, DARK_RED)
                    .line()
                    .line("{红色合金导线稍微有点特殊，它需要包一层橡胶成为线缆才能超导。其他的超导体制成导线直接就可以使用}", YELLOW))
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.RedAlloy))
            .dependsOn(SUPER_CON_DUCTOR));

    public static final Quest NEED_A_LOT_OF_STEEL = CHAPTER.quest("need_a_lot_of_steel", q -> q
            .at(13, 2)
            .title("需要大量钢？")
            .subtitle("32层举高高炉")
            .desc(d -> d
                    .line("虽然很酷，但这不是你现在能想的机器")
                    .line()
                    .line("准备{11组钢锭}再来看看吧", DARK_RED))
            .task(Task.item(MultiBlockA.LEAP_FORWARD_ONE_BLAST_FURNACE))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest COKE_OVEN_PRO_MAX_EDITION = CHAPTER.quest("coke_oven_pro_max_edition", q -> q
            .at(11.25, 1.5)
            .title("焦炉Pro Max版")
            .subtitle("大大大大大大！")
            .desc(d -> d
                    .line("你需要更多的焦煤和杂酚油吗？来，让这个大家伙满足你！")
                    .line()
                    .line("{大型焦炉}有{8并行}，而且最多可以安装{5个}{焦炉仓}来进行交互，绝对可以满足你的需求", GOLD, DARK_PURPLE, BLUE, GOLD)
                    .line()
                    .line("当然也别{盲目追求}{半流质发电}，该进行技术革新的时候就要果断进行", DARK_RED, GREEN)
                    .line()
                    .line("{要不然就会跟某个堆了32个大型焦炉的玩家一样苦恼于如何处理它们}", DARK_GRAY.strike()))
            .task(Task.item(MultiBlockG.LARGE_COKE_OVEN))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest LARGER_INSULATED_CUP = CHAPTER.quest("larger_insulated_cup", q -> q
            .at(11.25, 0)
            .title("更大的保温杯")
            .subtitle("装点热水？")
            .desc(d -> d
                    .line("你可以通过{钢制储罐阀门}来控制流体的输入和输出，如果你把阀门向下摆，则可以{自动}输出流体", GOLD, GREEN)
                    .line()
                    .line("里面可以塞很多东西，比如{蒸汽}和{杂酚油}等等", GOLD, GOLD)
                    .line()
                    .line("为什么存{蒸汽}？你得确保有些大胃袋机器能临时跑得起来", GOLD)
                    .line()
                    .line("至于{杂酚油}，前期发电的好东西", GOLD))
            .task(Task.item(GTMultiMachines.STEEL_MULTIBLOCK_TANK))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest MORE_POWERFUL_MACHINES = CHAPTER.quest("more_powerful_machines", q -> q
            .at(7.5, 4)
            .title("更猛的机器们")
            .subtitle("Work！Work！")
            .desc(d -> d
                    .line("有了钢锭，你现在可以制作{高压蒸汽机器}，它们的效率是低压版本的{2倍}，但同时每秒也消耗更多{蒸汽}", GREEN, RED, GOLD)
                    .line()
                    .line("而{高压锅炉}可以获得比{普通锅炉}更高的产出，从2倍到3倍不止，建议查看EMI以及个人基地的{实际情况}进行考量", GOLD, GOLD, DARK_BLUE))
            .task(
                    Task.anyOf(1, GTMachines.STEAM_SOLID_BOILER.second(), GTOMachines.STEAM_VACUUM_PUMP.second(), GTMachines.STEAM_LIQUID_BOILER.second(), GTMachines.STEAM_SOLAR_BOILER.second(), GTMachines.STEAM_EXTRACTOR.second(), GTMachines.STEAM_MACERATOR.second(), GTMachines.STEAM_COMPRESSOR.second(), GTMachines.STEAM_HAMMER.second(), GTMachines.STEAM_FURNACE.second(), GTMachines.STEAM_ALLOY_SMELTER.second(), GTMachines.STEAM_ROCK_CRUSHER.second()).title("高压蒸汽机器"))
            .dependsOn(FIRST_STEEL_INGOT));

    public static final Quest PLATE_CASTING_MOLD = CHAPTER.quest("plate_casting_mold", q -> q
            .at(6.5, 7)
            .task(Task.item(GTItems.SHAPE_MOLD_PLATE))
            .dependsOn(THE_PREDECESSOR_OF_THE_MOLD));

    public static final Quest BLOCK_CASTING_MOLD = CHAPTER.quest("block_casting_mold", q -> q
            .at(13, 7)
            .task(Task.item(GTItems.SHAPE_MOLD_BLOCK))
            .dependsOn(THE_PREDECESSOR_OF_THE_MOLD));

    public static final Quest BALL_CASTING_MOLD = CHAPTER.quest("ball_casting_mold", q -> q
            .at(13, 5.5)
            .task(Task.item(GTItems.SHAPE_MOLD_BALL))
            .dependsOn(THE_PREDECESSOR_OF_THE_MOLD));

    public static final Quest VAULTS = CHAPTER.quest("vaults", q -> q
            .at(11.25, -1.5)
            .icon(MultiBlockG.ITEM_VAULT)
            .title("保险库们")
            .subtitle("箱子装不下了")
            .desc(d -> d
                    .line("GTO专门为游戏的前期设计了用于存储物品的{保险库}", GOLD)
                    .line()
                    .line("这是你拥有AE存储前能够使用的最大的存储容器了")
                    .line()
                    .line("{容量}由{密封机械方块数量和等级}共同决定，保险库内部要填满密封机械方块才会成型", AQUA, YELLOW)
                    .line()
                    .line("建议配合{简易合成终端}使用", GOLD))
            .task(
                    Task.item(MultiBlockG.ITEM_VAULT),
                    Task.item(MultiBlockG.FLUID_VAULT).optional(),
                    Task.item(MultiBlockG.GENERAL_VAULT).optional(),
                    Task.item(GTOMachines.VAULT_HATCH).optional())
            .reward(Reward.item(GTOBlocks.HERMETIC_CASING_ULV))
            .dependsOn(MainStoneageChapter.SIMPLE_CRAFTING_TERMINAL, FIRST_STEEL_INGOT));

    public static final Quest OTHER_COMMON_MOLDS = CHAPTER.quest("other_common_molds", q -> q
            .at(7.5, 8.5)
            .title("其他常用模具")
            .subtitle("你可能会用到它们")
            .task(Task.item(GTItems.SHAPE_MOLD_NUGGET), Task.item(GTItems.SHAPE_MOLD_INGOT))
            .dependsOn(THE_PREDECESSOR_OF_THE_MOLD)
            .optional());

    public static final Quest LP_STEAM_MINER = CHAPTER.quest("lp_steam_miner", q -> q
            .at(-11, 4.5)
            .subtitle("并不能解放双手")
            .desc(d -> d
                    .line("可以帮你自动挖矿，但是效率实在是太低了，还没你手挖的快")
                    .line()
                    .line("你可以选择不完成这个任务，毕竟它是可选的"))
            .task(Task.item(GTMachines.STEAM_MINER.first()), Task.item(GTMachines.STEAM_MINER.second()))
            .dependsOn(MISTY_MOVEMENT)
            .optional());

    public static final Quest STEEL_SUBSIDY = CHAPTER.quest("steel_subsidy", q -> q
            .at(7.5, 5.15)
            .icon(ModItems.STEEL_INGOT.get())
            .title("钢补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(ModItems.STEEL_INGOT.get(), 640L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(ModItems.STEEL_INGOT.get(), 192L))
            .dependsOn(MORPHING)
            .optional().hideUntilDepsVisible());

    public static final Quest PREPARE_GLASS_EASY = CHAPTER.quest("prepare_glass_easy", q -> q
            .at(17, 8.5)
            .icon(Items.GLASS)
            .title("制备玻璃{（简单）}", GREEN)
            .desc(d -> d
                    .line("简单保留了原版玻璃的合成配方，因此你无需通过石英砂制作玻璃。直接完成此任务可跳过制备玻璃第一步")
                    .line()
                    .line("玻璃粉可以通过锤子敲玻璃获得"))
            .task(Task.item(Items.GLASS), Task.checkmark())
            .dependsOn(UtilChapter.Q_056EC4E6)
            .optional().hideUntilDepsVisible());

    public static final Quest PREPARING_GLASS_STEP_TWO = CHAPTER.quest("preparing_glass_step_two", q -> q
            .at(15.000000000000007, 8.5)
            .title("制备玻璃......第二步！")
            .subtitle("手动拼接？")
            .desc(d -> d
                    .line("对的，你最开始的{玻璃粉}就是需要{工作台}手动组装", GOLD, GREEN)
                    .line()
                    .line("等到你有条件使用{搅拌机}了，也就可以省点事了，但你距离能使用{蒸汽搅拌机}还有一定的距离，所以短时间内你还是得在{工作台}上手动组装{玻璃粉}", GOLD, GOLD, GREEN, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Glass))
            .dependsOn(PREPARE_GLASS_FIRST_STEP, PREPARE_GLASS_EASY)
            .requireAny());

    public static final Quest PREPARING_GLASS_STEP_THREE = CHAPTER.quest("preparing_glass_step_three", q -> q
            .at(13, 8.5)
            .title("制备玻璃......第三步！")
            .subtitle("熔炼成块！")
            .desc(d -> d
                    .line("用{合金炉}和{模具(块)}来塑形玻璃，这是最后一步", GOLD, GOLD)
                    .line()
                    .line("累了要休息下吗？不妨听听后续的改进建议：")
                    .line()
                    .line("当然你要想让{玻璃制造}变得更简单，还有办法，让{玻璃粉}由{搅拌机}生产是其中之一；而用提取机提取{石英岩}得到的{液态玻璃}直接在{流体固化器}内用{模具(块)}则可以更加简便地生产玻璃", GOLD, GREEN, GOLD, LIGHT_PURPLE, GREEN, GOLD, GOLD)
                    .line()
                    .line("另还有将{液态玻璃}与{沙子}在{电弧炉}内反应的办法；以及使用{冲压机床}生产，节省1/3的时间", LIGHT_PURPLE, YELLOW, GOLD, GOLD)
                    .line()
                    .line("至于后续如何选择，取决于你"))
            .task(Task.item(Items.GLASS))
            .reward(Reward.item(Items.GLASS, 16))
            .dependsOn(PREPARING_GLASS_STEP_TWO, BLOCK_CASTING_MOLD));

    public static final Quest EXTRACTOR_NOT_A_JUICER = CHAPTER.quest("extractor_not_a_juicer", q -> q
            .at(13, 10.5)
            .title("提取机，不是榨汁机！")
            .subtitle("但其实也没什么区别不是吗")
            .desc(d -> d
                    .line("当前阶段你需要这个家伙来帮助你完成{橡胶}的制作", GOLD)
                    .line()
                    .line("我建议你留着{橡胶原木}，因为一来它的产出并不多，二来它能在{离心机}里产出{胶水}", LIGHT_PURPLE, GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_EXTRACTOR.first()))
            .dependsOn(PREPARING_GLASS_STEP_THREE));

    public static final Quest UNRIPE_RUBBER = CHAPTER.quest("unripe_rubber", q -> q
            .at(11.25, 10.5)
            .title("青涩的橡胶")
            .subtitle("提取的产物")
            .desc(d -> d
                    .line("你在查EMI吗？我来直接告诉你吧：你现在只能用{蒸汽提取机}来提取生橡胶末", GOLD)
                    .line()
                    .line("如果你提取的是粘性树脂，那你一次可以提取{三个}生橡胶末；如果是橡胶原木则一次性只能产出{一个}", GREEN, GREEN)
                    .line()
                    .line("记得整个容器来自动容纳产物，提取的流程很漫长而且容易{堵塞产物}", RED)
                    .line()
                    .line("当然你有{蒸汽分离机}了就可以用其他办法来制造生橡胶末了", GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.RawRubber))
            .dependsOn(EXTRACTOR_NOT_A_JUICER, THE_TREASURED_PRODUCT_OF_THE_RUBBER));

    public static final Quest RUBBER_FINALLY = CHAPTER.quest("rubber_finally", q -> q
            .at(9, 10.5)
            .title("橡胶，终于")
            .subtitle("千辛万苦")
            .desc(d -> d
                    .line("将你得到的{生橡胶末}和{硫粉}在{合金炉}内反应即可生成{橡胶条}", DARK_AQUA, YELLOW, GOLD, GOLD)
                    .line()
                    .line("当然这种方式的速度是比较慢的，等你到了LV阶段的时候你就可以使用{化学反应釜}来制{液态橡胶}，并且通过{流体固化器}来固化成你想要的任何形状的{橡胶}", GOLD, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("但距离LV还是有一定距离的，你还是需要先提升一下你当前阶段的生产力"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Rubber))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Rubber, 32), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32))
            .dependsOn(UNRIPE_RUBBER, CAN_IT_BE_USED_FOR_BEAUTY)
            .hideDependencyLines(false));

    public static final Quest BLOW_A_GLASS_BUBBLE = CHAPTER.quest("blow_a_glass_bubble", q -> q
            .at(15, 5.5)
            .title("吹个玻璃泡泡")
            .subtitle("让合金炉来！")
            .desc(d -> d
                    .line("使用{合金炉}和特定形状的{模具}来塑形玻璃，此类配方你将来会遇到很多", GOLD, GOLD)
                    .line()
                    .line("当然合金炉只是目前的处理办法，你后面用{流体固化机}或者{压模器}都是一个{更好的}选择", GOLD, GOLD, GREEN))
            .task(Task.item(GTItems.GLASS_TUBE))
            .dependsOn(PREPARING_GLASS_STEP_TWO, BALL_CASTING_MOLD));

    public static final Quest NOT_AN_ELECTRON_TUBE = CHAPTER.quest("not_an_electron_tube", q -> q
            .at(15, 3.5)
            .title("并非电子管")
            .subtitle("工作台要搓冒烟了！")
            .desc(d -> d
                    .line("在你制作出第一个电路之前，你需要做一个它的前身，那就是{粗真空管}", GOLD)
                    .line()
                    .line("这东西{只有}工作台的配方，所以我希望你准备好了足够多的材料，然后在工作台上大闹一番吧", YELLOW))
            .task(Task.item(GTOItems.RAW_VACUUM_TUBE))
            .dependsOn(BLOW_A_GLASS_BUBBLE));

    public static final Quest CENTRIFUGE = CHAPTER.quest("centrifuge", q -> q
            .at(9, 13)
            .title("离心吗？")
            .subtitle("FFF机器")
            .desc(d -> d
                    .line("哦？你准备用这个大家伙了吗？")
                    .line()
                    .line("你需要拿它来离心各种你想要的东西，还是{矿物处理}的一环", YELLOW)
                    .line()
                    .line("你马上就要用到它离心{黏性树脂}，{空气}和{水煤气}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{蒸汽流体输入仓}只能支持{16B}的流体输入，你可以制作{无限进气仓}来利用满机器的并行去离心空气", GOLD, AQUA, GOLD)
                    .line()
                    .line("{如果遇到执行不了的配方记得看看EMI，看看你的输入输出仓是不是比配方要求和产出的量小，或者是不是装少了仓室}", GRAY.italic()))
            .task(Task.item(MultiBlockA.STEAM_SEPARATOR))
            .dependsOn(RUBBER_FINALLY)
            .hideDependentLines());

    public static final Quest LARGE_POOL = CHAPTER.quest("large_pool", q -> q
            .at(7.5, 12.5)
            .title("大泳池")
            .subtitle("并不会淹死人")
            .desc(d -> d
                    .line("它有两个用处：")
                    .line()
                    .line("一是用特殊的液体处理特殊的矿物中间产物得到一些中间产物，比如{金红石粉和}{铂粉}等", LIGHT_PURPLE, YELLOW)
                    .line()
                    .line("二是处理一些{化学浸洗}的配方", AQUA)
                    .line()
                    .line("后续的使用量会很大，所以你肯定会需要这个{8并行}的版本的", DARK_RED))
            .task(Task.item(MultiBlockA.STEAM_BATH))
            .dependsOn(RUBBER_FINALLY));

    public static final Quest DRUM_WASHING_MACHINE_MINERAL_SPECIFIC = CHAPTER.quest("drum_washing_machine_mineral_specific", q -> q
            .at(10.5, 12.5)
            .title("滚筒洗衣机，矿物专用")
            .subtitle("然而并不能完全洗干净")
            .desc(d -> d
                    .line("注意看EMI的配方，不同的配方需要{不同的电路}，对应需要的液体也会{有所不同}，但需要在洗矿机内执行的含液体配方无非就是{水}和{蒸馏水}二选一", DARK_RED, RED, DARK_BLUE, AQUA)
                    .line()
                    .line("{8并行}的版本足够你应付目前的部分处理配方的，只要你不手欠处理了{红石}和{青金石}等配方...", DARK_RED, DARK_RED, AQUA))
            .task(Task.item(MultiBlockA.STEAM_ORE_WASHER))
            .dependsOn(RUBBER_FINALLY));

    public static final Quest SHEET_LIKE_RUBBER_IS_GOOD_RUBBER = CHAPTER.quest("sheet_like_rubber_is_good_rubber", q -> q
            .at(6.5, 10.5)
            .title("片状的橡胶才是好橡胶")
            .subtitle("我需要多一台合金炉！")
            .desc(d -> d
                    .line("用{合金炉}和{板模具}将{橡胶条}塑形为{橡胶片}", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("很明显的这是{合金炉}的配方，你肯定需要多一台{合金炉}", GOLD, GOLD)
                    .line()
                    .line("或者一个{多方块合金炉}的", link(A_MESSY_STEW)))
            .task(Task.item(TagPrefix.plate, GTMaterials.Rubber))
            .dependsOn(RUBBER_FINALLY, PLATE_CASTING_MOLD));

    public static final Quest ULV_SUPERCONDUCTOR = CHAPTER.quest("ulv_superconductor", q -> q
            .at(-3.5, 10.5)
            .title("ULV超导")
            .subtitle("线靠衣装")
            .desc(d -> d
                    .line("唯二你能手动合成的线缆之一，在你做出{原始打包机}之后就可以不用手动合成了", GOLD)
                    .line()
                    .line("当然只能合成1x的线缆，即该线缆只能支持{1A}的电流上限，{电流再大就要烧导线了！！}", DARK_RED, DARK_RED)
                    .line()
                    .line("不过合成导线的方式{不止一条}，多看看EMI吧！", YELLOW))
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.RedAlloy))
            .reward(Reward.item(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 16))
            .dependsOn(SHEET_LIKE_RUBBER_IS_GOOD_RUBBER, YOU_CAN_STICK_WIRES));

    public static final Quest RUBBER_SUBSIDY = CHAPTER.quest("rubber_subsidy", q -> q
            .at(5.5, 11.5)
            .icon(TagPrefix.plate, GTMaterials.Rubber)
            .title("橡胶补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Rubber, 128L))
            .dependsOn(SHEET_LIKE_RUBBER_IS_GOOD_RUBBER)
            .optional().hideUntilDepsVisible());

    public static final Quest GT_PIPES = CHAPTER.quest("gt_pipes", q -> q
            .at(-11, 6.5)
            .icon(TagPrefix.pipeNormalFluid, GTMaterials.Bronze)
            .title("GT管道")
            .desc(d -> d
                    .line("GTO中的GT管道经过了魔改：")
                    .line()
                    .line("- 优化tps，流体管道没有缓存，流体从输入直达输出")
                    .line()
                    .line("- 能够主动抽取机器/容器中的物品/流体，用扳手控制管道方向的同时也将激活管道的主动抽取功能"))
            .task(Task.checkmark())
            .reward(Reward.item(TagPrefix.pipeNormalFluid, GTMaterials.Bronze, 8))
            .dependsOn(MISTY_MOVEMENT));

    public static final Quest WROUGHT_IRON_INGOT_SUBSIDY = CHAPTER.quest("wrought_iron_ingot_subsidy", q -> q
            .at(-1, 14)
            .icon(TagPrefix.ingot, GTMaterials.WroughtIron)
            .title("锻铁锭补助")
            .subtitle("下一个阶段的重要材料")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.ingot, GTMaterials.WroughtIron, 192L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(UlvChapter.WELCOME_TO_THE_ULTRA_LOW_PRESSURE)
            .optional().hideUntilDepsVisible());

    public static final Quest ENERGISTICALLY_APPLIED_START_EASY = CHAPTER.quest("energistically_applied_start_easy", q -> q
            .at(6.75, -2)
            .icon(AEBlocks.ENERGY_ACCEPTOR)
            .title("应用能源起步{（简单）}", GREEN)
            .subtitle("没有频道的AE真好")
            .desc(d -> d
                    .line("你可以开始制作{应用能源(AE2)}的敲门砖——{能源接收器}了！", link(AeChapter.GETTING_STARTED_WITH_AE), GOLD)
                    .line()
                    .line("{简单模式的AE配方没有魔改，因此请忽略在LV，MV和HV章节的AE相关任务}", YELLOW.bold())
                    .line()
                    .line("{AE机器和ME网络都需要AE能量才能工作，使用}{能源接收器}将GT的电能转化为AE能量", YELLOW.bold(), GOLD.bold())
                    .line()
                    .line("GTO的AE默认{无限频道}，无需{ME控制器}，只需要确保为当前网络提供足够的能量即可", GREEN, GOLD)
                    .line()
                    .line("{如果你还不会AE，请尽快寻找教程快速入门。本包无需精通AE，但至少需要了解如何下单。}", GRAY.italic()))
            .task(Task.item(AEBlocks.ENERGY_ACCEPTOR).optional(), Task.checkmark())
            .reward(Reward.item(AEItems.NETWORK_TOOL).personal())
            .dependsOn(BACK_TO_THE_PAST, UtilChapter.Q_056EC4E6)
            .optional().hideUntilDepsVisible());

    public static final Quest MULTIBLOCK_STEAM_MACHINE_HATCH_GUIDE = CHAPTER.quest("multiblock_steam_machine_hatch_guide", q -> q
            .at(-11, 2.5)
            .icon(GTMachines.STEAM_IMPORT_BUS)
            .title("多方块蒸汽机器仓室指南")
            .desc(d -> d
                    .line("当你准备尝试使用{蒸汽多方块机器}的时候，别忘了放置对应的仓室！", AQUA)
                    .line()
                    .line("- {蒸汽流体输入/输出仓}：用于输入和输出流体", GOLD)
                    .line("- {蒸汽输入/输出总线}：用于输入和输出物品", GOLD)
                    .line()
                    .line("- {蒸汽输入仓}：将蒸汽作为能源输入机器", GOLD)
                    .line("- {蒸汽排气仓}：排放机器的废气， 仓室前方不能放置方块", GOLD))
            .task(
                    Task.item(GTOMachines.STEAM_FLUID_INPUT_HATCH),
                    Task.item(GTOMachines.STEAM_FLUID_OUTPUT_HATCH),
                    Task.item(GTMachines.STEAM_IMPORT_BUS),
                    Task.item(GTMachines.STEAM_EXPORT_BUS),
                    Task.item(GTMachines.STEAM_HATCH),
                    Task.item(GTOMachines.STEAM_VENT_HATCH))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(MISTY_MOVEMENT));

    public static final Quest ITEM_PIPE = CHAPTER.quest("item_pipe", q -> q
            .at(-7.5, 9.75)
            .subtitle("另一种管道")
            .desc(d -> d.line("你也可以使用来自pipez的管道来物流"))
            .task(Task.item(QuestExternalRefs.PipezItems.ITEM_PIPE))
            .reward(Reward.item(QuestExternalRefs.PipezItems.ITEM_PIPE, 12))
            .dependsOn(MISTY_MOVEMENT)
            .hideDependencyLines());

    public static final Quest FLUID_PIPE = CHAPTER.quest("fluid_pipe", q -> q
            .at(-10.5, 9.75)
            .subtitle("另一种管道")
            .desc(d -> d.line("你也可以使用来自pipez的管道来物流"))
            .task(Task.item(QuestExternalRefs.PipezItems.FLUID_PIPE))
            .reward(Reward.item(QuestExternalRefs.PipezItems.FLUID_PIPE, 12))
            .dependsOn(MISTY_MOVEMENT)
            .hideDependencyLines());

    public static final Quest STEAM_POWERED = CHAPTER.quest("steam_powered", q -> q
            .at(-9, 11.25)
            .icon(GTMaterials.Steam.getBucket())
            .title("蒸汽驱动")
            .desc(d -> d
                    .line("在游戏中，默认情况下2mb蒸汽=1EU，你的蒸汽机器消耗蒸汽以及蒸汽发电都会遵循这个规则")
                    .line()
                    .line("低压机器工作所有配方都需要2x耗时，不过每秒消耗的蒸汽会减少50%")
                    .line()
                    .line("高压机器回归正常的耗时和耗能，因此低压和高压运行配方需要的蒸汽总量是不变的")
                    .line()
                    .line("多方块机器在tooltips中标注的额外耗时同时会让你运行配方需要的蒸汽总量也变多")
                    .line()
                    .line("专家模式下低压机器还有额外50%的耗能惩罚"))
            .task(Task.checkmark())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MISTY_MOVEMENT)
            .hideDependencyLines());

    public static final Quest EXTRACTION_PIPE = CHAPTER.quest("extraction_pipe", q -> q
            .at(15, -0.5)
            .title("抽取管道")
            .desc(d -> d
                    .line("为了让泵能够工作，你需要放入一个{抽取管道}，等待其多次抽取真空直到真空等级大于0", YELLOW)
                    .line()
                    .line("低压和高压蒸汽泵使用{巨型青铜管道}并输入蒸汽", GOLD)
                    .line()
                    .line("用电的真空泵需要放置对应电压的{流体校准器}", GOLD))
            .task(Task.item(TagPrefix.pipeHugeFluid, GTMaterials.Bronze))
            .dependsOn(A_LITTLE_DEVICE_THAT_CAN_CREATE));

    public static final Quest RAW_CIRCUIT_BOARD = CHAPTER.quest("raw_circuit_board", q -> q
            .at(15, 1.5)
            .title("原始电路板？")
            .subtitle("嘬嘬嘬")
            .desc(d -> d
                    .line("棒极了，这次不用史蒂夫的嘴")
                    .line()
                    .line("手持{粗真空管}潜行右击{真空泵}制造{真空管}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{做出原始组装机后，你就可以直接在组装机里制造真空管了}", GRAY.italic()))
            .task(Task.item(GTItems.VACUUM_TUBE))
            .reward(Reward.item(GTItems.VACUUM_TUBE, 4))
            .dependsOn(NOT_AN_ELECTRON_TUBE, EXTRACTION_PIPE));

    public static final Quest VACUUM_TUBE_SUBSIDY = CHAPTER.quest("vacuum_tube_subsidy", q -> q
            .at(17, 1.5)
            .icon(GTItems.VACUUM_TUBE)
            .title("真空管补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.VACUUM_TUBE, 64))
            .normalSubsidy(Reward.item(GTItems.VACUUM_TUBE, 12))
            .dependsOn(RAW_CIRCUIT_BOARD)
            .optional().hideUntilDepsVisible());

    public static final Quest ANY_PLUNGERS = CHAPTER.quest("any_plungers", q -> q
            .at(5.5, 9.5)
            .desc(d -> d.line("清空容器或者机器中的流体，{不会返还}", RED))
            .task(Task.tag(CustomTags.PLUNGERS, 1).title("任意搋子"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(SHEET_LIKE_RUBBER_IS_GOOD_RUBBER)
            .optional());

    public static final Quest CAVE_DESTROYER = CHAPTER.quest("cave_destroyer", q -> q
            .at(-8.5, 2)
            .icon(Items.CLAY_BALL)
            .title("洞穴毁灭者")
            .subtitle("只不过是轻轻动了动黏土而已")
            .desc(d -> d
                    .line("好吧，你现在需要去搞点黏土了")
                    .line()
                    .line("你需要一些黏土去制作后面几个大家伙")
                    .line()
                    .line("那么黏土去哪找呢？去河流边上，或者直接点，繁茂洞穴，或者再直接点，{去买点}", link(ShopChapter.A_STACK_OF_CLAY)))
            .task(Task.item(Items.CLAY_BALL, 64), Task.tag(Tags.Items.SAND, 1).title("沙子").icon(Items.SAND))
            .reward(Reward.item(Items.CLAY, 64), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MainStoneageChapter.A_FULL_ARSENAL)
            .hideDependencyLines());

    public static final Quest BRICK_WOODEN_FORM = CHAPTER.quest("brick_wooden_form", q -> q
            .at(-8.5, 0)
            .subtitle("可重复")
            .desc(d -> d.line("这东西在接下来的合成中是可以重复利用的，所以造一个就好了"))
            .task(Task.item(GTItems.WOODEN_FORM_BRICK))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MainStoneageChapter.A_FULL_ARSENAL)
            .optional());

    public static final Quest COMPRESSED_COKE_CLAY = CHAPTER.quest("compressed_coke_clay", q -> q
            .at(-6.5, 0)
            .subtitle("然后我们需要把这堆东西挤进来")
            .desc(d -> d
                    .line("你需要116x{焦炉砖}以搭建一个最简可用焦炉", GOLD)
                    .line()
                    .line("所以你需要搓116个压缩{焦黏土}", GOLD)
                    .line()
                    .line("或者选择合金炉配方")
                    .line()
                    .line("对的，专家模式下会更难"))
            .task(Task.item(GTItems.COMPRESSED_COKE_CLAY))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(BRICK_WOODEN_FORM)
            .optional());

    public static final Quest COKE_OVEN_BRICK = CHAPTER.quest("coke_oven_brick", q -> q
            .at(-6.5, 2)
            .subtitle("需要点火")
            .desc(d -> d.line("你有两个选择制作焦炉砖块，使用耗材较多，无需等待的手搓配方；或是耗材较少，但是需要等待的合金炉配方"))
            .task(Task.item(GTItems.COKE_OVEN_BRICK).consume(false))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(COMPRESSED_COKE_CLAY, FUSION_POWER, CAVE_DESTROYER)
            .requireAny()
            .requireCount(2));

    public static final Quest COKE_OVEN = CHAPTER.quest("coke_oven", q -> q
            .at(-3.5, 2)
            .icon(GTMultiMachines.COKE_OVEN)
            .title("焦炉")
            .subtitle("你的第一个多方块结构")
            .desc(d -> d
                    .line("焦炉是个3x3x3的中空多方块结构，记得中间一定要留空")
                    .line()
                    .line("预留好输出的{焦炉仓}，要不然你还得来回舀", GOLD)
                    .line()
                    .line("焦炉产出的焦煤是正常煤炭燃烧时间的两倍，同时产出的杂酚油用来制作蒸汽时代的设备")
                    .line()
                    .line("还在傻傻手搭多方块吗？我建议你用{终端}来辅助搭建", link(MainStoneageChapter.MULTIBLOCK_HELPER))
                    .line()
                    .line("{一定要多堆点焦炉，要炼钢一台可不够。}", DARK_GRAY.italic()))
            .task(Task.item(GTMultiMachines.COKE_OVEN), Task.item(GTBlocks.CASING_COKE_BRICKS, 25))
            .reward(Reward.item(GTMachines.WOODEN_DRUM, 2).personal(), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(COKE_OVEN_BRICK));

    public static final Quest LOOKING_AT_A_PUMP_TO_QUENCH = CHAPTER.quest("looking_at_a_pump_to_quench", q -> q
            .at(-1, 2)
            .icon(GTMultiMachines.PRIMITIVE_PUMP)
            .title("望泵止渴")
            .subtitle("所处皆为井")
            .desc(d -> d
                    .line("手动运水——终于受够了，我只需要一点杂酚油来造一台{原始水泵}", GOLD)
                    .line()
                    .line("{它不需要放在水上}，不过{下雨}的时候会给予{1.5倍}的产出", GRAY, YELLOW, AQUA)
                    .line()
                    .line("{右击}或{[U]}键主方块查看多方块结构", YELLOW.italic(), YELLOW)
                    .line()
                    .line("用{任意流体管道}来运输流体，但是注意{木制管道}不能运输{蒸汽}", GOLD, GOLD, GOLD)
                    .page()
                    .line("将原始水泵放置在不同的群系内会获得不同的产出速率，这里提供部分数据：")
                    .line()
                    .line("海洋/河流群系：1000mb/s")
                    .line("沼泽：800mb/s")
                    .line("平原/森林：250mb/s"))
            .task(
                    Task.item(GTMultiMachines.PRIMITIVE_PUMP),
                    Task.item(GTMachines.PUMP_HATCH),
                    Task.item(GTBlocks.CASING_PUMP_DECK, 10),
                    Task.item(TagPrefix.frameGt, GTMaterials.TreatedWood, 10))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(COKE_OVEN));

    public static final Quest SEMI_AUTOMATION = CHAPTER.quest("semi_automation", q -> q
            .at(-1, -0.5)
            .icon(GTOItems.ULV_CONVEYOR_MODULE)
            .title("半自动化")
            .desc(d -> d
                    .line("一块焦煤能支持一个低压锅炉或一个加热器{1280秒}，意味着手动填燃料不失为中策", GOLD)
                    .line()
                    .line("在你找到大量燃料供应前，这足够你渡过炼钢前的时期")
                    .line()
                    .line("当你打算寻找其他产出蒸汽手段的时候，不妨看看右边"))
            .task(Task.checkmark().title("自动化"))
            .reward(Reward.item(TagPrefix.gem, GTMaterials.Coke, 16), Reward.item(Items.HOPPER))
            .dependsOn(LOOKING_AT_A_PUMP_TO_QUENCH));

    public static final Quest Q_7BCD4971 = CHAPTER.quest("q_7bcd4971", q -> q
            .at(1, -0.5)
            .icon(GTBlocks.HIGH_PRESSURE_HAZARD_SIGN_BLOCK)
            .subtitle("必要的决策")
            .desc(d -> d
                    .line("手动填充燃料终究还是太麻烦了，你希望找到一个自动化产生蒸汽的办法")
                    .line()
                    .line("蒸汽自动化的关键是提供稳定的燃料来源，以及注意供应足够多的水")
                    .line()
                    .line("{温馨提示：}基本所有的锅炉{在温度达到水的沸点且干烧}的情况下通入{水}都会{爆炸}", GOLD, YELLOW, AQUA, DARK_RED))
            .task(Task.checkmark().title("蒸汽自动化"))
            .reward(Reward.item(GTMaterials.Steam.getBucket()))
            .dependsOn(SEMI_AUTOMATION));

    public static final Quest ALTERNATIVE_PATH = CHAPTER.quest("alternative_path", q -> q
            .at(1, -2)
            .title("另辟蹊径")
            .subtitle("更高效也更危险的产蒸汽手段")
            .desc(d -> d
                    .line("{锅炉}与{加热器}相邻放置，为{加热器}提供燃料，再将水输入{锅炉}，当温度达到{水的沸点}时即可产出蒸汽", GOLD, GOLD, GOLD, GOLD, YELLOW)
                    .line()
                    .line("锅炉可同时受到多个相邻方块加热，可以让温度上升得更快")
                    .line()
                    .line("{生产蒸汽的时候会消耗温度}", YELLOW)
                    .line()
                    .line("当满足任一以下条件，机器会发生{爆炸}：", DARK_RED)
                    .line("1.{加热器}的温度达到{800K}", GOLD, DARK_GREEN)
                    .line("2.{锅炉}的温度达到{600K}", GOLD, DARK_GREEN)
                    .line("3.{锅炉}内蒸汽满了{没有及时排出}", GOLD, YELLOW)
                    .line()
                    .line("在加热器正面放置方块后加热器会暂停工作")
                    .line()
                    .line("{产能远超同时期机器，但是如何提供燃料以及如何控制启停是个问题}", GRAY.italic()))
            .task(Task.item(GTOMachines.HEATER), Task.item(GTOMachines.BOILER))
            .dependsOn(Q_7BCD4971, UlvChapter.BRAND_NEW_ENERGY_SYSTEM_HEAT)
            .optional());

    public static final Quest SOLAR_BOILER = CHAPTER.quest("solar_boiler", q -> q
            .at(1, 1)
            .title("太阳能锅炉")
            .subtitle("不会钙化")
            .desc(d -> d
                    .line("利用太阳的能量获得免费的{蒸汽}！", GRAY)
                    .line()
                    .line("{玻璃}在拥有一些{钢}后才能获取，野外地牢的{战利品箱}中偶尔能开出一些，如果你足够幸运地发现了{8个钢锭}（或是6个，如果你已经拥有一台{蒸汽锻造锤}），配合{蒸汽合金炉}就能很快获得{免费蒸汽}，如果你欧气爆棚摸到了更多钢锭，那么你甚至可以直接跳过普通版本的{太阳能锅炉}造台高压的！", GOLD, GOLD, YELLOW, GOLD, GOLD, GOLD, GRAY, GOLD)
                    .line()
                    .line("如果你的欧气不够也没关系，在能够生产{钢}之后你也可以直接制作{高压版本}的{太阳能锅炉}", GOLD, YELLOW, GOLD)
                    .line()
                    .line("{银}在这个世界并不稀缺，你是否注意到了地上的那些{亮晶晶的银色岩石碎屑}？", AQUA, LIGHT_PURPLE))
            .task(Task.item(GTMachines.STEAM_SOLAR_BOILER.first()))
            .dependsOn(Q_7BCD4971)
            .optional());

    public static final Quest HP_STEAM_SOLAR_BOILER = CHAPTER.quest("hp_steam_solar_boiler", q -> q
            .at(2.5, 1)
            .desc(d -> d.line("三倍蒸汽产能，如果有钢，几乎没有理由用普通版本！"))
            .task(Task.item(GTMachines.STEAM_SOLAR_BOILER.second()))
            .dependsOn(Q_7BCD4971)
            .optional());

    public static final Quest LP_STEAM_LIQUID_BOILER = CHAPTER.quest("lp_steam_liquid_boiler", q -> q
            .at(2.5, -0.5)
            .desc(d -> d
                    .line("{液体燃料锅炉}的产能是{固体燃料锅炉}的两倍，如果你的{杂酚油}多得没地方存，为何不将这些黏糊糊的{半流质}流体通通喂给{液体燃料锅炉}呢？", GOLD, YELLOW, GOLD, GOLD, GOLD)
                    .line()
                    .line("{……我刚刚说了}{半流质}{对吧}", YELLOW, link(UlvChapter.THIS_IS_AN_INTERNAL_COMBUSTION_ENGINE), YELLOW))
            .task(Task.item(GTMachines.STEAM_LIQUID_BOILER.first()))
            .dependsOn(Q_7BCD4971)
            .optional());

    public static final Quest HP_STEAM_LIQUID_BOILER = CHAPTER.quest("hp_steam_liquid_boiler", q -> q
            .at(4, -0.5)
            .subtitle("更疯狂的杂酚油吞噬者")
            .desc(d -> d.line("两倍蒸汽产能，如果有钢，几乎没有理由用普通版本！"))
            .task(Task.item(GTMachines.STEAM_LIQUID_BOILER.second()))
            .dependsOn(Q_7BCD4971)
            .optional());

    public static final Quest BRONZE_LARGE_BOILER = CHAPTER.quest("bronze_large_boiler", q -> q
            .at(4, -2)
            .desc(d -> d.line("它是ULV时代的产物，LV时代前最强大的蒸汽来源，如果你没有接受《清洁能源挑战》，那么不妨为你的{大型蒸汽机器们}制造一台，几乎找不到缺点", GOLD))
            .task(Task.item(GTMultiMachines.LARGE_BOILER_BRONZE))
            .dependsOn(Q_7BCD4971));

    public static final Quest STEAM_EXPANSION = CHAPTER.quest("steam_expansion", q -> q
            .at(2.5, -2)
            .title("蒸汽扩产")
            .desc(d -> d
                    .line("扩产{蒸汽}能够加速你的资源生产！", YELLOW)
                    .line()
                    .line("在拥有钢之后，{高压锅炉}必然是更优的蒸汽来源，其蒸汽输出为普通版本的{两至三倍}！", GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_SOLID_BOILER.second()))
            .dependsOn(Q_7BCD4971)
            .optional());

    public static final Quest COKE_OVEN_HATCH = CHAPTER.quest("coke_oven_hatch", q -> q
            .at(-3.5, 0)
            .subtitle("我们需要自动化！")
            .desc(d -> d
                    .line("焦炉仓可以同时作为输入和输出，它{不会}自动抽取物品，但可以{自动从输出口输出}物品和流体", YELLOW, YELLOW)
                    .line()
                    .line("所以实现自动化你可能需要一个漏斗")
                    .line()
                    .line("焦炉仓与主方块共享内容物"))
            .task(Task.item(GTMachines.COKE_OVEN_HATCH))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(COKE_OVEN));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.DIAMOND_BLOCK, 3.285714285714292, 6, 1, 1))
                .image(QuestImage.of(QuestTextures.DIAMOND_BLOCK, 5.285714285714292, 6, 1, 1))
                .image(QuestImage.of(QuestTextures.DIAMOND_BLOCK, 5.285714285714292, 8, 1, 1))
                .image(QuestImage.of(QuestTextures.DIAMOND_BLOCK, 3.285714285714292, 8, 1, 1))
                .image(QuestImage.of(QuestTextures.MULTIBLOCK_ANCIENT_REACTOR_CORE_OVERLAY_TOP, 4.285714285714292, 7, 1, 1))
                .image(QuestImage.of(QuestTextures.AGON_BLOCK_13_BLACK, 4.285714285714292, 7, 1, 1).after(BACK_TO_THE_PAST))
                .image(
                        QuestImage.of(QuestTextures.NETHER_PORTAL, 4.285714285714292, 7, 1.5, 1.5).rotation(45).tint(0xA40CAD).order(1).hover("前往远古世界").clickCommand("/execute in gtocore:ancient_world run tp 0 128 0").after(BACK_TO_THE_PAST))
                .image(QuestImage.of(QuestTextures.CASINGS_STEAM_BRICKED_STEEL_TOP, 4.285714285714292, 6, 1, 1).order(2))
                .image(QuestImage.of(QuestTextures.CASINGS_STEAM_BRICKED_STEEL_TOP, 5.285714285714292, 7, 1, 1).order(2))
                .image(QuestImage.of(QuestTextures.CASINGS_STEAM_BRICKED_STEEL_TOP, 4.285714285714292, 8, 1, 1).order(2))
                .image(QuestImage.of(QuestTextures.CASINGS_STEAM_BRICKED_STEEL_TOP, 3.285714285714292, 7, 1, 1).order(2))
                .link(QuestLink.of(LvChapter.LARGE_STEAM_SOLAR_BOILER, 4, 1))
                .link(QuestLink.of(UlvChapter.WELCOME_TO_THE_ULTRA_LOW_PRESSURE, -3.5, 14))
                .link(QuestLink.of(UlvChapter.BRAND_NEW_ENERGY_SYSTEM_HEAT, -1, -2)));
    }
}
