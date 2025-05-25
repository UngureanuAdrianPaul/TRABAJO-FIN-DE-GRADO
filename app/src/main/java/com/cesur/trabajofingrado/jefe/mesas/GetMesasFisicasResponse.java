package com.cesur.trabajofingrado.jefe.mesas;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetMesasFisicasResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("mesas") // La clave JSON que devuelve el PHP
    private List<MesaInfo> mesas;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<MesaInfo> getMesas() {
        return mesas;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}