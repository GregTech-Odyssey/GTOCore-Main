package com.gtocore.client.renderer.item

import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gtolib.utils.ClientUtil
import com.lowdragmc.lowdraglib.client.renderer.IRenderer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import committee.nova.mods.renderblender.api.client.render.buffer.AlphaOverrideVertexConsumer

enum class TranslucentRenderer : IRenderer {
    INSTANCE,
    ;

    @OnlyIn(Dist.CLIENT)
    override fun renderItem(stack: ItemStack, transformType: ItemDisplayContext, leftHand: Boolean, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int, model: BakedModel?) {
        if (stack.getItem() !is BlockItem) {
            poseStack.pushPose()
            RenderSystem.enableBlend()
            RenderSystem.disableDepthTest()
            RenderSystem.defaultBlendFunc()
            RenderSystem.setShaderColor(1f, 1f, 1f, (Math.sin((System.currentTimeMillis() / 10f).toDouble()) * 0.5f + 0.5f).toFloat())
            poseStack.translate(-0.5f, -0.5f, -0.5f)
            val vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, ItemBlockRenderTypes.getRenderType(stack, true), true, stack.hasFoil())
            ClientUtil.getItemRenderer().renderModelLists(
                ClientUtil.getVanillaModel(stack, null, null),
                stack,
                combinedLight,
                combinedOverlay,
                poseStack,
                AlphaOverrideVertexConsumer(vertexConsumer, Math.sin(System.currentTimeMillis() * 0.0034) * 0.3 + 0.5),
            )
            RenderSystem.disableBlend()
            RenderSystem.enableDepthTest()
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            poseStack.popPose()
        } else {
            ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, ClientUtil.getVanillaModel(stack, null, null))
        }
    }
}
