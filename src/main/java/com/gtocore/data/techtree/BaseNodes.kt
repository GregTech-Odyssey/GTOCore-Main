package com.gtocore.data.techtree

import com.gtocore.api.data.tag.GTOTagPrefix
import com.gtocore.api.data.tag.GTOTagPrefix.NANITES
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.ASSEMBLY
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.COMPUTATION
import com.gtocore.api.research.ResearchTag.DATA_STORAGE
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.ResearchTag.EXOTIC
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.api.research.ResearchTag.OPTICS
import com.gtocore.api.research.ResearchTag.QUANTUM
import com.gtocore.api.research.ResearchTag.SUPRACAUSAL
import com.gtocore.api.research.ResearchTag.THERMODYNAMICS
import com.gtocore.api.research.techtree.TechNode
import com.gtocore.api.research.techtree.TechNode.OTHER_REWARD_LABEL
import com.gtocore.api.research.techtree.TechTreeManager
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOFluids
import com.gtocore.common.data.GTOItems
import com.gtocore.common.data.GTOMaterials
import com.gtocore.common.data.machines.ExResearchMachines
import com.gtocore.common.data.machines.MultiBlockA.CHEMICAL_PLANT
import com.gtocore.common.data.machines.MultiBlockD
import com.gtocore.data.techtree.ComponentNodes.ComponentInAssemblyLineluv
import com.gtocore.data.techtree.ComponentNodes.ComponentInAssemblyLineuhv
import com.gtocore.data.techtree.ComponentNodes.EnergyIOs
import com.gtocore.data.techtree.MachinesNode.LaserPlasmaCondenser
import com.gtocore.data.techtree.MachinesNode.MolecularSeriesCasings
import com.gtocore.data.techtree.MachinesNode.SuprachronalAssemblyLine

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import com.google.common.collect.ImmutableList
import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.LuV
import com.gregtechceu.gtceu.api.GTValues.UEV
import com.gregtechceu.gtceu.api.GTValues.UHV
import com.gregtechceu.gtceu.api.GTValues.UV
import com.gregtechceu.gtceu.api.GTValues.ZPM
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.LARGE_CHEMICAL_REACTOR
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import com.gtolib.api.lang.CNEN
import com.gtolib.utils.RegistriesUtils
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import earth.terrarium.adastra.common.registry.ModItems

object BaseNodes : AutoInitialize<BaseNodes>() {

    val langMap: Map<String, CNEN> = if (GTCEu.isDataGen()) O2OOpenCacheHashMap() else emptyMap()

    init {
        if (!GTCEu.isDataGen()) TechTreeManager.REGISTRY.unfreeze()
    }

    @JvmField
    val MainTree: TechTreeManager =
        TechTreeManager("main_tree", "主研究", "Main Research", ItemStackTexture(GTOItems.BLUE_HALIDE_LAMP.asStack()))

    @JvmField
    val AETree: TechTreeManager =
        TechTreeManager("ae_tree", "AE研究", "AE Research", ItemStackTexture(AEParts.TERMINAL.asItem()))

    @JvmField
    val EnergyTree: TechTreeManager =
        TechTreeManager("energy_tree", "能源生产与传输", "Energy Production and Transmission", ItemStackTexture(GTOItems.EXTREMELY_MAX_BATTERY.asStack()))

    @JvmField
    val ComponentTree: TechTreeManager =
        TechTreeManager("component_tree", "部件装配", "Component Assembly", ItemStackTexture(GTItems.EMITTER_LuV.asStack()))

    @JvmField
    val NanitesTree: TechTreeManager =
        TechTreeManager("nanites_tree", "纳米蜂群技术应用", "Nanites Application", ItemStackTexture(ChemicalHelper.get(NANITES, GTMaterials.Copper)))

    @JvmField
    val SpaceTree: TechTreeManager =
        TechTreeManager("space_tree", "太空发掘与利用", "Space Exploration and Utilization", ItemStackTexture(ModItems.TIER_4_ROCKET.get()))

    @JvmField
    val MachinesTree: TechTreeManager =
        TechTreeManager("machines_tree", "高新机器与生产线", "High-Tech Machines and Production Lines", ItemStackTexture(GTBlocks.SUPERCONDUCTING_COIL.asStack()))

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
        ComponentNodes.init()
        AENodes.init()
        EnergyNodes.init()
        SpaceNodes.init()
        NanitesNodes.init()
        MachinesNode.init()
        if (!GTCEu.isDataGen()) TechTreeManager.REGISTRY.freeze()
        MainTree.freeze()
        EnergyTree.freeze()
        AETree.freeze()
        ComponentTree.freeze()
        SpaceTree.freeze()
        NanitesTree.freeze()
        MachinesTree.freeze()
    }

    @JvmField
    val NuclearPhysics = MainTree.builder("nuclear_physics", "核物理研究", "Nuclear Physics Research")
        .description("研究原子核的结构和行为，以及核反应的机制和应用", "Study the structure and behavior of atomic nuclei, as well as the mechanisms and applications of nuclear reactions")
        .requirements(
            ResearchRequirements.Builder().setCWUNeeded(15)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:uranium_stainless_steel_target"), 1.0F).build(),
        )
        .icon(RegistriesUtils.getItem("gtocore:uranium_excited_stainless_steel_target"))
        .build()

    @JvmField
    val Thermodynamics = MainTree.builder("thermodynamics", "热力学研究", "Thermodynamics Research")
        .description("研究能量传递和转化的规律，以及热力学系统的行为和性能", "Study the laws of energy transfer and transformation, as well as the behavior and performance of thermodynamic systems")
        .requirements(
            ResearchRequirements.Builder().setCWUNeeded(15)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:electric_heater"), 1.0F).build(),
        )
        .icon(RegistriesUtils.getItem("gtocore:electric_heater"))
        .build()

    @JvmField
    val LaserFoundations = MainTree.builder("laser_foundations", "激光基础研究", "Laser Foundations Research")
        .description("研究激光的产生、传播和应用，以及激光技术在科学和工业中的潜力", "Study the generation, propagation, and application of lasers, as well as the potential of laser technology in science and industry")
        .requirements(
            ResearchRequirements.Builder().setCWUNeeded(15)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:normal_laser_pipe"), 1.0F).build(),
        )
        .icon(RegistriesUtils.getItem("gtceu:active_transformer"))
        .build()

    @JvmField
    val SuperConductingMaterialResearch = MainTree.builder("super_conducting_material_research", "超导材料研究", "Superconducting Material Research")
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
    val SupercriticalPhaseBasicResearch = MainTree.builder("supercritical_phase_basic_research", "超临界相态基础研究", "Supercritical Phase Basic Research")
        .description("研究物质在极端条件下处于超临界相态的物理特性与应用", "Study the physical properties and applications of matter in a supercritical phase under extreme conditions")
        .icon(GTOMaterials.SupercriticalSteam.fluid)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 300L)
                .setEurekaFluid(GTOMaterials.SupercriticalSteam.fluid, 1.0F)
                .build(),
        )
        .build()

    @JvmField
    val TokamakFusionReactor = MainTree.builder("tokamak_fusion_reactor", "托卡马克聚变反应堆", "Tokamak Fusion Reactor")
        .description("掌握可控的托卡马克聚变反应堆技术，实现元素的聚变与等离子体的生产", "Master the technology of controllable Tokamak fusion reactors, achieving element fusion and plasma production")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTBlocks.SUPERCONDUCTING_COIL, 1.0f).build())
        .icon(FUSION_REACTOR[LuV].asStack())
        .prerequisites(SuperConductingMaterialResearch, NuclearPhysics)
        .build()

    @JvmField
    val TokamakFusionReactor2 = MainTree.builder("tokamak_fusion_reactor2", "托卡马克聚变反应堆II", "Tokamak Fusion Reactor II")
        .description("更甜的甜甜圈，更高的温度，更强的磁场，更快的聚变", "Sweeter donut, higher temperature, stronger magnetic field, faster fusion")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 240L).setEurekaItem(FUSION_REACTOR[LuV], 0.8f).build())
        .icon(FUSION_REACTOR[ZPM].asStack())
        .prerequisites(TokamakFusionReactor)
        .tier(1)
        .build()

    @JvmField
    val TokamakFusionReactor3 = MainTree.builder("tokamak_fusion_reactor3", "托卡马克聚变反应堆III", "Tokamak Fusion Reactor III")
        .description("甜甜圈3号，甜度超标！", "Donut No. 3, sweetness overload!")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(128 * 20 * 1200L).setEurekaItem(FUSION_REACTOR[ZPM], 0.8f).build())
        .icon(FUSION_REACTOR[UV].asStack())
        .prerequisites(TokamakFusionReactor2)
        .tier(2)
        .build()

    @JvmField
    val TokamakFusionReactor4 = MainTree.builder("tokamak_fusion_reactor4", "托卡马克聚变反应堆IV", "Tokamak Fusion Reactor IV")
        .description("甜甜圈4号，想造什么元素自己填", "Donut No. 4, fill in whatever element you want to make")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(256 * 20 * 2400L).setEurekaItem(FUSION_REACTOR[UV], 0.8f).build())
        .icon(MultiBlockD.FUSION_REACTOR[UHV].asStack())
        .prerequisites(TokamakFusionReactor3)
        .tier(3)
        .build()

    @JvmField
    val TokamakFusionReactor5 = MainTree.builder("tokamak_fusion_reactor5", "托卡马克聚变反应堆V", "Tokamak Fusion Reactor V")
        .description("最后一个甜甜圈，最极致的点瓶子", "The last donut, the ultimate time-twister overclocking")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(512 * 20 * 4800L).setEurekaItem(MultiBlockD.FUSION_REACTOR[UHV], 0.8f).build())
        .icon(MultiBlockD.FUSION_REACTOR[UEV].asStack())
        .prerequisites(TokamakFusionReactor4)
        .tier(4)
        .build()

    @JvmField
    val DataBase = MainTree.builder("data_base", "数据库", "Data Base")
        .description("数据库是一个用于存储和管理数据的系统，安装数据仓与光学传输仓实现生产线数据的存储和路由", "The database is a system for storing and managing data, installing data warehouses and optical transmission warehouses to achieve storage and routing of production line data")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTResearchMachines.DATA_ACCESS_HATCH, 1.0f).build())
        .icon(GTResearchMachines.DATA_BANK)
        .build()

    @JvmField
    val ComputationArray = MainTree.builder("computation_array", "算力供应传输基础", "Computation Supply and Transmission Foundation")
        .description("搭建基础算力供应与多源算力分配逻辑", "Build a basic computation supply and multi-source computation distribution logic")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.COVER_SCREEN, 1.0f).build())
        .icon(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY)
        .prerequisites(DataBase)
        .build()

    @JvmField
    val ScanStation = MainTree.builder("scan_station", "扫描站", "Scan Station")
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
    val DataCenter = MainTree.builder("data_in_research", "科研数据中心", "Data Center in Research")
        .description("科研数据中心是一个用于存储和处理科研数据的高性能计算平台，相比于数据库，它可以将其中标准化的生产数据与积累的科研数据进行整合，推进基地技术迭代", "The research data center is a high-performance computing platform for storing and processing research data. Compared to databases, it can integrate standardized production data with accumulated research data, promoting base technology iteration")
        .requirements(
            ResearchRequirements.Builder().setCWUNeeded(20)
                .addMaterialNeeded(DATA_STORAGE, 14)
                .addMaterialNeeded(COMPUTATION, 120)
                .setEurekaItem(GTResearchMachines.DATA_BANK, 1.0f).build(),
        )
        .icon(GTResearchMachines.DATA_BANK)
        .prerequisites(DataBase)
        .build()

    @JvmField
    val CrystalTechMainframe = MainTree.builder("crystal_tech_mainframe", "晶体技术主机", "Crystal Technology Mainframe")
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
    val VirtualCoinCurrency = MainTree.builder("virtual_coin_currency", "虚拟货币", "Virtual Coin Currency")
        .description("给了冰冷的溢出算力一种独特的用法，通过帮别人计算一串随机的数字，换成温暖的虚拟（？）货币", "Give the cold and lifeless overflow computing power a unique use, by calculating a string of random numbers for others, in exchange for warm virtual (?) currency")
        .icon(RegistriesUtils.getItem("gtocore:infinity_coin"))
        .prerequisites(ComputationArray)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(128 * 20 * 400L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:gold_coin"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val DataStorageIteration = MainTree.builder("data_storage_iteration", "数据存储迭代", "Data Storage Iteration")
        .description("随着数据量的需求爆炸量的增加，数据存储技术也需要不断迭代升级，以满足更高效的数据存储和访问需求", "As the demand for data volume increases explosively, data storage technology also needs to be continuously iterated and upgraded to meet more efficient data storage and access needs")
        .icon(RegistriesUtils.getItem("gtocore:data_form_testing_me_interface"))
        .prerequisites(ComputationArray)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(128 * 20 * 600L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:data_module"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val VoidMiner = MainTree.builder("void_miner", "虚空矿脉采掘技术", "Void Vein Mining Technology")
        .description("从一无所有的虚空中定向寻找并采掘出各种矿脉", "From the void of nothingness, directionally search for and mine various veins")
        .icon(RegistriesUtils.getItem("gtocore:void_miner"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:void_miner"), 0.8F)
                .addMaterialNeeded(MATERIAL, 256)
                .addMaterialNeeded(MECHANICS, 8)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val WetwareTech = MainTree.builder("wetware_tech", "湿件技术", "Wetware Technology")
        .description("把湿件着你的几团肉拼在一起，组合它们的湿件能力的技术", "The technology of putting your wetware together and combining their wetware capabilities")
        .icon(RegistriesUtils.getItem("gtceu:wetware_processor_mainframe"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 120L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:wetware_processor_assembly"), 0.8F)
                .addMaterialNeeded(BIOLOGY, 25)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val ParticleAccelerators = MainTree.builder("particle_accelerator", "高能粒子实验", "High-Energy Particle Experiments")
        .description("利用粒子加速器进行高能物理实验，探索微观世界的奥秘", "Use particle accelerators for high-energy physics experiments, exploring the mysteries of the microscopic world")
        .icon(RegistriesUtils.getItem("gtocore:alpha_particle_particle_source"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 960L)
                .addMaterialNeeded(MATERIAL, 768)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:accelerated_pipeline"), 0.8F)
                .build(),
        )
        .tier(2)
        .prerequisites(NuclearPhysics)
        .build()

    @JvmField
    val BiowareTech = MainTree.builder("bioware_tech", "生物件技术", "Bioware Technology")
        .description("这年头，蘑菇也会算数了", "These days, even mushrooms can do math")
        .icon(RegistriesUtils.getItem("gtocore:bioware_mainframe"))
        .prerequisites(WetwareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 2400L)
                .addMaterialNeeded(BIOLOGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bioware_chip"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val BiowareDataStorage = MainTree.builder("bioware_data_storage", "生物件数据存储", "Bioware Data Storage")
        .description("利用生物件的自我复制能力，实现数据的高密度存储与快速访问", "Utilize the self-replication ability of bioware to achieve high-density data storage and fast access")
        .icon(RegistriesUtils.getItem("gtocore:bio_data_access_hatch"))
        .prerequisites(BiowareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 2400L)
                .addMaterialNeeded(BIOLOGY, 128)
                .addMaterialNeeded(DATA_STORAGE, 576)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:advanced_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val SupercomputingTech = MainTree.builder("super_computing_tech", "超算集群", "Supercomputing Cluster")
        .description("掌握超级计算机的设计与制造技术，供应更强大的算力与数据处理能力", "Master the design and manufacturing technology of supercomputers, providing more powerful computing power and data processing capabilities")
        .icon(RegistriesUtils.getItem("gtocore:supercomputing_center"))
        .prerequisites(ComputationArray)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 2400L)
                .addMaterialNeeded(DATA_STORAGE, 128)
                .addMaterialNeeded(COMPUTATION, 1024)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:hpca_active_cooler_component"), 0.7F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val ExcitationCrystalLaser = MainTree.builder("excitation_crystal_laser", "激发晶体光束研究", "Excitation Crystal Beam Research")
        .description("使用最近发现的光透域材料，创新性与纳米蜂群技术结合制造的奇异晶体，能够将激光的能量集中在极小的空间内，产生全新的高强度的光束形态激光", "Using the recently discovered light-transmissive material, combined with nanobee technology to create a strange crystal, capable of concentrating the energy of the laser in an extremely small space, producing a new high-intensity beam form of laser")
        .icon(RegistriesUtils.getItem("gtocore:excitation_crystal"))
        .prerequisites(LaserFoundations)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(OPTICS, 2)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:non_linear_optical_lens"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserEngraver = MainTree.builder("laser_engraver", "极细尺度激光导向刻蚀", "Ultra-Fine Scale Laser Guided Etching")
        .description("利用激光的极细尺度，进行导向刻蚀，制造出高精度的微结构", "Use the ultra-fine scale of lasers for guided etching, creating high-precision microstructures")
        .icon(RegistriesUtils.getItem("gtocore:non_linear_optical_lens"))
        .prerequisites(LaserFoundations)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .addMaterialNeeded(OPTICS, 2)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:high_frequency_laser"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val ScannerInnovation = MainTree.builder("scanner_innovation", "扫描仪革新", "Scanner Innovation")
        .description("将扫描仪接入先进的AE物质存储系统，想扫什么就扫什么，想扫多大就扫多大", "Connect the scanner to the advanced AE matter storage system, scan whatever you want, and scan as big as you want")
        .icon(RegistriesUtils.getItem("gtocore:intelligent_scanning_management_platform"))
        .prerequisites(ScanStation)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 14400L)
                .addMaterialNeeded(OPTICS, 384)
                .addMaterialNeeded(DATA_STORAGE, 1024)
                .addMaterialNeeded(COMPUTATION, 4096)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:uhv_world_data_scanner"), 0.75F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val BiowareSupercomputing = MainTree.builder("bioware_supercomputing", "生物件超算", "Bioware Supercomputing")
        .description("利用生物组织的复杂性和高效性，构建出能够进行复杂且高通用性计算的生物计算机", "Utilize the complexity and efficiency of biological tissues to construct a biocomputer capable of performing complex and highly general-purpose computations")
        .icon(RegistriesUtils.getItem("gtocore:biocomputer_casing"))
        .prerequisites(BiowareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 14400L)
                .addMaterialNeeded(BIOLOGY, 384)
                .addMaterialNeeded(COMPUTATION, 2048)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:biocomputer_casing"), 0.9F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val DataCenterOverclocking = MainTree.builder("data_center_overclocking", "数据中心超频", "Data Center Overclocking")
        .description("用更高的算力来推动数据中心的研究进度，实现更快的数据处理和科研成果的产出", "Use higher computing power to accelerate the research progress of the data center, achieving faster data processing and output of scientific research results")
        .icon(ExResearchMachines.DATA_CENTER.asStack())
        .prerequisites(BiowareSupercomputing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 14400L)
                .addMaterialNeeded(COMPUTATION, 2048)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:highly_concurrent_intensive_optical_computing_channel"), 0.9F)
                .build(),
        )
        .tier(3)
        .build()
        .addRewardDescription("数据中心每使用3倍于最大算力的算力处理节点时，速度提升至2倍", "When the data center uses 3 times the maximum computing power of the computing node, the speed is increased to 2 times")

    @JvmField
    val BedrockMining = MainTree.builder("bedrock_production", "基岩开采与加工", "Bedrock Mining and Processing")
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
    val MatterFabricator = MainTree.builder("matter_fabricator", "物质制造机", "Matter Fabricator")
        .description("通过高能物理实验，将能量直接转化为物质，实现物质的直接制造", "Through high-energy physics experiments, directly convert energy into matter, achieving direct matter fabrication")
        .icon(RegistriesUtils.getItem("gtocore:matter_fabricator"))
        .prerequisites(ParticleAccelerators)
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
    val OpticalTech = MainTree.builder("optical_tech", "光学计算技术", "Optical Computing Technology")
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
    val StellarForge = MainTree.builder("stellar_forge", "恒星锻造材料", "Stellar Forge Materials")
        .description("将恒星级别的能量用于物质加工，制造出超高性能的材料。祈祷它别爆炸吧", "Use stellar-level energy for material processing, creating ultra-high-performance materials. Pray it doesn't explode")
        .icon(RegistriesUtils.getItem("gtocore:stellar_forge"))
        .prerequisites(MolecularSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 1800)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:stellar_containment_casing"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val BlockholeDataStorage = MainTree.builder("blockhole_data_storage", "黑洞数据存储技术", "Black Hole Data Storage Technology")
        .description("利用黑洞的极端引力场，将数据压缩存储在黑洞中，实现超大规模的数据存储与管理", "Use the extreme gravitational field of black holes to compress and store data in black holes, achieving ultra-large-scale data storage and management")
        .icon(RegistriesUtils.getItem("gtocore:black_hole_data_access_hatch"))
        .prerequisites(BiowareDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(DATA_STORAGE, 3072)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bio_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val LaserEngraver2 = MainTree.builder("laser_engraver2", "维度聚焦激光蚀刻技术", "Dimensional Focusing Laser Etching Technology")
        .description("从不同维度给它打光，让它在不同维度的光线下进行蚀刻，制造出更高精度的微结构", "Illuminate it from different dimensions, allowing it to etch under light from different dimensions, creating higher precision microstructures")
        .icon(RegistriesUtils.getItem("gtocore:dimensional_focus_engraving_array"))
        .prerequisites(LaserEngraver)
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
    val SPSTech = MainTree.builder("sps_tech", "超临界移相技术", "Supercritical Phase Shift Technology")
        .description("偷偷告诉你实际上它的工作原理是哭泣黑曜石在一边嘬超然物质一边看煽情片", "I'll tell you a secret, its working principle is actually crying obsidian sucking transcending matter on one side while watching a tear-jerking movie on the other side")
        .icon(RegistriesUtils.getFluid("gtocore:transcending_matter"))
        .prerequisites(TokamakFusionReactor4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 2560)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:pellet_antimatter"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val AtomicEnergyExciting = MainTree.builder("atomic_energy_exciting", "原子能激发技术", "Atomic Energy Excitation Technology")
        .description("通过激发原子核的能量，充分压榨原子能的潜力，生产出更高能量密度的燃料", "By exciting the energy of atomic nuclei, fully exploiting the potential of atomic energy, producing fuel with higher energy density")
        .icon(RegistriesUtils.getItem("gtocore:atomic_energy_excitation_plant"))
        .prerequisites(SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:large_naquadah_reactor"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val PlanetCoreExtraction = MainTree.builder("planet_core_extraction", "行星核心提取技术", "Planet Core Extraction Technology")
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
    val CryotheumSupercoductingTech = MainTree.builder("cryotheum_superconducting_tech", "凛冰超导技术", "Cryotheum Superconducting Technology")
        .description("使用凛冰循环浸淋超导材料，进一步提升超导导体性能的稳定性", "Use cryotheum circulation to immerse superconducting materials, further improving the stability of superconducting performance")
        .icon(GTOFluids.GELID_CRYOTHEUM.get())
        .prerequisites(SuperConductingMaterialResearch, SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 3600L)
                .addMaterialNeeded(MATERIAL, 2800)
                .setEurekaFluid(GTOFluids.GELID_CRYOTHEUM.get(), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val GWCAComputingTech = MainTree.builder("gwca_computing_tech", "GWCA计算技术", "GWCA Computing Technology")
        .description("通过操纵引力波的传播与干涉，实现对信息的传输与处理，能够实现超越传统计算机的运算能力", "By manipulating the propagation and interference of gravitational waves, achieve information transmission and processing, capable of achieving computational power beyond traditional computers")
        .icon(RegistriesUtils.getItem("gtocore:gwca_empty_component"))
        .prerequisites(SupercomputingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 3300)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:graviton_computer_casing"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val MassEnergyConversionTech = MainTree.builder("mass_energy_conversion_tech", "质能转换技术", "Mass-Energy Conversion Technology")
        .description("掌握质能转换的核心技术，实现物质与能量的(不太高效)的互换", "Master the core technology of mass-energy conversion, achieving (not very efficient) interchange between matter and energy")
        .icon(RegistriesUtils.getItem("gtocore:mass_fabricator"))
        .prerequisites(SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 3300)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:matter_fabricator"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val EnergyInjectedFissionTech = MainTree.builder("energy_injected_fission_tech", "能量注入裂变技术", "Energy-Injected Fission Technology")
        .description("通过向裂变反应堆注入高能粒子，提升裂变反应的效率与能量输出", "By injecting high-energy particles into the fission reactor, improve the efficiency and energy output of the fission reaction")
        .icon(RegistriesUtils.getItem("gtocore:entropy_flux_engine"))
        .prerequisites(ParticleAccelerators)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 3300)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:graviton_field_constraint_casing"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val TimeDilationTech = MainTree.builder("time_dilation_tech", "时间膨胀技术", "Time Dilation Technology")
        .description("利用相对论效应，控制时间流速，能够在现实时间尺度上完成时间条件苛刻的实验", "Use relativistic effects to control the flow of time, allowing experiments with stringent time conditions to be completed on a real-time scale")
        .icon(RegistriesUtils.getItem("gtocore:temporal_matter"))
        .prerequisites(SPSTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 3300)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:empty_laser_cooling_container"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val ExoticTech = MainTree.builder("exotic_technology", "奇异处理器技术", "Exotic Processor Technology")
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
    val VirtualUniverseDataStorage = MainTree.builder("virtual_universe_data_storage", "虚拟宇宙数据存储技术", "Virtual Universe Data Storage Technology")
        .description("通过模拟一个完整的虚拟宇宙，将数据存储在其中，实现超大规模的数据存储与管理", "By simulating a complete virtual universe, data is stored within it, achieving ultra-large-scale data storage and management")
        .icon(RegistriesUtils.getItem("gtocore:virtual_universe_data_access_hatch"))
        .prerequisites(BlockholeDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(DATA_STORAGE, 16384)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:black_hole_data_access_hatch"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val LeptonicCharge = MainTree.builder("leptonic_charge", "轻子爆弹", "Leptonic Charge")
        .description("威力极其强大的爆弹，几乎是万亿亿级的TNT当量", "An extremely powerful explosive, almost equivalent to a trillion trillion TNT")
        .icon(RegistriesUtils.getItem("gtocore:leptonic_charge"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 4000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:naquadria_charge"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val CosmicTech = MainTree.builder("cosmic_technology", "寰宇处理器技术", "Cosmic Processor Technology")
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
    val ExDurablePlasmaContainer = MainTree.builder("ex_durable_plasma_container", "高耐久等离子体容器", "Ex-Durable Plasma Container")
        .description("用于存储高能等离子体的容器，能够承受极端的温度和压力", "A container for storing high-energy plasma, capable of withstanding extreme temperatures and pressures")
        .icon(RegistriesUtils.getItem("gtocore:extremely_durable_plasma_cell"))
        .prerequisites(LaserPlasmaCondenser)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(MATERIAL, 5400)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:plasma_containment_cell"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val QuantumChromodynamicCharge = MainTree.builder("quantum_chromodynamic_charge", "量子色动力学爆弹", "Quantum Chromodynamic Charge")
        .description("别把它点了...至少别在你面前点了它", "Don't light it... at least don't light it in front of you")
        .icon(RegistriesUtils.getItem("gtocore:quantum_chromodynamic_charge"))
        .prerequisites(LeptonicCharge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 5400)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:leptonic_charge"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val ManifoldOscillatory = MainTree.builder("manifold_oscillatory", "多维流形振荡技术", "Manifold Oscillatory Technology")
        .description("通过多维流形的振荡，实现对时空的微观调控，能够在实验室中模拟宇宙级别的物理现象", "Achieve microscopic control of spacetime through oscillations of multi-dimensional manifolds, capable of simulating cosmic-level physical phenomena in the laboratory")
        .icon(RegistriesUtils.getItem("gtocore:manifold_oscillatory_power_cell"))
        .prerequisites(ExoticTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 5400)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:exotic_processing_core"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val AdvancedMassFabricationTech = MainTree.builder("advanced_mass_fabrication_tech", "进阶质能制造技术", "Advanced Mass Fabrication Technology")
        .description("比最初研究的那版质能制造技术省电", "More energy-efficient than the original mass fabrication technology")
        .icon(RegistriesUtils.getItem("gtocore:advanced_mass_fabricator"))
        .prerequisites(MassEnergyConversionTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 6400)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:mass_fabricator"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val ElementFabricationTech = MainTree.builder("element_fabrication_tech", "元素制造技术", "Element Fabrication Technology")
        .description("操纵物质的基本构成，实现对元素的直接制造与转换", "Manipulate the fundamental composition of matter, achieving direct fabrication and conversion of elements")
        .icon(RegistriesUtils.getItem("gtocore:element_copying"))
        .prerequisites(MassEnergyConversionTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 6400)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:star_ultimate_material_forge_factory"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val SupracausalTech = MainTree.builder("supracausal_tech", "超因果技术", "Supracausal Technology")
        .description("掌握超越因果律的技术，能够在问题提出之前就得到答案，实现对未来的预测与控制", "Master technology that transcends causality, allowing answers to be obtained before questions are even asked, achieving prediction and control of the future")
        .icon(RegistriesUtils.getItem("gtocore:supracausal_processing_core"))
        .prerequisites(CosmicTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 7200L)
                .addMaterialNeeded(SUPRACAUSAL, 1)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:relativistic_spinorial_memory_system"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val StellarUltimateForge = MainTree.builder("stellar_ultimate_forge", "恒星终极锻造技术", "Stellar Ultimate Forge Technology")
        .description("制造出只在恒星中心才能存在的材料的技术，能够制造出超越已知物理极限的材料", "Technology that creates materials that can only exist at the center of stars, capable of producing materials that surpass known physical limits")
        .icon(RegistriesUtils.getItem("gtocore:proto_matter"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 7200L)
                .addMaterialNeeded(MATERIAL, 8000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:ultimate_stellar_containment_casing"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val HyperDimensionalForge = MainTree.builder("hyper_dimensional_forge", "超维锻造技术", "Hyper-Dimensional Forge Technology")
        .description("通过操纵高维空间的物理规律，实现对物质的超维度锻造，制造出超越三维空间极限的材料", "By manipulating the physical laws of higher-dimensional space, achieve hyper-dimensional forging of matter, producing materials that surpass the limits of three-dimensional space")
        .icon(RegistriesUtils.getItem("gtocore:hyperdimensional_plasma_fusion_core"))
        .prerequisites(StellarForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 8000L)
                .addMaterialNeeded(MATERIAL, 8000)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:stellar_forge"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val DragonCore = MainTree.builder("dragon_core", "龙之能量核心", "Dragon Energy Core")
        .description("通过操纵龙之能量的流动，实现对能量的极致掌控", "By manipulating the flow of dragon energy, achieve ultimate control over energy")
        .icon(RegistriesUtils.getItem("gtocore:wyvern_core"))
        .prerequisites(SupracausalTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 28800L)
                .addMaterialNeeded(EXOTIC, 4)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:draconic_core"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val HyperDimensionalForgeCoil = MainTree.builder("hyper_dimensional_forge_coil", "超维锻造线圈改良", "Hyper-Dimensional Forge Coil Improvement")
        .description("用于超维锻造的线圈，汇聚来自高维空间的热量", "A coil used for hyper-dimensional forging, gathering heat from higher-dimensional space")
        .icon(RegistriesUtils.getItem("gtocore:infinity_coil_block"))
        .prerequisites(HyperDimensionalForge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 10800L)
                .addMaterialNeeded(MECHANICS, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:starmetal_coil_block"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val UniverseSimulation = MainTree.builder("universe_simulation", "宇宙创造与湮灭技术", "Universe Creation And Annihilation Technology")
        .description("宇宙冷漠，这张牌我是非常了解的", "The universe is indifferent, and I am very familiar with this card")
        .icon(RegistriesUtils.getItem("gtocore:eye_of_harmony"))
        .prerequisites(VirtualUniverseDataStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 28800L)
                .addMaterialNeeded(DATA_STORAGE, 65536)
                .addMaterialNeeded(ENERGY, 65536)
                .addMaterialNeeded(BIOLOGY, 16536)
                .addMaterialNeeded(OPTICS, 6536)
                .addMaterialNeeded(QUANTUM, 100)
                .addMaterialNeeded(EXOTIC, 40)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:recursively_folded_negative_space"), 0.7F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val AwakenedCore = MainTree.builder("awakened_core", "觉醒核心", "Awakened Core")
        .description("觉醒你内在的神龙之力", "Awaken your inner dragon power")
        .icon(RegistriesUtils.getItem("gtocore:awakened_core"))
        .prerequisites(DragonCore)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 1)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:wyvern_core"), 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val ChaosCore = MainTree.builder("chaos_core", "混沌核心", "Chaos Core")
        .description("§k混沌混沌混沌混沌混沌§r", "§kChaos C haos Ch aosCha osChaos§r")
        .icon(RegistriesUtils.getItem("gtocore:chaotic_core"))
        .prerequisites(AwakenedCore)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 2)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:awakened_core"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val SuprachronalDrone = MainTree.builder("suprachronal_drone", "超时空无人机", "Suprachronal Drone")
        .description("在维度之外工作，随意的穿梭于不同的时空中", "Working outside of dimensions, freely shuttling through different space-times")
        .icon(RegistriesUtils.getItem("gtocore:hyperdimensional_drone"))
        .prerequisites(SuprachronalAssemblyLine)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 16)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:chaotic_core"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val SuprachronalCircuits = MainTree.builder("suprachronal_circuits", "超时空电路", "Suprachronal Circuits")
        .description("随意的提供任意级别的计算能力", "Freely provide any level of computing power")
        .icon(RegistriesUtils.getItem("gtocore:suprachronal_circuit_max"))
        .prerequisites(SuprachronalDrone)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 28800L)
                .addMaterialNeeded(SUPRACAUSAL, 16)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:supracausal_mainframe"), 0.8F)
                .build(),
        )
        .tier(7)
        .build()

    @JvmField
    val Create = MainTree.builder("create", "创造", "Create")
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
            additionalLines.add(
                Component.translatable(OTHER_REWARD_LABEL).withStyle(
                    ChatFormatting.DARK_PURPLE,
                ).append(Component.translatable("gtocore.data.${this.name}").withStyle(ChatFormatting.GRAY)),
            )
        }
        return this
    }
}
