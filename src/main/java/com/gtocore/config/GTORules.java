package com.gtocore.config;

import com.gtolib.api.rule.BoolRule;
import com.gtolib.api.rule.DoubleRule;
import com.gtolib.api.rule.FloatRule;
import com.gtolib.api.rule.IntRule;
import com.gtolib.api.rule.LongRule;
import com.gtolib.api.rule.RuleGroup;
import com.gtolib.api.rule.RuleManager;
import com.gtolib.api.rule.RulePreset;
import com.gtolib.api.rule.RuleSelection;
import com.gtolib.api.rule.Rules;
import com.gtolib.api.rule.TierRule;

import net.minecraftforge.fml.loading.FMLPaths;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public final class GTORules {

    private static final Logger LOGGER = LogUtils.getLogger();

    private GTORules() {}

    public static final RuleGroup RECIPES = Rules.group("recipes", "gtocore.rule.group.recipes", "配方", "Recipes");
    public static final RuleGroup RESOURCES = Rules.group("resources", "gtocore.rule.group.resources", "资源与世界", "Resources & World");
    public static final RuleGroup STEAM = Rules.group("steam", "gtocore.rule.group.steam", "蒸汽时代", "Steam Age");
    public static final RuleGroup POWER = Rules.group("power", "gtocore.rule.group.power", "发电与能源", "Power & Energy");
    public static final RuleGroup MACHINES = Rules.group("machines", "gtocore.rule.group.machines", "机器", "Machines");
    public static final RuleGroup ME = Rules.group("me", "gtocore.rule.group.me", "ME 网络", "ME Network");
    public static final RuleGroup RESEARCH = Rules.group("research", "gtocore.rule.group.research", "研究与太空", "Research & Space");
    public static final RuleGroup SURVIVAL = Rules.group("survival", "gtocore.rule.group.survival", "生存与杂项", "Survival & Misc");
    public static final RuleGroup GTM_RECIPES = Rules.group("gtm_recipes", "gtocore.rule.group.gtm_recipes", "GTM 配方", "GTM Recipes");
    public static final RuleGroup GTM_MACHINES = Rules.group("gtm_machines", "gtocore.rule.group.gtm_machines", "GTM 机器与工具", "GTM Machines & Tools");

    public static final TierRule RECIPE_TIER = Rules.tierRule("recipes.tier").group(RECIPES)
            .name("gtocore.rule.recipes.tier", "配方难度", "Recipe Difficulty")
            .desc("gtocore.rule.recipes.tier.desc", "决定 GTO 自身配方的档位，包括原料用量、电路等级、部分机器和物品的合成方式，以及聚变、恒星熔炼等配方类型的规则。", "Sets the tier of GTO's own recipes: ingredient amounts, circuit tiers, how some machines and items are crafted, and the rules of recipe types such as fusion and stellar forge.")
            .register();
    public static final TierRule MOD_RECIPE_TIER = Rules.tierRule("recipes.mod_integration").group(RECIPES)
            .name("gtocore.rule.recipes.mod_integration", "模组配方难度", "Mod Recipe Difficulty")
            .desc("gtocore.rule.recipes.mod_integration.desc", "决定管道、路由、背包、存储、电脑等辅助模组的配方档位。简单档保留这些模组的原始配方。", "Sets the recipe tier of utility mods such as pipes, routers, backpacks, storage and computers. The easy tier keeps these mods' original recipes.")
            .register();
    public static final TierRule ORE_TIER = Rules.tierRule("recipes.ore_processing").group(RECIPES)
            .name("gtocore.rule.recipes.ore_processing", "矿物处理难度", "Ore Processing Difficulty")
            .desc("gtocore.rule.recipes.ore_processing.desc", "决定矿石处理配方的产出、耗时和耗电。", "Sets the output, duration and energy use of ore processing recipes.")
            .register();
    public static final DoubleRule RECIPE_DURATION = Rules.doubleRule("recipes.duration_multiplier").group(RECIPES)
            .defaults(1, 1, 1).range(0.001, 1000)
            .name("gtocore.rule.recipes.duration_multiplier", "配方时间乘数", "Recipe Duration Multiplier")
            .desc("gtocore.rule.recipes.duration_multiplier.desc", "全局配方的运行时间都会乘以这个倍数，发电和产魔机器不受影响。", "All recipe durations are multiplied by this factor. EU and mana generators are not affected.")
            .register();
    public static final IntRule CIRCUIT_ASSEMBLY_LINE_AMOUNT = Rules.intRule("recipes.circuit_assembly_line_amount").group(RECIPES)
            .defaults(3, 3, 4).range(1, 64)
            .name("gtocore.rule.recipes.circuit_assembly_line_amount", "电路装配线电路板需求数量", "Circuit Assembly Line Component Amount")
            .register();

    public static final TierRule FLUID_TIER = Rules.tierRule("resources.fluid").group(RESOURCES)
            .name("gtocore.rule.resources.fluid", "流体资源难度", "Fluid Resource Difficulty")
            .desc("gtocore.rule.resources.fluid.desc", "专家档下，基岩流体矿脉和油砂离心会产出含杂质的流体，需要额外的化工流程提纯。", "On the expert tier, bedrock fluid veins and oil sand centrifuging yield impure fluids that need extra chemical processing.")
            .register();
    public static final FloatRule GTM_RUBBER_TREE = Rules.floatRule("gtm.rubber_tree_chance").group(RESOURCES)
            .defaults(1.5F, 1.0F, 0.5F).range(0, 10)
            .name("gtocore.rule.gtm.rubber_tree_chance", "橡胶树生成率", "Rubber Tree Spawn Chance")
            .desc("gtocore.rule.gtm.rubber_tree_chance.desc", "每次尝试生成橡胶树时成功的概率，大于 1 时总会生成。只影响新生成的区块。", "Chance for each rubber tree placement attempt; values above 1 always succeed. Only affects newly generated chunks.")
            .register();
    public static final IntRule GTM_BEDROCK_ORE_DISTANCE = Rules.intRule("gtm.bedrock_ore_distance").group(RESOURCES)
            .defaults(1, 2, 3).range(1, 64)
            .name("gtocore.rule.gtm.bedrock_ore_distance", "基岩矿脉间距", "Bedrock Ore Vein Distance")
            .desc("gtocore.rule.gtm.bedrock_ore_distance.desc", "基岩矿脉之间相隔的区块数。", "Distance between bedrock ore veins in chunks.")
            .register();
    public static final BoolRule GTM_INFINITE_BEDROCK = Rules.boolRule("gtm.infinite_bedrock_resources").group(RESOURCES)
            .defaults(true, false, false)
            .name("gtocore.rule.gtm.infinite_bedrock_resources", "基岩资源无限", "Infinite Bedrock Resources")
            .desc("gtocore.rule.gtm.infinite_bedrock_resources.desc", "开启后基岩矿脉和基岩流体矿脉不会枯竭。", "Bedrock ore and fluid veins never deplete.")
            .register();

    public static final DoubleRule STEAM_DURATION = Rules.doubleRule("steam.multiblock_duration").group(STEAM)
            .defaults(1.2, 1.5, 1.6).range(0.01, 100)
            .name("gtocore.rule.steam.multiblock_duration", "蒸汽多方块耗时乘数", "Steam Multiblock Duration Multiplier")
            .tooltip("gtocore.rule.steam.multiblock_duration.tooltip", "蒸汽耗时乘数 : %s", "Recipe using time: Duration Multiplier x %s")
            .tooltipLine("gtocore.rule.steam.multiblock_duration.tooltip.0", "蒸汽时代，多方块并行机器的配方处理受技术限制，时间被迫延长", "In the steam era, the processing time of parallel machines is limited by technology, and the time is forced to be extended")
            .register();
    public static final IntRule STEAM_PARALLELS = Rules.intRule("steam.multiblock_parallels").group(STEAM)
            .defaults(16, 8, 8).range(1, 1024)
            .name("gtocore.rule.steam.multiblock_parallels", "蒸汽多方块最大并行数", "Steam Multiblock Max Parallels")
            .tooltip("gtocore.rule.steam.multiblock_parallels.tooltip", "最大并行数", "Max Parallels")
            .register();
    public static final DoubleRule LARGE_STEAM_DURATION = Rules.doubleRule("steam.large_multiblock_duration").group(STEAM)
            .defaults(1.0, 1.2, 1.5).range(0.01, 100)
            .name("gtocore.rule.steam.large_multiblock_duration", "大型蒸汽多方块耗时乘数", "Large Steam Multiblock Duration Multiplier")
            .tooltip("gtocore.rule.steam.large_multiblock_duration.tooltip", "蒸汽耗时乘数 : %s", "Recipe using time: Duration Multiplier x %s")
            .tooltipLine("gtocore.rule.steam.large_multiblock_duration.tooltip.0", "蒸汽时代，多方块并行机器的配方处理受技术限制，时间被迫延长", "In the steam era, the processing time of parallel machines is limited by technology, and the time is forced to be extended")
            .register();
    public static final IntRule LARGE_STEAM_PARALLELS = Rules.intRule("steam.large_multiblock_parallels").group(STEAM)
            .defaults(64, 32, 32).range(1, 1024)
            .name("gtocore.rule.steam.large_multiblock_parallels", "大型蒸汽多方块最大并行数", "Large Steam Multiblock Max Parallels")
            .tooltip("gtocore.rule.steam.large_multiblock_parallels.tooltip", "最大并行数", "Max Parallels")
            .tooltipLine("gtocore.rule.steam.large_multiblock_parallels.tooltip.0", "此多方块机器可以使用的最大并行数", "The maximum number of parallel machines that can be used in this multiblock")
            .register();
    public static final IntRule STEAM_CIRCUIT_PARALLELS = Rules.intRule("steam.circuit_assembler_parallels").group(STEAM)
            .defaults(8, 4, 4).range(1, 1024)
            .name("gtocore.rule.steam.circuit_assembler_parallels", "大型蒸汽电路组装机最大并行数", "Large Steam Circuit Assembler Max Parallels")
            .tooltip("gtocore.rule.steam.circuit_assembler_parallels.tooltip", "最大并行数", "Max Parallels")
            .register();
    public static final IntRule STEAM_CIRCUIT_DURATION = Rules.intRule("steam.circuit_assembler_duration").group(STEAM)
            .defaults(2, 4, 6).range(1, 100)
            .name("gtocore.rule.steam.circuit_assembler_duration", "大型蒸汽电路组装机增产模式耗时乘数", "Large Steam Circuit Assembler Multiply Mode Duration")
            .tooltip("gtocore.rule.steam.circuit_assembler_duration.tooltip", "增产模式耗时乘数 : %s", "Multiply Mode, Recipe Duration: x %s")
            .tooltipLine("gtocore.rule.steam.circuit_assembler_duration.tooltip.0", "更多的时间，更高的产出，一报换一报这很合理", "More time, higher output, this is reasonable")
            .register();
    public static final IntRule STEAM_CIRCUIT_STEAM_COST = Rules.intRule("steam.circuit_assembler_steam_cost").group(STEAM)
            .defaults(2, 4, 6).range(1, 100)
            .name("gtocore.rule.steam.circuit_assembler_steam_cost", "大型蒸汽电路组装机增产模式蒸汽消耗乘数", "Large Steam Circuit Assembler Multiply Mode Steam Cost")
            .tooltip("gtocore.rule.steam.circuit_assembler_steam_cost.tooltip", "增产模式蒸汽消耗乘数 : %s", "Multiply Mode Steam Cost: x %s")
            .register();
    public static final IntRule STEAM_CIRCUIT_OUTPUT = Rules.intRule("steam.circuit_assembler_output").group(STEAM)
            .defaults(4, 2, 2).range(1, 100)
            .name("gtocore.rule.steam.circuit_assembler_output", "大型蒸汽电路组装机增产模式产出乘数", "Large Steam Circuit Assembler Multiply Mode Output")
            .tooltip("gtocore.rule.steam.circuit_assembler_output.tooltip", "增产模式产出乘数 : %s", "Multiply Mode Multiply Output: x %s")
            .register();
    public static final IntRule STEAM_CIRCUIT_ENGRAVING = Rules.intRule("steam.circuit_assembler_engraving").group(STEAM)
            .defaults(8, 16, 32).range(1, 1024)
            .name("gtocore.rule.steam.circuit_assembler_engraving", "大型蒸汽电路组装机蚀刻电路所需数量", "Large Steam Circuit Assembler Engraving Amount")
            .tooltip("gtocore.rule.steam.circuit_assembler_engraving.tooltip", "蚀刻电路所需数量", "Engraving Circuit Needed Amount")
            .tooltipLine("gtocore.rule.steam.circuit_assembler_engraving.tooltip.0", "执行相应电路组装配方前，需要蚀刻此电路所需的物品数量", "The amount of items needed to engrave the circuit before executing the corresponding circuit assembly recipe")
            .register();
    public static final IntRule STEAM_SOLAR_RATE = Rules.intRule("steam.solar_boiler_rate").group(STEAM)
            .defaults(20, 15, 10).range(1, 10000)
            .name("gtocore.rule.steam.solar_boiler_rate", "大型太阳能锅炉单集热管产率", "Large Solar Boiler Steam per Tube")
            .tooltip("gtocore.rule.steam.solar_boiler_rate.tooltip", "单集热管产率 : %s / t", "Steam production per tube : %s / t")
            .register();
    public static final IntRule STEAM_TANK_CAPACITY = Rules.intRule("steam.storage_tank_capacity").group(STEAM)
            .defaults(1296000000, 864000000, 648000000).range(1, Integer.MAX_VALUE)
            .name("gtocore.rule.steam.storage_tank_capacity", "大型蒸汽储存罐容量", "Large Steam Storage Tank Capacity")
            .tooltip("gtocore.rule.steam.storage_tank_capacity.tooltip", "容量", "Capacity")
            .tooltipLine("gtocore.rule.steam.storage_tank_capacity.tooltip.0", "大型蒸汽储存罐的容量，单位为mB", "The capacity of the large steam storage tank in mB")
            .register();
    public static final IntRule GTM_BOILER_OUTPUT = Rules.intRule("gtm.boiler_output").group(STEAM)
            .defaults(4, 2, 1).range(1, 64)
            .name("gtocore.rule.gtm.boiler_output", "锅炉倍率", "Boiler Factor")
            .desc("gtocore.rule.gtm.boiler_output.desc", "小型锅炉的产汽量，以及大型锅炉的每份水产汽量、最高温度和升温速度都乘以这个数。", "Multiplies small boiler output, and large boiler steam per water, maximum temperature and heating speed.")
            .register();

    public static final DoubleRule GENERATOR_ARRAY_MULTIPLY = Rules.doubleRule("power.generator_array_multiplier").group(POWER)
            .defaults(2, 1.3, 1.3).range(0, 100)
            .name("gtocore.rule.power.generator_array_multiplier", "发电阵列乘数", "Generator Array Multiply")
            .tooltipLine("gtocore.rule.power.generator_array_multiplier.tooltip.0", "发电阵列的功率奖励乘数，影响每个发电机的输出功率。", "The power multiplier bonus of the generator array, which affects the output power of each generator.")
            .tooltipLine("gtocore.rule.power.generator_array_multiplier.tooltip.1", "数值越大，发电机的输出功率越高。", "The larger the value, the higher the output power of the generator.")
            .tooltipLine("gtocore.rule.power.generator_array_multiplier.tooltip.2", "此值仅与难度挂钩，代表不同难度下的发电机效率。", "This value is only related to difficulty, representing the efficiency of generators at different difficulty levels.")
            .register();
    public static final IntRule GENERATOR_ARRAY_LOSS = Rules.intRule("power.generator_array_loss").group(POWER)
            .defaults(4, 5, 8).range(0, 9)
            .name("gtocore.rule.power.generator_array_loss", "发电阵列无线模式损耗", "Generator Array Wireless Loss")
            .tooltip("gtocore.rule.power.generator_array_loss.tooltip", "发电阵列无线模式损耗 : 0.0%s", "Generator Array Wireless Loss : 0.0%s")
            .tooltipLine("gtocore.rule.power.generator_array_loss.tooltip.0", "发电阵列在无线模式下的损耗，影响传输到无线网络的能量损失。", "The loss of the generator array in wireless mode, which affects the loss of energy transferred to the wireless network.")
            .tooltipLine("gtocore.rule.power.generator_array_loss.tooltip.1", "数值越大，连接无线网络的损耗越大。", "The larger the value, the greater the connection loss to the wireless network.")
            .register();
    public static final IntRule GENERATOR_ARRAY_LIMIT = Rules.intRule("power.generator_array_limit").group(POWER)
            .defaults(16, 4, 4).range(1, 64)
            .name("gtocore.rule.power.generator_array_limit", "发电阵列内部发电机限制", "Generator Array Internal Generator Limit")
            .tooltipLine("gtocore.rule.power.generator_array_limit.tooltip.0", "发电阵列发电量和消耗量取决于内部发电机种类和个数", "The power generation and consumption of the generator array depend on the types and number of internal generators.")
            .tooltipLine("gtocore.rule.power.generator_array_limit.tooltip.1", "内部发电机个数越多，其发电量和消耗量越高。", "The more internal generators, the higher the power generation and consumption.")
            .tooltipLine("gtocore.rule.power.generator_array_limit.tooltip.2", "例如：放4个蒸汽发电机，发电量为(4*发电阵列乘数*蒸汽发电机的发电量)，", "For example: placing 4 steam generators will result in a power generation of (4 * generator array multiplier * steam generator's power generation).")
            .register();
    public static final DoubleRule LIGHTNING_ROD_BREAK = Rules.doubleRule("power.lightning_rod_break_chance").group(POWER)
            .defaults(0.2, 0.3, 0.4).range(0, 1)
            .name("gtocore.rule.power.lightning_rod_break_chance", "雷击杆破坏概率", "Lightning Rod Break Probability")
            .tooltipLine("gtocore.rule.power.lightning_rod_break_chance.tooltip.0", "雷击杆被雷击后，被破坏的概率为%s。", "Probability of the lightning rod being destroyed after being struck by lightning: %s.")
            .register();
    public static final IntRule WIND_MILL_AMPERAGE = Rules.intRule("power.wind_mill_amperage").group(POWER)
            .defaults(2, 1, 1).range(1, 64)
            .name("gtocore.rule.power.wind_mill_amperage", "风力涡轮机输出电流", "Wind Turbine Output Amperage")
            .tooltip("gtocore.rule.power.wind_mill_amperage.tooltip", "输出电流", "Output Amperage")
            .tooltipLine("gtocore.rule.power.wind_mill_amperage.tooltip.0", "风力涡轮机的最大输出电流。", "The maximum output amperage of the wind turbine.")
            .register();
    public static final TierRule FUEL_CELL_TIER = Rules.tierRule("power.fuel_cell").group(POWER)
            .name("gtocore.rule.power.fuel_cell", "燃料电池机制", "Fuel Cell Mechanics")
            .desc("gtocore.rule.power.fuel_cell.desc", "简单档的吸收效率不会衰减；专家档使用专家膜效率与衰减参数。", "On the easy tier, absorption efficiency never decays; the expert tier uses the expert membrane efficiency and decay values.")
            .register();
    public static final DoubleRule FUEL_CELL_CONSUME = Rules.doubleRule("power.fuel_cell_membrane_damage").group(POWER)
            .defaults(0, 0.035, 0.055).range(0, 1)
            .name("gtocore.rule.power.fuel_cell_membrane_damage", "放电时膜损坏概率", "Fuel Cell Membrane Damage Chance on Discharge")
            .tooltipLine("gtocore.rule.power.fuel_cell_membrane_damage.tooltip.0", "放电时使用的膜材料的损坏概率。", "The chance of the membrane material used being damaged upon discharging.")
            .register();
    public static final BoolRule MEGA_TURBINE_REGULATOR = Rules.boolRule("power.mega_turbine_regulator").group(POWER)
            .defaults(false, false, true)
            .name("gtocore.rule.power.mega_turbine_regulator", "巨型涡轮高速调节器", "Mega Turbine High-Speed Regulator")
            .desc("gtocore.rule.power.mega_turbine_regulator.desc", "开启后巨型涡轮可以调节高速倍率，玻璃等级会降低转子损坏基数。", "Mega turbines can adjust the high-speed multiplier, and the glass tier lowers the rotor damage base.")
            .register();
    public static final FloatRule MEGA_TURBINE_OUTPUT = Rules.floatRule("power.mega_turbine_high_speed_output").group(POWER)
            .defaults(4.0F, 3.0F, 2.5F).range(0, 100)
            .name("gtocore.rule.power.mega_turbine_high_speed_output", "巨型涡轮高速模式输出乘数", "Mega Turbine High Speed Mode Output Multiplier")
            .tooltip("gtocore.rule.power.mega_turbine_high_speed_output.tooltip", "高速模式输出乘数 : %s", "High Speed Mode Output Multiplier : %s Multiplier")
            .register();
    public static final IntRule MEGA_TURBINE_ROTOR_DAMAGE = Rules.intRule("power.mega_turbine_high_speed_rotor_damage").group(POWER)
            .defaults(4, 10, 12).range(1, 1000)
            .name("gtocore.rule.power.mega_turbine_high_speed_rotor_damage", "巨型涡轮高速模式转子损坏乘数", "Mega Turbine High Speed Mode Rotor Damage Multiplier")
            .tooltip("gtocore.rule.power.mega_turbine_high_speed_rotor_damage.tooltip", "高速模式转子损坏乘数 : %s", "High Speed Mode Rotor Damage Multiplier : %s Multiplier")
            .register();
    public static final FloatRule MEGA_TURBINE_FAULT = Rules.floatRule("power.mega_turbine_high_speed_fault").group(POWER)
            .defaults(4F, 8F, 10F).range(0, 1000)
            .name("gtocore.rule.power.mega_turbine_high_speed_fault", "巨型涡轮高速模式机器故障乘数", "Mega Turbine High Speed Mode Machine Fault Multiplier")
            .tooltip("gtocore.rule.power.mega_turbine_high_speed_fault.tooltip", "高速模式机器故障乘数 : %s", "High Speed Mode Machine Fault Multiplier : %s Multiplier")
            .register();
    public static final IntRule MAGIC_GENERATOR_AMPERAGE = Rules.intRule("power.magic_generator_amperage").group(POWER)
            .defaults(8, 4, 2).range(1, 64)
            .name("gtocore.rule.power.magic_generator_amperage", "魔力发电机输出电流", "Magic Generator Output Amperage")
            .desc("gtocore.rule.power.magic_generator_amperage.desc", "单方块魔力发电机的输入与输出电流。", "Input and output amperage of single-block magic generators.")
            .register();
    public static final IntRule FUEL_EFFICIENCY_OFFSET = Rules.intRule("power.fuel_efficiency_offset").group(POWER)
            .defaults(15, 0, -15).range(-100, 100)
            .name("gtocore.rule.power.fuel_efficiency_offset", "燃料发电效率修正（%）", "Fuel Generator Efficiency Offset (%)")
            .desc("gtocore.rule.power.fuel_efficiency_offset.desc", "加到单方块燃料发电机效率上的百分比。", "Percentage added to the efficiency of single-block fuel generators.")
            .register();
    public static final IntRule BIO_STIMULATION_DURATION = Rules.intRule("power.bio_stimulation_duration").group(POWER)
            .defaults(120, 240, 360).range(1, 72000)
            .name("gtocore.rule.power.bio_stimulation_duration", "生物电刺激耗时（刻）", "Bio Stimulation Duration (ticks)")
            .desc("gtocore.rule.power.bio_stimulation_duration.desc", "生物振荡发电机的电刺激模块每次运行的耗时。", "Duration of each run of the Bio Oscillation Generator's electrical stimulator.")
            .register();
    public static final TierRule WIRELESS_ENERGY_TIER = Rules.tierRule("power.wireless_energy_unit").group(POWER)
            .name("gtocore.rule.power.wireless_energy_unit", "无线能量单元难度", "Wireless Energy Unit Difficulty")
            .desc("gtocore.rule.power.wireless_energy_unit.desc", "决定无线能量单元的容量和损耗。", "Sets the capacity and loss of wireless energy units.")
            .register();
    public static final BoolRule GTM_GENERATE_NO_MATCH = Rules.boolRule("gtm.generate_energy_no_match").group(POWER)
            .defaults(false, false, true)
            .name("gtocore.rule.gtm.generate_energy_no_match", "发电机满载时照常消耗燃料", "Generators Burn Fuel When Full")
            .desc("gtocore.rule.gtm.generate_energy_no_match.desc", "开启后，发电机在能量缓存已满时仍会运行，多出的能量被浪费。", "Generators keep running when their energy buffer is full, wasting the extra energy.")
            .register();

    public static final IntRule FLUID_PIPE_THROUGHPUT = Rules.intRule("machines.fluid_pipe_throughput").group(MACHINES)
            .defaults(3, 2, 1).range(1, 64)
            .name("gtocore.rule.machines.fluid_pipe_throughput", "流体管道流量倍数", "Fluid Pipe Throughput Multiplier")
            .register();
    public static final IntRule MAINTENANCE_WEAR = Rules.intRule("machines.maintenance_wear").group(MACHINES)
            .defaults(1, 2, 3).range(0, 100).runtime()
            .name("gtocore.rule.machines.maintenance_wear", "维护损耗速度", "Maintenance Wear Rate")
            .desc("gtocore.rule.machines.maintenance_wear.desc", "多方块机器运行时，维护计时累积速度的倍率。", "How fast multiblocks accumulate maintenance time while running.")
            .register();
    public static final BoolRule MAINTENANCE_FAILURES = Rules.boolRule("machines.maintenance_random_failures").group(MACHINES)
            .defaults(false, true, true).runtime()
            .name("gtocore.rule.machines.maintenance_random_failures", "随机维护故障", "Random Maintenance Failures")
            .desc("gtocore.rule.machines.maintenance_random_failures.desc", "开启后，维护计时累积到一定程度时机器会随机出现维护问题。", "Machines randomly develop maintenance problems once enough maintenance time accumulates.")
            .register();
    public static final FloatRule CONFIGURABLE_MAINTENANCE_MAX = Rules.floatRule("machines.configurable_maintenance_max").group(MACHINES)
            .defaults(1.3F, 1.2F, 1.1F).range(0.01F, 100)
            .name("gtocore.rule.machines.configurable_maintenance_max", "可配置维护仓速度乘数上限", "Configurable Maintenance Speed Multiplier Maximum")
            .tooltip("gtocore.rule.machines.configurable_maintenance_max.tooltip", "配方处理速度乘数上限 : %s", "Configurable Recipe Speed Multiplier Maximum : %s Multiplier")
            .tooltipLine("gtocore.rule.machines.configurable_maintenance_max.tooltip.0", "不计超频，配方处理速度乘数的最高值", "Ignore overclocking, the recipe processing speed is the highest multiplier for normal speed")
            .register();
    public static final FloatRule CONFIGURABLE_MAINTENANCE_MIN = Rules.floatRule("machines.configurable_maintenance_min").group(MACHINES)
            .defaults(0.7F, 0.8F, 0.9F).range(0.01F, 100)
            .name("gtocore.rule.machines.configurable_maintenance_min", "可配置维护仓速度乘数下限", "Configurable Maintenance Speed Multiplier Minimum")
            .tooltip("gtocore.rule.machines.configurable_maintenance_min.tooltip", "配方处理速度乘数下限 : %s", "Configurable Recipe Speed Multiplier Minimum : %s Multiplier")
            .tooltipLine("gtocore.rule.machines.configurable_maintenance_min.tooltip.0", "不计超频，配方处理速度乘数的最低值", "Ignore overclocking, the recipe processing speed is the lowest multiplier for normal speed")
            .register();
    public static final TierRule MUFFLER_TIER = Rules.tierRule("machines.muffler").group(MACHINES)
            .name("gtocore.rule.machines.muffler", "消声仓机制", "Muffler Mechanics")
            .desc("gtocore.rule.machines.muffler.desc", "简单档不产生灰烬；专家档的消声仓等级必须与机器等级匹配，且总会产生灰烬。", "No ash on the easy tier; on the expert tier the muffler tier must match the machine tier and ash is always produced.")
            .register();
    public static final BoolRule POWER_LOSS_SHUTDOWN = Rules.boolRule("machines.power_loss_shutdown").group(MACHINES)
            .defaults(false, false, true).runtime()
            .name("gtocore.rule.machines.power_loss_shutdown", "断电停机", "Shut Down on Power Loss")
            .desc("gtocore.rule.machines.power_loss_shutdown.desc", "开启后，机器断电会直接停机并清空进度；关闭时只回退进度。", "Machines shut down and lose their progress on power loss; otherwise the progress only rolls back.")
            .register();
    public static final IntRule PARALLEL_HATCH_BONUS = Rules.intRule("machines.parallel_hatch_bonus").group(MACHINES)
            .defaults(1, 0, 0).range(0, 5)
            .name("gtocore.rule.machines.parallel_hatch_bonus", "并行控制仓等级加成", "Parallel Hatch Tier Bonus")
            .desc("gtocore.rule.machines.parallel_hatch_bonus.desc", "并行控制仓按高出这么多等级计算并行数。", "Parallel hatches count as this many tiers higher.")
            .register();
    public static final IntRule PROCESSING_PLANT_PARALLEL = Rules.intRule("machines.processing_plant_parallel").group(MACHINES)
            .defaults(4, 2, 2).range(1, 1024)
            .name("gtocore.rule.machines.processing_plant_parallel", "加工厂每级并行数", "Processing Plant Parallels per Tier")
            .desc("gtocore.rule.machines.processing_plant_parallel.desc", "加工厂每高一级获得的并行数，安装扩展模块后翻倍。", "Parallels gained per tier by the processing plant, doubled with the extension module.")
            .register();
    public static final IntRule TIME_TWISTER_ENERGY = Rules.intRule("machines.time_twister_energy").group(MACHINES)
            .defaults(4, 8, 16).range(1, 1024).runtime()
            .name("gtocore.rule.machines.time_twister_energy", "时间扭曲器耗能倍率", "Time Twister Energy Multiplier")
            .desc("gtocore.rule.machines.time_twister_energy.desc", "使用时间扭曲器加速机器时，能量消耗的倍率。", "Energy cost multiplier when accelerating machines with the Time Twister.")
            .register();
    public static final IntRule WIRELESS_CHARGER_AMOUNT = Rules.intRule("machines.wireless_charger_amount").group(MACHINES)
            .defaults(1, 16, 64).range(1, 64)
            .name("gtocore.rule.machines.wireless_charger_amount", "力场发生器需求数量", "Field Generator Required Amount")
            .tooltipLine("gtocore.rule.machines.wireless_charger_amount.tooltip.0", "放入%s个对应等级的力场发生器，即可开启无线功能。", "Put %s Field Generator into the machine, and it will be enabled.")
            .tooltipLine("gtocore.rule.machines.wireless_charger_amount.tooltip.1", "等级越高，综合电流越大，FE物品速度无限。", "The higher the level, the greater the total current, and the speed of FE items is infinite.")
            .tooltipLine("gtocore.rule.machines.wireless_charger_amount.tooltip.2", "当等级达到HV时，其他机器会自动添加到无线网络中。", "When the level is HV or above, other machines will be automatically added to the wireless network.")
            .tooltipLine("gtocore.rule.machines.wireless_charger_amount.tooltip.3", "当等级达到EV时，机器为玩家的充电范围变为无限。", "When the level is EV or above, the charging range for players becomes infinite.")
            .tooltipLine("gtocore.rule.machines.wireless_charger_amount.tooltip.4", "机器内可查看为机器远程充电的距离和最大电流", "You can view the charging range and maximum current for remote charging in the machine.")
            .register();

    public static final IntRule SIMPLE_PATTERN_BUFFER_SLOTS = Rules.intRule("me.simple_pattern_buffer_slots").group(ME)
            .defaults(9, 0, 0).range(0, 64)
            .name("gtocore.rule.me.simple_pattern_buffer_slots", "ME简单样板总成插槽数", "ME Simple Pattern Buffer Slots")
            .desc("gtocore.rule.me.simple_pattern_buffer_slots.desc", "为 0 时不提供 ME 简单样板总成。", "The ME Simple Pattern Buffer is unavailable when this is 0.")
            .register();
    public static final IntRule ME_MUFFLER_MAX = Rules.intRule("me.muffler_amplifier_max").group(ME)
            .defaults(4, 16, 64).range(1, 64)
            .name("gtocore.rule.me.muffler_amplifier_max", "ME消声仓集控核心最大数量", "ME Muffler Hatch Amplifier Maximum")
            .desc("gtocore.rule.me.muffler_amplifier_max.desc", "增幅到最大值所需的集控核心数量。", "Number of amplifiers needed for the maximum bonus.")
            .register();
    public static final IntRule ME_MUFFLER_MIN = Rules.intRule("me.muffler_amplifier_min").group(ME)
            .defaults(1, 4, 16).range(0, 64)
            .name("gtocore.rule.me.muffler_amplifier_min", "ME消声仓集控核心最小数量", "ME Muffler Hatch Amplifier Minimum")
            .desc("gtocore.rule.me.muffler_amplifier_min.desc", "启用增幅所需的集控核心数量。", "Number of amplifiers needed to start the bonus.")
            .register();
    public static final DoubleRule GTM_ME_HATCH_ENERGY = Rules.doubleRule("gtm.me_hatch_energy").group(ME)
            .defaults(32, 64, 96).range(0, 100000)
            .name("gtocore.rule.gtm.me_hatch_energy", "ME仓室耗能", "ME Hatch Energy Usage")
            .desc("gtocore.rule.gtm.me_hatch_energy.desc", "ME 输入/输出仓室与总线每刻消耗的 AE 能量。", "AE energy consumed per tick by ME hatches and buses.")
            .register();

    public static final IntRule SCAN_DURATION = Rules.intRule("research.scan_duration").group(RESEARCH)
            .defaults(200, 400, 600).range(1, 72000)
            .name("gtocore.rule.research.scan_duration", "扫描耗时（刻）", "Scan Duration (ticks)")
            .desc("gtocore.rule.research.scan_duration.desc", "扫描仪和智能扫描管理平台每次扫描的耗时。", "Duration of each scan in the Scanner and the Intelligent Scanning Management Platform.")
            .register();
    public static final FloatRule SCAN_PENALTY = Rules.floatRule("research.repeated_scan_penalty").group(RESEARCH)
            .defaults(0.25F, 0.0625F, 0.015625F).range(0, 1)
            .name("gtocore.rule.research.repeated_scan_penalty", "重复扫描收益系数", "Repeated Scan Factor")
            .desc("gtocore.rule.research.repeated_scan_penalty.desc", "重复扫描同一种物品时，获得研究点数的倍率。", "Research point multiplier when scanning something that has already been scanned.")
            .register();
    public static final TierRule TECH_TREE_TIER = Rules.tierRule("research.tech_tree").group(RESEARCH)
            .name("gtocore.rule.research.tech_tree", "科技树难度", "Tech Tree Difficulty")
            .desc("gtocore.rule.research.tech_tree.desc", "决定科技树节点的研究材料、灵感物品概率和节点等级。", "Sets the research materials, eureka chances and tiers of tech tree nodes.")
            .register();
    public static final IntRule ENERGY_DATA_WARMUP = Rules.intRule("research.energy_data_warmup").group(RESEARCH)
            .defaults(600, 900, 1200).range(0, 720000)
            .name("gtocore.rule.research.energy_data_warmup", "能源数据仓预热时间（刻）", "Energy Data Hatch Warm-up (ticks)")
            .desc("gtocore.rule.research.energy_data_warmup.desc", "机器连续运行多久后，能源数据仓才开始收集数据。", "How long a machine must run continuously before the energy data hatch starts collecting data.")
            .register();
    public static final DoubleRule SPACE_ELEVATOR_COMPUTATION = Rules.doubleRule("research.space_elevator_computation").group(RESEARCH)
            .defaults(1.5, 3.0, 4.5).range(0, 1000)
            .name("gtocore.rule.research.space_elevator_computation", "太空电梯连接算力系数", "Space Elevator Link Computation Factor")
            .desc("gtocore.rule.research.space_elevator_computation.desc", "连接模块对太空电梯算力需求的放大系数。", "How much connector modules scale up the space elevator's computation demand.")
            .register();
    public static final IntRule SPACE_DATA_WORKS = Rules.intRule("research.space_data_module_works").group(RESEARCH)
            .defaults(50, 50, 75).range(1, 100000)
            .name("gtocore.rule.research.space_data_module_works", "太空电梯工程数据模块工作量", "Space Elevator Engineering Data Workload")
            .desc("gtocore.rule.research.space_data_module_works.desc", "工程数据模块每产出一份数据需要完成的工作次数。", "Number of works the engineering data module needs for each piece of data.")
            .register();

    public static final BoolRule MOB_ENHANCEMENT = Rules.boolRule("survival.mob_enhancement").group(SURVIVAL)
            .defaults(false, true, true).runtime()
            .name("gtocore.rule.survival.mob_enhancement", "怪物强化", "Mob Enhancement")
            .desc("gtocore.rule.survival.mob_enhancement.desc", "开启后，怪物会按原版难度获得额外强化。", "Mobs gain extra enhancements based on the vanilla difficulty.")
            .register();
    public static final IntRule REGEN_FOOD_THRESHOLD = Rules.intRule("survival.regen_food_threshold").group(SURVIVAL)
            .defaults(5, 15, 15).range(0, 20).runtime()
            .name("gtocore.rule.survival.regen_food_threshold", "额外回血饱食度门槛", "Extra Regeneration Food Threshold")
            .desc("gtocore.rule.survival.regen_food_threshold.desc", "饱食度高于此值时，玩家会周期性额外回复生命。", "Players periodically regain extra health while their food level is above this value.")
            .register();
    public static final IntRule REGEN_FACTOR = Rules.intRule("survival.regen_factor").group(SURVIVAL)
            .defaults(3, 2, 1).range(1, 100).runtime()
            .name("gtocore.rule.survival.regen_factor", "额外回血强度", "Extra Regeneration Strength")
            .desc("gtocore.rule.survival.regen_factor.desc", "数值越大，每次额外回复的生命越多。", "Higher values restore more health each time.")
            .register();
    public static final IntRule ULTIMINE_LIMIT = Rules.intRule("survival.ultimine_limit").group(SURVIVAL)
            .defaults(64, 32, 16).range(1, 4096).runtime()
            .name("gtocore.rule.survival.ultimine_limit", "连锁挖掘上限", "Ultimine Limit")
            .desc("gtocore.rule.survival.ultimine_limit.desc", "空手或普通物品连锁挖掘的方块上限，挖掘工具为其一半。", "Block limit for ultimining with bare hands or ordinary items; digging tools get half of it.")
            .register();
    public static final LongRule VIRTUAL_COIN_COST = Rules.longRule("survival.virtual_coin_cost").group(SURVIVAL)
            .defaults(250, 250, 1000).range(1, 1_000_000_000_000L).runtime()
            .name("gtocore.rule.survival.virtual_coin_cost", "虚拟币基础工作量", "Virtual Coin Base Workload")
            .desc("gtocore.rule.survival.virtual_coin_cost.desc", "获得下一枚虚拟币所需工作量的基数。", "Base of the work needed to earn the next virtual coin.")
            .register();

    public static final BoolRule GTM_DISABLE_MANUAL_COMPRESSION = gtmRecipe("gtm.disable_manual_compression", "gtocore.rule.gtm.disable_manual_compression", "gtocore.rule.gtm.disable_manual_compression.desc", false, true, true, "禁用手动压缩", "Disable Manual Compression", "移除工作台里的块与锭的压缩和解压配方。", "Removes block and ingot compression and decompression in the crafting table.");
    public static final BoolRule GTM_HARDER_RODS = gtmRecipe("gtm.harder_rods", "gtocore.rule.gtm.harder_rods", "gtocore.rule.gtm.harder_rods.desc", false, false, true, "更难的杆配方", "Harder Rods", "杆的制作更贵。", "Makes rods more expensive to craft.");
    public static final BoolRule GTM_HARDER_BRICKS = gtmRecipe("gtm.harder_bricks", "gtocore.rule.gtm.harder_bricks", "gtocore.rule.gtm.harder_bricks.desc", false, false, true, "更难的砖配方", "Harder Bricks", "砖、耐火砖、下界砖和焦炉砖的合成更难。", "Makes bricks, firebricks, nether bricks and coke oven bricks harder to craft.");
    public static final BoolRule GTM_NERF_WOOD = gtmRecipe("gtm.nerf_wood_crafting", "gtocore.rule.gtm.nerf_wood_crafting", "gtocore.rule.gtm.nerf_wood_crafting.desc", false, true, true, "削弱木材合成", "Nerf Wood Crafting", "1 个原木只能合成 2 个木板，2 个木板只能合成 2 根木棍。", "One log makes 2 planks, and 2 planks make 2 sticks.");
    public static final BoolRule GTM_HARD_WOOD = gtmRecipe("gtm.hard_wood", "gtocore.rule.gtm.hard_wood", "gtocore.rule.gtm.hard_wood.desc", false, true, true, "更难的木制品配方", "Hard Wood Recipes", "除木棍和木板外的木制品配方更难。", "Makes wood-related recipes harder, except sticks and planks.");
    public static final BoolRule GTM_HARD_IRON = gtmRecipe("gtm.hard_iron", "gtocore.rule.gtm.hard_iron", "gtocore.rule.gtm.hard_iron.desc", false, true, true, "更难的铁制品配方", "Hard Iron Recipes", "桶、炼药锅、漏斗、铁栏杆等配方更难。", "Makes buckets, cauldrons, hoppers, iron bars and similar recipes harder.");
    public static final BoolRule GTM_HARD_REDSTONE = gtmRecipe("gtm.hard_redstone", "gtocore.rule.gtm.hard_redstone", "gtocore.rule.gtm.hard_redstone.desc", false, true, true, "更难的红石配方", "Hard Redstone Recipes", "红石相关配方更难。", "Makes redstone-related recipes harder.");
    public static final BoolRule GTM_HARD_TOOL_ARMOR = gtmRecipe("gtm.hard_tool_armor", "gtocore.rule.gtm.hard_tool_armor", "gtocore.rule.gtm.hard_tool_armor.desc", false, true, true, "更难的原版工具与盔甲", "Hard Tool and Armor Recipes", "原版工具和盔甲的配方更难。", "Makes vanilla tool and armor recipes harder.");
    public static final BoolRule GTM_HARD_MISC = gtmRecipe("gtm.hard_misc", "gtocore.rule.gtm.hard_misc", "gtocore.rule.gtm.hard_misc.desc", false, true, true, "更难的杂项配方", "Hard Misc Recipes", "各种杂项配方更难。", "Makes miscellaneous recipes harder.");
    public static final BoolRule GTM_HARD_GLASS = gtmRecipe("gtm.hard_glass", "gtocore.rule.gtm.hard_glass", "gtocore.rule.gtm.hard_glass.desc", false, true, true, "更难的玻璃配方", "Hard Glass Recipes", "玻璃相关配方更难。", "Makes glass-related recipes harder.");
    public static final BoolRule GTM_NERF_PAPER = gtmRecipe("gtm.nerf_paper", "gtocore.rule.gtm.nerf_paper", "gtocore.rule.gtm.nerf_paper.desc", false, true, true, "削弱纸张合成", "Nerf Paper Crafting", "削弱纸的合成配方。", "Nerfs the paper crafting recipe.");
    public static final BoolRule GTM_HARD_ADVANCED_IRON = gtmRecipe("gtm.hard_advanced_iron", "gtocore.rule.gtm.hard_advanced_iron", "gtocore.rule.gtm.hard_advanced_iron.desc", false, true, true, "更难的高级铁制品配方", "Hard Advanced Iron Recipes", "铁门、铁活板门、铁砧等配方更难。", "Makes iron doors, trapdoors, anvils and similar recipes harder.");
    public static final BoolRule GTM_HARD_DYE = gtmRecipe("gtm.hard_dye", "gtocore.rule.gtm.hard_dye", "gtocore.rule.gtm.hard_dye.desc", false, true, true, "更难的染色配方", "Hard Dye Recipes", "染色混凝土、玻璃等方块更难。", "Makes coloring blocks such as concrete or glass harder.");
    public static final BoolRule GTM_HARDER_CHARCOAL = gtmRecipe("gtm.harder_charcoal", "gtocore.rule.gtm.harder_charcoal", "gtocore.rule.gtm.harder_charcoal.desc", false, true, true, "移除熔炉烧木炭", "Harder Charcoal", "移除原版熔炉烧制木炭的配方。", "Removes charcoal smelting from the vanilla furnace.");
    public static final BoolRule GTM_FLINT_AND_STEEL = gtmRecipe("gtm.flint_and_steel_requires_steel", "gtocore.rule.gtm.flint_and_steel_requires_steel", "gtocore.rule.gtm.flint_and_steel_requires_steel.desc", false, true, true, "打火石需要钢", "Flint and Steel Requires Steel", "打火石的配方需要钢制零件。", "The flint and steel recipe requires steel parts.");
    public static final BoolRule GTM_REMOVE_VANILLA_BLOCKS = gtmRecipe("gtm.remove_vanilla_block_recipes", "gtocore.rule.gtm.remove_vanilla_block_recipes", "gtocore.rule.gtm.remove_vanilla_block_recipes.desc", false, true, true, "移除原版方块配方", "Remove Vanilla Block Recipes", "移除工作台中的原版方块配方。", "Removes vanilla block recipes from the crafting table.");
    public static final BoolRule GTM_REMOVE_TNT = gtmRecipe("gtm.remove_vanilla_tnt", "gtocore.rule.gtm.remove_vanilla_tnt", "gtocore.rule.gtm.remove_vanilla_tnt.desc", false, true, true, "移除原版 TNT 配方", "Remove Vanilla TNT Recipe", "移除工作台中的原版 TNT 配方。", "Removes the vanilla TNT recipe from the crafting table.");
    public static final IntRule GTM_CASINGS_PER_CRAFT = Rules.intRule("gtm.casings_per_craft").group(GTM_RECIPES)
            .defaults(2, 1, 1).range(1, 3)
            .name("gtocore.rule.gtm.casings_per_craft", "每次合成的机械方块数", "Casings per Craft")
            .desc("gtocore.rule.gtm.casings_per_craft.desc", "每次合成得到的多方块机械方块数量。", "How many multiblock casings each craft makes.")
            .register();
    public static final BoolRule GTM_HARDER_CIRCUITS = gtmRecipe("gtm.harder_circuits", "gtocore.rule.gtm.harder_circuits", "gtocore.rule.gtm.harder_circuits.desc", false, true, true, "更难的电路配方", "Harder Circuit Recipes", "电路配方产出更少。", "Circuit recipes yield less.");
    public static final BoolRule GTM_HARD_MULTIBLOCKS = gtmRecipe("gtm.hard_multiblock_controllers", "gtocore.rule.gtm.hard_multiblock_controllers", "gtocore.rule.gtm.hard_multiblock_controllers.desc", false, false, true, "更难的多方块主机配方", "Hard Multiblock Controller Recipes", "削弱多方块机器主机的配方。", "Nerfs multiblock controller recipes.");

    public static final BoolRule GTM_ENCHANTED_TOOLS = gtmMachine("gtm.enchanted_tools", "gtocore.rule.gtm.enchanted_tools", "gtocore.rule.gtm.enchanted_tools.desc", true, false, false, "工具自带附魔", "Enchanted Tools", "GT 工具制作出来时自带附魔。", "GT tools come with enchantments when crafted.");
    public static final BoolRule GTM_LOW_ENERGY_PROGRESS = gtmMachine("gtm.recipe_progress_low_energy", "gtocore.rule.gtm.recipe_progress_low_energy", "gtocore.rule.gtm.recipe_progress_low_energy.desc", false, false, true, "缺电清空进度", "Reset Progress on Low Energy", "供电不足时机器的配方进度归零。", "Machines lose all recipe progress when energy runs short.");
    public static final BoolRule GTM_REQUIRE_TOOLS = gtmMachine("gtm.require_gt_tools", "gtocore.rule.gtm.require_gt_tools", "gtocore.rule.gtm.require_gt_tools.desc", false, true, true, "需要 GT 工具拆除方块", "Require GT Tools", "拆除机器、外壳、导线等需要扳手、剪线钳等 GT 工具。", "Wrenches, wire cutters and other GT tools are required to break machines, casings, wires and more.");
    public static final BoolRule GTM_WEATHER_EXPLOSION = gtmMachine("gtm.weather_explosions", "gtocore.rule.gtm.weather_explosions", "gtocore.rule.gtm.weather_explosions.desc", false, false, true, "雨天和环境爆炸", "Weather and Terrain Explosions", "机器在雨中或靠近火、岩浆等时会爆炸。", "Machines explode in the rain or next to fire, lava and similar terrain.");
    public static final BoolRule GTM_EXPLOSION_TERRAIN = gtmMachine("gtm.explosion_damages_terrain", "gtocore.rule.gtm.explosion_damages_terrain", "gtocore.rule.gtm.explosion_damages_terrain.desc", false, true, true, "爆炸破坏地形", "Explosions Damage Terrain", "机器和锅炉爆炸会破坏地形。", "Machine and boiler explosions damage the terrain.");
    public static final BoolRule GTM_HARMLESS_TRANSFORMERS = gtmMachine("gtm.harmless_active_transformers", "gtocore.rule.gtm.harmless_active_transformers", "gtocore.rule.gtm.harmless_active_transformers.desc", true, false, false, "有源变压器无害", "Harmless Active Transformers", "有源变压器不会造成伤害或爆炸。", "Active transformers never hurt or explode.");
    public static final BoolRule GTM_CLEANROOM = gtmMachine("gtm.cleanroom", "gtocore.rule.gtm.cleanroom", "gtocore.rule.gtm.cleanroom.desc", false, true, true, "启用超净间", "Enable Cleanroom", "部分配方需要超净间。", "Some recipes require a cleanroom.");
    public static final BoolRule GTM_CLEAN_MULTIBLOCKS = gtmMachine("gtm.clean_multiblocks", "gtocore.rule.gtm.clean_multiblocks", "gtocore.rule.gtm.clean_multiblocks.desc", true, false, false, "多方块无视超净要求", "Multiblocks Ignore Cleanroom", "多方块机器无视所有超净间要求。", "Multiblocks ignore every cleanroom requirement.");
    public static final BoolRule GTM_MAINTENANCE = gtmMachine("gtm.maintenance", "gtocore.rule.gtm.maintenance", "gtocore.rule.gtm.maintenance.desc", false, true, true, "启用维护", "Enable Maintenance", "多方块机器需要维护仓并会出现维护问题。", "Multiblocks need a maintenance hatch and develop maintenance problems.");
    public static final IntRule GTM_DUAL_CHAMBER = Rules.intRule("gtm.dual_chamber_mode").group(GTM_MACHINES)
            .defaults(1, 1, 3).range(1, 3)
            .name("gtocore.rule.gtm.dual_chamber_mode", "双腔增压模式", "Dual Chamber Pressurization Mode")
            .desc("gtocore.rule.gtm.dual_chamber_mode.desc", "1 为简单，2 为普通，3 为专家。", "1 is simple, 2 is normal, 3 is expert.")
            .register();
    public static final BoolRule GTM_TIERED_CASINGS = gtmMachine("gtm.tiered_casings", "gtocore.rule.gtm.tiered_casings", "gtocore.rule.gtm.tiered_casings.desc", false, true, true, "等级外壳", "Tiered Casings", "大部分 GCYM 多方块需要限定最高电压的等级方块。", "Most GCYM multiblocks need blocks that set their maximum voltage.");
    public static final BoolRule GTM_ORDERED_ITEMS = gtmMachine("gtm.ordered_assembly_line_items", "gtocore.rule.gtm.ordered_assembly_line_items", "gtocore.rule.gtm.ordered_assembly_line_items.desc", false, true, true, "装配线物品有序", "Ordered Assembly Line Items", "装配线的物品输入必须按顺序放置。", "Assembly line item inputs must be in order.");
    public static final BoolRule GTM_ORDERED_FLUIDS = gtmMachine("gtm.ordered_assembly_line_fluids", "gtocore.rule.gtm.ordered_assembly_line_fluids", "gtocore.rule.gtm.ordered_assembly_line_fluids.desc", false, false, true, "装配线流体有序", "Ordered Assembly Line Fluids", "装配线的流体输入必须按顺序放置。", "Assembly line fluid inputs must be in order.");
    public static final IntRule GTM_ELECTRIC_TOOL_ENERGY = Rules.intRule("gtm.electric_tool_energy").group(GTM_MACHINES)
            .defaults(100, 200, 300).range(0, 100000)
            .name("gtocore.rule.gtm.electric_tool_energy", "电动工具耗电（%）", "Electric Tool Energy Use (%)")
            .desc("gtocore.rule.gtm.electric_tool_energy.desc", "电动物品耗电的百分比倍率。", "Energy use multiplier for electric items, in percent.")
            .register();
    public static final IntRule GTM_PROSPECTOR_ENERGY = Rules.intRule("gtm.prospector_energy").group(GTM_MACHINES)
            .defaults(100, 200, 300).range(0, 100000)
            .name("gtocore.rule.gtm.prospector_energy", "探矿仪耗电（%）", "Prospector Energy Use (%)")
            .desc("gtocore.rule.gtm.prospector_energy.desc", "探矿仪耗电的百分比倍率。", "Energy use multiplier for prospectors, in percent.")
            .register();
    public static final IntRule GTM_ELECTRIC_TOOL_DAMAGE = Rules.intRule("gtm.electric_tool_damage_chance").group(GTM_MACHINES)
            .defaults(10, 20, 40).range(0, 100)
            .name("gtocore.rule.gtm.electric_tool_damage_chance", "电动工具损耗概率（%）", "Electric Tool Damage Chance (%)")
            .desc("gtocore.rule.gtm.electric_tool_damage_chance.desc", "电动工具每次使用时真正损耗耐久的概率。", "Chance for electric tools to take actual damage on each use.")
            .register();
    public static final BoolRule GTM_DISABLE_HAZARDS = gtmMachine("gtm.disable_hazards", "gtocore.rule.gtm.disable_hazards", "gtocore.rule.gtm.disable_hazards.desc", false, false, false, "关闭材料危害", "Disable Material Hazards", "开启后关闭有害材料的危害效果；关闭时沿用 GTM 配置。", "Turns off hazardous material effects; when off, the GTM config decides.");

    private static BoolRule gtmRecipe(String id, String nameKey, String descKey, boolean easy, boolean normal, boolean expert, String cn, String en, String descCn, String descEn) {
        return Rules.boolRule(id).group(GTM_RECIPES).defaults(easy, normal, expert)
                .name(nameKey, cn, en)
                .desc(descKey, descCn, descEn)
                .register();
    }

    private static BoolRule gtmMachine(String id, String nameKey, String descKey, boolean easy, boolean normal, boolean expert, String cn, String en, String descCn, String descEn) {
        return Rules.boolRule(id).group(GTM_MACHINES).defaults(easy, normal, expert)
                .name(nameKey, cn, en)
                .desc(descKey, descCn, descEn)
                .register();
    }

    public static void init() {}

    private static final Pattern LEGACY_LINE = Pattern.compile("^\\s*difficulty\\s*:\\s*(\\S+)\\s*$");

    @Nullable
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static RuleSelection legacySelection() {
        var file = FMLPaths.CONFIGDIR.get().resolve("gtocore.yaml");
        if (!Files.exists(file)) return null;
        try {
            RulePreset base = null;
            for (var line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                var matcher = LEGACY_LINE.matcher(line);
                if (matcher.matches()) base = RulePreset.byId(matcher.group(1));
            }
            var selection = new RuleSelection(base == null ? RulePreset.NORMAL : base);
            LOGGER.info("Migrated GTO rules from {}: preset {}", file, selection.base().id);
            return selection;
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Failed to migrate GTO rules from {}", file, e);
            return null;
        }
    }

    static {
        RuleManager.bootstrap(FMLPaths.CONFIGDIR.get().resolve("gtocore-rules.txt"), GTORules::legacySelection);
    }
}
