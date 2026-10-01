package com.gtocore.data;

import com.gtolib.api.annotation.DataGeneratorScanned;

import com.gregtechceu.gtceu.uiwidgets.icon.IdleReasonInfo.Severity;

import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public final class IdleReason extends com.gtolib.api.recipe.IdleReason {

    public static final IdleReason ORDERED_ITEM = new IdleReason("gtocore.idle_reason.ordered.item", "未满足物品有序要求", "Item Ordered Not Satisfies", "gtocore.idle_reason.ordered.item.desc", "物品须按配方要求的顺序放入。", "The items must be placed in the order required by the recipe.", Severity.BLOCKING);

    public static final IdleReason ORDERED_FLUID = new IdleReason("gtocore.idle_reason.ordered.fluid", "未满足流体有序要求", "Fluid Ordered Not Satisfies", "gtocore.idle_reason.ordered.fluid.desc", "流体须按配方要求的顺序放入。", "The fluids must be placed in the order required by the recipe.", Severity.BLOCKING);

    public static final IdleReason SET_CIRCUIT = new IdleReason("gtocore.idle_reason.set_circuit", "需要设置电路", "Need to set circuit", "gtocore.issue.set_circuit.desc", "需要为本机器设置编程电路。", "A programmed circuit must be set for this machine.", Severity.BLOCKING);

    public static final IdleReason GRIND_BALL = new IdleReason("gtocore.idle_reason.grindball", "需要研磨球", "Need to grind ball", "gtocore.issue.grindball.desc", "需要安装研磨球。", "A grinding ball must be installed.", Severity.BLOCKING);

    public static final IdleReason CHARGE = new IdleReason("gtocore.issue.charge", "工具电量不足", "Tool Charge Low", "gtocore.issue.charge.desc", "需要工具电量 %s EU，剩余 %s EU。", "Requires %s EU of tool charge, remaining %s EU.", Severity.BLOCKING);

    public static final IdleReason FELLING_TOOL = new IdleReason("gtocore.idle_reason.felling_tool", "需要伐木工具", "Need to felling tool", "gtocore.issue.felling_tool.desc", "需要放入斧头等伐木工具。", "An axe or other felling tool must be installed.", Severity.BLOCKING);

    public static final IdleReason RADIATION = new IdleReason("gtocore.idle_reason.radiation", "未处在要求辐射范围内", "Not in required radiation range", "gtocore.issue.radiation.desc", "需要辐射度 %s ± 5，当前 %s。", "Requires radioactivity %s ± 5, current %s.", Severity.BLOCKING);

    public static final IdleReason NO_ORES = new IdleReason("gtocore.idle_reason.no_ores", "该维度中没有可用的矿石", "No ores available in this dimension", null, null, null, Severity.BLOCKING);

    public static final IdleReason INCORRECT_DIRECTION_VOLTA = new IdleReason("gtocore.idle_reason.incorrect_direction_volta", "这个方向摆放的机器晒不到太阳", "The machine placed in this direction can't get sunlight", null, null, null, Severity.BLOCKING);
    public static final IdleReason OBSTRUCTED_VOLTA = new IdleReason("gtocore.idle_reason.obstructed_volta", "太阳能板被遮挡了", "The solar panel is obstructed", null, null, null, Severity.BLOCKING);
    public static final IdleReason SURFACE_ONLY_VOLTA = new IdleReason("gtocore.idle_reason.surface_only_volta", "光伏电站无法在星球轨道中工作", "The photovoltaic power station cannot operate in planetary orbit", null, null, null, Severity.BLOCKING);
    public static final IdleReason ORBIT_ONLY_VOLTA = new IdleReason("gtocore.idle_reason.orbit_only_volta", "光伏帆板仅能在星球轨道中工作", "The photovoltaic sail only operates in planetary orbit", null, null, null, Severity.BLOCKING);

    public static final IdleReason SIMULATION_TIER = new IdleReason("gtocore.idle_reason.simulation_tier", "模拟等级低于配方等级", "The simulation tier is below the recipe tier", "gtocore.issue.simulation_tier.desc", "需要模拟等级 %s，当前 %s。", "Requires simulation tier %s, current %s.", Severity.BLOCKING);

    public static final IdleReason MUFFLER_NOT_SUPPORTED = new IdleReason("gtocore.idle_reason.muffler_not_supported", "机器电压等级不支持高级消声仓", "The machine voltage tier does not support advanced muffler", null, null, null, Severity.BLOCKING);

    public static final IdleReason MANA_CONDENSER_FORM = new IdleReason("gtocore.idle_reason.mana_condenser_form", "阵列形态与所在维度不符", "The array form does not match the current dimension", null, null, null, Severity.BLOCKING);

    public static final IdleReason ICE_SHORT = new IdleReason("gtocore.issue.ice_short", "冰不足", "Ice Short", "gtocore.issue.ice_short.desc", "需要 %s mB 冰，当前 %s mB。", "Requires %s mB of ice, available %s mB.", Severity.BLOCKING);
    public static final IdleReason BLAZE_SHORT = new IdleReason("gtocore.issue.blaze_short", "烈焰不足", "Blaze Short", "gtocore.issue.blaze_short.desc", "需要 %s mB 烈焰，当前 %s mB。", "Requires %s mB of blaze, available %s mB.", Severity.BLOCKING);
    public static final IdleReason WATER_SHORT = new IdleReason("gtocore.issue.water_short", "水不足", "Water Short", "gtocore.issue.water_short.desc", "需要 %s mB 水，当前 %s mB。", "Requires %s mB of water, available %s mB.", Severity.BLOCKING);
    public static final IdleReason DISTILLED_WATER_SHORT = new IdleReason("gtocore.issue.distilled_water_short", "蒸馏水不足", "Distilled Water Short", "gtocore.issue.distilled_water_short.desc", "需要 %s mB 蒸馏水，当前 %s mB。", "Requires %s mB of distilled water, available %s mB.", Severity.BLOCKING);
    public static final IdleReason HYDROGEN_RESERVE_SHORT = new IdleReason("gtocore.issue.hydrogen_reserve_short", "氢储量不足", "Hydrogen Reserve Low", "gtocore.issue.hydrogen_reserve_short.desc", "需要氢储量 %s mB，当前 %s mB。", "Requires a hydrogen reserve of %s mB, current %s mB.", Severity.BLOCKING);
    public static final IdleReason HELIUM_RESERVE_SHORT = new IdleReason("gtocore.issue.helium_reserve_short", "氦储量不足", "Helium Reserve Low", "gtocore.issue.helium_reserve_short.desc", "需要氦储量 %s mB，当前 %s mB。", "Requires a helium reserve of %s mB, current %s mB.", Severity.BLOCKING);
    public static final IdleReason CELESTIAL_SHORT = new IdleReason("gtocore.issue.celestial_short", "天体能量不足", "Celestial Energy Short", "gtocore.issue.celestial_short.desc", "需要 %s 点天体能量，当前储量 %s。", "Requires %s points of celestial energy, stored %s.", Severity.BLOCKING);
    public static final IdleReason DIMENSION_DATA_MISSING = new IdleReason("gtocore.issue.dimension_data_missing", "缺少维度数据", "No Dimension Data", "gtocore.issue.dimension_data_missing.desc", "机器槽中需要放入记录了维度的维度数据。", "Dimension data recording a dimension must be placed in the machine slot.", Severity.BLOCKING);
    public static final IdleReason MACHINE_STORAGE_EMPTY = new IdleReason("gtocore.issue.machine_storage_empty", "未放入机器", "No Machine Installed", "gtocore.issue.machine_storage_empty.desc", "需要在控制器的机器槽中放入机器。", "A machine must be placed in the controller machine slot.", Severity.BLOCKING);

    public static final IdleReason SPOOL = new IdleReason("gtocore.issue.spool", "线轴不符", "Spool Mismatch", "gtocore.issue.spool.desc", "线轴仓中需要放入配方所需等级的线轴。", "The spool hatch must hold the spool tier required by the recipe.", Severity.BLOCKING);
    public static final IdleReason DRILL_HEAD_MISSING = new IdleReason("gtocore.issue.drill_head_missing", "缺少钻头", "No Drill Head", "gtocore.issue.drill_head_missing.desc", "机器槽中需要放入钻头。", "A drill head must be placed in the machine slot.", Severity.BLOCKING);

    public static final IdleReason TISSUE_TIER = new IdleReason("gtocore.issue.tissue_tier", "运行控制方块等级不足", "Control Block Tier Too Low", "gtocore.issue.tissue_tier.desc", "需要等级 %s，当前 %s。", "Requires tier %s, current %s.", Severity.BLOCKING);
    public static final IdleReason MEDIUM_TIER = new IdleReason("gtocore.issue.medium_tier", "培养液等级不足", "Culture Medium Tier Too Low", "gtocore.issue.medium_tier.desc", "需要培养液等级 %s，当前 %s。", "Requires culture medium tier %s, current %s.", Severity.BLOCKING);
    public static final IdleReason PROCESSING_TIER_MISMATCH = new IdleReason("gtocore.issue.processing_tier_mismatch", "机器等级不匹配", "Machine Tier Mismatch", "gtocore.issue.processing_tier_mismatch.desc", "放入的机器为 %s，能源仓为 %s，两者等级需相同。", "The installed machine is %s and the Energy Hatch is %s; the tiers must match.", Severity.BLOCKING);
    public static final IdleReason POWER_MODULE_TIER = new IdleReason("gtocore.issue.power_module_tier", "动力模块等级不足", "Power Module Tier Too Low", "gtocore.issue.power_module_tier.desc", "需要动力模块等级 %s，当前 %s。", "Requires power module tier %s, current %s.", Severity.BLOCKING);
    public static final IdleReason CIRCUIT_ENGRAVING = new IdleReason("gtocore.issue.circuit_engraving", "电路刻印不足", "Engraving Incomplete", "gtocore.issue.circuit_engraving.desc", "需要刻印 %s 块电路，当前 %s 块。", "Requires %s engraved circuits, current %s.", Severity.BLOCKING);

    public static final IdleReason THERMAL_ZONE_TEMP_DIFFERENCE = new IdleReason("gtocore.issue.thermal_zone_temp_difference", "热区温差过大", "Thermal Zone Difference Too Large", "gtocore.issue.thermal_zone_temp_difference.desc", "需要温差不超过 %s，当前 %s。", "Requires a temperature difference of at most %s, current %s.", Severity.BLOCKING);
    public static final IdleReason PREREQUISITES_NOT_RESEARCHED = new IdleReason("gtocore.machine.data_center.data_access.prerequisites_not_researched", null, null, "gtocore.issue.prerequisites_not_researched.desc", "所选科技节点的前置节点尚未研究。", "The prerequisites of the selected tech node have not been researched.", Severity.BLOCKING);
    public static final IdleReason NO_OWNER = new IdleReason("gtocore.issue.no_owner", "无所有者", "No Owner", "gtocore.issue.no_owner.desc", "机器没有所有者，无法使用无线网络。", "The machine has no owner and cannot use the wireless network.", Severity.BLOCKING);

    public static final IdleReason NO_BEDROCK = new IdleReason("gtocore.issue.no_bedrock", "下方无基岩", "No Bedrock Below", "gtocore.issue.no_bedrock.desc", "控制器下方第九格需要是基岩。", "The ninth block below the controller must be bedrock.", Severity.BLOCKING);
    public static final IdleReason NO_BEDROCK_FLUIDS = new IdleReason("gtocore.issue.no_bedrock_fluids", "无可用流体", "No Fluids Available", "gtocore.issue.no_bedrock_fluids.desc", "所记录的维度中没有基岩流体。", "The recorded dimension has no bedrock fluids.", Severity.BLOCKING);
    public static final IdleReason DAYTIME_ONLY = new IdleReason("gtocore.issue.daytime_only", "仅限白天", "Daytime Only", "gtocore.issue.daytime_only.desc", "本机器只在白天工作。", "This machine works only during the day.", Severity.BLOCKING);
    public static final IdleReason SKY_OBSTRUCTED = new IdleReason("gtocore.issue.sky_obstructed", "天空被遮挡", "Sky Obstructed", "gtocore.issue.sky_obstructed.desc", "机器上方的接收区域须能直视天空。", "The receiving area above the machine must have a clear view of the sky.", Severity.BLOCKING);
    public static final IdleReason NETHER_ONLY = new IdleReason("gtocore.issue.nether_only", "仅限下界", "Nether Only", "gtocore.issue.nether_only.desc", "本机器只能在下界运行。", "This machine works only in the Nether.", Severity.BLOCKING);
    public static final IdleReason BIOME_UNSUITABLE = new IdleReason("gtocore.issue.biome_unsuitable", "生物群系不适用", "Unsuitable Biome", "gtocore.issue.biome_unsuitable.desc", "所在生物群系无法产出水。", "No water can be produced in the current biome.", Severity.BLOCKING);
    public static final IdleReason INSUFFICIENT_CLEANROOM = new IdleReason("gtocore.machine.space_bio_research_module.insufficient_cleanroom", null, null, null, null, null, Severity.BLOCKING);
    public static final IdleReason ONLY_IN_SPACE = new IdleReason("gtocore.issue.only_in_space", "仅限太空", "Space Only", "gtocore.issue.only_in_space.desc", "本机器只能在太空中运行。", "This machine works only in space.", Severity.BLOCKING);
    public static final IdleReason ONLY_ON_PLANET = new IdleReason("gtocore.issue.only_on_planet", "仅限行星", "Planet Only", "gtocore.issue.only_on_planet.desc", "本机器只能在行星上运行。", "This machine works only on a planet.", Severity.BLOCKING);
    public static final IdleReason DYSON_SPHERE_COMPLETE = new IdleReason("gtocore.issue.dyson_sphere_complete", "戴森球已建成", "Dyson Sphere Complete", "gtocore.issue.dyson_sphere_complete.desc", "所在维度的戴森球已有 %s 个模块，达到上限。", "The Dyson sphere of this dimension already has %s modules, the maximum.", Severity.INFO);
    public static final IdleReason DYSON_SPHERE_EMPTY = new IdleReason("gtocore.issue.dyson_sphere_empty", "无戴森球", "No Dyson Sphere", "gtocore.issue.dyson_sphere_empty.desc", "所在维度尚未发射戴森球模块。", "No Dyson sphere module has been launched in this dimension.", Severity.BLOCKING);
    public static final IdleReason DYSON_SPHERE_IN_USE = new IdleReason("gtocore.issue.dyson_sphere_in_use", "戴森球被占用", "Dyson Sphere In Use", "gtocore.issue.dyson_sphere_in_use.desc", "所在维度的另一台机器正在使用戴森球。", "Another machine in this dimension is using the Dyson sphere.", Severity.BLOCKING);

    public static final IdleReason NANITES_NOT_CONNECTED = new IdleReason("gtocore.issue.nanites_not_connected", "未连接主机", "Not Connected", "gtocore.issue.nanites_not_connected.desc", "本模块需要连接到纳米集成加工中心。", "The module must be connected to a Nanites Integrated Processing Center.", Severity.BLOCKING);
    public static final IdleReason NANITES_MODULE_MISSING = new IdleReason("gtocore.issue.nanites_module_missing", "缺少模块", "Module Missing", "gtocore.issue.nanites_module_missing.desc", "本配方需要连接%s。", "This recipe requires a connected %s.", Severity.BLOCKING);
    public static final IdleReason MAGIC_MODULE_MISSING = new IdleReason("gtocore.issue.magic_module_missing", "缺少魔法模块", "Magic Module Missing", "gtocore.issue.magic_module_missing.desc", "产出魔力的配方需要搭建魔法模块。", "Mana-producing recipes require the Magic Module to be built.", Severity.BLOCKING);
    public static final IdleReason PLASMA_WINGS_MISSING = new IdleReason("gtocore.issue.plasma_wings_missing", "缺少等离子冷凝翼", "Plasma Condensing Wings Missing", "gtocore.issue.plasma_wings_missing.desc", "等离子冷凝配方需要搭建等离子冷凝翼。", "Plasma condensing recipes require the Plasma Condensing Wings to be built.", Severity.BLOCKING);
    public static final IdleReason SPACE_ELEVATOR_NOT_CONNECTED = new IdleReason("gtocore.issue.space_elevator_not_connected", "未连接太空电梯", "Elevator Not Connected", "gtocore.issue.space_elevator_not_connected.desc", "本模块未连接到太空电梯。", "This module is not connected to a space elevator.", Severity.BLOCKING);
    public static final IdleReason SPACE_ELEVATOR_NOT_RUNNING = new IdleReason("gtocore.issue.space_elevator_not_running", "太空电梯未运行", "Elevator Not Running", "gtocore.issue.space_elevator_not_running.desc", "所连接的太空电梯未在运行，模块仅在其运行时工作。", "The connected space elevator is not running; modules work only while it runs.", Severity.BLOCKING);
    public static final IdleReason COIL_NOT_USABLE = new IdleReason("gtocore.machine.dimensionally_transcendent_plasma_forge.coil", null, null, null, null, null, Severity.BLOCKING);
    public static final IdleReason STEAM_VENT_OBSTRUCTED = new IdleReason("gtocore.issue.steam_vent_obstructed", "蒸汽排气口受阻", "Steam Vent Obstructed", "recipe.condition.steam_vent.tooltip", null, null, Severity.BLOCKING);
    public static final IdleReason ROTOR_MISMATCH = new IdleReason("gtocore.issue.rotor_mismatch", "转子材料不一致", "Rotor Materials Differ", "gtocore.issue.rotor_mismatch.desc", "所有转子需使用同一种材料。", "All rotors must be made of the same material.", Severity.BLOCKING);

    public static final IdleReason MANA_FLOW_TOO_WEAK = new IdleReason("gtocore.issue.mana_flow_too_weak", "魔力流不足", "Mana Flow Too Weak", "gtocore.issue.mana_flow_too_weak.desc", "需要 %s 魔力/t，最大速率 %s 魔力/t。", "Requires %s mana/t, maximum rate %s mana/t.", Severity.BLOCKING);
    public static final IdleReason DRONE_NO_ENERGY = new IdleReason("gtocore.machine.space_drone_dock.drone_no_energy", null, null, null, null, null, Severity.BLOCKING);
    public static final IdleReason HARMONY_GRID_SHORT = new IdleReason("gtocore.issue.harmony_grid_short", "无线电网储能不足", "Wireless Grid Short", "gtocore.issue.harmony_grid_short.desc", "启动耗能为 %s EU，无线电网储能须大于该值。", "The startup energy is %s EU; the wireless grid must hold more than this.", Severity.BLOCKING);
    public static final IdleReason HEAT_FULL = new IdleReason("gtocore.issue.heat_full", "热量已满", "Heat Full", "gtocore.issue.heat_full.desc", "储热为 %s / %s HU，热量被取用后继续加热。", "Stored heat is %s / %s HU; heating resumes once heat is drawn.", Severity.INFO);

    public IdleReason(String key, String cn, String en) {
        super(key, en, cn);
    }

    public IdleReason(String key, String cn, String en, @Nullable String descKey, @Nullable String descCn, @Nullable String descEn, Severity severity) {
        super(key, en, cn, descKey, descEn, descCn, severity);
    }

    public static void init() {}
}
