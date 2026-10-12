package com.examguard;

import com.examguard.db.DatabaseManager;
import com.examguard.ui.BenchmarkView;
import com.examguard.ui.DataManagementView;
import com.examguard.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Main JavaFX Application entry point for ExamGuard.
 * Coordinates SQLite persistence, live state invalidation across tabs,
 * and hosts the 3 core views:
 * 1. Timetable & Animation (MainView)
 * 2. Algorithm Benchmark (BenchmarkView)
 * 3. Data Management (DataManagementView)
 */
public class App extends Application {

    private DatabaseManager databaseManager;

    @Override
    public void start(Stage primaryStage) {
        // Initialize persistent SQLite database (auto-creates schema & seeds default university scenario)
        databaseManager = new DatabaseManager();

        // 1. Initialize Views
        MainView mainView = new MainView(databaseManager);
        BenchmarkView benchmarkView = new BenchmarkView(databaseManager);
        DataManagementView dataManagementView = new DataManagementView(databaseManager);

        // 2. Wire live state synchronization:
        // Any CRUD or enrollment change in Data Management immediately rebuilds the active graph,
        // refreshes the benchmark tab, and invalidates stale timetable/animation states in Tab 1.
        dataManagementView.setOnDataChanged(() -> {
            mainView.reloadFromDatabase(databaseManager);
            benchmarkView.refreshActiveGraph();
        });

        // 3. Assemble Root Navigation TabPane
        TabPane rootTabs = new TabPane();
        rootTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        rootTabs.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px;");

        Tab tab1 = new Tab("Timetable & Animation", mainView);
        Tab tab2 = new Tab("Algorithm Benchmark", benchmarkView);
        Tab tab3 = new Tab("Data Management", dataManagementView);

        rootTabs.getTabs().addAll(tab1, tab2, tab3);

        Scene scene = new Scene(rootTabs, 1200, 740);

        primaryStage.setTitle("ExamGuard \u2014 Exam Timetable Conflict Minimizer (DAA Project)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
