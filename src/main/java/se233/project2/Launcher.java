package se233.project2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.project2.controller.DrawingLoop;
import se233.project2.controller.GameLoop;
import se233.project2.controller.GenerateAsteroidTask;
import se233.project2.view.GameMenu;
import se233.project2.view.GameStage;

public class Launcher extends Application {
    private GameMenu gameMenu;
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        gameMenu = new GameMenu();
        Scene scene = new Scene(gameMenu);
        primaryStage = stage;
        primaryStage.setScene(scene);
        primaryStage.setTitle("Asteroid Game");
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
