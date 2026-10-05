package com.gtocore.api.wireless.energy;

/**
 * 整笔结算：本地先付，其余经 FlowPlan 按最大流分配；duration > 0 时线路按每 tick 功率预约整个时长，否则一次性占用令牌。
 * plan 只计算并写出收据，commit 照收据执行，refund 照收据原路退回（存量、令牌、预约）。
 */
final class Settlement {

    private Settlement() {}

    static SettleResult plan(EnergyAccount account, GridNode d, int voltage, long hi, long lo, int duration, int priority, SettleReceipt receipt) {
        receipt.clear();
        if (hi == 0 && lo == 0) return SettleResult.OK;
        if (voltage > d.reachTier) return SettleResult.NO_COVERAGE;
        receipt.origin = d;
        int now = GridClock.tick();
        receipt.restHi = hi;
        receipt.restLo = lo;
        takeBanks(d, voltage, hi, lo, receipt);
        if (!settled(receipt) && d.routes.length != 0) {
            if (duration > 0) reserveRemote(d, voltage, duration, priority, now, receipt);
            else tokenRemote(d, voltage, priority, now, receipt);
        }
        if (settled(receipt)) {
            receipt.priority = priority;
            receipt.reserve = duration > 0;
            receipt.until = duration > 0 ? (int) Math.min(Integer.MAX_VALUE, (long) now + duration) : now;
            return SettleResult.OK;
        }
        receipt.clear();
        receipt.reachable = reachableStorage(account, d, voltage);
        return receipt.reachable >= U126.toDouble(hi, lo) ? SettleResult.NO_TRANSFER : SettleResult.NO_STORAGE;
    }

    private static boolean settled(SettleReceipt receipt) {
        return receipt.restHi == 0 && receipt.restLo == 0;
    }

    private static void takeBanks(GridNode node, int voltage, long hi, long lo, SettleReceipt receipt) {
        for (int m = node.drawable(voltage); m != 0 && (hi != 0 || lo != 0); m &= m - 1) {
            int t = Integer.numberOfTrailingZeros(m);
            long th = node.bankHi[t], tl = node.bankLo[t];
            if (U126.compare(th, tl, hi, lo) > 0) {
                th = hi;
                tl = lo;
            }
            receipt.add(node, t, th, tl);
            receipt.consume(th, tl);
            long r = lo - tl;
            hi = hi - th + (r >> 63);
            lo = r & U126.MASK;
        }
    }

    private static void tokenRemote(GridNode d, int voltage, int priority, int now, SettleReceipt receipt) {
        if (receipt.restHi != 0) return;
        long need = receipt.restLo;
        var plan = FlowPlan.SHARED;
        if (plan.settle(d, voltage, need, 0, priority, now) < need) return;
        for (int i = 0, n = plan.arcCount(); i < n; i++) {
            var arc = plan.arc(i);
            if (arc.planFlow > 0) receipt.addArc(arc, arc.planFlow);
        }
        for (int i = 0, n = plan.nodeCount(); i < n; i++) {
            var node = plan.node(i);
            if (node.planAmount > 0) takeBanks(node, voltage, 0, node.planAmount, receipt);
        }
    }

    private static void reserveRemote(GridNode d, int voltage, int duration, int priority, int now, SettleReceipt receipt) {
        long perTick = U126.divCeil(receipt.restHi, receipt.restLo, duration);
        var plan = FlowPlan.SHARED;
        if (plan.settle(d, voltage, perTick, duration, priority, now) < perTick) return;
        for (int i = 0, n = plan.arcCount(); i < n; i++) {
            var arc = plan.arc(i);
            if (arc.planFlow > 0) receipt.addArc(arc, arc.planFlow);
        }
        for (int i = 0, n = plan.nodeCount(); i < n && !settled(receipt); i++) {
            var node = plan.node(i);
            if (node.planAmount <= 0) continue;
            long th = U126.mulHi(node.planAmount, duration), tl = U126.mulLo(node.planAmount, duration);
            if (U126.compare(th, tl, receipt.restHi, receipt.restLo) > 0) {
                th = receipt.restHi;
                tl = receipt.restLo;
            }
            takeBanks(node, voltage, th, tl, receipt);
        }
    }

    static double reachableStorage(EnergyAccount account, GridNode d, int voltage) {
        int stamp = account.nextVisit();
        double sum = d.storageDouble(voltage);
        for (var r : d.routes) {
            if (r.tier < voltage || r.source.visited == stamp) continue;
            r.source.visited = stamp;
            sum += r.source.storageDouble(voltage);
        }
        return sum;
    }

    static void commit(EnergyAccount account, SettleReceipt receipt) {
        int now = GridClock.tick();
        for (int i = 0; i < receipt.count; i++) {
            var node = receipt.nodes[i];
            node.withdraw(receipt.banks[i], receipt.his[i], receipt.los[i]);
            account.roomFreed(node);
        }
        for (int i = 0; i < receipt.arcCount; i++) {
            if (receipt.reserve) receipt.arcs[i].reserve(receipt.amounts[i], receipt.until, now);
            else receipt.arcs[i].consume(receipt.amounts[i], now, receipt.priority);
        }
        if (receipt.total > 0) {
            account.meter.out(receipt.total);
            NodeMeter.out(account, receipt.origin, receipt.total);
        }
    }

    static void refund(EnergyAccount account, SettleReceipt receipt) {
        int now = GridClock.tick();
        for (int i = 0; i < receipt.count; i++) {
            var node = receipt.nodes[i];
            if (account.nodes.get(node.dimension) == node && node.hasCapacity()) {
                node.refund(receipt.banks[i], receipt.his[i], receipt.los[i]);
            } else {
                account.addPending(receipt.his[i], receipt.los[i]);
                account.markDirty();
            }
        }
        for (int i = 0; i < receipt.arcCount; i++) {
            if (receipt.reserve) receipt.arcs[i].cancel(receipt.amounts[i], receipt.until, now);
            else receipt.arcs[i].giveBack(receipt.amounts[i], now);
        }
        if (receipt.total > 0) {
            account.meter.out(-receipt.total);
            NodeMeter.out(account, receipt.origin, -receipt.total);
        }
        for (int i = 0; i < receipt.count; i++) {
            account.supplyArrived(receipt.nodes[i]);
            account.sinkFreed(receipt.nodes[i]);
        }
        receipt.clear();
    }
}
