package com.gtocore.client.renderer.sky;

import com.gtocore.client.screen.starmap.base.DysonShell;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DysonShellTest {

    @ParameterizedTest
    @CsvSource({ "1, 0", "6300, 12345", "50000, 45000", "100000, 89999" })
    void mapConstructionRemainsDeterministicAndClearsOnRemoval(int launched, long millis) throws ReflectiveOperationException {
        var map = new DysonShell();
        var repeated = new DysonShell();
        map.prepare(launched, millis);
        repeated.prepare(launched, millis);
        for (var hemisphere : new String[] { "front", "back" }) {
            var field = DysonShell.class.getDeclaredField(hemisphere);
            field.setAccessible(true);
            var mapPixels = (int[]) field.get(map);
            var repeatedPixels = (int[]) field.get(repeated);
            assertArrayEquals(mapPixels, repeatedPixels, "The same progress and animation phase must keep the same map cells");
            for (int i = 0; i < mapPixels.length; i++) {
                if (launched == 100000) assertTrue((mapPixels[i] >>> 24) > 0, "A complete shell must have no gaps");
            }
            repeated.prepare(0, millis);
            for (int color : repeatedPixels) assertEquals(0, color, "Removing the shell must clear its cached pixels");
            repeated.prepare(launched, millis);
        }
    }
}
