package com.cesur.trabajofingrado;

import com.cesur.trabajofingrado.comanda.CrearComandaRequest;
import com.cesur.trabajofingrado.comanda.CrearComandaResponse;
import com.cesur.trabajofingrado.comanda.GetComandaItemsResponse;
import com.cesur.trabajofingrado.comanda.SincronizarItemsRequest;
import com.cesur.trabajofingrado.jefe.mesas.AnadirMesaRequest;
import com.cesur.trabajofingrado.jefe.mesas.AnadirMesaResponse;
import com.cesur.trabajofingrado.jefe.mesas.EditarMesaRequest;
import com.cesur.trabajofingrado.jefe.mesas.EliminarMesaFisicaRequest;
import com.cesur.trabajofingrado.jefe.productos.AnadirProductoRequest;
import com.cesur.trabajofingrado.jefe.productos.AnadirProductoResponse;
import com.cesur.trabajofingrado.jefe.productos.EditarProductoRequest;
import com.cesur.trabajofingrado.jefe.productos.EliminarProductoRequest;
import com.cesur.trabajofingrado.jefe.mesas.GetMesasFisicasResponse;
import com.cesur.trabajofingrado.login.LoginResponse;
import com.cesur.trabajofingrado.mesas.ActivarMesaRequest;
import com.cesur.trabajofingrado.mesas.ActivarMesaResponse;
import com.cesur.trabajofingrado.mesas.CerrarMesaRequest;
import com.cesur.trabajofingrado.mesas.GetMesasResponse;
import com.cesur.trabajofingrado.productos.GetProductosResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiService {

    // --- Endpoint para el Login ---
    @FormUrlEncoded
    // Para enviar datos como si fuera un formulario HTML (coincide con $_POST en PHP)
    @POST("tfg_api/login.php")
    Call<LoginResponse> login(
            @Field("usuario") String username,    // Nombre del parámetro que espera PHP
            @Field("contrasena") String password // Nombre del parámetro que espera PHP
    );

    // --- Endpoint para obtener mesas y nombre del camarero ---
    @GET("tfg_api/get_mesas.php")
    Call<GetMesasResponse> getMesas(@Query("userId") int userId);

    // --- Endpoint para activar una mesa  ---
    @POST("tfg_api/activar_mesa.php")
    Call<ActivarMesaResponse> activarMesa(@Body ActivarMesaRequest request);

    // --- Endpoint para obtener los productos de la base de datos en funcion de la categoria ---
    @GET("tfg_api/get_productos.php")
    Call<GetProductosResponse> getProductos(@Query("category") String category // Parámetro 'comida' o 'bebida'
    );

    // --- Endpoint para crear una comanda ---
    @POST("tfg_api/crear_comanda.php")

    Call<CrearComandaResponse> crearComanda(@Body CrearComandaRequest requestBody);

    // --- Endpoint para obtener items de una comanda activa ---
    @GET("tfg_api/get_comanda_items.php")
    Call<GetComandaItemsResponse> getComandaItems(@Query("id_mesa_activa") int idMesaActiva
    );

    // --- Endpoint para cerrar una mesa e imprimir comanda ---
    @POST("tfg_api/cerrar_mesa.php")
    Call<GenericApiResponse> cerrarMesa(@Body CerrarMesaRequest request);

    // --- Endpoint para sincronizar la comanda activa y enviada ---
    @POST("tfg_api/sincronizar_items_comanda.php")
    Call<GenericApiResponse> sincronizarItemsComanda(@Body SincronizarItemsRequest request);

    // --- Endpoint para añadir un nuevo producto ---
    @POST("tfg_api/anadir_producto.php")
    Call<AnadirProductoResponse> anadirProducto(@Body AnadirProductoRequest request);

    // --- Endpoint para editar un producto existente ---
    @POST("tfg_api/editar_producto.php")
    Call<GenericApiResponse> editarProducto(@Body EditarProductoRequest request);

    // --- Endpoint para eliminar un producto existente ---
    @POST("tfg_api/eliminar_producto.php")
    Call<GenericApiResponse> eliminarProducto(@Body EliminarProductoRequest request);

    // --- Endpoint para obtener la lista de mesas físicas (para el Jefe) ---
    @GET("tfg_api/get_mesas_fisicas.php")
    Call<GetMesasFisicasResponse> getMesasFisicas();

    // --- Endpoint para añadir una nueva mesa física ---
    @POST("tfg_api/anadir_mesa_fisica.php")
    Call<AnadirMesaResponse> anadirMesaFisica(@Body AnadirMesaRequest request);

    // --- Endpoint para editar la capacidad de una mesa física ---
    @POST("tfg_api/editar_mesa_fisica.php")
    Call<GenericApiResponse> editarMesaFisica(@Body EditarMesaRequest request);

    // --- Endpoint para eliminar una mesa física existente ---
    @POST("tfg_api/eliminar_mesa_fisica.php")
    Call<GenericApiResponse> eliminarMesaFisica(@Body EliminarMesaFisicaRequest request);

}
