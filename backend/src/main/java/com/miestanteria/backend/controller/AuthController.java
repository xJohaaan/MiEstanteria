package com.miestanteria.backend.controller;

import com.miestanteria.backend.dto.AuthDtos.AuthResponse;
import com.miestanteria.backend.dto.AuthDtos.LoginRequest;
import com.miestanteria.backend.dto.AuthDtos.RegisterRequest;
import com.miestanteria.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Endpoints publicos de autenticacion. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/v1/auth/register - Crear una cuenta nueva. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.registrar(req);
    }

    /** POST /api/v1/auth/login - Iniciar sesion y obtener el token. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
