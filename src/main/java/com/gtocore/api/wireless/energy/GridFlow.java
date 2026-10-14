package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 拓扑快照里两个节点之间经全部线路的最大输送（每条线两个方向各按预算独立，Edmonds-Karp），以及可达的最高线路电压。
 * lineFlow 与 topology.lines() 同序，正值为 a→b、负值为 b→a，单位 EU/t。
 */
public final class GridFlow {

    public record Result(long flow, int tier, long[] lineFlow) {

        public int usedLines() {
            int used = 0;
            for (long f : lineFlow) {
                if (f != 0) used++;
            }
            return used;
        }

        public boolean reachable() {
            return tier >= 0;
        }
    }

    private GridFlow() {}

    public static Result solve(GridView.TopologyView topology, @Nullable ResourceKey<Level> from, @Nullable ResourceKey<Level> to) {
        return solve(topology, from == null ? -1 : topology.indexOf(GridBody.of(from)), to == null ? -1 : topology.indexOf(GridBody.of(to)));
    }

    public static Result solve(GridView.TopologyView topology, int from, int to) {
        var lines = topology.lines();
        int n = topology.nodes().size(), m = lines.size();
        var lineFlow = new long[m];
        if (from < 0 || to < 0 || from >= n || to >= n || from == to) return new Result(0, -1, lineFlow);
        var graph = new Graph(n, m);
        for (int l = 0; l < m; l++) graph.line(l, lines.get(l));
        long total = graph.maxFlow(from, to);
        for (int l = 0; l < m; l++) lineFlow[l] = total == 0 ? 0 : lines.get(l).budget() - graph.cap[l << 1];
        return new Result(total, graph.widest(from, to), lineFlow);
    }

    private static final class Graph {

        final int n;
        final int[] head, next, dest, tier, parent, queue;
        final long[] cap;

        Graph(int n, int m) {
            this.n = n;
            head = new int[n];
            next = new int[m << 1];
            dest = new int[m << 1];
            tier = new int[m];
            cap = new long[m << 1];
            parent = new int[n];
            queue = new int[n];
            Arrays.fill(head, -1);
        }

        void line(int l, GridView.LineInfo line) {
            long budget = Math.max(0, line.budget());
            if (line.a() < 0 || line.b() < 0 || line.a() >= n || line.b() >= n || line.a() == line.b()) {
                tier[l] = -1;
                cap[l << 1] = budget;
                return;
            }
            tier[l] = budget > 0 ? line.tier() : -1;
            edge(l << 1, line.a(), line.b(), budget);
            edge(l << 1 | 1, line.b(), line.a(), budget);
        }

        private void edge(int e, int u, int v, long budget) {
            dest[e] = v;
            cap[e] = budget;
            next[e] = head[u];
            head[u] = e;
        }

        long maxFlow(int s, int t) {
            long total = 0;
            while (total < U126.MASK && search(s, t, 0, true)) {
                long push = U126.MASK;
                for (int v = t; v != s; v = dest[parent[v] ^ 1]) push = Math.min(push, cap[parent[v]]);
                for (int v = t; v != s; v = dest[parent[v] ^ 1]) {
                    int e = parent[v];
                    cap[e] -= push;
                    cap[e ^ 1] = U126.saturatedAdd(cap[e ^ 1], push);
                }
                total = U126.saturatedAdd(total, push);
            }
            return total;
        }

        int widest(int s, int t) {
            for (int level = GTValues.MAX; level >= 0; level--) {
                if (search(s, t, level, false)) return level;
            }
            return -1;
        }

        private boolean search(int s, int t, int minTier, boolean residual) {
            Arrays.fill(parent, -1);
            parent[s] = -2;
            int tail = 0;
            queue[tail++] = s;
            for (int at = 0; at < tail && parent[t] == -1; at++) {
                int u = queue[at];
                for (int e = head[u]; e >= 0; e = next[e]) {
                    int v = dest[e];
                    if (parent[v] != -1 || (residual ? cap[e] <= 0 : tier[e >> 1] < minTier)) continue;
                    parent[v] = e;
                    queue[tail++] = v;
                }
            }
            return parent[t] != -1;
        }
    }
}
