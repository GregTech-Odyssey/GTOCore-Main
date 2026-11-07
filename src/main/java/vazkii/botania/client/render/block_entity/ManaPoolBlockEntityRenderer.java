package vazkii.botania.client.render.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;

import java.util.Random;

/**
 * 魔力池的魔力渲染：按储量在池内抬升魔力水面，福鲁伊克斯池用彩虹色重绘方块模型，池底为覆盖提供者时绘制覆盖图标。
 */
public class ManaPoolBlockEntityRenderer<T extends ManaPoolBlockEntity> implements BlockEntityRenderer<T> {

    private final TextureAtlasSprite waterSprite;
    private final BlockRenderDispatcher blockRenderDispatcher;

    public ManaPoolBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blockRenderDispatcher = ctx.getBlockRenderDispatcher();
        this.waterSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("botania", "block/mana_water"));
    }

    @Override
    public void render(@NotNull T pool, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();

        ManaPoolBlock.Variant variant = pool.getBlockState().getBlock() instanceof ManaPoolBlock block ? block.variant : ManaPoolBlock.Variant.DEFAULT;
        boolean fab = variant == ManaPoolBlock.Variant.FABULOUS;
        boolean diluted = variant == ManaPoolBlock.Variant.DILUTED;
        boolean creative = variant == ManaPoolBlock.Variant.CREATIVE;

        int insideUVStart = diluted ? 1 : 2;
        int insideUVEnd = 16 - insideUVStart;
        float poolBottom = insideUVStart / 16F + 0.001F;
        float poolTop = (diluted ? 5 : creative ? 9 : 7) / 16F;

        if (fab) {
            float time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
            time += new Random(pool.getBlockPos().asLong()).nextInt(100000);
            time *= 0.005F;
            int poolColor = pool.getColor().map(DyeColor::getFireworkColor).orElse(-1);
            int color = Mth.hsvToRgb(Mth.frac(time), 0.6F, 1F);
            if (poolColor != -1) {
                color = multiplyColor(color, poolColor);
            }
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            BlockState state = pool.getBlockState();
            BakedModel model = blockRenderDispatcher.getBlockModel(state);
            VertexConsumer buffer = buffers.getBuffer(ItemBlockRenderTypes.getRenderType(state, false));
            blockRenderDispatcher.getModelRenderer().renderModel(ms.last(), buffer, state, model,
                    red / 255F, green / 255F, blue / 255F, light, overlay);
        }

        Block below = pool.getLevel().getBlockState(pool.getBlockPos().below()).getBlock();
        if (below instanceof PoolOverlayProvider overlayProvider) {
            ResourceLocation spriteId = overlayProvider.getIcon(pool.getLevel(), pool.getBlockPos());
            TextureAtlasSprite icon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(spriteId);
            float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + partialTicks) / 20.0) + 1) * 0.3 + 0.2);
            ms.pushPose();
            ms.translate(0, poolBottom, 0);
            ms.mulPose(Axis.XP.rotationDegrees(90F));
            VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
            renderIconCropped(ms, buffer, insideUVStart, insideUVStart, insideUVEnd, insideUVEnd, icon, 0xFFFFFF, alpha, light, overlay);
            ms.popPose();
        }

        long mana = pool.getCurrentManaLong();
        long maxMana = pool.getMaxManaLong();
        if (maxMana <= 0) {
            maxMana = ManaPoolBlockEntity.MAX_MANA;
        }
        float manaLevel = (float) ((double) mana / (double) maxMana);
        if (manaLevel > 0) {
            ms.pushPose();
            ms.translate(0, Mth.clampedMap(manaLevel, 0, 1, poolBottom, poolTop), 0);
            ms.mulPose(Axis.XP.rotationDegrees(90F));
            VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
            renderIconCropped(ms, buffer, insideUVStart, insideUVStart, insideUVEnd, insideUVEnd,
                    waterSprite, 0xFFFFFF, 1, light, overlay);
            ms.popPose();
        }

        ms.popPose();
    }

    /** 在 1/16 方块坐标里绘制裁剪过的图标，参数沿用原模组的像素单位。 */
    private static void renderIconCropped(PoseStack ms, VertexConsumer buffer, int uvStartX, int uvStartY, int uvEndX,
                                          int uvEndY, TextureAtlasSprite icon, int color, float alpha, int light,
                                          int overlay) {
        float startX = uvStartX / 16F;
        float startY = uvStartY / 16F;
        float endX = uvEndX / 16F;
        float endY = uvEndY / 16F;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int a = (int) (alpha * 255F);
        Matrix4f mat = ms.last().pose();
        buffer.vertex(mat, startX, endY, 0).color(red, green, blue, a)
                .uv(icon.getU(uvStartX), icon.getV(uvEndY)).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        buffer.vertex(mat, endX, endY, 0).color(red, green, blue, a)
                .uv(icon.getU(uvEndX), icon.getV(uvEndY)).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        buffer.vertex(mat, endX, startY, 0).color(red, green, blue, a)
                .uv(icon.getU(uvEndX), icon.getV(uvStartY)).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
        buffer.vertex(mat, startX, startY, 0).color(red, green, blue, a)
                .uv(icon.getU(uvStartX), icon.getV(uvStartY)).overlayCoords(overlay).uv2(light).normal(0, 0, 1).endVertex();
    }

    private static int multiplyColor(int color1, int color2) {
        int r = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 255;
        int g = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 255;
        int b = (color1 & 0xFF) * (color2 & 0xFF) / 255;
        return r << 16 | g << 8 | b;
    }
}
