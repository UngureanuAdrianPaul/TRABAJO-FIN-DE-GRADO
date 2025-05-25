package com.cesur.trabajofingrado.productos;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.R;
import com.cesur.trabajofingrado.comanda.ItemComandaTemporal;
import java.util.List;

public class ProductosSeleccionadosAdapter extends RecyclerView.Adapter<ProductosSeleccionadosAdapter.ViewHolder> {

    private List<ItemComandaTemporal> selectedItems;
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION; // Para saber qué item está seleccionado para borrar
    private OnProductSelectedClickListener clickListener; // Listener para comunicar la selección
    private ImageButton deleteButtonRef;

    // Interfaz para notificar a la Activity/Dialog cuando se selecciona un item
    public interface OnProductSelectedClickListener {
        void onProductSelectedClick(int position); // Pasamos la posición seleccionada
    }


    public ProductosSeleccionadosAdapter(Context context, List<ItemComandaTemporal> selectedItems, ImageButton deleteButton, OnProductSelectedClickListener listener) {
        this.context = context;
        this.selectedItems = selectedItems;
        this.deleteButtonRef = deleteButton;
        this.clickListener = listener;

        // Asegurarse que el botón empieza deshabilitado
        if (this.deleteButtonRef != null) {
            this.deleteButtonRef.setEnabled(false);
        }
    }

    // Metodo para actualizar los datos desde fuera (cuando se añaden/borran items)
    public void setSelectedItems(List<ItemComandaTemporal> newSelectedItems) {
        this.selectedItems = newSelectedItems;
        notifyDataSetChanged();
    }

    // Metodo para obtener la posición seleccionada (para el botón Borrar)
    public int getSelectedPosition() {
        return selectedPosition;
    }

    // Metodo para deseleccionar (MODIFICADO para usar deleteButtonRef)
    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        // Deshabilitar botón borrar al limpiar seleccion
        if (deleteButtonRef != null) {
            deleteButtonRef.setEnabled(false);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout item_product_selected.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_productos_seleccionados, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // 'position' aquí es solo para el bind inicial
        // Obtener el item actual usando la posición inicial (esto es seguro aquí)
        // Asegurémonos de que la posición es válida antes de acceder a la lista
        if (position < 0 || position >= selectedItems.size()) {
            // Posición inválida, no hacer nada o loguear error
            Log.e("SelectedProductAdapter", "Posición inválida en onBindViewHolder: " + position);
            return;
        }
        ItemComandaTemporal currentItem = selectedItems.get(position);

        // Asignar datos a las vistas
        holder.productNameTextView.setText(currentItem.getProductName());
        holder.quantityTextView.setText("x" + currentItem.getQuantity());

        // Resaltar si está seleccionado (usa la variable miembro 'selectedPosition')
        if (holder.getAdapterPosition() == selectedPosition) { // Compara con la posición actual del holder
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_highlight));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // O el color de fondo normal
        }

        // Configurar el listener para seleccionar/deseleccionar
        holder.itemView.setOnClickListener(v -> {
            // --- OBTENER LA POSICIÓN ACTUALIZADA ---
            int currentPosition = holder.getAdapterPosition(); // O getAdapterPosition()

            // --- COMPROBAR SI LA POSICIÓN ES VÁLIDA ---
            // Es importante porque el item podría haber sido eliminado justo antes del clic
            if (currentPosition == RecyclerView.NO_POSITION) {
                Log.w("SelectedProductAdapter", "Clic en un ViewHolder sin posición válida.");
                return; // No hacer nada si la posición no es válida
            }

            // --- Lógica de Selección/Deselección ---
            int previousSelectedPosition = selectedPosition;

            if (selectedPosition == currentPosition) { // Compara con la posición ACTUAL
                selectedPosition = RecyclerView.NO_POSITION; // Deseleccionar
            } else {
                selectedPosition = currentPosition; // Seleccionar nuevo usando la posición ACTUAL
                // Quitar resaltado del anterior (si había uno y NO es el actual)
                if (previousSelectedPosition != RecyclerView.NO_POSITION && previousSelectedPosition != selectedPosition) {
                    notifyItemChanged(previousSelectedPosition);
                }
            }
            // Resaltar/quitar resaltado del actual
            notifyItemChanged(currentPosition); // Usa la posición ACTUAL

            // Habilitar/deshabilitar el botón BORRAR directamente desde el adapter
            if (deleteButtonRef != null) {
                deleteButtonRef.setEnabled(selectedPosition != RecyclerView.NO_POSITION);
            }

            // Notificar a la Activity/Dialog sobre el cambio de selección
            if (clickListener != null) {
                // Pasamos la posición ACTUAL seleccionada (o NO_POSITION si se deseleccionó)
                clickListener.onProductSelectedClick(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedItems == null ? 0 : selectedItems.size();
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView quantityTextView;
        // Podríamos añadir botones +/- aquí si quisiéramos

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textViewSelectedProductName);
            quantityTextView = itemView.findViewById(R.id.textViewSelectedQuantity);
        }
    }
}
