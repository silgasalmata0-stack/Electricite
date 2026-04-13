package com.example.electricite;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView welcomeText, txtStatutReseau, txtSecteursCritiques;
    private DrawerLayout drawer;
    private MaterialButton btnActionSignaler;
    private CardView cardRecents, cardZones;
    private DatabaseReference database;
    private long applicationStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        applicationStartTime = System.currentTimeMillis() - 5000;

        // Permissions notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Configuration Toolbar et Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // --- GESTION DU HEADER DU MENU LATÉRAL ---
        // On récupère la vue mais on n'appelle plus navNom/navEmail car ils sont supprimés du XML
        View headerView = navigationView.getHeaderView(0);

        welcomeText = findViewById(R.id.welcomeText);
        txtStatutReseau = findViewById(R.id.txtStatutReseau);
        txtSecteursCritiques = findViewById(R.id.txtSecteursCritiques);


        // Message de bienvenue dynamique sur l'écran d'accueil (Dashboard)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference("Users").child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && user.nom != null) {
                                // Ici on concatène "Bienvenue " avec le nom de l'utilisateur
                                welcomeText.setText("Bienvenue " + user.nom);
                            } else {
                                welcomeText.setText("Bienvenue");
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        database = FirebaseDatabase.getInstance().getReference("Signalements");
        monitorerReseau();

        // Écouteur pour les nouvelles notifications
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Signalement s = snapshot.getValue(Signalement.class);
                if (s != null) {
                    String monUid = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                            FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

                    if (s.getTimestamp() > applicationStartTime && !monUid.equals(s.getUserId())) {
                        String titre = "⚠️ Alerte Électricité";
                        String msg = "Secteur : " + s.getZone();
                        if ("Coupure".equalsIgnoreCase(s.getType())) titre = "⚠️ Nouvelle coupure !";
                        else if ("Retour".equalsIgnoreCase(s.getType())) titre = "✅ Courant rétabli !";
                        NotificationService.envoyerNotification(DashboardActivity.this, titre, msg);
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Boutons et Cards
        btnActionSignaler = findViewById(R.id.btnActionSignaler);
        btnActionSignaler.setOnClickListener(v -> startActivity(new Intent(this, FormulaireActivity.class)));

        cardRecents = findViewById(R.id.cardSignalementsRecents);
        cardZones = findViewById(R.id.cardZonesAffectees);
        cardRecents.setOnClickListener(v -> ouvrirListe("RECENTS"));
        cardZones.setOnClickListener(v -> ouvrirListe("ZONES_CRITIQUES"));

        // Gestion du bouton retour
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
                else finish();
            }
        });
    }

    private void ouvrirListe(String action) {
        Intent intent = new Intent(this, SignalementActivity.class);
        intent.putExtra("ACTION", action);
        startActivity(intent);
    }

    private void monitorerReseau() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coupures = 0;
                for (DataSnapshot d : snapshot.getChildren()) {
                    Signalement s = d.getValue(Signalement.class);
                    if (s != null && "Coupure".equalsIgnoreCase(s.getType())) coupures++;
                }
                if (coupures > 0) {
                    txtStatutReseau.setText("ALERTE ACTIVE");
                    txtStatutReseau.setTextColor(Color.RED);
                    txtSecteursCritiques.setText(coupures + " SECTEUR(S) EN COUPURE");
                } else {
                    txtStatutReseau.setText("RÉSEAU STABLE");
                    txtStatutReseau.setTextColor(Color.GREEN);
                    txtSecteursCritiques.setText("AUCUNE COUPURE");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_signalements) {
            ouvrirListe("RECENTS");
        } else if (id == R.id.nav_history) {
            ouvrirListe("MON_HISTORIQUE");
        } else if (id == R.id.nav_stats) {
            startActivity(new Intent(this, StatsActivity.class));
        } else if (id == R.id.nav_carte) {
            startActivity(new Intent(this, CarteActivity.class));
        } else if (id == R.id.nav_profil) {
            startActivity(new Intent(this, ProfilActivity.class));
        } else if (id == R.id.nav_about) {
            // --- MODIFICATION ICI : On remplace l'AlertDialog par l'activité pro ---
            Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}