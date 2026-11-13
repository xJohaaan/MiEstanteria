package com.miestanteria.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objetos que viajan en las peticiones de registro e inicio de sesion. */
public final class AuthDtos {

    private AuthDtos() {}

    /** Longitud minima de la contrasena (RF01). */
    public static final int MIN_PASSWORD = 8;

    public record RegisterRequest(
            @NotBlank(message = "El nombre de usuario es obligatorio")
            @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
            String nombreUsuario,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es valido")
            @Size(max = 150, message = "El correo es demasiado largo")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = MIN_PASSWORD, max = 100, message = "La contrasena debe tener al menos 8 caracteres")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es valido")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            String password
    ) {}

    /** Respuesta de login/registro: token JWT + datos basicos del usuario. */
    public record AuthResponse(String token, long expiraEnSegundos, UsuarioDto usuario) {}
}
