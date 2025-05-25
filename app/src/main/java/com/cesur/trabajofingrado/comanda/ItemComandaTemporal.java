package com.cesur.trabajofingrado.comanda;


import java.io.Serializable;

public class ItemComandaTemporal implements Serializable {

    private int productId;
    private String productName;
    private String productType; // "comida" o "bebida"
    private int quantity;
    private double unitPrice;
    private boolean isSentToKitchen = false;


    // Constructor
    public ItemComandaTemporal(int productId, String productName, String productType, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.isSentToKitchen = false;
    }

    // Getters (necesarios para el Adapter)
    public int getProductId() {
        return productId;
    }
    public boolean isSentToKitchen() {
        return isSentToKitchen;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductType() {
        return productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    // Setters
    public void setQuantity(int quantity) {

        this.quantity = quantity;
    }
    public void setSentToKitchen(boolean sentToKitchen) {
        isSentToKitchen = sentToKitchen;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    // Metodo para calcular el precio total de esta línea
    public double getTotalPrice() {
        return this.quantity * this.unitPrice;
    }
}
