package com.gtolib.drone.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DroneCaptureResolutionTest {

    @Test
    void hardwareLimitsApplyToScaledDimensionsOnBothAxes() {
        assertTrue(DroneCaptureResolution.SIXTEEN.fits(1024, 512, 16384, 8192));
        assertFalse(DroneCaptureResolution.SIXTEEN.fits(1025, 512, 16384, 8192));
        assertFalse(DroneCaptureResolution.SIXTEEN.fits(1024, 513, 16384, 8192));
        assertTrue(DroneCaptureResolution.SIXTY_FOUR.fits(128, 64, 8192, 4096));
        assertFalse(DroneCaptureResolution.SIXTY_FOUR.fits(129, 64, 8192, 4096));
        assertFalse(DroneCaptureResolution.SIXTY_FOUR.fits(128, 65, 8192, 4096));
    }

    @Test
    void imageAddressingLimitIsCheckedEvenIfTheGpuSupportsTheDimensions() {
        assertTrue(DroneCaptureResolution.ORIGINAL.fits(32767, 16384, 32768, 32768));
        assertFalse(DroneCaptureResolution.ORIGINAL.fits(32768, 16384, 32768, 32768));
        assertFalse(DroneCaptureResolution.SIXTY_FOUR.fits(Integer.MAX_VALUE, Integer.MAX_VALUE, 32768, 32768));
        assertFalse(DroneCaptureResolution.ORIGINAL.fits(0, 100, 32768, 32768));
    }

    @Test
    void warningsStartAtFourTimesResolution() {
        assertNull(DroneCaptureResolution.ORIGINAL.warning);
        assertNull(DroneCaptureResolution.DOUBLE.warning);
        assertNotNull(DroneCaptureResolution.QUADRUPLE.warning);
        assertNotNull(DroneCaptureResolution.SIXTEEN.warning);
        assertNotNull(DroneCaptureResolution.SIXTY_FOUR.warning);
    }
}
