package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.data.quest.QuestTextures;
import com.gtocore.data.techtree.AENodes;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.EnergyNodes;
import com.gtocore.data.techtree.MachinesNode;
import com.gtocore.data.techtree.NanitesNodes;
import com.gtocore.data.techtree.SpaceNodes;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;

public final class TechnodeChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("technode", c -> c
            .title("科技节点大全")
            .icon(GTOItems.DATA_CRYSTAL_MK4)
            .hideQuestsUntilDepsComplete()
            .autofocus(TechnodeChapter.LASER_FOUNDATIONS)
            .background(QuestTextures.TECHTREE_NODE, 0x555555));

    public static final Quest LASER_FOUNDATIONS = CHAPTER.quest("laser_foundations", q -> q
            .at(0, 0)
            .task(Task.techNode(BaseNodes.LaserFoundations)));

    public static final Quest LASER_ENGRAVER = CHAPTER.quest("laser_engraver", q -> q
            .at(2.132, 0.281)
            .task(Task.techNode(BaseNodes.LaserEngraver))
            .dependsOn(LASER_FOUNDATIONS));

    public static final Quest DATA_BASE = CHAPTER.quest("data_base", q -> q
            .at(-0.123, -4.698)
            .task(Task.techNode(BaseNodes.DataBase)));

    public static final Quest NUCLEAR_PHYSICS = CHAPTER.quest("nuclear_physics", q -> q
            .at(-3.653, -2.958)
            .task(Task.techNode(BaseNodes.NuclearPhysics)));

    public static final Quest SUPER_CONDUCTING_MATERIAL_RESEARCH = CHAPTER.quest("super_conducting_material_research", q -> q
            .at(-4.58, -1.057)
            .task(Task.techNode(BaseNodes.SuperConductingMaterialResearch)));

    public static final Quest SUPERCRITICAL_PHASE_BASIC_RESEARCH = CHAPTER.quest("supercritical_phase_basic_research", q -> q
            .at(4.05, 2.385)
            .task(Task.techNode(BaseNodes.SupercriticalPhaseBasicResearch)));

    public static final Quest THERMODYNAMICS = CHAPTER.quest("thermodynamics", q -> q
            .at(3.052, 3.574)
            .task(Task.techNode(BaseNodes.Thermodynamics)));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE = CHAPTER.quest("component_in_assembly_line", q -> q
            .at(-4.402, 1.646)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineluv)));

    public static final Quest IRIDIUM_CASING_PRODUCTION = CHAPTER.quest("iridium_casing_production", q -> q
            .at(4.388, -1.684)
            .task(Task.techNode(MachinesNode.IridiumCasingProduction)));

    public static final Quest BASE_ME_MACHINES = CHAPTER.quest("base_me_machines", q -> q
            .at(1.491, -4.457)
            .task(Task.techNode(AENodes.BaseMEMachines)));

    public static final Quest ENERGY_IO6 = CHAPTER.quest("energy_io6", q -> q
            .at(1.646, 4.402)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.LuV])));

    public static final Quest HIGH_DENSITY_ENERGY_STORAGE = CHAPTER.quest("high_density_energy_storage", q -> q
            .at(0.041, 4.7)
            .task(Task.techNode(EnergyNodes.LapotronEnergeStorge)));

    public static final Quest SUPER_ROCKET_TECH = CHAPTER.quest("super_rocket_tech", q -> q
            .at(-1.53, 4.444)
            .task(Task.techNode(SpaceNodes.SuperRocketTech)));

    public static final Quest THERMODYNAMICS_2 = CHAPTER.quest("thermodynamics_2", q -> q
            .at(2.861, -3.729)
            .task(Task.techNode(BaseNodes.Thermodynamics)));

    public static final Quest CHEMICAL_PLANT_ENVIRONMENT_CONTROL = CHAPTER.quest("chemical_plant_environment_control", q -> q
            .at(-2.958, 3.653)
            .task(Task.techNode(MachinesNode.ChemicalPlantEnvironmentControl)));

    public static final Quest COMPONENT_CASING6 = CHAPTER.quest("component_casing6", q -> q
            .at(-6.048, 1.957)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.LuV]))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE1 = CHAPTER.quest("component_in_assembly_line1", q -> q
            .at(-6.351, 0.285)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLinezpm))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest COMPONENT_CASING7 = CHAPTER.quest("component_casing7", q -> q
            .at(-7.662, 2.249)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.ZPM]))
            .dependsOn(COMPONENT_CASING6, COMPONENT_IN_ASSEMBLY_LINE1));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE2 = CHAPTER.quest("component_in_assembly_line2", q -> q
            .at(-7.965, 0.576)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuv))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE1));

    public static final Quest COMPONENT_CASING8 = CHAPTER.quest("component_casing8", q -> q
            .at(-9.276, 2.541)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.UV]))
            .dependsOn(COMPONENT_CASING7, COMPONENT_IN_ASSEMBLY_LINE2));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE3 = CHAPTER.quest("component_in_assembly_line3", q -> q
            .at(-9.578, 0.868)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuhv))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE2));

    public static final Quest COMPONENT_CASING9 = CHAPTER.quest("component_casing9", q -> q
            .at(-10.89, 2.833)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.UHV]))
            .dependsOn(COMPONENT_CASING8, COMPONENT_IN_ASSEMBLY_LINE3));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE4 = CHAPTER.quest("component_in_assembly_line4", q -> q
            .at(-11.192, 1.16)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuev))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE3));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE5 = CHAPTER.quest("component_in_assembly_line5", q -> q
            .at(-12.806, 1.452)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuiv))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE4));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE6 = CHAPTER.quest("component_in_assembly_line6", q -> q
            .at(-14.42, 1.744)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuxv))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE5));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE7 = CHAPTER.quest("component_in_assembly_line7", q -> q
            .at(-16.034, 2.036)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineopv))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE6));

    public static final Quest COMPONENT_IN_ASSEMBLY_LINE8 = CHAPTER.quest("component_in_assembly_line8", q -> q
            .at(-17.647, 2.327)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLinemax))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE7));

    public static final Quest COMPONENT_CASING10 = CHAPTER.quest("component_casing10", q -> q
            .at(-12.504, 3.125)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.UEV]))
            .dependsOn(COMPONENT_CASING9, COMPONENT_IN_ASSEMBLY_LINE4));

    public static final Quest COMPONENT_CASING11 = CHAPTER.quest("component_casing11", q -> q
            .at(-14.117, 3.417)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.UIV]))
            .dependsOn(COMPONENT_CASING10, COMPONENT_IN_ASSEMBLY_LINE5));

    public static final Quest COMPONENT_CASING12 = CHAPTER.quest("component_casing12", q -> q
            .at(-15.731, 3.708)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.UXV]))
            .dependsOn(COMPONENT_CASING11, COMPONENT_IN_ASSEMBLY_LINE6));

    public static final Quest COMPONENT_CASING13 = CHAPTER.quest("component_casing13", q -> q
            .at(-17.345, 4)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.OpV]))
            .dependsOn(COMPONENT_CASING12, COMPONENT_IN_ASSEMBLY_LINE7));

    public static final Quest COMPONENT_CASING14 = CHAPTER.quest("component_casing14", q -> q
            .at(-18.959, 4.292)
            .task(Task.techNode(ComponentNodes.ComponentCasingsNodes[GTValues.MAX]))
            .dependsOn(COMPONENT_CASING13, COMPONENT_IN_ASSEMBLY_LINE8));

    public static final Quest WETWARE_TECH = CHAPTER.quest("wetware_tech", q -> q
            .at(-1.986, -4.26)
            .task(Task.techNode(BaseNodes.WetwareTech)));

    public static final Quest ME_ADVANCED_ASSEMBLY = CHAPTER.quest("me_advanced_assembly", q -> q
            .at(1.3, -6.102)
            .task(Task.techNode(AENodes.MEAdvancedAssembly))
            .dependsOn(BASE_ME_MACHINES));

    public static final Quest ME_CAPACITY_EXPANSION = CHAPTER.quest("me_capacity_expansion", q -> q
            .at(2.543, -7.188)
            .task(Task.techNode(AENodes.MECapacityExpansion))
            .dependsOn(BASE_ME_MACHINES));

    public static final Quest ME_CATALYST_SUPPLYING = CHAPTER.quest("me_catalyst_supplying", q -> q
            .at(2.998, -8.961)
            .task(Task.techNode(AENodes.MECatalystSupplying))
            .dependsOn(BASE_ME_MACHINES));

    public static final Quest ME_SMART_GATING_CLUSTERING = CHAPTER.quest("me_smart_gating_clustering", q -> q
            .at(4.136, -10.085)
            .task(Task.techNode(AENodes.MESmartGatingClustering))
            .dependsOn(BASE_ME_MACHINES));

    public static final Quest ME_WASTE_RECYCLING = CHAPTER.quest("me_waste_recycling", q -> q
            .at(2.786, -5.637)
            .task(Task.techNode(AENodes.MEWasteRecycling))
            .dependsOn(BASE_ME_MACHINES));

    public static final Quest SPACE_ELEVATOR = CHAPTER.quest("space_elevator", q -> q
            .at(-1.424, 6.061)
            .task(Task.techNode(SpaceNodes.SpaceElevator))
            .dependsOn(SUPER_ROCKET_TECH));

    public static final Quest SPACE_ELEVATOR2 = CHAPTER.quest("space_elevator2", q -> q
            .at(-2.218, 7.893)
            .task(Task.techNode(SpaceNodes.SpaceElevator2))
            .dependsOn(SPACE_ELEVATOR));

    public static final Quest SPACE_ELEVATOR3 = CHAPTER.quest("space_elevator3", q -> q
            .at(-2.617, 9.444)
            .task(Task.techNode(SpaceNodes.SpaceElevator3))
            .dependsOn(SPACE_ELEVATOR2));

    public static final Quest SPACE_ELEVATOR4 = CHAPTER.quest("space_elevator4", q -> q
            .at(-3.822, 10.793)
            .task(Task.techNode(SpaceNodes.SpaceElevator4))
            .dependsOn(SPACE_ELEVATOR3));

    public static final Quest SPACE_ELEVATOR5 = CHAPTER.quest("space_elevator5", q -> q
            .at(-5.453, 11.966)
            .task(Task.techNode(SpaceNodes.SpaceElevator5))
            .dependsOn(SPACE_ELEVATOR4));

    public static final Quest SPACE_PROBE_SURFACE_RECEPTION = CHAPTER.quest("space_probe_surface_reception", q -> q
            .at(-7.425, 12.86)
            .task(Task.techNode(SpaceNodes.SpaceProbeSurfaceReception))
            .dependsOn(SPACE_ELEVATOR5));

    public static final Quest ENERGY_IO7 = CHAPTER.quest("energy_io7", q -> q
            .at(3.236, 5.292)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.ZPM]))
            .dependsOn(ENERGY_IO6));

    public static final Quest ENERGY_IO8 = CHAPTER.quest("energy_io8", q -> q
            .at(4.454, 6.81)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UV]))
            .dependsOn(ENERGY_IO7));

    public static final Quest ENERGY_IO9 = CHAPTER.quest("energy_io9", q -> q
            .at(4.824, 8.442)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UHV]))
            .dependsOn(ENERGY_IO8));

    public static final Quest ENERGY_IO10 = CHAPTER.quest("energy_io10", q -> q
            .at(5.532, 10.154)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UEV]))
            .dependsOn(ENERGY_IO9));

    public static final Quest ENERGY_IO11 = CHAPTER.quest("energy_io11", q -> q
            .at(5.489, 11.696)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UIV]))
            .dependsOn(ENERGY_IO10));

    public static final Quest ENERGY_IO12 = CHAPTER.quest("energy_io12", q -> q
            .at(5.538, 13.781)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UXV]))
            .dependsOn(ENERGY_IO11));

    public static final Quest ENERGY_IO13 = CHAPTER.quest("energy_io13", q -> q
            .at(3.651, 15.677)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.OpV]))
            .dependsOn(ENERGY_IO12));

    public static final Quest ENERGY_IO14 = CHAPTER.quest("energy_io14", q -> q
            .at(3.346, 17.331)
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.MAX]))
            .dependsOn(ENERGY_IO13));

    public static final Quest ENERGY_FLUX_ANALYSIS = CHAPTER.quest("energy_flux_analysis", q -> q
            .at(-0.644, 7.395)
            .task(Task.techNode(EnergyNodes.EnergyFluxAnalysis))
            .dependsOn(HIGH_DENSITY_ENERGY_STORAGE));

    public static final Quest ULTIMATE_ENERGY_STORAGE = CHAPTER.quest("ultimate_energy_storage", q -> q
            .at(0.439, 6.229)
            .task(Task.techNode(EnergyNodes.UltimateEnergyStorage))
            .dependsOn(HIGH_DENSITY_ENERGY_STORAGE));

    public static final Quest ULTIMATE_BATTERY2 = CHAPTER.quest("ultimate_battery2", q -> q
            .at(0.673, 8.345)
            .task(Task.techNode(EnergyNodes.UltimateBattery2))
            .dependsOn(ULTIMATE_ENERGY_STORAGE));

    public static final Quest ULTIMATE_BATTERY3 = CHAPTER.quest("ultimate_battery3", q -> q
            .at(-0.425, 9.741)
            .task(Task.techNode(EnergyNodes.UltimateBattery3))
            .dependsOn(ULTIMATE_BATTERY2));

    public static final Quest ULTIMATE_BATTERY4 = CHAPTER.quest("ultimate_battery4", q -> q
            .at(-1.202, 11.437)
            .task(Task.techNode(EnergyNodes.UltimateBattery4))
            .dependsOn(ULTIMATE_BATTERY3));

    public static final Quest ULTIMATE_BATTERY5 = CHAPTER.quest("ultimate_battery5", q -> q
            .at(-2.073, 13.087)
            .task(Task.techNode(EnergyNodes.UltimateBattery5))
            .dependsOn(ULTIMATE_BATTERY4));

    public static final Quest ULTIMATE_BATTERY6 = CHAPTER.quest("ultimate_battery6", q -> q
            .at(-3.108, 14.623)
            .task(Task.techNode(EnergyNodes.UltimateBattery6))
            .dependsOn(ULTIMATE_BATTERY5));

    public static final Quest BIOWARE_TECH = CHAPTER.quest("bioware_tech", q -> q
            .at(-2.654, -5.824)
            .task(Task.techNode(BaseNodes.BiowareTech))
            .dependsOn(WETWARE_TECH));

    public static final Quest BIO_ENERGY_CONVERSION = CHAPTER.quest("bio_energy_conversion", q -> q
            .at(-3.922, -6.677)
            .task(Task.techNode(EnergyNodes.BioEnergyConversion))
            .dependsOn(BIOWARE_TECH));

    public static final Quest LARGE_NAQUADAH_REACTOR = CHAPTER.quest("large_naquadah_reactor", q -> q
            .at(-19.699, 2.001)
            .task(Task.techNode(EnergyNodes.LargeNaquadahReactor))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE2));

    public static final Quest NANITES_TECH = CHAPTER.quest("nanites_tech", q -> q
            .at(-3.759, 5.08)
            .task(Task.techNode(NanitesNodes.NanitesTech))
            .dependsOn(CHEMICAL_PLANT_ENVIRONMENT_CONTROL));

    public static final Quest CIRCUIT_ASSEMBLY_LINE = CHAPTER.quest("circuit_assembly_line", q -> q
            .at(-4.149, 6.77)
            .task(Task.techNode(NanitesNodes.CircuitAssemblyLineTech))
            .dependsOn(NANITES_TECH));

    public static final Quest NANITES_PRODUCTION_LINE = CHAPTER.quest("nanites_production_line", q -> q
            .at(-5.572, 7.531)
            .task(Task.techNode(NanitesNodes.NanitesProductionLine))
            .dependsOn(NANITES_TECH));

    public static final Quest NANITES_MASSIVE_PRODUCTION = CHAPTER.quest("nanites_massive_production", q -> q
            .at(-7.119, 8.047)
            .task(Task.techNode(NanitesNodes.NanitesMassiveProduction))
            .dependsOn(NANITES_TECH));

    public static final Quest COMPUTATION_ARRAY = CHAPTER.quest("computation_array", q -> q
            .at(-0.981, -5.965)
            .task(Task.techNode(BaseNodes.ComputationArray))
            .dependsOn(DATA_BASE));

    public static final Quest DATA_IN_RESEARCH = CHAPTER.quest("data_in_research", q -> q
            .at(0.188, -7.21)
            .task(Task.techNode(BaseNodes.DataCenter))
            .dependsOn(DATA_BASE));

    public static final Quest TOKAMAK_FUSION_REACTOR = CHAPTER.quest("tokamak_fusion_reactor", q -> q
            .at(-5.867, -2.798)
            .task(Task.techNode(BaseNodes.TokamakFusionReactor))
            .dependsOn(SUPER_CONDUCTING_MATERIAL_RESEARCH, NUCLEAR_PHYSICS));

    public static final Quest SCAN_STATION = CHAPTER.quest("scan_station", q -> q
            .at(1.185, -9.796)
            .task(Task.techNode(BaseNodes.ScanStation))
            .dependsOn(DATA_BASE));

    public static final Quest CRYSTAL_TECH_MAINFRAME = CHAPTER.quest("crystal_tech_mainframe", q -> q
            .at(-7.5, 3.5)
            .task(Task.techNode(BaseNodes.CrystalTechMainframe))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest VOID_MINER = CHAPTER.quest("void_miner", q -> q
            .at(4.614, 0.897)
            .task(Task.techNode(BaseNodes.VoidMiner)));

    public static final Quest DATA_STORAGE_ITERATION = CHAPTER.quest("data_storage_iteration", q -> q
            .at(-1.3, -7.465)
            .task(Task.techNode(BaseNodes.DataStorageIteration))
            .dependsOn(COMPUTATION_ARRAY));

    public static final Quest TOKAMAK_FUSION_REACTOR2 = CHAPTER.quest("tokamak_fusion_reactor2", q -> q
            .at(-7.37, -3.595)
            .task(Task.techNode(BaseNodes.TokamakFusionReactor2))
            .dependsOn(TOKAMAK_FUSION_REACTOR));

    public static final Quest VIRTUAL_COIN_CURRENCY = CHAPTER.quest("virtual_coin_currency", q -> q
            .at(-0.34, -9.319)
            .task(Task.techNode(BaseNodes.VirtualCoinCurrency))
            .dependsOn(COMPUTATION_ARRAY));

    public static final Quest EXCITATION_CRYSTAL_LASER = CHAPTER.quest("excitation_crystal_laser", q -> q
            .at(-2.5, 0)
            .task(Task.techNode(BaseNodes.ExcitationCrystalLaser))
            .dependsOn(LASER_FOUNDATIONS));

    public static final Quest PARTICLE_ACCELERATOR = CHAPTER.quest("particle_accelerator", q -> q
            .at(-4.422, -4.346)
            .task(Task.techNode(BaseNodes.ParticleAccelerators))
            .dependsOn(NUCLEAR_PHYSICS));

    public static final Quest SUPER_COMPUTING_TECH = CHAPTER.quest("super_computing_tech", q -> q
            .at(-0.285, -10.893)
            .task(Task.techNode(BaseNodes.SupercomputingTech))
            .dependsOn(COMPUTATION_ARRAY));

    public static final Quest BIOWARE_DATA_STORAGE = CHAPTER.quest("bioware_data_storage", q -> q
            .at(-3.728, -8.194)
            .task(Task.techNode(BaseNodes.BiowareDataStorage))
            .dependsOn(BIOWARE_TECH));

    public static final Quest TOKAMAK_FUSION_REACTOR3 = CHAPTER.quest("tokamak_fusion_reactor3", q -> q
            .at(-8.86, -4.417)
            .task(Task.techNode(BaseNodes.TokamakFusionReactor3))
            .dependsOn(TOKAMAK_FUSION_REACTOR2));

    public static final Quest SELF_MAINTENANCE_SYSTEM = CHAPTER.quest("self_maintenance_system", q -> q
            .at(-0.328, 2.76)
            .task(Task.techNode(MachinesNode.SelfMaintenanceSystem))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest ISA_MILLING_MACHINE = CHAPTER.quest("isa_milling_machine", q -> q
            .at(-8.851, 6.777)
            .task(Task.techNode(MachinesNode.IsaMillingMachine))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest PRECISE_MANUFACTURING_TECH = CHAPTER.quest("precise_manufacturing_tech", q -> q
            .at(-8.53, 10.508)
            .task(Task.techNode(MachinesNode.PreciseManufacturingTech))
            .dependsOn(COMPONENT_IN_ASSEMBLY_LINE));

    public static final Quest FUEL_REFINERY_COMPLEX = CHAPTER.quest("fuel_refinery_complex", q -> q
            .at(-5.368, 5.094)
            .task(Task.techNode(MachinesNode.FuelRefineryComplex))
            .dependsOn(CHEMICAL_PLANT_ENVIRONMENT_CONTROL));

    public static final Quest MOLECULAR_SERIES_CASINGS = CHAPTER.quest("molecular_series_casings", q -> q
            .at(6.313, -1.566)
            .task(Task.techNode(MachinesNode.MolecularSeriesCasings))
            .dependsOn(IRIDIUM_CASING_PRODUCTION));

    public static final Quest ADVANCED_ASSEMBLY_LINE_MACHINE = CHAPTER.quest("advanced_assembly_line_machine", q -> q
            .at(-13.835, 11.808)
            .task(Task.techNode(MachinesNode.AdvancedAssemblyLineMachine))
            .dependsOn(PRECISE_MANUFACTURING_TECH));

    public static final Quest LASER_BATCH_PRODUCTION_PROTO = CHAPTER.quest("laser_batch_production_proto", q -> q
            .at(2.529, -1.706)
            .task(Task.techNode(MachinesNode.LaserBatchProduction))
            .dependsOn(IRIDIUM_CASING_PRODUCTION, LASER_FOUNDATIONS));

    public static final Quest LASER_ASSEMBLY_PROCESSING = CHAPTER.quest("laser_assembly_processing", q -> q
            .at(5.796, -6.387)
            .task(Task.techNode(MachinesNode.LaserAssemblyProcessing))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_CRUSHING_ROTATING = CHAPTER.quest("laser_crushing_rotating", q -> q
            .at(4.296, -3.228)
            .task(Task.techNode(MachinesNode.LaserCrushingRotating))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_CUTTING_LATHING = CHAPTER.quest("laser_cutting_lathing", q -> q
            .at(4.008, -4.746)
            .task(Task.techNode(MachinesNode.LaserCuttingLathing))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_ELECTROMAGNETIC_PROCESSING = CHAPTER.quest("laser_electromagnetic_processing", q -> q
            .at(4.167, -6.393)
            .task(Task.techNode(MachinesNode.LaserElectroMagneticProcessing))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_REACTION_ADJUSTER = CHAPTER.quest("laser_reaction_adjuster", q -> q
            .at(7.421, -2.622)
            .task(Task.techNode(MachinesNode.LaserConditionControlling))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_HIGH_TEMP_THERMAL_PROCESSING = CHAPTER.quest("laser_high_temp_thermal_processing", q -> q
            .at(5.605, -4.685)
            .task(Task.techNode(MachinesNode.LaserHiTempThermalProcessing))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_PACKAGER = CHAPTER.quest("laser_packager", q -> q
            .at(5.664, -0.182)
            .task(Task.techNode(MachinesNode.LaserSortingPackaging))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest LASER_ROLLING_PRESSING = CHAPTER.quest("laser_rolling_pressing", q -> q
            .at(5.896, -3.041)
            .task(Task.techNode(MachinesNode.LaserRollingPressing))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest DIMENSION_SERIES_CASINGS = CHAPTER.quest("dimension_series_casings", q -> q
            .at(9.235, 0.047)
            .task(Task.techNode(MachinesNode.DimensionSeriesCasings))
            .dependsOn(MOLECULAR_SERIES_CASINGS));

    public static final Quest LASER_PLASMA_CONDENSER = CHAPTER.quest("laser_plasma_condenser", q -> q
            .at(7.235, -4.465)
            .task(Task.techNode(MachinesNode.LaserPlasmaCondenser))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest NEUTRON_ACTIVATOR_SELF_ADOPTION = CHAPTER.quest("neutron_activator_self_adoption", q -> q
            .at(7.571, -0.747)
            .task(Task.techNode(MachinesNode.NeutronActivatorSelfAdoption))
            .dependsOn(LASER_BATCH_PRODUCTION_PROTO));

    public static final Quest COMPONENT_PRODUCTION_ENHANCEMENT = CHAPTER.quest("component_production_enhancement", q -> q
            .at(7.214, -7.57)
            .task(Task.techNode(MachinesNode.ComponentProductionEnhancement))
            .dependsOn(LASER_ASSEMBLY_PROCESSING));

    public static final Quest MAGNETO_RESONATIC_CIRCUIT_UPGRADE = CHAPTER.quest("magneto_resonatic_circuit_upgrade", q -> q
            .at(8.658, -8.602)
            .task(Task.techNode(MachinesNode.MagnetoResonaticCircuitUpgrade))
            .dependsOn(LASER_ASSEMBLY_PROCESSING));

    public static final Quest COMPOUND_EXTREME_PLASMA_CONDENSER = CHAPTER.quest("compound_extreme_plasma_condenser", q -> q
            .at(7.864, -5.85)
            .task(Task.techNode(MachinesNode.ComplexPlasmaCondenser))
            .dependsOn(LASER_PLASMA_CONDENSER));

    public static final Quest HIGH_ENERGY_BIO_ENGINEERING = CHAPTER.quest("high_energy_bio_engineering", q -> q
            .at(9.731, -4.85)
            .task(Task.techNode(MachinesNode.LaserBioEngineering))
            .dependsOn(LASER_REACTION_ADJUSTER));

    public static final Quest LASER_SPACE_ENGINEERING = CHAPTER.quest("laser_space_engineering", q -> q
            .at(12.385, -6.434)
            .task(Task.techNode(MachinesNode.LaserSpaceEngineering))
            .dependsOn(LASER_REACTION_ADJUSTER));

    public static final Quest RARE_EARTH_PROCESSING = CHAPTER.quest("rare_earth_processing", q -> q
            .at(5.938, 2.644)
            .task(Task.techNode(MachinesNode.RareEarthProcessing))
            .dependsOn(VOID_MINER));

    public static final Quest SUPRACHRONAL_ASSEMBLY_LINE = CHAPTER.quest("suprachronal_assembly_line", q -> q
            .at(9.128, -10.787)
            .task(Task.techNode(MachinesNode.SuprachronalAssemblyLine))
            .dependsOn(LASER_ASSEMBLY_PROCESSING));

    public static final Quest DYSON_SPHERE_SERIES_CASING = CHAPTER.quest("dyson_sphere_series_casing", q -> q
            .at(12.564, 0.602)
            .task(Task.techNode(MachinesNode.DysonSphereSeriesCasing))
            .dependsOn(DIMENSION_SERIES_CASINGS));

    public static final Quest MAGNETIC_CONFINEMENT_DIMENSIONALLY_SHOCK_PROCESS = CHAPTER.quest("magnetic_confinement_dimensionally_shock_process", q -> q
            .at(10.479, -0.917)
            .task(Task.techNode(MachinesNode.MagneticConfinementDimensionallyShockProcess))
            .dependsOn(DIMENSION_SERIES_CASINGS));

    public static final Quest QFT_SERIES_CASING = CHAPTER.quest("qft_series_casing", q -> q
            .at(12.763, -1.065)
            .task(Task.techNode(MachinesNode.QFTSeriesCasing))
            .dependsOn(DIMENSION_SERIES_CASINGS));

    public static final Quest TIME_DILATION_DIMENSION_SERIES_CASING = CHAPTER.quest("time_dilation_dimension_series_casing", q -> q
            .at(10.85, 0.809)
            .task(Task.techNode(MachinesNode.TimeDilationDimensionSeriesCasing))
            .dependsOn(DIMENSION_SERIES_CASINGS));

    public static final Quest QFT_MANIPULATOR = CHAPTER.quest("qft_manipulator", q -> q
            .at(14.231, -2.189)
            .task(Task.techNode(MachinesNode.QFTManipulator))
            .dependsOn(QFT_SERIES_CASING));

    public static final Quest STELLAR_FORGE = CHAPTER.quest("stellar_forge", q -> q
            .at(9.131, -1.925)
            .task(Task.techNode(BaseNodes.StellarForge))
            .dependsOn(MOLECULAR_SERIES_CASINGS));

    public static final Quest BEDROCK_PRODUCTION = CHAPTER.quest("bedrock_production", q -> q
            .at(7.217, 3.677)
            .task(Task.techNode(BaseNodes.BedrockMining))
            .dependsOn(VOID_MINER));

    public static final Quest LASER_ENGRAVER2 = CHAPTER.quest("laser_engraver2", q -> q
            .at(3.639, -0.286)
            .task(Task.techNode(BaseNodes.LaserEngraver2))
            .dependsOn(LASER_ENGRAVER));

    public static final Quest OPTICAL_TECH = CHAPTER.quest("optical_tech", q -> q
            .at(-5.564, -7.946)
            .task(Task.techNode(BaseNodes.OpticalTech))
            .dependsOn(BIOWARE_TECH));

    public static final Quest BIOWARE_SUPERCOMPUTING = CHAPTER.quest("bioware_supercomputing", q -> q
            .at(-3.316, -10.524)
            .task(Task.techNode(BaseNodes.BiowareSupercomputing))
            .dependsOn(BIOWARE_TECH));

    public static final Quest SCANNER_INNOVATION = CHAPTER.quest("scanner_innovation", q -> q
            .at(1.348, -8.231)
            .task(Task.techNode(BaseNodes.ScannerInnovation))
            .dependsOn(SCAN_STATION));

    public static final Quest MATTER_FABRICATOR = CHAPTER.quest("matter_fabricator", q -> q
            .at(-5.314, -6.113)
            .task(Task.techNode(BaseNodes.MatterFabricator))
            .dependsOn(PARTICLE_ACCELERATOR));

    public static final Quest BLOCKHOLE_DATA_STORAGE = CHAPTER.quest("blockhole_data_storage", q -> q
            .at(-4.482, -9.493)
            .task(Task.techNode(BaseNodes.BlockholeDataStorage))
            .dependsOn(BIOWARE_DATA_STORAGE));

    public static final Quest DATA_CENTER_OVERCLOCKING = CHAPTER.quest("data_center_overclocking", q -> q
            .at(-2.247, -8.66)
            .task(Task.techNode(BaseNodes.DataCenterOverclocking))
            .dependsOn(BIOWARE_SUPERCOMPUTING));

    public static final Quest TOKAMAK_FUSION_REACTOR4 = CHAPTER.quest("tokamak_fusion_reactor4", q -> q
            .at(-10.336, -5.266)
            .task(Task.techNode(BaseNodes.TokamakFusionReactor4))
            .dependsOn(TOKAMAK_FUSION_REACTOR3));

    public static final Quest LEPTONIC_CHARGE = CHAPTER.quest("leptonic_charge", q -> q
            .at(8.91, -3.435)
            .task(Task.techNode(BaseNodes.LeptonicCharge))
            .dependsOn(STELLAR_FORGE));

    public static final Quest PLANET_CORE_EXTRACTION = CHAPTER.quest("planet_core_extraction", q -> q
            .at(8.4, 4.85)
            .task(Task.techNode(BaseNodes.PlanetCoreExtraction))
            .dependsOn(VOID_MINER));

    public static final Quest ENERGY_INJECTED_FISSION_TECH = CHAPTER.quest("energy_injected_fission_tech", q -> q
            .at(-6.592, -4.878)
            .task(Task.techNode(BaseNodes.EnergyInjectedFissionTech))
            .dependsOn(PARTICLE_ACCELERATOR));

    public static final Quest GWCA_COMPUTING_TECH = CHAPTER.quest("gwca_computing_tech", q -> q
            .at(-1.702, -10.064)
            .task(Task.techNode(BaseNodes.GWCAComputingTech))
            .dependsOn(DATA_STORAGE_ITERATION));

    public static final Quest VIRTUAL_UNIVERSE_DATA_STORAGE = CHAPTER.quest("virtual_universe_data_storage", q -> q
            .at(-5.026, -10.921)
            .task(Task.techNode(BaseNodes.VirtualUniverseDataStorage))
            .dependsOn(BLOCKHOLE_DATA_STORAGE));

    public static final Quest TOKAMAK_FUSION_REACTOR5 = CHAPTER.quest("tokamak_fusion_reactor5", q -> q
            .at(-10.517, -7.641)
            .task(Task.techNode(BaseNodes.TokamakFusionReactor5))
            .dependsOn(TOKAMAK_FUSION_REACTOR4));

    public static final Quest SPS_TECH = CHAPTER.quest("sps_tech", q -> q
            .at(-12.575, -4.33)
            .task(Task.techNode(BaseNodes.SPSTech))
            .dependsOn(TOKAMAK_FUSION_REACTOR4));

    public static final Quest ATOMIC_ENERGY_EXCITING = CHAPTER.quest("atomic_energy_exciting", q -> q
            .at(-14.575, -2.57)
            .task(Task.techNode(BaseNodes.AtomicEnergyExciting))
            .dependsOn(SPS_TECH));

    public static final Quest TIME_DILATION_TECH = CHAPTER.quest("time_dilation_tech", q -> q
            .at(-11.019, -9.578)
            .task(Task.techNode(BaseNodes.TimeDilationTech))
            .dependsOn(SPS_TECH));

    public static final Quest MASS_ENERGY_CONVERSION_TECH = CHAPTER.quest("mass_energy_conversion_tech", q -> q
            .at(-13.837, -7.203)
            .task(Task.techNode(BaseNodes.MassEnergyConversionTech))
            .dependsOn(SPS_TECH));

    public static final Quest CRYOTHEUM_SUPERCONDUCTING_TECH = CHAPTER.quest("cryotheum_superconducting_tech", q -> q
            .at(-13.449, -1.177)
            .task(Task.techNode(BaseNodes.CryotheumSupercoductingTech))
            .dependsOn(SPS_TECH, SUPER_CONDUCTING_MATERIAL_RESEARCH));

    public static final Quest EXOTIC_TECHNOLOGY = CHAPTER.quest("exotic_technology", q -> q
            .at(-7.427, -9.679)
            .task(Task.techNode(BaseNodes.ExoticTech))
            .dependsOn(OPTICAL_TECH, TIME_DILATION_TECH));

    public static final Quest COSMIC_TECHNOLOGY = CHAPTER.quest("cosmic_technology", q -> q
            .at(-7.712, -11.564)
            .task(Task.techNode(BaseNodes.CosmicTech))
            .dependsOn(EXOTIC_TECHNOLOGY));

    public static final Quest EX_DURABLE_PLASMA_CONTAINER = CHAPTER.quest("ex_durable_plasma_container", q -> q
            .at(9.624, -6.707)
            .task(Task.techNode(BaseNodes.ExDurablePlasmaContainer))
            .dependsOn(LASER_PLASMA_CONDENSER));

    public static final Quest HYPER_DIMENSIONAL_FORGE = CHAPTER.quest("hyper_dimensional_forge", q -> q
            .at(11.905, -3.298)
            .task(Task.techNode(BaseNodes.HyperDimensionalForge))
            .dependsOn(STELLAR_FORGE));

    public static final Quest STELLAR_ULTIMATE_FORGE = CHAPTER.quest("stellar_ultimate_forge", q -> q
            .at(10.376, -2.896)
            .task(Task.techNode(BaseNodes.StellarUltimateForge))
            .dependsOn(STELLAR_FORGE));

    public static final Quest HYPER_DIMENSIONAL_FORGE_COIL = CHAPTER.quest("hyper_dimensional_forge_coil", q -> q
            .at(13.438, -3.689)
            .task(Task.techNode(BaseNodes.HyperDimensionalForgeCoil))
            .dependsOn(HYPER_DIMENSIONAL_FORGE));

    public static final Quest QUANTUM_CHROMODYNAMIC_CHARGE = CHAPTER.quest("quantum_chromodynamic_charge", q -> q
            .at(11.298, -4.794)
            .task(Task.techNode(BaseNodes.QuantumChromodynamicCharge))
            .dependsOn(LEPTONIC_CHARGE));

    public static final Quest ADVANCED_MASS_FABRICATION_TECH = CHAPTER.quest("advanced_mass_fabrication_tech", q -> q
            .at(-14.906, -8.78)
            .task(Task.techNode(BaseNodes.AdvancedMassFabricationTech))
            .dependsOn(MASS_ENERGY_CONVERSION_TECH));

    public static final Quest ELEMENT_FABRICATION_TECH = CHAPTER.quest("element_fabrication_tech", q -> q
            .at(-15.742, -7.174)
            .task(Task.techNode(BaseNodes.ElementFabricationTech))
            .dependsOn(MASS_ENERGY_CONVERSION_TECH));

    public static final Quest MANIFOLD_OSCILLATORY = CHAPTER.quest("manifold_oscillatory", q -> q
            .at(-7.727, -13.437)
            .task(Task.techNode(BaseNodes.ManifoldOscillatory))
            .dependsOn(COSMIC_TECHNOLOGY));

    public static final Quest SUPRACAUSAL_TECH = CHAPTER.quest("supracausal_tech", q -> q
            .at(-7.479, -15.266)
            .task(Task.techNode(BaseNodes.SupracausalTech))
            .dependsOn(MANIFOLD_OSCILLATORY));

    public static final Quest DRAGON_CORE = CHAPTER.quest("dragon_core", q -> q
            .at(-6.982, -17.024)
            .task(Task.techNode(BaseNodes.DragonCore))
            .dependsOn(SUPRACAUSAL_TECH));

    public static final Quest UNIVERSE_SIMULATION = CHAPTER.quest("universe_simulation", q -> q
            .at(-6.251, -18.682)
            .task(Task.techNode(BaseNodes.UniverseSimulation))
            .dependsOn(DRAGON_CORE));

    public static final Quest AWAKENED_CORE = CHAPTER.quest("awakened_core", q -> q
            .at(-5.304, -20.216)
            .task(Task.techNode(BaseNodes.AwakenedCore))
            .dependsOn(UNIVERSE_SIMULATION));

    public static final Quest SUPRACHRONAL_DRONE = CHAPTER.quest("suprachronal_drone", q -> q
            .at(-4.198, -21.596)
            .task(Task.techNode(BaseNodes.SuprachronalDrone))
            .dependsOn(AWAKENED_CORE));

    public static final Quest SUPRACHRONAL_CIRCUITS = CHAPTER.quest("suprachronal_circuits", q -> q
            .at(-2.922, -22.814)
            .task(Task.techNode(BaseNodes.SuprachronalCircuits))
            .dependsOn(SUPRACHRONAL_DRONE));

    public static final Quest CHAOS_CORE = CHAPTER.quest("chaos_core", q -> q
            .at(-1.501, -23.853)
            .task(Task.techNode(BaseNodes.ChaosCore))
            .dependsOn(SUPRACHRONAL_CIRCUITS));

    public static final Quest CREATE = CHAPTER.quest("create", q -> q
            .at(0.043, -24.7)
            .task(Task.techNode(BaseNodes.Create))
            .dependsOn(CHAOS_CORE));

    public static final Quest DYSON_SPHERE = CHAPTER.quest("dyson_sphere", q -> q
            .at(14.454, 0.581)
            .task(Task.techNode(EnergyNodes.DysonSphere))
            .dependsOn(DYSON_SPHERE_SERIES_CASING));

    public static final Quest HYPER_REACTOR = CHAPTER.quest("hyper_reactor", q -> q
            .at(-16.169, -3.29)
            .task(Task.techNode(EnergyNodes.HyperReactor))
            .dependsOn(ATOMIC_ENERGY_EXCITING, LARGE_NAQUADAH_REACTOR));

    public static final Quest ADVANCED_HYPER_REACTOR = CHAPTER.quest("advanced_hyper_reactor", q -> q
            .at(-17.924, -2.519)
            .task(Task.techNode(EnergyNodes.AdvancedHyperReactor))
            .dependsOn(HYPER_REACTOR));

    public static final Quest ANNIHILATION_GENERATOR = CHAPTER.quest("annihilation_generator", q -> q
            .at(-19.353, -1.353)
            .task(Task.techNode(EnergyNodes.AnnihilationGenerator))
            .dependsOn(ADVANCED_HYPER_REACTOR));

    public static final Quest Q_07D69A7A = CHAPTER.quest("q_07d69a7a", q -> q
            .at(0, -2)
            .desc(d -> d
                    .line("此章节为EMI中展示的科技树的复刻版本，隐去了当前暂未解锁的科技节点。")
                    .line()
                    .line("可以从这里挑选自己的研究目标。"))
            .task(Task.checkmark()));

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_IV, -0.5, -2.5, 2, 2)));
    }
}
