package com.miestanteria.backend.dto;

import com.miestanteria.backend.model.Usuario;

/** Datos publicos de un usuario (nunca incluye el hash de la contrasena). */
public record UsuarioDto(
        Long id,
        String nombreUsuario,
        String email,
        String fotoPerfilUrl,
        String biografia,
        String fechaRegistro
) {
    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(
                u.getId(),
                u.getNombreUsuario(),
                u.getEmail(),
                u.getFotoPerfilUrl(),
                u.getBiografia(),
                Fechas.iso(u.getFechaRegistro())
        );
    }
}
