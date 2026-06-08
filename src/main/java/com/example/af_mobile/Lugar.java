package com.example.af_mobile;

public class Lugar {

    private String nome;
    private double latitude;
    private double longitude;
    private String tipo;

    public Lugar() {
    }

    public Lugar(String nome, double latitude, double longitude, String tipo) {
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTipo() {
        return tipo;
    }
}