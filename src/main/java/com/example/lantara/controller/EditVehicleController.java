package com.example.lantara.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import com.example.lantara.model.Vehicle;

public class EditVehicleController {

    @FXML private TextField nopolField;
    @FXML private TextField merekField;
    @FXML private TextField modelField;
    @FXML private TextField tahunField;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private Label errorLabel;

    private Vehicle vehicleToEdit;

    @FXML
    public void initialize() {
        statusChoiceBox.getItems().addAll("Tersedia", "Digunakan");
        setupArrowKeyNavigation();
        setupEnterKeyNavigation();
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicleToEdit = vehicle;
        nopolField.setText(vehicle.getNomorPolisi());
        merekField.setText(vehicle.getMerek());
        modelField.setText(vehicle.getModel());
        tahunField.setText(String.valueOf(vehicle.getTahun()));
        statusChoiceBox.setValue(vehicle.getStatus());
    }
    
    private void setupEnterKeyNavigation() {
        nopolField.setOnAction(event -> merekField.requestFocus());
        merekField.setOnAction(event -> modelField.requestFocus());
        modelField.setOnAction(event -> tahunField.requestFocus());
        tahunField.setOnAction(event -> statusChoiceBox.requestFocus());
    }

    private void setupArrowKeyNavigation() {
        nopolField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) merekField.requestFocus();
        });

        merekField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) modelField.requestFocus();
            else if (event.getCode() == KeyCode.UP) nopolField.requestFocus();
        });

        modelField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) tahunField.requestFocus();
            else if (event.getCode() == KeyCode.UP) merekField.requestFocus();
        });

        tahunField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) statusChoiceBox.requestFocus();
            else if (event.getCode() == KeyCode.UP) modelField.requestFocus();
        });

        statusChoiceBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) statusChoiceBox.show();
            else if (event.getCode() == KeyCode.UP) tahunField.requestFocus();
        });
    }

    @FXML
    private void handleSaveButton() {
        if (nopolField.getText().isEmpty() || merekField.getText().isEmpty()) {
            errorLabel.setText("Nomor Polisi dan Merek tidak boleh kosong!");
            return;
        }

        try {
            vehicleToEdit.setNomorPolisi(nopolField.getText());
            vehicleToEdit.setMerek(merekField.getText());
            vehicleToEdit.setModel(modelField.getText());
            vehicleToEdit.setTahun(Integer.parseInt(tahunField.getText()));
            vehicleToEdit.updateStatus(statusChoiceBox.getValue());
            
            closeWindow();
        } catch (NumberFormatException e) {
            errorLabel.setText("Tahun harus berupa angka!");
        }
    }

    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nopolField.getScene().getWindow();
        stage.close();
    }
}