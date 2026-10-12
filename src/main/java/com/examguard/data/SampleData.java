package com.examguard.data;

import com.examguard.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Preloaded realistic university dataset for demonstration, testing, and viva defense.
 * Features 6 subjects, 8 students, and enrollments that form a conflict graph with
 * chromatic number chi(G) = 3 (requiring exactly 3 timeslots).
 */
public class SampleData {

    private final List<Subject> subjects;
    private final List<Student> students;
    private final List<Enrollment> enrollments;
    private final ConflictGraph defaultGraph;

    public SampleData() {
        // 1. Initialize 6 Subjects
        Subject cs101 = new Subject(1, "CS101", "Intro to Programming");
        Subject cs102 = new Subject(2, "CS102", "Data Structures");
        Subject math101 = new Subject(3, "MATH101", "Calculus I");
        Subject math102 = new Subject(4, "MATH102", "Linear Algebra");
        Subject phys101 = new Subject(5, "PHYS101", "Physics I");
        Subject chem101 = new Subject(6, "CHEM101", "Chemistry I");

        this.subjects = List.of(cs101, cs102, math101, math102, phys101, chem101);

        // 2. Initialize 8 Students
        Student s1 = new Student(1, "Alice Smith", "alice@university.edu");
        Student s2 = new Student(2, "Bob Jones", "bob@university.edu");
        Student s3 = new Student(3, "Charlie Brown", "charlie@university.edu");
        Student s4 = new Student(4, "David Clark", "david@university.edu");
        Student s5 = new Student(5, "Emma Watson", "emma@university.edu");
        Student s6 = new Student(6, "Frank Miller", "frank@university.edu");
        Student s7 = new Student(7, "Grace Hopper", "grace@university.edu");
        Student s8 = new Student(8, "Henry Ford", "henry@university.edu");

        this.students = List.of(s1, s2, s3, s4, s5, s6, s7, s8);

        // 3. Create Enrollments producing clashes (studentId, subjectId):
        // Alice (1): CS101 (1) + CS102 (2)
        // Bob (2): CS101 (1) + MATH101 (3)
        // Charlie (3): CS102 (2) + MATH101 (3) -> Triangle K_3 forces at least 3 slots
        // David (4): CS102 (2) + MATH102 (4)
        // Emma (5): MATH102 (4) + PHYS101 (5)
        // Frank (6): PHYS101 (5) + CS101 (1)
        // Grace (7): CHEM101 (6) + MATH101 (3)
        // Henry (8): CHEM101 (6) + MATH102 (4)
        List<Enrollment> list = new ArrayList<>();
        list.add(new Enrollment(s1.getId(), cs101.getId()));
        list.add(new Enrollment(s1.getId(), cs102.getId()));

        list.add(new Enrollment(s2.getId(), cs101.getId()));
        list.add(new Enrollment(s2.getId(), math101.getId()));

        list.add(new Enrollment(s3.getId(), cs102.getId()));
        list.add(new Enrollment(s3.getId(), math101.getId()));

        list.add(new Enrollment(s4.getId(), cs102.getId()));
        list.add(new Enrollment(s4.getId(), math102.getId()));

        list.add(new Enrollment(s5.getId(), math102.getId()));
        list.add(new Enrollment(s5.getId(), phys101.getId()));

        list.add(new Enrollment(s6.getId(), phys101.getId()));
        list.add(new Enrollment(s6.getId(), cs101.getId()));

        list.add(new Enrollment(s7.getId(), chem101.getId()));
        list.add(new Enrollment(s7.getId(), math101.getId()));

        list.add(new Enrollment(s8.getId(), chem101.getId()));
        list.add(new Enrollment(s8.getId(), math102.getId()));

        this.enrollments = Collections.unmodifiableList(list);

        // 4. Build Default Conflict Graph
        this.defaultGraph = ConflictGraphBuilder.build(this.subjects, this.enrollments, this.students);
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public ConflictGraph getDefaultGraph() {
        return defaultGraph;
    }
}
