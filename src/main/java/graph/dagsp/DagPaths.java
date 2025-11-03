package graph.dagsp;

import graph.scc.GraphCondensation.CondensationResult;
import graph.scc.GraphCondensation.CondEdge;
import graph.scc.GraphCondensation.*;
import util.Metrics;

import java.util.*;

public class DagPaths {

    public static class PathResultComp {
        public final Map<Integer, Integer> dist;
        public final Map<Integer, Integer> parent;
        public PathResultComp(Map<Integer, Integer> dist, Map<Integer, Integer> parent) {
            this.dist = dist;
            this.parent = parent;
        }
    }

    public static PathResultComp shortestFromSource(CondensationResult cr, int sourceComp, List<Integer> topoOrder, Metrics metrics) {
        final int INF = Integer.MAX_VALUE / 4;
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        for (int i = 0; i < cr.componentsCount; i++) dist.put(i, INF);
        dist.put(sourceComp, 0);

        for (Integer u : topoOrder) {
            int du = dist.getOrDefault(u, INF);
            if (du == INF) continue;
            for (CondEdge e : cr.weightedAdj.getOrDefault(u, Collections.emptyList())) {
                if (metrics != null) metrics.inc("dag_relaxations");
                int v = e.to;
                int w = e.minWeight;
                if (du + w < dist.get(v)) {
                    dist.put(v, du + w);
                    parent.put(v, u);
                }
            }
        }
        return new PathResultComp(dist, parent);
    }

    public static PathResultComp longestFromSource(CondensationResult cr, int sourceComp, List<Integer> topoOrder, Metrics metrics) {
        final int NEG = Integer.MIN_VALUE / 4;
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();

        for (int i = 0; i < cr.componentsCount; i++) dist.put(i, NEG);
        dist.put(sourceComp, 0);

        for (Integer u : topoOrder) {
            int du = dist.getOrDefault(u, NEG);
            if (du == NEG) continue;
            for (CondEdge e : cr.weightedAdj.getOrDefault(u, Collections.emptyList())) {
                if (metrics != null) metrics.inc("dag_relaxations");
                int v = e.to;
                int w = e.maxWeight;
                if (du + w > dist.get(v)) {
                    dist.put(v, du + w);
                    parent.put(v, u);
                }
            }
        }
        return new PathResultComp(dist, parent);
    }

    public static List<Integer> reconstructComponentPath(Map<Integer,Integer> parent, int sourceComp, int targetComp) {
        List<Integer> compPath = new ArrayList<>();
        Integer cur = targetComp;
        while (cur != null && cur != sourceComp) {
            compPath.add(cur);
            cur = parent.get(cur);
        }
        if (cur == null) return Collections.emptyList();
        compPath.add(sourceComp);
        Collections.reverse(compPath);
        return compPath;
    }

    public static List<Integer> reconstructNodePathFromComponentPath(List<Integer> compPath, CondensationResult cr, Map<Integer, List<int[]>> adjW, int[] compId, boolean useMinWeight) {
        if (compPath.isEmpty()) return Collections.emptyList();
        List<Integer> nodePath = new ArrayList<>();
        int startComp = compPath.get(0);
        List<Integer> startNodes = cr.compToNodes.get(startComp);
        int curNode = startNodes.stream().min(Integer::compareTo).orElse(startNodes.get(0));
        nodePath.add(curNode);

        for (int idx = 0; idx + 1 < compPath.size(); idx++) {
            int cu = compPath.get(idx);
            int cv = compPath.get(idx+1);
            int targetWeight = Integer.MIN_VALUE;
            if (useMinWeight) {
                int found = Integer.MAX_VALUE;
                for (CondEdge ce : cr.weightedAdj.getOrDefault(cu, Collections.emptyList())) {
                    if (ce.to == cv) { found = Math.min(found, ce.minWeight); }
                }
                if (found==Integer.MAX_VALUE) return Collections.emptyList();
                targetWeight = found;
            } else {
                int found = Integer.MIN_VALUE;
                for (CondEdge ce : cr.weightedAdj.getOrDefault(cu, Collections.emptyList())) {
                    if (ce.to == cv) { found = Math.max(found, ce.maxWeight); }
                }
                if (found==Integer.MIN_VALUE) return Collections.emptyList();
                targetWeight = found;
            }

             boolean edgeFound = false;
             for (Integer u : cr.compToNodes.get(cu)) {
                for (int[] vw : adjW.getOrDefault(u, Collections.emptyList())) {
                    int v = vw[0], w = vw[1];
                    if (compId[v]==cv && w == targetWeight) {
                        nodePath.add(v);
                        curNode = v;
                        edgeFound = true;
                        break;
                    }
                }
                if (edgeFound) break;
            }
            if (!edgeFound) {
                boolean anyFound = false;
                for (Integer u : cr.compToNodes.get(cu)) {
                    for (int[] vw : adjW.getOrDefault(u, Collections.emptyList())) {
                        int v = vw[0];
                        if (compId[v]==cv) {
                            nodePath.add(v);
                            curNode = v;
                            anyFound = true;
                            break;
                        }
                    }
                    if (anyFound) break;
                }
                if (!anyFound) return Collections.emptyList();
            }
        }
        return nodePath;
    }

}
