package com.example.miestanteria.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

/** Datos de la sesion guardados en el telefono. */
data class Session(
    val token: String,
    val expiraEn: Long, // epoch en milisegundos
    val userId: Long,
    val nombre: String,
    val email: String
) {
    fun vencida(ahora: Long = System.currentTimeMillis()): Boolean = ahora >= expiraEn
}

/**
 * Guarda el token JWT y los datos basicos del usuario con DataStore (almacenamiento privado de la app),
 * para que la sesion se mantenga al cerrar y abrir la aplicacion.
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val EXPIRA = longPreferencesKey("expira_en")
        val USER_ID = longPreferencesKey("user_id")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
    }

    /** Copia en memoria del token para que el interceptor HTTP lo lea sin esperar al disco. */
    @Volatile
    private var cachedToken: String? = null

    val session: Flow<Session?> = context.dataStore.data.map { it.toSession() }

    fun currentToken(): String? = cachedToken

    /** Lee la sesion guardada (la llama el Splash al abrir la app). */
    suspend fun load(): Session? {
        val s = context.dataStore.data.first().toSession()
        cachedToken = s?.takeIf { !it.vencida() }?.token
        return s
    }

    suspend fun save(token: String, expiraEnSegundos: Long, userId: Long, nombre: String, email: String) {
        val expira = System.currentTimeMillis() + expiraEnSegundos * 1000
        context.dataStore.edit {
            it[Keys.TOKEN] = token
            it[Keys.EXPIRA] = expira
            it[Keys.USER_ID] = userId
            it[Keys.NOMBRE] = nombre
            it[Keys.EMAIL] = email
        }
        cachedToken = token
    }

    suspend fun updateNombre(nombre: String) {
        context.dataStore.edit { it[Keys.NOMBRE] = nombre }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedToken = null
    }

    private fun Preferences.toSession(): Session? {
        val token = this[Keys.TOKEN] ?: return null
        return Session(
            token = token,
            expiraEn = this[Keys.EXPIRA] ?: 0L,
            userId = this[Keys.USER_ID] ?: 0L,
            nombre = this[Keys.NOMBRE] ?: "",
            email = this[Keys.EMAIL] ?: ""
        )
    }
}
