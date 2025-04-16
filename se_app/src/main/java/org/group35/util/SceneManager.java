package org.group35.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage primaryStage;

    /**
     * Sets the primary stage for the application.
     *
     * @param stage the primary Stage
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Loads an FXML file from the given path and sets it as the current scene.
     *
     * @param fxmlPath the path to the FXML file (e.g., "/org/group35/view/HomePage.fxml")
     */
    public static void switchScene(String fxmlPath, int width, int height, String title) {
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(root, width, height);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method for switching to the login page.
     */
    public static void showLoginPage() {
        switchScene("/org/group35/view/LoginPage.fxml", 1000, 500, "Monora");
    }

    /**
     * Convenience method for switching to the home page.
     */
    public static void showHomePage() {
        switchScene("/org/group35/view/HomePage.fxml", 1000, 500, "Monora");
    }
}
