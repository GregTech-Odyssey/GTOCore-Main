package com.gtocore.data.techtree

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.ResearchTag.EXOTIC
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.common.data.GTOItems
import com.gtocore.common.data.machines.GeneratorMultiblock
import com.gtocore.data.techtree.BaseNodes.AdvancedMassFabricationTech
import com.gtocore.data.techtree.BaseNodes.AtomicEnergyExciting
import com.gtocore.data.techtree.BaseNodes.BiowareTech
import com.gtocore.data.techtree.BaseNodes.DysonSphereSeriesCasing
import com.gtocore.data.techtree.BaseNodes.EnergyTree
import com.gtocore.data.techtree.BaseNodes.MainTree
import com.gtocore.data.techtree.ComponentNodes.EnergyIOs

import com.gregtechceu.gtceu.api.GTValues.ZPM
import com.gregtechceu.gtceu.common.data.GTItems
import com.gtolib.utils.RegistriesUtils

object EnergyNodes : AutoInitialize<EnergyNodes>() {
    @JvmField
    val LapotronEnergeStorge = EnergyTree.builder("high_density_energy_storage", "兰博顿水晶能量存储", "Lapotron Crystal Energy Storage")
        .description("利用高能量密度的兰博顿水晶，通过不断的压缩与优化，实现高效的能量存储与释放", "Utilize high energy density Lapotron crystals, through continuous compression and optimization, to achieve efficient energy storage and release")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.ENERGY_LAPOTRONIC_ORB, 1.0f).build())
        .icon(GTItems.ENERGY_LAPOTRONIC_ORB_CLUSTER)
        .build()

    @JvmField
    val EnergyFluxAnalysis = EnergyTree.builder("energy_flux_analysis", "能量流分析", "Energy Flux Analysis")
        .description("分析能量流的传输与分布，实现高效的能量管理与优化", "Analyze the transmission and distribution of energy flux, achieving efficient energy management and optimization")
        .icon(RegistriesUtils.getItem("gtmthings:wireless_energy_terminal"))
        .prerequisites(LapotronEnergeStorge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(48 * 20 * 300L)
                .addMaterialNeeded(ENERGY, 20)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:engraved_lapotron_crystal_chip"), 0.8F)
                .build(),
        )
        .tier(1)
        .build()

    @JvmField
    val UltimateEnergyStorage = EnergyTree.builder("ultimate_energy_storage", "终极能量存储", "Ultimate Energy Storage")
        .description("将至少500倍体积的兰博顿水晶压缩到一个电池中存储的技术", "The technology of compressing at least 500 times the volume of Lapotron crystals into a single battery for storage")
        .icon(GTItems.ULTIMATE_BATTERY)
        .prerequisites(LapotronEnergeStorge)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:energy_cluster"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val UltimateBattery2 = EnergyTree.builder("ultimate_battery2", "终极电池II", "Ultimate Battery II")
        .description("换了颜色的终极电池", "A different colored ultimate battery")
        .icon(GTOItems.REALLY_MAX_BATTERY)
        .prerequisites(UltimateEnergyStorage)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 512)
                .setEurekaItem(GTItems.ULTIMATE_BATTERY, 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val UltimateBattery3 = EnergyTree.builder("ultimate_battery3", "终极电池III", "Ultimate Battery III")
        .description("换了颜色的终极电池II", "A different colored ultimate battery II")
        .icon(GTOItems.TRANSCENDENT_MAX_BATTERY)
        .prerequisites(UltimateBattery2)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 1024)
                .setEurekaItem(GTOItems.REALLY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val UltimateBattery4 = EnergyTree.builder("ultimate_battery4", "终极电池IV", "Ultimate Battery IV")
        .description("颜色更加鲜艳的终极电池III", "An even more colorful ultimate battery III")
        .icon(GTOItems.EXTREMELY_MAX_BATTERY)
        .prerequisites(UltimateBattery3)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(4096 * 20 * 8000L)
                .addMaterialNeeded(ENERGY, 4096)
                .setEurekaItem(GTOItems.TRANSCENDENT_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(5)
        .build()

    @JvmField
    val UltimateBattery5 = EnergyTree.builder("ultimate_battery5", "终极电池V", "Ultimate Battery V")
        .description("看起来比较疯狂的终极电池IV", "A seemingly insane ultimate battery IV")
        .icon(GTOItems.INSANELY_MAX_BATTERY)
        .prerequisites(UltimateBattery4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 12288)
                .setEurekaItem(GTOItems.EXTREMELY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val UltimateBattery6 = EnergyTree.builder("ultimate_battery6", "终极电池VI", "Ultimate Battery VI")
        .description("终极电池的终极形态，看着很帅", "The ultimate form of the ultimate battery, looks very cool")
        .icon(GTOItems.MEGA_MAX_BATTERY)
        .prerequisites(UltimateBattery5)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 28800L)
                .addMaterialNeeded(ENERGY, 16384)
                .setEurekaItem(GTOItems.INSANELY_MAX_BATTERY, 0.8F)
                .build(),
        )
        .tier(6)
        .build()

    @JvmField
    val LargeNaquadahReactor = EnergyTree.builder("large_naquadah_reactor", "大型硅岩反应堆", "Large Naquadah Reactor")
        .description("硅岩这种材料怎么就这么神奇呢？又硬又坚韧，还能用来做反应堆的核心燃料", "How is naquadah such a magical material? It's hard and tough, and can even be used as the core fuel for reactors")
        .icon(RegistriesUtils.getItem("gtocore:large_naquadah_reactor"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1500L)
                .addMaterialNeeded(ENERGY, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:zpm_naquadah_reactor"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val HyperReactor = EnergyTree.builder("hyper_reactor", "超能反应堆", "Hyper Reactor")
        .description("用于对超高能硅岩燃料进行反应的超高能反应堆，能够提供极高的能量输出与出色的运行性能", "A hyper reactor used for reacting with hyper-silicon fuel, capable of providing extremely high energy output and excellent operating performance")
        .icon(RegistriesUtils.getItem("gtocore:hyper_reactor"))
        .prerequisites(AtomicEnergyExciting, LargeNaquadahReactor)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 512)
                .setEurekaItem(GeneratorMultiblock.LARGE_NAQUADAH_REACTOR.asItem(), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val AdvancedHyperReactor = EnergyTree.builder("advanced_hyper_reactor", "进阶超高能反应堆", "Advanced Hyper Reactor")
        .description("用于对更加浓缩的超高能硅岩燃料进行反应的超高能反应堆，能够提供更高的能量输出与更稳定的运行性能", "A hyper reactor used for reacting with more concentrated hyper-silicon fuel, capable of providing higher energy output and more stable operating performance")
        .icon(RegistriesUtils.getItem("gtocore:advanced_hyper_reactor"))
        .prerequisites(HyperReactor)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(ENERGY, 4096)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:hyper_reactor"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val DysonSphere = EnergyTree.builder("dyson_sphere", "戴森球建造技术", "Dyson Sphere Construction Technology")
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
    val BioEnergyConversion = EnergyTree.builder("bio_energy_conversion", "生物能量转换技术", "Bio-Energy Conversion Technology")
        .description("利用生物体的代谢过程，将生物能量转化为可用的电能，实现绿色能源的高效利用", "Utilize the metabolic processes of organisms to convert bio-energy into usable electrical energy, achieving efficient use of green energy")
        .icon(RegistriesUtils.getItem("gtocore:bio_cardiomyocyte_cluster"))
        .prerequisites(BiowareTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(256 * 20 * 2400L)
                .addMaterialNeeded(BIOLOGY, 128)
                .addMaterialNeeded(ENERGY, 300)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:bio_cardiomyocyte_cluster"), 0.7F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val AnnihilationGenerator = EnergyTree.builder("annihilation_generator", "湮灭发电机", "Annihilation Generator")
        .description("通过物质与反物质的湮灭反应，产生巨大的能量输出", "Generate enormous energy output through matter-antimatter annihilation reactions")
        .icon(RegistriesUtils.getItem("gtocore:annihilate_generator"))
        .prerequisites(AdvancedMassFabricationTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(8192 * 20 * 7200L)
                .addMaterialNeeded(ENERGY, 16384)
                .addMaterialNeeded(EXOTIC, 10)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:annihilation_constrainer"), 0.7F)
                .build(),
        )
        .tier(5)
        .build()
}
