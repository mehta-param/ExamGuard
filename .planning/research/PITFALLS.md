# Pitfalls & Mitigations: ExamGuard

## Technical & Algorithmic Pitfalls

### 1. UI Thread Freeze During Backtracking (Concurrency)
- **Pitfall**: In graph coloring with recursive backtracking, finding the chromatic number on a dense graph with 15+ nodes can take thousands of recursive calls. If run on the JavaFX Application Thread, the window becomes unresponsive ("Not Responding" OS freeze).
- **Mitigation**:
  - Run the algorithm off the UI thread (or pre-generate the `ExecutionTrace` synchronously if graph is small, or inside a `javafx.concurrent.Task`).
  - Cap backtracking depth or offer an explicit timeout / step limit safeguard to prevent runaway recursion during live viva demos.

### 2. Graph Node Overlapping & Cluttered Edges
- **Pitfall**: Arbitrary or random $(x, y)$ coordinate placement of nodes causes overlapping circles and chaotic edge spaghetti, making graph inspection impossible.
- **Mitigation**:
  - Use a clean **Circular Layout**: Nodes arranged along an ellipse or circle with radius proportional to canvas dimensions.
  - Compute angles $\theta_i = \frac{2\pi \cdot i}{N}$ with node coordinates $(cx + R \cos\theta_i, cy + R \sin\theta_i)$.
  - For small graphs ($N \le 20$), circular layout guarantees zero node collisions and predictable edge lines.

### 3. Tying Animation Logic Directly Into Algorithm Loops
- **Pitfall**: Using `Thread.sleep(500)` inside the algorithm's recursive function and calling `Platform.runLater()` to update the UI. This leads to thread synchronization bugs, race conditions, and makes unit testing impossible.
- **Mitigation**:
  - The **Event Trace Pattern**: Algorithms are pure functions that take a graph and return a `ColoringResult` containing the final mapping AND a list of discrete step events (`ExecutionTrace`).
  - The UI animator consumes this trace using a JavaFX `Timeline`. Users can step backwards, pause, restart, or scrub speed without altering algorithm state.

### 4. Color Palette Readability
- **Pitfall**: Using arbitrary random RGB colors can yield unreadable text contrast (e.g. bright yellow text on white background) or indistinguishable color shades.
- **Mitigation**:
  - Predefine a curated palette of 12 distinct, high-contrast, accessible HSL colors with designated dark text labels (e.g., Soft Indigo, Emerald, Amber, Coral, Cyan, Purple, Teal, Rose).

### 5. Over-engineering Persistence
- **Pitfall**: Trying to use Hibernate/JPA, which introduces reflection proxies, annotations, and logging clutter that distract from DAA concepts.
- **Mitigation**:
  - Use straightforward standard JDBC with `java.sql.PreparedStatement` and `try-with-resources`. Easy to inspect and explain in viva.
