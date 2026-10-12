package com.examguard.algorithm;

import com.examguard.model.ConflictGraph;
import com.examguard.model.Schedule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result object returned by any GraphColoringAlgorithm.
 * Contains success status, vertex assignments, chromatic count, execution time,
 * and the complete ExecutionTrace for animation and benchmarking.
 */
public class ColoringResult {
    private final boolean success;
    private final Map<Integer, Integer> assignments;
    private final int colorsUsed;
    private final long executionTimeNanos;
    private final int totalSteps;
    private final ExecutionTrace trace;
    private final String message;

    public ColoringResult(boolean success, Map<Integer, Integer> assignments, int colorsUsed, 
                          long executionTimeNanos, int totalSteps, ExecutionTrace trace, String message) {
        this.success = success;
        this.assignments = assignments != null ? new LinkedHashMap<>(assignments) : Collections.emptyMap();
        this.colorsUsed = colorsUsed;
        this.executionTimeNanos = executionTimeNanos;
        this.totalSteps = totalSteps;
        this.trace = trace != null ? trace : new ExecutionTrace();
        this.message = message != null ? message : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<Integer, Integer> getAssignments() {
        return Collections.unmodifiableMap(assignments);
    }

    public int getColor(int vertexIndex) {
        return assignments.getOrDefault(vertexIndex, 0);
    }

    public int getColorsUsed() {
        return colorsUsed;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public double getExecutionTimeMillis() {
        return executionTimeNanos / 1_000_000.0;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public ExecutionTrace getTrace() {
        return trace;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Converts vertex-index assignments to a concrete domain Schedule.
     */
    public Schedule toSchedule(ConflictGraph graph) {
        Schedule schedule = new Schedule();
        if (graph == null) return schedule;
        for (Map.Entry<Integer, Integer> entry : assignments.entrySet()) {
            schedule.assign(graph.getSubject(entry.getKey()), entry.getValue());
        }
        return schedule;
    }

    @Override
    public String toString() {
        return String.format("ColoringResult{success=%s, colorsUsed=%d, steps=%d, time=%.3f ms, msg='%s'}",
            success, colorsUsed, totalSteps, getExecutionTimeMillis(), message);
    }
}
