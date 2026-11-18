package com.example.miestanteria.data

import com.example.miestanteria.data.local.BibliotecaEntity
import com.example.miestanteria.data.remote.BibliotecaItemDto
import com.example.miestanteria.data.remote.LibroDto
import com.example.miestanteria.data.remote.LibroEntrada
import com.example.miestanteria.data.remote.RecomendacionDto
import com.example.miestanteria.data.remote.ResenaDto
import com.example.miestanteria.data.remote.UsuarioResumenDto
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Lector
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.Recomendacion
import com.example.miestanteria.model.Resena

/** Conversiones entre los objetos de red (DTO), la base local (Entity) y el modelo de la app. */

fun LibroDto.toLibro() = Libro(
    apiId = apiId,
    titulo = titulo,
    autores = autores ?: "Autor desconocido",
    editorial = editorial,
    fechaPublicacion = fechaPublicacion,
    portadaUrl = portadaUrl,
    descripcion = descripcion,
    categoria = categoria,
    paginas = paginas?.takeIf { it > 0 }
)

fun Libro.toEntrada() = LibroEntrada(
    apiId = apiId, titulo = titulo, autores = autores, editorial = editorial,
    fechaPublicacion = fechaPublicacion, portadaUrl = portadaUrl, descripcion = descripcion,
    categoria = categoria, paginas = paginas
)

fun BibliotecaItemDto.toEntity(userId: Long) = BibliotecaEntity(
    id = id,
    userId = userId,
    apiId = libro.apiId,
    titulo = libro.titulo,
    autores = libro.autores ?: "Autor desconocido",
    editorial = libro.editorial,
    fechaPublicacion = libro.fechaPublicacion,
    portadaUrl = libro.portadaUrl,
    descripcion = libro.descripcion,
    categoria = libro.categoria,
    paginas = libro.paginas?.takeIf { it > 0 },
    estado = estadoLectura ?: EstadoLectura.POR_LEER.name,
    paginaActual = paginaActual ?: 0,
    fechaAgregado = fechaAgregado ?: ""
)

fun UsuarioResumenDto.toLector() = Lector(id, nombreUsuario, fotoPerfilUrl)

fun ResenaDto.toResena() = Resena(
    id = id, autor = autor.toLector(), libro = libro.toLibro(), calificacion = calificacion,
    comentario = comentario, fecha = fechaPublicacion ?: "", meGustas = meGustas,
    meGustaMio = meGustaMio, esMia = esMia
)

fun RecomendacionDto.toRecomendacion() = Recomendacion(
    id = id, emisor = emisor.toLector(), libro = libro.toLibro(), mensaje = mensaje,
    visto = visto, fecha = fechaEnvio ?: ""
)

fun BibliotecaEntity.toItem() = ItemBiblioteca(
    id = id,
    libro = Libro(
        apiId = apiId, titulo = titulo, autores = autores, editorial = editorial,
        fechaPublicacion = fechaPublicacion, portadaUrl = portadaUrl, descripcion = descripcion,
        categoria = categoria, paginas = paginas?.takeIf { it > 0 }
    ),
    estado = EstadoLectura.desde(estado),
    paginaActual = paginaActual,
    fechaAgregado = fechaAgregado
)
