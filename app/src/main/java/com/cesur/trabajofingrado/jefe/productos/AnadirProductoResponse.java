package com.cesur.trabajofingrado.jefe.productos;

import com.google.gson.annotations.SerializedName;

public class AnadirProductoResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("producto_id")
    private Integer productoId; // Usar Integer para que pueda ser null si la API no lo envía en caso de error

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}