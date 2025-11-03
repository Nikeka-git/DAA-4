Assignment 4 Report — Smart City / Smart Campus Scheduling
1. Dataset Summary

Each dataset represents a directed dependency graph describing smart-city or campus scheduling tasks — such as maintenance, cleaning, or analytics subtasks.
The graphs vary in size (small, medium, large), density (sparse/dense), and presence of cycles.



| Dataset                   | n (nodes) | m (edges)   | Cyclic | SCC count | Largest SCC size |
| ------------------------- | --------- |-------------| ------ | --------- | ---------------- |
| small_1_dag.json          | 6         | 6           | DAG    | 6         | 1                |
| small_2_cycle.json        | 8         | 12          | Cyclic | 3         | 6                |
| small_3_mixed.json        | 9         | 11          | Mixed  | 3         | 3                |
| medium_1_sparse_dag.json  | 12        | 13          | DAG    | 12        | 1                |
| medium_2_multi_scc.json   | 15        | 27          | Cyclic | 3         | 5                |
| medium_3_mixed.json       | 17        | 38          | Mixed  | 1         | 17               |
| large_1_sparse.json       | 22        | 37          | DAG    | 22        | 1                |
| large_2_medium.json       | 26        | 115         | DAG    | 26        | 1                |
| large_3_dense_cycles.json | 30        | 245         | Cyclic | 30        | 1                |

2. Results


   Below are summarized algorithmic results for all datasets.
   All times are in milliseconds (ms).

| Dataset                       | DFS<br>visits | DFS<br>edges | Tarjan<br>time | Condensation<br>time | Topo<br>len | Kahn<br>push/pop | Kahn<br>time | Relaxations | DAG<br>shortest | DAG<br>longest | Shortest<br>dist | Longest<br>dist |
| :---------------------------- | ------------: | -----------: | -------------: | -------------------: | ----------: | ---------------: | -----------: | ----------: | --------------: | -------------: | ---------------: | --------------: |
| **small_1_dag.json**          |             6 |            6 |          15.12 |                 5.70 |           6 |              6/6 |         1.90 |          12 |           2.996 |          0.096 |                1 |              10 |
| **small_2_cycle.json**        |             8 |           12 |           5.34 |                 4.69 |           3 |              3/3 |         1.86 |           6 |           2.660 |          0.065 |                2 |               4 |
| **small_3_mixed.json**        |             9 |           11 |           6.66 |                 5.71 |           3 |              3/3 |         2.17 |           2 |           4.303 |          0.075 |                4 |               4 |
| **medium_1_sparse_dag.json**  |            12 |           13 |           7.39 |                 6.02 |          12 |            12/12 |         2.31 |          24 |           3.424 |          0.199 |                2 |              14 |
| **medium_2_multi_scc.json**   |            15 |           27 |           5.54 |                 4.44 |           3 |              3/3 |         1.50 |           6 |           2.895 |          0.070 |                1 |               5 |
| **medium_3_mixed.json**       |            17 |           38 |           5.58 |                 2.73 |           1 |              1/1 |         1.86 |           0 |           2.838 |          0.031 |                – |               0 |
| **large_1_sparse.json**       |            22 |           37 |           6.95 |                 5.82 |          22 |            22/22 |         1.78 |          32 |           3.524 |          0.126 |                1 |              11 |
| **large_2_medium.json**       |            26 |          115 |           5.23 |                 9.50 |          26 |            26/26 |         2.70 |         230 |           4.389 |          0.594 |                1 |              44 |
| **large_3_dense_cycles.json** |            30 |          245 |           6.34 |                23.31 |          30 |            30/30 |         3.78 |         490 |           5.960 |          0.648 |                1 |              58 |




3. Analysis


   3.1 SCC Detection (Tarjan)

Bottlenecks: For dense graphs (large_3_dense_cycles.json), Tarjan’s recursion and stack operations cause higher runtime (up to 23.3 ms).

Observation: Sparse DAGs (e.g., medium_1_sparse_dag.json) show very low Tarjan times since there are no cycles and minimal low-link updates.

Effect of structure:

Cyclic graphs: Lead to fewer SCCs but larger components.

Acyclic graphs: Many SCCs of size 1.

Metrics trend: DFS edges grow proportionally to graph density; DFS visits = |V|.

3.2 Topological Ordering (Kahn)

Bottlenecks: The number of pushes/pops = number of vertices, so complexity is linear in |V| + |E|.

Observation: Dense graphs show slightly increased Kahn time due to queue operations but remain efficient (<4 ms even for 30 nodes).

Condensation DAG: Time increases when many SCCs merge into fewer nodes (more edges in DAG).

3.3 Shortest and Longest Paths (DAG-SP)

Model: Edge weights (as specified).

Shortest paths: Linear in |V| + |E|; relaxations scale linearly.

Longest path (Critical Path): Found via sign inversion and DP over topological order.

Observation:

large_2_medium.json shows 230 relaxations — matches 115 edges × 2 directions in weighted DAG.

Dense cyclic graphs show the highest timing due to many relaxation attempts before SCC compression.

4. Conclusions

Tarjan SCC:

Best for cyclic detection and condensation building.

Time grows with density; efficient for sparse real-world task graphs.

Recommended when cycles exist or dependency validation is needed.

Topological Sort (Kahn):

Fast and reliable on DAGs.

Ideal for scheduling after SCC compression.

Time cost is negligible even on large graphs.

Shortest/Longest Paths in DAG:

Simple DP method is highly efficient.

Longest path gives insight into critical scheduling chains.

Recommended for optimized task planning after removing cycles.

Overall Performance Trends:

Sparse DAGs yield fastest execution.

Dense cyclic graphs produce measurable delays in SCC compression and path reconstruction.

Across all datasets, runtime remains within milliseconds — algorithms are suitable for real-time smart-city scheduling analytics.

| Situation                              | Recommended Algorithm       | Reason                                            |
| -------------------------------------- | --------------------------- | ------------------------------------------------- |
| Detecting dependencies or loops        | **Tarjan SCC**              | Fast cycle detection, necessary before scheduling |
| Task scheduling in DAGs                | **Topological Sort (Kahn)** | Clear order without recursion                     |
| Critical path / longest duration tasks | **DAG Longest Path (DP)**   | Identifies performance bottlenecks                |
| Earliest task completion times         | **DAG Shortest Path (DP)**  | Efficient schedule optimization                   |

5. Run Instructions

| Class                            | Description                                                                 | How to Run                    |
| -------------------------------- |-----------------------------------------------------------------------------|-------------------------------|
| `graph.scc.TarjanSCC`            | Finds in a directed graph using Tarjan’s algorithm.                         | Run `Main.java`                 |
| `graph.topo.KahnTopologicalSort` | Performs topological sorting using Kahn’s algorithm.                        | Rin `Main.java`                 |
| `graph.dagsp.DAGShortestPath`    | Computes shortest paths in a DAG.                                           | Run `Main.java`                 |
| `util.DatasetInspector`          | Loads datasets, collects metrics, and saves them to CSV for later analysis. | Run `DatasetInspector.main()` |

