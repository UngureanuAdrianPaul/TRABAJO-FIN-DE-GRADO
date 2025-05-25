package com.cesur.trabajofingrado.comanda;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CrearComandaRequest {

    @SerializedName("id_camarero")
    private int idCamarero;

    @SerializedName("id_mesa_activa")
    private int idMesaActiva;

    @SerializedName("items") // La lista de productos
    private List<ItemComandaTemporal> items;

    // Constructor
    public CrearComandaRequest(int idCamarero, int idMesaActiva, List<ItemComandaTemporal> items) {
        this.idCamarero = idCamarero;
        this.idMesaActiva = idMesaActiva;
        this.items = items;
    }

    // Getters (Gson los usa)
    public int getIdCamarero() {
        return idCamarero;
    }

    public int getIdMesaActiva() {
        return idMesaActiva;
    }

    public List<ItemComandaTemporal> getItems() {
        return items;
    }
}