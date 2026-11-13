package com.example.miestanteria

import android.app.Application
import com.example.miestanteria.data.AppContainer

/**
 * Clase Application: crea una sola vez las dependencias compartidas
 * (cliente HTTP, sesion, repositorios) y las deja disponibles para toda la app.
 */
class MiEstanteriaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
