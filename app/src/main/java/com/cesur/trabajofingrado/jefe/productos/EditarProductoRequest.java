package com.cesur.trabajofingrado.jefe.productos;

import com.google.gson.annotations.SerializedName;

public class EditarProductoRequest {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("precio")
    private double precio;

    @SerializedName("categoria")
    private String categoria;

    public EditarProductoRequest(int id, String nombre, double precio, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }

    // Getters son opcionales para la serialización con Gson, pero pueden ser útiles
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }
}