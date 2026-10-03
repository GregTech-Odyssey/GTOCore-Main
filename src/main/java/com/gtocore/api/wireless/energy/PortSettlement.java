package com.gtocore.api.wireless.energy;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * 端点的整笔结算：先让位给同维度更高优先级、因存量不足而缺电的端点，再交给 Settlement 计划；同 tick 同条件的失败直接复用。
 */
final class PortSettlement {

    private static final SettleReceipt CHECK = new SettleReceipt();

    private final SettleReceipt receipt = new SettleReceipt();
    private int failTick = Integer.MIN_VALUE, failGen, failDuration, failTier;
    private long failHi, failLo;
    private SettleResult failResult = SettleResult.OK;
    private double failStorage;

    SettleResult check(EnergyAccount account, GridNode node, int priority, long hi, long lo, int voltageTier, int duration) {
        var result = plan(account, node, priority, hi, lo, voltageTier, duration, CHECK);
        CHECK.clear();
        return result;
    }

    SettleResult settle(EnergyAccount account, GridNode node, int priority, long hi, long lo, int voltageTier, int duration) {
        var result = plan(account, node, priority, hi, lo, voltageTier, duration, receipt);
        if (result.ok()) Settlement.commit(account, receipt);
        return result;
    }

    double refund(EnergyAccount account) {
        if (receipt.count == 0) return 0;
        double total = receipt.total;
        if (account != EnergyAccount.NONE) Settlement.refund(account, receipt);
        receipt.clear();
        return total;
    }

    @Nullable
    static long[] wide(BigInteger amount) {
        if (amount.signum() <= 0 || amount.bitLength() > 126) return null;
        return new long[] { U126.hi(amount), U126.lo(amount) };
    }

    static SettleResult trivial(BigInteger amount) {
        return amount.signum() <= 0 ? SettleResult.OK : SettleResult.NO_STORAGE;
    }

    void forget() {
        receipt.clear();
    }

    double shortfallStorage() {
        return failStorage;
    }

    private SettleResult plan(EnergyAccount account, GridNode node, int priority, long hi, long lo, int voltageTier, int duration, SettleReceipt target) {
        target.clear();
        if (account == EnergyAccount.NONE) return SettleResult.NO_ACCOUNT;
        duration = Math.max(0, duration);
        int now = GridClock.tick();
        if (failTick == now && failGen == account.supplyGen && failDuration == duration && voltageTier >= failTier && U126.compare(hi, lo, failHi, failLo) >= 0) return failResult;
        var result = yields(account, node, priority, hi, lo, voltageTier) ? SettleResult.RESERVED : Settlement.plan(account, node, voltageTier, hi, lo, duration, priority, target);
        if (result.ok()) return result;
        failTick = now;
        failGen = account.supplyGen;
        failDuration = duration;
        failTier = voltageTier;
        failHi = hi;
        failLo = lo;
        failResult = result;
        failStorage = result == SettleResult.NO_STORAGE ? target.reachable : 0;
        return result;
    }

    private static boolean yields(EnergyAccount account, GridNode node, int priority, long hi, long lo, int voltageTier) {
        long held = node.drawHeldAbove(priority);
        return held > 0 && Settlement.reachableStorage(account, node, voltageTier) - held < U126.toDouble(hi, lo);
    }
}
