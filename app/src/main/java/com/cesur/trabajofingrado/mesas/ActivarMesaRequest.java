package com.cesur.trabajofingrado.mesas;

import com.google.gson.annotations.SerializedName;

public class ActivarMesaRequest {

    @SerializedName("num_mesa") // Coincide con la clave que espera el PHP
    private int numMesa;

    @SerializedName("id_camarero") // Coincide con la clave que espera el PHP
    private int idCamarero;

    // Constructor
    public ActivarMesaRequest(int numMesa, int idCamarero) {
        this.numMesa = numMesa;
        this.idCamarero = idCamarero;
    }

    // Getters (Gson podría no necesitarlos para serializar, pero son buena práctica)
    public int getNumMesa() {
        return numMesa;
    }

    public int getIdCamarero() {
        return idCamarero;
    }
}