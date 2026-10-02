package com.gtocore.integration.jade.provider

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.LiquidBlock

import com.gtolib.GTOCore
import com.gtolib.utils.NumberUtils
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

open class DestroyTimeProvider : IBlockComponentProvider {
    override fun appendTooltip(iTooltip: ITooltip, blockAccessor: BlockAccessor, iPluginConfig: IPluginConfig) {
        if (blockAccessor.block is LiquidBlock) return
        iTooltip.add(
            Component.translatable(
                "behavior.portable_scanner.block_hardness",
                NumberUtils.numberText(blockAccessor.block.defaultDestroyTime().toDouble()).withStyle(ChatFormatting.BLUE),
                NumberUtils.numberText(blockAccessor.block.explosionResistance.toDouble()).withStyle(ChatFormatting.DARK_BLUE),
            ).withStyle(ChatFormatting.GRAY),
        )
    }

    override fun getUid(): ResourceLocation = GTOCore.id("destroy_time_provider")

    override fun enabledByDefault(): Boolean = false
}
