package com.example.testlistacheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

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

public class login extends AppCompatActivity {

    private EditText inputAccount;
    private EditText inputPass;
    private Button buttonLogin;

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputAccount = findViewById(R.id.input_account);
        inputPass = findViewById(R.id.input_pass);
        buttonLogin = findViewById(R.id.button_login);

        setupButtonClickListener();
    }

    private void setupButtonClickListener() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = inputAccount.getText().toString();
                String pass = inputPass.getText().toString();
                sendPostRequest(account, pass);
            }
        });
    }

    private void sendPostRequest(String account, String pass) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = String.format("{\"solicitud\": \"login\", \"correo\": \"%s\", \"pass\": %s}", account, pass);
                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbzQZccaPJx0_IGCvpBWCp4LrM_EppNMEA7IzWcD15ZrTtuNyynRV63Gwi84i0YOsn0PWA/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        Log.d("Response", responseBody);

                        JSONArray jsonResponseArray = new JSONArray(responseBody);
                        JSONObject jsonResponse = jsonResponseArray.getJSONObject(0);
                        String token = jsonResponse.optString("Tkn", "");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar snackbar;
                                Log.d("bodyyyyyy", String.valueOf(request));
                                if (!token.isEmpty()) {
                                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Acceso Correcto", Snackbar.LENGTH_SHORT);
                                    snackbar.setBackgroundTint(ContextCompat.getColor(login.this, android.R.color.holo_green_light));

                                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("Token", token);
                                    editor.putString("Email", account);
                                    editor.apply();

                                    Intent intent = new Intent(login.this, MenuActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Error en credenciales", Snackbar.LENGTH_SHORT);
                                    snackbar.setBackgroundTint(ContextCompat.getColor(login.this, android.R.color.holo_red_light));
                                }
                                snackbar.show();
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
