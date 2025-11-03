package graph.topo;

import org.junit.jupiter.api.Test;
import util.SimpleMetrics;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TopoTest {

    @Test
    public void simpleDAG() {
        Map<Integer, Set<Integer>> dag = new HashMap<>();
        dag.put(0, new HashSet<>(Set.of(1)));
        dag.put(1, new HashSet<>(Set.of(2)));
        dag.put(2, new HashSet<>(Set.of(3)));
        dag.put(3, new HashSet<>());

        SimpleMetrics metrics = new SimpleMetrics();
        List<Integer> order = KahnTopologicalSort.topoSort(dag, metrics);

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    public void disconnectedNodes() {
        Map<Integer, Set<Integer>> dag = new HashMap<>();
        for (int i = 0; i < 5; i++) dag.put(i, new HashSet<>());
        dag.get(0).add(1);
        dag.get(2).add(3);

        SimpleMetrics metrics = new SimpleMetrics();
        List<Integer> order = KahnTopologicalSort.topoSort(dag, metrics);

        assertEquals(5, order.size(), "Topological order must include all nodes");
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    public void cycleThrowsOrIncomplete() {
        Map<Integer, Set<Integer>> dag = new HashMap<>();
        dag.put(0, new HashSet<>(Set.of(1)));
        dag.put(1, new HashSet<>(Set.of(2)));
        dag.put(2, new HashSet<>(Set.of(0)));
        SimpleMetrics metrics = new SimpleMetrics();
        try {
            List<Integer> order = KahnTopologicalSort.topoSort(dag, metrics);
            assertTrue(order.size() < 3, "Cyclic graph -> incomplete topo order expected");
        } catch (IllegalStateException ex) {
            assertNotNull(ex.getMessage());
        }
    }
}
