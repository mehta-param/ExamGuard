# Phase 1 Context: Project Foundation, Conflict Graph & Pure Algorithms

## 1. Executive Summary & Goals
Phase 1 builds the structural foundation of ExamGuard:
- Maven project setup targeting Java 17 LTS with JavaFX 21 dependencies and JUnit 5.
- Pure Java domain models (`Subject`, `Student`, `Enrollment`, `ConflictEdge`, `ConflictGraph`, `Schedule`).
- `ConflictGraphBuilder` that transforms student course enrollments into an undirected conflict graph with rich student clash metadata.
- Welsh-Powell Greedy heuristic algorithm with descending-degree ordering.
- Recursive Backtracking exact $k$-coloring decision and chromatic number search with safety checks and backtrack limits.
- Decoupled `ExecutionTrace` recording of discrete `AlgorithmStep` events for future Phase 2 JavaFX animation playback.
- Comprehensive JUnit 5 test suite verifying graph construction, edge derivation, and coloring correctness.

## 2. Locked Technical Decisions

### 2.1 Language, Build & Packaging
- **Target Java Version**: Java 17 LTS (records, switch expressions, strong academic portability).
- **Build System**: Apache Maven (`pom.xml`).
- **Dependencies**:
  - `org.openjfx:javafx-controls:21.0.2`
  - `org.openjfx:javafx-fxml:21.0.2`
  - `org.xerial:sqlite-jdbc:3.45.1.0`
  - `org.junit.jupiter:junit-jupiter:5.10.2` (test scope)
- **Plugin**: `org.openjfx:javafx-maven-plugin:0.0.8` (configured with `com.examguard.App` main class).
- **Architectural Boundary**: Pure Java in `com.examguard.model` and `com.examguard.algorithm`. Zero `javafx.*` imports in these packages. Headless JUnit testing.

### 2.2 Domain & Graph Modeling (`com.examguard.model`)
- **Entities**:
  - `Subject(int id, String code, String name)`
  - `Student(int id, String studentId, String name)`
  - `Enrollment(int studentId, int subjectId)`
  - `ConflictEdge(Subject subjectA, Subject subjectB, Set<Student> conflictingStudents)`
- **ConflictGraph**:
  - Maps vertices to index $0 \dots V-1$ for high-speed matrix and array operations while retaining `Subject` lookup.
  - Maintains both **Adjacency List** (`Map<Integer, Set<Integer>>`) and **Adjacency Matrix** (`boolean[][]`).
  - Stores rich conflict edges: `Map<EdgeKey, Set<Student>>` so any edge $(u, v)$ can report exactly which students enrolled in both.
  - Graph metrics: `getVertexCount()`, `getEdgeCount()`, `getDegree(int v)`, `getMaxDegree()`, `getDensity()`.

### 2.3 Algorithm & Tracing Architecture (`com.examguard.algorithm`)
- **Core Interface**:
  ```java
  public interface GraphColoringAlgorithm {
      ColoringResult color(ConflictGraph graph);
      ColoringResult colorWithLimit(ConflictGraph graph, int maxColors);
  }
  ```
- **Step Tracing (`ExecutionTrace` & `AlgorithmStep`)**:
  - `StepType` enum:
    - `SELECT_VERTEX`: Focuses on a vertex $v$.
    - `TRY_COLOR`: Attempts timeslot color $c$ on vertex $v$.
    - `CONFLICT_DETECTED`: Detects that neighbor $u$ already has color $c$ (caused by student clashes).
    - `ASSIGN_COLOR`: Safely assigns color $c$ to vertex $v$.
    - `BACKTRACK`: No valid color in range; resets assignment and retreats to previous vertex.
    - `FINISH`: Algorithm completes (success or failure).
  - `AlgorithmStep`:
    - `int stepNumber`
    - `StepType type`
    - `int vertexIndex`
    - `int color` (1-indexed timeslot, 0 = uncolored)
    - `int conflictingNeighborIndex` (-1 if none)
    - `String explanation` (e.g., *"Cannot assign Slot 1 to CS102: conflicts with CS101 (Student Alice is enrolled in both)"*)
  - `ColoringResult`:
    - `boolean success`
    - `Map<Integer, Integer> assignments` (vertex index -> color)
    - `int colorsUsed` (chromatic count / slots used)
    - `long executionTimeNanos`
    - `int totalSteps`
    - `ExecutionTrace trace`

### 2.4 Algorithm Implementations
- **Welsh-Powell Greedy (`WelshPowellAlgorithm`)**:
  1. Calculate degrees for all vertices.
  2. Order vertices in descending order of degree: $\deg(v_1) \ge \deg(v_2) \ge \dots \ge \deg(v_n)$.
  3. For each color $c = 1, 2, \dots$:
     - Assign color $c$ to the first uncolored vertex in the sorted list.
     - Scan down the sorted list: assign color $c$ to any uncolored vertex that has no conflict with already assigned vertices of color $c$.
  4. Record all steps in `ExecutionTrace`.
- **Recursive Backtracking (`BacktrackingAlgorithm`)**:
  1. Solves the $K$-coloring decision problem recursively:
     - `isSafe(v, c)` checks if any adjacent vertex has color $c$.
     - Base case: If all vertices colored, return true.
     - Try colors $c = 1 \dots K$: if safe, assign and recurse to $v + 1$. If recursion returns false, backtrack and try next color.
  2. Chromatic number search: Tests $K = 1, 2, \dots$ up to Welsh-Powell upper bound $\min(\Delta + 1, K_{WP})$. The first $K$ that succeeds is the exact chromatic number $\chi(G)$.
  3. Safety Safeguard: Configurable maximum step threshold (default 50,000 steps) to prevent infinite recursion or UI freezes on unsolvable/dense inputs.

## 3. Test & Verification Plan
- **Unit Tests (`src/test/java/com/examguard/`)**:
  - `ConflictGraphTest`:
    - Independent subjects (0 shared students) produce empty edge set.
    - Two subjects shared by 1 student produce 1 undirected edge with student recorded.
    - Multi-student enrollment produces exact graph density and degree calculations.
  - `WelshPowellAlgorithmTest`:
    - Valid coloring verification (for all edges $(u, v)$, $color(u) \neq color(v)$).
    - Degree sorting verification.
    - Trace recording completeness.
  - `BacktrackingAlgorithmTest`:
    - Valid coloring verification on bipartite, cycle, and complete graphs.
    - Solves known graphs with exact chromatic numbers (e.g. $K_4 \implies 4$ colors, $C_5 \implies 3$ colors).
    - Correctly fails when $K < \chi(G)$.
    - Enforces step threshold without throwing uncaught exceptions.

## 4. Next Step
Run `/gsd-plan-phase 1` to generate detailed implementation plans.
