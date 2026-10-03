package com.gtocore.api.wireless.energy;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * 一次远程取、存电的分配：先沿路由就近贪心；不满且有线路卡住时，在残量图上补增广路，结果即此刻的最大流。
 * 只在弧与节点上记下本次计划量（按印记区分），由调用方落实或写进收据。
 */
final class FlowPlan {

    static final FlowPlan SHARED = new FlowPlan();

    private static final int DRAW = 0, PUT = 1, TOKENS = 2, RESERVE = 3;
    private static final int MAX_AUGMENTS = 256;

    private long stamp;
    private long visit;
    private int mode;
    private boolean inbound;
    private int voltage;
    private int now;
    private int yieldTo;
    private int extraLoss;
    private int duration;
    private GridNode dest = GridNode.EMPTY;
    private long need;
    private long total;
    private boolean cut;
    private final ObjectArrayList<Arc> arcs = new ObjectArrayList<>();
    private final ObjectArrayList<GridNode> nodes = new ObjectArrayList<>();
    private final ObjectArrayList<GridNode> queue = new ObjectArrayList<>();

    private FlowPlan() {}

    long draw(GridNode d, int voltage, long need, int now) {
        return solve(DRAW, true, d, voltage, need, now, -1, 0, 0);
    }

    long put(GridNode d, int voltage, long need, int extraLoss, int yieldTo, int now) {
        return solve(PUT, false, d, voltage, need, now, yieldTo, extraLoss, 0);
    }

    long settle(GridNode d, int voltage, long need, int duration, int priority, int now) {
        return solve(duration > 0 ? RESERVE : TOKENS, true, d, voltage, need, now, priority, 0, duration);
    }

    long total() {
        return total;
    }

    boolean limited() {
        if (total >= need) return false;
        for (var r : dest.routes) {
            if (r.tier >= voltage && spare(r.source) > 0) return true;
        }
        return false;
    }

    int nodeCount() {
        return nodes.size();
    }

    GridNode node(int i) {
        return nodes.get(i);
    }

    int arcCount() {
        return arcs.size();
    }

    Arc arc(int i) {
        return arcs.get(i);
    }

    void applyDraw(EnergyAccount account, int priority) {
        for (int i = 0, n = arcs.size(); i < n; i++) {
            var arc = arcs.get(i);
            if (arc.planFlow > 0) arc.consume(arc.planFlow, now, priority);
        }
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            if (node.planAmount > 0) GridTransfer.take(account, node, node.planAmount);
        }
    }

    void applyPut(EnergyAccount account, int priority) {
        for (int i = 0, n = arcs.size(); i < n; i++) {
            var arc = arcs.get(i);
            if (arc.planFlow > 0) arc.consume(arc.planFlow, now, priority);
        }
        double gross = 0, lost = 0;
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            long g = node.planAmount;
            if (g <= 0) continue;
            long net = g - Loss.of(g, Loss.combined(node.loss, extraLoss));
            GridTransfer.add(account, node, net);
            gross += g;
            lost += g - net;
        }
        if (gross > 0) {
            account.meter.in(gross, lost);
            NodeMeter.in(account, dest, gross, lost);
        }
        if (total < need) {
            for (var r : dest.routes) {
                if (r.tier >= voltage && spare(r.source) <= 0) r.source.awaitingRoom = true;
            }
        }
    }

    private long solve(int mode, boolean inbound, GridNode d, int voltage, long need, int now, int yieldTo, int extraLoss, int duration) {
        this.mode = mode;
        this.inbound = inbound;
        this.dest = d;
        this.voltage = voltage;
        this.need = need;
        this.now = now;
        this.yieldTo = yieldTo;
        this.extraLoss = extraLoss;
        this.duration = duration;
        stamp++;
        total = 0;
        cut = false;
        arcs.clear();
        nodes.clear();
        if (need <= 0) return 0;
        greedy();
        if (total < need && cut) augment();
        return total;
    }

    private void greedy() {
        var routes = dest.routes;
        for (int i = 0; i < routes.length && total < need; i++) {
            var r = routes[i];
            if (r.tier < voltage) continue;
            var s = r.source;
            long want = Math.min(need - total, spare(s));
            if (want <= 0) continue;
            var path = inbound ? r.inbound : r.outbound;
            long a = want;
            for (int j = 0; j < path.length && a > 0; j++) a = Math.min(a, residual(path[j]));
            if (a < want) cut = true;
            if (a <= 0) continue;
            for (var arc : path) arc.planFlow += a;
            s.planAmount += a;
            total += a;
        }
    }

    private void augment() {
        for (int k = 0; k < MAX_AUGMENTS && total < need; k++) {
            var end = search();
            if (end == null) return;
            long push = Math.min(need - total, spare(end));
            for (var v = end; v != dest;) {
                var arc = v.planParent;
                push = Math.min(push, v.planBackward ? arc.planFlow : residual(arc));
                v = toward(arc, v.planBackward);
            }
            if (push <= 0) return;
            for (var v = end; v != dest;) {
                var arc = v.planParent;
                arc.planFlow += v.planBackward ? -push : push;
                v = toward(arc, v.planBackward);
            }
            end.planAmount += push;
            total += push;
        }
    }

    private GridNode search() {
        long mark = ++visit;
        queue.clear();
        queue.add(dest);
        dest.planVisit = mark;
        for (int head = 0; head < queue.size(); head++) {
            var v = queue.get(head);
            for (var arc : v.out) {
                var w = arc.to;
                if (w.planVisit == mark) continue;
                var forward = inbound ? arc.reverse : arc;
                var backward = inbound ? arc : arc.reverse;
                if (forward.tier >= voltage && residual(forward) > 0) {
                    w.planParent = forward;
                    w.planBackward = false;
                } else if (flow(backward) > 0) {
                    w.planParent = backward;
                    w.planBackward = true;
                } else {
                    continue;
                }
                w.planVisit = mark;
                if (spare(w) > 0) return w;
                queue.add(w);
            }
        }
        return null;
    }

    private GridNode toward(Arc arc, boolean backward) {
        return inbound != backward ? arc.to : arc.from;
    }

    private long flow(Arc arc) {
        return arc.planStamp == stamp ? arc.planFlow : 0;
    }

    private long residual(Arc arc) {
        if (arc.planStamp != stamp) {
            arc.planStamp = stamp;
            arc.planCap = capacity(arc);
            arc.planFlow = 0;
            arcs.add(arc);
        }
        return arc.planCap - arc.planFlow;
    }

    private long capacity(Arc arc) {
        long cap = switch (mode) {
            case DRAW -> arc.allowance(now);
            case PUT -> yieldTo < 0 ? arc.allowance(now) : arc.allowance(now) - arc.usedAbove(yieldTo, now);
            case TOKENS -> arc.allowance(now) - arc.usedAbove(yieldTo, now);
            default -> arc.freeBudget(now) - arc.usedAbove(yieldTo, now);
        };
        return Math.max(0, cap);
    }

    private long spare(GridNode node) {
        if (node == dest) return 0;
        if (node.planStamp != stamp) {
            node.planStamp = stamp;
            node.planCap = node.tier >= voltage ? supply(node) : 0;
            node.planAmount = 0;
            nodes.add(node);
        }
        return node.planCap - node.planAmount;
    }

    private long supply(GridNode node) {
        return switch (mode) {
            case PUT -> Loss.acceptGross(node.free(), U126.MASK, Loss.combined(node.loss, extraLoss));
            case RESERVE -> U126.divCeil(Math.max(0, node.hi), node.hi < 0 ? 0 : node.lo, duration);
            default -> node.available();
        };
    }
}
