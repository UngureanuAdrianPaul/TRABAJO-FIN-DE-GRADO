package com.cesur.trabajofingrado.jefe.mesas;

import com.google.gson.annotations.SerializedName;

public class EliminarMesaFisicaRequest {

    @SerializedName("numero_mesa") // Debe coincidir con la clave que espera el PHP
    private int numeroMesa;

    public EliminarMesaFisicaRequest(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }
    
}