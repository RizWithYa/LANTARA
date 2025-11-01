package com.example.lantara.model;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Optional;
import com.example.lantara.MainApp;

public class DatabaseHelper {

    private static final String URL = "jdbc:sqlite:lantara.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String createUserTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            );
        """;

        String createDriverTableSql = """
            CREATE TABLE IF NOT EXISTS drivers (
                nik TEXT PRIMARY KEY,
                nama TEXT NOT NULL,
                no_sim TEXT NOT NULL UNIQUE,
                status TEXT DEFAULT 'Tersedia'
            );
        """;

        String createAssignmentTableSql = """
            CREATE TABLE IF NOT EXISTS assignments (
                id TEXT PRIMARY KEY,
                driver_nik TEXT NOT NULL,
                vehicle_nopol TEXT NOT NULL,
                tujuan TEXT,
                tanggal_pinjam TEXT,
                tanggal_kembali TEXT,
                status TEXT NOT NULL
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTableSql);
            stmt.execute(createDriverTableSql);
            stmt.execute(createAssignmentTableSql);

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO users(username,password,role) VALUES('manajer','manajer123','MANAJER')");
                    stmt.execute("INSERT INTO users(username,password,role) VALUES('staf','staf123','STAF')");
                }
            }

            ensureDriverStatusColumn(conn);
        } catch (SQLException e) {
            if (!String.valueOf(e.getMessage()).contains("UNIQUE")) {
                System.err.println("Error inisialisasi DB: " + e.getMessage());
            }
        }
    }

    private static void ensureDriverStatusColumn(Connection conn) {
        try (Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("PRAGMA table_info(drivers)")) {
            boolean hasStatus = false;
            while (rs.next()) {
                if ("status".equalsIgnoreCase(rs.getString("name"))) {
                    hasStatus = true; break;
                }
            }
            if (!hasStatus) {
                try (Statement alter = conn.createStatement()) {
                    alter.execute("ALTER TABLE drivers ADD COLUMN status TEXT DEFAULT 'Tersedia'");
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal cek kolom status: " + e.getMessage());
        }
    }

    public static User validateUser(String username, String password, String role) {
        String sql = "SELECT username,password,role FROM users WHERE username=? AND password=? AND UPPER(role)=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("username"),
                                    rs.getString("password"),
                                    rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validasi user: " + e.getMessage());
        }
        return null;
    }

    public static boolean addDriver(Driver driver) {
        String sql = "INSERT INTO drivers(nik, nama, no_sim, status) VALUES(?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driver.getNomorIndukKaryawan());
            ps.setString(2, driver.getNama());
            ps.setString(3, driver.getNomorSIM());
            ps.setString(4, driver.getStatus());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal menambah pengemudi: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateDriver(String nik, String nama, String noSim) {
        String sql = "UPDATE drivers SET nama=?, no_sim=? WHERE nik=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setString(2, noSim);
            ps.setString(3, nik);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update driver: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateDriverStatus(String nik, String newStatus) {
        String sql = "UPDATE drivers SET status=? WHERE nik=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, nik);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal ubah status driver: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteDriver(String nik) {
        String sql = "DELETE FROM drivers WHERE nik=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nik);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal hapus driver: " + e.getMessage());
            return false;
        }
    }

    public static ObservableList<Driver> getAllDrivers() {
        String sql = "SELECT nik, nama, no_sim, status FROM drivers ORDER BY nama ASC";
        ObservableList<Driver> list = FXCollections.observableArrayList();
        try (Connection conn = connect(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Driver(
                        rs.getString("nik"),
                        rs.getString("nama"),
                        rs.getString("no_sim"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal ambil data driver: " + e.getMessage());
        }
        return list;
    }

    public static Assignment getActiveAssignmentByDriver(String driverNIK) {
        String sql = """
            SELECT id, driver_nik, vehicle_nopol, tujuan, tanggal_pinjam, tanggal_kembali, status
            FROM assignments
            WHERE driver_nik = ? AND status = 'Berlangsung'
            LIMIT 1;
        """;
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driverNIK);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nopol = rs.getString("vehicle_nopol");
                Optional<Vehicle> vOpt = MainApp.allVehicles.stream()
                        .filter(v -> v.getNomorPolisi().equalsIgnoreCase(nopol))
                        .findFirst();
                Optional<Driver> dOpt = MainApp.allDrivers.stream()
                        .filter(d -> d.getNomorIndukKaryawan().equalsIgnoreCase(driverNIK))
                        .findFirst();
                if (vOpt.isPresent() && dOpt.isPresent()) {
                    return new Assignment(
                            rs.getString("id"),
                            vOpt.get(),
                            dOpt.get(),
                            rs.getString("tujuan"),
                            rs.getString("tanggal_pinjam"),
                            rs.getString("tanggal_kembali"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal ambil assignment aktif: " + e.getMessage());
        }
        return null;
    }
}
