package com.miestanteria.backend;

import com.miestanteria.backend.repository.BibliotecaUsuarioRepository;
import com.miestanteria.backend.repository.LibroRepository;
import com.miestanteria.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integracion de RF04 (biblioteca de lectura) sobre H2 en memoria. */
@SpringBootTest
class BibliotecaControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository usuarios;
    @Autowired LibroRepository libros;
    @Autowired BibliotecaUsuarioRepository biblioteca;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        biblioteca.deleteAll();
        libros.deleteAll();
        usuarios.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String token(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"Lector\",\"email\":\"" + email + "\",\"password\":\"secreta123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private static final String LIBRO = """
            {"libro":{"apiId":"abc123","titulo":"El Principito","autores":"Antoine de Saint-Exupery",
             "paginas":96,"categoria":"Fiction","portadaUrl":"https://x/p.jpg"},
             "estadoLectura":"%s","paginaActual":%s}""";

    private long agregar(String auth, String estado, String pagina) throws Exception {
        String body = mvc.perform(post("/api/v1/biblioteca").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(LIBRO.formatted(estado, pagina)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }

    @Test
    void agregarYListarLibro() throws Exception {
        String auth = token("a@test.com");
        agregar(auth, "LEYENDO", "20");

        mvc.perform(get("/api/v1/biblioteca").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].libro.titulo").value("El Principito"))
                .andExpect(jsonPath("$[0].estadoLectura").value("LEYENDO"))
                .andExpect(jsonPath("$[0].paginaActual").value(20));
    }

    @Test
    void elMismoLibroNoSePuedeAgregarDosVeces() throws Exception {
        String auth = token("a@test.com");
        agregar(auth, "POR_LEER", "0");
        mvc.perform(post("/api/v1/biblioteca").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(LIBRO.formatted("LEYENDO", "5")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Este libro ya esta en tu biblioteca"));
        assertThat(libros.count()).isEqualTo(1);
    }

    @Test
    void dosUsuariosCompartenElMismoLibroSinDuplicarlo() throws Exception {
        agregar(token("a@test.com"), "POR_LEER", "0");
        agregar(token("b@test.com"), "LEIDO", "0");
        assertThat(libros.count()).isEqualTo(1);
        assertThat(biblioteca.count()).isEqualTo(2);
    }

    @Test
    void filtrarPorEstado() throws Exception {
        String auth = token("a@test.com");
        agregar(auth, "LEYENDO", "10");
        mvc.perform(get("/api/v1/biblioteca?estado=LEIDO").header("Authorization", auth))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/biblioteca?estado=LEYENDO").header("Authorization", auth))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/v1/biblioteca?estado=INVENTADO").header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarPaginaYMarcarComoLeido() throws Exception {
        String auth = token("a@test.com");
        long id = agregar(auth, "LEYENDO", "10");

        mvc.perform(put("/api/v1/biblioteca/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paginaActual\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaActual").value(50))
                .andExpect(jsonPath("$.estadoLectura").value("LEYENDO"));

        mvc.perform(put("/api/v1/biblioteca/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estadoLectura\":\"LEIDO\"}"))
                .andExpect(jsonPath("$.paginaActual").value(96));
    }

    @Test
    void paginaMayorAlTotalEsRechazada() throws Exception {
        String auth = token("a@test.com");
        long id = agregar(auth, "LEYENDO", "10");
        mvc.perform(put("/api/v1/biblioteca/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paginaActual\":500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El libro solo tiene 96 paginas"));
        mvc.perform(put("/api/v1/biblioteca/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paginaActual\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarLibro() throws Exception {
        String auth = token("a@test.com");
        long id = agregar(auth, "POR_LEER", "0");
        mvc.perform(delete("/api/v1/biblioteca/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/biblioteca").header("Authorization", auth)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void unUsuarioNoPuedeTocarLaBibliotecaDeOtro() throws Exception {
        long id = agregar(token("a@test.com"), "POR_LEER", "0");
        String otro = token("b@test.com");
        mvc.perform(put("/api/v1/biblioteca/" + id).header("Authorization", otro)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paginaActual\":5}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/biblioteca/" + id).header("Authorization", otro)).andExpect(status().isNotFound());
        assertThat(biblioteca.count()).isEqualTo(1);
    }

    @Test
    void sinTokenNoHayAcceso() throws Exception {
        mvc.perform(get("/api/v1/biblioteca")).andExpect(status().isUnauthorized());
    }
}
