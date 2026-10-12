package com.examguard.db;

import com.examguard.data.SampleData;
import com.examguard.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard JDBC manager for local SQLite persistence (examguard.db).
 * Manages tables for Subjects, Students, and Enrollments with foreign-key cascades,
 * auto-seeds default scenario on first launch, and constructs the active ConflictGraph.
 */
public class DatabaseManager implements AutoCloseable {

    public static final String DEFAULT_JDBC_URL = "jdbc:sqlite:examguard.db";
    private final String jdbcUrl;
    private final Connection connection;

    public DatabaseManager() {
        this(DEFAULT_JDBC_URL);
    }

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            try (Statement stmt = this.connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish SQLite connection to " + jdbcUrl, e);
        }
        initSchema();
        seedIfEmpty();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * Creates database schema if tables do not exist.
     */
    public void initSchema() {
        String createSubjects = """
                CREATE TABLE IF NOT EXISTS subjects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL
                );
                """;

        String createStudents = """
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL
                );
                """;

        String createEnrollments = """
                CREATE TABLE IF NOT EXISTS enrollments (
                    student_id INTEGER NOT NULL,
                    subject_id INTEGER NOT NULL,
                    PRIMARY KEY (student_id, subject_id),
                    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSubjects);
            stmt.execute(createStudents);
            stmt.execute(createEnrollments);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite database schema", e);
        }
    }

    /**
     * Seeds the database with default university sample data if subjects table is empty.
     */
    public void seedIfEmpty() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM subjects")) {
            if (rs.next() && rs.getInt(1) == 0) {
                resetToSampleData();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking database content for seeding", e);
        }
    }

    /**
     * Resets the database to the preloaded SampleData scenario (6 subjects, 8 students).
     */
    public synchronized void resetToSampleData() {
        SampleData demo = new SampleData();
        try {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM enrollments;");
                stmt.execute("DELETE FROM students;");
                stmt.execute("DELETE FROM subjects;");
            }

            // Insert Subjects
            String insertSubject = "INSERT INTO subjects (id, code, name) VALUES (?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insertSubject)) {
                for (Subject s : demo.getSubjects()) {
                    ps.setInt(1, s.getId());
                    ps.setString(2, s.getCode());
                    ps.setString(3, s.getName());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Insert Students
            String insertStudent = "INSERT INTO students (id, student_id, name) VALUES (?, ?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insertStudent)) {
                for (Student st : demo.getStudents()) {
                    ps.setInt(1, st.getId());
                    ps.setString(2, st.getStudentId());
                    ps.setString(3, st.getName());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Insert Enrollments
            String insertEnrollment = "INSERT INTO enrollments (student_id, subject_id) VALUES (?, ?);";
            try (PreparedStatement ps = connection.prepareStatement(insertEnrollment)) {
                for (Enrollment en : demo.getEnrollments()) {
                    ps.setInt(1, en.getStudentId());
                    ps.setInt(2, en.getSubjectId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // ignore
            }
            throw new RuntimeException("Failed to reset database to sample data", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    // --- SUBJECT CRUD ---

    public synchronized List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, code, name FROM subjects ORDER BY code ASC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Subject(rs.getInt("id"), rs.getString("code"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching subjects", e);
        }
        return list;
    }

    public synchronized Subject getSubjectById(int id) {
        String sql = "SELECT id, code, name FROM subjects WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Subject(rs.getInt("id"), rs.getString("code"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching subject by ID: " + id, e);
        }
        return null;
    }

    public synchronized Subject insertSubject(String code, String name) {
        String sql = "INSERT INTO subjects (code, name) VALUES (?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Subject(rs.getInt(1), code.trim(), name.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert subject: " + e.getMessage(), e);
        }
        return null;
    }

    public synchronized boolean updateSubject(int id, String code, String name) {
        String sql = "UPDATE subjects SET code = ?, name = ? WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update subject: " + e.getMessage(), e);
        }
    }

    public synchronized boolean deleteSubject(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete subject ID: " + id, e);
        }
    }

    // --- STUDENT CRUD ---

    public synchronized List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, student_id, name FROM students ORDER BY student_id ASC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(rs.getInt("id"), rs.getString("student_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching students", e);
        }
        return list;
    }

    public synchronized Student getStudentById(int id) {
        String sql = "SELECT id, student_id, name FROM students WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(rs.getInt("id"), rs.getString("student_id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching student ID: " + id, e);
        }
        return null;
    }

    public synchronized Student insertStudent(String studentId, String name) {
        String sql = "INSERT INTO students (student_id, name) VALUES (?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, name.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Student(rs.getInt(1), studentId.trim(), name.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert student: " + e.getMessage(), e);
        }
        return null;
    }

    public synchronized boolean updateStudent(int id, String studentId, String name) {
        String sql = "UPDATE students SET student_id = ?, name = ? WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, name.trim());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student: " + e.getMessage(), e);
        }
    }

    public synchronized boolean deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete student ID: " + id, e);
        }
    }

    // --- ENROLLMENTS ---

    public synchronized List<Enrollment> getAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT student_id, subject_id FROM enrollments;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Enrollment(rs.getInt("student_id"), rs.getInt("subject_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching enrollments", e);
        }
        return list;
    }

    public synchronized List<Integer> getEnrolledSubjectIds(int studentId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT subject_id FROM enrollments WHERE student_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("subject_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching enrollments for student " + studentId, e);
        }
        return list;
    }

    public synchronized void setStudentEnrollments(int studentId, List<Integer> subjectIds) {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM enrollments WHERE student_id = ?;")) {
                del.setInt(1, studentId);
                del.executeUpdate();
            }

            if (subjectIds != null && !subjectIds.isEmpty()) {
                String insert = "INSERT OR IGNORE INTO enrollments (student_id, subject_id) VALUES (?, ?);";
                try (PreparedStatement ins = connection.prepareStatement(insert)) {
                    for (int subjId : subjectIds) {
                        ins.setInt(1, studentId);
                        ins.setInt(2, subjId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // ignore
            }
            throw new RuntimeException("Failed to update enrollments for student " + studentId, e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public synchronized void addEnrollment(int studentId, int subjectId) {
        String sql = "INSERT OR IGNORE INTO enrollments (student_id, subject_id) VALUES (?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add enrollment", e);
        }
    }

    public synchronized void removeEnrollment(int studentId, int subjectId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND subject_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove enrollment", e);
        }
    }

    /**
     * Constructs the active ConflictGraph using the latest database records.
     */
    public synchronized ConflictGraph loadActiveConflictGraph() {
        List<Subject> subjects = getAllSubjects();
        List<Student> students = getAllStudents();
        List<Enrollment> enrollments = getAllEnrollments();
        return ConflictGraphBuilder.build(subjects, enrollments, students);
    }
}
