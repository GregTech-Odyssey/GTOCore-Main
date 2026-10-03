package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 一次路由求解：主线程拍下只读快照，后台线程求解，回到主线程后若该账户拓扑没有再变就换上新路由。
 */
final class RouteJob {

    private final EnergyAccount account;
    private final GridNode[] nodes;
    private final Arc[] arcs;
    private final int[] reverse;
    private final RoutePlanner.Topology topology;
    @Nullable
    private CompletableFuture<RoutePlanner.Plan> future;

    RouteJob(EnergyAccount account, ObjectArrayList<GridNode> nodeList, Arc[] arcs, Arc[][] incoming) {
        this.account = account;
        this.nodes = nodeList.toArray(new GridNode[0]);
        this.arcs = arcs;
        int n = nodes.length, m = arcs.length;
        var nodeTier = new int[n];
        var out = new int[n][];
        var in = new int[n][];
        for (int i = 0; i < n; i++) {
            nodeTier[i] = nodes[i].tier;
            out[i] = indices(nodes[i].out);
            in[i] = indices(incoming[i]);
        }
        var arcFrom = new int[m];
        var arcTo = new int[m];
        var arcTier = new int[m];
        var arcBudget = new long[m];
        reverse = new int[m];
        for (int i = 0; i < m; i++) {
            var arc = arcs[i];
            arcFrom[i] = arc.from.index;
            arcTo[i] = arc.to.index;
            arcTier[i] = arc.tier;
            arcBudget[i] = arc.budget;
            reverse[i] = reverseOf(arc).index;
        }
        topology = new RoutePlanner.Topology(nodeTier, arcFrom, arcTo, arcTier, arcBudget, out, in);
    }

    void runNow() {
        apply(RoutePlanner.plan(topology));
    }

    void submit(Executor executor) {
        var snapshot = topology;
        future = CompletableFuture.supplyAsync(() -> RoutePlanner.plan(snapshot), executor);
    }

    boolean poll() {
        var pending = future;
        if (pending == null || !pending.isDone()) return false;
        if (account.routeJob == this && !account.removed) {
            try {
                apply(pending.join());
            } catch (RuntimeException e) {
                GTOCore.LOGGER.error("[无线电网] 后台路由求解失败，改为主线程求解", e);
                runNow();
            }
            account.routeJob = null;
            account.invalidatePorts();
            account.wakeAll();
        }
        return true;
    }

    private void apply(RoutePlanner.Plan plan) {
        for (int d = 0; d < nodes.length; d++) {
            var sources = plan.sources()[d];
            var paths = plan.paths()[d];
            var routes = sources.length == 0 ? GridNode.NO_ROUTES : new Route[sources.length];
            for (int k = 0; k < sources.length; k++) {
                var path = paths[k];
                var inbound = new Arc[path.length];
                var outbound = new Arc[path.length];
                for (int j = 0; j < path.length; j++) {
                    int arc = path[path.length - 1 - j];
                    inbound[j] = arcs[arc];
                    outbound[j] = arcs[reverse[arc]];
                }
                routes[k] = new Route(nodes[sources[k]], inbound, outbound);
            }
            nodes[d].routes = routes;
            nodes[d].reachTier = plan.reach()[d];
        }
        linkDependents(plan);
    }

    private void linkDependents(RoutePlanner.Plan plan) {
        var lists = new ObjectArrayList[nodes.length];
        var stamp = new int[nodes.length];
        Arrays.fill(stamp, -1);
        for (int d = 0; d < nodes.length; d++) {
            for (int source : plan.sources()[d]) {
                if (stamp[source] == d) continue;
                stamp[source] = d;
                if (lists[source] == null) lists[source] = new ObjectArrayList<GridNode>();
                lists[source].add(nodes[d]);
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            @SuppressWarnings("unchecked")
            var list = (ObjectArrayList<GridNode>) lists[i];
            nodes[i].dependents = list == null ? GridNode.NO_NODES : list.toArray(GridNode.NO_NODES);
        }
    }

    private static int[] indices(Arc[] list) {
        var result = new int[list.length];
        for (int i = 0; i < list.length; i++) result[i] = list[i].index;
        return result;
    }

    private static Arc reverseOf(Arc arc) {
        for (var back : arc.to.out) {
            if (back.to == arc.from) return back;
        }
        throw new IllegalStateException("missing reverse arc");
    }
}
