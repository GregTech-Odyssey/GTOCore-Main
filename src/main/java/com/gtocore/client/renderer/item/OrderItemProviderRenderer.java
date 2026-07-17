package com.gtocore.client.renderer.item;

import com.gtocore.common.item.OrderItem;

import com.gtolib.utils.ClientUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

public class OrderItemProviderRenderer implements IRenderer {

    public static final OrderItemProviderRenderer INSTANCE = new OrderItemProviderRenderer();

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        ItemStack marked = OrderItem.getTarget(stack);
        poseStack.pushPose();
        var vanilla = ClientUtil.getVanillaModel(stack, null, null);
        if (marked.isEmpty()) {
            ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, vanilla);
        } else if (transformType == ItemDisplayContext.GUI) {
            Minecraft mc = Minecraft.getInstance();
            BakedModel bakedModel = mc.getItemRenderer().getModel(marked, mc.level, mc.player, 0);
            mc.getItemRenderer().render(marked, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedModel);
            poseStack.translate(-0.15, -0.15, 1);
            poseStack.scale(0.5f, 0.5f, 1);

            ClientUtil.vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, vanilla);
        }
        poseStack.popPose();
    }
}
