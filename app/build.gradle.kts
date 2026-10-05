plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
// Agrega esta línea:
    id("com.google.gms.google-services")
}
android {
    namespace = "com.example.eventpass"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.eventpass"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.code.scanner)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
// --- FIREBASE ---
    // Importa el BoM de Firebase (el controlador de versiones)
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))

    // Librería para Login / Registro (Firebase Auth)
    implementation("com.google.firebase:firebase-auth")

    // Librería para Base de Datos en la nube (Firestore)
    implementation("com.google.firebase:firebase-firestore")
    // --- REQUERIMIENTOS DEL PROFESOR ---

    // 1. Base de datos SQLite (Room) para modo Offline
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // 2. Extra: Conexión Web API (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 3. Proveedor de Imágenes y Video (Cloudinary)
    implementation("com.cloudinary:cloudinary-android:3.0.2")
}