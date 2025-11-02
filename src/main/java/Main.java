import io.TaskGraphIO;
import graph.scc.*;
import graph.topo.KahnTopologicalSort;
import util.Metrics;
import util.SimpleMetrics;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/tasks_example.json";
        System.out.println("Reading graph from: " + path);

        TaskGraphIO.GraphData gd = TaskGraphIO.read(path);
        Map<Integer, List<Integer>> adj = TaskGraphIO.toAdjList(gd);
        Map<Integer, List<int[]>> adjW = TaskGraphIO.toAdjListWithWeights(gd);

        Metrics metrics = new SimpleMetrics();

        metrics.startTimer("tarjan");
        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        List<List<Integer>> sccs = tarjan.run();
        metrics.stopTimer("tarjan");

        System.out.println("\n--- Strongly Connected Components (SCC) ---");
        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> comp = new ArrayList<>(sccs.get(i));
            Collections.sort(comp);
            System.out.printf("Comp %d: nodes=%s size=%d%n", i, comp, comp.size());
        }
        System.out.printf("Total components: %d%n", sccs.size());
        System.out.printf("Tarjan: dfs_visits=%d dfs_edges=%d scc_count=%d time(ns)=%d%n",
                metrics.getCount("dfs_visits"),
                metrics.getCount("dfs_edges"),
                metrics.getCount("scc_count"),
                metrics.getTime("tarjan"));

        long t0 = System.nanoTime();
        GraphCondensation.CondensationResult cr = GraphCondensation.condense(adj, adjW, sccs);
        long tCond = System.nanoTime() - t0;
        System.out.println("\n--- Condensation (component DAG) ---");
        for (int i = 0; i < cr.componentsCount; i++) {
            List<Integer> nodes = new ArrayList<>(cr.compToNodes.get(i));
            Collections.sort(nodes);
            System.out.printf("Comp %d nodes=%s%n", i, nodes);
            System.out.println("  edges: " + cr.weightedAdj.getOrDefault(i, Collections.emptyList()));
        }
        System.out.printf("Condensation build time (ns): %d%n", tCond);

        metrics.startTimer("kahn");
        List<Integer> topo = KahnTopologicalSort.topoSort(cr.dag, metrics);
        metrics.stopTimer("kahn");

        System.out.println("\n--- Topological order of components ---");
        System.out.println(topo);
        System.out.printf("Kahn: kahn_push=%d kahn_pop=%d time(ns)=%d%n",
                metrics.getCount("kahn_push"),
                metrics.getCount("kahn_pop"),
                metrics.getTime("kahn"));

        List<Integer> derivedOrder = new ArrayList<>();
        for (Integer comp : topo) {
            List<Integer> nodes = new ArrayList<>(cr.compToNodes.get(comp));
            Collections.sort(nodes);
            derivedOrder.addAll(nodes);
        }
        System.out.println("\n--- Derived order of original tasks (by components) ---");
        System.out.println(derivedOrder);
        
        System.out.println("\n--- Summary metrics ---");
        System.out.printf("n=%d m=%d directed=%b source=%s weight_model=%s%n",
                gd.n, (gd.edges==null?0:gd.edges.size()), gd.directed, gd.source, gd.weight_model);
    }
}
