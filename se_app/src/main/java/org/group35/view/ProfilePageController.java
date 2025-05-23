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
import javafx.scene.control.TextInputDialog;
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

import javafx.scene.input.MouseEvent;
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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProfilePageController implements Initializable {

    @FXML private Label warningLabel;
    @FXML private VBox hintContainer;

    @FXML private ImageView avatarImage;
    @FXML final Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/group35/view/assets/images/default_avatar.png")));
    @FXML private TextField nameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> TimezoneField;
    @FXML private TextField LocationField;
    @FXML private ComboBox<String> categoryBox;

    private Boolean isWarning;

    private UserManager uManager;
    private String currentname;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        hideWarning();

        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        this.uManager = runtime.getUserManager();
        currentname = runtime.getCurrentUser().getUsername();

        setTimezones();
        setCategoryBox();

        categoryBox.setValue(categoryBox.getItems().get(0));
        LocationField.setPromptText(uManager.getLocation(currentname));

        Image avatar = ImageUtils.fromBase64(uManager.getUserAvatar(currentname));
        if (avatar != null) {
            avatarImage.setImage(avatar);
            double radius = avatarImage.getFitWidth() / 2.0;
            Circle clip = new Circle(radius, radius, radius);
            avatarImage.setClip(clip);
        }
        else {
            resetAvatarToDefault();
            LogUtils.error("No Avatar");
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
        List<String> categories = uManager.getCategory();
        categoryBox.setItems(FXCollections.observableArrayList(categories));
    }


    /**
     * Hide the inline warning label.
     */
    private void hideWarning() {
        //TODO
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    /**
     * Show an inline warning message above the input field.
     */
    private void showError(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    @FXML
    private void handleSelectAvatar(MouseEvent event) {
        Window win = categoryBox.getScene().getWindow();
        try{
            String selectedBase64 = ImageUtils.chooseAndProcessImage(win, 256);
            Image selectedImage = ImageUtils.fromBase64(selectedBase64);
            if (selectedImage != null) {  // only update when a pic actually selected
                avatarImage.setImage(selectedImage);
                double radius = avatarImage.getFitWidth() / 2.0;
                Circle clip = new Circle(radius, radius, radius);
                avatarImage.setClip(clip);
                uManager.setAvatar(selectedBase64);

            }
        }
        catch (IOException e) {
            LogUtils.error("Invalid File");
            resetAvatarToDefault();
        }
    }

    private void resetAvatarToDefault() {
        avatarImage.setImage(defaultAvatar);
        double radius = avatarImage.getFitWidth() / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        avatarImage.setClip(clip);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        hideWarning();

        String initialPwd = passwordField.getText().trim();
        if (initialPwd.isEmpty()) {
            return;
        }
        if (initialPwd.length() < 8 || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showError("Password must be at least 8 characters and include letters and digit.");
            return;
        }
        uManager.setHashedPassword(PasswordUtils.hashPassword(initialPwd));


        String category = categoryBox.getValue();

        //TODO: store to User

        String timezone = TimezoneField.getValue();
        uManager.setTimezone(timezone);

        String location = LocationField.getText();
        uManager.setLocation(location);

        //TODO: add more validation


    }

    @FXML
    private void handleCancel(ActionEvent event) {
        //TODO: clear all textfiled
    }


    @FXML
    public void gotoMore(Event e) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MORE);
    }

    @FXML
    private void addCategory(ActionEvent event) {
        // 实现添加分类的逻辑
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter new category name");
        dialog.setContentText("Category:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(category -> {
            if (!category.isEmpty()) {
                categoryBox.getItems().add(category);
                categoryBox.getSelectionModel().select(category);
            }
        });
    }

    @FXML
    private void removeCategory(ActionEvent event) {
        // 实现删除分类的逻辑
        String selected = categoryBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            categoryBox.getItems().remove(selected);
        } else {
            // 可以显示警告信息
            showError("Please select a category to remove");
        }
    }
}