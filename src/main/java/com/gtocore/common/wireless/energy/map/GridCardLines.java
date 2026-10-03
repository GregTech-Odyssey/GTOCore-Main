package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 星球卡片的线路列表键：与该节点相连的线路按利用率从高到低排序（同值保持拓扑顺序），顺序变化才推进版本。
 */
final class GridCardLines {

    static final int MAX_ROWS = 32;

    private final IntArrayList touching = new IntArrayList();
    private final IntArrayList keys = new IntArrayList();
    private int[] order = new int[0];
    private float[] ratios = new float[0];
    @Nullable
    private GridView.TopologyView topology;
    @Nullable
    private GridView.LiveView live;
    private int version;
    private Component title = GridFormat.NO_VALUE;

    void refresh(GridView.TopologyView topology, GridView.LiveView live, int node) {
        boolean structural = topology != this.topology;
        if (!structural && live == this.live) return;
        this.topology = topology;
        this.live = live;
        if (structural) collect(topology, node);
        sort(topology, live);
    }

    private void collect(GridView.TopologyView topology, int node) {
        touching.clear();
        var lines = topology.lines();
        for (int i = 0, n = lines.size(); i < n; i++) {
            var line = lines.get(i);
            if (node >= 0 && (line.a() == node || line.b() == node)) touching.add(i);
        }
        if (order.length < touching.size()) {
            order = new int[touching.size()];
            ratios = new float[touching.size()];
        }
        title = Component.translatable(GridMapLang.LINES, touching.size());
        keys.clear();
        version++;
    }

    private void sort(GridView.TopologyView topology, GridView.LiveView live) {
        int n = touching.size();
        for (int i = 0; i < n; i++) {
            int line = touching.getInt(i);
            ratios[i] = (float) busyness(topology, live, line);
            int j = i;
            while (j > 0 && ratios[order[j - 1]] < ratios[i]) {
                order[j] = order[j - 1];
                j--;
            }
            order[j] = i;
        }
        int shown = Math.min(n, MAX_ROWS);
        boolean same = keys.size() == shown;
        for (int i = 0; same && i < shown; i++) same = keys.getInt(i) == touching.getInt(order[i]);
        if (same) return;
        keys.clear();
        for (int i = 0; i < shown; i++) keys.add(touching.getInt(order[i]));
        version++;
    }

    static double usage(GridView.TopologyView topology, GridView.LiveView live, int line) {
        return Math.min(1, rawUsage(topology, live, line));
    }

    static double busyness(GridView.TopologyView topology, GridView.LiveView live, int line) {
        if (!isSaturated(topology, live, line)) return usage(topology, live, line);
        return 1 + Math.log10(1 + peakFlow(topology, live, line)) / 100;
    }

    static boolean isSaturated(GridView.TopologyView topology, GridView.LiveView live, int line) {
        return rawUsage(topology, live, line) >= 1;
    }

    static double peakFlow(GridView.TopologyView topology, GridView.LiveView live, int line) {
        if (line < 0 || line >= topology.lines().size() || live.topologyRevision() != topology.revision()) return 0;
        return Math.max(live.flowAB(line), live.flowBA(line));
    }

    private static double rawUsage(GridView.TopologyView topology, GridView.LiveView live, int line) {
        var lines = topology.lines();
        if (line < 0 || line >= lines.size()) return 0;
        long budget = lines.get(line).budget();
        return budget > 0 ? peakFlow(topology, live, line) / budget : 0;
    }

    IntArrayList keys() {
        return keys;
    }

    int version() {
        return version;
    }

    Component title() {
        return title;
    }
}
