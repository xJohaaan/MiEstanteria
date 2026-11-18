package com.miestanteria.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integracion de RF05 (resenas), RF06 (me gusta) y RF07 (recomendaciones). */
@SpringBootTest
class ComunidadControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;

    String sofia;
    String carlos;

    private static final String LIBRO = """
            {"apiId":"abc123","titulo":"El Principito","autores":"Saint-Exupery","paginas":96}""";

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.limpiarBd(jdbc);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        sofia = TestSupport.registrar(mvc, "Sofia Diaz", "sofia@test.com");
        carlos = TestSupport.registrar(mvc, "Carlos Ruiz", "carlos@test.com");
    }

    private void agregarALaBiblioteca(String auth) throws Exception {
        mvc.perform(post("/api/v1/biblioteca").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"estadoLectura\":\"LEIDO\"}"))
                .andExpect(status().isCreated());
    }

    private long resenar(String auth, int estrellas, String comentario) throws Exception {
        String body = mvc.perform(post("/api/v1/resenas").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":" + estrellas + ",\"comentario\":\"" + comentario + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return TestSupport.primerId(body);
    }

    private long idDe(String auth) throws Exception {
        String body = mvc.perform(get("/api/v1/usuarios/me").header("Authorization", auth))
                .andReturn().getResponse().getContentAsString();
        return TestSupport.primerId(body);
    }

    // ---------- RF05: resenas ----------

    @Test
    void publicarResenaYVerlaConPromedio() throws Exception {
        agregarALaBiblioteca(sofia);
        agregarALaBiblioteca(carlos);
        resenar(sofia, 5, "Precioso");
        resenar(carlos, 4, "Muy bueno");

        mvc.perform(get("/api/v1/libros/abc123/resenas").header("Authorization", sofia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.promedio").value(4.5))
                .andExpect(jsonPath("$.resenas", hasSize(2)));
    }

    @Test
    void noSePuedeResenarUnLibroQueNoEstaEnLaBiblioteca() throws Exception {
        mvc.perform(post("/api/v1/resenas").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Agrega el libro a tu biblioteca para poder resenarlo"));
    }

    @Test
    void calificacionFueraDeRangoYComentarioLargoSonRechazados() throws Exception {
        agregarALaBiblioteca(sofia);
        mvc.perform(post("/api/v1/resenas").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":6}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/resenas").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":0}"))
                .andExpect(status().isBadRequest());
        String largo = "a".repeat(501);
        mvc.perform(post("/api/v1/resenas").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":4,\"comentario\":\"" + largo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El comentario puede tener maximo 500 caracteres"));
    }

    @Test
    void soloUnaResenaPorLibro() throws Exception {
        agregarALaBiblioteca(sofia);
        resenar(sofia, 5, "Primera");
        mvc.perform(post("/api/v1/resenas").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":" + LIBRO + ",\"calificacion\":3}"))
                .andExpect(status().isConflict());
    }

    @Test
    void editarYEliminarResenaPropia() throws Exception {
        agregarALaBiblioteca(sofia);
        long id = resenar(sofia, 3, "Regular");
        mvc.perform(put("/api/v1/resenas/" + id).header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":5,\"comentario\":\"Cambie de opinion\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.comentario").value("Cambie de opinion"));
        mvc.perform(delete("/api/v1/resenas/" + id).header("Authorization", sofia)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/libros/abc123/resenas").header("Authorization", sofia))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void nadiePuedeEditarNiBorrarLaResenaDeOtro() throws Exception {
        agregarALaBiblioteca(sofia);
        long id = resenar(sofia, 5, "Mia");
        mvc.perform(put("/api/v1/resenas/" + id).header("Authorization", carlos).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/resenas/" + id).header("Authorization", carlos)).andExpect(status().isForbidden());
    }

    // ---------- RF06: me gusta ----------

    @Test
    void darYQuitarMeGusta() throws Exception {
        agregarALaBiblioteca(sofia);
        long id = resenar(sofia, 5, "Hermoso");

        mvc.perform(post("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meGustas").value(1))
                .andExpect(jsonPath("$.meGustaMio").value(true));
        // Repetirlo no duplica
        mvc.perform(post("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos))
                .andExpect(jsonPath("$.meGustas").value(1));
        mvc.perform(get("/api/v1/libros/abc123/resenas").header("Authorization", carlos))
                .andExpect(jsonPath("$.resenas[0].meGustas").value(1))
                .andExpect(jsonPath("$.resenas[0].meGustaMio").value(true))
                .andExpect(jsonPath("$.resenas[0].esMia").value(false));

        mvc.perform(delete("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos))
                .andExpect(jsonPath("$.meGustas").value(0))
                .andExpect(jsonPath("$.meGustaMio").value(false));
    }

    @Test
    void noSePuedeDarMeGustaALaPropiaResena() throws Exception {
        agregarALaBiblioteca(sofia);
        long id = resenar(sofia, 5, "Mia");
        mvc.perform(post("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", sofia))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No puedes dar Me gusta a tu propia resena"));
    }

    // ---------- RF07: recomendaciones ----------

    @Test
    void recomendarLibroYVerloEnElFeedDelReceptor() throws Exception {
        long idCarlos = idDe(carlos);
        mvc.perform(post("/api/v1/recomendaciones").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receptorId\":" + idCarlos + ",\"libro\":" + LIBRO + ",\"mensaje\":\"Te va a encantar\"}"))
                .andExpect(status().isCreated());

        String feed = mvc.perform(get("/api/v1/feed").header("Authorization", carlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noVistas").value(1))
                .andExpect(jsonPath("$.recomendaciones[0].mensaje").value("Te va a encantar"))
                .andExpect(jsonPath("$.recomendaciones[0].emisor.nombreUsuario").value("Sofia Diaz"))
                .andReturn().getResponse().getContentAsString();

        long idRec = Long.parseLong(feed.replaceAll(".*\"recomendaciones\":\\[\\{\"id\":(\\d+).*", "$1"));
        mvc.perform(put("/api/v1/recomendaciones/" + idRec + "/visto").header("Authorization", carlos))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/feed").header("Authorization", carlos)).andExpect(jsonPath("$.noVistas").value(0));
    }

    @Test
    void noSePuedeRecomendarASiMismo() throws Exception {
        long idSofia = idDe(sofia);
        mvc.perform(post("/api/v1/recomendaciones").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receptorId\":" + idSofia + ",\"libro\":" + LIBRO + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No puedes recomendarte un libro a ti mismo"));
    }

    @Test
    void mensajeDeRecomendacionMaximo300() throws Exception {
        long idCarlos = idDe(carlos);
        mvc.perform(post("/api/v1/recomendaciones").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receptorId\":" + idCarlos + ",\"libro\":" + LIBRO + ",\"mensaje\":\"" + "x".repeat(301) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloElReceptorPuedeMarcarComoVista() throws Exception {
        long idCarlos = idDe(carlos);
        String body = mvc.perform(post("/api/v1/recomendaciones").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receptorId\":" + idCarlos + ",\"libro\":" + LIBRO + "}"))
                .andReturn().getResponse().getContentAsString();
        long idRec = TestSupport.primerId(body);
        mvc.perform(put("/api/v1/recomendaciones/" + idRec + "/visto").header("Authorization", sofia))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarUsuariosExcluyeAlPropio() throws Exception {
        mvc.perform(get("/api/v1/usuarios/buscar?q=ruiz").header("Authorization", sofia))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreUsuario").value("Carlos Ruiz"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
        mvc.perform(get("/api/v1/usuarios/buscar?q=sofia").header("Authorization", sofia))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/v1/usuarios/buscar?q=a").header("Authorization", sofia))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void quitarMeGustaDosVecesNoDaError() throws Exception {
        agregarALaBiblioteca(sofia);
        long id = resenar(sofia, 5, "Hermoso");
        mvc.perform(post("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/resenas/" + id + "/me-gusta").header("Authorization", carlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meGustas").value(0));
    }

    // ---------- RF08: perfil ----------

    @Test
    void perfilConEstadisticasReales() throws Exception {
        agregarALaBiblioteca(sofia); // LEIDO, 96 paginas
        mvc.perform(post("/api/v1/biblioteca").header("Authorization", sofia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":{\"apiId\":\"dune\",\"titulo\":\"Dune\",\"paginas\":600,\"categoria\":\"Ciencia ficcion\"},"
                                + "\"estadoLectura\":\"LEYENDO\",\"paginaActual\":150}"))
                .andExpect(status().isCreated());
        long idResena = resenar(sofia, 4, "Lindo");
        mvc.perform(post("/api/v1/resenas/" + idResena + "/me-gusta").header("Authorization", carlos));
        long idSofia = idDe(sofia);

        mvc.perform(get("/api/v1/usuarios/" + idSofia).header("Authorization", sofia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esMio").value(true))
                .andExpect(jsonPath("$.estadisticas.leidos").value(1))
                .andExpect(jsonPath("$.estadisticas.leyendo").value(1))
                .andExpect(jsonPath("$.estadisticas.paginasLeidas").value(96 + 150))
                .andExpect(jsonPath("$.estadisticas.resenasEscritas").value(1))
                .andExpect(jsonPath("$.estadisticas.promedioCalificacion").value(4.0))
                .andExpect(jsonPath("$.estadisticas.meGustasRecibidos").value(1))
                .andExpect(jsonPath("$.generosFavoritos[0].etiqueta").value("Ciencia ficcion"))
                .andExpect(jsonPath("$.leidosPorAnio[0].cantidad").value(1))
                .andExpect(jsonPath("$.resenas", hasSize(1)));

        // Visto por otro lector: es publico pero no es suyo y no muestra el correo
        mvc.perform(get("/api/v1/usuarios/" + idSofia).header("Authorization", carlos))
                .andExpect(jsonPath("$.esMio").value(false))
                .andExpect(jsonPath("$.nombreUsuario").value("Sofia Diaz"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void perfilInexistenteDa404() throws Exception {
        mvc.perform(get("/api/v1/usuarios/999999").header("Authorization", sofia)).andExpect(status().isNotFound());
    }

    @Test
    void feedMuestraResenasRecientes() throws Exception {
        agregarALaBiblioteca(sofia);
        resenar(sofia, 4, "Lindo");
        mvc.perform(get("/api/v1/feed").header("Authorization", carlos))
                .andExpect(jsonPath("$.resenasRecientes", hasSize(1)))
                .andExpect(jsonPath("$.resenasRecientes[0].autor.nombreUsuario").value("Sofia Diaz"));
    }
}
