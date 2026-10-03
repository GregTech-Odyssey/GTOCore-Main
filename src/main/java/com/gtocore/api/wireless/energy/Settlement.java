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
        if (voltage <= d.tier) takeLocal(d, receipt);
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

    private static void takeLocal(GridNode d, SettleReceipt receipt) {
        long sh = Math.max(0, d.hi), sl = d.hi < 0 ? 0 : d.lo;
        boolean all = U126.compare(sh, sl, receipt.restHi, receipt.restLo) >= 0;
        long th = all ? receipt.restHi : sh, tl = all ? receipt.restLo : sl;
        if (th == 0 && tl == 0) return;
        receipt.add(d, th, tl);
        receipt.consume(th, tl);
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
            if (node.planAmount <= 0) continue;
            receipt.add(node, 0, node.planAmount);
            receipt.consume(0, node.planAmount);
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
            long sh = Math.max(0, node.hi), sl = node.hi < 0 ? 0 : node.lo;
            if (U126.compare(th, tl, sh, sl) > 0) {
                th = sh;
                tl = sl;
            }
            if (U126.compare(th, tl, receipt.restHi, receipt.restLo) > 0) {
                th = receipt.restHi;
                tl = receipt.restLo;
            }
            receipt.add(node, th, tl);
            receipt.consume(th, tl);
        }
    }

    static double reachableStorage(EnergyAccount account, GridNode d, int voltage) {
        int stamp = account.nextVisit();
        double sum = voltage <= d.tier ? d.storageDouble() : 0;
        for (var r : d.routes) {
            if (r.tier < voltage || r.source.visited == stamp) continue;
            r.source.visited = stamp;
            sum += r.source.storageDouble();
        }
        return sum;
    }

    static void commit(EnergyAccount account, SettleReceipt receipt) {
        int now = GridClock.tick();
        for (int i = 0; i < receipt.count; i++) {
            var node = receipt.nodes[i];
            node.subtract(receipt.his[i], receipt.los[i]);
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
            if (account.nodes.get(node.dimension) == node) {
                node.addWide(receipt.his[i], receipt.los[i]);
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
