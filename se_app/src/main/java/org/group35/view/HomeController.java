package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private LineChart<String, Number> spendingChart;
    @FXML private PieChart categoryPieChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spendingChart.getStyleClass().add("chart");
        categoryPieChart.getStyleClass().add("pie-chart");
        insertDemoData();
    }

    /**
     * Clears and sets the spending line chart with the given series.
     * @param series the data series to display
     */
    public void setSpendingChartData(XYChart.Series<String, Number> series) {
        spendingChart.getData().clear();
        spendingChart.getData().add(series);
        spendingChart.setTitle(null);
        spendingChart.setCreateSymbols(false);
        spendingChart.setLegendVisible(false);
    }

    /**
     * Clears and sets the category pie chart with the given data.
     * @param data the list of PieChart.Data to display
     */
    public void setCategoryPieData(List<PieChart.Data> data) {
        categoryPieChart.getData().clear();
        categoryPieChart.getData().addAll(data);
        categoryPieChart.setLegendVisible(false);
    }

    /**
     * Inserts demo data into both charts.
     * You can remove or replace this call when injecting real data.
     */
    public void insertDemoData() {
        // Demo for line chart
        XYChart.Series<String, Number> demoSeries = new XYChart.Series<>();
        demoSeries.setName("Spending Distribution");
        demoSeries.getData().addAll(
                new XYChart.Data<>("Jan", 50),
                new XYChart.Data<>("Feb", 420),
                new XYChart.Data<>("Mar", 200),
                new XYChart.Data<>("Apr", 470),
                new XYChart.Data<>("May", 100),
                new XYChart.Data<>("Jun", 850),
                new XYChart.Data<>("Jul", 270),
                new XYChart.Data<>("Aug", 500)
        );
        setSpendingChartData(demoSeries);

        // Demo for pie chart
        List<PieChart.Data> demoPie = Arrays.asList(
                new PieChart.Data("Food", 30),
                new PieChart.Data("Transport", 20),
                new PieChart.Data("Entertainment", 25),
                new PieChart.Data("Others", 25)
        );
        setCategoryPieData(demoPie);
    }
}
