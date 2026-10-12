# ExamGuard Development Guidelines

This project follows the GSD (Get Stuff Done) workflow.

## Project Summary
ExamGuard is a beginner-friendly educational Java & JavaFX desktop application for a university DAA project. It solves the Exam Timetable Conflict Minimization problem using Welsh-Powell Greedy and Recursive Backtracking graph-coloring algorithms, featuring an interactive Canvas animation and performance benchmarking.

## Key Principles & Constraints
1. **Educational Clarity & Viva Defense**:
   - The code must be clean, readable, well-commented, and free of unnecessary abstractions.
   - Core algorithms must be implemented directly in pure Java (`com.examguard.algorithm`) with zero JavaFX or GUI dependencies.
2. **Animation Architecture**:
   - Algorithms produce an `ExecutionTrace` consisting of discrete `AlgorithmStep` events.
   - The UI animator replays this trace using a JavaFX `Timeline`, keeping algorithms pure and preventing UI thread freezes.
3. **Graph Rendering**:
   - The conflict graph is drawn on a custom JavaFX `Canvas` using circular trigonometry (`cos`, `sin`). No external graph libraries.
4. **Persistence**:
   - Local SQLite using standard JDBC. No JPA/Hibernate overhead.
5. **Workflow**:
   - Work through `.planning/ROADMAP.md` phase by phase using `/gsd-plan-phase [N]` and `/gsd-execute-phase [N]`.
