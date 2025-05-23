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
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import org.group35.util.TimezoneUtils;
import org.group35.util.CurrencyUtils;

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

    // 新增：本地化信息的UI元素（需要在FXML中添加fx:id）
    @FXML
    private Label timezoneLabel;
    @FXML
    private Label currencyLabel;
    @FXML
    private Label exchangeRateLabel;
    @FXML
    private Label lastUpdatedLabel;

    public void initialize() {
        // 新增：加载本地化信息
        loadLocalInfo();
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
    /**
     * 新增方法：加载本地化信息
     */
    private void loadLocalInfo() {
        // 异步获取本地化信息，避免阻塞UI
        TimezoneUtils.getCurrentLocalInfoAsync()
                .thenAccept(localInfo -> {
                    // 在JavaFX主线程中更新UI
                    Platform.runLater(() -> updateLocalInfoDisplay(localInfo));
                })
                .exceptionally(throwable -> {
                    System.err.println("Error loading local info: " + throwable.getMessage());
                    // 使用默认信息
                    Platform.runLater(() -> updateLocalInfoDisplay(TimezoneUtils.getCurrentLocalInfo()));
                    return null;
                });
    }

    /**
     * 新增方法：更新本地化信息显示
     */
    private void updateLocalInfoDisplay(TimezoneUtils.LocalInfo localInfo) {
        try {
            if (timezoneLabel != null) {
                timezoneLabel.setText(localInfo.getTimezone());
            }

            if (currencyLabel != null) {
                String formattedCurrency = CurrencyUtils.formatCurrencyName(localInfo.getCurrency());
                currencyLabel.setText(formattedCurrency);
            }

            if (exchangeRateLabel != null) {
                exchangeRateLabel.setText(localInfo.getExchangeRate());
            }

            if (lastUpdatedLabel != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
                String updateTime = localInfo.getUpdateTime().format(formatter);
                lastUpdatedLabel.setText("Last updated: " + updateTime);
            }

            System.out.println("Local info updated - Timezone: " + localInfo.getTimezone() +
                    ", Currency: " + localInfo.getCurrency());
        } catch (Exception e) {
            System.err.println("Error updating local info display: " + e.getMessage());
        }
    }

    /**
     * 新增方法：手动刷新本地化信息
     */
    @FXML
    private void refreshLocalInfo() {
        TimezoneUtils.refreshCache();
        loadLocalInfo();
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