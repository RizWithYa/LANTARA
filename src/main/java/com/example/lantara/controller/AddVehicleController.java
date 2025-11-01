package com.example.lantara.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.example.lantara.MainApp;
import com.example.lantara.model.PassengerCar;
import com.example.lantara.model.Truck;
import com.example.lantara.model.Vehicle;

import java.util.Optional;

public class AddVehicleController {

    @FXML private TextField nopolField;
    @FXML private TextField merekField;
    @FXML private TextField modelField;
    @FXML private TextField tahunField;
    @FXML private ChoiceBox<String> jenisChoiceBox;
    @FXML private VBox kapasitasPenumpangBox;
    @FXML private TextField penumpangField;
    @FXML private VBox kapasitasAngkutBox;
    @FXML private TextField angkutField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ObservableList<Vehicle> vehicleList;

    @FXML
    public void initialize() {
        jenisChoiceBox.getItems().addAll("Mobil Penumpang", "Truk");

        jenisChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            boolean isPassenger = "Mobil Penumpang".equals(nv);
            boolean isTruck     = "Truk".equals(nv);

            kapasitasPenumpangBox.setVisible(isPassenger);
            kapasitasPenumpangBox.setManaged(isPassenger);

            kapasitasAngkutBox.setVisible(isTruck);
            kapasitasAngkutBox.setManaged(isTruck);

            Stage st = (Stage) jenisChoiceBox.getScene().getWindow();
            if (st != null) st.sizeToScene();
        });

        setupArrowKeyNavigation();

        penumpangField.setOnAction(e -> saveButton.fire());
        angkutField.setOnAction(e -> saveButton.fire());

        if (saveButton != null)   saveButton.setDefaultButton(true);
        if (cancelButton != null) cancelButton.setCancelButton(true);
    }

    public void setVehicleList(ObservableList<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @FXML
    private void handleSaveButton() {
        errorLabel.setText("");

        Vehicle newVehicle = buildVehicleFromForm();
        if (newVehicle == null) return;

        String nopol = newVehicle.getNomorPolisi();
        boolean exists = MainApp.allVehicles.stream()
                .anyMatch(v -> v.getNomorPolisi().equalsIgnoreCase(nopol));
        if (exists) {
            showError("Nomor Polisi sudah digunakan. Gunakan nomor lain.");
            return;
        }

        Stage formStage  = (Stage) saveButton.getScene().getWindow();
        Stage ownerStage = (Stage) formStage.getOwner();
        formStage.hide();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Simpan");
        confirm.setHeaderText("Simpan Kendaraan Baru");
        confirm.setContentText("Apakah Anda yakin ingin menyimpan data kendaraan ini?");
        if (ownerStage != null) {
            confirm.initOwner(ownerStage);
            confirm.initModality(Modality.WINDOW_MODAL);
        }

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            formStage.show();
            return;
        }

        if (vehicleList != null) {
            vehicleList.add(newVehicle);
        } else {
            MainApp.allVehicles.add(newVehicle);
        }
        MainApp.saveAllData();
        formStage.close();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Sukses");
        info.setHeaderText(null);
        info.setContentText("Kendaraan berhasil disimpan.");
        if (ownerStage != null) {
            info.initOwner(ownerStage);
            info.initModality(Modality.WINDOW_MODAL);
        }
        info.showAndWait();
    }

    @FXML
    private void handleCancelButton() {
        ((Stage) nopolField.getScene().getWindow()).close();
    }

    private Vehicle buildVehicleFromForm() {
        String nopol = trim(nopolField.getText());
        String merek = trim(merekField.getText());
        String model = trim(modelField.getText());
        String jenis = jenisChoiceBox.getValue();
        String tahunStr = trim(tahunField.getText());

        if (nopol.isEmpty() || merek.isEmpty() || tahunStr.isEmpty() || jenis == null) {
            showError("Nomor Polisi, Merek, Tahun, dan Jenis harus diisi!");
            return null;
        }

        int tahun;
        try {
            tahun = Integer.parseInt(tahunStr);
            if (tahun < 1950 || tahun > 2100) {
                showError("Tahun kendaraan tidak wajar (1950–2100).");
                return null;
            }
        } catch (NumberFormatException ex) {
            showError("Tahun harus berupa angka yang valid!");
            return null;
        }

        Vehicle v;
        try {
            if ("Mobil Penumpang".equals(jenis)) {
                String kapStr = trim(penumpangField.getText());
                if (kapStr.isEmpty()) {
                    showError("Isi kapasitas penumpang.");
                    return null;
                }
                int penumpang = Integer.parseInt(kapStr);
                if (penumpang <= 0) {
                    showError("Kapasitas penumpang harus > 0.");
                    return null;
                }
                v = new PassengerCar(nopol, merek, model, tahun, penumpang);
            } else { 
                String kapStr = trim(angkutField.getText());
                if (kapStr.isEmpty()) {
                    showError("Isi kapasitas angkut (ton).");
                    return null;
                }
                double ton = Double.parseDouble(kapStr);
                if (ton <= 0) {
                    showError("Kapasitas angkut harus > 0.");
                    return null;
                }
                v = new Truck(nopol, merek, model, tahun, ton);
            }
        } catch (NumberFormatException ex) {
            showError("Kapasitas harus berupa angka yang valid.");
            return null;
        }

        v.updateStatus("Tersedia");
        return v;
    }

    private void showError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    private void setupArrowKeyNavigation() {
        nopolField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.DOWN) merekField.requestFocus(); });

        merekField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) modelField.requestFocus();
            else if (e.getCode() == KeyCode.UP) nopolField.requestFocus();
        });

        modelField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) tahunField.requestFocus();
            else if (e.getCode() == KeyCode.UP) merekField.requestFocus();
        });

        tahunField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) jenisChoiceBox.requestFocus();
            else if (e.getCode() == KeyCode.UP) modelField.requestFocus();
        });

        jenisChoiceBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                jenisChoiceBox.show();
            } else if (e.getCode() == KeyCode.DOWN) {
                if (kapasitasPenumpangBox.isVisible()) penumpangField.requestFocus();
                else if (kapasitasAngkutBox.isVisible()) angkutField.requestFocus();
            } else if (e.getCode() == KeyCode.UP) {
                tahunField.requestFocus();
            }
        });

        penumpangField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.UP) jenisChoiceBox.requestFocus(); });
        angkutField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.UP) jenisChoiceBox.requestFocus(); });
    }
}
