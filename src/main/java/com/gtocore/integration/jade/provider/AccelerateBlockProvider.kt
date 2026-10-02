package com.gtocore.integration.jade.provider

import com.gtocore.common.item.TimeTwisterBehavior

import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gtolib.GTOCore
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.BoxStyle

class AccelerateBlockProvider : CapabilityBlockProvider<Int>(GTOCore.id("accelerate_provider")) {
    override fun appendServerData(data: CompoundTag, blockAccessor: BlockAccessor) {
        super.appendServerData(data, blockAccessor)
        TimeTwisterBehavior.appendWailaData(data, blockAccessor)
    }

    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): Int? {
        if (blockEntity != null && blockEntity.getPersistentData().contains("accelerate_tick")) {
            return blockEntity.getPersistentData().getInt("accelerate_tick")
        }
        return null
    }

    protected override fun write(data: CompoundTag, capability: Int?) {
        capability?.let { data.putInt("accelerate_tick", it) }
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor, blockEntity: BlockEntity?, config: IPluginConfig) {
        val tick = capData.getInt("accelerate_tick")
        if (tick == 0) {
            TimeTwisterBehavior.appendWailaTooltip(block.serverData, tooltip, block, config)
            return
        }
        val helper = tooltip.elementHelper
        tooltip.add(
            helper.progress(
                getProgress(tick.toLong(), 100L),
                Component.literal("$tick / 100 Tick"),
                helper.progressStyle().color(0xFFFFFFFF.toInt(), 0xFFADD8E6.toInt()).textColor(-1),
                Util.make(BoxStyle.DEFAULT) { style -> style.borderColor = 0xFF888888.toInt() },
                true,
            ),
        )
    }
}
