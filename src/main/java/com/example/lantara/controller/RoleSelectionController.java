package com.example.lantara.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.example.lantara.MainApp;

public class RoleSelectionController {

    @FXML
    private Button managerButton;

    @FXML
    private void handleManagerButton() {
        openLoginScreen("MANAJER");
    }

    @FXML
    private void handleStaffButton() {
        openLoginScreen("STAF");
    }
    
    @FXML
    private void handleBackButton() {
        try {
            Stage currentStage = (Stage) managerButton.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            boolean isMaximized = currentStage.isMaximized();
            currentStage.close();

            Parent root = FXMLLoader.load(MainApp.class.getResource("view/landing-page-view.fxml"));
            Stage landingStage = new Stage();
            landingStage.setTitle("LANTARA");
            landingStage.setScene(new Scene(root, width, height));

            if (isMaximized) {
                landingStage.setMaximized(true);
            }
            
            landingStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLoginScreen(String role) {
        try {
            Stage currentStage = (Stage) managerButton.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            boolean isMaximized = currentStage.isMaximized();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/login-view.fxml"));
            Parent root = loader.load();

            LoginViewController loginController = loader.getController();
            loginController.initRole(role);

            Stage stage = new Stage();
            stage.setTitle("LANTARA - Login " + role);
            stage.setScene(new Scene(root, width, height));

            if (isMaximized) {
                stage.setMaximized(true);
            }

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}