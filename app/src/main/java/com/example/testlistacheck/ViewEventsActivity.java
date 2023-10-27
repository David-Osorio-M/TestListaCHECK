package com.example.testlistacheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ViewEventsActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private LinearLayout eventsContainer;  // Referencia al contenedor de eventos
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events);

        eventsContainer = findViewById(R.id.events_container); // Inicializa el contenedor de eventos
        progressBar = findViewById(R.id.progress_bar);

        fetchEvents();
    }
    public void onBackButtonClick(View view) {
        Intent intent = new Intent(ViewEventsActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
    private void fetchEvents() {
        progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                    String token = sharedPreferences.getString("Token", "");
                    String usuario = sharedPreferences.getString("Email", "");

                    String json = String.format(
                            "{\"solicitud\": \"listarEventosUsuario\", \"token\": \"%s\", \"usuario\": \"%s\"}",
                            token, usuario
                    );

                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbxM0bhZ8D7qF4y40yogvrFW5i49wCRfNzhJGQqaDK5Dmz0G1QKEpWow1UlWfzanAzorUQ/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        Log.d("Response", responseBody);

                        JSONArray eventsArray = new JSONArray(responseBody);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                for (int i = 0; i < eventsArray.length(); i++) {
                                    try {
                                        JSONObject eventObject = eventsArray.getJSONObject(i);
                                        String nombreEvento = eventObject.getString("nombreEvento");

                                        TextView eventTextView = new TextView(ViewEventsActivity.this);
                                        eventTextView.setText(nombreEvento);
                                        eventTextView.setTextSize(18);
                                        eventTextView.setPadding(0, 0, 0, 16);  // Añade un padding inferior

                                        eventsContainer.addView(eventTextView);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
