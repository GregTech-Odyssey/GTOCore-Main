package com.gtocore.common.machine.multiblock.part.voiding;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import appeng.hooks.IUnique;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class FilterVoidOutputBusPartMachine extends VoidOutputBusPartMachine implements AEKeyFilter {

    @Getter
    @SaveToDisk
    private final StackInventory inventory = new StackInventory(81) {

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    @Setter
    @Getter
    @SaveToDisk(defaultValue = "false")
    public boolean reverse;

    private final IntOpenHashSet ids = new IntOpenHashSet();

    public FilterVoidOutputBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MV);
    }

    @Override
    public boolean matches(AEKey key) {
        if (!(key instanceof AEItemKey itemKey)) return false;
        var id = ((IUnique) itemKey.getItem()).ae2$getUid();
        if (reverse) {
            return !ids.contains(id);
        } else {
            return ids.contains(id);
        }
    }

    private void onSlotChanged() {
        ids.clear();
        for (var stack : inventory.stacks) {
            if (!stack.isEmpty()) {
                ids.add(((IUnique) stack.getItem()).ae2$getUid());
            }
        }
        if (ids.isEmpty()) {
            handler.setFilter(null);
        } else {
            handler.setFilter(this);
        }
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onSlotChanged();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators((new IFancyConfiguratorButton.Toggle(WidgetIcons.BLACKLIST, WidgetIcons.WHITELIST, this::isReverse, (clickData, pressed) -> this.setReverse(pressed))).setTooltipsSupplier((pressed) -> List.of(Component.translatable("gui.ae2wtlib.switch").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)).append(Component.translatable(pressed ? "gui.ae2wtlib.whitelist" : "gui.ae2wtlib.blacklist")))));
    }

    @Override
    public Widget createUIWidget() {
        var transfer = ItemTransferHelperImpl.toItemTransfer(new ForgeStackAdapter(inventory));
        return HatchViews.page(SlotGrid.square(81, index -> {
            var slot = PhantomItemSlot.of(transfer, index);
            slot.setChangeListener(this::onSlotChanged);
            return slot.xeiPhantom();
        }));
    }
}
