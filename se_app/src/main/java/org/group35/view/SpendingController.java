package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

public class SpendingController {

    @FXML
    public void initialize() {}

    @FXML
    private void gotoManualEntry(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MANUAL_ENTRY);
    }

    @FXML
    private void gotoRecogBill(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.RECOGNIZE_BILL);
    }
}
