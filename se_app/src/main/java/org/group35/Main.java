package org.group35;

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
        launch(args); // 启动 JavaFX 应用
    }
}
