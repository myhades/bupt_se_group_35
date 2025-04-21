package org.group35.view;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;

public class HomeController {

    @FXML
    private LineChart<String, Number> spendingChart;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    public void initialize() {
        // Apply styles to the charts after they are initialized
        spendingChart.getStyleClass().add("chart");
        categoryPieChart.getStyleClass().add("pie-chart");

        // Initialize Spending Distribution Chart with fake data
        XYChart.Series<String, Number> spendingSeries = new XYChart.Series<>();
        spendingSeries.setName("Spending Distribution");

        spendingSeries.getData().add(new XYChart.Data<>("Jan", 350));
        spendingSeries.getData().add(new XYChart.Data<>("Feb", 420));
        spendingSeries.getData().add(new XYChart.Data<>("Mar", 300));
        spendingSeries.getData().add(new XYChart.Data<>("Apr", 470));
        spendingSeries.getData().add(new XYChart.Data<>("May", 380));
        spendingSeries.getData().add(new XYChart.Data<>("Jun", 450));
        spendingSeries.getData().add(new XYChart.Data<>("Jul", 500));

        spendingChart.getData().add(spendingSeries);

        // Initialize Category Pie Chart with fake data
        PieChart.Data foodData = new PieChart.Data("Food", 30);
        PieChart.Data transportData = new PieChart.Data("Transport", 20);
        PieChart.Data entertainmentData = new PieChart.Data("Entertainment", 25);
        PieChart.Data othersData = new PieChart.Data("Others", 25);

        categoryPieChart.getData().addAll(foodData, transportData, entertainmentData, othersData);
    }
}
