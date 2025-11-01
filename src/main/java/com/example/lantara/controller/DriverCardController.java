package com.example.lantara.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.example.lantara.MainApp;
import com.example.lantara.model.Assignment;
import com.example.lantara.model.Driver;
import com.example.lantara.model.Vehicle;

public class DriverCardController {

    @FXML private ImageView avatarImageView;
    @FXML private Label namaLabel;
    @FXML private Label nikLabel;
    @FXML private Label simLabel;

    @FXML private Label statusLabel;
    @FXML private VBox  assignmentInfoBox;
    @FXML private Label vehicleInfoLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Driver currentDriver;
    private DriverViewController parent;

    public void setDriver(Driver driver) {
        this.currentDriver = driver;

        namaLabel.setText(driver.getNama());
        nikLabel.setText("NIK: " + driver.getNomorIndukKaryawan());
        simLabel.setText("SIM: " + driver.getNomorSIM());

        Assignment aktif = MainApp.allAssignments.stream()
                .filter(a -> a.getDriver() != null
                        && a.getDriver().getNomorIndukKaryawan().equals(driver.getNomorIndukKaryawan())
                        && "Berlangsung".equalsIgnoreCase(a.getStatusTugas()))
                .findFirst()
                .orElse(null);

        if (aktif != null) {
            statusLabel.setText("Ditugaskan");
            statusLabel.getStyleClass().removeAll("driver-status-tersedia");
            statusLabel.getStyleClass().add("driver-status-bertugas");

            Vehicle v = aktif.getVehicle();
            vehicleInfoLabel.setText(v == null ? "N/A" : v.getMerek() + " (" + v.getNomorPolisi() + ")");
            assignmentInfoBox.setVisible(true);
            assignmentInfoBox.setManaged(true);

        } else {
            statusLabel.setText("Tersedia");
            statusLabel.getStyleClass().removeAll("driver-status-bertugas");
            statusLabel.getStyleClass().add("driver-status-tersedia");

            assignmentInfoBox.setVisible(false);
            assignmentInfoBox.setManaged(false);
        }
    }

    public void setParentController(DriverViewController parent) { this.parent = parent; }

    public void setReadOnly(boolean readOnly) {
        editButton.setVisible(!readOnly);
        editButton.setManaged(!readOnly);
        deleteButton.setVisible(!readOnly);
        deleteButton.setManaged(!readOnly);
    }

    @FXML private void handleEditAction()  { if (parent != null) parent.openEditForm(currentDriver); }
    @FXML private void handleDeleteAction(){ if (parent != null) parent.deleteDriver(currentDriver); }
}
