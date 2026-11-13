package com.miestanteria.backend.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-mi-estanteria-con-32-caracteres-o-mas";

    @Test
    void tokenGeneradoSeValidaYDevuelveElUsuario() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generarToken(42L, "a@b.com");
        assertThat(jwt.validarYObtenerUsuario(token)).isEqualTo(42L);
    }

    @Test
    void tokenFirmadoConOtraClaveEsRechazado() {
        String token = new JwtService(SECRETO, 60).generarToken(1L, "a@b.com");
        JwtService otro = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres!!", 60);
        assertThatThrownBy(() -> otro.validarYObtenerUsuario(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenVencidoEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, -1); // caduco hace un minuto
        String token = jwt.generarToken(1L, "a@b.com");
        assertThatThrownBy(() -> jwt.validarYObtenerUsuario(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void claveCortaNoSePermite() {
        assertThatThrownBy(() -> new JwtService("corta", 60)).isInstanceOf(IllegalStateException.class);
    }
}
