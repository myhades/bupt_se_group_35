package org.group35.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.group35.util.LoggerHelper;

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
        LoggerHelper.info("Primary stage configured");
    }

    /**
     * Loads an FXML file and sets it as the current scene.
     */
    public static void switchScene(String fxmlPath, int width, int height, String title) {
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(root, width, height);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
            LoggerHelper.info("Navigated to scene: " + fxmlPath);
        } catch (Exception e) {
            LoggerHelper.error("Failed to load scene '" + fxmlPath + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convenience method for showing the login page.
     */
    public static void showLoginPage() {
        LoggerHelper.debug("Switching to LoginPage.fxml");
        switchScene("/org/group35/view/LoginPage.fxml", 1000, 500, "Monora");
    }

    /**
     * Convenience method for showing the home page.
     */
    public static void showHomePage() {
        LoggerHelper.debug("Switching to HomePage.fxml");
        switchScene("/org/group35/view/HomePage.fxml", 1000, 500, "Monora");
    }
}
