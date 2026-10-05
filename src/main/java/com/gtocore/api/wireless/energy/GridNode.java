package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 一个队伍在一颗星球（含其轨道）或一个异界维度里的电池与线路汇接点：本地存取不限电流，电压受本维度能源塔单元等级限制；routes 按跳数由近到远排列。
 * dry/full 是本 tick 远程已取空、已存满的负缓存；另挂着在此等待资源的端点与各优先级的缺额登记，plan* 是 FlowPlan 的本次计划量。
 */
public final class GridNode {

    static final int BANKS = GTValues.MAX + 1;
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
    int banks;
    int stocked;
    final long[] bankHi = new long[BANKS];
    final long[] bankLo = new long[BANKS];
    final long[] bankCapHi = new long[BANKS];
    final long[] bankCapLo = new long[BANKS];
    final int[] bankLoss = new int[BANKS];
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private long legacyHi, legacyLo;
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
        return stocked == 0;
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

    int drawable(int voltage) {
        int m = banks & stocked;
        if (voltage <= 0) return m;
        return voltage >= BANKS ? 0 : m & (-1 << voltage);
    }

    long available(int voltage) {
        long sum = 0;
        for (int m = drawable(voltage); m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            if (bankHi[t] != 0) return U126.MASK;
            sum += bankLo[t];
            if (sum < 0) return U126.MASK;
        }
        return sum;
    }

    double storageDouble(int voltage) {
        double sum = 0;
        for (int m = drawable(voltage); m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            sum += U126.toDouble(bankHi[t], bankLo[t]);
        }
        return sum;
    }

    long reservable(int voltage, int duration) {
        long sh = 0, sl = 0;
        for (int m = drawable(voltage); m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            long s = sl + bankLo[t];
            sh = U126.saturatedAdd(U126.saturatedAdd(sh, bankHi[t]), s >>> 63);
            sl = s & U126.MASK;
        }
        return U126.divCeil(sh, sl, duration);
    }

    void take(int voltage, long amount) {
        for (int m = drawable(voltage); m != 0 && amount > 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            long part = U126.minClamp(bankHi[t], bankLo[t], amount);
            debit(t, 0, part);
            amount -= part;
        }
    }

    void withdraw(int bank, long aHi, long aLo) {
        debit(bank, aHi, aLo);
    }

    long acceptable(long gross, int extraLoss) {
        long left = gross;
        for (int m = banks; m != 0 && left > 0;) {
            int t = 31 - Integer.numberOfLeadingZeros(m);
            m ^= 1 << t;
            left -= Loss.acceptGross(bankFree(t), left, Loss.combined(bankLoss[t], extraLoss));
        }
        return gross - left;
    }

    long store(long gross, int extraLoss) {
        long left = gross, lost = 0;
        for (int m = banks; m != 0 && left > 0;) {
            int t = 31 - Integer.numberOfLeadingZeros(m);
            m ^= 1 << t;
            int permille = Loss.combined(bankLoss[t], extraLoss);
            long g = Loss.acceptGross(bankFree(t), left, permille);
            if (g <= 0) continue;
            long l = Loss.of(g, permille);
            credit(t, 0, g - l);
            lost += l;
            left -= g;
        }
        if (left > 0) {
            int t = Math.max(0, tier);
            long l = Loss.of(left, Loss.combined(bankLoss[t], extraLoss));
            credit(t, 0, left - l);
            lost += l;
        }
        return lost;
    }

    BigInteger lumpRoom(BigInteger gross, int extraLoss) {
        return lump(gross, extraLoss, false);
    }

    BigInteger storeLump(BigInteger gross, int extraLoss) {
        return lump(gross, extraLoss, true);
    }

    private BigInteger lump(BigInteger gross, int extraLoss, boolean apply) {
        var left = gross;
        var net = BigInteger.ZERO;
        for (int m = banks; m != 0 && left.signum() > 0;) {
            int t = 31 - Integer.numberOfLeadingZeros(m);
            m ^= 1 << t;
            int permille = Loss.combined(bankLoss[t], extraLoss);
            if (permille >= Loss.PERMILLE) continue;
            var free = U126.toBig(bankCapHi[t], bankCapLo[t]).subtract(U126.toBig(bankHi[t], bankLo[t]));
            if (free.signum() <= 0) continue;
            var g = left.min(free.multiply(BigInteger.valueOf(Loss.PERMILLE)).divide(BigInteger.valueOf(Loss.PERMILLE - permille)));
            while (g.signum() > 0 && Loss.netOf(g, permille).compareTo(free) > 0) g = g.subtract(BigInteger.ONE);
            if (g.signum() <= 0) continue;
            if (apply) {
                var n = Loss.netOf(g, permille);
                credit(t, U126.hi(n), U126.lo(n));
                net = net.add(n);
            }
            left = left.subtract(g);
        }
        return apply ? net : gross.subtract(left);
    }

    void absorb(long aHi, long aLo, @Nullable EnergyAccount overflow) {
        for (int m = banks; m != 0 && (aHi != 0 || aLo != 0);) {
            int t = 31 - Integer.numberOfLeadingZeros(m);
            m ^= 1 << t;
            long fl = bankCapLo[t] - bankLo[t];
            long fh = bankCapHi[t] - bankHi[t] + (fl >> 63);
            fl &= U126.MASK;
            if (fh < 0 || fh == 0 && fl == 0) continue;
            if (U126.compare(fh, fl, aHi, aLo) > 0) {
                fh = aHi;
                fl = aLo;
            }
            credit(t, fh, fl);
            long r = aLo - fl;
            aHi = aHi - fh + (r >> 63);
            aLo = r & U126.MASK;
        }
        if (aHi == 0 && aLo == 0) return;
        if (overflow != null) overflow.addPending(aHi, aLo);
        else credit(Math.max(0, tier), aHi, aLo);
    }

    void refund(int bank, long aHi, long aLo) {
        if ((banks & (1 << bank)) != 0) credit(bank, aHi, aLo);
        else absorb(aHi, aLo, null);
    }

    void load(int bank, long aHi, long aLo) {
        credit(bank, aHi, aLo);
    }

    void moveTo(GridNode target) {
        for (int m = stocked; m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            target.credit(t, bankHi[t], bankLo[t]);
        }
        clearStorage();
    }

    void spill(EnergyAccount account) {
        if (banks == 0) return;
        long eh = 0, el = 0;
        for (int m = stocked; m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            long dl = bankLo[t] - bankCapLo[t];
            long dh = bankHi[t] - bankCapHi[t] + (dl >> 63);
            dl &= U126.MASK;
            if (dh < 0 || dh == 0 && dl == 0) continue;
            debit(t, dh, dl);
            long s = el + dl;
            eh = U126.saturatedAdd(U126.saturatedAdd(eh, dh), s >>> 63);
            el = s & U126.MASK;
        }
        if (eh != 0 || el != 0) absorb(eh, el, account);
    }

    void trim() {
        for (int m = stocked; m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            long dl = bankLo[t] - bankCapLo[t];
            long dh = bankHi[t] - bankCapHi[t] + (dl >> 63);
            dl &= U126.MASK;
            if (dh >= 0 && (dh != 0 || dl != 0)) debit(t, dh, dl);
        }
    }

    long[] snapshot() {
        var saved = new long[BANKS << 1];
        for (int t = 0; t < BANKS; t++) {
            saved[t << 1] = bankHi[t];
            saved[t << 1 | 1] = bankLo[t];
        }
        return saved;
    }

    void rollback(long[] saved) {
        clearStorage();
        for (int t = 0; t < BANKS; t++) {
            if (saved[t << 1] != 0 || saved[t << 1 | 1] != 0) credit(t, saved[t << 1], saved[t << 1 | 1]);
        }
    }

    void clearStorage() {
        Arrays.fill(bankHi, 0);
        Arrays.fill(bankLo, 0);
        hi = 0;
        lo = 0;
        stocked = 0;
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    void addLegacy(long aHi, long aLo) {
        long s = legacyLo + aLo;
        legacyHi = U126.saturatedAdd(U126.saturatedAdd(legacyHi, aHi), s >>> 63);
        legacyLo = s & U126.MASK;
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    void spreadLegacy() {
        if (legacyHi == 0 && legacyLo == 0) return;
        var total = U126.toBig(legacyHi, legacyLo);
        legacyHi = 0;
        legacyLo = 0;
        if (banks == 0) {
            credit(0, U126.hi(total), U126.lo(total));
            return;
        }
        var capacity = capacity();
        var left = total;
        for (int m = banks; m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            var share = (m & (m - 1)) == 0 ? left : total.multiply(U126.toBig(bankCapHi[t], bankCapLo[t])).divide(capacity);
            credit(t, U126.hi(share), U126.lo(share));
            left = left.subtract(share);
        }
    }

    void clearCapacity() {
        Arrays.fill(bankCapHi, 0);
        Arrays.fill(bankCapLo, 0);
        Arrays.fill(bankLoss, 0);
        capHi = 0;
        capLo = 0;
        banks = 0;
        tier = -1;
        loss = 0;
    }

    void addCapacity(int bank, long aHi, long aLo) {
        long s = bankCapLo[bank] + aLo;
        bankCapHi[bank] = U126.saturatedAdd(U126.saturatedAdd(bankCapHi[bank], aHi), s >>> 63);
        bankCapLo[bank] = s & U126.MASK;
        s = capLo + aLo;
        capHi = U126.saturatedAdd(U126.saturatedAdd(capHi, aHi), s >>> 63);
        capLo = s & U126.MASK;
    }

    void setLoss(int bank, int permille) {
        bankLoss[bank] = permille;
    }

    void finishCapacity() {
        int mask = 0;
        for (int t = 0; t < BANKS; t++) {
            if (bankCapHi[t] != 0 || bankCapLo[t] != 0) mask |= 1 << t;
        }
        banks = mask;
        tier = mask == 0 ? -1 : 31 - Integer.numberOfLeadingZeros(mask);
        double c = capacityDouble();
        loss = c > 0 ? (int) Math.min(Loss.PERMILLE, Math.round(lossWeight() / c)) : 0;
    }

    double lossWeight() {
        double sum = 0;
        for (int m = banks; m != 0; m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            sum += U126.toDouble(bankCapHi[t], bankCapLo[t]) * bankLoss[t];
        }
        return sum;
    }

    private long bankFree(int t) {
        long dl = bankCapLo[t] - bankLo[t];
        long dh = bankCapHi[t] - bankHi[t] + (dl >> 63);
        if (dh < 0) return 0;
        return dh != 0 ? U126.MASK : dl & U126.MASK;
    }

    private void credit(int t, long aHi, long aLo) {
        long s = bankLo[t] + aLo;
        bankHi[t] = U126.saturatedAdd(U126.saturatedAdd(bankHi[t], aHi), s >>> 63);
        bankLo[t] = s & U126.MASK;
        s = lo + aLo;
        hi = U126.saturatedAdd(U126.saturatedAdd(hi, aHi), s >>> 63);
        lo = s & U126.MASK;
        mark(t);
    }

    private void debit(int t, long aHi, long aLo) {
        if (U126.compare(aHi, aLo, bankHi[t], bankLo[t]) > 0) {
            aHi = bankHi[t];
            aLo = bankLo[t];
        }
        long r = bankLo[t] - aLo;
        bankHi[t] = bankHi[t] - aHi + (r >> 63);
        bankLo[t] = r & U126.MASK;
        r = lo - aLo;
        long h = hi - aHi + (r >> 63);
        hi = Math.max(0, h);
        lo = h < 0 ? 0 : r & U126.MASK;
        mark(t);
    }

    private void mark(int t) {
        if (bankHi[t] != 0 || bankLo[t] != 0) stocked |= 1 << t;
        else stocked &= ~(1 << t);
    }
}
