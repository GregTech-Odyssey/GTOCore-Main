package com.gtocore.common.data.translation

import com.gtocore.api.data.Algae
import com.gtocore.api.lang.ComponentListSupplier
import com.gtocore.api.lang.ComponentSupplier
import com.gtocore.api.lang.toComponentSupplier
import com.gtocore.api.lang.toLiteralSupplier
import com.gtocore.api.lang.translatable
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.common.data.translation.ComponentSlang.AfterModuleInstallation
import com.gtocore.common.data.translation.ComponentSlang.EfficiencyBonus
import com.gtocore.common.data.translation.ComponentSlang.MainFunction
import com.gtocore.common.data.translation.ComponentSlang.RunningRequirements
import com.gtocore.common.machine.multiblock.electric.research.DataFormTestingPlantMachine

import net.minecraft.network.chat.Component

import appeng.api.config.PowerUnits
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.config.ConfigHolder
import com.gtolib.GTOCore

object GTOMachineTooltipsA : AutoInitialize<GTOMachineTooltipsA>() {

    @JvmField
    val ComputationalDataHolder: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("computational_data_holder")

        section(MainFunction)
        function("安装在计算机或超算中心等场所，用于读取算力流动的生产数据" translatedTo "Installed in computers or supercomputing centers, used to read production data of computational workload flow")
        function("每5秒，获得√(最大算力+当前使用中的算力)的计算研究点数，并存储于机器内的晶片中" translatedTo "Every 5 seconds, it generates √(maximum computational workload + currently used computational workload) computational research points, which are stored in the data crystal inside the machine")
    }

    @JvmField
    val ConnectingRodHatchTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("connecting_rod_hatch")

        section(MainFunction)
        function("用于将附着的生物组织的生物能量转化为可导出的能量" translatedTo "Used to convert the bioenergy of attached biological tissues into exportable energy")
    }

    @JvmField
    val BioOscillationGeneratorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("bio_oscillation_generator")

        section(MainFunction)
        function("将生物组织的生长与培养液转化为电能" translatedTo "Converts biological tissue growth and culture medium into electrical energy")
        command("生物组织与培养液均需要一次性装入机器；每次装填最多吸收64个组织与1,000,000,000mB培养液" translatedTo "Biological tissue and culture medium must be loaded into the machine at once; each filling accepts up to 64 tissues and 1,000,000,000mB of culture medium")
        command("组织从0点开始生长，依次经历幼年、成年、壮年和老年阶段；达到老年阶段上限后组织死亡并被清除" translatedTo "Tissue starts growing from 0 points and passes through Juvenile, Adult, Mature, and Elderly stages; it dies and is cleared after exceeding the Elderly stage limit")
        command("可以通过组织传感器监测组织的生长点数" translatedTo "Tissue growth points can be monitored through the Tissue Sensor")

        section(RunningRequirements)
        command("运行需要同时放入生物组织、匹配等级的培养液和连接杆；培养液等级与机加工控制模块等级均不得低于组织要求" translatedTo "Running requires biological tissue, culture medium of a matching tier, and a connecting rod; both the medium tier and machining control module tier must meet the tissue requirement")
        command("组织每5ticks生长一次；培养液营养可用率初始为100%，每秒降低0.1%，低于设定阈值后培养液会被清空并输出对应废液" translatedTo "Tissue grows every 5 ticks; culture medium starts at 100% nutrient availability and decreases by 0.1% per second, then is cleared and converted into its corresponding waste fluid below the configured threshold")
        command("连接杆每20ticks损耗1点耐久；连接杆材料等级越高，发电乘数越高" translatedTo "The connecting rod loses 1 durability every 20 ticks; higher-tier connecting rod materials provide a higher generation multiplier")

        section(EfficiencyBonus)
        info("基础发电量 = 组织等级^连接杆等级 × 组织数量 × 培养液数量 × 8EU/t" translatedTo "Base power = tissue tier^connecting rod tier × tissue amount × culture medium amount × 8EU/t")
        info("组织生长量 = 最大生长速率 × 营养可用率 ÷（饱和因子 + 营养可用率）/5ticks" translatedTo "Tissue growth = maximum growth rate × nutrient availability × (saturation factor + nutrient availability) per 5 ticks")
        increase("能量控制模块等级为2时发电量×1.2，等级为3时发电量×1.5" translatedTo "An energy control module tier of 2 multiplies power by 1.2, and tier 3 by 1.5")
        increase("通过电刺激模块对组织进行电刺激，可进一步提升发电量" translatedTo "Electrical stimulation of tissue through the electrical stimulation module can further increase power generation")
        info("组织阶段决定电刺激增幅；组织数量和培养液数量决定基础发电量，营养可用率主要影响组织生长" translatedTo "The tissue stage determines the stimulation boost; tissue amount and culture medium amount determine base power, while nutrient availability mainly affects tissue growth")
    }

    @JvmField
    val BioOscillationElectricStimulatorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("bio_oscillation_electric_stimulator")

        section(MainFunction)
        function("作为生物振荡发电机的模块，对其中的生物组织进行电刺激" translatedTo "Acts as a module for the Bio Oscillation Generator and electrically stimulates its biological tissue")
        command(translatable("gtocore.biooscillation.button.set.electrical.stimulation.desc", GTOCore.difficulty * 6))
        command("运行完成后消耗等量的组织生长点数，并按组织当前阶段的电刺激增幅提供临时发电加成" translatedTo "After completing, the stimulator consumes the corresponding amount of tissue growth points and grants a temporary power boost based on the tissue's current stage")

        section(RunningRequirements)
        command("模块必须连接至生物振荡发电机，且发电机内必须存在有效的生物组织" translatedTo "The module must be connected to a Bio Oscillation Generator containing valid biological tissue")
        command("100%强度时，一次电刺激消耗组织1000点；耗电量为组织数据中标注的电刺激能耗，低于100%时按比例减少" translatedTo "At 100% intensity, one stimulation consumes 1,000 tissue points; power consumption equals the tissue's listed stimulation energy cost and scales with lower intensities")

        section(EfficiencyBonus)
        increase("电刺激增幅取决于组织当前阶段；刺激点数越多，增幅持续时间越长，最多持续60秒" translatedTo "The stimulation boost depends on the tissue's current stage; more stimulation points provide a longer boost, up to 60 seconds")
        info("新的电刺激会累加剩余持续时间；新的增幅系数会覆盖旧的增幅系数" translatedTo "New stimulations add to the remaining duration; new boost coefficients overwrite old ones")
    }

    @JvmField
    val pulseMachineMaintenancePedestalTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("pulse_machine_maintenance_pedestal")

        section(MainFunction)
        command("当机器上方的脉冲核心收到一个强度不低于240的魔力脉冲时，尝试工作一次" translatedTo "When the pulse core above the machine receives a mana pulse with strength not less than 240, it will attempt to work once")
        command("工作时会尝试对附近的机器进行维护，或是从消声仓中消除4份灰尘" translatedTo "When working, it will attempt to maintain nearby machines or eliminate 4 units of dust from the muffler hatch")
        info("工作半径为12格，且每次工作仅随机维护一个机器或随机消除4份灰尘" translatedTo "The working radius is 12 blocks, and each time it works, it only randomly maintains one machine or randomly eliminates 4 units of dust")
        guide("这样的魔力脉冲可以通过魔力发射器安装魔力透镜：强度来发射" translatedTo "Such mana pulses can be emitted by installing a mana lens: Power on a mana blaster")
        guide("或是使用精灵等级及以上的魔力发射器" translatedTo "Or using a mana blaster of Alfhelm tier or above")
    }

    @JvmField
    val virtualCoinMinerTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("virtual_coin_miner")

        section(MainFunction)
        highlight("通过消耗算力来挖掘虚拟货币" translatedTo "Mines virtual coins by consuming computational workload")
        info("每提交一定的算力后，矿机将获得一个金币奖励" translatedTo "After submitting a certain amount of computational workload, the miner will receive a gold coin reward")
        command("每获得一次金币奖励，下一次奖励将需要提交更多的算力" translatedTo "Each time a gold coin reward is obtained, the next reward will require more computational workload to be submitted")

        section(RunningRequirements)
        command("运行需要每秒消耗20mB多氯联苯冷却剂" translatedTo "Consumes 20mB of PCB coolant per second while running")
        command("每提交的1CWU算力需要1920EU的能量支持" translatedTo "Each 1 CWU of computational workload submitted requires 1920 EU of energy support")
    }

    @JvmField
    val meInputBufferPartMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("me_input_buffer_part_machine")

        section(MainFunction)
        command("ME输入仓室的一种特殊模式，仅能使用样板进行配置" translatedTo "A special mode of the ME input hatch/bus, can only be configured using patterns")
        command("在该模式下，每个槽位使用样板配置一组特定的物品或流体，仓室将从ME网络提取对应的物品与流体" translatedTo "In this mode, each slot is configured with a pattern for a specific group of items or fluids, and the hatch/bus will extract the corresponding items and fluids from the ME network")
    }

    @JvmField
    val planetaryGasCollectorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("planetary_gas_collector")

        section(MainFunction)
        command("用于在行星表面收集大气中的气体(效率非常惊人)" translatedTo "Used to collect gases from the atmosphere on the surface of planets (with an amazing efficiency)")
        command("在地球建立的空间站能够收集到来自主世界、下界和末地的气体" translatedTo "In the space station built on Earth, gases from the Overworld, Nether, and End can be collected")
        command("在其他行星建立的空间站能够收集到该行星特有的大气气体" translatedTo "In the space station built on other planets, the unique atmospheric gases of that planet can be collected")
    }

    @JvmField
    val space3DPrinterTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("space_3d_printer")

        story("GTO的工程师们想到，只要在重力微乎其微的空间站中进行3D打印，就不用像在地球上那样，还要打印支架" translatedTo "GTO engineers thought that as long as 3D printing is done in a space station with minimal gravity, there is no need to print supports like on Earth")
        story("于是，空间站3D打印机诞生了" translatedTo "Thus, the space station 3D printer was born")
    }

    @JvmField
    val DataExportMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("data_export_machine")

        section(MainFunction)
        command("用于将科技树解锁的数据导出到闪存等存储设备中" translatedTo "Used to export data unlocked in the tech tree to flash drives and other storage devices")
        command("导出需要消耗7680EU/t与一定时间进行数据拷贝" translatedTo "Exporting requires consuming 7680 EU/t and a certain amount of time for data copying")
    }

    @JvmField
    val DataCenterTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("data_center")

        section(MainFunction)
        function("用于存储大量数据的多功能数据中心" translatedTo "A multifunctional data center for storing large amounts of data")
        function("同时，可以接受算力输入进行数据处理与研究" translatedTo "At the same time, it can accept computational workload input for data processing and research")
        command("接受算力的上限为16*2^(玻璃等级-6)CWU/t" translatedTo "The upper limit of computational workload accepted is 16*2^(glass level-6) CWU/t")
        command("每个数据仓的槽位与每个安装的配方数据分别额外提供0.01%和1%的算力输入上限（独立乘区）" translatedTo "Each data hatch slot and each installed recipe data additionally provides 0.01% and 1% of the computational workload input limit (independently multiplied)")
        info("等效上限公式：16*2^(玻璃等级-6)*(1+数据仓槽位数*0.0001)*(1+配方数据数*0.01)CWU/t" translatedTo "Equivalent upper limit formula: 16*2^(glass level-6)*(1+data hatch slot count*0.0001)*(1+recipe data count*0.01) CWU/t")
        guide("从机器UI的左侧访问科技树与便捷导出功能" translatedTo "Access the tech tree and convenient export functions from the left side of the machine UI")
        section(RunningRequirements)
        command("闲置时，每个数据/光学仓耗能为§f1920 EU/t§7。" translatedTo "When idle, each data/optical hatch consumes §f1920 EU/t§7.")
        command("连接时，每个已连接的数据/光学仓耗能为§f30,720 EU/t§7。" translatedTo "When connected, each connected data/optical hatch consumes §f30,720 EU/t§7.")
        command("处于研究状态时，电力消耗翻倍，并需要100mB/秒的多氯联苯冷却剂" translatedTo "When in research state, power consumption is doubled and requires 100mB/s of PCB coolant")
    }

    @JvmField
    val ScanStationMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("scan_station_machine")

        section(MainFunction)
        function("将存储在晶片中的数据进行解析，转化为团队共享的研究点数" translatedTo "Parse the data stored in the data crystal and convert it into team-shared research points")
        command("无论内部有多少数据，每张晶片的解析耗时与耗能均为固定值" translatedTo "Regardless of how much data is inside, the parsing time and energy consumption for each data crystal are fixed")
    }

    @JvmField
    val IntelligentScanningProxyTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("intelligent_scanning_me_proxy")

        section(MainFunction)
        function("连接ME网络，将其中的物品与流体提供给智能扫描管理平台" translatedTo "Connects to an ME network and provides its items and fluids to the Intelligent Scanning Management Platform")
        command("数据晶体不会作为扫描目标；带有标签的物品或流体按无标签的基础类型处理" translatedTo "Data crystals are excluded from scan targets; tagged items and fluids are treated as their untagged base types")
        info("只记录库存中的物品与储量超过1000mB的流体，并每2秒刷新一次" translatedTo "Records items and fluids stored above 1,000mB, refreshing every 2 seconds")
    }

    @JvmField
    val IntelligentScanningManagementPlatformMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("intelligent_scanning_management_platform")

        section(MainFunction)
        function("从ME网络消耗物品或流体进行扫描，并将扫描数据写入数据晶片" translatedTo "Consumes items or fluids from an ME network to scan them and write the scan data to a data crystal")
        command("扫描目标可以在机器GUI的扫描队列页面管理" translatedTo "Scan targets can be managed on the scan queue page of the machine GUI")
        section("工作模式：选定物品模式" translatedTo "Working Mode: Selected Item Mode")
        content("从界面选择多项物品或流体作为扫描目标" translatedTo "Select multiple items or fluids from the interface as scan targets")
        content("机器会试图重复扫描它们，直到填满数据晶片" translatedTo "The machine will attempt to repeatedly scan them until the data crystal is filled")
        content("选择多项组成材料相同的物品或流体时，机器只会扫描其中的一项" translatedTo "When selecting multiple items or fluids with the same composition, the machine will only scan one of them")
        section("工作模式：选定物品单次扫描模式" translatedTo "Working Mode: Selected Item Single Scan Mode")
        content("从界面选择多项物品或流体作为扫描目标" translatedTo "Select multiple items or fluids from the interface as scan targets")
        content("机器会试图扫描它们1次，然后停止扫描" translatedTo "The machine will attempt to scan them once, then stop scanning")
        section("工作模式：未学习物品模式" translatedTo "Working Mode: Unlearned Item Mode")
        content("扫描所有未学习的物品或流体，每种仅扫描一个目标" translatedTo "Scans all unlearned items or fluids, only one target of each type is scanned")
        content("机器会持续检测并将新增的未学习物品或流体加入扫描队列" translatedTo "The machine will continuously detect and add newly unlearned items or fluids to the scan queue")
        section("工作模式：未学习物品单次扫描模式" translatedTo "Working Mode: Unlearned Item Single Scan Mode")
        content("扫描所有未学习的物品或流体，每种仅扫描一个目标" translatedTo "Scans all unlearned items or fluids, only one target of each type is scanned")
        content("在未找到新的未学习物品或流体后，机器将停止扫描" translatedTo "The machine will stop scanning after no new unlearned items or fluids are found")
        section(RunningRequirements)
        important("扫描使用的晶片必须额外输入到机器中，且每次扫描消耗1个晶片" translatedTo "The data crystal used for scanning must be additionally input into the machine, and each scan consumes one data crystal")
        command("扫描物品时花费1个物品，扫描流体时花费1,000mB流体" translatedTo "Scanning an item consumes 1 item, scanning a fluid consumes 1,000mB of fluid")
        info("每次运行持续%s秒，耗能为8×本次扫描的数据字节数+8 EU/t".translatedWithArgs("Each operation lasts %s seconds and uses 8 × scanned data bytes + 8 EU/t", GTOCore.difficulty * 10))
        info("同一种材料每次运行只扫描一个目标" translatedTo "Only one target of the same material is scanned per operation")
    }

    @JvmField
    val CatalystDataHolder: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("catalyst_data_holder")

        section(MainFunction)
        function("安装在化工厂等场所，用于监测使用催化剂的反应" translatedTo "Installed in chemical plants and other places, used to monitor reactions that use catalysts")
        function("每次机器运行包含催化剂的反应时，催化剂数据仓将记录该次配方的总效率" translatedTo "Each time the machine runs a reaction that contains a catalyst, the catalyst data hatch will record the total efficiency of that recipe")
        info("总效率为配方时间乘以能耗与标准配方时间与能耗的比值" translatedTo "Total efficiency is the product of recipe time and energy consumption divided by the standard recipe time and energy consumption")
        command("配方完成后，记录并产生(总效率×配方并行数×原始配方等级)的催化研究点数，并存储于机器内的晶片中" translatedTo "After the recipe is completed, the catalyst data hatch will record and generate (total efficiency × recipe parallelism × original recipe level) catalyst research points, which will be stored in the data crystal inside the machine")
    }

    @JvmField
    val EnergyDataHolder: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("energy_data_holder")

        section(MainFunction)
        function("安装在能源产生相关的机器上，对能源稳定性与功率进行监测" translatedTo "Installed on energy generation-related machines to monitor energy stability and power")
        command(
            Component.translatable("gtocore.lang.energy_data_holder.2", 15 + 15 * GTOCore.difficulty)
                .toComponentSupplier(),
        )
        command("此后每次机器完成输出UEV级及以上功率的配方时，能源数据仓将转化(功率等级 - 10)²的能源研究点数，并存储于机器内的晶片中" translatedTo "Thereafter, each time the machine finishes recipes that output UEV-level or higher power, the energy data hatch will convert (power level - 9)² energy research points and store them in the data crystal inside the machine")
    }

    @JvmField
    val ThermaldynamicsDataHolder: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("thermodynamic_data_holder")

        section(MainFunction)
        function("配合热力学分析平台使用，用于热力学相关的研究" translatedTo "Used in conjunction with the thermodynamic analysis platform for thermodynamics-related research")
    }

    @JvmField
    val ThermodynamicAnalysisPlatformMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("thermodynamic_analysis_platform_machine")

        story("一名热力膨胀的前员工阐述到，他之前发现，热力膨胀公司的业务领域涉及到机械，材料，装备等方方面面，但在热力学研究方面却投入甚少" translatedTo "A former employee of Thermal Expansion stated that he previously found that Thermal Expansion's business areas involved machinery, materials, equipment, and many other aspects, but invested very little in thermodynamics research")
        story("(而且热力膨胀公司近几年好像还在深耕神秘学领域，并且进展甚微)" translatedTo "(Moreover, Thermal Expansion seems to have been delving into the field of thaumaturgy in recent years, with little progress)")
        story("于是他带着他自己研发的热力学分析平台从热力膨胀公司跳槽到GTO，并在GTO的支持下，成立了热力学研究所" translatedTo "So he left Thermal Expansion with his own developed thermodynamic analysis platform and, with GTO's support, established the Thermodynamics Research Institute")
        section(MainFunction)
        function("用于监测热力学相关的实验" translatedTo "Used to monitor thermodynamics-related experiments")
        info("机器结构中，可见有两个热力学封闭体系，分别为§b低温体系§r与§c高温体系§r" translatedTo "In the machine structure, there are two independent thermodynamic closed systems, namely §blow-temperature system§r and §chigh-temperature system§r")
        info("当两边体系温差达到热平衡时（温差不超过10K即可计入），热力学分析平台将开始进行热力学分析" translatedTo "When the temperature difference between the two systems reaches thermal equilibrium(<10K), the thermodynamic analysis platform will begin thermodynamic analysis")
        info("分析持续30秒，期间可以通过对应的热传导仓向两边体系输入热量以调节温差" translatedTo "The analysis lasts for 30 seconds, during which heat can be input to both systems through the corresponding thermal conduction hatches to adjust the temperature difference")
        command("分析结束时，两侧体系的温差越大，产生的热力学研究点数越多" translatedTo "At the end of the analysis, the greater the temperature difference between the two systems, the more thermodynamic research points are generated")
        command("每次分析产生的热力学研究点数 = (温差/250K)" translatedTo "The thermodynamic research points generated by each analysis = (temperature difference / 250K)")

        section(RunningRequirements)
        command("分析期间，热力学分析平台每秒消耗§f30,720 EU/t§r" translatedTo "During the analysis, the thermodynamic analysis platform consumes §f30,720 EU/t§r per second")
    }

    @JvmField
    val LaserComputationTestingPlatformMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("laser_computation_testing_platform_machine")

        story("光子计算是GTO集团近年重点研究的方向之一" translatedTo "Photon computation is one of the key research directions of GTO Group in recent years")
        story("攻克了它，便能使设备的计算与处理能力达到前所未有的高度" translatedTo "Overcoming it will enable the computing and processing capabilities of devices to reach unprecedented heights")
        story("还能让各种杂碎的产线也能得到驾驭超高能的能力，生产力获得史诗级的飞跃" translatedTo "It can also allow various miscellaneous production lines to gain the ability to harness ultra-high energy, resulting in an epic leap in productivity")

        section(MainFunction)
        content("机制施工中..." translatedTo "Mechanism under construction...")
    }

    @JvmField
    val DataHolderUniversal: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("data_holder_universal")

        section("数据支架仓" translatedTo "About Data Holder")
        info("这种仓室可以收集不同来源的数据，并转化为研究点数存储于晶片中" translatedTo "This hatch can collect data from different sources and convert it into research points stored in the data crystal")
        info("安装到对应的机器上以生效" translatedTo "Install it on the corresponding machine to take effect")
    }

    @JvmField
    val directedHyperCubeMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("directed_hyper_cube_machine")

        section(MainFunction)
        highlight("代理多个流体或物品存储器，且指定代理方向" translatedTo "Proxy (a or multi) (fluid or item or both)storage with directed sides")
        command("使用§b坐标标签枪§r按照分配顺序绑定方块" translatedTo "Use the §bTesseract Target Marker§r to bind blocks in allocation order")
        section("被样板供应器推送时" translatedTo "When being pushed by the Pattern Provider")
        function("将样板供应器的样板内容按照编写顺序依次输出到多个方块的多个面" translatedTo "Outputs the pattern contents of the Pattern Provider to multiple sides of multiple blocks in the order written")
        command("原料在样板中对应的编号严格对应绑定方块的编号" translatedTo "The number corresponding to the raw material in the pattern strictly corresponds to the number of the bound block")
        command("若样板内容的长度大于绑定的方块数量，则该样板将拒绝被推送" translatedTo "If the length of the pattern content is greater than the number of bound blocks, the pattern will refuse to be pushed")
        guide("适用于一些较为复杂的自动化场景（如新生魔艺的附魔装置自动化）" translatedTo "Suitable for some more complex automation scenarios (such as Ars Nouveau's Enchanting Apparatus)")
    }

    @JvmField
    val meEnergySubstationTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("me_energy_substation")

        section(MainFunction)
        ok("为ME网络提供额外的能量供应" translatedTo "Provides additional energy supply for the ME network")
        command(
            ("每一点EU可以转换成 " translatedTo "Each point of EU can be converted into ") +
                PowerUnits.FE.convertTo(PowerUnits.AE, ConfigHolder.INSTANCE.compat.energy.euToFeRatio.toDouble())
                    .toLiteralSupplier() +
                (" 点AE能量" translatedTo " points of AE energy"),
        )
        info("使用ME能量访问仓导出能量到ME网络" translatedTo "Use the ME Energy Access Hatch to export energy to the ME network")
        increase("玻璃等级每级可将转换效率提升30%" translatedTo "Each glass level can increase the conversion efficiency by 30%")
        section(AfterModuleInstallation)
        increase("安装模块可使转换效率额外x2" translatedTo "Installing modules can further double the conversion efficiency")
    }

    @JvmField
    val spaceBioResearchModuleTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("space_bio_research_module")

        section(MainFunction)
        command("用于在空间站内进行生物研究" translatedTo "Used for biological research in the space station")
        important("生物研究的运行结束时辐射剂量指的是，配方的最后一刻时，机器需要满足的辐射剂量要求" translatedTo "The radiation dose at the end of biological research refers to the radiation dose requirement that the machine needs to meet at the last moment of the recipe")
        error("生物研究配方仅采用常规超频模式运行" translatedTo "Biological research recipes only run in normal overclocking mode")
        command("超净间环境等级由环境维护舱决定" translatedTo "The cleanroom environment level is determined by the Environmental Maintenance Module")
        info("机器本体可以为各种配方提供可调节的0~80Sv背景辐射环境" translatedTo "The machine body can provide an adjustable 0~80Sv background radiation environment for various recipes")
    }

    @JvmField
    val spaceElevatorConnectorModuleTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("space_elevator_connector_module")

        command("与当前星球的太空电梯连接" translatedTo "Connects to the space elevator of the current planet")
        increase(
            "连接后，空间站各运行模块（如轨道冶炼舱等）可获得(0.9^n)×的耗时减免，n为太空电梯的动力模块等级" translatedTo
                "After connecting, each operating module of the space station (such as orbital smelting chamber, etc.) can get a time reduction of (0.9^n)×, where n is the power module level of the space elevator",
        )
        increase(
            "太空电梯安装的模块也将获得额外(0.9^(n/2))×的耗时减免" translatedTo
                "Modules installed on the space elevator will also receive a time reduction of (0.9^(n/2))×",
        )
        increase(
            "如果安装了高能转换调配舱，则底数0.9将变为0.8" translatedTo
                "If a high-energy conversion and allocation chamber is installed, the base 0.9 will become 0.8",
        )
        decrease(
            Component.translatable(
                "gtocore.lang.space_elevator_connector_module.3",
                50.0 + 150.0 * GTOCore.difficulty,
            ).toComponentSupplier(),
        )

        command("该模块仅能连接在其他模块的下方" translatedTo "This module can only connect below other modules")
    }

    @JvmField
    val SpaceElevatorEngineeringDataModule: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("space_elevator_engineering_data_module")

        section(MainFunction)
        command("收集太空电梯其他模块的运行数据" translatedTo "Collects operational data from other modules of the space elevator")
        command(
            Component.translatable(
                "gtocore.lang.space_elevator_engineering_data_module.2",
                if (GTOCore.isExpert()) 75 else 50,
            ).toComponentSupplier(),
        )
        command("转化为的研究点数将存储于通用数据仓的晶片中" translatedTo "The converted research points will be stored in the data crystal of the universal data hatch")
        increase("太空电梯动力模块等级为n时，每次收集需要的运行次数乘以(4/(3+n))" translatedTo "When the power module level of the space elevator is n, the number of runs needed for each collection is multiplied by (4/(3+n))")
        increase("连接到空间站时，转化为的研究点数翻倍" translatedTo "When connected to the space station, the converted research points are doubled")
    }

    @JvmField
    val DataFormTestingPlantMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("data_form_testing_plant_machine")

        story("为什么有时，大量的数据就能够被极其密集的存储，而有时这些数据却形成了物质球/奇点这样的不可逆的物质形态？" translatedTo "Why is it that sometimes a large amount of data can be stored extremely densely, while other times this data forms irreversible material forms such as matter balls/singularities?")
        story("为什么有时，数据的压缩率可以达到极限，而有时却无法被压缩？" translatedTo "Why is it that sometimes the compression rate of data can reach its limit, while at other times it cannot be compressed?")
        story("欢迎收看这一期的我爱发明《数据形态测试工厂》" translatedTo "Welcome to this episode of I Love Invention: 'Data Form Testing Plant'")
        section(MainFunction)
        function("用于测试数据存储形态与压缩能力的数据形态测试工厂" translatedTo "A data form testing plant used to test data storage forms and compression capabilities")
        info("根据配方输入物品，选择1号电路即可开始测试" translatedTo "Select circuit 1 to start testing based on the recipe input items")
        info("运行共分为两个阶段：测试阶段与分析阶段" translatedTo "The operation is divided into two stages: the testing stage and the analysis stage")
        section("测试阶段" translatedTo "Testing Stage")
        content("测试阶段通过数据形式测试机ME接口向机器提供任意AE物质" translatedTo "During this stage, provide any AE material to the machine through the Data Form Testing ME Interface")
        content("提供的AE物质将被消耗；每次提供都会重置测试进度并增加碎片化等级" translatedTo "Provided AE materials are consumed; each insertion resets testing progress and increases fragmentation")
        command("配方的数据密度容量决定测试阶段最多可接受的AE物质数据量" translatedTo "The recipe's Data Density Capacity determines the maximum amount of AE material data accepted during testing")
        command("碎片化等级决定分析阶段的耗时" translatedTo "Fragmentation level determines the time consumption of the analysis stage")
        info("每次写入时，碎片化等级将增加：类型增量+min(ceil(初始数据密度容量/本次实际写入字节)-1,60)" translatedTo "Each time data is written, the fragmentation level will increase: type increase + min(ceil(initial capacity / bytes written this insertion) - 1, 60)")
        info("本次实际写入不足1字节时，数量项固定为60" translatedTo "If less than one byte is written, the amount term is fixed at 60")
        info("若当前输入与上次输入相同，则类型增量为0" translatedTo "If the current input is the same as the previous input, the type increase is 0")
        info("若当前输入与之前所有的输入都不同，则类型增量为2；若当前输入与之前的输入中有相同的，则类型增量为15" translatedTo "If the current input is different from all previous inputs, the type increase is 2; if the current input matches any previous input, the type increase is 15")
        guide("中断输入或是填满所有数据密度容量后，机器会自动进入分析阶段" translatedTo "The machine automatically enters the analysis stage when input is interrupted or all data density capacity is filled")
        section("分析阶段" translatedTo "Analysis Stage")
        command("根据测试结果，可以转化为数据存储研究点数，并写入存储数据支架中的数据晶体" translatedTo "Based on the test results, it can be converted into data storage research points and written to the data crystal in the Storage Data Holder")
        info("研究点数=floor(0.1×log₂(初始容量)²×已记录输入项目数×填充比例²)" translatedTo "Data Storage research points = floor(0.1 × log₂(initial capacity)² × recorded input entries × fill ratio²)")
        info("其中，填充比例=已写入字节/初始容量；已记录输入项目数以测试阶段机器记录为准" translatedTo "Fill ratio = bytes written / initial capacity; recorded input entries are those tracked by the machine during testing")
        command("分析阶段耗时为(20+碎片化等级)秒，耗能与测试阶段相同" translatedTo "The analysis stage takes (20 + fragmentation level) seconds and uses the same EU/t as the testing stage")
    }

    @JvmField
    val NeutronIrradiationPartMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("neutron_irradiation_part_machine")

        section(MainFunction)
        function("中子辐照室的辐照仓，每个部件提供16个独立辐照槽位" translatedTo "An irradiation part for the Neutron Irradiation Chamber; each part provides 16 independent irradiation slots")
        content("每个槽位只能放置1个物品；放入物品后自动开始中子辐照，辐照完成后在原槽位输出产物" translatedTo "Each slot holds one item stack; inserted items automatically match Neutron Irradiation recipes, and outputs replace them in the same slot when irradiation finishes")
        content("每个正在辐照的槽位每5tick消耗1eV中子通量，且配方的最低中子通量不满足时暂停辐照" translatedTo "Each irradiating slot consumes 1eV of neutron flux every 5 ticks, and irradiation pauses if the recipe's minimum neutron flux is not met")
        command("每个辐照仓的中子通量上限为10,000keV（10MeV）" translatedTo "Each irradiation part has a neutron flux limit of 10,000keV (10MeV)")
    }

    @JvmField
    val NeutronIrradiationChamberTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("neutron_irradiation_chamber")

        section(MainFunction)
        function("无需电力，消耗中子源为辐照部件提供中子通量" translatedTo "Requires no power; consumes neutron sources to provide neutron flux to irradiation parts")
        command("每秒消耗输入中的中子源，并将其转化为中子通量，均分到每个辐照仓中" translatedTo "Consumes neutron sources from input per second and converts them into neutron flux, evenly distributed to each irradiation part")
        info("反中子源：石墨粉-1000eV，小堆石墨粉-250eV，小撮石墨粉-100eV；" translatedTo "Flux reducers: graphite dust -1,000eV, small pile of graphite dust -250eV, tiny pile of graphite dust -100eV")
        info("中子源：锑-铍源+10eV，钚-铍源+100eV，锎-252粒子源+1000eV" translatedTo "Neutron sources: antimony-beryllium +10eV, plutonium-beryllium +100eV, californium-252 +1,000eV")
        command("可安装中子传感器；传感器默认报告所有辐照仓中的最低通量，也可切换为平均通量，单位为MeV" translatedTo "Install at most one Neutron Sensor; it reports the minimum flux among parts by default, or the average flux when switched, in MeV")
    }

    // 合金冶炼炉
    @JvmField
    val AlloySmelterTooltips = ComponentListSupplier {
        setTranslationPrefix("alloy_blast_smelter")

        section(AfterModuleInstallation)
        increase("运行速度翻倍" translatedTo "The running speed doubles")
    }

    // 溶解罐
    @JvmField
    val DissolvingTankTooltips = ComponentListSupplier {
        setTranslationPrefix("dissolving_tank")

        section(RunningRequirements)
        command("必须保证输入的流体与配方流体比例相同，否则无产物输出" translatedTo "Must ensure the ratio of input fluid to recipe fluid is the same, otherwise no product output")

        section(AfterModuleInstallation)
        increase("模块将帮助机器自动进行原料配比，无上述条件限制" translatedTo "The module will help the machine automatically match the raw materials, without the above conditions")
    }

    // 狂飙巨型核聚变反应堆
    @JvmField
    val kuangbiaoGiantNuclearFusionReactorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("kuangbiao_giant_nuclear_fusion_reactor")

        section(AfterModuleInstallation)
        info("模块分为两种：高能模块与超频模块" translatedTo "There are two types of modules: high-energy modules and overclock modules")
        increase("每多安装一个高能模块，反应堆热容量提升一倍" translatedTo "For each additional high-energy module installed, the reactor's heat capacity is doubled")
        command("高能模块必须按顺序安装，且不可重复安装相同模块" translatedTo "High-energy modules must be installed in order and the same module cannot be installed repeatedly")
        command("高能模块总计可提升四次热容量" translatedTo "High-energy modules can increase heat capacity a total of four times")
        increase("超频模块允许安装超频仓/线程仓" translatedTo "Overclock modules allow the installation of overclocking chambers/thread chambers")
        command("超频模块仅允许安装一个" translatedTo "Only one overclock module is allowed to be installed")
        info("多方块预览中的前四个预览位分别对应前四级高能模块安装后的状态" translatedTo "The first four preview slots in the multiblock preview correspond to the states after installing the first three high-energy modules")
        info("最后一个预览位对应安装超频模块后的状态" translatedTo "The last preview slot corresponds to the state after installing the overclock module")

        command("若高能模块与超频模块存在冲突，请先安装高能模块，再安装超频模块" translatedTo "If there is a conflict between the high-energy module and the overclock module, please install the high-energy module first, then install the overclock module")
    }

    // 狂飙一号巨型聚变反应堆控制电脑
    @JvmField
    val KuangbiaoGiantNuclearFusionReactorEnergyStorageTooltip = { eut: Long ->
        ComponentListSupplier {
            setTranslationPrefix("kuangbiao_giant_nuclear_fusion_reactor_energy_storage")

            command(
                ComponentSupplier(Component.translatable("gtceu.machine.fusion_reactor.capacity", eut)) +
                    (" [可安装模块扩容]" translatedTo " [can be expanded by installing modules]").rainbowFast(),
            )
        }
    }

    // 工业空间站六向衔接舱
    @JvmField
    val SpaceStationDockingModule = ComponentListSupplier {
        setTranslationPrefix("space_station_docking_module")
        important("使用高级终端的模块搭建功能来选择该舱的不同形态" translatedTo "Use the module building function of the advanced terminal to select different forms of this chamber")
        important("仅在成型任意一个形态后，该模块才可正常工作" translatedTo "This module can only function properly after forming any shape")
        error("无法同时成型多个形态" translatedTo "Cannot form multiple shapes at the same time")
    }

    // 大型藻类养殖中心
    @JvmField
    val LargeAlgaeFarmTooltips = ComponentListSupplier {
        setTranslationPrefix("large_algae_farm")

        section(RunningRequirements)
        command("耗能：(电压等级对应电压/2) EU/t" translatedTo "Energy consumption: (voltage level corresponding voltage / 2) EU/t")
        important(
            "每种藻类每次繁殖需要消耗1mb/个体/秒的生物质，请确保输入总线提供足够的生物质，否则藻类可能会死亡" translatedTo
                "Each type of algae requires 1mb/individual/second of biomass for each reproduction. Please ensure that the input bus provides enough biomass, otherwise the algae may die",
        )
        section("藻类生长机制" translatedTo "Algae Growth Mechanism")

        command("每秒更新一次藻类生长状态" translatedTo "Updates algae growth status once per second")
        important("每次更新，藻类种群会根据其环境最大容量与种群权重呈S型增长" translatedTo "With each update, the algae population grows in an S-curve based on its environmental maximum capacity and population weight")
        command("注意：每种藻类仅对其互补颜色的光源有最大提升效果" translatedTo "Note: Each type of algae only has the maximum enhancement effect on its complementary color light source")

        info("公式：增长量 = x(cap-x)(1-f)/(x+f(cap-x))" translatedTo "Formula: Growth amount = x(cap-x)(1-f)/(x+f(cap-x))")
        info(
            "其中x为当前种群数量" translatedTo
                "where x is the current population",
        )
        info(
            "cap决定环境最大容量,其值为(4^玻璃等级)*藻类权重" translatedTo
                "cap determines the environmental maximum capacity, its value is (4^[glass level])*[algae weight]",
        )
        info(
            "f为藻类的增长因子（越接近0越快），其值为0.1+0.9*e^(-(电压等级 + 1.0) * 藻类吸光/2)" translatedTo
                "f is the growth factor of algae(the closer to 0, the faster), where its value is 0.1+0.9*e^(-([voltage level] + 1.0) * [algae light absorption]/2)",
        )

        section("光吸收与权重机制" translatedTo "Light absorption & weight mechanics")
        info("藻类生长速度受环境光照强度影响" translatedTo "Algae growth rate is affected by environmental light intensity")
        info("每种颜色的卤素灯可以为对应波长范围的藻类提供额外光照，提升其种群权重" translatedTo "Each color of halogen lamp can provide additional illumination for algae in the corresponding wavelength range, increasing its population weight")
        info("向输入总线提供红/绿/蓝三种卤素灯以提升光照强度" translatedTo "Provide red/green/blue halogen lights to the input bus to enhance light intensity")
        command("每种颜色的卤素灯最多安装16个" translatedTo "A maximum of 16 halogen lights of each color can be installed")
        command(
            "光照强度 = min( min( 红色卤素灯数量,16 ) + min( 绿色卤素灯数量,16 ) + min( 蓝色卤素灯数量,16 ),16)" translatedTo
                "Light intensity = min( min( redHalogenLampCount,16 ) + min( greenHalogenLampCount,16 ) + min( blueHalogenLampCount,16 ),16 )",
        )

        info(
            "每次更新先按红/绿/蓝三色累计吸收：藻类的单色光吸收率 = (单色吸收数据(列于下表) / 255) * 单色光占比" translatedTo
                "In each update, first accumulate absorption by red/green/blue: Algae's monochromatic light absorption rate = (monochromatic absorption data (listed in the table below) / 255) * colorWeight",
        )
        info(
            "单色光占比为当前卤素灯数量占所有输入的卤素灯数量的比例" translatedTo
                "colorWeight is the proportion of the current halogen lamp count to the total input halogen lamp count",
        )

        info(
            "每种藻类的权重 = max( 红色光吸收率, 绿色光吸收率, 蓝色光吸收率 )，用于决定环境容量的占比：cap = 4^玻璃等级 * 权重" translatedTo
                "Algae weight = max( redRatio, greenRatio, blueRatio ), used to determine the proportion of environmental capacity: cap = 4^[glass level] * weight",
        )

        info(
            "当前光吸收值 = (r/255*红色光吸收率 + g/255*绿色光吸收率 + b/255*蓝色光吸收率) * (光照强度 / 16)" translatedTo
                "Current light absorption = (r/255*redRatio + g/255*greenRatio + b/255*blueRatio) * (lightIntensity / 16)",
        )
        info(
            "该吸收值用于决定藻类的增长因子f" translatedTo
                "This absorption value is used to determine the growth factor f of algae",
        )

        section("藻类卤素灯光波段吸收数据" translatedTo "Algae Halogen Lamp Light Wavelength Absorption Data")
        info("可养殖的藻类为：红藻、褐藻、金藻、绿藻、蓝藻" translatedTo "The cultivable algae are: red algae, brown algae, golden algae, green algae, blue algae")
        info("使用专用的藻类访问仓来收集或投放藻类" translatedTo "Use a dedicated algae access hatch to collect or release algae")
        Algae.entries.forEach { algae ->
            val colorName = when (algae) {
                Algae.RedAlgae -> "红藻" translatedTo "Red"
                Algae.BrownAlgae -> "褐藻" translatedTo "Brown"
                Algae.GoldAlgae -> "金藻" translatedTo "Golden"
                Algae.GreenAlgae -> "绿藻" translatedTo "Green"
                Algae.BlueAlgae -> "蓝藻" translatedTo "Blue"
            }.color(algae.color)
            info(
                colorName +
                    ("r:" + algae.redAbsorption + " g:" + algae.greenAbsorption + " b:" + algae.blueAbsorption).toLiteralSupplier(),
            )
        }
    }

    // 蒸汽裂化机
    @JvmField
    val LargeSteamCrackerTooltips = ComponentListSupplier {
        setTranslationPrefix("large_steam_cracker")
        info("原料效率仅正常裂化机的40%" translatedTo "The raw material efficiency is only 40% of that of a normal cracker")
        increase("每使用高一等级的蒸汽输入仓，配方产出提升100mb" translatedTo "For each higher level of steam input hatch used, the output increases by 100mb")
    }

    // 魔力流合成台
    @JvmField
    val ManaFlowAssemblerTooltips = ComponentListSupplier {
        setTranslationPrefix("mana_flow_assembler")
        story("原始人的泰拉凝聚板" translatedTo "The original person's Terra Condenser Plate")
        content("该机器利用四角魔力池内的魔力流来运行" translatedTo "This machine operates using the mana flow in the quadrilateral mana pool")
        content("魔力流的强度取决于四角魔力池上方魔力水晶的等级相应提供量之和" translatedTo "The strength of the mana flow depends on the sum of the levels of the mana crystals above the quadrilateral mana pool")
        info("每种魔力水晶强度：" translatedTo "Mana crystal strength: ")
        info("魔力水晶：提供§b8§rMana/t" translatedTo "Mana Crystal: provides §b8§r Mana/t")
        info("自然水晶：提供§b32§rMana/t" translatedTo "Natura Crystal: provides §b32§r Mana/t")
        info("精灵水晶：提供§b128§rMana/t" translatedTo "Alfsteel Crystal: provides §b128§r Mana/t")
        info("盖亚水晶：提供§b512§rMana/t" translatedTo "Gaia Crystal: provides §b512§r Mana/t")
        content("向合成台上投掷物品以输入，输出产物将以同样的方式投掷出来" translatedTo "Throw items onto the crafting station for input, and the output products will be thrown out in the same way")
        important("只有前9个掉落物会被作为输入进行处理" translatedTo "Only the first 9 dropped items will be processed as input")
        command("机器总是会以可使用的最大魔力强度运行,且配方时间固定为10秒" translatedTo "The machine will always operate at the maximum mana strength available, and the recipe time is fixed at 10 seconds")
        command("且以此魔力强度计算配方时长(总魔力消耗量/魔力强度)，超过10秒的配方无法运行" translatedTo "And the recipe time is calculated based on this mana strength (total mana consumption / mana strength), recipes that exceed 10 seconds cannot run")
        important("无法运行电力配方" translatedTo "Cannot run recipes that require EU")
    }

    // 磁流体发电机
    @JvmField
    val magneticFluidGeneratorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("magnetic_fluid_generator")

        section(RunningRequirements)
        important("玻璃等级限制了能量输出仓等级" translatedTo "The glass tier limits the energy output hatch tier")
        command("实际产出由等离子热值决定" translatedTo "Actual output is determined by plasma heat value")
        command("基础并行：64" translatedTo "Basic parallel: 64")

        section(EfficiencyBonus)
        increase("如果密封外壳等级大于LuV，则提升效率 x 密封外壳等级/4" translatedTo "If the hermetic casing tier is greater than LuV, the efficiency is increased by x (hermetic casing tier) / 4")
        increase("如果使用激光仓，则提升发电量 x 2^等级" translatedTo "If a laser hatch is used, power generation is increased by x 2^tier")

        section(AfterModuleInstallation)
        increase("提升效率 x 2" translatedTo "Efficiency is increased by x 2")
        increase("基础并行 x 4" translatedTo "Basic parallel x 4")
        increase("如果使用激光仓，则提升发电量 x 4^等级" translatedTo "If a laser hatch is used, power generation is increased by x 4^tier")
    }

    // 戴森球接收站
    @JvmField
    val dysonSphereReceivingStationTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("dyson_sphere_receiving_station")

        section(MainFunction)
        command("发射戴森球模块后开始工作" translatedTo "Starts working after launching Dyson Sphere modules")
        command("需要输入极寒之凛冰作为冷却剂" translatedTo "Requires Gelid Cryotheum as a coolant")
        info("产能功率，和需求算力由发射的模块数量决定" translatedTo "Power capacity and demand computing power are determined by the number of launched modules")
        increase("每次发射可使功率增加1A MAX" translatedTo "Each launch can increase power by 1A MAX")

        section("损坏机制" translatedTo "Damage Mechanics")
        command("每次运行都有(模块数量/128 + 1)%的概率损坏一次模块" translatedTo "Each run has a (Module Count / 128 + 1)% chance to damage a module")
        important("当损坏高于60%时，输出效率随损坏值由100%逐渐降低到20%，并输出随损坏值增强的红石信号" translatedTo "When damage exceeds 60%, output efficiency gradually decreases from 100% to 20% with damage value, and outputs a redstone signal enhanced by the damage value")
        info("当损坏达到100%时减少一次模块发射数量，并重置损坏值" translatedTo "When damage reaches 100%, it reduces the number of module launches by one and resets the damage value")
        info("在损坏值高于60%时发射不会增加发射次数，但会重置损坏值" translatedTo "When damage value is above 60%, launching will not increase the launch count but will reset the damage value")
    }

    // 虚拟物品供应机
    @JvmField
    val virtualItemSupplyMachineTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("virtual_item_supply_machine")

        section(MainFunction)
        function("为ME网络提供虚拟物品" translatedTo "Provides virtual items for the ME network")
        increase("虚拟物品可用于替代样板中不消耗的物品" translatedTo "Virtual items can be used to replace items in the blueprint that do not consume resources")
        content("将任何物品放入供应机中均可转换为虚拟物品" translatedTo "Place any item into the supply machine to convert it into a virtual item")
    }

    @JvmField
    val BeamGeneratorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("beam_generator")

        section(MainFunction)
        function("根据运行的配方发射高能光束射线" translatedTo "Emits high-energy beam rays based on the running recipe")
        command("超频规则：电压每乘以4，光强等级乘以2" translatedTo "Overclocking rule: For every 4 times increase in voltage, the light intensity level increases by 2 times")
        content("在机器gui中调节射线的水平角与俯仰角" translatedTo "Adjust the horizontal and pitch angles of the beam in the machine GUI")

        important("光束射线在非超净间的环境中，每经过一格，光强都会乘以0.95" translatedTo "In a non-cleanroom environment, the light intensity of the beam will be attenuated by a factor of 0.95 per block")
    }

    @JvmField
    val BeamRedirectorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("beam_redirector")

        section(MainFunction)
        function("用于改变高能光束射线的方向" translatedTo "Used to change the direction of high-energy beam rays")
        command("在机器GUI中调节射线的水平角与俯仰角" translatedTo "Adjust the horizontal and pitch angles of the beam in the machine GUI")
    }

    @JvmField
    val ExcitationCrystalTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("excitation_crystal")

        section(MainFunction)
        function("如果它周围有晶体激发器，它将会被激发并使穿过的高能光束射线的光强翻倍" translatedTo "If there are crystal exciters around it, it will be excited and double the light intensity of the high-energy beam rays passing through it")
        command("该过程需要(光强 * 4096EU/t)的能量" translatedTo "This process requires (light intensity * 4096 EU/t) of energy")
        command("并且激发功率受到晶体激发器的等级限制（至多2A*机器等级）" translatedTo "And the excitation power is limited by the level of the crystal exciter (up to 2A * machine level)")
    }

    @JvmField
    val CrystalExciterTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("crystal_exciter")
        section(MainFunction)
        function("用于激发周围的晶体" translatedTo "Used to excite the surrounding crystals")
        command("需要消耗能量来激发晶体" translatedTo "Requires energy consumption to excite the crystals")
    }

    @JvmField
    val BeamSemiReflectorTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("beam_semi_reflector")

        section(MainFunction)
        function("将穿过的高能光束射线分为两路，一路继续前进，一路被反射" translatedTo "Splits the high-energy beam rays passing through into two paths, one continues forward, and the other is reflected")
        command("在机器GUI中调节射线的反射率" translatedTo "Adjust the reflectivity of the beam in the machine GUI")
    }

    @JvmField
    val BeamPolarizerTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("beam_polarizer")

        section(MainFunction)
        function("改变穿过的高能光束射线的振动方向" translatedTo "Changes the vibration direction of the high-energy beam rays passing through")
        command("旋光角度由机器内的流体决定" translatedTo "The angle of rotation is determined by the fluid inside the machine")
    }

    @JvmField
    val BeamAccessHatchTooltips: ComponentListSupplier = ComponentListSupplier {
        setTranslationPrefix("beam_access_hatch")

        section(MainFunction)
        function("为机器提供导入精确的高能光束射线" translatedTo "Provides precise high-energy beam rays for the machine")
    }
}
