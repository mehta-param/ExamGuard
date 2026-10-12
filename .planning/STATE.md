# Project State: ExamGuard

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-10-12)

**Core value:** Clear, demonstrable, and educational graph-coloring conflict resolution that a solo student can thoroughly explain in a viva and easily modify.
**Current focus:** Phase 2: Interactive JavaFX Canvas, Step Animator & Timetable View

## Current Phase

- **Active Phase**: Phase 3
- **Phase Goal**: Build side-by-side empirical performance comparison view, full CRUD management UI for subjects, students, and enrollments, SQLite persistence via JDBC, and demo datasets.
- **Context**: `.planning/phases/03-benchmarking-sqlite-persistence-entity-crud-management/03-CONTEXT.md`
- **Status**: Ready to plan (`/gsd-plan-phase 3`)

## Phase Map

| Phase | Name | Mode | Status |
|---|---|---|---|
| 1 | Project Foundation, Conflict Graph & Pure Algorithms | mvp | Completed ✓ |
| 2 | Interactive JavaFX Canvas, Step Animator & Timetable View | mvp | Completed ✓ |
| 3 | Benchmarking, SQLite Persistence & Entity CRUD Management | mvp | In Progress (Planning) |

## Key Decisions

| Decision | Date | Context | Outcome |
|---|---|---|---|
| Custom Canvas vs Third-party Lib | 2026-10-12 | Custom JavaFX Canvas ensures 100% viva-explainable math | ✓ Good |
| Pure Algorithm + Event Trace | 2026-10-12 | Complete separation of algorithm logic from JavaFX UI thread | ✓ Good |
| Welsh-Powell + Recursive Backtracking | 2026-10-12 | Classic DAA comparison: polynomial heuristic vs exponential exact search | ✓ Good |
| SQLite with JDBC | 2026-10-12 | Embedded, lightweight single-file DB without ORM bloat | ✓ Good |
| Vertical MVP Phase Slices | 2026-10-12 | Working end-to-end increments rather than deferred integration | ✓ Good |
