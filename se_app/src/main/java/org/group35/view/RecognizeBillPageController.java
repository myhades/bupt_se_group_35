package org.group35.view;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.CameraUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


public class RecognizeBillPageController implements Initializable {

    @FXML private StackPane previewStack;
    @FXML private ImageView cameraView;
    @FXML private Text hintText;
    @FXML private HBox buttonPanel;

    private final CameraUtils cameraUtils = ApplicationRuntime.getInstance().getCameraService();
    private boolean previewPaused = false;
    private Image snapshot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind cameraView to fill container
        cameraView.setPreserveRatio(true);
        cameraView.fitWidthProperty().bind(previewStack.widthProperty());
        cameraView.fitHeightProperty().bind(previewStack.heightProperty());

        // Initial hint and button visibility
        hintText.setText("Place in the frame\nthen click to capture");
        buttonPanel.managedProperty().bind(buttonPanel.visibleProperty());
        buttonPanel.setVisible(false);

        // Click to capture
        cameraView.setOnMouseClicked(this::handleCapture);
        cameraUtils.startCamera(cameraView);
    }

    /**
     * Handle click on the camera preview to capture a photo.
     */
    private void handleCapture(MouseEvent event) {
        if (previewPaused) return;

        // Prepare animation
        Rectangle flash = new Rectangle(
                previewStack.getWidth(),
                previewStack.getHeight(),
                Color.WHITE
        );
        flash.setOpacity(1);
        previewStack.getChildren().add(flash);
        FadeTransition fade = new FadeTransition(Duration.millis(800), flash);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            previewStack.getChildren().remove(flash);
        });

        // Take snapshot
        snapshot = cameraUtils.captureSnapshot();
        if (snapshot != null) {
            cameraView.setImage(snapshot);
        } else {
            return;
        }
        fade.play();

        // Pause preview and update UI
        cameraUtils.stopCamera();
        previewPaused = true;
        hintText.setText("Image captured");
        buttonPanel.setVisible(true);
    }

    /**
     * Discard snapshot and resume live preview.
     */
    @FXML
    private void handleRetake(ActionEvent event) {
        if (!previewPaused) return;
        previewPaused = false;
        buttonPanel.setVisible(false);
        hintText.setText("Place in the frame\nthen click to capture");
        cameraUtils.startCamera(cameraView);
    }

    /**
     * Continue with captured image.
     */
    @FXML
    private void handleContinue(ActionEvent event) {
        Map<String,Object> params = new HashMap<>();
        params.put("snapshot", snapshot);
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.CONFIRM_ENTRY, params);
    }

    @FXML
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}
