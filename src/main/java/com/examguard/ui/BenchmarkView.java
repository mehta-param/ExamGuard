package com.examguard.ui;

import com.examguard.benchmark.BenchmarkMetric;
import com.examguard.benchmark.BenchmarkPresets;
import com.examguard.benchmark.BenchmarkService;
import com.examguard.db.DatabaseManager;
import com.examguard.model.ConflictGraph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

/**
 * JavaFX View providing empirical benchmarking comparing Welsh-Powell Greedy
 * and Recursive Backtracking on the exact same ConflictGraph instance.
 */
public class BenchmarkView extends BorderPane {

    private final DatabaseManager db;
    private final BenchmarkService benchmarkService = new BenchmarkService();

    private ComboBox<String> scenarioCombo;
    private Label verticesValLabel;
    private Label edgesValLabel;
    private Label maxDegreeValLabel;
    private Label densityValLabel;

    // Welsh-Powell card labels
    private Label wpTimeLabel;
    private Label wpStepsLabel;
    private Label wpSlotsLabel;
    private Label wpStatusLabel;
    private Label wpValidBadge;

    // Backtracking card labels
    private Label btTimeLabel;
    private Label btStepsLabel;
    private Label btSlotsLabel;
    private Label btStatusLabel;
    private Label btValidBadge;

    private ConflictGraph currentBenchmarkGraph;

    public BenchmarkView(DatabaseManager db) {
        this.db = db;
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createTopHeaderAndControls());
        setCenter(createCenterContent());

        // Default to active database graph
        loadScenario("Active Database Graph");
        runBenchmark();
    }

    private Pane createTopHeaderAndControls() {
        VBox topBox = new VBox(12);
        topBox.setPadding(new Insets(16, 20, 14, 20));
        topBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("DAA Algorithm Benchmarking & Empirical Analysis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #0F172A;");

        Label subtitle = new Label("Compare polynomial-time greedy heuristics against exhaustive recursive backtracking on the exact same graph instance.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setStyle("-fx-text-fill: #64748B;");

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label selectLbl = new Label("Select Scenario:");
        selectLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        selectLbl.setStyle("-fx-text-fill: #0F172A;");

        scenarioCombo = new ComboBox<>();
        scenarioCombo.getItems().addAll(
                "Active Database Graph",
                "Small University Scenario (6 Subjects)",
                "Medium Department Scenario (10 Subjects)",
                "Dense / Clash-Heavy Scenario (8 Subjects)"
        );
        scenarioCombo.setValue("Active Database Graph");
        scenarioCombo.setStyle("-fx-font-size: 13px; -fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 6;");
        scenarioCombo.setOnAction(e -> {
            loadScenario(scenarioCombo.getValue());
            runBenchmark();
        });

        Button runBtn = new Button("Run Benchmark \u25B6");
        runBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 8 18; -fx-background-radius: 6;");
        runBtn.setOnAction(e -> runBenchmark());

        controls.getChildren().addAll(selectLbl, scenarioCombo, runBtn);

        topBox.getChildren().addAll(title, subtitle, controls);
        return topBox;
    }

    private javafx.scene.Node createCenterContent() {
        VBox centerBox = new VBox(16);
        centerBox.setPadding(new Insets(16, 20, 20, 20));

        // 1. Graph Metrics Bar
        centerBox.getChildren().add(createGraphMetricsBar());

        // 2. Comparison Cards
        centerBox.getChildren().add(createComparisonCards());

        // 3. Theoretical Tradeoff Panel
        centerBox.getChildren().add(createTradeoffPanel());

        ScrollPane scrollPane = new ScrollPane(centerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8FAFC;");
        return scrollPane;
    }

    private Pane createGraphMetricsBar() {
        HBox bar = new HBox(24);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label label = new Label("Graph Topology:");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        label.setStyle("-fx-text-fill: #0F172A;");

        verticesValLabel = createMetricPill("Vertices (V)", "0");
        edgesValLabel = createMetricPill("Edges (E)", "0");
        maxDegreeValLabel = createMetricPill("Max Degree \u0394(G)", "0");
        densityValLabel = createMetricPill("Graph Density", "0.000");

        bar.getChildren().addAll(label, verticesValLabel, edgesValLabel, maxDegreeValLabel, densityValLabel);
        return bar;
    }

    private Label createMetricPill(String title, String defaultVal) {
        Label lbl = new Label(title + ": " + defaultVal);
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lbl.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0F172A; -fx-padding: 4 10; -fx-border-radius: 6; -fx-background-radius: 6;");
        return lbl;
    }

    private Pane createComparisonCards() {
        HBox cardsBox = new HBox(16);
        cardsBox.setAlignment(Pos.TOP_CENTER);

        // Welsh-Powell Card
        VBox wpCard = createAlgorithmCard(
                "Welsh-Powell Greedy",
                "Polynomial-time heuristic \u2022 O(V\u00B2 + E)",
                "#1D4ED8",
                true
        );
        HBox.setHgrow(wpCard, Priority.ALWAYS);

        // Backtracking Card
        VBox btCard = createAlgorithmCard(
                "Recursive Backtracking",
                "Exhaustive branch-and-bound \u2022 O(m\u1D58)",
                "#059669",
                false
        );
        HBox.setHgrow(btCard, Priority.ALWAYS);

        cardsBox.getChildren().addAll(wpCard, btCard);
        return cardsBox;
    }

    private VBox createAlgorithmCard(String algoName, String subtitle, String accentColor, boolean isWp) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Header
        VBox head = new VBox(2);
        Label titleLbl = new Label(algoName);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLbl.setStyle("-fx-text-fill: " + accentColor + ";");

        Label subLbl = new Label(subtitle);
        subLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        subLbl.setStyle("-fx-text-fill: #64748B;");
        head.getChildren().addAll(titleLbl, subLbl);

        Separator sep = new Separator();

        // Metrics Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        Label timeTitle = new Label("Execution Time:");
        timeTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        Label timeVal = new Label("—");
        timeVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        timeVal.setStyle("-fx-text-fill: #0F172A;");

        Label stepsTitle = new Label("Trace Steps:");
        stepsTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        Label stepsVal = new Label("—");
        stepsVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        stepsVal.setStyle("-fx-text-fill: #0F172A;");

        Label slotsTitle = new Label("Timeslots Required:");
        slotsTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        Label slotsVal = new Label("—");
        slotsVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        slotsVal.setStyle("-fx-text-fill: #0F172A;");

        grid.add(timeTitle, 0, 0); grid.add(timeVal, 1, 0);
        grid.add(stepsTitle, 0, 1); grid.add(stepsVal, 1, 1);
        grid.add(slotsTitle, 0, 2); grid.add(slotsVal, 1, 2);

        // Status badges
        Label validBadge = new Label("Zero Conflicts Verified \u2713");
        validBadge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;");

        Label statusBadge = new Label("Status: Awaiting");
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusBadge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0F172A; -fx-padding: 6 12; -fx-background-radius: 6;");

        if (isWp) {
            wpTimeLabel = timeVal;
            wpStepsLabel = stepsVal;
            wpSlotsLabel = slotsVal;
            wpValidBadge = validBadge;
            wpStatusLabel = statusBadge;
        } else {
            btTimeLabel = timeVal;
            btStepsLabel = stepsVal;
            btSlotsLabel = slotsVal;
            btValidBadge = validBadge;
            btStatusLabel = statusBadge;
        }

        card.getChildren().addAll(head, sep, grid, validBadge, statusBadge);
        return card;
    }

    private Pane createTradeoffPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("DAA Theoretical Trade-off Analysis (Viva Defense Summary)");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #0F172A;");

        Label text1 = new Label("\u2022 Welsh-Powell Greedy sorts vertices in descending degree order and applies a first-fit greedy choice. It executes in predictable O(V\u00B2) time, rapidly producing a valid upper bound. However, it is an approximation heuristic and may use more timeslots than mathematically necessary.");
        text1.setWrapText(true);
        text1.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px;");

        Label text2 = new Label("\u2022 Recursive Backtracking systematically explores the coloring state space using branch-and-bound pruning. It guarantees finding the exact minimum chromatic number \u03C7(G). However, in dense graphs, its search tree experiences exponential O(m\u1D58) branching, demonstrating why NP-hard graph coloring relies on greedy heuristics for large instances.");
        text2.setWrapText(true);
        text2.setStyle("-fx-text-fill: #334155; -fx-font-size: 12px;");

        panel.getChildren().addAll(title, text1, text2);
        return panel;
    }

    private void loadScenario(String scenarioName) {
        if ("Small University Scenario (6 Subjects)".equals(scenarioName)) {
            currentBenchmarkGraph = BenchmarkPresets.getSmallScenario();
        } else if ("Medium Department Scenario (10 Subjects)".equals(scenarioName)) {
            currentBenchmarkGraph = BenchmarkPresets.getMediumScenario();
        } else if ("Dense / Clash-Heavy Scenario (8 Subjects)".equals(scenarioName)) {
            currentBenchmarkGraph = BenchmarkPresets.getDenseScenario();
        } else {
            // Active Database Graph
            currentBenchmarkGraph = db.loadActiveConflictGraph();
        }

        updateGraphMetrics();
    }

    private void updateGraphMetrics() {
        if (currentBenchmarkGraph == null) return;

        verticesValLabel.setText("Vertices (V): " + currentBenchmarkGraph.getVertexCount());
        edgesValLabel.setText("Edges (E): " + currentBenchmarkGraph.getEdgeCount());
        maxDegreeValLabel.setText("Max Degree \u0394(G): " + currentBenchmarkGraph.getMaxDegree());
        densityValLabel.setText(String.format("Graph Density: %.3f", currentBenchmarkGraph.getDensity()));
    }

    public void runBenchmark() {
        if (currentBenchmarkGraph == null) {
            loadScenario(scenarioCombo.getValue());
        }

        Map<String, BenchmarkMetric> results = benchmarkService.runComparison(currentBenchmarkGraph);

        // Update Welsh-Powell
        BenchmarkMetric wp = results.get(BenchmarkService.ALGO_WELSH_POWELL);
        if (wp != null) {
            wpTimeLabel.setText(wp.getFormattedTime());
            wpStepsLabel.setText(String.valueOf(wp.getTotalSteps()));
            wpSlotsLabel.setText(wp.getTimeslotsUsed() + " Timeslots");
            wpValidBadge.setText(wp.isConflictFree() ? "Zero Conflicts Verified \u2713" : "Conflict Detected \u2717");
            wpValidBadge.setStyle(wp.isConflictFree()
                    ? "-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;"
                    : "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;");

            wpStatusLabel.setText(wp.getStatus().getDisplayLabel());
            wpStatusLabel.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 6;");
        }

        // Update Backtracking
        BenchmarkMetric bt = results.get(BenchmarkService.ALGO_BACKTRACKING);
        if (bt != null) {
            btTimeLabel.setText(bt.getFormattedTime());
            btStepsLabel.setText(String.valueOf(bt.getTotalSteps()));
            btSlotsLabel.setText(bt.getTimeslotsUsed() + " Timeslots");
            btValidBadge.setText(bt.isConflictFree() ? "Zero Conflicts Verified \u2713" : "Conflict Detected \u2717");
            btValidBadge.setStyle(bt.isConflictFree()
                    ? "-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;"
                    : "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6;");

            btStatusLabel.setText(bt.getStatus().getDisplayLabel());
            if (bt.getStatus() == BenchmarkMetric.OutcomeStatus.PROVEN_MINIMUM) {
                btStatusLabel.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 6;");
            } else if (bt.getStatus() == BenchmarkMetric.OutcomeStatus.INTERRUPTED_SEARCH) {
                btStatusLabel.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 6;");
            } else {
                btStatusLabel.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 6;");
            }
        }
    }

    public void refreshActiveGraph() {
        if ("Active Database Graph".equals(scenarioCombo.getValue())) {
            loadScenario("Active Database Graph");
            runBenchmark();
        }
    }
}
