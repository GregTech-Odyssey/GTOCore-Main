package com.gtocore.common.item

import com.gtocore.common.block.HeatPipeBlock

import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gregtechceu.gtceu.api.item.PipeBlockItem
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider
import com.lowdragmc.lowdraglib.client.renderer.IRenderer

import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
open class HeatPipeBlockItem(block: HeatPipeBlock, properties: Properties) :
    PipeBlockItem(block, properties),
    IItemRendererProvider {
    @OnlyIn(Dist.CLIENT)
    override fun getRenderer(stack: ItemStack): IRenderer? = getBlock().getRenderer(getBlock().defaultBlockState())
}
