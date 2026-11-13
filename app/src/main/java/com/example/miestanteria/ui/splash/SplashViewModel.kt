package com.example.miestanteria.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.repository.SessionState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Decide a donde ir al abrir la app: panel principal si hay sesion valida, o login. */
class SplashViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _destino = MutableStateFlow<SessionState?>(null)
    val destino: StateFlow<SessionState?> = _destino.asStateFlow()

    init {
        viewModelScope.launch {
            val check = async { auth.checkSession() }
            delay(600) // tiempo minimo para que se vea el logo
            _destino.value = check.await()
        }
    }
}
