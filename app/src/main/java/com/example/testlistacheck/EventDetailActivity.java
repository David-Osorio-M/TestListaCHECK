package com.example.testlistacheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.Executors;

public class EventDetailActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button checkInButton = findViewById(R.id.button_check_in);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckInDialog();
            }
        });

        eventName = getIntent().getStringExtra("eventName");
        fetchEventDetails(eventName);
    }

    private void showCheckInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_in, null);
        builder.setView(dialogView);

        final EditText editTextRut = dialogView.findViewById(R.id.editTextRut);

        builder.setTitle("Check In")
                .setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String rutInvitado = editTextRut.getText().toString();
                        sendCheckInRequest(rutInvitado);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void sendCheckInRequest(String rutInvitado) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                    String token = sharedPreferences.getString("Token", "");

                    String json = String.format(
                            "{\"solicitud\": \"cambiarEstado\", \"token\": \"%s\", \"nombreEvento\": \"%s\", \"rutInvitado\": \"%s\"}",
                            token, eventName, rutInvitado
                    );

                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbzSYxY5ocJdt_A7Kmktm3VNTpLSIuyK6x9tbXJJg2nPK_D-wOoSz4mMbUXs3n86YxaP9Q/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fetchEventDetails(eventName);  // Refrescar la lista
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fetchEventDetails(String eventName) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                    String token = sharedPreferences.getString("Token", "");

                    String json = String.format(
                            "{\"solicitud\": \"listarInvitadosEvento\", \"token\": \"%s\", \"nombreEvento\": \"%s\"}",
                            token, eventName
                    );

                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbzSYxY5ocJdt_A7Kmktm3VNTpLSIuyK6x9tbXJJg2nPK_D-wOoSz4mMbUXs3n86YxaP9Q/exec")
                            .post(body)
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        JSONArray guestsArray = new JSONArray(responseBody);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TableLayout table = findViewById(R.id.table_layout);
                                table.removeAllViews();  // Limpiar tabla antes de añadir nuevas filas
                                for (int i = 0; i < guestsArray.length(); i++) {
                                    try {
                                        JSONObject guestObject = guestsArray.getJSONObject(i);
                                        String nombre = guestObject.getString("nombre");
                                        String rut = guestObject.getString("rut");
                                        int check = guestObject.getInt("check");
                                        String checkStatus = (check == 0) ? "No Ingresado" : "Ingresado";

                                        TableRow row = new TableRow(EventDetailActivity.this);
                                        row.addView(createTextView(nombre));
                                        row.addView(createTextView(rut));
                                        row.addView(createTextView(checkStatus));

                                        table.addView(row);
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

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        return textView;
    }
}
