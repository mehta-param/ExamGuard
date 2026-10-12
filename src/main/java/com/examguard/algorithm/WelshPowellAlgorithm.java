package com.examguard.algorithm;

import com.examguard.model.ConflictEdge;
import com.examguard.model.ConflictGraph;
import com.examguard.model.Student;
import com.examguard.model.Subject;

import java.util.*;

/**
 * Welsh-Powell Greedy Graph Coloring Algorithm.
 * 
 * Algorithm Strategy (DAA):
 * 1. Calculate degrees for all vertices in the ConflictGraph.
 * 2. Sort vertices in descending order of their degree: deg(v1) >= deg(v2) >= ... >= deg(vn).
 * 3. Color 1 is assigned to the first vertex in the sorted list.
 * 4. Scan down the sorted list: assign Color 1 to any vertex that is not adjacent to
 *    any vertex already assigned Color 1.
 * 5. Repeat with Color 2, Color 3, etc., until all vertices are colored.
 * 
 * Time Complexity: O(V log V + V^2) or O(V log V + E).
 * Provides an effective polynomial heuristic for timetable scheduling.
 */
public class WelshPowellAlgorithm implements GraphColoringAlgorithm {

    @Override
    public String getName() {
        return "Welsh-Powell Greedy";
    }

    @Override
    public String getDescription() {
        return "Orders subjects by degree descending and greedily assigns timeslots to independent sets.";
    }

    @Override
    public ColoringResult color(ConflictGraph graph) {
        return execute(graph, Integer.MAX_VALUE);
    }

    @Override
    public ColoringResult colorWithLimit(ConflictGraph graph, int maxColors) {
        return execute(graph, maxColors);
    }

    private ColoringResult execute(ConflictGraph graph, int maxColors) {
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

        // Step 1: Create vertex order sorted by degree descending
        List<Integer> sortedVertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            sortedVertices.add(i);
        }

        // Sort descending by degree. Break ties by index for deterministic behavior
        sortedVertices.sort((a, b) -> {
            int degDiff = Integer.compare(graph.getDegree(b), graph.getDegree(a));
            if (degDiff != 0) return degDiff;
            return Integer.compare(a, b);
        });

        // Track color assigned to each vertex (0 = uncolored, 1, 2, ... = timeslot)
        int[] colorAssignments = new int[n];
        int coloredCount = 0;
        int currentColor = 1;

        // Step 2: Iteratively assign colors to independent sets in sorted order
        while (coloredCount < n) {
            // Check if we exceeded user's maxColors limit
            if (currentColor > maxColors) {
                trace.record(StepType.FINISH, -1, currentColor, -1, 
                    "Exceeded maximum allowed timeslots (" + maxColors + "). Graph requires more slots.");
                long duration = System.nanoTime() - startTime;
                return new ColoringResult(false, toMap(colorAssignments), currentColor - 1, 
                    duration, trace.getStepCount(), trace, "Infeasible: Exceeded slot limit " + maxColors);
            }

            // Vertices assigned the current color during this pass
            List<Integer> currentSlotVertices = new ArrayList<>();

            for (int v : sortedVertices) {
                if (colorAssignments[v] != 0) {
                    continue; // Already colored in a previous pass
                }

                Subject subjV = graph.getSubject(v);
                trace.record(StepType.SELECT_VERTEX, v, currentColor, -1, 
                    "Considering Subject " + subjV.getCode() + " (degree " + graph.getDegree(v) + ") for Timeslot " + currentColor);

                trace.record(StepType.TRY_COLOR, v, currentColor, -1, 
                    "Testing Timeslot " + currentColor + " on " + subjV.getCode());

                // Check if vertex v is adjacent to any vertex already colored with currentColor
                boolean hasClash = false;
                int clashingNeighbor = -1;
                String clashExplanation = "";

                for (int coloredV : currentSlotVertices) {
                    if (graph.hasConflict(v, coloredV)) {
                        hasClash = true;
                        clashingNeighbor = coloredV;
                        Subject subjOther = graph.getSubject(coloredV);
                        ConflictEdge edge = graph.getEdge(v, coloredV);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Clash with ").append(subjOther.getCode());
                        if (edge != null && !edge.getConflictingStudents().isEmpty()) {
                            Student firstStudent = edge.getConflictingStudents().iterator().next();
                            sb.append(" (e.g. Student ").append(firstStudent.getName())
                              .append(" [").append(firstStudent.getStudentId()).append("] enrolled in both)");
                        }
                        clashExplanation = sb.toString();
                        break;
                    }
                }

                if (hasClash) {
                    trace.record(StepType.CONFLICT_DETECTED, v, currentColor, clashingNeighbor, clashExplanation);
                } else {
                    // Safe to assign
                    colorAssignments[v] = currentColor;
                    currentSlotVertices.add(v);
                    coloredCount++;

                    trace.record(StepType.ASSIGN_COLOR, v, currentColor, -1, 
                        "Assigned Timeslot " + currentColor + " to " + subjV.getCode() + " (no shared students in slot)");
                }
            }

            currentColor++;
        }

        int totalColorsUsed = currentColor - 1;
        long duration = System.nanoTime() - startTime;

        trace.record(StepType.FINISH, -1, totalColorsUsed, -1, 
            "Welsh-Powell completed successfully! Total timeslots required: " + totalColorsUsed);

        return new ColoringResult(
            true,
            toMap(colorAssignments),
            totalColorsUsed,
            duration,
            trace.getStepCount(),
            trace,
            "Success: " + totalColorsUsed + " timeslots used"
        );
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
