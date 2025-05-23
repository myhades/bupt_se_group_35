package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.group35.controller.TransactionManager;
import org.group35.controller.UserManager;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.service.BillsRecognition;
import org.group35.util.FileUtils;
import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;
import org.group35.util.PasswordUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfilePageController implements Initializable {

    @FXML private Label previousPageLabel;
    @FXML private Label warningLabel;
    @FXML private VBox hintContainer;
    @FXML private VBox loadContainer;
    @FXML private VBox inputContainer;
    @FXML private VBox emptyInputContainer;
    @FXML private Arc spinnerArc;
    @FXML private ImageView avatarImage;
    @FXML private TextField nameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> TimezoneField;
    @FXML private TextField LocationField;
    @FXML private ComboBox<String> categoryBox;

    private ProgramStatus fromStatus;
    private Boolean isWarning;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User currentuser = runtime.getCurrentUser();
        Object fromPageObj = runtime.getNavParam("fromPage");
        Object fromStatusObj = runtime.getNavParam("fromStatus");

        setTimezones();
        setCategoryBox();

        Image avatar = ImageUtils.fromBase64(currentuser.getAvatar());
        if (avatar != null) {
            avatarImage.setImage(avatar);
            double radius = avatarImage.getFitWidth() / 2.0;
            Circle clip = new Circle(radius, radius, radius);
            avatarImage.setClip(clip);
        }
        else {
            LogUtils.error("No Avatar");
        }

    }


    private void populateFields(Transaction tx) {
        nameField.setText(tx.getName());
        passwordField.setText(tx.getAmount().toPlainString());
        LocalDateTime ts = tx.getTimestamp();
        String loc = tx.getLocation();
        LocationField.setText(loc != null ? loc : "");
        String cat = tx.getCategory();
        if (cat != null && !cat.isEmpty()) {
            categoryBox.setValue(cat);
        }
    }

    private void setTimezones() {
        ObservableList<String> timezones = FXCollections.observableArrayList(
                ZoneId.getAvailableZoneIds()
                        .stream()
                        .sorted()
                        .collect(Collectors.toList())
        );

        TimezoneField.setItems(timezones);

        // Set default to system timezone
        TimezoneField.setValue(ZoneId.systemDefault().toString());
    }

    private void setCategoryBox() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        List<String> categories = rt.getCurrentUser().getCategory();
        categoryBox.setItems(FXCollections.observableArrayList(categories));
    }

    /**
     * Show an inline warning message above the input field.
     */
//    private void showWarning(String msg, boolean isWarning) { //TODO
//        if (loginGroup.isVisible()) {
//            warningLabel.setText(msg);
//            warningLabel.setVisible(true);
//            warningLabel.setManaged(true);
//        } else {
//            confirmWarningLabel.setText(msg);
//            confirmWarningLabel.setVisible(true);
//            confirmWarningLabel.setManaged(true);
//        }
//    }

    /**
     * Hide the inline warning label.
     */
    private void hideWarning() { //TODO
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    private void toggleProcessing(boolean isProcessing) {
//        this.isProcessing = isProcessing;
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

//    @FXML
//    private void handleSave(ActionEvent event) {
//
//        String name = nameField.getText();
//        if (name == null || name.trim().isEmpty()) {
//            showError("Name cannot be empty.");
//            return;
//        }
//
//        // 2) Validate amount
//        BigDecimal amount;
//        try {
//            amount = new BigDecimal(passwordField.getText().trim());
//            if (amount.compareTo(BigDecimal.ZERO) == 0) {
//                showError("Amount must be not be zero.");
//                return;
//            }
//        } catch (NumberFormatException e) {
//            showError("Invalid amount format.");
//            return;
//        }
//
//        // 3) Validate date/time
////        String dtInput = TimezoneField.getText() == null
////                ? ""
////                : TimezoneField.getText().trim();
//        LocalDateTime timestamp;
//        timestamp = LocalDateTime.now();
////        if (dtInput.isEmpty()) {
////            timestamp = LocalDateTime.now();
////        } else {
////            Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?: (\\d{2}:\\d{2}))?$");
////            Matcher matcher = pattern.matcher(dtInput);
////            if (!matcher.matches()) {
////                showError("Invalid date/time. Use yyyy-MM-dd (HH:mm).");
////                return;
////            }
////            LocalDate date;
////            LocalTime time = LocalTime.MIDNIGHT;
////            try {
////                date = LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
////                String timePart = matcher.group(2);
////                if (timePart != null) {
////                    time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
////                }
////            } catch (DateTimeParseException e) {
////                showError("Could not parse date or time.");
////                return;
////            }
////            timestamp = LocalDateTime.of(date, time);
////            if (timestamp.isAfter(LocalDateTime.now())) {
////                showError("Date and time cannot be in the future.");
////                return;
////            }
////        }
//
//        // 4) Get location
//        String location = LocationField.getText().trim();
//
//        // 5) Validate category
//        String category = categoryBox.getValue();
//        if (category == null || category.trim().isEmpty()) {
//            showError("Please select a category.");
//            return;
//        }
//
//        // 6) Construct transaction model
//        Transaction tx = new Transaction();
//        ApplicationRuntime rt = ApplicationRuntime.getInstance();
//        TransactionManager txm = rt.getTranscationManager();
//
//        String username = rt.getCurrentUser().getUsername();
//        tx.setUsername(username);
//        tx.setName(name);
//        tx.setAmount(amount);
//        tx.setTimestamp(timestamp);
//        tx.setCategory(category);
//        if (!location.isEmpty()) {
//            tx.setLocation(location);
//        }
//
//        // 7) Saving and going back
//        txm.add(tx);
//        goBack(event);
//    }

    private void showError(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    @FXML
    private void handleSelectAvatar(ActionEvent event) {
        Window win = passwordField.getScene().getWindow();
        File file = FileUtils.chooseFile(
                win,
                "Select CSV File",
                null,
                List.of(
                        new FileChooser.ExtensionFilter("CSV", "*.csv"),
                        new FileChooser.ExtensionFilter("All files", "*.*")
                )
        );
        if (file == null) {
//            showStatus("Selection cancelled.", true);
            return;
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        hideWarning();
//        toggleProcessing(false);
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User currentuser = runtime.getCurrentUser();

//        String username = nameField.getText().trim();
//
//        if (username.isEmpty()) {
//            showError("Please enter a username.");
//            return;
//        }
//        if (!username.matches("[A-Za-z0-9]+")) {
//            showError("Username can only contain letters and digits.");
//            return;
//        }
//        if (UserManager.getPersistentUsers().stream()
//                .anyMatch(u -> u.getUsername().equals(username))) {
//            showError("That username is already taken.");
//            return;
//        }
        //TODO: Store to User


        String initialPwd = passwordField.getText().trim();
        if (initialPwd.isEmpty()) {
            showError("Please enter a password.");
            return;
        }
        if (initialPwd.length() < 8 || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showError("Password must be at least 8 characters and include letters and digit.");
            return;
        }
        currentuser.setHashedPassword(PasswordUtils.hashPassword(initialPwd));


        String category = categoryBox.getValue();

        //TODO: store to User

        String timezone = TimezoneField.getValue();
        currentuser.setTimezone(timezone);

        String location = LocationField.getText().trim();
        currentuser.setLocation(location);

        //TODO: add more validation

        // switch to confirm group
//        loginGroup.setManaged(false);
//        loginGroup.setVisible(false);
//        confirmGroup.setManaged(true);
//        confirmGroup.setVisible(true);
//        confirmPasswordField.clear();
//        Platform.runLater(confirmPasswordField::requestFocus);


    }

    @FXML
    private void handleCancel(ActionEvent event) {
//        toggleProcessing(false);
        //TODO: clear all textfiled
    }

    @FXML
    public void goBack(Event e) {
        ApplicationRuntime.getInstance().navigateTo(fromStatus);
    }

    @FXML
    public void gotoMore(Event e) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MORE);
    }
}