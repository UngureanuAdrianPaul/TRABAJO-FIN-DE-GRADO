
package com.cesur.trabajofingrado.jefe.productos;

import android.content.Intent;
import android.os.Bundle;
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
import com.cesur.trabajofingrado.productos.GetProductosResponse;
import com.cesur.trabajofingrado.productos.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.textfield.TextInputEditText;
import android.text.TextUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionProductosActivity extends AppCompatActivity implements GestionProductoAdapter.OnProductoManagementClickListener {

    private static final String TAG = "GestionProductosAct";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAnadir;
    private GestionProductoAdapter adapter;
    private List<Producto> listaDeProductos;
    private ApiService apiService;
    private String categoriaActual;
    private int itemEditandoPosicion = -1;// "comida" o "bebida"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_productos);

        toolbar = findViewById(R.id.toolbarGestionProductos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Botón de atrás
        }

        recyclerView = findViewById(R.id.recyclerViewGestionProductos);
        fabAnadir = findViewById(R.id.fabAnadirProducto);

        apiService = ApiClient.getApiService();
        listaDeProductos = new ArrayList<>();

        adapter = new GestionProductoAdapter(this, listaDeProductos,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Obtener la categoría del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("categoria_producto")) {
            categoriaActual = intent.getStringExtra("categoria_producto");
            if (categoriaActual != null) {
                String titulo = "Gestionar " + (categoriaActual.equalsIgnoreCase("comida") ? "Comida" : "Bebidas");
                getSupportActionBar().setTitle(titulo);
                cargarProductos();
            } else {
                Log.e(TAG, "Categoría recibida es null.");
                //  Toast.makeText(this, "Error: Categoría no especificada.", Toast.LENGTH_LONG).show();
                finish(); // Salir si no hay categoría
            }
        } else {
            Log.e(TAG, "No se recibió la categoría del producto.");
            //   Toast.makeText(this, "Error: Categoría no especificada.", Toast.LENGTH_LONG).show();
            finish(); // Salir si no hay categoría
        }

        fabAnadir.setOnClickListener(v -> {
            mostrarDialogoAnadirProducto(); // Llamar al nuevo metodo
        });
    }

    // --- Metodo para mostrar el dialodo de añadir producto ---
    private void mostrarDialogoAnadirProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_anadir_producto, null);
        builder.setView(dialogView);

        final TextInputEditText etNombreProducto = dialogView.findViewById(R.id.editTextNombreProductoNuevo);
        final TextInputEditText etPrecioProducto = dialogView.findViewById(R.id.editTextPrecioProductoNuevo);
        final TextView tvTituloDialogo = dialogView.findViewById(R.id.textViewDialogAnadirProductoTitulo);

        String tipoProductoParaTitulo = categoriaActual.substring(0, 1).toUpperCase() + categoriaActual.substring(1);
        tvTituloDialogo.setText("Añadir Nueva " + tipoProductoParaTitulo);


        builder.setPositiveButton("Guardar", null); // Se configura el listener abajo para controlar el cierre
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        // Evitar que el diálogo se cierre si la validación falla
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String nombre = etNombreProducto.getText().toString().trim();
                String precioStr = etPrecioProducto.getText().toString().trim();

                // Validaciones
                if (TextUtils.isEmpty(nombre)) {
                    etNombreProducto.setError("El nombre es requerido.");
                    return;
                } else {
                    etNombreProducto.setError(null);
                }

                double precio = -1;
                if (TextUtils.isEmpty(precioStr)) {
                    etPrecioProducto.setError("El precio es requerido.");
                    return;
                } else {
                    try {
                        // Para asegurar que se parsea bien con coma o punto como decimal
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault()); // O Locale.GERMANY para coma
                        char GdecimalSeparator = symbols.getDecimalSeparator();
                        if(precioStr.contains(".") && GdecimalSeparator == ',') precioStr = precioStr.replace('.',',');
                        else if(precioStr.contains(",") && GdecimalSeparator == '.') precioStr = precioStr.replace(',','.');

                        DecimalFormat df = new DecimalFormat("#0.00", symbols); // Usar dos decimales
                        Number number = df.parse(precioStr);
                        precio = number.doubleValue();

                        if (precio <= 0) {
                            etPrecioProducto.setError("El precio debe ser mayor que cero.");
                            return;
                        }
                        etPrecioProducto.setError(null);
                    } catch (ParseException e) {
                        etPrecioProducto.setError("Formato de precio inválido (ej. 9,95 o 9.95).");
                        Log.e(TAG, "Error parseando precio: " + precioStr, e);
                        return;
                    }
                }

                // Si es válido, llamar a la API para añadir el producto
                Log.d(TAG, "Guardar producto: " + nombre + ", Precio: " + precio + ", Categoría: " + categoriaActual);
                anadirProductoALaAPI(nombre, precio, categoriaActual);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // --- Metodo para llamar a la API  ---
    private void anadirProductoALaAPI(String nombre, double precio, String categoria) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en anadirProductoALaAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PRODUCTO GUARDADO", Toast.LENGTH_SHORT).show();

        AnadirProductoRequest request = new AnadirProductoRequest(nombre, precio, categoria);
        Call<AnadirProductoResponse> call = apiService.anadirProducto(request);

        call.enqueue(new Callback<AnadirProductoResponse>() {
            @Override
            public void onResponse(Call<AnadirProductoResponse> call, Response<AnadirProductoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnadirProductoResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.i(TAG, "Producto añadido con éxito: " + apiResponse.getMessage() + ", ID: " + apiResponse.getProductoId());
                      // Toast.makeText(GestionProductosActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refrescar la lista de productos para mostrar el nuevo
                        cargarProductos();
                    } else {
                        // Error lógico devuelto por la API (ej. nombre duplicado, validación fallida en servidor)
                        Log.e(TAG, "Error de API al añadir producto: " + apiResponse.getMessage());
                        Toast.makeText(GestionProductosActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP (404, 500, etc.)
                    String errorBodyStr = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyStr = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo errorBody", e);
                    }
                    Log.e(TAG, "Error en respuesta HTTP al añadir producto. Código: " + response.code() + " Cuerpo: " + errorBodyStr);
                    //  Toast.makeText(GestionProductosActivity.this, "Error del servidor (" + response.code() + ") al añadir producto.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AnadirProductoResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API anadirProducto: " + t.getMessage(), t);
                Toast.makeText(GestionProductosActivity.this, "Error de red. No se pudo añadir el producto.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Metodo para mostrar los productos de la base de datos ---
    private void cargarProductos() {
        if (categoriaActual == null || categoriaActual.isEmpty()) {
            Log.e(TAG, "Categoría actual es nula o vacía, no se pueden cargar productos.");
            return;
        }

       // Toast.makeText(this, "Cargando " + categoriaActual + "...", Toast.LENGTH_SHORT).show();
        Call<GetProductosResponse> call = apiService.getProductos(categoriaActual); // Reutilizamos el endpoint existente

        call.enqueue(new Callback<GetProductosResponse>() {
            @Override
            public void onResponse(Call<GetProductosResponse> call, Response<GetProductosResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    List<Producto> productosRecibidos = response.body().getProductos();
                    if (productosRecibidos != null) {
                        Log.d(TAG, "Productos (" + categoriaActual + ") recibidos: " + productosRecibidos.size());
                        adapter.actualizarLista(productosRecibidos);
                    } else {
                        adapter.actualizarLista(new ArrayList<>()); // Lista vacía
                    //    Toast.makeText(GestionProductosActivity.this, "No hay productos en esta categoría.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Error al cargar productos";
                    if (response.body() != null && response.body().getMessage() != null) errorMsg = response.body().getMessage();
                    else if (!response.isSuccessful()) errorMsg = "Error HTTP: " + response.code();
                    Log.e(TAG, "Error API al cargar productos (" + categoriaActual + "): " + errorMsg);
                    // Toast.makeText(GestionProductosActivity.this, "Error API: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GetProductosResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red al cargar productos (" + categoriaActual + "): " + t.getMessage(), t);
                Toast.makeText(GestionProductosActivity.this, "Error de red. Verifica tu conexión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Para que el botón de atrás de la Toolbar funcione
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- Metodo de la interfaz ---
    @Override
    public void onEditProductoClick(Producto producto, int position) {
        Log.d(TAG, "Editar producto: " + producto.getNombre() + " en posición: " + position);
        this.itemEditandoPosicion = position; // Guardar la posición para posible actualización de UI después
        mostrarDialogoEditarProducto(producto); // Llamar al metodo que mostrará el diálogo de edición
    }

    // --- Metodo de la interfaz ---
    @Override
    public void onDeleteProductoClick(Producto producto, int position) {
        Log.d(TAG, "Solicitud para eliminar producto: " + producto.getNombre() + " en posición: " + position);

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar el producto '" + producto.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    // El usuario confirmó, llamar al metodo que contactará a la API
                    eliminarProductoEnAPI(producto.getId(), categoriaActual, position);
                })
                .setNegativeButton("No, Cancelar", (dialog, which) -> {
                    // El usuario canceló, no hacer nada
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de advertencia
                .show();
    }
    // --- FIN MÉTODOS DE LA INTERFAZ ---

    // --- Metodo para llamar a la API de eliminacion ---
    private void eliminarProductoEnAPI(int idProducto, String categoria, final int position) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en eliminarProductoEnAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

         Toast.makeText(this, "ELIMINANDO PRODUCTO", Toast.LENGTH_SHORT).show();

        EliminarProductoRequest request = new EliminarProductoRequest(idProducto, categoria);
        Call<GenericApiResponse> call = apiService.eliminarProducto(request);

        call.enqueue(new Callback<GenericApiResponse>() {
            @Override
            public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) { // O si quieres manejar el status "warning" del PHP
                        Log.i(TAG, "Respuesta de API eliminar producto: " + apiResponse.getMessage());
                        //  Toast.makeText(GestionProductosActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        cargarProductos();

                    } else {
                        // Error lógico devuelto por la API (ej. producto no encontrado, restricción FK)
                        Log.e(TAG, "Error de API al eliminar producto: " + apiResponse.getMessage());
                        Toast.makeText(GestionProductosActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP (404, 409 Conflict, 500, etc.)
                    // Aquí es donde necesitamos manejar el caso 409 específicamente si el cuerpo no es parseado como GenericApiResponse
                    String errorMessage = "Error del servidor (" + response.code() + ") al eliminar producto.";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string(); // Leer el cuerpo del error solo una vez
                            Log.e(TAG, "Error en respuesta HTTP al eliminar producto. Código: " + response.code() + " Cuerpo: " + errorBodyStr);

                            // Intentar parsear el errorBody como GenericApiResponse
                            // Esto es útil si el servidor envía un JSON de error incluso con códigos HTTP de error
                            if (errorBodyStr.startsWith("{")) { // Verificar si parece JSON
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                GenericApiResponse errorResponse = gson.fromJson(errorBodyStr, GenericApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    errorMessage = errorResponse.getMessage(); // Usar el mensaje del JSON de error
                                } else {
                                    // Si no se pudo parsear o no tiene mensaje, mantener el genérico o uno más específico para el código
                                    if(response.code() == 409) { // HTTP 409 Conflict
                                        errorMessage = "No se puede eliminar: el producto está en uso.";
                                    }
                                }
                            } else {
                                // Si errorBody no es JSON, pero hay un código 409, podemos asumir el mensaje
                                if(response.code() == 409) {
                                    errorMessage = "No se puede eliminar: el producto está en uso";
                                }
                                // Para otros errores, podrías mostrar parte del errorBodyStr si es texto simple,
                                // o un mensaje genérico para errores HTML no controlados.
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error leyendo o parseando errorBody al eliminar", e);
                        }
                    }
                    Toast.makeText(GestionProductosActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API eliminarProducto: " + t.getMessage(), t);
                Toast.makeText(GestionProductosActivity.this, "Error de red. No se pudo eliminar el producto.", Toast.LENGTH_LONG).show();
            }
        });
    }


    // --- Metodo para mostrar el dialogo de editar producto ---
    private void mostrarDialogoEditarProducto(final Producto productoAEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_anadir_producto, null);
        builder.setView(dialogView);

        final TextInputEditText etNombreProducto = dialogView.findViewById(R.id.editTextNombreProductoNuevo);
        final TextInputEditText etPrecioProducto = dialogView.findViewById(R.id.editTextPrecioProductoNuevo);
        final TextView tvTituloDialogo = dialogView.findViewById(R.id.textViewDialogAnadirProductoTitulo);

        tvTituloDialogo.setText("Editar " + productoAEditar.getNombre()); // Cambiar título

        // Pre-rellenar con los datos del producto
        etNombreProducto.setText(productoAEditar.getNombre());

        // Formatear el precio para mostrarlo correctamente
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault()); // Usar el locale adecuado
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        etPrecioProducto.setText(format.format(productoAEditar.getPrecio()));


        builder.setPositiveButton("Guardar Cambios", null); // Listener se configura después
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            itemEditandoPosicion = -1; // Resetear posición si se cancela
            dialog.dismiss();
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String nuevoNombre = etNombreProducto.getText().toString().trim();
                String nuevoPrecioStr = etPrecioProducto.getText().toString().trim();

                // Validaciones (similar a añadir, pero puedes permitir que el nombre no cambie)
                if (TextUtils.isEmpty(nuevoNombre)) {
                    etNombreProducto.setError("El nombre es requerido.");
                    return;
                } else {
                    etNombreProducto.setError(null);
                }

                double nuevoPrecio = -1;
                if (TextUtils.isEmpty(nuevoPrecioStr)) {
                    etPrecioProducto.setError("El precio es requerido.");
                    return;
                } else {
                    try {
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                        char GdecimalSeparator = symbols.getDecimalSeparator();
                        if(nuevoPrecioStr.contains(".") && GdecimalSeparator == ',') nuevoPrecioStr = nuevoPrecioStr.replace('.',',');
                        else if(nuevoPrecioStr.contains(",") && GdecimalSeparator == '.') nuevoPrecioStr = nuevoPrecioStr.replace(',','.');

                        DecimalFormat df = new DecimalFormat("#0.00", symbols);
                        Number number = df.parse(nuevoPrecioStr);
                        nuevoPrecio = number.doubleValue();

                        if (nuevoPrecio <= 0) {
                            etPrecioProducto.setError("El precio debe ser mayor que cero.");
                            return;
                        }
                        etPrecioProducto.setError(null);
                    } catch (ParseException e) {
                        etPrecioProducto.setError("Formato de precio inválido.");
                        Log.e(TAG, "Error parseando precio editado: " + nuevoPrecioStr, e);
                        return;
                    }
                }

                // Comprobar si realmente hubo cambios
                if (nuevoNombre.equals(productoAEditar.getNombre()) && nuevoPrecio == productoAEditar.getPrecio()) {
                    Toast.makeText(this, "No se realizaron cambios.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Guardar cambios para producto ID: " + productoAEditar.getId() +
                            ", Nuevo Nombre: " + nuevoNombre + ", Nuevo Precio: " + nuevoPrecio +
                            ", Categoría: " + categoriaActual);
                    // Llamar a la API para editar el producto
                    editarProductoEnAPI(productoAEditar.getId(), nuevoNombre, nuevoPrecio, categoriaActual);
                }
                dialog.dismiss();
            });
        });
        dialog.show();
    }


    // --- Metodo para llamar a la API de edición ---
    private void editarProductoEnAPI(int idProducto, String nombre, double precio, String categoria) {
        if (apiService == null) {
            Log.e(TAG, "ApiService no inicializado en editarProductoEnAPI");
            Toast.makeText(this, "Error: Servicio no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PRODUCTO ACTUALIZADO", Toast.LENGTH_SHORT).show();

        EditarProductoRequest request = new EditarProductoRequest(idProducto, nombre, precio, categoria);
        Call<GenericApiResponse> call = apiService.editarProducto(request);

        call.enqueue(new Callback<GenericApiResponse>() {
            @Override
            public void onResponse(Call<GenericApiResponse> call, Response<GenericApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.i(TAG, "Producto actualizado con éxito: " + apiResponse.getMessage());
                        //   Toast.makeText(GestionProductosActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refrescar la lista de productos para mostrar los cambios
                        cargarProductos();
                        itemEditandoPosicion = -1; // Resetear la posición después de la edición
                    } else {
                        // Error lógico devuelto por la API (ej. nombre duplicado, id no encontrado)
                        Log.e(TAG, "Error de API al editar producto: " + apiResponse.getMessage());
                        Toast.makeText(GestionProductosActivity.this, "Error API: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Error en la respuesta HTTP (404, 500, etc.)
                    String errorBodyStr = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyStr = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo errorBody al editar", e);
                    }
                    Log.e(TAG, "Error en respuesta HTTP al editar producto. Código: " + response.code() + " Cuerpo: " + errorBodyStr);
                    //   Toast.makeText(GestionProductosActivity.this, "Error del servidor (" + response.code() + ") al editar producto.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en llamada API editarProducto: " + t.getMessage(), t);
                Toast.makeText(GestionProductosActivity.this, "Error de red. No se pudo editar el producto.", Toast.LENGTH_LONG).show();
            }
        });
    }


}