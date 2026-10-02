package com.gtocore.common.item

import com.gtocore.common.block.MufflerPipeBlock

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gregtechceu.gtceu.api.item.PipeBlockItem
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider
import com.lowdragmc.lowdraglib.client.renderer.IRenderer

import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
open class MufflerPipeBlockItem(block: MufflerPipeBlock, properties: Properties) :
    PipeBlockItem(block, properties),
    IItemRendererProvider {
    @OnlyIn(Dist.CLIENT)
    override fun getRenderer(stack: ItemStack): IRenderer? = getBlock().getRenderer(getBlock().defaultBlockState())

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, isAdvanced: TooltipFlag) {
        super.appendHoverText(stack, level, tooltip, isAdvanced)
        tooltip.add(Component.translatable("gtocore.tooltip.item.muffler_pipe.desc").withStyle(ChatFormatting.GRAY))
    }
}
