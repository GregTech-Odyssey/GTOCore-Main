package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

/**
 * 只读拓扑快照上的路由求解，可在后台线程运行：对每个目的节点和每个来源，按电压从高到低每层独立求最大流并取出路径，跨层去重。
 * 每条路径的电压至少是所在层，高压路径不会被低层改道，低层也不会因高层占用而少算；路径间共用的弧由运行时令牌兜底。
 */
final class RoutePlanner {

    static final int MAX_PATHS = 8;
    private static final int MAX_AUGMENTS = 64;

    record Topology(int[] nodeTier, int[] arcFrom, int[] arcTo, int[] arcTier, long[] arcBudget, int[][] out, int[][] in) {

        int nodes() {
            return nodeTier.length;
        }
    }

    record Plan(int[][] sources, int[][][] paths, int[] reach) {}

    private final Topology topo;
    private final long[] flow;
    private final int[] tiers;
    private final int[][] components;
    private final int[] seen;
    private int stamp;
    private final int[] parent;
    private final boolean[] backward;
    private final IntArrayList queue;
    private final IntArrayList touched = new IntArrayList();

    private RoutePlanner(Topology topo) {
        int n = topo.nodes();
        this.topo = topo;
        this.flow = new long[topo.arcFrom.length];
        this.tiers = presentTiers(topo.arcTier);
        this.components = new int[tiers.length][];
        for (int i = 0; i < tiers.length; i++) components[i] = components(topo, tiers[i]);
        this.seen = new int[n];
        this.parent = new int[n];
        this.backward = new boolean[n];
        this.queue = new IntArrayList(n);
    }

    static Plan plan(Topology topo) {
        return new RoutePlanner(topo).build();
    }

    private Plan build() {
        int n = topo.nodes();
        var sources = new int[n][];
        var paths = new int[n][][];
        var reach = new int[n];
        var found = new ObjectArrayList<int[]>();
        var owners = new IntArrayList();
        for (int dest = 0; dest < n; dest++) {
            found.clear();
            owners.clear();
            for (int source = 0; source < n; source++) {
                if (source != dest && topo.nodeTier[source] >= 0 && tiers.length > 0 && components[tiers.length - 1][source] == components[tiers.length - 1][dest]) decompose(source, dest, found, owners);
            }
            var order = new int[found.size()];
            for (int i = 0; i < order.length; i++) order[i] = i;
            IntArrays.quickSort(order, (x, y) -> {
                int hx = found.get(x).length, hy = found.get(y).length;
                return hx != hy ? Integer.compare(hx, hy) : Long.compare(bottleneck(found.get(y)), bottleneck(found.get(x)));
            });
            sources[dest] = new int[order.length];
            paths[dest] = new int[order.length][];
            int best = topo.nodeTier[dest];
            for (int i = 0; i < order.length; i++) {
                int[] path = found.get(order[i]);
                int source = owners.getInt(order[i]);
                sources[dest][i] = source;
                paths[dest][i] = path;
                int t = topo.nodeTier[source];
                for (int arc : path) t = Math.min(t, topo.arcTier[arc]);
                best = Math.max(best, t);
            }
            reach[dest] = best;
        }
        return new Plan(sources, paths, reach);
    }

    private long bottleneck(int[] path) {
        long b = Long.MAX_VALUE;
        for (int arc : path) b = Math.min(b, topo.arcBudget[arc]);
        return b;
    }

    private void decompose(int source, int dest, ObjectArrayList<int[]> found, IntArrayList owners) {
        int first = found.size();
        int augments = 0, paths = 0;
        int sourceTier = topo.nodeTier[source];
        for (int i = 0; i < tiers.length && augments < MAX_AUGMENTS && paths < MAX_PATHS; i++) {
            int tier = tiers[i];
            if (tier > sourceTier && i + 1 < tiers.length && tiers[i + 1] >= sourceTier) continue;
            if (components[i][source] != components[i][dest]) continue;
            reset();
            int before = augments;
            while (augments < MAX_AUGMENTS && augment(source, dest, tier)) augments++;
            if (augments == before) continue;
            for (int[] path; paths < MAX_PATHS && (path = takePath(source, dest, tier)) != null;) {
                if (contains(found, first, path)) continue;
                paths++;
                found.add(path);
                owners.add(source);
            }
        }
    }

    private void reset() {
        for (int i = 0, n = touched.size(); i < n; i++) flow[touched.getInt(i)] = 0;
        touched.clear();
    }

    private long residual(int arc) {
        return topo.arcBudget[arc] - flow[arc];
    }

    private static boolean contains(ObjectArrayList<int[]> found, int from, int[] path) {
        for (int i = from, n = found.size(); i < n; i++) {
            if (Arrays.equals(found.get(i), path)) return true;
        }
        return false;
    }

    private boolean augment(int source, int dest, int tier) {
        queue.clear();
        queue.add(source);
        int mark = ++stamp;
        seen[source] = mark;
        boolean found = false;
        for (int head = 0; head < queue.size() && !found; head++) {
            int u = queue.getInt(head);
            for (int arc : topo.out[u]) {
                int v = topo.arcTo[arc];
                if (seen[v] == mark || topo.arcTier[arc] < tier || residual(arc) <= 0) continue;
                seen[v] = mark;
                parent[v] = arc;
                backward[v] = false;
                if (v == dest) found = true;
                queue.add(v);
            }
            for (int arc : topo.in[u]) {
                int v = topo.arcFrom[arc];
                if (seen[v] == mark || flow[arc] <= 0) continue;
                seen[v] = mark;
                parent[v] = arc;
                backward[v] = true;
                queue.add(v);
            }
        }
        if (!found) return false;
        long push = Long.MAX_VALUE;
        for (int v = dest; v != source;) {
            int arc = parent[v];
            push = Math.min(push, backward[v] ? flow[arc] : residual(arc));
            v = backward[v] ? topo.arcTo[arc] : topo.arcFrom[arc];
        }
        for (int v = dest; v != source;) {
            int arc = parent[v];
            if (flow[arc] == 0) touched.add(arc);
            flow[arc] += backward[v] ? -push : push;
            v = backward[v] ? topo.arcTo[arc] : topo.arcFrom[arc];
        }
        return true;
    }

    private int[] takePath(int source, int dest, int tier) {
        int mark = ++stamp;
        queue.clear();
        queue.add(source);
        seen[source] = mark;
        for (int head = 0; head < queue.size() && seen[dest] != mark; head++) {
            for (int arc : topo.out[queue.getInt(head)]) {
                int v = topo.arcTo[arc];
                if (seen[v] == mark || topo.arcTier[arc] < tier || flow[arc] <= 0) continue;
                seen[v] = mark;
                parent[v] = arc;
                queue.add(v);
            }
        }
        if (seen[dest] != mark) return null;
        int length = 0;
        long push = Long.MAX_VALUE;
        for (int v = dest; v != source; v = topo.arcFrom[parent[v]]) {
            length++;
            push = Math.min(push, flow[parent[v]]);
        }
        var path = new int[length];
        for (int v = dest; v != source; v = topo.arcFrom[parent[v]]) {
            path[--length] = parent[v];
            flow[parent[v]] -= push;
        }
        return path;
    }

    private static int[] presentTiers(int[] arcTier) {
        var present = new boolean[GTValues.MAX + 1];
        int count = 0;
        for (int tier : arcTier) {
            if (!present[tier]) {
                present[tier] = true;
                count++;
            }
        }
        var tiers = new int[count];
        for (int t = GTValues.MAX, i = 0; t >= 0; t--) {
            if (present[t]) tiers[i++] = t;
        }
        return tiers;
    }

    private static int[] components(Topology topo, int tier) {
        int n = topo.nodes();
        var root = new int[n];
        for (int i = 0; i < n; i++) root[i] = i;
        for (int arc = 0; arc < topo.arcFrom.length; arc++) {
            if (topo.arcTier[arc] < tier) continue;
            int a = find(root, topo.arcFrom[arc]), b = find(root, topo.arcTo[arc]);
            if (a != b) root[a] = b;
        }
        for (int i = 0; i < n; i++) root[i] = find(root, i);
        return root;
    }

    private static int find(int[] root, int i) {
        while (root[i] != i) {
            root[i] = root[root[i]];
            i = root[i];
        }
        return i;
    }
}
