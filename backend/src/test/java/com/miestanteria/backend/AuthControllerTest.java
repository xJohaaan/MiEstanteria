package com.miestanteria.backend;

import com.miestanteria.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integracion de RF01 (registro) y RF02 (inicio de sesion) sobre H2 en memoria. */
@SpringBootTest
class AuthControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository usuarios;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        usuarios.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String registro(String nombre, String email, String pass) {
        return """
                {"nombreUsuario":"%s","email":"%s","password":"%s"}""".formatted(nombre, email, pass);
    }

    @Test
    void registroValidoDevuelveTokenYGuardaPasswordCifrada() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Sofia", "Sofia@Correo.com", "secreta123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value("sofia@correo.com"))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());

        var u = usuarios.findByEmailIgnoreCase("sofia@correo.com").orElseThrow();
        assertThat(u.getPasswordHash()).isNotEqualTo("secreta123").startsWith("$2");
    }

    @Test
    void registroConCorreoRepetidoDevuelve409() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(registro("Sofia", "sofia@correo.com", "secreta123"))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Otra", "SOFIA@correo.com", "otraclave1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya existe una cuenta con ese correo"));
    }

    @Test
    void registroConContrasenaCortaDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Sofia", "sofia@correo.com", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La contrasena debe tener al menos 8 caracteres"));
    }

    @Test
    void registroConCorreoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Sofia", "no-es-correo", "secreta123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginCorrectoYAccesoARutaProtegida() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(registro("Sofia", "sofia@correo.com", "secreta123"))).andExpect(status().isCreated());

        MvcResult res = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"sofia@correo.com\",\"password\":\"secreta123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Sofia"));
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(registro("Sofia", "sofia@correo.com", "secreta123"))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"sofia@correo.com\",\"password\":\"equivocada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contrasena incorrectos"));
    }

    @Test
    void rutaProtegidaSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }
}
