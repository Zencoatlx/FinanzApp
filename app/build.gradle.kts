plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" // Usamos KSP en lugar de kapt
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.finanzapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.finanzapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Java 17
        targetCompatibility = JavaVersion.VERSION_17 // Java 17
    }
    kotlinOptions {
        jvmTarget = "17" // Java 17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navegación entre pantallas
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Base de datos Room
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // Gráficos para estadísticas
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ViewModels y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Corrutinas de Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Apache POI para Excel (exportación)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // Biblioteca Google Play Billing
    implementation("com.android.billingclient:billing-ktx:6.0.1")
}