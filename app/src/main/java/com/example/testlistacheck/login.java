package com.example.testlistacheck;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class login extends AppCompatActivity {

    // Declaración de variables para almacenar las referencias a los widgets y los datos.
    private EditText inputAccount;
    private EditText inputPass;
    private Button buttonLogin;

    // Definición del cliente HTTP y el tipo de medio JSON
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Obtención de las referencias a los widgets.
        inputAccount = findViewById(R.id.input_account);
        inputPass = findViewById(R.id.input_pass);
        buttonLogin = findViewById(R.id.button_login);



        // Establecimiento del OnClickListener.
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
                    String json = String.format("{\"sol\": \"validateAccount\", \"account\": \"%s\", \"pass\": \"%s\"}", account, pass);
                    RequestBody body = RequestBody.create(json, JSON);
                    Request request = new Request.Builder()
                            .url("https://script.google.com/macros/s/AKfycbwYbRSIEwu4areW4uJy2W3wEIkt74JDiPQLmukFKvWXR-IxFby_Lq957COBGJANxnEr/exec")
                            .post(body)
                            .build();
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        String responseBody = response.body().string();
                        Log.d("Response", responseBody);

                        // Parse the response body to JSON
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        int codigo = jsonResponse.getInt("codigo");
                        String mensaje = jsonResponse.getString("mensaje");

                        // Show the message using Toast on the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(login.this, mensaje, Toast.LENGTH_SHORT).show();
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
