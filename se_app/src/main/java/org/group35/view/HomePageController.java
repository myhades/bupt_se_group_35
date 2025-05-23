package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.LogUtils;

//import java.awt.event.ActionEvent;
import javafx.event.ActionEvent;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HomePageController implements Initializable {

    @FXML private LineChart<String, Number> spendingChart;
    @FXML private PieChart categoryPieChart;
    @FXML private Button toggleIncomeExpenseBtn;
    @FXML private NumberAxis yAxis;

    private boolean showIncome = true; // 默认显示收入

    // txManager instance
    private TransactionManager txManager;
    private User currentUser;

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
        currentUser = ApplicationRuntime.getInstance().getCurrentUser();

        // Fetch all transactions for the current user
        List<Transaction> transactions = txManager.getByUser(currentUser.getUsername());

        // Update charts with real data
        updateCharts(transactions);
    }

    @FXML
    private void onToggleIncomeExpense(ActionEvent event) {
        showIncome = !showIncome;
        toggleIncomeExpenseBtn.setText((showIncome ? "Income " : "Expense"));

//        String currentUser = ApplicationRuntime.getInstance().getCurrentUser().getUsername();
        List<Transaction> transactions = txManager.getByUser(currentUser.getUsername());

        // 可选：根据收支状态过滤交易
        List<Transaction> filtered = transactions.stream()
                .filter(tx -> showIncome ? tx.getAmount().compareTo(BigDecimal.ZERO) > 0
                        : tx.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .toList();

        updateCharts(filtered);
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
        applyCategoryBasedColoring();
        setupYAxis(transactions);
    }

    /**
     * Dynamically adjusts the Y-axis tick unit and range based on current data.
     */
    private void setupYAxis(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            yAxis.setLabel("Amount");
            yAxis.setTickUnit(100);
            yAxis.setAutoRanging(true);
            return;
        }

        double maxAmount = transactions.stream()
            .map(Transaction::getAmount)
            .map(BigDecimal::abs)
            .mapToDouble(BigDecimal::doubleValue)
            .max()
            .orElse(1000);

        double minAmount = transactions.stream()
            .map(Transaction::getAmount)
            .mapToDouble(BigDecimal::doubleValue)
            .min()
            .orElse(0);

        double tickUnit;

        if (maxAmount-minAmount < 100) {
            tickUnit = 20;
        } else if (maxAmount-minAmount < 500) {
            tickUnit = 50;
        } else if (maxAmount-minAmount < 1000) {
            tickUnit = 100;
        } else if (maxAmount-minAmount < 5000) {
            tickUnit = 500;
        } else {
            tickUnit = 1000;
        }

        yAxis.setLabel("Amount");
        yAxis.setAutoRanging(true);
        yAxis.setTickUnit(tickUnit);
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

            BigDecimal amount = tx.getAmount();
            if (!showIncome && amount.compareTo(BigDecimal.ZERO) < 0) {
                amount = amount.abs();
            }
            if (showIncome && amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            monthlySpending.put(month, monthlySpending.getOrDefault(month, BigDecimal.ZERO).add(amount));
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

        List<String> validCategories = currentUser.getCategory();

//        for (String category : validCategories) {
//            /// Group by category and sum amount
//            List<Transaction> txsInCategory = txManager.getByCategory(category);
//            BigDecimal totalAmount = txsInCategory.stream()
//                    .map(Transaction::getAmount)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
//                categoryTotals.put(category, totalAmount);
//            }
//        }
//
//        List<PieChart.Data> pieData = new ArrayList<>();
//        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
//            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
//        }
        for (Transaction tx : transactions) {
            String category = tx.getCategory();
            if (category == null || category.trim().isEmpty()) {
                continue; // 跳过无类别的交易
            }

            BigDecimal amount = tx.getAmount();

            // 根据 showIncome 决定是否保留该交易
            if ((showIncome && amount.compareTo(BigDecimal.ZERO) <= 0) ||
                    (!showIncome && amount.compareTo(BigDecimal.ZERO) >= 0)) {
                continue; // 跳过不符合条件的交易
            }

            categoryTotals.put(category, categoryTotals.getOrDefault(category, BigDecimal.ZERO).add(amount.abs())); // 统计绝对值用于饼图展示
        }

        // 转换为 PieChart.Data 列表
        List<PieChart.Data> pieData = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            String categoryName = entry.getKey();
            double total = entry.getValue().doubleValue();
            pieData.add(new PieChart.Data(categoryName, total));
        }

        return pieData;
    }

    /**
     * Applies dynamic coloring to the pie chart based on category's first letter.
     */
    public void applyCategoryBasedColoring() {
        double innerRadiusPercent = 0.4;
        categoryPieChart.setLegendVisible(false);
        categoryPieChart.setStyle("-fx-pie-chart-inner-radius: " + innerRadiusPercent + ";");

        categoryPieChart.getData().forEach(data -> {
            String categoryName = data.getName();
            if (categoryName != null && !categoryName.isEmpty()) {
                char letterChar = categoryName.toUpperCase().charAt(0);
                data.getNode().setStyle("-fx-pie-color: " + toRGBCode(getColorForLetter(letterChar)) + ";");
            } else {
                LogUtils.warn("Invalid category found for categoryName: " + categoryName);
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