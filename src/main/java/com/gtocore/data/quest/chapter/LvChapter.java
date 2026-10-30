package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.QuestStacks;
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

import appeng.core.definitions.AEItems;

import com.hepdd.gtmthings.data.CustomItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class LvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("lv", c -> c
            .title("4. 低压(LV)")
            .subtitle("Tier1-LV")
            .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
            .hideQuestsUntilDepsVisible()
            .autofocus(LvChapter.WELCOME_TO_THE_LOW_VOLTAGE_STAGE));

    public static final Quest QUARTZITE_GEM = CHAPTER.quest("quartzite_gem", q -> q
            .at(4, -7)
            .task(Task.item(TagPrefix.gem, GTMaterials.Quartzite)));

    public static final Quest WELCOME_TO_THE_LOW_VOLTAGE_STAGE = CHAPTER.quest("welcome_to_the_low_voltage_stage", q -> q
            .at(0.5, -6.5)
            .icon(GTMachines.HULL[GTValues.LV])
            .title("欢迎来到{低压}阶段", GRAY)
            .subtitle("{你需要天马行空般的想象力}", GRAY)
            .desc(d -> d
                    .line("这一阶段你将会解锁更多的机器类型！我想你并不喜欢一直用工具{低效率}地制作部件吧！", GRAY)
                    .line()
                    .line("从这个阶段开始，你会开始遇到复杂的化工产线，希望你不会被吓到。把{MV级电路}作为本阶段的第一个目标是一个不错的选择", GOLD)
                    .line()
                    .line("以及，如同大多数GT流程一样，你马上就会遇到前期最可怕的吃电大户——{电力高炉}，把它作为目标前进吧！", GREEN))
            .task(Task.item(GTMachines.HULL[GTValues.LV]))
            .reward(
                    Reward.item(QuestExternalRefs.FarmersDelightItems.GRILLED_SALMON, 2),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(UlvChapter.PLASTIC_SHEETS));

    public static final Quest AUTOMATE_MAGNETIZATION = CHAPTER.quest("automate_magnetization", q -> q
            .at(0.5, -8.5)
            .title("自动磁化！")
            .subtitle("拯救你的红石")
            .desc(d -> d
                    .line("磁化铁杆可以手动合成，但磁化钢杆起就不行了")
                    .line()
                    .line("之后的磁化物质都需要通过它来制作"))
            .task(Task.item(GTMachines.POLARIZER[GTValues.LV]))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependentLines());

    public static final Quest POWER = CHAPTER.quest("power", q -> q
            .at(5.5, 1)
            .title("POWER!")
            .subtitle("需要来给你电一下")
            .desc(d -> d
                    .line("{基础电解机}的主要用途就是电解水来获取无尽的{氢气}和{氧气}，或是电解{蓝宝石粉}来获取{铝粉}，但这不是你唯一能在低压时代获取{铝粉}的办法", GOLD, GOLD, GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("如果你需要来点{镓粉}制作{二极管}，也可以通过抽奖般的可怜概率获得一些", GOLD, GOLD))
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.LV]))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LV_SUPERCONDUCTOR = CHAPTER.quest("lv_superconductor", q -> q
            .at(-1, -8.5)
            .title("LV超导")
            .desc(d -> d.line("这个是LV阶段的超导材料"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.ConductiveAlloy))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LV_ARC_FURNACE = CHAPTER.quest("lv_arc_furnace", q -> q
            .at(2, -7)
            .task(Task.item(GTMachines.ARC_FURNACE[GTValues.LV]))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE));

    public static final Quest ANNEALED_COPPER_INGOT = CHAPTER.quest("annealed_copper_ingot", q -> q
            .at(2, -8.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.AnnealedCopper))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.AnnealedCopper, 32))
            .dependsOn(LV_ARC_FURNACE));

    public static final Quest ELECTRIC_HEATER = CHAPTER.quest("electric_heater", q -> q
            .at(3.5, -8.5)
            .desc(d -> d.line("温度过高会自动关机"))
            .task(Task.item(GTOMachines.ELECTRIC_HEATER))
            .dependsOn(ANNEALED_COPPER_INGOT)
            .requireAny()
            .hideDependentLines());

    public static final Quest YOU_WILL_HATE_THEM = CHAPTER.quest("you_will_hate_them", q -> q
            .at(0.5, -4.5)
            .title("你会讨厌它们的")
            .subtitle("部件的部件")
            .desc(d -> d
                    .line("这是你每个电力时代第一个需要做的零件")
                    .line()
                    .line("你或许已经发现了，从低压(LV)时代开始你将不再能够手动合成{马达}、{活塞}、{电动泵}等部件，它们只能在组装机内制作，且这{三种部件均需要}{壳体}{零件作为组装材料}", GOLD, GOLD, GOLD, YELLOW, GOLD, YELLOW))
            .task(Task.item(GTOTagPrefix.MOTOR_ENCLOSURE, GTMaterials.Steel))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE));

    public static final Quest LV_ELECTRIC_MOTOR = CHAPTER.quest("lv_electric_motor", q -> q
            .at(0.5, -2.5)
            .subtitle("欢迎来到低压时代")
            .desc(d -> d
                    .line("从低压时代开始，各种部件和材料大多都需要用特定的机器制作，而不能手动合成")
                    .line()
                    .line("而且随着阶段推进，需要的各种资源数量会越来越{令人痛苦}！所以最好养成批量合成半组、一组甚至十几组材料的好习惯！", RED))
            .task(Task.item(GTItems.ELECTRIC_MOTOR_LV))
            .reward(Reward.item(GTItems.ELECTRIC_MOTOR_LV, 8))
            .dependsOn(YOU_WILL_HATE_THEM, AUTOMATE_MAGNETIZATION));

    public static final Quest OPTIONS_FOR_POWER_GENERATION_IN_THE = CHAPTER.quest("options_for_power_generation_in_the", q -> q
            .at(2, -5.5)
            .title("低压时代你能选择的电力来源")
            .subtitle("通往罗马的路不止一条")
            .desc(d -> d
                    .line("LV时代的单方块发电机，输出电流均为2A")
                    .line()
                    .line("建议选用基础蒸汽轮机，原材料来源最简单方便"))
            .task(
                    Task.item(GTMachines.STEAM_TURBINE[GTValues.LV]).optional(),
                    Task.item(GTMachines.COMBUSTION[GTValues.LV]).optional(),
                    Task.item(GTMachines.GAS_TURBINE[GTValues.LV]).optional(),
                    Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.LV]).optional(),
                    Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.LV]).optional(),
                    Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.LV]).optional())
            .dependsOn(LV_ELECTRIC_MOTOR)
            .optional());

    public static final Quest MIX_AND_MIX = CHAPTER.quest("mix_and_mix", q -> q
            .at(1, -1)
            .title("搅啊搅")
            .subtitle("这可不是洗衣机")
            .desc(d -> d
                    .line("你需要用{搅拌机}来生产{液态锡铁合金}", GOLD, GOLD)
                    .line()
                    .line("后面搅拌机会用得非常多，为了避免串配方，整多几个总不是坏事")
                    .line()
                    .line("{提示：小机器的左下角都有一个“输入限制”按钮，点击启用则一种原材料最多只能占用机器的一个输入槽}", GRAY.italic()))
            .task(Task.item(GTMachines.MIXER[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest BLACK_BOX_REACTOR = CHAPTER.quest("black_box_reactor", q -> q
            .at(4, -1)
            .title("黑盒反应器")
            .subtitle("伟大，无需多言")
            .desc(d -> d
                    .line("绝大多数的化学反应都在这里进行")
                    .line()
                    .line("你需要进行的化学反应不少，强烈建议多做几个"))
            .task(Task.item(GTMachines.CHEMICAL_REACTOR[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest RUBBER_BUCKET = CHAPTER.quest("rubber_bucket", q -> q
            .at(5.5, -1)
            .icon(GTMaterials.Rubber.getBucket())
            .subtitle("线缆的重要组成部分")
            .desc(d -> d
                    .line("你可能已经在前面的章节做出了橡胶片来制作最低级的线缆，但生产更高级的线缆需要{液态橡胶}", GOLD)
                    .line()
                    .line("在化学反应釜里完成它吧"))
            .task(Task.item(GTMaterials.Rubber.getBucket()).optional(), Task.checkmark())
            .dependsOn(BLACK_BOX_REACTOR)
            .hideDependentLines());

    public static final Quest FORMALDEHYDE_BUCKET = CHAPTER.quest("formaldehyde_bucket", q -> q
            .at(-6.5, 6.5)
            .icon(GTMaterials.Formaldehyde.getBucket())
            .desc(d -> d.line("使用甲醇和氧气合成"))
            .task(Task.item(GTMaterials.Formaldehyde.getBucket()).optional(), Task.checkmark())
            .dependsOn(BLACK_BOX_REACTOR)
            .hideDependencyLines());

    public static final Quest SULFURIC_ACID_BUCKET = CHAPTER.quest("sulfuric_acid_bucket", q -> q
            .at(-5.5, 6.5)
            .icon(GTMaterials.SulfuricAcid.getBucket())
            .desc(d -> d
                    .line("多种方式获取，推荐硫粉一路氧化，最后加水")
                    .line()
                    .line("你在脱硫中获得的硫化氢也可以制成硫酸"))
            .task(Task.item(GTMaterials.SulfuricAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(BLACK_BOX_REACTOR)
            .hideDependencyLines());

    public static final Quest STEEL_PISTON_HOUSING = CHAPTER.quest("steel_piston_housing", q -> q
            .at(-1, -2.5)
            .task(Task.item(GTOTagPrefix.PISTON_HOUSING, GTMaterials.Steel))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest LV_ELECTRIC_PISTON = CHAPTER.quest("lv_electric_piston", q -> q
            .at(-3, -2.5)
            .task(Task.item(GTItems.ELECTRIC_PISTON_LV))
            .reward(Reward.item(GTItems.ELECTRIC_PISTON_LV))
            .dependsOn(STEEL_PISTON_HOUSING));

    public static final Quest LV_LATHE = CHAPTER.quest("lv_lathe", q -> q
            .at(-4.5, -4)
            .subtitle("多面手")
            .desc(d -> d
                    .line("用车床可以让你以一个锭的代价生产{两个}对应的杆", AQUA)
                    .line()
                    .line("除此之外车床还可以加工螺栓成{螺丝}，加工材料粒成材料{滚珠}，加工齿轮成{锯片}，加工精致宝石和宝石板成{透镜}", GOLD, GOLD, GOLD, GOLD))
            .task(Task.item(GTMachines.LATHE[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON));

    public static final Quest LV_EXTRUDER = CHAPTER.quest("lv_extruder", q -> q
            .at(-3, -4)
            .desc(d -> d
                    .line("{压模器}大部分的配方都需要更高的电压", GOLD)
                    .line()
                    .line("现阶段可以压部分管道，螺栓"))
            .task(Task.item(GTMachines.EXTRUDER[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON));

    public static final Quest STEEL_PUMP_BARREL = CHAPTER.quest("steel_pump_barrel", q -> q
            .at(2, -2.5)
            .task(Task.item(GTOTagPrefix.PUMP_BARREL, GTMaterials.Steel))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest LV_ELECTRIC_PUMP = CHAPTER.quest("lv_electric_pump", q -> q
            .at(4, -2.5)
            .task(Task.item(GTItems.ELECTRIC_PUMP_LV))
            .reward(Reward.item(GTItems.ELECTRIC_PUMP_LV))
            .dependsOn(STEEL_PUMP_BARREL));

    public static final Quest LV_PUMP = CHAPTER.quest("lv_pump", q -> q
            .at(5.5, -2.5)
            .subtitle("终于可以不用桶了")
            .desc(d -> d.line("可以抽世界中生成的实体流体比如水和岩浆，当然最推荐抽石油"))
            .task(Task.item(GTMachines.PUMP[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PUMP));

    public static final Quest LV_EXTRACTOR = CHAPTER.quest("lv_extractor", q -> q
            .at(2, -1)
            .desc(d -> d
                    .line("{提取机}可以用于提取液态锡，生橡胶末和染料", GOLD)
                    .line()
                    .line("与原版GT不同的是，{提取机}的大部分职能被{液化炉}取代了，导致提取机其实没什么用", GOLD, GOLD))
            .task(Task.item(GTMachines.EXTRACTOR[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON, LV_ELECTRIC_PUMP)
            .hideDependencyLines());

    public static final Quest LIQUID_TIN = CHAPTER.quest("liquid_tin", q -> q
            .at(2, 0)
            .icon(GTMaterials.Tin.getBucket())
            .title("液态锡")
            .subtitle("你的第一桶液态金属")
            .desc(d -> d
                    .line("你的第一桶液态锡来源于锡锭高温提取")
                    .line()
                    .line("{注意需要外部热源}", YELLOW))
            .task(Task.item(GTMaterials.Tin.getBucket()).optional(), Task.checkmark())
            .dependsOn(LV_EXTRACTOR));

    public static final Quest LIQUID_TIN_STEEL_ALLOY = CHAPTER.quest("liquid_tin_steel_alloy", q -> q
            .at(1, 0)
            .icon(GTMaterials.TinAlloy.getBucket())
            .title("液态锡铁合金")
            .subtitle("你的第一桶液态合金")
            .desc(d -> d
                    .line("将{铁粉}与{液态锡}放置在{搅拌机}内搅拌即可获得", GOLD, GOLD, GOLD)
                    .line()
                    .line("{需要的温度更高，你需要一台电力加热器}", YELLOW))
            .task(Task.item(GTMaterials.TinAlloy.getBucket()).optional(), Task.checkmark())
            .dependsOn(MIX_AND_MIX, LIQUID_TIN, ELECTRIC_HEATER));

    public static final Quest LV_ROBOT_ARM = CHAPTER.quest("lv_robot_arm", q -> q
            .at(-3, -1)
            .task(Task.item(GTItems.ROBOT_ARM_LV))
            .reward(Reward.item(GTItems.ROBOT_ARM_LV))
            .dependsOn(LV_ELECTRIC_MOTOR, LV_ELECTRIC_PISTON));

    public static final Quest LV_CONVEYOR_MODULE = CHAPTER.quest("lv_conveyor_module", q -> q
            .at(-1, -1)
            .task(Task.item(GTItems.CONVEYOR_MODULE_LV))
            .reward(Reward.item(GTItems.CONVEYOR_MODULE_LV))
            .dependsOn(LV_ELECTRIC_MOTOR, RUBBER_BUCKET));

    public static final Quest ASSEMBLE_EVERYTHING = CHAPTER.quest("assemble_everything", q -> q
            .at(-3, 1)
            .title("组装一切")
            .subtitle("复仇者，集结！")
            .desc(d -> d
                    .line("大量的配方都需要组装机完成")
                    .line()
                    .line("同理，多起几台"))
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.LV]))
            .dependsOn(LV_ROBOT_ARM, LV_CONVEYOR_MODULE));

    public static final Quest RUBBER_BATH_MACHINE = CHAPTER.quest("rubber_bath_machine", q -> q
            .at(7, 1)
            .title("橡胶泡澡机")
            .subtitle("给你的导线洗个澡")
            .desc(d -> d.line("高级的{线缆}都需要在{过胶机}内完成，且能帮你省去固化步骤", GOLD, GOLD))
            .task(Task.item(GTOMachines.LAMINATOR[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PUMP, LV_CONVEYOR_MODULE)
            .hideDependencyLines());

    public static final Quest LARGER_HOT_WATER_BOTTLE = CHAPTER.quest("larger_hot_water_bottle", q -> q
            .at(-4.5, 1)
            .title("更大的热水壶")
            .subtitle("你最想要的缓存")
            .desc(d -> d
                    .line("这个大家伙的输入和输出都只能通过{钢制储罐阀门}来进行。", GOLD)
                    .line()
                    .line("{钢制储罐阀门}的自动输出需要开口向下，但大型蒸汽储罐没有地方可以让钢制储罐阀门向下开口，所以你需要用{管道}的{主动抽取功能}将蒸汽导出，或者使用{蒸汽泵}。", GOLD, GOLD, AQUA, GOLD))
            .task(Task.item(MultiBlockC.LARGE_STEAM_STORAGE_TANK))
            .dependsOn(ASSEMBLE_EVERYTHING)
            .optional());

    public static final Quest INPUT_AND_OUTPUT_OF_MATERIALS = CHAPTER.quest("input_and_output_of_materials", q -> q
            .at(-0.5, 3.5)
            .title("材料的输入与输出")
            .subtitle("关系到你的产物")
            .desc(d -> d
                    .line("安装{输入/输出仓}使多方块机器能够输入/输出{流体}，", YELLOW, GOLD)
                    .line()
                    .line("而同样的，安装{输入/输出总线}就能够使多方块机器输入/输出{物品}。", YELLOW, GOLD)
                    .line()
                    .line("在你能够制作{输入/输出总成}之前都必须保持这个组合！", YELLOW)
                    .line()
                    .line("请注意：如果没有配方对应的输出仓室，机器是不会执行配方的，它会显示“输出空间不足”以提示你解决该问题")
                    .line()
                    .line("·例如：如果你的电力高炉没有放置输出总线，那它就不会为你炼制铝锭！")
                    .line()
                    .line("提示：所有的舱室都自带{自动输入/输出}，你可以用{软锤}右键舱室以开关这个功能", YELLOW, GOLD))
            .task(
                    Task.item(GTMachines.FLUID_IMPORT_HATCH[GTValues.LV]),
                    Task.item(GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]),
                    Task.item(GTMachines.ITEM_IMPORT_BUS[GTValues.LV]),
                    Task.item(GTMachines.ITEM_EXPORT_BUS[GTValues.LV]))
            .dependsOn(ASSEMBLE_EVERYTHING));

    public static final Quest FUNCTIONAL_CHAMBERS = CHAPTER.quest("functional_chambers", q -> q
            .at(-0.5, 2.5)
            .icon(GTMachines.MAINTENANCE_HATCH)
            .title("功能性仓室")
            .subtitle("最后要交代的琐碎话")
            .desc(d -> d
                    .line("严禁共用维护仓和消声仓！")
                    .line()
                    .line("维护{维护仓}时，请将所需工具放在{物品栏/工具皮带}中，空手点击维修槽位即可。如果放入{胶带}，机器会在故障时自动消耗一个胶带进行维护。", GOLD, YELLOW, GOLD)
                    .line()
                    .line("{消声仓}前方3格必须{无遮挡}（包括管道）！并且现在消声仓在默认配置、普通以上难度时每秒有一定概率会产生灰烬，灰烬满后也视为{被遮挡}，使得机器罢工！", GOLD, RED, RED)
                    .line()
                    .line("{消声仓}的回收概率是{不产生灰烬的概率}，越高级的消声仓能抑制更多的污染，从而产生更少的灰烬，同时也有更大的容量，最终减少你每次清理消声仓的频率", GOLD, YELLOW)
                    .line()
                    .line("需要额外注意的是，消声仓{只能通过无人机来自动清理灰烬}，不过除了升级你的消声仓以外，{空气净化器}也能大幅减少你清理消声仓的频率，减少一些痛苦……", YELLOW, GOLD))
            .task(Task.item(GTMachines.MAINTENANCE_HATCH), Task.item(GTMachines.MUFFLER_HATCH[GTValues.LV]))
            .reward(Reward.item(GTItems.DUCT_TAPE, 5))
            .dependsOn(ASSEMBLE_EVERYTHING));

    public static final Quest LV_FLUID_REGULATOR = CHAPTER.quest("lv_fluid_regulator", q -> q
            .at(4, -4)
            .task(Task.item(GTItems.FLUID_REGULATOR_LV))
            .dependsOn(LV_ELECTRIC_PUMP)
            .hideDependentLines());

    public static final Quest STEEL_EMITTER_BASE = CHAPTER.quest("steel_emitter_base", q -> q
            .at(5.5, -8.5)
            .task(Task.item(GTOTagPrefix.EMITTER_BASES, GTMaterials.Steel))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LV_EMITTER = CHAPTER.quest("lv_emitter", q -> q
            .at(5.5, -7)
            .task(Task.item(GTItems.EMITTER_LV))
            .dependsOn(STEEL_EMITTER_BASE, QUARTZITE_GEM));

    public static final Quest POWER_OF_LASER = CHAPTER.quest("power_of_laser", q -> q
            .at(7, -7)
            .title("激光之力！")
            .subtitle("拯救你的材料")
            .desc(d -> d
                    .line("用激光焊接机可以自动化并节省你制作各种部件外壳的材料")
                    .line()
                    .line("推荐根据你需要的部件外壳种类多做几个")
                    .line()
                    .line("除此之外，它也可以制作转子，机械方块和长杆"))
            .task(Task.item(GTOMachines.LASER_WELDER[GTValues.LV]))
            .dependsOn(LV_EMITTER));

    public static final Quest LV_ROLLING = CHAPTER.quest("lv_rolling", q -> q
            .at(-4.5, -2.5)
            .subtitle("最需要尽早做出的机器")
            .desc(d -> d
                    .line("通过调整电路，辊轧机可以高效率地让你的锭变成单层板或者双层板")
                    .line()
                    .line("{可以将锭1:1变成单层板，大幅节省材料}", YELLOW))
            .task(Task.item(GTOMachines.ROLLING[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON));

    public static final Quest LV_CLUSTER = CHAPTER.quest("lv_cluster", q -> q
            .at(2, -4)
            .desc(d -> d
                    .line("多辊式轧机可以让你的单层板变成箔")
                    .line()
                    .line("你需要用它来制作{云母绝缘薄片}，以及{翻倍产出}优质电子电路中需要的{箔}", GOLD, YELLOW, GOLD))
            .task(Task.item(GTOMachines.CLUSTER[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR)
            .hideDependentLines());

    public static final Quest LV_BENDER = CHAPTER.quest("lv_bender", q -> q
            .at(-2, -4)
            .desc(d -> d.line("卷弯机可以让你的长杆变成弹簧，杆变成环，板变成弯曲板，以及合成特殊的空单元"))
            .task(Task.item(GTMachines.BENDER[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON));

    public static final Quest STEEL_SENSOR_CASING = CHAPTER.quest("steel_sensor_casing", q -> q
            .at(5.5, -4)
            .task(Task.item(GTOTagPrefix.SENSOR_CASING, GTMaterials.Steel))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LV_SENSOR = CHAPTER.quest("lv_sensor", q -> q
            .at(5.5, -5.5)
            .task(Task.item(GTItems.SENSOR_LV))
            .dependsOn(STEEL_SENSOR_CASING, QUARTZITE_GEM));

    public static final Quest LV_UNPACKER = CHAPTER.quest("lv_unpacker", q -> q
            .at(-6, 2)
            .task(Task.item(GTOMachines.UNPACKER[GTValues.LV]))
            .dependsOn(LV_ROBOT_ARM, LV_CONVEYOR_MODULE)
            .hideDependencyLines());

    public static final Quest SMALL_GALLIUM_ARSENIDE_DUST = CHAPTER.quest("small_gallium_arsenide_dust", q -> q
            .at(-4.5, 2)
            .desc(d -> d
                    .line("{砷粉}来自于{离心}{雄黄粉}", GOLD, YELLOW, GOLD)
                    .line()
                    .line("{镓粉}来自于{离心}{洁净闪锌矿粉}或是{电解}{闪锌矿粉}，前者有固定1/9的概率产出，而后者供给LV电压的情况下产出{镓粉}的概率是5%。而随着电压等级的提升，这个概率会逐步上升。", GOLD, YELLOW, GOLD, YELLOW, GOLD, GOLD))
            .task(Task.item(TagPrefix.dustSmall, GTMaterials.GalliumArsenide, 4))
            .dependsOn(LV_UNPACKER));

    public static final Quest DIODE = CHAPTER.quest("diode", q -> q
            .at(-3, 3.5)
            .subtitle("其貌不扬")
            .desc(d -> d.line("如果你的砷化镓少得可怜，你也可以先做出{电弧炉}降低你的砷化镓用量，注意查看配方", GOLD))
            .task(Task.item(GTItems.DIODE, 2))
            .dependsOn(ASSEMBLE_EVERYTHING, SMALL_GALLIUM_ARSENIDE_DUST));

    public static final Quest LV_WIREMILL = CHAPTER.quest("lv_wiremill", q -> q
            .at(-1, -4)
            .desc(d -> d.line("线材轧机可以更便宜地让你把锭变成对应的细导线和1x导线"))
            .task(Task.item(GTMachines.WIREMILL[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest LV_LOOM = CHAPTER.quest("lv_loom", q -> q
            .at(-1, -5.5)
            .desc(d -> d.line("织布机可以将1x导线编织为更粗的导线"))
            .task(Task.item(GTOMachines.LOOM[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest COBALT_BRASS_BUZZ_SAW_BLADE = CHAPTER.quest("cobalt_brass_buzz_saw_blade", q -> q
            .at(7, -2.5)
            .task(Task.item(TagPrefix.toolHeadBuzzSaw, GTMaterials.CobaltBrass))
            .dependsOn(MIX_AND_MIX)
            .hideDependencyLines());

    public static final Quest LV_CUTTER = CHAPTER.quest("lv_cutter", q -> q
            .at(7, -4)
            .subtitle("电动菜刀")
            .desc(d -> d
                    .line("{基础切割机}在此阶段不是必需的机器", GOLD)
                    .line()
                    .line("但也许你可以把它当自动菜刀用"))
            .task(Task.item(GTMachines.CUTTER[GTValues.LV]))
            .dependsOn(COBALT_BRASS_BUZZ_SAW_BLADE)
            .hideDependentLines());

    public static final Quest STEEL_FIELD_GENERATOR_CASING = CHAPTER.quest("steel_field_generator_casing", q -> q
            .at(6.5, 5)
            .task(Task.item(GTOTagPrefix.FIELD_GENERATOR_CASING, GTMaterials.Steel))
            .dependsOn(WELCOME_TO_THE_LOW_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest LV_MINER = CHAPTER.quest("lv_miner", q -> q
            .at(4, -5.5)
            .subtitle("Take it easy")
            .desc(d -> d.line("你可以在任何时候结束你的手动挖矿生活，为何不是现在？"))
            .task(Task.item(GTMachines.MINER[GTValues.LV]))
            .dependsOn(LV_SENSOR));

    public static final Quest LV_VOLTAGE_COIL = CHAPTER.quest("lv_voltage_coil", q -> q
            .at(-1.5, 5)
            .task(Task.item(GTItems.VOLTAGE_COIL_LV))
            .dependsOn(ASSEMBLE_EVERYTHING));

    public static final Quest ENERGY_HATCH = CHAPTER.quest("energy_hatch", q -> q
            .at(0, 5)
            .icon(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV])
            .title("能源仓")
            .subtitle("影响机器工作电压和配方的执行速度")
            .desc(d -> d
                    .line("多方块机器的工作速度本质上取决于能源仓所能提供的{总功率}", GOLD)
                    .line()
                    .line("所以想加快处理速度有两个{通用}办法：增加{电压}和增加{电流}", AQUA, YELLOW, YELLOW)
                    .line()
                    .line("能源仓还影响多方块机器的{配方等级}，机器的配方等级必须{大于等于}配方的电压等级才会工作", YELLOW, GREEN)
                    .line()
                    .line("{想要加速机器还有很多其他方式，本章中的}{加速仓}{也是其中之一。完整加速方法请前往}{提示}{查看}", GRAY.italic(), link(LvChapter.MAKE_TIME_GO_FASTER), GRAY.italic(), link(TipsChapter.MEANS_OF_ACCELERATION), GRAY.italic())
                    .line()
                    .line("=====")
                    .line()
                    .line("{机制介绍<双仓升压>}：在一台多方块机器上安装至少{两个}{同等级能源仓}（无需电流相同）可以触发机器{升压}，机器升压后可以执行{高一级电压}的配方", AQUA, GREEN, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("在刚开始，你需要给{高炉}安装{两个}{LV能源仓}来升压到MV来满足铝的{电压要求}", GOLD, GREEN, GOLD, YELLOW))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], 2), Task.checkmark().title("请务必知晓双仓升压!"))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Silver, 1))
            .dependsOn(LV_VOLTAGE_COIL));

    public static final Quest LAVA_FURNACE = CHAPTER.quest("lava_furnace", q -> q
            .at(-12, 3)
            .subtitle("无限燃料")
            .desc(d -> d.line("能够生产{熔岩}，代价是电和圆石", GOLD))
            .task(Task.item(MultiBlockA.LAVA_FURNACE))
            .dependsOn(ENERGY_HATCH, SteamChapter.A_SLIGHTLY_LARGER_FURNACE)
            .hideDependencyLines());

    public static final Quest TREE_GROWTH_SIMULATOR = CHAPTER.quest("tree_growth_simulator", q -> q
            .at(-13.5, 3)
            .subtitle("无限木材")
            .desc(d -> d
                    .line("注意需要安装伐木工具")
                    .line()
                    .line("安装斧头消耗耐久，安装链锯消耗电量")
                    .line()
                    .line("EV以后配合{无线充能器}可以直接给主机里的链锯充电", link(LvChapter.WIRELESS_CHARGER)))
            .task(Task.item(MultiBlockG.TREE_GROWTH_SIMULATOR))
            .reward(
                    Reward.item(ToolHelper.get(GTToolType.AXE, GTOMaterials.Elementium).getItem()),
                    Reward.stack(() -> QuestStacks.charged(ToolHelper.get(GTToolType.CHAINSAW_LV, GTMaterials.Iron), 100000L, 100000L), 1))
            .dependsOn(LV_VOLTAGE_COIL, LV_CONVEYOR_MODULE, LV_ROBOT_ARM)
            .hideDependencyLines());

    public static final Quest LV_ENERGY_INPUT_HATCH_16A = CHAPTER.quest("lv_energy_input_hatch_16a", q -> q
            .at(0, 6.5)
            .desc(d -> d
                    .line("制作更强大的能源仓，能够支持多方块机器获得更高的超频和并行")
                    .line()
                    .line("{注意你的发电供应}", GRAY))
            .task(Task.item(GTOMachines.ENERGY_INPUT_HATCH_16A[GTValues.LV]))
            .dependsOn(ENERGY_HATCH));

    public static final Quest LV_ENERGY_OUTPUT_HATCH = CHAPTER.quest("lv_energy_output_hatch", q -> q
            .at(-2, 6.5)
            .desc(d -> d
                    .line("{动力仓}是构成多方块发电机器的重要仓室，作用是负责输出机器产生的电能", GOLD)
                    .line()
                    .line("普通的{动力仓}只有{2A}输出容限，你应该继续制作更强大的动力仓以满足机器的发电容量", GOLD, DARK_GREEN))
            .task(Task.item(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.LV]))
            .dependsOn(LV_VOLTAGE_COIL));

    public static final Quest GENERATOR_ARRAY = CHAPTER.quest("generator_array", q -> q
            .at(-9, 3)
            .desc(d -> d
                    .line("厌倦了堆叠单方块发电机？")
                    .line()
                    .line("你可以在任何大量用电的地方见识一下它的POWER")
                    .line()
                    .line("{使用无线电网模式无需放置动力仓}", YELLOW))
            .task(Task.item(GeneratorMultiblock.GENERATOR_ARRAY))
            .dependsOn(LV_ENERGY_OUTPUT_HATCH)
            .optional().hideDependencyLines());

    public static final Quest STEAM_PUMP = CHAPTER.quest("steam_pump", q -> q
            .at(-6, 1)
            .task(Task.item(GTOItems.STEAM_PUMP))
            .dependsOn(LARGER_HOT_WATER_BOTTLE, LV_FLUID_REGULATOR)
            .optional());

    public static final Quest THERMAL_POWER_PUMP = CHAPTER.quest("thermal_power_pump", q -> q
            .at(-15, 3)
            .desc(d -> d
                    .line("基本就是一台蒸汽水泵")
                    .line()
                    .line("输入{蒸汽}，它会回报你{等量}的水！", GOLD, AQUA))
            .task(Task.item(MultiBlockC.THERMAL_POWER_PUMP))
            .dependsOn(ASSEMBLE_EVERYTHING)
            .hideDependencyLines());

    public static final Quest LV_POWER_AMPLIFIERS = CHAPTER.quest("lv_power_amplifiers", q -> q
            .at(-1, 6.5)
            .subtitle("力大砖飞")
            .desc(d -> d
                    .line("可以安装在任意单方块机器和几乎所有多方块机器上，以额外的功率为代价换取更低的配方耗时，总能耗不变")
                    .line()
                    .line("{增幅器的优先级比超频高，因此很多时候会顶掉一次超频，这不是bug，因此不要给无损超频的机器贴}", YELLOW)
                    .line()
                    .line("{在0次超频的机器上贴增幅器有可能会使机器拉取超过上限的功率，导致机器断电}", RED))
            .task(Task.item(GTOItems.LV_POWER_AMPLIFIERS))
            .dependsOn(LV_VOLTAGE_COIL));

    public static final Quest GAS_COMPRESSOR = CHAPTER.quest("gas_compressor", q -> q
            .at(4, 7.5)
            .desc(d -> d.line("你目前需要拿它制作{高压氮气}", GOLD))
            .task(Task.item(MultiBlockG.GAS_COMPRESSOR))
            .dependsOn(LV_FLUID_REGULATOR));

    public static final Quest LV_FORMING_PRESS = CHAPTER.quest("lv_forming_press", q -> q
            .at(-4.5, -1)
            .desc(d -> d.line("冲压机床的功能不多，主要用于制作扇叶。你需要它来制作{云母基板}", GOLD))
            .task(Task.item(GTMachines.FORMING_PRESS[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_PISTON)
            .hideDependentLines());

    public static final Quest MICA_BASED_PULP = CHAPTER.quest("mica_based_pulp", q -> q
            .at(2.5, 1)
            .task(Task.item(GTOItems.MICA_BASED_PULP))
            .dependsOn(MIX_AND_MIX)
            .hideDependencyLines());

    public static final Quest MICA_BASED_SHEET = CHAPTER.quest("mica_based_sheet", q -> q
            .at(1.5, 1)
            .task(Task.item(GTOItems.MICA_BASED_SHEET))
            .dependsOn(MICA_BASED_PULP, LV_FORMING_PRESS));

    public static final Quest MICA_INSULATOR_FOIL = CHAPTER.quest("mica_insulator_foil", q -> q
            .at(0.5, 1)
            .desc(d -> d.line("后续所有线圈都需要用到，值得一条产线"))
            .task(Task.item(GTOItems.MICA_INSULATOR_FOIL))
            .dependsOn(MICA_BASED_SHEET, LV_CLUSTER));

    public static final Quest COIL_BLOCK = CHAPTER.quest("coil_block", q -> q
            .at(-1, 1)
            .title("线圈方块")
            .subtitle("特斯拉并没有发明这个")
            .desc(d -> d
                    .line("{线圈方块}在很多{多方块机器}内都会使用到。", GOLD, YELLOW)
                    .line()
                    .line("更高级的线圈方块有更多更好的增益，如获得{能耗减免}，{无损超频}，以及{增加并行}。也有很多配方会限定线圈的等级。", YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("所以在升级多方块机器的时候，除了升级{能源仓}，剩下就要着重升级{线圈方块}。", GOLD, GOLD))
            .task(Task.item(GTBlocks.COIL_CUPRONICKEL, 16))
            .dependsOn(ASSEMBLE_EVERYTHING, LIQUID_TIN_STEEL_ALLOY, MICA_INSULATOR_FOIL));

    public static final Quest LIQUEFACTION_FURNACE = CHAPTER.quest("liquefaction_furnace", q -> q
            .at(-12, 4.5)
            .subtitle("给提取机放个假？")
            .desc(d -> d
                    .line("GTO新增的机器，{主线流程中必须制造的机器}。你可以用这个大家伙将固态物质{高温液化}为液态形式", YELLOW, YELLOW)
                    .line()
                    .line("由于熔点过高，{流体提取机}无法做到很多事情，需要{液化炉}出马", GOLD, GOLD)
                    .line()
                    .line("加装附属模块后支持{并行}", YELLOW))
            .task(Task.item(MultiBlockB.LIQUEFACTION_FURNACE))
            .dependsOn(ENERGY_HATCH, COIL_BLOCK)
            .hideDependencyLines().hideDependentLines());

    public static final Quest ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF = CHAPTER.quest("electric_arc_furnace_the_beginning_of", q -> q
            .at(2, 3)
            .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
            .title("电力高炉：一切的开端")
            .subtitle("千里之行，始于足下")
            .desc(d -> d
                    .line("上面列出了你完成一个完整的电力高炉所需要的全部材料。")
                    .line()
                    .line("随着游戏进程的发展，你会制造更为强大的机器，但他们的基础材料来源都是电力高炉。")
                    .line()
                    .line("{再次提醒}：你需要给{高炉}安装{两个}{LV能源仓}来升压到MV来满足铝的{电压要求}，直到你可以制作更高级的能源仓", AQUA, GOLD, GREEN, GOLD, YELLOW))
            .task(
                    Task.item(GTMultiMachines.ELECTRIC_BLAST_FURNACE),
                    Task.item(GTMachines.ITEM_IMPORT_BUS[GTValues.LV]).disableToast(),
                    Task.item(GTMachines.ITEM_EXPORT_BUS[GTValues.LV]).disableToast(),
                    Task.item(GTMachines.FLUID_IMPORT_HATCH[GTValues.LV]).disableToast(),
                    Task.item(GTBlocks.COIL_CUPRONICKEL, 16).disableToast(),
                    Task.item(GTMachines.MAINTENANCE_HATCH).disableToast(),
                    Task.item(GTMachines.MUFFLER_HATCH[GTValues.LV]).disableToast(),
                    Task.item(GTBlocks.CASING_INVAR_HEATPROOF, 10),
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], 2).disableToast())
            .dependsOn(COIL_BLOCK, ENERGY_HATCH, INPUT_AND_OUTPUT_OF_MATERIALS, FUNCTIONAL_CHAMBERS)
            .requireCount(1));

    public static final Quest ALUMINIUM_INGOT = CHAPTER.quest("aluminium_ingot", q -> q
            .at(5.5, 3)
            .subtitle("新家伙")
            .desc(d -> d
                    .line("吁！")
                    .line()
                    .line("恭喜你得到了MV阶段的重要金属材料！")
                    .line()
                    .line("{在进入下个阶段之前，最好检查你是否拥有足够的发电量或发电潜力}", YELLOW))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Aluminium))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Aluminium, 8))
            .dependsOn(ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF, POWER));

    public static final Quest DARK_STEEL_INGOT = CHAPTER.quest("dark_steel_ingot", q -> q
            .at(1.5, 6)
            .subtitle("爱来自EIO")
            .desc(d -> d.line("有了它之后就可以制作精密蒸汽构件了。"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.DarkSteel, 8))
            .dependsOn(ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF));

    public static final Quest PRECISION_STEAM_MECHANISM = CHAPTER.quest("precision_steam_mechanism", q -> q
            .at(2, 7.5)
            .subtitle("蒸汽时代再续！")
            .desc(d -> d
                    .line("有了它，你可以制作{大型蒸汽机器}了，这能极大地提升你的生产效率", link(LvChapter.LARGE_STEAM_CIRCUIT_ASSEMBLER))
                    .line()
                    .line("你也可以直接在{商店}中购买，或者是在探险途中找到", YELLOW))
            .task(Task.item(GTOItems.PRECISION_STEAM_MECHANISM))
            .reward(Reward.item(GTOItems.PRECISION_STEAM_MECHANISM))
            .dependsOn(DARK_STEEL_INGOT, UtilChapter.PRECISION_STEAM_MECHANISM)
            .requireAny()
            .hideDependentLines());

    public static final Quest LARGE_STEAM_CIRCUIT_ASSEMBLER = CHAPTER.quest("large_steam_circuit_assembler", q -> q
            .at(-12, -3)
            .subtitle("解放电路生产力")
            .desc(d -> d
                    .line("这个大家伙可以做到{两倍}（简单模式四倍）的产出，但它{只能执行一种配方}", DARK_GREEN, YELLOW)
                    .line()
                    .line("使用前需要{铭刻}电路，即在它的输入总线里面放置需求数量的{电路板}，然后打开主方块的GUI并且按下铭刻电路即可。", YELLOW, GOLD)
                    .line()
                    .line("你需要到达{MV}阶段才能制作它", MV))
            .task(Task.item(MultiBlockA.LARGE_STEAM_CIRCUIT_ASSEMBLER))
            .dependsOn(PRECISION_STEAM_MECHANISM, AeChapter.WORK_BLOCK_FOR_ORIGINAL_SYNTHESIS_RECIPES));

    public static final Quest MANGANESE_PHOSPHIDE_INGOT = CHAPTER.quest("manganese_phosphide_ingot", q -> q
            .at(4, 5.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.ManganesePhosphide))
            .reward(Reward.item(Items.ENDER_PEARL, 16))
            .dependsOn(ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF));

    public static final Quest LV_AIR_SCRUBBER = CHAPTER.quest("lv_air_scrubber", q -> q
            .at(2.5, 6)
            .subtitle("香甜的空气")
            .desc(d -> d
                    .line("{基础空气净化器}能净化周围{16}格内的空气，使消声仓产生灰烬的概率降低{50%}", GOLD, DARK_GREEN, DARK_GREEN)
                    .line()
                    .line("{空气净化器}的净化能力和净化范围都会随等级提升", GOLD)
                    .line()
                    .line("{若将它放在某些维度，它甚至能作为一台“矿机”……}", GRAY))
            .task(Task.item(GTMachines.AIR_SCRUBBER[GTValues.LV]))
            .dependsOn(ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF, LV_CUTTER));

    public static final Quest UPGRADE_MODULE = CHAPTER.quest("upgrade_module", q -> q
            .at(4, 1)
            .icon(TagPrefix.frameGt, GTMaterials.StainlessSteel)
            .title("附属模块")
            .subtitle("提高机器效率")
            .desc(d -> d
                    .line("{附属模块}机制是GTO的特色之一！", YELLOW.bold())
                    .line()
                    .line("在你之后获得了更好的资源之后，有一部分机器能加装{附属模块}，它们允许机器安装新的仓室（如{加速仓}，{并行控制仓}，更多的{输入输出和能源仓}），部分机器的附属模块本身还提供了{额外效果}（如通用工厂）", YELLOW, GOLD, GOLD, GOLD, AQUA)
                    .line()
                    .line("打开EMI多方块预览，点击右侧P可以查看附属模块，需要注意，{你应当把模块加装到原来机器上，而非拆除原本机器重新建造}！", YELLOW)
                    .line()
                    .line("使用{高级终端}可以指定建造附属模块", AQUA)
                    .line()
                    .line("在你进入HV获得{不锈钢}时，你可以为电力高炉安装{附属模块}", GOLD, YELLOW))
            .task(Task.item(TagPrefix.frameGt, GTMaterials.StainlessSteel, 10), Task.item(GTBlocks.CASING_STEEL_PIPE, 8))
            .dependsOn(ELECTRIC_ARC_FURNACE_THE_BEGINNING_OF, UlvChapter.SOME_PRIMITIVE_PETROCHEMICAL_PROCESSING));

    public static final Quest SHAKE_IT_HARD = CHAPTER.quest("shake_it_hard", q -> q
            .at(-13.5, 0)
            .title("用力摇！")
            .subtitle("头晕了吗？")
            .desc(d -> d
                    .line("它能处理的是{洁净矿粉}，由{研磨机}处理纯净矿石而来", LIGHT_PURPLE, GOLD)
                    .line()
                    .line("一般来说这条处理方式并不常见，所以一般情况下它是用于处理其他配方而并非着眼于矿物处理产线的")
                    .line()
                    .line("尤其要注意你的产物是由离心机还是{热力离心机}生产而成，两字之差距离远矣", link(LvChapter.NOT_ONLY_CENTRIFUGAL_BUT_ALSO_A)))
            .task(Task.item(MultiBlockA.LARGE_STEAM_CENTRIFUGE))
            .dependsOn(SteamChapter.CENTRIFUGE, PRECISION_STEAM_MECHANISM));

    public static final Quest NOT_ONLY_CENTRIFUGAL_BUT_ALSO_A = CHAPTER.quest("not_only_centrifugal_but_also_a", q -> q
            .at(-12, 0)
            .title("不仅离心，还要有点热")
            .subtitle("离心机异父异母的兄弟")
            .desc(d -> d
                    .line("它能处理的是{粉碎矿石}和{纯净矿石}，前者是直接由{研磨机}生产完成，后者是{洗矿机}处理完毕的", LIGHT_PURPLE, YELLOW, GOLD, GOLD)
                    .line()
                    .line("尤其要注意你的产物是由{离心机}还是热力离心机生产而成，两字之差距离远矣", link(SHAKE_IT_HARD)))
            .task(Task.item(MultiBlockA.LARGE_STEAM_THERMAL_CENTRIFUGE))
            .dependsOn(PRECISION_STEAM_MECHANISM, SteamChapter.CENTRIFUGE));

    public static final Quest LARGER_DRUM_WASHING_MACHINE = CHAPTER.quest("larger_drum_washing_machine", q -> q
            .at(-11.25, 1.5)
            .title("更大的滚筒洗衣机")
            .subtitle("我的蒸汽都去哪了")
            .desc(d -> d
                    .line("你肯定已经发现了，卡住你矿物处理流程的最关键一环就是{洗矿}的流程", GOLD)
                    .line()
                    .line("那么你肯定想整这个大家伙，并且让它满功率运行")
                    .line()
                    .line("然而我告诉你满功率你需要的{蒸汽}你目前绝无可能达成，所以还是不超频来运转吧", GOLD)
                    .line()
                    .line("{32并行}已经很够了，注意{输出大小}，太小的话是没办法执行某些配方的", DARK_RED, LIGHT_PURPLE))
            .task(Task.item(MultiBlockA.LARGE_STEAM_ORE_WASHER))
            .dependsOn(SteamChapter.DRUM_WASHING_MACHINE_MINERAL_SPECIFIC, PRECISION_STEAM_MECHANISM));

    public static final Quest LARGE_POOL_HIGH_CAPACITY = CHAPTER.quest("large_pool_high_capacity", q -> q
            .at(-12.75, 1.5)
            .title("大泳池，大容量")
            .subtitle("跟隔壁那个家伙怎么这么像")
            .desc(d -> d
                    .line("用法和配置需求都跟{右边那个家伙}是一样的，甚至长得也很像", link(LARGER_DRUM_WASHING_MACHINE))
                    .line()
                    .line("不过你肯定是用来处理{矿物}的，其他的配方你也不会用到的", GOLD)
                    .line()
                    .line("跟所有的大型蒸汽多方块一样，都是有着{32并行}", DARK_RED))
            .task(Task.item(MultiBlockA.LARGE_STEAM_BATH))
            .dependsOn(SteamChapter.LARGE_POOL, PRECISION_STEAM_MECHANISM));

    public static final Quest THROW_ALL_THE_STUFF_IN_AND = CHAPTER.quest("throw_all_the_stuff_in_and", q -> q
            .at(-10.5, 0)
            .title("把所有的玩意儿全丢进去烧了")
            .subtitle("以前都过的是什么苦日子？")
            .desc(d -> d
                    .line("天空传来一声巨响，{大型青铜熔炉}闪亮登场！！", GOLD)
                    .line()
                    .line("之前的{8并行}不够用？那这是{32并行}版本！", LIGHT_PURPLE, DARK_RED)
                    .line()
                    .line("{大型青铜熔炉}足以让你用到{HV}阶段，直到{工业熔炉}的并行数超过它并足以执行多次超频", GOLD, HV, GOLD)
                    .line()
                    .line("即便有1.2倍处理耗时惩罚，但在当前阶段它就是{绝对}无敌的存在", YELLOW))
            .task(Task.item(MultiBlockA.LARGE_STEAM_FURNACE))
            .dependsOn(SteamChapter.A_SLIGHTLY_LARGER_FURNACE, PRECISION_STEAM_MECHANISM));

    public static final Quest A_LARGER_CAULDRON_AND_A_BIGGER = CHAPTER.quest("a_larger_cauldron_and_a_bigger", q -> q
            .at(-12.75, -1.5)
            .title("更大的锅与更大的搅拌棍")
            .subtitle("并没有在制作魔法药水")
            .desc(d -> d
                    .line("用于大量产出你目前需要的合金粉")
                    .line()
                    .line("跟其他进阶蒸汽多方块一样的{并行、蒸汽消耗和debuff}", LIGHT_PURPLE)
                    .line()
                    .line("注意它不能执行{液体}参与搅拌的配方", GOLD))
            .task(Task.item(MultiBlockA.LARGE_STEAM_MIXER))
            .dependsOn(SteamChapter.MIXER_STEAM_POWERED, PRECISION_STEAM_MECHANISM));

    public static final Quest SMASH_THEM = CHAPTER.quest("smash_them", q -> q
            .at(-14.25, 1.5)
            .title("敲碎他们！")
            .subtitle("这位爷，这边先请")
            .desc(d -> d
                    .line("{32并行}的多方块大机器，仅有{1.2倍}的处理时长惩罚，并且可通过替换{蒸汽输入仓}来实现处理电压的{升级}", DARK_RED, YELLOW, GOLD, DARK_PURPLE)
                    .line()
                    .line("{那么古尔丹，代价是什么呢？}", DARK_RED)
                    .line()
                    .line("在你使用蒸汽输入仓的情况下需要提供{1000mb/t}的蒸汽来维持满并行处理", DARK_RED))
            .task(Task.item(MultiBlockC.LARGE_STEAM_CRUSHER))
            .dependsOn(SteamChapter.ORE_SMASHER, PRECISION_STEAM_MECHANISM));

    public static final Quest STEAM_POWER = CHAPTER.quest("steam_power", q -> q
            .at(-9.75, 1.5)
            .title("蒸汽之力")
            .subtitle("搅碎万物...吗？")
            .desc(d -> d
                    .line("这个大家伙不仅具有{32并行}，而且还可以通过升级蒸汽输入仓来产出{副产物}", YELLOW, DARK_RED)
                    .line()
                    .line("但很明显的，{大型蒸汽输入仓}你现在是获取不了的，不过到了能获取它的时候，{大型蒸汽研磨机}依然很强力", GREEN, GOLD)
                    .line()
                    .line("{记得准备一个足够大的输出总线}", DARK_GRAY))
            .task(Task.item(MultiBlockA.LARGE_STEAM_MACERATOR))
            .dependsOn(SteamChapter.LARGER_AUTO_MORTAR, PRECISION_STEAM_MECHANISM));

    public static final Quest LARGE_STEAM_SOLAR_BOILER = CHAPTER.quest("large_steam_solar_boiler", q -> q
            .at(-10.5, 3)
            .desc(d -> d
                    .line("有没有想过MC世界的太阳有多{炽烈}？", DARK_RED)
                    .line()
                    .line("{巨量的蒸汽}因为{集热管}聚集的热量正不断喷涌而出！", YELLOW, GOLD)
                    .line()
                    .line("这台多方块机器的造价比{大型青铜锅炉}更高，但它的运转是免费的，或许这很值得", GOLD))
            .task(Task.item(MultiBlockH.LARGE_STEAM_SOLAR_BOILER))
            .dependsOn(PRECISION_STEAM_MECHANISM, SteamChapter.Q_7BCD4971)
            .optional());

    public static final Quest Q_1X_MANGANESE_PHOSPHIDE_WIRE = CHAPTER.quest("q_1x_manganese_phosphide_wire", q -> q
            .at(5, 6.5)
            .icon(TagPrefix.wireGtSingle, GTMaterials.ManganesePhosphide)
            .title("1x磷化锰导线")
            .desc(d -> d
                    .line("同样也是超导体，也同样需要经过粗胚步骤")
                    .line()
                    .line("与魔法超导体不同的是，这种超导体往往是作为合成材料而使用的，铺设它们显得有些昂贵了"))
            .task(
                    Task.item(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.ManganesePhosphide),
                    Task.item(TagPrefix.wireGtSingle, GTMaterials.ManganesePhosphide))
            .dependsOn(GAS_COMPRESSOR, MANGANESE_PHOSPHIDE_INGOT));

    public static final Quest LV_FIELD_GENERATOR = CHAPTER.quest("lv_field_generator", q -> q
            .at(6.5, 6.5)
            .subtitle("最麻烦的部件")
            .desc(d -> d
                    .line("这东西做起来确实很麻烦，但等到有自动化手段了其实也能接受。")
                    .line()
                    .line("你需要做四个来搭建{工业屠宰场}。", GOLD))
            .task(Task.item(GTItems.FIELD_GENERATOR_LV))
            .dependsOn(STEEL_FIELD_GENERATOR_CASING, Q_1X_MANGANESE_PHOSPHIDE_WIRE));

    public static final Quest LV_WORLD_ACCELERATOR = CHAPTER.quest("lv_world_accelerator", q -> q
            .at(8, 8)
            .desc(d -> d.line("在某些时候有用，不过目前最大的作用还是用来组装{工业屠宰场}", GOLD))
            .task(Task.item(GTMachines.WORLD_ACCELERATOR[GTValues.LV]))
            .dependsOn(LV_FIELD_GENERATOR));

    public static final Quest SLAUGHTERHOUSE = CHAPTER.quest("slaughterhouse", q -> q
            .at(8, 9.5)
            .subtitle("众生平息")
            .desc(d -> d
                    .line("你的末影珍珠拯救者，不用再出门迫害末影人了！")
                    .line()
                    .line("产出的资源在后面都有高效的替代方法"))
            .task(Task.item(MultiBlockD.SLAUGHTERHOUSE))
            .dependsOn(LV_WORLD_ACCELERATOR));

    public static final Quest MAKE_TIME_GO_FASTER = CHAPTER.quest("make_time_go_faster", q -> q
            .at(6.5, 8)
            .title("让时间变得快起来")
            .subtitle("狠狠地加速！")
            .desc(d -> d
                    .line("按照比例无条件缩短{多方块机器}的配方处理时间", GOLD)
                    .line()
                    .line("当加速仓等级{低于}当前配方的配方等级时，加速效果将{大幅度}减弱，每低一级减少20%", AQUA, DARK_RED)
                    .line()
                    .line("{无条件就是指总耗能也变少了}", GRAY.italic()))
            .task(Task.item(GTOMachines.ACCELERATE_HATCH[GTValues.LV]))
            .dependsOn(LV_FIELD_GENERATOR));

    public static final Quest LV_COMPONENT_SUBSIDY = CHAPTER.quest("lv_component_subsidy", q -> q
            .at(-4, 0)
            .icon(GTItems.ELECTRIC_MOTOR_LV)
            .title("LV部件补助")
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(GTItems.ELECTRIC_MOTOR_LV, 24),
                    Reward.item(GTItems.CONVEYOR_MODULE_LV, 8),
                    Reward.item(GTItems.ELECTRIC_PISTON_LV, 4),
                    Reward.item(GTItems.ROBOT_ARM_LV, 8),
                    Reward.item(GTItems.ELECTRIC_PUMP_LV, 4))
            .normalSubsidy(
                    Reward.item(GTItems.ELECTRIC_MOTOR_LV, 12),
                    Reward.item(GTItems.CONVEYOR_MODULE_LV, 4),
                    Reward.item(GTItems.ELECTRIC_PISTON_LV, 4),
                    Reward.item(GTItems.ROBOT_ARM_LV, 4),
                    Reward.item(GTItems.ELECTRIC_PUMP_LV, 2))
            .dependsOn(ASSEMBLE_EVERYTHING)
            .optional().hideUntilDepsVisible());

    public static final Quest ALUMINIUM_SUBSIDY = CHAPTER.quest("aluminium_subsidy", q -> q
            .at(4.5, 4)
            .icon(TagPrefix.ingot, GTMaterials.Aluminium)
            .title("铝补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(TagPrefix.ingot, GTMaterials.Aluminium, 320L))
            .normalSubsidy(Reward.item(TagPrefix.ingot, GTMaterials.Aluminium, 56))
            .dependsOn(ALUMINIUM_INGOT)
            .optional().hideUntilDepsVisible());

    public static final Quest UPGRADE_MODULES_ACCELERATE = CHAPTER.quest("upgrade_modules_accelerate", q -> q
            .at(-1, 8)
            .title("升级模块！加速吧！！！")
            .subtitle("今日手气如何？")
            .desc(d -> d
                    .line("{右键}可使用模块的机器会消耗玩家最多{30970点}（即{100级}）的经验来获取附加属性。玩家至少需要拥有10级经验，当消耗100级经验时附加属性几率达到最大值", YELLOW, GREEN, GREEN)
                    .line()
                    .line("升级效果为永久加成，{可重复升级}。重复升级时会综合叠加新乘数和旧乘数", YELLOW)
                    .line()
                    .line("{升级模块}可以无条件给机器提供两个乘区，效果比他的兄弟增幅器{强力非常多}，因此可以应用模块的机器{较为有限}，以下为已知可以使用模块的机器：", GOLD, YELLOW, RED)
                    .line()
                    .line("- 所有单方块机器")
                    .line("- 电力高炉，反应炉，真空冷冻机和合金冶炼炉")
                    .line("- 烈焰高炉和寒冰冷冻机")
                    .line("- {“大批量加工”}章节中的机器", YELLOW)
                    .line("- 转底炉，巨型合金冶炼炉和巨型真空冷冻机")
                    .line()
                    .line("{注意：拆除机器主机会直接清空机器主机上的升级！}", RED))
            .task(Task.item(GTOItems.SPEED_UPGRADE_MODULE), Task.item(GTOItems.ENERGY_UPGRADE_MODULE))
            .reward(Reward.stack(() -> QuestStacks.aeCharged(AEItems.PORTABLE_ITEM_CELL4K, 20000.0d), 1))
            .dependsOn(ApotheosisChapter.RARITY_FORGE, LV_POWER_AMPLIFIERS));

    public static final Quest WIRELESS_CHARGER = CHAPTER.quest("wireless_charger", q -> q
            .at(8, 6.5)
            .icon(MultiBlockG.WIRELESS_CHARGER)
            .desc(d -> d
                    .line("可以给小机器和玩家无线充电，放入{HV力场发生器}之后配合{无线充能覆盖版}可以给方块充电", AQUA, GOLD)
                    .line()
                    .line("点{这里}获取覆盖版用法", link(TipsChapter.TANGIBLE_SMALL_BOARD)))
            .task(Task.item(MultiBlockG.WIRELESS_CHARGER), Task.item(GTOItems.WIRELESS_CHARGER_COVER).optional())
            .dependsOn(LV_FIELD_GENERATOR));

    public static final Quest LV_DUAL_INPUT_HATCH = CHAPTER.quest("lv_dual_input_hatch", q -> q
            .at(8, 5)
            .desc(d -> d.line("总成可以让你{同时输入物品和流体}，这在你后续使用AE下单的时候非常有用", YELLOW))
            .task(Task.item(GTMachines.DUAL_IMPORT_HATCH[GTValues.LV]))
            .dependsOn(LV_FIELD_GENERATOR));

    public static final Quest LV_LASER_ENGRAVER = CHAPTER.quest("lv_laser_engraver", q -> q
            .at(7, -8.5)
            .desc(d -> d
                    .line("{激光蚀刻机}在此阶段并不是必需的机器", GOLD)
                    .line()
                    .line("它主要用于制作晶圆，以及无暇/精致宝石")
                    .line()
                    .line("在普通/专家模式下，{MV}阶段还需要用它制作AE电路板", MV)
                    .line()
                    .line("{此处仅用于介绍激光蚀刻机这一机器类型，不代表必须制作LV版本}", GRAY.italic()))
            .task(Task.item(GTMachines.LASER_ENGRAVER[GTValues.LV]))
            .dependsOn(LV_EMITTER));

    public static final Quest LV_DIODE = CHAPTER.quest("lv_diode", q -> q
            .at(-4.5, 3)
            .desc(d -> d.line("能够{限制电流大小}，避免传输电流过大导致烧毁导线", YELLOW))
            .task(Task.item(GTMachines.DIODE[GTValues.LV]))
            .dependsOn(DIODE)
            .optional());

    public static final Quest PREPARING_TO_BEGIN_CHEMICAL_ENGINEERING = CHAPTER.quest("preparing_to_begin_chemical_engineering", q -> q
            .at(-9, 5.5)
            .icon(GTItems.WETWARE_CIRCUIT_BOARD)
            .title("准备开始...化工！")
            .desc(d -> d
                    .line("聚乙烯的合成只是小打小闹，现在你将迎来你的第一条化工产线--{酚醛树脂印刷电路基板}产线", GOLD)
                    .line()
                    .line("准备好你的化学反应釜！")
                    .line()
                    .line("若化学反应釜不多，建议每个配方多生产一些，然后切换配方生产下一种产物，避免来回倒腾的麻烦")
                    .line()
                    .line("当然我更推荐你为每个配方准备一个化学反应釜，然后用管道将它们串在一起，形成一条自动化生产电路基板的产线！"))
            .task(Task.checkmark())
            .dependsOn(BLACK_BOX_REACTOR)
            .hideDependencyLines());

    public static final Quest PHENOL_BUCKET = CHAPTER.quest("phenol_bucket", q -> q
            .at(-7.5, 5.5)
            .icon(GTMaterials.Phenol.getBucket())
            .desc(d -> d.line("早期可以通过{异丙苯，二氯苯}等方式制取", GOLD))
            .task(Task.item(GTMaterials.Phenol.getBucket()).optional(), Task.checkmark())
            .dependsOn(UlvChapter.SOME_PRIMITIVE_PETROCHEMICAL_PROCESSING, PREPARING_TO_BEGIN_CHEMICAL_ENGINEERING));

    public static final Quest PHENOLIC_RESIN_BUCKET = CHAPTER.quest("phenolic_resin_bucket", q -> q
            .at(-6.5, 5.5)
            .icon(GTOMaterials.PhenolicResin.getBucket())
            .task(Task.item(GTOMaterials.PhenolicResin.getBucket()).optional(), Task.checkmark())
            .dependsOn(PHENOL_BUCKET, FORMALDEHYDE_BUCKET));

    public static final Quest PHENOLIC_CIRCUIT_BOARD = CHAPTER.quest("phenolic_circuit_board", q -> q
            .at(-5.5, 5.5)
            .task(Task.item(GTItems.PHENOLIC_BOARD))
            .dependsOn(PHENOLIC_RESIN_BUCKET, SULFURIC_ACID_BUCKET));

    public static final Quest PHENOLIC_PRINTED_CIRCUIT_BOARD = CHAPTER.quest("phenolic_printed_circuit_board", q -> q
            .at(-4.5, 5.5)
            .desc(d -> d.line("氯化铁和过硫酸钠二选一，这2个材料推荐搭建一个连续生产线，后面用量很大"))
            .task(Task.item(GTItems.GOOD_CIRCUIT_BOARD))
            .dependsOn(PHENOLIC_CIRCUIT_BOARD));

    public static final Quest GOOD_ELECTRONIC_CIRCUIT = CHAPTER.quest("good_electronic_circuit", q -> q
            .at(-3, 5.5)
            .title("{优质电子电路}", RED)
            .subtitle("更上一层楼")
            .desc(d -> d
                    .line("这是你的第二块电路板")
                    .line()
                    .line("手动合成两个，制作出{2个}{MV级电路}后你就可以使用{电路组装机}大量增产电路！", DARK_GREEN, GOLD, GOLD))
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_MV))
            .reward(Reward.item(GTItems.ELECTRONIC_CIRCUIT_MV, 2), Reward.item(QuestExternalRefs.FarmersDelightItems.HAMBURGER, 8))
            .dependsOn(DIODE, PHENOLIC_PRINTED_CIRCUIT_BOARD));

    public static final Quest LV_CIRCUIT_ASSEMBLER = CHAPTER.quest("lv_circuit_assembler", q -> q
            .at(-3, 8)
            .subtitle("电路自动化！！")
            .desc(d -> d
                    .line("{电路组装机}是每个阶段最贵也是最值得的机器，它能将你之前的手动合成电路板的成本降低为一半以下", GOLD)
                    .line()
                    .line("从现在起你后面的几乎所有的电路都需要在{电路组装机}上组装，直到电路装配线", GOLD)
                    .line()
                    .line("{有的配方中需要}{额外的条件}{，这些都需要满足，否则电路组装机是不会执行配方的}", YELLOW, GOLD, YELLOW))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LV]))
            .dependsOn(GOOD_ELECTRONIC_CIRCUIT));

    public static final Quest CHIP_SUBSIDY = CHAPTER.quest("chip_subsidy", q -> q
            .at(-4, 6.5)
            .icon(GTItems.ELECTRONIC_CIRCUIT_MV)
            .title("芯片补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_MV, 16))
            .normalSubsidy(Reward.item(GTItems.ELECTRONIC_CIRCUIT_MV, 6))
            .dependsOn(GOOD_ELECTRONIC_CIRCUIT)
            .optional().hideUntilDepsVisible());

    public static final Quest LIQUID_FUEL_JETPACK = CHAPTER.quest("liquid_fuel_jetpack", q -> q
            .at(3, -4)
            .desc(d -> d
                    .line("厌倦了私人飞机的旅行？")
                    .line()
                    .line("试试{Iron} {Man}！", DARK_RED, YELLOW)
                    .line()
                    .line("{如何补充燃料：右击你的燃料储存桶}", GRAY)
                    .line()
                    .line("含硫轻燃油获取方便，可作为其燃料之一"))
            .task(Task.item(GTItems.LIQUID_FUEL_JETPACK))
            .reward(Reward.item(QuestExternalRefs.FarmersDelightItems.HONEY_GLAZED_HAM))
            .dependsOn(LV_ELECTRIC_PUMP)
            .optional());

    public static final Quest HEAD_TO_THE_VOID_AND_THE = CHAPTER.quest("head_to_the_void_and_the", q -> q
            .at(0.5, 8)
            .icon(QuestTextures.NETHER_PORTAL)
            .title("前往虚空和超平坦世界{（简单）}", GREEN)
            .desc(d -> d
                    .line("在简单模式下，你可以提前进入虚空和超平坦世界")
                    .line()
                    .line("虚空传送器没有电压需求，需要充满能量后才可使用。{16安LV能源仓}刚好满足它的最低功率需求", GOLD)
                    .line()
                    .line("虚空传送器的具体使用方法请参考位于HV章节的{虚空传送器}任务", link(HvChapter.VOID_TRANSPORTER)))
            .task(Task.checkmark())
            .reward(Reward.item(MultiBlockG.VOID_TRANSPORTER))
            .dependsOn(UtilChapter.Q_056EC4E6, LV_ENERGY_INPUT_HATCH_16A, DARK_STEEL_INGOT)
            .optional().hideUntilDepsVisible());

    public static final Quest LV_CANNER = CHAPTER.quest("lv_canner", q -> q
            .at(-1, -7)
            .task(Task.item(GTMachines.CANNER[GTValues.LV]))
            .dependsOn(LV_ELECTRIC_MOTOR));

    public static final Quest ANY_LV = CHAPTER.quest("any_lv", q -> q
            .at(-2.5, -7)
            .icon(GTItems.BATTERY_HULL_LV)
            .task(Task.tag(CustomTags.LV_BATTERIES, 1).title("小型电池"))
            .dependsOn(LV_CANNER));

    public static final Quest LV_BATTERY_BUFFER_16X = CHAPTER.quest("lv_battery_buffer_16x", q -> q
            .at(-4.5, -5.5)
            .task(Task.item(GTMachines.BATTERY_BUFFER_16[GTValues.LV]))
            .dependsOn(ANY_LV));

    public static final Quest ANY_DRILLS = CHAPTER.quest("any_drills", q -> q
            .at(-4.5, -7.5)
            .iconStack(
                    () -> QuestStacks.charged(ToolHelper.get(GTToolType.DRILL_LV, GTMaterials.Steel), 2147483647L, 2147483647L))
            .desc(d -> d
                    .line("低压时代你可以使用可重复充电的电钻来挖矿，{电钻}有{4x}和{2^电压等级}的连锁数量加成", GOLD, DARK_GREEN, DARK_GREEN)
                    .line()
                    .line("你也可以考虑直到做出{采矿机}再说", GOLD))
            .task(Task.tag(CustomTags.DRILLS, 1).title("电钻").icon(ToolHelper.get(GTToolType.DRILL_MV, GTMaterials.Steel).getItem()))
            .dependsOn(ANY_LV));

    public static final Quest ANY_CHAINSAWS = CHAPTER.quest("any_chainsaws", q -> q
            .at(-4.5, -6.5)
            .iconStack(
                    () -> QuestStacks.charged(ToolHelper.get(GTToolType.CHAINSAW_LV, GTMaterials.Steel), 2147483647L, 2147483647L))
            .desc(d -> d
                    .line("如果你需要大量原木，制作一把链锯是不错的选择")
                    .line()
                    .line("支线挑战：{DreamMasterXXL}希望你去砍光一片森林群系，他会给你一个灿烂的笑容作为奖励", AQUA))
            .task(Task.tag(CustomTags.CHAINSAWS, 1).title("链锯"))
            .dependsOn(ANY_LV));

    public static final Quest PROSPECTOR_LV = CHAPTER.quest("prospector_lv", q -> q
            .at(-4.5, -8.5)
            .desc(d -> d
                    .line("最低级的探矿仪，只能勘探实体矿脉")
                    .line()
                    .line("可以查看探矿仪的EMI来获取矿脉信息"))
            .task(Task.item(GTItems.PROSPECTOR_LV))
            .dependsOn(ANY_LV));

    public static final Quest YOUR_SECOND_CABLE = CHAPTER.quest("your_second_cable", q -> q
            .at(7, -1)
            .title("你的第二条线缆")
            .subtitle("但实际上是作为合成材料")
            .desc(d -> d.line("铜线缆的作用也仅仅只是合成罢了，为什么不去看看这个阶段的{超导体}呢？", link(LV_SUPERCONDUCTOR)))
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Copper))
            .reward(Reward.item(TagPrefix.cableGtSingle, GTMaterials.Copper, 16))
            .dependsOn(RUBBER_BATH_MACHINE, RUBBER_BUCKET));

    public static final Quest WIRELESS_ITEM_TRANSFER_COVER = CHAPTER.quest("wireless_item_transfer_cover", q -> q
            .at(7, -5.5)
            .icon(CustomItems.WIRELESS_ITEM_TRANSFER_COVER)
            .desc(d -> d
                    .line("这是低压时代你能制作的无线P2P物流，拥有超模的传输速率")
                    .line()
                    .line("你还需要{基础切割机}得到{末影珍珠板}", GOLD, GOLD)
                    .line()
                    .line("点{这里}获取覆盖版用法", link(TipsChapter.TANGIBLE_SMALL_BOARD))
                    .line()
                    .line("{进入高压时代，你还能制作对应的高级版本}", GRAY))
            .task(Task.item(CustomItems.WIRELESS_ITEM_TRANSFER_COVER), Task.item(CustomItems.WIRELESS_FLUID_TRANSFER_COVER))
            .dependsOn(LV_EMITTER, LV_SENSOR, LV_CUTTER, LV_FLUID_REGULATOR));

    public static final Quest LARGE_STEAM_FORGE_HAMMER = CHAPTER.quest("large_steam_forge_hammer", q -> q
            .at(-11.25, -1.5)
            .task(Task.item(MultiBlockA.LARGE_STEAM_FORGE_HAMMER))
            .dependsOn(SteamChapter.THE_HAMMER_FALLS));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(MvChapter.WELCOME_TO_THE_MEDIUM_VOLTAGE_STAGE, 8, 3))
                .link(QuestLink.of(AeChapter.GETTING_STARTED_WITH_AE, -4.5, 4.25))
                .link(QuestLink.of(UlvChapter.DIGITAL_MINER, -12, 6))
                .link(QuestLink.of(Magic2Chapter.PULSE_MACHINE_MAINTENANCE_PEDESTAL, -6, 3.5)));
    }
}
