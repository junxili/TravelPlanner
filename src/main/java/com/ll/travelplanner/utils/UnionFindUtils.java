package com.ll.travelplanner.utils;

import com.ll.travelplanner.models.Node;
import lombok.NonNull;

import java.util.Map;

public final class UnionFindUtils {
    private UnionFindUtils() {}

    public static Node find(@NonNull final Map<Node, Node> parents, @NonNull final Node node) {
        if (!parents.get(node).equals(node)) {
            parents.put(node, find(parents, parents.get(node)));
        }

        return parents.get(node);
    }

    public static void unite(@NonNull final Map<Node, Node> parents, @NonNull final Node p, @NonNull final Node q) {
        final Node pRoot = find(parents, p);
        final Node qRoot = find(parents, q);
        parents.put(pRoot, qRoot);
    }

    public static boolean isCycle(@NonNull final Map<Node, Node> parents,
                                  @NonNull final Node p,
                                  @NonNull final Node q) {
        final boolean hasCycle = find(parents, p).equals(find(parents, q));
        if (!hasCycle) {
            unite(parents, p, q);
        }
        return hasCycle;
    }
}