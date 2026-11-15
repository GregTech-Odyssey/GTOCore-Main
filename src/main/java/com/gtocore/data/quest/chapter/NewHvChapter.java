package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.player.OrganTier;
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

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import com.hepdd.gtmthings.data.CustomItems;
import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewHvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_hv", c -> c
            .title("6. 高压时代(HV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.HV]));

    public static final Quest POLYETHYLENE_FOIL = CHAPTER.quest("polyethylene_foil", q -> q
            .at(26, -2)
            .title("聚乙烯箔")
            .task(Task.item(TagPrefix.foil, GTMaterials.Polyethylene, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.Aluminium, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CAPACITOR = CHAPTER.quest("capacitor", q -> q
            .at(24, -2)
            .title("电容")
            .task(Task.item(GTItems.CAPACITOR, 16))
            .scaledReward(GTItems.CAPACITOR, 8)
            .dependsOn(POLYETHYLENE_FOIL));

    public static final Quest ELECTRUM = CHAPTER.quest("electrum", q -> q
            .at(18, -4)
            .title("琥珀金")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Electrum, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Silver, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest HV_COMPONENTS = CHAPTER.quest("hv_components", q -> q
            .at(18, -2)
            .title("HV 部件")
            .icon(GTItems.ELECTRIC_MOTOR_HV)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_HV, 2),
                    Task.item(GTItems.ELECTRIC_PISTON_HV),
                    Task.item(GTItems.ELECTRIC_PUMP_HV),
                    Task.item(GTItems.CONVEYOR_MODULE_HV, 2),
                    Task.item(GTItems.ROBOT_ARM_HV))
            .scaledReward(GTItems.ELECTRIC_MOTOR_HV, 1)
            .dependsOn(ELECTRUM));

    public static final QuestLink HV_COMPONENTS_COPY_1 = CHAPTER.copy(HV_COMPONENTS, 16, 2);

    public static final QuestLink HV_COMPONENTS_COPY_2 = CHAPTER.copy(HV_COMPONENTS, 2, 10);

    public static final QuestLink HV_COMPONENTS_COPY_3 = CHAPTER.copy(HV_COMPONENTS, 2, 8);

    public static final QuestLink HV_COMPONENTS_COPY_4 = CHAPTER.copy(HV_COMPONENTS, 10, 14);

    public static final QuestLink HV_COMPONENTS_COPY_5 = CHAPTER.copy(HV_COMPONENTS, 24, 16);

    public static final Quest HV_GENERATOR = CHAPTER.quest("hv_generator", q -> q
            .at(20, -2)
            .title("HV 发电")
            .task(Task.anyOf(2, GTMachines.STEAM_TURBINE[GTValues.HV], GTMachines.COMBUSTION[GTValues.HV], GTMachines.GAS_TURBINE[GTValues.HV], GTOMachines.SEMI_FLUID_GENERATOR[GTValues.HV]).title("任意 HV 发电机"))
            .scaledReward(Items.COAL_BLOCK, 8)
            .dependsOn(HV_COMPONENTS));

    public static final Quest ITEM_FILTER = CHAPTER.quest("item_filter", q -> q
            .at(18, 2)
            .title("物品过滤器")
            .task(Task.item(TagPrefix.foil, GTMaterials.Zinc, 24), Task.item(GTItems.ITEM_FILTER, 3))
            .scaledReward(TagPrefix.foil, GTMaterials.Zinc, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BLUE_ALLOY = CHAPTER.quest("blue_alloy", q -> q
            .at(20, 2)
            .title("蓝色合金")
            .task(Task.item(TagPrefix.ingot, GTMaterials.BlueAlloy, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Electrotine, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CONCRETE = CHAPTER.quest("concrete", q -> q
            .at(16, -2)
            .title("混凝土")
            .task(Task.item(GTMaterials.Concrete.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Stone, 16)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CLEANROOM = CHAPTER.quest("cleanroom", q -> q
            .at(18, 0)
            .milestone()
            .title("超净间")
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
            .task(Task.item(GTMultiMachines.CLEANROOM), Task.item(GTBlocks.FILTER_CASING, 8), Task.item(GTBlocks.PLASTCRETE, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Polyethylene, 12)
            .dependsOn(ITEM_FILTER, BLUE_ALLOY, CONCRETE, HV_COMPONENTS));

    public static final Quest INDUCTOR = CHAPTER.quest("inductor", q -> q
            .at(24, 6)
            .title("电感")
            .task(Task.item(GTItems.INDUCTOR, 4))
            .scaledReward(TagPrefix.ring, GTMaterials.Steel, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PLASTIC_SUBSTRATE = CHAPTER.quest("plastic_substrate", q -> q
            .at(8, -2)
            .title("塑料基板")
            .task(Task.item(GTItems.PLASTIC_BOARD, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.Copper, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PLASTIC_BOARD = CHAPTER.quest("plastic_board", q -> q
            .at(8, 0)
            .milestone()
            .title("塑料印刷电路板")
            .desc(d -> d
                    .line("制作微型处理器系列需要用到的基板，所有微型处理器都会用到它")
                    .line()
                    .line("HV开始用量非常大，准备一条产线总不是坏事"))
            .task(Task.item(GTItems.PLASTIC_CIRCUIT_BOARD, 4))
            .scaledReward(GTItems.PLASTIC_CIRCUIT_BOARD, 2)
            .dependsOn(PLASTIC_SUBSTRATE));

    public static final Quest GLASS_LENS = CHAPTER.quest("glass_lens", q -> q
            .at(12, -2)
            .title("玻璃透镜")
            .task(Task.item(TagPrefix.lens, GTMaterials.Glass, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Glass, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final QuestLink GLASS_LENS_COPY_1 = CHAPTER.copy(GLASS_LENS, -4, 6);

    public static final QuestLink GLASS_LENS_COPY_2 = CHAPTER.copy(GLASS_LENS, 22, 8);

    public static final Quest CPU = CHAPTER.quest("cpu", q -> q
            .at(12, 0)
            .milestone()
            .title("CPU 芯片")
            .desc(d -> d.line("处理器序列的核心芯片"))
            .task(Task.item(TagPrefix.lens, GTMaterials.Diamond), Task.item(GTItems.CENTRAL_PROCESSING_UNIT, 8))
            .scaledReward(GTItems.CENTRAL_PROCESSING_UNIT, 4)
            .dependsOn(GLASS_LENS));

    public static final Quest MICROPROCESSOR = CHAPTER.quest("microprocessor", q -> q
            .at(22, 0)
            .milestone()
            .title("微型处理器")
            .desc(d -> d
                    .line("有了进阶电路组装机，你可以制造处理器序列电路了")
                    .line()
                    .line("更便宜的{LV电路}和{MV电路}", GOLD, GOLD)
                    .line()
                    .line("更高级的{HV电路}组装需要{超净间}", GOLD, GOLD)
                    .line()
                    .line("=====")
                    .line()
                    .line("{微型处理器}是你接触到的{第一种}{处理器电路}", LIGHT_PURPLE, AQUA, YELLOW)
                    .line()
                    .line("从{HV}开始，每个阶段的电路板都固定为{4个}电路，分别为{处理器}，{处理器集群}，{处理器超级计算机}和{处理器主机}", HV, GREEN, GOLD, GOLD, GOLD, GOLD)
                    .line()
                    .line("处理器主机通常是一个阶段中最重要的目标之一，它将为你打开下一个阶段的大门"))
            .icon(GTItems.PROCESSOR_MV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.MV]), Task.item(GTItems.PROCESSOR_MV, 6))
            .scaledReward(GTItems.PROCESSOR_MV, 2)
            .dependsOn(PLASTIC_BOARD, CPU, CAPACITOR));

    public static final Quest PROCESSOR_ASSEMBLY = CHAPTER.quest("processor_assembly", q -> q
            .at(22, 6)
            .milestone()
            .title("微型处理器集群")
            .desc(d -> d
                    .line("更便宜的HV电路，集成电路系列可以被淘汰了")
                    .line()
                    .line("需要超净间环境，是时候给MV电装搬个家了"))
            .task(Task.item(GTItems.PROCESSOR_ASSEMBLY_HV, 6))
            .scaledReward(GTItems.PROCESSOR_ASSEMBLY_HV, 2)
            .dependsOn(MICROPROCESSOR, INDUCTOR, CLEANROOM));

    public static final Quest WORKSTATION = CHAPTER.quest("workstation", q -> q
            .at(10, 6)
            .milestone()
            .title("微型处理器超级计算机")
            .desc(d -> d.line("EV电路！我的天哪！"))
            .task(Task.item(GTItems.WORKSTATION_EV, 2))
            .scaledReward(GTItems.WORKSTATION_EV, 1)
            .dependsOn(PROCESSOR_ASSEMBLY));

    public static final QuestLink WORKSTATION_COPY_1 = CHAPTER.copy(WORKSTATION, 6, 2);

    public static final Quest BENTONITE = CHAPTER.quest("bentonite", q -> q
            .at(-6, 4)
            .title("膨润土")
            .task(Task.item(TagPrefix.dust, GTMaterials.Bentonite, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final QuestLink BENTONITE_COPY_1 = CHAPTER.copy(BENTONITE, 26, 22);

    public static final Quest ORANGE_DYE = CHAPTER.quest("orange_dye", q -> q
            .at(-4, 4)
            .title("橙色染料")
            .task(Task.item(GTMaterials.DyeOrange.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.ORANGE_DYE, 8)
            .dependsOn(BENTONITE));

    public static final Quest ORANGE_LENS = CHAPTER.quest("orange_lens", q -> q
            .at(-2, 4)
            .title("橙色透镜")
            .desc(d -> d.line("给{透镜}染色， 大幅降低{末影之眼}的造价", YELLOW, YELLOW))
            .icon(GTItems.GLASS_LENSES.get(DyeColor.ORANGE))
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.HV]), Task.item(GTItems.GLASS_LENSES.get(DyeColor.ORANGE)))
            .scaledReward(Items.ORANGE_DYE, 8)
            .dependsOn(ORANGE_DYE, GLASS_LENS_COPY_1));

    public static final Quest LPIC = CHAPTER.quest("lpic", q -> q
            .at(0, 4)
            .title("低功率集成电路")
            .icon(GTItems.LOW_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(GTMachines.CUTTER[GTValues.HV]), Task.item(GTItems.LOW_POWER_INTEGRATED_CIRCUIT, 4))
            .scaledReward(GTItems.LOW_POWER_INTEGRATED_CIRCUIT, 2)
            .dependsOn(ORANGE_LENS));

    public static final Quest HV_ASSEMBLER = CHAPTER.quest("hv_assembler", q -> q
            .at(16, 4)
            .title("HV 组装机")
            .desc(d -> d.line("制造出{HV组装机}，你可以开始大批量生产{贴片元件}！", GOLD, GOLD))
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.HV]))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(HV_COMPONENTS_COPY_1));

    public static final QuestLink HV_ASSEMBLER_COPY_1 = CHAPTER.copy(HV_ASSEMBLER, 0, 14);

    public static final Quest BLACK_BRONZE = CHAPTER.quest("black_bronze", q -> q
            .at(14, 2)
            .title("黑青铜")
            .task(Task.item(TagPrefix.dust, GTMaterials.BlackBronze, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Electrum, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BLACK_STEEL = CHAPTER.quest("black_steel", q -> q
            .at(14, 4)
            .title("黑钢")
            .icon(TagPrefix.ingot, GTMaterials.BlackSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.BlackSteel, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Nickel, 2)
            .dependsOn(BLACK_BRONZE));

    public static final Quest HV_VOLTAGE_COIL = CHAPTER.quest("hv_voltage_coil", q -> q
            .at(16, 6)
            .milestone()
            .title("HV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_HV)
            .task(Task.item(GTItems.VOLTAGE_COIL_HV, 2))
            .scaledReward(TagPrefix.rod, GTMaterials.SteelMagnetic, 1)
            .dependsOn(BLACK_STEEL, HV_ASSEMBLER));

    public static final Quest SODIUM_AND_POTASSIUM = CHAPTER.quest("sodium_and_potassium", q -> q
            .at(4, 4)
            .title("钠与钾")
            .icon(TagPrefix.dust, GTMaterials.Potassium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Sodium, 2), Task.item(TagPrefix.dust, GTMaterials.Potassium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.RockSalt, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SODIUM_POTASSIUM = CHAPTER.quest("sodium_potassium", q -> q
            .at(2, 4)
            .title("钠钾合金")
            .icon(GTMaterials.SodiumPotassium.getBucket())
            .task(Task.item(GTMaterials.SodiumPotassium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 1)
            .dependsOn(SODIUM_AND_POTASSIUM));

    public static final Quest BLAZE_POWDER = CHAPTER.quest("blaze_powder", q -> q
            .at(4, 10)
            .title("烈焰粉")
            .icon(Items.BLAZE_POWDER)
            .task(Task.item(GTMachines.CHEMICAL_REACTOR[GTValues.HV]), Task.item(Items.BLAZE_POWDER, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(HV_COMPONENTS_COPY_2));

    public static final QuestLink BLAZE_POWDER_COPY_1 = CHAPTER.copy(BLAZE_POWDER, 12, 14);

    public static final QuestLink BLAZE_POWDER_COPY_2 = CHAPTER.copy(BLAZE_POWDER, 14, 24);

    public static final QuestLink BLAZE_POWDER_COPY_3 = CHAPTER.copy(BLAZE_POWDER, 10, 20);

    public static final Quest ENDER_EYE = CHAPTER.quest("ender_eye", q -> q
            .at(4, 8)
            .title("末影之眼")
            .desc(d -> d
                    .line("拥有神秘的力量")
                    .line()
                    .line("如果你不知道怎么搞到烈焰粉，不妨看看魔法线的任务"))
            .icon(Items.ENDER_EYE)
            .task(Task.item(Items.ENDER_EYE, 4))
            .scaledReward(Items.ENDER_PEARL, 2)
            .dependsOn(BLAZE_POWDER));

    public static final QuestLink ENDER_EYE_COPY_1 = CHAPTER.copy(ENDER_EYE, 14, 8);

    public static final Quest HV_EMITTER_SENSOR = CHAPTER.quest("hv_emitter_sensor", q -> q
            .at(6, 6)
            .milestone()
            .title("HV 发射器与传感器")
            .desc(d -> d
                    .line("高压时代最重要的部件，你会需要很多！")
                    .line()
                    .line("你现在可以去{冯虚御风}章节看看了，你的太空之旅将从这里开始", link(BeyondTheStarsChapter.STARGAZING)))
            .task(Task.item(GTItems.EMITTER_HV, 2), Task.item(GTItems.SENSOR_HV, 2))
            .scaledReward(Items.BLAZE_POWDER, 8)
            .dependsOn(ENDER_EYE));

    public static final QuestLink HV_EMITTER_SENSOR_COPY_1 = CHAPTER.copy(HV_EMITTER_SENSOR, 8, 8);

    public static final Quest HV_ENERGY_HATCH = CHAPTER.quest("hv_energy_hatch", q -> q
            .at(2, 6)
            .milestone()
            .title("HV 能源仓")
            .subtitle("I'm so hot!")
            .desc(d -> d
                    .line("随着电压逐级升高，高级的{能源仓}开始需要大量的冷却媒质以保证散热", GOLD)
                    .line()
                    .line("{不要思考这句话的深意，只是增加游戏沉浸感}", GRAY))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.HV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Gold, 4)
            .dependsOn(HV_VOLTAGE_COIL, SODIUM_POTASSIUM, LPIC));

    public static final QuestLink HV_ENERGY_HATCH_COPY_1 = CHAPTER.copy(HV_ENERGY_HATCH, 6, 14);

    public static final Quest PHOSPHORUS = CHAPTER.quest("phosphorus", q -> q
            .at(24, 8)
            .title("磷")
            .task(Task.item(TagPrefix.dust, GTMaterials.Phosphorus, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 18)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PHOSPHORUS_BOULE = CHAPTER.quest("phosphorus_boule", q -> q
            .at(24, 10)
            .title("磷晶柱")
            .task(Task.item(GTItems.PHOSPHORUS_BOULE))
            .scaledReward(TagPrefix.dust, GTMaterials.Phosphorus, 8)
            .dependsOn(HV_ENERGY_HATCH, PHOSPHORUS));

    public static final Quest PHOSPHORUS_WAFER = CHAPTER.quest("phosphorus_wafer", q -> q
            .at(22, 10)
            .title("磷晶圆")
            .desc(d -> d
                    .line("更高级的芯片需要更高级的晶圆才能蚀刻")
                    .line()
                    .line("同时，高等级的晶圆在切出低等级芯片的时候产率会高出不少"))
            .task(Task.item(GTItems.PHOSPHORUS_WAFER, 2))
            .scaledReward(GTItems.PHOSPHORUS_WAFER, 1)
            .dependsOn(PHOSPHORUS_BOULE));

    public static final Quest DYE_LENSES = CHAPTER.quest("dye_lenses", q -> q
            .at(20, 8)
            .title("粉色、灰色与青色透镜")
            .icon(GTItems.GLASS_LENSES.get(DyeColor.PINK))
            .task(
                    Task.item(GTItems.GLASS_LENSES.get(DyeColor.PINK)),
                    Task.item(GTItems.GLASS_LENSES.get(DyeColor.GRAY)),
                    Task.item(GTItems.GLASS_LENSES.get(DyeColor.CYAN)))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 6)
            .dependsOn(GLASS_LENS_COPY_2));

    public static final Quest MEMORY_CHIPS = CHAPTER.quest("memory_chips", q -> q
            .at(20, 10)
            .title("存储芯片")
            .desc(d -> d
                    .line("不同于先前的{单晶硅}，{磷掺杂的单晶硅}能够一次切割出32片{晶圆}，这对你的生产力有着直接的提升", YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("高级的{芯片}通常只能由更高级的{晶圆}蚀刻出，并且低级的{芯片}也能获得巨量的产能加成，{拉制}现阶段最高级的{单晶硅}通常是不二选择", YELLOW, YELLOW, YELLOW, AQUA, YELLOW))
            .icon(GTItems.NAND_MEMORY_CHIP)
            .task(
                    Task.item(GTMachines.LASER_ENGRAVER[GTValues.HV]),
                    Task.item(GTItems.NOR_MEMORY_CHIP, 16),
                    Task.item(GTItems.NAND_MEMORY_CHIP, 32))
            .scaledReward(GTItems.NAND_MEMORY_CHIP, 8)
            .dependsOn(PHOSPHORUS_WAFER, DYE_LENSES, HV_EMITTER_SENSOR));

    public static final QuestLink MEMORY_CHIPS_COPY_1 = CHAPTER.copy(MEMORY_CHIPS, 20, 24);

    public static final Quest SIMPLE_SOC = CHAPTER.quest("simple_soc", q -> q
            .at(18, 12)
            .milestone()
            .title("简易 SoC")
            .icon(GTItems.SIMPLE_SYSTEM_ON_CHIP)
            .task(Task.item(GTItems.SIMPLE_SYSTEM_ON_CHIP, 12))
            .scaledReward(GTItems.SIMPLE_SYSTEM_ON_CHIP, 4)
            .dependsOn(MEMORY_CHIPS));

    public static final Quest DATA_STICK = CHAPTER.quest("data_stick", q -> q
            .at(22, 24)
            .title("数据棒")
            .subtitle("一级数据载体")
            .desc(d -> d.line("用于{存储数据}，是{扫描仪}和{世界信息扫描仪}的{信息载体}，也可以作为合成物品和工具", YELLOW, GOLD, GOLD, YELLOW))
            .icon(GTItems.TOOL_DATA_STICK)
            .task(Task.item(GTItems.TOOL_DATA_STICK, 2))
            .scaledReward(GTItems.NAND_MEMORY_CHIP, 8)
            .dependsOn(MEMORY_CHIPS_COPY_1, MICROPROCESSOR));

    public static final Quest PLANET_DATA_CHIP = CHAPTER.quest("planet_data_chip", q -> q
            .at(22, 22)
            .title("星球数据芯片")
            .desc(d -> d
                    .line("{等级：}{殖民级 - 战略资产}", YELLOW, GOLD)
                    .line()
                    .line("此芯片是{「行星扫描」}任务的最终产物，内部以非易失性存储方式，记录了目标天体（如月球）的完整重力场图、光谱矿物分布、地形高程模型及潜在着陆点危险评估数据。", YELLOW)
                    .line()
                    .line("{警告}{：此芯片为单一副本，是规划地外任务的核心。遗失或损坏将导致整个扫描任务失效。}", RED, YELLOW))
            .icon(GTOItems.PLANET_DATA_CHIP)
            .task(Task.item(GTOItems.PLANET_DATA_CHIP))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 4)
            .dependsOn(DATA_STICK, SIMPLE_SOC));

    public static final Quest MV_SCANNER = CHAPTER.quest("mv_scanner", q -> q
            .at(20, 22)
            .title("MV 扫描仪")
            .task(Task.item(GTMachines.SCANNER[GTValues.MV]))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 1)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SALTPETER = CHAPTER.quest("saltpeter", q -> q
            .at(26, 8)
            .title("硝石")
            .icon(TagPrefix.dust, GTMaterials.Saltpeter)
            .task(Task.item(TagPrefix.dust, GTMaterials.Saltpeter, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest GUNPOWDER = CHAPTER.quest("gunpowder", q -> q
            .at(26, 10)
            .title("火药")
            .icon(Items.GUNPOWDER)
            .task(Task.item(Items.GUNPOWDER, 48))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 8)
            .dependsOn(SALTPETER));

    public static final Quest POWDER_BARREL = CHAPTER.quest("powder_barrel", q -> q
            .at(26, 12)
            .title("火药桶")
            .icon(GTBlocks.POWDERBARREL)
            .task(Task.item(GTBlocks.POWDERBARREL, 12))
            .scaledReward(Items.GUNPOWDER, 16)
            .dependsOn(GUNPOWDER));

    public static final Quest IMPLOSION_COMPRESSOR = CHAPTER.quest("implosion_compressor", q -> q
            .at(4, 0)
            .milestone()
            .title("内爆压缩机")
            .subtitle("唯一一台你想要爆一爆的机器")
            .desc(d -> d.line("你需要它来制造{一阶火箭}和{火箭装配机}的材料", GOLD, GOLD))
            .icon(GTMultiMachines.IMPLOSION_COMPRESSOR)
            .task(Task.item(GTMultiMachines.IMPLOSION_COMPRESSOR))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest FLUORINE = CHAPTER.quest("fluorine", q -> q
            .at(10, 16)
            .title("氟")
            .icon(GTMaterials.Fluorine.getBucket())
            .task(Task.item(GTMaterials.Fluorine.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.gem, GTMaterials.Topaz, 6)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest HYDROFLUORIC_ACID = CHAPTER.quest("hydrofluoric_acid", q -> q
            .at(12, 16)
            .title("氢氟酸")
            .icon(GTMaterials.HydrofluoricAcid.getBucket())
            .task(Task.item(GTMaterials.HydrofluoricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 2)
            .dependsOn(FLUORINE));

    public static final Quest METHANE = CHAPTER.quest("methane", q -> q
            .at(16, 14)
            .title("甲烷")
            .icon(GTMaterials.Methane.getBucket())
            .task(Task.item(GTMaterials.Methane.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CHLOROFORM = CHAPTER.quest("chloroform", q -> q
            .at(16, 16)
            .title("氯仿")
            .icon(GTMaterials.Chloroform.getBucket())
            .task(Task.item(GTMaterials.Chloroform.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(METHANE));

    public static final Quest TETRAFLUOROETHYLENE = CHAPTER.quest("tetrafluoroethylene", q -> q
            .at(14, 16)
            .title("四氟乙烯")
            .icon(GTMaterials.Tetrafluoroethylene.getBucket())
            .task(Task.item(GTMaterials.Tetrafluoroethylene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(HYDROFLUORIC_ACID, CHLOROFORM, BLAZE_POWDER_COPY_1));

    public static final Quest POLYMERIZATION_REACTOR = CHAPTER.quest("polymerization_reactor", q -> q
            .at(0, 6)
            .milestone()
            .title("聚合反应器")
            .desc(d -> d
                    .line("更好的聚合物制造，{聚四氟乙烯}只能在{聚合反应器}制造", GOLD, GOLD)
                    .line()
                    .line("现在你可以更高效地制作{聚乙烯、聚氯乙烯}", GOLD))
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .scaledReward(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 6)
            .dependsOn(WORKSTATION, HV_COMPONENTS_COPY_3));

    public static final Quest PTFE = CHAPTER.quest("ptfe", q -> q
            .at(14, 14)
            .title("聚四氟乙烯")
            .desc(d -> d
                    .line("{聚四氟乙烯}是{聚乙烯}的进阶版本，它能用在许多用得到{聚乙烯}的地方并且消耗减半", GOLD, YELLOW, YELLOW)
                    .line()
                    .line("最重要的是它解锁了HV阶段重要的机器——{大型化学反应釜}", GOLD))
            .icon(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 36))
            .scaledReward(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene, 6)
            .dependsOn(POLYMERIZATION_REACTOR, TETRAFLUOROETHYLENE));

    public static final Quest HERMETIC_CASINGS = CHAPTER.quest("hermetic_casings", q -> q
            .at(14, 12)
            .milestone()
            .title("密封外壳")
            .icon(GTBlocks.HERMETIC_CASING_HV)
            .task(Task.item(GTBlocks.HERMETIC_CASING_LV, 24), Task.item(GTBlocks.HERMETIC_CASING_HV, 23))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 16)
            .dependsOn(PTFE));

    public static final Quest AE_PRESSES = CHAPTER.quest("ae_presses", q -> q
            .at(28, 14)
            .title("压印模板")
            .icon(AEItems.LOGIC_PROCESSOR_PRESS)
            .task(Task.item(AEItems.LOGIC_PROCESSOR_PRESS), Task.item(AEItems.SILICON_PRESS))
            .scaledReward(TagPrefix.plate, GTMaterials.Gold, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest LOGIC_PROCESSOR = CHAPTER.quest("logic_processor", q -> q
            .at(28, 16)
            .title("逻辑处理器")
            .icon(AEItems.LOGIC_PROCESSOR)
            .task(Task.item(AEItems.LOGIC_PROCESSOR, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Silicon, 4)
            .dependsOn(AE_PRESSES));

    public static final Quest AE_CORES = CHAPTER.quest("ae_cores", q -> q
            .at(26, 16)
            .title("构型核心与湮灭核心")
            .icon(AEItems.FORMATION_CORE)
            .task(Task.item(AEItems.FORMATION_CORE, 2), Task.item(AEItems.ANNIHILATION_CORE, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.CertusQuartz, 4)
            .dependsOn(LOGIC_PROCESSOR));

    public static final Quest MOLECULAR_ASSEMBLER = CHAPTER.quest("molecular_assembler", q -> q
            .at(26, 14)
            .title("分子装配室")
            .task(Task.item(AEBlocks.MOLECULAR_ASSEMBLER))
            .scaledReward(TagPrefix.plate, GTMaterials.Invar, 6)
            .dependsOn(AE_CORES));

    public static final Quest NASA_WORKBENCH = CHAPTER.quest("nasa_workbench", q -> q
            .at(24, 14)
            .title("NASA 工作台")
            .task(Task.item(ModItems.NASA_WORKBENCH.get()))
            .scaledReward(GTItems.ROBOT_ARM_HV, 1)
            .dependsOn(MOLECULAR_ASSEMBLER, HV_EMITTER_SENSOR, HV_COMPONENTS_COPY_5));

    public static final Quest IMPLOSION_COMPRESSOR_PRODUCT = CHAPTER.quest("implosion_compressor_product", q -> q
            .at(24, 12)
            .title("一阶重型板")
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_1, 8))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(IMPLOSION_COMPRESSOR, POWDER_BARREL));

    public static final Quest ROCKET_ASSEMBLER = CHAPTER.quest("rocket_assembler", q -> q
            .at(22, 12)
            .milestone()
            .title("火箭装配机")
            .subtitle("我的火箭老家")
            .desc(d -> d
                    .line("所有的火箭（除了7阶火箭）都从此诞生")
                    .line()
                    .line("结构有点大，但实际做出来的火箭并不大"))
            .icon(MultiBlockG.ROCKET_ASSEMBLER)
            .task(Task.item(MultiBlockG.ROCKET_ASSEMBLER), Task.item(GTBlocks.CASING_GRATE, 49))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 2)
            .dependsOn(NASA_WORKBENCH, HERMETIC_CASINGS, IMPLOSION_COMPRESSOR_PRODUCT, HV_EMITTER_SENSOR));

    public static final Quest AMMONIA = CHAPTER.quest("ammonia", q -> q
            .at(16, 24)
            .title("氨")
            .icon(GTMaterials.Ammonia.getBucket())
            .task(Task.item(GTMaterials.Ammonia.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 4)
            .dependsOn(BLAZE_POWDER_COPY_2));

    public static final Quest HYPOCHLOROUS_ACID = CHAPTER.quest("hypochlorous_acid", q -> q
            .at(16, 22)
            .title("次氯酸")
            .task(Task.item(GTMaterials.HypochlorousAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest DIMETHYLHYDRAZINE = CHAPTER.quest("dimethylhydrazine", q -> q
            .at(18, 24)
            .title("偏二甲肼")
            .icon(GTMaterials.Dimethylhydrazine.getBucket())
            .task(Task.item(GTMaterials.Dimethylhydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Methanol.getBucket(), 2)
            .dependsOn(AMMONIA, HYPOCHLOROUS_ACID));

    public static final Quest ROCKET_FUEL = CHAPTER.quest("rocket_fuel", q -> q
            .at(18, 22)
            .title("火箭燃料")
            .subtitle("这可不能喝")
            .desc(d -> d
                    .line("{32桶}{火箭燃料}是登月所需要的最小需求量", YELLOW, GOLD)
                    .line()
                    .line("{火箭燃料}也是很好的发电燃料，你可以选择{火箭引擎}作为你的主力发电设施，这需要你搭建稳定供应的{火箭燃料产线}", GOLD, GOLD, YELLOW))
            .task(Task.item(GTMaterials.RocketFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 4)
            .dependsOn(DIMETHYLHYDRAZINE));

    public static final Quest ENGINE_PARTS = CHAPTER.quest("engine_parts", q -> q
            .at(22, 20)
            .title("储罐与引擎配件")
            .icon(ModItems.STEEL_TANK.get())
            .task(Task.item(ModItems.STEEL_TANK.get(), 8), Task.item(ModItems.FAN.get(), 4), Task.item(GTItems.LIQUID_FUEL_JETPACK, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest GLASS_GEM = CHAPTER.quest("glass_gem", q -> q
            .at(18, 14)
            .title("玻璃宝石")
            .icon(TagPrefix.gem, GTMaterials.Glass)
            .task(Task.item(TagPrefix.gem, GTMaterials.Glass, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Glass, 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest EXQUISITE_GLASS = CHAPTER.quest("exquisite_glass", q -> q
            .at(20, 14)
            .title("完美玻璃宝石")
            .icon(TagPrefix.gemExquisite, GTMaterials.Glass)
            .task(Task.item(TagPrefix.gemExquisite, GTMaterials.Glass))
            .scaledReward(TagPrefix.gem, GTMaterials.Glass, 2)
            .dependsOn(GLASS_GEM));

    public static final Quest NOSE_CONE = CHAPTER.quest("rocket_nose_cone", q -> q
            .at(20, 16)
            .title("火箭鼻锥")
            .icon(ModItems.ROCKET_NOSE_CONE.get())
            .task(Task.item(ModItems.ROCKET_NOSE_CONE.get()))
            .scaledReward(GTItems.INTEGRATED_CIRCUIT_HV, 2)
            .dependsOn(EXQUISITE_GLASS));

    public static final Quest COBALT_PIPE = CHAPTER.quest("cobalt_restrictive_pipe", q -> q
            .at(24, 24)
            .title("钴限流管")
            .icon(TagPrefix.pipeNormalRestrictive, GTMaterials.Cobalt)
            .task(Task.item(TagPrefix.pipeNormalRestrictive, GTMaterials.Cobalt, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Cobalt, 6)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest ENGINE_FRAME = CHAPTER.quest("engine_frame", q -> q
            .at(24, 22)
            .title("引擎框架")
            .icon(ModItems.ENGINE_FRAME.get())
            .task(Task.item(ModItems.ENGINE_FRAME.get(), 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.BlackSteel, 4)
            .dependsOn(COBALT_PIPE));

    public static final Quest STEEL_ENGINE = CHAPTER.quest("steel_engine", q -> q
            .at(24, 20)
            .title("钢引擎")
            .icon(ModItems.STEEL_ENGINE.get())
            .task(Task.item(ModItems.STEEL_ENGINE.get(), 4))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(ENGINE_FRAME));

    public static final Quest MAGNESIUM = CHAPTER.quest("magnesium", q -> q
            .at(26, 20)
            .title("镁")
            .task(Task.item(TagPrefix.dust, GTMaterials.Magnesium, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 30)
            .dependsOn(BENTONITE_COPY_1));

    public static final QuestLink MAGNESIUM_COPY_1 = CHAPTER.copy(MAGNESIUM, 8, 22);

    public static final Quest MAGNALIUM = CHAPTER.quest("magnalium", q -> q
            .at(26, 18)
            .title("镁铝合金与铝青铜")
            .icon(TagPrefix.dust, GTMaterials.Magnalium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Magnalium, 36), Task.item(TagPrefix.plateDouble, GTOMaterials.AluminiumBronze, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 8)
            .dependsOn(MAGNESIUM));

    public static final Quest ROCKET_FIN = CHAPTER.quest("rocket_fin", q -> q
            .at(24, 18)
            .title("火箭尾翼")
            .icon(ModItems.ROCKET_FIN.get())
            .task(Task.item(ModItems.ROCKET_FIN.get(), 4))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 1)
            .dependsOn(MAGNALIUM));

    public static final Quest TIER_1_ROCKET = CHAPTER.quest("tier_1_rocket", q -> q
            .at(22, 18)
            .milestone()
            .title("T1 火箭")
            .subtitle("飞呀飞呀飞")
            .desc(d -> d
                    .line("{T1火箭}是你目前为止见到的最复杂的东西", GOLD)
                    .line()
                    .line("EMI自带{配方树}功能，好好研究一下如何使用它，它会带给你极大的方便", GOLD))
            .task(Task.item(ModItems.TIER_1_ROCKET.get()))
            .scaledReward(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
            .dependsOn(NOSE_CONE, STEEL_ENGINE, ROCKET_FIN, ENGINE_PARTS, ROCKET_ASSEMBLER));

    public static final Quest TANTALUM = CHAPTER.quest("tantalum", q -> q
            .at(-6, 14)
            .title("钽")
            .icon(TagPrefix.dust, GTMaterials.Tantalum)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tantalum, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Tantalite, 9)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest ANTIMONY = CHAPTER.quest("antimony", q -> q
            .at(-4, 20)
            .title("锑")
            .icon(TagPrefix.ingot, GTMaterials.Antimony)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Antimony, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Antimony, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest BATTERY_ALLOY = CHAPTER.quest("battery_alloy", q -> q
            .at(-2, 18)
            .title("电池合金")
            .icon(TagPrefix.ingot, GTMaterials.BatteryAlloy)
            .task(Task.item(TagPrefix.plate, GTMaterials.BatteryAlloy, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Lead, 16)
            .dependsOn(ANTIMONY));

    public static final Quest CADMIUM_BATTERY = CHAPTER.quest("cadmium_battery", q -> q
            .at(-2, 14)
            .title("镉电池")
            .icon(GTItems.BATTERY_HV_CADMIUM)
            .task(Task.item(TagPrefix.dust, GTMaterials.Cadmium, 64), Task.item(GTItems.BATTERY_HV_CADMIUM, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Cadmium, 16)
            .dependsOn(BATTERY_ALLOY));

    public static final Quest TANTALUM_BATTERY = CHAPTER.quest("tantalum_battery", q -> q
            .at(-4, 14)
            .title("钽电容")
            .icon(GTItems.BATTERY_ULV_TANTALUM)
            .task(Task.item(GTItems.BATTERY_ULV_TANTALUM, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Manganese, 8)
            .dependsOn(TANTALUM));

    public static final Quest SUPER_CAPACITOR = CHAPTER.quest("super_capacitor", q -> q
            .at(-4, 12)
            .title("超级电容")
            .icon(GTOItems.SUPER_CAPACITOR)
            .task(Task.item(GTOItems.SUPER_CAPACITOR, 2))
            .scaledReward(TagPrefix.rod, GTMaterials.Lapis, 8)
            .dependsOn(TANTALUM_BATTERY));

    public static final Quest SOLAR_COVER = CHAPTER.quest("solar_cover", q -> q
            .at(-2, 12)
            .title("太阳能板")
            .icon(GTItems.COVER_SOLAR_PANEL)
            .task(Task.item(GTItems.COVER_SOLAR_PANEL, 2))
            .scaledReward(GTItems.SILICON_WAFER, 2)
            .dependsOn(SUPER_CAPACITOR));

    public static final Quest SCAN_SATELLITE = CHAPTER.quest("scan_satellite", q -> q
            .at(0, 12)
            .milestone()
            .title("行星扫描卫星")
            .desc(d -> d
                    .line("{行星扫描卫星 - GT-PSS-1 型}", GOLD)
                    .line()
                    .line("本设备是深空探索的先驱，是迈向地外殖民的第一步。")
                    .line()
                    .line("由高精度HV传感器阵列、高扭矩HV机械臂与高效光伏板材构成，该卫星能在极端环境中稳定运行。其核心的电力推进器确保了其能被准确送入环月轨道，并对月球地表进行全面光谱分析与矿物分布测绘。")
                    .line()
                    .line("{警告}{：任何跨维度太空任务，在未收到该卫星传回的完整行星扫描数据前，}{均被视为自杀行为}", RED, YELLOW, RED))
            .task(Task.item(GTOItems.PLANET_SCAN_SATELLITE))
            .scaledReward(TagPrefix.dust, GTMaterials.Cadmium, 16)
            .dependsOn(SOLAR_COVER, CADMIUM_BATTERY, HV_EMITTER_SENSOR, HV_ASSEMBLER_COPY_1));

    public static final Quest FLUIX = CHAPTER.quest("fluix", q -> q
            .at(2, 14)
            .title("福鲁伊克斯")
            .icon(AEItems.FLUIX_DUST)
            .task(Task.item(AEItems.FLUIX_DUST, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.NetherQuartz, 4)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest WIRELESS_BOOSTER = CHAPTER.quest("wireless_booster", q -> q
            .at(4, 14)
            .title("无线增幅器")
            .task(Task.item(AEItems.WIRELESS_BOOSTER))
            .scaledReward(AEItems.FLUIX_DUST, 2)
            .dependsOn(FLUIX));

    public static final Quest SATELLITE_CONTROL_CENTER = CHAPTER.quest("satellite_control_center", q -> q
            .at(4, 12)
            .milestone()
            .title("卫星控制中心")
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
            .icon(MultiBlockG.SATELLITE_CONTROL_CENTER)
            .task(Task.item(MultiBlockG.SATELLITE_CONTROL_CENTER), Task.item(GTOBlocks.MULTI_FUNCTIONAL_CASING, 31))
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 4)
            .dependsOn(HV_EMITTER_SENSOR, WIRELESS_BOOSTER));

    public static final Quest UNLOCK_MOON = CHAPTER.quest("unlock_moon", q -> q
            .at(20, 20)
            .title("解锁月球")
            .subtitle("找到你了！")
            .desc(d -> d
                    .line("将{16B火箭燃料}，{一阶火箭}，{行星扫描卫星}，{星球数据芯片}放入{卫星控制中心}，调整等级为{1}，点击{启程}，等待火箭返回，带回{月球数据}，使用{扫描仪}扫描数据后{解锁月球}", GOLD, GOLD, GOLD, GOLD, GOLD, DARK_GREEN, YELLOW, GOLD, GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{那么恭喜你，你已经具备了载人登月所需的一切条件！}", YELLOW))
            .task(Task.checkmark())
            .reward(Reward.item(ModItems.LAUNCH_PAD.get(), 1))
            .dependsOn(SATELLITE_CONTROL_CENTER, SCAN_SATELLITE, TIER_1_ROCKET, ROCKET_FUEL, PLANET_DATA_CHIP, MV_SCANNER));

    public static final Quest ORGANS = CHAPTER.quest("organs", q -> q
            .at(18, 16)
            .title("标准级器官")
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
            .scaledReward(GTItems.ROBOT_ARM_MV, 2)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest SPACE_SUIT = CHAPTER.quest("space_suit", q -> q
            .at(16, 20)
            .title("宇航服")
            .task(
                    Task.item(ModItems.SPACE_HELMET.get()),
                    Task.item(ModItems.SPACE_SUIT.get()),
                    Task.item(ModItems.SPACE_PANTS.get()),
                    Task.item(ModItems.SPACE_BOOTS.get()))
            .scaledReward(ModItems.STEEL_PLATE.get(), 8)
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest MOON = CHAPTER.quest("moon", q -> q
            .at(16, 18)
            .milestone()
            .title("登陆月球")
            .subtitle("格雷科技的一大步")
            .desc(d -> d
                    .line("现在，朝着亲爱的月球进发？不不不，还有几件事需要你{再确认一下}：", RED)
                    .line()
                    .line("首先在出发之前，要穿戴好{宇航套装}，以及把你身上的{器官}都强化到{标准级}", GOLD, GOLD, YELLOW)
                    .line()
                    .line("其次把你的背包腾出一定的空间，记住{公司}派你去月球是去{挖矿}的，不是去{旅游}的", DARK_GREEN, YELLOW, YELLOW)
                    .line()
                    .line("最后着陆前注意提前{打开助推器减速}，否则在有不幸的事情发生前就会有{更不幸的事情发生}", YELLOW, DARK_RED))
            .icon(ModItems.MOON_GLOBE.get())
            .task(Task.dimension(Dimension.MOON).title("造访月球").icon(ModItems.MOON_GLOBE.get()))
            .scaledReward(TagPrefix.dust, GTMaterials.Magnesium, 8)
            .dependsOn(UNLOCK_MOON, ORGANS, SPACE_SUIT));

    public static final Quest ILMENITE = CHAPTER.quest("ilmenite", q -> q
            .at(12, 22)
            .title("钛铁矿")
            .subtitle("比铝土制钛产量高，但慢")
            .desc(d -> d
                    .line("月球上的钛还有另一种获取方式就是粗钛铁矿")
                    .line()
                    .line("在处理粗矿得到钛铁矿粉之后，你需要用碳粉在{反应炉}内将其还原回{金红石粉}并生成二氧化碳", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("{同样，矿处产出的金红石也有不少，在前期烧制钛锭较慢的时候可以选择不进行钛铁矿粉的后续处理}", GRAY.italic()))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 4)
            .dependsOn(MOON));

    public static final Quest BAUXITE = CHAPTER.quest("bauxite", q -> q
            .at(14, 22)
            .title("铝土矿")
            .desc(d -> d
                    .line("到达月球后，你第一件事是探矿，找到钛的来源")
                    .line()
                    .line("月球上的钛不以单质或简单化合物形式出现，你需要从{铝土矿}中提炼出{金红石}用来制取钛", GOLD, GOLD)
                    .line()
                    .line("{铝土矿矿处产出的金红石可比直接电解铝土矿粉多不少}", GRAY.italic()))
            .icon(TagPrefix.rawOre, GTMaterials.Bauxite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bauxite, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Bauxite, 15)
            .dependsOn(MOON));

    public static final Quest RUTILE = CHAPTER.quest("rutile", q -> q
            .at(12, 24)
            .title("金红石")
            .task(Task.item(TagPrefix.dust, GTMaterials.Rutile, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Rutile, 2)
            .dependsOn(ILMENITE, BAUXITE));

    public static final Quest TITANIUM_TETRACHLORIDE = CHAPTER.quest("titanium_tetrachloride", q -> q
            .at(10, 22)
            .title("四氯化钛")
            .desc(d -> d
                    .line("这是最后一步了！")
                    .line()
                    .line("四氯化钛也可用于催化聚合"))
            .task(Task.item(GTMaterials.TitaniumTetrachloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(RUTILE, BLAZE_POWDER_COPY_3));

    public static final Quest HOT_TITANIUM = CHAPTER.quest("hot_titanium", q -> q
            .at(8, 20)
            .title("热钛锭")
            .task(Task.item(TagPrefix.ingotHot, GTMaterials.Titanium, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Magnesium, 8)
            .dependsOn(TITANIUM_TETRACHLORIDE, MAGNESIUM_COPY_1));

    public static final Quest VACUUM_FREEZER = CHAPTER.quest("vacuum_freezer", q -> q
            .at(10, 12)
            .milestone()
            .title("真空冷冻机")
            .subtitle("真正的冰箱")
            .desc(d -> d.line("简单的冷却浸洗已经不再能承受其他热锭的高温，你需要一个真正的冷冻机"))
            .task(Task.item(GTMultiMachines.VACUUM_FREEZER))
            .scaledReward(GTItems.ELECTRIC_PUMP_HV, 1)
            .dependsOn(HV_COMPONENTS_COPY_4, WORKSTATION));

    public static final Quest TITANIUM = CHAPTER.quest("titanium", q -> q
            .at(6, 18)
            .milestone()
            .title("钛锭")
            .desc(d -> d.line("你的努力终有回报"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Titanium, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.Titanium, 2)
            .dependsOn(HOT_TITANIUM, VACUUM_FREEZER));

    public static final Quest EV_CASING = CHAPTER.quest("ev_casing", q -> q
            .at(2, 20)
            .title("EV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_EV))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 1)
            .dependsOn(TITANIUM));

    public static final Quest EV_HULL = CHAPTER.quest("ev_hull", q -> q
            .at(0, 18)
            .milestone()
            .title("EV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", DARK_PURPLE)
            .desc(d -> d
                    .line("我建议你首先发射二阶火箭获取{钨}，然后开始着手设计你的{铂处}产线", GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.EV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.EV], 1))
            .dependsOn(EV_CASING));

    public static final Quest RECYCLER = CHAPTER.quest("recycler", q -> q
            .at(0, 20)
            .side()
            .desc(d -> d.line("废料盒右键可以抽奖，也可以放入本机器自动抽奖"))
            .task(Task.item(MultiBlockA.RECYCLER))
            .dependsOn(EV_HULL));

    public static final Quest HARDER_AE2_TWEAKS_EXPERT = CHAPTER.quest("harder_ae2_tweaks_expert", q -> q
            .at(-2, 2)
            .side()
            .title("更难的AE2魔改{（专家）}", LIGHT_PURPLE)
            .subtitle("前面的区域，EV再探索吧")
            .desc(d -> d.line("专家模式下，如果魔法没有发展到精灵钢，那么必须要等到EV才可以开始自动化"))
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(UtilChapter.Q_1FB1EC5B, NewMvChapter.HV_HULL));

    public static final Quest CONFIGURABLE_MAINTENANCE_HATCH = CHAPTER.quest("configurable_maintenance_hatch", q -> q
            .at(-4, 0)
            .side()
            .desc(d -> d
                    .line("{负荷调节维护仓}能使你通过调整维护仓获得{免费的机器运行速度}，但换来的代价是更频繁的维护问题", GOLD, YELLOW)
                    .line()
                    .line("你也可以降低机器运行速度以换来更少的维护问题"))
            .task(Task.item(GTMachines.CONFIGURABLE_MAINTENANCE_HATCH))
            .dependsOn(NewMvChapter.HV_HULL, NewMvChapter.ADVANCED_IC));

    public static final Quest HV_MACERATOR = CHAPTER.quest("hv_macerator", q -> q
            .at(-2, -4)
            .side()
            .desc(d -> d.line("终于，我们有了研磨副产物"))
            .task(Task.item(GTMachines.MACERATOR[GTValues.HV]))
            .dependsOn(NewMvChapter.HV_HULL, NewMvChapter.ADVANCED_IC));

    public static final Quest HV_SUPERCONDUCTIVITY = CHAPTER.quest("hv_superconductivity", q -> q
            .at(-4, 2)
            .side()
            .title("HV超导")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.VibrantAlloy))
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest INFINITE_WATER_COVER = CHAPTER.quest("infinite_water_cover", q -> q
            .at(-4, -2)
            .side()
            .task(Task.item(GTItems.COVER_INFINITE_WATER))
            .dependsOn(NewMvChapter.HV_HULL, NewMvChapter.ADVANCED_IC));

    public static final Quest MULTI_SMELTER = CHAPTER.quest("multi_smelter", q -> q
            .at(0, 2)
            .side()
            .desc(d -> d
                    .line("能够执行电炉配方，根据{线圈}和{能源仓}的等级拥有更高的并行和超频", GOLD, GOLD)
                    .line()
                    .line("你决定在必要的时候用这个替换掉你的{蒸汽熔炉}", GOLD))
            .task(Task.item(GTMultiMachines.MULTI_SMELTER))
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest STEAM_LARGE_TURBINE = CHAPTER.quest("steam_large_turbine", q -> q
            .at(2, -4)
            .side()
            .desc(d -> d
                    .line("HV蒸汽轮机倒下了，但另一位老大哥站起来了")
                    .line()
                    .line("你的第一个真正的多方块发电设施，通过提升{转子支架}等级获得{指数级}的功率提升", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("放入其中的涡轮转子材料影响发电效率和功率"))
            .task(Task.item(GTMultiMachines.LARGE_STEAM_TURBINE))
            .optional()
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest EVAPORATION_PLANT = CHAPTER.quest("evaporation_plant", q -> q
            .at(-2, -2)
            .side()
            .desc(d -> d.line("蒸发塔用来获取盐水和粗卤水以制取{氯、溴和碘}", GOLD))
            .task(Task.item(MultiBlockA.EVAPORATION_PLANT))
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest PROSPECTOR_HV = CHAPTER.quest("prospector_hv", q -> q
            .at(8, 4)
            .side()
            .desc(d -> d
                    .line("HV阶段你可以做出更高一级的探矿仪，可以勘探实体矿脉，基岩矿脉和基岩流体")
                    .line()
                    .line("可以查看探矿仪的EMI来获取矿脉和流体信息"))
            .task(Task.item(GTItems.PROSPECTOR_HV))
            .dependsOn(HV_EMITTER_SENSOR));

    public static final Quest Q_47E810FD = CHAPTER.quest("q_47e810fd", q -> q
            .at(18, 4)
            .side()
            .task(Task.checkmark().title("更便宜的贴片元件！"))
            .dependsOn(HV_ASSEMBLER));

    public static final Quest SPACE_NANOMUSCLE_CHESTPLATE = CHAPTER.quest("space_nanomuscle_chestplate", q -> q
            .at(6, 8)
            .side()
            .subtitle("还有高手")
            .desc(d -> d
                    .line("这个比{宇航服}更好！", GOLD)
                    .line()
                    .line("{EV阶段}你还能制作{进阶版的太空纳米胸甲}", EV, GOLD)
                    .line()
                    .line("{别问我整合包里为什么加入了一个比宇航服更好的衣服，却还让你在任务流程中制作它们……}", GRAY.italic()))
            .task(Task.item(GTOItems.SPACE_NANOMUSCLE_CHESTPLATE))
            .optional()
            .dependsOn(HV_EMITTER_SENSOR));

    public static final Quest FISHING_GROUND = CHAPTER.quest("fishing_ground", q -> q
            .at(6, 4)
            .side()
            .desc(d -> d
                    .line("可以养鱼，也可以调节编程电路自动钓鱼")
                    .line()
                    .line("要这么多{鱼}有什么用？看看{鱼油}", GOLD, GOLD))
            .task(Task.item(MultiBlockA.FISHING_GROUND))
            .dependsOn(HV_EMITTER_SENSOR, WORKSTATION_COPY_1));

    public static final Quest HV_DRONE = CHAPTER.quest("hv_drone", q -> q
            .at(10, 8)
            .side()
            .subtitle("解放维护与掏灰")
            .desc(d -> d
                    .line("拥有{EV级电路}后你可以开始使用{无人机}自动化清理{消声仓}的{灰烬}和维护{维护仓}", YELLOW, GOLD, YELLOW, YELLOW, YELLOW)
                    .line()
                    .line("建造一座{无人机控制中心}，派出你的{无人机}", GOLD, GOLD)
                    .line()
                    .line("{记得给这些小家伙充电}", YELLOW)
                    .line()
                    .line("前往查看{无人机控制中心}", link(NewHvChapter.DRONE_CONTROL_CENTER)))
            .task(Task.item(GTOItems.HV_DRONE))
            .dependsOn(WORKSTATION, HV_EMITTER_SENSOR_COPY_1));

    public static final Quest DRONE_CONTROL_CENTER = CHAPTER.quest("drone_control_center", q -> q
            .at(12, 8)
            .side()
            .icon(MultiBlockG.DRONE_CONTROL_CENTER)
            .desc(d -> d
                    .line("根据无人机范围，{在消声仓将满的时候自动掏出消声仓的灰烬}（灰烬通过该机器的输出总线输出），同时{自动修复维护仓的维护问题}", YELLOW, YELLOW)
                    .line()
                    .line("每次操作有几秒的冷却时间，并且消耗无人机少量的电")
                    .line()
                    .line("放入更多无人机可同时进行多项工作")
                    .line()
                    .line("EV以后配合{无线充能器}可以直接给无人机仓的无人机充电", link(NewLvChapter.WIRELESS_CHARGER))
                    .line()
                    .line("{别再问为什么无人机不工作啦，也许是你的机器还没有到需要它们工作的时候呢}", GRAY.italic()))
            .task(Task.item(MultiBlockG.DRONE_CONTROL_CENTER), Task.item(GTOMachines.DRONE_HATCH[GTValues.HV]))
            .dependsOn(HV_DRONE));

    public static final Quest AUTO_MAINTENANCE_HATCH = CHAPTER.quest("auto_maintenance_hatch", q -> q
            .at(10, 4)
            .side()
            .desc(d -> d.line("{自动维护仓}为你免去了维护的痛苦", GOLD))
            .task(Task.item(GTMachines.AUTO_MAINTENANCE_HATCH))
            .dependsOn(WORKSTATION));

    public static final Quest Q_50ACA7F4 = CHAPTER.quest("q_50aca7f4", q -> q
            .at(12, 4)
            .side()
            .icon(GTMaterials.RawOil.getBucket())
            .desc(d -> d.line("是时候升级你的原始石化产线了！"))
            .task(Task.checkmark().title("进阶石化产线"))
            .dependsOn(WORKSTATION));

    public static final Quest PRIMARY_DISTILLATION = CHAPTER.quest("primary_distillation", q -> q
            .at(12, 2)
            .side()
            .title("初级蒸馏")
            .desc(d -> d.line("电力蒸馏塔"))
            .task(Task.item(GTMultiMachines.DISTILLATION_TOWER))
            .dependsOn(Q_50ACA7F4));

    public static final Quest DESULFURIZATION = CHAPTER.quest("desulfurization", q -> q
            .at(10, 2)
            .side()
            .title("脱硫")
            .desc(d -> d.line("专业脱硫机器，比单方块化学反应釜高效得多！"))
            .task(Task.item(MultiBlockB.DESULFURIZER))
            .dependsOn(PRIMARY_DISTILLATION));

    public static final Quest SECONDARY_DISTILLATION = CHAPTER.quest("secondary_distillation", q -> q
            .at(8, 2)
            .side()
            .title("二次蒸馏")
            .desc(d -> d.line("和{原始蒸馏塔}一样，你也需要造第二台{蒸馏塔}来二次蒸馏裂化油", GOLD, GOLD))
            .task(Task.item(GTMultiMachines.DISTILLATION_TOWER))
            .dependsOn(DESULFURIZATION));

    public static final Quest POLYETHYLENE_INGOT = CHAPTER.quest("polyethylene_ingot", q -> q
            .at(-2, 6)
            .side()
            .desc(d -> d.line("你现在可以更加高效地生产{聚乙烯}，产出比最大的配方需要一点{四氯化钛}", GOLD, GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polyethylene))
            .dependsOn(POLYMERIZATION_REACTOR));

    public static final Quest POLYVINYL_CHLORIDE_INGOT = CHAPTER.quest("polyvinyl_chloride_ingot", q -> q
            .at(-2, 8)
            .side()
            .desc(d -> d.line("你现在可以更加高效地生产{聚氯乙烯}，产出比最大的配方需要一点{四氯化钛}", GOLD, GOLD))
            .task(Task.item(TagPrefix.ingot, GTMaterials.PolyvinylChloride))
            .dependsOn(POLYMERIZATION_REACTOR));

    public static final Quest INTEGRAL_FRAMEWORK_MV = CHAPTER.quest("integral_framework_mv", q -> q
            .at(8, 14)
            .side()
            .desc(d -> d
                    .line("{MV}等级的整体框架可以让你的通用工厂执行{MV}级别以下的所有配方", MV, MV)
                    .line()
                    .line("如果你还没有大量铺设通用工厂，是时候这么做了"))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_MV))
            .dependsOn(VACUUM_FREEZER, HV_ENERGY_HATCH_COPY_1));

    public static final Quest MV_FIELD_GENERATOR = CHAPTER.quest("mv_field_generator", q -> q
            .at(12, 10)
            .side()
            .task(Task.item(GTItems.FIELD_GENERATOR_MV))
            .dependsOn(VACUUM_FREEZER));

    public static final Quest VOID_TRANSPORTER = CHAPTER.quest("void_transporter", q -> q
            .at(14, 10)
            .side()
            .desc(d -> d
                    .line("你现在可以前往虚空/超平坦世界搭建你的基地")
                    .line()
                    .line("搭建这个多方块结构，并给它充能")
                    .line()
                    .line("黑曜石对应虚空世界，哭泣黑曜石对应超平坦世界"))
            .task(Task.item(MultiBlockG.VOID_TRANSPORTER))
            .dependsOn(ENDER_EYE_COPY_1, MV_FIELD_GENERATOR));

    public static final Quest PROGRAMMABLE_CHAMBER_ROOM = CHAPTER.quest("programmable_chamber_room", q -> q
            .at(10, 10)
            .side()
            .minWidth(250)
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
            .dependsOn(MV_FIELD_GENERATOR));

    public static final Quest VISIT_FLAT = CHAPTER.quest("visit_flat", q -> q
            .at(16, 8)
            .side()
            .icon(Items.CRYING_OBSIDIAN)
            .desc(d -> d
                    .line("你可以在超平坦世界建造你的基地")
                    .line()
                    .line("{如果你需要一些哭泣的黑曜石，可以去看看黑曜石的tooltips}", GRAY.italic()))
            .task(Task.dimension(Dimension.FLAT).title("访问：超平坦世界"))
            .dependsOn(VOID_TRANSPORTER));

    public static final Quest VISIT_VOID = CHAPTER.quest("visit_void", q -> q
            .at(16, 10)
            .side()
            .icon(Items.OBSIDIAN)
            .desc(d -> d.line("你可以在虚空世界建造你的基地"))
            .task(Task.dimension(Dimension.VOID).title("访问：虚空世界"))
            .dependsOn(VOID_TRANSPORTER));

    public static final Quest VISIT_NETHER = CHAPTER.quest("visit_nether", q -> q
            .at(18, 8)
            .side()
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
            .at(8, 10)
            .side()
            .desc(d -> d
                    .line("将{可编程覆盖版}贴到{单方块机器}上，可为其添加{虚拟物品槽}", GOLD, AQUA, YELLOW)
                    .line()
                    .line("没有{虚拟物品槽}的机器是{无法接受}虚拟物品的", YELLOW, RED))
            .task(Task.item(CustomItems.PROGRAMMABLE_COVER))
            .dependsOn(PROGRAMMABLE_CHAMBER_ROOM));

    public static final Quest NAND_CHIP = CHAPTER.quest("nand_chip", q -> q
            .at(18, 10)
            .side()
            .desc(d -> d.line("另一种ULV电路"))
            .task(Task.item(GTItems.NAND_CHIP_ULV))
            .dependsOn(SIMPLE_SOC));

    public static final Quest GAS_LARGE_TURBINE = CHAPTER.quest("gas_large_turbine", q -> q
            .at(0, 16)
            .side()
            .task(Task.item(GTMultiMachines.LARGE_GAS_TURBINE))
            .optional()
            .dependsOn(EV_HULL));

    public static final Quest Q_132AD659 = CHAPTER.quest("q_132ad659", q -> q
            .at(2, 2)
            .side()
            .icon(QuestExternalRefs.FunctionalStorageItems.MAX_STORAGE_UPGRADE)
            .desc(d -> d
                    .line("看看右面这两个多方块，它们能够提升你的产能！")
                    .line()
                    .line("如果你早就做出来了那就更好啦"))
            .task(Task.checkmark().title("提升产能?"))
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest CLEANROOM_EASY = CHAPTER.quest("cleanroom_easy", q -> q
            .at(2, -2)
            .side()
            .icon(GTMultiMachines.CLEANROOM)
            .title("超净间{（简单）}", GREEN)
            .desc(d -> d.line("简单模式机器不需要超净环境也能工作，因此我在这里直接送你一份用于过任务"))
            .task(Task.checkmark())
            .reward(Reward.item(GTMultiMachines.CLEANROOM), Reward.item(GTBlocks.FILTER_CASING, 8))
            .optional().hideUntilDepsVisible()
            .dependsOn(UtilChapter.Q_056EC4E6, NewMvChapter.HV_HULL));

    public static final Quest HV_COMPONENT_SUBSIDY = CHAPTER.quest("hv_component_subsidy", q -> q
            .at(0, -4)
            .side()
            .icon(GTItems.ELECTRIC_MOTOR_HV)
            .title("HV部件补助")
            .desc(d -> d.line("从HV开始，简单不再有额外补助。用自己的双手创造一切吧！"))
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTItems.ELECTRIC_MOTOR_HV, 32))
            .optional().hideUntilDepsVisible()
            .dependsOn(NewMvChapter.HV_HULL));

    public static final Quest THREE_DIMENSIONAL_PRINTER = CHAPTER.quest("three_dimensional_printer", q -> q
            .at(8, 16)
            .side()
            .desc(d -> d
                    .line("一步完成外壳生产，也可以用来做{一次性工具}", GOLD)
                    .line()
                    .line("{实体工具}是不能放进样板的，只有{一次性工具}可以放进样板", GOLD, GOLD))
            .task(Task.item(MultiBlockC.THREE_DIMENSIONAL_PRINTER))
            .dependsOn(TITANIUM));

    public static final Quest TITANIUM_LARGE_BOILER = CHAPTER.quest("titanium_large_boiler", q -> q
            .at(6, 20)
            .side()
            .desc(d -> d.line("比{大型钢锅炉}更强大的锅炉，喂饱你的{蒸汽涡轮}", YELLOW, YELLOW))
            .task(Task.item(GTMultiMachines.LARGE_BOILER_TITANIUM))
            .dependsOn(TITANIUM));

    public static final Quest PULSATING_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("pulsating_photovoltaic_power_station", q -> q
            .at(4, 22)
            .side()
            .desc(d -> d.line("T2级别的{光伏电站}，比上一等级拥有更高的发电量", GOLD))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_PULSATING))
            .optional()
            .dependsOn(TITANIUM));

    public static final Quest INTEGRAL_FRAMEWORK_HV = CHAPTER.quest("integral_framework_hv", q -> q
            .at(6, 16)
            .side()
            .desc(d -> d.line("如果你通过其他手段提前搞到了一点钛，可以优先制作{HV}整体框架，非常值得", HV))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_HV))
            .dependsOn(TITANIUM));

    public static final Quest LARGE_STEAM_INPUT_HATCH = CHAPTER.quest("large_steam_input_hatch", q -> q
            .at(4, 16)
            .side()
            .desc(d -> d
                    .line("大型蒸汽输入仓能使多方块蒸汽机器提升一级{配方等级}，同时具有{超频}能力", GOLD, GOLD)
                    .line()
                    .line("每提升一级超频，蒸汽消耗量提升{4x}，速度提升{2x}", DARK_GREEN, DARK_GREEN))
            .task(Task.item(GTOMachines.LARGE_STEAM_HATCH))
            .dependsOn(NewLvChapter.PRECISION_STEAM_MECHANISM, TITANIUM));

    public static final Quest Q_1A1DFCA1 = CHAPTER.quest("q_1a1dfca1", q -> q
            .at(12, 20)
            .side()
            .icon(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV)
            .desc(d -> d
                    .line("以你目前的科技水平，无线电网无法覆盖其他维度。万幸的是，你可以跨维度传输物品，因此你可以传输燃料到其他维度进行发电，或者铺设太阳能。")
                    .line()
                    .line("在IV阶段，你将解锁{无线电网维度中继器}，它建造在哪个维度，哪个维度就会共用基地的无线电网。", GRAY.underline()))
            .task(Task.checkmark().title("其他维度如何用电"))
            .dependsOn(MOON));

    public static final Quest HV_BEDROCK_ORE_MINER = CHAPTER.quest("hv_bedrock_ore_miner", q -> q
            .at(2, 22)
            .side()
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.HV]))
            .dependsOn(TITANIUM));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewMvChapter.HV_HULL, 0, 0))
                .mainLine(NewMvChapter.HV_HULL, IMPLOSION_COMPRESSOR, PLASTIC_BOARD, CPU, CLEANROOM, MICROPROCESSOR, PROCESSOR_ASSEMBLY, HV_VOLTAGE_COIL, WORKSTATION, HV_EMITTER_SENSOR, HV_ENERGY_HATCH, POLYMERIZATION_REACTOR, SCAN_SATELLITE, SATELLITE_CONTROL_CENTER, VACUUM_FREEZER, HERMETIC_CASINGS, SIMPLE_SOC, ROCKET_ASSEMBLER, TIER_1_ROCKET, MOON, TITANIUM, EV_HULL));
    }
}
