package com.gtocore.integration.jade.provider

import net.minecraft.Util
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
import com.gtolib.api.capability.IManaContainer
import com.gtolib.api.machine.mana.feature.IManaMachine
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.BoxStyle

class ManaContainerBlockProvider : CapabilityBlockProvider<IManaContainer>(GTOCore.id("mana_container_provider")) {
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): IManaContainer? = (MetaMachine.getMachine(blockEntity) as? IManaMachine)?.manaContainer

    protected override fun write(data: CompoundTag, capability: IManaContainer) {
        data.putLong("Mana", capability.currentMana)
        data.putLong("MaxMana", capability.maxMana)
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val maxStorage = capData.getLong("MaxMana")
        if (maxStorage == 0L) return
        val stored = capData.getLong("Mana")
        val helper = tooltip.elementHelper
        val manaText = "${FormattingUtil.formatNumbers(stored)} / ${FormattingUtil.formatNumbers(maxStorage)} Mana"
        tooltip.add(
            helper.progress(
                getProgress(stored, maxStorage),
                Component.literal(manaText),
                helper.progressStyle().color(0xFF00B1FF.toInt(), 0xFF00B1FF.toInt()).textColor(-1),
                Util.make(BoxStyle.DEFAULT) { style -> style.borderColor = 0xFF888888.toInt() },
                true,
            ),
        )
    }
}
