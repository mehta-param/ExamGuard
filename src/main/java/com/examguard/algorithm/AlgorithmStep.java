package com.examguard.algorithm;

/**
 * Immutable snapshot of a single discrete algorithm event.
 * Stores vertex, timeslot, conflict context, and educational explanation text.
 */
public class AlgorithmStep {
    private final int stepNumber;
    private final StepType type;
    private final int vertexIndex;
    private final int color;
    private final int conflictingNeighborIndex;
    private final String explanation;

    public AlgorithmStep(int stepNumber, StepType type, int vertexIndex, int color, 
                         int conflictingNeighborIndex, String explanation) {
        this.stepNumber = stepNumber;
        this.type = type;
        this.vertexIndex = vertexIndex;
        this.color = color;
        this.conflictingNeighborIndex = conflictingNeighborIndex;
        this.explanation = explanation != null ? explanation : "";
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public StepType getType() {
        return type;
    }

    public int getVertexIndex() {
        return vertexIndex;
    }

    public int getColor() {
        return color;
    }

    public int getConflictingNeighborIndex() {
        return conflictingNeighborIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return String.format("#%03d [%-17s] Vertex %d, Color %d%s -> %s",
            stepNumber, type, vertexIndex, color,
            (conflictingNeighborIndex >= 0 ? " (Clash with V" + conflictingNeighborIndex + ")" : ""),
            explanation);
    }
}
