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

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.item.Items;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewSteamChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_steam", c -> c
            .title("2. 蒸汽时代(Steam Age)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.STEAM_SOLID_BOILER.first()));

    public static final Quest WRENCH = CHAPTER.quest("wrench", q -> q
            .at(8, -2)
            .title("扳手")
            .task(Task.tag(CustomTags.WRENCHES, 1).title("任意扳手"))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink WRENCH_COPY_1 = CHAPTER.copy(WRENCH, 10, -2);

    public static final QuestLink WRENCH_COPY_2 = CHAPTER.copy(WRENCH, 18, 0);

    public static final Quest FILE = CHAPTER.quest("file", q -> q
            .at(16, -4)
            .title("锉")
            .icon(ToolHelper.get(GTToolType.FILE, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.FILES, 1).title("任意锉"))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink FILE_COPY_1 = CHAPTER.copy(FILE, -6, 2);

    public static final Quest SAW = CHAPTER.quest("saw", q -> q
            .at(18, -4)
            .title("锯")
            .icon(ToolHelper.get(GTToolType.SAW, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.SAWS, 1).title("任意锯"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE));

    public static final Quest SCREWDRIVER = CHAPTER.quest("screwdriver", q -> q
            .at(16, -2)
            .title("螺丝刀")
            .icon(ToolHelper.get(GTToolType.SCREWDRIVER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.SCREWDRIVERS, 1).title("任意螺丝刀"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE));

    public static final QuestLink SCREWDRIVER_COPY_1 = CHAPTER.copy(SCREWDRIVER, 20, -4);

    public static final QuestLink SCREWDRIVER_COPY_2 = CHAPTER.copy(SCREWDRIVER, 12, 4);

    public static final Quest IRON_SCREW = CHAPTER.quest("iron_screw", q -> q
            .at(18, -2)
            .title("铁螺丝")
            .task(Task.item(TagPrefix.screw, GTMaterials.Iron, 5))
            .scaledReward(TagPrefix.rod, GTMaterials.Iron, 2)
            .dependsOn(SAW));

    public static final QuestLink IRON_SCREW_COPY_1 = CHAPTER.copy(IRON_SCREW, 22, -2);

    public static final QuestLink IRON_SCREW_COPY_2 = CHAPTER.copy(IRON_SCREW, 12, 8);

    public static final Quest WIRE_CUTTER = CHAPTER.quest("wire_cutter", q -> q
            .at(20, -2)
            .title("剪线钳")
            .icon(ToolHelper.get(GTToolType.WIRE_CUTTER, GTMaterials.Iron).getItem())
            .task(Task.tag(CustomTags.WIRE_CUTTERS, 1).title("任意剪线钳"))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(IRON_SCREW_COPY_1, SCREWDRIVER_COPY_1));

    public static final QuestLink WIRE_CUTTER_COPY_1 = CHAPTER.copy(WIRE_CUTTER, 10, 18);

    public static final Quest IRON_PICKAXE = CHAPTER.quest("iron_pickaxe", q -> q
            .at(-4, 2)
            .title("铁镐")
            .task(Task.item(Items.IRON_PICKAXE))
            .scaledReward(Items.IRON_INGOT, 2)
            .dependsOn(FILE_COPY_1));

    public static final Quest DIAMOND = CHAPTER.quest("diamond", q -> q
            .at(-4, 4)
            .title("钻石")
            .task(Task.item(Items.DIAMOND, 2))
            .scaledReward(Items.TORCH, 16)
            .dependsOn(IRON_PICKAXE));

    public static final Quest BLAST_FURNACE = CHAPTER.quest("blast_furnace", q -> q
            .at(16, 0)
            .milestone()
            .title("高炉")
            .task(Task.item(Items.BLAST_FURNACE))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(WRENCH_COPY_2, IRON_SCREW, SCREWDRIVER));

    public static final Quest HOT_IRON = CHAPTER.quest("hot_iron", q -> q
            .at(18, 16)
            .title("热铁锭")
            .task(Task.item(GTOItems.HOT_IRON_INGOT))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(BLAST_FURNACE));

    public static final Quest WROUGHT_IRON = CHAPTER.quest("wrought_iron", q -> q
            .at(16, 16)
            .title("锻铁")
            .subtitle("千锤百炼")
            .desc(d -> d
                    .line("把{铁锭}丢到炉子里面烧成{热铁锭}，然后在工作台上用{锻造锤}把它打成{锻铁锭}", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("你在下一个阶段会{大量}需要它的", DARK_PURPLE))
            .task(Task.item(TagPrefix.ingot, GTMaterials.WroughtIron, 22))
            .scaledReward(TagPrefix.ingot, GTMaterials.WroughtIron, 2)
            .dependsOn(HOT_IRON));

    public static final Quest ULV_CASING = CHAPTER.quest("ulv_casing", q -> q
            .at(16, 14)
            .title("ULV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_ULV))
            .scaledReward(TagPrefix.plate, GTMaterials.WroughtIron, 1)
            .dependsOn(WROUGHT_IRON));

    public static final Quest SOLID_BOILER = CHAPTER.quest("solid_boiler", q -> q
            .at(8, 0)
            .milestone()
            .title("低压蒸汽固体燃料锅炉")
            .subtitle("你终于可以开始用蒸汽做点事了")
            .desc(d -> d
                    .line("到这了？如果你从开始一直玩到这的话可以去休息会。")
                    .line()
                    .line("你需要制作一个{低压蒸汽固体燃料锅炉}，你不得不从这台机器开始。", GOLD)
                    .line()
                    .line("{继续前进，路还很长呢。}", DARK_GREEN))
            .task(Task.item(GTMachines.STEAM_SOLID_BOILER.first()))
            .scaledReward(Items.COAL, 4)
            .dependsOn(WRENCH));

    public static final Quest BRONZE_PIPE = CHAPTER.quest("bronze_pipe", q -> q
            .at(12, -2)
            .title("小型青铜流体管道")
            .task(Task.item(TagPrefix.pipeSmallFluid, GTMaterials.Bronze, 12))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(WRENCH_COPY_1));

    public static final QuestLink BRONZE_PIPE_COPY_1 = CHAPTER.copy(BRONZE_PIPE, 18, 2);

    public static final QuestLink BRONZE_PIPE_COPY_2 = CHAPTER.copy(BRONZE_PIPE, -8, 6);

    public static final Quest ALLOY_SMELTER = CHAPTER.quest("alloy_smelter", q -> q
            .at(12, 0)
            .milestone()
            .title("低压蒸汽合金炉")
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
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 1)
            .scaledReward(Items.REDSTONE, 4)
            .dependsOn(BRONZE_PIPE));

    public static final QuestLink ALLOY_SMELTER_COPY_1 = CHAPTER.copy(ALLOY_SMELTER, -2, -6);

    public static final Quest BRONZE_HULL = CHAPTER.quest("bronze_hull", q -> q
            .at(14, 4)
            .title("青铜外壳")
            .task(Task.item(GTBlocks.BRONZE_HULL, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final QuestLink BRONZE_HULL_COPY_1 = CHAPTER.copy(BRONZE_HULL, -6, 4);

    public static final QuestLink BRONZE_HULL_COPY_2 = CHAPTER.copy(BRONZE_HULL, -4, 10);

    public static final Quest RED_ALLOY = CHAPTER.quest("red_alloy", q -> q
            .at(20, 4)
            .title("红色合金")
            .subtitle("Ver.0")
            .desc(d -> d
                    .line("{ULV}时代重要的超导体，现在先用来制作{活塞}", ULV, GOLD)
                    .line()
                    .line("当然还有{非常重要}的{超导线}，你必然不会想错过它的吧？", DARK_PURPLE, DARK_RED))
            .task(Task.item(TagPrefix.ingot, GTMaterials.RedAlloy, 10))
            .scaledReward(Items.REDSTONE, 8)
            .dependsOn(ALLOY_SMELTER));

    public static final QuestLink RED_ALLOY_COPY_1 = CHAPTER.copy(RED_ALLOY, 8, 16);

    public static final Quest IRON_SMALL_GEAR = CHAPTER.quest("iron_small_gear", q -> q
            .at(20, 0)
            .title("铁小齿轮")
            .task(Task.item(TagPrefix.gearSmall, GTMaterials.Iron, 6))
            .scaledReward(TagPrefix.rod, GTMaterials.Iron, 2)
            .dependsOn(WIRE_CUTTER));

    public static final Quest PISTON = CHAPTER.quest("piston", q -> q
            .at(20, 2)
            .title("活塞")
            .subtitle("妈妈，我不要这种合成方式！")
            .desc(d -> d.line("特供版活塞，具有{格}{雷}{主}{义}的合成方式，敬请享用", AQUA, YELLOW, AQUA, YELLOW))
            .task(Task.item(Items.PISTON, 3))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(RED_ALLOY, IRON_SMALL_GEAR));

    public static final QuestLink PISTON_COPY_1 = CHAPTER.copy(PISTON, -8, 4);

    public static final QuestLink PISTON_COPY_2 = CHAPTER.copy(PISTON, -2, 14);

    public static final Quest COMPRESSOR = CHAPTER.quest("compressor", q -> q
            .at(16, 6)
            .milestone()
            .title("低压蒸汽压缩机")
            .subtitle("瘦身之旅")
            .desc(d -> d
                    .line("想要炼钢，就要有个能炼钢的炉子，想要有能承受足够高炉温的炉子，就需要足够优秀的外壳材料，那么就需要一个{强力的机器(?)}来生产这种材料了", YELLOW)
                    .line()
                    .line("很明显{低压蒸汽压缩机}会比你手动压缩更加强力...吧？", GOLD)
                    .line()
                    .line("不管怎么说，你需要{低压蒸汽压缩机}来帮你完成耐火砖的准备，而这是建造{土高炉}所必须的", GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_COMPRESSOR.first()))
            .scaledReward(Items.CLAY_BALL, 16)
            .dependsOn(PISTON, BRONZE_HULL, BRONZE_PIPE_COPY_1));

    public static final Quest STEAM_MACERATOR = CHAPTER.quest("steam_macerator", q -> q
            .at(-6, 6)
            .title("低压蒸汽粉碎机")
            .subtitle("拯救你的研钵")
            .desc(d -> d
                    .line("通入{蒸汽}，即可粉碎矿石或者是其他零零碎碎的东西", GOLD)
                    .line()
                    .line("什么，你问为什么研磨矿石没有如EMI所示出产{副产物}？", YELLOW)
                    .line()
                    .line("想要研磨矿石出产副产物需要至少{HV级}的{研磨机}，你可以在其他研磨机的tooltips中看到", YELLOW, GOLD))
            .task(Task.item(GTMachines.STEAM_MACERATOR.first()))
            .scaledReward(Items.SAND, 16)
            .dependsOn(DIAMOND, PISTON_COPY_1, BRONZE_HULL_COPY_1, BRONZE_PIPE_COPY_2));

    public static final Quest CLAY_BRICK_DUST = CHAPTER.quest("clay_brick_dust", q -> q
            .at(14, 10)
            .title("黏土粉与砖粉")
            .icon(TagPrefix.dust, GTMaterials.Clay)
            .task(Task.item(TagPrefix.dust, GTMaterials.Clay, 8), Task.item(TagPrefix.dust, GTMaterials.Brick, 8))
            .scaledReward(Items.CLAY_BALL, 8)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest FIRECLAY_DUST = CHAPTER.quest("fireclay_dust", q -> q
            .at(16, 10)
            .title("耐火黏土粉")
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
            .task(Task.item(TagPrefix.dust, GTMaterials.Fireclay, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Fireclay, 4)
            .dependsOn(CLAY_BRICK_DUST));

    public static final Quest COMPRESSED_FIRECLAY = CHAPTER.quest("compressed_fireclay", q -> q
            .at(16, 8)
            .title("压缩耐火黏土")
            .task(Task.item(GTItems.COMPRESSED_FIRECLAY, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Fireclay, 4)
            .dependsOn(FIRECLAY_DUST, COMPRESSOR));

    public static final Quest FIREBRICK = CHAPTER.quest("firebrick", q -> q
            .at(14, 8)
            .title("耐火砖")
            .task(Task.item(GTItems.FIRECLAY_BRICK, 16))
            .scaledReward(GTBlocks.CASING_PRIMITIVE_BRICKS, 2)
            .dependsOn(COMPRESSED_FIRECLAY));

    public static final Quest PRIMITIVE_BLAST_FURNACE = CHAPTER.quest("primitive_blast_furnace", q -> q
            .at(12, 6)
            .milestone()
            .title("土高炉")
            .subtitle("准备好了吗？")
            .desc(d -> d
                    .line("{土高炉}能够像{焦炉}一样{自}{动}{化}了，试试看吧", GOLD, GOLD, LIGHT_PURPLE, YELLOW, AQUA)
                    .line()
                    .line("{终于不用手搬来搬去的了}", DARK_GRAY.strike())
                    .line()
                    .line("具体的多方块结构参考EMI的“{多方块结构信息}”选项", DARK_GREEN)
                    .line()
                    .line("以及，土高炉运作的时候中间那块岩浆是{有伤害的}", DARK_RED))
            .task(Task.item(GTMultiMachines.PRIMITIVE_BLAST_FURNACE), Task.item(GTOMachines.PRIMITIVE_BLAST_FURNACE_HATCH, 2), Task.item(GTBlocks.CASING_PRIMITIVE_BRICKS, 30))
            .scaledReward(Items.COAL, 8)
            .dependsOn(FIREBRICK, IRON_SCREW_COPY_2, SCREWDRIVER_COPY_2));

    public static final Quest STEEL = CHAPTER.quest("steel", q -> q
            .at(8, 6)
            .milestone()
            .title("第一块钢锭")
            .subtitle("真正的生产力大跃进")
            .desc(d -> d
                    .line("在这个阶段，我建议你使用{木炭/煤炭块}来生产钢。因为这种方式下生产的副产物可以生成{碳粉}，是碳粉前期的获取途径之一", GREEN, YELLOW)
                    .line()
                    .line("{另一种方法是处理滑石/石墨矿}", GRAY.italic()))
            .task(Task.item(ModItems.STEEL_INGOT.get(), 16))
            .scaledReward(ModItems.STEEL_INGOT.get(), 2)
            .dependsOn(PRIMITIVE_BLAST_FURNACE));

    public static final QuestLink STEEL_COPY_1 = CHAPTER.copy(STEEL, -8, -6);

    public static final Quest EMPTY_MOLD = CHAPTER.quest("empty_mold", q -> q
            .at(-4, 16)
            .title("空白模具")
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
            .scaledReward(ModItems.STEEL_INGOT.get(), 2)
            .dependsOn(STEEL));

    public static final Quest MOLDS = CHAPTER.quest("molds", q -> q
            .at(-4, 14)
            .title("模具")
            .task(Task.item(GTItems.SHAPE_MOLD_BLOCK), Task.item(GTItems.SHAPE_MOLD_PLATE), Task.item(GTItems.SHAPE_MOLD_BALL))
            .scaledReward(Items.SAND, 8)
            .dependsOn(EMPTY_MOLD));

    public static final QuestLink MOLDS_COPY_1 = CHAPTER.copy(MOLDS, 4, -4);

    public static final QuestLink MOLDS_COPY_2 = CHAPTER.copy(MOLDS, 4, 10);

    public static final Quest GLASS_DUST = CHAPTER.quest("glass_dust", q -> q
            .at(4, 0)
            .milestone()
            .title("玻璃粉")
            .subtitle("手动拼接？")
            .desc(d -> d
                    .line("对的，你最开始的{玻璃粉}就是需要{工作台}手动组装", GOLD, GREEN)
                    .line()
                    .line("等到你有条件使用{搅拌机}了，也就可以省点事了，但你距离能使用{蒸汽搅拌机}还有一定的距离，所以短时间内你还是得在{工作台}上手动组装{玻璃粉}", GOLD, GOLD, GREEN, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Glass, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Glass, 2)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest GLASS = CHAPTER.quest("glass", q -> q
            .at(-4, 12)
            .title("玻璃")
            .subtitle("熔炼成块！")
            .desc(d -> d
                    .line("简单保留了原版玻璃的合成配方，因此你无需通过石英砂制作玻璃。直接完成此任务可跳过制备玻璃第一步")
                    .line()
                    .line("玻璃粉可以通过锤子敲玻璃获得")
                    .line()
                    .line("用{合金炉}和{模具(块)}来塑形玻璃，这是最后一步", GOLD, GOLD)
                    .line()
                    .line("累了要休息下吗？不妨听听后续的改进建议：")
                    .line()
                    .line("当然你要想让{玻璃制造}变得更简单，还有办法，让{玻璃粉}由{搅拌机}生产是其中之一；而用提取机提取{石英岩}得到的{液态玻璃}直接在{流体固化器}内用{模具(块)}则可以更加简便地生产玻璃", GOLD, GREEN, GOLD, LIGHT_PURPLE, GREEN, GOLD, GOLD)
                    .line()
                    .line("另还有将{液态玻璃}与{沙子}在{电弧炉}内反应的办法；以及使用{冲压机床}生产，节省1/3的时间", LIGHT_PURPLE, YELLOW, GOLD, GOLD)
                    .line()
                    .line("至于后续如何选择，取决于你"))
            .task(Task.item(Items.GLASS, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Bronze, 2)
            .dependsOn(MOLDS, GLASS_DUST, ALLOY_SMELTER));

    public static final QuestLink GLASS_COPY_1 = CHAPTER.copy(GLASS, -8, 0);

    public static final QuestLink GLASS_COPY_2 = CHAPTER.copy(GLASS, -4, -6);

    public static final QuestLink GLASS_COPY_3 = CHAPTER.copy(GLASS, -6, 0);

    public static final QuestLink GLASS_COPY_4 = CHAPTER.copy(GLASS, -4, 0);

    public static final Quest STEAM_EXTRACTOR = CHAPTER.quest("steam_extractor", q -> q
            .at(-2, 12)
            .milestone()
            .title("低压蒸汽提取机")
            .subtitle("但其实也没什么区别不是吗")
            .desc(d -> d
                    .line("当前阶段你需要这个家伙来帮助你完成{橡胶}的制作", GOLD)
                    .line()
                    .line("我建议你留着{橡胶原木}，因为一来它的产出并不多，二来它能在{离心机}里产出{胶水}", LIGHT_PURPLE, GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_EXTRACTOR.first()))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(GLASS, PISTON_COPY_2, BRONZE_HULL_COPY_2));

    public static final Quest STICKY_RESIN = CHAPTER.quest("sticky_resin", q -> q
            .at(0, 16)
            .title("粘性树脂")
            .subtitle("并不需要木龙头")
            .desc(d -> d
                    .line("希望你注意到了{橡胶树}并且把它们带回了家，如果没有注意到初章的任务你现在将尴尬地等待橡胶树生长", GOLD)
                    .line()
                    .line("{公司警告你禁止摸鱼}", DARK_GRAY.strike())
                    .line()
                    .line("应该尽快种起来，{种多点肯定是没错的}", YELLOW)
                    .line()
                    .line("然后用连锁功能把橡胶原木批量砍下来就行了，粘性树脂会随机掉落"))
            .task(Task.item(GTItems.STICKY_RESIN, 4))
            .scaledReward(GTBlocks.RUBBER_SAPLING, 4)
            .dependsOn(NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest RAW_RUBBER = CHAPTER.quest("raw_rubber", q -> q
            .at(0, 14)
            .title("生橡胶粉")
            .subtitle("提取的产物")
            .desc(d -> d
                    .line("你在查EMI吗？我来直接告诉你吧：你现在只能用{蒸汽提取机}来提取生橡胶末", GOLD)
                    .line()
                    .line("如果你提取的是粘性树脂，那你一次可以提取{三个}生橡胶末；如果是橡胶原木则一次性只能产出{一个}", GREEN, GREEN)
                    .line()
                    .line("记得整个容器来自动容纳产物，提取的流程很漫长而且容易{堵塞产物}", RED)
                    .line()
                    .line("当然你有{蒸汽分离机}了就可以用其他办法来制造生橡胶末了", GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.RawRubber, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.RawRubber, 6)
            .dependsOn(STEAM_EXTRACTOR, STICKY_RESIN));

    public static final Quest PULSATING_ALLOY = CHAPTER.quest("pulsating_alloy", q -> q
            .at(2, 8)
            .title("脉冲合金")
            .subtitle("噢！是魔法超导体！")
            .desc(d -> d
                    .line("这也是一种{超导体}，只是需要{魔力灌注}", DARK_RED, AQUA)
                    .line()
                    .line("在{ULV}电压，常规超导线非常容易获取（即{红色合金线缆}）。但在后面的电压，常规超导线往往需要推进许多科技才可以制作", ULV, GOLD)
                    .line()
                    .line("因此我强烈推荐你制作每个阶段的{魔法合金}作为超导体", GOLD))
            .icon(TagPrefix.ingot, GTOMaterials.PulsatingAlloy)
            .task(Task.item(TagPrefix.block, GTOMaterials.PulsatingAlloy))
            .scaledReward(Items.ENDER_PEARL, 4)
            .dependsOn(ALLOY_SMELTER, COMPRESSOR));

    public static final Quest ENGINE = CHAPTER.quest("engine", q -> q
            .at(22, 2)
            .side()
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
            .optional()
            .dependsOn(PISTON));

    public static final Quest ANCIENT_REACTOR_CORE = CHAPTER.quest("ancient_reactor_core", q -> q
            .at(2, 6)
            .milestone()
            .title("远古反应核")
            .subtitle("《回到过去》")
            .desc(d -> d
                    .line("用这些材料摆一个3x3的平面结构")
                    .line()
                    .line("关于结构：{远古反应核}置于中心，相邻4个方块为{钢块}，角落4个方块为钻石块，{平放}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{右键远古反应核即可前往远古世界}", GOLD)
                    .line()
                    .line("如果你还不会飞，可以带个{飞机}", link(NewSteamChapter.ENGINE))
                    .line()
                    .line("{MAN!}{别}{坠}{机}{了}", GOLD, ITALIC.strike(), GOLD.italic().strike(), DARK_PURPLE.strike(), STRIKE))
            .task(Task.item(MultiBlockG.ANCIENT_REACTOR_CORE))
            .scaledReward(ModItems.STEEL_INGOT.get(), 8)
            .dependsOn(PULSATING_ALLOY, STEEL, DIAMOND));

    public static final Quest ANCIENT_WORLD = CHAPTER.quest("ancient_world", q -> q
            .at(-2, 6)
            .milestone()
            .title("远古世界")
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
            .scaledReward(TagPrefix.dust, GTMaterials.Sphalerite, 8)
            .dependsOn(ANCIENT_REACTOR_CORE));

    public static final Quest SULFUR = CHAPTER.quest("sulfur", q -> q
            .at(2, 16)
            .title("硫粉")
            .subtitle("不行，一股臭鸡蛋味儿")
            .desc(d -> d
                    .line("硫粉，主世界并不直接产出，但经常作为各种矿物处理中的副产物产出")
                    .line()
                    .line("不过上述的处理大多数时候都是需要{更高的电压}才能完成", DARK_GREEN)
                    .line()
                    .line("而更直接的来源是去远古世界挖掘{硫矿脉}", DARK_PURPLE)
                    .line()
                    .line("你需要准备多点，因为你需要{很多的}{橡胶}", DARK_RED, GOLD))
            .task(Task.item(TagPrefix.dust, GTMaterials.Sulfur, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 2)
            .dependsOn(ANCIENT_WORLD));

    public static final Quest RUBBER_INGOT = CHAPTER.quest("rubber_ingot", q -> q
            .at(4, 14)
            .title("橡胶锭")
            .subtitle("千辛万苦")
            .desc(d -> d
                    .line("将你得到的{生橡胶末}和{硫粉}在{合金炉}内反应即可生成{橡胶条}", DARK_AQUA, YELLOW, GOLD, GOLD)
                    .line()
                    .line("当然这种方式的速度是比较慢的，等你到了LV阶段的时候你就可以使用{化学反应釜}来制{液态橡胶}，并且通过{流体固化器}来固化成你想要的任何形状的{橡胶}", GOLD, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("但距离LV还是有一定距离的，你还是需要先提升一下你当前阶段的生产力"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Rubber, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Rubber, 2)
            .dependsOn(RAW_RUBBER, SULFUR));

    public static final Quest A_MESSY_STEW = CHAPTER.quest("a_messy_stew", q -> q
            .at(12, 2)
            .side()
            .title("一锅乱炖")
            .subtitle("但这口锅够大")
            .desc(d -> d
                    .line("这是一个拥有{8并行}的{蒸汽合金炉}", GREEN, GOLD)
                    .line()
                    .line("当然你需要足够的蒸汽，大概是{250mb/t}就足够了，要不然没办法满功率运作它", RED)
                    .line()
                    .line("记得千万别站在{蒸汽排气口}面前，喷出蒸汽的时候是{有伤害的}，也要注意{蒸汽排气口}是否有被{阻挡}", DARK_RED, DARK_PURPLE, DARK_RED, GREEN))
            .task(Task.item(MultiBlockA.STEAM_FOUNDRY))
            .dependsOn(ALLOY_SMELTER));

    public static final Quest RUBBER_PLATE = CHAPTER.quest("rubber_plate", q -> q
            .at(6, 12)
            .milestone()
            .title("橡胶片")
            .subtitle("我需要多一台合金炉！")
            .desc(d -> d
                    .line("用{合金炉}和{板模具}将{橡胶条}塑形为{橡胶片}", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("很明显的这是{合金炉}的配方，你肯定需要多一台{合金炉}", GOLD, GOLD)
                    .line()
                    .line("或者一个{多方块合金炉}的", link(NewSteamChapter.A_MESSY_STEW)))
            .task(Task.item(TagPrefix.plate, GTMaterials.Rubber, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Rubber, 2)
            .dependsOn(RUBBER_INGOT, MOLDS_COPY_2));

    public static final Quest RED_ALLOY_WIRE = CHAPTER.quest("red_alloy_wire", q -> q
            .at(10, 16)
            .title("红色合金导线")
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
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.RedAlloy, 2))
            .scaledReward(TagPrefix.ingot, GTMaterials.RedAlloy, 2)
            .dependsOn(RED_ALLOY_COPY_1, WIRE_CUTTER_COPY_1));

    public static final Quest RED_ALLOY_CABLE = CHAPTER.quest("red_alloy_cable", q -> q
            .at(10, 14)
            .title("红色合金线缆")
            .subtitle("线靠衣装")
            .desc(d -> d
                    .line("唯二你能手动合成的线缆之一，在你做出{原始打包机}之后就可以不用手动合成了", GOLD)
                    .line()
                    .line("当然只能合成1x的线缆，即该线缆只能支持{1A}的电流上限，{电流再大就要烧导线了！！}", DARK_RED, DARK_RED)
                    .line()
                    .line("不过合成导线的方式{不止一条}，多看看EMI吧！", YELLOW))
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.WroughtIron, 1)
            .dependsOn(RED_ALLOY_WIRE, RUBBER_PLATE));

    public static final Quest ULV_HULL = CHAPTER.quest("ulv_hull", q -> q
            .at(16, 12)
            .milestone()
            .title("ULV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", GRAY)
            .desc(d -> d
                    .line("欢迎来到超低压阶段")
                    .line()
                    .line("哪怕是再微弱的电那都是电，所以注意不要被裸露的导线电死了")
                    .line()
                    .line("从现在起，你可以开始着手降低材料的造价，或许将{原始线材轧机}、{原始车床}作为你新时代的开端是不错的选择？", GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.ULV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.ULV], 1))
            .dependsOn(ULV_CASING, RED_ALLOY_CABLE));

    public static final Quest BIG_HAMMER_80_SMALL_HAMMER_40 = CHAPTER.quest("big_hammer_80_small_hammer_40", q -> q
            .at(18, 8)
            .side()
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
            .dependsOn(COMPRESSOR));

    public static final Quest CAN_STEAM_BE_USED_FOR_COOKING = CHAPTER.quest("can_steam_be_used_for_cooking", q -> q
            .at(2, 4)
            .side()
            .title("蒸汽能做饭吗？")
            .subtitle("最大程度节省你的燃料")
            .desc(d -> d
                    .line("小蒸汽机器必须通入{蒸汽}才能运转，而且{排气口}{不能}被阻挡（包括管道）", GOLD, DARK_GREEN, DARK_RED)
                    .line()
                    .line("什么，你问我怎么调整排气口的位置？那你肯定是没仔细看{使用手册}", link(TipsChapter.GT_TOOL_USER_MANUAL))
                    .line()
                    .line("由于巨型熔炉的存在，蒸汽熔炉系列是可选的"))
            .task(Task.item(GTMachines.STEAM_FURNACE.first()))
            .optional()
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest A_SLIGHTLY_LARGER_FURNACE = CHAPTER.quest("a_slightly_larger_furnace", q -> q
            .at(4, 4)
            .side()
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
            .optional()
            .dependsOn(CAN_STEAM_BE_USED_FOR_COOKING));

    public static final Quest LARGER_AUTO_MORTAR = CHAPTER.quest("larger_auto_mortar", q -> q
            .at(-4, 6)
            .side()
            .title("更大的自动研钵")
            .subtitle("效率这一块")
            .desc(d -> d
                    .line("这个家伙具有{8并行}，但跟它的小机器版本一样，{没有副产物产出}", DARK_RED, DARK_RED)
                    .line()
                    .line("普通难度下，单个配方具有{1.5倍}的用时惩罚，但依然是一个非常优秀的选择", DARK_RED))
            .task(Task.item(GTMultiMachines.STEAM_GRINDER))
            .dependsOn(STEAM_MACERATOR));

    public static final Quest THE_HAMMER_FALLS = CHAPTER.quest("the_hammer_falls", q -> q
            .at(18, 10)
            .side()
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

    public static final Quest THIS_IS_NO_HYDRAULIC_PRESS = CHAPTER.quest("this_is_no_hydraulic_press", q -> q
            .at(18, 6)
            .side()
            .title("这可不是液压机")
            .subtitle("我们需要更多的土高炉！")
            .desc(d -> d
                    .line("{8并行}，如同之前的其他小机器升级版本一样，也有{1.5倍}的配方时间惩罚", DARK_RED, DARK_RED)
                    .line()
                    .line("当然你现在这个阶段做这个东西肯定是为了做更多的{土高炉}，对吧？", GOLD))
            .task(Task.item(MultiBlockA.STEAM_PRESSOR))
            .dependsOn(COMPRESSOR));

    public static final Quest PREPARE_GLASS_FIRST_STEP = CHAPTER.quest("prepare_glass_first_step", q -> q
            .at(-6, 8)
            .side()
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
            .dependsOn(STEAM_MACERATOR));

    public static final Quest DISCOUNT_TIME_BOOM_BOOM_BOOM = CHAPTER.quest("discount_time_boom_boom_boom", q -> q
            .at(20, 8)
            .side()
            .title("打折时刻！爆爆爆！")
            .subtitle("用你的新锻造锤做点东西吧")
            .task(Task.item(TagPrefix.plate, GTMaterials.WroughtIron, 8))
            .reward(Reward.item(Items.IRON_INGOT, 16))
            .dependsOn(BIG_HAMMER_80_SMALL_HAMMER_40));

    public static final Quest MIXER_STEAM_POWERED = CHAPTER.quest("mixer_steam_powered", q -> q
            .at(10, 2)
            .side()
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
            .dependsOn(STEEL));

    public static final Quest ORE_SMASHER = CHAPTER.quest("ore_smasher", q -> q
            .at(6, 8)
            .side()
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
            .dependsOn(STEEL));

    public static final Quest MORPHING = CHAPTER.quest("morphing", q -> q
            .at(10, 8)
            .side()
            .title("形态变化！")
            .subtitle("三维变二维？")
            .desc(d -> d
                    .line("希望你这时没有在使用工作台加锤子的组合来制作钢板了")
                    .line()
                    .line("你需要节省材料和时间"))
            .task(Task.item(ModItems.STEEL_PLATE.get()))
            .reward(Reward.item(ModItems.STEEL_PLATE.get(), 16))
            .dependsOn(STEEL));

    public static final Quest NEED_A_LOT_OF_STEEL = CHAPTER.quest("need_a_lot_of_steel", q -> q
            .at(8, 8)
            .side()
            .title("需要大量钢？")
            .subtitle("32层举高高炉")
            .desc(d -> d
                    .line("虽然很酷，但这不是你现在能想的机器")
                    .line()
                    .line("准备{11组钢锭}再来看看吧", DARK_RED))
            .task(Task.item(MultiBlockA.LEAP_FORWARD_ONE_BLAST_FURNACE))
            .dependsOn(STEEL));

    public static final Quest COKE_OVEN_PRO_MAX_EDITION = CHAPTER.quest("coke_oven_pro_max_edition", q -> q
            .at(6, 4)
            .side()
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
            .dependsOn(STEEL));

    public static final Quest LARGER_INSULATED_CUP = CHAPTER.quest("larger_insulated_cup", q -> q
            .at(10, 4)
            .side()
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
            .dependsOn(STEEL));

    public static final Quest MORE_POWERFUL_MACHINES = CHAPTER.quest("more_powerful_machines", q -> q
            .at(8, 2)
            .side()
            .title("更猛的机器们")
            .subtitle("Work！Work！")
            .desc(d -> d
                    .line("有了钢锭，你现在可以制作{高压蒸汽机器}，它们的效率是低压版本的{2倍}，但同时每秒也消耗更多{蒸汽}", GREEN, RED, GOLD)
                    .line()
                    .line("而{高压锅炉}可以获得比{普通锅炉}更高的产出，从2倍到3倍不止，建议查看EMI以及个人基地的{实际情况}进行考量", GOLD, GOLD, DARK_BLUE))
            .task(
                    Task.anyOf(1, GTMachines.STEAM_SOLID_BOILER.second(), GTOMachines.STEAM_VACUUM_PUMP.second(), GTMachines.STEAM_LIQUID_BOILER.second(), GTMachines.STEAM_SOLAR_BOILER.second(), GTMachines.STEAM_EXTRACTOR.second(), GTMachines.STEAM_MACERATOR.second(), GTMachines.STEAM_COMPRESSOR.second(), GTMachines.STEAM_HAMMER.second(), GTMachines.STEAM_FURNACE.second(), GTMachines.STEAM_ALLOY_SMELTER.second(), GTMachines.STEAM_ROCK_CRUSHER.second()).title("高压蒸汽机器"))
            .dependsOn(STEEL));

    public static final Quest VAULTS = CHAPTER.quest("vaults", q -> q
            .at(6, 2)
            .side()
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
            .dependsOn(NeolithicChapter.SIMPLE_CRAFTING_TERMINAL, STEEL));

    public static final Quest OTHER_COMMON_MOLDS = CHAPTER.quest("other_common_molds", q -> q
            .at(-2, 16)
            .side()
            .title("其他常用模具")
            .subtitle("你可能会用到它们")
            .task(Task.item(GTItems.SHAPE_MOLD_NUGGET), Task.item(GTItems.SHAPE_MOLD_INGOT))
            .optional()
            .dependsOn(EMPTY_MOLD));

    public static final Quest LP_STEAM_MINER = CHAPTER.quest("lp_steam_miner", q -> q
            .at(-2, -2)
            .side()
            .subtitle("并不能解放双手")
            .desc(d -> d
                    .line("可以帮你自动挖矿，但是效率实在是太低了，还没你手挖的快")
                    .line()
                    .line("你可以选择不完成这个任务，毕竟它是可选的"))
            .task(Task.item(GTMachines.STEAM_MINER.first()), Task.item(GTMachines.STEAM_MINER.second()))
            .optional()
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest STEEL_SUBSIDY = CHAPTER.quest("steel_subsidy", q -> q
            .at(10, 10)
            .side()
            .icon(ModItems.STEEL_INGOT.get())
            .title("钢补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(ModItems.STEEL_INGOT.get(), 640L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(ModItems.STEEL_INGOT.get(), 192L))
            .optional().hideUntilDepsVisible()
            .dependsOn(MORPHING));

    public static final Quest BLOW_A_GLASS_BUBBLE = CHAPTER.quest("blow_a_glass_bubble", q -> q
            .at(4, -2)
            .side()
            .title("吹个玻璃泡泡")
            .subtitle("让合金炉来！")
            .desc(d -> d
                    .line("使用{合金炉}和特定形状的{模具}来塑形玻璃，此类配方你将来会遇到很多", GOLD, GOLD)
                    .line()
                    .line("当然合金炉只是目前的处理办法，你后面用{流体固化机}或者{压模器}都是一个{更好的}选择", GOLD, GOLD, GREEN))
            .task(Task.item(GTItems.GLASS_TUBE))
            .dependsOn(GLASS_DUST, MOLDS_COPY_1));

    public static final Quest NOT_AN_ELECTRON_TUBE = CHAPTER.quest("not_an_electron_tube", q -> q
            .at(6, -2)
            .side()
            .title("并非电子管")
            .subtitle("工作台要搓冒烟了！")
            .desc(d -> d
                    .line("在你制作出第一个电路之前，你需要做一个它的前身，那就是{粗真空管}", GOLD)
                    .line()
                    .line("这东西{只有}工作台的配方，所以我希望你准备好了足够多的材料，然后在工作台上大闹一番吧", YELLOW))
            .task(Task.item(GTOItems.RAW_VACUUM_TUBE))
            .dependsOn(BLOW_A_GLASS_BUBBLE));

    public static final Quest LARGE_POOL = CHAPTER.quest("large_pool", q -> q
            .at(4, 16)
            .side()
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
            .dependsOn(RUBBER_INGOT));

    public static final Quest DRUM_WASHING_MACHINE_MINERAL_SPECIFIC = CHAPTER.quest("drum_washing_machine_mineral_specific", q -> q
            .at(6, 16)
            .side()
            .title("滚筒洗衣机，矿物专用")
            .subtitle("然而并不能完全洗干净")
            .desc(d -> d
                    .line("注意看EMI的配方，不同的配方需要{不同的电路}，对应需要的液体也会{有所不同}，但需要在洗矿机内执行的含液体配方无非就是{水}和{蒸馏水}二选一", DARK_RED, RED, DARK_BLUE, AQUA)
                    .line()
                    .line("{8并行}的版本足够你应付目前的部分处理配方的，只要你不手欠处理了{红石}和{青金石}等配方...", DARK_RED, DARK_RED, AQUA))
            .task(Task.item(MultiBlockA.STEAM_ORE_WASHER))
            .dependsOn(RUBBER_INGOT));

    public static final Quest RUBBER_SUBSIDY = CHAPTER.quest("rubber_subsidy", q -> q
            .at(6, 10)
            .side()
            .icon(TagPrefix.plate, GTMaterials.Rubber)
            .title("橡胶补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Rubber, 128L))
            .optional().hideUntilDepsVisible()
            .dependsOn(RUBBER_PLATE));

    public static final Quest GT_PIPES = CHAPTER.quest("gt_pipes", q -> q
            .at(2, -2)
            .side()
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
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest WROUGHT_IRON_INGOT_SUBSIDY = CHAPTER.quest("wrought_iron_ingot_subsidy", q -> q
            .at(18, 12)
            .side()
            .icon(TagPrefix.ingot, GTMaterials.WroughtIron)
            .title("锻铁锭补助")
            .subtitle("下一个阶段的重要材料")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.ingot, GTMaterials.WroughtIron, 192L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .optional().hideUntilDepsVisible()
            .dependsOn(ULV_HULL));

    public static final Quest ENERGISTICALLY_APPLIED_START_EASY = CHAPTER.quest("energistically_applied_start_easy", q -> q
            .at(0, 8)
            .side()
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
            .optional().hideUntilDepsVisible()
            .dependsOn(ANCIENT_WORLD, UtilChapter.Q_056EC4E6));

    public static final Quest MULTIBLOCK_STEAM_MACHINE_HATCH_GUIDE = CHAPTER.quest("multiblock_steam_machine_hatch_guide", q -> q
            .at(4, 2)
            .side()
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
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest ITEM_PIPE = CHAPTER.quest("item_pipe", q -> q
            .at(-2, 2)
            .side()
            .subtitle("另一种管道")
            .desc(d -> d.line("你也可以使用来自pipez的管道来物流"))
            .task(Task.item(QuestExternalRefs.PipezItems.ITEM_PIPE))
            .reward(Reward.item(QuestExternalRefs.PipezItems.ITEM_PIPE, 12))
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest FLUID_PIPE = CHAPTER.quest("fluid_pipe", q -> q
            .at(-2, 0)
            .side()
            .subtitle("另一种管道")
            .desc(d -> d.line("你也可以使用来自pipez的管道来物流"))
            .task(Task.item(QuestExternalRefs.PipezItems.FLUID_PIPE))
            .reward(Reward.item(QuestExternalRefs.PipezItems.FLUID_PIPE, 12))
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest STEAM_POWERED = CHAPTER.quest("steam_powered", q -> q
            .at(0, 2)
            .side()
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
            .dependsOn(NeolithicChapter.WESTERNIZATION_MOVEMENT, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest VACUUM_TUBE_SUBSIDY = CHAPTER.quest("vacuum_tube_subsidy", q -> q
            .at(6, -4)
            .side()
            .icon(GTItems.VACUUM_TUBE)
            .title("真空管补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.VACUUM_TUBE, 64))
            .normalSubsidy(Reward.item(GTItems.VACUUM_TUBE, 12))
            .optional().hideUntilDepsVisible()
            .dependsOn(NOT_AN_ELECTRON_TUBE, NeolithicChapter.WESTERNIZATION_MOVEMENT));

    public static final Quest ANY_PLUNGERS = CHAPTER.quest("any_plungers", q -> q
            .at(6, 14)
            .side()
            .desc(d -> d.line("清空容器或者机器中的流体，{不会返还}", RED))
            .task(Task.tag(CustomTags.PLUNGERS, 1).title("任意搋子"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .optional()
            .dependsOn(RUBBER_PLATE));

    public static final Quest BRICK_WOODEN_FORM = CHAPTER.quest("brick_wooden_form", q -> q
            .at(0, -2)
            .side()
            .subtitle("可重复")
            .desc(d -> d.line("这东西在接下来的合成中是可以重复利用的，所以造一个就好了"))
            .task(Task.item(GTItems.WOODEN_FORM_BRICK))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .optional()
            .dependsOn(NeolithicChapter.A_FULL_ARSENAL, NeolithicChapter.BRONZE_BRICK_HULL));

    public static final Quest COMPRESSED_COKE_CLAY = CHAPTER.quest("compressed_coke_clay", q -> q
            .at(0, -4)
            .side()
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
            .optional()
            .dependsOn(BRICK_WOODEN_FORM));

    public static final Quest COKE_OVEN_BRICK = CHAPTER.quest("coke_oven_brick", q -> q
            .at(0, -6)
            .side()
            .subtitle("需要点火")
            .desc(d -> d.line("你有两个选择制作焦炉砖块，使用耗材较多，无需等待的手搓配方；或是耗材较少，但是需要等待的合金炉配方"))
            .task(Task.item(GTItems.COKE_OVEN_BRICK).consume(false))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .requireAny()
            .requireCount(2)
            .dependsOn(COMPRESSED_COKE_CLAY, ALLOY_SMELTER_COPY_1, NeolithicChapter.STICKY));

    public static final Quest LOOKING_AT_A_PUMP_TO_QUENCH = CHAPTER.quest("looking_at_a_pump_to_quench", q -> q
            .at(-2, -4)
            .side()
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
            .dependsOn(COKE_OVEN_BRICK));

    public static final Quest SEMI_AUTOMATION = CHAPTER.quest("semi_automation", q -> q
            .at(-4, -4)
            .side()
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
            .at(-6, -4)
            .side()
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

    public static final Quest SOLAR_BOILER = CHAPTER.quest("solar_boiler", q -> q
            .at(-8, -2)
            .side()
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
            .optional()
            .dependsOn(Q_7BCD4971, GLASS_COPY_1));

    public static final Quest HP_STEAM_SOLAR_BOILER = CHAPTER.quest("hp_steam_solar_boiler", q -> q
            .at(-6, -6)
            .side()
            .desc(d -> d.line("三倍蒸汽产能，如果有钢，几乎没有理由用普通版本！"))
            .task(Task.item(GTMachines.STEAM_SOLAR_BOILER.second()))
            .optional()
            .dependsOn(Q_7BCD4971, GLASS_COPY_2));

    public static final Quest LP_STEAM_LIQUID_BOILER = CHAPTER.quest("lp_steam_liquid_boiler", q -> q
            .at(-6, -2)
            .side()
            .desc(d -> d
                    .line("{液体燃料锅炉}的产能是{固体燃料锅炉}的两倍，如果你的{杂酚油}多得没地方存，为何不将这些黏糊糊的{半流质}流体通通喂给{液体燃料锅炉}呢？", GOLD, YELLOW, GOLD, GOLD, GOLD)
                    .line()
                    .line("{……我刚刚说了}{半流质}{对吧}", YELLOW, GRAY.underline(), YELLOW))
            .task(Task.item(GTMachines.STEAM_LIQUID_BOILER.first()))
            .optional()
            .dependsOn(Q_7BCD4971, GLASS_COPY_3));

    public static final Quest HP_STEAM_LIQUID_BOILER = CHAPTER.quest("hp_steam_liquid_boiler", q -> q
            .at(-4, -2)
            .side()
            .subtitle("更疯狂的杂酚油吞噬者")
            .desc(d -> d.line("两倍蒸汽产能，如果有钢，几乎没有理由用普通版本！"))
            .task(Task.item(GTMachines.STEAM_LIQUID_BOILER.second()))
            .optional()
            .dependsOn(Q_7BCD4971, GLASS_COPY_4));

    public static final Quest STEAM_EXPANSION = CHAPTER.quest("steam_expansion", q -> q
            .at(-8, -4)
            .side()
            .title("蒸汽扩产")
            .desc(d -> d
                    .line("扩产{蒸汽}能够加速你的资源生产！", YELLOW)
                    .line()
                    .line("在拥有钢之后，{高压锅炉}必然是更优的蒸汽来源，其蒸汽输出为普通版本的{两至三倍}！", GOLD, GOLD))
            .task(Task.item(GTMachines.STEAM_SOLID_BOILER.second()))
            .optional()
            .dependsOn(Q_7BCD4971, STEEL_COPY_1));

    public static final Quest COKE_OVEN_HATCH = CHAPTER.quest("coke_oven_hatch", q -> q
            .at(2, -6)
            .side()
            .subtitle("我们需要自动化！")
            .desc(d -> d
                    .line("焦炉仓可以同时作为输入和输出，它{不会}自动抽取物品，但可以{自动从输出口输出}物品和流体", YELLOW, YELLOW)
                    .line()
                    .line("所以实现自动化你可能需要一个漏斗")
                    .line()
                    .line("焦炉仓与主方块共享内容物"))
            .task(Task.item(GTMachines.COKE_OVEN_HATCH))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .dependsOn(COKE_OVEN_BRICK));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NeolithicChapter.BRONZE_BRICK_HULL, 0, 0))
                .mainLine(NeolithicChapter.BRONZE_BRICK_HULL, GLASS_DUST, SOLID_BOILER, ALLOY_SMELTER, BLAST_FURNACE, COMPRESSOR, PRIMITIVE_BLAST_FURNACE, STEEL, ANCIENT_REACTOR_CORE, ANCIENT_WORLD, STEAM_EXTRACTOR, RUBBER_PLATE, ULV_HULL));
    }
}
