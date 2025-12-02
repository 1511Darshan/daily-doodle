package com.example.dailydoodle.di

import android.content.Context
import com.example.dailydoodle.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AppModule {
    // Application context (will be set in Application class)
    var appContext: Context? = null
    
    // Firebase instances
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }
    
    val chainRepository: ChainRepository by lazy {
        ChainRepository(firestore)
    }
    
    val panelRepository: PanelRepository by lazy {
        PanelRepository(firestore)
    }
    
    val storageRepository: StorageRepository by lazy {
        StorageRepository(appContext ?: throw IllegalStateException("AppContext not initialized"))
    }
    
    val moderationRepository: ModerationRepository by lazy {
        ModerationRepository(firestore)
    }
    
    val trashRepository: TrashRepository by lazy {
        TrashRepository(firestore)
    }
    
    val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepository(firestore)
    }
}
