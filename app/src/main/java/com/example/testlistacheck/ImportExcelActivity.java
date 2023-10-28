package com.example.testlistacheck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImportExcelActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_excel);

        eventNameEditText = findViewById(R.id.event_name_edit_text);
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCreateEventRequest();
            }
        });
    }

    private void sendCreateEventRequest() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("Token", null);
                    String eventName = eventNameEditText.getText().toString();
                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    String email = sharedPreferences.getString("Email", null);

                    JSONObject jsonRequest = new JSONObject();
                    jsonRequest.put("solicitud", "crearEvento");
                    jsonRequest.put("token", token);
                    jsonRequest.put("nombreEvento", eventName);
                    jsonRequest.put("fecha", currentDate);
                    jsonRequest.put("hora", currentTime);
                    jsonRequest.put("usuarioCreador", email);

                    RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbzQZccaPJx0_IGCvpBWCp4LrM_EppNMEA7IzWcD15ZrTtuNyynRV63Gwi84i0YOsn0PWA/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(ImportExcelActivity.this, MenuActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
