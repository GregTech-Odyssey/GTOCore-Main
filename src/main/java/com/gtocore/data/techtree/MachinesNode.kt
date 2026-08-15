package com.gtocore.data.techtree

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.ASSEMBLY
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.CATALYSIS
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.api.research.ResearchTag.THERMODYNAMICS
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOMaterials
import com.gtocore.common.data.machines.MultiBlockA.CHEMICAL_PLANT
import com.gtocore.common.data.machines.MultiBlockC
import com.gtocore.common.data.machines.SpaceMultiblock
import com.gtocore.data.techtree.BaseNodes.LaserFoundations
import com.gtocore.data.techtree.BaseNodes.MachinesTree
import com.gtocore.data.techtree.BaseNodes.VoidMiner
import com.gtocore.data.techtree.BaseNodes.addRewardDescription
import com.gtocore.data.techtree.ComponentNodes.ComponentInAssemblyLineluv
import com.gtocore.data.techtree.ComponentNodes.ComponentInAssemblyLineuhv

import com.gregtechceu.gtceu.api.GTValues.UHV
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.LARGE_CHEMICAL_REACTOR
import com.gtolib.utils.RegistriesUtils

object MachinesNode : AutoInitialize<MachinesNode>() {

    // ======= 外壳 =======
    @JvmField
    val IridiumCasingProduction = MachinesTree.builder("iridium_casing_production", "高性能机器外壳", "High-Performance Machine Casing")
        .description("生产铱强化机械方块的外壳，这种外壳有着强大的耐久性和抗辐射能力", "Produce the casing for iridium-reinforced machine blocks, which has strong durability and radiation resistance")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(15).setEurekaItem(TagPrefix.block, GTMaterials.Osmiridium, 1.0f).build())
        .icon(GTOBlocks.IRIDIUM_CASING)
        .build()

    @JvmField
    val MolecularSeriesCasings = MachinesTree.builder("molecular_series_casings", "分子级系列外壳", "Molecular Series Casings")
        .description("一种看上去流淌着恐怖级能量的外壳，能够承受极端的能量流动", "A casing that appears to flow with terrifying levels of energy, capable of withstanding extreme energy flows")
        .icon(RegistriesUtils.getItem("gtocore:molecular_casing"))
        .prerequisites(IridiumCasingProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(THERMODYNAMICS, 256)
                .addMaterialNeeded(MECHANICS, 32)
                .addMaterialNeeded(ASSEMBLY, 192)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:molecular_casing"), 0.7F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val DimensionSeriesCasings = MachinesTree.builder("dimension_series_casings", "维度级系列外壳", "Dimension Series Casings")
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
    val DysonSphereSeriesCasing = MachinesTree.builder("dyson_sphere_series_casing", "戴森球系列外壳", "Dyson Sphere Series Casing")
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
    val QFTSeriesCasing = MachinesTree.builder("qft_series_casing", "量子场论级系列外壳", "Quantum Field Theory Series Casings")
        .description("能够可控扭曲时空的外壳，适用于相关机器的建造", "A casing capable of controllably warping spacetime, suitable for the construction of related machines")
        .icon(RegistriesUtils.getItem("gtocore:spacetime_bending_core"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 256)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:manipulator"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val TimeDilationDimensionSeriesCasing = MachinesTree.builder("time_dilation_dimension_series_casing", "时间膨胀维度级系列外壳", "Time Dilation Dimension Series Casings")
        .description("能够在不同时间流速下稳定运作的外壳，适用于相关机器的建造", "A casing capable of stable operation under different time flow rates, suitable for the construction of related machines")
        .icon(RegistriesUtils.getItem("gtocore:dimensional_stability_casing"))
        .prerequisites(DimensionSeriesCasings)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 256)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:dimensional_bridge_casing"), 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val ChemicalPlantEnvironmentControl = MachinesTree.builder("chemical_plant_environment_control", "化工厂环境控制", "Chemical Plant Environment Control")
        .description("掌握化工厂的环境控制技术，实现更大规模的化学产品生产与更高效的资源利用", "Master the environmental control technology of chemical plants, achieving larger-scale chemical product production and more efficient resource utilization")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 40L).setEurekaItem(LARGE_CHEMICAL_REACTOR, 1.0f).build())
        .icon(CHEMICAL_PLANT)
        .build()

    @JvmField
    val SelfMaintenanceSystem = MachinesTree.builder("self_maintenance_system", "自维护系统", "Self-Maintenance System")
        .description("开发自维护系统，实现设备的自动检测与修复，减少人工干预", "Develop a self-maintenance system to achieve automatic detection and repair of equipment, reducing manual intervention")
        .icon(RegistriesUtils.getItem("ad_astra:wrench"))
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 180L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:auto_maintenance_hatch"), 1.0F)
                .addMaterialNeeded(MECHANICS, 4)
                .build(),
        )
        .build()

    @JvmField
    val PreciseManufacturingTech = MachinesTree.builder("precise_manufacturing_tech", "精密制造技术", "Precision Manufacturing Technology")
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
    val IsaMillingMachine = MachinesTree.builder("isa_milling_machine", "艾萨研磨处理技术", "Isa Ore Processing Technology")
        .description("掌握艾萨研磨处理矿物这种滚珠暴力碾磨一切再拿泔水泡的技术", "Master the Isa milling process for minerals, a technology that violently grinds everything with ball bearings and soaks it in swill")
        .prerequisites(ComponentInAssemblyLineluv)
        .requirements(
            ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 60L)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:iv_macerator"), 0.8f)
                .addMaterialNeeded(MATERIAL, 256)
                .build(),
        )
        .icon(RegistriesUtils.getItem("gtocore:milled_nickel"))
        .tier(1)
        .build()

    @JvmField
    val FuelRefineryComplex = MachinesTree.builder("fuel_refinery_complex", "燃料精炼综合管理", "Fuel Refinery Complex")
        .description("将能烧的东西处理成更能烧的东西的技术", "The technology of processing burnable things into more burnable things")
        .icon(RegistriesUtils.getItem("gtocore:fuel_refining_complex"))
        .prerequisites(ChemicalPlantEnvironmentControl)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:large_cracker"), 0.8F)
                .addMaterialNeeded(MATERIAL, 1024)
                .addMaterialNeeded(ENERGY, 64)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val AdvancedAssemblyLineMachine = MachinesTree.builder("advanced_assembly_line_machine", "进阶装配线", "Advanced Assembly Line")
        .description("集成GTO公司机械组搭的框架，电子组布置的线路和物流组搞的配送，打造出一条虽然很费电但高通量的装配线", "Integrating the framework built by GTO's mechanical team, the circuits laid out by the electronics team, and the logistics team's distribution, creating an assembly line that is very power-hungry but high-throughput")
        .icon(RegistriesUtils.getItem("gtocore:advanced_assembly_line_unit"))
        .prerequisites(PreciseManufacturingTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(MECHANICS, 32)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:assembly_line"), 0.9F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserBatchProduction = MachinesTree.builder("laser_batch_production_proto", "激光能源革命", "Laser Energy Revolution")
        .description("使用高功率的激光束传输能量，彻底翻新你的各种生产机器！", "Use high-power laser beams to transmit energy, completely revamping your various production machines!")
        .icon(RegistriesUtils.getItem("gtocore:energy_control_module_mk2"))
        .prerequisites(LaserFoundations, IridiumCasingProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 32)
                .addMaterialNeeded(ASSEMBLY, 192)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:energy_control_module_mk2"), 0.6F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserHiTempThermalProcessing = MachinesTree.builder("laser_high_temp_thermal_processing", "激光高温热处理", "Laser High-Temperature Thermal Processing")
        .description("激光能源在加热器中的应用，提供极高的瞬间加热能力，适用于高温工艺", "Application of laser energy in heaters, providing extremely high instantaneous heating capability, suitable for high-temperature processes")
        .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .addMaterialNeeded(THERMODYNAMICS, 640)
                .setEurekaItem(GTMultiMachines.ELECTRIC_BLAST_FURNACE, 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserSortingPackaging = MachinesTree.builder("laser_packager", "激光分选与包装", "Laser Sorting and Packaging")
        .description("激光能源在包装器中的应用，能够快速分选与包装物品，适用于大规模生产线", "Application of laser energy in packagers, capable of quickly sorting and packaging items, suitable for large-scale production lines")
        .icon(GTMachines.PACKER[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .addMaterialNeeded(ASSEMBLY, 640)
                .setEurekaItem(GTMachines.PACKER[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserConditionControlling = MachinesTree.builder("laser_reaction_adjuster", "激光反应条件控制", "Laser Reaction Condition Control")
        .description("激光能源在化学反应器中的应用，能够精确控制反应条件，提高产率与效率", "Application of laser energy in chemical reactors, capable of precisely controlling reaction conditions, improving yield and efficiency")
        .icon(LARGE_CHEMICAL_REACTOR)
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .addMaterialNeeded(CATALYSIS, 640)
                .setEurekaItem(LARGE_CHEMICAL_REACTOR, 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserCuttingLathing = MachinesTree.builder("laser_cutting_lathing", "激光切割与车削", "Laser Cutting and Lathing")
        .description("激光能源在切割机与车床中的应用，利用激光加工的细粒度与高精度，实现复杂零件的快速加工", "Application of laser energy in cutting machines and lathes, utilizing the fine granularity and high precision of laser processing to achieve rapid machining of complex parts")
        .icon(GTMachines.LATHE[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .setEurekaItem(GTMachines.LATHE[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserRollingPressing = MachinesTree.builder("laser_rolling_pressing", "激光轧制与压延", "Laser Rolling and Pressing")
        .description("激光能源在轧机与压延机中的应用，驱动高动量的锻锤与压辊，对材料进行高效的轧制与压延加工", "Application of laser energy in rolling mills and calenders, driving high-momentum forging hammers and rollers for efficient rolling and pressing of materials")
        .icon(GTMachines.LATHE[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .setEurekaItem(GTMachines.LATHE[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserCrushingRotating = MachinesTree.builder("laser_crushing_rotating", "激光破碎与旋转加工", "Laser Crushing and Rotating Processing")
        .description("激光能源在破碎机与旋转加工设备中的应用，驱动锯片、研磨头、离心轮等高速旋转部件，实现高效的破碎与旋转加工", "Application of laser energy in crushers and rotating processing equipment, driving high-speed rotating components such as rotary cutters, grinding discs, and centrifugal wheels for efficient crushing and rotating processing")
        .icon(GTMachines.CENTRIFUGE[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .setEurekaItem(GTMachines.CENTRIFUGE[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserElectroMagneticProcessing = MachinesTree.builder("laser_electromagnetic_processing", "激光电磁加工", "Laser Electromagnetic Processing")
        .description("激光能源在电磁加工设备中的应用，利用高能光子与电磁场的协同作用，实现对材料的高效加工与改性", "Application of laser energy in electromagnetic processing equipment, utilizing the synergistic effect of high-energy photons and electromagnetic fields for efficient material processing and modification")
        .icon(GTMachines.POLARIZER[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(384 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .setEurekaItem(GTMachines.POLARIZER[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserAssemblyProcessing = MachinesTree.builder("laser_assembly_processing", "激光装配加工", "Laser Assembly Processing")
        .description("激光能源在装配加工设备中的应用，在装配过程中提供高精度的定位与焊接能力，实现复杂组件的高效组装", "Application of laser energy in assembly processing equipment, providing high-precision positioning and welding capabilities during assembly, achieving efficient assembly of complex components")
        .icon(GTMachines.ASSEMBLER[UHV])
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(MECHANICS, 108)
                .setEurekaItem(GTMachines.ASSEMBLER[UHV], 0.75F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val LaserBioEngineering = MachinesTree.builder("high_energy_bio_engineering", "激光生物工程", "Laser Bioengineering")
        .description("融合激光能源的高能效与生物工程生产的严谨性，是生物技术的巅峰之作", "Combining the high energy efficiency of laser energy with the rigor of bioengineering production, it is the pinnacle of biotechnology")
        .icon(RegistriesUtils.getItem("gtocore:microorganism_master"))
        .prerequisites(LaserConditionControlling)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 3600L)
                .addMaterialNeeded(MECHANICS, 108)
                .addMaterialNeeded(BIOLOGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:law_filter_casing"), 0.75F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val LaserSpaceEngineering = MachinesTree.builder("laser_space_engineering", "激光太空工程", "Laser Space Engineering")
        .description("将激光能源的高效性带上太空！", "Bringing the efficiency of laser energy into space!")
        .icon(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE.asItem())
        .prerequisites(LaserConditionControlling)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 3600L)
                .addMaterialNeeded(MECHANICS, 108)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 2048)
                .setEurekaItem(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE.asItem(), 0.75F)
                .build(),
        )
        .tier(3)
        .build()
        .addRewardDescription("解锁空间站激光仓、高级仓室的使用", "Unlock the use of laser bays and advanced chambers in space stations")

    @JvmField
    val ComponentProductionEnhancement = MachinesTree.builder("component_production_enhancement", "组件生产强化", "Component Production Enhancement")
        .description("通过优化生产线和改进组件设计，实现大批量组件的高效节省生产", "Achieve efficient and cost-effective production of large quantities of components through optimized production lines and improved component design")
        .icon(RegistriesUtils.getItem("gtocore:component_assembly_line"))
        .prerequisites(LaserAssemblyProcessing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 9900L)
                .addMaterialNeeded(MECHANICS, 144)
                .addMaterialNeeded(ASSEMBLY, 384)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:component_assembler"), 0.75F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val NeutronActivatorSelfAdoption = MachinesTree.builder("neutron_activator_self_adoption", "中子活化自适应技术", "Neutron Activator Self-Adoption Technology")
        .description("自动检测并获取中子活化耦合能，从而调整中子活化设备的运行参数，实现高效的中子活化处理", "Automatically detect and acquire neutron activation coupling energy, thereby adjusting the operating parameters of neutron activation equipment for efficient neutron activation processing")
        .icon(MultiBlockC.NEUTRON_VORTEX.asItem())
        .prerequisites(LaserBatchProduction)
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
    val LaserPlasmaCondenser = MachinesTree.builder("laser_plasma_condenser", "激光等离子体冷凝器", "Laser Plasma Condenser")
        .description("俺寻思热的东西不是因为它的热运动很强吗？那就用激光把它的热运动给定住不就好了", "I think the hot thing is that its thermal motion is very strong, right? Then just use a laser to fix its thermal motion, isn't it?")
        .icon(RegistriesUtils.getItem("gtocore:plasma_condenser"))
        .prerequisites(LaserBatchProduction)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(THERMODYNAMICS, 1280)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:laser_cooling_casing"), 0.75F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val ComplexPlasmaCondenser = MachinesTree.builder("compound_extreme_plasma_condenser", "复杂激光等离子体冷凝器", "Complex Laser Plasma Condenser")
        .description("GTO寰宇重工集团里最大的冰箱", "The largest refrigerator in GTO Universal Heavy Industries Group")
        .icon(RegistriesUtils.getItem("gtocore:compound_extreme_cooling_unit"))
        .prerequisites(LaserPlasmaCondenser)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(THERMODYNAMICS, 2048)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:vacuum_freezer"), 0.75F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val RareEarthProcessing = MachinesTree.builder("rare_earth_processing", "稀土直接分离技术", "Rare Earth Direct Separation Technology")
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
    val MagnetoResonaticCircuitUpgrade = MachinesTree.builder("magneto_resonatic_circuit_upgrade", "磁共振电路升级", "Magneto Resonatic Circuit Upgrade")
        .description("改进磁共振电路的设计与制造，减少材料消耗并提升产率", "Improve the design and manufacturing of magneto resonatic circuits, reducing material consumption and increasing yield")
        .icon(RegistriesUtils.getItem("gtocore:magneto_resonatic_circuit_uhv"))
        .prerequisites(LaserAssemblyProcessing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 29900L)
                .addMaterialNeeded(ASSEMBLY, 512)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:magneto_resonatic_circuit_uhv"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()
        .addRewardDescription("所有磁共振电路的产量提升1", "Increase the yield of all magneto resonatic circuits by 1")

    @JvmField
    val SuprachronalAssemblyLine = MachinesTree.builder("suprachronal_assembly_line", "超时空装配线", "Suprachronal Assembly Line")
        .description("装配线已经是极限了？不，GTO寰宇重工的工程师们已经突破了时空的限制，让装配线在不同的时空中同时运作，实现了超时空的装配生产", "The assembly line has reached its limit? No, the engineers of GTO Universal Heavy Industries have broken the limits of space-time, allowing the assembly line to operate simultaneously in different space-times, achieving suprachronal assembly production")
        .icon(RegistriesUtils.getItem("gtocore:nyarlathoteps_tentacle"))
        .prerequisites(LaserAssemblyProcessing)
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
    val MagneticConfinementDimensionallyShockProcess = MachinesTree.builder("magnetic_confinement_dimensionally_shock_process", "磁约束维度冲击工艺", "Magnetic Confinement Dimensionally Shock Process")
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
    val QFTManipulator = MachinesTree.builder("qft_manipulator", "量子场论操纵器", "Quantum Field Theory Manipulator")
        .description("通过操纵量子场的波动，实现对物质与能量的精确控制", "By manipulating the fluctuations of quantum fields, achieve precise control over matter and energy")
        .icon(RegistriesUtils.getItem("gtocore:quantum_force_transformer"))
        .prerequisites(QFTSeriesCasing)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(2048 * 20 * 7200L)
                .addMaterialNeeded(MECHANICS, 256)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:quantum_force_transformer_coil"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()
}
