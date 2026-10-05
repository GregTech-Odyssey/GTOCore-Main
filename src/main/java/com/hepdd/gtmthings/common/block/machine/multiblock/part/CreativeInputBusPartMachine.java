package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInfiniteSource;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import lombok.Getter;

import java.util.ArrayList;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeInputBusPartMachine extends WorkableTieredIOPartMachine implements IDistinctPart {

    private final int ITEM_SIZE = 5;

    @Getter
    private final NotifiableInfiniteSource<AEItemKey> inventory;
    @Getter
    @SaveToDisk
    protected final NotifiableInventory<AEItemKey> circuitInventory;
    @SaveToDisk
    private final KeyInventory<AEItemKey> creativeStorage;
    protected ArrayList<Item> lstItem;

    @Getter
    @SaveToDisk
    private boolean isDistinct = false;

    public CreativeInputBusPartMachine(MetaMachineBlockEntity holder, Function<Integer, KeyInventory<AEItemKey>> transferFactory) {
        super(holder, GTValues.MAX, IO.IN);
        this.creativeStorage = transferFactory.apply(this.getInventorySize());
        this.inventory = createInventory();
        this.circuitInventory = CircuitHandler.create(this);
        this.lstItem = new ArrayList<>();
    }

    public CreativeInputBusPartMachine(MetaMachineBlockEntity holder) {
        this(holder, KeyInventory::items);
    }

    protected int getInventorySize() {
        return ITEM_SIZE * ITEM_SIZE;
    }

    protected NotifiableInfiniteSource<AEItemKey> createInventory() {
        return new NotifiableInfiniteSource<>(this, creativeStorage, io, io, false);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lstItem.clear();
        for (int i = 0; i < this.getInventorySize(); i++) {
            var key = this.creativeStorage.keyAt(i);
            if (key != null) {
                lstItem.add(key.getItem());
            }
        }
        if (isDistinct) {
            getHandlerUnit().setDistinct(true);
        }
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        getHandlerUnit().setDistinctAndNotify(isDistinct);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
    }

    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    @Override
    public Widget createUIWidget() {
        var transfer = ItemTransferHelperImpl.toItemTransfer(new MenuItemAdapter(creativeStorage));
        return HatchViews.page(SlotGrid.square(getInventorySize(), i -> createSlot(transfer, i)));
    }

    private Widget createSlot(IItemTransfer transfer, int index) {
        return new PhantomItemSlot(transfer, index) {

            @Override
            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                ItemStack stackSlot = slot.getItem();
                ItemStack stack = stackSlot.isEmpty() ? ItemStack.EMPTY : stackSlot.copy();
                if (stackHeld.isEmpty() || mouseButton == 2 || mouseButton == 1) {
                    lstItem.remove(stackSlot.getItem());
                    fillPhantomSlot(slot, ItemStack.EMPTY);
                } else if (stackSlot.isEmpty()) {
                    if (!lstItem.contains(stackHeld.getItem())) {
                        lstItem.add(stackHeld.getItem());
                        fillPhantomSlot(slot, stackHeld);
                    }
                } else if (!ItemStack.matches(stackSlot, stackHeld) && !lstItem.contains(stackHeld.getItem())) {
                    lstItem.remove(stackSlot.getItem());
                    lstItem.add(stackHeld.getItem());
                    fillPhantomSlot(slot, stackHeld);
                }
                return stack;
            }

            private void fillPhantomSlot(Slot slot, ItemStack stackHeld) {
                if (stackHeld.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.set(stackHeld.copyWithCount(1));
                }
            }
        }.setClearSlotOnRightClick(false).setChangeListener(this::onChanged);
    }
}
