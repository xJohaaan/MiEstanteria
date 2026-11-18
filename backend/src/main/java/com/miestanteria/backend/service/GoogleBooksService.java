package com.miestanteria.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;

/**
 * Cliente de la API publica de Google Books (RF03).
 * El servidor hace de intermediario para que la app no dependa directamente de Google.
 */
@Service
public class GoogleBooksService {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksService.class);
    private static final int RESULTADOS_POR_PAGINA = 20;

    private final RestClient client;
    private final String apiKey;

    public GoogleBooksService(@Value("${app.google-books.api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.client = RestClient.builder()
                .baseUrl("https://www.googleapis.com/books/v1")
                .requestFactory(factory)
                .build();
        this.apiKey = apiKey;
    }

    /**
     * Busca libros.
     * @param texto  lo que escribio el usuario
     * @param tipo   "titulo", "autor", "categoria" o cualquier otro valor para busqueda general
     * @param pagina numero de pagina (empieza en 0)
     */
    public List<LibroDto> buscar(String texto, String tipo, int pagina) {
        if (texto == null || texto.isBlank()) {
            throw ApiException.badRequest("Escribe algo para buscar");
        }
        String q = switch (tipo == null ? "" : tipo) {
            case "titulo" -> "intitle:" + texto.trim();
            case "autor" -> "inauthor:" + texto.trim();
            case "categoria" -> "subject:" + texto.trim();
            default -> texto.trim();
        };
        try {
            VolumesResponse resp = client.get()
                    .uri(b -> construirUri(b, q, Math.max(pagina, 0)))
                    .retrieve()
                    .body(VolumesResponse.class);
            if (resp == null || resp.items() == null) return List.of();
            return resp.items().stream().map(GoogleBooksService::aLibroDto).toList();
        } catch (RestClientException e) {
            String detalle = e.getMessage() == null ? "" : e.getMessage();
            log.warn("Fallo la consulta a Google Books: {}", detalle.length() > 80 ? detalle.substring(0, 80) + "..." : detalle);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo consultar Google Books. Intenta de nuevo.");
        }
    }

    /** Obtiene un volumen concreto por su id de Google Books. */
    /** Sin clave, Google Books da cuota 0 por dia: no vale la pena intentarlo. */
    public boolean tieneClave() {
        return !apiKey.isBlank();
    }

    public LibroDto obtener(String apiId) {
        try {
            Volume v = client.get()
                    .uri(b -> {
                        UriBuilder ub = b.path("/volumes/{id}");
                        if (!apiKey.isBlank()) ub.queryParam("key", apiKey);
                        return ub.build(apiId);
                    })
                    .retrieve()
                    .body(Volume.class);
            if (v == null) throw ApiException.notFound("Libro no encontrado");
            return aLibroDto(v);
        } catch (RestClientException e) {
            throw ApiException.notFound("Libro no encontrado en Google Books");
        }
    }

    private URI construirUri(UriBuilder b, String q, int pagina) {
        UriBuilder ub = b.path("/volumes")
                .queryParam("q", q)
                .queryParam("maxResults", RESULTADOS_POR_PAGINA)
                .queryParam("startIndex", pagina * RESULTADOS_POR_PAGINA)
                .queryParam("printType", "books");
        if (!apiKey.isBlank()) ub.queryParam("key", apiKey);
        return ub.build();
    }

    static LibroDto aLibroDto(Volume v) {
        VolumeInfo i = v.volumeInfo() != null ? v.volumeInfo() : new VolumeInfo(null, null, null, null, null, null, null, null);
        String autores = (i.authors() == null || i.authors().isEmpty()) ? "Autor desconocido" : String.join(", ", i.authors());
        String categoria = (i.categories() == null || i.categories().isEmpty()) ? null : recortar(i.categories().get(0), 50);
        String portada = null;
        if (i.imageLinks() != null) {
            portada = i.imageLinks().thumbnail() != null ? i.imageLinks().thumbnail() : i.imageLinks().smallThumbnail();
            if (portada != null) portada = portada.replace("http://", "https://"); // RNF03: solo HTTPS
        }
        return new LibroDto(
                null,
                v.id(),
                i.title() != null ? recortar(i.title(), 255) : "Sin titulo",
                recortar(autores, 255),
                recortar(i.publisher(), 150),
                recortar(i.publishedDate(), 20),
                portada,
                i.description(),
                categoria,
                i.pageCount()
        );
    }

    private static String recortar(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    // ---- Estructura del JSON de Google Books (solo los campos que usamos) ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VolumesResponse(List<Volume> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Volume(String id, VolumeInfo volumeInfo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VolumeInfo(String title, List<String> authors, String publisher, String publishedDate,
                      String description, Integer pageCount, List<String> categories, ImageLinks imageLinks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageLinks(String smallThumbnail, String thumbnail) {}
}
