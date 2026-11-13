package com.example.miestanteria.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.miestanteria.MiEstanteriaApp
import com.example.miestanteria.data.AppContainer
import com.example.miestanteria.ui.auth.LoginViewModel
import com.example.miestanteria.ui.auth.RegisterViewModel
import com.example.miestanteria.ui.main.MainViewModel
import com.example.miestanteria.ui.splash.SplashViewModel

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as MiEstanteriaApp).container

/** Fabrica unica de ViewModels: les entrega sus dependencias desde el AppContainer. */
object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer { SplashViewModel(container().authRepository) }
        initializer { LoginViewModel(container().authRepository) }
        initializer { RegisterViewModel(container().authRepository) }
        initializer { MainViewModel(container().authRepository) }
    }
}
