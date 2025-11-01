package com.example.lantara.controller;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.example.lantara.MainApp;
import com.example.lantara.model.Assignment;
import com.example.lantara.model.Driver;
import com.example.lantara.model.User;
import com.example.lantara.model.Vehicle;
import com.example.lantara.model.DatabaseHelper;

public class AssignmentViewController {

    @FXML private GridPane formPenugasan;
    @FXML private ChoiceBox<Vehicle> vehicleChoiceBox;
    @FXML private ChoiceBox<Driver>  driverChoiceBox;
    @FXML private TextField tujuanField;

    @FXML private TableView<Assignment> assignmentTable;
    @FXML private TableColumn<Assignment, String> colKode;
    @FXML private TableColumn<Assignment, String> colKendaraan;
    @FXML private TableColumn<Assignment, String> colPengemudi;
    @FXML private TableColumn<Assignment, String> colTujuan;
    @FXML private TableColumn<Assignment, String> colTglPinjam;
    @FXML private TableColumn<Assignment, String> colStatus;
    @FXML private TableColumn<Assignment, Void>   colAksi;

    private User currentUser;

    private final ObservableList<Vehicle> availableVehicles = FXCollections.observableArrayList();
    private final ObservableList<Driver>  availableDrivers  = FXCollections.observableArrayList();

    private static final String VEHICLE_DATA_FILE    = "vehicles.csv";
    private static final String ASSIGNMENT_DATA_FILE = "assignments.csv";

    public void initData(User user) {
        this.currentUser = user;
        setupVisibility();
        loadInitialData();
        refreshTableForUser();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionColumn();
        assignmentTable.setItems(MainApp.allAssignments);

        loadAvailableVehicles();
        loadAvailableDrivers();
    }

    private void setupVisibility() {
        boolean isManager = currentUser != null && "MANAJER".equalsIgnoreCase(currentUser.getRole());
        formPenugasan.setVisible(isManager);
        formPenugasan.setManaged(isManager);
        colAksi.setVisible(isManager);
    }

    private void setupTableColumns() {
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodePenugasan"));
        colTujuan.setCellValueFactory(new PropertyValueFactory<>("tujuan"));
        colTglPinjam.setCellValueFactory(a -> a.getValue().tanggalPenugasanFormattedProperty());
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusTugas"));

        colKendaraan.setCellValueFactory(cd -> {
            Vehicle v = cd.getValue().getVehicle();
            return new javafx.beans.property.SimpleStringProperty(v != null ? v.getNomorPolisi() : "-");
        });

        colPengemudi.setCellValueFactory(cd -> {
            Driver d = cd.getValue().getDriver();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getNama() : "-");
        });
    }

    private void setupActionColumn() {
        colAksi.setCellFactory(param -> new TableCell<>() {
            private final Button btnSelesai = new Button("Selesaikan");

            {
                btnSelesai.setStyle("-fx-background-color:#1E90FF; -fx-text-fill:white; -fx-background-radius:6;");
                btnSelesai.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
                btnSelesai.setOnAction(e -> {
                    Assignment a = getTableView().getItems().get(getIndex());
                    handleSelesaikanTugas(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Assignment a = getTableView().getItems().get(getIndex());
                if ("Selesai".equalsIgnoreCase(a.getStatusTugas())) {
                    Label done = new Label("✔ Selesai");
                    done.setTextFill(Color.web("#00A86B"));
                    setGraphic(done);
                } else {
                    btnSelesai.setDisable(!"Berlangsung".equalsIgnoreCase(a.getStatusTugas()));
                    setGraphic(new HBox(btnSelesai));
                }
            }
        });
    }

    private void loadInitialData() {
        loadAvailableVehicles();
        loadAvailableDrivers();
    }

    private void loadAvailableVehicles() {
        availableVehicles.clear();
        for (Vehicle v : MainApp.allVehicles) {
            if ("Tersedia".equalsIgnoreCase(v.getStatus())) {
                availableVehicles.add(v);
            }
        }
        vehicleChoiceBox.setItems(availableVehicles);
    }

    private void loadAvailableDrivers() {
        availableDrivers.setAll(MainApp.allDrivers);
        driverChoiceBox.setItems(availableDrivers);
    }

    @FXML
    private void handleAjukanButton() {
        Vehicle selectedVehicle = vehicleChoiceBox.getValue();
        Driver  selectedDriver  = driverChoiceBox.getValue();
        String  tujuan          = tujuanField.getText().trim();

        if (selectedVehicle == null || selectedDriver == null || tujuan.isEmpty()) {
            showAlert(AlertType.ERROR, "Input Tidak Lengkap",
                    "Harap pilih kendaraan, pengemudi, dan isi tujuan.");
            return;
        }

        String kode = "PJ" + String.format("%03d", MainApp.allAssignments.size() + 1);
        Assignment newAssignment = new Assignment(kode, selectedVehicle, selectedDriver, tujuan);

        MainApp.allAssignments.add(newAssignment);

        selectedVehicle.updateStatus("Digunakan");
        updateVehicleStatusInFile(selectedVehicle.getNomorPolisi(), "Digunakan");
        setDriverStatus(selectedDriver.getNomorIndukKaryawan(), "Ditugaskan");
        saveAssignmentsToFile();
        refreshTableForUser();
        loadAvailableVehicles(); 
        tujuanField.clear();
        vehicleChoiceBox.getSelectionModel().clearSelection();
        driverChoiceBox.getSelectionModel().clearSelection();

        showAlert(AlertType.INFORMATION, "Sukses",
                "Penugasan " + kode + " berhasil diajukan dan disimpan.");
    }

    private void handleSelesaikanTugas(Assignment assignment) {
        if (assignment == null || !"Berlangsung".equalsIgnoreCase(assignment.getStatusTugas())) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Penyelesaian");
        alert.setHeaderText("Selesaikan tugas " + assignment.getKodePenugasan() + "?");
        alert.setContentText("Kendaraan akan dikembalikan ke status 'Tersedia' dan driver menjadi 'Tersedia'.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        assignment.completeAssignment();

        Vehicle v = assignment.getVehicle();
        if (v != null) {
            v.updateStatus("Tersedia");
            updateVehicleStatusInFile(v.getNomorPolisi(), "Tersedia");
        }

        Driver d = assignment.getDriver();
        if (d != null) {
            setDriverStatus(d.getNomorIndukKaryawan(), "Tersedia");
        }

        saveAssignmentsToFile();
        refreshTableForUser();
        loadAvailableVehicles();

        showAlert(AlertType.INFORMATION, "Berhasil", "Tugas berhasil diselesaikan.");
    }

    private void saveAssignmentsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ASSIGNMENT_DATA_FILE))) {
            for (Assignment a : MainApp.allAssignments) {
                String tglKembali = (a.getTanggalKembali() == null) ? "null" : a.getTanggalKembali().toString();
                String line = String.join(",",
                        a.getKodePenugasan(),
                        a.getVehicle().getNomorPolisi(),
                        a.getDriver().getNomorIndukKaryawan(),
                        a.getTujuan(),
                        a.getTanggalPenugasan().toString(),
                        tglKembali,
                        a.getStatusTugas()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Gagal Menyimpan", "Tidak bisa menulis berkas assignments.csv");
        }
    }

    private void updateVehicleStatusInFile(String nopolToUpdate, String newStatus) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(VEHICLE_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].equals(nopolToUpdate)) {
                    if (data.length > 4) data[4] = newStatus;
                    line = String.join(",", data);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VEHICLE_DATA_FILE))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDriverStatus(String nik, String statusBaru) {
        DatabaseHelper.updateDriverStatus(nik, statusBaru);
        for (Driver drv : MainApp.allDrivers) {
            if (drv.getNomorIndukKaryawan().equals(nik)) {
                drv.setStatus(statusBaru);
                break;
            }
        }
    }

private void refreshTableForUser() {
    if (currentUser == null) return;

    if ("MANAJER".equalsIgnoreCase(currentUser.getRole())) {
        assignmentTable.setItems(MainApp.allAssignments);

    } else if ("STAF".equalsIgnoreCase(currentUser.getRole())) {
        List<Assignment> myAssignments = MainApp.allAssignments.stream()
            .filter(a -> a.getDriver() != null &&
                        a.getDriver().getNama().equalsIgnoreCase(currentUser.getUsername())
                        || a.getDriver().getNomorIndukKaryawan().equalsIgnoreCase(currentUser.getUsername()))
            .collect(Collectors.toList());

        assignmentTable.setItems(FXCollections.observableArrayList(myAssignments));
    }

    assignmentTable.refresh();
}

    private void showAlert(AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
