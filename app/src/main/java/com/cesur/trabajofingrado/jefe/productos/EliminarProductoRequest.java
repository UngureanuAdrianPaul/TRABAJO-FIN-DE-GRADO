package com.cesur.trabajofingrado.jefe.productos;

import com.google.gson.annotations.SerializedName;

public class EliminarProductoRequest {

    @SerializedName("id")
    private int id;

    @SerializedName("categoria")
    private String categoria;

    public EliminarProductoRequest(int id, String categoria) {
        this.id = id;
        this.categoria = categoria;
    }

    // Getters son opcionales para la serialización con Gson
    public int getId() {
        return id;
    }

    public String getCategoria() {
        return categoria;
    }
}
