package com.cesur.trabajofingrado;

public class AppPreferences {
    public static final String PREF_NAME = "TfgAppPrefs"; // Nombre del archivo de preferencias

    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName"; // Para el nombre completo
    public static final String KEY_USER_ROLE = "userRole";

    // Constructor privado para evitar instanciación (clase de utilidades/constantes)
    private AppPreferences() {
    }
}
