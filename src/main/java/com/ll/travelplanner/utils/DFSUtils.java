package com.ll.travelplanner.utils;

import com.ll.travelplanner.models.Edge;
import com.ll.travelplanner.models.Node;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author llu
 */

public class DFSUtils {
//    public List<Edge> bfs(@NonNull final Node origin, @NonNull final Map<Node, List<Edge>> graph) {
//        final Queue<Node> frontier = new LinkedList<>();
//        final Set<Node> visitedNodes = new HashSet<>();
//        final Set<Edge> visitedEdges = new HashSet<>();
////        final List<List<Edge>> paths = new ArrayList<>();
//        List<Edge> path = new ArrayList<>();
//
//        frontier.add(origin);
//        while(!frontier.isEmpty()) {
//            final Node cur = frontier.poll();
//            visitedNodes.add(cur);
//            for(final Edge edge : graph.get(cur)) {
//                frontier.add(edge.getDestination());
//                visitedEdges.add(edge);
//            }
//        }
//    }

    public static void dfs(@NonNull final Node origin,
                           @NonNull final Map<Node, Set<Edge>> graph,
                           @NonNull final Set<Node> visitedNodes,
                           @NonNull final Set<Edge> visitedEdges,
                           @NonNull final List<Edge> path,
                           @NonNull final List<List<Edge>> paths) {
        visitedNodes.add(origin);
        if (visitedNodes.size() == graph.size()) {
            paths.add(new ArrayList<>(path));
            visitedNodes.remove(origin);
            return;
        }

        final Set<Edge> neighbors = graph.getOrDefault(origin, new HashSet<>());
        final Set<Node> allReachableDestinations = neighbors.stream()
                .map(Edge::getDestination)
                .collect(Collectors.toSet());
        for (final Edge edge : neighbors) {
            final Node destination = edge.getDestination();
            final boolean hasVisitedAllNeighbors = visitedNodes.containsAll(allReachableDestinations);
            if ((hasVisitedAllNeighbors && visitedEdges.contains(edge))
                    || (!hasVisitedAllNeighbors && visitedNodes.contains(destination))) {
                continue;
            }
            final Node neighbor = edge.getDestination();
            visitedEdges.add(edge);
            path.add(edge);
            dfs(neighbor, graph, visitedNodes, visitedEdges, path, paths);
            path.remove(path.size() - 1);
            visitedEdges.remove(edge);
        }
        visitedNodes.remove(origin);
    }
}
