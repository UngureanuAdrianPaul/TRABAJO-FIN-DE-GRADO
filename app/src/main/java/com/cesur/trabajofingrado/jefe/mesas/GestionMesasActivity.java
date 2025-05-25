package com.cesur.trabajofingrado.jefe.mesas;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.ApiClient;
import com.cesur.trabajofingrado.ApiService;
import com.cesur.trabajofingrado.GenericApiResponse;
import com.cesur.trabajofingrado.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionMesasActivity extends AppCompatActivity implements GestionMesaAdapter.OnMesaManagementClickListener {

    private static final String TAG = "GestionMesasActivity";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAnadirMesa;
    private GestionMesaAdapter adapter;
    private List<MesaInfo> listaDeMesas;
    private ApiService apiService;
    private int mesaEditandoPosicion = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_mesas);

        toolbar = findViewById(R.id.toolbarGestionMesas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestionar Mesas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewGestionMesas);
        fabAnadirMesa = findViewById(R.id.fabAnadirMesa);

        apiService = ApiClient.getApiService();
        listaDeMesas = new ArrayList<>();
        adapter = new GestionMesaAdapter(this, listaDeMesas, this); // 'this' si implementas listener

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        cargarMesasFisicas();

        fabAnadirMesa.setOnClickListener(v -> {
            mostrarDialogoAnadirMesa();
        });
    }

    // --- Metodo de la interfaz ---
    @Override
    public void onEditMesaClick(MesaInfo mesaInfo, int position) {
        Log.d(TAG, "Editar mesa: " + mesaInfo.getNumero() + " en posición: " + position);
        this.mesaEditandoPosicion = position;
        mostrarDialogoEditarCapacidadMesa(mesaInfo);
    }

    // --- Metodo de la interfaz ---
    @Override
    public void onDeleteMesaClick(final MesaInfo mesaInfo, final int position) {
        Log.d(TAG, "Solicitud para eliminar mesa física: N°" + mesaInfo.getNumero() + " en posición: " + position);

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar la Mesa N°" + mesaInfo.getNumero() + "?\n\nADVERTENCIA: Si esta mesa ha sido usada (tiene entradas en mesas activas, incluso pasadas), es posible que no se pueda eliminar directamente para mantener la integridad de los datos históricos. Si la eliminación falla, puede ser por esta razón.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    eliminarMesaFisicaEnAPI(mesaInfo.getNumero(), position);
                })
                .setNegativeButton("No, Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    // --- FIN MÉTODOS DE LA INTERFAZ ---

    // --- Metodo para llamar a la API de eliminacion de mesa
    private void eliminarMesaFisicaEnAPI(int numeroMesa, final int position) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en eliminarMesaFisicaEnAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

         Toast.makeText(this, "ELIMINANDO MESA", Toast.LENGTH_SHORT).show();

        EliminarMesaFisicaRequest request = new EliminarMesaFisicaRequest(numeroMesa);
        Call<GenericApiResponse> call = apiService.eliminarMesaFisica(request);

        call.enqueue(new Callback<GenericApiResponse>() {
            @Override
            public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.i(TAG, "Respuesta de API eliminar mesa física: " + apiResponse.getMessage());
                      //  Toast.makeText(GestionMesasActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refrescar la lista de mesas para eliminar la mesa de la UI
                        cargarMesasFisicas();
                    } else {
                        // Error lógico devuelto por la API (ej. mesa no encontrada, o el error de FK "mesa en uso")
                        Log.e(TAG, "Error de API al eliminar mesa física: " + apiResponse.getMessage());
                        Toast.makeText(GestionMesasActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP (404, 409 Conflict, 500, etc.)
                    String errorBodyStr = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyStr = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo errorBody al eliminar mesa", e);
                    }

                    String httpErrorMsg = "Error del servidor (" + response.code() + ") al eliminar mesa.";
                    if (response.code() == 409) { // Conflict

                        if (!errorBodyStr.isEmpty() && errorBodyStr.startsWith("{")) { // Intentar ver si es el JSON de error
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                GenericApiResponse errorResponse = gson.fromJson(errorBodyStr, GenericApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    httpErrorMsg = errorResponse.getMessage();
                                }
                            } catch (com.google.gson.JsonSyntaxException e) {
                                Log.w(TAG, "No se pudo parsear errorBody como JSON: " + errorBodyStr);
                            }
                        } else if (!errorBodyStr.isEmpty()) {

                        }
                        Toast.makeText(GestionMesasActivity.this, httpErrorMsg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GestionMesasActivity.this, httpErrorMsg, Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "Error en respuesta HTTP al eliminar mesa física. Código: " + response.code() + " Cuerpo: " + errorBodyStr);
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API eliminarMesaFisica: " + t.getMessage(), t);
                Toast.makeText(GestionMesasActivity.this, "Error de red. No se pudo eliminar la mesa.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Metodo para mostrar el dialogo de editar capacidad ---
    private void mostrarDialogoEditarCapacidadMesa(final MesaInfo mesaAEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_editar_capacidad_mesa, null);
        builder.setView(dialogView);

        final TextView tvTituloDialogo = dialogView.findViewById(R.id.textViewDialogEditarMesaTitulo);
        final TextView tvNumeroMesaInfo = dialogView.findViewById(R.id.textViewNumeroMesaInfo);
        final TextInputEditText etNuevaCapacidad = dialogView.findViewById(R.id.editTextNuevaCapacidadMesa);

        tvTituloDialogo.setText("Editar Mesa N°" + mesaAEditar.getNumero());
        tvNumeroMesaInfo.setText(String.format(Locale.getDefault(), "Mesa Actual N°: %d", mesaAEditar.getNumero()));
        etNuevaCapacidad.setText(String.valueOf(mesaAEditar.getCapacidad())); // Pre-rellenar capacidad

        builder.setPositiveButton("Guardar Cambios", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            mesaEditandoPosicion = -1; // Resetear si se cancela
            dialog.dismiss();
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String nuevaCapacidadStr = etNuevaCapacidad.getText().toString().trim();
                if (TextUtils.isEmpty(nuevaCapacidadStr)) {
                    etNuevaCapacidad.setError("La capacidad es requerida.");
                    return;
                }

                int nuevaCapacidad = -1;
                try {
                    nuevaCapacidad = Integer.parseInt(nuevaCapacidadStr);
                    if (nuevaCapacidad <= 0) {
                        etNuevaCapacidad.setError("La capacidad debe ser positiva.");
                        return;
                    }
                    etNuevaCapacidad.setError(null);
                } catch (NumberFormatException e) {
                    etNuevaCapacidad.setError("Capacidad inválida.");
                    return;
                }

                if (nuevaCapacidad == mesaAEditar.getCapacidad()) {
                   // Toast.makeText(this, "No se realizaron cambios en la capacidad.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Guardar cambios para Mesa N°: " + mesaAEditar.getNumero() +
                            ", Nueva Capacidad: " + nuevaCapacidad);
                    editarMesaFisicaEnAPI(mesaAEditar.getNumero(), nuevaCapacidad);
                }
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    // --- Metodo para llamar a la api de edicion de mesa ---
    private void editarMesaFisicaEnAPI(int numeroMesa, int nuevaCapacidad) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en editarMesaFisicaEnAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "CAPACIDAD ACTUALIZADA", Toast.LENGTH_SHORT).show();

        EditarMesaRequest request = new EditarMesaRequest(numeroMesa, nuevaCapacidad);
        Call<GenericApiResponse> call = apiService.editarMesaFisica(request);

        call.enqueue(new Callback<GenericApiResponse>() {
            @Override
            public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericApiResponse apiResponse = response.body();
                    // El script PHP devuelve "success" o "info" para casos de "sin cambios"
                    if (apiResponse.isSuccess() || "info".equalsIgnoreCase(apiResponse.getStatus())) {
                        Log.i(TAG, "Respuesta de API editar mesa física: " + apiResponse.getMessage());
                       // Toast.makeText(GestionMesasActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refrescar la lista de mesas para mostrar los cambios
                        cargarMesasFisicas();
                        mesaEditandoPosicion = -1; // Resetear la posición después de la edición
                    } else {
                        // Error lógico devuelto por la API
                        Log.e(TAG, "Error de API al editar mesa física: " + apiResponse.getMessage());
                        Toast.makeText(GestionMesasActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP
                    String errorBodyStr = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyStr = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo errorBody al editar mesa", e);
                    }
                    Log.e(TAG, "Error en respuesta HTTP al editar mesa física. Código: " + response.code() + " Cuerpo: " + errorBodyStr);
                    Toast.makeText(GestionMesasActivity.this, "Error del servidor (" + response.code() + ") al editar mesa.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API editarMesaFisica: " + t.getMessage(), t);
                Toast.makeText(GestionMesasActivity.this, "Error de red. No se pudo editar la mesa.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Metodo para mostrar el dialogo de añadir mesa ---
    private void mostrarDialogoAnadirMesa() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_anadir_mesa, null);
        builder.setView(dialogView);

        final TextInputEditText etNumeroMesa = dialogView.findViewById(R.id.editTextNumeroMesaNueva);
        final TextInputEditText etCapacidadMesa = dialogView.findViewById(R.id.editTextCapacidadMesaNueva);
        // El TextView del título ya está en el XML, no necesitamos modificarlo aquí si es estático.

        builder.setPositiveButton("Guardar", null); // Listener se configura después para controlar cierre
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String numeroMesaStr = etNumeroMesa.getText().toString().trim();
                String capacidadMesaStr = etCapacidadMesa.getText().toString().trim();

                // Validaciones
                if (TextUtils.isEmpty(numeroMesaStr)) {
                    etNumeroMesa.setError("El número de mesa es requerido.");
                    return;
                }
                if (TextUtils.isEmpty(capacidadMesaStr)) {
                    etCapacidadMesa.setError("La capacidad es requerida.");
                    return;
                }

                int numeroMesa = -1;
                int capacidadMesa = -1;

                try {
                    numeroMesa = Integer.parseInt(numeroMesaStr);
                    if (numeroMesa <= 0) {
                        etNumeroMesa.setError("El número de mesa debe ser positivo.");
                        return;
                    }
                    etNumeroMesa.setError(null); // Limpiar error previo
                } catch (NumberFormatException e) {
                    etNumeroMesa.setError("Número de mesa inválido.");
                    return;
                }

                try {
                    capacidadMesa = Integer.parseInt(capacidadMesaStr);
                    if (capacidadMesa <= 0) {
                        etCapacidadMesa.setError("La capacidad debe ser positiva.");
                        return;
                    }
                    etCapacidadMesa.setError(null); // Limpiar error previo
                } catch (NumberFormatException e) {
                    etCapacidadMesa.setError("Capacidad inválida.");
                    return;
                }

                // Si todo es válido, llamar a la API para añadir la mesa
                Log.d(TAG, "Guardar Mesa Física: Número=" + numeroMesa + ", Capacidad=" + capacidadMesa);
                anadirMesaFisicaALaAPI(numeroMesa, capacidadMesa);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    // --- Metodo para llamar a la API ---
    private void anadirMesaFisicaALaAPI(int numeroMesa, int capacidad) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en anadirMesaFisicaALaAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "MESA GUARDADA", Toast.LENGTH_SHORT).show();

        AnadirMesaRequest request = new AnadirMesaRequest(numeroMesa, capacidad);
        Call<AnadirMesaResponse> call = apiService.anadirMesaFisica(request);

        call.enqueue(new Callback<AnadirMesaResponse>() {
            @Override
            public void onResponse(Call<AnadirMesaResponse> call, Response<AnadirMesaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnadirMesaResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.i(TAG, "Mesa física añadida con éxito: " + apiResponse.getMessage() + ", ID: " + apiResponse.getMesaId());
                       // Toast.makeText(GestionMesasActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refrescar la lista de mesas para mostrar la nueva
                        cargarMesasFisicas();
                    } else {
                        // Error lógico devuelto por la API (ej. número de mesa duplicado)
                        Log.e(TAG, "Error de API al añadir mesa física: " + apiResponse.getMessage());
                        Toast.makeText(GestionMesasActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP (400, 405, 500, etc.)
                    String errorBodyStr = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyStr = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo errorBody al añadir mesa", e);
                    }
                    Log.e(TAG, "Error en respuesta HTTP al añadir mesa física. Código: " + response.code() + " Cuerpo: " + errorBodyStr);
                    Toast.makeText(GestionMesasActivity.this, "No puedes añadir una mesa existente", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AnadirMesaResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API anadirMesaFisica: " + t.getMessage(), t);
                Toast.makeText(GestionMesasActivity.this, "Error de red. No se pudo añadir la mesa.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---Metodo para cargar la lista de las mesas del local ---
    private void cargarMesasFisicas() {
       // Toast.makeText(this, "Cargando mesas...", Toast.LENGTH_SHORT).show();
        Call<GetMesasFisicasResponse> call = apiService.getMesasFisicas();

        call.enqueue(new Callback<GetMesasFisicasResponse>() {
            @Override
            public void onResponse(Call<GetMesasFisicasResponse> call, Response<GetMesasFisicasResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MesaInfo> mesasRecibidas = response.body().getMesas();
                    if (mesasRecibidas != null) {
                        Log.d(TAG, "Mesas físicas recibidas: " + mesasRecibidas.size());
                        adapter.actualizarLista(mesasRecibidas);
                    } else {
                        adapter.actualizarLista(new ArrayList<>()); // Lista vacía
                        Toast.makeText(GestionMesasActivity.this, "No hay mesas físicas registradas.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Error al cargar mesas físicas";
                    if (response.body() != null && response.body().getMessage() != null)
                        errorMsg = response.body().getMessage();
                    else if (!response.isSuccessful()) errorMsg = "Error HTTP: " + response.code();
                    Log.e(TAG, "Error API al cargar mesas físicas: " + errorMsg);
                    Toast.makeText(GestionMesasActivity.this, "Error API: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GetMesasFisicasResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar mesas físicas: " + t.getMessage(), t);
                Toast.makeText(GestionMesasActivity.this, "Error de red. Verifica tu conexión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}