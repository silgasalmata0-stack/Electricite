package com.example.electricite;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class StatsActivity extends AppCompatActivity {

    private BarChart barChart;
    private DatabaseReference database;
    private MaterialButton btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        barChart = findViewById(R.id.barChart);
        btnRetour = findViewById(R.id.btnRetour);
        database = FirebaseDatabase.getInstance().getReference("Signalements");

        btnRetour.setOnClickListener(v -> finish());

        recupererDonneesEtAfficherGraphique();
    }

    private void recupererDonneesEtAfficherGraphique() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> coupuresParDate = new TreeMap<>();
                Map<String, Integer> retoursParDate = new TreeMap<>();
                TreeSet<String> toutesLesDates = new TreeSet<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Signalement s = data.getValue(Signalement.class);
                    if (s != null && s.getDate() != null) {
                        String date = s.getDate();
                        toutesLesDates.add(date);

                        if ("Coupure".equalsIgnoreCase(s.getType())) {
                            coupuresParDate.put(date, coupuresParDate.getOrDefault(date, 0) + 1);
                        } else if ("Retour".equalsIgnoreCase(s.getType())) {
                            retoursParDate.put(date, retoursParDate.getOrDefault(date, 0) + 1);
                        }
                    }
                }

                if (toutesLesDates.isEmpty()) {
                    Toast.makeText(StatsActivity.this, "Aucune donnée à afficher", Toast.LENGTH_SHORT).show();
                } else {
                    preparerGraphiqueGroupe(toutesLesDates, coupuresParDate, retoursParDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatsActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preparerGraphiqueGroupe(TreeSet<String> dates, Map<String, Integer> coupures, Map<String, Integer> retours) {
        List<BarEntry> entriesCoupures = new ArrayList<>();
        List<BarEntry> entriesRetours = new ArrayList<>();
        List<String> labelsDate = new ArrayList<>(dates);

        for (int i = 0; i < labelsDate.size(); i++) {
            String date = labelsDate.get(i);
            entriesCoupures.add(new BarEntry(i, coupures.getOrDefault(date, 0).floatValue()));
            entriesRetours.add(new BarEntry(i, retours.getOrDefault(date, 0).floatValue()));
        }

        BarDataSet setCoupure = new BarDataSet(entriesCoupures, "Coupures");
        setCoupure.setColor(Color.parseColor("#FF5722")); // Orange
        setCoupure.setValueTextColor(Color.BLACK);

        BarDataSet setRetour = new BarDataSet(entriesRetours, "Retours");
        setRetour.setColor(Color.parseColor("#4CAF50")); // Vert
        setRetour.setValueTextColor(Color.BLACK);

        BarData data = new BarData(setCoupure, setRetour);

        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Config technique pour le groupement
        float groupSpace = 0.08f;
        float barSpace = 0.03f;
        float barWidth = 0.43f;

        data.setBarWidth(barWidth);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsDate));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labelsDate.size());

        barChart.groupBars(0f, groupSpace, barSpace);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setExtraBottomOffset(40f);
        barChart.setFitBars(true);
        barChart.invalidate();
        barChart.animateY(1000);
    }
}