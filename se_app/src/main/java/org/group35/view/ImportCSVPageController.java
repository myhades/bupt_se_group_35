package org.group35.view;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import org.group35.runtime.ApplicationRuntime;

public class ImportCSVPageController {

    @FXML
    public void initialize() {

    }

    @FXML
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.SPENDING);
    }
}
