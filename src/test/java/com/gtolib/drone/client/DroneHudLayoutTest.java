package com.gtolib.drone.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneHudLayoutTest {

    @Test
    void allPanelsFitSmallWindowsWithoutInvertedClampBounds() {
        for (int kind = 0; kind < 4; kind++) {
            var layout = new DroneHudLayout(kind);
            for (int width : new int[] { 1, 80, 147, 200, 320, 854 }) {
                for (int height : new int[] { 1, 40, 80, 120, 240, 480 }) {
                    layout.update(width, height);
                    assertInsideWindow(layout, width, height);
                    layout.resizeFromCorner(3, 0, 0, false, false, width, height);
                    assertInsideWindow(layout, width, height);
                    layout.move(-11, -11, width, height);
                    assertEquals(0, layout.bounds.getX());
                    assertEquals(0, layout.bounds.getY());
                    layout.move(width + 100, height + 100, width, height);
                    assertEquals(width, layout.bounds.getX() + layout.bounds.getWidth());
                    assertEquals(height, layout.bounds.getY() + layout.bounds.getHeight());
                }
            }
        }
    }

    @Test
    void resizingScalesContentAndPreservesTheChosenPosition() {
        var layout = new DroneHudLayout(0);
        layout.move(40, 30, 1000, 800);
        layout.resizeFromCorner(1.5, 40, 30, false, false, 1000, 800);
        assertEquals(40, layout.bounds.getX());
        assertEquals(30, layout.bounds.getY());
        assertEquals(204, layout.bounds.getWidth());
        assertEquals(153, layout.bounds.getHeight());
        assertEquals(70, layout.screenX(20));
        assertEquals(60, layout.screenY(20));
        assertEquals(20, layout.localX(70), 0.0001);
        assertEquals(20, layout.localY(60), 0.0001);
    }

    @Test
    void temporaryWindowFitRestoresTheRequestedScaleAndPosition() {
        var layout = new DroneHudLayout(3);
        layout.move(40, 30, 1000, 800);
        layout.resizeFromCorner(2, 40, 30, false, false, 1000, 800);
        layout.update(100, 40);
        assertInsideWindow(layout, 100, 40);
        assertEquals(0.5, layout.scale, 0.0001);
        layout.update(1000, 800);
        assertEquals(2, layout.scale, 0.0001);
        assertEquals(400, layout.bounds.getWidth());
        assertEquals(102, layout.bounds.getHeight());
        assertEquals(40, layout.bounds.getX());
        assertEquals(30, layout.bounds.getY());
    }

    @Test
    void independentPanelsKeepTheirOwnScaleAndLimitExtremeInput() {
        var mode = new DroneHudLayout(0);
        var speed = new DroneHudLayout(1);
        mode.update(2000, 2000);
        speed.update(2000, 2000);
        mode.resizeFromCorner(0.01, 0, 0, false, false, 2000, 2000);
        speed.resizeFromCorner(100, 0, 0, false, false, 2000, 2000);
        assertEquals(34, mode.bounds.getWidth());
        assertEquals(408, speed.bounds.getWidth());
        mode.resizeFromCorner(1, 0, 0, false, false, 2000, 2000);
        assertEquals(136, mode.bounds.getWidth());
        assertEquals(408, speed.bounds.getWidth());
    }

    @Test
    void draggingAnyCornerKeepsTheOppositeCornerFixed() {
        for (int corner = 0; corner < 4; corner++) {
            var layout = new DroneHudLayout(0);
            layout.move(300, 250, 1000, 800);
            boolean left = (corner & 1) == 0, top = (corner & 2) == 0;
            int anchorX = left ? 436 : 300, anchorY = top ? 352 : 250;
            for (double scale : new double[] { 1.5, 0.5, 3 }) {
                layout.resizeFromCorner(scale, anchorX, anchorY, left, top, 1000, 800);
                assertEquals(anchorX, layout.bounds.getX() + (left ? layout.bounds.getWidth() : 0));
                assertEquals(anchorY, layout.bounds.getY() + (top ? layout.bounds.getHeight() : 0));
                assertEquals(Math.round(136 * scale), layout.bounds.getWidth());
                assertEquals(Math.round(102 * scale), layout.bounds.getHeight());
                assertInsideWindow(layout, 1000, 800);
            }
        }
    }

    @Test
    void cornerResizeStopsAtWindowEdgeWithoutMovingTheAnchor() {
        var layout = new DroneHudLayout(0);
        layout.move(50, 30, 320, 240);
        layout.resizeFromCorner(3, 50, 30, false, false, 320, 240);
        assertEquals(50, layout.bounds.getX());
        assertEquals(30, layout.bounds.getY());
        assertEquals(320, layout.bounds.getX() + layout.bounds.getWidth());
        assertInsideWindow(layout, 320, 240);
        layout.update(320, 240);
        assertEquals(50, layout.bounds.getX());
        assertEquals(30, layout.bounds.getY());
        assertInsideWindow(layout, 320, 240);
    }

    private static void assertInsideWindow(DroneHudLayout layout, int width, int height) {
        var bounds = layout.bounds;
        assertTrue(bounds.getWidth() > 0);
        assertTrue(bounds.getHeight() > 0);
        assertTrue(bounds.getX() >= 0);
        assertTrue(bounds.getY() >= 0);
        assertTrue(bounds.getX() + bounds.getWidth() <= width);
        assertTrue(bounds.getY() + bounds.getHeight() <= height);
    }
}
