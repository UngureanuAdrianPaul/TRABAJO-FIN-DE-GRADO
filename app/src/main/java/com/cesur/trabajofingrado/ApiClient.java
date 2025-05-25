package com.cesur.trabajofingrado;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {


    private static final String BASE_URL = ""; // IP DEL PC

    private static Retrofit retrofit = null; // Instancia unica de Retrofit (queremos un uso generico)
    private static ApiService apiService = null; // Instancia unica del servicio

    // Constructor privado para prevenir instanciación externa
    private ApiClient() {
    }
    
    //Obtiene la instancia unica de Retrofit. Si no existe, la crea.

    private static Retrofit getClient() {
        if (retrofit == null) {

            retrofit = new Retrofit.Builder().baseUrl(BASE_URL) // URL base de la API
                    .addConverterFactory(GsonConverterFactory.create()) // Convertidor para JSON (Gson)
                    .build(); // Construye la instancia de Retrofit
        }
        return retrofit;
    }


    //Obtiene la instancia única de ApiService. Si no existe, la crea usando Retrofit.

    public static ApiService getApiService() {
        if (apiService == null) {
            // Crea la implementacion de la interfaz ApiService usando la instancia de Retrofit
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    // Metodo opcional para resetear las instancias
    public static void resetApiService() {
        retrofit = null;
        apiService = null;
    }
}