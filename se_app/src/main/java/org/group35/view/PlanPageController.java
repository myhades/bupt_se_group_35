package org.group35.view;

import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

public class PlanPageController {
    @FXML
    private Button recommendationButton;
    @FXML
    private PieChart budgetPieChart;
    public void initialize() {
        recommendationButton.setWrapText(true);
        budgetPieChart.getStyleClass().add("pie-chart");
        insertBudgetData();
    }
    /**
     * Clears and sets the budget pie chart with the given data.
     * @param data the list of PieChart.Data to display
     */
    public void setBudgetPieData(List<PieChart.Data> data) {
        budgetPieChart.getData().clear();
        budgetPieChart.getData().addAll(data);
    }

    /**
     * You can remove or replace this call when injecting real data.
     */
    public void insertBudgetData() {
        // Demo for pie chart
        List<PieChart.Data> demoPie = Arrays.asList(
                new PieChart.Data("Used", 80),
                new PieChart.Data("Available", 20)
        );
        setBudgetPieData(demoPie);
        budgetPieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #115371;");
        budgetPieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #8498a9;");
    }
}
