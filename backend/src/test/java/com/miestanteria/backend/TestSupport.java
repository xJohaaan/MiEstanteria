package com.miestanteria.backend;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Utilidades compartidas por las pruebas de integracion. */
final class TestSupport {

    private TestSupport() {}

    private static final String[] TABLAS = {
            "me_gusta_resenas", "recomendaciones", "resenas", "biblioteca_usuario", "libros", "usuarios"
    };

    /** Deja la base H2 vacia antes de cada prueba. */
    static void limpiarBd(JdbcTemplate jdbc) {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String t : TABLAS) jdbc.execute("TRUNCATE TABLE " + t);
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /** Registra un usuario y devuelve la cabecera "Bearer token". */
    static String registrar(MockMvc mvc, String nombre, String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"" + nombre + "\",\"email\":\"" + email + "\",\"password\":\"secreta123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    static long idUsuario(String authRespuesta) {
        return Long.parseLong(authRespuesta.replaceAll(".*\"usuario\":\\{\"id\":(\\d+).*", "$1"));
    }

    static long primerId(String json) {
        return Long.parseLong(json.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }
}
