package com.gtocore.data.recipe.research

import com.gtocore.api.data.tag.GTOTagPrefix
import com.gtocore.api.data.tag.GTOTagPrefix.NANITES
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.CATALYSIS
import com.gtocore.api.research.ResearchTag.DATA_STORAGE
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.ResearchTag.EXOTIC
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.api.research.ResearchTag.OPTICS
import com.gtocore.api.research.ResearchTag.SUPRACAUSAL
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

import com.google.common.collect.ImmutableList
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
    val TierItems = ImmutableList.of(
        GTItems.TOOL_DATA_STICK,
        GTItems.TOOL_DATA_ORB,
        GTItems.TOOL_DATA_MODULE,
        GTOItems.NEURAL_MATRIX,
        GTOItems.ATOMIC_ARCHIVES,
        GTOItems.OBSIDIAN_MATRIX,
        GTOItems.CLOSED_TIMELIKE_CURVE_GUIDANCE_UNIT,
        GTOItems.MICROCOSM,
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
        .requirements(ResearchRequirements.Builder().setCWUNeeded(20).addMaterialNeeded(DATA_STORAGE, 120).setEurekaItem(GTResearchMachines.DATA_ACCESS_HATCH, 1.0f).build())
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
    val FuelRefineryComplex = TechTree.builder("fuel_refinery_complex", "燃料精炼综合管理", "Fuel Refinery Complex")
        .description("将能烧的东西处理成更能烧的东西的技术", "The technology of processing burnable things into more burnable things")
        .icon(RegistriesUtils.getItem("gtocore:fuel_refining_complex"))
        .prerequisites(ChemicalPlantEnvironmentControl)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:large_cracker"), 0.8F)
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
                .setEurekaItem(RegistriesUtils.getItem("gtceu:energy_cluster"), 0.8F)
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
                .addMaterialNeeded(DATA_STORAGE, 512)
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
                .addMaterialNeeded(DATA_STORAGE, 128)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:pattern_content_access_terminal"), if (GTOCore.isEasy()) 1f else 0.8F)
                .build(),
        )
        .tier(GTOCore.difficulty)
        .build()

    @JvmField
    val LargeNaquadahReactor = TechTree.builder("large_naquadah_reactor", "大型硅岩反应堆", "Large Naquadah Reactor")
        .description("硅岩这种材料怎么就这么神奇呢？又硬又坚韧，还能用来做反应堆的核心燃料", "How is naquadah such a magical material? It's hard and tough, and can even be used as the core fuel for reactors")
        .icon(RegistriesUtils.getItem("gtocore:large_naquadah_reactor"))
        .prerequisites(EnergyIOs[ZPM])
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:zpm_naquadah_reactor"), 0.8F)
                .build(),
        )
        .tier(2)
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
    val BiowareTech = TechTree.builder("bioware_tech", "生物件技术", "Bioware Technology")
        .description("这年头，蘑菇也会算数了", "These days, even mushrooms can do math")
        .icon(RegistriesUtils.getItem("gtocore:bioware_mainframe"))
        .prerequisites(WetwareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(BIOLOGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bioware_chip"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val BiowareDataStorage = TechTree.builder("bioware_data_storage", "生物件数据存储", "Bioware Data Storage")
        .description("利用生物件的自我复制能力，实现数据的高密度存储与快速访问", "Utilize the self-replication ability of bioware to achieve high-density data storage and fast access")
        .icon(RegistriesUtils.getItem("gtocore:bio_data_access_hatch"))
        .prerequisites(BiowareTech, DataCenter)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(BIOLOGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:advanced_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val SupercomputingTech = TechTree.builder("super_computing_tech", "超算技术", "Supercomputing Technology")
        .description("掌握超级计算机的设计与制造技术，供应更强大的算力与数据处理能力", "Master the design and manufacturing technology of supercomputers, providing more powerful computing power and data processing capabilities")
        .icon(RegistriesUtils.getItem("gtocore:supercomputing_center"))
        .prerequisites(ComputationArray)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(DATA_STORAGE, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:hpca_active_cooler_component"), 0.7F)
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
    val LaserEngraver = TechTree.builder("laser_engraver", "极细尺度激光导向刻蚀", "Ultra-Fine Scale Laser Guided Etching")
        .description("利用激光的极细尺度，进行导向刻蚀，制造出高精度的微结构", "Use the ultra-fine scale of lasers for guided etching, creating high-precision microstructures")
        .icon(RegistriesUtils.getItem("gtocore:non_linear_optical_lens"))
        .prerequisites(LaserBatchProduction0)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:high_frequency_laser"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val MolecularSeriesCasings = TechTree.builder("molecular_series_casings", "分子级系列外壳", "Molecular Series Casings")
        .description("一种看上去流淌着恐怖级能量的外壳，能够承受极端的能量流动", "A casing that appears to flow with terrifying levels of energy, capable of withstanding extreme energy flows")
        .icon(RegistriesUtils.getItem("gtocore:molecular_casing"))
        .prerequisites(IridiumCasingProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:molecular_casing"), 0.7F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val SpaceTimeAssemblyLine = TechTree.builder("space_time_assembly_line", "时空装配技术", "Space-Time Assembly Technology")
        .description("通过时空压缩技术，让产品在装配线中以更快的速度完成组装，同时减少能量与物质的消耗", "Through space-time compression technology, products can be assembled at a faster speed on the assembly line, while reducing energy and material consumption")
        .icon(RegistriesUtils.getItem("gtocore:spacetime_assembly_line_unit"))
        .prerequisites(AdvancedAssemblyLineMachine)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:advanced_assembly_line_unit"), 0.7F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val ComponentProductionEnhancement = TechTree.builder("component_production_enhancement", "组件生产强化", "Component Production Enhancement")
        .description("通过优化生产线和改进组件设计，实现大批量组件的高效节省生产", "Achieve efficient and cost-effective production of large quantities of components through optimized production lines and improved component design")
        .icon(RegistriesUtils.getItem("gtocore:component_assembly_line"))
        .prerequisites(LaserBatchProduction0, SpaceTimeAssemblyLine)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 9900L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:component_assembler"), 0.7F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val LaserBatchProduction1 = TechTree.builder("laser_batch_production", "激光能源批量生产初步", "Laser Energy Batch Production Preliminary")
        .description("利用高功率激光，传输大量能量用于超大批量的生产加工", "Use high-power lasers to transmit large amounts of energy for ultra-large-scale production and processing")
        .icon(RegistriesUtils.getItem("gtocore:machining_control_module_mk2"))
        .prerequisites(ComponentInAssemblyLineuhv, LaserBatchProduction0, MolecularSeriesCasings)
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
    val UltimateBattery2 = TechTree.builder("ultimate_battery2", "终极电池II", "Ultimate Battery II")
        .description("换了颜色的终极电池", "A different colored ultimate battery")
        .icon(GTOItems.REALLY_MAX_BATTERY)
        .prerequisites(UltimateEnergyStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 256)
                .setEurekaItem(GTItems.ULTIMATE_BATTERY, 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val LaserPlasmaCondenser = TechTree.builder("laser_plasma_condenser", "激光等离子体冷凝器", "Laser Plasma Condenser")
        .description("俺寻思热的东西不是因为它的热运动很强吗？那就用激光把它的热运动给定住不就好了", "I think the hot thing is that its thermal motion is very strong, right? Then just use a laser to fix its thermal motion, isn't it?")
        .icon(RegistriesUtils.getItem("gtocore:plasma_condenser"))
        .prerequisites(LaserBatchProduction1)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:laser_cooling_casing"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val ComplexPlasmaCondenser = TechTree.builder("compound_extreme_plasma_condenser", "复杂激光等离子体冷凝器", "Complex Laser Plasma Condenser")
        .description("GTO寰宇重工集团里最大的冰箱", "The largest refrigerator in GTO Universal Heavy Industries Group")
        .icon(RegistriesUtils.getItem("gtocore:compound_extreme_cooling_unit"))
        .prerequisites(LaserPlasmaCondenser)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:vacuum_freezer"), 0.7F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val SpaceElevator2 = TechTree.builder("space_elevator2", "太空电梯动力改良", "Space Elevator Power Improvement")
        .description("改良太空电梯的动力系统，实现更高效的能量传输与运输能力", "Improve the power system of the space elevator, achieving more efficient energy transmission and transportation capabilities")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_2"))
        .prerequisites(SpaceElevator)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 1200L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 96)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_1"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val SpaceElevator3 = TechTree.builder("space_elevator3", "太空电梯动力改良II", "Space Elevator Power Improvement II")
        .description("升级太空电梯的动力系统，实现更高效的能量传输与运输能力", "Upgrade the power system of the space elevator, achieving more efficient energy transmission and transportation capabilities")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_3"))
        .prerequisites(SpaceElevator2)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_2"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val DimensionSeriesCasings = TechTree.builder("dimension_series_casings", "维度级系列外壳", "Dimension Series Casings")
        .description("能够承载维度级别能量与力场的外壳，适用于极端环境下的设备保护与建造", "A casing capable of withstanding dimension-level energy and force fields, suitable for equipment protection and construction in extreme environments")
        .icon(RegistriesUtils.getItem("gtocore:dimensional_bridge_casing"))
        .prerequisites(MolecularSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:spacetime_assembly_line_casing"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val BedrockMining = TechTree.builder("bedrock_production", "基岩开采与加工", "Bedrock Mining and Processing")
        .description("你就不好奇MC里最坚不可摧的方块里面的物质组成吗？", "Aren't you curious about the material composition of the most indestructible block in Minecraft?")
        .icon(RegistriesUtils.getItem("gtocore:bedrock_drill"))
        .prerequisites(VoidMiner)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bedrock_drill"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val MatterFabricator = TechTree.builder("matter_fabricator", "物质制造机", "Matter Fabricator")
        .description("通过高能物理实验，将能量直接转化为物质，实现物质的直接制造", "Through high-energy physics experiments, directly convert energy into matter, achieving direct matter fabrication")
        .icon(RegistriesUtils.getItem("gtocore:matter_fabricator"))
        .prerequisites(HighDensityEnergyStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 128)
                .addMaterialNeeded(MATERIAL, 1280)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:recycler"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val OpticalTech = TechTree.builder("optical_tech", "光学计算技术", "Optical Computing Technology")
        .description("直接利用光子进行计算，摆脱电子的限制，实现更高效的计算与数据处理", "Directly use photons for computing, breaking free from the limitations of electrons, achieving more efficient computing and data processing")
        .icon(RegistriesUtils.getItem("gtocore:optical_processing_core"))
        .prerequisites(BiowareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(OPTICS, 128)
                .setEurekaItem(GTOItems.SIMPLE_OPTICAL_SOC, 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val HighEnergyBioEngineering = TechTree.builder("high_energy_bio_engineering", "高能生物工程", "High Energy Bio Engineering")
        .description("生物技术的巅峰之作", "The pinnacle of biotechnology")
        .icon(RegistriesUtils.getItem("gtocore:microorganism_master"))
        .prerequisites(NanitesTech, LaserBatchProduction1)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(BIOLOGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:law_filter_casing"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val StellarForge = TechTree.builder("stellar_forge", "恒星锻造技术", "Stellar Forge Technology")
        .description("将恒星级别的能量用于物质加工，制造出超高性能的材料。祈祷它别爆炸吧", "Use stellar-level energy for material processing, creating ultra-high-performance materials. Pray it doesn't explode")
        .icon(RegistriesUtils.getItem("gtocore:stellar_forge"))
        .prerequisites(MolecularSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:stellar_containment_casing"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val BlockholeDataStorage = TechTree.builder("blockhole_data_storage", "黑洞数据存储技术", "Black Hole Data Storage Technology")
        .description("利用黑洞的极端引力场，将数据压缩存储在黑洞中，实现超大规模的数据存储与管理", "Use the extreme gravitational field of black holes to compress and store data in black holes, achieving ultra-large-scale data storage and management")
        .icon(RegistriesUtils.getItem("gtocore:black_hole_data_access_hatch"))
        .prerequisites(BiowareDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(DATA_STORAGE, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bio_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val LaserEngraver2 = TechTree.builder("laser_engraver2", "维度聚焦激光蚀刻技术", "Dimensional Focusing Laser Etching Technology")
        .description("从不同维度给它打光，让它在不同维度的光线下进行蚀刻，制造出更高精度的微结构", "Illuminate it from different dimensions, allowing it to etch under light from different dimensions, creating higher precision microstructures")
        .icon(RegistriesUtils.getItem("gtocore:dimensional_focus_engraving_array"))
        .prerequisites(LaserEngraver, LaserBatchProduction1)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:engraving_laser_plant"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val SPSTech = TechTree.builder("sps_tech", "超临界移相技术", "Supercritical Phase Shift Technology")
        .description("偷偷告诉你实际上它的工作原理是哭泣黑曜石在一边嘬超然物质一边看煽情片", "I'll tell you a secret, its working principle is actually crying obsidian sucking transcending matter on one side while watching a tear-jerking movie on the other side")
        .icon(RegistriesUtils.getFluid("gtocore:transcending_matter"))
        .prerequisites(TokamakFusionReactor4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:pellet_antimatter"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val AtomicEnergyExciting = TechTree.builder("atomic_energy_exciting", "原子能激发技术", "Atomic Energy Excitation Technology")
        .description("通过激发原子核的能量，充分压榨原子能的潜力，生产出更高能量密度的燃料", "By exciting the energy of atomic nuclei, fully exploiting the potential of atomic energy, producing fuel with higher energy density")
        .icon(RegistriesUtils.getItem("gtocore:atomic_energy_excitation_plant"))
        .prerequisites(FuelRefineryComplex, SPSTech, LargeNaquadahReactor)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:large_naquadah_reactor"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val RareEarthProcessing = TechTree.builder("rare_earth_processing", "稀土直接分离技术", "Rare Earth Direct Separation Technology")
        .description("直接分离稀土矿产中的所有元素，无需经过复杂的化学处理过程，实现高效的稀土资源利用", "Directly separate all elements in rare earth minerals without complex chemical processing, achieving efficient utilization of rare earth resources")
        .icon(RegistriesUtils.getItem("gtocore:comprehensive_tombarthite_processing_facility"))
        .prerequisites(VoidMiner)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 1280)
                .setEurekaFluid(GTOMaterials.RareEarthChlorides.getFluid(), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val PlanetCoreExtraction = TechTree.builder("planet_core_extraction", "行星核心提取技术", "Planet Core Extraction Technology")
        .description("使用抽空星球级别的矿机，将行星的每个角落都挖空，提取出极其大量的矿产资源", "Using planet-emptying level mining machines, excavate every corner of the planet to extract an extremely large amount of mineral resources")
        .icon(RegistriesUtils.getItem("gtocore:planet_core_drilling"))
        .prerequisites(VoidMiner)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 2560)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:large_void_miner"), 0.7F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val DysonSphereSeriesCasing = TechTree.builder("dyson_sphere_series_casing", "戴森球系列外壳", "Dyson Sphere Series Casing")
        .description("能够长时间耐受恒星辐射的外壳，适用于戴森球的建造与维护", "A casing that can withstand stellar radiation for a long time, suitable for the construction and maintenance of Dyson spheres")
        .icon(RegistriesUtils.getItem("gtocore:dyson_deployment_core"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:dyson_control_casing"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val DysonSphere = TechTree.builder("dyson_sphere", "戴森球建造技术", "Dyson Sphere Construction Technology")
        .description("掌握戴森球的建造技术，通过发射大量的戴森球组件，最终在恒星周围形成一个完整的戴森球，实现对恒星能量的最大化利用", "Master the construction technology of Dyson spheres, by launching a large number of Dyson sphere components, eventually forming a complete Dyson sphere around the star, achieving maximum utilization of stellar energy")
        .icon(RegistriesUtils.getItem("gtocore:dyson_sphere_launch_silo"))
        .prerequisites(DysonSphereSeriesCasing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 256)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:dyson_swarm_module"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val CryotheumSupercoductingTech = TechTree.builder("cryotheum_superconducting_tech", "凛冰超导技术", "Cryotheum Superconducting Technology")
        .description("使用凛冰循环浸淋超导材料，进一步提升超导导体性能的稳定性", "Use cryotheum circulation to immerse superconducting materials, further improving the stability of superconducting performance")
        .icon(GTOFluids.GELID_CRYOTHEUM.get())
        .prerequisites(SuperConductingMaterialResearch, SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 3600L)
                .addMaterialNeeded(MATERIAL, 8000)
                .setEurekaFluid(GTOFluids.GELID_CRYOTHEUM.get(), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SpaceElevator4 = TechTree.builder("space_elevator4", "太空电梯动力改良III", "Space Elevator Power Improvement III")
        .description("我要是能乘坐在上面观光就好了", "I wish I could take a sightseeing ride on it")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_4"))
        .prerequisites(SpaceElevator3)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 160)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_3"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val UltimateBattery3 = TechTree.builder("ultimate_battery3", "终极电池III", "Ultimate Battery III")
        .description("换了颜色的终极电池II", "A different colored ultimate battery II")
        .icon(GTOItems.TRANSCENDENT_MAX_BATTERY)
        .prerequisites(UltimateBattery2)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 512)
                .setEurekaItem(GTOItems.REALLY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val AdvancedHyperReactor = TechTree.builder("advanced_hyper_reactor", "进阶超高能反应堆", "Advanced Hyper Reactor")
        .description("用于对更加浓缩的超高能硅岩燃料进行反应的超高能反应堆，能够提供更高的能量输出与更稳定的运行性能", "A hyper reactor used for reacting with more concentrated hyper-silicon fuel, capable of providing higher energy output and more stable operating performance")
        .icon(RegistriesUtils.getItem("gtocore:advanced_hyper_reactor"))
        .prerequisites(AtomicEnergyExciting, UltimateBattery3)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:hyper_reactor"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SuprachronalAssemblyLine = TechTree.builder("suprachronal_assembly_line", "超时空装配线", "Suprachronal Assembly Line")
        .description("装配线已经是极限了？不，GTO寰宇重工的工程师们已经突破了时空的限制，让装配线在不同的时空中同时运作，实现了超时空的装配生产", "The assembly line has reached its limit? No, the engineers of GTO Universal Heavy Industries have broken the limits of space-time, allowing the assembly line to operate simultaneously in different space-times, achieving suprachronal assembly production")
        .icon(RegistriesUtils.getItem("gtocore:nyarlathoteps_tentacle"))
        .prerequisites(LaserBatchProduction1, SpaceTimeAssemblyLine)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MECHANICS, 160)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:circuit_assembly_line"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val GWCAComputingTech = TechTree.builder("gwca_computing_tech", "GWCA计算技术", "GWCA Computing Technology")
        .description("通过操纵引力波的传播与干涉，实现对信息的传输与处理，能够实现超越传统计算机的运算能力", "By manipulating the propagation and interference of gravitational waves, achieve information transmission and processing, capable of achieving computational power beyond traditional computers")
        .icon(RegistriesUtils.getItem("gtocore:gwca_empty_component"))
        .prerequisites(SupercomputingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:graviton_computer_casing"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SpaceElevator5 = TechTree.builder("space_elevator5", "太空电梯动力改良IV", "Space Elevator Power Improvement IV")
        .description("全GTO寰宇重工最快的电梯！", "The fastest elevator in the entire GTO Universal Heavy Industries!")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_5"))
        .prerequisites(SpaceElevator4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 9600L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 200)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_4"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SpaceProbeSurfaceReception = TechTree.builder("space_probe_surface_reception", "空间探测器表面接收技术", "Space Probe Surface Reception Technology")
        .description("将空间中杂乱的辐射能量进行收集与转换，转化为可用的资源", "Collect and convert the chaotic radiation energy in space into usable resources")
        .icon(RegistriesUtils.getItem("gtocore:space_probe_surface_reception"))
        .prerequisites(SpaceElevator5)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:cosmic_detection_receiver_material_ray_absorbing_array"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val MassEnergyConversionTech = TechTree.builder("mass_energy_conversion_tech", "质能转换技术", "Mass-Energy Conversion Technology")
        .description("掌握质能转换的核心技术，实现物质与能量的(不太高效)的互换", "Master the core technology of mass-energy conversion, achieving (not very efficient) interchange between matter and energy")
        .icon(RegistriesUtils.getItem("gtocore:mass_fabricator"))
        .prerequisites(SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:matter_fabricator"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val EnergyInjectedFissionTech = TechTree.builder("energy_injected_fission_tech", "能量注入裂变技术", "Energy-Injected Fission Technology")
        .description("通过向裂变反应堆注入高能粒子，提升裂变反应的效率与能量输出", "By injecting high-energy particles into the fission reactor, improve the efficiency and energy output of the fission reaction")
        .icon(RegistriesUtils.getItem("gtocore:entropy_flux_engine"))
        .prerequisites(ParticleAccelerators, LaserBatchProduction1)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:graviton_field_constraint_casing"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val TimeDilationTech = TechTree.builder("time_dilation_tech", "时间膨胀技术", "Time Dilation Technology")
        .description("利用相对论效应，控制时间流速，能够在现实时间尺度上完成时间条件苛刻的实验", "Use relativistic effects to control the flow of time, allowing experiments with stringent time conditions to be completed on a real-time scale")
        .icon(RegistriesUtils.getItem("gtocore:temporal_matter"))
        .prerequisites(SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:empty_laser_cooling_container"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val ExoticTech = TechTree.builder("exotic_technology", "奇异处理器技术", "Exotic Processor Technology")
        .description("操纵时空与物理定律用于计算的技术，能够实现超越传统计算机的运算能力", "Technology that manipulates spacetime and physical laws for computation, capable of achieving computational power beyond traditional computers")
        .icon(RegistriesUtils.getItem("gtocore:exotic_processing_core"))
        .prerequisites(OpticalTech, TimeDilationTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(EXOTIC, 16)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:exotic_chip"), 0.7F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val LeptonicCharge = TechTree.builder("leptonic_charge", "轻子爆弹", "Leptonic Charge")
        .description("威力极其强大的爆弹，几乎是万亿亿级的TNT当量", "An extremely powerful explosive, almost equivalent to a trillion trillion TNT")
        .icon(RegistriesUtils.getItem("gtocore:leptonic_charge"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:naquadria_charge"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val CosmicTech = TechTree.builder("cosmic_technology", "寰宇处理器技术", "Cosmic Processor Technology")
        .description("通过预设条件模拟宇宙演变，进行计算的处理器架构", "A processor architecture that simulates the evolution of the universe under preset conditions for computation")
        .icon(RegistriesUtils.getItem("gtocore:cosmic_processing_core"))
        .prerequisites(ExoticTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(EXOTIC, 16)
                .setEurekaItem(GTOItems.COSMIC_PROCESSING_UNIT_CORE, 0.7F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val ExDurablePlasmaContainer = TechTree.builder("ex_durable_plasma_container", "高耐久等离子体容器", "Ex-Durable Plasma Container")
        .description("用于存储高能等离子体的容器，能够承受极端的温度和压力", "A container for storing high-energy plasma, capable of withstanding extreme temperatures and pressures")
        .icon(RegistriesUtils.getItem("gtocore:extremely_durable_plasma_cell"))
        .prerequisites(LaserPlasmaCondenser)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:plasma_containment_cell"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val MagneticConfinementDimensionallyShockProcess = TechTree.builder("magnetic_confinement_dimensionally_shock_process", "磁约束维度冲击工艺", "Magnetic Confinement Dimensionally Shock Process")
        .description("通过磁约束技术，将物质在不同维度下进行冲击处理，实现物质的维度级融合", "Through magnetic confinement technology, subject matter to shock processing in different dimensions, achieving dimensional-level fusion of matter")
        .icon(RegistriesUtils.getItem("gtocore:magnetic_confinement_dimensionality_shock_device"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:kerr_newman_homogenizer"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val QuantumChromodynamicCharge = TechTree.builder("quantum_chromodynamic_charge", "量子色动力学爆弹", "Quantum Chromodynamic Charge")
        .description("别把它点了...至少别在你面前点了它", "Don't light it... at least don't light it in front of you")
        .icon(RegistriesUtils.getItem("gtocore:quantum_chromodynamic_charge"))
        .prerequisites(LeptonicCharge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 16000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:leptonic_charge"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val ManifoldOscillatory = TechTree.builder("manifold_oscillatory", "多维流形振荡技术", "Manifold Oscillatory Technology")
        .description("通过多维流形的振荡，实现对时空的微观调控，能够在实验室中模拟宇宙级别的物理现象", "Achieve microscopic control of spacetime through oscillations of multi-dimensional manifolds, capable of simulating cosmic-level physical phenomena in the laboratory")
        .icon(RegistriesUtils.getItem("gtocore:manifold_oscillatory_power_cell"))
        .prerequisites(ExoticTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:exotic_processing_core"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val UltimateBattery4 = TechTree.builder("ultimate_battery4", "终极电池IV", "Ultimate Battery IV")
        .description("颜色更加鲜艳的终极电池III", "An even more colorful ultimate battery III")
        .icon(GTOItems.EXTREMELY_MAX_BATTERY)
        .prerequisites(UltimateBattery3)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 8000L)
                .addMaterialNeeded(ENERGY, 1024)
                .setEurekaItem(GTOItems.TRANSCENDENT_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val VirtualUniverseDataStorage = TechTree.builder("virtual_universe_data_storage", "虚拟宇宙数据存储技术", "Virtual Universe Data Storage Technology")
        .description("通过模拟一个完整的虚拟宇宙，将数据存储在其中，实现超大规模的数据存储与管理", "By simulating a complete virtual universe, data is stored within it, achieving ultra-large-scale data storage and management")
        .icon(RegistriesUtils.getItem("gtocore:virtual_universe_data_access_hatch"))
        .prerequisites(BlockholeDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(DATA_STORAGE, 256)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:black_hole_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val NanitesMassiveProduction = TechTree.builder("nanites_massive_production", "纳米蜂群批量复制技术", "Nanites Massive Production Technology")
        .description("通过纳米蜂群的自我复制与协作，实现纳米蜂群的批量生产与应用", "Achieve mass production and application of nanite swarms through self-replication and collaboration of nanite swarms")
        .icon(RegistriesUtils.getItem("gtocore:swarm_core"))
        .prerequisites(NanitesTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:nano_forge"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val AdvancedMassFabricationTech = TechTree.builder("advanced_mass_fabrication_tech", "进阶质能制造技术", "Advanced Mass Fabrication Technology")
        .description("比最初研究的那版质能制造技术省电", "More energy-efficient than the original mass fabrication technology")
        .icon(RegistriesUtils.getItem("gtocore:advanced_mass_fabricator"))
        .prerequisites(MassEnergyConversionTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:mass_fabricator"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val ElementFabricationTech = TechTree.builder("element_fabrication_tech", "元素制造技术", "Element Fabrication Technology")
        .description("操纵物质的基本构成，实现对元素的直接制造与转换", "Manipulate the fundamental composition of matter, achieving direct fabrication and conversion of elements")
        .icon(RegistriesUtils.getItem("gtocore:element_copying"))
        .prerequisites(MassEnergyConversionTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:star_ultimate_material_forge_factory"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val SupracausalTech = TechTree.builder("supracausal_tech", "超因果技术", "Supracausal Technology")
        .description("掌握超越因果律的技术，能够在问题提出之前就得到答案，实现对未来的预测与控制", "Master technology that transcends causality, allowing answers to be obtained before questions are even asked, achieving prediction and control of the future")
        .icon(RegistriesUtils.getItem("gtocore:supracausal_processing_core"))
        .prerequisites(CosmicTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(SUPRACAUSAL, 1)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:relativistic_spinorial_memory_system"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val StellarUltimateForge = TechTree.builder("stellar_ultimate_forge", "恒星终极锻造技术", "Stellar Ultimate Forge Technology")
        .description("制造出只在恒星中心才能存在的材料的技术，能够制造出超越已知物理极限的材料", "Technology that creates materials that can only exist at the center of stars, capable of producing materials that surpass known physical limits")
        .icon(RegistriesUtils.getItem("gtocore:proto_matter"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:ultimate_stellar_containment_casing"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val HyperDimensionalForge = TechTree.builder("hyper_dimensional_forge", "超维锻造技术", "Hyper-Dimensional Forge Technology")
        .description("通过操纵高维空间的物理规律，实现对物质的超维度锻造，制造出超越三维空间极限的材料", "By manipulating the physical laws of higher-dimensional space, achieve hyper-dimensional forging of matter, producing materials that surpass the limits of three-dimensional space")
        .icon(RegistriesUtils.getItem("gtocore:hyperdimensional_plasma_fusion_core"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 8000L)
                .addMaterialNeeded(MATERIAL, 32000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:stellar_forge"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val QFTSeriesCasing = TechTree.builder("qft_series_casing", "量子场论级系列外壳", "Quantum Field Theory Series Casings")
        .description("能够可控扭曲时空的外壳，适用于相关机器的建造", "A casing capable of controllably warping spacetime, suitable for the construction of related machines")
        .icon(RegistriesUtils.getItem("gtocore:spacetime_bending_core"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:manipulator"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val QFTManipulator = TechTree.builder("qft_manipulator", "量子场论操纵器", "Quantum Field Theory Manipulator")
        .description("通过操纵量子场的波动，实现对物质与能量的精确控制", "By manipulating the fluctuations of quantum fields, achieve precise control over matter and energy")
        .icon(RegistriesUtils.getItem("gtocore:quantum_force_transformer"))
        .prerequisites(QFTSeriesCasing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:quantum_force_transformer_coil"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val DragonCore = TechTree.builder("dragon_core", "龙之能量核心", "Dragon Energy Core")
        .description("通过操纵龙之能量的流动，实现对能量的极致掌控", "By manipulating the flow of dragon energy, achieve ultimate control over energy")
        .icon(RegistriesUtils.getItem("gtocore:wyvern_core"))
        .prerequisites(SupracausalTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(16384 * 20 * 28800L)
                .addMaterialNeeded(EXOTIC, 4)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:draconic_core"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val TimeDilationDimensionSeriesCasing = TechTree.builder("time_dilation_dimension_series_casing", "时间膨胀维度级系列外壳", "Time Dilation Dimension Series Casings")
        .description("能够在不同时间流速下稳定运作的外壳，适用于相关机器的建造", "A casing capable of stable operation under different time flow rates, suitable for the construction of related machines")
        .icon(RegistriesUtils.getItem("gtocore:dimensional_stability_casing"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:dimensional_bridge_casing"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val AnnihilationGenerator = TechTree.builder("annihilation_generator", "湮灭发电机", "Annihilation Generator")
        .description("通过物质与反物质的湮灭反应，产生巨大的能量输出", "Generate enormous energy output through matter-antimatter annihilation reactions")
        .icon(RegistriesUtils.getItem("gtocore:annihilate_generator"))
        .prerequisites(AdvancedHyperReactor)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 1024)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:annihilation_constrainer"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val HyperDimensionalForgeCoil = TechTree.builder("hyper_dimensional_forge_coil", "超维锻造线圈改良", "Hyper-Dimensional Forge Coil Improvement")
        .description("用于超维锻造的线圈，汇聚来自高维空间的热量", "A coil used for hyper-dimensional forging, gathering heat from higher-dimensional space")
        .icon(RegistriesUtils.getItem("gtocore:infinity_coil_block"))
        .prerequisites(HyperDimensionalForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 10800L)
                .addMaterialNeeded(MECHANICS, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:starmetal_coil_block"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val UltimateBattery5 = TechTree.builder("ultimate_battery5", "终极电池V", "Ultimate Battery V")
        .description("看起来比较疯狂的终极电池IV", "A seemingly insane ultimate battery IV")
        .icon(GTOItems.INSANELY_MAX_BATTERY)
        .prerequisites(UltimateBattery4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 1024)
                .setEurekaItem(GTOItems.EXTREMELY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val UniverseSimulation = TechTree.builder("universe_simulation", "宇宙模拟技术", "Universe Simulation Technology")
        .description("宇宙冷漠，这张牌我是非常了解的", "The universe is indifferent, and I am very familiar with this card")
        .icon(RegistriesUtils.getItem("gtocore:eye_of_harmony"))
        .prerequisites(VirtualUniverseDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(16384 * 20 * 28800L)
                .addMaterialNeeded(DATA_STORAGE, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:recursively_folded_negative_space"), 0.7F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val AwakenedCore = TechTree.builder("awakened_core", "觉醒核心", "Awakened Core")
        .description("觉醒你内在的神龙之力", "Awaken your inner dragon power")
        .icon(RegistriesUtils.getItem("gtocore:awakened_core"))
        .prerequisites(DragonCore)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(16384 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 1)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:wyvern_core"), 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val UltimateBattery6 = TechTree.builder("ultimate_battery6", "终极电池VI", "Ultimate Battery VI")
        .description("终极电池的终极形态，看着很帅", "The ultimate form of the ultimate battery, looks very cool")
        .icon(GTOItems.MEGA_MAX_BATTERY)
        .prerequisites(UltimateBattery5)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 28800L)
                .addMaterialNeeded(ENERGY, 1024)
                .setEurekaItem(GTOItems.INSANELY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val ChaosCore = TechTree.builder("chaos_core", "混沌核心", "Chaos Core")
        .description("§k混沌混沌混沌混沌混沌§r", "§kChaos C haos Ch aosCha osChaos§r")
        .icon(RegistriesUtils.getItem("gtocore:chaotic_core"))
        .prerequisites(AwakenedCore)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32768 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 2)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:awakened_core"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val SuprachronalDrone = TechTree.builder("suprachronal_drone", "超时空无人机", "Suprachronal Drone")
        .description("在维度之外工作，随意的穿梭于不同的时空中", "Working outside of dimensions, freely shuttling through different space-times")
        .icon(RegistriesUtils.getItem("gtocore:hyperdimensional_drone"))
        .prerequisites(SuprachronalAssemblyLine)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32768 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 16)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:chaotic_core"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val SuprachronalCircuits = TechTree.builder("suprachronal_circuits", "超时空电路", "Suprachronal Circuits")
        .description("随意的提供任意级别的计算能力", "Freely provide any level of computing power")
        .icon(RegistriesUtils.getItem("gtocore:suprachronal_circuit_max"))
        .prerequisites(SuprachronalDrone)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32768 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 16)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:supracausal_mainframe"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val Create = TechTree.builder("create", "创造", "Create")
        .description("创造一切", "Create everything")
        .icon(RegistriesUtils.getItem("minecraft:command_block"))
        .prerequisites(ChaosCore)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(65536 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 4)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:mega_max_battery"), 0.4F)
                .build(),
        )
        .tier(7)
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
