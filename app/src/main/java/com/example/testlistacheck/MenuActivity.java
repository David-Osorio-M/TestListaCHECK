package com.example.testlistacheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void onImportExcelClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ImportExcelActivity.class);
        startActivity(intent);
    }
    public void onViewEventsClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ViewEventsActivity.class);
        startActivity(intent);
    }


    public void onRegisterEventClick(View view) {
        // Código para registrar evento
    }

    public void onExitClick(View view) {
        Intent intent = new Intent(MenuActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();  // (Opcional) Cierra la actividad de menu
    }

}
