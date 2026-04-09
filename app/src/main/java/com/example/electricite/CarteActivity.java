package com.example.electricite;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CarteActivity extends AppCompatActivity {
    private MapView map;
    private DatabaseReference database;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CONFIGURATION CRUCIALE : Identification de l'application
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_carte);

        map = findViewById(R.id.mapView);
        map.setMultiTouchControls(true);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Centrer sur Ouagadougou
        GeoPoint startPoint = new GeoPoint(12.3714, -1.5197);
        map.getController().setZoom(13.0);
        map.getController().setCenter(startPoint);

        database = FirebaseDatabase.getInstance().getReference("Signalements");
        chargerMarqueurs();

        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(v -> finish());
    }

    private void chargerMarqueurs() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                map.getOverlays().clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Signalement s = data.getValue(Signalement.class);
                    if (s != null && s.getZone() != null) {
                        ajouterMarqueurDepuisNomQuartier(s);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CarteActivity.this, "Erreur Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ajouterMarqueurDepuisNomQuartier(Signalement s) {
        // Fil secondaire pour éviter le crash "Application bloquée"
        new Thread(() -> {
            try {
                // Recherche précise avec Ville et Pays
                List<Address> addresses = geocoder.getFromLocationName(s.getZone() + ", Ouagadougou, Burkina Faso", 1);

                if (addresses != null && !addresses.isEmpty()) {
                    double lat = addresses.get(0).getLatitude();
                    double lon = addresses.get(0).getLongitude();

                    // Retour sur le fil principal pour l'affichage
                    runOnUiThread(() -> {
                        Marker marker = new Marker(map);
                        marker.setPosition(new GeoPoint(lat, lon));
                        marker.setTitle(s.getZone());
                        marker.setSnippet(s.getType() + " à " + s.getHeure());

                        // Icône par défaut colorée
                        marker.setIcon(getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default));
                        if ("Coupure".equalsIgnoreCase(s.getType())) {
                            marker.getIcon().setTint(Color.RED);
                        } else {
                            marker.getIcon().setTint(Color.GREEN);
                        }

                        map.getOverlays().add(marker);
                        map.invalidate();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onResume() { super.onResume(); map.onResume(); }
    @Override
    public void onPause() { super.onPause(); map.onPause(); }
}