package com.gtocore.data.techtree

import com.gtocore.api.data.tag.GTOTagPrefix
import com.gtocore.api.data.tag.GTOTagPrefix.NANITES
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.ASSEMBLY
import com.gtocore.api.research.ResearchTag.BIOLOGY
import com.gtocore.api.research.ResearchTag.CATALYSIS
import com.gtocore.api.research.ResearchTag.MATERIAL
import com.gtocore.api.research.ResearchTag.MECHANICS
import com.gtocore.common.data.machines.MultiBlockC
import com.gtocore.data.techtree.BaseNodes.ChemicalPlantEnvironmentControl
import com.gtocore.data.techtree.BaseNodes.PreciseManufacturingTech
import com.gtocore.data.techtree.BaseNodes.TechTree

import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTMaterials.Carbon
import com.gtolib.utils.RegistriesUtils

object NanitesNodes : AutoInitialize<NanitesNodes>() {
    @JvmField
    val NanitesTech = TechTree.builder("nanites_tech", "纳米蜂群技术", "Nanites Technology")
        .description("强行融合干细胞和细粒度纳米碳粉，制造出的可复制可模板化的纳米蜂群，实现原子尺度上的物质操控", "Forcibly fusing stem cells and fine-grained nano carbon powder to create replicable and templateable nanite swarms, achieving material manipulation at the atomic scale")
        .icon(NANITES, Carbon)
        .prerequisites(ChemicalPlantEnvironmentControl)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(MATERIAL, 1024)
                .addMaterialNeeded(ASSEMBLY, 64)
                .setEurekaItem(RegistriesUtils.getItem("gtceu:activated_carbon_dust"), 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val NanitesProductionLine = TechTree.builder("nanites_production_line", "蜂群生产线加工", "Nanites Production Line")
        .description("让蜂群参与那些繁琐的、工序复杂的生产线加工，优化掉那些产线！", "Let the swarms participate in those tedious and complex production lines, optimizing those production lines!")
        .icon(MultiBlockC.NANITES_INTEGRATED_PROCESSING_CENTER.asItem())
        .prerequisites(NanitesTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(CATALYSIS, 960)
                .addMaterialNeeded(BIOLOGY, 35)
                .setEurekaItem(NANITES, GTMaterials.Copper, 0.8F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val CircuitAssemblyLineTech = TechTree.builder("circuit_assembly_line", "蜂群电路装配", "Circuit Assembly Line Technology")
        .description("让装着纳米蜂群的机器人装配元件，流水线化生产电路板", "Let robots equipped with nanite swarms assemble components, producing circuit boards in an assembly line")
        .icon(RegistriesUtils.getItem("gtocore:circuit_assembly_line"))
        .prerequisites(NanitesTech)
        .requirements(
            ResearchRequirements.Builder()
                .setCWUNeeded(64 * 20 * 1200L)
                .addMaterialNeeded(MECHANICS, 32)
                .addMaterialNeeded(ASSEMBLY, 96)
                .setEurekaItem(RegistriesUtils.getItem("gtocore:precision_circuit_assembly_robot_mk1"), 1.0F)
                .build(),
        )
        .tier(2)
        .build()

    @JvmField
    val NanitesMassiveProduction = TechTree.builder("nanites_massive_production", "纳米蜂群批量复制技术", "Nanites Massive Production Technology")
        .description("以惊人的速度像水龙头一样源源不断的喷出纳米蜂群！", "Spray out nanite swarms continuously like a faucet at an amazing speed!")
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
}
