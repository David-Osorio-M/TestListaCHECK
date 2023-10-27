package com.example.testlistacheck;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ImportExcelActivity extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE_REQUEST_CODE = 1;
    private EditText eventNameEditText;
    private Uri excelFileUri;  // Variable para almacenar la URI del archivo Excel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_excel);

        eventNameEditText = findViewById(R.id.event_name_edit_text);
    }

    public void onAttachExcelClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_EXCEL_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            excelFileUri = data.getData();  // Guarda la URI del archivo Excel
        }
    }

    private void downloadFile(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Descargando Plantilla");
        request.setDescription("Descargando archivo Excel...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "plantilla.xlsx");

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    public void onDownloadTemplateClick(View view) {
        String url = "https://docs.google.com/spreadsheets/d/1tjMFr7TSSgocUj_Q-PeRX1rypmwJ3W6b6UZYrsST7IE/export?format=xlsx";
        downloadFile(url);
    }

    public void onCreateEventClick(View view) {
        try {
            // Obtener la información requerida
            String eventName = eventNameEditText.getText().toString();
            String excelFilePath = (excelFileUri != null) ? excelFileUri.toString() : null;
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());

            SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", null);

            // Crear el objeto JSON
            JSONObject eventInfo = new JSONObject();
            eventInfo.put("nombreEvento", eventName);
            eventInfo.put("archivoExcel", excelFilePath);
            eventInfo.put("fecha", currentDate);
            eventInfo.put("hora", currentTime);
            eventInfo.put("usuario", username);

            // Log the JSON object
            Log.d("EventInfo", eventInfo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
