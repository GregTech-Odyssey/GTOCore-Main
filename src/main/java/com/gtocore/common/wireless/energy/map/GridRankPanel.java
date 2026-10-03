package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;

/**
 * 星图汇总下方的排行：按页签切换储能最多、储能比例最低、最忙线路的前 3 名（带滞后），点击一行定位到画布上的天体或线路。
 */
final class GridRankPanel extends UIElement {

    private static final int VALUE_WIDTH = 96, BUSY = 2;
    private static final double LOW_STORAGE = 0.1, SATURATED = 0.95, SWAP_GAP = 0.05;
    private static final String[] TABS = { GridMapLang.RANK_TAB_MOST, GridMapLang.RANK_TAB_LOW, GridMapLang.RANK_TAB_BUSY };
    private static final String[] TAB_TIPS = { GridMapLang.RANK_MOST, GridMapLang.RANK_LOW, GridMapLang.RANK_BUSY };

    private final GridMapContext ctx;
    private final Group most = new Group(new GridRanking(true, true, SWAP_GAP));
    private final Group lowest = new Group(new GridRanking(false, false, SWAP_GAP));
    private final Group busiest = new Group(new GridRanking(true, false, SWAP_GAP));
    private final Group[] groups = { most, lowest, busiest };
    private final SyncValue<Integer> shownTab;
    private int tab;
    private double[] nodeMetric = new double[0], lineMetric = new double[0];
    @Nullable
    private GridView.TopologyView shownTopology;
    @Nullable
    private GridView.LiveView shownLive;

    GridRankPanel(GridMapContext ctx) {
        this.ctx = ctx;
        layout(l -> l.column().width(LayoutStyle.AUTO).gapAll(UISizes.GAP));
        shownTab = addSyncValue(SyncValue.ofInt(() -> tab, 0));
        var tabs = ButtonGroup.single(TABS.length, i -> Component.translatable(TABS[i]), () -> tab, i -> tab = Math.max(0, Math.min(TABS.length - 1, i)))
                .optionTooltips(i -> Collections.singletonList(Component.translatable(TAB_TIPS[i]))).horizontal().compact();
        var panel = new StatusPanel(LayoutStyle.AUTO);
        for (int i = 0; i < GridRanking.SIZE; i++) {
            int rank = i;
            var synced = addSyncValue(SyncValue.ofInt(() -> current().targets[rank], -1));
            var row = new GridRankRow(VALUE_WIDTH, () -> current().names[rank], () -> current().values[rank], () -> current().levels[rank],
                    synced::getValue, this::focus, GridMapLang.RANK_CLICK);
            synced.onChanged(index -> row.setDisplay(rank == 0 || index >= 0));
            row.setDisplay(rank == 0);
            panel.addChild(row);
        }
        addChildren(tabs, panel);
    }

    private Group current() {
        refreshed();
        return groups[tab];
    }

    private void focus(int index) {
        if (shownTab.getValue() == BUSY) focusLine(index);
        else focusNode(index);
    }

    private void focusNode(int node) {
        var navigator = ctx.navigator();
        if (navigator != null) navigator.focusNode(node);
    }

    private void focusLine(int line) {
        var navigator = ctx.navigator();
        if (navigator != null) navigator.focusLine(line);
    }

    private void refreshed() {
        var topology = ctx.topology();
        var live = ctx.live();
        if (topology == shownTopology && live == shownLive) return;
        if (topology != shownTopology) {
            for (var group : groups) group.ranking.reset();
        }
        shownTopology = topology;
        shownLive = live;
        rebuild(topology, live);
    }

    private void rebuild(GridView.TopologyView topology, GridView.LiveView live) {
        var nodes = topology.nodes();
        int n = nodes.size(), m = topology.lines().size();
        if (nodeMetric.length < n) nodeMetric = new double[n];
        if (lineMetric.length < m) lineMetric = new double[m];
        boolean current = live.topologyRevision() == topology.revision();
        for (int i = 0; i < n; i++) {
            var info = nodes.get(i);
            nodeMetric[i] = current && info.towers() > 0 ? live.storage(i) : Double.NaN;
        }
        most.ranking.update(nodeMetric, n);
        for (int i = 0; i < n; i++) {
            float capacity = nodes.get(i).capacity();
            nodeMetric[i] = Double.isNaN(nodeMetric[i]) || capacity <= 0 ? Double.NaN : nodeMetric[i] / capacity;
        }
        lowest.ranking.update(nodeMetric, n);
        for (int l = 0; l < m; l++) {
            lineMetric[l] = GridCardLines.usage(topology, live, l) > 0 ? GridCardLines.busyness(topology, live, l) : Double.NaN;
        }
        busiest.ranking.update(lineMetric, m);
        for (int r = 0; r < GridRanking.SIZE; r++) {
            nodeRow(most, r, topology, live, true);
            nodeRow(lowest, r, topology, live, false);
            lineRow(r, topology, live);
        }
    }

    private static void nodeRow(Group group, int r, GridView.TopologyView topology, GridView.LiveView live, boolean amount) {
        int node = group.ranking.get(r);
        group.set(r, node);
        if (node < 0) return;
        var info = topology.nodes().get(node);
        double ratio = info.capacity() > 0 ? live.storage(node) / (double) info.capacity() : 0;
        var percent = (long) Math.floor(ratio * 100);
        group.names[r] = GridFormat.bodyName(info.dimension());
        group.values[r] = amount ? Component.translatable(GridMapLang.RANK_STORED_VALUE, GridFormat.compact(live.storage(node)), percent) :
                Component.translatable(GridMapLang.PERCENT, percent);
        group.levels[r] = !amount && ratio < LOW_STORAGE ? Level.WARNING : Level.NORMAL;
    }

    private void lineRow(int r, GridView.TopologyView topology, GridView.LiveView live) {
        int line = busiest.ranking.get(r);
        busiest.set(r, line);
        if (line < 0) return;
        var info = topology.lines().get(line);
        var nodes = topology.nodes();
        busiest.names[r] = Component.translatable(GridMapLang.TIP_LINE_ENDS, GridFormat.bodyName(nodes.get(info.a()).dimension()),
                GridFormat.bodyName(nodes.get(info.b()).dimension()));
        boolean saturated = GridCardLines.isSaturated(topology, live, line);
        double usage = GridCardLines.usage(topology, live, line);
        busiest.values[r] = saturated ? Component.translatable(GridMapLang.RANK_SATURATED, GridFormat.compact(GridCardLines.peakFlow(topology, live, line))) :
                Component.translatable(GridMapLang.PERCENT, Math.round(usage * 100));
        busiest.levels[r] = usage >= SATURATED ? Level.WARNING : Level.NORMAL;
    }

    private static final class Group {

        private static final Integer NONE = -1;

        final GridRanking ranking;
        final Integer[] targets = new Integer[GridRanking.SIZE];
        final Component[] names = new Component[GridRanking.SIZE];
        final Component[] values = new Component[GridRanking.SIZE];
        final Level[] levels = new Level[GridRanking.SIZE];

        private Group(GridRanking ranking) {
            this.ranking = ranking;
            Arrays.fill(targets, NONE);
            Arrays.fill(names, GridFormat.NO_VALUE);
            Arrays.fill(values, Component.empty());
            Arrays.fill(levels, Level.NORMAL);
        }

        void set(int r, int index) {
            if (targets[r] != index) targets[r] = index;
            if (index >= 0) return;
            names[r] = GridFormat.NO_VALUE;
            values[r] = Component.empty();
            levels[r] = Level.NORMAL;
        }
    }
}
