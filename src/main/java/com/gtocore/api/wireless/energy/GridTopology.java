package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.math.BigInteger;

/**
 * 由登记的能源塔与中继重建一个队伍的网络：节点容量/损耗/电压、线路束与单向弧、各节点到各来源的路由。
 * 路由取自分电压层的最大流分解（高电压线路优先），干线可被多条路径共用，运行时按弧令牌扣减。
 */
final class GridTopology {

    private final EnergyAccount account;
    private final ObjectArrayList<GridNode> nodes;
    private Arc[] arcs = GridNode.NO_ARCS;
    private Arc[][] incoming;

    private GridTopology(EnergyAccount account) {
        this.account = account;
        this.nodes = account.nodeList;
    }

    static void rebuild(EnergyAccount account, boolean async) {
        var topology = new GridTopology(account);
        topology.resetNodes();
        topology.aggregateTowers();
        topology.spill();
        var bundles = topology.bundleRelays();
        topology.relocateStranded();
        topology.pruneShells();
        topology.placePending();
        var shape = topology.shapeOf(bundles);
        if (shape.equals(account.shape)) {
            topology.refreshRouteTiers();
        } else {
            topology.buildArcs(bundles);
            var job = new RouteJob(account, topology.nodes, topology.arcs, topology.incoming);
            if (async) {
                account.routeJob = job;
                WirelessGrid.submit(job);
            } else {
                account.routeJob = null;
                job.runNow();
            }
            account.shape = shape;
        }
        account.invalidatePorts();
    }

    record Shape(ObjectOpenHashSet<Bundle> bundles, ReferenceOpenHashSet<GridNode> nodes, ObjectOpenHashSet<Source> sources) {}

    private record Bundle(GridNode a, GridNode b, long tier, long budget) {}

    private record Source(GridNode node, int tier) {}

    private Shape shapeOf(Reference2ObjectOpenHashMap<GridNode, Reference2ObjectOpenHashMap<GridNode, long[]>> bundles) {
        var set = new ObjectOpenHashSet<Bundle>();
        bundles.forEach((a, inner) -> inner.forEach((b, bundle) -> set.add(new Bundle(a, b, bundle[0], bundle[1]))));
        var sources = new ObjectOpenHashSet<Source>();
        for (var node : nodes) {
            if (node.tier >= 0) sources.add(new Source(node, node.tier));
        }
        return new Shape(set, new ReferenceOpenHashSet<>(nodes), sources);
    }

    private void refreshRouteTiers() {
        for (var node : nodes) {
            int reach = node.tier;
            for (var route : node.routes) {
                route.refreshTier();
                reach = Math.max(reach, route.tier);
            }
            node.reachTier = reach;
        }
    }

    private void resetNodes() {
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            node.clearCapacity();
            node.linked = false;
        }
    }

    private void aggregateTowers() {
        var atOrAbove = new long[GTValues.MAX + 2];
        for (var tower : account.towers.values()) {
            for (var unit : tower.units()) {
                if (unit.tier() >= 0 && unit.tier() <= GTValues.MAX) atOrAbove[unit.tier()] += unit.count();
            }
        }
        for (int t = GTValues.MAX - 1; t >= 0; t--) atOrAbove[t] += atOrAbove[t + 1];
        account.towerCapacities.clear();
        var weights = new Reference2ObjectOpenHashMap<GridNode, double[]>();
        for (var tower : account.towers.values()) {
            var capacity = BigInteger.ZERO;
            double lossWeight = 0;
            for (var unit : tower.units()) {
                long scale = unit.tier() >= 0 && unit.tier() <= GTValues.MAX ? atOrAbove[unit.tier()] : 1;
                var unitCapacity = unit.capacity().multiply(BigInteger.valueOf(unit.count())).multiply(BigInteger.valueOf(scale));
                capacity = capacity.add(unitCapacity);
                lossWeight += unitCapacity.doubleValue() * unit.loss();
            }
            account.towerCapacities.put(tower.pos(), capacity);
            var node = account.nodeOrCreate(tower.pos().dimension());
            int bank = tower.tier();
            if (bank < 0 || capacity.signum() <= 0) continue;
            node.addCapacity(bank, U126.hi(capacity), U126.lo(capacity));
            var w = weights.computeIfAbsent(node, k -> new double[GridNode.BANKS << 1]);
            w[bank << 1] += lossWeight;
            w[bank << 1 | 1] += capacity.doubleValue();
        }
        weights.forEach((node, w) -> {
            for (int t = 0; t < GridNode.BANKS; t++) {
                if (w[t << 1 | 1] > 0) node.setLoss(t, (int) Math.min(Loss.PERMILLE, Math.round(w[t << 1] / w[t << 1 | 1])));
            }
        });
        for (int i = 0, n = nodes.size(); i < n; i++) nodes.get(i).finishCapacity();
    }

    private void spill() {
        for (int i = 0, n = nodes.size(); i < n; i++) nodes.get(i).spill(account);
    }

    private Reference2ObjectOpenHashMap<GridNode, Reference2ObjectOpenHashMap<GridNode, long[]>> bundleRelays() {
        var bundles = new Reference2ObjectOpenHashMap<GridNode, Reference2ObjectOpenHashMap<GridNode, long[]>>();
        for (var relay : account.relays.values()) {
            if (!relay.connected()) continue;
            var a = account.nodeOrCreate(relay.pos().dimension());
            var b = account.nodeOrCreate(relay.target());
            a.linked = true;
            b.linked = true;
            if (a.dimension.location().compareTo(b.dimension.location()) > 0) {
                var t = a;
                a = b;
                b = t;
            }
            var bundle = bundles.computeIfAbsent(a, k -> new Reference2ObjectOpenHashMap<>()).computeIfAbsent(b, k -> new long[] { -1, 0 });
            bundle[0] = Math.max(bundle[0], relay.tier());
            bundle[1] = U126.saturatedAdd(bundle[1], U126.saturatedMul(GTValues.V[relay.tier()], relay.amperage()));
        }
        return bundles;
    }

    private void relocateStranded() {
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            if (node.hasCapacity() || node.isEmpty()) continue;
            account.addPending(node.hi, node.lo);
            node.clearStorage();
        }
    }

    private void pruneShells() {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            var node = nodes.get(i);
            if (node.linked || node.hasCapacity() || !node.isEmpty()) continue;
            nodes.remove(i);
            account.nodes.remove(node.dimension);
            node.out = GridNode.NO_ARCS;
            node.routes = GridNode.NO_ROUTES;
            node.reachTier = -1;
        }
    }

    private void placePending() {
        if (account.pendingHi == 0 && account.pendingLo == 0) return;
        GridNode best = null;
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            if (node.hasCapacity() && (best == null || node.capacityDouble() > best.capacityDouble())) best = node;
        }
        if (best == null) return;
        long pHi = account.pendingHi, pLo = account.pendingLo;
        account.pendingHi = 0;
        account.pendingLo = 0;
        best.absorb(pHi, pLo, null);
    }

    private void buildArcs(Reference2ObjectOpenHashMap<GridNode, Reference2ObjectOpenHashMap<GridNode, long[]>> bundles) {
        var outs = new Reference2ObjectOpenHashMap<GridNode, ObjectArrayList<Arc>>(nodes.size());
        var list = new ObjectArrayList<Arc>();
        bundles.forEach((a, inner) -> inner.forEach((b, bundle) -> {
            var ab = reuse(a, b);
            var ba = reuse(b, a);
            ab.setCapacity((int) bundle[0], bundle[1]);
            ba.setCapacity((int) bundle[0], bundle[1]);
            ab.reverse = ba;
            ba.reverse = ab;
            outs.computeIfAbsent(a, k -> new ObjectArrayList<>()).add(ab);
            outs.computeIfAbsent(b, k -> new ObjectArrayList<>()).add(ba);
            list.add(ab);
            list.add(ba);
        }));
        retire(list);
        account.arcs.clear();
        account.arcs.addAll(list);
        arcs = list.toArray(GridNode.NO_ARCS);
        var inLists = new ObjectArrayList[nodes.size()];
        for (int i = 0, n = nodes.size(); i < n; i++) {
            var node = nodes.get(i);
            node.index = i;
            var out = outs.get(node);
            node.out = out == null ? GridNode.NO_ARCS : out.toArray(GridNode.NO_ARCS);
            inLists[i] = new ObjectArrayList<Arc>();
        }
        incoming = new Arc[nodes.size()][];
        for (var arc : arcs) {
            @SuppressWarnings("unchecked")
            var in = (ObjectArrayList<Arc>) inLists[arc.to.index];
            in.add(arc);
        }
        for (int i = 0; i < incoming.length; i++) {
            @SuppressWarnings("unchecked")
            var in = (ObjectArrayList<Arc>) inLists[i];
            incoming[i] = in.toArray(GridNode.NO_ARCS);
        }
        for (int i = 0; i < arcs.length; i++) arcs[i].index = i;
    }

    private Arc reuse(GridNode from, GridNode to) {
        for (var arc : from.out) {
            if (arc.to == to) return arc;
        }
        var arc = new Arc(from, to);
        var old = account.retiredArcs.remove(new Arc.Key(from.dimension, to.dimension));
        if (old != null) arc.inherit(old, GridClock.tick());
        return arc;
    }

    private void retire(ObjectArrayList<Arc> list) {
        int now = GridClock.tick();
        account.retiredArcs.values().removeIf(arc -> arc.idle(now));
        var kept = new ReferenceOpenHashSet<>(list);
        for (var arc : account.arcs) {
            if (kept.contains(arc)) continue;
            arc.setCapacity(arc.tier, 0);
            if (!arc.idle(now)) account.retiredArcs.put(new Arc.Key(arc.from.dimension, arc.to.dimension), arc);
        }
    }
}
