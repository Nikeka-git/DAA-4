package io;

import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

public class TaskGraphIO {
    public static class Edge { public int u, v, w; }
    public static class GraphData {
        public boolean directed;
        public int n;
        public List<Edge> edges;
        public Integer source;
        public String weight_model;
    }

    public static GraphData read(String path) throws Exception {
        Path p = Path.of(path);
        if (!Files.exists(p)) {
            throw new IOException("File not found: " + p.toAbsolutePath());
        }
        if (!Files.isReadable(p)) {
            throw new IOException("File exists but not readable: " + p.toAbsolutePath());
        }

        String json;
        try {
            json = Files.readString(p, StandardCharsets.UTF_8);
        } catch (UnsupportedOperationException uex) {
            byte[] bytes = Files.readAllBytes(p);
            json = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ioex) {
            throw new IOException("Failed to read file: " + p.toAbsolutePath() + " -> " + ioex.getMessage(), ioex);
        }

        if (json.length() > 0 && json.charAt(0) == '\uFEFF') {
            json = json.substring(1);
        }

        Gson gson = new GsonBuilder().setLenient().create();
        try {
            GraphData gd = gson.fromJson(json, GraphData.class);
            if (gd == null) throw new JsonParseException("Parsed JSON is null (empty file?)");
            if (gd.n <= 0) throw new IllegalArgumentException("Field 'n' must be > 0, got: " + gd.n);
            if (gd.edges == null) gd.edges = new ArrayList<>();
            return gd;
        } catch (JsonParseException ex) {
            String snippet = json.length() <= 400 ? json : (json.substring(0, 200) + "\n...SNIP...\n" + json.substring(json.length()-200));
            String msg = "Failed to parse JSON from " + p.toAbsolutePath() + ": " + ex.getMessage()
                    + "\n--- file snippet ---\n" + snippet;
            throw new JsonParseException(msg, ex);
        } catch (Exception ex) {
            throw new Exception("Unexpected error parsing JSON from " + p.toAbsolutePath() + ": " + ex.getMessage(), ex);
        }
    }

    public static Map<Integer, List<Integer>> toAdjList(GraphData g) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int i = 0; i < g.n; i++) adj.put(i, new ArrayList<>());
        if (g.edges == null) return adj;
        for (Edge e : g.edges) {
            if (e.u < 0 || e.u >= g.n || e.v < 0 || e.v >= g.n) {
                throw new IllegalArgumentException("Edge references invalid vertex: u=" + e.u + " v=" + e.v + " n=" + g.n);
            }
            adj.get(e.u).add(e.v);
            if (!g.directed) adj.get(e.v).add(e.u);
        }
        return adj;
    }

    public static Map<Integer, List<int[]>> toAdjListWithWeights(GraphData g) {
        Map<Integer, List<int[]>> adj = new HashMap<>();
        for (int i = 0; i < g.n; i++) adj.put(i, new ArrayList<>());
        if (g.edges == null) return adj;
        for (Edge e : g.edges) {
            if (e.u < 0 || e.u >= g.n || e.v < 0 || e.v >= g.n) {
                throw new IllegalArgumentException("Edge references invalid vertex: u=" + e.u + " v=" + e.v + " n=" + g.n);
            }
            adj.get(e.u).add(new int[]{e.v, e.w});
            if (!g.directed) adj.get(e.v).add(new int[]{e.u, e.w});
        }
        return adj;
    }
}
