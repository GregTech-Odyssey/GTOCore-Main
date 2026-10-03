package com.gtocore.api.wireless.energy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * 一个队伍在一颗星球（含其轨道）或一个异界维度里的电池与线路汇接点：本地存取不限电流，电压受本维度能源塔单元等级限制；routes 按跳数由近到远排列。
 * dry/full 是本 tick 远程已取空、已存满的负缓存；另挂着在此等待资源的端点与各优先级的缺额登记，plan* 是 FlowPlan 的本次计划量。
 */
public final class GridNode {

    static final Route[] NO_ROUTES = new Route[0];
    static final Arc[] NO_ARCS = new Arc[0];
    static final GridNode[] NO_NODES = new GridNode[0];
    static final GridNode EMPTY = new GridNode(null);

    @Nullable
    final ResourceKey<Level> dimension;
    long hi, lo;
    long capHi, capLo;
    int loss;
    int tier = -1;
    int reachTier = -1;
    Route[] routes = NO_ROUTES;
    Arc[] out = NO_ARCS;
    boolean linked;
    int index;
    int visited = -1;

    boolean awaitingRoom;
    GridNode[] dependents = NO_NODES;
    private final ObjectArrayList<EnergyPort> parked = new ObjectArrayList<>();
    private final long[] heldDraw = new long[GridScheduler.PRIORITIES];
    private final long[] heldPut = new long[GridScheduler.PRIORITIES];
    final NegativeCache dry = new NegativeCache();
    final NegativeCache full = new NegativeCache();

    long planStamp;
    long planCap;
    long planAmount;
    long planVisit;
    @Nullable
    Arc planParent;
    boolean planBackward;

    @Nullable
    NodeMeter meter;
    long sampledHi, sampledLo;
    int sampledTick = Integer.MIN_VALUE;
    float sampledDelta;

    GridNode(@Nullable ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Nullable
    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public int tier() {
        return tier;
    }

    public int reachTier() {
        return reachTier;
    }

    public BigInteger storage() {
        return U126.toBig(Math.max(0, hi), lo);
    }

    public BigInteger capacity() {
        return U126.toBig(capHi, capLo);
    }

    public double storageDouble() {
        return U126.toDouble(Math.max(0, hi), lo);
    }

    public double capacityDouble() {
        return U126.toDouble(capHi, capLo);
    }

    @Nullable
    public EnergyStats stats() {
        return meter == null ? null : meter.stats;
    }

    public boolean hasCapacity() {
        return capHi != 0 || capLo != 0;
    }

    boolean isEmpty() {
        return hi <= 0 && lo == 0;
    }

    void park(EnergyPort port) {
        port.cycle.parkSlot = parked.size();
        parked.add(port);
    }

    void unpark(EnergyPort port) {
        int slot = port.cycle.parkSlot;
        if (slot < 0 || slot >= parked.size() || parked.get(slot) != port) return;
        var tail = parked.remove(parked.size() - 1);
        if (tail != port) {
            parked.set(slot, tail);
            tail.cycle.parkSlot = slot;
        }
        port.cycle.parkSlot = -1;
    }

    void wakeParked() {
        while (!parked.isEmpty()) {
            var port = parked.remove(parked.size() - 1);
            port.cycle.parkSlot = -1;
            port.resume();
        }
    }

    void wakeDependents() {
        wakeParked();
        for (var node : dependents) node.wakeParked();
    }

    void hold(int priority, long draw, long put) {
        heldDraw[priority] += draw;
        heldPut[priority] += put;
    }

    long drawHeldAbove(int priority) {
        return above(heldDraw, priority);
    }

    long putHeldAbove(int priority) {
        return above(heldPut, priority);
    }

    private static long above(long[] held, int priority) {
        long sum = 0;
        for (int p = priority + 1; p < held.length; p++) sum = U126.saturatedAdd(sum, held[p]);
        return sum;
    }

    long available() {
        return hi > 0 ? U126.MASK : hi == 0 ? lo : 0;
    }

    long free() {
        return freeOf(hi, lo);
    }

    private long freeOf(long sHi, long sLo) {
        long dh = capHi - sHi;
        long dl = capLo - sLo;
        dh += dl >> 63;
        dl &= U126.MASK;
        if (dh < 0) return 0;
        return dh != 0 ? U126.MASK : dl;
    }

    void take(long amount) {
        long r = lo - amount;
        hi += r >> 63;
        lo = r & U126.MASK;
    }

    void add(long amount) {
        long s = lo + amount;
        hi += s >>> 63;
        lo = s & U126.MASK;
    }

    void subtract(long aHi, long aLo) {
        long r = lo - aLo;
        hi = hi - aHi + (r >> 63);
        lo = r & U126.MASK;
    }

    void addWide(long aHi, long aLo) {
        long s = lo + aLo;
        hi = U126.saturatedAdd(U126.saturatedAdd(hi, aHi), s >>> 63);
        lo = s & U126.MASK;
    }

    void addCapacity(long aHi, long aLo) {
        long s = capLo + aLo;
        capHi = U126.saturatedAdd(U126.saturatedAdd(capHi, aHi), s >>> 63);
        capLo = s & U126.MASK;
    }

    void clearStorage() {
        hi = 0;
        lo = 0;
    }
}
