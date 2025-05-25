package com.cesur.trabajofingrado.camarero;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.cesur.trabajofingrado.ApiService;
import com.cesur.trabajofingrado.ApiClient;
import com.cesur.trabajofingrado.GenericApiResponse;
import com.cesur.trabajofingrado.R;
import com.cesur.trabajofingrado.comanda.ComandaAdapter;
import com.cesur.trabajofingrado.comanda.ItemComandaTemporal;
import com.cesur.trabajofingrado.comanda.SincronizarItemsRequest;
import com.cesur.trabajofingrado.login.LoginActivity;
import com.cesur.trabajofingrado.productos.GetProductosResponse;
import com.cesur.trabajofingrado.productos.Producto;
import com.cesur.trabajofingrado.productos.ProductosDisponiblesAdapter;
import com.cesur.trabajofingrado.productos.ProductosSeleccionadosAdapter;
import com.cesur.trabajofingrado.comanda.GetComandaItemsResponse;
import com.cesur.trabajofingrado.mesas.MesaActivity;
import com.cesur.trabajofingrado.comanda.CrearComandaRequest;
import com.cesur.trabajofingrado.comanda.CrearComandaResponse;
import com.cesur.trabajofingrado.mesas.CerrarMesaRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CamareroActivity extends AppCompatActivity implements ProductosDisponiblesAdapter.OnProductAvailableClickListener, ProductosSeleccionadosAdapter.OnProductSelectedClickListener, ComandaAdapter.OnComandaItemClickListener {

    private static final String TAG = "CamareroActivity";
    private ImageButton dropDownMenuButton;
    private TextView tableInfoTextView;
    private ImageButton tableMenuButton;
    private ImageButton drinkFoodMenuButton;
    private RecyclerView productListRecyclerView;
    private ComandaAdapter comandaAdapter;
    private List<ItemComandaTemporal> itemsComandaActual;
    private ApiService apiService;
    private TextView finalTotalPriceTextView;
    private ImageButton deleteProductButton;
    private ImageButton sendOrderButton;

    private int numeroMesa = -1;
    private int numComensales = -1;
    private int idMesaActiva = -1;
    private int idCamarero = -1;
    private Integer comandaIdPrincipal = null;


    // Variable para saber qué item borrar en el diálogo de selección
    private int selectedItemPositionForDeletion = RecyclerView.NO_POSITION;
    // Referencia al adaptador derecho DENTRO del diálogo (para notificar cambios)
    private ProductosSeleccionadosAdapter selectedProductDialogAdapter = null;
    // Lista temporal de items seleccionados DENTRO del diálogo
    private List<ItemComandaTemporal> selectedItemsInDialog = new ArrayList<>();
    // Mapa para búsqueda rápida en la lista DENTRO del diálogo
    private Map<Integer, ItemComandaTemporal> selectedItemsDialogMap = new HashMap<>();
    // Botón borrar del layout principal
    private int selectedItemPositionInMainList = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camarero); // Establece el layout correcto

        // --- Inicialización de ApiService ---
        apiService = ApiClient.getApiService();

        // --- Inicialización de Vistas ---
        tableInfoTextView = findViewById(R.id.tableInfo);
        dropDownMenuButton = findViewById(R.id.dropDownMenuButton);
        tableMenuButton = findViewById(R.id.tableMenuButton);
        drinkFoodMenuButton = findViewById(R.id.drinkFoodMenuButton);
        productListRecyclerView = findViewById(R.id.productList);
        finalTotalPriceTextView = findViewById(R.id.finalTotalPrice);
        deleteProductButton = findViewById(R.id.deleteProductButton);
        sendOrderButton = findViewById(R.id.sendOrderButton);


        // --- Inicializar la lista temporal y el adaptador ---
        itemsComandaActual = new ArrayList<>(); // Crea la lista vacía
        comandaAdapter = new ComandaAdapter(this, itemsComandaActual, this);
        // --- Configurar el RecyclerView ---
        productListRecyclerView.setAdapter(comandaAdapter);
        // Un RecyclerView necesita un LayoutManager para saber cómo organizar los items
        productListRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Recibir datos del Intent
        Intent intent = getIntent();
        if (intent != null) {
            numeroMesa = intent.getIntExtra("NUMERO_MESA", -1);
            numComensales = intent.getIntExtra("NUM_COMENSALES", -1);
            // Leer el nuevo ID de la mesa activa
            idMesaActiva = intent.getIntExtra(MesaActivity.ID_MESA_ACTIVA_KEY, -1);
            idCamarero = intent.getIntExtra(MesaActivity.USER_ID_KEY, -1);
        }


        if (tableInfoTextView != null) {
            if (numeroMesa != -1 && idMesaActiva != -1 && idCamarero != -1) { // Validar todos los IDs
                tableInfoTextView.setText(getString(R.string.info_mesa_placeholder, numeroMesa));
                Log.d(TAG, "Mesa: " + numeroMesa + ", Comensales: " + numComensales + ", ID Mesa Activa: " + idMesaActiva + ", ID Camarero: " + idCamarero);
            } else {
                tableInfoTextView.setText("Error al recibir datos");
                Log.e(TAG, "Error: IDs incompletos. Mesa: " + numeroMesa + ", ID Mesa Activa: " + idMesaActiva + ", ID Camarero: " + idCamarero);
                if (sendOrderButton != null) sendOrderButton.setEnabled(false);
            }

            if (numeroMesa == -1 || idMesaActiva == -1 || idCamarero == -1) {
                // Si faltan IDs cruciales, mostrar error
                Toast.makeText(this, "Error: IDs de sesión no válidos.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCreate: IDs inválidos, no se cargarán items.");
            } else {
                // --- LLAMAR A CARGAR ITEMS SI LOS IDS SON VÁLIDOS ---
                cargarItemsComandaExistente();
            }
        } else {
            Log.e(TAG, "Error: TextView tableInfo no encontrado.");
        }

        // ---  Listener para el botón del menú desplegable ---
        if (dropDownMenuButton != null) {
            dropDownMenuButton.setOnClickListener(v -> mostrarMenuBurger());
        } else {
            Log.e(TAG, "ImageButton dropDownMenuButton no encontrado en el layout.");
        }

        // --- Listener para el botón Volver a Mesas ---
        if (tableMenuButton != null) {
            tableMenuButton.setOnClickListener(v -> {
                Log.d(TAG, "Botón tableMenuButton pulsado. Volviendo a MesaActivity.");
                // Simplemente cierra la Activity actual (CamareroActivity)
                finish();
            });
        } else {
            Log.e(TAG, "ImageButton tableMenuButton no encontrado en el layout.");
        }

        // --- Listener para el botón de menú Comida/Bebida ---
        if (drinkFoodMenuButton != null) {
            drinkFoodMenuButton.setOnClickListener(v -> mostrarDialogoCategoria());
        } else {
            Log.e(TAG, "ImageButton drinkFoodMenuButton no encontrado en el layout.");
        }

        // --- Listener para el botón de borrar producto ---
        if (deleteProductButton != null) {
            deleteProductButton.setEnabled(false); // Empieza deshabilitado
            deleteProductButton.setOnClickListener(v -> {
                // Comprobar si hay una posición válida seleccionada
                if (selectedItemPositionInMainList != RecyclerView.NO_POSITION) {
                    if (selectedItemPositionInMainList < itemsComandaActual.size()) { // Comprobar límites
                        Log.d(TAG, "Borrando item de comanda principal en posición: " + selectedItemPositionInMainList);
                        // Eliminar el item de la lista de datos
                        itemsComandaActual.remove(selectedItemPositionInMainList);
                        // Notificar al adaptador sobre la eliminación
                        comandaAdapter.notifyItemRemoved(selectedItemPositionInMainList);
                        // Notificar cambio en items siguientes (para actualizar posiciones internas)
                        if (selectedItemPositionInMainList < itemsComandaActual.size()) {
                            comandaAdapter.notifyItemRangeChanged(selectedItemPositionInMainList, itemsComandaActual.size() - selectedItemPositionInMainList);
                        }

                        // Resetear selección y deshabilitar botón
                        selectedItemPositionInMainList = RecyclerView.NO_POSITION;
                        deleteProductButton.setEnabled(false); // Deshabilitar tras borrar

                        // Recalcular y mostrar el precio total
                        calcularYMostrarPrecioFinal();
                    } else {
                        Log.e(TAG, "Error: Posición seleccionada (" + selectedItemPositionInMainList + ") fuera de límites (" + itemsComandaActual.size() + ")");
                        selectedItemPositionInMainList = RecyclerView.NO_POSITION; // Resetear
                        deleteProductButton.setEnabled(false);
                    }
                } else {
                    Log.w(TAG, "Botón Borrar pulsado pero no hay item seleccionado.");
                }
            });
        } else {
            Log.e(TAG, "ImageButton deleteProductButton no encontrado.");
        }

        setupDeleteButtonListener();

        // --- Listener para el botón Enviar Comanda ---

        if (sendOrderButton != null) {
            sendOrderButton.setOnClickListener(v -> enviarComanda());
        } else {
            Log.e(TAG, "ImageButton sendOrderButton no encontrado en el layout.");
        }

        calcularYMostrarPrecioFinal();


    } //------------------------FIN ON CREATE---------------------------------

    // --- Metodo para cargar los productos de una comanda que ya se ha enviado ---
    private void cargarItemsComandaExistente() {
        if (idMesaActiva == -1) {
            Log.e(TAG, "No se puede cargar items, idMesaActiva es -1");
            return;
        }

        Log.d(TAG, "Cargando items para idMesaActiva: " + idMesaActiva);
        //Toast.makeText(this, "Cargando comanda existente...", Toast.LENGTH_SHORT).show();

        Call<GetComandaItemsResponse> call = apiService.getComandaItems(idMesaActiva);
        call.enqueue(new Callback<GetComandaItemsResponse>() {
            @Override
            public void onResponse(Call<GetComandaItemsResponse> call, Response<GetComandaItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    comandaIdPrincipal = response.body().getComandaId();
                    Log.d(TAG, "Comanda ID principal para esta sesión: " + comandaIdPrincipal);
                    List<ItemComandaTemporal> itemsRecibidos = response.body().getItems();
                    if (itemsRecibidos != null && !itemsRecibidos.isEmpty()) {
                        Log.d(TAG, "Items recibidos: " + itemsRecibidos.size());
                        // Marcar todos los items recibidos como ya enviados
                        for (ItemComandaTemporal item : itemsRecibidos) {
                            item.setSentToKitchen(true);
                        }
                        // Actualizar la lista principal y notificar al adaptador
                        itemsComandaActual.clear(); // Limpiar por si acaso
                        itemsComandaActual.addAll(itemsRecibidos);
                        comandaAdapter.notifyDataSetChanged();
                        calcularYMostrarPrecioFinal(); // Recalcular total
                    } else {
                        Log.d(TAG, "No se encontraron items existentes para esta mesa activa.");
                        // La lista itemsComandaActual ya está inicializada vacía, no hacer nada.
                    }
                } else {
                    // Manejar error de API o respuesta no exitosa
                    String errorMsg = "Error al cargar items";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMsg = "Error HTTP: " + response.code();
                        // Loguear error body si existe
                        try {
                            if (response.errorBody() != null)
                                Log.e(TAG, "Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                        }
                    }
                    Log.e(TAG, "Error al cargar items comanda: " + errorMsg);
                    //   Toast.makeText(CamareroActivity.this, "Error cargando comanda: " + errorMsg, Toast.LENGTH_LONG).show();
                    // Decidir qué hacer si falla la carga
                    itemsComandaActual.clear();
                    comandaAdapter.notifyDataSetChanged();
                    calcularYMostrarPrecioFinal();
                }
            }

            @Override
            public void onFailure(Call<GetComandaItemsResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar items comanda: " + t.getMessage(), t);
                //  Toast.makeText(CamareroActivity.this, "Error de red al cargar comanda.", Toast.LENGTH_LONG).show();
                // Decidir qué hacer si falla la carga
                itemsComandaActual.clear();
                comandaAdapter.notifyDataSetChanged();
                calcularYMostrarPrecioFinal();
            }
        });
    }

    // --- Metodo para mostrar el burger con botones ---
    private void mostrarMenuBurger() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.burger_camarero, null);

        // Establecer la vista personalizada en el AlertDialog
        builder.setView(dialogView);

        // Crear el AlertDialog para poder cerrarlo después
        final AlertDialog menuDialog = builder.create();

        // Encontrar los botones dentro del dialogView
        Button closeSessionButton = dialogView.findViewById(R.id.closeWaiterSessionButton);
        Button printBillButton = dialogView.findViewById(R.id.printBillButton);

        // Configurar acción para "Cerrar Sesión"
        if (closeSessionButton != null) {
            closeSessionButton.setOnClickListener(v_button -> {
                Log.d(TAG, "Botón Cerrar Sesión (AlertDialog) pulsado.");
                menuDialog.dismiss(); // Cerrar el AlertDialog

                LoginActivity.clearSession(CamareroActivity.this);

                // Crear Intent para volver a LoginActivity
                Intent intent = new Intent(CamareroActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Cierra CamareroActivity también
            });
        } else {
            Log.e(TAG, "Botón closeWaiterSessionButton no encontrado en burger_camarero.xml");
        }

        // Configurar acción placeholder para "Imprimir Comanda"
        if (printBillButton != null) {
            printBillButton.setOnClickListener(v_button -> {
                Log.d(TAG, "Botón Imprimir Comanda/Cerrar Mesa pulsado.");
                menuDialog.dismiss(); // Cerrar el menú de opciones primero

                if (idMesaActiva == -1) {
                    Toast.makeText(CamareroActivity.this, "No hay una sesión de mesa activa.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Si no hay items y no hay comandaIdPrincipal, no tiene sentido "imprimir" ni cerrar una mesa vacía
                // que nunca tuvo un pedido. Pero si ya tuvo un pedido (comandaIdPrincipal != null) aunque ahora
                // esté vacía porque se borró entero, si se puede "cerrar"
                if (itemsComandaActual.isEmpty() && comandaIdPrincipal == null) {
                    Toast.makeText(CamareroActivity.this, "COMANDA VACÍA", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. Preparar los datos para la "cuenta" simulada
                final String cuentaSimulada = generarTextoCuenta();

                // 2. Mostrar la cuenta simulada en un AlertDialog
                new AlertDialog.Builder(CamareroActivity.this)
                        .setTitle("CUENTA MESA " + numeroMesa)
                        .setMessage(cuentaSimulada) // Mostramos el texto de la cuenta
                        .setPositiveButton("Aceptar y Cerrar Mesa", (dialogInterface, which) -> {
                            // 3. Llamar a la API para cerrar la mesa en el servidor
                            cerrarMesaEnServidor();
                        })
                        .setNegativeButton("Cancelar", (dialogInterface, which) -> {
                            // El usuario canceló, no hacemos nada más
                            Log.d(TAG, "Cierre de mesa cancelado por el usuario.");
                        })
                        .setCancelable(false) // Evitar que se cierre tocando fuera
                        .show();
            });
        } else {
            Log.e(TAG, "Botón printBillButton no encontrado en el layout burger_camarero.xml");
        }

        // Mostrar el AlertDialog
        menuDialog.show();
    }

    // --- Metodo para llamar a la API y cerrar la mesa ---
    private void cerrarMesaEnServidor() {
        if (idMesaActiva == -1) {
            Log.e(TAG, "Intento de cerrar mesa sin idMesaActiva válido.");
            Toast.makeText(this, "Error interno: No se puede cerrar la mesa.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toast.makeText(this, "Cerrando mesa " + numeroMesa + " y comanda...", Toast.LENGTH_SHORT).show();

        // Crear el request con idMesaActiva y comandaIdPrincipal (puede ser null)
        CerrarMesaRequest request = new CerrarMesaRequest(idMesaActiva, comandaIdPrincipal);
        Call<GenericApiResponse> call = apiService.cerrarMesa(request);

        call.enqueue(new Callback<GenericApiResponse>() {
            @Override
            public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        ("success".equalsIgnoreCase(response.body().getStatus()) || "warning".equalsIgnoreCase(response.body().getStatus()))) { // Aceptar 'success' o 'warning'

                    String successMessage = response.body().getMessage();
                    Log.i(TAG, "Respuesta del servidor al cerrar mesa: " + successMessage);
                    //Toast.makeText(CamareroActivity.this, successMessage, Toast.LENGTH_LONG).show();

                    // Limpiar la lista de la comanda actual en la UI
                    itemsComandaActual.clear();
                    comandaAdapter.notifyDataSetChanged();
                    calcularYMostrarPrecioFinal(); // Reset total a 0

                    // Resetear el comandaIdPrincipal para esta actividad
                    comandaIdPrincipal = null;

                    // Cerrar CamareroActivity para volver a MesaActivity
                    finish();

                } else {
                    String errorMsg = "Error al cerrar mesa";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (!response.isSuccessful()) {
                        errorMsg = "Error HTTP: " + response.code();
                        try {
                            if (response.errorBody() != null)
                                Log.e(TAG, "CerrarMesa Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                        }
                    }
                    Log.e(TAG, "Error API al cerrar mesa: " + errorMsg);
                    //  Toast.makeText(CamareroActivity.this, "API Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cerrar mesa: " + t.getMessage(), t);
                //  Toast.makeText(CamareroActivity.this, "Error de red al cerrar la mesa.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Implementación del listener de ComandaAdapter ---
    @Override
    public void onComandaItemSelected(int position) {
        this.selectedItemPositionInMainList = position;
        // Habilitar o deshabilitar el botón borrar según si hay algo seleccionado
        if (deleteProductButton != null) {
            deleteProductButton.setEnabled(position != RecyclerView.NO_POSITION);
        }
        Log.d(TAG, "Item seleccionado/deseleccionado en lista principal, nueva posición guardada: " + position);
    }

    // --- Metodo para generar el texto de la cuenta ---
    private String generarTextoCuenta() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------\n");
        sb.append("      RESTAURANTE TFG\n");
        sb.append("-----------------------------------\n");
        sb.append("Mesa: ").append(numeroMesa).append("\n");
        if (numComensales != -1 && numComensales > 0) { // numComensales puede ser -1 si se reabrió
            sb.append("Comensales: ").append(numComensales).append("\n");
        }
        sb.append("Fecha: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new java.util.Date())).append("\n");
        sb.append("-----------------------------------\n\n");
        sb.append("DETALLE:\n");

        if (itemsComandaActual == null || itemsComandaActual.isEmpty()) {
            sb.append("No hay consumiciones registradas.\n");
        } else {
            for (ItemComandaTemporal item : itemsComandaActual) {
                // Formatear cada línea para que se vea bien alineada (esto es un ejemplo simple)
                String lineaProducto = String.format(Locale.GERMAN, "%-20s x%d", item.getProductName(), item.getQuantity());
                String precioLinea = String.format(Locale.GERMAN, "%.2f €", item.getTotalPrice());
                sb.append(String.format("%-28s %8s\n", lineaProducto, precioLinea));
            }
        }
        sb.append("\n-----------------------------------\n");

        // Recalcular el total para asegurar que es el correcto
        double totalDouble = 0.0;
        if (itemsComandaActual != null) {
            for (ItemComandaTemporal item : itemsComandaActual) {
                totalDouble += item.getTotalPrice();
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        String totalFormateado = currencyFormat.format(totalDouble);

        sb.append(String.format(Locale.GERMAN, "TOTAL A PAGAR: %15s\n", totalFormateado));
        sb.append("-----------------------------------\n");
        sb.append("      ¡Gracias por su visita!\n");
        sb.append("-----------------------------------\n");

        return sb.toString();
    }

    // --- Metodo para mostrar el diálogo de selección de categoría ---
    private void mostrarDialogoCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflar la vista personalizada
        View dialogView = inflater.inflate(R.layout.popup_botones_menu, null);
        builder.setView(dialogView);

        // Crear el diálogo para poder acceder a sus botones y cerrarlo
        final AlertDialog dialog = builder.create();

        // Encontrar los botones DENTRO de la vista del diálogo
        Button drinkButton = dialogView.findViewById(R.id.drinkMenuButton);
        Button foodButton = dialogView.findViewById(R.id.foodMenuButton);

        // --- Configurar acciones para los botones del diálogo ---
        drinkButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón Bebida pulsado en diálogo.");

            buscarYMostrarDialogoProductos("bebida");
            dialog.dismiss(); // Cerrar el diálogo
        });

        foodButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón Comida pulsado en diálogo.");
            buscarYMostrarDialogoProductos("comida"); // Llama a cargar y mostrar diálogo de productos
            dialog.dismiss(); // Cerrar el diálogo
        });

        // Mostrar el diálogo
        dialog.show();
    }

    // --- Metodo para llamar a la API y preparar el diálogo de selección ---
    private void buscarYMostrarDialogoProductos(final String category) {

        Call<GetProductosResponse> call = apiService.getProductos(category);
        call.enqueue(new Callback<GetProductosResponse>() {
            @Override
            public void onResponse(Call<GetProductosResponse> call, Response<GetProductosResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    GetProductosResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        List<Producto> availableProducts = apiResponse.getProductos();
                        if (availableProducts != null && !availableProducts.isEmpty()) {
                            Log.d(TAG, "Productos (" + category + ") recibidos: " + availableProducts.size());

                            // Llamar al metodo que mostrará el diálogo con las dos listas
                            mostrarDialogoSeleccionProductos(category, availableProducts);
                        } else {
                            Log.w(TAG, "La API devolvió éxito pero la lista de productos (" + category + ") está vacía o es nula.");
                            Toast.makeText(CamareroActivity.this, "No hay productos disponibles en esta categoría.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Error desconocido de la API";
                        Log.e(TAG, "Error de API al cargar productos (" + category + "): " + errorMsg);
                        //   Toast.makeText(CamareroActivity.this, "Error API: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Error en respuesta HTTP productos (" + category + "). Código: " + response.code());
                    //  Toast.makeText(CamareroActivity.this, "Error del servidor al cargar productos (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GetProductosResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API productos (" + category + "): " + t.getMessage(), t);
                //  Toast.makeText(CamareroActivity.this, "Error de red al cargar productos. Verifica tu conexión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Metodo para mostrar los productos de una categoría específica ---
    private void mostrarDialogoSeleccionProductos(final String category, List<Producto> availableProducts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_seleccionar_productos, null);
        builder.setView(dialogView);

        // --- Reinicializar listas y selección para este diálogo ---
        selectedItemsInDialog.clear();
        selectedItemsDialogMap.clear();
        selectedItemPositionForDeletion = RecyclerView.NO_POSITION;


        // --- Vistas del diálogo ---
        TextView dialogTitle = dialogView.findViewById(R.id.dialogProductTitle);
        RecyclerView availableRecyclerView = dialogView.findViewById(R.id.recyclerViewAvailableProducts);
        RecyclerView selectedRecyclerView = dialogView.findViewById(R.id.recyclerViewSelectedProducts);
        ImageButton deleteButton = dialogView.findViewById(R.id.buttonDeleteSelectedItem);
        Button cancelButton = dialogView.findViewById(R.id.buttonCancelSelection);
        Button acceptButton = dialogView.findViewById(R.id.buttonAcceptSelection);

        dialogTitle.setText("Seleccionar " + (category.equals("comida") ? "Comida" : "Bebida"));

        // --- Configurar Adaptador Derecho (Seleccionados) ---
        // Creamos el adaptador y lo guardamos en la variable miembro para notificarle cambios
        selectedProductDialogAdapter = new ProductosSeleccionadosAdapter(this, selectedItemsInDialog, deleteButton, this);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedRecyclerView.setAdapter(selectedProductDialogAdapter);

        // --- Configurar Adaptador Izquierdo (Disponibles) ---
        // Le pasamos 'this' porque CamareroActivity implementa OnProductAvailableClickListener
        ProductosDisponiblesAdapter availableAdapter = new ProductosDisponiblesAdapter(this, availableProducts, category, this);
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        availableRecyclerView.setAdapter(availableAdapter);


        deleteButton.setOnClickListener(v -> {
            // La lógica de borrado usa la variable miembro 'selectedItemPositionForDeletion'
            if (selectedItemPositionForDeletion != RecyclerView.NO_POSITION) {
                if (selectedItemPositionForDeletion < selectedItemsInDialog.size()) {
                    Log.d(TAG, "Borrando item en posición: " + selectedItemPositionForDeletion);
                    ItemComandaTemporal itemToRemove = selectedItemsInDialog.get(selectedItemPositionForDeletion);
                    selectedItemsDialogMap.remove(itemToRemove.getProductId());
                    selectedItemsInDialog.remove(selectedItemPositionForDeletion);

                    selectedProductDialogAdapter.clearSelection(); // Limpiar selección interna del adapter Y DESHABILITA BOTÓN
                    selectedProductDialogAdapter.notifyItemRemoved(selectedItemPositionForDeletion);
                    if (selectedItemPositionForDeletion < selectedItemsInDialog.size()) {
                        selectedProductDialogAdapter.notifyItemRangeChanged(selectedItemPositionForDeletion, selectedItemsInDialog.size() - selectedItemPositionForDeletion);
                    }
                    selectedItemPositionForDeletion = RecyclerView.NO_POSITION; // Resetear
                } else {
                    Log.e(TAG, "Posición para borrar inválida: " + selectedItemPositionForDeletion);
                    selectedItemPositionForDeletion = RecyclerView.NO_POSITION;
                }
            } else {
                Log.w(TAG, "Botón borrar pulsado pero no había nada seleccionado.");
            }

        });

        final AlertDialog dialog = builder.create();
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        acceptButton.setOnClickListener(v -> {
            anadirElementosAComanda(selectedItemsInDialog);
            dialog.dismiss();
        });

        Log.d(TAG, "Mostrando diálogo de selección para: " + category);
        dialog.show();
    }

    // --- Implementación del listener de ProductosDisponiblesAdapter ---
    @Override
    public void onProductAvailableClick(Producto producto, String category) { // Recibe la categoría
        Log.d(TAG, "Producto disponible pulsado: " + producto.getNombre() + " (Categoría: " + category + ")");
        ItemComandaTemporal existingItem = selectedItemsDialogMap.get(producto.getId());

        if (existingItem != null) {
            existingItem.incrementQuantity();
            if (selectedProductDialogAdapter != null) {
                int existingItemIndex = selectedItemsInDialog.indexOf(existingItem);
                if (existingItemIndex != -1) {
                    selectedProductDialogAdapter.notifyItemChanged(existingItemIndex);
                } else {
                    selectedProductDialogAdapter.notifyDataSetChanged();
                }
            }
        } else {
            // Ahora usamos la 'category' recibida como parámetro
            ItemComandaTemporal newItem = new ItemComandaTemporal(producto.getId(), producto.getNombre(), category, // <--- Usamos el parámetro recibido
                    1, producto.getPrecio());
            selectedItemsInDialog.add(newItem);
            selectedItemsDialogMap.put(producto.getId(), newItem);
            if (selectedProductDialogAdapter != null) {
                selectedProductDialogAdapter.notifyItemInserted(selectedItemsInDialog.size() - 1);
            }
        }
        // Deseleccionar item en la lista derecha
        if (selectedProductDialogAdapter != null) {
            selectedProductDialogAdapter.clearSelection();
        }
        selectedItemPositionForDeletion = RecyclerView.NO_POSITION;
        // Deshabilitar botón borrar (necesitaríamos la referencia al botón aquí)
        // Como alternativa, se deshabilitará la próxima vez que se seleccione algo o al borrar.
    }

    // --- Implementación del listener de ProductosSeleccionadosAdapter ---
    @Override
    public void onProductSelectedClick(int position) {
        this.selectedItemPositionForDeletion = position;

    }

    // --- Metodo para añadir items a la comanda principal ---
    private void anadirElementosAComanda(List<ItemComandaTemporal> itemsToAdd) {

        if (itemsToAdd == null || itemsToAdd.isEmpty()) {
            Log.d(TAG, "No se añadieron items nuevos (lista vacía o nula).");
            return;
        }

        Map<Integer, ItemComandaTemporal> currentOrderMap = new HashMap<>();
        for (ItemComandaTemporal item : itemsComandaActual) {
            currentOrderMap.put(item.getProductId(), item);
        }

        for (ItemComandaTemporal newItem : itemsToAdd) {

            ItemComandaTemporal existingItem = currentOrderMap.get(newItem.getProductId());
            if (existingItem != null && existingItem.getProductType().equals(newItem.getProductType())) { // Comprobar también tipo por si IDs coincidieran entre comida/bebida
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                Log.d(TAG, "Cantidad actualizada para: " + existingItem.getProductName());
            } else {
                itemsComandaActual.add(newItem);
                Log.d(TAG, "Item nuevo añadido: " + newItem.getProductName());
            }
        }

        comandaAdapter.notifyDataSetChanged();
        calcularYMostrarPrecioFinal();
        Log.d(TAG, itemsToAdd.size() + " items procesados para añadir/actualizar en la comanda actual.");
    }

    // --- Metodo para cargar items existentes ---
    private void setupDeleteButtonListener() {
        if (deleteProductButton != null) {
            deleteProductButton.setEnabled(false); // Empieza deshabilitado
            deleteProductButton.setOnClickListener(v -> {
                if (selectedItemPositionInMainList != RecyclerView.NO_POSITION) {
                    if (selectedItemPositionInMainList < itemsComandaActual.size()) {

                        Log.d(TAG, "Borrando localmente item de comanda principal en posición: " + selectedItemPositionInMainList);
                        itemsComandaActual.remove(selectedItemPositionInMainList);
                        comandaAdapter.notifyItemRemoved(selectedItemPositionInMainList);
                        if (selectedItemPositionInMainList < itemsComandaActual.size()) { // Ajustar índices para las siguientes notificaciones
                            comandaAdapter.notifyItemRangeChanged(selectedItemPositionInMainList, itemsComandaActual.size() - selectedItemPositionInMainList);
                        }

                        selectedItemPositionInMainList = RecyclerView.NO_POSITION; // Resetear selección
                        deleteProductButton.setEnabled(false); // Deshabilitar tras borrar
                        calcularYMostrarPrecioFinal(); // Recalcular total
                    } else {
                        Log.e(TAG, "Error: Posición seleccionada (" + selectedItemPositionInMainList + ") fuera de límites (" + itemsComandaActual.size() + ")");
                        selectedItemPositionInMainList = RecyclerView.NO_POSITION;
                        deleteProductButton.setEnabled(false);
                    }
                } else {
                    Log.w(TAG, "Botón Borrar pulsado pero no hay item seleccionado.");
                }
            });
        } else {
            Log.e(TAG, "ImageButton deleteProductButton no encontrado.");
        }
    }

    // --- Metodo para calcular y mostrar el total  ---
    private void calcularYMostrarPrecioFinal() {
        double totalPrice = 0.0;
        for (ItemComandaTemporal item : itemsComandaActual) {
            totalPrice += item.getTotalPrice();
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        if (finalTotalPriceTextView != null) {
            finalTotalPriceTextView.setText(format.format(totalPrice));
        } else {
            Log.e(TAG, "TextView finalTotalPrice no encontrado.");
        }
    }

    // --- Metodo para enviar comandas a la BD ---
    private void enviarComanda() {

        if (itemsComandaActual == null) {
            itemsComandaActual = new ArrayList<>(); // Seguridad, aunque ya se inicializa en onCreate
        }
        // Toast.makeText(this, "Sincronizando comanda...", Toast.LENGTH_SHORT).show();

        if (comandaIdPrincipal == null) {
            // --- PRIMER ENVÍO: Crear nueva comanda ---
            Log.d(TAG, "Enviando comanda por primera vez para idMesaActiva: " + idMesaActiva);
            CrearComandaRequest requestBody = new CrearComandaRequest(idCamarero, idMesaActiva, itemsComandaActual);
            Call<CrearComandaResponse> callCrear = apiService.crearComanda(requestBody);
            callCrear.enqueue(new Callback<CrearComandaResponse>() {
                @Override
                public void onResponse(Call<CrearComandaResponse> call, Response<CrearComandaResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null && "success".equals(resp.body().getStatus())) {
                        comandaIdPrincipal = resp.body().getComandaId(); // Guardar el ID de la nueva comanda
                        Log.i(TAG, "Comanda creada con éxito. ID Comanda: " + comandaIdPrincipal);
                        Toast.makeText(CamareroActivity.this, "COMANDA ENVIADA", Toast.LENGTH_LONG).show();
                        marcarTodosComoEnviadosYActualizarUI();
                    } else {
                        String errorMsg = (resp.body() != null) ? resp.body().getMessage() : "Error creando comanda";
                        Log.e(TAG, "Error API crear comanda: " + errorMsg);
                        //  Toast.makeText(CamareroActivity.this, "Error API: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CrearComandaResponse> call, Throwable t) {
                    Log.e(TAG, "Fallo en llamada API crearComanda: " + t.getMessage(), t);
                    // Toast.makeText(CamareroActivity.this, "Error de red al crear comanda.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // --- ENVÍOS SUBSECUENTES: Sincronizar/Actualizar items de la comanda existente ---
            Log.d(TAG, "Actualizando items para comandaIdPrincipal: " + comandaIdPrincipal);
            SincronizarItemsRequest requestBody = new SincronizarItemsRequest(comandaIdPrincipal, itemsComandaActual);
            Call<GenericApiResponse> callSincro = apiService.sincronizarItemsComanda(requestBody);
            callSincro.enqueue(new Callback<GenericApiResponse>() {
                @Override
                public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                        Log.i(TAG, "Comanda sincronizada con éxito para ID Comanda: " + comandaIdPrincipal);
                        Toast.makeText(CamareroActivity.this, "COMANDA ACTUALIZADA.", Toast.LENGTH_LONG).show();
                        marcarTodosComoEnviadosYActualizarUI();
                    } else {
                        String errorMsg = (resp.body() != null) ? resp.body().getMessage() : "Error sincronizando comanda";
                        Log.e(TAG, "Error API sincronizar comanda: " + errorMsg);
                        //   Toast.makeText(CamareroActivity.this, "Error API: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                    Log.e(TAG, "Fallo en llamada API sincronizarItemsComanda: " + t.getMessage(), t);
                    //  Toast.makeText(CamareroActivity.this, "Error de red al sincronizar comanda.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // Metodo auxiliar para evitar duplicar código
    private void marcarTodosComoEnviadosYActualizarUI() {
        if (itemsComandaActual != null) {
            for (ItemComandaTemporal item : itemsComandaActual) {
                item.setSentToKitchen(true);
            }
        }
        comandaAdapter.notifyDataSetChanged();
        // Resetear selección del botón borrar, si aplica
        if (deleteProductButton != null && selectedItemPositionInMainList != RecyclerView.NO_POSITION) {
            comandaAdapter.clearSelection();
            selectedItemPositionInMainList = RecyclerView.NO_POSITION;
            deleteProductButton.setEnabled(false);
        }
        if (sendOrderButton != null) {
            sendOrderButton.setEnabled(true); // Mantener habilitado para más cambios
        }
    }


}