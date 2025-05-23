package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

import java.math.BigDecimal;
import java.util.List;

public class EditBudgetDialogController {
    @FXML
    private TextField budgetInputField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label budgetStatusLabel;
    private String newBudget;
    private boolean confirmed = false;

    public void initialize() {
    }

    public void setNewBudget(String newBudget) {
        this.newBudget = newBudget;
    }

    public String getNewBudget() {
        return newBudget;
    }

    @FXML
    private void gotoRecommendation(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.RECOMMENDATION);
    }
    @FXML
    private void handleCancel() {
        confirmed = false;
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.PLAN);
    }

    @FXML
    private void handleSave() {
        String input = budgetInputField.getText().trim();

        // Number detection
        if (!isNumeric(input)) {
            errorLabel.setText("Budget input is invalid.");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        double budgetValue = Double.parseDouble(input);

        // Negative detection
        if (budgetValue < 0) {
            errorLabel.setText("Budget cannot be negative.");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        // Get used budget
        double usedBudget = calculateCurrentUsedBudget();

        // "Less than" detection
        if (budgetValue < usedBudget) {
            errorLabel.setText("Budget cannot be less than used amount ($" + String.format("%.0f", usedBudget) + ").");
            errorLabel.setStyle("-fx-text-fill: #bf1c1c; -fx-font-size: 13px;");
            errorLabel.setVisible(true);
            return;
        }

        // all pass
        newBudget = input;
        confirmed = true;

        // success hint
        errorLabel.setText("Budget updated successfully.");
        errorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 13px;");
        errorLabel.setVisible(true);

        System.out.println("New budget: " + newBudget);

        // Use UserManager to update budget
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal newBudgetBD = BigDecimal.valueOf(budgetValue);
            runtime.getUserManager().setMonthlyBudget(newBudgetBD);
            System.out.println("Budget updated via UserManager: $" + budgetValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return to plan page automatically
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.PLAN);
            });
        }).start();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double calculateCurrentUsedBudget() {
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User currentUser = runtime.getCurrentUser();

        if (currentUser == null) {
            return 0.0;
        }

        try {
            // Get this user's transaction through TransactionManager
            List<Transaction> transactions = runtime.getTranscationManager().getByUser(currentUser.getUsername());

            double totalExpenses = 0.0;
            for (Transaction transaction : transactions) {
                BigDecimal amount = transaction.getAmount();
                if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
                    totalExpenses += amount.abs().doubleValue(); // less than 0
                }
            }

            return totalExpenses;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

}