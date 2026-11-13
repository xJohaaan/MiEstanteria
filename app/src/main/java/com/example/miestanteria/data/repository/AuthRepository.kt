package com.example.miestanteria.data.repository

import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.remote.AuthResponse
import com.example.miestanteria.data.remote.LoginRequest
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.remote.RegisterRequest
import com.example.miestanteria.data.remote.safeApiCall
import com.example.miestanteria.data.session.Session
import com.example.miestanteria.data.session.SessionManager

/** Estado de la sesion al abrir la app. */
enum class SessionState { VALIDA, SIN_SESION }

/** Operaciones de autenticacion que usan los ViewModels (interfaz para poder probarlos con datos falsos). */
interface AuthGateway {
    suspend fun login(email: String, password: String): NetworkResult<Session>
    suspend fun register(nombre: String, email: String, password: String): NetworkResult<Session>
}

/** Modelo (MVC/MVVM): registro, inicio y cierre de sesion contra el servidor. */
class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) : AuthGateway {

    val session = sessionManager.session

    override suspend fun login(email: String, password: String): NetworkResult<Session> {
        val r = safeApiCall { api.login(LoginRequest(email.trim(), password)) }
        return guardar(r)
    }

    override suspend fun register(nombre: String, email: String, password: String): NetworkResult<Session> {
        val r = safeApiCall { api.register(RegisterRequest(nombre.trim(), email.trim(), password)) }
        return guardar(r)
    }

    /**
     * Revisa si hay una sesion guardada y vigente.
     * Si no hay internet se permite entrar igual (modo offline); si el servidor dice 401, se cierra la sesion.
     */
    suspend fun checkSession(): SessionState {
        val s = sessionManager.load() ?: return SessionState.SIN_SESION
        if (s.vencida()) {
            sessionManager.clear()
            return SessionState.SIN_SESION
        }
        return when (val r = safeApiCall { api.me() }) {
            is NetworkResult.Success -> {
                if (r.data.nombreUsuario != s.nombre) sessionManager.updateNombre(r.data.nombreUsuario)
                SessionState.VALIDA
            }
            is NetworkResult.Error -> if (r.code == 401) {
                sessionManager.clear()
                SessionState.SIN_SESION
            } else {
                SessionState.VALIDA // sin conexion: se trabaja con los datos locales
            }
        }
    }

    suspend fun logout() = sessionManager.clear()

    private suspend fun guardar(r: NetworkResult<AuthResponse>): NetworkResult<Session> = when (r) {
        is NetworkResult.Success -> {
            val d = r.data
            sessionManager.save(d.token, d.expiraEnSegundos, d.usuario.id, d.usuario.nombreUsuario, d.usuario.email ?: "")
            NetworkResult.Success(sessionManager.load()!!)
        }
        is NetworkResult.Error -> r
    }
}
