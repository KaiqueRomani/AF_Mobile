package com.example.af_mobile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private double latitudeAtual;
    private double longitudeAtual;

    private TextView txtLocalizacao;
    private Spinner spCategoria;
    private RecyclerView rvLocais;
    private Button btnBuscar;

    private List<Lugar> listaLugares;
    private LugarAdapter lugarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        spCategoria = findViewById(R.id.spCategoria);
        rvLocais = findViewById(R.id.rvLocais);
        btnBuscar = findViewById(R.id.btnBuscar);

        String[] categorias = {
                "Farmácia",
                "Hospital",
                "Escola",
                "Restaurante",
                "Praça",
                "Mercado"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        categorias);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);

        rvLocais.setLayoutManager(new LinearLayoutManager(this));
        listaLugares = new ArrayList<>();
        lugarAdapter = new LugarAdapter(listaLugares);
        rvLocais.setAdapter(lugarAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        obterLocalizacao();

        btnBuscar.setOnClickListener(v -> buscarLugares());
    }

    private void obterLocalizacao() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) {
                        Toast.makeText(this,
                                "Não foi possível obter localização",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    latitudeAtual = location.getLatitude();
                    longitudeAtual = location.getLongitude();

                    txtLocalizacao.setText(
                            "Lat: " + latitudeAtual +
                                    "\nLon: " + longitudeAtual
                    );
                });
    }

    private void buscarLugares() {

        String categoria = spCategoria.getSelectedItem().toString();
        String tag;

        switch (categoria) {
            case "Farmácia": tag = "pharmacy"; break;
            case "Hospital": tag = "hospital"; break;
            case "Escola": tag = "school"; break;
            case "Restaurante": tag = "restaurant"; break;
            case "Praça": tag = "park"; break;
            case "Mercado": tag = "supermarket"; break;
            default: tag = "supermarket"; break;
        }

        String query =
                "[out:json];" +
                        "node[amenity=" + tag + "]" +
                        "(around:2000," + latitudeAtual + "," + longitudeAtual + ");" +
                        "out body;";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "https://overpass.kumi.systems/api/interpreter?data=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Android-App")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Erro na requisição",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try (Response res = response) {

                    if (!res.isSuccessful()) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this,
                                        "Erro na API",
                                        Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    String body = res.body() != null ? res.body().string() : "";

                    runOnUiThread(() -> processarResultado(body));
                }
            }
        });
    }

    private void processarResultado(String json) {

        try {
            JSONObject obj = new JSONObject(json);
            JSONArray elements = obj.getJSONArray("elements");

            listaLugares.clear();

            for (int i = 0; i < elements.length(); i++) {

                JSONObject item = elements.getJSONObject(i);

                double lat = item.getDouble("lat");
                double lon = item.getDouble("lon");

                String nome = "Sem nome";

                if (item.has("tags")) {
                    JSONObject tags = item.getJSONObject("tags");
                    nome = tags.optString("name", "Sem nome");
                }

                listaLugares.add(new Lugar(nome, lat, lon, "Próximo"));
            }

            lugarAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Erro ao processar dados",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        }
    }
}