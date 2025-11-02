import io.TaskGraphIO;
import graph.scc.*;
import util.SimpleMetrics;
import util.Metrics;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length>0 ? args[0] : "data/tasks_example.json";
        Metrics metrics = new SimpleMetrics();

        TaskGraphIO.GraphData gd = TaskGraphIO.read(path);
        Map<Integer, List<Integer>> adj = TaskGraphIO.toAdjList(gd);
        Map<Integer, List<int[]>> adjW = TaskGraphIO.toAdjListWithWeights(gd);

        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        List<List<Integer>> sccs = tarjan.run();

        GraphCondensation.CondensationResult cr = GraphCondensation.condense(adj, adjW, sccs);

        System.out.println("Components: " + cr.componentsCount);
        for (int i = 0; i < cr.componentsCount; i++) {
            System.out.println("Comp " + i + " nodes=" + cr.compToNodes.get(i));
            System.out.println("  edges: " + cr.weightedAdj.get(i));
        }
    }
}
