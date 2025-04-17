package org.group35.view;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.controller.UserManager;

import java.util.List;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private void initialize() {
        backgroundImage.setImage(new Image("/org/group35/view/assets/LoginPage_background.jpg"));
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", null, "Please enter both username and password.");
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
                showAlert(Alert.AlertType.ERROR, "Login Error", null, "User record not found.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Error", null, "Invalid username or password.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        // Basic username checks
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null, "Please enter a username.");
            return;
        }
        // Alphanumeric only
        if (!username.matches("[A-Za-z0-9]+")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null, "Username can only contain letters and digits.");
            return;
        }
        // Duplicate username check
        boolean exists = UserManager.getUsers().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null, "That username is already taken.");
            return;
        }

        // Initial password from main form
        String initialPwd = passwordField.getText();
        if (initialPwd.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null, "Please enter a password.");
            return;
        }
        // Password strength check
        if (initialPwd.length() < 8
                || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", null,
                    "Password must be at least 8 characters and include letters and digit.");
            return;
        }

        // Confirmation popup
        Optional<String> pwdOpt = showConfirmPasswordDialog(initialPwd);
        if (pwdOpt.isEmpty()) {
            return; // cancelled or mismatch
        }

        // Register user
        UserManager.registerUser(username, initialPwd);
        showAlert(Alert.AlertType.INFORMATION, "Registration Successful", null,
                "Account created.");

        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Shows a dialog prompting the user to re-enter their password for confirmation.
     * Returns the password if it matches initialPwd, empty otherwise.
     */
    private Optional<String> showConfirmPasswordDialog(String initialPwd) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Confirm Password");
        dialog.setHeaderText("Please re-enter your password:");

        ButtonType confirmType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        dialog.getDialogPane().setContent(confirmField);

        Platform.runLater(confirmField::requestFocus);
        dialog.setResultConverter(btn -> btn == confirmType ? confirmField.getText() : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String confirm = result.get();
            if (!confirm.equals(initialPwd)) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", null, "Passwords do not match.");
                return Optional.empty();
            }
            return Optional.of(initialPwd);
        }
        return Optional.empty();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}