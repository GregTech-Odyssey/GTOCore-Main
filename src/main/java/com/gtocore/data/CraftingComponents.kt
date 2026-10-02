package com.gtocore.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.data.recipe.CraftingComponent
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*
import com.gto.registrate.util.entry.BlockEntry
import com.gto.registrate.util.entry.ItemEntry
import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOItems
import com.gtocore.common.data.GTOMaterials

object CraftingComponents {
    @JvmField
    var BUFFER: CraftingComponent? = null

    @JvmField
    var FLUID_REGULATOR: CraftingComponent? = null

    @JvmField
    var INTEGRATED_CONTROL_CORE: CraftingComponent? = null

    @JvmName("add$1")
    private fun CraftingComponent.add(pair: Pair<Int, ItemEntry<*>>): CraftingComponent {
        return add(pair.first, pair.second.asItem())
    }

    @JvmName("add$2")
    private fun CraftingComponent.add(pair: Pair<Int, BlockEntry<*>>): CraftingComponent {
        return add(pair.first, pair.second.asItem())
    }

    @JvmName("add$3")
    private fun CraftingComponent.add(pair: Pair<Int, MachineDefinition>): CraftingComponent {
        return add(pair.first, pair.second.asItem())
    }

    @JvmName("add$4")
    private fun CraftingComponent.add(pair: Pair<Int, MaterialEntry>): CraftingComponent {
        return add(pair.first, pair.second)
    }

    @JvmStatic
    fun init() {
        GTCraftingComponents.init()

        PUMP.add(14 to GTOItems.MAX_ELECTRIC_PUMP)
        CONVEYOR.add(14 to GTOItems.MAX_CONVEYOR_MODULE)
        MOTOR.add(14 to GTOItems.MAX_ELECTRIC_MOTOR)
        PISTON.add(14 to GTOItems.MAX_ELECTRIC_PISTON)
        EMITTER.add(14 to GTOItems.MAX_EMITTER)
        SENSOR.add(14 to GTOItems.MAX_SENSOR)
        FIELD_GENERATOR.add(14 to GTOItems.MAX_FIELD_GENERATOR)
        ROBOT_ARM.add(14 to GTOItems.MAX_ROBOT_ARM)

        GRINDER.apply {
            add(5 to GTItems.COMPONENT_GRINDER_DIAMOND)
            add(6 to GTItems.COMPONENT_GRINDER_DIAMOND)
            add(7 to GTItems.COMPONENT_GRINDER_TUNGSTEN)
            add(8 to GTItems.COMPONENT_GRINDER_TUNGSTEN)
            add(9 to GTItems.COMPONENT_GRINDER_TUNGSTEN)
            add(10 to GTItems.COMPONENT_GRINDER_TUNGSTEN)
            add(11 to GTItems.COMPONENT_GRINDER_TUNGSTEN)
            add(12 to GTOItems.BEDROCK_DRILL)
            add(13 to GTOItems.BEDROCK_DRILL)
        }

        SAWBLADE.apply {
            add(3 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.VanadiumSteel))
            add(4 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.TungstenSteel))
            add(5 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.TungstenSteel))
            add(6 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.HSSE))
            add(7 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.HSSE))
            add(8 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.NaquadahAlloy))
            add(9 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.NaquadahAlloy))
            add(10 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium))
            add(11 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Duranium))
            add(12 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Neutronium))
            add(13 to MaterialEntry(TagPrefix.toolHeadBuzzSaw, GTMaterials.Neutronium))
        }

        WIRE_ELECTRIC.apply {
            add(10 to MaterialEntry(TagPrefix.wireGtSingle, GTMaterials.Mendelevium))
            add(11 to MaterialEntry(TagPrefix.wireGtSingle, GTMaterials.Mendelevium))
            add(12 to MaterialEntry(TagPrefix.wireGtSingle, GTMaterials.Mendelevium))
            add(13 to MaterialEntry(TagPrefix.wireGtSingle, GTOMaterials.Uruium))
            add(14 to MaterialEntry(TagPrefix.wireGtSingle, GTOMaterials.Uruium))
        }

        listOf(
            WIRE_QUAD to TagPrefix.wireGtQuadruple,
            WIRE_OCT to TagPrefix.wireGtOctal,
            WIRE_HEX to TagPrefix.wireGtHex,
            CABLE to TagPrefix.cableGtSingle,
            CABLE_DOUBLE to TagPrefix.cableGtDouble,
            CABLE_QUAD to TagPrefix.cableGtDouble,
            WIRE_ELECTRIC to TagPrefix.cableGtQuadruple,
            CABLE_OCT to TagPrefix.cableGtOctal,
            CABLE_HEX to TagPrefix.cableGtHex,
        ).forEach {
            it.first.apply {
                add(10 to MaterialEntry(it.second, GTOMaterials.Mithril))
                add(11 to MaterialEntry(it.second, GTMaterials.Neutronium))
                add(12 to MaterialEntry(it.second, GTOMaterials.Taranium))
                add(13 to MaterialEntry(it.second, GTOMaterials.CrystalMatrix))
                add(14 to MaterialEntry(it.second, GTOMaterials.CosmicNeutronium))
            }
        }

        listOf(
            CABLE_TIER_UP to (TagPrefix.cableGtSingle to TagPrefix.wireGtSingle),
            CABLE_TIER_UP_DOUBLE to (TagPrefix.cableGtDouble to TagPrefix.wireGtDouble),
            CABLE_TIER_UP_QUAD to (TagPrefix.cableGtQuadruple to TagPrefix.wireGtQuadruple),
            CABLE_TIER_UP_OCT to (TagPrefix.cableGtOctal to TagPrefix.wireGtOctal),
            CABLE_TIER_UP_HEX to (TagPrefix.cableGtHex to TagPrefix.wireGtHex),
        ).forEach {
            it.first.apply {
                add(9 to MaterialEntry(it.second.first, GTOMaterials.Mithril))
                add(10 to MaterialEntry(it.second.first, GTMaterials.Neutronium))
                add(11 to MaterialEntry(it.second.first, GTOMaterials.Taranium))
                add(12 to MaterialEntry(it.second.first, GTOMaterials.CrystalMatrix))
                add(13 to MaterialEntry(it.second.first, GTOMaterials.CosmicNeutronium))
                add(14 to MaterialEntry(it.second.second, GTOMaterials.SpaceTime))
            }
        }

        listOf(
            PIPE_NORMAL to TagPrefix.pipeNormalFluid,
            PIPE_LARGE to TagPrefix.pipeLargeFluid,
            PIPE_NONUPLE to TagPrefix.pipeNonupleFluid,
        ).forEach {
            it.first.apply {
                add(9 to MaterialEntry(it.second, GTMaterials.Neutronium))
                add(10 to MaterialEntry(it.second, GTMaterials.Neutronium))
                add(11 to MaterialEntry(it.second, GTOMaterials.Enderium))
                add(12 to MaterialEntry(it.second, GTOMaterials.Enderium))
                add(13 to MaterialEntry(it.second, GTOMaterials.HeavyQuarkDegenerateMatter))
                add(14 to MaterialEntry(it.second, GTOMaterials.HeavyQuarkDegenerateMatter))
            }
        }

        PIPE_LARGE.apply {
            add(7 to MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Iridium))
        }

        PIPE_NONUPLE.apply {
            add(0 to MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Bronze))
            add(1 to MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Bronze))
            add(2 to MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Steel))
            add(3 to MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.StainlessSteel))
        }

        GLASS.apply {
            add(GTValues.UHV to GTBlocks.FUSION_GLASS)
            add(GTValues.UEV to GTBlocks.FUSION_GLASS)
            add(GTValues.UIV to GTOBlocks.FORCE_FIELD_GLASS)
            add(GTValues.UXV to GTOBlocks.FORCE_FIELD_GLASS)
            add(GTValues.OpV to GTOBlocks.FORCE_FIELD_GLASS)
            add(GTValues.MAX to GTOBlocks.FORCE_FIELD_GLASS)
        }

        PLATE.apply {
            add(10 to MaterialEntry(TagPrefix.plate, GTOMaterials.Quantanium))
            add(11 to MaterialEntry(TagPrefix.plate, GTOMaterials.Adamantium))
            add(12 to MaterialEntry(TagPrefix.plate, GTOMaterials.Vibranium))
            add(13 to MaterialEntry(TagPrefix.plate, GTOMaterials.Draconium))
            add(14 to MaterialEntry(TagPrefix.plate, GTOMaterials.ChaosInfinityAlloy))
        }

        HULL_PLATE.apply {
            add(0 to MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron))
            add(1 to MaterialEntry(TagPrefix.plate, GTMaterials.Polyethylene))
            add(2 to MaterialEntry(TagPrefix.plate, GTMaterials.Polyethylene))
            add(3 to MaterialEntry(TagPrefix.plate, GTMaterials.PolyvinylChloride))
            add(4 to MaterialEntry(TagPrefix.plate, GTMaterials.PolyvinylChloride))
            add(5 to MaterialEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene))
            add(6 to MaterialEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene))
            add(7 to MaterialEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole))
            add(8 to MaterialEntry(TagPrefix.plate, GTMaterials.Polybenzimidazole))
            add(9 to MaterialEntry(TagPrefix.plate, GTOMaterials.Polyetheretherketone))
            add(10 to MaterialEntry(TagPrefix.plate, GTOMaterials.Polyetheretherketone))
            add(11 to MaterialEntry(TagPrefix.plate, GTOMaterials.Zylon))
            add(12 to MaterialEntry(TagPrefix.plate, GTOMaterials.Zylon))
            add(13 to MaterialEntry(TagPrefix.plate, GTOMaterials.FullerenePolymerMatrixPulp))
            add(14 to MaterialEntry(TagPrefix.plate, GTOMaterials.Radox))
        }

        ROTOR.apply {
            add(9 to MaterialEntry(TagPrefix.rotor, GTMaterials.Neutronium))
            add(10 to MaterialEntry(TagPrefix.rotor, GTOMaterials.Quantanium))
            add(11 to MaterialEntry(TagPrefix.rotor, GTOMaterials.Adamantium))
            add(12 to MaterialEntry(TagPrefix.rotor, GTOMaterials.Vibranium))
            add(13 to MaterialEntry(TagPrefix.rotor, GTOMaterials.Draconium))
            add(14 to MaterialEntry(TagPrefix.rotor, GTOMaterials.TranscendentMetal))
        }

        COIL_HEATING.apply {
            add(9 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.AbyssalAlloy))
            add(10 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.TitanSteel))
            add(11 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.Adamantine))
            add(12 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.NaquadriaticTaranium))
            add(13 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.Starmetal))
            add(14 to MaterialEntry(TagPrefix.wireGtDouble, GTOMaterials.Hypogen))
        }

        COIL_HEATING_DOUBLE.apply {
            add(9 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.AbyssalAlloy))
            add(10 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.TitanSteel))
            add(11 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.Adamantine))
            add(12 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.NaquadriaticTaranium))
            add(13 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.Starmetal))
            add(14 to MaterialEntry(TagPrefix.wireGtQuadruple, GTOMaterials.Hypogen))
        }

        COIL_ELECTRIC.apply {
            add(9 to MaterialEntry(TagPrefix.wireGtOctal, GTOMaterials.Mithril))
            add(10 to MaterialEntry(TagPrefix.wireGtOctal, GTOMaterials.Mithril))
            add(11 to MaterialEntry(TagPrefix.wireGtHex, GTOMaterials.Mithril))
            add(12 to MaterialEntry(TagPrefix.wireGtHex, GTOMaterials.Mithril))
            add(13 to MaterialEntry(TagPrefix.wireGtOctal, GTOMaterials.CrystalMatrix))
            add(14 to MaterialEntry(TagPrefix.wireGtHex, GTOMaterials.CrystalMatrix))
        }

        ROD_DISTILLATION.apply {
            add(9 to MaterialEntry(TagPrefix.spring, GTMaterials.Europium))
            add(10 to MaterialEntry(TagPrefix.spring, GTOMaterials.Mithril))
            add(11 to MaterialEntry(TagPrefix.spring, GTMaterials.Neutronium))
            add(12 to MaterialEntry(TagPrefix.spring, GTOMaterials.Taranium))
            add(13 to MaterialEntry(TagPrefix.spring, GTOMaterials.CrystalMatrix))
            add(14 to MaterialEntry(TagPrefix.spring, GTOMaterials.CosmicNeutronium))
        }

        ROD_ELECTROMAGNETIC.apply {
            add(5 to MaterialEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
            add(6 to MaterialEntry(TagPrefix.rod, GTMaterials.VanadiumGallium))
            add(7 to MaterialEntry(TagPrefix.rod, GTMaterials.NiobiumTitanium))
            add(8 to MaterialEntry(TagPrefix.rod, GTMaterials.NiobiumTitanium))
            add(9 to MaterialEntry(TagPrefix.rod, GTOMaterials.EnergeticNetherite))
            add(10 to MaterialEntry(TagPrefix.rod, GTOMaterials.EnergeticNetherite))
            add(11 to MaterialEntry(TagPrefix.rod, GTOMaterials.Mithril))
            add(12 to MaterialEntry(TagPrefix.rod, GTOMaterials.Mithril))
            add(13 to MaterialEntry(TagPrefix.rod, GTOMaterials.Echoite))
            add(14 to MaterialEntry(TagPrefix.rod, GTOMaterials.Echoite))
        }

        PIPE_REACTOR.apply {
            add(9 to MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polybenzimidazole))
            add(10 to MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polybenzimidazole))
            add(11 to MaterialEntry(TagPrefix.pipeHugeFluid, GTMaterials.Polybenzimidazole))
            add(12 to MaterialEntry(TagPrefix.pipeNormalFluid, GTOMaterials.FullerenePolymerMatrixPulp))
            add(13 to MaterialEntry(TagPrefix.pipeLargeFluid, GTOMaterials.FullerenePolymerMatrixPulp))
            add(14 to MaterialEntry(TagPrefix.pipeHugeFluid, GTOMaterials.FullerenePolymerMatrixPulp))
        }

        POWER_COMPONENT.apply {
            add(10 to GTOItems.NM_CHIP)
            add(11 to GTOItems.PM_CHIP)
            add(12 to GTOItems.PM_CHIP)
            add(13 to GTOItems.FM_CHIP)
            add(14 to GTOItems.FM_CHIP)
        }

        VOLTAGE_COIL.apply {
            add(9 to GTOItems.UHV_VOLTAGE_COIL)
            add(10 to GTOItems.UEV_VOLTAGE_COIL)
            add(11 to GTOItems.UIV_VOLTAGE_COIL)
            add(12 to GTOItems.UXV_VOLTAGE_COIL)
            add(13 to GTOItems.OPV_VOLTAGE_COIL)
            add(14 to GTOItems.MAX_VOLTAGE_COIL)
        }

        SPRING.apply {
            add(10 to MaterialEntry(TagPrefix.spring, GTOMaterials.Mithril))
            add(11 to MaterialEntry(TagPrefix.spring, GTMaterials.Neutronium))
            add(12 to MaterialEntry(TagPrefix.spring, GTOMaterials.Taranium))
            add(13 to MaterialEntry(TagPrefix.spring, GTOMaterials.CrystalMatrix))
            add(14 to MaterialEntry(TagPrefix.spring, GTOMaterials.CosmicNeutronium))
        }

        CRATE.apply {
            add(9 to GTMachines.SUPER_CHEST[2])
            add(10 to GTMachines.SUPER_CHEST[3])
            add(11 to GTMachines.SUPER_CHEST[4])
            add(12 to GTMachines.QUANTUM_CHEST[5])
            add(13 to GTMachines.QUANTUM_CHEST[6])
            add(14 to GTMachines.QUANTUM_CHEST[7])
        }

        DRUM.apply {
            add(9 to GTMachines.SUPER_TANK[2])
            add(10 to GTMachines.SUPER_TANK[3])
            add(11 to GTMachines.SUPER_TANK[4])
            add(12 to GTMachines.QUANTUM_TANK[5])
            add(13 to GTMachines.QUANTUM_TANK[6])
            add(14 to GTMachines.QUANTUM_TANK[7])
        }

        FRAME.apply {
            add(9 to MaterialEntry(TagPrefix.frameGt, GTMaterials.Tritanium))
            add(10 to MaterialEntry(TagPrefix.frameGt, GTMaterials.Neutronium))
            add(11 to MaterialEntry(TagPrefix.frameGt, GTOMaterials.Quantanium))
            add(12 to MaterialEntry(TagPrefix.frameGt, GTOMaterials.Adamantium))
            add(13 to MaterialEntry(TagPrefix.frameGt, GTOMaterials.Draconium))
            add(14 to MaterialEntry(TagPrefix.frameGt, GTOMaterials.Infinity))
        }

        BUFFER = CraftingComponent.of(GTMachines.BUFFER[1].asItem()).apply {
            add(1 to GTMachines.BUFFER[1])
            add(2 to GTMachines.BUFFER[2])
            add(3 to GTMachines.BUFFER[3])
            add(4 to GTMachines.BUFFER[4])
            add(5 to GTMachines.BUFFER[5])
            add(6 to GTMachines.BUFFER[6])
            add(7 to GTMachines.BUFFER[7])
            add(8 to GTMachines.BUFFER[8])
            add(9 to GTMachines.BUFFER[9])
            add(10 to GTMachines.BUFFER[10])
            add(11 to GTMachines.BUFFER[11])
            add(12 to GTMachines.BUFFER[12])
            add(13 to GTMachines.BUFFER[13])
            add(14 to GTMachines.BUFFER[14])
        }

        FLUID_REGULATOR = CraftingComponent.of(GTItems.FLUID_REGULATOR_LV).apply {
            add(1 to GTItems.FLUID_REGULATOR_LV)
            add(2 to GTItems.FLUID_REGULATOR_MV)
            add(3 to GTItems.FLUID_REGULATOR_HV)
            add(4 to GTItems.FLUID_REGULATOR_EV)
            add(5 to GTItems.FLUID_REGULATOR_IV)
            add(6 to GTItems.FLUID_REGULATOR_LuV)
            add(7 to GTItems.FLUID_REGULATOR_ZPM)
            add(8 to GTItems.FLUID_REGULATOR_UV)
            add(9 to GTItems.FLUID_REGULATOR_UHV)
            add(10 to GTItems.FLUID_REGULATOR_UEV)
            add(11 to GTItems.FLUID_REGULATOR_UIV)
            add(12 to GTItems.FLUID_REGULATOR_UXV)
            add(13 to GTItems.FLUID_REGULATOR_OpV)
        }

        INTEGRATED_CONTROL_CORE = CraftingComponent.of(GTOItems.INTEGRATED_CONTROL_CORE_UV).apply {
            add(8 to GTOItems.INTEGRATED_CONTROL_CORE_UV)
            add(9 to GTOItems.INTEGRATED_CONTROL_CORE_UHV)
            add(10 to GTOItems.INTEGRATED_CONTROL_CORE_UEV)
            add(11 to GTOItems.INTEGRATED_CONTROL_CORE_UIV)
            add(12 to GTOItems.INTEGRATED_CONTROL_CORE_UXV)
            add(13 to GTOItems.INTEGRATED_CONTROL_CORE_OpV)
            add(14 to GTOItems.INTEGRATED_CONTROL_CORE_MAX)
        }
    }
}
