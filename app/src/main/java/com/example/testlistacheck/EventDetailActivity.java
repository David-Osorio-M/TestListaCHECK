package com.example.testlistacheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class EventDetailActivity extends AppCompatActivity {

    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private String eventName;

    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;

    Button btnEnviarUno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Button backButton = findViewById(R.id.button_back);

         btnEnviarUno = findViewById(R.id.btnEnviarUno);

        btnEnviarUno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarDatos("1");
            }
        });

        establecerConexionBluetooth();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button checkInButton = findViewById(R.id.button_check_in);

        Button checkOutButton = findViewById(R.id.button_check_out);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckInDialog();
            }
        });

        checkOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckOutDialog();
            }
        });

        eventName = getIntent().getStringExtra("eventName");
        fetchEventDetails(eventName);
    }

    private void establecerConexionBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("00:22:09:01:7B:64"); // Reemplaza con la dirección MAC de tu HC-05

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarDatos(String datos) {
        try {
            outputStream.write(datos.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    private void showCheckOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_in, null);
        builder.setView(dialogView);

        final EditText editTextRut = dialogView.findViewById(R.id.editTextRut);

        builder.setTitle("Check Out")
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
                    Log.d("jasonBody", String.valueOf(json));
                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbwZXgLugt1cMYir7OTZ2tHfIQCW81LQByrw85zyGmeA5Xk1XdyZhw5xOmlnOVkYohdyCw/exe")
                            .post(body)
                            .build();
                    Log.d("bodyyyyyy", String.valueOf(request));
                    Log.d("token", String.valueOf(token));
                    Log.d("eventName", String.valueOf(eventName));
                    Log.d("rutInvitado", String.valueOf(rutInvitado));
                    Log.d("body", String.valueOf(body));
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);


                        String responseString = response.body().string();
                        Log.d("RESPUESTA", String.valueOf(responseString));
                        String dataToSend = responseString.equals("1") ? "1" : "0";
                        enviarDatos(dataToSend);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fetchEventDetails(eventName);  // Refrescar la lista
                            }
                        });
                    }
                } catch (IOException e) {
                    enviarDatos("0");
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
                            .url("https://script.google.com/macros/s/AKfycbzQZccaPJx0_IGCvpBWCp4LrM_EppNMEA7IzWcD15ZrTtuNyynRV63Gwi84i0YOsn0PWA/exec")
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
