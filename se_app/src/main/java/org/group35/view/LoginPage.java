package org.group35.view;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("EBU6304 Software Engineering");

        // Create the main container (HBox for left-right layout)
        HBox root = new HBox(10);  // No gap between left and right panels
        root.setAlignment(Pos.CENTER);

        // Create the login form panel (VBox for vertical layout)
        VBox loginPanel = new VBox(20);
        loginPanel.setAlignment(Pos.CENTER);

        // Create the title and welcome label
        Label titleLabel = new Label("EBU6304 Software Engineering");
        titleLabel.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #0B76A0;");

        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create text fields for username and password
        TextField usernameField = new TextField("Username");
        setPlaceholderBehavior(usernameField, "Username");
        usernameField.setPrefHeight(40);
        usernameField.setPrefWidth(300);

        PasswordField passwordField = new PasswordField();
        setPasswordPlaceholderBehavior(passwordField, "Password");
        passwordField.setPrefHeight(40);

        // Create login and register buttons
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(40);
        registerButton.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        registerButton.setPrefWidth(120);
        registerButton.setPrefHeight(40);

        // Button container (HBox to place buttons side by side)
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getChildren().addAll(loginButton, registerButton);

        // Add components to the login panel
        loginPanel.getChildren().addAll(titleLabel, welcomeLabel, usernameField, passwordField, buttonPanel);

        // Set the loginPanel width dynamically
        loginPanel.setPrefWidth(500); // This ensures loginPanel takes up 50% of the width

        // Create the image panel with a background image
        StackPane imagePanel = new StackPane();
        Image backgroundImage = new Image("file:F:/BUPT lessons/SoftwareProject/EBU6304GP/src/image/background.png");
        ImageView imageView = new ImageView(backgroundImage);
        imageView.setFitWidth(500);

        //imageView.setPreserveRatio(true);  // Preserve the aspect ratio of the image
        imagePanel.getChildren().add(imageView);

        // Set the imagePanel to fill the right half of the HBox dynamically
        imagePanel.setMaxWidth(Double.MAX_VALUE); // Ensure it fills the remaining space

        // Add a Region to fill the remaining space for loginPanel
        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);  // Ensure the loginPanel fills the available space on the left side


        // Add login panel and image panel to root
        root.getChildren().addAll(loginPanel, filler, imagePanel);

        // Event handling for login button
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.equals("Usr123") && password.equals("123")) {
//                // Show success message in an alert popup
//                Alert successAlert = new Alert(AlertType.INFORMATION);
//                successAlert.setTitle("Login Success");
//                successAlert.setHeaderText(null);  // Optional: can be used for a header
//                successAlert.setContentText("Login successful!");
//                successAlert.showAndWait();  // Show the alert and wait for user action
                MainPage mainPage = new MainPage();
                try {
                    mainPage.start(primaryStage);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                // Show error message in an alert popup
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Login Error");
                errorAlert.setHeaderText(null);  // Optional: can be used for a header
                errorAlert.setContentText("Invalid username or password.");
                errorAlert.showAndWait();  // Show the alert and wait for user action
            }
        });

        // Event handling for register button
        registerButton.setOnAction(event -> {
            // Show information message for the register button
            Alert registerAlert = new Alert(AlertType.INFORMATION);
            registerAlert.setTitle("Register");
            registerAlert.setHeaderText(null);  // Optional: can be used for a header
            registerAlert.setContentText("Register functionality is not implemented yet.");
            registerAlert.showAndWait();  // Show the alert and wait for user action
        });

        // Set up the scene and stage
        Scene scene = new Scene(root, 1000, 500); // Total size of the window
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    // Method to simulate placeholder behavior for username field
    private void setPlaceholderBehavior(TextField field, String placeholder) {
        field.setStyle("-fx-text-fill: gray;");
        field.setText(placeholder);

        field.setOnMouseClicked(event -> {
            if (field.getText().equals(placeholder)) {
                field.setText("");
                field.setStyle("-fx-text-fill: gray;");
            }
        });

        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && field.getText().isEmpty()) {
                field.setStyle("-fx-text-fill: gray;");
                field.setText(placeholder);
            }
        });
    }

    // Method to simulate password placeholder behavior
    private void setPasswordPlaceholderBehavior(PasswordField field, String placeholder) {
        field.setStyle("-fx-text-fill: gray;");
        field.setPromptText(placeholder);

        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && field.getText().isEmpty()) {
                field.setStyle("-fx-text-fill: gray;");
                field.setPromptText(placeholder);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
