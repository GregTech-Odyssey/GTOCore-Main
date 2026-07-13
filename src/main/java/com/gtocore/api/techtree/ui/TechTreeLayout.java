package com.gtocore.api.techtree.ui;

import com.gtocore.api.techtree.TechNode;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class TechTreeLayout {

    public record NodePlacement(int x, int y, int column, int row, int tier) {

        public int layer() {
            return column;
        }
    }

    public record TierRegion(int tier, int startColumn, int endColumn, int minX, int maxX) {}

    private final List<TechNode> orderedNodes;
    private final Map<TechNode, NodePlacement> placements;
    private final List<TierRegion> tierRegions;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    TechTreeLayout(List<TechNode> orderedNodes,
                   Map<TechNode, NodePlacement> placements,
                   List<TierRegion> tierRegions,
                   int minX,
                   int minY,
                   int maxX,
                   int maxY) {
        this.orderedNodes = List.copyOf(orderedNodes);
        this.placements = Map.copyOf(new IdentityHashMap<>(placements));
        this.tierRegions = List.copyOf(tierRegions);
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
