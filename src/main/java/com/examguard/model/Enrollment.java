package com.examguard.model;

import java.util.Objects;

/**
 * Represents the registration of a Student in a Subject.
 * When a student is enrolled in subjects A and B, a conflict edge (A, B) is created.
 */
public class Enrollment {
    private final int studentId;
    private final int subjectId;

    public Enrollment(int studentId, int subjectId) {
        this.studentId = studentId;
        this.subjectId = subjectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return studentId == that.studentId && subjectId == that.subjectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, subjectId);
    }

    @Override
    public String toString() {
        return "Enrollment{student=" + studentId + ", subject=" + subjectId + '}';
    }
}
