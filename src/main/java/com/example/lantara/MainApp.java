package com.example.lantara;

import java.io.*;
import java.nio.file.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.example.lantara.model.*;

public class MainApp extends Application {


    public static ObservableList<Vehicle>    allVehicles    = FXCollections.observableArrayList();
    public static ObservableList<Driver>     allDrivers     = FXCollections.observableArrayList();
    public static ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();

    private static final String VEHICLE_FILE    = "vehicles.csv";
    private static final String ASSIGNMENT_FILE = "assignments.csv";
    private static final String DRIVER_FILE     = "drivers.csv";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/landing-page-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 720);

        stage.setTitle("LANTARA - Lacak Armada Nusantara");
        setStageIcon(stage);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            DatabaseHelper.initializeDatabase();
        } catch (Throwable ignore) {

        }

        loadAllData();

        launch(args);
    }

    public static void loadAllData() {
        loadDriversFromCsv();
        loadVehiclesFromCsv();
        loadAssignmentsFromCsv();
    }

    private static void loadVehiclesFromCsv() {
        allVehicles.clear();
        ensureFileExists(VEHICLE_FILE);

        try (BufferedReader br = new BufferedReader(new FileReader(VEHICLE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] d = line.split(",", -1);
                if (d.length < 7) continue;

                String nopol  = d[0];
                String merek  = d[1];
                String model  = d[2];
                int tahun     = safeParseInt(d[3], 0);
                String jenis  = d[4];
                String status = d[5];

                Vehicle v;
                if ("Mobil Penumpang".equalsIgnoreCase(jenis) || "PASSENGER".equalsIgnoreCase(jenis)) {
                    int kapasitas = safeParseInt(d[6], 0);
                    v = new PassengerCar(nopol, merek, model, tahun, kapasitas);
                } else {
                    double ton = safeParseDouble(d[6], 0.0);
                    v = new Truck(nopol, merek, model, tahun, ton);
                }
                v.updateStatus(status);
                allVehicles.add(v);
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + VEHICLE_FILE + ": " + e.getMessage());
        }
    }

    private static void loadAssignmentsFromCsv() {
        allAssignments.clear();
        ensureFileExists(ASSIGNMENT_FILE);

        try (BufferedReader br = new BufferedReader(new FileReader(ASSIGNMENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] d = line.split(",", -1);
                if (d.length < 7) continue;

                String kode       = d[0];
                String nopol      = d[1];
                String nik        = d[2];
                String tujuan     = d[3];
                String tglPinjam  = d[4];
                String tglKembali = d[5];
                String status     = d[6];

                Vehicle v = allVehicles.stream()
                        .filter(x -> x.getNomorPolisi().equalsIgnoreCase(nopol))
                        .findFirst().orElse(null);
                Driver drv = allDrivers.stream()
                        .filter(x -> x.getNomorIndukKaryawan().equalsIgnoreCase(nik))
                        .findFirst().orElse(null);

                if (v != null && drv != null) {
                    allAssignments.add(new Assignment(kode, v, drv, tujuan, tglPinjam, tglKembali, status));
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + ASSIGNMENT_FILE + ": " + e.getMessage());
        }
    }

    private static void loadDriversFromCsv() {
        allDrivers.clear();
        ensureFileExists(DRIVER_FILE);

        boolean gotFromCsv = false;
        try (BufferedReader br = new BufferedReader(new FileReader(DRIVER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] d = line.split(",", -1);
                if (d.length < 3) continue;

                String nik   = d[0];
                String nama  = d[1];
                String noSim = d[2];
                Driver drv = new Driver(nik, nama, noSim);
                allDrivers.add(drv);
                gotFromCsv = true;
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + DRIVER_FILE + ": " + e.getMessage());
        }

        if (!gotFromCsv) {
            try {
                allDrivers.addAll(DatabaseHelper.getAllDrivers());
            } catch (Throwable ignore) {
            }
        }
    }

    public static void saveAllData() {
        saveVehiclesToCsv();
        saveAssignmentsToCsv();
        saveDriversToCsv();
    }

    public static void saveVehiclesToCsv() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VEHICLE_FILE))) {
            for (Vehicle v : allVehicles) {
                String jenis = (v instanceof PassengerCar) ? "Mobil Penumpang" : "Truk";
                String kapasitas = (v instanceof PassengerCar)
                        ? String.valueOf(((PassengerCar) v).getKapasitasPenumpang())
                        : String.valueOf(((Truck) v).getKapasitasAngkutTon());
                String line = String.join(",",
                        v.getNomorPolisi(),
                        v.getMerek(),
                        v.getModel(),
                        String.valueOf(v.getTahun()),
                        jenis,
                        v.getStatus(),
                        kapasitas
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Gagal menyimpan " + VEHICLE_FILE + ": " + e.getMessage());
        }
    }

    public static void saveAssignmentsToCsv() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ASSIGNMENT_FILE))) {
            for (Assignment a : allAssignments) {
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
            System.err.println("Gagal menyimpan " + ASSIGNMENT_FILE + ": " + e.getMessage());
        }
    }

    public static void saveDriversToCsv() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DRIVER_FILE))) {
            for (Driver d : allDrivers) {
                String line = String.join(",",
                        d.getNomorIndukKaryawan(),
                        d.getNama(),
                        d.getNomorSIM()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Gagal menyimpan " + DRIVER_FILE + ": " + e.getMessage());
        }
    }

    private static void ensureFileExists(String fileName) {
        try {
            Path p = Paths.get(fileName);
            if (!Files.exists(p)) {
                Files.createFile(p);
            }
        } catch (IOException e) {
            System.err.println("Tidak dapat membuat file " + fileName + ": " + e.getMessage());
        }
    }

    private static int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static double safeParseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    public static void setStageIcon(Stage stage) {
        try {
            Image icon = new Image(MainApp.class.getResourceAsStream("/com/example/lantara/assets/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon aplikasi: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        saveAllData();   
        Platform.exit();
    }
}
