package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.service.CatalogoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Busqueda de libros (RF03): Google Books, con Open Library como respaldo. */
@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

    private final CatalogoService catalogo;

    public LibroController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    /** GET /api/v1/libros/buscar?q=texto&tipo=titulo|autor|categoria&pagina=0&anioDesde=2000&anioHasta=2020 */
    @GetMapping("/buscar")
    public List<LibroDto> buscar(@RequestParam("q") String q,
                                 @RequestParam(value = "tipo", required = false) String tipo,
                                 @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                                 @RequestParam(value = "anioDesde", required = false) Integer anioDesde,
                                 @RequestParam(value = "anioHasta", required = false) Integer anioHasta) {
        return catalogo.buscar(q, tipo, pagina, anioDesde, anioHasta);
    }

    /** GET /api/v1/libros/{apiId} - Detalle de un libro. */
    @GetMapping("/{apiId}")
    public LibroDto detalle(@PathVariable String apiId) {
        return catalogo.obtener(apiId);
    }
}
