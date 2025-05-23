package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmEntryPageController implements Initializable {

    @FXML private Label previousPageLabel;
    @FXML private Label warningLabel;
    @FXML private VBox hintContainer;
    @FXML private VBox loadContainer;
    @FXML private VBox inputContainer;
    @FXML private VBox emptyInputContainer;
    @FXML private Arc spinnerArc;
    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private TextField datetimeField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> categoryBox;

    private ProgramStatus fromStatus;
    private Boolean isProcessing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        Object fromPageObj = rt.getNavParam("fromPage");
        Object fromStatusObj = rt.getNavParam("fromStatus");
        Object needsProcessObj = rt.getNavParam("needsProcess");
        String prevPage = (fromPageObj instanceof String)
                ? ((String) fromPageObj) : "UNKNOWN";
        fromStatus = (fromStatusObj instanceof ProgramStatus)
                ? ((ProgramStatus) fromStatusObj) : ProgramStatus.HOME;
        Boolean needsProcess = (needsProcessObj instanceof Boolean)
                ? ((Boolean) needsProcessObj) : Boolean.FALSE;
        previousPageLabel.setText(prevPage);

        warningLabel.managedProperty().bind(warningLabel.visibleProperty());
        hintContainer.managedProperty().bind(hintContainer.visibleProperty());
        loadContainer.managedProperty().bind(loadContainer.visibleProperty());
        inputContainer.managedProperty().bind(inputContainer.visibleProperty());
        emptyInputContainer.managedProperty().bind(emptyInputContainer.visibleProperty());
        warningLabel.setVisible(false);
        hintContainer.setVisible(false);
        loadContainer.setVisible(true);
        inputContainer.setVisible(false);
        emptyInputContainer.setVisible(true);

        setCategoryBox();
        toggleProcessing(needsProcess);

    }

    private void setCategoryBox() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        List<String> categories = rt.getCurrentUser().getCategory();
        categoryBox.setItems(FXCollections.observableArrayList(categories));
    }

    private void toggleProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
        if (isProcessing){
            hintContainer.setVisible(false);
            loadContainer.setVisible(true);
            inputContainer.setVisible(false);
            emptyInputContainer.setVisible(true);
            spinnerArc.setRadiusX(24);
            spinnerArc.setRadiusY(24);
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), spinnerArc);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
        }
        else {
            hintContainer.setVisible(true);
            loadContainer.setVisible(false);
            inputContainer.setVisible(true);
            emptyInputContainer.setVisible(false);
        }
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
        goBack(event);
    }

    private void showError(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        toggleProcessing(false);
    }

    @FXML
    public void goBack(Event e) {
        ApplicationRuntime.getInstance().navigateTo(fromStatus);
    }
}