package com.cesur.trabajofingrado.jefe.mesas;

import com.google.gson.annotations.SerializedName;

public class AnadirMesaRequest {

    @SerializedName("numero_mesa")
    private int numeroMesa;

    @SerializedName("capacidad")
    private int capacidad;

    public AnadirMesaRequest(int numeroMesa, int capacidad) {
        this.numeroMesa = numeroMesa;
        this.capacidad = capacidad;
    }

    // Getters son opcionales para la serialización con Gson
    public int getNumeroMesa() {
        return numeroMesa;
    }

    public int getCapacidad() {
        return capacidad;
    }
}