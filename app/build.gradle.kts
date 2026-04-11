plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.electricite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.electricite"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- CONFIGURATION FIREBASE (Version synchronisée par le BOM) ---
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.firebase:firebase-messaging")

    // --- LIBRAIRIE POUR LES STATISTIQUES ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- LIBRAIRIE POUR LA CARTE (OSMDroid) ---
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.android.volley:volley:1.2.1")


        // Pour la localisation (GPS)
    implementation("com.google.android.gms:play-services-location:21.2.0")


}