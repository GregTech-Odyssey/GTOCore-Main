/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.spark.SparkAttachable;

/**
 * Any Block or Block Entity with this capability can hold and receive mana from mana bursts.
 */
public interface ManaReceiver extends ManaEndpoint {

    Level getManaReceiverLevel();

    BlockPos getManaReceiverPos();

    /**
     * Gets the amount of mana currently in this block.
     */
    int getCurrentMana();

    default long getCurrentManaLong() {
        return getCurrentMana();
    }

    /**
     * Is this Mana Receiver is full? Being full means no mana bursts will be sent.
     */
    boolean isFull();

    /**
     * Called when this receiver receives mana.
     */
    void receiveMana(int mana);

    default void receiveManaLong(long mana) {
        while (mana != 0) {
            int step = (int) Math.max(-Integer.MAX_VALUE, Math.min(Integer.MAX_VALUE, mana));
            receiveMana(step);
            mana -= step;
        }
    }

    /**
     * Can this tile receive mana from bursts? Generally set to false for
     * implementations of ManaCollector.
     */
    boolean canReceiveManaFromBursts();

    @Override
    default long addMana(long amount, boolean simulate) {
        if (amount <= 0 || isFull()) {
            return 0;
        }
        long room = roomOf(this);
        if (room < 0) {
            if (simulate) {
                return amount;
            }
            long before = getCurrentManaLong();
            receiveManaLong(amount);
            return Math.max(0, Math.min(amount, getCurrentManaLong() - before));
        }
        long change = Math.min(amount, room);
        if (change > 0 && !simulate) {
            receiveManaLong(change);
        }
        return Math.max(0, change);
    }

    @Override
    default long removeMana(long amount, boolean simulate) {
        long change = Math.min(amount, getCurrentManaLong());
        if (change <= 0) {
            return 0;
        }
        if (!simulate) {
            receiveManaLong(-change);
        }
        return change;
    }

    private static long roomOf(ManaReceiver receiver) {
        if (receiver instanceof SparkAttachable attachable) {
            return Math.max(0, attachable.getAvailableSpaceForManaLong());
        }
        if (receiver instanceof ManaPool pool) {
            return Math.max(0, pool.getMaxManaLong() - pool.getCurrentManaLong());
        }
        if (receiver instanceof ManaCollector collector) {
            return Math.max(0, collector.getMaxMana() - collector.getCurrentManaLong());
        }
        return -1;
    }
}
