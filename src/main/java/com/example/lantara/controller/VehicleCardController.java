package com.example.lantara.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.example.lantara.model.PassengerCar;
import com.example.lantara.model.Truck;
import com.example.lantara.model.Vehicle;

public class VehicleCardController {
    @FXML private Label nopolLabel, merekModelLabel, statusLabel, kapasitasLabel;
    @FXML private Button editButton, deleteButton;
    private Vehicle currentVehicle;
    private MainViewController mainViewController;

    public void setData(Vehicle vehicle, MainViewController mainController) {
        this.currentVehicle = vehicle;
        this.mainViewController = mainController;

        nopolLabel.setText(vehicle.getNomorPolisi());
        merekModelLabel.setText(vehicle.getMerek() + " " + vehicle.getModel() + " (" + vehicle.getTahun() + ")");
        statusLabel.setText(vehicle.getStatus());

        statusLabel.getStyleClass().removeAll("card-status-tersedia", "card-status-digunakan");
        if ("Tersedia".equals(vehicle.getStatus())) {
            statusLabel.getStyleClass().add("card-status-tersedia");
        } else {
            statusLabel.getStyleClass().add("card-status-digunakan");
        }

        if (vehicle instanceof PassengerCar) {
            kapasitasLabel.setText(((PassengerCar) vehicle).getKapasitasPenumpang() + " Penumpang");
        } else if (vehicle instanceof Truck) {
            kapasitasLabel.setText(((Truck) vehicle).getKapasitasAngkutTon() + " Ton");
        }
    }

    @FXML
    private void handleEditAction() {
        mainViewController.openEditForm(currentVehicle);
    }

    @FXML
    private void handleDeleteAction() {
        mainViewController.deleteVehicle(currentVehicle);
    }
}