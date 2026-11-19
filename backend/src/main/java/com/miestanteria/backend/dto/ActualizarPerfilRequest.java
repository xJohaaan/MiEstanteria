package com.miestanteria.backend.dto;

import jakarta.validation.constraints.Size;

/** Cuerpo de la peticion PUT /api/v1/usuarios/me (actualizar nombre y biografia). */
public record ActualizarPerfilRequest(
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombreUsuario,

        @Size(max = 2000, message = "La biografia no puede superar los 2000 caracteres")
        String biografia
) {}
