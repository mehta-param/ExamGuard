# Phase 2 Context: Interactive JavaFX Canvas, Step Animator & Timetable View

## 1. Executive Summary & Goals
Phase 2 delivers the interactive visual layer of ExamGuard:
- A custom JavaFX `Canvas` that renders the `ConflictGraph` using circular trigonometry (`cos`, `sin`) with high-contrast color fills for timeslots and visual conflict alerts.
- A decoupled playback animator (`GraphAnimator`) using JavaFX `Timeline` to step through the precomputed `ExecutionTrace` with **Play**, **Pause**, **Step-Forward**, and **Reset** controls.
- A live educational explanation banner describing every algorithmic decision and student conflict in plain English.
- A clean conflict-free **Timetable View** grouping subjects by timeslot with an automated **"Zero Conflicts Verified ✓"** status badge.
- Preloaded 6-subject university scenario on startup for immediate live viva demonstration.
- Zero unnecessary abstractions: Direct pure JavaFX code, no FXML reflection complexity, no external graph libraries.

## 2. Locked Technical Decisions

### 2.1 UI Architecture & Components (`com.examguard.ui`)
- **Package Layout**:
  - `com.examguard.App`: Standard JavaFX `Application` entry point launching `MainView`.
  - `com.examguard.ui.GraphCanvas`: Custom Canvas component handling circular node positioning, edge lines, active vertex glow, and color fills.
  - `com.examguard.ui.GraphAnimator`: Controls animation replay of `ExecutionTrace` using JavaFX `Timeline` (~600ms per step).
  - `com.examguard.ui.MainView`: Pure Java layout (`BorderPane`) containing:
    - Top: Algorithm selection (`ComboBox`), "Start Animation" button, "Reset" button.
    - Center: `GraphCanvas` + bottom playback controls (`Play`, `Pause`, `Step-Forward`, `Reset`) + live explanation banner label.
    - Right: Timetable card panel displaying assigned exam slots and conflict verification badge.

### 2.2 Circular Canvas Geometry
- Node positioning formula:
  $$\theta_i = \frac{2\pi \cdot i}{N} - \frac{\pi}{2}, \quad x_i = c_x + R \cos\theta_i, \quad y_i = c_y + R \sin\theta_i$$
- Default radius $R \approx 160\text{px}$, node circle radius $r = 24\text{px}$.
- High-contrast timeslot color palette:
  1. Slot 1: `#4A90E2` (Sky Blue)
  2. Slot 2: `#50E3C2` (Aqua Green)
  3. Slot 3: `#F5A623` (Warm Amber)
  4. Slot 4: `#BD10E0` (Purple)
  5. Slot 5: `#9013FE` (Deep Violet)
  6. Slot 6: `#D0021B` (Coral Red)
  7. Slot 7: `#FF69B4` (Pink)
  8. Slot 8: `#F8E71C` (Bright Yellow)
  - Uncolored: `#E0E0E0` (Neutral light grey with dark outline).
  - Active vertex highlight: `#FFF3CD` with bold golden border.
  - Clashing edge highlight: `#E74C3C` with thicker stroke width.

### 2.3 Animation Replay Mechanism
- Pure linear step index `int currentStepIndex` advancing through `trace.getSteps()`.
- Standard JavaFX `Timeline` with `KeyFrame(Duration.millis(600))` calling `stepForward()`.
- Buttons:
  - `Play`: Starts or resumes the timeline.
  - `Pause`: Pauses the timeline at the current step.
  - `Step-Forward`: Pauses the timeline and advances exactly 1 step.
  - `Reset`: Stops the timeline, resets step index to 0, and restores initial graph state.

### 2.4 Preloaded Startup Dataset
- 6 university subjects:
  - `CS101` (Intro to Programming)
  - `CS102` (Data Structures)
  - `MATH101` (Calculus I)
  - `MATH102` (Linear Algebra)
  - `PHYS101` (Physics I)
  - `CHEM101` (Chemistry I)
- Students with realistic overlaps creating clashes requiring 3 timeslots, demonstrating both Welsh-Powell and Backtracking in live viva demos.

## 3. Requirements Mapped to Phase 2
- `REQ-ANIM-01`: Circular Canvas conflict graph layout.
- `REQ-ANIM-02`: Dynamic visual highlights for vertices, candidate colors, and clashing edges.
- `REQ-ANIM-03`: Animation playback controls (Play, Pause, Step, Reset).
- `REQ-ANIM-04`: Plain-English explanation banner.
- `REQ-TIME-01`: Grouped timetable view by timeslot.
- `REQ-TIME-02`: Conflict validation status badge.

## 4. Next Step
Run `/gsd-plan-phase 2` to generate the detailed Phase 2 implementation plan.
