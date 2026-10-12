# Phase 3 Context: Benchmarking, SQLite Persistence & Entity CRUD Management

## 1. Executive Summary & Goals
Phase 3 completes the ExamGuard application by delivering:
1. **Top TabPane Navigation**:
   - Tab 1: **Canvas & Timetable** (preserving existing Phase 2 interactive view).
   - Tab 2: **Algorithm Benchmark** (empirical side-by-side comparison table between Welsh-Powell Greedy and Recursive Backtracking).
   - Tab 3: **Data Management** (full CRUD management for Subjects and Students, plus an Enrollment checklist).
2. **Side-by-Side Algorithm Benchmarking**:
   - Compares Welsh-Powell Greedy vs. Recursive Backtracking on the active graph and preset benchmark scenarios.
   - Measures and compares: Execution Time (ms / µs), Steps Recorded, Chromatic Number / Timeslots Used, Solution Validity, and DAA Tradeoff summary.
3. **Local SQLite Persistence (JDBC)**:
   - Zero-configuration local database `examguard.db` using standard JDBC (no JPA/Hibernate overhead).
   - Auto-creates tables on first launch: `subjects`, `students`, `enrollments`.
   - Auto-seeds from `SampleData` if the database is initially empty so the app is always ready out-of-the-box.
   - Provides a "Reset to Demo Data" button.
4. **Entity CRUD Management**:
   - `TableView<Subject>` with Add/Edit/Delete dialogs.
   - `TableView<Student>` with Add/Edit/Delete dialogs.
   - Student-Subject Enrollment editor: select a student to check/uncheck subject registrations, automatically rebuilding database records and in-memory conflict graphs.

---

## 2. Locked Technical Decisions

### 2.1 Navigation & Layout Architecture
- **Root Container**: `TabPane` replacing single view in `App.java`.
  - **Tab 1 ("Timetable & Animation")**: Existing `MainView`.
  - **Tab 2 ("Algorithm Benchmark")**: `BenchmarkView`.
  - **Tab 3 ("Data Management")**: `DataManagementView`.
- Tab styling: Clean, modern JavaFX styling matching existing high-contrast aesthetics.

### 2.2 SQLite Schema & Persistence (`com.examguard.db`)
- Database path: `examguard.db` in project root.
- Driver: `org.sqlite.JDBC` (already in `pom.xml`: `sqlite-jdbc:3.45.1.0`).
- Tables:
  ```sql
  CREATE TABLE IF NOT EXISTS subjects (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      code TEXT NOT NULL UNIQUE,
      name TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS students (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      email TEXT NOT NULL UNIQUE
  );

  CREATE TABLE IF NOT EXISTS enrollments (
      student_id INTEGER NOT NULL,
      subject_id INTEGER NOT NULL,
      PRIMARY KEY (student_id, subject_id),
      FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
      FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
  );
  ```
- Component: `DatabaseManager.java` managing connections, schema creation, seeding, and DAO operations for `Subject`, `Student`, and `Enrollment`.
- Clean error handling with informative alerts if constraint violations occur (e.g., duplicate code or email).

### 2.3 Data Management View (`DataManagementView.java`)
- Sub-tabs or split layout:
  - **Subjects Tab**: TableView with ID, Code, Name. Action buttons: "Add Subject", "Edit Subject", "Delete Subject".
  - **Students Tab**: TableView with ID, Name, Email. Action buttons: "Add Student", "Edit Student", "Delete Student".
  - **Enrollments Tab**: Student selector dropdown/table on left, Subject checklist on right. Checking/unchecking updates enrollment in SQLite and refreshes active graph.
  - **Preset Controls**: "Reset to Demo Data" button restoring the default 6-subject 8-student scenario.

### 2.4 Algorithm Benchmarking View (`BenchmarkView.java`)
- Side-by-side comparison table / cards:
  - Metrics:
    - **Execution Time**: measured using high-precision `System.nanoTime()` (formatted in milliseconds and microseconds).
    - **Step Count**: discrete operations from `ExecutionTrace.getSteps().size()`.
    - **Timeslots Used**: chromatic count comparison ($\chi(G)$ exact vs greedy approximation).
    - **Optimality**: badge indicating optimal vs heuristic result.
    - **Conflict Check**: verification confirming 0 adjacent clashes.
- Test Scenarios selector:
  - Current Active Graph
  - Small Demo (6 subjects, 8 students)
  - Medium Scenario (10 subjects, 15 students)
  - Dense / Clash-Heavy Scenario (complete subgraphs, testing backtracking branching)
- Viva explanation panel explaining the empirical $O(V + E)$ vs $O(m^V)$ performance tradeoffs.

---

## 3. Plan Decomposition (2 Waves)

- **Plan 03-01**: SQLite Database Layer & CRUD Data Management View
  - `DatabaseManager.java` (schema init, CRUD methods, seed from `SampleData`).
  - `DataManagementView.java` (Subjects table, Students table, Enrollment editor, Add/Edit/Delete dialogs).
  - Headless unit tests verifying SQLite database CRUD and cascade delete behavior.

- **Plan 03-02**: Benchmark View & Application Integration
  - `BenchmarkService.java` (runs algorithms on datasets, records nano-timing and metrics).
  - `BenchmarkView.java` (side-by-side comparison UI, scenario selector, viva tradeoff analysis).
  - App root `TabPane` integration connecting `MainView`, `BenchmarkView`, and `DataManagementView` with live data synchronization.
  - Headless unit tests for benchmark metric accuracy and database integration.

---

## 4. Verification Criteria
- `mvn clean test` passes with 100% success across all model, algorithm, database, and UI geometry tests.
- Launching the app initializes SQLite (`examguard.db`) and seeds `SampleData` if empty.
- User can add a new subject/student, assign enrollments, and immediately observe the updated graph in Tab 1 and Tab 2.
- Benchmarking tab executes both algorithms, displaying side-by-side time, step, and timeslot metrics.
