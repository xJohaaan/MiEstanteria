package com.example.miestanteria.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.BibliotecaRepository
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrdenEstanteria(val etiqueta: String) {
    RECIENTES("Agregados recientemente"),
    TITULO("Título (A-Z)"),
    AUTOR("Autor (A-Z)"),
    PROGRESO("Mayor avance")
}

data class ShelfUiState(
    val items: List<ItemBiblioteca> = emptyList(),
    val conteo: Map<EstadoLectura?, Int> = emptyMap(),
    val filtro: EstadoLectura? = null,
    val orden: OrdenEstanteria = OrdenEstanteria.RECIENTES,
    val cargandoInicial: Boolean = true
)

/** Controlador de "Mi Estanteria" (MyShelfFragment): filtrar por estado, ordenar, editar pagina y quitar. */
class ShelfViewModel(private val biblioteca: BibliotecaRepository) : ViewModel() {

    private val filtro = MutableStateFlow<EstadoLectura?>(null)
    private val orden = MutableStateFlow(OrdenEstanteria.RECIENTES)
    private val recibido = MutableStateFlow(false)

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Ultimo libro quitado, para poder deshacer. */
    private var ultimoQuitado: ItemBiblioteca? = null

    val state: StateFlow<ShelfUiState> = combine(biblioteca.biblioteca, filtro, orden, recibido) { todos, f, o, r ->
        ShelfUiState(
            items = aplicar(todos, f, o),
            conteo = contar(todos),
            filtro = f,
            orden = o,
            cargandoInicial = todos.isEmpty() && !r
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShelfUiState())

    init {
        refrescar()
    }

    fun refrescar() {
        viewModelScope.launch {
            _refrescando.value = true
            val r = biblioteca.refrescar()
            _refrescando.value = false
            recibido.value = true
            if (r is NetworkResult.Error) _mensaje.value = "Sin conexión: mostrando tu biblioteca guardada"
        }
    }

    fun onFiltro(e: EstadoLectura?) { filtro.value = e }
    fun onOrden(o: OrdenEstanteria) { orden.value = o }

    fun actualizarPagina(item: ItemBiblioteca, pagina: Int) {
        viewModelScope.launch {
            val estado = if (item.estado == EstadoLectura.POR_LEER) EstadoLectura.LEYENDO else null
            val r = biblioteca.actualizar(item.id, estado = estado, pagina = pagina)
            _mensaje.value = if (r is NetworkResult.Error) r.message else "Progreso guardado"
        }
    }

    fun quitar(item: ItemBiblioteca) {
        viewModelScope.launch {
            when (val r = biblioteca.eliminar(item.id)) {
                is NetworkResult.Success -> {
                    ultimoQuitado = item
                    _mensaje.value = QUITADO
                }
                is NetworkResult.Error -> _mensaje.value = r.message
            }
        }
    }

    fun deshacerQuitar() {
        val item = ultimoQuitado ?: return
        ultimoQuitado = null
        viewModelScope.launch {
            val r = biblioteca.agregar(item.libro, item.estado, item.paginaActual)
            if (r is NetworkResult.Error) _mensaje.value = r.message
        }
    }

    fun mensajeMostrado() = _mensaje.update { null }

    companion object {
        const val QUITADO = "Libro quitado de tu estantería"

        fun aplicar(items: List<ItemBiblioteca>, filtro: EstadoLectura?, orden: OrdenEstanteria): List<ItemBiblioteca> {
            val filtrados = if (filtro == null) items else items.filter { it.estado == filtro }
            return when (orden) {
                OrdenEstanteria.RECIENTES -> filtrados.sortedByDescending { it.fechaAgregado }
                OrdenEstanteria.TITULO -> filtrados.sortedBy { it.libro.titulo.lowercase() }
                OrdenEstanteria.AUTOR -> filtrados.sortedBy { it.libro.autores.lowercase() }
                OrdenEstanteria.PROGRESO -> filtrados.sortedByDescending { it.progreso ?: 0f }
            }
        }

        fun contar(items: List<ItemBiblioteca>): Map<EstadoLectura?, Int> =
            items.groupingBy<ItemBiblioteca, EstadoLectura?> { it.estado }.eachCount() + (null to items.size)
    }
}
