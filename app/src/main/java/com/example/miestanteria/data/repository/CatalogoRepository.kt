package com.example.miestanteria.data.repository

import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.remote.safeApiCall
import com.example.miestanteria.data.toLibro
import com.example.miestanteria.model.Libro
import java.util.concurrent.ConcurrentHashMap

/** Tipo de busqueda (RF03). */
enum class TipoBusqueda(val etiqueta: String, val valorApi: String?) {
    TODO("Todo", null),
    TITULO("Título", "titulo"),
    AUTOR("Autor", "autor"),
    CATEGORIA("Categoría", "categoria")
}

/** Operaciones de busqueda que usa la pantalla Explorar (interfaz para poder probarla). */
interface CatalogoGateway {
    suspend fun buscar(
        texto: String,
        tipo: TipoBusqueda,
        pagina: Int,
        anioDesde: Int? = null,
        anioHasta: Int? = null
    ): NetworkResult<List<Libro>>
}

/** Modelo: busqueda de libros a traves del servidor (que consulta Google Books u Open Library). */
class CatalogoRepository(private val api: ApiService) : CatalogoGateway {

    /** Libros vistos en busquedas recientes, para abrir su detalle sin volver a pedirlos. */
    private val cache = ConcurrentHashMap<String, Libro>()

    override suspend fun buscar(
        texto: String,
        tipo: TipoBusqueda,
        pagina: Int,
        anioDesde: Int?,
        anioHasta: Int?
    ): NetworkResult<List<Libro>> =
        when (val r = safeApiCall { api.buscarLibros(texto.trim(), tipo.valorApi, pagina, anioDesde, anioHasta) }) {
            is NetworkResult.Success -> {
                val libros = r.data.map { it.toLibro() }
                libros.forEach { cache[it.apiId] = it }
                NetworkResult.Success(libros)
            }
            is NetworkResult.Error -> r
        }

    fun enCache(apiId: String): Libro? = cache[apiId]

    /** Detalle: usa la cache y, si el servidor trae mas datos (como la sinopsis), los combina. */
    suspend fun detalle(apiId: String): NetworkResult<Libro> {
        val local = cache[apiId]
        return when (val r = safeApiCall { api.detalleLibro(apiId) }) {
            is NetworkResult.Success -> {
                val remoto = r.data.toLibro()
                val combinado = if (local == null) remoto else local.copy(
                    descripcion = local.descripcion ?: remoto.descripcion,
                    categoria = local.categoria ?: remoto.categoria,
                    paginas = local.paginas ?: remoto.paginas,
                    editorial = local.editorial ?: remoto.editorial,
                    portadaUrl = local.portadaUrl ?: remoto.portadaUrl
                )
                cache[apiId] = combinado
                NetworkResult.Success(combinado)
            }
            is NetworkResult.Error -> if (local != null) NetworkResult.Success(local) else r
        }
    }
}
