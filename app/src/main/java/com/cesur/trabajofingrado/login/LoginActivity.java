package com.cesur.trabajofingrado.login;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import com.cesur.trabajofingrado.AppPreferences;
import com.cesur.trabajofingrado.ApiClient;
import com.cesur.trabajofingrado.ApiService;
import com.cesur.trabajofingrado.mesas.MesaActivity;
import com.cesur.trabajofingrado.jefe.JefeActivity;
import com.cesur.trabajofingrado.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText userLogin;
    private EditText passwordLogin;
    private Button loginButton;
    private TextView errorTextLogin;
    private ApiService apiServiceLogin;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(AppPreferences.PREF_NAME, Context.MODE_PRIVATE);

        // --- COMPROBAR SI YA HAY UNA SESIÓN ACTIVA ---

        if (sharedPreferences.getBoolean(AppPreferences.KEY_IS_LOGGED_IN, false)) {
            // Si ya está logueado, obtener datos y redirigir
            int userId = sharedPreferences.getInt(AppPreferences.KEY_USER_ID, -1);
            String userName = sharedPreferences.getString(AppPreferences.KEY_USER_NAME, null);
            String userRole = sharedPreferences.getString(AppPreferences.KEY_USER_ROLE, null);

            if (userId != -1 && userName != null && userRole != null) {
                Log.i(TAG, "Sesión activa encontrada para: " + userName + " (" + userRole + ")");
                redirigirSegunRol(userId, userName, userRole);
                return; // Importante: Salir de onCreate para no mostrar el layout de login
            } else {
                // Datos de sesión corruptos, limpiar y proceder al login normal
                Log.w(TAG, "Datos de sesión corruptos. Limpiando SharedPreferences.");
                clearSession(); // Metodo para limpiar SharedPreferences
            }
        }

        // --- FIN COMPROBACIÓN SESIÓN ACTIVA ---

        // Inicializar vistas
        userLogin = findViewById(R.id.userLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.buttonLogin);
        errorTextLogin = findViewById(R.id.errorTextLogin);

        // Configurar el listener del botón de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtener usuario y contraseña
                String username = userLogin.getText().toString().trim();
                String password = passwordLogin.getText().toString().trim();

                //Validar que no estén vacíos
                if (username.isEmpty()) {
                    userLogin.setError(getString(R.string.error_campo_requerido));
                    return; // Detener proceso
                }
                if (password.isEmpty()) {
                    passwordLogin.setError(getString(R.string.error_campo_requerido));
                    return; // Detener proceso
                }

                // --- Configuración Retrofit ---
                apiServiceLogin = ApiClient.getApiService();

                //Llamar a la API para hacer login
                makeLogin(username, password);
            }
        });
    }

    //Metodo que hace uso de la API login.php
    private void makeLogin(String username, String password) {

        Call<LoginResponse> call = apiServiceLogin.login(username, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && (response.body() != null)) {
                    LoginResponse loginResponse = response.body();

                    // Verificar la respuesta del servidor
                    if ("success".equals(loginResponse.getStatus())) { // Asume que el JSON devuelve un campo 'status'
                        Log.i(TAG, "Login exitoso. Rol: " + loginResponse.getRol());

                        // Obtener datos del usuario desde la respuesta
                        String userRole = loginResponse.getRol();
                        String userFullName = loginResponse.getUserName();
                        int userId = loginResponse.getUserId();

                        // Verificar si se obtuvo un ID válido
                        if (userId <= 0) { // Asumiendo que los IDs son positivos
                            Log.e(TAG, "Error: userId inválido recibido del login: " + userId);
                            showError("Error interno al obtener datos de usuario.");
                            return; // No continuar si el ID no es válido
                        }


                        // --- GUARDAR SESIÓN EN SharedPreferences ---
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(AppPreferences.KEY_IS_LOGGED_IN, true);
                        editor.putInt(AppPreferences.KEY_USER_ID, userId);
                        editor.putString(AppPreferences.KEY_USER_NAME, userFullName); // Guardar el nombre completo
                        editor.putString(AppPreferences.KEY_USER_ROLE, userRole);
                        editor.apply(); // Usar apply() para guardar en segundo plano
                        // --- FIN GUARDAR SESIÓN ---

                        // Decidir a qué Activity ir segun el rol
                        redirigirSegunRol(userId, userFullName, userRole);
                    } else {
                        // Credenciales incorrectas u otro error devuelto por la API
                        String errorMessage = loginResponse.getMessage(); // Asume campo 'message' en JSON
                        if (errorMessage == null || errorMessage.isEmpty()) {
                            errorMessage = getString(R.string.login_error_credentials);
                        }
                        showError(errorMessage);
                        Log.w(TAG, "Login fallido: " + errorMessage);
                    }
                } else {
                    // Error en la respuesta HTTP (401, 404, 500, etc.)
                    String errorMsg = getString(R.string.login_error_credentials); // Mensaje genérico
                    if (response.code() == 401) { // No autorizado (usuario/pass incorrecto)
                        errorMsg = getString(R.string.login_error_credentials);
                    } else if (response.code() >= 500) { // Error del servidor
                        errorMsg = getString(R.string.login_error_server) + " (" + response.code() + ")";
                    }
                    // Podrías intentar leer response.errorBody() para más detalles si el servidor envía JSON en errores
                    Log.w(TAG, "Login fallido. Código de respuesta HTTP: " + response.code());
                    showError(errorMsg);

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                // Error de red o fallo al conectar/procesar
                Log.e(TAG, "Fallo en llamada login: " + t.getMessage(), t);
                showError(getString(R.string.login_error_network));
            }
        });
    }

    // Metodo para redirigir y pasar datos
    private void redirigirSegunRol(int userId, String userName, String userRole) {
        if ("camarero".equals(userRole)) {
            Intent intent = new Intent(LoginActivity.this, MesaActivity.class);

            // Pasamos USERNAME (que es el nombre completo) y USER_ID
            intent.putExtra(MesaActivity.USERNAME_KEY, userName); // Usar la constante de MesaActivity
            intent.putExtra(MesaActivity.USER_ID_KEY, userId);   // Usar la constante de MesaActivity
            startActivity(intent);
            finish();
            Toast.makeText(LoginActivity.this, "Login Camarero OK", Toast.LENGTH_SHORT).show();
        } else if ("jefe".equals(userRole)) {
            Intent intent = new Intent(LoginActivity.this, JefeActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(LoginActivity.this, "Login Jefe OK", Toast.LENGTH_SHORT).show();
        } else {
            showError(getString(R.string.login_error_unknown_role));
        }
    }

    // Metodo para limpiar SharedPreferences (lo usaremos también al cerrar sesión)
    public static void clearSession(Context context) { // Hacerlo estático para poder llamarlo desde otras activities
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppPreferences.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AppPreferences.KEY_IS_LOGGED_IN, false);
        editor.remove(AppPreferences.KEY_USER_ID);
        editor.remove(AppPreferences.KEY_USER_NAME);
        editor.remove(AppPreferences.KEY_USER_ROLE);
        editor.apply();
        Log.i("SessionManagement", "Sesión limpiada.");
    }

    // Sobrecarga para llamarlo sin contexto desde la misma clase (si es necesario)
    private void clearSession() {
        clearSession(this);
    }

    // Función auxiliar para mostrar el error en el TextView
    private void showError(String message) {
        errorTextLogin.setText(message);
        errorTextLogin.setVisibility(View.VISIBLE);
    }
}