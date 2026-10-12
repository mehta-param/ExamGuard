package com.examguard.ui;

import com.examguard.algorithm.AlgorithmStep;
import com.examguard.algorithm.ExecutionTrace;
import com.examguard.algorithm.StepType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller that animates an Algorithm ExecutionTrace step-by-step
 * using a JavaFX Timeline, keeping the algorithms pure and the UI decoupled.
 */
public class GraphAnimator {

    private final GraphCanvas canvas;
    private ExecutionTrace trace;
    private int currentStepIndex = 0;
    private final Map<Integer, Integer> currentColors = new HashMap<>();

    private Timeline timeline;
    private double stepDurationMs = 600.0;

    private Consumer<AlgorithmStep> onStepChanged;
    private Runnable onFinished;
    private Runnable onReset;

    public GraphAnimator(GraphCanvas canvas) {
        this.canvas = canvas;
        initTimeline();
    }

    private void initTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(stepDurationMs), event -> {
            if (hasNextStep()) {
                stepForward();
            } else {
                pause();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void setStepDurationMs(double ms) {
        this.stepDurationMs = Math.max(100.0, ms);
        boolean wasRunning = isPlaying();
        if (wasRunning) {
            pause();
        }
        initTimeline();
        if (wasRunning) {
            play();
        }
    }

    public void setOnStepChanged(Consumer<AlgorithmStep> callback) {
        this.onStepChanged = callback;
    }

    public void setOnFinished(Runnable callback) {
        this.onFinished = callback;
    }

    public void setOnReset(Runnable callback) {
        this.onReset = callback;
    }

    public void loadTrace(ExecutionTrace newTrace) {
        pause();
        this.trace = newTrace;
        this.currentStepIndex = 0;
        this.currentColors.clear();
        if (canvas != null) {
            canvas.resetColors();
        }
        if (onReset != null) {
            onReset.run();
        }
    }

    public boolean hasNextStep() {
        return trace != null && currentStepIndex < trace.getSteps().size();
    }

    public boolean hasPreviousStep() {
        return currentStepIndex > 0;
    }

    public void play() {
        if (trace == null || trace.getSteps().isEmpty()) {
            return;
        }
        if (!hasNextStep()) {
            reset();
        }
        timeline.play();
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    public boolean isPlaying() {
        return timeline != null && timeline.getStatus() == Timeline.Status.RUNNING;
    }

    /**
     * Executes the next discrete algorithm step and updates the canvas.
     */
    public boolean stepForward() {
        if (!hasNextStep()) {
            return false;
        }

        AlgorithmStep step = trace.getSteps().get(currentStepIndex);
        currentStepIndex++;

        applyStepEffect(step);

        if (canvas != null) {
            canvas.updateState(currentColors, step.getVertexIndex(), step.getConflictingNeighborIndex());
        }

        if (onStepChanged != null) {
            onStepChanged.accept(step);
        }

        if (!hasNextStep() && onFinished != null) {
            pause();
            onFinished.run();
        }

        return true;
    }

    /**
     * Resets animation to step 0.
     */
    public void reset() {
        pause();
        this.currentStepIndex = 0;
        this.currentColors.clear();
        if (canvas != null) {
            canvas.resetColors();
        }
        if (onReset != null) {
            onReset.run();
        }
    }

    private void applyStepEffect(AlgorithmStep step) {
        if (step.getType() == StepType.ASSIGN_COLOR) {
            currentColors.put(step.getVertexIndex(), step.getColor());
        } else if (step.getType() == StepType.BACKTRACK) {
            currentColors.remove(step.getVertexIndex());
        }
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public int getTotalSteps() {
        return (trace != null) ? trace.getSteps().size() : 0;
    }

    public boolean isCompleted() {
        return trace != null && !trace.getSteps().isEmpty() && currentStepIndex >= trace.getSteps().size();
    }

    public boolean isInitialState() {
        return currentStepIndex == 0 && currentColors.isEmpty();
    }

    public ExecutionTrace getTrace() {
        return trace;
    }

    public Map<Integer, Integer> getCurrentColors() {
        return new HashMap<>(currentColors);
    }
}
