package com.cesur.trabajofingrado;

import com.google.gson.annotations.SerializedName;

public class GenericApiResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
