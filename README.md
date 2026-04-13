#  Application de Signalement des Coupures d’Électricité

## Sujet du projet

Ce projet consiste à développer une application mobile Android permettant aux utilisateurs de signaler les coupures d’électricité et le retour
du courant dans leur zone, afin de partager l'information a tant réel.

## Membres du groupe

- Silga Salmata
- Yameogo Dimitri

## Architecture utilisée

L’application est développée selon une architecture simple en couches :

- Interface utilisateur (Activities / XML)
- Logique métier (gestion des signalements)
- Accès aux données via Firebase

Technologies utilisées :
- Java 
- Android Studio
- Firebase Authentication (authentification)
- Firebase Realtime Database ou Firestore (stockage des données)

## Fonctionnalités principales

- Authentification des utilisateurs
- Signalement d’une coupure d’électricité
- Consultation de l’historique des signalements
- Voir les signalement Récent
- Voir la carte géographique avec les zones en coupure et le retour
- Les statistique  a chaque signalement
- Affichage des zones affectées par les coupures
- Déconnexion utilisateur


## Étapes d’installation 
1. Cloner le projet

 Télécharger le projet depuis GitHub :git clone https://github.com/ton_nom/SignalementElectricite.git
 
2. Ouvrir le projet

 Ouvrir Android Studio
 Cliquer sur :Open Project

Sélectionner le dossier du projet qui est Electricite situer dans utilisateurs

3. Configurer Firebase (très important)
 Aller sur Firebase Console
 Créer un projet

Ensuite :Ajouter une application Android
-Télécharger le fichier : google-services.json
-Le placer dans :app/

4. Activer les services Firebase
Dans Firebase :
- ✔ Authentication → Email/Password
- ✔ Realtime Database ou Firestore

6. Synchroniser le projet
- Dans Android Studio : Sync Project with Gradle Files

6. Lancer l’application

 Connecter un téléphone OU utiliser un émulateur
Cliquer sur : Run ▶

 Étapes de test 
-Créer un compte
-Se connecter
-Ajouter un signalement
-Vérifier l’historique
-Tester la déconnexion
