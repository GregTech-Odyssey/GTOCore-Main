package com.gtocore.common.machine.mana;

import com.gtolib.api.capability.IManaContainer;
import com.gtolib.api.machine.ManaDistributorMachine;
import com.gtolib.api.misc.BufferedManaContainerList;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BufferedManaContainerListTest {

    @Test
    void onlyActualBufferChangesMarkTheOwnerDirty() {
        int[] changes = { 0 };
        var state = new BufferedManaContainerList.Buffer(() -> changes[0]++);
        var cached = new BufferedManaContainerList(state, new Container(10_000, 100));
        assertEquals(100, cached.removeMana(100, 1, true));
        assertEquals(0, changes[0]);
        assertEquals(100, cached.removeMana(100, 1, false));
        assertEquals(2, changes[0]);
        assertEquals(100, cached.removeMana(100, 1, false));
        assertEquals(3, changes[0]);
    }

    @Test
    void simulationDoesNotPrefetchOrConsumeMana() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);

        assertEquals(100, cached.removeMana(100, 1, true));
        assertEquals(10_000, source.stored);
        assertEquals(0, cached.getBufferedMana());
        assertEquals(0, source.removals);
    }

    @Test
    void refillMovesTenTicksOfActualManaBeforeConsuming() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);

        assertEquals(100, cached.removeMana(100, 1, false));
        assertEquals(9_000, source.stored);
        assertEquals(900, cached.getBufferedMana());
        assertEquals(1_000, cached.getBufferCapacity());
        assertEquals(9_900, cached.getCurrentMana());
    }

    @Test
    void cachedManaContinuesWorkingAfterHatchEmptiesThenRunsOut() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);
        assertEquals(100, cached.removeMana(100, 1, false));
        source.setCurrentMana(0);

        for (int tick = 0; tick < 9; tick++) {
            assertEquals(100, cached.removeMana(100, 1, false));
        }
        assertEquals(0, cached.removeMana(100, 1, false));
        assertEquals(0, cached.getCurrentMana());
    }

    @Test
    void partialSourceCreditsOnlyWhatWasActuallyRemoved() {
        var source = new Container(35, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);

        assertEquals(35, cached.removeMana(100, 1, false));
        assertEquals(0, source.stored);
        assertEquals(0, cached.getBufferedMana());
    }

    @Test
    void simulationDoesNotPromiseManaAfterSourceChanges() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);
        assertEquals(100, cached.removeMana(100, 1, true));
        source.setCurrentMana(25);

        assertEquals(25, cached.removeMana(100, 1, false));
        assertEquals(0, cached.getCurrentMana());
    }

    @Test
    void sameBufferSurvivesContainerReconstruction() {
        var source = new Container(10_000, 100);
        var state = new BufferedManaContainerList.Buffer();
        var first = new BufferedManaContainerList(state, source);
        assertEquals(100, first.removeMana(100, 1, false));

        var reformed = new BufferedManaContainerList(state, source);
        assertEquals(900, reformed.getBufferedMana());
        assertEquals(9_900, reformed.getCurrentMana());
        assertEquals(100, reformed.removeMana(100, 1, false));
        assertEquals(1, source.removals);
    }

    @Test
    void requestsLargerThanBufferKeepOriginalRateMultiplier() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);

        assertEquals(2_000, cached.removeMana(2_000, 20, false));
        assertEquals(8_000, cached.getCurrentMana());
        assertEquals(100, cached.removeMana(2_000, 1, true));
    }

    @Test
    void unrestrictedRemovalIncludesBufferedMana() {
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);
        assertEquals(100, cached.removeMana(100, 1, false));

        assertEquals(9_900, cached.removeManaUnrestricted(9_900, false));
        assertEquals(0, source.stored);
        assertEquals(0, cached.getBufferedMana());
    }

    @Test
    void fullLoadAmortizesOneHundredHatchRemovalsToTen() {
        var originalSource = new Container(100_000, 100);
        var bufferedSource = new Container(100_000, 100);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), bufferedSource);

        for (int tick = 0; tick < 100; tick++) {
            assertEquals(100, originalSource.removeMana(100, 1, false));
            assertEquals(100, cached.removeMana(100, 1, false));
        }
        assertEquals(100, originalSource.removals);
        assertEquals(10, bufferedSource.removals);
        assertEquals(originalSource.stored, cached.getCurrentMana());
    }

    @Test
    void rebuildingWithSmallerHatchesPreservesPreviouslyWithdrawnMana() {
        var state = new BufferedManaContainerList.Buffer();
        var source = new Container(10_000, 100);
        var cached = new BufferedManaContainerList(state, source);
        assertEquals(100, cached.removeMana(100, 1, false));
        var smaller = new BufferedManaContainerList(state, new Container(0, 1));
        assertEquals(900, smaller.getBufferedMana());
        assertEquals(10, smaller.getBufferCapacity());
        assertEquals(900, smaller.removeManaUnrestricted(900, false));
        assertEquals(0, smaller.getBufferedMana());
    }

    @Test
    void overflowingPrefetchRateFallsBackToDirectConsumption() {
        var source = new Container(Long.MAX_VALUE, Long.MAX_VALUE);
        var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);

        assertEquals(1, cached.removeMana(1, 1, false));
        assertEquals(Long.MAX_VALUE - 1, source.stored);
        assertEquals(0, cached.getBufferedMana());
    }

    @Test
    void bufferCanBeDisabledWithoutChangingConsumption() {
        String property = "gtocore.disableManaBuffer";
        String previous = System.getProperty(property);
        System.setProperty(property, "true");
        try {
            var source = new Container(10_000, 100);
            var cached = new BufferedManaContainerList(new BufferedManaContainerList.Buffer(), source);
            assertEquals(100, cached.removeMana(100, 1, false));
            assertEquals(9_900, source.stored);
            assertEquals(0, cached.getBufferedMana());
        } finally {
            if (previous == null) System.clearProperty(property);
            else System.setProperty(property, previous);
        }
    }

    private static final class Container implements IManaContainer {

        private long stored;
        private final long capacity;
        private final long rate;
        private int removals;

        private Container(long stored, long rate) {
            this.stored = stored;
            this.capacity = stored;
            this.rate = rate;
        }

        @Override
        public long removeMana(long amount, int rateMultiplier, boolean simulate) {
            if (!simulate) removals++;
            return IManaContainer.super.removeMana(amount, rateMultiplier, simulate);
        }

        @Override
        public boolean acceptDistributor() {
            return false;
        }

        @Override
        public MetaMachine getMachine() {
            return null;
        }

        @Override
        public long getMaxMana() {
            return capacity;
        }

        @Override
        public long getCurrentMana() {
            return stored;
        }

        @Override
        public void setCurrentMana(long mana) {
            stored = mana;
        }

        @Override
        public long getMaxIORate() {
            return rate;
        }

        @Override
        public UUID getOwnerUUID() {
            return null;
        }

        @Override
        public ManaDistributorMachine getNetMachineCache() {
            return null;
        }

        @Override
        public void setNetMachineCache(ManaDistributorMachine cache) {}
    }
}
