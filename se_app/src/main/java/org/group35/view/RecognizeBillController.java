package org.group35.view;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.group35.AIModel.CaptureTest;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.LoggerHelper;
import org.group35.util.SceneManager;
import org.group35.util.imageToBase64;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecognizeBillController implements Initializable {

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Button captureButton;

    @FXML
    private Button processButton;

    @FXML
    private ImageView previewImage;

    @FXML
    private StackPane cameraContainer;

    @FXML
    private VBox placeholderBox;

    private Webcam webcam;
    private ScheduledExecutorService cameraExecutor;
    private BufferedImage capturedImage;
    private boolean isCapturing = false;
    private WebcamPanel webcamPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoggerHelper.info("Initializing RecognizeBillController");

        // 设置用户名称
        if (ApplicationRuntime.getInstance().getCurrentUser() != null) {
            userNameLabel.setText(ApplicationRuntime.getInstance().getCurrentUser().getUsername());
        }

        // 初始化界面，预览按钮最初禁用
        processButton.setDisable(true);

        // 初始化摄像头
        initializeCamera();
    }

    private void initializeCamera() {
        Thread cameraInitThread = new Thread(() -> {
            try {
                // 获取默认摄像头
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    Platform.runLater(() -> showAlert("摄像头错误", "未找到摄像头。请确保摄像头已连接并正常工作。"));
                    return;
                }

                // 设置摄像头分辨率
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open();

                // 创建Swing面板（不直接添加到JavaFX，而是在后台捕获图像）
                webcamPanel = new WebcamPanel(webcam);
                webcamPanel.setFPSDisplayed(true);
                webcamPanel.setDisplayDebugInfo(false);
                webcamPanel.setImageSizeDisplayed(true);
                webcamPanel.setMirrored(true);

                // 在JavaFX线程中更新UI
                Platform.runLater(() -> {
                    placeholderBox.setVisible(false);
                    isCapturing = true;

                    // 开始摄像头预览
                    startCameraPreview();
                });

            } catch (Exception e) {
                LoggerHelper.error("摄像头初始化错误：" + e.getMessage());
                Platform.runLater(() -> showAlert("摄像头错误", "初始化摄像头时出错：" + e.getMessage()));
            }
        });

        cameraInitThread.setDaemon(true);
        cameraInitThread.start();
    }

    private void startCameraPreview() {
        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
        cameraExecutor.scheduleAtFixedRate(() -> {
            if (webcam != null && webcam.isOpen() && isCapturing) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        updatePreview(image);
                    }
                } catch (Exception e) {
                    LoggerHelper.error("摄像头预览错误：" + e.getMessage());
                }
            }
        }, 0, 33, TimeUnit.MILLISECONDS); // 约30fps
    }

    private void updatePreview(BufferedImage image) {
        try {
            // 将BufferedImage转换为JavaFX的Image
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            Platform.runLater(() -> {
                Image fxImage = new Image(in);
                previewImage.setImage(fxImage);
            });
        } catch (IOException e) {
            LoggerHelper.error("更新预览错误：" + e.getMessage());
        }
    }

    @FXML
    private void handleCaptureButton(ActionEvent event) {
        if (webcam != null && webcam.isOpen()) {
            capturedImage = webcam.getImage();
            if (capturedImage != null) {
                updatePreview(capturedImage);
                processButton.setDisable(false);
                isCapturing = false;  // 暂停实时预览
                LoggerHelper.info("图像已捕获");
            } else {
                LoggerHelper.warn("无法捕获图像");
                showAlert("捕获错误", "无法捕获图像，请重试。");
            }
        } else {
            LoggerHelper.warn("摄像头未打开");
            showAlert("摄像头错误", "摄像头未打开或不可用。");
        }
    }

    @FXML
    private void handleProcessButton(ActionEvent event) {
        if (capturedImage == null) {
            showAlert("处理错误", "没有可处理的图像。请先捕获图像。");
            return;
        }

        processButton.setDisable(true);
        captureButton.setDisable(true);

        // 创建处理线程
        Thread processThread = new Thread(() -> {
            try {
                LoggerHelper.info("开始处理账单图像");

                // 转换图像为Base64
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(capturedImage, "PNG", outputStream);
                byte[] imageData = outputStream.toByteArray();
                String base64Image = imageToBase64.IToBase64(imageData);

                // 从应用程序运行时获取用户设置信息
                double savingGoal = 1000.0; // 默认值
                double monthlyIncome = 5000.0; // 默认值

                // 使用CaptureTest处理图像
                CaptureTest captureTest = new CaptureTest(savingGoal, monthlyIncome);
                String prompt = captureTest.buildCapturePrompt();
                String requestBody = captureTest.buildRequestBody(base64Image, prompt);
                String response = captureTest.DeepSeekCalling(requestBody);

                // 解析响应
                if (response != null) {
                    LoggerHelper.info("账单处理完成");
                    JSONObject jsonResponse = new JSONObject(response);

                    // 更新UI
                    Platform.runLater(() -> {
                        processButton.setDisable(false);
                        captureButton.setDisable(false);
                        showAlert("处理成功", "账单已成功处理，可以继续捕获新的账单。");
                        // TODO: 显示处理结果，例如添加到支出记录中
                    });
                } else {
                    LoggerHelper.warn("处理账单失败");
                    Platform.runLater(() -> {
                        processButton.setDisable(false);
                        captureButton.setDisable(false);
                        showAlert("处理失败", "处理账单时出错。请重试。");
                    });
                }

            } catch (Exception e) {
                LoggerHelper.error("处理账单错误：" + e.getMessage());
                Platform.runLater(() -> {
                    processButton.setDisable(false);
                    captureButton.setDisable(false);
                    showAlert("处理错误", "处理账单时出错：" + e.getMessage());
                });
            }
        });

        processThread.setDaemon(true);
        processThread.start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        // 返回上一页面
        SceneManager.showHomePage();
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        // 导航到仪表盘
        SceneManager.showHomePage();
    }

    @FXML
    private void handleSpendingButton(ActionEvent event) {
        // 当前已在支出页面，无需操作
    }

    @FXML
    private void handlePlanButton(ActionEvent event) {
        // TODO: 导航到计划页面
    }

    @FXML
    private void handleMoreButton(ActionEvent event) {
        // TODO: 导航到更多页面
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        // 登出操作
        ApplicationRuntime.getInstance().setCurrentUser(null);
        SceneManager.showLoginPage();
    }

    @FXML
    private void handleCloseButton(ActionEvent event) {
        // 关闭应用程序
        Platform.exit();
    }

    private void releaseCamera() {
        isCapturing = false;
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
            try {
                if (!cameraExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    cameraExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cameraExecutor.shutdownNow();
                LoggerHelper.error("关闭摄像头线程错误：" + e.getMessage());
            }
        }

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            LoggerHelper.info("摄像头已关闭");
        }
    }

    // 当控制器被销毁时释放资源
    public void destroy() {
        releaseCamera();
    }
}