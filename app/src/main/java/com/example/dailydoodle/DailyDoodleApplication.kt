package com.example.dailydoodle

import android.app.Application
import android.util.Log
import com.example.dailydoodle.di.AppModule
import com.google.firebase.FirebaseApp

class DailyDoodleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize app context for repositories
        AppModule.appContext = this
        
        try {
            // Only initialize if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            // Log error but don't crash - allows app to run in demo mode
            Log.e("DailyDoodleApp", "Firebase initialization failed: ${e.message}")
            Log.w("DailyDoodleApp", "App will run in demo mode. Add valid google-services.json for full functionality.")
        }
    }
}
