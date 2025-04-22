package org.group35.view;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
public class PlanController {
    @FXML
    private Button recommendationButton;

    public void initialize() {
        // 开启自动换行
        recommendationButton.setWrapText(true);
    }
}
