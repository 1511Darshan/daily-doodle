package com.example.dailydoodle.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dailydoodle.ui.screen.settings.ColorPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

// Theme mode options
enum class ThemeMode(val displayName: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

class ThemePreferences(private val context: Context) {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
    }
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[THEME_MODE_KEY] ?: ThemeMode.System.name
        ThemeMode.valueOf(modeName)
    }
    
    val colorPalette: Flow<ColorPalette> = context.dataStore.data.map { preferences ->
        val paletteName = preferences[COLOR_PALETTE_KEY] ?: ColorPalette.Default.name
        try {
            ColorPalette.valueOf(paletteName)
        } catch (e: Exception) {
            ColorPalette.Default
        }
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    suspend fun setColorPalette(palette: ColorPalette) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_PALETTE_KEY] = palette.name
        }
    }
}
