package com.cesur.trabajofingrado.jefe.mesas;


import com.google.gson.annotations.SerializedName;

public class AnadirMesaResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("mesa_id")
    private Integer mesaId;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getMesaId() {
        return mesaId;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}