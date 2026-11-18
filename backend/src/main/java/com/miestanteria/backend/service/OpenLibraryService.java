package com.miestanteria.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Fuente de respaldo: Open Library (https://openlibrary.org), API publica y gratuita.
 * Se usa cuando Google Books no responde o limita las consultas, para que la busqueda siga funcionando.
 * Sus identificadores tienen la forma "OL12345W".
 */
@Service
public class OpenLibraryService {

    private static final Pattern ID_OPEN_LIBRARY = Pattern.compile("^OL\\d+W$");
    private static final int RESULTADOS_POR_PAGINA = 20;
    // Sin "subject": esa lista es enorme y hace la respuesta mucho mas lenta
    private static final String CAMPOS = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median,publisher";

    private final RestClient client;

    public OpenLibraryService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Open Library es gratuita pero lenta: algunas busquedas tardan mas de 10 segundos
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(20000);
        this.client = RestClient.builder()
                .baseUrl("https://openlibrary.org")
                .defaultHeader("User-Agent", "MiEstanteria/1.0 (proyecto academico)")
                .requestFactory(factory)
                .build();
    }

    public static boolean esIdOpenLibrary(String apiId) {
        return apiId != null && ID_OPEN_LIBRARY.matcher(apiId).matches();
    }

    public List<LibroDto> buscar(String texto, String tipo, int pagina) {
        String param = switch (tipo == null ? "" : tipo) {
            case "titulo" -> "title";
            case "autor" -> "author";
            case "categoria" -> "subject";
            default -> "q";
        };
        try {
            SearchResponse resp = client.get()
                    .uri(b -> b.path("/search.json")
                            .queryParam(param, texto.trim())
                            .queryParam("limit", RESULTADOS_POR_PAGINA)
                            .queryParam("page", Math.max(pagina, 0) + 1)
                            .queryParam("fields", CAMPOS)
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
            if (resp == null || resp.docs() == null) return List.of();
            return resp.docs().stream().filter(d -> d.key() != null && d.title() != null)
                    .map(OpenLibraryService::aLibroDto).toList();
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo consultar el catalogo de libros. Intenta de nuevo.");
        }
    }

    /** Detalle de una obra. Open Library no trae autores en este recurso, solo titulo, sinopsis y temas. */
    @SuppressWarnings("unchecked")
    public LibroDto obtener(String apiId) {
        try {
            Map<String, Object> w = client.get()
                    .uri((UriBuilder b) -> b.path("/works/{id}.json").build(apiId))
                    .retrieve()
                    .body(Map.class);
            if (w == null) throw ApiException.notFound("Libro no encontrado");
            Object desc = w.get("description");
            String descripcion = desc instanceof Map<?, ?> m ? String.valueOf(m.get("value")) : (desc != null ? desc.toString() : null);
            List<Object> covers = (List<Object>) w.get("covers");
            List<Object> subjects = (List<Object>) w.get("subjects");
            return new LibroDto(null, apiId, String.valueOf(w.getOrDefault("title", "Sin titulo")), "Autor desconocido",
                    null, null,
                    covers != null && !covers.isEmpty() ? portada(covers.get(0)) : null,
                    descripcion,
                    subjects != null && !subjects.isEmpty() ? recortar(String.valueOf(subjects.get(0)), 50) : null,
                    null);
        } catch (RestClientException e) {
            throw ApiException.notFound("Libro no encontrado");
        }
    }

    static LibroDto aLibroDto(Doc d) {
        String id = d.key().replace("/works/", "");
        String autores = d.authorName() == null || d.authorName().isEmpty() ? "Autor desconocido"
                : String.join(", ", d.authorName().stream().limit(3).toList());
        return new LibroDto(
                null, id, recortar(d.title(), 255), recortar(autores, 255),
                d.publisher() != null && !d.publisher().isEmpty() ? recortar(d.publisher().get(0), 150) : null,
                d.firstPublishYear() != null ? String.valueOf(d.firstPublishYear()) : null,
                d.coverI() != null ? portada(d.coverI()) : null,
                null,
                d.subject() != null && !d.subject().isEmpty() ? recortar(d.subject().get(0), 50) : null,
                d.numberOfPagesMedian()
        );
    }

    private static String portada(Object coverId) {
        return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
    }

    private static String recortar(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResponse(List<Doc> docs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Doc(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorName,
            @JsonProperty("cover_i") Long coverI,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
            @JsonProperty("number_of_pages_median") Integer numberOfPagesMedian,
            List<String> subject,
            List<String> publisher
    ) {}
}
