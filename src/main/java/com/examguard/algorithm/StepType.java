package com.examguard.algorithm;

/**
 * Enumerates the discrete algorithmic events recorded during graph coloring.
 * These step types are consumed by the UI animator in Phase 2 to drive visual feedback.
 */
public enum StepType {
    /**
     * The algorithm shifts focus to a specific vertex for inspection.
     */
    SELECT_VERTEX,

    /**
     * The algorithm attempts to test a candidate timeslot (color) on the vertex.
     */
    TRY_COLOR,

    /**
     * A clash is detected between the current vertex and an adjacent neighbor with the same color.
     */
    CONFLICT_DETECTED,

    /**
     * The candidate color is verified safe and assigned to the vertex.
     */
    ASSIGN_COLOR,

    /**
     * All colors in the available range caused conflicts; the algorithm retreats to the previous vertex.
     */
    BACKTRACK,

    /**
     * The algorithm has completed execution (either succeeded or reached failure state).
     */
    FINISH
}
