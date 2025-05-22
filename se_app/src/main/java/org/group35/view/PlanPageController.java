package org.group35.view;

import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import java.io.IOException;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import org.group35.runtime.ApplicationRuntime;
import javafx.scene.Node;

public class PlanPageController {
    @FXML
    private Button recommendationButton;
    @FXML
    private PieChart budgetPieChart;
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

    // 添加预算相关的成员变量
    private double totalBudget = 1800.0;  // 默认总预算，可以从数据库或配置文件读取
    private double usedBudget = 1600.0;   // 实际已使用预算，应该从消费记录中计算得出

    public void initialize() {
        recommendationButton.setWrapText(true);
        budgetPieChart.getStyleClass().add("pie-chart");

        // 初始化时加载真实的预算数据
        loadBudgetData();
        updateBudgetDisplay();

        // 修改编辑预算按钮的事件处理
        editBudgetButton.setOnAction(this::showEditBudgetPopup);
//        recommendationButton.setOnAction(e -> showRecommendationDialog());
//        AIButton.setOnAction(e -> showAIDialog());
    }

    // 新增方法：加载预算数据（这里应该从数据库或其他数据源加载）
    private void loadBudgetData() {
        // TODO: 从数据库或其他数据源加载实际的预算和消费数据
        // 这里使用模拟数据，实际应用中应该替换为真实的数据加载逻辑

        // 示例：从数据库加载总预算
        // totalBudget = DatabaseService.getTotalBudget();

        // 示例：从消费记录计算已使用预算
        // usedBudget = DatabaseService.calculateUsedBudget();

        // 当前使用默认值进行演示
        totalBudget = 1800.0;
        usedBudget = 1600.0;
    }

    // 新增方法：更新预算显示
    private void updateBudgetDisplay() {
        double availableBudget = totalBudget - usedBudget;
        if (availableBudget < 0) availableBudget = 0;

        // 计算百分比
        double usedPercentage = totalBudget > 0 ? (usedBudget / totalBudget) * 100 : 0;
        double availablePercentage = totalBudget > 0 ? (availableBudget / totalBudget) * 100 : 0;

        // 更新饼图数据
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

        // 在界面上更新预算显示数据 - 这里需要与FXML中的Label绑定
        updateBudgetLabels(totalBudget, usedBudget, availableBudget, usedPercentage, availablePercentage);
    }

    // 新增方法：更新界面上的预算标签显示
    private void updateBudgetLabels(double total, double used, double available, double usedPercent, double availablePercent) {
        // 更新界面标签 - 如果标签存在的话
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
    }

    // 修改后的方法：显示编辑预算弹窗
    private void showEditBudgetPopup(ActionEvent event) {
        // 获取事件源节点
        Node source = (Node) event.getSource();
        // 获取当前窗口
        Stage ownerStage = (Stage) source.getScene().getWindow();

        // 获取当前预算值（传递总预算值，但弹窗中不显示）
        String currentBudget = String.valueOf((int)totalBudget);

        // 显示预算编辑对话框 - 移除了位置参数，改为自动定位到饼图区域
        EditBudgetDialogController.showDialog(ownerStage, currentBudget, this);
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

    /**
     * Clears and sets the budget pie chart with the given data.
     * @param data the list of PieChart.Data to display
     */
    public void setBudgetPieData(List<PieChart.Data> data) {
        budgetPieChart.getData().clear();
        budgetPieChart.getData().addAll(data);
    }

    /**
     * 保留原方法用于演示，但在实际应用中应该被 updateBudgetDisplay() 替代
     */
    public void insertBudgetData() {
        // 这个方法现在被 updateBudgetDisplay() 替代
        updateBudgetDisplay();
    }

    // 修改后的方法：更新总预算
    public void updateTotalBudget(double newBudget) {
        this.totalBudget = newBudget;

        // TODO: 这里应该将新的总预算保存到数据库
        // DatabaseService.saveTotalBudget(newBudget);

        // 更新显示
        updateBudgetDisplay();

        System.out.println("Total budget updated to: $" + String.format("%.0f", totalBudget));
    }

    // 新增方法：获取当前已使用的预算
    public double getUsedBudget() {
        // TODO: 实际应用中应该从数据库实时计算已使用预算
        // return DatabaseService.calculateUsedBudget();

        return usedBudget;
    }

    // 新增方法：获取总预算
    public double getTotalBudget() {
        return totalBudget;
    }

    // 新增方法：设置已使用预算（当有新的消费记录时调用）
    public void setUsedBudget(double usedAmount) {
        this.usedBudget = usedAmount;
        updateBudgetDisplay();
    }

    // 新增方法：添加消费（当用户添加新的消费记录时调用）
    public void addExpense(double expenseAmount) {
        this.usedBudget += expenseAmount;

        // TODO: 将新的消费记录保存到数据库
        // DatabaseService.saveExpense(expenseAmount);

        updateBudgetDisplay();
        System.out.println("Added expense: $" + String.format("%.2f", expenseAmount));
    }

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

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showRecommendationDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/group35/view/RecommendationDialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Recommendation");
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/group35/util/assets/monora_icon.png")));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(dialogRoot));

            RecommendationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAIDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/group35/view/AIDialog.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("AI Suggestions");
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/group35/util/assets/monora_icon.png")));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(dialogRoot));

            AIDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}