package com.example.timeblock.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val garminEnabled: Boolean = false,
    val wordleShare: Boolean = false
)

class SettingsDataStore(private val context: Context) {
    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            theme = ThemeMode.valueOf(prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name),
            garminEnabled = prefs[KEY_GARMIN] ?: false,
            wordleShare = prefs[KEY_WORDLE] ?: false
        )
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setGarminEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_GARMIN] = enabled }
    }

    suspend fun setWordleShare(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WORDLE] = enabled }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_GARMIN = booleanPreferencesKey("garmin_integration")
        private val KEY_WORDLE = booleanPreferencesKey("wordle_share_format")
    }
}
