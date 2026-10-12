package com.examguard.benchmark;

import com.examguard.data.SampleData;
import com.examguard.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standardized preset graph scenarios for consistent, repeatable algorithm benchmarking:
 * 1. Small University Scenario (6 subjects, 8 students, \u03C7(G) = 3)
 * 2. Medium Department Scenario (10 subjects, 18 students, \u03C7(G) = 4)
 * 3. Dense / Clash-Heavy Scenario (8 subjects, K_4 subgraphs, dense conflicts, \u03C7(G) = 4)
 */
public class BenchmarkPresets {

    public static ConflictGraph getSmallScenario() {
        return new SampleData().getDefaultGraph();
    }

    public static ConflictGraph getMediumScenario() {
        List<Subject> subjects = List.of(
                new Subject(1, "CS101", "Intro to Programming"),
                new Subject(2, "CS102", "Data Structures"),
                new Subject(3, "CS201", "Algorithms"),
                new Subject(4, "CS202", "Operating Systems"),
                new Subject(5, "MATH101", "Calculus I"),
                new Subject(6, "MATH102", "Linear Algebra"),
                new Subject(7, "MATH201", "Discrete Math"),
                new Subject(8, "PHYS101", "Physics I"),
                new Subject(9, "CHEM101", "Chemistry I"),
                new Subject(10, "ENG101", "Technical English")
        );

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            students.add(new Student(i, String.format("STU-%03d", i), "Student " + i));
        }

        List<Enrollment> enrollments = new ArrayList<>();
        // Group A: CS core (CS101, CS102, CS201, MATH201)
        enrollments.add(new Enrollment(1, 1)); enrollments.add(new Enrollment(1, 2));
        enrollments.add(new Enrollment(2, 2)); enrollments.add(new Enrollment(2, 3));
        enrollments.add(new Enrollment(3, 3)); enrollments.add(new Enrollment(3, 7));
        enrollments.add(new Enrollment(4, 1)); enrollments.add(new Enrollment(4, 7));

        // Group B: Systems & Math (CS202, MATH102, MATH201, PHYS101)
        enrollments.add(new Enrollment(5, 4)); enrollments.add(new Enrollment(5, 6));
        enrollments.add(new Enrollment(6, 6)); enrollments.add(new Enrollment(6, 7));
        enrollments.add(new Enrollment(7, 4)); enrollments.add(new Enrollment(7, 8));
        enrollments.add(new Enrollment(8, 8)); enrollments.add(new Enrollment(8, 5));

        // Group C: First year sciences (MATH101, PHYS101, CHEM101, ENG101)
        enrollments.add(new Enrollment(9, 5)); enrollments.add(new Enrollment(9, 8));
        enrollments.add(new Enrollment(10, 8)); enrollments.add(new Enrollment(10, 9));
        enrollments.add(new Enrollment(11, 9)); enrollments.add(new Enrollment(11, 10));
        enrollments.add(new Enrollment(12, 5)); enrollments.add(new Enrollment(12, 10));

        // Group D: Cross-disciplinary bridge clashes
        enrollments.add(new Enrollment(13, 2)); enrollments.add(new Enrollment(13, 5));
        enrollments.add(new Enrollment(14, 3)); enrollments.add(new Enrollment(14, 4));
        enrollments.add(new Enrollment(15, 1)); enrollments.add(new Enrollment(15, 6));
        enrollments.add(new Enrollment(16, 6)); enrollments.add(new Enrollment(16, 9));
        enrollments.add(new Enrollment(17, 7)); enrollments.add(new Enrollment(17, 10));
        enrollments.add(new Enrollment(18, 4)); enrollments.add(new Enrollment(18, 10));

        return ConflictGraphBuilder.build(subjects, enrollments, students);
    }

    public static ConflictGraph getDenseScenario() {
        // 8 subjects with dense complete subgraphs (K_4 on 1..4 and K_4 on 5..8, with cross-links)
        List<Subject> subjects = List.of(
                new Subject(1, "SUBJ-A", "Advanced Algorithms"),
                new Subject(2, "SUBJ-B", "Distributed Systems"),
                new Subject(3, "SUBJ-C", "Machine Learning"),
                new Subject(4, "SUBJ-D", "Compiler Design"),
                new Subject(5, "SUBJ-E", "Database Internals"),
                new Subject(6, "SUBJ-F", "Network Security"),
                new Subject(7, "SUBJ-G", "Computer Architecture"),
                new Subject(8, "SUBJ-H", "Quantum Computing")
        );

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            students.add(new Student(i, String.format("STU-D%02d", i), "Dense Cohort " + i));
        }

        List<Enrollment> enrollments = new ArrayList<>();
        int stu = 1;
        // K_4 on A, B, C, D: all pairwise pairs
        int[] clique1 = {1, 2, 3, 4};
        for (int i = 0; i < clique1.length; i++) {
            for (int j = i + 1; j < clique1.length; j++) {
                enrollments.add(new Enrollment(stu, clique1[i]));
                enrollments.add(new Enrollment(stu, clique1[j]));
                stu++;
            }
        }

        // K_4 on E, F, G, H: all pairwise pairs
        int[] clique2 = {5, 6, 7, 8};
        for (int i = 0; i < clique2.length; i++) {
            for (int j = i + 1; j < clique2.length; j++) {
                enrollments.add(new Enrollment(stu, clique2[i]));
                enrollments.add(new Enrollment(stu, clique2[j]));
                stu++;
            }
        }

        // Inter-clique connecting edges
        enrollments.add(new Enrollment(stu, 1)); enrollments.add(new Enrollment(stu++, 5));
        enrollments.add(new Enrollment(stu, 2)); enrollments.add(new Enrollment(stu++, 6));
        enrollments.add(new Enrollment(stu, 3)); enrollments.add(new Enrollment(stu++, 7));
        enrollments.add(new Enrollment(stu, 4)); enrollments.add(new Enrollment(stu++, 8));

        return ConflictGraphBuilder.build(subjects, enrollments, students);
    }
}
