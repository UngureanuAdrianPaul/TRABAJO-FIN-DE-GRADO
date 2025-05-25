package com.cesur.trabajofingrado.comanda;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetComandaItemsResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message") // Opcional para errores
    private String message;

    @SerializedName("items")
    private List<ItemComandaTemporal> items;

    @SerializedName("comanda_id")
    private Integer comandaId;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ItemComandaTemporal> getItems() {
        return items;
    }

    public Integer getComandaId() {
        return comandaId;
    }

    // Metodo auxiliar
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
