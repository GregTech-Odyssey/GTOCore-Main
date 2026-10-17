package com.gtocore.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import earth.terrarium.adastra.client.ClientPlatformUtils;
import org.joml.Matrix4f;

public final class SolarGlobeRenderer {

    private static final ResourceLocation MODEL = new ResourceLocation("ad_astra", "block/solar_surface_globe_cube");
    private static final float GLOW_RADIUS = 0.95F;

    private SolarGlobeRenderer() {}

    public static void render(BlockState state, float rotation, PoseStack poseStack, MultiBufferSource buffer, int overlay) {
        var minecraft = Minecraft.getInstance();
        var model = ClientPlatformUtils.getModel(minecraft.getModelManager(), MODEL);
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.translate(-0.5, 0, -0.5);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(GTORenderTypes.SOLAR_GLOBE),
                state, model, 1, 1, 1, LightTexture.FULL_BRIGHT, overlay);
        poseStack.popPose();
        renderGlow(poseStack.last().pose(), buffer);
        // Keep both draws in this pass, before the caller changes projection or render target.
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(GTORenderTypes.SOLAR_GLOBE);
            source.endBatch(GTORenderTypes.SOLAR_GLOBE_GLOW);
        }
    }

    private static void renderGlow(Matrix4f pose, MultiBufferSource buffer) {
        float scaleX = pose.m00() * pose.m00() + pose.m01() * pose.m01() + pose.m02() * pose.m02();
        float scaleY = pose.m10() * pose.m10() + pose.m11() * pose.m11() + pose.m12() * pose.m12();
        float scaleZ = pose.m20() * pose.m20() + pose.m21() * pose.m21() + pose.m22() * pose.m22();
        float radius = GLOW_RADIUS * (float) Math.sqrt(Math.max(scaleX, Math.max(scaleY, scaleZ)));
        var vertices = buffer.getBuffer(GTORenderTypes.SOLAR_GLOBE_GLOW);
        // Each vertex carries the same center; UV stores the camera-facing offset in world units.
        vertices.vertex(pose, 0.5F, 0.75F, 0.5F).uv(-radius, -radius).endVertex();
        vertices.vertex(pose, 0.5F, 0.75F, 0.5F).uv(radius, -radius).endVertex();
        vertices.vertex(pose, 0.5F, 0.75F, 0.5F).uv(radius, radius).endVertex();
        vertices.vertex(pose, 0.5F, 0.75F, 0.5F).uv(-radius, radius).endVertex();
    }
}
