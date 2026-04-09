package com.example.electricite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editEmail, editPassword;
    private Button btnSeConnecter, btnCreerCompte; // On change le nom pour plus de clarté

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // --- 1. LIAISON AVEC LES IDS DU DESIGN ---
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnSeConnecter = findViewById(R.id.btnSeConnecter); // Bouton Bleu
        btnCreerCompte = findViewById(R.id.btnCreerCompte); // Bouton Orange

        // --- 2. ACTION : SE CONNECTER (Bouton Bleu) ---
        btnSeConnecter.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Champs vides pour la connexion !", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                            allerAuDashboard();
                        } else {
                            Toast.makeText(this, "Erreur de connexion : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // --- 3. ACTION : CRÉER UN COMPTE (Bouton Orange) ---
        // Cette partie rend ton bouton orange opérationnel immédiatement
        btnCreerCompte.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(this, "L'email doit être valide et le mot de passe > 6 caractères", Toast.LENGTH_LONG).show();
                return;
            }

            // Commande Firebase pour créer un nouvel utilisateur
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                            allerAuDashboard(); // Ouvre la page après la création
                        } else {
                            Toast.makeText(this, "Erreur de création : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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