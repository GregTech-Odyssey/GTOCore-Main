package vazkii.botania.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.VineBallEntity;

/**
 * 藤蔓球物品：右键投出藤蔓球。
 */
public class VineBallItem extends Item {

    public VineBallItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        if (!level.isClientSide) {
            VineBallEntity ball = new VineBallEntity(player, true);
            ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(ball);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
