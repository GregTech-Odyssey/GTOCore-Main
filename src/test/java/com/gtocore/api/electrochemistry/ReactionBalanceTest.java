package com.gtocore.api.electrochemistry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReactionBalanceTest {

    @Test
    void crossUsesAbsoluteOppositeChargesWithoutReducingCoefficients() {
        var balance = ReactionBalance.cross(2, -4);
        assertEquals(4, balance.firstMultiplier());
        assertEquals(2, balance.secondMultiplier());
        assertEquals(0, 2 * balance.firstMultiplier() - 4 * balance.secondMultiplier());
        assertEquals(new ReactionBalance(2, 4), ReactionBalance.cross(-4, 2));
    }

    @Test
    void nonreactingAndUnrepresentableChargesAreRejected() {
        assertNull(ReactionBalance.cross(1, 3));
        assertNull(ReactionBalance.cross(-1, -3));
        assertNull(ReactionBalance.cross(0, -1));
        assertNull(ReactionBalance.cross(Long.MIN_VALUE, 1));
    }

    @Test
    void fractionalMembraneEfficiencyBalancesEitherFuelSideExactly() {
        var balance = ReactionBalance.fuel(2, -1, true, 105);
        assertEquals(new ReactionBalance(10, 21), balance);
        assertEquals(2 * 105 * balance.firstMultiplier(), 100 * balance.secondMultiplier());
        assertEquals(new ReactionBalance(21, 10), ReactionBalance.fuel(-1, 2, false, 105));
    }

    @Test
    void storageNeverRoundsUpAndScalingMakesSmallFuelBatchesUsable() {
        assertEquals(0, ReactionBalance.storedAmount(99, 1, 100, 100));
        long scale = ReactionBalance.minimumStorageScale(99, 1, 100, 100);
        assertEquals(2, scale);
        assertEquals(1, ReactionBalance.storedAmount(99, scale, 100, 100));
        assertEquals(13, ReactionBalance.storedAmount(1000, 1, 130, 100));
    }

    @Test
    void intermediateOverflowCannotCreateOrLoseStoredEnergy() {
        assertEquals(Long.MAX_VALUE / 2, ReactionBalance.storedAmount(Long.MAX_VALUE, 10, 250, 50));
        assertEquals(0, ReactionBalance.storedAmount(Long.MAX_VALUE, 10, 250, 1));
    }
}
