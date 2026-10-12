package com.examguard.model;

import java.util.Objects;

/**
 * Represents a student enrolled in one or more subjects.
 * Students who enroll in multiple subjects create conflict edges between those subjects.
 */
public class Student {
    private final int id;
    private final String studentId;
    private final String name;

    public Student(int id, String studentId, String name) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty");
        }
        this.id = id;
        this.studentId = studentId.trim();
        this.name = (name == null || name.trim().isEmpty()) ? this.studentId : name.trim();
    }

    public int getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId);
    }

    @Override
    public String toString() {
        return name + " [" + studentId + "]";
    }
}
