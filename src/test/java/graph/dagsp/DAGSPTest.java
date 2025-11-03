package graph.dagsp;

import org.junit.jupiter.api.Test;
import util.SimpleMetrics;
import graph.scc.TarjanSCC;
import graph.scc.GraphCondensation;
import graph.scc.GraphCondensation.CondensationResult;
import graph.scc.GraphCondensation.CondEdge;
import graph.topo.KahnTopologicalSort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DAGSPTest {

    @Test
    public void shortestAndLongestSimple() {
        int n = 4;
        Map<Integer, List<Integer>> adj = new HashMap<>();
        Map<Integer, List<int[]>> adjW = new HashMap<>();
        for (int i = 0; i < n; i++) { adj.put(i, new ArrayList<>()); adjW.put(i, new ArrayList<>()); }
        addEdge(adj, adjW, 0, 1, 2);
        addEdge(adj, adjW, 1, 2, 2);
        addEdge(adj, adjW, 0, 2, 5);
        addEdge(adj, adjW, 2, 3, 1);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        List<List<Integer>> sccs = tarjan.run();

        CondensationResult cr = GraphCondensation.condense(adj, adjW, sccs);
        List<Integer> topo = KahnTopologicalSort.topoSort(cr.dag, metrics);

        int sourceComp = cr.compId[0];

        metrics = new SimpleMetrics();
        var shortest = DagPaths.shortestFromSource(cr, sourceComp, topo, metrics);
        var longest = DagPaths.longestFromSource(cr, sourceComp, topo, metrics);

        int compOf3 = cr.compId[3];
        int INF = Integer.MAX_VALUE / 4;
        int d = shortest.dist.getOrDefault(compOf3, INF);
        assertEquals(5, d, "Expected shortest distance 5");

        int longestVal = longest.dist.getOrDefault(compOf3, Integer.MIN_VALUE);
        assertEquals(6, longestVal, "Expected longest distance 6 (0->2->3)");
    }

    @Test
    public void unreachableNode() {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        Map<Integer, List<int[]>> adjW = new HashMap<>();
        for (int i = 0; i < 3; i++) { adj.put(i, new ArrayList<>()); adjW.put(i, new ArrayList<>()); }
        addEdge(adj, adjW, 0, 1, 1);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        List<List<Integer>> sccs = tarjan.run();
        CondensationResult cr = GraphCondensation.condense(adj, adjW, sccs);
        List<Integer> topo = KahnTopologicalSort.topoSort(cr.dag, metrics);
        int sourceComp = cr.compId[0];

        metrics = new SimpleMetrics();
        var shortest = DagPaths.shortestFromSource(cr, sourceComp, topo, metrics);

        int comp2 = cr.compId[2];
        int INF = Integer.MAX_VALUE / 4;
        assertEquals(INF, shortest.dist.getOrDefault(comp2, INF));
    }
    private static void addEdge(Map<Integer, List<Integer>> adj, Map<Integer, List<int[]>> adjW, int u, int v, int w) {
        adj.get(u).add(v);
        adjW.get(u).add(new int[]{v, w});
    }
}
