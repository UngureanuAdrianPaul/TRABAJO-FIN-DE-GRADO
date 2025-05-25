package com.cesur.trabajofingrado.mesas;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetMesasResponse {

    @SerializedName("status")
    private String status; // "success" o "error"

    @SerializedName("nombreCamarero")
    private String nombreCamarero; // Nombre completo para mostrar

    @SerializedName("listaMesas")
    private List<Mesa> listaMesas; // La lista de objetos Mesa

    @SerializedName("message")
    private String message; // Para mensajes de éxito o error desde la API

    // --- Getters ---
    public String getStatus() {
        return status;
    }

    public String getNombreCamarero() {
        return nombreCamarero;
    }

    public List<Mesa> getListaMesas() {
        return listaMesas;
    }

    public String getMessage() {
        return message;
    }

}

