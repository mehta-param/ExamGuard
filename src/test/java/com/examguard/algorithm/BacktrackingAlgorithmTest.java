package com.examguard.algorithm;

import com.examguard.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Recursive Backtracking Algorithm Tests")
class BacktrackingAlgorithmTest {

    private BacktrackingAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new BacktrackingAlgorithm();
    }

    @Test
    @DisplayName("Edgeless graph requires 1 timeslot and verifies valid schedule")
    void testEdgelessGraph() {
        Subject s1 = new Subject(1, "A", "A");
        Subject s2 = new Subject(2, "B", "B");
        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2), List.of(), List.of());

        ColoringResult result = algorithm.color(graph);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getColorsUsed());

        Schedule schedule = result.toSchedule(graph);
        assertTrue(schedule.isValid(graph));
    }

    @Test
    @DisplayName("Complete graph K4 finds exact chromatic number 4 and fails on 3 slots")
    void testCompleteGraphK4() {
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            subjects.add(new Subject(i, "S" + i, "Subject " + i));
        }

        List<Student> students = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int studentId = 1;

        for (int i = 1; i <= 4; i++) {
            for (int j = i + 1; j <= 4; j++) {
                Student s = new Student(studentId, "ST" + studentId, "Student " + studentId);
                students.add(s);
                enrollments.add(new Enrollment(studentId, i));
                enrollments.add(new Enrollment(studentId, j));
                studentId++;
            }
        }

        ConflictGraph graph = ConflictGraphBuilder.build(subjects, enrollments, students);

        // 1. color() finds exact chromatic number chi(G) = 4
        ColoringResult result = algorithm.color(graph);
        assertTrue(result.isSuccess());
        assertEquals(4, result.getColorsUsed(), "K4 chromatic number is strictly 4");
        assertTrue(result.toSchedule(graph).isValid(graph));

        // 2. colorWithLimit(graph, 3) must be INFEASIBLE
        ColoringResult limitedFail = algorithm.colorWithLimit(graph, 3);
        assertFalse(limitedFail.isSuccess(), "K4 cannot be colored with 3 colors");

        // 3. colorWithLimit(graph, 4) must be FEASIBLE
        ColoringResult limitedPass = algorithm.colorWithLimit(graph, 4);
        assertTrue(limitedPass.isSuccess(), "K4 can be colored with 4 colors");
        assertTrue(limitedPass.toSchedule(graph).isValid(graph));
    }

    @Test
    @DisplayName("Bipartite graph K2,3 requires exactly 2 colors")
    void testBipartiteGraph() {
        // Set A: {1, 2}, Set B: {3, 4, 5}
        List<Subject> subjects = List.of(
            new Subject(1, "A1", "A1"), new Subject(2, "A2", "A2"),
            new Subject(3, "B1", "B1"), new Subject(4, "B2", "B2"), new Subject(5, "B3", "B3")
        );

        List<Student> students = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int id = 1;

        int[] setA = {1, 2};
        int[] setB = {3, 4, 5};

        for (int a : setA) {
            for (int b : setB) {
                Student s = new Student(id, "S" + id, "Student " + id);
                students.add(s);
                enrollments.add(new Enrollment(id, a));
                enrollments.add(new Enrollment(id, b));
                id++;
            }
        }

        ConflictGraph graph = ConflictGraphBuilder.build(subjects, enrollments, students);
        ColoringResult result = algorithm.color(graph);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getColorsUsed(), "Bipartite graph has chromatic number 2");
        assertTrue(result.toSchedule(graph).isValid(graph));
    }

    @Test
    @DisplayName("Odd cycle C5 requires chromatic number 3")
    void testOddCycleC5() {
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            subjects.add(new Subject(i, "S" + i, "S" + i));
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
        assertEquals(3, result.getColorsUsed(), "C5 requires chromatic number 3");
        assertTrue(result.toSchedule(graph).isValid(graph));
    }

    @Test
    @DisplayName("Safety step threshold aborts runaway backtracking without infinite recursion")
    void testSafetyStepLimit() {
        // Create complete graph K5
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            subjects.add(new Subject(i, "S" + i, "S" + i));
        }

        List<Student> students = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int studentId = 1;

        for (int i = 1; i <= 5; i++) {
            for (int j = i + 1; j <= 5; j++) {
                Student s = new Student(studentId, "ST" + studentId, "Student " + studentId);
                students.add(s);
                enrollments.add(new Enrollment(studentId, i));
                enrollments.add(new Enrollment(studentId, j));
                studentId++;
            }
        }

        ConflictGraph graph = ConflictGraphBuilder.build(subjects, enrollments, students);

        // Configure tiny step threshold of 2 steps so it triggers abortion immediately
        BacktrackingAlgorithm limitedAlgo = new BacktrackingAlgorithm(2);
        ColoringResult result = limitedAlgo.colorWithLimit(graph, 4); // K5 requires 5 colors, searching with 4 branches

        assertFalse(result.isSuccess(), "Should abort cleanly due to step limit");
        assertTrue(result.getMessage().contains("Step limit exceeded") || result.getMessage().contains("Aborted"),
            "Expected abortion message but was: " + result.getMessage());
    }
}
