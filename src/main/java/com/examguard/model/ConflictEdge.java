package com.examguard.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an undirected conflict edge between two Subject vertices.
 * The edge stores the specific set of students enrolled in both subjects,
 * providing the exact clash rationale required for viva defense and explanation banners.
 */
public class ConflictEdge {
    private final Subject subjectA;
    private final Subject subjectB;
    private final Set<Student> conflictingStudents;

    public ConflictEdge(Subject subjectA, Subject subjectB) {
        if (subjectA == null || subjectB == null) {
            throw new IllegalArgumentException("Subjects cannot be null");
        }
        if (subjectA.equals(subjectB)) {
            throw new IllegalArgumentException("Self-loop conflict is not allowed: " + subjectA);
        }
        this.subjectA = subjectA;
        this.subjectB = subjectB;
        this.conflictingStudents = new LinkedHashSet<>();
    }

    public ConflictEdge(Subject subjectA, Subject subjectB, Set<Student> students) {
        this(subjectA, subjectB);
        if (students != null) {
            this.conflictingStudents.addAll(students);
        }
    }

    public Subject getSubjectA() {
        return subjectA;
    }

    public Subject getSubjectB() {
        return subjectB;
    }

    public Set<Student> getConflictingStudents() {
        return Collections.unmodifiableSet(conflictingStudents);
    }

    public int getClashCount() {
        return conflictingStudents.size();
    }

    public void addStudent(Student student) {
        if (student != null) {
            conflictingStudents.add(student);
        }
    }

    /**
     * Given one endpoint of this edge, returns the other endpoint.
     */
    public Subject getOtherEndpoint(Subject subject) {
        if (subjectA.equals(subject)) {
            return subjectB;
        } else if (subjectB.equals(subject)) {
            return subjectA;
        }
        throw new IllegalArgumentException("Subject " + subject + " is not an endpoint of this edge");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConflictEdge that = (ConflictEdge) o;
        // Undirected comparison: (A, B) equals (B, A)
        return (Objects.equals(subjectA, that.subjectA) && Objects.equals(subjectB, that.subjectB)) ||
               (Objects.equals(subjectA, that.subjectB) && Objects.equals(subjectB, that.subjectA));
    }

    @Override
    public int hashCode() {
        // Commutative hash for undirected edge
        return Objects.hashCode(subjectA) ^ Objects.hashCode(subjectB);
    }

    @Override
    public String toString() {
        return subjectA.getCode() + " <-> " + subjectB.getCode() + " (" + conflictingStudents.size() + " clash(es))";
    }
}
