# Project Roadmap: ExamGuard

## Overview
ExamGuard is structured into 3 vertical MVP phases, progressively delivering working, demonstrable software from core graph-coloring algorithms to interactive JavaFX animation, timetable viewing, performance benchmarking, and data persistence.

## Phase Overview

| # | Phase | Mode | Goal | Requirements | Success Criteria |
|---|---|---|---|---|---|
| 1 | Project Foundation, Conflict Graph & Pure Algorithms | mvp | Build project scaffolding, domain models, conflict graph builder, Welsh-Powell & Recursive Backtracking with execution tracing and unit tests | REQ-GRAPH-01, REQ-GRAPH-02, REQ-GRAPH-03, REQ-ALGO-01, REQ-ALGO-02, REQ-ALGO-03, REQ-ALGO-04 | 4 criteria |
| 2 | Interactive JavaFX Canvas, Step Animator & Timetable View | mvp | Build custom JavaFX Canvas with circular layout, step-by-step playback controls, explanation banner, timetable view, and dynamic subject subset selection | REQ-ANIM-01 to 05, REQ-TIME-01 to 02 | 5 criteria |
| 3 | Benchmarking, SQLite Persistence & Entity CRUD Management | mvp | Build empirical performance comparison view, full student/subject/enrollment CRUD UI, SQLite persistence, and demo datasets | REQ-PERF-01 to 02, REQ-DATA-01 to 04 | 4 criteria |

---

## Phase Details

### Phase 1: Project Foundation, Conflict Graph & Pure Algorithms
**Goal**: Establish Maven project build, Java 17/JavaFX setup, domain models (`Subject`, `Student`, `Enrollment`), `ConflictGraph` builder from course enrollments, Welsh-Powell Greedy algorithm, Recursive Backtracking algorithm, immutable `ExecutionTrace` recording, and unit test suite verifying conflict-free coloring.
**Mode**: mvp
**UI hint**: no

**Requirements Mapped**:
- `REQ-GRAPH-01`: Subject vertices and shared enrollment conflict edges
- `REQ-GRAPH-02`: Adjacency list and matrix representations with graph metrics
- `REQ-GRAPH-03`: Zero false conflicts verification and density computation
- `REQ-ALGO-01`: Welsh-Powell Greedy heuristic (descending degree ordering + first-fit color)
- `REQ-ALGO-02`: Recursive Backtracking algorithm (exact $k$-coloring & chromatic number search)
- `REQ-ALGO-03`: Fixed $K$-timeslot feasibility checking
- `REQ-ALGO-04`: Decoupled `ExecutionTrace` of step events (`TRY_COLOR`, `CONFLICT`, `ASSIGN`, `BACKTRACK`)

**Success Criteria**:
1. Maven project builds and runs unit tests cleanly with `mvn test`.
2. Given sample enrollments, `ConflictGraph` computes exact adjacency and degree metrics with zero false conflicts.
3. Both Welsh-Powell and Recursive Backtracking color conflict graphs without adjacent clashes, producing recorded `ExecutionTrace` step logs.
4. Fixed $K$-timeslot feasibility test accurately reports true/false for solvable vs uncolorable inputs.

---

### Phase 2: Interactive JavaFX Canvas, Step Animator & Timetable View
**Goal**: Build custom JavaFX `Canvas` with circular node positioning, animated step-by-step coloring playback engine, live algorithmic explanation banner, conflict-free timetable view, and dynamic subject subset selection.
**Mode**: mvp
**UI hint**: yes

**Requirements Mapped**:
- `REQ-ANIM-01`: Custom JavaFX Canvas with circular layout
- `REQ-ANIM-02`: Visual feedback for active vertex, color attempts, and conflict edges
- `REQ-ANIM-03`: Playback controls: Play, Pause, Step-Forward, Reset, Speed Slider
- `REQ-ANIM-04`: Plain English algorithmic action explanation banner
- `REQ-ANIM-05`: Dynamic subject subset selection (inclusion/exclusion filtering)
- `REQ-TIME-01`: Timetable view grouped by timeslots
- `REQ-TIME-02`: Conflict validation indicator badge ("Zero Conflicts Verified")

**Success Criteria**:
1. Desktop JavaFX application launches with a clean, high-contrast circular conflict graph canvas.
2. User can select Welsh-Powell or Backtracking and watch smooth step-by-step animation with Play, Pause, Step-Forward, and Speed adjustment.
3. Visual highlights show current vertex inspection, color assignments, and conflict line alerts alongside an active explanation banner.
4. Timetable view displays final assigned timeslots with a verified zero-conflict indicator badge.
5. Deselecting any subject dynamically rebuilds the active conflict graph and regenerates a valid timetable using only the selected subjects.

---

### Phase 3: Benchmarking, SQLite Persistence & Entity CRUD Management
**Goal**: Build side-by-side empirical algorithm performance comparison view, full CRUD management UI (Add, Edit, Delete) for subjects, students, and student-subject enrollments, SQLite persistence, and demo datasets.
**Mode**: mvp
**UI hint**: yes

**Requirements Mapped**:
- `REQ-PERF-01`: Side-by-side comparison table between Welsh-Powell and Backtracking
- `REQ-PERF-02`: Empirical comparison metrics (execution time, step count, timeslots used)
- `REQ-DATA-01`: Preloaded demo datasets (Small, Medium, Dense/clash-heavy)
- `REQ-DATA-02`: Full CRUD management UI (Add, Edit, Delete) for Subjects, Students, and Enrollments
- `REQ-DATA-03`: CSV import and export for subjects and enrollments (optional utility)
- `REQ-DATA-04`: Local SQLite database persistence via JDBC

**Success Criteria**:
1. Benchmark view renders side-by-side comparison metrics (time in ms/µs, step count, timeslots used) highlighting algorithmic tradeoffs.
2. User can add, edit, and delete subjects, students, and enrollments with changes persisting across app restarts in SQLite (`examguard.db`).
3. Preloaded demo scenarios can be loaded with one click for immediate viva demonstrations.
4. Optional CSV import/export enables external file loading and timetable data export.
