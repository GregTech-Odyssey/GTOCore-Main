package com.gtocore.data.techtree

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.CATALYSIS
import com.gtocore.api.research.ResearchTag.DATA_STORAGE
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.common.data.GTOItems
import com.gtocore.data.techtree.BaseNodes.TechTree

import com.gtolib.GTOCore
import com.gtolib.utils.RegistriesUtils

object AENodes : AutoInitialize<AENodes>() {
    @JvmField
    val BaseMEMachines = TechTree.builder("base_me_machines", "基础ME机器", "Basic ME Machines")
        .description("在AE网络内高速传输与组装物质信息，并和生产设备深度交互", "High-speed transmission and assembly of matter information within the AE network, and deep interaction with production equipment")
        .icon(RegistriesUtils.getItem("gtocore:super_molecular_assembler"))
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(32 * 20 * 240L)
                .setEurekaItem(RegistriesUtils.getItem("expatternprovider:ex_molecular_assembler"), if (GTOCore.isExpert()) 0.9f else 1f)
                .build(),
        )
        .build()

    @JvmField
    val MECapacityExpansion = TechTree.builder("me_capacity_expansion", "ME设备扩容", "ME Capacity Expansion")
        .description("扩展ME设备的存储容量，实现更大规模的物质信息存储与管理", "Expand the storage capacity of ME devices, achieving larger-scale matter information storage and management")
        .icon(GTOItems.PATTERN_BUFFER_UPGRADER1)
        .prerequisites(BaseMEMachines)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(DATA_STORAGE, 250)
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
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(MATERIAL, 640)
                .addMaterialNeeded(CATALYSIS, 256)
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
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(DATA_STORAGE, 25L shl (GTOCore.difficulty * 2))
                .addMaterialNeeded(MECHANICS, 12L * GTOCore.difficulty)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:pattern_content_access_terminal"), if (GTOCore.isEasy()) 1f else 0.8F)
                .build(),
        )
        .tier(GTOCore.difficulty)
        .build()

    @JvmField
    val MEWasteRecycling = TechTree.builder("me_waste_recycling", "ME废料回收", "ME Waste Recycling")
        .description("通过回收和再利用生产设备的废料，减少环境污染并提高资源利用率", "By recycling and reusing waste from production equipment, reduce environmental pollution and improve resource utilization")
        .icon(RegistriesUtils.getItem("gtocore:me_muffler_hatch"))
        .prerequisites(BaseMEMachines)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(MATERIAL, 640)
                .addMaterialNeeded(CATALYSIS, 128)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:iv_drone"), if (GTOCore.isEasy()) 1f else 0.8F)
                .build(),
        )
        .tier(if (GTOCore.isEasy()) 1 else 2)
        .build()
}
