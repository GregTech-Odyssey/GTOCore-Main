package com.gtocore.data.lang;

import com.gtocore.common.machine.monitor.DisplayRegistry;

import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import static com.gtocore.data.lang.LangHandler.addCNEN;

final class MachineLang {

    static void init() {
        addCNEN("gtocore.structure.deprecated", "该结构已弃用", "This structure has been deprecated");
        addCNEN("gui.tooltips.redstone_mode.enabled", "启用红石模式，机器接收红石信号", "Enable redstone mode, the machine will receive redstone signals");
        addCNEN("gui.tooltips.redstone_mode.disabled", "禁用红石模式，机器无视红石信号", "Disable redstone mode, the machine will ignore redstone signals");
        addCNEN("gtocore.machine.programmablec_hatch.extra_tooltip.0", "通过虚拟物品提供器来设置电路槽物品", "Set the circuit slot items through a virtual item provider");
        addCNEN("gtocore.machine.programmablec_hatch.extra_tooltip.1", "可独立指定仓室的配方类型，覆盖机器的默认设定", "Can independently select recipe types, overriding the machine’s default setting");
        addCNEN("gtocore.machine.programmablec_hatch.extra_tooltip.2", "虚拟物品槽：§b1§r", "Virtual item slots: §b1§r");
        addCNEN("gtocore.machine.energy_loss", "能量损失: %s", "Energy loss: %s");
        addCNEN("gtocore.machine.highlight_module", "高亮显示模块位置", "Highlight module position");
        addCNEN("gtocore.machine.highlight_obstruction", "高亮显示会被阻挡的区域", "Highlight areas that will be obstructed");
        addCNEN("gtocore.machine.storage_transfer", "存储转移", "Transfer Storage");
        addCNEN("gtocore.machine.virtual_ingredient_provider.configured", "已配置", "Configured");
        addCNEN("gtocore.machine.storage_transfer.tooltip", "把网络里其它 ME 存储的内容全部搬进这里（装不下的留在原处）", "Moves everything held by the other ME storages in the network in here (what does not fit stays where it is)");
        addCNEN("gtocore.machine.structure_check", "更新结构检查", "Update structure check");
        addCNEN("gtocore.machine.structure_check.click", "点击重新检查结构", "Click to recheck the structure");
        addCNEN("gtocore.machine.structure_check.checking", "检查中", "Checking");
        addCNEN("gtocore.machine.overclock_configurator", "调整机器超频的最小时间", "Adjust the minimum time for machine overclocking");
        addCNEN("gtocore.machine.thread", "同时处理至多 %1$s 种不同配方，每种配方至多 %2$s 个", "Processing up to %1$s different recipes simultaneously, with a maximum of %2$s for each recipe");
        addCNEN("gtocore.machine.thread.0", "同时处理至多 %s 种不同配方", "Processing up to %s different recipes simultaneously");
        addCNEN("gtocore.machine.thread.1", "每种配方至多 %s 个", "With a maximum of %s for each recipe");
        addCNEN("gtocore.machine.thread_hatch.tooltip.0", "可为机器提供 %s 线程的并行", "Can provide %s thread parallel processing for the machine");
        addCNEN("gtocore.machine.total_time.duration", "额外耗时减免: %s", "Additional Duration Reduction: %s");
        addCNEN("gtocore.machine.block_conversion_room.am", "每次转化数量: %s", "Amount converted each time: %s");
        addCNEN("gtocore.machine.vacuum_pump.tooltip.0", "仅向水平相邻方块提供真空", "Only provides vacuum to horizontally adjacent blocks");
        addCNEN("gtocore.machine.dimensionally_transcendent_plasma_forge.coil", "当前配方模式无法使用该线圈", "Current recipe mode cannot use this coil");
        addCNEN("gtocore.machine.duration_multiplier.tooltip", "耗时乘数: %s", "Duration Multiplication: %s");
        addCNEN("gtocore.machine.dyson_sphere.amount", "该星系发射次数: %s / 100000", "Launch Times: %s / 100000");
        addCNEN("gtocore.machine.dyson_sphere.voltage", "最大能量输出: %s", "Maximum Energy Output: %s");
        addCNEN("gtocore.machine.efficiency.tooltip", "§7效率: §r%s", "§7Efficiency: §r%s");
        addCNEN("gtocore.machine.eut_multiplier.tooltip", "耗能乘数: %s", "Energy Consumption Multiplier: %s");
        addCNEN("gtocore.machine.eye_of_harmony.eu", "启动耗能: %s", "Startup Energy Consumption: %s");
        addCNEN("gtocore.machine.eye_of_harmony.helium", "氦储量: %smB", "Helium Storage: %smB");
        addCNEN("gtocore.machine.eye_of_harmony.hydrogen", "氢储量: %smB", "Hydrogen Storage: %smB");
        addCNEN("gtocore.machine.fission_reactor.cooler", "冷却组件数量: %s，相邻数: %s", "Amount of Cooling Components: %s, Adjacent Count: %s");
        addCNEN("gtocore.machine.fission_reactor.damaged", "损坏: %s", "Damage: %s");
        addCNEN("gtocore.machine.fission_reactor.fuel", "燃料组件数量: %s，相邻数: %s", "Amount of Fuel Components: %s, Adjacent Count: %s");
        addCNEN("gtocore.machine.fission_reactor.heat", "堆温: %s K", "Reactor Temperature: %s K");
        addCNEN("gtocore.machine.generator_array.wireless", "无线电网模式:", "Wireless Network Mode:");
        addCNEN("gtocore.machine.greenhouse.SkyLight", "当前光照: %s", "Current Illumination: %s");
        addCNEN("gtocore.machine.large_combustion_engine.Joint_boosted", "§b联合促燃中", "§bJoint Combustion Boosted");
        addCNEN("gtocore.machine.large_combustion_engine.supply_dinitrogen_tetroxide_to_boost", "提供四氧化二氮来联合促燃", "Provide Dinitrogen Tetroxide to joint combustion");
        addCNEN("gtocore.machine.large_steam_circuit_assembler.circuit", "已铭刻电路: %s", "Inscribed Circuit: %s");
        addCNEN("gtocore.machine.large_steam_circuit_assembler.engrave_circuit", "铭刻电路", "Engrave Circuit");
        addCNEN("gtocore.machine.lightning_rod.tooltip.0", "上方避雷针被雷击后产生大量能量", "Large amounts of energy are generated after the lightning rod above is struck");
        addCNEN("gtocore.machine.lightning_rod.tooltip.1", "每0.5秒只能产生一次能量，且有一定几率破坏上方避雷针", "Can only generate energy once every 0.5 seconds, with a chance to damage the lightning rod above");
        addCNEN("gtocore.machine.lightning_rod.tooltip.2", "如果存储能量已满，机器将会爆炸", "If the stored energy is full, the machine will explode");
        addCNEN("gtocore.machine.mega_turbine.high_speed_mode", "高速模式:", "High Speed Mode:");
        addCNEN("gtocore.machine.module", "已安装的模块数量: %s", "Amount of Installed Modules: %s");
        addCNEN("gtocore.machine.module.base", "基础模块数量: %s", "Amount of Base Modules: %s");
        addCNEN("gtocore.machine.module.mega", "大型模块数量: %s", "Amount of Mega Modules: %s");
        addCNEN("gtocore.machine.module.am", "已安装的模块数: %s", "Amount of Installed Modules: %s");
        addCNEN("gtocore.machine.module.have", "该模块已成功安装", "This module has been successfully installed");
        addCNEN("gtocore.machine.module.null", "该模块未成功安装", "This module has not been successfully installed");
        addCNEN("gtocore.machine.nanites_integrated_processing_center.not_connected", "未连接到任何模块", "Not connected to any module");
        addCNEN("gtocore.machine.nanites_module.connected", "已连接到纳米集成加工中心", "Connected to Nanites Integrated Processing Center");
        addCNEN("gtocore.machine.nanites_module.not_connected", "未连接到纳米集成加工中心", "Not connected to Nanites Integrated Processing Center");
        addCNEN("gtocore.machine.multiple_recipes.tooltip", "支持跨配方并行", "Supports cross recipe parallel processing");
        addCNEN("gtocore.machine.neutron_activator.efficiency", "动能消耗乘数: %s", "Kinetic Energy Consumption Multiplier: %s");
        addCNEN("gtocore.machine.neutron_activator.ev", "当前中子动能: %seV", "Current Neutron Kinetic Energy: %seV");
        addCNEN("gtocore.machine.height", "高度: %s", "Height: %s");
        addCNEN("gtocore.machine.sensor.invert.disabled", "红石输出: 普通", "Redstone Output: Normal");
        addCNEN("gtocore.machine.sensor.invert.enabled", "红石输出: 反转", "Redstone Output: Inverted");
        addCNEN("gtocore.machine.oc_amount", "超频次数: %s", "Overclocking Times: %s");
        addCNEN("gtocore.machine.off", "关闭", "Off");
        addCNEN("gtocore.machine.on", "打开", "On");
        addCNEN("gtocore.machine.muffler.config", "设置消声仓运行时产灰被阻止的概率", "Set the probability of ash being blocked when the muffler hatch is running");
        addCNEN("gtocore.machine.muffler.config.desc", "每次产灰判定时有 %s%% 的概率被取消", "There is a %s%% chance of being canceled each time ash is produced");
        addCNEN("gtocore.machine.pattern.error.tier", "§c必须使用同种等级方块§r", "§cMust use blocks of the same tier§r");
        addCNEN("gtocore.machine.primitive_magic_energy.tooltip.0", "无尽地吸收机器上方末地水晶的能量，如果能量已满，机器将会爆炸", "Endlessly absorbs the energy from ender crystals above the machine, if the energy capacity is full, the machine will explode");
        addCNEN("gtocore.machine.primitive_magic_energy.tooltip.1", "每秒需输入同电压x电流的魔力，否则爆炸", "Requires a constant mana input of [X] EU/s (Voltage × Current). Failure cause an explosion");
        addCNEN("gtocore.machine.processing_plant.mismatched", "小机器电压与能源仓电压不匹配", "Small machine tier mismatch with Energy Hatch");
        addCNEN("gtocore.machine.radiation_hatch.inhibition_dose", "抑制量: %s Sv", "Inhibition Amount: %s Sv");
        addCNEN("gtocore.machine.radiation_hatch.time", "时间: %s / %s Tick", "Time: %s / %s Tick");
        addCNEN("gtocore.machine.simple_spacestation.distilled_water", "向外供给蒸馏水： %s mB 每秒·每仓", "Distilled Water Output: %s mB per second per hatch");
        addCNEN("gtocore.machine.slaughterhouse.is_spawn", "实体生成模式: ", "Entity Generation: ");
        addCNEN("gtocore.machine.slaughterhouse.active_weapon", "使用§6%d§r击杀生物", "Kill the creature using §6%d§r");
        addCNEN("gtocore.machine.slaughterhouse.filter_nbt", "舍弃带有NBT的物品", "Do not keep items with NBT");
        addCNEN("gtocore.machine.space_elevator.set_out", "启程", "Set Off");
        addCNEN("gtocore.machine.space_elevator.connected", "已连接正在运行的太空电梯", "Connected To A Running SpaceElevator");
        addCNEN("gtocore.machine.space_elevator.not_connected", "未连接正在运行的太空电梯", "Not Connected To A Running SpaceElevator");
        addCNEN("gtocore.machine.steam_parallel_machine.modification_oc", "修改超频次数: ", "Modify Overclocking Times: ");
        addCNEN("gtocore.machine.steam_parallel_machine.oc", "每次超频会使处理时间减半，但使蒸汽消耗增至四倍。", "Each overclock halves the processing time but quadruples the steam consumption");
        addCNEN("gtocore.machine.total_time", "连续运行时间: %s Tick", "Continuous Running Time: %s Tick");
        addCNEN("gtocore.machine.uev_fusion_reactor.description", "核聚变反应堆MK-V是台大型多方块结构，用于融合元素形成更重的元素。它仅可使用UEV等级的能源仓。每个能源仓可增加160MEU的能量缓存，最大能量缓存为2560MEU。", "The Fusion Reactor MK V is a large multiblock structure used for fusing elements into heavier ones. It can only use UEV Energy Hatches. For every Hatch it has, its buffer increases by 160M EU, and has a maximum of 2560M.");
        addCNEN("gtocore.machine.uhv_fusion_reactor.description", "核聚变反应堆MK-IV是台大型多方块结构，用于融合元素形成更重的元素。它仅可使用UHV等级的能源仓。每个能源仓可增加80MEU的能量缓存，最大能量缓存为1280MEU。", "The Fusion Reactor MK-IV is a large multiblock structure used for fusing elements into heavier ones. It can only use UHV Energy Hatches. For every Hatch it has, its buffer increases by 80M EU, and has a maximum of 1280M.");
        addCNEN("gtocore.machine.wind_mill_turbine.actualPower", "能量输出: %s", "Energy Output: %s");
        addCNEN("gtocore.machine.wind_mill_turbine.tooltip.0", "风速低于转子最小值无法工作，高于最大值转子将快速损坏", "The rotor cannot operate below the minimum wind speed, and it will be damaged quickly above the maximum wind speed");
        addCNEN("gtocore.machine.wind_mill_turbine.tooltip.1", "风速加成: 雨天x1.5，雷雨天x2，风速决定最高转速", "Wind Speed Bonus: x1.5 for rainy days, x2 for thunderstorms, the wind speed determines the maximum rotation speed");
        addCNEN("gtocore.machine.wind_mill_turbine.wind", "当前风力: %s", "Current Wind Speed: %s");
        addCNEN("gtocore.machine.water_purification_plant.bind", "已绑定机器:", "Bound Machine:");
        addCNEN("gtocore.machine.water_purification_unit.success_chance", "成功率: %s%%", "Success Chance: %s%%");
        addCNEN("gtocore.machine.absolute_baryonic_perfection_purification_unit.items", "本次循环组合:\n%s, %s", "Current combination for this cycle:\n%s, %s");
        addCNEN("gtocore.machine.residual_decontaminant_degasser_purification_unit.fluids", "本次循环需求:\n%s", "Current cycle requirements:\n%s");
        addCNEN("gtocore.machine.wireless_data_hatch.bind", "无线数据仓绑定完成", "Wireless data hatch binding completed");
        addCNEN("gtocore.machine.wireless_data_transmitter_hatch.tooltip.0", "需要使用闪存右键无线光学数据靶仓和无线数据源仓进行绑定", "Use the flash drive to right-click and bind the wireless optical data target hatch and the wireless data source hatch");
        addCNEN("gtocore.machine.wireless_data_transmitter_hatch.to_bind", "源仓数据读取完成，请右键靶仓进行绑定", "Source hatch data reading completed, please right-click the target hatch to bind");
        addCNEN("gtocore.machine.wireless_data_transmitter_hatch.bind", "已绑定无线光学数据靶仓(%s)", "Bound wireless optical data target hatch (%s)");
        addCNEN("gtocore.machine.wireless_data_transmitter_hatch.unbind", "未绑定无线光学数据靶仓", "Wireless optical data target hatch not bound");
        addCNEN("gtocore.machine.wireless_data_receiver_hatch.tooltip.0", "需要使用闪存右键无线光学数据靶仓和无线光学数据源仓进行绑定", "Use the flash drive to right-click and bind the wireless optical data target hatch and the wireless optical data source hatch");
        addCNEN("gtocore.machine.wireless_data_receiver_hatch.to_bind", "靶仓数据读取完成，请右键源仓进行绑定", "Target hatch data reading completed, please right-click the source hatch to bind");
        addCNEN("gtocore.machine.wireless_data_receiver_hatch.bind", "已绑定无线光学数据源仓(%s)", "Bound wireless optical data source hatch (%s)");
        addCNEN("gtocore.machine.wireless_data_receiver_hatch.unbind", "未绑定无线光学数据源仓", "Wireless optical data source hatch not bound");
        addCNEN("gtocore.machine.pattern_buffer_proxy.tooltip.0", "手持闪存潜行右键§6ME样板总成§f，然后右键§6ME样板总成镜像§f绑定。", "Hold a flash drive and sneak-right-click the §6ME Pattern Buffer§f, then right-click the §6ME Pattern Buffer Proxy§f to bind them.");
        addCNEN("gtocore.machine.need", "需要: %s", "Need: %s");
        addCNEN("gtocore.machine.advanced_infinite_driller.not_fluid_head", "无钻头", "No drill head");
        addCNEN("gtocore.machine.advanced_infinite_driller.heat", "最大温度: %s K / 工作温度: %s K", "Max Temperature: %s K / Operating Temperature: %s K");
        addCNEN("gtocore.machine.current_temperature", "当前温度: %s K", "Current Temperature: %s K");
        addCNEN("gtocore.machine.thermometer.ambient", "环境温度：%s K", "Ambient Temperature: %s K");
        addCNEN("gtocore.machine.thermometer.conductivity", "导热率：%s HU/(t·K)", "Thermal Conductivity: %s HU/(t·K)");
        addCNEN("gtocore.machine.thermometer.cooldown", "自然散热系数：%s HU/(t·√K)", "Natural Cooling Coefficient: %s HU/(t·√K)");
        addCNEN("gtocore.machine.thermometer.change", "实时温变：%s K/s（最近 1 秒）", "Temperature Change: %s K/s (last second)");
        addCNEN("gtocore.machine.thermometer.heat", "当前热量：%s HU", "Stored Heat: %s HU");
        addCNEN("gtocore.machine.thermometer.scale", "刻度范围：0 ～ %s K", "Scale: 0–%s K");
        addCNEN("gtocore.machine.thermometer.explosion", "温度上限（红线）：%s K", "Explosion Threshold (red line): %s K");
        addCNEN("gtocore.machine.neutron_flux", "当前中子通量: %s keV", "Current Neutron Flux: %s keV");
        addCNEN("gtocore.machine.temp.per_second", "配方每秒升温: %s K", "Recipe Temperature Increase Per Second: %s K");
        addCNEN("gtocore.recipe.neutron_flux.k", "最小中子通量: %s keV", "Minimum Neutron Flux: %s keV");
        addCNEN("gtocore.recipe.neutron_flux.m", "最小中子通量: %s MeV", "Minimum Neutron Flux: %s MeV");
        addCNEN("gtocore.recipe.neutron_flux.change", "每秒中子通量变化: %s keV", "Neutron Flux Change Per Second: %s keV");
        addCNEN("gtocore.recipe.heat.change", "配方基础产热: %sK/s", "Recipe Base Heat Production: %sK/s");
        addCNEN("gtocore.recipe.fuelcell.converted_energy", "可转换的基础能量: %s", "Convertible Base Energy: %s");
        addCNEN("gtocore.machine.advanced_infinite_driller.drilled_fluid", "流体: %s 产量: %s", "Fluid: %s Output: %s");
        addCNEN("gtocore.machine.steam.tooltip.1", "默认支持%s等级及以下的配方处理", "default can process %s Tier recipes and below, processing time is 1.5 times");
        addCNEN("gtocore.machine.steam.tooltip.2", "安装大型蒸汽输入仓后提升一个配方等级，并解锁超频功能", "After installing a large steam input hatch, upgrade one recipe tier and unlock the overclocking function");
        addCNEN("gtocore.machine.mana_stored", "魔力总量: %s", "Total Mana: %s");
        addCNEN("gtocore.machine.mana_consumption", "最大魔力消耗: %s", "Max Mana Consumption Rate: %s");
        addCNEN("gtocore.machine.mana_production", "最大魔力产出: %s", "Max Mana Production Rate: %s");
        addCNEN("gtocore.machine.mana_input", "魔力输入: %s", "Mana input: %s");
        addCNEN("gtocore.machine.mana_output", "魔力输出: %s", "Mana output: %s");
        addCNEN("gtocore.recipe.info.mana_usage", "魔力消耗", "Mana Usage");
        addCNEN("gtocore.recipe.info.mana_generation", "魔力产出", "Mana Generation");
        addCNEN("gtocore.recipe.info.total_mana", "魔力总计", "Total Mana");
        addCNEN("gtocore.recipe.info.mana_overclock", "最低魔力等级为%s；每提高一级，耗时 ÷4，魔力消耗 ×4", "Minimum mana tier: %s. Each tier above divides duration by 4 and multiplies mana usage by 4");
        for (int tier = 0; tier < GTOValues.MANA.length; tier++) {
            addCNEN("gtocore.recipe.info.mana_tier." + tier, GTOValues.MANACN[tier], GTOValues.MANAN[tier]);
        }
        addCNEN("gtocore.machine.mana_eu", "支持电力配方，转换比1:1", "Supports EU recipes, conversion ratio 1:1");
        addCNEN("gtocore.machine.processing_array.tooltip.0", "玻璃等级限制了内部机器等级", "Tier is limited by glass grade");
        addCNEN("gtocore.machine.processing_array.tooltip.1", "并行数由内部机器数量决定", "Parallel are determined by the amount of internal machines");
        addCNEN("gtocore.machine.maximum_amount", "最大数量: %s", "Maximum amount: %s");
        addCNEN("gtocore.machine.binding_amount", "绑定数量: %s", "Binding amount: %s");
        addCNEN("gtocore.machine.recipe.run", "运行%s配方时: ", "When running the %s recipe: ");
        addCNEN("gtocore.machine.parallel", "最大并行数: %s", "Maximum number of parallel: %s");
        addCNEN("gtocore.machine.processing_plant.parallel_per_tier_tooltip", "自ULV起，配方等级每高出1级，获得的并行数+%s", "From ULV, each voltage tier increases the obtained parallelism by %s");
        addCNEN("gtocore.machine.processing_plant.parallel_per_tier_formula", "公式：%s * (tier - 0)", "Formula: %s * (tier - 0)");
        addCNEN("gtocore.machine.air_scrubber.ash_chance", "掏灰概率：%s%%", "Ash extraction chance: %s%%");
        addCNEN("gtocore.machine.air_scrubber.range", "工作半径：%s格", "Working radius: %s blocks");
        addCNEN("gtocore.machine.cwut_modification", "算力修正系数: %s", "Hashrate correction factor: %s");
        addCNEN("gtocore.machine.components_list", "组件列表: ", "Components List: ");
        addCNEN("gtocore.machine.tag_filter.tooltip.0", "* 表示通配符 () 表示优先", "* Indicates a wildcard () Indicates Priority");
        addCNEN("gtocore.machine.tag_filter.tooltip.1", "& = 逻辑与 | = 逻辑或 ^ = 逻辑异或", "& = Logic with | = Logic or ^ = Logical XOR");
        addCNEN("gtocore.machine.me_dual_hatch_stock.turns.0", "自动拉取关", "Auto-Pull Disable");
        addCNEN("gtocore.machine.me_dual_hatch_stock.turns.1", "自动拉取流体/物品", "Auto-Pull Fluid or Item");
        addCNEN("gtocore.machine.me_dual_hatch_stock.turns.2", "仅拉取物品", "Auto-Pull Item");
        addCNEN("gtocore.machine.me_dual_hatch_stock.turns.3", "仅拉取流体", "Auto-Pull Fluid");
        addCNEN("gtocore.machine.me_dual_hatch_stock.tooltip.0", "可标记64种流体或物品", "Keeps 64 fluid or item types in stock");
        addCNEN("gtocore.machine.me_dual_hatch_stock.tooltip.1", "直接从ME网络抽取流体或物品", "Retrieves fluids or item directly from the ME network");
        addCNEN("gtocore.machine.me_dual_hatch_stock.data_stick.name", "§oME库存输入总成配置数据", "§oME Stock Input Dual Hatch Config Data");
        addCNEN("gtocore.machine.scanning", "扫描中...", "Scanning...");
        addCNEN("gtocore.machine.analysis", "分析中...", "Analysing...");
        addCNEN("gtocore.machine.assembling", "装配中...", "Assembling...");
        addCNEN("gtocore.machine.wireless_mode", "无线模式", "Wireless Mode");
        addCNEN("gtocore.machine.alchemical.chance_can_be_boosted", "该配方的概率会随运行次数提升", "The chance of this recipe increases with the amount of attempts");
        addCNEN("gtocore.machine.alchemical_device.1", "嬗变中", "Transmutation");
        addCNEN("gtocore.machine.alchemical_device.2", "完美嬗变中", "Perfect Transmutation");
        addCNEN("gtocore.machine.monitor.no_information", "没有可显示的信息", "No information to display");
        addCNEN("gtocore.machine.monitor.mana.current", "当前网络魔力: ", "Current Network Mana: ");
        addCNEN("gtocore.machine.monitor.mana.pool.0", "(约", "(Approx. ");
        addCNEN("gtocore.machine.monitor.mana.pool.1", "池)", "Pools)");
        addCNEN("gtocore.machine.monitor.mana.pool.2", "池/s)", "Pools per Second)");
        addCNEN("gtocore.machine.monitor.mana.increase", "每秒魔力增长: ", "Mana Growth Per Second: ");
        addCNEN("gtocore.machine.monitor.mana.decrease", "每秒魔力消耗: ", "Mana Consumption Per Second: ");
        addCNEN("gtocore.machine.monitor.cwu.capacity", "当前网络可请求算力: %s CWU", "Current Network Requestable Hashrate: %s CWU");
        addCNEN("gtocore.machine.monitor.cwu.used", "（平均使用算力: %s CWU）", "(Average Used Hashrate: %s CWU)");
        addCNEN("gtocore.machine.monitor.eu.fullness", "能量塔能量存量: %s%%", "Energy Tower Energy Storage: %s%%");
        addCNEN("gtocore.machine.monitor.priority", "调整显示的优先级", "Adjust the display priority");
        addCNEN("gtocore.machine.monitor.adjust_component.move_up", "上移", "Move Up");
        addCNEN("gtocore.machine.monitor.adjust_component.move_down", "下移", "Move Down");
        addCNEN("gtocore.machine.monitor.adjust_component.switch", "切换显示", "Toggle Display");
        addCNEN("gtocore.machine.monitor.adjust_component.reset", "清除显示顺序配置", "Clear Display Order Configuration");
        addCNEN("gtocore.machine.monitor.custom_info.tooltip", "自定义信息", "Custom Information");
        addCNEN("gtocore.machine.monitor.custom_info.code_input_tooltip.1", "复制§a格式化代码§r到剪贴板", "Copy §aformatting code§r to clipboard");
        addCNEN("gtocore.machine.monitor.custom_info.code_input_tooltip.2", "§l格式化代码（Formatting code）§r，又称§l颜色代码（Color code）§r，能使在游戏中加入含颜色和格式信息。",
                "§lFormatting code§r, also known as §lcolor code§r, allows adding color and formatting information in the game.");
        addCNEN("gtocore.machine.monitor.custom_info.code_input_tooltip.3", "请前往§dMinecraft Wiki§r了解更多关于格式化代码的信息。",
                "Please visit the §dMinecraft Wiki§r for more information about formatting codes.");
        addCNEN("gtocore.machine.monitor.ae.status.no_grid", "警告: 无法连接到AE网络！", "Warning: Unable to connect to AE network!");
        addCNEN("gtocore.machine.monitor.ae.status.no_config", "无配置信息", "No configuration information");
        addCNEN("gtocore.machine.monitor.ae.status.0", "正在监视的物品: %s", "Monitored Items: %s");
        addCNEN("gtocore.machine.monitor.ae.status.1", "正在监视的流体: %s", "Monitored Fluids: %s");
        addCNEN("gtocore.machine.monitor.ae.amount", "总量统计: %s", "Total Amount: %s");
        addCNEN("gtocore.machine.monitor.ae.stat.title", "平均吞吐量统计", "Average Throughput Statistics");
        addCNEN("gtocore.machine.monitor.ae.stat.minute", "近一分钟: %s", "Last Minute: %s");
        addCNEN("gtocore.machine.monitor.ae.stat.hour", "近一小时: %s", "Last Hour: %s");
        addCNEN("gtocore.machine.monitor.ae.stat.day", "近一天: %s", "Last Day: %s");
        addCNEN("gtocore.machine.monitor.ae.stat.remaining_time", "预计耗尽时间: %s", "Estimated Depletion Time: %s");
        addCNEN("gtocore.machine.monitor.ae.set_filter", "设置过滤器", "Set Filter");
        addCNEN("gtocore.machine.basic_monitor.tooltip.1", "将多个监控器连接在一起，形成一个多功能监控器", "Connect multiple monitors together to form a multifunctional monitor");
        addCNEN("gtocore.machine.basic_monitor.tooltip.2", "在连接的监控器中添加§b组件§r来显示不同的信息", "Add §bcomponents§r in connected monitors to display different information");
        addCNEN("gtocore.machine.boiler.tooltip.warning", "§c警告§r: 请勿在锅炉内积攒过多的蒸汽！", "§cWarning: §rDo not accumulate too much steam in the boiler!");
        addCNEN("gtocore.machine.monitor.ae.cpu.usage", "正在工作的CPU: %s 个（总数:  %s 个）", "Working CPUs: %s (Total: %s)");
        addCNEN("gtocore.machine.monitor.ae.cpu.monitored", "正在监视的CPU: #%s - %s", "Monitored CPU: #%s - %s");
        addCNEN("gtocore.machine.monitor.ae.cpu.list", "CPU列表", "CPU List");
        addCNEN("gtocore.machine.monitor.ae.cpu.set.1", "设置监视的CPU", "Set Monitored CPU");
        addCNEN("gtocore.machine.monitor.ae.cpu.set.2", "§c警告§r: 网络中CPU变动时，需要重新选择！", "Warning: When the CPU changes in the network, you need to reselect!");
        addCNEN("gtocore.machine.machine_monitor.slot", "请放入机器坐标信息卡", "Insert Machine Coordinate Card");
        addCNEN("gtocore.part.extendae.tag_filter.whitelist.tooltip", "可放入目标，选择白名单的标签过滤器", "Can place target, select whitelist tag filter");
        addCNEN("gtocore.part.extendae.tag_filter.blacklist.tooltip", "可放入目标，选择黑名单的标签过滤器", "Can place target, select blacklist tag filter");
        addCNEN("gtocore.machine.area_destruction_tools.detonate_instruction", "起爆", "Detonate instruction");
        addCNEN("gtocore.machine.area_destruction_tools.missing_items", "缺失物品", "Missing items");
        addCNEN("gtocore.machine.area_destruction_tools.model.0", "模式: 空", "Model: Empty");
        addCNEN("gtocore.machine.area_destruction_tools.model.1", "模式: 球", "Model: Sphere");
        addCNEN("gtocore.machine.area_destruction_tools.model.2", "模式: 圆柱", "Model: Cylinder");
        addCNEN("gtocore.machine.area_destruction_tools.model.3", "模式: 区块", "Model: Chunk");
        addCNEN("gtocore.machine.area_destruction_tools.model.4", "模式: 指定区域", "Model: Designated Area");
        addCNEN("gtocore.machine.area_destruction_tools.explosive_yield", "爆炸当量: %s", "Explosive Yield: %s");
        addCNEN("gtocore.machine.large_steam_solar_boiler.size", "尺寸: %s × %s", "Size: %s × %s");
        addCNEN("gtocore.machine.large_steam_solar_boiler.heat_collector_pipe", "有效集热管数量: %s", "Amount of effective Heat collector pipe: %s");
        addCNEN("gtocore.machine.large_steam_solar_boiler.steam_production", "蒸汽产量: %s / s", "Steam production: %s / s");
        addCNEN("gtocore.machine.model", "模式: %s", "Model: %s");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.0", "工作模式: 未设定", "Working Mode: Not set");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.1", "工作模式: 物品解构", "Working Mode: Item Deconstruction");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.2", "工作模式: 物品 + 附魔 解构", "Working Mode: Item + Enchantments Deconstruction");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.3", "工作模式: 物品 + 刻印 解构", "Working Mode: Item + Affixes Deconstruction");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.4", "工作模式: 物品 + 附魔 + 刻印 解构", "Working Mode: Item + Enchantments + Affixes Deconstruction");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.5", "工作模式: 附魔精粹合成附魔书", "Working Mode: Essence to craft Enchanted Book");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.6", "工作模式: 附魔书合并", "Working Mode: Enchantment Enchanted Book Merge");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.7", "工作模式: 刻印精粹合成铭刻之布", "Working Mode: Affix to craft Enchanted Book");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.8", "工作模式: 宝石合并", "Working Mode: Gem Merge");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.9", "工作模式: 宝石粉碎", "Working Mode: Gem Crushing");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.10", "工作模式: 强行附魔", "Working Mode: Forced enchantment");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.11", "工作模式: 强行刻印", "Working Mode: Forced add affixes");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.12", "工作模式: 强行修改物品稀有度", "Working Mode: Forcefully modify item rarity");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.13", "工作模式: 强行添加镶孔", "Working Mode: Forced addition of sockets");
        addCNEN("gtocore.machine.the_primordial_reconstructor.mode.14", "工作模式: 强行镶嵌宝石", "Working Mode: Forced gem inlay");
        addCNEN("gtceu.machine.me.import_part.data_stick.name", "§o %s配置数据", "§o %s Configuration Data");
        addCNEN("gtocore.compound_extreme_cooling_unit.tooltips.combined", "选择等离子冷凝模式以启用", "Select plasma condensation mode to enable");
        addCNEN("gtocore.machine.spacestation.ready", "空间站准备： %s%%就绪", "Space Station Readiness: %s%% Ready");
        addCNEN("gtocore.machine.spacestation.module_count", "连接的舱室模块数量： %s", "Amount of Connected Module Hatches: %s");
        addCNEN("gtocore.machine.spacestation.energy_consumption.total", "所有空间站舱室总能量消耗： %s", "Total Energy Consumption of All Space Station Modules: %s");
        addCNEN("gtocore.machine.spacestation.require_module", "当前空间站缺少必要的舱室模块： %s", "The current space station is missing the required module: %s");
        addCNEN("gtocore.machine.space_shield_hatch.info", "机器太空护盾：运转正常", "Machine Space Shield: Operating Normally");
        addCNEN("gtocore.machine.space_shield_hatch.insufficient", "机器太空护盾：无激光供应", "Machine Space Shield: No Laser Supply");
        addCNEN("gtocore.machine.space_shield_hatch.not_in_space", "机器太空护盾：不在太空中", "Machine Space Shield: Not in Space");
        addCNEN("gtocore.machine.tooltips.items_are_hidden", "（剩余%s项已隐藏）", "（%s items hidden）");
        addCNEN("gtocore.machine.tooltip.upgrade_action", "Shift + 右键点击%s，可升级内部等级较低的%s", "Shift + Right-click %s to upgrade lower-tier %s");
        addCNEN("gtocore.lang.space_elevator_connector_module.3", "会增加太空电梯%s%%的算力消耗", "Increases the space elevator's Computational Workload consumption by %s%%");
        addCNEN("gtocore.lang.space_elevator_engineering_data_module.2", "将每收集的%s次运行数据，转化为1点星际工程研究点数", "Converts every %s collected operation data into 1 point of interstellar engineering research points");
        addCNEN("gtocore.lang.energy_data_holder.2", "在机器正常连续运行%s秒后，此仓才会开始收集数据", "After the machine has been running continuously for %s seconds, this hatch will start collecting data");
        addCNEN("gtocore.machine.raybeam_generator.tooltip.0", "在机器GUI内调节光束的发射角度和发射功率", "Adjust the emission angle and emission power of the beam in the machine GUI");

        addCNEN("gtocore.machine.industrial_platform_deployment_tools.title.0", "简介", "Introduction");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.title.1", "选择预设", "Select Preset");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.title.2", "确认耗材", "Confirm consumables");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.title.3", "调整设置", "Adjust settings");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.choose_this", "选这个: ", "Choose this: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.unselected", "未选择...", "Unselected...");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.selected", "已选择: %s - %s", "Selected: %s - %s");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.preview", "显示预览", "Show Preview");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.highlight", "高亮区域", "Highlight area");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.source", "来源: %s", "Source: %s");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.text.size", "大小: (%s %s %s) (%s %s)", "Size: (%s %s %s) (%s %s)");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.reserves", "储量: ", "Reserves: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.demand", "需求: ", "Demand: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.0", "基础: ", "Basic: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.1", "扩展: ", "Extended: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.2", "特种: ", "Specialized: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.extra_demand", "额外需求: ", "Additional Demand: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.loading", "§3[ 材料装载 ]§r", "§3[ Material Loading ]§r");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.unloading", "§3[ 材料卸载 ]§r", "§3[ Material Unloading ]§r");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.adequate", "材料充足", "Material adequate");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.material.insufficient", "材料不足", "Material insufficient");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.offset", "X Y Z方向的偏移(X Z方向单位为区块，Y方块单位为块)", "X, Y, Z offsets (X Z units are blocks, Y units are blocks)");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.offset.x", "X: %s", "X: %s");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.offset.z", "Z: %s", "Z: %s");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.offset.y", "Y: %s", "Y: %s");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.boundary", "边界范围: ", "Boundary Range: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.breakBlocks", "破坏方块: ", "Break Blocks: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.skipAir", "跳过空气: ", "Skip Air: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.updateLight", "光照更新: ", "Update Light: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.speed", "速度: ", "Speed: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.xMirror", "X轴对称: ", "X-axis symmetry: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.zMirror", "Z轴对称: ", "Z-axis symmetry: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.rotation", "Y轴旋转: ", "Y-axis rotation: ");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.start", "[开始]", "[Start]");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.doing", "[运行中.%s]", "[In Progress.%s]");
        addCNEN("gtocore.machine.village_trading_station.help.core", "核心功能", "Core Functions");
        addCNEN("gtocore.machine.village_trading_station.help.core.1", "作为优化政企村协作的智能工具，其核心优势在于‘无缝对接+自主协同’", "As an intelligent tool for optimizing enterprise-village collaboration, its core strengths lie in 'seamless integration + autonomous coordination'.");
        addCNEN("gtocore.machine.village_trading_station.help.core.2", "支持9名村民信息录入与锁定，无需工作方块，由交易站系统按设定周期自动执行补货（重置交易次数）与交易操作", "Supports information input and locking for 9 villagers; no work blocks needed. The trading station system automatically executes restocks (resetting trade counts) and transactions at set intervals.");
        addCNEN("gtocore.machine.village_trading_station.help.core.3", "配备自动输出功能，可将交易成果自动输送至指定方向的外部容器（如储物箱）", "Equipped with auto-output functionality to automatically transfer trade results to external containers (e.g., chests) in the specified direction.");
        addCNEN("gtocore.machine.village_trading_station.help.ui", "界面说明", "Interface Explanation");
        addCNEN("gtocore.machine.village_trading_station.help.ui.1", "一：主页 - 展示机器基础信息与功能介绍", "1. Home Page - Displays basic machine information and function descriptions.");
        addCNEN("gtocore.machine.village_trading_station.help.ui.2", "二：交易控制页 - 核心操作界面，包含9个村民槽位，支持村民放置/锁定、配方切换、交易启停等核心操作", "2. Trade Control Page - Core interface with 9 villager slots, supporting key operations like villager placement/locking, recipe switching, and trade activation/deactivation.");
        addCNEN("gtocore.machine.village_trading_station.help.ui.3", "三：升级控制页 - 包含增强槽与升级槽：增强槽调控全局补货间隔与交易乘数，升级槽可提升村民单次交易上限", "3. Upgrade Control Page - Features enhancement and upgrade slots: enhancement slots adjust global restock intervals and trade multiples; upgrade slots increase villagers' single-trade limits.");
        addCNEN("gtocore.machine.village_trading_station.help.ui.4", "四：方向配置页 - 可设置输出物品的方向，灵活适配基地物流布局", "4. Direction Configuration Page - Configures item output direction to flexibly adapt to base logistics layouts.");
        addCNEN("gtocore.machine.village_trading_station.help.steps", "操作流程", "Operation Process");
        addCNEN("gtocore.machine.village_trading_station.help.steps.1", "第一步：打开机器界面（IFancyUIMachine），默认显示交易控制页", "Step 1: Open the machine interface (IFancyUIMachine), which defaults to the Trade Control Page.");
        addCNEN("gtocore.machine.village_trading_station.help.steps.2", "第二步：在村民槽位放入村民（最多9名），点击锁定按钮录入其交易配方信息", "Step 2: Place villagers in the slots (max 9) and click the lock button to input their trade recipe data.");
        addCNEN("gtocore.machine.village_trading_station.help.steps.3", "第三步：通过切换按钮选定目标交易配方，点击“启动交易”按钮激活自动交易流程", "Step 3: Select target trade recipes using the switch button, then click 'Start Trading' to activate the automatic transaction process.");
        addCNEN("gtocore.machine.village_trading_station.help.steps.4", "第四步：向输入仓库投放交易所需物品（如绿宝石），系统将按周期自动执行交易", "Step 4: Place required trading items (e.g., emeralds) into the input warehouse; the system will automatically execute trades at set intervals.");
        addCNEN("gtocore.machine.village_trading_station.help.steps.5", "第五步：按需在升级控制页放入增强物品（优化效率）或升级物品（提升交易上限），交易成果可从输出仓库直接提取或通过自动输出转移", "Step 5: Place enhancement items (to optimize efficiency) or upgrade items (to increase trade limits) in the Upgrade Control Page as needed. Trade results can be directly retrieved from the output warehouse or transferred via auto-output.");
        addCNEN("gtocore.machine.village_trading_station.help.upgrade", "升级参数", "Upgrade Parameters");
        addCNEN("gtocore.machine.village_trading_station.help.upgrade.1", "增强模块（增强槽）：随等级提升逐步缩短补货间隔（最低可至600游戏刻），高等级可解锁多倍交易（最高4倍）", "Enhancement modules (enhancement slot): Gradually shorten restock intervals with higher levels (minimum 600 game ticks); high levels unlock bulk trading (maximum 4x).");
        addCNEN("gtocore.machine.village_trading_station.help.upgrade.2", "升级模块（升级槽）：通过能量发生器提升村民单次交易上限，最高可扩展至256次交易额度", "Upgrade modules (upgrade slot): Increase villagers' single-trade limits via field generators, with a maximum expansion to 256 uses per trade.");
        addCNEN("gtocore.machine.village_trading_station.help.material", "材料说明", "Material Instructions");
        addCNEN("gtocore.machine.village_trading_station.help.material.1", "增强需消耗对应等级的发射器或集成控制核心，升级需消耗不同电压的能量发生器", "Enhancements require emitters or integrated control cores of corresponding tiers; upgrades require field generators of various voltage ratings.");
        addCNEN("gtocore.machine.village_trading_station.help.material.2", "交易物品需放入共享输入仓库，系统会自动匹配各村民的需求；输出物品统一存入共享输出仓库", "Trading items must be placed in the shared input warehouse—the system automatically matches each villager's requirements. Output items are uniformly stored in the shared output warehouse.");
        addCNEN("gtocore.machine.village_trading_station.help.notes", "注意事项", "Cautions");
        addCNEN("gtocore.machine.village_trading_station.help.notes.1", "村民信息锁定后，需先解锁才能更换，解锁会清空该村民的交易配置及已加载的配方数据", "Once villager information is locked, unlocking is required for replacement—this will clear the villager's trade configuration and loaded recipe data.");
        addCNEN("gtocore.machine.village_trading_station.help.notes.2", "输出仓库满载时，对应交易将自动暂停；启用自动输出功能可减少此问题，清空后需确保交易处于启动状态以恢复运行", "Corresponding trades will automatically pause when the output warehouse is full. Enabling auto-output reduces this issue; after clearing, ensure trades are active to resume operation.");
        addCNEN("gtocore.machine.village_trading_station.help.notes.3", "交易乘数会等比例增加输入物品消耗与输出物品数量（如乘数为2时，消耗2倍输入并获得2倍输出）", "Bulk trade coefficients proportionally increase input consumption and output quantity (e.g., 2x trading requires 2x input and yields 2x output).");
        addCNEN("gtocore.machine.village_trading_station.help.notes.4", "按流程完成配置后，锁定村民并启用交易即可自动运行，系统会按设定周期执行补货与交易", "After configuration as per the process, lock villagers and enable trading to start automatic operation—the system will execute restocks and trades at set intervals.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.core", "核心功能", "Core Functions");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.core.1", "本工具通过解析预设蓝图数据，可快速生成各类标准化工业平台结构", "This tool rapidly generates various standardized industrial platform structures by parsing preset blueprint data.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.core.2", "既能快速搭建矿石分拣站等基础生产节点，也可扩展为包含精炼车间、仓储中心的复杂工业集群", "It can both quickly build basic production nodes like ore sorting stations and expand into complex industrial clusters with refining workshops and storage centers.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.ui", "界面说明", "Interface Explanation");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.ui.1", "一：蓝图选择页 - 用于浏览/筛选预设蓝图、预览结构效果、查看材料清单及调整部署参数", "1. Blueprint Selection Page - For browsing/filtering presets, previewing structures, viewing material lists, and adjusting deployment parameters");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.ui.2", "二：控制模块 - 位于界面右上角，集成翻页导航、部署启动按钮及实时进度监控条", "2. Control Module - Located at the top right, integrating page navigation, deployment start button, and real-time progress bar");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.ui.3", "三：材料槽区 - 含27格存储位，前9格为GUI直接访问区，用于加载部署所需工业组件", "3. Material Slot Area - 27 storage slots total, with first 9 GUI-accessible for loading deployment-required industrial components");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps", "操作流程", "Operation Process");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps.1", "第一步：从蓝图库中选定目标平台预设(可通过搜索栏快速定位)", "1. Select target platform preset from the blueprint library (quickly locatable via search bar)");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps.2", "第二步：确认材料清单后，从背包导入或手动放置所需材料至材料槽", "2. After confirming material list, import from inventory or manually place required materials into slots");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps.3", "第三步：调整部署参数(含坐标偏移量、结构旋转角度、镜像模式等)", "3. Adjust deployment parameters (including coordinate offset, structure rotation angle, mirror mode, etc.)");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps.4", "第四步：点击“启动”按钮，工具将自动执行搭建流程", "4. Click 'Start'—the tool will automatically execute the construction process");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.steps.5", "第五步：部署完成后可保存当前参数为模板，或加载新蓝图开启下一轮搭建", "5. After completion, save current parameters as a template or load a new blueprint for the next deployment");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.advanced", "高级功能", "Advanced Features");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.advanced.1", "支持通过坐标卡记录指定区域(需设定对角锚点)，一键导出为可复用的自定义蓝图", "Supports recording designated areas (via diagonal anchor points) with coordinate cards, exporting as reusable custom blueprints with one click");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.advanced.2", "可调节部署速率、光照同步模式及空气方块处理策略(保留/替换)", "Adjustable deployment speed, light synchronization mode, and air block handling (retain/replace)");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.material", "材料要求", "Material Requirements");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.material.1", "系统会根据蓝图复杂度自动计算所需材料", "The system automatically calculates required materials based on blueprint complexity.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.material.2", "高阶蓝图需额外投入材料", "High-tier blueprints require additional materials.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.notes", "注意事项", "Cautions");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.notes.1", "部署任务启动后将持续执行至完成，中途无法中断，启动前务必确认部署位置无障碍物且材料充足", "Once started, deployment runs to completion without interruption. Ensure the target location is obstacle-free and materials are sufficient beforehand.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.notes.2", "大型结构部署会占用较高系统资源", "Deploying large structures consumes significant system resources.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.notes.3", "按流程完成参数配置与材料准备后，点击控制模块的确认按钮即可启动部署", "After completing parameter configuration and material preparation as instructed, click the control module's confirm button to start deployment.");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export", "蓝图导出功能", "Blueprint Export Function");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.1", "将两张带有坐标位置的坐标卡插入工具底部插槽后，打开选择预设界面可激活导出模式", "After inserting two coordinate cards with coordinate positions into the slot at the bottom of the tool, open the selection preset interface to activate the export mode");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.2", "导出蓝图在游戏根目录下logs/platform文件夹中", "The exported blueprint is located in the logs/platform folder under the game's root directory");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.3", "欢迎提交优质蓝图至社区仓库", "Welcome to submit high-quality blueprints to the community repository");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.4", "不支持框架方块和小方块", "Does not support FramedBlocks or LittleTiles");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.5", "https://github.com/GregTech-Odyssey/Gto-Extended-Platform-Presets", "https://github.com/GregTech-Odyssey/Gto-Extended-Platform-Presets");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.help.export.6", "详细导入教程请看 example/READIR_cn.md", "For detailed import tutorial, please refer to example/README_en.md");
        addCNEN("gtocore.machine.industrial_platform_deployment_tools.export", "[导出]", "[Export]");

        addCNEN("gtocore.machine.village_trading_station.increase", "使用 %s 提升 1 最大交易次数", "Use %s to increase the maximum amount of transactions by 1");
        addCNEN("gtocore.machine.village_trading_station.enhance", "使用 %s 升级", "Upgrade using %s");
        addCNEN("gtocore.machine.village_trading_station.upper_limit", "已达到提升上限", "The upgrade limit has been reached");
        addCNEN("gtocore.machine.village_trading_station.replenishment_interval", "补货间隔: %s tick", "Replenishment Interval: %s tick");
        addCNEN("gtocore.machine.village_trading_station.trading_multiple", "交易乘数: %s", "Trading Multiple: %s");

        addCNEN("gtocore.machine.resonance_flower.stable_operation_times", "剩余稳定运行次数: %s", "Remaining stable operation times: %s");
        addCNEN("gtocore.machine.resonance_flower.time_fluctuation_coefficient", "时间波动系数: %s", "Time fluctuation coefficient: %s");
        addCNEN("gtocore.machine.resonance_flower.elemental_fluctuation_coefficient", "元素波动系数: %s", "Elemental fluctuation coefficient: %s");
        addCNEN("gtocore.machine.resonance_flower.current_recipe", "当前配方：%s", "Current recipe: %s");
        addCNEN("gtocore.machine.resonance_flower.tier_progress", "等级：Lv.%s  %s / %s", "Tier: Lv.%s  %s / %s");
        addCNEN("gtocore.machine.resonance_flower.tier_max", "等级：Lv.%s  §a§lMAX§r", "Tier: Lv.%s  §a§lMAX§r");
        addCNEN("gtocore.machine.resonance_flower.tier_effect", "耗时：%s%% 并行：%s", "Duration: %s%% Parallel: %s");

        addSingleMachineTooltips();

        DisplayRegistry.registerLanguage();
    }

    private static void addSingleMachineTooltips() {
        // machines defined in GTM
        SingleMachineTooltipBuilder.createGTM("electric_furnace")
                .epic("§7工业级烤面包机", "§7Industrial-grade Toaster")
                .legendary("§7太阳核心模拟器", "§7Solar Core Simulator");
        SingleMachineTooltipBuilder.createGTM("alloy_smelter")
                .epic("§7合金搅拌大师", "§7Alloy Mixing Master")
                .legendary("§7纳米级材料融合装置", "§7Nanoscale Material Fusion Device");
        SingleMachineTooltipBuilder.createGTM("arc_furnace")
                .epic("§7雷电法王", "§7Lightning Arc Master")
                .legendary("§7宙斯之怒", "§7Wrath of Zeus");
        SingleMachineTooltipBuilder.createGTM("assembler")
                .primitive("§7拼凑小物件", "§7Cobbling small items")
                .epic("§7自动化工厂核心", "§7Automated Factory Core")
                .legendary("§7自我复制的纳米机器人集群", "§7Self-replicating Nanobot Swarm");
        SingleMachineTooltipBuilder.createGTM("autoclave")
                .epic("§7高压灭菌Pro Max", "§7High-pressure Sterilization Pro Max")
                .legendary("§7超立方体相变舱", "§7Hypercube Phase Change Chamber");
        SingleMachineTooltipBuilder.createGTM("bender")
                .epic("§7金属芭蕾舞者", "§7Metal Ballerina")
                .legendary("§7黑洞级别的弯折力量", "§7Blackhole-level Bending Force");
        SingleMachineTooltipBuilder.createGTM("brewery")
                .epic("§7量子酿酒师", "§7Quantum Brewer")
                .legendary("§7醉倒泰坦的佳酿", "§7Titan-intoxicating Nectar");
        SingleMachineTooltipBuilder.createGTM("canner")
                .epic("§7罐头喷射器", "§7Can Launcher")
                .legendary("§7时空封装装置", "§7Spacetime Sealing Device");
        SingleMachineTooltipBuilder.createGTM("centrifuge")
                .epic("§7超速旋转咖啡杯", "§7Hyperspin Coffee Cup")
                .legendary("§7人工中子星核心", "§7Artificial Neutron Star Core");
        SingleMachineTooltipBuilder.createGTM("chemical_bath")
                .epic("§7元素温泉", "§7Elemental Hot Spring")
                .legendary("§7地球原始汤", "§7Primordial Soup");
        SingleMachineTooltipBuilder.createGTM("chemical_reactor")
                .primitive("§7简单物质化合", "§7Simple substance combination")
                .epic("§7分子舞池", "§7Molecular Dance Floor")
                .legendary("§7炼金术师的终极梦想", "§7Ultimate Dream of Alchemists");
        SingleMachineTooltipBuilder.createGTM("compressor")
                .epic("§7超级压力机", "§7Super Compressor")
                .legendary("§7人造白矮星核心", "§7Artificial White Dwarf Core");
        SingleMachineTooltipBuilder.createGTM("cutter")
                .epic("§7光子切割大师", "§7Photon Cutting Master")
                .legendary("§7因果律切割刀", "§7Causality Cutting Blade");
        SingleMachineTooltipBuilder.createGTM("distillery")
                .epic("§7精密提纯装置", "§7Precision Distillation Unit")
                .legendary("§7绝对纯净领域", "§7Absolute Pure Realm");
        SingleMachineTooltipBuilder.createGTM("electrolyzer")
                .epic("§7雷霆分解者", "§7Thunder Splitter")
                .legendary("§7强相互作用力破解器", "§7Strong Force Decryptor");
        SingleMachineTooltipBuilder.createGTM("electromagnetic_separator")
                .epic("§7磁暴分离器", "§7Magnetic Storm Separator")
                .legendary("§7希格斯场操纵仪", "§7Higgs Field Manipulator");
        SingleMachineTooltipBuilder.createGTM("extractor")
                .epic("§7终极榨汁机", "§7Ultimate Juicer")
                .legendary("§7真空零点能提取器", "§7Zero-point Energy Extractor");
        SingleMachineTooltipBuilder.createGTM("extruder")
                .epic("§7无限面条机", "§7Infinite Noodle Maker")
                .legendary("§7时空挤压装置", "§7Spacetime Extrusion Device");
        SingleMachineTooltipBuilder.createGTM("fermenter")
                .epic("§7微生物狂欢派对", "§7Microorganism Rave Party")
                .legendary("§7有机体超进化池", "§7Organism Hyper-evolution Pool");
        SingleMachineTooltipBuilder.createGTM("fluid_heater")
                .epic("§7等离子加热器", "§7Plasma Heater")
                .legendary("§7恒星内核模拟装置", "§7Stellar Core Simulator");
        SingleMachineTooltipBuilder.createGTM("fluid_solidifier")
                .primitive("§7静置液体形成固体", "§7Liquid to Solid by Settling")
                .epic("§7瞬间冷冻大师", "§7Instant Freeze Master")
                .legendary("§7绝对零度生成器", "§7Absolute Zero Generator");
        SingleMachineTooltipBuilder.createGTM("forge_hammer")
                .epic("§7雷神之锤", "§7Thor's Hammer")
                .legendary("§7行星级锻压装置", "§7Planetary Forging Device");
        SingleMachineTooltipBuilder.createGTM("forming_press")
                .epic("§7上帝之手", "§7God's Hand")
                .legendary("§7现实重塑器", "§7Reality Reshaper");
        SingleMachineTooltipBuilder.createGTM("lathe")
                .primitive("§7以稍高的效率生产杆", "§7Produces Rods a little more efficiently")
                .epic("§7比金刚石更加坚硬", "§7Harder than diamond")
                .legendary("§7特种钢超级飞轮", "§7Special Steel Super Flywheel");
        SingleMachineTooltipBuilder.createGTM("scanner")
                .epic("§7量子扫描仪", "§7Quantum Scanner")
                .legendary("§7超维信息读取器", "§7Hyperdimensional Information Reader");
        SingleMachineTooltipBuilder.createGTM("mixer")
                .epic("§7混沌搅拌机", "§7Chaos Mixer")
                .legendary("§7大爆炸模拟器", "§7Big Bang Simulator");
        SingleMachineTooltipBuilder.createGTM("ore_washer")
                .epic("§7矿物SPA中心", "§7Ore SPA Center")
                .legendary("§7量子级纯净清洗", "§7Quantum-level Purification");
        SingleMachineTooltipBuilder.createGTM("packer")
                .primitive("§7物品封装", "§7Item Packing")
                .epic("§7空间压缩包装机", "§7Space Compression Packer")
                .legendary("§7二向箔包装技术", "§7Dimensional Foil Packing");
        SingleMachineTooltipBuilder.createGTM("polarizer")
                .epic("§7磁极大师", "§7Polarity Master")
                .legendary("§7量子自旋操纵仪", "§7Quantum Spin Manipulator");
        SingleMachineTooltipBuilder.createGTM("laser_engraver")
                .epic("§7光子雕刻师", "§7Photon Engraver")
                .legendary("§7时空蚀刻机", "§7Spacetime Etcher");
        SingleMachineTooltipBuilder.createGTM("sifter")
                .epic("§7量子筛分仪", "§7Quantum Sifter")
                .legendary("§7平行宇宙过滤器", "§7Parallel Universe Filter");
        SingleMachineTooltipBuilder.createGTM("thermal_centrifuge")
                .epic("§7等离子离心机", "§7Plasma Centrifuge")
                .legendary("§7超高温物质分离器", "§7Ultra-high Temperature Separator");
        SingleMachineTooltipBuilder.createGTM("wiremill")
                .primitive("§7简单地生产导线", "§7Simple Wire Production")
                .epic("§7拉力约一百万牛", "§7~1MN Tension")
                .legendary("§7彻底摧毁范德华力", "§7Van der Waals Force Destroyer");
        SingleMachineTooltipBuilder.createGTM("circuit_assembler")
                .epic("§7纳米级电路工坊", "§7Nanoscale Circuit Workshop")
                .legendary("§7量子计算机组装线", "§7Quantum Computer Assembly Line");
        SingleMachineTooltipBuilder.createGTM("macerator")
                .epic("§7分子破碎机", "§7Molecular Grinder")
                .legendary("§7物质解构器", "§7Matter Deconstructor");
        // tooltips for higher tier of gas collector and rock crusher has written by GTM

        // machines defined in GTO
        SingleMachineTooltipBuilder.createGTO("arc_generator")
                .basic("§7摩擦生电", "§7Friction Electricity")
                .elite("§7特斯拉线圈", "§7Tesla Coil")
                .ultimate("§7人工闪电风暴", "§7Artificial Lightning Storm")
                .epic("§7等离子电弧矩阵", "§7Plasma Arc Matrix")
                .legendary("§7恒星能量提取器", "§7Stellar Energy Extractor");
        SingleMachineTooltipBuilder.createGTO("dehydrator")
                .basic("§7脱水，脱水！", "§7Dehydrate, Dehydrate!")
                .elite("§7沙漠风暴模拟器", "§7Desert Storm Simulator")
                .ultimate("§7绝对干燥领域", "§7Absolute Dry Zone")
                .epic("§7真空干燥奇点", "§7Vacuum Drying Singularity")
                .legendary("§7水分子消除装置", "§7Water Molecule Eliminator");
        SingleMachineTooltipBuilder.createGTO("unpacker")
                .primitive("§7粗糙地拆开", "§7Crude Unpacking")
                .basic("§7标准拆包操作", "§7Standard Unpacking")
                .elite("§7精准解包大师", "§7Precision Unpacking Master")
                .ultimate("§7分子级拆解", "§7Molecular Disassembly")
                .epic("§7空间解压缩装置", "§7Spatial Decompression Unit")
                .legendary("§7因果律包装破解器", "§7Causality Package Cracker");
        SingleMachineTooltipBuilder.createGTO("cluster")
                .basic("§7生产薄片", "§7Produces Foils")
                .elite("§7精密镀膜机", "§7Precision Coating Machine")
                .ultimate("§7原子级沉积", "§7Atomic Deposition")
                .epic("§7量子层积装置", "§7Quantum Lamination Device")
                .legendary("§7现实编织者", "§7Reality Weaver");
        SingleMachineTooltipBuilder.createGTO("rolling")
                .basic("§7压扁成型", "§7Flat Rolling")
                .elite("§7液压精密轧制", "§7Hydraulic Precision Rolling")
                .ultimate("§7分子级压延", "§7Molecular Calendering")
                .epic("§7时空碾压机", "§7Spacetime Roller")
                .legendary("§7维度碾压装置", "§7Dimensional Compactor");
        SingleMachineTooltipBuilder.createGTO("laminator")
                .basic("§7绝缘套皮，安全用电！", "§7Insulation Coating, Safe Electricity!")
                .elite("§7纳米涂层机", "§7Nano Coating Machine")
                .ultimate("§7量子层压技术", "§7Quantum Lamination")
                .epic("§7超导覆膜装置", "§7Superconducting Coating Unit")
                .legendary("§7绝对绝缘屏障生成器", "§7Absolute Insulation Barrier Generator");
        SingleMachineTooltipBuilder.createGTO("loom")
                .primitive("§7只是把导线捆在一起", "§7Just Bundling Wires Together")
                .basic("§7自动化编织", "§7Automated Weaving")
                .elite("§7精密纺织大师", "§7Precision Textile Master")
                .ultimate("§7分子级编织", "§7Molecular Weaving")
                .epic("§7量子织布机", "§7Quantum Loom")
                .legendary("§7命运纺纱机", "§7Fate Spinning Wheel");
        SingleMachineTooltipBuilder.createGTO("laser_welder")
                .basic("§7古法电焊技术", "§7Ancient Welding Technique")
                .elite("§7光子焊接大师", "§7Photon Welding Master")
                .ultimate("§7量子纠缠焊接", "§7Quantum Entanglement Welding")
                .epic("§7时空缝合装置", "§7Spacetime Stitcher")
                .legendary("§7因果律焊接器", "§7Causality Welder");
        SingleMachineTooltipBuilder.createGTO("world_data_scanner")
                .basic("§7区块信息读取器", "§7Chunk Data Reader")
                .elite("§7维度特征分析仪", "§7Dimensional Feature Analyzer")
                .ultimate("§7现实数据采集装置", "§7Reality Data Collection Unit")
                .epic("§7量子态世界扫描仪", "§7Quantum State World Scanner")
                .legendary("§7宇宙信息库终端", "§7Cosmic Database Terminal");
        SingleMachineTooltipBuilder.createGTO("vacuum_pump")
                .steam("§7简易抽气装置", "§7Simple Air Pump", "§7过热蒸汽真空泵", "§7Superheated Steam Vacuum Pump")
                .add("§7基础真空发生器", "§7Basic Vacuum Generator", GTValues.LV)
                .add("§7工业级真空系统", "§7Industrial Vacuum System", GTValues.MV)
                .add("§7高效真空抽取装置", "§7High-efficiency Vacuum Extraction Unit", GTValues.HV);
    }

    private record SingleMachineTooltipBuilder(String namespace, String type) {

        private static SingleMachineTooltipBuilder createGTM(String machineType) {
            return new SingleMachineTooltipBuilder(GTCEu.MOD_ID, machineType);
        }

        private static SingleMachineTooltipBuilder createGTO(String machineType) {
            return new SingleMachineTooltipBuilder(GTOCore.MOD_ID, machineType);
        }

        private SingleMachineTooltipBuilder add(String cn, String en, int tier) {
            addCNEN(namespace + ".machine." + GTValues.VN[tier].toLowerCase() + "_" + type + ".tooltip", cn, en);
            return this;
        }

        private SingleMachineTooltipBuilder add(String cn, String en, int tierMin, int tierMax) {
            for (int tier = tierMin; tier <= tierMax; tier++) {
                add(cn, en, tier);
            }
            return this;
        }

        private SingleMachineTooltipBuilder steam(String lpCn, String lpEn, String hpCn, String hpEn) {
            addCNEN(namespace + ".machine.lp_steam_" + type + ".tooltip", lpCn, lpEn);
            addCNEN(namespace + ".machine.hp_steam_" + type + ".tooltip", hpCn, hpEn);
            return this;
        }

        private SingleMachineTooltipBuilder primitive(String cn, String en) {
            // primitive machines are all from GTO
            addCNEN(GTOCore.MOD_ID + ".machine." + GTValues.VN[GTValues.ULV].toLowerCase() + "_" + type + ".tooltip", cn, en);
            return this;
        }

        private SingleMachineTooltipBuilder basic(String cn, String en) {
            return add(cn, en, GTValues.LV, GTValues.EV);
        }

        private SingleMachineTooltipBuilder elite(String cn, String en) {
            return add(cn, en, GTValues.IV, GTValues.ZPM);
        }

        private SingleMachineTooltipBuilder ultimate(String cn, String en) {
            return add(cn, en, GTValues.UV);
        }

        private SingleMachineTooltipBuilder epic(String cn, String en) {
            return add(cn, en, GTValues.UHV, GTValues.UXV);
        }

        private SingleMachineTooltipBuilder legendary(String cn, String en) {
            return add(cn, en, GTValues.OpV);
        }
    }
}
