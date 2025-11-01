package com.example.lantara.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.lantara.MainApp;

import java.io.IOException;

public class SplashViewController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        playWelcomeAnimation();
    }

    private void playWelcomeAnimation() {
        GaussianBlur blur = new GaussianBlur(10.0);
        welcomeLabel.setEffect(blur);
        welcomeLabel.setOpacity(0); 

        TranslateTransition translate = new TranslateTransition(Duration.seconds(2.5), welcomeLabel);
        translate.setFromY(-100); 
        translate.setToY(0);     

        Timeline timeline = new Timeline();
        KeyValue kvBlur = new KeyValue(blur.radiusProperty(), 0.0);
        KeyValue kvOpacity = new KeyValue(welcomeLabel.opacityProperty(), 1.0);
        
        KeyFrame kf = new KeyFrame(Duration.seconds(2.5), kvBlur, kvOpacity);
        timeline.getKeyFrames().add(kf);

        ParallelTransition parallelTransition = new ParallelTransition(translate, timeline);

        parallelTransition.setOnFinished(event -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openLoginScreen();
            closeSplashScreen();
        });

        parallelTransition.play();
    }

    private void openLoginScreen() {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("view/login-view.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("LANTARA - Login");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSplashScreen() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }
}