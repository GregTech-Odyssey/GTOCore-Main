package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.SpaceMultiblock;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class SpaceStationChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("space_station", c -> c
            .title("穹宇居所")
            .subtitle("Aerospace Industry")
            .icon(GTOItems.PLANET_SCAN_SATELLITE)
            .hideQuestsUntilDepsVisible()
            .flexible()
            .autofocus(SpaceStationChapter.SPACE_STATION_ENVIRONMENTAL_MAINTENANCE_MODULE)
            .background(QuestTextures.SPACE, 0x555555));

    public static final Quest WE_NEED_A_HOUSE_IN_SPACE = CHAPTER.quest("we_need_a_house_in_space", q -> q
            .at(5, -6)
            .icon(GTOItems.SPACE_PROBE_MK1)
            .title("我们需要一个在太空中的房子")
            .subtitle("地轨道长期驻留")
            .desc(d -> d
                    .line("漂浮在{太空}中，{几乎为零的重力环境}能改善一些加工条件，使得某些高级材料的制作成为可能", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("不但如此，{光伏电站}在{太空}能得到最高的发电量", YELLOW, GOLD)
                    .line()
                    .line("{但是}，将机器裸露在太空中是一个危险的决定，严苛的工作环境让绝大多数机器都无法正常的工作", BOLD)
                    .line()
                    .line("{能够承受住太空恶劣环境的机器均有机器提示标出}", GRAY)
                    .line()
                    .line("因此，你需要为机器建造一个{真正的房子}来让机器得以正常工作", GOLD)
                    .line()
                    .line("你现在可以去{穹宇居所}章节看看了", link(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE))
                    .line()
                    .line("{注意，地球空间站所需的材料相比其他空间站更便宜，你可以通过emi来查看具体需要的材料}", YELLOW)
                    .page()
                    .line("{你的第一个空间站（地球）所需材料列表}", LIGHT_PURPLE)
                    .line("{（可在EMI查看每个星球建造空间站所需材料清单）}", DARK_GRAY)
                    .line()
                    .line("空间站 * 1")
                    .line("洁净不锈钢外壳 *64")
                    .line("铝合金8090蒙皮机械方块 * 64")
                    .line("加强钛机械外壳 * 32")
                    .line("铝合金7050支撑机械方块 * 16")
                    .line("压力容器机械方块 * 16")
                    .line("过滤器机械方块 * 16")
                    .line("不锈钢-GC4框架 * 16")
                    .line("航天器密封机械方块 * 16"))
            .task(
                    Task.dimension(Planet.EARTH_ORBIT).title("造访轨道空间站").icon(GTOItems.SPACE_PROBE_MK1),
                    Task.item(SpaceMultiblock.SPACE_STATION))
            .reward(Reward.item(ModItems.EARTH_GLOBE.get()))
            .dependsOn(HvChapter.HEAD_TO_THE_MOON));

    public static final Quest SPACE_STATION_MECHANISM_EXPLAINED = CHAPTER.quest("space_station_mechanism_explained", q -> q
            .at(8.5, -3)
            .icon(Items.SPYGLASS)
            .title("空间站机制详解")
            .subtitle("太空版超净间")
            .desc(d -> d
                    .line("空间站在太空中运行时，可以：")
                    .line()
                    .line("- 为内部的机器提供{太空工作环境}", AQUA)
                    .line("- 根据安装的过滤器方块的种类，为内部的机器提供{超净环境}", AQUA)
                    .line("- 每秒试图向安装在外侧的{光伏电站专用输出仓}供给默认{8mB蒸馏水/每仓}（可配置）", YELLOW, AQUA)
                    .line("- 为机器内的玩家提供可呼吸的空气")
                    .line()
                    .line("但是可惜，机器的运行{并不是免费的}，内部环境的维持需要供给{1A EV/t的电}{持续消耗少量的资源}，同时会排出废料", DARK_RED, LIGHT_PURPLE, GOLD)
                    .line()
                    .line("每10秒，机器需要：")
                    .line("{15 mB 蒸馏水}", AQUA)
                    .line("{10 mB 火箭燃料} {（只能是T1火箭燃料）}", AQUA, YELLOW)
                    .line("{100mB 空气}", AQUA))
            .task(Task.checkmark())
            .dependsOn(WE_NEED_A_HOUSE_IN_SPACE));

    public static final Quest SPACE_STATION_CONSTRUCTION_TIPS = CHAPTER.quest("space_station_construction_tips", q -> q
            .at(2, -8.5)
            .icon(GTItems.GRAVITATION_ENGINE)
            .title("空间站建造提示")
            .desc(d -> d
                    .line("格雷公司为了照顾新入职的员工，给员工第一个地球空间站的建造提供了相当力度的补贴")
                    .line()
                    .line("每个太空轨道上的建造都会提供{一个成型的空间站}，{4个充能光伏电站}，以及{火箭燃料/蒸馏水/空气各80B}", YELLOW, YELLOW, AQUA)
                    .line()
                    .line("降落时一定要小心！不然{降落仓会直接砸坏轨道空间站}", RED)
                    .line()
                    .line("启动前向安装的光伏电站提供一些{蒸馏水}可以保证空间站电力的自给", AQUA)
                    .line()
                    .line("至于空间站里的机器的用电...自己带点{电池箱}或者使用{火箭引擎小机器}发电吧", YELLOW, YELLOW))
            .task(Task.checkmark())
            .dependsOn(WE_NEED_A_HOUSE_IN_SPACE));

    public static final Quest INDUSTRIAL_SPACE_STATION = CHAPTER.quest("industrial_space_station", q -> q
            .at(6.5, -6.5).iconScale(0.9)
            .title("工业空间站")
            .subtitle("太空超净间ProMax")
            .desc(d -> d
                    .line("{为了能在太空展开大规模的科研生产，}{公司}{研发了崭新的}{工业空间站}{。其内部空间是原来的数倍，同时带有}{拓展舱}{。然而，实际使用的员工却哀嚎连天——它需要制作许多为太空特制的新材料！}", ITALIC, DARK_AQUA.italic(), ITALIC, GOLD.italic(), ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{你需要在太空中另找一片位置，将这个空间站放下去}", YELLOW.italic())
                    .line()
                    .line("{内部的空间足以放置}{IV时代}的{工业大机器}", YELLOW.italic(), IV.italic(), YELLOW)
                    .line()
                    .line("同时每个空间站舱段还能{与其他舱段相连}来扩展整个空间站的空间与功能", GOLD)
                    .line()
                    .line("除此之外，利用太空提供的特殊环境，在这里你还能建造一些舱室，来{进行一些地面上不可能的材料处理}", GOLD)
                    .line()
                    .page()
                    .line("工业空间站的舱段分为三种：{核心舱}，{模块舱}，{衔接舱}", RED, YELLOW, GREEN)
                    .line()
                    .line("{核心舱}、{模块舱}只能和{衔接舱}相连", RED, YELLOW, GREEN)
                    .line()
                    .line("而{衔接舱}可以与{模块舱}，以及{衔接舱}同类相连", GREEN, YELLOW, GREEN))
            .task(Task.item(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE))
            .dependsOn(WE_NEED_A_HOUSE_IN_SPACE, IvChapter.Q_0BFFC724));

    public static final Quest SPACE_STATION_DOCKING_MODULE = CHAPTER.quest("space_station_docking_module", q -> q
            .at(10.5, -5.5)
            .task(Task.item(SpaceMultiblock.SPACE_STATION_DOCKING_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_STATION_TRANSPARENT_DOCKING_MODULE = CHAPTER.quest("space_station_transparent_docking_module", q -> q
            .at(12.5, -4)
            .task(Task.item(SpaceMultiblock.SPACE_STATION_TRANSPARENT_DOCKING_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_STATION_EXTENSION_MODULE = CHAPTER.quest("space_station_extension_module", q -> q
            .at(7, -9.5)
            .desc(d -> d.line("专门用于放置大机器的仓室，拥有可变长度"))
            .task(Task.item(SpaceMultiblock.SPACE_STATION_EXTENSION_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_STATION_ENVIRONMENTAL_MAINTENANCE_MODULE = CHAPTER.quest("space_station_environmental_maintenance_module", q -> q
            .at(9.5, -7.55)
            .desc(d -> d.line("用于给其他仓室提供环境，同时也可以维护和清理在空间站中的机器"))
            .task(Task.item(SpaceMultiblock.SPACE_STATION_ENVIRONMENTAL_MAINTENANCE_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest ORBITAL_SMELTING_FACILITY = CHAPTER.quest("orbital_smelting_facility", q -> q
            .at(10.5, -10)
            .task(Task.item(SpaceMultiblock.ORBITAL_SMELTING_FACILITY))
            .dependsOn(ZpmChapter.MEGA_ALLOY_BLAST_SMELTER, ZpmChapter.MEGA_BLAST_FURNACE, LuvChapter.TIER_6_ROCKET, INDUSTRIAL_SPACE_STATION));

    public static final Quest ORBITAL_FINE_MATERIALS_FACTORY = CHAPTER.quest("orbital_fine_materials_factory", q -> q
            .at(14.5, -7)
            .task(Task.item(SpaceMultiblock.ORBITAL_FINE_MATERIALS_FACTORY))
            .dependsOn(LuvChapter.TIER_6_ROCKET, INDUSTRIAL_SPACE_STATION));

    public static final Quest ORBITAL_NANOPRECISION_PROCESSING_CHAMBER = CHAPTER.quest("orbital_nanoprecision_processing_chamber", q -> q
            .at(13.5, -9).iconScale(0.9)
            .task(Task.item(SpaceMultiblock.ORBITAL_NANOPRECISION_PROCESSING_CHAMBER))
            .dependsOn(ORBITAL_FINE_MATERIALS_FACTORY, UvChapter.NANO_FORGE, ZpmChapter.TIER_7_ROCKET, ORBITAL_SMELTING_FACILITY));

    public static final Quest SPACE_DRONE_DOCK = CHAPTER.quest("space_drone_dock", q -> q
            .at(12, -8.45)
            .desc(d -> d.line("根据所在空间站星球的不同会产出不同的宇宙尘埃"))
            .task(Task.item(SpaceMultiblock.SPACE_DRONE_DOCK))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_STATION_ENERGY_CONVERSION_MODULE = CHAPTER.quest("space_station_energy_conversion_module", q -> q
            .at(15.9, -9.85).iconScale(0.9)
            .desc(d -> d
                    .line("{终于，你可以在空间站中使用激光仓了}", STRIKE)
                    .line()
                    .line("激光仓和高级仓室们需要 科技节点 激光太空工程 解锁，当然也会顺带解锁空间站高能转换调配仓"))
            .task(Task.item(SpaceMultiblock.SPACE_STATION_ENERGY_CONVERSION_MODULE))
            .dependsOn(ORBITAL_NANOPRECISION_PROCESSING_CHAMBER));

    public static final Quest SPACE_ELEVATOR_CONNECTOR_MODULE = CHAPTER.quest("space_elevator_connector_module", q -> q
            .at(9, -11).iconScale(0.9)
            .desc(d -> d.line("连接地面与太空"))
            .task(Task.item(SpaceMultiblock.SPACE_ELEVATOR_CONNECTOR_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_BIO_RESEARCH_MODULE = CHAPTER.quest("space_bio_research_module", q -> q
            .at(16.5, -5.5)
            .task(Task.item(SpaceMultiblock.SPACE_BIO_RESEARCH_MODULE))
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest SPACE_SHIELD_HATCH = CHAPTER.quest("space_shield_hatch", q -> q
            .at(14.816326530612223, -10.754081632653055)
            .desc(d -> d.line("让你的机器可以放在空间站之外"))
            .task(Task.item(GTOMachines.SPACE_SHIELD_HATCH))
            .dependsOn(UevChapter.WELCOME_TO_THE_EXTREME_OVERPRESSURE_STAGE, ORBITAL_NANOPRECISION_PROCESSING_CHAMBER));

    public static final Quest PLANETARY_GAS_COLLECTOR = CHAPTER.quest("planetary_gas_collector", q -> q
            .at(17, -9)
            .task(Task.item(SpaceMultiblock.PLANETARY_GAS_COLLECTOR))
            .dependsOn(ORBITAL_NANOPRECISION_PROCESSING_CHAMBER));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.STATION_1, 9, -6.5, 25.6, 14.4))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, 4.5, -3.5, 2, 2))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_UEV, 16.5, -10.2, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, 6, -7.25, 1, 1)));
    }
}
