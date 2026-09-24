package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** 一棵树的布局结果（{@link TechTreeAutoLayout} 算出、按树缓存）：节点位置、数据等级分块、分区线位置与走好的连线。 */
public final class TechTreeLayout {

    public record NodePlacement(int x, int y, int column, int row, int tier) {

        public int layer() {
            return column;
        }
    }

    public record TierRegion(int tier, int startColumn, int endColumn, int minX, int maxX) {}

    /** 一条依赖的正交折线（{@code points} 为 x0, y0, x1, y1…，从左边的端点到右边的端点，端点在节点中心）。 */
    public record RoutedEdge(TechNode from, TechNode to, float[] points) {}

    private final List<TechNode> orderedNodes;
    private final Map<TechNode, NodePlacement> placements;
    private final List<TierRegion> tierRegions;
    private final IntArrayList tierSeparators;
    private final List<RoutedEdge> edges;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    TechTreeLayout(List<TechNode> orderedNodes,
                   Map<TechNode, NodePlacement> placements,
                   List<TierRegion> tierRegions,
                   IntArrayList tierSeparators,
                   List<RoutedEdge> edges,
                   int minX,
                   int minY,
                   int maxX,
                   int maxY) {
        this.orderedNodes = List.copyOf(orderedNodes);
        this.placements = new IdentityHashMap<>(placements);
        this.tierRegions = List.copyOf(tierRegions);
        this.tierSeparators = tierSeparators;
        this.edges = List.copyOf(edges);
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public List<TechNode> orderedNodes() {
        return orderedNodes;
    }

    public NodePlacement getPlacement(TechNode node) {
        NodePlacement placement = placements.get(node);
        if (placement == null) {
            throw new IllegalArgumentException("Node " + node.name + " is not part of this layout");
        }
        return placement;
    }

    public int getX(TechNode node) {
        return getPlacement(node).x();
    }

    public int getY(TechNode node) {
        return getPlacement(node).y();
    }

    public List<TierRegion> tierRegions() {
        return tierRegions;
    }

    /** 相邻数据等级之间分区线的横坐标（从左往右，比 {@link #tierRegions()} 少一个）。 */
    public IntArrayList tierSeparators() {
        return tierSeparators;
    }

    /** 本树内的所有依赖连线（别的树的前置不画线）。 */
    public List<RoutedEdge> edges() {
        return edges;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }
}
