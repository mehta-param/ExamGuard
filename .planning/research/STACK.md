# Technology Stack Research: ExamGuard

## Recommended Tech Stack

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| **Language** | Java (OpenJDK) | 17 LTS or 21 LTS | Modern records, switch pattern matching, strong typing, clean syntax for viva. |
| **UI Framework** | JavaFX (openjfx) | 21+ | Native desktop GUI, high-performance Canvas rendering, built-in animation API (`Timeline`, `KeyFrame`). |
| **Build & Dependencies** | Apache Maven | 3.8+ | Standard dependency management, straightforward `javafx-maven-plugin` configuration. |
| **Persistence** | SQLite with SQLite-JDBC | 3.45+ | Embedded zero-configuration database; single `.db` file in user directory or project root. |
| **Testing** | JUnit 5 | 5.10+ | Clean unit testing for algorithm correctness and conflict graph assertions. |

## Detailed Layer Analysis

### 1. UI & Graphics Layer: JavaFX Canvas vs Scene Graph Nodes
- **Selected Approach**: Custom `javafx.scene.canvas.Canvas` + `GraphicsContext`.
- **Why**: 
  - Having 20–50 graph nodes as individual JavaFX `Circle` and `Line` nodes can cause scene graph overhead and complex state synchronization during high-speed animation.
  - A Canvas-based render loop redraws nodes, edges, labels, and color highlights cleanly with clear mathematical coordinates:
    ```
    x = centerX + radius * cos(2 * PI * i / N)
    y = centerY + radius * sin(2 * PI * i / N)
    ```
  - Perfect for viva: The student can explain every pixel calculation without third-party magic.

### 2. Animation & Concurrency
- **Problem**: Running a recursive backtracking algorithm or iterative greedy loop directly on the JavaFX Application Thread freezes the UI window.
- **Solution**:
  - Algorithm runs in a background worker (`javafx.concurrent.Task<T>`) or emits step snapshots into an event queue.
  - Alternatively, algorithms record an `ExecutionTrace` (list of `AlgorithmStep` events: VISIT_NODE, TRY_COLOR, CONFLICT_DETECTED, ASSIGN_COLOR, BACKTRACK).
  - A JavaFX `Timeline` or `PauseTransition` replays the trace on the Canvas with Play / Pause / Step-Forward / Speed Slider controls.
  - This separation makes algorithm logic 100% pure (pure Java, zero JavaFX imports in algorithm classes) and keeps UI responsive!

### 3. Data & Storage: SQLite with Raw JDBC
- **Why avoid Hibernate / JPA / Spring Data**: Heavy enterprise ORMs add enormous boilerplate (entities, annotations, entity managers) that confuses viva evaluators and obscures basic SQL concepts.
- **Approach**: A simple `DatabaseManager` with clean `PreparedStatement` methods for:
  - `subjects` (`id`, `code`, `name`)
  - `students` (`id`, `student_id`, `name`)
  - `enrollments` (`student_id`, `subject_id`)
  - `schedules` (`id`, `dataset_name`, `algorithm`, `subject_code`, `timeslot`)

### 4. Build Configuration: Maven
- Uses standard `org.openjfx:javafx-controls`, `org.openjfx:javafx-fxml`, `org.xerial:sqlite-jdbc`.
- Plugins: `javafx-maven-plugin` to run `mvn javafx:run` directly without complex modular JVM runtime flags on the command line.
