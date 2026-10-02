package com.gtocore.integration.jade.provider

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gtolib.GTOCore
import com.gtolib.api.capability.IIWirelessInteractor
import com.gtolib.api.machine.feature.IAirScrubberInteractor
import com.gtolib.api.machine.mana.feature.IManaMachine
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class WirelessInteractorMachineProvider : CapabilityBlockProvider<MetaMachine>(GTOCore.id("wireless_interactor_provider")) {
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): MetaMachine? {
        val machine = MetaMachine.getMachine(blockEntity)
        return if (machine is IManaMachine || machine is IIWirelessInteractor<*> || machine is IAirScrubberInteractor) machine else null
    }

    protected override fun write(data: CompoundTag, capability: MetaMachine?) {
        when (capability) {
            is IManaMachine -> capability.manaContainer.netMachine?.let { writeMachine(data, "pos", it) }

            is IIWirelessInteractor<*> -> {
                (capability.netMachine as MetaMachine?)?.let { writeMachine(data, "pos", it) }
                if (capability is IAirScrubberInteractor) {
                    capability.airScrubberMachine?.let { writeMachine(data, "pos_a", it) }
                }
            }
        }
    }

    private fun writeMachine(data: CompoundTag, key: String, machine: MetaMachine) {
        data.putString(key, Component.Serializer.toJson(Component.translatable(machine.getDefinition().getDescriptionId()).append("[").append(machine.getPos().toShortString()).append("]")))
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val pos = capData.getString("pos")
        if (pos.isNotEmpty()) {
            tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", Component.Serializer.fromJson(pos)))
        }
        val airScrubberPos = capData.getString("pos_a")
        if (airScrubberPos.isNotEmpty()) {
            tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", Component.Serializer.fromJson(airScrubberPos)))
        }
    }
}
