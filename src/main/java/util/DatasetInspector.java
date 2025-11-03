package util;

import io.TaskGraphIO;
import graph.scc.TarjanSCC;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public class DatasetInspector {
    public static void main(String[] args) throws Exception {
        Path data = Paths.get("data");
        if (!Files.exists(data) || !Files.isDirectory(data)) {
            System.err.println("No data/ directory found");
            return;
        }
        Path outDir = Paths.get("results");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        Path out = outDir.resolve("dataset_summary.csv");
        Files.writeString(out, "filename;n;m;scc_count;largest_scc_size;isDAG;density\n");

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(data, "*.json")) {
            for (Path p : ds) {
                try {
                    TaskGraphIO.GraphData gd = TaskGraphIO.read(p.toString());
                    Map<Integer, List<Integer>> adj = TaskGraphIO.toAdjList(gd);
                    int n = gd.n;
                    int m = gd.edges == null ? 0 : gd.edges.size();

                    TarjanSCC tarjan = new TarjanSCC(adj, new util.SimpleMetrics());
                    List<List<Integer>> sccs = tarjan.run();
                    int sccCount = sccs.size();
                    int largest = sccs.stream().mapToInt(List::size).max().orElse(0);
                    boolean isDAG = (largest == 1);
                    double density = m / (double)(n * (n - 1));

                    String line = String.format("%s;%d;%d;%d;%d;%b;%.6f\n",
                            p.getFileName().toString(), n, m, sccCount, largest, isDAG, density);
                    Files.writeString(out, line, StandardOpenOption.APPEND);
                } catch (Exception ex) {
                    String err = String.format("%s;ERROR;;;%s\n", p.getFileName().toString(), escapeForCsv(ex.getMessage()));
                    Files.writeString(out, err, StandardOpenOption.APPEND);
                }
            }
        }

        System.out.println("Wrote dataset summary to " + out.toAbsolutePath());
    }

    private static String escapeForCsv(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }
}
