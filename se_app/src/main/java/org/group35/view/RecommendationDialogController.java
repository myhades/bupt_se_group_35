package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.io.IOException;

public class RecommendationDialogController {

    @FXML
    private Text recommendationText;

    private Stage dialogStage;
    private PlanPageController planPageController;

    // 初始化方法
    public void initialize() {
        // 设置默认的推荐内容
        String defaultRecommendations = generateDefaultRecommendations();

        if (recommendationText != null) {
            recommendationText.setText(defaultRecommendations);
        }

        System.out.println("Recommendation Dialog initialized successfully");
    }

    // 设置对话框舞台
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // 设置Plan页面控制器的引用
    public void setPlanPageController(PlanPageController controller) {
        this.planPageController = controller;
    }

    // 生成默认的推荐内容
    private String generateDefaultRecommendations() {
        return "Recommendations For You\n\n" +
                "🏪 Popular Restaurants:\n" +
                "• McDonald's Shanghai Plaza - Fast food, ¥25-45\n" +
                "• Haidilao Hotpot - Chinese cuisine, ¥80-120\n" +
                "• Starbucks Coffee - Beverages & snacks, ¥30-60\n" +
                "• Pizza Hut Century Park - Western food, ¥60-100\n\n" +
                "🛍️ Shopping & Discounts:\n" +
                "• Carrefour Supermarket - Groceries 15% off weekend\n" +
                "• Uniqlo Flagship Store - Winter clothing 30% off\n"
                // ... 更多内容以确保可以滚动
                ;
    }

    // API接口：设置推荐内容
    public void setRecommendationContent(String content) {
        if (recommendationText != null) {
            recommendationText.setText(content);
        }
    }

    // 处理返回按钮点击
    @FXML
    private void handleBack() {
        System.out.println("Back button clicked");
        closeDialog();
    }

    // 处理关闭按钮点击
    @FXML
    private void handleClose() {
        System.out.println("Close button clicked");
        closeDialog();
    }

    // 关闭对话框的统一方法
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // 静态方法：显示推荐覆盖层
    public static void showRecommendationOverlay(PlanPageController planController) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    RecommendationDialogController.class.getResource("/org/group35/view/RecommendationDialog.fxml")
            );
            Parent recommendationPane = loader.load();

            RecommendationDialogController controller = loader.getController();
            controller.setPlanPageController(planController);

            // 创建一个无装饰的悬浮窗口来模拟覆盖效果
            Stage overlayStage = new Stage();
            overlayStage.setTitle("Local Recommendations");
            overlayStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            overlayStage.initModality(javafx.stage.Modality.NONE);
            overlayStage.setAlwaysOnTop(true);
            overlayStage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(recommendationPane);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            overlayStage.setScene(scene);

            controller.setDialogStage(overlayStage);

            // 计算位置，使其覆盖在右侧区域
            try {
                Stage currentStage = (Stage) planController.budgetPieChart.getScene().getWindow();
                double currentX = currentStage.getX();
                double currentY = currentStage.getY();

                // 设置弹窗位置在右侧推荐区域
                overlayStage.setX(currentX + 610); // 向右偏移到推荐区域
                overlayStage.setY(currentY + 220); // 向下偏移到合适位置
            } catch (Exception e) {
                System.out.println("Could not calculate precise position, using default");
                overlayStage.centerOnScreen();
            }

            overlayStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to load recommendation interface: " + e.getMessage());
        }
    }
}