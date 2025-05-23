package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.group35.runtime.ApplicationRuntime;

public class SubscriptionPageController {

    @FXML
    public void initialize() {
        // 初始化页面
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.MORE);
    }

    @FXML
    public void upgradeToPro(ActionEvent actionEvent) {
        showUpgradeAlert("Pro");
    }

    @FXML
    public void upgradeToPremium(ActionEvent actionEvent) {
        showUpgradeAlert("Premium");
    }

    private void showUpgradeAlert(String plan) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Subscription Upgrade");
        alert.setHeaderText("Upgrade to " + plan + " Plan");
        alert.setContentText("This is a demo. In a real application, this would redirect to a payment page.");
        alert.showAndWait();
    }
}
