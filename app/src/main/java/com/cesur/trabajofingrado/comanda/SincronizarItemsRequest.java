package com.cesur.trabajofingrado.comanda;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SincronizarItemsRequest {

    @SerializedName("comanda_id")
    private int comandaId;

    @SerializedName("items")
    private List<ItemComandaTemporal> items;

    public SincronizarItemsRequest(int comandaId, List<ItemComandaTemporal> items) {
        this.comandaId = comandaId;
        this.items = items;
    }
}