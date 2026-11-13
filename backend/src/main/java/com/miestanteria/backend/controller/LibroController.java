package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.service.GoogleBooksService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Busqueda de libros en Google Books (RF03). */
@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final GoogleBooksService googleBooks;

    public LibroController(GoogleBooksService googleBooks) {
        this.googleBooks = googleBooks;
    }

    /** GET /api/v1/libros/buscar?q=texto&tipo=titulo|autor|categoria&pagina=0 */
    @GetMapping("/buscar")
    public List<LibroDto> buscar(@RequestParam("q") String q,
                                 @RequestParam(value = "tipo", required = false) String tipo,
                                 @RequestParam(value = "pagina", defaultValue = "0") int pagina) {
        return googleBooks.buscar(q, tipo, pagina);
    }

    /** GET /api/v1/libros/{apiId} - Detalle de un libro de Google Books. */
    @GetMapping("/{apiId}")
    public LibroDto detalle(@PathVariable String apiId) {
        return googleBooks.obtener(apiId);
    }
}
