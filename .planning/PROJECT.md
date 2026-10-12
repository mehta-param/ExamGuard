# ExamGuard — Exam Timetable Conflict Minimizer

## What This Is

ExamGuard is an educational desktop application built for a college DAA (Design and Analysis of Algorithms) project that generates conflict-free exam timetables from student course enrollments. It models subjects as graph vertices and student sharing as conflict edges, applying Welsh-Powell Greedy and Recursive Backtracking graph-coloring algorithms to assign exam timeslots. The application features an interactive custom JavaFX graph canvas with step-by-step coloring animations, timetable views, and algorithm performance benchmarking.

## Core Value

Clear, demonstrable, and educational graph-coloring conflict resolution that a solo student can thoroughly explain in a viva and easily modify.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Interactive conflict graph representation where subjects are vertices and shared student enrollments are edges
- [ ] Welsh-Powell Greedy graph-coloring algorithm implementation (largest degree first)
- [ ] Recursive Backtracking graph-coloring algorithm implementation (exact k-coloring / chromatic number search)
- [ ] Step-by-step visual animation of both graph-coloring algorithms on a custom JavaFX Canvas
- [ ] Timeslot assignment minimizing total exam slots needed, with option to test fixed K-slot feasibility
- [ ] Conflict-free timetable view (grid/table grouped by timeslot and subject)
- [ ] Preloaded demonstration datasets (small, medium, conflict-dense) plus manual subject/enrollment entry and CSV import/export
- [ ] Empirical algorithm performance comparison view (step count, execution time in ms, chromatic number / slots used)
- [ ] Local persistence of subjects, enrollments, and generated schedules using SQLite via JDBC

### Out of Scope

- Room allocation and venue capacity management — deferred to keep focus strictly on DAA graph coloring
- Faculty/invigilator scheduling — unnecessary complexity for viva scope
- User authentication and role-based access control — single-user local educational tool
- Cloud syncing, remote web services, or multi-tenant database — standalone desktop app priority
- Physics-based external graph visualization libraries — custom Canvas keeps code transparent and viva-defensible

## Context

- Built as a university Design & Analysis of Algorithms (DAA) project.
- The project emphasizes conceptual clarity over enterprise complexity: clean class structures, well-commented algorithms, and direct JavaFX Canvas math (circle nodes, edge lines, color fills).
- The conflict graph is modeled such that if at least one student is enrolled in both Subject A and Subject B, an undirected edge (A, B) exists, meaning they cannot share the same exam timeslot.
- Graph coloring maps colors to discrete exam timeslots (Color 1 = Slot 1, Color 2 = Slot 2, etc.).

## Constraints

- **Tech Stack**: Java (JDK 17+), JavaFX, Maven, SQLite with JDBC — lightweight, portable, zero external native dependencies.
- **Visuals**: Vanilla JavaFX Canvas for graph drawing to ensure complete explainability in academic evaluations.
- **Maintainability**: Clear separation of concerns (Model, Algorithm, View/Canvas, Database) without heavy framework bloat (e.g. no Spring, no heavy ORMs).

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Custom JavaFX Canvas vs External Graph Library | Custom Canvas avoids black-box libraries, making every line of rendering code explainable in viva | ✓ Good |
| Welsh-Powell + Recursive Backtracking | Provides a classic DAA comparison between a polynomial heuristic and an exponential exact algorithm | ✓ Good |
| SQLite with JDBC | Simple single-file relational database without server setup | ✓ Good |
| Preloaded Datasets + CSV Import | Enables immediate instant demo in viva evaluations without tedious manual typing | ✓ Good |

---
*Last updated: 2026-10-12 after initialization questioning*
