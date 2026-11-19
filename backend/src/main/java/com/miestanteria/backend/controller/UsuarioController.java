package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.ActualizarPerfilRequest;
import com.miestanteria.backend.dto.CambiarPasswordRequest;
import com.miestanteria.backend.dto.PerfilDto;
import com.miestanteria.backend.dto.UsuarioDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.UsuarioRepository;
import com.miestanteria.backend.security.UsuarioActual;
import com.miestanteria.backend.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Datos y perfil de usuarios (RF08). */
@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final PerfilService perfilService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarios, PerfilService perfilService, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.perfilService = perfilService;
        this.passwordEncoder = passwordEncoder;
    }

    /** GET /api/v1/usuarios/me - Datos del usuario de la sesion (la app lo usa para validar el token). */
    @GetMapping("/usuarios/me")
    public UsuarioDto yo() {
        return usuarios.findById(UsuarioActual.id())
                .map(UsuarioDto::from)
                .orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
    }

    /** PUT /api/v1/usuarios/me - Actualizar nombre y biografia del usuario autenticado. */
    @PutMapping("/usuarios/me")
    public UsuarioDto actualizarPerfil(@Valid @RequestBody ActualizarPerfilRequest req) {
        Usuario u = usuarios.findById(UsuarioActual.id())
                .orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
        if (req.nombreUsuario() != null && !req.nombreUsuario().isBlank()) {
            u.setNombreUsuario(req.nombreUsuario().trim());
        }
        if (req.biografia() != null) {
            u.setBiografia(req.biografia());
        }
        usuarios.save(u);
        return UsuarioDto.from(u);
    }

    /** PUT /api/v1/usuarios/me/password - Cambiar contrasena del usuario autenticado. */
    @PutMapping("/usuarios/me/password")
    public void cambiarPassword(@Valid @RequestBody CambiarPasswordRequest req) {
        Usuario u = usuarios.findById(UsuarioActual.id())
                .orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
        if (!passwordEncoder.matches(req.passwordActual(), u.getPasswordHash())) {
            throw ApiException.badRequest("La contrasena actual no es correcta");
        }
        u.setPasswordHash(passwordEncoder.encode(req.passwordNueva()));
        usuarios.save(u);
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
