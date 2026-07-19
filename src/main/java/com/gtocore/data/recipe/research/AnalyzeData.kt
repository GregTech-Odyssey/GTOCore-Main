package com.gtocore.data.recipe.research

import com.gtocore.api.data.tag.GTOTagPrefix
import com.gtocore.api.data.tag.GTOTagPrefix.NANITES
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.CATALYSIS
import com.gtocore.api.research.ResearchTag.DATA_ENGINEERING
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.api.research.techtree.TechNode
import com.gtocore.api.research.techtree.TechNode.OTHER_REWARD_LABEL
import com.gtocore.api.research.techtree.TechTreeManager
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOFluids
import com.gtocore.common.data.GTOItems
import com.gtocore.common.data.GTOMaterials
import com.gtocore.common.data.machines.MultiBlockA.CHEMICAL_PLANT
import com.gtocore.common.data.machines.MultiBlockD

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.IV
import com.gregtechceu.gtceu.api.GTValues.LuV
import com.gregtechceu.gtceu.api.GTValues.MAX
import com.gregtechceu.gtceu.api.GTValues.UEV
import com.gregtechceu.gtceu.api.GTValues.UHV
import com.gregtechceu.gtceu.api.GTValues.ULV
import com.gregtechceu.gtceu.api.GTValues.UV
import com.gregtechceu.gtceu.api.GTValues.VN
import com.gregtechceu.gtceu.api.GTValues.ZPM
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTMaterials.Carbon
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.LARGE_CHEMICAL_REACTOR
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gto.fastcollection.O2OOpenCacheHashMap
import com.gto.registrate.util.entry.BlockEntry
import com.gtolib.GTOCore
import com.gtolib.api.lang.CNEN
import com.gtolib.utils.RegistriesUtils
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture

object AnalyzeData : AutoInitialize<AnalyzeData>() {

    val langMap: Map<String, CNEN> = if (GTCEu.isDataGen()) O2OOpenCacheHashMap() else emptyMap()

    @JvmField
    val TechTree: TechTreeManager =
        TechTreeManager("main_tree", "研究树", "Research Tree", ItemStackTexture(GTOItems.BLUE_HALIDE_LAMP.asStack()))

    @JvmField
    val TierItems = mapOf(
        0 to GTItems.TOOL_DATA_STICK,
        1 to GTItems.TOOL_DATA_ORB,
        2 to GTItems.TOOL_DATA_MODULE,
        3 to GTOItems.NEURAL_MATRIX,
        4 to GTOItems.ATOMIC_ARCHIVES,
        5 to GTOItems.OBSIDIAN_MATRIX,
        6 to GTOItems.MICROCOSM,
    )

    override fun init() {
    }

    @JvmField
    val ComponentInAssemblyLineluv = TechTree.builder("component_in_assembly_line", "装配线基础部件", "Basic components in assembly line")
        .description("在装配线中组装基础的组件", "Assemble basic components in the assembly line")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.FIELD_GENERATOR_IV, 1.0f).build())
        .icon(GTItems.FIELD_GENERATOR_LuV)
        .tier(0)
        .build()

    @JvmField
    val ComponentInAssemblyLinezpm = TechTree.builder("component_in_assembly_line1", "装配线进阶部件I", "Advanced components in assembly line I")
        .description("在装配线中组装更加复杂的部件", "Assemble more complex components in the assembly line")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 240L).setEurekaItem(GTItems.FIELD_GENERATOR_LuV, 0.8f).build())
        .icon(GTItems.FIELD_GENERATOR_ZPM)
        .prerequisites(ComponentInAssemblyLineluv)
        .tier(1)
        .build()

    @JvmField
    val ComponentInAssemblyLineuv = TechTree.builder("component_in_assembly_line2", "装配线进阶部件II", "Advanced components in assembly line II")
        .description("利用三钛合金制造成更加强悍的部件", "Use trititanium alloy to manufacture even more powerful components")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 1200L).setEurekaItem(GTItems.FIELD_GENERATOR_ZPM, 0.8f).build())
        .icon(GTItems.FIELD_GENERATOR_UV)
        .prerequisites(ComponentInAssemblyLinezpm)
        .tier(2)
        .build()

    @JvmField
    val ComponentInAssemblyLineuhv = TechTree.builder("component_in_assembly_line3", "装配线进阶部件III", "Advanced components in assembly line III")
        .description("充能下界合金的磁化与山铜为其带来了更强的动力与耐久性", "The magnetization of charged nether alloy and the addition of copper bring it stronger power and durability")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 1800L).setEurekaItem(GTItems.FIELD_GENERATOR_UV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UHV)
        .prerequisites(ComponentInAssemblyLineuv)
        .tier(2)
        .build()

    @JvmField
    val ComponentInAssemblyLineuev = TechTree.builder("component_in_assembly_line4", "装配线进阶部件IV", "Advanced components in assembly line IV")
        .description("搭载了下一代末影耐造材料与技术", "Equipped with next-generation enderly durable materials and technology")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(325 * 20 * 2400L).setEurekaItem(GTItems.FIELD_GENERATOR_UHV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UEV)
        .prerequisites(ComponentInAssemblyLineuhv)
        .tier(3)
        .build()

    @JvmField
    val ComponentInAssemblyLineuiv = TechTree.builder("component_in_assembly_line5", "装配线进阶部件V", "Advanced components in assembly line V")
        .description("制造能抗住微型黑洞的新部件", "Manufacture new components that can withstand micro black holes")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(800 * 20 * 2400L).setEurekaItem(GTItems.FIELD_GENERATOR_UEV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UIV)
        .prerequisites(ComponentInAssemblyLineuev)
        .tier(3)
        .build()

    @JvmField
    val ComponentInAssemblyLineuxv = TechTree.builder("component_in_assembly_line6", "装配线进阶部件VI", "Advanced components in assembly line VI")
        .description("制造撕裂宇宙的新部件", "Manufacture new components that can tear the universe apart")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(2000 * 20 * 3600L).setEurekaItem(GTItems.FIELD_GENERATOR_UIV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UXV)
        .prerequisites(ComponentInAssemblyLineuiv)
        .tier(4)
        .build()

    @JvmField
    val ComponentInAssemblyLineopv = TechTree.builder("component_in_assembly_line7", "装配线进阶部件VII", "Advanced components in assembly line VII")
        .description("将混沌神龙力量注入到部件中", "Inject the power of the chaotic dragon into the components")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(16000 * 20 * 4000L).setEurekaItem(GTItems.FIELD_GENERATOR_UXV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_OpV)
        .prerequisites(ComponentInAssemblyLineuxv)
        .tier(5)
        .build()

    @JvmField
    val ComponentInAssemblyLinemax = TechTree.builder("component_in_assembly_line8", "装配线进阶部件VIII", "Advanced components in assembly line VIII")
        .description("通过扭曲时空来驱动的永恒之马达", "The eternal motor driven by twisting space-time")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32000 * 20 * 4800L).setEurekaItem(GTItems.FIELD_GENERATOR_OpV, 0.7f).build())
        .icon(GTOItems.MAX_FIELD_GENERATOR)
        .prerequisites(ComponentInAssemblyLineopv)
        .tier(6)
        .build()

    private val EnergyIOsTiers: IntArray = intArrayOf(0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 4, 5, 6)

    @JvmField
    val ComponentInAssemblyLines: Array<TechNode?> = arrayOf(
        null, null, null, null, null, null,
        ComponentInAssemblyLineluv, ComponentInAssemblyLinezpm, ComponentInAssemblyLineuv, ComponentInAssemblyLineuhv, ComponentInAssemblyLineuev,
        ComponentInAssemblyLineuiv, ComponentInAssemblyLineuxv, ComponentInAssemblyLineopv, ComponentInAssemblyLinemax,
    )

    private val ComponentCasings: Array<BlockEntry<Block>?> = arrayOf(
        null,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_MV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_HV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_EV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_IV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UHV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UEV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UIV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UXV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_OPV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_MAX,
    )
    private var lastComponentCasingNode: TechNode? = null

    fun ComponentCasing(tier: Int): TechNode {
        val node = TechTree.builder(
            "component_casing$tier",
            "装配线部件外壳${VN[tier]}",
            "Assembly Line Component Casing ${VN[tier]}",
        )
            .description(
                "精密而坚固的装配线部件外壳，为部件装配线提供${VN[tier]}级的部件装配条件",
                "Precision and sturdy assembly line component casing, providing ${VN[tier]} level component assembly conditions for the assembly line",
            )
            .requirements(
                ResearchRequirements.Builder().setCWUNeeded(32L * (1L shl (tier * 2)))
                    .setEurekaItem(ComponentCasings[tier - 1], if (tier == LuV) 1f else 0.7f).build(),
            )
            .icon(ComponentCasings[tier])
            .tier(EnergyIOsTiers[tier])
        if (lastComponentCasingNode != null) {
            node.prerequisites(lastComponentCasingNode!!, ComponentInAssemblyLines[tier])
        } else {
            node.prerequisites(ComponentInAssemblyLines[tier])
        }
        lastComponentCasingNode = node.build()
        return lastComponentCasingNode!!
    }

    @JvmField
    val ComponentCasingsNodes: Array<TechNode?> = (ULV..MAX).map { if (it >= LuV) ComponentCasing(it) else null }.toTypedArray()

    private var lastEnergyIONode: TechNode? = null
    fun energyIONode(tier: Int): TechNode {
        val node = TechTree.builder(
            "energy_io$tier",
            "高压能量输入输出${FormattingUtil.toRomanNumeral(tier - IV)}",
            "High Voltage Energy Input/Output${FormattingUtil.toRomanNumeral(tier - IV)}",
        )
            .description(
                "安全处理高达${FormattingUtil.formatNumbers(8L * (1L shl (tier * 2)))}EU/t的高压能量流",
                "Safely handle high-voltage energy flows up to ${FormattingUtil.formatNumbers(8L * (1L shl (tier * 2)))} EU/t",
            )
            .requirements(
                ResearchRequirements.Builder().setCWUNeeded(80L * (1 shl (tier * 2)))
                    .setEurekaItem(GTMachines.ENERGY_INPUT_HATCH[tier - 1], if (tier == LuV) 1f else 0.7f).build(),
            )
            .icon(GTMachines.ENERGY_INPUT_HATCH[tier])
            .tier(EnergyIOsTiers[tier])
        if (lastEnergyIONode != null) {
            node.prerequisites(lastEnergyIONode!!)
        }
        lastEnergyIONode = node.build()
        return lastEnergyIONode!!
    }

    @JvmField
    val EnergyIOs: Array<TechNode?> = (ULV..MAX).map { if (it >= LuV) energyIONode(it) else null }.toTypedArray()

    @JvmField
    val IridiumCasingProduction = TechTree.builder("iridium_casing_production", "高性能机器外壳生产", "High-Performance Machine Casing Production")
        .description("生产铱强化机械方块的外壳，这种外壳有着强大的耐久性和抗辐射能力", "Produce the casing for iridium-reinforced machine blocks, which has strong durability and radiation resistance")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(15).setEurekaItem(TagPrefix.block, GTMaterials.Osmiridium, 1.0f).build())
        .icon(GTOBlocks.IRIDIUM_CASING)
        .build()

    @JvmField
    val SuperConductingMaterialResearch = TechTree.builder("super_conducting_material_research", "超导材料研究", "Superconducting Material Research")
        .description("将具有超导特性的材料封装并维持在环境中，实现电压传输的零线损", "Encapsulate materials with superconducting properties and maintain them in the environment to achieve zero-loss voltage transmission")
        .icon(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.UraniumRhodiumDinaquadide)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 300L)
                .setEurekaItem(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.UraniumRhodiumDinaquadide, 1.0F)
                .build(),
        )
        .build()

    @JvmField
    val DataBase = TechTree.builder("data_base", "数据库", "Data Base")
        .description("数据库是一个用于存储和管理数据的系统，安装数据仓与光学传输仓实现生产线数据的存储和路由", "The database is a system for storing and managing data, installing data warehouses and optical transmission warehouses to achieve storage and routing of production line data")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTResearchMachines.DATA_ACCESS_HATCH, 1.0f).build())
        .icon(GTResearchMachines.DATA_BANK)
        .build()

    @JvmField
    val ComputationArray = TechTree.builder("computation_array", "算力供应传输基础", "Computation Supply and Transmission Foundation")
        .description("搭建基础算力供应与多源算力分配逻辑", "Build a basic computation supply and multi-source computation distribution logic")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.COVER_SCREEN, 1.0f).build())
        .icon(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY)
        .prerequisites(DataBase)
        .build()

    @JvmField
    val HighDensityEnergyStorage = TechTree.builder("high_density_energy_storage", "高密度能量存储", "High Density Energy Storage")
        .description("优化兰博顿水晶的能量密度与充放电效率，增强能量存储与传输能力", "Optimize the energy density and charge/discharge efficiency of Lambton crystals, enhancing energy storage and transmission capabilities")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.ENERGY_LAPOTRONIC_ORB, 1.0f).build())
        .icon(GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER)
        .build()

    @JvmField
    val ChemicalPlantEnvironmentControl = TechTree.builder("chemical_plant_environment_control", "化工厂环境控制", "Chemical Plant Environment Control")
        .description("掌握化工厂的环境控制技术，实现更大规模的化学产品生产与更高效的资源利用", "Master the environmental control technology of chemical plants, achieving larger-scale chemical product production and more efficient resource utilization")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 40L).setEurekaItem(LARGE_CHEMICAL_REACTOR, 1.0f).build())
        .icon(CHEMICAL_PLANT)
        .build()

    @JvmField
    val DataCenter = TechTree.builder("data_in_research", "科研数据中心", "Data Center in Research")
        .description("科研数据中心是一个用于存储和处理科研数据的高性能计算平台，相比于数据库，它可以将其中标准化的生产数据与积累的科研数据进行整合，推进基地技术迭代", "The research data center is a high-performance computing platform for storing and processing research data. Compared to databases, it can integrate standardized production data with accumulated research data, promoting base technology iteration")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(20).addMaterialNeeded(DATA_ENGINEERING, 120).setEurekaItem(GTResearchMachines.DATA_ACCESS_HATCH, 1.0f).build())
        .icon(GTResearchMachines.DATA_BANK)
        .prerequisites(DataBase)
        .build()

    @JvmField
    val ScanStation = TechTree.builder("scan_station", "扫描站", "Scan Station")
        .description("将晶片中的数据进行扫描与分析，获取其中的科研数据，并积累到团队知识库中", "Scan and analyze the data in the chip, obtain the research data, and accumulate it into the team knowledge base")
        .icon(GTOItems.DATA_CRYSTAL_MK1)
        .prerequisites("data_base")
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 60L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:luv_scanner"), 1.0F)
                .build(),
        )
        .build()

    @JvmField
    val SelfMaintenanceSystem = TechTree.builder("self_maintenance_system", "自维护系统", "Self-Maintenance System")
        .description("开发自维护系统，实现设备的自动检测与修复，减少人工干预", "Develop a self-maintenance system to achieve automatic detection and repair of equipment, reducing manual intervention")
        .icon(RegistriesUtils.getItem("ad_astra:wrench"))
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 180L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:auto_maintenance_hatch"), 1.0F)
                .addMaterialNeeded(ResearchTag.MECHANICS, 4)
                .build(),
        )
        .build()

    @JvmField
    val CrystalTechMainframe = TechTree.builder("crystal_tech_mainframe", "晶体技术主机", "Crystal Technology Mainframe")
        .description("合成大晶片", "Synthesize large crystal chips")
        .icon(GTItems.CRYSTAL_MAINFRAME_UV)
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 240L)
                .setEurekaItem(GTItems.CRYSTAL_COMPUTER_ZPM, 0.92F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val PreciseManufacturingTech = TechTree.builder("precise_manufacturing_tech", "精密制造技术", "Precision Manufacturing Technology")
        .description("掌握精密制造技术，实现高精度零件的生产与组装", "Master precision manufacturing technology to achieve the production and assembly of high-precision parts")
        .icon(RegistriesUtils.getItem("gtocore:precision_assembler"))
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:large_assembler"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val BaseMEMachines = TechTree.builder("base_me_machines", "基础ME机器", "Basic ME Machines")
        .description("在AE网络内高速传输与组装物质信息，并和生产设备深度交互", "High-speed transmission and assembly of matter information within the AE network, and deep interaction with production equipment")
        .icon(RegistriesUtils.getItem("gtocore:super_molecular_assembler"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("expatternprovider:ex_molecular_assembler"), if (GTOCore.isEasy()) 1f else 0.8f)
                .build(),
        )
        .build()

    @JvmField
    val ParticleAccelerators = TechTree.builder("particle_accelerator", "粒子可控运动", "Constrained Particle Motion")
        .description("利用粒子加速器进行高能物理实验，探索微观世界的奥秘", "Use particle accelerators for high-energy physics experiments, exploring the mysteries of the microscopic world")
        .icon(RegistriesUtils.getItem("gtocore:alpha_particle_particle_source"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 120L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:accelerated_pipeline"), 0.8F)
                .build(),
        )
        .tier(1)
        .prerequisites(SuperConductingMaterialResearch)
        .build()

    @JvmField
    val IsaMillingMachine = TechTree.builder("isa_milling_machine", "艾萨研磨处理技术", "Isa Ore Processing Technology")
        .description("掌握艾萨研磨处理矿物这种滚珠暴力碾磨一切再拿泔水泡的技术", "Master the Isa milling process for minerals, a technology that violently grinds everything with ball bearings and soaks it in swill")
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 60L).setEurekaItem(RegistriesUtils.getItem("gtceu:iv_macerator"), 0.8f).build())
        .icon(RegistriesUtils.getItem("gtocore:milled_nickel"))
        .tier(1)
        .build()

    @JvmField
    val VirtualCoinCurrency = TechTree.builder("virtual_coin_currency", "虚拟货币", "Virtual Coin Currency")
        .description("给了冰冷的溢出算力一种独特的用法，通过帮别人计算一串随机的数字，换成温暖的虚拟（？）货币", "Give the cold and lifeless overflow computing power a unique use, by calculating a string of random numbers for others, in exchange for warm virtual (?) currency")
        .icon(RegistriesUtils.getItem("gtocore:infinity_coin"))
        .prerequisites(ComputationArray)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 120L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:gold_coin"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val VoidMiner = TechTree.builder("void_miner", "虚空矿脉采掘技术", "Void Vein Mining Technology")
        .description("从一无所有的虚空中定向寻找并采掘出各种矿脉", "From the void of nothingness, directionally search for and mine various veins")
        .icon(RegistriesUtils.getItem("gtocore:void_miner"))
        .prerequisites(PreciseManufacturingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:void_miner"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val WetwareTech = TechTree.builder("wetware_tech", "湿件技术", "Wetware Technology")
        .description("把湿件着你的几团肉拼在一起，组合它们的湿件能力的技术", "The technology of putting your wetware together and combining their wetware capabilities")
        .icon(RegistriesUtils.getItem("gtceu:wetware_processor_mainframe"))
        .prerequisites(PreciseManufacturingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 120L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:wetware_processor_assembly"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val SuperRocketTech = TechTree.builder("super_rocket_tech", "超级火箭技术", "Super Rocket Technology")
        .description("掌握超级火箭的设计与制造技术，实现更高效的太空运输与探索", "Master the design and manufacturing technology of super rockets, achieving more efficient space transportation and exploration")
        .icon(RegistriesUtils.getItem("ad_astra_rocketed:tier_7_rocket"))
        .prerequisites(ComponentInAssemblyLinezpm)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 120L)
                .setEurekaFluid(GTOMaterials.StellarEnergyRocketFuel.fluid, 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val AdvancedAssemblyLineMachine = TechTree.builder("advanced_assembly_line_machine", "进阶装配线", "Advanced Assembly Line")
        .description("集成GTO公司机械组搭的框架，电子组布置的线路和物流组搞的配送，打造出一条虽然很费电但高通量的装配线", "Integrating the framework built by GTO's mechanical team, the circuits laid out by the electronics team, and the logistics team's distribution, creating an assembly line that is very power-hungry but high-throughput")
        .icon(RegistriesUtils.getItem("gtocore:advanced_assembly_line_unit"))
        .prerequisites(PreciseManufacturingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 1200L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:assembly_line"), 0.9F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val NanitesTech = TechTree.builder("nanites_tech", "纳米蜂群技术", "Nanites Technology")
        .description("强行融合干细胞和细粒度纳米碳粉，制造出的可复制可模板化的纳米蜂群，实现原子尺度上的物质操控", "Forcibly fusing stem cells and fine-grained nano carbon powder to create replicable and templateable nanite swarms, achieving material manipulation at the atomic scale")
        .icon(NANITES, Carbon)
        .prerequisites(ChemicalPlantEnvironmentControl)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(CATALYSIS, 640)
                .addMaterialNeeded(BIOLOGY, 120)
                .addMaterialNeeded(MATERIAL, 8000)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:activated_carbon_dust"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val UltimateEnergyStorage = TechTree.builder("ultimate_energy_storage", "终极能量存储", "Ultimate Energy Storage")
        .description("将至少50倍体积的兰博顿水晶压缩到一个电池中存储的技术", "The technology of compressing at least 50 times the volume of Lambton crystals into a single battery for storage")
        .icon(GTItems.ULTIMATE_BATTERY)
        .prerequisites(HighDensityEnergyStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:nanite_energy_storage"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val CircuitAssemblyLine = TechTree.builder("circuit_assembly_line", "电路装配线", "Circuit Assembly Line")
        .description("让装着纳米蜂群的机器人装配元件，流水线化生产电路板", "Let robots equipped with nanite swarms assemble components, producing circuit boards in an assembly line")
        .icon(RegistriesUtils.getItem("gtocore:circuit_assembly_line"))
        .prerequisites(PreciseManufacturingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:precision_circuit_assembly_robot_mk1"), 1.0F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val MECapacityExpansion = TechTree.builder("me_capacity_expansion", "ME设备扩容", "ME Capacity Expansion")
        .description("扩展ME设备的存储容量，实现更大规模的物质信息存储与管理", "Expand the storage capacity of ME devices, achieving larger-scale matter information storage and management")
        .icon(GTOItems.PATTERN_BUFFER_UPGRADER1)
        .prerequisites(BaseMEMachines)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(DATA_ENGINEERING, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:me_pattern_buffer_proxy"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val MECatalystSupplying = TechTree.builder("me_catalyst_supplying", "ME催化剂供应", "ME Catalyst Supplying")
        .description("将催化剂的损耗降低到零点，并与ME设备深度交互，使得带有催化剂的生产线被更方便的管理", "Reduce the loss of catalysts to zero and interact deeply with ME devices, making production lines with catalysts easier to manage")
        .icon(RegistriesUtils.getItem("gtocore:me_catalyst_pattern_buffer"))
        .prerequisites(BaseMEMachines)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 4000)
                .addMaterialNeeded(CATALYSIS, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:advanced_catalyst_hatch"), if (GTOCore.isEasy()) 1f else 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val MESmartGatingClustering = TechTree.builder("me_smart_gating_clustering", "ME智能分流集群", "ME Smart Gating Clustering")
        .description("将ME设备的物质信息进行智能分流与集群化管理，实现更高效的生产线调度与资源利用", "Intelligently divert and cluster the matter information of ME devices, achieving more efficient production line scheduling and resource utilization")
        .icon(RegistriesUtils.getItem("gtocore:me_wildcard_pattern_buffer"))
        .prerequisites(BaseMEMachines)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(DATA_ENGINEERING, 128)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:pattern_content_access_terminal"), if (GTOCore.isEasy()) 1f else 0.8F)
                .build(),
        )
        .tier(GTOCore.difficulty)
        .build()

    @JvmField
    val SpaceElevator = TechTree.builder("space_elevator", "太空电梯", "Space Elevator")
        .description("建造一条连接地球与太空的电梯，把手可摘星辰变为现实", "Build an elevator connecting the Earth and space, turning the dream of reaching the stars into reality")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator"))
        .prerequisites(SuperRocketTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 1200L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:gravitation_engine_unit"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserBatchProduction0 = TechTree.builder("laser_batch_production_proto", "激光能源原始批量生产", "Laser Energy Batch Production Prototype")
        .description("原始地利用高功率激光，传输大量能量用于加热炉子", "Primarily use high-power lasers to transmit large amounts of energy for heating furnaces")
        .icon(RegistriesUtils.getItem("gtocore:energy_control_module_mk2"))
        .prerequisites(ComponentInAssemblyLineuv, IridiumCasingProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:energy_control_module_mk2"), 0.6F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserBatchProduction1 = TechTree.builder("laser_batch_production", "激光能源批量生产初步", "Laser Energy Batch Production Preliminary")
        .description("利用高功率激光，传输大量能量用于超大批量的生产加工", "Use high-power lasers to transmit large amounts of energy for ultra-large-scale production and processing")
        .icon(RegistriesUtils.getItem("gtocore:machining_control_module_mk2"))
        .prerequisites(ComponentInAssemblyLineuhv, LaserBatchProduction0)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:machining_control_module_mk3"), 0.6F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val CryotheumSupercoductingTech = TechTree.builder("cryotheum_superconducting_tech", "凛冰超导技术", "Cryotheum Superconducting Technology")
        .description("使用凛冰循环浸淋超导材料，进一步提升超导导体性能的稳定性", "Use cryotheum circulation to immerse superconducting materials, further improving the stability of superconducting performance")
        .icon(GTOFluids.GELID_CRYOTHEUM.get())
        .prerequisites(SuperConductingMaterialResearch)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 3600L)
                .addMaterialNeeded(MATERIAL, 8000)
                .setEurekaFluid(GTOFluids.GELID_CRYOTHEUM.get(), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val TokamakFusionReactor = TechTree.builder("tokamak_fusion_reactor", "托卡马克聚变反应堆", "Tokamak Fusion Reactor")
        .description("掌握可控的托卡马克聚变反应堆技术，实现元素的聚变与等离子体的生产", "Master the technology of controllable Tokamak fusion reactors, achieving element fusion and plasma production")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTBlocks.SUPERCONDUCTING_COIL, 1.0f).build())
        .icon(FUSION_REACTOR[LuV].asStack())
        .prerequisites(SuperConductingMaterialResearch)
        .build()

    @JvmField
    val TokamakFusionReactor2 = TechTree.builder("tokamak_fusion_reactor2", "托卡马克聚变反应堆II", "Tokamak Fusion Reactor II")
        .description("更甜的甜甜圈，更高的温度，更强的磁场，更快的聚变", "Sweeter donut, higher temperature, stronger magnetic field, faster fusion")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 240L).setEurekaItem(FUSION_REACTOR[LuV], 0.8f).build())
        .icon(FUSION_REACTOR[ZPM].asStack())
        .prerequisites(TokamakFusionReactor)
        .tier(1)
        .build()

    @JvmField
    val TokamakFusionReactor3 = TechTree.builder("tokamak_fusion_reactor3", "托卡马克聚变反应堆III", "Tokamak Fusion Reactor III")
        .description("甜甜圈3号，甜度超标！", "Donut No. 3, sweetness overload!")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(128 * 20 * 1200L).setEurekaItem(FUSION_REACTOR[ZPM], 0.8f).build())
        .icon(FUSION_REACTOR[UV].asStack())
        .prerequisites(TokamakFusionReactor2)
        .tier(2)
        .build()

    @JvmField
    val TokamakFusionReactor4 = TechTree.builder("tokamak_fusion_reactor4", "托卡马克聚变反应堆IV", "Tokamak Fusion Reactor IV")
        .description("甜甜圈4号，想造什么元素自己填", "Donut No. 4, fill in whatever element you want to make")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(256 * 20 * 2400L).setEurekaItem(FUSION_REACTOR[UV], 0.8f).build())
        .icon(MultiBlockD.FUSION_REACTOR[UHV].asStack())
        .prerequisites(TokamakFusionReactor3)
        .tier(3)
        .build()

    @JvmField
    val TokamakFusionReactor5 = TechTree.builder("tokamak_fusion_reactor5", "托卡马克聚变反应堆V", "Tokamak Fusion Reactor V")
        .description("最后一个甜甜圈，最极致的点瓶子", "The last donut, the ultimate time-twister overclocking")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(512 * 20 * 4800L).setEurekaItem(MultiBlockD.FUSION_REACTOR[UHV], 0.8f).build())
        .icon(MultiBlockD.FUSION_REACTOR[UEV].asStack())
        .prerequisites(TokamakFusionReactor4)
        .tier(4)
        .build()
    fun TechNode.addRewardDescription(descriptionCN: String, descriptionEN: String): TechNode {
        if (langMap is O2OOpenCacheHashMap) {
            langMap[this.name] = CNEN(descriptionCN, descriptionEN)
        } else {
            rewardLines.add(
                Component.translatable(OTHER_REWARD_LABEL).withStyle(
                    ChatFormatting.DARK_PURPLE,
                ).append(Component.translatable("gtocore.data.${this.name}").withStyle(ChatFormatting.GRAY)),
            )
        }
        return this
    }
}
