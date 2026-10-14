package com.gtocore.common.block;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

public interface WirelessEnergyTowerUnit {

    int getTier();

    long getAmperage();

    default boolean isSuperUnit() {
        return false;
    }

    /** Twice the additive multiplier, so UHV's 2.5 is represented exactly. */
    default int getSuperMultiplierHalf() {
        if (!isSuperUnit()) return 0;
        return superMultiplierHalf(getTier());
    }

    static int superMultiplierHalf(int tier) {
        return switch (tier) {
            case GTValues.UHV -> 5;
            case GTValues.UEV -> 20;
            case GTValues.UIV -> 80;
            case GTValues.UXV -> 320;
            case GTValues.OpV -> 1280;
            case GTValues.MAX -> 15000;
            default -> 0;
        };
    }

    default long getThroughput() {
        if (isSuperUnit()) return 0;
        long voltage = GTValues.V[getTier()];
        long amperage = getAmperage();
        return amperage > Long.MAX_VALUE / voltage ? Long.MAX_VALUE : voltage * amperage;
    }

    static BigInteger boostCapacity(BigInteger capacity, int multiplierHalf) {
        return multiplierHalf == 0 ? capacity : capacity.multiply(BigInteger.valueOf(multiplierHalf)).shiftRight(1);
    }

    static long boostThroughput(long throughput, int multiplierHalf) {
        if (multiplierHalf == 0) return throughput;
        var boosted = boostCapacity(BigInteger.valueOf(throughput), multiplierHalf);
        return boosted.bitLength() > 63 ? Long.MAX_VALUE : boosted.longValue();
    }

    default Block towerBlock() {
        return (Block) this;
    }

    record BlockData(@Nullable WirelessEnergyTowerUnit unit, BlockPos pos) {}
}
