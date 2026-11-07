package vazkii.botania.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import vazkii.botania.client.render.item.PylonItemRenderer;
import vazkii.botania.common.block.PylonBlock;

import java.util.function.Consumer;

/**
 * 水晶物品。
 *
 * <p>
 * 物品外观与原模组一致，由方块实体渲染器绘制，因此能显示旋转与浮动的动画；
 * 方块放置仍走普通 {@link BlockItem}。
 * </p>
 */
public class PylonItem extends BlockItem {

    public final PylonBlock.Variant variant;

    public PylonItem(PylonBlock block, Properties properties) {
        super(block, properties);
        this.variant = block.variant;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return PylonItemRenderer.instance();
            }
        });
    }
}
