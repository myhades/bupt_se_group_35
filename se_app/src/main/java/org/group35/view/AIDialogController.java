package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

public class AIDialogController {

    @FXML
    private Text suggestionText;

    private Stage dialogStage;
    private PlanPageController planPageController;

    // 初始化方法
    public void initialize() {
        // 设置默认的AI建议内容
        String defaultSuggestions = generateDefaultSuggestions();

        if (suggestionText != null) {
            suggestionText.setText(defaultSuggestions);
        }

        System.out.println("AI Dialog initialized successfully");
    }

    // 设置对话框舞台
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // 设置Plan页面控制器的引用
    public void setPlanPageController(PlanPageController controller) {
        this.planPageController = controller;
    }

    // 生成默认的AI建议内容
    private String generateDefaultSuggestions() {
        return "🤖 AI Budget Recommendations\n\n" +
                "📊 Budget Analysis:\n" +
                "• Total Budget: $1,800\n" +
                "• Used: $1,600 (89%)\n" +
                "• Available: $200 (11%)\n\n" +
                "💡 AI Suggestions:\n" +
                "• Reduce dining expenses by 30%\n" +
                "• Use public transportation\n" +
                "• Set up automatic savings\n" +
                "• Find better deals and discounts\n";
    }

    // API接口：设置建议内容
    public void setSuggestionContent(String content) {
        if (suggestionText != null) {
            suggestionText.setText(content);
        }
    }

    // 处理返回按钮点击
    @FXML
    private void handleBack() {
        System.out.println("Back button clicked");
        closeDialog();
    }

    // 关闭对话框的统一方法
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // 静态方法：显示AI建议覆盖层 - 完全照搬Recommendation的实现
    public static void showAISuggestionOverlay(PlanPageController planController) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AIDialogController.class.getResource("/org/group35/view/AIDialog.fxml")
            );
            Parent aiPane = loader.load();

            AIDialogController controller = loader.getController();
            controller.setPlanPageController(planController);

            // 创建一个无装饰的悬浮窗口来模拟覆盖效果
            Stage overlayStage = new Stage();
            overlayStage.setTitle("AI Budget Suggestions");
            overlayStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            overlayStage.initModality(javafx.stage.Modality.NONE);
            overlayStage.setAlwaysOnTop(true);
            overlayStage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(aiPane);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            overlayStage.setScene(scene);

            controller.setDialogStage(overlayStage);

            // 计算位置，使其覆盖在饼图区域
            try {
                Stage currentStage = (Stage) planController.budgetPieChart.getScene().getWindow();
                double currentX = currentStage.getX();
                double currentY = currentStage.getY();

                // 设置弹窗位置在饼图区域
                overlayStage.setX(currentX + 80);
                overlayStage.setY(currentY + 262);
            } catch (Exception e) {
                System.out.println("Could not calculate precise position, using default");
                overlayStage.centerOnScreen();
            }

            overlayStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to load AI suggestion interface: " + e.getMessage());
        }
    }
}