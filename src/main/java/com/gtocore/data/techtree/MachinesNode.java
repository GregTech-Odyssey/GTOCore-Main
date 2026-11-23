package com.gtocore.data.techtree;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gregtechceu.gtceu.api.GTValues.IV;
import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.LARGE_CHEMICAL_REACTOR;
import static com.gtocore.api.research.ResearchTag.ASSEMBLY;
import static com.gtocore.api.research.ResearchTag.BIOLOGY;
import static com.gtocore.api.research.ResearchTag.CATALYSIS;
import static com.gtocore.api.research.ResearchTag.ENERGY;
import static com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING;
import static com.gtocore.api.research.ResearchTag.MATERIAL;
import static com.gtocore.api.research.ResearchTag.MECHANICS;
import static com.gtocore.api.research.ResearchTag.THERMODYNAMICS;
import static com.gtocore.common.data.machines.MultiBlockA.CHEMICAL_PLANT;
import static com.gtocore.data.techtree.BaseNodes.eurekaRate1;
import static com.gtocore.data.techtree.BaseNodes.eurekaRate2;
import static com.gtocore.data.techtree.ComponentNodes.ComponentInAssemblyLineluv;

public final class MachinesNode {

    // ======= 外壳 =======
    public static final TechNode IridiumCasingProduction = BaseNodes.MachinesTree.builder("iridium_casing_production", "高性能机器外壳", "High-Performance Machine Casing")
            .description("生产铱强化机械方块的外壳，这种外壳有着强大的耐久性和抗辐射能力", "Produce the casing for iridium-reinforced machine blocks, which has strong durability and radiation resistance")
            .requirements(new ResearchRequirements.Builder().setCWUNeeded(15).setEureka(GTMaterials.Osmiridium, 1.0f).build())
            .icon(GTOBlocks.IRIDIUM_CASING)
            .build();

    public static final TechNode MolecularSeriesCasings = BaseNodes.MachinesTree.builder("molecular_series_casings", "分子级系列外壳", "Molecular Series Casings")
            .description("一种看上去流淌着恐怖级能量的外壳，能够承受极端的能量流动", "A casing that appears to flow with terrifying levels of energy, capable of withstanding extreme energy flows")
            .icon(GTOBlocks.MOLECULAR_CASING)
            .prerequisites(IridiumCasingProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(256 * 20 * 2400L)
                            .addMaterialNeeded(THERMODYNAMICS, 256)
                            .addMaterialNeeded(MECHANICS, 32)
                            .addMaterialNeeded(ASSEMBLY, 192)
                            .setEureka(GTOBlocks.MOLECULAR_CASING, eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode DimensionSeriesCasings = BaseNodes.MachinesTree.builder("dimension_series_casings", "维度级系列外壳", "Dimension Series Casings")
            .description("能够承载维度级别能量与力场的外壳，适用于极端环境下的设备保护与建造", "A casing capable of withstanding dimension-level energy and force fields, suitable for equipment protection and construction in extreme environments")
            .icon(GTOBlocks.DIMENSIONAL_BRIDGE_CASING)
            .prerequisites(MolecularSeriesCasings)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 4800L)
                            .addMaterialNeeded(MECHANICS, 64)
                            .setEureka(GTOBlocks.SPACETIME_ASSEMBLY_LINE_CASING, eurekaRate1)
                            .build())
            .tier(3)
            .build();

    public static final TechNode DysonSphereSeriesCasing = BaseNodes.MachinesTree.builder("dyson_sphere_series_casing", "戴森球系列外壳", "Dyson Sphere Series Casing")
            .description("能够长时间耐受恒星辐射的外壳，适用于戴森球的建造与维护", "A casing that can withstand stellar radiation for a long time, suitable for the construction and maintenance of Dyson spheres")
            .icon(GTOBlocks.DYSON_DEPLOYMENT_CORE)
            .prerequisites(DimensionSeriesCasings)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(1024 * 20 * 7200L)
                            .addMaterialNeeded(MECHANICS, 128)
                            .setEureka(GTOBlocks.DYSON_CONTROL_CASING, eurekaRate1)
                            .build())
            .tier(4)
            .build();

    public static final TechNode QFTSeriesCasing = BaseNodes.MachinesTree.builder("qft_series_casing", "量子场论级系列外壳", "Quantum Field Theory Series Casings")
            .description("能够可控扭曲时空的外壳，适用于相关机器的建造", "A casing capable of controllably warping spacetime, suitable for the construction of related machines")
            .icon(GTOBlocks.SPACETIME_BENDING_CORE)
            .prerequisites(DimensionSeriesCasings)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(2048 * 20 * 7200L)
                            .addMaterialNeeded(MECHANICS, 256)
                            .setEureka(GTOBlocks.MANIPULATOR, eurekaRate1)
                            .build())
            .tier(4)
            .build();

    public static final TechNode TimeDilationDimensionSeriesCasing = BaseNodes.MachinesTree.builder("time_dilation_dimension_series_casing", "时间膨胀维度级系列外壳", "Time Dilation Dimension Series Casings")
            .description("能够在不同时间流速下稳定运作的外壳，适用于相关机器的建造", "A casing capable of stable operation under different time flow rates, suitable for the construction of related machines")
            .icon(GTOBlocks.DIMENSIONAL_STABILITY_CASING)
            .prerequisites(DimensionSeriesCasings)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(2048 * 20 * 7200L)
                            .addMaterialNeeded(MECHANICS, 256)
                            .setEureka(GTOBlocks.DIMENSIONAL_BRIDGE_CASING, eurekaRate1)
                            .build())
            .tier(5)
            .build();

    public static final TechNode ChemicalPlantEnvironmentControl = BaseNodes.MachinesTree.builder("chemical_plant_environment_control", "化工厂环境控制", "Chemical Plant Environment Control")
            .description("掌握化工厂的环境控制技术，实现更大规模的化学产品生产与更高效的资源利用", "Master the environmental control technology of chemical plants, achieving larger-scale chemical product production and more efficient resource utilization")
            .requirements(new ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 40L).setEureka(LARGE_CHEMICAL_REACTOR, 1.0f).build())
            .icon(CHEMICAL_PLANT)
            .build();

    public static final TechNode SelfMaintenanceSystem = BaseNodes.MachinesTree.builder("self_maintenance_system", "自维护系统", "Self-Maintenance System")
            .description("开发自维护系统，实现设备的自动检测与修复，减少人工干预", "Develop a self-maintenance system to achieve automatic detection and repair of equipment, reducing manual intervention")
            .icon(ModItems.WRENCH.get())
            .prerequisites(ComponentInAssemblyLineluv)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(32 * 20 * 180L)
                            .setEureka(GTMachines.AUTO_MAINTENANCE_HATCH, 1.0F)
                            .addMaterialNeeded(MECHANICS, 4)
                            .build())
            .build();

    public static final TechNode PreciseManufacturingTech = BaseNodes.MachinesTree.builder("precise_manufacturing_tech", "精密制造技术", "Precision Manufacturing Technology")
            .description("掌握精密制造技术，实现高精度零件的生产与组装", "Master precision manufacturing technology to achieve the production and assembly of high-precision parts")
            .icon(MultiBlockA.PRECISION_ASSEMBLER)
            .prerequisites(ComponentInAssemblyLineluv)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(32 * 20 * 240L)
                            .setEureka(GCYMMachines.LARGE_ASSEMBLER, eurekaRate1)
                            .build())
            .tier(1)
            .build();

    public static final TechNode IsaMillingMachine = BaseNodes.MachinesTree.builder("isa_milling_machine", "艾萨研磨处理技术", "Isa Ore Processing Technology")
            .description("掌握艾萨研磨处理矿物这种滚珠暴力碾磨一切再拿泔水泡的技术", "Master the Isa milling process for minerals, a technology that violently grinds everything with ball bearings and soaks it in swill")
            .prerequisites(ComponentInAssemblyLineluv)
            .requirements(
                    new ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 60L)
                            .setEureka(GTMachines.MACERATOR[IV], eurekaRate1)
                            .addMaterialNeeded(MATERIAL, 256)
                            .build())
            .icon(GTMaterialItems.MATERIAL_ITEMS.get(GTOTagPrefix.MILLED, GTMaterials.Nickel))
            .tier(1)
            .build();

    public static final TechNode FuelRefineryComplex = BaseNodes.MachinesTree.builder("fuel_refinery_complex", "燃料精炼综合管理", "Fuel Refinery Complex")
            .description("将能烧的东西处理成更能烧的东西的技术", "The technology of processing burnable things into more burnable things")
            .icon(MultiBlockB.FUEL_REFINING_COMPLEX)
            .prerequisites(ChemicalPlantEnvironmentControl)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 240L)
                            .setEureka(MultiBlockA.LARGE_CRACKER, eurekaRate1)
                            .addMaterialNeeded(MATERIAL, 1024)
                            .addMaterialNeeded(ENERGY, 64)
                            .build())
            .tier(2)
            .build();

    public static final TechNode AdvancedAssemblyLineMachine = BaseNodes.MachinesTree.builder("advanced_assembly_line_machine", "进阶装配线", "Advanced Assembly Line")
            .description("集成GTO公司机械组搭的框架，电子组布置的线路和物流组搞的配送，打造出一条虽然很费电但高通量的装配线", "Integrating the framework built by GTO's mechanical team, the circuits laid out by the electronics team, and the logistics team's distribution, creating an assembly line that is very power-hungry but high-throughput")
            .icon(GTOBlocks.ADVANCED_ASSEMBLY_LINE_UNIT)
            .prerequisites(PreciseManufacturingTech)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(MECHANICS, 32)
                            .setEureka(GTMultiMachines.ASSEMBLY_LINE, eurekaRate1)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserBatchProduction = BaseNodes.MachinesTree.builder("laser_batch_production_proto", "激光能源革命", "Laser Energy Revolution")
            .description("使用高功率的激光束传输能量，彻底翻新你的各种生产机器！", "Use high-power laser beams to transmit energy, completely revamping your various production machines!")
            .icon(GTOItems.ENERGY_CONTROL_MODULE_MK2)
            .prerequisites(BaseNodes.LaserFoundations, IridiumCasingProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(256 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 32)
                            .addMaterialNeeded(ASSEMBLY, 192)
                            .setEureka(GTOItems.ENERGY_CONTROL_MODULE_MK2, eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserHiTempThermalProcessing = BaseNodes.MachinesTree.builder("laser_high_temp_thermal_processing", "激光高温热处理", "Laser High-Temperature Thermal Processing")
            .description("激光能源在加热器中的应用，提供极高的瞬间加热能力，适用于高温工艺", "Application of laser energy in heaters, providing extremely high instantaneous heating capability, suitable for high-temperature processes")
            .icon(GTMultiMachines.ELECTRIC_BLAST_FURNACE)
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .addMaterialNeeded(THERMODYNAMICS, 640)
                            .setEureka(GTMultiMachines.ELECTRIC_BLAST_FURNACE, eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserSortingPackaging = BaseNodes.MachinesTree.builder("laser_packager", "激光分选与包装", "Laser Sorting and Packaging")
            .description("激光能源在包装器中的应用，能够快速分选与包装物品，适用于大规模生产线", "Application of laser energy in packagers, capable of quickly sorting and packaging items, suitable for large-scale production lines")
            .icon(GTMachines.PACKER[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .addMaterialNeeded(ASSEMBLY, 640)
                            .setEureka(GTMachines.PACKER[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserConditionControlling = BaseNodes.MachinesTree.builder("laser_reaction_adjuster", "激光反应条件控制", "Laser Reaction Condition Control")
            .description("激光能源在化学反应器中的应用，能够精确控制反应条件，提高产率与效率", "Application of laser energy in chemical reactors, capable of precisely controlling reaction conditions, improving yield and efficiency")
            .icon(LARGE_CHEMICAL_REACTOR)
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .addMaterialNeeded(CATALYSIS, 640)
                            .setEureka(LARGE_CHEMICAL_REACTOR, eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserCuttingLathing = BaseNodes.MachinesTree.builder("laser_cutting_lathing", "激光切割与车削", "Laser Cutting and Lathing")
            .description("激光能源在切割机与车床中的应用，利用激光加工的细粒度与高精度，实现复杂零件的快速加工", "Application of laser energy in cutting machines and lathes, utilizing the fine granularity and high precision of laser processing to achieve rapid machining of complex parts")
            .icon(GTMachines.LATHE[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .setEureka(GTMachines.LATHE[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserRollingPressing = BaseNodes.MachinesTree.builder("laser_rolling_pressing", "激光轧制与压延", "Laser Rolling and Pressing")
            .description("激光能源在轧机与压延机中的应用，驱动高动量的锻锤与压辊，对材料进行高效的轧制与压延加工", "Application of laser energy in rolling mills and calenders, driving high-momentum forging hammers and rollers for efficient rolling and pressing of materials")
            .icon(GTMachines.LATHE[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .setEureka(GTMachines.LATHE[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserCrushingRotating = BaseNodes.MachinesTree.builder("laser_crushing_rotating", "激光破碎与旋转加工", "Laser Crushing and Rotating Processing")
            .description("激光能源在破碎机与旋转加工设备中的应用，驱动锯片、研磨头、离心轮等高速旋转部件，实现高效的破碎与旋转加工", "Application of laser energy in crushers and rotating processing equipment, driving high-speed rotating components such as rotary cutters, grinding discs, and centrifugal wheels for efficient crushing and rotating processing")
            .icon(GTMachines.CENTRIFUGE[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .setEureka(GTMachines.CENTRIFUGE[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserElectroMagneticProcessing = BaseNodes.MachinesTree.builder("laser_electromagnetic_processing", "激光电磁加工", "Laser Electromagnetic Processing")
            .description("激光能源在电磁加工设备中的应用，利用高能光子与电磁场的协同作用，实现对材料的高效加工与改性", "Application of laser energy in electromagnetic processing equipment, utilizing the synergistic effect of high-energy photons and electromagnetic fields for efficient material processing and modification")
            .icon(GTMachines.POLARIZER[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(384 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .setEureka(GTMachines.POLARIZER[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserAssemblyProcessing = BaseNodes.MachinesTree.builder("laser_assembly_processing", "激光装配加工", "Laser Assembly Processing")
            .description("激光能源在装配加工设备中的应用，在装配过程中提供高精度的定位与焊接能力，实现复杂组件的高效组装", "Application of laser energy in assembly processing equipment, providing high-precision positioning and welding capabilities during assembly, achieving efficient assembly of complex components")
            .icon(GTMachines.ASSEMBLER[UHV])
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 2400L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .setEureka(GTMachines.ASSEMBLER[UHV], eurekaRate2)
                            .build())
            .tier(2)
            .build();

    public static final TechNode LaserBioEngineering = BaseNodes.MachinesTree.builder("high_energy_bio_engineering", "激光生物工程", "Laser Bioengineering")
            .description("融合激光能源的高能效与生物工程生产的严谨性，是生物技术的巅峰之作", "Combining the high energy efficiency of laser energy with the rigor of bioengineering production, it is the pinnacle of biotechnology")
            .icon(MultiBlockB.MICROORGANISM_MASTER)
            .prerequisites(LaserConditionControlling)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 3600L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .addMaterialNeeded(BIOLOGY, 128)
                            .setEureka(GTOBlocks.LAW_FILTER_CASING, eurekaRate2)
                            .build())
            .tier(3)
            .build();

    public static final TechNode LaserSpaceEngineering = BaseNodes.addRewardDescription(BaseNodes.MachinesTree.builder("laser_space_engineering", "激光太空工程", "Laser Space Engineering")
            .description("将激光能源的高效性带上太空！", "Bringing the efficiency of laser energy into space!")
            .icon(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE.asItem())
            .prerequisites(LaserConditionControlling)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 3600L)
                            .addMaterialNeeded(MECHANICS, 108)
                            .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 2048)
                            .setEureka(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE.asItem(), eurekaRate2)
                            .build())
            .tier(3)
            .build(), "解锁空间站超高安仓及其他高级仓室的使用", "Unlock the use of High-Amp Hatches and other advanced modules in space stations");

    public static final TechNode ComponentProductionEnhancement = BaseNodes.MachinesTree.builder("component_production_enhancement", "组件生产强化", "Component Production Enhancement")
            .description("通过优化生产线和改进组件设计，实现大批量组件的高效节省生产", "Achieve efficient and cost-effective production of large quantities of components through optimized production lines and improved component design")
            .icon(MultiBlockA.COMPONENT_ASSEMBLY_LINE)
            .prerequisites(LaserAssemblyProcessing)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 9900L)
                            .addMaterialNeeded(MECHANICS, 144)
                            .addMaterialNeeded(ASSEMBLY, 384)
                            .setEureka(MultiBlockC.COMPONENT_ASSEMBLER, eurekaRate2)
                            .build())
            .tier(3)
            .build();

    public static final TechNode NeutronActivatorSelfAdoption = BaseNodes.MachinesTree.builder("neutron_activator_self_adoption", "中子活化自适应技术", "Neutron Activator Self-Adoption Technology")
            .description("自动检测并获取中子活化耦合能，从而调整中子活化设备的运行参数，实现高效的中子活化处理", "Automatically detect and acquire neutron activation coupling energy, thereby adjusting the operating parameters of neutron activation equipment for efficient neutron activation processing")
            .icon(MultiBlockC.NEUTRON_VORTEX.asItem())
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 4800L)
                            .addMaterialNeeded(MECHANICS, 64)
                            .setEureka(GTOItems.MACHINING_CONTROL_MODULE_MK3, eurekaRate2)
                            .build())
            .tier(3)
            .build();

    public static final TechNode LaserPlasmaCondenser = BaseNodes.MachinesTree.builder("laser_plasma_condenser", "激光等离子体冷凝器", "Laser Plasma Condenser")
            .description("俺寻思热的东西不是因为它的热运动很强吗？那就用激光把它的热运动给定住不就好了", "I think the hot thing is that its thermal motion is very strong, right? Then just use a laser to fix its thermal motion, isn't it?")
            .icon(MultiBlockA.PLASMA_CONDENSER)
            .prerequisites(LaserBatchProduction)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 4800L)
                            .addMaterialNeeded(THERMODYNAMICS, 1280)
                            .setEureka(GTOBlocks.LASER_COOLING_CASING, eurekaRate2)
                            .build())
            .tier(3)
            .build();

    public static final TechNode ComplexPlasmaCondenser = BaseNodes.MachinesTree.builder("compound_extreme_plasma_condenser", "复杂激光等离子体冷凝器", "Complex Laser Plasma Condenser")
            .description("GTO寰宇重工集团里最大的冰箱", "The largest refrigerator in GTO Universal Heavy Industries Group")
            .icon(MultiBlockF.COMPOUND_EXTREME_COOLING_UNIT)
            .prerequisites(LaserPlasmaCondenser)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 4800L)
                            .addMaterialNeeded(THERMODYNAMICS, 2048)
                            .setEureka(GTMultiMachines.VACUUM_FREEZER, eurekaRate2)
                            .build())
            .tier(3)
            .build();

    public static final TechNode RareEarthProcessing = BaseNodes.MachinesTree.builder("rare_earth_processing", "稀土直接分离技术", "Rare Earth Direct Separation Technology")
            .description("直接分离稀土矿产中的所有元素，无需经过复杂的化学处理过程，实现高效的稀土资源利用", "Directly separate all elements in rare earth minerals without complex chemical processing, achieving efficient utilization of rare earth resources")
            .icon(MultiBlockA.ADVANCED_RARE_EARTH_CENTRIFUGAL)
            .prerequisites(BaseNodes.VoidMiner)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 7200L)
                            .addMaterialNeeded(MATERIAL, 1280)
                            .setEureka(GTOMaterials.RareEarthChlorides, eurekaRate1)
                            .build())
            .tier(4)
            .build();

    public static final TechNode MagnetoResonaticCircuitUpgrade = BaseNodes.addRewardDescription(BaseNodes.MachinesTree.builder("magneto_resonatic_circuit_upgrade", "磁共振电路升级", "Magneto Resonatic Circuit Upgrade")
            .description("改进磁共振电路的设计与制造，减少材料消耗并提升产率", "Improve the design and manufacturing of magneto resonatic circuits, reducing material consumption and increasing yield")
            .icon(GTOItems.MAGNETO_RESONATIC_CIRCUIT[UHV])
            .prerequisites(LaserAssemblyProcessing)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(512 * 20 * 29900L)
                            .addMaterialNeeded(ASSEMBLY, 512)
                            .setEureka(GTOItems.MAGNETO_RESONATIC_CIRCUIT[UHV], eurekaRate1)
                            .build())
            .tier(3)
            .build(), "所有磁共振电路的产量提升1", "Increase the yield of all magneto resonatic circuits by 1");

    public static final TechNode SuprachronalAssemblyLine = BaseNodes.MachinesTree.builder("suprachronal_assembly_line", "超时空装配线", "Suprachronal Assembly Line")
            .description("装配线已经是极限了？不，GTO寰宇重工的工程师们已经突破了时空的限制，让装配线在不同的时空中同时运作，实现了超时空的装配生产", "The assembly line has reached its limit? No, the engineers of GTO Universal Heavy Industries have broken the limits of space-time, allowing the assembly line to operate simultaneously in different space-times, achieving suprachronal assembly production")
            .icon(MultiBlockD.NYARLATHOTEPS_TENTACLE)
            .prerequisites(LaserAssemblyProcessing)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(1024 * 20 * 4800L)
                            .addMaterialNeeded(MECHANICS, 160)
                            .setEureka(MultiBlockD.CIRCUIT_ASSEMBLY_LINE, eurekaRate1)
                            .build())
            .tier(4)
            .build();

    public static final TechNode MagneticConfinementDimensionallyShockProcess = BaseNodes.MachinesTree.builder("magnetic_confinement_dimensionally_shock_process", "磁约束维度冲击工艺", "Magnetic Confinement Dimensionally Shock Process")
            .description("通过磁约束技术，将物质在不同维度下进行冲击处理，实现物质的维度级融合", "Through magnetic confinement technology, subject matter to shock processing in different dimensions, achieving dimensional-level fusion of matter")
            .icon(MultiBlockA.MAGNETIC_CONFINEMENT_DIMENSIONALITY_SHOCK_DEVICE)
            .prerequisites(DimensionSeriesCasings)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(1024 * 20 * 4800L)
                            .addMaterialNeeded(MATERIAL, 2560)
                            .setEureka(MultiBlockH.KERR_NEWMAN_HOMOGENIZER, eurekaRate1)
                            .build())
            .tier(4)
            .build();

    public static final TechNode QFTManipulator = BaseNodes.MachinesTree.builder("qft_manipulator", "量子场论操纵器", "Quantum Field Theory Manipulator")
            .description("通过操纵量子场的波动，实现对物质与能量的精确控制", "By manipulating the fluctuations of quantum fields, achieve precise control over matter and energy")
            .icon(MultiBlockA.QUANTUM_FORCE_TRANSFORMER)
            .prerequisites(QFTSeriesCasing)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(2048 * 20 * 7200L)
                            .addMaterialNeeded(MECHANICS, 256)
                            .setEureka(GTOBlocks.QUANTUM_FORCE_TRANSFORMER_COIL, eurekaRate2)
                            .build())
            .tier(4)
            .build();

    private MachinesNode() {}

    public static void init() {}
}
