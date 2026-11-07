package vazkii.botania.client.render.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import vazkii.botania.common.block.block_entity.TileReturnPortal;

/**
 * 返回门渲染：同样使用精灵门旋涡贴图，水平铺在门框内。
 */
public class RenderReturnPortal implements BlockEntityRenderer<TileReturnPortal> {

    private final TextureAtlasSprite sprite;

    public RenderReturnPortal(BlockEntityRendererProvider.Context context) {
        this.sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("botania", "block/alfheim_portal_swirl"));
    }

    @Override
    public void render(@NotNull TileReturnPortal portal, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        float time = (System.currentTimeMillis() % 100000L) / 50.0F + partialTicks;
        float alpha = (float) Math.min(1, (Math.sin(time / 8D) + 1) / 7D) + 0.7F;

        ms.pushPose();
        ms.mulPose(Axis.XP.rotationDegrees(90));
        ms.translate(0, 0, -0.5);
        ms.scale(1 / 3F, 1 / 3F, 1 / 3F);
        ms.translate(0, 0, -0.5);
        renderPortal(ms, buffers, 0, 0, 3, 3, alpha, overlay);
        ms.translate(3, 0, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderPortal(ms, buffers, 0, 0, 3, 3, alpha, overlay);
        ms.popPose();
    }

    private void renderPortal(PoseStack ms, MultiBufferSource buffers, int x, int y, int width, int height,
                              float alpha, int overlay) {
        VertexConsumer vertex = buffers.getBuffer(Sheets.translucentItemSheet());
        Matrix4f model = ms.last().pose();
        Matrix3f normal = ms.last().normal();
        vertex.vertex(model, x, y + height, 0).color(1, 1, 1, alpha).uv(sprite.getU0(), sprite.getV1())
                .overlayCoords(overlay).uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        vertex.vertex(model, x + width, y + height, 0).color(1, 1, 1, alpha).uv(sprite.getU1(), sprite.getV1())
                .overlayCoords(overlay).uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        vertex.vertex(model, x + width, y, 0).color(1, 1, 1, alpha).uv(sprite.getU1(), sprite.getV0())
                .overlayCoords(overlay).uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        vertex.vertex(model, x, y, 0).color(1, 1, 1, alpha).uv(sprite.getU0(), sprite.getV0()).overlayCoords(overlay)
                .uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
    }
}
