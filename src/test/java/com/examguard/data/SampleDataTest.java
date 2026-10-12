package com.examguard.data;

import com.examguard.algorithm.BacktrackingAlgorithm;
import com.examguard.algorithm.ColoringResult;
import com.examguard.algorithm.WelshPowellAlgorithm;
import com.examguard.model.ConflictGraph;
import com.examguard.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SampleDataTest {

    private SampleData sampleData;

    @BeforeEach
    void setUp() {
        sampleData = new SampleData();
    }

    @Test
    @DisplayName("SampleData contains exactly 6 subjects, 8 students, and valid default graph")
    void testSampleDataEntities() {
        assertEquals(6, sampleData.getSubjects().size(), "Must have 6 preloaded subjects");
        assertEquals(8, sampleData.getStudents().size(), "Must have 8 preloaded students");
        assertFalse(sampleData.getEnrollments().isEmpty(), "Enrollments must not be empty");

        ConflictGraph graph = sampleData.getDefaultGraph();
        assertNotNull(graph);
        assertEquals(6, graph.getVertexCount(), "Default conflict graph must have 6 vertices");
        assertTrue(graph.getEdgeCount() >= 5, "Graph must have conflict edges");
    }

    @Test
    @DisplayName("Welsh-Powell solves SampleData into a valid conflict-free schedule requiring 3 timeslots")
    void testWelshPowellOnSampleData() {
        ConflictGraph graph = sampleData.getDefaultGraph();
        WelshPowellAlgorithm algo = new WelshPowellAlgorithm();
        ColoringResult result = algo.color(graph);

        assertEquals(3, result.getColorsUsed(), "Sample scenario should color in exactly 3 slots");
        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph), "Schedule must be 100% conflict-free");
        assertEquals(6, schedule.getAssignments().size(), "All 6 subjects must be scheduled");
    }

    @Test
    @DisplayName("Backtracking solves SampleData into an optimal conflict-free schedule requiring 3 timeslots")
    void testBacktrackingOnSampleData() {
        ConflictGraph graph = sampleData.getDefaultGraph();
        BacktrackingAlgorithm algo = new BacktrackingAlgorithm();
        ColoringResult result = algo.color(graph);

        assertEquals(3, result.getColorsUsed(), "Optimal chromatic number chi(G) must be 3");
        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph), "Schedule must be 100% conflict-free");
        assertEquals(6, schedule.getAssignments().size(), "All 6 subjects must be scheduled");
    }
}
