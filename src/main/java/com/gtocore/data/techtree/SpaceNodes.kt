package com.gtocore.data.techtree

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.INTERSTELLAR_ENGINEERING
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.common.data.GTOMaterials
import com.gtocore.data.techtree.BaseNodes.MainTree

import com.gtolib.utils.RegistriesUtils

object SpaceNodes : AutoInitialize<SpaceNodes>() {
    @JvmField
    val SuperRocketTech = MainTree.builder("super_rocket_tech", "超级火箭技术", "Super Rocket Technology")
        .description("掌握超级火箭的设计与制造技术，实现更高效的太空运输与探索", "Master the design and manufacturing technology of super rockets, achieving more efficient space transportation and exploration")
        .icon(RegistriesUtils.getItem("ad_astra_rocketed:tier_7_rocket"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 120L)
                .setEurekaFluid(GTOMaterials.StellarEnergyRocketFuel.fluid, 0.8F)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 180)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val SpaceElevator = MainTree.builder("space_elevator", "太空电梯", "Space Elevator")
        .description("建造一条连接地球与太空的电梯，把手可摘星辰变为现实", "Build an elevator connecting the Earth and space, turning the dream of reaching the stars into reality")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator"))
        .prerequisites(SuperRocketTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 400)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:gravitation_engine_unit"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val SpaceElevator2 = MainTree.builder("space_elevator2", "太空电梯动力改良", "Space Elevator Power Improvement")
        .description("改良太空电梯的动力系统，实现更高效的能量传输与运输能力", "Improve the power system of the space elevator, achieving more efficient energy transmission and transportation capabilities")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_2"))
        .prerequisites(SpaceElevator)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 1200L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 1920)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_1"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val SpaceElevator3 = MainTree.builder("space_elevator3", "太空电梯动力改良II", "Space Elevator Power Improvement II")
        .description("升级太空电梯的动力系统，实现更高效的能量传输与运输能力", "Upgrade the power system of the space elevator, achieving more efficient energy transmission and transportation capabilities")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_3"))
        .prerequisites(SpaceElevator2)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 2400L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 7680)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_2"), 0.8F)
                .build(),
        )
        .tier(3)
        .build()

    @JvmField
    val SpaceElevator4 = MainTree.builder("space_elevator4", "太空电梯动力改良III", "Space Elevator Power Improvement III")
        .description("我要是能乘坐在上面观光就好了", "I wish I could take a sightseeing ride on it")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_4"))
        .prerequisites(SpaceElevator3)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 15360)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_3"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SpaceElevator5 = MainTree.builder("space_elevator5", "太空电梯动力改良IV", "Space Elevator Power Improvement IV")
        .description("全GTO寰宇重工最快的电梯！", "The fastest elevator in the entire GTO Universal Heavy Industries!")
        .icon(RegistriesUtils.getItem("gtocore:space_elevator_power_module_5"))
        .prerequisites(SpaceElevator4)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(512 * 20 * 9600L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 25600)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:space_elevator_power_module_4"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()

    @JvmField
    val SpaceProbeSurfaceReception = MainTree.builder("space_probe_surface_reception", "空间探测器表面接收技术", "Space Probe Surface Reception Technology")
        .description("将空间中杂乱的辐射能量进行收集与转换，转化为可用的资源", "Collect and convert the chaotic radiation energy in space into usable resources")
        .icon(RegistriesUtils.getItem("gtocore:space_probe_surface_reception"))
        .prerequisites(SpaceElevator5)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(1024 * 20 * 4800L)
                .addMaterialNeeded(INTERSTELLAR_ENGINEERING, 16000)
                .addMaterialNeeded(MECHANICS, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:cosmic_detection_receiver_material_ray_absorbing_array"), 0.8F)
                .build(),
        )
        .tier(4)
        .build()
}
