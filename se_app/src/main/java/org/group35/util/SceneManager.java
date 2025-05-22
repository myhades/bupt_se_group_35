package org.group35.util;

import java.io.InputStream;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.group35.config.Settings;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

/**
 * Manages switching between application scenes.
 */
public class SceneManager {

    private static Stage primaryStage;

    private static final Settings CFG        = Settings.getInstance();
    private static final double   BASE_W     = CFG.getWindowWidth();
    private static final double   BASE_H     = CFG.getWindowHeight();
    private static final String   APP_TITLE  = CFG.appName;
    private static final String   ICON_PATH  = "/org/group35/util/assets/monora_icon.png";
    private static final String   GLOBAL_CSS = SceneManager.class.getResource("/org/group35/view/Global.css")
                                                        .toExternalForm();

    /**
     * Sets the primary stage for the application.
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false);
        try (InputStream icon = SceneManager.class.getResourceAsStream(ICON_PATH)) {
            if (icon != null) {
                primaryStage.getIcons().add(new Image(icon));
            } else {
                LogUtils.warn("App icon not found at");
            }
        } catch (Exception e) {
            LogUtils.error("Error loading app icon");
        }
        LogUtils.info("Primary stage configured");
    }

    /**
     * Loads an FXML file and sets it as the current scene.
     */
    public static void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
            Scene scene = new Scene(root, BASE_W, BASE_H);
            scene.getStylesheets().add(GLOBAL_CSS);
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.show();
            LogUtils.info("Navigated to scene: " + fxmlPath);
        } catch (Exception e) {
            LogUtils.error("Failed to load scene '" + fxmlPath + "': " + e.getMessage());
        }
    }

    public static void showPage(ProgramStatus status) {
        String fxml = status.getFxmlPath();
        if (fxml == null) {
            LogUtils.warn("No FXML bound for status: " + status);
            return;
        }
        switchScene(fxml);
    }
}
