package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.service.AIAssistant;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.group35.util.LogUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SpendingPageController {

    @FXML private VBox spendingListVBox;
    @FXML private TextField searchField;
    @FXML private Button sortNameButton;
    @FXML private Button sortAmountButton;
    @FXML private Button sortDateButton;
    @FXML private Button updateSummaryButton;
    @FXML private Label budgetLeftAmount;
    @FXML private Label summaryText;
    @FXML private Label noEntryText;
    @FXML private Arc loadingArc;

    private enum SortType  { DATE, NAME, AMOUNT }
    private enum SortOrder { ASC, DESC }
    private SortType  currentSortType  = null;
    private SortOrder currentSortOrder = null;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final Color[] COLOR_PALETTE = {
            Color.web("#2E86AB"),
            Color.web("#1B4F72"),
            Color.web("#7B241C"),
            Color.web("#633974"),
            Color.web("#117A65"),
            Color.web("#B03A2E"),
            Color.web("#5D6D7E"),
            Color.web("#4A235A"),
            Color.web("#1D8348"),
            Color.web("#7E5109"),
            Color.web("#4B4B4B"),
            Color.web("#1F2A44")
    };

    private static class SortControl {
        final SortType   type;
        final Button     button;
        final String     ascSvgPath;
        final String     descSvgPath;

        SortControl(SortType type, Button button, String ascSvgPath, String descSvgPath) {
            this.type        = type;
            this.button      = button;
            this.ascSvgPath  = ascSvgPath;
            this.descSvgPath = descSvgPath;
        }
    }
    private List<SortControl> sortControls;

    @FXML
    public void initialize() {

        updateSummaryButton.managedProperty().bind(updateSummaryButton.visibleProperty());
        summaryText.managedProperty().bind(summaryText.visibleProperty());
        noEntryText.managedProperty().bind(noEntryText.visibleProperty());
        loadingArc.managedProperty().bind(loadingArc.visibleProperty());

        loadingArc.setRadiusX(24);
        loadingArc.setRadiusY(24);
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), loadingArc);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
        setProcessing("initial");

        sortControls = List.of(
                new SortControl(SortType.DATE, sortDateButton,
                        "M20 17H23L19 21L15 17H18V3H20V17M8 5C4.14 5 1 8.13 1 12C1 15.87 4.13 19 8 19C11.86 19 15 15.87 15 12C15 8.13 11.87 5 8 5M8 7.15C10.67 7.15 12.85 9.32 12.85 12C12.85 14.68 10.68 16.85 8 16.85C5.32 16.85 3.15 14.68 3.15 12C3.15 9.32 5.32 7.15 8 7.15M7 9V12.69L10.19 14.53L10.94 13.23L8.5 11.82V9",
                        "M18 7H15L19 3L23 7H20V21H18V7M8 5C4.14 5 1 8.13 1 12C1 15.87 4.13 19 8 19C11.86 19 15 15.87 15 12C15 8.13 11.87 5 8 5M8 7.15C10.67 7.15 12.85 9.32 12.85 12C12.85 14.68 10.68 16.85 8 16.85C5.32 16.85 3.15 14.68 3.15 12C3.15 9.32 5.32 7.15 8 7.15M7 9V12.69L10.19 14.53L10.94 13.23L8.5 11.82V9"),
                new SortControl(SortType.NAME, sortNameButton,
                        "M19 17H22L18 21L14 17H17V3H19M11 13V15L7.67 19H11V21H5V19L8.33 15H5V13M9 3H7C5.9 3 5 3.9 5 5V11H7V9H9V11H11V5C11 3.9 10.11 3 9 3M9 7H7V5H9Z",
                        "M19 7H22L18 3L14 7H17V21H19M11 13V15L7.67 19H11V21H5V19L8.33 15H5V13M9 3H7C5.9 3 5 3.9 5 5V11H7V9H9V11H11V5C11 3.9 10.11 3 9 3M9 7H7V5H9Z"),
                new SortControl(SortType.AMOUNT, sortAmountButton,
                        "M19 17H22L18 21L14 17H17V3H19V17M9 13H7C5.9 13 5 13.9 5 15V16C5 17.11 5.9 18 7 18H9V19H5V21H9C10.11 21 11 20.11 11 19V15C11 13.9 10.11 13 9 13M9 16H7V15H9V16M9 3H7C5.9 3 5 3.9 5 5V9C5 10.11 5.9 11 7 11H9C10.11 11 11 10.11 11 9V5C11 3.9 10.11 3 9 3M9 9H7V5H9V9Z",
                        "M19 7H22L18 3L14 7H17V21H19M9 21H5V19H9V18H7C5.9 18 5 17.11 5 16V15C5 13.9 5.9 13 7 13H9C10.11 13 11 13.9 11 15V19C11 20.11 10.11 21 9 21M9 15H7V16H9M7 3H9C10.11 3 11 3.9 11 5V9C11 10.11 10.11 11 9 11H7C5.9 11 5 10.11 5 9V5C5 3.9 5.9 3 7 3M7 9H9V5H7Z")
        );

        searchField.textProperty().addListener((obs, old, nw) -> refreshList());
        sortControls.forEach(ctrl ->
                ctrl.button.setOnAction(e -> onSortPressed(ctrl.type))
        );
        this.onSortPressed(SortType.NAME);
        refreshList();

        this.setBudgetLeftAmount();
    }

    private void setProcessing(String status) {
        updateSummaryButton.setVisible(status.equals("initial"));
        loadingArc.setVisible(status.equals("processing"));
        summaryText.setVisible(status.equals("done"));
    }

    private void setBudgetLeftAmount(){
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        BigDecimal totalBudgetBD = rt.getUserManager().getMonthlyBudget();
        double totalBudget = totalBudgetBD != null ? totalBudgetBD.doubleValue() : 2000.0;
        double usedBudget = calculateUsedBudget();
        double availableBudget = totalBudget - usedBudget;
        if (availableBudget < 0) availableBudget = 0;
        budgetLeftAmount.setText("$"+String.format("%.0f", availableBudget));
    }

    private void setAISummary(String summary){
        summaryText.setText(summary);
        setProcessing("done");
    }

    private void onSortPressed(SortType type) {
        if (type != currentSortType) {
            currentSortType  = type;
            currentSortOrder = SortOrder.ASC;
        } else {
            currentSortOrder = (currentSortOrder == SortOrder.DESC
                    ? SortOrder.ASC
                    : SortOrder.DESC);
        }
        refreshList();
    }

    private void refreshList() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        TransactionManager txm = rt.getTranscationManager();
        String user = rt.getCurrentUser().getUsername();
        List<Transaction> txs = txm.getByUser(user);

        // Apply keyword filtering if any
        String kw = searchField.getText().trim().toLowerCase();
        if (!kw.isEmpty()) {
            txs = txm.searchByKeyword(txs, kw);
        }

        // Apply sorting
        if (currentSortType != null) {
            txs = switch (currentSortType) {
                case DATE -> txm.sortByTimestamp(txs, currentSortOrder == SortOrder.ASC);
                case NAME -> txm.sortByName(txs, currentSortOrder == SortOrder.ASC);
                case AMOUNT -> txm.sortByAmount(txs, currentSortOrder == SortOrder.ASC);
            };
        }

        noEntryText.setVisible(txs.isEmpty());
        setTransactionList(txs);
        updateSortIcons();
    }

    private void updateSortIcons() {
        for (SortControl ctrl : sortControls) {
            Button btn= ctrl.button;
            SVGPath icon = (SVGPath)btn.getGraphic();
            boolean active = ctrl.type == currentSortType;
            btn.getStyleClass().removeAll("active");
            if (active) {
                btn.getStyleClass().add("active");
                icon.setContent(currentSortOrder == SortOrder.ASC
                                ? ctrl.ascSvgPath
                                : ctrl.descSvgPath
                );
            } else {
                icon.setContent(ctrl.descSvgPath);
            }
        }
    }

    @FXML void updateSummary(ActionEvent event){
        setProcessing("processing");
        CompletableFuture<String> future = AIAssistant.AISummaryAsync();
        future.whenComplete((text, err) -> Platform.runLater(() -> {
            if (err != null) {
                LogUtils.error("AI summary generation failed: " + err.getMessage());
                setProcessing("initial");
            } else {
                setAISummary(text);
            }
        }));
    }

    @FXML
    private void gotoManualEntry(ActionEvent event) {
        Map<String,Object> params = new HashMap<>();
        params.put("fromPage", "Manual");
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.CONFIRM_ENTRY, params);
    }

    @FXML
    private void gotoDescribe(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.DESCRIBE);
    }

    @FXML
    private void gotoRecognizeBill(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.RECOGNIZE_BILL);
    }

    @FXML
    private void gotoImportCSV(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.IMPORT_CSV);
    }

    /**
     * Clears and repopulates the list with the given transactions.
     */
    public void setTransactionList(List<Transaction> transactions) {
        spendingListVBox.getChildren().clear();
        for (Transaction tx : transactions) {
            spendingListVBox.getChildren().add(createSpendingItem(tx));
        }
    }

    /**
     * Build the same HBox structure you had in FXML, but with real data.
     */
    private HBox createSpendingItem(Transaction tx) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("spending-item");

        box.setUserData(tx.getId());
        box.setOnMouseClicked(evt -> {
            String uuid = (String) box.getUserData();
            onSpendingItemClicked(uuid);
        });

        Circle circle = new Circle(20);
        circle.getStyleClass().add("icon-circle");
        char letterChar = tx.getName().toUpperCase().charAt(0);
        circle.setFill(getColorForLetter(letterChar));
        Label letter = new Label(letterChar+"");
        letter.getStyleClass().add("icon-letter");
        StackPane icon = new StackPane(circle, letter);

        Label nameLbl = new Label(tx.getName());
        nameLbl.getStyleClass().add("spending-name");
        Label dateLbl = new Label(tx.getTimestamp()
                .toLocalDate()
                .format(DATE_FMT));
        dateLbl.getStyleClass().add("spending-date");
        VBox info = new VBox(0, nameLbl, dateLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        BigDecimal amt = tx.getAmount().setScale(2, RoundingMode.HALF_UP);
        Label amtLbl = new Label("$ " + amt.toPlainString());
        amtLbl.getStyleClass().add("spending-amount");

        box.getChildren().addAll(icon, info, amtLbl);
        return box;
    }

    private void onSpendingItemClicked(String uuid) {
        Map<String,Object> params = new HashMap<>();
        params.put("uuid", uuid);
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.CONFIRM_ENTRY, params);
    }

    /**
     * Returns a Color for letters A–Z;
     * anything else falls back to mid-gray.
     */
    private Color getColorForLetter(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            int idx = (ch - 'A') % COLOR_PALETTE.length;
            return COLOR_PALETTE[idx];
        }
        return Color.web("#555555");
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
}
