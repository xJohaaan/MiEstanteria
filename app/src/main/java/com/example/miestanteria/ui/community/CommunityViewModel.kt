package com.example.miestanteria.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.ComunidadRepository
import com.example.miestanteria.model.Feed
import com.example.miestanteria.model.Recomendacion
import com.example.miestanteria.model.Resena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val feed: Feed? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/** Controlador del muro de la comunidad (FeedFragment / RecommendationsFragment). */
class CommunityViewModel(private val comunidad: ComunidadRepository) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    fun refrescar() {
        if (_state.value.cargando) return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            when (val r = comunidad.feed()) {
                is NetworkResult.Success -> _state.update { it.copy(feed = r.data, cargando = false) }
                is NetworkResult.Error -> _state.update { it.copy(cargando = false, error = r.message) }
            }
        }
    }

    /** Al abrir una recomendacion nueva se marca como vista y baja el contador. */
    fun abrirRecomendacion(rec: Recomendacion) {
        if (rec.visto) return
        _state.update { s ->
            val f = s.feed ?: return@update s
            s.copy(feed = f.copy(
                noVistas = (f.noVistas - 1).coerceAtLeast(0),
                recomendaciones = f.recomendaciones.map { if (it.id == rec.id) it.copy(visto = true) else it }
            ))
        }
        viewModelScope.launch { comunidad.marcarVista(rec.id) }
    }

    fun alternarMeGusta(resena: Resena) {
        viewModelScope.launch {
            when (val r = comunidad.alternarMeGusta(resena)) {
                is NetworkResult.Success -> _state.update { s ->
                    val f = s.feed ?: return@update s
                    s.copy(feed = f.copy(resenasRecientes = f.resenasRecientes.map {
                        if (it.id == resena.id) it.copy(meGustas = r.data.meGustas, meGustaMio = r.data.meGustaMio) else it
                    }))
                }
                is NetworkResult.Error -> _state.update { it.copy(mensaje = r.message) }
            }
        }
    }

    fun mensajeMostrado() = _state.update { it.copy(mensaje = null) }
}
