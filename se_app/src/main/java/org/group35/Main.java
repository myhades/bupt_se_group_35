package org.group35;

import javafx.application.Application;
import javafx.stage.Stage;
import org.group35.config.Settings;
import org.group35.util.LogUtils;
import org.group35.util.SceneManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.persistence.PersistentDataManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        SceneManager.showLoginPage();
    }

    public static void main(String[] args) {
        initialize();
        launch(args);
    }

    private static void initialize() {

        // Initialize settings (singleton)
        Settings settings = Settings.getInstance();

        // Initialize logger (static)
        LogUtils.configureLogLevel(settings);

        // Initialize persistent data manager (static)
        PersistentDataManager.initialize(settings);

        // Initialize application runtime (singleton)
        ApplicationRuntime.getInstance();

        LogUtils.info("Application initialization complete.");

    }

    @Override
    public void stop() throws Exception {
        PersistentDataManager.saveStore();
        ApplicationRuntime.getInstance().shutdown();
        super.stop();
    }
}
