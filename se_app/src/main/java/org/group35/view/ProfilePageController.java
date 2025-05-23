package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import  javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilePageController {

    @FXML private Label warningLabel;
    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private TextField datetimeField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> categoryBox;
    private Boolean isProcessing;

    @FXML
    public void initialize() {

    }


    @FXML
    private void handleLoginKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
//            handleLogin(null);  // Trigger the login action when ENTER is pressed
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        toggleProcessing(false);
    }

    private void toggleProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
        if (isProcessing){

        }
        else {

        }
    }

    private void showError(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    @FXML
    private void handleSave(ActionEvent event) {

        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }

        // 2) Validate amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                showError("Amount must be not be zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
            return;
        }

        // 3) Validate date/time
        String dtInput = datetimeField.getText() == null
                ? ""
                : datetimeField.getText().trim();
        LocalDateTime timestamp;

        if (dtInput.isEmpty()) {
            timestamp = LocalDateTime.now();
        } else {
            Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?: (\\d{2}:\\d{2}))?$");
            Matcher matcher = pattern.matcher(dtInput);
            if (!matcher.matches()) {
                showError("Invalid date/time. Use yyyy-MM-dd (HH:mm).");
                return;
            }
            LocalDate date;
            LocalTime time = LocalTime.MIDNIGHT;
            try {
                date = LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
                String timePart = matcher.group(2);
                if (timePart != null) {
                    time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
                }
            } catch (DateTimeParseException e) {
                showError("Could not parse date or time.");
                return;
            }
            timestamp = LocalDateTime.of(date, time);
            if (timestamp.isAfter(LocalDateTime.now())) {
                showError("Date and time cannot be in the future.");
                return;
            }
        }

        // 4) Get location
        String location = locationField.getText().trim();

        // 5) Validate category
        String category = categoryBox.getValue();
        if (category == null || category.trim().isEmpty()) {
            showError("Please select a category.");
            return;
        }

        // 6) Construct transaction model
        Transaction tx = new Transaction();
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        TransactionManager txm = rt.getTranscationManager();

        String username = rt.getCurrentUser().getUsername();
        tx.setUsername(username);
        tx.setName(name);
        tx.setAmount(amount);
        tx.setTimestamp(timestamp);
        tx.setCategory(category);
        if (!location.isEmpty()) {
            tx.setLocation(location);
        }

        // 7) Saving and going back
        txm.add(tx);
        gotoMore(event);
    }

    @FXML
    private void handleEmailChange(MouseEvent event) {

    }

    @FXML
    private void handlePasswordChange(MouseEvent event) {

    }

    @FXML
    private void handleUsernameChange(MouseEvent event) {

    }

    @FXML
    private void gotoAbout(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.ABOUT);
    }

    @FXML
    private void gotoSubscription(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.SUBSCRIPTION);
    }

    @FXML
    private void gotoFeedback(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.FEEDBACK);
    }

    @FXML
    public void gotoMore(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.MORE);
    }
}
