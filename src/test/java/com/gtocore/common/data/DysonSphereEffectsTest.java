package com.gtocore.common.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DysonSphereEffectsTest {

    @Test
    void everyLaunchReducesDaylightLinearlyWithoutPercentageThresholds() {
        float previous = 1;
        for (int launched = 1; launched <= StarMapDysonSync.COMPLETE; launched++) {
            float daylight = DysonSphereEffects.transmission(launched, 1);
            assertEquals(1 - launched / (double) StarMapDysonSync.COMPLETE, daylight, 0.0000001);
            assertTrue(daylight < previous, "Each launch must reduce visual daylight before integer light levels change");
            assertEquals(1 - launched / (double) StarMapDysonSync.COMPLETE * 0.5,
                    DysonSphereEffects.transmission(launched, 0.5f), 0.0000001);
            previous = daylight;
        }
    }

    @ParameterizedTest
    @CsvSource({ "0, 1, 0", "6300, 0.937, 1", "50000, 0.5, 8", "100000, 0, 15" })
    void daylightFollowsLaunchProgress(int launched, float expectedTransmission, int expectedDarken) {
        float transmission = DysonSphereEffects.transmission(launched, 1);
        assertEquals(expectedTransmission, transmission, 0.000001f);
        assertEquals(expectedDarken, DysonSphereEffects.skyDarken(0, transmission));
    }

    @ParameterizedTest
    @CsvSource({ "0", "6300", "50000", "100000" })
    void nightRetainsItsOriginalBrightness(int launched) {
        float transmission = DysonSphereEffects.transmission(launched, 0);
        assertEquals(1, transmission);
        assertEquals(11, DysonSphereEffects.skyDarken(11, transmission));
    }

    @Test
    void twilightFadesAndWeatherNeverMakesCompleteDaylightLeak() {
        assertEquals(0.75f, DysonSphereEffects.transmission(50000, 0.5f));
        float fullShell = DysonSphereEffects.transmission(100000, 1);
        assertEquals(15, DysonSphereEffects.skyDarken(5, fullShell));
        assertEquals(15, DysonSphereEffects.skyDarken(9, fullShell));
    }
}
