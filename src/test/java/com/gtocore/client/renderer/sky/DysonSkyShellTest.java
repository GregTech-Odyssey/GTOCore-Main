package com.gtocore.client.renderer.sky;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DysonSkyShellTest {

    @Test
    void vanillaMaskFitsTheBrightEightTexelSunCore() {
        assertEquals(8.25f, DysonSkyShell.maskHalf(30 * 8 / 32f), 0.000001f);
    }

    @Test
    void shrinkingTheMaskStillKeepsHexSidesAtFifteenPercentOfTheirOldSize() {
        for (float starHalf : new float[] { 6, 20, 24 }) {
            for (float coreHalf : new float[] { 7.5f, starHalf }) {
                float oldHalf = starHalf * 40 / 24;
                float sideRatio = DysonSkyShell.maskHalf(coreHalf) / oldHalf / DysonSkyShell.gridScale(starHalf, coreHalf);
                assertEquals(0.15f, sideRatio, 0.000001f);
            }
        }
    }
}
