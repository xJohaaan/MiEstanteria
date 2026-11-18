package com.example.miestanteria

import com.example.miestanteria.model.EstadoLectura
import com.example.miestanteria.model.ItemBiblioteca
import com.example.miestanteria.model.Libro
import com.example.miestanteria.ui.components.PaginaValidator
import com.example.miestanteria.ui.detail.BookDetailViewModel
import com.example.miestanteria.ui.shelf.OrdenEstanteria
import com.example.miestanteria.ui.shelf.ShelfViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Reglas de la biblioteca (RF04) que no dependen de Android. */
class BibliotecaLogicTest {

    private fun item(id: Long, titulo: String, autor: String, estado: EstadoLectura, pag: Int, total: Int?, fecha: String) =
        ItemBiblioteca(id, Libro("a$id", titulo, autor, paginas = total), estado, pag, fecha)

    private val items = listOf(
        item(1, "Cien años de soledad", "García Márquez", EstadoLectura.LEYENDO, 100, 400, "2026-11-01"),
        item(2, "El principito", "Saint-Exupéry", EstadoLectura.LEIDO, 96, 96, "2026-11-03"),
        item(3, "Dune", "Herbert", EstadoLectura.POR_LEER, 0, 600, "2026-11-02"),
        item(4, "Rayuela", "Cortázar", EstadoLectura.LEYENDO, 300, 600, "2026-10-30")
    )

    @Test
    fun progresoSeCalculaConLasPaginas() {
        assertEquals(0.25f, items[0].progreso!!, 0.001f)
        assertEquals(1f, items[1].progreso!!, 0.001f)
        assertNull(item(9, "X", "Y", EstadoLectura.LEYENDO, 10, null, "").progreso)
    }

    @Test
    fun filtrarPorEstado() {
        val leyendo = ShelfViewModel.aplicar(items, EstadoLectura.LEYENDO, OrdenEstanteria.RECIENTES)
        assertEquals(listOf(1L, 4L), leyendo.map { it.id })
    }

    @Test
    fun ordenarPorTituloYPorRecientes() {
        assertEquals(listOf("Cien años de soledad", "Dune", "El principito", "Rayuela"),
            ShelfViewModel.aplicar(items, null, OrdenEstanteria.TITULO).map { it.libro.titulo })
        assertEquals(listOf(2L, 3L, 1L, 4L), ShelfViewModel.aplicar(items, null, OrdenEstanteria.RECIENTES).map { it.id })
    }

    @Test
    fun ordenarPorProgreso() {
        assertEquals(listOf(2L, 4L, 1L, 3L), ShelfViewModel.aplicar(items, null, OrdenEstanteria.PROGRESO).map { it.id })
    }

    @Test
    fun contarPorEstado() {
        val c = ShelfViewModel.contar(items)
        assertEquals(4, c[null])
        assertEquals(2, c[EstadoLectura.LEYENDO])
        assertEquals(1, c[EstadoLectura.LEIDO])
        assertEquals(1, c[EstadoLectura.POR_LEER])
    }

    @Test
    fun validacionDePagina() {
        assertNull(PaginaValidator.validar("50", 100))
        assertNull(PaginaValidator.validar("", 100))
        assertEquals("El libro tiene 100 páginas", PaginaValidator.validar("150", 100))
        assertNotNull(PaginaValidator.validar("abc", 100))
        assertNull(PaginaValidator.validar("9999", null))
    }

    @Test
    fun sinopsisHtmlSeConvierteATextoPlano() {
        assertEquals("Hola\nmundo & \"todos\"",
            BookDetailViewModel.textoPlano("<p><b>Hola</b><br>mundo &amp; &quot;todos&quot;</p>"))
    }

    @Test
    fun combinarDetalleConservaDatosLocales() {
        val local = Libro("x", "Titulo", "Autor real", paginas = 200)
        val remoto = Libro("x", "Titulo", "Autor desconocido", descripcion = "Sinopsis", paginas = 999)
        val r = BookDetailViewModel.combinar(local, remoto)
        assertEquals("Autor real", r.autores)
        assertEquals("Sinopsis", r.descripcion)
        assertEquals(200, r.paginas)
    }

    @Test
    fun anioSeExtraeDeDistintosFormatos() {
        assertEquals("2018", Libro("x", "t", "a", fechaPublicacion = "2018-05-15").anio)
        assertEquals("2023", Libro("x", "t", "a", fechaPublicacion = "May 2023").anio)
        assertNull(Libro("x", "t", "a").anio)
    }
}
