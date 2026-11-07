package vazkii.botania.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.common.block.PylonBlock;

/**
 * 水晶物品渲染：交给方块实体渲染器绘制，与原模组一致地显示旋转、浮动与辉光。
 */
public class PylonItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static PylonItemRenderer instance;

    private PylonItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static PylonItemRenderer instance() {
        if (instance == null) {
            instance = new PylonItemRenderer();
        }
        return instance;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffers,
                             int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        if (!(block instanceof PylonBlock pylonBlock) || !(block instanceof EntityBlock entityBlock)) {
            return;
        }
        BlockEntity dummy = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (dummy == null) {
            return;
        }
        PylonBlockEntityRenderer.forceVariant = pylonBlock.variant;
        PylonBlockEntityRenderer.forceTransform = context;
        BlockEntityRenderer renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(dummy);
        if (renderer != null) {
            renderer.render(null, Minecraft.getInstance().getFrameTime(), ms, buffers, light, overlay);
        }
    }
}
