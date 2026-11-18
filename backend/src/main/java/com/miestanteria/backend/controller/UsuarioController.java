package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.PerfilDto;
import com.miestanteria.backend.dto.UsuarioDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.repository.UsuarioRepository;
import com.miestanteria.backend.security.UsuarioActual;
import com.miestanteria.backend.service.PerfilService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Datos y perfil de usuarios (RF08). */
@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final PerfilService perfilService;

    public UsuarioController(UsuarioRepository usuarios, PerfilService perfilService) {
        this.usuarios = usuarios;
        this.perfilService = perfilService;
    }

    /** GET /api/v1/usuarios/me - Datos del usuario de la sesion (la app lo usa para validar el token). */
    @GetMapping("/usuarios/me")
    public UsuarioDto yo() {
        return usuarios.findById(UsuarioActual.id())
                .map(UsuarioDto::from)
                .orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
    }

    /** GET /api/v1/usuarios/{id} - Perfil publico con estadisticas de lectura y resenas. */
    @GetMapping("/usuarios/{id}")
    public PerfilDto perfil(@PathVariable Long id) {
        return perfilService.perfil(UsuarioActual.id(), id);
    }

    /** GET /api/v1/salud - Comprobacion rapida de que el servidor esta arriba. */
    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}
