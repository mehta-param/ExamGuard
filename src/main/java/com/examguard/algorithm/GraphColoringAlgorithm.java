package com.examguard.algorithm;

import com.examguard.model.ConflictGraph;

/**
 * Standard interface for all graph coloring algorithms in ExamGuard.
 * Keeps algorithm implementations decoupled from the UI and persistence layers.
 */
public interface GraphColoringAlgorithm {

    /**
     * Solves the graph coloring problem minimizing the total number of colors (timeslots).
     * @param graph The ConflictGraph to color.
     * @return ColoringResult containing vertex assignments and execution trace.
     */
    ColoringResult color(ConflictGraph graph);

    /**
     * Solves the decision problem: Can the graph be colored using at most maxColors (K timeslots)?
     * @param graph The ConflictGraph to color.
     * @param maxColors The maximum number of allowed colors (timeslots).
     * @return ColoringResult with success=true if feasible, success=false if infeasible.
     */
    ColoringResult colorWithLimit(ConflictGraph graph, int maxColors);

    /**
     * Returns the human-readable name of the algorithm (e.g., "Welsh-Powell Greedy").
     */
    String getName();

    /**
     * Returns a brief explanation of the algorithm's strategy for viva defense.
     */
    String getDescription();
}
