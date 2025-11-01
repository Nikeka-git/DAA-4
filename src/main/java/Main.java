import io.TaskGraphIO;
import graph.scc.TarjanSCC;
import util.SimpleMetrics;
import util.Metrics;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length>0 ? args[0] : "data/tasks_example.json";
        TaskGraphIO.GraphData gd = TaskGraphIO.read(path);

        Map<Integer, List<Integer>> adj = TaskGraphIO.toAdjList(gd);

        Metrics metrics = new SimpleMetrics();

        TarjanSCC tarjan = new TarjanSCC(adj, metrics);
        metrics.startTimer("tarjan");
        List<List<Integer>> sccs = tarjan.run();
        metrics.stopTimer("tarjan");

        System.out.println("SCCs: " + sccs);
        System.out.println("scc_count = " + metrics.getCount("scc_count"));
        System.out.println("dfs_edges = " + metrics.getCount("dfs_edges"));
        System.out.println("dfs_visits = " + metrics.getCount("dfs_visits"));
        System.out.println("tarjan time (ms) = " + metrics.getTime("tarjan") / 1000000.0);

        Map<Integer, List<int[]>> adjW = TaskGraphIO.toAdjListWithWeights(gd);
    }
}
