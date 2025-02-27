package se233.project2.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.Launcher;
import se233.project2.view.GameOverMenu;
import se233.project2.view.GameStage;

public class GameStageController {
    private static void startCountdown(Label countdownLabel, Runnable runnable) {
        countdownLabel.setVisible(true);

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            int countdown = 3; // Starting count

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // 1 second
                    lastUpdate = now;
                    if (countdown >= 0) {
                        if (countdown == 0) {
                            countdownLabel.setText("The Game Starts");
                        }else{
                            countdownLabel.setText(String.valueOf(countdown));}
                        countdown--;
                    } else {
                        this.stop();
                        countdownLabel.setVisible(false);
                        Platform.runLater(runnable);
                        // Proceed to start the game
                        //startGame();
                    }
                }
            }
        };
        Media countdown = new Media(Launcher.class.getResource("audio/countdown.mp3").toString());
        MediaPlayer countdownPlayer = new MediaPlayer(countdown);
        countdownPlayer.play();
        timer.start();
    }

    public static void onLoad(Label countdownLabel, GameStage gameStage) {
        startCountdown(countdownLabel,()-> {
            GameLoop gameLoop=new GameLoop(gameStage);
            DrawingLoop drawingLoop=new DrawingLoop(gameStage);
            GenerateEnemyTask generateEnemyTask =new GenerateEnemyTask(gameStage);
            Thread thread=new Thread(gameLoop);
            Thread thread1=new Thread(drawingLoop);
            Thread thread2=new Thread(generateEnemyTask);
            thread.setDaemon(true);
            thread1.setDaemon(true);
            thread2.setDaemon(true);
            thread.start();
            thread1.start();
            thread2.start();
        });

    }

    public static void changeToGameOver(String labelText,GameStage gameStage) {
        GameOverMenu gameOverMenu=new GameOverMenu(gameStage,new Label(labelText));
        Scene scene=new Scene(gameOverMenu);
        Launcher.primaryStage.setScene(scene);
    }

}
