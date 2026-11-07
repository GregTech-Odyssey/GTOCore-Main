package vazkii.botania.forge.integration.ae2;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ManaCellItem extends Item {

    private final Supplier<? extends ItemLike> component;
    private final Supplier<? extends ItemLike> housing;
    private final long capacity;
    private final double idleDrain;

    public ManaCellItem(Properties props, Supplier<? extends ItemLike> component, Supplier<? extends ItemLike> housing, int kilobytes, double idleDrain) {
        super(props);
        this.component = component;
        this.housing = housing;
        this.capacity = kilobytes * 1000L * ManaKeyType.AMOUNT_PER_BYTE;
        this.idleDrain = idleDrain;
    }

    public long getCapacity() {
        return capacity;
    }

    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (ManaCellInventory.storedIn(stack) > 0) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            stack.shrink(1);
            var inventory = player.getInventory();
            inventory.placeItemBackInInventory(new ItemStack(component.get()));
            inventory.placeItemBackInInventory(new ItemStack(housing.get()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("botania.ae2.cell_stored", String.format("%,d", ManaCellInventory.storedIn(stack)), String.format("%,d", capacity))
                .withStyle(ChatFormatting.GRAY));
    }
}
