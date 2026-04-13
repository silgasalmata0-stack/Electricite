package com.example.electricite;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Optionnel : Ajouter une flèche de retour dans la barre d'action
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("À propos du projet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Gestion du clic sur la flèche de retour
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Ferme cette activité et retourne à l'écran précédent
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}