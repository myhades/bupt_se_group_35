package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmEntryPageController implements Initializable {

    @FXML private Label previousPageLabel;
    @FXML private VBox hintContainer;
    @FXML private VBox loadContainer;
    @FXML private VBox inputContainer;
    @FXML private Arc spinnerArc;

    private ProgramStatus fromStatus;
    private Boolean isProcessing;

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

        hintContainer.managedProperty().bind(hintContainer.visibleProperty());
        loadContainer.managedProperty().bind(loadContainer.visibleProperty());
        inputContainer.managedProperty().bind(inputContainer.visibleProperty());
        hintContainer.setVisible(false);
        loadContainer.setVisible(true);
        inputContainer.setVisible(false);

        spinnerArc.setRadiusX(24);
        spinnerArc.setRadiusY(24);
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), spinnerArc);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
    }

    @FXML
    private void handleSave(ActionEvent event) {
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        isProcessing = false;
        loadContainer.setVisible(false);
        hintContainer.setVisible(true);
        inputContainer.setVisible(true);
    }

    @FXML
    public void goBack(Event e) {
        ApplicationRuntime.getInstance().navigateTo(fromStatus);
    }
}