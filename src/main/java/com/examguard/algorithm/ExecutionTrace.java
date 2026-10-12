package com.examguard.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete historical sequence of AlgorithmSteps recorded
 * during graph coloring. Enables step-by-step UI replay, scrubbing, and metrics inspection.
 */
public class ExecutionTrace {
    private final List<AlgorithmStep> steps;

    public ExecutionTrace() {
        this.steps = new ArrayList<>();
    }

    public ExecutionTrace(List<AlgorithmStep> steps) {
        this.steps = new ArrayList<>(steps);
    }

    public synchronized void addStep(AlgorithmStep step) {
        if (step != null) {
            steps.add(step);
        }
    }

    public synchronized void record(StepType type, int vertexIndex, int color, 
                                   int conflictingNeighborIndex, String explanation) {
        int nextNum = steps.size() + 1;
        steps.add(new AlgorithmStep(nextNum, type, vertexIndex, color, conflictingNeighborIndex, explanation));
    }

    public int getStepCount() {
        return steps.size();
    }

    public AlgorithmStep getStep(int index) {
        if (index < 0 || index >= steps.size()) {
            throw new IndexOutOfBoundsException("Step index " + index + " out of bounds for trace size " + steps.size());
        }
        return steps.get(index);
    }

    public List<AlgorithmStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int getBacktrackCount() {
        int count = 0;
        for (AlgorithmStep step : steps) {
            if (step.getType() == StepType.BACKTRACK) {
                count++;
            }
        }
        return count;
    }

    public void clear() {
        steps.clear();
    }
}
