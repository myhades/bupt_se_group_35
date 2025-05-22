package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.net.URL;
import java.util.ResourceBundle;


public class ConfirmEntryPageController implements Initializable {

    @FXML private Label previousPageLabel;

    private ProgramStatus fromStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();

        Object fromPageObj = rt.getNavParam("fromPage");
        Object fromStatusObj = rt.getNavParam("fromStatus");
        String prevPage = (fromPageObj instanceof String)
                ? ((String) fromPageObj) : "UNKNOWN";
        fromStatus = (fromStatusObj instanceof ProgramStatus)
                ? ((ProgramStatus) fromStatusObj) : ProgramStatus.HOME;

        previousPageLabel.setText(prevPage);
    }

    public void goBack(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(fromStatus);
    }
}
