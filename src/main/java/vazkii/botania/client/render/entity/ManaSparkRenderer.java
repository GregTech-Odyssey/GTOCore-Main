package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import vazkii.botania.common.entity.ManaSparkEntity;

/**
 * 火花渲染：绘制按类型着色的发光球。
 */
public class ManaSparkRenderer extends EntityRenderer<ManaSparkEntity> {

    public ManaSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull ManaSparkEntity entity) {
        return new ResourceLocation("botania", "textures/misc/white.png");
    }

    @Override
    public void render(@NotNull ManaSparkEntity entity, float yaw, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light) {
        int color = entity.getNetwork().getFireworkColor();
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;
        float size = 0.06F;

        ms.pushPose();
        Matrix4f matrix = ms.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        float[][] corners = {
                { -size, -size, -size }, { size, -size, -size }, { size, size, -size }, { -size, size, -size },
                { -size, -size, size }, { size, -size, size }, { size, size, size }, { -size, size, size }
        };
        int[][] faces = {
                { 0, 1, 2, 3 }, { 5, 4, 7, 6 }, { 4, 0, 3, 7 },
                { 1, 5, 6, 2 }, { 3, 2, 6, 7 }, { 4, 5, 1, 0 }
        };
        for (int[] face : faces) {
            for (int index : face) {
                float[] corner = corners[index];
                consumer.vertex(matrix, corner[0], corner[1], corner[2]).color(r, g, b, 0.9F).endVertex();
            }
        }
        ms.popPose();
    }
}
