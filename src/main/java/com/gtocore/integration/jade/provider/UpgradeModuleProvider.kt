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
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gtolib.GTOCore
import com.gtolib.api.machine.feature.IUpgradeMachine
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class UpgradeModuleProvider : CapabilityBlockProvider<IUpgradeMachine>(GTOCore.id("upgrade_module_provider")) {
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): IUpgradeMachine? = MetaMachine.getMachine(blockEntity) as? IUpgradeMachine

    protected override fun write(data: CompoundTag, capability: IUpgradeMachine?) {
        capability?.let {
            if (it.`gtolib$getSpeed`() != 1.0) data.putDouble("speed", it.`gtolib$getSpeed`())
            if (it.`gtolib$getEnergy`() != 1.0) data.putDouble("energy", it.`gtolib$getEnergy`())
        }
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val speed = capData.getDouble("speed")
        if (speed > 0) {
            tooltip.add(Component.translatable("item.gtocore.speed_upgrade_module").append(": x").append(FormattingUtil.formatNumbers(speed)))
        }
        val energy = capData.getDouble("energy")
        if (energy > 0) {
            tooltip.add(Component.translatable("item.gtocore.energy_upgrade_module").append(": x").append(FormattingUtil.formatNumbers(energy)))
        }
    }
}
