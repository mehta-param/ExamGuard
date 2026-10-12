package com.examguard.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Conflict Graph & Builder Tests")
class ConflictGraphTest {

    @Test
    @DisplayName("Empty or isolated subjects produce 0 edges, degree 0, density 0.0")
    void testIsolatedSubjects() {
        Subject s1 = new Subject(1, "CS101", "Intro to CS");
        Subject s2 = new Subject(2, "MATH101", "Calculus I");
        Subject s3 = new Subject(3, "PHYS101", "Physics I");

        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2, s3), List.of(), List.of());

        assertEquals(3, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        assertEquals(0, graph.getDegree(0));
        assertEquals(0, graph.getDegree(1));
        assertEquals(0, graph.getDegree(2));
        assertEquals(0, graph.getMaxDegree());
        assertEquals(0.0, graph.getDensity(), 0.001);
        assertFalse(graph.hasConflict(0, 1));
        assertFalse(graph.hasConflict(1, 2));
    }

    @Test
    @DisplayName("Single student shared between two subjects creates one undirected conflict edge")
    void testSingleSharedStudent() {
        Subject s1 = new Subject(1, "CS101", "Intro to CS");
        Subject s2 = new Subject(2, "MATH101", "Calculus I");

        Student alice = new Student(101, "S001", "Alice");

        List<Enrollment> enrollments = List.of(
            new Enrollment(101, 1),
            new Enrollment(101, 2)
        );

        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2), enrollments, List.of(alice));

        assertEquals(2, graph.getVertexCount());
        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getDegree(0));
        assertEquals(1, graph.getDegree(1));
        assertEquals(1, graph.getMaxDegree());
        assertEquals(1.0, graph.getDensity(), 0.001);

        assertTrue(graph.hasConflict(0, 1));
        assertTrue(graph.hasConflict(1, 0));

        ConflictEdge edge = graph.getEdge(0, 1);
        assertNotNull(edge);
        assertEquals(1, edge.getClashCount());
        assertTrue(edge.getConflictingStudents().contains(alice));
    }

    @Test
    @DisplayName("Multiple students sharing the same course pair are recorded on the same edge")
    void testMultipleStudentsOnSameEdge() {
        Subject s1 = new Subject(1, "CS101", "Intro to CS");
        Subject s2 = new Subject(2, "MATH101", "Calculus I");

        Student alice = new Student(101, "S001", "Alice");
        Student bob = new Student(102, "S002", "Bob");

        List<Enrollment> enrollments = List.of(
            new Enrollment(101, 1),
            new Enrollment(101, 2),
            new Enrollment(102, 1),
            new Enrollment(102, 2)
        );

        ConflictGraph graph = ConflictGraphBuilder.build(List.of(s1, s2), enrollments, List.of(alice, bob));

        assertEquals(1, graph.getEdgeCount());
        ConflictEdge edge = graph.getEdge(0, 1);
        assertNotNull(edge);
        assertEquals(2, edge.getClashCount());
        assertTrue(edge.getConflictingStudents().contains(alice));
        assertTrue(edge.getConflictingStudents().contains(bob));
    }

    @Test
    @DisplayName("Triangle graph K3 has 3 vertices, 3 edges, max degree 2, and density 1.0")
    void testCompleteGraphTriangle() {
        Subject s1 = new Subject(1, "CS101", "Computer Science");
        Subject s2 = new Subject(2, "MATH101", "Mathematics");
        Subject s3 = new Subject(3, "PHYS101", "Physics");

        Student alice = new Student(1, "S1", "Alice"); // takes s1, s2
        Student bob = new Student(2, "S2", "Bob");     // takes s2, s3
        Student charlie = new Student(3, "S3", "Charlie"); // takes s3, s1

        List<Enrollment> enrollments = List.of(
            new Enrollment(1, 1), new Enrollment(1, 2),
            new Enrollment(2, 2), new Enrollment(2, 3),
            new Enrollment(3, 3), new Enrollment(3, 1)
        );

        ConflictGraph graph = ConflictGraphBuilder.build(
            List.of(s1, s2, s3),
            enrollments,
            List.of(alice, bob, charlie)
        );

        assertEquals(3, graph.getVertexCount());
        assertEquals(3, graph.getEdgeCount());
        assertEquals(2, graph.getDegree(0));
        assertEquals(2, graph.getDegree(1));
        assertEquals(2, graph.getDegree(2));
        assertEquals(2, graph.getMaxDegree());
        assertEquals(1.0, graph.getDensity(), 0.001);

        assertTrue(graph.hasConflict(0, 1));
        assertTrue(graph.hasConflict(1, 2));
        assertTrue(graph.hasConflict(2, 0));
    }

    @Test
    @DisplayName("Zero false conflicts: distinct students taking separate subjects produce zero edges")
    void testZeroFalseConflicts() {
        Subject s1 = new Subject(1, "CS101", "CS");
        Subject s2 = new Subject(2, "CHEM101", "Chemistry");

        Student alice = new Student(1, "S1", "Alice"); // takes only CS101
        Student bob = new Student(2, "S2", "Bob");     // takes only CHEM101

        List<Enrollment> enrollments = List.of(
            new Enrollment(1, 1),
            new Enrollment(2, 2)
        );

        ConflictGraph graph = ConflictGraphBuilder.build(
            List.of(s1, s2),
            enrollments,
            List.of(alice, bob)
        );

        assertEquals(2, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        assertFalse(graph.hasConflict(0, 1));
    }

    @Test
    @DisplayName("Schedule validation correctly detects conflicting vs conflict-free timeslots")
    void testScheduleValidation() {
        Subject s1 = new Subject(1, "CS101", "CS");
        Subject s2 = new Subject(2, "MATH101", "Math");
        Subject s3 = new Subject(3, "ENG101", "English");

        Student student = new Student(1, "S1", "Alice"); // takes CS101 and MATH101
        List<Enrollment> enrollments = List.of(
            new Enrollment(1, 1),
            new Enrollment(1, 2)
        );

        ConflictGraph graph = ConflictGraphBuilder.build(
            List.of(s1, s2, s3),
            enrollments,
            List.of(student)
        );

        // Schedule with conflict: CS101 and MATH101 both in slot 1
        Schedule badSchedule = new Schedule();
        badSchedule.assign(s1, 1);
        badSchedule.assign(s2, 1);
        badSchedule.assign(s3, 2);
        assertFalse(badSchedule.isValid(graph), "Schedule with clash must be invalid");

        // Schedule without conflict: CS101 in slot 1, MATH101 in slot 2, ENG101 in slot 1
        Schedule goodSchedule = new Schedule();
        goodSchedule.assign(s1, 1);
        goodSchedule.assign(s2, 2);
        goodSchedule.assign(s3, 1); // ENG101 has no clash with CS101
        assertTrue(goodSchedule.isValid(graph), "Conflict-free schedule must be valid");
        assertEquals(2, goodSchedule.getTotalSlotsUsed());
    }
}
