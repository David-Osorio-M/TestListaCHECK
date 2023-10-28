
package com.example.testlistacheck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class AddPersonActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        eventName = getIntent().getStringExtra("eventName");
    }

    public void onBackButtonClick(View view) {
        Intent intent = new Intent(AddPersonActivity.this, ViewEventsActivity.class);
        startActivity(intent);
    }
    public void onSubmitButtonClick(View view) {
        EditText editTextName = findViewById(R.id.edit_text_name);
        EditText editTextRut = findViewById(R.id.edit_text_rut);

        String nombrePersona = editTextName.getText().toString();
        String rutPersona = editTextRut.getText().toString();

        sendPostRequest(nombrePersona, rutPersona);
    }

    private void sendPostRequest(String nombrePersona, String rutPersona) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                    String token = sharedPreferences.getString("Token", "");

                    String json = String.format(
                            "{\"solicitud\":\"insertarPersona\",\"token\":\"%s\",\"nombreEvento\":\"%s\",\"nombrePersona\":\"%s\",\"rutPersona\":\"%s\"}",
                            token, eventName, nombrePersona, rutPersona
                    );

                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbzQZccaPJx0_IGCvpBWCp4LrM_EppNMEA7IzWcD15ZrTtuNyynRV63Gwi84i0YOsn0PWA/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        Log.d("Response", responseBody);
                        Intent intent = new Intent(AddPersonActivity.this, ViewEventsActivity.class);
                        startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
