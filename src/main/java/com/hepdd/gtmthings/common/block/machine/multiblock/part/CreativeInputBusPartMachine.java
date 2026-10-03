package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInfiniteSource;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget.drawSelectionOverlay;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;

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
        int rowSize = ITEM_SIZE;
        int colSize = ITEM_SIZE;
        if (getInventorySize() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        var storageAdapter = new MenuItemAdapter(this.creativeStorage);
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(
                        new PhantomSlotWidget(storageAdapter, finalIndex, 4 + x * 18, 4 + y * 18) {

                            @Override
                            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                                ItemStack stack = ItemStack.EMPTY;
                                ItemStack stackSlot = slot.getItem();
                                if (!stackSlot.isEmpty()) {
                                    stack = stackSlot.copy();
                                }

                                if (stackHeld.isEmpty() || mouseButton == 2 || mouseButton == 1) {   // held is
                                                                                                     // empty,right
                                                                                                     // click,middle
                                                                                                     // click -> clear
                                                                                                     // slot
                                    lstItem.remove(stackSlot.getItem());
                                    fillPhantomSlot(slot, ItemStack.EMPTY);
                                } else if (stackSlot.isEmpty()) {   // slot is empty
                                    if (!stackHeld.isEmpty() && !lstItem.contains(stackHeld.getItem())) { // held is not
                                                                                                          // empty and
                                                                                                          // item not in
                                                                                                          // other slot
                                                                                                          // -> add to
                                                                                                          // slot
                                        lstItem.add(stackHeld.getItem());
                                        fillPhantomSlot(slot, stackHeld);
                                    }
                                } else {
                                    if (!areItemsEqual(stackSlot, stackHeld)) {  // slot item not equal to held item
                                        if (!lstItem.contains(stackHeld.getItem())) { // item not in other slot ->
                                                                                      // change the slot
                                            lstItem.remove(stackSlot.getItem());
                                            lstItem.add(stackHeld.getItem());
                                            fillPhantomSlot(slot, stackHeld);
                                        }
                                    }
                                }
                                return stack;
                            }

                            @Override
                            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                                Position position = getPosition();
                                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                int stackX = position.x + 1;
                                int stackY = position.y + 1;
                                ItemStack stack;
                                if (getHandler() != null) {
                                    stack = getHandler().getItem();
                                    drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
                                }
                                if (mouseOverStock(mouseX, mouseY)) {
                                    drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
                                }
                            }

                            private void fillPhantomSlot(Slot slot, ItemStack stackHeld) {
                                if (stackHeld.isEmpty()) {
                                    slot.set(ItemStack.EMPTY);
                                } else {
                                    ItemStack phantomStack = stackHeld.copy();
                                    phantomStack.setCount(1);
                                    slot.set(phantomStack);
                                }
                            }

                            public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
                                return ItemStack.matches(itemStack1, itemStack2);
                            }

                            private boolean mouseOverStock(double mouseX, double mouseY) {
                                Position position = getPosition();
                                return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
                            }
                        }
                                .setClearSlotOnRightClick(false)
                                .setChangeListener(this::onChanged));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
