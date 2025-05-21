package org.group35.view;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.CameraUtils;
import org.group35.util.LogUtils;

import java.net.URL;
import java.util.ResourceBundle;


public class RecognizeBillPageController implements Initializable {

    @FXML private StackPane previewStack;
    @FXML private ImageView cameraView;
    private final CameraUtils cameraUtils = ApplicationRuntime.getInstance().getCameraService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cameraView.setPreserveRatio(true);
        cameraView.fitWidthProperty().bind(previewStack.widthProperty());
        cameraView.fitHeightProperty().bind(previewStack.heightProperty());
        cameraView.setOnMouseClicked(this::handleCapture);
        cameraUtils.startCamera(cameraView);
    }

    /**
     * Handle click on the camera preview to capture a photo.
     */
    private void handleCapture(MouseEvent event) {

        // Take snapshot
        Image snapshot = cameraUtils.captureSnapshot();
        if (snapshot != null) {
            // [API] Placeholder
            LogUtils.info("Snapshot captured and ready for upload.");
        } else {
            LogUtils.warn("Snapshot capture failed.");
        }

        // Play animation
        Rectangle flash = new Rectangle(
                previewStack.getWidth(),
                previewStack.getHeight(),
                Color.WHITE
        );
        flash.setOpacity(1);
        previewStack.getChildren().add(flash);
        FadeTransition fade = new FadeTransition(Duration.millis(400), flash);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            previewStack.getChildren().remove(flash);
        });
        fade.play();
    }

    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}
