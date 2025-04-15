package org.group35.view;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create login panel (for simplicity)
        Button loginButton = new Button("Login");

        // Create the home page (after login)
        StackPane homePane = new StackPane();
        Button homeButton = new Button("Home Page");  // Placeholder for home content
        homePane.getChildren().add(homeButton);

        // Set up the login button's action event to show the home page after login
        loginButton.setOnAction(event -> {
            // Create a scene for the home page
            Scene homeScene = new Scene(homePane, 600, 400);
            primaryStage.setScene(homeScene);  // Switch to home page scene
        });

        // Set up the login scene
        StackPane loginPane = new StackPane();
        loginPane.getChildren().add(loginButton);
        Scene loginScene = new Scene(loginPane, 300, 200);

        // Set the primary stage with the login scene
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(loginScene);  // Initially show the login scene
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
