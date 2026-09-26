package com.gtocore.common.item;

import com.gtolib.utils.RLUtils;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import org.jetbrains.annotations.NotNull;

public final class OrderItem implements IItemUIFactory, ICustomDescriptionId {

    public static final OrderItem INSTANCE = new OrderItem();

    private static final String TARGET = "item.gtocore.order.target";
    private static final String NO_TARGET = "item.gtocore.order.no_target";

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return HeldItemPage.create(holder, player, window -> {
            var name = TextLine.of(0, () -> {
                var target = getTarget(holder.getHeld());
                return target.isEmpty() ? Component.translatable(NO_TARGET) : target.getHoverName();
            }).setColor(UITheme.PANEL_TEXT);
            name.layout(l -> l.flex(1));
            var slot = new PhantomItemSlot(new TargetSlot(holder), 0).xeiPhantom();
            slot.setHoverTooltips(Component.translatable(TARGET));
            var row = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter()).addChildren(slot, name);
            return UIElement.section(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH)).addChild(row);
        });
    }

    public static ItemStack setTarget(ItemStack stack, ItemStack target) {
        var tag = stack.getOrCreateTag();
        var id = BuiltInRegistries.ITEM.getKey(target.getItem());
        tag.putString("marker_id", id.toString());
        if (target.hasTag()) {
            tag.put("marker_nbt", target.getTag().copy());
        } else {
            tag.remove("marker_nbt");
        }
        return stack;
    }

    public static ItemStack getTarget(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var id = tag.getString("marker_id");
        if (id.isEmpty()) {
            return ItemStack.EMPTY;
        }
        CompoundTag nbt = tag.getCompound("marker_nbt");
        var i = new ItemStack(RegistriesUtils.getItem(RLUtils.parse(id)));
        if (nbt != null) {
            i.setTag(nbt.copy());
        }
        return i;
    }

    public static ItemStack clearTarget(ItemStack stack) {
        if (!stack.hasTag()) return stack;
        var tag = stack.getOrCreateTag();
        tag.remove("marker_id");
        tag.remove("marker_nbt");
        return stack;
    }

    @Override
    public Component getItemName(ItemStack stack) {
        Component name = Component.empty();
        if (stack.hasTag()) {
            name = getTarget(stack).getHoverName();
        }
        return Component.translatable(stack.getDescriptionId(), name);
    }

    private static final class TargetSlot implements IItemTransfer {

        private final HeldItemUIFactory.HeldItemHolder holder;
        private ItemStack clientStack = ItemStack.EMPTY;

        private TargetSlot(HeldItemUIFactory.HeldItemHolder holder) {
            this.holder = holder;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (holder.isRemote()) return clientStack;
            return getTarget(holder.getHeld());
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            if (holder.isRemote()) {
                clientStack = stack;
                return;
            }
            if (stack.isEmpty()) clearTarget(holder.getHeld());
            else if (isItemValid(index, stack)) setTarget(holder.getHeld(), stack);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
            if (!simulate) setStackInSlot(slot, stack);
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            var target = getTarget(holder.getHeld());
            if (!simulate && !target.isEmpty()) setStackInSlot(slot, ItemStack.EMPTY);
            return target;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() != holder.getHeld().getItem();
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return getTarget(holder.getHeld());
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof ItemStack stack) setStackInSlot(0, stack);
        }
    }
}
