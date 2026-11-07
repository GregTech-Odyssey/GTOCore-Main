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

/** 多媒体索引物品渲染：复用方块实体渲染器的物品分支。 */
public class CorporeaIndexItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static CorporeaIndexItemRenderer instance;

    private CorporeaIndexItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static CorporeaIndexItemRenderer instance() {
        if (instance == null) {
            instance = new CorporeaIndexItemRenderer();
        }
        return instance;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, MultiBufferSource buffers,
                             int light, int overlay) {
        Block block = Block.byItem(stack.getItem());
        if (!(block instanceof EntityBlock entityBlock)) {
            return;
        }
        BlockEntity dummy = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (dummy == null) {
            return;
        }
        BlockEntityRenderer renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(dummy);
        if (renderer != null) {
            renderer.render(null, Minecraft.getInstance().getFrameTime(), ms, buffers, light, overlay);
        }
    }
}
