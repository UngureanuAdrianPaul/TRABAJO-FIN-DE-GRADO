package com.cesur.trabajofingrado.jefe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.cesur.trabajofingrado.R;
import com.cesur.trabajofingrado.jefe.mesas.GestionMesasActivity;
import com.cesur.trabajofingrado.jefe.productos.GestionProductosActivity;
import com.cesur.trabajofingrado.login.LoginActivity;

public class JefeActivity extends AppCompatActivity {


    private static final String TAG = "JefeActivity";
    private ImageButton dropDownMenuButtonJefe;
    private Button btnManageTables;
    private Button btnManageFood;
    private Button btnManageDrinks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe);

        dropDownMenuButtonJefe = findViewById(R.id.dropDownMenuButtonJefe);
        btnManageTables = findViewById(R.id.manageTableButton);
        btnManageFood = findViewById(R.id.manageFoodButton);
        btnManageDrinks = findViewById(R.id.manageDrinkButton);

        if (dropDownMenuButtonJefe != null) {
            dropDownMenuButtonJefe.setOnClickListener(v -> mostrarDialogoMenuJefe());
        } else {
            Log.e(TAG, "dropDownMenuButtonJefe no encontrado en el layout.");
        }

        // Listener para "Gestiona Comida"
        if (btnManageFood != null) {
            btnManageFood.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Gestiona Comida' pulsado.");
                Intent intent = new Intent(JefeActivity.this, GestionProductosActivity.class); // Crear esta Activity después
                intent.putExtra("categoria_producto", "comida");
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Botón manageFoodButton no encontrado.");
        }

        // Listener para "Gestiona Bebida"
        if (btnManageDrinks != null) {
            btnManageDrinks.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Gestiona Bebida' pulsado.");
                Intent intent = new Intent(JefeActivity.this, GestionProductosActivity.class); // Crear esta Activity después
                intent.putExtra("categoria_producto", "bebida");
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Botón manageDrinkButton no encontrado.");
        }

        // Listener para "Gestiona Mesas"
        if (btnManageTables != null) {
            btnManageTables.setOnClickListener(v -> {
                Log.d(TAG, "Botón 'Gestiona Mesas' pulsado.");
                Intent intent = new Intent(JefeActivity.this, GestionMesasActivity.class); // Crear esta Activity
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Botón manageTableButton no encontrado.");
        }


    }


    // --- Metodo para mostar el menu del jefe ---
    private void mostrarDialogoMenuJefe() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        // Usar el layout burger_jefe.xml
        View dialogView = inflater.inflate(R.layout.burger_jefe, null);

        builder.setView(dialogView);

        final AlertDialog menuDialog = builder.create();

        Button closeSessionButton = dialogView.findViewById(R.id.closeBossSessionButton);


        if (closeSessionButton != null) {
            closeSessionButton.setOnClickListener(v_button -> {
                Log.d(TAG, "Botón Cerrar Sesión Jefe (AlertDialog) pulsado.");
                menuDialog.dismiss();

                LoginActivity.clearSession(JefeActivity.this); // Limpiar sesión

                Intent intent = new Intent(JefeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            Log.e(TAG, "Botón 'closeBossSessionButton' no encontrado en burger_jefe.xml");
        }


        menuDialog.show();

    }


}