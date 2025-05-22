package org.group35.util;

import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.group35.config.Settings;

/**
 * Manages switching between application scenes.
 */
public class SceneManager {
    private static Stage primaryStage;

    /**
     * Sets the primary stage for the application.
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false);
        Image icon = new Image(
                SceneManager.class.getResourceAsStream("/org/group35/util/assets/monora_icon.png")
        );
        primaryStage.getIcons().add(icon);
        LogUtils.info("Primary stage configured");
    }

    /**
     * Loads an FXML file and sets it as the current scene.
     */
    public static void switchScene(String fxmlPath) {
        try {
            Settings settings = Settings.getInstance();
            Parent root = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
            Scene scene = new Scene(root, settings.getWindowWidth(), settings.getWindowHeight());
            scene.getStylesheets().add(
                    Objects.requireNonNull(SceneManager.class.getResource("/org/group35/view/Global.css")).toExternalForm()
            );
            primaryStage.setTitle(settings.appName);
            primaryStage.setScene(scene);
            primaryStage.show();
            LogUtils.info("Navigated to scene: " + fxmlPath);
        } catch (Exception e) {
            LogUtils.error("Failed to load scene '" + fxmlPath + "': " + e.getMessage());
        }
    }

    public static void showLoginPage() {
        LogUtils.debug("Switching to LoginPage.fxml");
        switchScene("/org/group35/view/LoginPage.fxml");
    }

    public static void showHomePage() {
        LogUtils.debug("Switching to HomePage.fxml");
        switchScene("/org/group35/view/HomePage.fxml");
    }

    public static void showPlanPage() {
        LogUtils.debug("Switching to PlanPage.fxml");
        switchScene("/org/group35/view/PlanPage.fxml");
    }

    public static void showMorePage() {
        LogUtils.debug("Switching to MorePage.fxml");
        switchScene("/org/group35/view/MorePage.fxml");
    }

    public static void showRecognizeBillPage() {
        LogUtils.debug("Switching to RecognizeBillPage.fxml");
        switchScene("/org/group35/view/RecognizeBillPage.fxml");
    }

    public static void showManualEntryPage() {
        LogUtils.debug("Switching to ManualEntryPage.fxml");
        switchScene("/org/group35/view/ManualEntryPage.fxml");
    }

    public static void showConfirmEntryPage() {
        LogUtils.debug("Switching to ConfirmEntryPage.fxml");
        switchScene("/org/group35/view/ConfirmEntryPage.fxml");
    }

    public static void showSpendingPage() {
        LogUtils.debug("Switching to SpendingPage.fxml");
        switchScene("/org/group35/view/SpendingPage.fxml");
    }

    public static void showEditBudgetPage() {
        LogUtils.debug("Switching to EditBudgetPage.fxml");
        switchScene("/org/group35/view/SpendingPage.fxml");
    }

    public static void showAISuggestionPage() {
        LogUtils.debug("Switching to AISuggestionPage.fxml");
        switchScene("/org/group35/view/SpendingPage.fxml");
    }

    public static void showRecommendationPage() {
        LogUtils.debug("Switching to RecommendationPage.fxml");
        switchScene("/org/group35/view/SpendingPage.fxml");
    }
}
