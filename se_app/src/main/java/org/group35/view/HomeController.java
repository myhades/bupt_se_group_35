package org.group35.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.group35.util.SceneManager;
import org.group35.util.LoggerHelper;

public class HomeController {

    @FXML
    private ImageView avatarImage;

    @FXML
    private Button recognizeBillButton;

    @FXML
    public void initialize() {
        // 初始化头像（如果有）
        // avatarImage.setImage(new Image(...));

        // 其他初始化代码
        LoggerHelper.info("首页初始化完成");
    }

    /**
     * 处理账单识别按钮点击事件
     * 导航到账单识别页面
     */
    @FXML
    private void handleRecognizeBillButton(ActionEvent event) {
        LoggerHelper.info("导航到账单识别页面");
        SceneManager.showRecognizeBillPage();
    }
}