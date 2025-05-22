package org.group35.view;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.LogUtils;

public class ManualEntryPageController {
    @FXML private TextArea aiTextArea;
    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private TextField categoryField;

    @FXML
    private void initialize() {}

    @FXML
    private void handleMicro(ActionEvent event) {
        LogUtils.debug("Micro button has been clicked.");
    }

    @FXML
    private void handleConvert(ActionEvent event) {
        LogUtils.debug("Convert button clicked.");
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
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}