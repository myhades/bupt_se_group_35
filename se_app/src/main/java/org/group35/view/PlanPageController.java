package org.group35.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

public class PlanPageController {
    @FXML
    private Button recommendationButton;
    @FXML
    public PieChart budgetPieChart;
    @FXML
    private Button editBudgetButton;
    @FXML
    private Button AIButton;
    @FXML
    private Label budgetStatusLabel;
    @FXML
    private Label availableAmountLabel;
    @FXML
    private Label availablePercentLabel;
    @FXML
    private Label usedAmountLabel;
    @FXML
    private Label usedPercentLabel;
    @FXML
    private TextArea aiSuggestionTextArea;

    public void initialize() {
        try {
            if (recommendationButton != null) {
                recommendationButton.setWrapText(true);
            }
            if (budgetPieChart != null) {
                budgetPieChart.getStyleClass().add("pie-chart");
            }
            loadBudgetData();
            updateBudgetDisplay();
            System.out.println("PlanPageController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error in PlanPageController.initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void setAISuggestion(String suggestion) {
        aiSuggestionTextArea.setText(suggestion);
    }

    private void loadBudgetData() {
        try {
            System.out.println("Loading budget data...");
        } catch (Exception e) {
            System.err.println("Error loading budget data: " + e.getMessage());
        }
    }

    private void updateBudgetDisplay() {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();

            BigDecimal totalBudgetBD = runtime.getUserManager().getMonthlyBudget();
            double totalBudget = totalBudgetBD != null ? totalBudgetBD.doubleValue() : 2000.0;

            double usedBudget = calculateUsedBudget();

            double availableBudget = totalBudget - usedBudget;
            if (availableBudget < 0) availableBudget = 0;

            double usedPercentage = totalBudget > 0 ? (usedBudget / totalBudget) * 100 : 0;
            double availablePercentage = totalBudget > 0 ? (availableBudget / totalBudget) * 100 : 0;

            if (budgetPieChart != null) {
                List<PieChart.Data> budgetData = Arrays.asList(
                        new PieChart.Data("Used", usedBudget),
                        new PieChart.Data("Available", availableBudget)
                );
                setBudgetPieData(budgetData);

                if (!budgetPieChart.getData().isEmpty()) {
                    budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
                    budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
                }
            }

            updateBudgetLabels(totalBudget, usedBudget, availableBudget, usedPercentage, availablePercentage);
            if (budgetStatusLabel != null) {

                if (totalBudgetBD == null || totalBudget <= 0.0) {
                    budgetStatusLabel.setText("Welcome to set your budget here.");
                } else if (usedBudget > totalBudget) {
                    budgetStatusLabel.setText("Oops! It seems that you are over budget!");
                } else {
                    budgetStatusLabel.setText("You're currently on a budget.");
                }
            }

            System.out.println("Budget display updated - Total: $" + totalBudget + ", Used: $" + usedBudget);
        } catch (Exception e) {
            System.err.println("Error updating budget display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBudgetLabels(double total, double used, double available, double usedPercent, double availablePercent) {
        try {
            if (availableAmountLabel != null) {
                availableAmountLabel.setText("$" + String.format("%.0f", available));
            }
            if (availablePercentLabel != null) {
                availablePercentLabel.setText(String.format("%.0f", availablePercent) + "%");
            }
            if (usedAmountLabel != null) {
                usedAmountLabel.setText("$" + String.format("%.0f", used));
            }
            if (usedPercentLabel != null) {
                usedPercentLabel.setText(String.format("%.0f", usedPercent) + "%");
            }
        } catch (Exception e) {
            System.err.println("Error updating budget labels: " + e.getMessage());
        }
    }

    @FXML
    private void gotoEditBudget(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.EDIT_BUDGET);
    }

    @FXML
    private void gotoRecommendation(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.RECOMMENDATION);
    }

    @FXML
    private void gotoAISuggestion(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.AI_SUGGESTION);
    }

    public void setBudgetPieData(List<PieChart.Data> data) {
        if (budgetPieChart != null) {
            budgetPieChart.getData().clear();
            budgetPieChart.getData().addAll(data);
        }
    }

    public void updateTotalBudget(double newBudget) {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal newBudgetBD = BigDecimal.valueOf(newBudget);
            runtime.getUserManager().setMonthlyBudget(newBudgetBD);
            updateBudgetDisplay();
            System.out.println("Total budget updated to: $" + String.format("%.0f", newBudget));
        } catch (Exception e) {
            System.err.println("Error updating total budget: " + e.getMessage());
        }
    }

    private double calculateUsedBudget() {
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User currentUser = runtime.getCurrentUser();

        if (currentUser == null) {
            return 0.0;
        }

        try {
            List<Transaction> transactions = runtime.getTranscationManager().getByUser(currentUser.getUsername());

            LocalDateTime now = LocalDateTime.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            double totalExpenses = 0.0;
            for (Transaction transaction : transactions) {
                // check whether this transaction happens in this month
                LocalDateTime transactionTime = transaction.getTimestamp();
                if (transactionTime != null &&
                        transactionTime.getMonthValue() == currentMonth &&
                        transactionTime.getYear() == currentYear) {

                    BigDecimal amount = transaction.getAmount();
                    if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
                        totalExpenses += amount.abs().doubleValue(); // less than 0
                    }
                }
            }

            return totalExpenses;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public double getUsedBudget() {
      return calculateUsedBudget();
    }

    public double getTotalBudget() {
        try {
            ApplicationRuntime runtime = ApplicationRuntime.getInstance();
            BigDecimal totalBudgetBD = runtime.getUserManager().getMonthlyBudget();
            return totalBudgetBD != null ? totalBudgetBD.doubleValue() : 2000.0;
        } catch (Exception e) {
            System.err.println("Error getting total budget: " + e.getMessage());
            return 2000.0;
        }
    }

    public void addExpense(double expenseAmount) {
        System.out.println("Added expense: $" + String.format("%.2f", expenseAmount));
        updateBudgetDisplay();
    }
}