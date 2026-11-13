package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.AuthDtos.AuthResponse;
import com.miestanteria.backend.dto.AuthDtos.LoginRequest;
import com.miestanteria.backend.dto.AuthDtos.RegisterRequest;
import com.miestanteria.backend.dto.UsuarioDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.EstadoUsuario;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.UsuarioRepository;
import com.miestanteria.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Logica de registro (RF01) e inicio de sesion (RF02). */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Crea la cuenta si el correo no existe y devuelve un token de sesion. */
    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        String email = normalizarEmail(req.email());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombreUsuario(req.nombreUsuario().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setEstado(EstadoUsuario.ACTIVO);
        usuarios.save(u);
        return respuestaPara(u);
    }

    /** Valida correo y contrasena. Responde 401 sin revelar cual de los dos fallo. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(normalizarEmail(req.email()))
                .orElseThrow(() -> ApiException.unauthorized("Correo o contrasena incorrectos"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Correo o contrasena incorrectos");
        }
        if (u.getEstado() == EstadoUsuario.INACTIVO) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta esta deshabilitada");
        }
        return respuestaPara(u);
    }

    private AuthResponse respuestaPara(Usuario u) {
        String token = jwtService.generarToken(u.getId(), u.getEmail());
        return new AuthResponse(token, jwtService.getExpirationSeconds(), UsuarioDto.from(u));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
