package com.cesur.trabajofingrado.productos;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Útil si necesitas pasar objetos Producto

public class Producto implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre") // Coincide con el alias 'nombre' en el PHP
    private String nombre;

    @SerializedName("precio") // Coincide con el alias 'precio' en el PHP
    private double precio;

    // Constructor vacío
    public Producto() { }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    // Setters (opcional)

    // toString (útil para debug)
    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                '}';
    }
}
