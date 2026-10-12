# Requirements: ExamGuard

## Overview
ExamGuard is a DAA college project that solves the Exam Timetable Conflict Minimization problem by representing subjects and shared student enrollments as an undirected conflict graph, applying Welsh-Powell Greedy and Recursive Backtracking graph coloring, animating execution on a custom JavaFX Canvas, and comparing algorithmic performance.

## v1 Requirements

### Conflict Graph Modeling (`GRAPH`)
- **REQ-GRAPH-01**: Represent subjects as vertices and shared student enrollments as undirected conflict edges.
- **REQ-GRAPH-02**: Provide adjacency list and adjacency matrix representations with graph metrics (vertex count, edge count, vertex degrees, maximum degree $\Delta(G)$).
- **REQ-GRAPH-03**: Verify zero false conflicts and calculate graph density $\frac{2|E|}{|V|(|V|-1)}$.

### Graph Coloring Algorithms (`ALGO`)
- **REQ-ALGO-01**: Implement Welsh-Powell Greedy graph-coloring heuristic (descending vertex degree ordering + first-fit color assignment).
- **REQ-ALGO-02**: Implement Recursive Backtracking graph-coloring algorithm (exact $k$-coloring decision check and minimum chromatic number $\chi(G)$ finder).
- **REQ-ALGO-03**: Support optional fixed $K$-timeslot feasibility test (determine whether all exams can be scheduled within $K$ slots).
- **REQ-ALGO-04**: Decouple algorithm execution from GUI by recording an immutable `ExecutionTrace` of step events (`TRY_COLOR`, `CONFLICT`, `ASSIGN`, `BACKTRACK`, `FINISH`).

### Graph Visualization & Animation (`ANIM`)
- **REQ-ANIM-01**: Render conflict graph on custom JavaFX `Canvas` with an automated circular mathematical layout (no third-party physics engine).
- **REQ-ANIM-02**: Dynamic visual feedback: highlight active vertex, display candidate color fills, and highlight conflicting edges with adjacent vertices.
- **REQ-ANIM-03**: Animation playback controls: Play, Pause, Step-Forward, Reset, and adjustable Speed Slider.
- **REQ-ANIM-04**: Live text explanation banner describing the exact algorithmic action occurring at each step in plain English for viva clarity.

### Timetable & Validation View (`TIME`)
- **REQ-TIME-01**: Display conflict-free timetable view grouped by Timeslot with scheduled subjects.
- **REQ-TIME-02**: Automated conflict validator confirming zero student clashes across all slots with a visual status badge.

### Algorithm Performance Benchmark (`PERF`)
- **REQ-PERF-01**: Side-by-side comparison table between Welsh-Powell Greedy and Recursive Backtracking.
- **REQ-PERF-02**: Compare empirical metrics: Execution time (ms/µs), total step/operation count, and total timeslots used.

### Data Management & Persistence (`DATA`)
- **REQ-DATA-01**: Preloaded demo datasets (Small 5-subject, Medium 12-subject, Dense/clash-heavy).
- **REQ-DATA-02**: Manual entry UI for subjects and student course enrollments.
- **REQ-DATA-03**: CSV import and export for subjects and student enrollments.
- **REQ-DATA-04**: Local SQLite database persistence via JDBC.

## Traceability Matrix

| Requirement ID | Category | Phase | Status |
|---|---|---|---|
| REQ-GRAPH-01 | Graph Modeling | Phase 1 | Completed |
| REQ-GRAPH-02 | Graph Modeling | Phase 1 | Completed |
| REQ-GRAPH-03 | Graph Modeling | Phase 1 | Completed |
| REQ-ALGO-01 | Algorithms | Phase 1 | Completed |
| REQ-ALGO-02 | Algorithms | Phase 1 | Completed |
| REQ-ALGO-03 | Algorithms | Phase 1 | Completed |
| REQ-ALGO-04 | Algorithms | Phase 1 | Completed |
| REQ-ANIM-01 | Visualization & UI | Phase 2 | Pending |
| REQ-ANIM-02 | Visualization & UI | Phase 2 | Pending |
| REQ-ANIM-03 | Visualization & UI | Phase 2 | Pending |
| REQ-ANIM-04 | Visualization & UI | Phase 2 | Pending |
| REQ-TIME-01 | Timetable View | Phase 2 | Pending |
| REQ-TIME-02 | Timetable View | Phase 2 | Pending |
| REQ-PERF-01 | Benchmarking | Phase 3 | Pending |
| REQ-PERF-02 | Benchmarking | Phase 3 | Pending |
| REQ-DATA-01 | Data & Persistence | Phase 3 | Pending |
| REQ-DATA-02 | Data & Persistence | Phase 3 | Pending |
| REQ-DATA-03 | Data & Persistence | Phase 3 | Pending |
| REQ-DATA-04 | Data & Persistence | Phase 3 | Pending |

## Out of Scope (Explicit Boundaries)
- **Room / Venue Allocation**: Omitted to keep algorithmic focus on graph vertex coloring without 2D bin packing constraints.
- **Faculty / Invigilator Scheduling**: Additional constraint satisfaction problem outside core DAA graph syllabus.
- **User Authentication & Cloud Storage**: Desktop educational tool for viva demonstration; SQLite provides local single-user persistence.
- **Third-Party Graph Libraries**: Custom JavaFX Canvas ensures 100% of visual rendering code is viva-explainable without library magic.

## Future Considerations (v2)
- Multi-slot constraints (e.g., student cannot have two exams on the same day).
- Interactive drag-and-drop node positioning on the Canvas.
- PDF timetable export.
