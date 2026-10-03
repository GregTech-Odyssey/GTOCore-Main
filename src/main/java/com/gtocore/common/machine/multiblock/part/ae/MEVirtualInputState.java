package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.recipe.content.Circuits;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerView;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the slots populated from virtual providers in an input-buffer pattern.
 * Virtual contents are configuration state, not ME-owned stock: replacing them
 * must never refund them to the network.
 */
final class MEVirtualInputState {

    private final KeyInventory<AEItemKey> itemStorage;
    private final KeyInventory<AEFluidKey> fluidStorage;
    private final KeyInventory<AEItemKey> circuitStorage;
    private final LockableItems[] itemUiHandlers;
    private final LockableKeys<AEFluidKey>[] fluidLocks;
    private final IFluidHandler[] fluidUiHandlers;

    private int virtualItemSlots;
    private int virtualFluidSlots;
    private boolean virtualCircuit;

    @SuppressWarnings("unchecked")
    MEVirtualInputState(KeyInventory<AEItemKey> itemStorage, KeyInventory<AEFluidKey> fluidStorage,
                        KeyInventory<AEItemKey> circuitStorage) {
        this.itemStorage = itemStorage;
        this.fluidStorage = fluidStorage;
        this.circuitStorage = circuitStorage;
        var menuAdapter = new MenuItemAdapter(itemStorage);
        this.itemUiHandlers = new LockableItems[itemStorage.size()];
        for (int i = 0; i < itemUiHandlers.length; i++) {
            this.itemUiHandlers[i] = new LockableItems(menuAdapter);
        }
        int tanks = fluidStorage.size();
        this.fluidLocks = new LockableKeys[tanks];
        this.fluidUiHandlers = new IFluidHandler[tanks];
        for (int i = 0; i < tanks; i++) {
            var lock = new LockableKeys<>(fluidStorage);
            this.fluidLocks[i] = lock;
            this.fluidUiHandlers[i] = new ForgeFluidAdapter(lock);
        }
    }

    void setVirtualItem(int slot, AEItemKey key, long amount) {
        virtualItemSlots |= 1 << slot;
        itemUiHandlers[slot].setLock(true);
        itemStorage.set(slot, key, amount);
    }

    void setVirtualFluid(int slot, AEFluidKey key, long amount) {
        virtualFluidSlots |= 1 << slot;
        fluidLocks[slot].setLock(true);
        fluidStorage.set(slot, key, amount);
    }

    void setVirtualCircuit(AEItemKey circuit) {
        virtualCircuit = true;
        circuitStorage.set(0, circuit, 1);
    }

    void setManualCircuit(int configuration) {
        virtualCircuit = false;
        Circuits.set(circuitStorage, 0, Math.min(configuration, Circuits.MAX));
    }

    boolean isVirtualCircuit() {
        return virtualCircuit;
    }

    boolean isVirtualItemSlot(int slot) {
        return (virtualItemSlots & (1 << slot)) != 0;
    }

    boolean isVirtualFluidSlot(int slot) {
        return (virtualFluidSlots & (1 << slot)) != 0;
    }

    /**
     * Discards projected virtual values in place. Only real user/ME contents
     * remain candidates for the normal refund path.
     */
    void clearVirtualInputs() {
        int itemSlots = virtualItemSlots;
        for (int slot = 0; slot < itemStorage.size(); slot++) {
            if ((itemSlots & (1 << slot)) != 0) {
                itemStorage.set(slot, null, 0);
                itemUiHandlers[slot].setLock(false);
            }
        }

        int fluidSlots = virtualFluidSlots;
        for (int slot = 0; slot < fluidStorage.size(); slot++) {
            if ((fluidSlots & (1 << slot)) != 0) {
                fluidStorage.set(slot, null, 0);
                fluidLocks[slot].setLock(false);
            }
        }

        if (virtualCircuit) {
            circuitStorage.set(0, null, 0);
        }

        virtualItemSlots = 0;
        virtualFluidSlots = 0;
        virtualCircuit = false;
    }

    /**
     * NBT loading is authoritative. Clear every mutable slot first because a
     * missing NBT entry must not leave an old virtual value behind.
     */
    void resetForDeserialize() {
        itemStorage.clear();
        for (var handler : itemUiHandlers) {
            handler.setLock(false);
        }
        fluidStorage.clear();
        for (var lock : fluidLocks) {
            lock.setLock(false);
        }
        circuitStorage.set(0, null, 0);
        virtualItemSlots = 0;
        virtualFluidSlots = 0;
        virtualCircuit = false;
    }

    @Nullable
    KeyInventory<AEItemKey> createPersistentItemStorage() {
        KeyInventory<AEItemKey> persistent = null;
        for (int slot = 0; slot < itemStorage.size(); slot++) {
            if (isVirtualItemSlot(slot)) continue;
            var key = itemStorage.keyAt(slot);
            if (key == null) continue;
            if (persistent == null) {
                persistent = KeyInventory.items(itemStorage.size());
                persistent.setUniqueKeys(itemStorage.isUniqueKeys());
            }
            persistent.set(slot, key, itemStorage.amountAt(slot));
        }
        return persistent;
    }

    IItemHandlerModifiable[] getItemUiHandlers() {
        return itemUiHandlers;
    }

    IFluidHandler[] getFluidUiHandlers() {
        return fluidUiHandlers;
    }

    static final class LockableItems implements IItemHandlerModifiable {

        private final IItemHandlerModifiable delegate;
        private boolean lock;

        LockableItems(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        LockableItems setLock(boolean lock) {
            this.lock = lock;
            return this;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return lock ? stack : delegate.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return lock ? ItemStack.EMPTY : delegate.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !lock && delegate.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!lock) delegate.setStackInSlot(slot, stack);
        }
    }

    static final class LockableKeys<K extends AEKey> extends KeyHandlerView<K> {

        private boolean lock;

        LockableKeys(IKeyHandler<K> delegate) {
            super(delegate);
        }

        void setLock(boolean lock) {
            this.lock = lock;
        }

        @Override
        protected boolean canInsert(K key) {
            return !lock;
        }

        @Override
        protected boolean canExtract(K key) {
            return !lock;
        }
    }
}
