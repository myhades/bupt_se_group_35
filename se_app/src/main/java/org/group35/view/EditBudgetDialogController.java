package org.group35.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class EditBudgetDialogController {

    @FXML
    private TextField budgetInputField;
    @FXML
    private Label errorLabel;
    private Stage dialogStage;
    private String newBudget;
    private boolean confirmed = false;

    // Stage switch
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Get New Budget
    public String getNewBudget() {
        return newBudget;
    }

    // Check "Confirm"
    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleSave() {
        String input = budgetInputField.getText().trim();
        if (isNumeric(input)) {
            newBudget = input;
            confirmed = true;
            errorLabel.setText("New budget has been updated.");
            errorLabel.getStyleClass().removeAll("error-text");
            if (!errorLabel.getStyleClass().contains("success-text"))
                errorLabel.getStyleClass().add("success-text");
            errorLabel.setVisible(true);
            System.out.println("New Budget: " + newBudget);

            // Delay for 1.5s
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> dialogStage.close());
            }).start();
        } else {
            errorLabel.setText("Improper input for budget setting.");
            errorLabel.setVisible(true);
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
