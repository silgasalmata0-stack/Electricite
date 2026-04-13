package com.example.electricite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormulaireActivity extends AppCompatActivity {

    private TextInputEditText editZone;
    private RadioGroup radioGroupStatus;
    private MaterialButton btnEnvoyer;
    private DatabaseReference database;
    private DatabaseReference usersRef; // Ajouté pour le score de confiance
    private String keyModif = null;
    private FusedLocationProviderClient fusedLocationClient;

    private final String SERVER_KEY = "AIzaSyD6nRTn_ZL5-ePJoCBQQGqLsPjxk4ExxXk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire);

        database = FirebaseDatabase.getInstance().getReference("Signalements");
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Initialisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editZone = findViewById(R.id.editZone);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnEnvoyer = findViewById(R.id.btnEnvoyerSignalement);

        if (getIntent().hasExtra("KEY_MODIF")) {
            keyModif = getIntent().getStringExtra("KEY_MODIF");
            editZone.setText(getIntent().getStringExtra("ZONE_MODIF"));
            String typeRecu = getIntent().getStringExtra("TYPE_MODIF");
            if ("Coupure".equalsIgnoreCase(typeRecu)) {
                radioGroupStatus.check(R.id.radioCoupure);
            } else {
                radioGroupStatus.check(R.id.radioRetour);
            }
            btnEnvoyer.setText("Mettre à jour");
        }

        btnEnvoyer.setOnClickListener(v -> verifierScoreEtPermissions());
    }

    private void verifierScoreEtPermissions() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnvoyer.setEnabled(false);

        // 1. Vérification du Score de Confiance avant tout
        usersRef.child(uid).child("scoreConfiance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si le score n'existe pas encore, on considère qu'il est de 100 par défaut
                int score = snapshot.exists() ? snapshot.getValue(Integer.class) : 100;

                if (score <= 0) {
                    Toast.makeText(FormulaireActivity.this,
                            "❌ Votre compte est suspendu pour signalements abusifs.",
                            Toast.LENGTH_LONG).show();
                    btnEnvoyer.setEnabled(true);
                } else {
                    // 2. Si le score est bon, on vérifie les permissions GPS
                    continuerVersLocalisation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnEnvoyer.setEnabled(true);
            }
        });
    }

    private void continuerVersLocalisation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            btnEnvoyer.setEnabled(true);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            double lat = (location != null) ? location.getLatitude() : 0.0;
            double lng = (location != null) ? location.getLongitude() : 0.0;
            procederAEnregistrement(lat, lng);
        });
    }

    private void procederAEnregistrement(double lat, double lng) {
        String zoneSaisie = editZone.getText().toString().trim();
        if (zoneSaisie.isEmpty()) {
            editZone.setError("Quartier requis");
            btnEnvoyer.setEnabled(true);
            return;
        }

        RadioButton selectedRadio = findViewById(radioGroupStatus.getCheckedRadioButtonId());
        if (selectedRadio == null) {
            Toast.makeText(this, "Sélectionnez un statut", Toast.LENGTH_SHORT).show();
            btnEnvoyer.setEnabled(true);
            return;
        }
        String typeSaisi = selectedRadio.getText().toString();

        if ("Retour".equalsIgnoreCase(typeSaisi)) {
            database.orderByChild("zone").equalTo(zoneSaisie).limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean peutSignalerRetour = false;
                            if (snapshot.exists()) {
                                for (DataSnapshot d : snapshot.getChildren()) {
                                    Signalement dernier = d.getValue(Signalement.class);
                                    if (dernier != null && "Coupure".equalsIgnoreCase(dernier.getType())) {
                                        peutSignalerRetour = true;
                                    }
                                }
                            }

                            if (peutSignalerRetour) {
                                enregistrerVraiSignalement(zoneSaisie, typeSaisi, lat, lng);
                            } else {
                                Toast.makeText(FormulaireActivity.this,
                                        "Action impossible : Aucune coupure n'est active dans cette zone !",
                                        Toast.LENGTH_LONG).show();
                                btnEnvoyer.setEnabled(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            btnEnvoyer.setEnabled(true);
                        }
                    });
        } else {
            enregistrerVraiSignalement(zoneSaisie, typeSaisi, lat, lng);
        }
    }

    private void enregistrerVraiSignalement(String zoneSaisie, String typeSaisi, double lat, double lng) {
        Calendar calendar = Calendar.getInstance();
        String dateActuelle = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        String heureActuelle = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());
        String currentUserId = FirebaseAuth.getInstance().getUid() != null ? FirebaseAuth.getInstance().getUid() : "";

        Map<String, Object> data = new HashMap<>();
        data.put("zone", zoneSaisie);
        data.put("type", typeSaisi);
        data.put("date", dateActuelle);
        data.put("heure", heureActuelle);
        data.put("userId", currentUserId);
        data.put("timestamp", System.currentTimeMillis());
        data.put("latitude", lat);
        data.put("longitude", lng);

        DatabaseReference ref = (keyModif != null) ? database.child(keyModif) : database.push();

        ref.setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                declencherNotificationFCM(zoneSaisie, typeSaisi, currentUserId);
                Toast.makeText(this, "Signalement enregistré !", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                btnEnvoyer.setEnabled(true);
            }
        });
    }

    private void declencherNotificationFCM(String zone, String type, String senderId) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject mainPayload = new JSONObject();
            mainPayload.put("to", "/topics/alertes");

            JSONObject dataBundle = new JSONObject();
            dataBundle.put("title", "⚠️ Alerte Électricité");
            dataBundle.put("message", type + " à " + zone);
            dataBundle.put("senderId", senderId);

            mainPayload.put("data", dataBundle);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, mainPayload, null, null) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + SERVER_KEY);
                    return headers;
                }
            };
            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) { e.printStackTrace(); }
    }
}