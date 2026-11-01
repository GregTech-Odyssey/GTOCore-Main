package com.gtolib.drone;

import net.minecraft.core.BlockPos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneSelectionTest {

    @Test
    void rectangleUsesInclusiveColumnsAndLowestAnchorRegardlessOfClickOrder() {
        var selection = new DroneSelection(new BlockPos(15, 100, -4), new BlockPos(-48, -20, 59), false);
        assertTrue(selection.fits(3));
        assertEquals(-20, selection.minY());
        assertTrue(selection.contains(-48, -4));
        assertTrue(selection.contains(15, 59));
        assertFalse(selection.contains(16, 59));
        assertFalse(selection.contains(15, 60));
        var reversed = new DroneSelection(selection.second(), selection.first(), false);
        for (int x = -49; x <= 16; x++) {
            for (int z = -5; z <= 60; z++) assertEquals(selection.contains(x, z), reversed.contains(x, z));
        }
    }

    @Test
    void tierLimitsApplyIndependentlyToBothRectangleSides() {
        var origin = new BlockPos(0, 0, 0);
        assertFalse(new DroneSelection(origin, new BlockPos(64, 0, 0), false).fits(3));
        assertFalse(new DroneSelection(origin, new BlockPos(0, 0, 64), false).fits(3));
        assertTrue(new DroneSelection(origin, new BlockPos(255, 0, 255), false).fits(4));
        assertFalse(new DroneSelection(origin, new BlockPos(256, 0, 255), false).fits(4));
        assertFalse(new DroneSelection(origin, new BlockPos(255, 0, 256), false).fits(4));
        assertTrue(new DroneSelection(origin, new BlockPos(1000000, 0, -1000000), false).fits(5));
    }

    @Test
    void circleUsesHorizontalRadiusRatherThanHeightOrBoundingSquareCorners() {
        var selection = new DroneSelection(new BlockPos(-10, 120, -20), new BlockPos(-7, -40, -16), true);
        assertEquals(5, selection.radius());
        assertEquals(-40, selection.minY());
        assertTrue(selection.contains(-7, -16));
        assertTrue(selection.contains(-15, -20));
        assertFalse(selection.contains(-15, -25));
        assertEquals(-15, selection.minX());
        assertEquals(-5, selection.maxX());
        assertEquals(-25, selection.minZ());
        assertEquals(-15, selection.maxZ());
    }

    @Test
    void circularLimitsUseDiameterAndDoNotOverflowForLargeIvSelections() {
        var origin = BlockPos.ZERO;
        assertTrue(new DroneSelection(origin, new BlockPos(32, 0, 0), true).fits(3));
        assertFalse(new DroneSelection(origin, new BlockPos(32, 0, 1), true).fits(3));
        assertTrue(new DroneSelection(origin, new BlockPos(128, 0, 0), true).fits(4));
        assertFalse(new DroneSelection(origin, new BlockPos(129, 0, 0), true).fits(4));
        var huge = new DroneSelection(origin, new BlockPos(20000000, 300, 20000000), true);
        assertTrue(huge.fits(5));
        assertTrue(huge.contains(20000000, 20000000));
        assertFalse(huge.contains(29000000, 29000000));
    }
}
