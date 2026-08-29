package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.LockableIFluidHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.LockableItemStackHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the slots populated from virtual providers in an input-buffer pattern.
 * Virtual contents are configuration state, not ME-owned stock: replacing them
 * must never refund them to the network.
 */
final class MEVirtualInputState {

    private final CustomItemStackHandler itemStorage;
    private final CustomFluidTank[] fluidStorage;
    private final CustomItemStackHandler circuitStorage;
    private final LockableItemStackHandler[] itemUiHandlers;
    private final LockableIFluidHandler[] fluidUiHandlers;

    private int virtualItemSlots;
    private int virtualFluidSlots;
    private boolean virtualCircuit;

    MEVirtualInputState(CustomItemStackHandler itemStorage, CustomFluidTank[] fluidStorage,
                        CustomItemStackHandler circuitStorage) {
        this.itemStorage = itemStorage;
        this.fluidStorage = fluidStorage;
        this.circuitStorage = circuitStorage;
        this.itemUiHandlers = new LockableItemStackHandler[itemStorage.getSlots()];
        for (int i = 0; i < itemStorage.getSlots(); i++) {
            this.itemUiHandlers[i] = new LockableItemStackHandler(itemStorage);
        }
        this.fluidUiHandlers = new LockableIFluidHandler[fluidStorage.length];
        for (int i = 0; i < fluidStorage.length; i++) {
            this.fluidUiHandlers[i] = new LockableIFluidHandler(fluidStorage[i]);
        }
    }

    void setVirtualItem(int slot, @NotNull ItemStack stack) {
        virtualItemSlots |= 1 << slot;
        itemUiHandlers[slot].setLock(true);
        itemStorage.setStackInSlot(slot, stack);
    }

    void setVirtualFluid(int slot, @NotNull FluidStack fluid) {
        virtualFluidSlots |= 1 << slot;
        fluidUiHandlers[slot].setLock(true);
        fluidStorage[slot].setFluid(fluid);
    }

    void setVirtualCircuit(@NotNull ItemStack circuit) {
        virtualCircuit = true;
        circuitStorage.setStackInSlot(0, circuit);
    }

    void setManualCircuit(@NotNull ItemStack circuit) {
        virtualCircuit = false;
        circuitStorage.setStackInSlot(0, circuit);
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
        for (int slot = 0; slot < itemStorage.getSlots(); slot++) {
            if ((itemSlots & (1 << slot)) != 0) {
                itemStorage.setStackInSlot(slot, ItemStack.EMPTY);
                itemUiHandlers[slot].setLock(false);
            }
        }

        int fluidSlots = virtualFluidSlots;
        for (int slot = 0; slot < fluidStorage.length; slot++) {
            if ((fluidSlots & (1 << slot)) != 0) {
                fluidStorage[slot].setFluid(FluidStack.EMPTY);
                fluidUiHandlers[slot].setLock(false);
            }
        }

        if (virtualCircuit) {
            circuitStorage.setStackInSlot(0, ItemStack.EMPTY);
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
        for (int slot = 0; slot < itemStorage.getSlots(); slot++) {
            itemStorage.setStackInSlot(slot, ItemStack.EMPTY);
            itemUiHandlers[slot].setLock(false);
        }
        for (int slot = 0; slot < fluidStorage.length; slot++) {
            fluidStorage[slot].setFluid(FluidStack.EMPTY);
            fluidUiHandlers[slot].setLock(false);
        }
        circuitStorage.setStackInSlot(0, ItemStack.EMPTY);
        virtualItemSlots = 0;
        virtualFluidSlots = 0;
        virtualCircuit = false;
    }

    @Nullable
    CustomItemStackHandler createPersistentItemStorage() {
        CustomItemStackHandler persistent = null;
        for (int slot = 0; slot < itemStorage.getSlots(); slot++) {
            if (isVirtualItemSlot(slot)) continue;
            ItemStack stack = itemStorage.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (persistent == null) {
                persistent = new CustomItemStackHandler(itemStorage.getSlots());
                persistent.isInputLimited = itemStorage.isInputLimited;
            }
            persistent.setStackInSlot(slot, stack.copy());
        }
        return persistent;
    }

    LockableItemStackHandler[] getItemUiHandlers() {
        return itemUiHandlers;
    }

    LockableIFluidHandler[] getFluidUiHandlers() {
        return fluidUiHandlers;
    }
}
