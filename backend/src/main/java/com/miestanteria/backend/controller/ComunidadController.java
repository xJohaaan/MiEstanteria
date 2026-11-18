package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.ComunidadDtos.FeedDto;
import com.miestanteria.backend.dto.ComunidadDtos.RecomendacionDto;
import com.miestanteria.backend.dto.ComunidadDtos.RecomendarRequest;
import com.miestanteria.backend.dto.ComunidadDtos.UsuarioResumen;
import com.miestanteria.backend.security.UsuarioActual;
import com.miestanteria.backend.service.ComunidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Recomendaciones (RF07), muro de actividad y busqueda de lectores. */
@RestController
@RequestMapping("/api/v1")
public class ComunidadController {

    private final ComunidadService service;

    public ComunidadController(ComunidadService service) {
        this.service = service;
    }

    /** POST /api/v1/recomendaciones - Recomendar un libro a otro usuario. */
    @PostMapping("/recomendaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public RecomendacionDto recomendar(@Valid @RequestBody RecomendarRequest req) {
        return service.recomendar(UsuarioActual.id(), req);
    }

    /** GET /api/v1/recomendaciones/recibidas - Recomendaciones que me enviaron. */
    @GetMapping("/recomendaciones/recibidas")
    public List<RecomendacionDto> recibidas() {
        return service.recibidas(UsuarioActual.id());
    }

    /** PUT /api/v1/recomendaciones/{id}/visto - Marcar una recomendacion como vista. */
    @PutMapping("/recomendaciones/{id}/visto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarVista(@PathVariable Long id) {
        service.marcarVista(UsuarioActual.id(), id);
    }

    /** GET /api/v1/feed - Actividad: recomendaciones recibidas y resenas recientes. */
    @GetMapping("/feed")
    public FeedDto feed() {
        return service.feed(UsuarioActual.id());
    }

    /** GET /api/v1/usuarios/buscar?q=texto - Buscar lectores por nombre. */
    @GetMapping("/usuarios/buscar")
    public List<UsuarioResumen> buscarUsuarios(@RequestParam("q") String q) {
        return service.buscarUsuarios(UsuarioActual.id(), q);
    }
}
