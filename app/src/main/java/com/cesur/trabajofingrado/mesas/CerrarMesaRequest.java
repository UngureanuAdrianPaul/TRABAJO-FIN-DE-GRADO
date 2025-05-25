package com.cesur.trabajofingrado.mesas;

import com.google.gson.annotations.SerializedName;



public class CerrarMesaRequest {
    @SerializedName("id_mesa_activa")
    private int idMesaActiva;

    @SerializedName("comanda_id")
    private Integer comandaId; // Integer para que pueda ser null

    public CerrarMesaRequest(int idMesaActiva, Integer comandaId) {
        this.idMesaActiva = idMesaActiva;
        this.comandaId = comandaId;
    }

}