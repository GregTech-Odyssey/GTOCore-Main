package com.gtocore.data;

import com.gtocore.common.machine.multiblock.electric.nano.NanitesIntegratedMachine;
import com.gtocore.common.machine.multiblock.noenergy.HarmonyMachine;
import com.gtocore.common.machine.multiblock.noenergy.NeutronActivatorMachine;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;

import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.machine.issue.IssueCategory;
import com.gregtechceu.gtceu.api.machine.issue.IssueSeverity;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueText;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

@DataGeneratorScanned
public final class IdleReason extends com.gtolib.api.recipe.IdleReason {

    public static final IdleReason ORDERED_ITEM = new IdleReason(builder("gtocore.idle_reason.ordered.item", null, null)
            .as(GTIssues.ORDERED_INPUT, IO.IN, ItemRecipeInfo.INSTANCE));
    public static final IdleReason ORDERED_FLUID = new IdleReason(builder("gtocore.idle_reason.ordered.fluid", null, null)
            .as(GTIssues.ORDERED_INPUT, IO.IN, FluidRecipeInfo.INSTANCE));
    public static final IdleReason SET_CIRCUIT = new IdleReason(builder("gtocore.idle_reason.set_circuit", "Need to set circuit", "需要设置电路")
            .id(GTOCore.id("set_circuit")).category(IssueCategory.INPUT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.set_circuit.desc", "A programmed circuit must be set for this machine.", "需要为本机器设置编程电路。"));
    public static final IdleReason ICE_SHORT = new IdleReason(builder("gtocore.issue.ice_short", "Ice Short", "冰不足")
            .id(GTOCore.id("ice_short")).category(IssueCategory.INPUT).stage(IssueStage.INPUT).args(NUMBERS)
            .desc("gtocore.issue.ice_short.desc", "Requires %s mB of ice, available %s mB.", "需要 %s mB 冰，当前 %s mB。"));
    public static final IdleReason BLAZE_SHORT = new IdleReason(builder("gtocore.issue.blaze_short", "Blaze Short", "烈焰不足")
            .id(GTOCore.id("blaze_short")).category(IssueCategory.INPUT).stage(IssueStage.INPUT).args(NUMBERS)
            .desc("gtocore.issue.blaze_short.desc", "Requires %s mB of blaze, available %s mB.", "需要 %s mB 烈焰，当前 %s mB。"));
    public static final IdleReason WATER_SHORT = new IdleReason(builder("gtocore.issue.water_short", "Water Short", "水不足")
            .id(GTOCore.id("water_short")).category(IssueCategory.INPUT).stage(IssueStage.INPUT).args(NUMBERS)
            .desc("gtocore.issue.water_short.desc", "Requires %s mB of water, available %s mB.", "需要 %s mB 水，当前 %s mB。"));
    public static final IdleReason DISTILLED_WATER_SHORT = new IdleReason(builder("gtocore.issue.distilled_water_short", "Distilled Water Short", "蒸馏水不足")
            .id(GTOCore.id("distilled_water_short")).category(IssueCategory.INPUT).stage(IssueStage.INPUT).args(NUMBERS)
            .desc("gtocore.issue.distilled_water_short.desc", "Requires %s mB of distilled water, available %s mB.", "需要 %s mB 蒸馏水，当前 %s mB。"));
    public static final IdleReason HYDROGEN_RESERVE_SHORT = new IdleReason(builder("gtocore.issue.hydrogen_reserve_short", "Hydrogen Reserve Low", "氢储量不足")
            .id(GTOCore.id("hydrogen_reserve_short")).category(IssueCategory.INPUT).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.hydrogen_reserve_short.desc", "Requires a hydrogen reserve of %s mB, current %s mB.", "需要氢储量 %s mB，当前 %s mB。"));
    public static final IdleReason HELIUM_RESERVE_SHORT = new IdleReason(builder("gtocore.issue.helium_reserve_short", "Helium Reserve Low", "氦储量不足")
            .id(GTOCore.id("helium_reserve_short")).category(IssueCategory.INPUT).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.helium_reserve_short.desc", "Requires a helium reserve of %s mB, current %s mB.", "需要氦储量 %s mB，当前 %s mB。"));
    public static final IdleReason CELESTIAL_SHORT = new IdleReason(builder("gtocore.issue.celestial_short", "Celestial Energy Short", "天体能量不足")
            .id(GTOCore.id("celestial_short")).category(IssueCategory.INPUT).stage(IssueStage.CONDITION).args(NUMBERS)
            .desc("gtocore.issue.celestial_short.desc", "Requires %s points of celestial energy, stored %s.", "需要 %s 点天体能量，当前储量 %s。"));
    public static final IdleReason DIMENSION_DATA_MISSING = new IdleReason(builder("gtocore.issue.dimension_data_missing", "No Dimension Data", "缺少维度数据")
            .id(GTOCore.id("dimension_data_missing")).category(IssueCategory.INPUT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.dimension_data_missing.desc", "Dimension data recording a dimension must be placed in the machine slot.", "机器槽中需要放入记录了维度的维度数据。"));
    public static final IdleReason MACHINE_STORAGE_EMPTY = new IdleReason(builder("gtocore.issue.machine_storage_empty", "No Machine Installed", "未放入机器")
            .id(GTOCore.id("machine_storage_empty")).category(IssueCategory.MACHINE).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.machine_storage_empty.desc", "A machine must be placed in the controller machine slot.", "需要在控制器的机器槽中放入机器。"));
    public static final IdleReason FRAMEWORK_TIER_LIMIT = new IdleReason(builder("gtocore.issue.framework_tier_limit", "Framework Tier Limit", "整体框架等级限制")
            .id(GTOCore.id("framework_tier_limit")).category(IssueCategory.TIER).severity(IssueSeverity.INFO).stage(IssueStage.TIER)
            .args(i -> new Object[] { IssueText.tierName(i.a()), IssueText.tierName(i.b()) })
            .desc("gtocore.issue.framework_tier_limit.desc", "The integral framework limits the machine tier to %s; the energy hatches support %s.", "整体框架将机器等级限制为 %s，能源仓可支持 %s。"));

    public static final IdleReason GRIND_BALL = new IdleReason(builder("gtocore.idle_reason.grindball", "Need to grind ball", "需要研磨球")
            .id(GTOCore.id("grindball")).category(IssueCategory.TOOL).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.grindball.desc", "A grinding ball must be installed.", "需要安装研磨球。"));
    public static final IdleReason CHARGE = new IdleReason(builder("gtocore.issue.charge", "Tool Charge Low", "工具电量不足")
            .id(GTOCore.id("charge")).category(IssueCategory.TOOL).stage(IssueStage.CONDITION).args(NUMBERS)
            .desc("gtocore.issue.charge.desc", "Requires %s EU of tool charge, remaining %s EU.", "需要工具电量 %s EU，剩余 %s EU。"));
    public static final IdleReason FELLING_TOOL = new IdleReason(builder("gtocore.idle_reason.felling_tool", "Need to felling tool", "需要伐木工具")
            .id(GTOCore.id("felling_tool")).category(IssueCategory.TOOL).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.felling_tool.desc", "An axe or other felling tool must be installed.", "需要放入斧头等伐木工具。"));
    public static final IdleReason SPOOL = new IdleReason(builder("gtocore.issue.spool", "Spool Mismatch", "线轴不符")
            .id(GTOCore.id("spool")).category(IssueCategory.TOOL).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.spool.desc", "The spool hatch must hold the spool tier required by the recipe.", "线轴仓中需要放入配方所需等级的线轴。"));
    public static final IdleReason DRILL_HEAD_MISSING = new IdleReason(builder("gtocore.issue.drill_head_missing", "No Drill Head", "缺少钻头")
            .id(GTOCore.id("drill_head_missing")).category(IssueCategory.TOOL).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.drill_head_missing.desc", "A drill head must be placed in the machine slot.", "机器槽中需要放入钻头。"));

    public static final IdleReason SIMULATION_TIER = new IdleReason(builder("gtocore.issue.simulation_tier", "Simulation Tier Too Low", "模拟等级不足")
            .id(GTOCore.id("simulation_tier")).category(IssueCategory.TIER).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.simulation_tier.desc", "Requires simulation tier %s, current %s.", "需要模拟等级 %s，当前 %s。"));
    public static final IdleReason TISSUE_TIER = new IdleReason(builder("gtocore.issue.tissue_tier", "Control Block Tier Too Low", "运行控制方块等级不足")
            .id(GTOCore.id("tissue_tier")).category(IssueCategory.TIER).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.tissue_tier.desc", "Requires tier %s, current %s.", "需要等级 %s，当前 %s。"));
    public static final IdleReason MEDIUM_TIER = new IdleReason(builder("gtocore.issue.medium_tier", "Culture Medium Tier Too Low", "培养液等级不足")
            .id(GTOCore.id("medium_tier")).category(IssueCategory.TIER).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.medium_tier.desc", "Requires culture medium tier %s, current %s.", "需要培养液等级 %s，当前 %s。"));
    public static final IdleReason PROCESSING_TIER_MISMATCH = new IdleReason(builder("gtocore.issue.processing_tier_mismatch", "Machine Tier Mismatch", "机器等级不匹配")
            .id(GTOCore.id("processing_tier_mismatch")).category(IssueCategory.TIER).stage(IssueStage.CONDITION)
            .args(i -> new Object[] { IssueText.tierName(i.a()), IssueText.tierName(i.b()) })
            .desc("gtocore.issue.processing_tier_mismatch.desc", "The installed machine is %s and the Energy Hatch is %s; the tiers must match.", "放入的机器为 %s，能源仓为 %s，两者等级需相同。"));
    public static final IdleReason POWER_MODULE_TIER = new IdleReason(builder("gtocore.issue.power_module_tier", "Power Module Tier Too Low", "动力模块等级不足")
            .id(GTOCore.id("power_module_tier")).category(IssueCategory.TIER).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.power_module_tier.desc", "Requires power module tier %s, current %s.", "需要动力模块等级 %s，当前 %s。"));
    public static final IdleReason CIRCUIT_ENGRAVING = new IdleReason(builder("gtocore.issue.circuit_engraving", "Engraving Incomplete", "电路刻印不足")
            .id(GTOCore.id("circuit_engraving")).category(IssueCategory.MACHINE).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.circuit_engraving.desc", "Requires %s engraved circuits, current %s.", "需要刻印 %s 块电路，当前 %s 块。"));

    public static final IdleReason RADIATION = new IdleReason(builder("gtocore.idle_reason.radiation", "Not in required radiation range", "未处在要求辐射范围内")
            .id(GTOCore.id("radiation")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.radiation.desc", "Requires radioactivity %s ± 5, current %s.", "需要辐射度 %s ± 5，当前 %s。"));
    public static final IdleReason VACUUM_TIER = new IdleReason(builder("gtocore.issue.vacuum_tier", "Vacuum Tier Too Low", "真空等级不足")
            .id(GTOCore.id("vacuum_tier")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION).args(NUMBERS)
            .desc("gtocore.issue.vacuum_tier.desc", "Requires vacuum tier %s, current %s.", "需要真空等级 %s，当前 %s。"));
    public static final IdleReason GRAVITY = new IdleReason(builder("gtocore.issue.gravity", "Gravity Mismatch", "重力不符")
            .id(GTOCore.id("gravity")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION)
            .args(i -> new Object[] { percent(i.a()), percent(i.b()) })
            .desc("gtocore.issue.gravity.desc", "Requires gravity of %s, current %s.", "需要重力为 %s，当前 %s。"));
    public static final IdleReason BEAM_INTENSITY = new IdleReason(builder("gtocore.issue.beam_intensity", "Beam Requirement Not Met", "光束未达要求")
            .id(GTOCore.id("beam_intensity")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION).args(NUMBER)
            .desc("gtocore.issue.beam_intensity.desc", "Requires a matching beam with an average intensity of at least %s.", "需要符合要求且平均强度不低于 %s 的光束。"));
    public static final IdleReason RUN_LIMIT = new IdleReason(builder("gtocore.issue.run_limit", "Run Limit Reached", "运行次数已达上限")
            .id(GTOCore.id("run_limit")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION).args(NUMBERS)
            .desc("gtocore.issue.run_limit.desc", "This recipe can run at most %s times; it has run %s times.", "本配方最多运行 %s 次，已运行 %s 次。"));
    public static final IdleReason THERMAL_ZONE_TEMP_DIFFERENCE = new IdleReason(builder("gtocore.issue.thermal_zone_temp_difference", "Thermal Zone Difference Too Large", "热区温差过大")
            .id(GTOCore.id("thermal_zone_temp_difference")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION).args(NUMBERS)
            .desc("gtocore.issue.thermal_zone_temp_difference.desc", "Requires a temperature difference of at most %s, current %s.", "需要温差不超过 %s，当前 %s。"));
    public static final IdleReason NEUTRON_EV_RANGE = new IdleReason(builder("gtocore.issue.neutron_ev_range", "Kinetic Energy Out of Range", "中子动能不在区间")
            .id(GTOCore.id("neutron_ev_range")).category(IssueCategory.CONDITION).stage(IssueStage.MODIFIER)
            .args(i -> new Object[] { IssueText.number(NeutronActivatorMachine.rangeMin(i.a())), IssueText.number(NeutronActivatorMachine.rangeMax(i.a())), FormattingUtil.formatNumbers(i.b() / 1000000D) })
            .desc("gtocore.issue.neutron_ev_range.desc", "Requires between %s and %s MeV, exclusive; current %s MeV.", "需要 %s ~ %s MeV（不含端点），当前 %s MeV。"));
    public static final IdleReason TECH_NODE_LOCKED = new IdleReason(builder("gtocore.issue.tech_node_locked", "Tech Node Locked", "科技节点未解锁")
            .id(GTOCore.id("tech_node_locked")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.tech_node_locked.desc", "The required tech node must be unlocked first.", "需要先解锁对应的科技节点。"));
    public static final IdleReason PREREQUISITES_NOT_RESEARCHED = new IdleReason(builder("gtocore.machine.data_center.data_access.prerequisites_not_researched", null, null)
            .id(GTOCore.id("prerequisites_not_researched")).category(IssueCategory.CONDITION).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.prerequisites_not_researched.desc", "The prerequisites of the selected tech node have not been researched.", "所选科技节点的前置节点尚未研究。"));
    public static final IdleReason NO_OWNER = new IdleReason(builder("gtocore.issue.no_owner", "No Owner", "无所有者")
            .id(GTOCore.id("no_owner")).category(IssueCategory.CONDITION).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.no_owner.desc", "The machine has no owner and cannot use the wireless network.", "机器没有所有者，无法使用无线网络。"));

    public static final IdleReason NO_ORES = new IdleReason(builder("gtocore.idle_reason.no_ores", "No ores available in this dimension", "该维度中没有可用的矿石")
            .id(GTOCore.id("no_ores")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.no_ores", "No Ores Available", "无可用矿石").desc("gtocore.idle_reason.no_ores"));
    public static final IdleReason NO_BEDROCK = new IdleReason(builder("gtocore.issue.no_bedrock", "No Bedrock Below", "下方无基岩")
            .id(GTOCore.id("no_bedrock")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.no_bedrock.desc", "The ninth block below the controller must be bedrock.", "控制器下方第九格需要是基岩。"));
    public static final IdleReason NO_BEDROCK_FLUIDS = new IdleReason(builder("gtocore.issue.no_bedrock_fluids", "No Fluids Available", "无可用流体")
            .id(GTOCore.id("no_bedrock_fluids")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.no_bedrock_fluids.desc", "The recorded dimension has no bedrock fluids.", "所记录的维度中没有基岩流体。"));
    public static final IdleReason DAYTIME_ONLY = new IdleReason(builder("gtocore.issue.daytime_only", "Daytime Only", "仅限白天")
            .id(GTOCore.id("daytime_only")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.daytime_only.desc", "This machine works only during the day.", "本机器只在白天工作。"));
    public static final IdleReason INCORRECT_DIRECTION_VOLTA = new IdleReason(builder("gtocore.idle_reason.incorrect_direction_volta", "The machine placed in this direction can't get sunlight", "这个方向摆放的机器晒不到太阳")
            .id(GTOCore.id("incorrect_direction_volta")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.incorrect_direction_volta", "Wrong Facing", "朝向错误").desc("gtocore.idle_reason.incorrect_direction_volta"));
    public static final IdleReason OBSTRUCTED_VOLTA = new IdleReason(builder("gtocore.idle_reason.obstructed_volta", "The solar panel is obstructed", "太阳能板被遮挡了")
            .id(GTOCore.id("obstructed_volta")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.obstructed_volta", "Sunlight Obstructed", "光照被遮挡").desc("gtocore.idle_reason.obstructed_volta"));
    public static final IdleReason SURFACE_ONLY_VOLTA = new IdleReason(builder("gtocore.idle_reason.surface_only_volta", "The photovoltaic power station cannot operate in planetary orbit", "光伏电站无法在星球轨道中工作")
            .id(GTOCore.id("surface_only_volta")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.surface_only_volta", "Surface Only", "仅限星球表面").desc("gtocore.idle_reason.surface_only_volta"));
    public static final IdleReason ORBIT_ONLY_VOLTA = new IdleReason(builder("gtocore.idle_reason.orbit_only_volta", "The photovoltaic sail only operates in planetary orbit", "光伏帆板仅能在星球轨道中工作")
            .id(GTOCore.id("orbit_only_volta")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.orbit_only_volta", "Orbit Only", "仅限星球轨道").desc("gtocore.idle_reason.orbit_only_volta"));
    public static final IdleReason SKY_OBSTRUCTED = new IdleReason(builder("gtocore.issue.sky_obstructed", "Sky Obstructed", "天空被遮挡")
            .id(GTOCore.id("sky_obstructed")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.sky_obstructed.desc", "The receiving area above the machine must have a clear view of the sky.", "机器上方的接收区域须能直视天空。"));
    public static final IdleReason MANA_CONDENSER_FORM = new IdleReason(builder("gtocore.idle_reason.mana_condenser_form", "The array form does not match the current dimension", "阵列形态与所在维度不符")
            .id(GTOCore.id("mana_condenser_form")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.mana_condenser_form", "Array Form Mismatch", "阵列形态不符").desc("gtocore.idle_reason.mana_condenser_form"));
    public static final IdleReason NETHER_ONLY = new IdleReason(builder("gtocore.issue.nether_only", "Nether Only", "仅限下界")
            .id(GTOCore.id("nether_only")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.nether_only.desc", "This machine works only in the Nether.", "本机器只能在下界运行。"));
    public static final IdleReason BIOME_UNSUITABLE = new IdleReason(builder("gtocore.issue.biome_unsuitable", "Unsuitable Biome", "生物群系不适用")
            .id(GTOCore.id("biome_unsuitable")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.biome_unsuitable.desc", "No water can be produced in the current biome.", "所在生物群系无法产出水。"));
    public static final IdleReason INSUFFICIENT_CLEANROOM = new IdleReason(builder("gtocore.machine.space_bio_research_module.insufficient_cleanroom", null, null)
            .id(GTOCore.id("insufficient_cleanroom")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .title("gtocore.issue.insufficient_cleanroom", "Cleanroom Level Too Low", "超净等级不足").desc("gtocore.machine.space_bio_research_module.insufficient_cleanroom"));
    public static final IdleReason ONLY_IN_SPACE = new IdleReason(builder("gtocore.issue.only_in_space", "Space Only", "仅限太空")
            .id(GTOCore.id("only_in_space")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.only_in_space.desc", "This machine works only in space.", "本机器只能在太空中运行。"));
    public static final IdleReason ONLY_ON_PLANET = new IdleReason(builder("gtocore.issue.only_on_planet", "Planet Only", "仅限行星")
            .id(GTOCore.id("only_on_planet")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.only_on_planet.desc", "This machine works only on a planet.", "本机器只能在行星上运行。"));
    public static final IdleReason DYSON_SPHERE_COMPLETE = new IdleReason(builder("gtocore.issue.dyson_sphere_complete", "Dyson Sphere Complete", "戴森球已建成")
            .id(GTOCore.id("dyson_sphere_complete")).category(IssueCategory.ENVIRONMENT).severity(IssueSeverity.INFO).stage(IssueStage.MODIFIER).args(NUMBER)
            .desc("gtocore.issue.dyson_sphere_complete.desc", "The Dyson sphere of this dimension already has %s modules, the maximum.", "所在维度的戴森球已有 %s 个模块，达到上限。"));
    public static final IdleReason DYSON_SPHERE_EMPTY = new IdleReason(builder("gtocore.issue.dyson_sphere_empty", "No Dyson Sphere", "无戴森球")
            .id(GTOCore.id("dyson_sphere_empty")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.dyson_sphere_empty.desc", "No Dyson sphere module has been launched in this dimension.", "所在维度尚未发射戴森球模块。"));
    public static final IdleReason DYSON_SPHERE_IN_USE = new IdleReason(builder("gtocore.issue.dyson_sphere_in_use", "Dyson Sphere In Use", "戴森球被占用")
            .id(GTOCore.id("dyson_sphere_in_use")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.dyson_sphere_in_use.desc", "Another machine in this dimension is using the Dyson sphere.", "所在维度的另一台机器正在使用戴森球。"));

    public static final IdleReason NANITES_NOT_CONNECTED = new IdleReason(builder("gtocore.issue.nanites_not_connected", "Not Connected", "未连接主机")
            .id(GTOCore.id("nanites_not_connected")).category(IssueCategory.STRUCTURE).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.nanites_not_connected.desc", "The module must be connected to a Nanites Integrated Processing Center.", "本模块需要连接到纳米集成加工中心。"));
    public static final IdleReason NANITES_MODULE_MISSING = new IdleReason(builder("gtocore.issue.nanites_module_missing", "Module Missing", "缺少模块")
            .id(GTOCore.id("nanites_module_missing")).category(IssueCategory.STRUCTURE).stage(IssueStage.MODIFIER)
            .args(i -> new Object[] { NanitesIntegratedMachine.moduleName(i.a()) })
            .desc("gtocore.issue.nanites_module_missing.desc", "This recipe requires a connected %s.", "本配方需要连接%s。"));
    public static final IdleReason MAGIC_MODULE_MISSING = new IdleReason(builder("gtocore.issue.magic_module_missing", "Magic Module Missing", "缺少魔法模块")
            .id(GTOCore.id("magic_module_missing")).category(IssueCategory.STRUCTURE).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.magic_module_missing.desc", "Mana-producing recipes require the Magic Module to be built.", "产出魔力的配方需要搭建魔法模块。"));
    public static final IdleReason PLASMA_WINGS_MISSING = new IdleReason(builder("gtocore.issue.plasma_wings_missing", "Plasma Condensing Wings Missing", "缺少等离子冷凝翼")
            .id(GTOCore.id("plasma_wings_missing")).category(IssueCategory.STRUCTURE).stage(IssueStage.CONDITION)
            .desc("gtocore.issue.plasma_wings_missing.desc", "Plasma condensing recipes require the Plasma Condensing Wings to be built.", "等离子冷凝配方需要搭建等离子冷凝翼。"));
    public static final IdleReason SPACE_ELEVATOR_NOT_CONNECTED = new IdleReason(builder("gtocore.issue.space_elevator_not_connected", "Elevator Not Connected", "未连接太空电梯")
            .id(GTOCore.id("space_elevator_not_connected")).category(IssueCategory.STRUCTURE).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.space_elevator_not_connected.desc", "This module is not connected to a space elevator.", "本模块未连接到太空电梯。"));
    public static final IdleReason SPACE_ELEVATOR_NOT_RUNNING = new IdleReason(builder("gtocore.issue.space_elevator_not_running", "Elevator Not Running", "太空电梯未运行")
            .id(GTOCore.id("space_elevator_not_running")).category(IssueCategory.ENVIRONMENT).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.space_elevator_not_running.desc", "The connected space elevator is not running; modules work only while it runs.", "所连接的太空电梯未在运行，模块仅在其运行时工作。"));
    public static final IdleReason MUFFLER_NOT_SUPPORTED = new IdleReason(builder("gtocore.idle_reason.muffler_not_supported", "The machine voltage tier does not support advanced muffler", "机器电压等级不支持高级消声仓")
            .id(GTOCore.id("muffler_not_supported")).category(IssueCategory.PART).stage(IssueStage.MODIFIER)
            .title("gtocore.issue.muffler_not_supported", "Muffler Tier Too High", "消声仓等级过高").desc("gtocore.idle_reason.muffler_not_supported"));
    public static final IdleReason COIL_NOT_USABLE = new IdleReason(builder("gtocore.machine.dimensionally_transcendent_plasma_forge.coil", null, null)
            .id(GTOCore.id("coil_not_usable")).category(IssueCategory.PART).stage(IssueStage.MODIFIER)
            .title("gtocore.issue.coil_not_usable", "Coil Not Usable", "线圈不适用").desc("gtocore.machine.dimensionally_transcendent_plasma_forge.coil"));
    public static final IdleReason STEAM_VENT_OBSTRUCTED = new IdleReason(builder("gtocore.issue.steam_vent_obstructed", "Steam Vent Obstructed", "蒸汽排气口受阻")
            .id(GTOCore.id("steam_vent_obstructed")).category(IssueCategory.PART).stage(IssueStage.MODIFIER)
            .desc("recipe.condition.steam_vent.tooltip"));
    public static final IdleReason ROTOR_MISMATCH = new IdleReason(builder("gtocore.issue.rotor_mismatch", "Rotor Materials Differ", "转子材料不一致")
            .id(GTOCore.id("rotor_mismatch")).category(IssueCategory.PART).stage(IssueStage.MODIFIER)
            .desc("gtocore.issue.rotor_mismatch.desc", "All rotors must be made of the same material.", "所有转子需使用同一种材料。"));

    public static final IdleReason MANA_FLOW_TOO_WEAK = new IdleReason(builder("gtocore.issue.mana_flow_too_weak", "Mana Flow Too Weak", "魔力流不足")
            .id(GTOCore.id("mana_flow_too_weak")).category(IssueCategory.ENERGY).stage(IssueStage.MODIFIER).args(NUMBERS)
            .desc("gtocore.issue.mana_flow_too_weak.desc", "Requires %s mana/t, maximum rate %s mana/t.", "需要 %s 魔力/t，最大速率 %s 魔力/t。"));
    public static final IdleReason DRONE_NO_ENERGY = new IdleReason(builder("gtocore.machine.space_drone_dock.drone_no_energy", null, null)
            .id(GTOCore.id("drone_no_energy")).category(IssueCategory.ENERGY).stage(IssueStage.CONDITION)
            .title("gtocore.issue.drone_no_energy", "Drone Out of Power", "无人机电量不足").desc("gtocore.machine.space_drone_dock.drone_no_energy"));
    public static final IdleReason HARMONY_GRID_SHORT = new IdleReason(builder("gtocore.issue.harmony_grid_short", "Wireless Grid Short", "无线电网储能不足")
            .id(GTOCore.id("harmony_grid_short")).category(IssueCategory.ENERGY).stage(IssueStage.MODIFIER)
            .args(i -> new Object[] { i.b() < 1 || i.b() > 4 ? IssueText.number(-1) : FormattingUtil.formatNumbers(HarmonyMachine.recipeEnergy((int) i.b(), (int) i.a())) })
            .desc("gtocore.issue.harmony_grid_short.desc", "The startup energy is %s EU; the wireless grid must hold more than this.", "启动耗能为 %s EU，无线电网储能须大于该值。"));

    public static final IdleReason HEAT_FULL = new IdleReason(builder("gtocore.issue.heat_full", "Heat Full", "热量已满")
            .id(GTOCore.id("heat_full")).category(IssueCategory.OUTPUT).severity(IssueSeverity.INFO).stage(IssueStage.OUTPUT).args(NUMBERS)
            .desc("gtocore.issue.heat_full.desc", "Stored heat is %s / %s HU; heating resumes once heat is drawn.", "储热为 %s / %s HU，热量被取用后继续加热。"));
    public static final IdleReason PLANT_WAITING = new IdleReason(builder("gtocore.issue.plant_waiting", "Plant Waiting", "水处理厂等待中")
            .id(GTOCore.id("plant_waiting")).category(IssueCategory.MACHINE).severity(IssueSeverity.WARNING).stage(IssueStage.WORKING)
            .desc("gtocore.issue.plant_waiting.desc", "The water purification plant is waiting, so this unit is waiting as well.", "所属水处理厂处于等待状态，本单元随之等待。"));

    private IdleReason(Builder builder) {
        super(builder);
    }

    public static void init() {}

    private static Component percent(long value) {
        return value < 0 ? Component.literal("—") : Component.literal(value + "%");
    }
}
