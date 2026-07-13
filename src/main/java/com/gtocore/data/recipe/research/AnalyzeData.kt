package com.gtocore.data.recipe.research

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.TeamResearchContext
import com.gtocore.api.research.ui.ResearchTreeSideTab.OTHER_REWARD_LABEL
import com.gtocore.api.research.ui.ResearchTreeSideTab.RECIPE_REWARD_LABEL
import com.gtocore.api.research.ui.ResearchTreeSideTab.UNLOCKABLE_LABEL
import com.gtocore.api.techtree.TechNode
import com.gtocore.api.techtree.TechTreeManager
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOItems
import com.gtocore.common.data.GTOMaterials

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gto.fastcollection.O2OOpenCacheHashMap
import com.gtolib.api.annotation.DataGeneratorScanned
import com.gtolib.api.annotation.language.RegisterLanguage
import com.gtolib.api.lang.CNEN
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import earth.terrarium.adastra.common.registry.ModItems
import it.unimi.dsi.fastutil.objects.*

object AnalyzeData : AutoInitialize<AnalyzeData>() {

    val langMap: Map<String, CNEN> = if (GTCEu.isDataGen()) O2OOpenCacheHashMap() else emptyMap()

    val techTree: TechTreeManager<TeamResearchContext> =
        TechTreeManager("main_tree", "研究树", "Research Tree", ItemStackTexture(GTOItems.BLUE_HALIDE_LAMP.asStack()))

    val recipe2Node: Reference2ReferenceMap<GTRecipeDefinition, TechNode<TeamResearchContext>> =
        Reference2ReferenceOpenHashMap()
    val node2Recipes: Reference2ObjectMap<TechNode<TeamResearchContext>, ReferenceSet<GTRecipeDefinition>> =
        Reference2ObjectOpenHashMap()
    val node2RewardLines: Reference2ObjectMap<TechNode<TeamResearchContext>, ArrayList<Component>> = Reference2ObjectOpenHashMap()

    val tierItems = mapOf(
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
    val BasicMaterialStudy = techTree.builder("basic_material_study", "基础材料研究", "Basic Material Study")
        .description("研究金属与矿物特性", "Study metal and mineral properties")
        .icon(Items.IRON_INGOT)
        .build()
    val EnergyTransmissionResearch = techTree.builder("energy_transmission_research", "能量传输研究", "Energy Transmission Research")
        .description("探索高效能量传输方法", "Explore efficient energy transmission methods")
        .icon(ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Copper))
        .build()

    // ==================== dataTier 1: 行星系探索基础（15项） ====================
    // 行星观测与定位
    @JvmField
    val PlanetaryTelescopeCalibration = techTree.builder("planetary_telescope_calibration", "行星望远镜校准", "Planetary Telescope Calibration")
        .description("优化光学望远镜参数，提高行星观测精度", "Optimize optical telescope parameters to improve planetary observation accuracy")
        .icon(Items.ENDER_EYE)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(10).setEurekaItem(Items.ENDER_EYE, 1.0f).build())
        .tier(1)
        .build()

    @JvmField
    val SolarSystemOrbitModeling = techTree.builder("solar_system_orbit_modeling", "太阳系轨道建模", "Solar System Orbit Modeling")
        .description("研究行星轨道运动规律，建立太阳系模型", "Study planetary orbital motion laws and establish a solar system model")
        .icon(Items.COMPASS)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(15).setEurekaItem(Items.COMPASS, 1.0f).build())
        .tier(1)
        .build()

    @JvmField
    val PlanetarySurfaceFeatureMapping = techTree.builder("planetary_surface_feature_mapping", "行星表面特征测绘", "Planetary Surface Feature Mapping")
        .description("利用遥感技术绘制行星表面特征图", "Use remote sensing technology to map planetary surface features")
        .icon(Items.MAP)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(20).setEurekaItem(Items.MAP, 1.0f).build())
        .tier(1)
        .build()

    @JvmField
    val StellarReferenceFrame = techTree.builder("stellar_reference_frame", "恒星参考系建立", "Stellar Reference Frame Establishment")
        .description("建立恒星参考系，用于精确定位和导航", "Establish a stellar reference frame for precise positioning and navigation")
        .icon(Items.NETHER_STAR)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(25).setEurekaItem(Items.NETHER_STAR, 1.0f).build())
        .tier(1)
        .build()
        .addRewardDescription("你对星系的理解有了质的飞跃", "You have made a qualitative leap in your understanding of the galaxy")

    // 基础航天材料
    @JvmField
    val AluminumMagnesiumAlloyForging = techTree.builder("aluminum_magnesium_alloy_forging", "铝镁合金锻造", "Aluminum-Magnesium Alloy Forging")
        .description("研究铝镁合金的锻造工艺与性能", "Study the forging process and properties of aluminum-magnesium alloy")
        .icon(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Magnalium))
        .tier(1)
        .prerequisites(BasicMaterialStudy)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(2500000000).setEurekaItem(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Magnalium), 0.5f).build())
        .build()

    @JvmField
    val VacuumResistantMaterialTesting = techTree.builder("vacuum_resistant_material_testing", "耐真空材料测试", "Vacuum-Resistant Material Testing")
        .description("检测材料在真空环境下的物理性能稳定性", "Detect the physical performance stability of materials in vacuum environment")
        .icon(GTOItems.RAW_VACUUM_TUBE)
        .prerequisites(BasicMaterialStudy)
        .tier(1)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(6666666666).setEurekaItem(GTOItems.RAW_VACUUM_TUBE, 0.5f).build())
        .build()

    @JvmField
    val LowTemperatureResistantPolymerRAndD = techTree.builder("low_temp_resistant_polymer", "耐低温聚合物研发", "Low-Temperature Resistant Polymer R&D")
        .description("开发适用于低温环境的高性能聚合物材料", "Develop high-performance polymer materials suitable for low-temperature environments")
        .icon(ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polyethylene))
        .tier(1)
        .requirements(ResearchRequirements.Builder().setCWUNeeded(4000).setEurekaItem(Items.NETHER_STAR, 1.0f).build())
        .prerequisites(BasicMaterialStudy)
        .build()

    // 行星探测准备
    @JvmField
    val UnmannedProbeBatteryTech = techTree.builder("unmanned_probe_battery_tech", "无人探测器电池技术", "Unmanned Probe Battery Technology")
        .description("研发高能量密度电池，为无人探测器提供持久动力", "Develop high energy density batteries to provide long-lasting power for unmanned probes")
        .icon(GTItems.BATTERY_HV_LITHIUM)
        .tier(1)
        .prerequisites(EnergyTransmissionResearch, VacuumResistantMaterialTesting)
        .build()

    @JvmField
    val SimpleThermalControlSystem = techTree.builder("simple_thermal_control_system", "简易热控制系统", "Simple Thermal Control System")
        .description("设计基础热控制系统，确保探测器在极端温度下正常工作", "Design a basic thermal control system to ensure the probe operates normally under extreme temperatures")
        .icon(GTOBlocks.HEAT_PIPES[0])
        .tier(1)
        .prerequisites(LowTemperatureResistantPolymerRAndD, AluminumMagnesiumAlloyForging)
        .build()

    // 星际通信入门
    @JvmField
    val RadioWavePropagationInSpace = techTree.builder("radio_wave_propagation_in_space", "空间无线电波传播", "Radio Wave Propagation in Space")
        .description("研究无线电波在太空中的传播特性", "Study the propagation characteristics of radio waves in space")
        .icon(GTItems.EMITTER_IV)
        .tier(1)
        .prerequisites(PlanetaryTelescopeCalibration, SolarSystemOrbitModeling)
        .build()

    @JvmField
    val ProbeDataTransmissionProtocol = techTree.builder("probe_data_transmission_protocol", "探测器数据传输协议", "Probe Data Transmission Protocol")
        .description("制定探测器与地面站的通信数据格式标准", "Formulate communication data format standards between probes and ground stations")
        .icon(GTOItems.PLANET_DATA_CHIP)
        .tier(1)
        .prerequisites(RadioWavePropagationInSpace)
        .build()

    @JvmField
    val SignalNoiseReductionTechnology = techTree.builder("signal_noise_reduction_tech", "信号降噪技术", "Signal Noise Reduction Technology")
        .description("开发有效的信号降噪方法，提高通信质量", "Develop effective signal noise reduction methods to improve communication quality")
        .icon(GTItems.SENSOR_IV)
        .tier(1)
        .prerequisites(ProbeDataTransmissionProtocol)
        .build()

    // 行星资源初探
    @JvmField
    val LunarSoilCompositionAnalysis = techTree.builder("lunar_soil_composition_analysis", "月球土壤成分分析", "Lunar Soil Composition Analysis")
        .description("分析月球土壤的化学成分和矿物结构", "Analyze the chemical composition and mineral structure of lunar soil")
        .icon(ModItems.MOON_SAND.get())
        .tier(1)
        .prerequisites(PlanetarySurfaceFeatureMapping, UnmannedProbeBatteryTech)
        .build()

    @JvmField
    val PlanetaryResourceSignatureDetection = techTree.builder("planetary_resource_signature_detection", "行星资源特征探测", "Planetary Resource Signature Detection")
        .description("利用遥感技术识别行星表面的资源特征", "Use remote sensing technology to identify resource features on planetary surfaces")
        .icon(GTOItems.PLANET_DATA_CHIP)
        .tier(1)
        .prerequisites(LunarSoilCompositionAnalysis)
        .build()

    @JvmField
    val WaterIceSignatureRecognition = techTree.builder("water_ice_signature_recognition", "水冰特征识别", "Water Ice Signature Recognition")
        .description("检测行星表面和地下的水冰存在特征", "Detect the presence features of water ice on planetary surfaces and underground")
        .icon(ModItems.ICE_SHARD.get())
        .tier(1)
        .prerequisites(PlanetaryResourceSignatureDetection)
        .build()

    // ==================== dataTier 2: 行星探测深化（25项） ====================
    // 观测技术升级

    @JvmField
    val InfraredImagingSensorDevelopment = techTree.builder("infrared_imaging_sensor_development", "红外成像传感器开发", "Infrared Imaging Sensor Development")
        .description("研发适用于行星观测的高灵敏度红外传感器", "Develop high-sensitivity infrared sensors suitable for planetary observation")
        .icon(GTItems.SENSOR_LuV)
        .tier(2)
        .prerequisites(PlanetaryTelescopeCalibration, PlanetarySurfaceFeatureMapping)
        .build()

    @JvmField
    val MultispectralPlanetScanning = techTree.builder("multispectral_planet_scanning", "多光谱行星扫描", "Multispectral Planetary Scanning")
        .description("利用多光谱成像技术获取行星表面信息", "Use multispectral imaging technology to obtain planetary surface information")
        .icon(GTOItems.PLANET_SCAN_SATELLITE)
        .tier(2)
        .prerequisites(InfraredImagingSensorDevelopment, StellarReferenceFrame)
        .build()

    @JvmField
    val PlanetaryAtmosphereCompositionDetection = techTree.builder("planetary_atmosphere_composition_detection", "行星大气成分检测", "Planetary Atmosphere Composition Detection")
        .description("分析行星大气的化学成分和物理特性", "Analyze the chemical composition and physical characteristics of planetary atmospheres")
        .icon(GTOMaterials.JupiterAir.getFluid())
        .tier(2)
        .prerequisites(PlanetaryResourceSignatureDetection)
        .build()

    @JvmField
    val SubsurfaceStructureRadarProbe = techTree.builder("subsurface_structure_radar_probe", "地下结构雷达探测", "Subsurface Structure Radar Probe")
        .description("利用雷达技术探测行星地下结构和资源分布", "Use radar technology to detect planetary subsurface structures and resource distribution")
        .icon(GTOItems.PROSPECTOR_MANA_HV)
        .tier(2)
        .prerequisites(PlanetarySurfaceFeatureMapping)
        .build()

    // 推进系统优化
    @JvmField
    val LiquidFuelRocketEngineTesting = techTree.builder("liquid_fuel_rocket_engine_testing", "液体燃料火箭发动机测试", "Liquid Fuel Rocket Engine Testing")
        .description("测试液体燃料火箭发动机性能，优化推进系统", "Test liquid fuel rocket engine performance and optimize propulsion systems")
        .icon(ModItems.DESH_ENGINE.get())
        .tier(2)
        .prerequisites(AluminumMagnesiumAlloyForging, UnmannedProbeBatteryTech, SimpleThermalControlSystem)
        .build()

    @JvmField
    val RocketThrustEnhancement = techTree.builder("rocket_thrust_enhancement", "火箭推力增强", "Rocket Thrust Enhancement")
        .description("改进燃料喷射系统提升火箭发动机推力", "Improve fuel injection system to enhance rocket engine thrust")
        .icon(ModItems.ROCKET_FIN.get())
        .tier(2)
        .prerequisites(LiquidFuelRocketEngineTesting)
        .build()

    @JvmField
    val PropellantEfficiencyOptimization = techTree.builder("propellant_efficiency_optimization", "推进剂效率优化", "Propellant Efficiency Optimization")
        .description("优化推进剂配方和燃烧效率，提升航天器性能", "Optimize propellant formulation and combustion efficiency to enhance spacecraft performance")
        .icon(GTOMaterials.Hydrazine.getFluid())
        .tier(2)
        .prerequisites(LiquidFuelRocketEngineTesting)
        .build()

    @JvmField
    val RocketNozzleDesignImprovement = techTree.builder("rocket_nozzle_design_improvement", "火箭喷管设计改进", "Rocket Nozzle Design Improvement")
        .description("改进火箭喷管设计，提高推进效率和稳定性", "Improve rocket nozzle design to enhance propulsion efficiency and stability")
        .icon(GTOBlocks.SPACE_ENGINE_NOZZLE)
        .tier(2)
        .prerequisites(RocketThrustEnhancement, VacuumResistantMaterialTesting)
        .build()

    fun addRecipeToNode(recipe: GTRecipeDefinition, node: TechNode<TeamResearchContext>) {
        recipe2Node[recipe] = node
        node2Recipes.computeIfAbsent(node) { ReferenceOpenHashSet() }.add(recipe)
        node2RewardLines.computeIfAbsent(node) { arrayListOf() }
            .add(
                Component.translatable(RECIPE_REWARD_LABEL).withStyle(ChatFormatting.DARK_PURPLE)
                    .append(getMainOutput(recipe).withStyle(ChatFormatting.GRAY)),
            )
    }
    private fun getMainOutput(recipe: GTRecipeDefinition): MutableComponent {
        val outputs0 = recipe.itemOutputs
        if (outputs0.isNotEmpty()) {
            return outputs0[0].name
        }
        val outputs1 = recipe.fluidOutputs
        if (outputs1.isNotEmpty()) {
            return outputs1[0].name
        }
        return Component.empty()
    }

    fun TechNode<TeamResearchContext>.addRewardDescription(descriptionCN: String, descriptionEN: String): TechNode<TeamResearchContext> {
        if (langMap is O2OOpenCacheHashMap) {
            langMap[this.name] = CNEN(descriptionCN, descriptionEN)
        } else {
            node2RewardLines.computeIfAbsent(this) { arrayListOf() }.add(
                Component.translatable(OTHER_REWARD_LABEL).withStyle(
                    ChatFormatting.DARK_PURPLE,
                ).append(Component.translatable("gtocore.data.${this.name}").withStyle(ChatFormatting.GRAY)),
            )
        }
        return this
    }

    fun getRewardLines(node: TechNode<TeamResearchContext>?): List<Component> {
        if (node == null) return emptyList()
        val lines = node2RewardLines[node]
        val firstLine = Component.translatable(UNLOCKABLE_LABEL).withStyle(ChatFormatting.GRAY)
        return if (lines != null) {
            listOf(firstLine) + lines
        } else {
            emptyList()
        }
    }
}
