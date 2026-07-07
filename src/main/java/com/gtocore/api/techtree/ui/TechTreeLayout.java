package com.gtocore.api.techtree.ui;

import com.gtocore.api.techtree.TechNode;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class TechTreeLayout<T> {

    public record NodePlacement(int x, int y, int layer, int row) {}

    private final List<TechNode<T>> orderedNodes;
    private final Map<TechNode<T>, NodePlacement> placements;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    TechTreeLayout(List<TechNode<T>> orderedNodes,
                   Map<TechNode<T>, NodePlacement> placements,
                   int minX,
                   int minY,
                   int maxX,
                   int maxY) {
        this.orderedNodes = List.copyOf(orderedNodes);
        this.placements = Map.copyOf(new IdentityHashMap<>(placements));
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public List<TechNode<T>> orderedNodes() {
        return orderedNodes;
    }

    public NodePlacement getPlacement(TechNode<?> node) {
        @SuppressWarnings("unchecked")
        NodePlacement placement = placements.get((TechNode<T>) node);
        if (placement == null) {
            throw new IllegalArgumentException("Node " + node.name + " is not part of this layout");
        }
        return placement;
    }

    public int getX(TechNode<?> node) {
        return getPlacement(node).x();
    }

    public int getY(TechNode<?> node) {
        return getPlacement(node).y();
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
