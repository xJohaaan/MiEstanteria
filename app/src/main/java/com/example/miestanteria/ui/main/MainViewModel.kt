package com.example.miestanteria.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.session.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado compartido por las pestanas principales: el usuario de la sesion y el cierre de sesion. */
class MainViewModel(private val auth: AuthRepository) : ViewModel() {

    val session: StateFlow<Session?> = auth.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }
}
