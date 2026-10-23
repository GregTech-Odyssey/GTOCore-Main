package com.gtolib.api.dimension;

import net.minecraft.nbt.CompoundTag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionClockTest {

    @Test
    void longSolarDayRetainsSubTickProgressAcrossSaving() {
        long mercuryDay = 4_223_376;
        var clock = new DimensionClock(18000);
        for (int i = 0; i < 73; i++) clock.advance(mercuryDay);
        assertEquals(18000, clock.dayTime());
        assertTrue(clock.remainder() > 0);
        var restored = DimensionClock.load(clock.save(new CompoundTag()));
        for (int i = 73; i < mercuryDay; i++) restored.advance(mercuryDay);
        assertEquals(42000, restored.dayTime());
        assertEquals(0, restored.remainder());
    }

    @Test
    void fasterDaysAndTimeChangesKeepIndependentPhases() {
        var fast = new DimensionClock(0);
        var slow = new DimensionClock(0);
        for (int i = 0; i < 10000; i++) {
            fast.advance(9076);
            slow.advance(708735);
        }
        assertEquals(240_000_000L / 9076, fast.dayTime());
        assertEquals(240_000_000L % 9076, fast.remainder());
        long slowTime = slow.dayTime();
        long slowRemainder = slow.remainder();
        fast.setDayTime(6000);
        assertEquals(0, fast.remainder());
        assertEquals(slowTime, slow.dayTime());
        assertEquals(slowRemainder, slow.remainder());
        fast.advance(9076);
        assertEquals(6002, fast.dayTime());
    }

    @Test
    void dormantCatchUpMatchesContinuousSimulationAcrossLongSolarDays() {
        for (long day : new long[] { 9076, 24000, 708735, 4223376 }) {
            var running = new DimensionClock(8000);
            var dormant = new DimensionClock(8000);
            for (int tick = 0; tick < 200003; tick++) running.advance(day);
            dormant.advance(day, 200003);
            assertEquals(running.dayTime(), dormant.dayTime());
            assertEquals(running.remainder(), dormant.remainder());
        }
    }
}
