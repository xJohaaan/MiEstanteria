package com.example.miestanteria.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.CatalogoGateway
import com.example.miestanteria.data.repository.TipoBusqueda
import com.example.miestanteria.model.Libro
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val tipo: TipoBusqueda = TipoBusqueda.TODO,
    val resultados: List<Libro> = emptyList(),
    val cargando: Boolean = false,
    val cargandoMas: Boolean = false,
    val error: String? = null,
    val buscado: Boolean = false,
    val pagina: Int = 0,
    val hayMas: Boolean = false
)

/**
 * Controlador de la busqueda (RF03).
 * Autocompletado: busca solo cuando el usuario deja de escribir (espera [DEBOUNCE_MS])
 * y tiene al menos [MIN_CARACTERES] letras, para no saturar el servidor.
 */
class ExploreViewModel(
    private val catalogo: CatalogoGateway,
    enBiblioteca: Flow<Set<String>>
) : ViewModel() {

    companion object {
        const val DEBOUNCE_MS = 450L
        const val MIN_CARACTERES = 3
        const val POR_PAGINA = 20
    }

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    /** apiIds de los libros que el usuario ya tiene, para marcarlos en los resultados. */
    val idsEnBiblioteca: StateFlow<Set<String>> =
        enBiblioteca.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var busqueda: Job? = null

    fun onQueryChange(texto: String) {
        _state.update { it.copy(query = texto, error = null) }
        busqueda?.cancel()
        if (texto.trim().length < MIN_CARACTERES) {
            if (texto.isBlank()) _state.update { it.copy(resultados = emptyList(), buscado = false, cargando = false) }
            return
        }
        busqueda = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            ejecutar(nueva = true)
        }
    }

    fun onTipoChange(tipo: TipoBusqueda) {
        if (tipo == _state.value.tipo) return
        _state.update { it.copy(tipo = tipo) }
        if (_state.value.query.isNotBlank()) buscarAhora()
    }

    /** Buscar de inmediato (boton "buscar" del teclado o una sugerencia). */
    fun buscarAhora(texto: String? = null) {
        if (texto != null) _state.update { it.copy(query = texto) }
        if (_state.value.query.isBlank()) return
        busqueda?.cancel()
        busqueda = viewModelScope.launch { ejecutar(nueva = true) }
    }

    fun limpiar() {
        busqueda?.cancel()
        _state.value = ExploreUiState(tipo = _state.value.tipo)
    }

    /** Carga la siguiente pagina al llegar al final de la lista. */
    fun cargarMas() {
        val s = _state.value
        if (!s.hayMas || s.cargando || s.cargandoMas) return
        busqueda = viewModelScope.launch { ejecutar(nueva = false) }
    }

    private suspend fun ejecutar(nueva: Boolean) {
        val s = _state.value
        val pagina = if (nueva) 0 else s.pagina + 1
        _state.update { if (nueva) it.copy(cargando = true, error = null) else it.copy(cargandoMas = true) }
        when (val r = catalogo.buscar(s.query, s.tipo, pagina)) {
            is NetworkResult.Success -> _state.update { cur ->
                val lista = if (nueva) r.data else (cur.resultados + r.data).distinctBy { it.apiId }
                cur.copy(
                    resultados = lista, cargando = false, cargandoMas = false, buscado = true,
                    pagina = pagina, hayMas = r.data.size >= POR_PAGINA
                )
            }
            is NetworkResult.Error -> _state.update {
                it.copy(cargando = false, cargandoMas = false, buscado = true, error = r.message, hayMas = false)
            }
        }
    }
}
