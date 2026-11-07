package vazkii.botania.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaItems;
import vazkii.botania.common.entity.ThornChakramEntity;

/**
 * 荆棘飞轮物品：右键投出飞轮，闪耀飞轮带火焰。
 */
public class ThornChakramItem extends Item {

    public ThornChakramItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            ThornChakramEntity chakram = new ThornChakramEntity(player, level, copy);
            chakram.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            if (stack.is(BotaniaItems.FLARE_CHAKRAM.get())) {
                chakram.setFire(true);
            }
            level.addFreshEntity(chakram);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
