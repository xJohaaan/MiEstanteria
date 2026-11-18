package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.ComunidadDtos.ActualizarResenaRequest;
import com.miestanteria.backend.dto.ComunidadDtos.CrearResenaRequest;
import com.miestanteria.backend.dto.ComunidadDtos.MeGustaDto;
import com.miestanteria.backend.dto.ComunidadDtos.ResenaDto;
import com.miestanteria.backend.dto.ComunidadDtos.ResenasLibroDto;
import com.miestanteria.backend.security.UsuarioActual;
import com.miestanteria.backend.service.ResenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Resenas (RF05) y "Me gusta" (RF06). */
@RestController
@RequestMapping("/api/v1")
public class ResenaController {

    private final ResenaService service;

    public ResenaController(ResenaService service) {
        this.service = service;
    }

    /** POST /api/v1/resenas - Publicar una resena con calificacion. */
    @PostMapping("/resenas")
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaDto crear(@Valid @RequestBody CrearResenaRequest req) {
        return service.crear(UsuarioActual.id(), req);
    }

    /** PUT /api/v1/resenas/{id} - Editar una resena propia. */
    @PutMapping("/resenas/{id}")
    public ResenaDto actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarResenaRequest req) {
        return service.actualizar(UsuarioActual.id(), id, req);
    }

    /** DELETE /api/v1/resenas/{id} - Eliminar una resena propia. */
    @DeleteMapping("/resenas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(UsuarioActual.id(), id);
    }

    /** GET /api/v1/libros/{apiId}/resenas - Resenas de un libro con su promedio. */
    @GetMapping("/libros/{apiId}/resenas")
    public ResenasLibroDto deLibro(@PathVariable String apiId) {
        return service.deLibro(UsuarioActual.id(), apiId);
    }

    /** GET /api/v1/usuarios/{id}/resenas - Resenas escritas por un usuario. */
    @GetMapping("/usuarios/{id}/resenas")
    public List<ResenaDto> deUsuario(@PathVariable Long id) {
        return service.deUsuario(UsuarioActual.id(), id);
    }

    /** POST /api/v1/resenas/{id}/me-gusta - Dar Me gusta. */
    @PostMapping("/resenas/{id}/me-gusta")
    public MeGustaDto darMeGusta(@PathVariable Long id) {
        return service.darMeGusta(UsuarioActual.id(), id);
    }

    /** DELETE /api/v1/resenas/{id}/me-gusta - Quitar el Me gusta. */
    @DeleteMapping("/resenas/{id}/me-gusta")
    public MeGustaDto quitarMeGusta(@PathVariable Long id) {
        return service.quitarMeGusta(UsuarioActual.id(), id);
    }
}
