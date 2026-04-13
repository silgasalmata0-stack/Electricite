package com.example.electricite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SignalementAdapter extends RecyclerView.Adapter<SignalementAdapter.MyViewHolder> {

    private List<Signalement> listAffichee;
    private List<Signalement> listComplete;

    public SignalementAdapter(List<Signalement> list) {
        this.listAffichee = (list != null) ? list : new ArrayList<>();
        this.listComplete = new ArrayList<>(this.listAffichee);
    }

    public void updateList(List<Signalement> newList) {
        if (newList != null) {
            this.listAffichee = newList;
            this.listComplete = new ArrayList<>(newList);
            notifyDataSetChanged();
        }
    }

    public void filtrer(String texte) {
        String query = texte.toLowerCase().trim();
        List<Signalement> resultats = new ArrayList<>();

        if (query.isEmpty()) {
            resultats.addAll(listComplete);
        } else {
            for (Signalement s : listComplete) {
                if (s.getZone() != null && s.getZone().toLowerCase().contains(query)) {
                    resultats.add(s);
                }
            }
        }
        this.listAffichee = resultats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signalement, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Signalement s = listAffichee.get(position);
        String monUid = FirebaseAuth.getInstance().getUid();

        holder.txtZone.setText(s.getZone() != null ? s.getZone() : "Zone inconnue");
        holder.txtDateTime.setText(s.getDate() + " - " + s.getHeure());

        // --- COLORATION DU BADGE ---
        if ("Coupure".equalsIgnoreCase(s.getType())) {
            holder.txtType.setText("⚠️ COUPURE");
            holder.txtType.setTextColor(Color.WHITE);
            if (holder.txtType.getBackground() != null) {
                holder.txtType.getBackground().setTint(Color.parseColor("#D32F2F"));
            }
        } else {
            holder.txtType.setText("✅ RETOUR");
            holder.txtType.setTextColor(Color.WHITE);
            if (holder.txtType.getBackground() != null) {
                holder.txtType.getBackground().setTint(Color.parseColor("#388E3C"));
            }
        }

        // --- GESTION DES ACTIONS (SÉCURITÉ ET MODÉRATION) ---
        if (monUid != null && monUid.equals(s.getUserId())) {
            // C'est mon signalement
            holder.imgEdit.setVisibility(View.VISIBLE);
            holder.imgSignalerFaux.setVisibility(View.GONE); // On ne peut pas se signaler soi-même

            holder.imgEdit.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), FormulaireActivity.class);
                i.putExtra("KEY_MODIF", s.getKey());
                i.putExtra("ZONE_MODIF", s.getZone());
                i.putExtra("TYPE_MODIF", s.getType());
                v.getContext().startActivity(i);
            });

            holder.itemView.setOnLongClickListener(v -> {
                confirmerSuppression(v.getContext(), s.getKey());
                return true;
            });

        } else {
            // C'est le signalement de quelqu'un d'autre
            holder.imgEdit.setVisibility(View.GONE);
            holder.imgSignalerFaux.setVisibility(View.VISIBLE);
            holder.itemView.setOnLongClickListener(null);

            holder.imgSignalerFaux.setOnClickListener(v -> {
                // Pour la démo : baisse fictive du score ou message
                Toast.makeText(v.getContext(), "Merci, ce signalement sera vérifié par la communauté", Toast.LENGTH_SHORT).show();
                // Ici, tu pourrais ajouter la logique Firebase pour décrémenter le score de s.getUserId()
            });
        }
    }

    private void confirmerSuppression(Context context, String key) {
        new AlertDialog.Builder(context)
                .setTitle("Suppression")
                .setMessage("Voulez-vous vraiment supprimer ce signalement ?")
                .setPositiveButton("Oui, supprimer", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference("Signalements")
                            .child(key)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Supprimé !", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return listAffichee.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtZone, txtType, txtDateTime;
        ImageView imgEdit, imgSignalerFaux;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtZone = itemView.findViewById(R.id.txtZone);
            txtType = itemView.findViewById(R.id.txtType);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgSignalerFaux = itemView.findViewById(R.id.imgSignalerFaux); // Initialisation indispensable
        }
    }
}