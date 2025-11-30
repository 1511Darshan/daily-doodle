plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.dailydoodle"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dailydoodle"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // AdMob App ID - Replace with your actual App ID from AdMob console
        // For testing, you can use: "ca-app-pub-3940256099942544~3347511713"
        resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    
    // Firebase BOM - manages all Firebase library versions
    // Using 33.7.0 (34.6.0 may not be published yet)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    
    // Firebase dependencies (versions managed by BoM above - don't specify versions)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Crashlytics removed for MVP - requires Gradle plugin which causes build issues
    // implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Google Sign-In
    implementation(libs.play.services.auth)
    
    // AdMob
    implementation(libs.admob)
    
    // Jetpack Ink for drawing canvas
    implementation(libs.ink.authoring)
    implementation(libs.ink.brush)
    implementation(libs.ink.geometry)
    implementation(libs.ink.nativeloader)
    implementation(libs.ink.rendering)
    implementation(libs.ink.strokes)
    implementation(libs.ink.storage)
    implementation(libs.ink.authoring.android)
    implementation(libs.ink.authoring.compose)
    implementation(libs.ink.brush.compose)
    implementation(libs.ink.geometry.compose)
    
    // Motion prediction for better stylus support
    implementation(libs.motion.prediction)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // Pager (Accompanist)
    implementation(libs.accompanist.pager)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}