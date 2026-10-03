package com.gtocore.api.wireless.energy;

import java.math.BigInteger;

final class Loss {

    static final int PERMILLE = 1000;
    static final int PERCENT = 10;

    private Loss() {}

    static int combined(int nodeLoss, int extraLoss) {
        return Math.min(PERMILLE, nodeLoss + extraLoss);
    }

    static long of(long gross, int permille) {
        if (permille <= 0) return 0;
        return (gross / PERMILLE) * permille + (gross % PERMILLE) * permille / PERMILLE;
    }

    static BigInteger netOf(BigInteger gross, int permille) {
        if (permille <= 0) return gross;
        return gross.subtract(gross.multiply(BigInteger.valueOf(permille)).divide(BigInteger.valueOf(PERMILLE)));
    }

    static long acceptGross(long free, long gross, int permille) {
        if (free <= 0 || permille >= PERMILLE) return 0;
        long net = gross - of(gross, permille);
        if (net <= free) return gross;
        int keep = PERMILLE - permille;
        long q = free / keep, r = free % keep;
        long g = q > U126.MASK / PERMILLE ? U126.MASK : q * PERMILLE + r * PERMILLE / keep;
        g = Math.min(g, gross);
        while (g > 0 && g - of(g, permille) > free) g--;
        return Math.max(0, g);
    }
}
