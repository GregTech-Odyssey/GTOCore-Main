package com.gtocore.api.wireless.energy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import org.jetbrains.annotations.Nullable;

/**
 * 两个维度之间一束中继的一个方向：电压取束内最高等级，功率为各中继"电流×电压"之和；两个方向互不占用。
 * 令牌桶限制逐 tick 传输，预约按结束时刻聚合、到期出堆；plan* 字段是 FlowPlan 的本次计划量。
 */
final class Arc {

    record Key(ResourceKey<Level> from, ResourceKey<Level> to) {}

    final GridNode from;
    final GridNode to;
    int tier;
    long budget;
    int index;
    Arc reverse;
    long planStamp;
    long planCap;
    long planFlow;
    long carriedHi, carriedLo;
    long sampledHi, sampledLo;
    int sampledTick = Integer.MIN_VALUE;
    float sampledFlow;

    private final TokenBucket bucket = new TokenBucket();
    private long reserved;
    private final Int2LongOpenHashMap reservedUntil = new Int2LongOpenHashMap();
    private final IntHeapPriorityQueue ends = new IntHeapPriorityQueue();
    private int nextExpiry = Integer.MAX_VALUE;

    private final long[] used = new long[GridScheduler.PRIORITIES];
    private final long[] recent = new long[GridScheduler.PRIORITIES];
    private final long[] above = new long[GridScheduler.PRIORITIES];
    @Nullable
    private Arc successor;
    private int usedTick = Integer.MIN_VALUE;

    Arc(GridNode from, GridNode to) {
        this.from = from;
        this.to = to;
    }

    public GridNode from() {
        return from;
    }

    public GridNode to() {
        return to;
    }

    public int tier() {
        return tier;
    }

    public long budget() {
        return budget;
    }

    public long reserved(int now) {
        expireUntil(now);
        return reserved;
    }

    private long rate(int now) {
        expireUntil(now);
        return budget - reserved;
    }

    long allowance(int now) {
        return bucket.allowance(rate(now), now);
    }

    void consume(long amount, int now, int priority) {
        bucket.allowance(rate(now), now);
        bucket.take(amount);
        roll(now);
        used[priority] = U126.saturatedAdd(used[priority], amount);
        long s = carriedLo + amount;
        carriedHi = U126.saturatedAdd(carriedHi, s >>> 63);
        carriedLo = s & U126.MASK;
    }

    double carriedDouble() {
        return U126.toDouble(carriedHi, carriedLo);
    }

    long usedAbove(int priority, int now) {
        roll(now);
        return above[priority];
    }

    private void roll(int now) {
        if (now == usedTick) return;
        int elapsed = now - usedTick;
        boolean live = elapsed > 0 && elapsed <= TokenBucket.BURST_TICKS;
        long sum = 0;
        for (int p = used.length - 1; p >= 0; p--) {
            above[p] = sum;
            recent[p] = live ? Math.max(used[p] >> (elapsed - 1), recent[p] >> elapsed) : 0;
            used[p] = 0;
            sum = U126.saturatedAdd(sum, recent[p]);
        }
        usedTick = now;
    }

    void inherit(Arc old, int now) {
        bucket.copyFrom(old.bucket);
        reserved = old.reserved;
        reservedUntil.putAll(old.reservedUntil);
        for (var it = old.reservedUntil.keySet().iterator(); it.hasNext();) ends.enqueue(it.nextInt());
        nextExpiry = old.nextExpiry;
        old.roll(now);
        System.arraycopy(old.recent, 0, recent, 0, recent.length);
        System.arraycopy(old.above, 0, above, 0, above.length);
        usedTick = now;
        carriedHi = old.carriedHi;
        carriedLo = old.carriedLo;
        sampledHi = old.sampledHi;
        sampledLo = old.sampledLo;
        sampledTick = old.sampledTick;
        sampledFlow = old.sampledFlow;
        old.successor = this;
    }

    boolean idle(int now) {
        return reserved(now) == 0;
    }

    void giveBack(long amount, int now) {
        if (successor != null) {
            successor.giveBack(amount, now);
            return;
        }
        bucket.allowance(rate(now), now);
        bucket.giveBack(amount, rate(now));
        long r = carriedLo - amount;
        long h = carriedHi + (r >> 63);
        carriedHi = Math.max(0, h);
        carriedLo = h < 0 ? 0 : r & U126.MASK;
    }

    long freeBudget(int now) {
        return Math.max(0, rate(now));
    }

    void reserve(long perTick, int until, int now) {
        if (perTick <= 0 || until <= now) return;
        bucket.allowance(rate(now), now);
        if (reservedUntil.addTo(until, perTick) == 0) ends.enqueue(until);
        reserved += perTick;
        if (until < nextExpiry) nextExpiry = until;
        bucket.clampTo(Math.max(0, budget - reserved));
    }

    void cancel(long perTick, int until, int now) {
        if (successor != null) {
            successor.cancel(perTick, until, now);
            return;
        }
        long old = reservedUntil.get(until);
        if (old <= 0) return;
        long removed = Math.min(old, perTick);
        if (old <= perTick) reservedUntil.remove(until);
        else reservedUntil.put(until, old - perTick);
        bucket.allowance(rate(now), now);
        reserved -= removed;
    }

    private void expireUntil(int now) {
        if (now < nextExpiry) return;
        while (!ends.isEmpty() && ends.firstInt() <= now) reserved -= reservedUntil.remove(ends.dequeueInt());
        nextExpiry = ends.isEmpty() ? Integer.MAX_VALUE : ends.firstInt();
        if (reserved < 0) reserved = 0;
    }

    void setCapacity(int tier, long budget) {
        this.tier = tier;
        this.budget = budget;
        bucket.clampTo(Math.max(0, budget - reserved));
    }
}
