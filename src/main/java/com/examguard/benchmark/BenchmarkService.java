package com.examguard.benchmark;

import com.examguard.algorithm.BacktrackingAlgorithm;
import com.examguard.algorithm.ColoringResult;
import com.examguard.algorithm.WelshPowellAlgorithm;
import com.examguard.model.ConflictGraph;
import com.examguard.model.Schedule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service that runs both Welsh-Powell Greedy and Recursive Backtracking algorithms
 * on the exact same ConflictGraph instance to produce strictly comparable empirical metrics.
 */
public class BenchmarkService {

    public static final String ALGO_WELSH_POWELL = "Welsh-Powell Greedy";
    public static final String ALGO_BACKTRACKING = "Recursive Backtracking";

    private final WelshPowellAlgorithm welshPowell = new WelshPowellAlgorithm();
    private final BacktrackingAlgorithm backtracking = new BacktrackingAlgorithm();

    /**
     * Executes both algorithms on the provided graph and records timing, step counts,
     * timeslots required, and optimality guarantees.
     */
    public Map<String, BenchmarkMetric> runComparison(ConflictGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null for benchmarking");
        }

        Map<String, BenchmarkMetric> results = new LinkedHashMap<>();

        // 1. Run Welsh-Powell Greedy
        long startWp = System.nanoTime();
        ColoringResult wpResult = welshPowell.color(graph);
        long endWp = System.nanoTime();
        long wpDuration = endWp - startWp;

        Schedule wpSchedule = wpResult.toSchedule(graph);
        boolean wpValid = wpResult.isSuccess() && wpSchedule.isValid(graph);
        BenchmarkMetric wpMetric = new BenchmarkMetric(
                ALGO_WELSH_POWELL,
                wpDuration,
                wpResult.getTrace().getStepCount(),
                wpResult.getColorsUsed(),
                wpValid,
                BenchmarkMetric.OutcomeStatus.HEURISTIC_RESULT,
                wpResult.getMessage()
        );
        results.put(ALGO_WELSH_POWELL, wpMetric);

        // 2. Run Recursive Backtracking on the exact same graph
        long startBt = System.nanoTime();
        ColoringResult btResult = backtracking.color(graph);
        long endBt = System.nanoTime();
        long btDuration = endBt - startBt;

        Schedule btSchedule = btResult.toSchedule(graph);
        boolean btValid = btResult.isSuccess() && btSchedule.isValid(graph);

        BenchmarkMetric.OutcomeStatus btStatus;
        if (!btResult.isSuccess() || btResult.getMessage().toLowerCase().contains("aborted") || btResult.getMessage().toLowerCase().contains("limit")) {
            btStatus = BenchmarkMetric.OutcomeStatus.INTERRUPTED_SEARCH;
        } else {
            btStatus = BenchmarkMetric.OutcomeStatus.PROVEN_MINIMUM;
        }

        BenchmarkMetric btMetric = new BenchmarkMetric(
                ALGO_BACKTRACKING,
                btDuration,
                btResult.getTrace().getStepCount(),
                btResult.getColorsUsed(),
                btValid,
                btStatus,
                btResult.getMessage()
        );
        results.put(ALGO_BACKTRACKING, btMetric);

        return results;
    }
}
