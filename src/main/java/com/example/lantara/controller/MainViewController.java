package com.example.lantara.controller;

import java.io.IOException;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.example.lantara.MainApp;
import com.example.lantara.model.PassengerCar;
import com.example.lantara.model.Truck;
import com.example.lantara.model.User;
import com.example.lantara.model.Vehicle;

public class MainViewController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String>  colNomorPolisi;
    @FXML private TableColumn<Vehicle, String>  colMerek;
    @FXML private TableColumn<Vehicle, String>  colModel;
    @FXML private TableColumn<Vehicle, Integer> colTahun;
    @FXML private TableColumn<Vehicle, String>  colStatus;
    @FXML private TableColumn<Vehicle, String>  colJenis;
    @FXML private TableColumn<Vehicle, String>  colKapasitas;
    @FXML private Button addNewVehicleButton;

    private final ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        boolean isManager = user != null && "MANAJER".equals(user.getRole());

        if (addNewVehicleButton != null) {
            addNewVehicleButton.setVisible(isManager);
            addNewVehicleButton.setManaged(isManager);
        }

        vehicleTable.refresh();
    }

    @FXML
    public void initialize() {
        colNomorPolisi.setCellValueFactory(new PropertyValueFactory<>("nomorPolisi"));
        colMerek.setCellValueFactory(new PropertyValueFactory<>("merek"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colTahun.setCellValueFactory(new PropertyValueFactory<>("tahun"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colJenis.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        (cd.getValue() instanceof PassengerCar) ? "Mobil Penumpang" : "Truk"
                )
        );
        colKapasitas.setCellValueFactory(cd -> {
            Vehicle v = cd.getValue();
            String text = (v instanceof PassengerCar)
                    ? ((PassengerCar) v).getKapasitasPenumpang() + " Penumpang"
                    : ((Truck) v).getKapasitasAngkutTon() + " Ton";
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        TableColumn<Vehicle, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Hapus");
            private final HBox box = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("table-action-button");
                deleteBtn.getStyleClass().add("table-action-button");

                editBtn.setOnAction(e -> {
                    Vehicle v = getTableView().getItems().get(getIndex());
                    openEditForm(v);
                });
                deleteBtn.setOnAction(e -> {
                    Vehicle v = getTableView().getItems().get(getIndex());
                    deleteVehicle(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    boolean isManager = (currentUser != null && "MANAJER".equals(currentUser.getRole()));
                    setGraphic(isManager ? box : null);
                }
            }
        });
        if (!vehicleTable.getColumns().contains(aksiCol)) {
            vehicleTable.getColumns().add(aksiCol);
        }

        vehicleTable.setItems(MainApp.allVehicles);
    }

    @FXML
    protected void handleAddNewVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/add-vehicle-view.fxml"));
            Parent root = loader.load();

            AddVehicleController controller = loader.getController();
            controller.setVehicleList(MainApp.allVehicles); 

            Stage stage = new Stage();
            stage.setTitle("Tambah Kendaraan Baru");
            MainApp.setStageIcon(stage);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(vehicleTable.getScene().getWindow());
            stage.showAndWait();

            vehicleTable.refresh();
            MainApp.saveAllData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleRefreshButton() {
        MainApp.loadAllData();
        vehicleTable.refresh();
    }

    public void openEditForm(Vehicle vehicle) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/edit-vehicle-view.fxml"));
            Parent root = loader.load();

            EditVehicleController controller = loader.getController();
            controller.setVehicle(vehicle);

            Stage stage = new Stage();
            stage.setTitle("Edit Kendaraan");
            MainApp.setStageIcon(stage);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(vehicleTable.getScene().getWindow());
            stage.showAndWait();

            MainApp.saveAllData();
            vehicleTable.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Kendaraan: " + vehicle.getNomorPolisi());
        alert.setContentText("Apakah Anda yakin?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            MainApp.allVehicles.remove(vehicle);
            MainApp.saveAllData();
            vehicleTable.refresh();
        }
    }
}
