package com.example.lantara.controller;

import java.io.IOException;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.example.lantara.MainApp;
import com.example.lantara.model.User;

public class DashboardController {

    @FXML private AnchorPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnKendaraan;
    @FXML private Button btnPengemudi;
    @FXML private Button btnPenugasan;
    @FXML private Button btnLogout;
    @FXML private Label  welcomeLabel;

    private User currentUser;
    private Button currentButton;

    public void initData(User user) {
        this.currentUser = user;
        final String uname = user != null ? user.getUsername() : "User";
        final String cap = uname.substring(0, 1).toUpperCase() + uname.substring(1);
        welcomeLabel.setText("Selamat Datang, " + cap + "!");
        handleBtnDashboard();
    }

    @FXML
    private void handleBtnDashboard() {
        setActiveButton(btnDashboard);
        loadView("dashboard-content-view.fxml");  
    }

    @FXML
    private void handleBtnKendaraan() {
        setActiveButton(btnKendaraan);
        loadView("main-view.fxml");               
    }

    @FXML
    private void handleBtnPengemudi() {
        setActiveButton(btnPengemudi);
        loadView("driver-view.fxml");             
    }

    @FXML
    private void handleBtnPenugasan() {
        setActiveButton(btnPenugasan);
        loadView("assignment-view.fxml");         
    }

    @FXML
    private void handleBtnLogout() {
        try {
            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            boolean maximized = currentStage.isMaximized();
            currentStage.close();

            FXMLLoader fxml = new FXMLLoader(MainApp.class.getResource("view/role-selection-view.fxml"));
            Parent root = fxml.load();
            Stage stage = new Stage();
            stage.setTitle("LANTARA - Pilih Peran");
            MainApp.setStageIcon(stage);
            stage.setScene(new Scene(root, width, height));
            if (maximized) stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showError("Gagal membuka halaman pemilihan peran.", e);
        }
    }

    private void setActiveButton(Button button) {
        if (button == null) return;

        if (currentButton != null) {
            currentButton.getStyleClass().remove("selected");
        }
        if (!button.getStyleClass().contains("selected")) {
            button.getStyleClass().add("selected");
        }
        currentButton = button;

        playRippleAnimation(button);
    }

    private void playRippleAnimation(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(160), button);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.05); st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);

        FadeTransition ft = new FadeTransition(Duration.millis(160), button);
        ft.setFromValue(0.8); ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);

        new ParallelTransition(st, ft).play();
    }

    private void loadView(String fxmlFile) {
        try {
            if (contentArea == null) return;

            URL url = MainApp.class.getResource("view/" + fxmlFile);
            if (url == null) {
                throw new IOException("File FXML tidak ditemukan: view/" + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Pane view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof MainViewController m) {
                m.initData(currentUser);
            } else if (controller instanceof AssignmentViewController a) {
                a.initData(currentUser);
            } else if (controller instanceof DashboardContentController d) {
                d.initData(currentUser);
            } else if (controller instanceof DriverViewController drv) {
                drv.initData(currentUser);
            }

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);

            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(180), view);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

        } catch (IOException e) {
            showError("Gagal memuat tampilan: " + fxmlFile, e);
        }
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Terjadi Kesalahan");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
