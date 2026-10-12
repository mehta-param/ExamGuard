package com.examguard.model;

import java.util.*;

/**
 * Builds a ConflictGraph from raw domain lists of Subjects, Enrollments, and Students.
 * Two subjects receive an undirected conflict edge if and only if at least one student
 * is enrolled in both subjects.
 */
public class ConflictGraphBuilder {

    /**
     * Builds a ConflictGraph from lists of subjects, enrollments, and students.
     */
    public static ConflictGraph build(List<Subject> subjects, List<Enrollment> enrollments, List<Student> students) {
        if (subjects == null) {
            throw new IllegalArgumentException("Subjects list cannot be null");
        }

        ConflictGraph graph = new ConflictGraph(subjects);
        if (enrollments == null || enrollments.isEmpty() || students == null || students.isEmpty()) {
            return graph; // Edgeless graph
        }

        // Map subjectId -> Subject entity
        Map<Integer, Subject> subjectMap = new HashMap<>();
        for (Subject s : subjects) {
            subjectMap.put(s.getId(), s);
        }

        // Map studentId -> Student entity
        Map<Integer, Student> studentMap = new HashMap<>();
        for (Student st : students) {
            studentMap.put(st.getId(), st);
        }

        // Group subjects enrolled by each student
        Map<Integer, Set<Subject>> studentToSubjects = new HashMap<>();
        for (Enrollment enr : enrollments) {
            Subject subj = subjectMap.get(enr.getSubjectId());
            if (subj != null) {
                studentToSubjects.computeIfAbsent(enr.getStudentId(), k -> new LinkedHashSet<>()).add(subj);
            }
        }

        // Collect all conflict pairs and group the clashing students
        // Key for undirected pair: min(subjIdA, subjIdB) + "-" + max(subjIdA, subjIdB)
        Map<String, ConflictEdge> edgeMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, Set<Subject>> entry : studentToSubjects.entrySet()) {
            int studentId = entry.getKey();
            Student student = studentMap.get(studentId);
            if (student == null) continue;

            List<Subject> enrolledList = new ArrayList<>(entry.getValue());
            int numEnrolled = enrolledList.size();

            // Pairwise combination of subjects for this student
            for (int i = 0; i < numEnrolled; i++) {
                for (int j = i + 1; j < numEnrolled; j++) {
                    Subject subA = enrolledList.get(i);
                    Subject subB = enrolledList.get(j);

                    if (subA.equals(subB)) continue;

                    int minId = Math.min(subA.getId(), subB.getId());
                    int maxId = Math.max(subA.getId(), subB.getId());
                    String pairKey = minId + "-" + maxId;

                    ConflictEdge edge = edgeMap.computeIfAbsent(pairKey, k -> new ConflictEdge(subA, subB));
                    edge.addStudent(student);
                }
            }
        }

        // Insert all created edges into the ConflictGraph
        for (ConflictEdge edge : edgeMap.values()) {
            graph.addEdge(edge);
        }

        return graph;
    }
}
