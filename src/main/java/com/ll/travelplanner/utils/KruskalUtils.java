package com.ll.travelplanner.utils;

import com.ll.travelplanner.models.Edge;
import com.ll.travelplanner.models.Node;
import lombok.NonNull;

import java.util.*;

public final class KruskalUtils {
    private KruskalUtils() { }

    public static Set<Edge> getOptimalRoutes(
            @NonNull final Queue<Edge> edgesSortedByDistance,
            @NonNull final Map<Node, List<Edge>> graph) {
        final Set<Edge> result = new HashSet<>();
        final Map<Node, Node> parents = makeParents(graph);

        while (!edgesSortedByDistance.isEmpty()) {
            final Edge edge = edgesSortedByDistance.poll();
            if (!UnionFindUtils.isCycle(parents, edge.getOrigin(), edge.getDestination())) {
                result.add(edge);
            }
        }

        return result;
    }

    private static Map<Node, Node> makeParents(@NonNull final Map<Node, List<Edge>> graph) {
        final Map<Node, Node> parents = new HashMap<>();
        for (Map.Entry<Node, List<Edge>> entry : graph.entrySet()) {
            parents.put(entry.getKey(), entry.getKey());
        }

        return parents;
    }
}
