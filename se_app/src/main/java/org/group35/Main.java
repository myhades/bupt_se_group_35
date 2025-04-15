package org.group35;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import org.group35.config.Settings;
import org.group35.util.LoggerHelper;
import org.group35.persistence.PersistentDataManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            URL resource = getClass().getResource("/org/group35/view/LoginPage.fxml");
            if (resource == null) {
                throw new IllegalStateException("Cannot find LoginPage.fxml");
            }
            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root, 1000, 500);

            primaryStage.setTitle("Monora");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // Initialize persistent data manager
        PersistentDataManager.initialize(settings);

        LoggerHelper.info("Application initialization complete.");

    }

    @Override
    public void stop() throws Exception {
        PersistentDataManager.saveStore();
        super.stop();
    }
}
