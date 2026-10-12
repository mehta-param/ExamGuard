package com.examguard.ui;

import com.examguard.model.ConflictEdge;
import com.examguard.model.ConflictGraph;
import com.examguard.model.Subject;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JavaFX Canvas for rendering the exam conflict graph in an educational circular layout.
 * <p>
 * Circular trigonometry positions vertices around a circle:
 * theta_i = (2 * PI * i / N) - (PI / 2)
 * x_i = cx + R * cos(theta_i)
 * y_i = cy + R * sin(theta_i)
 */
public class GraphCanvas extends Canvas {

    public static final double NODE_RADIUS = 24.0;

    // Harmonious distinct colors for timeslots (Slot 1 to 8)
    public static final Color[] SLOT_PALETTE = {
            Color.web("#3498DB"), // 1: Sky Blue
            Color.web("#E67E22"), // 2: Amber Orange
            Color.web("#2ECC71"), // 3: Emerald Green
            Color.web("#9B59B6"), // 4: Amethyst Purple
            Color.web("#E74C3C"), // 5: Crimson Red
            Color.web("#F1C40F"), // 6: Sunflower Yellow
            Color.web("#1ABC9C"), // 7: Turquoise
            Color.web("#E91E63")  // 8: Coral Pink
    };

    public static final Color DEFAULT_NODE_FILL = Color.web("#34495E");
    public static final Color ACTIVE_RING_COLOR = Color.web("#F39C12");
    public static final Color CLASH_COLOR = Color.web("#C0392B");
    public static final Color DEFAULT_EDGE_COLOR = Color.web("#BDC3C7");

    private ConflictGraph currentGraph;
    private final Map<Integer, Integer> vertexColors = new HashMap<>();
    private int activeVertexIndex = -1;
    private int conflictingVertexIndex = -1;

    public GraphCanvas(double width, double height) {
        super(width, height);
        // Clear background
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#F8F9FA"));
        gc.fillRect(0, 0, width, height);
    }

    /**
     * Pure trigonometry helper for vertex positioning. Accessible for headless unit tests.
     */
    public static Point2D computeVertexPosition(int index, int totalVertices, double width, double height) {
        if (totalVertices <= 0) {
            return new Point2D(width / 2.0, height / 2.0);
        }
        double cx = width / 2.0;
        double cy = height / 2.0;
        double radius = Math.min(cx, cy) - 55.0;
        if (radius < 30.0) {
            radius = 30.0;
        }

        double theta = (2.0 * Math.PI * index / totalVertices) - (Math.PI / 2.0);
        double x = cx + radius * Math.cos(theta);
        double y = cy + radius * Math.sin(theta);
        return new Point2D(x, y);
    }

    public static Color getColorForSlot(int slotNumber) {
        if (slotNumber <= 0) {
            return DEFAULT_NODE_FILL;
        }
        int index = (slotNumber - 1) % SLOT_PALETTE.length;
        return SLOT_PALETTE[index];
    }

    public void setGraph(ConflictGraph graph) {
        this.currentGraph = graph;
        this.vertexColors.clear();
        this.activeVertexIndex = -1;
        this.conflictingVertexIndex = -1;
        redraw();
    }

    public ConflictGraph getGraph() {
        return currentGraph;
    }

    public void updateState(Map<Integer, Integer> colors, int activeIdx, int conflictIdx) {
        this.vertexColors.clear();
        if (colors != null) {
            this.vertexColors.putAll(colors);
        }
        this.activeVertexIndex = activeIdx;
        this.conflictingVertexIndex = conflictIdx;
        redraw();
    }

    public void setFinalColors(Map<Subject, Integer> subjectColorMap) {
        this.vertexColors.clear();
        this.activeVertexIndex = -1;
        this.conflictingVertexIndex = -1;
        if (currentGraph != null && subjectColorMap != null) {
            for (Map.Entry<Subject, Integer> entry : subjectColorMap.entrySet()) {
                try {
                    int idx = currentGraph.getVertexIndex(entry.getKey());
                    this.vertexColors.put(idx, entry.getValue());
                } catch (Exception ignored) {
                }
            }
        }
        redraw();
    }

    public void resetColors() {
        this.vertexColors.clear();
        this.activeVertexIndex = -1;
        this.conflictingVertexIndex = -1;
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Clear background
        gc.setFill(Color.web("#F8F9FA"));
        gc.fillRect(0, 0, w, h);

        if (currentGraph == null || currentGraph.getVertices().isEmpty()) {
            gc.setFill(Color.web("#7F8C8D"));
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No subjects selected. Select subjects to view conflict graph.", w / 2.0, h / 2.0);
            return;
        }

        List<Subject> vertices = currentGraph.getVertices();
        int n = vertices.size();

        // Precalculate positions for each subject
        Map<Integer, Point2D> positions = new HashMap<>();
        for (int i = 0; i < n; i++) {
            positions.put(i, computeVertexPosition(i, n, w, h));
        }

        // 2. Draw conflict edges
        Collection<ConflictEdge> edges = currentGraph.getAllEdges();
        for (ConflictEdge edge : edges) {
            int u = currentGraph.getVertexIndex(edge.getSubjectA());
            int v = currentGraph.getVertexIndex(edge.getSubjectB());
            Point2D p1 = positions.get(u);
            Point2D p2 = positions.get(v);
            if (p1 == null || p2 == null) continue;

            boolean isClash = (activeVertexIndex >= 0 && conflictingVertexIndex >= 0)
                    && ((u == activeVertexIndex && v == conflictingVertexIndex)
                    || (u == conflictingVertexIndex && v == activeVertexIndex));

            if (isClash) {
                gc.setStroke(CLASH_COLOR);
                gc.setLineWidth(3.5);
            } else {
                gc.setStroke(DEFAULT_EDGE_COLOR);
                gc.setLineWidth(1.5);
            }
            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }

        // 3. Draw vertices (nodes)
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < n; i++) {
            Subject s = vertices.get(i);
            Point2D pos = positions.get(i);
            double px = pos.getX();
            double py = pos.getY();

            // Determine fill color based on assigned timeslot
            Integer slot = vertexColors.get(i);
            Color fillColor = (slot != null && slot > 0) ? getColorForSlot(slot) : DEFAULT_NODE_FILL;

            // Base node circle
            gc.setFill(fillColor);
            gc.fillOval(px - NODE_RADIUS, py - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            // Active or conflict highlight rings
            if (i == activeVertexIndex) {
                gc.setStroke(ACTIVE_RING_COLOR);
                gc.setLineWidth(4.0);
                gc.strokeOval(px - NODE_RADIUS - 3, py - NODE_RADIUS - 3, (NODE_RADIUS + 3) * 2, (NODE_RADIUS + 3) * 2);
            } else if (i == conflictingVertexIndex) {
                gc.setStroke(CLASH_COLOR);
                gc.setLineWidth(3.0);
                gc.strokeOval(px - NODE_RADIUS - 2, py - NODE_RADIUS - 2, (NODE_RADIUS + 2) * 2, (NODE_RADIUS + 2) * 2);
            } else {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.8);
                gc.strokeOval(px - NODE_RADIUS, py - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            }

            // Subject label text
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.fillText(s.getCode(), px, py + 4.0);

            // Slot badge indicator above node if colored
            if (slot != null && slot > 0) {
                gc.setFill(Color.web("#2C3E50"));
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
                gc.fillText("Slot " + slot, px, py - NODE_RADIUS - 5);
            }
        }
    }
}
