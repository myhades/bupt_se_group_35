package org.group35.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.controller.UserManager;

import java.util.Objects;
import java.util.Optional;

public class LoginPageController {

    @FXML private Label warningLabel, confirmWarningLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private VBox loginGroup, confirmGroup;
    @FXML private ImageView backgroundImage;

    // Store pending registration data
    private String pendingUsername;
    private String pendingPassword;

    @FXML
    private void initialize() {
        backgroundImage.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/org/group35/view/assets/images/LoginPage_background.jpg"))
        ));
        hideWarning();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        hideWarning();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showWarning("Please enter both username and password.");
            return;
        }

        boolean success = UserManager.loginUser(username, password);
        if (!success) {
            showWarning("Invalid username or password.");
            return;
        }

        Optional<User> opt = UserManager.getUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
        if (opt.isEmpty()) {
            showWarning("User record not found.");
            return;
        }
        ApplicationRuntime.getInstance().loginUser(opt.get());
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        hideWarning();

        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showWarning("Please enter a username.");
            return;
        }
        if (!username.matches("[A-Za-z0-9]+")) {
            showWarning("Username can only contain letters and digits.");
            return;
        }
        if (UserManager.getUsers().stream()
                .anyMatch(u -> u.getUsername().equals(username))) {
            showWarning("That username is already taken.");
            return;
        }

        String initialPwd = passwordField.getText();
        if (initialPwd.isEmpty()) {
            showWarning("Please enter a password.");
            return;
        }
        if (initialPwd.length() < 8 || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showWarning("Password must be at least 8 characters and include letters and digit.");
            return;
        }

        // Store for confirmation
        pendingUsername = username;
        pendingPassword = initialPwd;

        // switch to confirm group
        loginGroup.setManaged(false);
        loginGroup.setVisible(false);
        confirmGroup.setManaged(true);
        confirmGroup.setVisible(true);
        confirmPasswordField.clear();
        Platform.runLater(confirmPasswordField::requestFocus);
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        hideWarning();
        String confirmPwd = confirmPasswordField.getText();
        if (!confirmPwd.equals(pendingPassword)) {
            showWarning("Passwords do not match.");
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
        resetToLogin();
    }

    /**
     * Resets the UI back to the login view, clearing fields and warnings.
     */
    private void resetToLogin() {
        pendingPassword = null;

        confirmGroup.setManaged(false);
        confirmGroup.setVisible(false);
        loginGroup.setManaged(true);
        loginGroup.setVisible(true);

        usernameField.clear();
        passwordField.clear();
        hideWarning();
    }

    /**
     * Show an inline warning message above the input field.
     */
    private void showWarning(String msg) {
        if (loginGroup.isVisible()) {
            warningLabel.setText(msg);
            warningLabel.setVisible(true);
            warningLabel.setManaged(true);
        } else {
            confirmWarningLabel.setText(msg);
            confirmWarningLabel.setVisible(true);
            confirmWarningLabel.setManaged(true);
        }
    }

    /**
     * Hide the inline warning label.
     */
    private void hideWarning() {
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    @FXML
    private void handleLoginKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);  // Trigger the login action when ENTER is pressed
        }
    }
    @FXML
    private void handleConfirmKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleConfirm(null);  // Trigger the login action when ENTER is pressed
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}