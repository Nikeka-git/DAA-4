package io;

import com.google.gson.*;
import java.nio.file.*;
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
        String json = Files.readString(Path.of(path));
        Gson gson = new Gson();
        return gson.fromJson(json, GraphData.class);
    }

    public static Map<Integer, List<Integer>> toAdjList(GraphData g) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int i = 0; i < g.n; i++) adj.put(i, new ArrayList<>());
        if (g.edges == null) return adj;
        for (Edge e : g.edges) {
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
            adj.get(e.u).add(new int[]{e.v, e.w});
            if (!g.directed) adj.get(e.v).add(new int[]{e.u, e.w});
        }
        return adj;
    }
}
