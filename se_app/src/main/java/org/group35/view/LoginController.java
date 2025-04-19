package org.group35.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.controller.UserManager;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private VBox loginGroup;

    @FXML
    private VBox confirmGroup;

    @FXML
    private ImageView backgroundImage;

    // Store pending registration data
    private String pendingUsername;
    private String pendingPassword;

    @FXML
    private void initialize() {
        backgroundImage.setImage(new Image(
                getClass().getResourceAsStream("/org/group35/view/assets/images/LoginPage_background.jpg")
        ));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", null,
                    "Please enter both username and password.");
            return;
        }

        boolean success = UserManager.loginUser(username, password);
        if (success) {
            Optional<User> opt = UserManager.getUsers().stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst();

            if (opt.isPresent()) {
                ApplicationRuntime.getInstance().loginUser(opt.get());
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Error", null,
                        "User record not found.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Error", null,
                    "Invalid username or password.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Prepare for registration: collect and validate initial data
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Please enter a username.");
            return;
        }
        if (!username.matches("[A-Za-z0-9]+")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Username can only contain letters and digits.");
            return;
        }
        if (UserManager.getUsers().stream()
                .anyMatch(u -> u.getUsername().equals(username))) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "That username is already taken.");
            return;
        }

        String initialPwd = passwordField.getText();
        if (initialPwd.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Please enter a password.");
            return;
        }
        if (initialPwd.length() < 8 || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Password must be at least 8 characters and include letters and digit.");
            return;
        }

        // Store for confirmation
        pendingUsername = username;
        pendingPassword = initialPwd;

        // Show confirm group
        loginGroup.setManaged(false);
        loginGroup.setVisible(false);
        confirmGroup.setManaged(true);
        confirmGroup.setVisible(true);
        confirmPasswordField.clear();
        Platform.runLater(confirmPasswordField::requestFocus);
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        String confirmPwd = confirmPasswordField.getText();
        if (!confirmPwd.equals(pendingPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Passwords do not match.");
            return;
        }
        // Perform registration
        UserManager.registerUser(pendingUsername, pendingPassword);
        showAlert(Alert.AlertType.INFORMATION, "Registration Successful", null,
                "Account created.");

        // Reset UI
        resetToLogin();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Cancel registration, go back
        resetToLogin();
    }

    /**
     * Resets the UI back to the login group, clearing all fields.
     */
    private void resetToLogin() {
        pendingUsername = null;
        pendingPassword = null;

        confirmGroup.setManaged(false);
        confirmGroup.setVisible(false);
        loginGroup.setManaged(true);
        loginGroup.setVisible(true);

        usernameField.clear();
        passwordField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
