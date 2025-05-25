package com.cesur.trabajofingrado.comanda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.R;
import java.util.List;
import java.util.Locale;


public class ComandaAdapter extends RecyclerView.Adapter<ComandaAdapter.ComandaViewHolder> {

    private List<ItemComandaTemporal> items;
    private Context context;
    private OnComandaItemClickListener itemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    // --- Interfaz para notificar selección a la Activity ---
    public interface OnComandaItemClickListener {
        void onComandaItemSelected(int position); // Notifica la posición seleccionada (o NO_POSITION)
    }


    // --- Constructor MODIFICADO ---
    public ComandaAdapter(Context context, List<ItemComandaTemporal> items, OnComandaItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.itemClickListener = listener;
    }

    // --- Metodo para actualizar datos ( ---
    public void setItems(List<ItemComandaTemporal> newItems) {
        this.items = newItems;
        selectedPosition = RecyclerView.NO_POSITION; // Resetear selección al actualizar lista
        notifyDataSetChanged();
    }

    // --- Metodo para obtener la posición seleccionada ---
    public int getSelectedPosition() {
        return selectedPosition;
    }

    // --- Metodo para obtener el item seleccionado ---
    public ItemComandaTemporal getSelectedItem() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
    }

    // --- Metodo para deseleccionar ---
    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (oldPosition != RecyclerView.NO_POSITION) {
            // Quitar resaltado si la vista aún es visible
            notifyItemChanged(oldPosition);
        }
    }

    @NonNull
    @Override
    public ComandaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout de la fila item_comanda_linea.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_comanda_linea, parent, false);
        return new ComandaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComandaViewHolder holder, int position) {

        if (position < 0 || position >= items.size()) { return; } // Safety check

        // Obtener el item actual
        ItemComandaTemporal currentItem = items.get(position);

        // Poner los datos en los TextViews del ViewHolder
        holder.productNameTextView.setText(currentItem.getProductName());
        holder.quantityTextView.setText(String.valueOf(currentItem.getQuantity())); // Convertir int a String

        // --- LÓGICA DE COLOR DE FONDO ---
        if (currentItem.isSentToKitchen()) {
            // Si el ítem ha sido enviado, píntalo de verde
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.item_sent_background));
        } else {
            // Si no ha sido enviado, aplica la lógica de selección normal
            if (holder.getAdapterPosition() == selectedPosition) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_highlight));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT); // O tu color de fondo por defecto para ítems no seleccionados
            }
        }

        // Configurar el listener para seleccionar/deseleccionar
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) { return; }

            int previousSelectedPosition = selectedPosition;

            if (selectedPosition == currentPosition) {
                selectedPosition = RecyclerView.NO_POSITION; // Deseleccionar si se pulsa el mismo
            } else {
                selectedPosition = currentPosition; // Seleccionar nuevo
                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition); // Quitar resaltado anterior
                }
            }
            notifyItemChanged(currentPosition); // Aplicar/quitar resaltado actual

            // Notificar a la Activity
            if (itemClickListener != null) {
                itemClickListener.onComandaItemSelected(selectedPosition);
            }
        });
        // Formatear el precio con dos decimales y símbolo de euro
        String formattedPrice = String.format(Locale.GERMANY, "%.2f €", currentItem.getTotalPrice());

        holder.priceTextView.setText(formattedPrice);

    }

    @Override
    public int getItemCount() {
        // Devolver el número total de items en la lista
        return items == null ? 0 : items.size();
    }

    // --- ViewHolder ---
    // Define cómo es la vista de cada fila y mantiene las referencias a sus elementos
    public static class ComandaViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView quantityTextView;
        TextView priceTextView;

        public ComandaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encontrar los TextViews definidos en item_comanda_linea.xml
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            quantityTextView = itemView.findViewById(R.id.textViewQuantity);
            priceTextView = itemView.findViewById(R.id.textViewPrice);
        }
    }
    // --- Fin ViewHolder ---
}