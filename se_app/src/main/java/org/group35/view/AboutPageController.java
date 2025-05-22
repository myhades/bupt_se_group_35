package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import org.group35.runtime.ApplicationRuntime;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutPageController implements Initializable {

    @FXML
    private ScrollPane softwareInfoScrollPane;

    @FXML
    private ScrollPane privacyPolicyScrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 可以在这里添加初始化逻辑
        configureScrollPanes();
    }

    private void configureScrollPanes() {
        // 可以在这里添加滚动面板的配置代码
    }

    /**
     * 返回到MORE页面
     */
    @FXML
    public void goBack(ActionEvent actionEvent) {
        ApplicationRuntime.getInstance().navigateTo(ApplicationRuntime.ProgramStatus.MORE);
    }
}
