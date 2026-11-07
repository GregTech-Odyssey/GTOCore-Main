package vazkii.botania.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaMenus;
import vazkii.botania.common.item.FlowerPouchItem;

/** 采花袋界面：32 格花槽与玩家背包。 */
public class FlowerPouchContainer extends AbstractContainerMenu {

    public final SimpleContainer flowerBagInv;
    private final ItemStack bag;

    public static FlowerPouchContainer fromNetwork(int windowId, Inventory inventory, FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return new FlowerPouchContainer(windowId, inventory, inventory.player.getItemInHand(hand));
    }

    public FlowerPouchContainer(int windowId, Inventory playerInventory, ItemStack bag) {
        super(BotaniaMenus.FLOWER_BAG.get(), windowId);
        this.bag = bag;
        this.flowerBagInv = playerInventory.player.level().isClientSide ? new SimpleContainer(FlowerPouchItem.SIZE) : FlowerPouchItem.getInventory(bag);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                int slot = col + row * 8;
                addSlot(new Slot(flowerBagInv, slot, 17 + col * 18, 26 + row * 18) {

                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.is(FlowerPouchItem.getFlowerForSlot(slot));
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 120 + row * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (playerInventory.getItem(i) == bag) {
                addSlot(new Slot(playerInventory, i, 8 + i * 18, 178) {

                    @Override
                    public boolean mayPickup(@NotNull Player player) {
                        return false;
                    }

                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });
            } else {
                addSlot(new Slot(playerInventory, i, 8 + i * 18, 178));
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem().getItem() instanceof FlowerPouchItem || player.getOffhandItem().getItem() instanceof FlowerPouchItem;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < FlowerPouchItem.SIZE) {
            if (!moveItemStackTo(stack, FlowerPouchItem.SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int bagSlot = FlowerPouchItem.slotFor(stack);
            if (bagSlot < 0 || !slots.get(bagSlot).mayPlace(stack)) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, bagSlot, bagSlot + 1, true)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }
}
