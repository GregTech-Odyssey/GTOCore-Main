package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 科技树自动布局（分层图 / Sugiyama 框架），结果由 {@code TechTreeManager#getLayout} 缓存，每棵树只算一次：
 * <ol>
 * <li><b>分列</b>：按数据等级分块，块内按"块内依赖"的深度分列：只数同一数据等级里的前置，别的等级的前置在左边的块里，
 * 不占列，互不依赖的节点并排在同一列（同一数据等级的节点在同一块里，块与块之间画分区线）。</li>
 * <li><b>长连线拆段</b>：跨多列的依赖在中间每列放一个虚拟点（每条连线各自一串，不与别的连线合用），
 * 虚拟点和节点一起排序、定位，连线从节点之间穿过，不会压在节点上。</li>
 * <li><b>列内排序</b>：各连通分量分开排（互不相连的几组节点上下排开、不交错）；重心法上下扫描 + 相邻交换，取交叉最少的结果。</li>
 * <li><b>出入口</b>：节点的每条连线在节点右侧（出）或左侧（入）各占一个出入口，纵向错开，按另一端的上下顺序排：
 * 同一节点的多条线从一开始就是分开的，任何两条连线都不共用线段。</li>
 * <li><b>纵向定位</b>：Brandes-Köpf：每个点尽量让连线两端的出入口同高（能走直线就走直线，长连线优先笔直），
 * 在保持顺序与最小间距的前提下紧凑排开。</li>
 * <li><b>正交走线</b>：连线横着走，两端不同高时拐弯的竖段放在列与列之间的通道里，每条竖段独占一条竖线（轨道）。
 * 轨道的左右顺序按"横段穿过别的竖段、横段碰到别的竖段端点（看着像连上了）、不同连线的横段重合"加权计数，逐条挪到代价最小的位置。
 * 通道宽度按轨道数加宽；数据等级分界处的通道正中留给分区线。</li>
 * </ol>
 */
public final class TechTreeAutoLayout {

    /** 节点见方的边长（世界单位）。 */
    public static final int NODE_SIZE = 26;
    /// 同一列里相邻两点中心的最小间距：节点与节点、节点与虚拟点（连线从节点之间穿过）、虚拟点与虚拟点
    private static final int NODE_GAP = 48;
    private static final int NODE_DUMMY_GAP = 24;
    private static final int DUMMY_GAP = 6;
    /// 节点一侧相邻出入口的间距，与出入口离节点上下边的最小距离
    private static final int PORT_GAP = 6;
    private static final int PORT_MARGIN = 3;
    /// 列与列之间通道的最小宽度，与轨道间距
    private static final int MIN_CHANNEL = 38;
    private static final int TRACK_SPACING = 6;
    private static final int ORDER_SWEEPS = 24;
    private static final int TRANSPOSE_PASSES = 8;
    private static final int SIFT_PASSES = 8;
    /// 轨道排序的代价：横段穿过竖段、横段碰到竖段端点、两条不同连线的横段重合
    private static final int CROSS_COST = 1;
    private static final int TOUCH_COST = 4;
    private static final int OVERLAP_COST = 10;

    private TechTreeAutoLayout() {}

    public static TechTreeLayout create(Collection<TechNode> definitions) {
        if (definitions.isEmpty()) {
            return new TechTreeLayout(Collections.emptyList(), Collections.emptyMap(), Collections.emptyList(),
                    new IntArrayList(), Collections.emptyList(), 0, 0, 0, 0);
        }
        List<TechNode> nodes = new ArrayList<>(definitions);
        nodes.sort(NAME_ORDER);

        Reference2ObjectOpenHashMap<TechNode, List<TechNode>> children = new Reference2ObjectOpenHashMap<>(nodes.size());
        Reference2IntOpenHashMap<TechNode> indegrees = new Reference2IntOpenHashMap<>(nodes.size());
        for (var node : nodes) {
            children.put(node, new ArrayList<>());
            indegrees.put(node, 0);
        }
        for (var node : nodes) {
            for (var prerequisite : node.prerequisites) {
                if (prerequisite.getManager() != node.getManager()) continue;
                List<TechNode> dependentNodes = children.get(prerequisite);
                if (dependentNodes == null) {
                    throw new IllegalStateException("Tech node " + node.name + " references unknown prerequisite " + prerequisite.name);
                }
                dependentNodes.add(node);
                indegrees.addTo(node, 1);
            }
        }
        List<TechNode> topoOrder = topologicalSort(nodes, indegrees, children);
        Map<TechNode, Integer> depths = assignTierDepths(topoOrder);
        var columns = buildTierColumns(topoOrder, depths);
        return new Graph(columns).layout();
    }

    // ==================== 分列 ====================

    private static List<TechNode> topologicalSort(List<TechNode> nodes, Reference2IntOpenHashMap<TechNode> indegrees,
                                                  Reference2ObjectOpenHashMap<TechNode, List<TechNode>> children) {
        PriorityQueue<TechNode> queue = new PriorityQueue<>(NAME_ORDER);
        for (var node : nodes) {
            if (indegrees.getInt(node) == 0) queue.add(node);
        }
        List<TechNode> ordered = new ArrayList<>(nodes.size());
        while (!queue.isEmpty()) {
            TechNode node = queue.remove();
            ordered.add(node);
            for (var child : children.get(node)) {
                int remaining = indegrees.getInt(child) - 1;
                indegrees.put(child, remaining);
                if (remaining == 0) queue.add(child);
            }
        }
        if (ordered.size() != nodes.size()) {
            List<String> cycleNodes = new ArrayList<>();
            for (var node : nodes) {
                if (indegrees.getInt(node) > 0) cycleNodes.add(node.name);
            }
            cycleNodes.sort(String::compareTo);
            throw new IllegalStateException("Tech tree contains prerequisite cycles: " + String.join(", ", cycleNodes));
        }
        return ordered;
    }

    /** 块内深度：只数同一数据等级里的前置（别的等级的前置在别的块里，不影响本块的列）。 */
    private static Map<TechNode, Integer> assignTierDepths(List<TechNode> topoOrder) {
        Map<TechNode, Integer> depths = new IdentityHashMap<>();
        for (var node : topoOrder) {
            int depth = 0;
            for (var prerequisite : node.prerequisites) {
                if (prerequisite.getManager() != node.getManager() || prerequisite.getTier() != node.getTier()) continue;
                Integer prerequisiteDepth = depths.get(prerequisite);
                if (prerequisiteDepth != null) depth = Math.max(depth, prerequisiteDepth + 1);
            }
            depths.put(node, depth);
        }
        return depths;
    }

    /** 按数据等级分块、块内按深度分列；返回各列的节点（按名称排好的初始顺序）与各块的列范围。 */
    private static TierColumns buildTierColumns(List<TechNode> topoOrder, Map<TechNode, Integer> depths) {
        Map<Integer, List<TechNode>> nodesByTier = new TreeMap<>();
        for (var node : topoOrder) nodesByTier.computeIfAbsent(node.getTier(), ignored -> new ArrayList<>()).add(node);

        List<List<TechNode>> nodesByColumn = new ArrayList<>();
        IntArrayList tiers = new IntArrayList(nodesByTier.size());
        IntArrayList tierStarts = new IntArrayList(nodesByTier.size());
        IntArrayList tierEnds = new IntArrayList(nodesByTier.size());
        int columnOffset = 0;
        for (var entry : nodesByTier.entrySet()) {
            TreeSet<Integer> tierDepths = new TreeSet<>();
            for (var node : entry.getValue()) tierDepths.add(depths.get(node));
            Map<Integer, Integer> localColumns = new TreeMap<>();
            int localColumn = 0;
            for (var depth : tierDepths) {
                localColumns.put(depth, localColumn++);
                nodesByColumn.add(new ArrayList<>());
            }
            for (var node : entry.getValue()) nodesByColumn.get(columnOffset + localColumns.get(depths.get(node))).add(node);
            int endColumn = columnOffset + tierDepths.size() - 1;
            for (int column = columnOffset; column <= endColumn; column++) nodesByColumn.get(column).sort(NAME_ORDER);
            tiers.add(entry.getKey().intValue());
            tierStarts.add(columnOffset);
            tierEnds.add(endColumn);
            columnOffset = endColumn + 1;
        }
        return new TierColumns(nodesByColumn, tiers, tierStarts, tierEnds);
    }

    private record TierColumns(List<List<TechNode>> nodesByColumn, IntArrayList tiers, IntArrayList tierStarts, IntArrayList tierEnds) {}

    // ==================== 分层图 ====================

    /** 分层图：点 0 ~ n-1 是节点，之后是虚拟点；边只连相邻两列（左 → 右）。 */
    private static final class Graph {

        private final TierColumns tierColumns;
        private final List<TechNode> nodes = new ArrayList<>();
        private final int columnCount;
        private final IntArrayList column = new IntArrayList();
        private final List<IntArrayList> succ = new ArrayList<>();
        private final List<IntArrayList> pred = new ArrayList<>();
        /// 各列的点（顺序即上下顺序）
        private IntArrayList[] layers;
        /// 点在所在列里的序号、中心的纵坐标
        private int[] pos;
        private int[] y;
        /// 出入口相对节点中心的纵向偏移，按相邻列连线（左点, 右点）查：左点的出口、右点的入口；虚拟点为 0
        private final Long2IntOpenHashMap exitOffset = new Long2IntOpenHashMap(), entryOffset = new Long2IntOpenHashMap();
        /// 原始依赖：前置、节点、经过的点（左 → 右）
        private final List<TechNode> edgeFrom = new ArrayList<>(), edgeTo = new ArrayList<>();
        private final List<int[]> edgePaths = new ArrayList<>();

        private Graph(TierColumns tierColumns) {
            this.tierColumns = tierColumns;
            var byColumn = tierColumns.nodesByColumn();
            columnCount = byColumn.size();
            layers = new IntArrayList[columnCount];
            Reference2IntOpenHashMap<TechNode> index = new Reference2IntOpenHashMap<>();
            index.defaultReturnValue(-1);
            for (int c = 0; c < columnCount; c++) {
                layers[c] = new IntArrayList(byColumn.get(c).size());
                for (var node : byColumn.get(c)) {
                    int v = addVertex(c);
                    nodes.add(node);
                    index.put(node, v);
                    layers[c].add(v);
                }
            }
            buildEdges(index);
        }

        private int addVertex(int c) {
            column.add(c);
            succ.add(new IntArrayList(2));
            pred.add(new IntArrayList(2));
            return column.size() - 1;
        }

        private void link(int u, int v) {
            var out = succ.get(u);
            if (out.contains(v)) return;
            out.add(v);
            pred.get(v).add(u);
        }

        /** 依赖拆成相邻列之间的边。连线没有方向箭头，统一从左边的端点走到右边的端点，跨列时每列一个自己的虚拟点。 */
        private void buildEdges(Reference2IntOpenHashMap<TechNode> index) {
            int n = nodes.size();
            for (int v = 0; v < n; v++) {
                var node = nodes.get(v);
                for (var prerequisite : node.prerequisites) {
                    int p = index.getInt(prerequisite);
                    if (p < 0) continue; // 别的树的前置不画线
                    int left = column.getInt(p) <= column.getInt(v) ? p : v, right = left == p ? v : p;
                    int span = column.getInt(right) - column.getInt(left);
                    if (span == 0) continue;
                    int[] path = new int[span + 1];
                    path[0] = left;
                    for (int i = 1; i < span; i++) {
                        int d = addVertex(column.getInt(left) + i);
                        layers[column.getInt(d)].add(d);
                        path[i] = d;
                    }
                    path[span] = right;
                    for (int i = 0; i < span; i++) link(path[i], path[i + 1]);
                    edgeFrom.add(prerequisite);
                    edgeTo.add(node);
                    edgePaths.add(path);
                }
            }
        }

        private boolean isNode(int v) {
            return v < nodes.size();
        }

        TechTreeLayout layout() {
            order();
            computePorts();
            place();
            return route();
        }

        // ==================== 列内排序 ====================

        /**
         * 连通分量各自排序；每列里按分量依次摆放（分量之间不交错），纵向定位时各分量自然上下排开、互不穿插。
         * 分量按最左的列、再按名称顺序排。
         */
        private void order() {
            int vertexCount = column.size();
            pos = new int[vertexCount];
            int[] component = components();
            int componentCount = 0;
            for (int c : component) componentCount = Math.max(componentCount, c + 1);
            IntArrayList[][] componentLayers = new IntArrayList[componentCount][columnCount];
            int[] firstColumn = new int[componentCount], firstVertex = new int[componentCount];
            Arrays.fill(firstColumn, Integer.MAX_VALUE);
            Arrays.fill(firstVertex, Integer.MAX_VALUE);
            for (int k = 0; k < componentCount; k++) {
                for (int c = 0; c < columnCount; c++) componentLayers[k][c] = new IntArrayList();
            }
            for (int c = 0; c < columnCount; c++) {
                var layer = layers[c];
                for (int i = 0; i < layer.size(); i++) {
                    int v = layer.getInt(i), k = component[v];
                    componentLayers[k][c].add(v);
                    firstColumn[k] = Math.min(firstColumn[k], c);
                    if (isNode(v)) firstVertex[k] = Math.min(firstVertex[k], v);
                }
            }
            Integer[] componentOrder = new Integer[componentCount];
            for (int k = 0; k < componentCount; k++) componentOrder[k] = k;
            Arrays.sort(componentOrder, Comparator.<Integer>comparingInt(k -> firstColumn[k]).thenComparingInt(k -> firstVertex[k]));

            IntArrayList[] all = layers;
            for (int k = 0; k < componentCount; k++) {
                layers = componentLayers[k];
                orderLayers();
            }
            layers = all;
            for (int c = 0; c < columnCount; c++) {
                layers[c].clear();
                for (var k : componentOrder) layers[c].addAll(componentLayers[k][c]);
            }
            updatePositions();
        }

        /** 连通分量编号（并查集）。 */
        private int[] components() {
            int vertexCount = column.size();
            int[] parent = new int[vertexCount];
            for (int v = 0; v < vertexCount; v++) parent[v] = v;
            for (int u = 0; u < vertexCount; u++) {
                var out = succ.get(u);
                for (int j = 0; j < out.size(); j++) {
                    int a = find(parent, u), b = find(parent, out.getInt(j));
                    if (a != b) parent[a] = b;
                }
            }
            int[] ids = new int[vertexCount];
            Arrays.fill(ids, -1);
            int[] component = new int[vertexCount];
            int next = 0;
            for (int v = 0; v < vertexCount; v++) {
                int r = find(parent, v);
                if (ids[r] < 0) ids[r] = next++;
                component[v] = ids[r];
            }
            return component;
        }

        private static int find(int[] parent, int v) {
            while (parent[v] != v) {
                parent[v] = parent[parent[v]];
                v = parent[v];
            }
            return v;
        }

        /** 对当前的 {@link #layers}（一个连通分量）做重心法扫描 + 相邻交换，取交叉最少的结果。 */
        private void orderLayers() {
            updatePositions();
            IntArrayList[] best = copyLayers();
            long bestCrossings = crossings();
            for (int sweep = 0; sweep < ORDER_SWEEPS && bestCrossings > 0; sweep++) {
                if (sweep % 2 == 0) {
                    for (int c = 1; c < columnCount; c++) sortByBarycenter(c, pred);
                } else {
                    for (int c = columnCount - 2; c >= 0; c--) sortByBarycenter(c, succ);
                }
                transpose();
                long crossings = crossings();
                if (crossings < bestCrossings) {
                    bestCrossings = crossings;
                    best = copyLayers();
                }
            }
            System.arraycopy(best, 0, layers, 0, columnCount);
            updatePositions();
        }

        private IntArrayList[] copyLayers() {
            IntArrayList[] copy = new IntArrayList[columnCount];
            for (int c = 0; c < columnCount; c++) copy[c] = layers[c].clone();
            return copy;
        }

        private void updatePositions() {
            for (var layer : layers) {
                for (int i = 0; i < layer.size(); i++) pos[layer.getInt(i)] = i;
            }
        }

        private void sortByBarycenter(int c, List<IntArrayList> neighbors) {
            var layer = layers[c];
            int size = layer.size();
            if (size < 2) return;
            double[] key = new double[column.size()];
            for (int i = 0; i < size; i++) {
                int v = layer.getInt(i);
                var list = neighbors.get(v);
                if (list.isEmpty()) {
                    key[v] = i;
                    continue;
                }
                double sum = 0;
                for (int j = 0; j < list.size(); j++) sum += pos[list.getInt(j)];
                key[v] = sum / list.size();
            }
            int[] order = layer.toIntArray();
            IntArrays.mergeSort(order, (a, b) -> {
                int cmp = Double.compare(key[a], key[b]);
                return cmp != 0 ? cmp : Integer.compare(pos[a], pos[b]);
            });
            layer.clear();
            layer.addElements(0, order);
            for (int i = 0; i < size; i++) pos[order[i]] = i;
        }

        /** 相邻交换：交换后与左右两列的交叉更少就换，直到没有可换的。 */
        private void transpose() {
            for (int pass = 0; pass < TRANSPOSE_PASSES; pass++) {
                boolean improved = false;
                for (int c = 0; c < columnCount; c++) {
                    var layer = layers[c];
                    for (int i = 0; i + 1 < layer.size(); i++) {
                        int u = layer.getInt(i), v = layer.getInt(i + 1);
                        if (pairCrossings(v, u) < pairCrossings(u, v)) {
                            layer.set(i, v);
                            layer.set(i + 1, u);
                            pos[v] = i;
                            pos[u] = i + 1;
                            improved = true;
                        }
                    }
                }
                if (!improved) return;
            }
        }

        /** {@code u} 在 {@code v} 上面时，两者与左右两列的连线交叉数。 */
        private int pairCrossings(int u, int v) {
            return inversions(pred.get(u), pred.get(v)) + inversions(succ.get(u), succ.get(v));
        }

        private int inversions(IntArrayList a, IntArrayList b) {
            int count = 0;
            for (int i = 0; i < a.size(); i++) {
                int pa = pos[a.getInt(i)];
                for (int j = 0; j < b.size(); j++) {
                    if (pa > pos[b.getInt(j)]) count++;
                }
            }
            return count;
        }

        private long crossings() {
            long total = 0;
            IntArrayList from = new IntArrayList(), to = new IntArrayList();
            for (int c = 0; c + 1 < columnCount; c++) {
                from.clear();
                to.clear();
                var layer = layers[c];
                for (int i = 0; i < layer.size(); i++) {
                    var out = succ.get(layer.getInt(i));
                    for (int j = 0; j < out.size(); j++) {
                        from.add(i);
                        to.add(pos[out.getInt(j)]);
                    }
                }
                for (int a = 0; a < from.size(); a++) {
                    for (int b = a + 1; b < from.size(); b++) {
                        long d = (long) (from.getInt(a) - from.getInt(b)) * (to.getInt(a) - to.getInt(b));
                        if (d < 0) total++;
                    }
                }
            }
            return total;
        }

        // ==================== 出入口 ====================

        /**
         * 节点两侧的出入口：一个节点的多条连线各占一个出入口（纵向错开 {@link #PORT_GAP}，放不下时收紧），
         * 按另一端在相邻列里的上下顺序排，连线在节点旁不交叉、不重合。虚拟点只有一进一出，在正中。
         */
        private void computePorts() {
            for (int v = 0; v < nodes.size(); v++) {
                assignPorts(v, succ.get(v), exitOffset, true);
                assignPorts(v, pred.get(v), entryOffset, false);
            }
        }

        private void assignPorts(int v, IntArrayList neighbors, Long2IntOpenHashMap offsets, boolean exit) {
            int k = neighbors.size();
            if (k == 0) return;
            int[] sorted = neighbors.toIntArray();
            IntArrays.quickSort(sorted, (a, b) -> Integer.compare(pos[a], pos[b]));
            double spacing = k > 1 ? Math.min(PORT_GAP, (NODE_SIZE - 2.0 * PORT_MARGIN) / (k - 1)) : 0;
            for (int i = 0; i < k; i++) {
                int offset = (int) Math.round((i - (k - 1) / 2.0) * spacing);
                offsets.put(exit ? edgeKey(v, sorted[i]) : edgeKey(sorted[i], v), offset);
            }
        }

        /** 点 {@code v} 上连到相邻列的点 {@code other} 的那个出入口，相对 {@code v} 中心的纵向偏移。 */
        private int portOffset(int v, int other) {
            return column.getInt(other) > column.getInt(v) ? exitOffset.get(edgeKey(v, other)) : entryOffset.get(edgeKey(other, v));
        }

        // ==================== 纵向定位（Brandes-Köpf） ====================

        private int gap(int upper, int lower) {
            boolean a = isNode(upper), b = isNode(lower);
            return a && b ? NODE_GAP : a || b ? NODE_DUMMY_GAP : DUMMY_GAP;
        }

        /**
         * Brandes-Köpf 纵向定位（带出入口偏移）：每个点尽量与它在相邻列里的中位邻点连成一条横线——
         * 对齐的是两端的出入口，不是节点中心，连线是真正的直线；长连线的虚拟点链优先保持笔直（与之交叉的其他连线让路）。
         * 对齐成块后，块在保持顺序与最小间距的前提下尽量紧凑地排开。
         * 左右两个方向 × 上下两个方向各做一遍，取直线最多的那个（相同时取总高度最小的）；不取平均，保证对齐的线分毫不差。
         */
        private void place() {
            int vertexCount = column.size();
            y = new int[vertexCount];
            if (vertexCount == 0) return;
            LongOpenHashSet conflicts = innerSegmentConflicts();
            int bestStraight = -1, bestSpan = Integer.MAX_VALUE;
            for (int variant = 0; variant < 4; variant++) {
                boolean fromRight = variant >= 2, bottomUp = (variant & 1) == 1;
                IntArrayList[] adjusted = new IntArrayList[columnCount];
                for (int c = 0; c < columnCount; c++) {
                    var layer = layers[fromRight ? columnCount - 1 - c : c].clone();
                    if (bottomUp) IntArrays.reverse(layer.elements(), 0, layer.size());
                    adjusted[c] = layer;
                }
                int[] shift = new int[vertexCount];
                int[] root = align(adjusted, conflicts, fromRight ? succ : pred, bottomUp ? -1 : 1, shift);
                int[] ys = compact(adjusted, root, shift);
                if (bottomUp) {
                    for (int v = 0; v < vertexCount; v++) ys[v] = -ys[v];
                }
                int straight = straightCount(ys), min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                for (int value : ys) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
                if (straight > bestStraight || (straight == bestStraight && max - min < bestSpan)) {
                    bestStraight = straight;
                    bestSpan = max - min;
                    y = ys;
                }
            }
            int[] nodeYs = Arrays.copyOf(y, nodes.size());
            Arrays.sort(nodeYs);
            int center = nodeYs[nodeYs.length / 2];
            for (int v = 0; v < vertexCount; v++) y[v] -= center;
        }

        /** 两端出入口同高（画出来是直线）的相邻列连线数。 */
        private int straightCount(int[] ys) {
            int count = 0;
            for (int u = 0; u < ys.length; u++) {
                var out = succ.get(u);
                for (int j = 0; j < out.size(); j++) {
                    int v = out.getInt(j);
                    if (ys[u] + portOffset(u, v) == ys[v] + portOffset(v, u)) count++;
                }
            }
            return count;
        }

        /**
         * 与虚拟点链（两端都是虚拟点的段）交叉的其他段：对齐时不让它们成块，长连线保持笔直（Brandes-Köpf 的第一类冲突）。
         */
        private LongOpenHashSet innerSegmentConflicts() {
            LongOpenHashSet conflicts = new LongOpenHashSet();
            for (int c = 1; c < columnCount; c++) {
                var previous = layers[c - 1];
                var layer = layers[c];
                if (layer.isEmpty()) continue;
                int k0 = 0, scanPos = 0, last = layer.getInt(layer.size() - 1);
                for (int i = 0; i < layer.size(); i++) {
                    int v = layer.getInt(i);
                    int inner = -1;
                    if (!isNode(v)) {
                        var in = pred.get(v);
                        for (int j = 0; j < in.size(); j++) {
                            if (!isNode(in.getInt(j))) inner = in.getInt(j);
                        }
                    }
                    int k1 = inner >= 0 ? pos[inner] : previous.size();
                    if (inner >= 0 || v == last) {
                        for (int s = scanPos; s <= i; s++) {
                            int scan = layer.getInt(s);
                            var in = pred.get(scan);
                            for (int j = 0; j < in.size(); j++) {
                                int u = in.getInt(j), uPos = pos[u];
                                if ((uPos < k0 || k1 < uPos) && (isNode(u) || isNode(scan))) conflicts.add(pairKey(u, scan));
                            }
                        }
                        scanPos = i + 1;
                        k0 = k1;
                    }
                }
            }
            return conflicts;
        }

        private static long pairKey(int a, int b) {
            return a < b ? edgeKey(a, b) : edgeKey(b, a);
        }

        /**
         * 对齐成块：每个点与邻列里的中位邻点（顺序不冲突、不是第一类冲突时）同块，返回每个点所在块的根；
         * {@code shift} 填入每个点相对块根的偏移（让两端出入口同高），{@code sign} 为 -1 时坐标轴上下翻转。
         */
        private int[] align(IntArrayList[] adjusted, LongOpenHashSet conflicts, List<IntArrayList> neighbors, int sign, int[] shift) {
            int vertexCount = column.size();
            int[] root = new int[vertexCount], alignTo = new int[vertexCount], p = new int[vertexCount];
            for (int v = 0; v < vertexCount; v++) root[v] = alignTo[v] = v;
            for (var layer : adjusted) {
                for (int i = 0; i < layer.size(); i++) p[layer.getInt(i)] = i;
            }
            for (var layer : adjusted) {
                int r = -1;
                for (int i = 0; i < layer.size(); i++) {
                    int v = layer.getInt(i);
                    var list = neighbors.get(v);
                    if (list.isEmpty()) continue;
                    int[] ws = list.toIntArray();
                    IntArrays.quickSort(ws, (a, b) -> Integer.compare(p[a], p[b]));
                    int lo = (ws.length - 1) / 2, hi = ws.length / 2;
                    for (int m = lo; m <= hi; m++) {
                        int w = ws[m];
                        if (alignTo[v] == v && r < p[w] && !conflicts.contains(pairKey(v, w))) {
                            alignTo[w] = v;
                            root[v] = root[w];
                            alignTo[v] = root[v];
                            shift[v] = shift[w] + sign * (portOffset(w, v) - portOffset(v, w));
                            r = p[w];
                        }
                    }
                }
            }
            return root;
        }

        /**
         * 块的紧凑排布：块图（同列相邻两点所在的块之间一条边，权重是两点的最小间距再计入两点在块里的偏移）上
         * 先按最长路径取最小坐标，再逆序把每块推到紧挨着下方的块（减少不必要的空隙）。返回每个点的坐标。
         */
        private int[] compact(IntArrayList[] adjusted, int[] root, int[] shift) {
            int vertexCount = column.size();
            Long2IntOpenHashMap weights = new Long2IntOpenHashMap();
            weights.defaultReturnValue(Integer.MIN_VALUE);
            for (var layer : adjusted) {
                for (int i = 1; i < layer.size(); i++) {
                    int u = layer.getInt(i - 1), v = layer.getInt(i);
                    long key = edgeKey(root[u], root[v]);
                    weights.put(key, Math.max(weights.get(key), gap(u, v) + shift[u] - shift[v]));
                }
            }
            List<IntArrayList> outTargets = new ArrayList<>(vertexCount), outWeights = new ArrayList<>(vertexCount);
            for (int v = 0; v < vertexCount; v++) {
                outTargets.add(null);
                outWeights.add(null);
            }
            int[] indegree = new int[vertexCount];
            var it = weights.long2IntEntrySet().fastIterator();
            while (it.hasNext()) {
                var entry = it.next();
                int from = (int) (entry.getLongKey() >>> 32), to = (int) entry.getLongKey();
                if (outTargets.get(from) == null) {
                    outTargets.set(from, new IntArrayList(2));
                    outWeights.set(from, new IntArrayList(2));
                }
                outTargets.get(from).add(to);
                outWeights.get(from).add(entry.getIntValue());
                indegree[to]++;
            }
            IntArrayList topo = new IntArrayList(vertexCount);
            for (int v = 0; v < vertexCount; v++) {
                if (root[v] == v && indegree[v] == 0) topo.add(v);
            }
            for (int head = 0; head < topo.size(); head++) {
                var targets = outTargets.get(topo.getInt(head));
                if (targets == null) continue;
                for (int j = 0; j < targets.size(); j++) {
                    int w = targets.getInt(j);
                    if (--indegree[w] == 0) topo.add(w);
                }
            }
            long[] xs = new long[vertexCount];
            boolean[] placed = new boolean[vertexCount];
            for (int t = 0; t < topo.size(); t++) {
                int v = topo.getInt(t);
                if (!placed[v]) {
                    placed[v] = true;
                    xs[v] = 0;
                }
                var targets = outTargets.get(v);
                if (targets == null) continue;
                for (int j = 0; j < targets.size(); j++) {
                    int w = targets.getInt(j);
                    long candidate = xs[v] + outWeights.get(v).getInt(j);
                    if (!placed[w] || candidate > xs[w]) {
                        xs[w] = candidate;
                        placed[w] = true;
                    }
                }
            }
            for (int t = topo.size() - 1; t >= 0; t--) {
                int v = topo.getInt(t);
                var targets = outTargets.get(v);
                if (targets == null) continue;
                long limit = Long.MAX_VALUE;
                for (int j = 0; j < targets.size(); j++) limit = Math.min(limit, xs[targets.getInt(j)] - outWeights.get(v).getInt(j));
                xs[v] = Math.max(xs[v], limit);
            }
            int[] ys = new int[vertexCount];
            for (int v = 0; v < vertexCount; v++) ys[v] = (int) xs[root[v]] + shift[v];
            return ys;
        }

        // ==================== 正交走线 ====================

        /** 一段相邻列之间的连线：左右两端出入口的纵坐标，需要竖段时占一条轨道。 */
        private static final class Segment {

            final int leftY, rightY, top, bottom;
            /// 轨道序号（从左往右），-1 为不需要竖段（两端同高）
            int track = -1;

            Segment(int leftY, int rightY) {
                this.leftY = leftY;
                this.rightY = rightY;
                this.top = Math.min(leftY, rightY);
                this.bottom = Math.max(leftY, rightY);
            }

            boolean needsTrack() {
                return top < bottom;
            }

            /** 别的横段在高度 {@code yy} 经过本段竖线的代价：穿过、碰到端点（看着像连上了）。 */
            int passCost(int yy) {
                if (yy > top && yy < bottom) return CROSS_COST;
                if (yy == top || yy == bottom) return TOUCH_COST;
                return 0;
            }
        }

        private TechTreeLayout route() {
            int n = nodes.size();
            boolean[] boundary = new boolean[Math.max(0, columnCount - 1)];
            for (int t = 0; t + 1 < tierColumns.tierEnds().size(); t++) boundary[tierColumns.tierEnds().getInt(t)] = true;

            // 每个通道：每段连线一条轨道（两端同高的不要），排轨道顺序、定宽度
            Long2IntOpenHashMap segmentIndex = new Long2IntOpenHashMap();
            List<List<Segment>> channelSegments = new ArrayList<>(columnCount);
            int[] channelWidth = new int[Math.max(0, columnCount - 1)];
            int[] trackCount = new int[channelWidth.length];
            for (int c = 0; c + 1 < columnCount; c++) {
                List<Segment> segments = new ArrayList<>();
                var layer = layers[c];
                for (int i = 0; i < layer.size(); i++) {
                    int u = layer.getInt(i);
                    var out = succ.get(u);
                    for (int j = 0; j < out.size(); j++) {
                        int v = out.getInt(j);
                        segmentIndex.put(edgeKey(u, v), segments.size());
                        segments.add(new Segment(y[u] + portOffset(u, v), y[v] + portOffset(v, u)));
                    }
                }
                trackCount[c] = orderTracks(segments);
                int slots = trackCount[c] + (boundary[c] ? 1 : 0);
                channelWidth[c] = Math.max(MIN_CHANNEL, (slots + 1) * TRACK_SPACING);
                channelSegments.add(segments);
            }

            // 列的横坐标、轨道的横坐标、分区线
            int[] columnX = new int[columnCount];
            for (int c = 1; c < columnCount; c++) columnX[c] = columnX[c - 1] + NODE_SIZE + channelWidth[c - 1];
            int[][] trackX = new int[channelWidth.length][];
            IntArrayList separators = new IntArrayList();
            for (int c = 0; c < channelWidth.length; c++) {
                int slots = trackCount[c] + (boundary[c] ? 1 : 0);
                int separatorSlot = boundary[c] ? slots / 2 : -1;
                int start = columnX[c] + NODE_SIZE, width = channelWidth[c];
                trackX[c] = new int[trackCount[c]];
                for (int slot = 0, track = 0; slot < slots; slot++) {
                    int x = start + Math.round((slot + 1) * (float) width / (slots + 1));
                    if (slot == separatorSlot) separators.add(x);
                    else trackX[c][track++] = x;
                }
            }

            // 节点位置
            Map<TechNode, TechTreeLayout.NodePlacement> placements = new IdentityHashMap<>(n);
            List<TechNode> orderedNodes = new ArrayList<>(n);
            int half = NODE_SIZE / 2;
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (int c = 0; c < columnCount; c++) {
                var layer = layers[c];
                int row = 0;
                for (int i = 0; i < layer.size(); i++) {
                    int v = layer.getInt(i);
                    if (!isNode(v)) continue;
                    var node = nodes.get(v);
                    int x = columnX[c], top = y[v] - half;
                    placements.put(node, new TechTreeLayout.NodePlacement(x, top, c, row++, node.getTier()));
                    orderedNodes.add(node);
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, top);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, top);
                }
            }

            // 各块的范围
            var tiers = tierColumns.tiers();
            List<TechTreeLayout.TierRegion> regions = new ArrayList<>(tiers.size());
            for (int t = 0; t < tiers.size(); t++) {
                int start = tierColumns.tierStarts().getInt(t), end = tierColumns.tierEnds().getInt(t);
                regions.add(new TechTreeLayout.TierRegion(tiers.getInt(t), start, end, columnX[start], columnX[end]));
            }

            // 连线折线：从左端点右边的出口出发，每个通道里两端不同高就拐到自己的轨道上，到右端点左边的入口
            List<TechTreeLayout.RoutedEdge> edges = new ArrayList<>(edgePaths.size());
            FloatArrayList points = new FloatArrayList();
            for (int e = 0; e < edgePaths.size(); e++) {
                int[] path = edgePaths.get(e);
                int first = path[0], last = path[path.length - 1];
                points.clear();
                points.add(columnX[column.getInt(first)] + NODE_SIZE);
                points.add(y[first] + portOffset(first, path[1]));
                for (int i = 0; i + 1 < path.length; i++) {
                    int u = path[i], v = path[i + 1], c = column.getInt(u);
                    var segment = channelSegments.get(c).get(segmentIndex.get(edgeKey(u, v)));
                    if (segment.track < 0) continue;
                    float x = trackX[c][segment.track];
                    points.add(x);
                    points.add(segment.leftY);
                    points.add(x);
                    points.add(segment.rightY);
                }
                points.add(columnX[column.getInt(last)]);
                points.add(y[last] + portOffset(last, path[path.length - 2]));
                edges.add(new TechTreeLayout.RoutedEdge(edgeFrom.get(e), edgeTo.get(e), points.toFloatArray()));
            }
            return new TechTreeLayout(orderedNodes, placements, regions, separators, edges, minX, minY, maxX, maxY);
        }

        private static long edgeKey(int u, int v) {
            return (long) u << 32 | (v & 0xFFFFFFFFL);
        }

        /**
         * 给需要竖段的连线排轨道（从左往右）。{@code cost[a][b]} 是 a 在 b 左边时的代价：
         * b 的左侧横段（从左列到 b 的轨道）经过 a 的竖段、a 的右侧横段（从 a 的轨道到右列）经过 b 的竖段，
         * 以及 b 的左侧横段与 a 的右侧横段同高、在两条轨道之间重合。代价两两相加，逐条挪到代价最小的位置，直到不再变好。
         *
         * @return 轨道数
         */
        private static int orderTracks(List<Segment> segments) {
            List<Segment> tracked = new ArrayList<>(segments.size());
            for (var segment : segments) {
                if (segment.needsTrack()) tracked.add(segment);
            }
            int m = tracked.size();
            if (m == 0) return 0;
            int[][] cost = new int[m][m];
            for (int a = 0; a < m; a++) {
                for (int b = 0; b < m; b++) {
                    if (a != b) cost[a][b] = leftOfCost(tracked.get(a), tracked.get(b));
                }
            }
            // 初始：按纵向范围的中点
            Integer[] order = new Integer[m];
            for (int i = 0; i < m; i++) order[i] = i;
            Arrays.sort(order, Comparator.comparingInt(i -> tracked.get(i).top + tracked.get(i).bottom));
            IntArrayList sequence = new IntArrayList(m);
            for (var i : order) sequence.add(i.intValue());
            for (int pass = 0; pass < SIFT_PASSES; pass++) {
                boolean improved = false;
                for (int k = 0; k < m; k++) {
                    int item = sequence.getInt(k);
                    sequence.removeInt(k);
                    // 放在第 p 个位置的代价 = 左边各条对它 + 它对右边各条
                    int bestPos = 0, bestCost = Integer.MAX_VALUE, current = 0;
                    int after = 0;
                    for (int j = 0; j < sequence.size(); j++) after += cost[item][sequence.getInt(j)];
                    int before = 0;
                    for (int p = 0; p <= sequence.size(); p++) {
                        int total = before + after;
                        if (p == k) current = total;
                        if (total < bestCost) {
                            bestCost = total;
                            bestPos = p;
                        }
                        if (p < sequence.size()) {
                            int other = sequence.getInt(p);
                            before += cost[other][item];
                            after -= cost[item][other];
                        }
                    }
                    if (bestCost < current) improved = true;
                    else bestPos = k;
                    sequence.add(bestPos, item);
                }
                if (!improved) break;
            }
            for (int i = 0; i < m; i++) tracked.get(sequence.getInt(i)).track = i;
            return m;
        }

        /** a 的轨道在 b 左边时的代价（见 {@link #orderTracks}）。 */
        private static int leftOfCost(Segment a, Segment b) {
            int total = a.passCost(b.leftY) + b.passCost(a.rightY);
            if (b.leftY == a.rightY) total += OVERLAP_COST;
            return total;
        }
    }

    private static final Comparator<TechNode> NAME_ORDER = Comparator.comparing(node -> node.name);
}
