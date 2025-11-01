package com.example.lantara.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.example.lantara.MainApp;
import com.example.lantara.model.Assignment;
import com.example.lantara.model.Driver;
import com.example.lantara.model.User;
import com.example.lantara.model.Vehicle;

public class DashboardContentController {

    @FXML private Label totalVehicleLabel;
    @FXML private PieChart vehicleStatusChart;
    @FXML private VBox notificationBox;
    @FXML private VBox assignmentBox;
    @FXML private TableView<Assignment> historyTable;
    @FXML private TableColumn<Assignment, String> colHistoryTanggal;
    @FXML private TableColumn<Assignment, String> colHistoryKendaraan;
    @FXML private TableColumn<Assignment, String> colHistoryPengemudi;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        refreshAll();

        MainApp.allVehicles.addListener((ListChangeListener<Vehicle>) c -> refreshAll());
        MainApp.allAssignments.addListener((ListChangeListener<Assignment>) c -> refreshAll());
    }

    @FXML
    public void initialize() {
        colHistoryTanggal.setCellValueFactory(a -> a.getValue().tanggalPenugasanFormattedProperty());
        colHistoryKendaraan.setCellValueFactory(a -> {
            Vehicle v = a.getValue().getVehicle();
            String txt = (v == null) ? "N/A" : v.getMerek() + " (" + v.getNomorPolisi() + ")";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        colHistoryPengemudi.setCellValueFactory(a -> {
            Driver d = a.getValue().getDriver();
            return new javafx.beans.property.SimpleStringProperty(d == null ? "N/A" : d.getNama());
        });

        if (notificationBox != null) notificationBox.getChildren().setAll(new Label("Tidak ada notifikasi."));
        if (assignmentBox != null) assignmentBox.getChildren().setAll(new Label("Tidak ada penugasan aktif."));
    }

    private void refreshAll() {
        refreshArmadaPanel();
        refreshAssignmentsAndNotifications();
        refreshHistoryTable();
    }

    private void refreshArmadaPanel() {
        int total = MainApp.allVehicles.size();
        long tersedia = MainApp.allVehicles.stream()
                .filter(v -> "Tersedia".equalsIgnoreCase(v.getStatus()))
                .count();
        long digunakan = Math.max(0, total - tersedia);

        if (totalVehicleLabel != null) {
            totalVehicleLabel.setText(String.valueOf(total));
        }

        if (vehicleStatusChart != null) {
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                    new PieChart.Data("Tersedia (" + tersedia + ")", tersedia),
                    new PieChart.Data("Digunakan (" + digunakan + ")", digunakan)
            );
            vehicleStatusChart.setData(data);
            vehicleStatusChart.setLabelsVisible(false);
            vehicleStatusChart.setLegendVisible(true);
        }
    }

    private void refreshAssignmentsAndNotifications() {
        if (notificationBox == null || assignmentBox == null) return;

        notificationBox.getChildren().clear();
        assignmentBox.getChildren().clear();

        List<Assignment> sumber = MainApp.allAssignments;
        if (currentUser != null && "STAF".equalsIgnoreCase(currentUser.getRole())) {
            String myNikOrUser = currentUser.getUsername();
            sumber = sumber.stream()
                    .filter(a -> a.getDriver() != null &&
                            (myNikOrUser.equals(a.getDriver().getNomorIndukKaryawan())
                                    || myNikOrUser.equalsIgnoreCase(a.getDriver().getNama())))
                    .collect(Collectors.toList());
        }

        List<Assignment> aktif = sumber.stream()
                .filter(a -> "Berlangsung".equalsIgnoreCase(a.getStatusTugas()))
                .collect(Collectors.toList());

        int notifCount = 0;
        LocalDate today = LocalDate.now();

        for (Assignment a : aktif) {
            assignmentBox.getChildren().add(makeAssignmentRow(a));
            Vehicle v = a.getVehicle();
            String nopol = (v == null) ? "N/A" : v.getNomorPolisi();
            String merek = (v == null) ? "Kendaraan" : v.getMerek();

            Label title = new Label("Mobil belum dikembalikan");
            title.setStyle("-fx-font-weight: bold;");
            Label detail = new Label(merek + " (" + nopol + "), dipinjam " + a.tanggalPenugasanFormattedProperty().get());
            notificationBox.getChildren().add(new VBox(2, title, detail));
            notifCount++;

            // (Contoh) jika tanggal kembali lewat hari ini -> tambahkan warning
            if (a.getTanggalKembali() != null && a.getTanggalKembali().isBefore(today)) {
                Label warn = new Label("⚠️ Terlambat mengembalikan sejak " + a.getTanggalKembali());
                warn.setStyle("-fx-text-fill: #c0392b;");
                notificationBox.getChildren().add(warn);
            }
        }

        if (aktif.isEmpty()) {
            assignmentBox.getChildren().add(new Label("Tidak ada penugasan aktif."));
        }
        if (notifCount == 0) {
            notificationBox.getChildren().add(new Label("Tidak ada notifikasi."));
        }
    }

    private void refreshHistoryTable() {
        if (historyTable == null) return;

        List<Assignment> selesai = MainApp.allAssignments.stream()
                .filter(a -> {
                    String s = a.getStatusTugas();
                    return (s != null && (s.equalsIgnoreCase("Selesai") || s.equalsIgnoreCase("Sudah Dikembalikan")))
                            || a.getTanggalKembali() != null;
                })
                .sorted(Comparator.comparing(Assignment::getTanggalPenugasan,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .collect(Collectors.toList());

        historyTable.setItems(FXCollections.observableArrayList(selesai));

        if (selesai.isEmpty()) {
            historyTable.setPlaceholder(new Label("Belum ada riwayat peminjaman yang selesai."));
        }
    }

    private HBox makeAssignmentRow(Assignment a) {
        Vehicle v = a.getVehicle();
        Driver d  = a.getDriver();

        String kendaraan = (v == null) ? "N/A" : v.getMerek() + " (" + v.getNomorPolisi() + ")";
        String pengemudi = (d == null) ? "N/A" : d.getNama();
        String tanggal   = a.tanggalPenugasanFormattedProperty().get();

        Label l = new Label(kendaraan + " → " + pengemudi + "  •  " + tanggal);
        HBox box = new HBox(l);
        box.setSpacing(6);
        return box;
    }
}