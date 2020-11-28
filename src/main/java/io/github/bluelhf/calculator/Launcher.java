package io.github.bluelhf.calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/logo.png"));
        ((Controller)loader.getController()).init(stage);

        stage.setTitle("Calculator");
        stage.show();
        ((Controller)loader.getController()).postInit();
    }

    public static void main() {
        launch();
    }
}
