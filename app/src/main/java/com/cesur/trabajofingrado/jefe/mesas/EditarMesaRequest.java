package com.cesur.trabajofingrado.jefe.mesas;

import com.google.gson.annotations.SerializedName;

public class EditarMesaRequest {

    @SerializedName("numero_mesa") // Coincide con la clave que espera el PHP
    private int numeroMesa;

    @SerializedName("nueva_capacidad")
    private int nuevaCapacidad;

    public EditarMesaRequest(int numeroMesa, int nuevaCapacidad) {
        this.numeroMesa = numeroMesa;
        this.nuevaCapacidad = nuevaCapacidad;
    }

}