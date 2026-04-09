package com.example.electricite;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SignalementAdapter adapter;
    private List<Signalement> signalementList;
    private DatabaseReference database;
    private ProgressBar progressBar;
    private String actionActuelle = "TOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signalement);

        if (getIntent().hasExtra("ACTION")) {
            actionActuelle = getIntent().getStringExtra("ACTION");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            switch (actionActuelle) {
                case "ZONES_CRITIQUES": getSupportActionBar().setTitle("Zones en Coupure"); break;
                case "MON_HISTORIQUE": getSupportActionBar().setTitle("Mon Historique Perso"); break;
                default: getSupportActionBar().setTitle("Signalements Récents"); break;
            }
        }

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewSignalements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        signalementList = new ArrayList<>();
        adapter = new SignalementAdapter(signalementList);
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference("Signalements");

        chargerSignalements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recherche, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Rechercher un secteur...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrer(newText);
                return true;
            }
        });
        return true;
    }

    private void chargerSignalements() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                signalementList.clear();

                // Récupération de l'UID pour le filtre Historique
                String monUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

                for (DataSnapshot data : snapshot.getChildren()) {
                    Signalement s = data.getValue(Signalement.class);
                    if (s != null) {
                        s.setKey(data.getKey()); // Important pour modif/suppr

                        // --- LOGIQUE DE FILTRAGE SELON L'ACTION ---
                        if ("ZONES_CRITIQUES".equals(actionActuelle)) {
                            if ("Coupure".equalsIgnoreCase(s.getType())) signalementList.add(s);
                        }
                        else if ("MON_HISTORIQUE".equals(actionActuelle)) {
                            // On ne garde que ceux qui nous appartiennent
                            if (s.getUserId() != null && s.getUserId().equals(monUid)) {
                                signalementList.add(s);
                            }
                        }
                        else {
                            signalementList.add(s);
                        }
                    }
                }

                // Tri : du plus récent au plus ancien
                Collections.sort(signalementList, (s1, s2) -> Long.compare(s2.getTimestamp(), s1.getTimestamp()));

                // Limite à 15 pour l'onglet "Récents"
                if ("RECENTS".equals(actionActuelle) && signalementList.size() > 15) {
                    signalementList = new ArrayList<>(signalementList.subList(0, 15));
                }

                adapter.updateList(signalementList);
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (signalementList.isEmpty()) {
                    Toast.makeText(SignalementActivity.this, "Aucun signalement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}