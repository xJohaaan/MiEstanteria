package com.example.miestanteria

import com.example.miestanteria.model.FechaRelativa
import com.example.miestanteria.model.Lector
import com.example.miestanteria.model.Libro
import com.example.miestanteria.model.ReglasComunidad
import com.example.miestanteria.model.Resena
import com.example.miestanteria.model.ResenasDeLibro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** Reglas de comunidad que no dependen de Android (RF05, RF06, RF07). */
class ComunidadLogicTest {

    private val libro = Libro("x", "Titulo", "Autor")
    private fun resena(id: Long, mia: Boolean) =
        Resena(id, Lector(id, "Lector $id"), libro, 4, null, "", 0, false, mia)

    @Test
    fun separaLaResenaPropiaDeLasDeOtros() {
        val r = ResenasDeLibro(4.0, 3, listOf(resena(1, false), resena(2, true), resena(3, false)))
        assertEquals(2L, r.mia?.id)
        assertEquals(listOf(1L, 3L), r.deOtros.map { it.id })
    }

    @Test
    fun sinResenaPropia() {
        assertNull(ResenasDeLibro(0.0, 0, emptyList()).mia)
    }

    @Test
    fun calificacionSoloDeUnoACinco() {
        assertTrue(ReglasComunidad.calificacionValida(1))
        assertTrue(ReglasComunidad.calificacionValida(5))
        assertFalse(ReglasComunidad.calificacionValida(0))
        assertFalse(ReglasComunidad.calificacionValida(6))
    }

    @Test
    fun inicialesDelLector() {
        assertEquals("SD", Lector(1, "Sofia Diaz").iniciales)
        assertEquals("C", Lector(1, "carlos").iniciales)
        assertEquals("AB", Lector(1, "Ana  Beltran Ruiz").iniciales)
    }

    @Test
    fun fechaRelativa() {
        val ahora = LocalDateTime.of(2026, 11, 18, 12, 0)
        assertEquals("ahora", FechaRelativa.desde("2026-11-18T11:59:40", ahora))
        assertEquals("hace 5 min", FechaRelativa.desde("2026-11-18T11:55:00", ahora))
        assertEquals("hace 3 h", FechaRelativa.desde("2026-11-18T09:00:00.123456", ahora))
        assertEquals("ayer", FechaRelativa.desde("2026-11-17T08:00:00", ahora))
        assertEquals("hace 5 días", FechaRelativa.desde("2026-11-13T12:00:00", ahora))
        assertEquals("01/01/2026", FechaRelativa.desde("2026-01-01T12:00:00", ahora))
        assertEquals("", FechaRelativa.desde("no-es-fecha", ahora))
    }

    @Test
    fun fechaUtcDelServidorSeConvierteALaHoraLocal() {
        val bogota = java.time.ZoneId.of("America/Bogota") // UTC-5
        // 03:55 UTC = 22:55 del dia anterior en Bogota; "ahora" en Bogota son las 23:00
        val ahoraBogota = LocalDateTime.of(2026, 11, 17, 23, 0)
        assertEquals("hace 5 min", FechaRelativa.desde("2026-11-18T03:55:00Z", ahoraBogota, bogota))
        assertEquals(LocalDateTime.of(2026, 11, 17, 22, 55), FechaRelativa.aLocal("2026-11-18T03:55Z", bogota))
    }
}
