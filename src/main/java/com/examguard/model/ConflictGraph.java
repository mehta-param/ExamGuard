package com.examguard.model;

import java.util.*;

/**
 * Undirected Conflict Graph representing exam clashes.
 * Vertices are Subjects; Edges exist if at least one student is enrolled in both subjects.
 * Provides both Adjacency List and Adjacency Matrix representations for algorithm execution,
 * alongside complete graph metrics (vertex count, edge count, degrees, max degree, density).
 */
public class ConflictGraph {
    private final List<Subject> vertices;
    private final Map<Subject, Integer> subjectToIndex;
    private final Map<Integer, Set<Integer>> adjacencyList;
    private final boolean[][] adjacencyMatrix;
    private final Map<String, ConflictEdge> edges;

    public ConflictGraph(List<Subject> subjects) {
        if (subjects == null) {
            throw new IllegalArgumentException("Subjects list cannot be null");
        }
        // Deduplicate subjects while preserving deterministic order
        Set<Subject> unique = new LinkedHashSet<>(subjects);
        this.vertices = new ArrayList<>(unique);
        int n = this.vertices.size();

        this.subjectToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            subjectToIndex.put(vertices.get(i), i);
        }

        this.adjacencyList = new HashMap<>();
        for (int i = 0; i < n; i++) {
            adjacencyList.put(i, new TreeSet<>()); // Sorted neighbors by index
        }

        this.adjacencyMatrix = new boolean[n][n];
        this.edges = new HashMap<>();
    }

    /**
     * Adds an undirected conflict edge between two vertices.
     */
    public void addEdge(ConflictEdge edge) {
        if (edge == null) return;
        Integer u = subjectToIndex.get(edge.getSubjectA());
        Integer v = subjectToIndex.get(edge.getSubjectB());

        if (u == null || v == null) {
            throw new IllegalArgumentException("Edge endpoints must exist in graph vertices");
        }
        if (u.equals(v)) {
            return; // No self-loops in conflict graph
        }

        adjacencyList.get(u).add(v);
        adjacencyList.get(v).add(u);
        adjacencyMatrix[u][v] = true;
        adjacencyMatrix[v][u] = true;

        String key = getEdgeKey(u, v);
        ConflictEdge existing = edges.get(key);
        if (existing == null) {
            edges.put(key, edge);
        } else {
            // Merge students if edge already added
            for (Student s : edge.getConflictingStudents()) {
                existing.addStudent(s);
            }
        }
    }

    private String getEdgeKey(int u, int v) {
        int min = Math.min(u, v);
        int max = Math.max(u, v);
        return min + "-" + max;
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public Subject getSubject(int vertexIndex) {
        if (vertexIndex < 0 || vertexIndex >= vertices.size()) {
            throw medicalBounds(vertexIndex);
        }
        return vertices.get(vertexIndex);
    }

    public int getVertexIndex(Subject subject) {
        Integer idx = subjectToIndex.get(subject);
        if (idx == null) {
            throw new NoSuchElementException("Subject not in graph: " + subject);
        }
        return idx;
    }

    public List<Subject> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public Set<Integer> getNeighbors(int vertexIndex) {
        Set<Integer> neighbors = adjacencyList.get(vertexIndex);
        if (neighbors == null) {
            throw medicalBounds(vertexIndex);
        }
        return Collections.unmodifiableSet(neighbors);
    }

    public boolean hasConflict(int u, int v) {
        if (u < 0 || u >= vertices.size() || v < 0 || v >= vertices.size()) {
            return false;
        }
        return adjacencyMatrix[u][v];
    }

    public ConflictEdge getEdge(int u, int v) {
        return edges.get(getEdgeKey(u, v));
    }

    public Collection<ConflictEdge> getAllEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    public int getDegree(int vertexIndex) {
        Set<Integer> neighbors = adjacencyList.get(vertexIndex);
        return (neighbors == null) ? 0 : neighbors.size();
    }

    public int getMaxDegree() {
        int max = 0;
        for (int i = 0; i < vertices.size(); i++) {
            max = Math.max(max, getDegree(i));
        }
        return max;
    }

    /**
     * Calculates the graph density: 2 * |E| / (|V| * (|V| - 1)).
     * Density is 0.0 for edgeless graphs and 1.0 for a complete graph (K_n).
     */
    public double getDensity() {
        int v = vertices.size();
        if (v <= 1) return 0.0;
        long maxEdges = (long) v * (v - 1) / 2;
        return (double) edges.size() / maxEdges;
    }

    public boolean[][] getAdjacencyMatrix() {
        // Return defensive copy
        int n = vertices.size();
        boolean[][] copy = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(adjacencyMatrix[i], 0, copy[i], 0, n);
        }
        return copy;
    }

    private IndexOutOfBoundsException medicalBounds(int idx) {
        return new IndexOutOfBoundsException("Vertex index " + idx + " out of bounds for V=" + vertices.size());
    }

    @Override
    public String toString() {
        return "ConflictGraph{vertices=" + getVertexCount() + ", edges=" + getEdgeCount() + 
               ", maxDegree=" + getMaxDegree() + ", density=" + String.format("%.2f", getDensity()) + '}';
    }
}
