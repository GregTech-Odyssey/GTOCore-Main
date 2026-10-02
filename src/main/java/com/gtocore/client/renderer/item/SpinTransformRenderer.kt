package com.gtocore.client.renderer.item

import com.gtocore.client.renderer.PosestackHelper

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

enum class SpinTransformRenderer : IRenderer {
    INSTANCE,
    ;

    @OnlyIn(Dist.CLIENT)
    override fun renderItem(stack: ItemStack, transformType: ItemDisplayContext, leftHand: Boolean, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int, model: BakedModel?) {
        poseStack.pushPose()
        if (transformType == ItemDisplayContext.GUI) {
            PosestackHelper.spinningTransformPosestack(
                poseStack,
                1f,
                Math.max(0f, Math.sin(System.currentTimeMillis() / 6000.0).toFloat()),
                20,
                Minecraft.getInstance().level!!.getGameTime(),
                Minecraft.getInstance().getPartialTick(),
            )
        }
        ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, ClientUtil.getVanillaModel(stack, null, null))
        poseStack.popPose()
    }
}
