package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.CsvUtils;
import org.group35.util.FileUtils;
import org.group35.controller.TransactionManager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportCSVPageController {

    @FXML private Label statusLabel;
    @FXML private VBox spendingListVBox;
    @FXML private Button importButton;

    private final CsvUtils csvUtils = ApplicationRuntime.getInstance().getCsvUtils();
    private List<Transaction> importedTxs;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d");
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

    @FXML
    public void initialize() {
        importedTxs = new ArrayList<>();

        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        importButton.managedProperty().bind(importButton.visibleProperty());
        statusLabel.setVisible(false);
        importButton.setVisible(false);
    }

    private void showStatus(String message, boolean isWarning) {
        statusLabel.setVisible(!message.isEmpty());
        statusLabel.setText(message);
        if (isWarning) statusLabel.getStyleClass().add("warning");
        else statusLabel.getStyleClass().removeAll("warning");
    }

    @FXML
    public void handleSelect(ActionEvent actionEvent) {
        importedTxs.clear();
        importButton.setVisible(false);
        refreshList();

        Window win = statusLabel.getScene().getWindow();
        File file = FileUtils.chooseFile(
                win,
                "Select CSV File",
                null,
                List.of(
                        new ExtensionFilter("CSV", "*.csv"),
                        new ExtensionFilter("All files", "*.*")
                )
        );
        if (file == null) {
            showStatus("Selection cancelled.", true);
            return;
        }

        try {
            List<Map<String,Object>> raw = csvUtils.readTransactions(
                    file.toPath(),
                    StandardCharsets.UTF_8,
                    null
            );
            String user = ApplicationRuntime.getInstance()
                    .getCurrentUser()
                    .getUsername();

            importedTxs.clear();
            for (Map<String,Object> row : raw) {
                Transaction tx = TransactionManager.fromMap(row, user);
                importedTxs.add(tx);
            }

            refreshList();
            if (!importedTxs.isEmpty()) importButton.setVisible(true);
            showStatus("Loaded " + importedTxs.size() + " transactions.", false);
        } catch (IOException e) {
            importedTxs.clear();
            showStatus("Failed to read CSV: " + e.getMessage(), false);
        }
    }

    private void refreshList() {
        spendingListVBox.getChildren().clear();
        for (Transaction tx : importedTxs) {
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

        BigDecimal amt = tx.getAmount();
        Label amtLbl = new Label("$ " + amt.toPlainString());
        amtLbl.getStyleClass().add("spending-amount");

        box.getChildren().addAll(icon, info, amtLbl);
        return box;
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

    @FXML
    public void handleImport(ActionEvent actionEvent) {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        TransactionManager txm = rt.getTranscationManager();
        String username = rt.getCurrentUser().getUsername();
        for (Transaction tx : importedTxs) {
            tx.setUsername(username);
            txm.add(tx);
        }
        gotoSpending(actionEvent);
    }

    @FXML
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.SPENDING);
    }
}
