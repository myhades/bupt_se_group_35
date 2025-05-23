package org.group35.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.group35.runtime.ApplicationRuntime;

public class ProfilePageController {

    @FXML
    public void initialize() {

    }


    @FXML
    private void handleLoginKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
//            handleLogin(null);  // Trigger the login action when ENTER is pressed
        }
    }

    @FXML
    private void handleEmailChange(MouseEvent event) {

    }

    @FXML
    private void handlePasswordChange(MouseEvent event) {

    }

    @FXML
    private void handleUsernameChange(MouseEvent event) {

    }

    @FXML
    private void gotoAbout(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.ABOUT);
    }

    @FXML
    private void gotoSubscription(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.SUBSCRIPTION);
    }

    @FXML
    private void gotoFeedback(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.FEEDBACK);
    }

    @FXML
    private void gotoMore(MouseEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.MORE);
    }
}
