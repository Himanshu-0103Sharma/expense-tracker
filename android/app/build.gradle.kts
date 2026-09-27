import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.expensetracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.expensetracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "CLOUDINARY_CLOUD", "\"${localProps.getProperty("CLOUDINARY_CLOUD", "")}\"")
        buildConfigField("String", "CLOUDINARY_PRESET", "\"${localProps.getProperty("CLOUDINARY_PRESET", "")}\"")
        buildConfigField("String", "API_BASE_URL", "\"${localProps.getProperty("API_BASE_URL", "http://10.0.2.2:8080")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Compose UI
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Ktor Client (for making HTTP requests to our server)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Kotlin Serialization (JSON parsing)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Encrypted storage for token persistence
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coil (image loading from URLs)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase Cloud Messaging (push notifications)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
}
