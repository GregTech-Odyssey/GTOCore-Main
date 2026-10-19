package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class UlvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("ulv", c -> c
            .title("3. 超低压(ULV)")
            .subtitle("Tier0.5-ULV")
            .icon(MultiBlockC.PRIMITIVE_DISTILLATION_TOWER)
            .hideQuestsUntilDepsVisible()
            .autofocus(UlvChapter.WELCOME_TO_THE_ULTRA_LOW_PRESSURE));

    public static final Quest WHERE_DOES_AIR_COME_FROM = CHAPTER.quest("where_does_air_come_from", q -> q
            .at(-2, 2.85)
            .title("空气从哪来？")
            .subtitle("疯狂吸入")
            .desc(d -> d
                    .line("通风板需要安装在{机器/仓室/流体容器}等的任意一个侧面，在这个侧面不被阻挡的情况下可以以200mB/秒的速度抽入空气", GOLD)
                    .line()
                    .line("{可以安装在多个侧面并行工作，增加抽入速度}", GRAY.italic()))
            .task(Task.item(GTOItems.AIR_VENT))
            .dependsOn(SteamChapter.FIRST_STEEL_INGOT)
            .hideDependentLines());

    public static final Quest BIGGER_AND_STRONGER_VACUUM = CHAPTER.quest("bigger_and_stronger_vacuum", q -> q
            .at(3.5, 2.85)
            .icon(GTOMachines.STEAM_VACUUM_PUMP.second())
            .title("更大更强的真空")
            .subtitle("喜欢贴贴")
            .desc(d -> d
                    .line("{2级}真空需要这个这个家伙，而为了让机器获得真空等级则需要让真空泵{紧贴}机器。", AQUA, DARK_RED)
                    .line()
                    .line("{注意：真空泵不能位于机器的正上方或正下方}", YELLOW))
            .task(Task.item(GTOMachines.STEAM_VACUUM_PUMP.second()), Task.item(TagPrefix.pipeHugeFluid, GTMaterials.Bronze))
            .dependsOn(SteamChapter.FIRST_STEEL_INGOT));

    public static final Quest ULV_TRANSFORMER_1A = CHAPTER.quest("ulv_transformer_1a", q -> q
            .at(17, -5)
            .subtitle("万能的转换器")
            .desc(d -> d
                    .line("你可以用它在本级电压和高一级电压之间相互转换")
                    .line()
                    .line("使用{螺丝刀}来改变它的工作方式", GOLD)
                    .line()
                    .line("其中的一个面会与其他的五个面看上去不同，此面为输出端，其余面均为输入端"))
            .task(Task.item(GTMachines.TRANSFORMER[GTValues.ULV]))
            .dependsOn(LvChapter.WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .optional());

    public static final Quest PULSATING_ALLOY_SUPERCONDUCTOR_BASE = CHAPTER.quest("pulsating_alloy_superconductor_base", q -> q
            .at(-0.5, -8.5)
            .subtitle("怎么是粗胚?")
            .desc(d -> d
                    .line("这些用于制作超导体的{魔法合金}可以让你在每个电压的初期就能用上{超导体}，仅仅是多了一步额外的处理步骤：{魔力灌注}", GOLD, GOLD, AQUA)
                    .line()
                    .line("如果你想在之后的阶段快速用到{超导体}，你需要开始发展{魔法线}，并拥有至少一个{魔力池}", GOLD, GREEN, GOLD))
            .task(Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTOMaterials.PulsatingAlloy))
            .dependsOn(SteamChapter.HUH_YOU_TOO)
            .optional());

    public static final Quest THE_POWER_OF_MAGIC = CHAPTER.quest("the_power_of_magic", q -> q
            .at(1, -8.5)
            .title("魔法的力量")
            .subtitle("弹弹弹，弹走大电阻")
            .desc(d -> d
                    .line("如果说你有满满{一池魔力池}的话，你能造{很多的}{脉冲合金导线}，不骗你，是真的", AQUA, LIGHT_PURPLE, GREEN)
                    .line()
                    .line("前期可以直接用{火红莲}生产一些魔力，可以配合{悬浮沙漏}来自动做好魔力的生产", DARK_PURPLE, GOLD))
            .task(Task.item(TagPrefix.wireGtSingle, GTOMaterials.PulsatingAlloy))
            .dependsOn(Magic1Chapter.MANA, PULSATING_ALLOY_SUPERCONDUCTOR_BASE)
            .optional());

    public static final Quest WELCOME_TO_THE_ULTRA_LOW_PRESSURE = CHAPTER.quest("welcome_to_the_ultra_low_pressure", q -> q
            .at(-2, -6.5)
            .title("欢迎来到{超低压}阶段", GRAY)
            .subtitle("{你需要天马行空般的想象力}", GRAY)
            .desc(d -> d
                    .line("欢迎来到超低压阶段")
                    .line()
                    .line("哪怕是再微弱的电那都是电，所以注意不要被裸露的导线电死了")
                    .line()
                    .line("从现在起，你可以开始着手降低材料的造价，或许将{原始线材轧机}、{原始车床}作为你新时代的开端是不错的选择？", GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.ULV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(SteamChapter.ULV_SUPERCONDUCTOR));

    public static final Quest ULV_BATTERY_BUFFER_4X = CHAPTER.quest("ulv_battery_buffer_4x", q -> q
            .at(-4, -8)
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
            .dependsOn(WELCOME_TO_THE_ULTRA_LOW_PRESSURE)
            .optional());

    public static final Quest SLOWEST_MOTOR = CHAPTER.quest("slowest_motor", q -> q
            .at(-2, -3.5)
            .title("最慢的马达")
            .subtitle("你还是多造点吧")
            .desc(d -> d
                    .line("{电动马达}是各个时代发展生产力{必须大量生产}的部件，所以最好一次做上个一组或半组！", GRAY, DARK_RED)
                    .line()
                    .line("超低压的时代的六件套主要需要{铜}和{锡}来制造，也是蒸汽时代的主要材料的延续", link(OresChapter.COPPER_VEIN), link(OresChapter.TIN_ORE_VEIN)))
            .task(Task.item(GTOItems.ULV_ELECTRIC_MOTOR))
            .reward(Reward.item(GTOItems.ULV_ELECTRIC_MOTOR, 4))
            .dependsOn(WELCOME_TO_THE_ULTRA_LOW_PRESSURE));

    public static final Quest PRIMITIVE_GENERATOR = CHAPTER.quest("primitive_generator", q -> q
            .at(-3.5, -5)
            .title("最原始的发电机")
            .subtitle("以热生电")
            .desc(d -> d
                    .line("顾名思义，烧东西来发电。")
                    .line()
                    .line("{本质上就是一个小型的火力发电机}", GRAY.italic()))
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.ULV]))
            .reward(Reward.item(Items.COAL, 16))
            .dependsOn(SLOWEST_MOTOR)
            .optional());

    public static final Quest THIS_IS_A_WIND_TURBINE = CHAPTER.quest("this_is_a_wind_turbine", q -> q
            .at(-5, -5)
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
            .dependsOn(PRIMITIVE_GENERATOR)
            .optional());

    public static final Quest ORIGINAL_CAULDRON = CHAPTER.quest("original_cauldron", q -> q
            .at(2, -3.5)
            .title("初代大乱炖")
            .subtitle("能力有限")
            .desc(d -> d
                    .line("由于电压过低，实际上也只能处理那么几个配方。")
                    .line()
                    .line("聚乙烯是你必须要处理的配方。"))
            .task(Task.item(GTOMachines.ULV_CHEMICAL_REACTOR[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.MUTTON_WRAP))
            .dependsOn(SLOWEST_MOTOR));

    public static final Quest CONDUCTORS_START_PRODUCTION = CHAPTER.quest("conductors_start_production", q -> q
            .at(-3.5, -3.5)
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
            .dependsOn(SLOWEST_MOTOR));

    public static final Quest MAKE_LIQUIDS_FLOW = CHAPTER.quest("make_liquids_flow", q -> q
            .at(-0.3, -5)
            .title("让液体动起来")
            .subtitle("但这不能抽原油")
            .desc(d -> d
                    .line("在小机器的一个面上放置电动泵，即可作为覆盖板{主动抽取/排出}小机器内的液体", LIGHT_PURPLE)
                    .line()
                    .line("那当然前提是你得有个{容器}紧贴你的小机器对吧", YELLOW)
                    .line()
                    .line("每tick可以操作{32mb}的液体，虽然不多，但也算是一个自动化的方案了", DARK_RED))
            .task(Task.item(GTOItems.ULV_ELECTRIC_PUMP))
            .reward(Reward.item(GTOItems.ULV_ELECTRIC_PUMP))
            .dependsOn(SLOWEST_MOTOR));

    public static final Quest SOLIDIFICATION = CHAPTER.quest("solidification", q -> q
            .at(2, -7)
            .title("固化成型")
            .subtitle("让它冷却下来")
            .desc(d -> d
                    .line("{固化机}是可以将液体重塑为固体的机器", GOLD)
                    .line()
                    .line("同样的问题，电压过低，所以就处理几个关键配方就行了")
                    .line()
                    .line("你需要制作聚乙烯片"))
            .task(Task.item(GTOMachines.ULV_FLUID_SOLIDIFIER[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.KELP_ROLL_SLICE))
            .dependsOn(MAKE_LIQUIDS_FLOW));

    public static final Quest IT_CAN_HELP_ME_MOVE_THINGS = CHAPTER.quest("it_can_help_me_move_things", q -> q
            .at(-3.5, -1.5)
            .title("它能帮我搬东西！")
            .subtitle("凎，这东西怎么速度也没我快！")
            .desc(d -> d
                    .line("贴在小机器的面上可以自动拉取物品，但速度实在是堪忧......")
                    .line()
                    .line("以及你需要提升一下你的{橡胶}产量了", GOLD))
            .task(Task.item(GTOItems.ULV_CONVEYOR_MODULE))
            .reward(Reward.item(GTOItems.ULV_CONVEYOR_MODULE))
            .dependsOn(SLOWEST_MOTOR));

    public static final Quest BURETTE = CHAPTER.quest("burette", q -> q
            .at(2, -5)
            .title("滴定管？")
            .subtitle("精确到位")
            .desc(d -> d
                    .line("如果你的小机器的槽位有{多个}液体输入的话，那无疑使用流体校准器是个{更优的选择}", GOLD, GREEN)
                    .line()
                    .line("它的功能包括{精确输入}与{保持输入}，前者可以精确输入的{位置}，后者是{避免输入量过大}挤占其他流体的槽位", AQUA, BLUE, LIGHT_PURPLE, YELLOW))
            .task(Task.item(GTOItems.ULV_FLUID_REGULATOR))
            .dependsOn(MAKE_LIQUIDS_FLOW)
            .hideDependencyLines(false).hideDependentLines());

    public static final Quest PRIMITIVE_MEANS_PRIMITIVE_RECIPE = CHAPTER.quest("primitive_means_primitive_recipe", q -> q
            .at(12.5, -5)
            .title("原始手段，原始配方")
            .subtitle("似曾相识故人来")
            .desc(d -> d
                    .line("蒸汽裂化机只能处理石化四种主要的中间产物（{石脑油}，{炼油气}，{轻燃油}，{重燃油}）", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("产量{偏低且慢}，在{LV}能制作{基础化学反应釜}之后可以换用反应釜配方获得50%的原料效率", RED, LV, AQUA)
                    .line()
                    .line("在{HV}能做{裂化机}之后可以获得100%的原料效率", HV, AQUA))
            .task(Task.item(MultiBlockC.STEAM_CRACKER))
            .reward(Reward.item(QuestExternalRefs.BiomesWeveGoneItems.GREEN_APPLE_PIE))
            .dependsOn(BURETTE));

    public static final Quest CANNOT_BE_USED_FOR_PUMPING_WATER = CHAPTER.quest("cannot_be_used_for_pumping_water", q -> q
            .at(-0.75, -2.5)
            .title("一样不能用来抽水")
            .subtitle("通往烦人之路")
            .desc(d -> d
                    .line("活塞是一类重要的中间合成产物，除了合成之外没有别的用途")
                    .line()
                    .line("不过我猜你会喜欢由它合成的半流质发电机"))
            .task(Task.item(GTOItems.ULV_ELECTRIC_PISTON))
            .reward(Reward.item(GTOItems.ULV_ELECTRIC_PISTON))
            .dependsOn(SLOWEST_MOTOR));

    public static final Quest THIS_IS_AN_INTERNAL_COMBUSTION_ENGINE = CHAPTER.quest("this_is_an_internal_combustion_engine", q -> q
            .at(0.75, -2.5)
            .title("这是内燃机")
            .subtitle("实际上什么油都能烧")
            .desc(d -> d
                    .line("另一种发电方式，可以让你直接使用石化燃料发电。")
                    .line()
                    .line("产能略微比原始热力发电机大。"))
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.CABBAGE_ROLLS))
            .dependsOn(CANNOT_BE_USED_FOR_PUMPING_WATER)
            .optional());

    public static final Quest ULV_LATHE = CHAPTER.quest("ulv_lathe", q -> q
            .at(-0.75, -1)
            .subtitle("电动矬子")
            .desc(d -> d
                    .line("{车床}是可以将金属锭直接制作为{杆}（产量比手搓的翻倍哦！）、将螺栓制作为{螺丝}、将齿轮制作为{锯片}的机器", GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("由于电压过低，目前你还无法加工{钢}", GOLD)
                    .line()
                    .line("{我讨厌锉子！}", GRAY.italic()))
            .task(Task.item(GTOMachines.ULV_LATHE[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.CAKE_SLICE))
            .dependsOn(CANNOT_BE_USED_FOR_PUMPING_WATER));

    public static final Quest PLACE_IN_POSITION = CHAPTER.quest("place_in_position", q -> q
            .at(-2, -1.5)
            .title("摆放到位")
            .subtitle("速度还不如手搬呢！")
            .desc(d -> d.line("机械臂的合成就复杂很多了，所以前期的其他工作一定要处理好"))
            .task(Task.item(GTOItems.ULV_ROBOT_ARM))
            .reward(Reward.item(GTOItems.ULV_ROBOT_ARM))
            .dependsOn(SLOWEST_MOTOR, CANNOT_BE_USED_FOR_PUMPING_WATER));

    public static final Quest PRIMITIVE_ASSEMBLY = CHAPTER.quest("primitive_assembly", q -> q
            .at(-2, 0)
            .title("原始拼接")
            .subtitle("反正能把它们组装在一起就行了")
            .desc(d -> d
                    .line("看到EMI组装机右下角的{电压等级}了吗？它决定你必须要用什么机器来制作！", YELLOW)
                    .line()
                    .line("很可怜，并没有几台机器支持{ULV}电压...", ULV))
            .task(Task.item(GTOMachines.ULV_ASSEMBLER[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersRespiteItems.ROSE_HIP_PIE_SLICE))
            .dependsOn(IT_CAN_HELP_ME_MOVE_THINGS, PLACE_IN_POSITION));

    public static final Quest RUBY_ROD = CHAPTER.quest("ruby_rod", q -> q
            .at(0.75, -1)
            .subtitle("本质上是催化剂？")
            .desc(d -> d
                    .line("红宝石杆能作为聚合反应的催化剂以参与聚乙烯的合成")
                    .line()
                    .line("以及这东西能合成搋子，可以清除机器内残留的液体。")
                    .line()
                    .line("{但既然任何一种*杆都能合成搋子，为什么要用红宝石的呢？}", DARK_GRAY.italic()))
            .task(Task.item(TagPrefix.rod, GTMaterials.Ruby))
            .dependsOn(ULV_LATHE)
            .hideDependentLines());

    public static final Quest THEN_THAT_IS_YOUR_FIRST_CIRCUIT = CHAPTER.quest("then_that_is_your_first_circuit", q -> q
            .at(2, 0)
            .icon(GTOItems.SUPRACHRONAL_CIRCUIT[GTValues.LV])
            .title("那么，这是你的第一个电路")
            .subtitle("准备好了吗？")
            .desc(d -> d
                    .line("现在你可以开始着手制作第一个电路")
                    .line()
                    .line("你需要准备三种原料：真空管、电阻和覆膜印刷电路基板"))
            .task(Task.checkmark().title("电路"))
            .dependsOn(PRIMITIVE_ASSEMBLY, ORIGINAL_CAULDRON));

    public static final Quest THE_THREE_MUSKETEERS_ONE_VACUUM_TUBE = CHAPTER.quest("the_three_musketeers_one_vacuum_tube", q -> q
            .at(3.5, -1)
            .title("三剑客其一：真空管")
            .subtitle("希望你准备好了不少")
            .desc(d -> d.line("显然，你目前只能选择产量最低的配方"))
            .task(Task.item(GTItems.VACUUM_TUBE))
            .dependsOn(THEN_THAT_IS_YOUR_FIRST_CIRCUIT, SteamChapter.RAW_CIRCUIT_BOARD));

    public static final Quest THE_THREE_MUSKETEERS_PART_2_RESISTOR = CHAPTER.quest("the_three_musketeers_part_2_resistor", q -> q
            .at(3.5, 0)
            .title("三剑客其二：电阻")
            .subtitle("你接触的第一个电子元件")
            .desc(d -> d
                    .line("仔细翻看EMI，不要遗漏它的两个配方！")
                    .line()
                    .line("你现在做不出退火铜，所以你必须使用{铜导线}", GOLD)
                    .line()
                    .line("目前获取碳粉的途径有两个：处理{滑石/石墨矿}，或者离心{黑色灰烬}（炼钢副产物）", GOLD, GOLD))
            .task(Task.item(GTItems.RESISTOR))
            .dependsOn(THEN_THAT_IS_YOUR_FIRST_CIRCUIT));

    public static final Quest PRIMITIVE_PACKAGING_METHODS = CHAPTER.quest("primitive_packaging_methods", q -> q
            .at(3.5, -5)
            .title("原始人的打包办法")
            .subtitle("就是把它们挤在一起")
            .desc(d -> d
                    .line("{打包机}是可以将物品{4合1}或者{9合1}的机器", GOLD, YELLOW, YELLOW)
                    .line()
                    .line("可以将{小粉}打包成{大粉}，也可以用于制作{多重流体管道}", GOLD, GOLD, GOLD)
                    .line()
                    .line("当然目前你需要它来制作{锡线缆}", GOLD))
            .task(Task.item(GTOMachines.ULV_PACKER[GTValues.ULV]))
            .reward(Reward.item(QuestExternalRefs.FarmersRespiteItems.COFFEE_CAKE_SLICE, 2))
            .dependsOn(IT_CAN_HELP_ME_MOVE_THINGS, PLACE_IN_POSITION, THE_THREE_MUSKETEERS_PART_2_RESISTOR)
            .hideDependencyLines());

    public static final Quest LOW_VOLTAGE_WIRES = CHAPTER.quest("low_voltage_wires", q -> q
            .at(5, -5)
            .title("低压线缆")
            .subtitle("这可不能手搓")
            .desc(d -> d
                    .line("将非超导的导线制成{线缆}可以让你不会因为触电而受到伤害，并且{减少了线损}", GOLD, YELLOW)
                    .line()
                    .line("但依然不能突破最大电压或电流，否则会导致导线{融毁}", RED))
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Tin))
            .reward(Reward.item(TagPrefix.cableGtSingle, GTMaterials.Tin, 16))
            .dependsOn(PRIMITIVE_PACKAGING_METHODS));

    public static final Quest MAKE_WIRES_ALL_IN_ONE = CHAPTER.quest("make_wires_all_in_one", q -> q
            .at(-5, -3.5)
            .title("让导线多合一")
            .subtitle("但并不能织布")
            .desc(d -> d
                    .line("{织布机}是可以将导线制作为多重导线的机器", GOLD)
                    .line()
                    .line("比压缩机效率更高，后续的高A导线都必须使用织布机合成"))
            .task(Task.item(GTOMachines.ULV_LOOM[GTValues.ULV]))
            .dependsOn(CONDUCTORS_START_PRODUCTION));

    public static final Quest ULV_CHARGER_4X = CHAPTER.quest("ulv_charger_4x", q -> q
            .at(-4, -6.5)
            .desc(d -> d.line("用于给需要充电的工具或者装备{快速充电}，比用{电池箱}充电快", YELLOW, GOLD))
            .task(Task.item(GTMachines.CHARGER_4[GTValues.ULV]))
            .dependsOn(WELCOME_TO_THE_ULTRA_LOW_PRESSURE)
            .optional());

    public static final Quest ULV_COMPONENT_SUBSIDY = CHAPTER.quest("ulv_component_subsidy", q -> q
            .at(-3.5, 1)
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
            .dependsOn(PRIMITIVE_ASSEMBLY)
            .optional().hideUntilDepsVisible());

    public static final Quest BUILD_A_CEILING_FOR_YOUR_PRODUCTION = CHAPTER.quest("build_a_ceiling_for_your_production", q -> q
            .at(0, -6.5)
            .title("给你的产线搭个天花板{（专家）}", LIGHT_PURPLE)
            .subtitle("机器淋雨会爆炸！")
            .task(Task.checkmark())
            .dependsOn(UtilChapter.Q_1FB1EC5B, WELCOME_TO_THE_ULTRA_LOW_PRESSURE)
            .optional().hideUntilDepsVisible());

    public static final Quest OXYGEN_BUCKET = CHAPTER.quest("oxygen_bucket", q -> q
            .at(0, 2.85)
            .icon(ModItems.OXYGEN_BUCKET.get())
            .subtitle("你活着都靠它")
            .desc(d -> d
                    .line("如果你还没有造{蒸汽分离机}，是时候造一台了", GOLD)
                    .line()
                    .line("离心{空气}获取氧气", GOLD)
                    .line()
                    .line("{提示：你需要安装至少两个蒸汽流体输出仓才可以执行离心空气的配方（因为这个配方有两个流体产物）}", GRAY.italic()))
            .task(Task.item(ModItems.OXYGEN_BUCKET.get()).optional(), Task.checkmark())
            .dependsOn(WHERE_DOES_AIR_COME_FROM, SteamChapter.CENTRIFUGE));

    public static final Quest PREDECESSOR_OF_CIRCUIT_BOARDS = CHAPTER.quest("predecessor_of_circuit_boards", q -> q
            .at(2, 2.85)
            .icon(GTItems.COATED_BOARD)
            .title("电路基板的前身")
            .subtitle("上面会铭刻什么呢")
            .desc(d -> d.line("它的配方速度太慢了，你需要多准备{多几个}化学反应釜", DARK_PURPLE))
            .task(Task.item(GTItems.COATED_BOARD))
            .dependsOn(OXYGEN_BUCKET));

    public static final Quest THE_THREE_MUSKETEERS_PART_3_RESIN = CHAPTER.quest("the_three_musketeers_part_3_resin", q -> q
            .at(3.5, 1)
            .title("三剑客其三：覆膜印刷电路基板")
            .subtitle("准备强力写入！")
            .desc(d -> d
                    .line("制造这个宝贝需要你的组装机达到{真空2级}的条件", DARK_RED)
                    .line()
                    .line("希望你已经准备好了{高压蒸汽真空泵}", GOLD))
            .task(Task.item(GTItems.BASIC_CIRCUIT_BOARD))
            .dependsOn(PREDECESSOR_OF_CIRCUIT_BOARDS, THEN_THAT_IS_YOUR_FIRST_CIRCUIT, BIGGER_AND_STRONGER_VACUUM));

    public static final Quest BASIC_ELECTRONIC_CIRCUIT = CHAPTER.quest("basic_electronic_circuit", q -> q
            .at(5, 0)
            .title("{基础电子电路}", RED)
            .subtitle("你的第一块电路板")
            .desc(d -> d
                    .line("恭喜！你终于用上了{电路板}！", GRAY)
                    .line()
                    .line("一般来说新一级电压带来的{新电路板}都会比上一级电压的电路板{便宜}，记得做好更新迭代", YELLOW, AQUA))
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_LV))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.HAMBURGER, 8), Reward.item(GTItems.ELECTRONIC_CIRCUIT_LV, 2))
            .dependsOn(THE_THREE_MUSKETEERS_PART_3_RESIN, THE_THREE_MUSKETEERS_ONE_VACUUM_TUBE, THE_THREE_MUSKETEERS_PART_2_RESISTOR));

    public static final Quest DEVELOPMENT_AND_ENERGY = CHAPTER.quest("development_and_energy", q -> q
            .at(5, -3.5)
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
            .dependsOn(BASIC_ELECTRONIC_CIRCUIT));

    public static final Quest A_BUCKET_OF_CRUDE_OIL = CHAPTER.quest("a_bucket_of_crude_oil", q -> q
            .at(6.5, -2)
            .title("一桶原油")
            .subtitle("打包带回家")
            .desc(d -> d
                    .line("这种黑色的液体会生成在地表以及地下，粘稠状，掉入其中就像掉到水里一样会扣减氧气")
                    .line()
                    .line("一桶只能舀走{1B}，所以如果你想一次性带走更多原油就需要更大的单元，如{钢单元}可以一次性装{8B}的单一流体；而{铝单元}可以装{32B}流体，不过你现在还没有{铝}，你在{下一个阶段}才能见到它……", AQUA, GOLD, AQUA, GOLD, AQUA, GOLD, YELLOW)
                    .line()
                    .line("不过幸好我这里有，送你一个！"))
            .task(Task.item(GTMaterials.RawOil.getBucket()))
            .reward(
                    Reward.stack(() -> QuestStacks.filled(GTItems.FLUID_CELL_LARGE_ALUMINIUM, new FluidStack(GTMaterials.RawOil.getFluid(), 32000)), 1))
            .dependsOn(MainStoneageChapter.OIL_MEDIUM_BUCKET, DEVELOPMENT_AND_ENERGY));

    public static final Quest BRONZE_LARGE_BOILER = CHAPTER.quest("bronze_large_boiler", q -> q
            .at(6.5, -5)
            .subtitle("那些产蒸汽的大家伙")
            .desc(d -> d
                    .line("更强的锅炉，蒸汽产出是单方块锅炉的几十倍")
                    .line()
                    .line("请务必在预热阶段就加入水")
                    .line()
                    .line("除了生产大量蒸汽，青铜锅炉还能用来生产{水煤气}", GOLD))
            .task(Task.item(GTMultiMachines.LARGE_BOILER_BRONZE))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.CHOCOLATE_PIE_SLICE))
            .dependsOn(DEVELOPMENT_AND_ENERGY, LOW_VOLTAGE_WIRES));

    public static final Quest ALCHEMY_CAULDRON = CHAPTER.quest("alchemy_cauldron", q -> q
            .at(6.5, -3.5)
            .subtitle("不要用它来做饭")
            .desc(d -> d
                    .line("放一个加热器来让其升温")
                    .line()
                    .line("多个加热器可以更快的升温，小心{过热爆炸}！", DARK_RED))
            .task(Task.item(ManaMachine.ALCHEMY_CAULDRON))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.COOKED_RICE))
            .dependsOn(DEVELOPMENT_AND_ENERGY));

    public static final Quest GET_SOME_COAL_SLURRY = CHAPTER.quest("get_some_coal_slurry", q -> q
            .at(8.5, -3.5)
            .icon(GTOMaterials.CoalSlurry.getBucket())
            .title("来点煤浆")
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
            .reward(Reward.item(Items.COAL_BLOCK))
            .dependsOn(ALCHEMY_CAULDRON));

    public static final Quest BAKE_THEN_TRANSFORM = CHAPTER.quest("bake_then_transform", q -> q
            .at(8.5, -5)
            .icon(GTOMaterials.WaterGas.getBucket())
            .title("烘烤然后蜕变")
            .subtitle("青铜锅炉的第一件产物")
            .desc(d -> d
                    .line("水煤气需要在{1000K}以上发生反应", GREEN)
                    .line()
                    .line("你需要将锅炉提前预热")
                    .line()
                    .line("{在处理水煤气的时候请保持锅炉干烧（即不加入水），锅炉此时不会产出蒸汽}", YELLOW))
            .task(Task.item(GTOMaterials.WaterGas.getBucket()).optional(), Task.checkmark())
            .dependsOn(BRONZE_LARGE_BOILER, GET_SOME_COAL_SLURRY));

    public static final Quest HYDROGEN_THIS_WAY_PLEASE = CHAPTER.quest("hydrogen_this_way_please", q -> q
            .at(10.5, -5)
            .icon(ModItems.HYDROGEN_BUCKET.get())
            .title("氢，这边请")
            .subtitle("正氧负氢？")
            .desc(d -> d
                    .line("氢气可以通过离心水煤气制取")
                    .line()
                    .line("什么，你现在还没有搭建{蒸汽分离机}？", GOLD))
            .task(Task.item(ModItems.HYDROGEN_BUCKET.get()).optional(), Task.checkmark())
            .dependsOn(BAKE_THEN_TRANSFORM, SteamChapter.CENTRIFUGE));

    public static final Quest INFINITE_INTAKE_HATCH = CHAPTER.quest("infinite_intake_hatch", q -> q
            .at(-3.5, 2.85)
            .subtitle("更大马力,更大容量")
            .desc(d -> d
                    .line("可以往多方块里面塞{无限}的空气参与合成", DARK_RED)
                    .line()
                    .line("当然，这些空气是{无法}被拿出来的", DARK_RED))
            .task(Task.item(GTOMachines.INFINITE_INTAKE_HATCH))
            .dependsOn(WHERE_DOES_AIR_COME_FROM));

    public static final Quest DIGITAL_MINER = CHAPTER.quest("digital_miner", q -> q
            .at(5, 2.85)
            .subtitle("解放双手")
            .desc(d -> d
                    .line("仍然只能挖掘实体矿脉，机器的电压等级取整体框架和能源仓提供电压的最小值")
                    .line()
                    .line("如果觉得造价太高可以先用单方块矿机，等你产能上来了再做")
                    .line()
                    .line("{LV阶段可以解锁用电的单方块矿机，速度比蒸汽矿机快了不少，至少能用了}", GRAY.italic()))
            .task(Task.item(MultiBlockA.DIGITAL_MINER))
            .dependsOn(BASIC_ELECTRONIC_CIRCUIT));

    public static final Quest PROGRAMMED_CIRCUIT = CHAPTER.quest("programmed_circuit", q -> q
            .at(6.5, 0)
            .desc(d -> d
                    .line("神奇的电路，主要用于控制配方合成。")
                    .line()
                    .line("从ULV开始的输入仓和总线都自带一个编程电路槽，只需要点击就可以调整电路。只有蒸汽输入总线需要手动制作电路放入。")
                    .line()
                    .line("用电的单方块机器也自带一个编程电路槽。"))
            .task(Task.item(GTItems.PROGRAMMED_CIRCUIT))
            .dependsOn(BASIC_ELECTRONIC_CIRCUIT));

    public static final Quest CHIP_SUBSIDY = CHAPTER.quest("chip_subsidy", q -> q
            .at(6, 1.5)
            .icon(GTItems.ELECTRONIC_CIRCUIT_LV)
            .title("芯片补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_LV, 16), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_LV, 6))
            .dependsOn(BASIC_ELECTRONIC_CIRCUIT)
            .optional().hideUntilDepsVisible());

    public static final Quest BRAND_NEW_ENERGY_SYSTEM_HEAT = CHAPTER.quest("brand_new_energy_system_heat", q -> q
            .at(8.5, 0)
            .icon(GTOMachines.HEATER)
            .title("全新的能量系统：热量！")
            .subtitle("锟斤拷烫烫烫")
            .desc(d -> d
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
            .task(
                    Task.item(GTOMachines.HEATER),
                    Task.item(GTOMachines.COOLER).optional(),
                    Task.item(GTOMachines.ELECTRIC_HEATER).optional(),
                    Task.item(ManaMachine.MANA_HEATER).optional(),
                    Task.item(GTOBlocks.HEAT_PIPES[0]).optional(),
                    Task.item(GTOItems.HEAT_DETECTOR_COVER).optional(),
                    Task.item(GTOItems.HEAT_INTERFACE).optional())
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 1))
            .dependsOn(MainStoneageChapter.ANCIENT_STEELMAKING));

    public static final Quest SOME_PRIMITIVE_PETROCHEMICAL_PROCESSING = CHAPTER.quest("some_primitive_petrochemical_processing", q -> q
            .at(8.5, -2)
            .icon(MultiBlockC.PRIMITIVE_DISTILLATION_TOWER)
            .title("来点原始人的石化处理")
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
            .task(Task.item(MultiBlockC.PRIMITIVE_DISTILLATION_TOWER), Task.item(GTOMachines.HEAT_HATCH))
            .reward(Reward.item(QuestExternalRefs.BiomesWeveGoneItems.BLUEBERRY_PIE, 3))
            .dependsOn(A_BUCKET_OF_CRUDE_OIL, BRAND_NEW_ENERGY_SYSTEM_HEAT));

    public static final Quest THE_REMAINING_THREE_GUYS = CHAPTER.quest("the_remaining_three_guys", q -> q
            .at(10.5, 0)
            .title("剩下的三个家伙")
            .subtitle("并非无用")
            .desc(d -> d.line("以目前的生产力处理与存储这些原油产物是比较麻烦的，可以到AE存储出来后进行处理，不会太久"))
            .task(
                    Task.item(GTMaterials.SulfuricGas.getBucket()).optional(),
                    Task.item(GTMaterials.SulfuricHeavyFuel.getBucket()).optional(),
                    Task.item(GTMaterials.SulfuricLightFuel.getBucket()).optional(),
                    Task.checkmark().disableToast())
            .dependsOn(SOME_PRIMITIVE_PETROCHEMICAL_PROCESSING));

    public static final Quest YES_IT_S_YOU_I_WANT = CHAPTER.quest("yes_it_s_you_i_want", q -> q
            .at(10.5, -2)
            .icon(GTMaterials.SulfuricNaphtha.getBucket())
            .title("对，我要的就是你")
            .subtitle("小小的一步")
            .desc(d -> d.line("按照EMI的配方产出，即会产出于{第三层}", LIGHT_PURPLE))
            .task(Task.item(GTMaterials.SulfuricNaphtha.getBucket()).optional(), Task.checkmark())
            .dependsOn(SOME_PRIMITIVE_PETROCHEMICAL_PROCESSING));

    public static final Quest DESULFURIZER_WHEN = CHAPTER.quest("desulfurizer_when", q -> q
            .at(10.5, -3.5)
            .icon(GTMaterials.Naphtha.getBucket())
            .title("脱硫时间到")
            .subtitle("副产不要丢，可以做硫酸")
            .desc(d -> d.line("目前只能使用化学反应釜，在HV能做{脱硫机}来进行脱硫", GOLD))
            .task(Task.item(GTMaterials.Naphtha.getBucket()).optional(), Task.checkmark())
            .dependsOn(YES_IT_S_YOU_I_WANT, HYDROGEN_THIS_WAY_PLEASE));

    public static final Quest CRACKER_DIVINE_POWER = CHAPTER.quest("cracker_divine_power", q -> q
            .at(12.5, -3.5)
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
            .dependsOn(PRIMITIVE_MEANS_PRIMITIVE_RECIPE, DESULFURIZER_WHEN));

    public static final Quest C2H4_PRONOUNCED_ETHYLENE = CHAPTER.quest("c2h4_pronounced_ethylene", q -> q
            .at(14.5, -3.5)
            .icon(GTMaterials.Ethylene.getBucket())
            .title("C2H4，读作乙烯")
            .subtitle("嗨呀，真的是费劲")
            .desc(d -> d
                    .line("当然蒸馏塔的使用需要{严格遵循}EMI的配方指引", DARK_PURPLE)
                    .line()
                    .line("如果运行的配方有{11}种产出，则需要搭建{12}层高的蒸馏塔(第一层只能放置输出总线输出固体产物)", GREEN, GREEN)
                    .line()
                    .line("以及还是那个问题，注意别让原始蒸馏塔{爆炸了}", DARK_RED))
            .task(Task.item(GTMaterials.Ethylene.getBucket()).optional(), Task.checkmark())
            .dependsOn(CRACKER_DIVINE_POWER));

    public static final Quest ETHYLENE_UNITE = CHAPTER.quest("ethylene_unite", q -> q
            .at(14.5, -5)
            .icon(GTMaterials.Polyethylene.getBucket())
            .title("乙烯，合体！")
            .subtitle("不要尝试去测定它的分子量")
            .desc(d -> d
                    .line("聚乙烯是唯一能在{化学反应釜}合成的简单有机聚合物，更高级的聚合物都需要在专门的{聚合器}完成反应", GOLD, GOLD)
                    .line()
                    .line("在此阶段，你需要放一个{加热器}来提供外部热源，并且只能在{红宝石杆}的催化下进行{空气}聚合", GOLD, GOLD, GOLD)
                    .line()
                    .line("在{Jade 提示框HUD}上{没有温度属性}的机器，需要额外贴一个{导热接口}来赋予其温度属性，且导热接口需要贴在加热的那一面", AQUA, RED, GOLD))
            .task(Task.item(GTOItems.HEAT_INTERFACE), Task.item(GTMaterials.Polyethylene.getBucket()))
            .dependsOn(RUBY_ROD, C2H4_PRONOUNCED_ETHYLENE, WHERE_DOES_AIR_COME_FROM));

    public static final Quest PLASTIC_SHEETS = CHAPTER.quest("plastic_sheets", q -> q
            .at(14.5, -7)
            .title("塑料片")
            .subtitle("千辛万苦")
            .desc(d -> d.line("在超低压时代先造一些凑活用点，后面电压上去了一定要换其他的机器来制作它"))
            .task(Task.item(TagPrefix.plate, GTMaterials.Polyethylene))
            .reward(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 8))
            .dependsOn(SOLIDIFICATION, SteamChapter.PLATE_CASTING_MOLD, ETHYLENE_UNITE));

    public static final Quest PLASTIC_SUBSIDY = CHAPTER.quest("plastic_subsidy", q -> q
            .at(15.5, -8)
            .icon(TagPrefix.plate, GTMaterials.Polyethylene)
            .title("塑料补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 128L), Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(TagPrefix.plate, GTMaterials.Polyethylene, 24))
            .dependsOn(PLASTIC_SHEETS)
            .optional().hideUntilDepsVisible());

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(SteamChapter.HUH_YOU_TOO, -2, -8.5))
                .link(QuestLink.of(ApotheosisChapter.RARITY_FORGE, -2, 1.5))
                .link(QuestLink.of(ApotheosisChapter.BOSS_SUMMONER, -3.5, 0))
                .link(QuestLink.of(LvChapter.WELCOME_TO_THE_LOW_VOLTAGE_STAGE, 17, -7))
                .link(QuestLink.of(SteamChapter.CENTRIFUGE, 0, 1.5)));
    }
}
