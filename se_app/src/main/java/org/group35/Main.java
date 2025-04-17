package org.group35;

import javafx.application.Application;
import javafx.stage.Stage;
import org.group35.config.Settings;
import org.group35.util.LoggerHelper;
import org.group35.util.SceneManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.persistence.PersistentDataManager;

import org.group35.controller.UserManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setPrimaryStage(primaryStage);
        SceneManager.showLoginPage();
    }

    public static void main(String[] args) {
        initialize();
        test_endpoint();
        launch(args);
    }

    private static void initialize() {

        // Initialize settings
        Settings settings = new Settings();
        settings.setLogLevel("INFO");

        // Initialize logger
        LoggerHelper.configureLogLevel(settings);

        // Initialize persistent data manager
        PersistentDataManager.initialize(settings);

        // Initialize application runtime (singleton)
        ApplicationRuntime.getInstance();

        LoggerHelper.info("Application initialization complete.");

    }

    private static void test_endpoint() {

//        UserManager userManager = ApplicationRuntime.getInstance().getUserManager();
//        userManager.registerUser("admin", "admin");

    }

    @Override
    public void stop() throws Exception {
        PersistentDataManager.saveStore();
        super.stop();
    }
}
