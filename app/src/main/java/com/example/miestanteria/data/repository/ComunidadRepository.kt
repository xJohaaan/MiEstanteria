package com.example.miestanteria.data.repository

import com.example.miestanteria.data.remote.ActualizarResenaRequest
import com.example.miestanteria.data.remote.ApiService
import com.example.miestanteria.data.remote.CrearResenaRequest
import com.example.miestanteria.data.remote.MeGustaDto
import com.example.miestanteria.data.remote.NetworkResult
import com.example.miestanteria.data.remote.RecomendarRequest
import com.example.miestanteria.data.remote.safeApiCall
import com.example.miestanteria.data.toEntrada
import com.example.miestanteria.data.toLector
import com.example.miestanteria.data.toRecomendacion
import com.example.miestanteria.data.toResena
import com.example.miestanteria.model.Conteo
import com.example.miestanteria.model.Feed
import com.example.miestanteria.model.Perfil
import com.example.miestanteria.model.Lector
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.Resena
import com.example.miestanteria.model.ResenasDeLibro

/** Modelo de la comunidad: resenas (RF05), me gusta (RF06), recomendaciones (RF07) y feed. */
class ComunidadRepository(private val api: ApiService) {

    suspend fun resenasDeLibro(apiId: String): NetworkResult<ResenasDeLibro> =
        safeApiCall {
            val r = api.resenasDeLibro(apiId)
            ResenasDeLibro(r.promedio, r.total, r.resenas.map { it.toResena() })
        }

    suspend fun publicarResena(libro: Libro, estrellas: Int, comentario: String?): NetworkResult<Resena> =
        safeApiCall { api.crearResena(CrearResenaRequest(libro.toEntrada(), estrellas, comentario?.trim()?.ifBlank { null })).toResena() }

    suspend fun editarResena(id: Long, estrellas: Int, comentario: String?): NetworkResult<Resena> =
        safeApiCall { api.actualizarResena(id, ActualizarResenaRequest(estrellas, comentario?.trim()?.ifBlank { null })).toResena() }

    suspend fun eliminarResena(id: Long): NetworkResult<Unit> = safeApiCall {
        val resp = api.eliminarResena(id)
        if (!resp.isSuccessful && resp.code() != 404) throw retrofit2.HttpException(resp)
    }

    /** Pone o quita el "Me gusta" segun el estado actual. */
    suspend fun alternarMeGusta(resena: Resena): NetworkResult<MeGustaDto> = safeApiCall {
        if (resena.meGustaMio) api.quitarMeGusta(resena.id) else api.darMeGusta(resena.id)
    }

    suspend fun recomendar(receptor: Lector, libro: Libro, mensaje: String?): NetworkResult<Unit> = safeApiCall {
        api.recomendar(RecomendarRequest(receptor.id, libro.toEntrada(), mensaje?.trim()?.ifBlank { null }))
        Unit
    }

    suspend fun buscarLectores(texto: String): NetworkResult<List<Lector>> =
        safeApiCall { api.buscarUsuarios(texto.trim()).map { it.toLector() } }

    suspend fun feed(): NetworkResult<Feed> = safeApiCall {
        val f = api.feed()
        Feed(f.noVistas, f.recomendaciones.map { it.toRecomendacion() }, f.resenasRecientes.map { it.toResena() })
    }

    suspend fun perfil(idUsuario: Long): NetworkResult<Perfil> = safeApiCall {
        val p = api.perfil(idUsuario)
        val e = p.estadisticas
        Perfil(
            lector = Lector(p.id, p.nombreUsuario, p.fotoPerfilUrl),
            biografia = p.biografia, fechaRegistro = p.fechaRegistro, esMio = p.esMio,
            leidos = e.leidos, leyendo = e.leyendo, porLeer = e.porLeer, paginasLeidas = e.paginasLeidas,
            resenasEscritas = e.resenasEscritas, promedioCalificacion = e.promedioCalificacion,
            meGustasRecibidos = e.meGustasRecibidos, recomendacionesRecibidas = e.recomendacionesRecibidas,
            generosFavoritos = p.generosFavoritos.orEmpty().map { Conteo(it.etiqueta, it.cantidad) },
            leidosPorAnio = p.leidosPorAnio.orEmpty().map { Conteo(it.etiqueta, it.cantidad) },
            resenas = p.resenas.orEmpty().map { it.toResena() }
        )
    }

    suspend fun marcarVista(idRecomendacion: Long): NetworkResult<Unit> = safeApiCall {
        api.marcarRecomendacionVista(idRecomendacion)
        Unit
    }
}
