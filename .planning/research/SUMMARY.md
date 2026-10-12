# Project Research Summary: ExamGuard

## Key Findings

1. **Algorithm Architecture**: 
   - A pure, decoupled algorithm layer (Welsh-Powell Greedy heuristic + Recursive Backtracking exact search) returning an `ExecutionTrace` of immutable step events provides clean unit-testability while empowering smooth, interactive JavaFX UI animation.
   - Welsh-Powell (sorting vertices by degree descending) achieves near-optimal coloring in $O(V \log V + E)$ time, serving as an intuitive heuristic.
   - Recursive Backtracking searches for exact chromatic number $\chi(G)$, offering a classic DAA demonstration of exponential state-space exploration and pruning.

2. **UI & Visualization Strategy**:
   - A custom JavaFX `Canvas` with circular mathematical node positioning eliminates third-party library bloat and renders instantly with crisp 60fps performance.
   - Separating algorithm execution from animation playback via a recorded step trace avoids UI thread blocking and concurrency hazards.

3. **Data & Storage Strategy**:
   - SQLite via JDBC provides local, portable, zero-configuration persistence without complex ORM layers.
   - Providing preloaded demo datasets (small, medium, dense conflict) ensures immediate, foolproof viva demonstrations without typing errors.

4. **Educational Priority**:
   - Keeping class and method names strictly aligned with standard DAA terminology (`ConflictGraph`, `isSafe`, `welshPowellColor`, `backtrackColor`, `ExecutionTrace`) ensures the code is transparent, educational, and easily defendable in viva exams.

## Implications for Roadmap

- **Phase 1: Project Foundation & Domain Modeling**: Establish Maven build, JavaFX environment, domain models (`Subject`, `Student`, `Enrollment`), `ConflictGraph` builder, and SQLite schema with sample datasets.
- **Phase 2: Graph Coloring Algorithms & Benchmarking**: Implement Welsh-Powell Greedy and Recursive Backtracking with step trace recording, unit tests, and performance comparison engine.
- **Phase 3: Interactive Conflict Graph Canvas & Step Animator**: Build the custom JavaFX Canvas with circular layout, node/edge rendering, and step-by-step playback controls (Play, Pause, Step, Speed).
- **Phase 4: Timetable View, Dataset Management & Polish**: Create timetable schedule table/grid view, manual enrollment entry form, CSV import/export, and complete viva documentation guide.

## Sources

- Cormen, Leiserson, Rivest, Stein (CLRS) — *Introduction to Algorithms* (Graph Coloring, Backtracking).
- Welsh, D. J. A., & Powell, M. B. (1967) — *An upper bound for the chromatic number of a graph and its application to timetabling problems*, The Computer Journal.
- OpenJFX Documentation (JavaFX Canvas, Animation Timeline API).
- SQLite-JDBC Official Guide (Xerial).
