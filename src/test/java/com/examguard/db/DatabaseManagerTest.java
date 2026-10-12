package com.examguard.db;

import com.examguard.model.ConflictGraph;
import com.examguard.model.Student;
import com.examguard.model.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        // Use an in-memory SQLite database for test isolation
        db = new DatabaseManager("jdbc:sqlite::memory:");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    @Test
    void testAutoSeedingOnInit() {
        List<Subject> subjects = db.getAllSubjects();
        assertEquals(6, subjects.size(), "Should auto-seed 6 subjects from SampleData");

        List<Student> students = db.getAllStudents();
        assertEquals(8, students.size(), "Should auto-seed 8 students from SampleData");

        assertEquals(16, db.getAllEnrollments().size(), "Should auto-seed 16 enrollments");
    }

    @Test
    void testSubjectCrud() {
        Subject created = db.insertSubject("CS301", "Operating Systems");
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("CS301", created.getCode());

        Subject retrieved = db.getSubjectById(created.getId());
        assertNotNull(retrieved);
        assertEquals("Operating Systems", retrieved.getName());

        boolean updated = db.updateSubject(created.getId(), "CS301-A", "Advanced Operating Systems");
        assertTrue(updated);
        Subject afterUpdate = db.getSubjectById(created.getId());
        assertEquals("CS301-A", afterUpdate.getCode());
        assertEquals("Advanced Operating Systems", afterUpdate.getName());

        boolean deleted = db.deleteSubject(created.getId());
        assertTrue(deleted);
        assertNull(db.getSubjectById(created.getId()));
    }

    @Test
    void testDuplicateSubjectCodeThrowsException() {
        assertThrows(RuntimeException.class, () -> db.insertSubject("CS101", "Duplicate Code"));
    }

    @Test
    void testStudentCrud() {
        Student created = db.insertStudent("STU-009", "Grace Hopper");
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("STU-009", created.getStudentId());
        assertEquals("Grace Hopper", created.getName());

        Student retrieved = db.getStudentById(created.getId());
        assertNotNull(retrieved);
        assertEquals("Grace Hopper", retrieved.getName());

        boolean updated = db.updateStudent(created.getId(), "STU-009A", "Admiral Grace Hopper");
        assertTrue(updated);
        Student afterUpdate = db.getStudentById(created.getId());
        assertEquals("STU-009A", afterUpdate.getStudentId());
        assertEquals("Admiral Grace Hopper", afterUpdate.getName());

        boolean deleted = db.deleteStudent(created.getId());
        assertTrue(deleted);
        assertNull(db.getStudentById(created.getId()));
    }

    @Test
    void testDuplicateStudentIdThrowsException() {
        assertThrows(RuntimeException.class, () -> db.insertStudent("STU-001", "Duplicate Alice"));
    }

    @Test
    void testEnrollmentManagementAndCascadeOnDelete() {
        Student student = db.insertStudent("STU-099", "Test Student");
        Subject s1 = db.getAllSubjects().get(0);
        Subject s2 = db.getAllSubjects().get(1);

        db.setStudentEnrollments(student.getId(), List.of(s1.getId(), s2.getId()));
        List<Integer> enrolled = db.getEnrolledSubjectIds(student.getId());
        assertEquals(2, enrolled.size());
        assertTrue(enrolled.contains(s1.getId()));
        assertTrue(enrolled.contains(s2.getId()));

        // Delete subject s1 -> cascade delete should remove enrollment
        db.deleteSubject(s1.getId());
        List<Integer> afterSubjDelete = db.getEnrolledSubjectIds(student.getId());
        assertEquals(1, afterSubjDelete.size());
        assertEquals(s2.getId(), afterSubjDelete.get(0));

        // Delete student -> enrollments should be gone
        db.deleteStudent(student.getId());
        assertTrue(db.getEnrolledSubjectIds(student.getId()).isEmpty());
    }

    @Test
    void testResetToSampleData() {
        db.insertSubject("EXTRA", "Extra Subject");
        assertTrue(db.getAllSubjects().size() > 6);

        db.resetToSampleData();
        assertEquals(6, db.getAllSubjects().size());
        assertEquals(8, db.getAllStudents().size());
        assertEquals(16, db.getAllEnrollments().size());
    }

    @Test
    void testLoadActiveConflictGraph() {
        ConflictGraph graph = db.loadActiveConflictGraph();
        assertNotNull(graph);
        assertEquals(6, graph.getVertexCount(), "Active graph should have 6 vertices from sample data");
        assertEquals(8, graph.getEdgeCount(), "Active graph should have 8 edges from sample data");
    }
}
