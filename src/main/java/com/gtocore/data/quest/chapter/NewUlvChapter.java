package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewUlvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_ulv", c -> c
            .title("3. 超低压时代(ULV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.ULV]));

    public static final Quest MOTOR = CHAPTER.quest("motor", q -> q
            .at(14, -4)
            .title("ULV 马达")
            .subtitle("你还是多造点吧")
            .desc(d -> d
                    .line("{电动马达}是各个时代发展生产力{必须大量生产}的部件，所以最好一次做上个一组或半组！", GRAY, DARK_RED)
                    .line()
                    .line("超低压的时代的六件套主要需要{铜}和{锡}来制造，也是蒸汽时代的主要材料的延续", link(OresChapter.COPPER_VEIN), link(OresChapter.TIN_ORE_VEIN)))
            .task(Task.item(GTOItems.ULV_ELECTRIC_MOTOR, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink MOTOR_COPY_1 = CHAPTER.copy(MOTOR, 18, 8);

    public static final QuestLink MOTOR_COPY_2 = CHAPTER.copy(MOTOR, 10, 10);

    public static final QuestLink MOTOR_COPY_3 = CHAPTER.copy(MOTOR, 4, 10);

    public static final QuestLink MOTOR_COPY_4 = CHAPTER.copy(MOTOR, 8, 16);

    public static final Quest VACUUM_PUMP = CHAPTER.quest("vacuum_pump", q -> q
            .at(10, -4)
            .title("低压蒸汽真空泵")
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
            .scaledReward(GTOItems.RAW_VACUUM_TUBE, 1)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink VACUUM_PUMP_COPY_1 = CHAPTER.copy(VACUUM_PUMP, 10, 2);

    public static final Quest VACUUM_PUMP_PRODUCT = CHAPTER.quest("vacuum_pump_product", q -> q
            .at(10, -2)
            .title("巨型青铜管道")
            .desc(d -> d
                    .line("为了让泵能够工作，你需要放入一个{抽取管道}，等待其多次抽取真空直到真空等级大于0", YELLOW)
                    .line()
                    .line("低压和高压蒸汽泵使用{巨型青铜管道}并输入蒸汽", GOLD)
                    .line()
                    .line("用电的真空泵需要放置对应电压的{流体校准器}", GOLD))
            .task(Task.item(TagPrefix.pipeHugeFluid, GTMaterials.Bronze))
            .scaledReward(GTOItems.RAW_VACUUM_TUBE, 1)
            .dependsOn(VACUUM_PUMP));

    public static final Quest VACUUM_TUBE = CHAPTER.quest("vacuum_tube", q -> q
            .at(10, 0)
            .milestone()
            .title("真空管")
            .subtitle("嘬嘬嘬")
            .desc(d -> d
                    .line("棒极了，这次不用史蒂夫的嘴")
                    .line()
                    .line("手持{粗真空管}潜行右击{真空泵}制造{真空管}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{做出原始组装机后，你就可以直接在组装机里制造真空管了}", GRAY.italic())
                    .line()
                    .line("显然，你目前只能选择产量最低的配方"))
            .task(Task.item(GTItems.VACUUM_TUBE, 4))
            .scaledReward(GTItems.VACUUM_TUBE, 1)
            .dependsOn(VACUUM_PUMP_PRODUCT));

    public static final Quest THERMAL_GENERATOR = CHAPTER.quest("thermal_generator", q -> q
            .at(12, -2)
            .title("ULV 热力发电机")
            .subtitle("以热生电")
            .desc(d -> d
                    .line("顾名思义，烧东西来发电。")
                    .line()
                    .line("{本质上就是一个小型的火力发电机}", GRAY.italic()))
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.ULV]))
            .scaledReward(Items.COAL, 8)
            .dependsOn(MOTOR, VACUUM_TUBE));

    public static final Quest PISTON = CHAPTER.quest("piston", q -> q
            .at(14, -2)
            .title("ULV 活塞")
            .subtitle("通往烦人之路")
            .desc(d -> d
                    .line("活塞是一类重要的中间合成产物，除了合成之外没有别的用途")
                    .line()
                    .line("不过我猜你会喜欢由它合成的半流质发电机"))
            .task(Task.item(GTOItems.ULV_ELECTRIC_PISTON, 2))
            .scaledReward(GTOItems.ULV_ELECTRIC_PISTON, 2)
            .dependsOn(MOTOR));

    public static final QuestLink PISTON_COPY_1 = CHAPTER.copy(PISTON, 4, 16);

    public static final Quest ROBOT_ARM = CHAPTER.quest("robot_arm", q -> q
            .at(14, 0)
            .milestone()
            .title("ULV 机械臂")
            .subtitle("速度还不如手搬呢！")
            .desc(d -> d.line("机械臂的合成就复杂很多了，所以前期的其他工作一定要处理好"))
            .task(Task.item(GTOItems.ULV_ROBOT_ARM, 2))
            .scaledReward(GTOItems.ULV_ROBOT_ARM, 2)
            .dependsOn(PISTON));

    public static final Quest CONVEYOR = CHAPTER.quest("conveyor", q -> q
            .at(18, 6)
            .title("ULV 传送带")
            .subtitle("凎，这东西怎么速度也没我快！")
            .desc(d -> d
                    .line("贴在小机器的面上可以自动拉取物品，但速度实在是堪忧......")
                    .line()
                    .line("以及你需要提升一下你的{橡胶}产量了", GOLD))
            .task(Task.item(GTOItems.ULV_CONVEYOR_MODULE, 2))
            .scaledReward(GTOItems.ULV_CONVEYOR_MODULE, 2)
            .dependsOn(MOTOR_COPY_1));

    public static final QuestLink CONVEYOR_COPY_1 = CHAPTER.copy(CONVEYOR, -2, 2);

    public static final Quest PUMP = CHAPTER.quest("pump", q -> q
            .at(6, 8)
            .title("ULV 泵")
            .subtitle("但这不能抽原油")
            .desc(d -> d
                    .line("在小机器的一个面上放置电动泵，即可作为覆盖板{主动抽取/排出}小机器内的液体", LIGHT_PURPLE)
                    .line()
                    .line("那当然前提是你得有个{容器}紧贴你的小机器对吧", YELLOW)
                    .line()
                    .line("每tick可以操作{32mb}的液体，虽然不多，但也算是一个自动化的方案了", DARK_RED))
            .task(Task.item(GTOItems.ULV_ELECTRIC_PUMP, 3))
            .scaledReward(GTOItems.ULV_ELECTRIC_PUMP, 2)
            .dependsOn(MOTOR_COPY_3));

    public static final QuestLink PUMP_COPY_1 = CHAPTER.copy(PUMP, 14, 16);

    public static final Quest FLUID_REGULATOR = CHAPTER.quest("fluid_regulator", q -> q
            .at(8, 8)
            .title("ULV 流体校准器")
            .subtitle("精确到位")
            .desc(d -> d
                    .line("如果你的小机器的槽位有{多个}液体输入的话，那无疑使用流体校准器是个{更优的选择}", GOLD, GREEN)
                    .line()
                    .line("它的功能包括{精确输入}与{保持输入}，前者可以精确输入的{位置}，后者是{避免输入量过大}挤占其他流体的槽位", AQUA, BLUE, LIGHT_PURPLE, YELLOW))
            .task(Task.item(GTOItems.ULV_FLUID_REGULATOR, 2))
            .scaledReward(GTOItems.ULV_FLUID_REGULATOR, 2)
            .dependsOn(PUMP, VACUUM_TUBE));

    public static final Quest ASSEMBLER = CHAPTER.quest("assembler", q -> q
            .at(16, 4)
            .title("ULV 组装机")
            .subtitle("反正能把它们组装在一起就行了")
            .desc(d -> d
                    .line("看到EMI组装机右下角的{电压等级}了吗？它决定你必须要用什么机器来制作！", YELLOW)
                    .line()
                    .line("很可怜，并没有几台机器支持{ULV}电压...", ULV))
            .task(Task.item(GTOMachines.ULV_ASSEMBLER[GTValues.ULV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 2)
            .dependsOn(ROBOT_ARM, CONVEYOR));

    public static final QuestLink ASSEMBLER_COPY_1 = CHAPTER.copy(ASSEMBLER, 8, 4);

    public static final QuestLink ASSEMBLER_COPY_2 = CHAPTER.copy(ASSEMBLER, 10, 16);

    public static final Quest STEAM_SEPARATOR = CHAPTER.quest("steam_separator", q -> q
            .at(4, 0)
            .milestone()
            .title("蒸汽分离机")
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
            .task(
                    Task.item(MultiBlockA.STEAM_SEPARATOR),
                    Task.item(GTMachines.STEAM_HATCH),
                    Task.item(GTMachines.STEAM_IMPORT_BUS),
                    Task.item(GTMachines.STEAM_EXPORT_BUS),
                    Task.item(GTOMachines.STEAM_FLUID_INPUT_HATCH),
                    Task.item(GTOMachines.STEAM_FLUID_OUTPUT_HATCH, 3),
                    Task.item(GTOMachines.STEAM_VENT_HATCH))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest GLUE = CHAPTER.quest("glue", q -> q
            .at(12, 4)
            .title("胶水")
            .task(Task.item(GTMaterials.Glue.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.STICKY_RESIN, 8)
            .dependsOn(STEAM_SEPARATOR));

    public static final QuestLink GLUE_COPY_1 = CHAPTER.copy(GLUE, 10, 8);

    public static final Quest CARBON = CHAPTER.quest("carbon", q -> q
            .at(16, 6)
            .title("碳粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Carbon, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.DarkAsh, 4)
            .dependsOn(STEAM_SEPARATOR));

    public static final Quest RESISTOR = CHAPTER.quest("resistor", q -> q
            .at(14, 6)
            .milestone()
            .title("电阻")
            .subtitle("你接触的第一个电子元件")
            .desc(d -> d
                    .line("仔细翻看EMI，不要遗漏它的两个配方！")
                    .line()
                    .line("你现在做不出退火铜，所以你必须使用{铜导线}", GOLD)
                    .line()
                    .line("目前获取碳粉的途径有两个：处理{滑石/石墨矿}，或者离心{黑色灰烬}（炼钢副产物）", GOLD, GOLD))
            .task(Task.item(GTItems.RESISTOR, 3))
            .scaledReward(GTItems.RESISTOR, 1)
            .dependsOn(GLUE, CARBON, ASSEMBLER));

    public static final Quest PACKER = CHAPTER.quest("packer", q -> q
            .at(-2, 4)
            .title("ULV 打包机")
            .subtitle("就是把它们挤在一起")
            .desc(d -> d
                    .line("{打包机}是可以将物品{4合1}或者{9合1}的机器", GOLD, YELLOW, YELLOW)
                    .line()
                    .line("可以将{小粉}打包成{大粉}，也可以用于制作{多重流体管道}", GOLD, GOLD, GOLD)
                    .line()
                    .line("当然目前你需要它来制作{锡线缆}", GOLD))
            .task(Task.item(GTOMachines.ULV_PACKER[GTValues.ULV]))
            .scaledReward(TagPrefix.ingot, GTMaterials.Tin, 4)
            .dependsOn(RESISTOR, ROBOT_ARM, CONVEYOR_COPY_1));

    public static final Quest TIN_CABLE = CHAPTER.quest("tin_cable", q -> q
            .at(0, 4)
            .title("锡线缆")
            .subtitle("这可不能手搓")
            .desc(d -> d
                    .line("将非超导的导线制成{线缆}可以让你不会因为触电而受到伤害，并且{减少了线损}", GOLD, YELLOW)
                    .line()
                    .line("但依然不能突破最大电压或电流，否则会导致导线{融毁}", RED))
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Tin, 6))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 1)
            .dependsOn(PACKER));

    public static final QuestLink TIN_CABLE_COPY_1 = CHAPTER.copy(TIN_CABLE, -2, 6);

    public static final QuestLink TIN_CABLE_COPY_2 = CHAPTER.copy(TIN_CABLE, 16, 14);

    public static final Quest OXYGEN = CHAPTER.quest("oxygen", q -> q
            .at(14, 8)
            .title("氧")
            .subtitle("疯狂吸入")
            .desc(d -> d
                    .line("通风板需要安装在{机器/仓室/流体容器}等的任意一个侧面，在这个侧面不被阻挡的情况下可以以200mB/秒的速度抽入空气", GOLD)
                    .line()
                    .line("{可以安装在多个侧面并行工作，增加抽入速度}", GRAY.italic()))
            .icon(GTMaterials.Oxygen.getBucket())
            .task(Task.item(GTOItems.AIR_VENT), Task.item(GTMaterials.Oxygen.getBucket()))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(STEAM_SEPARATOR));

    public static final Quest CHEMICAL_REACTOR = CHAPTER.quest("chemical_reactor", q -> q
            .at(12, 10)
            .title("ULV 化学反应釜")
            .subtitle("能力有限")
            .desc(d -> d
                    .line("由于电压过低，实际上也只能处理那么几个配方。")
                    .line()
                    .line("聚乙烯是你必须要处理的配方。"))
            .task(Task.item(GTOMachines.ULV_CHEMICAL_REACTOR[GTValues.ULV]))
            .scaledReward(Items.OAK_PLANKS, 4)
            .dependsOn(MOTOR_COPY_2));

    public static final QuestLink CHEMICAL_REACTOR_COPY_1 = CHAPTER.copy(CHEMICAL_REACTOR, 18, 0);

    public static final QuestLink CHEMICAL_REACTOR_COPY_2 = CHAPTER.copy(CHEMICAL_REACTOR, 4, 14);

    public static final Quest COATED_BOARD = CHAPTER.quest("coated_board", q -> q
            .at(12, 8)
            .title("覆膜板")
            .subtitle("上面会铭刻什么呢")
            .desc(d -> d.line("它的配方速度太慢了，你需要多准备{多几个}化学反应釜", DARK_PURPLE))
            .task(Task.item(GTItems.COATED_BOARD))
            .scaledReward(GTItems.COATED_BOARD, 1)
            .dependsOn(CHEMICAL_REACTOR, OXYGEN));

    public static final Quest HP_VACUUM_PUMP = CHAPTER.quest("hp_vacuum_pump", q -> q
            .at(10, 4)
            .title("高压蒸汽真空泵")
            .subtitle("喜欢贴贴")
            .desc(d -> d
                    .line("{2级}真空需要这个这个家伙，而为了让机器获得真空等级则需要让真空泵{紧贴}机器。", AQUA, DARK_RED)
                    .line()
                    .line("{注意：真空泵不能位于机器的正上方或正下方}", YELLOW))
            .task(Task.item(GTOMachines.STEAM_VACUUM_PUMP.second()))
            .scaledReward(TagPrefix.foil, GTMaterials.Copper, 4)
            .dependsOn(VACUUM_PUMP_COPY_1));

    public static final Quest CIRCUIT_BOARD = CHAPTER.quest("circuit_board", q -> q
            .at(10, 6)
            .milestone()
            .title("覆膜印刷电路基板")
            .subtitle("准备强力写入！")
            .desc(d -> d
                    .line("制造这个宝贝需要你的组装机达到{真空2级}的条件", DARK_RED)
                    .line()
                    .line("希望你已经准备好了{高压蒸汽真空泵}", GOLD))
            .task(Task.item(GTItems.BASIC_CIRCUIT_BOARD))
            .scaledReward(GTItems.VACUUM_TUBE, 2)
            .dependsOn(COATED_BOARD, GLUE_COPY_1, ASSEMBLER_COPY_1, HP_VACUUM_PUMP));

    public static final Quest LV_CIRCUIT = CHAPTER.quest("lv_circuit", q -> q
            .at(2, 6)
            .milestone()
            .title("基础电子电路")
            .subtitle("你的第一块电路板")
            .desc(d -> d
                    .line("恭喜！你终于用上了{电路板}！", GRAY)
                    .line()
                    .line("一般来说新一级电压带来的{新电路板}都会比上一级电压的电路板{便宜}，记得做好更新迭代", YELLOW, AQUA))
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_LV, 4))
            .scaledReward(GTItems.ELECTRONIC_CIRCUIT_LV, 1)
            .dependsOn(TIN_CABLE, CIRCUIT_BOARD, VACUUM_TUBE, RESISTOR));

    public static final Quest HEATER = CHAPTER.quest("heater", q -> q
            .at(-2, 14)
            .title("加热器")
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
                    .line("{产能远超同时期机器，但是如何提供燃料以及如何控制启停是个问题}", GRAY.italic())
                    .line()
                    .line("为了拓展机制，GTO加入了一种新的能量系统：{热量（HU）}！", AQUA)
                    .line()
                    .line("标着{需要外部热源}的配方，你需要给工作机器供给热量，配方才会运行", YELLOW)
                    .line()
                    .line("你可以选择直接把机器和{加热器}的顶部贴在一起，也可以使用{热管}传输热量", GOLD, GOLD)
                    .line()
                    .line("机器如果承受了{高于上限}的温度会直接{爆炸}，{热量探测覆盖版}可将机器温度转化为{红石信号}输出", LIGHT_PURPLE, RED, GOLD, GREEN)
                    .line()
                    .line("还有一些机器拥有和热量相关联的特殊机制（如{原始蒸馏塔}）", GOLD)
                    .line()
                    .line("{制造一台加热器以完成此任务}", YELLOW)
                    .page()
                    .line("{热容}：温度 = 热量 / 热容，热容越高，机器越不容易升温", GOLD)
                    .line()
                    .line("{传热速度}：机器与相邻机器交换热量的能力，ΔT 是两者温差。温差越大，每 tick 传递的热量越多", GOLD)
                    .line()
                    .line("{自然冷却速度}：表示机器自行向环境散热的能力，√ΔT 是机器与环境之间的温差的平方根", GOLD))
            .task(Task.item(GTOMachines.HEATER, 2))
            .scaledReward(Items.COAL, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink HEATER_COPY_1 = CHAPTER.copy(HEATER, 8, 10);

    public static final Quest LARGE_BRONZE_BOILER = CHAPTER.quest("large_bronze_boiler", q -> q
            .at(0, 6)
            .milestone()
            .title("大型青铜锅炉")
            .subtitle("那些产蒸汽的大家伙")
            .desc(d -> d
                    .line("它是ULV时代的产物，LV时代前最强大的蒸汽来源，如果你没有接受《清洁能源挑战》，那么不妨为你的{大型蒸汽机器们}制造一台，几乎找不到缺点", GOLD)
                    .line()
                    .line("更强的锅炉，蒸汽产出是单方块锅炉的几十倍")
                    .line()
                    .line("请务必在预热阶段就加入水")
                    .line()
                    .line("除了生产大量蒸汽，青铜锅炉还能用来生产{水煤气}", GOLD))
            .task(Task.item(GTMultiMachines.LARGE_BOILER_BRONZE))
            .scaledReward(Items.COAL, 8)
            .dependsOn(TIN_CABLE_COPY_1, LV_CIRCUIT));

    public static final Quest ALCHEMY_CAULDRON = CHAPTER.quest("alchemy_cauldron", q -> q
            .at(-4, 12)
            .title("炼金锅")
            .subtitle("不要用它来做饭")
            .desc(d -> d
                    .line("放一个加热器来让其升温")
                    .line()
                    .line("多个加热器可以更快的升温，小心{过热爆炸}！", DARK_RED))
            .task(Task.item(ManaMachine.ALCHEMY_CAULDRON))
            .scaledReward(TagPrefix.dust, GTMaterials.Coal, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest COAL_SLURRY = CHAPTER.quest("coal_slurry", q -> q
            .at(-2, 12)
            .title("煤浆")
            .subtitle("注意防爆")
            .desc(d -> d
                    .line("那么让我告诉你一些自动化生产的关键：")
                    .line()
                    .line("{加热器}的正面是不能被阻挡的，一旦被阻挡了那就会{中断加热}，所以我们可以利用这个机制控制加热与否", GOLD, YELLOW)
                    .line()
                    .line("其次，在安装{热量探测覆盖版}后，加热器在768K的时候会发出14强度的红石信号，那么我们可以利用一个{红石比较器}来判断温度是否符合", GOLD, RED)
                    .line()
                    .line("若温度过高则在加热器面前放置一个方块防止加热器过热，温度下降到合适的区间之后将该方块移走，这个活可以交给{粘性活塞}", GREEN)
                    .line()
                    .line("最后的最后：温度一到达800K就会{瞬间爆炸}", DARK_RED))
            .task(Task.item(GTOMaterials.CoalSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Coal, 4)
            .dependsOn(ALCHEMY_CAULDRON, HEATER));

    public static final Quest WATER_GAS = CHAPTER.quest("water_gas", q -> q
            .at(0, 12)
            .milestone()
            .title("水煤气")
            .subtitle("青铜锅炉的第一件产物")
            .desc(d -> d
                    .line("水煤气需要在{1000K}以上发生反应", GREEN)
                    .line()
                    .line("你需要将锅炉提前预热")
                    .line()
                    .line("{在处理水煤气的时候请保持锅炉干烧（即不加入水），锅炉此时不会产出蒸汽}", YELLOW))
            .task(Task.item(GTOMaterials.WaterGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.COAL, 4)
            .dependsOn(LARGE_BRONZE_BOILER, COAL_SLURRY));

    public static final Quest HYDROGEN = CHAPTER.quest("hydrogen", q -> q
            .at(0, 14)
            .title("氢")
            .subtitle("正氧负氢？")
            .desc(d -> d
                    .line("氢气可以通过离心水煤气制取")
                    .line()
                    .line("什么，你现在还没有搭建{蒸汽分离机}？", GOLD))
            .task(Task.item(ModItems.HYDROGEN_BUCKET.get()).optional(), Task.checkmark())
            .scaledReward(ModItems.STEEL_PLATE.get(), 4)
            .dependsOn(WATER_GAS, STEAM_SEPARATOR));

    public static final Quest HEAT_HATCH = CHAPTER.quest("heat_hatch", q -> q
            .at(8, 14)
            .title("导热仓")
            .task(Task.item(GTOMachines.HEAT_HATCH, 2))
            .scaledReward(ModItems.STEEL_PLATE.get(), 4)
            .dependsOn(ASSEMBLER_COPY_2));

    public static final Quest ULV_FLUID_HATCH = CHAPTER.quest("ulv_fluid_hatch", q -> q
            .at(8, -2)
            .title("ULV 流体仓")
            .task(Task.item(GTMachines.FLUID_IMPORT_HATCH[GTValues.ULV]), Task.item(GTMachines.FLUID_EXPORT_HATCH[GTValues.ULV], 3))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest DISTILLATION_TOWER = CHAPTER.quest("distillation_tower", q -> q
            .at(8, 0)
            .milestone()
            .title("原始蒸馏塔")
            .subtitle("原始蒸馏塔")
            .desc(d -> d
                    .line("原始人对蒸馏塔的仿制版本，由于机体材料受限，温度过高会导致内部压力超出容限从而发生{爆炸}", DARK_RED)
                    .line()
                    .line("{原始蒸馏塔}可以加高，最高{13}层。{每一层都输出且}{仅能}{输出一种产物}，如果层数不够，则会{吞掉}靠上的产物", GOLD, GREEN, YELLOW, LIGHT_PURPLE, YELLOW, RED)
                    .line()
                    .line("它还需要安装两个{导热仓}，{底部输入热量，顶部排出热量}。底部温度越高，顶部温度越低，蒸馏塔的工作效率也就越高", GOLD, YELLOW)
                    .line()
                    .line("目前你还只能使用{加热器}来加热，使用{冷却器}来散热。进入LV之后你可以使用更高效的{电力加热器}", GOLD, GOLD, GOLD)
                    .line()
                    .line("{注意：最下面那一层是输出物品的，流体从第二层开始输出}", GRAY.italic()))
            .task(Task.item(MultiBlockC.PRIMITIVE_DISTILLATION_TOWER))
            .scaledReward(ModItems.STEEL_PLATE.get(), 8)
            .dependsOn(ULV_FLUID_HATCH));

    public static final Quest RAW_OIL = CHAPTER.quest("raw_oil", q -> q
            .at(0, 16)
            .title("原油")
            .subtitle("是美军吸引器")
            .desc(d -> d
                    .line("恭喜你遇到了原油")
                    .line()
                    .line("拿桶舀一点吧，现在可能用不上但后面会需要用到的")
                    .line()
                    .line("等到了LV阶段就可以用基础泵来抽取原油泉了——你会需要原油并搭建石化产线")
                    .line()
                    .line("这种黑色的液体会生成在地表以及地下，粘稠状，掉入其中就像掉到水里一样会扣减氧气")
                    .line()
                    .line("一桶只能舀走{1B}，所以如果你想一次性带走更多原油就需要更大的单元，如{钢单元}可以一次性装{8B}的单一流体；而{铝单元}可以装{32B}流体，不过你现在还没有{铝}，你在{下一个阶段}才能见到它……", AQUA, GOLD, AQUA, GOLD, AQUA, GOLD, YELLOW)
                    .line()
                    .line("不过幸好我这里有，送你一个！"))
            .task(Task.item(GTMaterials.RawOil.getBucket()))
            .scaledReward(Items.BUCKET, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest SULFURIC_NAPHTHA = CHAPTER.quest("sulfuric_naphtha", q -> q
            .at(2, 16)
            .title("含硫石脑油")
            .subtitle("小小的一步")
            .desc(d -> d.line("按照EMI的配方产出，即会产出于{第三层}", LIGHT_PURPLE))
            .task(Task.item(GTMaterials.SulfuricNaphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RawOil.getBucket(), 1)
            .dependsOn(RAW_OIL, DISTILLATION_TOWER));

    public static final Quest NAPHTHA = CHAPTER.quest("naphtha", q -> q
            .at(2, 14)
            .title("石脑油")
            .subtitle("副产不要丢，可以做硫酸")
            .desc(d -> d.line("目前只能使用化学反应釜，在HV能做{脱硫机}来进行脱硫", GOLD))
            .task(Task.item(GTMaterials.Naphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 1)
            .dependsOn(SULFURIC_NAPHTHA, HYDROGEN, CHEMICAL_REACTOR_COPY_2));

    public static final QuestLink NAPHTHA_COPY_1 = CHAPTER.copy(NAPHTHA, 6, 2);

    public static final Quest STEAM_CRACKER = CHAPTER.quest("steam_cracker", q -> q
            .at(6, 6)
            .milestone()
            .title("蒸汽裂化机")
            .subtitle("似曾相识故人来")
            .desc(d -> d
                    .line("蒸汽裂化机只能处理石化四种主要的中间产物（{石脑油}，{炼油气}，{轻燃油}，{重燃油}）", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("产量{偏低且慢}，在{LV}能制作{基础化学反应釜}之后可以换用反应釜配方获得50%的原料效率", RED, LV, AQUA)
                    .line()
                    .line("在{HV}能做{裂化机}之后可以获得100%的原料效率", HV, AQUA))
            .task(Task.item(MultiBlockC.STEAM_CRACKER))
            .scaledReward(Items.COAL, 8)
            .dependsOn(FLUID_REGULATOR));

    public static final Quest CRACKED_NAPHTHA = CHAPTER.quest("cracked_naphtha", q -> q
            .at(4, 12)
            .milestone()
            .title("重度蒸汽裂化石脑油")
            .task(Task.item(GTMaterials.SeverelySteamCrackedNaphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Naphtha.getBucket(), 1)
            .dependsOn(STEAM_CRACKER, NAPHTHA));

    public static final Quest ETHYLENE = CHAPTER.quest("ethylene", q -> q
            .at(6, 10)
            .title("乙烯")
            .subtitle("嗨呀，真的是费劲")
            .desc(d -> d
                    .line("当然蒸馏塔的使用需要{严格遵循}EMI的配方指引", DARK_PURPLE)
                    .line()
                    .line("如果运行的配方有{11}种产出，则需要搭建{12}层高的蒸馏塔(第一层只能放置输出总线输出固体产物)", GREEN, GREEN)
                    .line()
                    .line("以及还是那个问题，注意别让原始蒸馏塔{爆炸了}", DARK_RED))
            .task(Task.item(GTMaterials.Ethylene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 1)
            .dependsOn(CRACKED_NAPHTHA, DISTILLATION_TOWER));

    public static final Quest LATHE = CHAPTER.quest("lathe", q -> q
            .at(6, 16)
            .title("ULV 车床")
            .subtitle("电动矬子")
            .desc(d -> d
                    .line("{车床}是可以将金属锭直接制作为{杆}（产量比手搓的翻倍哦！）、将螺栓制作为{螺丝}、将齿轮制作为{锯片}的机器", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("由于电压过低，目前你还无法加工{钢}", GOLD)
                    .line()
                    .line("{我讨厌锉子！}", GRAY.italic()))
            .task(Task.item(GTOMachines.ULV_LATHE[GTValues.ULV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Ruby, 1)
            .dependsOn(MOTOR_COPY_4, PISTON_COPY_1));

    public static final Quest RUBY_ROD = CHAPTER.quest("ruby_rod", q -> q
            .at(6, 14)
            .title("红宝石杆")
            .subtitle("本质上是催化剂？")
            .desc(d -> d
                    .line("红宝石杆能作为聚合反应的催化剂以参与聚乙烯的合成")
                    .line()
                    .line("以及这东西能合成搋子，可以清除机器内残留的液体。")
                    .line()
                    .line("{但既然任何一种*杆都能合成搋子，为什么要用红宝石的呢？}", DARK_GRAY.italic()))
            .task(Task.item(TagPrefix.rod, GTMaterials.Ruby))
            .scaledReward(TagPrefix.rod, GTMaterials.Steel, 2)
            .dependsOn(LATHE));

    public static final Quest POLYETHYLENE = CHAPTER.quest("polyethylene", q -> q
            .at(8, 12)
            .milestone()
            .title("聚乙烯")
            .subtitle("不要尝试去测定它的分子量")
            .desc(d -> d
                    .line("聚乙烯是唯一能在{化学反应釜}合成的简单有机聚合物，更高级的聚合物都需要在专门的{聚合器}完成反应", GOLD, GOLD)
                    .line()
                    .line("在此阶段，你需要放一个{加热器}来提供外部热源，并且只能在{红宝石杆}的催化下进行{空气}聚合", GOLD, GOLD, GOLD)
                    .line()
                    .line("在{Jade 提示框HUD}上{没有温度属性}的机器，需要额外贴一个{导热接口}来赋予其温度属性，且导热接口需要贴在加热的那一面", AQUA, RED, GOLD))
            .task(Task.item(GTOItems.HEAT_INTERFACE), Task.item(GTMaterials.Polyethylene.getBucket()))
            .scaledReward(ModItems.STEEL_PLATE.get(), 2)
            .dependsOn(ETHYLENE, RUBY_ROD, HEAT_HATCH, HEATER_COPY_1));

    public static final Quest FLUID_SOLIDIFIER = CHAPTER.quest("fluid_solidifier", q -> q
            .at(12, 16)
            .title("ULV 流体固化器")
            .subtitle("让它冷却下来")
            .desc(d -> d
                    .line("{固化机}是可以将液体重塑为固体的机器", GOLD)
                    .line()
                    .line("同样的问题，电压过低，所以就处理几个关键配方就行了")
                    .line()
                    .line("你需要制作聚乙烯片"))
            .task(Task.item(GTOMachines.ULV_FLUID_SOLIDIFIER[GTValues.ULV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 1)
            .dependsOn(PUMP_COPY_1));

    public static final Quest POLYETHYLENE_PLATE = CHAPTER.quest("polyethylene_plate", q -> q
            .at(12, 14)
            .title("聚乙烯板")
            .subtitle("千辛万苦")
            .desc(d -> d.line("在超低压时代先造一些凑活用点，后面电压上去了一定要换其他的机器来制作它"))
            .task(Task.item(TagPrefix.plate, GTMaterials.Polyethylene, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Polyethylene, 2)
            .dependsOn(FLUID_SOLIDIFIER, POLYETHYLENE));

    public static final Quest LV_CASING = CHAPTER.quest("lv_casing", q -> q
            .at(14, 10)
            .title("LV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_LV))
            .scaledReward(ModItems.STEEL_PLATE.get(), 2)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest LV_HULL = CHAPTER.quest("lv_hull", q -> q
            .at(14, 12)
            .milestone()
            .title("LV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", GRAY)
            .desc(d -> d
                    .line("这一阶段你将会解锁更多的机器类型！我想你并不喜欢一直用工具{低效率}地制作部件吧！", GRAY)
                    .line()
                    .line("从这个阶段开始，你会开始遇到复杂的化工产线，希望你不会被吓到。把{MV级电路}作为本阶段的第一个目标是一个不错的选择", GOLD)
                    .line()
                    .line("以及，如同大多数GT流程一样，你马上就会遇到前期最可怕的吃电大户——{电力高炉}，把它作为目标前进吧！", GREEN))
            .task(Task.item(GTMachines.HULL[GTValues.LV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.LV], 1))
            .dependsOn(LV_CASING, POLYETHYLENE_PLATE, TIN_CABLE_COPY_2));

    public static final Quest ULV_TRANSFORMER_1A = CHAPTER.quest("ulv_transformer_1a", q -> q
            .at(16, 12)
            .side()
            .subtitle("万能的转换器")
            .desc(d -> d
                    .line("你可以用它在本级电压和高一级电压之间相互转换")
                    .line()
                    .line("使用{螺丝刀}来改变它的工作方式", GOLD)
                    .line()
                    .line("其中的一个面会与其他的五个面看上去不同，此面为输出端，其余面均为输入端"))
            .task(Task.item(GTMachines.TRANSFORMER[GTValues.ULV]))
            .optional()
            .dependsOn(LV_HULL));

    public static final Quest PULSATING_ALLOY_SUPERCONDUCTOR_BASE = CHAPTER.quest("pulsating_alloy_superconductor_base", q -> q
            .at(2, -2)
            .side()
            .subtitle("怎么是粗胚?")
            .desc(d -> d
                    .line("这些用于制作超导体的{魔法合金}可以让你在每个电压的初期就能用上{超导体}，仅仅是多了一步额外的处理步骤：{魔力灌注}", GOLD, GOLD, AQUA)
                    .line()
                    .line("如果你想在之后的阶段快速用到{超导体}，你需要开始发展{魔法线}，并拥有至少一个{魔力池}", GOLD, GREEN, GOLD))
            .task(Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.PulsatingAlloy))
            .optional()
            .dependsOn(NewSteamChapter.PULSATING_ALLOY, NewSteamChapter.ULV_HULL));

    public static final Quest THE_POWER_OF_MAGIC = CHAPTER.quest("the_power_of_magic", q -> q
            .at(4, -2)
            .side()
            .title("魔法的力量")
            .subtitle("弹弹弹，弹走大电阻")
            .desc(d -> d
                    .line("如果说你有满满{一池魔力池}的话，你能造{很多的}{脉冲合金导线}，不骗你，是真的", AQUA, LIGHT_PURPLE, GREEN)
                    .line()
                    .line("前期可以直接用{火红莲}生产一些魔力，可以配合{悬浮沙漏}来自动做好魔力的生产", DARK_PURPLE, GOLD))
            .task(Task.item(TagPrefix.wireGtSingle, GTOMaterials.PulsatingAlloy))
            .optional()
            .dependsOn(PULSATING_ALLOY_SUPERCONDUCTOR_BASE));

    public static final Quest ULV_BATTERY_BUFFER_4X = CHAPTER.quest("ulv_battery_buffer_4x", q -> q
            .at(-2, 0)
            .side()
            .subtitle("你的应急能源")
            .desc(d -> d
                    .line("{电池箱}能够为你的电网提供{缓存}", GOLD, YELLOW)
                    .line()
                    .line("虽然你已经可以制作电池箱，但{电池}你还{无法生产}", GOLD, RED)
                    .line()
                    .line("不过在这个阶段，你大概也没有蓄电需求？如果你真的需要蓄电，做一个电池箱会送你一个电容")
                    .line()
                    .line("{【TODO：超级电容将在不久的版本后移动到商店】}", GRAY))
            .task(Task.item(GTMachines.BATTERY_BUFFER_4[GTValues.ULV]))
            .reward(Reward.item(GTOItems.SUPER_CAPACITOR))
            .optional()
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest THIS_IS_A_WIND_TURBINE = CHAPTER.quest("this_is_a_wind_turbine", q -> q
            .at(12, -4)
            .side()
            .title("这是大风车")
            .subtitle("清洁能源")
            .desc(d -> d
                    .line("风力跟高度（Y值）直接相关，在Y值低于0的地方是没有风力来发电的")
                    .line()
                    .line("转子前不能有任何遮挡，包括生物实体和方块")
                    .line()
                    .line("最好与正在转动的扇叶保持距离"))
            .task(Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.ULV]))
            .reward(Reward.item(GTOItems.WOOD_ROTOR))
            .optional()
            .dependsOn(THERMAL_GENERATOR));

    public static final Quest CONDUCTORS_START_PRODUCTION = CHAPTER.quest("conductors_start_production", q -> q
            .at(16, -4)
            .side()
            .title("导线，开始生产！")
            .subtitle("保护你的剪线钳")
            .desc(d -> d
                    .line("{线材轧机}是可以将金属锭直接制作为{导线}的机器", GOLD, GOLD)
                    .line()
                    .line("相比徒手制作导线 ，材料产出率整整提高了{2-3倍}之多", AQUA)
                    .line()
                    .line("或许是你应该做的第一台ULV机器？")
                    .line()
                    .line("由于电压过低，目前你还无法加工{铜}", GOLD))
            .task(Task.item(GTOMachines.ULV_WIREMILL[GTValues.ULV]))
            .reward(Reward.item(Items.PUMPKIN_PIE))
            .dependsOn(MOTOR));

    public static final Quest THIS_IS_AN_INTERNAL_COMBUSTION_ENGINE = CHAPTER.quest("this_is_an_internal_combustion_engine", q -> q
            .at(16, -2)
            .side()
            .title("这是内燃机")
            .subtitle("实际上什么油都能烧")
            .desc(d -> d
                    .line("另一种发电方式，可以让你直接使用石化燃料发电。")
                    .line()
                    .line("产能略微比原始热力发电机大。"))
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.CABBAGE_ROLLS))
            .optional()
            .dependsOn(PISTON));

    public static final Quest THEN_THAT_IS_YOUR_FIRST_CIRCUIT = CHAPTER.quest("then_that_is_your_first_circuit", q -> q
            .at(18, 2)
            .side()
            .icon(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LV])
            .title("那么，这是你的第一个电路")
            .subtitle("准备好了吗？")
            .desc(d -> d
                    .line("现在你可以开始着手制作第一个电路")
                    .line()
                    .line("你需要准备三种原料：真空管、电阻和覆膜印刷电路基板"))
            .task(Task.checkmark().title("电路"))
            .dependsOn(ASSEMBLER, CHEMICAL_REACTOR_COPY_1));

    public static final Quest MAKE_WIRES_ALL_IN_ONE = CHAPTER.quest("make_wires_all_in_one", q -> q
            .at(18, -4)
            .side()
            .title("让导线多合一")
            .subtitle("但并不能织布")
            .desc(d -> d
                    .line("{织布机}是可以将导线制作为多重导线的机器", GOLD)
                    .line()
                    .line("比压缩机效率更高，后续的高A导线都必须使用织布机合成"))
            .task(Task.item(GTOMachines.ULV_LOOM[GTValues.ULV]))
            .dependsOn(CONDUCTORS_START_PRODUCTION));

    public static final Quest ULV_CHARGER_4X = CHAPTER.quest("ulv_charger_4x", q -> q
            .at(0, -2)
            .side()
            .desc(d -> d.line("用于给需要充电的工具或者装备{快速充电}，比用{电池箱}充电快", YELLOW, GOLD))
            .task(Task.item(GTMachines.CHARGER_4[GTValues.ULV]))
            .optional()
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest ULV_COMPONENT_SUBSIDY = CHAPTER.quest("ulv_component_subsidy", q -> q
            .at(18, 4)
            .side()
            .icon(GTOItems.ULV_ELECTRIC_MOTOR)
            .title("ULV部件补助")
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(GTOItems.ULV_ELECTRIC_MOTOR, 16),
                    Reward.item(GTOItems.ULV_CONVEYOR_MODULE, 4),
                    Reward.item(GTOItems.ULV_ELECTRIC_PISTON, 4),
                    Reward.item(GTOItems.ULV_ROBOT_ARM, 4),
                    Reward.item(GTOItems.ULV_ELECTRIC_PUMP, 2))
            .normalSubsidy(
                    Reward.item(GTOItems.ULV_ELECTRIC_MOTOR, 8),
                    Reward.item(GTOItems.ULV_CONVEYOR_MODULE, 2),
                    Reward.item(GTOItems.ULV_ELECTRIC_PISTON, 2),
                    Reward.item(GTOItems.ULV_ROBOT_ARM, 2),
                    Reward.item(GTOItems.ULV_ELECTRIC_PUMP, 2))
            .optional().hideUntilDepsVisible()
            .dependsOn(ASSEMBLER));

    public static final Quest BUILD_A_CEILING_FOR_YOUR_PRODUCTION = CHAPTER.quest("build_a_ceiling_for_your_production", q -> q
            .at(0, 2)
            .side()
            .title("给你的产线搭个天花板{（专家）}", LIGHT_PURPLE)
            .subtitle("机器淋雨会爆炸！")
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(UtilChapter.Q_1FB1EC5B, NewSteamChapter.ULV_HULL));

    public static final Quest OXYGEN_BUCKET = CHAPTER.quest("oxygen_bucket", q -> q
            .at(16, 10)
            .side()
            .icon(ModItems.OXYGEN_BUCKET.get())
            .subtitle("你活着都靠它")
            .desc(d -> d
                    .line("如果你还没有造{蒸汽分离机}，是时候造一台了", GOLD)
                    .line()
                    .line("离心{空气}获取氧气", GOLD)
                    .line()
                    .line("{提示：你需要安装至少两个蒸汽流体输出仓才可以执行离心空气的配方（因为这个配方有两个流体产物）}", GRAY.italic()))
            .task(Task.item(ModItems.OXYGEN_BUCKET.get()).optional(), Task.checkmark())
            .dependsOn(OXYGEN));

    public static final Quest DEVELOPMENT_AND_ENERGY = CHAPTER.quest("development_and_energy", q -> q
            .at(2, 8)
            .side()
            .icon(QuestExternalRefs.ImmersiveAircraftItems.INDUSTRIAL_GEARS)
            .title("发展与能源")
            .subtitle("发展的核心")
            .desc(d -> d
                    .line("与常规GregTech流程不同的是，为了进入{LV}阶段，在超低压阶段你就需要制作塑料片（{聚乙烯板}）来合成{LV机器外壳}", LV, GOLD, GOLD)
                    .line()
                    .line("因此，你现在需要搭建一条{原始}的石化产线来获取乙烯。这对于仍在{ULV}阶段的你是一个不小的考验", GRAY, ULV)
                    .line()
                    .line("{在接下来的任务中你会看到}{“检查标记”}{按钮，在你按下这个按钮之后就可以进行下一步的任务了}", YELLOW, GREEN, YELLOW)
                    .line()
                    .line("{避免出现为了一桶产物倒回去重新运行一次配方的尴尬}", DARK_GRAY.strike()))
            .task(Task.checkmark().title("化工、能源"))
            .dependsOn(LV_CIRCUIT));

    public static final Quest INFINITE_INTAKE_HATCH = CHAPTER.quest("infinite_intake_hatch", q -> q
            .at(16, 8)
            .side()
            .subtitle("更大马力,更大容量")
            .desc(d -> d
                    .line("可以往多方块里面塞{无限}的空气参与合成", DARK_RED)
                    .line()
                    .line("当然，这些空气是{无法}被拿出来的", DARK_RED))
            .task(Task.item(GTOMachines.INFINITE_INTAKE_HATCH))
            .dependsOn(OXYGEN));

    public static final Quest DIGITAL_MINER = CHAPTER.quest("digital_miner", q -> q
            .at(4, 4)
            .side()
            .subtitle("解放双手")
            .desc(d -> d
                    .line("仍然只能挖掘实体矿脉，机器的电压等级取整体框架和能源仓提供电压的最小值")
                    .line()
                    .line("如果觉得造价太高可以先用单方块矿机，等你产能上来了再做")
                    .line()
                    .line("{LV阶段可以解锁用电的单方块矿机，速度比蒸汽矿机快了不少，至少能用了}", GRAY.italic()))
            .task(Task.item(MultiBlockA.DIGITAL_MINER))
            .dependsOn(LV_CIRCUIT));

    public static final Quest PROGRAMMED_CIRCUIT = CHAPTER.quest("programmed_circuit", q -> q
            .at(4, 8)
            .side()
            .desc(d -> d
                    .line("神奇的电路，主要用于控制配方合成。")
                    .line()
                    .line("从ULV开始的输入仓和总线都自带一个编程电路槽，只需要点击就可以调整电路。只有蒸汽输入总线需要手动制作电路放入。")
                    .line()
                    .line("用电的单方块机器也自带一个编程电路槽。"))
            .task(Task.item(GTItems.PROGRAMMED_CIRCUIT))
            .dependsOn(LV_CIRCUIT));

    public static final Quest CHIP_SUBSIDY = CHAPTER.quest("chip_subsidy", q -> q
            .at(2, 4)
            .side()
            .icon(GTItems.ELECTRONIC_CIRCUIT_LV)
            .title("芯片补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_LV, 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_LV, 6))
            .optional().hideUntilDepsVisible()
            .dependsOn(LV_CIRCUIT));

    public static final Quest THE_REMAINING_THREE_GUYS = CHAPTER.quest("the_remaining_three_guys", q -> q
            .at(8, 2)
            .side()
            .title("剩下的三个家伙")
            .subtitle("并非无用")
            .desc(d -> d.line("以目前的生产力处理与存储这些原油产物是比较麻烦的，可以到AE存储出来后进行处理，不会太久"))
            .task(
                    Task.item(GTMaterials.SulfuricGas.getBucket()).optional(),
                    Task.item(GTMaterials.SulfuricHeavyFuel.getBucket()).optional(),
                    Task.item(GTMaterials.SulfuricLightFuel.getBucket()).optional(),
                    Task.checkmark().disableToast())
            .dependsOn(DISTILLATION_TOWER));

    public static final Quest CRACKER_DIVINE_POWER = CHAPTER.quest("cracker_divine_power", q -> q
            .at(6, 4)
            .side()
            .icon(GTMaterials.SeverelySteamCrackedNaphtha.getBucket())
            .title("裂化机神力")
            .subtitle("蒸汽真厉害")
            .desc(d -> d
                    .line("你可以选择裂化成任意一种")
                    .line()
                    .line("重度蒸汽裂化石脑油分解更彻底，具有更高的产率"))
            .task(
                    Task.item(GTMaterials.LightlySteamCrackedNaphtha.getBucket()).optional(),
                    Task.item(GTMaterials.SeverelySteamCrackedNaphtha.getBucket()).optional(),
                    Task.checkmark())
            .dependsOn(STEAM_CRACKER, NAPHTHA_COPY_1));

    public static final Quest PLASTIC_SUBSIDY = CHAPTER.quest("plastic_subsidy", q -> q
            .at(14, 14)
            .side()
            .icon(TagPrefix.plate, GTMaterials.Polyethylene)
            .title("塑料补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 128L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 24))
            .optional().hideUntilDepsVisible()
            .dependsOn(POLYETHYLENE_PLATE));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewSteamChapter.ULV_HULL, 0, 0))
                .mainLine(NewSteamChapter.ULV_HULL, STEAM_SEPARATOR, DISTILLATION_TOWER, VACUUM_TUBE, ROBOT_ARM, RESISTOR, CIRCUIT_BOARD, STEAM_CRACKER, LV_CIRCUIT, LARGE_BRONZE_BOILER, WATER_GAS, CRACKED_NAPHTHA, POLYETHYLENE, LV_HULL));
    }
}
