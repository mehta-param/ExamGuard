package com.examguard.model;

import java.util.*;

/**
 * Represents the final or intermediate exam timetable schedule.
 * Maps each Subject to an integer timeslot (where Color 1 = Slot 1, Color 2 = Slot 2, etc.).
 */
public class Schedule {
    private final Map<Subject, Integer> assignments;

    public Schedule() {
        this.assignments = new LinkedHashMap<>();
    }

    public Schedule(Map<Subject, Integer> assignments) {
        this.assignments = new LinkedHashMap<>(assignments);
    }

    public void assign(Subject subject, int timeslot) {
        assignments.put(subject, timeslot);
    }

    public Integer getTimeslot(Subject subject) {
        return assignments.get(subject);
    }

    public Map<Subject, Integer> getAssignments() {
        return Collections.unmodifiableMap(assignments);
    }

    public int getTotalSlotsUsed() {
        if (assignments.isEmpty()) return 0;
        return assignments.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /**
     * Groups subjects by their scheduled timeslots.
     */
    public Map<Integer, List<Subject>> getSubjectsByTimeslot() {
        Map<Integer, List<Subject>> slots = new TreeMap<>();
        for (Map.Entry<Subject, Integer> entry : assignments.entrySet()) {
            slots.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        return slots;
    }

    /**
     * Validates whether this schedule contains any conflicts according to the given ConflictGraph.
     * @return true if 0 conflicts exist; false if any two adjacent subjects share a timeslot.
     */
    public boolean isValid(ConflictGraph graph) {
        if (graph == null) return false;
        for (int u = 0; u < graph.getVertexCount(); u++) {
            Subject subjectU = graph.getSubject(u);
            Integer slotU = assignments.get(subjectU);
            if (slotU == null || slotU <= 0) return false; // unassigned

            for (int v : graph.getNeighbors(u)) {
                if (u < v) { // check each undirected edge once
                    Subject subjectV = graph.getSubject(v);
                    Integer slotV = assignments.get(subjectV);
                    if (Objects.equals(slotU, slotV)) {
                        return false; // Clash found!
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Schedule{" + assignments.size() + " subjects scheduled across " + getTotalSlotsUsed() + " slots}";
    }
}
