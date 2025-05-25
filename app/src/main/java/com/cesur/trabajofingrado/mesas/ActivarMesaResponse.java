package com.cesur.trabajofingrado.mesas;

import com.google.gson.annotations.SerializedName;

public class ActivarMesaResponse {

    @SerializedName("status") // Coincide con la clave que devuelve el PHP
    private String status;

    @SerializedName("message") // Coincide con la clave que devuelve el PHP
    private String message;

    // Usamos Integer para que pueda ser null si la API no lo devuelve en caso de error
    @SerializedName("id_mesa_activa")
    private Integer idMesaActiva;

    // Getters (Necesarios para leer la respuesta en la Activity)
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getIdMesaActiva() {
        return idMesaActiva;
    }

    // Metodo auxiliar para comprobar éxito fácilmente
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) && idMesaActiva != null && idMesaActiva > 0;
    }
}
