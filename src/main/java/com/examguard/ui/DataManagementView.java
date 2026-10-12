package com.examguard.ui;

import com.examguard.db.DatabaseManager;
import com.examguard.model.Student;
import com.examguard.model.Subject;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX View for managing database entities (Subjects, Students, Enrollments)
 * with instant SQLite persistence and conflict graph invalidation callbacks.
 */
public class DataManagementView extends BorderPane {

    private final DatabaseManager db;
    private Runnable onDataChanged;

    private final ObservableList<Subject> subjectsData = FXCollections.observableArrayList();
    private final ObservableList<Student> studentsData = FXCollections.observableArrayList();

    private TableView<Subject> subjectsTable;
    private TableView<Student> studentsTable;
    private ListView<Student> enrollmentStudentList;
    private VBox enrollmentCheckboxesBox;
    private Label enrollmentHeaderLabel;
    private Student currentEnrollmentStudent;

    public DataManagementView(DatabaseManager db) {
        this.db = db;
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createTopHeader());
        setCenter(createTabPane());
        setBottom(createBottomBar());

        refreshAll();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private Pane createTopHeader() {
        VBox headerBox = new VBox(4);
        headerBox.setPadding(new Insets(16, 20, 12, 20));
        headerBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Database & Entity Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #0F172A;");

        Label subtitle = new Label("Manage subjects, students, and course enrollments. All modifications persist to SQLite and automatically rebuild the conflict graph.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setStyle("-fx-text-fill: #64748B;");

        headerBox.getChildren().addAll(title, subtitle);
        return headerBox;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #F8FAFC;");

        Tab subjectsTab = new Tab("Subjects (Courses)", createSubjectsPanel());
        Tab studentsTab = new Tab("Students", createStudentsPanel());
        Tab enrollmentsTab = new Tab("Student Enrollments", createEnrollmentsPanel());

        tabPane.getTabs().addAll(subjectsTab, studentsTab, enrollmentsTab);
        return tabPane;
    }

    // --- 1. SUBJECTS PANEL ---

    private Pane createSubjectsPanel() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));

        subjectsTable = new TableView<>(subjectsData);
        subjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Subject, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        idCol.setPrefWidth(60);
        idCol.setMaxWidth(80);

        TableColumn<Subject, String> codeCol = new TableColumn<>("Subject Code");
        codeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        codeCol.setPrefWidth(140);

        TableColumn<Subject, String> nameCol = new TableColumn<>("Subject Title / Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(300);

        subjectsTable.getColumns().addAll(idCol, codeCol, nameCol);

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(12, 0, 0, 0));
        actions.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("+ Add Subject");
        addBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> handleAddSubject());

        Button editBtn = new Button("Edit Selected");
        editBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> handleEditSubject());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> handleDeleteSubject());

        actions.getChildren().addAll(addBtn, editBtn, deleteBtn);

        pane.setCenter(subjectsTable);
        pane.setBottom(actions);
        return pane;
    }

    private void handleAddSubject() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.setHeaderText("Enter subject code and title:");

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. CS201");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Computer Networks");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                showError("Validation Error", "Code and name cannot be empty.");
                return;
            }
            try {
                db.insertSubject(code, name);
                refreshAll();
                notifyDataChanged();
            } catch (Exception ex) {
                showError("Database Error", "Failed to add subject: " + ex.getMessage());
            }
        }
    }

    private void handleEditSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a subject to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Subject");
        dialog.setHeaderText("Modify subject details:");

        TextField codeField = new TextField(selected.getCode());
        TextField nameField = new TextField(selected.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                showError("Validation Error", "Code and name cannot be empty.");
                return;
            }
            try {
                db.updateSubject(selected.getId(), code, name);
                refreshAll();
                notifyDataChanged();
            } catch (Exception ex) {
                showError("Database Error", "Failed to update subject: " + ex.getMessage());
            }
        }
    }

    private void handleDeleteSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a subject to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete subject " + selected.getCode() + "?");
        confirm.setContentText("This will also remove all student enrollments for this subject via CASCADE.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            db.deleteSubject(selected.getId());
            refreshAll();
            notifyDataChanged();
        }
    }

    // --- 2. STUDENTS PANEL ---

    private Pane createStudentsPanel() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));

        studentsTable = new TableView<>(studentsData);
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Student, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()));
        idCol.setPrefWidth(60);
        idCol.setMaxWidth(80);

        TableColumn<Student, String> rollCol = new TableColumn<>("Student ID / Roll No");
        rollCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudentId()));
        rollCol.setPrefWidth(160);

        TableColumn<Student, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(260);

        studentsTable.getColumns().addAll(idCol, rollCol, nameCol);

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(12, 0, 0, 0));
        actions.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("+ Add Student");
        addBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> handleAddStudent());

        Button editBtn = new Button("Edit Selected");
        editBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> handleEditStudent());

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> handleDeleteStudent());

        actions.getChildren().addAll(addBtn, editBtn, deleteBtn);

        pane.setCenter(studentsTable);
        pane.setBottom(actions);
        return pane;
    }

    private void handleAddStudent() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student ID and name:");

        TextField idField = new TextField();
        idField.setPromptText("e.g. STU-009");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Alan Turing");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Student ID / Roll:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String roll = idField.getText().trim();
            String name = nameField.getText().trim();
            if (roll.isEmpty() || name.isEmpty()) {
                showError("Validation Error", "Student ID and name cannot be empty.");
                return;
            }
            try {
                db.insertStudent(roll, name);
                refreshAll();
                notifyDataChanged();
            } catch (Exception ex) {
                showError("Database Error", "Failed to add student: " + ex.getMessage());
            }
        }
    }

    private void handleEditStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a student to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Modify student details:");

        TextField idField = new TextField(selected.getStudentId());
        TextField nameField = new TextField(selected.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Student ID / Roll:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String roll = idField.getText().trim();
            String name = nameField.getText().trim();
            if (roll.isEmpty() || name.isEmpty()) {
                showError("Validation Error", "Student ID and name cannot be empty.");
                return;
            }
            try {
                db.updateStudent(selected.getId(), roll, name);
                refreshAll();
                notifyDataChanged();
            } catch (Exception ex) {
                showError("Database Error", "Failed to update student: " + ex.getMessage());
            }
        }
    }

    private void handleDeleteStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete student " + selected.getName() + " [" + selected.getStudentId() + "]?");
        confirm.setContentText("This will also remove all course enrollments for this student via CASCADE.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            db.deleteStudent(selected.getId());
            refreshAll();
            notifyDataChanged();
        }
    }

    // --- 3. ENROLLMENTS PANEL ---

    private Pane createEnrollmentsPanel() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(16));

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");

        // Left side: Student Selector
        VBox leftBox = new VBox(8);
        leftBox.setPadding(new Insets(0, 10, 0, 0));

        Label listLabel = new Label("Select Student:");
        listLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        listLabel.setStyle("-fx-text-fill: #0F172A;");

        enrollmentStudentList = new ListView<>(studentsData);
        enrollmentStudentList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getStudentId() + ")");
                    setStyle("-fx-text-fill: #0F172A;");
                }
            }
        });
        enrollmentStudentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentEnrollmentStudent = newVal;
            loadEnrollmentsForStudent(newVal);
        });

        VBox.setVgrow(enrollmentStudentList, Priority.ALWAYS);
        leftBox.getChildren().addAll(listLabel, enrollmentStudentList);

        // Right side: Subject Checklist
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(0, 0, 0, 10));

        enrollmentHeaderLabel = new Label("Enrolled Subjects (Select a student on left)");
        enrollmentHeaderLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        enrollmentHeaderLabel.setStyle("-fx-text-fill: #0F172A;");

        enrollmentCheckboxesBox = new VBox(8);
        enrollmentCheckboxesBox.setPadding(new Insets(12));
        enrollmentCheckboxesBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");

        ScrollPane scroll = new ScrollPane(enrollmentCheckboxesBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #FFFFFF;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        rightBox.getChildren().addAll(enrollmentHeaderLabel, scroll);

        splitPane.getItems().addAll(leftBox, rightBox);
        splitPane.setDividerPositions(0.38);

        pane.setCenter(splitPane);
        return pane;
    }

    private void loadEnrollmentsForStudent(Student student) {
        enrollmentCheckboxesBox.getChildren().clear();
        if (student == null) {
            enrollmentHeaderLabel.setText("Enrolled Subjects (Select a student on left)");
            return;
        }

        enrollmentHeaderLabel.setText("Enrolled Subjects for " + student.getName() + " [" + student.getStudentId() + "]:");

        List<Integer> enrolledIds = db.getEnrolledSubjectIds(student.getId());
        List<Subject> allSubjects = db.getAllSubjects();

        if (allSubjects.isEmpty()) {
            Label emptyLbl = new Label("No subjects exist. Add subjects in the Subjects tab first.");
            emptyLbl.setStyle("-fx-text-fill: #64748B;");
            enrollmentCheckboxesBox.getChildren().add(emptyLbl);
            return;
        }

        for (Subject s : allSubjects) {
            CheckBox cb = new CheckBox(s.getCode() + " — " + s.getName());
            cb.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px;");
            cb.setSelected(enrolledIds.contains(s.getId()));

            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                saveEnrollmentsForCurrentStudent();
            });

            enrollmentCheckboxesBox.getChildren().add(cb);
        }
    }

    private void saveEnrollmentsForCurrentStudent() {
        if (currentEnrollmentStudent == null) return;

        List<Subject> allSubjects = db.getAllSubjects();
        List<Integer> selectedSubjectIds = new ArrayList<>();

        for (int i = 0; i < enrollmentCheckboxesBox.getChildren().size(); i++) {
            if (enrollmentCheckboxesBox.getChildren().get(i) instanceof CheckBox cb) {
                if (cb.isSelected() && i < allSubjects.size()) {
                    selectedSubjectIds.add(allSubjects.get(i).getId());
                }
            }
        }

        db.setStudentEnrollments(currentEnrollmentStudent.getId(), selectedSubjectIds);
        notifyDataChanged();
    }

    // --- 4. BOTTOM BAR & DEMO RESET ---

    private Pane createBottomBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        Button resetBtn = new Button("Reset to Demo Data");
        resetBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-radius: 6;");
        resetBtn.setOnAction(e -> handleResetToDemoData());

        Label hint = new Label("Restores default 6 subjects, 8 students, and chromatic number \u03C7(G) = 3 conflicts.");
        hint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        hint.setStyle("-fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dbStatus = new Label("\u2022 Local SQLite: Connected");
        dbStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 12px;");

        bar.getChildren().addAll(resetBtn, hint, spacer, dbStatus);
        return bar;
    }

    private void handleResetToDemoData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Reset");
        confirm.setHeaderText("Reset to Demo Scenario?");
        confirm.setContentText("This will wipe all custom entities and restore the default 6-subject university scenario.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            db.resetToSampleData();
            refreshAll();
            notifyDataChanged();
        }
    }

    public void refreshAll() {
        subjectsData.setAll(db.getAllSubjects());
        studentsData.setAll(db.getAllStudents());

        if (currentEnrollmentStudent != null) {
            loadEnrollmentsForStudent(currentEnrollmentStudent);
        } else if (!studentsData.isEmpty()) {
            enrollmentStudentList.getSelectionModel().select(0);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
