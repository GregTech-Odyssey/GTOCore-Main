package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.gtmthings.data.CustomItems;
import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class HvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("hv", c -> c
            .title("6. 高压(HV)")
            .subtitle("Tier3-HV")
            .icon(GTMultiMachines.CLEANROOM)
            .hideQuestsUntilDepsVisible()
            .autofocus(HvChapter.WELCOME_TO_THE_HIGH_VOLTAGE_STAGE));

    public static final Quest RECYCLER = CHAPTER.quest("recycler", q -> q
            .at(-2.25, -6.75)
            .desc(d -> d.line("废料盒右键可以抽奖，也可以放入本机器自动抽奖"))
            .task(Task.item(MultiBlockA.RECYCLER))
            .dependsOn(EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest HARDER_AE2_TWEAKS_EXPERT = CHAPTER.quest("harder_ae2_tweaks_expert", q -> q
            .at(-8.5, 5.5)
            .title("更难的AE2魔改{（专家）}", LIGHT_PURPLE)
            .subtitle("前面的区域，EV再探索吧")
            .desc(d -> d.line("专家模式下，如果魔法没有发展到精灵钢，那么必须要等到EV才可以开始自动化"))
            .task(Task.checkmark())
            .dependsOn(UtilChapter.Q_1FB1EC5B)
            .optional().hideUntilDepsVisible());

    public static final Quest WELCOME_TO_THE_HIGH_VOLTAGE_STAGE = CHAPTER.quest("welcome_to_the_high_voltage_stage", q -> q
            .at(-6, 1)
            .title("欢迎来到{高压}阶段", GOLD)
            .subtitle("{你需要天马行空般的想象力}", GOLD)
            .desc(d -> d
                    .line("这个阶段你的首要目标是做出{EV电路}。它能解锁这个阶段中最重要的机器{真空冷冻机}，让你能够更新原有的{石化产线}，并解锁你接下来会大量使用到的{大型化学反应釜}", GOLD, GOLD, YELLOW, GOLD)
                    .line()
                    .line("同时，在这个阶段你会遇到第一个{生产挑战}：制造自己的第一个{火箭}并{登月}。{T1}器官将是你造过最贵的东西，请务必{提升产能}，希望你的航天之旅一切顺利！", GREEN, GOLD, LIGHT_PURPLE, AQUA, YELLOW)
                    .line()
                    .line("{不要忘了发展AE与无线电网，它们同样是你这个阶段的重要目标}", GRAY.italic()))
            .task(Task.item(GTMachines.HULL[GTValues.HV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(MvChapter.STAINLESS_STEEL_INGOT, MvChapter.POLYVINYL_CHLORIDE_PLATE));

    public static final Quest HV_CHEMICAL_BATH = CHAPTER.quest("hv_chemical_bath", q -> q
            .at(-4, 1)
            .desc(d -> d.line("给{透镜}染色， 大幅降低{末影之眼}的造价", YELLOW, YELLOW))
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.HV]))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest ENDER_EYE = CHAPTER.quest("ender_eye", q -> q
            .at(-1.5, 1)
            .desc(d -> d
                    .line("拥有神秘的力量")
                    .line()
                    .line("如果你不知道怎么搞到烈焰粉，不妨看看魔法线的任务"))
            .task(Task.item(Items.ENDER_EYE))
            .dependsOn(HV_CHEMICAL_BATH, UtilChapter.ENDER_EYE)
            .requireAny());

    public static final Quest CLEANROOM = CHAPTER.quest("cleanroom", q -> q
            .at(-4, -3)
            .icon(GTMultiMachines.CLEANROOM)
            .subtitle("Home, sweet home")
            .desc(d -> d
                    .line("{HV切割机、电装和激光蚀刻}，这是它们甜蜜的家", GOLD)
                    .line()
                    .line("如果你对{超净间}不够熟悉，在EMI看看多方块结构，你也可以查看下一页的指引", GOLD)
                    .page()
                    .line("{Credit: GTNH(部分修改)}", YELLOW)
                    .line()
                    .line("更加先进的芯片、电路都需要在更加洁净的环境中才能制作，这就是为什么我们需要超净间")
                    .line()
                    .line("这是一个有趣的多方块结构，你能在它内部放置各种单方块机器，提供执行配方所需要的{超净条件}！", YELLOW)
                    .line()
                    .line("超净间是整体大小在5x5x5到15x15x15之间的中空多方块结构.超净间的底部并不需要是正方形,长方形也可以.顶层由{过滤器机械方块}构成,除了边缘,它是{塑料混凝土}方块.{控制器}必须位于顶层的中心.如果超净间的长或宽是双数,那么控制器可以放在中心两块(或四块)中的任意一块.结构的其余部分均由塑料混凝土方块搭建而成.", GOLD, GOLD, GOLD)
                    .page()
                    .line("{非顶层}的方块都可以用任意数量的方块代替：", YELLOW)
                    .line()
                    .line("一扇{铁门}可以让你进出无尘室.请确保门是关着的,否则超净间会迅速失去洁净度.{旅行锚}和{电梯}也可以使用,如果需要,甚至可以将它们{嵌入地板中}.", GOLD, GOLD, GOLD, YELLOW)
                    .line()
                    .line("你可以使用{二极管或机器外壳},来自动通过墙壁输送{电力和AE信号}.但是在为你的内部机器更新换代时你要万分小心.务必拆下旧的机器外壳和线缆二级管,然后用新的更高电压的替换,否则你一定会很伤心的.如果只是用于传输材料,那就不用管等级的事.", GOLD, YELLOW)
                    .page()
                    .line("如果你想看到内部,可以使用{超净间玻璃}.如果你喜欢，甚至可以用{超净间玻璃}替代所有非顶层的{塑料混凝土方块}使它看上去就像白色海景房.这个多方块结构的耗电量有点不同寻常,一开始是30EU/t,然后逐渐降低.但即使是在使用30EU/t的情况下,要给它足够的电,单个LV能源仓就足够了,你往往会用MV电的.请记住,这只适用于超净间,并不是一个普适规则.", GOLD, GOLD, GOLD)
                    .line()
                    .line("让我们现在先造一个5x5x5大小的超净间,你以后可以根据需要对它进行扩建.给它通电,让它运行,直到洁净度达到100%.如果洁净度低于100%,超净间内的机器就有几率吞掉在其中制作的物品,因此您要让超净间始终保持运行状态!")
                    .line()
                    .line("任何维护问题都会使洁净度上限降低10%.使用维护提示覆盖板配上红石灯来监控你的超净间.或者直接使用自动维护仓免去维护的痛苦."))
            .task(Task.item(GTMultiMachines.CLEANROOM), Task.item(GTBlocks.FILTER_CASING, 8))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT)
            .hideDependentLines());

    public static final Quest CONFIGURABLE_MAINTENANCE_HATCH = CHAPTER.quest("configurable_maintenance_hatch", q -> q
            .at(-8, 0)
            .desc(d -> d
                    .line("{负荷调节维护仓}能使你通过调整维护仓获得{免费的机器运行速度}，但换来的代价是更频繁的维护问题", GOLD, YELLOW)
                    .line()
                    .line("你也可以降低机器运行速度以换来更少的维护问题"))
            .task(Task.item(GTMachines.CONFIGURABLE_MAINTENANCE_HATCH))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest HV_MACERATOR = CHAPTER.quest("hv_macerator", q -> q
            .at(-8, 2)
            .desc(d -> d.line("终于，我们有了研磨副产物"))
            .task(Task.item(GTMachines.MACERATOR[GTValues.HV]))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest HV_SUPERCONDUCTIVITY = CHAPTER.quest("hv_superconductivity", q -> q
            .at(-8, -1)
            .title("HV超导")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.VibrantAlloy))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest INFINITE_WATER_COVER = CHAPTER.quest("infinite_water_cover", q -> q
            .at(-6, -1)
            .task(Task.item(GTItems.COVER_INFINITE_WATER))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest ORANGE_GLASS_LENS = CHAPTER.quest("orange_glass_lens", q -> q
            .at(-3.5, 2.5)
            .task(Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Orange)))
            .dependsOn(HV_CHEMICAL_BATH));

    public static final Quest HV_CUTTER = CHAPTER.quest("hv_cutter", q -> q
            .at(-4, -0.5)
            .task(Task.item(GTMachines.CUTTER[GTValues.HV]))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT)
            .hideDependentLines());

    public static final Quest HV_LAUNCHER_AND_HV_SENSOR = CHAPTER.quest("hv_launcher_and_hv_sensor", q -> q
            .at(3, 1)
            .title("{HV}发射器和{HV}传感器", HV, HV)
            .desc(d -> d
                    .line("高压时代最重要的部件，你会需要很多！")
                    .line()
                    .line("你现在可以去{冯虚御风}章节看看了，你的太空之旅将从这里开始", link(BeyondTheStarsChapter.STARGAZING)))
            .task(Task.item(GTItems.EMITTER_HV), Task.item(GTItems.SENSOR_HV))
            .dependsOn(ENDER_EYE));

    public static final Quest PLANET_SCAN_SATELLITE = CHAPTER.quest("planet_scan_satellite", q -> q
            .at(6, -0.5)
            .desc(d -> d
                    .line("{行星扫描卫星 - GT-PSS-1 型}", GOLD)
                    .line()
                    .line("本设备是深空探索的先驱，是迈向地外殖民的第一步。")
                    .line()
                    .line("由高精度HV传感器阵列、高扭矩HV机械臂与高效光伏板材构成，该卫星能在极端环境中稳定运行。其核心的电力推进器确保了其能被准确送入环月轨道，并对月球地表进行全面光谱分析与矿物分布测绘。")
                    .line()
                    .line("{警告}{：任何跨维度太空任务，在未收到该卫星传回的完整行星扫描数据前，}{均被视为自杀行为}", RED, YELLOW, RED))
            .task(Task.item(GTOItems.PLANET_SCAN_SATELLITE))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest SATELLITE_CONTROL_CENTER = CHAPTER.quest("satellite_control_center", q -> q
            .at(6, 1)
            .desc(d -> d
                    .line("要实现载人登月这一航天壮举，必须遵循循序渐进的科学探索规律！")
                    .line()
                    .line("在正式派遣你自己登陆{月球}之前，你需要构建完整的\"无人探测-载人验证-常驻开发\"技术发展体系", YELLOW)
                    .line()
                    .line("其中向{月球}发射携带{星球数据芯片}的{行星扫描卫星}是基础性战略步骤，无人探月工程能系统性获取地月空间环境参数、月球重力场数据、辐射剂量谱分布等关键信息，为后续载人登月提供数据支撑。", YELLOW, GOLD, GOLD)
                    .line()
                    .line("你必须先向{月球}发射{卫星}以拿到{月球数据}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("{所有的星球，你都需要先解锁才能登陆}", YELLOW)
                    .line()
                    .line("{顺带一提，卫星控制中心UI左下角的HUD控制是控制航天服的氧气显示的}", YELLOW))
            .task(Task.item(MultiBlockG.SATELLITE_CONTROL_CENTER))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest ROCKET_FUEL_BUCKET = CHAPTER.quest("rocket_fuel_bucket", q -> q
            .at(10.5, -0.5)
            .icon(GTMaterials.RocketFuel.getBucket())
            .subtitle("这可不能喝")
            .desc(d -> d
                    .line("{32桶}{火箭燃料}是登月所需要的最小需求量", YELLOW, GOLD)
                    .line()
                    .line("{火箭燃料}也是很好的发电燃料，你可以选择{火箭引擎}作为你的主力发电设施，这需要你搭建稳定供应的{火箭燃料产线}", GOLD, GOLD, YELLOW))
            .task(Task.item(GTMaterials.RocketFuel.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest MULTI_SMELTER = CHAPTER.quest("multi_smelter", q -> q
            .at(-6, -5.25)
            .desc(d -> d
                    .line("能够执行电炉配方，根据{线圈}和{能源仓}的等级拥有更高的并行和超频", GOLD, GOLD)
                    .line()
                    .line("你决定在必要的时候用这个替换掉你的{蒸汽熔炉}", GOLD))
            .task(Task.item(GTMultiMachines.MULTI_SMELTER))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest STEAM_LARGE_TURBINE = CHAPTER.quest("steam_large_turbine", q -> q
            .at(-3.75, -9.75)
            .desc(d -> d
                    .line("HV蒸汽轮机倒下了，但另一位老大哥站起来了")
                    .line()
                    .line("你的第一个真正的多方块发电设施，通过提升{转子支架}等级获得{指数级}的功率提升", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("放入其中的涡轮转子材料影响发电效率和功率"))
            .task(Task.item(GTMultiMachines.LARGE_STEAM_TURBINE))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .optional().hideDependencyLines());

    public static final Quest EVAPORATION_PLANT = CHAPTER.quest("evaporation_plant", q -> q
            .at(-3.75, -6.75)
            .desc(d -> d.line("蒸发塔用来获取盐水和粗卤水以制取{氯、溴和碘}", GOLD))
            .task(Task.item(MultiBlockA.EVAPORATION_PLANT))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest PROSPECTOR_HV = CHAPTER.quest("prospector_hv", q -> q
            .at(3.5, -1)
            .desc(d -> d
                    .line("HV阶段你可以做出更高一级的探矿仪，可以勘探实体矿脉，基岩矿脉和基岩流体")
                    .line()
                    .line("可以查看探矿仪的EMI来获取矿脉和流体信息")
                    .line()
                    .line("{登上月球后会送你一个}", GRAY.italic()))
            .task(Task.item(GTItems.PROSPECTOR_HV))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest HV_ASSEMBLER = CHAPTER.quest("hv_assembler", q -> q
            .at(-6, 3.5)
            .desc(d -> d.line("制造出{HV组装机}，你可以开始大批量生产{贴片元件}！", GOLD, GOLD))
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.HV]))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MvChapter.ADVANCED_INTEGRATED_CIRCUIT));

    public static final Quest PINK_GLASS_LENS = CHAPTER.quest("pink_glass_lens", q -> q
            .at(-0.5, 2.5)
            .task(Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Pink)))
            .dependsOn(HV_CHEMICAL_BATH));

    public static final Quest LPIC_CHIP = CHAPTER.quest("lpic_chip", q -> q
            .at(-3.5, 4)
            .task(Task.item(GTItems.LOW_POWER_INTEGRATED_CIRCUIT))
            .dependsOn(ORANGE_GLASS_LENS, HV_CUTTER, CLEANROOM));

    public static final Quest HV_ENERGY_INPUT_HATCH = CHAPTER.quest("hv_energy_input_hatch", q -> q
            .at(-3.5, 5.5)
            .subtitle("I'm so hot!")
            .desc(d -> d
                    .line("随着电压逐级升高，高级的{能源仓}开始需要大量的冷却媒质以保证散热", GOLD)
                    .line()
                    .line("{不要思考这句话的深意，只是增加游戏沉浸感}", GRAY))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.HV]))
            .dependsOn(LPIC_CHIP)
            .hideDependentLines());

    public static final Quest Q_47E810FD = CHAPTER.quest("q_47e810fd", q -> q
            .at(-7.5, 3.5)
            .task(Task.checkmark().title("更便宜的贴片元件！"))
            .dependsOn(HV_ASSEMBLER));

    public static final Quest SMD_RESISTOR = CHAPTER.quest("smd_resistor", q -> q
            .at(-6.75, 2.75)
            .task(Task.item(GTItems.SMD_RESISTOR))
            .dependsOn(Q_47E810FD));

    public static final Quest SMD_TRANSISTOR = CHAPTER.quest("smd_transistor", q -> q
            .at(-8.5, 3)
            .task(Task.item(GTItems.SMD_TRANSISTOR))
            .dependsOn(Q_47E810FD));

    public static final Quest SMD_CAPACITOR = CHAPTER.quest("smd_capacitor", q -> q
            .at(-8.5, 4)
            .task(Task.item(GTItems.SMD_CAPACITOR))
            .dependsOn(Q_47E810FD));

    public static final Quest SMD_INDUCTOR = CHAPTER.quest("smd_inductor", q -> q
            .at(-6.75, 4.25)
            .task(Task.item(GTItems.SMD_INDUCTOR))
            .dependsOn(Q_47E810FD));

    public static final Quest SMD_DIODE = CHAPTER.quest("smd_diode", q -> q
            .at(-9.5, 3.5)
            .desc(d -> d.line("你可以无需经过{铂处理}，从化学浸洗过的镍矿中获得{铂金属粉}", YELLOW, GOLD))
            .task(Task.item(GTItems.SMD_DIODE))
            .dependsOn(Q_47E810FD));

    public static final Quest SPACE_NANOMUSCLE_CHESTPLATE = CHAPTER.quest("space_nanomuscle_chestplate", q -> q
            .at(2.5, -1)
            .subtitle("还有高手")
            .desc(d -> d
                    .line("这个比{宇航服}更好！", GOLD)
                    .line()
                    .line("{EV阶段}你还能制作{进阶版的太空纳米胸甲}", EV, GOLD)
                    .line()
                    .line("{别问我整合包里为什么加入了一个比宇航服更好的衣服，却还让你在任务流程中制作它们……}", GRAY.italic()))
            .task(Task.item(GTOItems.SPACE_NANOMUSCLE_CHESTPLATE))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR)
            .optional());

    public static final Quest GRAY_GLASS_LENS = CHAPTER.quest("gray_glass_lens", q -> q
            .at(-2, 2.5)
            .task(Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Gray)))
            .dependsOn(HV_CHEMICAL_BATH));

    public static final Quest CYAN_GLASS_LENS = CHAPTER.quest("cyan_glass_lens", q -> q
            .at(-5, 2.5)
            .task(Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Cyan)))
            .dependsOn(HV_CHEMICAL_BATH));

    public static final Quest SIMPLE_SOC = CHAPTER.quest("simple_soc", q -> q
            .at(-5, 4)
            .task(Task.item(GTItems.SIMPLE_SYSTEM_ON_CHIP))
            .dependsOn(CYAN_GLASS_LENS)
            .hideDependentLines());

    public static final Quest MICROPROCESSOR_ASSEMBLY = CHAPTER.quest("microprocessor_assembly", q -> q
            .at(-0.5, -3)
            .title("{微型处理器集群}", YELLOW)
            .desc(d -> d
                    .line("更便宜的HV电路，集成电路系列可以被淘汰了")
                    .line()
                    .line("需要超净间环境，是时候给MV电装搬个家了"))
            .task(Task.item(GTItems.PROCESSOR_ASSEMBLY_HV))
            .dependsOn(CLEANROOM, MvChapter.MICROPROCESSOR));

    public static final Quest MICROPROCESSOR_SUPERCOMPUTER = CHAPTER.quest("microprocessor_supercomputer", q -> q
            .at(3, -3)
            .title("{微型处理器超级计算机}", YELLOW)
            .desc(d -> d.line("EV电路！我的天哪！"))
            .task(Task.item(GTItems.WORKSTATION_EV))
            .dependsOn(MICROPROCESSOR_ASSEMBLY));

    public static final Quest HV_CIRCUIT_ASSEMBLER = CHAPTER.quest("hv_circuit_assembler", q -> q
            .at(4.75, -1.5)
            .desc(d -> d.line("HV最贵的单方块机器"))
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.HV]))
            .dependsOn(MICROPROCESSOR_SUPERCOMPUTER, HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest MICROPROCESSOR_MAINFRAME = CHAPTER.quest("microprocessor_mainframe", q -> q
            .at(7.25, -1.5)
            .title("{微型处理器主机}", YELLOW)
            .subtitle("第一个IV级电路")
            .desc(d -> d
                    .line("IV级电路，目前达到的最高等级，你还暂时用不上它")
                    .line()
                    .line("你应该第一时间想到你需要它来做出下一级别的{电路组装机}", GOLD)
                    .line()
                    .line("{你应该已经发现了，从微型处理器开始，每个序列均存在四种电路，最高级的电路一般都只是你开启下一阶段大门的}{钥匙}", GRAY.italic(), YELLOW.italic()))
            .task(Task.item(GTItems.MAINFRAME_IV))
            .dependsOn(HV_CIRCUIT_ASSEMBLER));

    public static final Quest FISHING_GROUND = CHAPTER.quest("fishing_ground", q -> q
            .at(-5.25, -6.75)
            .desc(d -> d
                    .line("可以养鱼，也可以调节编程电路自动钓鱼")
                    .line()
                    .line("要这么多{鱼}有什么用？看看{鱼油}", GOLD, GOLD))
            .task(Task.item(MultiBlockA.FISHING_GROUND))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR, MICROPROCESSOR_SUPERCOMPUTER)
            .hideDependencyLines());

    public static final Quest HV_DRONE = CHAPTER.quest("hv_drone", q -> q
            .at(1.25, -1.5)
            .subtitle("解放维护与掏灰")
            .desc(d -> d
                    .line("拥有{EV级电路}后你可以开始使用{无人机}自动化清理{消声仓}的{灰烬}和维护{维护仓}", YELLOW, GOLD, YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("建造一座{无人机控制中心}，派出你的{无人机}", GOLD, GOLD)
                    .line()
                    .line("{记得给这些小家伙充电}", YELLOW)
                    .line()
                    .line("前往查看{无人机控制中心}", link(HvChapter.DRONE_CONTROL_CENTER)))
            .task(Task.item(GTOItems.HV_DRONE))
            .dependsOn(MICROPROCESSOR_SUPERCOMPUTER, HV_LAUNCHER_AND_HV_SENSOR));

    public static final Quest DRONE_CONTROL_CENTER = CHAPTER.quest("drone_control_center", q -> q
            .at(-4.5, -5.25)
            .icon(MultiBlockG.DRONE_CONTROL_CENTER)
            .desc(d -> d
                    .line("根据无人机范围，{在消声仓将满的时候自动掏出消声仓的灰烬}（灰烬通过该机器的输出总线输出），同时{自动修复维护仓的维护问题}", YELLOW, YELLOW)
                    .line()
                    .line("每次操作有几秒的冷却时间，并且消耗无人机少量的电")
                    .line()
                    .line("放入更多无人机可同时进行多项工作")
                    .line()
                    .line("EV以后配合{无线充能器}可以直接给无人机仓的无人机充电", link(LvChapter.WIRELESS_CHARGER))
                    .line()
                    .line("{别再问为什么无人机不工作啦，也许是你的机器还没有到需要它们工作的时候呢}", GRAY.italic()))
            .task(Task.item(MultiBlockG.DRONE_CONTROL_CENTER), Task.item(GTOMachines.DRONE_HATCH[GTValues.HV]))
            .dependsOn(HV_DRONE)
            .hideDependencyLines());

    public static final Quest VACUUM_FREEZER = CHAPTER.quest("vacuum_freezer", q -> q
            .at(9, -3)
            .subtitle("真正的冰箱")
            .desc(d -> d.line("简单的冷却浸洗已经不再能承受其他热锭的高温，你需要一个真正的冷冻机"))
            .task(Task.item(GTMultiMachines.VACUUM_FREEZER))
            .dependsOn(MICROPROCESSOR_SUPERCOMPUTER));

    public static final Quest NICHROME_INGOT = CHAPTER.quest("nichrome_ingot", q -> q
            .at(10.5, -4.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Nichrome))
            .dependsOn(VACUUM_FREEZER, HV_ENERGY_INPUT_HATCH));

    public static final Quest NICHROME_COIL_BLOCK = CHAPTER.quest("nichrome_coil_block", q -> q
            .at(12.5, -4.5)
            .subtitle("是时候升级你的{电力高炉}了", GOLD)
            .desc(d -> d
                    .line("{镍铬合金线圈}的基础热容刚好能使高炉在烧铝和不锈钢时获得一次额外的{无损超频}", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("除了电力高炉能获得其增益，许多带有{线圈结构方块}的多方块机器都能一步步升级线圈获得更高的{生产力}", YELLOW, YELLOW))
            .task(Task.item(GTBlocks.COIL_NICHROME))
            .dependsOn(NICHROME_INGOT));

    public static final Quest AUTO_MAINTENANCE_HATCH = CHAPTER.quest("auto_maintenance_hatch", q -> q
            .at(-8, 1)
            .desc(d -> d.line("{自动维护仓}为你免去了维护的痛苦", GOLD))
            .task(Task.item(GTMachines.AUTO_MAINTENANCE_HATCH))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE, MICROPROCESSOR_SUPERCOMPUTER)
            .hideDependencyLines());

    public static final Quest Q_50ACA7F4 = CHAPTER.quest("q_50aca7f4", q -> q
            .at(3, -6)
            .icon(GTMaterials.RawOil.getBucket())
            .desc(d -> d.line("是时候升级你的原始石化产线了！"))
            .task(Task.checkmark().title("进阶石化产线"))
            .dependsOn(MICROPROCESSOR_SUPERCOMPUTER));

    public static final Quest PRIMARY_DISTILLATION = CHAPTER.quest("primary_distillation", q -> q
            .at(0.75, -6)
            .title("初级蒸馏")
            .desc(d -> d.line("电力蒸馏塔"))
            .task(Task.item(GTMultiMachines.DISTILLATION_TOWER))
            .dependsOn(Q_50ACA7F4));

    public static final Quest DESULFURIZATION = CHAPTER.quest("desulfurization", q -> q
            .at(1.5, -7.5)
            .title("脱硫")
            .desc(d -> d.line("专业脱硫机器，比单方块化学反应釜高效得多！"))
            .task(Task.item(MultiBlockB.DESULFURIZER))
            .dependsOn(PRIMARY_DISTILLATION));

    public static final Quest CRACKING = CHAPTER.quest("cracking", q -> q
            .at(3, -8.25)
            .title("裂化")
            .desc(d -> d
                    .line("不同于蒸汽裂化，你现在可以将任意的初级油品{加氢/蒸汽裂化}为任何你想要的裂化油，以获得你需要的石油化工材料", YELLOW)
                    .line()
                    .line("{相比在化学反应釜执行裂化，它能获得双倍的产出}", YELLOW))
            .task(Task.item(GTMultiMachines.CRACKER))
            .dependsOn(DESULFURIZATION));

    public static final Quest SECONDARY_DISTILLATION = CHAPTER.quest("secondary_distillation", q -> q
            .at(4.5, -7.5)
            .title("二次蒸馏")
            .desc(d -> d.line("和{原始蒸馏塔}一样，你也需要造第二台{蒸馏塔}来二次蒸馏裂化油", GOLD, GOLD))
            .task(Task.item(GTMultiMachines.DISTILLATION_TOWER))
            .dependsOn(CRACKING));

    public static final Quest POLYMERIZATION_REACTION = CHAPTER.quest("polymerization_reaction", q -> q
            .at(5.25, -6)
            .title("聚合反应")
            .desc(d -> d
                    .line("更好的聚合物制造，{聚四氟乙烯}只能在{聚合反应器}制造", GOLD, GOLD)
                    .line()
                    .line("现在你可以更高效地制作{聚乙烯、聚氯乙烯}", GOLD))
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .dependsOn(SECONDARY_DISTILLATION, Q_50ACA7F4)
            .requireAny());

    public static final Quest POLYTETRAFLUOROETHYLENE_INGOT = CHAPTER.quest("polytetrafluoroethylene_ingot", q -> q
            .at(7.5, -6)
            .desc(d -> d
                    .line("{聚四氟乙烯}是{聚乙烯}的进阶版本，它能用在许多用得到{聚乙烯}的地方并且消耗减半", GOLD, YELLOW, YELLOW)
                    .line()
                    .line("最重要的是它解锁了HV阶段重要的机器——{大型化学反应釜}", GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 12))
            .dependsOn(POLYMERIZATION_REACTION));

    public static final Quest LARGE_CHEMICAL_REACTOR = CHAPTER.quest("large_chemical_reactor", q -> q
            .at(9.75, -6)
            .subtitle("没有人比我更懂化学")
            .desc(d -> d
                    .line("有条件的{无损超频}，运行低压配方速度更快", YELLOW)
                    .line()
                    .line("{大型化学反应釜}能够执行许多特殊配方，检查EMI配方页查看它对你有什么帮助", GOLD))
            .task(Task.item(GTMultiMachines.LARGE_CHEMICAL_REACTOR))
            .dependsOn(POLYTETRAFLUOROETHYLENE_INGOT));

    public static final Quest POLYETHYLENE_INGOT = CHAPTER.quest("polyethylene_ingot", q -> q
            .at(7.5, -7)
            .desc(d -> d.line("你现在可以更加高效地生产{聚乙烯}，产出比最大的配方需要一点{四氯化钛}", GOLD, GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polyethylene))
            .dependsOn(POLYMERIZATION_REACTION));

    public static final Quest POLYVINYL_CHLORIDE_INGOT = CHAPTER.quest("polyvinyl_chloride_ingot", q -> q
            .at(7.5, -5)
            .desc(d -> d.line("你现在可以更加高效地生产{聚氯乙烯}，产出比最大的配方需要一点{四氯化钛}", GOLD, GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.PolyvinylChloride))
            .dependsOn(POLYMERIZATION_REACTION));

    public static final Quest INTEGRAL_FRAMEWORK_MV = CHAPTER.quest("integral_framework_mv", q -> q
            .at(12.5, -6)
            .desc(d -> d
                    .line("{MV}等级的整体框架可以让你的通用工厂执行{MV}级别以下的所有配方", MV, MV)
                    .line()
                    .line("如果你还没有大量铺设通用工厂，是时候这么做了"))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_MV))
            .dependsOn(NICHROME_COIL_BLOCK));

    public static final Quest MV_FIELD_GENERATOR = CHAPTER.quest("mv_field_generator", q -> q
            .at(9, -1)
            .task(Task.item(GTItems.FIELD_GENERATOR_MV))
            .dependsOn(VACUUM_FREEZER)
            .hideDependentLines());

    public static final Quest VOID_TRANSPORTER = CHAPTER.quest("void_transporter", q -> q
            .at(-1.5, -0.25)
            .desc(d -> d
                    .line("你现在可以前往虚空/超平坦世界搭建你的基地")
                    .line()
                    .line("搭建这个多方块结构，并给它充能")
                    .line()
                    .line("黑曜石对应虚空世界，哭泣黑曜石对应超平坦世界"))
            .task(Task.item(MultiBlockG.VOID_TRANSPORTER))
            .dependsOn(ENDER_EYE, MV_FIELD_GENERATOR));

    public static final Quest PROGRAMMABLE_CHAMBER_ROOM = CHAPTER.quest("programmable_chamber_room", q -> q
            .at(4.5, -4.5).minWidth(250)
            .icon(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE)
            .title("{可编程仓室}", YELLOW)
            .subtitle("可编程仓室")
            .desc(d -> d
                    .line("拥有{EV级电路}后，你似乎意识到了有某种方法可以只用一台{多方块机器}来处理所有带有编程电路的配方", GOLD, YELLOW)
                    .line()
                    .line("你需要两样东西：")
                    .line("·{虚拟物品供应机}", GOLD)
                    .line("·{可编程仓}", GOLD)
                    .line()
                    .line("如何使用：")
                    .line("1. {在供应机接入ME网络后}，把想要虚拟化的物品放入供应机，它会自动转换成虚拟物品显示在ME网络中", YELLOW)
                    .line("2. 打开EMI，对你想编写的配方，使用{[Ctrl]+[左键]}快速填充；或者直接将{带有黄色边框的虚拟物品}从AE终端拖入样板编码区", YELLOW, AQUA)
                    .line("3. 将样板放入{贴在可编程仓上}的{样板供应器}", YELLOW, GOLD)
                    .line()
                    .line("{提示：}", LIGHT_PURPLE)
                    .line("1. 任何物品都可以是虚拟的（电路、透镜、模具等）")
                    .line("2. 请注意对应设备的{虚拟物品槽}的数量，样板写入过多虚拟物品是无法工作的", YELLOW))
            .task(Task.item(GTAEMachines.VIRTUAL_ITEM_SUPPLY_MACHINE), Task.item(GTOMachines.PROGRAMMABLEC_HATCH[GTValues.MV]).optional())
            .dependsOn(MICROPROCESSOR_SUPERCOMPUTER, MV_FIELD_GENERATOR));

    public static final Quest VISIT_FLAT = CHAPTER.quest("visit_flat", q -> q
            .at(-2.5, -1)
            .icon(Items.CRYING_OBSIDIAN)
            .desc(d -> d
                    .line("你可以在超平坦世界建造你的基地")
                    .line()
                    .line("{如果你需要一些哭泣的黑曜石，可以去看看黑曜石的tooltips}", GRAY.italic()))
            .task(Task.dimension(Dimension.FLAT).title("访问：超平坦世界"))
            .dependsOn(VOID_TRANSPORTER));

    public static final Quest VISIT_VOID = CHAPTER.quest("visit_void", q -> q
            .at(-0.5, -1)
            .icon(Items.OBSIDIAN)
            .desc(d -> d.line("你可以在虚空世界建造你的基地"))
            .task(Task.dimension(Dimension.VOID).title("访问：虚空世界"))
            .dependsOn(VOID_TRANSPORTER));

    public static final Quest VISIT_NETHER = CHAPTER.quest("visit_nether", q -> q
            .at(-1.5, -2)
            .icon(MultiBlockG.NETHER_REACTOR_CORE)
            .title("访问：下界")
            .desc(d -> d
                    .line("在{超平坦世界}使用世界信息扫描仪得到{下界维度数据}，搭建{下界反应核结构}", YELLOW, GOLD, GOLD)
                    .line()
                    .line("与虚空传送器一样，{下界反应核}需要充满能量才能进行传送", GOLD)
                    .line()
                    .line("下界缺少氧气且需要防火，你需要穿戴{纳米肌体套装+纳米太空胸甲}或者更高级的套装才能在下界存活", YELLOW)
                    .line()
                    .line("{其实只要给够功率就可以啦}", GRAY.italic()))
            .task(Task.item(MultiBlockG.NETHER_REACTOR_CORE), Task.dimension(Dimension.THE_NETHER).title("访问：下界"))
            .dependsOn(VISIT_FLAT));

    public static final Quest PROGRAMMABLE_COVER = CHAPTER.quest("programmable_cover", q -> q
            .at(6, -4.5)
            .desc(d -> d
                    .line("将{可编程覆盖版}贴到{单方块机器}上，可为其添加{虚拟物品槽}", GOLD, AQUA, YELLOW)
                    .line()
                    .line("没有{虚拟物品槽}的机器是{无法接受}虚拟物品的", YELLOW, RED))
            .task(Task.item(CustomItems.PROGRAMMABLE_COVER))
            .dependsOn(PROGRAMMABLE_CHAMBER_ROOM));

    public static final Quest NAND_CHIP = CHAPTER.quest("nand_chip", q -> q
            .at(-5, 5.5)
            .desc(d -> d.line("另一种ULV电路"))
            .task(Task.item(GTItems.NAND_CHIP_ULV))
            .dependsOn(SIMPLE_SOC));

    public static final Quest PHOSPHORUS_WAFER = CHAPTER.quest("phosphorus_wafer", q -> q
            .at(-6, -3)
            .desc(d -> d
                    .line("更高级的芯片需要更高级的晶圆才能蚀刻")
                    .line()
                    .line("同时，高等级的晶圆在切出低等级芯片的时候产率会高出不少"))
            .task(Task.item(GTItems.PHOSPHORUS_WAFER))
            .dependsOn(CLEANROOM, HV_CUTTER)
            .hideDependentLines());

    public static final Quest NOR_MEMORY_CHIP = CHAPTER.quest("nor_memory_chip", q -> q
            .at(1, 2.5)
            .desc(d -> d
                    .line("不同于先前的{单晶硅}，{磷掺杂的单晶硅}能够一次切割出32片{晶圆}，这对你的生产力有着直接的提升", YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("高级的{芯片}通常只能由更高级的{晶圆}蚀刻出，并且低级的{芯片}也能获得巨量的产能加成，{拉制}现阶段最高级的{单晶硅}通常是不二选择", YELLOW, YELLOW, YELLOW, AQUA, YELLOW))
            .task(Task.item(GTItems.NOR_MEMORY_CHIP))
            .dependsOn(PINK_GLASS_LENS, HV_CUTTER, CLEANROOM, PHOSPHORUS_WAFER));

    public static final Quest NAND_MEMORY_CHIP = CHAPTER.quest("nand_memory_chip", q -> q
            .at(-2, 4)
            .task(Task.item(GTItems.NAND_MEMORY_CHIP))
            .dependsOn(HV_CUTTER, GRAY_GLASS_LENS, CLEANROOM, PHOSPHORUS_WAFER));

    public static final Quest DATA_STICK = CHAPTER.quest("data_stick", q -> q
            .at(1, 4)
            .subtitle("一级数据载体")
            .desc(d -> d.line("用于{存储数据}，是{扫描仪}和{世界信息扫描仪}的{信息载体}，也可以作为合成物品和工具", YELLOW, GOLD, GOLD, YELLOW))
            .task(Task.item(GTItems.TOOL_DATA_STICK))
            .dependsOn(NAND_MEMORY_CHIP));

    public static final Quest PLANET_DATA_CHIP = CHAPTER.quest("planet_data_chip", q -> q
            .at(6, 2.5)
            .desc(d -> d
                    .line("{等级：}{殖民级 - 战略资产}", YELLOW, GOLD)
                    .line()
                    .line("此芯片是{「行星扫描」}任务的最终产物，内部以非易失性存储方式，记录了目标天体（如月球）的完整重力场图、光谱矿物分布、地形高程模型及潜在着陆点危险评估数据。", YELLOW)
                    .line()
                    .line("{警告}{：此芯片为单一副本，是规划地外任务的核心。遗失或损坏将导致整个扫描任务失效。}", RED, YELLOW))
            .task(Task.item(GTOItems.PLANET_DATA_CHIP))
            .dependsOn(HV_LAUNCHER_AND_HV_SENSOR, NOR_MEMORY_CHIP, SIMPLE_SOC, DATA_STICK));

    public static final Quest GAS_LARGE_TURBINE = CHAPTER.quest("gas_large_turbine", q -> q
            .at(-3, -5.25)
            .task(Task.item(GTMultiMachines.LARGE_GAS_TURBINE))
            .dependsOn(EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE)
            .optional().hideDependencyLines());

    public static final Quest Q_132AD659 = CHAPTER.quest("q_132ad659", q -> q
            .at(-6.5, -8.25)
            .icon(QuestExternalRefs.FunctionalStorageItems.MAX_STORAGE_UPGRADE)
            .desc(d -> d
                    .line("看看右面这两个多方块，它们能够提升你的产能！")
                    .line()
                    .line("如果你早就做出来了那就更好啦"))
            .task(Task.checkmark().title("提升产能?"))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .hideDependencyLines());

    public static final Quest CLEANROOM_EASY = CHAPTER.quest("cleanroom_easy", q -> q
            .at(-4.75, -1.5)
            .icon(GTMultiMachines.CLEANROOM)
            .title("超净间{（简单）}", GREEN)
            .desc(d -> d.line("简单模式机器不需要超净环境也能工作，因此我在这里直接送你一份用于过任务"))
            .task(Task.checkmark())
            .reward(Reward.item(GTMultiMachines.CLEANROOM), Reward.item(GTBlocks.FILTER_CASING, 8))
            .dependsOn(UtilChapter.Q_056EC4E6, WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .optional().hideUntilDepsVisible());

    public static final Quest IMPLOSION_COMPRESSOR = CHAPTER.quest("implosion_compressor", q -> q
            .at(3, 5.5)
            .subtitle("唯一一台你想要爆一爆的机器")
            .desc(d -> d.line("你需要它来制造{一阶火箭}和{火箭装配机}的材料", GOLD, GOLD))
            .task(Task.item(GTMultiMachines.IMPLOSION_COMPRESSOR))
            .dependsOn(MvChapter.ADVANCED_INTEGRATED_CIRCUIT)
            .hideDependencyLines());

    public static final Quest EXQUISITE_GLASS_GEM = CHAPTER.quest("exquisite_glass_gem", q -> q
            .at(5, 4)
            .task(Task.item(TagPrefix.gemExquisite, GTMaterials.Glass))
            .dependsOn(IMPLOSION_COMPRESSOR));

    public static final Quest ROCKET_COMPONENTS = CHAPTER.quest("rocket_components", q -> q
            .at(7, 4)
            .title("火箭组件")
            .task(
                    Task.item(ModItems.ROCKET_NOSE_CONE.get()),
                    Task.item(ModItems.STEEL_TANK.get(), 4),
                    Task.item(ModItems.STEEL_ENGINE.get(), 4),
                    Task.item(ModItems.ROCKET_FIN.get(), 4))
            .dependsOn(EXQUISITE_GLASS_GEM, BeyondTheStarsChapter.HEAVY_DUTY_PLATE_1));

    public static final Quest HV_COMPONENT_SUBSIDY = CHAPTER.quest("hv_component_subsidy", q -> q
            .at(-8, -3)
            .icon(GTItems.ELECTRIC_MOTOR_HV)
            .title("HV部件补助")
            .desc(d -> d.line("从HV开始，简单不再有额外补助。用自己的双手创造一切吧！"))
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.ELECTRIC_MOTOR_HV, 32))
            .dependsOn(WELCOME_TO_THE_HIGH_VOLTAGE_STAGE)
            .optional().hideUntilDepsVisible());

    public static final Quest ROCKET_ASSEMBLER = CHAPTER.quest("rocket_assembler", q -> q
            .at(7, 5.5)
            .subtitle("我的火箭老家")
            .desc(d -> d
                    .line("所有的火箭（除了7阶火箭）都从此诞生")
                    .line()
                    .line("结构有点大，但实际做出来的火箭并不大"))
            .task(Task.item(MultiBlockG.ROCKET_ASSEMBLER))
            .dependsOn(BeyondTheStarsChapter.HEAVY_DUTY_PLATE_1));

    public static final Quest TIER_1_ROCKET = CHAPTER.quest("tier_1_rocket", q -> q
            .at(9, 4)
            .subtitle("飞呀飞呀飞")
            .desc(d -> d
                    .line("{T1火箭}是你目前为止见到的最复杂的东西", GOLD)
                    .line()
                    .line("EMI自带{配方树}功能，好好研究一下如何使用它，它会带给你极大的方便", GOLD))
            .task(Task.item(ModItems.TIER_1_ROCKET.get()))
            .dependsOn(ROCKET_COMPONENTS, ROCKET_ASSEMBLER));

    public static final Quest UNLOCK_MOON = CHAPTER.quest("unlock_moon", q -> q
            .at(9, 1)
            .icon(ModItems.EARTH_GLOBE.get())
            .title("{解锁月球}", LIGHT_PURPLE)
            .subtitle("找到你了！")
            .desc(d -> d
                    .line("将{16B火箭燃料}，{一阶火箭}，{行星扫描卫星}，{星球数据芯片}放入{卫星控制中心}，调整等级为{1}，点击{启程}，等待火箭返回，带回{月球数据}，使用{扫描仪}扫描数据后{解锁月球}", GOLD, GOLD, GOLD, GOLD, GOLD, DARK_GREEN, YELLOW, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{那么恭喜你，你已经具备了载人登月所需的一切条件！}", YELLOW))
            .task(Task.checkmark())
            .reward(
                    Reward.stack(() -> QuestStacks.filled(GTItems.FLUID_CELL_LARGE_STEEL, new FluidStack(GTMaterials.RocketFuel.getFluid(), 8000)), 2),
                    Reward.item(ModItems.LAUNCH_PAD.get()))
            .dependsOn(PLANET_SCAN_SATELLITE, TIER_1_ROCKET, SATELLITE_CONTROL_CENTER, PLANET_DATA_CHIP, ROCKET_FUEL_BUCKET));

    public static final Quest HEAD_TO_THE_MOON = CHAPTER.quest("head_to_the_moon", q -> q
            .at(12, 1)
            .title("前往月球")
            .subtitle("格雷科技的一大步")
            .desc(d -> d
                    .line("现在，朝着亲爱的月球进发？不不不，还有几件事需要你{再确认一下}：", RED)
                    .line()
                    .line("首先在出发之前，要穿戴好{宇航套装}，以及把你身上的{器官}都强化到{标准级}", GOLD, GOLD, YELLOW)
                    .line()
                    .line("其次把你的背包腾出一定的空间，记住{公司}派你去月球是去{挖矿}的，不是去{旅游}的", DARK_GREEN, YELLOW, YELLOW)
                    .line()
                    .line("最后着陆前注意提前{打开助推器减速}，否则在有不幸的事情发生前就会有{更不幸的事情发生}", YELLOW, DARK_RED))
            .task(Task.dimension(Dimension.MOON).title("造访月球").icon(ModItems.MOON_GLOBE.get()))
            .reward(Reward.stack(() -> QuestStacks.withNbt(new ItemStack(GTItems.PROSPECTOR_HV), "{Charge:1600000L}"), 1))
            .dependsOn(UNLOCK_MOON, ROCKET_FUEL_BUCKET, BeyondTheStarsChapter.FLESH_IS_WEAK_MACHINERY_ASCENDS, BeyondTheStarsChapter.BASIC_SPACE_SUIT));

    public static final Quest RAW_BAUXITE = CHAPTER.quest("raw_bauxite", q -> q
            .at(13.5, 0)
            .desc(d -> d
                    .line("到达月球后，你第一件事是探矿，找到钛的来源")
                    .line()
                    .line("月球上的钛不以单质或简单化合物形式出现，你需要从{铝土矿}中提炼出{金红石}用来制取钛", GOLD, GOLD)
                    .line()
                    .line("{铝土矿矿处产出的金红石可比直接电解铝土矿粉多不少}", GRAY.italic()))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bauxite))
            .dependsOn(HEAD_TO_THE_MOON));

    public static final Quest RAW_ILMENITE = CHAPTER.quest("raw_ilmenite", q -> q
            .at(13.5, 2)
            .subtitle("比铝土制钛产量高，但慢")
            .desc(d -> d
                    .line("月球上的钛还有另一种获取方式就是粗钛铁矿")
                    .line()
                    .line("在处理粗矿得到钛铁矿粉之后，你需要用碳粉在{反应炉}内将其还原回{金红石粉}并生成二氧化碳", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{同样，矿处产出的金红石也有不少，在前期烧制钛锭较慢的时候可以选择不进行钛铁矿粉的后续处理}", GRAY.italic()))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite))
            .dependsOn(HEAD_TO_THE_MOON));

    public static final Quest RUTILE_DUST = CHAPTER.quest("rutile_dust", q -> q
            .at(15, 1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rutile))
            .dependsOn(RAW_BAUXITE, RAW_ILMENITE)
            .requireAny());

    public static final Quest TITANIUM_TETRACHLORIDE_BUCKET = CHAPTER.quest("titanium_tetrachloride_bucket", q -> q
            .at(15, -1)
            .icon(GTMaterials.TitaniumTetrachloride.getBucket())
            .desc(d -> d
                    .line("这是最后一步了！")
                    .line()
                    .line("四氯化钛也可用于催化聚合"))
            .task(Task.item(GTMaterials.TitaniumTetrachloride.getBucket()).optional(), Task.checkmark())
            .dependsOn(RUTILE_DUST));

    public static final Quest TITANIUM_INGOT = CHAPTER.quest("titanium_ingot", q -> q
            .at(15, -3)
            .desc(d -> d.line("你的努力终有回报"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Titanium))
            .dependsOn(TITANIUM_TETRACHLORIDE_BUCKET, VACUUM_FREEZER));

    public static final Quest THREE_DIMENSIONAL_PRINTER = CHAPTER.quest("three_dimensional_printer", q -> q
            .at(15, -6)
            .desc(d -> d
                    .line("一步完成外壳生产，也可以用来做{一次性工具}", GOLD)
                    .line()
                    .line("{实体工具}是不能放进样板的，只有{一次性工具}可以放进样板", GOLD, GOLD))
            .task(Task.item(MultiBlockC.THREE_DIMENSIONAL_PRINTER))
            .dependsOn(TITANIUM_INGOT));

    public static final Quest TITANIUM_LARGE_BOILER = CHAPTER.quest("titanium_large_boiler", q -> q
            .at(15.75, -5)
            .desc(d -> d.line("比{大型钢锅炉}更强大的锅炉，喂饱你的{蒸汽涡轮}", YELLOW, YELLOW))
            .task(Task.item(GTMultiMachines.LARGE_BOILER_TITANIUM))
            .dependsOn(TITANIUM_INGOT));

    public static final Quest PULSATING_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("pulsating_photovoltaic_power_station", q -> q
            .at(17, -3.75)
            .desc(d -> d.line("T2级别的{光伏电站}，比上一等级拥有更高的发电量", GOLD))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_PULSATING))
            .dependsOn(TITANIUM_INGOT)
            .optional());

    public static final Quest INTEGRAL_FRAMEWORK_HV = CHAPTER.quest("integral_framework_hv", q -> q
            .at(13.5, -1.5)
            .desc(d -> d.line("如果你通过其他手段提前搞到了一点钛，可以优先制作{HV}整体框架，非常值得", HV))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_HV))
            .dependsOn(TITANIUM_INGOT));

    public static final Quest LARGE_STEAM_INPUT_HATCH = CHAPTER.quest("large_steam_input_hatch", q -> q
            .at(16.5, -1.5)
            .desc(d -> d
                    .line("大型蒸汽输入仓能使多方块蒸汽机器提升一级{配方等级}，同时具有{超频}能力", GOLD, GOLD)
                    .line()
                    .line("每提升一级超频，蒸汽消耗量提升{4x}，速度提升{2x}", DARK_GREEN, DARK_GREEN))
            .task(Task.item(GTOMachines.LARGE_STEAM_HATCH))
            .dependsOn(LvChapter.PRECISION_STEAM_MECHANISM, TITANIUM_INGOT));

    public static final Quest Q_1A1DFCA1 = CHAPTER.quest("q_1a1dfca1", q -> q
            .at(13.5, 1)
            .icon(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV)
            .desc(d -> d
                    .line("以你目前的科技水平，无线电网无法覆盖其他维度。万幸的是，你可以跨维度传输物品，因此你可以传输燃料到其他维度进行发电，或者铺设太阳能。")
                    .line()
                    .line("在IV阶段，你将解锁{无线电网维度中继器}，它建造在哪个维度，哪个维度就会共用基地的无线电网。", link(IvChapter.WIRELESS_DIMENSION_REPEATER)))
            .task(Task.checkmark().title("其他维度如何用电"))
            .dependsOn(HEAD_TO_THE_MOON));

    public static final Quest HV_BEDROCK_ORE_MINER = CHAPTER.quest("hv_bedrock_ore_miner", q -> q
            .at(16.75, -4.75)
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.HV]))
            .dependsOn(TITANIUM_INGOT));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(FoodChapter.ELECTRIC_COOKING, -1.5, -5.25))
                .link(QuestLink.of(Magic1Chapter.STABLE_WARP_SCROLL, 12, -1))
                .link(QuestLink.of(AeChapter.THE_FIRST_PIECE_OF_THE_AUTO, -6, 5.5))
                .link(QuestLink.of(AeChapter.THE_SECOND_PIECE_OF_THE_AUTO, -7, 5.5))
                .link(QuestLink.of(AeChapter.THE_THIRD_PIECE_OF_THE_AUTO, -2, 5.5))
                .link(QuestLink.of(AeChapter.ASSEMBLY_MATRIX, 1.5, -4.5))
                .link(QuestLink.of(AeChapter.P2P_SAMPLE, 1, 5.5))
                .link(QuestLink.of(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE, 12, 3.5))
                .link(QuestLink.of(EvChapter.WELCOME_TO_THE_EXTREME_VOLTAGE_STAGE, 18, -3))
                .link(QuestLink.of(MvChapter.COMPONENT_ASSEMBLER, -4.5, -8.25))
                .link(QuestLink.of(MvChapter.PROCESSING_PLANT, -3, -8.25))
                .link(QuestLink.of(BeyondTheStarsChapter.HEAVY_DUTY_PLATE_1, 5, 5.5))
                .link(QuestLink.of(BeyondTheStarsChapter.FLESH_IS_WEAK_MACHINERY_ASCENDS, 10.5, 2))
                .link(QuestLink.of(BeyondTheStarsChapter.BASIC_SPACE_SUIT, 10.5, 0.5)));
    }
}
