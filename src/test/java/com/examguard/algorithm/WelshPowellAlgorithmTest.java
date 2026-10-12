package com.examguard.algorithm;

import com.examguard.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Welsh-Powell Greedy Algorithm Tests")
class WelshPowellAlgorithmTest {

    private WelshPowellAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new WelshPowellAlgorithm();
    }

    @Test
    @DisplayName("Edgeless graph with isolated subjects requires exactly 1 timeslot")
    void testEdgelessGraph() {
        List<Subject> subjects = List.of(
            new Subject(1, "CS101", "CS 1"),
            new Subject(2, "CS102", "CS 2"),
            new Subject(3, "CS103", "CS 3")
        );
        ConflictGraph graph = ConflictGraphBuilder.build(subjects, List.of(), List.of());

        ColoringResult result = algorithm.color(graph);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getColorsUsed());
        assertEquals(1, result.getColor(0));
        assertEquals(1, result.getColor(1));
        assertEquals(1, result.getColor(2));

        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph));
    }

    @Test
    @DisplayName("Complete graph K4 requires exactly 4 distinct colors/timeslots")
    void testCompleteGraphK4() {
        // Build K4: 4 subjects, all pairs clash
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            subjects.add(new Subject(i, "SUB" + i, "Subject " + i));
        }

        List<Student> students = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int studentId = 100;

        // One student for each of the 6 pairs
        for (int i = 1; i <= 4; i++) {
            for (int j = i + 1; j <= 4; j++) {
                Student s = new Student(studentId, "STU" + studentId, "Student " + studentId);
                students.add(s);
                enrollments.add(new Enrollment(studentId, i));
                enrollments.add(new Enrollment(studentId, j));
                studentId++;
            }
        }

        ConflictGraph graph = ConflictGraphBuilder.build(subjects, enrollments, students);
        assertEquals(4, graph.getVertexCount());
        assertEquals(6, graph.getEdgeCount());

        ColoringResult result = algorithm.color(graph);

        assertTrue(result.isSuccess());
        assertEquals(4, result.getColorsUsed());

        // Verify valid coloring
        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph));

        // Verify all 4 vertices have unique colors
        long uniqueColors = result.getAssignments().values().stream().distinct().count();
        assertEquals(4, uniqueColors);
    }

    @Test
    @DisplayName("Odd cycle graph C5 is colored with 3 colors without conflicts")
    void testOddCycleC5() {
        // Cycle: 1-2, 2-3, 3-4, 4-5, 5-1
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            subjects.add(new Subject(i, "S" + i, "Subject " + i));
        }

        List<Student> students = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int[][] pairs = {{1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 1}};

        for (int i = 0; i < pairs.length; i++) {
            Student s = new Student(i + 1, "ST" + (i + 1), "Student " + (i + 1));
            students.add(s);
            enrollments.add(new Enrollment(s.getId(), pairs[i][0]));
            enrollments.add(new Enrollment(s.getId(), pairs[i][1]));
        }

        ConflictGraph graph = ConflictGraphBuilder.build(subjects, enrollments, students);
        ColoringResult result = algorithm.color(graph);

        assertTrue(result.isSuccess());
        assertEquals(3, result.getColorsUsed(), "Odd cycle C5 has chromatic number 3");

        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph));
    }

    @Test
    @DisplayName("Execution trace is non-empty, sequential, and ends with FINISH step")
    void testExecutionTraceRecording() {
        Subject s1 = new Subject(1, "A", "A");
        Subject s2 = new Subject(2, "B", "B");
        Student st = new Student(1, "S1", "Alice");
        List<Enrollment> enrollments = List.of(new Enrollment(1, 1), new Enrollment(1, 2));

        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2), enrollments, List.of(st));
        ColoringResult result = algorithm.color(graph);

        ExecutionTrace trace = result.getTrace();
        assertNotNull(trace);
        assertTrue(trace.getStepCount() > 0);

        AlgorithmStep first = trace.getStep(0);
        assertEquals(1, first.getStepNumber());

        AlgorithmStep last = trace.getStep(trace.getStepCount() - 1);
        assertEquals(StepType.FINISH, last.getType());
        assertEquals(trace.getStepCount(), last.getStepNumber());
    }

    @Test
    @DisplayName("Coloring with tight slot limit fails when limit is less than required slots")
    void testColorWithLimitFailure() {
        Subject s1 = new Subject(1, "A", "A");
        Subject s2 = new Subject(2, "B", "B");
        Student st = new Student(1, "S1", "Alice");
        List<Enrollment> enrollments = List.of(new Enrollment(1, 1), new Enrollment(1, 2));

        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2), enrollments, List.of(st));

        // 2 subjects clash, require at least 2 slots. Limit to 1 slot:
        ColoringResult result = algorithm.colorWithLimit(graph, 1);
        assertFalse(result.isSuccess(), "Should fail when 2 clashing subjects are forced into 1 slot");
    }
}
