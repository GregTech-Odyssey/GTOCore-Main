package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockD;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class GeneratorChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("generator", c -> c
            .title("发电线路")
            .subtitle("Power Generation Guide")
            .icon(GTItems.ULTIMATE_BATTERY));

    public static final Quest ULV_THERMAL_GENERATOR = CHAPTER.quest("ulv_thermal_generator", q -> q
            .at(-13, -12.5)
            .desc(d -> d.line("很经典的斯特林发电机，直接烧物品进行发电."))
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.ULV])));

    public static final Quest LV_THERMAL_GENERATOR = CHAPTER.quest("lv_thermal_generator", q -> q
            .at(-11.5, -12.5)
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.LV]))
            .dependsOn(ULV_THERMAL_GENERATOR));

    public static final Quest MV_THERMAL_GENERATOR = CHAPTER.quest("mv_thermal_generator", q -> q
            .at(-10, -12.5)
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.MV]))
            .dependsOn(LV_THERMAL_GENERATOR));

    public static final Quest ULV_SEMI_FLUID = CHAPTER.quest("ulv_semi_fluid", q -> q
            .at(-13, -6.5)
            .desc(d -> d.line("该系列发电的燃料主要是杂酚油和重燃油.由于半流质发电燃料的燃值普遍不高，MV-HV之后需要转内燃机或者燃气轮机."))
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.ULV])));

    public static final Quest LV_SEMI_FLUID = CHAPTER.quest("lv_semi_fluid", q -> q
            .at(-11.5, -6.5)
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.LV]))
            .dependsOn(ULV_SEMI_FLUID));

    public static final Quest MV_SEMI_FLUID = CHAPTER.quest("mv_semi_fluid", q -> q
            .at(-10, -6.5)
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.MV]))
            .dependsOn(LV_SEMI_FLUID));

    public static final Quest HV_SEMI_FLUID = CHAPTER.quest("hv_semi_fluid", q -> q
            .at(-8.5, -6.5)
            .task(Task.item(GTOMachines.SEMI_FLUID_GENERATOR[GTValues.HV]))
            .dependsOn(MV_SEMI_FLUID));

    public static final Quest LARGE_SEMI_FLUID_GENERATOR = CHAPTER.quest("large_semi_fluid_generator", q -> q
            .at(-7, -6.5)
            .task(Task.item(GeneratorMultiblock.LARGE_SEMI_FLUID_GENERATOR))
            .dependsOn(HV_SEMI_FLUID));

    public static final Quest ULV_WIND_MILL_TURBINE = CHAPTER.quest("ulv_wind_mill_turbine", q -> q
            .at(-13, -11)
            .icon(GTOMachines.WIND_MILL_TURBINE[GTValues.ULV])
            .desc(d -> d
                    .line("风力发电机中，风力取决于发电机自身的Y轴高度，在雨天会进行1.5倍风力加成，暴雨则是2倍风力加成.")
                    .line("{注意，发电机会因为风力过大导致过载爆炸.具体表现为:将原始风力发电机放在511高度下会立马爆炸.}", RED))
            .task(
                    Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.ULV]),
                    Task.item(GTOItems.WOOD_ROTOR),
                    Task.item(GTOItems.CARBON_ROTOR),
                    Task.item(GTOItems.STEEL_ROTOR),
                    Task.item(GTOItems.IRON_ROTOR)));

    public static final Quest LV_WIND_MILL_TURBINE = CHAPTER.quest("lv_wind_mill_turbine", q -> q
            .at(-11.5, -11)
            .task(Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.LV]))
            .dependsOn(ULV_WIND_MILL_TURBINE));

    public static final Quest MV_WIND_MILL_TURBINE = CHAPTER.quest("mv_wind_mill_turbine", q -> q
            .at(-10, -11)
            .task(Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.MV]))
            .dependsOn(LV_WIND_MILL_TURBINE));

    public static final Quest HV_WIND_MILL_TURBINE = CHAPTER.quest("hv_wind_mill_turbine", q -> q
            .at(-8.5, -11)
            .task(Task.item(GTOMachines.WIND_MILL_TURBINE[GTValues.HV]))
            .dependsOn(MV_WIND_MILL_TURBINE));

    public static final Quest LV_COMBUSTION = CHAPTER.quest("lv_combustion", q -> q
            .at(-13, -3.5)
            .desc(d -> d
                    .line()
                    .line("使用石化处理的产物发电.")
                    .line("由于GTM的交互比GT5的方便，只需要小机器就能完成高十六产线的搭建.")
                    .line("只烧高十六，可以支撑你到IV中期,再往后，你就需要火箭燃料发电."))
            .task(Task.item(GTMachines.COMBUSTION[GTValues.LV])));

    public static final Quest MV_COMBUSTION = CHAPTER.quest("mv_combustion", q -> q
            .at(-11.5, -3.5)
            .task(Task.item(GTMachines.COMBUSTION[GTValues.MV]))
            .dependsOn(LV_COMBUSTION));

    public static final Quest HV_COMBUSTION = CHAPTER.quest("hv_combustion", q -> q
            .at(-10, -3.5)
            .task(Task.item(GTMachines.COMBUSTION[GTValues.HV]))
            .dependsOn(MV_COMBUSTION));

    public static final Quest LARGE_COMBUSTION_ENGINE = CHAPTER.quest("large_combustion_engine", q -> q
            .at(-8.5, -3.5)
            .task(Task.item(GTMultiMachines.LARGE_COMBUSTION_ENGINE))
            .dependsOn(HV_COMBUSTION));

    public static final Quest EXTREME_COMBUSTION_ENGINE = CHAPTER.quest("extreme_combustion_engine", q -> q
            .at(-7, -3.5)
            .task(Task.item(GTMultiMachines.EXTREME_COMBUSTION_ENGINE))
            .dependsOn(LARGE_COMBUSTION_ENGINE));

    public static final Quest ENERGETIC_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("energetic_photovoltaic_power_station", q -> q
            .at(-13, -9.5)
            .desc(d -> d
                    .line("光伏发电的发电量取决于机器所在维度.")
                    .line("在空间站可以无视天气，昼夜循环进行发电，并且达到最高发电.")
                    .line("{普通模式下极限发电为1aHv}", RED))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_ENERGETIC)));

    public static final Quest PULSATING_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("pulsating_photovoltaic_power_station", q -> q
            .at(-11.5, -9.5)
            .desc(d -> d.line("{普通模式下极限发电为1aEv}", RED))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_PULSATING))
            .dependsOn(ENERGETIC_PHOTOVOLTAIC_POWER_STATION));

    public static final Quest VIBRANT_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("vibrant_photovoltaic_power_station", q -> q
            .at(-10, -9.5)
            .desc(d -> d.line("{普通模式下极限发电为1aIv}", RED))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_VIBRANT))
            .dependsOn(PULSATING_PHOTOVOLTAIC_POWER_STATION));

    public static final Quest LV_GAS_TURBINE = CHAPTER.quest("lv_gas_turbine", q -> q
            .at(-13, -5)
            .desc(d -> d
                    .line("该系列发电燃料的燃料主力是苯.")
                    .line("由于燃值低，发电极限最多撑到EV后期，往后就吃力许多了."))
            .task(Task.item(GTMachines.GAS_TURBINE[GTValues.LV])));

    public static final Quest MV_GAS_TURBINE = CHAPTER.quest("mv_gas_turbine", q -> q
            .at(-11.5, -5)
            .task(Task.item(GTMachines.GAS_TURBINE[GTValues.MV]))
            .dependsOn(LV_GAS_TURBINE));

    public static final Quest HV_GAS_TURBINE = CHAPTER.quest("hv_gas_turbine", q -> q
            .at(-10, -5)
            .task(Task.item(GTMachines.GAS_TURBINE[GTValues.HV]))
            .dependsOn(MV_GAS_TURBINE));

    public static final Quest GAS_LARGE_TURBINE = CHAPTER.quest("gas_large_turbine", q -> q
            .at(-8.5, -5)
            .task(Task.item(GTMultiMachines.LARGE_GAS_TURBINE))
            .dependsOn(HV_GAS_TURBINE));

    public static final Quest GAS_MEGA_TURBINE = CHAPTER.quest("gas_mega_turbine", q -> q
            .at(-7, -5)
            .task(Task.item(GeneratorMultiblock.GAS_MEGA_TURBINE))
            .dependsOn(GAS_LARGE_TURBINE));

    public static final Quest EV_ROCKET_ENGINE = CHAPTER.quest("ev_rocket_engine", q -> q
            .at(-13, -2)
            .desc(d -> d
                    .line("火箭燃料的推荐在化学篇章有提到.")
                    .line("该系列属于内燃机系列的上位替代，在转子的产能加持下也能在ZPM后期搏一搏.")
                    .line("但对于LUV之后解锁的新发电机而言，有点弱."))
            .task(Task.item(GTOMachines.ROCKET_ENGINE_GENERATOR[GTValues.EV])));

    public static final Quest IV_ROCKET_ENGINE = CHAPTER.quest("iv_rocket_engine", q -> q
            .at(-11.5, -2)
            .task(Task.item(GTOMachines.ROCKET_ENGINE_GENERATOR[GTValues.IV]))
            .dependsOn(EV_ROCKET_ENGINE));

    public static final Quest LUV_ROCKET_ENGINE = CHAPTER.quest("luv_rocket_engine", q -> q
            .at(-10, -2)
            .task(Task.item(GTOMachines.ROCKET_ENGINE_GENERATOR[GTValues.LuV]))
            .dependsOn(IV_ROCKET_ENGINE));

    public static final Quest ROCKET_LARGE_TURBINE = CHAPTER.quest("rocket_large_turbine", q -> q
            .at(-8.5, -2)
            .task(Task.item(GeneratorMultiblock.ROCKET_LARGE_TURBINE))
            .dependsOn(EV_ROCKET_ENGINE));

    public static final Quest ROCKET_MEGA_TURBINE = CHAPTER.quest("rocket_mega_turbine", q -> q
            .at(-7, -2)
            .task(Task.item(GeneratorMultiblock.ROCKET_MEGA_TURBINE))
            .dependsOn(ROCKET_LARGE_TURBINE));

    public static final Quest CHEMICAL_ENERGY_DEVOURER = CHAPTER.quest("chemical_energy_devourer", q -> q
            .at(-5, -3.5)
            .desc(d -> d
                    .line("做好充足准备和产线去维持该机器发电.")
                    .line("尽量采取热值更高，更方便生产的燃料，比如，火箭燃料.")
                    .line()
                    .line("在普通模式下，使用火箭燃料偏二甲肼-四氧化二氮 联合促燃后的发电为1aUHV.")
                    .line("相较于其他同时期发电设施，这个是更偏向于耐烧."))
            .task(Task.item(GeneratorMultiblock.CHEMICAL_ENERGY_DEVOURER))
            .dependsOn(ROCKET_MEGA_TURBINE, GAS_MEGA_TURBINE, EXTREME_COMBUSTION_ENGINE)
            .requireAnyStarted());

    public static final Quest IV_NAQUADAH_REACTOR = CHAPTER.quest("iv_naquadah_reactor", q -> q
            .at(-13, -0.5)
            .desc(d -> d
                    .line("硅岩发电，小机器发电更多是为了提供一些特殊情况的时候,比如超净间.去外太空放矿机，钻机.")
                    .line("但由于无线能源的普及，显得格外弱鸡了."))
            .task(Task.item(GTOMachines.NAQUADAH_REACTOR_GENERATOR[GTValues.IV])));

    public static final Quest LUV_NAQUADAH_REACTOR = CHAPTER.quest("luv_naquadah_reactor", q -> q
            .at(-11.5, -0.5)
            .task(Task.item(GTOMachines.NAQUADAH_REACTOR_GENERATOR[GTValues.LuV]))
            .dependsOn(IV_NAQUADAH_REACTOR));

    public static final Quest ZPM_NAQUADAH_REACTOR = CHAPTER.quest("zpm_naquadah_reactor", q -> q
            .at(-10, -0.5)
            .task(Task.item(GTOMachines.NAQUADAH_REACTOR_GENERATOR[GTValues.ZPM]))
            .dependsOn(LUV_NAQUADAH_REACTOR)
            .hideDependentLines());

    public static final Quest LARGE_NAQUADAH_REACTOR = CHAPTER.quest("large_naquadah_reactor", q -> q
            .at(-2.25, -5.25)
            .desc(d -> d.line("普通模式下，至少发电为1A UIV，最多为1A OpV"))
            .task(Task.item(GeneratorMultiblock.LARGE_NAQUADAH_REACTOR))
            .dependsOn(ZPM_NAQUADAH_REACTOR));

    public static final Quest HYPER_REACTOR = CHAPTER.quest("hyper_reactor", q -> q
            .at(-2.25, -3)
            .desc(d -> d
                    .line("使用超能燃料进行发电，提供额外的等离子体可以进行16并行发电.")
                    .line("超能燃料1使用{山铜等离子体}进行并行发电.", RED)
                    .line("超能燃料2使用{末影素等离子体}进行并行发电.", RED)
                    .line("超能燃料3使用{魔金等离子体}进行并行发电.", RED)
                    .line("超能燃料4使用{亚稳态𬭶等离子体}进行并行发电.", RED)
                    .line("普通模式下，使用超能燃料1并行之后的产能为1aMAX"))
            .task(Task.item(GeneratorMultiblock.HYPER_REACTOR))
            .dependsOn(LARGE_NAQUADAH_REACTOR));

    public static final Quest ADVANCED_HYPER_REACTOR = CHAPTER.quest("advanced_hyper_reactor", q -> q
            .at(-2.25, -0.75)
            .desc(d -> d.line("不并行情况下，浓缩燃料1是1aMAX+5，浓缩燃料2是1aMAX+6."))
            .task(Task.item(GeneratorMultiblock.ADVANCED_HYPER_REACTOR))
            .dependsOn(HYPER_REACTOR));

    public static final Quest LV_STEAM_TURBINE = CHAPTER.quest("lv_steam_turbine", q -> q
            .at(-13, -8)
            .desc(d -> d.line("蒸汽产出靠锅炉.可以压缩成高压蒸汽进行发电."))
            .task(Task.item(GTMachines.STEAM_TURBINE[GTValues.LV])));

    public static final Quest MV_STEAM_TURBINE = CHAPTER.quest("mv_steam_turbine", q -> q
            .at(-11.5, -8)
            .task(Task.item(GTMachines.STEAM_TURBINE[GTValues.MV]))
            .dependsOn(LV_STEAM_TURBINE));

    public static final Quest HV_STEAM_TURBINE = CHAPTER.quest("hv_steam_turbine", q -> q
            .at(-10, -8)
            .task(Task.item(GTMachines.STEAM_TURBINE[GTValues.HV]))
            .dependsOn(MV_STEAM_TURBINE));

    public static final Quest STEAM_LARGE_TURBINE = CHAPTER.quest("steam_large_turbine", q -> q
            .at(-8.5, -8)
            .task(Task.item(GTMultiMachines.LARGE_STEAM_TURBINE))
            .dependsOn(HV_STEAM_TURBINE));

    public static final Quest STEAM_MEGA_TURBINE = CHAPTER.quest("steam_mega_turbine", q -> q
            .at(-7, -8)
            .task(Task.item(GeneratorMultiblock.STEAM_MEGA_TURBINE))
            .dependsOn(STEAM_LARGE_TURBINE));

    public static final Quest GENERATOR_ARRAY = CHAPTER.quest("generator_array", q -> q
            .at(-15, -4)
            .subtitle("大哥阵列幼年版")
            .desc(d -> d
                    .line("塞入小发电机进行并行发电，还有额外发电倍率。")
                    .line("无线电网启用有5%损耗")
                    .line("{最多可塞入4个小发电机}", RED))
            .task(Task.item(GeneratorMultiblock.GENERATOR_ARRAY))
            .dependsOn(LV_COMBUSTION, LV_STEAM_TURBINE, ULV_SEMI_FLUID, LV_GAS_TURBINE, EV_ROCKET_ENGINE, IV_NAQUADAH_REACTOR)
            .requireAny());

    public static final Quest FISSION_REACTOR = CHAPTER.quest("fission_reactor", q -> q
            .at(-3.5, -11)
            .desc(d -> d.line("配合热交换进行发电."))
            .task(Task.item(MultiBlockD.FISSION_REACTOR))
            .dependsOn(IvChapter.POLYVINYL_BUTYRAL_BUCKET));

    public static final Quest DYSON_SPHERE_LAUNCH_SILO = CHAPTER.quest("dyson_sphere_launch_silo", q -> q
            .at(1, -7)
            .desc(d -> d.line("用于发射，发电部分在接收器."))
            .task(Task.item(GeneratorMultiblock.DYSON_SPHERE_LAUNCH_SILO)));

    public static final Quest DYSON_SPHERE_RECEIVING_STATION = CHAPTER.quest("dyson_sphere_receiving_station", q -> q
            .at(1, -4)
            .desc(d -> d.line("每次发射功率增加1aMAX"))
            .task(Task.item(GeneratorMultiblock.DYSON_SPHERE_RECEIVING_STATION))
            .dependsOn(DYSON_SPHERE_LAUNCH_SILO));

    public static final Quest ANNIHILATE_GENERATOR = CHAPTER.quest("annihilate_generator", q -> q
            .at(1.5, -1.5)
            .desc(d -> d
                    .line("最强发电.")
                    .line()
                    .line()
                    .line("{高能机械方块除了在主机旁边，斜边也有一些.}", DARK_GRAY))
            .task(Task.item(GeneratorMultiblock.ANNIHILATE_GENERATOR)));

    public static final Quest PLASMA_SUPERCRITICAL_GENERATION = CHAPTER.quest("plasma_supercritical_generation", q -> q
            .at(-5, -9.5)
            .icon(GTMultiMachines.FUSION_REACTOR[GTValues.ZPM])
            .title("等离子体，超临界发电...")
            .desc(d -> d
                    .line("在获得聚变mk2之后，你将可以使用等离子体发电.")
                    .line("超临界蒸汽发电仅次于硅岩反应堆的发电量.")
                    .line("磁流体发电的发电难度在于如何维持住等离子体的产出."))
            .task(Task.checkmark())
            .dependsOn(ZpmChapter.ZPM_FUSION_REACTOR));

    public static final Quest GENERATE_POWER_DIRECTLY_USING_PLASMA_GOODBYE = CHAPTER.quest("generate_power_directly_using_plasma_goodbye", q -> q
            .at(-3.5, -8)
            .title("直接使用等离子发电,告别中间商赚差价.")
            .desc(d -> d
                    .line()
                    .line("极限发电取决于激光仓的电压等级和输入的燃料燃值.")
                    .line()
                    .line("普通动力仓是启用4并行消耗燃料,IV激光仓是128并行，LUV激光仓是256并行，以此类推.")
                    .line()
                    .line("{普通模式下的参考:氧等离子,动力仓发电为1aLUV电，iv激光仓发电为0.5aUHV左右.}", RED)
                    .line("{银等离子,iv激光仓发电为0.5aUEV}", RED))
            .task(Task.item(GeneratorMultiblock.MAGNETIC_FLUID_GENERATOR))
            .dependsOn(PLASMA_SUPERCRITICAL_GENERATION));

    public static final Quest HEAT_EXCHANGER = CHAPTER.quest("heat_exchanger", q -> q
            .at(-3.5, -9.5)
            .task(Task.item(MultiBlockD.HEAT_EXCHANGER))
            .dependsOn(FISSION_REACTOR, PLASMA_SUPERCRITICAL_GENERATION)
            .requireAnyStarted());

    public static final Quest SUPERCRITICAL_STEAM_TURBINE = CHAPTER.quest("supercritical_steam_turbine", q -> q
            .at(-2, -9.5)
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_STEAM_TURBINE))
            .dependsOn(HEAT_EXCHANGER));

    public static final Quest SUPERCRITICAL_MEGA_STEAM_TURBINE = CHAPTER.quest("supercritical_mega_steam_turbine", q -> q
            .at(-0.5, -9.5)
            .desc(d -> d.line("最强的涡轮"))
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_MEGA_STEAM_TURBINE))
            .dependsOn(HEAT_EXCHANGER));

    public static final Quest NOTE = CHAPTER.quest("note", q -> q
            .at(-15.5, -8)
            .icon(Items.OAK_SIGN)
            .title("注意")
            .desc(d -> d
                    .line("注意，发电量取决于你选的难度，专家难度下的发电是比普通难度低的.")
                    .line()
                    .line("本章节的发电数据均来自普通模式下."))
            .task(Task.checkmark().title("请确认")));

    public static final Quest IV_LIGHTNING_ROD = CHAPTER.quest("iv_lightning_rod", q -> q
            .at(-5, -12.5)
            .subtitle("从此主世界再无晴天")
            .desc(d -> d
                    .line()
                    .line("插上一根避雷针，在雷雨天气下即可工作.")
                    .line("并不是类似于GT5的避雷针一样的工作，而是偶尔雷劈一下.发电并不稳定，不过可以通过堆数量改变.")
                    .line()
                    .line("单次雷劈能产生6aUHV.")
                    .line()
                    .line("三叉戟引雷可以发电."))
            .task(Task.item(GTOMachines.LIGHTNING_ROD[GTValues.IV])));

    public static final Quest LUV_LIGHTNING_ROD = CHAPTER.quest("luv_lightning_rod", q -> q
            .at(-3.5, -12.5)
            .desc(d -> d.line("单次雷劈能产生6aUEV"))
            .task(Task.item(GTOMachines.LIGHTNING_ROD[GTValues.LuV]))
            .dependsOn(IV_LIGHTNING_ROD));

    public static final Quest FUEL_CELL_GENERATOR = CHAPTER.quest("fuel_cell_generator", q -> q
            .at(-5, -1)
            .desc(d -> d.line("新时代发电机，榨干燃料中的所有能量"))
            .task(Task.item(GeneratorMultiblock.FUEL_CELL_GENERATOR))
            .dependsOn(CHEMICAL_ENERGY_DEVOURER));
}
