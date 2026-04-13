package com.example.electricite;

public class User {
    // Attributs du profil
    public String uid;
    public String nom;
    public String telephone;
    public int scoreConfiance;

    // 1. Constructeur vide obligatoire pour Firebase
    public User() {
    }

    // 2. Constructeur pour créer un nouvel utilisateur
    public User(String uid, String nom, String telephone) {
        this.uid = uid;
        this.nom = nom;
        this.telephone = telephone;
        this.scoreConfiance = 10; // On commence avec un petit score de base
    }

    // 3. (Optionnel) Constructeur complet si besoin de modifier le score
    public User(String uid, String nom, String telephone, int scoreConfiance) {
        this.uid = uid;
        this.nom = nom;
        this.telephone = telephone;
        this.scoreConfiance = scoreConfiance;
    }
}