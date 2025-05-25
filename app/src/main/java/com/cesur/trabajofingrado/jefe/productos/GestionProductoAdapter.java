package com.cesur.trabajofingrado.jefe.productos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.R;
import com.cesur.trabajofingrado.productos.Producto;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GestionProductoAdapter extends RecyclerView.Adapter<GestionProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private Context context;
    private OnProductoManagementClickListener productoManagementClickListener;

    public interface OnProductoManagementClickListener {
        void onEditProductoClick(Producto producto, int position); // Pasamos el producto y su posición
        void onDeleteProductoClick(Producto producto, int position); // Para el futuro botón de eliminar
    }


    public GestionProductoAdapter(Context context, List<Producto> listaProductos, OnProductoManagementClickListener listener) { // <<< MODIFICADO: Añadir listener al constructor
        this.context = context;
        this.listaProductos = listaProductos;
        this.productoManagementClickListener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_gestion_producto, parent, false);
        return new ProductoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto productoActual = listaProductos.get(position);
        holder.nombreProducto.setText(productoActual.getNombre());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        holder.precioProducto.setText(format.format(productoActual.getPrecio()));

        // --- Listener para Editar ---
        holder.btnEditar.setOnClickListener(v -> {
            if (productoManagementClickListener != null) {
                productoManagementClickListener.onEditProductoClick(productoActual, holder.getAdapterPosition()); // <<< LLAMAR AL LISTENER
            }
        });

        // --- Listener para Eliminar  ---
        holder.btnEliminar.setOnClickListener(v -> {
            if (productoManagementClickListener != null) {
                productoManagementClickListener.onDeleteProductoClick(productoActual, holder.getAdapterPosition()); // <<< LLAMAR AL LISTENER
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos == null ? 0 : listaProductos.size();
    }

    public void actualizarLista(List<Producto> nuevaLista) {
        this.listaProductos.clear();
        if (nuevaLista != null) {
            this.listaProductos.addAll(nuevaLista);
        }
        notifyDataSetChanged();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView nombreProducto;
        TextView precioProducto;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreProducto = itemView.findViewById(R.id.textViewNombreProductoGestion);
            precioProducto = itemView.findViewById(R.id.textViewPrecioProductoGestion);
            btnEditar = itemView.findViewById(R.id.buttonEditarProducto);
            btnEliminar = itemView.findViewById(R.id.buttonEliminarProducto);
        }
    }
}