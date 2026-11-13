package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.UsuarioDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.repository.UsuarioRepository;
import com.miestanteria.backend.security.UsuarioActual;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Datos de usuarios. Las estadisticas del perfil (RF08) se agregan en el Sprint de perfil. */
@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** GET /api/v1/usuarios/me - Datos del usuario de la sesion (la app lo usa para validar el token). */
    @GetMapping("/usuarios/me")
    public UsuarioDto yo() {
        return usuarios.findById(UsuarioActual.id())
                .map(UsuarioDto::from)
                .orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
    }

    /** GET /api/v1/salud - Comprobacion rapida de que el servidor esta arriba. */
    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}
