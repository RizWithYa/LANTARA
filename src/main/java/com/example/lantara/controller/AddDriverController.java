package com.example.lantara.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.example.lantara.model.DatabaseHelper;
import com.example.lantara.model.Driver;

public class AddDriverController {

    @FXML private TextField nikField;
    @FXML private TextField namaField;
    @FXML private TextField simField;
    @FXML private Label errorLabel;

    private DriverViewController parent; 

    public void initData(DriverViewController parent) { this.parent = parent; }

    @FXML
    private void handleSaveButton() {
        errorLabel.setText("");

        String nik  = nikField.getText()  == null ? "" : nikField.getText().trim();
        String nama = namaField.getText() == null ? "" : namaField.getText().trim();
        String sim  = simField.getText()  == null ? "" : simField.getText().trim();

        if (nik.isEmpty() || nama.isEmpty() || sim.isEmpty()) {
            errorLabel.setText("Semua field harus diisi.");
            return;
        }

        if (!nik.matches("[A-Za-z0-9]+")) {
            errorLabel.setText("Format NIK hanya huruf/angka.");
            return;
        }
        if (!sim.matches("[A-Za-z0-9]+")) {
            errorLabel.setText("Format No. SIM hanya huruf/angka.");
            return;
        }

        Driver d = new Driver(nik, nama, sim, "Tersedia");
        boolean ok = DatabaseHelper.addDriver(d);
        if (!ok) {
            errorLabel.setText("Gagal menyimpan. NIK/No. SIM mungkin sudah ada.");
            return;
        }

        if (parent != null) parent.loadDriverData();

        new Alert(Alert.AlertType.INFORMATION, "Pengemudi berhasil ditambahkan.").showAndWait();
        closeWindow();
    }

    @FXML
    private void handleCancelButton() { closeWindow(); }

    private void closeWindow() {
        Stage st = (Stage) nikField.getScene().getWindow();
        st.close();
    }
}
