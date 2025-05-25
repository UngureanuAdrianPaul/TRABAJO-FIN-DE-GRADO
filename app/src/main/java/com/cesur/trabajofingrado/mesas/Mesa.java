package com.cesur.trabajofingrado.mesas;

import com.google.gson.annotations.SerializedName;

public class Mesa {

    // Los nombres deben coincidir con las claves JSON de get_mesas.php
    // o usar @SerializedName si son diferentes.
    @SerializedName("numero")
    private int numero;

    @SerializedName("capacidad")
    private int capacidad;

    @SerializedName("estado") // Esperamos "libre" u "ocupada"
    private String estado;

    @SerializedName("idMesaActiva")
    private Integer idMesaActiva;

    // --- Getters ---
    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public Integer getIdMesaActiva() {
        return idMesaActiva;
    }

}