package com.example.miestanteria.data.repository

import com.example.miestanteria.data.local.BibliotecaDao
import com.example.miestanteria.data.remote.ActualizarBibliotecaRequest
import com.example.miestanteria.data.remote.AgregarBibliotecaRequest
import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.remote.safeApiCall
import com.example.miestanteria.data.session.SessionManager
import com.example.miestanteria.data.toEntity
import com.example.miestanteria.data.toEntrada
import com.example.miestanteria.data.toItem
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Libro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Modelo de la biblioteca personal (RF04).
 * La pantalla siempre lee de Room (funciona sin internet); el servidor es la fuente de verdad
 * y cada operacion exitosa actualiza la copia local.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BibliotecaRepository(
    private val api: ApiService,
    private val dao: BibliotecaDao,
    private val session: SessionManager
) {

    private val userId: Flow<Long?> = session.session.map { it?.userId }.distinctUntilChanged()

    val biblioteca: Flow<List<ItemBiblioteca>> = userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else dao.observarTodo(id).map { lista -> lista.map { it.toItem() } }
    }

    fun observarLibro(apiId: String): Flow<ItemBiblioteca?> = userId.flatMapLatest { id ->
        if (id == null) flowOf(null) else dao.observarPorApiId(id, apiId).map { it?.toItem() }
    }

    /** Descarga la biblioteca del servidor y reemplaza la copia local. Sin red, se conserva la copia. */
    suspend fun refrescar(): NetworkResult<Unit> {
        val id = session.load()?.userId ?: return NetworkResult.Error("Sin sesion")
        return when (val r = safeApiCall { api.biblioteca() }) {
            is NetworkResult.Success -> {
                dao.reemplazar(id, r.data.map { it.toEntity(id) })
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> r
        }
    }

    suspend fun agregar(libro: Libro, estado: EstadoLectura, pagina: Int?): NetworkResult<ItemBiblioteca> {
        val id = session.load()?.userId ?: return NetworkResult.Error("Sin sesion")
        val r = safeApiCall {
            api.agregarABiblioteca(AgregarBibliotecaRequest(libro.toEntrada(), estado.name, pagina))
        }
        return guardarResultado(id, r)
    }

    suspend fun actualizar(idItem: Long, estado: EstadoLectura? = null, pagina: Int? = null): NetworkResult<ItemBiblioteca> {
        val id = session.load()?.userId ?: return NetworkResult.Error("Sin sesion")
        val r = safeApiCall { api.actualizarBiblioteca(idItem, ActualizarBibliotecaRequest(estado?.name, pagina)) }
        return guardarResultado(id, r)
    }

    suspend fun eliminar(idItem: Long): NetworkResult<Unit> {
        val r = safeApiCall {
            val resp = api.eliminarDeBiblioteca(idItem)
            if (!resp.isSuccessful && resp.code() != 404) throw retrofit2.HttpException(resp)
        }
        if (r is NetworkResult.Success) dao.eliminar(idItem)
        return r
    }

    /** Borra la copia local (al cerrar sesion). */
    suspend fun limpiar() = dao.borrarTodo()

    private suspend fun guardarResultado(
        userId: Long,
        r: NetworkResult<com.example.miestanteria.data.remote.BibliotecaItemDto>
    ): NetworkResult<ItemBiblioteca> = when (r) {
        is NetworkResult.Success -> {
            val entity = r.data.toEntity(userId)
            dao.guardar(entity)
            NetworkResult.Success(entity.toItem())
        }
        is NetworkResult.Error -> r
    }
}
