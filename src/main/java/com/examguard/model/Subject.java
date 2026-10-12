package com.examguard.model;

import java.util.Objects;

/**
 * Represents an academic subject (course) to be scheduled for an exam.
 * In graph theory terms, each Subject corresponds to a Vertex in the Conflict Graph.
 */
public class Subject {
    private final int id;
    private final String code;
    private final String name;

    public Subject(int id, String code, String name) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code cannot be empty");
        }
        this.id = id;
        this.code = code.trim().toUpperCase();
        this.name = (name == null || name.trim().isEmpty()) ? this.code : name.trim();
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id == subject.id && Objects.equals(code, subject.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return code + " (" + name + ")";
    }
}
