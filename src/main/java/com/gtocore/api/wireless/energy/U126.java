package com.gtocore.api.wireless.energy;

import java.math.BigInteger;

/**
 * 126 位非负整数的双 long 表示：值 = hi·2^63 + lo，0 ≤ lo < 2^63，0 ≤ hi < 2^63。
 */
public final class U126 {

    public static final long MASK = Long.MAX_VALUE;
    private static final BigInteger BIG_MASK = BigInteger.valueOf(MASK);
    private static final double TWO_63 = 9.223372036854775808E18;

    private U126() {}

    public static BigInteger toBig(long hi, long lo) {
        if (hi == 0) return BigInteger.valueOf(lo);
        return BigInteger.valueOf(hi).shiftLeft(63).or(BigInteger.valueOf(lo));
    }

    public static long hi(BigInteger value) {
        if (value.signum() <= 0) return 0;
        if (value.bitLength() > 126) return MASK;
        return value.shiftRight(63).longValue();
    }

    public static long lo(BigInteger value) {
        if (value.signum() <= 0) return 0;
        if (value.bitLength() > 126) return MASK;
        return value.and(BIG_MASK).longValue();
    }

    public static long clamp(long hi, long lo) {
        return hi != 0 ? MASK : lo;
    }

    public static int compare(long aHi, long aLo, long bHi, long bLo) {
        if (aHi != bHi) return aHi < bHi ? -1 : 1;
        return Long.compare(aLo, bLo);
    }

    public static long mulHi(long a, long b) {
        return (Math.multiplyHigh(a, b) << 1) | ((a * b) >>> 63);
    }

    public static long mulLo(long a, long b) {
        return (a * b) & MASK;
    }

    public static double toDouble(long hi, long lo) {
        return hi * TWO_63 + lo;
    }

    public static double difference(long aHi, long aLo, long bHi, long bLo) {
        long dl = aLo - bLo;
        long dh = aHi - bHi + (dl >> 63);
        return dh < 0 ? 0 : toDouble(dh, dl & MASK);
    }

    public static double signedDifference(long aHi, long aLo, long bHi, long bLo) {
        return compare(aHi, aLo, bHi, bLo) >= 0 ? difference(aHi, aLo, bHi, bLo) : -difference(bHi, bLo, aHi, aLo);
    }

    public static long saturatedMul(long a, long b) {
        long hi = Math.multiplyHigh(a, b);
        long lo = a * b;
        if (hi != 0 || lo < 0) return MASK;
        return lo;
    }

    public static long saturatedAdd(long a, long b) {
        long r = a + b;
        return r < 0 ? MASK : r;
    }

    public static long divCeil(long hi, long lo, int divisor) {
        if (hi == 0) {
            long q = lo / divisor;
            return lo % divisor == 0 ? q : q + 1;
        }
        long upper = hi >>> 1;
        long lower = (hi << 63) | lo;
        long q3 = (upper >>> 32) / divisor;
        long rem = (upper >>> 32) % divisor;
        long cur = rem << 32 | (upper & 0xFFFFFFFFL);
        long q2 = cur / divisor;
        rem = cur % divisor;
        cur = rem << 32 | lower >>> 32;
        long q1 = cur / divisor;
        rem = cur % divisor;
        cur = rem << 32 | (lower & 0xFFFFFFFFL);
        long q0 = cur / divisor;
        rem = cur % divisor;
        if (q3 != 0 || q2 != 0 || q1 >>> 31 != 0) return MASK;
        long q = q1 << 32 | q0;
        return rem == 0 ? q : saturatedAdd(q, 1);
    }

    public static long minClamp(long hi, long lo, long cap) {
        return hi != 0 || lo > cap ? cap : lo;
    }
}
