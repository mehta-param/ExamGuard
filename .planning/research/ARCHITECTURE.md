# Architecture & Structural Design: ExamGuard

## Guiding Architectural Principle: Educational Clarity (DAA Viva-Ready)

ExamGuard follows a modular, layer-separated architecture with **zero circular dependencies**, no reflection-heavy frameworks, and distinct separation between **Algorithms**, **Data Models**, **UI/Canvas Rendering**, and **Persistence**.

```
+-------------------------------------------------------------+
|                         JavaFX UI                           |
|  (MainView, GraphCanvas, TimetableView, ComparisonView)     |
+------------------------------+------------------------------+
                               | Dispatches events & observes
                               v
+-------------------------------------------------------------+
|                     Presentation Layer                      |
|       (MainController, GraphAnimator, AnimationTimeline)    |
+------------------------------+------------------------------+
                               | Triggers algorithms & feeds models
                               v
+-------------------------------------------------------------+
|                       Algorithm Layer                       |
|   (GraphColoringAlgorithm, WelshPowellColoring,             |
|    BacktrackingColoring, ExecutionTrace, AlgorithmStep)     |
+------------------------------+------------------------------+
                               | Operates on domain graph
                               v
+-------------------------------------------------------------+
|                         Domain Layer                        |
|   (Subject, Student, Enrollment, ConflictGraph, Schedule)   |
+------------------------------+------------------------------+
                               | Loads & saves entities
                               v
+-------------------------------------------------------------+
|                      Persistence Layer                      |
|       (DatabaseManager, SubjectDao, EnrollmentDao, CsvDao)  |
+-------------------------------------------------------------+
```

## Layer Breakdown

### 1. Domain Layer (`com.examguard.model`)
- `Subject`: Record or class with `id`, `code`, `name`.
- `Student`: Record or class with `id`, `studentId`, `name`.
- `Enrollment`: Links `studentId` to `subjectId`.
- `ConflictGraph`:
  - Contains vertices (`List<Subject>`) and edges (`Map<Subject, Set<Subject>>` or `int[][] matrix`).
  - Helper methods: `getDegree(Subject s)`, `getNeighbors(Subject s)`, `hasConflict(Subject a, Subject b)`.
  - Built by `ConflictGraphBuilder.fromEnrollments(subjects, enrollments)`.
- `Schedule`: Maps each `Subject` to an assigned integer `timeslot` (color).

### 2. Algorithm Layer (`com.examguard.algorithm`)
- **Core Interface**:
  ```java
  public interface GraphColoringAlgorithm {
      ColoringResult color(ConflictGraph graph);
  }
  ```
- **Trace Recorder Pattern**:
  - To allow both instant computation (for benchmarks) and step-by-step playback (for animations), algorithms take an optional trace listener or return an `ExecutionTrace`:
    ```java
    public record AlgorithmStep(
        StepType type, // SELECT_VERTEX, TRY_COLOR, CONFLICT, ASSIGN, BACKTRACK
        int vertexId,
        int color,
        String explanation
    ) {}
    ```
  - This completely decouples algorithm logic from JavaFX! Algorithms can be unit-tested in JUnit without starting JavaFX.

### 3. Presentation & Animation Layer (`com.examguard.ui`)
- `GraphCanvas`: Custom JavaFX `Canvas` extending or wrapping canvas:
  - Renders nodes as circles with subject codes and fill colors corresponding to assigned timeslots.
  - Draws edges with conflict indicator styles.
  - Highlights currently inspected node/edge during animation.
- `GraphAnimator`:
  - Takes `ExecutionTrace` and steps through it using JavaFX `Timeline` or `KeyFrame`.
  - Exposes `play()`, `pause()`, `step()`, `setSpeed(double ms)`.
- `MainController`:
  - Coordinates navigation between Graph Canvas view, Timetable view, and Performance Benchmark view.

### 4. Persistence Layer (`com.examguard.storage`)
- `DatabaseManager`:
  - Initializes SQLite connection: `jdbc:sqlite:examguard.db`.
  - Runs schema creation DDL if tables do not exist.
- `DataService` / `DAO`:
  - Handles CRUD for subjects and enrollments.
  - Pre-populates sample data if database is empty.
  - Handles CSV file reading and writing.

## Viva-Defense Highlights
1. **Explainable Graph Construction**: Demonstrates how $O(S \cdot C^2)$ enrollment joining creates conflict edges.
2. **Purity of Algorithm Code**: No GUI elements inside `WelshPowellColoring.java` or `BacktrackingColoring.java`.
3. **Decoupled Animation Engine**: The GUI merely replays step events recorded by the algorithm.
