package com.example.miestanteria.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Opciones de tema visual de la app. */
enum class AppTheme {
    /** Sigue el modo claro u oscuro del sistema. */
    SYSTEM,
    /** Fuerza el modo claro. */
    LIGHT,
    /** Fuerza el modo oscuro. */
    DARK
}

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "tema")

/**
 * Guarda la preferencia de tema (claro/oscuro/sistema) en DataStore,
 * para que persista entre sesiones sin necesidad de cuenta.
 */
class ThemePreference(private val context: Context) {

    private object Keys {
        val TEMA = stringPreferencesKey("tema")
    }

    val tema: Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        val valor = prefs[Keys.TEMA] ?: AppTheme.SYSTEM.name
        try { AppTheme.valueOf(valor) } catch (_: Exception) { AppTheme.SYSTEM }
    }

    suspend fun set(tema: AppTheme) {
        context.themeDataStore.edit { it[Keys.TEMA] = tema.name }
    }
}
