package com.example.lantara.model;

public class Vehicle {
    private String nomorPolisi;
    private String merek;
    private String model;
    private int tahun;
    private String status;

    public Vehicle(String nomorPolisi, String merek, String model, int tahun) {
        this.nomorPolisi = nomorPolisi;
        this.merek = merek;
        this.model = model;
        this.tahun = tahun;
        this.status = "Tersedia";
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    public String getNomorPolisi() { return nomorPolisi; }
    public String getStatus() { return status; }
    public String getMerek() { return merek; }
    public String getModel() { return model; }
    public int getTahun() { return tahun; }

    public void setNomorPolisi(String nomorPolisi) { this.nomorPolisi = nomorPolisi; }
    public void setMerek(String merek) { this.merek = merek; }
    public void setModel(String model) { this.model = model; }
    public void setTahun(int tahun) { this.tahun = tahun; }

    @Override
    public String toString() {
        return nomorPolisi + " (" + merek + " " + model + ")";
    }
}