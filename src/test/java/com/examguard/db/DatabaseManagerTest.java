package com.examguard.db;

import com.examguard.model.ConflictGraph;
import com.examguard.model.Student;
import com.examguard.model.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        // Use an in-memory SQLite database for test isolation
        db = new DatabaseManager("jdbc:sqlite::memory:");
    }

    @AfterEach
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

        int s1Id = s1.getId();
        int studentId = student.getId();

        // Delete subject s1 -> cascade delete should remove enrollment from enrollments table
        db.deleteSubject(s1Id);
        List<Integer> afterSubjDelete = db.getEnrolledSubjectIds(studentId);
        assertEquals(1, afterSubjDelete.size());
        assertEquals(s2.getId(), afterSubjDelete.get(0));

        // Direct check: verify no orphan row with deleted subject_id exists in enrollments table
        boolean hasOrphanSubjectEnrollment = db.getAllEnrollments().stream()
                .anyMatch(e -> e.getSubjectId() == s1Id);
        assertFalse(hasOrphanSubjectEnrollment, "Cascade delete must leave no orphan enrollment for deleted subject");

        // Delete student -> enrollments should be gone from enrollments table
        db.deleteStudent(studentId);
        assertTrue(db.getEnrolledSubjectIds(studentId).isEmpty());

        // Direct check: verify no orphan row with deleted student_id exists in enrollments table
        boolean hasOrphanStudentEnrollment = db.getAllEnrollments().stream()
                .anyMatch(e -> e.getStudentId() == studentId);
        assertFalse(hasOrphanStudentEnrollment, "Cascade delete must leave no orphan enrollment for deleted student");
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

    @Test
    void testPersistenceAcrossRestarts(@TempDir Path tempDir) {
        String dbPath = tempDir.resolve("examguard_test_restart.db").toAbsolutePath().toString();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;

        // 1. First "launch": Auto-seeds 6 subjects, 8 students.
        try (DatabaseManager db1 = new DatabaseManager(jdbcUrl)) {
            assertEquals(6, db1.getAllSubjects().size());

            // Add custom subject and student
            Subject customSubj = db1.insertSubject("CS401", "Distributed Systems");
            Student customStu = db1.insertStudent("STU-777", "Donald Knuth");
            db1.setStudentEnrollments(customStu.getId(), List.of(customSubj.getId()));

            assertEquals(7, db1.getAllSubjects().size());
            assertEquals(9, db1.getAllStudents().size());
            assertEquals(17, db1.getAllEnrollments().size());
        } // Connection closed simulating application exit

        // 2. Second "launch": Connects to the same file. Must NOT re-seed and MUST preserve data.
        try (DatabaseManager db2 = new DatabaseManager(jdbcUrl)) {
            assertEquals(7, db2.getAllSubjects().size(), "Subjects must survive restart");
            assertEquals(9, db2.getAllStudents().size(), "Students must survive restart");
            assertEquals(17, db2.getAllEnrollments().size(), "Enrollments must survive restart");

            Subject loadedSubj = db2.getAllSubjects().stream()
                    .filter(s -> s.getCode().equals("CS401"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(loadedSubj);
            assertEquals("Distributed Systems", loadedSubj.getName());

            Student loadedStu = db2.getAllStudents().stream()
                    .filter(s -> s.getStudentId().equals("STU-777"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(loadedStu);
            assertEquals("Donald Knuth", loadedStu.getName());

            List<Integer> enrolled = db2.getEnrolledSubjectIds(loadedStu.getId());
            assertEquals(1, enrolled.size());
            assertEquals(loadedSubj.getId(), enrolled.get(0));
        }
    }

    @Test
    void testSeedingOnlyWhenEmpty() {
        // Since setUp() initialized db with 6 subjects, calling seedIfEmpty() again must not duplicate
        int initialSubjects = db.getAllSubjects().size();
        assertEquals(6, initialSubjects);

        db.seedIfEmpty();
        assertEquals(6, db.getAllSubjects().size(), "seedIfEmpty must be a no-op when database is not empty");
    }
}
