package com.example.miestanteria.data

import android.content.Context
import com.example.miestanteria.BuildConfig
import com.example.miestanteria.data.local.AppDatabase
import com.example.miestanteria.data.remote.ApiClient
import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.repository.BibliotecaRepository
import com.example.miestanteria.data.repository.CatalogoRepository
import com.example.miestanteria.data.session.SessionManager

/** Contenedor manual de dependencias (inyeccion de dependencias sencilla, sin librerias). */
class AppContainer(context: Context) {

    val sessionManager = SessionManager(context.applicationContext)

    private val database = AppDatabase.crear(context.applicationContext)

    val api: ApiService = ApiClient.create(
        baseUrl = BuildConfig.API_URL,
        tokenProvider = { sessionManager.currentToken() },
        debug = BuildConfig.DEBUG
    )

    val catalogoRepository = CatalogoRepository(api)

    val bibliotecaRepository = BibliotecaRepository(api, database.bibliotecaDao(), sessionManager)

    val authRepository = AuthRepository(api, sessionManager, onLogout = { bibliotecaRepository.limpiar() })
}
