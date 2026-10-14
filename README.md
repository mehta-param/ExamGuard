# ExamGuard — Exam Timetable Conflict Minimizer

An interactive Java desktop application that uses **graph coloring algorithms** to generate conflict-free exam timetables, visualize the algorithms step by step, and compare their performance.

ExamGuard was developed as a solo academic project for **Design and Analysis of Algorithms (DAA)**. Its goal is to make graph coloring understandable through a practical, visual demonstration.

## Overview

Scheduling examinations becomes difficult when students enroll in multiple subjects. Two subjects cannot have their exams at the same time if at least one student is enrolled in both.

ExamGuard models this problem as an **undirected conflict graph**:

- **Vertices:** Subjects that need to be scheduled.
- **Edges:** Pairs of subjects that share at least one student.
- **Colors:** Exam timeslots.
- **Objective:** Assign colors so that no two adjacent vertices have the same color, while using as few timeslots as possible.

The application compares a fast greedy heuristic with an exact graph-coloring search and visualizes their behavior.

## Features

- **Interactive conflict graph:** Visualizes subjects as vertices and scheduling conflicts as edges.
- **Welsh–Powell Greedy algorithm:** Assigns colors using a degree-based vertex ordering.
- **Recursive Backtracking algorithm:** Searches for a minimum coloring and can test whether a graph is colorable using a fixed number of colors.
- **Step-by-step animation:** Shows the algorithms' progress on a custom JavaFX Canvas, with playback controls.
- **Timetable visualization:** Displays the generated exam slots and assigned subjects.
- **Subject filtering:** Allows a subset of subjects to be selected for analysis.
- **Data management:** Add, edit, and delete subjects and students, and manage student-subject enrollments.
- **SQLite persistence:** Stores subjects, students, and enrollments locally across application restarts.
- **Algorithm benchmarking:** Compares Greedy and Backtracking using execution time, execution steps, and slots used.
- **Input validation:** Handles invalid entries and common database constraint errors.
- **Automated tests:** Tests graph construction, algorithms, database behavior, benchmarking, and selected UI logic.

## Algorithms

### 1. Welsh–Powell Greedy Coloring

Welsh–Powell orders vertices by descending degree and assigns each vertex the first available color that does not conflict with its already-colored neighbors.

**Advantages**
- Efficient for larger graphs.
- Straightforward to understand and visualize.
- Produces a valid coloring when implemented correctly.

**Limitation**

It is a heuristic. It does not guarantee the minimum possible number of colors for every graph.

### 2. Recursive Backtracking Coloring

Backtracking explores possible color assignments recursively, rejecting assignments that violate graph-coloring constraints.

ExamGuard tests increasing numbers of colors and searches for the first feasible coloring, up to the Greedy solution's upper bound.

**Advantages**
- Can establish the minimum number of colors when the search completes successfully.
- Demonstrates recursive search and constraint checking.

**Limitations**
- The search can become expensive as graph size and complexity increase.
- ExamGuard applies a safety step limit. If the search is interrupted before finding a solution, it must not be interpreted as a proof of optimality.

### Complexity comparison

Let \(V\) be the number of subjects (vertices) and \(E\) the number of conflict edges.

- **Welsh–Powell:** Commonly implemented with \(O(V^2)\) coloring work after vertex ordering, plus the cost of sorting.
- **Backtracking:** Has exponential worst-case search complexity. A loose upper bound for trying up to \(V\) colors is \(O(V^V)\), excluding implementation-specific overhead.

Actual execution time depends on graph structure, vertex ordering, and the search path.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Application logic and algorithms |
| JavaFX 21 | Desktop interface, Canvas visualization, and animation |
| Maven | Dependency management, build, testing, and application launch |
| SQLite | Local relational database |
| SQLite JDBC | Java-to-SQLite connectivity |
| JUnit 5 | Automated testing |

## Project Structure

```text
ExamGuard/
├── src/
│   ├── main/java/com/examguard/
│   │   ├── App.java
│   │   ├── algorithm/     # Graph-coloring algorithms and traces
│   │   ├── benchmark/     # Benchmarking logic and presets
│   │   ├── data/          # Sample data
│   │   ├── db/             # SQLite persistence and CRUD
│   │   ├── model/          # Graph, subject, student, and schedule models
│   │   └── ui/             # JavaFX views, Canvas, and animation
│   └── test/java/com/examguard/
│       ├── algorithm/
│       ├── benchmark/
│       ├── data/
│       ├── db/
│       ├── model/
│       └── ui/
├── .planning/              # Project planning and phase documentation
├── .gitignore
└── pom.xml
```

## Prerequisites

Install the following:

- **JDK 17 or compatible newer JDK**
- **Apache Maven**
- A desktop environment supported by JavaFX

Verify your Java and Maven installations:

```bash
java -version
mvn -version
```

## Run the Application

Clone the repository and enter the project directory:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd ExamGuard
```

Build and test the project:

```bash
mvn clean test
```

Launch the desktop application:

```bash
mvn javafx:run
```

Maven downloads the required dependencies on the first run. A graphical desktop session is required to display the JavaFX application.

## Database and Data Persistence

ExamGuard uses a local SQLite database named `examguard.db`.

- The database is created and initialized by the application as needed.
- Sample data is seeded when the database has no subjects.
- Subject, student, and enrollment changes are persisted locally.
- SQLite foreign-key constraints and cascading deletion help maintain enrollment integrity.

The local database file is excluded from Git. Each installation can create its own database when the application runs.

## Testing

Run the automated test suite:

```bash
mvn clean test
```

The tests cover areas including:

- Conflict graph construction and graph properties.
- Welsh–Powell and Backtracking coloring behavior.
- Known graph-coloring results.
- Backtracking safety-limit handling.
- SQLite persistence, CRUD operations, and enrollment deletion.
- Benchmark service behavior.
- Selected graph visualization and subject-filtering logic.

**Latest reported verification:** 40 tests passed, with zero failures or errors. This result reflects the development environment's recorded test run; rerun the command to verify the current checkout.

## Scope and Limitations

ExamGuard focuses on graph-coloring-based exam scheduling.

It does not currently handle:
- Room allocation or room capacity.
- Faculty or invigilator scheduling.
- Cloud synchronization or multi-user access.
- CSV import/export.

The Backtracking algorithm can be interrupted by its safety limit on difficult inputs. An interrupted search does not establish that the best possible timetable has been found.

## Learning Outcomes

This project demonstrates practical applications of:

- Graph modeling and adjacency relationships.
- Greedy algorithms and heuristic solutions.
- Recursive Backtracking and constraint satisfaction.
- Time-complexity analysis and empirical benchmarking.
- JavaFX visualization and event-driven programming.
- Relational data modeling and SQLite persistence.
- Unit testing and software quality assurance.

## Future Improvements

Possible extensions include:
- Exporting timetables to CSV or PDF.
- Adding more graph benchmarks and larger test datasets.
- Improving visualization and accessibility.
- Exploring additional graph-coloring heuristics.

## License

No license has been selected yet. If you intend to make this project open source, choose and add an appropriate license before describing the repository as open source.
