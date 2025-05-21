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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.geometry.Pos;
import javafx.application.Platform;
import org.group35.runtime.ApplicationRuntime;


public class PlanPageController {
    @FXML
    private Button recommendationButton;
    @FXML
    private PieChart budgetPieChart;
    @FXML
    private Button editBudgetButton;
    @FXML
    private Button AIButton;

    // 预算数据
    private double totalBudget = 1800; // 默认总预算
    private double usedAmount = 1600;  // 已使用金额（保持不变）

    // 内联预算编辑器相关组件
    private AnchorPane budgetEditorPane;
    private TextField budgetInputField;
    private Label errorLabel;

    // 标签引用
    @FXML
    private Label availableLabel;
    @FXML
    private Label availablePercentLabel;
    @FXML
    private Label usedLabel;
    @FXML
    private Label usedPercentLabel;

    public void initialize() {
        recommendationButton.setWrapText(true);
        budgetPieChart.getStyleClass().add("pie-chart");
        insertBudgetData();

//        editBudgetButton.setOnAction(e -> showEditBudgetDialog());
//        recommendationButton.setOnAction(e -> showRecommendationDialog());
//        AIButton.setOnAction(e -> showAIDialog());
    }

    @FXML
    private void gotoEditBudget(ActionEvent event) {
        // 不执行跳转
        // ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.EDIT_BUDGET);

        try {
            // 获取根容器（通过向上遍历父节点，寻找第一个AnchorPane）
            Pane rootPane = findRootPane(budgetPieChart);

            if (rootPane == null) {
                System.err.println("无法找到合适的父容器来添加编辑器");
                return;
            }

            // 移除已存在的编辑器（如果有）
            rootPane.getChildren().removeIf(node ->
                    node instanceof AnchorPane && "budgetEditorPane".equals(node.getId()));

            // 创建新的预算编辑器面板
            createBudgetEditor(rootPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找合适的父容器（向上遍历直到找到Pane类型的容器）
     */
    private Pane findRootPane(javafx.scene.Node node) {
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof Pane) {
                return (Pane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * 创建与EditBudgetDialog.fxml完全一致的预算编辑器
     */
    private void createBudgetEditor(Pane parent) {
        // 创建编辑器容器
        budgetEditorPane = new AnchorPane();
        budgetEditorPane.setId("budgetEditorPane");
        budgetEditorPane.getStyleClass().add("dialog-container");
        budgetEditorPane.setPrefWidth(350);
        budgetEditorPane.setPrefHeight(180);

        // 设置标题
        Label titleLabel = new Label("New Budget Setting");
        titleLabel.getStyleClass().add("title-label");
        AnchorPane.setTopAnchor(titleLabel, 14.0);
        AnchorPane.setLeftAnchor(titleLabel, 60.0);

        // 设置图标
        SVGPath updateIcon = new SVGPath();
        updateIcon.setContent("M21,10.12H14.22L16.96,7.3C14.23,4.6 9.81,4.5 7.08,7.2C4.35,9.91 4.35,14.28 7.08,17C9.81,19.7 14.23,19.7 16.96,17C18.32,15.65 19,14.08 19,12.1H21C21,14.08 20.12,16.65 18.36,18.39C14.85,21.87 9.15,21.87 5.64,18.39C2.14,14.92 2.11,9.28 5.62,5.81C9.13,2.34 14.76,2.34 18.27,5.81L21,3V10.12M12.5,8V12.25L16,14.33L15.28,15.54L11,13V8H12.5Z");
        updateIcon.getStyleClass().add("update-icon");
        AnchorPane.setTopAnchor(updateIcon, 20.0);
        AnchorPane.setLeftAnchor(updateIcon, 25.0);

        // 设置状态点
        Label dotLabel = new Label("●");
        dotLabel.getStyleClass().add("Dot");
        AnchorPane.setTopAnchor(dotLabel, 15.0);
        AnchorPane.setLeftAnchor(dotLabel, 275.0);

        // 创建卡片背景
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(15);
        card.getStyleClass().add("card");
        AnchorPane.setTopAnchor(card, 50.0);
        AnchorPane.setLeftAnchor(card, 20.0);
        AnchorPane.setRightAnchor(card, 20.0);
        AnchorPane.setBottomAnchor(card, 20.0);

        // 创建输入行
        HBox inputRow = new HBox(10);
        Label inputLabel = new Label("New Budget：");
        inputLabel.getStyleClass().add("dialog-label");

        budgetInputField = new TextField();
        budgetInputField.setPromptText("Unit: $");
        budgetInputField.getStyleClass().add("input-field");

        inputRow.getChildren().addAll(inputLabel, budgetInputField);

        // 创建错误标签
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        // 创建按钮行
        HBox buttonRow = new HBox(30);
        buttonRow.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("dialog-button");
        confirmButton.setOnAction(e -> handleSave());

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("dialog-button");
        cancelButton.setOnAction(e -> handleCancel());

        buttonRow.getChildren().addAll(confirmButton, cancelButton);

        // 将所有组件添加到卡片
        card.getChildren().addAll(inputRow, errorLabel, buttonRow);

        // 将所有组件添加到编辑器面板
        budgetEditorPane.getChildren().addAll(titleLabel, updateIcon, dotLabel, card);

        // 设置编辑器位置（尝试放在红框位置）
        // 注意：位置需要根据实际布局调整
        // 可能需要使用绝对定位或相对于父容器定位
        double x = 120;
        double y = 320;

        // 设置位置
        budgetEditorPane.setLayoutX(x);
        budgetEditorPane.setLayoutY(y);

        // 设置样式使其看起来像浮动窗口
        budgetEditorPane.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // 添加到父容器
        parent.getChildren().add(budgetEditorPane);

        // 聚焦到输入框
        Platform.runLater(() -> budgetInputField.requestFocus());
    }

    /**
     * 处理保存按钮点击
     */
    private void handleSave() {
        String input = budgetInputField.getText().trim();
        if (isNumeric(input)) {
            // 更新预算
            totalBudget = Double.parseDouble(input);

            // 更新显示
            updateBudgetDisplay();

            // 显示成功消息
            errorLabel.setText("New budget has been updated.");
            errorLabel.getStyleClass().removeAll("error-text");
            if (!errorLabel.getStyleClass().contains("success-text"))
                errorLabel.getStyleClass().add("success-text");
            errorLabel.setVisible(true);

            System.out.println("New Budget: " + input);

            // 延迟后关闭编辑器
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    Pane parent = findRootPane(budgetPieChart);
                    if (parent != null && budgetEditorPane != null && parent.getChildren().contains(budgetEditorPane)) {
                        parent.getChildren().remove(budgetEditorPane);
                    }
                });
            }).start();
        } else {
            // 显示错误消息
            errorLabel.setText("Improper input for budget setting.");
            errorLabel.getStyleClass().removeAll("success-text");
            if (!errorLabel.getStyleClass().contains("error-text"))
                errorLabel.getStyleClass().add("error-text");
            errorLabel.setVisible(true);
        }
    }

    /**
     * 处理取消按钮点击
     */
    private void handleCancel() {
        Pane parent = findRootPane(budgetPieChart);
        if (parent != null && budgetEditorPane != null && parent.getChildren().contains(budgetEditorPane)) {
            parent.getChildren().remove(budgetEditorPane);
        }
    }

    /**
     * 更新预算显示
     */
    private void updateBudgetDisplay() {
        // 计算可用金额和百分比
        double availableAmount = totalBudget - usedAmount;
        double availablePercentage = (availableAmount / totalBudget) * 100;
        double usedPercentage = (usedAmount / totalBudget) * 100;

        // 更新标签（如果存在）
        if (availableLabel != null) {
            availableLabel.setText("$" + String.format("%.0f", availableAmount));
        }
        if (availablePercentLabel != null) {
            availablePercentLabel.setText(String.format("%.0f%%", availablePercentage));
        }
        if (usedLabel != null) {
            usedLabel.setText("$" + String.format("%.0f", usedAmount));
        }
        if (usedPercentLabel != null) {
            usedPercentLabel.setText(String.format("%.0f%%", usedPercentage));
        }

        // 更新饼图
        List<PieChart.Data> pieData = Arrays.asList(
                new PieChart.Data("Used", usedPercentage),
                new PieChart.Data("Available", availablePercentage)
        );
        setBudgetPieData(pieData);
        budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
        budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
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
     * You can remove or replace this call when injecting real data.
     */
    public void insertBudgetData() {
        // Demo for pie chart
        List<PieChart.Data> demoPie = Arrays.asList(
                new PieChart.Data("Used", 80),
                new PieChart.Data("Available", 20)
        );
        setBudgetPieData(demoPie);
        budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
        budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
    }

    /**
     * 判断字符串是否为数字
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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