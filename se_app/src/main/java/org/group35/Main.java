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
        // Basic stage configurations
        Label label = new Label("Monora");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Monora");
        primaryStage.setScene(scene);
        primaryStage.show();
        LoginPage LOGINPAGE = new LoginPage();
        LOGINPAGE.start(primaryStage);
    }

    public static void main(String[] args) {
        initialize();

        launch(args);
    }

    private static void initialize() {
        // Initialize settings
        Settings settings = new Settings();

        settings.setLogLevel("INFO");
        settings.setFileLoggingEnabled(true);
        settings.setLogFilePath("./log/app.log");

        // Initialize logger
        LoggerHelper.configureLogLevel(settings);

        LoggerHelper.info("Application initialization complete.");

    }
}
