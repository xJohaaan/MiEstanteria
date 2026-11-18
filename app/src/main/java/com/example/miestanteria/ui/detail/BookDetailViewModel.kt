package com.example.miestanteria.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.repository.BibliotecaRepository
import com.example.miestanteria.data.repository.CatalogoRepository
import com.example.miestanteria.data.repository.ComunidadRepository
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Lector
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.Resena
import com.example.miestanteria.model.ResenasDeLibro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val libro: Libro? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val trabajando: Boolean = false,
    val mensaje: String? = null,
    val resenas: ResenasDeLibro? = null,
    val cargandoResenas: Boolean = true,
    val errorResenas: String? = null
)

/**
 * Controlador del detalle del libro (BookDetailFragment): ver datos, agregar y cambiar estado/pagina,
 * resenas con calificacion (RF05), me gusta (RF06) y recomendar a otro lector (RF07).
 */
class BookDetailViewModel(
    private val apiId: String,
    private val catalogo: CatalogoRepository,
    private val biblioteca: BibliotecaRepository,
    private val comunidad: ComunidadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailUiState())
    val state: StateFlow<BookDetailUiState> = _state.asStateFlow()

    /** Registro del libro en la biblioteca del usuario (null si no lo tiene). Se actualiza solo desde Room. */
    val item: StateFlow<ItemBiblioteca?> =
        biblioteca.observarLibro(apiId).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        cargar()
        cargarResenas()
    }

    // ---------- Resenas (RF05) y me gusta (RF06) ----------

    fun cargarResenas() {
        viewModelScope.launch {
            _state.update { it.copy(cargandoResenas = true, errorResenas = null) }
            when (val r = comunidad.resenasDeLibro(apiId)) {
                is NetworkResult.Success -> _state.update { it.copy(resenas = r.data, cargandoResenas = false) }
                is NetworkResult.Error -> _state.update { it.copy(cargandoResenas = false, errorResenas = r.message) }
            }
        }
    }

    fun publicarResena(estrellas: Int, comentario: String) {
        val libro = _state.value.libro ?: return
        val mia = _state.value.resenas?.mia
        ejecutar(if (mia == null) "Reseña publicada" else "Reseña actualizada", recargarResenas = true) {
            if (mia == null) comunidad.publicarResena(libro, estrellas, comentario)
            else comunidad.editarResena(mia.id, estrellas, comentario)
        }
    }

    fun eliminarResena() {
        val mia = _state.value.resenas?.mia ?: return
        ejecutar("Reseña eliminada", recargarResenas = true) { comunidad.eliminarResena(mia.id) }
    }

    /** Me gusta: se actualiza solo la resena tocada con la respuesta del servidor. */
    fun alternarMeGusta(resena: Resena) {
        viewModelScope.launch {
            when (val r = comunidad.alternarMeGusta(resena)) {
                is NetworkResult.Success -> _state.update { s ->
                    val actual = s.resenas ?: return@update s
                    s.copy(resenas = actual.copy(resenas = actual.resenas.map {
                        if (it.id == resena.id) it.copy(meGustas = r.data.meGustas, meGustaMio = r.data.meGustaMio) else it
                    }))
                }
                is NetworkResult.Error -> _state.update { it.copy(mensaje = r.message) }
            }
        }
    }

    // ---------- Recomendar (RF07) ----------

    suspend fun buscarLectores(texto: String): NetworkResult<List<Lector>> = comunidad.buscarLectores(texto)

    fun recomendar(receptor: Lector, mensaje: String) {
        val libro = _state.value.libro ?: return
        ejecutar("Recomendación enviada a ${receptor.nombre}") { comunidad.recomendar(receptor, libro, mensaje) }
    }

    fun cargar() {
        viewModelScope.launch {
            // 1) Lo que ya hay en el telefono (busqueda reciente o biblioteca): se muestra al instante
            val local = catalogo.enCache(apiId) ?: biblioteca.observarLibro(apiId).first()?.libro
            _state.update { it.copy(libro = local, cargando = local == null, error = null) }
            // 2) El servidor completa lo que falte (por ejemplo la sinopsis)
            when (val r = catalogo.detalle(apiId)) {
                is NetworkResult.Success -> _state.update { it.copy(libro = combinar(it.libro, r.data), cargando = false) }
                is NetworkResult.Error -> _state.update {
                    if (it.libro == null) it.copy(cargando = false, error = r.message) else it.copy(cargando = false)
                }
            }
        }
    }

    fun agregar(estado: EstadoLectura, pagina: Int?) {
        val libro = _state.value.libro ?: return
        ejecutar("Libro agregado a tu biblioteca") { biblioteca.agregar(libro, estado, pagina) }
    }

    fun cambiarEstado(estado: EstadoLectura) {
        val actual = item.value ?: return
        if (actual.estado == estado) return
        ejecutar("Estado actualizado: ${estado.etiqueta}") { biblioteca.actualizar(actual.id, estado = estado) }
    }

    fun actualizarPagina(pagina: Int) {
        val actual = item.value ?: return
        val estado = if (actual.estado == EstadoLectura.POR_LEER) EstadoLectura.LEYENDO else null
        ejecutar("Vas en la página $pagina") { biblioteca.actualizar(actual.id, estado = estado, pagina = pagina) }
    }

    fun quitar(onDone: () -> Unit) {
        val actual = item.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(trabajando = true) }
            when (val r = biblioteca.eliminar(actual.id)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(trabajando = false) }
                    onDone()
                }
                is NetworkResult.Error -> _state.update { it.copy(trabajando = false, mensaje = r.message) }
            }
        }
    }

    fun mensajeMostrado() = _state.update { it.copy(mensaje = null) }

    private fun <T> ejecutar(exito: String, recargarResenas: Boolean = false, accion: suspend () -> NetworkResult<T>) {
        if (_state.value.trabajando) return
        viewModelScope.launch {
            _state.update { it.copy(trabajando = true) }
            val r = accion()
            _state.update {
                it.copy(trabajando = false, mensaje = if (r is NetworkResult.Error) r.message else exito)
            }
            if (recargarResenas && r is NetworkResult.Success) cargarResenas()
        }
    }

    companion object {
        /** Une los datos locales con los del servidor sin perder los que ya se tenian. */
        fun combinar(base: Libro?, remoto: Libro): Libro {
            if (base == null) return remoto
            return base.copy(
                autores = if (base.autores == "Autor desconocido") remoto.autores else base.autores,
                editorial = base.editorial ?: remoto.editorial,
                fechaPublicacion = base.fechaPublicacion ?: remoto.fechaPublicacion,
                portadaUrl = base.portadaUrl ?: remoto.portadaUrl,
                descripcion = base.descripcion ?: remoto.descripcion,
                categoria = base.categoria ?: remoto.categoria,
                paginas = base.paginas ?: remoto.paginas
            )
        }

        /** Google Books entrega la sinopsis con etiquetas HTML; se convierten a texto plano. */
        fun textoPlano(html: String): String = html
            .replace(Regex("(?i)<br\\s*/?>|</p>"), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"").replace("&amp;", "&").replace("&#39;", "'")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}
