package com.miestanteria.backend.security;

import com.miestanteria.backend.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utilidad para obtener el id del usuario autenticado en la peticion actual. */
public final class UsuarioActual {

    private UsuarioActual() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long id)) {
            throw ApiException.unauthorized("Debes iniciar sesion");
        }
        return id;
    }
}
