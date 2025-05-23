package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.AudioUtils;
import org.group35.util.LogUtils;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class DescribePageController implements Initializable {

    @FXML private Button recordButton;
    @FXML private Button stopButton;
    @FXML private Label hintProcessLabel;

    private final AudioUtils audioUtils = ApplicationRuntime.getInstance().getAudioService();
    private boolean isRecording = false;
    private boolean isProcessing = false;
    private String base64Audio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recordButton.managedProperty().bind(recordButton.visibleProperty());
        stopButton.managedProperty().bind(stopButton.visibleProperty());
        hintProcessLabel.managedProperty().bind(hintProcessLabel.visibleProperty());
        recordButton.setVisible(true);
        stopButton.setVisible(false);
        hintProcessLabel.setVisible(false);
    }

    @FXML
    private void handleRecord(ActionEvent event) {
        if (isProcessing) return;

        try {
            audioUtils.startRecording();
            isRecording = true;
            recordButton.setVisible(false);
            stopButton.setVisible(true);
        } catch (LineUnavailableException e) {
            LogUtils.error("Unable to start audio recording: " + e.getMessage());
        }
    }

    @FXML
    private void handleStop(ActionEvent event) {
        isRecording = false;
        try {
            byte[] wavData = audioUtils.stopRecording();
            base64Audio = Base64.getEncoder().encodeToString(wavData);
        } catch (IOException e) {
            LogUtils.error("Error stopping audio recording: " + e.getMessage());
        } finally {
            audioUtils.shutdown();
        }
        isRecording = false;
        isProcessing = true;
        stopButton.setVisible(false);
        recordButton.setVisible(true);
        hintProcessLabel.setVisible(true);
    }

    @FXML
    private void handleContinue(ActionEvent event) {
        if (isRecording || isProcessing) return;
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.CONFIRM_ENTRY);
    }

    @FXML
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}
