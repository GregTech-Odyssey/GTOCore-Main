package com.gtocore.data.techtree;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.utils.RegistriesUtils;

import static com.gtocore.api.research.ResearchTag.ASSEMBLY;
import static com.gtocore.api.research.ResearchTag.CATALYSIS;
import static com.gtocore.api.research.ResearchTag.DATA_STORAGE;
import static com.gtocore.api.research.ResearchTag.MATERIAL;
import static com.gtocore.api.research.ResearchTag.MECHANICS;

public final class AENodes {

    public static final TechNode BaseMEMachines = BaseNodes.AETree.builder("base_me_machines", "基础ME机器", "Basic ME Machines")
            .description("在AE网络内高速传输与组装物质信息，并和生产设备深度交互", "High-speed transmission and assembly of matter information within the AE network, and deep interaction with production equipment")
            .icon(RegistriesUtils.getItem("gtocore:super_molecular_assembler"))
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(32 * 20 * 240L)
                            .setEurekaItem(RegistriesUtils.getItem("expatternprovider:ex_molecular_assembler"), GTORules.TECH_TREE_TIER.isExpert() ? 0.9f : 1f)
                            .build())
            .build();

    public static final TechNode MECapacityExpansion = BaseNodes.AETree.builder("me_capacity_expansion", "ME设备扩容", "ME Capacity Expansion")
            .description("扩展ME设备的存储容量，实现更大规模的物质信息存储与管理", "Expand the storage capacity of ME devices, achieving larger-scale matter information storage and management")
            .icon(GTOItems.PATTERN_BUFFER_UPGRADER1)
            .prerequisites(BaseMEMachines)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(DATA_STORAGE, 250)
                            .setEurekaItem(RegistriesUtils.getItem("gtceu:me_pattern_buffer_proxy"), 0.8F)
                            .build())
            .tier(2)
            .build();

    public static final TechNode MEAdvancedAssembly = BaseNodes.AETree.builder("me_advanced_assembly", "ME高级组装", "ME Advanced Assembly")
            .description("在ME网络中实现更高效的物质信息组装，扩充生产线的跨产品处理能力", "Achieve more efficient matter information assembly in the ME network, expanding the cross-product processing capabilities of production lines")
            .icon(MultiBlockG.SUPER_MOLECULAR_ASSEMBLER.asStack())
            .prerequisites(BaseMEMachines)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(DATA_STORAGE, 150)
                            .addMaterialNeeded(ASSEMBLY, 150)
                            .setEurekaItem(RegistriesUtils.getItem("gtceu:me_pattern_buffer"), 0.9F)
                            .build())
            .tier(2)
            .build();

    public static final TechNode MECatalystSupplying = BaseNodes.AETree.builder("me_catalyst_supplying", "ME催化剂供应", "ME Catalyst Supplying")
            .description("将催化剂的损耗降低到零点，并与ME设备深度交互，使得带有催化剂的生产线被更方便的管理", "Reduce the loss of catalysts to zero and interact deeply with ME devices, making production lines with catalysts easier to manage")
            .icon(RegistriesUtils.getItem("gtocore:me_catalyst_pattern_buffer"))
            .prerequisites(BaseMEMachines)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(MATERIAL, 640)
                            .addMaterialNeeded(CATALYSIS, 256)
                            .setEurekaItem(RegistriesUtils.getItem("gtocore:advanced_catalyst_hatch"), GTORules.TECH_TREE_TIER.isEasy() ? 1f : 0.8F)
                            .build())
            .tier(2)
            .build();

    public static final TechNode MESmartGatingClustering = BaseNodes.AETree.builder("me_smart_gating_clustering", "ME智能分流集群", "ME Smart Gating Clustering")
            .description("将ME设备的物质信息进行智能分流与集群化管理，实现更高效的生产线调度与资源利用", "Intelligently divert and cluster the matter information of ME devices, achieving more efficient production line scheduling and resource utilization")
            .icon(RegistriesUtils.getItem("gtocore:me_wildcard_pattern_buffer"))
            .prerequisites(BaseMEMachines)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(DATA_STORAGE, 25L << (GTORules.TECH_TREE_TIER.level() * 2))
                            .addMaterialNeeded(MECHANICS, 12L * GTORules.TECH_TREE_TIER.level())
                            .setEurekaItem(RegistriesUtils.getItem("gtocore:pattern_content_access_terminal"), GTORules.TECH_TREE_TIER.isEasy() ? 1f : 0.8F)
                            .build())
            .tier(GTORules.TECH_TREE_TIER.level())
            .build();

    public static final TechNode MEWasteRecycling = BaseNodes.AETree.builder("me_waste_recycling", "ME废料回收", "ME Waste Recycling")
            .description("通过回收和再利用生产设备的废料，减少环境污染并提高资源利用率", "By recycling and reusing waste from production equipment, reduce environmental pollution and improve resource utilization")
            .icon(RegistriesUtils.getItem("gtocore:me_muffler_hatch"))
            .prerequisites(BaseMEMachines)
            .requirements(
                    new ResearchRequirements.Builder()
                            .setCWUNeeded(64 * 20 * 1200L)
                            .addMaterialNeeded(MATERIAL, 640)
                            .addMaterialNeeded(CATALYSIS, 128)
                            .setEurekaItem(RegistriesUtils.getItem("gtocore:iv_drone"), GTORules.TECH_TREE_TIER.isEasy() ? 1f : 0.8F)
                            .build())
            .tier(GTORules.TECH_TREE_TIER.isEasy() ? 1 : 2)
            .build();

    private AENodes() {}

    public static void init() {}
}
