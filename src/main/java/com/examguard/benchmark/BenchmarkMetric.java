package com.examguard.benchmark;

/**
 * Immutable metrics data class capturing performance and correctness
 * results for an algorithm execution on a ConflictGraph.
 */
public class BenchmarkMetric {

    public enum OutcomeStatus {
        PROVEN_MINIMUM("Proven Minimum (Optimal \u03C7(G))"),
        HEURISTIC_RESULT("Heuristic Upper Bound"),
        INTERRUPTED_SEARCH("Interrupted Search (Safety Cap Hit)");

        private final String displayLabel;

        OutcomeStatus(String displayLabel) {
            this.displayLabel = displayLabel;
        }

        public String getDisplayLabel() {
            return displayLabel;
        }
    }

    private final String algorithmName;
    private final long durationNanos;
    private final int totalSteps;
    private final int timeslotsUsed;
    private final boolean conflictFree;
    private final OutcomeStatus status;
    private final String message;

    public BenchmarkMetric(String algorithmName, long durationNanos, int totalSteps,
                           int timeslotsUsed, boolean conflictFree, OutcomeStatus status, String message) {
        this.algorithmName = algorithmName;
        this.durationNanos = durationNanos;
        this.totalSteps = totalSteps;
        this.timeslotsUsed = timeslotsUsed;
        this.conflictFree = conflictFree;
        this.status = status;
        this.message = message;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public double getDurationMillis() {
        return durationNanos / 1_000_000.0;
    }

    public double getDurationMicros() {
        return durationNanos / 1_000.0;
    }

    public String getFormattedTime() {
        if (durationNanos < 1_000_000) {
            return String.format("%.1f \u00B5s", getDurationMicros());
        }
        return String.format("%.2f ms (%.0f \u00B5s)", getDurationMillis(), getDurationMicros());
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getTimeslotsUsed() {
        return timeslotsUsed;
    }

    public boolean isConflictFree() {
        return conflictFree;
    }

    public OutcomeStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
