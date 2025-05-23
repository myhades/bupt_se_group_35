//package org.group35.view;
//
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.chart.LineChart;
//import javafx.scene.chart.XYChart;
//import javafx.scene.chart.PieChart;
//
//import java.net.URL;
//import java.util.Arrays;
//import java.util.List;
//import java.util.ResourceBundle;
//
//public class HomePageController implements Initializable {
//
//    @FXML private LineChart<String, Number> spendingChart;
//    @FXML private PieChart categoryPieChart;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        spendingChart.getStyleClass().add("chart");
//        categoryPieChart.getStyleClass().add("pie-chart");
//        insertDemoData();
//    }
//
//    /**
//     * Clears and sets the spending line chart with the given series.
//     * @param series the data series to display
//     */
//    public void setSpendingChartData(XYChart.Series<String, Number> series) {
//        spendingChart.getData().clear();
//        spendingChart.getData().add(series);
//        spendingChart.setTitle(null);
//        spendingChart.setCreateSymbols(false);
//        spendingChart.setLegendVisible(false);
//    }
//
//    /**
//     * Clears and sets the category pie chart with the given data.
//     * @param data the list of PieChart.Data to display
//     */
//    public void setCategoryPieData(List<PieChart.Data> data) {
//        categoryPieChart.getData().clear();
//        categoryPieChart.getData().addAll(data);
//        categoryPieChart.setLegendVisible(false);
//    }
//
//    /**
//     * Inserts demo data into both charts.
//     * You can remove or replace this call when injecting real data.
//     */
//    public void insertDemoData() {
//        // Demo for line chart
//        XYChart.Series<String, Number> demoSeries = new XYChart.Series<>();
//        demoSeries.setName("Spending Distribution");
//        demoSeries.getData().addAll(
//                new XYChart.Data<>("Jan", 50),
//                new XYChart.Data<>("Feb", 420),
//                new XYChart.Data<>("Mar", 200),
//                new XYChart.Data<>("Apr", 470),
//                new XYChart.Data<>("May", 100),
//                new XYChart.Data<>("Jun", 850),
//                new XYChart.Data<>("Jul", 270),
//                new XYChart.Data<>("Aug", 500)
//        );
//        setSpendingChartData(demoSeries);
//
//        // Demo for pie chart
//        List<PieChart.Data> demoPie = Arrays.asList(
//                new PieChart.Data("Food", 30),
//                new PieChart.Data("Transport", 20),
//                new PieChart.Data("Entertainment", 25),
//                new PieChart.Data("Others", 25)
//        );
//        setCategoryPieData(demoPie);
//    }
//}

package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HomePageController implements Initializable {

    @FXML private LineChart<String, Number> spendingChart;
    @FXML private PieChart categoryPieChart;

    // txManager instance
    private TransactionManager txManager;

    // Add this near the top of the class
    private static final Color[] COLOR_PALETTE = {
            Color.web("#2E86AB"),
            Color.web("#1B4F72"),
            Color.web("#7B241C"),
            Color.web("#633974"),
            Color.web("#117A65"),
            Color.web("#B03A2E"),
            Color.web("#5D6D7E"),
            Color.web("#4A235A"),
            Color.web("#1D8348"),
            Color.web("#7E5109"),
            Color.web("#4B4B4B"),
            Color.web("#1F2A44")
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize txManager
        txManager = ApplicationRuntime.getInstance().getTranscationManager();

        // Get current user's username
        String currentUser = ApplicationRuntime.getInstance().getCurrentUser().getUsername();

        // Fetch all transactions for the current user
        List<Transaction> transactions = txManager.getByUser(currentUser);

        // Update charts with real data
        updateCharts(transactions);
    }



    /**
     * Updates both the line chart and pie chart with the given transaction data.
     *
     * @param transactions The list of transactions to visualize
     */
    private void updateCharts(List<Transaction> transactions) {
        XYChart.Series<String, Number> spendingSeries = generateSpendingChartData(transactions);
        setSpendingChartData(spendingSeries);

        List<PieChart.Data> categoryData = generateCategoryPieData(transactions);
        setCategoryPieData(categoryData);

        // Apply coloring after setting data
        applyCategoryBasedColoring();
    }

    /**
     * Generates a series for the line chart based on monthly spending.
     *
     * @param transactions List of transactions
     * @return XYChart.Series containing monthly totals
     */
    private XYChart.Series<String, Number> generateSpendingChartData(List<Transaction> transactions) {
        Map<String, BigDecimal> monthlySpending = new TreeMap<>();

        for (Transaction tx : transactions) {
            LocalDate date = tx.getTimestamp().toLocalDate();
            String month = date.format(DateTimeFormatter.ofPattern("MMM")); // e.g. Jan, Feb

            monthlySpending.put(month, monthlySpending.getOrDefault(month, BigDecimal.ZERO).add(tx.getAmount()));
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Spending");

        for (Map.Entry<String, BigDecimal> entry : monthlySpending.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        return series;
    }

    /**
     * Generates pie chart data grouped by category with color based on category's first letter.
     *
     * @param transactions List of transactions
     * @return List of PieChart.Data entries with assigned colors
     */
    private List<PieChart.Data> generateCategoryPieData(List<Transaction> transactions) {
        Map<String, BigDecimal> categoryTotals = new HashMap<>();

        // Group by category and sum amount
        for (Transaction tx : transactions) {
            String category = tx.getCategory();
            if (category != null && !category.trim().isEmpty()) {
                categoryTotals.put(category, categoryTotals.getOrDefault(category, BigDecimal.ZERO).add(tx.getAmount()));
            }
        }

        List<PieChart.Data> pieData = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }

        return pieData;
    }

    /**
     * Applies dynamic coloring to the pie chart based on category's first letter.
     */
    public void applyCategoryBasedColoring() {
        categoryPieChart.getData().forEach(data -> {
            String categoryName = data.getName();
            if (categoryName != null && !categoryName.isEmpty()) {
                char letterChar = categoryName.toUpperCase().charAt(0);
                data.getNode().setStyle("-fx-pie-color: " + toRGBCode(getColorForLetter(letterChar)) + ";");
            } else {
                data.getNode().setStyle("-fx-pie-color: #555555;");
            }
        });
    }

    /**
     * Converts a JavaFX Color to an RGB hex string suitable for CSS.
     */
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    /**
     * Clears and sets the spending line chart with the given series.
     *
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
     *
     * @param data the list of PieChart.Data to display
     */
    public void setCategoryPieData(List<PieChart.Data> data) {
        categoryPieChart.getData().clear();
        categoryPieChart.getData().addAll(data);
        categoryPieChart.setLegendVisible(false);
    }

    /**
     * Returns a Color for letters A–Z;
     * anything else falls back to mid-gray.
     */
    private Color getColorForLetter(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            int idx = (ch - 'A') % COLOR_PALETTE.length;
            return COLOR_PALETTE[idx];
        }
        return Color.web("#555555");
    }
}