package vazkii.botania.client.render.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.suried.gtorenders.geometry.SpriteQuads;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.common.block.block_entity.mana.LargeManaPoolBlockEntity;
import vazkii.botania.common.block.mana.LargeManaPoolBlock;

/**
 * 大型魔力池的魔力渲染：以核心为中心在 3×3 的开口处按储量绘制魔力水面，池底有覆盖提供者时绘制覆盖图标。
 */
public class LargeManaPoolBlockEntityRenderer<T extends LargeManaPoolBlockEntity> implements BlockEntityRenderer<T> {

    private final TextureAtlasSprite waterSprite;

    public LargeManaPoolBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.waterSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new net.minecraft.resources.ResourceLocation("botania", "block/mana_water"));
    }

    @Override
    public void render(@NotNull T pool, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        if (!pool.isCore() || pool.getLevel() == null) {
            return;
        }
        BlockPos core = pool.getBlockPos();
        if (pool.getLevel().getBlockState(core.below()).getBlock() instanceof PoolOverlayProvider provider) {
            TextureAtlasSprite icon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(provider.getIcon(pool.getLevel(), core));
            float alpha = (float) ((Math.sin((vazkii.botania.client.core.handler.ClientTickHandler.ticksInGame + partialTicks) / 20.0) + 1) * 0.3 + 0.2);
            renderLayer(pool, core, LargeManaPoolBlock.FLOOR / 16D + 0.0005, icon, alpha, ms, light, buffers);
        }
        float fill = pool.getFillFraction();
        if (fill <= 0) {
            return;
        }
        double bottom = LargeManaPoolBlock.FLOOR / 16D + 0.001;
        double top = 15 / 16D;
        renderLayer(pool, core, bottom + (top - bottom) * fill, waterSprite, 1, ms, light, buffers);
    }

    private static void renderLayer(LargeManaPoolBlockEntity pool, BlockPos core, double height, TextureAtlasSprite sprite,
                                    float alpha, PoseStack ms, int light, MultiBufferSource buffers) {
        int argb = (int) (alpha * 255) << 24 | 0xFFFFFF;
        float y = (float) (height * 16);
        VertexConsumer buffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState state = pool.getLevel().getBlockState(core.offset(dx, 0, dz));
                if (!(state.getBlock() instanceof LargeManaPoolBlock)) {
                    continue;
                }
                int rim = LargeManaPoolBlock.RIM;
                int x0 = state.getValue(LargeManaPoolBlock.WEST) ? 0 : rim;
                int x1 = state.getValue(LargeManaPoolBlock.EAST) ? 16 : 16 - rim;
                int z0 = state.getValue(LargeManaPoolBlock.NORTH) ? 0 : rim;
                int z1 = state.getValue(LargeManaPoolBlock.SOUTH) ? 16 : 16 - rim;
                SpriteQuads.face(buffer, ms.last().pose(), sprite, Direction.UP,
                        dx * 16 + x0, y, dz * 16 + z0, dx * 16 + x1, y, dz * 16 + z1, argb, light);
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull LargeManaPoolBlockEntity pool) {
        return true;
    }
}
