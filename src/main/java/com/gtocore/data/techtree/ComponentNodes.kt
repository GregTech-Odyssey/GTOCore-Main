package com.gtocore.data.techtree

import com.gtocore.api.misc.AutoInitialize
import com.gtocore.api.research.ResearchRequirements
import com.gtocore.api.research.ResearchTag.ASSEMBLY
import com.gtocore.api.research.ResearchTag.ENERGY
import com.gtocore.api.research.techtree.TechNode
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOItems
import com.gtocore.data.techtree.BaseNodes.MainTree

import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.api.GTValues.IV
import com.gregtechceu.gtceu.api.GTValues.LuV
import com.gregtechceu.gtceu.api.GTValues.MAX
import com.gregtechceu.gtceu.api.GTValues.ULV
import com.gregtechceu.gtceu.api.GTValues.UV
import com.gregtechceu.gtceu.api.GTValues.VN
import com.gregtechceu.gtceu.api.GTValues.ZPM
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gto.registrate.util.entry.BlockEntry

import kotlin.collections.map

object ComponentNodes : AutoInitialize<BaseNodes>() {
    @JvmField
    val ComponentInAssemblyLineluv = MainTree.builder("component_in_assembly_line", "装配线基础部件", "Basic components in assembly line")
        .description("在装配线中组装基础的组件", "Assemble basic components in the assembly line")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 20L).setEurekaItem(GTItems.FIELD_GENERATOR_IV, 1.0f).build())
        .icon(GTItems.FIELD_GENERATOR_LuV)
        .tier(0)
        .build()

    @JvmField
    val ComponentInAssemblyLinezpm = MainTree.builder("component_in_assembly_line1", "装配线进阶部件I", "Advanced components in assembly line I")
        .description("在装配线中组装更加复杂的部件", "Assemble more complex components in the assembly line")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32 * 20 * 240L).setEurekaItem(GTItems.FIELD_GENERATOR_LuV, 1.0f).build())
        .icon(GTItems.FIELD_GENERATOR_ZPM)
        .prerequisites(ComponentInAssemblyLineluv)
        .tier(0)
        .build()

    @JvmField
    val ComponentInAssemblyLineuv = MainTree.builder("component_in_assembly_line2", "装配线进阶部件II", "Advanced components in assembly line II")
        .description("利用三钛合金制造成更加强悍的部件", "Use trititanium alloy to manufacture even more powerful components")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 1200L).setEurekaItem(GTItems.FIELD_GENERATOR_ZPM, 0.8f).build())
        .icon(GTItems.FIELD_GENERATOR_UV)
        .prerequisites(ComponentInAssemblyLinezpm)
        .tier(1)
        .build()

    @JvmField
    val ComponentInAssemblyLineuhv = MainTree.builder("component_in_assembly_line3", "装配线进阶部件III", "Advanced components in assembly line III")
        .description("充能下界合金的磁化与山铜为其带来了更强的动力与耐久性", "The magnetization of charged nether alloy and the addition of copper bring it stronger power and durability")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(64 * 20 * 1800L).setEurekaItem(GTItems.FIELD_GENERATOR_UV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UHV)
        .prerequisites(ComponentInAssemblyLineuv)
        .tier(2)
        .build()

    @JvmField
    val ComponentInAssemblyLineuev = MainTree.builder("component_in_assembly_line4", "装配线进阶部件IV", "Advanced components in assembly line IV")
        .description("搭载了下一代末影耐造材料与技术", "Equipped with next-generation enderly durable materials and technology")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(325 * 20 * 2400L).setEurekaItem(GTItems.FIELD_GENERATOR_UHV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UEV)
        .prerequisites(ComponentInAssemblyLineuhv)
        .tier(3)
        .build()

    @JvmField
    val ComponentInAssemblyLineuiv = MainTree.builder("component_in_assembly_line5", "装配线进阶部件V", "Advanced components in assembly line V")
        .description("制造能抗住微型黑洞的新部件", "Manufacture new components that can withstand micro black holes")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(800 * 20 * 2400L).setEurekaItem(GTItems.FIELD_GENERATOR_UEV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UIV)
        .prerequisites(ComponentInAssemblyLineuev)
        .tier(3)
        .build()

    @JvmField
    val ComponentInAssemblyLineuxv = MainTree.builder("component_in_assembly_line6", "装配线进阶部件VI", "Advanced components in assembly line VI")
        .description("制造撕裂宇宙的新部件", "Manufacture new components that can tear the universe apart")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(2000 * 20 * 3600L).setEurekaItem(GTItems.FIELD_GENERATOR_UIV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_UXV)
        .prerequisites(ComponentInAssemblyLineuiv)
        .tier(4)
        .build()

    @JvmField
    val ComponentInAssemblyLineopv = MainTree.builder("component_in_assembly_line7", "装配线进阶部件VII", "Advanced components in assembly line VII")
        .description("将混沌神龙力量注入到部件中", "Inject the power of the chaotic dragon into the components")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(16000 * 20 * 4000L).setEurekaItem(GTItems.FIELD_GENERATOR_UXV, 0.7f).build())
        .icon(GTItems.FIELD_GENERATOR_OpV)
        .prerequisites(ComponentInAssemblyLineuxv)
        .tier(5)
        .build()

    @JvmField
    val ComponentInAssemblyLinemax = MainTree.builder("component_in_assembly_line8", "装配线进阶部件VIII", "Advanced components in assembly line VIII")
        .description("通过扭曲时空来驱动的永恒之马达", "The eternal motor driven by twisting space-time")
        .requirements(ResearchRequirements.Builder().setCWUNeeded(32000 * 20 * 4800L).setEurekaItem(GTItems.FIELD_GENERATOR_OpV, 0.7f).build())
        .icon(GTOItems.MAX_FIELD_GENERATOR)
        .prerequisites(ComponentInAssemblyLineopv)
        .tier(6)
        .build()

    @JvmField
    val ComponentInAssemblyLines: Array<TechNode?> = arrayOf(
        null, null, null, null, null, null,
        ComponentInAssemblyLineluv, ComponentInAssemblyLinezpm, ComponentInAssemblyLineuv, ComponentInAssemblyLineuhv, ComponentInAssemblyLineuev,
        ComponentInAssemblyLineuiv, ComponentInAssemblyLineuxv, ComponentInAssemblyLineopv, ComponentInAssemblyLinemax,
    )

    private val EnergyIOsTiers: IntArray = intArrayOf(0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 4, 5, 6)
    private val ComponentCasings: Array<BlockEntry<Block>?> = arrayOf(
        null,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_MV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_HV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_EV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_IV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UHV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UEV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UIV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_UXV,
        GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_OPV, GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_MAX,
    )
    private var lastComponentCasingNode: TechNode? = null
    private val eurekaProgresses: Array<Float> = arrayOf(
        0f,
        0f, 0f, 0f, 0f,
        0f, 1f, 1f, 0.95f,
        0.9f, 0.8f, 0.8f, 0.8f,
        0.7f, 0.6f,
    )

    fun ComponentCasing(tier: Int): TechNode {
        val req = ResearchRequirements.Builder().setCWUNeeded(1800L * (tier shl (tier)))
            .setEurekaItem(ComponentCasings[tier - 1], eurekaProgresses[tier] - 0.1f)
        if (tier >= ZPM) {
            req.addMaterialNeeded(ASSEMBLY, (64L shl (tier - ZPM)))
        }
        val node = MainTree.builder(
            "component_casing$tier",
            "装配线部件外壳${VN[tier]}",
            "Assembly Line Component Casing ${VN[tier]}",
        )
            .description(
                "精密而坚固的装配线部件外壳，为部件装配线提供${VN[tier]}级的部件装配条件",
                "Precision and sturdy assembly line component casing, providing ${VN[tier]} level component assembly conditions for the assembly line",
            )
            .requirements(
                req.build(),
            )
            .icon(ComponentCasings[tier])
            .tier(EnergyIOsTiers[tier])
        if (lastComponentCasingNode != null) {
            node.prerequisites(lastComponentCasingNode!!, ComponentInAssemblyLines[tier])
        } else {
            node.prerequisites(ComponentInAssemblyLines[tier])
        }
        lastComponentCasingNode = node.build()
        return lastComponentCasingNode!!
    }

    @JvmField
    val ComponentCasingsNodes: Array<TechNode?> = (ULV..MAX).map { if (it >= LuV) ComponentCasing(it) else null }.toTypedArray()

    private var lastEnergyIONode: TechNode? = null
    fun energyIONode(tier: Int): TechNode {
        val req = ResearchRequirements.Builder().setCWUNeeded(320L * (tier shl (tier)))
            .setEurekaItem(GTMachines.ENERGY_INPUT_HATCH[tier - 1], eurekaProgresses[tier])
        if (tier >= UV) {
            req.addMaterialNeeded(ENERGY, 256L * (1 shl (tier - UV)))
        }
        val node = MainTree.builder(
            "energy_io$tier",
            "高压能量输入输出${FormattingUtil.toRomanNumeral(tier - IV)}",
            "High Voltage Energy Input/Output${FormattingUtil.toRomanNumeral(tier - IV)}",
        )
            .description(
                "安全处理高达${FormattingUtil.formatNumbers(8L * (1L shl (tier * 2)))}EU/t的高压能量流",
                "Safely handle high-voltage energy flows up to ${FormattingUtil.formatNumbers(8L * (1L shl (tier * 2)))} EU/t",
            )
            .requirements(
                req.build(),
            )
            .icon(GTMachines.ENERGY_INPUT_HATCH[tier])
            .tier(EnergyIOsTiers[tier])
        if (lastEnergyIONode != null) {
            node.prerequisites(lastEnergyIONode!!)
        }
        lastEnergyIONode = node.build()
        return lastEnergyIONode!!
    }

    @JvmField
    val EnergyIOs: Array<TechNode?> = (ULV..MAX).map { if (it >= LuV) energyIONode(it) else null }.toTypedArray()
}
