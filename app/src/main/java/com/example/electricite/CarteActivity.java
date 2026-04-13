package com.example.electricite;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
        new Thread(() -> {
            try {
                // RE-AJOUTÉ : Recherche de l'adresse précise
                String zoneRecherche = s.getZone() + ", Ouagadougou, Burkina Faso";
                List<Address> addresses = geocoder.getFromLocationName(zoneRecherche, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    double lat = addresses.get(0).getLatitude();
                    double lon = addresses.get(0).getLongitude();

                    runOnUiThread(() -> {
                        Marker marker = new Marker(map);
                        marker.setPosition(new GeoPoint(lat, lon));
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setTitle("Zone : " + s.getZone());

                        // RE-AJOUTÉ : Gestion de l'icône et des couleurs
                        Drawable iconeUnique = getResources()
                                .getDrawable(org.osmdroid.library.R.drawable.marker_default)
                                .mutate();

                        if ("Coupure".equalsIgnoreCase(s.getType())) {
                            marker.setSnippet("Statut : Coupure");
                            iconeUnique.setTint(Color.RED);
                        } else {
                            marker.setSnippet("Statut : Rétabli");
                            iconeUnique.setTint(Color.parseColor("#2E7D32"));
                        }

                        marker.setIcon(iconeUnique);
                        map.getOverlays().add(marker);
                        map.invalidate();
                    });
                }
            } catch (IOException e) {
                Log.e("GEO_ERR", "Erreur pour : " + s.getZone());
            }
        }).start();
    }

    @Override
    public void onResume() { super.onResume(); map.onResume(); }
    @Override
    public void onPause() { super.onPause(); map.onPause(); }
}