package com.examguard.ui;

import com.examguard.algorithm.BacktrackingAlgorithm;
import com.examguard.algorithm.ColoringResult;
import com.examguard.algorithm.WelshPowellAlgorithm;
import com.examguard.data.SampleData;
import com.examguard.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SubjectFilterTest {

    private SampleData sampleData;
    private List<Subject> allSubjects;

    @BeforeEach
    void setUp() {
        sampleData = new SampleData();
        allSubjects = sampleData.getSubjects();
    }

    @Test
    @DisplayName("Safeguard 1: Subject Filtering strictly excludes deselected subjects and purges their conflict edges")
    void testSubjectFilteringExcludesDeselectedAndPurgesEdges() {
        // Exclude CS101 and CHEM101, keeping 4 subjects
        Subject cs101 = allSubjects.stream().filter(s -> s.getCode().equals("CS101")).findFirst().orElseThrow();
        Subject chem101 = allSubjects.stream().filter(s -> s.getCode().equals("CHEM101")).findFirst().orElseThrow();

        List<Subject> selected = allSubjects.stream()
                .filter(s -> !s.equals(cs101) && !s.equals(chem101))
                .toList();

        assertEquals(4, selected.size(), "Selected subset must contain 4 subjects");

        // Rebuild active graph from strictly selected subjects
        ConflictGraph filteredGraph = ConflictGraphBuilder.build(
                selected,
                sampleData.getEnrollments(),
                sampleData.getStudents()
        );

        // Verify vertex count and identity
        assertEquals(4, filteredGraph.getVertexCount(), "Active graph must have strictly 4 vertices");
        assertFalse(filteredGraph.getVertices().contains(cs101), "Excluded subject CS101 must not be in vertices");
        assertFalse(filteredGraph.getVertices().contains(chem101), "Excluded subject CHEM101 must not be in vertices");

        // Verify edges: NO edge may touch CS101 or CHEM101
        for (ConflictEdge edge : filteredGraph.getAllEdges()) {
            assertNotEquals(cs101, edge.getSubjectA(), "No edge should connect to excluded subject CS101");
            assertNotEquals(cs101, edge.getSubjectB(), "No edge should connect to excluded subject CS101");
            assertNotEquals(chem101, edge.getSubjectA(), "No edge should connect to excluded subject CHEM101");
            assertNotEquals(chem101, edge.getSubjectB(), "No edge should connect to excluded subject CHEM101");
        }

        // Specifically verify the original CS101-CS102 clash edge is gone
        int vCs102 = filteredGraph.getVertexIndex(allSubjects.stream().filter(s -> s.getCode().equals("CS102")).findFirst().orElseThrow());
        for (int neighbor : filteredGraph.getNeighbors(vCs102)) {
            Subject neighborSubject = filteredGraph.getSubject(neighbor);
            assertNotEquals("CS101", neighborSubject.getCode(), "CS102 neighbor list must not contain excluded CS101");
        }
    }

    @Test
    @DisplayName("Safeguard 2: Filtered subset generates valid conflict-free timetable matching current selection")
    void testFilteredSubsetProducesConflictFreeTimetable() {
        // Select 3 subjects: CS101, MATH101, CHEM101
        List<Subject> selected = allSubjects.stream()
                .filter(s -> List.of("CS101", "MATH101", "CHEM101").contains(s.getCode()))
                .toList();

        ConflictGraph filteredGraph = ConflictGraphBuilder.build(
                selected,
                sampleData.getEnrollments(),
                sampleData.getStudents()
        );

        // Solve using Welsh-Powell
        WelshPowellAlgorithm welshPowell = new WelshPowellAlgorithm();
        ColoringResult wpResult = welshPowell.color(filteredGraph);
        Schedule wpSchedule = wpResult.toSchedule(filteredGraph);

        assertTrue(wpSchedule.isValid(filteredGraph), "Welsh-Powell timetable on subset must be 100% conflict-free");
        assertEquals(3, wpSchedule.getAssignments().size(), "Timetable must schedule exactly the 3 selected subjects");
        Set<Subject> scheduledSubjects = wpSchedule.getAssignments().keySet();
        assertEquals(Set.copyOf(selected), scheduledSubjects, "Scheduled subjects must strictly match current selection");

        // Solve using Backtracking
        BacktrackingAlgorithm backtracking = new BacktrackingAlgorithm();
        ColoringResult btResult = backtracking.color(filteredGraph);
        Schedule btSchedule = btResult.toSchedule(filteredGraph);

        assertTrue(btSchedule.isValid(filteredGraph), "Backtracking timetable on subset must be 100% conflict-free");
        assertEquals(3, btSchedule.getAssignments().size(), "Timetable must schedule exactly the 3 selected subjects");
    }

    @Test
    @DisplayName("Safeguard 3: Selection modification invalidates previous solution state and requires re-solving")
    void testSelectionChangeInvalidatesPreviousSchedule() {
        // Initial full graph (6 subjects)
        ConflictGraph fullGraph = sampleData.getDefaultGraph();
        WelshPowellAlgorithm algo = new WelshPowellAlgorithm();
        ColoringResult fullResult = algo.color(fullGraph);
        Schedule fullSchedule = fullResult.toSchedule(fullGraph);
        assertTrue(fullSchedule.isValid(fullGraph), "Full schedule valid for full graph");

        // User deselects MATH101 and PHYS101 -> 4 subjects remain
        List<Subject> reduced = allSubjects.stream()
                .filter(s -> !s.getCode().equals("MATH101") && !s.getCode().equals("PHYS101"))
                .toList();

        ConflictGraph reducedGraph = ConflictGraphBuilder.build(
                reduced,
                sampleData.getEnrollments(),
                sampleData.getStudents()
        );

        // Old schedule contains keys that are not part of reducedGraph's vertices
        // A schedule tailored to full graph does not match reduced graph specification
        ColoringResult reducedResult = algo.color(reducedGraph);
        Schedule reducedSchedule = reducedResult.toSchedule(reducedGraph);

        assertEquals(4, reducedSchedule.getAssignments().size(), "New schedule must have 4 subjects");
        assertTrue(reducedSchedule.isValid(reducedGraph), "New schedule is valid for reduced graph");

        // Old 6-subject schedule has 6 entries, cannot match current 4-subject active selection
        assertNotEquals(fullSchedule.getAssignments().size(), reducedSchedule.getAssignments().size(),
                "Displayed schedule must not retain old 6-subject assignments when 4 subjects are active");
    }
}
