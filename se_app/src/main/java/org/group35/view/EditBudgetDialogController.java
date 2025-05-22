package org.group35.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

public class EditBudgetDialogController {

    @FXML
    private TextField budgetInputField;
    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private String newBudget;
    private boolean confirmed = false;

    // 父控制器引用，用于更新预算显示
    private PlanPageController parentController;

    public void initialize() {
        // 需要的初始化代码
    }

    // 设置父控制器引用以更新预算值
    public void setParentController(PlanPageController controller) {
        this.parentController = controller;
    }

    // 在特定坐标显示弹窗的方法
    public void showAsPopup(Stage ownerStage, double x, double y) {
        if (dialogStage == null) {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setAlwaysOnTop(true);
            dialogStage.initModality(Modality.NONE);

            Scene scene = dialogStage.getScene();
            if (scene != null) {
                scene.setFill(Color.TRANSPARENT);
            }
        }

        // 计算位置 - 可以根据所有者窗口进行调整
        dialogStage.setX(x);
        dialogStage.setY(y);

        // 显示对话框
        dialogStage.show();

        // 聚焦到输入框
        Platform.runLater(() -> budgetInputField.requestFocus());
    }

    // Stage设置器 - 向后兼容
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // 设置新预算
    public void setNewBudget(String newBudget) {
        this.newBudget = newBudget;
        System.out.println(newBudget);
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
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleSave() {
        String input = budgetInputField.getText().trim();
        if (isNumeric(input)) {
            newBudget = input;
            confirmed = true;

            // 更新错误标签为成功消息
            errorLabel.setText("New budget has been updated.");
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setVisible(true);

            System.out.println("New Budget: " + newBudget);

            // 如果存在父控制器，则更新预算
            if (parentController != null) {
                try {
                    // 尝试更新父控制器的预算
                    // 这取决于你的实现
                    double budget = Double.parseDouble(newBudget);
                    // 你需要在PlanPageController中添加一个更新预算的方法
                    // 例如：parentController.updateTotalBudget(budget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 延迟1.5秒
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    if (dialogStage != null) {
                        dialogStage.close();
                    }
                });
            }).start();
        } else {
            errorLabel.setText("Improper input for budget setting.");
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setVisible(true);
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}