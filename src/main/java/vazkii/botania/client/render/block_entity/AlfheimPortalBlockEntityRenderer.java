package vazkii.botania.client.render.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import vazkii.botania.common.block.ReplicaVisualBlocks;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;

import java.util.Objects;

/**
 * 精灵门门户渲染：在两片门面上绘制旋涡并随时间脉动，激活后逐渐显现。
 */
public class AlfheimPortalBlockEntityRenderer implements BlockEntityRenderer<AlfheimPortalBlockEntity> {

    private final TextureAtlasSprite portalSprite;

    public AlfheimPortalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.portalSprite = Objects.requireNonNull(
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(new net.minecraft.resources.ResourceLocation("botania", "block/alfheim_portal_swirl")));
    }

    @Override
    public void render(@NotNull AlfheimPortalBlockEntity portal, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        ReplicaVisualBlocks.PortalState state = portal.getBlockState()
                .getValue(ReplicaVisualBlocks.PORTAL_STATE);
        if (state == ReplicaVisualBlocks.PortalState.OFF) {
            return;
        }
        float time = (System.currentTimeMillis() % 100000L) / 50.0F + partialTicks;
        float alpha = (float) Math.min(1F, (Math.sin(time / 8D) + 1D) / 7D + 0.6D) * (Math.min(60, portal.ticksOpen) / 60F) * 0.5F;

        ms.pushPose();
        if (state == ReplicaVisualBlocks.PortalState.ON_X) {
            ms.translate(0.3125, 1, 2);
            ms.mulPose(Axis.YP.rotationDegrees(90));
        } else {
            ms.translate(-1, 1, 0.3125);
        }
        renderIcon(ms, buffers, portalSprite, 0, 0, 3, 3, alpha, overlay);
        ms.popPose();

        ms.pushPose();
        if (state == ReplicaVisualBlocks.PortalState.ON_X) {
            ms.translate(0.6875, 1, -1);
            ms.mulPose(Axis.YP.rotationDegrees(90));
        } else {
            ms.translate(2, 1, 0.6875);
        }
        ms.mulPose(Axis.YP.rotationDegrees(180));
        renderIcon(ms, buffers, portalSprite, 0, 0, 3, 3, alpha, overlay);
        ms.popPose();
    }

    private void renderIcon(PoseStack ms, MultiBufferSource buffers, TextureAtlasSprite icon, int x, int y, int width,
                            int height, float alpha, int overlay) {
        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentItemSheet());
        Matrix4f model = ms.last().pose();
        Matrix3f normal = ms.last().normal();
        buffer.vertex(model, x, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV1()).overlayCoords(overlay)
                .uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        buffer.vertex(model, x + width, y + height, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV1())
                .overlayCoords(overlay).uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        buffer.vertex(model, x + width, y, 0).color(1, 1, 1, alpha).uv(icon.getU1(), icon.getV0()).overlayCoords(overlay)
                .uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
        buffer.vertex(model, x, y, 0).color(1, 1, 1, alpha).uv(icon.getU0(), icon.getV0()).overlayCoords(overlay)
                .uv2(0xF000F0).normal(normal, 1, 0, 0).endVertex();
    }
}
