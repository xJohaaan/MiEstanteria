package com.miestanteria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de la peticion PUT /api/v1/usuarios/me/password (cambiar contrasena). */
public record CambiarPasswordRequest(
        @NotBlank(message = "La contrasena actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 8, max = 100, message = "La contrasena debe tener al menos 8 caracteres")
        String passwordNueva
) {}
