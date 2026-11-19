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
import java.util.regex.Pattern;

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
    private static final Pattern ANIO = Pattern.compile("\\d{4}");

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

    public List<LibroDto> buscar(String texto, String tipo, int pagina, Integer anioDesde, Integer anioHasta) {
        if (texto == null || texto.isBlank()) {
            throw ApiException.badRequest("Escribe algo para buscar");
        }
        if (anioDesde != null && anioHasta != null && anioDesde > anioHasta) {
            throw ApiException.badRequest("El ano inicial no puede ser mayor que el ano final");
        }
        String clave = texto.trim().toLowerCase(Locale.ROOT) + "|" + tipo + "|" + pagina
                + "|" + anioDesde + "|" + anioHasta;
        synchronized (cache) {
            Entrada e = cache.get(clave);
            if (e != null && e.creada().plus(VIGENCIA_CACHE).isAfter(Instant.now())) return e.libros();
        }
        List<LibroDto> libros = filtrarPorAnio(consultar(texto, tipo, pagina), anioDesde, anioHasta);
        synchronized (cache) {
            cache.put(clave, new Entrada(libros, Instant.now()));
        }
        return libros;
    }

    /**
     * Filtra la pagina ya descargada. Google Books y Open Library no permiten filtrar
     * por ano en la consulta, asi que se aplica aqui sobre los resultados recibidos.
     */
    private static List<LibroDto> filtrarPorAnio(List<LibroDto> libros, Integer desde, Integer hasta) {
        if (desde == null && hasta == null) return libros;
        return libros.stream().filter(l -> {
            Integer anio = anioDe(l.fechaPublicacion());
            if (anio == null) return false;
            if (desde != null && anio < desde) return false;
            if (hasta != null && anio > hasta) return false;
            return true;
        }).toList();
    }

    /** Extrae el ano de fechas como "2018", "2018-05-01" o "May 2018". */
    private static Integer anioDe(String fecha) {
        if (fecha == null) return null;
        var m = ANIO.matcher(fecha);
        if (!m.find()) return null;
        try {
            return Integer.parseInt(m.group());
        } catch (NumberFormatException e) {
            return null;
        }
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
