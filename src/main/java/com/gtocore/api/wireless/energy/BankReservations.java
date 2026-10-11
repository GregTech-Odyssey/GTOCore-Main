package com.gtocore.api.wireless.energy;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;

final class BankReservations {

    private static final int BANK_BITS = 4;

    private final long[] reserved = new long[GridNode.BANKS];
    private final Long2LongOpenHashMap byEnd = new Long2LongOpenHashMap();
    private final LongHeapPriorityQueue ends = new LongHeapPriorityQueue();
    private int nextExpiry = Integer.MAX_VALUE;

    private static long key(int until, int bank) {
        return (long) until << BANK_BITS | bank;
    }

    long reserved(int bank, int now) {
        expire(now);
        return reserved[bank];
    }

    void reserve(int bank, long perTick, int until, int now) {
        if (perTick <= 0 || until <= now) return;
        expire(now);
        long key = key(until, bank);
        if (byEnd.addTo(key, perTick) == 0) ends.enqueue(key);
        reserved[bank] = U126.saturatedAdd(reserved[bank], perTick);
        if (until < nextExpiry) nextExpiry = until;
    }

    void cancel(int bank, long perTick, int until, int now) {
        expire(now);
        long key = key(until, bank);
        long old = byEnd.get(key);
        if (old <= 0) return;
        long removed = Math.min(old, perTick);
        if (old <= perTick) byEnd.remove(key);
        else byEnd.put(key, old - perTick);
        reserved[bank] = Math.max(0, reserved[bank] - removed);
    }

    private void expire(int now) {
        if (now < nextExpiry) return;
        while (!ends.isEmpty() && (int) (ends.firstLong() >>> BANK_BITS) <= now) {
            long key = ends.dequeueLong();
            int bank = (int) (key & ((1 << BANK_BITS) - 1));
            reserved[bank] = Math.max(0, reserved[bank] - byEnd.remove(key));
        }
        nextExpiry = ends.isEmpty() ? Integer.MAX_VALUE : (int) (ends.firstLong() >>> BANK_BITS);
    }
}
