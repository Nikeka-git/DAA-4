package graph.scc;

import util.Metrics;
import java.util.*;

public class TarjanSCC {
    private final Map<Integer, List<Integer>> graph;
    private final Metrics metrics;

    public TarjanSCC(Map<Integer, List<Integer>> graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public List<List<Integer>> run() {
        Map<Integer,Integer> index = new HashMap<>();
        Map<Integer,Integer> low = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> onStack = new HashSet<>();
        List<List<Integer>> sccs = new ArrayList<>();
        int[] idx = {0};

        for (Integer v : graph.keySet()) {
            if (!index.containsKey(v)) dfs(v, index, low, stack, onStack, sccs, idx);
        }
        return sccs;
    }

    private void dfs(Integer v, Map<Integer,Integer> index, Map<Integer,Integer> low,
                     Deque<Integer> stack, Set<Integer> onStack, List<List<Integer>> sccs, int[] idx) {
        index.put(v, idx[0]);
        low.put(v, idx[0]);
        idx[0]++;
        stack.push(v);
        onStack.add(v);
        metrics.inc("dfs_visits");

        for (Integer to : graph.getOrDefault(v, Collections.emptyList())) {
            metrics.inc("dfs_edges");
            if (!index.containsKey(to)) {
                dfs(to, index, low, stack, onStack, sccs, idx);
                low.put(v, Math.min(low.get(v), low.get(to)));
            } else if (onStack.contains(to)) {
                low.put(v, Math.min(low.get(v), index.get(to)));
            }
        }

        if (low.get(v).equals(index.get(v))) {
            List<Integer> comp = new ArrayList<>();
            Integer w;
            do {
                w = stack.pop();
                onStack.remove(w);
                comp.add(w);
            } while (!w.equals(v));
            sccs.add(comp);
            metrics.inc("scc_count");
        }
    }
}
