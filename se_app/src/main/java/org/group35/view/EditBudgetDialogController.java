package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

import java.math.BigDecimal;
import java.util.List;

public class EditBudgetDialogController {
    @FXML
    private TextField budgetInputField;
    @FXML
    private Label errorLabel;
    private Stage dialogStage;
    private String newBudget;
    private boolean confirmed = false;

    public void initialize() {
    }

    // Stage设置器
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // 获取对话框Stage
    public Stage getDialogStage() {
        return dialogStage;
    }

    // 设置新预算
    public void setNewBudget(String newBudget) {
        this.newBudget = newBudget;
        // 不再自动设置到输入框，保持空白
    }

    // 获取新预算
    public String getNewBudget() {
        return newBudget;
    }

    // 检查"确认"状态
    public boolean isConfirmed() {
        return confirmed;
    }
    @FXML
    private void gotoRecommendation(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.RECOMMENDATION);
    }
    @FXML
    private void handleCancel() {
        confirmed = false;
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.PLAN);
    }

    @FXML
    private void handleSave() {
        String input = budgetInputField.getText().trim();

        // 检查输入是否为数字
        if (!isNumeric(input)) {
            errorLabel.setText("Budget input is invalid.");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        double budgetValue = Double.parseDouble(input);

        // 检查是否为负数
        if (budgetValue < 0) {
            errorLabel.setText("Budget cannot be negative.");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        // 计算当前已使用的预算
        double usedBudget = calculateCurrentUsedBudget();

        // 检查新预算是否小于已使用的预算
        if (budgetValue < usedBudget) {
            errorLabel.setText("Budget cannot be less than used amount ($" + String.format("%.0f", usedBudget) + ").");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        // 如果所有验证都通过
        newBudget = input;
        confirmed = true;

        // 更新成功消息
        errorLabel.setText("Budget updated successfully.");
        errorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 13px;");
        errorLabel.setVisible(true);

        System.out.println("New budget: " + newBudget);

        // 使用UserManager更新预算
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal newBudgetBD = BigDecimal.valueOf(budgetValue);
            runtime.getUserManager().setMonthlyBudget(newBudgetBD);
            System.out.println("Budget updated via UserManager: $" + budgetValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 延迟后跳转回Plan页面
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                if (dialogStage != null) {
                    dialogStage.close();
                }
                ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.PLAN);
            });
        }).start();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double calculateCurrentUsedBudget() {
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User currentUser = runtime.getCurrentUser();

        if (currentUser == null) {
            return 0.0;
        }

        try {
            // 从TransactionManager获取当前用户的交易记录
            List<Transaction> transactions = runtime.getTranscationManager().getByUser(currentUser.getUsername());

            // 计算支出总额
            double totalExpenses = 0.0;
            for (Transaction transaction : transactions) {
                // 根据你们的Transaction模型调整这里的逻辑
                BigDecimal amount = transaction.getAmount();
                if (amount != null) {
                    totalExpenses += amount.doubleValue();
                }
            }

            return totalExpenses;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    // 修改静态方法，移除位置参数，改为自动计算饼图区域位置
    public static EditBudgetDialogController showDialog(Stage ownerStage, String currentBudget, PlanPageController parentController) {
        try {
            // 加载FXML
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(EditBudgetDialogController.class.getResource("/org/group35/view/EditBudgetDialog.fxml"));
            javafx.scene.layout.AnchorPane dialogPane = loader.load();

            // 获取控制器
            EditBudgetDialogController controller = loader.getController();

            // 创建对话框Stage
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(ownerStage);
            dialogStage.initModality(Modality.NONE);

            // 创建场景
            Scene scene = new Scene(dialogPane);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);

            // 设置控制器的Stage
            controller.setDialogStage(dialogStage);

            // 计算弹窗在饼图区域的位置（调整以覆盖更大区域）
            double ownerX = ownerStage.getX();
            double ownerY = ownerStage.getY();

            // 调整位置以覆盖整个左侧区域（包括饼图和文字）
            double dialogX = ownerX + 45;   // 向左移动更多以覆盖文字
            double dialogY = ownerY + 200;  // 调整Y坐标

            dialogStage.setX(dialogX);
            dialogStage.setY(dialogY);

            // 不再设置当前预算到输入框，保持空白
            // controller.setNewBudget(currentBudget);

            // 显示对话框
            dialogStage.show();

            // 聚焦到输入框
            Platform.runLater(() -> controller.budgetInputField.requestFocus());

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}