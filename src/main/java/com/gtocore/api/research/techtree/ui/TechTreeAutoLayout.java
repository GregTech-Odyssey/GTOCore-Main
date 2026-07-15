package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

public final class TechTreeAutoLayout {

    private static final int HORIZONTAL_SPACING = 64;
    private static final int VERTICAL_SPACING = 48;
    private static final int SWEEP_PASSES = 4;

    private TechTreeAutoLayout() {}

    public static TechTreeLayout create(Collection<TechNode> definitions) {
        if (definitions.isEmpty()) {
            return new TechTreeLayout(List.of(), Map.of(), List.of(), 0, 0, 0, 0);
        }

        List<TechNode> nodes = new ArrayList<>(definitions);
        nodes.sort(nameOrder);

        Map<TechNode, List<TechNode>> children = new IdentityHashMap<>();
        Map<TechNode, Integer> indegrees = new IdentityHashMap<>();
        for (var node : nodes) {
            children.put(node, new ArrayList<>());
            indegrees.put(node, node.prerequisites.size());
        }
        for (var node : nodes) {
            for (var prerequisite : node.prerequisites) {
                List<TechNode> dependentNodes = children.get(prerequisite);
                if (dependentNodes == null) {
                    throw new IllegalStateException("Tech node " + node.name + " references unknown prerequisite " + prerequisite.name);
                }
                dependentNodes.add(node);
            }
        }
        for (var dependentNodes : children.values()) {
            dependentNodes.sort(nameOrder);
        }

        List<TechNode> topoOrder = topologicalSort(nodes, indegrees, children);
        Map<TechNode, Integer> depths = assignDepths(topoOrder);
        TierColumnLayout tierColumnLayout = buildTierColumns(topoOrder, depths);
        List<List<TechNode>> nodesByColumn = tierColumnLayout.nodesByColumn();
        optimizeColumnOrdering(nodesByColumn, children);

        Map<TechNode, TechTreeLayout.NodePlacement> placements = new IdentityHashMap<>();
        List<TechNode> orderedNodes = new ArrayList<>(nodes.size());
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int column = 0; column < nodesByColumn.size(); column++) {
            var columnNodes = nodesByColumn.get(column);
            int startY = -((columnNodes.size() - 1) * VERTICAL_SPACING) / 2;
            for (int row = 0; row < columnNodes.size(); row++) {
                TechNode node = columnNodes.get(row);
                int x = column * HORIZONTAL_SPACING;
                int y = startY + row * VERTICAL_SPACING;
                placements.put(node, new TechTreeLayout.NodePlacement(x, y, column, row, node.getTier()));
                orderedNodes.add(node);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        return new TechTreeLayout(orderedNodes, placements, tierColumnLayout.tierRegions(), minX, minY, maxX, maxY);
    }

    private static List<TechNode> topologicalSort(List<TechNode> nodes,
                                                  Map<TechNode, Integer> indegrees,
                                                  Map<TechNode, List<TechNode>> children) {
        PriorityQueue<TechNode> queue = new PriorityQueue<>(nameOrder);
        for (var node : nodes) {
            if (indegrees.get(node) == 0) {
                queue.add(node);
            }
        }

        List<TechNode> ordered = new ArrayList<>(nodes.size());
        while (!queue.isEmpty()) {
            TechNode node = queue.remove();
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

    private static Map<TechNode, Integer> assignDepths(List<TechNode> topoOrder) {
        Map<TechNode, Integer> depths = new IdentityHashMap<>();
        for (var node : topoOrder) {
            int depth = 0;
            for (var prerequisite : node.prerequisites) {
                depth = Math.max(depth, depths.get(prerequisite) + 1);
            }
            depths.put(node, depth);
        }
        return depths;
    }

    private static TierColumnLayout buildTierColumns(List<TechNode> topoOrder,
                                                     Map<TechNode, Integer> depths) {
        Map<Integer, List<TechNode>> nodesByTier = new TreeMap<>();
        for (var node : topoOrder) {
            nodesByTier.computeIfAbsent(node.getTier(), ignored -> new ArrayList<>()).add(node);
        }

        List<List<TechNode>> nodesByColumn = new ArrayList<>();
        List<TechTreeLayout.TierRegion> tierRegions = new ArrayList<>(nodesByTier.size());
        int columnOffset = 0;

        for (var entry : nodesByTier.entrySet()) {
            TreeSet<Integer> tierDepths = new TreeSet<>();
            for (var node : entry.getValue()) {
                tierDepths.add(depths.get(node));
            }

            Map<Integer, Integer> localColumns = new HashMap<>(tierDepths.size());
            int localColumn = 0;
            for (var depth : tierDepths) {
                localColumns.put(depth, localColumn++);
                nodesByColumn.add(new ArrayList<>());
            }

            for (var node : entry.getValue()) {
                int globalColumn = columnOffset + localColumns.get(depths.get(node));
                nodesByColumn.get(globalColumn).add(node);
            }

            int startColumn = columnOffset;
            int endColumn = columnOffset + tierDepths.size() - 1;
            for (int column = startColumn; column <= endColumn; column++) {
                nodesByColumn.get(column).sort(nameOrder);
            }
            tierRegions.add(new TechTreeLayout.TierRegion(entry.getKey(), startColumn, endColumn,
                    startColumn * HORIZONTAL_SPACING, endColumn * HORIZONTAL_SPACING));
            columnOffset = endColumn + 1;
        }

        return new TierColumnLayout(nodesByColumn, tierRegions);
    }

    private static void optimizeColumnOrdering(List<List<TechNode>> nodesByColumn,
                                               Map<TechNode, List<TechNode>> children) {
        if (nodesByColumn.size() < 2) {
            return;
        }

        for (int pass = 0; pass < SWEEP_PASSES; pass++) {
            Map<TechNode, Integer> rowIndices = buildRowIndices(nodesByColumn);
            for (int column = 1; column < nodesByColumn.size(); column++) {
                sortColumn(nodesByColumn.get(column), rowIndices, true, children);
                rowIndices = buildRowIndices(nodesByColumn);
            }

            rowIndices = buildRowIndices(nodesByColumn);
            for (int column = nodesByColumn.size() - 2; column >= 0; column--) {
                sortColumn(nodesByColumn.get(column), rowIndices, false, children);
                rowIndices = buildRowIndices(nodesByColumn);
            }
        }
    }

    private static void sortColumn(List<TechNode> columnNodes,
                                   Map<TechNode, Integer> rowIndices,
                                   boolean usePrerequisites,
                                   Map<TechNode, List<TechNode>> children) {
        if (columnNodes.size() < 2) {
            return;
        }

        Map<TechNode, Double> barycenters = new IdentityHashMap<>();
        for (var node : columnNodes) {
            List<TechNode> neighbors = usePrerequisites ? node.prerequisites : children.get(node);
            barycenters.put(node, averageNeighborRow(neighbors, rowIndices, rowIndices.getOrDefault(node, 0)));
        }

        columnNodes.sort(Comparator
                .comparingDouble((TechNode node) -> barycenters.get(node))
                .thenComparingInt(node -> rowIndices.getOrDefault(node, 0))
                .thenComparing(node -> node.name));
    }

    private static double averageNeighborRow(List<TechNode> neighbors,
                                             Map<TechNode, Integer> rowIndices,
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

    private static Map<TechNode, Integer> buildRowIndices(List<List<TechNode>> nodesByColumn) {
        Map<TechNode, Integer> rowIndices = new IdentityHashMap<>();
        for (var columnNodes : nodesByColumn) {
            for (int row = 0; row < columnNodes.size(); row++) {
                rowIndices.put(columnNodes.get(row), row);
            }
        }
        return rowIndices;
    }

    private record TierColumnLayout(List<List<TechNode>> nodesByColumn,
                                    List<TechTreeLayout.TierRegion> tierRegions) {}

    private static final Comparator<TechNode> nameOrder = Comparator.comparing(node -> node.name);
}
