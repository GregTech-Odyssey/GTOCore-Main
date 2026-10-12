package com.gtocore.common.data.translation;

import com.gtocore.api.data.Algae;
import com.gtocore.api.lang.GTOTips;
import com.gtocore.api.lang.Tx;
import com.gtocore.config.GTORules;

import com.gregtechceu.gtceu.api.tooltip.TipSlot;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import appeng.api.config.PowerUnits;

import static com.gtocore.common.data.translation.ComponentSlang.AfterModuleInstallation;
import static com.gtocore.common.data.translation.ComponentSlang.EfficiencyBonus;
import static com.gtocore.common.data.translation.ComponentSlang.MainFunction;
import static com.gtocore.common.data.translation.ComponentSlang.RunningRequirements;

public final class GTOMachineTooltipsA {

    public static final TipSpec ComputationalDataHolder = GTOTips.of("computational_data_holder", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("安装在计算机或超算中心等场所，用于读取算力流动的生产数据", "Installed in computers or supercomputing centers, used to read production data of computational workload flow"));
        t.feature(t.tr("每5秒，获得√(最大算力+当前使用中的算力)的计算研究点数，并存储于机器内的晶片中", "Every 5 seconds, it generates √(maximum computational workload + currently used computational workload) computational research points, which are stored in the data crystal inside the machine"));
    });

    public static final TipSpec ConnectingRodHatchTooltips = GTOTips.of("connecting_rod_hatch", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于将附着的生物组织的生物能量转化为可导出的能量", "Used to convert the bioenergy of attached biological tissues into exportable energy"));
    });

    public static final TipSpec BioOscillationGeneratorTooltips = GTOTips.of("bio_oscillation_generator", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("将生物组织的生长与培养液转化为电能", "Converts biological tissue growth and culture medium into electrical energy"));
        t.requirement(t.tr("生物组织与培养液均需要一次性装入机器；每次装填最多吸收64个组织与1,000,000,000mB培养液", "Biological tissue and culture medium must be loaded into the machine at once; each filling accepts up to 64 tissues and 1,000,000,000mB of culture medium"));
        t.feature(t.tr("组织从0点开始生长，依次经历幼年、成年、壮年和老年阶段；达到老年阶段上限后组织死亡并被清除", "Tissue starts growing from 0 points and passes through Juvenile, Adult, Mature, and Elderly stages; it dies and is cleared after exceeding the Elderly stage limit"));
        t.feature(t.tr("可以通过组织传感器监测组织的生长点数", "Tissue growth points can be monitored through the Tissue Sensor"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("运行需要同时放入生物组织、匹配等级的培养液和连接杆；培养液等级与机加工控制模块等级均不得低于组织要求", "Running requires biological tissue, culture medium of a matching tier, and a connecting rod; both the medium tier and machining control module tier must meet the tissue requirement"));
        t.requirement(t.tr("组织每5ticks生长一次；培养液营养可用率初始为100%，每秒降低0.1%，低于设定阈值后培养液会被清空并输出对应废液", "Tissue grows every 5 ticks; culture medium starts at 100% nutrient availability and decreases by 0.1% per second, then is cleared and converted into its corresponding waste fluid below the configured threshold"));
        t.requirement(t.tr("连接杆每20ticks损耗1点耐久；连接杆材料等级越高，发电乘数越高", "The connecting rod loses 1 durability every 20 ticks; higher-tier connecting rod materials provide a higher generation multiplier"));

        t.heading(EfficiencyBonus);
        t.formula(t.tr("基础发电量 = 组织等级^连接杆等级 × 组织数量 × 培养液数量 × 8EU/t", "Base power = tissue tier^connecting rod tier × tissue amount × culture medium amount × 8EU/t"));
        t.formula(t.tr("组织生长量 = 最大生长速率 × 营养可用率 ÷（饱和因子 + 营养可用率）/5ticks", "Tissue growth = maximum growth rate × nutrient availability × (saturation factor + nutrient availability) per 5 ticks"));
        t.bonus(t.tr("能量控制模块等级为2时发电量×1.2，等级为3时发电量×1.5", "An energy control module tier of 2 multiplies power by 1.2, and tier 3 by 1.5"));
        t.bonus(t.tr("通过电刺激模块对组织进行电刺激，可进一步提升发电量", "Electrical stimulation of tissue through the electrical stimulation module can further increase power generation"));
        t.feature(t.tr("组织阶段决定电刺激增幅；组织数量和培养液数量决定基础发电量，营养可用率主要影响组织生长", "The tissue stage determines the stimulation boost; tissue amount and culture medium amount determine base power, while nutrient availability mainly affects tissue growth"));
    });

    public static final TipSpec BioOscillationElectricStimulatorTooltips = GTOTips.of("bio_oscillation_electric_stimulator", t -> {
        t.heading(MainFunction);
        t.keepOrder();
        t.feature(t.tr("作为生物振荡发电机的模块，对其中的生物组织进行电刺激", "Acts as a module for the Bio Oscillation Generator and electrically stimulates its biological tissue"));
        t.control(Component.translatable("gtocore.biooscillation.button.set.electrical.stimulation.desc", GTORules.BIO_STIMULATION_DURATION.get() / 20));
        t.feature(t.tr("运行完成后消耗等量的组织生长点数，并按组织当前阶段的电刺激增幅提供临时发电加成", "After completing, the stimulator consumes the corresponding amount of tissue growth points and grants a temporary power boost based on the tissue's current stage"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("模块必须连接至生物振荡发电机，且发电机内必须存在有效的生物组织", "The module must be connected to a Bio Oscillation Generator containing valid biological tissue"));
        t.requirement(t.tr("100%强度时，一次电刺激消耗组织1000点；耗电量为组织数据中标注的电刺激能耗，低于100%时按比例减少", "At 100% intensity, one stimulation consumes 1,000 tissue points; power consumption equals the tissue's listed stimulation energy cost and scales with lower intensities"));

        t.heading(EfficiencyBonus);
        t.bonus(t.tr("电刺激增幅取决于组织当前阶段；刺激点数越多，增幅持续时间越长，最多持续60秒", "The stimulation boost depends on the tissue's current stage; more stimulation points provide a longer boost, up to 60 seconds"));
        t.feature(t.tr("新的电刺激会累加剩余持续时间；新的增幅系数会覆盖旧的增幅系数", "New stimulations add to the remaining duration; new boost coefficients overwrite old ones"));
    });

    public static final TipSpec pulseMachineMaintenancePedestalTooltips = GTOTips.of("pulse_machine_maintenance_pedestal", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("当机器上方的脉冲核心收到一个强度不低于240的魔力脉冲时，尝试工作一次", "When the pulse core above the machine receives a mana pulse with strength not less than 240, it will attempt to work once"));
        t.feature(t.tr("工作时会尝试对附近的机器进行维护，或是从消声仓中消除4份灰尘", "When working, it will attempt to maintain nearby machines or eliminate 4 units of dust from the muffler hatch"));
        t.feature(t.tr("工作半径为12格，且每次工作仅随机维护一个机器或随机消除4份灰尘", "The working radius is 12 blocks, and each time it works, it only randomly maintains one machine or randomly eliminates 4 units of dust"));
        t.feature(t.tr("这样的魔力脉冲可以通过魔力发射器安装魔力透镜：强度来发射", "Such mana pulses can be emitted by installing a mana lens: Power on a mana blaster"));
        t.feature(t.tr("或是使用精灵等级及以上的魔力发射器", "Or using a mana blaster of Alfhelm tier or above"));
    });

    public static final TipSpec virtualCoinMinerTooltips = GTOTips.of("virtual_coin_miner", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("通过消耗算力来挖掘虚拟货币", "Mines virtual coins by consuming computational workload"));
        t.feature(t.tr("每提交一定的算力后，矿机将获得一个金币奖励", "After submitting a certain amount of computational workload, the miner will receive a gold coin reward"));
        t.penalty(t.tr("每获得一次金币奖励，下一次奖励将需要提交更多的算力", "Each time a gold coin reward is obtained, the next reward will require more computational workload to be submitted"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("运行需要每秒消耗20mB多氯联苯冷却剂", "Consumes 20mB of PCB coolant per second while running"));
        t.requirement(t.tr("每提交的1CWU算力需要1920EU的能量支持", "Each 1 CWU of computational workload submitted requires 1920 EU of energy support"));
    });

    public static final TipSpec meInputBufferPartMachineTooltips = GTOTips.of("me_input_buffer_part_machine", t -> {
        t.heading(MainFunction);
        t.requirement(t.tr("ME输入仓室的一种特殊模式，仅能使用样板进行配置", "A special mode of the ME input hatch/bus, can only be configured using patterns"));
        t.feature(t.tr("在该模式下，每个槽位使用样板配置一组特定的物品或流体，仓室将从ME网络提取对应的物品与流体", "In this mode, each slot is configured with a pattern for a specific group of items or fluids, and the hatch/bus will extract the corresponding items and fluids from the ME network"));
    });

    public static final TipSpec planetaryGasCollectorTooltips = GTOTips.of("planetary_gas_collector", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于在行星表面收集大气中的气体(效率非常惊人)", "Used to collect gases from the atmosphere on the surface of planets (with an amazing efficiency)"));
        t.feature(t.tr("在地球建立的空间站能够收集到来自主世界、下界和末地的气体", "In the space station built on Earth, gases from the Overworld, Nether, and End can be collected"));
        t.feature(t.tr("在其他行星建立的空间站能够收集到该行星特有的大气气体", "In the space station built on other planets, the unique atmospheric gases of that planet can be collected"));
    });

    public static final TipSpec space3DPrinterTooltips = GTOTips.of("space_3d_printer", t -> {
        t.lore(t.tr("GTO的工程师们想到，只要在重力微乎其微的空间站中进行3D打印，就不用像在地球上那样，还要打印支架", "GTO engineers thought that as long as 3D printing is done in a space station with minimal gravity, there is no need to print supports like on Earth"));
        t.lore(t.tr("于是，空间站3D打印机诞生了", "Thus, the space station 3D printer was born"));
    });

    public static final TipSpec DataExportMachineTooltips = GTOTips.of("data_export_machine", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于将科技树解锁的数据导出到闪存等存储设备中", "Used to export data unlocked in the tech tree to flash drives and other storage devices"));
        t.requirement(t.tr("导出需要消耗7680EU/t与一定时间进行数据拷贝", "Exporting requires consuming 7680 EU/t and a certain amount of time for data copying"));
    });

    public static final TipSpec DataCenterTooltips = GTOTips.of("data_center", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于存储大量数据的多功能数据中心", "A multifunctional data center for storing large amounts of data"));
        t.feature(t.tr("同时，可以接受算力输入进行数据处理与研究", "At the same time, it can accept computational workload input for data processing and research"));
        t.requirement(t.tr("接受算力的上限为16*2^(玻璃等级-6)CWU/t", "The upper limit of computational workload accepted is 16*2^(glass level-6) CWU/t"));
        t.bonus(t.tr("每个数据仓的槽位与每个安装的配方数据分别额外提供0.01%和1%的算力输入上限（独立乘区）", "Each data hatch slot and each installed recipe data additionally provides 0.01% and 1% of the computational workload input limit (independently multiplied)"));
        t.formula(t.tr("等效上限公式：16*2^(玻璃等级-6)*(1+数据仓槽位数*0.0001)*(1+配方数据数*0.01)CWU/t", "Equivalent upper limit formula: 16*2^(glass level-6)*(1+data hatch slot count*0.0001)*(1+recipe data count*0.01) CWU/t"));
        t.control(t.tr("从机器UI的左侧访问科技树与便捷导出功能", "Access the tech tree and convenient export functions from the left side of the machine UI"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("闲置时，每个数据/光学仓耗能为§f1920 EU/t§7。", "When idle, each data/optical hatch consumes §f1920 EU/t§7."));
        t.requirement(t.tr("连接时，每个已连接的数据/光学仓耗能为§f30,720 EU/t§7。", "When connected, each connected data/optical hatch consumes §f30,720 EU/t§7."));
        t.requirement(t.tr("处于研究状态时，电力消耗翻倍，并需要100mB/秒的多氯联苯冷却剂", "When in research state, power consumption is doubled and requires 100mB/s of PCB coolant"));
    });

    public static final TipSpec ScanStationMachineTooltips = GTOTips.of("scan_station_machine", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("将存储在晶片中的数据进行解析，转化为团队共享的研究点数", "Parse the data stored in the data crystal and convert it into team-shared research points"));
        t.feature(t.tr("无论内部有多少数据，每张晶片的解析耗时与耗能均为固定值", "Regardless of how much data is inside, the parsing time and energy consumption for each data crystal are fixed"));
    });

    public static final TipSpec IntelligentScanningProxyTooltips = GTOTips.of("intelligent_scanning_me_proxy", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("连接ME网络，将其中的物品与流体提供给智能扫描管理平台", "Connects to an ME network and provides its items and fluids to the Intelligent Scanning Management Platform"));
        t.feature(t.tr("数据晶体不会作为扫描目标；带有标签的物品或流体按无标签的基础类型处理", "Data crystals are excluded from scan targets; tagged items and fluids are treated as their untagged base types"));
        t.feature(t.tr("只记录库存中的物品与储量超过1000mB的流体，并每2秒刷新一次", "Records items and fluids stored above 1,000mB, refreshing every 2 seconds"));
    });

    public static final TipSpec IntelligentScanningManagementPlatformMachineTooltips = GTOTips.of("intelligent_scanning_management_platform", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("从ME网络消耗物品或流体进行扫描，并将扫描数据写入数据晶片", "Consumes items or fluids from an ME network to scan them and write the scan data to a data crystal"));
        t.control(t.tr("扫描目标可以在机器GUI的扫描队列页面管理", "Scan targets can be managed on the scan queue page of the machine GUI"));

        t.heading(t.tr("工作模式：选定物品模式", "Working Mode: Selected Item Mode"));
        t.keepOrder();
        t.control(t.tr("从界面选择多项物品或流体作为扫描目标", "Select multiple items or fluids from the interface as scan targets"));
        t.feature(t.tr("机器会试图重复扫描它们，直到填满数据晶片", "The machine will attempt to repeatedly scan them until the data crystal is filled"));
        t.feature(t.tr("选择多项组成材料相同的物品或流体时，机器只会扫描其中的一项", "When selecting multiple items or fluids with the same composition, the machine will only scan one of them"));

        t.heading(t.tr("工作模式：选定物品单次扫描模式", "Working Mode: Selected Item Single Scan Mode"));
        t.keepOrder();
        t.control(t.tr("从界面选择多项物品或流体作为扫描目标", "Select multiple items or fluids from the interface as scan targets"));
        t.feature(t.tr("机器会试图扫描它们1次，然后停止扫描", "The machine will attempt to scan them once, then stop scanning"));

        t.heading(t.tr("工作模式：未学习物品模式", "Working Mode: Unlearned Item Mode"));
        t.feature(t.tr("扫描所有未学习的物品或流体，每种仅扫描一个目标", "Scans all unlearned items or fluids, only one target of each type is scanned"));
        t.feature(t.tr("机器会持续检测并将新增的未学习物品或流体加入扫描队列", "The machine will continuously detect and add newly unlearned items or fluids to the scan queue"));

        t.heading(t.tr("工作模式：未学习物品单次扫描模式", "Working Mode: Unlearned Item Single Scan Mode"));
        t.feature(t.tr("扫描所有未学习的物品或流体，每种仅扫描一个目标", "Scans all unlearned items or fluids, only one target of each type is scanned"));
        t.feature(t.tr("在未找到新的未学习物品或流体后，机器将停止扫描", "The machine will stop scanning after no new unlearned items or fluids are found"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("扫描使用的晶片必须额外输入到机器中，且每次扫描消耗1个晶片", "The data crystal used for scanning must be additionally input into the machine, and each scan consumes one data crystal"));
        t.requirement(t.tr("扫描物品时花费1个物品，扫描流体时花费1,000mB流体", "Scanning an item consumes 1 item, scanning a fluid consumes 1,000mB of fluid"));
        t.requirement(t.tr("每次运行持续%s秒，耗能为8×本次扫描的数据字节数+8 EU/t", "Each operation lasts %s seconds and uses 8 × scanned data bytes + 8 EU/t", GTORules.SCAN_DURATION.get() / 20));
        t.feature(t.tr("同一种材料每次运行只扫描一个目标", "Only one target of the same material is scanned per operation"));
    });

    public static final TipSpec CatalystDataHolder = GTOTips.of("catalyst_data_holder", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("安装在化工厂等场所，用于监测使用催化剂的反应", "Installed in chemical plants and other places, used to monitor reactions that use catalysts"));
        t.feature(t.tr("每次机器运行包含催化剂的反应时，催化剂数据仓将记录该次配方的总效率", "Each time the machine runs a reaction that contains a catalyst, the catalyst data hatch will record the total efficiency of that recipe"));
        t.formula(t.tr("总效率为配方时间乘以能耗与标准配方时间与能耗的比值", "Total efficiency is the product of recipe time and energy consumption divided by the standard recipe time and energy consumption"));
        t.feature(t.tr("配方完成后，记录并产生(总效率×配方并行数×原始配方等级)的催化研究点数，并存储于机器内的晶片中", "After the recipe is completed, the catalyst data hatch will record and generate (total efficiency × recipe parallelism × original recipe level) catalyst research points, which will be stored in the data crystal inside the machine"));
    });

    public static final TipSpec EnergyDataHolder = GTOTips.of("energy_data_holder", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("安装在能源产生相关的机器上，对能源稳定性与功率进行监测", "Installed on energy generation-related machines to monitor energy stability and power"));
        t.requirement(Component.translatable("gtocore.lang.energy_data_holder.2", GTORules.ENERGY_DATA_WARMUP.get() / 20));
        t.feature(t.tr("此后每次机器完成输出UEV级及以上功率的配方时，能源数据仓将转化(功率等级 - 10)²的能源研究点数，并存储于机器内的晶片中", "Thereafter, each time the machine finishes recipes that output UEV-level or higher power, the energy data hatch will convert (power level - 9)² energy research points and store them in the data crystal inside the machine"));
    });

    public static final TipSpec ThermaldynamicsDataHolder = GTOTips.of("thermodynamic_data_holder", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("配合热力学分析平台使用，用于热力学相关的研究", "Used in conjunction with the thermodynamic analysis platform for thermodynamics-related research"));
    });

    public static final TipSpec ThermodynamicAnalysisPlatformMachineTooltips = GTOTips.of("thermodynamic_analysis_platform_machine", t -> {
        t.lore(t.tr("一名热力膨胀的前员工阐述到，他之前发现，热力膨胀公司的业务领域涉及到机械，材料，装备等方方面面，但在热力学研究方面却投入甚少", "A former employee of Thermal Expansion stated that he previously found that Thermal Expansion's business areas involved machinery, materials, equipment, and many other aspects, but invested very little in thermodynamics research"));
        t.lore(t.tr("(而且热力膨胀公司近几年好像还在深耕神秘学领域，并且进展甚微)", "(Moreover, Thermal Expansion seems to have been delving into the field of thaumaturgy in recent years, with little progress)"));
        t.lore(t.tr("于是他带着他自己研发的热力学分析平台从热力膨胀公司跳槽到GTO，并在GTO的支持下，成立了热力学研究所", "So he left Thermal Expansion with his own developed thermodynamic analysis platform and, with GTO's support, established the Thermodynamics Research Institute"));

        t.heading(MainFunction);
        t.feature(t.tr("用于监测热力学相关的实验", "Used to monitor thermodynamics-related experiments"));
        t.feature(t.tr("机器结构中，可见有两个热力学封闭体系，分别为§b低温体系§r与§c高温体系§r", "In the machine structure, there are two independent thermodynamic closed systems, namely §blow-temperature system§r and §chigh-temperature system§r"));
        t.feature(t.tr("当两边体系温差达到热平衡时（温差不超过10K即可计入），热力学分析平台将开始进行热力学分析", "When the temperature difference between the two systems reaches thermal equilibrium(<10K), the thermodynamic analysis platform will begin thermodynamic analysis"));
        t.feature(t.tr("分析持续30秒，期间可以通过对应的热传导仓向两边体系输入热量以调节温差", "The analysis lasts for 30 seconds, during which heat can be input to both systems through the corresponding thermal conduction hatches to adjust the temperature difference"));
        t.feature(t.tr("分析结束时，两侧体系的温差越大，产生的热力学研究点数越多", "At the end of the analysis, the greater the temperature difference between the two systems, the more thermodynamic research points are generated"));
        t.formula(t.tr("每次分析产生的热力学研究点数 = (温差/250K)", "The thermodynamic research points generated by each analysis = (temperature difference / 250K)"));

        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("分析期间，热力学分析平台每秒消耗§f30,720 EU/t§r", "During the analysis, the thermodynamic analysis platform consumes §f30,720 EU/t§r per second"));
    });

    public static final TipSpec LaserComputationTestingPlatformMachineTooltips = GTOTips.of("laser_computation_testing_platform_machine", t -> {
        t.lore(t.tr("光子计算是GTO集团近年重点研究的方向之一", "Photon computation is one of the key research directions of GTO Group in recent years"));
        t.lore(t.tr("攻克了它，便能使设备的计算与处理能力达到前所未有的高度", "Overcoming it will enable the computing and processing capabilities of devices to reach unprecedented heights"));
        t.lore(t.tr("还能让各种杂碎的产线也能得到驾驭超高能的能力，生产力获得史诗级的飞跃", "It can also allow various miscellaneous production lines to gain the ability to harness ultra-high energy, resulting in an epic leap in productivity"));

        t.heading(MainFunction);
        t.feature(t.tr("提供一个光子计算的测试平台，用于测试光子计算相关的实验", "Provides a photon computation testing platform for testing photon computation-related experiments"));
        t.feature(t.tr("向机器的光束仓输入符合要求的光束，机器将进行光子计算测试", "Input beams that meet the requirements into the machine's beam hatch, and the machine will perform photon computation tests"));
        t.supported(t.tr("可以利用半反射镜，让一束光束在激发晶体内多次反射，利用谐振腔的原理，在调节适当的反射率等参数后，产生更高强度的光束，来达到测试要求", "You can use a semi-reflective mirror to allow a beam of light to reflect multiple times within the excitation crystal, using the principle of a resonant cavity to generate a higher intensity beam to meet the testing requirements"));
    });

    public static final TipSpec DataHolderUniversal = GTOTips.of("data_holder_universal", t -> {
        t.heading(t.tr("数据支架仓", "About Data Holder"));
        t.feature(t.tr("这种仓室可以收集不同来源的数据，并转化为研究点数存储于晶片中", "This hatch can collect data from different sources and convert it into research points stored in the data crystal"));
        t.requirement(t.tr("安装到对应的机器上以生效", "Install it on the corresponding machine to take effect"));
    });

    public static final TipSpec directedHyperCubeMachineTooltips = GTOTips.of("directed_hyper_cube_machine", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("代理多个流体或物品存储器，且指定代理方向", "Proxy (a or multi) (fluid or item or both)storage with directed sides"));
        t.control(t.tr("使用§b坐标标签枪§r按照分配顺序绑定方块", "Use the §bTesseract Target Marker§r to bind blocks in allocation order"));

        t.heading(t.tr("被样板供应器推送时", "When being pushed by the Pattern Provider"));
        t.feature(t.tr("将样板供应器的样板内容按照编写顺序依次输出到多个方块的多个面", "Outputs the pattern contents of the Pattern Provider to multiple sides of multiple blocks in the order written"));
        t.requirement(t.tr("原料在样板中对应的编号严格对应绑定方块的编号", "The number corresponding to the raw material in the pattern strictly corresponds to the number of the bound block"));
        t.requirement(t.tr("若样板内容的长度大于绑定的方块数量，则该样板将拒绝被推送", "If the length of the pattern content is greater than the number of bound blocks, the pattern will refuse to be pushed"));
        t.feature(t.tr("适用于一些较为复杂的自动化场景（如新生魔艺的附魔装置自动化）", "Suitable for some more complex automation scenarios (such as Ars Nouveau's Enchanting Apparatus)"));
    });

    public static final TipSpec meEnergySubstationTooltips = GTOTips.of("me_energy_substation", t -> {
        t.heading(MainFunction);
        t.keepOrder();
        t.feature(t.tr("为ME网络提供额外的能量供应", "Provides additional energy supply for the ME network"));
        t.feature(Tx.join(
                t.tr("每一点EU可以转换成 ", "Each point of EU can be converted into "),
                Tx.lit(PowerUnits.FE.convertTo(PowerUnits.AE, (double) ConfigHolder.INSTANCE.compat.energy.euToFeRatio)),
                t.tr(" 点AE能量", " points of AE energy")));
        t.control(t.tr("使用ME能量访问仓导出能量到ME网络", "Use the ME Energy Access Hatch to export energy to the ME network"));
        t.bonus(t.tr("玻璃等级每级可将转换效率提升30%", "Each glass level can increase the conversion efficiency by 30%"));

        t.heading(AfterModuleInstallation);
        t.bonus(t.tr("安装模块可使转换效率额外x2", "Installing modules can further double the conversion efficiency"));
    });

    public static final TipSpec spaceBioResearchModuleTooltips = GTOTips.of("space_bio_research_module", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于在空间站内进行生物研究", "Used for biological research in the space station"));
        t.attention(t.tr("生物研究的运行结束时辐射剂量指的是，配方的最后一刻时，机器需要满足的辐射剂量要求", "The radiation dose at the end of biological research refers to the radiation dose requirement that the machine needs to meet at the last moment of the recipe"));
        t.requirement(t.tr("生物研究配方仅采用常规超频模式运行", "Biological research recipes only run in normal overclocking mode"));
        t.feature(t.tr("超净间环境等级由环境维护舱决定", "The cleanroom environment level is determined by the Environmental Maintenance Module"));
        t.feature(t.tr("机器本体可以为各种配方提供可调节的0~80Sv背景辐射环境", "The machine body can provide an adjustable 0~80Sv background radiation environment for various recipes"));
    });

    public static final TipSpec spaceElevatorConnectorModuleTooltips = GTOTips.of("space_elevator_connector_module", t -> {
        t.feature(t.tr("与当前星球的太空电梯连接", "Connects to the space elevator of the current planet"));
        t.bonus(t.tr("连接后，空间站各运行模块（如轨道冶炼舱等）可获得(0.9^n)×的耗时减免，n为太空电梯的动力模块等级", "After connecting, each operating module of the space station (such as orbital smelting chamber, etc.) can get a time reduction of (0.9^n)×, where n is the power module level of the space elevator"));
        t.bonus(t.tr("太空电梯安装的模块也将获得额外(0.9^(n/2))×的耗时减免", "Modules installed on the space elevator will also receive a time reduction of (0.9^(n/2))×"));
        t.bonus(t.tr("如果安装了高能转换调配舱，则底数0.9将变为0.8", "If a high-energy conversion and allocation chamber is installed, the base 0.9 will become 0.8"));
        t.penalty(Component.translatable("gtocore.lang.space_elevator_connector_module.3", 50.0 + 100.0 * GTORules.SPACE_ELEVATOR_COMPUTATION.get()));
        t.requirement(t.tr("该模块仅能连接在其他模块的下方", "This module can only connect below other modules"));
    });

    public static final TipSpec SpaceElevatorEngineeringDataModule = GTOTips.of("space_elevator_engineering_data_module", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("收集太空电梯其他模块的运行数据", "Collects operational data from other modules of the space elevator"));
        t.feature(Component.translatable("gtocore.lang.space_elevator_engineering_data_module.2", GTORules.SPACE_DATA_WORKS.get()));
        t.feature(t.tr("转化为的研究点数将存储于通用数据仓的晶片中", "The converted research points will be stored in the data crystal of the universal data hatch"));
        t.bonus(t.tr("太空电梯动力模块等级为n时，每次收集需要的运行次数乘以(4/(3+n))", "When the power module level of the space elevator is n, the number of runs needed for each collection is multiplied by (4/(3+n))"));
        t.bonus(t.tr("连接到空间站时，转化为的研究点数翻倍", "When connected to the space station, the converted research points are doubled"));
    });

    public static final TipSpec DataFormTestingPlantMachineTooltips = GTOTips.of("data_form_testing_plant_machine", t -> {
        t.lore(t.tr("为什么有时，大量的数据就能够被极其密集的存储，而有时这些数据却形成了物质球/奇点这样的不可逆的物质形态？", "Why is it that sometimes a large amount of data can be stored extremely densely, while other times this data forms irreversible material forms such as matter balls/singularities?"));
        t.lore(t.tr("为什么有时，数据的压缩率可以达到极限，而有时却无法被压缩？", "Why is it that sometimes the compression rate of data can reach its limit, while at other times it cannot be compressed?"));
        t.lore(t.tr("欢迎收看这一期的我爱发明《数据形态测试工厂》", "Welcome to this episode of I Love Invention: 'Data Form Testing Plant'"));

        t.heading(MainFunction);
        t.feature(t.tr("用于测试数据存储形态与压缩能力的数据形态测试工厂", "A data form testing plant used to test data storage forms and compression capabilities"));
        t.requirement(t.tr("根据配方输入物品，选择1号电路即可开始测试", "Select circuit 1 to start testing based on the recipe input items"));
        t.feature(t.tr("运行共分为两个阶段：测试阶段与分析阶段", "The operation is divided into two stages: the testing stage and the analysis stage"));

        t.heading(t.tr("测试阶段", "Testing Stage"));
        t.keepOrder();
        t.control(t.tr("测试阶段通过数据形式测试机ME接口向机器提供任意AE物质", "During this stage, provide any AE material to the machine through the Data Form Testing ME Interface"));
        t.feature(t.tr("提供的AE物质将被消耗；每次提供都会重置测试进度并增加碎片化等级", "Provided AE materials are consumed; each insertion resets testing progress and increases fragmentation"));
        t.requirement(t.tr("配方的数据密度容量决定测试阶段最多可接受的AE物质数据量", "The recipe's Data Density Capacity determines the maximum amount of AE material data accepted during testing"));
        t.feature(t.tr("碎片化等级决定分析阶段的耗时", "Fragmentation level determines the time consumption of the analysis stage"));
        t.formula(t.tr("每次写入时，碎片化等级将增加：类型增量+min(ceil(初始数据密度容量/本次实际写入字节)-1,60)", "Each time data is written, the fragmentation level will increase: type increase + min(ceil(initial capacity / bytes written this insertion) - 1, 60)"));
        t.feature(t.tr("本次实际写入不足1字节时，数量项固定为60", "If less than one byte is written, the amount term is fixed at 60"));
        t.feature(t.tr("若当前输入与上次输入相同，则类型增量为0", "If the current input is the same as the previous input, the type increase is 0"));
        t.feature(t.tr("若当前输入与之前所有的输入都不同，则类型增量为2；若当前输入与之前的输入中有相同的，则类型增量为15", "If the current input is different from all previous inputs, the type increase is 2; if the current input matches any previous input, the type increase is 15"));
        t.feature(t.tr("中断输入或是填满所有数据密度容量后，机器会自动进入分析阶段", "The machine automatically enters the analysis stage when input is interrupted or all data density capacity is filled"));

        t.heading(t.tr("分析阶段", "Analysis Stage"));
        t.feature(t.tr("根据测试结果，可以转化为数据存储研究点数，并写入存储数据支架中的数据晶体", "Based on the test results, it can be converted into data storage research points and written to the data crystal in the Storage Data Holder"));
        t.formula(t.tr("研究点数=floor(0.1×log₂(初始容量)²×已记录输入项目数×填充比例²)", "Data Storage research points = floor(0.1 × log₂(initial capacity)² × recorded input entries × fill ratio²)"));
        t.formula(t.tr("其中，填充比例=已写入字节/初始容量；已记录输入项目数以测试阶段机器记录为准", "Fill ratio = bytes written / initial capacity; recorded input entries are those tracked by the machine during testing"));
        t.requirement(t.tr("分析阶段耗时为(20+碎片化等级)秒，耗能与测试阶段相同", "The analysis stage takes (20 + fragmentation level) seconds and uses the same EU/t as the testing stage"));
    });

    public static final TipSpec NeutronIrradiationPartMachineTooltips = GTOTips.of("neutron_irradiation_part_machine", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("中子辐照室的辐照仓，每个部件提供16个独立辐照槽位", "An irradiation part for the Neutron Irradiation Chamber; each part provides 16 independent irradiation slots"));
        t.feature(t.tr("每个槽位只能放置1个物品；放入物品后自动开始中子辐照，辐照完成后在原槽位输出产物", "Each slot holds one item stack; inserted items automatically match Neutron Irradiation recipes, and outputs replace them in the same slot when irradiation finishes"));
        t.requirement(t.tr("每个正在辐照的槽位每5tick消耗1eV中子通量，且配方的最低中子通量不满足时暂停辐照", "Each irradiating slot consumes 1eV of neutron flux every 5 ticks, and irradiation pauses if the recipe's minimum neutron flux is not met"));
        t.requirement(t.tr("每个辐照仓的中子通量上限为10,000keV（10MeV）", "Each irradiation part has a neutron flux limit of 10,000keV (10MeV)"));
    });

    public static final TipSpec NeutronIrradiationChamberTooltips = GTOTips.of("neutron_irradiation_chamber", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("无需电力，消耗中子源为辐照部件提供中子通量", "Requires no power; consumes neutron sources to provide neutron flux to irradiation parts"));
        t.feature(t.tr("每秒消耗输入中的中子源，并将其转化为中子通量，均分到每个辐照仓中", "Consumes neutron sources from input per second and converts them into neutron flux, evenly distributed to each irradiation part"));
        t.feature(t.tr("反中子源：石墨粉-1000eV，小堆石墨粉-250eV，小撮石墨粉-100eV；", "Flux reducers: graphite dust -1,000eV, small pile of graphite dust -250eV, tiny pile of graphite dust -100eV"));
        t.feature(t.tr("中子源：锑-铍源+10eV，钚-铍源+100eV，锎-252粒子源+1000eV", "Neutron sources: antimony-beryllium +10eV, plutonium-beryllium +100eV, californium-252 +1,000eV"));
        t.supported(t.tr("可安装中子传感器；传感器默认报告所有辐照仓中的最低通量，也可切换为平均通量，单位为MeV", "Install at most one Neutron Sensor; it reports the minimum flux among parts by default, or the average flux when switched, in MeV"));
    });

    public static final TipSpec AlloySmelterTooltips = GTOTips.of("alloy_blast_smelter", t -> {
        t.heading(AfterModuleInstallation);
        t.bonus(t.tr("运行速度翻倍", "The running speed doubles"));
    });

    public static final TipSpec DissolvingTankTooltips = GTOTips.of("dissolving_tank", t -> {
        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("必须保证输入的流体与配方流体比例相同，否则无产物输出", "Must ensure the ratio of input fluid to recipe fluid is the same, otherwise no product output"));

        t.heading(AfterModuleInstallation);
        t.bonus(t.tr("模块将帮助机器自动进行原料配比，无上述条件限制", "The module will help the machine automatically match the raw materials, without the above conditions"));
    });

    public static final TipSpec kuangbiaoGiantNuclearFusionReactorTooltips = GTOTips.of("kuangbiao_giant_nuclear_fusion_reactor", t -> {
        t.heading(AfterModuleInstallation);
        t.feature(t.tr("模块分为两种：高能模块与超频模块", "There are two types of modules: high-energy modules and overclock modules"));
        t.bonus(t.tr("每多安装一个高能模块，反应堆热容量提升一倍", "For each additional high-energy module installed, the reactor's heat capacity is doubled"));
        t.requirement(t.tr("高能模块逐级搭建：每一级搭建在上一级模块上，缺少前一级时后续模块不生效", "High-energy modules are built level by level: each level is built onto the previous one, and later modules take no effect without the previous level"));
        t.bonus(t.tr("高能模块总计可提升四次热容量", "High-energy modules can increase heat capacity a total of four times"));
        t.supported(t.tr("超频模块允许安装超频仓/线程仓", "Overclock modules allow the installation of overclocking chambers/thread chambers"));
        t.requirement(t.tr("超频模块仅允许安装一个", "Only one overclock module is allowed to be installed"));
        t.control(t.tr("可在结构配置中选择要预览或搭建的模块", "Modules to preview or build can be selected in the structure configuration"));
    });

    public static final TipSpec KuangbiaoGiantNuclearFusionReactorEnergyStorageTooltip(long eut) {
        return GTOTips.of("kuangbiao_giant_nuclear_fusion_reactor_energy_storage", t -> t.statText(Tx.join(
                Component.translatable("gtceu.machine.fusion_reactor.capacity", eut),
                Tx.rainbowFast(t.tr(" [可安装模块扩容]", " [can be expanded by installing modules]")))));
    }

    public static final TipSpec SpaceStationDockingModule = GTOTips.of("space_station_docking_module", t -> {
        t.attention(t.tr("枢纽有侧接与后接两种形态，在结构配置中选择要预览或搭建的形态", "The hub has a side form and a rear form; select the form to preview or build in the structure configuration"));
        t.requirement(t.tr("必须搭建其中一种形态才能成型，两种形态的对接口位置不同", "One of the forms must be built for the structure to form; the two forms have different connector positions"));
    });

    public static final TipSpec LargeAlgaeFarmTooltips = GTOTips.of("large_algae_farm", t -> {
        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("耗能：(电压等级对应电压/2) EU/t", "Energy consumption: (voltage level corresponding voltage / 2) EU/t"));
        t.attention(t.tr("每种藻类每次繁殖需要消耗1mb/个体/秒的生物质，请确保输入总线提供足够的生物质，否则藻类可能会死亡", "Each type of algae requires 1mb/individual/second of biomass for each reproduction. Please ensure that the input bus provides enough biomass, otherwise the algae may die"));

        t.heading(t.tr("藻类生长机制", "Algae Growth Mechanism"));
        t.feature(t.tr("每秒更新一次藻类生长状态", "Updates algae growth status once per second"));
        t.feature(t.tr("每次更新，藻类种群会根据其环境最大容量与种群权重呈S型增长", "With each update, the algae population grows in an S-curve based on its environmental maximum capacity and population weight"));
        t.attention(t.tr("注意：每种藻类仅对其互补颜色的光源有最大提升效果", "Note: Each type of algae only has the maximum enhancement effect on its complementary color light source"));
        t.formula(t.tr("公式：增长量 = x(cap-x)(1-f)/(x+f(cap-x))", "Formula: Growth amount = x(cap-x)(1-f)/(x+f(cap-x))"));
        t.feature(t.tr("其中x为当前种群数量", "where x is the current population"));
        t.formula(t.tr("cap决定环境最大容量,其值为(4^玻璃等级)*藻类权重", "cap determines the environmental maximum capacity, its value is (4^[glass level])*[algae weight]"));
        t.formula(t.tr("f为藻类的增长因子（越接近0越快），其值为0.1+0.9*e^(-(电压等级 + 1.0) * 藻类吸光/2)", "f is the growth factor of algae(the closer to 0, the faster), where its value is 0.1+0.9*e^(-([voltage level] + 1.0) * [algae light absorption]/2)"));

        t.heading(t.tr("光吸收与权重机制", "Light absorption & weight mechanics"));
        t.keepOrder();
        t.feature(t.tr("藻类生长速度受环境光照强度影响", "Algae growth rate is affected by environmental light intensity"));
        t.feature(t.tr("每种颜色的卤素灯可以为对应波长范围的藻类提供额外光照，提升其种群权重", "Each color of halogen lamp can provide additional illumination for algae in the corresponding wavelength range, increasing its population weight"));
        t.control(t.tr("向输入总线提供红/绿/蓝三种卤素灯以提升光照强度", "Provide red/green/blue halogen lights to the input bus to enhance light intensity"));
        t.requirement(t.tr("每种颜色的卤素灯最多安装16个", "A maximum of 16 halogen lights of each color can be installed"));
        t.formula(t.tr("光照强度 = min( min( 红色卤素灯数量,16 ) + min( 绿色卤素灯数量,16 ) + min( 蓝色卤素灯数量,16 ),16)", "Light intensity = min( min( redHalogenLampCount,16 ) + min( greenHalogenLampCount,16 ) + min( blueHalogenLampCount,16 ),16 )"));
        t.formula(t.tr("每次更新先按红/绿/蓝三色累计吸收：藻类的单色光吸收率 = (单色吸收数据(列于下表) / 255) * 单色光占比", "In each update, first accumulate absorption by red/green/blue: Algae's monochromatic light absorption rate = (monochromatic absorption data (listed in the table below) / 255) * colorWeight"));
        t.feature(t.tr("单色光占比为当前卤素灯数量占所有输入的卤素灯数量的比例", "colorWeight is the proportion of the current halogen lamp count to the total input halogen lamp count"));
        t.formula(t.tr("每种藻类的权重 = max( 红色光吸收率, 绿色光吸收率, 蓝色光吸收率 )，用于决定环境容量的占比：cap = 4^玻璃等级 * 权重", "Algae weight = max( redRatio, greenRatio, blueRatio ), used to determine the proportion of environmental capacity: cap = 4^[glass level] * weight"));
        t.formula(t.tr("当前光吸收值 = (r/255*红色光吸收率 + g/255*绿色光吸收率 + b/255*蓝色光吸收率) * (光照强度 / 16)", "Current light absorption = (r/255*redRatio + g/255*greenRatio + b/255*blueRatio) * (lightIntensity / 16)"));
        t.feature(t.tr("该吸收值用于决定藻类的增长因子f", "This absorption value is used to determine the growth factor f of algae"));

        t.heading(t.tr("藻类卤素灯光波段吸收数据", "Algae Halogen Lamp Light Wavelength Absorption Data"));
        t.keepOrder();
        t.feature(t.tr("可养殖的藻类为：红藻、褐藻、金藻、绿藻、蓝藻", "The cultivable algae are: red algae, brown algae, golden algae, green algae, blue algae"));
        t.control(t.tr("使用专用的藻类访问仓来收集或投放藻类", "Use a dedicated algae access hatch to collect or release algae"));
        for (Algae algae : Algae.values()) {
            Component colorName = switch (algae) {
                case RedAlgae -> t.tr("红藻", "Red");
                case BrownAlgae -> t.tr("褐藻", "Brown");
                case GoldAlgae -> t.tr("金藻", "Golden");
                case GreenAlgae -> t.tr("绿藻", "Green");
                case BlueAlgae -> t.tr("蓝藻", "Blue");
            };
            t.feature(Tx.join(Tx.color(colorName, algae.getColor()),
                    Tx.lit("r:" + algae.redAbsorption + " g:" + algae.greenAbsorption + " b:" + algae.blueAbsorption)));
        }
    });

    public static final TipSpec LargeSteamCrackerTooltips = GTOTips.of("large_steam_cracker", t -> {
        t.penalty(t.tr("原料效率仅正常裂化机的40%", "The raw material efficiency is only 40% of that of a normal cracker"));
        t.bonus(t.tr("每使用高一等级的蒸汽输入仓，配方产出提升100mb", "For each higher level of steam input hatch used, the output increases by 100mb"));
    });

    public static final TipSpec ManaFlowAssemblerTooltips = GTOTips.of("mana_flow_assembler", t -> {
        t.lore(t.tr("原始人的泰拉凝聚板", "The original person's Terra Condenser Plate"));
        t.keepOrder();
        t.feature(t.tr("该机器利用四角魔力池内的魔力流来运行", "This machine operates using the mana flow in the quadrilateral mana pool"));
        t.feature(t.tr("魔力流的强度取决于四角魔力池上方魔力水晶的等级相应提供量之和", "The strength of the mana flow depends on the sum of the levels of the mana crystals above the quadrilateral mana pool"));
        t.feature(t.tr("每种魔力水晶强度：", "Mana crystal strength: "));
        t.statText(t.tr("魔力水晶：提供§b8§rMana/t", "Mana Crystal: provides §b8§r Mana/t"));
        t.statText(t.tr("自然水晶：提供§b32§rMana/t", "Natura Crystal: provides §b32§r Mana/t"));
        t.statText(t.tr("精灵水晶：提供§b128§rMana/t", "Alfsteel Crystal: provides §b128§r Mana/t"));
        t.statText(t.tr("盖亚水晶：提供§b512§rMana/t", "Gaia Crystal: provides §b512§r Mana/t"));
        t.control(t.tr("向合成台上投掷物品以输入，输出产物将以同样的方式投掷出来", "Throw items onto the crafting station for input, and the output products will be thrown out in the same way"));
        t.requirement(t.tr("只有前9个掉落物会被作为输入进行处理", "Only the first 9 dropped items will be processed as input"));
        t.feature(t.tr("机器总是会以可使用的最大魔力强度运行,且配方时间固定为10秒", "The machine will always operate at the maximum mana strength available, and the recipe time is fixed at 10 seconds"));
        t.requirement(t.tr("且以此魔力强度计算配方时长(总魔力消耗量/魔力强度)，超过10秒的配方无法运行", "And the recipe time is calculated based on this mana strength (total mana consumption / mana strength), recipes that exceed 10 seconds cannot run"));
        t.unsupported(t.tr("无法运行电力配方", "Cannot run recipes that require EU"));
    });

    public static final TipSpec magneticFluidGeneratorTooltips = GTOTips.of("magnetic_fluid_generator", t -> {
        t.heading(TipSlot.REQUIREMENTS, RunningRequirements);
        t.requirement(t.tr("玻璃等级限制了能量输出仓等级", "The glass tier limits the energy output hatch tier"));
        t.feature(t.tr("实际产出由等离子热值决定", "Actual output is determined by plasma heat value"));
        t.statText(t.tr("基础并行：64", "Basic parallel: 64"));
        t.supported(t.tr("可使用激光源仓或256A及以上的无线动力仓输出能量", "Energy can be output through Laser Source Hatches or wireless dynamo hatches rated at 256A or higher"));

        t.heading(EfficiencyBonus);
        t.bonus(t.tr("如果密封外壳等级大于LuV，则提升效率 x 密封外壳等级/4", "If the hermetic casing tier is greater than LuV, efficiency is increased by x (hermetic casing tier) / 4"));
        t.bonus(t.tr("如果使用激光源仓，仓室等级每超过LV一级，提升并行 x 2", "If a Laser Source Hatch is used, each level above LV gives parallel x 2"));

        t.heading(AfterModuleInstallation);
        t.bonus(t.tr("提升效率 x 2", "Efficiency is increased by x 2"));
        t.bonus(t.tr("基础并行 x 4", "Basic parallel x 4"));
        t.bonus(t.tr("激光源仓的并行加成提升为 x 4", "The Laser Source Hatch parallel bonus is increased to x 4"));
    });

    public static final TipSpec dysonSphereReceivingStationTooltips = GTOTips.of("dyson_sphere_receiving_station", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("发射戴森球模块后开始工作", "Starts working after launching Dyson Sphere modules"));
        t.requirement(t.tr("需要输入极寒之凛冰作为冷却剂", "Requires Gelid Cryotheum as a coolant"));
        t.feature(t.tr("产能功率，和需求算力由发射的模块数量决定", "Power capacity and demand computing power are determined by the number of launched modules"));
        t.bonus(t.tr("每次发射可使功率增加1A MAX", "Each launch can increase power by 1A MAX"));

        t.heading(t.tr("损坏机制", "Damage Mechanics"));
        t.warning(t.tr("每次运行都有(模块数量/128 + 1)%的概率损坏一次模块", "Each run has a (Module Count / 128 + 1)% chance to damage a module"));
        t.penalty(t.tr("当损坏高于60%时，输出效率随损坏值由100%逐渐降低到20%，并输出随损坏值增强的红石信号", "When damage exceeds 60%, output efficiency gradually decreases from 100% to 20% with damage value, and outputs a redstone signal enhanced by the damage value"));
        t.penalty(t.tr("当损坏达到100%时减少一次模块发射数量，并重置损坏值", "When damage reaches 100%, it reduces the number of module launches by one and resets the damage value"));
        t.feature(t.tr("在损坏值高于60%时发射不会增加发射次数，但会重置损坏值", "When damage value is above 60%, launching will not increase the launch count but will reset the damage value"));
    });

    public static final TipSpec virtualIngredientProviderTooltips = GTOTips.of("virtual_ingredient_provider", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("为ME网络提供虚拟成分", "Provides virtual ingredients for the ME network"));
        t.bonus(t.tr("虚拟成分可用于替代样板中不消耗的物品与流体", "Virtual ingredients can replace non-consumable items and fluids in patterns"));
        t.control(t.tr("放入物品、流体或已配置的虚拟提供器，即可向ME网络供应对应的虚拟成分", "Place items, fluids, or configured virtual providers into the machine to supply virtual ingredients"));
    });

    public static final TipSpec BeamGeneratorTooltips = GTOTips.of("beam_generator", t -> {
        t.heading(MainFunction);
        t.keepOrder();
        t.feature(t.tr("根据运行的配方发射高能光束射线", "Emits high-energy beam rays based on the running recipe"));
        t.feature(t.tr("超频规则：电压每乘以4，光强等级乘以2", "Overclocking rule: For every 4 times increase in voltage, the light intensity level increases by 2 times"));
        t.control(t.tr("在机器gui中调节射线的水平角与俯仰角", "Adjust the horizontal and pitch angles of the beam in the machine GUI"));
        t.penalty(t.tr("光束射线在非超净间的环境中，每经过一格，光强都会乘以0.95", "In a non-cleanroom environment, the light intensity of the beam will be attenuated by a factor of 0.95 per block"));
    });

    public static final TipSpec BeamRedirectorTooltips = GTOTips.of("beam_redirector", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于改变高能光束射线的方向", "Used to change the direction of high-energy beam rays"));
        t.control(t.tr("在机器GUI中调节射线的水平角与俯仰角", "Adjust the horizontal and pitch angles of the beam in the machine GUI"));
    });

    public static final TipSpec ExcitationCrystalTooltips = GTOTips.of("excitation_crystal", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("如果它周围有晶体激发器，它将会被激发并使穿过的高能光束射线的光强翻倍", "If there are crystal exciters around it, it will be excited and double the light intensity of the high-energy beam rays passing through it"));
        t.requirement(t.tr("该过程需要(光强 * 4096EU/t)的能量", "This process requires (light intensity * 4096 EU/t) of energy"));
        t.requirement(t.tr("并且激发功率受到晶体激发器的等级限制（至多2A*机器等级）", "And the excitation power is limited by the level of the crystal exciter (up to 2A * machine level)"));
    });

    public static final TipSpec CrystalExciterTooltips = GTOTips.of("crystal_exciter", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("用于激发周围的晶体", "Used to excite the surrounding crystals"));
        t.requirement(t.tr("需要消耗能量来激发晶体", "Requires energy consumption to excite the crystals"));
    });

    public static final TipSpec BeamSemiReflectorTooltips = GTOTips.of("beam_semi_reflector", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("将穿过的高能光束射线分为两路，一路继续前进，一路被反射", "Splits the high-energy beam rays passing through into two paths, one continues forward, and the other is reflected"));
        t.control(t.tr("在机器GUI中调节射线的反射率", "Adjust the reflectivity of the beam in the machine GUI"));
    });

    public static final TipSpec BeamPolarizerTooltips = GTOTips.of("beam_polarizer", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("改变穿过的高能光束射线的振动方向", "Changes the vibration direction of the high-energy beam rays passing through"));
        t.feature(t.tr("旋光角度由机器内的流体决定", "The angle of rotation is determined by the fluid inside the machine"));
    });

    public static final TipSpec BeamAccessHatchTooltips = GTOTips.of("beam_access_hatch", t -> {
        t.heading(MainFunction);
        t.feature(t.tr("为机器提供导入精确的高能光束射线", "Provides precise high-energy beam rays for the machine"));
    });

    private GTOMachineTooltipsA() {}

    public static void init() {}
}
