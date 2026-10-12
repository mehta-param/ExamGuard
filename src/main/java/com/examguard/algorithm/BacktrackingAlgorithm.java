package com.examguard.algorithm;

import com.examguard.model.ConflictEdge;
import com.examguard.model.ConflictGraph;
import com.examguard.model.Student;
import com.examguard.model.Subject;

import java.util.*;

/**
 * Recursive Backtracking Graph Coloring Algorithm.
 * 
 * Algorithm Strategy (DAA):
 * Solves the exact K-Coloring decision problem and finds the exact Chromatic Number chi(G).
 * 
 * 1. Recursive exploration: At vertex index `v`, test candidate colors c in [1 .. k].
 * 2. Pruning / Safety Check: `isSafe(v, c)` ensures no adjacent neighbor has color `c`.
 * 3. State-Space Traversal: If safe, assign color `c` and recurse to `v + 1`.
 * 4. Backtrack: If subsequent subtrees fail to find a valid coloring, reset assignment
 *    to 0, record a BACKTRACK event, and try the next color.
 * 5. Minimum Chromatic Number: Tests k = 1, 2, ... up to Welsh-Powell upper bound.
 *    The first successful k is the optimal chromatic number chi(G).
 * 
 * Time Complexity: Worst-case O(k^V), exponential in vertex count.
 * Includes a configurable safety step limit (default 50,000) to prevent UI/test freezes.
 */
public class BacktrackingAlgorithm implements GraphColoringAlgorithm {

    public static final int DEFAULT_MAX_STEPS = 50_000;
    private final int maxSteps;

    public BacktrackingAlgorithm() {
        this(DEFAULT_MAX_STEPS);
    }

    public BacktrackingAlgorithm(int maxSteps) {
        this.maxSteps = maxSteps > 0 ? maxSteps : DEFAULT_MAX_STEPS;
    }

    @Override
    public String getName() {
        return "Recursive Backtracking";
    }

    @Override
    public String getDescription() {
        return "Exhaustively explores valid timeslots using recursion with backtracking to find the optimal chromatic number.";
    }

    @Override
    public ColoringResult color(ConflictGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }

        long startTime = System.nanoTime();
        ExecutionTrace trace = new ExecutionTrace();
        int n = graph.getVertexCount();

        if (n == 0) {
            trace.record(StepType.FINISH, -1, 0, -1, "Empty graph; 0 timeslots needed.");
            return new ColoringResult(true, Collections.emptyMap(), 0, System.nanoTime() - startTime, 1, trace, "Success (0 vertices)");
        }

        // Edgeless graph requires exactly 1 timeslot
        if (graph.getEdgeCount() == 0) {
            int[] assignments = new int[n];
            Arrays.fill(assignments, 1);
            for (int i = 0; i < n; i++) {
                trace.record(StepType.ASSIGN_COLOR, i, 1, -1, "Edgeless graph: Subject " + graph.getSubject(i).getCode() + " assigned Slot 1");
            }
            trace.record(StepType.FINISH, -1, 1, -1, "All subjects can share Timeslot 1 (no conflicts)");
            return new ColoringResult(true, toMap(assignments), 1, System.nanoTime() - startTime, trace.getStepCount(), trace, "Success: 1 timeslot");
        }

        // Get an upper bound from Welsh-Powell heuristic to bound our search
        WelshPowellAlgorithm wp = new WelshPowellAlgorithm();
        ColoringResult wpResult = wp.color(graph);
        int upperBound = Math.max(1, wpResult.getColorsUsed());

        // Exact chromatic number search: Test k = 1, 2, ... upperBound
        int[] stepCounter = new int[]{0};
        boolean[] abortedDueToLimit = new boolean[]{false};

        for (int k = 1; k <= upperBound; k++) {
            int[] colorAssignments = new int[n];
            trace.record(StepType.SELECT_VERTEX, -1, k, -1, 
                "--- Starting chromatic search pass for K = " + k + " timeslot(s) ---");

            boolean solved = solveKColoring(graph, 0, k, colorAssignments, trace, stepCounter, abortedDueToLimit);

            if (abortedDueToLimit[0]) {
                long duration = System.nanoTime() - startTime;
                trace.record(StepType.FINISH, -1, 0, -1, "Backtracking exceeded safety step limit (" + maxSteps + " steps). Aborted.");
                return new ColoringResult(false, Collections.emptyMap(), 0, duration, stepCounter[0], trace, "Aborted: Safety step limit exceeded");
            }

            if (solved) {
                long duration = System.nanoTime() - startTime;
                trace.record(StepType.FINISH, -1, k, -1, 
                    "Optimal Chromatic Number chi(G) found: " + k + " timeslots!");
                return new ColoringResult(true, toMap(colorAssignments), k, duration, stepCounter[0], trace, "Optimal: " + k + " timeslots");
            }
        }

        // Fallback to Welsh-Powell result if exact search exhausted
        long duration = System.nanoTime() - startTime;
        return new ColoringResult(true, wpResult.getAssignments(), upperBound, duration, stepCounter[0], trace, "Solved: " + upperBound + " timeslots");
    }

    @Override
    public ColoringResult colorWithLimit(ConflictGraph graph, int maxColors) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (maxColors <= 0) {
            return new ColoringResult(false, Collections.emptyMap(), 0, 0, 0, new ExecutionTrace(), "Infeasible: maxColors must be >= 1");
        }

        long startTime = System.nanoTime();
        ExecutionTrace trace = new ExecutionTrace();
        int n = graph.getVertexCount();

        if (n == 0) {
            trace.record(StepType.FINISH, -1, 0, -1, "Empty graph; 0 timeslots needed.");
            return new ColoringResult(true, Collections.emptyMap(), 0, System.nanoTime() - startTime, 1, trace, "Success (0 vertices)");
        }

        int[] colorAssignments = new int[n];
        int[] stepCounter = new int[]{0};
        boolean[] abortedDueToLimit = new boolean[]{false};

        trace.record(StepType.SELECT_VERTEX, -1, maxColors, -1, 
            "Checking feasibility of scheduling graph in " + maxColors + " timeslots using Backtracking");

        boolean solved = solveKColoring(graph, 0, maxColors, colorAssignments, trace, stepCounter, abortedDueToLimit);
        long duration = System.nanoTime() - startTime;

        if (abortedDueToLimit[0]) {
            trace.record(StepType.FINISH, -1, 0, -1, "Safety step limit of " + maxSteps + " exceeded.");
            return new ColoringResult(false, Collections.emptyMap(), 0, duration, stepCounter[0], trace, "Aborted: Step limit exceeded");
        }

        if (solved) {
            int actualColors = Arrays.stream(colorAssignments).max().orElse(0);
            trace.record(StepType.FINISH, -1, actualColors, -1, 
                "Successfully found valid timetable in " + actualColors + " timeslots (<= " + maxColors + ")!");
            return new ColoringResult(true, toMap(colorAssignments), actualColors, duration, stepCounter[0], trace, "Feasible: " + actualColors + " slots used");
        } else {
            trace.record(StepType.FINISH, -1, maxColors, -1, 
                "Graph is NOT " + maxColors + "-colorable. Minimum timeslots required exceeds " + maxColors + ".");
            return new ColoringResult(false, Collections.emptyMap(), 0, duration, stepCounter[0], trace, "Infeasible: Cannot color within " + maxColors + " slots");
        }
    }

    /**
     * Recursive backtracking worker to test k-coloring feasibility.
     */
    private boolean solveKColoring(ConflictGraph graph, int vertexIndex, int k, 
                                  int[] colorAssignments, ExecutionTrace trace, 
                                  int[] stepCounter, boolean[] aborted) {
        // Base case: all vertices colored
        if (vertexIndex == graph.getVertexCount()) {
            return true;
        }

        // Safety limit check
        if (++stepCounter[0] > maxSteps) {
            aborted[0] = true;
            return false;
        }

        Subject subj = graph.getSubject(vertexIndex);
        trace.record(StepType.SELECT_VERTEX, vertexIndex, 0, -1, 
            "Inspecting Subject " + subj.getCode() + " (V" + vertexIndex + ")");

        // Try candidate colors 1 through k
        for (int c = 1; c <= k; c++) {
            trace.record(StepType.TRY_COLOR, vertexIndex, c, -1, 
                "Trying Timeslot " + c + " for " + subj.getCode());

            // Check safety
            int conflictingNeighbor = findConflictingNeighbor(graph, vertexIndex, c, colorAssignments);

            if (conflictingNeighbor >= 0) {
                // Conflict found!
                Subject clashingSubj = graph.getSubject(conflictingNeighbor);
                ConflictEdge edge = graph.getEdge(vertexIndex, conflictingNeighbor);

                String clashDetail = "Clash with " + clashingSubj.getCode() + " already assigned Timeslot " + c;
                if (edge != null && !edge.getConflictingStudents().isEmpty()) {
                    Student s = edge.getConflictingStudents().iterator().next();
                    clashDetail += " (shared student: " + s.getName() + ")";
                }

                trace.record(StepType.CONFLICT_DETECTED, vertexIndex, c, conflictingNeighbor, clashDetail);
            } else {
                // Color c is safe
                colorAssignments[vertexIndex] = c;
                trace.record(StepType.ASSIGN_COLOR, vertexIndex, c, -1, 
                    "Assigned Timeslot " + c + " to " + subj.getCode());

                // Recurse to next vertex
                if (solveKColoring(graph, vertexIndex + 1, k, colorAssignments, trace, stepCounter, aborted)) {
                    return true;
                }

                if (aborted[0]) {
                    return false;
                }

                // Backtrack! Undo assignment
                colorAssignments[vertexIndex] = 0;
                trace.record(StepType.BACKTRACK, vertexIndex, c, -1, 
                    "Backtracking: Resetting " + subj.getCode() + " (removed Timeslot " + c + ")");
            }
        }

        // No color worked for this vertex in current branch
        return false;
    }

    /**
     * Checks if color c is already assigned to any neighbor of vertex.
     * @return index of conflicting neighbor, or -1 if safe.
     */
    private int findConflictingNeighbor(ConflictGraph graph, int vertexIndex, int color, int[] assignments) {
        for (int neighbor : graph.getNeighbors(vertexIndex)) {
            if (assignments[neighbor] == color) {
                return neighbor;
            }
        }
        return -1;
    }

    private Map<Integer, Integer> toMap(int[] assignments) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] > 0) {
                map.put(i, assignments[i]);
            }
        }
        return map;
    }
}
