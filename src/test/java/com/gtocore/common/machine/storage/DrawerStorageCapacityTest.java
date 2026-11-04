package com.gtocore.common.machine.storage;

import com.gtolib.api.machine.storage.DrawerStorageCapacity;
import com.gtolib.api.machine.storage.DrawerStorageCapacity.StorageType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrawerStorageCapacityTest {

    private static final StorageType ULV_ITEM = new StorageType(false, 500_000, "ulv_chest");
    private static final StorageType LV_ITEM = new StorageType(false, 4_000_000, "lv_chest");
    private static final StorageType ULV_FLUID = new StorageType(true, 500_000, "ulv_tank");

    @Test
    void reproducesIssueExampleWithIndependentItemAndFluidLimits() {
        var capacity = new DrawerStorageCapacity();
        capacity.add(ULV_ITEM, 64);
        capacity.add(LV_ITEM, 1);
        capacity.add(new StorageType(true, 4_000_000, "lv_tank"), 1);
        assertEquals(260, capacity.getItemTypes());
        assertEquals(4, capacity.getFluidTypes());
        // The issue mockup omits the hermetic multiplier on its item total; the written formula includes it.
        assertEquals(1_040_000_000, capacity.capacityOf(false, 2, 1));
        assertEquals(16_000_000, capacity.capacityOf(true, 2, 1));
        assertEquals(2_000_000, capacity.capacityOf(true, 2, 4));
    }

    @Test
    void cheapContainersCannotIncreaseAnotherTiersSpecialization() {
        var capacity = new DrawerStorageCapacity();
        capacity.add(ULV_ITEM, 10);
        capacity.add(LV_ITEM, 1);
        assertEquals(204_545, capacity.capacityOf(false, 1, 44));
        assertEquals(409_090, capacity.capacityOf(false, 2, 44));
    }

    @Test
    void combiningContainersAcrossBusSlotsHasTheSameBonus() {
        var split = new DrawerStorageCapacity();
        split.add(ULV_FLUID, 32);
        split.add(ULV_FLUID, 32);
        assertEquals(512_000_000, split.capacityOf(true, 1, 1));
        var combined = new DrawerStorageCapacity();
        combined.add(ULV_FLUID, 128);
        assertEquals(split.capacityOf(true, 1, 1), combined.capacityOf(true, 1, 2));
    }

    @Test
    void saturatesOnlyAfterAveragingAndClearRemovesAllContributions() {
        var capacity = new DrawerStorageCapacity();
        capacity.add(new StorageType(false, Long.MAX_VALUE, "max_chest"), 64);
        assertEquals(1L << 61, capacity.capacityOf(false, 1, 256));
        assertEquals(Long.MAX_VALUE, capacity.capacityOf(false, 1, 1));
        capacity.clear();
        assertEquals(0, capacity.getItemTypes());
        assertEquals(0, capacity.getDrawerCount());
        assertEquals(0, capacity.capacityOf(false, 1, 1));
        assertEquals(0, capacity.capacityOf(true, 1, 0));
        capacity.add(ULV_ITEM, Long.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, capacity.getItemTypes());
        assertEquals(Long.MAX_VALUE, capacity.capacityOf(false, 16, 1));
    }
}
