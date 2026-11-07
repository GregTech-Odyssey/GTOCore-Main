package vazkii.botania.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.ManaBurstEntity;

/**
 * 魔力弹渲染：朝相机方向绘制彩色发光十字，颜色取自弹体。
 */
public class ManaBurstRenderer extends EntityRenderer<ManaBurstEntity> {

    public ManaBurstRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull ManaBurstEntity entity) {
        return new ResourceLocation("botania", "textures/misc/white.png");
    }

    @Override
    public void render(@NotNull ManaBurstEntity entity, float yaw, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light) {
        int color = entity.getColor();
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        float size = 0.12F;

        ms.pushPose();
        var camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        ms.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        ms.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        var matrix = ms.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());

        quad(consumer, matrix, -size, -size, size, size, r, g, b);
        quad(consumer, matrix, -size, size, size, -size, r, g, b);
        ms.popPose();

        super.render(entity, yaw, partialTicks, ms, buffers, light);
    }

    private static void quad(VertexConsumer consumer, org.joml.Matrix4f matrix, float x1, float y1, float x2, float y2,
                             float r, float g, float b) {
        consumer.vertex(matrix, x1, y1, 0).color(r, g, b, 1F).endVertex();
        consumer.vertex(matrix, x2, y1, 0).color(r, g, b, 1F).endVertex();
        consumer.vertex(matrix, x2, y2, 0).color(r, g, b, 1F).endVertex();
        consumer.vertex(matrix, x1, y2, 0).color(r, g, b, 1F).endVertex();
    }
}
