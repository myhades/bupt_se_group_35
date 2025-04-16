package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    // Inject FXML components using fx:id
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView backgroundImage;

    /**
     * Initializes the controller. This method is automatically called after the FXML is loaded.
     */
    @FXML
    private void initialize() {
        // Optionally set a background image.
        // Adjust the path below to the location of your image file.
        backgroundImage.setImage(new Image("/org/group35/view/assets/LoginPage_background.jpg"));
    }

    /**
     * Handles login button action.
     *
     * @param event the action event.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Example login validation; replace with actual logic.
        if ("Usr123".equals(username) && "123".equals(password)) {
            // On successful login, transition to the main page.
            // For example, you can load another FXML scene and switch the stage's scene.
            System.out.println("Login successful!");
        } else {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("Login Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Invalid username or password.");
            errorAlert.showAndWait();
        }
    }

    /**
     * Handles register button action.
     *
     * @param event the action event.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        Alert infoAlert = new Alert(AlertType.INFORMATION);
        infoAlert.setTitle("Register");
        infoAlert.setHeaderText(null);
        infoAlert.setContentText("Register functionality is not implemented yet.");
        infoAlert.showAndWait();
    }
}
