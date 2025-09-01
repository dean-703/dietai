package com.courtney.dietai;

import com.courtney.dietai.ai.OpenAIService;
import com.courtney.dietai.ai.SimpleHeuristicAnalyzer;
import com.courtney.dietai.analysis.DietSummary;
import com.courtney.dietai.analysis.NutritionAnalyzer;
import com.courtney.dietai.io.CsvImporter;
import com.courtney.dietai.model.DietEntry;
import com.courtney.dietai.profile.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class App extends Application {

    private final TableView<DietEntry> table = new TableView<>();
    private final ObservableList<DietEntry> allEntries = FXCollections.observableArrayList();
    private final ObservableList<DietEntry> filteredEntries = FXCollections.observableArrayList();

    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final TextField searchField = new TextField();

    private final Label summaryLabel = new Label("No data loaded.");
    private final Label targetsLabel = new Label("Targets not set.");
    private final PieChart macroPieChart = new PieChart();
    private final BarChart<String, Number> caloriesBarChart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());

    private final TextArea aiOutputArea = new TextArea();
    private final Button analyzeButton = new Button("Analyze with AI");
    private final ProgressIndicator progress = new ProgressIndicator();

    private final OpenAIService openAIService = new OpenAIService();
    private final SimpleHeuristicAnalyzer fallbackAnalyzer = new SimpleHeuristicAnalyzer();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Label statusLabel = new Label();

    // Profile and targets persisted via Preferences
    private final ProfileStore profileStore = new ProfileStore();
    private UserProfile profile;
    private GoalSettings goals;
    private Targets targets;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Diet Analysis with AI");

        // Load profile and targets
        profile = profileStore.loadProfile();
        goals = profileStore.loadGoals();
        targets = TargetCalculator.calculate(profile, goals);

        // Load API key from env
        openAIService.setApiKey(System.getenv("OPENAI_API_KEY"));
        openAIService.setModel("gpt-4o-mini");

        setupTable();
        setupCharts();
        setupAIOutput();

        BorderPane root = new BorderPane();
        root.setTop(buildMenuBar(stage));
        root.setCenter(buildCenterSplit());
        root.setBottom(buildBottomBar());

        Scene scene = new Scene(root, 1800, 960);
        applyModernStyling(scene, root);
        stage.setScene(scene);
        stage.show();

        updateSummaryAndCharts(); // Show targets even if no data loaded
    }

    private void applyModernStyling(Scene scene, Pane root) {
        // Minimal modern styling
        root.setStyle("-fx-background-color: -fx-base;");
        summaryLabel.setStyle("-fx-font-size: 13px;");
        targetsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -fx-accent;");
        aiOutputArea.setStyle("-fx-font-size: 13px;");
        table.setStyle("-fx-font-size: 13px;");
        analyzeButton.setDefaultButton(true);
    }

    private MenuBar buildMenuBar(Stage stage) {
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open CSV...");
        openItem.setOnAction(e -> openCsv(stage));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(openItem, new SeparatorMenuItem(), exitItem);

        Menu settingsMenu = new Menu("Settings");
        MenuItem profileItem = new MenuItem("Profile & Targets...");
        profileItem.setOnAction(e -> showProfileDialog());
        settingsMenu.getItems().add(profileItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);

        return new MenuBar(fileMenu, settingsMenu, helpMenu);
    }

    private SplitPane buildCenterSplit() {
        SplitPane centerSplit = new SplitPane();
        centerSplit.setOrientation(Orientation.HORIZONTAL);
        centerSplit.getItems().add(buildLeftPane());
        centerSplit.getItems().add(buildRightPane());
        centerSplit.setDividerPositions(0.56);
        return centerSplit;
    }

    private VBox buildLeftPane() {
        VBox left = new VBox(8);
        left.setPadding(new Insets(10));

        HBox filters = new HBox(8);
        fromDatePicker.setPromptText("From date");
        toDatePicker.setPromptText("To date");
        searchField.setPromptText("Search item/meal/notes");
        Button applyFilter = new Button("Apply Filter");
        Button clearFilter = new Button("Clear");
        applyFilter.setOnAction(e -> applyFilters());
        clearFilter.setOnAction(e -> {
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            searchField.clear();
            applyFilters();
        });
        filters.getChildren().addAll(new Label("From:"), fromDatePicker,
                new Label("To:"), toDatePicker, searchField, applyFilter, clearFilter);
        filters.setAlignment(Pos.CENTER_LEFT);

        left.getChildren().addAll(filters, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return left;
    }

    private VBox buildRightPane() {
        VBox right = new VBox(10);
        right.setPadding(new Insets(10));

        TitledPane summaryPane = new TitledPane("Summary", summaryLabel);
        summaryPane.setCollapsible(false);

        TitledPane targetsPane = new TitledPane("Targets vs Actual (Averages)", targetsLabel);
        targetsPane.setCollapsible(false);

        macroPieChart.setLegendVisible(true);
        TitledPane macroPane = new TitledPane("Macro Distribution", macroPieChart);
        macroPane.setCollapsible(false);

        caloriesBarChart.setTitle("Daily Calories");
        caloriesBarChart.setLegendVisible(false);
        TitledPane caloriesPane = new TitledPane("Daily Calories", caloriesBarChart);
        caloriesPane.setCollapsible(false);

        aiOutputArea.setWrapText(true);
        aiOutputArea.setEditable(false);
        TitledPane aiPane = new TitledPane("AI Insights", aiOutputArea);
        aiPane.setCollapsible(false);

        right.getChildren().addAll(summaryPane, targetsPane, macroPane, caloriesPane, aiPane);
        VBox.setVgrow(caloriesPane, Priority.SOMETIMES);
        VBox.setVgrow(aiPane, Priority.ALWAYS);
        return right;
    }

    private HBox buildBottomBar() {
        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(6));
        bottom.setAlignment(Pos.CENTER_LEFT);

        analyzeButton.setOnAction(e -> analyzeWithAI());
        analyzeButton.setDisable(true);

        progress.setVisible(false);
        progress.setPrefSize(22, 22);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottom.getChildren().addAll(analyzeButton, progress, spacer, statusLabel);
        return bottom;
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DietEntry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setMinWidth(110);

        TableColumn<DietEntry, String> mealCol = new TableColumn<>("Meal");
        mealCol.setCellValueFactory(new PropertyValueFactory<>("meal"));

        TableColumn<DietEntry, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("item"));

        TableColumn<DietEntry, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityOrDuration"));

        TableColumn<DietEntry, Double> calCol = new TableColumn<>("Calories");
        calCol.setCellValueFactory(new PropertyValueFactory<>("calories"));

        TableColumn<DietEntry, Double> carbsCol = new TableColumn<>("Carbs (g)");
        carbsCol.setCellValueFactory(new PropertyValueFactory<>("carbs"));

        TableColumn<DietEntry, Double> proteinCol = new TableColumn<>("Protein (g)");
        proteinCol.setCellValueFactory(new PropertyValueFactory<>("protein"));

        TableColumn<DietEntry, Double> fatCol = new TableColumn<>("Fat (g)");
        fatCol.setCellValueFactory(new PropertyValueFactory<>("fat"));

        TableColumn<DietEntry, Double> fiberCol = new TableColumn<>("Fiber (g)");
        fiberCol.setCellValueFactory(new PropertyValueFactory<>("fiber"));

        TableColumn<DietEntry, Double> sodiumCol = new TableColumn<>("Sodium (mg)");
        sodiumCol.setCellValueFactory(new PropertyValueFactory<>("sodiumMg"));

        TableColumn<DietEntry, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(dateCol, mealCol, itemCol, qtyCol, calCol, carbsCol, proteinCol, fatCol, fiberCol, sodiumCol, notesCol);
        table.setItems(filteredEntries);
    }

    private void setupCharts() {
        macroPieChart.setLegendVisible(true);
    }

    private void setupAIOutput() {
        aiOutputArea.setPromptText("AI insights will appear here after analysis.");
    }

    private void openCsv(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Diet CSV");
        chooser.setInitialDirectory(new File("/home/dean/Documents/DietCSV"));
        //chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        statusLabel.setText("Importing CSV...");
        Task<List<DietEntry>> task = new Task<>() {
            @Override
            protected List<DietEntry> call() throws Exception {
                return CsvImporter.importFile(file);
            }
        };
        task.setOnSucceeded(e -> {
            allEntries.setAll(task.getValue());
            applyFilters();
            statusLabel.setText("Loaded " + allEntries.size() + " entries from " + file.getName());
            analyzeButton.setDisable(allEntries.isEmpty());
            aiOutputArea.clear();
        });
        task.setOnFailed(e -> {
            statusLabel.setText("Failed to import CSV.");
            showError("Import Error", "Could not import CSV.", task.getException());
        });
        new Thread(task, "csv-import").start();
    }

    private void applyFilters() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String query = Optional.ofNullable(searchField.getText()).orElse("").trim().toLowerCase();

        filteredEntries.setAll(allEntries.stream().filter(entry -> {
            boolean okDate = true;
            if (from != null) okDate &= !entry.getDate().isBefore(from);
            if (to != null) okDate &= !entry.getDate().isAfter(to);
            boolean okSearch = query.isEmpty()
                    || safe(entry.getItem()).contains(query)
                    || safe(entry.getMeal()).contains(query)
                    || safe(entry.getNotes()).contains(query);
            return okDate && okSearch;
        }).collect(Collectors.toList()));

        updateSummaryAndCharts();
    }

    private String safe(String s) { return s == null ? "" : s.toLowerCase(); }

    private void updateSummaryAndCharts() {
        // Update targets regardless of data
        targets = TargetCalculator.calculate(profile, goals);

        if (filteredEntries.isEmpty()) {
            summaryLabel.setText("No data to summarize.");
            targetsLabel.setText(buildTargetsText(null, targets));
            macroPieChart.getData().setAll();
            caloriesBarChart.getData().setAll();
            return;
        }
        DietSummary summary = NutritionAnalyzer.summarize(filteredEntries);

        summaryLabel.setText(buildSummaryText(summary));
        targetsLabel.setText(buildTargetsText(summary, targets));

        // Macro pie
        macroPieChart.getData().setAll(
                new PieChart.Data("Protein", summary.getMacroPctProtein()),
                new PieChart.Data("Carbs", summary.getMacroPctCarbs()),
                new PieChart.Data("Fat", summary.getMacroPctFat())
        );

        // Daily calories bar chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        summary.getDailyCalories().forEach((date, cals) ->
                series.getData().add(new XYChart.Data<>(date.toString(), cals)));
        caloriesBarChart.getData().setAll(series);
    }

    private String buildSummaryText(DietSummary s) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Date range: %s to %s (%d days)\n", s.getStartDate(), s.getEndDate(), s.getDaysCount()));
        sb.append(String.format(Locale.US, "Entries: %d\n", s.getEntriesCount()));
        sb.append(String.format(Locale.US, "Total calories: %.0f (avg/day %.0f)\n", s.getTotalCalories(), s.getAvgCaloriesPerDay()));
        sb.append(String.format(Locale.US, "Average/day: Protein %.1fg, Carbs %.1fg, Fat %.1fg\n",
                s.getAvgProteinPerDayG(), s.getAvgCarbsPerDayG(), s.getAvgFatPerDayG()));
        sb.append(String.format(Locale.US, "Fiber avg/day: %.1fg, Sodium avg/day: %.0fmg\n",
                s.getAvgFiberPerDayG(), s.getAvgSodiumPerDayMg()));
        sb.append(String.format(Locale.US, "Macro calories: Protein %.0f%%, Carbs %.0f%%, Fat %.0f%%",
                s.getMacroPctProtein(), s.getMacroPctCarbs(), s.getMacroPctFat()));
        return sb.toString();
    }

    private String buildTargetsText(DietSummary s, Targets t) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US,
                "Profile: %s, %d y, %.1f kg, %.0f cm, Activity: %s\n",
                profile.getSex(), profile.getAge(), profile.getWeightKg(), profile.getHeightCm(), profile.getActivityLevel()));

        sb.append(String.format(Locale.US,
                "BMR: %.0f kcal | TDEE: %.0f kcal | Calorie target: %.0f kcal\n",
                t.getBmr(), t.getTdee(), t.getCalorieTarget()));

        sb.append(String.format(Locale.US,
                "Targets (g/day): Protein %.0f, Carbs %.0f, Fat %.0f, Fiber %.0f | Sodium %.0f mg\n",
                t.getProteinTargetG(), t.getCarbsTargetG(), t.getFatTargetG(), t.getFiberTargetG(), t.getSodiumTargetMg()));

        if (s != null && s.getDaysCount() > 0) {
            sb.append(String.format(Locale.US,
                    "Actual avg/day: Calories %.0f (%+.0f), Protein %.0fg (%+.0fg), Carbs %.0fg (%+.0fg), Fat %.0fg (%+.0fg)\n",
                    s.getAvgCaloriesPerDay(), s.getAvgCaloriesPerDay() - t.getCalorieTarget(),
                    s.getAvgProteinPerDayG(), s.getAvgProteinPerDayG() - t.getProteinTargetG(),
                    s.getAvgCarbsPerDayG(), s.getAvgCarbsPerDayG() - t.getCarbsTargetG(),
                    s.getAvgFatPerDayG(), s.getAvgFatPerDayG() - t.getFatTargetG()));
            sb.append(String.format(Locale.US,
                    "Fiber avg/day %.1fg (%+.1fg), Sodium avg/day %.0fmg (%+.0fmg)\n",
                    s.getAvgFiberPerDayG(), s.getAvgFiberPerDayG() - t.getFiberTargetG(),
                    s.getAvgSodiumPerDayMg(), s.getAvgSodiumPerDayMg() - t.getSodiumTargetMg()));
        } else {
            sb.append("Load data to compare actual averages to your targets.");
        }
        return sb.toString();
    }

    private void analyzeWithAI() {
        if (filteredEntries.isEmpty()) {
            showInfo("No Data", "Nothing to analyze. Please load a CSV and/or adjust filters.");
            return;
        }
        analyzeButton.setDisable(true);
        progress.setVisible(true);
        statusLabel.setText("Analyzing with AI...");

        DietSummary summary = NutritionAnalyzer.summarize(filteredEntries);
        Targets t = TargetCalculator.calculate(profile, goals);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("profile", profile);
        payload.put("goals", goals);
        payload.put("targets", t);
        payload.put("summary", summary);
        payload.put("note", "User-provided diet log aggregated. Targets estimated; not medical advice.");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
                String apiKey = openAIService.getApiKey();
                if (apiKey == null || apiKey.isBlank()) {
                    return fallbackAnalyzer.analyze(summary, profile, t);
                }
                try {
                    return openAIService.analyzeDiet(json);
                } catch (Exception ex) {
                    return "AI request failed (" + ex.getMessage() + "). Showing offline analysis instead:\n\n"
                            + fallbackAnalyzer.analyze(summary, profile, t);
                }
            }
        };

        task.setOnSucceeded(e -> {
            aiOutputArea.setText(task.getValue());
            statusLabel.setText("Analysis complete.");
            analyzeButton.setDisable(false);
            progress.setVisible(false);
        });
        task.setOnFailed(e -> {
            aiOutputArea.setText("Analysis failed: " + task.getException().getMessage());
            statusLabel.setText("Analysis failed.");
            analyzeButton.setDisable(false);
            progress.setVisible(false);
        });

        new Thread(task, "ai-analysis").start();
    }

    private void showProfileDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Profile & Targets");
        dialog.setHeaderText("Set your profile and goals");

        // Fields
        TextField ageField = new TextField(String.valueOf(profile.getAge()));
        ChoiceBox<UserProfile.Sex> sexBox = new ChoiceBox<>(FXCollections.observableArrayList(UserProfile.Sex.values()));
        sexBox.setValue(profile.getSex());
        TextField weightField = new TextField(String.format(Locale.US, "%.1f", profile.getWeightKg()));
        TextField heightField = new TextField(String.format(Locale.US, "%.0f", profile.getHeightCm()));
        ChoiceBox<UserProfile.ActivityLevel> activityBox = new ChoiceBox<>(FXCollections.observableArrayList(UserProfile.ActivityLevel.values()));
        activityBox.setValue(profile.getActivityLevel());

        ChoiceBox<GoalSettings.GoalMode> modeBox = new ChoiceBox<>(FXCollections.observableArrayList(GoalSettings.GoalMode.values()));
        modeBox.setValue(goals.getMode());
        TextField weeklyRateField = new TextField(String.format(Locale.US, "%.2f", goals.getWeeklyRateKg()));
        TextField proteinPerKgField = new TextField(String.format(Locale.US, "%.2f", goals.getProteinPerKg()));
        TextField fatPerKgField = new TextField(String.format(Locale.US, "%.2f", goals.getFatPerKg()));
        TextField fiberTargetField = new TextField(goals.getFiberTargetG() > 0 ? String.format(Locale.US, "%.0f", goals.getFiberTargetG()) : "");
        TextField sodiumTargetField = new TextField(goals.getSodiumTargetMg() > 0 ? String.format(Locale.US, "%.0f", goals.getSodiumTargetMg()) : "");

        Label calcBmr = new Label();
        Label calcTdee = new Label();
        Label calcCals = new Label();
        Label calcMacros = new Label();

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        int r = 0;
        grid.add(new Label("Age (years)"), 0, r); grid.add(ageField, 1, r++);
        grid.add(new Label("Sex"), 0, r); grid.add(sexBox, 1, r++);
        grid.add(new Label("Weight (kg)"), 0, r); grid.add(weightField, 1, r++);
        grid.add(new Label("Height (cm)"), 0, r); grid.add(heightField, 1, r++);
        grid.add(new Label("Activity"), 0, r); grid.add(activityBox, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        grid.add(new Label("Goal Mode"), 0, r); grid.add(modeBox, 1, r++);
        grid.add(new Label("Weekly rate (kg/week)"), 0, r); grid.add(weeklyRateField, 1, r++);
        grid.add(new Label("Protein per kg (g/kg)"), 0, r); grid.add(proteinPerKgField, 1, r++);
        grid.add(new Label("Fat per kg (g/kg)"), 0, r); grid.add(fatPerKgField, 1, r++);
        grid.add(new Label("Fiber target (g/day, optional)"), 0, r); grid.add(fiberTargetField, 1, r++);
        grid.add(new Label("Sodium target (mg/day, optional)"), 0, r); grid.add(sodiumTargetField, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);
        grid.add(new Label("Calculated:"), 0, r++);
        grid.add(calcBmr, 0, r++, 2, 1);
        grid.add(calcTdee, 0, r++, 2, 1);
        grid.add(calcCals, 0, r++, 2, 1);
        grid.add(calcMacros, 0, r++, 2, 1);

        // Live update
        Runnable updater = () -> {
            try {
                UserProfile p = new UserProfile();
                p.setAge(parseInt(ageField.getText(), 30));
                p.setSex(sexBox.getValue());
                p.setWeightKg(parseDouble(weightField.getText(), 70));
                p.setHeightCm(parseDouble(heightField.getText(), 170));
                p.setActivityLevel(activityBox.getValue());

                GoalSettings g = new GoalSettings();
                g.setMode(modeBox.getValue());
                g.setWeeklyRateKg(Math.max(0, parseDouble(weeklyRateField.getText(), 0)));
                g.setProteinPerKg(Math.max(0.5, parseDouble(proteinPerKgField.getText(), 1.6)));
                g.setFatPerKg(Math.max(0.3, parseDouble(fatPerKgField.getText(), 0.8)));
                double fiber = fiberTargetField.getText().isBlank() ? -1 : parseDouble(fiberTargetField.getText(), -1);
                double sodium = sodiumTargetField.getText().isBlank() ? -1 : parseDouble(sodiumTargetField.getText(), -1);
                g.setFiberTargetG(fiber);
                g.setSodiumTargetMg(sodium);

                Targets t = TargetCalculator.calculate(p, g);
                calcBmr.setText(String.format(Locale.US, "BMR: %.0f kcal/day", t.getBmr()));
                calcTdee.setText(String.format(Locale.US, "TDEE: %.0f kcal/day", t.getTdee()));
                calcCals.setText(String.format(Locale.US, "Calorie target: %.0f kcal/day", t.getCalorieTarget()));
                calcMacros.setText(String.format(Locale.US, "Targets: Protein %.0fg, Carbs %.0fg, Fat %.0fg, Fiber %.0fg, Sodium %.0fmg",
                        t.getProteinTargetG(), t.getCarbsTargetG(), t.getFatTargetG(), t.getFiberTargetG(), t.getSodiumTargetMg()));
            } catch (Exception ex) {
                // ignore live calc errors
            }
        };

        List<Control> controls = List.of(ageField, sexBox, weightField, heightField, activityBox,
                modeBox, weeklyRateField, proteinPerKgField, fatPerKgField, fiberTargetField, sodiumTargetField);
        controls.forEach(c -> {
            if (c instanceof TextField tf) tf.textProperty().addListener((obs, o, n) -> updater.run());
            if (c instanceof ChoiceBox<?> cb) cb.valueProperty().addListener((obs, o, n) -> updater.run());
        });
        updater.run();

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    int age = requireInRange(parseInt(ageField.getText(), 30), 13, 120, "Age");
                    double weight = requireInRange(parseDouble(weightField.getText(), 70), 30, 400, "Weight");
                    double height = requireInRange(parseDouble(heightField.getText(), 170), 120, 250, "Height");
                    double weekly = requireInRange(parseDouble(weeklyRateField.getText(), 0), 0, 1.5, "Weekly rate");
                    double proKg = requireInRange(parseDouble(proteinPerKgField.getText(), 1.6), 0.7, 3.0, "Protein per kg");
                    double fatKg = requireInRange(parseDouble(fatPerKgField.getText(), 0.8), 0.3, 1.5, "Fat per kg");

                    profile.setAge(age);
                    profile.setSex(sexBox.getValue());
                    profile.setWeightKg(weight);
                    profile.setHeightCm(height);
                    profile.setActivityLevel(activityBox.getValue());

                    goals.setMode(modeBox.getValue());
                    goals.setWeeklyRateKg(weekly);
                    goals.setProteinPerKg(proKg);
                    goals.setFatPerKg(fatKg);
                    goals.setFiberTargetG(fiberTargetField.getText().isBlank() ? -1 : parseDouble(fiberTargetField.getText(), -1));
                    goals.setSodiumTargetMg(sodiumTargetField.getText().isBlank() ? -1 : parseDouble(sodiumTargetField.getText(), -1));

                    profileStore.saveProfile(profile);
                    profileStore.saveGoals(goals);
                    targets = TargetCalculator.calculate(profile, goals);
                    updateSummaryAndCharts();
                    statusLabel.setText("Profile and targets saved.");
                    return true;
                } catch (IllegalArgumentException ex) {
                    showError("Validation Error", ex.getMessage(), null);
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private double parseDouble(String s, double def) {
        if (s == null || s.isBlank()) return def;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return def; }
    }
    private int parseInt(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return def; }
    }
    private double requireInRange(double v, double min, double max, String field) {
        if (v < min || v > max) throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        return v;
    }

    private int requireInRange(int v, int min, int max, String field) {
        if (v < min || v > max) throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        return v;
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Diet Analysis with AI");
        alert.setContentText("Import a CSV from your diet tracker, see summaries, compare against personalized targets, and get AI feedback.\nThis is educational software and not medical advice.");
        alert.showAndWait();
    }

    private void showError(String title, String header, Throwable t) {
        if (t != null) t.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(t == null ? "" : Optional.ofNullable(t.getMessage()).orElse(t.toString()));
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}