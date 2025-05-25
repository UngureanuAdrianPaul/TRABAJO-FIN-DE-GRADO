package com.cesur.trabajofingrado.jefe.mesas;

import com.google.gson.annotations.SerializedName;

public class MesaInfo {

    @SerializedName("numero")
    private int numero;

    @SerializedName("capacidad") // Corresponderá a 'max_comensales' de la tabla 'mesa'
    private int capacidad;

    // Constructor vacío para Gson (opcional pero buena práctica)
    public MesaInfo() {}

    // Constructor con parámetros (opcional)
    public MesaInfo(int numero, int capacidad) {
        this.numero = numero;
        this.capacidad = capacidad;
    }

    // Getters
    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    // Setters (útiles si necesitas modificar el objeto en Android)
    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
}