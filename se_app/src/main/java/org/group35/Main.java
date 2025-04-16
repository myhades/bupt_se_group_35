package org.group35;

import org.group35.config.Settings;
import org.group35.util.LoggerHelper;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.group35.view.LoginPage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello, JavaFX!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("JavaFX Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        LoginPage LOGINPAGE = new LoginPage();
        LOGINPAGE.start(primaryStage);
    }

    public static void main(String[] args) {
        initialize();
        launch(args); // 启动 JavaFX 应用
    }

    private static void initialize() {
        // Create a new Settings instance; these settings can later be loaded from a JSON file.
        Settings settings = new Settings();

        // Optionally modify the settings:
        settings.setLogLevel("INFO");         // Options: TRACE, DEBUG, INFO, WARN, ERROR.
        settings.setFileLoggingEnabled(true);   // Enable file logging.
        settings.setLogFilePath("./log/app.log");  // Set path for the log file.

        // Configure the logging framework based on these settings.
        LoggerHelper.configureLogLevel(settings);

    }
}
