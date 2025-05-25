package com.cesur.trabajofingrado.mesas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.R;
import java.util.List;
import java.util.Locale;

public class MesaAdapter extends RecyclerView.Adapter<MesaAdapter.MesaViewHolder> {

    private List<Mesa> mesaList;
    private Context context;
    private OnMesaClickListener listener; // Interfaz para manejar clics

    // --- Interfaz para comunicar clics a la Activity ---
    public interface OnMesaClickListener {
        void onMesaClick(Mesa mesa); // Pasa el objeto Mesa entero al hacer clic
    }


    // --- Constructor ---
    public MesaAdapter(Context context, List<Mesa> mesaList, OnMesaClickListener listener) {
        this.context = context;
        this.mesaList = mesaList;
        this.listener = listener;
    }

    // --- Metodo para actualizar la lista de datos desde la Activity ---
    public void updateData(List<Mesa> newMesaList) {
        this.mesaList.clear();
        if (newMesaList != null) {
            this.mesaList.addAll(newMesaList);
        }
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos cambiaron (forma simple)

    }

    @NonNull
    @Override
    public MesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea la vista para cada item usando item_mesa.xml
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mesa, parent, false);
        return new MesaViewHolder(itemView);
    }

    // Metodo para obtener la mesa actual de la lista
    @Override
    public void onBindViewHolder(@NonNull MesaViewHolder holder, int position) {
        Mesa mesaActual = mesaList.get(position);
        // Llama al metodo bind del ViewHolder para poner los datos en la vista
        holder.bind(mesaActual, listener, context);
    }

    // Metodo para obtener cuantos items hay en la lista
    @Override
    public int getItemCount() {
        return mesaList == null ? 0 : mesaList.size();
    }

    // --- ViewHolder: Representa CADA item visual en la lista ---
    public static class MesaViewHolder extends RecyclerView.ViewHolder {

        // Vistas dentro de item_mesa.xml
        ConstraintLayout tableItemContainer;
        TextView tableNumberTextView;
        ImageView tableImageView;

        public MesaViewHolder(@NonNull View itemView) {
            super(itemView);

            // --- Inicializar Vistas ---
            tableItemContainer = itemView.findViewById(R.id.tableItemContainer);
            tableNumberTextView = itemView.findViewById(R.id.tableNumber);
            tableImageView = itemView.findViewById(R.id.tableButton);
        }

        // Metodo para poner los datos de una Mesa en las vistas de este ViewHolder
        public void bind(final Mesa mesa, final OnMesaClickListener listener, Context context) {

            // 1. Poner el numero de mesa
            tableNumberTextView.setText(String.format(Locale.getDefault(), "%d", mesa.getNumero()));

            // 2. Cambiar el color de fondo segun el estado
            int backgroundColor;
            if ("libre".equalsIgnoreCase(mesa.getEstado())) {
                backgroundColor = ContextCompat.getColor(context, R.color.mesa_libre);
            } else if ("ocupada".equalsIgnoreCase(mesa.getEstado())) {
                backgroundColor = ContextCompat.getColor(context, R.color.mesa_ocupada);
            } else {
                // Color por defecto o para estado desconocido
                backgroundColor = ContextCompat.getColor(context, R.color.white);
            }
            tableItemContainer.setBackgroundColor(backgroundColor);


            // 3. Configurar el Click Listener solo para el icono de la mesa
            tableImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onMesaClick(mesa); // Llama al metodo de la interfaz pasando la mesa clicada
                    }
                }
            });


        }
    }

}
