package com.example.lantara.model;

public class Truck extends Vehicle {
    private double kapasitasAngkutTon;

    public Truck(String nomorPolisi, String merek, String model, int tahun, double kapasitasAngkutTon) {
        super(nomorPolisi, merek, model, tahun);
        this.kapasitasAngkutTon = kapasitasAngkutTon;
    }

    public double getKapasitasAngkutTon() {
        return kapasitasAngkutTon;
    }
    
}