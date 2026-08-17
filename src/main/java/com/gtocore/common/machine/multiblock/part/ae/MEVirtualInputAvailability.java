package com.gtocore.common.machine.multiblock.part.ae;

/**
 * Transient availability flags for virtual inputs projected into a pattern
 * buffer's configuration view.
 *
 * <p>The flags are deliberately not persisted: they describe the current ME
 * network and are recomputed whenever the pattern is decoded.</p>
 */
final class MEVirtualInputAvailability {

    private int missingItemSlots;
    private int missingFluidSlots;
    private boolean missingCircuit;

    void clear() {
        missingItemSlots = 0;
        missingFluidSlots = 0;
        missingCircuit = false;
    }

    void setItemMissing(int slot, boolean missing) {
        if (missing) {
            missingItemSlots |= 1 << slot;
        } else {
            missingItemSlots &= ~(1 << slot);
        }
    }

    void setFluidMissing(int slot, boolean missing) {
        if (missing) {
            missingFluidSlots |= 1 << slot;
        } else {
            missingFluidSlots &= ~(1 << slot);
        }
    }

    void setCircuitMissing(boolean missing) {
        missingCircuit = missing;
    }

    boolean isItemMissing(int slot) {
        return (missingItemSlots & (1 << slot)) != 0;
    }

    boolean isFluidMissing(int slot) {
        return (missingFluidSlots & (1 << slot)) != 0;
    }

    boolean isCircuitMissing() {
        return missingCircuit;
    }
}
