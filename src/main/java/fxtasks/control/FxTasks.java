package fxtasks.control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class FxTasks extends Application {

    public static void main(String[] args) {
        launch(FxTasks.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("java version = " + System.getProperty("java.version"));
        Parent root = FXMLLoader.load(getClass().getResource("/fxTasks.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/fxTasks.css");
        stage.setScene(scene);
        stage.setTitle("fxTasks");
        stage.show();
    }
}
