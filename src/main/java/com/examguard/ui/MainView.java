package com.examguard.ui;

import com.examguard.algorithm.*;
import com.examguard.data.SampleData;
import com.examguard.model.ConflictGraph;
import com.examguard.model.ConflictGraphBuilder;
import com.examguard.model.Schedule;
import com.examguard.model.Subject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/**
 * Main application layout assembling the top toolbar, left subject subset filter,
 * center circular conflict graph canvas with playback controls, and right timetable panel.
 */
public class MainView extends BorderPane {

    private final SampleData sampleData;
    private ConflictGraph activeGraph;

    // Components
    private final GraphCanvas canvas;
    private final GraphAnimator animator;
    private final TimetablePanel timetablePanel;

    // Controls
    private final ComboBox<String> algorithmSelector;
    private final Button runAnimateBtn;
    private final Button instantSolveBtn;

    private final Button playBtn;
    private final Button pauseBtn;
    private final Button stepBtn;
    private final Button resetBtn;
    private final Slider speedSlider;
    private final Label stepCounterLabel;
    private final Label explanationBanner;

    // Checkbox mapping for dynamic subject filtering
    private final Map<Subject, CheckBox> subjectCheckboxes = new LinkedHashMap<>();

    // Keep track of latest solved result for active selection
    private ColoringResult latestResult;

    public MainView() {
        this.sampleData = new SampleData();
        this.activeGraph = sampleData.getDefaultGraph();

        // Canvas & Animator
        this.canvas = new GraphCanvas(580, 480);
        this.animator = new GraphAnimator(canvas);
        this.timetablePanel = new TimetablePanel();

        // Top toolbar
        this.algorithmSelector = new ComboBox<>();
        this.algorithmSelector.getItems().addAll("Welsh-Powell Greedy", "Recursive Backtracking");
        this.algorithmSelector.setValue("Welsh-Powell Greedy");

        this.runAnimateBtn = new Button("▶ Run & Animate");
        this.instantSolveBtn = new Button("⚡ Instant Solve");

        // Playback bar controls
        this.playBtn = new Button("Play");
        this.pauseBtn = new Button("Pause");
        this.stepBtn = new Button("Step ❯");
        this.resetBtn = new Button("Reset ↺");
        this.speedSlider = new Slider(150, 1200, 600);
        this.stepCounterLabel = new Label("Step 0 / 0");
        this.explanationBanner = new Label("Ready to solve. Choose an algorithm and click 'Run & Animate' or 'Instant Solve'.");

        setupUI();
        setupEventHandlers();

        // Initialize canvas with default graph
        canvas.setGraph(activeGraph);
    }

    private void setupUI() {
        setStyle("-fx-background-color: #F1F5F9;");

        // 1. TOP TOOLBAR
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label appTitle = new Label("ExamGuard");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        appTitle.setTextFill(Color.web("#0F172A"));

        Label subtitle = new Label("Timetable Conflict Minimizer");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#64748B"));

        VBox titleBox = new VBox(2, appTitle, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label algoLabel = new Label("Algorithm:");
        algoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        algoLabel.setTextFill(Color.web("#334155"));

        algorithmSelector.setStyle("-fx-font-size: 12px;");

        runAnimateBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14 6 14; -fx-background-radius: 6;");
        instantSolveBtn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14 6 14; -fx-background-radius: 6;");

        topBar.getChildren().addAll(titleBox, spacer, algoLabel, algorithmSelector, runAnimateBtn, instantSolveBtn);
        setTop(topBar);

        // 2. LEFT SIDEBAR: Subject Subset Filter
        VBox leftSidebar = new VBox(10);
        leftSidebar.setPrefWidth(210);
        leftSidebar.setPadding(new Insets(16));
        leftSidebar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        Label filterTitle = new Label("Active Subjects");
        filterTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        filterTitle.setTextFill(Color.web("#1E293B"));

        Label filterDesc = new Label("Toggle to dynamically filter graph and rebuild conflicts:");
        filterDesc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        filterDesc.setTextFill(Color.web("#64748B"));
        filterDesc.setWrapText(true);

        VBox checkboxList = new VBox(8);
        checkboxList.setPadding(new Insets(4, 0, 4, 0));

        for (Subject subject : sampleData.getSubjects()) {
            CheckBox cb = new CheckBox(subject.getCode() + " (" + subject.getName() + ")");
            cb.setSelected(true);
            cb.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            cb.setStyle("-fx-text-fill: #334155;");
            subjectCheckboxes.put(subject, cb);
            checkboxList.getChildren().add(cb);
        }

        HBox selectAllRow = new HBox(8);
        Button selectAllBtn = new Button("Select All");
        Button deselectAllBtn = new Button("Deselect All");
        selectAllBtn.setStyle("-fx-font-size: 11px;");
        deselectAllBtn.setStyle("-fx-font-size: 11px;");
        selectAllBtn.setOnAction(e -> setAllSubjectsSelected(true));
        deselectAllBtn.setOnAction(e -> setAllSubjectsSelected(false));
        selectAllRow.getChildren().addAll(selectAllBtn, deselectAllBtn);

        leftSidebar.getChildren().addAll(filterTitle, filterDesc, checkboxList, selectAllRow);
        setLeft(leftSidebar);

        // 3. CENTER: Conflict Graph Canvas & Playback Bar
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(16));

        // Canvas container
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8;");
        VBox.setVgrow(canvasContainer, Priority.ALWAYS);

        // Educational Explanation Banner
        explanationBanner.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        explanationBanner.setTextFill(Color.web("#1E293B"));
        explanationBanner.setWrapText(true);
        explanationBanner.setAlignment(Pos.CENTER);
        explanationBanner.setMaxWidth(Double.MAX_VALUE);
        explanationBanner.setPadding(new Insets(8, 12, 8, 12));
        explanationBanner.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 6;");

        // Playback Bar
        HBox playbackBar = new HBox(10);
        playbackBar.setAlignment(Pos.CENTER);
        playbackBar.setPadding(new Insets(8, 12, 8, 12));
        playbackBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

        playBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        pauseBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        stepBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        resetBtn.setStyle("-fx-background-color: #94A3B8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");

        Label speedLabel = new Label("Speed:");
        speedLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        speedLabel.setTextFill(Color.web("#64748B"));
        speedSlider.setPrefWidth(100);

        stepCounterLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        stepCounterLabel.setTextFill(Color.web("#334155"));

        playbackBar.getChildren().addAll(playBtn, pauseBtn, stepBtn, resetBtn, stepCounterLabel, speedLabel, speedSlider);

        centerBox.getChildren().addAll(canvasContainer, explanationBanner, playbackBar);
        setCenter(centerBox);

        // 4. RIGHT SIDEBAR: Timetable Panel
        setRight(timetablePanel);
    }

    private void setupEventHandlers() {
        // Dynamic subject filtering listeners
        for (CheckBox cb : subjectCheckboxes.values()) {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> onSubjectSelectionChanged());
        }

        // Animator event listeners
        animator.setOnStepChanged(step -> {
            updateStepCounter();
            explanationBanner.setText(step.getExplanation());
        });

        animator.setOnFinished(() -> {
            explanationBanner.setText("Animation complete! Final conflict-free timetable verified.");
            if (latestResult != null) {
                timetablePanel.updateSchedule(latestResult.toSchedule(activeGraph), activeGraph, algorithmSelector.getValue());
                canvas.updateState(latestResult.getAssignments(), -1, -1);
            }
        });

        animator.setOnReset(() -> {
            updateStepCounter();
            explanationBanner.setText("Animation reset to initial uncolored state. Ready to replay.");
        });

        // Speed slider inverted: higher value on slider -> faster (shorter duration)
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double duration = 1350.0 - newVal.doubleValue();
            animator.setStepDurationMs(duration);
        });

        // Playback buttons
        playBtn.setOnAction(e -> animator.play());
        pauseBtn.setOnAction(e -> animator.pause());
        stepBtn.setOnAction(e -> animator.stepForward());
        resetBtn.setOnAction(e -> animator.reset());

        // Algorithm runners
        runAnimateBtn.setOnAction(e -> runAlgorithm(true));
        instantSolveBtn.setOnAction(e -> runAlgorithm(false));
    }

    /**
     * SAFEGUARD 1 & 2:
     * When subjects are selected or deselected:
     * 1. Rebuild the active conflict graph from ONLY the selected subjects and their enrollments.
     *    Never retain conflicts involving excluded subjects.
     * 2. Invalidate any previous timetable and animation state until a new solution is generated.
     */
    private void onSubjectSelectionChanged() {
        // Stop animator
        animator.pause();
        animator.loadTrace(null);

        // Filter selected subjects
        List<Subject> selected = new ArrayList<>();
        for (Map.Entry<Subject, CheckBox> entry : subjectCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }

        // Rebuild active graph from scratch with ONLY selected subjects
        activeGraph = ConflictGraphBuilder.build(selected, sampleData.getEnrollments(), sampleData.getStudents());
        latestResult = null;

        // Invalidate Canvas and Timetable
        canvas.setGraph(activeGraph);
        timetablePanel.invalidate("Subject selection updated (" + selected.size() + " active). Run algorithm to generate schedule.");

        updateStepCounter();
        explanationBanner.setText("Active graph rebuilt with " + selected.size() + " subjects and " +
                activeGraph.getEdgeCount() + " conflict edges. Ready to solve.");
    }

    private void setAllSubjectsSelected(boolean selected) {
        for (CheckBox cb : subjectCheckboxes.values()) {
            cb.setSelected(selected);
        }
    }

    private void runAlgorithm(boolean animate) {
        if (activeGraph == null || activeGraph.getVertexCount() == 0) {
            explanationBanner.setText("Cannot solve: No subjects currently selected.");
            return;
        }

        String algoChoice = algorithmSelector.getValue();
        GraphColoringAlgorithm algorithm;
        if ("Recursive Backtracking".equals(algoChoice)) {
            algorithm = new BacktrackingAlgorithm();
        } else {
            algorithm = new WelshPowellAlgorithm();
        }

        latestResult = algorithm.color(activeGraph);

        if (animate) {
            animator.loadTrace(latestResult.getTrace());
            timetablePanel.invalidate("Algorithm animation in progress...");
            animator.play();
        } else {
            animator.pause();
            animator.loadTrace(null);
            canvas.updateState(latestResult.getAssignments(), -1, -1);
            timetablePanel.updateSchedule(latestResult.toSchedule(activeGraph), activeGraph, algoChoice);
            updateStepCounter();
            explanationBanner.setText(String.format("Instant solve complete (%s). %d timeslots used. Zero conflicts verified ✓",
                    algoChoice, latestResult.getColorsUsed()));
        }
    }

    private void updateStepCounter() {
        int current = animator.getCurrentStepIndex();
        int total = animator.getTotalSteps();
        stepCounterLabel.setText("Step " + current + " / " + total);
    }

    // Accessors for headless testing
    public ConflictGraph getActiveGraph() {
        return activeGraph;
    }

    public GraphCanvas getCanvas() {
        return canvas;
    }

    public GraphAnimator getAnimator() {
        return animator;
    }

    public TimetablePanel getTimetablePanel() {
        return timetablePanel;
    }
}
