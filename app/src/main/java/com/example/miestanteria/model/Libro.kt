package com.example.miestanteria.model

/** Libro del catalogo (resultado de busqueda o libro guardado). */
data class Libro(
    val apiId: String,
    val titulo: String,
    val autores: String,
    val editorial: String? = null,
    val fechaPublicacion: String? = null,
    val portadaUrl: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val paginas: Int? = null
) {
    /** Solo el anio (las fuentes devuelven "2018", "2018-05" o "May 2018"). */
    val anio: String? get() = fechaPublicacion?.let { Regex("\\d{4}").find(it)?.value }
}

/** Estado de lectura dentro de la biblioteca personal. */
enum class EstadoLectura(val etiqueta: String) {
    POR_LEER("Por leer"),
    LEYENDO("Leyendo"),
    LEIDO("Leído");

    companion object {
        fun desde(valor: String?): EstadoLectura = entries.firstOrNull { it.name == valor } ?: POR_LEER
    }
}

/** Un libro dentro de la biblioteca del usuario. */
data class ItemBiblioteca(
    val id: Long,
    val libro: Libro,
    val estado: EstadoLectura,
    val paginaActual: Int,
    val fechaAgregado: String
) {
    /** Avance de 0 a 1; null si no se conoce el numero de paginas. */
    val progreso: Float?
        get() = when {
            estado == EstadoLectura.LEIDO -> 1f
            libro.paginas == null || libro.paginas <= 0 -> null
            else -> (paginaActual.toFloat() / libro.paginas).coerceIn(0f, 1f)
        }
}
