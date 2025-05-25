package com.cesur.trabajofingrado.mesas;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import com.cesur.trabajofingrado.camarero.CamareroActivity;
import com.cesur.trabajofingrado.ApiService;
import com.cesur.trabajofingrado.ApiClient;
import com.cesur.trabajofingrado.R;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MesaActivity extends AppCompatActivity implements MesaAdapter.OnMesaClickListener {

    //Para Logcat
    private static final String TAG = "MesaActivity";

    //Claves que se utilizan para identificar los
    // datos que paso de una Activity a otra cuando se usa Intent.
    public static final String USERNAME_KEY = "USERNAME";
    public static final String USER_ID_KEY = "USER_ID";
    public static final String ID_MESA_ACTIVA_KEY = "ID_MESA_ACTIVA";

    private RecyclerView recyclerViewMesas;
    private MesaAdapter mesaAdapter;
    private TextView waiterNameTextView;


    private ApiService apiService;
    private int userId = -1; //ID Camarero Logueado


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesa);

        // --- Obtener datos del Intent ---
        Intent intent = getIntent();
        String username = null;

        // --- Inicializar Vistas ---
        waiterNameTextView = findViewById(R.id.waiterNameText);
        recyclerViewMesas = findViewById(R.id.tableListView);

        // --- Configuracion Retrofit ---
        apiService = ApiClient.getApiService();


        if (intent != null && intent.hasExtra(USERNAME_KEY)) {

            //Para recibir el nombre del camarero
            if (intent.hasExtra(USERNAME_KEY)) {
                username = intent.getStringExtra(USERNAME_KEY);
                Log.d(TAG, "Username recibido: " + username);
            } else {
                Log.e(TAG, "Error: No se recibió el username en el Intent.");
            }

            //Para recibir el userId
            userId = intent.getIntExtra(USER_ID_KEY, -1);
            if (userId == -1) {
                Log.e(TAG, "Error: No se recibió el userId en el Intent con la clave " + USER_ID_KEY);
            } else {
                Log.d(TAG, "UserId recibido: " + userId);
            }
        } else {
            Log.e(TAG, "Error: No se recibió el username en el Intent con la clave " + USERNAME_KEY);
            username = "Camarero"; // Un valor por defecto si falla
        }


        // --- Mostrar el nombre en el TextView ---
        if (waiterNameTextView != null) {
            waiterNameTextView.setText("Camarero: " + username);
        } else {
            Log.e(TAG, "Error: TextView waiterNameText no encontrado en el layout.");
        }


        // --- Configuracion RecyclerView ---

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewMesas.setLayoutManager(layoutManager);
        recyclerViewMesas.setHasFixedSize(true); // Optimizacion si el tamaño del item no cambia

        // Adaptador (inicialmente con lista vacía)
        mesaAdapter = new MesaAdapter(this, new ArrayList<>(), this);
        recyclerViewMesas.setAdapter(mesaAdapter);

        // --- Cargar Datos ---
        if (userId != -1) {
            cargarDatosMesas();
        } else {
            Log.e(TAG, "Error: userId no recibido del Intent.");
            Toast.makeText(this, R.string.user_not_identified_error, Toast.LENGTH_LONG).show();
        }
    }

    //Metodo para cargar los datos de las mesas existentes en la base de datos junto con el nombre
    //del camarero
    private void cargarDatosMesas() {

        Call<GetMesasResponse> call = apiService.getMesas(userId);

        call.enqueue(new Callback<GetMesasResponse>() {
            @Override
            public void onResponse(Call<GetMesasResponse> call, Response<GetMesasResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    GetMesasResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        // Actualizar nombre del camarero con el de la API
                        waiterNameTextView.setText("Mesas para: " + apiResponse.getNombreCamarero());
                        // Actualizar la lista de mesas en el adaptador
                        if (apiResponse.getListaMesas() != null) {
                            mesaAdapter.updateData(apiResponse.getListaMesas());
                        } else {
                            mesaAdapter.updateData(new ArrayList<>()); // Lista vacia si es null
                            Toast.makeText(MesaActivity.this, R.string.no_tables_available, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error logico devuelto por la API
                        String mensajeError = apiResponse.getMessage() != null ? apiResponse.getMessage() : getString(R.string.table_not_loading_error);
                        Toast.makeText(MesaActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error de API al cargar mesas: " + mensajeError);
                    }

                } else {
                    // Error en la respuesta HTTP
                    Log.e(TAG, "Error en respuesta HTTP mesas. Código: " + response.code());
                    Toast.makeText(MesaActivity.this, getString(R.string.server_error) + " (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GetMesasResponse> call, Throwable t) {

                // Error de red o de Retrofit
                Log.e(TAG, "Fallo en llamada API mesas: " + t.getMessage(), t);
                Toast.makeText(MesaActivity.this, R.string.table_server_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Implementacion del metodo de la interfaz OnMesaClickListener ---
    @Override
    public void onMesaClick(final Mesa mesa) {
        Log.i(TAG, "Mesa seleccionada: " + mesa.getNumero() + ", Estado UI: " + mesa.getEstado() + ", ID Activa (si existe): " + mesa.getIdMesaActiva());

        if ("libre".equalsIgnoreCase(mesa.getEstado())) {
            // --- FLUJO PARA MESA LIBRE (COMO ANTES) ---
            // Mostrar diálogo para comensales
            mostrarDialogoComensalesYActivar(mesa); // Mover lógica a un método nuevo

        } else { // La mesa está 'ocupada' según la UI (significa activa en BD + tiene comandas)
            // O podría estar 'libre' en UI pero tener idMesaActiva (activa en BD, sin comandas)
            // En ambos casos donde idMesaActiva no es null, queremos reabrir la comanda existente.

            Integer idMesaActivaExistente = mesa.getIdMesaActiva();

            if (idMesaActivaExistente != null && idMesaActivaExistente > 0) {
                // --- FLUJO PARA MESA YA ACTIVA (OCUPADA O NO EN UI) ---
                Log.d(TAG, "Reabriendo mesa activa existente con ID: " + idMesaActivaExistente);

                // No necesitamos activar, ya lo está. Vamos directo a CamareroActivity.
                // Pasamos -1 para comensales, ya que no los preguntamos al reabrir.
                iniciarCamareroActivity(mesa.getNumero(), -1, idMesaActivaExistente, userId);

            } else {
                // Caso raro: Estado es 'ocupada' pero no tenemos ID? Podría ser un error lógico.
                // O si el estado fuera 'libre' y el ID también null (caso normal de libre).
                // Por seguridad, tratamos como si estuviera libre si no hay ID activo.
                Log.w(TAG, "Mesa con estado " + mesa.getEstado() + " pero sin idMesaActiva válido. Tratando como libre.");
                mostrarDialogoComensalesYActivar(mesa);
            }
        }
    }

    // --- Metodo para mostrar el dialogo del numero de comensales ---
    private void mostrarDialogoComensalesYActivar(final Mesa mesa){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_comensales, null);
        builder.setView(dialogView);

        //Variables para el dialogo de comensales
        final EditText editTextComensales = dialogView.findViewById(R.id.editTextComensales);
        final TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        final TextView textCapacidad = dialogView.findViewById(R.id.textCapacidadMesa);

        // Personalizar titulo y mostrar capacidad
        dialogTitle.setText(getString(R.string.introducir_comensales_title) + " (Mesa " + mesa.getNumero() + ")");
        if (textCapacidad != null) {
            textCapacidad.setText(getString(R.string.capacidad_mesa_placeholder, mesa.getCapacidad()));
        }

        // Configurar botones del dialogo
        builder.setPositiveButton(R.string.dialog_aceptar, null); // Ponemos null para controlar el cierre manualmente
        builder.setNegativeButton(R.string.dialog_cancelar, (dialog, id) -> {
            // Accion al cancelar (simplemente cierra el dialogo)
            dialog.dismiss();
        });

        final AlertDialog dialog = builder.create();

        // Controlar clic del boton positivo manualmente para validacion
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String numComensalesStr = editTextComensales.getText().toString().trim();
                int numComensales = 0;
                boolean valido = false;

                // Validacion basica
                if (numComensalesStr.isEmpty()) {
                    editTextComensales.setError(getString(R.string.error_comensales_vacio));
                } else {
                    try {
                        numComensales = Integer.parseInt(numComensalesStr);
                        if (numComensales > 0) { // Asegurar que es positivo
                            // Validar contra capacidad
                            if (numComensales <= mesa.getCapacidad()) {
                                valido = true;
                            } else {
                                editTextComensales.setError("La capacidad máxima es " + mesa.getCapacidad());
                            }

                        } else {
                            editTextComensales.setError(getString(R.string.error_comensales_no_valido));
                        }
                    } catch (NumberFormatException e) {
                        editTextComensales.setError(getString(R.string.error_comensales_no_valido));
                    }
                }

                // Si es valido, navegar a CamareroActivity
                if (valido) {
                    dialog.dismiss(); // Cerrar el diálogo

                    // Llamar al metodo para activar la mesa
                    activarMesaYProceder(mesa.getNumero(), numComensales, userId);

                }

            });
        });

        dialog.show(); //Mostrar el dialogo

    }

    // --- Metodo para activar una mesa en concreto ---
    private void activarMesaYProceder(final int numeroMesa, final int numComensales, final int camareroId) {
        if (camareroId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al camarero para activar la mesa.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "activarMesaYProceder: camareroId es -1.");
            return;
        }


        //  Toast.makeText(this, "Activando mesa " + numeroMesa + "...", Toast.LENGTH_SHORT).show(); // Indicador simple

        ActivarMesaRequest request = new ActivarMesaRequest(numeroMesa, camareroId);
        Call<ActivarMesaResponse> call = apiService.activarMesa(request);

        call.enqueue(new Callback<ActivarMesaResponse>() {
            @Override
            public void onResponse(Call<ActivarMesaResponse> call, Response<ActivarMesaResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ActivarMesaResponse activarMesaResponse = response.body();
                    if (activarMesaResponse.isSuccess() && activarMesaResponse.getIdMesaActiva() != null) {
                        int idMesaActiva = activarMesaResponse.getIdMesaActiva();
                        Log.d(TAG, "Mesa " + numeroMesa + " activada con éxito. ID Mesa Activa: " + idMesaActiva);
                        //   Toast.makeText(MesaActivity.this, activarMesaResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        iniciarCamareroActivity(numeroMesa, numComensales, idMesaActiva, camareroId);

                    } else {
                        String errorMessage = activarMesaResponse.getMessage() != null ? activarMesaResponse.getMessage() : getString(R.string.error_activacion_mesa);
                        Log.e(TAG, "Error al activar mesa " + numeroMesa + ": " + errorMessage);
                        //   Toast.makeText(MesaActivity.this, "Error API: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Error en respuesta HTTP activarMesa. Código: " + response.code() + ", Mensaje: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    //  Toast.makeText(MesaActivity.this, getString(R.string.server_error) + " (activar " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ActivarMesaResponse> call, Throwable t) {
                // Opcional: Ocultar ProgressBar aquí
                Log.e(TAG, "Fallo en llamada API activarMesa para mesa " + numeroMesa + ": " + t.getMessage(), t);
                Toast.makeText(MesaActivity.this, getString(R.string.error_red_activacion_mesa), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Metodo para iniciar la activity del camarero---
    private void iniciarCamareroActivity(int numeroMesa, int numComensales, int idMesaActiva, int idCamarero) {
        Intent intent = new Intent(MesaActivity.this, CamareroActivity.class);
        intent.putExtra("NUMERO_MESA", numeroMesa);
        intent.putExtra("NUM_COMENSALES", numComensales); // Será -1 si se reabre la mesa
        intent.putExtra(ID_MESA_ACTIVA_KEY, idMesaActiva);
        intent.putExtra(USER_ID_KEY, idCamarero);
        startActivity(intent);
    }

    // Es buena práctica refrescar los datos cuando la actividad vuelve a estar en primer plano,
    // por si el estado de las mesas ha cambiado por otras acciones (ej. otro camarero, o el jefe).
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != -1) {
            Log.d(TAG, "onResume: Recargando datos de mesas.");
            cargarDatosMesas();
        }
    }

}