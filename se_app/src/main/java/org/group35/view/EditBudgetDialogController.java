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
        // 初始化代码
    }

    // 设置父控制器引用以更新预算值
    public void setParentController(PlanPageController controller) {
        this.parentController = controller;
    }

    // 在特定坐标显示弹窗的方法（修改为显示在饼图区域）
    public void showAsPopup(Stage ownerStage, String currentBudget) {
        if (dialogStage == null) {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(ownerStage);
            dialogStage.setAlwaysOnTop(true);
            dialogStage.initModality(Modality.NONE);

            // 如果场景不存在，我们需要从FXML加载它
            if (dialogStage.getScene() == null) {
                try {
                    // 假设当前对象已经是由FXMLLoader加载的控制器
                    Scene scene = new Scene(budgetInputField.getScene().getRoot());
                    scene.setFill(Color.TRANSPARENT);
                    dialogStage.setScene(scene);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                dialogStage.getScene().setFill(Color.TRANSPARENT);
            }
        }

        // 输入框保持空白，不显示当前预算值
        budgetInputField.setText("");

        // 计算弹窗在饼图区域的位置
        // 获取主窗口的位置和大小
        double ownerX = ownerStage.getX();
        double ownerY = ownerStage.getY();

        // 调整位置以覆盖整个左侧区域（包括饼图和文字）
        double dialogX = ownerX + 50;   // 向左移动更多以覆盖文字
        double dialogY = ownerY + 250;  // 调整Y坐标

        dialogStage.setX(dialogX);
        dialogStage.setY(dialogY);

        // 显示对话框
        dialogStage.show();

        // 聚焦到输入框
        Platform.runLater(() -> budgetInputField.requestFocus());
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
    private void handleCancel() {
        confirmed = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleSave() {
        String input = budgetInputField.getText().trim();

        // 检查输入是否为数字
        if (!isNumeric(input)) {
            errorLabel.setText("Budget input is invalid.");
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setVisible(true);
            return;
        }

        double budgetValue = Double.parseDouble(input);

        // 检查是否为负数
        if (budgetValue < 0) {
            errorLabel.setText("Budget cannot be negative.");
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setVisible(true);
            return;
        }

        // 获取当前已使用的预算（从父控制器获取实际的消费数据）
        double usedBudget = 0;
        if (parentController != null) {
            usedBudget = parentController.getUsedBudget();
        }

        // 检查新预算是否小于已使用的预算
        if (budgetValue < usedBudget) {
            errorLabel.setText("Budget cannot be less than used amount ($" + String.format("%.0f", usedBudget) + ").");
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setVisible(true);
            return;
        }

        // 如果所有验证都通过
        newBudget = input;
        confirmed = true;

        // 更新错误标签为成功消息（英文）
        errorLabel.setText("Budget updated successfully.");
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setVisible(true);

        System.out.println("New budget: " + newBudget);

        // 如果存在父控制器，则更新预算
        if (parentController != null) {
            try {
                // 调用父控制器的更新预算方法
                parentController.updateTotalBudget(budgetValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 延迟1.5秒后关闭
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
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
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
            controller.setParentController(parentController);

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