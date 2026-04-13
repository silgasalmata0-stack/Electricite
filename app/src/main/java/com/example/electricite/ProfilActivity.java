package com.example.electricite;

import android.graphics.Color; // Import important pour la couleur
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar; // Import pour la Snackbar
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfilActivity extends AppCompatActivity {

    private EditText editNom, editTel;
    private TextView txtScore;
    private Button btnEnregistrer;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        editNom = findViewById(R.id.editNomProfil);
        editTel = findViewById(R.id.editTelProfil);
        txtScore = findViewById(R.id.txtScoreConfiance);
        btnEnregistrer = findViewById(R.id.btnEnregistrerProfil);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            chargerDonneesProfil();
        }

        btnEnregistrer.setOnClickListener(v -> {
            enregistrerModifications();
        });
    }

    private void chargerDonneesProfil() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // Note : addListenerForSingleValueEvent est mieux ici pour ne pas boucler si on modifie
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    editNom.setText(user.nom);
                    editTel.setText(user.telephone);
                    txtScore.setText("Confiance : " + user.scoreConfiance + " / 100");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void enregistrerModifications() {
        String nouveauNom = editNom.getText().toString().trim();
        String nouveauTel = editTel.getText().toString().trim();

        if (nouveauNom.isEmpty()) {
            editNom.setError("Le nom est requis");
            return;
        }

        // Mise à jour Firebase
        userRef.child("nom").setValue(nouveauNom);
        userRef.child("telephone").setValue(nouveauTel)
                .addOnSuccessListener(aVoid -> {
                    // --- UTILISATION DE LA SNACKBAR PROFESSIONNELLE ---
                    Snackbar.make(findViewById(android.R.id.content),
                                    "✅ Profil mis à jour avec succès !",
                                    Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.parseColor("#4CAF50")) // Vert SONABEL/Succès
                            .setTextColor(Color.WHITE)
                            .show();

                    // On attend un peu avant de fermer pour que l'utilisateur voit le message
                    btnEnregistrer.postDelayed(this::finish, 1500);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(android.R.id.content),
                                    "❌ Erreur lors de l'enregistrement",
                                    Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.RED)
                            .show();
                });
    }
}