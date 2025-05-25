package com.cesur.trabajofingrado.productos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetProductosResponse {

    @SerializedName("status")
    private String status; // "success" o "error"

    @SerializedName("message") // Para mensajes de error desde la API
    private String message;

    @SerializedName("productos") // Debe coincidir con la clave JSON del PHP
    private List<Producto> productos;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Producto> getProductos() {
        return productos;
    }

}
