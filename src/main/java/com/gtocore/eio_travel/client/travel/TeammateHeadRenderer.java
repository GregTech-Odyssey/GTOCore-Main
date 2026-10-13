package com.gtocore.eio_travel.client.travel;

import com.gtocore.eio_travel.logic.TravelHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

public final class TeammateHeadRenderer {

    @Nullable
    private static SkullModel model;

    private TeammateHeadRenderer() {}

    public static void render(AbstractClientPlayer teammate, PoseStack poseStack, float partialTick, double distanceSquared, boolean active) {
        Minecraft minecraft = Minecraft.getInstance();
        if (model == null) {
            model = new SkullModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_HEAD));
        }
        double size = TravelHandler.teammateHeadSize(Math.sqrt(distanceSquared));
        double centerY = TravelHandler.teammateHeadCenterY(teammate.getBbHeight(), size);
        float scale = (float) (active ? size * 2.6 : size * 2);
        Vec3 pos = teammate.getPosition(partialTick);

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y + centerY, pos.z);
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -0.25, 0);
        poseStack.scale(-1, -1, 1);
        model.setupAnim(0, 0, 0);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(teammate.getSkinTextureLocation())),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poseStack.popPose();
        buffer.endBatch();
    }
}
