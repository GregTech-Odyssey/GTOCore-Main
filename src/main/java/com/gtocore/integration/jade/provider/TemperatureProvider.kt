package com.gtocore.integration.jade.provider

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gtolib.GTOCore
import com.gtolib.api.capability.IHeatContainer
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class TemperatureProvider : CapabilityBlockProvider<IHeatContainer>(GTOCore.id("temperature_provider")) {
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): IHeatContainer? = GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer::class.java, blockEntity, side)

    protected override fun write(data: CompoundTag, capability: IHeatContainer?) {
        capability?.let {
            data.putDouble("temperature", it.temperature)
            data.putLong("max_temperature", it.maxTemperature)
        }
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val maxTemperature = capData.getLong("max_temperature")
        if (maxTemperature == 0L) return
        val temperatureText = "${capData.getDouble("temperature").toLong()} / $maxTemperature"
        tooltip.add(Component.translatable("gtocore.machine.current_temperature", temperatureText))
    }
}
