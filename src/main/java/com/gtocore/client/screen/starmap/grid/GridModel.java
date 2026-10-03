package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;
import com.gtocore.common.wireless.energy.map.GridMapView;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.ints.IntArrays;

import java.util.Arrays;

/**
 * 同步快照到星图天体的映射：节点↔天体下标、线路两端天体、每条线的流量分档与满载方向、按容量排好的节点顺序。
 * 只在拓扑或实时数据换实例时重算，绘制只读数组。
 */
@OnlyIn(Dist.CLIENT)
final class GridModel {

    static final int FOLD_GAP = 16;

    private final GridMapView view;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final int[] nodeBody = new int[GridView.MAX_NODES];
    private final int[] degree = new int[GridView.MAX_NODES];
    private final int[] byCapacity = new int[GridView.MAX_NODES];
    private final int[] lineA = new int[GridView.MAX_LINES], lineB = new int[GridView.MAX_LINES];
    private final int[] level = new int[GridView.MAX_LINES];
    private final float[] flowAB = new float[GridView.MAX_LINES], flowBA = new float[GridView.MAX_LINES];
    private int[] neighborMark = new int[0];
    private int[] bodyNode = new int[0];
    private GridView.TopologyView topology = GridView.TopologyView.EMPTY;
    private GridView.LiveView live = GridView.LiveView.EMPTY;
    private int nodeCount, lineCount, topologyStamp, stamp, markStamp, markedBody = -1, markedTopology = -1;

    GridModel(GridMapView view, StarCatalog catalog, StarGeometry geometry) {
        this.view = view;
        this.catalog = catalog;
        this.geometry = geometry;
        Arrays.fill(nodeBody, -1);
    }

    boolean sync() {
        var nextTopology = view.topology();
        var nextLive = view.live();
        boolean structural = nextTopology != topology;
        if (!structural && nextLive == live) return false;
        if (structural) {
            topology = nextTopology;
            remap();
            topologyStamp++;
        }
        live = nextLive;
        refreshFlows();
        stamp++;
        return structural;
    }

    private void remap() {
        var nodes = topology.nodes();
        nodeCount = Math.min(nodes.size(), GridView.MAX_NODES);
        for (int i = 0; i < nodeCount; i++) {
            int body = catalog.indexOf(nodes.get(i).dimension());
            nodeBody[i] = body >= 0 ? body : catalog.addExtraRealm(nodes.get(i).dimension());
        }
        Arrays.fill(nodeBody, nodeCount, nodeBody.length, -1);
        if (bodyNode.length != catalog.size()) {
            bodyNode = new int[catalog.size()];
            neighborMark = new int[catalog.size()];
        }
        Arrays.fill(bodyNode, -1);
        for (int i = 0; i < nodeCount; i++) {
            if (nodeBody[i] >= 0 && nodeBody[i] < bodyNode.length) bodyNode[nodeBody[i]] = i;
        }
        var lines = topology.lines();
        lineCount = Math.min(lines.size(), GridView.MAX_LINES);
        Arrays.fill(degree, 0);
        for (int l = 0; l < lineCount; l++) {
            var line = lines.get(l);
            lineA[l] = body(line.a());
            lineB[l] = body(line.b());
            if (line.a() >= 0 && line.a() < nodeCount) degree[line.a()]++;
            if (line.b() >= 0 && line.b() < nodeCount) degree[line.b()]++;
            level[l] = GridStyle.IDLE;
        }
        for (int i = 0; i < nodeCount; i++) byCapacity[i] = i;
        IntArrays.mergeSort(byCapacity, 0, nodeCount, (a, b) -> Float.compare(nodes.get(b).capacity(), nodes.get(a).capacity()));
        markedTopology = -1;
    }

    private void refreshFlows() {
        boolean current = live.topologyRevision() == topology.revision();
        float max = 0;
        for (int l = 0; l < lineCount; l++) {
            flowAB[l] = current ? Math.max(0, live.flowAB(l)) : 0;
            flowBA[l] = current ? Math.max(0, live.flowBA(l)) : 0;
            max = Math.max(max, Math.max(flowAB[l], flowBA[l]));
        }
        var lines = topology.lines();
        for (int l = 0; l < lineCount; l++) level[l] = GridStyle.level(Math.max(flowAB[l], flowBA[l]), max, lines.get(l).budget(), level[l]);
    }

    int stamp() {
        return stamp;
    }

    int topologyStamp() {
        return topologyStamp;
    }

    GridView.TopologyView topology() {
        return topology;
    }

    boolean isLiveCurrent() {
        return live.topologyRevision() == topology.revision();
    }

    int nodeCount() {
        return nodeCount;
    }

    int lineCount() {
        return lineCount;
    }

    int body(int node) {
        return node >= 0 && node < nodeCount ? nodeBody[node] : -1;
    }

    int node(int body) {
        return body >= 0 && body < bodyNode.length ? bodyNode[body] : -1;
    }

    GridView.NodeInfo info(int node) {
        return topology.nodes().get(node);
    }

    GridView.LineInfo lineInfo(int line) {
        return topology.lines().get(line);
    }

    boolean isLineOnly(int node) {
        var info = info(node);
        return info.towers() <= 0 || !(info.capacity() > 0);
    }

    float storage(int node) {
        return isLiveCurrent() ? live.storage(node) : 0;
    }

    float storageDelta(int node) {
        return isLiveCurrent() ? live.storageDelta(node) : 0;
    }

    float fill(int node) {
        float capacity = info(node).capacity();
        return capacity > 0 ? Math.max(0, Math.min(1, storage(node) / capacity)) : 0;
    }

    int degree(int node) {
        return node >= 0 && node < nodeCount ? degree[node] : 0;
    }

    int byCapacity(int rank) {
        return byCapacity[rank];
    }

    int lineA(int line) {
        return lineA[line];
    }

    int lineB(int line) {
        return lineB[line];
    }

    boolean isPlaced(int line) {
        return lineA[line] >= 0 && lineB[line] >= 0 && lineA[line] != lineB[line];
    }

    boolean touches(int line, int body) {
        return body >= 0 && (lineA[line] == body || lineB[line] == body);
    }

    int level(int line) {
        return level[line];
    }

    float flowAB(int line) {
        return flowAB[line];
    }

    float flowBA(int line) {
        return flowBA[line];
    }

    float worldX(int body) {
        return geometry.isFolded(body) ? geometry.parentX(body) + foldOffset(body, true) : geometry.x(body);
    }

    float worldY(int body) {
        return geometry.isFolded(body) ? geometry.parentY(body) + foldOffset(body, false) : geometry.y(body);
    }

    private float foldOffset(int body, boolean horizontal) {
        float dx = geometry.x(body) - geometry.parentX(body), dy = geometry.y(body) - geometry.parentY(body);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0) return 0;
        int parent = catalog.parent(body);
        float size = parent >= 0 ? geometry.pixels(parent) : geometry.anchorPixels();
        float distance = (size / 2f + FOLD_GAP) / Math.max(0.01f, geometry.scale());
        return (horizontal ? dx : dy) / length * distance;
    }

    boolean isNeighbor(int selected, int body) {
        if (selected < 0 || body < 0 || body >= neighborMark.length) return false;
        if (selected != markedBody || markedTopology != topologyStamp) markNeighbors(selected);
        return neighborMark[body] == markStamp;
    }

    private void markNeighbors(int selected) {
        markedBody = selected;
        markedTopology = topologyStamp;
        markStamp++;
        for (int l = 0; l < lineCount; l++) {
            if (!isPlaced(l)) continue;
            int other = lineA[l] == selected ? lineB[l] : lineB[l] == selected ? lineA[l] : -1;
            if (other >= 0 && other < neighborMark.length) neighborMark[other] = markStamp;
        }
    }
}
