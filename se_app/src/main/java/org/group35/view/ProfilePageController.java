package org.group35.view;

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
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Window;
import org.group35.controller.UserManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;
import org.group35.util.PasswordUtils;

import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProfilePageController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private ImageView avatarImage;
    @FXML final Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/group35/view/assets/images/default_avatar.png")));
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> timezoneField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> categoryBox;
    private TextField categoryInputField;

    private boolean isAddingCategory = false;
    private final ApplicationRuntime rt = ApplicationRuntime.getInstance();
    private final UserManager um = rt.getUserManager();
    private final String currentUsername = rt.getCurrentUser().getUsername();;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        showStatus("");

        setTimezones();
        setCategoryBox();
        loadAvatar();
        locationField.setText(um.getLocation(currentUsername));

    }

    private void loadAvatar() {
        Image avatar = ImageUtils.fromBase64(um.getUserAvatar(currentUsername));
        if (avatar != null) {
            avatarImage.setImage(avatar);
            double radius = avatarImage.getFitWidth() / 2.0;
            Circle clip = new Circle(radius, radius, radius);
            avatarImage.setClip(clip);
        }
        else {
            resetAvatarToDefault();
        }
    }

    private void setTimezones() {
        ObservableList<String> timezones = FXCollections.observableArrayList(
                ZoneId.getAvailableZoneIds()
                        .stream()
                        .sorted()
                        .collect(Collectors.toList())
        );

        timezoneField.setItems(timezones);

        // Set default to system timezone
        timezoneField.setValue(ZoneId.systemDefault().toString());
    }

    private void setCategoryBox() {

        List<String> categories = um.getCategory();
        categoryBox.setItems(FXCollections.observableArrayList(categories));

        // 初始化输入框但不显示
        categoryInputField = new TextField();
        categoryInputField.setPromptText("Enter new category");
        categoryInputField.getStyleClass().add("input-field"); // 保持样式一致
        categoryInputField.setVisible(false);

        // 监听回车确认
        categoryInputField.setOnAction(this::confirmAddCategory);

        // 监听失去焦点事件，自动恢复 categoryBox
        categoryInputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && isAddingCategory) { // 失去焦点且处于添加模式
                revertToCategoryBox();
            }
        });

        categoryBox.setValue(categoryBox.getItems().getFirst());
    }

    private void showStatus(String message) {
        showStatus(message, false);
    }

    private void showStatus(String message, boolean isWarning) {
        statusLabel.setVisible(!message.isEmpty());
        statusLabel.setText(message);
        if (isWarning) statusLabel.getStyleClass().add("warning");
        else statusLabel.getStyleClass().removeAll("warning");
    }

    @FXML
    private void handleSelectAvatar(MouseEvent event) {
        Window win = avatarImage.getScene().getWindow();
        try{
            String selectedBase64 = ImageUtils.chooseAndProcessImage(win, 128);
            Image selectedImage = ImageUtils.fromBase64(selectedBase64);
            if (selectedImage != null) {
                avatarImage.setImage(selectedImage);
                double radius = avatarImage.getFitWidth() / 2.0;
                Circle clip = new Circle(radius, radius, radius);
                avatarImage.setClip(clip);
                um.setAvatar(currentUsername, selectedBase64);
                showStatus("Profile picture updated.");
            }
        }
        catch (IOException e) {
            LogUtils.error("Exception occurred when updating profile picture");
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

        String initialPwd = passwordField.getText().trim();
        if (initialPwd.isEmpty()) {
            return;
        }
        if (initialPwd.length() < 8 || !initialPwd.matches(".*[A-Za-z].*")
                || !initialPwd.matches(".*\\d.*")) {
            showStatus("Password must be at least 8 characters and include letters and digit.", true);
            return;
        }
        um.setHashedPassword(PasswordUtils.hashPassword(initialPwd));


        String category = categoryBox.getValue();

        //TODO: store to User

        String timezone = timezoneField.getValue();
        um.setTimezone(timezone);

        String location = locationField.getText();
        um.setLocation(currentUsername, location);

        //TODO: add more validation


    }

    @FXML
    private void handleDiscard(ActionEvent event) {
        gotoMore(event);
    }


    @FXML
    public void gotoMore(Event e) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MORE);
    }

    @FXML
    private void addCategory(ActionEvent event) {
        if (!isAddingCategory) {
            // 切换到输入模式
            isAddingCategory = true;
            categoryBox.setVisible(false);
            categoryInputField.setVisible(true);

            // 将输入框添加到原categoryBox的位置
            StackPane parent = (StackPane) categoryBox.getParent().getParent();
            parent.getChildren().add(categoryInputField);
            // 自动获取焦点
            Platform.runLater(() -> categoryInputField.requestFocus());

        }
    }

    private void confirmAddCategory(ActionEvent event) {
        String newCategory = categoryInputField.getText().trim();
        if (um.addCategory(newCategory)){
            categoryBox.getItems().add(newCategory);
            categoryBox.getSelectionModel().select(newCategory);
            LogUtils.info("Add newCategory: " + newCategory);
        }
        else {
            showStatus("Category already exists or empty input.", true);
            LogUtils.error("Add newCategory failed: " + newCategory);
        }

        // 恢复原状
        revertToCategoryBox();
    }

    @FXML
    private void removeCategory(ActionEvent event) {
        if (isAddingCategory) {
            revertToCategoryBox();
            return;
        }

        String selected = categoryBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            categoryBox.getItems().remove(selected);
            um.removeCategory(selected);
        } else {
            showStatus("Please select a category to remove.", true);
        }
    }

    private void revertToCategoryBox() {
        isAddingCategory = false;
        categoryInputField.setVisible(false);
        categoryBox.setVisible(true);
        StackPane parent = (StackPane) categoryBox.getParent().getParent();
        parent.getChildren().remove(categoryInputField);
    }
}