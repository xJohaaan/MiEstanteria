package com.example.miestanteria.data

import com.example.miestanteria.data.local.BibliotecaEntity
import com.example.miestanteria.data.remote.BibliotecaItemDto
import com.example.miestanteria.data.remote.LibroDto
import com.example.miestanteria.data.remote.LibroEntrada
import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Libro

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
    paginas = paginas
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
    paginas = libro.paginas,
    estado = estadoLectura ?: EstadoLectura.POR_LEER.name,
    paginaActual = paginaActual ?: 0,
    fechaAgregado = fechaAgregado ?: ""
)

fun BibliotecaEntity.toItem() = ItemBiblioteca(
    id = id,
    libro = Libro(
        apiId = apiId, titulo = titulo, autores = autores, editorial = editorial,
        fechaPublicacion = fechaPublicacion, portadaUrl = portadaUrl, descripcion = descripcion,
        categoria = categoria, paginas = paginas
    ),
    estado = EstadoLectura.desde(estado),
    paginaActual = paginaActual,
    fechaAgregado = fechaAgregado
)
