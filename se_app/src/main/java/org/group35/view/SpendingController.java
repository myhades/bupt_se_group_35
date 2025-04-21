package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.group35.runtime.ApplicationRuntime;

public class SpendingController {

    @FXML
    public void initialize() {}

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
