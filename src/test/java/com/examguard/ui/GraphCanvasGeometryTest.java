package com.examguard.ui;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphCanvasGeometryTest {

    @Test
    @DisplayName("computeVertexPosition places first vertex at 12 o'clock (top center)")
    void testFirstVertexAtTwelveOClock() {
        double width = 600.0;
        double height = 500.0;
        int totalVertices = 6;

        Point2D pos0 = GraphCanvas.computeVertexPosition(0, totalVertices, width, height);
        double cx = width / 2.0;
        double cy = height / 2.0;
        double expectedRadius = Math.min(cx, cy) - 55.0;

        assertEquals(cx, pos0.getX(), 1e-4, "Top vertex X should match canvas center X");
        assertEquals(cy - expectedRadius, pos0.getY(), 1e-4, "Top vertex Y should be cy - radius");
    }

    @Test
    @DisplayName("All vertices lie on circle of computed radius R")
    void testAllVerticesOnCircle() {
        double width = 600.0;
        double height = 600.0;
        int totalVertices = 8;
        double cx = width / 2.0;
        double cy = height / 2.0;
        double expectedRadius = Math.min(cx, cy) - 55.0;

        for (int i = 0; i < totalVertices; i++) {
            Point2D pt = GraphCanvas.computeVertexPosition(i, totalVertices, width, height);
            double dist = Math.hypot(pt.getX() - cx, pt.getY() - cy);
            assertEquals(expectedRadius, dist, 1e-4, "Distance of vertex " + i + " from center must equal radius R");
        }
    }

    @Test
    @DisplayName("Color palette maps slots 1..8 and wraps cleanly")
    void testColorPaletteMapping() {
        Color slot1 = GraphCanvas.getColorForSlot(1);
        Color slot2 = GraphCanvas.getColorForSlot(2);
        assertNotEquals(slot1, slot2, "Slot 1 and Slot 2 should have distinct colors");

        Color slot9 = GraphCanvas.getColorForSlot(9); // 9 % 8 == 1
        assertEquals(slot1, slot9, "Slot 9 should wrap cleanly to slot 1 color");

        Color slot0 = GraphCanvas.getColorForSlot(0);
        assertEquals(GraphCanvas.DEFAULT_NODE_FILL, slot0, "Unassigned slot 0 returns default uncolored fill");
    }

    @Test
    @DisplayName("Empty or negative vertex count handles gracefully without throwing")
    void testEdgeCaseVertexCounts() {
        assertDoesNotThrow(() -> {
            Point2D pt = GraphCanvas.computeVertexPosition(0, 0, 500.0, 500.0);
            assertNotNull(pt);
            assertEquals(250.0, pt.getX());
            assertEquals(250.0, pt.getY());
        });
    }
}
