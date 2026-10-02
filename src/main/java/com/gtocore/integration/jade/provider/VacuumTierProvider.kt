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
import com.gtolib.api.machine.feature.IVacuumMachine
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class VacuumTierProvider : CapabilityBlockProvider<IVacuumMachine>(GTOCore.id("vacuum_tier_provider")) {
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): IVacuumMachine? = MetaMachine.getMachine(blockEntity) as? IVacuumMachine

    protected override fun write(data: CompoundTag, capability: IVacuumMachine?) {
        capability?.let { data.putInt("vacuum_tier", it.vacuumTier) }
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val tier = capData.getInt("vacuum_tier")
        if (tier == 0) return
        tooltip.add(Component.translatable("gtocore.recipe.vacuum.tier", tier))
    }
}
