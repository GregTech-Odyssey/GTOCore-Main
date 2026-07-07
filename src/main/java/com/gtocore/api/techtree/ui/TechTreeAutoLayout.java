package com.gtocore.api.techtree.ui;

import com.gtocore.api.techtree.TechNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class TechTreeAutoLayout {

    private static final int HORIZONTAL_SPACING = 64;
    private static final int VERTICAL_SPACING = 48;
    private static final int SWEEP_PASSES = 4;

    private TechTreeAutoLayout() {}

    public static <T> TechTreeLayout<T> create(Collection<TechNode<T>> definitions) {
        if (definitions.isEmpty()) {
            return new TechTreeLayout<>(List.of(), Map.of(), 0, 0, 0, 0);
        }

        List<TechNode<T>> nodes = new ArrayList<>(definitions);
        nodes.sort(nameOrder);

        Map<TechNode<T>, List<TechNode<T>>> children = new IdentityHashMap<>();
        Map<TechNode<T>, Integer> indegrees = new IdentityHashMap<>();
        for (var node : nodes) {
            children.put(node, new ArrayList<>());
            indegrees.put(node, node.prerequisites.size());
        }
        for (var node : nodes) {
            for (var prerequisite : node.prerequisites) {
                List<TechNode<T>> dependentNodes = children.get(prerequisite);
                if (dependentNodes == null) {
                    throw new IllegalStateException("Tech node " + node.name + " references unknown prerequisite " + prerequisite.name);
                }
                dependentNodes.add(node);
            }
        }
        for (var dependentNodes : children.values()) {
            dependentNodes.sort(nameOrder);
        }

        List<TechNode<T>> topoOrder = topologicalSort(nodes, indegrees, children);
        Map<TechNode<T>, Integer> layers = assignLayers(topoOrder);
        List<List<TechNode<T>>> nodesByLayer = groupByLayer(topoOrder, layers);
        optimizeLayerOrdering(nodesByLayer, children);

        Map<TechNode<T>, TechTreeLayout.NodePlacement> placements = new IdentityHashMap<>();
        List<TechNode<T>> orderedNodes = new ArrayList<>(nodes.size());
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int layer = 0; layer < nodesByLayer.size(); layer++) {
            var layerNodes = nodesByLayer.get(layer);
            int startY = -((layerNodes.size() - 1) * VERTICAL_SPACING) / 2;
            for (int row = 0; row < layerNodes.size(); row++) {
                TechNode<T> node = layerNodes.get(row);
                int x = layer * HORIZONTAL_SPACING;
                int y = startY + row * VERTICAL_SPACING;
                placements.put(node, new TechTreeLayout.NodePlacement(x, y, layer, row));
                orderedNodes.add(node);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        return new TechTreeLayout<>(orderedNodes, placements, minX, minY, maxX, maxY);
    }

    private static <T> List<TechNode<T>> topologicalSort(List<TechNode<T>> nodes,
                                                         Map<TechNode<T>, Integer> indegrees,
                                                         Map<TechNode<T>, List<TechNode<T>>> children) {
        PriorityQueue<TechNode<T>> queue = new PriorityQueue<>(nameOrder);
        for (var node : nodes) {
            if (indegrees.get(node) == 0) {
                queue.add(node);
            }
        }

        List<TechNode<T>> ordered = new ArrayList<>(nodes.size());
        while (!queue.isEmpty()) {
            TechNode<T> node = queue.remove();
            ordered.add(node);
            for (var child : children.get(node)) {
                int remaining = indegrees.get(child) - 1;
                indegrees.put(child, remaining);
                if (remaining == 0) {
                    queue.add(child);
                }
            }
        }

        if (ordered.size() != nodes.size()) {
            List<String> cycleNodes = new ArrayList<>();
            for (var node : nodes) {
                if (indegrees.get(node) > 0) {
                    cycleNodes.add(node.name);
                }
            }
            cycleNodes.sort(String::compareTo);
            throw new IllegalStateException("Tech tree contains prerequisite cycles: " + String.join(", ", cycleNodes));
        }
        return ordered;
    }

    private static <T> Map<TechNode<T>, Integer> assignLayers(List<TechNode<T>> topoOrder) {
        Map<TechNode<T>, Integer> layers = new IdentityHashMap<>();
        for (var node : topoOrder) {
            int layer = 0;
            for (var prerequisite : node.prerequisites) {
                layer = Math.max(layer, layers.get(prerequisite) + 1);
            }
            layers.put(node, layer);
        }
        return layers;
    }

    private static <T> List<List<TechNode<T>>> groupByLayer(List<TechNode<T>> topoOrder, Map<TechNode<T>, Integer> layers) {
        int maxLayer = 0;
        for (var layer : layers.values()) {
            maxLayer = Math.max(maxLayer, layer);
        }
        List<List<TechNode<T>>> nodesByLayer = new ArrayList<>(maxLayer + 1);
        for (int i = 0; i <= maxLayer; i++) {
            nodesByLayer.add(new ArrayList<>());
        }
        for (var node : topoOrder) {
            nodesByLayer.get(layers.get(node)).add(node);
        }
        for (var layerNodes : nodesByLayer) {
            layerNodes.sort(nameOrder);
        }
        return nodesByLayer;
    }

    private static <T> void optimizeLayerOrdering(List<List<TechNode<T>>> nodesByLayer,
                                                  Map<TechNode<T>, List<TechNode<T>>> children) {
        if (nodesByLayer.size() < 2) {
            return;
        }

        for (int pass = 0; pass < SWEEP_PASSES; pass++) {
            Map<TechNode<T>, Integer> rowIndices = buildRowIndices(nodesByLayer);
            for (int layer = 1; layer < nodesByLayer.size(); layer++) {
                sortLayer(nodesByLayer.get(layer), rowIndices, true, children);
                rowIndices = buildRowIndices(nodesByLayer);
            }

            rowIndices = buildRowIndices(nodesByLayer);
            for (int layer = nodesByLayer.size() - 2; layer >= 0; layer--) {
                sortLayer(nodesByLayer.get(layer), rowIndices, false, children);
                rowIndices = buildRowIndices(nodesByLayer);
            }
        }
    }

    private static <T> void sortLayer(List<TechNode<T>> layerNodes,
                                      Map<TechNode<T>, Integer> rowIndices,
                                      boolean usePrerequisites,
                                      Map<TechNode<T>, List<TechNode<T>>> children) {
        if (layerNodes.size() < 2) {
            return;
        }

        Map<TechNode<T>, Double> barycenters = new IdentityHashMap<>();
        for (var node : layerNodes) {
            List<TechNode<T>> neighbors = usePrerequisites ? node.prerequisites : children.get(node);
            barycenters.put(node, averageNeighborRow(neighbors, rowIndices, rowIndices.getOrDefault(node, 0)));
        }

        layerNodes.sort(Comparator
                .comparingDouble((TechNode<T> node) -> barycenters.get(node))
                .thenComparingInt(node -> rowIndices.getOrDefault(node, 0))
                .thenComparing(node -> node.name));
    }

    private static <T> double averageNeighborRow(List<TechNode<T>> neighbors,
                                                 Map<TechNode<T>, Integer> rowIndices,
                                                 int fallback) {
        if (neighbors.isEmpty()) {
            return fallback;
        }

        double sum = 0;
        int count = 0;
        for (var neighbor : neighbors) {
            Integer row = rowIndices.get(neighbor);
            if (row != null) {
                sum += row;
                count++;
            }
        }
        return count == 0 ? fallback : sum / count;
    }

    private static <T> Map<TechNode<T>, Integer> buildRowIndices(List<List<TechNode<T>>> nodesByLayer) {
        Map<TechNode<T>, Integer> rowIndices = new IdentityHashMap<>();
        for (var layerNodes : nodesByLayer) {
            for (int row = 0; row < layerNodes.size(); row++) {
                rowIndices.put(layerNodes.get(row), row);
            }
        }
        return rowIndices;
    }

    private static final Comparator<TechNode> nameOrder = Comparator.comparing(node -> node.name);
}
