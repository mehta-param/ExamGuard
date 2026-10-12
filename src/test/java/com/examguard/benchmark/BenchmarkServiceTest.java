package com.examguard.benchmark;

import com.examguard.model.ConflictGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Empirical Algorithm Benchmark Service Tests")
class BenchmarkServiceTest {

    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        benchmarkService = new BenchmarkService();
    }

    @Test
    @DisplayName("Benchmarking on Small scenario executes both algorithms on exact same graph with 0 conflicts")
    void testSmallScenarioBenchmark() {
        ConflictGraph graph = BenchmarkPresets.getSmallScenario();
        Map<String, BenchmarkMetric> results = benchmarkService.runComparison(graph);

        assertEquals(2, results.size());
        assertTrue(results.containsKey(BenchmarkService.ALGO_WELSH_POWELL));
        assertTrue(results.containsKey(BenchmarkService.ALGO_BACKTRACKING));

        BenchmarkMetric wp = results.get(BenchmarkService.ALGO_WELSH_POWELL);
        BenchmarkMetric bt = results.get(BenchmarkService.ALGO_BACKTRACKING);

        assertTrue(wp.isConflictFree(), "Welsh-Powell must be conflict-free");
        assertTrue(bt.isConflictFree(), "Backtracking must be conflict-free");

        assertEquals(3, wp.getTimeslotsUsed(), "Welsh-Powell uses 3 timeslots on small scenario");
        assertEquals(3, bt.getTimeslotsUsed(), "Backtracking finds exact optimal chromatic number 3");

        assertEquals(BenchmarkMetric.OutcomeStatus.HEURISTIC_RESULT, wp.getStatus());
        assertEquals(BenchmarkMetric.OutcomeStatus.PROVEN_MINIMUM, bt.getStatus());

        assertTrue(wp.getDurationNanos() > 0);
        assertTrue(bt.getDurationNanos() > 0);
        assertTrue(wp.getTotalSteps() > 0);
        assertTrue(bt.getTotalSteps() > 0);
    }

    @Test
    @DisplayName("Benchmarking on Medium department scenario guarantees Backtracking slots <= Welsh-Powell slots")
    void testMediumScenarioBenchmark() {
        ConflictGraph graph = BenchmarkPresets.getMediumScenario();
        Map<String, BenchmarkMetric> results = benchmarkService.runComparison(graph);

        BenchmarkMetric wp = results.get(BenchmarkService.ALGO_WELSH_POWELL);
        BenchmarkMetric bt = results.get(BenchmarkService.ALGO_BACKTRACKING);

        assertTrue(wp.isConflictFree());
        assertTrue(bt.isConflictFree());
        assertTrue(bt.getTimeslotsUsed() <= wp.getTimeslotsUsed(), 
                "Backtracking optimal timeslots must be <= Welsh-Powell heuristic upper bound");
    }

    @Test
    @DisplayName("Benchmarking on Dense / Clash-heavy scenario validates 0 conflicts and status guarantees")
    void testDenseScenarioBenchmark() {
        ConflictGraph graph = BenchmarkPresets.getDenseScenario();
        Map<String, BenchmarkMetric> results = benchmarkService.runComparison(graph);

        BenchmarkMetric wp = results.get(BenchmarkService.ALGO_WELSH_POWELL);
        BenchmarkMetric bt = results.get(BenchmarkService.ALGO_BACKTRACKING);

        assertTrue(wp.isConflictFree());
        assertTrue(bt.isConflictFree());
        assertEquals(BenchmarkMetric.OutcomeStatus.HEURISTIC_RESULT, wp.getStatus());
        assertEquals(BenchmarkMetric.OutcomeStatus.PROVEN_MINIMUM, bt.getStatus());
    }

    @Test
    @DisplayName("Throws IllegalArgumentException if graph is null")
    void testNullGraphThrows() {
        assertThrows(IllegalArgumentException.class, () -> benchmarkService.runComparison(null));
    }
}
