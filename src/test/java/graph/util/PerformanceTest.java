package graph.util;

import graph.scc.TarjanSCC;
import org.junit.jupiter.api.Test;
import util.SimpleMetrics;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class PerformanceTest {

    @Test
    public void deepChainNoStackOverflow() {
        int n = 3000;
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int i = 0; i < n; i++) adj.put(i, new ArrayList<>());
        for (int i = 0; i < n - 1; i++) adj.get(i).add(i + 1);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        List<List<Integer>> comps = tarjan.run();
        assertEquals(n, comps.size());
    }

    @Test
    public void moderatePerformance() {
        int n = 1200;
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int i = 0; i < n; i++) adj.put(i, new ArrayList<>());
        for (int i = 0; i < n; i++) {
            for (int j = 1; j <= 3 && i + j < n; j++) adj.get(i).add(i + j);
        }
        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        long t0 = System.nanoTime();
        tarjan.run();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms < 3000, "Tarjan should finish under 3s for moderate graph");
    }
}
