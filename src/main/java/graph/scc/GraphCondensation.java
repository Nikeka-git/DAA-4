package graph.scc;

import java.util.*;

public class GraphCondensation {

    public static class CondEdge {
        public final int to;
        public final int minWeight;
        public final int maxWeight;
        public final int count;

        public CondEdge(int to, int minWeight, int maxWeight, int count) {
            this.to = to;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            this.count = count;
        }

        @Override
        public String toString() {
            return String.format("->%d (min=%d, max=%d, cnt=%d)", to, minWeight, maxWeight, count);
        }
    }

    public static class CondensationResult {
        public final Map<Integer, List<Integer>> compToNodes;
        public final int[] compId;
        public final Map<Integer, Set<Integer>> dag;
        public final Map<Integer, List<CondEdge>> weightedAdj;
        public final int componentsCount;

        public CondensationResult(Map<Integer, List<Integer>> compToNodes,
                                  int[] compId,
                                  Map<Integer, Set<Integer>> dag,
                                  Map<Integer, List<CondEdge>> weightedAdj) {
            this.compToNodes = compToNodes;
            this.compId = compId;
            this.dag = dag;
            this.weightedAdj = weightedAdj;
            this.componentsCount = compToNodes.size();
        }
    }


    public static CondensationResult condense(Map<Integer, List<Integer>> adj, Map<Integer, List<int[]>> adjW, List<List<Integer>> sccs) {
        int n = adj.size();
        int comps = sccs.size();
        int[] compId = new int[n];
        Map<Integer, List<Integer>> compToNodes = new HashMap<>();
        for (int i = 0; i < comps; i++) {
            compToNodes.put(i, new ArrayList<>(sccs.get(i)));
            for (Integer v : sccs.get(i)) {
                compId[v] = i;
            }
        }

        Map<Integer, Set<Integer>> dag = new HashMap<>();
        for (int i = 0; i < comps; i++) dag.put(i, new HashSet<>());

        Map<Long, Integer> minMap = new HashMap<>();
        Map<Long, Integer> maxMap = new HashMap<>();
        Map<Long, Integer> cntMap = new HashMap<>();

        if (adjW != null) {
            for (Map.Entry<Integer, List<int[]>> e : adjW.entrySet()) {
                int u = e.getKey();
                for (int[] vw : e.getValue()) {
                    int v = vw[0];
                    int w = vw[1];
                    int cu = compId[u];
                    int cv = compId[v];
                    if (cu == cv) continue;
                    long key = keyOf(cu, cv);
                    dag.get(cu).add(cv);

                    minMap.put(key, Math.min(minMap.getOrDefault(key, Integer.MAX_VALUE), w));
                    maxMap.put(key, Math.max(maxMap.getOrDefault(key, Integer.MIN_VALUE), w));
                    cntMap.put(key, cntMap.getOrDefault(key, 0) + 1);
                }
            }
        } else {
            for (Map.Entry<Integer, List<Integer>> e : adj.entrySet()) {
                int u = e.getKey();
                for (Integer v : e.getValue()) {
                    int cu = compId[u];
                    int cv = compId[v];
                    if (cu == cv) continue;
                    dag.get(cu).add(cv);
                    long key = keyOf(cu, cv);
                    if (!minMap.containsKey(key)) {
                        minMap.put(key, 1);
                        maxMap.put(key, 1);
                        cntMap.put(key, 1);
                    } else {
                        cntMap.put(key, cntMap.get(key) + 1);
                    }
                }
            }
        }

        // 4) build weightedAdj from maps
        Map<Integer, List<CondEdge>> weightedAdj = new HashMap<>();
        for (int i = 0; i < comps; i++) weightedAdj.put(i, new ArrayList<>());

        for (Map.Entry<Long, Integer> me : minMap.entrySet()) {
            long key = me.getKey();
            int cu = (int) (key >>> 32);
            int cv = (int) (key & 0xffffffffL);
            int minW = me.getValue();
            int maxW = maxMap.getOrDefault(key, minW);
            int cnt = cntMap.getOrDefault(key, 1);
            weightedAdj.get(cu).add(new CondEdge(cv, minW, maxW, cnt));
        }

        return new CondensationResult(compToNodes, compId, dag, weightedAdj);
    }

    private static long keyOf(int a, int b) {
        return (((long) a) << 32) | ((long) b & 0xffffffffL);
    }
}
