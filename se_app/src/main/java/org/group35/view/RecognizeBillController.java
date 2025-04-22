package org.group35.view;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.CameraUtils;
import org.group35.util.LoggerHelper;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class RecognizeBillController implements Initializable {

    @FXML private Button    captureButton;
    @FXML private Button    processButton;
    @FXML private ImageView previewImage;
    @FXML private StackPane cameraContainer;
    @FXML private VBox      placeholderBox;

    private final CameraUtils cameraService =
            ApplicationRuntime.getInstance().getCameraService();

    private BufferedImage capturedImage;

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        LoggerHelper.info("Initializing RecognizeBillController");

        // disable buttons until first frame arrives
        captureButton.setDisable(true);
        processButton.setDisable(true);

        // register for new frames and errors
        cameraService.addFXFrameListener(this::onNewFrame);
        cameraService.addErrorListener(this::onError);
    }

    /** Called on each new frame (JavaFX thread). */
    private void onNewFrame(WritableImage fxImage) {
        if (placeholderBox.isVisible()) {
            placeholderBox.setVisible(false);
            captureButton.setDisable(false);
        }
        previewImage.setImage(fxImage);
    }

    /** Called on camera errors. */
    private void onError(Exception e) {
        Platform.runLater(() ->
                showAlert("Camera Error", e.getMessage())
        );
    }

    @FXML
    private void handleCaptureButton(ActionEvent evt) {
        captureButton.setDisable(true);
        processButton.setDisable(false);

        // pause the shared preview
        cameraService.pausePreview();

        // capture exactly one frame asynchronously
        cameraService.captureFrame(raw -> {
            capturedImage = raw;
            WritableImage fx = SwingFXUtils.toFXImage(raw, null);
            Platform.runLater(() -> previewImage.setImage(fx));
            LoggerHelper.info("Captured one frame.");
        });
    }

    @FXML
    private void handleProcessButton(ActionEvent evt) {
        // TODO: implement Base64 + OCR logic using `capturedImage`
        showAlert("Info", "Processing is not implemented yet.");
    }

    @FXML
    private void handleLogoutButton(ActionEvent evt) {
        ApplicationRuntime.getInstance().logoutUser();
    }

    @FXML
    private void handleCloseButton(ActionEvent evt) {
        destroy();
        Platform.exit();
    }

    @FXML private void handlePlanButton(ActionEvent e)    {}
    @FXML private void handleMoreButton(ActionEvent e)    {}

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    /** Unregister this controller’s camera listeners. */
    public void destroy() {
        cameraService.removeFXFrameListener(this::onNewFrame);
        cameraService.removeErrorListener(this::onError);
    }

    public void gotoManualEntry(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MANUAL_ENTRY);
    }
}
