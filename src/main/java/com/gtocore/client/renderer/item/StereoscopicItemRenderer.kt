package com.gtocore.client.renderer.item

import com.gtocore.client.renderer.PosestackHelper

import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gtolib.utils.ClientUtil
import com.lowdragmc.lowdraglib.client.renderer.IRenderer
import com.mojang.blaze3d.vertex.PoseStack
import org.embeddedt.modernfix.render.RenderState

class StereoscopicItemRenderer : IRenderer {
    companion object {
        @JvmField
        val INSTANCE = StereoscopicItemRenderer()
    }

    @OnlyIn(Dist.CLIENT)
    override fun renderItem(stack: ItemStack, transformType: ItemDisplayContext, leftHand: Boolean, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int, model: BakedModel?) {
        poseStack.pushPose()
        if (transformType == ItemDisplayContext.GUI) {
            PosestackHelper.stereoTransformPosestack(poseStack, 0.3f, 0.5f, 0.2f, (System.currentTimeMillis() % 9000L).toFloat() / 25f)
        }
        RenderState.IS_RENDERING_LEVEL = true
        ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, ClientUtil.getVanillaModel(stack, null, null))
        RenderState.IS_RENDERING_LEVEL = false
        poseStack.popPose()
    }

    @OnlyIn(Dist.CLIENT)
    override fun useBlockLight(stack: ItemStack): Boolean = ClientUtil.getVanillaModel(stack, null, null).usesBlockLight()
}
