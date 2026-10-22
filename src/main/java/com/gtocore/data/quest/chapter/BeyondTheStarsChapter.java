package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.player.OrganTier;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class BeyondTheStarsChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("beyond_the_stars", c -> c
            .title("冯虚御风")
            .subtitle("Voyage to the Void")
            .icon(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_7_ROCKET.get())
            .hideQuestsUntilDepsVisible()
            .flexible()
            .autofocus(BeyondTheStarsChapter.STARGAZING)
            .background(QuestTextures.BEYONDSTARS, 0x555555));

    public static final Quest STARGAZING = CHAPTER.quest("stargazing", q -> q
            .at(-4.5, -4.5)
            .icon(ModItems.NASA_WORKBENCH.get())
            .title("仰望星空")
            .subtitle("冲上云霄！")
            .desc(d -> d
                    .line("欢迎来到本章节！在这里你将游历每个星球{（然后把它们的矿全部挖走）}", STRIKE)
                    .line()
                    .line("章节左边按照火箭的顺序列出了所有的火箭燃料，右边则是列出了火箭及其对应能到达的星球")
                    .line()
                    .line("格雷科技™有限公司希望你能将本公司的业务拓展到地球之外，所以请你{认真对待}公司的资产，{严禁出现火箭坠机、憋死异星或者是燃料不足等现象}", DARK_PURPLE, DARK_RED.bold())
                    .line()
                    .line("若出现上述情况，公司将{切断}你的AE系统能源供应作为惩罚", DARK_RED)
                    .line()
                    .line("{上述惩罚从未执行过也不会执行}", DARK_GRAY.italic()))
            .task(Task.checkmark())
            .dependsOn(HvChapter.HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest HEAVY_DUTY_PLATE_1 = CHAPTER.quest("heavy_duty_plate_1", q -> q
            .at(-1.5, -3)
            .subtitle("火箭的新衣")
            .desc(d -> d
                    .line("你总共需要22个完成{一阶火箭}的准备与建造，分别用于：", GOLD)
                    .line()
                    .line("火箭鼻锥，2个")
                    .line("火箭尾翼，4个")
                    .line("合成一阶火箭，12个")
                    .line("火箭装配机，4个")
                    .line()
                    .line("以及大量的TNT，你以后会要很多的"))
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_1))
            .dependsOn(HvChapter.IMPLOSION_COMPRESSOR)
            .disableToast());

    public static final Quest ROCKET_ASSEMBLER = CHAPTER.quest("rocket_assembler", q -> q
            .at(-2.6, -1.5)
            .subtitle("我的火箭老家")
            .desc(d -> d
                    .line("所有的火箭（除了7阶火箭）都从此诞生")
                    .line()
                    .line("结构有点大，但实际做出来的火箭并不大"))
            .task(Task.item(MultiBlockG.ROCKET_ASSEMBLER))
            .dependsOn(STARGAZING, HEAVY_DUTY_PLATE_1)
            .disableToast());

    public static final Quest STEEL_TANK = CHAPTER.quest("steel_tank", q -> q
            .at(-5.5, 0.5)
            .task(Task.item(ModItems.STEEL_TANK.get()))
            .dependsOn(STARGAZING)
            .hideDependencyLines());

    public static final Quest ROCKET_NOSE_CONE = CHAPTER.quest("rocket_nose_cone", q -> q
            .at(-5.5, -1)
            .task(Task.item(ModItems.ROCKET_NOSE_CONE.get()))
            .dependsOn(STARGAZING)
            .hideDependencyLines());

    public static final Quest ROCKET_FIN = CHAPTER.quest("rocket_fin", q -> q
            .at(-5.5, 3.5)
            .task(Task.item(ModItems.ROCKET_FIN.get()))
            .dependsOn(STARGAZING)
            .hideDependencyLines());

    public static final Quest STEEL_ENGINE = CHAPTER.quest("steel_engine", q -> q
            .at(-5.5, 2)
            .task(Task.item(ModItems.STEEL_ENGINE.get()))
            .dependsOn(STEEL_TANK));

    public static final Quest LAUNCH_PAD = CHAPTER.quest("launch_pad", q -> q
            .at(-3.5, 3.5)
            .task(Task.item(ModItems.LAUNCH_PAD.get()))
            .dependsOn(STARGAZING)
            .hideDependencyLines());

    public static final Quest A_SPARK_IGNITES = CHAPTER.quest("a_spark_ignites", q -> q
            .at(-1.25, 1)
            .title("微火初燃")
            .subtitle("这里是T1火箭")
            .desc(d -> d.line("现在你可以前往月球和地球空间站了"))
            .task(Task.item(ModItems.TIER_1_ROCKET.get()))
            .dependsOn(ROCKET_ASSEMBLER, ROCKET_NOSE_CONE, STEEL_TANK, STEEL_ENGINE, ROCKET_FIN, LAUNCH_PAD));

    public static final Quest PLANET_DATA_CHIP = CHAPTER.quest("planet_data_chip", q -> q
            .at(1.5, -3)
            .desc(d -> d
                    .line("{等级：}{殖民级 - 战略资产}", YELLOW, GOLD)
                    .line()
                    .line("此芯片是{「行星扫描」}任务的最终产物，内部以非易失性存储方式，记录了目标天体（如月球）的完整重力场图、光谱矿物分布、地形高程模型及潜在着陆点危险评估数据。", YELLOW)
                    .line()
                    .line("{警告}{：此芯片为单一副本，是规划地外任务的核心。遗失或损坏将导致整个扫描任务失效。}", RED, YELLOW))
            .task(Task.item(GTOItems.PLANET_DATA_CHIP))
            .dependsOn(HvChapter.HV_LAUNCHER_AND_HV_SENSOR, HvChapter.NOR_MEMORY_CHIP, HvChapter.SIMPLE_SOC, HvChapter.DATA_STICK)
            .disableToast());

    public static final Quest PLANET_SCAN_SATELLITE = CHAPTER.quest("planet_scan_satellite", q -> q
            .at(3.5, -3)
            .desc(d -> d
                    .line("{行星扫描卫星 - GT-PSS-1 型}", GOLD)
                    .line()
                    .line("本设备是深空探索的先驱，是迈向地外殖民的第一步。")
                    .line()
                    .line("由高精度HV传感器阵列、高扭矩HV机械臂与高效光伏板材构成，该卫星能在极端环境中稳定运行。其核心的电力推进器确保了其能被准确送入环月轨道，并对月球地表进行全面光谱分析与矿物分布测绘。")
                    .line()
                    .line("{警告}{：任何跨维度太空任务，在未收到该卫星传回的完整行星扫描数据前，}{均被视为自杀行为}", RED, YELLOW, RED))
            .task(Task.item(GTOItems.PLANET_SCAN_SATELLITE))
            .dependsOn(HvChapter.HV_LAUNCHER_AND_HV_SENSOR)
            .disableToast());

    public static final Quest SATELLITE_CONTROL_CENTER = CHAPTER.quest("satellite_control_center", q -> q
            .at(2.5, -1.5)
            .subtitle("我要去哪儿？")
            .desc(d -> d
                    .line("要实现载人登月这一航天壮举，必须遵循循序渐进的科学探索规律！")
                    .line()
                    .line("在正式派遣你自己登陆{月球}之前，你需要构建完整的\"无人探测-载人验证-常驻开发\"技术发展体系", YELLOW)
                    .line()
                    .line("其中向{月球}发射携带{星球数据芯片}的{行星扫描卫星}是基础性战略步骤，无人探月工程能系统性获取地月空间环境参数、月球重力场数据、辐射剂量谱分布等关键信息，为后续载人登月提供数据支撑。", YELLOW, GOLD, GOLD)
                    .line()
                    .line("你必须先向{月球}发射{卫星}以拿到{月球数据}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("顺带一提，卫星控制中心UI左下角的HUD控制是控制航天服的氧气显示的"))
            .task(Task.item(MultiBlockG.SATELLITE_CONTROL_CENTER))
            .dependsOn(HvChapter.HV_LAUNCHER_AND_HV_SENSOR, PLANET_DATA_CHIP, PLANET_SCAN_SATELLITE));

    public static final Quest FIND_THE_MOON = CHAPTER.quest("find_the_moon", q -> q
            .at(2.5, 1)
            .icon(ModItems.EARTH_GLOBE.get())
            .title("寻找月球")
            .subtitle("找到你了！")
            .desc(d -> d
                    .line("将{16B火箭燃料}，{一阶火箭}，{行星扫描卫星}，{星球数据芯片}放入{卫星控制中心}，调整等级为{1}，点击{启程}，等待火箭返回，带回{月球数据}，使用{扫描仪}扫描数据后{解锁月球}", GOLD, GOLD, GOLD, GOLD, GOLD, DARK_GREEN, YELLOW, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{那么恭喜你，你已经具备了载人登月所需的一切条件！}", YELLOW))
            .task(Task.checkmark())
            .dependsOn(SATELLITE_CONTROL_CENTER, A_SPARK_IGNITES, HvChapter.ROCKET_FUEL_BUCKET, HvChapter.TIER_1_ROCKET));

    public static final Quest ROCKET_FUEL = CHAPTER.quest("rocket_fuel", q -> q
            .at(-7, -3.15)
            .icon(ModItems.ETRIONIC_CORE.get())
            .title("火箭燃料")
            .subtitle("这可不能吨吨吨")
            .desc(d -> d
                    .line("火箭燃料一来可以供给{火箭}使用，二来可以用于{发电}，三来可以用于{太空采矿}和{太空钻井}。{各级火箭燃料之间不通用}", DARK_RED, GOLD, LIGHT_PURPLE, DARK_PURPLE, YELLOW)
                    .line()
                    .line("发电和太空采矿/钻井的最佳选择一般是用{偏二甲肼-四氧化二氮}（也就是俗称的绿油）", DARK_GREEN))
            .task(Task.checkmark())
            .dependsOn(STARGAZING));

    public static final Quest HYDROGEN_BUCKET = CHAPTER.quest("hydrogen_bucket", q -> q
            .at(-11, -5)
            .icon(ModItems.HYDROGEN_BUCKET.get())
            .task(Task.item(ModItems.HYDROGEN_BUCKET.get()).optional(), Task.checkmark()));

    public static final Quest NITROGEN_BUCKET = CHAPTER.quest("nitrogen_bucket", q -> q
            .at(-9, -5)
            .icon(GTMaterials.Nitrogen.getBucket())
            .task(Task.item(GTMaterials.Nitrogen.getBucket()).optional(), Task.checkmark()));

    public static final Quest AMMONIA_BUCKET = CHAPTER.quest("ammonia_bucket", q -> q
            .at(-10, -3.5)
            .icon(GTMaterials.Ammonia.getBucket())
            .task(Task.item(GTMaterials.Ammonia.getBucket()).optional(), Task.checkmark())
            .dependsOn(NITROGEN_BUCKET, HYDROGEN_BUCKET));

    public static final Quest CARBON_DIOXIDE_BUCKET = CHAPTER.quest("carbon_dioxide_bucket", q -> q
            .at(-13, -5)
            .task(Task.item(GTMaterials.CarbonDioxide.getBucket()), Task.item(GTMaterials.CarbonMonoxide.getBucket())));

    public static final Quest METHANOL_BUCKET = CHAPTER.quest("methanol_bucket", q -> q
            .at(-12, -3.5)
            .icon(GTMaterials.Methanol.getBucket())
            .task(Task.item(GTMaterials.Methanol.getBucket()).optional(), Task.checkmark())
            .dependsOn(HYDROGEN_BUCKET, CARBON_DIOXIDE_BUCKET));

    public static final Quest CHLORINE_BUCKET = CHAPTER.quest("chlorine_bucket", q -> q
            .at(-15, -2)
            .icon(GTMaterials.Chlorine.getBucket())
            .task(Task.item(GTMaterials.Chlorine.getBucket()).optional(), Task.checkmark()));

    public static final Quest MERCURY_BUCKET = CHAPTER.quest("mercury_bucket", q -> q
            .at(-14, -3.5)
            .icon(GTMaterials.Mercury.getBucket())
            .task(Task.item(GTMaterials.Mercury.getBucket()).optional(), Task.checkmark()));

    public static final Quest HYPOCHLOROUS_ACID_BUCKET = CHAPTER.quest("hypochlorous_acid_bucket", q -> q
            .at(-13, -2)
            .icon(GTMaterials.HypochlorousAcid.getBucket())
            .task(Task.item(GTMaterials.HypochlorousAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(MERCURY_BUCKET, CHLORINE_BUCKET));

    public static final Quest DIMETHYLHYDRAZINE_BUCKET = CHAPTER.quest("dimethylhydrazine_bucket", q -> q
            .at(-11, -2)
            .icon(GTMaterials.Dimethylhydrazine.getBucket())
            .task(Task.item(GTMaterials.Dimethylhydrazine.getBucket()).optional(), Task.checkmark())
            .dependsOn(METHANOL_BUCKET, HYPOCHLOROUS_ACID_BUCKET, AMMONIA_BUCKET));

    public static final Quest T1_ROCKET_FUEL_ROCKET_FUEL = CHAPTER.quest("t1_rocket_fuel_rocket_fuel", q -> q
            .at(-9, -2)
            .icon(GTMaterials.RocketFuel.getBucket())
            .title("T1火箭燃料：火箭燃料")
            .subtitle("圆梦时刻！")
            .desc(d -> d
                    .line("最初级的火箭燃料，你也可以借此来搭建化工相关的产线")
                    .line()
                    .line("显而易见的是你需要很多很多的氯，你可以从盐矿中获取它们"))
            .task(Task.item(GTMaterials.RocketFuel.getBucket()).optional(), Task.checkmark())
            .dependsOn(DIMETHYLHYDRAZINE_BUCKET, ROCKET_FUEL));

    public static final Quest BASIC_SPACE_SUIT = CHAPTER.quest("basic_space_suit", q -> q
            .at(5, 3)
            .title("基础宇航套装")
            .subtitle("史蒂夫的一小步")
            .desc(d -> d
                    .line("月球{缺少}大气环境", DARK_RED)
                    .line()
                    .line("你必须身着{充有氧气的}{宇航套装}，不然会有不幸的事情发生", YELLOW, GOLD)
                    .line()
                    .line("往{航天服}内通入氧气——就把{航天服}当成桶，从AE里面舀就行了", GOLD, GOLD)
                    .line()
                    .line("{宇}{航}{服}{可}{以}{染}{色}，不会影响任务提交", DARK_GREEN, GOLD, BLUE, YELLOW, LIGHT_PURPLE, AQUA, DARK_RED))
            .task(
                    Task.item(ModItems.SPACE_HELMET.get()),
                    Task.item(ModItems.SPACE_SUIT.get()),
                    Task.item(ModItems.SPACE_PANTS.get()),
                    Task.item(ModItems.SPACE_BOOTS.get()))
            .dependsOn(HvChapter.UNLOCK_MOON, SteamChapter.MORPHING)
            .requireAny());

    public static final Quest FLESH_IS_WEAK_MACHINERY_ASCENDS = CHAPTER.quest("flesh_is_weak_machinery_ascends", q -> q
            .at(5, -1)
            .title("血肉苦弱，机械飞升！")
            .subtitle("我们终将决定我们自己的进化道路")
            .desc(d -> d
                    .line("你需要把你的身体的各个{器官}都强化到{标准级}之后才能抵御{星际乱流}的影响", GOLD, YELLOW, LIGHT_PURPLE)
                    .line()
                    .line("用{器官修改器}来把各个{器官部件}装进你的身体里面", GOLD, GOLD)
                    .line()
                    .line("当然以后你每去一个新地方都需要查看自己身上的{器官}是不是已经强化到对应的阶级了", GOLD)
                    .line()
                    .line("{自己给自己在清醒时做手术，可真的是有点惊悚呢}", DARK_GRAY.italic()))
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.STANDARD)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.STANDARD)))
            .reward(Reward.item(GTOOrganItems.ORGAN_MODIFIER))
            .dependsOn(HvChapter.UNLOCK_MOON, HvChapter.MV_FIELD_GENERATOR)
            .requireAny());

    public static final Quest GREG_TO_THE_MOON = CHAPTER.quest("greg_to_the_moon", q -> q
            .at(8, 1).iconScale(1.5)
            .icon(ModItems.MOON_GLOBE.get())
            .title("格雷奔月")
            .subtitle("格雷科技的一小步")
            .desc(d -> d
                    .line("现在，朝着亲爱的月球进发？不不不，还有几件事需要你{再确认一下}：", RED)
                    .line()
                    .line("首先在出发之前，要穿戴好{宇航套装}，以及把你身上的{器官}都强化到{标准级}", GOLD, GOLD, YELLOW)
                    .line()
                    .line("其次把你的背包腾出一定的空间，记住{公司}派你去月球是去{挖矿}的，不是去{旅游}的", DARK_GREEN, YELLOW, YELLOW)
                    .line()
                    .line("最后着陆前注意提前{打开助推器减速}，否则在有不幸的事情发生前就会{有更不幸的事情发生}", YELLOW, DARK_RED)
                    .page()
                    .line("以下是矿石资源分布报告：")
                    .line()
                    .line("{锡石矿脉：}{锡}—{锡石}", rgb(0x89847E), rgb(0xFAFEFF), rgb(0x89847E))
                    .line("{铝土矿脉：铝土矿}—{钛铁矿}—{铝}", rgb(0xCFB853), rgb(0x2B2A24), rgb(0x7DB9D8))
                    .line("{磁铁矿脉：磁铁矿}—{钒磁铁矿}—{金}", rgb(0x9D9D9D), rgb(0x505D70), rgb(0xFDF55F))
                    .line("{沥青铀矿脉：沥青铀矿}—{晶质铀矿}", rgb(0xFFD647), rgb(0xFFD52E))
                    .line("{独居石矿脉：氟碳镧铈矿}—{独居石}—{钕}", rgb(0xCAAB60), rgb(0xD0EE98), rgb(0x6C5863))
                    .line("{皂滑矿脉：皂石}—{滑石}—{海绿石砂}—{镍黄铁矿}", rgb(0x5A7261), rgb(0xEBFFE9), rgb(0x1DA351), rgb(0xE3CF13))
                    .line("{锡石榴石矿脉：}{锡石矿砂}—{石榴石砂}—{石棉}—{硅藻土}", rgb(0xCC4C25), rgb(0x89847E), rgb(0xCC4C25), rgb(0xE6E6E6), rgb(0xFFFAFA)))
            .task(Task.dimension(Dimension.MOON))
            .reward(Reward.item(ModItems.MOON_GLOBE.get()))
            .dependsOn(HvChapter.ROCKET_FUEL_BUCKET, FLESH_IS_WEAK_MACHINERY_ASCENDS, BASIC_SPACE_SUIT, FIND_THE_MOON));

    public static final Quest FLUID_SPECIALTY_MOON = CHAPTER.quest("fluid_specialty_moon", q -> q
            .at(8, -1)
            .title("流体特产：月球")
            .subtitle("液态氦与氦-3")
            .desc(d -> d
                    .line("{氦-3}主要用于聚变反应", YELLOW)
                    .line()
                    .line("{气态氦}液化成的{液态氦}是最重要的冷却剂，而{气态氦}本身也有很多重要用途，在这里直接采集可以大大减少离心空气的需求量", YELLOW, GOLD, YELLOW))
            .task(Task.item(GTMaterials.Helium3.getBucket()), Task.item(GTMaterials.Helium.getBucket()))
            .dependsOn(GREG_TO_THE_MOON));

    public static final Quest MORTAL_FLESH_DECAYS_GENES_REFORGED = CHAPTER.quest("mortal_flesh_decays_genes_reforged", q -> q
            .at(27, -1)
            .title("凡躯易朽，基因重塑")
            .subtitle("自然选择？那已是过时的旧神。从今日起，我们执掌生命之笔，谱写属于自己的基因序列。")
            .desc(d -> d.line("这里是Tier2器官，它可以带你去{金星}、{水星}和{谷神星}", YELLOW, DARK_PURPLE, DARK_GRAY))
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.REINFORCED)))
            .dependsOn(EvChapter.EV_EMITTER, EvChapter.EV_SENSOR, EvChapter.HV_FIELD_GENERATOR)
            .hideDependencyLines());

    public static final Quest KEROSENE_BUCKET = CHAPTER.quest("kerosene_bucket", q -> q
            .at(-13, 1)
            .icon(GTOMaterials.Kerosene.getBucket())
            .task(Task.item(GTOMaterials.Kerosene.getBucket()).optional(), Task.checkmark()));

    public static final Quest RP_1_BUCKET = CHAPTER.quest("rp_1_bucket", q -> q
            .at(-11, 1)
            .icon(GTOMaterials.Rp1.getBucket())
            .task(Task.item(GTOMaterials.Rp1.getBucket()).optional(), Task.checkmark())
            .dependsOn(KEROSENE_BUCKET));

    public static final Quest HIGH_PRESSURE_OXYGEN_BUCKET = CHAPTER.quest("high_pressure_oxygen_bucket", q -> q
            .at(-12, -0.5)
            .icon(GTOMaterials.HighPressureOxygen.getBucket())
            .task(Task.item(GTOMaterials.HighPressureOxygen.getBucket()).optional(), Task.checkmark()));

    public static final Quest LIQUID_OXYGEN_BUCKET = CHAPTER.quest("liquid_oxygen_bucket", q -> q
            .at(-10, -0.5)
            .icon(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket())
            .task(Task.item(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket()).optional(), Task.checkmark())
            .dependsOn(HIGH_PRESSURE_OXYGEN_BUCKET));

    public static final Quest T2_ROCKET_FUEL_ROCKET_FUEL_RP = CHAPTER.quest("t2_rocket_fuel_rocket_fuel_rp", q -> q
            .at(-9, 1)
            .icon(GTOMaterials.RocketFuelRp1.getBucket())
            .title("T2火箭燃料：火箭燃料RP-1")
            .subtitle("让我们把东方一号送上天")
            .desc(d -> d
                    .line("液氧-煤油体系是最常见的火箭发射燃料组合了")
                    .line()
                    .line("你需要搭建一条前期的木化产线来满足你的燃料生产需求"))
            .task(Task.item(GTOMaterials.RocketFuelRp1.getBucket()).optional(), Task.checkmark())
            .dependsOn(RP_1_BUCKET, LIQUID_OXYGEN_BUCKET, T1_ROCKET_FUEL_ROCKET_FUEL));

    public static final Quest ANTHRACENE_BUCKET = CHAPTER.quest("anthracene_bucket", q -> q
            .at(-12.5, 2.5)
            .icon(GTOMaterials.Anthracene.getBucket())
            .task(Task.item(GTOMaterials.Anthracene.getBucket()).optional(), Task.checkmark()));

    public static final Quest POTASSIUM_DICHROMATE_DUST = CHAPTER.quest("potassium_dichromate_dust", q -> q
            .at(-19.5, 1)
            .task(Task.item(TagPrefix.dust, GTMaterials.PotassiumDichromate)));

    public static final Quest DIMETHYLBENZENE_BUCKET = CHAPTER.quest("dimethylbenzene_bucket", q -> q
            .at(-17.5, 1)
            .icon(GTMaterials.Dimethylbenzene.getBucket())
            .task(Task.item(GTMaterials.Dimethylbenzene.getBucket()).optional(), Task.checkmark()));

    public static final Quest CATALYTIC_OXIDATION_OF_O_XYLENE = CHAPTER.quest("catalytic_oxidation_of_o_xylene", q -> q
            .at(-18.5, 2.5)
            .title("邻二甲苯催化氧化法")
            .desc(d -> d.line("重铬酸钾后续主要用于转子镀层"))
            .task(Task.checkmark())
            .dependsOn(DIMETHYLBENZENE_BUCKET, POTASSIUM_DICHROMATE_DUST));

    public static final Quest NAPHTHALENE_BUCKET = CHAPTER.quest("naphthalene_bucket", q -> q
            .at(-21.5, 5)
            .icon(GTMaterials.Naphthalene.getBucket())
            .task(Task.item(GTMaterials.Naphthalene.getBucket()).optional(), Task.checkmark()));

    public static final Quest SULFURIC_ACID_BUCKET = CHAPTER.quest("sulfuric_acid_bucket", q -> q
            .at(-21.5, 3)
            .icon(GTMaterials.SulfuricAcid.getBucket())
            .task(Task.item(GTMaterials.SulfuricAcid.getBucket()).optional(), Task.checkmark()));

    public static final Quest NAPHTHALENE_CATALYTIC_OXIDATION_METHOD = CHAPTER.quest("naphthalene_catalytic_oxidation_method", q -> q
            .at(-20, 4)
            .title("萘催化氧化法")
            .desc(d -> d.line("只要你的木化产线完备，萘应该不是问题"))
            .task(Task.checkmark())
            .dependsOn(NAPHTHALENE_BUCKET, SULFURIC_ACID_BUCKET));

    public static final Quest PHTHALIC_ACID_BUCKET = CHAPTER.quest("phthalic_acid_bucket", q -> q
            .at(-18.5, 4)
            .icon(GTMaterials.PhthalicAcid.getBucket())
            .desc(d -> d.line("选择一种方法制备即可"))
            .task(Task.item(GTMaterials.PhthalicAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(CATALYTIC_OXIDATION_OF_O_XYLENE, NAPHTHALENE_CATALYTIC_OXIDATION_METHOD)
            .requireAny());

    public static final Quest PHTHALIC_ANHYDRIDE_DUST = CHAPTER.quest("phthalic_anhydride_dust", q -> q
            .at(-17, 4)
            .task(Task.item(TagPrefix.dust, GTOMaterials.PhthalicAnhydride))
            .dependsOn(PHTHALIC_ACID_BUCKET));

    public static final Quest ETHYLANTHRAQUINONE_BUCKET = CHAPTER.quest("ethylanthraquinone_bucket", q -> q
            .at(-15.5, 4)
            .icon(GTOMaterials.Ethylanthraquinone.getBucket())
            .task(Task.item(GTOMaterials.Ethylanthraquinone.getBucket()).optional(), Task.checkmark())
            .dependsOn(PHTHALIC_ANHYDRIDE_DUST));

    public static final Quest ETHYLANTHRAHYDROQUINONE_BUCKET = CHAPTER.quest("ethylanthrahydroquinone_bucket", q -> q
            .at(-14, 4)
            .icon(GTOMaterials.Ethylanthrahydroquinone.getBucket())
            .task(Task.item(GTOMaterials.Ethylanthrahydroquinone.getBucket()).optional(), Task.checkmark())
            .dependsOn(ETHYLANTHRAQUINONE_BUCKET));

    public static final Quest HYDROGEN_PEROXIDE_BUCKET = CHAPTER.quest("hydrogen_peroxide_bucket", q -> q
            .at(-12.5, 4)
            .icon(GTMaterials.HydrogenPeroxide.getBucket())
            .desc(d -> d.line("记得回收中间相关产物"))
            .task(Task.item(GTMaterials.HydrogenPeroxide.getBucket()).optional(), Task.checkmark())
            .dependsOn(ANTHRACENE_BUCKET, ETHYLANTHRAHYDROQUINONE_BUCKET));

    public static final Quest HYDRAZINE_BUCKET = CHAPTER.quest("hydrazine_bucket", q -> q
            .at(-11, 4)
            .icon(GTOMaterials.Hydrazine.getBucket())
            .task(Task.item(GTOMaterials.Hydrazine.getBucket()).optional(), Task.checkmark())
            .dependsOn(HYDROGEN_PEROXIDE_BUCKET));

    public static final Quest T3_ROCKET_FUEL_DENSE_HYDRAZINE_FUEL = CHAPTER.quest("t3_rocket_fuel_dense_hydrazine_fuel", q -> q
            .at(-9, 4)
            .icon(GTOMaterials.DenseHydrazineFuelMixture.getBucket())
            .title("T3火箭燃料：浓缩肼混合燃料")
            .subtitle("土星五号的光辉")
            .desc(d -> d
                    .line("利用蒽醌法制取过氧化氢的产线挺繁琐的，不过其中的过渡产物可以重复利用")
                    .line()
                    .line("从这里你会发现有一些参与反应的原材料并没有被最终消耗掉，这些就是催化剂")
                    .line()
                    .line("你在后面会见到更多催化剂的"))
            .task(Task.item(GTOMaterials.DenseHydrazineFuelMixture.getBucket()).optional(), Task.checkmark())
            .dependsOn(HYDRAZINE_BUCKET, T2_ROCKET_FUEL_ROCKET_FUEL_RP));

    public static final Quest CONFINEMENT_OF_SPACE_AND_TIME_ETERNAL = CHAPTER.quest("confinement_of_space_and_time_eternal", q -> q
            .at(18, 15.5)
            .title("时空桎梏，深空永生")
            .subtitle("进化是我们的选择，钢铁是我们的归宿。此乃，文明的终极形态")
            .desc(d -> d.line("这里是Tier3器官，它可以带你去{木卫一}、{木卫三}、{土卫二}、{土卫六}、{冥王星}和{霜原星}", RED, DARK_BLUE, DARK_PURPLE, BLUE, LIGHT_PURPLE, DARK_GREEN))
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.TACTICAL)))
            .dependsOn(IvChapter.IV_FIELD_GENERATOR, IvChapter.ASSEMBLY_LINE)
            .hideDependencyLines());

    public static final Quest TRANSCEND_THE_FLESH_BIRTH_OF_A = CHAPTER.quest("transcend_the_flesh_birth_of_a", q -> q
            .at(-3, 16)
            .title("超脱形骸，神祇创生")
            .subtitle("褪去物质的蒙昧，见证灵魂的伟力。吾等已窥见神之领域，此即为——永恒")
            .desc(d -> d.line("这里是Tier4器官，你可以前往所有星球！"))
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.PROTOTYPE)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.PROTOTYPE)))
            .dependsOn(UvChapter.UV_FORCE_FIELD_GENERATOR_LAUNCHER_AND)
            .hideDependencyLines());

    public static final Quest MONOMETHYLHYDRAZINE_BUCKET = CHAPTER.quest("monomethylhydrazine_bucket", q -> q
            .at(-11, 7)
            .icon(GTOMaterials.Monomethylhydrazine.getBucket())
            .task(Task.item(GTOMaterials.Monomethylhydrazine.getBucket()).optional(), Task.checkmark()));

    public static final Quest T4_ROCKET_FUEL_METHYLHYDRAZINE_NITRATE = CHAPTER.quest("t4_rocket_fuel_methylhydrazine_nitrate", q -> q
            .at(-9, 7)
            .icon(GTOMaterials.RocketFuelCn3h7o3.getBucket())
            .title("T4火箭燃料：硝酸甲肼")
            .subtitle("黑火药的后继传人")
            .desc(d -> d
                    .line("硝酸甲肼有1.42倍TNT的爆炸威力")
                    .line()
                    .line("{所以本质上你就是坐着炸药上天的呢）}", DARK_GRAY.italic()))
            .task(Task.item(GTOMaterials.RocketFuelCn3h7o3.getBucket()).optional(), Task.checkmark())
            .dependsOn(MONOMETHYLHYDRAZINE_BUCKET, T3_ROCKET_FUEL_DENSE_HYDRAZINE_FUEL));

    public static final Quest DIMETHYLHYDRAZINE_BUCKET_2 = CHAPTER.quest("dimethylhydrazine_bucket_2", q -> q
            .at(-11, 10.5)
            .icon(GTMaterials.Dimethylhydrazine.getBucket())
            .task(Task.item(GTMaterials.Dimethylhydrazine.getBucket()).optional(), Task.checkmark()));

    public static final Quest DINITROGEN_TETROXIDE_BUCKET = CHAPTER.quest("dinitrogen_tetroxide_bucket", q -> q
            .at(-11, 9)
            .icon(GTMaterials.DinitrogenTetroxide.getBucket())
            .task(Task.item(GTMaterials.DinitrogenTetroxide.getBucket()).optional(), Task.checkmark()));

    public static final Quest T5_ROCKET_FUEL_UNSYMMETRICAL_DIMETHYLHYDRAZINE_DINITROGEN = CHAPTER.quest("t5_rocket_fuel_unsymmetrical_dimethylhydrazine_dinitrogen", q -> q
            .at(-9, 10)
            .icon(GTOMaterials.RocketFuelH8n4c2o4.getBucket())
            .title("T5火箭燃料：偏二甲肼-四氧化二氮")
            .subtitle("重走长征路")
            .desc(d -> d
                    .line("这是目前为止最强大的火箭燃料组合，有着目前最高的比冲")
                    .line()
                    .line("你会大量需要这个燃料的，早到当前阶段，晚到太空采矿，它的身影将无处不在")
                    .line()
                    .line("中国的“神箭”：长征-2F就是用的偏二甲肼-四氧化二氮作为燃料"))
            .task(Task.item(GTOMaterials.RocketFuelH8n4c2o4.getBucket()).optional(), Task.checkmark())
            .dependsOn(DIMETHYLHYDRAZINE_BUCKET_2, DINITROGEN_TETROXIDE_BUCKET, T4_ROCKET_FUEL_METHYLHYDRAZINE_NITRATE));

    public static final Quest EXPLOSIVEHYDRAZINE_BUCKET = CHAPTER.quest("explosivehydrazine_bucket", q -> q
            .at(-11, 14)
            .icon(GTOMaterials.ExplosiveHydrazine.getBucket())
            .task(Task.item(GTOMaterials.ExplosiveHydrazine.getBucket()).optional(), Task.checkmark()));

    public static final Quest ICE_SHARD = CHAPTER.quest("ice_shard", q -> q
            .at(-11, 12)
            .task(Task.item(ModItems.ICE_SHARD.get())));

    public static final Quest CETANE_BOOSTED_DIESEL_BUCKET = CHAPTER.quest("cetane_boosted_diesel_bucket", q -> q
            .at(-11.5, 13)
            .icon(GTMaterials.CetaneBoostedDiesel.getBucket())
            .task(Task.item(GTMaterials.CetaneBoostedDiesel.getBucket()).optional(), Task.checkmark()));

    public static final Quest T6_ROCKET_FUEL_FRIGID_ICE_FUEL = CHAPTER.quest("t6_rocket_fuel_frigid_ice_fuel", q -> q
            .at(-9, 13)
            .icon(ModItems.CRYO_FUEL_BUCKET.get())
            .title("T6火箭燃料：凛冰燃料")
            .subtitle("低温发射！")
            .task(Task.item(ModItems.CRYO_FUEL_BUCKET.get()).optional(), Task.checkmark())
            .dependsOn(T5_ROCKET_FUEL_UNSYMMETRICAL_DIMETHYLHYDRAZINE_DINITROGEN, ICE_SHARD, CETANE_BOOSTED_DIESEL_BUCKET, EXPLOSIVEHYDRAZINE_BUCKET));

    public static final Quest HMXEXPLOSIVE_DUST = CHAPTER.quest("hmxexplosive_dust", q -> q
            .at(-11, 16)
            .task(Task.item(TagPrefix.dust, GTOMaterials.HmxExplosive)));

    public static final Quest T7_ROCKET_FUEL_STELLAR_ENERGY_ROCKET = CHAPTER.quest("t7_rocket_fuel_stellar_energy_rocket", q -> q
            .at(-9, 16)
            .icon(GTOMaterials.StellarEnergyRocketFuel.getBucket())
            .title("T7火箭燃料：星能火箭燃料")
            .subtitle("恒星之力，借我一用！")
            .desc(d -> d.line("最后的集大成者，当然产量也非常堪忧"))
            .task(Task.item(GTOMaterials.StellarEnergyRocketFuel.getBucket()).optional(), Task.checkmark())
            .dependsOn(HMXEXPLOSIVE_DUST, T6_ROCKET_FUEL_FRIGID_ICE_FUEL));

    public static final Quest ORES_FLUIDS_GUIDE = CHAPTER.quest("ores_fluids_guide", q -> q
            .at(2.5, 3)
            .icon(GTItems.PROSPECTOR_LuV)
            .title("矿石与流体指南")
            .subtitle("别忘了来看看")
            .desc(d -> d
                    .line("本章节会把每个星球上新出现的矿石和流体矿脉都罗列出来")
                    .line()
                    .line("每个星球的资源都各有差异，你肯定是需要带一个探矿仪去了解清楚的"))
            .task(Task.checkmark())
            .dependsOn(FIND_THE_MOON));

    public static final Quest ORE_SPECIALTY_I_MOON = CHAPTER.quest("ore_specialty_i_moon", q -> q
            .at(10, 0)
            .title("矿石特产一：月球")
            .subtitle("无尽的铝！")
            .desc(d -> d
                    .line("到达月球后，你第一件事是探矿，找到钛的来源")
                    .line()
                    .line("月球上的钛不以单质或简单化合物形式出现，你需要从{铝土矿}中提炼出{金红石}用来制取钛", GOLD, GOLD))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bauxite))
            .dependsOn(GREG_TO_THE_MOON));

    public static final Quest ORE_SPECIALTY_II_MOON = CHAPTER.quest("ore_specialty_ii_moon", q -> q
            .at(10, 2)
            .title("矿石特产二：月球")
            .subtitle("对，我钛需要你了")
            .desc(d -> d
                    .line("月球上的钛还有另一种获取方式就是粗钛铁矿")
                    .line()
                    .line("在处理粗矿并得到钛铁矿粉之后，需要用碳粉在电力高炉内将其还原回{金红石粉}并生成二氧化碳", LIGHT_PURPLE))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite))
            .dependsOn(GREG_TO_THE_MOON));

    public static final Quest HEAVY_DUTY_PLATE_2 = CHAPTER.quest("heavy_duty_plate_2", q -> q
            .at(12, 1)
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_2))
            .dependsOn(ORE_SPECIALTY_I_MOON, ORE_SPECIALTY_II_MOON));

    public static final Quest A_LIGHT_BOAT_CARRIES_A_GENTLE = CHAPTER.quest("a_light_boat_carries_a_gentle", q -> q
            .at(14.5, 1)
            .title("轻舟载微")
            .subtitle("这里是T2火箭")
            .desc(d -> d.line("现在你可以前往火星了"))
            .task(Task.item(ModItems.TIER_2_ROCKET.get()))
            .dependsOn(HEAVY_DUTY_PLATE_2));

    public static final Quest MARTIAN_WATCH = CHAPTER.quest("martian_watch", q -> q
            .at(17.5, 1).iconScale(1.5)
            .icon(ModItems.MARS_GLOBE.get())
            .title("荧惑守望")
            .subtitle("寻找生命存在的证明")
            .desc(d -> d
                    .line("以下是矿石资源分布报告：")
                    .line()
                    .line("{白钨矿脉：白钨矿}—{钨酸锂}—{锂}", rgb(0xD7E8B3), rgb(0x3B3A32), rgb(0xD7E7EE))
                    .line("{黝铜矿脉：黝铜矿}—{铜}—{辉锑矿}", rgb(0xA3A09B), rgb(0xE77C56), rgb(0x656565))
                    .line("{云母矿脉：}{蓝晶石}—{云母}—{铯榴石}", rgb(0xECFEFF), rgb(0xD5FFFF), rgb(0xECFEFF), rgb(0xEED9E1))
                    .line("{磁铁矿脉：磁铁矿}—{钒磁铁矿}—{金}", rgb(0x9D9D9D), rgb(0x505D70), rgb(0xFDF55F))
                    .line("{磷灰石矿脉：磷灰石}—{磷酸三钙}—{烧绿石}", rgb(0x06CDF1), rgb(0xFFFDDB), rgb(0x5B4838))
                    .line("{铜矿脉：}{黄铜矿}—{铁}—{黄铁矿}—{铜}", rgb(0xE77C56), rgb(0x96C185), rgb(0xEEEEEE), rgb(0xFFFEE6), rgb(0xE77C56))
                    .line("{谢尔顿矿脉：}{斑铜矿}—{谢尔顿矿}—{铂}—{钯}", rgb(0xFFF4BA), rgb(0xFFE05A), rgb(0x665F2F), rgb(0xFFF4BA), rgb(0xBD92B5))
                    .line("{硝石矿脉：硝石}—{硅藻土}—{蓝石}—{明矾石}", rgb(0xE6E6E6), rgb(0xFFFAFA), rgb(0x83CBF5), rgb(0xFBD677))
                    .line("{铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{孔雀石}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0x00F1B0))
                    .line("{石榴石矿脉：红石榴石}—{黄石榴石}—{紫水晶}—{猫眼石}", rgb(0x950C15), rgb(0xF6FF09), rgb(0xCFA0F3), rgb(0xD8BFD8))
                    .line("{矿砂矿脉：玄武岩矿砂}—{花岗岩矿砂}—{漂白土}—{石膏}", rgb(0x5C5C5C), rgb(0xD69077), rgb(0xF3EFBB), rgb(0xFFFAEC))
                    .line("{硼砂矿脉：硼砂}—{盐}—{岩盐}—{锂云母}", rgb(0xFAFAFA), rgb(0xFAFAFA), rgb(0xFFEAE1), rgb(0xFFDAE4)))
            .task(Task.dimension(Dimension.MARS))
            .reward(Reward.item(ModItems.MARS_GLOBE.get()))
            .dependsOn(A_LIGHT_BOAT_CARRIES_A_GENTLE));

    public static final Quest ORE_SPECIALTY_I_MARS = CHAPTER.quest("ore_specialty_i_mars", q -> q
            .at(19.5, 1)
            .title("矿石特产一：火星")
            .subtitle("白钨矿脉")
            .desc(d -> d.line("{粗钨酸锂}产出的钨比{粗白钨}多，不过你最好都把它们带回家", YELLOW, LIGHT_PURPLE))
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Scheelite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Tungstate),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lithium))
            .dependsOn(MARTIAN_WATCH));

    public static final Quest HEAVY_DUTY_PLATE_3 = CHAPTER.quest("heavy_duty_plate_3", q -> q
            .at(21.5, 1)
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_3))
            .dependsOn(ORE_SPECIALTY_I_MARS));

    public static final Quest FLUID_SPECIALTY_MARS = CHAPTER.quest("fluid_specialty_mars", q -> q
            .at(17.5, -1)
            .icon(GTMaterials.Radon.getBucket())
            .title("流体特产：火星")
            .subtitle("气态氡")
            .desc(d -> d
                    .line("你需要拿它做{量子之眼}", LIGHT_PURPLE)
                    .line()
                    .line("当你的硅岩产线缺乏{氡}的时候，记得回趟火星", LIGHT_PURPLE))
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .dependsOn(MARTIAN_WATCH));

    public static final Quest A_BOLT_FROM_THE_BLUE = CHAPTER.quest("a_bolt_from_the_blue", q -> q
            .at(24, 1)
            .title("平地惊雷")
            .subtitle("这里是T3火箭")
            .desc(d -> d.line("现在你可以前往金星了"))
            .task(Task.item(ModItems.TIER_3_ROCKET.get()))
            .dependsOn(HEAVY_DUTY_PLATE_3));

    public static final Quest ORE_SPECIALTY_II_MARS = CHAPTER.quest("ore_specialty_ii_mars", q -> q
            .at(17.5, 3)
            .title("矿石特产二：火星")
            .subtitle("谢尔顿矿脉")
            .desc(d -> d.line("看到这你应该需要去准备你的{铂处理线}了，如果你不想现在做也没问题，迟早也要做，但先带点矿回家吧", link(EvChapter.SOFT_AND_FLUFFY_PLATINUM_ORE)))
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Cooperite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Platinum),
                    Task.item(TagPrefix.rawOre, GTMaterials.Palladium),
                    Task.item(TagPrefix.rawOre, GTMaterials.Bornite))
            .dependsOn(MARTIAN_WATCH));

    public static final Quest TAI_BAI_PASSING_THROUGH_THE_SKY = CHAPTER.quest("tai_bai_passing_through_the_sky", q -> q
            .at(27, 1).iconScale(1.5)
            .icon(ModItems.VENUS_GLOBE.get())
            .title("太白经天")
            .subtitle("与晨曦同频")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{戴斯矿脉：戴斯}—{菱镁矿}", rgb(0xF2A057), rgb(0xFBFBF6))
                    .line("{方铅矿脉：方铅矿}—{银}—{铅}", rgb(0xF3E8FA), rgb(0xDCDCFF), rgb(0x7E6F82))
                    .line("{硫矿脉：硫}—{黄铁矿}—{闪锌矿}", rgb(0xFDFF31), rgb(0xFFFEE6), rgb(0xFFDC88))
                    .line("{钻石矿脉：}{石墨}—{钻石}—{煤炭}", rgb(0xC8FFFF), rgb(0xA8A89E), rgb(0xC8FFFF), rgb(0x393E41))
                    .line("{铬铁矿脉：磁铁矿}—{钒磁铁矿}—{铬铁矿}", rgb(0x9D9D9D), rgb(0x2B2A24), rgb(0xC5C1A8))
                    .line("{钼矿脉：}{钼铅矿}—{辉钼矿}—{钼}—{钼钙矿}", rgb(0xE3DDC3), rgb(0xFF9000), rgb(0xE3DDC3), rgb(0x404068), rgb(0xD8CFAC))
                    .line("{带状铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{金}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0xFDF55F))
                    .line("{橄榄石矿脉：}{膨润土}—{磁铁矿}—{橄榄石}—{海绿石砂}", rgb(0xA7E404), rgb(0xEDE8A3), rgb(0x9D9D9D), rgb(0xA7E404), rgb(0x1DA351))
                    .line("{硼砂矿脉：硼砂}—{盐}—{岩盐}—{锂云母}", rgb(0xFAFAFA), rgb(0xFAFAFA), rgb(0xFFEAE1), rgb(0xFFDAE4)))
            .task(Task.dimension(Dimension.VENUS))
            .reward(Reward.item(ModItems.VENUS_GLOBE.get()))
            .dependsOn(A_BOLT_FROM_THE_BLUE, MORTAL_FLESH_DECAYS_GENES_REFORGED));

    public static final Quest FLUID_SPECIALTY_VENUS = CHAPTER.quest("fluid_specialty_venus", q -> q
            .at(25, 3)
            .icon(GTMaterials.SulfuricAcid.getBucket())
            .title("流体特产：金星")
            .subtitle("硫酸")
            .desc(d -> d.line("三种基础酸之一，你最好能从各个星球上抽取出来，减少前期的压力"))
            .task(Task.item(GTMaterials.SulfuricAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(TAI_BAI_PASSING_THROUGH_THE_SKY));

    public static final Quest ORE_SPECIALTY_VENUS = CHAPTER.quest("ore_specialty_venus", q -> q
            .at(27, 3)
            .title("矿石特产：金星")
            .subtitle("戴斯矿脉")
            .desc(d -> d.line("{戴斯}主要用于{下一级火箭}，用途相对较少", rgb(0xFF9933), link(BeyondTheStarsChapter.ROARING_THROUGH_THE_SKIES)))
            .task(Task.item(ModItems.RAW_DESH.get()), Task.item(TagPrefix.rawOre, GTMaterials.Magnesite))
            .dependsOn(TAI_BAI_PASSING_THROUGH_THE_SKY));

    public static final Quest FACE_THE_TRIAL = CHAPTER.quest("face_the_trial", q -> q
            .at(17.5, 4.5)
            .title("接受考验？")
            .subtitle("对的，这东西不能单独烧出来")
            .desc(d -> d
                    .line("想完成这一步任务吗，你需要完整搭建你的铂处理线，希望你喜欢")
                    .line()
                    .line("P.S：不要吝啬你的大机器，他们就应该用于提升你的产线效率"))
            .task(Task.item(TagPrefix.dustTiny, GTMaterials.Rhodium))
            .dependsOn(ORE_SPECIALTY_II_MARS));

    public static final Quest DENSE_DESH_PLATE = CHAPTER.quest("dense_desh_plate", q -> q
            .at(27, 4.5)
            .task(Task.item(TagPrefix.plateDense, GTOMaterials.Desh))
            .dependsOn(ORE_SPECIALTY_VENUS, FACE_THE_TRIAL));

    public static final Quest DESH_TANK = CHAPTER.quest("desh_tank", q -> q
            .at(26.5, 6)
            .task(Task.item(ModItems.DESH_TANK.get()))
            .dependsOn(DENSE_DESH_PLATE));

    public static final Quest DESH_ENGINE = CHAPTER.quest("desh_engine", q -> q
            .at(27.5, 6)
            .task(Task.item(ModItems.DESH_ENGINE.get()))
            .dependsOn(DENSE_DESH_PLATE));

    public static final Quest ROARING_THROUGH_THE_SKIES = CHAPTER.quest("roaring_through_the_skies", q -> q
            .at(27, 8)
            .title("声震云霄")
            .subtitle("这里是T4火箭")
            .desc(d -> d.line("现在你可以前往水星和谷神星了"))
            .task(Task.item(ModItems.TIER_4_ROCKET.get()))
            .dependsOn(DESH_ENGINE, DESH_TANK));

    public static final Quest CHASING_THE_SUN_AT_DAWN = CHAPTER.quest("chasing_the_sun_at_dawn", q -> q
            .at(22.5, 8).iconScale(1.5)
            .icon(ModItems.MERCURY_GLOBE.get())
            .title("晨星逐日")
            .subtitle("真的很热热热！！！")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{耐热矿脉：}{耐热金属}—{钴}—{烧绿石}", rgb(0xC8FFFF), rgb(0xE65757), rgb(0x5050FA), rgb(0x5B4838))
                    .line("{铜锡矿脉：黄铜矿}—{沸石}—{锡石}—{雄黄}", rgb(0x96C185), rgb(0xF2E3E0), rgb(0x89847E), rgb(0xFF3D33))
                    .line("{谢尔顿矿脉：}{斑铜矿}—{谢尔顿矿}—{铂}—{钯}", rgb(0xFFF4BA), rgb(0xFFE05A), rgb(0x665F2F), rgb(0xFFF4BA), rgb(0xBD92B5))
                    .line("{硝石矿脉：硝石}—{硅藻土}—{蓝石}—{明矾石}", rgb(0xE6E6E6), rgb(0xFFFAFA), rgb(0x83CBF5), rgb(0xFBD677))
                    .line("{镍矿脉：}{硅镁镍矿}—{镍}—{辉钴矿}—{镍黄铁矿}", rgb(0xCCDFF5), rgb(0x32C880), rgb(0xCCDFF5), rgb(0x1975FF), rgb(0xE3CF13))
                    .line("{黄玉矿脉：}{蓝黄玉}—{黄玉}—{辉铜矿}—{斑铜矿}", rgb(0xE8D73A), rgb(0xDBFEFF), rgb(0xE8D73A), rgb(0x657882), rgb(0xFFE05A))
                    .line("{锰矿脉：钙铝榴石}—{锰铝榴石}—{软锰矿}—{钽铁矿}", rgb(0xFFB777), rgb(0xFFA81E), rgb(0xC7B5AB), rgb(0x4E6B94)))
            .task(Task.dimension(Dimension.MERCURY))
            .reward(Reward.item(ModItems.MERCURY_GLOBE.get()))
            .dependsOn(ROARING_THROUGH_THE_SKIES));

    public static final Quest ORE_SPECIALTY_MERCURY = CHAPTER.quest("ore_specialty_mercury", q -> q
            .at(22.5, 10)
            .title("矿石特产：水星")
            .subtitle("耐热金属矿脉")
            .desc(d -> d
                    .line("{T6火箭}的核心源材料，你需要准备硅岩产线来制作T6火箭的防护板", link(BeyondTheStarsChapter.STELLAR_BRIDGE_CROSSING))
                    .line()
                    .line("{当然不准备一个完整的产线也行，因为富集硅岩粉有其他的产出途径}", DARK_GRAY))
            .task(
                    Task.item(ModItems.RAW_CALORITE.get()),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cobalt),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrochlore))
            .dependsOn(CHASING_THE_SUN_AT_DAWN)
            .hideDependentLines());

    public static final Quest FLUID_SPECIALTY_MERCURY = CHAPTER.quest("fluid_specialty_mercury", q -> q
            .at(20.5, 8)
            .icon(GTMaterials.Deuterium.getBucket())
            .title("流体特产：水星")
            .subtitle("气态氘")
            .desc(d -> d.line("{氘}是后期聚变反应的重要材料，现在你可能没有太多需求，可以以后再来抽", YELLOW))
            .task(Task.item(GTMaterials.Deuterium.getBucket()).optional(), Task.checkmark())
            .dependsOn(CHASING_THE_SUN_AT_DAWN));

    public static final Quest YIN_YANG_RIFT = CHAPTER.quest("yin_yang_rift", q -> q
            .at(27, 11).iconScale(1.5)
            .icon(GTOGlobes.Items.CERES_GLOBE.get())
            .title("阴阳之隙")
            .subtitle("来自于一颗完整行星的碎片")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{紫金矿脉：}{紫金}—{金}", rgb(0xFDF55F), rgb(0xE5939B), rgb(0xFDF55F))
                    .line("{白钨矿脉：白钨矿}—{钨酸锂}—{锂}", rgb(0xD7E8B3), rgb(0x3B3A32), rgb(0xD7E7EE))
                    .line("{独居石矿脉：氟碳镧铈矿}—{独居石}—{钕}", rgb(0xCAAB60), rgb(0xD0EE98), rgb(0x6C5863))
                    .line("{铜锡矿脉：黄铜矿}—{沸石}—{锡石}—{雄黄}", rgb(0x96C185), rgb(0xF2E3E0), rgb(0x89847E), rgb(0xFF3D33))
                    .line("{铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{孔雀石}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0x00F1B0))
                    .line("{皂滑矿脉：皂石}—{滑石}—{海绿石砂}—{镍黄铁矿}", rgb(0x5A7261), rgb(0xEBFFE9), rgb(0x1DA351), rgb(0xE3CF13))
                    .line("{锰矿脉：钙铝榴石}—{锰铝榴石}—{软锰矿}—{钽铁矿}", rgb(0xFFB777), rgb(0xFFA81E), rgb(0xC7B5AB), rgb(0x4E6B94))
                    .line("{橄榄石矿脉：}{膨润土}—{磁铁矿}—{橄榄石}—{海绿石砂}", rgb(0xA7E404), rgb(0xEDE8A3), rgb(0x9D9D9D), rgb(0xA7E404), rgb(0x1DA351))
                    .line("{硼砂矿脉：硼砂}—{盐}—{岩盐}—{锂云母}", rgb(0xFAFAFA), rgb(0xFAFAFA), rgb(0xFFEAE1), rgb(0xFFDAE4)))
            .task(Task.dimension(Dimension.CERES))
            .reward(Reward.item(GTOGlobes.Items.CERES_GLOBE.get()))
            .dependsOn(ROARING_THROUGH_THE_SKIES));

    public static final Quest FLUID_SPECIALTY_CERES = CHAPTER.quest("fluid_specialty_ceres", q -> q
            .at(25, 11)
            .title("流体特产：谷神星")
            .subtitle("稀有气体族")
            .desc(d -> d
                    .line("这堆家伙是各种合金锻造的保护气体，以及后期的重要合成原料")
                    .line()
                    .line("其中{氪}会出现在{C60}的产线中，而{氙}会大量应用于{硅岩产线}", rgb(0x00FF00), link(UhvChapter.HEXANITROHEXAAXAISOWURTZITANE_DUST), rgb(0x00FFFF), link(LuvChapter.NAQUADRIA_INGOT)))
            .task(
                    Task.item(GTMaterials.Neon.getBucket()).optional(),
                    Task.item(GTMaterials.Krypton.getBucket()).optional(),
                    Task.item(GTMaterials.Xenon.getBucket()).optional())
            .dependsOn(YIN_YANG_RIFT));

    public static final Quest ORE_SPECIALTY_CERES = CHAPTER.quest("ore_specialty_ceres", q -> q
            .at(27, 13)
            .title("矿石特产：谷神星")
            .subtitle("紫金矿脉")
            .desc(d -> d.line("准备好重现{紫}{金}王朝了吗？你需要准备{IV超导体}", rgb(0x660066), rgb(0xFFFF00), link(IvChapter.SAMARIUM_IRON_ARSENIC_OXIDE_INGOT)))
            .task(Task.item(ModItems.RAW_OSTRUM.get()), Task.item(Items.RAW_GOLD))
            .dependsOn(YIN_YANG_RIFT));

    public static final Quest DENSE_OSTRUM_PLATE = CHAPTER.quest("dense_ostrum_plate", q -> q
            .at(25, 13)
            .task(Task.item(TagPrefix.plateDense, GTOMaterials.Ostrum))
            .dependsOn(ORE_SPECIALTY_CERES));

    public static final Quest OSTRUM_ENGINE = CHAPTER.quest("ostrum_engine", q -> q
            .at(23.5, 12.5)
            .task(Task.item(ModItems.OSTRUM_ENGINE.get()))
            .dependsOn(DENSE_OSTRUM_PLATE));

    public static final Quest OSTRUM_TANK = CHAPTER.quest("ostrum_tank", q -> q
            .at(23.5, 13.5)
            .task(Task.item(ModItems.OSTRUM_TANK.get()))
            .dependsOn(DENSE_OSTRUM_PLATE));

    public static final Quest HERCULEAN_STRENGTH = CHAPTER.quest("herculean_strength", q -> q
            .at(21.5, 13)
            .title("力拔山河")
            .subtitle("这里是T5火箭")
            .desc(d -> d.line("现在你可以前往木卫一和木卫三了"))
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_5_ROCKET.get()))
            .dependsOn(OSTRUM_ENGINE, OSTRUM_TANK));

    public static final Quest MOON_OF_THE_REALM_LORD = CHAPTER.quest("moon_of_the_realm_lord", q -> q
            .at(21.5, 15.5).iconScale(1.5)
            .icon(GTOGlobes.Items.GANYMEDE_GLOBE.get())
            .title("界主之月")
            .subtitle("最大的卫星")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{铍矿脉：铍}—{绿宝石}", rgb(0x73D73D), rgb(0x17FF6C))
                    .line("{锡石矿脉：}{锡}—{锡石}", rgb(0x89847E), rgb(0xFAFEFF), rgb(0x89847E))
                    .line("{铝土矿脉：铝土矿}—{钛铁矿}—{铝}", rgb(0xCFB853), rgb(0x2B2A24), rgb(0x7DB9D8))
                    .line("{云母矿脉：}{蓝晶石}—{云母}—{铯榴石}", rgb(0xECFEFF), rgb(0xD5FFFF), rgb(0xECFEFF), rgb(0xEED9E1))
                    .line("{铜锡矿脉：黄铜矿}—{沸石}—{锡石}—{雄黄}", rgb(0x96C185), rgb(0xF2E3E0), rgb(0x89847E), rgb(0xFF3D33))
                    .line("{镍矿脉：}{硅镁镍矿}—{镍}—{辉钴矿}—{镍黄铁矿}", rgb(0xCCDFF5), rgb(0x32C880), rgb(0xCCDFF5), rgb(0x1975FF), rgb(0xE3CF13))
                    .line("{皂滑矿脉：皂石}—{滑石}—{海绿石砂}—{镍黄铁矿}", rgb(0x5A7261), rgb(0xEBFFE9), rgb(0x1DA351), rgb(0xE3CF13)))
            .task(Task.dimension(Dimension.GANYMEDE))
            .reward(Reward.item(GTOGlobes.Items.GANYMEDE_GLOBE.get()))
            .dependsOn(CONFINEMENT_OF_SPACE_AND_TIME_ETERNAL, HERCULEAN_STRENGTH));

    public static final Quest JUPITER_IN_THE_SKY = CHAPTER.quest("jupiter_in_the_sky", q -> q
            .at(18, 13).iconScale(1.5)
            .icon(GTOGlobes.Items.IO_GLOBE.get())
            .title("岁星当空")
            .subtitle("这是火山星")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{硅岩矿脉：硅岩}—{钚}", rgb(0x181D3A), rgb(0xBA2727))
                    .line("{硫矿脉：硫}—{黄铁矿}—{闪锌矿}", rgb(0xFDFF31), rgb(0xFFFEE6), rgb(0xFFDC88))
                    .line("{钻石矿脉：}{石墨}—{钻石}—{煤炭}", rgb(0xC8FFFF), rgb(0xA8A89E), rgb(0xC8FFFF), rgb(0x393E41))
                    .line("{钼矿脉：}{钼铅矿}—{辉钼矿}—{钼}—{钼钙矿}", rgb(0xE3DDC3), rgb(0xFF9000), rgb(0xE3DDC3), rgb(0x404068), rgb(0xD8CFAC))
                    .line("{天青石矿脉：}{天青石}—{谢尔顿矿}—{天然碱}", rgb(0xE6E6A5), rgb(0xDE953C), rgb(0x665F2F), rgb(0xE6E6A5))
                    .line("{铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{孔雀石}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0x00F1B0))
                    .line("{橄榄石矿脉：}{膨润土}—{磁铁矿}—{橄榄石}—{海绿石砂}", rgb(0xA7E404), rgb(0xEDE8A3), rgb(0x9D9D9D), rgb(0xA7E404), rgb(0x1DA351))
                    .line("{矿砂矿脉：玄武岩矿砂}—{花岗岩矿砂}—{漂白土}—{石膏}", rgb(0x5C5C5C), rgb(0xD69077), rgb(0xF3EFBB), rgb(0xFFFAEC)))
            .task(Task.dimension(Dimension.IO))
            .reward(Reward.item(GTOGlobes.Items.IO_GLOBE.get()))
            .dependsOn(HERCULEAN_STRENGTH, CONFINEMENT_OF_SPACE_AND_TIME_ETERNAL));

    public static final Quest ORE_SPECIALTY_I_IO = CHAPTER.quest("ore_specialty_i_io", q -> q
            .at(16, 13)
            .title("矿石特产一：木卫一")
            .subtitle("硅岩矿脉")
            .desc(d -> d
                    .line("{极其重要的产线，必须完整准备}", DARK_RED)
                    .line()
                    .line("{我们才刚开始理解它的潜力。它不仅是能源，更通向全新的物理学}", GOLD))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Naquadah), Task.item(TagPrefix.rawOre, GTMaterials.Plutonium239))
            .dependsOn(JUPITER_IN_THE_SKY));

    public static final Quest FLUID_SPECIALTY_IO = CHAPTER.quest("fluid_specialty_io", q -> q
            .at(18, 11)
            .icon(GTMaterials.CoalGas.getBucket())
            .title("流体特产：木卫一")
            .subtitle("煤气")
            .desc(d -> d
                    .line("你不会想拿这东西去点你家的灶台吧？")
                    .line()
                    .line("{现在都改成天然气了当然点不着}", DARK_GRAY.strike()))
            .task(Task.item(GTMaterials.CoalGas.getBucket()).optional(), Task.checkmark())
            .dependsOn(JUPITER_IN_THE_SKY));

    public static final Quest ORE_SPECIALTY_II_IO = CHAPTER.quest("ore_specialty_ii_io", q -> q
            .at(16, 11)
            .title("矿石特产二：木卫一")
            .subtitle("天青石矿脉")
            .desc(d -> d.line("后面你搭建细菌产线的时候需要{天青石粉}", AQUA))
            .task(
                    Task.item(TagPrefix.rawOre, GTOMaterials.Celestine),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cooperite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Trona))
            .dependsOn(JUPITER_IN_THE_SKY));

    public static final Quest FLUID_SPECIALTY_GANYMEDE = CHAPTER.quest("fluid_specialty_ganymede", q -> q
            .at(21.5, 17)
            .icon(GTMaterials.HydrochloricAcid.getBucket())
            .title("流体特产：木卫三")
            .subtitle("盐酸")
            .desc(d -> d.line("三种基础酸之一，你会发现到处都需要它，以及可以大大减轻{氯}的压力", DARK_AQUA))
            .task(Task.item(GTMaterials.HydrochloricAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(MOON_OF_THE_REALM_LORD));

    public static final Quest DENSE_CALORITE_PLATE = CHAPTER.quest("dense_calorite_plate", q -> q
            .at(14, 13)
            .task(Task.item(TagPrefix.plateDense, GTOMaterials.Calorite))
            .dependsOn(ORE_SPECIALTY_I_IO, ORE_SPECIALTY_MERCURY));

    public static final Quest CALORITE_ENGINE = CHAPTER.quest("calorite_engine", q -> q
            .at(12.5, 12.5)
            .task(Task.item(ModItems.CALORITE_ENGINE.get()))
            .dependsOn(DENSE_CALORITE_PLATE));

    public static final Quest CALORITE_TANK = CHAPTER.quest("calorite_tank", q -> q
            .at(12.5, 13.5)
            .task(Task.item(ModItems.CALORITE_TANK.get()))
            .dependsOn(DENSE_CALORITE_PLATE));

    public static final Quest STELLAR_BRIDGE_CROSSING = CHAPTER.quest("stellar_bridge_crossing", q -> q
            .at(10.5, 13)
            .title("星桥飞渡")
            .subtitle("这里是T6火箭")
            .desc(d -> d.line("现在你可以前往土卫二、土卫六和冥王星了"))
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_6_ROCKET.get()))
            .dependsOn(CALORITE_TANK, CALORITE_ENGINE));

    public static final Quest SHINING_STARFIELD = CHAPTER.quest("shining_starfield", q -> q
            .at(7.5, 15.5).iconScale(1.5)
            .icon(GTOGlobes.Items.ENCELADUS_GLOBE.get())
            .title("光耀星野")
            .subtitle("看啊，闪闪发光！")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{钛矿脉：钛铁矿}—{钛}", rgb(0x2B2A24), rgb(0xED8EEA))
                    .line("{方铅矿脉：方铅矿}—{银}—{铅}", rgb(0xF3E8FA), rgb(0xDCDCFF), rgb(0x7E6F82))
                    .line("{红石矿脉：红石}—{红宝石}—{朱砂}", rgb(0xFF0000), rgb(0xD72310), rgb(0xFF335F))
                    .line("{铜矿脉：}{黄铜矿}—{铁}—{黄铁矿}—{铜}", rgb(0xE77C56), rgb(0x96C185), rgb(0xEEEEEE), rgb(0xFFFEE6), rgb(0xE77C56))
                    .line("{钼矿脉：}{钼铅矿}—{辉钼矿}—{钼}—{钼钙矿}", rgb(0xE3DDC3), rgb(0xFF9000), rgb(0xE3DDC3), rgb(0x404068), rgb(0xD8CFAC))
                    .line("{谢尔顿矿脉：}{斑铜矿}—{谢尔顿矿}—{铂}—{钯}", rgb(0xFFF4BA), rgb(0xFFE05A), rgb(0x665F2F), rgb(0xFFF4BA), rgb(0xBD92B5))
                    .line("{黄玉矿脉：}{蓝黄玉}—{黄玉}—{辉铜矿}—{斑铜矿}", rgb(0xE8D73A), rgb(0xDBFEFF), rgb(0xE8D73A), rgb(0x657882), rgb(0xFFE05A)))
            .task(Task.dimension(Dimension.ENCELADUS))
            .reward(Reward.item(GTOGlobes.Items.ENCELADUS_GLOBE.get()))
            .dependsOn(STELLAR_BRIDGE_CROSSING));

    public static final Quest FLUID_SPECIALTY_ENCELADUS = CHAPTER.quest("fluid_specialty_enceladus", q -> q
            .at(7.5, 17)
            .title("流体特产：土卫二")
            .subtitle("气态氟与气态氯")
            .desc(d -> d
                    .line("那么这两种气体终于解放了，欢呼吧！")
                    .line()
                    .line("终于可以不用大量电解{盐}和{蓝黄玉}了", GRAY, GOLD))
            .task(Task.item(GTMaterials.Chlorine.getBucket()).optional(), Task.item(GTMaterials.Fluorine.getBucket()).optional())
            .dependsOn(SHINING_STARFIELD));

    public static final Quest STELLAR_DELIMITATION = CHAPTER.quest("stellar_delimitation", q -> q
            .at(7.5, 10.5).iconScale(1.5)
            .icon(GTOGlobes.Items.TITAN_GLOBE.get())
            .title("镇星定界")
            .subtitle("寻找惠更斯号")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{戴斯矿脉：戴斯}—{菱镁矿}", rgb(0xF2A057), rgb(0xFBFBF6))
                    .line("{黝铜矿脉：黝铜矿}—{铜}—{辉锑矿}", rgb(0xA3A09B), rgb(0xE77C56), rgb(0x656565))
                    .line("{沥青铀矿脉：沥青铀矿}—{晶质铀矿}", rgb(0xFFD647), rgb(0xFFD52E))
                    .line("{铬铁矿脉：磁铁矿}—{钒磁铁矿}—{铬铁矿}", rgb(0x9D9D9D), rgb(0x2B2A24), rgb(0xC5C1A8))
                    .line("{磷灰石矿脉：磷灰石}—{磷酸三钙}—{烧绿石}", rgb(0x06CDF1), rgb(0xFFFDDB), rgb(0x5B4838))
                    .line("{铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{孔雀石}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0x00F1B0))
                    .line("{锡石榴石矿脉：}{锡石矿砂}—{石榴石砂}—{石棉}—{硅藻土}", rgb(0xCC4C25), rgb(0x89847E), rgb(0xCC4C25), rgb(0xE6E6E6), rgb(0xFFFAFA))
                    .line("{蓝宝石矿脉：}{铁铝榴石}—{镁铝榴石}—{蓝宝石}—{绿色蓝宝石}", rgb(0x3235E3), rgb(0xA21717), rgb(0xE81958), rgb(0x3235E3), rgb(0x9AE6B0)))
            .task(Task.dimension(Dimension.TITAN))
            .reward(Reward.item(GTOGlobes.Items.TITAN_GLOBE.get()))
            .dependsOn(STELLAR_BRIDGE_CROSSING));

    public static final Quest FLUID_SPECIALTY_TITAN = CHAPTER.quest("fluid_specialty_titan", q -> q
            .at(7.5, 9)
            .title("流体特产：土卫六")
            .subtitle("苯、气态甲烷与木炭副产物")
            .desc(d -> d
                    .line("如果你做好了木化产线，那你应该不会缺{木炭副产物}", GOLD)
                    .line()
                    .line("{苯}跟{甲烷}都是好东西，肯定要大量抽取的", AQUA, DARK_PURPLE))
            .task(
                    Task.item(GTMaterials.CharcoalByproducts.getBucket()).optional(),
                    Task.item(GTMaterials.Methane.getBucket()).optional(),
                    Task.item(GTMaterials.Benzene.getBucket()).optional())
            .dependsOn(STELLAR_DELIMITATION));

    public static final Quest FAR_ORBIT_PATROL = CHAPTER.quest("far_orbit_patrol", q -> q
            .at(7.5, 13).iconScale(1.5)
            .icon(GTOGlobes.Items.PLUTO_GLOBE.get())
            .title("远日巡疆")
            .subtitle("沧海一粟")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{硅岩矿脉：硅岩}—{钚}", rgb(0x181D3A), rgb(0xBA2727))
                    .line("{方铅矿脉：方铅矿}—{银}—{铅}", rgb(0xF3E8FA), rgb(0xDCDCFF), rgb(0x7E6F82))
                    .line("{云母矿脉：}{蓝晶石}—{云母}—{铯榴石}", rgb(0xECFEFF), rgb(0xD5FFFF), rgb(0xECFEFF), rgb(0xEED9E1))
                    .line("{沥青铀矿脉：沥青铀矿}—{晶质铀矿}", rgb(0xFFD647), rgb(0xFFD52E))
                    .line("{锆石矿脉：锆石}—{钨}—{软锰矿}—{钽铁矿}", rgb(0xB99B7E), rgb(0x3B3A32), rgb(0xC7B5AB), rgb(0x4E6B94))
                    .line("{磷灰石矿脉：磷灰石}—{磷酸三钙}—{烧绿石}", rgb(0x06CDF1), rgb(0xFFFDDB), rgb(0x5B4838))
                    .line("{赛特斯石英：}{石英岩}—{赛特斯石英}—{重晶石}", rgb(0xC2D6FF), rgb(0xF2F5ED), rgb(0xC2D6FF), rgb(0xE8E2D1))
                    .line("{石榴石矿脉：红石榴石}—{黄石榴石}—{紫水晶}—{猫眼石}", rgb(0x950C15), rgb(0xF6FF09), rgb(0xCFA0F3), rgb(0xD8BFD8)))
            .task(Task.dimension(Dimension.PLUTO))
            .reward(Reward.item(GTOGlobes.Items.PLUTO_GLOBE.get()))
            .dependsOn(STELLAR_BRIDGE_CROSSING));

    public static final Quest FLUID_SPECIALTY_PLUTO = CHAPTER.quest("fluid_specialty_pluto", q -> q
            .at(6, 14.5)
            .icon(GTMaterials.NitricAcid.getBucket())
            .title("流体特产：冥王星")
            .subtitle("硝酸")
            .desc(d -> d
                    .line("三种基础酸之一，{极大的需求量}", DARK_RED)
                    .line()
                    .line("{反正你就抽吧，一抽一个不吱声}", DARK_GRAY.italic()))
            .task(Task.item(GTMaterials.NitricAcid.getBucket()).optional(), Task.checkmark())
            .dependsOn(FAR_ORBIT_PATROL));

    public static final Quest SOARING_SKY_AND_SAILING_SEA = CHAPTER.quest("soaring_sky_and_sailing_sea", q -> q
            .at(4.5, 13)
            .title("擎天驾海")
            .subtitle("这里是T7火箭")
            .desc(d -> d.line("现在你可以前往霜原星了"))
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_7_ROCKET.get()))
            .dependsOn(FAR_ORBIT_PATROL));

    public static final Quest ICE_MIRROR_SHINES_THE_SKY = CHAPTER.quest("ice_mirror_shines_the_sky", q -> q
            .at(1.5, 13).iconScale(1.5)
            .icon(ModItems.GLACIO_GLOBE.get())
            .title("冰镜耀空")
            .subtitle("比邻星的比邻星")
            .desc(d -> d
                    .line("以下是矿石资源报告：")
                    .line()
                    .line("{油砂矿脉：油砂}", rgb(0xE3C78A))
                    .line("{煤炭矿脉：煤炭}", rgb(0x393E41))
                    .line("{锡石矿脉：}{锡}—{锡石}", rgb(0x89847E), rgb(0xFAFEFF), rgb(0x89847E))
                    .line("{紫金矿脉：}{紫金}—{金}", rgb(0xFDF55F), rgb(0xE5939B), rgb(0xFDF55F))
                    .line("{盐矿脉：}{岩盐}—{盐}—{锂辉石}", rgb(0xFAFAFA), rgb(0xFFEAE1), rgb(0xFAFAFA), rgb(0xFFBCBC))
                    .line("{白钨矿脉：白钨矿}—{钨酸锂}—{锂}", rgb(0xD7E8B3), rgb(0x3B3A32), rgb(0xD7E7EE))
                    .line("{云母矿脉：}{蓝晶石}—{云母}—{铯榴石}", rgb(0xECFEFF), rgb(0xD5FFFF), rgb(0xECFEFF), rgb(0xEED9E1))
                    .line("{锆石矿脉：锆石}—{钨}—{软锰矿}—{钽铁矿}", rgb(0xB99B7E), rgb(0x3B3A32), rgb(0xC7B5AB), rgb(0x4E6B94))
                    .line("{独居石矿脉：氟碳镧铈矿}—{独居石}—{钕}", rgb(0xCAAB60), rgb(0xD0EE98), rgb(0x6C5863))
                    .line("{谢尔顿矿脉：}{斑铜矿}—{谢尔顿矿}—{铂}—{钯}", rgb(0xFFF4BA), rgb(0xFFE05A), rgb(0x665F2F), rgb(0xFFF4BA), rgb(0xBD92B5))
                    .line("{天青石矿脉：}{天青石}—{谢尔顿矿}—{天然碱}", rgb(0xE6E6A5), rgb(0xDE953C), rgb(0x665F2F), rgb(0xE6E6A5))
                    .line("{青金石矿脉：}{蓝金石}—{方钠石}—{青金石}—{方解石}", rgb(0x85A9FF), rgb(0x2836F1), rgb(0x3D54FF), rgb(0x85A9FF), rgb(0xFFFEF8))
                    .line("{锡石榴石矿脉：}{锡石矿砂}—{石榴石砂}—{石棉}—{硅藻土}", rgb(0xCC4C25), rgb(0x89847E), rgb(0xCC4C25), rgb(0xE6E6E6), rgb(0xFFFAFA)))
            .task(Task.dimension(Dimension.GLACIO))
            .reward(Reward.item(ModItems.GLACIO_GLOBE.get()))
            .dependsOn(SOARING_SKY_AND_SAILING_SEA));

    public static final Quest GLACIO_SPIRIT = CHAPTER.quest("glacio_spirit", q -> q
            .at(-0.5, 13)
            .subtitle("今日手气如何？")
            .desc(d -> d.line("你要杀多少只霜原羊呢？"))
            .task(Task.item(GTOItems.GLACIO_SPIRIT))
            .dependsOn(ICE_MIRROR_SHINES_THE_SKY));

    public static final Quest RECONSTRUCT_THE_UNIVERSE = CHAPTER.quest("reconstruct_the_universe", q -> q
            .at(-3, 13)
            .title("重构乾坤")
            .subtitle("我们的目标是星辰大海")
            .desc(d -> d.line("这是T8火箭，解锁巴纳德C"))
            .task(Task.item(MultiBlockD.SPACE_ELEVATOR))
            .dependsOn(ZpmChapter.WETWARE_PROCESSOR_MAINFRAME, GLACIO_SPIRIT));

    public static final Quest STELLAR_HORIZONS = CHAPTER.quest("stellar_horizons", q -> q
            .at(-3, 19).iconScale(2)
            .icon(GTOGlobes.Items.BARNARDA_C_GLOBE.get())
            .title("苍穹彼岸")
            .subtitle("第二个地球？")
            .desc(d -> d
                    .line("终于，你踏足了最远的星系。探索到此告一段落{？}", STRIKE)
                    .page()
                    .line("以下是矿石分布报告：")
                    .line()
                    .line("{油砂矿脉：油砂}", rgb(0xE3C78A))
                    .line("{煤炭矿脉：煤炭}", rgb(0x434864))
                    .line("{硅岩矿脉：硅岩}—{钚}", rgb(0x181D3A), rgb(0xBA2727))
                    .line("{盐矿脉：}{岩盐}—{盐}—{锂辉石}", rgb(0xFAFAFA), rgb(0xFFEAE1), rgb(0xFAFAFA), rgb(0xFFBCBC))
                    .line("{方铅矿脉：方铅矿}—{银}—{铅}", rgb(0xF3E8FA), rgb(0xDCDCFF), rgb(0x7E6F82))
                    .line("{红石矿脉：红石}—{红宝石}—{朱砂}", rgb(0xFF0000), rgb(0xD72310), rgb(0xFF335F))
                    .line("{耐热矿脉：}{耐热金属}—{钴}—{烧绿石}", rgb(0xC8FFFF), rgb(0xE65757), rgb(0x5050FA), rgb(0x5B4838))
                    .line("{磷灰石矿脉：磷灰石}—{磷酸三钙}—{烧绿石}", rgb(0x06CDF1), rgb(0xFFFDDB), rgb(0x5B4838))
                    .line("{矿砂矿脉：玄武岩矿砂}—{花岗岩矿砂}—{漂白土}—{石膏}", rgb(0x5C5C5C), rgb(0xD69077), rgb(0xF3EFBB), rgb(0xFFFAEC)))
            .task(Task.dimension(Dimension.BARNARDA_C))
            .reward(Reward.item(GTOGlobes.Items.BARNARDA_C_GLOBE.get()))
            .dependsOn(TRANSCEND_THE_FLESH_BIRTH_OF_A, RECONSTRUCT_THE_UNIVERSE));

    public static final Quest FLUID_SPECIALTY_BARNARDA_C = CHAPTER.quest("fluid_specialty_barnarda_c", q -> q
            .at(-3, 21.5)
            .title("流体特产：巴纳德C")
            .subtitle("不明液体与巴纳德C空气")
            .desc(d -> d
                    .line("{不明液体}的用处都很靠后了，但你这阶段就会开始使用它们了", DARK_BLUE)
                    .line()
                    .line("至于{巴纳德-C}空气？反正它们不能用大型集气室来抽取，可以先用小机器顶着，建议后面使用太空钻井", LIGHT_PURPLE))
            .task(Task.item(GTOMaterials.UnknowWater.getBucket()), Task.item(GTOMaterials.BarnardaAir.getBucket()))
            .dependsOn(STELLAR_HORIZONS));

    public static final Quest NANOMUSCLE_HELMET = CHAPTER.quest("nanomuscle_helmet", q -> q
            .at(-2, 4.5)
            .desc(d -> d.line("这套护甲并没有储存氧气的功能，你需要做{太空定制版}", link(BeyondTheStarsChapter.SPACE_NANOMUSCLE_CHESTPLATE)))
            .task(Task.item(GTItems.NANO_HELMET), Task.item(GTItems.NANO_CHESTPLATE), Task.item(GTItems.NANO_LEGGINGS), Task.item(GTItems.NANO_BOOTS))
            .dependsOn(BASIC_SPACE_SUIT)
            .optional().hideDependencyLines());

    public static final Quest SPACE_NANOMUSCLE_CHESTPLATE = CHAPTER.quest("space_nanomuscle_chestplate", q -> q
            .at(0, 4.5)
            .subtitle("还有高手")
            .desc(d -> d
                    .line("这个比{宇航服}更好！太空纳米胸甲相比普通纳米胸甲来说多了一个氧气槽位", GOLD)
                    .line()
                    .line("{别问我整合包里为什么加入了一个比宇航服更好的衣服，却还让你在任务流程中制作它们……}", GRAY)
                    .line()
                    .line("{EV阶段}你还能制作{进阶版的太空纳米胸甲}", EV, GOLD))
            .task(Task.item(GTOItems.SPACE_NANOMUSCLE_CHESTPLATE))
            .dependsOn(HvChapter.HV_LAUNCHER_AND_HV_SENSOR, NANOMUSCLE_HELMET)
            .optional());

    public static final Quest ADVANCED_NANOMUSCLE_CHESTPLATE = CHAPTER.quest("advanced_nanomuscle_chestplate", q -> q
            .at(2, 4.5)
            .desc(d -> d.line("更强大的胸甲，但一样会被憋死，所以你需要——{太空定制版}", link(BeyondTheStarsChapter.SPACE_ADVANCED_NANOMUSCLE_CHESTPLATE)))
            .task(Task.item(GTItems.NANO_CHESTPLATE_ADVANCED))
            .dependsOn(SPACE_NANOMUSCLE_CHESTPLATE)
            .optional());

    public static final Quest SPACE_ADVANCED_NANOMUSCLE_CHESTPLATE = CHAPTER.quest("space_advanced_nanomuscle_chestplate", q -> q
            .at(4, 4.5)
            .task(Task.item(GTOItems.SPACE_ADVANCED_NANOMUSCLE_CHESTPLATE))
            .dependsOn(HvChapter.TITANIUM_INGOT, ADVANCED_NANOMUSCLE_CHESTPLATE)
            .optional());

    public static final Quest QUARKTECH_HELMET = CHAPTER.quest("quarktech_helmet", q -> q
            .at(6, 4.5)
            .desc(d -> d
                    .line("这套绿油油的护甲将会是你太空探索下一阶段的主力")
                    .line()
                    .line("但你知道{我要说什么}，所以你不能止步于此", link(BeyondTheStarsChapter.SPACE_QUARKTECH_CHESTPLATE)))
            .task(
                    Task.item(GTItems.QUANTUM_HELMET),
                    Task.item(GTItems.QUANTUM_CHESTPLATE),
                    Task.item(GTItems.QUANTUM_LEGGINGS),
                    Task.item(GTItems.QUANTUM_BOOTS))
            .dependsOn(SPACE_ADVANCED_NANOMUSCLE_CHESTPLATE, EvChapter.LET_ME_SEE_YOUR_RESULTS)
            .optional());

    public static final Quest SPACE_QUARKTECH_CHESTPLATE = CHAPTER.quest("space_quarktech_chestplate", q -> q
            .at(8, 4.5)
            .desc(d -> d.line("对的，它能存储氧气"))
            .task(Task.item(GTOItems.SPACE_QUARKTECH_CHESTPLATE))
            .dependsOn(QUARKTECH_HELMET, EvChapter.TUNGSTEN_STEEL_INGOT)
            .optional());

    public static final Quest ADVANCED_QUARKTECH_CHESTPLATE = CHAPTER.quest("advanced_quarktech_chestplate", q -> q
            .at(10, 4.5)
            .desc(d -> d
                    .line("想不到有朝一日你会需要装配线来制作一件护甲呢")
                    .line()
                    .line("以及{还是那句话——}", link(BeyondTheStarsChapter.SPACE_ADVANCED_QUARKTECH_CHESTPLATE)))
            .task(Task.item(GTItems.QUANTUM_CHESTPLATE_ADVANCED))
            .dependsOn(SPACE_QUARKTECH_CHESTPLATE)
            .optional());

    public static final Quest SPACE_ADVANCED_QUARKTECH_CHESTPLATE = CHAPTER.quest("space_advanced_quarktech_chestplate", q -> q
            .at(12, 4.5)
            .desc(d -> d.line("这个小套装才是你永远的家呀"))
            .task(Task.item(GTOItems.SPACE_ADVANCED_QUARKTECH_CHESTPLATE))
            .dependsOn(ADVANCED_QUARKTECH_CHESTPLATE)
            .optional());

    public static final Quest T8_ROCKET_DOES_NOT_NEED_FUEL = CHAPTER.quest("t8_rocket_does_not_need_fuel", q -> q
            .at(-9, 19)
            .title("T8火箭不需要燃料")
            .task(Task.checkmark())
            .dependsOn(T7_ROCKET_FUEL_STELLAR_ENERGY_ROCKET));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.TIER_T4, -7.5, 7, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T5, -7.5, 10, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T6, -7.5, 13, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T7, -7.5, 16, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T1, -7.5, -2, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T2, -7.5, 1, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T3, -7.5, 4, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T1, -0.25, 0, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T2, 15.5, 0, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T3, 25, 0, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T4, 28, 7, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T5, 22.5, 12, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T6, 11.5, 12, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T7, 5.5, 12, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T8, -2, 12, 1, 1))
                .image(QuestImage.of(QuestTextures.TIER_T8, -7.5, 19, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -3, -5.5, 2, 2)));
    }
}
