package com.example.miestanteria.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.BibliotecaRepository
import com.example.miestanteria.data.repository.ComunidadRepository
import com.example.miestanteria.data.session.SessionManager
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.Perfil
import com.example.miestanteria.model.Resena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val perfil: Perfil? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mostrarDialogoRecomendar: Boolean = false,
    val libroSeleccionado: Libro? = null,
    val mensajeRecomendacion: String = "",
    val recomendarOk: Boolean = false,
    val recomendarError: String? = null
)

/**
 * Controlador del perfil (ProfileFragment / UserProfileFragment).
 * Con [idPerfil] null muestra el perfil propio; con un id, el perfil publico de otro lector.
 */
class ProfileViewModel(
    private val idPerfil: Long?,
    private val comunidad: ComunidadRepository,
    private val session: SessionManager,
    private val bibliotecaRepo: BibliotecaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    /** Biblioteca del usuario actual (para elegir que libro recomendar). */
    val miBiblioteca: StateFlow<List<ItemBiblioteca>> = bibliotecaRepo.biblioteca
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cargar() {
        if (_state.value.cargando) return
        viewModelScope.launch {
            val id = idPerfil ?: session.load()?.userId
            if (id == null) {
                _state.update { it.copy(error = "Sin sesión") }
                return@launch
            }
            _state.update { it.copy(cargando = true, error = null) }
            when (val r = comunidad.perfil(id)) {
                is NetworkResult.Success -> _state.update { it.copy(perfil = r.data, cargando = false) }
                is NetworkResult.Error -> _state.update { it.copy(cargando = false, error = r.message) }
            }
        }
    }

    fun alternarMeGusta(resena: Resena) {
        viewModelScope.launch {
            val r = comunidad.alternarMeGusta(resena)
            if (r is NetworkResult.Success) _state.update { s ->
                val p = s.perfil ?: return@update s
                s.copy(perfil = p.copy(resenas = p.resenas.map {
                    if (it.id == resena.id) it.copy(meGustas = r.data.meGustas, meGustaMio = r.data.meGustaMio) else it
                }))
            }
        }
    }

    /** Recomendar un libro al lector del perfil actual. */
    fun recomendarLibro(libro: Libro, mensaje: String?) {
        val perfil = _state.value.perfil ?: return
        viewModelScope.launch {
            when (val r = comunidad.recomendar(perfil.lector, libro, mensaje)) {
                is NetworkResult.Success -> _state.update { it.copy(recomendarOk = true, recomendarError = null) }
                is NetworkResult.Error -> _state.update { it.copy(recomendarOk = false, recomendarError = r.message) }
            }
        }
    }

    fun mostrarDialogoRecomendar() {
        _state.update { it.copy(mostrarDialogoRecomendar = true, libroSeleccionado = null, mensajeRecomendacion = "") }
    }

    fun ocultarDialogoRecomendar() {
        _state.update { it.copy(mostrarDialogoRecomendar = false, libroSeleccionado = null, mensajeRecomendacion = "") }
    }

    fun seleccionarLibro(libro: Libro) {
        _state.update { it.copy(libroSeleccionado = libro) }
    }

    fun actualizarMensaje(mensaje: String) {
        _state.update { it.copy(mensajeRecomendacion = mensaje) }
    }

    fun enviarRecomendacion() {
        val libro = _state.value.libroSeleccionado ?: return
        val mensaje = _state.value.mensajeRecomendacion.takeIf { it.isNotBlank() }
        recomendarLibro(libro, mensaje)
        _state.update { it.copy(mostrarDialogoRecomendar = false) }
    }

    fun limpiarEstadoRecomendar() {
        _state.update { it.copy(recomendarOk = false, recomendarError = null) }
    }
}
