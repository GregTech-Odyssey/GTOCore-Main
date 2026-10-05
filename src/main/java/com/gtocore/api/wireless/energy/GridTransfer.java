package com.gtocore.api.wireless.energy;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * 逐 tick 的取电与存电：本地节点不限电流，不够时经 FlowPlan 跨维度按最大流分配、受线路令牌限制。
 * 一次实际取（存）不满即记入节点的负缓存，本 tick 内同电压及以上不再重算；结果里注明是否有线路受限的成分。
 */
final class GridTransfer {

    static final class Outcome {

        boolean lineLimited;
    }

    private static final Outcome IGNORED = new Outcome();

    private GridTransfer() {}

    static long draw(EnergyAccount account, GridNode d, int voltage, long want, int priority, @Nullable Outcome outcome) {
        long got = Math.min(want, d.available(voltage));
        if (got > 0) take(account, d, voltage, got);
        boolean limited = false;
        if (got < want && d.routes.length != 0) {
            int now = GridClock.tick();
            if (d.dry.hit(now, account.supplyGen, voltage)) {
                limited = d.dry.limited;
            } else {
                long need = want - got;
                var plan = FlowPlan.SHARED;
                long remote = plan.draw(d, voltage, need, now);
                limited = plan.limited();
                plan.applyDraw(account, priority);
                if (remote < need) d.dry.mark(now, account.supplyGen, voltage, limited);
                got += remote;
            }
        }
        if (outcome != null) outcome.lineLimited = limited;
        if (got > 0) {
            account.meter.out(got);
            NodeMeter.out(account, d, got);
        }
        return got;
    }

    static long put(EnergyAccount account, GridNode d, int voltage, long gross, int extraLoss, int priority, @Nullable Outcome outcome) {
        var out = outcome != null ? outcome : IGNORED;
        out.lineLimited = false;
        long done = putLocal(account, d, voltage, gross, extraLoss);
        if (done < gross && d.routes.length != 0) {
            int now = GridClock.tick();
            if (d.full.hit(now, account.sinkGen, voltage)) {
                out.lineLimited = d.full.limited;
            } else {
                long rest = gross - done;
                var plan = FlowPlan.SHARED;
                long remote = plan.put(d, voltage, rest, extraLoss, -1, now);
                out.lineLimited = plan.limited();
                plan.applyPut(account, priority);
                if (remote < rest) d.full.mark(now, account.sinkGen, voltage, out.lineLimited);
                done += remote;
            }
        }
        return done;
    }

    private static long putLocal(EnergyAccount account, GridNode d, int voltage, long gross, int extraLoss) {
        if (voltage > d.tier) return 0;
        long done = d.acceptable(gross, extraLoss);
        if (done > 0) {
            long lost = store(account, d, done, extraLoss);
            account.meter.in(done, lost);
            NodeMeter.in(account, d, done, lost);
        }
        if (done < gross) d.awaitingRoom = true;
        return done;
    }

    static boolean putLump(EnergyAccount account, GridNode d, int voltage, BigInteger gross, int extraLoss, boolean apply, int priority) {
        if (gross.signum() <= 0) return true;
        var local = voltage <= d.tier ? d.lumpRoom(gross, extraLoss) : BigInteger.ZERO;
        var rest = gross.subtract(local);
        if (rest.bitLength() > 63) return false;
        long remote = rest.longValue();
        var plan = FlowPlan.SHARED;
        if (remote > 0 && (d.routes.length == 0 || plan.put(d, voltage, remote, extraLoss, priority, GridClock.tick()) < remote)) return false;
        if (!apply) return true;
        if (local.signum() > 0) {
            int stocked = d.stocked;
            var net = d.storeLump(local, extraLoss);
            arrived(account, d, stocked);
            double localGross = local.doubleValue(), lost = local.subtract(net).doubleValue();
            account.meter.in(localGross, lost);
            NodeMeter.in(account, d, localGross, lost);
        }
        if (remote > 0) {
            if (voltage <= d.tier) d.awaitingRoom = true;
            plan.applyPut(account, priority);
        }
        return true;
    }

    static void take(EnergyAccount account, GridNode node, int voltage, long amount) {
        node.take(voltage, amount);
        account.roomFreed(node);
    }

    static long store(EnergyAccount account, GridNode node, long gross, int extraLoss) {
        int stocked = node.stocked;
        long lost = node.store(gross, extraLoss);
        arrived(account, node, stocked);
        return lost;
    }

    static void arrived(EnergyAccount account, GridNode node, int stocked) {
        if ((node.stocked & ~stocked) != 0) account.supplyArrived(node);
    }
}
