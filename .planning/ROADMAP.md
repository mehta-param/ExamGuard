# Project Roadmap: ExamGuard

## Overview
ExamGuard is structured into 3 vertical MVP phases, progressively delivering working, demonstrable software from core graph-coloring algorithms to interactive JavaFX animation, timetable viewing, performance benchmarking, and data persistence.

## Phase Overview

| # | Phase | Mode | Goal | Requirements | Success Criteria |
|---|---|---|---|---|---|
| 1 | Project Foundation, Conflict Graph & Pure Algorithms | mvp | Build project scaffolding, domain models, conflict graph builder, Welsh-Powell & Recursive Backtracking with execution tracing and unit tests | REQ-GRAPH-01, REQ-GRAPH-02, REQ-GRAPH-03, REQ-ALGO-01, REQ-ALGO-02, REQ-ALGO-03, REQ-ALGO-04 | 4 criteria |
| 2 | Interactive JavaFX Canvas, Step Animator & Timetable View | mvp | Build custom JavaFX Canvas with circular layout, step-by-step playback controls, visual explanation banner, and timetable schedule view | REQ-ANIM-01, REQ-ANIM-02, REQ-ANIM-03, REQ-ANIM-04, REQ-TIME-01, REQ-TIME-02 | 4 criteria |
| 3 | Benchmarking, SQLite Persistence & Dataset Management | mvp | Build empirical performance comparison view, preloaded viva demo scenarios, CSV import/export, manual entry UI, and SQLite persistence | REQ-PERF-01, REQ-PERF-02, REQ-DATA-01, REQ-DATA-02, REQ-DATA-03, REQ-DATA-04 | 4 criteria |

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
**Goal**: Build custom JavaFX `Canvas` with circular node positioning, animated step-by-step coloring playback engine, live algorithmic explanation banner, and conflict-free timetable view.
**Mode**: mvp
**UI hint**: yes

**Requirements Mapped**:
- `REQ-ANIM-01`: Custom JavaFX Canvas with circular layout
- `REQ-ANIM-02`: Visual feedback for active vertex, color attempts, and conflict edges
- `REQ-ANIM-03`: Playback controls: Play, Pause, Step-Forward, Reset, Speed Slider
- `REQ-ANIM-04`: Plain English algorithmic action explanation banner
- `REQ-TIME-01`: Timetable view grouped by timeslots
- `REQ-TIME-02`: Conflict validation indicator badge ("Zero Conflicts Verified")

**Success Criteria**:
1. Desktop JavaFX application launches with a clean, high-contrast circular conflict graph canvas.
2. User can select Welsh-Powell or Backtracking and watch smooth step-by-step animation with Play, Pause, Step-Forward, and Speed adjustment.
3. Visual highlights show current vertex inspection, color assignments, and conflict line alerts alongside an active explanation banner.
4. Timetable view displays final assigned timeslots with a verified zero-conflict indicator badge.

---

### Phase 3: Benchmarking, SQLite Persistence & Dataset Management
**Goal**: Build side-by-side empirical algorithm performance comparison view, preloaded viva demo datasets, manual data entry UI, CSV import/export, and SQLite persistence.
**Mode**: mvp
**UI hint**: yes

**Requirements Mapped**:
- `REQ-PERF-01`: Side-by-side comparison table between Welsh-Powell and Backtracking
- `REQ-PERF-02`: Empirical comparison metrics (execution time, step count, timeslots used)
- `REQ-DATA-01`: Preloaded demo datasets (Small, Medium, Dense/clash-heavy)
- `REQ-DATA-02`: Manual entry UI for subjects and student enrollments
- `REQ-DATA-03`: CSV import and export for subjects and enrollments
- `REQ-DATA-04`: Local SQLite database persistence via JDBC

**Success Criteria**:
1. Benchmark view renders side-by-side comparison metrics (time in ms/µs, step count, timeslots used) highlighting algorithmic tradeoffs.
2. Preloaded demo scenarios can be loaded with one click for immediate viva demonstrations.
3. User can manually add/edit subjects and student enrollments or import/export via CSV.
4. All subjects, enrollments, and generated schedules persist across app restarts in local SQLite database (`examguard.db`).
