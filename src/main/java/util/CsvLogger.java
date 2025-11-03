package util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.StringJoiner;

public class CsvLogger {

    private static final Path OUT_DIR = Paths.get("results");
    private static final Path OUT_FILE = OUT_DIR.resolve("raw_results.csv");
    private static final String HEADER = "dataset;n;m;directed;weight_model;source;"
            + "scc_count;largest_scc_size;dfs_visits;dfs_edges;tarjan_time_ms;condensation_time_ms;"
            + "topo_order_len;kahn_push;kahn_pop;kahn_time_ms;"
            + "dag_relaxations;dag_shortest_time_ms;dag_longest_time_ms;"
            + "shortest_target_comp;shortest_target_dist;shortest_node_path_len;"
            + "longest_target_comp;longest_target_dist;longest_node_path_len\n";

        public static synchronized void appendRun(
            String datasetName, String n, String m, String directed, String weightModel, String source,
            String sccCount, String largestScc, String dfsVisits, String dfsEdges, String tarjanTimeNs, String condensationTimeNs,
            String topoLen, String kahnPush, String kahnPop, String kahnTimeNs,
            String dagRelax, String dagShortestTimeNs, String dagLongestTimeNs,
            String shortestTargetComp, String shortestTargetDist, String shortestNodePathLen,
            String longestTargetComp, String longestTargetDist, String longestNodePathLen
    ) throws IOException {
        if (!Files.exists(OUT_DIR)) Files.createDirectories(OUT_DIR);
        if (!Files.exists(OUT_FILE)) {
            Files.writeString(OUT_FILE, HEADER, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        StringJoiner sj = new StringJoiner(";");
        sj.add(escape(datasetName)).add(n).add(m).add(directed).add(escape(weightModel)).add(source)
                .add(sccCount).add(largestScc).add(dfsVisits).add(dfsEdges).add(tarjanTimeNs).add(condensationTimeNs)
                .add(topoLen).add(kahnPush).add(kahnPop).add(kahnTimeNs)
                .add(dagRelax).add(dagShortestTimeNs).add(dagLongestTimeNs)
                .add(shortestTargetComp).add(shortestTargetDist).add(shortestNodePathLen)
                .add(longestTargetComp).add(longestTargetDist).add(longestNodePathLen);

        String line = sj.toString() + "\n";
        Files.writeString(OUT_FILE, line, StandardOpenOption.APPEND);
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    public static String nowIso() {
        return Instant.now().toString();
    }
}
