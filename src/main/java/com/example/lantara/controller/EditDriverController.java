package com.example.lantara.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.example.lantara.model.DatabaseHelper;
import com.example.lantara.model.Driver;

public class EditDriverController {

    @FXML private TextField nikField;
    @FXML private TextField namaField;
    @FXML private TextField simField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Driver currentDriver;

    public void setDriver(Driver driver) {
        this.currentDriver = driver;
        nikField.setText(driver.getNomorIndukKaryawan());
        namaField.setText(driver.getNama());
        simField.setText(driver.getNomorSIM());
    }

    @FXML
    private void handleSaveButton() {
        String nama = namaField.getText().trim();
        String sim = simField.getText().trim();

        if (nama.isEmpty() || sim.isEmpty()) {
            errorLabel.setText("Nama dan Nomor SIM tidak boleh kosong.");
            return;
        }

        DatabaseHelper.updateDriver(currentDriver.getNomorIndukKaryawan(), nama, sim);
        closeWindow();
    }

    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nikField.getScene().getWindow();
        stage.close();
    }
}