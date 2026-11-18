package com.example.miestanteria.model

/** Lector visible en la comunidad. */
data class Lector(val id: Long, val nombre: String, val fotoUrl: String? = null) {
    val iniciales: String
        get() = nombre.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
}

/** Resena de un libro (RF05) con sus "Me gusta" (RF06). */
data class Resena(
    val id: Long,
    val autor: Lector,
    val libro: Libro,
    val calificacion: Int,
    val comentario: String?,
    val fecha: String,
    val meGustas: Long,
    val meGustaMio: Boolean,
    val esMia: Boolean
)

data class ResenasDeLibro(val promedio: Double, val total: Int, val resenas: List<Resena>) {
    val mia: Resena? get() = resenas.firstOrNull { it.esMia }
    val deOtros: List<Resena> get() = resenas.filter { !it.esMia }
}

/** Recomendacion recibida de otro lector (RF07). */
data class Recomendacion(
    val id: Long,
    val emisor: Lector,
    val libro: Libro,
    val mensaje: String?,
    val visto: Boolean,
    val fecha: String
)

data class Feed(val noVistas: Long, val recomendaciones: List<Recomendacion>, val resenasRecientes: List<Resena>)

/** Reglas de los formularios de comunidad (mismas que valida el servidor). */
object ReglasComunidad {
    const val MAX_COMENTARIO = 500
    const val MAX_MENSAJE = 300

    fun calificacionValida(estrellas: Int) = estrellas in 1..5
}

/**
 * "hace 5 min", "hace 2 h", "ayer"... a partir de una fecha ISO del servidor.
 * El servidor envia UTC con zona ("2026-11-18T03:53:00Z"); se convierte a la hora local del celular.
 */
object FechaRelativa {
    fun aLocal(iso: String, zona: java.time.ZoneId = java.time.ZoneId.systemDefault()): java.time.LocalDateTime? = try {
        if (iso.endsWith("Z") || Regex("[+-]\\d{2}:\\d{2}$").containsMatchIn(iso)) {
            java.time.OffsetDateTime.parse(iso).atZoneSameInstant(zona).toLocalDateTime()
        } else {
            java.time.LocalDateTime.parse(iso.take(19))
        }
    } catch (_: Exception) {
        null
    }

    fun desde(
        iso: String,
        ahora: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        zona: java.time.ZoneId = java.time.ZoneId.systemDefault()
    ): String {
        val fecha = aLocal(iso, zona) ?: return ""
        val minutos = java.time.Duration.between(fecha, ahora).toMinutes().coerceAtLeast(0)
        return when {
            minutos < 1 -> "ahora"
            minutos < 60 -> "hace $minutos min"
            minutos < 60 * 24 -> "hace ${minutos / 60} h"
            minutos < 60 * 48 -> "ayer"
            minutos < 60 * 24 * 30 -> "hace ${minutos / (60 * 24)} días"
            else -> "%02d/%02d/%d".format(fecha.dayOfMonth, fecha.monthValue, fecha.year)
        }
    }
}
