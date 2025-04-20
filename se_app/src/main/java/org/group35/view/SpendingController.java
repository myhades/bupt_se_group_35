package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.group35.controller.UserManager;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

import java.util.Optional;

public class SpendingController {

    @FXML
    public void initialize() {}

    @FXML
    private void gotoDashboard(ActionEvent event) {

        ApplicationRuntime.getInstance().gotoHome();
    }

    @FXML
    private void gotoSpending(ActionEvent event) {

        ApplicationRuntime.getInstance().gotoSpending();
    }
}
