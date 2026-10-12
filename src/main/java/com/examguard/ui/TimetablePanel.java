package com.examguard.ui;

import com.examguard.model.ConflictGraph;
import com.examguard.model.Schedule;
import com.examguard.model.Subject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Map;

/**
 * Clean JavaFX component displaying the generated exam timetable grouped by timeslot,
 * featuring an automated conflict verification badge confirming zero clashes.
 */
public class TimetablePanel extends VBox {

    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Label statusBadge;
    private final VBox cardsContainer;
    private final ScrollPane scrollPane;

    public TimetablePanel() {
        setSpacing(12);
        setPadding(new Insets(16));
        setPrefWidth(320);
        setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        // Header Title
        titleLabel = new Label("Exam Timetable");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#0F172A"));
        titleLabel.setStyle("-fx-text-fill: #0F172A;");

        subtitleLabel = new Label("Slots: 0 | Active Subjects: 0");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        subtitleLabel.setTextFill(Color.web("#334155"));
        subtitleLabel.setStyle("-fx-text-fill: #334155;");

        // Status Badge (Pill)
        statusBadge = new Label("Awaiting Solution...");
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusBadge.setTextFill(Color.WHITE);
        statusBadge.setPadding(new Insets(4, 12, 4, 12));
        setStatusBadgeStyle("#475569"); // Dark slate

        // Cards Container inside ScrollPane
        cardsContainer = new VBox(10);
        cardsContainer.setPadding(new Insets(4));

        scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(titleLabel, subtitleLabel, statusBadge, scrollPane);

        invalidate("Initial state. Run an algorithm to generate a timetable.");
    }

    private void setStatusBadgeStyle(String backgroundColor) {
        statusBadge.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 14; -fx-padding: 4 12 4 12;",
                backgroundColor
        ));
    }

    /**
     * Updates the timetable view with the new schedule and validates it against the active graph.
     */
    public void updateSchedule(Schedule schedule, ConflictGraph graph, String algorithmName) {
        cardsContainer.getChildren().clear();

        if (schedule == null || graph == null || graph.getVertexCount() == 0) {
            invalidate("No subjects available.");
            return;
        }

        int totalSlots = schedule.getTotalSlotsUsed();
        int totalSubjects = graph.getVertexCount();
        subtitleLabel.setText(String.format("%s | Slots: %d | Subjects: %d",
                (algorithmName != null ? algorithmName : "Solved"), totalSlots, totalSubjects));

        boolean isValid = schedule.isValid(graph);
        if (isValid) {
            statusBadge.setText("Zero Conflicts Verified ✓");
            setStatusBadgeStyle("#059669"); // Strong high-contrast emerald green
        } else {
            statusBadge.setText("Conflicts Detected ✗");
            setStatusBadgeStyle("#DC2626"); // Crimson red
        }

        Map<Integer, List<Subject>> slots = schedule.getSubjectsByTimeslot();
        if (slots.isEmpty()) {
            Label emptyLbl = new Label("No slots assigned.");
            emptyLbl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
            emptyLbl.setTextFill(Color.web("#1E293B"));
            cardsContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Map.Entry<Integer, List<Subject>> entry : slots.entrySet()) {
            int slotNum = entry.getKey();
            List<Subject> subjects = entry.getValue();

            VBox card = createSlotCard(slotNum, subjects);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createSlotCard(int slotNum, List<Subject> subjects) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10, 12, 10, 12));
        Color slotColor = GraphCanvas.getColorForSlot(slotNum);
        String hexColor = toHex(slotColor);

        // Crisp white card with high-contrast borders and a distinct left stripe for slot color
        card.setStyle(String.format(
                "-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #CBD5E1 #CBD5E1 #CBD5E1 %s; " +
                "-fx-border-width: 1 1 1 5; " +
                "-fx-border-radius: 6; -fx-background-radius: 6;",
                hexColor
        ));

        // Slot Title Header with color dot
        HBox slotHeader = new HBox(8);
        slotHeader.setAlignment(Pos.CENTER_LEFT);

        Region colorDot = new Region();
        colorDot.setMinSize(14, 14);
        colorDot.setMaxSize(14, 14);
        colorDot.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 7; -fx-border-color: #64748B; -fx-border-width: 0.5; -fx-border-radius: 7;",
                hexColor
        ));

        Label slotTitle = new Label("Slot " + slotNum);
        slotTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        slotTitle.setTextFill(Color.web("#0F172A"));
        slotTitle.setStyle("-fx-text-fill: #0F172A;");

        Label countBadge = new Label("(" + subjects.size() + " exam" + (subjects.size() > 1 ? "s" : "") + ")");
        countBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        countBadge.setTextFill(Color.web("#334155"));
        countBadge.setStyle("-fx-text-fill: #334155;");

        slotHeader.getChildren().addAll(colorDot, slotTitle, countBadge);
        card.getChildren().add(slotHeader);

        // Subject items
        for (Subject s : subjects) {
            HBox itemRow = new HBox(8);
            itemRow.setAlignment(Pos.CENTER_LEFT);

            Label codeLbl = new Label(s.getCode());
            codeLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            codeLbl.setTextFill(Color.web("#0F172A"));
            codeLbl.setStyle(
                    "-fx-text-fill: #0F172A; " +
                    "-fx-background-color: #F1F5F9; " +
                    "-fx-border-color: #CBD5E1; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 4; " +
                    "-fx-background-radius: 4; " +
                    "-fx-padding: 3 8 3 8;"
            );

            Label nameLbl = new Label(s.getName());
            nameLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            nameLbl.setTextFill(Color.web("#0F172A"));
            nameLbl.setStyle("-fx-text-fill: #0F172A;");

            itemRow.getChildren().addAll(codeLbl, nameLbl);
            card.getChildren().add(itemRow);
        }

        return card;
    }

    /**
     * Resets the timetable view and displays invalidation notice
     * when the subject subset or active graph changes.
     */
    public void invalidate(String reason) {
        cardsContainer.getChildren().clear();
        statusBadge.setText("Awaiting Solution...");
        setStatusBadgeStyle("#475569");
        subtitleLabel.setText("Selection updated. Run algorithm to solve.");

        VBox noticeBox = new VBox(8);
        noticeBox.setPadding(new Insets(16, 8, 16, 8));
        noticeBox.setAlignment(Pos.CENTER);

        Label noticeIcon = new Label("ℹ");
        noticeIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        noticeIcon.setTextFill(Color.web("#475569"));

        Label noticeMsg = new Label(reason != null ? reason : "Graph modified. Re-run algorithm to update schedule.");
        noticeMsg.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        noticeMsg.setTextFill(Color.web("#0F172A")); // Dark readable text
        noticeMsg.setWrapText(true);
        noticeMsg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        noticeBox.getChildren().addAll(noticeIcon, noticeMsg);
        cardsContainer.getChildren().add(noticeBox);
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
