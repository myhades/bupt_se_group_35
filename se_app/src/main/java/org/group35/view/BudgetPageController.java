package org.group35.view;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.group35.runtime.ApplicationRuntime;

public class BudgetPageController {

    // 预算相关控件
    @FXML private Label totalBudgetLabel;
    @FXML private Label usedLabel;
    @FXML private Label usedPercentLabel;
    @FXML private Label availableLabel;
    @FXML private Label availablePercentLabel;
    @FXML private PieChart budgetPieChart;
    @FXML private Button editBudgetButton;

    // 近期消费历史表格
    @FXML private TableView<SpendingEntry> recentSpendingTable;
    @FXML private TableColumn<SpendingEntry, String> dateColumn;
    @FXML private TableColumn<SpendingEntry, String> categoryColumn;
    @FXML private TableColumn<SpendingEntry, Double> amountColumn;
    @FXML private TableColumn<SpendingEntry, String> descriptionColumn;

    // 其他按钮
    @FXML private Button recommendationButton;
    @FXML private Button AIButton;

    // 预算数据
    private double totalBudget = 1800; // 默认总预算
    private double usedAmount = 1600;  // 已使用金额

    // 示例消费记录数据
    private ObservableList<SpendingEntry> spendingData = FXCollections.observableArrayList(
            new SpendingEntry("2025-05-18", "Groceries", 85.25, "Weekly grocery shopping"),
            new SpendingEntry("2025-05-17", "Dining", 42.80, "Dinner with friends"),
            new SpendingEntry("2025-05-15", "Transportation", 25.00, "Gas refill"),
            new SpendingEntry("2025-05-14", "Entertainment", 15.99, "Movie tickets"),
            new SpendingEntry("2025-05-12", "Utilities", 120.50, "Electricity bill")
    );

    @FXML
    public void initialize() {
        // 初始化预算显示
        updateBudgetDisplay();

        // 初始化表格
        setupSpendingTable();

        // 设置饼图样式
        budgetPieChart.getStyleClass().add("pie-chart");

        // 设置按钮样式
        recommendationButton.setWrapText(true);
    }

    private void setupSpendingTable() {
        // 配置表格列
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 设置金额列的自定义单元格格式
        amountColumn.setCellFactory(column -> new TableCell<SpendingEntry, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        // 添加数据到表格
        recentSpendingTable.setItems(spendingData);
    }

    /**
     *更新预算显示
     */
    private void updateBudgetDisplay() {
        // 计算可用金额和百分比
        double availableAmount = totalBudget - usedAmount;
        double availablePercentage = (availableAmount / totalBudget) * 100;
        double usedPercentage = (usedAmount / totalBudget) * 100;

        // 更新标签
        totalBudgetLabel.setText("$" + String.format("%.0f", totalBudget));
        availableLabel.setText("$" + String.format("%.0f", availableAmount));
        availablePercentLabel.setText(String.format("%.0f%%", availablePercentage));
        usedLabel.setText("$" + String.format("%.0f", usedAmount));
        usedPercentLabel.setText(String.format("%.0f%%", usedPercentage));

        // 更新饼图
        List<PieChart.Data> pieData = Arrays.asList(
                new PieChart.Data("Used", usedPercentage),
                new PieChart.Data("Available", availablePercentage)
        );
        budgetPieChart.getData().clear();
        budgetPieChart.getData().addAll(pieData);

        // 设置饼图颜色 - 确保在添加数据后设置
        Platform.runLater(() -> {
            if (budgetPieChart.getData().size() >= 2) {
                budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
                budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
            }
        });
    }

    /**
     * 显示预算编辑对话框
     */
    @FXML
    private void showEditBudgetDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/group35/view/EditBudgetDialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Budget");
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/group35/util/assets/monora_icon.png")));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(dialogRoot));

            EditBudgetDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // 设置当前预算值
            controller.setNewBudget(String.valueOf(totalBudget));

            // 显示对话框并等待关闭
            dialogStage.showAndWait();

            // 如果用户确认了更改，则更新预算
            if (controller.isConfirmed()) {
                try {
                    totalBudget = Double.parseDouble(controller.getNewBudget());
                    updateBudgetDisplay();
                } catch (NumberFormatException e) {
                    // 处理数字格式错误
                    System.err.println("Invalid budget format: " + controller.getNewBudget());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void gotoRecommendation(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.RECOMMENDATION);
    }

    @FXML
    private void gotoAISuggestion(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.AI_SUGGESTION);
    }

    /**
     * 内部类：消费记录条目
     */
    public static class SpendingEntry {
        private final String date;
        private final String category;
        private final double amount;
        private final String description;

        public SpendingEntry(String date, String category, double amount, String description) {
            this.date = date;
            this.category = category;
            this.amount = amount;
            this.description = description;
        }

        public String getDate() { return date; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }
}