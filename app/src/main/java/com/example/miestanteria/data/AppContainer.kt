package com.example.miestanteria.data

import android.content.Context
import com.example.miestanteria.BuildConfig
import com.example.miestanteria.data.remote.ApiClient
import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.session.SessionManager

/** Contenedor manual de dependencias (inyeccion de dependencias sencilla, sin librerias). */
class AppContainer(context: Context) {

    val sessionManager = SessionManager(context.applicationContext)

    val api: ApiService = ApiClient.create(
        baseUrl = BuildConfig.API_URL,
        tokenProvider = { sessionManager.currentToken() },
        debug = BuildConfig.DEBUG
    )

    val authRepository = AuthRepository(api, sessionManager)
}
