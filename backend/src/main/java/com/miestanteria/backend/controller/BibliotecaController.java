package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.BibliotecaDtos.ActualizarRequest;
import com.miestanteria.backend.dto.BibliotecaDtos.AgregarRequest;
import com.miestanteria.backend.dto.BibliotecaDtos.BibliotecaItemDto;
import com.miestanteria.backend.model.EstadoLectura;
import com.miestanteria.backend.security.UsuarioActual;
import com.miestanteria.backend.service.BibliotecaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Biblioteca personal del usuario autenticado (RF04). */
@RestController
@RequestMapping("/api/v1/biblioteca")
public class BibliotecaController {

    private final BibliotecaService service;

    public BibliotecaController(BibliotecaService service) {
        this.service = service;
    }

    /** GET /api/v1/biblioteca?estado=LEYENDO - Ver la biblioteca (opcionalmente filtrada por estado). */
    @GetMapping
    public List<BibliotecaItemDto> listar(@RequestParam(value = "estado", required = false) EstadoLectura estado) {
        return service.listar(UsuarioActual.id(), estado);
    }

    /** POST /api/v1/biblioteca - Agregar un libro con su estado de lectura. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BibliotecaItemDto agregar(@Valid @RequestBody AgregarRequest req) {
        return service.agregar(UsuarioActual.id(), req);
    }

    /** PUT /api/v1/biblioteca/{id} - Cambiar el estado de lectura o la pagina actual. */
    @PutMapping("/{id}")
    public BibliotecaItemDto actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarRequest req) {
        return service.actualizar(UsuarioActual.id(), id, req);
    }

    /** DELETE /api/v1/biblioteca/{id} - Quitar un libro de la biblioteca. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(UsuarioActual.id(), id);
    }
}
