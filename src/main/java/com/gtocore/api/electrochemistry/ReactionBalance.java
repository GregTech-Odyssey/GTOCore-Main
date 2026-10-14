package com.gtocore.api.electrochemistry;

import java.math.BigInteger;

/** Exact integer coefficients and energy conversion; no floating-point rounding of charge. */
public record ReactionBalance(long firstMultiplier, long secondMultiplier) {

    public static ReactionBalance cross(long firstCharge, long secondCharge) {
        if (firstCharge == 0 || secondCharge == 0 || firstCharge == Long.MIN_VALUE || secondCharge == Long.MIN_VALUE || (firstCharge > 0) == (secondCharge > 0)) return null;
        return new ReactionBalance(Math.abs(secondCharge), Math.abs(firstCharge));
    }

    public static ReactionBalance fuel(long firstCharge, long secondCharge, boolean firstIsFuel, long efficiencyPercent) {
        var balance = cross(firstCharge, secondCharge);
        if (balance == null || efficiencyPercent <= 0) return null;
        long first = Math.multiplyExact(balance.firstMultiplier, firstIsFuel ? 100 : efficiencyPercent);
        long second = Math.multiplyExact(balance.secondMultiplier, firstIsFuel ? efficiencyPercent : 100);
        long divisor = gcd(first, second);
        return new ReactionBalance(first / divisor, second / divisor);
    }

    private static long gcd(long first, long second) {
        while (second != 0) {
            long next = first % second;
            first = second;
            second = next;
        }
        return first;
    }

    public static long storedAmount(long fuelEnergy, long fuelMultiplier, long efficiencyPercent, long density) {
        if (fuelEnergy <= Long.MAX_VALUE / fuelMultiplier && fuelEnergy * fuelMultiplier <= Long.MAX_VALUE / efficiencyPercent) {
            return fuelEnergy * fuelMultiplier * efficiencyPercent / density / 100;
        }
        var amount = energy(fuelEnergy, fuelMultiplier, efficiencyPercent).divide(BigInteger.valueOf(density)).divide(BigInteger.valueOf(100));
        return amount.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? 0 : amount.longValue();
    }

    public static long minimumStorageScale(long fuelEnergy, long fuelMultiplier, long efficiencyPercent, long density) {
        var energy = energy(fuelEnergy, fuelMultiplier, efficiencyPercent);
        var minimum = BigInteger.valueOf(density).multiply(BigInteger.valueOf(100));
        if (energy.compareTo(minimum) >= 0) return 1;
        return minimum.add(energy).subtract(BigInteger.ONE).divide(energy).longValueExact();
    }

    private static BigInteger energy(long fuelEnergy, long fuelMultiplier, long efficiencyPercent) {
        return BigInteger.valueOf(fuelEnergy).multiply(BigInteger.valueOf(fuelMultiplier)).multiply(BigInteger.valueOf(efficiencyPercent));
    }
}
