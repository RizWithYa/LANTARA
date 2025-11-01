package com.example.lantara.model;

public class Driver {

    private String nomorIndukKaryawan;
    private String nama;
    private String nomorSIM;
    private String status;

    public Driver(String nomorIndukKaryawan, String nama, String nomorSIM) {
        this(nomorIndukKaryawan, nama, nomorSIM, "Tersedia");
    }

    public Driver(String nomorIndukKaryawan, String nama, String nomorSIM, String status) {
        this.nomorIndukKaryawan = nomorIndukKaryawan;
        this.nama = nama;
        this.nomorSIM = nomorSIM;
        this.status = (status == null || status.isEmpty()) ? "Tersedia" : status;
    }

    public String getNomorIndukKaryawan() { return nomorIndukKaryawan; }
    public void setNomorIndukKaryawan(String nomorIndukKaryawan) { this.nomorIndukKaryawan = nomorIndukKaryawan; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getNomorSIM() { return nomorSIM; }
    public void setNomorSIM(String nomorSIM) { this.nomorSIM = nomorSIM; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return nama + " (" + nomorIndukKaryawan + ")";
    }
}
