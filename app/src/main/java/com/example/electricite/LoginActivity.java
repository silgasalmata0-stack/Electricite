package com.example.electricite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText editEmail, editPassword;
    private Button btnSeConnecter, btnCreerCompte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- MODE HORS-LIGNE ---
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Déjà activé
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnSeConnecter = findViewById(R.id.btnSeConnecter);
        btnCreerCompte = findViewById(R.id.btnCreerCompte);

        // --- ACTION : SE CONNECTER ---
        btnSeConnecter.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Champs vides !", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                            allerAuDashboard();
                        } else {
                            Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // --- ACTION : CRÉER UN COMPTE (ADAPTÉ AU PROFIL) ---
        btnCreerCompte.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(this, "Email valide et mot de passe > 6 car.", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 1. Récupérer l'ID unique de l'utilisateur
                            String userId = mAuth.getCurrentUser().getUid();

                            // 2. Créer un nom par défaut (partie avant le @ de l'email)
                            String nomParDefaut = email.split("@")[0];

                            // 3. Créer l'objet User (via ta classe User.java)
                            User nouveauProfil = new User(userId, nomParDefaut, "Non renseigné");

                            // 4. Enregistrer dans le dossier "Users"
                            mDatabase.child("Users").child(userId).setValue(nouveauProfil)
                                    .addOnCompleteListener(taskDb -> {
                                        if (taskDb.isSuccessful()) {
                                            Toast.makeText(this, "Compte et Profil créés !", Toast.LENGTH_SHORT).show();
                                            allerAuDashboard();
                                        } else {
                                            Toast.makeText(this, "Erreur Database : " + taskDb.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Erreur Auth : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void allerAuDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}