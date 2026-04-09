package com.example.electricite;

public class Signalement {
    // AJOUT DE LA CLÉ : Indispensable pour identifier le signalement dans Firebase
    private String key;

    private String userId;
    private String zone;
    private String type;
    private String date;
    private String heure;
    private String description;
    private long timestamp;

    // 1. Constructeur vide obligatoire pour Firebase
    public Signalement() {
    }

    // 2. Constructeur avec paramètres
    public Signalement(String userId, String zone, String type, String date, String heure, String description, long timestamp) {
        this.userId = userId;
        this.zone = zone;
        this.type = type;
        this.date = date;
        this.heure = heure;
        this.description = (description == null || description.isEmpty()) ? "Aucune description" : description;
        this.timestamp = timestamp;
    }

    // --- GETTERS ET SETTERS ---

    // TRÈS IMPORTANT : Getter et Setter pour la clé Firebase
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getDescription() {
        return description == null ? "Aucune description" : description;
    }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}