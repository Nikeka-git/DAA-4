package graph.scc;

import org.junit.jupiter.api.Test;
import util.SimpleMetrics;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SCTest {

    @Test
    public void testMultipleSCCs() {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 0; i <= 8; i++) graph.put(i, new ArrayList<>());

        graph.get(0).add(1);
        graph.get(1).add(2);
        graph.get(2).add(0);

        graph.get(3).add(4);
        graph.get(4).add(3);

        graph.get(5).add(6);
        graph.get(6).add(7);
        graph.get(7).add(5);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.run();

        assertEquals(4, sccs.size(), "Waited 4 CSSs");

        assertTrue(sccs.stream().anyMatch(c -> c.containsAll(List.of(0, 1, 2))));
        assertTrue(sccs.stream().anyMatch(c -> c.containsAll(List.of(3, 4))));
        assertTrue(sccs.stream().anyMatch(c -> c.containsAll(List.of(5, 6, 7))));
        assertTrue(sccs.stream().anyMatch(c -> c.size() == 1 && c.contains(8)));
    }

    @Test
    public void testSingleNodeNoEdges() {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        graph.put(0, new ArrayList<>());

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.run();

        assertEquals(1, sccs.size(), "Only one SCC");
        assertEquals(List.of(0), sccs.get(0));
    }

    @Test
    public void testLinearGraphNoCycles() {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 0; i <= 3; i++) graph.put(i, new ArrayList<>());
        graph.get(0).add(1);
        graph.get(1).add(2);
        graph.get(2).add(3);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.run();

        assertEquals(4, sccs.size());
        for (List<Integer> comp : sccs) {
            assertEquals(1, comp.size());
        }
    }

    @Test
    public void testDeepRecursionDoesNotCrash() {
        int n = 1000;
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 0; i < n; i++) graph.put(i, new ArrayList<>());
        for (int i = 0; i < n - 1; i++) graph.get(i).add(i + 1);

        SimpleMetrics metrics = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        List<List<Integer>> sccs = tarjan.run();

        assertEquals(n, sccs.size(), "Every vertex - SCC");
    }
}
