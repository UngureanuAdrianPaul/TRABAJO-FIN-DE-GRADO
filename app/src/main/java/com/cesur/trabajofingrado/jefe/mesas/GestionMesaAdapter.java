package com.cesur.trabajofingrado.jefe.mesas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cesur.trabajofingrado.R;
import java.util.List;
import java.util.Locale;

public class GestionMesaAdapter extends RecyclerView.Adapter<GestionMesaAdapter.MesaInfoViewHolder> {

    private List<MesaInfo> listaMesas;
    private Context context;
    private OnMesaManagementClickListener listener;

    // <<< NUEVA INTERFAZ >>>
    public interface OnMesaManagementClickListener {
        void onEditMesaClick(MesaInfo mesaInfo, int position);
        void onDeleteMesaClick(MesaInfo mesaInfo, int position);
    }

    public GestionMesaAdapter(Context context, List<MesaInfo> listaMesas, OnMesaManagementClickListener listener) {
        this.context = context;
        this.listaMesas = listaMesas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MesaInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_gestion_mesa, parent, false);
        return new MesaInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MesaInfoViewHolder holder, int position) {
        MesaInfo mesaActual = listaMesas.get(position);

        holder.numeroMesa.setText(String.format(Locale.getDefault(), "Mesa N°: %d", mesaActual.getNumero()));
        holder.capacidadMesa.setText(String.format(Locale.getDefault(), "Capacidad: %d", mesaActual.getCapacidad()));

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMesaClick(mesaActual, holder.getAdapterPosition()); // <<< LLAMAR AL LISTENER
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMesaClick(mesaActual, holder.getAdapterPosition()); // <<< LLAMAR AL LISTENER
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMesas == null ? 0 : listaMesas.size();
    }

    public void actualizarLista(List<MesaInfo> nuevaLista) {
        this.listaMesas.clear();
        if (nuevaLista != null) {
            this.listaMesas.addAll(nuevaLista);
        }
        notifyDataSetChanged();
    }

    static class MesaInfoViewHolder extends RecyclerView.ViewHolder {
        TextView numeroMesa;
        TextView capacidadMesa;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        public MesaInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            numeroMesa = itemView.findViewById(R.id.textViewNumeroMesaGestion);
            capacidadMesa = itemView.findViewById(R.id.textViewCapacidadMesaGestion);
            btnEditar = itemView.findViewById(R.id.buttonEditarMesa);
            btnEliminar = itemView.findViewById(R.id.buttonEliminarMesa);
        }
    }
}