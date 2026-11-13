package com.miestanteria.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/** Genera y valida los tokens JWT con los que la app se autentica en cada peticion. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationMinutes * 60;
    }

    public long getExpirationSeconds() { return expirationSeconds; }

    /** Crea un token cuyo "subject" es el id del usuario. */
    public String generarToken(Long idUsuario, String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim("email", email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * Devuelve el id del usuario si el token es valido y no ha caducado.
     * @throws JwtException si el token esta alterado o vencido
     */
    public Long validarYObtenerUsuario(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
