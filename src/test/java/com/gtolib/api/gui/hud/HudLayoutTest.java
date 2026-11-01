package com.gtolib.api.gui.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudLayoutTest {

    @Test
    void draggingEachCornerUsesTheCorrectDirectionAndFixesTheOppositeCorner() {
        for (int corner = 0; corner < 4; corner++) {
            var layout = new HudLayout();
            layout.update(1000, 800, 300, 250, 136, 102);
            boolean left = (corner & 1) == 0, top = (corner & 2) == 0;
            int x = left ? 301 : 434, y = top ? 251 : 350;
            assertTrue(layout.beginResize(x, y));
            layout.dragResize(x + (left ? -68 : 68), y + (top ? -51 : 51), 1000, 800);
            assertEquals(204, layout.bounds.getWidth());
            assertEquals(153, layout.bounds.getHeight());
            assertEquals(left ? 436 : 300, layout.bounds.getX() + (left ? layout.bounds.getWidth() : 0));
            assertEquals(top ? 352 : 250, layout.bounds.getY() + (top ? layout.bounds.getHeight() : 0));
            layout.endResize();
            assertFalse(layout.isResizing());
        }
    }

    @Test
    void savedLayoutRestoresGeometryAndPointerCoordinates() {
        var layout = new HudLayout();
        layout.update(800, 600, 20, 30, 190, 140);
        layout.resizeFromCorner(1.75, 20, 30, false, false, 800, 600);
        layout.move(147, 211, 800, 600);
        var restored = new HudLayout();
        restored.load(layout.requestedScale(), layout.relativeX(), layout.relativeY());
        restored.update(800, 600, 0, 0, 190, 140);
        assertEquals(layout.bounds.getX(), restored.bounds.getX());
        assertEquals(layout.bounds.getY(), restored.bounds.getY());
        assertEquals(layout.bounds.getWidth(), restored.bounds.getWidth());
        assertEquals(layout.bounds.getHeight(), restored.bounds.getHeight());
        assertEquals(40, restored.localX(restored.screenX(40)), 0.0001);
        assertEquals(32, restored.localY(restored.screenY(32)), 0.0001);
        assertFalse(restored.beginResize(restored.screenX(40), restored.screenY(32)));
    }

    @Test
    void changingContentSizeAndWindowSizeKeepsTheSavedRelativeAnchorAndScale() {
        var layout = new HudLayout();
        layout.load(2, 100, 50);
        layout.update(800, 600, 0, 0, 160, 100);
        assertEquals(480, layout.bounds.getX());
        assertEquals(200, layout.bounds.getY());
        layout.update(100, 80, 0, 0, 160, 100);
        assertEquals(100, layout.bounds.getWidth());
        assertEquals(0, layout.bounds.getX());
        layout.update(800, 600, 0, 0, 200, 150);
        assertEquals(2, layout.scale, 0.0001);
        assertEquals(400, layout.bounds.getX());
        assertEquals(150, layout.bounds.getY());
    }

    @Test
    void anUnavailablePanelDoesNotExposeResizeHandles() {
        var layout = new HudLayout();
        layout.update(320, 240, 0, 0, 0, 0);
        assertFalse(layout.beginResize(0, 0));
    }
}
