package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Punto unico para buscar libros: primero Google Books (fuente principal, RF03)
 * y, si falla o limita las consultas, Open Library como respaldo.
 */
@Service
public class CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);

    private final GoogleBooksService google;
    private final OpenLibraryService openLibrary;

    public CatalogoService(GoogleBooksService google, OpenLibraryService openLibrary) {
        this.google = google;
        this.openLibrary = openLibrary;
    }

    public List<LibroDto> buscar(String texto, String tipo, int pagina) {
        if (texto == null || texto.isBlank()) {
            throw ApiException.badRequest("Escribe algo para buscar");
        }
        try {
            return google.buscar(texto, tipo, pagina);
        } catch (ApiException e) {
            if (e.getStatus() != HttpStatus.BAD_GATEWAY) throw e;
            log.info("Google Books no disponible, se usa Open Library para '{}'", texto);
            return openLibrary.buscar(texto, tipo, pagina);
        }
    }

    public LibroDto obtener(String apiId) {
        return OpenLibraryService.esIdOpenLibrary(apiId) ? openLibrary.obtener(apiId) : google.obtener(apiId);
    }
}
