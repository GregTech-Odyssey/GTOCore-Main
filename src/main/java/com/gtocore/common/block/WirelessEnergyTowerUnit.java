package com.gtocore.common.block;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

public interface WirelessEnergyTowerUnit {

    int getTier();

    long getAmperage();

    default int getIoTier() {
        return Math.min(getTier() + 1, GTValues.MAX);
    }

    default long getThroughput() {
        long voltage = GTValues.V[getIoTier()];
        long amperage = getAmperage();
        return amperage > Long.MAX_VALUE / voltage ? Long.MAX_VALUE : voltage * amperage;
    }

    default Block towerBlock() {
        return (Block) this;
    }

    record BlockData(@Nullable WirelessEnergyTowerUnit unit, BlockPos pos) {}
}
