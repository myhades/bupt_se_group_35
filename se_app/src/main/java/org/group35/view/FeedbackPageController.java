package org.group35.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.group35.util.SceneManager;
import org.group35.runtime.ApplicationRuntime;

public class FeedbackPageController {

    @FXML
    private VBox feedbackContentVBox;

    private int selectedScore = 0;

    private static final String[] FEEDBACK_TEXTS = {
            "We sincerely apologize for falling short of your expectations. Your feedback is valuable to us, and we’ll work hard to improve. Please let us know how we can make things right.",
            "We regret that your experience wasn’t satisfactory. Thank you for sharing your concerns—we’ll address them to provide better service in the future.",
            "Thank you for your feedback. We appreciate your input and will strive to enhance our service to meet your expectations next time.",
            "We’re glad you had a positive experience! Thank you for your rating—we’ll keep working to maintain and improve our service.",
            "Thank you so much for your perfect rating! We’re thrilled to meet your expectations and look forward to serving you again."
    };

    @FXML
    private void goBack(ActionEvent event) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.MORE);
    }

    @FXML
    private void rate1(MouseEvent event) { setScore(1); }
    @FXML
    private void rate2(MouseEvent event) { setScore(2); }
    @FXML
    private void rate3(MouseEvent event) { setScore(3); }
    @FXML
    private void rate4(MouseEvent event) { setScore(4); }
    @FXML
    private void rate5(MouseEvent event) { setScore(5); }

    private void setScore(int score) {
        selectedScore = score;
        feedbackContentVBox.getChildren().clear();
        if (score >= 1 && score <= 5) {
            Label feedbackLabel = new Label(FEEDBACK_TEXTS[score - 1]);
            feedbackLabel.getStyleClass().add("info-text");
            feedbackLabel.setWrapText(true);
            feedbackContentVBox.getChildren().add(feedbackLabel);
        }
    }
}
