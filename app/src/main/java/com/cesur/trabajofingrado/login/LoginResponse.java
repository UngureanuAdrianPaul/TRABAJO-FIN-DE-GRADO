package com.cesur.trabajofingrado.login;


import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    // Los nombres de los campos deben coincidir con las claves del JSON que envía PHP
    // O usar @SerializedName si los nombres son diferentes

    @SerializedName("status")
    private String status; // 'success' o 'error'

    @SerializedName("message")
    private String message; // Mensaje descriptivo

    // Estos campos solo vendrán si status es 'success'
    @SerializedName("userId")
    private int userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("rol")
    private String rol; // 'camarero' o 'jefe'

    // --- Getters ---
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRol() {
        return rol;
    }


}






