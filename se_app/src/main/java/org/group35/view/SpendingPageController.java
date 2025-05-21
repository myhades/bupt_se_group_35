package org.group35.view;

import javafx.scene.layout.HBox;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SpendingPageController {

    @FXML private VBox spendingListVBox;
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
        setTransactionList(getSampleTransactions());
    }

    @FXML
    private void gotoManualEntry(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MANUAL_ENTRY);
    }

    @FXML
    private void gotoRecogBill(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.RECOGNIZE_BILL);
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

        // Icon: circle + first letter
        Circle circle = new Circle(20);
        circle.getStyleClass().add("icon-circle");
        char letterChar = tx.getName().toUpperCase().charAt(0);
        circle.setFill(getColorForLetter(letterChar));
        Label letter = new Label(letterChar+"");
        letter.getStyleClass().add("icon-letter");
        StackPane icon = new StackPane(circle, letter);

        // Name + date
        Label nameLbl = new Label(tx.getName());
        nameLbl.getStyleClass().add("spending-name");
        Label dateLbl = new Label(tx.getTimestamp()
                .toLocalDate()
                .format(DATE_FMT));
        dateLbl.getStyleClass().add("spending-date");
        VBox info = new VBox(2, nameLbl, dateLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Amount
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

    /**
     * Provide sample transactions (Netflix and McDonald) for initial load.
     */
    private List<Transaction> getSampleTransactions() {
        Transaction netflix = new Transaction();
        netflix.setName("Netflix Family");
        netflix.setTimestamp(LocalDateTime.of(2025, 12, 11, 0, 0));
        netflix.setAmount(new BigDecimal("-29.9"));

        Transaction mcDonald = new Transaction();
        mcDonald.setName("McDonald");
        mcDonald.setTimestamp(LocalDateTime.of(2025, 12, 25, 0, 0));
        mcDonald.setAmount(new BigDecimal("-15.9"));

        return List.of(netflix, mcDonald);
    }
}
