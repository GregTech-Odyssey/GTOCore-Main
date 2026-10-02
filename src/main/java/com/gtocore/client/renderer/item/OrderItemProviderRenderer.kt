package com.gtocore.client.renderer.item

import com.gtocore.common.item.OrderItem

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gtolib.utils.ClientUtil
import com.lowdragmc.lowdraglib.client.renderer.IRenderer
import com.mojang.blaze3d.vertex.PoseStack

open class OrderItemProviderRenderer : IRenderer {
    companion object {
        @JvmField
        val INSTANCE = OrderItemProviderRenderer()
    }

    @OnlyIn(Dist.CLIENT)
    override fun renderItem(stack: ItemStack, transformType: ItemDisplayContext, leftHand: Boolean, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int, model: BakedModel?) {
        val marked = OrderItem.getTarget(stack)
        poseStack.pushPose()
        val vanilla = ClientUtil.getVanillaModel(stack, null, null)
        if (marked.isEmpty) {
            ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, vanilla)
        } else if (transformType == ItemDisplayContext.GUI) {
            val mc = Minecraft.getInstance()
            val bakedModel = mc.getItemRenderer().getModel(marked, mc.level, mc.player, 0)
            mc.getItemRenderer().render(marked, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedModel)
            poseStack.translate(-0.15, -0.15, 1.0)
            poseStack.scale(0.5f, 0.5f, 1f)
            ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, vanilla)
        }
        poseStack.popPose()
    }
}
