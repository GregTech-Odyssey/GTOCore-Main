package vazkii.botania.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** 多媒体索引物品：使用 3D 方块实体渲染。 */
public class CorporeaIndexItem extends BlockItem {

    public CorporeaIndexItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return vazkii.botania.client.render.item.CorporeaIndexItemRenderer.instance();
            }
        });
    }
}
