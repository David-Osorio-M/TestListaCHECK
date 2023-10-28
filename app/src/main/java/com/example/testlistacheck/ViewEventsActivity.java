package com.example.testlistacheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private LinearLayout eventsContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events);

        eventsContainer = findViewById(R.id.events_container);
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
                            .url("https://script.google.com/macros/s/AKfycbzQZccaPJx0_IGCvpBWCp4LrM_EppNMEA7IzWcD15ZrTtuNyynRV63Gwi84i0YOsn0PWA/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        JSONArray eventsArray = new JSONArray(responseBody);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);

                                for (int i = 0; i < eventsArray.length(); i++) {
                                    try {
                                        JSONObject eventObject = eventsArray.getJSONObject(i);
                                        String nombreEvento = eventObject.getString("nombreEvento");

                                        View eventRow = getLayoutInflater().inflate(R.layout.event_row, null);
                                        TextView eventName = eventRow.findViewById(R.id.event_name);
                                        Button buttonView = eventRow.findViewById(R.id.button_view);
                                        Button buttonAdd = eventRow.findViewById(R.id.button_add);

                                        eventName.setText(nombreEvento);

                                        buttonView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(ViewEventsActivity.this, EventDetailActivity.class);
                                                intent.putExtra("eventName", nombreEvento);
                                                startActivity(intent);
                                            }
                                        });



                                        buttonAdd.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(ViewEventsActivity.this, AddPersonActivity.class);
                                                intent.putExtra("eventName", nombreEvento);
                                                startActivity(intent);
                                            }
                                        });

                                        eventsContainer.addView(eventRow);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
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
