import io.TaskGraphIO;
import graph.scc.TarjanSCC;
import graph.scc.GraphCondensation;
import graph.scc.GraphCondensation.CondensationResult;
import graph.scc.GraphCondensation.CondEdge;
import graph.topo.KahnTopologicalSort;
import graph.dagsp.DagPaths;
import graph.dagsp.DagPaths.PathResultComp;
import util.Metrics;
import util.SimpleMetrics;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "data/tasks_example.json";
        System.out.println("Input file: " + path);
        try {
            TaskGraphIO.GraphData gd = TaskGraphIO.read(path);
            Map<Integer, List<Integer>> adj = TaskGraphIO.toAdjList(gd);
            Map<Integer, List<int[]>> adjW = TaskGraphIO.toAdjListWithWeights(gd);

            Metrics metrics = new SimpleMetrics();

            metrics.startTimer("tarjan");
            TarjanSCC tarjan = new TarjanSCC(adj, metrics);
            List<List<Integer>> sccs = tarjan.run();
            metrics.stopTimer("tarjan");

            System.out.println("\n--- SCCs ---");
            for (int i = 0; i < sccs.size(); i++) {
                List<Integer> comp = new ArrayList<>(sccs.get(i));
                Collections.sort(comp);
                System.out.printf("Comp %d: nodes=%s size=%d%n", i, comp, comp.size());
            }
            System.out.printf("Total components: %d%n", sccs.size());
            System.out.printf("Tarjan metrics: dfs_visits=%d dfs_edges=%d scc_count=%d time(ns)=%d%n",
                    metrics.getCount("dfs_visits"),
                    metrics.getCount("dfs_edges"),
                    metrics.getCount("scc_count"),
                    metrics.getTime("tarjan"));

            long t0 = System.nanoTime();
            CondensationResult cr = GraphCondensation.condense(adj, adjW, sccs);
            long tCond = System.nanoTime() - t0;

            System.out.println("\n--- Condensation (component DAG + weighted edges) ---");
            for (int i = 0; i < cr.componentsCount; i++) {
                List<Integer> nodes = new ArrayList<>(cr.compToNodes.get(i));
                Collections.sort(nodes);
                System.out.printf("Comp %d nodes=%s%n", i, nodes);
                List<CondEdge> edges = cr.weightedAdj.getOrDefault(i, Collections.emptyList());
                System.out.println("  edges: " + edges);
            }
            System.out.printf("Condensation time (ns): %d%n", tCond);

            metrics.startTimer("kahn");
            List<Integer> topo = KahnTopologicalSort.topoSort(cr.dag, metrics);
            metrics.stopTimer("kahn");

            System.out.println("\n--- Topological order of components ---");
            System.out.println(topo);
            System.out.printf("Kahn metrics: kahn_push=%d kahn_pop=%d time(ns)=%d%n",
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

            int sourceNode = gd.source != null ? gd.source : 0;
            if (sourceNode < 0 || sourceNode >= gd.n) sourceNode = 0;
            int sourceComp = cr.compId[sourceNode];
            System.out.printf("%nSource node = %d, source component = %d%n", sourceNode, sourceComp);

            metrics.startTimer("dag_shortest");
            PathResultComp shortest = DagPaths.shortestFromSource(cr, sourceComp, topo, metrics);
            metrics.stopTimer("dag_shortest");

            metrics.startTimer("dag_longest");
            PathResultComp longest = DagPaths.longestFromSource(cr, sourceComp, topo, metrics);
            metrics.stopTimer("dag_longest");

            System.out.println("\n--- Shortest distances (component-level, INF = unreachable) ---");
            final int INF = Integer.MAX_VALUE / 4;
            for (int i = 0; i < cr.componentsCount; i++) {
                int d = shortest.dist.getOrDefault(i, INF);
                System.out.printf("comp %d -> %s%n", i, d == INF ? "INF" : Integer.toString(d));
            }
            System.out.printf("dag_shortest time(ns)=%d relaxations=%d%n", metrics.getTime("dag_shortest"), metrics.getCount("dag_relaxations"));

            int targetCompShort = -1;
            int bestShort = INF;
            for (int i = 0; i < cr.componentsCount; i++) {
                int d = shortest.dist.getOrDefault(i, INF);
                if (i == sourceComp) continue;
                if (d < bestShort) {
                    bestShort = d;
                    targetCompShort = i;
                }
            }
            if (targetCompShort != -1 && bestShort < INF) {
                List<Integer> compPathShort = DagPaths.reconstructComponentPath(shortest.parent, sourceComp, targetCompShort);
                List<Integer> nodePathShort = DagPaths.reconstructNodePathFromComponentPath(compPathShort, cr, adjW, cr.compId, true);
                System.out.printf("%nShortest example: targetComp=%d dist=%d%n", targetCompShort, bestShort);
                System.out.println("  component path: " + compPathShort);
                System.out.println("  representative node-level path: " + nodePathShort);
            } else {
                System.out.println("\nNo reachable target for shortest-path example (only source reachable).");
            }

            int bestComp = -1;
            int bestVal = Integer.MIN_VALUE;
            for (Map.Entry<Integer, Integer> e : longest.dist.entrySet()) {
                if (e.getValue() > bestVal) {
                    bestVal = e.getValue();
                    bestComp = e.getKey();
                }
            }
            if (bestComp != -1 && bestVal > Integer.MIN_VALUE / 8) {
                List<Integer> compPathLong = DagPaths.reconstructComponentPath(longest.parent, sourceComp, bestComp);
                List<Integer> nodePathLong = DagPaths.reconstructNodePathFromComponentPath(compPathLong, cr, adjW, cr.compId, false);
                System.out.printf("%nCritical (longest) path: targetComp=%d length=%d%n", bestComp, bestVal);
                System.out.println("  component path: " + compPathLong);
                System.out.println("  representative node-level path: " + nodePathLong);
            } else {
                System.out.println("\nNo reachable target for longest-path (all unreachable).");
            }

            System.out.printf("%nDag metrics: dag_relaxations=%d%n", metrics.getCount("dag_relaxations"));
            System.out.printf("dag_shortest time(ns)=%d, dag_longest time(ns)=%d%n",
                    metrics.getTime("dag_shortest"), metrics.getTime("dag_longest"));

            System.out.println("\n--- Summary ---");
            System.out.printf("n=%d m=%d directed=%b source=%s weight_model=%s%n",
                    gd.n, (gd.edges == null ? 0 : gd.edges.size()), gd.directed, gd.source, gd.weight_model);

        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(2);
        }
    }
}
