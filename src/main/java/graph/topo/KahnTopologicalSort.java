package graph.topo;

import util.Metrics;

import java.util.*;

public class KahnTopologicalSort {

    public static List<Integer> topoSort(Map<Integer, Set<Integer>> dag, Metrics metrics) {
        Map<Integer, Integer> indeg = new HashMap<>();
        for (Integer u : dag.keySet()) indeg.putIfAbsent(u, 0);
        for (Map.Entry<Integer, Set<Integer>> e : dag.entrySet()) {
            for (Integer v : e.getValue()) indeg.put(v, indeg.getOrDefault(v, 0) + 1);
        }

        Deque<Integer> q = new ArrayDeque<>();
        for (Map.Entry<Integer, Integer> e : indeg.entrySet()) {
            if (e.getValue() == 0) {
                q.addLast(e.getKey());
                if (metrics != null) metrics.inc("kahn_push");
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            Integer u = q.removeFirst();
            if (metrics != null) metrics.inc("kahn_pop");
            order.add(u);
            for (Integer v : dag.getOrDefault(u, Collections.emptySet())) {
                indeg.put(v, indeg.get(v) - 1);
                if (indeg.get(v) == 0) {
                    q.addLast(v);
                    if (metrics != null) metrics.inc("kahn_push");
                }
            }
        }

        if (order.size() < indeg.size()) {
            throw new IllegalStateException("Topological sort failed: graph has a cycle (order size < nodes).");
        }

        return order;
    }
}
