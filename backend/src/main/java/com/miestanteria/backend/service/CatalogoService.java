package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Punto unico para buscar libros: primero Google Books (fuente principal, RF03)
 * y, si falla, no hay clave o limita las consultas, Open Library como respaldo.
 * Guarda en memoria las busquedas recientes para responder al instante si se repiten (RNF02).
 */
@Service
public class CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);
    private static final Duration VIGENCIA_CACHE = Duration.ofMinutes(30);
    private static final int MAX_CACHE = 300;

    private final GoogleBooksService google;
    private final OpenLibraryService openLibrary;

    private record Entrada(List<LibroDto> libros, Instant creada) {}

    /** Cache LRU: al pasar de MAX_CACHE se descarta la busqueda usada hace mas tiempo. */
    private final Map<String, Entrada> cache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entrada> eldest) {
            return size() > MAX_CACHE;
        }
    };

    public CatalogoService(GoogleBooksService google, OpenLibraryService openLibrary) {
        this.google = google;
        this.openLibrary = openLibrary;
    }

    public List<LibroDto> buscar(String texto, String tipo, int pagina) {
        if (texto == null || texto.isBlank()) {
            throw ApiException.badRequest("Escribe algo para buscar");
        }
        String clave = texto.trim().toLowerCase(Locale.ROOT) + "|" + tipo + "|" + pagina;
        synchronized (cache) {
            Entrada e = cache.get(clave);
            if (e != null && e.creada().plus(VIGENCIA_CACHE).isAfter(Instant.now())) return e.libros();
        }
        List<LibroDto> libros = consultar(texto, tipo, pagina);
        synchronized (cache) {
            cache.put(clave, new Entrada(libros, Instant.now()));
        }
        return libros;
    }

    private List<LibroDto> consultar(String texto, String tipo, int pagina) {
        if (google.tieneClave()) {
            try {
                return google.buscar(texto, tipo, pagina);
            } catch (ApiException e) {
                if (e.getStatus() != HttpStatus.BAD_GATEWAY) throw e;
                log.info("Google Books no disponible, se usa Open Library para '{}'", texto);
            }
        }
        return openLibrary.buscar(texto, tipo, pagina);
    }

    public LibroDto obtener(String apiId) {
        return OpenLibraryService.esIdOpenLibrary(apiId) ? openLibrary.obtener(apiId) : google.obtener(apiId);
    }
}
