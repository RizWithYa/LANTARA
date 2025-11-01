package com.example.lantara.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.example.lantara.MainApp;

public class Assignment {
    private final StringProperty kodePenugasan;
    private final ObjectProperty<Vehicle> vehicle;
    private final ObjectProperty<Driver> driver;
    private final StringProperty tujuan;
    private final ObjectProperty<LocalDate> tanggalPenugasan;
    private final ObjectProperty<LocalDate> tanggalKembali;
    private final StringProperty statusTugas;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Assignment(String kodePenugasan, Vehicle vehicle, Driver driver, String tujuan) {
        this.kodePenugasan = new SimpleStringProperty(kodePenugasan);
        this.vehicle = new SimpleObjectProperty<>(vehicle);
        this.driver = new SimpleObjectProperty<>(driver);
        this.tujuan = new SimpleStringProperty(tujuan);
        this.tanggalPenugasan = new SimpleObjectProperty<>(LocalDate.now());
        this.tanggalKembali = new SimpleObjectProperty<>(null);
        this.statusTugas = new SimpleStringProperty("Berlangsung");

        if (vehicle != null) {
            vehicle.updateStatus("Digunakan");
        }
    }

    public Assignment(String kodePenugasan, Vehicle vehicle, Driver driver,
                        String tujuan, String tglPinjam, String tglKembali, String status) {
        this.kodePenugasan = new SimpleStringProperty(kodePenugasan);
        this.vehicle = new SimpleObjectProperty<>(vehicle);
        this.driver = new SimpleObjectProperty<>(driver);
        this.tujuan = new SimpleStringProperty(tujuan);

        LocalDate tanggalPinjamParsed = null;
        LocalDate tanggalKembaliParsed = null;

        try {
            if (tglPinjam != null && !tglPinjam.isBlank() && !"null".equalsIgnoreCase(tglPinjam)) {
                tanggalPinjamParsed = LocalDate.parse(tglPinjam, DATE_FORMATTER);
            }
            if (tglKembali != null && !tglKembali.isBlank() && !"null".equalsIgnoreCase(tglKembali)) {
                tanggalKembaliParsed = LocalDate.parse(tglKembali, DATE_FORMATTER);
            }
        } catch (Exception e) {
            System.err.println("Gagal parse tanggal: " + e.getMessage());
        }

        this.tanggalPenugasan = new SimpleObjectProperty<>(tanggalPinjamParsed);
        this.tanggalKembali = new SimpleObjectProperty<>(tanggalKembaliParsed);
        this.statusTugas = new SimpleStringProperty(status != null ? status : "Berlangsung");

        if (vehicle != null && "Digunakan".equalsIgnoreCase(status)) {
            vehicle.updateStatus("Digunakan");
        } else if (vehicle != null && "Selesai".equalsIgnoreCase(status)) {
            vehicle.updateStatus("Tersedia");
        }
    }

    public Assignment(String kode, String nopol, String nik, String tujuan, String tglPinjam, String tglKembali, String status) {
        this(kode, findVehicle(nopol), findDriver(nik), tujuan, tglPinjam, tglKembali, status);
    }

    private static Vehicle findVehicle(String nopol) {
        return MainApp.allVehicles.stream()
                .filter(v -> v.getNomorPolisi().equals(nopol))
                .findFirst()
                .orElse(null);
    }

    private static Driver findDriver(String nik) {
        return MainApp.allDrivers.stream()
                .filter(d -> d.getNomorIndukKaryawan().equals(nik))
                .findFirst()
                .orElse(null);
    }

    public StringProperty kodePenugasanProperty() { return kodePenugasan; }
    public StringProperty tujuanProperty() { return tujuan; }
    public StringProperty statusTugasProperty() { return statusTugas; }

    public StringProperty tanggalPenugasanFormattedProperty() {
        if (tanggalPenugasan.get() == null) {
            return new SimpleStringProperty("-");
        }
        return new SimpleStringProperty(tanggalPenugasan.get().format(DATE_FORMATTER));
    }

    public Vehicle getVehicle() { return vehicle.get(); }
    public Driver getDriver() { return driver.get(); }
    public String getStatusTugas() { return statusTugas.get(); }
    public String getTujuan() { return tujuan.get(); }
    public LocalDate getTanggalPenugasan() { return tanggalPenugasan.get(); }
    public LocalDate getTanggalKembali() { return tanggalKembali.get(); }
    public String getKodePenugasan() { return kodePenugasan.get();
}


    public void completeAssignment() {
        this.statusTugas.set("Selesai");
        this.tanggalKembali.set(LocalDate.now());
        if (vehicle.get() != null) {
            vehicle.get().updateStatus("Tersedia");
        }
    }
}
