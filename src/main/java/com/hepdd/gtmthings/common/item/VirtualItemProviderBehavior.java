package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

public final class VirtualItemProviderBehavior implements IAddInformation, IItemUIFactory {

    public static final VirtualItemProviderBehavior INSTANCE = new VirtualItemProviderBehavior();

    public static ItemStack setVirtualItem(ItemStack stack, ItemStack virtualItem) {
        if (virtualItem.isEmpty()) {
            VirtualProviderData.clearContent(stack);
            return stack;
        }
        VirtualProviderData.setContent(stack, virtualItem.copyWithCount(1).save(new CompoundTag()));
        return stack;
    }

    public static ItemStack getVirtualItem(ItemStack stack) {
        CompoundTag itemTag = VirtualProviderData.getContent(stack);
        if (itemTag == null) return ItemStack.EMPTY;
        return ItemStack.of(itemTag);
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
        return new ModularUI(176, 166, holder, entityPlayer)
                .widget(new FancyMachineUIWidget(new ProviderUI(holder.getHand()), 176, 166));
    }

    private record ProviderUI(InteractionHand hand) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 18 + 16, 18 + 16);
            WidgetGroup container = new WidgetGroup(4, 4, 18 + 8, 18 + 8);
            container.addWidget(new SlotWidget(new ItemHandler(widget.getGui().entityPlayer, hand), 0, 4, 4, true, true)
                    .setBackground(GuiTextures.SLOT));
            group.addWidget(container);
            return group;
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(CustomItems.VIRTUAL_ITEM_PROVIDER.get());
        }

        @Override
        public Component getTitle() {
            return CustomItems.VIRTUAL_ITEM_PROVIDER.get().getDescription();
        }
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
            if (entityPlayer.isLocalPlayer() || arg.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) return;
            virtualItem = arg.copyWithCount(1);
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
