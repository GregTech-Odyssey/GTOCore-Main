package com.hepdd.gtmthings.common.item;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

@DataGeneratorScanned
public final class VirtualItemProviderBehavior implements IAddInformation, IItemUIFactory {

    public static final VirtualItemProviderBehavior INSTANCE = new VirtualItemProviderBehavior();

    @RegisterLanguage(cn = "未设置虚拟物品", en = "No virtual item set")
    private static final String EMPTY = "gtocore.virtual_item_provider.empty";

    public static ItemStack setVirtualItem(ItemStack stack, ItemStack virtualItem) {
        return VirtualProviderData.setVirtualItem(stack, virtualItem);
    }

    public static ItemStack getVirtualItem(ItemStack stack) {
        return VirtualProviderData.getVirtualItem(stack);
    }

    @Override
    public void appendTooltips(@NotNull ItemStack itemstack, @Nullable Level world, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        ItemStack item = getVirtualItem(itemstack);
        if (item.isEmpty()) return;
        list.add(Component.translatable("gui.ae2.Items").append(": "));
        list.addAll(item.getTooltipLines(null, TooltipFlag.Default.NORMAL));
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return HeldItemPage.create(holder, entityPlayer, window -> {
            var handler = new ItemHandler(entityPlayer, holder.getHand());
            var name = TextLine.of(0, () -> {
                var stack = handler.getStackInSlot(0);
                return stack.isEmpty() ? Component.translatable(EMPTY) : stack.getHoverName();
            }).setColor(UITheme.PANEL_TEXT);
            name.layout(l -> l.flex(1));
            var row = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter()).addChildren(ItemSlot.of(handler, 0), name);
            return UIElement.section(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH)).addChild(row);
        });
    }

    private static class ItemHandler implements ICustomItemStackHandler {

        private ItemStack getItem() {
            return entityPlayer.getItemInHand(hand);
        }

        private ItemStack virtualItem;
        private final Player entityPlayer;
        private final InteractionHand hand;

        private ItemHandler(Player entityPlayer, InteractionHand hand) {
            this.entityPlayer = entityPlayer;
            this.hand = hand;
        }

        @Override
        public void setStackInSlot(int i, @NotNull ItemStack arg) {
            if (arg.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) return;
            virtualItem = arg.isEmpty() ? ItemStack.EMPTY : arg.copyWithCount(1);
            if (entityPlayer.isLocalPlayer()) return;
            entityPlayer.setItemInHand(hand, setVirtualItem(getItem(), virtualItem));
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int i) {
            if (virtualItem == null) virtualItem = getVirtualItem(getItem());
            return virtualItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int i, @NotNull ItemStack arg, boolean simulate) {
            if (entityPlayer.isLocalPlayer() || arg.isEmpty() || arg.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) return arg;
            if (!simulate) {
                virtualItem = arg.copyWithCount(1);
                entityPlayer.setItemInHand(hand, setVirtualItem(getItem(), virtualItem));
            }
            return arg.copyWithCount(arg.getCount() - 1);
        }

        @Override
        public @NotNull ItemStack extractItem(int i, int amount, boolean simulate) {
            if (amount <= 0 || entityPlayer.isLocalPlayer() || VirtualProviderData.isLocked(getItem())) return ItemStack.EMPTY;
            ItemStack old = getStackInSlot(0).copy();
            if (!simulate && !old.isEmpty()) {
                entityPlayer.setItemInHand(hand, setVirtualItem(getItem(), ItemStack.EMPTY));
                virtualItem = ItemStack.EMPTY;
            }
            return old;
        }

        @Override
        public int getSlotLimit(int i) {
            return 1;
        }

        @Override
        public boolean isItemValid(int i, @NotNull ItemStack arg) {
            return !entityPlayer.isLocalPlayer() && !arg.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get());
        }
    }
}
