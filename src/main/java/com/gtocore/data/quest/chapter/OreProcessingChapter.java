package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class OreProcessingChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("ore_processing", c -> c
            .title("资源获取与处理")
            .subtitle("Ore Processing")
            .icon(MultiBlockD.RESOURCE_COLLECTION_MODULE));

    public static final Quest MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK = CHAPTER.quest("mineral_resource_acquisition_methods_multi_block", q -> q
            .at(-8, -7.5)
            .icon(MultiBlockD.EYE_OF_HARMONY)
            .title("矿物资源获取手段--多方块机器")
            .desc(d -> d.line("聪明的你应该注意到矿物资源的需求过大，接下来将一一简单介绍一些多方块机器让你熟悉下"))
            .task(Task.checkmark()));

    public static final Quest MV_BEDROCK_ORE_MINER = CHAPTER.quest("mv_bedrock_ore_miner", q -> q
            .at(-4, -9)
            .subtitle("钻井，狠狠的钻")
            .desc(d -> d
                    .line("平平无奇的采矿机。唯独不同的是，采用的是和油田一样的机制，随着持续采集，矿脉会逐渐枯竭")
                    .line()
                    .line("更进阶的版本产矿速度更快且更多，矿脉损耗更慢"))
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.MV]))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest HV_BEDROCK_ORE_MINER = CHAPTER.quest("hv_bedrock_ore_miner", q -> q
            .at(-1, -9)
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.HV]))
            .dependsOn(MV_BEDROCK_ORE_MINER));

    public static final Quest EV_BEDROCK_ORE_MINER = CHAPTER.quest("ev_bedrock_ore_miner", q -> q
            .at(2, -9)
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.EV]))
            .dependsOn(HV_BEDROCK_ORE_MINER));

    public static final Quest VOID_MINER = CHAPTER.quest("void_miner", q -> q
            .at(-4, -6)
            .subtitle("终于可以不用到处插矿机了")
            .desc(d -> d.line("该机器将根据所提供的维度数据采集对应维度的产出矿石"))
            .task(Task.item(GTMultiMachines.LARGE_PLASMA_TURBINE))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest LARGE_VOID_MINER = CHAPTER.quest("large_void_miner", q -> q
            .at(-1, -6)
            .subtitle("我猜，你是闭着眼选择随机采矿的")
            .desc(d -> d
                    .line("该机器有两种模式，精准模式需要原矿方块配合生物线的培养出精华")
                    .line()
                    .line("在你能够建造{太空电梯}之前，这是少有的大量获取一些稀缺矿物的手段了）", GOLD)
                    .line()
                    .line("它也可以直接连上电，然后随机获取矿石")
                    .line()
                    .line("电压越高并行越高，稀缺矿物概率也会随之增高"))
            .task(Task.item(MultiBlockA.LARGE_VOID_MINER))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest RESOURCE_COLLECTION_MODULE = CHAPTER.quest("resource_collection_module", q -> q
            .at(2, -6)
            .subtitle("采矿机器人辛勤工作中")
            .desc(d -> d.line("做好火箭燃料产线，就是另外一种矿机"))
            .task(Task.item(MultiBlockD.RESOURCE_COLLECTION_MODULE))
            .dependsOn(LARGE_VOID_MINER));

    public static final Quest THIS_IS_ALSO_A_MINING_MACHINE = CHAPTER.quest("this_is_also_a_mining_machine", q -> q
            .at(-1, -3)
            .title("这也是矿机！")
            .subtitle("都这时候了，我还缺这点粉么")
            .task(Task.item(MultiBlockD.EYE_OF_HARMONY))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest Q_6582357D = CHAPTER.quest("q_6582357d", q -> q
            .at(-37, -18)
            .icon(TagPrefix.dustPure, GTMaterials.Cobaltite)
            .desc(d -> d.line("这里是一些矿石处理的机器"))
            .task(Task.checkmark().title("矿石处理")));

    public static final Quest LARGE_CENTRIFUGE = CHAPTER.quest("large_centrifuge", q -> q
            .at(-27, -18)
            .task(Task.item(GCYMMachines.LARGE_CENTRIFUGE))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, Q_6582357D));

    public static final Quest LARGE_MACERATION_TOWER = CHAPTER.quest("large_maceration_tower", q -> q
            .at(-31, -18)
            .task(Task.item(GCYMMachines.LARGE_MACERATION_TOWER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, Q_6582357D));

    public static final Quest LARGE_SIFTING_FUNNEL = CHAPTER.quest("large_sifting_funnel", q -> q
            .at(-25, -18)
            .task(Task.item(GCYMMachines.LARGE_SIFTING_FUNNEL))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, Q_6582357D));

    public static final Quest LARGE_CHEMICAL_BATH = CHAPTER.quest("large_chemical_bath", q -> q
            .at(-29, -18)
            .task(Task.item(GCYMMachines.LARGE_CHEMICAL_BATH))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, Q_6582357D));

    public static final Quest LARGE_CRUSHER = CHAPTER.quest("large_crusher", q -> q
            .at(-33, -18)
            .task(Task.item(GCYMMachines.LARGE_CRUSHER))
            .dependsOn(IvChapter.WELCOME_TO_THE_HIGH_PRESSURE_STAGE, Q_6582357D));

    public static final Quest ISA_MILL = CHAPTER.quest("isa_mill", q -> q
            .at(-33, -16)
            .task(Task.item(MultiBlockD.ISA_MILL))
            .dependsOn(Q_6582357D, IvChapter.ASSEMBLY_LINE));

    public static final Quest INDUSTRIAL_FLOTATION_CELL = CHAPTER.quest("industrial_flotation_cell", q -> q
            .at(-31, -16)
            .task(Task.item(MultiBlockA.INDUSTRIAL_FLOTATION_CELL))
            .dependsOn(ISA_MILL));

    public static final Quest VACUUM_DRYING_FURNACE = CHAPTER.quest("vacuum_drying_furnace", q -> q
            .at(-29, -16)
            .task(Task.item(MultiBlockA.VACUUM_DRYING_FURNACE))
            .dependsOn(INDUSTRIAL_FLOTATION_CELL));

    public static final Quest FLUID_RESOURCE_ACQUISITION_METHODS_MULTIBLOCK_MACHINES = CHAPTER.quest("fluid_resource_acquisition_methods_multiblock_machines", q -> q
            .at(-8, -18)
            .icon(MultiBlockD.EYE_OF_HARMONY)
            .title("流体资源获取手段--多方块机器")
            .desc(d -> d.line("聪明的你应该注意到许多流体资源深深藏在基岩之下，接下来将一一简单介绍一些多方块机器让你熟悉下"))
            .task(Task.checkmark()));

    public static final Quest MV_FLUID_DRILLING_RIG = CHAPTER.quest("mv_fluid_drilling_rig", q -> q
            .at(-4, -18)
            .desc(d -> d.line("终于！你可以拿这个钻取基岩之下的流体"))
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.MV]))
            .dependsOn(FLUID_RESOURCE_ACQUISITION_METHODS_MULTIBLOCK_MACHINES));

    public static final Quest HV_FLUID_DRILLING_RIG = CHAPTER.quest("hv_fluid_drilling_rig", q -> q
            .at(-1, -18)
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.HV]))
            .dependsOn(MV_FLUID_DRILLING_RIG));

    public static final Quest EV_FLUID_DRILLING_RIG = CHAPTER.quest("ev_fluid_drilling_rig", q -> q
            .at(2, -18)
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.EV]))
            .dependsOn(HV_FLUID_DRILLING_RIG));

    public static final Quest INFINITY_FLUID_DRILLING_RIG = CHAPTER.quest("infinity_fluid_drilling_rig", q -> q
            .at(-1, -15)
            .desc(d -> d.line("与采矿机不同，流体钻机有0%损耗的版本。不用给你的钻机搬家了！"))
            .task(Task.item(MultiBlockD.INFINITY_FLUID_DRILLING_RIG))
            .dependsOn(FLUID_RESOURCE_ACQUISITION_METHODS_MULTIBLOCK_MACHINES));

    public static final Quest VOID_FLUID_DRILLING_RIG = CHAPTER.quest("void_fluid_drilling_rig", q -> q
            .at(-4, -21)
            .desc(d -> d.line("该机器将根据所提供的维度数据和电路采集对应维度的流体"))
            .task(Task.item(MultiBlockA.VOID_FLUID_DRILLING_RIG))
            .dependsOn(FLUID_RESOURCE_ACQUISITION_METHODS_MULTIBLOCK_MACHINES));

    public static final Quest DRILLING_CONTROL_CENTER = CHAPTER.quest("drilling_control_center", q -> q
            .at(-4, -15)
            .desc(d -> d.line("用这个免去大批量铺设钻机的苦恼（虽然你可能还是需要铺不少）"))
            .task(Task.item(MultiBlockG.DRILLING_CONTROL_CENTER))
            .dependsOn(FLUID_RESOURCE_ACQUISITION_METHODS_MULTIBLOCK_MACHINES));

    public static final Quest ADVANCED_INFINITE_DRILLER = CHAPTER.quest("advanced_infinite_driller", q -> q
            .at(2, -15)
            .task(Task.item(MultiBlockC.ADVANCED_INFINITE_DRILLER))
            .dependsOn(INFINITY_FLUID_DRILLING_RIG));

    public static final Quest MEGA_SE_MINING = CHAPTER.quest("mega_se_mining", q -> q
            .at(-1, -21)
            .desc(d -> d.line("做好火箭燃料产线，就是另外一种矿机"))
            .task(Task.item(MultiBlockH.MEGA_RESOURCE_MINING))
            .dependsOn(VOID_FLUID_DRILLING_RIG));

    public static final Quest LV_PUMP = CHAPTER.quest("lv_pump", q -> q
            .at(-11.5, -18.5)
            .desc(d -> d.line("只能采到实际存在的流体--主要用来抽取石油"))
            .task(Task.item(GTMachines.PUMP[GTValues.LV])));

    public static final Quest MV_PUMP = CHAPTER.quest("mv_pump", q -> q
            .at(-10.5, -18.5)
            .task(Task.item(GTMachines.PUMP[GTValues.MV])));

    public static final Quest HV_PUMP = CHAPTER.quest("hv_pump", q -> q
            .at(-11.5, -17.5)
            .task(Task.item(GTMachines.PUMP[GTValues.HV])));

    public static final Quest EV_PUMP = CHAPTER.quest("ev_pump", q -> q
            .at(-10.5, -17.5)
            .task(Task.item(GTMachines.PUMP[GTValues.EV])));

    public static final Quest DIGITAL_MINER = CHAPTER.quest("digital_miner", q -> q
            .at(-4, -12)
            .desc(d -> d
                    .line("这个系列挖掘的都是实体矿石")
                    .line()
                    .line("该多方块系列机器的精准模式可以采集到原矿方块，该标签下的矿石都是2-3倍于普通的粉碎产出")
                    .line()
                    .line("如果你需要更多的矿石或者原矿就选择他，但不要指望速度多快。记得标记区块加载！"))
            .task(Task.item(MultiBlockA.DIGITAL_MINER))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest EV_LARGE_MINER = CHAPTER.quest("ev_large_miner", q -> q
            .at(-1, -12)
            .subtitle("精准采集！")
            .task(Task.item(GTMultiMachines.LARGE_MINER[GTValues.EV]))
            .dependsOn(DIGITAL_MINER));

    public static final Quest IV_LARGE_MINER = CHAPTER.quest("iv_large_miner", q -> q
            .at(2, -12)
            .desc(d -> d.line("更大的工作范围和电压需求"))
            .task(Task.item(GTMultiMachines.LARGE_MINER[GTValues.IV]))
            .dependsOn(EV_LARGE_MINER));

    public static final Quest LUV_LARGE_MINER = CHAPTER.quest("luv_large_miner", q -> q
            .at(5, -12)
            .desc(d -> d.line("与前者同理"))
            .task(Task.item(GTMultiMachines.LARGE_MINER[GTValues.LuV]))
            .dependsOn(IV_LARGE_MINER));

    public static final Quest LP_STEAM_MINER = CHAPTER.quest("lp_steam_miner", q -> q
            .at(-11.5, -9)
            .desc(d -> d.line("蒸汽的版本不要做，没有实际价值"))
            .task(Task.item(GTMachines.STEAM_MINER.first())));

    public static final Quest HP_STEAM_MINER = CHAPTER.quest("hp_steam_miner", q -> q
            .at(-10.5, -9)
            .task(Task.item(GTMachines.STEAM_MINER.second())));

    public static final Quest STEAM_CRUSHER = CHAPTER.quest("steam_crusher", q -> q
            .at(-33, -20)
            .desc(d -> d
                    .line("这三台机器是你前期可以搭建的最小矿处")
                    .line()
                    .line("当然你也可以选择其他流程"))
            .task(Task.item(MultiBlockC.STEAM_CRUSHER))
            .dependsOn(Q_6582357D));

    public static final Quest PLANET_CORE_DRILLING = CHAPTER.quest("planet_core_drilling", q -> q
            .at(-4, -3)
            .task(Task.item(MultiBlockC.PLANET_CORE_DRILLING))
            .dependsOn(MINERAL_RESOURCE_ACQUISITION_METHODS_MULTI_BLOCK));

    public static final Quest LV_MINER = CHAPTER.quest("lv_miner", q -> q
            .at(-11.5, -8)
            .desc(d -> d.line("放在实际存在的矿脉上方采矿"))
            .task(Task.item(GTMachines.MINER[GTValues.LV])));

    public static final Quest MV_MINER = CHAPTER.quest("mv_miner", q -> q
            .at(-10.5, -8)
            .task(Task.item(GTMachines.MINER[GTValues.MV])));

    public static final Quest HV_MINER = CHAPTER.quest("hv_miner", q -> q
            .at(-11.5, -7)
            .task(Task.item(GTMachines.MINER[GTValues.HV])));

    public static final Quest LV_DIGITAL_MINER = CHAPTER.quest("lv_digital_miner", q -> q
            .at(-10.5, -7)
            .task(Task.item(GTOMachines.DIGITAL_MINER[GTValues.LV])));

    public static final Quest MV_DIGITAL_MINER = CHAPTER.quest("mv_digital_miner", q -> q
            .at(-11.5, -6)
            .task(Task.item(GTOMachines.DIGITAL_MINER[GTValues.MV])));

    public static final Quest HV_DIGITAL_MINER = CHAPTER.quest("hv_digital_miner", q -> q
            .at(-10.5, -6)
            .task(Task.item(GTOMachines.DIGITAL_MINER[GTValues.HV])));

    public static final Quest STEAM_PISTON_HAMMER = CHAPTER.quest("steam_piston_hammer", q -> q
            .at(-31, -20)
            .task(Task.item(MultiBlockA.STEAM_PISTON_HAMMER))
            .dependsOn(STEAM_CRUSHER));

    public static final Quest STEAM_ORE_WASHER = CHAPTER.quest("steam_ore_washer", q -> q
            .at(-29, -20)
            .task(Task.item(MultiBlockA.STEAM_ORE_WASHER))
            .dependsOn(STEAM_PISTON_HAMMER));

    public static final Quest Q_15815409 = CHAPTER.quest("q_15815409", q -> q
            .at(-13, -18)
            .task(Task.checkmark().title("单方块泵")));

    public static final Quest Q_7B09C06E = CHAPTER.quest("q_7b09c06e", q -> q
            .at(-13, -7.5)
            .task(Task.checkmark().title("单方块采矿机")));

    public static final Quest RAW_IRON = CHAPTER.quest("raw_iron", q -> q
            .at(-37, -11)
            .icon(Items.RAW_IRON)
            .subtitle("c:raw_materials")
            .desc(d -> d
                    .line("开挖")
                    .line()
                    .line("吃时运哦"))
            .task(Task.checkmark().title("粗矿"), Task.item(Items.RAW_IRON).optional()));

    public static final Quest IRON_ORE = CHAPTER.quest("iron_ore", q -> q
            .at(-37, -7)
            .icon(TagPrefix.ore, GTMaterials.Iron)
            .subtitle("forge:ores")
            .desc(d -> d.line("如果你有精准采集"))
            .task(Task.checkmark().title("矿石"), Task.item(TagPrefix.ore, GTMaterials.Iron).optional()));

    public static final Quest Q_41AF3C6D = CHAPTER.quest("q_41af3c6d", q -> q
            .at(-33, -9)
            .desc(d -> d.line("进行后续操作时，原矿相比矿石产出翻倍"))
            .task(Task.checkmark())
            .dependsOn(RAW_IRON, IRON_ORE));

    public static final Quest CRUSHED_IRON_ORE = CHAPTER.quest("crushed_iron_ore", q -> q
            .at(-29, -9)
            .icon(TagPrefix.crushed, GTMaterials.Iron)
            .subtitle("c:crushed_ores")
            .desc(d -> d.line("把原矿和矿石塞进粉碎机狠狠粉碎(快)"))
            .task(Task.checkmark().title("粉碎矿石"), Task.item(TagPrefix.crushed, GTMaterials.Iron).optional())
            .dependsOn(Q_41AF3C6D)
            .requireAny()
            .requireCount(1));

    public static final Quest IMPURE_IRON_DUST = CHAPTER.quest("impure_iron_dust", q -> q
            .at(-21, -6)
            .icon(TagPrefix.dustImpure, GTMaterials.Iron)
            .subtitle("c:impure_dusts")
            .desc(d -> d.line("粉碎矿石 -> 锻造(快)/研磨(慢)"))
            .task(Task.checkmark().title("含杂粉"), Task.item(TagPrefix.dustImpure, GTMaterials.Iron).optional())
            .dependsOn(CRUSHED_IRON_ORE));

    public static final Quest MILLED_PYROPE = CHAPTER.quest("milled_pyrope", q -> q
            .at(-29, -3)
            .icon(GTOTagPrefix.MILLED, GTMaterials.Pyrope)
            .subtitle("c:milleds")
            .desc(d -> d
                    .line("艾萨线的东西，LuV才会用到")
                    .line()
                    .line("特定矿物可以进行湿法碾磨"))
            .task(Task.checkmark().title("碾磨"), Task.item(GTOTagPrefix.MILLED, GTMaterials.Pyrope).optional())
            .dependsOn(Q_41AF3C6D));

    public static final Quest PYROPE_FRONT_BUCKET = CHAPTER.quest("pyrope_front_bucket", q -> q
            .at(-21, -3)
            .icon(GTOMaterials.PyropeFront.getBucket())
            .subtitle("gtocore:*_front")
            .desc(d -> d.line("碾磨矿石 -> 浮游选矿"))
            .task(Task.checkmark().title("矿物泡沫"), Task.item(GTOMaterials.PyropeFront.getBucket()).optional())
            .dependsOn(MILLED_PYROPE));

    public static final Quest PURIFIED_IRON_ORE = CHAPTER.quest("purified_iron_ore", q -> q
            .at(-25, -12)
            .icon(TagPrefix.crushedPurified, GTMaterials.Iron)
            .subtitle("c:purified_ores")
            .desc(d -> d.line("粉碎矿石 -> 洗矿(快/慢)/浸洗(慢)"))
            .task(Task.checkmark().title("纯净矿石"), Task.item(TagPrefix.crushedPurified, GTMaterials.Iron).optional())
            .dependsOn(CRUSHED_IRON_ORE));

    public static final Quest REFINED_IRON_ORE = CHAPTER.quest("refined_iron_ore", q -> q
            .at(-21, -9)
            .icon(TagPrefix.crushedRefined, GTMaterials.Iron)
            .subtitle("c:refined_ores")
            .desc(d -> d.line("纯净矿石/粉碎矿石 -> 热离(慢)"))
            .task(Task.checkmark().title("精炼矿石"), Task.item(TagPrefix.crushedRefined, GTMaterials.Iron).optional())
            .dependsOn(CRUSHED_IRON_ORE, PURIFIED_IRON_ORE)
            .requireAny());

    public static final Quest CLEAN_POWDER = CHAPTER.quest("clean_powder", q -> q
            .at(-21, -12)
            .icon(TagPrefix.dustPure, GTMaterials.Iron)
            .title("洁净粉")
            .subtitle("c:pure_dusts")
            .desc(d -> d.line("纯净矿石 -> 锻造(快)/研磨(慢)"))
            .task(Task.checkmark().title("纯净粉"), Task.item(TagPrefix.dustPure, GTMaterials.Iron).optional())
            .dependsOn(PURIFIED_IRON_ORE));

    public static final Quest IRON_DUST = CHAPTER.quest("iron_dust", q -> q
            .at(-17, -9)
            .icon(TagPrefix.dust, GTMaterials.Iron)
            .subtitle("c:dusts")
            .desc(d -> d
                    .line("含杂粉/洁净粉 -> 离心(中)/洗矿(快)")
                    .line("精炼矿石 -> 锻造(快)/研磨(慢)")
                    .line()
                    .line("部分洁净粉 -> 磁选(慢)")
                    .line()
                    .line("矿物泡沫 -> 真空干燥"))
            .task(Task.checkmark().title("粉"), Task.item(TagPrefix.dust, GTMaterials.Iron).optional())
            .dependsOn(IMPURE_IRON_DUST, REFINED_IRON_ORE, CLEAN_POWDER, PYROPE_FRONT_BUCKET)
            .requireAny());

    public static final Quest FLAWLESS_EMERALD_GEM = CHAPTER.quest("flawless_emerald_gem", q -> q
            .at(-21, -15)
            .icon(TagPrefix.gemFlawless, GTMaterials.Emerald)
            .subtitle("c:flawless_gems / c:exquisite_gems")
            .desc(d -> d.line("纯净矿石 -> 筛选(慢)"))
            .task(Task.checkmark().title("宝石"), Task.item(TagPrefix.gemFlawless, GTMaterials.Emerald).optional())
            .dependsOn(PURIFIED_IRON_ORE));
}
