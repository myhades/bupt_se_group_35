package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;

import java.net.URL;
import java.util.ResourceBundle;

public class MorePageController implements Initializable {

    @FXML private Label userLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User current = ApplicationRuntime.getInstance().getCurrentUser();
        if (current != null) {
            userLabel.setText(current.getUsername());
        } else {
            userLabel.setText("Guest");
        }
    }
}
