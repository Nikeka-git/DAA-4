package io;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public class TaskGraphIO {

    public static class Edge {
        public int u, v, w;
    }

    public static class GraphData {
        public boolean directed;
        public int n;
        public List<Edge> edges;
        public int source;
        public String weight_model;
    }

    public static GraphData read(String path) throws Exception {
        String json = Files.readString(Path.of(path));
        Gson gson = new Gson();
        return gson.fromJson(json, GraphData.class);
    }

    public static Map<Integer, List<int[]>> toAdjList(GraphData data) {
        Map<Integer, List<int[]>> g = new HashMap<>();
        for (int i = 0; i < data.n; i++) g.put(i, new ArrayList<>());
        for (Edge e : data.edges) g.get(e.u).add(new int[]{e.v, e.w});
        return g;
    }
}
