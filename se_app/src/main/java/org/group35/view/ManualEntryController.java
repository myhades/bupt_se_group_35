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

public class ManualEntryController {
    @FXML
    private TextArea aiTextArea;

    @FXML
    private TextField nameField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField categoryField;

    @FXML
    private ImageView avatarImageView;
    @FXML
    private ImageView DashboardImageView;
    @FXML
    private ImageView SpendingImageView;
    @FXML
    private ImageView PlanImageView;
    @FXML
    private ImageView MoreImageView;
    @FXML
    private ImageView BackButtonImageView;
    @FXML
    private ImageView MicrophoneImageView;
    @FXML
    private ImageView QuitButtonImageView;

    @FXML
    private void initialize() {
//        avatarImageView.setImage(new Image("/org/group35/view/assets/Test_Avatar.jpg"));
//        DashboardImageView.setImage(new Image("/org/group35/view/assets/DashBoard_Logo.jpg"));
//        SpendingImageView.setImage(new Image("/org/group35/view/assets/Spending_Logo.jpg"));
//        PlanImageView.setImage(new Image("/org/group35/view/assets/Plan_Logo.jpg"));
//        MoreImageView.setImage(new Image("/org/group35/view/assets/More_Logo.jpg"));
//        BackButtonImageView.setImage(new Image("/org/group35/view/assets/BackButton.jpg"));
//        MicrophoneImageView.setImage(new Image("/org/group35/view/assets/Micro.jpg"));
//        QuitButtonImageView.setImage(new Image("/org/group35/view/assets/QuitButton.jpg"));
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        System.out.println("Dashboard button clicked.");
    }

    @FXML
    private void handleSpending(ActionEvent event) {
        System.out.println("Spending button clicked.");
    }

    @FXML
    private void handlePlan(ActionEvent event) {
        System.out.println("Plan button clicked.");
    }

    @FXML
    private void handleMore(ActionEvent event) {
        System.out.println("More button clicked.");
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        System.out.println("LogOut button clicked.");
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        System.out.println("Quit button clicked.");
        if (showConfirmationDialog("Exit Confirmation", "Are you sure you want to exit the application?", "Yes", "No")) {
            Platform.exit();
            System.exit(0);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        System.out.println("Back button clicked.");
    }

    @FXML
    private void handleMicro(ActionEvent event) {
        System.out.println("Micro button clicked.");
        //Test code
        aiTextArea.setText("API here！");
        //api here
    }

    @FXML
    private void handleConvert(ActionEvent event) {
        System.out.println("Convert button clicked.");
        // 判断 aiTextArea 是否为空
        if (aiTextArea.getText().trim().isEmpty()) {
            showAlertDialog("Input Error", "Please enter some text in the AI Recognition area before converting.", "OK");
            return;
        }

//        // Call the API for identification (here, a simple boolean is used to simulate whether the identification is successful or not)
//        boolean isRecognitionSuccessful = simulateAIRecognition(aiTextArea.getText());
//
//        if (isRecognitionSuccessful) {
//            // Suppose that after successful recognition, Name, Amount, and Category are parsed out (here it is just a simulated assignment).
//            String name = "Parsed Name";
//            String amount = "100.0";
//            String category = "Parsed Category";
//
//            nameField.setText(name);
//            amountField.setText(amount);
//            categoryField.setText(category);
//
//            showInfoDialog("Recognition Successful", "The AI recognition was successful and the data has been filled.", "OK");
//        } else {
//            showAlertDialog("Recognition Error", "The AI recognition was invalid. Please enter the text again.", "OK");
//        }
//    }
//    }

}

    @FXML
    private void handleSave(ActionEvent event) {
        System.out.println("Save button clicked.");
        String name = nameField.getText();
        String amountStr = amountField.getText();
        String category = categoryField.getText();

        if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
            showAlertDialog("Input Error", "Please fill in the complete name, amount, and category information.", "OK");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            // The data can be saved to the database or other storage,just printed out here
            System.out.println("Saved data: Name: " + name + ", Amount: " + amount + ", Category: " + category);
            showInfoDialog("Save Successful", "The data has been successfully saved.", "OK");
        } catch (NumberFormatException e) {
            showAlertDialog("Input Error", "The amount must be a valid number.", "OK");
        }
    }

    // Show Information
    private void showInfoDialog(String title, String content, String buttonText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ButtonType okButton = new ButtonType(buttonText);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }
    // Show Confirmation
    private boolean showConfirmationDialog(String title, String content, String confirmButtonText, String cancelButtonText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ButtonType confirmButton = new ButtonType(confirmButtonText);
        ButtonType cancelButton = new ButtonType(cancelButtonText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    // Show Alert
    private void showAlertDialog(String title, String content, String buttonText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        ButtonType okButton = new ButtonType(buttonText);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }

    @FXML
    private void gotoHome(ActionEvent event) {
        ApplicationRuntime.getInstance().gotoHome();
    }

    @FXML
    private void gotoSpending(ActionEvent event) {
        ApplicationRuntime.getInstance().gotoSpending();
    }

    @FXML
    private void gotoManualEntry(ActionEvent event) {
        ApplicationRuntime.getInstance().gotoManualEntry();
    }

    @FXML
    private void gotoRecogBill(ActionEvent event) {
        ApplicationRuntime.getInstance().gotoRecogBill();
    }
}