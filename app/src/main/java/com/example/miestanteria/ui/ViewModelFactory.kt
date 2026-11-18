package com.example.miestanteria.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.miestanteria.MiEstanteriaApp
import com.example.miestanteria.data.AppContainer
import com.example.miestanteria.ui.auth.LoginViewModel
import com.example.miestanteria.ui.auth.RegisterViewModel
import com.example.miestanteria.ui.community.CommunityViewModel
import com.example.miestanteria.ui.detail.BookDetailViewModel
import com.example.miestanteria.ui.explore.ExploreViewModel
import com.example.miestanteria.ui.main.MainViewModel
import com.example.miestanteria.ui.profile.ProfileViewModel
import com.example.miestanteria.ui.shelf.ShelfViewModel
import com.example.miestanteria.ui.splash.SplashViewModel
import kotlinx.coroutines.flow.map

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as MiEstanteriaApp).container

/** Fabrica unica de ViewModels: les entrega sus dependencias desde el AppContainer. */
object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer { SplashViewModel(container().authRepository) }
        initializer { LoginViewModel(container().authRepository) }
        initializer { RegisterViewModel(container().authRepository) }
        initializer { MainViewModel(container().authRepository, container().bibliotecaRepository) }
        initializer {
            val c = container()
            ExploreViewModel(c.catalogoRepository, c.bibliotecaRepository.biblioteca.map { l -> l.map { it.libro.apiId }.toSet() })
        }
        initializer { ShelfViewModel(container().bibliotecaRepository) }
        initializer {
            val apiId = requireNotNull(createSavedStateHandle().get<String>("apiId"))
            val c = container()
            BookDetailViewModel(apiId, c.catalogoRepository, c.bibliotecaRepository, c.comunidadRepository)
        }
        initializer { CommunityViewModel(container().comunidadRepository) }
        initializer {
            // Sin argumento "id" es el perfil propio (pestana); con "id", el perfil publico de otro lector
            val id = createSavedStateHandle().get<Long>("id")
            ProfileViewModel(id, container().comunidadRepository, container().sessionManager)
        }
    }
}
