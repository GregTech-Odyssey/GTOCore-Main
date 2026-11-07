package vazkii.botania.client.render.block_entity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

/**
 * 魔力发射器渲染：在方块中心绘制按储量缩放的发光滑块，颜色按发射器变体。
 */
public class ManaSpreaderBlockEntityRenderer implements BlockEntityRenderer<ManaSpreaderBlockEntity> {

    private static final float MAX_MANA = 1000F;

    public ManaSpreaderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull ManaSpreaderBlockEntity spreader, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        float fill = Math.min(1F, spreader.getMana() / MAX_MANA);
        if (fill <= 0) {
            return;
        }
        float size = 0.08F + 0.14F * fill;
        int color = colorFor(spreader);
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;

        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
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
                consumer.vertex(matrix, corner[0], corner[1], corner[2]).color(r, g, b, 0.7F).endVertex();
            }
        }
        ms.popPose();
    }

    private static int colorFor(ManaSpreaderBlockEntity spreader) {
        var level = spreader.getLevel();
        if (level == null) {
            return 0x40E0D0;
        }
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(spreader.getBlockState().getBlock()).getPath();
        return switch (path) {
            case "redstone_spreader" -> 0xFF5050;
            case "elven_spreader" -> 0xE0FFE0;
            case "gaia_spreader" -> 0xFF80FF;
            default -> 0x40E0D0;
        };
    }
}
