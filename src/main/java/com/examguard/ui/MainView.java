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
    private final Label playbackStatusLabel;
    private final Label stepCounterLabel;
    private final Label explanationBanner;

    // Checkbox mapping for dynamic subject filtering
    private final Map<Subject, CheckBox> subjectCheckboxes = new LinkedHashMap<>();

    // State tracking
    private ColoringResult latestResult;
    private boolean isInstantSolved = false;

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
        this.playBtn = new Button("▶ Play");
        this.pauseBtn = new Button("⏸ Pause");
        this.stepBtn = new Button("⏭ Step");
        this.resetBtn = new Button("↺ Reset");
        this.speedSlider = new Slider(150, 1200, 600);
        this.playbackStatusLabel = new Label("Status: Ready");
        this.playbackStatusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        this.stepCounterLabel = new Label("Step 0 / 0");
        this.explanationBanner = new Label("Ready to solve. Choose an algorithm and click 'Run & Animate' or 'Instant Solve'.");

        setupUI();
        setupEventHandlers();

        // Initialize canvas with default graph
        canvas.setGraph(activeGraph);
        updatePlaybackControls();
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

        playBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        pauseBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        stepBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
        resetBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");

        Label speedLabel = new Label("Speed:");
        speedLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        speedLabel.setTextFill(Color.web("#64748B"));
        speedSlider.setPrefWidth(100);

        stepCounterLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        stepCounterLabel.setTextFill(Color.web("#0F172A"));

        playbackBar.getChildren().addAll(playBtn, pauseBtn, stepBtn, resetBtn, playbackStatusLabel, stepCounterLabel, speedLabel, speedSlider);

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
            explanationBanner.setText(step.getExplanation());
            updatePlaybackControls();
        });

        animator.setOnFinished(() -> {
            explanationBanner.setText("Animation complete! Final conflict-free timetable verified.");
            if (latestResult != null) {
                timetablePanel.updateSchedule(latestResult.toSchedule(activeGraph), activeGraph, algorithmSelector.getValue());
                canvas.updateState(latestResult.getAssignments(), -1, -1);
            }
            updatePlaybackControls();
        });

        animator.setOnReset(() -> {
            explanationBanner.setText("Animation reset to initial uncolored state. Ready to replay.");
            updatePlaybackControls();
        });

        // Speed slider inverted: higher value on slider -> faster (shorter duration)
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double duration = 1350.0 - newVal.doubleValue();
            animator.setStepDurationMs(duration);
        });

        // Playback buttons
        playBtn.setOnAction(e -> {
            isInstantSolved = false;
            animator.play();
            updatePlaybackControls();
        });

        pauseBtn.setOnAction(e -> {
            animator.pause();
            updatePlaybackControls();
        });

        stepBtn.setOnAction(e -> {
            isInstantSolved = false;
            animator.stepForward();
            updatePlaybackControls();
        });

        resetBtn.setOnAction(e -> {
            isInstantSolved = false;
            animator.reset();
            canvas.resetColors();
            timetablePanel.invalidate("Animation reset to initial state. Ready to replay.");
            explanationBanner.setText("Animation reset to initial uncolored state. Ready to replay.");
            updatePlaybackControls();
        });

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
        isInstantSolved = false;

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

        explanationBanner.setText("Active graph rebuilt with " + selected.size() + " subjects and " +
                activeGraph.getEdgeCount() + " conflict edges. Ready to solve.");
        updatePlaybackControls();
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
            isInstantSolved = false;
            animator.loadTrace(latestResult.getTrace());
            timetablePanel.invalidate("Algorithm animation in progress...");
            animator.play();
            updatePlaybackControls();
        } else {
            isInstantSolved = true;
            animator.pause();
            animator.loadTrace(latestResult.getTrace());
            canvas.updateState(latestResult.getAssignments(), -1, -1);
            timetablePanel.updateSchedule(latestResult.toSchedule(activeGraph), activeGraph, algoChoice);
            explanationBanner.setText(String.format("Instant solve complete (%s). %d timeslots used. Zero conflicts verified ✓",
                    algoChoice, latestResult.getColorsUsed()));
            updatePlaybackControls();
        }
    }

    private void updatePlaybackControls() {
        boolean isPlaying = animator.isPlaying();
        boolean isCompleted = isInstantSolved || animator.isCompleted();
        boolean isAtStepZero = (animator.getCurrentStepIndex() == 0);
        boolean hasTrace = (animator.getTrace() != null && !animator.getTrace().getSteps().isEmpty());
        boolean hasNextStep = animator.hasNextStep();
        boolean isInitialState = animator.isInitialState() && !isInstantSolved;

        // Update step counter label
        int total = animator.getTotalSteps();
        if (isInstantSolved && total > 0) {
            stepCounterLabel.setText("Step " + total + " / " + total);
        } else {
            stepCounterLabel.setText("Step " + animator.getCurrentStepIndex() + " / " + total);
        }

        // Distinct visible status text and styling (not relying on color alone)
        if (isPlaying) {
            playbackStatusLabel.setText("Status: Playing ▶");
            playbackStatusLabel.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-border-color: #93C5FD; -fx-border-radius: 4;");
        } else if (isCompleted) {
            playbackStatusLabel.setText("Status: Completed ✓");
            playbackStatusLabel.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-border-color: #6EE7B7; -fx-border-radius: 4;");
        } else if (hasTrace && !isAtStepZero) {
            playbackStatusLabel.setText("Status: Paused ⏸");
            playbackStatusLabel.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-border-color: #FCD34D; -fx-border-radius: 4;");
        } else {
            playbackStatusLabel.setText("Status: Ready");
            playbackStatusLabel.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4; -fx-border-color: #CBD5E1; -fx-border-radius: 4;");
        }

        // Guaranteed state-driven button enable/disable properties
        if (isPlaying) {
            // While actively running: prevent duplicate starts, allow pause or reset
            playBtn.setDisable(true);
            pauseBtn.setDisable(false);
            stepBtn.setDisable(true);
            resetBtn.setDisable(false);
            runAnimateBtn.setDisable(true);
            instantSolveBtn.setDisable(true);
        } else if (isCompleted) {
            // When completed: Reset remains available, new runs can be started
            playBtn.setDisable(true);
            pauseBtn.setDisable(true);
            stepBtn.setDisable(true);
            resetBtn.setDisable(false);
            runAnimateBtn.setDisable(false);
            instantSolveBtn.setDisable(false);
        } else if (hasTrace && !isAtStepZero) {
            // Paused mid-animation: prevent duplicate runs while allowing resume/step/reset
            playBtn.setDisable(!hasNextStep);
            pauseBtn.setDisable(true);
            stepBtn.setDisable(!hasNextStep);
            resetBtn.setDisable(false);
            runAnimateBtn.setDisable(true); // Prevents duplicate run while paused mid-animation
            instantSolveBtn.setDisable(false);
        } else {
            // In initial state or reset to step 0
            playBtn.setDisable(!hasNextStep);
            pauseBtn.setDisable(true);
            stepBtn.setDisable(!hasNextStep);
            resetBtn.setDisable(isInitialState); // Disabled only if already in initial uncolored state
            runAnimateBtn.setDisable(activeGraph == null || activeGraph.getVertexCount() == 0);
            instantSolveBtn.setDisable(activeGraph == null || activeGraph.getVertexCount() == 0);
        }
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
