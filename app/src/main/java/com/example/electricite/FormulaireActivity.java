package com.example.electricite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private String keyModif = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire);

        database = FirebaseDatabase.getInstance().getReference("Signalements");

        editZone = findViewById(R.id.editZone);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnEnvoyer = findViewById(R.id.btnEnvoyerSignalement);

        // --- 1. RÉCUPÉRATION DES DONNÉES SI MODIFICATION ---
        if (getIntent().hasExtra("KEY_MODIF")) {
            keyModif = getIntent().getStringExtra("KEY_MODIF");
            String zoneRecue = getIntent().getStringExtra("ZONE_MODIF");
            String typeRecu = getIntent().getStringExtra("TYPE_MODIF");

            editZone.setText(zoneRecue);

            // Vérification du type pour cocher le bon bouton radio
            if ("Coupure".equalsIgnoreCase(typeRecu)) {
                radioGroupStatus.check(R.id.radioCoupure);
            } else {
                radioGroupStatus.check(R.id.radioRetour);
            }
            btnEnvoyer.setText("Mettre à jour");
        }

        btnEnvoyer.setOnClickListener(v -> envoyerSignalement());
    }

    private void envoyerSignalement() {
        String zoneSaisie = editZone.getText().toString().trim();

        // Validation du champ texte
        if (zoneSaisie.isEmpty()) {
            editZone.setError("Veuillez entrer un quartier ou secteur");
            return;
        }

        // Validation du choix du statut
        int selectedId = radioGroupStatus.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Veuillez choisir un statut", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String typeSaisi = selectedRadio.getText().toString();

        // Récupération de l'ID utilisateur pour Firebase
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Gestion de la date et de l'heure
        Calendar calendar = Calendar.getInstance();
        long timestampActuel = System.currentTimeMillis();
        String dateActuelle = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
        String heureActuelle = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

        // Préparation de la Map de données
        Map<String, Object> signalementData = new HashMap<>();
        signalementData.put("zone", zoneSaisie);
        signalementData.put("type", typeSaisi);
        signalementData.put("date", dateActuelle);
        signalementData.put("heure", heureActuelle);
        signalementData.put("description", (keyModif != null) ? "Signalement mis à jour" : "Nouveau signalement");
        signalementData.put("userId", currentUserId);
        signalementData.put("timestamp", timestampActuel);

        btnEnvoyer.setEnabled(false); // Désactive pour éviter les doubles clics

        // Choix de la cible : modification d'un existant ou nouvel envoi (.push)
        DatabaseReference referenceCible = (keyModif != null) ? database.child(keyModif) : database.push();

        referenceCible.setValue(signalementData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 1. Message de succès
                String msg = (keyModif != null) ? "Mise à jour réussie !" : "Signalement enregistré avec succès !";
                Toast.makeText(FormulaireActivity.this, msg, Toast.LENGTH_LONG).show();

                // 2. Redirection vers le Tableau de Bord (Dashboard)
                Intent intent = new Intent(FormulaireActivity.this, DashboardActivity.class);

                // On nettoie la pile d'activités pour ne pas revenir au formulaire vide
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);
                finish(); // Ferme le formulaire
            } else {
                btnEnvoyer.setEnabled(true);
                Toast.makeText(this, "Erreur lors de l'enregistrement.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}