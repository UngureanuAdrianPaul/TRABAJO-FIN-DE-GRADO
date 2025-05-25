package com.cesur.trabajofingrado.comanda;

import com.google.gson.annotations.SerializedName;

public class CrearComandaResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("comanda_id") // Opcional, pero útil
    private Integer comandaId; // Usamos Integer para que pueda ser null si hay error

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getComandaId() {
        return comandaId;
    }
}
