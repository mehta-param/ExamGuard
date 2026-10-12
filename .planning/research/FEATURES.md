# Feature Analysis: ExamGuard

## Core Feature Dimensions

### 1. Conflict Graph Generation
- **Subject-Node Representation**: Each unique subject is a vertex $V_i$.
- **Enrollment-Edge Derivation**: An undirected edge $(V_i, V_j)$ exists if and only if $\exists$ at least one student enrolled in both Subject $i$ and Subject $j$.
- **Adjacency Representation**: Adjacency List (`Map<Subject, Set<Subject>>`) and Adjacency Matrix (`boolean[][]`) for easy algorithm consumption.
- **Graph Metrics**: Vertex count $|V|$, edge count $|E|$, vertex degree $\deg(v)$, max degree $\Delta(G)$, density $\frac{2|E|}{|V|(|V|-1)}$.

### 2. Graph Coloring Algorithms (DAA Core)
- **Algorithm A: Welsh-Powell Heuristic (Greedy)**
  - Sort vertices in descending order of degree: $\deg(v_1) \ge \deg(v_2) \ge \dots \ge \deg(v_n)$.
  - Assign Color 1 to the first uncolored vertex; traverse remaining vertices in sorted order and assign Color 1 to any vertex not adjacent to already colored vertices of Color 1.
  - Repeat with Color 2, 3, ... until all vertices are colored.
  - Time Complexity: $O(V \log V + V^2)$ or $O(V \log V + E)$.
- **Algorithm B: Recursive Backtracking (Exact Chromatic Number & K-Coloring)**
  - Recursive search function: `colorGraph(vertexIndex, k)`.
  - For each vertex, try colors $1 \dots k$:
    - Check safety (`isSafe(vertex, color)`: no adjacent vertex shares `color`).
    - Recurse to `vertexIndex + 1`.
    - If no color works, backtrack (reset color, undo assignment).
  - Chromatic number search: Try $k = 1, 2, 3 \dots$ until a valid coloring is found, or binary search over $[1 \dots \Delta(G) + 1]$.
  - Time Complexity: Worst-case $O(k^V)$ exponential, illustrating the NP-complete nature of graph coloring.

### 3. Step-by-Step Visualization & Animation
- **Visual Canvas**:
  - Circular layout (default) where nodes are evenly spaced along a circle for clean readability.
  - Nodes colored dynamically as assigned; uncolored nodes have neutral outline.
  - Conflict edges drawn as subtle grey lines, turning red/warning when a conflict is checked.
- **Animation Controls**:
  - Play / Pause button
  - Step-Forward (inspect single iteration/decision)
  - Reset / Clear
  - Speed Slider (100ms to 2000ms delay per step)
  - Live Status Log / Explanation Banner ("Checking Subject CS101 with Timeslot 1... Conflict with CS102 enrolled by Student Alice! Backtracking...")

### 4. Conflict-Free Timetable View
- **Tabular / Grid View**:
  - Grouped by Timeslot (Slot 1, Slot 2, ...).
  - Lists subjects scheduled in each slot.
  - Shows student count and verifies 0 overlapping students.
- **Validation Badge**: "Zero Conflicts Verified ✓" indicator.

### 5. Empirical Performance Comparison
- Side-by-side comparison table & visual bar metrics:
  - Metric 1: Total Timeslots / Colors Used (Optimality).
  - Metric 2: Execution Time in Milliseconds / Microseconds.
  - Metric 3: Total Operation / Step Count (Comparisons, color checks, backtracks).
- Educational takeaway highlight: Showing when Welsh-Powell finds an optimal or near-optimal solution in milliseconds vs when Backtracking requires heavy step counts.

### 6. Dataset Management & Persistence
- **Preset Demo Scenarios**:
  - Small (5 subjects, 3 slots) — Quick live walkthrough.
  - Medium (12 subjects, realistic university elective clashes).
  - Dense / Complete graph (testing worst-case chromatic number).
  - Bipartite / Cycle graph (theoretical edge cases).
- **Manual Data Entry UI**: Add/edit subjects and student enrollments.
- **CSV Import/Export**: Import students and subjects from plain CSV files.
