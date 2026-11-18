package com.example.miestanteria.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.repository.AuthRepository
import com.example.miestanteria.data.repository.BibliotecaRepository
import com.example.miestanteria.data.session.Session
import com.example.miestanteria.model.ItemBiblioteca
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado compartido por las pestanas principales: usuario de la sesion, biblioteca y cierre de sesion. */
class MainViewModel(
    private val auth: AuthRepository,
    private val bibliotecaRepo: BibliotecaRepository
) : ViewModel() {

    val session: StateFlow<Session?> = auth.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Biblioteca local (Room): disponible tambien sin conexion. */
    val biblioteca: StateFlow<List<ItemBiblioteca>> = bibliotecaRepo.biblioteca
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }
}
