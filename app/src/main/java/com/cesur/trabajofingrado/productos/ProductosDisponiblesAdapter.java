package com.cesur.trabajofingrado.productos;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cesur.trabajofingrado.R;


import java.util.List;
import java.util.Locale;

public class ProductosDisponiblesAdapter extends RecyclerView.Adapter<ProductosDisponiblesAdapter.ViewHolder> {

    private List<Producto> availableProducts;
    private Context context;
    private OnProductAvailableClickListener clickListener;
    private final String category;

    // Interfaz para notificar a la Activity/Dialog cuando se pulsa un producto
    public interface OnProductAvailableClickListener {
        void onProductAvailableClick(Producto producto, String category);
    }

    // --- Constructor  ---
    public ProductosDisponiblesAdapter(Context context, List<Producto> availableProducts, String category, OnProductAvailableClickListener listener) {
        this.context = context;
        this.availableProducts = availableProducts;
        this.category = category;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout item_product_selectable.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_productos_disponibles, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // position inicial
        if (position < 0 || position >= availableProducts.size()) {
            Log.e("AvailableProdAdapter", "Posición inválida en onBindViewHolder: " + position);
            return;
        }
        Producto currentProduct = availableProducts.get(position);

        holder.productNameTextView.setText(currentProduct.getNombre());
        String formattedPrice = String.format(Locale.GERMANY, "%.2f €", currentProduct.getPrecio());
        holder.productPriceTextView.setText(formattedPrice);

        // --- Listener  ---
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // Posición segura al momento del clic
            if (currentPosition != RecyclerView.NO_POSITION) {
                if (clickListener != null) {
                    // Pasamos el producto y la categoría guardada en el adapter
                    clickListener.onProductAvailableClick(availableProducts.get(currentPosition), category);
                }
            } else {
                Log.w("AvailableProdAdapter", "Clic en ViewHolder sin posición válida.");
            }
        });
        // -------------------------
    }

    @Override
    public int getItemCount() {
        return availableProducts == null ? 0 : availableProducts.size();
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView productPriceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textViewAvailableProductName);
            productPriceTextView = itemView.findViewById(R.id.textViewAvailableProductPrice);
        }
    }
}