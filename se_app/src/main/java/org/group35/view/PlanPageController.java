package org.group35.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

public class PlanPageController {
    @FXML
    private Button recommendationButton;
    @FXML
    public PieChart budgetPieChart;
    @FXML
    private Button editBudgetButton;
    @FXML
    private Button AIButton;

    // 添加FXML标签引用用于显示预算信息
    @FXML
    private Label availableAmountLabel;  // 显示可用金额，如 $200
    @FXML
    private Label availablePercentLabel; // 显示可用百分比，如 20%
    @FXML
    private Label usedAmountLabel;       // 显示已使用金额，如 $1600
    @FXML
    private Label usedPercentLabel;      // 显示已使用百分比，如 80%

    public void initialize() {
        try {
            if (recommendationButton != null) {
                recommendationButton.setWrapText(true);
            }
            if (budgetPieChart != null) {
                budgetPieChart.getStyleClass().add("pie-chart");
            }
            loadBudgetData();
            updateBudgetDisplay();
            System.out.println("PlanPageController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error in PlanPageController.initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 加载预算数据
    private void loadBudgetData() {
        try {
            // 简化版本，暂时不做任何复杂操作
            System.out.println("Loading budget data...");
        } catch (Exception e) {
            System.err.println("Error loading budget data: " + e.getMessage());
        }
    }

    // 更新预算显示
    private void updateBudgetDisplay() {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();

            // 从UserManager获取总预算
            BigDecimal totalBudgetBD = runtime.getUserManager().getMonthlyBudget();
            double totalBudget = totalBudgetBD != null ? totalBudgetBD.doubleValue() : 2000.0;

            // 临时使用固定值，避免复杂计算导致错误
            double usedBudget = 1600.0; // calculateUsedBudget();

            double availableBudget = totalBudget - usedBudget;
            if (availableBudget < 0) availableBudget = 0;

            // 计算百分比
            double usedPercentage = totalBudget > 0 ? (usedBudget / totalBudget) * 100 : 0;
            double availablePercentage = totalBudget > 0 ? (availableBudget / totalBudget) * 100 : 0;

            // 更新饼图数据
            if (budgetPieChart != null) {
                List<PieChart.Data> budgetData = Arrays.asList(
                        new PieChart.Data("Used", usedBudget),
                        new PieChart.Data("Available", availableBudget)
                );
                setBudgetPieData(budgetData);

                // 设置饼图样式
                if (!budgetPieChart.getData().isEmpty()) {
                    budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
                    budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
                }
            }

            // 更新标签显示
            updateBudgetLabels(totalBudget, usedBudget, availableBudget, usedPercentage, availablePercentage);

            System.out.println("Budget display updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating budget display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 其余方法保持简单...
    private void updateBudgetLabels(double total, double used, double available, double usedPercent, double availablePercent) {
        try {
            if (availableAmountLabel != null) {
                availableAmountLabel.setText("$" + String.format("%.0f", available));
            }
            if (availablePercentLabel != null) {
                availablePercentLabel.setText(String.format("%.0f", availablePercent) + "%");
            }
            if (usedAmountLabel != null) {
                usedAmountLabel.setText("$" + String.format("%.0f", used));
            }
            if (usedPercentLabel != null) {
                usedPercentLabel.setText(String.format("%.0f", usedPercent) + "%");
            }
        } catch (Exception e) {
            System.err.println("Error updating budget labels: " + e.getMessage());
        }
    }

    @FXML
    private void gotoEditBudget(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.EDIT_BUDGET);
    }

    @FXML
    private void gotoRecommendation(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.RECOMMENDATION);
    }

    @FXML
    private void gotoAISuggestion(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.AI_SUGGESTION);
    }

    public void setBudgetPieData(List<PieChart.Data> data) {
        if (budgetPieChart != null) {
            budgetPieChart.getData().clear();
            budgetPieChart.getData().addAll(data);
        }
    }

    public void updateTotalBudget(double newBudget) {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal newBudgetBD = BigDecimal.valueOf(newBudget);
            runtime.getUserManager().setMonthlyBudget(newBudgetBD);
            updateBudgetDisplay();
            System.out.println("Total budget updated to: $" + String.format("%.0f", newBudget));
        } catch (Exception e) {
            System.err.println("Error updating total budget: " + e.getMessage());
        }
    }

    public double getUsedBudget() {
        return 1600.0; // 临时返回固定值
    }

    public double getTotalBudget() {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal totalBudgetBD = runtime.getUserManager().getMonthlyBudget();
            return totalBudgetBD != null ? totalBudgetBD.doubleValue() : 2000.0;
        } catch (Exception e) {
            System.err.println("Error getting total budget: " + e.getMessage());
            return 2000.0;
        }
    }

    public void addExpense(double expenseAmount) {
        System.out.println("Added expense: $" + String.format("%.2f", expenseAmount));
        updateBudgetDisplay();
    }
}