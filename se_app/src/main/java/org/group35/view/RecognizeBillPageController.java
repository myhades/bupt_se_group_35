package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.net.URL;
import java.util.ResourceBundle;

public class RecognizeBillPageController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}
