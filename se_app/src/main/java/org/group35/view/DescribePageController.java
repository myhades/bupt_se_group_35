package org.group35.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.service.AudioRecognition;
import org.group35.util.AudioUtils;
import org.group35.util.LogUtils;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DescribePageController implements Initializable {

    @FXML private Button recordButton;
    @FXML private Button stopButton;
    @FXML private Button continueButton;
    @FXML private Label hintProcessLabel;
    @FXML private TextArea describeArea;

    private final AudioUtils audioUtils = ApplicationRuntime.getInstance().getAudioService();
    private boolean isRecording = false;
    private boolean isProcessing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recordButton.managedProperty().bind(recordButton.visibleProperty());
        stopButton.managedProperty().bind(stopButton.visibleProperty());
        hintProcessLabel.managedProperty().bind(hintProcessLabel.visibleProperty());
        continueButton.managedProperty().bind(continueButton.visibleProperty());

        continueButton.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            String txt = describeArea.getText();
                            return txt != null && !txt.trim().isEmpty();
                        },
                        describeArea.textProperty()
                )
        );

        toggleRecording(false);
        toggleProcessing(false);
    }

    @FXML
    private void handleRecord(ActionEvent event) {
        if (isProcessing) return;
        try {
            audioUtils.startRecording();
            toggleRecording(true);
        } catch (LineUnavailableException e) {
            LogUtils.error("Unable to start audio recording: " + e.getMessage());
        }
    }

    @FXML
    private void handleStop(ActionEvent event) {
        try {
            byte[] wavData = audioUtils.stopRecording();
            toggleRecording(false);
            toggleProcessing(true);
            AudioRecognition
                .transcribeAsync(wavData)
                .whenComplete((text, err) -> {
                    Platform.runLater(() -> {
                        toggleProcessing(false);
                        if (err != null) {
                            LogUtils.error("Transcription failed: " + err.getMessage());
                        } else {
                            String currentText = describeArea.getText() != null
                                    ? describeArea.getText()
                                    : "";
                            describeArea.setText(currentText + " " + text);
                        }
                    });
                });
        } catch (IOException e) {
            LogUtils.error("Error stopping audio recording: " + e.getMessage());
        } finally {
            audioUtils.shutdown();
        }
    }

    private void toggleRecording(boolean isRecording) {
        this.isRecording = isRecording;
        stopButton.setVisible(isRecording);
        recordButton.setVisible(!isRecording);
    }

    private void toggleProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
        hintProcessLabel.setVisible(isProcessing);

    }

    @FXML
    private void handleContinue(ActionEvent event) {
        String description = describeArea.getText() != null
                ? describeArea.getText()
                : "";
        if (isRecording || isProcessing || description.isEmpty()) return;
        Map<String,Object> params = new HashMap<>();
        params.put("description",  description);
        params.put("needsProcess", Boolean.TRUE);
        params.put("processType",  "text");
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.CONFIRM_ENTRY, params);
    }

    @FXML
    public void gotoSpending(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}
